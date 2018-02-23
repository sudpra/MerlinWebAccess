package MFClient.Tests.MFDAndSFDConversion;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.WebDriverUtils;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.openqa.selenium.WebDriver;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import MFClient.Pages.ConfigurationPage;
import MFClient.Pages.HomePage;
import MFClient.Pages.LoginPage;
import MFClient.Wrappers.Caption;
import MFClient.Wrappers.ListView;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class ObjectNotValidForMFDAndSFDConversion {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String userName = null;
	public static String password = null;
	public static String testVault = null;
	public static String className = null;
	public static String productVersion = null;
	public static WebDriver driver = null;

	/**
	 * init : Before Class method to perform initial operations.
	 */
	@BeforeClass (alwaysRun=true)
	public void init() throws Exception {

		try {

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			xlTestDataWorkBook = xmlParameters.getParameter("TestData");
			loginURL = xmlParameters.getParameter("webSite");
			userName = xmlParameters.getParameter("UserName");
			password = xmlParameters.getParameter("Password");
			testVault = xmlParameters.getParameter("VaultName");

			className = this.getClass().getSimpleName().toString().trim();
			if (xmlParameters.getParameter("driverType").equalsIgnoreCase("IE"))
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim() + xmlParameters.getParameter("driverVersion").trim();
			else
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim();

			Utility.restoreTestVault();
			Utility.configureUsers(xlTestDataWorkBook);

		} //End try

		catch(Exception e) {
			if (e instanceof SkipException) 
				throw new SkipException(e.getMessage());
			else if (e.getClass().toString().contains("NullPointerException")) 
				throw new Exception ("Test data sheet does not exists.");
			else
				throw e;
		} //End catch

	} //End init

	/**
	 * quitDriver: Quits the driver after the method
	 * @throws Exception
	 */
	@AfterMethod (alwaysRun = true)
	public void quitDriver() throws Exception{

		try {

			if (driver != null)//Checks if driver is not equals to null
				driver.quit();//Quits the driver

			Log.endTestCase();//Ends the test case

		}//End try

		catch(Exception e){
			throw e;
		}//End Catch
	}//End quitDriver

	/**
	 * cleanApp : At after class method to destroy the vault used in the class
	 */
	@AfterClass (alwaysRun = true)
	public void cleanApp() throws Exception{

		try {
			Utility.destroyUsers(xlTestDataWorkBook);
			Utility.destroyTestVault();

		}//End try

		catch(Exception e){
			throw e;
		}//End Catch
	}//End cleanApp


	/**
	 * 32.3.7.5A.1 : Convert to MFD document is not possible for checked in non-document object types in Default layout - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Convert to MFD document is not possible for checked in  non-document object types in Default layout - Operations menu")
	public void SprintTest32_3_7_5A_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			Log.addTestRunMachineInfo(driver);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Default Layout' layout
			//------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. " + Caption.ConfigSettings.Config_Default.Value + " layout is selected and saved.");

			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5 : Select the object
			//--------------------------
			if (ListView.getObjectTypeByItem(driver, dataPool.get("ObjectName")).equalsIgnoreCase("DOCUMENT")) //Checks if an object is not a document object
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is of document object type.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value))
				Log.pass("Test case Passed. Non-Document objects cannot be converted to MFD through operations menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in operations menu for Non-Document objects.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try
			{
				Utility.resetToDefaultLayout(driver);				
			}
			catch(Exception e1)
			{
				Log.exception(e1, driver);
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_7_5A_1

	/**
	 * 32.3.7.5A.2 : Convert to MFD document is not possible for checked in non-document object types in Default with Navigation pane layout - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Convert to MFD document is not possible for checked in  non-document object types in Default with Navigation pane layout - Operations menu")
	public void SprintTest32_3_7_5A_2(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			Log.addTestRunMachineInfo(driver);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Default with Navigation pane Layout' layout
			//----------------------------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_DefaultAndNavigation.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. " + Caption.ConfigSettings.Config_DefaultAndNavigation.Value + " layout is selected and saved.");

			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5 : Select the object
			//--------------------------
			if (ListView.getObjectTypeByItem(driver, dataPool.get("ObjectName")).equalsIgnoreCase("DOCUMENT")) //Checks if an object is not a document object
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is of document object type.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value))
				Log.pass("Test case Passed. Non-Document objects cannot be converted to MFD through operations menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in operations menu for Non-Document objects.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try
			{
				Utility.resetToDefaultLayout(driver);				
			}
			catch(Exception e1)
			{
				Log.exception(e1, driver);
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_7_5A_2

	/**
	 * 32.3.7.5A.3 : Convert to MFD document is not possible for checked in non-document object types in No Java Applet layout - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Convert to MFD document is not possible for checked in  non-document object types in No Java Applet layout - Operations menu")
	public void SprintTest32_3_7_5A_3(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			Log.addTestRunMachineInfo(driver);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Applet Layout' layout
			//----------------------------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);//Disabled the java applet in configuration page

			if(configurationPage.configurationPanel.isJavaAppletEnabled())//Verify if java applet is disabled or not
				throw new Exception("Java applet is enabled.");

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings in configuration page
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. Java applet is disabled and Configuration settings are saved.");

			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5 : Select the object
			//--------------------------
			if (ListView.getObjectTypeByItem(driver, dataPool.get("ObjectName")).equalsIgnoreCase("DOCUMENT")) //Checks if an object is not a document object
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is of document object type.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//--------------------------------------------------------------------------			
			if (!homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value))
				Log.pass("Test case Passed. Non-Document objects cannot be converted to MFD through operations menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in operations menu for Non-Document objects.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try
			{
				Utility.resetToDefaultLayout(driver);				
			}
			catch(Exception e1)
			{
				Log.exception(e1, driver);
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_7_5A_3

	/**
	 * 32.3.7.5A.4 : Convert to MFD document is not possible for checked in non-document object types in No Java Applet, No Task area layout - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Convert to MFD document is not possible for checked in  non-document object types in No Java Applet, No Task area layout - Operations menu")
	public void SprintTest32_3_7_5A_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {


			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			Log.addTestRunMachineInfo(driver);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Applet, No Task area Layout' layout
			//----------------------------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);

			if(configurationPage.configurationPanel.isJavaAppletEnabled())
				throw new Exception("Java applet is enabled");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_NoTaskArea.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. No java applet and no task area layout is selected and saved.");

			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5 : Select the object
			//--------------------------
			if (ListView.getObjectTypeByItem(driver, dataPool.get("ObjectName")).equalsIgnoreCase("DOCUMENT")) //Checks if an object is not a document object
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is of document object type.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value))
				Log.pass("Test case Passed. Non-Document objects cannot be converted to MFD through operations menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in operations menu for Non-Document objects.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try
			{
				Utility.resetToDefaultLayout(driver);				
			}
			catch(Exception e1)
			{
				Log.exception(e1, driver);
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_7_5A_4

	/**
	 * 32.3.7.5A.5 : Convert to MFD document is not possible for checked in non-document object types in No Java Applet, No Task area but show GoTo shortcuts layout - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Convert to MFD document is not possible for checked in  non-document object types in No Java Applet, No Task area but show GoTo shortcuts  layout - Operations menu")
	public void SprintTest32_3_7_5A_5(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {


			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			Log.addTestRunMachineInfo(driver);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Applet, No Task area but show GoTo shortcuts Layout' layout
			//----------------------------------------------------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);

			if(configurationPage.configurationPanel.isJavaAppletEnabled())
				throw new Exception("Java applet is enabled");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_TaskAreaWithShowGoTo.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. No java applet with task area with go to layout is selected and saved.");

			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5 : Select the object
			//--------------------------
			if (ListView.getObjectTypeByItem(driver, dataPool.get("ObjectName")).equalsIgnoreCase("DOCUMENT")) //Checks if an object is not a document object
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is of document object type.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value))
				Log.pass("Test case Passed. Non-Document objects cannot be converted to MFD through operations menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in operations menu for Non-Document objects.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try
			{
				Utility.resetToDefaultLayout(driver);				
			}
			catch(Exception e1)
			{
				Log.exception(e1, driver);
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_7_5A_5

	/**
	 * 32.3.7.5B.1 : Convert to MFD document is not possible for non-document object types in Default layout - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Convert to MFD document is not possible for non-document object types in Default layout - Context menu")
	public void SprintTest32_3_7_5B_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {


			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			Log.addTestRunMachineInfo(driver);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Default Layout' layout
			//------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. " + Caption.ConfigSettings.Config_Default.Value + " layout is selected and saved.");

			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5 : Select the object
			//--------------------------
			if (ListView.getObjectTypeByItem(driver, dataPool.get("ObjectName")).equalsIgnoreCase("DOCUMENT")) //Checks if an object is not a document object
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is of document object type.");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is rightClicked.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.ConvertToMFD_C.Value))
				Log.pass("Test case Passed. Non-Document objects cannot be converted to MFD through context menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in context menu for Non-Document objects.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try
			{
				Utility.resetToDefaultLayout(driver);				
			}
			catch(Exception e1)
			{
				Log.exception(e1, driver);
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_7_5B_1

	/**
	 * 32.3.7.5B.2 : Convert to MFD document is not possible for non-document object types in Default with Navigation pane layout - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Convert to MFD document is not possible for non-document object types in Default with Navigation pane layout - Context menu")
	public void SprintTest32_3_7_5B_2(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			Log.addTestRunMachineInfo(driver);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Default with Navigation Pane Layout' layout
			//----------------------------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_DefaultAndNavigation.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. " + Caption.ConfigSettings.Config_DefaultAndNavigation.Value + " layout is selected and saved.");

			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5 : Select the object
			//--------------------------
			if (ListView.getObjectTypeByItem(driver, dataPool.get("ObjectName")).equalsIgnoreCase("DOCUMENT")) //Checks if an object is not a document object
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is of document object type.");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is rightClicked.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.ConvertToMFD_C.Value))
				Log.pass("Test case Passed. Non-Document objects cannot be converted to MFD through context menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in context menu for Non-Document objects.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try
			{
				Utility.resetToDefaultLayout(driver);				
			}
			catch(Exception e1)
			{
				Log.exception(e1, driver);
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_7_5B_2

	/**
	 * 32.3.7.5B.3 : Convert to MFD document is not possible for non-document object types in No Java Applet layout - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Convert to MFD document is not possible for non-document object types in No Java Applet layout- Context menu")
	public void SprintTest32_3_7_5B_3(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			Log.addTestRunMachineInfo(driver);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Applet Layout' layout
			//----------------------------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);//Disabled the java applet in configuration page

			if(configurationPage.configurationPanel.isJavaAppletEnabled())//Verify if java applet is disabled or not
				throw new Exception("Java applet is enabled.");

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings in configuration page
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. Java applet is disabled and Configuration settings are saved.");

			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5 : Select the object
			//--------------------------
			if (ListView.getObjectTypeByItem(driver, dataPool.get("ObjectName")).equalsIgnoreCase("DOCUMENT")) //Checks if an object is not a document object
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is of document object type.");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is rightClicked.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.ConvertToMFD_C.Value))
				Log.pass("Test case Passed. Non-Document objects cannot be converted to MFD through context menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in context menu for Non-Document objects.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try
			{
				Utility.resetToDefaultLayout(driver);				
			}
			catch(Exception e1)
			{
				Log.exception(e1, driver);
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_7_5B_3

	/**
	 * 32.3.7.5B.4 : Convert to MFD document is not possible for non-document object types in No Java Applet, No Task Area layout - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Convert to MFD document is not possible for non-document object types in No Java Applet, No Task Area layout- Context menu")
	public void SprintTest32_3_7_5B_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			Log.addTestRunMachineInfo(driver);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Applet, No Task Area Layout' layout
			//----------------------------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);

			if(configurationPage.configurationPanel.isJavaAppletEnabled())
				throw new Exception("Java applet is enabled");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_NoTaskArea.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. No java applet and no task area layout is selected and saved.");


			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5 : Select the object
			//--------------------------
			if (ListView.getObjectTypeByItem(driver, dataPool.get("ObjectName")).equalsIgnoreCase("DOCUMENT")) //Checks if an object is not a document object
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is of document object type.");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is rightClicked.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.ConvertToMFD_C.Value))
				Log.pass("Test case Passed. Non-Document objects cannot be converted to MFD through context menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in context menu for Non-Document objects.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try
			{
				Utility.resetToDefaultLayout(driver);				
			}
			catch(Exception e1)
			{
				Log.exception(e1, driver);
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_7_5B_4

	/**
	 * 32.3.7.5B.5 : Convert to MFD document is not possible for non-document object types in No Java Applet, No Task Area but show GoTo shortcuts layout - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Convert to MFD document is not possible for non-document object types in No Java Applet, No Task Area but show GoTo shortcuts layout- Context menu")
	public void SprintTest32_3_7_5B_5(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			Log.addTestRunMachineInfo(driver);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Applet, No Task Area Layout' layout
			//----------------------------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);

			if(configurationPage.configurationPanel.isJavaAppletEnabled())
				throw new Exception("Java applet is enabled");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_TaskAreaWithShowGoTo.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. No java applet with task area with go to layout is selected and saved.");

			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5 : Select the object
			//--------------------------
			if (ListView.getObjectTypeByItem(driver, dataPool.get("ObjectName")).equalsIgnoreCase("DOCUMENT")) //Checks if an object is not a document object
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is of document object type.");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is rightClicked.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.ConvertToMFD_C.Value))
				Log.pass("Test case Passed. Non-Document objects cannot be converted to MFD through context menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in context menu for Non-Document objects.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try
			{
				Utility.resetToDefaultLayout(driver);				
			}
			catch(Exception e1)
			{
				Log.exception(e1, driver);
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_7_5B_5

	/**
	 * 32.3.7.5B.6 : Convert to MFD document is not possible for non-document object types in Listing and Properties pane only layout - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Convert to MFD document is not possible for non-document object types in Listing and Properties pane only layout- Context menu")
	public void SprintTest32_3_7_5B_6(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			Log.addTestRunMachineInfo(driver);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Listing and Properties pane only Layout' layout
			//----------------------------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_ListingPropertiesPaneOnly.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. " + Caption.ConfigSettings.Config_ListingPropertiesPaneOnly.Value + " layout is selected and saved.");

			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5 : Select the object
			//--------------------------
			if (ListView.getObjectTypeByItem(driver, dataPool.get("ObjectName")).equalsIgnoreCase("DOCUMENT")) //Checks if an object is not a document object
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is of document object type.");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is rightClicked.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.ConvertToMFD_C.Value))
				Log.pass("Test case Passed. Non-Document objects cannot be converted to MFD through context menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in context menu for Non-Document objects.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try
			{
				Utility.resetToDefaultLayout(driver);				
			}
			catch(Exception e1)
			{
				Log.exception(e1, driver);
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_7_5B_6

	/**
	 * 32.3.7.5B.7 : Convert to MFD document is not possible for non-document object types in Listing pane only layout - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Convert to MFD document is not possible for non-document object types in Listing pane only layout- Context menu")
	public void SprintTest32_3_7_5B_7(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			Log.addTestRunMachineInfo(driver);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Listing pane only Layout' layout
			//----------------------------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_ListingPaneOnly.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. " + Caption.ConfigSettings.Config_ListingPaneOnly.Value + " layout is selected and saved.");

			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5 : Select the object
			//--------------------------
			if (ListView.getObjectTypeByItem(driver, dataPool.get("ObjectName")).equalsIgnoreCase("DOCUMENT")) //Checks if an object is not a document object
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is of document object type.");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is rightClicked.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.ConvertToMFD_C.Value))
				Log.pass("Test case Passed. Non-Document objects cannot be converted to MFD through context menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in context menu for Non-Document objects.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try
			{
				Utility.resetToDefaultLayout(driver);				
			}
			catch(Exception e1)
			{
				Log.exception(e1, driver);
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_7_5B_7

	/**
	 * 32.3.7.5C : Convert to MFD document is not possible for checked out non-document object types - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Convert to MFD document is not possible for checked out non-document object types - Operations menu")
	public void SprintTest32_3_7_5C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			Log.addTestRunMachineInfo(driver);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (ListView.getObjectTypeByItem(driver, dataPool.get("ObjectName")).equalsIgnoreCase("DOCUMENT")) //Checks if an object is not a document object
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is of document object type.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel


			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if it is in checked in state
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-3 : Select the object
			//--------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("3. Checked out Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value))
				Log.pass("Test case Passed. Checked out Non-Document objects cannot be converted to MFD through operations menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in operations menu for Checked out non-document objects.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_7_5C

	/**
	 * 32.3.7.5D : Convert to MFD document is not possible for checked out non-document object types - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Convert to MFD document is not possible for checked out non-document object types - Context menu")
	public void SprintTest32_3_7_5D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {


			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			Log.addTestRunMachineInfo(driver);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (ListView.getObjectTypeByItem(driver, dataPool.get("ObjectName")).equalsIgnoreCase("DOCUMENT")) //Checks if an object is not a document object
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is of document object type.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel


			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if it is in checked in state
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-3 : Select the object
			//--------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("3. Checked out Object (" + dataPool.get("ObjectName") + ") is right clicked.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.ConvertToMFD_C.Value))
				Log.pass("Test case Passed. Checked out Non-Document objects cannot be converted to MFD through context menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in context menu for Checked out non-document objects.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_7_5D

	/**
	 * 32.3.7.6A : Convert to MFD document is not possible for checked in MFD document - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Convert to MFD document is not possible for checked in MFD document - Operations menu")
	public void SprintTest32_3_7_6A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			Log.addTestRunMachineInfo(driver);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object
			//--------------------------
			if (ListView.isSFDByItemName(driver, dataPool.get("ObjectName")))  //Checks if an object MFD
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is a Single file Document.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value))
				Log.pass("Test case Passed. MFD cannot be converted to MFD through operations menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in operations menu for MFD.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_7_6A

	/**
	 * 32.3.7.6B : Convert to MFD document is not possible for checked in MFD document - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Convert to MFD document is not possible for checked in MFD document - Context menu")
	public void SprintTest32_3_7_6B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			Log.addTestRunMachineInfo(driver);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object
			//--------------------------
			if (ListView.isSFDByItemName(driver, dataPool.get("ObjectName")))  //Checks if an object MFD
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is a Single file Document.");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.ConvertToMFD_C.Value))
				Log.pass("Test case Passed. MFD cannot be converted to MFD through context menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in context menu for MFD.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_7_6B

	/**
	 * 32.3.7.6C : Convert to MFD document is not possible for checked out MFD document - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Convert to MFD document is not possible for checked out MFD document - Operations menu")
	public void SprintTest32_3_7_6C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			Log.addTestRunMachineInfo(driver);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (ListView.isSFDByItemName(driver, dataPool.get("ObjectName"))) //Checks if an object MFD
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is a Single file Document.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel


			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if it is in checked in state
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-3 : Select the object
			//--------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("3. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value))
				Log.pass("Test case Passed. MFD cannot be converted to MFD through operations menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in operations menu for MFD.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_7_6C

	/**
	 * 32.3.7.6D : Convert to MFD document is not possible for checked out MFD document - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Convert to MFD document is not possible for checked out MFD document - Context menu")
	public void SprintTest32_3_7_6D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			Log.addTestRunMachineInfo(driver);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (ListView.isSFDByItemName(driver, dataPool.get("ObjectName"))) //Checks if an object MFD
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is a Single file Document.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel


			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if it is in checked in state
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-3 : Select the object
			//--------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("3. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.ConvertToMFD_C.Value))
				Log.pass("Test case Passed. MFD cannot be converted to MFD through context menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in context menu for MFD.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_7_6D


	/**
	 * 32.3.8.1C : Convert to SFD document is not possible for checked out multi file MFD document - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Convert to SFD document is not possible for checked out multi file MFD document - Operations menu")
	public void SprintTest32_3_8_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			Log.addTestRunMachineInfo(driver);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Check if object is multi file MFD document 
			//-----------------------------------------------------
			if (!ListView.openMFDByItemName(driver, dataPool.get("ObjectName")))  //Checks if an object MFD
				throw new Exception("MFD document (" + dataPool.get("ObjectName") + ") is not opened to check if it is multi file mfd document.");

			if (homePage.listView.itemCount() <= 1)
				throw new Exception("MFD document (" + dataPool.get("ObjectName") + ") is not multi file MFD document.");

			homePage.listView.clickBackToViewButton(); //Navigates back to the view


			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is verifed that it is multi file MFD document.");

			//Step-3 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel


			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if it is in checked in state
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("3. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-4 : Select multi-file MFD document
			//---------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("4. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Verification : To Verify if MFD to SFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.ConvertToSFD_O.Value))
				Log.pass("Test case Passed. Checked out Mutli-file MFD cannot be converted to SFD through operations menu.");
			else
				Log.fail("Test case Failed. Convert MFD to SFD is enabled in operations menu for checked out multi file MFD document.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_8_1C

	/**
	 * 32.3.8.1D : Convert to SFD document is not possible for checked out multi file MFD document - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Convert to SFD document is not possible for checked out multi file MFD document - Context menu")
	public void SprintTest32_3_8_1D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			Log.addTestRunMachineInfo(driver);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Check if object is multi file MFD document 
			//-----------------------------------------------------
			if (!ListView.openMFDByItemName(driver, dataPool.get("ObjectName")))  //Checks if an object MFD
				throw new Exception("MFD document (" + dataPool.get("ObjectName") + ") is not opened to check if it is multi file mfd document.");

			if (homePage.listView.itemCount() <= 1)
				throw new Exception("MFD document (" + dataPool.get("ObjectName") + ") is not multi file MFD document.");

			homePage.listView.clickBackToViewButton(); //Navigates back to the view


			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is verifed that it is multi file MFD document.");

			//Step-3 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel


			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if it is in checked in state
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("3. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-4 : Select multi-file MFD document
			//---------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("4. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Verification : To Verify if MFD to SFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.ConvertToSFD_C.Value))
				Log.pass("Test case Passed. Checked out Mutli-file MFD cannot be converted to SFD through context menu.");
			else
				Log.fail("Test case Failed. Convert MFD to SFD is enabled in context menu for checked out multi file MFD document.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_8_1D

	/**
	 * 32.3.8.2A : Convert to SFD document is not possible for checked in non-document objects - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Convert to SFD document is not possible for checked in non-document objects - Operations menu")
	public void SprintTest32_3_8_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			Log.addTestRunMachineInfo(driver);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object
			//--------------------------
			if (ListView.getObjectTypeByItem(driver, dataPool.get("ObjectName")).equalsIgnoreCase("DOCUMENT")) //Checks if an object is not a document object
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is of document object type.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Verification : To Verify if MFD to SFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.ConvertToSFD_O.Value))
				Log.pass("Test case Passed. Checked in non document object cannot be converted to SFD through operations menu.");
			else
				Log.fail("Test case Failed. Convert MFD to SFD is enabled in operations menu for checked in non document object.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_8_2A

	/**
	 * 32.3.8.2B : Convert to SFD document is not possible for checked in non-document objects - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Convert to SFD document is not possible for checked in non-document objects - Context menu")
	public void SprintTest32_3_8_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			Log.addTestRunMachineInfo(driver);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object
			//--------------------------
			if (ListView.getObjectTypeByItem(driver, dataPool.get("ObjectName")).equalsIgnoreCase("DOCUMENT")) //Checks if an object is not a document object
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is of document object type.");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Verification : To Verify if MFD to SFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.ConvertToSFD_C.Value))
				Log.pass("Test case Passed. Checked in non-document cannot be converted to SFD through context menu.");
			else
				Log.fail("Test case Failed. Convert MFD to SFD is enabled in context menu for checked in non-document.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch 

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_8_2B

	/**
	 * 32.3.8.2C : Convert to SFD document is not possible for checked out non-document objects - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Convert to SFD document is not possible for checked out non-document objects - Operations menu")
	public void SprintTest32_3_8_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			Log.addTestRunMachineInfo(driver);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel


			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if it is in checked in state
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-3 : Select the object
			//--------------------------
			if (ListView.getObjectTypeByItem(driver, dataPool.get("ObjectName")).equalsIgnoreCase("DOCUMENT")) //Checks if an object is not a document object
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is of document object type.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("3. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Verification : To Verify if MFD to SFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.ConvertToSFD_O.Value))
				Log.pass("Test case Passed. Checked out non document object cannot be converted to SFD through operations menu.");
			else
				Log.fail("Test case Failed. Convert MFD to SFD is enabled in operations menu for checked out non document object.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_8_2C

	/**
	 * 32.3.8.2D : Convert to SFD document is not possible for checked out non-document objects - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Convert to SFD document is not possible for checked out non-document objects - Context menu")
	public void SprintTest32_3_8_2D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			Log.addTestRunMachineInfo(driver);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel


			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if it is in checked in state
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-3 : Select the object
			//--------------------------
			if (ListView.getObjectTypeByItem(driver, dataPool.get("ObjectName")).equalsIgnoreCase("DOCUMENT")) //Checks if an object is not a document object
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is of document object type.");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("3. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Verification : To Verify if MFD to SFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.ConvertToSFD_O.Value))
				Log.pass("Test case Passed. Checked out non document object cannot be converted to SFD through context menu.");
			else
				Log.fail("Test case Failed. Convert MFD to SFD is enabled in context menu for checked out non document object.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_8_2D



} //End class ObjectNotValidForMFDAndSFDConversion

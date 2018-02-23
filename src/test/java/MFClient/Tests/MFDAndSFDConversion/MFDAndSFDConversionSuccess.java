package MFClient.Tests.MFDAndSFDConversion;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.Utils;
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
public class MFDAndSFDConversionSuccess {

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
	 * 32.3.7.2A.1 : Convert checked out SFD to MFD document in default layout - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32", "Smoke"}, 
			description = "Convert checked out SFD to MFD document in default layout - Operations menu")
	public void SprintTest32_3_7_2A_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

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

			//Step-5 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!ListView.isSFDByItemName(driver, dataPool.get("ObjectName"))) //Checks if this is SFD
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not Single file document.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if it is in checked in state
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-6 : Select Convert to MFD from operations menu
			//---------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value); //Clicks Convert SFD to MFD option from operations menu
			Utils.fluentWait(driver);

			String mfdName = dataPool.get("ObjectName").split("\\.")[0];

			Log.message("6. Convert SFD to MFD option is selected from operations menu for SFD (" + dataPool.get("ObjectName") + ").");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!ListView.isSFDByItemName(driver, mfdName))
				Log.pass("Test case Passed. Checked out SFD is converted to MFD through operations menu.");
			else
				Log.fail("Test case Failed. Checked out SFD is not converted to MFD through operations menu.", driver);
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

	} //End SprintTest32_3_7_2A_1

	/**
	 * 32.3.7.2A.2 : Convert checked out SFD to MFD document in default with navigation pane layout - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32", "Smoke"}, 
			description = "Convert checked out SFD to MFD document in default with navigation pane layout - Operations menu")
	public void SprintTest32_3_7_2A_2(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Default with navigation pane Layout' layout
			//------------------------------------------------
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

			//Step-5 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!ListView.isSFDByItemName(driver, dataPool.get("ObjectName"))) //Checks if this is SFD
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not Single file document.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if it is in checked in state
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-6 : Select Convert to MFD from operations menu
			//---------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value); //Clicks Convert SFD to MFD option from operations menu
			Utils.fluentWait(driver);

			String mfdName = dataPool.get("ObjectName").split("\\.")[0];

			Log.message("6. Convert SFD to MFD option is selected from operations menu for SFD (" + dataPool.get("ObjectName") + ").");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!ListView.isSFDByItemName(driver, mfdName))
				Log.pass("Test case Passed. Checked out SFD is converted to MFD through operations menu.");
			else
				Log.fail("Test case Failed. Checked out SFD is not converted to MFD through operations menu.", driver);
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

	} //End SprintTest32_3_7_2A_2

	/**
	 * 32.3.7.2A.3 : Convert checked out SFD to MFD document in No Java Applet layout - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32", "Smoke"}, 
			description = "Convert checked out SFD to MFD document in No Java Applet layout - Operations menu")
	public void SprintTest32_3_7_2A_3(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Applet Layout' layout
			//------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);//Disabled the java applet in configuration page

			if(configurationPage.configurationPanel.isJavaAppletEnabled())//Verify if java applet is disabled or not
				throw new Exception("Java applet is enabled.");

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings in configuration page
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. Java applet is enabled and Configuration settings are saved.");

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

			//Step-5 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!ListView.isSFDByItemName(driver, dataPool.get("ObjectName"))) //Checks if this is SFD
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not Single file document.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if it is in checked in state
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-6 : Select Convert to MFD from operations menu
			//---------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value); //Clicks Convert SFD to MFD option from operations menu
			Utils.fluentWait(driver);

			String mfdName = dataPool.get("ObjectName").split("\\.")[0];

			Log.message("6. Convert SFD to MFD option is selected from operations menu for SFD (" + dataPool.get("ObjectName") + ").");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!ListView.isSFDByItemName(driver, mfdName))
				Log.pass("Test case Passed. Checked out SFD is converted to MFD through operations menu.");
			else
				Log.fail("Test case Failed. Checked out SFD is not converted to MFD through operations menu.", driver);
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

	} //End SprintTest32_3_7_2A_3

	/**
	 * 32.3.7.2A.4 : Convert checked out SFD to MFD document in No Java Applet, No task area layout - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32", "Smoke"}, 
			description = "Convert checked out SFD to MFD document in No Java Applet, No task area layout - Operations menu")
	public void SprintTest32_3_7_2A_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Applet, No task area Layout' layout
			//-----------------------------------------------------------------------
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

			//Step-5 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!ListView.isSFDByItemName(driver, dataPool.get("ObjectName"))) //Checks if this is SFD
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not Single file document.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if it is in checked in state
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-6 : Select Convert to MFD from operations menu
			//---------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value); //Clicks Convert SFD to MFD option from operations menu
			Utils.fluentWait(driver);

			String mfdName = dataPool.get("ObjectName").split("\\.")[0];

			Log.message("6. Convert SFD to MFD option is selected from operations menu for SFD (" + dataPool.get("ObjectName") + ").");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!ListView.isSFDByItemName(driver, mfdName))
				Log.pass("Test case Passed. Checked out SFD is converted to MFD through operations menu.");
			else
				Log.fail("Test case Failed. Checked out SFD is not converted to MFD through operations menu.", driver);
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

	} //End SprintTest32_3_7_2A_4

	/**
	 * 32.3.7.2A.5 : Convert checked out SFD to MFD document in No Java Applet, No task area but show GoTo Shortcuts layout - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32", "Smoke"}, 
			description = "Convert checked out SFD to MFD document in No Java Applet, No task area but show GoTo shortcuts layout - Operations menu")
	public void SprintTest32_3_7_2A_5(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Applet, No task area, but show GoTo shortcuts Layout' layout
			//-----------------------------------------------------------------------------------------------
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

			//Step-5 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!ListView.isSFDByItemName(driver, dataPool.get("ObjectName"))) //Checks if this is SFD
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not Single file document.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if it is in checked in state
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-6 : Select Convert to MFD from operations menu
			//---------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value); //Clicks Convert SFD to MFD option from operations menu
			Utils.fluentWait(driver);

			String mfdName = dataPool.get("ObjectName").split("\\.")[0];

			Log.message("6. Convert SFD to MFD option is selected from operations menu for SFD (" + dataPool.get("ObjectName") + ").");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!ListView.isSFDByItemName(driver, mfdName))
				Log.pass("Test case Passed. Checked out SFD is converted to MFD through operations menu.");
			else
				Log.fail("Test case Failed. Checked out SFD is not converted to MFD through operations menu.", driver);
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

	} //End SprintTest32_3_7_2A_5

	/**
	 * 32.3.7.2B.1 : Convert checked out SFD to MFD document in default layout - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32", "Smoke"}, 
			description = "Convert checked out SFD to MFD document in default layout - Context menu")
	public void SprintTest32_3_7_2B_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

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

			//Step-4 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!ListView.isSFDByItemName(driver, dataPool.get("ObjectName"))) //Checks if this is SFD
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not Single file document.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if it is in checked in state
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-5 : Select Convert to MFD from context menu
			//---------------------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.ConvertToMFD_C.Value); //Clicks Convert SFD to MFD option from context menu
			Utils.fluentWait(driver);

			String mfdName = dataPool.get("ObjectName").split("\\.")[0];

			Log.message("6. Convert SFD to MFD option is selected from operations menu for SFD (" + dataPool.get("ObjectName") + ").");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!ListView.isSFDByItemName(driver, mfdName))
				Log.pass("Test case Passed. Checked out SFD is converted to MFD through context menu.");
			else
				Log.fail("Test case Failed. Checked out SFD is not converted to MFD through context menu.", driver);
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

	} //End SprintTest32_3_7_2B_1

	/**
	 * 32.3.7.2B.2 : Convert checked out SFD to MFD document in default with navigation pane layout - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32", "Smoke"}, 
			description = "Convert checked out SFD to MFD document in default with navigation pane layout - Context menu")
	public void SprintTest32_3_7_2B_2(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Default Layout' layout
			//------------------------------------------------
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

			//Step-4 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!ListView.isSFDByItemName(driver, dataPool.get("ObjectName"))) //Checks if this is SFD
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not Single file document.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if it is in checked in state
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-5 : Select Convert to MFD from context menu
			//---------------------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.ConvertToMFD_C.Value); //Clicks Convert SFD to MFD option from context menu
			Utils.fluentWait(driver);

			String mfdName = dataPool.get("ObjectName").split("\\.")[0];

			Log.message("6. Convert SFD to MFD option is selected from operations menu for SFD (" + dataPool.get("ObjectName") + ").");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!ListView.isSFDByItemName(driver, mfdName))
				Log.pass("Test case Passed. Checked out SFD is converted to MFD through context menu.");
			else
				Log.fail("Test case Failed. Checked out SFD is not converted to MFD through context menu.", driver);
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

	} //End SprintTest32_3_7_2B_2

	/**
	 * 32.3.7.2B.3 : Convert checked out SFD to MFD document in No Java Applet layout - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32", "Smoke"}, 
			description = "Convert checked out SFD to MFD document in No Java Applet layout - Context menu")
	public void SprintTest32_3_7_2B_3(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Applet Layout' layout
			//---------------------------------------------------------
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

			//Step-4 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!ListView.isSFDByItemName(driver, dataPool.get("ObjectName"))) //Checks if this is SFD
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not Single file document.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if it is in checked in state
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-5 : Select Convert to MFD from context menu
			//---------------------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.ConvertToMFD_C.Value); //Clicks Convert SFD to MFD option from context menu
			Utils.fluentWait(driver);

			String mfdName = dataPool.get("ObjectName").split("\\.")[0];

			Log.message("6. Convert SFD to MFD option is selected from operations menu for SFD (" + dataPool.get("ObjectName") + ").");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!ListView.isSFDByItemName(driver, mfdName))
				Log.pass("Test case Passed. Checked out SFD is converted to MFD through context menu.");
			else
				Log.fail("Test case Failed. Checked out SFD is not converted to MFD through context menu.", driver);
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

	} //End SprintTest32_3_7_2B_3

	/**
	 * 32.3.7.2B.4 : Convert checked out SFD to MFD document in No Java Applet, No Task Area layout - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32", "Smoke"}, 
			description = "Convert checked out SFD to MFD document in No Java Applet, No Task Area layout - Context menu")
	public void SprintTest32_3_7_2B_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Applet, No Task Area Layout' layout
			//-----------------------------------------------------------------------
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

			//Step-4 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!ListView.isSFDByItemName(driver, dataPool.get("ObjectName"))) //Checks if this is SFD
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not Single file document.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if it is in checked in state
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-5 : Select Convert to MFD from context menu
			//---------------------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.ConvertToMFD_C.Value); //Clicks Convert SFD to MFD option from context menu
			Utils.fluentWait(driver);

			String mfdName = dataPool.get("ObjectName").split("\\.")[0];

			Log.message("6. Convert SFD to MFD option is selected from operations menu for SFD (" + dataPool.get("ObjectName") + ").");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!ListView.isSFDByItemName(driver, mfdName))
				Log.pass("Test case Passed. Checked out SFD is converted to MFD through context menu.");
			else
				Log.fail("Test case Failed. Checked out SFD is not converted to MFD through context menu.", driver);
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

	} //End SprintTest32_3_7_2B_4

	/**
	 * 32.3.7.2B.5 : Convert checked out SFD to MFD document in No Java Applet, No Task Area but show GoTo shortcuts layout - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32", "Smoke"}, 
			description = "Convert checked out SFD to MFD document in No Java Applet, No Task Area but show GoTo shortcuts layout - Context menu")
	public void SprintTest32_3_7_2B_5(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Applet, No Task Area but show GoTo shortcuts Layout' layout
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

			//Step-4 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!ListView.isSFDByItemName(driver, dataPool.get("ObjectName"))) //Checks if this is SFD
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not Single file document.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if it is in checked in state
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-5 : Select Convert to MFD from context menu
			//---------------------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.ConvertToMFD_C.Value); //Clicks Convert SFD to MFD option from context menu
			Utils.fluentWait(driver);

			String mfdName = dataPool.get("ObjectName").split("\\.")[0];

			Log.message("6. Convert SFD to MFD option is selected from operations menu for SFD (" + dataPool.get("ObjectName") + ").");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!ListView.isSFDByItemName(driver, mfdName))
				Log.pass("Test case Passed. Checked out SFD is converted to MFD through context menu.");
			else
				Log.fail("Test case Failed. Checked out SFD is not converted to MFD through context menu.", driver);
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

	} //End SprintTest32_3_7_2B_5

	/**
	 * 32.3.7.2B.6 : Convert checked out SFD to MFD document in Listing and Properties Pane only layout - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32", "Smoke"}, 
			description = "Convert checked out SFD to MFD document in Listing and Properties Pane only layout - Context menu")
	public void SprintTest32_3_7_2B_6(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Listing and Properties Pane only Layout' layout
			//----------------------------------------------------------------------------------------------
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

			//Step-4 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!ListView.isSFDByItemName(driver, dataPool.get("ObjectName"))) //Checks if this is SFD
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not Single file document.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if it is in checked in state
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-5 : Select Convert to MFD from context menu
			//---------------------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.ConvertToMFD_C.Value); //Clicks Convert SFD to MFD option from context menu
			Utils.fluentWait(driver);

			String mfdName = dataPool.get("ObjectName").split("\\.")[0];

			Log.message("5. Convert SFD to MFD option is selected from operations menu for SFD (" + dataPool.get("ObjectName") + ").");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!ListView.isSFDByItemName(driver, mfdName))
				Log.pass("Test case Passed. Checked out SFD is converted to MFD through context menu.");
			else
				Log.fail("Test case Failed. Checked out SFD is not converted to MFD through context menu.", driver);
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

	} //End SprintTest32_3_7_2B_6

	/**
	 * 32.3.7.2B.7 : Convert checked out SFD to MFD document in Listing Pane only layout - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32", "Smoke"}, 
			description = "Convert checked out SFD to MFD document in Listing only layout - Context menu")
	public void SprintTest32_3_7_2B_7(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Listing and Properties Pane only Layout' layout
			//----------------------------------------------------------------------------------------------
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

			//Step-4 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!ListView.isSFDByItemName(driver, dataPool.get("ObjectName"))) //Checks if this is SFD
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not Single file document.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if it is in checked in state
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-5 : Select Convert to MFD from context menu
			//---------------------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.ConvertToMFD_C.Value); //Clicks Convert SFD to MFD option from context menu
			Utils.fluentWait(driver);

			String mfdName = dataPool.get("ObjectName").split("\\.")[0];

			Log.message("6. Convert SFD to MFD option is selected from operations menu for SFD (" + dataPool.get("ObjectName") + ").");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!ListView.isSFDByItemName(driver, mfdName))
				Log.pass("Test case Passed. Checked out SFD is converted to MFD through context menu.");
			else
				Log.fail("Test case Failed. Checked out SFD is not converted to MFD through context menu.", driver);
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

	} //End SprintTest32_3_7_2B_7


	/**
	 * 32.3.8.4A. : Convert checked out Single file MFD document to SFD document - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32", "Bug"}, 
			description = "Convert checked out Single file MFD document to SFD document - Operations menu")
	public void SprintTest32_3_8_4A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!ListView.isSFDByItemName(driver, dataPool.get("ObjectName"))) //Checks if this is SFD
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not Single file document.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if it is in checked in state
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-3 : Select Convert to MFD from operations menu
			//---------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value); //Clicks Convert SFD to MFD option from operations menu
			Utils.fluentWait(driver);
			String mfdName = dataPool.get("ObjectName").split("\\.")[0];

			if (ListView.isSFDByItemName(driver, mfdName))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not converted to MFD document.");

			Log.message("3. Object (" + dataPool.get("ObjectName") + ") converted to MFD document.");

			//Step-4 : Select MFD and Convert to SFD from opeartions menu
			//---------------------------------------------------------
			if (!homePage.listView.clickItem(mfdName)) //Selects the Object in the list
				throw new Exception("Object (" + mfdName + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.ConvertToSFD_O.Value); //Clicks Convert SFD to MFD option from operations menu
			Utils.fluentWait(driver);

			Log.message("4. Convert MFD to SFD option is selected from operations menu for Single file MFD (" + mfdName + ").");

			//Verification : To Verify if MFD is converted to SFD
			//---------------------------------------------------			
			if (ListView.isSFDByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Checked out Single file MFD is converted to SFD through operations menu.");
			else
				Log.fail("Test case Failed. Checked out Single file MFD is not converted to SFD through operations menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_8_4A

	/**
	 * 32.3.8.4B : Convert checked out Single file MFD document to SFD document - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32", "Bug"}, 
			description = "Convert checked out Single file MFD document to SFD document - Context menu")
	public void SprintTest32_3_8_4B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!ListView.isSFDByItemName(driver, dataPool.get("ObjectName"))) //Checks if this is SFD
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not Single file document.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if it is in checked in state
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-3 : Select Convert to MFD from operations menu
			//---------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value); //Clicks Convert SFD to MFD option from operations menu
			Utils.fluentWait(driver);
			String mfdName = dataPool.get("ObjectName").split("\\.")[0];

			if (ListView.isSFDByItemName(driver, mfdName))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not converted to MFD document.");

			Log.message("3. Object (" + dataPool.get("ObjectName") + ") converted to MFD document.");

			//Step-4 : Select MFD and Convert to SFD from operations menu
			//---------------------------------------------------------
			if (!homePage.listView.rightClickItem(mfdName)) //Selects the Object in the list
				throw new Exception("Object (" + mfdName + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.ConvertToSFD_C.Value); //Clicks Convert SFD to MFD option from operations menu
			Utils.fluentWait(driver);

			Log.message("4. Convert MFD to SFD option is selected from operations menu for MFD (" + mfdName + ").");

			//Verification : To Verify if MFD is converted to SFD
			//---------------------------------------------------			
			if (ListView.isSFDByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Checked out Single file MFD is converted to SFD through operations menu.");
			else
				Log.fail("Test case Failed. Checked out Single file MFD is not converted to SFD through operations menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_8_4B

} //End class MFDAndSFDConversionSuccess

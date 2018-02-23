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
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class TryMFDAndSFDConversionAfterCheckoutCheckIn {

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
	 * 32.3.7.3A.1 : Convert Undo-Checked out SFD to MFD document in Default layout - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32"}, 
			description = "Convert Undo-Checked out SFD to MFD document in Default layout - Operations menu")
	public void SprintTest32_3_7_3A_1(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-6 : Perform Undo-Checkout operation
			//----------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo-checkout operation

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not undo checked out.");

			Log.message("6. Object (" + dataPool.get("ObjectName") + ") is undo-checked out.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value))
				Log.pass("Test case Passed. Undo-Checkedout SFD's cannot be converted to MFD through operations menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in operations menu for undo-checked out objects.", driver);
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

	} //End SprintTest32_3_7_3A_1

	/**
	 * 32.3.7.3A.2 : Convert Undo-Checked out SFD to MFD document in Default with navigation pane layout - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32"}, 
			description = "Convert Undo-Checked out SFD to MFD document in Default with navigation pane layout - Operations menu")
	public void SprintTest32_3_7_3A_2(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Default with navigation pane Layout' layout
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

			//Step-6 : Perform Undo-Checkout operation
			//----------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo-checkout operation

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not undo checked out.");

			Log.message("6. Object (" + dataPool.get("ObjectName") + ") is undo-checked out.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value))
				Log.pass("Test case Passed. Undo-Checkedout SFD's cannot be converted to MFD through operations menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in operations menu for undo-checked out objects.", driver);
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

	} //End SprintTest32_3_7_3A_2

	/**
	 * 32.3.7.3A.3 : Convert Undo-Checked out SFD to MFD document in No Java Applet layout - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32"}, 
			description = "Convert Undo-Checked out SFD to MFD document in No Java Applet layout - Operations menu")
	public void SprintTest32_3_7_3A_3(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

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

			//Step-6 : Perform Undo-Checkout operation
			//----------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo-checkout operation

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not undo checked out.");

			Log.message("6. Object (" + dataPool.get("ObjectName") + ") is undo-checked out.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value))
				Log.pass("Test case Passed. Undo-Checkedout SFD's cannot be converted to MFD through operations menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in operations menu for undo-checked out objects.", driver);
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

	} //End SprintTest32_3_7_3A_3

	/**
	 * 32.3.7.3A.4 : Convert Undo-Checked out SFD to MFD document in No Java Applet, No Task Area layout - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32"}, 
			description = "Convert Undo-Checked out SFD to MFD document in No Java Applet, No Task Area layout - Operations menu")
	public void SprintTest32_3_7_3A_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

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

			//Step-6 : Perform Undo-Checkout operation
			//----------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo-checkout operation

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not undo checked out.");

			Log.message("6. Object (" + dataPool.get("ObjectName") + ") is undo-checked out.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value))
				Log.pass("Test case Passed. Undo-Checkedout SFD's cannot be converted to MFD through operations menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in operations menu for undo-checked out objects.", driver);
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

	} //End SprintTest32_3_7_3A_4

	/**
	 * 32.3.7.3A.5 : Convert Undo-Checked out SFD to MFD document in No Java Applet, No Task Area but show GoTo shortcuts layout - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32"}, 
			description = "Convert Undo-Checked out SFD to MFD document in No Java Applet, No Task Area but show GoTo shortcuts layout - Operations menu")
	public void SprintTest32_3_7_3A_5(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Applet, No Task Area but show GoTo Shortcuts Layout' layout
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

			//Step-6 : Perform Undo-Checkout operation
			//----------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo-checkout operation

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not undo checked out.");

			Log.message("6. Object (" + dataPool.get("ObjectName") + ") is undo-checked out.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value))
				Log.pass("Test case Passed. Undo-Checkedout SFD's cannot be converted to MFD through operations menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in operations menu for undo-checked out objects.", driver);
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

	} //End SprintTest32_3_7_3A_5

	/**
	 * 32.3.7.3B.1 : Convert Undo-Checked out SFD to MFD document in Default layout - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32"}, 
			description = "Convert Undo-Checked out SFD to MFD document in Default layout - Context menu")
	public void SprintTest32_3_7_3B_1(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-6 : Perform Undo-Checkout operation
			//----------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo-checkout operation

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not undo checked out.");

			Log.message("6. Object (" + dataPool.get("ObjectName") + ") is undo-checked out.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.ConvertToMFD_C.Value))
				Log.pass("Test case Passed. Undo-Checkedout SFD's cannot be converted to MFD through context menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in context menu for undo-checked out objects.", driver);
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

	} //End SprintTest32_3_7_3B_1

	/**
	 * 32.3.7.3B.2 : Convert Undo-Checked out SFD to MFD document in Default with Navigation Pane layout - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32"}, 
			description = "Convert Undo-Checked out SFD to MFD document in Default with Navigation Pane layout - Context menu")
	public void SprintTest32_3_7_3B_2(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Default with Navigation pane Layout' layout
			//-----------------------------------------------------------------------
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

			//Step-6 : Perform Undo-Checkout operation
			//----------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo-checkout operation

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not undo checked out.");

			Log.message("6. Object (" + dataPool.get("ObjectName") + ") is undo-checked out.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.ConvertToMFD_C.Value))
				Log.pass("Test case Passed. Undo-Checkedout SFD's cannot be converted to MFD through context menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in context menu for undo-checked out objects.", driver);
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

	} //End SprintTest32_3_7_3B_2

	/**
	 * 32.3.7.3B.3 : Convert Undo-Checked out SFD to MFD document in No Java Applet layout - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32"}, 
			description = "Convert Undo-Checked out SFD to MFD document in No Java Applet layout - Context menu")
	public void SprintTest32_3_7_3B_3(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Applet Layout' layout
			//-----------------------------------------------------------------------
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

			//Step-6 : Perform Undo-Checkout operation
			//----------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo-checkout operation

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not undo checked out.");

			Log.message("6. Object (" + dataPool.get("ObjectName") + ") is undo-checked out.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.ConvertToMFD_C.Value))
				Log.pass("Test case Passed. Undo-Checkedout SFD's cannot be converted to MFD through context menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in context menu for undo-checked out objects.", driver);
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

	} //End SprintTest32_3_7_3B_3

	/**
	 * 32.3.7.3B.4 : Convert Undo-Checked out SFD to MFD document in No Java Applet, No Task Area layout - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32"}, 
			description = "Convert Undo-Checked out SFD to MFD document in No Java Applet, No Task Area layout - Context menu")
	public void SprintTest32_3_7_3B_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Applet Layout' layout
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

			//Step-6 : Perform Undo-Checkout operation
			//----------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo-checkout operation

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not undo checked out.");

			Log.message("6. Object (" + dataPool.get("ObjectName") + ") is undo-checked out.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.ConvertToMFD_C.Value))
				Log.pass("Test case Passed. Undo-Checkedout SFD's cannot be converted to MFD through context menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in context menu for undo-checked out objects.", driver);
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

	} //End SprintTest32_3_7_3B_4

	/**
	 * 32.3.7.3B.5 : Convert Undo-Checked out SFD to MFD document in No Java Applet, No Task Area but Show GoTo shortcuts layout - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32"}, 
			description = "Convert Undo-Checked out SFD to MFD document in No Java Applet, No Task Area but Show GoTo shortcuts layout - Context menu")
	public void SprintTest32_3_7_3B_5(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Applet No Task Area but show GoTo shortcuts Layout' layout
			//---------------------------------------------------------------------------------------------
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

			//Step-6 : Perform Undo-Checkout operation
			//----------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo-checkout operation

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not undo checked out.");

			Log.message("6. Object (" + dataPool.get("ObjectName") + ") is undo-checked out.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.ConvertToMFD_C.Value))
				Log.pass("Test case Passed. Undo-Checkedout SFD's cannot be converted to MFD through context menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in context menu for undo-checked out objects.", driver);
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

	} //End SprintTest32_3_7_3B_5

	/**
	 * 32.3.7.3B.6 : Convert Undo-Checked out SFD to MFD document in Listing Pane and Properties Pane only layout - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32"}, 
			description = "Convert Undo-Checked out SFD to MFD document in Listing Pane and Properties Pane only layout - Context menu")
	public void SprintTest32_3_7_3B_6(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Listing Pane and Properties Pane only Layout' layout
			//---------------------------------------------------------------------------------------------
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

			//Step-6 : Perform Undo-Checkout operation
			//----------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo-checkout operation

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not undo checked out.");

			Log.message("6. Object (" + dataPool.get("ObjectName") + ") is undo-checked out.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.ConvertToMFD_C.Value))
				Log.pass("Test case Passed. Undo-Checkedout SFD's cannot be converted to MFD through context menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in context menu for undo-checked out objects.", driver);
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

	} //End SprintTest32_3_7_3B_6

	/**
	 * 32.3.7.3B.7 : Convert Undo-Checked out SFD to MFD document in Listing Pane only layout - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32"}, 
			description = "Convert Undo-Checked out SFD to MFD document in Listing Pane only layout - Context menu")
	public void SprintTest32_3_7_3B_7(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Listing Pane and Properties Pane only Layout' layout
			//---------------------------------------------------------------------------------------------
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

			//Step-6 : Perform Undo-Checkout operation
			//----------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo-checkout operation

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not undo checked out.");

			Log.message("6. Object (" + dataPool.get("ObjectName") + ") is undo-checked out.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.ConvertToMFD_C.Value))
				Log.pass("Test case Passed. Undo-Checkedout SFD's cannot be converted to MFD through context menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in context menu for undo-checked out objects.", driver);
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

	} //End SprintTest32_3_7_3B_7

	/**
	 * 32.3.7.4A.1 : Checkout, Checkin and Convert SFD to MFD in Default layout - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32"}, 
			description = "Checkout, Checkin and Convert SFD to MFD in Default layout - Operations menu")
	public void SprintTest32_3_7_4A_1(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-6 : Perform Check-in operation
			//-------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not undo checked out.");

			Log.message("6. Object (" + dataPool.get("ObjectName") + ") is Checked in.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value))
				Log.pass("Test case Passed. Checked-in SFD's cannot be converted to MFD through operations menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in operations menu for Checked-in objects.", driver);
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

	} //End SprintTest32_3_7_4A_1

	/**
	 * 32.3.7.4A.2 : Checkout, Checkin and Convert SFD to MFD in Default with Navigation Pane layout - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32"}, 
			description = "Checkout, Checkin and Convert SFD to MFD in Default with Navigation pane layout - Operations menu")
	public void SprintTest32_3_7_4A_2(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-6 : Perform Check-in operation
			//-------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not undo checked out.");

			Log.message("6. Object (" + dataPool.get("ObjectName") + ") is Checked in.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value))
				Log.pass("Test case Passed. Checked-in SFD's cannot be converted to MFD through operations menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in operations menu for Checked-in objects.", driver);
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

	} //End SprintTest32_3_7_4A_2

	/**
	 * 32.3.7.4A.3 : Checkout, Checkin and Convert SFD to MFD in No Java Applet layout - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32"}, 
			description = "Checkout, Checkin and Convert SFD to MFD in No Java Applet layout - Operations menu")
	public void SprintTest32_3_7_4A_3(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-6 : Perform Check-in operation
			//-------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not undo checked out.");

			Log.message("6. Object (" + dataPool.get("ObjectName") + ") is Checked in.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value))
				Log.pass("Test case Passed. Checked-in SFD's cannot be converted to MFD through operations menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in operations menu for Checked-in objects.", driver);
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

	} //End SprintTest32_3_7_4A_3

	/**
	 * 32.3.7.4A.4 : Checkout, Checkin and Convert SFD to MFD in No Java Applet, No Task Area layout - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32"}, 
			description = "Checkout, Checkin and Convert SFD to MFD in No Java Applet, No Task Area layout - Operations menu")
	public void SprintTest32_3_7_4A_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

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

			//Step-6 : Perform Check-in operation
			//-------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not undo checked out.");

			Log.message("6. Object (" + dataPool.get("ObjectName") + ") is Checked in.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value))
				Log.pass("Test case Passed. Checked-in SFD's cannot be converted to MFD through operations menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in operations menu for Checked-in objects.", driver);
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

	} //End SprintTest32_3_7_4A_4

	/**
	 * 32.3.7.4A.5 : Checkout, Checkin and Convert SFD to MFD in No Java Applet, No Task Area layout but Show GoTo shortcuts - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32"}, 
			description = "Checkout, Checkin and Convert SFD to MFD in No Java Applet, No Task Area layout but Show GoTo shortcuts- Operations menu")
	public void SprintTest32_3_7_4A_5(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Applet, No Task Area Layout but Show GoTo shortcuts' layout
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

			Log.message("1. No java applet with task area with go to layout is selected and saved.");//Step-2 : Log out from configuration page


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

			//Step-6 : Perform Check-in operation
			//-------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not undo checked out.");

			Log.message("6. Object (" + dataPool.get("ObjectName") + ") is Checked in.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value))
				Log.pass("Test case Passed. Checked-in SFD's cannot be converted to MFD through operations menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in operations menu for Checked-in objects.", driver);
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

	} //End SprintTest32_3_7_4A_5

	/**
	 * 32.3.7.4B.1 : Checkout, Checkin and Convert SFD to MFD in Default layout - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32"}, 
			description = "Checkout, Checkin and Convert SFD to MFD in Default layout - Context menu")
	public void SprintTest32_3_7_4B_1(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-6 : Perform Undo-Checkout operation
			//----------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo-checkout operation

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not undo checked out.");

			Log.message("6. Object (" + dataPool.get("ObjectName") + ") is undo-checked out.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.ConvertToMFD_C.Value))
				Log.pass("Test case Passed. Undo-Checkedout SFD's cannot be converted to MFD through context menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in context menu for undo-checked out objects.", driver);
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

	} //End SprintTest32_3_7_4B_1

	/**
	 * 32.3.7.4B.2 : Checkout, Checkin and Convert SFD to MFD in Default with Navigation Pane layout - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32"}, 
			description = "Checkout, Checkin and Convert SFD to MFD in Default with Navigation Pane layout - Context menu")
	public void SprintTest32_3_7_4B_2(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

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

			//Step-6 : Perform Undo-Checkout operation
			//----------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo-checkout operation

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not undo checked out.");

			Log.message("6. Object (" + dataPool.get("ObjectName") + ") is undo-checked out.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.ConvertToMFD_C.Value))
				Log.pass("Test case Passed. Undo-Checkedout SFD's cannot be converted to MFD through context menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in context menu for undo-checked out objects.", driver);
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

	} //End SprintTest32_3_7_4B_2

	/**
	 * 32.3.7.4B.3 : Checkout, Checkin and Convert SFD to MFD in No Java Applet layout - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32"}, 
			description = "Checkout, Checkin and Convert SFD to MFD in No Java Applet layout - Context menu")
	public void SprintTest32_3_7_4B_3(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

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

			//Step-6 : Perform Undo-Checkout operation
			//----------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo-checkout operation

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not undo checked out.");

			Log.message("6. Object (" + dataPool.get("ObjectName") + ") is undo-checked out.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.ConvertToMFD_C.Value))
				Log.pass("Test case Passed. Undo-Checkedout SFD's cannot be converted to MFD through context menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in context menu for undo-checked out objects.", driver);
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

	} //End SprintTest32_3_7_4B_3

	/**
	 * 32.3.7.4B.4 : Checkout, Checkin and Convert SFD to MFD in No Java Applet, No task Area layout - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32"}, 
			description = "Checkout, Checkin and Convert SFD to MFD in No Java Applet, No Task area layout - Context menu")
	public void SprintTest32_3_7_4B_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Applet, No task area Layout' layout
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

			//Step-6 : Perform Undo-Checkout operation
			//----------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo-checkout operation

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not undo checked out.");

			Log.message("6. Object (" + dataPool.get("ObjectName") + ") is undo-checked out.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.ConvertToMFD_C.Value))
				Log.pass("Test case Passed. Undo-Checkedout SFD's cannot be converted to MFD through context menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in context menu for undo-checked out objects.", driver);
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

	} //End SprintTest32_3_7_4B_4

	/**
	 * 32.3.7.4B.5 : Checkout, Checkin and Convert SFD to MFD in No Java Applet, No task Area layout but show goto shortcuts- Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32"}, 
			description = "Checkout, Checkin and Convert SFD to MFD in No Java Applet, No task Area layout but show goto shortcuts layout - Context menu")
	public void SprintTest32_3_7_4B_5(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Applet, No task Area layout but show goto shortcuts Layout' layout
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

			//Step-6 : Perform Undo-Checkout operation
			//----------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo-checkout operation

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not undo checked out.");

			Log.message("6. Object (" + dataPool.get("ObjectName") + ") is undo-checked out.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.ConvertToMFD_C.Value))
				Log.pass("Test case Passed. Undo-Checkedout SFD's cannot be converted to MFD through context menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in context menu for undo-checked out objects.", driver);
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

	} //End SprintTest32_3_7_4B_5

	/**
	 * 32.3.7.4B.6 : Checkout, Checkin and Convert SFD to MFD in Properties and Listing Pane layout only - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32"}, 
			description = "Checkout, Checkin and Convert SFD to MFD in Listing and Properties Pane only layout - Context menu")
	public void SprintTest32_3_7_4B_6(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Listing and Properties Pane only layout' layout
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

			//Step-6 : Perform Undo-Checkout operation
			//----------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo-checkout operation

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not undo checked out.");

			Log.message("6. Object (" + dataPool.get("ObjectName") + ") is undo-checked out.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.ConvertToMFD_C.Value))
				Log.pass("Test case Passed. Undo-Checkedout SFD's cannot be converted to MFD through context menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in context menu for undo-checked out objects.", driver);
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

	} //End SprintTest32_3_7_4B_6

	/**
	 * 32.3.7.4B.7 : Checkout, Checkin and Convert SFD to MFD in Listing Pane layout only - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32"}, 
			description = "Checkout, Checkin and Convert SFD to MFD in Listing Pane only layout - Context menu")
	public void SprintTest32_3_7_4B_7(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Listing Pane only layout' layout
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

			//Step-6 : Perform Undo-Checkout operation
			//----------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo-checkout operation

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not undo checked out.");

			Log.message("6. Object (" + dataPool.get("ObjectName") + ") is undo-checked out.");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.ConvertToMFD_C.Value))
				Log.pass("Test case Passed. Undo-Checkedout SFD's cannot be converted to MFD through context menu.");
			else
				Log.fail("Test case Failed. Convert SFD to MFD is enabled in context menu for undo-checked out objects.", driver);
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

	} //End SprintTest32_3_7_4B_7


} //End class TryMFDAndSFDConversionAfterCheckoutCheckIn

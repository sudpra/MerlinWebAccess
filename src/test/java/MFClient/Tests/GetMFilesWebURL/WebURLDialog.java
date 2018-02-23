package MFClient.Tests.GetMFilesWebURL;


import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.WebDriverUtils;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.openqa.selenium.By;
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
public class WebURLDialog {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String configURL = null;
	public static String userName = null;
	public static String password = null;
	public static String testVault = null;
	public static String productVersion = null;
	public static WebDriver driver = null;
	public static String userFullName = null;
	public static String className = null;

	/**
	 * init : Before Class method to perform initial operations.
	 */
	@BeforeClass (alwaysRun=true)
	public void init() throws Exception {

		try {

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			xlTestDataWorkBook = xmlParameters.getParameter("TestData");
			loginURL = xmlParameters.getParameter("webSite");
			configURL = xmlParameters.getParameter("ConfigurationURL");
			Utility.restoreTestVault();
			Utility.configureUsers(xlTestDataWorkBook);
			className = this.getClass().getSimpleName().toString().trim();
			if (xmlParameters.getParameter("driverType").equalsIgnoreCase("IE"))
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim() + xmlParameters.getParameter("driverVersion").trim();
			else
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim();

			userName = xmlParameters.getParameter("UserName");
			password = xmlParameters.getParameter("Password");
			testVault = xmlParameters.getParameter("VaultName");		
			userFullName = xmlParameters.getParameter("UserFullName");

			/*	if(userConfig != "") {
				String[] userDetails = userConfig.split(",");
				userName = userDetails[0];
				password = userDetails[1];
				userFullName = userDetails[2];
			}
			 */
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

			Log.endTestCase();

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
	 * 25.2.1A : Click X button in Get Hyper link dialog opened through context menu 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL"}, 
			description = "Click X button in Get Hyper link dialog opened through context menu")
	public void SprintTest25_2_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Right click on the object and select Get M-Files Web URL from context menu
			//-----------------------------------------------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			Log.message("2. Right clicked on an object (" + dataPool.get("ObjectName") + ") and Get M-Files Web URL from context menu is selected.");

			//Step-3 : Open Get M-Files Web URL dialog for the object from context menu
			//-------------------------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Click x button in the dialog
			//-------------------------------------
			mfilesDialog.clickCloseButton(); //Clicks X button in the dialog

			Log.message("4. Close (X) button is clicked in the dialog.");

			//Verification : To Verify MFiles dialog is closed on clicking x button
			//---------------------------------------------------------------------

			//Verifies that MFiles dialog does not exists
			if (!MFilesDialog.exists(driver))
				Log.pass("Test case Passed. MFiles dialog is closed on clicking close (X) button.");
			else
				Log.fail("Test case Failed. MFiles dialog is not closed on clicking close (X) button.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_1A

	/**
	 * 25.2.1B : Click X button in Get Hyper link dialog opened through Operations menu 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL"}, 
			description = "Click X button in Get Hyper link dialog opened through Operations menu")
	public void SprintTest25_2_1B(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select Get M-Files Web URL from Operations menu
			//--------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			Log.message("2. 'Get M-Files Web URL' is selected from operations menu.");

			//Step-3 : Open Get M-Files Web URL dialog for the object from operations menu
			//-------------------------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-4 : Click x button in the dialog
			//-------------------------------------
			mfilesDialog.clickCloseButton(); //Clicks X button in the dialog

			Log.message("4. Close (X) button is clicked in the dialog.");

			//Verification : To Verify MFiles dialog is closed on clicking x button
			//---------------------------------------------------------------------

			//Verifies that MFiles dialog does not exists
			if (!MFilesDialog.exists(driver))
				Log.pass("Test case Passed. MFiles dialog is closed on clicking close (X) button.");
			else
				Log.fail("Test case Failed. MFiles dialog is not closed on clicking close (X) button.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_1B

	/**
	 * 25.2.2A : Click Esc key in Get Hyper link dialog opened through context menu 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL"}, 
			description = "Click X button in Get Hyper link dialog opened through context menu")
	public void SprintTest25_2_2A(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();


			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Right click on the object and select Get M-Files Web URL from context menu
			//-----------------------------------------------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			Log.message("2. Right clicked on an object (" + dataPool.get("ObjectName") + ") and Get M-Files Web URL from context menu is selected.");

			//Step-3 : Open Get M-Files Web URL dialog for the object from context menu
			//-------------------------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-4 : Click Esc key in the dialog
			//-------------------------------------
			mfilesDialog.clickEscapeKey();

			Log.message("4. Esc key is clicked in the dialog.");

			//Verification : To Verify MFiles dialog is closed on clicking Esc key
			//---------------------------------------------------------------------

			//Verifies that MFiles dialog does not exists
			if (!MFilesDialog.exists(driver))
				Log.pass("Test case Passed. MFiles dialog is closed on clicking Esc key.");
			else
				Log.fail("Test case Failed. MFiles dialog is not closed on clicking Esc key.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_2A

	/**
	 * 25.2.2B : Click Esc key in Get Hyper link dialog opened through Operations menu 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL"}, 
			description = "Click Esc key in Get Hyper link dialog opened through Operations menu")
	public void SprintTest25_2_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select Get M-Files Web URL from Operations menu
			//--------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			Log.message("2. 'Get M-Files Web URL' is selected from operations menu.");

			//Step-3 : Open Get M-Files Web URL dialog for the object from operations menu
			//-------------------------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-4 : Click Esc key in the dialog
			//-------------------------------------
			mfilesDialog.clickEscapeKey();

			Log.message("4. Esc key is clicked in the dialog.");

			//Verification : To Verify MFiles dialog is closed on clicking x button
			//---------------------------------------------------------------------

			//Verifies that MFiles dialog does not exists
			if (!MFilesDialog.exists(driver))
				Log.pass("Test case Passed. MFiles dialog is closed on clicking close (X) button.");
			else
				Log.fail("Test case Failed. MFiles dialog is not closed on clicking close (X) button.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_2B

	/**
	 * 25.2.4A : Copy to Clipboard should copy the URL displayed in text box - Context menu 
	 */
	/*@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL"}, 
			description = "Copy to Clipboard should copy the URL displayed in text box - Context menu .")*/
	public void SprintTest25_2_4A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Right click on the object and select Get M-Files Web URL from context menu
			//-----------------------------------------------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			Log.message("2. Right clicked on an object (" + dataPool.get("ObjectName") + ") and Get M-Files Web URL from context menu is selected.");

			//Step-3 : Open Get M-Files Web URL dialog for the object from context menu
			//-------------------------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-4 : Click Copy to Clipboard link
			//-------------------------------------
			if (!mfilesDialog.isCopyToClipBoardLinkExists()) //Checks if Copy to clipboard link exists
				throw new Exception("Copy to clipboard link does not exists.");

			mfilesDialog.clickCopyToClipBoardLink();

			Log.message("4. Copy to Clipboard link is clicked.");

			//Verification : To Verify Copy to clipboard link copies text in the text box
			//---------------------------------------------------------------------------

			//Verifies Copy to clipboard link copies text in the text box
			if (mfilesDialog.getHyperlink().equalsIgnoreCase(Utility.getTextFromClipboard()))
				Log.pass("Test case Passed. 'Copy to Clipboard' link copied the text in the Get M-Files Web URL dialog.");
			else
				Log.fail("Test case Failed. 'Copy to Clipboard' link does not copied the text in the Get M-Files Web URL dialog.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_4A

	/**
	 * 25.2.4B : Copy to Clipboard should copy the URL displayed in text box - Operations menu  
	 */
	/*@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL"}, 
			description = "Copy to Clipboard should copy the URL displayed in text box - Operations menu.")*/
	public void SprintTest25_2_4B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select Get M-Files Web URL from Operations menu
			//--------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			Log.message("2. 'Get M-Files Web URL' is selected from operations menu.");

			//Step-3 : Open Get M-Files Web URL dialog for the object from operations menu
			//-------------------------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-4 : Click Copy to Clipboard link
			//-------------------------------------
			if (!mfilesDialog.isCopyToClipBoardLinkExists()) //Checks if Copy to clipboard link exists
				throw new Exception("Copy to clipboard link does not exists.");

			mfilesDialog.clickCopyToClipBoardLink();

			Log.message("4. Copy to Clipboard link is clicked.");

			//Verification : To Verify Copy to clipboard link copies text in the text box
			//---------------------------------------------------------------------------

			//Verifies Copy to clipboard link copies text in the text box
			if (mfilesDialog.getHyperlink().equalsIgnoreCase(Utility.getTextFromClipboard()))
				Log.pass("Test case Passed. 'Copy to Clipboard' link copied the text in the Get M-Files Web URL dialog.");
			else
				Log.fail("Test case Failed. 'Copy to Clipboard' link copied the text in the Get M-Files Web URL dialog.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_4B


	/**
	 * 25.2.12.1A : 'Show the selected object' in Get M-Files Web URL should be selected by default - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL"}, 
			description = "'Show the selected object' in Get M-Files Web URL should be selected by default - Context menu.")
	public void SprintTest25_2_12_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from context menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Verification : To Verify if 'Show the selected object' is selected by default
			//------------------------------------------------------------------------------

			if (mfilesDialog.isHyperLinkActionSelected("Show Object")) //Verifies if member object exists in the hyperlink
				Log.pass("Test case Passed. 'Show the selected object' in Get M-Files Web URL dialog is selected by Default.");
			else
				Log.fail("Test case Failed. 'Show the selected object' in Get M-Files Web URL dialog is selected by Default.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_12_1A

	/**
	 * 25.2.12.1B : 'Show the selected object' in Get M-Files Web URL should be selected by default - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "Show members"}, 
			description = "'Show the selected object' in Get M-Files Web URL should be selected by default - Operations menu.")
	public void SprintTest25_2_12_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from operations menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Verification : To Verify if 'Show the selected object' is selected by default
			//------------------------------------------------------------------------------
			if (mfilesDialog.isHyperLinkActionSelected("Show Object")) //Verifies 'Show the selected object' in the Get hyperlink
				Log.pass("Test case Passed. 'Show the selected object' in Get M-Files Web URL dialog is selected by Default.");
			else
				Log.fail("Test case Failed. 'Show the selected object' in Get M-Files Web URL dialog is selected by Default.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_12_1B

	/**
	 * 25.2.12.2A : 'Default' custom layout should be selected by default - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL"}, 
			description = "'Default' custom layout should be selected by default - Context menu")
	public void SprintTest25_2_12_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from context menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Verification : To Verify if 'Default' custom layout option is selected by default
			//----------------------------------------------------
			if (mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value)) //Verifies if member object exists in the hyperlink
				Log.pass("Test case Passed. 'Default' custom layout in Get M-Files Web URL dialog is selected by Default.");
			else
				Log.fail("Test case Failed. 'Default' custom layout in Get M-Files Web URL dialog is not selected by Default.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_12_2A

	/**
	 * 25.2.12.2B : 'Default' custom layout should be selected by default - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL"}, 
			description = "'Default' custom layout should be selected by default - Operations menu")
	public void SprintTest25_2_12_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from operations menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Verification : To Verify if 'Default' custom layout option is selected by default
			//----------------------------------------------------------------------------------
			if (mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value)) //Verifies custom layout selected is default
				Log.pass("Test case Passed. 'Default' custom layout in Get M-Files Web URL dialog is selected by Default.");
			else
				Log.fail("Test case Failed. 'Default' custom layout in Get M-Files Web URL dialog is not selected by Default.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_12_2B



	/**
	 * 25.2.14.1A : 'Download the selected file' in Get M-Files Web URL dialog should be enabled for SFD document - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "Views"}, 
			description = "'Download the selected file' in Get M-Files Web URL dialog should be enabled for SFD document - Context menu")
	public void SprintTest25_2_14_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();


			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from context menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isSFDByItemName(driver, dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") is not SFD document.");

			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Verification : To Verify if 'Download the selected file' is enabled in Get M-Files Web URL dialog 
			//-------------------------------------------------------------------------------------------
			if (mfilesDialog.isHyperLinkActionEnabled("DOWNLOAD FILE")) //Verifies if member object exists in the hyperlink
				Log.pass("Test case Passed. 'Download the selected file' is enabled for SFD document in Get M-Files Web URL dialog opened through context menu.");
			else
				Log.fail("Test case Failed. 'Download the selected file' is not enabled for SFD document in Get M-Files Web URL dialog opened through context menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_14_1A

	/**
	 * 25.2.14.1B : 'Download the selected file' in Get M-Files Web URL dialog should be enabled for SFD document - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "Views"}, 
			description = "'Download the selected file' in Get M-Files Web URL dialog should be enabled for SFD document - Operations menu")
	public void SprintTest25_2_14_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from context menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isSFDByItemName(driver, dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") is not SFD document.");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Verification : To Verify if 'Download the selected file' is enabled in Get M-Files Web URL dialog 
			//-------------------------------------------------------------------------------------------
			if (mfilesDialog.isHyperLinkActionEnabled("DOWNLOAD FILE")) //Verifies if member object exists in the hyperlink
				Log.pass("Test case Passed. 'Download the selected file' is enabled for SFD document in Get M-Files Web URL dialog opened through operations menu.");
			else
				Log.fail("Test case Failed. 'Download the selected file' is not enabled for SFD document in Get M-Files Web URL dialog opened through operations menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_14_1B

	/**
	 * 25.2.14.2A : 'Download the selected file' for MFD document is not possible in Get M-Files Web URL dialog - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "Views"}, 
			description = "'Download the selected file' for MFD document is not possible in Get M-Files Web URL dialog - Context menu")
	public void SprintTest25_2_14_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from context menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (ListView.isSFDByItemName(driver, dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") is not MFD document.");

			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Right Clicks on an object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Verification : To Verify if 'Download the selected file' is disabled in Get M-Files Web URL dialog 
			//-------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkActionEnabled("DOWNLOAD FILE")) 
				Log.pass("Test case Passed. 'Download the selected file' is disabled for MFD document in Get M-Files Web URL dialog opened through context menu.");
			else
				Log.fail("Test case Failed. 'Download the selected file' is not disabled for MFD document in Get M-Files Web URL dialog opened through context menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_14_2A

	/**
	 * 25.2.14.2B : 'Download the selected file' for MFD document is not possible in Get M-Files Web URL dialog - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "Views"}, 
			description = "'Download the selected file' for MFD document is not possible in Get M-Files Web URL dialog - Operations menu")
	public void SprintTest25_2_14_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from context menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (ListView.isSFDByItemName(driver, dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") is not MFD document.");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Verification : To Verify if 'Download the selected file' is disabled in Get M-Files Web URL dialog 
			//-------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkActionEnabled("DOWNLOAD FILE"))
				Log.pass("Test case Passed. 'Download the selected file' is disabled for MFD document in Get M-Files Web URL dialog opened through Operations menu.");
			else
				Log.fail("Test case Failed. 'Download the selected file' is not disabled for MFD document in Get M-Files Web URL dialog opened through Operations menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_14_2B

	/**
	 * 25.2.14.3A : 'Download the selected file' for non-document object types is not possible in Get M-Files Web URL dialog - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "Views"}, 
			description = "'Download the selected file' for non-document object types is not possible in Get M-Files Web URL dialog - Context menu")
	public void SprintTest25_2_14_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from context menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (homePage.listView.getColumnValueByItemName(dataPool.get("ObjectName"), "Object Type").equalsIgnoreCase("Document")) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") is not a non-document object type.");

			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Right Clicks on an object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Verification : To Verify if 'Download the selected file' is disabled in Get M-Files Web URL dialog 
			//-------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkActionEnabled("DOWNLOAD FILE")) 
				Log.pass("Test case Passed. 'Download the selected file' is disabled for MFD document in Get M-Files Web URL dialog opened through context menu.");
			else
				Log.fail("Test case Failed. 'Download the selected file' is not disabled for MFD document in Get M-Files Web URL dialog opened through context menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_14_3A

	/**
	 * 25.2.14.3B : 'Download the selected file' for non-document object types is not possible in Get M-Files Web URL dialog - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "Views"}, 
			description = "'Download the selected file' for non-document object types is not possible in Get M-Files Web URL dialog - Operations menu")
	public void SprintTest25_2_14_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from context menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (homePage.listView.getColumnValueByItemName(dataPool.get("ObjectName"), "Object Type").equalsIgnoreCase("Document")) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") is not a non-document object type.");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Verification : To Verify if 'Download the selected file' is disabled in Get M-Files Web URL dialog 
			//-------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkActionEnabled("DOWNLOAD FILE")) 
				Log.pass("Test case Passed. 'Download the selected file' is disabled for MFD document in Get M-Files Web URL dialog opened through Operations menu.");
			else
				Log.fail("Test case Failed. 'Download the selected file' is not disabled for MFD document in Get M-Files Web URL dialog opened through Operations menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_14_3B

	/**
	 * 25.2.14.4A : 'Download the selected file' for view and virtual folders is not possible in Get M-Files Web URL dialog - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "Views"}, 
			description = "'Download the selected file' for view and virtual folders is not possible in Get M-Files Web URL dialog - Context menu")
	public void SprintTest25_2_14_4A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from context menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ViewName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. View/Virtual Folder (" + dataPool.get("ViewName") + ") does not exists in the listView.");

			if(!homePage.listView.rightClickItem(dataPool.get("ViewName"))) //Right Clicks on an object
				throw new Exception("View/Virutal folder (" + dataPool.get("ViewName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an View/Virtual Folder (" + dataPool.get("ViewName") + ") is opened from context menu.");

			//Verification : To Verify if 'Download the selected file' is disabled in Get M-Files Web URL dialog 
			//-------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkActionEnabled("DOWNLOAD FILE")) 
				Log.pass("Test case Passed. 'Download the selected file' is disabled for View/Virtual Folder in Get M-Files Web URL dialog opened through context menu.");
			else
				Log.fail("Test case Failed. 'Download the selected file' is not disabled for View/Virtual Folder in Get M-Files Web URL dialog opened through context menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_14_4A

	/**
	 * 25.2.14.4B : 'Download the selected file' for view and virtual folders is not possible in Get M-Files Web URL dialog - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "Views"}, 
			description = "'Download the selected file' for view and virtual folders is not possible in Get M-Files Web URL dialog - Operations menu")
	public void SprintTest25_2_14_4B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from context menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ViewName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. View/Virtual Folder (" + dataPool.get("ViewName") + ") does not exists in the listView.");

			if(!homePage.listView.clickItem(dataPool.get("ViewName"))) //Right Clicks on an object
				throw new Exception("View/Virtual Folder (" + dataPool.get("ViewName") + ") is not right clicked.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an View/Virtual Folder (" + dataPool.get("ViewName") + ") is opened from context menu.");

			//Verification : To Verify if 'Download the selected file' is disabled in Get M-Files Web URL dialog 
			//-------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkActionEnabled("DOWNLOAD FILE")) 
				Log.pass("Test case Passed. 'Download the selected file' is disabled for View/Virtual Folder in Get M-Files Web URL dialog opened through Operations menu.");
			else
				Log.fail("Test case Failed. 'Download the selected file' is not disabled for View/Virtual Folder in Get M-Files Web URL dialog opened through Operations menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_14_4A

	/**
	 * 25.2.14.5A : 'Download the selected file' in Get M-Files Web URL dialog should be enabled for latest version of SFD document - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "Views"}, 
			description = "'Download the selected file' in Get M-Files Web URL dialog should be enabled for latest version of SFD document - Context menu")
	public void SprintTest25_2_14_5A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Open the History View of the object
			//--------------------------------------------
			if (!ListView.isSFDByItemName(driver, dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") is not SFD Documennt.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Click on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Click the History link in the task pane


			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History View of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open context menu of the latest version of an object
			//-------------------------------------------------------------
			if (!homePage.listView.rightClickItemByIndex(0)) //Right clicks on latest version of an object
				throw new Exception("Latest version of an object is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of latest version of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Verification : To Verify if 'Download the selected file' is enabled in Get M-Files Web URL dialog 
			//-------------------------------------------------------------------------------------------
			if (mfilesDialog.isHyperLinkActionEnabled("DOWNLOAD FILE")) //Verifies if member object exists in the hyperlink
				Log.pass("Test case Passed. 'Download the selected file' is enabled for latest version of SFD document.");
			else
				Log.fail("Test case Failed. 'Download the selected file' is not enabled for latest version of SFD document", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_14_5A

	/**
	 * 25.2.14.5B : 'Download the selected file' in Get M-Files Web URL dialog should be enabled for latest version of SFD document - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "Views"}, 
			description = "'Download the selected file' in Get M-Files Web URL dialog should be enabled for latest version of SFD document - Operations menu")
	public void SprintTest25_2_14_5B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Open the History View of the object
			//--------------------------------------------
			if (!ListView.isSFDByItemName(driver, dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") is not SFD Documennt.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Click on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Click the History link in the task pane


			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History View of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open context menu of the latest version of an object
			//-------------------------------------------------------------
			if (!homePage.listView.clickItemByIndex(0)) //Right clicks on latest version of an object
				throw new Exception("Latest version of an object is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of latest version of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Verification : To Verify if 'Download the selected file' is enabled in Get M-Files Web URL dialog 
			//-------------------------------------------------------------------------------------------
			if (mfilesDialog.isHyperLinkActionEnabled("DOWNLOAD FILE")) //Verifies if member object exists in the hyperlink
				Log.pass("Test case Passed. 'Download the selected file' is enabled for latest version of SFD document.");
			else
				Log.fail("Test case Failed. 'Download the selected file' is not enabled for latest version of SFD document", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_14_5B

	/**
	 * 25.2.14.5C : 'Download the selected file' in Get M-Files Web URL dialog should be enabled for older version of SFD document - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "Views"}, 
			description = "'Download the selected file' in Get M-Files Web URL dialog should be enabled for older version of SFD document - Context menu")
	public void SprintTest25_2_14_5C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Open the History View of the object
			//--------------------------------------------
			if (!ListView.isSFDByItemName(driver, dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") is not SFD Documennt.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Click on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Click the History link in the task pane


			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History View of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open context menu of the latest version of an object
			//-------------------------------------------------------------
			if (homePage.listView.itemCount() <=1)
				throw new Exception("No older version available for an object.");

			if (!homePage.listView.rightClickItemByIndex(1)) //Right clicks on latest version of an object
				throw new Exception("Older version of an object is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of older version of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Verification : To Verify if 'Download the selected file' is enabled in Get M-Files Web URL dialog 
			//-------------------------------------------------------------------------------------------
			if (mfilesDialog.isHyperLinkActionEnabled("DOWNLOAD FILE")) //Verifies if member object exists in the hyperlink
				Log.pass("Test case Passed. 'Download the selected file' is enabled for older version of SFD document.");
			else
				Log.fail("Test case Failed. 'Download the selected file' is not enabled for older version of SFD document", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_14_5C

	/**
	 * 25.2.14.5D : 'Download the selected file' in Get M-Files Web URL dialog should be enabled for older version of SFD document - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "Views"}, 
			description = "'Download the selected file' in Get M-Files Web URL dialog should be enabled for older version of SFD document - Operations menu")
	public void SprintTest25_2_14_5D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Open the History View of the object
			//--------------------------------------------
			if (!ListView.isSFDByItemName(driver, dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") is not SFD Documennt.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Click on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Click the History link in the task pane


			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History View of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open context menu of the latest version of an object
			//-------------------------------------------------------------
			if (homePage.listView.itemCount() <=1)
				throw new Exception("No older version available for an object.");

			if (!homePage.listView.clickItemByIndex(1)) //Right clicks on latest version of an object
				throw new Exception("Older version of an object is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of older version of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Verification : To Verify if 'Download the selected file' is enabled in Get M-Files Web URL dialog 
			//-------------------------------------------------------------------------------------------
			if (mfilesDialog.isHyperLinkActionEnabled("DOWNLOAD FILE")) //Verifies if member object exists in the hyperlink
				Log.pass("Test case Passed. 'Download the selected file' is enabled for older version of SFD document.");
			else
				Log.fail("Test case Failed. 'Download the selected file' is not enabled for older version of SFD document", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_14_5D

	/**
	 * 25.2.14.6A : 'Download the selected file' for latest version of MFD document is not possible in Get M-Files Web URL dialog - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25"}, 
			description = "'Download the selected file' for latest version of MFD document is not possible in Get M-Files Web URL dialog - Context menu")
	public void SprintTest25_2_14_6A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Open the History View of the object
			//--------------------------------------------
			if (ListView.isSFDByItemName(driver, dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") is not MFD Documennt.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Click on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Click the History link in the task pane

			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History View of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open context menu of the latest version of an object
			//-------------------------------------------------------------
			if (!homePage.listView.rightClickItemByIndex(0)) //Right clicks on latest version of an object
				throw new Exception("Latest version of an object is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of latest version of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Verification : To Verify if 'Download the selected file' is not enabled in Get M-Files Web URL dialog 
			//-------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkActionEnabled("DOWNLOAD FILE")) //Verifies if member object exists in the hyperlink
				Log.pass("Test case Passed. 'Download the selected file' is not enabled for latest version of MFD document.");
			else
				Log.fail("Test case Failed. 'Download the selected file' is enabled for latest version of MFD document", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_14_6A

	/**
	 * 25.2.14.6B : 'Download the selected file' for latest version of MFD document is not possible in Get M-Files Web URL dialog - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25"}, 
			description = "'Download the selected file' for latest version of MFD document is not possible in Get M-Files Web URL dialog - Operations menu")
	public void SprintTest25_2_14_6B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Open the History View of the object
			//--------------------------------------------
			if (ListView.isSFDByItemName(driver, dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") is not MFD Documennt.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Click on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Click the History link in the task pane


			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History View of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open context menu of the latest version of an object
			//-------------------------------------------------------------
			if (!homePage.listView.clickItemByIndex(0)) //Selects latest version of an object
				throw new Exception("Latest version of an object is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of latest version of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Verification : To Verify if 'Download the selected file' is not enabled in Get M-Files Web URL dialog 
			//-------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkActionEnabled("DOWNLOAD FILE")) //Verifies if member object exists in the hyperlink
				Log.pass("Test case Passed. 'Download the selected file' is not enabled for latest version of MFD document.");
			else
				Log.fail("Test case Failed. 'Download the selected file' is enabled for latest version of MFD document", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_14_6B

	/**
	 * 25.2.14.6C : 'Download the selected file' for older version of MFD document is not possible in Get M-Files Web URL dialog - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25"}, 
			description = "'Download the selected file' for older version of MFD document is not possible in Get M-Files Web URL dialog - Context menu")
	public void SprintTest25_2_14_6C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Open the History View of the object
			//--------------------------------------------
			if (ListView.isSFDByItemName(driver, dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") is not MFD Documennt.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Click on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Click the History link in the task pane


			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History View of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open context menu of the older version of an object
			//-------------------------------------------------------------
			if (homePage.listView.itemCount() <=1)
				throw new Exception("No older version available for an object.");

			if (!homePage.listView.rightClickItemByIndex(1)) //Right clicks on latest version of an object
				throw new Exception("Older version of an object is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of older version of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Verification : To Verify if 'Download the selected file' is not enabled in Get M-Files Web URL dialog 
			//-------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkActionEnabled("DOWNLOAD FILE")) //Verifies if member object exists in the hyperlink
				Log.pass("Test case Passed. 'Download the selected file' is not enabled for latest version of MFD document.");
			else
				Log.fail("Test case Failed. 'Download the selected file' is enabled for latest version of MFD document", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_14_6C

	/**
	 * 25.2.14.6D : 'Download the selected file' for older version of MFD document is not possible in Get M-Files Web URL dialog - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25"}, 
			description = "'Download the selected file' for older version of MFD document is not possible in Get M-Files Web URL dialog - Operations menu")
	public void SprintTest25_2_14_6D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Open the History View of the object
			//--------------------------------------------
			if (ListView.isSFDByItemName(driver, dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") is not MFD Documennt.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Click on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Click the History link in the task pane


			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History View of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open context menu of the latest version of an object
			//-------------------------------------------------------------
			if (homePage.listView.itemCount() <=1)
				throw new Exception("No older version available for an object.");

			if (!homePage.listView.clickItemByIndex(1)) //Selects older version of an object
				throw new Exception("Older version of an object is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of older version of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Verification : To Verify if 'Download the selected file' is not enabled in Get M-Files Web URL dialog 
			//-------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkActionEnabled("DOWNLOAD FILE")) //Verifies if member object exists in the hyperlink
				Log.pass("Test case Passed. 'Download the selected file' is not enabled for latest version of MFD document.");
			else
				Log.fail("Test case Failed. 'Download the selected file' is enabled for latest version of MFD document", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_14_6D


	/**
	 * 25.2.14.7A : 'Download the selected file' for latest version of non-document object type is not possible in Get M-Files Web URL dialog - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25"}, 
			description = "'Download the selected file' for latest version of non-document object type is not possible in Get M-Files Web URL dialog - Context menu")
	public void SprintTest25_2_14_7A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Open the History View of the object
			//--------------------------------------------
			if (ListView.getObjectTypeByItem(driver, dataPool.get("ObjectName")).equalsIgnoreCase("DOCUMENT"))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") is not non-documennt object type.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Click on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Click the History link in the task pane


			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History View of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open context menu of the latest version of an object
			//-------------------------------------------------------------
			if (!homePage.listView.rightClickItemByIndex(0)) //Right clicks on latest version of an object
				throw new Exception("Latest version of an object is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of latest version of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Verification : To Verify if 'Download the selected file' is not enabled in Get M-Files Web URL dialog 
			//-------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkActionEnabled("DOWNLOAD FILE")) //Verifies if member object exists in the hyperlink
				Log.pass("Test case Passed. 'Download the selected file' is not enabled for latest version of non-document object type.");
			else
				Log.fail("Test case Failed. 'Download the selected file' is enabled for latest version of non-document object type.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_14_7A

	/**
	 * 25.2.14.7B : 'Download the selected file' for latest version of non-document object type is not possible in Get M-Files Web URL dialog - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25"}, 
			description = "'Download the selected file' for latest version of non-document object type is not possible in Get M-Files Web URL dialog - Operations menu")
	public void SprintTest25_2_14_7B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Open the History View of the object
			//--------------------------------------------
			if (ListView.getObjectTypeByItem(driver, dataPool.get("ObjectName")).equalsIgnoreCase("DOCUMENT"))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") is not non-documennt object type.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Click on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Click the History link in the task pane


			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History View of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open operations menu of the latest version of an object
			//-------------------------------------------------------------
			if (!homePage.listView.clickItemByIndex(0)) //Selects latest version of an object
				throw new Exception("Latest version of an object is not right clicked.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of latest version of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Verification : To Verify if 'Download the selected file' is not enabled in Get M-Files Web URL dialog 
			//-------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkActionEnabled("DOWNLOAD FILE")) //Verifies if member object exists in the hyperlink
				Log.pass("Test case Passed. 'Download the selected file' is not enabled for latest version of non-document object type.");
			else
				Log.fail("Test case Failed. 'Download the selected file' is enabled for latest version of non-document object type.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_14_7B

	/**
	 * 25.2.14.7C : 'Download the selected file' for Older version of non-document object type is not possible in Get M-Files Web URL dialog - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25"}, 
			description = "'Download the selected file' for Older version of non-document object type is not possible in Get M-Files Web URL dialog - Context menu")
	public void SprintTest25_2_14_7C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Open the History View of the object
			//--------------------------------------------
			if (ListView.getObjectTypeByItem(driver, dataPool.get("ObjectName")).equalsIgnoreCase("DOCUMENT"))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") is not non-documennt object type.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Click on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Click the History link in the task pane


			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History View of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open context menu of the latest version of an object
			//-------------------------------------------------------------
			if (homePage.listView.itemCount() <=1)
				throw new Exception("No older version available for an object.");

			if (!homePage.listView.rightClickItemByIndex(1)) //Right clicks on latest version of an object
				throw new Exception("Older version of an object is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of older version of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Verification : To Verify if 'Download the selected file' is not enabled in Get M-Files Web URL dialog 
			//-------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkActionEnabled("DOWNLOAD FILE")) //Verifies if member object exists in the hyperlink
				Log.pass("Test case Passed. 'Download the selected file' is not enabled for older version of non-document object type.");
			else
				Log.fail("Test case Failed. 'Download the selected file' is enabled for older version of non-document object type.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_14_7C

	/**
	 * 25.2.14.7D : 'Download the selected file' for Older version of non-document object type is not possible in Get M-Files Web URL dialog - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25"}, 
			description = "'Download the selected file' for Older version of non-document object type is not possible in Get M-Files Web URL dialog - Operations menu")
	public void SprintTest25_2_14_7D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Open the History View of the object
			//--------------------------------------------
			if (ListView.getObjectTypeByItem(driver, dataPool.get("ObjectName")).equalsIgnoreCase("DOCUMENT"))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") is not non-documennt object type.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Click on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Click the History link in the task pane


			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History View of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open context menu of the latest version of an object
			//-------------------------------------------------------------
			if (homePage.listView.itemCount() <=1)
				throw new Exception("No older version available for an object.");

			if (!homePage.listView.clickItemByIndex(1)) //Selects older version of an object
				throw new Exception("Older version of an object is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of older version of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Verification : To Verify if 'Download the selected file' is not enabled in Get M-Files Web URL dialog 
			//-------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkActionEnabled("DOWNLOAD FILE")) //Verifies if member object exists in the hyperlink
				Log.pass("Test case Passed. 'Download the selected file' is not enabled for older version of non-document object type.");
			else
				Log.fail("Test case Failed. 'Download the selected file' is enabled for older version of non-document object type.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_14_7D

	/**
	 * 25.2.15.1A : 'Default custom layout should have all layouts selected - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL"}, 
			description = "'Default custom layout should have all layouts selected - Context menu")
	public void SprintTest25_2_15_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from context menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-3 : Select 'Default' Custom layout if not selected
			//-------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			mfilesDialog.setHyperLinkLayoutItem(Caption.GetMFilesWebURL.JavaApplet.Value);

			Log.message("3. Default custom layout is selected.");

			//Verification : To Verify if all layout items are selected on selecting Default layout
			//-------------------------------------------------------------------------------------
			String unSelectedLayouts = "";
			String layouts[] = {Caption.GetMFilesWebURL.SearchArea.Value, Caption.GetMFilesWebURL.TaskArea.Value, Caption.GetMFilesWebURL.TopMenu.Value,
					Caption.GetMFilesWebURL.Breadcrumb.Value, Caption.GetMFilesWebURL.JavaApplet.Value, Caption.GetMFilesWebURL.Metadatacard.Value} ;

			for (int i=0; i<layouts.length; i++) //Checks if all layouts are selected
				if (!mfilesDialog.isHyperLinkLayoutItemSelected(layouts[i]))
					unSelectedLayouts = unSelectedLayouts +  layouts[i] + ";";

			if (unSelectedLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. All layouts are selected on selecting 'Default' custom layout.");
			else
				Log.fail("Test case Failed. All layouts are not selected on selecting 'Default' custom layout. Unselected layots : " 
						+ unSelectedLayouts, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_15_1A

	/**
	 * 25.2.15.1B : 'Default custom layout should have all layouts selected - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL"}, 
			description = "'Default custom layout should have all layouts selected - Operations menu")
	public void SprintTest25_2_15_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from operations menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-3 : Select 'Default' Custom layout if not selected
			//-------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			mfilesDialog.setHyperLinkLayoutItem(Caption.GetMFilesWebURL.JavaApplet.Value);

			Log.message("3. Default custom layout & Java applet is selected.");

			//Verification : To Verify if all layout items are selected on selecting Default layout
			//-------------------------------------------------------------------------------------
			String unSelectedLayouts = "";
			String layouts[] = {Caption.GetMFilesWebURL.SearchArea.Value, Caption.GetMFilesWebURL.TaskArea.Value, Caption.GetMFilesWebURL.TopMenu.Value,
					Caption.GetMFilesWebURL.Breadcrumb.Value, Caption.GetMFilesWebURL.JavaApplet.Value, Caption.GetMFilesWebURL.Metadatacard.Value} ;

			for (int i=0; i<layouts.length; i++) //Checks if all layouts are selected
				if (!mfilesDialog.isHyperLinkLayoutItemSelected(layouts[i]))
					unSelectedLayouts = unSelectedLayouts +  layouts[i] + ";";

			if (unSelectedLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. All layouts are selected on selecting 'Default' custom layout.");
			else
				Log.fail("Test case Failed. All layouts are not selected on selecting 'Default' custom layout. Unselected layots : " 
						+ unSelectedLayouts, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_15_1B

	/**
	 * 25.2.15.2A : Hyperlink URL page should have search pane, task pane, properties pane, top menu, breadcrumb and metadatacard visible on selecting Default layout - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL"}, 
			description = "Hyperlink URL page should have search pane, task pane, properties pane, top menu, breadcrumb and metadatacard visible on selecting Default layout - Context menu")
	public void SprintTest25_2_15_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from context menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-3 : Select 'Default' Custom layout if not selected and copy the hyperlink url
			//----------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			mfilesDialog.setHyperLinkLayoutItem(Caption.GetMFilesWebURL.JavaApplet.Value);

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Default custom layout is selected and hyperlink URL is obtained.");

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("4. Navigated to the URL copied from hyperlink dialog.");			

			//Verification : Verify if all required layouts are available in the page
			//-----------------------------------------------------------------------
			String unAvailableLayouts = "";

			if (!homePage.isSearchbarPresent()) //Checks if Search bar present
				unAvailableLayouts = "Search Bar";

			if (!homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				unAvailableLayouts = unAvailableLayouts + "Task Pane;";

			if (!homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value))
				unAvailableLayouts = unAvailableLayouts + "Properties Pane;";

			if (!homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				unAvailableLayouts = unAvailableLayouts + "Menu Bar;";

			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				unAvailableLayouts = unAvailableLayouts + "Breadcrumb;";

			if (!homePage.listView.isMetadataPropertiesEnabled())
				unAvailableLayouts = unAvailableLayouts + "Metadatacard;";

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page had search pane, task pane, properties pane, top menu, breadcrumb, java applet and metadatacard visible on selecting Default layout.");
			else
				Log.fail("Test case Failed. Few layots are missing on selecting 'Default' custom layout. Missed layots : " 
						+ unAvailableLayouts, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_15_2A

	/**
	 * 25.2.15.2B : Hyperlink URL page should have search pane, task pane, properties pane, top menu, breadcrumb and metadatacard visible on selecting Default layout - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL"}, 
			description = "Hyperlink URL page should have search pane, task pane, properties pane, top menu, breadcrumb and metadatacard visible on selecting Default layout - Operations menu")
	public void SprintTest25_2_15_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from operations menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");			

			//Step-3 : Select 'Default' Custom layout if not selected and copy the hyperlink url
			//----------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);


			mfilesDialog.setHyperLinkLayoutItem(Caption.GetMFilesWebURL.JavaApplet.Value);

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Default custom layout is selected and hyperlink URL is obtained.");

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("4. Navigated to the URL copied from hyperlink dialog.");			

			//Verification : Verify if all required layouts are available in the page
			//-----------------------------------------------------------------------
			String unAvailableLayouts = "";

			if (!homePage.isSearchbarPresent()) //Checks if Search bar present
				unAvailableLayouts = "Search Bar";

			if (!homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				unAvailableLayouts = unAvailableLayouts + "Task Pane;";

			if (!homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value))
				unAvailableLayouts = unAvailableLayouts + "Properties Pane;";

			if (!homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				unAvailableLayouts = unAvailableLayouts + "Menu Bar;";

			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				unAvailableLayouts = unAvailableLayouts + "Breadcrumb;";

			if (!homePage.listView.isMetadataPropertiesEnabled())
				unAvailableLayouts = unAvailableLayouts + "Metadatacard;";

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page had search pane, task pane, properties pane, top menu, breadcrumb, java applet and metadatacard visible on selecting Default layout.");
			else
				Log.fail("Test case Failed. Few layots are missing on selecting 'Default' custom layout. Missed layots : " 
						+ unAvailableLayouts, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_15_2B

	/**
	 * 25.2.16.1A : 'Simple Listing should unselect Java Applet and Task area in Get M-Files Web URL dialog - Context Menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL"}, 
			description = "'Simple Listing should unselect Java Applet and Task area in Get M-Files Web URL dialog - Context Menu")
	public void SprintTest25_2_16_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from context menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-3 : Select 'SimpleListing' Custom layout if not selected
			//-------------------------------------------------------
			mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Verifies if member object exists in the hyperlink
				throw new Exception("Simple Listing custom layout is not selected.");

			Log.message("3. Simple Listing custom layout is selected.");

			//Verification : To Verify if all layout items are selected on selecting Default layout
			//-------------------------------------------------------------------------------------
			String errDescription = "";
			String selectedLayouts[] = {Caption.GetMFilesWebURL.SearchArea.Value, Caption.GetMFilesWebURL.TopMenu.Value,
					Caption.GetMFilesWebURL.Breadcrumb.Value, Caption.GetMFilesWebURL.Metadatacard.Value} ;
			String unSelectedLayouts[] = {Caption.GetMFilesWebURL.TaskArea.Value,Caption.GetMFilesWebURL.JavaApplet.Value} ;			

			for (int i=0; i<selectedLayouts.length; i++) //Checks if all layouts are selected
				if (!mfilesDialog.isHyperLinkLayoutItemSelected(selectedLayouts[i]))
					errDescription = errDescription + selectedLayouts[i] + ";";

			if (!errDescription.equals("")) //Verifies single listing layout have selected required layouts
				throw new Exception("Layout that needs to be in selected state is in de-selected state. Layouts : " + errDescription);

			for (int i=0; i<unSelectedLayouts.length; i++) //Checks if all layouts are selected
				if (mfilesDialog.isHyperLinkLayoutItemSelected(unSelectedLayouts[i]))
					errDescription = errDescription + unSelectedLayouts[i] + ";";

			if (errDescription.equals("")) //Verifies single listing layout have de-selected required layouts
				Log.pass("Test case Passed. Layouts (" + unSelectedLayouts + ") are in de-selected selected on selecting 'Single listing' custom layout.");
			else 
				Log.fail("Test case Failed. Few layouts are in selected state on selecting 'Single listing' custom layout. Selected layots : " 
						+ errDescription, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_16_1A

	/**
	 * 25.2.16.1B : 'Simple Listing should unselect Java Applet and Task area in Get M-Files Web URL dialog - Operations Menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL"}, 
			description = "'Simple Listing should unselect Java Applet and Task area in Get M-Files Web URL dialog - Operations Menu")
	public void SprintTest25_2_16_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from operations menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-3 : Select 'Default' Custom layout if not selected
			//-------------------------------------------------------
			mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Verifies if member object exists in the hyperlink
				throw new Exception("Simple Listing custom layout is not selected.");

			Log.message("3. Simple Listing custom layout is selected.");

			//Verification : To Verify if all layout items are selected on selecting Default layout
			//-------------------------------------------------------------------------------------
			String errDescription = "";
			String selectedLayouts[] = {Caption.GetMFilesWebURL.SearchArea.Value, Caption.GetMFilesWebURL.TopMenu.Value,
					Caption.GetMFilesWebURL.Breadcrumb.Value, Caption.GetMFilesWebURL.Metadatacard.Value} ;
			String unSelectedLayouts[] = {Caption.GetMFilesWebURL.TaskArea.Value,Caption.GetMFilesWebURL.JavaApplet.Value} ;			

			for (int i=0; i<selectedLayouts.length; i++) //Checks if all layouts are selected
				if (!mfilesDialog.isHyperLinkLayoutItemSelected(selectedLayouts[i]))
					errDescription = errDescription +  selectedLayouts[i] + ";";

			if (!errDescription.equals("")) //Verifies single listing layout have selected required layouts
				throw new Exception("Layout that needs to be in selected state is in de-selected state. Layouts : " + errDescription);

			for (int i=0; i<unSelectedLayouts.length; i++) //Checks if all layouts are selected
				if (mfilesDialog.isHyperLinkLayoutItemSelected(unSelectedLayouts[i]))
					errDescription = errDescription + unSelectedLayouts[i] + ";";

			if (errDescription.equals("")) //Verifies single listing layout have de-selected required layouts
				Log.pass("Test case Passed. Layouts (" + unSelectedLayouts + ") are in de-selected selected on selecting 'Single listing' custom layout.");
			else 
				Log.fail("Test case Failed. Few layouts are in selected state on selecting 'Single listing' custom layout. Selected layots : " 
						+ errDescription, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_16_1B

	/**
	 * 40.1.10A : Copy to clipboard should be available to the left of the Cancel button - Context menu
	 */
	/*@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint40", "Get M-Files Web URL"}, 
			description = "Copy to clipboard should be available to the left of the Cancel button - Context menu")*/
	public void SprintTest40_1_10A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from context menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Verification : Verify Copy to clip board link is avaliable to the left of Cance button
			//--------------------------------------------------------------------------------------
			int xPosCopy = mfilesDialog.getCopyToClipboardPosition().get("XPos");
			int xPosCancelBtn = mfilesDialog.getCancelButtonPosition().get("XPos");

			String addlInfo = "---X Position---Copy to Clipboard : " + xPosCopy + "; Cancel Button : " + xPosCancelBtn;

			if (xPosCopy < xPosCancelBtn) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Copy to Hyperlink is available to the left of Cancel Button.");
			else
				Log.fail("Test case Failed. Copy to Hyperlink is not available to the left of Cancel Button. Additional information : " 
						+ addlInfo, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest40_1_10A

	/**
	 * 40.1.10B : Copy to clipboard should be available to the left of the Cancel button - Operations menu
	 */
	/*@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint40", "Get M-Files Web URL"}, 
			description = "Copy to clipboard should be available to the left of the Cancel button - Operations menu")*/
	public void SprintTest40_1_10B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from operations menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Verification : Verify Copy to clip board link is avaliable to the left of Cance button
			//--------------------------------------------------------------------------------------
			int xPosCopy = mfilesDialog.getCopyToClipboardPosition().get("XPos");
			int xPosCancelBtn = mfilesDialog.getCancelButtonPosition().get("XPos");

			String addlInfo = "---X Position---Copy to Clipboard : " + xPosCopy + "; Cancel Button : " + xPosCancelBtn;

			if (xPosCopy < xPosCancelBtn) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Copy to Hyperlink is available to the left of Cancel Button.");
			else
				Log.fail("Test case Failed. Copy to Hyperlink is not available to the left of Cancel Button. Additional information : " 
						+ addlInfo, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest40_1_10B


	/**
	 * 25.2.24A : Verify the Copy to clipboard in Get M-Files Web URL dialog in No Applet View - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL"}, 
			description = "Verify the Copy to clipboard in Get M-Files Web URL dialog in No Applet View - Context menu")
	public void SprintTest25_2_24A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 


		try {

			if (driverType.equalsIgnoreCase("IE"))
				throw new SkipException("Copy to Clipboard link will always be availble for '" + driverType + "' driver type irrespective of java applet");

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Applet' layout
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
			HomePage homePage = LoginPage.launchDriverAndLogin(driver,false); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Navigate to any view
			//-----------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5: Select the object and open Get M-Files Web URL dialog from context menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("5. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Verification : Verify if "Copy to Clipboard button" does not exists in the Get M-Files Web URL dialog
			//----------------------------------------------------------------------------------------------
			if (driverType.toUpperCase().equalsIgnoreCase("IE")) { //Copy to clipboard link should be avaialable in GetHyperlink dialog
				if (mfilesDialog.isCopyToClipBoardLinkExists()) //Verifies default layout have selected default custom layout
					Log.pass("Test case Passed. Copy to Clipboard link exists in IE Browser in No Applet view.");
				else
					Log.fail("Test case Failed. Copy to Clipboard link does exists in IE Browser in No Applet view.", driver);
			}
			else {
				if (!mfilesDialog.isCopyToClipBoardLinkExists()) //Verifies default layout have selected default custom layout
					Log.pass("Test case Passed. Copy to Clipboard link does not exists.");
				else
					Log.fail("Test case Failed. Copy to Clipboard link exists in 'No Java Applet' view", driver);
			}

			mfilesDialog.close(); //Closes MFiles dialog

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (!driverType.equalsIgnoreCase("IE")){
				try
				{
					Utility.logOut(driver);
					ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);
					configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view
					configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);	
					configurationPage.configurationPanel.saveSettings();
				}
				catch (Exception e0) {
					Log.exception(e0, driver);
				} //End catch
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_24A

	/**
	 * 25.2.24B : Verify the Copy to clipboard in Get M-Files Web URL dialog in No Applet View - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL"}, 
			description = "Verify the Copy to clipboard in Get M-Files Web URL dialog in No Applet View - Operations menu")
	public void SprintTest25_2_24B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 


		try {

			if (driverType.equalsIgnoreCase("IE"))
				throw new SkipException("Copy to Clipboard link will always be availble for '" + driverType + "' driver type irrespective of java applet");

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true);

			//Step-1 : Change layout to 'No Java Applet' layout
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

			//Step-4 : Navigate to any view
			//-----------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5: Select the object and open Get M-Files Web URL dialog from operations menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("5. Get M-Files Web URL dialog of an objectSprintTest40_1_10A (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Verification : Verify if "Copy to Clipboard button" does not exists in the Get M-Files Web URL dialog
			//----------------------------------------------------------------------------------------------
			if (driverType.toUpperCase().equalsIgnoreCase("IE")) { //Copy to clipboard link should be avaialable in GetHyperlink dialog
				if (mfilesDialog.isCopyToClipBoardLinkExists()) //Verifies default layout have selected default custom layout
					Log.pass("Test case Passed. Copy to Clipboard link exists in IE Browser in No Applet view.");
				else
					Log.fail("Test case Failed. Copy to Clipboard link does exists in IE Browser in No Applet view.", driver);
			}
			else {
				if (!mfilesDialog.isCopyToClipBoardLinkExists()) //Verifies default layout have selected default custom layout
					Log.pass("Test case Passed. Copy to Clipboard link does not exists.");
				else
					Log.fail("Test case Failed. Copy to Clipboard link exists in 'No Java Applet' view", driver);
			}

			mfilesDialog.close(); //Closes MFiles dialog

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (driverType.equalsIgnoreCase("IE")) {
				try
				{
					Utility.logOut(driver);
					ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);
					configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view
					configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);		
					configurationPage.configurationPanel.saveSettings();
				}
				catch (Exception e0) {
					Log.exception(e0, driver);
				} //End catch
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_24B


	/**
	 * 38131_1 : Select the 'Download selected file' in the GetMfilesWebURL dialog and opened the link in new tab.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL"}, 
			description = "Select the 'Download selected file' in the GetMfilesWebURL dialog and opened the link in new tab.")
	public void SprintTest38131_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Logged into the MFWA
			//--------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Click the search button and navigate to any view
			//---------------------------------------------------------
			homePage.searchPanel.clickSearch();

			Log.message("1. Clicked the Search button and navigate to the specified view.", driver);

			//Step-2 : Selected the any specified object in the Search view
			//--------------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectName"));

			Log.message("2. Selected the specified object : "+dataPool.get("ObjectName")+" from the navigate to view.", driver);

			//Step-3 : Selected the 'Get mfiles web url' option from the Operations menu
			//--------------------------------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			Log.message("3. Selected the " + Caption.MenuItems.GetMFilesWebURL.Value + " in the operations menu.", driver);

			//Verify if mfiles dialog is opened or not
			//----------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("4. Get M-files web url dialog is opened for the object : " + dataPool.get("ObjectName"), driver);

			//Step-5 : Select 'Show the Selected object' option in GetMFilesWebURL dialog
			//-------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("DOWNLOAD FILE")) //Selects Show object
				throw new Exception("Download the selected file is not selected in GetMFilesWebURL.");

			Log.message("5. 'Download the selected file' is selected in GetMFilesWebURL.");

			//Step-6 : Selected the hyperlink url from the GetMfilesWebURL
			//------------------------------------------------------------
			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton();// close(); //Closes M-Files dialog

			Log.message("6. Copy the hyperlink url in GetMFilesWebURL dialog.", driver);

			//Step-5 : Log out from current session
			//-------------------------------------
			if (!Utility.logOut(driver)) //Logs out from the vault
				throw new Exception ("Log out from vault (" + testVault + ") is unsuccessful.");

			Log.message("7. Log out from vault (" + testVault + ") is successful.");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL, userName, password, ""); //Navigates to the URL in the hyperlink dialog

			Log.message("8. Pasted the URL copied from hyperlink dialog and logged in with valid credentials.");

			//Verification : Verify if URL 
			if (driver.getCurrentUrl().toUpperCase().contains( loginURL.toUpperCase() + "?URL=OPENFILE.ASPX"))
				Log.pass("Test Case Passed.Object : " +dataPool.get("ObjectName")+ " link for 'Download the selected file' is opened in new tab.");
			else 
				Log.fail("Test Case Failed.Download Object link is not opened in new tab.", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally{
			Utility.quitDriver(driver);
		}//End finally

	}//End SprintTest38131_1


	/**
	 * 34289 : Verify if check url gets redirect to Error page when Specifying with incorrect File GUID.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL","9920"}, 
			description = "Verify if check url gets redirect to Error page when Specifying with incorrect File GUID.")
	public void SprintTest34289(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Logged into the MFWA
			//--------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : navigate to any search view
			//------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("MFDObject"));

			Log.message("1. Navigated to the " +  viewToNavigate + " view.");

			//Step-2 : Select the specified MFD object in the Search view
			//--------------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("MFDObject"));
			homePage.listView.expandItemByName(dataPool.get("MFDObject"));

			Log.message("2. Expand the selected MFD object : "+dataPool.get("MFDObject"), driver);

			//Step-3 : Select the any add file document in the MFD object
			//-----------------------------------------------------------
			homePage.listView.rightClickItem(dataPool.get("ObjectName"));

			Log.message("3. Right clicked the added file object : " +dataPool.get("ObjectName"));

			//Step-4 : Selected the 'Get mfiles web url' option from the Operations menu
			//--------------------------------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			Log.message("3. Selected the " + Caption.MenuItems.GetMFilesWebURL.Value + " in the operations menu.", driver);

			//Verify if mfiles dialog is opened or not
			//----------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("4. Get M-files web url dialog is opened for the object : " + dataPool.get("ObjectName"), driver);

			//Step-5 : Select 'Show the Selected object' option in GetMFilesWebURL dialog
			//-------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("DOWNLOAD FILE")) //Selects Show object
				throw new Exception("Download the selected file is not selected in GetMFilesWebURL.");

			Log.message("5. 'Download the selected file' is selected in GetMFilesWebURL.");

			//Step-6 : Selected the hyperlink url from the GetMfilesWebURL
			//------------------------------------------------------------
			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton();// close(); //Closes M-Files dialog

			Log.message("6. Copy the hyperlink url in GetMFilesWebURL dialog.", driver);

			if(!hyperlinkURL.contains("fileGUID"))//Verify if hyperlink url is displayed with fileGUID
				throw new Exception("Hyperlink url is not displayed as expected.");

			String[] getHyperlink = hyperlinkURL.split("&");
			String FileGUID = getHyperlink[getHyperlink.length-2];

			String invalidURL= hyperlinkURL.replaceAll(FileGUID, "fileGUID=invalidGUID");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			driver.get(invalidURL); //Navigates to the URL in the hyperlink dialog

			Log.message("8. Pasted the URL copied from hyperlink dialog and edited the url with invalid id." + invalidURL);

			//Verification : Verify if Error page is displayed for the invalid url
			//--------------------------------------------------------------------
			if (driver.findElement(By.id("message")).getText().contains("Error executing child request for /openfile.aspx."))
				Log.pass("Test Case Passed.Error page is displayed for the selected Object : " +dataPool.get("ObjectName")+ " when specified the incorrect file GUID. .");
			else 
				Log.fail("Test Case Failed.Error page is  not displayed for the selected Object : " +dataPool.get("ObjectName")+ " when specified the incorrect file GUID. ", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally{
			Utility.quitDriver(driver);
		}//End finally

	}//End SprintTest34289

	/**
	 * 34286 : Verify if url gets redirect to Error page when Specifying with incorrect object ID.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL","9920"}, 
			description = "Verify if url gets redirect to Error page when Specifying with incorrect object ID.")
	public void SprintTest34286(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Logged into the MFWA
			//--------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : navigate to any search view
			//------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("MFDObject"));

			Log.message("1. Navigated to the " +  viewToNavigate + " view.");

			//Step-2 : Select the specified MFD object in the Search view
			//--------------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("MFDObject"));
			homePage.listView.expandItemByName(dataPool.get("MFDObject"));

			Log.message("2. Expand the selected MFD object : "+dataPool.get("MFDObject"), driver);

			//Step-3 : Select the any add file document in the MFD object
			//-----------------------------------------------------------
			homePage.listView.rightClickItem(dataPool.get("ObjectName"));

			Log.message("3. Right clicked the added file object : " +dataPool.get("ObjectName"));

			//Step-4 : Selected the 'Get mfiles web url' option from the Operations menu
			//--------------------------------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			Log.message("3. Selected the " + Caption.MenuItems.GetMFilesWebURL.Value + " in the operations menu.", driver);

			//Verify if mfiles dialog is opened or not
			//----------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("4. Get M-files web url dialog is opened for the object : " + dataPool.get("ObjectName"), driver);

			//Step-5 : Select 'Show the Selected object' option in GetMFilesWebURL dialog
			//-------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("DOWNLOAD FILE")) //Selects Show object
				throw new Exception("Download the selected file is not selected in GetMFilesWebURL.");

			Log.message("5. 'Download the selected file' is selected in GetMFilesWebURL.");

			//Step-6 : Selected the hyperlink url from the GetMfilesWebURL
			//------------------------------------------------------------
			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton();// close(); //Closes M-Files dialog

			Log.message("6. Copy the hyperlink url in GetMFilesWebURL dialog.", driver);

			if(!hyperlinkURL.contains("objectGUID"))//Verify if hyperlink url is displayed with fileGUID
				throw new Exception("Hyperlink url is not displayed as expected.");

			String[] getHyperlink = hyperlinkURL.split("&");
			String objectGUID = getHyperlink[getHyperlink.length-3];

			String invalidURL= hyperlinkURL.replaceAll(objectGUID, "objectGUID=invalidGUID");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			driver.get(invalidURL); //Navigates to the URL in the hyperlink dialog

			Log.message("8. Pasted the URL copied from hyperlink dialog and edited the url with invalid object GUID.");

			//Verification : Verify if Error page is displayed for the invalid url
			//--------------------------------------------------------------------
			if (driver.findElement(By.id("message")).getText().contains("Error executing child request for /openfile.aspx."))
				Log.pass("Test Case Passed.Error page is displayed for the selected Object : " +dataPool.get("ObjectName")+ " when specified the incorrect object GUID. .");
			else 
				Log.fail("Test Case Failed.Error page is  not displayed for the selected Object : " +dataPool.get("ObjectName")+ " when specified the incorrect object GUID. ", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally{
			Utility.quitDriver(driver);
		}//End finally

	}//End SprintTest34286

	/**
	 * Commented cases are obselte due to the -	Functionality "EmbedAuthenticationToken" is deprecated due to User story #M-10068: [PO] I want to deprecate supporting "Embed my username and password to the link" from Get M-Files Web URL in M-Files 2017
	 * 
	 * 
	 * 
	 * 25.2.12.3A : 'Embed authentication details' should not be selected by default - Context menu
	 *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "Show members"}, 
			description = "''Embed authentication details' should not be selected by default - Context menu")
	public void SprintTest25_2_12_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from context menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Verification : To Verify if Embed Authentication Details is not selected by default
			//-----------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperlinkAuthenticationEmbed()) //Verifies if member object exists in the hyperlink
				Log.pass("Test case Passed. 'Embed Authentication Details' in Get M-Files Web URL dialog is not selected by default.");
			else
				Log.fail("Test case Failed. 'Embed Authentication Details' in Get M-Files Web URL dialog is not selected by default.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_12_3A

	  *//**
	  * 25.2.12.3B : 'Embed authentication details' should not be selected by default - Operations menu
	  *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL"}, 
			description = "''Embed authentication details' should not be selected by default - Operations menu")
	public void SprintTest25_2_12_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from operations menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Verification : To Verify if Embed Authentication Details is not selected by default
			//-----------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperlinkAuthenticationEmbed()) //Verifies if member object exists in the hyperlink
				Log.pass("Test case Passed. 'Embed Authentication Details' in Get M-Files Web URL dialog is not selected by default.");
			else
				Log.fail("Test case Failed. 'Embed Authentication Details' in Get M-Files Web URL dialog is not selected by default.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_12_3B
	   */




}//End WebURLDialog

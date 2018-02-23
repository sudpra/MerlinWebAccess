package MFClient.Tests.GetMFilesWebURL;

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




import MFClient.Pages.HomePage;
import MFClient.Pages.LoginPage;
import MFClient.Wrappers.Caption;
import MFClient.Wrappers.ListView;
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class ObjectWebURL {

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
	 * 25.2.5A : Open the Hyperlink copied from hyperlink textbox - Context menu 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "Smoke"}, 
			description = "Open the Hyperlink copied from hyperlink textbox - Context menu.")
	public void SprintTest25_2_5A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-4 : Copy the link from text box and close the Get M-Files Web URL dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied and Get M-Files Web URL dialog is closed. Hyperlink URL : " + hyperlinkText, driver);

			//Step-5 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver.get(hyperlinkText); //Navigates to the hyperlink url
			Utils.fluentWait(driver);

			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied[" + hyperlinkText + "] object Hyperlink. Actual URL loaded : " + driver.getCurrentUrl());

			Log.message("5. Object Hyperlink is opened in the browser.");

			//Verification : To Verify if object exists in the URL
			//----------------------------------------------------
			homePage = new HomePage(driver);

			if (homePage.listView.itemCount() != 1) {  //Verifies if only one object is displayed in the list
				Log.fail("Test case Failed. One item is not getting displayed on opening the hyperlink.", driver);
				return;
			}

			if (homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Verifies if object exists in the hyperlink
				Log.pass("Test case Passed. Object (" + dataPool.get("ObjectName") + ") exists in the hyperlink.");
			else
				Log.fail("Test case Failed. Object (" + dataPool.get("ObjectName") + ") does not exists in the hyperlink.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_5A

	/**
	 * 25.2.5B : Open the Hyperlink copied from hyperlink textbox - Operations menu 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL"}, 
			description = "Open the Hyperlink copied from hyperlink textbox - Operations menu.")
	public void SprintTest25_2_5B(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-4 : Copy the link from text box and close the Get M-Files Web URL dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied and Get M-Files Web URL dialog is closed. Hyperlink URL : " + hyperlinkText, driver);

			//Step-5 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver.get(hyperlinkText); //Navigates to the hyperlink url
			Utils.fluentWait(driver);

			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied[" + hyperlinkText + "] object Hyperlink. Actual URL loaded : " + driver.getCurrentUrl());

			Log.message("5. Object Hyperlink is opened in the browser.");

			//Verification : To Verify if object exists in the URL
			//----------------------------------------------------
			homePage = new HomePage(driver);

			//Verifies if only one object is displayed in the list
			if (homePage.listView.itemCount() != 1) { 
				Log.fail("Test case Failed. One item is not getting displayed on opening the hyperlink.", driver);
				return;
			}

			//Verifies if object exists in the hyperlink
			if (homePage.listView.isItemExists(dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object (" + dataPool.get("ObjectName") + ") exists in the hyperlink.");
			else
				Log.fail("Test case Failed. Object (" + dataPool.get("ObjectName") + ") does not exists in the hyperlink.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_5B

	/**
	 * 25.2.6A : Open the Hyperlink copied using Copy to Clipboard link - Context menu 
	 */
	/*@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL"}, 
			description = "Open the Hyperlink copied using Copy to Clipboard link - Context menu .")*/
	public void SprintTest25_2_6A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			//--------------------------------------
			if (!mfilesDialog.isCopyToClipBoardLinkExists()) //Checks if Copy to clipboard link exists
				throw new Exception("Copy to clipboard link does not exists.");

			mfilesDialog.clickCopyToClipBoardLink(); //Clicks Copy to Clipboard link

			String hyperlinkText = Utility.getTextFromClipboard(); //Gets the copied text from clipboard

			Log.message("4. Copy to Clipboard link is clicked in Get M-Files Web URL dialog.");

			//Step-5 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver.get(hyperlinkText); //Navigates to the hyperlink url
			Utils.fluentWait(driver);

			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied[" + hyperlinkText + "] object Hyperlink. Actual URL loaded : " + driver.getCurrentUrl());

			Log.message("5. Object Hyperlink is opened in the browser.");

			//Verification : To Verify if object exists in the URL
			//----------------------------------------------------
			homePage = new HomePage(driver);

			if (homePage.listView.itemCount() != 1) { //Verifies if only one object is displayed in the list 
				Log.fail("Test case Failed. One item is not getting displayed on opening the hyperlink.", driver);
				return;
			}

			if (homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Verifies if object exists in the hyperlink
				Log.pass("Test case Passed. Object (" + dataPool.get("ObjectName") + ") exists in the hyperlink.");
			else
				Log.fail("Test case Failed. Object (" + dataPool.get("ObjectName") + ") does not exists in the hyperlink.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_6A

	/**
	 * 25.2.6B :  Open the Hyperlink copied using Copy to Clipboard link - Operations menu 
	 */
	/*@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL"}, 
			description = "Open the Hyperlink copied using Copy to Clipboard link - Operations menu.")*/
	public void SprintTest25_2_6B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			//--------------------------------------
			if (!mfilesDialog.isCopyToClipBoardLinkExists()) //Checks if Copy to clipboard link exists
				throw new Exception("Copy to clipboard link does not exists.");

			mfilesDialog.clickCopyToClipBoardLink(); //Clicks Copy to Clipboard link

			String hyperlinkText = Utility.getTextFromClipboard(); //Gets the copied text from clipboard

			Log.message("4. Copy to Clipboard link is clicked in Get M-Files Web URL dialog.");

			//Step-5 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver.get(hyperlinkText); //Navigates to the hyperlink url
			Utils.fluentWait(driver);

			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied[" + hyperlinkText + "] object Hyperlink. Actual URL loaded : " + driver.getCurrentUrl());

			Log.message("5. Object Hyperlink is opened in the browser.");

			//Verification : To Verify if object exists in the URL
			//----------------------------------------------------
			homePage = new HomePage(driver);

			//Verifies if only one object is displayed in the list
			if (homePage.listView.itemCount() != 1) { 
				Log.fail("Test case Failed. One item is not getting displayed on opening the hyperlink.", driver);
				return;
			}

			//Verifies if object exists in the hyperlink
			if (homePage.listView.isItemExists(dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object (" + dataPool.get("ObjectName") + ") exists in the hyperlink.");
			else
				Log.fail("Test case Failed. Object (" + dataPool.get("ObjectName") + ") does not exists in the hyperlink.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_6B

	/**
	 * 25.2.7C : Open the Hyperlink of latest version in History view - Context menu 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "History"}, 
			description = "Open the Hyperlink of latest version in History view - Context menu.")
	public void SprintTest25_2_7C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Open the History View of the object
			//--------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Click on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Click the History link in the task pane
			Utils.fluentWait(driver);

			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History View of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open Get M-Files Web URL dialog for the object from context menu
			//-------------------------------------------------------------------
			String latestVersion = homePage.listView.getColumnValueByItemIndex(0, "Version"); //Gets the version of the latest

			if (!homePage.listView.rightClickItemByIndex(0)) //Selects the latest version of an object in the list
				throw new Exception("Latest version of an object is not right clicked.");

			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.GetMFilesWebURL.Value) ) //Verifies if Get M-Files Web URL is enabled for latest version
				throw new Exception("Get M-Files Web URL is not enabled for latest version of an object (" + dataPool.get("ObjectName") + ") in context menu.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			if (!MFilesDialog.exists(driver))
				throw new Exception("Get M-Files Web URL dialog is not opened.");

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened through context menu.");

			//Step-4 : Copy Hyperlink from text box
			//--------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied from the text box.");

			//Step-5 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver.get(hyperlinkText); //Navigates to the hyperlink url
			Utils.fluentWait(driver);

			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied[" + hyperlinkText + "] object Hyperlink. Actual URL loaded : " + driver.getCurrentUrl());

			Log.message("5. Object Hyperlink is opened in the browser.");

			//Verification : To Verify if object exists in the URL
			//----------------------------------------------------
			homePage = new HomePage(driver);

			if (homePage.listView.itemCount() != 1) { //Verifies if only one object is displayed in the list 
				Log.fail("Test case Failed. One item is not getting displayed on opening the hyperlink.", driver);
				return;
			}

			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) { //Verifies if object exists in the hyperlink
				Log.fail("Test case Failed. Object (" + dataPool.get("ObjectName") + ") does not exists in the hyperlink.", driver);
				return;
			}

			if (!homePage.listView.getColumnValueByItemIndex(0, "Version").equals(latestVersion)) { //Verifies if object with correct version is displayed
				Log.fail("Test case Failed. Object (" + dataPool.get("ObjectName") + ") with version " + latestVersion + " is not displayed.", driver);
				return;
			}

			if (driver.getCurrentUrl().toUpperCase().endsWith("LATEST")) //Verifies if latest is available in the hyperlink URL
				Log.pass("Test case Passed. Hyperlink of an object (" + dataPool.get("ObjectName") + ") is opened successfully.");
			else
				Log.fail("Test case Failed. Hyperlink does not have 'latest' in its URL.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_7C

	/**
	 * 25.2.7D : Open the Hyperlink of latest version in History view - Operations menu 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "History"}, 
			description = "Open the Hyperlink of latest version in History view - Operations menu.")
	public void SprintTest25_2_7D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Open the History View of the object
			//--------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Click on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Click the History link in the task pane
			Utils.fluentWait(driver);

			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History View of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open Get M-Files Web URL dialog for the object from context menu
			//-------------------------------------------------------------------
			if (!homePage.listView.clickItemByIndex(0)) //Clicks on latest version of an object
				throw new Exception("Latest version of an object is not selected.");

			String latestVersion = homePage.listView.getColumnValueByItemIndex(0, "Version"); //Gets the version of the latest
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			if (!MFilesDialog.exists(driver))
				throw new Exception("Get M-Files Web URL dialog is not opened.");

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-4 : Copy Hyperlink from text box
			//--------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied from the text box.");

			//Step-5 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver.get(hyperlinkText); //Navigates to the hyperlink URL
			Utils.fluentWait(driver);

			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied[" + hyperlinkText + "] object Hyperlink. Actual URL loaded : " + driver.getCurrentUrl());

			Log.message("5. Object Hyperlink is opened in the browser.");

			//Verification : To Verify if object exists in the URL
			//----------------------------------------------------
			homePage = new HomePage(driver);

			if (homePage.listView.itemCount() != 1) { //Verifies if only one object is displayed in the list 
				Log.fail("Test case Failed. One item is not getting displayed on opening the hyperlink.", driver);
				return;
			}

			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) { //Verifies if object exists in the hyperlink
				Log.fail("Test case Failed. Object (" + dataPool.get("ObjectName") + ") does not exists in the hyperlink.", driver);
				return;
			}

			if (!homePage.listView.getColumnValueByItemIndex(0, "Version").equals(latestVersion)) { //Verifies if object with correct version is displayed
				Log.fail("Test case Failed. Object (" + dataPool.get("ObjectName") + ") with version " + latestVersion + " is not displayed.", driver);
				return;
			}

			if (driver.getCurrentUrl().toUpperCase().endsWith("LATEST")) //Verifies if latest is available in the hyperlink URL
				Log.pass("Test case Passed. Hyperlink of an object (" + dataPool.get("ObjectName") + ") is opened successfully.");
			else
				Log.fail("Test case Failed. Hyperlink does not have 'latest' in its URL.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_7D

	/**
	 * 25.2.8C : Open the Hyperlink of older version in History view - Context menu 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "History"}, 
			description = "Open the Hyperlink copied of older version in History view - Context menu.")
	public void SprintTest25_2_8C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Open the History View of the object
			//--------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Click on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Click the History link in the task pane
			Utils.fluentWait(driver);

			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History View of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open Get M-Files Web URL dialog for the object from context menu
			//-------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 1) //Checks if older version of an object exists
				throw new SkipException("Older versions are not available for object (" + dataPool.get("ObjectName") + ").");

			String olderVersion = homePage.listView.getColumnValueByItemIndex(1, "Version"); //Gets the version of the object
			String objectName = homePage.listView.getColumnValueByItemIndex(1, "Name"); //Gets the name of the older version of object

			if (!homePage.listView.rightClickItemByIndex(1)) //Right clicks on Older version of an object
				throw new Exception("Older Version of an object is not right clicked.");

			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.GetMFilesWebURL.Value) ) //Verifies if Get M-Files Web URL is enabled for latest version
				throw new Exception("Get M-Files Web URL is not enabled for older version of an object (" + dataPool.get("ObjectName") + ") in context menu.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			if (!MFilesDialog.exists(driver))
				throw new Exception("Get M-Files Web URL dialog is not opened.");

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-4 : Copy Hyperlink from text box
			//--------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied from the text box.");

			//Step-5 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver.get(hyperlinkText); //Navigates to the hyperlink url
			Utils.fluentWait(driver);

			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied[" + hyperlinkText + "] object Hyperlink. Actual URL loaded : " + driver.getCurrentUrl());

			Log.message("5. Object Hyperlink is opened in the browser.");

			//Verification : To Verify if object exists in the URL
			//----------------------------------------------------
			homePage = new HomePage(driver);

			if (homePage.listView.itemCount() != 1) { //Verifies if only one object is displayed in the list 
				Log.fail("Test case Failed. One item is not getting displayed on opening the hyperlink.", driver);
				return;
			}

			if (!homePage.listView.isItemExists(objectName)) { //Verifies if object exists in the hyperlink
				Log.fail("Test case Failed. Object (" + objectName + ") does not exists in the hyperlink.", driver);
				return;
			}

			//Verifies if object with correct version is displayed
			if (!homePage.listView.getColumnValueByItemIndex(0, "Version").equals(olderVersion)) {
				Log.fail("Test case Failed. Object (" + dataPool.get("ObjectName") + ") with version " + olderVersion + " is not displayed.", driver);
				return;
			}

			if (driver.getCurrentUrl().toUpperCase().endsWith(olderVersion)) //Verifies if latest is available in the hyperlink URL
				Log.pass("Test case Passed. Hyperlink of an object (" + dataPool.get("ObjectName") + ") is opened successfully.");
			else
				Log.fail("Test case Failed. Hyperlink does not have 'latest' in its URL.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_8C

	/**
	 * 25.2.8D : Open the Hyperlink of older version in History view - Operations menu 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "History"}, 
			description = "Open the Hyperlink of older version in History view - Operations menu.")
	public void SprintTest25_2_8D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Open the History View of the object
			//--------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Click on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Click the History link in the task pane
			Utils.fluentWait(driver);

			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History View of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open Get M-Files Web URL dialog for the object from operations menu
			//---------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 1) //Checks if older version of an object exists
				throw new SkipException("Older versions are not available for object (" + dataPool.get("ObjectName") + ").");

			String olderVersion = homePage.listView.getColumnValueByItemIndex(1, "Version"); //Gets the version of the object
			String objectName = homePage.listView.getColumnValueByItemIndex(1, "Name"); //Gets the name of the older version of object

			if (!homePage.listView.clickItemByIndex(1)) //Clicks on older version of an object
				throw new Exception("Older Version of an object is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			if (!MFilesDialog.exists(driver))
				throw new Exception("Get M-Files Web URL dialog is not opened.");

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-4 : Copy Hyperlink from text box
			//--------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied from the text box.");

			//Step-5 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver.get(hyperlinkText); //Navigates to the hyperlink url
			Utils.fluentWait(driver);

			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied[" + hyperlinkText + "] object Hyperlink. Actual URL loaded : " + driver.getCurrentUrl());

			Log.message("5. Object Hyperlink is opened in the browser.");

			//Verification : To Verify if object exists in the URL
			//----------------------------------------------------
			homePage = new HomePage(driver);

			if (homePage.listView.itemCount() != 1) { //Verifies if only one object is displayed in the list 
				Log.fail("Test case Failed. One item is not getting displayed on opening the hyperlink.", driver);
				return;
			}

			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) { //Verifies if object exists in the hyperlink
				Log.fail("Test case Failed. Object (" + dataPool.get("ObjectName") + ") does not exists in the hyperlink.", driver);
				return;
			}

			if (!homePage.listView.getColumnValueByItemIndex(0, "Version").equals(olderVersion)) { //Verifies if object with correct version is displayed
				Log.fail("Test case Failed. Object (" + dataPool.get("ObjectName") + ") with version " + olderVersion + " is not displayed.", driver);
				return;
			}

			if (driver.getCurrentUrl().toUpperCase().endsWith(olderVersion)) //Verifies if older is available in the hyperlink URL
				Log.pass("Test case Passed. Hyperlink of an object (" + objectName + ") is opened successfully.");
			else
				Log.fail("Test case Failed. Hyperlink does not have '" + olderVersion + "' in its URL.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_8D

	/**
	 * 25.2.9C : Open the Hyperlink of related object in Relationships view - Context menu  
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "Relationship"}, 
			description = "Open the Hyperlink of related object in Relationships view - Context menu.")
	public void SprintTest25_2_9C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Open the Relationships View of the object
			//--------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Click on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu("Relationships"); //Click the Relationships link in the task pane
			Utils.fluentWait(driver);

			if (!ListView.isRelationshipsViewOpened(driver))
				throw new Exception("Relationships view of an Object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. Relationships View of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open Get M-Files Web URL dialog for the object from context menu
			//-------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0) //Checks if object has any related object
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") does not has any related objects.");

			String relatedObject = homePage.listView.getColumnValueByItemIndex(0, "Name");

			if (!homePage.listView.rightClickItemByIndex(0)) //Right clicks on related object
				throw new Exception ("Related object is not right clicked.");

			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.GetMFilesWebURL.Value) ) //Verifies if Get M-Files Web URL is enabled for latest version
				throw new Exception("Get M-Files Web URL is not enabled for related object in context menu.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			if (!MFilesDialog.exists(driver))
				throw new Exception("Get M-Files Web URL dialog is not opened.");

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-4 : Copy Hyperlink from text box
			//--------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied from the text box.");

			//Step-5 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver.get(hyperlinkText); //Navigates to the hyperlink url
			Utils.fluentWait(driver);

			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied[" + hyperlinkText + "] object Hyperlink. Actual URL loaded : " + driver.getCurrentUrl());

			Log.message("5. Object Hyperlink is opened in the browser.");

			//Verification : To Verify if object exists in the URL
			//----------------------------------------------------
			homePage = new HomePage(driver);

			if (homePage.listView.itemCount() != 1) { //Verifies if only one object is displayed in the list 
				Log.fail("Test case Failed. One item is not getting displayed on opening the hyperlink.", driver);
				return;
			}

			if (homePage.listView.isItemExists(relatedObject)) //Verifies if related object exists in the hyperlink
				Log.pass("Test case Passed. Hyperlink of an related object (" + relatedObject + ") is opened successfully.");
			else
				Log.fail("Test case Failed. Related object (" + relatedObject + ") does not exists in the hyperlink.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_9C

	/**
	 * 25.2.9D : Open the Hyperlink of related object in Relationships view - Operations menu  
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "Relationship"}, 
			description = "Open the Hyperlink of related object in Relationships view - Operations menu.")
	public void SprintTest25_2_9D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {


			driver = WebDriverUtils.getDriver();			

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Open the Relationships View of the object
			//--------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Click on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu("Relationships"); //Click the Relationships link in the task pane
			Utils.fluentWait(driver);

			if (!ListView.isRelationshipsViewOpened(driver))
				throw new Exception("Relationships view of an Object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. Relationships View of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open Get M-Files Web URL dialog for the object from operations menu
			//---------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0) //Checks if object has any related object
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") does not has any related objects.");

			String relatedObject = homePage.listView.getColumnValueByItemIndex(0, "Name");

			if(!homePage.listView.clickItemByIndex(0)) //Clicks on related of an object
				throw new Exception("Related object is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			if (!MFilesDialog.exists(driver))
				throw new Exception("Get M-Files Web URL dialog is not opened.");

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-4 : Copy Hyperlink from text box
			//--------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied from the text box.");

			//Step-5 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver.get(hyperlinkText); //Navigates to the hyperlink url
			Utils.fluentWait(driver);

			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied[" + hyperlinkText + "] object Hyperlink. Actual URL loaded : " + driver.getCurrentUrl());

			Log.message("5. Object Hyperlink is opened in the browser.");

			//Verification : To Verify if object exists in the URL
			//----------------------------------------------------
			homePage = new HomePage(driver);

			if (homePage.listView.itemCount() != 1) { //Verifies if only one object is displayed in the list 
				Log.fail("Test case Failed. One item is not getting displayed on opening the hyperlink.", driver);
				return;
			}

			if (homePage.listView.isItemExists(relatedObject)) //Verifies if related object exists in the hyperlink
				Log.pass("Test case Passed. Hyperlink of an related object (" + relatedObject + ") is opened successfully.");
			else
				Log.fail("Test case Failed. Related object (" + relatedObject + ") does not exists in the hyperlink.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_9D

	/**
	 * 25.2.10C : Open the Hyperlink of sub-object in Sub Objects view - Context menu 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "Subobjects"}, 
			description = "Open the Hyperlink of sub-object in Sub Objects view - Context menu")
	public void SprintTest25_2_10C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Open the Subobjects View of the object
			//--------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Click on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.ShowSubObjects.Value); //Click the Show Subobjects link in the task pane
			Utils.fluentWait(driver);

			if (!ListView.isSubObjectsViewOpened(driver))
				throw new Exception("Subobjects view of an Object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. Subobjects View of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open Get M-Files Web URL dialog for the object from operations menu
			//---------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0)
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") does not has any Subobject.");

			String relatedObject = homePage.listView.getColumnValueByItemIndex(0, "Name");

			if (!homePage.listView.rightClickItemByIndex(0)) //Right clicks on related object
				throw new Exception("Related object is not right clicked.");

			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.GetMFilesWebURL.Value) ) //Verifies if Get M-Files Web URL is enabled for latest version
				throw new Exception("Get M-Files Web URL is not enabled for related object in context menu.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			if (!MFilesDialog.exists(driver))
				throw new Exception("Get M-Files Web URL dialog is not opened.");

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of an object (" + relatedObject + ") is opened.");

			//Step-4 : Copy Hyperlink from text box
			//--------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied from the text box.");

			//Step-5 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver.get(hyperlinkText); //Navigates to the hyperlink url
			Utils.fluentWait(driver);

			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied[" + hyperlinkText + "] object Hyperlink. Actual URL loaded : " + driver.getCurrentUrl());

			Log.message("5. Object Hyperlink is opened in the browser.");

			//Verification : To Verify if object exists in the URL
			//----------------------------------------------------
			homePage = new HomePage(driver);

			if (homePage.listView.itemCount() != 1) { //Verifies if only one object is displayed in the list 
				Log.fail("Test case Failed. One item is not getting displayed on opening the hyperlink.", driver);
				return;
			}

			if (homePage.listView.isItemExists(relatedObject)) //Verifies if related object exists in the hyperlink
				Log.pass("Test case Passed. Hyperlink of an related object (" + relatedObject + ") is opened successfully.");
			else
				Log.fail("Test case Failed. Related object (" + relatedObject + ") does not exists in the hyperlink.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_10C

	/**
	 * 25.2.10D : Open the Hyperlink of sub-object in Sub Objects view - Operations menu 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "Subobjects"}, 
			description = "Open the Hyperlink of sub-object in Sub Objects view - Context menu")
	public void SprintTest25_2_10D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Open the Subobjects View of the object
			//--------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Click on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.ShowSubObjects.Value); //Click the Show Subobjects link in the task pane
			Utils.fluentWait(driver);

			if (!ListView.isSubObjectsViewOpened(driver))
				throw new Exception("Subobjects view of an Object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. Subobjects View of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open Get M-Files Web URL dialog for the object from operations menu
			//---------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0)
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") does not has any Subobject.");

			String relatedObject = homePage.listView.getColumnValueByItemIndex(0, "Name");

			if (!homePage.listView.clickItemByIndex(0)) //Clicks on related object
				throw new Exception("Related object is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			if (!MFilesDialog.exists(driver))
				throw new Exception("Get M-Files Web URL dialog is not opened.");

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of an object (" + relatedObject + ") is opened.");

			//Step-4 : Copy Hyperlink from text box
			//--------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied from the text box.");

			//Step-5 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver.get(hyperlinkText); //Navigates to the hyperlink url
			Utils.fluentWait(driver);

			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied[" + hyperlinkText + "] object Hyperlink. Actual URL loaded : " + driver.getCurrentUrl());

			Log.message("5. Object Hyperlink is opened in the browser.");

			//Verification : To Verify if object exists in the URL
			//----------------------------------------------------
			homePage = new HomePage(driver);

			if (homePage.listView.itemCount() != 1) { //Verifies if only one object is displayed in the list 
				Log.fail("Test case Failed. One item is not getting displayed on opening the hyperlink.", driver);
				return;
			}

			if (homePage.listView.isItemExists(relatedObject)) //Verifies if related object exists in the hyperlink
				Log.pass("Test case Passed. Hyperlink of an related object (" + relatedObject + ") is opened successfully.");
			else
				Log.fail("Test case Failed. Related object (" + relatedObject + ") does not exists in the hyperlink.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_10D

	/**
	 * 25.2.11C : Open the Hyperlink of Collection members in Show members view - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "Show members"}, 
			description = "Open the Hyperlink of Collection members in Show members view - Context menu.")
	public void SprintTest25_2_11C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Open the Show members View of the object
			//-------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!homePage.listView.insertColumn("Object Type")) //Inserts Object Type column if not exists
				throw new SkipException("Object type column is not inserted.");

			if (!homePage.listView.getColumnValueByItemName(dataPool.get("ObjectName"), "Object Type").equalsIgnoreCase("DOCUMENT COLLECTION")) //Checks if Document collection object is selected
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") is not document collection object type.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Click on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("Show Members"); //Click the Show Members link in the task pane
			Utils.fluentWait(driver);

			if (!ListView.isMembersViewOpened(driver)) //Checks if collection member view is opened
				throw new Exception("Collection members view of an Object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. Collection members View of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open Get M-Files Web URL dialog for the collection member from context menu
			//------------------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0) //Checks if collection memeber exists in the list
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") does not has any collection members.");

			String memberObject = homePage.listView.getColumnValueByItemIndex(0, "Name");

			if (!homePage.listView.rightClickItemByIndex(0)) //Right clicks on collection member object
				throw new Exception("Member object is not right clicked.");

			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.GetMFilesWebURL.Value) ) //Verifies if Get M-Files Web URL is enabled for latest version
				throw new Exception("Get M-Files Web URL is not enabled for member object in context menu.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			if (!MFilesDialog.exists(driver))
				throw new Exception("Get M-Files Web URL dialog is not opened.");

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of member object (" + memberObject + ") is opened.");

			//Step-4 : Copy Hyperlink from text box
			//--------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied from the text box.");

			//Step-5 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver.get(hyperlinkText); //Navigates to the hyperlink url
			Utils.fluentWait(driver);

			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied[" + hyperlinkText + "] object Hyperlink. Actual URL loaded : " + driver.getCurrentUrl());

			Log.message("5. Object Hyperlink is opened in the browser.");

			//Verification : To Verify if object exists in the URL
			//----------------------------------------------------
			homePage = new HomePage(driver);

			if (homePage.listView.itemCount() != 1) { //Verifies if only one object is displayed in the list 
				Log.fail("Test case Failed. One item is not getting displayed on opening the hyperlink.", driver);
				return;
			}

			if (homePage.listView.isItemExists(memberObject)) //Verifies if member object exists in the hyperlink
				Log.pass("Test case Passed. Hyperlink of an collection member object (" + memberObject + ") is opened successfully.");
			else
				Log.fail("Test case Failed. collection member object (" + memberObject + ") does not exists in the hyperlink.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_11C

	/**
	 * 25.2.11D : Open the Hyperlink of Collection members in Show members view - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "Show members"}, 
			description = "Open the Hyperlink of Collection members in Show members view - Operations menu.")
	public void SprintTest25_2_11D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Open the Show members View of the object
			//------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!homePage.listView.insertColumn("Object Type")) //Inserts Object Type column if not exists
				throw new SkipException("Object type column is not inserted.");

			if (!homePage.listView.getColumnValueByItemName(dataPool.get("ObjectName"), "Object Type").equalsIgnoreCase("DOCUMENT COLLECTION")) //Checks if Document collection object is selected
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") is not document collection object type.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Click on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("Show Members"); //Click the Show Members link in the task pane
			Utils.fluentWait(driver);

			if (!ListView.isMembersViewOpened(driver)) //Checks if collection member view is opened
				throw new Exception("Collection members view of an Object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. Collection members View of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open Get M-Files Web URL dialog for the collection member from context menu
			//------------------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0) //Checks if collection member exists in the list
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") does not has any collection members.");

			String memberObject = homePage.listView.getColumnValueByItemIndex(0, "Name");

			if(!homePage.listView.clickItemByIndex(0)) //Clicks on related object
				throw new Exception("Member object is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			if (!MFilesDialog.exists(driver))
				throw new Exception("Get M-Files Web URL dialog is not opened.");

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of member object (" + memberObject + ") is opened.");

			//Step-4 : Copy Hyperlink from text box
			//--------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied from the text box.");

			//Step-5 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver.get(hyperlinkText); //Navigates to the hyperlink url
			Utils.fluentWait(driver);

			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied[" + hyperlinkText + "] object Hyperlink. Actual URL loaded : " + driver.getCurrentUrl());

			Log.message("5. Object Hyperlink is opened in the browser.");

			//Verification : To Verify if object exists in the URL
			//----------------------------------------------------
			homePage = new HomePage(driver);

			if (homePage.listView.itemCount() != 1) { //Verifies if only one object is displayed in the list 
				Log.fail("Test case Failed. One item is not getting displayed on opening the hyperlink.", driver);
				return;
			}

			if (homePage.listView.isItemExists(memberObject)) //Verifies if member object exists in the hyperlink
				Log.pass("Test case Passed. Hyperlink of an collection member object (" + memberObject + ") is opened successfully.");
			else
				Log.fail("Test case Failed. collection member object (" + memberObject + ") does not exists in the hyperlink.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_11D


	/**
	 * 40.1.16.1A : Get M-Files Web URL should be available for expanded MFD objects - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint40", "Get M-Files Web URL"}, 
			description = "Get M-Files Web URL should be available for expanded MFD objects - Context menu")
	public void SprintTest40_1_16_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Expand the object
			//-------------------------
			if (ListView.isSFDByItemName(driver, dataPool.get("MFDObjectName")))
				throw new Exception("Object (" + dataPool.get("MFDObjectName") + ") is MFD document.");

			if (!homePage.listView.expandItemByName(dataPool.get("MFDObjectName")))
				throw new Exception("Object (" + dataPool.get("MFDObjectName") + ") is not expanded.");

			Log.message("2. Object (" + dataPool.get("MFDObjectName") + ") is expanded.");

			//Step-3 : Right click on the object and select Get M-Files Web URL from context menu
			//-----------------------------------------------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("SFDObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("SFDObjectName") + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of an object (" + dataPool.get("SFDObjectName") + ") is opened from context menu.");

			//Step-4 : Copy the link from text box and close the Get M-Files Web URL dialog
			//-----------------------------------------------------------------------
			mfilesDialog.setHyperLinkAction("SHOW OBJECT"); //Selects Show object from hyperlink dialog

			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied and Get M-Files Web URL dialog is closed. Hyperlink URL : " + hyperlinkText, driver);

			//Step-5 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver.get(hyperlinkText); //Navigates to the hyperlink url
			Utils.fluentWait(driver);

			/*if (!driver.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkText.replace(".aspx?", ".aspx").toUpperCase())) //Checks if hyperlink URL is same as in the dialog
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");
			 */
			Log.message("5. Object Hyperlink is opened in the browser.");

			//Verification : To Verify if object exists in the URL
			//----------------------------------------------------
			homePage = new HomePage(driver);

			if (homePage.listView.itemCount() != 1) {  //Verifies if only one object is displayed in the list
				Log.fail("Test case Failed. One item is not getting displayed on opening the hyperlink.", driver);
				return;
			}

			if (homePage.listView.isItemExists(dataPool.get("MFDObjectName"))) //Verifies if object exists in the hyperlink
				Log.pass("Test case Passed. Object (" + dataPool.get("MFDObjectName") + ") exists in the hyperlink.");
			else
				Log.fail("Test case Failed. Object (" + dataPool.get("MFDObjectName") + ") does not exists in the hyperlink.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest40_1_16_1A

	/**
	 * 40.1.16.1B : Get M-Files Web URL should be available for expanded MFD objects - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint40", "Get M-Files Web URL"}, 
			description = "Get M-Files Web URL should be available for expanded MFD objects - Operations menu")
	public void SprintTest40_1_16_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Expand the object
			//-------------------------
			if (ListView.isSFDByItemName(driver, dataPool.get("MFDObjectName")))
				throw new Exception("Object (" + dataPool.get("MFDObjectName") + ") MFD document.");

			if (!homePage.listView.expandItemByName(dataPool.get("MFDObjectName")))
				throw new Exception("Object (" + dataPool.get("MFDObjectName") + ") is not expanded.");

			Log.message("2. Object (" + dataPool.get("MFDObjectName") + ") is expanded.");

			//Step-3 : Right click on the object and select Get M-Files Web URL from operations menu
			//-----------------------------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("SFDObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("SFDObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of an object (" + dataPool.get("SFDObjectName") + ") is opened from operation menu.");

			//Step-4 : Copy the link from text box and close the Get M-Files Web URL dialog
			//-----------------------------------------------------------------------
			mfilesDialog.setHyperLinkAction("SHOW OBJECT"); //Selects Show object from hyperlink dialog
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied and Get M-Files Web URL dialog is closed. Hyperlink URL : " + hyperlinkText, driver);

			//Step-5 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver.get(hyperlinkText); //Navigates to the hyperlink url
			Utils.fluentWait(driver);

			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied[" + hyperlinkText + "] object Hyperlink. Actual URL loaded : " + driver.getCurrentUrl());

			Log.message("5. Object Hyperlink is opened in the browser.");

			//Verification : To Verify if object exists in the URL
			//----------------------------------------------------
			homePage = new HomePage(driver);

			if (homePage.listView.itemCount() != 1) {  //Verifies if only one object is displayed in the list
				Log.fail("Test case Failed. One item is not getting displayed on opening the hyperlink.", driver);
				return;
			}

			if (homePage.listView.isItemExists(dataPool.get("MFDObjectName"))) //Verifies if object exists in the hyperlink
				Log.pass("Test case Passed. Object (" + dataPool.get("MFDObjectName") + ") exists in the hyperlink.");
			else
				Log.fail("Test case Failed. Object (" + dataPool.get("MFDObjectName") + ") does not exists in the hyperlink.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest40_1_16_1A

	/**
	 * 40.1.16.2A : Get M-Files Web URL should be available for expanded Related objects - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint40", "Get M-Files Web URL"}, 
			description = "Get M-Files Web URL should be available for expanded Related objects - Context menu")
	public void SprintTest40_1_16_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Expand the object
			//-------------------------
			if (!homePage.listView.expandItemByName(dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not expanded.");

			int objIndex = homePage.listView.getItemIndexByItemName(dataPool.get("ObjectName"));

			if (!homePage.listView.expandItemByIndex(objIndex + 1))
				throw new Exception("Object type header is not expanded.");

			String relatedItem = homePage.listView.getItemNameByItemIndex(objIndex + 2);

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is expanded.");

			//Step-3 : Right click on the object and select Get M-Files Web URL from context menu
			//-----------------------------------------------------------------------------
			if (!homePage.listView.rightClickItem(relatedItem)) //Selects the Object in the list
				throw new Exception("Object (" + relatedItem + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of an object (" + relatedItem + ") is opened from context menu.");

			//Step-4 : Copy the link from text box and close the Get M-Files Web URL dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied and Get M-Files Web URL dialog is closed. Hyperlink URL : " + hyperlinkText, driver);

			//Step-5 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver.get(hyperlinkText); //Navigates to the hyperlink url
			Utils.fluentWait(driver);

			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied[" + hyperlinkText + "] object Hyperlink. Actual URL loaded : " + driver.getCurrentUrl());

			Log.message("5. Object Hyperlink is opened in the browser.");

			//Verification : To Verify if object exists in the URL
			//----------------------------------------------------
			homePage = new HomePage(driver);

			if (homePage.listView.itemCount() != 1) {  //Verifies if only one object is displayed in the list
				Log.fail("Test case Failed. One item is not getting displayed on opening the hyperlink.", driver);
				return;
			}

			if (homePage.listView.isItemExists(relatedItem)) //Verifies if object exists in the hyperlink
				Log.pass("Test case Passed. Related Object (" + relatedItem + ") exists in the hyperlink.");
			else
				Log.fail("Test case Failed. Related Object (" + relatedItem + ") does not exists in the hyperlink.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest40_1_16_2A

	/**
	 * 40.1.16.2B : Get M-Files Web URL should be available for expanded Related objects - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint40", "Get M-Files Web URL"}, 
			description = "Get M-Files Web URL should be available for expanded Related objects - Operations menu")
	public void SprintTest40_1_16_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Expand the object
			//-------------------------
			if (!homePage.listView.expandItemByName(dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not expanded.");

			int objIndex = homePage.listView.getItemIndexByItemName(dataPool.get("ObjectName"));

			if (!homePage.listView.expandItemByIndex(objIndex + 1))
				throw new Exception("Object type header is not expanded.");

			String relatedItem = homePage.listView.getItemNameByItemIndex(objIndex + 2);

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is expanded.");

			//Step-3 : Right click on the object and select Get M-Files Web URL from operations menu
			//-----------------------------------------------------------------------------
			if (!homePage.listView.clickItem(relatedItem)) //Selects the Object in the list
				throw new Exception("Object (" + relatedItem + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of an object (" + relatedItem + ") is opened from operations menu.");

			//Step-4 : Copy the link from text box and close the Get M-Files Web URL dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied and Get M-Files Web URL dialog is closed. Hyperlink URL : " + hyperlinkText, driver);

			//Step-5 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver.get(hyperlinkText); //Navigates to the hyperlink url
			Utils.fluentWait(driver);

			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied[" + hyperlinkText + "] object Hyperlink. Actual URL loaded : " + driver.getCurrentUrl());

			Log.message("5. Object Hyperlink is opened in the browser.");

			//Verification : To Verify if object exists in the URL
			//----------------------------------------------------
			homePage = new HomePage(driver);

			if (homePage.listView.itemCount() != 1) {  //Verifies if only one object is displayed in the list
				Log.fail("Test case Failed. One item is not getting displayed on opening the hyperlink.", driver);
				return;
			}

			if (homePage.listView.isItemExists(relatedItem)) //Verifies if object exists in the hyperlink
				Log.pass("Test case Passed. Related Object (" + relatedItem + ") exists in the hyperlink.");
			else
				Log.fail("Test case Failed. Related Object (" + relatedItem + ") does not exists in the hyperlink.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest40_1_16_2B


	/**
	 * 41.2.1.1A : URL Fragment hash value should be available on selecting 'Show the selected file' - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint41", "Get M-Files Web URL"}, 
			description = "URL Fragment hash value should be available on selecting 'Show the selected file' - Context menu")
	public void SprintTest41_2_1_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {


			driver = WebDriverUtils.getDriver();


			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from context menu
			//-------------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0) //Checks if item exists in the list
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") does not have items.");

			String itemName = homePage.listView.getItemNameByItemIndex(0);

			if(!homePage.listView.rightClickItemByIndex(0)) //Clicks on related object
				throw new Exception("First Object in the list is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + itemName + ") is opened from context menu.");

			//Step-3 : Select 'Show the Selected object' option in Get M-Files Web URL dialog
			//-------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT"))
				throw new Exception("Show Selected object is not selected in Get M-Files Web URL dialog.");

			Log.message("3. Show Selected Object is selected in Get M-Files Web URL dialog.");

			//Step-4 : Copy the link from text box and close the Get M-Files Web URL dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied and Get M-Files Web URL dialog is closed. Hyperlink URL : " + hyperlinkText, driver);

			//Step-5 : Log out from default page of MFWA
			//------------------------------------------
			if (!Utility.logOut(driver))
				throw new Exception("Log out is not successful.");

			Log.message("5. Logged out from MFiles web access default page.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver.get(hyperlinkText); //Navigates to the hyperlink url
			Utils.fluentWait(driver);

			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Page is not navigated to login page.");

			Log.message("6. Object Hyperlink is pasted in browser and log in page is displayed.");

			//Step-7 : Log in to the hyperlink
			//--------------------------------
			LoginPage loginPage = new LoginPage(driver);
			homePage = loginPage.loginToWebApplication(userName, password, "");

			Log.message("7. Login credentials provided and login button is clicked.");

			//Verification : To Verify if object exists in the URL
			//----------------------------------------------------
			/*if (!driver.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkText.replace(".aspx?", ".aspx").toUpperCase()))
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");	*/		

			if (homePage.listView.itemCount() != 1) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. One item is not getting displayed on opening the hyperlink.");

			if (!homePage.listView.isItemExists(itemName)) //Verifies if object exists in the hyperlink
				throw new Exception("Test case Failed. Object (" + itemName + ") does not exists in the hyperlink.");

			if (driver.getCurrentUrl().toUpperCase().contains("#")) //Verifies if object exists in the hyperlink
				Log.pass("Test case Passed. Hyperlink URL contains hash (#) value.");
			else
				Log.fail("Test case Failed. Hyperlink URL does not contain hash (#) value.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest41_2_1_1A

	/**
	 * 41.2.1.1B : URL Fragment hash value should be available on selecting 'Show the selected file' - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint41", "Get M-Files Web URL"}, 
			description = "URL Fragment hash value should be available on selecting 'Show the selected file' - Context menu")
	public void SprintTest41_2_1_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from operations menu
			//-------------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0) //Checks if item exists in the list
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") does not have items.");

			String itemName = homePage.listView.getItemNameByItemIndex(0);

			if(!homePage.listView.clickItemByIndex(0)) //Clicks on object
				throw new Exception("First Object in the list is not right clicked.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + itemName + ") is opened from operations menu.");

			//Step-3 : Select 'Show the Selected object' option in Get M-Files Web URL dialog
			//-------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT"))
				throw new Exception("Show Selected object is not selected in Get M-Files Web URL dialog.");

			Log.message("3. Show Selected Object is selected in Get M-Files Web URL dialog.");

			//Step-4 : Copy the link from text box and close the Get M-Files Web URL dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied and Get M-Files Web URL dialog is closed. Hyperlink URL : " + hyperlinkText, driver);

			//Step-5 : Log out from default page of MFWA
			//------------------------------------------
			if (!Utility.logOut(driver))
				throw new Exception("Log out is not successful.");

			Log.message("5. Logged out from MFiles web access default page.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver.get(hyperlinkText); //Navigates to the hyperlink url
			Utils.fluentWait(driver);

			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Page is not navigated to login page.");

			Log.message("6. Object Hyperlink is pasted in browser and log in page is displayed.");

			//Step-7 : Log in to the hyperlink
			//--------------------------------
			LoginPage loginPage = new LoginPage(driver);
			homePage = loginPage.loginToWebApplication(userName, password, "");

			Log.message("7. Login credentials provided and login button is clicked.");

			//Verification : To Verify if object exists in the URL
			//----------------------------------------------------
			/*	if (!driver.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkText.replace(".aspx?", ".aspx").toUpperCase()))
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");	*/		

			if (homePage.listView.itemCount() != 1) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. One item is not getting displayed on opening the hyperlink.");

			if (!homePage.listView.isItemExists(itemName)) //Verifies if object exists in the hyperlink
				throw new Exception("Test case Failed. Object (" + itemName + ") does not exists in the hyperlink.");

			if (driver.getCurrentUrl().toUpperCase().contains("#")) //Verifies if object exists in the hyperlink
				Log.pass("Test case Passed. Hyperlink URL contains hash (#) value.");
			else
				Log.fail("Test case Failed. Hyperlink URL does not contain hash (#) value.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest41_2_1_1B

	/**
	 * 41.2.1.1C : URL Fragment hash value should be available on selecting 'Show the current view' - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint41", "Get M-Files Web URL"}, 
			description = "URL Fragment hash value should be available on selecting 'Show the current view' - Context menu")
	public void SprintTest41_2_1_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from context menu
			//-------------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0) //Checks if item exists in the list
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") does not have items.");

			int itemCt = homePage.listView.itemCount();

			if(!homePage.listView.rightClickItemByIndex(0)) //Clicks on related object
				throw new Exception("First Object in the list is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object is opened from context menu.");

			//Step-3 : Select 'Show the current view' option in Get M-Files Web URL dialog
			//-------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("SHOW VIEW"))
				throw new Exception("Show the current view is not selected in Get M-Files Web URL dialog.");

			Log.message("3. Show current view is selected in Get M-Files Web URL dialog.");

			//Step-4 : Copy the link from text box and close the Get M-Files Web URL dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied and Get M-Files Web URL dialog is closed. Hyperlink URL : " + hyperlinkText, driver);

			//Step-5 : Log out from default page of MFWA
			//------------------------------------------
			if (!Utility.logOut(driver))
				throw new Exception("Log out is not successful.");

			Log.message("5. Logged out from MFiles web access default page.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver.get(hyperlinkText); //Navigates to the hyperlink url
			Utils.fluentWait(driver);

			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Page is not navigated to login page.");

			Log.message("6. Object Hyperlink is pasted in browser and log in page is displayed.");

			//Step-7 : Log in to the hyperlink
			//--------------------------------
			LoginPage loginPage = new LoginPage(driver);
			homePage = loginPage.loginToWebApplication(userName, password, "");

			Log.message("7. Login credentials provided and login button is clicked.");

			//Verification : To Verify if object exists in the URL
			//----------------------------------------------------
			/*if (!driver.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkText.replace(".aspx?", ".aspx").toUpperCase()))
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");	*/		

			if (homePage.listView.itemCount() != itemCt) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. One item is not getting displayed on opening the hyperlink.");

			if (driver.getCurrentUrl().toUpperCase().contains("#")) //Verifies if object exists in the hyperlink
				Log.pass("Test case Passed. Hyperlink URL contains hash (#) value.");
			else
				Log.fail("Test case Failed. Hyperlink URL does not contain hash (#) value.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest41_2_1_1C

	/**
	 * 41.2.1.1D : URL Fragment hash value should be available on selecting 'Show the current view' - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint41", "Get M-Files Web URL"}, 
			description = "URL Fragment hash value should be available on selecting 'Show the current view' - Operations menu")
	public void SprintTest41_2_1_1D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from operations menu
			//----------------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0) //Checks if item exists in the list
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") does not have items.");

			int itemCt = homePage.listView.itemCount(); 

			if(!homePage.listView.clickItemByIndex(0)) //Clicks on object
				throw new Exception("First Object in the list is not right clicked.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object is opened from operations menu.");

			//Step-3 : Select 'Show the current view' option in Get M-Files Web URL dialog
			//----------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("SHOW VIEW"))
				throw new Exception("Show the current view is not selected in Get M-Files Web URL dialog.");

			Log.message("3. Show current view is selected in Get M-Files Web URL dialog.");

			//Step-4 : Copy the link from text box and close the Get M-Files Web URL dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied and Get M-Files Web URL dialog is closed. Hyperlink URL : " + hyperlinkText, driver);

			//Step-5 : Log out from default page of MFWA
			//------------------------------------------
			if (!Utility.logOut(driver))
				throw new Exception("Log out is not successful.");

			Log.message("5. Logged out from MFiles web access default page.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver.get(hyperlinkText); //Navigates to the hyperlink url
			Utils.fluentWait(driver);

			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Page is not navigated to login page.");

			Log.message("6. Object Hyperlink is pasted in browser and log in page is displayed.");

			//Step-7 : Log in to the hyperlink
			//--------------------------------
			LoginPage loginPage = new LoginPage(driver);
			homePage = loginPage.loginToWebApplication(userName, password, "");

			Log.message("7. Login credentials provided and login button is clicked.");

			//Verification : To Verify if object exists in the URL
			//----------------------------------------------------
			/*if (!driver.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkText.replace(".aspx?", ".aspx").toUpperCase()))
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");			
			 */
			if (homePage.listView.itemCount() != itemCt) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. One item is not getting displayed on opening the hyperlink.");

			if (driver.getCurrentUrl().toUpperCase().contains("#")) //Verifies if object exists in the hyperlink
				Log.pass("Test case Passed. Hyperlink URL contains hash (#) value.");
			else
				Log.fail("Test case Failed. Hyperlink URL does not contain hash (#) value.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest41_2_1_1D


	/**
	 * 41.2.11.1A : URL Fragment hash value should be available on selecting 'Show the selected file'in new window - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIPIE11_MultiDriver", "Sprint41", "Get M-Files Web URL"}, 
			description = "URL Fragment hash value should be available on selecting 'Show the selected file'in new window - Context menu")
	public void SprintTest41_2_11_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("EDGE"))
			throw new SkipException("Second instance of browser will not be opened with active first session for '" + driverType + "' driver type.");

		driver = null; 
		WebDriver driver2 = null;

		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from context menu
			//-------------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0) //Checks if item exists in the list
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") does not have items.");

			String itemName = homePage.listView.getItemNameByItemIndex(0);

			if(!homePage.listView.rightClickItemByIndex(0)) //Clicks on related object
				throw new Exception("First Object in the list is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + itemName + ") is opened from context menu.");

			//Step-3 : Select 'Show the Selected object' option in Get M-Files Web URL dialog
			//-------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT"))
				throw new Exception("Show Selected object is not selected in Get M-Files Web URL dialog.");

			Log.message("3. Show Selected Object is selected in Get M-Files Web URL dialog.");

			//Step-4 : Copy the link from text box and close the Get M-Files Web URL dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied and Get M-Files Web URL dialog is closed. Hyperlink URL : " + hyperlinkText, driver);

			//Step-5 : Open new window and open the hyperlink URL in the page
			//---------------------------------------------------------------
			driver2 = WebDriverUtils.getDriver();
			Utils.fluentWait(driver2);

			Log.message("5. New browser window is opened.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver2.get(hyperlinkText);
			Utils.fluentWait(driver2);

			if (!driver2.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Page is not navigated to login page.");

			Log.message("6. Object Hyperlink is pasted in new window and log in page is displayed.");

			//Step-7 : Log in to the hyperlink
			//--------------------------------
			LoginPage loginPage = new LoginPage(driver2);
			homePage = loginPage.loginToWebApplication(userName, password, "");

			Log.message("7. Login credentials provided and login button is clicked.");

			//Verification : To Verify if object exists in the URL
			//----------------------------------------------------
			/*	if (!driver2.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkText.replace(".aspx?", ".aspx").toUpperCase()))
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");			
			 */
			if (homePage.listView.itemCount() != 1) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. One item is not getting displayed on opening the hyperlink.");

			if (!homePage.listView.isItemExists(itemName)) //Verifies if object exists in the hyperlink
				throw new Exception("Test case Failed. Object (" + itemName + ") does not exists in the hyperlink.");

			if (driver2.getCurrentUrl().toUpperCase().contains("#")) //Verifies if object exists in the hyperlink
				Log.pass("Test case Passed. Hyperlink URL contains hash (#) value.");
			else
				Log.fail("Test case Failed. Hyperlink URL does not contain hash (#) value.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
			if (driver2 != null) {Utility.quitDriver(driver2);}
		} //End finally

	} //End SprintTest41_2_11_1A

	/**
	 * 41.2.11.1B : URL Fragment hash value should be available on selecting 'Show the selected file'in new window - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIPIE11_MultiDriver", "Sprint41", "Get M-Files Web URL"}, 
			description = "URL Fragment hash value should be available on selecting 'Show the selected file'in new window - Operations menu")
	public void SprintTest41_2_11_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("EDGE"))
			throw new SkipException("Second instance of browser will not be opened with active first session for '" + driverType + "' driver type.");

		driver = null; 
		WebDriver driver2 = null;

		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from operations menu
			//----------------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0) //Checks if item exists in the list
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") does not have items.");

			String itemName = homePage.listView.getItemNameByItemIndex(0);

			if(!homePage.listView.clickItemByIndex(0)) //Clicks on object
				throw new Exception("First Object in the list is not right clicked.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + itemName + ") is opened from operations menu.");

			//Step-3 : Select 'Show the Selected object' option in Get M-Files Web URL dialog
			//-------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT"))
				throw new Exception("Show Selected object is not selected in Get M-Files Web URL dialog.");

			Log.message("3. Show Selected Object is selected in Get M-Files Web URL dialog.");

			//Step-4 : Copy the link from text box and close the Get M-Files Web URL dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied and Get M-Files Web URL dialog is closed. Hyperlink URL : " + hyperlinkText, driver);

			//Step-5 : Open new window and open the hyperlink URL in the page
			//---------------------------------------------------------------
			driver2 = WebDriverUtils.getDriver();
			Utils.fluentWait(driver2);

			Log.message("5. New browser window is opened.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver2.get(hyperlinkText);
			Utils.fluentWait(driver2);

			if (!driver2.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Page is not navigated to login page.");

			Log.message("6. Object Hyperlink is pasted in new window and log in page is displayed.");

			//Step-7 : Log in to the hyperlink
			//--------------------------------
			LoginPage loginPage = new LoginPage(driver2);
			homePage = loginPage.loginToWebApplication(userName, password, "");

			Log.message("7. Login credentials provided and login button is clicked.");

			//Verification : To Verify if object exists in the URL
			//----------------------------------------------------
			/*	if (!driver2.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkText.replace(".aspx?", ".aspx").toUpperCase()))
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");	*/		

			if (homePage.listView.itemCount() != 1) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. One item is not getting displayed on opening the hyperlink.");

			if (!homePage.listView.isItemExists(itemName)) //Verifies if object exists in the hyperlink
				throw new Exception("Test case Failed. Object (" + itemName + ") does not exists in the hyperlink.");

			if (driver2.getCurrentUrl().toUpperCase().contains("#")) //Verifies if object exists in the hyperlink
				Log.pass("Test case Passed. Hyperlink URL contains hash (#) value.");
			else
				Log.fail("Test case Failed. Hyperlink URL does not contain hash (#) value.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
			if (driver2 != null) {Utility.quitDriver(driver2);}
		} //End finally

	} //End SprintTest41_2_11_1B

	/**
	 * 41.2.11.1C : URL Fragment hash value should be available on selecting 'Show the selected view'in new window - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIPIE11_MultiDriver", "Sprint41", "Get M-Files Web URL"}, 
			description = "URL Fragment hash value should be available on selecting 'Show the selected view'in new window - Context menu")
	public void SprintTest41_2_11_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("EDGE"))
			throw new SkipException("Second instance of browser will not be opened with active first session for '" + driverType + "' driver type.");

		driver = null; 
		WebDriver driver2 = null;

		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from context menu
			//-------------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0) //Checks if item exists in the list
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") does not have items.");

			int itemCt = homePage.listView.itemCount();

			if(!homePage.listView.rightClickItemByIndex(0)) //Clicks on related object
				throw new Exception("First Object in the list is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object is opened from context menu.");

			//Step-3 : Select 'Show the current view' option in Get M-Files Web URL dialog
			//-------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("SHOW VIEW"))
				throw new Exception("Show the current view is not selected in Get M-Files Web URL dialog.");

			Log.message("3. Show current view is selected in Get M-Files Web URL dialog.");

			//Step-4 : Copy the link from text box and close the Get M-Files Web URL dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied and Get M-Files Web URL dialog is closed. Hyperlink URL : " + hyperlinkText, driver);

			//Step-5 : Open new window and open the hyperlink URL in the page
			//---------------------------------------------------------------
			driver2 = WebDriverUtils.getDriver();
			Utils.fluentWait(driver2);

			Log.message("5. New browser window is opened.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver2.get(hyperlinkText);
			Utils.fluentWait(driver2);

			if (!driver2.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Page is not navigated to login page.");

			Log.message("6. Object Hyperlink is pasted in new window and log in page is displayed.");

			//Step-7 : Log in to the hyperlink
			//--------------------------------
			LoginPage loginPage = new LoginPage(driver2);
			homePage = loginPage.loginToWebApplication(userName, password, "");

			Log.message("7. Login credentials provided and login button is clicked.");

			//Verification : To Verify if object exists in the URL
			//----------------------------------------------------
			/*	if (!driver2.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkText.replace(".aspx?", ".aspx").toUpperCase()))
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");	*/		

			if (homePage.listView.itemCount() != itemCt) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. One item is not getting displayed on opening the hyperlink.");

			if (driver2.getCurrentUrl().toUpperCase().contains("#")) //Verifies if object exists in the hyperlink
				Log.pass("Test case Passed. Hyperlink URL contains hash (#) value.");
			else
				Log.fail("Test case Failed. Hyperlink URL does not contain hash (#) value.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
			if (driver2 != null) {Utility.quitDriver(driver2);}
		} //End finally

	} //End SprintTest41_2_11_1C

	/**
	 * 41.2.11.1D : URL Fragment hash value should be available on selecting 'Show the selected view'in new window - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIPIE11_MultiDriver", "Sprint41", "Get M-Files Web URL"}, 
			description = "URL Fragment hash value should be available on selecting 'Show the selected view'in new window - Operations menu")
	public void SprintTest41_2_11_1D(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("EDGE"))
			throw new SkipException("Second instance of browser will not be opened with active first session for '" + driverType + "' driver type.");

		driver = null; 
		WebDriver driver2 = null;

		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from operations menu
			//----------------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0) //Checks if item exists in the list
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") does not have items.");

			int itemCt = homePage.listView.itemCount(); 

			if(!homePage.listView.clickItemByIndex(0)) //Clicks on object
				throw new Exception("First Object in the list is not right clicked.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object is opened from operations menu.");

			//Step-3 : Select 'Show the current view' option in Get M-Files Web URL dialog
			//-------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("SHOW VIEW"))
				throw new Exception("Show the current view is not selected in Get M-Files Web URL dialog.");

			Log.message("3. Show current view is selected in Get M-Files Web URL dialog.");

			//Step-4 : Copy the link from text box and close the Get M-Files Web URL dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied and Get M-Files Web URL dialog is closed. Hyperlink URL : " + hyperlinkText, driver);

			//Step-5 : Open new window and open the hyperlink URL in the page
			//---------------------------------------------------------------
			driver2 = WebDriverUtils.getDriver();
			Utils.fluentWait(driver2);

			Log.message("5. New browser window is opened.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver2.get(hyperlinkText);
			Utils.fluentWait(driver2);

			if (!driver2.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Page is not navigated to login page.");

			Log.message("6. Object Hyperlink is pasted in new window and log in page is displayed.");

			//Step-7 : Log in to the hyperlink
			//--------------------------------
			LoginPage loginPage = new LoginPage(driver2);
			homePage = loginPage.loginToWebApplication(userName, password, "");

			Log.message("7. Login credentials provided and login button is clicked.");

			//Verification : To Verify if object exists in the URL
			//----------------------------------------------------
			/*	if (!driver2.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkText.replace(".aspx?", ".aspx").toUpperCase()))
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");			*/

			if (homePage.listView.itemCount() != itemCt) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. One item is not getting displayed on opening the hyperlink.");

			if (driver2.getCurrentUrl().toUpperCase().contains("#")) //Verifies if object exists in the hyperlink
				Log.pass("Test case Passed. Hyperlink URL contains hash (#) value.");
			else
				Log.fail("Test case Failed. Hyperlink URL does not contain hash (#) value.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
			if (driver2 != null) {Utility.quitDriver(driver2);}
		} //End finally

	} //End SprintTest41_2_11_1D




}

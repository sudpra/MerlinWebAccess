package MFClient.Tests.GetMFilesWebURL;


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
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class ViewWebURL {

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

			userName = xmlParameters.getParameter("UserName");
			password = xmlParameters.getParameter("Password");
			testVault = xmlParameters.getParameter("VaultName");		
			userFullName = xmlParameters.getParameter("UserFullName");
			className = this.getClass().getSimpleName().toString().trim();
			if (xmlParameters.getParameter("driverType").equalsIgnoreCase("IE"))
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim() + xmlParameters.getParameter("driverVersion").trim();
			else
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim();

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
	 * 25.2.13.1C : 'Show the current view' should open the link of the current view - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "Views", "Smoke"}, 
			description = "'Show the current view' should open the link of the current view - Context menu")
	public void SprintTest25_2_13_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-3: Select 'Show the current View' in the dialog
			//----------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("Show View")) //Selects 'Show the Current View' option from hyperlink action
				throw new Exception("'Show the Current View' option is not selected in Get hyperlink dialog.");

			Log.message("3. 'Show the current View' option in hyperlink dialog is selected.");

			//Step-4 : Copy the hyperlink from the dialog and open the link.
			//--------------------------------------------------------------
			String hyperlinkURL = mfilesDialog.getHyperlink();
			//	if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkURL.replace(".aspx?", ".aspx"))) //Checks if link in browser and hyperlink dialog are same.
			//		throw new Exception("Link displayed in the browser and Link in the hyperlink dialog are not same.");

			// if (!mfilesDialog.getHyperlink().toUpperCase().contains("VIEWS")) //Verifies if member object exists in the hyperlink
			//	throw new Exception("'Views' text is not available in hyperlink on selecting 'Show the current view' option.");

			mfilesDialog.close(); //Closes MFiles dialog

			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog

			Log.message("4. Hyperlink is copied from the dialog and navigated to the copied URL.");

			//Verification : To Verify if link of view is opened
			//--------------------------------------------------
			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkURL)) {//Verifies if URL is same as the hyperlink url
				Log.fail("Test case Failed. Link displayed in Hyperlink dialog has opened the current view of an object on selecting 'Show the Current View' option.", driver);
				return;
			}

			if (homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Verifies if item exists in the view displayed
				Log.pass("Test case Passed. Link displayed in Hyperlink dialog has opened the current view with an object on selecting 'Show the Current View' option.");
			else
				Log.fail("Test case Failed. Object is not displayed in the view opened through Hyperlink..", driver);

		} //End try
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_13_1C

	/**
	 * 25.2.13.1D : 'Show the current view' should open the link of the current view - Operations menu 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "Views"}, 
			description = "'Show the current view' should open the link of the current view - Operations menu")
	public void SprintTest25_2_13_1D(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-3: Select 'Show the current View' in the dialog
			//----------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("Show View")) //Selects 'Show the Current View' option from hyperlink action
				throw new Exception("'Show the Current View' option is not selected in Get hyperlink dialog.");

			Log.message("3. 'Show the current View' option in hyperlink dialog is selected.");

			//Step-4 : Copy the hyperlink from the dialog and open the link.
			//--------------------------------------------------------------
			String hyperlinkURL = mfilesDialog.getHyperlink();

			/*if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkURL.replace(".aspx?", ".aspx"))) //Checks if link in browser and hyperlink dialog are same.
				throw new Exception("Link displayed in the browser and Link in the hyperlink dialog are not same.");

			if (!mfilesDialog.getHyperlink().toUpperCase().contains("VIEWS")) //Verifies if member object exists in the hyperlink
				throw new Exception("'Views' text is not available in hyperlink on selecting 'Show the current view' option.");*/

			mfilesDialog.close(); //Closes MFiles dialog
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog

			Log.message("4. Hyperlink is copied from the dialog and navigated to the copied URL.");

			//Verification : To Verify if link of view is opened
			//--------------------------------------------------
			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkURL)) {//Verifies if URL is same as the hyperlink url
				Log.fail("Test case Failed. Link displayed in Hyperlink dialog has opened the current view of an object on selecting 'Show the Current View' option.", driver);
				return;
			}

			if (homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Verifies if item exists in the view displayed
				Log.pass("Test case Passed. Link displayed in Hyperlink dialog has opened the current view with an object on selecting 'Show the Current View' option.");
			else
				Log.fail("Test case Failed. Object is not displayed in the view opened through Hyperlink.", driver);

		} //End try
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_13_1D

	/**
	 * 25.2.13.2A : 'Show the current view' for recent version of objects in History view- Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "History"}, 
			description = "'Show the current view' for recent version of objects in History view- Context menu")
	public void SprintTest25_2_13_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Click on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Click the History link in the task pane


			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History View of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open context menu of the latest version of an object
			//-------------------------------------------------------------
			String prevLatestVersion = homePage.listView.getColumnValueByItemIndex(0, "Version");

			if (!homePage.listView.rightClickItemByIndex(0)) //Right clicks on latest version of an object
				throw new Exception("Latest version of an object is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of latest version is opened from context menu.");

			//Step-4: Select 'Show the current View' in the dialog
			//----------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("Show View")) //Selects 'Show the Current View' option from hyperlink action
				throw new Exception("'Show the Current View' option is not selected in Get hyperlink dialog.");

			Log.message("4. 'Show the current View' option in hyperlink dialog is selected.");

			//Step-5 : Copy the hyperlink from the dialog and open the link.
			//--------------------------------------------------------------
			String hyperlinkURL = mfilesDialog.getHyperlink();

			/*if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkURL.replace(".aspx?", ".aspx"))) //Checks if link in browser and hyperlink dialog are same.
				throw new Exception("Link displayed in the browser and Link in the hyperlink dialog are not same.");
			 */
			mfilesDialog.close(); //Closes MFiles dialog
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog

			Log.message("5. Hyperlink is copied from the dialog and navigated to the copied URL.");

			//Verification : To Verify if link of view is opened
			//--------------------------------------------------
			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkURL)) {//Verifies if URL is same as the hyperlink url
				Log.fail("Test case Failed. Hyperlink URL copied and URL after navigation is not same.", driver);
				return;
			}



			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) {//Verifies if item exists in the view displayed
				Log.fail("Test case Failed. Object is not displayed in the view opened through Hyperlink.", driver);
				return;
			}

			if (homePage.listView.getColumnValueByItemIndex(0, "Version").equals(prevLatestVersion)) //Verifies if latest version of object is displayed
				Log.pass("Test case Passed. Link displayed in Hyperlink dialog has opened the current view with an object on selecting 'Show the Current View' option.");
			else
				Log.fail("Test case Failed. Wrong Object version is displayed in the view opened through Hyperlink.", driver);

		} //End try
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_13_2A

	/**
	 * 25.2.13.2B : 'Show the current view' for recent version of objects in History view- Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "History"}, 
			description = "'Show the current view' for recent version of objects in History view- Operations menu")
	public void SprintTest25_2_13_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Click on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Click the History link in the task pane


			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History View of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open context menu of the latest version of an object
			//-------------------------------------------------------------
			if (!homePage.listView.clickItemByIndex(0)) //Selects the latest version of an object
				throw new Exception("Latest version of an object is not selected.");

			String prevLatestVersion = homePage.listView.getColumnValueByItemIndex(0, "Version");
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of latest version is opened from operations menu.");

			//Step-4: Select 'Show the current View' in the dialog
			//----------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("Show View")) //Selects 'Show the Current View' option from hyperlink action
				throw new Exception("'Show the Current View' option is not selected in Get hyperlink dialog.");

			Log.message("4. 'Show the current View' option in hyperlink dialog is selected.");

			//Step-5 : Copy the hyperlink from the dialog and open the link.
			//--------------------------------------------------------------
			String hyperlinkURL = mfilesDialog.getHyperlink();

			/*if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkURL.replace(".aspx?", ".aspx"))) //Checks if link in browser and hyperlink dialog are same.
				throw new Exception("Link displayed in the browser and Link in the hyperlink dialog are not same.");
			 */
			mfilesDialog.close(); //Closes MFiles dialog
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog

			Log.message("5. Hyperlink is copied from the dialog and navigated to the copied URL.");

			//Verification : To Verify if link of view is opened
			//--------------------------------------------------
			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkURL)) {//Verifies if URL is same as the hyperlink url
				Log.fail("Test case Failed. Hyperlink URL copied and URL after navigation is not same.", driver);
				return;
			}



			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) {//Verifies if item exists in the view displayed
				Log.fail("Test case Failed. Object is not displayed in the view opened through Hyperlink..", driver);
				return;
			}

			if (homePage.listView.getColumnValueByItemIndex(0, "Version").equals(prevLatestVersion)) //Verifies if latest version of object is displayed
				Log.pass("Test case Passed. Link displayed in Hyperlink dialog has opened the current view with an object on selecting 'Show the Current View' option.");
			else
				Log.fail("Test case Failed. Wrong Object version is displayed in the view opened through Hyperlink.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_13_2B

	/**
	 * 25.2.13.2C : 'Show the current view' for older version of objects in History view- Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "History"}, 
			description = "'Show the current view' for older version of objects in History view- Context menu")
	public void SprintTest25_2_13_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Click on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Click the History link in the task pane


			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History View of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open context menu of the older version of an object
			//-------------------------------------------------------------
			if (homePage.listView.itemCount() <= 1) //Checks if object has older versions
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") does not have older versions.");

			String olderVersion = homePage.listView.getColumnValueByItemIndex(1, "Version"); //Gets the older version
			String olderVersionObjName = homePage.listView.getColumnValueByItemIndex(1, "Name"); //Gets the older version

			if (!homePage.listView.rightClickItemByIndex(1)) //Right clicks on older version of an object
				throw new Exception("Older version of an object is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of older version is opened from context menu.");

			//Step-4: Select 'Show the current View' in the dialog
			//----------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("Show View")) //Selects 'Show the Current View' option from hyperlink action
				throw new Exception("'Show the Current View' option is not selected in Get hyperlink dialog.");

			Log.message("4. 'Show the current View' option in hyperlink dialog is selected.");

			//Step-5 : Copy the hyperlink from the dialog and open the link.
			//--------------------------------------------------------------
			String hyperlinkURL = mfilesDialog.getHyperlink();

			/*if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkURL.replace(".aspx?", ".aspx"))) //Checks if link in browser and hyperlink dialog are same.
				throw new Exception("Link displayed in the browser and Link in the hyperlink dialog are not same.");
			 */
			mfilesDialog.close(); //Closes MFiles dialog
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog

			Log.message("5. Hyperlink is copied from the dialog and navigated to the copied URL.");

			//Verification : To Verify if link of view is opened
			//--------------------------------------------------
			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkURL)) {//Verifies if URL is same as the hyperlink url
				Log.fail("Test case Failed. Hyperlink URL copied and URL after navigation is not same.", driver);
				return;
			}



			if (!homePage.listView.isItemExists(olderVersionObjName)) {//Verifies if item exists in the view displayed
				Log.fail("Test case Failed. Object is not displayed in the view opened through Hyperlink..", driver);
				return;
			}

			if (homePage.listView.getColumnValueByItemIndex(1, "Version").equals(olderVersion)) //Verifies if older version of object is displayed
				Log.pass("Test case Passed. Link displayed in Hyperlink dialog has opened the current view with an object's older version on selecting 'Show the Current View' option.");
			else
				Log.fail("Test case Failed. Version is not displayed in the view opened through Hyperlink.", driver);

		} //End try
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_13_2C

	/**
	 * 25.2.13.2D : 'Show the current view' for older version of objects in History view- Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "History"}, 
			description = "'Show the current view' for older version of objects in History view- Operations menu")
	public void SprintTest25_2_13_2D(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Click on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Click the History link in the task pane


			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History View of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open context menu of the older version of an object
			//-------------------------------------------------------------
			if (homePage.listView.itemCount() <= 1) //Checks if object has older versions
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") does not have older versions.");

			String olderVersion = homePage.listView.getColumnValueByItemIndex(1, "Version"); //Gets the older version
			String olderVersionObjName = homePage.listView.getColumnValueByItemIndex(1, "Name"); //Gets the older version

			if (!homePage.listView.clickItemByIndex(1)) //Selects the older version of an object
				throw new Exception("Older version of an object is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of older version is opened from context menu.");

			//Step-4: Select 'Show the current View' in the dialog
			//----------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("Show View")) //Selects 'Show the Current View' option from hyperlink action
				throw new Exception("'Show the Current View' option is not selected in Get hyperlink dialog.");

			Log.message("4. 'Show the current View' option in hyperlink dialog is selected.");

			//Step-5 : Copy the hyperlink from the dialog and open the link.
			//--------------------------------------------------------------
			String hyperlinkURL = mfilesDialog.getHyperlink();

			/*	if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkURL.replace(".aspx?", ".aspx"))) //Checks if link in browser and hyperlink dialog are same.
				throw new Exception("Link displayed in the browser and Link in the hyperlink dialog are not same.");
			 */	
			mfilesDialog.close(); //Closes MFiles dialog
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog

			Log.message("5. Hyperlink is copied from the dialog and navigated to the copied URL.");

			//Verification : To Verify if link of view is opened
			//--------------------------------------------------
			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkURL)) {//Verifies if URL is same as the hyperlink url
				Log.fail("Test case Failed. Hyperlink URL copied and URL after navigation is not same.", driver);
				return;
			}



			if (!homePage.listView.isItemExists(olderVersionObjName)) {//Verifies if item exists in the view displayed
				Log.fail("Test case Failed. Object is not displayed in the view opened through Hyperlink..", driver);
				return;
			}

			if (homePage.listView.getColumnValueByItemIndex(1, "Version").equals(olderVersion)) //Verifies if older version of object is displayed
				Log.pass("Test case Passed. Link displayed in Hyperlink dialog has opened the current view with an object's older version on selecting 'Show the Current View' option.");
			else
				Log.fail("Test case Failed. Version is not displayed in the view opened through Hyperlink.", driver);

		} //End try
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_13_2D

	/**
	 * 25.2.13.3A : 'Show the current view' for related objects in Relationships view- Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "Relationships"}, 
			description = "'Show the current view' for related objects in Relationships view- Context menu")
	public void SprintTest25_2_13_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Open the Relationships View of the object
			//--------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Click on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Relationships.Value); //Click the Relationships link in the task pane


			if (!ListView.isRelationshipsViewOpened(driver))
				throw new Exception("Relationships view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. Relationships View of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open Get M-Files Web URL dialog of related object from context menu
			//----------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0)  //Checks if there are related objects
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not related objects.");

			String relatedObjName = homePage.listView.getColumnValueByItemIndex(0, "Name");

			if (!homePage.listView.rightClickItemByIndex(0)) //Right clicks on latest version of an object
				throw new Exception("Latest version of an object is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of related object (" + relatedObjName + ") is opened from context menu.");

			//Step-4: Select 'Show the current View' in the dialog
			//----------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("Show View")) //Selects 'Show the Current View' option from hyperlink action
				throw new Exception("'Show the Current View' option is not selected in Get hyperlink dialog.");

			Log.message("4. 'Show the current View' option in hyperlink dialog is selected.");

			//Step-5 : Copy the hyperlink from the dialog and open the link.
			//--------------------------------------------------------------
			String hyperlinkURL = mfilesDialog.getHyperlink();
			/*		
			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkURL.replace(".aspx?", ".aspx"))) //Checks if link in browser and hyperlink dialog are same.
				throw new Exception("Link displayed in the browser and Link in the hyperlink dialog are not same.");
			 */		
			mfilesDialog.close(); //Closes MFiles dialog
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog

			Log.message("5. Hyperlink is copied from the dialog and navigated to the copied URL.");

			//Verification : To Verify if link of view is opened
			//--------------------------------------------------
			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkURL)) {//Verifies if URL is same as the hyperlink url
				Log.fail("Test case Failed. Hyperlink URL copied and URL after navigation is not same.", driver);
				return;
			}



			if (homePage.listView.isItemExists(relatedObjName)) //Verifies if latest version of object is displayed
				Log.pass("Test case Passed. Link displayed in Hyperlink dialog has opened the current view with an object on selecting 'Show the Current View' option.");
			else
				Log.fail("Test case Failed. Object is not displayed in the view opened through Hyperlink.", driver);

		} //End try
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_13_3A

	/**
	 * 25.2.13.3B : 'Show the current view' for related objects in Relationships view- Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "Relationships"}, 
			description = "'Show the current view' for related objects in Relationships view- Operations menu")
	public void SprintTest25_2_13_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Open the Relationships View of the object
			//--------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Click on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Relationships.Value);//Click the Relationships link in the task pane


			if (!ListView.isRelationshipsViewOpened(driver))
				throw new Exception("Relationships view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. Relationships View of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open Get M-Files Web URL dialog of related object from context menu
			//----------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0)  //Checks if there are related objects
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not related objects.");

			String relatedObjName = homePage.listView.getColumnValueByItemIndex(0, "Name");

			if (!homePage.listView.clickItemByIndex(0)) //Right clicks on latest version of an object
				throw new Exception("Latest version of an object is not right clicked.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of related object (" + relatedObjName + ") is opened from context menu.");

			//Step-4: Select 'Show the current View' in the dialog
			//----------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("Show View")) //Selects 'Show the Current View' option from hyperlink action
				throw new Exception("'Show the Current View' option is not selected in Get hyperlink dialog.");

			Log.message("4. 'Show the current View' option in hyperlink dialog is selected.");

			//Step-5 : Copy the hyperlink from the dialog and open the link.
			//--------------------------------------------------------------
			String hyperlinkURL = mfilesDialog.getHyperlink();

			/*		if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkURL.replace(".aspx?", ".aspx"))) //Checks if link in browser and hyperlink dialog are same.
				throw new Exception("Link displayed in the browser and Link in the hyperlink dialog are not same.");
			 */		
			mfilesDialog.close(); //Closes MFiles dialog
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog

			Log.message("5. Hyperlink is copied from the dialog and navigated to the copied URL.");

			//Verification : To Verify if link of view is opened
			//--------------------------------------------------
			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkURL)) {//Verifies if URL is same as the hyperlink url
				Log.fail("Test case Failed. Hyperlink URL copied and URL after navigation is not same.", driver);
				return;
			}



			if (homePage.listView.isItemExists(relatedObjName)) //Verifies if latest version of object is displayed
				Log.pass("Test case Passed. Link displayed in Hyperlink dialog has opened the current view with an object on selecting 'Show the Current View' option.");
			else
				Log.fail("Test case Failed. Object is not displayed in the view opened through Hyperlink.", driver);

		} //End try
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_13_3B

	/**
	 * 25.2.13.4A : 'Show the current view' for subobject objects in SubObjects view- Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "Relationships"}, 
			description = "'Show the current view' for subobject objects in SubObjects view- Context menu")
	public void SprintTest25_2_13_4A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Open the SubObjects View of the object
			//--------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Click on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.ShowSubObjects.Value); //Click the Show subobjects link in the task pane


			if (!ListView.isSubObjectsViewOpened(driver))
				throw new Exception("SubObjects view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. SubObjects View of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open Get M-Files Web URL dialog of sub object from context menu
			//----------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0)  //Checks if there are related objects
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") has no sub objects.");

			String subObjName = homePage.listView.getColumnValueByItemIndex(0, "Name");

			if (!homePage.listView.rightClickItemByIndex(0)) //Right clicks on latest version of an object
				throw new Exception("Latest version of an object is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of sub object (" + subObjName + ") is opened from context menu.");

			//Step-4: Select 'Show the current View' in the dialog
			//----------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("Show View")) //Selects 'Show the Current View' option from hyperlink action
				throw new Exception("'Show the Current View' option is not selected in Get hyperlink dialog.");

			Log.message("4. 'Show the current View' option in hyperlink dialog is selected.");

			//Step-5 : Copy the hyperlink from the dialog and open the link.
			//--------------------------------------------------------------
			String hyperlinkURL = mfilesDialog.getHyperlink();

			/*	if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkURL.replace(".aspx?", ".aspx"))) //Checks if link in browser and hyperlink dialog are same.
				throw new Exception("Link displayed in the browser and Link in the hyperlink dialog are not same.");
			 */	
			mfilesDialog.close(); //Closes MFiles dialog
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog

			Log.message("5. Hyperlink is copied from the dialog and navigated to the copied URL.");

			//Verification : To Verify if link of view is opened
			//--------------------------------------------------
			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkURL)) {//Verifies if URL is same as the hyperlink url
				Log.fail("Test case Failed. Hyperlink URL copied and URL after navigation is not same.", driver);
				return;
			}



			if (homePage.listView.isItemExists(subObjName)) //Verifies if latest version of object is displayed
				Log.pass("Test case Passed. Link displayed in Hyperlink dialog has opened the current view with an object on selecting 'Show the Current View' option.");
			else
				Log.fail("Test case Failed. Object is not displayed in the view opened through Hyperlink.", driver);

		} //End try
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_13_4A

	/**
	 * 25.2.13.4B : 'Show the current view' for subobject objects in SubObjects view- Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "Relationships"}, 
			description = "'Show the current view' for subobject objects in SubObjects view- Operations menu")
	public void SprintTest25_2_13_4B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Open the SubObjects View of the object
			//--------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Click on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.ShowSubObjects.Value); //Click the Show subobjects link in the task pane


			if (!ListView.isSubObjectsViewOpened(driver))
				throw new Exception("SubObjects view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. SubObjects View of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open Get M-Files Web URL dialog of sub object from operations menu
			//----------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0)  //Checks if there are related objects
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") has no sub objects.");

			String subObjName = homePage.listView.getColumnValueByItemIndex(0, "Name");

			if (!homePage.listView.clickItemByIndex(0)) //Select the sub-object
				throw new Exception("Latest version of an object is not right clicked.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of sub object (" + subObjName + ") is opened from operations menu.");

			//Step-4: Select 'Show the current View' in the dialog
			//----------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("Show View")) //Selects 'Show the Current View' option from hyperlink action
				throw new Exception("'Show the Current View' option is not selected in Get hyperlink dialog.");

			Log.message("4. 'Show the current View' option in hyperlink dialog is selected.");

			//Step-5 : Copy the hyperlink from the dialog and open the link.
			//--------------------------------------------------------------
			String hyperlinkURL = mfilesDialog.getHyperlink();

			/*	if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkURL.replace(".aspx?", ".aspx"))) //Checks if link in browser and hyperlink dialog are same.
				throw new Exception("Link displayed in the browser and Link in the hyperlink dialog are not same.");
			 */	
			mfilesDialog.close(); //Closes MFiles dialog
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog

			Log.message("5. Hyperlink is copied from the dialog and navigated to the copied URL.");

			//Verification : To Verify if link of view is opened
			//--------------------------------------------------
			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkURL)) {//Verifies if URL is same as the hyperlink url
				Log.fail("Test case Failed. Hyperlink URL copied and URL after navigation is not same.", driver);
				return;
			}



			if (homePage.listView.isItemExists(subObjName)) //Verifies if latest version of object is displayed
				Log.pass("Test case Passed. Link displayed in Hyperlink dialog has opened the current view with an object on selecting 'Show the Current View' option.");
			else
				Log.fail("Test case Failed. Object is not displayed in the view opened through Hyperlink.", driver);

		} //End try
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_13_4B

	/**
	 * 25.2.13.5A : 'Show the current view' for member objects in Members view- Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "Relationships"}, 
			description = "'Show the current view' for member objects in Members view- Context menu")
	public void SprintTest25_2_13_5A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Open the SubObjects View of the object
			//--------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Click on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("Show Members"); //Click the Show subobjects link in the task pane


			if (!ListView.isMembersViewOpened(driver))
				throw new Exception("Members view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. Members View of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open Get M-Files Web URL dialog of sub object from context menu
			//----------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0)  //Checks if there are related objects
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") has no members.");

			String memberObj = homePage.listView.getColumnValueByItemIndex(0, "Name");

			if (!homePage.listView.rightClickItemByIndex(0)) //Right clicks on latest version of an object
				throw new Exception("Member object is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of member object (" + memberObj + ") is opened from context menu.");

			//Step-4: Select 'Show the current View' in the dialog
			//----------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("Show View")) //Selects 'Show the Current View' option from hyperlink action
				throw new Exception("'Show the Current View' option is not selected in Get hyperlink dialog.");

			Log.message("4. 'Show the current View' option in hyperlink dialog is selected.");

			//Step-5 : Copy the hyperlink from the dialog and open the link.
			//--------------------------------------------------------------
			String hyperlinkURL = mfilesDialog.getHyperlink();

			//			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkURL.replace(".aspx?", ".aspx"))) //Checks if link in browser and hyperlink dialog are same.
			//				throw new Exception("Link displayed in the browser and Link in the hyperlink dialog are not same.");
			//			
			mfilesDialog.close(); //Closes MFiles dialog
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog

			Log.message("5. Hyperlink is copied from the dialog and navigated to the copied URL.");

			//Verification : To Verify if link of view is opened
			//--------------------------------------------------
			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkURL)) {//Verifies if URL is same as the hyperlink url
				Log.fail("Test case Failed. Hyperlink URL copied and URL after navigation is not same.", driver);
				return;
			}

			if (homePage.listView.isItemExists(memberObj)) //Verifies if latest version of object is displayed
				Log.pass("Test case Passed. Link displayed in Hyperlink dialog has opened the current view with an object on selecting 'Show the Current View' option.");
			else
				Log.fail("Test case Failed. Object is not displayed in the view opened through Hyperlink.", driver);

		} //End try
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_13_5A

	/**
	 * 25.2.13.5B : 'Show the current view' for member objects in Members view- Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "Relationships"}, 
			description = "'Show the current view' for member objects in Members view- Operations menu")
	public void SprintTest25_2_13_5B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Open the SubObjects View of the object
			//--------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Click on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("Show Members"); //Click the Show subobjects link in the task pane


			if (!ListView.isMembersViewOpened(driver))
				throw new Exception("Members view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. Members View of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open Get M-Files Web URL dialog of sub object from context menu
			//----------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0)  //Checks if there are related objects
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") has no members.");

			String memberObj = homePage.listView.getColumnValueByItemIndex(0, "Name");

			if (!homePage.listView.clickItemByIndex(0)) //Selects latest version of an object
				throw new Exception("Member object is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of member object (" + memberObj + ") is opened from operations menu.");

			//Step-4: Select 'Show the current View' in the dialog
			//----------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("Show View")) //Selects 'Show the Current View' option from hyperlink action
				throw new Exception("'Show the Current View' option is not selected in Get hyperlink dialog.");

			Log.message("4. 'Show the current View' option in hyperlink dialog is selected.");

			//Step-5 : Copy the hyperlink from the dialog and open the link.
			//--------------------------------------------------------------
			String hyperlinkURL = mfilesDialog.getHyperlink();

			//			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkURL.replace(".aspx?", ".aspx"))) //Checks if link in browser and hyperlink dialog are same.
			//				throw new Exception("Link displayed in the browser and Link in the hyperlink dialog are not same.");
			//			
			mfilesDialog.close(); //Closes MFiles dialog
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog

			Log.message("5. Hyperlink is copied from the dialog and navigated to the copied URL.");

			//Verification : To Verify if link of view is opened
			//--------------------------------------------------
			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkURL)) {//Verifies if URL is same as the hyperlink url
				Log.fail("Test case Failed. Hyperlink URL copied and URL after navigation is not same.", driver);
				return;
			}



			if (homePage.listView.isItemExists(memberObj)) //Verifies if latest version of object is displayed
				Log.pass("Test case Passed. Link displayed in Hyperlink dialog has opened the current view with an object on selecting 'Show the Current View' option.");
			else
				Log.fail("Test case Failed. Object is not displayed in the view opened through Hyperlink.", driver);

		} //End try
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_13_5B


	/**
	 * 41.1.13.1A : Navigation path should be available in default layout of hyperlink URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint41", "Get M-Files Web URL"}, 
			description = "Navigation path should be available in default layout of hyperlink URL - Context menu")
	public void SprintTest41_1_13_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 


		try {



			driver = WebDriverUtils.getDriver();


			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Default' layout
			//-------------------------------------------
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

			//Step-4 : Navigate to any view
			//-----------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			if (driver.getCurrentUrl().toUpperCase().contains("TEMPSEARCH"))
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") is search type view that does not have breadcrumb item.");

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5: Select the object and open Get M-Files Web URL dialog from context menu
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

			Log.message("5. Get M-Files Web URL dialog of an object (" + itemName + ") is opened from context menu.");

			//Step-6 : Select 'Show the current view' and copy the url
			//--------------------------------------------------------
			mfilesDialog.setHyperLinkAction("SHOW VIEW"); //Select show the current view in the dialog

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("6. 'Show the current view' is selected and hyperlink URL is copied.");

			//Step-7 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog

			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkURL)) //Verifies if URL is same as the hyperlink url
				throw new Exception("Link displayed in Hyperlink dialog and current page URL are not same.");



			if (!homePage.listView.isItemExists(itemName)) //Verifies if item exists in the view displayed
				throw new Exception("Item (" + itemName + ") does not exists in the hyperlink URL.");

			Log.message("7. Object Hyperlink is opened in the browser.");

			//Verification : Verify if breadcrumb available with navigated view
			//-----------------------------------------------------------------
			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Verifies if breadcrumb is displayed
				throw new Exception("Test case Failed. Breadcrumb is not displayed.");

			String viewName = dataPool.get("NavigateToView").split(">>")[dataPool.get("NavigateToView").split(">>").length - 1].trim();

			//Verifies if navigated view available in breadcrumb
			if (homePage.menuBar.GetBreadCrumbItem().toUpperCase().contains(viewName.toUpperCase())) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Breadcrumb is available with the navigated view in Default layout.");
			else
				Log.fail("Test case Failed. Breadcrumb is not available with the navigated view in Default layout.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			if (driver != null){
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

	} //End SprintTest41_1_13_1A

	/**
	 * 41.1.13.1B : Navigation path should be available in default layout of hyperlink URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint41", "Get M-Files Web URL"}, 
			description = "Navigation path should be available in default layout of hyperlink URL - Operations menu")
	public void SprintTest41_1_13_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 


		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Default' layout
			//-------------------------------------------
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

			//Step-4 : Navigate to any view
			//-----------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			if (driver.getCurrentUrl().toUpperCase().contains("TEMPSEARCH"))
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") is search type view that does not have breadcrumb item.");

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5: Select the object and open Get M-Files Web URL dialog from operations menu
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

			Log.message("5. Get M-Files Web URL dialog of an object (" + itemName + ") is opened from operations menu.");

			//Step-6 : Select 'Show the current view' and copy the url
			//--------------------------------------------------------
			mfilesDialog.setHyperLinkAction("SHOW VIEW"); //Select show the current view in the dialog

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("6. 'Show the current view' is selected and hyperlink URL is copied.");

			//Step-7 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog

			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkURL)) //Verifies if URL is same as the hyperlink url
				throw new Exception("Link displayed in Hyperlink dialog and current page URL are not same.");



			if (!homePage.listView.isItemExists(itemName)) //Verifies if item exists in the view displayed
				throw new Exception("Item (" + itemName + ") does not exists in the hyperlink URL.");

			Log.message("7. Object Hyperlink is opened in the browser.");

			//Verification : Verify if breadcrumb available with navigated view
			//-----------------------------------------------------------------
			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Verifies if breadcrumb is displayed
				throw new Exception("Test case Failed. Breadcrumb is not displayed.");

			String viewName = dataPool.get("NavigateToView").split(">>")[dataPool.get("NavigateToView").split(">>").length - 1].trim();

			//Verifies if navigated view available in breadcrumb
			if (homePage.menuBar.GetBreadCrumbItem().toUpperCase().contains(viewName.toUpperCase())) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Breadcrumb is available with the navigated view in Default layout.");
			else
				Log.fail("Test case Failed. Breadcrumb is not available with the navigated view in Default layout.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (driver != null){
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

	} //End SprintTest41_1_13_1B

	/**
	 * 41.1.13.2A : Navigation path should be available in default and navigation pane layout of hyperlink URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint41", "Get M-Files Web URL"}, 
			description = "Navigation path should be available in default and navigation pane layout of hyperlink URL - Context menu")
	public void SprintTest41_1_13_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 


		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Default and Navigation pane' layout
			//--------------------------------------------------------------
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

			//Step-4 : Navigate to any view
			//-----------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			if (driver.getCurrentUrl().toUpperCase().contains("TEMPSEARCH"))
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") is search type view that does not have breadcrumb item.");

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5: Select the object and open Get M-Files Web URL dialog from context menu
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

			Log.message("5. Get M-Files Web URL dialog of an object (" + itemName + ") is opened from context menu.");

			//Step-6 : Select 'Show the current view' and copy the url
			//--------------------------------------------------------
			mfilesDialog.setHyperLinkAction("SHOW VIEW"); //Select show the current view in the dialog

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("6. 'Show the current view' is selected and hyperlink URL is copied.");

			//Step-7 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog

			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkURL)) //Verifies if URL is same as the hyperlink url
				throw new Exception("Link displayed in Hyperlink dialog and current page URL are not same.");



			if (!homePage.listView.isItemExists(itemName)) //Verifies if item exists in the view displayed
				throw new Exception("Item (" + itemName + ") does not exists in the hyperlink URL.");

			Log.message("7. Object Hyperlink is opened in the browser.");

			//Verification : Verify if breadcrumb available with navigated view
			//-----------------------------------------------------------------
			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Verifies if breadcrumb is displayed
				throw new Exception("Test case Failed. Breadcrumb is not displayed.");

			String viewName = dataPool.get("NavigateToView").split(">>")[dataPool.get("NavigateToView").split(">>").length - 1].trim();

			//Verifies if navigated view available in breadcrumb
			if (homePage.menuBar.GetBreadCrumbItem().toUpperCase().contains(viewName.toUpperCase())) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Breadcrumb is available with the navigated view in Default and navigation pane layout.");
			else
				Log.fail("Test case Failed. Breadcrumb is not available with the navigated view in Default and navigation pane layout.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (driver != null){
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

	} //End SprintTest41_1_13_2A

	/**
	 * 41.1.13.2B : Navigation path should be available in default and navigation pane layout of hyperlink URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint41", "Get M-Files Web URL"}, 
			description = "Navigation path should be available in default and navigation pane layout of hyperlink URL - Operations menu")
	public void SprintTest41_1_13_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 


		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Default and Navigation pane' layout
			//--------------------------------------------------------------
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

			//Step-4 : Navigate to any view
			//-----------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			if (driver.getCurrentUrl().toUpperCase().contains("TEMPSEARCH"))
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") is search type view that does not have breadcrumb item.");

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5: Select the object and open Get M-Files Web URL dialog from operations menu
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

			Log.message("5. Get M-Files Web URL dialog of an object (" + itemName + ") is opened from operations menu.");

			//Step-6 : Select 'Show the current view' and copy the url
			//--------------------------------------------------------
			mfilesDialog.setHyperLinkAction("SHOW VIEW"); //Select show the current view in the dialog

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("6. 'Show the current view' is selected and hyperlink URL is copied.");

			//Step-7 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog

			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkURL)) //Verifies if URL is same as the hyperlink url
				throw new Exception("Link displayed in Hyperlink dialog and current page URL are not same.");



			if (!homePage.listView.isItemExists(itemName)) //Verifies if item exists in the view displayed
				throw new Exception("Item (" + itemName + ") does not exists in the hyperlink URL.");

			Log.message("7. Object Hyperlink is opened in the browser.");

			//Verification : Verify if breadcrumb available with navigated view
			//-----------------------------------------------------------------
			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Verifies if breadcrumb is displayed
				throw new Exception("Test case Failed. Breadcrumb is not displayed.");

			String viewName = dataPool.get("NavigateToView").split(">>")[dataPool.get("NavigateToView").split(">>").length - 1].trim();

			//Verifies if navigated view available in breadcrumb
			if (homePage.menuBar.GetBreadCrumbItem().toUpperCase().contains(viewName.toUpperCase())) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Breadcrumb is available with the navigated view in Default and navigation pane layout.");
			else
				Log.fail("Test case Failed. Breadcrumb is not available with the navigated view in Default and navigation pane layout.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			if (driver != null){

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

	} //End SprintTest41_1_13_2B

	/**
	 * 41.1.13.3A : Navigation path should be available in No Java applet layout of hyperlink URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint41", "Get M-Files Web URL"}, 
			description = "Navigation path should be available in No Java applet layout of hyperlink URL - Context menu")
	public void SprintTest41_1_13_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Applet' layout
			//--------------------------------------------------------------
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

			//Step-4 : Navigate to any view
			//-----------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			if (driver.getCurrentUrl().toUpperCase().contains("TEMPSEARCH"))
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") is search type view that does not have breadcrumb item.");

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5: Select the object and open Get M-Files Web URL dialog from context menu
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

			Log.message("5. Get M-Files Web URL dialog of an object (" + itemName + ") is opened from context menu.");

			//Step-6 : Select 'Show the current view' and copy the url
			//--------------------------------------------------------
			mfilesDialog.setHyperLinkAction("SHOW VIEW"); //Select show the current view in the dialog

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("6. 'Show the current view' is selected and hyperlink URL is copied.");

			//Step-7 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog

			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkURL)) //Verifies if URL is same as the hyperlink url
				throw new Exception("Link displayed in Hyperlink dialog and current page URL are not same.");



			if (!homePage.listView.isItemExists(itemName)) //Verifies if item exists in the view displayed
				throw new Exception("Item (" + itemName + ") does not exists in the hyperlink URL.");

			Log.message("7. Object Hyperlink is opened in the browser.");

			//Verification : Verify if breadcrumb available with navigated view
			//-----------------------------------------------------------------
			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Verifies if breadcrumb is displayed
				throw new Exception("Test case Failed. Breadcrumb is not displayed.");

			String viewName = dataPool.get("NavigateToView").split(">>")[dataPool.get("NavigateToView").split(">>").length - 1].trim();

			//Verifies if navigated view available in breadcrumb
			if (homePage.menuBar.GetBreadCrumbItem().toUpperCase().contains(viewName.toUpperCase())) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Breadcrumb is available with the navigated view in No Java Applet layout.");
			else
				Log.fail("Test case Failed. Breadcrumb is not available with the navigated view in No Java Applet layout.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (driver != null){
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

	} //End SprintTest41_1_13_3A

	/**
	 * 41.1.13.3B : Navigation path should be available in No Java applet layout of hyperlink URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint41", "Get M-Files Web URL"}, 
			description = "Navigation path should be available in No Java applet layout of hyperlink URL - Operations menu")
	public void SprintTest41_1_13_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 


		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Applet' layout
			//--------------------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);//Disabled the java applet in configuration page

			if(configurationPage.configurationPanel.isJavaAppletEnabled())//Verify if java applet is disabled or not
				throw new Exception("Java applet is enabled.");

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
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

			if (driver.getCurrentUrl().toUpperCase().contains("TEMPSEARCH"))
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") is search type view that does not have breadcrumb item.");

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5: Select the object and open Get M-Files Web URL dialog from operations menu
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

			Log.message("5. Get M-Files Web URL dialog of an object (" + itemName + ") is opened from operations menu.");

			//Step-6 : Select 'Show the current view' and copy the url
			//--------------------------------------------------------
			mfilesDialog.setHyperLinkAction("SHOW VIEW"); //Select show the current view in the dialog

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("6. 'Show the current view' is selected and hyperlink URL is copied.");

			//Step-7 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog

			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkURL)) //Verifies if URL is same as the hyperlink url
				throw new Exception("Link displayed in Hyperlink dialog and current page URL are not same.");



			if (!homePage.listView.isItemExists(itemName)) //Verifies if item exists in the view displayed
				throw new Exception("Item (" + itemName + ") does not exists in the hyperlink URL.");

			Log.message("7. Object Hyperlink is opened in the browser.");

			//Verification : Verify if breadcrumb available with navigated view
			//-----------------------------------------------------------------
			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Verifies if breadcrumb is displayed
				throw new Exception("Test case Failed. Breadcrumb is not displayed.");

			String viewName = dataPool.get("NavigateToView").split(">>")[dataPool.get("NavigateToView").split(">>").length - 1].trim();

			//Verifies if navigated view available in breadcrumb
			if (homePage.menuBar.GetBreadCrumbItem().toUpperCase().contains(viewName.toUpperCase())) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Breadcrumb is available with the navigated view in No Java Applet layout.");
			else
				Log.fail("Test case Failed. Breadcrumb is not available with the navigated view in No Java Applet layout.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			if (driver != null){
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

	} //End SprintTest41_1_13_3B

	/**
	 * 41.1.13.4A : Navigation path should be available in No Java Applet, No task area layout of hyperlink URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint41", "Get M-Files Web URL"}, 
			description = "Navigation path should be available in No Java Applet, No task area layout of hyperlink URL - Context menu")
	public void SprintTest41_1_13_4A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 


		try {




			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Appletn no task area' layout
			//--------------------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			/*configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_NoJavaAppletTaskArea.Value);*/


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

			//Step-4 : Navigate to any view
			//-----------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			if (driver.getCurrentUrl().toUpperCase().contains("TEMPSEARCH"))
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") is search type view that does not have breadcrumb item.");

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5: Select the object and open Get M-Files Web URL dialog from context menu
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

			Log.message("5. Get M-Files Web URL dialog of an object (" + itemName + ") is opened from context menu.");

			//Step-6 : Select 'Show the current view' and copy the url
			//--------------------------------------------------------
			mfilesDialog.setHyperLinkAction("SHOW VIEW"); //Select show the current view in the dialog

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("6. 'Show the current view' is selected and hyperlink URL is copied.");

			//Step-7 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog

			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkURL)) //Verifies if URL is same as the hyperlink url
				throw new Exception("Link displayed in Hyperlink dialog and current page URL are not same.");

			if (!homePage.listView.isItemExists(itemName)) //Verifies if item exists in the view displayed
				throw new Exception("Item (" + itemName + ") does not exists in the hyperlink URL.");

			Log.message("7. Object Hyperlink is opened in the browser.");

			//Verification : Verify if breadcrumb available with navigated view
			//-----------------------------------------------------------------
			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Verifies if breadcrumb is displayed
				throw new Exception("Test case Failed. Breadcrumb is not displayed.");

			String viewName = dataPool.get("NavigateToView").split(">>")[dataPool.get("NavigateToView").split(">>").length - 1].trim();

			//Verifies if navigated view available in breadcrumb
			if (homePage.menuBar.GetBreadCrumbItem().toUpperCase().contains(viewName.toUpperCase())) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Breadcrumb is available with the navigated view in No Java applet, no task area layout.");
			else
				Log.fail("Test case Failed. Breadcrumb is not available with the navigated view in No Java applet, no task area layout.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (driver != null){
				try
				{
					driver.get(configURL);
					Thread.sleep(5000);
					ConfigurationPage configurationPage = new ConfigurationPage(driver);
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

	} //End SprintTest41_1_13_4A

	/**
	 * 41.1.13.4B : Navigation path should be available in No Java Applet, No task area layout of hyperlink URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint41", "Get M-Files Web URL"}, 
			description = "Navigation path should be available in No Java Applet, No task area layout of hyperlink URL - Operations menu")
	public void SprintTest41_1_13_4B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Appletn no task area' layout
			//--------------------------------------------------------------
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

			//Step-4 : Navigate to any view
			//-----------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5: Select the object and open Get M-Files Web URL dialog from operations menu
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

			Log.message("5. Get M-Files Web URL dialog of an object (" + itemName + ") is opened from operations menu.");

			//Step-6 : Select 'Show the current view' and copy the url
			//--------------------------------------------------------
			mfilesDialog.setHyperLinkAction("SHOW VIEW"); //Select show the current view in the dialog

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("6. 'Show the current view' is selected and hyperlink URL is copied.");

			//Step-7 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog

			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkURL)) //Verifies if URL is same as the hyperlink url
				throw new Exception("Link displayed in Hyperlink dialog and current page URL are not same.");



			if (!homePage.listView.isItemExists(itemName)) //Verifies if item exists in the view displayed
				throw new Exception("Item (" + itemName + ") does not exists in the hyperlink URL.");

			Log.message("7. Object Hyperlink is opened in the browser.");

			//Verification : Verify if breadcrumb available with navigated view
			//-----------------------------------------------------------------
			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Verifies if breadcrumb is displayed
				throw new Exception("Test case Failed. Breadcrumb is not displayed.");

			String viewName = dataPool.get("NavigateToView").split(">>")[dataPool.get("NavigateToView").split(">>").length - 1].trim();

			//Verifies if navigated view available in breadcrumb
			if (homePage.menuBar.GetBreadCrumbItem().toUpperCase().contains(viewName.toUpperCase())) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Breadcrumb is available with the navigated view in No Java applet, no task area layout.");
			else
				Log.fail("Test case Failed. Breadcrumb is not available with the navigated view in No Java applet, no task area layout.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (driver != null){
				try
				{
					driver.get(configURL);
					Thread.sleep(5000);
					ConfigurationPage configurationPage = new ConfigurationPage(driver);
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

	} //End SprintTest41_1_13_4B

	/**
	 * 41.1.13.5A : Navigation path should be available in No Java Applet, No task area but show GoTo shortcuts layout of hyperlink URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint41", "Get M-Files Web URL"}, 
			description = "Navigation path should be available in No Java Applet, No task area but show GoTo shortcuts layout of hyperlink URL - Context menu")
	public void SprintTest41_1_13_5A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 


		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Applet, No task area but show GoTo shortcuts' layout
			//---------------------------------------------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");


			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);

			if(configurationPage.configurationPanel.isJavaAppletEnabled())
				throw new Exception("Java applet is enabled");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_TaskAreaWithShowGoTo.Value);

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

			//Step-4 : Navigate to any view
			//-----------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			if (driver.getCurrentUrl().toUpperCase().contains("TEMPSEARCH"))
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") is search type view that does not have breadcrumb item.");

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5: Select the object and open Get M-Files Web URL dialog from context menu
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

			Log.message("5. Get M-Files Web URL dialog of an object (" + itemName + ") is opened from context menu.");

			//Step-6 : Select 'Show the current view' and copy the url
			//--------------------------------------------------------
			mfilesDialog.setHyperLinkAction("SHOW VIEW"); //Select show the current view in the dialog

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("6. 'Show the current view' is selected and hyperlink URL is copied.");

			//Step-7 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog

			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkURL)) //Verifies if URL is same as the hyperlink url
				throw new Exception("Link displayed in Hyperlink dialog and current page URL are not same.");



			if (!homePage.listView.isItemExists(itemName)) //Verifies if item exists in the view displayed
				throw new Exception("Item (" + itemName + ") does not exists in the hyperlink URL.");

			Log.message("7. Object Hyperlink is opened in the browser.");

			//Verification : Verify if breadcrumb available with navigated view
			//-----------------------------------------------------------------
			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Verifies if breadcrumb is displayed
				throw new Exception("Test case Failed. Breadcrumb is not displayed.");

			String viewName = dataPool.get("NavigateToView").split(">>")[dataPool.get("NavigateToView").split(">>").length - 1].trim();

			//Verifies if navigated view available in breadcrumb
			if (homePage.menuBar.GetBreadCrumbItem().toUpperCase().contains(viewName.toUpperCase())) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Breadcrumb is available with the navigated view in No Java applet, no task area but show GoTo shortcuts layout.");
			else
				Log.fail("Test case Failed. Breadcrumb is not available with the navigated view in No Java applet, no task area but show GoTo shortcuts layout.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			if (driver != null)
			{
				try{					
					Utility.resetToDefaultLayout(driver);
				}
				catch (Exception e0) {
					Log.exception(e0, driver);
				} //End catch
			}

			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest41_1_13_5A

	/**
	 * 41.1.13.5B : Navigation path should be available in No Java Applet, No task area but show GoTo shortcuts layout of hyperlink URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint41", "Get M-Files Web URL"}, 
			description = "Navigation path should be available in No Java Applet, No task area but show GoTo shortcuts layout of hyperlink URL - Operations menu")
	public void SprintTest41_1_13_5B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 


		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Applet, No task area but show GoTo shortcuts' layout
			//---------------------------------------------------------------------------------------
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

			//Step-4 : Navigate to any view
			//-----------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			if (driver.getCurrentUrl().toUpperCase().contains("TEMPSEARCH"))
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") is search type view that does not have breadcrumb item.");

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5: Select the object and open Get M-Files Web URL dialog from operations menu
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

			Log.message("5. Get M-Files Web URL dialog of an object (" + itemName + ") is opened from operations menu.");

			//Step-6 : Select 'Show the current view' and copy the url
			//--------------------------------------------------------
			mfilesDialog.setHyperLinkAction("SHOW VIEW"); //Select show the current view in the dialog

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("6. 'Show the current view' is selected and hyperlink URL is copied.");

			//Step-7 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog

			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkURL)) //Verifies if URL is same as the hyperlink url
				throw new Exception("Link displayed in Hyperlink dialog and current page URL are not same.");



			if (!homePage.listView.isItemExists(itemName)) //Verifies if item exists in the view displayed
				throw new Exception("Item (" + itemName + ") does not exists in the hyperlink URL.");

			Log.message("7. Object Hyperlink is opened in the browser.");

			//Verification : Verify if breadcrumb available with navigated view
			//-----------------------------------------------------------------
			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Verifies if breadcrumb is displayed
				throw new Exception("Test case Failed. Breadcrumb is not displayed.");

			String viewName = dataPool.get("NavigateToView").split(">>")[dataPool.get("NavigateToView").split(">>").length - 1].trim();

			//Verifies if navigated view available in breadcrumb
			if (homePage.menuBar.GetBreadCrumbItem().toUpperCase().contains(viewName.toUpperCase())) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Breadcrumb is available with the navigated view in No Java applet, no task area but show GoTo shortcuts layout.");
			else
				Log.fail("Test case Failed. Breadcrumb is not available with the navigated view in No Java applet, no task area but show GoTo shortcuts layout.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (driver != null){
				try
				{
					driver.get(configURL);
					Thread.sleep(5000);
					ConfigurationPage configurationPage = new ConfigurationPage(driver);
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

	} //End SprintTest41_1_13_5B

	/**
	 * 41.1.13.6 : Navigation path should not be available in Listing pane and Properties Pane only layout of hyperlink URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint41", "Get M-Files Web URL"}, 
			description = "Navigation path should not be available in Listing pane and Properties Pane only layout of hyperlink URL - Context menu")
	public void SprintTest41_1_13_6(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 


		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Listing pane and Properties Pane only' layout
			//---------------------------------------------------------------------------------------
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

			//Step-4 : Navigate to any view
			//-----------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			if (driver.getCurrentUrl().toUpperCase().contains("TEMPSEARCH"))
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") is search type view that does not have breadcrumb item.");

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5: Select the object and open Get M-Files Web URL dialog from context menu
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

			Log.message("5. Get M-Files Web URL dialog of an object (" + itemName + ") is opened from context menu.");

			//Step-6 : Select 'Show the current view' and copy the url
			//--------------------------------------------------------
			mfilesDialog.setHyperLinkAction("SHOW VIEW"); //Select show the current view in the dialog

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("6. 'Show the current view' is selected and hyperlink URL is copied.");

			//Step-7 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog

			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkURL)) //Verifies if URL is same as the hyperlink url
				throw new Exception("Link displayed in Hyperlink dialog and current page URL are not same.");



			if (!homePage.listView.isItemExists(itemName)) //Verifies if item exists in the view displayed
				throw new Exception("Item (" + itemName + ") does not exists in the hyperlink URL.");

			Log.message("7. Object Hyperlink is opened in the browser.");

			//Verification : Verify if breadcrumb available with navigated view
			//-----------------------------------------------------------------
			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Verifies if breadcrumb is displayed
				Log.pass("Test case Passed. Breadcrumb is not available with the navigated view in Properties pane and Listing pane only layout.");
			else
				Log.fail("Test case Failed. Breadcrumb is available with the navigated view in Properties pane and Listing pane only layout.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (driver != null){
				try
				{

					driver.get(configURL);
					Thread.sleep(5000);
					ConfigurationPage configurationPage = new ConfigurationPage(driver);
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

	} //End SprintTest41_1_13_6

	/**
	 * 41.1.13.7 : Navigation path should not be available in Listing pane only layout of hyperlink URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint41", "Get M-Files Web URL"}, 
			description = "Navigation path should not be available in Listing pane only layout of hyperlink URL - Context menu")
	public void SprintTest41_1_13_7(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Listing pane only only' layout
			//---------------------------------------------------------
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

			//Step-4 : Navigate to any view
			//-----------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			if (driver.getCurrentUrl().toUpperCase().contains("TEMPSEARCH"))
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") is search type view that does not have breadcrumb item.");

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5: Select the object and open Get M-Files Web URL dialog from context menu
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

			Log.message("5. Get M-Files Web URL dialog of an object (" + itemName + ") is opened from context menu.");

			//Step-6 : Select 'Show the current view' and copy the url
			//--------------------------------------------------------
			mfilesDialog.setHyperLinkAction("SHOW VIEW"); //Select show the current view in the dialog

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("6. 'Show the current view' is selected and hyperlink URL is copied.");

			//Step-7 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog

			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkURL)) //Verifies if URL is same as the hyperlink url
				throw new Exception("Link displayed in Hyperlink dialog and current page URL are not same.");



			if (!homePage.listView.isItemExists(itemName)) //Verifies if item exists in the view displayed
				throw new Exception("Item (" + itemName + ") does not exists in the hyperlink URL.");

			Log.message("7. Object Hyperlink is opened in the browser.");

			//Verification : Verify if breadcrumb available with navigated view
			//-----------------------------------------------------------------
			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Verifies if breadcrumb is displayed
				Log.pass("Test case Passed. Breadcrumb is not available with the navigated view in Listing pane only layout.");
			else
				Log.fail("Test case Failed. Breadcrumb is not available with the navigated view in Listing pane only layout.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (driver != null){
				try
				{
					driver.get(configURL);
					Thread.sleep(5000);
					ConfigurationPage configurationPage = new ConfigurationPage(driver);
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

	} //End SprintTest41_1_13_7


	/**
	 * SprintTest34319 : Verify if old hyperlink URL value with SFD ID is working correctly with Get M-files Web URL.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "GetHyperlink","US-9920"}, 
			description = "Verify if old hyperlink URL value with SFD ID is working correctly with Get M-files Web URL.")
	public void SprintTest34319(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to the 'Search only: documents' view
			//------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"),dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select object & checkout the object in that search view
			//----------------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectName"));//Select the object from the list view

			Log.message("2. Opened the metadatacard for the selected object : " + dataPool.get("ObjectName"));

			//Step-3 : Fetch the values from the selected object metadatacard
			//---------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver,true);//Instantiate the pop-out metadatacard
			int objectVersion = metadataCard.getVersion();
			int objectID = metadataCard.getObjectID();

			String objectIDVersion =  " " +dataPool.get("ObjectType") +"/" + objectID + "/"+ objectVersion;

			Log.message("3. Object type,Version and ID values are fetched from the selected object : "+ dataPool.get("ObjectName") + " opened metadatacard.", driver);

			//Step-4 : Select the object in Specified view
			//--------------------------------------------
			homePage.listView.rightClickItem(dataPool.get("ObjectName"));//Select the object in specified view

			Log.message("4. Right clicked the selected object :  " + dataPool.get("ObjectName") );

			//Step-5 : Click the 'GetHyperlink' option from the Context menu
			//--------------------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value);

			Log.message("5. Selected the " + Caption.MenuItems.GetMFilesWebURL.Value +" option from the context menu.");

			//Step-6 : Get the current url from the Get M-files web url
			//---------------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with " + Caption.MenuItems.GetMFilesWebURL.Value + " title is not opened.");

			Log.message("6." + Caption.MenuItems.GetMFilesWebURL.Value + " is opened for object (" + dataPool.get("ObjectName") + ") from context menu.");

			//Step-7: Get link URL value from Combo URL dialog
			//------------------------------------------------
			mfilesDialog.setHyperLinkAction("SHOW OBJECT"); //Select show the current view in the dialog

			//Step-8 : Get the Object GUID from the getHyperlink dialog
			//---------------------------------------------------------
			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("7. 'Show the selected object' is selected and hyperlink URL is copied.");

			//Step-7 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			String[] getMfilesWebURL = hyperlinkURL.split("/");
			String ObjectGUID = getMfilesWebURL[getMfilesWebURL.length-2] + "/" + getMfilesWebURL[getMfilesWebURL.length-1];

			Log.message("8. Fetched the object " + dataPool.get("ObjectName") + " GUID :  " + ObjectGUID +" from the Gethyperlink URL" + hyperlinkURL);


			//Step-10 : Remove the Object GUID and replace with the object type/id/version
			//----------------------------------------------------------------------------
			String modifiedURL = hyperlinkURL.replaceAll(ObjectGUID,objectIDVersion.trim());
			driver.get(modifiedURL);

			Log.message("10. Launched the modified URL : " + modifiedURL + " with object ID,version & type in new tab.", driver);

			//Verification : Verify if selected object is displayed in the gethyperlink url as expected
			//-----------------------------------------------------------------------------------------
			if (homePage.listView.itemCount() == 1 && homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				Log.pass("Test case Passed. SFD object : " + dataPool.get("ObjectName") + " is displayed successfully after replacing the object GUID with Object ID,type & version.");
			else
				Log.fail("Test case Failed.SFD object : " + dataPool.get("ObjectName") + " is not displayed successfully after replacing the object GUID with Object ID,type & version. .", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest34319

	/**
	 * SprintTest34327 : Verify if the SFD GUID with Get M-files web url is not changed on checking in an SFD.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "GetHyperlink"}, 
			description = "Verify if the SFD GUID with Get M-files web url is not changed on checking in an SFD.")
	public void SprintTest34327(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Select any object from the  from New menu
			//--------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to the " +  viewToNavigate + " view.");

			//Step-2 : Click the 'history' option from the operations menu
			//------------------------------------------------------------
			homePage.listView.rightClickItem(dataPool.get("ObjectName"));//Selected the object name 

			Log.message("2. Right clicked the object " + dataPool.get("ObjectName") + " in the search view.");

			//Step-3: Select the latest version of the object from the histroy view
			//---------------------------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value);//Select the gethyperlink option from the context menu

			Log.message("3. Selected the " + Caption.MenuItems.GetMFilesWebURL.Value +" option from the context menu for the object." + dataPool.get("ObjectName"));

			//Step-4 : Get the current url from the gethyperlink
			//--------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with " + Caption.MenuItems.GetMFilesWebURL.Value + " title is not opened.");

			Log.message("4." + Caption.MenuItems.GetMFilesWebURL.Value + " is opened for object (" + dataPool.get("ObjectName") + ") from context menu.");

			//Step-7: Get link URL value from Combo URL dialog
			//------------------------------------------------
			mfilesDialog.setHyperLinkAction("SHOW OBJECT"); //Select show the current view in the dialog

			//Step-8 : Get the Object GUID from the getHyperlink dialog
			//---------------------------------------------------------
			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("5. " + dataPool.get("ItemToClick") + "  URL value is retrived from Get Hyperlink dialog. WEB URL : " + hyperlinkURL);

			//Step-6 : Get the Object GUID from the getHyperlink dialog
			//---------------------------------------------------------
			String[] getMfilesWebURL = hyperlinkURL.split("/");
			String ObjectGUID1 = getMfilesWebURL[getMfilesWebURL.length-2];

			Log.message("6. Selected object " + dataPool.get("ObjectName") + " GUID is :  " + ObjectGUID1);

			//Step-7 : Select the object in list view
			//----------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectName"));//Selected the object name
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);//Check out the object

			Log.message("7. Selected the object & Checkout the object from the list view." + dataPool.get("ObjectName") );

			//Step-8 : Modified the selected object
			//-------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver,true);//Instantiate the right pane metadatacard
			metadataCard.addNewProperty(dataPool.get("addProperty"));//Add the new property in opened metadatacard
			metadataCard.saveAndClose();//Save the metadatacard
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.CheckIn.Value);

			Log.message("8. Modified the selected object : " + dataPool.get("ObjectName") + " by adding the new property & Checkin the object.");

			//Step-9 : Fetch the object GUID for the selected object from the get m-files web url  
			//-----------------------------------------------------------------------------------
			homePage.listView.rightClickItem(dataPool.get("ObjectName"));//Selected the object name
			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value);

			Log.message("9. Right clicked the " +  Caption.MenuItems.GetMFilesWebURL.Value + " from the context menu.");

			//Step-10 : Get m-files web url is opened for the selected object
			//---------------------------------------------------------------
			mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with " + Caption.MenuItems.GetMFilesWebURL.Value + " title is not opened.");

			Log.message("10." + Caption.MenuItems.GetMFilesWebURL.Value + " is opened for object (" + dataPool.get("ObjectName") + ") from context menu.");

			//Step-11: Get link URL value from Combo URL dialog
			//------------------------------------------------
			mfilesDialog.setHyperLinkAction("SHOW OBJECT"); //Select show the current view in the dialog

			String getMfilesWebURL1 = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			String[] hyperlink = getMfilesWebURL1.split("/");//Split the gethyperlink url 
			String ObjectGUID2 = hyperlink[hyperlink.length-2];//fetch the object GUID 

			Log.message("10. Fetched the object GUID : " + ObjectGUID2 + " from the gethyperlink url.");

			//Verification : Verify if Object GUID is same in both GetHyperlink & GetMfiles web url links
			//-------------------------------------------------------------------------------------------
			if(ObjectGUID1.trim().equalsIgnoreCase(ObjectGUID2.trim()))
				Log.pass("Test Case Passed.Selected object : " + dataPool.get("ObjectName") + " GUID is not changed after checking the SFD ." + ObjectGUID1 , driver);
			else
				Log.fail("Test Case Failed.Selected object : " + dataPool.get("ObjectName") + " GUID is changed when checking the SFD.expected : " + ObjectGUID1 + " actual GUID :  " + ObjectGUID2 , driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest34327



}

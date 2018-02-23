package MFClient.Tests.Views;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.Utils;
import genericLibrary.WebDriverUtils;

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
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class MiscViewsCases {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String userName = null;
	public static String userFullName = null;
	public static String password = null;
	public static String testVault = null;
	public static String configURL = null;
	public static String driverType = null;
	public static String productVersion = null;
	public static WebDriver driver = null;
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
			userName = xmlParameters.getParameter("UserName");
			userFullName = xmlParameters.getParameter("UserFullName");
			password = xmlParameters.getParameter("Password");
			configURL = xmlParameters.getParameter("ConfigurationURL");
			testVault = xmlParameters.getParameter("VaultName");
			driverType = xmlParameters.getParameter("driverType");
			className = this.getClass().getSimpleName().toString().trim();

			//driverManager = new TestMethodWebDriverManager();

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
	 * 1.1.1 : Default columns in virtual folders containing objects
	 */	
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint1", "Views", "Columns"}, 
			description = "Default columns in virtual folders containing objects")
	public void SprintTest1_1_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Navigate to the virtual folder
			//---------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Verification: To Verify if the expected columns are present
			//------------------------------------------------------------
			String column = dataPool.get("DefaultColumns");
			String columns[] = column.split(",");
			int count = 0;

			for(count = 0; count < columns.length; count++)
				if(!homePage.listView.isColumnExists(columns[count]))
					break;

			if(count == columns.length) 
				Log.pass("Test Case Passed. Default columns (" + dataPool.get("DefaultColumns") + ") are available in virtual folder.");
			else
				Log.fail("Test Case Failed. The default column (" + columns[count] + ") is not found in the virtual folder.", driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest1_1_1

	/**
	 * 1.1.2 : Objects inside the class virtual folders should belongs to the virtual folder clas
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint1", "Smoke"}, 
			description = "Objects inside the class virtual folders should belongs to the virtual folder clas")
	public void SprintTest1_1_2(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Navigate to the virtual folder
			//---------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Verify if all the objects displayed belong to the particular class
			//-------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0) //Checks if it is an empty view
				throw new SkipException("No objects found in the list.");

			if (!homePage.listView.insertColumn("Class")) //Inserts class column
				throw new Exception("Column (Class) is not inserted.");

			List<String> values = homePage.listView.getColumnValues("Class");
			String expectedClass = dataPool.get("NavigateToView").split(">>")[dataPool.get("NavigateToView").split(">>").length-1];
			int count = 0;

			for(count = 0; count < values.size(); count++) 
				if(!values.get(count).equals(expectedClass))
					break;

			if( count == values.size()) //Verifies if all objects belongs to the same class as virtual folder
				Log.pass("Test Case Passed. The objects in the virtual folder is of same class (" + expectedClass + ") as virtual folder.");
			else
				Log.fail("Test Case Failed. All objects in the virtual folder does not belongs to same class.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			if(driver!=null && homePage!=null){
				try
				{
					homePage.listView.removeColumn("Class");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}				
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest1_1_2

	/**
	 * 1.1.3 : Default columns in view
	 */	
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint1"}, 
			description = "Default columns in view")
	public void SprintTest1_1_3(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Click the link to the view in the task pane
			//---------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Verification: To Verify if the expected columns are present
			//------------------------------------------------------------
			String column = dataPool.get("DefaultColumns");
			String columns[] = column.split(",");
			int count = 0;

			for(count = 0; count < columns.length; count++)
				if(!homePage.listView.isColumnExists(columns[count]))
					break;

			if(count == columns.length) 
				Log.pass("Test Case Passed. Default columns (" + dataPool.get("DefaultColumns") + ") are available in view.");
			else
				Log.fail("Test Case Failed. The default column (" + columns[count] + ") is not found in view.", driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest1_1_3

	/**
	 * 1.1.4 :'Checked out to me' view should have checked out objects of that user
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint1", "Smoke"}, 
			description = "'Checked out to me' view should have checked out objects of that user")
	public void SprintTest1_1_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Check out an object
			//----------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName")))  
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			homePage.listView.clickItem(dataPool.get("ObjectName"));
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-3: Navigate to checked out to me view
			//-------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckedOutToMe.Value); //Navigates to the checked out to me view
			Utils.fluentWait(driver);

			Log.message("3. Navigated to Checked out to me view.");

			//Verification: To Verify if the Checked out document is available in the view
			//----------------------------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) { //Checks if recently checked out object exists in the view
				Log.fail("Test Case Failed. The Checked out object was not fond in the Checked out view.", driver);
				return;
			}

			if (!homePage.listView.insertColumn(Caption.Column.Coln_CheckedOutTo.Value)) //Inserts the Checked out to column
				throw new Exception("Checked out to column is not inserted.");

			List<String> checkoutUser = homePage.listView.getColumnValues("Checked Out To");
			int count = 0;

			//Verifies if all the checked out to me view displays the user as logged in user
			for(count = 0; count < checkoutUser.size(); count++)
				if(!checkoutUser.get(count).equals(userFullName))
					break;

			if(count == checkoutUser.size())
				Log.pass("Test Case Passed. Checked out to me view displays the checked out objects of the logged in user.");
			else
				Log.fail("Test Case Failed. Checked out to me view does not displays the checked out objects of the logged in user.", driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest1_1_4

	/**
	 * 1.1.5 : Recently accessed objects should be available in Recently accessed by me view
	 */	
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint1", "Smoke"}, 
			description = "Recently accessed objects should be available in Recently accessed by me view")//, timeOut = TEST_TIMEOUT)
	public void SprintTest1_1_5(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();
			//driver = driverManager.startTesting(Utility.getMethodName(), extentTest);

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Open History view of an object
			//-----------------------------------------
			//if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) 
			//throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from taskpanel

			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3: Navigate to Recently accessed by me view
			//---------------------------------------------------
			homePage.taskPanel.clickItem("Recently Accessed by Me"); //Clicks Recently accessed by me from taskpanel
			//Utils.fluentWait(driver);

			if (!homePage.menuBar.GetBreadCrumbItem().contains("Recently Accessed by Me")) //Checks if Recently accessed by me view is opened
				throw new Exception("'Recently Accessed by Me' view is not opened.");

			Log.message("3. Navigated to 'Recently Accessed by Me' view");

			//Verification: To Verify if the recently accessed document is available in the view
			//---------------------------------------------------------------------------------
			if(homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Verifies if recently accessed object exists in recently accessed by me view
				Log.pass("Test Case Passed. Recently Accessed by me view displays the recently accessed objects of the logged in user.");
			else
				Log.fail("Test Case Failed. The recently accessed object is not found in the Recently Accessed by Me view.", driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest1_1_5

	/**
	 * 1.1.6 : Default columns in history view in Web Access
	 */	
	/*	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint1"}, 
			description = "Default columns in history view in Web Access")*/
	public void SprintTest1_1_6(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Open History view of an object
			//-----------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) 
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from taskpanel

			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Verification: To Verify if the default columns are present
			//------------------------------------------------------------
			String columns[] = dataPool.get("Columns").split(",");
			int count = 0;

			for(count = 0; count < columns.length; count++) 
				if(!homePage.listView.isColumnExists(columns[count]))
					break;

			if(count == columns.length) 
				Log.pass("Test Case Passed. The default Columns (" + dataPool.get("Columns") + ") are found in the history view.");
			else
				Log.fail("Test Case Failed. The default column '" + columns[count] + "' is not found in History view.", driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest1_1_6

	/**
	 * 1.2.1 : Default settings in Home view
	 */	
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint1"}, 
			description = "Default settings in Home view")
	public void SprintTest1_2_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Verify default columns are available in the Home view
			//-------------------------------------------------------------
			String columns[] = dataPool.get("Columns").split(",");
			int count = 0;
			Boolean tcPass = false;

			for(count = 0; count < columns.length; count++) 
				if(!homePage.listView.isColumnExists(columns[count]))
					break;

			if(count != columns.length) 
				Log.fail("1. The default column " + columns[count] + " does not exist in Home view", driver);
			else {
				Log.message("1. Default columns (" + dataPool.get("Columns") + ") are available in home view.");
				tcPass = true;
			}

			//Step-2 : To Verify if the default views are displayed in Details mode
			//----------------------------------------------------------------------
			String views[] = dataPool.get("Views").split(",");

			for(count = 0; count < views.length; count++)
				if(!homePage.listView.isItemExists(views[count]))
					break;

			if(count != views.length)
				Log.fail("2. The default view " + views[count] + " does not exist", driver);
			else {
				Log.message("2. Default views (" + dataPool.get("Views") + ") are available in home view.");
				tcPass = true;
			}

			//Step-3 : To Verify if only views are displayed in Home view
			//-----------------------------------------------------------
			for(count = 0; count < views.length; count++) 
				if(!homePage.listView.getColumnValueByItemName(views[count], "Type").equals("View"))
					break;

			if(count != views.length) 
				Log.fail("3. Home view contains item (" + views[count] + ") other than view type.", driver);
			else {
				Log.message("3. Items of type 'View' only is available in home view.");
				tcPass = true;
			}

			//Verification : Verify only if all default settings are correct in Home view
			//---------------------------------------------------------------------------
			if (tcPass)
				Log.pass("Test case Passed. Home view is displayed with all default settings.");
			else
				Log.fail("Test case Failed. Home view does not default settings.", driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest1_2_1

	/**
	 * 2.1.1 : Verify the display of context menu for the Virtual Folders in different views
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug", "Sprint2"}, 
			description = "Verifying the view area and the column bar after logging in")
	public void SprintTest2_1_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Navigate to any virtual folder
			//-----------------------------
			String enabledMenu = dataPool.get("EnabledMenu");
			String enabledMenus[] = enabledMenu.split(",");
			String disabledMenu = dataPool.get("DisabledMenu");
			String disabledMenus[] = disabledMenu.split(",");
			int count = 0;

			homePage.taskPanel.clickItem("Home"); //Navigate to home
			Utils.fluentWait(driver);

			homePage.listView.navigateThroughView(dataPool.get("Path")); //Navigate to the required view
			Utils.fluentWait(driver);

			Log.message("1. Navigate to the Virtual folder.");

			//Step-2: Right Click on the Virtual folder
			//--------------------------------------------
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("VirtualFolder"))) 
				throw new SkipException("Invalid Test data. The given virtual folder was not found in the path.");

			homePage.listView.rightClickItem(dataPool.get("VirtualFolder"));
			Utils.fluentWait(driver);

			Log.message("2. Right Click on the Virtual folder.");

			//Verification: To Verify if the context menus are displayed and disabled correctly
			//----------------------------------------------------------------------------------
			for(count = 0; count < enabledMenus.length; count++) { //verify if the expected options are available
				if(!homePage.listView.itemExistsInContextMenu(enabledMenus[count]))
					break;
			}

			if(count != enabledMenus.length) { 
				Log.fail("Test Case Failed. The Expected Menu " + enabledMenus[count] + " was not found in the context menu.", driver);
				return;
			}

			for(count = 0; count < disabledMenus.length; count++) {
				if(!homePage.listView.itemExistsInContextMenu(disabledMenus[count]))
					break;
			}

			if(count != disabledMenus.length) { //Verify if the expected menus are disabled
				Log.fail("Test Case Failed. The Expected Menu " + disabledMenus[count] + " was not found in the context menu.", driver);
				return;
			}

			for(count = 0; count < enabledMenus.length; count++) {//Verify if the expected menus are enabled
				if(!homePage.listView.itemEnabledInContextMenu(enabledMenus[count]))
					break;
			}

			if(count != enabledMenus.length) { 
				Log.fail("Test Case Failed. The Expected Menu " + enabledMenus[count] + " was not enabled in the context menu.", driver);
				return;
			}

			for(count = 0; count < disabledMenus.length; count++) { //Verify if the expected menus are disabled
				if(homePage.listView.itemEnabledInContextMenu(disabledMenus[count]))
					break;
			}

			if(count == disabledMenus.length) 
				Log.pass("Test Case Passed. The Expected context menu was enabled and the rest were disabled.");
			else
				Log.fail("Test Case Failed. The Expected Menu " + disabledMenus[count] + " was enabled in the context menu.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 2.1.2 : Verify the display of 'Date Modified' column value for the checked out document
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint2"}, 
			description = "Verify the display of 'Date Modified' column value for the checked out document")
	public void SprintTest2_1_2(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Search for an object and select it
			//-------------------------------------------
			homePage.searchPanel.search(dataPool.get("ObjectName"), Caption.Search.SearchAllObjects.Value);
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("ObjectName")))
				throw new SkipException("Invalid Test data. The given object was not found in the vault.");

			Log.message("1. Search for an object and select it.");

			//Step-2: Check out the object
			//-----------------------------
			homePage.listView.clickItem(dataPool.get("ObjectName"));
			DateFormat dateFormat = new SimpleDateFormat("M/d/yyyy h:mm aaa", Locale.ENGLISH);
			Date date = new Date();
			String dateTime = dateFormat.format(date);
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);

			Log.message("2. Check out the object at : " + dateTime);

			//Verification: To Verify if the Date modified column shows the current date and time
			//------------------------------------------------------------------------------------
			String colnDateTime = homePage.listView.getColumnValueByItemName(dataPool.get("ObjectName"), "Date Modified");
			String addlInfo = "Checked out time : " + dateTime + "; Date Modified Column : " + colnDateTime;

			if(colnDateTime.equals(dateTime))
				Log.pass("Test Case Passed. The Date Modified column has the value as the time at which the object was checked out.");
			else
				Log.fail("Test Case Failed. The Date Modified column did not show the value as the time at which the object was checked out. AddlInfo : " + addlInfo, driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 2.1.3 : Verify the display of 'Date Modified' column value when document is checkedIn without performing any modifications
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint2"}, 
			description = "Verify the display of 'Date Modified' column value when document is checkedIn without performing any modifications")
	public void SprintTest2_1_3(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Check out the object
			//-----------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) 
				throw new SkipException("Invalid Test data. The given object " + dataPool.get("ObjectName") + " was not found in the vault.");

			String expectedValue = homePage.listView.getColumnValueByItemName(dataPool.get("ObjectName"), "Date Modified");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Checks out the object
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if object is checked out
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-3 : Check-in the object without any changes
			//-------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value); //Checks in the object
			Utils.fluentWait(driver);

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if object is checked in

				Log.message("3. Object (" + dataPool.get("ObjectName") + ") is checked in.");

			//Verification: To Verify if the Date modified column shows the current date and time
			//------------------------------------------------------------------------------------
			String actualValue = homePage.listView.getColumnValueByItemName(dataPool.get("ObjectName"), "Date Modified");

			if (actualValue.equalsIgnoreCase(expectedValue)) //Verifies if date modified column is not changed
				Log.pass("Test Case Passed. No changes found in the Date Modified column when object is checked out and checked in without making any changes.");
			else
				Log.fail("Test Case Failed. Changes found in the Date Modified column when object is checked out and checked in without making any changes." +
						"Diff : " + expectedValue + " (Before) ;" + actualValue + " (After)", driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest2_1_3

	/**
	 * 2.1.4 : Verify the display of 'Sort Indicator' for all displayed columns
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint2"}, 
			description = "Verify the display of 'Sort Indicator' for all displayed columns")
	public void SprintTest2_1_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Navigate to any view
			//-----------------------------
			homePage.listView.navigateThroughView(dataPool.get("View")); //Navigate to the required view
			Utils.fluentWait(driver);

			Log.message("1. Navigate to any view.");

			//Step-2: Click on each column in the view
			//--------------------------------------------
			String columns[] = homePage.listView.getVisibleColumns();
			int count = 0;

			for(count = 0; count < columns.length; count++) {
				homePage.listView.clickColumn(columns[count]);
				if(homePage.listView.getColumnSortImage(columns[count]).equals(""))
					break;
			}

			Log.message("2. Click on each column in the view.");

			//Verification: To verify if each of the selected column has the sort icon displayed
			//----------------------------------------------------------------------------------
			if(count == columns.length) 
				Log.pass("Test Case Passed. The Sort icon appeared for all columns that was selected.");
			else
				Log.fail("Test Case Failed. The Sort icon did not appear for " + columns[count] + " all columns that was selected.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 2.1.5 : User should be able to re-size all the visble columns 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint2"}, 
			description = "User should be able to re-size all the visble columns")
	public void SprintTest2_1_5(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("Safari"))
			throw new SkipException(driverType.toUpperCase() + " driver does not support Actions.");

		//Variable Declaration
		driver = null;

		try {

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Navigate to any view
			//-----------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Click on each column in the view
			//----------------------------------------
			if (!homePage.previewPane.togglePreviewPane(true)) //Hides Preview pane
				throw new Exception("Preview pane is not hidden.");

			String columns[] = homePage.listView.getVisibleColumns();
			int width = 30;
			int prevWidth = 0;
			int count = 0;

			for(count = 0; count < columns.length; count++) {
				prevWidth = homePage.listView.getColumnWidth(columns[count]);
				Utils.fluentWait(driver);

				//
				if (count == 4){
					int column_width = 70;
					homePage.listView.resizeColumn(columns[0], -column_width);

				}
				int incrResizeWidth = homePage.listView.resizeColumn(columns[count], width); //Increases the width

				if(incrResizeWidth <= prevWidth) //Checks if width is increased.
					break;

				Utils.fluentWait(driver);

				if(homePage.listView.resizeColumn(columns[count], -width) >= incrResizeWidth) //Decrease the width and checks the decreased width.
					break;

				Utils.fluentWait(driver);
			}

			Log.message("2. All the visible columns are re-sized.");

			//Verification: To verify if the resizing can be done for all the displayed columns
			//----------------------------------------------------------------------------------
			if(count == columns.length) 
				Log.pass("Test Case Passed. All the columns were re-sized as expected.");
			else
				Log.fail("Test Case Failed. The Column " + columns[count] + " was not re-sized as expected.",driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		} //End finally

	}



	/**
	 * 29.2.1 : Verify the grouping of objects using various properties
	 */
	/*@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint29", "Subobjects"}, 
			description = "Verify the grouping of objects using various properties")*/
	public void SprintTest29_2_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Navigate in to the View with Date property based grouping
			//-------------------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ViewName")))
				throw new SkipException("Invalid Test data. The given View (" + dataPool.get("ViewName") + ") was not found in the vault.");

			homePage.listView.navigateThroughView(dataPool.get("ViewName"));
			Utils.fluentWait(driver);

			Log.message("1. Navigated in to the View with Date property based grouping.");

			//Verification: To verify if the expected virtual folders are created and the contents are appropriate 
			//-----------------------------------------------------------------------------------------------------
			String[] expectedValues = dataPool.get("Expected").split("\n");
			int folderCounter = 0;
			int objectCounter = 0;
			int itemCount = 0;

			for(folderCounter = 0; folderCounter < expectedValues.length; folderCounter++) {

				if(homePage.listView.isItemExists(expectedValues[folderCounter].toString())) {

					homePage.listView.navigateThroughView(expectedValues[folderCounter].toString());
					Utils.fluentWait(driver);
					itemCount = homePage.listView.itemCount();
					homePage.listView.insertColumn(dataPool.get("Column"));
					Utils.fluentWait(driver);

					for(objectCounter = 0; objectCounter < itemCount; objectCounter++) {
						if(!homePage.listView.getColumnValueByItemIndex(objectCounter, dataPool.get("Column")).startsWith(expectedValues[folderCounter].toString()))
							break;
					}

					if( objectCounter != itemCount)
						break;

					homePage.taskPanel.clickItem("Home");
					Utils.fluentWait(driver);
					homePage.listView.navigateThroughView(dataPool.get("ViewName"));
					Utils.fluentWait(driver);

				}

				else 
					break;

			}

			if (folderCounter == expectedValues.length)
				Log.pass("Test case Passed. The Virtual folders and it's contents are listed as expected.");
			else
				Log.fail("Test case Failed. The Virtual folders or it's contents are not listed as expected.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest29_2_1

}

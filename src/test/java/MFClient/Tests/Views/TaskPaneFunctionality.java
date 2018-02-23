package MFClient.Tests.Views;

import java.util.HashMap;
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
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class TaskPaneFunctionality {

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
	 * 33.1.1 : 'New menu' should be in collapsed state by default.
	 */
	@Test(groups = {"Sprint33", "Subobjects"}, description = "'New menu' should be in collapsed state by default.")
	public void SprintTest33_1_1() throws Exception {

		driver = null;

		try {


			driver = WebDriverUtils.getDriver();

			//Logged into MFWA with valid credentials
			//---------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Verification : Verify if header is in collapsed state
			//-----------------------------------------------------
			if(homePage.taskPanel.isNewMenuItemExpand())
				Log.pass("Test case Passed. The Task Panel 'New menu' is in the collapsed state.");
			else
				Log.fail("Test Case Failed. Expected New Menu item is not in collapsed state.",driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest33_1_1

	/**
	 * 33.1.2 : View and Modify items should be expanded by default,and sub items should be available 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint33", "Subobjects"}, 
			description = "View and Modify items should be expanded by default,and sub items should be available")
	public void SprintTest33_1_2(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Navigate to any view
			//-----------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("SearchType"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//2. Click on a Checked-in object
			//--------------------------------
			String objectName = dataPool.get("ObjectName");

			if (ListView.isCheckedOutByItemName(driver, objectName))
				throw new SkipException("Object " + objectName + " is in checked out state.");

			if (!homePage.listView.clickItem(objectName))
				throw new Exception("Object " + objectName + " is not selected.");

			Utils.fluentWait(driver);

			Log.message("2. Checked in object (" + objectName + ") is selected.");

			//Verification: To verify if new option is available and it is in expanded state
			//-------------------------------------------------------------------------------
			if(!homePage.taskPanel.isHeaderExpanded(dataPool.get("MenuItem")))
				throw new Exception("Test case Failed. The Task Panel item " + dataPool.get("MenuItem") + " was not in the expanded state.");

			String[] expectedMenu = dataPool.get("ExpectedMenu").split("\n");
			String addlInfo = "";

			for(int count = 0; count < expectedMenu.length; count++)
				if(!homePage.taskPanel.isItemExists(expectedMenu[count]))
					addlInfo = addlInfo + ";" + expectedMenu[count];

			if (addlInfo.equals(""))
				Log.pass("Test case Passed. Taskpanel is in expanded state and all the expected sub-items are available.");
			else
				Log.fail("Test Case Failed. ", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest33_1_2

	/**
	 * 33.1.3 : Verify the Default View and Modify items for checked out object by clicking checkout option in task pane.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint33", "Subobjects"}, 
			description = "Verify the Default View and Modify items for checked out object by clicking checkout option in task pane.")
	public void SprintTest33_1_3(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Perform the Search
			//-----------------------
			Utils.fluentWait(driver);
			homePage.searchPanel.search("", dataPool.get("SearchType"));

			Log.message("1. Performed the Search.");

			//2. Click on an object
			//----------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName")))
				throw new SkipException("Invalid Test Data. The specified object " + dataPool.get("ObjectName") + " was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("ObjectName"));
			Utils.fluentWait(driver);

			Log.message("2. Click on an object");

			//3. Check out the Object
			//---------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);

			Log.message("3. Check out the Object");

			//Verification: To verify if view and modify option and it's sub menu is available
			//---------------------------------------------------------------------------------
			if(!homePage.taskPanel.isHeaderExpanded(dataPool.get("MenuItem")))
				Log.fail("Test case Failed. The Task Panel item " + dataPool.get("MenuItem") + " was not in the expanded state.", driver);

			int count = 0;
			String[] expectedMenu = dataPool.get("ExpectedMenu").split("\n");

			for(count = 0; count < expectedMenu.length; count++) {
				if(!homePage.taskPanel.isItemExists(expectedMenu[count]))
					Log.fail("Test Case Failed. The " + dataPool.get("MenuItem") + " menu was expanded, but an expected menu "+expectedMenu[count]+" was not listed.", driver);
			}

			Log.pass("Test case Passed. The Task Panel item " + dataPool.get("MenuItem") + " was in the expanded state.");

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest33_1_3

	/**
	 * 33.1.4 : Verify the view after Collapse view and modify items.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint33", "Subobjects"}, 
			description = "Verify the view after Collapse view and modify items.")
	public void SprintTest33_1_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Navigate to any view
			//------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("SearchType"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//2. Click on an object
			//----------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName")))
				throw new SkipException("Invalid Test Data. The specified object " + dataPool.get("ObjectName") + " was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("ObjectName"));
			Utils.fluentWait(driver);

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//3. Collapse the View and Modify section in the Task pane.
			//----------------------------------------------------------
			if (!homePage.taskPanel.collapseItem(dataPool.get("MenuItem"))) //Collapse the taskpanel header
				throw new Exception ("Task Panel header item (" + dataPool.get("MenuItem") + ") is not collapsed.");

			Utils.fluentWait(driver);

			Log.message("3. Taskpanel header (" + dataPool.get("MenuItem") + ") is collapsed");

			//4. Click on any other object
			//-----------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName2")))
				throw new SkipException("Invalid Test Data. The specified object " + dataPool.get("ObjectName") + " was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("ObjectName2"));
			Utils.fluentWait(driver);

			Log.message("4. Object (" + dataPool.get("ObjectName2") + ") is selected.");

			//Verification: To verify if view and modify option is in collapsed state
			//------------------------------------------------------------------------
			if (!homePage.taskPanel.isHeaderExpanded(dataPool.get("MenuItem")))
				Log.pass("Test case Passed. The Task Panel item " + dataPool.get("MenuItem") + " is in collapsed state.");
			else
				Log.fail("Test Case Failed. The Task Panel item " + dataPool.get("MenuItem") + " is in expanded state.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest33_1_4

	/**
	 * 33.1.6 : Verify to Collapse Move into state item in taskpane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint33", "Subobjects"}, 
			description = "Verify to collapse Move into state item in taskpane.")
	public void SprintTest33_1_6(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//2. Set Workflow to the object
			//------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName")))
				throw new SkipException("Invalid Test Data. The specified object " + dataPool.get("ObjectName") + " was not found in the vault.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new SkipException("Object " + dataPool.get("ObjectName") + " is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);
			Utils.fluentWait(driver);

			MFilesDialog mFilesDialog = new MFilesDialog(driver); //Instatiate MFiles dialog
			mFilesDialog.setWorkflow(dataPool.get("Workflow")); //Sets the workflow
			mFilesDialog.setWorkflowState(dataPool.get("State")); //Sets the workflow state
			mFilesDialog.clickOkButton(); //Clicks Ok button
			Utils.fluentWait(driver);

			Log.message("2. Workflow and State are set to the object(" + dataPool.get("ObjectName") + ").");

			//3. Collapse the Move into State section in the Task pane.
			//----------------------------------------------------------
			if (!homePage.taskPanel.collapseItem(dataPool.get("MenuItem"))) //Collapse the taskpanel header
				throw new Exception ("Task Panel header item (" + dataPool.get("MenuItem") + ") is not collapsed.");

			Utils.fluentWait(driver);

			Log.message("3. Taskpanel header (" + dataPool.get("MenuItem") + ") is collapsed");

			//Verification: To verify if view and modify option is in collapsed state
			//------------------------------------------------------------------------
			if(!homePage.taskPanel.isHeaderExpanded(dataPool.get("MenuItem")))
				Log.pass("Test case Passed. The Task Panel item " + dataPool.get("MenuItem") + " is in collapsed state.");
			else
				Log.fail("Test Case Failed. The Task Panel item " + dataPool.get("MenuItem") + " is in expanded state.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest33_1_6

	/**
	 * 33.1.7A : Verify to Collapse 'View and Modify' item in taskpane for checked out objects (Collapse - Check out)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint33", "Subobjects"}, 
			description = "Verify to Collapse 'View and Modify' item in taskpane for checked out objects (Collapse - Check out).")
	public void SprintTest33_1_7A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//2. Check out an object
			//-----------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName")))
				throw new SkipException("Invalid Test Data. The specified object " + dataPool.get("ObjectName") + " was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("ObjectName"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);

			Log.message("2. Check out an object.");

			//3. Click on a checked in Object
			//-------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName2")))
				throw new SkipException("Invalid Test Data. The specified object " + dataPool.get("ObjectName") + " was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("ObjectName2"));
			Utils.fluentWait(driver);

			Log.message("3. Click on a checked in Object");

			//4. Collapse the menu
			//---------------------
			homePage.taskPanel.collapseItem(dataPool.get("MenuItem"));
			Utils.fluentWait(driver);

			Log.message("4. Collapsed the menu");

			//5. Click on the checked out object
			//----------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectName"));

			Log.message("4. Click on the checked out object");

			//Verification: To verify if view and modify option is in collapsed state
			//------------------------------------------------------------------------
			if(!homePage.taskPanel.isHeaderExpanded(dataPool.get("MenuItem")))
				Log.pass("Test case Passed. The Task Panel item " + dataPool.get("MenuItem") + " was in collapsed state.");
			else
				Log.fail("Test Case Failed. The Task Panel item " + dataPool.get("MenuItem") + " was in expanded state.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest33_1_7A

	/**
	 * 33.1.7B : Verify to Collapse 'View and Modify' item in taskpane for checked out objects (Check out - collapse)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint33", "Subobjects"}, 
			description = "Verify to Collpase 'View and Modify' item in taskpane for checked out objects (Check out - collapse).")
	public void SprintTest33_1_7B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//2. Check out an object
			//-----------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName")))
				throw new SkipException("Invalid Test Data. The specified object " + dataPool.get("ObjectName") + " was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("ObjectName"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);

			Log.message("2. Check out an object.");

			//3. Collapse the menu
			//---------------------
			homePage.taskPanel.collapseItem(dataPool.get("MenuItem"));
			Utils.fluentWait(driver);

			Log.message("3. Collapsed the menu");

			//Verification: To verify if view and modify option is in collapsed state
			//------------------------------------------------------------------------
			if(!homePage.taskPanel.isHeaderExpanded(dataPool.get("MenuItem")))
				Log.pass("Test case Passed. The Task Panel item " + dataPool.get("MenuItem") + " was in collapsed state.");
			else
				Log.fail("Test Case Failed. The Task Panel item " + dataPool.get("MenuItem") + " was in expanded state.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest33_1_7B

	/**
	 * 33.1.8 : Verify to collapse the 'Root view' items in task pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint33", "TaskPane"}, 
			description = "Verify to collapse the 'Root view' items in task pane")
	public void SprintTest33_1_8(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//2. Click on an object
			//-----------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName")))
				throw new SkipException("Invalid Test Data. The specified object " + dataPool.get("ObjectName") + " was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("ObjectName"));
			Utils.fluentWait(driver);

			Log.message("2. Click on an object.");

			//3. Collapse the menu
			//---------------------
			homePage.taskPanel.collapseItem(dataPool.get("MenuItem"));
			Utils.fluentWait(driver);

			Log.message("3. Collapsed the menu");

			//4. Click the Home Link
			//-----------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);
			Utils.fluentWait(driver);

			Log.message("4. Clicked the Home Link");

			//Verification: To verify if only New and Go To options are available
			//--------------------------------------------------------------------
			if(!homePage.taskPanel.isItemExists(dataPool.get("MenuItem")) && homePage.taskPanel.isItemExists("New") && homePage.taskPanel.isItemExists("Go To"))
				Log.pass("Test case Passed. The Task Panel item " + dataPool.get("MenuItem") + " was in collapsed state.");
			else
				Log.fail("Test Case Failed. The Task Panel item " + dataPool.get("MenuItem") + " was in expanded state.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest33_1_7B

	/**
	 * 41.8.6 : Clicking breadcrumb vault icon should navigate to Home view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint41"}, 
			description = "Clicking breadcrumb vault icon should navigate to Home view")
	public void SprintTest41_8_6(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Click vault icon in breadcrumb
			//----------------------------------------
			homePage.menuBar.clickBreadcrumbVaultIcon(); //Clicks Breadcrumb vault icon
			Utils.fluentWait(driver);

			Log.message("2. Breadcrumb vault icon is clicked.");

			//Verification : To verify if navigated to home view
			//--------------------------------------------------
			if (homePage.menuBar.GetBreadCrumbItem().equalsIgnoreCase(testVault))
				Log.pass("Test case Passed. Navigated to Home view successfully on clicking vault icon in breadcrumb.");
			else
				Log.fail("Test case Failed. Not navigated to Home view on clicking vault icon in breadcrumb.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest41_8_6

	/**
	 * 7.1.1A : Deleting an object should not display options related to the object - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint7", "History"}, 
			description = "Deleting an object should not display options related to the object - Context menu")
	public void SprintTest7_1_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Delete the Object
			//-------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) 
				throw new SkipException("Invalid Test Data. The specified Object " + dataPool.get("ObjectName") + " was not found in the vault.");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //click the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			Log.message("2. Object is right clicked (" + dataPool.get("ObjectName") + ").");

			//Step-3: Delete the Object through context menu
			//----------------------------------------------
			homePage.listView.clickContextMenuItem("Delete"); //Selects Delete from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class
			mfilesDialog.confirmDelete(); //Confirms the delete operation
			Utils.fluentWait(driver);

			if (homePage.listView.isItemExists(dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") exists in the list after clicking Delete from context menu.");

			Log.message("3. Object is (" + dataPool.get("ObjectName") + ") deleted through context menu.");

			//Verification: To Verify the options displayed in the task pane when the object is deleted
			//------------------------------------------------------------------------------------------
			String option = dataPool.get("Options");
			String options[] = option.split(",");
			int count = 0;

			for(count = 0; count < options.length; count++)
				if(homePage.taskPanel.isItemExists(options[count]))
					break;

			if(count == options.length) 
				Log.pass("Test Case Passed. The specified Options were not diaplayed in the task pane once the Object was deleted.");
			else
				Log.fail("Test Case Failed. The option " + options[count] + " was found in the task pane even after deleting the object.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest7_1_1

	/**
	 * 7.1.1B : Deleting an object should not display options related to the object - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint7", "History"}, 
			description = "Deleting an object should not display options related to the object - Operations menu")
	public void SprintTest7_1_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object
			//--------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) 
				throw new SkipException("Invalid Test Data. The specified Object " + dataPool.get("ObjectName") + " was not found in the vault.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //click the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-3: Delete the Object through operations menu
			//----------------------------------------------
			homePage.menuBar.ClickOperationsMenu("Delete"); //Select Delete from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class
			mfilesDialog.confirmDelete(); //Confirms the delete operation
			Utils.fluentWait(driver);

			if (homePage.listView.isItemExists(dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") exists in the list after clicking Delete from operations menu.");

			Log.message("3. Object is (" + dataPool.get("ObjectName") + ") deleted through operations menu.");

			//Verification: To Verify the options displayed in the task pane when the object is deleted
			//------------------------------------------------------------------------------------------
			String option = dataPool.get("Options");
			String options[] = option.split(",");
			int count = 0;

			for(count = 0; count < options.length; count++)
				if(homePage.taskPanel.isItemExists(options[count]))
					break;

			if(count == options.length) 
				Log.pass("Test Case Passed. The specified Options were not diaplayed in the task pane once the Object was deleted.");
			else
				Log.fail("Test Case Failed. The option " + options[count] + " was found in the task pane even after deleting the object.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest7_1_1B
}

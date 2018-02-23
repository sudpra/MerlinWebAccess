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
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class BackButtonInViews {

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
	 * 25.11.1.1A : Back button should be available in History view - Taskpane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "History"}, 
			description = "Back button should be available in History view - Taskpane")
	public void SprintTest25_11_1_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open History view of the object 
			//----------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel
			Utils.fluentWait(driver);

			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened through task panel.");

			//Verification : To verify if back button in the History view is available
			//------------------------------------------------------------------------
			if (homePage.listView.isBackToViewButtonExists())
				Log.pass("Test case Passed. Back button in History view exists.");
			else
				Log.fail("Test case Failed. Back button in History view does not exists.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_11_1_1A

	/**
	 * 25.11.1.1B : Back button should be available in History view - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "History"}, 
			description = "Back button should be available in History view - Context menu")
	public void SprintTest25_11_1_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open History view of the object
			//----------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not not right clicked.");

			homePage.listView.clickContextMenuItem("History"); //Selects History from Context menu
			Utils.fluentWait(driver);

			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened through Context menu.");

			//Verification : To verify if back button in the History view is available
			//------------------------------------------------------------------------
			if (homePage.listView.isBackToViewButtonExists())
				Log.pass("Test case Passed. Back button in History view exists.");
			else
				Log.fail("Test case Failed. Back button in History view does not exists.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_11_1_1B

	/**
	 * 25.11.1.1C : Back button should be available in History view - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "History"}, 
			description = "Back button should be available in History view - Operations menu")
	public void SprintTest25_11_1_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open History view of the object
			//----------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not not right clicked.");

			homePage.menuBar.ClickOperationsMenu("History"); //Selects History from Operations menu
			Utils.fluentWait(driver);

			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened through Operations menu.");

			//Verification : To verify if back button in the History view is available
			//------------------------------------------------------------------------
			if (homePage.listView.isBackToViewButtonExists())
				Log.pass("Test case Passed. Back button in History view exists.");
			else
				Log.fail("Test case Failed. Back button in History view does not exists.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_11_1_1C

	/**
	 * 25.11.1.2A : Clicking back button in History view should navigate to the previous view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "History"}, 
			description = "Clicking back button in History view should navigate to the previous view")
	public void SprintTest25_11_1_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open History view of the object
			//----------------------------------------
			String prevViewUrl = driver.getCurrentUrl(); //Gets the current view URL

			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel
			Utils.fluentWait(driver);

			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened through task panel.");

			//Step-3 : Click Back to view button in the History view
			//------------------------------------------------------
			homePage.listView.clickBackToViewButton(); //Clicks Back to view button
			Utils.fluentWait(driver);
			String currViewUrl = driver.getCurrentUrl();

			Log.message("3. Back to view button is clicked.");

			//Verification : To verify if back button in the History view is available
			//------------------------------------------------------------------------
			if (currViewUrl.equals(prevViewUrl))
				Log.pass("Test case Passed. Clicking back to view button navigated to previous view.");
			else
				Log.fail("Test case Failed. Clicking back to view button has not navigated to previous view.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_11_1_2A

	/**
	 * 25.11.1.2B : Clicking back button in History view should navigate to the previous view - Relationships view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "History"}, 
			description = "Clicking back button in History view should navigate to the previous view - Relationships view")
	public void SprintTest25_11_1_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open Relationships view of an object
			//---------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) 
				throw new SkipException("Invalid Test data. The given object " + dataPool.get("ObjectName") + " was not found in the view.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu("Relationships");
			Utils.fluentWait(driver);

			if (!ListView.isRelationshipsViewOpened(driver)) //Checks if History view is opened
				throw new Exception("Relationship view is not opened.");

			Log.message("2. Relationships view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open History view of the object and select latest version
			//--------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0)  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not have related objects.");

			String prevViewUrl = driver.getCurrentUrl(); //Gets the current view URL

			if (!homePage.listView.clickItemByIndex(0)) //Selects the first related object in the list
				throw new Exception("Object is not selected to open History view.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel
			Utils.fluentWait(driver);

			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view is not opened.");

			Log.message("3. History view is opened through task panel.");

			//Step-3 : Click Back to view button in the History view
			//------------------------------------------------------
			homePage.listView.clickBackToViewButton(); //Clicks Back to view button
			Utils.fluentWait(driver);
			String currViewUrl = driver.getCurrentUrl();

			Log.message("4. Back to view button is clicked.");

			//Verification : To verify if back button in the History view is available
			//------------------------------------------------------------------------
			if (currViewUrl.equals(prevViewUrl))
				Log.pass("Test case Passed. Clicking back to view button navigated to Relationships view.");
			else
				Log.fail("Test case Failed. Clicking back to view button has not navigated to Relationships view.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_11_1_2B

	/**
	 * 25.11.1.2C : Clicking back button in History view should navigate to the previous view - Sub-Objects view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "History"}, 
			description = "Clicking back button in History view should navigate to the previous view - Sub-Objects view")
	public void SprintTest25_11_1_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {




			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open Sub-Objects view of an object
			//---------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) 
				throw new SkipException("Invalid Test data. The given object " + dataPool.get("ObjectName") + " was not found in the view.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.ShowSubObjects.Value);
			Utils.fluentWait(driver);

			if (!ListView.isSubObjectsViewOpened(driver)) //Checks if History view is opened
				throw new Exception("Show Subobjects view is not opened.");

			Log.message("2. Show Subobjects view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open History view of the object and select latest version
			//--------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0)  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not have sub objects.");

			String prevViewUrl = driver.getCurrentUrl(); //Gets the current view URL

			if (!homePage.listView.clickItemByIndex(0)) //Selects the first related object in the list
				throw new Exception("Object is not selected to open History view.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel
			Utils.fluentWait(driver);

			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view is not opened.");

			Log.message("3. History view is opened through task panel.");

			//Step-3 : Click Back to view button in the History view
			//------------------------------------------------------
			homePage.listView.clickBackToViewButton(); //Clicks Back to view button
			Utils.fluentWait(driver);
			String currViewUrl = driver.getCurrentUrl();

			Log.message("4. Back to view button is clicked.");

			//Verification : To verify if back button in the History view is available
			//------------------------------------------------------------------------
			if (currViewUrl.equals(prevViewUrl))
				Log.pass("Test case Passed. Clicking back to view button navigated to Subobjects view.");
			else
				Log.fail("Test case Failed. Clicking back to view button has not navigated to Subobjects view.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_11_1_2C

	/**
	 * 25.11.1.2D : Clicking back button in History view should navigate to the previous view - Members view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "History"}, 
			description = "Clicking back button in History view should navigate to the previous view - Members view")
	public void SprintTest25_11_1_2D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open Members view of an object
			//---------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) 
				throw new SkipException("Invalid Test data. The given object " + dataPool.get("ObjectName") + " was not found in the view.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem("Show Members");
			Utils.fluentWait(driver);

			if (!ListView.isMembersViewOpened(driver)) //Checks if Members view is opened
				throw new Exception("Show Members view is not opened.");

			Log.message("2. Members view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open History view of the object and select latest version
			//--------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0)  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not have members.");

			String prevViewUrl = driver.getCurrentUrl(); //Gets the current view URL

			if (!homePage.listView.clickItemByIndex(0)) //Selects the first related object in the list
				throw new Exception("Object is not selected to open History view.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel
			Utils.fluentWait(driver);

			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view is not opened.");

			Log.message("3. History view is opened through task panel.");

			//Step-3 : Click Back to view button in the History view
			//------------------------------------------------------
			homePage.listView.clickBackToViewButton(); //Clicks Back to view button
			Utils.fluentWait(driver);
			String currViewUrl = driver.getCurrentUrl();

			Log.message("3. Back to view button is clicked.");

			//Verification : To verify if back button in the History view is available
			//------------------------------------------------------------------------
			if (currViewUrl.equals(prevViewUrl))
				Log.pass("Test case Passed. Clicking back to view button navigated to Members view.");
			else
				Log.fail("Test case Failed. Clicking back to view button has not navigated to Members view.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_11_1_2D

	/**
	 * 25.11.2.1A : Back button should be available in Relationships view - Taskpane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "History"}, 
			description = "Back button should be available in Relationships view - Taskpane")
	public void SprintTest25_11_2_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();


			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open Relationships view of the object
			//------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu("Relationships"); //Selects Relationships from task panel
			Utils.fluentWait(driver);

			if (!ListView.isRelationshipsViewOpened(driver)) //Checks if Relationships view is opened
				throw new Exception("Relationships view is not opened.");

			Log.message("2. Relationships view of an object (" + dataPool.get("ObjectName") + ") is opened through task panel.");

			//Verification : To verify if back button in the Relationships view is available
			//------------------------------------------------------------------------
			if (homePage.listView.isBackToViewButtonExists())
				Log.pass("Test case Passed. Back button in Relationships view exists.");
			else
				Log.fail("Test case Failed. Back button in Relationships view does not exists.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_11_2_1A

	/**
	 * 25.11.2.1B : Back button should be available in Relationships view - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "History"}, 
			description = "Back button should be available in Relationships view - Context menu")
	public void SprintTest25_11_2_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open Relationships view of the object
			//----------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not not right clicked.");

			homePage.listView.clickContextMenuItem("Relationships"); //Selects Relationships from Context menu
			Utils.fluentWait(driver);

			if (!ListView.isRelationshipsViewOpened(driver)) //Checks if Relationships view is opened
				throw new Exception("Relationships view is not opened.");

			Log.message("2. Relationships view of an object (" + dataPool.get("ObjectName") + ") is opened through Context menu.");

			//Verification : To verify if back button in the Relationships view is available
			//------------------------------------------------------------------------
			if (homePage.listView.isBackToViewButtonExists())
				Log.pass("Test case Passed. Back button in Relationships view exists.");
			else
				Log.fail("Test case Failed. Back button in Relationships view does not exists.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_11_2_1B

	/**
	 * 25.11.2.1C : Back button should be available in Relationships view - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "History"}, 
			description = "Back button should be available in Relationships view - Operations menu")
	public void SprintTest25_11_2_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open Relationships view of the object
			//------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu("Relationships"); //Selects Relationships from Operations menu
			Utils.fluentWait(driver);

			if (!ListView.isRelationshipsViewOpened(driver)) //Checks if Relationships view is opened
				throw new Exception("Relationships view is not opened.");

			Log.message("2. Relationships view of an object (" + dataPool.get("ObjectName") + ") is opened through Operations menu.");

			//Verification : To verify if back button in the Relationships view is available
			//------------------------------------------------------------------------
			if (homePage.listView.isBackToViewButtonExists())
				Log.pass("Test case Passed. Back button in Relationships view exists.");
			else
				Log.fail("Test case Failed. Back button in Relationships view does not exists.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_11_2_1C

	/**
	 * 25.11.2.2A : Clicking back button in Relationships view should navigate to the previous view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "History"}, 
			description = "Clicking back button in Relationships view should navigate to the previous view")
	public void SprintTest25_11_2_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open Relationships view of the object 
			//-------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			String prevViewUrl = driver.getCurrentUrl();

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu("Relationships"); //Selects Relationships from task panel
			Utils.fluentWait(driver);

			if (!ListView.isRelationshipsViewOpened(driver)) //Checks if Relationships view is opened
				throw new Exception("Relationships view is not opened.");

			Log.message("2. Relationships view of an object (" + dataPool.get("ObjectName") + ") is opened through task panel.");

			//Step-3 : Click Back to view button in the Relationships view
			//------------------------------------------------------
			homePage.listView.clickBackToViewButton(); //Clicks Back to view button
			Utils.fluentWait(driver);
			String currViewUrl = driver.getCurrentUrl();

			Log.message("3. Back to view button is clicked in Relationships view.");

			//Verification : To verify if back button in the History view is available
			//------------------------------------------------------------------------
			if (currViewUrl.equals(prevViewUrl))
				Log.pass("Test case Passed. Clicking back to view button in Relationships view has navigated to previous view.");
			else
				Log.fail("Test case Failed. Clicking back to view button in Relationships view has not navigated to previous view.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_11_2_2A

	/**
	 * 25.11.2.2B : Clicking back button in Relationships view should navigate to the History view for latest version
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "History", "Relationships"}, 
			description = "Clicking back button in Relationships view should navigate to the History view for latest version")
	public void SprintTest25_11_2_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open History view of the object
			//----------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel
			Utils.fluentWait(driver);

			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened through task panel.");

			//Step-3 : Open Relationships view of the object 
			//-------------------------------------------
			if (homePage.listView.itemCount() <= 0)  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not have item in its History view.");

			String prevViewUrl = driver.getCurrentUrl();

			if (!homePage.listView.clickItemByIndex(0)) //Selects the Object in the list
				throw new Exception("Latest version of an object is not selected.");

			homePage.menuBar.ClickOperationsMenu("Relationships"); //Selects Relationships from task panel
			Utils.fluentWait(driver);

			if (!ListView.isRelationshipsViewOpened(driver)) //Checks if Relationships view is opened
				throw new Exception("Relationships view is not opened.");

			Log.message("2. Relationships view of an latest version of an object is opened through task panel.");

			//Step-3 : Click Back to view button in the Relationships view
			//------------------------------------------------------
			homePage.listView.clickBackToViewButton(); //Clicks Back to view button
			Utils.fluentWait(driver);
			String currViewUrl = driver.getCurrentUrl();

			Log.message("3. Back to view button is clicked in Relationships view.");

			//Verification : To verify if back button in the History view is available
			//------------------------------------------------------------------------
			if (currViewUrl.equals(prevViewUrl))
				Log.pass("Test case Passed. Clicking back to view button in Relationships view has navigated to History view.");
			else
				Log.fail("Test case Failed. Clicking back to view button in Relationships view has not navigated to History view.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_11_2_2B

	/**
	 * 25.11.2.2C : Clicking back button in Relationships view should navigate to the History view for older version
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "History", "Relationships"}, 
			description = "Clicking back button in Relationships view should navigate to the History view for older version")
	public void SprintTest25_11_2_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open History view of the object
			//----------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel
			Utils.fluentWait(driver);

			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened through task panel.");

			//Step-3 : Open Relationships view of the object 
			//-------------------------------------------
			if (homePage.listView.itemCount() <= 1)  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not older version in its History view.");

			String prevViewUrl = driver.getCurrentUrl();

			if (!homePage.listView.clickItemByIndex(1)) //Selects the Object in the list
				throw new Exception("Older version of an object is not selected.");

			homePage.menuBar.ClickOperationsMenu("Relationships"); //Selects Relationships from task panel
			Utils.fluentWait(driver);

			if (!ListView.isRelationshipsViewOpened(driver)) //Checks if Relationships view is opened
				throw new Exception("Relationships view is not opened.");

			Log.message("3. Relationships view for an older version of an object is opened through task panel.");

			//Step-3 : Click Back to view button in the Relationships view
			//------------------------------------------------------
			homePage.listView.clickBackToViewButton(); //Clicks Back to view button
			Utils.fluentWait(driver);
			String currViewUrl = driver.getCurrentUrl();

			Log.message("4. Back to view button is clicked in Relationships view.");

			//Verification : To verify if back button in the History view is available
			//------------------------------------------------------------------------
			if (currViewUrl.equals(prevViewUrl))
				Log.pass("Test case Passed. Clicking back to view button in Relationships view has navigated to History view.");
			else
				Log.fail("Test case Failed. Clicking back to view button in Relationships view has not navigated to History view.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_11_2_2C

	/**
	 * 25.11.2.2D : Clicking back button in Relationships view should navigate to the Relationships view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "History", "Relationships"}, 
			description = "Clicking back button in Relationships view should navigate to the Relationships view")
	public void SprintTest25_11_2_2D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open Relationships view of an object
			//---------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) 
				throw new SkipException("Invalid Test data. The given object " + dataPool.get("ObjectName") + " was not found in the view.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu("Relationships");
			Utils.fluentWait(driver);

			if (!ListView.isRelationshipsViewOpened(driver)) //Checks if History view is opened
				throw new Exception("Relationship view is not opened.");

			Log.message("2. Relationships view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open Relationships view of the object 
			//-------------------------------------------
			if (homePage.listView.itemCount() <= 0)  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not have item in its Relationships view.");

			String prevViewUrl = driver.getCurrentUrl();

			if (!homePage.listView.clickItemByIndex(0)) //Selects the Object in the list
				throw new Exception("Latest version of an object is not selected.");

			homePage.menuBar.ClickOperationsMenu("Relationships"); //Selects Relationships from task panel
			Utils.fluentWait(driver);

			if (!ListView.isRelationshipsViewOpened(driver)) //Checks if Relationships view is opened
				throw new Exception("Relationships view is not opened.");

			Log.message("2. Relationships view of an object is opened through task panel.");

			//Step-3 : Click Back to view button in the Relationships view
			//------------------------------------------------------
			homePage.listView.clickBackToViewButton(); //Clicks Back to view button
			Utils.fluentWait(driver);
			String currViewUrl = driver.getCurrentUrl();

			Log.message("3. Back to view button is clicked in Relationships view.");

			//Verification : To verify if back button in the History view is available
			//------------------------------------------------------------------------
			if (currViewUrl.equals(prevViewUrl))
				Log.pass("Test case Passed. Clicking back to view button in Relationships view has navigated to Relationships view.");
			else
				Log.fail("Test case Failed. Clicking back to view button in Relationships view has not navigated to Relationships view.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_11_2_2D

	/**
	 * 25.11.2.2E : Clicking back button in Relationships view should navigate to the SubObjects view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "Relationships", "SubObjects"}, 
			description = "Clicking back button in Relationships view should navigate to the SubObjects view")
	public void SprintTest25_11_2_2E(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open Sub-Objects view of an object
			//---------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) 
				throw new SkipException("Invalid Test data. The given object " + dataPool.get("ObjectName") + " was not found in the view.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.ShowSubObjects.Value);
			Utils.fluentWait(driver);

			if (!ListView.isSubObjectsViewOpened(driver)) //Checks if History view is opened
				throw new Exception("Show Subobjects view is not opened.");

			Log.message("2. Show Subobjects view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open Relationships view of the object 
			//-------------------------------------------
			if (homePage.listView.itemCount() <= 0)  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not have item in its SubObjects view.");

			String prevViewUrl = driver.getCurrentUrl();

			if (!homePage.listView.clickItemByIndex(0)) //Selects the Object in the list
				throw new Exception("Latest version of an object is not selected.");

			homePage.menuBar.ClickOperationsMenu("Relationships"); //Selects Relationships from task panel
			Utils.fluentWait(driver);

			if (!ListView.isRelationshipsViewOpened(driver)) //Checks if Relationships view is opened
				throw new Exception("Relationships view is not opened.");

			Log.message("2. Relationships view of an object is opened through task panel.");

			//Step-3 : Click Back to view button in the Relationships view
			//------------------------------------------------------
			homePage.listView.clickBackToViewButton(); //Clicks Back to view button
			Utils.fluentWait(driver);
			String currViewUrl = driver.getCurrentUrl();

			Log.message("3. Back to view button is clicked in Relationships view.");

			//Verification : To verify if back button in the History view is available
			//------------------------------------------------------------------------
			if (currViewUrl.equals(prevViewUrl))
				Log.pass("Test case Passed. Clicking back to view button in Relationships view has navigated to SubObjects view.");
			else
				Log.fail("Test case Failed. Clicking back to view button in Relationships view has not navigated to SubObjects view.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_11_2_2E

	/**
	 * 25.11.2.2F : Clicking back button in Relationships view should navigate to the Members view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "Relationships", "Members"}, 
			description = "Clicking back button in Relationships view should navigate to the Members view")
	public void SprintTest25_11_2_2F(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open Members view of an object
			//---------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) 
				throw new SkipException("Invalid Test data. The given object " + dataPool.get("ObjectName") + " was not found in the view.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem("Show Members");
			Utils.fluentWait(driver);

			if (!ListView.isMembersViewOpened(driver)) //Checks if Members view is opened
				throw new Exception("Show Members view is not opened.");

			Log.message("2. Members view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open Relationships view of the object 
			//-------------------------------------------
			if (homePage.listView.itemCount() <= 0)  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not have item in its Members view.");

			String prevViewUrl = driver.getCurrentUrl();

			if (!homePage.listView.clickItemByIndex(0)) //Selects the Object in the list
				throw new Exception("Latest version of an object is not selected.");

			homePage.menuBar.ClickOperationsMenu("Relationships"); //Selects Relationships from task panel
			Utils.fluentWait(driver);

			if (!ListView.isRelationshipsViewOpened(driver)) //Checks if Relationships view is opened
				throw new Exception("Relationships view is not opened.");

			Log.message("3. Relationships view of an object is opened through task panel.");

			//Step-3 : Click Back to view button in the Relationships view
			//------------------------------------------------------
			homePage.listView.clickBackToViewButton(); //Clicks Back to view button
			Utils.fluentWait(driver);
			String currViewUrl = driver.getCurrentUrl();

			Log.message("3. Back to view button is clicked in Relationships view.");

			//Verification : To verify if back button in the History view is available
			//------------------------------------------------------------------------
			if (currViewUrl.equals(prevViewUrl))
				Log.pass("Test case Passed. Clicking back to view button in Relationships view has navigated to Members view.");
			else
				Log.fail("Test case Failed. Clicking back to view button in Relationships view has not navigated to Members view.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_11_2_2F

	/**
	 * 25.11.3.1A : Back button should be available in Subobjects view - Taskpane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "History"}, 
			description = "Back button should be available in Subobjects view - Taskpane")
	public void SprintTest25_11_3_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open Subobjects view of the object
			//------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.ShowSubObjects.Value); //Selects Subobjects from task panel
			Utils.fluentWait(driver);

			if (!ListView.isSubObjectsViewOpened(driver)) //Checks if Subobjects view is opened
				throw new Exception("Subobjects view is not opened.");

			Log.message("2. Subobjects view of an object (" + dataPool.get("ObjectName") + ") is opened through task panel.");


			//Verification : To verify if back button in the Relationships view is available
			//------------------------------------------------------------------------
			if (homePage.listView.isBackToViewButtonExists())
				Log.pass("Test case Passed. Back button in Subobjects view exists.");
			else
				Log.fail("Test case Failed. Back button in Subobjects view does not exists.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_11_3_1A

	/**
	 * 25.11.3.2A : Clicking back button in Subobjects view should navigate to the previous view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "Subobjects"}, 
			description = "Clicking back button in Subobjects view should navigate to the previous view")
	public void SprintTest25_11_3_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open Subobjects view of the object
			//------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			String prevViewUrl = driver.getCurrentUrl();

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.ShowSubObjects.Value); //Selects Subobjects from task panel
			Utils.fluentWait(driver);

			if (!ListView.isSubObjectsViewOpened(driver)) //Checks if Subobjects view is opened
				throw new Exception("Subobjects view is not opened.");

			Log.message("2. Subobjects view of an object (" + dataPool.get("ObjectName") + ") is opened through task panel.");

			//Step-3 : Click Back to view button in the Subobjects view
			//------------------------------------------------------
			homePage.listView.clickBackToViewButton(); //Clicks Back to view button
			Utils.fluentWait(driver);
			String currViewUrl = driver.getCurrentUrl();

			Log.message("3. Back to view button is clicked in Subobjects view.");

			//Verification : To verify if back button in the History view is available
			//------------------------------------------------------------------------
			if (currViewUrl.equals(prevViewUrl))
				Log.pass("Test case Passed. Clicking back to view button in Subobjects view has navigated to previous view.");
			else
				Log.fail("Test case Failed. Clicking back to view button in Subobjects view has not navigated to previous view.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_11_3_2A

	/**
	 * 25.11.3.2B : Clicking back button in Subobjects view should navigate to the History view for latest version
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "Subobjects"}, 
			description = "Clicking back button in Subobjects view should navigate to the History view for latest version")
	public void SprintTest25_11_3_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open History view of the object
			//----------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel
			Utils.fluentWait(driver);

			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened through task panel.");

			//Step-2 : Open Subobjects view of the object
			//------------------------------------------------
			if (homePage.listView.itemCount() <= 0)  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not have item in its History view.");

			String prevViewUrl = driver.getCurrentUrl();

			if (!homePage.listView.clickItemByIndex(0)) //Selects the Object in the list
				throw new Exception("Latest version of an object is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.ShowSubObjects.Value); //Selects Subobjects from task panel
			Utils.fluentWait(driver);

			if (!ListView.isSubObjectsViewOpened(driver)) //Checks if Subobjects view is opened
				throw new Exception("Subobjects view is not opened.");

			Log.message("2. Subobjects view of an object (" + dataPool.get("ObjectName") + ") is opened through task panel.");

			//Step-3 : Click Back to view button in the Subobjects view
			//------------------------------------------------------
			homePage.listView.clickBackToViewButton(); //Clicks Back to view button
			Utils.fluentWait(driver);
			String currViewUrl = driver.getCurrentUrl();

			Log.message("3. Back to view button is clicked in Subobjects view.");

			//Verification : To verify if back button in the History view is available
			//------------------------------------------------------------------------
			if (currViewUrl.equals(prevViewUrl))
				Log.pass("Test case Passed. Clicking back to view button in Subobjects view has navigated to History view.");
			else
				Log.fail("Test case Failed. Clicking back to view button in Subobjects view has not navigated to History view.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_11_3_2B

	/**
	 * 25.11.3.2C : Clicking back button in Subobjects view should navigate to the History view for Older version
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "Subobjects"}, 
			description = "Clicking back button in Subobjects view should navigate to the History view for Older version")
	public void SprintTest25_11_3_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open History view of the object
			//----------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel
			Utils.fluentWait(driver);

			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened through task panel.");

			//Step-2 : Open Subobjects view of the object
			//------------------------------------------------
			if (homePage.listView.itemCount() <= 1)  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not have older versions.");

			String prevViewUrl = driver.getCurrentUrl();

			if (!homePage.listView.clickItemByIndex(1)) //Selects the Object in the list
				throw new Exception("Older version of an object is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.ShowSubObjects.Value); //Selects Subobjects from task panel
			Utils.fluentWait(driver);

			if (!ListView.isSubObjectsViewOpened(driver)) //Checks if Subobjects view is opened
				throw new Exception("Subobjects view is not opened.");

			Log.message("2. Subobjects view of an object (" + dataPool.get("ObjectName") + ") is opened through task panel.");

			//Step-3 : Click Back to view button in the Subobjects view
			//------------------------------------------------------
			homePage.listView.clickBackToViewButton(); //Clicks Back to view button
			Utils.fluentWait(driver);
			String currViewUrl = driver.getCurrentUrl();

			Log.message("3. Back to view button is clicked in Subobjects view.");

			//Verification : To verify if back button in the History view is available
			//------------------------------------------------------------------------
			if (currViewUrl.equals(prevViewUrl))
				Log.pass("Test case Passed. Clicking back to view button in Subobjects view has navigated to History view.");
			else
				Log.fail("Test case Failed. Clicking back to view button in Subobjects view has not navigated to History view.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_11_3_2C

	/**
	 * 25.11.3.2D : Clicking back button in Subobjects view should navigate to the Relationships view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "Subobjects"}, 
			description = "Clicking back button in Subobjects view should navigate to the Relationships view")
	public void SprintTest25_11_3_2D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open Relationships view of an object
			//---------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) 
				throw new SkipException("Invalid Test data. The given object " + dataPool.get("ObjectName") + " was not found in the view.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu("Relationships");
			Utils.fluentWait(driver);

			if (!ListView.isRelationshipsViewOpened(driver)) //Checks if History view is opened
				throw new Exception("Relationship view is not opened.");

			Log.message("2. Relationships view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open Subobjects view of the object 
			//-------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("RelatedObject")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("RelatedObject") + ") is not in relationships view.");

			String prevViewUrl = driver.getCurrentUrl();

			if (!homePage.listView.clickItem(dataPool.get("RelatedObject"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("RelatedObject") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.ShowSubObjects.Value); //Selects Subobjects from task panel
			Utils.fluentWait(driver);

			if (!ListView.isSubObjectsViewOpened(driver)) //Checks if Subobjects view is opened
				throw new Exception("Subobjects view is not opened.");

			Log.message("2. Subobjects view of an object (" + dataPool.get("RelatedObject") + ") is opened through task panel.");

			//Step-3 : Click Back to view button in the Subobjects view
			//------------------------------------------------------
			homePage.listView.clickBackToViewButton(); //Clicks Back to view button
			Utils.fluentWait(driver);
			String currViewUrl = driver.getCurrentUrl();

			Log.message("3. Back to view button is clicked in Subobjects view.");

			//Verification : To verify if back button in the History view is available
			//------------------------------------------------------------------------
			if (currViewUrl.equals(prevViewUrl))
				Log.pass("Test case Passed. Clicking back to view button in Subobjects view has navigated to History view.");
			else
				Log.fail("Test case Failed. Clicking back to view button in Subobjects view has not navigated to History view.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_11_3_2D

	/**
	 * 25.11.4.1A : Back button should be available in Members view - Taskpanel
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "Members"}, 
			description = "Back button should be available in Members view - Taskpane")
	public void SprintTest25_11_4_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open Members view of the object
			//------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("Show Members"); //Selects Subobjects from task panel
			Utils.fluentWait(driver);

			if (!ListView.isMembersViewOpened(driver)) //Checks if Subobjects view is opened
				throw new Exception("Members view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. Members view of an object (" + dataPool.get("ObjectName") + ") is opened through task panel.");

			//Verification : To verify if back button in the Relationships view is available
			//------------------------------------------------------------------------
			if (homePage.listView.isBackToViewButtonExists())
				Log.pass("Test case Passed. Back button in Members view exists.");
			else
				Log.fail("Test case Failed. Back button in Members view does not exists.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_11_4_1A

	/**
	 * 25.11.4.2A : Clicking back button in Members view should navigate to the previous view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "Members"}, 
			description = "Clicking back button in Members view should navigate to the previous view")
	public void SprintTest25_11_4_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open Members view of the object
			//------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			String prevViewUrl = driver.getCurrentUrl();

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("Show Members"); //Selects Subobjects from task panel
			Utils.fluentWait(driver);

			if (!ListView.isMembersViewOpened(driver)) //Checks if Subobjects view is opened
				throw new Exception("Members view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. Members view of an object (" + dataPool.get("ObjectName") + ") is opened through task panel.");

			//Step-3 : Click Back to view button in the Members view
			//------------------------------------------------------
			homePage.listView.clickBackToViewButton(); //Clicks Back to view button
			Utils.fluentWait(driver);
			String currViewUrl = driver.getCurrentUrl();

			Log.message("3. Back to view button is clicked in Members view.");

			//Verification : To verify if back button in the History view is available
			//------------------------------------------------------------------------
			if (currViewUrl.equals(prevViewUrl))
				Log.pass("Test case Passed. Clicking back to view button in Members view has navigated to previous view.");
			else
				Log.fail("Test case Failed. Clicking back to view button in Members view has not navigated to previous view.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_11_4_2A

	/**
	 * 25.11.4.2B : Clicking back button in Members view should navigate to the History view for latest version
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "Subobjects"}, 
			description = "Clicking back button in Members view should navigate to the History view for latest version")
	public void SprintTest25_11_4_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open History view of the object
			//----------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel
			Utils.fluentWait(driver);

			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened through task panel.");

			//Step-3 : Open Members view of the object
			//------------------------------------------------
			if (homePage.listView.itemCount() <= 0)  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not have item in its History view.");

			String prevViewUrl = driver.getCurrentUrl();

			if (!homePage.listView.clickItemByIndex(0)) //Selects the Object in the list
				throw new Exception("Latest version of an object is not selected.");

			homePage.taskPanel.clickItem("Show Members"); //Selects Subobjects from task panel
			Utils.fluentWait(driver);

			if (!ListView.isMembersViewOpened(driver)) //Checks if Subobjects view is opened
				throw new Exception("Members view for latest version of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("3. Members view for latest version of an object (" + dataPool.get("ObjectName") + ") is opened through task panel.");

			//Step-4 : Click Back to view button in the Members view
			//------------------------------------------------------
			homePage.listView.clickBackToViewButton(); //Clicks Back to view button
			Utils.fluentWait(driver);
			String currViewUrl = driver.getCurrentUrl();

			Log.message("4. Back to view button is clicked in Members view.");

			//Verification : To verify if back button in the History view is available
			//------------------------------------------------------------------------
			if (currViewUrl.equals(prevViewUrl))
				Log.pass("Test case Passed. Clicking back to view button in Members view has navigated to History view.");
			else
				Log.fail("Test case Failed. Clicking back to view button in Members view has not navigated to History view.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_11_4_2B

	/**
	 * 25.11.4.2C : Clicking back button in Members view should navigate to the History view for older version
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "Subobjects"}, 
			description = "Clicking back button in Members view should navigate to the History view for older version")
	public void SprintTest25_11_4_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open History view of the object
			//----------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel
			Utils.fluentWait(driver);

			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened through task panel.");

			//Step-3 : Open Members view of the object
			//------------------------------------------------
			if (homePage.listView.itemCount() <= 1)  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not have older version in its History view.");

			String prevViewUrl = driver.getCurrentUrl();

			if (!homePage.listView.clickItemByIndex(1)) //Selects the Object in the list
				throw new Exception("Older version of an object is not selected.");

			homePage.taskPanel.clickItem("Show Members"); //Selects Subobjects from task panel
			Utils.fluentWait(driver);

			if (!ListView.isMembersViewOpened(driver)) //Checks if Subobjects view is opened
				throw new Exception("Members view for latest version of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("3. Members view for older version of an object (" + dataPool.get("ObjectName") + ") is opened through task panel.");

			//Step-4 : Click Back to view button in the Members view
			//------------------------------------------------------
			homePage.listView.clickBackToViewButton(); //Clicks Back to view button
			Utils.fluentWait(driver);
			String currViewUrl = driver.getCurrentUrl();

			Log.message("4. Back to view button is clicked in Members view.");

			//Verification : To verify if back button in the History view is available
			//------------------------------------------------------------------------
			if (currViewUrl.equals(prevViewUrl))
				Log.pass("Test case Passed. Clicking back to view button in Members view has navigated to History view.");
			else
				Log.fail("Test case Failed. Clicking back to view button in Members view has not navigated to History view.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_11_4_2C

	/**
	 * 25.11.4.2D : Clicking back button in Members view should navigate to the Relationships view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "Subobjects"}, 
			description = "Clicking back button in Members view should navigate to the Relationships view")
	public void SprintTest25_11_4_2D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open Relationships view of an object
			//---------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) 
				throw new SkipException("Invalid Test data. The given object " + dataPool.get("ObjectName") + " was not found in the view.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu("Relationships");
			Utils.fluentWait(driver);

			if (!ListView.isRelationshipsViewOpened(driver)) //Checks if History view is opened
				throw new Exception("Relationship view is not opened.");

			Log.message("2. Relationships view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Open Members view of the object
			//------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("RelatedObject")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("RelatedObject") + ") is not in relationships view.");

			String prevViewUrl = driver.getCurrentUrl();

			if (!homePage.listView.clickItem(dataPool.get("RelatedObject"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("RelatedObject") + ") is not selected.");

			homePage.taskPanel.clickItem("Show Members"); //Selects Subobjects from task panel
			Utils.fluentWait(driver);

			if (!ListView.isMembersViewOpened(driver)) //Checks if Subobjects view is opened
				throw new Exception("Members view for latest version of an object (" + dataPool.get("RelatedObject") + ") is not opened.");

			Log.message("3. Members view of an object (" + dataPool.get("RelatedObject") + ") is opened through task panel.");

			//Step-4 : Click Back to view button in the Members view
			//------------------------------------------------------
			homePage.listView.clickBackToViewButton(); //Clicks Back to view button
			Utils.fluentWait(driver);
			String currViewUrl = driver.getCurrentUrl();

			Log.message("4. Back to view button is clicked in Members view.");

			//Verification : To verify if back button in the Members view is available
			//------------------------------------------------------------------------
			if (currViewUrl.equals(prevViewUrl))
				Log.pass("Test case Passed. Clicking back to view button in Members view has navigated to Relationships view.");
			else
				Log.fail("Test case Failed. Clicking back to view button in Members view has not navigated to Relationships view.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_11_4_2D

}

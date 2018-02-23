package MFClient.Tests;

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

import MFClient.Pages.HomePage;
import MFClient.Pages.LoginPage;
import MFClient.Wrappers.Caption;
import MFClient.Wrappers.ListView;
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class History {

	public String xlTestDataWorkBook = null;
	public String loginURL = null;
	public String userName = null;
	public String password = null;
	public String testVault = null;
	public String className = null;
	public String productVersion = null;
	public WebDriver driver = null;

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
	 * 7.6.1 : 'Delete the recent version in History' - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","Sprint7", "History", "Smoke"}, 
			description = "'Delete the recent version in History' - Context menu")
	public void SprintTest7_6_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Open document history view
			//----------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object"))) 
				throw new SkipException("Invalid Test Data. The specified Object " + dataPool.get("Object") + " was not found in the vault.");

			homePage.listView.rightClickItem(dataPool.get("Object")); //Right click the object

			homePage.listView.clickContextMenuItem("History");


			Log.message("2. History view of an object is opened.");

			//Step-3: Right click on the latest version of the object
			//--------------------------------------------------------
			if (!homePage.listView.rightClickItemByIndex(0)) //click the object
				throw new Exception("Latest version of an object is not right clicked.");

			Log.message("3. Right clicked on the latest version of the object.");

			//Verification: To Verify if the 'Delete' context menu is in disabled state
			//--------------------------------------------------------------------------
			if(!homePage.listView.itemEnabledInContextMenu("Delete")) 
				Log.pass("Test Case Passed. The Delete context menu was in disabled state as expected.");
			else
				Log.fail("Test Case Failed. The Delete context menu was not in disabled state.", driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest7_6_1

	/**
	 * 7.6.2 : Delete the recent version in History - Operations menu 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","Sprint7", "History"}, 
			description = "'Delete the recent version in History - Operations menu")
	public void SprintTest7_6_2(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Open it's history view
			//-------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object"))) 
				throw new SkipException("Invalid Test Data. The specified Object " + dataPool.get("Object") + " was not found in the favorites view.");

			homePage.listView.rightClickItem(dataPool.get("Object")); //Right click the object

			homePage.listView.clickContextMenuItem("History");


			Log.message("2. History view of an object is opened.");

			//Step-3: Click on the latest version of the object
			//--------------------------------------------------
			if (!homePage.listView.clickItemByIndex(0)) //click the object
				throw new Exception("Latest version of an object is not selected.");



			Log.message("3. click on the latest version of the object");

			//Verification: To Verify if the 'Delete' context menu is in disabled state
			//--------------------------------------------------------------------------
			if(!homePage.menuBar.IsItemEnabledInOperationsMenu("Delete")) 
				Log.pass("Test Case Passed. The Delete context menu was in disabled state as expected.");
			else
				Log.fail("Test Case Failed. The Delete context menu was not in disabled state.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest7_6_2

	/**
	 * 7.6.3 : Delete the recent version in History. Try using 'Task Pane' 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","Sprint7", "History"}, 
			description = "Delete the recent version in History. Try using 'Task Pane' ")
	public void SprintTest7_6_3(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Open it's history view
			//-------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object"))) 
				throw new SkipException("Invalid Test Data. The specified Object " + dataPool.get("Object") + " was not found in the vault.");

			homePage.listView.rightClickItem(dataPool.get("Object")); //Right click the object

			homePage.listView.clickContextMenuItem("History");


			Log.message("2. History view of an object is opened.");

			//Step-3: click on the latest version of the object
			//--------------------------------------------------
			if (!homePage.listView.clickItemByIndex(0)) //Selects the latest version of the object 
				throw new Exception("Latest version of an object (" + dataPool.get("ObjectName") + ") is not selected.");

			Log.message("3. click on the latest version of the object");

			//Verification: To Verify if the 'Delete' context menu is in disabled state
			//--------------------------------------------------------------------------
			if(!homePage.taskPanel.isItemExists("Delete")) 
				Log.pass("Test Case Passed. The Delete context menu was in disabled state as expected.");
			else
				Log.fail("Test Case Failed. The Delete context menu was not in disabled state.", driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest7_6_3

	/**
	 * 7.6.4 : 'Delete the old version in History.Try using Right click
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","Sprint7", "History"}, 
			description = "'Delete the old version in History.Try using Right click")
	public void SprintTest7_6_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Open it's history view
			//-------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object"))) 
				throw new SkipException("Invalid Test Data. The specified Object " + dataPool.get("Object") + " was not found in the vault.");

			homePage.listView.rightClickItem(dataPool.get("Object")); //Right click the object

			homePage.listView.clickContextMenuItem("History");


			Log.message("2. History view of an object is opened.");

			//Step-3: Right click on the old version of the object
			//--------------------------------------------------------
			if (homePage.listView.itemCount() <= 1) 
				throw new SkipException("Invalid test data. Object (" + dataPool.get("Object") + ") does not have older versions.");

			if(!homePage.listView.rightClickItemByIndex(1)) //Selects the older version of the object
				throw new Exception("Older version of an object is not selected.");

			Log.message("3. Right click on the old version of the object.");

			//Verification: To Verify if the 'Delete' context menu is in disabled state
			//--------------------------------------------------------------------------
			if(!homePage.listView.itemEnabledInContextMenu("Delete")) 
				Log.pass("Test Case Passed. The Delete context menu was in disabled state as expected.");
			else
				Log.fail("Test Case Failed. The Delete context menu was not in disabled state.", driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest7_6_4

	/**
	 * 7.6.5 : Delete the old version in History. Try using 'Operations' 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","Sprint7", "History"}, 
			description = "'Delete the old version in History. Try using 'Operations' ")
	public void SprintTest7_6_5(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Open it's history view
			//-------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object"))) 
				throw new SkipException("Invalid Test Data. The specified Object " + dataPool.get("Object") + " was not found in the vault.");

			homePage.listView.rightClickItem(dataPool.get("Object")); //Right click the object

			homePage.listView.clickContextMenuItem("History");


			Log.message("2. History view of an object is opened.");

			//Step-3: click on the old version of the object
			//--------------------------------------------------
			if(homePage.listView.itemCount() < 2)
				throw new SkipException("Invalid Test Data. Object " + dataPool.get("Object") + " does not has lower versions.");

			if (!homePage.listView.clickItemByIndex(0)) //click the object
				throw new Exception("Older version of an object is not right clicked.");

			Log.message("3. click on the old version of the object");

			//Verification: To Verify if the 'Delete' context menu is in disabled state
			//--------------------------------------------------------------------------
			if(!homePage.menuBar.IsItemEnabledInOperationsMenu("Delete")) 
				Log.pass("Test Case Passed. The Delete context menu was in disabled state as expected.");
			else
				Log.fail("Test Case Failed. The Delete context menu was not in disabled state.", driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest7_6_5

	/**
	 * 7.6.6 : Delete the old version in History. Try using 'Task Pane' 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","Sprint7", "History"}, 
			description = "Delete the old version in History. Try using 'Task Pane' ")
	public void SprintTest7_6_6(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Open it's history view
			//-------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object"))) 
				throw new SkipException("Invalid Test Data. The specified Object " + dataPool.get("Object") + " was not found in the vault.");

			homePage.listView.rightClickItem(dataPool.get("Object")); //Right click the object

			homePage.listView.clickContextMenuItem("History");


			Log.message("2. History view of an object is opened.");

			//Step-3: click on the old version of the object
			//--------------------------------------------------
			if(homePage.listView.itemCount() < 2)
				throw new SkipException("Invalid Test Data. Object " + dataPool.get("Object") + " does not has lower versions.");

			if (!homePage.listView.clickItemByIndex(0)) //click the object
				throw new Exception("Older version of an object is not right clicked.");



			Log.message("3. click on the old version of the object");

			//Verification: To Verify if the 'Delete' context menu is in disabled state
			//--------------------------------------------------------------------------
			if(!homePage.taskPanel.isItemExists("Delete")) 
				Log.pass("Test Case Passed. The Delete context menu was in disabled state as expected.");
			else
				Log.fail("Test Case Failed. The Delete context menu was not in disabled state.", driver);

		} //End SprintTest7_6_6

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest7_6_6

	/**
	 * 8.5.1B : Checkout should not be visible in task pane for older version
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","Sprint8", "History", "Smoke"}, 
			description = "Checkout should not be visible in task pane for older version")
	public void SprintTest8_5_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open History dialog of the object
			//------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))//Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel


			if (!ListView.isHistoryViewOpened(driver)) 
				throw new Exception("History view is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Select the older version of the object
			//-----------------------------------------------
			if (homePage.listView.itemCount() <= 1) 
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not have older versions.");

			if(!homePage.listView.clickItemByIndex(1)) //Selects the older version of the object
				throw new Exception("Older version of an object is not selected.");

			Log.message("3. Older version of an object is selected.");

			//Verification : To Verify Check out is not visible in Task Pane
			//----------------------------------------------------------------
			if (!homePage.taskPanel.isItemExists(Caption.MenuItems.CheckOut.Value))
				Log.pass("Test case Passed. Check Out option is not visible in task pane for older version of the object.");
			else
				Log.fail("Test case Failed. Check Out option is visible in task pane for older version of the object.", driver);
		}

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest8_5_1B

	/**
	 * 8.5.1C : Checkout the latest version of the document in History view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","Bug","Sprint8", "History"}, 
			description = "Checkout the latest version of the document in History view")
	public void SprintTest8_5_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open History dialog of the object
			//------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel


			if (!ListView.isHistoryViewOpened(driver)) 
				throw new Exception("History view is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Select latest version of a document and select checkout from taskpane
			//-------------------------------------------------------------------------------
			if (!homePage.listView.clickItemByIndex(0)) 
				throw new Exception("Latest version of an object is not selected.");

			if(!homePage.taskPanel.isItemExists(Caption.MenuItems.CheckOut.Value))
				Log.fail("Test Case Failed. The Check Out option was not displayed when the latest version is selected.", driver);

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Checks out from Task Pane


			Log.message("3. Latest version of a document is selected and check out is selected from taskpane.");

			//Verification : To Verify latest version of object is checked out
			//----------------------------------------------------------------
			if (ListView.isCheckedOutByItemIndex(driver, 0)) //Verifies that Check out column is not empty after checking out
				Log.pass("Test case Passed. Latest version of an object is checked out successfully.");
			else
				Log.fail("Test case Failed. Latest version of an object is not checked out.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest8_5_1C

	/**
	 * 8.5.1D : Checkout the latest version should increase the number of version in History view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","Bug","Sprint8", "History"}, 
			description = "Checkout the latest version should increase the number of version in History view")
	public void SprintTest8_5_1D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open History dialog of the object
			//------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))//Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel


			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Check out the latest version of an object
			//--------------------------------------------------
			if (ListView.isCheckedOutByItemIndex(driver, 0)) //Checks if latest version is checked out not
				throw new Exception("Latest version of an object is already checked out.");

			int prevObjCt = homePage.listView.itemCount(); //Gets the number of objects displayed in History view
			int prevVersionCt = Integer.parseInt(homePage.listView.getColumnValueByItemIndex(0, "Version")); //Gets the version count of the latest version

			if (!homePage.listView.clickItemByIndex(0)) //Selects the latest version of the object
				throw new Exception("Latest version of an object is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Checks out from Task Pane


			if (!ListView.isCheckedOutByItemIndex(driver, 0)) //Checks if latest version is checked out not
				throw new Exception("Latest version of an object is not checked out.");

			Log.message("3. Latest version of a document is checked out.");

			//Verification : To Verify latest version of object is checked out
			//----------------------------------------------------------------
			int currObjCt = homePage.listView.itemCount(); //Gets the number of objects displayed in History view
			int currVersionCt = Integer.parseInt(homePage.listView.getColumnValueByItemIndex(0, "Version")); //Gets the version count of the latest version

			//Verifies if number of items in the list has increased by 1 after checking out
			if (currObjCt != prevObjCt + 1) {
				Log.fail("Test case Failed. Number of objects listed in History view has not increased by 1 after checking out.", driver);
				return;
			}

			//Verifies that Check out column is not empty after checking out
			if (currVersionCt == prevVersionCt + 1)
				Log.pass("Test case Passed. Latest version has increased by 1 (newer version) after checking out.");
			else
				Log.fail("Test case Failed. Latest version has not increased by 1 after checking out.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest8_5_1D

	/**
	 * 8.5.2B : Undo-Check out should not be available for older versions of the checked out document
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","Bug","Sprint8", "History"}, 
			description = "Undo-Check out should not be available for older versions of the checked out document")
	public void SprintTest8_5_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open History dialog of the object
			//------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel


			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Check out the latest version of an object
			//--------------------------------------------------
			if (!homePage.listView.clickItemByIndex(0)) //Selects the latest version of the object
				throw new Exception("Latest version of an object is not selected.");

			if (!ListView.isCheckedOutByItemIndex(driver, 0)) { //Checks out object if not checked out

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Checks out from Task Pane

				if (!ListView.isCheckedOutByItemIndex(driver, 0)) 
					throw new Exception("Latest version of an object is not checked out.");

				Log.message("3. Latest version of a object is checked out.");
			}
			else 
				Log.message("3. Latest version of a object is already checked out.");

			//Step-4 : Select the older version of the object
			//-----------------------------------------------
			homePage.listView.clickItemByIndex(1); //Selects the older version of object

			Log.message("4. Older version of an object is selected");

			//Verification : To Verify older version of an object does not have undo-checkout option
			//---------------------------------------------------------------------------------------

			//Verifies that task pane has undo checkout option
			if (!homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.UndoCheckOut.Value))
				Log.pass("Test case Passed. Undo-checkout option is not available in task panel for older version.");
			else
				Log.fail("Test case Failed. Undo-checkout option is available in task panel for older version.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest8_5_2B

	/**
	 * 8.5.2C : Performing Undo-Check out for latest version should not increase the version of the document
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","Bug","Sprint8", "History", "Smoke"}, 
			description = "Performing Undo-Check out for latest version should not increase the version of the document")
	public void SprintTest8_5_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open History dialog of the object
			//------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel


			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Check out the latest version of an object
			//--------------------------------------------------
			if (!homePage.listView.clickItemByIndex(0)) //Selects the latest version of the object
				throw new Exception("Latest version of an object is not selected.");

			if (!ListView.isCheckedOutByItemIndex(driver, 0)) { //Checks out object if not checked out

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Checks out from Task Pane

				if (!ListView.isCheckedOutByItemIndex(driver, 0)) 
					throw new Exception("Latest version of an object is not checked out.");
			}

			int prevObjCt = homePage.listView.itemCount(); //Gets the number of objects displayed in History view
			int prevVersionCt = Integer.parseInt(homePage.listView.getColumnValueByItemIndex(0, "Version")); //Gets the version count of the latest version

			Log.message("3. Latest version of a document is checked out.");

			//Step-4 : Undo-Checkout the object
			//---------------------------------
			if (!homePage.listView.clickItemByIndex(0)) //Selects the latest version of the object
				throw new Exception("Latest version of an object is not selected.");

			if(!homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.UndoCheckOut.Value))
				Log.fail("Test Case Failed. The Undo Checkout option was not displayed when the latest version is selected.", driver);

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Undo Checkout from Operation menu

			MFilesDialog  mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo checkout dialog



			//Verification : To Verify undo-Checkout is successful
			//----------------------------------------------------
			int currObjCt = homePage.listView.itemCount(); //Gets the number of objects displayed in History view
			int currVersionCt = Integer.parseInt(homePage.listView.getColumnValueByItemIndex(0, "Version")); //Gets the version count of the latest version

			//Verifies that number of items in history view has not increased in History view
			if (currObjCt != prevObjCt - 1) {
				Log.fail("Test case Failed. Number of versions listed in History view is not restored after performing undo check out.", driver);
				return;
			}

			//Verifies that new version is not created
			if (currVersionCt == prevVersionCt - 1)
				Log.pass("Test case Passed. Version of a object is restored after performing undo-checkout.");
			else
				Log.fail("Test case Failed. Version of a object is not restored after performing undo-checkout.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest8_5_5

	/**
	 * 8.5.3B : Rename should not be enabled in Operations menuBar for older version
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","Sprint8", "History"}, 
			description = "Rename should not be enabled in Operations menuBar for older version")
	public void SprintTest8_5_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open History dialog of the object and select older version
			//-------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) { //Selects the Object in the list
				Log.fail("Object (" + dataPool.get("ObjectName") + ") is not got selected.", driver);
				return;
			}

			homePage.taskPanel.clickItem("History"); //Selects History from task panel

			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Select the older version of the object
			//-----------------------------------------------
			if (homePage.listView.itemCount() <= 1) 
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not have older versions.");

			homePage.listView.clickItemByIndex(1); //Selects the older version of the object

			if (!homePage.listView.isItemSelectedByIndex(1)) //Checks if older version of the object is selected
				throw new Exception("Latest version of an object is not selected.");

			Log.message("3. Older version of an object is selected.");

			//Verification : To Verify Rename is not enabled in operations menuBar
			//------------------------------------------------------------------
			if (!homePage.menuBar.IsItemEnabledInOperationsMenu("Rename")) //Verifies that Rename is enabled for older version of objects
				Log.pass("Test case Passed. Rename is not enabled for older version of an object.");
			else
				Log.fail("Test case Failed. Rename is enabled for older version of an object.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest8_5_3B

	/**
	 * 8.5.3C : Rename latest version of a document in History view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","Sprint8", "History"}, 
			description = "Rename latest version of a document in History view")
	public void SprintTest8_5_3C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open History dialog of the object and select latest version
			//--------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");


			homePage.taskPanel.clickItem("History"); //Selects History from task panel


			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened and latest version is selected.");

			//Step-3 : Select Latest version of an object and enter new name to the latest version of an object
			//-------------------------------------------------------------------------------------------------
			int prevObjCt = homePage.listView.itemCount(); //Gets the number of objects displayed in History view
			int prevVersionCt = Integer.parseInt(homePage.listView.getColumnValueByItemIndex(0, "Version")); //Gets the version count of the latest version

			if (!homePage.listView.clickItemByIndex(0)) //Selects the latest version of the object
				throw new Exception("Latest version of an object is not selected.");

			String extension = "";

			if(homePage.listView.getSelectedListViewItem().contains("."))
				extension = "." + dataPool.get("ObjectName").split("\\.")[1];

			homePage.menuBar.ClickOperationsMenu("Rename"); //Selects Rename from operations menuBar

			String newName = "SprintTest8_5_3C_" + Utility.getCurrentDateTime(); 
			MFilesDialog  mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.rename(newName, true); //Enters new name to the latest version of the object


			Log.message("3. Latest version of an object is selected and new name is entered");

			//Verification : To Verify Rename is done successfully recent version of an object
			//--------------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(newName+extension)) { //Verifies if name of an object is renamed
				Log.fail("Test case Failed. Renamed object " + newName + " does not exists in History view.", driver);
				return;
			}

			int currObjCt = homePage.listView.itemCount(); //Gets the number of objects displayed in History view
			int currVersionCt = Integer.parseInt(homePage.listView.getColumnValueByItemIndex(0, "Version")); //Gets the version count of the latest version

			//Verifies that number of items in History view has increased by 1 in History view
			if (currObjCt != prevObjCt + 1) {
				Log.fail("Test case Failed. Number of versions listed in History view is not increased after renaming.", driver);
				return;
			}

			//Verifies that version has increased by 1 after renaming 
			if (currVersionCt == prevVersionCt + 1)
				Log.pass("Test case Passed. Version of a object is increased after renaming.");
			else
				Log.fail("Test case Failed. Version of a object is not increased after renaming.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest8_5_3C

	/**
	 * 8.5.4A : Workflow should be visible in operation menu for latest version in History view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","Sprint8", "History", "Smoke"},
			description	= "Workflow should be visible in task pane for latest version in History view")
	public void SprintTest8_5_4A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open History dialog of the object and select latest version
			//---------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel


			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view is not opened.");

			if (!homePage.listView.clickItemByIndex(0)) //Selects the latest version of the object
				throw new Exception("Latest version of an object is not selected.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened and latest version is selected.");

			//Verification : To Verify Check out is visible in Task Pane
			//---------------------------------------------------------
			if (homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.Workflow.Value))
				Log.pass("Test case Passed. Worflow option is visible in operation menu for latest version of the object.");
			else
				Log.fail("Test case Failed. Worflow option is not visible in operation for latest version of the object.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest8_5_4A

	/**
	 * 8.5.4B : Workflow should not be visible in operation menu for older version in History view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","Sprint8", "History"},
			description	= "Workflow should not be visible in task pane for older version in History view")
	public void SprintTest8_5_4B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open History dialog of the object
			//------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel


			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Select the older version of the object
			//-----------------------------------------------
			if (homePage.listView.itemCount() <= 1)
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not have older versions.");

			homePage.listView.clickItemByIndex(1); //Selects the older version of the object

			if (!homePage.listView.isItemSelectedByIndex(1)) //Checks if older version of the object is selected
				throw new Exception("Older version of an object is not selected.");

			Log.message("3. Older version of an object is selected.");

			//Verification : To Verify Check out is not visible in Task Pane
			//----------------------------------------------------------------
			if (!homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.Workflow.Value))
				Log.pass("Test case Passed. Workflow option is not visible in operation menu for older version of the object.");
			else
				Log.fail("Test case Failed. Workflow option is visible in operation menu for older version of the object.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest8_5_4B

	/**
	 * 8.5.4C : Change Workflow for latest version in History view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","Sprint8", "History"},
			description	= "Change Workflow for latest version in History view")
	public void SprintTest8_5_4C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open History dialog of the object and select latest version
			//---------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel


			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Select latest version and Change the workflow
			//------------------------------------------------------
			if (!homePage.listView.clickItemByIndex(0)) //Selects the latest version of the object
				throw new Exception("Latest version of an object is not selected.");

			if (homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.UndoCheckOut.Value)) { //Undo Checkouts objects if object is checked out
				homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);
				MFilesDialog  mfilesDialog = new MFilesDialog(driver);
				mfilesDialog.confirmUndoCheckOut(true);


				if (!homePage.listView.clickItemByIndex(0)) //Selects the latest version of the object
					throw new Exception("Latest version of an object is not selected after performing undo checkout.");
			}

			Log.message("3. Latest version of an object is selected.");

			//Step-4 : Change the workflow
			//----------------------------
			int prevObjCt = homePage.listView.itemCount(); //Gets the number of objects displayed in History view
			int prevVersionCt = Integer.parseInt(homePage.listView.getColumnValueByItemIndex(0, "Version")); //Gets the version count of the latest version

			if (!homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.Workflow.Value)) //Checks if Workflow is visible in 
				throw new Exception("Worflow option is not enabled in operation menu for latest version of the object.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value); //Selects workflow from Operation menu

			MFilesDialog  mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.changeWorkflow(dataPool.get(Caption.MenuItems.Workflow.Value), dataPool.get("WorkflowState"), dataPool.get("WorkflowComments"));


			Log.message("4. Workflow changed in change workflow mfilesDialog.");

			//Verification : To verify workflow and state are modified
			//---------------------------------------------------------
			int currObjCt = homePage.listView.itemCount(); //Gets the number of objects displayed in History view
			int currVersionCt = Integer.parseInt(homePage.listView.getColumnValueByItemIndex(0, "Version")); //Gets the version count of the latest version

			//Verifies that number of items in History view has increased by 1 in History view
			if (currObjCt != prevObjCt + 1) {
				Log.fail("Test case Failed. Number of versions listed in History view is not increased after changing workflow.", driver);
				return;
			}

			//Verifies that version has increased by 1 after renaming 
			if (currVersionCt == prevVersionCt + 1)
				Log.pass("Test case Passed. Version of a object is increased after changing workflow.");
			else
				Log.fail("Test case Failed. Version of a object is not increased after changing workflow.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest8_5_4C

	/**
	 * 8.5.5A : Double Clicking Latest version should open Checkout prompt
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint8", "History", "SKIP_JavaApplet"},
			description	= "Double Clicking Latest version should open Checkout prompt")
	public void SprintTest8_5_5A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open History dialog of the object
			//------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel


			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Select latest version and double click the latest version
			//-------------------------------------------------------------------
			if (!homePage.listView.clickItemByIndex(0)) //Selects the latest version of the object
				throw new Exception("Latest version of an object is not selected.");

			if (homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.UndoCheckOut.Value)) { //Undo Checkouts objects if object is checked out
				homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);

				MFilesDialog  mfilesDialog = new MFilesDialog(driver);
				mfilesDialog.confirmUndoCheckOut(true);
			}

			homePage.listView.doubleClickItemByIndex(0); //Double clicks latest version

			Log.message("3. Latest version of an object is double clicked.");

			//Verification : To verify Check-out prompt appears
			//---------------------------------------------------------
			if (!MFilesDialog.exists(driver)) { //Checks if M-Files Confirmation dialog has opened
				Log.fail("Test case Failed. M-Files Confirmation dialog to checkout is not opened.", driver);
				return;
			}

			MFilesDialog  mfilesDialog = new MFilesDialog(driver);

			//Verifies that Check-out prompt appears with Check-out button 
			if (mfilesDialog.isCheckOutPromtDisplayed())
				Log.pass("Test case Passed. Checkout Prompt is displayed after double clicking latest version.");
			else
				Log.fail("Test case Failed. Checkout Prompt is not displayed after double clicking latest version.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest8_5_5A

	/**
	 * 8.5.5B : Double Clicking older version should open confirmation prompt
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint8", "History", "SKIP_JavaApplet"},
			description	= "Double Clicking Latest version should open confirmation prompt")
	public void SprintTest8_5_5B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open History dialog of the object
			//------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel


			if (!ListView.isHistoryViewOpened(driver)) 
				throw new Exception ("History dialog does not opened for the document.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Select older version and double click the older version
			//-------------------------------------------------------------------
			if (!homePage.listView.clickItemByIndex(0)) //Selects the latest version of the object
				throw new Exception ("Recent version of an document is not selected to undo check out if document is checked out.");

			if (homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.UndoCheckOut.Value)) { //Undo Checkouts objects if object is checked out
				homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);
				MFilesDialog  mfilesDialog = new MFilesDialog(driver);
				mfilesDialog.confirmUndoCheckOut(true);

			}

			if (homePage.listView.itemCount() <= 1)
				throw new Exception ("There are no older versions available for selected document.");

			homePage.listView.doubleClickItemByIndex(1); //Double clicks latest version


			Log.message("3. Latest version of an object is double clicked.");

			//Verification : To verify if M-Files dialog has appeared
			//---------------------------------------------------------
			if (!MFilesDialog.exists(driver)) { //Checks if M-Files Confirmation dialog has opened
				Log.fail("Test case Failed. M-Files Confirmation dialog to checkout is not opened.", driver);
				return;
			}

			MFilesDialog  mfilesDialog = new MFilesDialog(driver);

			if (mfilesDialog.isCheckOutPromtDisplayed()) { //Checks if M-Files check out dialog has opened
				Log.fail("Test case Failed. Checkout prompt is displayed on double clicking older version of a document..", driver);
				return;
			}

			//Verifies that version has increased by 1 after renaming 
			if (mfilesDialog.getMessage().toUpperCase().contains("NOT THE LATEST VERSION"))
				Log.pass("Test case Passed. M-Files Prompt is displayed saying not a latest version.");
			else
				Log.fail("Test case Failed. M-Files Prompt is not displayed saying not a latest version.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest8_5_5B

	/**
	 * 8.5.5C : Double Clicking Latest version of Checked out document should open the document
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint8", "History"},
			description	= "Double Clicking Latest version of Checked out document should open the document")
	public void SprintTest8_5_5C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open History dialog of the object and select latest version
			//---------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel


			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened and latest version is selected.");

			//Step-3 : Select latest version and double click the latest version
			//-------------------------------------------------------------------
			if (!ListView.isCheckedOutByItemIndex(driver, 0)) { //Checks out object if not checked out

				if (!homePage.listView.clickItemByIndex(0))
					throw new Exception("Latest version of an object is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Checks out from Task Pane

				if (!ListView.isCheckedOutByItemIndex(driver, 0)) 
					throw new Exception("Latest version of an object is not checked out.");
			}

			homePage.listView.doubleClickItemByIndex(0); //Double clicks latest version


			Log.message("3. Latest version of an Checked out object is selected and double clicked.");

			//Verification : To verify workflow and state are modified
			//---------------------------------------------------------

			//Verifies that MFiles dialog has opened
			if (!MFilesDialog.exists(driver))
				Log.pass("Test case Passed.  M-Files Confirmation dialog to checkout is not opened.");
			else
				Log.fail("Test case Failed. M-Files Confirmation dialog to checkout is opened.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest8_5_5C

	/** 
	 * 8.5.5D : Double Clicking older version of Checked out document should open confirmation prompt
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint8", "History", "SKIP_JavaApplet"},
			description	= "Double Clicking older version of Checked out document should open confirmation prompt")
	public void SprintTest8_5_5D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open History dialog of the object and select latest version
			//---------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel


			if (!ListView.isHistoryViewOpened(driver)) 
				throw new Exception ("History dialog does not opened for the document.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened and latest version is selected.");

			//Step-3 : Select latest version and double click the older version
			//-------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 1)
				throw new SkipException ("There are no older versions available for selected document.");

			if (!ListView.isCheckedOutByItemIndex(driver, 0)) { //Checks out object if not checked out

				if (!homePage.listView.clickItemByIndex(0))
					throw new Exception("Latest version of an object is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Checks out from Task Pane

				if (!ListView.isCheckedOutByItemIndex(driver, 0)) 
					throw new Exception("Latest version of an object is not checked out.");
			}

			homePage.listView.doubleClickItemByIndex(1); //Double clicks older version


			Log.message("3. Older version of an checked out object is double clicked.");

			//Verification : To verify that MFiles dialog is prompted
			//---------------------------------------------------------
			if (!MFilesDialog.exists(driver)) { //Checks if M-Files Confirmation dialog has opened
				Log.fail("Test case Failed. M-Files Confirmation dialog is not opened.", driver);
				return;
			}

			MFilesDialog  mfilesDialog = new MFilesDialog(driver);

			if (mfilesDialog.isCheckOutPromtDisplayed()) { //Checks if M-Files check out dialog has opened
				Log.fail("Test case Failed. Checkout prompt is displayed on double clicking older version of a document..", driver);
				return;
			}

			//Verifies that version has increased by 1 after renaming 
			if (mfilesDialog.getMessage().toUpperCase().contains("NOT THE LATEST VERSION"))
				Log.pass("Test case Passed. M-Files Prompt is displayed saying not a latest version.");
			else
				Log.fail("Test case Failed. M-Files Prompt is not displayed saying not a latest version.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest8_5_5D

	/**
	 * 8.5.6A : Convert to Single file document is not enabled for previous version
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint8", "History"}, 
			description = "Convert to Single file document is not enabled for previous version")
	public void SprintTest8_5_6A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Check out the document
			//-------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) { //Checks out object if not checked out

				if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
					throw new Exception("Object('" + dataPool.get("ObjectName") + "') is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Checks out from Task Pane

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) 
					throw new Exception("Object('" + dataPool.get("ObjectName") + "') is not checked out.");
			}

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-3 : Check and Convert SFD to MFD Document
			//-----------------------------------------------
			if (!ListView.isSFDByItemName(driver, dataPool.get("ObjectName"))) //Checks if this is SFD
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not single file document.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value);		

			String mfdName = dataPool.get("ObjectName").split("\\.")[0];

			homePage.listView.clickRefresh();//Refreshes the view

			if (ListView.isSFDByItemName(driver, mfdName)) //Checks if this is SFD
				throw new SkipException("Object (" + mfdName + ") is not multi file document.");

			Log.message("3. SFD document (" + dataPool.get("ObjectName") + ") is converted to MFD document.");

			//Step-4 : Open History dialog of the object
			//------------------------------------------
			if (!homePage.listView.clickItem(mfdName)) //Selects the Object in the list
				throw new Exception("Object (" + mfdName + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel

			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view is not opened..");

			Log.message("4. History view of an object (" + mfdName + ") is opened.");

			//Step-5 : Select the older version of the object
			//-----------------------------------------------
			if (homePage.listView.itemCount() <= 1)
				throw new SkipException("Invalid test data. Object (" + mfdName + ") does not have older versions.");

			homePage.listView.clickItemByIndex(1); //Selects the older version of the object

			Log.message("5. Older version of an object is selected.");

			//Verification : To Verify Convert to Single-file Document is not enabled in operations menuBar
			//------------------------------------------------------------------

			//Verifies that Convert to Single-file Document is not enabled for older version of objects
			if (!homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.ConvertToSFD_O.Value))
				Log.pass("Test case Passed. 'Convert to Single-file Document' is not enabled for older version of an object.");
			else
				Log.fail("Test case Failed. 'Convert to Single-file Document' is enabled for older version of an object.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest8_5_6A

	/**
	 * 8.5.6B : Convert to Single file document is enabled for latest version
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint8", "History"}, 
			description = "Convert to Single file document is enabled for latest version")
	public void SprintTest8_5_6B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Check out the document
			//-------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) { //Checks out object if not checked out

				if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
					throw new Exception("Latest version of an object is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Checks out from Task Pane

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) 
					throw new Exception("Latest version of an object is not checked out.");
			}

			Log.message("2. Document is checked out.");

			//Step-3 : Check and Convert SFD to MFD Document
			//-----------------------------------------------
			if (!homePage.listView.isSFDBasedOnObjectIcon(dataPool.get("ObjectName")))
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") is MFD");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value);	


			String mfdName = dataPool.get("ObjectName").split("\\.")[0];

			if (homePage.listView.isSFDBasedOnObjectIcon(mfdName))
				throw new Exception ("Object is not converted to Multi-file document.");

			Log.message("3. SFD document (" + dataPool.get("ObjectName") + ") is converted to MFD document.");

			//Step-4 : Open History dialog of the object and select latest version of a document
			//----------------------------------------------------------------------------------
			if (!homePage.listView.clickItem(mfdName)) //Selects the Object in the list
				throw new Exception("Object (" + mfdName + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel


			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view is not opened..");

			homePage.listView.clickItemByIndex(0); //Selects the older version of the object

			Log.message("4. History view of an object (" + mfdName + ") is opened.");

			//Verification : To Verify Convert to Single-file Document is enabled in operations menuBar
			//---------------------------------------------------------------------------------------

			//Verifies that Convert to Single-file Document is enabled for latest version of objects
			if (homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.ConvertToSFD_O.Value))
				Log.pass("Test case Passed. 'Convert to Single-file Document' is not enabled for older version of an object.");
			else
				Log.fail("Test case Failed. 'Convert to Single-file Document' is enabled for older version of an object.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest8_5_6B

	/**
	 * 8.5.6C : Convert to Single file document should be successful for latest version
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint8", "History"}, 
			description = "Convert to Single file document is enabled for latest version")
	public void SprintTest8_5_6C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Check out the document
			//-------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) { //Checks out object if not checked out

				if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
					throw new Exception("Latest version of an object is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Checks out from Task Pane

				homePage.listView.clickRefresh();//Refreshes the view

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) 
					throw new Exception("Latest version of an object is not checked out.");
			}

			Log.message("2. Document is checked out.");

			//Step-3 : Check and Convert SFD to MFD Document
			//-----------------------------------------------
			if (!ListView.isSFDByItemName(driver, dataPool.get("ObjectName"))) 
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") is MFD document.") ;

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value);	

			homePage.listView.clickRefresh();//Refreshes the view

			String mfdName = dataPool.get("ObjectName").split("\\.")[0];

			if (ListView.isSFDByItemName(driver, mfdName))
				throw new Exception ("Object is not converted to Multi-file document.");

			Log.message("3. SFD document (" + dataPool.get("ObjectName") + ") is converted to MFD document.");

			//Step-4 : Open History dialog of the object and select latest version of a document
			//----------------------------------------------------------------------------------
			if (!homePage.listView.clickItem(mfdName)) //Selects the Object in the list
				throw new Exception("Object (" + mfdName + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel

			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view is not opened..");

			Log.message("4. History view of an object (" + mfdName + ") is opened.");

			//Step-5 : Select Convert to Single-File Document from operations menu for latest version
			//----------------------------------------------------------------------------------------
			if (!homePage.listView.clickItemByIndex(0)) //Selects the latest version of the object
				throw new Exception("Latest version of an object is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.ConvertToSFD_O.Value); //Selects Convert to Single-File Document from operations menu
			homePage.listView.clickRefresh();//Refreshes the view

			Log.message("5. Latest version is seleted and 'Convert to Single file Document' is selected from operations menu.");

			//Verification : To Verify document is converted to single file document successfully
			//---------------------------------------------------------------------------------------
			if (ListView.isSFDByItemIndex(driver, 0))
				Log.pass("Test case Passed. Latest version of Single file MFD is converted to single file successfully.");
			else
				Log.fail("Test case Failed. Single file Column is not showing value as Yes after conversion.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest8_5_6C

	/**
	 * 8.5.7A : Convert to Multi File document is not enabled for previous version
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint8", "History"}, 
			description = "Convert to Multi File document is not enabled for previous version")
	public void SprintTest8_5_7A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Check out the document
			//-------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) { //Checks out object if not checked out

				if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
					throw new Exception("Latest version of an object is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Checks out from Task Pane

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) 
					throw new Exception("Latest version of an object is not checked out.");
			}

			Log.message("2. Document is checked out.");

			//Step-3 : Open History dialog of the object 
			//-------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel


			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view is not opened..");

			homePage.listView.clickItemByIndex(0); //Selects the older version of the object

			Log.message("3. History view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-5 : Select the older version of the object
			//-----------------------------------------------
			if (homePage.listView.itemCount() <= 1) 
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not have older versions.");

			homePage.listView.clickItemByIndex(1); //Selects the older version of the object

			Log.message("4. Older version of an object is selected.");

			//Verification : To Verify Convert to Multi-file Document is not enabled in operations menuBar
			//---------------------------------------------------------------------------------------

			//Verifies that Convert to Multi-file Document is not enabled for older version of objects
			if (!homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value))
				Log.pass("Test case Passed. 'Convert to Multi-file Document' is not enabled for older version of an object.");
			else
				Log.fail("Test case Failed. 'Convert to Multi-file Document' is enabled for older version of an object.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest8_5_7A

	/**
	 * 8.5.7B : Convert to Multi File document is enabled for latest version
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint8", "History"}, 
			description = "Convert to Multi File document is not enabled for previous version")
	public void SprintTest8_5_7B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Check out the document
			//-------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) { //Checks out object if not checked out

				if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
					throw new Exception("Latest version of an object is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Checks out from Task Pane

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) 
					throw new Exception("Latest version of an object is not checked out.");
			}

			Log.message("2. Document is checked out.");

			//Step-3 : Open History dialog of the object and select latest version of an object
			//----------------------------------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel


			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view is not opened..");

			homePage.listView.clickItemByIndex(0); //Selects the latest version of the object

			Log.message("3. History view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Verification : To Verify Convert to Multi-file Document is enabled in operations menuBar
			//---------------------------------------------------------------------------------------

			//Verifies that Convert to Multi-file Document is not enabled for latest version of objects
			if (homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value))
				Log.pass("Test case Passed. 'Convert to Multi-file Document' is enabled for latest version of an object.");
			else
				Log.fail("Test case Failed. 'Convert to Multi-file Document' is not enabled for latest version of an object.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest8_5_7B

	/**
	 * 8.5.7C : Convert latest version of a document to Multi File document 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint8", "History"}, 
			description = "Convert latest version of a document to Multi File document")
	public void SprintTest8_5_7C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Check out the document
			//-------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) { //Checks out object if not checked out

				if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
					throw new Exception("Latest version of an object is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Checks out from Task Pane

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) 
					throw new Exception("Latest version of an object is not checked out.");
			}

			Log.message("2. Document is checked out.");

			//Step-3 : Open History dialog of the object 
			//-------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel


			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view is not opened..");

			Log.message("3. History view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-4 : Select Convert to Multi-File Document from operations menu for latest version
			//--------------------------------------------------------------------------------------
			if (!homePage.listView.clickItemByIndex(0)) //Selects the latest version of the object
				throw new Exception("Latest version of an object is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value); //Selects Convert to Single-File Document from operations menu

			Log.message("4. Latest version is seleted and 'Convert to Multi File Document' is selected from operations menu.");

			//Verification : To Verify document is converted to Multi file document successfully
			//---------------------------------------------------------------------------------------
			if (!ListView.isSFDByItemIndex(driver, 0))
				Log.pass("Test case Passed. Latest version of SFD is converted to MFD successfully.");
			else
				Log.fail("Test case Failed. Latest version of SFD is not converted to MFD.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest8_5_7C

	/** 
	 * 8.5.8A : Replace with file should be disabled for older version of a document
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint8", "History"},
			description	= "Replace with file should be disabled for older version of a document")
	public void SprintTest8_5_8A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open History dialog of the object
			//------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel


			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view is not opened..");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Select the older version of the object
			//-----------------------------------------------
			if (homePage.listView.itemCount() <= 1) 
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not have older versions.");

			homePage.listView.clickItemByIndex(1); //Selects the older version of the object

			if (!homePage.listView.isItemSelectedByIndex(1)) 
				throw new Exception("Older version of an object (" + dataPool.get("ObjectName") + ") is not selected.");

			Log.message("3. Older version of an object is selected.");

			//Verification : To Verify Replace with File (Upload) is not enabled in operations menuBar
			//--------------------------------------------------------------------------------------

			//Verifies that Replace with File (Upload) is enabled for older version of objects
			if (!homePage.menuBar.IsItemEnabledInOperationsMenu("Replace with File (Upload)"))
				Log.pass("Test case Passed. 'Replace with File (Upload)' is not enabled for older version of an object.");
			else
				Log.fail("Test case Failed. 'Replace with File (Upload)' is enabled for older version of an object.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest8_5_8A

	/** 
	 * 8.5.8B : Replace with file should be enabled for latest version of a document
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint8", "History"},
			description	= "Replace with file should be enabled for latest version of a document")
	public void SprintTest8_5_8B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Check-out and Open History dialog of the object
			//--------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {//Checks out an object
				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);


				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if object is checked out
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");
			}

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel


			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view is not opened..");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Select the latest version of the object
			//-----------------------------------------------
			if(!homePage.listView.clickItemByIndex(0)) //Selects the Older version of the object
				throw new Exception("Latest version of an object (" + dataPool.get("ObjectName") + ") is not selected.");



			Log.message("3. Latest version of an object is selected.");

			//Verification : To Verify Replace with File (Upload) is enabled in operations menuBar
			//---------------------------------------------------------------------------------

			//Verifies that Replace with File (Upload) is enabled for latest version of objects
			if (homePage.menuBar.IsItemEnabledInOperationsMenu("Replace with File (Upload)"))
				Log.pass("Test case Passed. 'Replace with File (Upload)' is enabled for latest version of an object.");
			else
				Log.fail("Test case Failed. 'Replace with File (Upload)' is not enabled for latest version of an object.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest8_5_8B

	/**
	 * 8.5.9A : Convert to Single file document is not enabled for latest version of non-document object types
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint8", "History"}, 
			description = "Convert to Single file document is not enabled for latest version of non-document object types")
	public void SprintTest8_5_9A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Check out the document
			//-------------------------------
			if (!homePage.listView.isColumnExists("Object Type")) //Checks and inserts Checked out To Column to the list
				if (!homePage.listView.insertColumn("Object Type"))
					throw new Exception("Column (Object Type) column does not inserted successfully.");

			if (homePage.listView.getColumnValueByItemName(dataPool.get("ObjectName"), "Object Type").equals("DOCUMENT"))  
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is of document object type.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) { //Checks out object if not checked out

				if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
					throw new Exception("Latest version of an object is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Checks out from Task Pane

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) 
					throw new Exception("Latest version of an object is not checked out.");
			}

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-3 : Open History dialog of the object and select latest version of a document
			//----------------------------------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel


			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view is not opened..");

			homePage.listView.clickItemByIndex(0); //Selects the latest version of the object

			Log.message("3. History view of an object (" + dataPool.get("ObjectName") + ") is opened and latest version is selected.");

			//Verification : To Verify Convert to Single-file Document is enabled in operations menuBar
			//---------------------------------------------------------------------------------------

			//Verifies that Convert to Single-file Document is enabled for latest version of objects
			if (!homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.ConvertToSFD_O.Value))
				Log.pass("Test case Passed. 'Convert to Single-file Document' is not enabled for latest version of non-document object types.");
			else
				Log.fail("Test case Failed. 'Convert to Single-file Document' is enabled for latest version of non-document object types.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest8_5_9A

	/**
	 * 8.5.9B : Convert to Multi File document is not enabled for latest version of non-document object types
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint8", "History"}, 
			description = "Convert to Multi File document is not enabled for latest version of non-document object types")
	public void SprintTest8_5_9B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Check out the document
			//-------------------------------
			if (!homePage.listView.isColumnExists("Object Type")) //Checks and inserts Checked out To Column to the list
				if (!homePage.listView.insertColumn("Object Type"))
					throw new Exception("Column (Object Type) column does not inserted successfully.");

			if (homePage.listView.getColumnValueByItemName(dataPool.get("ObjectName"), "Object Type").equals("DOCUMENT"))  
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is of document object type.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) { //Checks out object if not checked out

				if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
					throw new Exception("Latest version of an object is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Checks out from Task Pane

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) 
					throw new Exception("Latest version of an object is not checked out.");
			}

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-3 : Open History dialog of the object and select latest version of a document
			//----------------------------------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel


			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view is not opened..");

			homePage.listView.clickItemByIndex(0); //Selects the latest version of the object

			Log.message("3. History view of an object (" + dataPool.get("ObjectName") + ") is opened and latest version is selected.");

			//Verification : To Verify Convert to Multi-file Document is enabled in operations menuBar
			//---------------------------------------------------------------------------------------

			//Verifies that Convert to Multi-file Document is enabled for latest version of objects
			if (!homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value))
				Log.pass("Test case Passed. 'Convert to Multi-file Document' is not enabled for latest version of non-document object types.");
			else
				Log.fail("Test case Failed. 'Convert to Multi-file Document' is enabled for latest version of non-document object types.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest8_5_9B

	/** 
	 * 8.5.10A : Add File option should not available for older version of a document
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint8", "History"},
			description	= "Add File option should not available for older version of a document")
	public void SprintTest8_5_10A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open History dialog of the object and select older version
			//-------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel


			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view is not opened..");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Select the older version of the object
			//-----------------------------------------------
			if (homePage.listView.itemCount() <= 1) 
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not have older versions.");

			homePage.listView.clickItemByIndex(1); //Selects the older version of the object

			if (!homePage.listView.isItemSelectedByIndex(1)) 
				throw new Exception("Older version of an object (" + dataPool.get("ObjectName") + ") is not selected.");

			Log.message("3. Older version of an object is selected.");

			//Verification : To Verify Replace with File (Upload) is not enabled in operations menuBar
			//--------------------------------------------------------------------------------------

			//Verifies that Add File is not available for older version of objects
			if (!homePage.taskPanel.isItemExists("Add File"))
				Log.pass("Test case Passed. 'Add File' is not available for older version of an object.");
			else
				Log.fail("Test case Failed. 'Add File' is available for older version of an object.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest8_5_10A

	/** 
	 * 8.5.10B : Add File option should be available for latest version of a document
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint8", "History"},
			description	= "Add File option should be available for latest version of a document")
	public void SprintTest8_5_10B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Check-out and Open History dialog of the object
			//--------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) { //Checks out object if not checked out

				if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
					throw new Exception("Latest version of an object is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Checks out from Task Pane

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) 
					throw new Exception("Latest version of an object is not checked out.");
			}



			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel


			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view is not opened..");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Select the latest version of the object
			//-----------------------------------------------
			if (!homePage.listView.clickItemByIndex(0)) //Selects the latest version of the object 
				throw new Exception("Latest version of an object (" + dataPool.get("ObjectName") + ") is not selected.");

			Log.message("3. Latest version of an object is selected.");

			//Verification : To Verify Add File is available in operations menuBar
			//------------------------------------------------------------------

			//Verifies that Add File is available for latest version of objects
			if (homePage.taskPanel.isItemExists("Add File"))
				Log.pass("Test case Passed. 'Add File' is available for latest version of an object.");
			else
				Log.fail("Test case Failed. 'Add File' is not available for latest version of an object.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest8_5_10B

	/**
	 * 8.5.11B : Check-in should not be available for older versions of the checked out document
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint8", "History"}, 
			description = "Check-in should not be available for older versions of the checked out document")
	public void SprintTest8_5_11B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open History dialog of the object
			//------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel


			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Check out the latest version of an object
			//--------------------------------------------------
			if (!ListView.isCheckedOutByItemIndex(driver, 0)) { //Checks out object if not checked out

				if (!homePage.listView.clickItemByIndex(0))
					throw new Exception("Latest version of an object is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Checks out from Task Pane

				if (!ListView.isCheckedOutByItemIndex(driver, 0 )) 
					throw new Exception("Latest version of an object is not checked out.");
			}

			Log.message("3. Latest version of an object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-4 : Select the older version of the object
			//-----------------------------------------------
			homePage.listView.clickItemByIndex(1); //Selects the older version of object

			Log.message("4. Older version of an object is selected");

			//Verification : To Verify older version of an object does not have undo-checkout option
			//---------------------------------------------------------------------------------------

			//Verifies that task pane has undo checkout option
			if (!homePage.taskPanel.isItemExists(Caption.MenuItems.CheckIn.Value))
				Log.pass("Test case Passed. Check In option is not available in task panel for older version.");
			else
				Log.fail("Test case Failed. Check In option is available in task panel for older version.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest8_5_11B

	/**
	 * 8.5.11C : Performing Check out and immediate Check-in for latest version should not increase the version of the document
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint8", "History"}, 
			description = "Performing Check out and immediate Check-in for latest version should not increase the version of the document")
	public void SprintTest8_5_11C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open History dialog of the object
			//------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel


			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view is not opened..");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Check out the latest version of an object
			//--------------------------------------------------
			if (!ListView.isCheckedOutByItemIndex(driver, 0)) { //Checks out object if not checked out

				if (!homePage.listView.clickItemByIndex(0))
					throw new Exception("Latest version of an object is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Checks out from Task Pane

				if (!ListView.isCheckedOutByItemIndex(driver, 0 )) 
					throw new Exception("Latest version of an object is not checked out.");
			}

			int prevObjCt = homePage.listView.itemCount(); //Gets the number of objects displayed in History view
			int prevVersionCt = Integer.parseInt(homePage.listView.getColumnValueByItemIndex(0, "Version")); //Gets the version count of the latest version

			Log.message("3. Latest version of a document is checked out.");

			//Step-4 : Check-in the latest version of the object
			//--------------------------------------------------
			if (!homePage.listView.clickItemByIndex(0)) { //Selects the latest version of the object
				Log.fail("Latest version of an object is not selected.", driver);
				return;
			}

			if (!homePage.taskPanel.isItemExists(Caption.MenuItems.CheckIn.Value))
				Log.fail("Test Case failed. The Check In option was not available for the latest checked out version.", driver);

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value); //Undo Checkout from Task Pane


			Log.message("4. Latest version of an object is selected and check-in is clicked from task pane.");

			//Verification : To Verify latest version of object is checked out
			//----------------------------------------------------------------
			int currObjCt = homePage.listView.itemCount(); //Gets the number of objects displayed in History view
			int currVersionCt = Integer.parseInt(homePage.listView.getColumnValueByItemIndex(0, "Version")); //Gets the version count of the latest version

			//Verifies that number of version has not increased in History view
			if (currObjCt != prevObjCt - 1) {
				Log.fail("Test case Failed. Number of versions listed in History view is not restored after performing Check In.", driver);
				return;
			}

			//Verifies that new version is not created
			if (currVersionCt == prevVersionCt - 1)
				Log.pass("Test case Passed. Version of a object is restored after performing Check In.");
			else
				Log.fail("Test case Failed. Version of a object is not restored after performing Check In.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest8_5_27

	/**
	 * 9.1.1 : Verify history view is displayed with more than one Version 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint9", "History"},
			description = "Verify history view is displayed with more than one Version ")
	public void SprintTest9_1_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Click on the object
			//----------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object"))) 
				throw new SkipException("Invalid Test data. The given object " + dataPool.get("Object") + " was not found in the vault.");

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new Exception ("Object (" + dataPool.get("Object") + ") is not got selected.");

			Log.message("2. Click on the object");

			//Step-3: Click the History link from the task pane
			//--------------------------------------------------
			homePage.taskPanel.clickItem("History");


			Log.message("3. Click the History link from the task pane.");

			//Verification: To verify if the History view of the object is displayed with all the versions of the object
			//-----------------------------------------------------------------------------------------------------------
			if(!ListView.isHistoryViewOpened(driver)) {
				Log.fail("Test Case Failed. The History view of the object was not displayed.", driver);
				return;
			}

			if(homePage.listView.itemCount() >= 2)
				Log.pass("Test Case Passed. The history view of the object was displayed with more than one version.");
			else
				Log.fail("Test Case Failed. History view is not displayed with more than one version.",driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest9_1_1

} //End class History

package MFClient.Tests.ObjectOperations;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.ActionEventUtils;
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
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class ShortcutUseAndAvailability {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String userName = null;
	public static String password = null;
	public static String testVault = null;
	public static String configURL = null;
	public static String userFullName = null;
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
			configURL = xmlParameters.getParameter("ConfigurationURL");
			userName = xmlParameters.getParameter("UserName");
			password = xmlParameters.getParameter("Password");
			testVault = xmlParameters.getParameter("VaultName");		
			userFullName = xmlParameters.getParameter("UserFullName");

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
	 * 24.7.1A : Checkout should not have short cut key in the context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "Checkout should not have short cut key in the context menu.")
	public void SprintTest24_7_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not support Actions.");

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the item and Click CTRL+O Key
			//------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if object is checked out
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is already checked out.");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is right clicked.");

			//Verification : To Verify short key for check out is not available in context menu
			//------------------------------------------------------------------------------------

			//Verifies that Check out short cut key is available in context menu
			if (!homePage.listView.itemExistsInContextMenu("CTRL+O"))
				Log.pass("Test case Passed. Short cut keys is not available for check out operation in context menu.");
			else
				Log.fail("Test case Failed. Short cut keys is available for check out operation in context menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_1A

	/**
	 * 24.7.1B : Checkout should not have short cut key in the operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "Checkout should not have short cut key in the operations menu.")
	public void SprintTest24_7_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not support Actions.");

		driver = null;


		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the item and Click CTRL+O Key
			//------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if object is checked out
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is already checked out.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is Clicked.");

			//Verification : To Verify short key for check out is not available in operations menu
			//--------------------------------------------------------------------------------------

			//Verifies that Check out short cut key is not available in operations menu
			if (!homePage.menuBar.IsOperationMenuItemExists("CTRL+O"))
				Log.pass("Test case Passed. Short cut keys is not available for check out operation in operations menu.");
			else
				Log.fail("Test case Failed. Short cut keys is available for check out operation in operations menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_1B

	/**
	 * 24.7.1C : Object should not be checked out on clicking CTRL+O key
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = " Object should not be checked out on clicking CTRL+O key.")
	public void SprintTest24_7_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE")|| driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports key actions.");

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the item and Click CTRL+O Key
			//------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if object is checked out
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is already checked out.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			ActionEventUtils.pressCTRLKey(driver, "o");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected and CTRL+O key is pressed.");

			//Verification : To Verify object is not checked out on clicking CTRL+O key
			//----------------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Verifies that Check out column is empty after clicking CTRL+O Key
				Log.pass("Test case Passed. Object is not checked out on pressing 'CTRL+O' short cut key.");
			else
				Log.fail("Test case Failed. Object is checked out on pressing 'CTRL+O' short cut key.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_1C

	/**
	 * 24.7.2A : Properties [Alt + Enter] should not have short cut key in context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "Properties [Alt + Enter] should not have short cut key in context menu.")
	public void SprintTest24_7_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") ||  driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports key actions.");

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Right click the object to open context menu 
			//-----------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") has not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is right clicked.");

			//Verification : To Verify short key for Properties is available in context menu
			//------------------------------------------------------------------------------------

			//Verifies that Properties short cut key is available in context menu
			if (!homePage.listView.itemExistsInContextMenu("ALT+ENTER"))
				Log.pass("Test case Passed. Short cut keys (ALT+ENTER) is not available for Properties in context menu.");
			else
				Log.fail("Test case Failed. Short cut keys (ALT+ENTER) is available for Properties in context menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_2A

	/**
	 * 24.7.2B : Properties [Alt + Enter] should not have short cut key in operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "Properties [Alt + Enter] should not have short cut key in operations menu.")
	public void SprintTest24_7_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") ||  driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports key actions.");

		driver = null;

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Right click the object to open context menu 
			//-----------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") has not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is right clicked.");

			//Verification : To Verify short key for Properties is available in operations menu
			//------------------------------------------------------------------------------------

			//Verifies that Properties short cut key is available in operations menu
			if (!homePage.menuBar.IsOperationMenuItemExists("ALT+ENTER"))
				Log.pass("Test case Passed. Short cut keys (ALT+ENTER) is not available for Properties in operations menu.");
			else
				Log.fail("Test case Failed. Short cut keys (ALT+ENTER) is available for Properties in operations menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_2B

	/**
	 * 24.7.3A : Check In should have short cut key in the context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "Check In should have short cut key in the context menu.")
	public void SprintTest24_7_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") ||  driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports key actions.");

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the item 
			//-------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {//Checks if object is checked out
				homePage.listView.clickItem(dataPool.get("ObjectName"));
				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);				
				Utils.fluentWait(driver);

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception("Object(" + dataPool.get("ObjectName") + ") is not checked out.");
			}

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") has not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is right clicked.");

			//Verification : To Verify short key for Check In is available in context menu
			//------------------------------------------------------------------------------------

			//Verifies that Check In short cut key is available in context menu
			if (homePage.listView.itemExistsInContextMenu("CTRL+I"))
				Log.pass("Test case Passed. Short cut keys (CTRL+I) is available for check in operation in context menu.");
			else
				Log.fail("Test case Failed. Short cut keys (CTRL+I) is not available for check in operation in context menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_3A

	/**
	 * 24.7.3B : Check In should have short cut key in the Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "Check In should have short cut key in the Operations menu.")
	public void SprintTest24_7_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") ||  driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports key actions.");

		driver = null;

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the item
			//------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {//Checks if object is checked out
				homePage.listView.clickItem(dataPool.get("ObjectName"));
				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);				
				Utils.fluentWait(driver);

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception("Object(" + dataPool.get("ObjectName") + ") is not checked out.");
			}

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Verification : To Verify short key for check In is available in operations menu
			//--------------------------------------------------------------------------------------

			//Verifies that Check In short cut key is available in operations menu
			if (homePage.menuBar.IsOperationMenuItemExists("CTRL+I"))
				Log.pass("Test case Passed. Short cut keys (CTRL+I) is available for check in operation in context menu.");
			else
				Log.fail("Test case Failed. Short cut keys (CTRL+I) is not available for check in operation in context menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_3B

	/**
	 * 24.7.3C : Checked out Object should be checked In on clicking CTRL + I key
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "Checked out Object should be checked In on clicking CTRL + I key.")
	public void SprintTest24_7_3C(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports key actions.");

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the item and Click CTRL+I Key
			//------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {//Checks if object is checked out
				homePage.listView.clickItem(dataPool.get("ObjectName"));
				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);				
				Utils.fluentWait(driver);

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception("Object(" + dataPool.get("ObjectName") + ") is not checked out.");
			}

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-3 : Select the item and Click CTRL+I Key
			//------------------------------------------------
			ActionEventUtils.pressCTRLKey(driver, "i");
			Utils.fluentWait(driver);

			Log.message("3. CTRL+I is pressed.");

			//Verification : To Verify object is checked in by pressing short cut key
			//-----------------------------------------------------------------------

			//Verifies that Check out column is empty after checking in
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is checked in by pressing CTRL+I short cut key.");
			else
				Log.fail("Test case Failed. Object is not checked in by pressing CTRL+I short cut key.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_3C

	/**
	 * 24.7.3D : No Change on pressing CTRL + I key for checked in object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "No Change on pressing CTRL + I key for checked in object.")
	public void SprintTest24_7_3D(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE")|| driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports key actions.");

		driver = null;

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the item and Click CTRL+I Key
			//------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if object is checked out
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") is in checked out state.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Checked in Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-3 : Select the item and Click CTRL+I Key
			//------------------------------------------------
			ActionEventUtils.pressCTRLKey(driver, "i");
			Utils.fluentWait(driver);

			Log.message("3. CTRL+I is pressed.");

			//Verification : To Verify object is checked in by pressing short cut key
			//-----------------------------------------------------------------------

			//Verifies that Check out column is empty after checking in
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object remained in the checked in state.");
			else
				Log.fail("Test case Failed. Object is not in the checked in state.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_3D

	/**
	 * 24.7.4A : Checkin with comments [ Ctrl + Shift + I] should have short cut key in the context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "Checkin with comments [ Ctrl + Shift + I] should have short cut key in the context menu.")
	public void SprintTest24_7_4A(HashMap<String,String> dataValues, String driverType) throws Exception {


		if (driverType.equalsIgnoreCase("IE")|| driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports key actions.");

		driver = null;

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the item 
			//-------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {//Checks out if it is in checked in state
				homePage.listView.clickItem(dataPool.get("ObjectName"));
				homePage.taskPanel.clickItem("Check Out");				
				Utils.fluentWait(driver);

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");
			}

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") has not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is right clicked.");

			//Verification : To Verify short key for Checkin with comments is available in context menu
			//------------------------------------------------------------------------------------

			//Verifies that Checkin with comments short cut key is available in context menu
			if (homePage.listView.itemExistsInContextMenu("CTRL+SHIFT+I"))
				Log.pass("Test case Passed. Short cut keys (CTRL+SHIFT++I) is available for check in with comments operation in context menu.");
			else
				Log.fail("Test case Failed. Short cut keys (CTRL+SHIFT++I) is not available for check in with comments operation in context menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_4A

	/**
	 * 24.7.4B : Checkin with comments should have short cut key in the Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "Checkin with comments should have short cut key in the Operations menu.")
	public void SprintTest24_7_4B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE")|| driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports key actions.");

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the item 
			//-------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {//Checks out if it is in checked in state
				homePage.listView.clickItem(dataPool.get("ObjectName"));
				homePage.taskPanel.clickItem("Check Out");				
				Utils.fluentWait(driver);

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");
			}

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Verification : To Verify short key for Checkin with comments is available in operations menu
			//--------------------------------------------------------------------------------------

			//Verifies that Checkin with comments short cut key is available in operations menu
			if (homePage.menuBar.IsOperationMenuItemExists("CTRL+SHIFT+I"))
				Log.pass("Test case Passed. Short cut keys (CTRL+SHIFT+I) is available for Checkin with comments operation in context menu.");
			else
				Log.fail("Test case Failed. Short cut keys (CTRL+SHIFT+I) is not available for Checkin with comments operation in context menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_4B

	/**
	 * 24.7.5A : CTRL+N key should not be available for new document creation.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "CTRL+N key should not be available for new document creation.")
	public void SprintTest24_7_5A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE")|| driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports key actions.");

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Verification : To Verify short key for New Document is available in new menu
			//------------------------------------------------------------------------------

			//Verifies that New Document short cut key is available in new menu
			if (!homePage.menuBar.NewMenuItemExists("CTRL+N"))
				Log.pass("Test case Passed. Short cut keys (CTRL+N) is not available for new document creation in new menu.");
			else
				Log.fail("Test case Failed. Short cut keys (CTRL+N) is available for new document creation in new menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_5A

	/**
	 * 24.7.11A : Comments [CTRL+M] should be available in context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "Comments [CTRL+M] should be available in context menu.")
	public void SprintTest24_7_11A(HashMap<String,String> dataValues, String driverType) throws Exception {


		if (driverType.equalsIgnoreCase("IE")|| driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports key actions.");

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the item 
			//-------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is right clicked.");

			//Verification : To Verify short key for Comments is available in context menu
			//---------------------------------------------------------------------------

			//Verifies that Comments short cut key is available in context menu
			if (homePage.listView.itemExistsInContextMenu("CTRL+M"))
				Log.pass("Test case Passed. Short cut keys (CTRL+M) is available in context menu.");
			else
				Log.fail("Test case Failed. Short cut keys (CTRL+M) is not available in context menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_11A

	/**
	 * 24.7.11B : Comments [CTRL+M] should be available in operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "Comments [CTRL+M] should be available in context menu.")
	public void SprintTest24_7_11B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE")|| driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports key actions.");

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the item 
			//-------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Verification : To Verify short key for Comments is available in operations menu
			//-------------------------------------------------------------------------------

			//Verifies that Comments short cut key is available in context menu
			if (homePage.menuBar.IsOperationMenuItemExists("CTRL+M"))
				Log.pass("Test case Passed. Short cut keys (CTRL+M) is available in operations menu.");
			else
				Log.fail("Test case Failed. Short cut keys (CTRL+M) is not available in operations menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_11B

	/**
	 * 24.7.12A : History [CTRL+H] should be available in context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "History [CTRL+H] should be available in context menu.")
	public void SprintTest24_7_12A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE")|| driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports key actions.");

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the item 
			//-------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is right clicked.");

			//Verification : To Verify short key for History is available in context menu
			//---------------------------------------------------------------------------

			//Verifies that History short cut key is available in context menu
			if (homePage.listView.itemExistsInContextMenu("CTRL+H"))
				Log.pass("Test case Passed. Short cut keys (CTRL+H) is available for opening History view in context menu.");
			else
				Log.fail("Test case Failed. Short cut keys (CTRL+H) is not available for opening History view in context menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_12A

	/**
	 * 24.7.12B : History [CTRL+H] should be available in operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "History [CTRL+H] should be available in operations menu.")
	public void SprintTest24_7_12B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE")|| driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports key actions.");

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the item 
			//-------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Verification : To Verify short key for History is available in operations menu
			//---------------------------------------------------------------------------

			//Verifies that History short cut key is available in operations menu
			if (homePage.menuBar.IsOperationMenuItemExists("CTRL+H"))
				Log.pass("Test case Passed. Short cut keys (CTRL+H) is available for opening History view in operations menu.");
			else
				Log.fail("Test case Failed. Short cut keys (CTRL+H) is not available for opening History view in operations menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_12B

	/**
	 * 24.7.12C : Pressing CTRL+H should open History view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "Pressing CTRL+H should open History view.")
	public void SprintTest24_7_12C(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE")|| driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports key actions.");

		driver = null;

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the item 
			//-------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-3 Press CTRL+H key
			//------------------------
			ActionEventUtils.pressCTRLKey(driver, "h");

			Log.message("3. CTRL+H Key is pressed.");			

			//Verification : To Verify if History view is opened on clicking CTRL+H key
			//---------------------------------------------------------------------------

			//Verifies that History view is opened on clicking CTRL+H key
			if (homePage.listView.getViewCaption().toUpperCase().equals("HISTORY"))
				Log.pass("Test case Passed. History view is opened on clicking CTRL+H key.");
			else
				Log.fail("Test case Failed. History view is not opened on clicking CTRL+H key.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_12C


	/**
	 * 24.7.14A : Relationships [CTRL+L] should be available in context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "Relationships [CTRL+L] should be available in context menu.")
	public void SprintTest24_7_14A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE")|| driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports key actions.");

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the item 
			//-------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is right clicked.");

			//Verification : To Verify short key for Relationships is available in context menu
			//----------------------------------------------------------------------------------

			//Verifies that Relationships short cut key is available in context menu
			if (homePage.listView.itemExistsInContextMenu("CTRL+L"))
				Log.pass("Test case Passed. Short cut keys (CTRL+L) is available for opening Relationships view in context menu.");
			else
				Log.fail("Test case Failed. Short cut keys (CTRL+L) is not available for opening Relationships view in context menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_14A

	/**
	 * 24.7.14B : Relationships [CTRL+L] should be available in operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "Relationships [CTRL+L] should be available in operations menu.")
	public void SprintTest24_7_14B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE")|| driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports key actions.");

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the item 
			//-------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Verification : To Verify short key for Relationships is available in operations menu
			//---------------------------------------------------------------------------

			//Verifies that Relationships short cut key is available in operations menu
			if (homePage.menuBar.IsOperationMenuItemExists("CTRL+L"))
				Log.pass("Test case Passed. Short cut keys (CTRL+L) is available for opening Relationships view in operations menu.");
			else
				Log.fail("Test case Failed. Short cut keys (CTRL+L) is not available for opening Relationships view in operations menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_14B

	/**
	 * 24.7.14C : Pressing CTRL+L should open Relationships view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "Pressing CTRL+L should open Relationships view.")
	public void SprintTest24_7_14C(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE")|| driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports key actions.");

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the item 
			//-------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-3 Press CTRL+L key
			//------------------------
			ActionEventUtils.pressCTRLKey(driver, "l");

			Log.message("3. CTRL+L Key is pressed.");			

			//Verification : To Verify if Relationships view is opened on clicking CTRL+L key
			//-------------------------------------------------------------------------------

			//Verifies that Relationships view is opened on clicking CTRL+L key
			if (homePage.listView.getViewCaption().toUpperCase().contains("RELATIONSHIPS"))
				Log.pass("Test case Passed. Relationships view is opened on clicking CTRL+L key.");
			else
				Log.fail("Test case Failed. Relationships view is not opened on clicking CTRL+L key.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_14C


	/**
	 * 24.7.16A : Show members (Ctrl+Shift+L) should not be available in context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "Show members (Ctrl+Shift+L) should be available in context menu.")
	public void SprintTest24_7_16A(HashMap<String,String> dataValues, String driverType) throws Exception {


		if (driverType.equalsIgnoreCase("IE")|| driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports key actions.");

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the item 
			//-------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.isColumnExists("Object Type")) //Checks and inserts Checked out To Column to the list
				if (!homePage.listView.insertColumn("Object Type"))
					throw new Exception("Column (Object Type) column does not inserted successfully.");

			if (homePage.listView.getColumnValueByItemName(dataPool.get("ObjectName"), "Object Type").equals("DOCUMENT COLLECTION"))  
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is of document collection object type.");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is right clicked.");

			//Verification : To Verify short key for Members is available in context menu
			//----------------------------------------------------------------------------------

			//Verifies that Members short cut key is available in context menu
			if (!homePage.listView.itemExistsInContextMenu("CTRL+SHIFT+L"))
				Log.pass("Test case Passed. Short cut keys (CTRL+SHIFT+L) is not available in context menu.");
			else
				Log.fail("Test case Failed. Short cut keys (CTRL+SHIFT+L) is available in context menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_16A

	/**
	 * 24.7.16B : Show members (Ctrl+Shift+L) should not be available in operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "Show members (Ctrl+Shift+L) should not be available in operations menu.")
	public void SprintTest24_7_16B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE")|| driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports key actions.");

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the item 
			//-------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.isColumnExists("Object Type")) //Checks and inserts Checked out To Column to the list
				if (!homePage.listView.insertColumn("Object Type"))
					throw new Exception("Column (Object Type) column does not inserted successfully.");

			if (homePage.listView.getColumnValueByItemName(dataPool.get("ObjectName"), "Object Type").equals("DOCUMENT COLLECTION"))  
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is of document collection object type.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is right clicked.");

			//Verification : To Verify short key for Members is available in operations menu
			//----------------------------------------------------------------------------------

			//Verifies that Members short cut key is available in operations menu
			if (!homePage.menuBar.IsOperationMenuItemExists("CTRL+SHIFT+L"))
				Log.pass("Test case Passed. Short cut keys (CTRL+SHIFT+L) is not available in operations menu.");
			else
				Log.fail("Test case Failed. Short cut keys (CTRL+SHIFT+L) is available in operations menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_16B

	/**
	 * 24.7.16C : Ctrl+Shift+L should should show members of collection members
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24", "Smoke"}, 
			description = "Ctrl+Shift+L should should show members of collection members.")
	public void SprintTest24_7_16C(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE")|| driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports key actions.");

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the item 
			//-------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.isColumnExists("Object Type")) //Checks and inserts Checked out To Column to the list
				if (!homePage.listView.insertColumn("Object Type"))
					throw new Exception("Column (Object Type) column does not inserted successfully.");

			if (homePage.listView.getColumnValueByItemName(dataPool.get("ObjectName"), "Object Type").equals("DOCUMENT COLLECTION"))  
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is of document collection object type.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is right clicked.");

			//Step-3 Press CTRL+SHIFT+L key
			//-----------------------------
			ActionEventUtils.pressCtrlShiftLKey(driver, "l");

			Log.message("3. CTRL+SHIFT+L Key is pressed.");			

			//Verification : To Verify if Collection members view is opened on clicking CTRL+SHIFT+L key
			//-------------------------------------------------------------------------------------------

			//Verifies that Collection members view is opened on clicking CTRL+SHIFT+L key
			if (homePage.listView.getViewCaption().toUpperCase().contains("COLLECTION MEMBERS"))
				Log.pass("Test case Passed. Collection members view is opened on clicking CTRL+SHIFT+L key.");
			else
				Log.fail("Test case Failed. Collection members view is not opened on clicking CTRL+SHIFT+L key.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_16C


	/**
	 * 24.7.18A : Show sub-objects [Ctrl+J] should not be available in context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "Show subobjects [Ctrl+J] should not be available in context menu.")
	public void SprintTest24_7_18A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE")|| driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports key actions.");

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the item 
			//-------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.isColumnExists("Object Type")) //Checks and inserts Checked out To Column to the list
				if (!homePage.listView.insertColumn("Object Type"))
					throw new Exception("Column (Object Type) column does not inserted successfully.");

			if (homePage.listView.getColumnValueByItemName(dataPool.get("ObjectName"), "Object Type").equals("CUSTOMER"))  

				if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) 
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is right clicked.");

			//Verification : To Verify short key for sub-objects is available in context menu
			//----------------------------------------------------------------------------------

			//Verifies that sub-objects short cut key is available in context menu
			if (!homePage.listView.itemExistsInContextMenu("CTRL+J"))
				Log.pass("Test case Passed. Short cut keys (CTRL+J) is not available in context menu.");
			else
				Log.fail("Test case Failed. Short cut keys (CTRL+J) is available in context menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_18A

	/**
	 * 24.7.18B : Show sub-objects [Ctrl+J] should not be available in operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "Show sub-objects [Ctrl+J] should not be available in operations menu.")
	public void SprintTest24_7_18B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE")|| driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports key actions.");

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the item 
			//-------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.isColumnExists("Object Type")) //Checks and inserts Checked out To Column to the list
				if (!homePage.listView.insertColumn("Object Type"))
					throw new Exception("Column (Object Type) column does not inserted successfully.");

			if (homePage.listView.getColumnValueByItemName(dataPool.get("ObjectName"), "Object Type").equals("DOCUMENT COLLECTION"))  
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is of document collection object type.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") has not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is right clicked.");

			//Verification : To Verify short key for sub-objects is available in operations menu
			//----------------------------------------------------------------------------------

			//Verifies that sub-objects short cut key is available in operations menu
			if (!homePage.menuBar.IsOperationMenuItemExists("CTRL+J"))
				Log.pass("Test case Passed. Short cut keys (CTRL+J) is not available in operations menu.");
			else
				Log.fail("Test case Failed. Short cut keys (CTRL+J) is available in operations menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_18B

	/**
	 * 24.7.18C : Pressing Ctrl+J key should not open sub-objects view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "Pressing Ctrl+J key should not open sub-objects view.")
	public void SprintTest24_7_18C(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE")|| driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports key actions.");

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the item 
			//-------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.isColumnExists("Object Type")) //Checks and inserts Checked out To Column to the list
				if (!homePage.listView.insertColumn("Object Type"))
					throw new Exception("Column (Object Type) column does not inserted successfully.");

			if (homePage.listView.getColumnValueByItemName(dataPool.get("ObjectName"), "Object Type").equals("DOCUMENT COLLECTION"))  
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is of document collection object type.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") has not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-3 Press CTRL+J key
			//-----------------------------
			ActionEventUtils.pressCTRLKey(driver, "j");

			Log.message("3. CTRL+J Key is pressed.");			

			//Verification : To Verify if Sub-objects view is not opened on clicking CTRL+J key
			//-------------------------------------------------------------------------------------

			//Verifies that Sub-objects view is not opened on clicking CTRL+J key
			if (!homePage.listView.getViewCaption().toUpperCase().contains("SUBOBJECTS"))
				Log.pass("Test case Passed. Subobjects view is not opened on clicking CTRL+J key.");
			else
				Log.fail("Test case Failed. Subobjects view is opened on clicking CTRL+J key.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_18C


} //End class ObjectOperations

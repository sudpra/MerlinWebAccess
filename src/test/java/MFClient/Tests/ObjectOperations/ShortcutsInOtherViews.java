package MFClient.Tests.ObjectOperations;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.ActionEventUtils;
import genericLibrary.Log;
import genericLibrary.Utils;
import genericLibrary.WebDriverUtils;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.openqa.selenium.Keys;
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
public class ShortcutsInOtherViews {

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
	 * 24.7.13.1A : Latest version of an object should not be checked out on clicking CTRL+O key in History view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24", "Smoke"}, 
			description = "Latest version of an object should be checked out on clicking CTRL+O key in History view.")
	public void SprintTest24_7_13_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-2 : Open History dialog of the object and select latest version
			//--------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");


			homePage.taskPanel.clickItem("History"); //Selects History from task panel
			Utils.fluentWait(driver);

			if (!homePage.listView.getViewCaption().toUpperCase().equals("HISTORY")) //Checks if History view is opened
				throw new Exception("History view is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Select the latest version of an object in History view
			//---------------------------------------------------------------
			if (ListView.isCheckedOutByItemIndex(driver, 0)) //Checks if object is checked out
				throw new SkipException("Latest version of an object (" + dataPool.get("ObjectName") + ") is already checked out.");

			homePage.listView.clickItemByIndex(0); //Selects the latest version of the object

			Log.message("3. Latest version of an object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-4 :  Click CTRL+O Key
			//---------------------------
			ActionEventUtils.pressCTRLKey(driver, "o");

			Log.message("4. CTRL+O key is pressed.");

			//Verification : To Verify object is not checked out on clicking CTRL+O key
			//-------------------------------------------------------------------------

			//Verifies that Check out column is empty after clicking CTRL+O Key
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Latest version of an object is not checked out on pressing 'CTRL+O' short cut key.");
			else
				Log.fail("Test case Failed. Latest version of an object is checked out on pressing 'CTRL+O' short cut key.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_13.A

	/**
	 * 24.7.13.1B : Latest version of an checked out object should be checked in on clicking CTRL+I key in History view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24", "Smoke"}, 
			description = "Latest version of an checked out object should be checked in on clicking CTRL+I key in History view.")
	public void SprintTest24_7_13_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-2 : Open History dialog of the object and select latest version
			//--------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");


			homePage.taskPanel.clickItem("History"); //Selects History from task panel
			Utils.fluentWait(driver);

			if (!homePage.listView.getViewCaption().toUpperCase().equals("HISTORY")) //Checks if History view is opened
				throw new Exception("History view is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Select the latest version of an object in History view
			//---------------------------------------------------------------
			if (!ListView.isCheckedOutByItemIndex(driver, 0)) {//Checks if object is checked out
				homePage.listView.clickItemByIndex(0);
				homePage.taskPanel.clickItem("Check Out");

				if (!ListView.isCheckedOutByItemIndex(driver, 0))
					throw new SkipException("Latest version of an object (" + dataPool.get("ObjectName") + ") is already checked out.");
			}

			homePage.listView.clickItemByIndex(0); //Selects the latest version of the object

			Log.message("3. Latest version of an checked out object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-4 :  Click CTRL+I Key
			//---------------------------
			ActionEventUtils.pressCTRLKey(driver, "i");

			Log.message("4. CTRL+I key is pressed.");

			//Verification : To Verify object is not checked in on clicking CTRL+I key
			//-------------------------------------------------------------------------

			//Verifies that Check out column is empty after clicking CTRL+I Key
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Latest version of an checked out object is checked in on pressing 'CTRL+I' short cut key.");
			else
				Log.fail("Test case Failed. Latest version of an  checked out object is checked in on pressing 'CTRL+I' short cut key.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_13.1B

	/**
	 * 24.7.13.1E : Latest version of an object should not be deleted on clicking DEL key in History view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "Latest version of an object should not be deleted on clicking DEL key in History view.")
	public void SprintTest24_7_13_1E(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-2 : Open History dialog of the object and select latest version
			//--------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");


			homePage.taskPanel.clickItem("History"); //Selects History from task panel
			Utils.fluentWait(driver);

			if (!homePage.listView.getViewCaption().toUpperCase().equals("HISTORY")) //Checks if History view is opened
				throw new Exception("History view is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Select the latest version of an object in History view
			//---------------------------------------------------------------
			int prevCount = homePage.listView.itemCount();
			homePage.listView.clickItemByIndex(0); //Selects the latest version of the object

			Log.message("3. Latest version of an object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-4 :  Click DEl Key
			//---------------------------
			ActionEventUtils.pressKey(driver, Keys.DELETE);

			Log.message("4. DEL key is pressed.");

			//Verification : To Verify object is not deleted on clicking DEL key
			//-------------------------------------------------------------------------
			if (MFilesDialog.exists(driver))
				throw new Exception("M-Files dialog to Confirm Delete is opened on pressing DEL key in latest version.");

			int currCount = homePage.listView.itemCount();

			//Verifies that number of items in history view is same as after pressing DEL key
			if (prevCount != currCount) {
				Log.fail("Test case Failed. Number of version is not same as after clicking DEL key.", driver);
				return;
			}

			//Verifies that object is not deleted on clicking DEL key
			if (homePage.listView.isItemExists(dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Latest version of an object is not deleted after clicking DEL key.");
			else
				Log.fail("Test case Failed. Latest version of an object is deleted after clicking DEL key.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_13_1E

	/**
	 * 24.7.13.1F : Latest version of an object should be renamed on clicking F2 key in History view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "Latest version of an object should be renamed on clicking F2 key in History view.")
	public void SprintTest24_7_13_1F(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-2 : Open History dialog of the object and select latest version
			//--------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel
			Utils.fluentWait(driver);

			if (!homePage.listView.getViewCaption().toUpperCase().equals("HISTORY")) //Checks if History view is opened
				throw new Exception("History view is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Select the latest version of an object in History view
			//---------------------------------------------------------------
			int prevCount = homePage.listView.itemCount();
			int prevVersion = Integer.parseInt(homePage.listView.getColumnValueByItemIndex(0, "Version"));
			homePage.listView.clickItemByIndex(0); //Selects the latest version of the object

			String extension = "";

			if(dataPool.get("ObjectName").contains("."))
				extension = "." + dataPool.get("ObjectName").split("\\.")[1];

			Log.message("3. Latest version of an object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-4 :  Click F2 Key
			//---------------------------
			ActionEventUtils.pressKey(driver, Keys.F2);

			Log.message("4. F2 key is pressed.");

			//Step-4 : Enter new name in the rename dialog
			//----------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class
			String newName = "SprintTest24_7_9C_" + Utility.getCurrentDateTime(); 

			mfilesDialog.rename(newName, true);
			Utils.fluentWait(driver);

			Log.message("4. Rename dialog is opened and new name (" + newName +") is entered.");

			//Verification : To Verify object is not deleted on clicking DEL key
			//-------------------------------------------------------------------------
			int currCount = homePage.listView.itemCount();
			int currVersion = Integer.parseInt(homePage.listView.getColumnValueByItemIndex(0, "Version"));

			//Verifies that number of items in history view has not increased by 1 after renaming
			if (currCount != prevCount+1) {
				Log.fail("Test case Failed. Number of items in history view has not increased by 1 after renaming.", driver);
				return;
			}

			//Verifies that number of version in history view has not increased by 1 after renaming
			if (currVersion != prevVersion + 1) {
				Log.fail("Test case Failed. Version count in history view has not increased by 1 after renaming.", driver);
				return;
			}

			//Verifies that renamed object exists in History view
			if (homePage.listView.isItemExists(newName+extension))
				Log.pass("Test case Passed. Renamed object exists as latest version in the History view.");
			else
				Log.fail("Test case Failed. Renamed object exists as latest version in the History view.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_13_1F

	/**
	 * 24.7.13.2A : Older version of an object should not be checked out on clicking CTRL+O key in History view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "Older version of an object should not be checked out on clicking CTRL+O key in History view.")
	public void SprintTest24_7_13_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-2 : Open History dialog of the object and select latest version
			//--------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel
			Utils.fluentWait(driver);

			if (!homePage.listView.getViewCaption().toUpperCase().equals("HISTORY")) //Checks if History view is opened
				throw new Exception("History view is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Select the latest version of an object in History view
			//---------------------------------------------------------------
			if (homePage.listView.itemCount() <= 1) 
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not has older versions.");

			if(ListView.isCheckedOutByItemIndex(driver, 0))
				throw new SkipException("Latest verion of an object (" + dataPool.get("ObjectName") + ") is already checked out.");

			homePage.listView.clickItemByIndex(1); //Selects the older version of the object

			Log.message("3. Latest version of an object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-4 :  Click CTRL+O Key
			//---------------------------
			ActionEventUtils.pressCTRLKey(driver, "o");

			Log.message("4. CTRL+O key is pressed.");

			//Verification : To Verify object is not checked out on clicking CTRL+O key
			//-------------------------------------------------------------------------

			//Verifies that Check out column is empty after clicking CTRL+O Key
			if (!ListView.isCheckedOutByItemIndex(driver, 1))
				Log.pass("Test case Passed. Latest version of an object is not checked out on pressing 'CTRL+O' short cut key.");
			else
				Log.fail("Test case Failed. Latest version of an object is checked out on pressing 'CTRL+O' short cut key.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_13_2A

	/**
	 * 24.7.13.2B : Older version of an checked out object should not be checked in on clicking CTRL+I key in History view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "Older version of an checked out object should not be checked in on clicking CTRL+I key in History view.")
	public void SprintTest24_7_13_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-2 : Open History dialog of the object and select latest version
			//--------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel
			Utils.fluentWait(driver);

			if (!homePage.listView.getViewCaption().toUpperCase().equals("HISTORY")) //Checks if History view is opened
				throw new Exception("History view is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Select the latest version of an object in History view
			//---------------------------------------------------------------
			if (homePage.listView.itemCount() <= 1)
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not has older versions.");

			if (!ListView.isCheckedOutByItemIndex(driver, 0)) {//Checks if object is checked out
				homePage.listView.clickItemByIndex(0);
				homePage.taskPanel.clickItem("Check Out");

				if (!ListView.isCheckedOutByItemIndex(driver, 0))
					throw new SkipException("Latest version of an object (" + dataPool.get("ObjectName") + ") is already checked out.");
			}

			if (!homePage.listView.clickItemByIndex(1)) //Selects the older version of the object
				throw new Exception("Older version of an object is not selected.");

			Log.message("3. Older version of an checked out object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-4 :  Click CTRL+I Key
			//---------------------------
			ActionEventUtils.pressCTRLKey(driver, "i");

			Log.message("4. CTRL+I key is pressed.");

			//Verification : To Verify object is not checked in on clicking CTRL+I key
			//-------------------------------------------------------------------------

			//Verifies that Check out column is not empty after clicking CTRL+I Key
			if (ListView.isCheckedOutByItemIndex(driver, 0))
				Log.pass("Test case Passed. Older version of an checked out object is not checked in on pressing 'CTRL+I' short cut key.");
			else
				Log.fail("Test case Failed. Older version of an  checked out object is checked in on pressing 'CTRL+I' short cut key.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_13.2B

	/**
	 * 24.7.13.2E : Older version of an object should not be deleted on clicking DEL key in History view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "Older version of an object should not be deleted on clicking DEL key in History view.")
	public void SprintTest24_7_13_2E(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-2 : Open History dialog of the object
			//------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel
			Utils.fluentWait(driver);

			if (!homePage.listView.getViewCaption().toUpperCase().equals("HISTORY")) //Checks if History view is opened
				throw new Exception("History view is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Select the Older version of an object in History view
			//---------------------------------------------------------------
			int prevCount = homePage.listView.itemCount();
			homePage.listView.clickItemByIndex(1); //Selects the Older version of the object

			Log.message("3. Older version of an object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-4 :  Click DEl Key
			//---------------------------
			ActionEventUtils.pressKey(driver, Keys.DELETE);

			Log.message("4. DEl key is pressed.");

			//Verification : To Verify object is not deleted on clicking DEL key
			//-------------------------------------------------------------------------
			if (MFilesDialog.exists(driver))
				throw new Exception("M-Files dialog is opened on pressing DEL key in older version of an object .");

			int currCount = homePage.listView.itemCount();

			//Verifies that number of item has not increased after clicking DEL key
			if (prevCount != currCount) {
				Log.fail("Test case Failed. Number of version is not same as after clicking DEL key in older version.", driver);
				return;
			}

			//Verifies that object is not deleted on clicking DEL key
			if (homePage.listView.isItemExists(dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Older version of an object is not deleted after clicking DEL key.");
			else
				Log.fail("Test case Failed. Older version of an object is deleted after clicking DEL key.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_13_2E

	/**
	 * 24.7.13.2F : Older version of an object should not be renamed on clicking F2 key in History view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "Older version of an object should not be renamed on clicking F2 key in History view.")
	public void SprintTest24_7_13_2F(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-2 : Open History dialog of the object and select latest version
			//--------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel
			Utils.fluentWait(driver);

			if (!homePage.listView.getViewCaption().toUpperCase().equals("HISTORY")) //Checks if History view is opened
				throw new Exception("History view is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Select the Older version of an object in History view
			//---------------------------------------------------------------
			int prevCount = homePage.listView.itemCount();

			if (prevCount <= 1)
				throw new Exception("No older version of exists for an object.");

			int prevVersion = Integer.parseInt(homePage.listView.getColumnValueByItemIndex(0, "Version"));
			homePage.listView.clickItemByIndex(1); //Selects the Older version of the object

			Log.message("3. Latest version of an object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-4 :  Click F2 Key
			//---------------------------
			ActionEventUtils.pressKey(driver, Keys.F2);

			Log.message("4. F2 key is pressed.");

			//Verification : To Verify object is not renamed on clicking F2 key in older version
			//----------------------------------------------------------------------------------
			if (MFilesDialog.exists(driver)) { //Verifies M-Files Rename dialog has not opened
				Log.fail("Test case Failed. M-Files dialog is opened for older version on clicking F2 key .", driver);
				return;
			}

			int currCount = homePage.listView.itemCount();
			int currVersion = Integer.parseInt(homePage.listView.getColumnValueByItemIndex(0, "Version"));

			//Verifies that number of items in history view has not increased after rename operation has not performed
			if (currCount != prevCount) {
				Log.fail("Test case Failed. Number of items in history view is not same after clicking F2 key.", driver);
				return;
			}

			//Verifies that number of version in history view has not been changed
			if (prevVersion != currVersion) {
				Log.fail("Test case Failed. Version in history view is not same after clicking F2 key.", driver);
				return;
			}

			//Verifies that object is not renamed
			if (homePage.listView.isItemExists(dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Older version in the History view is not renamed.");
			else
				Log.fail("Test case Failed. Older version in the History view is not renamed.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_13_2F

	/**
	 * 24.7.15A : Related object should not be checked out on clicking CTRL+O key in Relationships view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "Latest version of an object should be checked out on clicking CTRL+O key in History view.")
	public void SprintTest24_7_15A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-2 : Open Relationship view of the object and select any object
			//--------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu("Relationships"); //Selects History from task panel
			Utils.fluentWait(driver);

			if (!homePage.listView.getViewCaption().toUpperCase().contains("RELATIONSHIPS")) //Checks if History view is opened
				throw new Exception("Relationship view is not opened.");

			if (ListView.isCheckedOutByItemIndex(driver, 0))
				throw new SkipException("First object in Relationships is already checked out.");

			homePage.listView.clickItemByIndex(0); //Selects the first related object

			Log.message("2. Relationship view of an object (" + dataPool.get("ObjectName") + ") is opened and object is selected.");

			//Step-4 :  Click CTRL+O Key
			//---------------------------
			ActionEventUtils.pressCTRLKey(driver, "o");

			Log.message("4. CTRL+O key is pressed.");

			//Verification : To Verify object is not checked out on clicking CTRL+O key
			//-------------------------------------------------------------------------

			//Verifies that Check out column is empty after clicking CTRL+O Key
			if (!ListView.isCheckedOutByItemIndex(driver, 0))
				Log.pass("Test case Passed. Object in relationships view is not checked out on pressing 'CTRL+O' short cut key.");
			else
				Log.fail("Test case Failed. Object in relationships view is checked out on pressing 'CTRL+O' short cut key.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_15A

	/**
	 * 24.7.15B : CTRL+I should check in the object in Relationship view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "CTRL+I should check in the object in Relationship view.")
	public void SprintTest24_7_15B(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-2 : Open Relationship view of the object and select any object
			//--------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu("Relationships"); //Selects History from task panel
			Utils.fluentWait(driver);

			if (!homePage.listView.getViewCaption().toUpperCase().contains("RELATIONSHIPS")) //Checks if History view is opened
				throw new Exception("Relationship view is not opened.");

			Log.message("2. Relationship view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Select a related object from relationship view
			//-------------------------------------------------------
			if (!ListView.isCheckedOutByItemIndex(driver, 0)) {//Checks if object is checked out
				homePage.listView.clickItemByIndex(0);
				homePage.taskPanel.clickItem("Check Out");

				if (!ListView.isCheckedOutByItemIndex(driver, 0))
					throw new Exception("Related object is not in checked out mode to perform check in operation.");
			}

			homePage.listView.clickItemByIndex(0);

			Log.message("3. Object is selected from the relationship view.");

			//Step-4 :  Click CTRL+I Key
			//---------------------------
			ActionEventUtils.pressCTRLKey(driver, "i");

			Log.message("4. CTRL+I key is pressed.");

			//Verification : To Verify object is not checked in on clicking CTRL+I key
			//-------------------------------------------------------------------------

			//Verifies that Check out column is empty after clicking CTRL+I Key
			if (!ListView.isCheckedOutByItemIndex(driver, 1))
				Log.pass("Test case Passed. Object in Relationships view is checked in on pressing 'CTRL+I' short cut key.");
			else
				Log.fail("Test case Failed. Object in Relationships view is checked in on pressing 'CTRL+I' short cut key.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_15B

	/**
	 * 24.7.15E : DEL key should delete the object in Relationship view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "DEL key should delete the object in Relationship view.")
	public void SprintTest24_7_15E(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-2 : Open Relationship view of the object and select any object
			//--------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu("Relationships"); //Selects History from task panel
			Utils.fluentWait(driver);

			if (!homePage.listView.getViewCaption().toUpperCase().contains("RELATIONSHIPS")) //Checks if History view is opened
				throw new Exception("Relationship view is not opened.");

			int prevCount = homePage.listView.itemCount();
			String objName = homePage.listView.getItemNameByItemIndex(0);

			if (!homePage.listView.clickItemByIndex(0)) //Selects the first related object
				throw new Exception("Related object is not selected.");

			Utils.fluentWait(driver);

			Log.message("2. Relationship view of an object (" + dataPool.get("ObjectName") + ") is opened and object is selected.");

			//Step-3 :  Click DEl Key
			//---------------------------
			ActionEventUtils.pressKey(driver, Keys.DELETE);

			Log.message("3. DEL key is pressed.");

			//Verification : To Verify object is not deleted on clicking DEL key
			//-------------------------------------------------------------------------
			if (!MFilesDialog.exists(driver))
				throw new Exception("M-Files dialog to Confirm Delete is not opened.");

			MFilesDialog mfilesDialog = new MFilesDialog(driver);

			if (!mfilesDialog.confirmDelete()) //Confirms delete operation
				throw new Exception("Confirm Delete operation is not successful.");

			int currCount = homePage.listView.itemCount();

			//Verifies that number of items in Relationships view has decreased by 1 after deleting related object
			if (currCount != prevCount - 1) {
				Log.fail("Test case Failed. Number of items in related object is not decreased by 1 after deleting object.", driver);
				return;
			}

			//Verifies that object is not deleted on clicking DEL key
			if (!homePage.listView.isItemExists(objName))
				Log.pass("Test case Passed. Object is deleted after clicking DEL key in relationships view.");
			else
				Log.fail("Test case Failed. Object is not deleted after clicking DEL key in relationships view.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_15E

	/**
	 * 24.7.15F : F2 should rename the object in Relationship view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "F2 should rename the object in Relationship view")
	public void SprintTest24_7_15F(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-2 : Open Relationship view of the object and select any object
			//--------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu("Relationships"); //Selects History from task panel
			Utils.fluentWait(driver);

			if (!homePage.listView.getViewCaption().toUpperCase().contains("RELATIONSHIPS")) //Checks if History view is opened
				throw new Exception("Relationship view is not opened.");

			int prevCount = homePage.listView.itemCount();
			homePage.listView.clickItemByIndex(0); //Selects the first related object

			Log.message("2. Relationship view of an object (" + dataPool.get("ObjectName") + ") is opened and object is selected.");

			//Step-4 :  Click F2 Key
			//---------------------------
			ActionEventUtils.pressKey(driver, Keys.F2);

			Log.message("4. F2 key is pressed.");

			//Step-4 : Enter new name in the rename dialog
			//----------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class
			String newName = "SprintTest24_7_9C_" + Utility.getCurrentDateTime(); 

			mfilesDialog.rename(newName, true);
			Utils.fluentWait(driver);

			Log.message("4. Rename dialog is opened and new name (" + newName +") is entered.");

			//Verification : To Verify object is not deleted on clicking DEL key
			//-------------------------------------------------------------------------
			int currCount = homePage.listView.itemCount();

			//Verifies that number of items in relationship view has no change after renaming
			if (currCount != prevCount) {
				Log.fail("Test case Failed. Number of items in relationships view is not as same after deleting.", driver);
				return;
			}

			//Verifies that renamed object exists in Relationships view
			if (homePage.listView.isItemExists(newName))
				Log.pass("Test case Passed. Renamed object exists as latest version in the Relationships view.");
			else
				Log.fail("Test case Failed. Renamed object exists as latest version in the Relationships view.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_15F

	/**
	 * 24.7.17A : Object should not be checked out on clicking CTRL+O key in Collection members view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "Object should not be checked out on clicking CTRL+O key in Collection members view")
	public void SprintTest24_7_17A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-2 : Open Collection Members view of the object and select any object
			//------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");


			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("Show Members"); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!homePage.listView.getViewCaption().toUpperCase().contains("COLLECTION MEMBERS")) //Checks if Collection members view is opened
				throw new Exception("Collection Members view is not opened.");

			if (ListView.isCheckedOutByItemIndex(driver, 0))
				throw new Exception("Member object is already in checked out mode to perform check in operation.");

			homePage.listView.clickItemByIndex(0); //Selects the first related object

			Log.message("2. Collection Members view of an object (" + dataPool.get("ObjectName") + ") is opened and object is selected.");

			//Step-4 :  Click CTRL+O Key
			//---------------------------
			ActionEventUtils.pressCTRLKey(driver, "o");

			Log.message("4. CTRL+O key is pressed.");

			//Verification : To Verify object is not checked out on clicking CTRL+O key
			//-------------------------------------------------------------------------

			//Verifies that Check out column is empty after clicking CTRL+O Key
			if (!ListView.isCheckedOutByItemIndex(driver, 0))
				Log.pass("Test case Passed. Object in Collection Members view is not checked out on pressing 'CTRL+O' short cut key.");
			else
				Log.fail("Test case Failed. Object in Collection Members view is checked out on pressing 'CTRL+O' short cut key.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_17A

	/**
	 * 24.7.17B : CTRL+I should check in the object in collection members view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "CTRL+I should check in the object in collection members view.")
	public void SprintTest24_7_17B(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-2 : Open Collection Members view of the object and select any object
			//--------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");


			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("Show Members"); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!homePage.listView.getViewCaption().toUpperCase().contains("COLLECTION MEMBERS")) //Checks if Collection members view is opened
				throw new Exception("Collection Members view is not opened.");

			Log.message("2. Collection Members view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Select an object from collection members view
			//-------------------------------------------------------
			if (!ListView.isCheckedOutByItemIndex(driver, 0)) {//Checks if object is checked out
				homePage.listView.clickItemByIndex(0);
				homePage.taskPanel.clickItem("Check Out");

				if (!ListView.isCheckedOutByItemIndex(driver, 0))
					throw new Exception("Member object is not in checked out mode to perform check in operation.");
			}

			homePage.listView.clickItemByIndex(0);

			Log.message("3. Object is selected from the relationship view.");

			//Step-4 :  Click CTRL+I Key
			//---------------------------
			ActionEventUtils.pressCTRLKey(driver, "i");

			Log.message("4. CTRL+I key is pressed.");

			//Verification : To Verify object is not checked in on clicking CTRL+I key
			//-------------------------------------------------------------------------

			//Verifies that Check out column is empty after clicking CTRL+I Key
			if (!ListView.isCheckedOutByItemIndex(driver, 1))
				Log.pass("Test case Passed. Object in Collection members view is checked in on pressing 'CTRL+I' short cut key.");
			else
				Log.fail("Test case Failed. Object in Collection members view is checked in on pressing 'CTRL+I' short cut key.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_17B

	/**
	 * 24.7.17E : DEL key should delete the object in Collection Members view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "DEL key should delete the object in Collection members view.")
	public void SprintTest24_7_17E(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-2 : Open Collection Members view of the object and select any object
			//--------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");


			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("Show Members"); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!homePage.listView.getViewCaption().toUpperCase().contains("COLLECTION MEMBERS")) //Checks if Collection members view is opened
				throw new Exception("Collection Members view is not opened.");

			int prevCount = homePage.listView.itemCount();
			String objName = homePage.listView.getItemNameByItemIndex(0);
			homePage.listView.clickItemByIndex(0); //Selects the first related object

			Log.message("2. Collection members view of an object (" + dataPool.get("ObjectName") + ") is opened and object is selected.");

			//Step-3 :  Click DEL Key
			//---------------------------
			ActionEventUtils.pressKey(driver, Keys.DELETE);

			Log.message("3. DEL key is pressed.");

			//Verification : To Verify object is not deleted on clicking DEL key
			//-------------------------------------------------------------------------
			if (!MFilesDialog.exists(driver))
				throw new Exception("M-Files dialog to Confirm Delete is not opened.");

			MFilesDialog mfilesDialog = new MFilesDialog(driver);

			if (!mfilesDialog.confirmDelete()) //Confirms delete operation
				throw new Exception("Confirm Delete operation is not successful.");

			int currCount = homePage.listView.itemCount();

			if (currCount != prevCount - 1) { //Verifies that number of items in Relationships view has decreased by 1 after deleting related object
				Log.fail("Test case Failed. Number of items in related object is not decreased by 1 after deleting object.", driver);
				return;
			}

			//Verifies that object is not deleted on clicking DEL key
			if (!homePage.listView.isItemExists(objName))
				Log.pass("Test case Passed. Object is deleted after clicking DEL key in Collection Members view.");
			else
				Log.fail("Test case Failed. Object is not deleted after clicking DEL key in Collection members view.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_17E

	/**
	 * 24.7.17F : F2 should rename the object in Collection Members view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "F2 should rename the object in Collection Members view")
	public void SprintTest24_7_17F(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-2 : Open Collection Members view of the object and select any object
			//--------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");


			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("Show Members"); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!homePage.listView.getViewCaption().toUpperCase().contains("COLLECTION MEMBERS")) //Checks if Collection members view is opened
				throw new Exception("Collection Members view is not opened.");

			int prevCount = homePage.listView.itemCount();
			homePage.listView.clickItemByIndex(0); //Selects the first related object

			String extension = "";

			if(homePage.listView.getSelectedListViewItem().contains("."))
				extension = "." + homePage.listView.getSelectedListViewItem().split("\\.")[1];

			Log.message("2. Collection Members view of an object (" + dataPool.get("ObjectName") + ") is opened and object is selected.");

			//Step-4 :  Click F2 Key
			//---------------------------
			ActionEventUtils.pressKey(driver, Keys.F2);

			Log.message("4. F2 key is pressed.");

			//Step-4 : Enter new name in the rename dialog
			//----------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class
			String newName = "SprintTest24_7_17F_" + Utility.getCurrentDateTime(); 

			mfilesDialog.rename(newName, true);
			Utils.fluentWait(driver);

			Log.message("4. Rename dialog is opened and new name (" + newName +") is entered.");

			//Verification : To Verify object is not deleted on clicking DEL key
			//-------------------------------------------------------------------------
			int currCount = homePage.listView.itemCount();

			//Verifies that number of items in relationship view has no change after renaming
			if (currCount != prevCount) {
				Log.fail("Test case Failed. Number of items in collection members view is not as same after deleting.", driver);
				return;
			}

			//Verifies that renamed object exists in Relationships view
			if (homePage.listView.isItemExists(newName+extension))
				Log.pass("Test case Passed. Renamed object exists as latest version in the Collection Members view.");
			else
				Log.fail("Test case Failed. Renamed object exists as latest version in the Collection members view.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_1F


	/**
	 * 24.7.19A : Object should not be checked out on clicking CTRL+O key in Sub-Objects view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "Object should not be checked out on clicking CTRL+O key in Sub-Objects view")
	public void SprintTest24_7_19A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-2 : Open Sub-Objects view of the object and select any object
			//------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");


			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.ShowSubObjects.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!homePage.listView.getViewCaption().toUpperCase().contains("SUBOBJECTS")) //Checks if Collection members view is opened
				throw new Exception("Sub-Objects view is not opened.");

			if (ListView.isCheckedOutByItemIndex(driver, 0))
				throw new SkipException("Sub-object is already checked out mode to perform check out operation.");

			homePage.listView.clickItemByIndex(0); //Selects the first related object

			Log.message("2. Sub-Objects view of an object (" + dataPool.get("ObjectName") + ") is opened and object is selected.");

			//Step-3 : Click CTRL+O Key
			//---------------------------
			ActionEventUtils.pressCTRLKey(driver, "o");

			Log.message("3. CTRL+O key is pressed.");

			//Verification : To Verify object is not checked out on clicking CTRL+O key
			//-------------------------------------------------------------------------

			//Verifies that Check out column is empty after clicking CTRL+O Key
			if (!ListView.isCheckedOutByItemIndex(driver, 0))
				Log.pass("Test case Passed. Object in Sub-Objects view is not checked out on pressing 'CTRL+O' short cut key.");
			else
				Log.fail("Test case Failed. Object in Sub-objects view is checked out on pressing 'CTRL+O' short cut key.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_19A

	/**
	 * 24.7.19B : CTRL+I should check in the object in Sub-Objects view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "CTRL+I should check in the object in Sub-Objects view.")
	public void SprintTest24_7_19B(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-2 : Open Sub-Objects view of the object and select any object
			//--------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");


			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.ShowSubObjects.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!homePage.listView.getViewCaption().toUpperCase().contains("SUBOBJECTS")) //Checks if Collection members view is opened
				throw new Exception("Sub-Objects view is not opened.");

			Log.message("2. Sub-Objects view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Select an object from Sub-Objects view
			//-------------------------------------------------------
			if (!ListView.isCheckedOutByItemIndex(driver, 0)) {//Checks if object is checked out
				homePage.listView.clickItemByIndex(0);
				homePage.taskPanel.clickItem("Check Out");

				if (!ListView.isCheckedOutByItemIndex(driver, 0))
					throw new Exception("Sub object is not in checked out mode to perform check in operation.");
			}

			homePage.listView.clickItemByIndex(0);

			Log.message("3. Object is selected from the Sub-Objects view.");

			//Step-4 :  Click CTRL+I Key
			//---------------------------
			ActionEventUtils.pressCTRLKey(driver, "i");

			Log.message("4. CTRL+I key is pressed.");

			//Verification : To Verify object is not checked in on clicking CTRL+I key
			//-------------------------------------------------------------------------

			//Verifies that Check out column is empty after clicking CTRL+I Key
			if (!ListView.isCheckedOutByItemIndex(driver, 0))
				Log.pass("Test case Passed. Object in Sub-Objects view is checked in on pressing 'CTRL+I' short cut key.");
			else
				Log.fail("Test case Failed. Object in Sub-Objects view is not checked in on pressing 'CTRL+I' short cut key.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_19B

	/**
	 * 24.7.19E : DEL key should delete the object in Sub-Objects view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24", "Smoke"}, 
			description = "DEL key should delete the object in Sub-Objects view.")
	public void SprintTest24_7_19E(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-2 : Open Sub-Objects view of the object and select any object
			//--------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");


			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.ShowSubObjects.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!homePage.listView.getViewCaption().toUpperCase().contains("SUBOBJECTS")) //Checks if Collection members view is opened
				throw new Exception("Sub-Objects view is not opened.");

			int prevCount = homePage.listView.itemCount();
			homePage.listView.clickItemByIndex(0); //Selects the first related object

			Log.message("2. Sub-Objects view of an object (" + dataPool.get("ObjectName") + ") is opened and object is selected.");

			//Step-3 :  Click DEL Key
			//---------------------------
			ActionEventUtils.pressKey(driver, Keys.DELETE);

			Log.message("3. DEL key is pressed.");

			//Verification : To Verify object is not deleted on clicking DEL key
			//-------------------------------------------------------------------------
			if (!MFilesDialog.exists(driver))
				throw new Exception("M-Files dialog to Confirm Delete is not opened.");

			MFilesDialog mfilesDialog = new MFilesDialog(driver);

			if (!mfilesDialog.confirmDelete()) //Confirms delete operation
				throw new Exception("Confirm Delete operation is not successful.");

			Utils.fluentWait(driver);

			int currCount = homePage.listView.itemCount();

			//Verifies that number of items in Sub-Objects view has decreased by 1 after deleting sub object
			if (currCount != prevCount - 1) {
				Log.fail("Test case Failed. Number of items in related object is not decreased by 1 after deleting object.", driver);
				return;
			}

			//Verifies that object is not deleted on clicking DEL key
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is deleted after clicking DEL key in Sub-Objects view.");
			else
				Log.fail("Test case Failed. Object is not deleted after clicking DEL key in Sub-Objects view.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_19E

	/**
	 * 24.7.19F : F2 should rename the object in Sub-objects view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "F2 should rename the object in Sub-Objects view")
	public void SprintTest24_7_19F(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-2 : Open Sub-Objects view of the object and select any object
			//--------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");


			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.ShowSubObjects.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!homePage.listView.getViewCaption().toUpperCase().contains("SUBOBJECTS")) //Checks if Collection members view is opened
				throw new Exception("Sub-Objects view is not opened.");

			homePage.listView.clickItemByIndex(0); //Selects the first Sub-Object

			Log.message("2. Sub-Objects view of an object (" + dataPool.get("ObjectName") + ") is opened and object is selected.");

			//Step-3 :  Click F2 Key
			//---------------------------
			ActionEventUtils.pressKey(driver, Keys.F2);

			Log.message("3. F2 key is pressed.");

			//Step-4 : Enter new name in the rename dialog
			//----------------------------------------------
			if (!MFilesDialog.exists(driver, Caption.MenuItems.Rename.Value))
				throw new Exception("Rename dialog is not opened while pressing F2 Key in the view for the selected sub object.");

			MFilesDialog mfilesDialog = new MFilesDialog(driver, Caption.MenuItems.Rename.Value); //Instantiating MFilesDialog wrapper class
			String newName = "SprintTest24_7_19F_" + Utility.getCurrentDateTime(); 
			try {mfilesDialog.rename(newName, true);}catch(Exception e0){}
			Utils.fluentWait(driver);

			Log.message("4. Rename dialog is opened and new name (" + newName +") is entered.");

			//Verifies that renamed object exists in Relationships view
			if (!MFilesDialog.exists(driver, "M-Files Web"))
				throw new Exception("Warning dialog is not displayed while rename the object which has automatic name.");

			mfilesDialog = new MFilesDialog(driver, "M-Files Web");//Instantiates the mfiles dialog

			if (mfilesDialog.getMessage().equalsIgnoreCase(dataPool.get("WarningMessage")))
				Log.pass("Test case Passed. Renamed dialog is opened while pressing F2 key in Sub-Objects view and Expected warning dialog is displayed while trying to rename the object which has automatic name vlaue for it.", driver);
			else
				Log.fail("Test case Failed. Renamed dialog is opened while pressing F2 key in Sub-Objects view but Expected warning dialog with message('" + dataPool.get("WarningMessage") + "') is not displayed while trying to rename the object which has automatic name vlaue for it..[Actual value : '" + mfilesDialog.getMessage()  + "']", driver);

			mfilesDialog.close();
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_19F


} //End class ObjectOperations

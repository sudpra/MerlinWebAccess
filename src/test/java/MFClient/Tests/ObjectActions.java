package MFClient.Tests;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.Utils;
import genericLibrary.WebDriverUtils;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
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
public class ObjectActions {

	public String xlTestDataWorkBook = null;
	public String loginURL = null;
	public String userName = null;
	public String password = null;
	public String testVault = null;
	public String className = null;
	public String productVersion = null;
	public WebDriver driver = null;
	public String userFullName = null;
	public String methodName = null;
	public String driverType = null;

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
	 * 24.7.9A : Rename [F2] should be available in context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint24"}, 
			description = "Rename [F2] should be available in context menu.")
	public void SprintTest24_7_9A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Verification : To Verify short key for Rename is available in context menu
			//---------------------------------------------------------------------------

			//Verifies that Rename short cut key is available in context menu
			if (homePage.listView.itemExistsInContextMenu("F2"))
				Log.pass("Test case Passed. Short cut keys (F2) is available for rename operation in context menu.");
			else
				Log.fail("Test case Failed. Short cut keys (F2) is not available for rename operation in context menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_9A

	/**
	 * 24.7.9B : Rename [F2] should be available in operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint24"}, 
			description = "Rename [F2] should be available in operations menu.")
	public void SprintTest24_7_9B(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Verification : To Verify short key for Rename is available in context menu
			//---------------------------------------------------------------------------

			//Verifies that Rename short cut key is available in operations menu
			if (homePage.menuBar.IsOperationMenuItemExists("F2"))
				Log.pass("Test case Passed. Short cut keys (F2) is available for rename operation in operations menu.");
			else
				Log.fail("Test case Failed. Short cut keys (F2) is not available for rename operation in operations menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_9B

	/**
	 * 24.7.9C : Rename the object by pressing F2 key
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24"}, 
			description = "Rename the object by pressing F2 key.")
	public void SprintTest24_7_9C(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("SAFARI") || driverType.equalsIgnoreCase("IE"))
			throw new SkipException(driverType.toUpperCase()+" does not support key actions");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the item from the list
			//--------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			String extension = "";

			if(dataPool.get("ObjectName").contains("."))
				extension = "." + dataPool.get("ObjectName").split("\\.")[1];


			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-3 : Press F2 Key
			//---------------------
			Actions action = new Actions(driver);
			action.sendKeys(Keys.F2).perform();
			Utils.fluentWait(driver);

			Log.message("3. F2 Key is pressed.");

			//Step-4 : Enter new name in the rename dialog
			//----------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class
			mfilesDialog.rename(dataPool.get("NewName"), true);
			Utils.fluentWait(driver);

			Log.message("4. Rename dialog is opened and new name (" + (dataPool.get("NewName").replaceAll("<", "{")).replaceAll(">", "}") +") is entered. (NOTE: For Script value '<' is replaced with '{' and '>' is replaced with '}')");

			//Verification : To Verify short key for Rename is available in context menu
			//---------------------------------------------------------------------------

			//Verifies that Rename short cut key is available in operations menu
			if (homePage.listView.isItemExists(dataPool.get("NewName")+extension))
				Log.pass("Test case Passed. Object is renamed successfully by pressing F2 key.");
			else
				Log.fail("Test case Failed. Object is not renamed successfully by pressing F2 key.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_9C

	/**
	 * 24.7.10A : Delete [Del] should be available in context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint24"}, 
			description = "Delete [Del] should be available in context menu.")
	public void SprintTest24_7_10A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Verification : To Verify short key for Delete is available in context menu
			//---------------------------------------------------------------------------

			//Verifies that Delete short cut key is available in context menu
			if (homePage.listView.itemExistsInContextMenu("DEL"))
				Log.pass("Test case Passed. Short cut keys (DEL) is available for delete operation in context menu.");
			else
				Log.fail("Test case Failed. Short cut keys (DEL) is not available for delete operation in context menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_10A

	/**
	 * 24.7.10B : Delete [Del] should be available in operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint24"}, 
			description = "Delete [Del] should be available in operations menu.")
	public void SprintTest24_7_10B(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Verification : To Verify short key for Delete is available in context menu
			//---------------------------------------------------------------------------

			//Verifies that Delete short cut key is available in operations menu
			if (homePage.menuBar.IsOperationMenuItemExists("DEL"))
				Log.pass("Test case Passed. Short cut keys (DEL) is available for delete operation in operations menu.");
			else
				Log.fail("Test case Failed. Short cut keys (DEL) is not available for delete operation in operations menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_10B

	/**
	 * 24.7.10C : Delete object using by pressing DEL key
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24", "Smoke"}, 
			description = "Delete object using by pressing DEL key.")
	public void SprintTest24_7_10C(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("SAFARI") || driverType.equalsIgnoreCase("IE"))
			throw new SkipException(driverType.toUpperCase()+" does not support key actions");

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

			int prevCt = homePage.listView.itemCount();

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-3 Press DEL key
			//--------------------
			Actions action = new Actions(driver);
			action.sendKeys(Keys.DELETE).perform();
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.confirmDelete()) //Clicks Yes in the confirmation dialog to perform delete operation
				throw new SkipException("Confirmation dialog has not appeared after pressing DEL key");

			Log.message("3. DEL Key is pressed.");

			//Verification : To Verify short key for Delete is available in context menu
			//---------------------------------------------------------------------------
			int currCt = homePage.listView.itemCount(); //Gets the number of object in the list after deletion

			if (prevCt != currCt + 1) { //Verifies if number of objects in the list has reduced by 1
				Log.fail("No of objects has not reduced by 1 after deleting document by DEL key.", driver);
				return;
			}


			if(homePage.listView.isItemExists(dataPool.get("ObjectName"))){
				Log.fail("Test case Failed. Object (" + dataPool.get("ObjectName") + ") exists after clicking DEL key.", driver);
				return;	
			}

			Log.message("4. Checked that  '" + dataPool.get("ObjectName") + "' does not exist in the list.");

			homePage.taskPanel.clickItem("Home");

			String viewToNavigate2 = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("5. Navigated back to home view and then again to '" + viewToNavigate2 + "' view.");

			//Verifies that object is not found in search after delete
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object (" + dataPool.get("ObjectName") + ") is deleted using DEL key.");
			else
				Log.fail("Test case Failed. Object (" + dataPool.get("ObjectName") + ") exists after clicking DEL key.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest24_7_10C

	/**
	 * 32.3.6.1A : Rename should not be possible for checked out object - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Rename should not be possible for checked out object - Operations menu")
	public void SprintTest32_3_6_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform checkout operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Verification : To Verify if Rename is not possible for checked out objects 
			//---------------------------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			//Verifies that Rename option is enabled in operations menu
			if (!homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.Rename.Value))
				Log.pass("Test case Passed. Rename through operations menu is not possible with checked out object.");
			else
				Log.fail("Test case Failed. Rename is enabled operations menu for checked out object.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_6_1A

	/**
	 * 32.3.6.1B : Rename should not be possible for checked out object - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Rename should not be possible for checked out object - Context menu")
	public void SprintTest32_3_6_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform checkout operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Verification : To Verify if Rename is not possible for checked out objects 
			//---------------------------------------------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Right clicks the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			//Verifies that Rename option is enabled in operations menu
			if (!homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.Rename.Value))
				Log.pass("Test case Passed. Rename through context menu is not possible with checked out object.");
			else
				Log.fail("Test case Failed. Rename is enabled in context menu for checked out object.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_6_1B

	/**
	 * 32.3.6.1C : Rename should not be possible for checked out object - F2 Key
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint32"}, 
			description = "Rename should not be possible for checked out object - F2 Key")
	public void SprintTest32_3_6_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("SAFARI") || driverType.equalsIgnoreCase("IE"))
			throw new SkipException(driverType.toUpperCase()+" does not support key actions");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform checkout operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Verification : To Verify if Rename is not possible for checked out objects 
			//---------------------------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Right clicks the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Actions action = new Actions(driver);
			action.sendKeys(Keys.F2).perform();
			Utils.fluentWait(driver);

			//Verifies that Rename option is enabled in operations menu
			if (!MFilesDialog.exists(driver))
				Log.pass("Test case Passed. Rename by pressing F2 key is not possible with checked out object.");
			else
				Log.fail("Test case Failed. Rename dialog got opened on pressing F2 key.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_6_1C

	/**
	 * 32.3.6.2A : Rename the object after performing undo-checkout - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Rename the object after performing undo-checkout - Operations menu")
	public void SprintTest32_3_6_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform checkout operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-3 : Perform Undo-Checkout operation
			//----------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo-checkout operation

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not undo checked out.");

			Log.message("3. Object (" + dataPool.get("ObjectName") + ") is undo-checked out.");

			//Step-4 : Rename the undo-checked out object
			//-------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			String extension = "";

			if(dataPool.get("ObjectName").contains("."))
				extension = "." + dataPool.get("ObjectName").split("\\.")[1];

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Rename.Value); //Selects Rename from Operations menu
			Utils.fluentWait(driver);

			mfilesDialog = new MFilesDialog(driver);
			String newName = Utility.getObjectName(Utility.getMethodName());
			mfilesDialog.rename(newName, true); //Renames the object

			Log.message("4. Object (" + dataPool.get("ObjectName") + ") is clicked the Rename from operations menu and new name is entered.");

			//Verification : To Verify if renamed undo-checked out object exists in the list
			//---------------------------------------------------------------------------			
			if (homePage.listView.isItemExists(newName+extension))
				Log.pass("Test case Passed. Undo-Checkedout object is renamed successfully.");
			else
				Log.fail("Test case Failed. Renamed Undo-Checkedout object does not exists.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_6_2A

	/**
	 * 32.3.6.2B : Rename the object after performing undo-checkout - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Rename the object after performing undo-checkout - Context menu")
	public void SprintTest32_3_6_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform checkout operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-3 : Perform Undo-Checkout operation
			//----------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo-checkout operation

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not undo checked out.");

			Log.message("3. Object (" + dataPool.get("ObjectName") + ") is undo-checked out.");

			//Step-4 : Rename the undo-checked out object through context menu
			//----------------------------------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got right clicked.");

			String extension = "";

			if(dataPool.get("ObjectName").contains("."))
				extension = "." + dataPool.get("ObjectName").split("\\.")[1];

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Rename.Value); //Selects Rename from context menu
			Utils.fluentWait(driver);

			mfilesDialog = new MFilesDialog(driver);
			String newName = Utility.getObjectName(Utility.getMethodName());
			mfilesDialog.rename(newName, true); //Renames the object

			Log.message("4. Object (" + dataPool.get("ObjectName") + ") is clicked the Rename from context menu and new name is entered.");

			//Verification : To Verify if renamed undo-checked out object exists in the list
			//---------------------------------------------------------------------------			
			if (homePage.listView.isItemExists(newName+extension))
				Log.pass("Test case Passed. Undo-Checkedout object is renamed successfully.");
			else
				Log.fail("Test case Failed. Renamed Undo-Checkedout object does not exists.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_6_2B

	/**
	 * 32.3.6.2C : Rename the object after performing undo-checkout - 'F2' Key
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint32"}, 
			description = "Rename the object after performing undo-checkout - 'F2' Key")
	public void SprintTest32_3_6_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("SAFARI") || driverType.equalsIgnoreCase("IE"))
			throw new SkipException(driverType.toUpperCase()+" does not support key actions");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform checkout operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-3 : Perform Undo-Checkout operation
			//----------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo-checkout operation

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not undo checked out.");

			Log.message("3. Object (" + dataPool.get("ObjectName") + ") is undo-checked out.");

			//Step-4 : Rename the undo-checked out object through F2 key
			//----------------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got clicked.");

			String extension = "";

			if(dataPool.get("ObjectName").contains("."))
				extension = "." + dataPool.get("ObjectName").split("\\.")[1];

			Actions action = new Actions(driver);
			action.sendKeys(Keys.F2).perform();
			Utils.fluentWait(driver);

			mfilesDialog = new MFilesDialog(driver);
			String newName = Utility.getObjectName(Utility.getMethodName());
			mfilesDialog.rename(newName, true); //Renames the object

			Log.message("4. Object (" + dataPool.get("ObjectName") + ") is clicked the Rename from F2 key and new name is entered.");

			//Verification : To Verify if renamed undo-checked out object exists in the list
			//---------------------------------------------------------------------------			
			if (homePage.listView.isItemExists(newName+extension))
				Log.pass("Test case Passed. Undo-Checkedout object is renamed successfully.");
			else
				Log.fail("Test case Failed. Renamed Undo-Checkedout object does not exists.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_6_2C

	/**
	 * 32.3.6.3A : Rename the object after performing Check-in - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Rename the object after performing Check-in - Operations menu")
	public void SprintTest32_3_6_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform checkout operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-3 : Perform Check-in operation
			//-------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not undo checked out.");

			Log.message("3. Object (" + dataPool.get("ObjectName") + ") is Checked in.");

			//Step-4 : Rename the undo-checked out object
			//-------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			String extension = "";

			if(dataPool.get("ObjectName").contains("."))
				extension = "." + dataPool.get("ObjectName").split("\\.")[1];

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Rename.Value); //Selects Rename from Operations menu
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			String newName = Utility.getObjectName(Utility.getMethodName());
			mfilesDialog.rename(newName, true); //Renames the object

			Log.message("4. Object (" + dataPool.get("ObjectName") + ") is clicked the Rename from operations menu and new name is entered.");

			//Verification : To Verify if renamed undo-checked out object exists in the list
			//---------------------------------------------------------------------------			
			if (homePage.listView.isItemExists(newName+extension))
				Log.pass("Test case Passed. Undo-Checkedout object is renamed successfully.");
			else
				Log.fail("Test case Failed. Renamed Undo-Checkedout object does not exists.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_6_3A

	/**
	 * 32.3.6.3B : Rename the object after performing Check-In - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Rename the object after performing undo-checkout - Context menu")
	public void SprintTest32_3_6_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform checkout operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-3 : Perform Check-in operation
			//-------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not undo checked out.");

			Log.message("3. Object (" + dataPool.get("ObjectName") + ") is Checked in.");

			//Step-4 : Rename the checked in object through context menu
			//-----------------------------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got right clicked.");

			String extension = "";

			if(dataPool.get("ObjectName").contains("."))
				extension = "." + dataPool.get("ObjectName").split("\\.")[1];

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Rename.Value); //Selects Rename from context menu
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			String newName = Utility.getObjectName(Utility.getMethodName());
			mfilesDialog.rename(newName, true); //Renames the object

			Log.message("4. Object (" + dataPool.get("ObjectName") + ") is clicked the Rename from context menu and new name is entered.");

			//Verification : To Verify if renamed checked-in out object exists in the list
			//---------------------------------------------------------------------------			
			if (homePage.listView.isItemExists(newName+extension))
				Log.pass("Test case Passed. Undo-Checkedout object is renamed successfully.");
			else
				Log.fail("Test case Failed. Renamed Undo-Checkedout object does not exists.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_6_3B

	/**
	 * 32.3.6.3C : Rename the object after performing check-in - 'F2' Key
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint32"}, 
			description = "Rename the object after performing check-in - 'F2' Key")
	public void SprintTest32_3_6_3C(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("SAFARI") || driverType.equalsIgnoreCase("IE"))
			throw new SkipException(driverType.toUpperCase()+" does not support key actions");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform checkout operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-3 : Perform Check-in operation
			//-------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not undo checked out.");

			Log.message("3. Object (" + dataPool.get("ObjectName") + ") is Checked in.");

			//Step-4 : Rename the checked in object through F2 key
			//----------------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got clicked.");

			String extension = "";

			if(dataPool.get("ObjectName").contains("."))
				extension = "." + dataPool.get("ObjectName").split("\\.")[1];

			Actions action = new Actions(driver);
			action.sendKeys(Keys.F2).perform();
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			String newName = Utility.getObjectName(Utility.getMethodName());
			mfilesDialog.rename(newName, true); //Renames the object

			Log.message("4. Object (" + dataPool.get("ObjectName") + ") is clicked the Rename from F2 key and new name is entered.");

			//Verification : To Verify if renamed undo-checked out object exists in the list
			//---------------------------------------------------------------------------			
			if (homePage.listView.isItemExists(newName+extension))
				Log.pass("Test case Passed. Undo-Checkedout object is renamed successfully.");
			else
				Log.fail("Test case Failed. Renamed Undo-Checkedout object does not exists.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_6_3C

	/**
	 * 32.5.2.1A : Rename the document with chinese value and checkout the document through Taskpanel
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Rename the document with chinese value and checkout the document through Taskpanel")
	public void SprintTest32_5_2_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Rename the object to chinese value
			//-------------------------------------------
			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is already checkedout.");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got right clicked.");

			String extension = "";

			if(dataPool.get("ObjectName").contains("."))
				extension = "." + dataPool.get("ObjectName").split("\\.")[1];

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Rename.Value); //Selects Rename from context menu
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			String chineseNewName = dataPool.get("ChineseName");
			mfilesDialog.rename(chineseNewName, true); //Renames the object

			chineseNewName += extension;

			if (!homePage.listView.isItemExists(chineseNewName))
				throw new Exception("Object is not renamed to chinese value successfully.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is renamed to chinese (" + chineseNewName + ") name.");

			//Step-3 Click Checkout from taskpanel
			//------------------------------------
			if (!homePage.listView.clickItem(chineseNewName)) //Selects the Object in the list
				throw new Exception("Object (" + chineseNewName + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			Log.message("3. Chinese object (" + chineseNewName + ") is selected Check out is clicked from Task panel.");

			//Verification : To Verify if renamed checked-in out object exists in the list
			//---------------------------------------------------------------------------			
			if (ListView.isCheckedOutByItemName(driver, chineseNewName))
				Log.pass("Test case Passed. Renamed to chinese named object is checkedout successfully through taskpanel.");
			else
				Log.fail("Test case Failed. Renamed to chinese named object is not checkedout through taskpanel.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_5_2_1A

	/**
	 * 32.5.2.1B : Rename the document with chinese value and checkout the document through Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Rename the document with chinese value and checkout the document through Operations menu")
	public void SprintTest32_5_2_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Rename the object to chinese value
			//-------------------------------------------
			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is already checkedout.");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got right clicked.");

			String extension = "";

			if(dataPool.get("ObjectName").contains("."))
				extension = "." + dataPool.get("ObjectName").split("\\.")[1];

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Rename.Value); //Selects Rename from context menu
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			String chineseNewName = dataPool.get("ChineseName");
			mfilesDialog.rename(chineseNewName, true); //Renames the object
			chineseNewName += extension;

			if (!homePage.listView.isItemExists(chineseNewName))
				throw new Exception("Object is not renamed to chinese value successfully.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is renamed to chinese (" + chineseNewName + ") name.");

			//Step-3 Click Checkout from taskpanel
			//------------------------------------
			if (!homePage.listView.clickItem(chineseNewName)) //Selects the Object in the list
				throw new Exception("Object (" + chineseNewName + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, chineseNewName)) //Checks if it is in checked in state
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("3. Chinese object (" + chineseNewName + ") is selected Check out is clicked from operations menu.");

			//Verification : To Verify if renamed checked-in out object exists in the list
			//---------------------------------------------------------------------------			
			if (ListView.isCheckedOutByItemName(driver, chineseNewName))
				Log.pass("Test case Passed. renamed to chinese named object is checkedout successfully through operations menu.");
			else
				Log.fail("Test case Failed. renamed to chinese named object is not checkedout through operations menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_5_2_1B

	/**
	 * 32.5.2.1C : Rename the document with chinese value and checkout the document through Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Rename the document with chinese value and checkout the document through Context menu")
	public void SprintTest32_5_2_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Rename the object to chinese value
			//-------------------------------------------
			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is already checkedout.");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got right clicked.");

			String extension = "";

			if(dataPool.get("ObjectName").contains("."))
				extension = "." + dataPool.get("ObjectName").split("\\.")[1];

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Rename.Value); //Selects Rename from context menu
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			String chineseNewName = dataPool.get("ChineseName");
			mfilesDialog.rename(chineseNewName, true); //Renames the object
			chineseNewName += extension;

			if (!homePage.listView.isItemExists(chineseNewName))
				throw new Exception("Object is not renamed to chinese value successfully.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is renamed to chinese (" + chineseNewName + ") name.");

			//Step-3 Click Checkout from taskpanel
			//------------------------------------
			if (!homePage.listView.rightClickItem(chineseNewName)) //Selects the Object in the list
				throw new Exception("Object (" + chineseNewName + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, chineseNewName)) //Checks if it is in checked in state
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("3. Chinese object (" + chineseNewName + ") is selected Check out is clicked from context menu.");

			//Verification : To Verify if renamed checked-in out object exists in the list
			//---------------------------------------------------------------------------			
			if (ListView.isCheckedOutByItemName(driver, chineseNewName))
				Log.pass("Test case Passed. renamed to chinese named object is checkedout successfully through context menu.");
			else
				Log.fail("Test case Failed. renamed to chinese named object is not checkedout through context menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_5_2_1C

	/**
	 * 32.5.2.1D : Rename document with chinese value and checkout operation is not possible through CTRL+O key
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint32"}, 
			description = "Rename document with chinese value and checkout operation is not possible through CTRL+O key")
	public void SprintTest32_5_2_1D(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports key actions.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Rename the object to chinese value
			//-------------------------------------------
			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is already checkedout.");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got right clicked.");

			String extension = "";

			if(dataPool.get("ObjectName").contains("."))
				extension = "." + dataPool.get("ObjectName").split("\\.")[1];

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Rename.Value); //Selects Rename from context menu
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			String chineseNewName = dataPool.get("ChineseName");
			mfilesDialog.rename(chineseNewName, true); //Renames the object

			chineseNewName += extension;

			if (!homePage.listView.isItemExists(chineseNewName))
				throw new Exception("Object is not renamed to chinese value successfully.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is renamed to chinese (" + chineseNewName + ") name.");

			//Step-3 Click Checkout from taskpanel
			//------------------------------------
			if (!homePage.listView.clickItem(chineseNewName)) //Selects the Object in the list
				throw new Exception("Object (" + chineseNewName + ") is not got selected.");

			Actions action = new Actions(driver);
			action.keyDown(Keys.CONTROL).sendKeys("o").perform();
			Utils.fluentWait(driver);
			Utils.fluentWait(driver);

			Log.message("3. Chinese object (" + chineseNewName + ") is selected and 'CTRL+O' key is pressed.");

			//Verification : To Verify if renamed checked-in out object exists in the list
			//---------------------------------------------------------------------------			
			if (!ListView.isCheckedOutByItemName(driver, chineseNewName))
				Log.pass("Test case Passed. renamed to chinese named object is not checked out by pressing 'CTRL+O' key.");
			else
				Log.fail("Test case Failed. renamed to chinese named object is checked out by pressing 'CTRL+O' key.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_5_2_1D

	/**
	 * 32.5.2.2A : Rename the document with chinese value and checkin the document through Taskpanel
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32", "Smoke"}, 
			description = "Rename the document with chinese value and checkin the document through Taskpanel")
	public void SprintTest32_5_2_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Rename the object to Chinese value
			//-------------------------------------------
			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) { //Undo Checks out the object
				homePage.listView.clickItem(dataPool.get("ObjectName"));
				homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Show Members from task panel
				MFilesDialog mfilesDialog = new MFilesDialog(driver);
				mfilesDialog.confirmUndoCheckOut(true);
				Utils.fluentWait(driver);

				if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not in checked in state.");
			}

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got right clicked.");

			String extension = "";

			if(dataPool.get("ObjectName").contains("."))
				extension = "." + dataPool.get("ObjectName").split("\\.")[1];

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Rename.Value); //Selects Rename from context menu
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			String chineseNewName = dataPool.get("ChineseName");
			mfilesDialog.rename(chineseNewName, true); //Renames the object

			chineseNewName += extension;

			if (!homePage.listView.isItemExists(chineseNewName))
				throw new Exception("Object is not renamed to chinese value successfully.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is renamed to chinese (" + chineseNewName + ") name.");

			//Step-3 Click Check-in from taskpanel
			//------------------------------------
			homePage.listView.clickItem(chineseNewName);
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, chineseNewName))
				throw new Exception("Chinese Object (" + chineseNewName + ") is not checked out.");

			if (!homePage.listView.clickItem(chineseNewName)) //Selects the Object in the list
				throw new Exception("Chinese Object (" + chineseNewName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);

			Log.message("3. Chinese object (" + chineseNewName + ") is selected Check in is clicked from Task panel.");

			//Verification : To Verify if renamed checked-in out object exists in the list
			//---------------------------------------------------------------------------			
			if (!ListView.isCheckedOutByItemName(driver, chineseNewName))
				Log.pass("Test case Passed. renamed to chinese named object is checkedin successfully through taskpanel.");
			else
				Log.fail("Test case Failed. renamed to chinese named object is not checkedin through taskpanel.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_5_2_2A

	/**
	 * 32.5.2.2B : Rename the document with chinese value and checkin the document through Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Rename the document with chinese value and checkin the document through Operations menu")
	public void SprintTest32_5_2_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Rename the object to chinese value
			//-------------------------------------------
			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) { //Undo Checks out the object
				homePage.listView.clickItem(dataPool.get("ObjectName"));
				homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Show Members from task panel
				MFilesDialog mfilesDialog = new MFilesDialog(driver);
				mfilesDialog.confirmUndoCheckOut(true);
				Utils.fluentWait(driver);

				if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not in checked in state.");
			}

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got right clicked.");

			String extension = "";

			if(dataPool.get("ObjectName").contains("."))
				extension = "." + dataPool.get("ObjectName").split("\\.")[1];

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Rename.Value); //Selects Rename from context menu
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			String chineseNewName = dataPool.get("ChineseName");
			mfilesDialog.rename(chineseNewName, true); //Renames the object

			chineseNewName += extension;

			if (!homePage.listView.isItemExists(chineseNewName))
				throw new Exception("Object is not renamed to chinese value successfully.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is renamed to chinese (" + chineseNewName + ") name.");

			//Step-3 Click Check-in from operations menu
			//------------------------------------
			homePage.listView.clickItem(chineseNewName);
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, chineseNewName))
				throw new Exception("Chinese Object (" + chineseNewName + ") is not checked out.");

			if (!homePage.listView.clickItem(chineseNewName)) //Selects the Object in the list
				throw new Exception("Chinese Object (" + chineseNewName + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.CheckIn.Value);
			Utils.fluentWait(driver);

			Log.message("3. Chinese object (" + chineseNewName + ") is selected Check in is clicked from operations menu.");

			//Verification : To Verify if renamed checked-in out object exists in the list
			//---------------------------------------------------------------------------			
			if (!ListView.isCheckedOutByItemName(driver, chineseNewName))
				Log.pass("Test case Passed. renamed to chinese named object is checkedin successfully through operations menu.");
			else
				Log.fail("Test case Failed. renamed to chinese named object is not checkedin through operations menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_5_2_2B

	/**
	 * 32.5.2.2C : Rename the document with chinese value and checkin the document through Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Rename the document with chinese value and checkin the document through Context menu")
	public void SprintTest32_5_2_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Rename the object to chinese value
			//-------------------------------------------
			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) { //Undo Checks out the object
				homePage.listView.clickItem(dataPool.get("ObjectName"));
				homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Show Members from task panel
				MFilesDialog mfilesDialog = new MFilesDialog(driver);
				mfilesDialog.confirmUndoCheckOut(true);
				Utils.fluentWait(driver);

				if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not in checked in state.");
			}

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got right clicked.");

			String extension = "";

			if(dataPool.get("ObjectName").contains("."))
				extension = "." + dataPool.get("ObjectName").split("\\.")[1];

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Rename.Value); //Selects Rename from context menu
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			String chineseNewName = dataPool.get("ChineseName");
			mfilesDialog.rename(chineseNewName, true); //Renames the object

			chineseNewName += extension;

			if (!homePage.listView.isItemExists(chineseNewName))
				throw new Exception("Object is not renamed to chinese value successfully.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is renamed to chinese (" + chineseNewName + ") name.");

			//Step-3 Click Check-in from operations menu
			//------------------------------------
			homePage.listView.clickItem(chineseNewName);
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, chineseNewName))
				throw new Exception("Chinese Object (" + chineseNewName + ") is not checked out.");

			if (!homePage.listView.rightClickItem(chineseNewName)) //Selects the Object in the list
				throw new Exception("Chinese Object (" + chineseNewName + ") is not selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.CheckIn.Value);

			Log.message("3. Chinese object (" + chineseNewName + ") is selected Check in is clicked from context menu.");

			//Verification : To Verify if renamed checked-in out object exists in the list
			//---------------------------------------------------------------------------			
			if (!ListView.isCheckedOutByItemName(driver, chineseNewName))
				Log.pass("Test case Passed. renamed to chinese named object is checkedin successfully through context menu.");
			else
				Log.fail("Test case Failed. renamed to chinese named object is not checkedin through context menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_5_2_2C

	/**
	 * 32.5.2.2D : Rename the document with Chinese value and checkin the document through CTRL + I key
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint32"}, 
			description = "Rename the document with Chinese value and checkin the document through CTRL + I key")
	public void SprintTest32_5_2_2D(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports key actions.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Rename the object to chinese value
			//-------------------------------------------
			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) { //Undo Checks out the object
				homePage.listView.clickItem(dataPool.get("ObjectName"));
				homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Show Members from task panel
				MFilesDialog mfilesDialog = new MFilesDialog(driver);
				mfilesDialog.confirmUndoCheckOut(true);
				Utils.fluentWait(driver);

				if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not in checked in state.");
			}

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got right clicked.");

			String extension = "";

			if(dataPool.get("ObjectName").contains("."))
				extension = "." + dataPool.get("ObjectName").split("\\.")[1];

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Rename.Value); //Selects Rename from context menu
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			String chineseNewName = dataPool.get("ChineseName");
			mfilesDialog.rename(chineseNewName, true); //Renames the object

			chineseNewName += extension;

			if (!homePage.listView.isItemExists(chineseNewName))
				throw new Exception("Object is not renamed to chinese value successfully.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is renamed to chinese (" + chineseNewName + ") name.");

			//Step-3 Click Check-in from operations menu
			//------------------------------------
			homePage.listView.clickItem(chineseNewName);
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, chineseNewName))
				throw new Exception("Chinese Object (" + chineseNewName + ") is not checked out.");

			if (!homePage.listView.clickItem(chineseNewName)) //Selects the Object in the list
				throw new Exception("Chinese Object (" + chineseNewName + ") is not selected.");

			Actions action = new Actions(driver);
			action.keyDown(Keys.CONTROL).sendKeys("i").perform();
			Utils.fluentWait(driver);

			Log.message("3. Chinese object (" + chineseNewName + ") is selected CTRL+I is pressed.");

			//Verification : To Verify if renamed checked-in out object exists in the list
			//---------------------------------------------------------------------------			
			if (!ListView.isCheckedOutByItemName(driver, chineseNewName))
				Log.pass("Test case Passed. renamed to chinese named object is checkedin successfully by pressing CTRL+I key.");
			else
				Log.fail("Test case Failed. renamed to chinese named object is not checkedin by pressing CTRL+I key.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_5_2_2D

	/**
	 * 32.5.2.3A : Rename the document with chinese value and undo-checkout the document through Taskpanel
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32", "Smoke"}, 
			description = "Rename the document with chinese value and undo-checkout the document through Taskpanel")
	public void SprintTest32_5_2_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Rename the object to Chinese value
			//-------------------------------------------
			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) { //Undo Checks out the object
				homePage.listView.clickItem(dataPool.get("ObjectName"));
				homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Show Members from task panel
				MFilesDialog mfilesDialog = new MFilesDialog(driver);
				mfilesDialog.confirmUndoCheckOut(true);
				Utils.fluentWait(driver);

				if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not in checked in state.");
			}

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got right clicked.");

			String extension = "";

			if(dataPool.get("ObjectName").contains("."))
				extension = "." + dataPool.get("ObjectName").split("\\.")[1];

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Rename.Value); //Selects Rename from context menu
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			String chineseNewName = dataPool.get("ChineseName");
			mfilesDialog.rename(chineseNewName, true); //Renames the object

			chineseNewName += extension;

			if (!homePage.listView.isItemExists(chineseNewName))
				throw new Exception("Object is not renamed to chinese value successfully.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is renamed to chinese (" + chineseNewName + ") name.");

			//Step-3 : Checkout the object
			//----------------------------
			if (!homePage.listView.clickItem(chineseNewName))
				throw new Exception("Chinese object (" + chineseNewName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, chineseNewName))
				throw new Exception("Chinese Object (" + chineseNewName + ") is not checked out.");

			Log.message("3. Object (" + chineseNewName + ") is checked out.");

			//Step-4 Click Undo-Checkout from taskpanel
			//------------------------------------------
			if (!homePage.listView.clickItem(chineseNewName)) //Selects the Object in the list
				throw new Exception("Chinese Object (" + chineseNewName + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);
			Utils.fluentWait(driver);			
			mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true);

			Log.message("4. Chinese object (" + chineseNewName + ") is selected and Undo-Checkout is clicked from Task panel.");

			//Verification : To Verify if renamed checked-in out object exists in the list
			//---------------------------------------------------------------------------			
			if (!ListView.isCheckedOutByItemName(driver, chineseNewName))
				Log.pass("Test case Passed. renamed to chinese named object is undo-checkedout successfully through taskpanel.");
			else
				Log.fail("Test case Failed. renamed to chinese named object is not undo-checkedout through taskpanel.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_5_2_3A

	/**
	 * 32.5.2.3B : Rename the document with Chinese value and undo-checkout the document through Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Rename the document with chinese value and undo-checkout the document through Operations menu")
	public void SprintTest32_5_2_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Rename the object to Chinese value
			//-------------------------------------------
			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) { //Undo Checks out the object
				homePage.listView.clickItem(dataPool.get("ObjectName"));
				homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Show Members from task panel
				MFilesDialog mfilesDialog = new MFilesDialog(driver);
				mfilesDialog.confirmUndoCheckOut(true);
				Utils.fluentWait(driver);

				if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not in checked in state.");
			}

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got right clicked.");

			String extension = "";

			if(dataPool.get("ObjectName").contains("."))
				extension = "." + dataPool.get("ObjectName").split("\\.")[1];

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Rename.Value); //Selects Rename from context menu
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			String chineseNewName = dataPool.get("ChineseName");
			mfilesDialog.rename(chineseNewName, true); //Renames the object

			chineseNewName += extension;

			if (!homePage.listView.isItemExists(chineseNewName))
				throw new Exception("Object is not renamed to chinese value successfully.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is renamed to chinese (" + chineseNewName + ") name.");

			//Step-3 : Checkout the object
			//----------------------------
			homePage.listView.clickItem(chineseNewName);
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, chineseNewName))
				throw new Exception("Chinese Object (" + chineseNewName + ") is not checked out.");

			Log.message("3. Object (" + chineseNewName + ") is checked out.");

			//Step-4 Click Check-in from operations menu
			//------------------------------------------
			if (!homePage.listView.clickItem(chineseNewName)) //Selects the Object in the list
				throw new Exception("Chinese Object (" + chineseNewName + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Undo Checks out from operations menu
			mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true);

			Log.message("4. Chinese object (" + chineseNewName + ") is selected and Undo-Checkout is clicked from Operations menu.");

			//Verification : To Verify if renamed checked-in out object exists in the list
			//---------------------------------------------------------------------------			
			if (!ListView.isCheckedOutByItemName(driver, chineseNewName))
				Log.pass("Test case Passed. renamed to chinese named object is undo-checkedout successfully through Operations menu.");
			else
				Log.fail("Test case Failed. renamed to chinese named object is not undo-checkedout through Operations menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_5_2_3B

	/**
	 * 32.5.2.3C : Rename the document with Chinese value and undo-checkout the document through Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "Rename the document with chinese value and undo-checkout the document through Context menu")
	public void SprintTest32_5_2_3C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Rename the object to Chinese value
			//-------------------------------------------
			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) { //Undo Checks out the object
				homePage.listView.clickItem(dataPool.get("ObjectName"));
				homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Show Members from task panel
				MFilesDialog mfilesDialog = new MFilesDialog(driver);
				mfilesDialog.confirmUndoCheckOut(true);
				Utils.fluentWait(driver);

				if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not in checked in state.");
			}

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got right clicked.");

			String extension = "";

			if(dataPool.get("ObjectName").contains("."))
				extension = "." + dataPool.get("ObjectName").split("\\.")[1];

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Rename.Value); //Selects Rename from context menu
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			String chineseNewName = dataPool.get("ChineseName");
			mfilesDialog.rename(chineseNewName, true); //Renames the object

			chineseNewName += extension;

			if (!homePage.listView.isItemExists(chineseNewName))
				throw new Exception("Object is not renamed to chinese value successfully.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is renamed to chinese (" + chineseNewName + ") name.");

			//Step-3 : Checkout the object
			//----------------------------
			homePage.listView.clickItem(chineseNewName);
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, chineseNewName))
				throw new Exception("Chinese Object (" + chineseNewName + ") is not checked out.");

			Log.message("3. Object (" + chineseNewName + ") is checked out.");

			//Step-4 Click Check-in from operations menu
			//------------------------------------------
			if (!homePage.listView.rightClickItem(chineseNewName)) //Selects the Object in the list
				throw new Exception("Chinese Object (" + chineseNewName + ") is not selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.UndoCheckOut.Value); //Undo Checks out from context menu
			mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true);

			Log.message("4. Chinese object (" + chineseNewName + ") is selected and Undo-Checkout is clicked from Context menu.");

			//Verification : To Verify if renamed checked-in out object exists in the list
			//---------------------------------------------------------------------------			
			if (!ListView.isCheckedOutByItemName(driver, chineseNewName))
				Log.pass("Test case Passed. renamed to chinese named object is undo-checkedout successfully through Context menu.");
			else
				Log.fail("Test case Failed. renamed to chinese named object is not undo-checkedout through Context menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_5_2_3C

	/**
	 * 32.5.2.4A : Rename the document with Chinese value and Checkout and convert SFD to MFD through Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug", "Sprint32", "Smoke"}, 
			description = "Rename the document with Chinese value and Checkout and convert SFD to MFD through Operations menu")
	public void SprintTest32_5_2_4A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Rename the object to Chinese value
			//-------------------------------------------
			//if (!ListView.isSFDByItemName(driver, dataPool.get("ObjectName"))) //Checks if this is SFD
			if(!homePage.listView.isSFDBasedOnObjectIcon(dataPool.get("ObjectName")))
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not Single file document.");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got right clicked.");

			String extension = "";

			if(dataPool.get("ObjectName").contains("."))
				extension = "." + dataPool.get("ObjectName").split("\\.")[1];

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Rename.Value); //Selects Rename from context menu
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			String chineseNewName = dataPool.get("ChineseName");
			mfilesDialog.rename(chineseNewName, true); //Renames the object

			chineseNewName += extension;

			if (!homePage.listView.isItemExists(chineseNewName))
				throw new Exception("Object is not renamed to chinese value successfully.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is renamed to chinese (" + chineseNewName + ") name.");

			//Step-3 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (!homePage.listView.clickItem(chineseNewName)) //Selects the Object in the list
				throw new Exception("Object (" + chineseNewName + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, chineseNewName)) //Checks if it is in checked in state
				throw new Exception("Object (" + chineseNewName + ") is not checked out.");

			Log.message("3. Object (" + chineseNewName + ") is checked out.");

			//Step-4 : Select Convert to MFD from operations menu
			//---------------------------------------------------
			if (!homePage.listView.clickItem(chineseNewName)) //Selects the Object in the list
				throw new Exception("Object (" + chineseNewName + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value); //Clicks Convert SFD to MFD option from operations menu
			Utils.fluentWait(driver);

			String mfdName = chineseNewName.split("\\.")[0];

			Log.message("4. Convert SFD to MFD option is selected from operations menu for SFD (" + chineseNewName + ").");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.isSFDBasedOnObjectIcon(mfdName))
				Log.pass("Test case Passed. Chinese named checked out SFD is converted to MFD through operations menu.");
			else
				Log.fail("Test case Failed. Chinese named checked out SFD is not converted to MFD through operations menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_7_4A

	/**
	 * 32.5.2.4B : Rename the document with Chinese value and Checkout and convert SFD to MFD through Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32"}, 
			description = "Rename the document with Chinese value and Checkout and convert SFD to MFD through Context menu")
	public void SprintTest32_5_2_4B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Rename the object to Chinese value
			//-------------------------------------------
			if(!homePage.listView.isSFDBasedOnObjectIcon(dataPool.get("ObjectName"))) //Checks if this is SFD
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not Single file document.");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got right clicked.");

			String extension = "";

			if(dataPool.get("ObjectName").contains("."))
				extension = "." + dataPool.get("ObjectName").split("\\.")[1];

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Rename.Value); //Selects Rename from context menu
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			String chineseNewName = dataPool.get("ChineseName");
			mfilesDialog.rename(chineseNewName, true); //Renames the object

			chineseNewName += extension;

			if (!homePage.listView.isItemExists(chineseNewName))
				throw new Exception("Object is not renamed to chinese value successfully.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is renamed to chinese (" + chineseNewName + ") name.");

			//Step-3 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (!homePage.listView.clickItem(chineseNewName)) //Selects the Object in the list
				throw new Exception("Object (" + chineseNewName + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, chineseNewName)) //Checks if it is in checked in state
				throw new Exception("Object (" + chineseNewName + ") is not checked out.");

			Log.message("3. Object (" + chineseNewName + ") is checked out.");

			//Step-4 : Select Convert to MFD from operations menu
			//---------------------------------------------------
			if (!homePage.listView.rightClickItem(chineseNewName)) //Selects the Object in the list
				throw new Exception("Object (" + chineseNewName + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.ConvertToMFD_C.Value); //Clicks Convert SFD to MFD option from context menu
			Utils.fluentWait(driver);

			String mfdName = chineseNewName.split("\\.")[0];

			Log.message("4. Convert SFD to MFD option is selected from context menu for SFD (" + chineseNewName + ").");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (!homePage.listView.isSFDBasedOnObjectIcon(mfdName))
				Log.pass("Test case Passed. Chinese named checked out SFD is converted to MFD through context menu.");
			else
				Log.fail("Test case Failed. Chinese named checked out SFD is not converted to MFD through context menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_7_4B

	/**
	 * 32.5.2.5A : Rename the document with Chinese value and Checkout and convert MFD to SFD through Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint32", "Smoke"}, 
			description = "Rename the document with Chinese value and Checkout and convert SFD to MFD through Operations menu")
	public void SprintTest32_5_2_5A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if it is in checked in state
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-3 : Select Convert to MFD from operations menu
			//---------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value); //Clicks Convert SFD to MFD option from context menu
			Utils.fluentWait(driver);

			String extn = dataPool.get("ObjectName").split("\\.")[1];
			String mfdName = dataPool.get("ObjectName").split("\\.")[0];

			if (homePage.listView.isSFDBasedOnObjectIcon(mfdName))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not converted to MFD.");

			Log.message("3. SFD (" + dataPool.get("ObjectName") + ") is converted to MFD.");

			//Step-4 : Check-in the converted MFD document
			//--------------------------------------------
			if (!homePage.listView.clickItem(mfdName)) //Selects the Object in the list
				throw new Exception("Object (" + mfdName + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);

			if (ListView.isCheckedOutByItemName(driver, mfdName)) //Checks if it is in checked in state
				throw new Exception("Object (" + mfdName + ") is not checked in.");

			Log.message("4. MFD document (" + mfdName + ") is checked in.");

			//Step-5 : Rename the object to Chinese value
			//-------------------------------------------
			if (homePage.listView.isSFDBasedOnObjectIcon(mfdName)) //Checks if this is SFD
				throw new SkipException("Object (" + mfdName + ") is not multi file document.");

			if (!homePage.listView.rightClickItem(mfdName)) //Selects the Object in the list
				throw new Exception("Object (" + mfdName + ") is not got right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Rename.Value); //Selects Rename from context menu
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			String chineseNewName = dataPool.get("ChineseName");
			mfilesDialog.rename(chineseNewName, true); //Renames the object

			if (!homePage.listView.isItemExists(chineseNewName))
				throw new Exception("Object is not renamed to chinese value successfully.");

			Log.message("5. Object (" + mfdName + ") is renamed to chinese (" + chineseNewName + ") name.");

			//Step-6 : Select MFD object and perform check out operation
			//----------------------------------------------------------
			if (!homePage.listView.clickItem(chineseNewName)) //Selects the Object in the list
				throw new Exception("Object (" + chineseNewName + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, chineseNewName)) //Checks if it is in checked in state
				throw new Exception("Object (" + chineseNewName + ") is not checked out.");

			Log.message("6. Renamed chinese object (" + chineseNewName + ") is checked out.");

			//Step-7 : Select Convert to SFD from operations menu
			//---------------------------------------------------
			if (!homePage.listView.clickItem(chineseNewName)) //Selects the Object in the list
				throw new Exception("Object (" + chineseNewName + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.ConvertToSFD_O.Value); //Clicks Convert SFD to MFD option from operations menu
			Utils.fluentWait(driver);

			Log.message("7. Convert MFD to SFD option is selected from operations menu for MFD (" + mfdName + ").");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (homePage.listView.isSFDBasedOnObjectIcon(chineseNewName + '.' + extn))
				Log.pass("Test case Passed. Chinese named checked out MFD is converted to SFD through operations menu.");
			else
				Log.fail("Test case Failed. Chinese named checked out MFD is not converted to SFD through operations menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_7_5A

	/**
	 * 32.5.2.5B : Rename the document with Chinese value and Checkout and convert MFD to SFD through Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug", "Sprint32"}, 
			description = "Rename the document with Chinese value and Checkout and convert SFD to MFD through Context menu")
	public void SprintTest32_5_2_5B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if it is in checked in state
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-3 : Select Convert to MFD from context menu
			//---------------------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.ConvertToMFD_C.Value); //Clicks Convert SFD to MFD option from context menu
			Utils.fluentWait(driver);

			String extn = dataPool.get("ObjectName").split("\\.")[1];
			String mfdName = dataPool.get("ObjectName").split("\\.")[0];

			if (homePage.listView.isSFDBasedOnObjectIcon(mfdName))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not converted to MFD.");

			Log.message("3. SFD (" + dataPool.get("ObjectName") + ") is converted to MFD.");			

			//Step-4 : Check-in the converted MFD document
			//--------------------------------------------
			if (!homePage.listView.clickItem(mfdName)) //Selects the Object in the list
				throw new Exception("Object (" + mfdName + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);

			if (ListView.isCheckedOutByItemName(driver, mfdName)) //Checks if it is in checked in state
				throw new Exception("Object (" + mfdName + ") is not checked in.");

			Log.message("4. MFD document (" + mfdName + ") is checked in.");

			//Step-5 : Rename the object to Chinese value
			//-------------------------------------------
			if (homePage.listView.isSFDBasedOnObjectIcon(mfdName)) //Checks if this is SFD
				throw new SkipException("Object (" + mfdName + ") is not multi file document.");

			if (!homePage.listView.rightClickItem(mfdName)) //Selects the Object in the list
				throw new Exception("Object (" + mfdName + ") is not got right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Rename.Value); //Selects Rename from context menu
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			String chineseNewName = dataPool.get("ChineseName");
			mfilesDialog.rename(chineseNewName, true); //Renames the object

			if (!homePage.listView.isItemExists(chineseNewName))
				throw new Exception("Object is not renamed to chinese value successfully.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is renamed to chinese (" + chineseNewName + ") name.");

			//Step-6 : Select MFD object and perform check out operation
			//----------------------------------------------------------
			if (!homePage.listView.clickItem(chineseNewName)) //Selects the Object in the list
				throw new Exception("Object (" + chineseNewName + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, chineseNewName)) //Checks if it is in checked in state
				throw new Exception("Object (" + chineseNewName + ") is not checked out.");

			Log.message("6. Renamed chinese object (" + chineseNewName + ") is checked out.");

			//Step-7 : Select Convert to MFD from context menu
			//---------------------------------------------------
			if (!homePage.listView.rightClickItem(chineseNewName)) //Selects the Object in the list
				throw new Exception("Object (" + chineseNewName + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.ConvertToSFD_C.Value); //Clicks Convert SFD to MFD option from operations menu
			Utils.fluentWait(driver);

			Log.message("7. Convert MFD to SFD option is selected from operations menu for MFD (" + mfdName + ").");

			//Verification : To Verify if SFD to MFD is possible for checked in objects
			//---------------------------------------------------------------------------			
			if (homePage.listView.isSFDBasedOnObjectIcon(chineseNewName + '.' + extn))
				Log.pass("Test case Passed. Chinese named checked out MFD is converted to SFD through context menu.");
			else
				Log.fail("Test case Failed. Chinese named checked out MFD is not converted to SFD through context menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest32_3_7_5B

	/**
	 * 43.3.4.1A : Cancelling Rename operation should not rename the document - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint43"}, 
			description = "Cancelling Rename operation should not rename the document - Operations menu")
	public void SprintTest43_3_4_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and open rename dialog
			//-------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			int prevCt = homePage.listView.itemCount(); //Gets item count from the list

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Rename.Value); //Clicks Rename from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver);

			if (!mfilesDialog.isRenameDialogOpened())
				throw new Exception("Rename dialog is not opened.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected and rename dialog is opened through operations menu.");

			//Step-3 : Enter the name and click Cancel button
			//-----------------------------------------------
			String newName = Utility.getObjectName(Utility.getMethodName()); 
			mfilesDialog.rename(newName, false); //New name is entered and cancel button is clicked

			Log.message("3. New name is entered and cancel button is clicked.");

			//Verification : To Verify if Rename operation is not successful
			//--------------------------------------------------------------
			int currCt = homePage.listView.itemCount(); //Gets item count from the list

			if (prevCt != currCt) //Verifies if number of objects before and after rename cancellation operation are equal
				throw new Exception("Test case Failed. Number of objects before and after rename cancellation operation are not equal.");

			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Test case Failed. Object (" + dataPool.get("ObjectName") + ") does not exists.");

			//Verifies that Rename option is enabled in operations menu
			if (!homePage.listView.isItemExists(newName))
				Log.pass("Test case Passed. Rename cancellation operation has not renamed the object.");
			else
				Log.fail("Test case Failed. Rename cancellation operation has renamed the object.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest43_3_4_1A

	/**
	 * 43.3.4.1B : Cancelling Rename operation should not rename the document - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint43"}, 
			description = "Cancelling Rename operation should not rename the document - Context menu")
	public void SprintTest43_3_4_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and open rename dialog
			//-------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			int prevCt = homePage.listView.itemCount(); //Gets item count from the list

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Rename.Value); //Clicks Rename from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver);

			if (!mfilesDialog.isRenameDialogOpened())
				throw new Exception("Rename dialog is not opened.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected and rename dialog is opened through context menu.");

			//Step-3 : Enter the name and click Cancel button
			//-----------------------------------------------
			String newName = Utility.getObjectName(Utility.getMethodName()); 
			mfilesDialog.rename(newName, false); //New name is entered and cancel button is clicked

			Log.message("3. New name is entered and cancel button is clicked.");

			//Verification : To Verify if Rename operation is not successful
			//--------------------------------------------------------------
			int currCt = homePage.listView.itemCount(); //Gets item count from the list

			if (prevCt != currCt) //Verifies if number of objects before and after rename cancellation operation are equal
				throw new Exception("Test case Failed. Number of objects before and after rename cancellation operation are not equal.");

			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Test case Failed. Object (" + dataPool.get("ObjectName") + ") does not exists.");

			//Verifies that Rename option is enabled in operations menu
			if (!homePage.listView.isItemExists(newName))
				Log.pass("Test case Passed. Rename cancellation operation has not renamed the object.");
			else
				Log.fail("Test case Failed. Rename cancellation operation has renamed the object.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest43_3_4_1B

	/**
	 * 43.3.4.1C : Cancelling Rename operation should not rename the document - F2 key
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint43"}, 
			description = "Cancelling Rename operation should not rename the document - F2 key")
	public void SprintTest43_3_4_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("SAFARI") || driverType.equalsIgnoreCase("IE"))
			throw new SkipException(driverType.toUpperCase()+" does not support key actions");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and open rename dialog
			//-------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			int prevCt = homePage.listView.itemCount(); //Gets item count from the list

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Actions action = new Actions(driver);
			action.sendKeys(Keys.F2).perform();
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);

			if (!mfilesDialog.isRenameDialogOpened())
				throw new Exception("Rename dialog is not opened.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected and rename dialog is opened by pressing F2 key.");

			//Step-3 : Enter the name and click Cancel button
			//-----------------------------------------------
			String newName = Utility.getObjectName(Utility.getMethodName()); 
			mfilesDialog.rename(newName, false); //New name is entered and cancel button is clicked

			Log.message("3. New name is entered and cancel button is clicked.");

			//Verification : To Verify if Rename operation is not successful
			//--------------------------------------------------------------
			int currCt = homePage.listView.itemCount(); //Gets item count from the list

			if (prevCt != currCt) //Verifies if number of objects before and after rename cancellation operation are equal
				throw new Exception("Test case Failed. Number of objects before and after rename cancellation operation are not equal.");

			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Test case Failed. Object (" + dataPool.get("ObjectName") + ") does not exists.");

			//Verifies that Rename option is enabled in operations menu
			if (!homePage.listView.isItemExists(newName))
				Log.pass("Test case Passed. Rename cancellation operation has not renamed the object.");
			else
				Log.fail("Test case Failed. Rename cancellation operation has renamed the object.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest43_3_4_1C

	/**
	 * 43.3.4.2A : Cancelling Rename operation should not rename the latest version of a document - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint43"}, 
			description = "Cancelling Rename operation should not rename the latest version of a document - Operations menu")
	public void SprintTest43_3_4_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			//--------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel
			Utils.fluentWait(driver);

			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Select Latest version of an object and open rename dialog
			//------------------------------------------------------------------
			int prevCt = homePage.listView.itemCount(); //Gets item count from the list
			int prevVersionCt = Integer.parseInt(homePage.listView.getColumnValueByItemIndex(0, "Version")); //Gets the version count of the latest version

			if (!homePage.listView.clickItemByIndex(0)) //Selects the latest version of the object
				throw new Exception("Latest version of an object is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Rename.Value); //Selects Rename from operations menuBar

			MFilesDialog mfilesDialog = new MFilesDialog(driver);

			if (!mfilesDialog.isRenameDialogOpened())
				throw new Exception("Rename dialog is not opened.");

			Log.message("3. Latest version is selected and rename dialog is opened through operations menu.");

			//Step-4 : Enter the name and click Cancel button
			//-----------------------------------------------
			String newName = Utility.getObjectName(Utility.getMethodName()); 
			mfilesDialog.rename(newName, false); //New name is entered and cancel button is clicked

			Log.message("4. New name is entered and cancel button is clicked.");

			//Verification : To Verify if Rename operation is not successful
			//--------------------------------------------------------------
			int currCt = homePage.listView.itemCount(); //Gets item count from the list
			int currVersionCt = Integer.parseInt(homePage.listView.getColumnValueByItemIndex(0, "Version")); //Gets the version count of the latest version

			if (prevCt != currCt) //Verifies if number of objects before and after rename cancellation operation are equal
				throw new Exception("Test case Failed. Number of objects before and after rename cancellation operation are not equal.");

			if (currVersionCt != prevVersionCt) //Verifies that number of items in History view has increased by 1 in History view
				throw new Exception("Test case Failed. Number of versions listed in History view is not same after rename cancellation operation.");

			//Verifies that Rename option is enabled in operations menu
			if (!homePage.listView.isItemExists(newName))
				Log.pass("Test case Passed. Rename cancellation operation for latest verion of an object has not renamed the object.");
			else
				Log.fail("Test case Failed. Rename cancellation operation for latest verion of an object has renamed the object.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest43_3_4_2A

	/**
	 * 43.3.4.2B : Cancelling Rename operation should not rename the latest version of a document - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint43"}, 
			description = "Cancelling Rename operation should not rename the latest version of a document - Context menu")
	public void SprintTest43_3_4_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			//--------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel
			Utils.fluentWait(driver);

			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Select Latest version of an object and open rename dialog
			//------------------------------------------------------------------
			int prevCt = homePage.listView.itemCount(); //Gets item count from the list
			int prevVersionCt = Integer.parseInt(homePage.listView.getColumnValueByItemIndex(0, "Version")); //Gets the version count of the latest version

			if (!homePage.listView.rightClickItemByIndex(0)) //Selects the latest version of the object
				throw new Exception("Latest version of an object is not selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Rename.Value); //Selects Rename from operations menuBar

			MFilesDialog mfilesDialog = new MFilesDialog(driver);

			if (!mfilesDialog.isRenameDialogOpened())
				throw new Exception("Rename dialog is not opened.");

			Log.message("3. Latest version is selected and rename dialog is opened through context menu.");

			//Step-4 : Enter the name and click Cancel button
			//-----------------------------------------------
			String newName = Utility.getObjectName(Utility.getMethodName()); 
			mfilesDialog.rename(newName, false); //New name is entered and cancel button is clicked

			Log.message("4. New name is entered and cancel button is clicked.");

			//Verification : To Verify if Rename operation is not successful
			//--------------------------------------------------------------
			int currCt = homePage.listView.itemCount(); //Gets item count from the list
			int currVersionCt = Integer.parseInt(homePage.listView.getColumnValueByItemIndex(0, "Version")); //Gets the version count of the latest version

			if (prevCt != currCt) //Verifies if number of objects before and after rename cancellation operation are equal
				throw new Exception("Test case Failed. Number of objects before and after rename cancellation operation are not equal.");

			if (currVersionCt != prevVersionCt) //Verifies that number of items in History view has increased by 1 in History view
				throw new Exception("Test case Failed. Number of versions listed in History view is not same after rename cancellation operation.");

			//Verifies that Rename option is enabled in operations menu
			if (!homePage.listView.isItemExists(newName))
				Log.pass("Test case Passed. Rename cancellation operation for latest verion of an object has not renamed the object.");
			else
				Log.fail("Test case Failed. Rename cancellation operation for latest verion of an object has renamed the object.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest43_3_4_2B

	/**
	 * 43.3.4.2C : Cancelling Rename operation should not rename the latest version of a document - F2 Key
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint43"}, 
			description = "Cancelling Rename operation should not rename the latest version of a document - F2 Key")
	public void SprintTest43_3_4_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("SAFARI") || driverType.equalsIgnoreCase("IE"))
			throw new SkipException(driverType.toUpperCase()+" does not support key actions");

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
			//--------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listview.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem("History"); //Selects History from task panel
			Utils.fluentWait(driver);

			if (!ListView.isHistoryViewOpened(driver)) //Checks if History view is opened
				throw new Exception("History view is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Select Latest version of an object and open rename dialog
			//------------------------------------------------------------------
			int prevCt = homePage.listView.itemCount(); //Gets item count from the list
			int prevVersionCt = Integer.parseInt(homePage.listView.getColumnValueByItemIndex(0, "Version")); //Gets the version count of the latest version

			if (!homePage.listView.clickItemByIndex(0)) //Selects the latest version of the object
				throw new Exception("Latest version of an object is not selected.");

			Actions action = new Actions(driver);
			action.sendKeys(Keys.F2).perform(); //Press F2 key
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);

			if (!mfilesDialog.isRenameDialogOpened())
				throw new Exception("Rename dialog is not opened.");

			Log.message("3. Latest version is selected and rename dialog is opened through F2 key.");

			//Step-4 : Enter the name and click Cancel button
			//-----------------------------------------------
			String newName = Utility.getObjectName(Utility.getMethodName()); 
			mfilesDialog.rename(newName, false); //New name is entered and cancel button is clicked

			Log.message("4. New name is entered and cancel button is clicked.");

			//Verification : To Verify if Rename operation is not successful
			//--------------------------------------------------------------
			int currCt = homePage.listView.itemCount(); //Gets item count from the list
			int currVersionCt = Integer.parseInt(homePage.listView.getColumnValueByItemIndex(0, "Version")); //Gets the version count of the latest version

			if (prevCt != currCt) //Verifies if number of objects before and after rename cancellation operation are equal
				throw new Exception("Test case Failed. Number of objects before and after rename cancellation operation are not equal.");

			if (currVersionCt != prevVersionCt) //Verifies that number of items in History view has increased by 1 in History view
				throw new Exception("Test case Failed. Number of versions listed in History view is not same after rename cancellation operation.");

			//Verifies that Rename option is enabled in operations menu
			if (!homePage.listView.isItemExists(newName))
				Log.pass("Test case Passed. Rename cancellation operation for latest verion of an object has not renamed the object.");
			else
				Log.fail("Test case Failed. Rename cancellation operation for latest verion of an object has renamed the object.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest43_3_4_2C

	/**
	 * 43.3.4.3A : Cancelling Rename operation should not rename the related object in Relationships view- Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint43"}, 
			description = "Cancelling Rename operation should not rename the related object in Relationships view- Operations menu")
	public void SprintTest43_3_4_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			///Step-2 : Open Relationships view of an object
			//---------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) 
				throw new SkipException("Invalid Test data. The given object " + dataPool.get("ObjectName") + " was not found in the view.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Relationships.Value);
			Utils.fluentWait(driver);

			if (!ListView.isRelationshipsViewOpened(driver)) //Checks if History view is opened
				throw new Exception("Relationship view is not opened.");

			Log.message("2. Relationships view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Select related object and open rename dialog
			//------------------------------------------------------
			int prevCt = homePage.listView.itemCount(); //Gets item count from the list

			if (!homePage.listView.clickItem(dataPool.get("RelatedObject"))) //Selects the related object
				throw new Exception("Related object of an object is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Rename.Value); //Selects Rename from operations menuBar

			MFilesDialog mfilesDialog = new MFilesDialog(driver);

			if (!mfilesDialog.isRenameDialogOpened())
				throw new Exception("Rename dialog is not opened.");

			Log.message("3. Related object is selected and rename dialog is opened through operations menu.");

			//Step-4 : Enter the name and click Cancel button
			//-----------------------------------------------
			String newName = Utility.getObjectName(Utility.getMethodName());
			mfilesDialog.rename(newName, false); //New name is entered and cancel button is clicked

			Log.message("4. New name is entered and cancel button is clicked.");

			//Verification : To Verify if Rename operation is not successful
			//--------------------------------------------------------------
			int currCt = homePage.listView.itemCount(); //Gets item count from the list

			if (prevCt != currCt) //Verifies if number of objects before and after rename cancellation operation are equal
				throw new Exception("Test case Failed. Number of objects before and after rename cancellation operation are not equal.");

			//Verifies that Rename option is enabled in operations menu
			if (!homePage.listView.isItemExists(newName))
				Log.pass("Test case Passed. Rename cancellation operation for related object has not renamed the object.");
			else
				Log.fail("Test case Failed. Rename cancellation operation for related object has renamed the object.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest43_3_4_3A

	/**
	 * 43.3.4.3B : Cancelling Rename operation should not rename the related object in Relationships view- Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint43"}, 
			description = "Cancelling Rename operation should not rename the related object in Relationships view- Context menu")
	public void SprintTest43_3_4_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			///Step-2 : Open Relationships view of an object
			//----------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) 
				throw new SkipException("Invalid Test data. The given object " + dataPool.get("ObjectName") + " was not found in the view.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Relationships.Value);
			Utils.fluentWait(driver);

			if (!ListView.isRelationshipsViewOpened(driver)) //Checks if History view is opened
				throw new Exception("Relationship view is not opened.");

			Log.message("2. Relationships view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Select related object and open rename dialog
			//------------------------------------------------------
			int prevCt = homePage.listView.itemCount(); //Gets item count from the list

			if (!homePage.listView.rightClickItem(dataPool.get("RelatedObject"))) //Selects the related object
				throw new Exception("Related object of an object is not selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Rename.Value); //Selects Rename from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver);

			if (!mfilesDialog.isRenameDialogOpened())
				throw new Exception("Rename dialog is not opened.");

			Log.message("3. Related object is selected and rename dialog is opened through context menu.");

			//Step-4 : Enter the name and click Cancel button
			//-----------------------------------------------
			String newName = Utility.getObjectName(Utility.getMethodName()); 
			mfilesDialog.rename(newName, false); //New name is entered and cancel button is clicked

			Log.message("4. New name is entered and cancel button is clicked.");

			//Verification : To Verify if Rename operation is not successful
			//--------------------------------------------------------------
			int currCt = homePage.listView.itemCount(); //Gets item count from the list

			if (prevCt != currCt) //Verifies if number of objects before and after rename cancellation operation are equal
				throw new Exception("Test case Failed. Number of objects before and after rename cancellation operation are not equal.");

			//Verifies that Rename option is enabled in operations menu
			if (!homePage.listView.isItemExists(newName))
				Log.pass("Test case Passed. Rename cancellation operation for related object has not renamed the object.");
			else
				Log.fail("Test case Failed. Rename cancellation operation for related object has renamed the object.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest43_3_4_3B

	/**
	 * 43.3.4.3C : Cancelling Rename operation should not rename the related object in Relationships view- F2 key
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint43"}, 
			description = "Cancelling Rename operation should not rename the related object in Relationships view- F2 key")
	public void SprintTest43_3_4_3C(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("SAFARI") || driverType.equalsIgnoreCase("IE"))
			throw new SkipException(driverType.toUpperCase()+" does not support key actions");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			///Step-2 : Open Relationships view of an object
			//----------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) 
				throw new SkipException("Invalid Test data. The given object " + dataPool.get("ObjectName") + " was not found in the view.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Relationships.Value);
			Utils.fluentWait(driver);

			if (!ListView.isRelationshipsViewOpened(driver)) //Checks if History view is opened
				throw new Exception("Relationship view is not opened.");

			Log.message("2. Relationships view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Select related object and open rename dialog
			//------------------------------------------------------
			int prevCt = homePage.listView.itemCount(); //Gets item count from the list

			if (!homePage.listView.clickItem(dataPool.get("RelatedObject"))) //Selects the related object
				throw new Exception("Related object of an object is not selected.");

			Actions action = new Actions(driver);
			action.sendKeys(Keys.F2).perform(); //Press F2 key
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);

			if (!mfilesDialog.isRenameDialogOpened())
				throw new Exception("Rename dialog is not opened.");

			Log.message("3. Related object is selected and rename dialog is opened through F2 key.");

			//Step-4 : Enter the name and click Cancel button
			//-----------------------------------------------
			String newName = Utility.getObjectName(Utility.getMethodName()); 
			mfilesDialog.rename(newName, false); //New name is entered and cancel button is clicked

			Log.message("4. New name is entered and cancel button is clicked.");

			//Verification : To Verify if Rename operation is not successful
			//--------------------------------------------------------------
			int currCt = homePage.listView.itemCount(); //Gets item count from the list

			if (prevCt != currCt) //Verifies if number of objects before and after rename cancellation operation are equal
				throw new Exception("Test case Failed. Number of objects before and after rename cancellation operation are not equal.");

			//Verifies that Rename option is enabled in operations menu
			if (!homePage.listView.isItemExists(newName))
				Log.pass("Test case Passed. Rename cancellation operation for related object has not renamed the object.");
			else
				Log.fail("Test case Failed. Rename cancellation operation for related object has renamed the object.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest43_3_4_3C

	/**
	 * 43.3.4.4A : Cancelling Rename operation should not rename the sub object in Sub Objects view- Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint43"}, 
			description = "Cancelling Rename operation should not rename the sub object in Sub Objects view- Operations menu")
	public void SprintTest43_3_4_4A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-3 : Select sub object and open rename dialog
			//------------------------------------------------------
			int prevCt = homePage.listView.itemCount(); //Gets item count from the list

			if (!homePage.listView.clickItemByIndex(0)) //Selects the latest version of the object
				throw new Exception("Sub object of an object is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Rename.Value); //Selects Rename from operations menuBar

			MFilesDialog mfilesDialog = new MFilesDialog(driver);

			if (!mfilesDialog.isRenameDialogOpened())
				throw new Exception("Rename dialog is not opened.");

			Log.message("3. Sub object is selected and rename dialog is opened through operations menu.");

			//Step-4 : Enter the name and click Cancel button
			//-----------------------------------------------
			String newName = Utility.getObjectName(Utility.getMethodName()); 
			mfilesDialog.rename(newName, false); //New name is entered and cancel button is clicked

			Log.message("4. New name is entered and cancel button is clicked.");

			//Verification : To Verify if Rename operation is not successful
			//--------------------------------------------------------------
			int currCt = homePage.listView.itemCount(); //Gets item count from the list

			if (prevCt != currCt) //Verifies if number of objects before and after rename cancellation operation are equal
				throw new Exception("Test case Failed. Number of objects before and after rename cancellation operation are not equal.");

			//Verifies that Rename option is enabled in operations menu
			if (!homePage.listView.isItemExists(newName))
				Log.pass("Test case Passed. Rename cancellation operation for sub object has not renamed the object.");
			else
				Log.fail("Test case Failed. Rename cancellation operation for sub object has renamed the object.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest43_3_4_4A

	/**
	 * 43.3.4.4B : Cancelling Rename operation should not rename the sub object in Sub Objects view- Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint43"}, 
			description = "Cancelling Rename operation should not rename the sub object in Sub Objects view- Context menu")
	public void SprintTest43_3_4_4B(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-3 : Select sub object and open rename dialog
			//------------------------------------------------------
			int prevCt = homePage.listView.itemCount(); //Gets item count from the list

			if (!homePage.listView.rightClickItemByIndex(0)) //Selects the latest version of the object
				throw new Exception("Sub object of an object is not selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Rename.Value); //Selects Rename from operations menuBar

			MFilesDialog mfilesDialog = new MFilesDialog(driver);

			if (!mfilesDialog.isRenameDialogOpened())
				throw new Exception("Rename dialog is not opened.");

			Log.message("3. Sub object is selected and rename dialog is opened through context menu.");

			//Step-4 : Enter the name and click Cancel button
			//-----------------------------------------------
			String newName = Utility.getObjectName(Utility.getMethodName()); 
			mfilesDialog.rename(newName, false); //New name is entered and cancel button is clicked

			Log.message("4. New name is entered and cancel button is clicked.");

			//Verification : To Verify if Rename operation is not successful
			//--------------------------------------------------------------
			int currCt = homePage.listView.itemCount(); //Gets item count from the list

			if (prevCt != currCt) //Verifies if number of objects before and after rename cancellation operation are equal
				throw new Exception("Test case Failed. Number of objects before and after rename cancellation operation are not equal.");

			//Verifies that Rename option is enabled in operations menu
			if (!homePage.listView.isItemExists(newName))
				Log.pass("Test case Passed. Rename cancellation operation for sub object has not renamed the object.");
			else
				Log.fail("Test case Failed. Rename cancellation operation for sub object has renamed the object.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest43_3_4_4B

	/**
	 * 43.3.4.4C : Cancelling Rename operation should not rename the sub object in Sub Objects view- F2 key
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint43"}, 
			description = "Cancelling Rename operation should not rename the sub object in Sub Objects view- F2 key")
	public void SprintTest43_3_4_4C(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("SAFARI") || driverType.equalsIgnoreCase("IE"))
			throw new SkipException(driverType.toUpperCase()+" does not support key actions");

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

			//Step-3 : Select sub object and open rename dialog
			//------------------------------------------------------
			int prevCt = homePage.listView.itemCount(); //Gets item count from the list

			if (!homePage.listView.clickItemByIndex(0)) //Selects the latest version of the object
				throw new Exception("Sub object of an object is not selected.");

			Actions action = new Actions(driver);
			action.sendKeys(Keys.F2).perform(); //Press F2 key
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);

			if (!mfilesDialog.isRenameDialogOpened())
				throw new Exception("Rename dialog is not opened.");

			Log.message("3. Sub object is selected and rename dialog is opened by pressing F2 key.");

			//Step-4 : Enter the name and click Cancel button
			//-----------------------------------------------
			String newName = Utility.getObjectName(Utility.getMethodName()); 
			mfilesDialog.rename(newName, false); //New name is entered and cancel button is clicked

			Log.message("4. New name is entered and cancel button is clicked.");

			//Verification : To Verify if Rename operation is not successful
			//--------------------------------------------------------------
			int currCt = homePage.listView.itemCount(); //Gets item count from the list

			if (prevCt != currCt) //Verifies if number of objects before and after rename cancellation operation are equal
				throw new Exception("Test case Failed. Number of objects before and after rename cancellation operation are not equal.");

			//Verifies that Rename option is enabled in operations menu
			if (!homePage.listView.isItemExists(newName))
				Log.pass("Test case Passed. Rename cancellation operation for sub object has not renamed the object.");
			else
				Log.fail("Test case Failed. Rename cancellation operation for sub object has renamed the object.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest43_3_4_4C

	/**
	 * 43.3.4.5A : Cancelling Rename operation should not rename the member object in Members view- Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint43"}, 
			description = "Cancelling Rename operation should not rename the member object in Members view- Operations menu")
	public void SprintTest43_3_4_5A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-3 : Select member object and open rename dialog
			//------------------------------------------------------
			int prevCt = homePage.listView.itemCount(); //Gets item count from the list

			if (!homePage.listView.clickItemByIndex(0)) //Selects the latest version of the object
				throw new Exception("Member object of an object is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Rename.Value); //Selects Rename from operations menuBar

			MFilesDialog mfilesDialog = new MFilesDialog(driver);

			if (!mfilesDialog.isRenameDialogOpened())
				throw new Exception("Rename dialog is not opened.");

			Log.message("3. Member object is selected and rename dialog is opened through operations menu.");

			//Step-4 : Enter the name and click Cancel button
			//-----------------------------------------------
			String newName = Utility.getObjectName(Utility.getMethodName()); 
			mfilesDialog.rename(newName, false); //New name is entered and cancel button is clicked

			Log.message("4. New name is entered and cancel button is clicked.");

			//Verification : To Verify if Rename operation is not successful
			//--------------------------------------------------------------
			int currCt = homePage.listView.itemCount(); //Gets item count from the list

			if (prevCt != currCt) //Verifies if number of objects before and after rename cancellation operation are equal
				throw new Exception("Test case Failed. Number of objects before and after rename cancellation operation are not equal.");

			//Verifies that Rename option is enabled in operations menu
			if (!homePage.listView.isItemExists(newName))
				Log.pass("Test case Passed. Rename cancellation operation for sub object has not renamed the object.");
			else
				Log.fail("Test case Failed. Rename cancellation operation for sub object has renamed the object.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest43_3_4_5A

	/**
	 * 43.3.4.5B : Cancelling Rename operation should not rename the member object in Members view- Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint43"}, 
			description = "Cancelling Rename operation should not rename the member object in Members view- Context menu")
	public void SprintTest43_3_4_5B(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-3 : Select member object and open rename dialog
			//------------------------------------------------------
			int prevCt = homePage.listView.itemCount(); //Gets item count from the list

			if (!homePage.listView.rightClickItemByIndex(0)) //Selects the latest version of the object
				throw new Exception("Member object of an object is not selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Rename.Value); //Selects Rename from operations menuBar

			MFilesDialog mfilesDialog = new MFilesDialog(driver);

			if (!mfilesDialog.isRenameDialogOpened())
				throw new Exception("Rename dialog is not opened.");

			Log.message("3. Member object is selected and rename dialog is opened through context menu.");

			//Step-4 : Enter the name and click Cancel button
			//-----------------------------------------------
			String newName = Utility.getObjectName(Utility.getMethodName()); 
			mfilesDialog.rename(newName, false); //New name is entered and cancel button is clicked

			Log.message("4. New name is entered and cancel button is clicked.");

			//Verification : To Verify if Rename operation is not successful
			//--------------------------------------------------------------
			int currCt = homePage.listView.itemCount(); //Gets item count from the list

			if (prevCt != currCt) //Verifies if number of objects before and after rename cancellation operation are equal
				throw new Exception("Test case Failed. Number of objects before and after rename cancellation operation are not equal.");

			//Verifies that Rename option is enabled in operations menu
			if (!homePage.listView.isItemExists(newName))
				Log.pass("Test case Passed. Rename cancellation operation for sub object has not renamed the object.");
			else
				Log.fail("Test case Failed. Rename cancellation operation for sub object has renamed the object.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest43_3_4_5B

	/**
	 * 43.3.4.5C : Cancelling Rename operation should not rename the member object in Members view- F2 key
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint43"}, 
			description = "Cancelling Rename operation should not rename the member object in Members view- F2 key")
	public void SprintTest43_3_4_5C(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("SAFARI") || driverType.equalsIgnoreCase("IE"))
			throw new SkipException(driverType.toUpperCase()+" does not support key actions");		

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

			//Step-3 : Select member object and open rename dialog
			//------------------------------------------------------
			int prevCt = homePage.listView.itemCount(); //Gets item count from the list

			if (!homePage.listView.clickItemByIndex(0)) //Selects the latest version of the object
				throw new Exception("Member object of an object is not selected.");

			Actions action = new Actions(driver);
			action.sendKeys(Keys.F2).perform(); //Press F2 key
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver);

			if (!mfilesDialog.isRenameDialogOpened())
				throw new Exception("Rename dialog is not opened.");

			Log.message("3. Member object is selected and rename dialog is opened by pressing F2 key.");

			//Step-4 : Enter the name and click Cancel button
			//-----------------------------------------------
			String newName = Utility.getObjectName(Utility.getMethodName());
			mfilesDialog.rename(newName, false); //New name is entered and cancel button is clicked

			Log.message("4. New name is entered and cancel button is clicked.");

			//Verification : To Verify if Rename operation is not successful
			//--------------------------------------------------------------
			int currCt = homePage.listView.itemCount(); //Gets item count from the list

			if (prevCt != currCt) //Verifies if number of objects before and after rename cancellation operation are equal
				throw new Exception("Test case Failed. Number of objects before and after rename cancellation operation are not equal.");

			//Verifies that Rename option is enabled in operations menu
			if (!homePage.listView.isItemExists(newName))
				Log.pass("Test case Passed. Rename cancellation operation for sub object has not renamed the object.");
			else
				Log.fail("Test case Failed. Rename cancellation operation for sub object has renamed the object.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest43_3_4_5C

} //End class ObjectActions
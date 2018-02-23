package MFClient.Tests.MetadataOperations;

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
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class MakeCopy {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String userName = null;
	public static String password = null;
	public static String testVault = null;
	public static String testVault2 = null;
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
			className = this.getClass().getSimpleName().toString().trim();
			if (xmlParameters.getParameter("driverType").equalsIgnoreCase("IE"))
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim() + xmlParameters.getParameter("driverVersion").trim();
			else
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim();
			userName = xmlParameters.getParameter("UserName");
			password = xmlParameters.getParameter("Password");
			testVault = xmlParameters.getParameter("VaultName");		
			userFullName = xmlParameters.getParameter("UserFullName");

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
	 * 58.1.1 : Verifying the make copy in web access
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint58", "Metadatacard", "Smoke"}, 
			description = "Verifying the make copy in web access")
	public void SprintTest58_1_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object
			//-------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType")+"s");

			Log.message("2. Search for the object");

			//3. Click the Object
			//--------------------
			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("The Specified object does not exist in the vault.");

			Log.message("3. Click the Object");

			//4. Click the Make Copy option from the Task Pane
			//-------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.MakeCopy.Value);
			Utils.fluentWait(driver);

			Log.message("4. Click the Make Copy option from the Task Pane");

			//Verification: To verify if the Metadatacard appears
			//----------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver, "New " + dataPool.get("ObjectType"));
			if(metadatacard.isEditMode())
				Log.pass("Test Case Passed. The Warning message appeared as expected.");
			else
				Log.fail("Test Case Failed. The Warning message did not appear.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.1.2A : Verify the 'Make copy' functionality
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint58", "Metadatacard"}, 
			description = "Verify the 'Make copy' functionality")
	public void SprintTest58_1_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object
			//-------------------------
			Utils.fluentWait(driver);
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType")+"s");

			Log.message("2. Search for the object");

			//3. Click the Object
			//--------------------
			Utils.fluentWait(driver);
			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("The Specified object does not exist in the vault.");

			Log.message("3. Click the Object");

			//4. Click the Make Copy option from the Task Pane
			//-------------------------------------------------
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.MakeCopy.Value);
			Utils.fluentWait(driver);

			Log.message("4. Click the Make Copy option from the Task Pane");

			//5. Click the Save Button in metadatacard
			//-----------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver, "New " + dataPool.get("ObjectType"));
			metadatacard.setCheckInImmediately(true);
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("5. Click the Save Button in metadatacard");

			//Verification: To verify if the copy of the object is created in the same name
			//------------------------------------------------------------------------------
			Utils.fluentWait(driver);
			if(homePage.listView.getItemOccurence(dataPool.get("Object"), "Name") == 2)
				Log.pass("Test Case Passed. The Copy of the object is created as expected.");
			else
				Log.fail("Test Case Failed. The Copy of the object is not created.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.1.2B : Verify the "Make copy" functionality (Properties verification)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint58", "Metadatacard"}, 
			description = "Verify the 'Make copy' functionality (Properties verification)")
	public void SprintTest58_1_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object
			//-------------------------
			Utils.fluentWait(driver);
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType")+"s");

			Log.message("2. Search for the object");

			//3. Click the Object
			//--------------------
			Utils.fluentWait(driver);
			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("The Specified object does not exist in the vault.");

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver, true);
			ConcurrentHashMap<String, String> expectedProps = metadatacard.getInfo();
			driver.switchTo().defaultContent();

			Log.message("3. Click the Object");

			//4. Click the Make Copy option from the Task Pane
			//-------------------------------------------------
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.MakeCopy.Value);
			Utils.fluentWait(driver);

			Log.message("4. Click the Make Copy option from the Task Pane");

			//5. Click the Save Button in metadatacard
			//-----------------------------------------
			metadatacard = new MetadataCard(driver, "New " + dataPool.get("ObjectType"));
			metadatacard.setCheckInImmediately(true);
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("5. Click the Save Button in metadatacard");

			//Verification: To verify if the copy of the object is created in the same name
			//------------------------------------------------------------------------------
			Utils.fluentWait(driver);
			metadatacard = new MetadataCard(driver, true);
			if(Utility.compareObjects(metadatacard.getInfo(), expectedProps).equals(("")))
				Log.pass("Test Case Passed. The Object was copied with the same properties");
			else
				Log.fail("Test Case Failed. The Object was not copied with the same proeperties", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.1.3 : Verify the 'Make copy' functionality (Check In)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint58", "Metadatacard", "Smoke"}, 
			description = "Verify the 'Make copy' functionality (Check In)")
	public void SprintTest58_1_3(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object
			//-------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType")+"s");

			Log.message("2. Search for the object");

			//3. Click the Object
			//--------------------
			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("The Specified object does not exist in the vault.");

			Log.message("3. Click the Object");

			//4. Click the Make Copy option from the Task Pane
			//-------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.MakeCopy.Value);
			Utils.fluentWait(driver);

			Log.message("4. Click the Make Copy option from the Task Pane");

			//5. Click the Save Button in metadatacard
			//-----------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver, "New " + dataPool.get("ObjectType"));
			metadatacard.setPropertyValue(dataPool.get("NameProperty"), dataPool.get("ObjectName"));
			metadatacard.setCheckInImmediately(true);
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("5. Click the Save Button in metadatacard");

			//6. Search for the object
			//------------------------
			String objectName = dataPool.get("ObjectName");

			if(!dataPool.get("Extension").equals(""))
				objectName = objectName+"."+dataPool.get("Extension");

			Utils.fluentWait(driver);
			homePage.searchPanel.search(objectName, "Search only: " + dataPool.get("ObjectType")+"s");
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			if(!homePage.listView.isItemExists(objectName))
				throw new Exception("Make Copy not successful. The Object was not created.");

			Log.message("6. Search for the object.");

			//Verification: To verify if the copy of the object is created in checked in state
			//--------------------------------------------------------------------------------
			homePage.listView.insertColumn("Checkout Time");

			if(homePage.listView.getColumnValueByItemName(objectName, "Checkout Time").equals(""))
				Log.pass("Test Case Passed. The Copy of the object is created and is checked in as expected.");
			else
				Log.fail("Test Case Failed. The Copy of the object is not created but it is not in checked in state.", driver);

			homePage.listView.removeColumn("Checkout Time");
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.1.4 : Verify the 'Make copy' functionality (Uncheck Check In)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint58", "Metadatacard"}, 
			description = "Verify the 'Make copy' functionality (Uncheck Check In)")
	public void SprintTest58_1_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object
			//-------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType")+"s");

			Log.message("2. Search for the object");

			//3. Click the Object
			//--------------------
			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("The Specified object does not exist in the vault.");

			Log.message("3. Click the Object");

			//4. Click the Make Copy option from the Task Pane
			//-------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.MakeCopy.Value);
			Utils.fluentWait(driver);

			Log.message("4. Click the Make Copy option from the Task Pane");

			//5. Click the Save Button in metadatacard
			//-----------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver, "New " + dataPool.get("ObjectType"));
			metadatacard.setPropertyValue(dataPool.get("NameProperty"), dataPool.get("ObjectName"));
			metadatacard.setCheckInImmediately(false);
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("5. Click the Save Button in metadatacard");

			//6. Search for the object
			//------------------------
			String objectName = dataPool.get("ObjectName");

			if(!dataPool.get("Extension").equals(""))
				objectName = objectName+"."+dataPool.get("Extension");

			Utils.fluentWait(driver);
			homePage.searchPanel.search(objectName, "Search only: " + dataPool.get("ObjectType")+"s");
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(objectName))
				throw new Exception("Make Copy not successful. The Object was not created.");

			Log.message("6. Search for the object.");

			//Verification: To verify if the copy of the object is created in checked in state
			//--------------------------------------------------------------------------------
			homePage.listView.insertColumn("Checkout Time");

			if(!homePage.listView.getColumnValueByItemName(objectName, "Checkout Time").equals(""))
				Log.pass("Test Case Passed. The Copy of the object is created and is checked in as expected.");
			else
				Log.fail("Test Case Failed. The Copy of the object is not created but it is not in checked in state.", driver);

			homePage.listView.removeColumn("Checkout Time");
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.1.5 : Make copy of object with no workflow
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint58", "Metadatacard"}, 
			description = "Make copy of object with no workflow")
	public void SprintTest58_1_5(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object
			//-------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType")+"s");

			Log.message("2. Search for the object");

			//3. Click the Object
			//--------------------
			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("The Specified object does not exist in the vault.");

			MetadataCard metadatacard = new MetadataCard(driver, true);

			if(!metadatacard.getWorkflow().equals(""))
				throw new SkipException("Invalid Test Data. Specify an object with no workflow");

			driver.switchTo().defaultContent();

			Log.message("3. Click the Object");

			//4. Click the Make Copy option from the Task Pane
			//-------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.MakeCopy.Value);
			Utils.fluentWait(driver);

			Log.message("4. Click the Make Copy option from the Task Pane");

			//5. Click the Save Button in metadatacard
			//-----------------------------------------
			metadatacard = new MetadataCard(driver, "New " + dataPool.get("ObjectType"));
			metadatacard.setPropertyValue(dataPool.get("NameProperty"), dataPool.get("ObjectName"));
			metadatacard.setCheckInImmediately(true);
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("5. Click the Save Button in metadatacard");

			//6. Search for the object
			//------------------------
			String objectName = dataPool.get("ObjectName");

			if(!dataPool.get("Extension").equals(""))
				objectName = objectName+"."+dataPool.get("Extension");

			homePage.searchPanel.search(objectName, "Search only: " + dataPool.get("ObjectType")+"s");

			if(!homePage.listView.clickItem(objectName))
				throw new Exception("Make Copy not successful. The Object was not created.");

			Log.message("6. Search for the object.");

			//Verification: To verify if the Workflow remains empty
			//------------------------------------------------------
			metadatacard = new MetadataCard(driver, true);

			if(metadatacard.getWorkflow().equals("") && metadatacard.getWorkflowState().equals(""))
				Log.pass("Test Case Passed. The Workflow of the copied object remained empty.");
			else
				Log.fail("Test Case Failed. The Workflow of the copied object did not remain empty.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.1.6 : Make copy of object with workflow and state
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint58", "Metadatacard"}, 
			description = "Make copy of object with workflow and state")
	public void SprintTest58_1_6(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object
			//-------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType")+"s");

			Log.message("2. Search for the object");

			//3. Click the Object
			//--------------------
			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("The Specified object does not exist in the vault.");

			MetadataCard metadatacard = new MetadataCard(driver, true);

			if(!metadatacard.getWorkflow().equals(dataPool.get("Workflow")))
				throw new SkipException("Invalid Test Data. The Object did not have the specified workflow.");

			driver.switchTo().defaultContent();

			Log.message("3. Click the Object");

			//4. Click the Make Copy option from the Task Pane
			//-------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.MakeCopy.Value);
			Utils.fluentWait(driver);

			Log.message("4. Click the Make Copy option from the Task Pane");

			//5. Click the Save Button in metadatacard
			//-----------------------------------------
			metadatacard = new MetadataCard(driver, "New " + dataPool.get("ObjectType"));
			metadatacard.setPropertyValue(dataPool.get("NameProperty"), dataPool.get("ObjectName"));
			metadatacard.setCheckInImmediately(true);
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("5. Click the Save Button in metadatacard");

			//6. Search for the object
			//------------------------
			String objectName = dataPool.get("ObjectName");

			if(!dataPool.get("Extension").equals(""))
				objectName = objectName + "." + dataPool.get("Extension");

			Utils.fluentWait(driver);
			homePage.searchPanel.search(objectName, "Search only: " + dataPool.get("ObjectType")+"s");
			Utils.fluentWait(driver);

			if(!homePage.listView.clickItem(objectName))
				throw new Exception("Make Copy not successful. The Object was not created.");
			Utils.fluentWait(driver);

			Log.message("6. Search for the object.");

			//Verification: To verify if the Workflow and the first state are set to the copied object
			//-----------------------------------------------------------------------------------------
			Utils.fluentWait(driver);
			metadatacard = new MetadataCard(driver,true);

			if(metadatacard.getWorkflow().equals(dataPool.get("Workflow")) && metadatacard.getWorkflowState().equals(dataPool.get("State")))
				Log.pass("Test Case Passed. The Workflow and the first state are set to the copied object.");
			else
				Log.fail("Test Case Failed. The Workflow and the first state are not set to the copied object.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.1.7 : By Default Open for Editing should be checked when Make Copy is done for Documents
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint58", "Metadatacard","Bug"}, 
			description = "By Default Open for Editing should be checked when Make Copy is done for Documents")
	public void SprintTest58_1_7(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object
			//-------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType")+"s");

			Log.message("2. Search for the object");

			//3. Click the Object
			//--------------------
			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("The Specified object does not exist in the vault.");

			Log.message("3. Click the Object");

			//4. Click the Make Copy option from the Task Pane
			//-------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.MakeCopy.Value);
			Utils.fluentWait(driver);

			Log.message("4. Click the Make Copy option from the Task Pane");

			//Verification: To verify if the Open for Editing checkbox is checked
			//-----------------------------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);
			if(metadatacard.isOpenForEditing())
				Log.pass("Test Case Passed. Open for editing check box is checked as expected.");
			else
				Log.fail("Test Case Failed. Open for editing check box is not checked by default.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.1.8 : By Default Open for Editing should be checked when Make Copy is done for Documents
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint58", "Metadatacard","Bug"}, 
			description = "By Default Open for Editing should be checked when Make Copy is done for Documents")
	public void SprintTest58_1_8(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object
			//-------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType")+"s");

			Log.message("2. Search for the object");

			//3. Click the Object
			//--------------------
			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("The Specified object does not exist in the vault.");

			Log.message("3. Click the Object");

			//4. Click the Make Copy option from the Task Pane
			//-------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.MakeCopy.Value);
			Utils.fluentWait(driver);

			Log.message("4. Click the Make Copy option from the Task Pane");

			//Verification: To verify if the Open for Editing checkbox is checked
			//-----------------------------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);
			if(metadatacard.isOpenForEditing())
				Log.pass("Test Case Passed. Open for editing check box is checked as expected.");
			else
				Log.fail("Test Case Failed. Open for editing check box is not checked by default.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.1.9 : Check Out an object and Make Copy with Check In checkbox checked
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint58", "Metadatacard"}, 
			description = "Check Out an object and Make Copy with Check In checkbox checked")
	public void SprintTest58_1_9(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object
			//-------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType")+"s");

			Log.message("2. Search for the object");

			//3. Check out the object
			//------------------------
			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("The Specified object does not exist in the vault.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);

			Log.message("3. Check out the object.");

			//4. Click the Make Copy option from the Task Pane
			//-------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.MakeCopy.Value);
			Utils.fluentWait(driver);

			Log.message("4. Click the Make Copy option from the Task Pane");

			//5. Click the Save Button in metadatacard
			//-----------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver, "New " + dataPool.get("ObjectType"));
			metadatacard.setPropertyValue(dataPool.get("NameProperty"), dataPool.get("ObjectName"));
			metadatacard.setCheckInImmediately(true);
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("5. Click the Save Button in metadatacard");

			//6. Search for the object
			//------------------------
			String objectName = dataPool.get("ObjectName");

			if(!dataPool.get("Extension").equals(""))
				objectName = objectName+"."+dataPool.get("Extension");

			Utils.fluentWait(driver);
			homePage.searchPanel.search(objectName, "Search only: " + dataPool.get("ObjectType")+"s");
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(objectName))
				throw new Exception("Make Copy not successful. The Object was not created.");

			Log.message("6. Search for the object.");

			//Verification: To verify if the copy of the object is created in checked in state
			//--------------------------------------------------------------------------------
			homePage.listView.insertColumn("Checkout Time");

			if(homePage.listView.getColumnValueByItemName(objectName, "Checkout Time").equals(""))
				Log.pass("Test Case Passed. The Copy of the object is created and is checked in as expected.");
			else
				Log.fail("Test Case Failed. The Copy of the object is not created but it is not in checked in state.", driver);

			homePage.listView.removeColumn("Checkout Time");
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.1.10 : Check Out an object and Make Copy with Check In checkbox unchecked
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint58", "Metadatacard"}, 
			description = "Check Out an object and Make Copy with Check In checkbox unchecked")
	public void SprintTest58_1_10(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object
			//-------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType")+"s");

			Log.message("2. Search for the object");

			//3. Check out the object
			//------------------------
			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("The Specified object does not exist in the vault.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);

			Log.message("3. Check out the object.");

			//4. Click the Make Copy option from the Task Pane
			//-------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.MakeCopy.Value);
			Utils.fluentWait(driver);

			Log.message("4. Click the Make Copy option from the Task Pane");

			//5. Click the Save Button in metadatacard
			//-----------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver, "New " + dataPool.get("ObjectType"));
			metadatacard.setPropertyValue(dataPool.get("NameProperty"), dataPool.get("ObjectName"));
			metadatacard.setOpenForEditing(false);
			metadatacard.setCheckInImmediately(false);
			metadatacard.saveAndClose();

			Log.message("5. Click the Save Button in metadatacard");

			//6. Search for the object
			//------------------------
			String objectName = dataPool.get("ObjectName");

			if(!dataPool.get("Extension").equals(""))
				objectName = objectName+"."+dataPool.get("Extension");

			homePage.searchPanel.search(objectName, "Search only: " + dataPool.get("ObjectType")+"s");

			if(!homePage.listView.isItemExists(objectName))
				throw new Exception("Make Copy not successful. The Object was not created.");

			Log.message("6. Search for the object.");

			//Verification: To verify if the copy of the object is created in checked in state
			//--------------------------------------------------------------------------------
			homePage.listView.insertColumn("Checkout Time");

			if(!homePage.listView.getColumnValueByItemName(objectName, "Checkout Time").equals(""))
				Log.pass("Test Case Passed. The Copy of the object is created and is not checked in as expected.");
			else
				Log.fail("Test Case Failed. The Copy of the object is not created but it is in checked in state.", driver);

			homePage.listView.removeColumn("Checkout Time");
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.1.11A : Check Out an object and Make Copy with Check In checkbox unchecked
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint58", "Metadatacard"}, 
			description = "Check Out an object and Make Copy with Check In checkbox unchecked")
	public void SprintTest58_1_11A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object
			//-------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType")+"s");

			Log.message("2. Search for the object");

			//3. Check out the object
			//------------------------
			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("The Specified object does not exist in the vault.");

			if(homePage.taskPanel.isItemExists(Caption.MenuItems.CheckOut.Value))
				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);

			Log.message("3. Check out the object.");

			//4. Make some changes to the object and make copy
			//-------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver, true);
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			homePage.taskPanel.clickItem(Caption.MenuItems.MakeCopy.Value);
			Utils.fluentWait(driver);

			Log.message("4. Make some changes to the object and make copy");

			//5. Click the Save Button in metadatacard
			//-----------------------------------------
			metadatacard = new MetadataCard(driver, "New " + dataPool.get("ObjectType"));
			metadatacard.setPropertyValue(dataPool.get("NameProperty"), dataPool.get("ObjectName"));
			metadatacard.setOpenForEditing(false);
			metadatacard.setCheckInImmediately(false);
			metadatacard.saveAndClose();

			Log.message("5. Click the Save Button in metadatacard");

			//6. Search for the object
			//------------------------
			String objectName = dataPool.get("ObjectName");

			if(!dataPool.get("Extension").equals(""))
				objectName = objectName+"."+dataPool.get("Extension");

			Utils.fluentWait(driver);
			homePage.searchPanel.search(objectName, "Search only: " + dataPool.get("ObjectType")+"s");
			Utils.fluentWait(driver);

			if(!homePage.listView.clickItem(objectName))
				throw new Exception("Make Copy not successful. The Object was not created.");

			Log.message("6. Search for the object.");

			//Verification: To verify if the copy of the object is created in checked in state
			//--------------------------------------------------------------------------------
			Utils.fluentWait(driver);
			metadatacard = new MetadataCard(driver, true);

			if(metadatacard.getPropertyValue(dataPool.get("Property")).equals(dataPool.get("Value")))
				Log.pass("Test Case Passed. The Copy of the object has the changes done after check out.");
			else
				Log.fail("Test Case Failed. The Copy of the object did not have the changes done after check out.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.1.11B : Check Out an object and Make Copy with Check In checkbox unchecked
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint58", "Metadatacard"}, 
			description = "Check Out an object and Make Copy with Check In checkbox unchecked")
	public void SprintTest58_1_11B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object
			//-------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType")+"s");

			Log.message("2. Search for the object");

			//3. Check out the object
			//------------------------
			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("The Specified object does not exist in the vault.");

			if(homePage.taskPanel.isItemExists(Caption.MenuItems.CheckOut.Value))
				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);

			Log.message("3. Check out the object.");

			//4. Make some changes to the object and make copy
			//-------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver, true);
			metadatacard.addNewProperty(dataPool.get("Property"));
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			homePage.taskPanel.clickItem(Caption.MenuItems.MakeCopy.Value);
			Utils.fluentWait(driver);

			Log.message("4. Make some changes to the object and make copy");

			//5. Click the Save Button in metadatacard
			//-----------------------------------------
			metadatacard = new MetadataCard(driver, "New " + dataPool.get("ObjectType"));
			metadatacard.setPropertyValue(dataPool.get("NameProperty"), dataPool.get("ObjectName"));
			metadatacard.setOpenForEditing(false);
			metadatacard.setCheckInImmediately(false);
			metadatacard.saveAndClose();

			Log.message("5. Click the Save Button in metadatacard");

			//6. Search for the object
			//------------------------
			String objectName = dataPool.get("ObjectName");

			if(!dataPool.get("Extension").equals(""))
				objectName = objectName+"."+dataPool.get("Extension");

			homePage.searchPanel.search(objectName, "Search only: " + dataPool.get("ObjectType")+"s");

			if(!homePage.listView.clickItem(objectName))
				throw new Exception("Make Copy not successful. The Object was not created.");

			Log.message("6. Search for the object.");

			//Verification: To verify if the copy of the object is created in checked in state
			//--------------------------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true);

			if(metadatacard.propertyExists(dataPool.get("Property")))
				Log.pass("Test Case Passed. The Copy of the object has the changes done after check out.");
			else
				Log.fail("Test Case Failed. The Copy of the object did not have the changes done after check out.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.1.12 : Focus on name property in metadatacard when make copy is clicked
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint58", "Metadatacard"}, 
			description = "Focus on name property in metadatacard when make copy is clicked")
	public void SprintTest58_1_12(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver(); 

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object
			//-------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType")+"s");

			Log.message("2. Search for the object");

			//3. Click the Object
			//--------------------
			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("The Specified object does not exist in the vault.");

			Log.message("3. Click the Object");

			//4. Click the Make Copy option from the Task Pane
			//-------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.MakeCopy.Value);
			Utils.fluentWait(driver);
			Thread.sleep(5000);
			MetadataCard metadataCard = new MetadataCard(driver);

			Log.message("4. Click the Make Copy option from the Task Pane");

			//Verification: To verify if the focus is in the name property in the metadatacard
			//---------------------------------------------------------------------------------
			/*int count = 0;
			WebElement propTable = driver.findElement(By.cssSelector("table[id='mf-property-table']"));
			List<WebElement> props = propTable.findElements(By.cssSelector("tr[class*='mf-property-'][class*='mf-dynamic-row']>td[class*='mf-dynamic-namefield']>div>span[class*='label']"));

			for(count = 0; count < props.size(); count++) {
				if(props.get(count).getText().trim().equals(dataPool.get("NameProperty")))
					break;
			}
			WebElement field = props.get(count).findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.cssSelector("td[class*='mf-dynamic-controlfield']"));*/


			//Verification: To verify if the focus is in the name property in the metadatacard
			//--------------------------------------------------------------------------------
			if(metadataCard.isPropertyInEditMode(dataPool.get("NameProperty")))
				Log.pass("Test Case Passed. The focus is in the name property as expected.");
			else
				Log.fail("Test Case Failed. The focus was not in the name property.", driver);

		}

		catch(Exception e) {
			if(e.getClass().toString().contains("NoSuchElementException")) 
				Log.fail("Test Case Failed. The focus was not in the name property.", driver);
			else
				Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.1.13 : Icon displayed in metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint58", "Metadatacard"}, 
			description = "Icon displayed in metadatacard")
	public void SprintTest58_1_13(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object
			//-------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType")+"s");
			Log.message("2. Search for the object");

			//3. Click the Object
			//--------------------
			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("The Specified object does not exist in the vault.");

			MetadataCard metadatacard = new MetadataCard(driver, true);
			String expectedIcon = metadatacard.getObjectIcon();
			driver.switchTo().defaultContent();

			Log.message("3. Click the Object");

			//4. Click the Make Copy option from the Task Pane
			//-------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.MakeCopy.Value);
			Utils.fluentWait(driver);
			metadatacard = new MetadataCard(driver);

			Log.message("4. Click the Make Copy option from the Task Pane");

			//Verification: To verify if the same icon is displayed in the copied object
			//---------------------------------------------------------------------------
			if(expectedIcon.equals(metadatacard.getObjectIcon()))
				Log.pass("Test Case Passed. The Icon displayed is the same as the object copied.");
			else
				Log.fail("Test Case Failed. The Icon displayed is different than the object copied.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.1.14 : Name property value should be preserved in make copy operation
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint58", "Metadatacard"}, 
			description = "Name property value should be preserved in make copy operation")
	public void SprintTest58_1_14(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object
			//-------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType")+"s");

			Log.message("2. Search for the object");

			//3. Click the Object
			//--------------------
			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("The Specified object does not exist in the vault.");

			MetadataCard metadatacard = new MetadataCard(driver, true);
			String expectedValue = metadatacard.getPropertyValue(dataPool.get("NameProperty"));
			driver.switchTo().defaultContent();

			Log.message("3. Click the Object");

			//4. Click the Make Copy option from the Task Pane
			//-------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.MakeCopy.Value);
			Utils.fluentWait(driver);
			metadatacard = new MetadataCard(driver);
			metadatacard.savePropValue(dataPool.get("NameProperty"));
			Utils.fluentWait(driver);

			Log.message("4. Click the Make Copy option from the Task Pane");

			//Verification: To verify if the value of the name property is preserved
			//-----------------------------------------------------------------------
			if(expectedValue.equals(metadatacard.getPropertyValue(dataPool.get("NameProperty"))))
				Log.pass("Test Case Passed. The value of the name property is preserved as expected.");
			else
				Log.fail("Test Case Failed. The value of the name property was not the same as the copied object.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.1.15 : Name property value should be preserved in make copy operation (Contact Person)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint58", "Metadatacard"}, 
			description = "Name property value should be preserved in make copy operation (Contact Person)")
	public void SprintTest58_1_15(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object
			//-------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType")+"s");

			Log.message("2. Search for the object");

			//3. Click the Object
			//--------------------
			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("The Specified object does not exist in the vault.");

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver, true);
			String expectedValue1 = metadatacard.getPropertyValue(dataPool.get("Property1"));
			String expectedValue2 = metadatacard.getPropertyValue(dataPool.get("Property2"));
			driver.switchTo().defaultContent();

			Log.message("3. Click the Object");

			//4. Click the Make Copy option from the Task Pane
			//-------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.MakeCopy.Value);
			Utils.fluentWait(driver);
			metadatacard = new MetadataCard(driver);
			metadatacard.savePropValue(dataPool.get("Property1"));

			Log.message("4. Click the Make Copy option from the Task Pane");

			//Verification: To verify if the value of the name property is preserved
			//-----------------------------------------------------------------------
			if(expectedValue1.equals(metadatacard.getPropertyValue(dataPool.get("Property1"))) && expectedValue2.equals(metadatacard.getPropertyValue(dataPool.get("Property2"))) && metadatacard.getPropertyValue(dataPool.get("NameProperty")).equals("(automatic)"))
				Log.pass("Test Case Passed. The value of the name property is preserved as expected.");
			else
				Log.fail("Test Case Failed. The value of the name property was not the same as the copied object.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.1.16 : Name property while make copy of an object with automatic value for name property
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint58", "Metadatacard"}, 
			description = "Name property while make copy of an object with automatic value for name property")
	public void SprintTest58_1_16(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object
			//-------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType")+"s");

			Log.message("2. Search for the object");

			//3. Click the Object
			//--------------------
			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("The Specified object does not exist in the vault.");

			Log.message("3. Click the Object");

			//4. Click the Make Copy option from the Task Pane
			//-------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.MakeCopy.Value);
			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("4. Click the Make Copy option from the Task Pane");

			//Verification: To verify if the value of the name property is set as "(Automatic)"
			//----------------------------------------------------------------------------------
			if(metadatacard.getPropertyValue(dataPool.get("NameProperty")).equals("(automatic)"))
				Log.pass("Test Case Passed. The value of the name property is set as expected.");
			else
				Log.fail("Test Case Failed. The value of the name property was not as expected.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.1.17 : Calculation of automatic name property value in make copy operation
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint58", "Metadatacard"}, 
			description = "Calculation of automatic name property value in make copy operation")
	public void SprintTest58_1_17(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object
			//-------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType")+"s");

			Log.message("2. Search for the object");

			//3. Click the Object
			//--------------------
			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("The Specified object does not exist in the vault.");

			Log.message("3. Click the Object");

			//4. Click the Make Copy option from the Task Pane
			//-------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.MakeCopy.Value);
			Utils.fluentWait(driver);
			Thread.sleep(5000);
			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard.setInfo(dataPool.get("Property"));
			metadatacard.setCheckInImmediately(true);
			metadatacard.saveAndClose();

			Log.message("4. Click the Make Copy option from the Task Pane");

			//5. Search for the copied object
			//--------------------------------
			homePage.searchPanel.search(dataPool.get("Value"), "Search only: " + dataPool.get("ObjectType")+"s");

			Log.message("5. Search for the copied object");

			//Verification: To verify if the automatic calculation works for Make Copy operation
			//-----------------------------------------------------------------------------------
			if(homePage.listView.isItemExists(dataPool.get("Value")))
				Log.pass("Test Case Passed. Automatic value was calculated as expected for the Name property.");
			else
				Log.fail("Test Case Failed. The object with the expected automatic value name was not created.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.1.18 : Automatic values for Properties in make copy operation
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint58", "Metadatacard"}, 
			description = "Automatic values for Properties in make copy operation")
	public void SprintTest58_1_18(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object
			//-------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType")+"s");
			Utils.fluentWait(driver);

			Log.message("2. Search for the object");

			//3. Click the Object
			//--------------------
			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("The Specified object does not exist in the vault. Please execute case 58.1.17.");

			Log.message("3. Click the Object");

			//4. Click the Make Copy option from the Task Pane
			//-------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.MakeCopy.Value);
			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard.setInfo(dataPool.get("Properties"));
			metadatacard.setCheckInImmediately(true);
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("4. Click the Make Copy option from the Task Pane");

			//5. Search for the copied object
			//--------------------------------
			homePage.searchPanel.search(dataPool.get("Value"), "");
			Utils.fluentWait(driver);

			if(!homePage.listView.clickItem(dataPool.get("Value")))
				throw new Exception("The copy of the object was not created.");

			Log.message("5. Search for the copied object");

			//Verification: To verify if the automatic value is calculated for property after make copy
			//------------------------------------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true);

			if(metadatacard.getPropertyValue(dataPool.get("Property")).equals(dataPool.get("ExpectedValue")))
				Log.pass("Test Case Passed. Automatic value was calculated as expected for the property.");
			else
				Log.fail("Test Case Failed.  Automatic value was calculated as expected for the property.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.1.19 : Make Copy of a template should ignore the Is template property
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint58", "Metadatacard"}, 
			description = "Make Copy of a template should ignore the Is template property")
	public void SprintTest58_1_19(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object
			//-------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType")+"s");

			Log.message("2. Search for the object");

			//3. Click the Object
			//--------------------
			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("The Specified object does not exist in the vault. Please execute case 58.1.17.");

			Log.message("3. Click the Object");

			//4. Click the Make Copy option from the Task Pane
			//-------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.MakeCopy.Value);
			Utils.fluentWait(driver);

			//Verification: To verify if the Is template property is ignored
			//---------------------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);

			if(!metadatacard.propertyExists(dataPool.get("Property")))
				Log.pass("Test Case Passed. 'Is template' property was ignored as expected.");
			else
				Log.fail("Test Case Failed.  'Is template' property was not ignored in Make Copy.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.1.20 : Make copy of an object should inherit the permission of the Object copied
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint58", "Metadatacard"}, 
			description = "Make copy of an object should inherit the permission of the Object copied")
	public void SprintTest58_1_20(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object
			//-------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType")+"s");
			Utils.fluentWait(driver);

			Log.message("2. Search for the object");

			//3. Click the Object
			//--------------------
			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("The Specified object does not exist in the vault.");

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver, true);
			metadatacard.setPermission(dataPool.get("Permission"));
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("3. Click the Object");

			//4. Click the Make Copy option from the Task Pane
			//-------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.MakeCopy.Value);
			Utils.fluentWait(driver);

			Log.message("4. Click the Make Copy option from the Task Pane");

			//5. Set the name and click create button
			//----------------------------------------
			metadatacard = new MetadataCard(driver);

			metadatacard.setPropertyValue(dataPool.get("NameProperty"), dataPool.get("Value"));
			metadatacard.setCheckInImmediately(true);
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("5. Set the name and click create button");

			//Verification: To verify if the permission is not copied to the object
			//----------------------------------------------------------------------
			homePage.listView.clickItemByIndex(homePage.listView.itemCount()-1);
			Utils.fluentWait(driver);

			metadatacard = new MetadataCard(driver, true);

			if(metadatacard.getPermission().equals(dataPool.get("Permission")))
				Log.pass("Test Case Passed. Permission was inherited from the Copied object.");
			else
				Log.fail("Test Case Failed.  Permission was not inherited from the Copied object.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}



	/**
	 * 58.1.27 : Make Copy a newly created Object (Check in immediately)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Make Copy a newly created Object (Check in immediately).")
	public void SprintTest58_1_27(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Perform the menu click to open the new object metadatacard
			//---------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));
			Utils.fluentWait(driver);
			String objectName = dataPool.get("Object");
			Utils.fluentWait(driver);
			if(Utility.selectTemplate(dataPool.get("Extension"), driver)) 
				objectName = objectName + "." + dataPool.get("Extension");

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Perform the menu click to open the new object metadatacard.");

			//3. Set the value for the required properties
			//--------------------------------------------
			metadatacard.setInfo(dataPool.get("Properties") + dataPool.get("Object"));
			Utils.fluentWait(driver);

			metadatacard.setCheckInImmediately(true);
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("3. Set the value for the required properties");

			//4. Search for the object and Make copy
			//---------------------------------------
			homePage.searchPanel.search(objectName, "Search only: "+dataPool.get("ObjectType")+"s");
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			if(!homePage.listView.clickItem(objectName))
				throw new Exception("The specified object '" + objectName + "' was not created.");

			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.MakeCopy.Value);
			Utils.fluentWait(driver);
			metadatacard = new MetadataCard(driver);
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("ObjectName"));
			metadatacard.setCheckInImmediately(true);
			objectName = dataPool.get("ObjectName");
			if(!dataPool.get("Extension").equals(""))
				objectName = objectName + "." + dataPool.get("Extension");
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("4. Search for the object and open it's metadatacard");

			homePage.searchPanel.search(objectName, "Search only: "+dataPool.get("ObjectType")+"s");
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			if(!homePage.listView.isItemExists(objectName))
				throw new Exception("The specified object '" + objectName + "' was not created.");

			Log.message("5. Search for the object and Make copy");

			//Verification: To verify if the object is copied and checked in
			//---------------------------------------------------------------
			if(!ListView.isCheckedOutByItemName(driver, objectName))
				Log.pass("Test Case Passed. The Object with the HTML tag name was checked out successfully.");
			else
				Log.fail("Test Case Failed. Checking out an object with HTML tag name was not successful.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.1.28 : Make Copy a newly created object (Uncheck - Check in immediatly)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Make Copy a newly created object (Uncheck - Check in immediatly).")
	public void SprintTest58_1_28(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Perform the menu click to open the new object metadatacard
			//---------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));
			Utils.fluentWait(driver);
			String objectName = dataPool.get("Object");
			Utils.fluentWait(driver);
			if(Utility.selectTemplate(dataPool.get("Extension"), driver)) 
				objectName = objectName + "." + dataPool.get("Extension");

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Perform the menu click to open the new object metadatacard.");

			//3. Set the value for the required properties
			//--------------------------------------------
			metadatacard.setInfo(dataPool.get("Properties") + dataPool.get("Object"));
			Utils.fluentWait(driver);

			metadatacard.setCheckInImmediately(true);
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("3. Set the value for the required properties");

			//4. Search for the object and Make copy
			//---------------------------------------
			homePage.searchPanel.search(objectName, "Search only: "+dataPool.get("ObjectType")+"s");
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			if(!homePage.listView.clickItem(objectName))
				throw new Exception("The specified object '" + objectName + "' was not created.");

			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.MakeCopy.Value);
			Utils.fluentWait(driver);
			metadatacard = new MetadataCard(driver);
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("ObjectName"));
			metadatacard.setCheckInImmediately(false);
			metadatacard.setOpenForEditing(false);
			objectName = dataPool.get("ObjectName");
			if(!dataPool.get("Extension").equals(""))
				objectName = objectName + "." + dataPool.get("Extension");
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("4. Search for the object and open it's metadatacard");

			homePage.searchPanel.search(objectName, "Search only: "+dataPool.get("ObjectType")+"s");
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			if(!homePage.listView.isItemExists(objectName))
				throw new Exception("The specified object '" + objectName + "' was not created.");

			Log.message("5. Search for the object and Make copy");

			//Verification: To verify if the object is copied and checked in
			//---------------------------------------------------------------
			if(ListView.isCheckedOutByItemName(driver, objectName))
				Log.pass("Test Case Passed. The Object was created and remained checked out.");
			else
				Log.fail("Test Case Failed. The Object was created but it was not checked out", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}


}

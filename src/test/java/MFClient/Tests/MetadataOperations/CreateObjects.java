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
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class CreateObjects {

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
			testVault2 = "MetadataOperations";

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
	 * 48.1.28 : Check In Immediately
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard", "Smoke"}, 
			description = "Check In Immediately.")
	public void SprintTest48_1_28(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Perfrom the Task pane click
			//------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));
			String object = dataPool.get("Object");

			Log.message("2. Perfrom the Task pane click");

			//3. Set the necessary info and create the object
			//------------------------------------------------
			if(Utility.selectTemplate(dataPool.get("Extension"), driver)) {
				object = object + "." + dataPool.get("Extension");
			}

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver);

			metadatacard.setInfo(dataPool.get("Properties")+dataPool.get("Object"));
			metadatacard.setCheckInImmediately(true);
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("3. Set the necessary info and create the object");

			//4. Search for the created object
			//---------------------------------
			homePage.searchPanel.search(object, "Search only: "+dataPool.get("ObjectType")+"s");
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(object))
				throw new Exception("The Object was not created.");

			Log.message("4. Search for the created object.");

			//Verification: To verify if the Object is checked in
			//----------------------------------------------------
			homePage.listView.insertColumn("Checkout Time");

			if(homePage.listView.getColumnValueByItemName(object, "Checkout Time").equals(""))
				Log.pass("Test Case Passed. The Object was created and it was checked in.");
			else
				Log.fail("Test Case Failed. The Object was created but it was not checked in.", driver);

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
	 * 48.1.29 : Un-Check Check In Immediately
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard", "Smoke"}, 
			description = "Un-Check Check In Immediately")
	public void SprintTest48_1_29(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Perfrom the Task pane click
			//------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));
			String object = dataPool.get("Object");

			Log.message("2. Perfrom the Task pane click");

			//3. Set the necessary info and create the object
			//------------------------------------------------
			if(Utility.selectTemplate(dataPool.get("Extension"), driver)) {
				object = object + "." + dataPool.get("Extension");
			}

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver);

			metadatacard.setInfo(dataPool.get("Properties")+dataPool.get("Object"));
			Utils.fluentWait(driver);
			metadatacard.setOpenForEditing(false);
			metadatacard.setCheckInImmediately(false);
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("3. Set the necessary info and create the object");

			//4. Search for the created object
			//---------------------------------
			Utils.fluentWait(driver);
			homePage.searchPanel.search(object, "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.isItemExists(object))
				throw new Exception("The Object was not created.");

			Log.message("4. Search for the created object.");

			//Verification: To verify if the Object is checked in
			//----------------------------------------------------
			homePage.listView.insertColumn("Checkout Time");

			if(!homePage.listView.getColumnValueByItemName(object, "Checkout Time").equals(""))
				Log.pass("Test Case Passed. The Object was created and it was not checked in.");
			else
				Log.fail("Test Case Failed. The Object was created but it was checked in.", driver);

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
	 * 48.1.30A : Check In Immediately
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Check In Immediately.")
	public void SprintTest48_1_30A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Perfrom the new menu click
			//------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));
			String object = dataPool.get("Object");

			Log.message("2. Perfrom the new menu click");

			//3. Set the necessary info and create the object
			//------------------------------------------------
			if(Utility.selectTemplate(dataPool.get("Extension"), driver)) {
				object = object + "." + dataPool.get("Extension");
			}

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver);

			metadatacard.setInfo(dataPool.get("Properties")+dataPool.get("Object"));
			metadatacard.setCheckInImmediately(true);
			metadatacard.saveAndClose();

			Log.message("3. Set the necessary info and create the object");

			//4. Search for the created object
			//---------------------------------
			if(dataPool.get("ObjectType").equals(Caption.ObjecTypes.DocumentCollection.Value))
				homePage.searchPanel.search(object, "Search only: "+Caption.ObjecTypes.Document.Value+"s");
			else
				homePage.searchPanel.search(object, "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.isItemExists(object))
				throw new Exception("The Object was not created.");

			Log.message("4. Search for the created object.");

			//Verification: To verify if the Object is checked in
			//----------------------------------------------------
			homePage.listView.insertColumn("Checkout Time");

			if(homePage.listView.getColumnValueByItemName(object, "Checkout Time").equals(""))
				Log.pass("Test Case Passed. The Object was created and it was checked in.");
			else
				Log.fail("Test Case Failed. The Object was created but it was not checked in.", driver);

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
	 * 48.1.30B : Un-Check Check In Immediately
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Un-Check Check In Immediately")
	public void SprintTest48_1_30B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Perform the new menu click
			//------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));
			String object = dataPool.get("Object");

			Log.message("2. Perform the new menu click");

			//3. Set the necessary info and create the object
			//------------------------------------------------
			if(Utility.selectTemplate(dataPool.get("Extension"), driver)) {
				object = object + "." + dataPool.get("Extension");
			}

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver);

			metadatacard.setInfo(dataPool.get("Properties")+dataPool.get("Object"));
			metadatacard.setOpenForEditing(false);
			metadatacard.setCheckInImmediately(false);
			metadatacard.saveAndClose();

			Log.message("3. Set the necessary info and create the object");

			//4. Search for the created object
			//---------------------------------
			if(dataPool.get("ObjectType").equals(Caption.ObjecTypes.DocumentCollection.Value))
				homePage.searchPanel.search(object, "Search only: "+Caption.ObjecTypes.Document.Value+"s");
			else
				homePage.searchPanel.search(object, "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.isItemExists(object))
				throw new Exception("The Object was not created.");

			Log.message("4. Search for the created object.");

			//Verification: To verify if the Object is checked in
			//----------------------------------------------------
			homePage.listView.insertColumn("Checkout Time");

			if(!homePage.listView.getColumnValueByItemName(object, "Checkout Time").equals(""))
				Log.pass("Test Case Passed. The Object was created and it was not checked in.");
			else
				Log.fail("Test Case Failed. The Object was created but it was checked in.", driver);

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
	 * 48.1.31A : Click on an object and create a new Assignment through Task Pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Click on an object and create a new Assignment through Task Pane.")
	public void SprintTest48_1_31A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for and click on an object
			//--------------------------------------
			homePage.searchPanel.search(dataPool.get("ObjectName"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new SkipException("Invalid Test data. The Object was not found in the vault.");

			Log.message("2. Search for and click on an object.");

			//3. Perform the Task pane click
			//-------------------------------
			homePage.taskPanel.clickItem(dataPool.get("NewType"));
			Utils.fluentWait(driver);

			if(MFilesDialog.exists(driver, "Confirm Autofill")) {
				MFilesDialog mFilesDialog = new MFilesDialog(driver, "Confirm Autofill");
				mFilesDialog.clickCancelButton();
			}

			Log.message("3. Perform the Task pane click.");

			//3. Set the necessary info and create the object
			//------------------------------------------------
			Utils.fluentWait(driver);

			if(MFilesDialog.exists(driver, "Confirm Autofill")) {
				MFilesDialog mFilesDialog = new MFilesDialog(driver, "Confirm Autofill");
				mFilesDialog.clickCancelButton();
			}

			MetadataCard metadatacard = new MetadataCard(driver);

			metadatacard.setInfo(dataPool.get("Properties")+dataPool.get("Object"));
			metadatacard.saveAndClose();

			Log.message("4. Set the necessary info and create the object");

			//4. Search for the created object
			//---------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("NewType")+"s");

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new Exception("The Object was not created.");

			Log.message("5. Search for the created object.");

			//Verification: To verify if the object is added to the new object
			//-----------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true);

			if(dataPool.get("ObjectName").startsWith(metadatacard.getPropertyValue(dataPool.get("ObjectType"))))
				Log.pass("Test Case Passed. The Object was added as property value to the new object.");
			else
				Log.fail("Test Case Failed. The Object was not added as property value to the new object.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.31B : Click on an object and create a new Assignment through New Menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Click on an object and create a new Assignment through New Menu.")
	public void SprintTest48_1_31B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for and click on an object
			//--------------------------------------
			homePage.searchPanel.search(dataPool.get("ObjectName"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new SkipException("Invalid Test data. The Object was not found in the vault.");

			Log.message("2. Search for and click on an object.");

			//3. Perform the New Menu click
			//-------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("NewType"));
			Utils.fluentWait(driver);

			if(MFilesDialog.exists(driver, "Confirm Autofill")) {
				MFilesDialog mFilesDialog = new MFilesDialog(driver, "Confirm Autofill");
				mFilesDialog.clickCancelButton();
			}

			Log.message("3. Perform the New Menu click.");

			//3. Set the necessary info and create the object
			//------------------------------------------------
			Utils.fluentWait(driver);

			if(MFilesDialog.exists(driver, "Confirm Autofill")) {
				MFilesDialog mFilesDialog = new MFilesDialog(driver, "Confirm Autofill");
				mFilesDialog.clickCancelButton();
			}

			MetadataCard metadatacard = new MetadataCard(driver);

			metadatacard.setInfo(dataPool.get("Properties")+dataPool.get("Object"));
			metadatacard.saveAndClose();

			Log.message("4. Set the necessary info and create the object");

			//4. Search for the created object
			//---------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("NewType")+"s");

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new Exception("The Object was not created.");

			Log.message("5. Search for the created object.");

			//Verification: To verify if the object is added to the new object
			//-----------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true);

			if(dataPool.get("ObjectName").startsWith(metadatacard.getPropertyValue(dataPool.get("ObjectType"))))
				Log.pass("Test Case Passed. The Object was added as property value to the new object.");
			else
				Log.fail("Test Case Failed. The Object was not added as property value to the new object.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.32 : Check Name or title Property when New->Assignment  is clicked after clicking on any object (Task Pane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Check Name or title Property when New->Assignment  is clicked after clicking on any object (Task Pane).")
	public void SprintTest48_1_32(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for and click on an object
			//--------------------------------------
			homePage.searchPanel.search(dataPool.get("ObjectName"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new SkipException("Invalid Test data. The Object was not found in the vault.");

			Log.message("2. Search for and click on an object.");

			//3. Perform the Task pane click
			//-------------------------------
			homePage.taskPanel.clickItem(dataPool.get("NewType"));
			Utils.fluentWait(driver);

			if(MFilesDialog.exists(driver, "Confirm Autofill")) {
				MFilesDialog mFilesDialog = new MFilesDialog(driver, "Confirm Autofill");
				mFilesDialog.clickCancelButton();
			}

			Log.message("3. Perform the Task pane click.");

			//Verification: To verify if the Name or title property is set with the expected value
			//-------------------------------------------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard.savePropValue("Name or title");
			Utils.fluentWait(driver);
			if(metadatacard.getPropertyValue("Name or title").equals(dataPool.get("NewType") + ": " + dataPool.get("ObjectName")))
				Log.pass("Test Case Passed. The Name or title property was set as expected.");
			else
				Log.fail("Test Case Failed. The Name or title property was not set as expected.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.33 : Check Name or title Property when New->Assignment  is clicked after clicking on any object (New Menu)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Check Name or title Property when New->Assignment  is clicked after clicking on any object (New Menu).")
	public void SprintTest48_1_33(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for and click on an object
			//--------------------------------------
			homePage.searchPanel.search(dataPool.get("ObjectName"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new SkipException("Invalid Test data. The Object was not found in the vault.");

			Log.message("2. Search for and click on an object.");

			//3. Perfrom the new menu click
			//-------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("NewType"));
			Utils.fluentWait(driver);

			if(MFilesDialog.exists(driver, "Confirm Autofill")) {
				MFilesDialog mFilesDialog = new MFilesDialog(driver, "Confirm Autofill");
				mFilesDialog.clickCancelButton();
			}

			Log.message("3. Perfrom the new menu click.");

			//Verification: To verify if the Name or title property is set with the expected value
			//-------------------------------------------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard.savePropValue("Name or title");
			Utils.fluentWait(driver);
			if(metadatacard.getPropertyValue("Name or title").equals(dataPool.get("NewType") + ": " + dataPool.get("ObjectName")))
				Log.pass("Test Case Passed. The Name or title property was set as expected.");
			else
				Log.fail("Test Case Failed. The Name or title property was not set as expected.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.34 : Check (Object) Property when New->Assignment  is clicked after clicking on any object (Task Pane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Check (Object) Property when New->Assignment  is clicked after clicking on any object (Task Pane).")
	public void SprintTest48_1_34(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for and click on an object
			//--------------------------------------
			homePage.searchPanel.search(dataPool.get("ObjectName"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new SkipException("Invalid Test data. The Object was not found in the vault.");

			Log.message("2. Search for and click on an object.");

			//3. Perform the Task pane click
			//-------------------------------
			homePage.taskPanel.clickItem(dataPool.get("NewType"));
			Utils.fluentWait(driver);

			if(MFilesDialog.exists(driver, "Confirm Autofill")) {
				MFilesDialog mFilesDialog = new MFilesDialog(driver, "Confirm Autofill");
				mFilesDialog.clickCancelButton();
			}

			Log.message("3. Perform the Task pane click.");

			//Verification: To verify if the Object property is set with the expected value
			//------------------------------------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);
			String value = dataPool.get("ObjectName");

			if(dataPool.get("ObjectName").contains("."))
				value = dataPool.get("ObjectName").split(".")[0];

			if(metadatacard.propertyExists(dataPool.get("ObjectType")) && metadatacard.getPropertyValue(dataPool.get("ObjectType")).equals(value))
				Log.pass("Test Case Passed. The Object property was set as expected.");
			else
				Log.fail("Test Case Failed. The Object property was not set as expected.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.35 : Check (Object) Property when New->Assignment  is clicked after clicking on any object (New Menu)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Check (Object) Property when New->Assignment  is clicked after clicking on any object (New Menu).")
	public void SprintTest48_1_35(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for and click on an object
			//--------------------------------------
			homePage.searchPanel.search(dataPool.get("ObjectName"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new SkipException("Invalid Test data. The Object was not found in the vault.");

			Log.message("2. Search for and click on an object.");

			//3. Perfrom the new menu click
			//-------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("NewType"));
			Utils.fluentWait(driver);

			if(MFilesDialog.exists(driver, "Confirm Autofill")) {
				MFilesDialog mFilesDialog = new MFilesDialog(driver, "Confirm Autofill");
				mFilesDialog.clickCancelButton();
			}

			Log.message("3. Perfrom the new menu click.");

			//Verification: To verify if the Object property is set with the expected value
			//------------------------------------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);

			String value = dataPool.get("ObjectName");

			if(dataPool.get("ObjectName").contains("."))
				value = dataPool.get("ObjectName").split(".")[0];

			if(metadatacard.propertyExists(dataPool.get("ObjectType")) && metadatacard.getPropertyValue(dataPool.get("ObjectType")).equals(value))
				Log.pass("Test Case Passed. The Object property was set as expected.");
			else
				Log.fail("Test Case Failed. The Object property was not set as expected.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.36A : Check (Object) Property when Create New Value is clicked for Assignment Property from any object's metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Check (Object) Property when Create New Value is clicked for Assignment Property from any object's metadatacard.")
	public void SprintTest48_1_36A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for and open the metadatacard of an object
			//-----------------------------------------------------
			homePage.searchPanel.search(dataPool.get("ObjectName"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new SkipException("Invalid Test data. The Object was not found in the vault.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Search for and open the metadatacard of an object.");

			//3. Add the Assignment property and click the Create Value button
			//-----------------------------------------------------------------
			metadatacard.createNewPropertyValue(dataPool.get("NewType"), 1);
			Utils.fluentWait(driver);

			if(MFilesDialog.exists(driver, "Confirm Autofill")) {
				MFilesDialog mFilesDialog = new MFilesDialog(driver, "Confirm Autofill");
				mFilesDialog.clickCancelButton();
			}

			Log.message("3. Add the Assignment property and click the Create Value button");

			//4. Check the metadatacard
			//--------------------------
			metadatacard = new MetadataCard(driver, "New " + dataPool.get("NewType"));

			Log.message("4. Check the metadatacard");

			//Verification: To verify if the object property is not added to the new object
			//------------------------------------------------------------------------------
			if(!metadatacard.propertyExists(dataPool.get("ObjectType")))
				Log.pass("Test Case Passed. The Object property was not added as expected.");
			else
				Log.fail("Test Case Failed. The Object property was set to the metadatacard", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.36B : Check (Object) Property when Create New Value is clicked for Assignment Property from any object's metadatacard (Side Pane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Check (Object) Property when Create New Value is clicked for Assignment Property from any object's metadatacard (Side Pane).")
	public void SprintTest48_1_36B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for and open the metadatacard of an object
			//-----------------------------------------------------
			homePage.searchPanel.search(dataPool.get("ObjectName"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new SkipException("Invalid Test data. The Object was not found in the vault.");

			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("2. Search for and open the metadatacard of an object.");

			//3. Add the Assignment property and click the Create Value button
			//-----------------------------------------------------------------
			metadatacard.createNewPropertyValue(dataPool.get("NewType"), 1);
			Utils.fluentWait(driver);

			if(MFilesDialog.exists(driver, "Confirm Autofill")) {
				MFilesDialog mFilesDialog = new MFilesDialog(driver, "Confirm Autofill");
				mFilesDialog.clickCancelButton();
			}

			Log.message("3. Add the Assignment property and click the Create Value button");

			//4. Check the metadatacard
			//--------------------------
			metadatacard = new MetadataCard(driver, "New " + dataPool.get("NewType"));

			Log.message("4. Check the metadatacard");

			//Verification: To verify if the object is added to the new object
			//-----------------------------------------------------------------
			if(!metadatacard.propertyExists(dataPool.get("ObjectType")))
				Log.pass("Test Case Passed. The Object property was not added as expected.");
			else
				Log.fail("Test Case Failed. The Object property was set to the metadatacard", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.36C : Check (Object) Property when Create New Value is clicked for Assignment Property from any object's metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Check (Object) Property when Create New Value is clicked for Assignment Property from any object's metadatacard.")
	public void SprintTest48_1_36C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for and open the metadatacard of an object
			//-----------------------------------------------------
			homePage.searchPanel.search(dataPool.get("ObjectName"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new SkipException("Invalid Test data. The Object was not found in the vault.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Search for and open the metadatacard of an object.");

			//3. Add the Assignment property and click the Create Value button
			//-----------------------------------------------------------------
			metadatacard.createNewPropertyValue(dataPool.get("NewType"), 1);
			Utils.fluentWait(driver);

			if(MFilesDialog.exists(driver, "Confirm Autofill")) {
				MFilesDialog mFilesDialog = new MFilesDialog(driver, "Confirm Autofill");
				mFilesDialog.clickCancelButton();
			}

			Log.message("3. Add the Assignment property and click the Create Value button");

			//4. Check the metadatacard
			//--------------------------
			metadatacard = new MetadataCard(driver, "New " + dataPool.get("NewType"));

			Log.message("4. Check the metadatacard");

			//Verification: To verify if the object is added to the new object
			//-----------------------------------------------------------------
			if(!metadatacard.propertyExists(dataPool.get("Property")))
				Log.pass("Test Case Passed. The Object property was not added as expected.");
			else
				Log.fail("Test Case Failed. The Object property was set to the metadatacard", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.36D : Check (Object) Property when Create New Value is clicked for Assignment Property from any object's metadatacard (Side Pane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Check (Object) Property when Create New Value is clicked for Assignment Property from any object's metadatacard (Side Pane).")
	public void SprintTest48_1_36D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for and open the metadatacard of an object
			//-----------------------------------------------------
			homePage.searchPanel.search(dataPool.get("ObjectName"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new SkipException("Invalid Test data. The Object was not found in the vault.");

			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("2. Search for and open the metadatacard of an object.");

			//3. Add the Assignment property and click the Create Value button
			//-----------------------------------------------------------------
			metadatacard.createNewPropertyValue(dataPool.get("NewType"), 1);
			Utils.fluentWait(driver);

			if(MFilesDialog.exists(driver, "Confirm Autofill")) {
				MFilesDialog mFilesDialog = new MFilesDialog(driver, "Confirm Autofill");
				mFilesDialog.clickCancelButton();
			}

			Log.message("3. Add the Assignment property and click the Create Value button");

			//4. Check the metadatacard
			//--------------------------
			metadatacard = new MetadataCard(driver, "New " + dataPool.get("NewType"));

			Log.message("4. Check the metadatacard");

			//Verification: To verify if the object is added to the new object
			//-----------------------------------------------------------------
			if(!metadatacard.propertyExists(dataPool.get("Property")))
				Log.pass("Test Case Passed. The Object property was not added as expected.");
			else
				Log.fail("Test Case Failed. The Object property was set to the metadatacard", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}


	/**
	 * SprintTest_38284 : Verify if object is created with the specified template.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Verify if object is created with the specified template.")
	public void SprintTest_38284(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1 :  Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);//Logged in with valid credentials

			Log.message("1. Logged into the Home View.");

			//Step-2 : Select the new document object type
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Document.Value);//Create the new document object type in task pane

			Log.message("2. Created the new document object from the task panel.", driver);

			//Step-3 : Select the template as Contract or Agreement 
			//-----------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the metadatacard
			metadatacard.setTemplate(dataPool.get("Template"));//Set the template in metadatacard

			Log.message("3. Set the specified template : " + dataPool.get("Template")+ " in metadatacard.", driver);

			//Step-4 : Set the all required property in metadatacard
			//------------------------------------------------------
			metadatacard = new MetadataCard(driver);
			metadatacard.setInfo(dataPool.get("Props"));//Set the Property for the metadatacard
			metadatacard.setCheckInImmediately(true);//Set the check in immediately check box
			metadatacard.saveAndClose();//Save the metadatacard

			Log.message("4. All the mandatory properties are filled and saved the metadatacard.", driver);

			//Step-5 : Navigate to specified view and select the specified object
			//-------------------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("objectName"));

			Log.message("5. Navigated to : " + viewToNavigate + " specified view.", driver);

			//Verification : Verify if itemexists or not
			//------------------------------------------
			if(homePage.listView.isItemExists(dataPool.get("objectName"))&& !MFilesDialog.exists(driver))
				Log.pass("Test Case Passed.Document object : "+ dataPool.get("objectName") + " successfully created.");
			else
				Log.fail("Test Case Failed.Document object does not created successfully.", driver);

		}//End try

		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally{
			Utility.quitDriver(driver);
		}//End finally
	}//End SprintTest_38284


}//Create objects
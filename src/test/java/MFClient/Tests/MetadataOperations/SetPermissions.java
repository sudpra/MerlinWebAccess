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
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class SetPermissions {

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
	 * 58.1.21 : Change the permission of an object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint58", "Metadatacard", "Smoke"}, 
			description = "Change the permission of an object")
	public void SprintTest58_1_21(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//3. Change the permission of an object
			//--------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("The Specified object does not exist in the vault.");
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard.setPermission(dataPool.get("Permission"));
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);
			Utility.logoutFromWebAccess(driver);

			Log.message("3. Change the permission of an object");

			//4. Login as the restricted user
			//--------------------------------
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String vault = xmlParameters.getParameter("VaultName");
			driver.get(loginURL); //Launches with the URL
			Utils.fluentWait(driver);

			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX")) //Checks if Login.aspx page is loaded
				throw new Exception ("Browser is not navigated to the Login page.");

			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), vault); //Logs into application
			Utils.fluentWait(driver);

			Log.message("4. Login as the restricted user.");

			//5. Search for the object
			//-------------------------
			Utils.fluentWait(driver);
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType")+"s");
			Utils.fluentWait(driver);

			Log.message("5. Search for the object.");

			//Verification: To verify if the Object is not displayed for the restricted user
			//-------------------------------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				Log.pass("Test Case Passed. The Object was hidden from the user as expected.");
			else
				Log.fail("Test Case Failed. The Object was not hidden from the user as expected.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.1.22 : Change the permission of an object (SidePane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint58", "Metadatacard"}, 
			description = "Change the permission of an object (SidePane)")
	public void SprintTest58_1_22(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//3. Change the permission of an object
			//--------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("The Specified object does not exist in the vault.");

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver, true);
			metadatacard.setPermission(dataPool.get("Permission"));
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);
			Utility.logoutFromWebAccess(driver);

			Log.message("3. Change the permission of an object");

			//4. Login as the restricted user
			//--------------------------------
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String vault = xmlParameters.getParameter("VaultName");
			driver.get(loginURL); //Launches with the URL
			Utils.fluentWait(driver);

			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX")) //Checks if Login.aspx page is loaded
				throw new Exception ("Browser is not navigated to the Login page.");

			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), vault); //Logs into application
			Utils.fluentWait(driver);

			Log.message("4. Login as the restricted user.");

			//5. Search for the object
			//-------------------------
			Utils.fluentWait(driver);
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType")+"s");
			Utils.fluentWait(driver);

			Log.message("5. Search for the object.");

			//Verification: To verify if the Object is not displayed for the restricted user
			//-------------------------------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				Log.pass("Test Case Passed. The Object was hidden from the user as expected.");
			else
				Log.fail("Test Case Failed. The Object was not hidden from the user as expected.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.1.23 : Create a new object by changing the default object permission
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard", "Smoke"}, 
			description = "Create a new object by changing the default object permission.")
	public void SprintTest58_1_23(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			if(!dataPool.get("Extension").equals("")) {
				Utility.selectTemplate(dataPool.get("Extension"), driver);
				objectName = objectName + "." + dataPool.get("Extension");
			}

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Perform the menu click to open the new object metadatacard.");

			//3. Set the value for the required properties
			//--------------------------------------------
			metadatacard.setInfo(dataPool.get("Properties")+dataPool.get("Object"));
			Utils.fluentWait(driver);
			metadatacard.setCheckInImmediately(true);
			metadatacard.setPermission(dataPool.get("Permission"));
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("3. Set the value for the required properties");

			//5. Search for the object and open it's metadatacard
			//-----------------------------------------------------
			homePage.searchPanel.search(objectName, "Search only: "+dataPool.get("ObjectType")+"s");
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			if(!homePage.listView.isItemExists(objectName))
				throw new Exception("The specified object '" + objectName + "' was not created.");

			homePage.listView.clickItem(objectName);
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			metadatacard = new MetadataCard(driver, true);
			Log.message("4. Search for the object and open it's metadatacard");

			//Verify if all the Expected properties appear in the Add Property Combo box
			//----------------------------------------------------------------------------
			if(metadatacard.getPermission().equals(dataPool.get("Permission")))
				Log.pass("Test Case Passed. Creattion of new Object without default permission was successful.");
			else
				Log.fail("Test Case Failed. The Object was not created, but the permission was not set.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.1.24 : Check Out Change Permission and Undo Check out
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint58", "Metadatacard", "Smoke"}, 
			description = "Check Out Change Permission and Undo Check out")
	public void SprintTest58_1_24(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//3. Check out the object
			//------------------------
			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("The Specified object does not exist in the vault.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);

			Log.message("3. Check out the object");

			//4. Change the permission of an object
			//--------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver);
			String expectedPermission = metadatacard.getPermission();
			metadatacard.setPermission(dataPool.get("Permission"));
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("4. Change the permission of an object");

			//5. Perfrom Undo-checkout
			//-------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);
			Utils.fluentWait(driver);

			MFilesDialog mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.confirmUndoCheckOut(true);
			Utils.fluentWait(driver);

			Log.message("5. Perfrom Undo-checkout");

			//6. Open the metadatacard of the object
			//---------------------------------------
			metadatacard = new MetadataCard(driver, true);

			Log.message("6. Open the metadatacard of the object");

			//Verification: To verify if the Permission change is reverted
			//-------------------------------------------------------------
			if(metadatacard.getPermission().equals(expectedPermission))
				Log.pass("Test Case Passed. Permission change was reverted upon Undo-checkout.");
			else
				Log.fail("Test Case Failed. Permission change was not reverted upon Undo-checkout.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.1.25 : Check Out Change Permission and Undo Check out (Side pane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint58", "Metadatacard", "Smoke"}, 
			description = "Check Out Change Permission and Undo Check out (Side pane)")
	public void SprintTest58_1_25(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//3. Check out the object
			//------------------------
			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("The Specified object does not exist in the vault.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);

			Log.message("3. Check out the object");

			//4. Change the permission of an object
			//--------------------------------------
			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver, true);
			String expectedPermission = metadatacard.getPermission();
			metadatacard.setPermission(dataPool.get("Permission"));
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("4. Change the permission of an object");

			//5. Perfrom Undo-checkout
			//-------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);
			Utils.fluentWait(driver);

			MFilesDialog mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.confirmUndoCheckOut(true);
			Utils.fluentWait(driver);

			Log.message("5. Perfrom Undo-checkout");

			//6. Open the metadatacard of the object
			//---------------------------------------
			metadatacard = new MetadataCard(driver, true);

			Log.message("6. Open the metadatacard of the object");

			//Verification: To verify if the Permission change is reverted
			//-------------------------------------------------------------
			if(metadatacard.getPermission().equals(expectedPermission))
				Log.pass("Test Case Passed. Permission change was reverted upon Undo-checkout.");
			else
				Log.fail("Test Case Failed. Permission change was not reverted upon Undo-checkout.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.1.26 : Change the permission of a Template and create an object from it
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Change the permission of a Template and create an object from it.")
	public void SprintTest58_1_26(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object
			//-------------------------
			homePage.searchPanel.search(dataPool.get("Template"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.clickItem(dataPool.get("Template")))
				throw new Exception("The Object does not exist in the vault.");

			Log.message("2. Search for an object");

			//3. Convert the object into a template
			//-------------------------------------
			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver, true);
			String defaultPermission = metadatacard.getPermission();
			metadatacard.setPermission(dataPool.get("Permission"));
			metadatacard.saveAndClose();

			Log.message("3. Convert the object into a template.");

			//Verification: To Verify if the Select template dialog appears
			//--------------------------------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			Utility.selectTemplate(dataPool.get("Template"), driver);
			Utils.fluentWait(driver);

			metadatacard = new MetadataCard(driver);

			if(metadatacard.getPermission().equals(defaultPermission))
				Log.pass("Test Case Passed. Permission was not inherited from the Template.");
			else
				Log.fail("Test Case Failed. Permission was inherited from the template.", driver);

			Utils.fluentWait(driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}


	/**
	 * 58.2.24A : Changing the permission of an object should not change it's version
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Changing the permission of an object should not change it's version.")
	public void SprintTest58_2_24A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object and open it's metadatacard
			//---------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);
			MetadataCard metadataCard = new MetadataCard(driver);

			Log.message("2. Search for an object and open it's metadatacard");

			//3. Change the permission of the object
			//---------------------------------------
			int expectedVersion = metadataCard.getVersion();
			metadataCard.setPermission(dataPool.get("Permission"));
			metadataCard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("3. Change the permission of the object.");

			//4. Open the Metadatacard of the object
			//--------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);
			metadataCard = new MetadataCard(driver);

			if(!metadataCard.getPermission().equals(dataPool.get("Permission")))
				throw new Exception("Permission of the object was not changed.");

			Log.message("4. Open the Metadatacard of the object.");

			//Verification: To verify if the Comment count is shown correct
			//--------------------------------------------------------------
			if(metadataCard.getVersion() == expectedVersion) 
				Log.pass("Test Case Passed. Changing the permission did not change the version of the object.");
			else 
				Log.fail("Test Case Failed. Changing the permission of the object changed it's version.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.24B : Changing the permission of an object in the right pane should not changes it's permission
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Changing the permission of an object in the right pane should not changes it's permission.")
	public void SprintTest58_2_24B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object
			//------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");

			Utils.fluentWait(driver);
			MetadataCard metadataCard = new MetadataCard(driver, true);

			Log.message("2. Search for an object.");

			//3. Change the permission of the object
			//---------------------------------------
			int expectedVersion = metadataCard.getVersion();
			metadataCard.setPermission(dataPool.get("Permission"));
			metadataCard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("3. Change the permission of the object.");

			//4. Open the Metadatacard of the object
			//--------------------------------------
			metadataCard = new MetadataCard(driver, true);

			if(!metadataCard.getPermission().equals(dataPool.get("Permission")))
				throw new Exception("Permission of the object was not changed.");

			Log.message("4. Open the Metadatacard of the object.");

			//Verification: To verify if the Comment count is shown correct
			//--------------------------------------------------------------
			if(metadataCard.getVersion() == expectedVersion) 
				Log.pass("Test Case Passed. Changing the permission did not change the version of the object.");
			else 
				Log.fail("Test Case Failed. Changing the permission of the object changed it's version.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.25A : Changing the permission of an checked out object and checking in should increase it's version
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Changing the permission of an checked out object and checking in should increase it's version.")
	public void SprintTest58_2_25A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object and check out the object
			//---------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);

			Log.message("2. Search for an object and check out the object");

			//3. Open it's metadatacard
			//--------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);
			MetadataCard metadataCard = new MetadataCard(driver);

			Log.message("3. Open it's metadatacard.");

			//4. Change the permission of the object
			//---------------------------------------
			int expectedVersion = metadataCard.getVersion();
			metadataCard.setPermission(dataPool.get("Permission"));
			metadataCard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("4. Change the permission of the object.");

			//5. Check in the object
			//-----------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);
			Utils.fluentWait(driver);

			Log.message("5. Check in the object");

			//6. Open the Metadatacard of the object
			//--------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);
			metadataCard = new MetadataCard(driver);

			if(!metadataCard.getPermission().equals(dataPool.get("Permission")))
				throw new Exception("Permission of the object was not changed.");

			Log.message("6. Open the Metadatacard of the object.");

			//Verification: To verify if the Comment count is shown correct
			//--------------------------------------------------------------
			if(metadataCard.getVersion() == expectedVersion) 
				Log.pass("Test Case Passed. Changing the permission did not change the version of the object.");
			else 
				Log.fail("Test Case Failed. Changing the permission of the object changed it's version.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.25B : Changing the permission of an object in the right pane should not changes it's permission
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Changing the permission of an object in the right pane should not changes it's permission.")
	public void SprintTest58_2_25B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object and check out
			//--------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			MetadataCard metadataCard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and check out.");

			//3. Change the permission of the object
			//---------------------------------------
			int expectedVersion = metadataCard.getVersion();
			metadataCard.setPermission(dataPool.get("Permission"));
			metadataCard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("3. Change the permission of the object.");

			//4. Check in the object
			//-----------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);
			Utils.fluentWait(driver);

			Log.message("4. Check in the object");

			//4. Open the Metadatacard of the object
			//--------------------------------------
			metadataCard = new MetadataCard(driver, true);

			if(!metadataCard.getPermission().equals(dataPool.get("Permission")))
				throw new Exception("Permission of the object was not changed.");

			Log.message("5. Open the Metadatacard of the object.");

			//Verification: To verify if the Comment count is shown correct
			//--------------------------------------------------------------
			if(metadataCard.getVersion() == expectedVersion) 
				Log.pass("Test Case Passed. Changing the permission did not change the version of the object.");
			else 
				Log.fail("Test Case Failed. Changing the permission of the object changed it's version.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}


}

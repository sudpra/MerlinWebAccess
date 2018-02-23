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
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class MetadataCardModes {

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
	 * 44.5.3A : Editing the contents of the metadatacard changes the color of the bar
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Editing the contents of the metadatacard changes the color of the bar.")
	public void SprintTest44_5_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Search for an object and open it's metadatacard.");

			//3. Set the value for the required properties
			//--------------------------------------------
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));
			Utils.fluentWait(driver);

			Log.message("3. Set the value for the required properties");

			//Verify if the metadatacard is in edit mode
			//-------------------------------------------
			if(metadatacard.isEditMode())
				Log.pass("Test Case Passed. The metadatacard is in edit mode.");
			else
				Log.fail("Test Case Failed. Metadatacard is not in edit mode when a value is set to a property.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 44.5.3B : Editing the contents of the metadatacard changes the color of the bar (Side Pane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Editing the contents of the metadatacard changes the color of the bar (Side Pane).")
	public void SprintTest44_5_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and open it's metadatacard.");

			//3. Set the value for the required properties
			//--------------------------------------------
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));
			Utils.fluentWait(driver);

			Log.message("3. Set the value for the required properties");

			//Verify if the metadatacard is in edit mode
			//-------------------------------------------
			if(metadatacard.isEditMode())
				Log.pass("Test Case Passed. The metadatacard is in edit mode.");
			else
				Log.fail("Test Case Failed. Metadatacard is not in edit mode when a value is set to a property.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 44.5.4A : Clicking a property field and leaving clicking the Tick button without changing it's values should change the colour of the bar to blue
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Clicking a property field and leaving clicking the Tick button without changing it's values should change the colour of the bar to blue.")
	public void SprintTest44_5_4A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Search for an object and open it's metadatacard.");

			//3. Click the property field and click somewhere else
			//---------------------------------------------------------
			metadatacard.clickPropertyField(dataPool.get("Property"));
			Utils.fluentWait(driver);
			metadatacard.clickProperty(dataPool.get("Property"));
			Utils.fluentWait(driver);

			Log.message("3. Click the property field and click somewhere else");

			//Verify if the metadatacard is in read mode
			//-------------------------------------------
			if(!metadatacard.isEditMode())
				Log.pass("Test Case Passed. The metadatacard is in edit mode.");
			else
				Log.fail("Test Case Failed. Metadatacard is not in read mode when value of the property is not changed", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 44.5.4B : Clicking a property field and leaving clicking the Tick button without changing it's values should change the colour of the bar to blue (Side Pane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Clicking a property field and leaving clicking the Tick button without changing it's values should change the colour of the bar to blue (Side Pane).")
	public void SprintTest44_5_4B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and open it's metadatacard.");

			//3. Click the property field and click somewhere else
			//----------------------------------------------------
			metadatacard.clickPropertyField(dataPool.get("Property"));
			Utils.fluentWait(driver);
			metadatacard.clickProperty(dataPool.get("Property"));
			Utils.fluentWait(driver);

			Log.message("3. Click the property field and click somewhere else");

			//Verify if the metadatacard is in read mode
			//-------------------------------------------
			if(!metadatacard.isEditMode())
				Log.pass("Test Case Passed. The metadatacard is in read mode.");
			else
				Log.fail("Test Case Failed. Metadatacard is not in read mode when value of the property is not changed.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 44.5.5A : Changing the Comments of an object changes the metadatacard to edit mode
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Changing the Comments of an object changes the metadatacard to edit mode.")
	public void SprintTest44_5_5A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Search for an object and open it's metadatacard.");

			//3. Set the value for the required properties
			//--------------------------------------------
			metadatacard.setComments(dataPool.get("Value"));
			Utils.fluentWait(driver);

			Log.message("3. Set the value for the required properties");

			//Verify if the metadatacard is in edit mode
			//-------------------------------------------
			if(metadatacard.isEditMode())
				Log.pass("Test Case Passed. The metadatacard is in edit mode.");
			else
				Log.fail("Test Case Failed. Metadatacard is not in edit mode when a value is set to a property.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 44.5.5B : Changing the Comments of an object changes the metadatacard to edit mode (Side Pane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Changing the Comments of an object changes the metadatacard to edit mode (Side Pane).")
	public void SprintTest44_5_5B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and open it's metadatacard.");

			//3. Set the value for the required properties
			//--------------------------------------------
			metadatacard.setComments(dataPool.get("Value"));
			Utils.fluentWait(driver);

			Log.message("3. Set the value for the required properties");

			//Verify if the metadatacard is in edit mode
			//-------------------------------------------
			if(metadatacard.isEditMode())
				Log.pass("Test Case Passed. The metadatacard is in edit mode.");
			else
				Log.fail("Test Case Failed. Metadatacard is not in edit mode when a value is set to a property.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}
	/**
	 * 58.1.29 : Clicking the Check In Immediately check box should un-check the Open for editing Check box while creating a document
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard", "Smoke"}, 
			description = "Clicking the Check In Immediately check box should un-check the Open for editing Check box while creating a document.")
	public void SprintTest58_1_29(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//3. Check the Check In immediatly check box
			//------------------------------------------
			metadatacard.setCheckInImmediately(true);
			Utils.fluentWait(driver);
			Log.message("3. Check the Check In immediatly check box.");

			//Verification: To verify if the Open for editing check box is unchecked
			//----------------------------------------------------------------------
			if(!metadatacard.isOpenForEditing())
				Log.pass("Test Case Passed. Open for editing checkbox is unchecked as expected.");
			else
				Log.fail("Test Case Failed. Open for editing checkbox is remains checked even after checking the Check In immediatly check box.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.1.30 : Clicking the Open for editing check box should un-check the Check in immediately Check box while creating a document
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Clicking the Open for editing check box should un-check the Check in immediately Check box while creating a document.")
	public void SprintTest58_1_30(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//3. Check the Check In immediatly check box
			//------------------------------------------
			metadatacard.setCheckInImmediately(true);
			Utils.fluentWait(driver);

			Log.message("3. Check the Check In immediatly check box.");

			//4. Check theOpen for editing check box
			//------------------------------------------
			metadatacard.setOpenForEditing(true);
			Utils.fluentWait(driver);

			Log.message("4. Check theOpen for editing check box.");

			//Verification: To verify if the Check In immeditaly check box is unchecked
			//----------------------------------------------------------------------
			if(!metadatacard.isCheckInImmediately())
				Log.pass("Test Case Passed. Check In immeditaly checkbox is unchecked as expected.");
			else
				Log.fail("Test Case Failed. Check In immeditaly checkbox is remains checked even after checking the Open for editing check box.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}
	/**
	 * 58.2.18A : Collapse Title in Metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Collapse Title in Metadatacard")
	public void SprintTest58_2_18A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);
			MetadataCard metadataCard = new MetadataCard(driver);

			Log.message("2. Search for an object and open it's metadatacard");

			//3. Collapse the header
			//-----------------------
			metadataCard.collapseHeader(true);
			Utils.fluentWait(driver);

			Log.message("3. Collapse the header");

			//Verification: To verify if the header is collapsed
			//---------------------------------------------------
			if(metadataCard.isHeaderCollapsed()) {
				Log.pass("Test Case Passed. The header was in collapsed state.");
				metadataCard.collapseHeader(false);
			}
			else
				Log.fail("Test Case Failed. The header was not it collapsed state.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.18B : Collapse Title in Metadatacard (Sidepane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Collapse Title in Metadatacard (Sidepane)")
	public void SprintTest58_2_18B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object and click on it
			//-----------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");

			Utils.fluentWait(driver);
			MetadataCard metadataCard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and click on it");

			//3. Collapse the header in the sidepane
			//---------------------------------------
			metadataCard.collapseHeader(true);
			Utils.fluentWait(driver);

			Log.message("3. Collapse the header in the sidepane");

			//Verification: To verify if the header is collapsed
			//---------------------------------------------------
			if(metadataCard.isHeaderCollapsed()) {
				Log.pass("Test Case Passed. The header was in collapsed state.");
				metadataCard.collapseHeader(false);
			}
			else
				Log.fail("Test Case Failed. The header was not it collapsed state.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.19A : Collapse Title in Metadatacard - Sidepane verification
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Collapse Title in Metadatacard - Sidepane verification")
	public void SprintTest58_2_19A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);
			MetadataCard metadataCard = new MetadataCard(driver);

			Log.message("2. Search for an object and open it's metadatacard");

			//3. Collapse the header
			//-----------------------
			metadataCard.collapseHeader(true);
			Utils.fluentWait(driver);
			metadataCard.clickDiscardButton();
			Utils.fluentWait(driver);

			Log.message("3. Collapse the header");

			//4. Click on the Object
			//-----------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);
			Utils.fluentWait(driver);
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");

			Log.message("4. Click on the Object");

			//Verification: To verify if the header is collapsed
			//---------------------------------------------------
			Utils.fluentWait(driver);
			metadataCard = new MetadataCard(driver, true);
			if(metadataCard.isHeaderCollapsed()) {
				Log.pass("Test Case Passed. The header was in collapsed state.");
				metadataCard.collapseHeader(false);
			}
			else
				Log.fail("Test Case Failed. The header was not in collapsed state.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.19B : Collapse Title in Metadatacard (Sidepane) - Metadatacard verification
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Collapse Title in Metadatacard (Sidepane) - Metadatacard verification")
	public void SprintTest58_2_19B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object and click on it
			//-----------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");

			Utils.fluentWait(driver);
			MetadataCard metadataCard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and click on it");

			//3. Collapse the header in the sidepane
			//---------------------------------------
			metadataCard.collapseHeader(true);
			Utils.fluentWait(driver);
			driver.switchTo().defaultContent();

			Log.message("3. Collapse the header in the sidepane");

			//4. Open the metadatacard of the object
			//--------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);
			metadataCard = new MetadataCard(driver);

			Log.message("4. Open the metadatacard of the object");

			//Verification: To verify if the header is collapsed
			//---------------------------------------------------
			if(metadataCard.isHeaderCollapsed()) {
				Log.pass("Test Case Passed. The header was in collapsed state.");
				metadataCard.collapseHeader(false);
			}
			else
				Log.fail("Test Case Failed. The header was not in collapsed state.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.20 : Pop-out Metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Pop-out Metadatacard")
	public void SprintTest58_2_20(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object and click on it
			//----------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");

			Utils.fluentWait(driver);
			MetadataCard metadataCard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and click on it.");

			//3. Click the Pop-out the metadatacard in the side pane
			//------------------------------------------------------
			metadataCard.popOutMetadatacard();
			Utils.fluentWait(driver);

			Log.message("3. Click the Pop-out the metadatacard in the side pane.");

			//Verification: To verify if the metadatacard dialog appears
			//-----------------------------------------------------------
			try {
				metadataCard = new MetadataCard(driver);
				Log.pass("Test Case Passed. Pop-out the metadatacard works as expected.");
			}
			catch (Exception e1) {
				Log.fail("Test Case Failed. Pop-out the metadatacard option did not work as expected.", driver);
			}

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}


	/**
	 * 105_2_9A : 'Color change in metadatacard header when a property value is edited
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "'Color change in metadatacard header when a property value is edited")
	public void SprintTest105_2_9A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object and open its metadatacard
			//--------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			MetadataCard metadataCard = new MetadataCard(driver);

			Log.message("2. Search for an object and open its metadatacard.");

			//3. Make some modifications in the metadatacard
			//-----------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));

			Log.message("3. Make some modifications in the metadatacard.");

			//Verification: To verify if the Color of header has changed
			//-----------------------------------------------------------
			if(dataPool.get("ColorCode").toUpperCase().contains(metadataCard.getHeaderColor().toUpperCase()))
				Log.pass("Test Case Passed. The Color of header has changed to yellow.");
			else
				Log.fail("Test Case Failed. Header color("+metadataCard.getHeaderColor()+") was not in the expected color("+dataPool.get("ColorCode")+").", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105_2_9B : 'Color change in right pane header when a property value is edited
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "'Color change in right pane header when a property value is edited")
	public void SprintTest105_2_9B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object and Click On it
			//----------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");

			Utils.fluentWait(driver);

			MetadataCard metadataCard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and Click On it.");

			//3. Make some modifications in the right pane
			//-----------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));

			Log.message("3. Make some modifications in the right pane.");

			//Verification: To verify if the Color of header has changed
			//-----------------------------------------------------------
			if(dataPool.get("ColorCode").toUpperCase().contains(metadataCard.getHeaderColor().toUpperCase()))
				Log.pass("Test Case Passed. The Color of header has changed to yellow.");
			else
				Log.fail("Test Case Failed. Header color("+metadataCard.getHeaderColor()+") was not in the expected color("+dataPool.get("ColorCode")+").", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105_2_10A : 'Color in metadatacard header when focus is changed from property field without editing it's value
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "'Color in metadatacard header when focus is changed from property field without editing it's value")
	public void SprintTest105_2_10A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object and Click On it
			//----------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			MetadataCard metadataCard = new MetadataCard(driver);

			Log.message("2. Search for an object and Click On it.");

			//3. Edit a property and change focus without making any changes
			//---------------------------------------------------------------
			metadataCard.clickPropertyField(dataPool.get("Property"));

			if(!dataPool.get("EditColorCode").toUpperCase().contains(metadataCard.getHeaderColor().toUpperCase()))
				throw new Exception("Metadatacard header color did not change on clicking on a property field.");

			metadataCard.savePropValue(dataPool.get("Property"));

			Log.message("3. Edit a property and change focus without making any changes.");

			//Verification: To verify if the color of the header remains unchanged
			//---------------------------------------------------------------------
			if(dataPool.get("ColorCode").toUpperCase().contains(metadataCard.getHeaderColor().toUpperCase()))
				Log.pass("Test Case Passed. The color of the header remains unchanged.");
			else
				Log.fail("Test Case Failed. Header color("+metadataCard.getHeaderColor()+") was not in the expected color("+dataPool.get("ColorCode")+").", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105_2_10B : 'Color in right pane header when focus is changed from property field without editing it's value
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "'Color in right pane header when focus is changed from property field without editing it's value")
	public void SprintTest105_2_10B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object and Click On it
			//----------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");

			MetadataCard metadataCard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and Click On it.");

			//3. Edit a property and change focus without making any changes
			//---------------------------------------------------------------
			metadataCard.clickPropertyField(dataPool.get("Property"));

			if(!dataPool.get("EditColorCode").toUpperCase().contains(metadataCard.getHeaderColor().toUpperCase()))
				throw new Exception("Metadatacard header color did not change on clicking on a property field.");

			metadataCard.savePropValue(dataPool.get("Property"));

			Log.message("3. Edit a property and change focus without making any changes.");

			//Verification: To verify if the color of the header remains unchanged
			//---------------------------------------------------------------------
			if(dataPool.get("ColorCode").toUpperCase().contains(metadataCard.getHeaderColor().toUpperCase()))
				Log.pass("Test Case Passed. The color of the header remains unchanged.");
			else
				Log.fail("Test Case Failed. Header color("+metadataCard.getHeaderColor()+") was not in the expected color("+dataPool.get("ColorCode")+").", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

}
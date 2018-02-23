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
public class Comments {

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
	 * 44.5.6A : Not changing the Comments also increases the Version of the object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Changing the Comments of an object changes the metadatacard to edit mode.")
	public void SprintTest44_5_6A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			int expectedVersion = metadatacard.getVersion();

			Log.message("2. Search for an object and open it's metadatacard.");

			//3. Set the value for the required properties
			//--------------------------------------------
			metadatacard.setComments("");
			Utils.fluentWait(driver);

			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("3. Set the value for the required properties");

			//Verify if the metadatacard is in edit mode
			//-------------------------------------------
			metadatacard = new MetadataCard(driver, true);

			if(expectedVersion == metadatacard.getVersion())
				Log.pass("Test Case Passed. The Version of the object was not changed, as expected.");
			else
				Log.fail("Test Case Failed. The Version of the object was changed even when no comments were set.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 44.5.6B : Not changing the Comments also increases the Version of the object (Side Pane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Changing the Comments of an object changes the metadatacard to edit mode (Side Pane).")
	public void SprintTest44_5_6B(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			int expectedVersion = metadatacard.getVersion();

			Log.message("2. Search for an object and open it's metadatacard.");

			//3. Set the value for the required properties
			//--------------------------------------------
			metadatacard.setComments("");
			Utils.fluentWait(driver);

			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("3. Set the value for the required properties");

			//Verify if the metadatacard is in edit mode
			//-------------------------------------------
			Utils.fluentWait(driver);
			metadatacard = new MetadataCard(driver, true);

			if(expectedVersion == metadatacard.getVersion())
				Log.pass("Test Case Passed. The Version of the object was not changed, as expected.");
			else
				Log.fail("Test Case Failed. The Version of the object was changed even when no comments were set.", driver);	

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.1 : Set Comments in Metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Set Comments in Metadatacard.")
	public void SprintTest58_2_1(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			Log.message("2. Searched for an object and opened it's metadatacard");

			//3. Set the Comment and save
			//----------------------------
			metadataCard.setComments(dataPool.get("Comment"));
			metadataCard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("3. Entered the Comment and saved the metadatacard.");

			//4. Open the Comments dialog
			//----------------------------
			homePage.listView.rightClickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Comments.Value);
			Utils.fluentWait(driver);

			//Step-5 : Verify if metadatacard comment dialog is displayed
			//-----------------------------------------------------------
			metadataCard = new MetadataCard(driver);
			if(!metadataCard.isMetadataCardCommentDisplayed())
				throw new Exception ("Metadatacard comment dialog is not displayed.");

			Log.message("4. Opened the metadatacard Comments dialog.");

			//Verification: To verify if the Comment is set
			//----------------------------------------------
			if(metadataCard.getComments().get(0).equals(dataPool.get("Comment")))
				Log.pass("Test Case Passed. Comment was set to the object through metadatacard.");
			else
				Log.fail("Test Case Failed. Comment was not set to the object through metadatacard.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.2 : Set Comments in Metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Set Comments in Metadatacard.")
	public void SprintTest58_2_2(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			MetadataCard metadataCard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and open it's metadatacard");

			//3. Set the Comment and save
			//----------------------------
			metadataCard.setComments(dataPool.get("Comment"));
			metadataCard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("3. Set the Comment and save.");

			//4. Open the Comments dialog
			//----------------------------
			homePage.listView.rightClickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Comments.Value);
			Utils.fluentWait(driver);

			//Step-5 : Verify if metadatacard comment dialog is displayed
			//-----------------------------------------------------------
			metadataCard = new MetadataCard(driver);
			if(!metadataCard.isMetadataCardCommentDisplayed())
				throw new Exception ("Metadatacard comment dialog is not displayed.");

			Log.message("4. Opened the metadatacard Comments dialog.");

			//Verification: To verify if the Comment is set
			//----------------------------------------------
			if(metadataCard.getComments().get(0).equals(dataPool.get("Comment")))
				Log.pass("Test Case Passed. Comment was set to the object  through Sidepane.");
			else
				Log.fail("Test Case Failed. Comment was not set to the object  through Sidepane.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.3 : Set Comments in Comments dialog
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Set Comments in Comments dialog.")
	public void SprintTest58_2_3(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object and open it's Comments dialog
			//------------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.rightClickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Comments.Value);
			Utils.fluentWait(driver);

			Log.message("2. Search for an object and open it's Comments dialog");

			MetadataCard metadataCard = new MetadataCard(driver);

			//3. Set the Comment and save
			//----------------------------
			metadataCard.setComments(dataPool.get("Comment"));
			metadataCard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("3. Set the Comment and save.");

			//Verification: To verify if the Comment is set
			//----------------------------------------------
			metadataCard = new MetadataCard(driver, true);

			if(metadataCard.getComments().contains(dataPool.get("Comment")))
				Log.pass("Test Case Passed. Comment was set to the object through Comments dialog.");
			else
				Log.fail("Test Case Failed. Comment was not set to the object through Comments dialog.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.4 : Set Comments in metadatacard after Check out and perfrom undo check out
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Set Comments in metadatacard after Check out and perfrom undo check out.")
	public void SprintTest58_2_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object, check out and open it's metadatacard
			//--------------------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);
			MetadataCard metadataCard = new MetadataCard(driver);

			Log.message("2. Search for an object, check out and open it's metadatacard");

			//3. Set the Comment and save
			//----------------------------
			metadataCard.setComments(dataPool.get("Comment"));
			metadataCard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("3. Set the Comment and save.");

			//4. Perfrom undo Checkout
			//-------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);
			Utils.fluentWait(driver);
			MFilesDialog mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.confirmUndoCheckOut(true);

			Log.message("4. Perfrom undo Checkout");

			//4. Open the Comments dialog
			//----------------------------
			homePage.listView.rightClickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Comments.Value);
			Utils.fluentWait(driver);

			metadataCard = new MetadataCard(driver);

			//Verification: To verify if the Comment is set
			//----------------------------------------------
			if(!metadataCard.getComments().contains(dataPool.get("Comment")))
				Log.pass("Test Case Passed. Comment was removed from the object after undo - check out.");
			else
				Log.fail("Test Case Failed. Comment was not removed from the object after undo - check out.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.5 : Set Comments in metadatacard side pane after Check out and perfrom undo check out
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Set Comments in metadatacard side pane after Check out and perfrom undo check out.")
	public void SprintTest58_2_5(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object, check out and open it's metadatacard
			//--------------------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			MetadataCard metadataCard = new MetadataCard(driver, true);

			Log.message("2. Search for an object, check out and open it's metadatacard");

			//3. Set the Comment and save
			//----------------------------
			metadataCard.setComments(dataPool.get("Comment"));
			metadataCard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("3. Set the Comment and save.");

			//4. Perfrom undo Checkout
			//-------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);
			Utils.fluentWait(driver);
			MFilesDialog mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.confirmUndoCheckOut(true);

			Log.message("4. Perfrom undo Checkout");

			//4. Open the Comments dialog
			//----------------------------
			homePage.listView.rightClickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Comments.Value);
			Utils.fluentWait(driver);

			metadataCard = new MetadataCard(driver);

			//Verification: To verify if the Comment is set
			//----------------------------------------------
			if(!metadataCard.getComments().contains(dataPool.get("Comment")))
				Log.pass("Test Case Passed. Comment was removed from the object after undo - check out.");
			else
				Log.fail("Test Case Failed. Comment was not removed from the object after undo - check out.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.6 : Set Comments in Comments dialog after Check out and perfrom undo check out
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Set Comments in Comments dialog after Check out and perfrom undo check out.")
	public void SprintTest58_2_6(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object, check out and open it's metadatacard
			//--------------------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);

			if(!homePage.listView.rightClickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Comments.Value);
			Utils.fluentWait(driver);

			MetadataCard metadataCard = new MetadataCard(driver);

			Log.message("2. Searched for an object, check out and opened it's metadatacard");

			//3. Set the Comment and save
			//----------------------------
			metadataCard.setComments(dataPool.get("Comment"));
			metadataCard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("3. Entered the Comment and saved the metadatacard.");

			//4. Perfrom undo Checkout
			//-------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);
			Utils.fluentWait(driver);

			MFilesDialog mFilesDialog = new MFilesDialog(driver);
			mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.confirmUndoCheckOut(true);

			Log.message("4. Perfrom undo Checkout");

			//4. Open the Comments dialog
			//----------------------------
			homePage.listView.rightClickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Comments.Value);
			Utils.fluentWait(driver);

			metadataCard = new MetadataCard(driver);

			//Verification: To verify if the Comment is set
			//----------------------------------------------
			if(!metadataCard.getComments().contains(dataPool.get("Comment")))
				Log.pass("Test Case Passed. Comment was removed from the object after undo - check out.");
			else
				Log.fail("Test Case Failed. Comment was not removed from the object after undo - check out.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.7 : Displaying multi-line comments in Listview
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Displaying multi-line comments in Listview.")
	public void SprintTest58_2_7(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//3. Set the Comment and save
			//----------------------------
			metadataCard.setComments(dataPool.get("Comment"));
			metadataCard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("3. Set the Comment and save.");

			//4. Insert the Comment column
			//-----------------------------
			homePage.listView.insertColumn("Comment");
			Utils.fluentWait(driver);

			Log.message("4. Insert the Comment column.");

			//Verification: To verify if the Comment is set
			//----------------------------------------------
			if(homePage.listView.getColumnValueByItemIndex(homePage.listView.getItemIndexByItemName(dataPool.get("Object")), "Comment").equals(dataPool.get("Comment").replace("\n", " "))) {
				homePage.listView.removeColumn("Comment");
				Log.pass("Test Case Passed. Comment was set to the object through metadatacard.");
			}
			else {
				homePage.listView.removeColumn("Comment");
				Log.fail("Test Case Failed. Comment was not set to the object through metadatacard.", driver);
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
	 * 58.2.8 : Number of Comments shown in the Toggle Comments icon
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Number of Comments shown in the Toggle Comments icon.")
	public void SprintTest58_2_8(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//3. Set the Comment and save
			//----------------------------
			metadataCard.setComments(dataPool.get("Comment"));
			metadataCard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("3. Set the Comment and save.");

			//4. Open the Metadatacard of the object
			//--------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);
			metadataCard = new MetadataCard(driver);

			Log.message("4. Open the Metadatacard of the object.");

			//Verification: To verify if the Comment count is shown correct
			//--------------------------------------------------------------
			if(metadataCard.getCommentCount() == metadataCard.getComments().size())
				Log.pass("Test Case Passed. Comment count was displayed correctly.");
			else 
				Log.fail("Test Case Failed. Comment count was not displayed correctly.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.9 : Number of Comments shown in the Toggle Comments icon (Side pane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Number of Comments shown in the Toggle Comments icon (Side pane).")
	public void SprintTest58_2_9(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			MetadataCard metadataCard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and open it's metadatacard");

			//3. Set the Comment and save
			//----------------------------
			metadataCard.setComments(dataPool.get("Comment"));
			metadataCard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("3. Set the Comment and save.");

			//4. Open the Metadatacard of the object
			//--------------------------------------
			Utils.fluentWait(driver);
			metadataCard = new MetadataCard(driver, true);

			Log.message("4. Open the Metadatacard of the object.");

			//Verification: To verify if the Comment count is shown correct
			//--------------------------------------------------------------
			if(metadataCard.getCommentCount() == metadataCard.getComments().size())
				Log.pass("Test Case Passed. Comment count was displayed correctly.");
			else 
				Log.fail("Test Case Failed. Comment count was not displayed correctly.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}


	/**
	 * 58.2.23A : Number of Comments shown in the Toggle Comments icon
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Number of Comments shown in the Toggle Comments icon.")
	public void SprintTest58_2_23A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//3. Set the Comment and save
			//----------------------------
			int expectedCount = metadataCard.getCommentCount();
			metadataCard.collapseHeader(true);
			metadataCard.setComments(dataPool.get("Comment"));
			metadataCard.saveAndClose();

			Log.message("3. Set the Comment and save.");

			//4. Open the Metadatacard of the object
			//--------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);
			metadataCard = new MetadataCard(driver);

			Log.message("4. Open the Metadatacard of the object.");

			//Verification: To verify if the Comment count is shown correct
			//--------------------------------------------------------------
			if(metadataCard.getCommentCount() == metadataCard.getComments().size() && metadataCard.getCommentCount() == expectedCount+1) {
				Log.pass("Test Case Passed. Comment count was displayed correctly.");
				metadataCard.collapseHeader(false);
				metadataCard.clickDiscardButton();
			}
			else 
				Log.fail("Test Case Failed. Comment count was not displayed correctly.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			try
			{
				ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

				HomePage homePage = new HomePage(driver);
				homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));
				Utils.fluentWait(driver);

				if(!homePage.listView.clickItem(dataPool.get("Object")))
					throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");

				homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
				Utils.fluentWait(driver);
				MetadataCard metadataCard = new MetadataCard(driver);

				//3. Set the Comment and save
				//----------------------------
				if(!metadataCard.isHeaderCollapsed())
					metadataCard.collapseHeader(false);

				metadataCard.clickDiscardButton();
			}
			catch (Exception e0) {
				Log.exception(e0, driver);
			} //End catch
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.23B : Number of Comments shown in the Toggle Comments icon (Side pane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Number of Comments shown in the Toggle Comments icon (Side pane).")
	public void SprintTest58_2_23B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			MetadataCard metadataCard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and open it's metadatacard");

			//3. Set the Comment and save
			//----------------------------
			int expectedCount = metadataCard.getCommentCount();
			metadataCard.collapseHeader(true);
			metadataCard.setComments(dataPool.get("Comment"));
			metadataCard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("3. Set the Comment and save.");

			//4. Open the Metadatacard of the object
			//--------------------------------------
			Utils.fluentWait(driver);
			metadataCard = new MetadataCard(driver, true);

			Log.message("4. Open the Metadatacard of the object.");

			//Verification: To verify if the Comment count is shown correct
			//--------------------------------------------------------------
			if(metadataCard.getCommentCount() == metadataCard.getComments().size() && metadataCard.getCommentCount() == expectedCount+1) {
				Log.pass("Test Case Passed. Comment count was displayed correctly.");
				metadataCard.collapseHeader(false);
			}
			else 
				Log.fail("Test Case Failed. Comment count was not displayed correctly.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			try
			{
				ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

				HomePage homePage = new HomePage(driver);
				homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));
				Utils.fluentWait(driver);

				if(!homePage.listView.clickItem(dataPool.get("Object")))
					throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");

				homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
				Utils.fluentWait(driver);
				MetadataCard metadataCard = new MetadataCard(driver);

				//3. Set the Comment and save
				//----------------------------
				if(!metadataCard.isHeaderCollapsed())
					metadataCard.collapseHeader(false);

				metadataCard.clickDiscardButton();
			}
			catch (Exception e0) {
				Log.exception(e0, driver);
			} //End catch
			Utility.quitDriver(driver);
		}
	}
}

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
public class FavoritesAndFollow {

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
			userName = xmlParameters.getParameter("UserName");
			password = xmlParameters.getParameter("Password");
			testVault = xmlParameters.getParameter("VaultName");		
			userFullName = xmlParameters.getParameter("UserFullName");
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
	 * 58.2.10 : Adding Object to Favorites from Metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Adding Object to Favorites from Metadatacard.")
	public void SprintTest58_2_10(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//3. Click the 'Star' in the ribbon
			//----------------------------------
			metadataCard.setFavorite(true);
			driver.switchTo().defaultContent();

			Log.message("3. Click the 'Star' in the ribbon");

			//4. Click the Ok button in the Confrimation dialog
			//--------------------------------------------------
			MFilesDialog mFilesDialog = new MFilesDialog(driver, "M-Files");
			mFilesDialog.clickOkButton();
			metadataCard = new MetadataCard(driver);
			metadataCard.clickDiscardButton();
			Utils.fluentWait(driver);

			Log.message("4. Click the Ok button in the Confrimation dialog");

			//5. Navigate to the Favorites View
			//---------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Favorites.Value);
			Utils.fluentWait(driver);

			Log.message("5. Navigate to the Favorites View");

			//Verification: To verify if the object is available in the Favorites view of the user
			//-------------------------------------------------------------------------------------
			if(homePage.listView.isItemExists(dataPool.get("Object")))
				Log.pass("Test Case Passed. The Object is available in the favorites view as expected.");
			else
				Log.fail("Test Case Failed. The Object was not available in the Favorites view.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.11 : Adding Object to Favorites from (SidePane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Adding Object to Favorites from (SidePane).")
	public void SprintTest58_2_11(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//3. Click the 'Star' in the ribbon
			//----------------------------------
			metadataCard.setFavorite(true);
			driver.switchTo().defaultContent();

			Log.message("3. Click the 'Star' in the ribbon");

			//4. Click the Ok button in the Confrimation dialog
			//--------------------------------------------------
			MFilesDialog mFilesDialog = new MFilesDialog(driver, "M-Files");
			mFilesDialog.clickOkButton();

			Log.message("4. Click the Ok button in the Confrimation dialog");

			//5. Navigate to the Favorites View
			//---------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Favorites.Value);
			Utils.fluentWait(driver);

			Log.message("5. Navigate to the Favorites View");

			//Verification: To verify if the object is available in the Favorites view of the user
			//-------------------------------------------------------------------------------------
			if(homePage.listView.isItemExists(dataPool.get("Object")))
				Log.pass("Test Case Passed. The Object is available in the favorites view as expected.");
			else
				Log.fail("Test Case Failed. The Object was not available in the Favorites view.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.12 : Adding Object to Favorites from Metadatacard (Metadatacard verification)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Adding Object to Favorites from Metadatacard.")
	public void SprintTest58_2_12(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//3. Click the 'Star' in the ribbon
			//----------------------------------
			metadataCard.setFavorite(true);
			driver.switchTo().defaultContent();

			Log.message("3. Click the 'Star' in the ribbon");

			//4. Click the Ok button in the Confrimation dialog
			//--------------------------------------------------
			MFilesDialog mFilesDialog = new MFilesDialog(driver, "M-Files");
			mFilesDialog.clickOkButton();
			metadataCard = new MetadataCard(driver);

			Log.message("4. Click the Ok button in the Confrimation dialog");

			//Verification: To verify if the Favorites icon displays the correct state
			//-------------------------------------------------------------------------
			if(metadataCard.isFavorite())
				Log.pass("Test Case Passed. The Favorite icon in the metadatacard shows the correct state.");
			else
				Log.fail("Test Case Failed. The Favorite icon in the metadatacard does not show the correct state.", driver);

			metadataCard.clickDiscardButton();
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.13 : Adding Object to Favorites from (SidePane) (SidePane verification)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Adding Object to Favorites from (SidePane) (SidePane verification)")
	public void SprintTest58_2_13(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//3. Click the 'Star' in the ribbon
			//----------------------------------
			metadataCard.setFavorite(true);
			driver.switchTo().defaultContent();

			Log.message("3. Click the 'Star' in the ribbon");

			//4. Click the Ok button in the Confrimation dialog
			//--------------------------------------------------
			MFilesDialog mFilesDialog = new MFilesDialog(driver, "M-Files");
			mFilesDialog.clickOkButton();
			metadataCard = new MetadataCard(driver, true);

			Log.message("4. Click the Ok button in the Confrimation dialog");

			//Verification: To verify if the Favorites icon displays the correct state
			//-------------------------------------------------------------------------
			if(metadataCard.isFavorite())
				Log.pass("Test Case Passed. The Favorite icon in the metadatacard shows the correct state.");
			else
				Log.fail("Test Case Failed. The Favorite icon in the metadatacard does not show the correct state.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.14 : Remove Object to Favorites from Metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Remove Object to Favorites from Metadatacard.")
	public void SprintTest58_2_14(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//3. Click the 'Star' in the ribbon
			//----------------------------------
			metadataCard.setFavorite(false);
			driver.switchTo().defaultContent();

			Log.message("3. Click the 'Star' in the ribbon");

			//4. Click the Ok button in the Confrimation dialog
			//--------------------------------------------------
			MFilesDialog mFilesDialog = new MFilesDialog(driver, "M-Files");
			mFilesDialog.clickOkButton();
			metadataCard = new MetadataCard(driver);
			metadataCard.clickDiscardButton();
			Utils.fluentWait(driver);

			Log.message("4. Click the Ok button in the Confrimation dialog");

			//5. Navigate to the Favorites View
			//---------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Favorites.Value);
			Utils.fluentWait(driver);

			Log.message("5. Navigate to the Favorites View");

			//Verification: To verify if the object is available in the Favorites view of the user
			//-------------------------------------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				Log.pass("Test Case Passed. The Object is removed from the favorites view as expected.");
			else
				Log.fail("Test Case Failed. The Object was not removed from the Favorites view.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.15 : Remove Object to Favorites from (SidePane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Remove Object to Favorites from (SidePane).")
	public void SprintTest58_2_15(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//3. Click the 'Star' in the ribbon
			//----------------------------------
			metadataCard.setFavorite(false);
			driver.switchTo().defaultContent();

			Log.message("3. Click the 'Star' in the ribbon");

			//4. Click the Ok button in the Confrimation dialog
			//--------------------------------------------------
			MFilesDialog mFilesDialog = new MFilesDialog(driver, "M-Files");
			mFilesDialog.clickOkButton();

			Log.message("4. Click the Ok button in the Confrimation dialog");

			//5. Navigate to the Favorites View
			//---------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Favorites.Value);
			Utils.fluentWait(driver);

			Log.message("5. Navigate to the Favorites View");

			//Verification: To verify if the object is available in the Favorites view of the user
			//-------------------------------------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				Log.pass("Test Case Passed. The Object is removed from the favorites view as expected.");
			else
				Log.fail("Test Case Failed. The Object was not removed from the Favorites view.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.16 : Remove Object to Favorites from Metadatacard (Metadatacard verification)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Remove Object to Favorites from Metadatacard.")
	public void SprintTest58_2_16(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//3. Click the 'Star' in the ribbon
			//----------------------------------
			metadataCard.setFavorite(false);

			Log.message("3. Click the 'Star' in the ribbon");

			//4. Click the Ok button in the Confrimation dialog
			//--------------------------------------------------
			MFilesDialog mFilesDialog = new MFilesDialog(driver, "M-Files");
			mFilesDialog.clickOkButton();
			metadataCard = new MetadataCard(driver);

			Log.message("4. Click the Ok button in the Confrimation dialog");

			//Verification: To verify if the Favorites icon displays the correct state
			//-------------------------------------------------------------------------
			if(!metadataCard.isFavorite())
				Log.pass("Test Case Passed. The Favorite icon in the metadatacard shows the correct state.");
			else
				Log.fail("Test Case Failed. The Favorite icon in the metadatacard does not show the correct state.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.17 : Remove Object to Favorites from (SidePane) (SidePane verification)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Remove Object to Favorites from (SidePane) (SidePane verification)")
	public void SprintTest58_2_17(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//3. Click the 'Star' in the ribbon
			//----------------------------------
			metadataCard.setFavorite(false);
			driver.switchTo().defaultContent();

			Log.message("3. Click the 'Star' in the ribbon");

			//4. Click the Ok button in the Confrimation dialog
			//--------------------------------------------------
			MFilesDialog mFilesDialog = new MFilesDialog(driver, "M-Files");
			mFilesDialog.clickOkButton();
			metadataCard = new MetadataCard(driver, true);

			Log.message("4. Click the Ok button in the Confrimation dialog");

			//Verification: To verify if the object is available in the Favorites view of the user
			//-------------------------------------------------------------------------------------
			if(!metadataCard.isFavorite())
				Log.pass("Test Case Passed. The Favorite icon in the metadatacard shows the correct state.");
			else
				Log.fail("Test Case Failed. The Favorite icon in the metadatacard does not show the correct state.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}


	/**
	 * 58.2.21A : Add to Favorites with Collapsed header
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Add to Favorites with Collapsed header")
	public void SprintTest58_2_21A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object and Open it's metadatacard
			//----------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Favorites.Value);
			Utils.fluentWait(driver);

			if(homePage.listView.isItemExists(dataPool.get("Object")))  
				throw new SkipException("The Object is already in the favorites of the current user.");

			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);
			MetadataCard metadataCard = new MetadataCard(driver);

			Log.message("2. Search for an object and Open it's metadatacard.");

			//3. Collapse the header
			//-----------------------
			metadataCard.collapseHeader(true);
			Utils.fluentWait(driver);

			Log.message("3. Collapse the header.");

			//4. Click the Favorites icon
			//----------------------------
			metadataCard.setFavorite(true);
			driver.switchTo().defaultContent();

			Log.message("4. Click the Favorites icon");

			//4. Click the Ok button in the Confrimation dialog
			//--------------------------------------------------
			MFilesDialog mFilesDialog = new MFilesDialog(driver, "M-Files");
			mFilesDialog.clickOkButton();
			metadataCard = new MetadataCard(driver);
			metadataCard.clickDiscardButton();
			Utils.fluentWait(driver);

			Log.message("5. Click the Ok button in the Confrimation dialog.");

			//Verification: To verify if the Object is added to favorites 
			//-----------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Favorites.Value);
			Utils.fluentWait(driver);

			if(homePage.listView.clickItem(dataPool.get("Object")))  {
				Log.pass("Test Case Passed. The Object is added to favorites.");
				Utils.fluentWait(driver);
				metadataCard = new MetadataCard(driver, true);
				metadataCard.collapseHeader(false);
			}
			else
				Log.fail("Test Case Failed. The object was not added to favorites view.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.21B : Add to Favorites with Collapsed header (Sidepane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Add to Favorites with Collapsed header (Sidepane)")
	public void SprintTest58_2_21B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object and Open it's metadatacard
			//----------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Favorites.Value);
			Utils.fluentWait(driver);

			if(homePage.listView.isItemExists(dataPool.get("Object")))  
				throw new SkipException("The Object is already in the favorites of the current user.");

			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");

			Utils.fluentWait(driver);
			MetadataCard metadataCard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and Open it's metadatacard.");

			//3. Collapse the header
			//-----------------------
			metadataCard.collapseHeader(true);
			Utils.fluentWait(driver);

			Log.message("3. Collapse the header.");

			//4. Click the Favorites icon
			//----------------------------
			metadataCard.setFavorite(true);
			driver.switchTo().defaultContent();

			Log.message("4. Click the Favorites icon");

			//4. Click the Ok button in the Confrimation dialog
			//--------------------------------------------------
			MFilesDialog mFilesDialog = new MFilesDialog(driver, "M-Files");
			mFilesDialog.clickOkButton();

			Log.message("5. Click the Ok button in the Confrimation dialog.");

			//Verification: To verify if the Object is added to favorites 
			//-----------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Favorites.Value);
			Utils.fluentWait(driver);

			if(homePage.listView.clickItem(dataPool.get("Object")))  {
				Log.pass("Test Case Passed. The Object is added to favorites.");
				Utils.fluentWait(driver);
				metadataCard = new MetadataCard(driver, true);
				metadataCard.collapseHeader(false);
			}
			else
				Log.fail("Test Case Failed. The object was not added to favorites view.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.22A : Remove from Favorites with collapsed header
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Remove from Favorites with collapsed header")
	public void SprintTest58_2_22A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object and Open it's metadatacard
			//----------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Favorites.Value);
			Utils.fluentWait(driver);

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);
			MetadataCard metadataCard = new MetadataCard(driver);

			Log.message("2. Search for an object and Open it's metadatacard.");

			//3. Collapse the header
			//-----------------------
			metadataCard.collapseHeader(true);
			Utils.fluentWait(driver);

			Log.message("3. Collapse the header.");

			//4. Click the Favorites icon
			//----------------------------
			metadataCard.setFavorite(false);
			driver.switchTo().defaultContent();
			Utils.fluentWait(driver); //Fluent wait required, object is removed from favorites while in favorites view (the listview is refreshed)

			Log.message("4. Click the Favorites icon");

			//4. Click the Ok button in the Confrimation dialog
			//--------------------------------------------------
			MFilesDialog mFilesDialog = new MFilesDialog(driver, "M-Files");
			mFilesDialog.clickOkButton();
			metadataCard = new MetadataCard(driver);
			metadataCard.clickDiscardButton();
			Utils.fluentWait(driver);

			Log.message("5. Click the Ok button in the Confrimation dialog.");

			//Verification: To verify if the Object is added to favorites 
			//-----------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);
			Utils.fluentWait(driver);

			homePage.taskPanel.clickItem(Caption.MenuItems.Favorites.Value);
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))  
				Log.pass("Test Case Passed. The Object is removed from favorites.");
			else
				Log.fail("Test Case Failed. The object was not removed from favorites view.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.22B : Remove from Favorites with collapsed header (Sidepane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Remove from Favorites with collapsed header (Sidepane)")
	public void SprintTest58_2_22B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object and Open it's metadatacard
			//----------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");

			Log.message("2. Search for an object and Open it's metadatacard.");

			//3. Set object to favorites
			//----------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);
			metadataCard.setFavorite(true);
			driver.switchTo().defaultContent();
			MFilesDialog mFilesDialog = new MFilesDialog(driver, "M-Files");
			mFilesDialog.clickOkButton();
			Log.message("3. Set object to favorites.");


			//4. Collapse the header
			//-----------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Favorites.Value);
			homePage.listView.clickItem(dataPool.get("Object"));
			metadataCard = new MetadataCard(driver, true);
			metadataCard.collapseHeader(true);

			Log.message("4. Open object's metadata card in favorites view and collapse the header.");

			//5. Click the Favorites icon
			//----------------------------
			metadataCard.setFavorite(false);
			driver.switchTo().defaultContent();
			Utils.fluentWait(driver); //Fluent wait required, object is removed from favorites while in favorites view (the listview is refreshed)
			Log.message("5. Click the Favorites icon");

			//6. Click the Ok button in the Confrimation dialog
			//--------------------------------------------------
			mFilesDialog = new MFilesDialog(driver, "M-Files");
			mFilesDialog.clickOkButton();

			Log.message("6. Click the Ok button in the Confrimation dialog.");

			//Verification: To verify if the Object is added to favorites 
			//-----------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);

			homePage.taskPanel.clickItem(Caption.MenuItems.Favorites.Value);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))  
				Log.pass("Test Case Passed. The Object is removed from favorites.");
			else
				Log.fail("Test Case Failed. The object was not removed from favorites view.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}


	/**
	 * 105_2_1A : Clicking flag icon in metadatacard to follow object should show confirmation dialog
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Clicking flag icon in metadatacard to follow object should show confirmation dialog")
	public void SprintTest105_2_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//3. Click on the flag icon in the metadatacard
			//----------------------------------------------
			metadataCard.setFollowFlag(true);

			Log.message("3. Click on the flag icon in the metadatacard.");

			//4. Click the Ok button in the conirmation dialog
			//-------------------------------------------------
			driver.switchTo().defaultContent();

			if(!MFilesDialog.exists(driver, "M-Files"))
				throw new Exception("The expected Confirmation dialog did not appear.");

			MFilesDialog mFilesDialog = new MFilesDialog(driver, "M-Files");
			String actualMessage = mFilesDialog.getMessage();
			mFilesDialog.clickOkButton();

			metadataCard = new MetadataCard(driver);
			if(!metadataCard.getFollowFlag())
				throw new Exception("The flag did not change to selected after clicking ok in the confirmation dialog.");

			Log.message("4. Click the Ok button in the conirmation dialog");

			//Verification: To verify if the Follow this flag appears On
			//-----------------------------------------------------------
			if(actualMessage.equals(dataPool.get("ExpectedMessage")))
				Log.pass("Test Case Passed. The Confirmation dialog with the expected message appeared.");
			else
				Log.fail("Test Case Failed. The Confirmation dialog did not have the expected message.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105_2_1B : Clicking flag icon in right pane to follow object should show confirmation dialog
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Clicking flag icon in right pane to follow object should show confirmation dialog")
	public void SprintTest105_2_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			Utils.fluentWait(driver);
			MetadataCard metadataCard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and open its metadatacard.");

			//3. Click on the flag icon in the metadatacard
			//----------------------------------------------
			metadataCard.setFollowFlag(true);

			Log.message("3. Click on the flag icon in the metadatacard.");

			//4. Click the Ok button in the conirmation dialog
			//-------------------------------------------------
			driver.switchTo().defaultContent();

			if(!MFilesDialog.exists(driver, "M-Files"))
				throw new Exception("The expected Confirmation dialog did not appear.");

			MFilesDialog mFilesDialog = new MFilesDialog(driver, "M-Files");
			String actualMessage = mFilesDialog.getMessage();
			mFilesDialog.clickOkButton();

			metadataCard = new MetadataCard(driver, true);
			if(!metadataCard.getFollowFlag())
				throw new Exception("The flag did not change to selected after clicking ok in the confirmation dialog.");

			Log.message("4. Click the Ok button in the conirmation dialog");

			//Verification: To verify if the Follow this flag appears On
			//-----------------------------------------------------------
			if(actualMessage.equals(dataPool.get("ExpectedMessage")))
				Log.pass("Test Case Passed. The Confirmation dialog with the expected message appeared.");
			else
				Log.fail("Test Case Failed. The Confirmation dialog did not have the expected message.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105_2_2A : Clicking flag icon in metadatacard of an object that is already marked for follow should show confirmation dialog
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Clicking on Follow object icon in metadatacard of an object that is already marked for follow should show confirmation dialog")
	public void SprintTest105_2_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//3. Click on the flag icon in the metadatacard
			//----------------------------------------------
			metadataCard.setFollowFlag(false);

			Log.message("3. Click on the flag icon in the metadatacard.");

			//4. Click the Ok button in the conirmation dialog
			//-------------------------------------------------
			driver.switchTo().defaultContent();

			if(!MFilesDialog.exists(driver, "M-Files"))
				throw new Exception("The expected Confirmation dialog did not appear.");

			MFilesDialog mFilesDialog = new MFilesDialog(driver, "M-Files");
			String actualMessage = mFilesDialog.getMessage();
			mFilesDialog.clickOkButton();

			metadataCard = new MetadataCard(driver);
			if(metadataCard.getFollowFlag())
				throw new Exception("The flag did not change to unselected after clicking ok in the confirmation dialog.");

			Log.message("4. Click the Ok button in the conirmation dialog");

			//Verification: To verify if the Follow this flag appears On
			//-----------------------------------------------------------
			if(actualMessage.equals(dataPool.get("ExpectedMessage")))
				Log.pass("Test Case Passed. The Confirmation dialog with the expected message appeared.");
			else
				Log.fail("Test Case Failed. The Confirmation dialog did not have the expected message.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105_2_2B : Clicking flag icon in right pane of an object that is already marked for follow should show confirmation dialog
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Clicking on Follow object icon in right pane of an object that is already marked for follow should show confirmation dialog")
	public void SprintTest105_2_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, "Search only: " + dataPool.get("ObjectType"), dataPool.get("Object"));

			Log.message("2. Navigated to '" + viewToNavigate + "' view.");
			/*	
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));
			Utils.fluentWait(driver);
			 */
			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");

			Utils.fluentWait(driver);
			MetadataCard metadataCard = new MetadataCard(driver, true);

			Log.message("3. Search for an object and open its metadatacard.");

			//3. Click on the flag icon in the metadatacard
			//----------------------------------------------
			metadataCard.setFollowFlag(false);

			Log.message("4. Click on the flag icon in the metadatacard.");

			//4. Click the Ok button in the conirmation dialog
			//-------------------------------------------------
			driver.switchTo().defaultContent();

			if(!MFilesDialog.exists(driver, "M-Files"))
				throw new Exception("The expected Confirmation dialog did not appear.");

			MFilesDialog mFilesDialog = new MFilesDialog(driver, "M-Files");
			String actualMessage = mFilesDialog.getMessage();
			mFilesDialog.clickOkButton();

			metadataCard = new MetadataCard(driver, true);
			if(metadataCard.getFollowFlag())
				throw new Exception("The flag did not change to unselected after clicking ok in the confirmation dialog.");

			Log.message("5. Click the Ok button in the conirmation dialog");

			//Verification: To verify if the Follow this flag appears On
			//-----------------------------------------------------------
			if(actualMessage.equals(dataPool.get("ExpectedMessage")))
				Log.pass("Test Case Passed. The Confirmation dialog with the expected message appeared.");
			else
				Log.fail("Test Case Failed. The Confirmation dialog did not have the expected message.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}



}

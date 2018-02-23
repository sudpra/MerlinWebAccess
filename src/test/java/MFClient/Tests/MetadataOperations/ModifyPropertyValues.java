package MFClient.Tests.MetadataOperations;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.Utils;
import genericLibrary.WebDriverUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class ModifyPropertyValues {

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
	 * 44.3.1A : All Properties should be listed in More properties dialog - Try in all the object types
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard", "Smoke"}, 
			description = "All Properties should be listed in More properties dialog - Try in all the object types.")
	public void SprintTest44_3_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object
			//------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object.");

			//3. Click on the object and open it's metadatacard
			//--------------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);


			Log.message("3. Click on the object and open it's metadatacard.");

			//Verify if all the Expected properties appear in the Add Property Combo box
			//----------------------------------------------------------------------------
			String[] expectedProps = dataPool.get("ExpectedProperties").split("\n");


			MetadataCard metadatacard = new MetadataCard(driver);

			List<String> availableProps = metadatacard.getAvailableAddProperties();
			for(int count = 0; count < expectedProps.length; count++) {
				if(availableProps.indexOf(expectedProps[count].trim()) == -1 && !metadatacard.propertyExists(expectedProps[count]))
					Log.fail("Test Case Failed. The Expected Property - '" +expectedProps[count]+"' was not listed.", driver);
			}

			Log.pass("Test Case Passed. All the Expected Properties were listed.");

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 44.3.1B : All Properties should be listed in More properties dialog - Try in all the object types (New Object - Metadatacard)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "All Properties should be listed in More properties dialog - Try in all the object types (New Object - Metadatacard).")
	public void SprintTest44_3_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Perform the menu click to open the new object metadatacard
			//---------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));


			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard = metadatacard.setTemplate(dataPool.get("Extension"));


			Log.message("2. Perform the menu click to open the new object metadatacard.");

			//Verify if all the Expected properties appear in the Add Property Combo box
			//----------------------------------------------------------------------------
			String[] expectedProps = dataPool.get("ExpectedProperties").split("\n");



			List<String> availableProps = metadatacard.getAvailableAddProperties();
			for(int count = 0; count < expectedProps.length; count++) {
				if(availableProps.indexOf(expectedProps[count].trim()) == -1 && !metadatacard.propertyExists(expectedProps[count]))
					Log.fail("Test Case Failed. The Expected Property - '" +expectedProps[count]+"' was not listed.", driver);
			}

			Log.pass("Test Case Passed. All the Expected Properties were listed.");

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 44.3.1C : All Properties should be listed in More properties dialog - Try in all the object types (Right Pane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "All Properties should be listed in More properties dialog - Try in all the object types (Right Pane).")
	public void SprintTest44_3_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for an object
			//------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));


			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object.");

			//3. Click on the object and open it's metadatacard
			//--------------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));


			Log.message("3. Click on the object and open it's metadatacard.");

			//Verify if all the Expected properties appear in the Add Property Combo box
			//----------------------------------------------------------------------------

			String[] expectedProps = dataPool.get("ExpectedProperties").split("\n");

			MetadataCard metadatacard = new MetadataCard(driver, true);

			List<String> availableProps = metadatacard.getAvailableAddProperties();
			for(int count = 0; count < expectedProps.length; count++) {
				if(availableProps.indexOf(expectedProps[count].trim()) == -1 && !metadatacard.propertyExists(expectedProps[count]))
					Log.fail("Test Case Failed. The Expected Property - '" +expectedProps[count]+"' was not listed.", driver);
			}

			Log.pass("Test Case Passed. All the Expected Properties were listed.");

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 44.3.1D : All Properties should be listed in More properties dialog - Try in all the object types Settings icon Pop-out metadatacard.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "All Properties should be listed in More properties dialog - Try in all the object types Settings icon Pop-out metadatacard.")
	public void SprintTest44_3_1D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object
			//------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object.");

			//3. Click on the object and open it's metadatacard
			//--------------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));

			Log.message("3. Click on the object and open it's metadatacard.");

			//Step-4 : Click the Settings icon in the right pane metadatacard
			//---------------------------------------------------------------			
			MetadataCard metadatacard = new MetadataCard(driver, true);
			metadatacard.popOutMetadatacard();

			Log.message("4. Opened the metadatacard from the settings icon in right pane metadatacard." );

			//Verify if all the Expected properties appear in the Add Property Combo box
			//----------------------------------------------------------------------------
			String[] expectedProps = dataPool.get("ExpectedProperties").split("\n");

			metadatacard = new MetadataCard(driver);

			List<String> availableProps = metadatacard.getAvailableAddProperties();
			for(int count = 0; count < expectedProps.length; count++) {
				if(availableProps.indexOf(expectedProps[count].trim()) == -1 && !metadatacard.propertyExists(expectedProps[count]))
					Log.fail("Test Case Failed. The Expected Property - '" +expectedProps[count]+"' was not listed.", driver);
			}

			Log.pass("Test Case Passed. All the Expected Properties were listed.");

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}//End SprintTest44_3_1D


	/**
	 * 44.3.1E : All Properties should be listed in More properties dialog - Try in all the object types using metadatatab pop-out metadatacard.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "All Properties should be listed in More properties dialog - Try in all the object types using metadatatab pop-out metadatacard.")
	public void SprintTest44_3_1E(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object
			//------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object.");

			//3. Click on the object and open it's metadatacard
			//--------------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));

			Log.message("3. Click on the object and open it's metadatacard.");

			//Step-4 : Click the Settings icon in the right pane metadatacard
			//---------------------------------------------------------------			
			MetadataCard metadatacard = new MetadataCard(driver, true);
			homePage.previewPane.popoutRightPaneMetadataTab();

			Log.message("4. Opened the metadatacard from the Pop-out metadatacard in right pane." );

			//Verify if all the Expected properties appear in the Add Property Combo box
			//----------------------------------------------------------------------------
			String[] expectedProps = dataPool.get("ExpectedProperties").split("\n");

			metadatacard = new MetadataCard(driver);

			List<String> availableProps = metadatacard.getAvailableAddProperties();
			for(int count = 0; count < expectedProps.length; count++) {
				if(availableProps.indexOf(expectedProps[count].trim()) == -1 && !metadatacard.propertyExists(expectedProps[count]))
					Log.fail("Test Case Failed. The Expected Property - '" +expectedProps[count]+"' was not listed.", driver);
			}

			Log.pass("Test Case Passed. All the Expected Properties were listed.");

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}//End SprintTest44_3_1E

	/**
	 * 44.4.1A : Add html tag as value to any property (Create a new object)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard", "Smoke"}, 
			description = "Add html tag as value to any property (Create a new object).")
	public void SprintTest44_4_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Log.message("1. Logged into the Home View.");

			//2. Perform the menu click to open the new object metadatacard
			//---------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));
			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard = metadatacard.setTemplate(dataPool.get("Extension"));

			Log.message("2. Perform the menu click to open the new object metadatacard.");

			//3. Set the value for the required properties
			//--------------------------------------------
			metadatacard.setInfo(dataPool.get("Properties"));
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));

			metadatacard.setCheckInImmediately(true);

			Log.message("3. Set the value for the required properties");

			//4. Click the Create button to create the object
			//------------------------------------------------
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);
			Log.message("4. Click the Create button to create the object");

			//5. Search for the object and open it's metadatacard
			//-----------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new Exception("The specified object '" + dataPool.get("Object") + "' was not created.");

			metadatacard = new MetadataCard(driver, true);
			Log.message("5. Search for the object and open it's metadatacard");

			//Verify if all the Expected properties appear in the Add Property Combo box
			//----------------------------------------------------------------------------
			if(metadatacard.getPropertyValue(dataPool.get("Property")).equals(dataPool.get("Value")))
				Log.pass("Test Case Passed. The HTML tag was set as value for the property.");
			else
				Log.fail("Test Case Failed. The Property did not have the value that was set.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 44.4.1B : Add html tag as value to any property
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Add html tag as value to any property.")
	public void SprintTest44_4_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for an object and openit's metadatacard
			//--------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value);

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Search for an object and openit's metadatacard.");

			//3. Set the value for the Property
			//----------------------------------
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));

			Log.message("3. Set the value for the Property.");

			//4. Click the Create button to create the object
			//------------------------------------------------
			metadatacard.saveAndClose();
			Log.message("4. Click the Create button to create the object");

			//5. Open it's metadatacard
			//--------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value);
			metadatacard = new MetadataCard(driver);

			Log.message("5. Open it's metadatacard.");

			//Verify if all the Expected properties appear in the Add Property Combo box
			//----------------------------------------------------------------------------
			if(metadatacard.getPropertyValue(dataPool.get("Property")).equals(dataPool.get("Value")))
				Log.pass("Test Case Passed. The HTML tag was set as value for the property.");
			else
				Log.fail("Test Case Failed. The Property did not have the value that was set.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 44.4.1C : Add html tag as value to any property (Side Pane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Add html tag as value to any property (Side Pane).")
	public void SprintTest44_4_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object and openit's metadatacard
			//--------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));

			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and openit's metadatacard.");

			//3. Set the value for the Property
			//----------------------------------
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));

			Log.message("3. Set the value for the Property.");

			//4. Click the Create button to create the object
			//------------------------------------------------
			metadatacard.saveAndClose();


			Log.message("4. Click the Create button to create the object");

			//5. Open it's metadatacard
			//--------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));

			metadatacard = new MetadataCard(driver, true);

			Log.message("5. Open it's metadatacard.");

			//Verify if all the Expected properties appear in the Add Property Combo box
			//----------------------------------------------------------------------------
			if(metadatacard.getPropertyValue(dataPool.get("Property")).equals(dataPool.get("Value")))
				Log.pass("Test Case Passed. The HTML tag was set as value for the property.");
			else
				Log.fail("Test Case Failed. The Property did not have the value that was set.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 44.4.2A : Perform basic operations like Checkout for the object with html tag name
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Perform basic operations like Checkout for the object with html tag name.")
	public void SprintTest44_4_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Perform the menu click to open the new object metadatacard
			//---------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));
			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard = metadatacard.setTemplate(dataPool.get("Extension"));

			Log.message("2. Perform the menu click to open the new object metadatacard.");

			//3. Set the value for the required properties
			//--------------------------------------------
			metadatacard.setInfo(dataPool.get("Properties"));
			metadatacard.setCheckInImmediately(true);

			Log.message("3. Set the value for the required properties");

			//4. Click the Create button to create the object
			//------------------------------------------------
			metadatacard.saveAndClose();

			Log.message("4. Click the Create button to create the object");

			//5. Search for the object and open it's metadatacard
			//-----------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.CheckOut.Value);


			Log.message("5. Search for the object and open it's metadatacard");

			//Verify if all the Expected properties appear in the Add Property Combo box
			//----------------------------------------------------------------------------
			if (ListView.isCheckedOutByItemName(driver, dataPool.get("Object"))) //Checks if object is in checked out mode
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
	 * 44.4.2B : Perform basic operations like  Rename for the object with html tag name
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard","Bug"}, 
			description = "Perform basic operations like  Rename for the object with html tag name.")
	public void SprintTest44_4_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Perform the menu click to open the new object metadatacard
			//---------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));
			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard = metadatacard.setTemplate(dataPool.get("Extension"));

			Log.message("2. Perform the menu click to open the new object metadatacard.");

			//3. Set the value for the required properties
			//--------------------------------------------
			metadatacard.setInfo(dataPool.get("Properties"));
			metadatacard.setCheckInImmediately(true);

			Log.message("3. Set the value for the required properties");

			//4. Click the Create button to create the object
			//------------------------------------------------
			metadatacard.saveAndClose();

			Log.message("4. Click the Create button to create the object");

			//5. Search for the object and open it's metadatacard
			//-----------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Rename.Value);


			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class
			mfilesDialog.rename(dataPool.get("NewName"), true);

			Log.message("5. Search for the object and open it's metadatacard");

			//Verify if all the Expected properties appear in the Add Property Combo box
			//----------------------------------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(homePage.listView.isItemExists(dataPool.get("Object")))
				throw new Exception("Renaming an object with HTML tag in title property was not successful.");

			homePage.searchPanel.search(dataPool.get("NewName"), "Search only: "+dataPool.get("ObjectType")+"s");



			if(homePage.listView.isItemExists(dataPool.get("NewName")))
				Log.pass("Test Case Passed. The Object with the HTML tag name was Renamed successfully.");
			else
				Log.fail("Test Case Failed. Renaming an object with HTML tag name was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 44.4.2C : Perform basic operations like changing display mode, for the object with html tag name
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Perform basic operations like changing display mode, for the object with html tag name.")
	public void SprintTest44_4_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Perform the menu click to open the new object metadatacard
			//---------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));
			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard = metadatacard.setTemplate(dataPool.get("Extension"));

			Log.message("2. Perform the menu click to open the new object metadatacard.");

			//3. Set the value for the required properties
			//--------------------------------------------
			metadatacard.setInfo(dataPool.get("Properties"));


			metadatacard.setCheckInImmediately(true);

			Log.message("3. Set the value for the required properties");

			//4. Click the Create button to create the object
			//------------------------------------------------
			metadatacard.saveAndClose();


			Log.message("4. Click the Create button to create the object");

			//5. Search for the object
			//-------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");



			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			Log.message("5. Search for the object.");

			//6. Change the Display Mode of the list
			//---------------------------------------
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Thumbnails");


			//Verify if the display mode is changed
			//--------------------------------------

			if(homePage.listView.isThumbnailsView()) {
				Log.pass("Test Case Passed. The Display mode of the listing was changed successfully.");
				homePage.menuBar.ClickOperationsMenu("Display Mode>>Details");

			}
			else
				Log.fail("Test Case Failed. The Display mode of the listing was not changed.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 44.4.2D : Perform basic operations like Check In for the object with html tag name
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Perform basic operations like Check In for the object with html tag name.")
	public void SprintTest44_4_2D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Perform the menu click to open the new object metadatacard
			//---------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));
			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard = metadatacard.setTemplate(dataPool.get("Extension"));

			Log.message("2. Perform the menu click to open the new object metadatacard.");

			//3. Set the value for the required properties
			//--------------------------------------------
			metadatacard.setInfo(dataPool.get("Properties"));


			metadatacard.setCheckInImmediately(true);

			Log.message("3. Set the value for the required properties");

			//4. Click the Create button to create the object
			//------------------------------------------------
			metadatacard.saveAndClose();


			Log.message("4. Click the Create button to create the object");

			//5. Search for the object and Check Out
			//---------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");




			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			if (!homePage.listView.clickItem(dataPool.get("Object")))
				throw new Exception("Object (" + dataPool.get("Object") + ") is not selected");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("Object"))) { //Checks out object if not checked out

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Checks out from Task Pane

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("Object"))) 
					throw new Exception("Object is not checked out.");
			}

			Log.message("5. Search for the object and Check Out.");

			//6. Check In the object
			//-----------------------

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.CheckIn.Value);


			Log.message("6. Check In the object");

			//Verify if all the Expected properties appear in the Add Property Combo box
			//----------------------------------------------------------------------------
			if(!ListView.isCheckedOutByItemName(driver, dataPool.get("Object")))
				Log.pass("Test Case Passed. The Object with the HTML tag name was checked in successfully.");
			else
				Log.fail("Test Case Failed. Checking in an object with HTML tag name was not successful.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 44.4.2E : Perform basic operations like Undo Check Out for the object with html tag name
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Perform basic operations like Undo Check Out for the object with html tag name.")
	public void SprintTest44_4_2E(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Perform the menu click to open the new object metadatacard
			//---------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));
			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard = metadatacard.setTemplate(dataPool.get("Extension"));

			Log.message("2. Perform the menu click to open the new object metadatacard.");

			//3. Set the value for the required properties
			//--------------------------------------------
			metadatacard.setInfo(dataPool.get("Properties"));


			metadatacard.setCheckInImmediately(true);

			Log.message("3. Set the value for the required properties");

			//4. Click the Create button to create the object
			//------------------------------------------------
			metadatacard.saveAndClose();


			Log.message("4. Click the Create button to create the object");

			//5. Search for the object and Check Out
			//---------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");


			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			if (!homePage.listView.clickItem(dataPool.get("Object")))
				throw new Exception("Object (" + dataPool.get("Object") + ") is not selected");



			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("Object"))) { //Checks out object if not checked out

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Checks out from Task Pane

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("Object"))) 
					throw new Exception("Object is not checked out.");
			}

			Log.message("5. Search for the object and Check Out.");

			//6. Undo Check out
			//------------------

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);


			MFilesDialog mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.confirmUndoCheckOut(true);


			Log.message("6. Undo Check out.");

			//Verify if all the Expected properties appear in the Add Property Combo box
			//----------------------------------------------------------------------------
			if(!ListView.isCheckedOutByItemName(driver, dataPool.get("Object")))
				Log.pass("Test Case Passed. The Object with the HTML tag name was undo-checked out successfully.");
			else
				Log.fail("Test Case Failed. Performing undo-checkout of an object with HTML tag name was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105_2_3A : 'Last modified by' area when user makes changes to an object in metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "'Last modified by' area when user makes changes to an object in metadatacard")
	public void SprintTest105_2_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String vault = xmlParameters.getParameter("VaultName");
			driver.get(loginURL); //Launches with the URL


			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX")) //Checks if Login.aspx page is loaded
				throw new Exception ("Browser is not navigated to the Login page.");

			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), vault); //Logs into application


			Log.message("1. Logged into the Home View.");

			//2. Search for an object and open its metadatacard
			//--------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			MetadataCard metadataCard = new MetadataCard(driver);

			Log.message("2. Search for an object and open its metadatacard.");

			//3. Make some modifications in the metadatacard and click the save button
			//-------------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));

			metadataCard.saveAndClose();

			Log.message("3. Make some modifications in the metadatacard and click the save button.");

			//4. Re-open the metadatacard
			//----------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			metadataCard = new MetadataCard(driver);

			Log.message("4. Re-open the metadatacard");

			//Verification: To verify if the Last modified by property shows the expected value
			//-----------------------------------------------------------------------------------
			//if(metadataCard.getLastModifiedBy().equals(dataPool.get("UserFullName")))
			if (dataPool.get("UserFullName").toUpperCase().contains(metadataCard.getLastModifiedBy().replace(".", "").toUpperCase()))
				Log.pass("Test Case Passed. The Last modified by property shows the expected value.");
			else
				Log.fail("Test Case Failed. The Last modified by property does not show the expected value.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105_2_3B : 'Last modified by' area when user makes changes to an object in right pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "'Last modified by' area when user makes changes to an object in right pane")
	public void SprintTest105_2_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String vault = xmlParameters.getParameter("VaultName");
			driver.get(loginURL); //Launches with the URL


			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX")) //Checks if Login.aspx page is loaded
				throw new Exception ("Browser is not navigated to the Login page.");

			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), vault); //Logs into application


			Log.message("1. Logged into the Home View.");

			//2. Search for an object and click on it
			//----------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));


			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");


			MetadataCard metadataCard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and click on it.");

			//3. In the right pane Make some modifications in the metadatacard and click the save button
			//--------------------------------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));
			metadataCard.saveAndClose();


			Log.message("3. In the right pane Make some modifications in the metadatacard and click the save button.");

			//Verification: To verify if the Last modified by property shows the expected value
			//-----------------------------------------------------------------------------------\
			metadataCard = new MetadataCard(driver, true);
			//if(metadataCard.getLastModifiedBy().equals(dataPool.get("UserFullName")))
			if (dataPool.get("UserFullName").toUpperCase().contains(metadataCard.getLastModifiedBy().replace(".", "").toUpperCase()))
				Log.pass("Test Case Passed. The Last modified by property shows the expected value.");
			else
				Log.fail("Test Case Failed. The Last modified by property does not show the expected value.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105_2_4A : 'Adding new property to object property and setting values in metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "'Adding new property to object property and setting values in metadatacard")
	public void SprintTest105_2_4A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object and open its metadatacard
			//--------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			MetadataCard metadataCard = new MetadataCard(driver);

			Log.message("2. Search for an object and open its metadatacard.");

			//3. Make some modifications in the metadatacard and click the save button
			//-------------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));
			metadataCard.saveAndClose();

			Log.message("3. Make some modifications in the metadatacard and click the save button.");

			//4. Re-open the metadatacard
			//----------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			metadataCard = new MetadataCard(driver);

			Log.message("4. Re-open the metadatacard");

			//Verification: To verify if the Property is added and the value remains set
			//-----------------------------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("Property")).equals(dataPool.get("Value")))
				Log.pass("Test Case Passed. The property was added and the value remains set.");
			else
				Log.fail("Test Case Failed. Adding Property and setting value was not successful", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105_2_4B : 'Adding new property to object property and setting values in sidepane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "'Adding new property to object property and setting values in sidepane")
	public void SprintTest105_2_4B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for an object and click on it
			//----------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));


			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");


			MetadataCard metadataCard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and click on it.");

			//3. In the right pane Make some modifications in the metadatacard and click the save button
			//--------------------------------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));
			metadataCard.saveAndClose();


			Log.message("3. In the right pane Make some modifications in the metadatacard and click the save button.");

			//Verification: To verify if the Property is added and the value remains set
			//---------------------------------------------------------------------------
			metadataCard = new MetadataCard(driver, true);
			if(metadataCard.getPropertyValue(dataPool.get("Property")).equals(dataPool.get("Value")))
				Log.pass("Test Case Passed. The property was added and the value remains set.");
			else
				Log.fail("Test Case Failed. Adding Property and setting value was not successful", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105_2_5A : 'Changing Object version when user makes changes to an object in metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "'Changing Object version when user makes changes to an object in metadatacard")
	public void SprintTest105_2_5A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for an object and open its metadatacard
			//--------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));


			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);


			MetadataCard metadataCard = new MetadataCard(driver);
			int prevVersion = metadataCard.getVersion();

			Log.message("2. Search for an object and open its metadatacard.");

			//3. Make some modifications in the metadatacard and click the save button
			//-------------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));
			metadataCard.saveAndClose();


			Log.message("3. Make some modifications in the metadatacard and click the save button.");

			//4. Re-open the metadatacard
			//----------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);


			metadataCard = new MetadataCard(driver);

			Log.message("4. Re-open the metadatacard");

			//Verification: To verify if the Version in incremented by one
			//-------------------------------------------------------------
			if(metadataCard.getVersion() == prevVersion+1)
				Log.pass("Test Case Passed. Object version was incremented by one as expected.");
			else
				Log.fail("Test Case Failed. Object version was not incemented by 1.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105_2_5B : 'Chaning Object Version when user makes changes to an object in right pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "'Chaning Object Version when user makes changes to an object in right pane")
	public void SprintTest105_2_5B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for an object and click on it
			//----------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));


			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");


			MetadataCard metadataCard = new MetadataCard(driver, true);
			int prevVersion = metadataCard.getVersion();

			Log.message("2. Search for an object and click on it.");

			//3. In the right pane Make some modifications in the metadatacard and click the save button
			//--------------------------------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));
			metadataCard.saveAndClose();


			Log.message("3. In the right pane Make some modifications in the metadatacard and click the save button.");

			//Verification: To verify if the Version in incremented by one
			//-------------------------------------------------------------
			metadataCard = new MetadataCard(driver, true);
			if(metadataCard.getVersion() == prevVersion+1)
				Log.pass("Test Case Passed. Object version was incremented by one as expected.");
			else
				Log.fail("Test Case Failed. Object version was not incemented by 1.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105_2_6A : 'last modified date when user makes changes to an object in metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "'last modified date when user makes changes to an object in metadatacard")
	public void SprintTest105_2_6A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for an object and open its metadatacard
			//--------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));


			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);


			MetadataCard metadataCard = new MetadataCard(driver);
			DateFormat dateFormat = new SimpleDateFormat("M/d/yyyy");

			//get current date time with Date()
			Date date = new Date();
			String expectedDate = dateFormat.format(date).toString();

			Log.message("2. Search for an object and open its metadatacard.");

			//3. Make some modifications in the metadatacard and click the save button
			//-------------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));
			metadataCard.saveAndClose();


			Log.message("3. Make some modifications in the metadatacard and click the save button.");

			//4. Re-open the metadatacard
			//----------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);


			metadataCard = new MetadataCard(driver);

			Log.message("4. Re-open the metadatacard");

			//Verification: To verify if the Last modified date is displayed as expected
			//---------------------------------------------------------------------------
			if(expectedDate.contains(metadataCard.getLastModifiedDate()))
				Log.pass("Test Case Passed. Last modified date was displayed as expected.");
			else
				Log.fail("Test Case Failed. Last modified date did not show the expected value.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105_2_6B : 'last modified date when user makes changes to an object in right pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "'last modified date when user makes changes to an object in right pane")
	public void SprintTest105_2_6B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for an object and click on it
			//----------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));


			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");


			MetadataCard metadataCard = new MetadataCard(driver, true);
			DateFormat dateFormat = new SimpleDateFormat("M/d/yyyy");
			Date date = new Date();
			String expectedDate = dateFormat.format(date).toString();

			Log.message("2. Search for an object and click on it.");

			//3. Make some modifications in the metadatacard and click the save button
			//-------------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));
			metadataCard.saveAndClose();


			Log.message("3. Make some modifications in the metadatacard and click the save button.");

			//Verification: To verify if the Last modified date is displayed as expected
			//---------------------------------------------------------------------------
			metadataCard = new MetadataCard(driver, true);
			if(expectedDate.contains(metadataCard.getLastModifiedDate()))
				Log.pass("Test Case Passed. Last modified date was displayed as expected.");
			else
				Log.fail("Test Case Failed. Last modified date did not show the expected value.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105_2_7A : Created By value in metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard", "Smoke"}, 
			description = "Created By value in metadatacard.")
	public void SprintTest105_2_7A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String vault = xmlParameters.getParameter("VaultName");
			driver.get(loginURL); //Launches with the URL


			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX")) //Checks if Login.aspx page is loaded
				throw new Exception ("Browser is not navigated to the Login page.");

			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), vault); //Logs into application


			Log.message("1. Logged into the Home View.");

			//2. Perform the menu click to open the new object metadatacard
			//---------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));
			Utility.selectTemplate(dataPool.get("Extension"), driver);

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Perform the menu click to open the new object metadatacard.");

			//3. Set the value for the required properties
			//--------------------------------------------
			metadatacard.setInfo(dataPool.get("Properties"));
			metadatacard.setCheckInImmediately(true);

			Log.message("3. Set the value for the required properties");

			//4. Click the Create button to create the object
			//------------------------------------------------
			metadatacard.saveAndClose();

			Log.message("4. Click the Create button to create the object");

			//5. Search for the object and open it's metadatacard
			//-----------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);


			metadatacard = new MetadataCard(driver);

			Log.message("5. Search for the object and open it's metadatacard");

			//Verify if the Created by area showed the full name of the created user
			//----------------------------------------------------------------------
			if(metadatacard.getCreatedBy().equals(dataPool.get("UserFullName")))
				Log.pass("Test Case Passed. The Created by area showed the full name of the created user.");
			else
				Log.fail("Test Case Failed. The Created by area did not show the full name of the created user.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105_2_7B : Created By value in right pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard", "Smoke"}, 
			description = "Created By value in right pane.")
	public void SprintTest105_2_7B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String vault = xmlParameters.getParameter("VaultName");
			driver.get(loginURL); //Launches with the URL


			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX")) //Checks if Login.aspx page is loaded
				throw new Exception ("Browser is not navigated to the Login page.");

			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), vault); //Logs into application


			Log.message("1. Logged into the Home View.");

			//2. Perform the menu click to open the new object metadatacard
			//---------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));
			Utility.selectTemplate(dataPool.get("Extension"), driver);

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Perform the menu click to open the new object metadatacard.");

			//3. Set the value for the required properties
			//--------------------------------------------
			metadatacard.setInfo(dataPool.get("Properties"));
			metadatacard.setCheckInImmediately(true);

			Log.message("3. Set the value for the required properties");

			//4. Click the Create button to create the object
			//------------------------------------------------
			metadatacard.saveAndClose();
			Log.message("4. Click the Create button to create the object");

			//5. Search for the object and click on it
			//-----------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");


			metadatacard = new MetadataCard(driver, true);

			Log.message("5. Search for the object and click on it");

			//Verify if the Created by area showed the full name of the created user
			//----------------------------------------------------------------------
			if(metadatacard.getCreatedBy().equals(dataPool.get("UserFullName")))
				Log.pass("Test Case Passed. The Created by area showed the full name of the created user.");
			else
				Log.fail("Test Case Failed. The Created by area did not show the full name of the created user.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105_2_8A : Created date in metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard", "Smoke"}, 
			description = "Created date in metadatacard.")
	public void SprintTest105_2_8A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Perform the menu click to open the new object metadatacard
			//---------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));
			Utility.selectTemplate(dataPool.get("Extension"), driver);

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Perform the menu click to open the new object metadatacard.");

			//3. Set the value for the required properties
			//--------------------------------------------
			metadatacard.setInfo(dataPool.get("Properties"));
			metadatacard.setCheckInImmediately(true);

			Log.message("3. Set the value for the required properties");

			//4. Click the Create button to create the object
			//------------------------------------------------
			DateFormat dateFormat = new SimpleDateFormat("M/d/yyyy");
			Date date = new Date();
			String expectedDate = dateFormat.format(date).toString();
			metadatacard.saveAndClose();


			Log.message("4. Click the Create button to create the object");

			//5. Search for the object and open it's metadatacard
			//-----------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");



			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);


			metadatacard = new MetadataCard(driver);

			Log.message("5. Search for the object and open it's metadatacard");

			//Verify if the Created by area showed the full name of the created user
			//----------------------------------------------------------------------
			if(metadatacard.getCreatedDate().contains(expectedDate))
				Log.pass("Test Case Passed. Created date was displayed as expected.");
			else
				Log.fail("Test Case Failed. Created date did not show the expected value.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105_2_8B : 'Created date in right pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard", "Smoke"}, 
			description = "'Created date in right pane.")
	public void SprintTest105_2_8B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Perform the menu click to open the new object metadatacard
			//---------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));



			Utility.selectTemplate(dataPool.get("Extension"), driver);



			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Perform the menu click to open the new object metadatacard.");

			//3. Set the value for the required properties
			//--------------------------------------------
			metadatacard.setInfo(dataPool.get("Properties"));
			metadatacard.setCheckInImmediately(true);

			Log.message("3. Set the value for the required properties");

			//4. Click the Create button to create the object
			//------------------------------------------------
			DateFormat dateFormat = new SimpleDateFormat("M/d/yyyy");
			Date date = new Date();
			String expectedDate = dateFormat.format(date).toString();

			metadatacard.saveAndClose();


			Log.message("4. Click the Create button to create the object");

			//5. Search for the object and Click on it
			//-----------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");



			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");


			metadatacard = new MetadataCard(driver, true);

			Log.message("5. Search for the object and Click on it");

			//Verify if the Created by area showed the full name of the created user
			//----------------------------------------------------------------------
			if(metadatacard.getCreatedDate().contains(expectedDate))
				Log.pass("Test Case Passed. Created date was displayed as expected.");
			else
				Log.fail("Test Case Failed. Created date did not show the expected value.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}


}
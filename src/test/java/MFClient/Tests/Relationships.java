package MFClient.Tests;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
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
public class Relationships {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String userName = null;
	public static String password = null;
	public static String testVault = null;
	public static String userFullName = null;
	public static String configURL = null;
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
	 * 15.1.4 :  Opening Relationship link from different views 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint15", "Smoke"}, 
			description = "Opening Relationship link from different views")
	public void SprintTest15_1_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewName"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2: Click on the object
			//----------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")))  //Check for item existence
				throw new SkipException("Invalid Test data. The given object " + dataPool.get("Object") + " was not found in the view.");

			if (!homePage.listView.clickItem(dataPool.get("Object"))) //Click on the item
				throw new Exception("Object (" + dataPool.get("Object") + ") is not selected.");

			Log.message("Step-2: Object (" + dataPool.get("Object") + ") is selected.");

			//Step-3: Click the Relationships link from the operation menu
			//--------------------------------------------------
			homePage.menuBar.ClickOperationsMenu("Relationships"); //Click the relationships in operation menu


			Log.message("Step-3: Relationships link from the task pane is clicked.");

			//Verification: To verify if the Relationships view of the object is displayed with all the relations of the object
			//-------------------------------------------------------------------------------------------------------------------
			//Verifies if caption of the view is Relationships
			if(!homePage.listView.getViewCaption().equals("Relationships - " + dataPool.get("Object"))) {
				Log.fail("Test Case Failed. The relationships view of the object was not displayed.", driver);
				return;
			}

			//Verifies if url has relationships
			if(driver.getCurrentUrl().contains("relationships"))
				Log.pass("Test Case Passed. The relationships view of the object was displayed.");
			else
				Log.fail("Test Case Failed. Current URL does not relationships.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 44.5.1 : Blue background should appear behind every selected label - Existing objects
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Relationships"}, 
			description = "Blue background should appear behind every selected label - Existing objects.")
	public void SprintTest44_5_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Create an object with Relations and expand it's relations
			//-------------------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");



			if(!homePage.listView.isItemExists(dataPool.get("Object"))) //Check if the item exists
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not found.");

			homePage.listView.expandRelations(dataPool.get("Object")); //Expand the relationship of the object

			Log.message("2. Create an object with Relations and expand it's relations.");

			//3. Click on the Relationship label
			//-----------------------------------
			homePage.listView.clickItem(dataPool.get("Label")); //Click the label


			if(!homePage.listView.isItemSelected(dataPool.get("Label"))) //Check if it appears selected
				throw new Exception("The Label did not have a blue background when selected.");

			Log.message("3. Click on the Relationship label");

			//4. Click the Related object
			//----------------------------
			homePage.listView.expandRelations(dataPool.get("Label")); //Expand the label to display the related objects

			homePage.listView.clickItem(dataPool.get("RelatedObject")); //Click the related object


			if(!homePage.listView.isItemSelected(dataPool.get("RelatedObject"))) //Check if the related object is selected
				throw new Exception("The Label did not have a blue background when selected.");

			Log.message("4. Click the Related object.");

			//5. Click the label again
			//------------------------
			homePage.listView.clickItem(dataPool.get("Label")); //Click the label


			Log.message("5. Click the label again");

			//Verify if all the Expected properties appear in the Add Property Combo box
			//----------------------------------------------------------------------------
			if(homePage.listView.isItemSelected(dataPool.get("Label"))) //Check if it appears selected
				Log.pass("Test Case Passed. The label was hightlighted as expected.");
			else
				Log.fail("Test Case Failed. The Label was not highlighted.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 44.5.2 : Blue background should appear behind every selected label - New objects
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Relationships"}, 
			description = "Blue background should appear behind every selected label - New objects.")
	public void SprintTest44_5_2(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for an object and expand it's relations
			//--------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType")); //Perfrom the menu click

			Utility.selectTemplate(dataPool.get("Extension"), driver); //select the extension

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiate the metadatacard

			metadatacard.setInfo(dataPool.get("Properties")); //Set values for the necessary properties

			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value")); //Add a relation to the object

			metadatacard.setCheckInImmediately(true); //Check the check in immediatly check box

			metadatacard.clickCreateBtn(); //Click the create button

			if(!homePage.listView.isItemExists(dataPool.get("Object"))) //Check if the object is created
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not found.");

			homePage.listView.expandRelations(dataPool.get("Object")); //Expand the relation of the object

			Log.message("2. Search for an object and expand it's relations.");

			//3. Click on the Relationship label
			//-----------------------------------
			homePage.listView.clickItem(dataPool.get("Property") + "s (1)"); //Select the label

			if(!homePage.listView.isItemSelected(dataPool.get("Property") + "s (1)")) //Check if the label appears selected
				throw new Exception("The Label did not have a blue background when selected.");

			Log.message("3. Click on the Relationship label");

			//4. Click the Related object
			//----------------------------
			homePage.listView.expandRelations(dataPool.get("Property") + "s (1)"); //Expand the relation

			homePage.listView.clickItem(dataPool.get("Value")); //Click on the related object


			if(!homePage.listView.isItemSelected(dataPool.get("Value"))) //Check if it appears selected
				throw new Exception("The Label did not have a blue background when selected.");

			Log.message("4. Click the Related object.");

			//5. Click the label again
			//------------------------
			homePage.listView.clickItem(dataPool.get("Property") + "s (1)"); //Click the label


			Log.message("5. Click the label again");

			//Verify if all the Expected properties appear in the Add Property Combo box
			//----------------------------------------------------------------------------
			if(homePage.listView.isItemSelected(dataPool.get("Property") + "s (1)")) //Check if the label appears selected
				Log.pass("Test Case Passed. The label was hightlighted as expected.");
			else
				Log.fail("Test Case Failed. The Label was not highlighted.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 47.1.1A : Verify the selected object 'version' which has related objects upon performing 'checkout' operation
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Relationships"}, 
			description = "Verify the selected object 'version' which has related objects upon performing 'checkout' operation.")
	public void SprintTest47_1_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Create an object with Relations and expand it's relations
			//-------------------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");



			if(!homePage.listView.isItemExists(dataPool.get("Object"))) //Check if an item exists
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not found.");

			homePage.listView.expandRelations(dataPool.get("Object")); //expand the relations of the object


			Log.message("2. Create an object with Relations and expand it's relations.");

			//3. Click the Related object
			//----------------------------
			homePage.listView.expandRelations(dataPool.get("Label")); //Expand the label

			homePage.listView.clickItem(dataPool.get("RelatedObject")); //Click on the related object


			MetadataCard metadatacard = new MetadataCard(driver, true); //Instantiate the right pane
			int version = metadatacard.getVersion(); //fetch the version of the object
			driver.switchTo().defaultContent();


			Log.message("3. Click the Related object.");

			//4. Click the Checkout option
			//----------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.CheckOut.Value); //Click the Check out option in the operations menu


			Log.message("4. Click the label again");

			//Verify if the version was incremented by one in the right pane
			//---------------------------------------------------------------

			metadatacard = new MetadataCard(driver, true); //Instantiate the metadacard
			if(version+1 == metadatacard.getVersion()) //Check if the version is incremented
				Log.pass("Test Case Passed. The version was incremented as expected.");
			else
				Log.fail("Test Case Failed. The version was not incremented.", driver);

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 47.1.1B : Verify the selected object 'version' which has related objects upon performing 'checkout' operation
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Relationships"}, 
			description = "Verify the selected object 'version' which has related objects upon performing 'checkout' operation.")
	public void SprintTest47_1_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Create an object with Relations and expand it's relations
			//-------------------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");



			if(!homePage.listView.isItemExists(dataPool.get("Object"))) //Check the existence of an object
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not found.");

			homePage.listView.expandRelations(dataPool.get("Object")); //Expand the relations of the object


			Log.message("2. Create an object with Relations and expand it's relations.");

			//3. Click the Related object
			//----------------------------
			homePage.listView.expandRelations(dataPool.get("Label")); //expand the label

			homePage.listView.clickItem(dataPool.get("RelatedObject")); //Click the related object


			Log.message("3. Click the Related object.");

			//4. Click the Checkout option
			//----------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.CheckOut.Value); //Click Check Out option


			Log.message("4. Click the label again");

			//Check if Check in is available in task pane
			//--------------------------------------------
			if(homePage.taskPanel.isItemExists(Caption.MenuItems.CheckIn.Value)) //Check if Check in is available in task pane
				Log.pass("Test Case Passed. Check In option was available as expected.");
			else
				Log.fail("Test Case Failed. Check In option was not available.", driver);

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 47.1.4A : Arrow Icon for MFD without Relationships
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Relationships", "Smoke"}, 
			description = "Arrow Icon for MFD without Relationships.")
	public void SprintTest47_1_4A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Create a new MFD with a document
			//------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Document.Value); //perform the menu click

			Utility.selectTemplate(dataPool.get("Extension"), driver); //select the extensioon

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiate the metadatacard

			metadatacard.setInfo(dataPool.get("Properties")+dataPool.get("Object")); //Set the value for required properties


			metadatacard.setCheckInImmediately(true); //Check the Check In immediatly check box

			metadatacard.clickCreateBtn(); //Click the create button


			homePage.searchPanel.search(dataPool.get("Object")+"."+dataPool.get("Extension"), "Search only: Documents");



			if(!homePage.listView.isItemExists(dataPool.get("Object")+"."+dataPool.get("Extension"))) //Check if the object is created
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not found.");

			homePage.listView.clickItem(dataPool.get("Object")+"."+dataPool.get("Extension")); //Click the created item

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Check out the object

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value); //Click the Convert to MFD option

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value); //Check in the object


			Log.message("2. Create a new MFD with a document.");

			//Verify if all the Files only Arrow icon is displayed.
			//------------------------------------------------------


			if(homePage.listView.getArrowIcon(dataPool.get("Object")).endsWith("FilesOnlyCollapsed.png")) //Check the relationship arrow 
				Log.pass("Test Case Passed. The Files only arrow icon was displayed for an MFD with no relations.");
			else
				Log.fail("Test Case Failed. The Files only arrow icon was not displayed for an MFD with no relations.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 47.1.4B : Expanding MFD without relationships
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Relationships", "Smoke"}, 
			description = "Expanding MFD without relationships.")
	public void SprintTest47_1_4B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for the MFD with document
			//------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: Documents"); //Search for the object



			if(!homePage.listView.isItemExists(dataPool.get("Object"))) //Check if the object exists
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not found.");

			Log.message("2. Search for the MFD with document.");

			//3. Click The Arrow icon to expand and view the documents
			//---------------------------------------------------------
			homePage.listView.expandRelations(dataPool.get("Object")); //expand the relations of the object


			//Verify if all the Files of the MFD are displayed
			//-------------------------------------------------

			String[] objects = dataPool.get("Objects").split("\n");
			for(int count = 0; count < objects.length; count++) {
				if(!homePage.listView.isItemExists(objects[count])) //Check of the expected objects are listed
					Log.fail("Test Case Failed. The Expected document of the MFD was not listed.", driver);
			}

			Log.pass("Test Case Passed. The Expected Documents of the MFD were displayed.");
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 47.1.4C : Adding Relationship to a MFD
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Relationships"}, 
			description = "Adding Relationship to a MFD.")
	public void SprintTest47_1_4C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for the MFD with document
			//------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: Documents");



			if(!homePage.listView.isItemExists(dataPool.get("Object"))) //Check if the object exists
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not found.");

			Log.message("2. Search for the MFD with document.");

			//3. Add a relation to the MFD
			//-----------------------------
			homePage.listView.clickItem(dataPool.get("Object")); //Click the object

			MetadataCard metadatacard = new MetadataCard(driver, true); //Instantiate the right pane

			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value")); //Set the value to the proeprty


			metadatacard.clickOKBtn(driver); //Click the Save button


			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value); //Click the home option in Task pane


			homePage.searchPanel.search(dataPool.get("Object"), "Search only: Documents");


			homePage.listView.clickItem(dataPool.get("Object")); //Click the object


			Log.message("3. Add a relation to the MFD");

			//Verify if the arrow icon is changed
			//------------------------------------

			if(homePage.listView.getArrowIcon(dataPool.get("Object")).contains("FilesandRelatedObjects")) //Check the arrow icon 
				Log.pass("Test Case Passed. The arrow icon was changed as expected.");
			else
				Log.fail("Test Case Failed. The expected arrow icon was not displayed.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 47.1.4D : Expanding MFD with relationship
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Relationships"}, 
			description = "Expanding MFD with relationship.")
	public void SprintTest47_1_4D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for the MFD with document
			//------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: Documents"); //Search fro the object



			if(!homePage.listView.isItemExists(dataPool.get("Object"))) //Check if the objects exists
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not found.");

			Log.message("2. Search for the MFD with document.");

			//3. Click The Arrow icon to expand and view the documents
			//---------------------------------------------------------
			homePage.listView.expandRelations(dataPool.get("Object")); //expand the relation of the object


			//Verify if all the Files of the MFD are displayed
			//-------------------------------------------------

			String[] objects = dataPool.get("Objects").split("\n"); //Check if all the related objects are present
			for(int count = 0; count < objects.length; count++) {
				if(!homePage.listView.isItemExists(objects[count]))
					throw new Exception("The Expected document of the MFD was not listed.");
			}

			if(!homePage.listView.isItemExists(dataPool.get("Property")+"s (1)")) //Check of the label is displayed
				throw new Exception("The Relationship header("+ dataPool.get("Property")+"s (1)) was not displayed.");

			homePage.listView.expandRelations(dataPool.get("Property")+"s (1)"); //Expand the label


			if(homePage.listView.isItemExists(dataPool.get("Value"))) //Check if the value is displayed
				Log.pass("Test Case Passed. The Expected Documents and the related objects of the MFD were displayed.");
			else
				Log.fail("Test Case Failed. The Related objects of the MFD were not displayed.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 47.1.4E : Adding a relationship while object is checked out and Perfrom undo-checkout
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Relationships"}, 
			description = "Adding a relationship while object is checked out and Perfrom undo-checkout.")
	public void SprintTest47_1_4E(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for the MFD with document
			//------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: Documents"); //Search for an object



			if(!homePage.listView.isItemExists(dataPool.get("Object"))) //Check if the object exists
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not found.");

			Log.message("2. Search for the MFD with document.");

			//3. Check Out the object
			//-----------------------
			homePage.listView.clickItem(dataPool.get("Object")); //Click the object

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Check out the object


			Log.message("3. Check Out the object");

			//4. Add a relation to the MFD
			//-----------------------------

			MetadataCard metadatacard = new MetadataCard(driver, true); //Instantiate the metadatacard

			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value")); //Set the value to the property


			metadatacard.clickOKBtn(driver); //Click the Save button


			Log.message("4. Add a relation to the MFD");

			//5. Perform Undo Checkout
			//-------------------------
			homePage.listView.expandRelations(dataPool.get("Object")); //Expand the relation of the object


			if(!homePage.listView.isItemExists(dataPool.get("Property")+"s (1)")) //Check if the label exists
				throw new Exception("The related object header was not displayed.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Perform Undo checkout
			MFilesDialog mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.confirmUndoCheckOut(true);


			Log.message("5. Perform Undo Checkout");

			//Verify if the arrow icon is changed
			//------------------------------------

			if(!homePage.listView.isItemExists(dataPool.get("Property")+"s (1)")) //Check if the label is removed
				Log.pass("Test Case Passed. The relation was removed upon undo checkout.");
			else
				Log.fail("Test Case Failed. The relation was not removed upon undo checkout.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 47.1.5A : Relationship arrow in History view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Relationships"}, 
			description = "Relationship arrow in History view.")
	public void SprintTest47_1_5A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Login to the vault


			Log.message("1. Logged into the Home View.");

			//2. Search for an object
			//------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: Documents"); //Search for the object



			if(!homePage.listView.isItemExists(dataPool.get("Object"))) //Check if the item exists
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not found.");

			Log.message("2. Search for an object.");

			//3. Navigate to the History View of the object
			//----------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object")); //Click the object

			homePage.taskPanel.clickItem(Caption.MenuItems.History.Value); //Click the History option


			Log.message("3. Navigate to the History View of the object.");

			//Verify if the relationship arrow icon is displayed
			//--------------------------------------------------

			if(homePage.listView.getArrowIcon(dataPool.get("Object")).contains("RelatedObjectsCollapsed")) //Check the relationship arrow 
				Log.pass("Test Case Passed. The relationship arrow icon was displayed as expected.");
			else
				Log.fail("Test Case Failed. The expected relationship arrow was not displayed.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 47.1.5B : Check out object in relationship tree
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Relationships","Bug"}, 
			description = "Check out object in relationship tree.")
	public void SprintTest47_1_5B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: Documents");



			if(!homePage.listView.isItemExists(dataPool.get("Object"))) //Check if item exists
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not found.");

			Log.message("2. Search for an object.");

			//3. Navigate to the History View of the object
			//----------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object")); //Click on the object

			homePage.taskPanel.clickItem(Caption.MenuItems.History.Value); //Click the history icon in the task pane


			int expectedVersion = Integer.parseInt(homePage.listView.getColumnValueByItemName(dataPool.get("Object"), "Version")); //Get the version of the object 
			int expectedCount = homePage.listView.itemCount();

			Log.message("3. Navigate to the History View of the object.");

			//4. Expand the relations till the object appears in the tree
			//------------------------------------------------------------
			homePage.listView.expandRelations(dataPool.get("Object")); //Expand the object

			homePage.listView.expandRelations(dataPool.get("Property")); //Expand the property

			homePage.listView.expandRelations(dataPool.get("Value")); //Expand the value

			homePage.listView.expandRelations(dataPool.get("Type")); //Expand the type


			Log.message("4. Expand the relations till the object appears in the tree");

			//5. Check out the object
			//------------------------
			homePage.listView.clickItemOccurence(dataPool.get("Object"), "Name", expectedCount+1); //Click the particular occurence of the object

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Perform Check out


			Log.message("5. Check out the object");

			//Verify if the Checked out version of the object appears in the history view
			//----------------------------------------------------------------------------
			if(homePage.listView.getItemOccurence(dataPool.get("Object"), "Name") != expectedCount+2) //Check of occurence has increased
				throw new Exception("The Checked out version of the object was not listed in the view.");


			if(expectedVersion+1 == Integer.parseInt(homePage.listView.getColumnValueByItemName(dataPool.get("Object"), "Version")))
				Log.pass("Test Case Passed. The Object was checked out as expected.");
			else
				Log.fail("Test Case Failed. The Checked out version of the object did not have the expected version.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 47.1.5C : Undo Check out the object in the relationship tree
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Relationships", "Bug"}, 
			description = "Undo Check out the object in the relationship tree.")
	public void SprintTest47_1_5C(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: Documents"); //Search for the object



			if(!homePage.listView.isItemExists(dataPool.get("Object"))) //Check for the existence of the object
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not found.");

			Log.message("2. Search for an object.");

			//3. Navigate to the History View of the object
			//----------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object")); //Click the object


			if(homePage.taskPanel.isItemExists(Caption.MenuItems.CheckOut.Value))//Check if the object is not already checked out
				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Check out the object


			homePage = new HomePage(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.History.Value); //Click the history option


			int expectedVersion = Integer.parseInt(homePage.listView.getColumnValueByItemName(dataPool.get("Object"), "Version")); //Fetch the latest version
			int expectedCount = homePage.listView.itemCount();

			Log.message("3. Navigate to the History View of the object.");

			//4. Expand the relations till the object appears in the tree
			//------------------------------------------------------------
			homePage.listView.expandRelationsByIndex(expectedCount-1); //Expand the oldest version

			homePage.listView.expandRelations(dataPool.get("Property")); //Expand the property

			homePage.listView.expandRelations(dataPool.get("Value")); //Expand the Value

			homePage.listView.expandRelations(dataPool.get("Type")); //Expand the Type


			Log.message("4. Expand the relations till the object appears in the tree");

			//5. Check out the object
			//------------------------

			homePage.listView.clickItemOccurence(dataPool.get("Object"), "Name", expectedCount+1); //Click the particular occurence of the object
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //perform Undo Check out

			MFilesDialog mFilesDialog = new MFilesDialog(driver); //Instantiate MFilesDialog  
			mFilesDialog.confirmUndoCheckOut(true); //Confirmt he undo-checkout operation


			Log.message("5. Check out the object");

			//Verify if the Checked out version of the object appears in the history view
			//----------------------------------------------------------------------------

			homePage = new HomePage(driver);
			if(homePage.listView.getItemOccurence(dataPool.get("Object"), "Name") != 2) //Check the item occurence
				throw new Exception("The Checked out version of the object was not listed in the view.");


			if(expectedVersion-1 == Integer.parseInt(homePage.listView.getColumnValueByItemName(dataPool.get("Object"), "Version")))
				Log.pass("Test Case Passed. Undo Checkout worked as expected.");
			else
				Log.fail("Test Case Failed. Undo Checkout did not work as expected.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.24A : Add relationship to an object (metadatacard) - relationship tree verification
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Relationships"}, 
			description = "Add relationship to an object (metadatacard) - relationship tree verification.")
	public void SprintTest58_2_24A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")); //Search for an object



			if(!homePage.listView.isItemExists(dataPool.get("Object"))) //Check if an item exists
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not found.");

			int expected = homePage.listView.getItemOccurence(dataPool.get("Value"), "Name");  //fetch the number of occurences of the item

			Log.message("2. Search for an object.");

			//3. Open the metadatacard
			//-------------------------
			homePage.listView.clickItem(dataPool.get("Object")); //Click on the object

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Click the Properties option in the Task pane

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("3. Open the metadatacard");

			//4. Add the relationship
			//------------------------
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value")); //Set the value to the property
			metadatacard.saveAndClose(); //Click the save button


			Log.message("4. Add the relationship");

			//5. Expand the relationship of the object
			//-----------------------------------------
			homePage.listView.expandRelations(dataPool.get("Object")); //Expand the relations of the object

			if(!homePage.listView.isItemExists(dataPool.get("Property")+"s (1)")) //Check the existence of the label
				throw new Exception("The Relationship header("+dataPool.get("Property")+"s (1)) was not found.");

			homePage.listView.expandRelations(dataPool.get("Property")+"s (1)"); //Expand the label


			Log.message("5. Expand the relationship of the object.");

			//Verification: To Verify if the object is added to the object
			//-------------------------------------------------------------
			if(homePage.listView.isItemExists(dataPool.get("Value")) && homePage.listView.getItemOccurence(dataPool.get("Value"), "Name") == expected+1) 
				Log.pass("Test Case Passed. The relationship was added as expected.");
			else
				Log.fail("Test Case Failed. Adding relationhsip through metadatacard was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.24B : Add relationship to an object (sidepane) - relationship tree verification
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Relationships"}, 
			description = "Add relationship to an object (sidepane) - relationship tree verification.")
	public void SprintTest58_2_24B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")); //Search for the object

			if(!homePage.listView.isItemExists(dataPool.get("Object"))) // Check the existence of the object
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not found.");

			int expected = homePage.listView.getItemOccurence(dataPool.get("Value"), "Name"); //get the occurence of the object 

			Log.message("2. Search for an object.");

			//3. Open the metadatacard
			//-------------------------
			homePage.listView.clickItem(dataPool.get("Object")); //Click on the object


			MetadataCard metadatacard = new MetadataCard(driver, true); //Instantiate the right pane

			Log.message("3. Open the metadatacard");

			//4. Add the relationship
			//------------------------
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value")); //Set the value to the property


			metadatacard.saveAndClose(); //Click the save button


			Log.message("4. Add the relationship");

			//5. Expand the relationship of the object
			//-----------------------------------------
			homePage.listView.expandRelations(dataPool.get("Object")); //Expand the relations of the object


			if(!homePage.listView.isItemExists(dataPool.get("Property")+"s (1)")) //Check if the label exists
				throw new Exception("The Relationship header(" + dataPool.get("Property")+"s (1)) was not found.");

			homePage.listView.expandRelations(dataPool.get("Property")+"s (1)"); //Expand the label


			Log.message("5. Expand the relationship of the object.");

			//Verification: To Verify if the object is added to the object
			//-------------------------------------------------------------
			if(homePage.listView.isItemExists(dataPool.get("Value")) && homePage.listView.getItemOccurence(dataPool.get("Value"), "Name") == expected+1) 
				Log.pass("Test Case Passed. The relationship was added as expected.");
			else
				Log.fail("Test Case Failed. Adding relationhsip through metadatacard was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.25A : Add relationship to an object (metadatacard) - relationship view verification
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Relationships"}, 
			description = "Add relationship to an object (metadatacard) - relationship view verification.")
	public void SprintTest58_2_25A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")); //Search for the object


			if(!homePage.listView.clickItem(dataPool.get("Object"))) //Check if the object exists and click it
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not found.");

			Log.message("2. Search for an object.");

			//3. Open the metadatacard
			//-------------------------

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Click the properties option

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiate the metadatacard

			Log.message("3. Open the metadatacard");

			//4. Add the relationship
			//------------------------
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value")); //Set the value to the property


			metadatacard.saveAndClose(); //Click the Save Button


			Log.message("4. Add the relationship");

			//5. Open the Relationship view of the object
			//--------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Relationships.Value); //Click the relationships option from the operation menu


			Log.message("5. Open the Relationship view of the object.");

			//Verification: To Verify if the object is added to the object
			//-------------------------------------------------------------
			if(homePage.listView.isItemExists(dataPool.get("Value"))) 
				Log.pass("Test Case Passed. The relationship was added as expected.");
			else
				Log.fail("Test Case Failed. Adding relationhsip through metadatacard was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.25B : Add relationship to an object (metadatacard) - relationship view verification
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Relationships"}, 
			description = "Add relationship to an object (metadatacard) - relationship view verification.")
	public void SprintTest58_2_25B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")); //Search for an object


			if(!homePage.listView.clickItem(dataPool.get("Object"))) //Check if it exists and click on it
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not found.");

			Log.message("2. Search for an object.");

			//3. Open the metadatacard
			//-------------------------

			MetadataCard metadatacard = new MetadataCard(driver, true); //Instantiate the metadatacard

			Log.message("3. Open the metadatacard");

			//4. Add the relationship
			//------------------------
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value")); //Set the value to the property


			metadatacard.saveAndClose(); //Click the Save button


			Log.message("4. Add the relationship");

			//5. Open the Relationship view of the object
			//--------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Relationships.Value); //Click the relationships option in the operation menu


			Log.message("5. Open the Relationship view of the object.");

			//Verification: To Verify if the object is added to the object
			//-------------------------------------------------------------
			if(homePage.listView.isItemExists(dataPool.get("Value"))) 
				Log.pass("Test Case Passed. The relationship was added as expected.");
			else
				Log.fail("Test Case Failed. Adding relationhsip through metadatacard was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.26A : Remove relationship to an object (metadatacard) - relationship view verification
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Relationships"}, 
			description = "Remove relationship to an object (metadatacard) - relationship view verification.")
	public void SprintTest58_2_26A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")); //Search for an object


			if(!homePage.listView.clickItem(dataPool.get("Object"))) //Check if the item exists and click on it
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not found.");

			Log.message("2. Search for an object.");

			//3. Open the metadatacard
			//-------------------------

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Click the properties option in the taskpane

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiate the metadatacard

			Log.message("3. Open the metadatacard");

			//4. Remove the relationship
			//------------------------
			String value = metadatacard.getPropertyValue(dataPool.get("Property")); //Check if the object has relation
			if(value.equals(""))
				throw new SkipException("Invalid Test data. The object did not have the expected relation.");

			metadatacard.removeProperty(dataPool.get("Property")); //Remove the relation
			metadatacard.saveAndClose(); //Click the Save button

			Log.message("4. Remove the relationship");

			//5. Open the Relationship view of the object
			//--------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Relationships.Value); //Click the Relationships option 


			Log.message("5. Open the Relationship view of the object.");

			//Verification: To Verify if the object is removed from the relationship
			//-----------------------------------------------------------------------
			if(!homePage.listView.isItemExists(value))  
				Log.pass("Test Case Passed. The relationship was removed as expected.");
			else
				Log.fail("Test Case Failed. Removing relationhsip through metadatacard was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.26B : Remove relationship to an object (sidepane) - relationship view verification
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Relationships"}, 
			description = "Remove relationship to an object (sidepane) - relationship view verification.")
	public void SprintTest58_2_26B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")); //Search for an object


			if(!homePage.listView.clickItem(dataPool.get("Object"))) //Check if the object exists and click it
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not found.");

			Log.message("2. Search for an object.");

			//3. Open the metadatacard
			//-------------------------

			MetadataCard metadatacard = new MetadataCard(driver, true); //Instantiate the metadatacard

			Log.message("3. Open the metadatacard");

			//4. Remove the relationship
			//------------------------
			String value = metadatacard.getPropertyValue(dataPool.get("Property")); //Check if the object has relationship
			if(value.equals(""))
				throw new SkipException("Invalid Test data. The object did not have the expected relation.");

			metadatacard.removeProperty(dataPool.get("Property")); //Remove the relation


			metadatacard.saveAndClose(); //Click the Save button


			Log.message("4. Remove the relationship");

			//5. Open the Relationship view of the object
			//--------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Relationships.Value); //Click the Relationsips option


			Log.message("5. Open the Relationship view of the object.");

			//Verification: To Verify if the object is removed from the relation
			//-------------------------------------------------------------------
			if(!homePage.listView.isItemExists(value))   
				Log.pass("Test Case Passed. The relationship was removed as expected.");
			else
				Log.fail("Test Case Failed. Removing relationhsip through metadatacard was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.27A : Remove relationship to an object (metadatacard) - relationship tree verification
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Relationships"}, 
			description = "Remove relationship to an object (metadatacard) - relationship tree verification.")
	public void SprintTest58_2_27A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType"));//Search for an object



			if(!homePage.listView.clickItem(dataPool.get("Object"))) //Check if the object exists and click on it
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not found.");

			Log.message("2. Search for an object.");

			//3. Open the metadatacard
			//-------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Click the properties option

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiate the metadatacard

			Log.message("3. Open the metadatacard");

			//4. Remove the relationship
			//------------------------
			String value = metadatacard.getPropertyValue(dataPool.get("Property")); //Check if the Object has relation 
			if(value.equals(""))
				throw new SkipException("Invalid Test data. The object did not have the expected relation.");

			metadatacard.removeProperty(dataPool.get("Property")); //Remove the relationship
			metadatacard.saveAndClose(); //Click the Save button

			Log.message("4. Remove the relationship");

			//Verification: To Verify if the relationship arrow is not displayed
			//-------------------------------------------------------------------
			if(homePage.listView.getArrowIcon(dataPool.get("Object")).equals(""))
				Log.pass("Test Case Passed. The relationship was removed as expected.");
			else
				Log.fail("Test Case Failed. Removing relationhsip through metadatacard was not successful.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.27B : Remove relationship to an object (sidepane) - relationship tree verification
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Relationships"}, 
			description = "Remove relationship to an object (sidepane) - relationship tree verification.")
	public void SprintTest58_2_27B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")); //Search for an object



			if(!homePage.listView.isItemExists(dataPool.get("Object"))) //Check for the object's existence
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not found.");

			Log.message("2. Search for an object.");

			//3. Open the metadatacard
			//-------------------------
			homePage.listView.clickItem(dataPool.get("Object")); //Click the object



			MetadataCard metadatacard = new MetadataCard(driver, true); //instantiate the right pane

			Log.message("3. Open the metadatacard");

			//4. Remove the relationship
			//------------------------
			String value = metadatacard.getPropertyValue(dataPool.get("Property")); //Check if the object has relations

			if(value.equals(""))
				throw new SkipException("Invalid Test data. The object did not have the expected relation.");

			//metadatacard.removeProperty(dataPool.get("Property")); //Remove the relationship from the object
			metadatacard.removePropertyValue(dataPool.get("Property"));//Remove the relationship from the object


			metadatacard.saveAndClose(); //Click the Save button


			Log.message("4. Remove the relationship");

			//Verification: To Verify if the relationship arrow is not displayed
			//-------------------------------------------------------------------
			if(homePage.listView.getArrowIcon(dataPool.get("Object")).equals(""))
				Log.pass("Test Case Passed. The relationship was removed as expected.");
			else
				Log.fail("Test Case Failed. Removing relationhsip through metadatacard was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.28A : Add relationship to a checkedout object (metadatacard) and perform undo checkout - relationship tree verification
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Relationships"}, 
			description = "Add relationship to a checkedout object (metadatacard) and perform undo checkout - relationship tree verification.")
	public void SprintTest58_2_28A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for an object and check out
			//--------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")); //Search for object



			if(!homePage.listView.clickItem(dataPool.get("Object"))) //Check if the object exists and click on it
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not found.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Checkout the object


			Log.message("2. Search for an object and check out.");

			//3. Open the metadatacard
			//-------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Click the properties option in the taskpane

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("3. Open the metadatacard");

			//4. Add the relationship
			//------------------------
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value")); //Set the value to the property


			metadatacard.saveAndClose(); //Click the save button


			Log.message("4. Add the relationship");

			//5. Perform Undo checkout
			//-------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Perform Undo-checkout
			MFilesDialog mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.confirmUndoCheckOut(true);


			Log.message("5. Perform Undo checkout");

			//Verification: To Verify if the relationship arrow is not displayed
			//-------------------------------------------------------------------
			if(homePage.listView.getArrowIcon(dataPool.get("Object")).equals("")) 
				Log.pass("Test Case Passed. The relationship was removed as expected.");
			else
				Log.fail("Test Case Failed. Removing relationhsip through undo checkout was not successful.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.28B : Add relationship to a checkedout object (sidepane) and perform undo checkout - relationship tree verification
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Relationships"}, 
			description = "Add relationship to a checkedout object (sidepane) and perform undo checkout - relationship tree verification.")
	public void SprintTest58_2_28B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for an object and checkout
			//-------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")); //Search for an object



			if(!homePage.listView.clickItem(dataPool.get("Object"))) //Click on the object
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not found.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Check out the object



			MetadataCard metadatacard = new MetadataCard(driver, true); //instantiate the right pane
			Log.message("2. Search for an object and checkout.");

			//3. Add the relationship
			//------------------------
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value")); //Set the Value to the property


			metadatacard.saveAndClose(); //Click the save button


			Log.message("3. Add the relationship.");

			//4. Perform Undo checkout
			//-------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //perform undo check out 
			MFilesDialog mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.confirmUndoCheckOut(true);//Confirmt the action


			Log.message("4. Perform Undo checkout");

			//Verification: To Verify if the relationship arrow is not displayed
			//-------------------------------------------------------------------
			if(homePage.listView.getArrowIcon(dataPool.get("Object")).equals(""))
				Log.pass("Test Case Passed. The relationship was removed as expected.");
			else
				Log.fail("Test Case Failed. Removing relationhsip through undo check out was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.29A : Adding Multiple related objects to an object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Relationships"}, 
			description = "Adding Multiple related objects to an object.")
	public void SprintTest58_2_29A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")); //Search for an object


			if(!homePage.listView.clickItem(dataPool.get("Object"))) //Click on the object
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not found.");

			Log.message("2. Search for an object.");

			//3. Open the metadatacard
			//-------------------------

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Click the properties option

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiate the metadatacard

			Log.message("3. Open the metadatacard");

			//4. Add the relationship
			//------------------------
			String[] values = dataPool.get("Values").split("\n");
			int count = 1;

			for(count = 1; count <= values.length; count++) {
				metadatacard.setPropertyValue(dataPool.get("Property"), values[count-1], count); //Set the values to the property

			}

			metadatacard.saveAndClose(); //Click the Save button


			Log.message("4. Add the relationship");

			//5. Open the Relationship view of the object
			//--------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Relationships.Value); //Click the relationships option


			Log.message("5. Open the Relationship view of the object.");

			//Verification: To Verify if the objects are related to the object
			//-----------------------------------------------------------------
			for(count = 0; count < values.length; count++) {
				if(!homePage.listView.isItemExists(values[count]))
					Log.fail("Test Case Failed. Related Object - " + values[count] + "was not listed in the relationship view.", driver);
			}

			if(count == values.length)
				Log.pass("Test Case Passed. The relationship was added as expected.");
			else
				Log.fail("Test Case Failed. Verification was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.29B : Adding Multiple related objects to an object (sidepane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Relationships"}, 
			description = "Adding Multiple related objects to an object (sidepane).")
	public void SprintTest58_2_29B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")); //Search for the object


			if(!homePage.listView.clickItem(dataPool.get("Object"))) //Click on the object
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not found.");

			Log.message("2. Search for an object.");

			//3. Open the metadatacard
			//-------------------------

			MetadataCard metadatacard = new MetadataCard(driver, true); //instantiate the right pane

			Log.message("3. Open the metadatacard");

			//4. Add the relationship
			//------------------------
			String[] values = dataPool.get("Values").split("\n");
			int count = 1;

			for(count = 1; count <= values.length; count++) {
				metadatacard.setPropertyValue(dataPool.get("Property"), values[count-1], count); //Set the values to the property

			}

			metadatacard.saveAndClose(); //Click the Save button


			Log.message("4. Add the relationship");

			//5. Open the Relationship view of the object
			//--------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Relationships.Value); //Click the relationships option


			Log.message("5. Open the Relationship view of the object.");

			//Verification: To Verify if the objects are related to the object
			//-------------------------------------------------------------
			for(count = 0; count < values.length; count++) {
				if(!homePage.listView.isItemExists(values[count]))
					Log.fail("Test Case Failed. Related Object - " + values[count] + "was not listed in the relationship view.", driver);
			}

			if(count == values.length)
				Log.pass("Test Case Passed. The relationship was added as expected.");
			else
				Log.fail("Test Case Failed. Verification was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.30A : Remove one of multiple related objects
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Relationships"}, 
			description = "Remove one of multiple related objects.")
	public void SprintTest58_2_30A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")); //Search for an object


			if(!homePage.listView.clickItem(dataPool.get("Object"))) //Click the Object
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not found.");

			Log.message("2. Search for an object.");

			//3. Open the metadatacard
			//-------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Click the properties option

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiate the metadatacard

			Log.message("3. Open the metadatacard");

			//4. Add the relationship
			//------------------------
			String[] values = dataPool.get("Values").split("\n");
			int count = 0;
			metadatacard.removePropertyValue(dataPool.get("Property"), 1); //Remove a value from the property

			metadatacard.saveAndClose(); //Click the Save button

			Log.message("4. Add the relationship");

			//5. Open the Relationship view of the object
			//--------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Relationships.Value); //Click the Relationships option

			Log.message("5. Open the Relationship view of the object.");

			//Verification: To Verify if the removed object is not related to the object
			//----------------------------------------------------------------------------
			for(count = 0; count < values.length; count++) {

				if(count == 0 && homePage.listView.isItemExists(values[count]))
					Log.fail("Test Case Failed. The removed relationship was also listed in the relationship view.", driver);

				if(count != 0 && !homePage.listView.isItemExists(values[count]))
					Log.fail("Test Case Failed. Related Object - " + values[count] + "was not listed in the relationship view.", driver);
			}

			if(count == values.length)
				Log.pass("Test Case Passed. The relationship was removed as expected.");
			else
				Log.fail("Test Case Failed. Verification was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.30B : Remove one of multiple related objects (Sidepane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Relationships"}, 
			description = "Remove one of multiple related objects (Sidepane).")
	public void SprintTest58_2_30B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")); //Search the object


			if(!homePage.listView.clickItem(dataPool.get("Object"))) //Click on the object
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not found.");

			Log.message("2. Search for an object.");

			//3. Open the metadatacard
			//-------------------------

			MetadataCard metadatacard = new MetadataCard(driver, true); //instantiate the right pane

			Log.message("3. Open the metadatacard");

			//4. Add the relationship
			//------------------------
			String[] values = dataPool.get("Values").split("\n");
			int count = 0;
			metadatacard.removePropertyValue(dataPool.get("Property"), 1);//Remove a value from the property


			metadatacard.saveAndClose(); //Click the Save button


			Log.message("4. Add the relationship");

			//5. Open the Relationship view of the object
			//--------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Relationships.Value); //Click on the relationships option


			Log.message("5. Open the Relationship view of the object.");

			//Verification: To Verify if the removed object is not related to the object
			//----------------------------------------------------------------------------
			for(count = 0; count < values.length; count++) {

				if(count == 0 && homePage.listView.isItemExists(values[count]))
					Log.fail("Test Case Failed. The removed relationship was also listed in the relationship view.", driver);

				if(count != 0 && !homePage.listView.isItemExists(values[count]))
					Log.fail("Test Case Failed. Related Object - " + values[count] + "was not listed in the relationship view.", driver);
			}

			if(count == values.length)
				Log.pass("Test Case Passed. The relationship was removed as expected.");
			else
				Log.fail("Test Case Failed. Verification was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.31A : Creating assignment while selecting an object Relationship tree (Verification)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint58", "Relationships"}, 
			description = "Creating assignment while selecting an object Relationship tree (Verification).")
	public void SprintTest58_2_31A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for and click on an object
			//--------------------------------------
			homePage.searchPanel.search(dataPool.get("ObjectName"), "Search only: "+dataPool.get("ObjectType")+"s"); //Search for an object

			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Check for existcen and click on the object
				throw new SkipException("Invalid Test data. The Object was not found in the vault.");

			Log.message("2. Search for and click on an object.");

			//3. Perform the Task pane click
			//-------------------------------
			homePage.taskPanel.clickItem(dataPool.get("NewType")); //Click the new object type option in the taskpane


			if(MFilesDialog.exists(driver, "Confirm Autofill")) {
				MFilesDialog mFilesDialog = new MFilesDialog(driver, "Confirm Autofill");
				mFilesDialog.clickCancelButton();
			}

			Log.message("3. Perform the Task pane click.");

			//3. Set the necessary info and create the object
			//------------------------------------------------

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiate the metadatacard

			metadatacard.setInfo(dataPool.get("Properties")+dataPool.get("Object")); //Set the necessary data
			metadatacard.saveAndClose(); //Click the Create button

			Log.message("4. Set the necessary info and create the object");

			//4. Search for the created assignment
			//-------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("NewType")+"s"); //Search for the new object

			if(!homePage.listView.clickItem(dataPool.get("Object"))) //Check existence and click on it
				throw new Exception("The Object was not created.");

			Log.message("5. Search for the created assignment.");

			//5. Expand it's relationship
			//----------------------------
			homePage.listView.expandRelations(dataPool.get("Object")); //Expand the object


			Log.message("6. Expand it's relationship");

			//Verification: To verify if the Assignment has the object related to it
			//-----------------------------------------------------------------------
			if(homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Check the relation
				Log.pass("Test Case Passed. The Object was related to it's assignment");
			else
				Log.fail("Test Case Failed. The object was not related to it's assignment.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.31B : Creating assignment while selecting an object (Relationship tree Verification)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint58", "Relationships"}, 
			description = "Creating assignment while selecting an object (Relationship tree Verification).")
	public void SprintTest58_2_31B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for and click on an object
			//--------------------------------------
			homePage.searchPanel.search(dataPool.get("ObjectName"), "Search only: "+dataPool.get("ObjectType")+"s"); //Search for the object

			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Check for existence and click on the object
				throw new SkipException("Invalid Test data. The Object was not found in the vault.");

			Log.message("2. Search for and click on an object.");

			//3. Perform the Task pane click
			//-------------------------------
			homePage.taskPanel.clickItem(dataPool.get("NewType")); //Click on the object type option in the task pane


			if(MFilesDialog.exists(driver, "Confirm Autofill")) { 
				MFilesDialog mFilesDialog = new MFilesDialog(driver, "Confirm Autofill");
				mFilesDialog.clickCancelButton();
			}

			Log.message("3. Perform the Task pane click.");

			//3. Set the necessary info and create the object
			//------------------------------------------------

			MetadataCard metadatacard = new MetadataCard(driver); //instantiate the metadatacard

			metadatacard.setInfo(dataPool.get("Properties")+dataPool.get("Object")); //Set the necessary data
			metadatacard.saveAndClose(); //Click the Save button

			Log.message("3. Set the necessary info and create the object");

			//5. Expand it's relationship
			//----------------------------
			homePage.listView.expandRelations(dataPool.get("ObjectName")); //expand the relations of the object


			if(!homePage.listView.isItemExists(dataPool.get("ObjectType")+"s (1)")) //Check if the item exists
				throw new Exception("The Relationship header(" +dataPool.get("ObjectType")+"s (1)) was not found.");

			homePage.listView.expandRelations(dataPool.get("ObjectType")+"s (1)"); //expand the label


			Log.message("4. Expand it's relationship");

			//Verification: To verify if the Assignment has the object related to it
			//-----------------------------------------------------------------------
			if(homePage.listView.isItemExists(dataPool.get("Object"))) //Check if the assignment is related
				Log.pass("Test Case Passed. The Assignment was related to the object.");
			else
				Log.fail("Test Case Failed. The Assignment was not related to the object.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

} //End Class Relationships
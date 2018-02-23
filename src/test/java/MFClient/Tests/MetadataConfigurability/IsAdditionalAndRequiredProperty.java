package MFClient.Tests.MetadataConfigurability;
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
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;


@Listeners(EmailReport.class)
public class IsAdditionalAndRequiredProperty {


	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String userName = null;
	public static String userFullName = null;
	public static String password = null;
	public static String testVault = null;
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
			userName = xmlParameters.getParameter("UserName");
			userFullName = xmlParameters.getParameter("UserFullName");
			password = xmlParameters.getParameter("Password");
			configURL = xmlParameters.getParameter("ConfigurationURL");
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
	 * 1.4.9.1.1A : Verify if property is Added/Not to metadatacard when isAdditional:(true/false) [Object creation via Task pane]

	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability", "IsAdditional"}, 
			description = "Verify if property is Added/Not to metadatacard when isAdditional:(true/false) [Object creation via Task pane].")
	public void TC_1_4_9_1_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials	

			//Step-1 : Click the new customer object from the taskpane
			//----------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Customer.Value);

			Log.message("1. Clicked the new 'Customer' object from the task panel.");

			//Step-2 : Instantiate the metadatacard and verify if property is added or not
			//----------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the new metadatacard

			String propertyExpected = "";

			if(!metadataCard.propertyExists(dataPool.get("PropertyName1")))//Verify if property exist in the metadata
				propertyExpected = "Property : " + dataPool.get("PropertyName1") + " not added to the new object metadatacard. ";

			if(metadataCard.propertyExists(dataPool.get("PropertyName2")))//Verify if property not exist in the metadata
				propertyExpected += "Property : " + dataPool.get("PropertyName2") + " is added to the new object metadatacard. ";

			//Checks if property group is exisit in the metadatacard [Inside the group]
			if (!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("GroupText")))//Checks if proeprty group is displayed in the metadatacard
				throw new Exception("Property Group is not displayed in the metadatacard.");

			metadataCard.expandPropertyGroup(1, true);//Expands the property group in the metadatacard

			//Checks if properties are exisit in the metadatacard [Inside the group]
			if (!metadataCard.propertyExistsInGroup(1, dataPool.get("GrpPropertyName1")))
				propertyExpected += " Property(" + dataPool.get("GrpPropertyName1") + ") is not added in the metadatacard while using the behavior isAdditional:true [Inside the group].";

			if (metadataCard.propertyExistsInGroup(1, dataPool.get("GrpPropertyName2")))
				propertyExpected += " Property(" + dataPool.get("GrpPropertyName2") + ") is displayed in the metadatacard while using the behavior isAdditional:true[Inside the group].";

			//Verification if rule is applied correcly in the metadatacard
			//------------------------------------------------------------

			if (propertyExpected.equalsIgnoreCase(""))
				Log.pass("Test case passed. Metadataconfiguration is working as expected while using rule behavior: IsAdditional: true/false [Object creation via task pane]", driver);
			else
				Log.fail("Test case failed. Metadataconfiguration is not working as expected while using rule behavior: IsAdditional: true/false [Object creation via task pane]. For more details: " + propertyExpected, driver);


		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally{
			Utility.quitDriver(driver);
		}//End finally

	}//End TC_1_4_9_1_1A

	/**
	 * 1.4.9.1.1B : Verify if property is Added/not to metadatacard When isAdditional:(true/false) [New object] rule is configured.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability"}, 
			description = "Verify if property is Added/not to metadatacard When isAdditional:(true/false) [Existing object] rule is configured.")
	public void TC_1_4_9_1_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Create the new "Customer" object from the Menuitems
			//---------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Customer.Value);

			Log.message("1. Selected the new 'Customer' object from new Menu bar.");

			String propertyExpected = " ";

			//Step-2 : Instantiate the new Metadatacard & Verify the Property is existing or not in the new metadatacard
			//----------------------------------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the new metadatacard
			if(!metadataCard.propertyExists(dataPool.get("PropertyName1")))//Verify if property exist in the metadata
				propertyExpected = "Property : " + dataPool.get("PropertyName1") + " not added to the new object metadatacard.";

			if(metadataCard.propertyExists(dataPool.get("PropertyName2")))//Verify if property not exist in the metadata
				propertyExpected = propertyExpected + "Property : " + dataPool.get("PropertyName2") + " is added to the new object metadatacard.";

			//Verify if the corresponding property is existing the new object
			//---------------------------------------------------------------
			if(propertyExpected.equals(" "))
				Log.pass("Test Case Passed. Specified property : '" +dataPool.get("PropertyName1") + " is added and Property : "+ dataPool.get("PropertyName2") + " is not added to the new object metadtacard as per the metadataconfigurability rule." , driver);
			else
				Log.fail("Test Case Failed.Property is added to new object metadatacard is not working as expected." +propertyExpected , driver);

		}//End try
		catch(Exception e){
			Log.exception(e,driver);
		}//End catch
		finally{
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_9_1_1B


	/**
	 * 1.4.9.1.1C : Verify if property is Created/not to metadatacard When isAdditional:(true/false) [new object] rule is configured.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability"}, 
			description = "Verify if property is Created/not to metadatacard When isAdditional:(true/false) [new object] rule is configured.")
	public void TC_1_4_9_1_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();
			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to any view and select the any existing 
			//---------------------------------------------------------
			homePage.searchPanel.clickSearchBtn(driver);

			Log.message("1. Navigated to any search view.");

			//Step-2 : Select the specified object type from the search view
			//--------------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectType"));

			Log.message("2. Selected the specified object (" + dataPool.get("ObjectType") +") from the search view.", driver);

			//Step-3 : Select the lookup property in the metadatacard
			//-------------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver,true);//Instantiate the new metadatacard
			if(!metadatacard.propertyExists("Customer"))//Verify if property is exists or not in metadatacard
				metadatacard.addNewProperty("Customer");//if property is not existing add the new customer property

			Log.message("3. 'Customer' Property is added to the new metadatacard", driver);

			//Step-4 : Select the existing property in metadatacard
			//-----------------------------------------------------
			metadatacard.clickAddValueButton("Customer");

			Log.message("4. Selected the Addfield property in the metadatacard");

			String propertyExpected = " ";

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the new metadatacard
			if(!metadataCard.propertyExists(dataPool.get("PropertyName1")))//Verify if property exist in the metadata
				propertyExpected = "Property : " + dataPool.get("PropertyName1") + " not added to the new object metadatacard.";

			if(metadataCard.propertyExists(dataPool.get("PropertyName2")))//Verify if property not exist in the metadata
				propertyExpected = propertyExpected + "Property : " + dataPool.get("PropertyName2") + " is added to the new object metadatacard.";

			//Verify if the corresponding property is existing the new object
			//---------------------------------------------------------------
			if(propertyExpected.equals(" "))
				Log.pass("Test Case Passed. Specified property : '" +dataPool.get("PropertyName1") + " is added and Property : "+ dataPool.get("PropertyName2") + " is not added to the existing object metadtacard as per the metadataconfigurability rule.", driver);
			else
				Log.fail("Test Case Failed.Property is added to existing object metadatacard is not working as expected." +propertyExpected , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End TC_1_4_9_1_1C


	/**
	 * 1.4.9.1.2A : Verify if property is added/not to metadatacard if rule isAdditional:(true/false) [Existing object] is set
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability"}, 
			description = "Verify if property is added/not to metadatacard if rule isAdditional:(true/false) [Existing object] is set.")
	public void TC_1_4_9_1_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to "Search only : Customers" View
			//---------------------------------------------------
			homePage.searchPanel.search(" ", Caption.Search.SearchOnlyCustomers.Value);

			Log.message("1. Navigated to " + Caption.Search.SearchOnlyCustomers.Value + " view.", driver);

			//Step-2 : Select any existing object from the search view
			//--------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectType"));

			Log.message("2. Selected the Object (" + dataPool.get("ObjectType") + ") from the specified search view.");

			//Step-3 : Select the "Properties" option from the taskpanel
			//----------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			Log.message("3. Selected the 'Properties' option from the Task panel.");

			//Step-3 : Instantiate the metadatacard & Verify if property is existing or not
			//-----------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the metadatcard

			String propertyExpected = " ";

			if(!metadataCard.propertyExists(dataPool.get("PropertyName1")))//Verify if property exist in the metadata
				propertyExpected = "Property : " + dataPool.get("PropertyName1") + " not added to the new object metadatacard.";

			if(metadataCard.propertyExists(dataPool.get("PropertyName2")))//Verify if property not exist in the metadata
				propertyExpected = propertyExpected + "Property : " + dataPool.get("PropertyName2") + " is added to the new object metadatacard.";

			//Verify if the corresponding property is existing the new object
			//---------------------------------------------------------------
			if(propertyExpected.equals(" "))
				Log.pass("Test Case Passed. Specified property : '" +dataPool.get("PropertyName1") + " is added and Property : "+ dataPool.get("PropertyName2") + " is not added to the existing object metadtacard as per the metadataconfigurability rule.", driver);
			else
				Log.fail("Test Case Failed.Property is added to existing object metadatacard is not working as expected." +propertyExpected , driver);

		}//end try
		catch (Exception e){
			Log.exception(e, driver);
		}//end catch
		finally {
			Utility.quitDriver(driver);
		}//end finally
	}//End TC_1_4_9_1_2A


	/**
	 * 1.4.9.1.2B : Verify if property is added/not to metadatacard if rule isAdditional:(true/false) [Existing object popout from taskpane] is set
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability"}, 
			description = "Verify if property is added/not to metadatacard if rule isAdditional:(true/false) [Existing object] is set.")
	public void TC_1_4_9_1_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to "Search only : Customers" View
			//---------------------------------------------------
			homePage.searchPanel.search(" ", Caption.Search.SearchOnlyCustomers.Value);

			Log.message("1. Navigated to " + Caption.Search.SearchOnlyCustomers.Value + " view.", driver);

			//Step-2 : Select any existing object from the search view
			//--------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectType"));

			Log.message("2. Selected the Object (" + dataPool.get("ObjectType") + ") from the specified search view.");

			//Step-3 : Select the "Properties" option from the taskpanel
			//----------------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value);

			Log.message("3. Selected the 'Properties' option from the Operations menu.");

			//Step-3 : Instantiate the metadatacard & Verify if property is existing or not
			//-----------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the metadatcard

			String propertyExpected = " ";

			if(!metadataCard.propertyExists(dataPool.get("PropertyName1")))//Verify if property exist in the metadata
				propertyExpected = "Property : " + dataPool.get("PropertyName1") + " not added to the new object metadatacard.";

			if(metadataCard.propertyExists(dataPool.get("PropertyName2")))//Verify if property not exist in the metadata
				propertyExpected = propertyExpected + "Property : " + dataPool.get("PropertyName2") + " is added to the new object metadatacard.";

			//Verify if the corresponding property is existing the new object
			//---------------------------------------------------------------
			if(propertyExpected.equals(" "))
				Log.pass("Test Case Passed. Specified property : '" +dataPool.get("PropertyName1") + " is added and Property : "+ dataPool.get("PropertyName2") + " is not added to the existing object metadtacard as per the metadataconfigurability rule.", driver);
			else
				Log.fail("Test Case Failed.Property is added to existing object metadatacard is not working as expected." +propertyExpected , driver);

		}//end try
		catch (Exception e){
			Log.exception(e, driver);
		}//end catch
		finally {
			Utility.quitDriver(driver);
		}//end finally
	}//End TC_1_4_9_1_2B

	/**
	 * 1.4.9.1.3A : Verify if property is added/not to metadatacard if rule isAdditional:(true/false) [Multiple Existing objects metadata card] is set
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "MFN119MetadataConfigurability"}, 
			description = "Verify if property is added/not to metadatacard if rule isAdditional:(true/false) [Multiple Existing objects metadata card] is set.")
	public void TC_1_4_9_1_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to "Search only : Customers" View
			//---------------------------------------------------
			homePage.searchPanel.search(" ", Caption.Search.SearchOnlyCustomers.Value);

			Log.message("1. Navigated to " + Caption.Search.SearchOnlyCustomers.Value + " view.", driver);

			//Step-2 : Select the multiple objects from the search view 
			//---------------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));

			Log.message("2. Selected the multiple objects from the 'Search only:Customers' view.");

			//Step-3 : Instantiate the right pane metadatacard and verify the property is exists or not 
			//-----------------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver,true);//Instantiate the metadatcard

			String propertyExpected = " ";

			if(!metadataCard.propertyExists(dataPool.get("PropertyName1")))//Verify if property exist in the metadata
				propertyExpected = "Property : " + dataPool.get("PropertyName1") + " not added to the new object metadatacard.";

			if(metadataCard.propertyExists(dataPool.get("PropertyName2")))//Verify if property not exist in the metadata
				propertyExpected = propertyExpected + "Property : " + dataPool.get("PropertyName2") + " is added to the new object metadatacard.";

			//Verify if the corresponding property is existing the new object
			//---------------------------------------------------------------
			if(propertyExpected.equals(" "))
				Log.pass("Test Case Passed. Specified property : '" +dataPool.get("PropertyName1") + " is added and Property : "+ dataPool.get("PropertyName2") + " is not added to the multiple existing objects metadtacard as per the metadataconfigurability rule.", driver);
			else
				Log.fail("Test Case Failed.Property is added to existing object metadatacard is not working as expected." +propertyExpected , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//end catch
		finally {
			Utility.quitDriver(driver);
		}//end finally
	}//end TC_1_4_9_1_3A

	/**
	 * 1.4.9.1.3B : Verify if property is added/not to metadatacard if rule isAdditional:(true/false) [Multiple Existing objects popout metadata card] is set
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "MFN119MetadataConfigurability"}, 
			description = "Verify if property is added/not to metadatacard if rule isAdditional:(true/false) [Multiple Existing objects popout metadata card] is set.")
	public void TC_1_4_9_1_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to "Search only : Customers" View
			//---------------------------------------------------
			homePage.searchPanel.search(" ", Caption.Search.SearchOnlyCustomers.Value);

			Log.message("1. Navigated to " + Caption.Search.SearchOnlyCustomers.Value + " view.", driver);

			//Step-2 : Select the multiple objects from the search view 
			//---------------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));

			Log.message("2. Selected the multiple objects from the 'Search only:Customers' view.", driver);

			//Step-3 : Instantiate the right pane metadatacard and verify the property is exists or not 
			//-----------------------------------------------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver,true);//Instantiate the metadatcard
			metadatacard.popOutMetadatacard();

			Log.message("3. Popout metadatcard is opened from the settings menu in right pane metadatacard.", driver);

			String propertyExpected = " ";

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the popout metadatacard
			if(!metadataCard.propertyExists(dataPool.get("PropertyName1")))//Verify if property exist in the metadata
				propertyExpected = "Property : " + dataPool.get("PropertyName1") + " not added to the new object metadatacard.";

			if(metadataCard.propertyExists(dataPool.get("PropertyName2")))//Verify if property not exist in the metadata
				propertyExpected = propertyExpected + "Property : " + dataPool.get("PropertyName2") + " is added to the new object metadatacard.";

			//Verify if the corresponding property is existing the new object
			//---------------------------------------------------------------
			if(propertyExpected.equals(" "))
				Log.pass("Test Case Passed. Specified property : '" +dataPool.get("PropertyName1") + " is added and Property : "+ dataPool.get("PropertyName2") + " is not added to the multiple existing objects metadtacard as per the metadataconfigurability rule.", driver);
			else
				Log.fail("Test Case Failed.Property is added to existing object metadatacard is not working as expected." +propertyExpected , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//end catch
		finally {
			Utility.quitDriver(driver);
		}//end finally
	}//end TC_1_4_9_1_3B

	/**
	 * 1.4.10.1.1A : Verify if property is exists or not when set the rule isHidden:(true/false) [Object creation via New taskpane]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability"}, 
			description = "Verify if property is exists or not when set the rule isHidden:(true/false) [Object creation via task pane].")
	public void TC_1_4_10_1_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Create the new customer object from the task pane
			//----------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Customer.Value);

			Log.message("1. Clicked the new Customer object from the Task panel.");

			String propertyExpected = " ";

			//Step-2 : Instantiate the metadatacard & Verify if property exists or not
			//------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the popout metadatacard
			if(!metadataCard.propertyExists(dataPool.get("PropertyName1")))//Verify if property exist in the metadata
				propertyExpected = "Property : " + dataPool.get("PropertyName1") + " not added to the new object metadatacard.";

			if(metadataCard.propertyExists(dataPool.get("PropertyName2")))//Verify if property not exist in the metadata
				propertyExpected = propertyExpected + "Property : " + dataPool.get("PropertyName2") + " is added to the new object metadatacard.";

			//Verify if the corresponding property is existing the new object
			//---------------------------------------------------------------
			if(propertyExpected.equals(" "))
				Log.pass("Test Case Passed. Specified property : '" +dataPool.get("PropertyName1") + " is added and Property : "+ dataPool.get("PropertyName2") + " is not added to the new object metadtacard as per the metadataconfigurability rule.",  driver);
			else
				Log.fail("Test Case Failed.Property is added to existing object metadatacard is not working as expected." +propertyExpected , driver);


		}//End try

		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_10_1_1B

	/**
	 * 1.4.10.1.1B : Verify if property is exists or not when set the rule isHidden:(true/false) [Object creation via New item menu]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability"}, 
			description = "Verify if property is exists or not when set the rule isHidden:(true/false) [Object creation via New item menu].")
	public void TC_1_4_10_1_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Create the new customer object from the task pane
			//----------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Customer.Value);//Clicks the new item from new item menu

			Log.message("1. Clicked the new Customer object from the new item menu.");

			String propertyExpected = " ";

			//Step-2 : Instantiate the metadatacard & Verify if property exists or not
			//------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the popout metadatacard
			if(!metadataCard.propertyExists(dataPool.get("PropertyName1")))//Verify if property exist in the metadata
				propertyExpected = "Property : " + dataPool.get("PropertyName1") + " not added to the new object metadatacard.";

			if(metadataCard.propertyExists(dataPool.get("PropertyName2")))//Verify if property not exist in the metadata
				propertyExpected = propertyExpected + "Property : " + dataPool.get("PropertyName2") + " is added to the new object metadatacard.";

			//Verify if the corresponding property is existing the new object
			//---------------------------------------------------------------
			if(propertyExpected.equals(" "))
				Log.pass("Test Case Passed. Specified property : '" +dataPool.get("PropertyName1") + " is added and Property : "+ dataPool.get("PropertyName2") + " is not added to the new object metadtacard as per the metadataconfigurability rule.",  driver);
			else
				Log.fail("Test Case Failed.Property is added to existing object metadatacard is not working as expected." +propertyExpected , driver);


		}//End try

		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_10_1_1B

	/**
	 * 1.4.10.1.1C : Verify if property is exists or not when set the rule  isHidden:(true/false) [Object creation via Object Property]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability"}, 
			description = "Verify if property is exists or not when set the rule  isHidden:(true/false) [Object creation via Object Property].")
	public void TC_1_4_10_1_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to any view and select the any existing 
			//---------------------------------------------------------
			homePage.searchPanel.clickSearchBtn(driver);

			Log.message("1. Navigate to any search view.");

			//Step-2 : Select the specified object type from the search view
			//--------------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectType"));

			Log.message("2. Selected the specified object (" + dataPool.get("ObjectType") +") from the search view.", driver);

			//Step-3 : Select the lookup property in the metadatacard
			//-------------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver,true);//Instantiate the new metadatacard
			if(!metadatacard.propertyExists("Customer"))//Verify if property is exists or not in metadatacard
				metadatacard.addNewProperty("Customer");//if property is not existing add the new customer property

			Log.message("3. 'Customer' Property is added to the new metadatacard", driver);

			//Step-4 : Select the existing property in metadatacard
			//-----------------------------------------------------
			metadatacard.clickAddValueButton("Customer");

			Log.message("4. Selected the Addfield property in the metadatacard");

			String propertyExpected = " ";

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the new metadatacard
			if(!metadataCard.propertyExists(dataPool.get("PropertyName1")))//Verify if property exist in the metadata
				propertyExpected = "Property : " + dataPool.get("PropertyName1") + " not added to the new object metadatacard.";

			if(metadataCard.propertyExists(dataPool.get("PropertyName2")))//Verify if property not exist in the metadata
				propertyExpected = propertyExpected + "Property : " + dataPool.get("PropertyName2") + " is added to the new object metadatacard.";

			//Verify if the corresponding property is existing the new object
			//---------------------------------------------------------------
			if(propertyExpected.equals(" "))
				Log.pass("Test Case Passed. Specified property : '" +dataPool.get("PropertyName1") + " is not hidden and Property : "+ dataPool.get("PropertyName2") + " is not hidden to the existing object metadtacard as per the metadataconfigurability rule.", driver);
			else
				Log.fail("Test Case Failed.Property is added to existing object metadatacard is not working as expected." +propertyExpected , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally{
			Utility.quitDriver(driver);
		}//End finally
	}//TC_1_4_10_1_1C


	/**
	 * 1.4.10.1.2A : Verify if property is exists or not when set the rule  isHidden:(true/false) [Object creation via Object Property]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability"}, 
			description = "Verify if property is exists or not when set the rule  isHidden:(true/false) [Object creation via Object Property].")
	public void TC_1_4_10_1_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to "Search only : Customers" View
			//---------------------------------------------------
			homePage.searchPanel.search(dataPool.get("ObjectType"), Caption.Search.SearchOnlyCustomers.Value);

			Log.message("1. Navigate to " + Caption.Search.SearchOnlyCustomers.Value + " view.", driver);

			//Step-2 : Select any existing object from the search view
			//--------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectType"));

			Log.message("2. Selected the Object (" + dataPool.get("ObjectType") + ") from the specified search view.");

			//Step-3 : Select the "Properties" option from the taskpanel
			//----------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			Log.message("3. Selected the 'Properties' option from the Task panel.");

			//Step-3 : Instantiate the metadatacard & Verify if property is existing or not
			//-----------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the metadatcard

			String propertyExpected = " ";

			if(!metadataCard.propertyExists(dataPool.get("PropertyName1")))//Verify if property exist in the metadata
				propertyExpected = "Property : " + dataPool.get("PropertyName1") + " not added to the new object metadatacard.";

			if(metadataCard.propertyExists(dataPool.get("PropertyName2")))//Verify if property not exist in the metadata
				propertyExpected = propertyExpected + "Property : " + dataPool.get("PropertyName2") + " is added to the new object metadatacard.";

			//Verify if the corresponding property is existing the new object
			//---------------------------------------------------------------
			if(propertyExpected.equals(" "))
				Log.pass("Test Case Passed. Specified property : '" +dataPool.get("PropertyName1") + " is not hidden and Property : "+ dataPool.get("PropertyName2") + " is not hidde to the existing object metadtacard as per the metadataconfigurability rule.", driver);
			else
				Log.fail("Test Case Failed.Property is added to existing object metadatacard is not working as expected." +propertyExpected , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally{
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_10_1_2A



	/**
	 * 1.4.10.1.2B : Verify if property is exists or not when set the rule isHidden:(true/false) [Existing object popout metadata card]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability"}, 
			description = "Verify if property is exists or not when set the rule isHidden:(true/false) [Existing object popout metadata card].")
	public void TC_1_4_10_1_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to "Search only : Customers" View
			//---------------------------------------------------
			homePage.searchPanel.search(" ", Caption.Search.SearchOnlyCustomers.Value);

			Log.message("1. Navigate to " + Caption.Search.SearchOnlyCustomers.Value + " view.", driver);

			//Step-2 : Select any existing object from the search view
			//--------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectType"));

			Log.message("2. Selected the Object (" + dataPool.get("ObjectType") + ") from the specified search view.");

			//Step-3 : Select the "Properties" option from the taskpanel
			//----------------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value);

			Log.message("3. Selected the 'Properties' option from the Operations menu.");

			//Step-3 : Instantiate the metadatacard & Verify if property is existing or not
			//-----------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the metadatcard

			String propertyExpected = " ";

			if(!metadataCard.propertyExists(dataPool.get("PropertyName1")))//Verify if property exist in the metadata
				propertyExpected = "Property : " + dataPool.get("PropertyName1") + " not added to the new object metadatacard.";

			if(metadataCard.propertyExists(dataPool.get("PropertyName2")))//Verify if property not exist in the metadata
				propertyExpected = propertyExpected + "Property : " + dataPool.get("PropertyName2") + " is added to the new object metadatacard.";

			//Verify if the corresponding property is existing the new object
			//---------------------------------------------------------------
			if(propertyExpected.equals(" "))
				Log.pass("Test Case Passed. Specified property : '" +dataPool.get("PropertyName1") + " is not hidden and Property : "+ dataPool.get("PropertyName2") + " is not hidden to the existing object metadtacard as per the metadataconfigurability rule.", driver);
			else
				Log.fail("Test Case Failed.Property is added to existing object metadatacard is not working as expected." +propertyExpected , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End TC_1_4_10_1_2B



	/**
	 * 1.4.10.1.3A : Verify if property is exists or not when set the rule isHidden:(true/false) [Multiple Existing objects metadata card]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "MFN119MetadataConfigurability"}, 
			description = "Verify if property is exists or not when set the rule isHidden:(true/false) [Multiple Existing objects metadata card].")
	public void TC_1_4_10_1_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to "Search only : Customers" View
			//---------------------------------------------------
			homePage.searchPanel.search(" ", Caption.Search.SearchOnlyCustomers.Value);

			Log.message("1. Navigate to " + Caption.Search.SearchOnlyCustomers.Value + " view.", driver);

			//Step-2 : Select the multiple objects from the search view 
			//---------------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));

			Log.message("2. Selected the multiple objects from the 'Search only:Customers' view.", driver);

			//Step-3 : Instantiate the right pane metadatacard and verify the property is exists or not 
			//-----------------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver,true);//Instantiate the metadatcard

			String propertyExpected = " ";

			if(!metadataCard.propertyExists(dataPool.get("PropertyName1")))//Verify if property exist in the metadata
				propertyExpected = "Property : " + dataPool.get("PropertyName1") + " not added to the new object metadatacard.";

			if(metadataCard.propertyExists(dataPool.get("PropertyName2")))//Verify if property not exist in the metadata
				propertyExpected = propertyExpected + "Property : " + dataPool.get("PropertyName2") + " is added to the new object metadatacard.";

			//Verify if the corresponding property is existing the new object
			//---------------------------------------------------------------
			if(propertyExpected.equals(" "))
				Log.pass("Test Case Passed. Specified property : '" +dataPool.get("PropertyName1") + " is not hidden and Property : "+ dataPool.get("PropertyName2") + " is hidden to the multiple existing objects metadtacard as per the metadataconfigurability rule.");
			else
				Log.fail("Test Case Failed.Property is added to existing object metadatacard is not working as expected." +propertyExpected , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//end catch
		finally {
			Utility.quitDriver(driver);
		}//end finally
	}//end TC_1_4_10_1_3A


	/**
	 * 1.4.9.1.3B : Verify if property is exists or not when set the rule isHidden:(true/false) [Multiple Existing objects popout metadata card]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "MFN119MetadataConfigurability"}, 
			description = "Verify if property is exists or not when set the rule isHidden:(true/false) [Multiple Existing objects popout metadata card].")
	public void TC_1_4_10_1_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to "Search only : Customers" View
			//---------------------------------------------------
			homePage.searchPanel.search(" ", Caption.Search.SearchOnlyCustomers.Value);

			Log.message("1. Navigate to " + Caption.Search.SearchOnlyCustomers.Value + " view.", driver);

			//Step-2 : Select the multiple objects from the search view 
			//---------------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));

			Log.message("2. Selected the multiple objects from the 'Search only:Customers' view.", driver);

			//Step-3 : Instantiate the right pane metadatacard and verify the property is exists or not 
			//-----------------------------------------------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver,true);//Instantiate the metadatcard
			metadatacard.popOutMetadatacard();

			Log.message("3. Popout metadatcard is opened from the settings menu in right pane metadatacard.", driver);

			String propertyExpected = " ";

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the popout metadatacard
			if(!metadataCard.propertyExists(dataPool.get("PropertyName1")))//Verify if property exist in the metadata
				propertyExpected = "Property : " + dataPool.get("PropertyName1") + " not added to the new object metadatacard.";

			if(metadataCard.propertyExists(dataPool.get("PropertyName2")))//Verify if property not exist in the metadata
				propertyExpected = propertyExpected + "Property : " + dataPool.get("PropertyName2") + " is added to the new object metadatacard.";

			//Verify if the corresponding property is existing the new object
			//---------------------------------------------------------------
			if(propertyExpected.equals(" "))
				Log.pass("Test Case Passed. Specified property : '" +dataPool.get("PropertyName1") + " is  not hidden and Property : "+ dataPool.get("PropertyName2") + " is hidden to the multiple existing objects metadtacard as per the metadataconfigurability rule.");
			else
				Log.fail("Test Case Failed.Property is added to existing object metadatacard is not working as expected." +propertyExpected , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//end catch
		finally {
			Utility.quitDriver(driver);
		}//end finally
	}//End TC_1_4_9_1_3B

	/**
	 * 1.4.2.1A : Verify if property is same time hidden and required[isHidden:true & isRequired:true], object creation is not possible. [Object creation via taskpane]
	 */
	/*@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability", "HOLD", "BUG"}, 
			description = "Verify if property is same time hidden and required[isHidden:true & isRequired:true], object creation is not possible. [Object creation via task pane]")
	 */public void TC_1_4_2_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		 driver = null; 

		 try {



			 driver = WebDriverUtils.getDriver();

			 //Launch the MFWA with valid credentials
			 //--------------------------------------
			 ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			 HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			 //Step-1: Click the new object link from task pane
			 //---------------------------------------------------
			 homePage.taskPanel.clickItem(dataPool.get("ObjectType"));//New object item is clicked from task pane

			 Log.message("1." + dataPool.get("ObjectType") + " is clicked from New item in task pane");

			 //Step-2: Verifies the First layer configurations in the metadatacard
			 //--------------------------------------------------------------------
			 MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadata card

			 metadataCard.setInfo(dataPool.get("Properties"));//Sets ther required property values in the metadatacard

			 //Checks if hidden property is visible in the metadatacard
			 //--------------------------------------------------------
			 if (metadataCard.propertyExists(dataPool.get("Property")))//Checks if hidden property is visible or not in the metadatacard
				 throw new Exception("Test case Failed. Property("+ dataPool.get("Property") +") is visible in the metadatacard");

			 Log.message("2. Required values are set in the metadatacard");

			 //Step-3: Click the create button in the metadatacard
			 //---------------------------------------------------
			 metadataCard.clickCreateBtn();//Clicks the create button in the metadatacard

			 //Verifies if Object is created or not with Required property is hidden in the metadatacard
			 //-----------------------------------------------------------------------------------------
			 if (metadataCard.isEditMode())
				 Log.pass("Test Case Passed. Object is not created when property is hidden and required in the metadatacard", driver);
			 else
				 Log.fail("Test case failed. Object is created when property is hidden and required in the metadatacard", driver);


		 }//End try
		 catch(Exception e){
			 Log.exception(e, driver);
		 }//End catch
		 finally {
			 Utility.quitDriver(driver);
		 }//End finally
	 }//End TC_1_4_2_1A

	 /**
	  * 1.4.2.1B : Verify if property is same time hidden and required[isHidden:true & isRequired:true], object creation is not possible. [Object creation via New Item menu]
	  */
	 /*@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability", "HOLD", "BUG"}, 
			description = "Verify if property is same time hidden and required[isHidden:true & isRequired:true], object creation is not possible. [Object creation via New Item menu]")
	  */public void TC_1_4_2_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		  driver = null; 

		  try {



			  driver = WebDriverUtils.getDriver();

			  //Launch the MFWA with valid credentials
			  //--------------------------------------
			  ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			  HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			  //Step-1: Click the new object link from new item menu
			  //---------------------------------------------------
			  homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//New object item is clicked from new menu item

			  Log.message("1." + dataPool.get("ObjectType") + " is clicked from New item in task pane");

			  //Step-2: Verifies the First layer configurations in the metadatacard
			  //--------------------------------------------------------------------
			  MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadata card

			  metadataCard.setInfo(dataPool.get("Properties"));//Sets ther required property values in the metadatacard

			  //Checks if hidden property is visible in the metadatacard
			  //--------------------------------------------------------
			  if (metadataCard.propertyExists(dataPool.get("Property")))//Checks if hidden property is visible or not in the metadatacard
				  throw new Exception("Test case Failed. Property("+ dataPool.get("Property") +") is visible in the metadatacard");

			  Log.message("2. Required values are set in the metadatacard");

			  //Step-3: Click the create button in the metadatacard
			  //---------------------------------------------------
			  metadataCard.clickCreateBtn();//Clicks the create button in the metadatacard

			  //Verifies if Object is created or not with Required property is hidden in the metadatacard
			  //-----------------------------------------------------------------------------------------
			  if (metadataCard.isEditMode())
				  Log.pass("Test Case Passed. Object is not created when property is hidden and required in the metadatacard", driver);
			  else
				  Log.fail("Test case failed. Object is created when property is hidden and required in the metadatacard", driver);


		  }//End try
		  catch(Exception e){
			  Log.exception(e, driver);
		  }//End catch
		  finally {
			  Utility.quitDriver(driver);
		  }//End finally
	  }//End TC_1_4_2_1B

	  /**
	   * 1.4.6.1.1A : Verify if Existing required property is stays required even configuration isRequired:false is used.[Object creation via task pane]
	   */
	  @Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability", "HOLD", "BUG"}, 
			  description = "Verify if Existing required property is stays required even configuration isRequired:false is used.[Object creation via task pane]")
	  public void TC_1_4_6_1_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		  driver = null; 

		  try {



			  driver = WebDriverUtils.getDriver();

			  //Launch the MFWA with valid credentials
			  //--------------------------------------
			  ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			  HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			  //Step-1: Click the new object link from task pane
			  //---------------------------------------------------
			  homePage.taskPanel.clickItem(dataPool.get("ObjectType"));//New object item is clicked from task pane


			  Log.message("1. " + dataPool.get("ObjectType") + " is clicked from New item in task pane");

			  //Step-2: Verifies the First layer configurations in the metadatacard
			  //--------------------------------------------------------------------
			  MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadata card

			  metadataCard.setInfo(dataPool.get("Properties"));//Sets the required values in the metadatacard

			  Log.message("2. New object("+ dataPool.get("ObjectType") +") metadatacard is displayed");

			  //Checks the exisiting required property in the metadatacard
			  //------------------------------------------------------------
			  String ExpectedMetadata = "";

			  if (!metadataCard.isRequiredProperty(dataPool.get("Property1")))//Checks if property is required or not
				  ExpectedMetadata += "Property("+ dataPool.get("Property1") +") is not displayed as a required property in Property group.";

			  if (!metadataCard.isRequiredProperty(dataPool.get("Property2")))//Checks if property is required or not
				  ExpectedMetadata += "Property("+ dataPool.get("Property2") +") is not displayed as a required property in metadatacard.";

			  //Verifies if existing required property is stays required even after configuration isRequired:false
			  //--------------------------------------------------------------------------------------------------
			  if (ExpectedMetadata.equalsIgnoreCase(""))
				  Log.pass("Test Case Passed. Existing required property is stays required even configuration isRequired:false is used.[Object creation via task pane]", driver);
			  else
				  Log.fail("Test case failed. Existing required property is not stays required when configuration isRequired:false is used.[Object creation via task pane]. For more details: "+ExpectedMetadata, driver);

		  }//End try
		  catch(Exception e){
			  Log.exception(e, driver);
		  }//End catch
		  finally {
			  Utility.quitDriver(driver);
		  }//End finally
	  }//End TC_1_4_6_1_1A

	  /**
	   * 1.4.6.1.1B : Verify if Existing required property is stays required even configuration isRequired:false is used.[Object creation via new item menu]
	   */
	  @Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability", "HOLD", "BUG"}, 
			  description = "Verify if Existing required property is stays required even configuration isRequired:false is used.[Object creation via new item menu]")
	  public void TC_1_4_6_1_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		  driver = null; 

		  try {



			  driver = WebDriverUtils.getDriver();

			  //Launch the MFWA with valid credentials
			  //--------------------------------------
			  ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			  HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			  //Step-1: Click the new object link from task pane
			  //---------------------------------------------------
			  homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//New object item is clicked from new item menu

			  Log.message("1. " + dataPool.get("ObjectType") + " is clicked from New item menu");

			  //Step-2: Verifies the First layer configurations in the metadatacard
			  //--------------------------------------------------------------------
			  MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadata card

			  metadataCard.setInfo(dataPool.get("Properties"));//Sets the required values in the metadatacard

			  Log.message("2. New object("+ dataPool.get("ObjectType") +") metadatacard is displayed");

			  //Checks the exisiting required property in the metadatacard
			  //------------------------------------------------------------
			  String ExpectedMetadata = "";

			  if (!metadataCard.isRequiredProperty(dataPool.get("Property1")))//Checks if property is required or not
				  ExpectedMetadata += "Property("+ dataPool.get("Property1") +") is not displayed as a required property in Property group.";

			  if (!metadataCard.isRequiredProperty(dataPool.get("Property2")))//Checks if property is required or not
				  ExpectedMetadata += "Property("+ dataPool.get("Property2") +") is not displayed as a required property in metadatacard.";

			  //Verifies if existing required property is stays required even after configuration isRequired:false
			  //--------------------------------------------------------------------------------------------------
			  if (ExpectedMetadata.equalsIgnoreCase(""))
				  Log.pass("Test Case Passed. Existing required property is stays required even configuration isRequired:false is used.[Object creation via new item menu]", driver);
			  else
				  Log.fail("Test case failed. Existing required property is not stays required when configuration isRequired:false is used.[Object creation via new item menu].For more details: "+ExpectedMetadata, driver);

		  }//End try
		  catch(Exception e){
			  Log.exception(e, driver);
		  }//End catch
		  finally {
			  Utility.quitDriver(driver);
		  }//End finally
	  }//End TC_1_4_6_1_1B

	  /**
	   * 1.4.6.1.1C : Verify if Existing required property is stays required even configuration isRequired:false is used.[Object creation via Object property]
	   */
	  @Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability", "HOLD", "BUG"}, 
			  description = "Verify if Existing required property is stays required even configuration isRequired:false is used.[Object creation via Object property]")
	  public void TC_1_4_6_1_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		  driver = null; 

		  try {



			  driver = WebDriverUtils.getDriver();

			  //Launch the MFWA with valid credentials
			  //--------------------------------------
			  ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			  HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			  //Step-1 : Navigate to specified view
			  //-----------------------------------
			  SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");//Navigates to the specific view

			  if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				  throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			  MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard of existing object

			  metadataCard.clickAddValueButton(dataPool.get("ObjectType"));//Clicks the Add value button to open the new customer object metadatacard

			  Log.message("1. Opened the new object(" + dataPool.get("ObjectType") +  ") metadatacard via object type property");

			  //Step-2: Verifies the First layer configurations in the metadatacard
			  //--------------------------------------------------------------------
			  metadataCard = new MetadataCard(driver);//Instantiates the metadata card

			  metadataCard.setInfo(dataPool.get("Properties"));//Sets the required values in the metadatacard

			  Log.message("2. New object("+ dataPool.get("ObjectType") +") metadatacard is displayed");

			  //Checks the exisiting required property in the metadatacard
			  //------------------------------------------------------------
			  String ExpectedMetadata = "";

			  if (!metadataCard.isRequiredProperty(dataPool.get("Property1")))//Checks if property is required or not
				  ExpectedMetadata += "Property("+ dataPool.get("Property1") +") is not displayed as a required property in Property group.";

			  if (!metadataCard.isRequiredProperty(dataPool.get("Property2")))//Checks if property is required or not
				  ExpectedMetadata += "Property("+ dataPool.get("Property2") +") is not displayed as a required property in metadatacard.";

			  //Verifies if existing required property is stays required even after configuration isRequired:false
			  //--------------------------------------------------------------------------------------------------
			  if (ExpectedMetadata.equalsIgnoreCase(""))
				  Log.pass("Test Case Passed. Existing required property is stays required even configuration isRequired:false is used.[Object creation via Object property]", driver);
			  else
				  Log.fail("Test case failed. Existing required property is not stays required when configuration isRequired:false is used.[Object creation via Object property]For more details: "+ExpectedMetadata, driver);

		  }//End try
		  catch(Exception e){
			  Log.exception(e, driver);
		  }//End catch
		  finally {
			  Utility.quitDriver(driver);
		  }//End finally
	  }//End TC_1_4_6_1_1C

	  /**
	   * 1.4.6.1.2A : Verify if Existing required property is stays required even configuration isRequired:false is used.[Existing object]
	   */
	  @Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability"}, 
			  description = "Verify if Existing required property is stays required even configuration isRequired:false is used.[Existing object]")
	  public void TC_1_4_6_1_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		  driver = null; 

		  try {



			  driver = WebDriverUtils.getDriver();

			  //Launch the MFWA with valid credentials
			  //--------------------------------------
			  ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			  HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			  //Step-1 : Navigate to specified view
			  //-----------------------------------
			  String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			  Log.message("1. Navigated to : " + viewToNavigate + " view.");

			  //Step-2 : Select any existing object
			  //------------------------------------------------
			  if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				  throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			  Log.message("2. Selected the specified object : " + dataPool.get("Object") + " in list view.");

			  //Checks the exisiting required property in the metadatacard
			  //------------------------------------------------------------
			  String ExpectedMetadata = "";

			  MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadata card

			  if (!metadataCard.isRequiredProperty(dataPool.get("Property1")))//Checks if property is required or not
				  ExpectedMetadata += "Property("+ dataPool.get("Property1") +") is not displayed as a required property in Property group.";

			  if (!metadataCard.isRequiredProperty(dataPool.get("Property2")))//Checks if property is required or not
				  ExpectedMetadata += "Property("+ dataPool.get("Property2") +") is not displayed as a required property in metadatacard.";

			  //Verifies if existing required property is stays required even after configuration isRequired:false
			  //--------------------------------------------------------------------------------------------------
			  if (ExpectedMetadata.equalsIgnoreCase(""))
				  Log.pass("Test Case Passed. Existing required property is stays required even configuration isRequired:false is used.[Existing object]", driver);
			  else
				  Log.fail("Test case failed. Existing required property is not stays required when configuration isRequired:false is used.[Existing object]. For more details: "+ExpectedMetadata, driver);

		  }//End try
		  catch(Exception e){
			  Log.exception(e, driver);
		  }//End catch
		  finally {
			  Utility.quitDriver(driver);
		  }//End finally
	  }//End TC_1_4_6_1_2A

	  /**
	   * 1.4.6.1.2B : Verify if Existing required property is stays required even configuration isRequired:false is used.[Existing object popped out metadatacard]
	   */
	  @Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability"}, 
			  description = "Verify if Existing required property is stays required even configuration isRequired:false is used.[Existing object popped out metadatacard]")
	  public void TC_1_4_6_1_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		  driver = null; 

		  try {



			  driver = WebDriverUtils.getDriver();

			  //Launch the MFWA with valid credentials
			  //--------------------------------------
			  ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			  HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			  //Step-1 : Navigate to specified view
			  //-----------------------------------
			  String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			  Log.message("1. Navigated to : " + viewToNavigate + " view.");

			  //Step-2 : Select any existing object
			  //------------------------------------------------
			  if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				  throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			  Log.message("2. Selected the specified object : " + dataPool.get("Object") + " in list view.");

			  //Step-3:Open the popout metadatacard
			  //--------------------------------------
			  if (!homePage.taskPanel.clickItem("Properties"))
				  throw new Exception("Properties is not clicked from task pane");

			  Log.message("3. Popped out metadatacard of the selected object is opened");

			  //Checks the exisiting required property in the metadatacard
			  //------------------------------------------------------------
			  String ExpectedMetadata = "";

			  MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadata card

			  if (!metadataCard.isRequiredProperty(dataPool.get("Property1")))//Checks if property is required or not
				  ExpectedMetadata += "Property("+ dataPool.get("Property1") +") is not displayed as a required property in Property group.";

			  if (!metadataCard.isRequiredProperty(dataPool.get("Property2")))//Checks if property is required or not
				  ExpectedMetadata += "Property("+ dataPool.get("Property2") +") is not displayed as a required property in metadatacard.";

			  //Verifies if existing required property is stays required even after configuration isRequired:false
			  //--------------------------------------------------------------------------------------------------
			  if (ExpectedMetadata.equalsIgnoreCase(""))
				  Log.pass("Test Case Passed. Existing required property is stays required even configuration isRequired:false is used.[Existing object popped out metadatacard]", driver);
			  else
				  Log.fail("Test case failed. Existing required property is not stays required when configuration isRequired:false is used.[Existing object popped out metadatacard]. For more details: "+ExpectedMetadata, driver);

		  }//End try
		  catch(Exception e){
			  Log.exception(e, driver);
		  }//End catch
		  finally {
			  Utility.quitDriver(driver);
		  }//End finally
	  }//End TC_1_4_6_1_2B

	  /**
	   * 1.4.6.1.3A : Verify if Existing required property is stays required even configuration isRequired:false is used.[Multi selected Existing objects metadatacard]
	   */
	  @Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "MFN119MetadataConfigurability"}, 
			  description = "Verify if Existing required property is stays required even configuration isRequired:false is used.[Multi selected Existing objects metadatacard]")
	  public void TC_1_4_6_1_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		  if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			  throw new SkipException(driverType + " driver does not supports Multi-select.");

		  driver = null; 

		  try {



			  driver = WebDriverUtils.getDriver();

			  //Launch the MFWA with valid credentials
			  //--------------------------------------
			  ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			  HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			  //Step-1 : Navigate to specified view
			  //-----------------------------------
			  String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			  Log.message("1. Navigated to : " + viewToNavigate + " view.");

			  //Step-2 : Select any existing object
			  //------------------------------------------------
			  homePage.listView.clickMultipleItems(dataPool.get("Object"));//Select the specified objects

			  Log.message("2. Selected the specified objects : " + dataPool.get("Object") + " in list view.");

			  //Checks the exisiting required property in the metadatacard
			  //------------------------------------------------------------
			  String ExpectedMetadata = "";

			  MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadata card

			  if (!metadataCard.isRequiredProperty(dataPool.get("Property1")))//Checks if property is required or not
				  ExpectedMetadata += "Property("+ dataPool.get("Property1") +") is not displayed as a required property in Property group.";

			  if (!metadataCard.isRequiredProperty(dataPool.get("Property2")))//Checks if property is required or not
				  ExpectedMetadata += "Property("+ dataPool.get("Property2") +") is not displayed as a required property in metadatacard.";

			  //Verifies if existing required property is stays required even after configuration isRequired:false
			  //--------------------------------------------------------------------------------------------------
			  if (ExpectedMetadata.equalsIgnoreCase(""))
				  Log.pass("Test Case Passed. Existing required property is stays required even configuration isRequired:false is used.[Multi selected Existing objects metadatacard]", driver);
			  else
				  Log.fail("Test case failed. Existing required property is not stays required when configuration isRequired:false is used.[Multi selected Existing objects metadatacard]. For more details: "+ExpectedMetadata, driver);

		  }//End try
		  catch(Exception e){
			  Log.exception(e, driver);
		  }//End catch
		  finally {
			  Utility.quitDriver(driver);
		  }//End finally
	  }//End TC_1_4_6_1_3A

	  /**
	   * 1.4.6.1.3B : Verify if Existing required property is stays required even configuration isRequired:false is used.[Multi selected Existing objects popped out metadatacard]
	   */
	  @Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "MFN119MetadataConfigurability"}, 
			  description = "Verify if Existing required property is stays required even configuration isRequired:false is used.[Multi selected Existing objects popped out metadatacard]")
	  public void TC_1_4_6_1_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		  if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			  throw new SkipException(driverType + " driver does not supports Multi-select.");

		  driver = null; 

		  try {



			  driver = WebDriverUtils.getDriver();

			  //Launch the MFWA with valid credentials
			  //--------------------------------------
			  ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			  HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			  //Step-1 : Navigate to specified view
			  //-----------------------------------
			  String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			  Log.message("1. Navigated to : " + viewToNavigate + " view.");

			  //Step-2 : Select any existing object
			  //------------------------------------------------
			  homePage.listView.clickMultipleItems(dataPool.get("Object"));//Select the specified objects

			  Log.message("2. Selected the specified objects : " + dataPool.get("Object") + " in list view.");

			  //Step-3:Open the popout metadatacard
			  //--------------------------------------
			  if (!homePage.taskPanel.clickItem("Properties"))
				  throw new Exception("Properties is not clicked from task pane");

			  Log.message("3. Popped out metadatacard of the selected object is opened");

			  //Checks the exisiting required property in the metadatacard
			  //------------------------------------------------------------
			  String ExpectedMetadata = "";

			  MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadata card

			  if (!metadataCard.isRequiredProperty(dataPool.get("Property1")))//Checks if property is required or not
				  ExpectedMetadata += "Property("+ dataPool.get("Property1") +") is not displayed as a required property in Property group.";

			  if (!metadataCard.isRequiredProperty(dataPool.get("Property2")))//Checks if property is required or not
				  ExpectedMetadata += "Property("+ dataPool.get("Property2") +") is not displayed as a required property in metadatacard.";

			  //Verifies if existing required property is stays required even after configuration isRequired:false
			  //--------------------------------------------------------------------------------------------------
			  if (ExpectedMetadata.equalsIgnoreCase(""))
				  Log.pass("Test Case Passed. Existing required property is stays required even configuration isRequired:false is used.[Multi selected Existing objects popped out metadatacard]", driver);
			  else
				  Log.fail("Test case failed. Existing required property is not stays required when configuration isRequired:false is used.[Multi selected Existing objects popped out metadatacard]. For more details: "+ExpectedMetadata, driver);

		  }//End try
		  catch(Exception e){
			  Log.exception(e, driver);
		  }//End catch
		  finally {
			  Utility.quitDriver(driver);
		  }//End finally
	  }//End TC_1_4_6_1_3B

	  /**
	   * 1.4.8.1.1A : Verify if Property is visible in metadata card when isAdditional:true/false & isHidden:false/true is used. [Object creation via Task pane]
	   */
	  @Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability"}, 
			  description = "Verify if Property is visible in metadata card when isAdditional:true/false & isHidden:false/true is used. [Object creation via Task pane]")
	  public void TC_1_4_8_1_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		  driver = null; 

		  try {



			  driver = WebDriverUtils.getDriver();

			  //Launch the MFWA with valid credentials
			  //--------------------------------------
			  ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			  HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			  //Step-1: Click the new object link from task pane
			  //---------------------------------------------------
			  homePage.taskPanel.clickItem(dataPool.get("ObjectType"));//New object item is clicked from task pane


			  Log.message("1. " + dataPool.get("ObjectType") + " is clicked from New item in task pane");

			  //Step-2: Sets the configuration rule in the metadatacard
			  //--------------------------------------------------------------------
			  MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadata card

			  metadataCard.setInfo(dataPool.get("Properties"));//Sets the property value in the metadatacard

			  String[] properties = dataPool.get("Properties").split("::");

			  if (!metadataCard.getPropertyValue(properties[0]).equalsIgnoreCase(properties[1]))
				  throw new Exception("Property("+properties[0]+") is not set with value("+properties[1]+" for the configuration rule in the metadatacard");

			  Log.message("2. Property("+properties[0]+") is set with value("+properties[1]+" for the configuration rule in the metadatacard", driver);

			  //Checks the configuration behavior in the metadatacard
			  //-----------------------------------------------------
			  metadataCard = new MetadataCard(driver);

			  String ExpectedMetadata = "";

			  //Checks if properties are exisit in the metadatacard [Outside the group]
			  if (!metadataCard.propertyExists(dataPool.get("Property1")))
				  ExpectedMetadata += " Property(" + dataPool.get("Property1") + ") is not added in the metadatacard while using the behavior isAdditional:true & isHidden:false [Outside the group].";

			  if (metadataCard.propertyExists(dataPool.get("Property2")))
				  ExpectedMetadata += " Property(" + dataPool.get("Property2") + ") is displayed in the metadatacard while using the behavior isAdditional:true & isHidden:true [Outside the group].";

			  //Checks if property group is exisit in the metadatacard [Inside the group]
			  if (!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("GroupText")))//Checks if proeprty group is displayed in the metadatacard
				  throw new Exception("Property Group is not displayed in the metadatacard.");

			  metadataCard.expandPropertyGroup(1, true);//Expands the property group in the metadatacard

			  //Checks if properties are exisit in the metadatacard [Inside the group]
			  if (!metadataCard.propertyExistsInGroup(1, dataPool.get("GrpProperty1")))
				  ExpectedMetadata += " Property(" + dataPool.get("GrpProperty2") + ") is not added in the metadatacard while using the behavior isAdditional:true & isHidden:false [Inside the group].";

			  if (metadataCard.propertyExistsInGroup(1, dataPool.get("GrpProperty2")))
				  ExpectedMetadata += " Property(" + dataPool.get("GrpProperty2") + ") is displayed in the metadatacard while using the behavior isAdditional:true & isHidden:true [Inside the group].";

			  if (ExpectedMetadata.equalsIgnoreCase(""))
				  Log.pass("Test case passed. Metadata configurability is working as expected when isAdditional:true/false & isHidden:false/true is used. [Object creation via Task pane]", driver);
			  else
				  Log.fail("Test case failed. Metadata configurability is not working as expected when isAdditional:true/false & isHidden:false/true is used. [Object creation via Task pane]. FOr more details: "+ ExpectedMetadata, driver);

		  }//End try
		  catch(Exception e){
			  Log.exception(e, driver);
		  }//End catch
		  finally {
			  Utility.quitDriver(driver);
		  }//End finally
	  }//End TC_1_4_8_1_1A

	  /**
	   * 1.4.8.1.1B : Verify if Property is visible in metadata card when isAdditional:true/false & isHidden:false/true is used. [Object creation via New item menu]
	   */
	  @Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability"}, 
			  description = "Verify if Property is visible in metadata card when isAdditional:true/false & isHidden:false/true is used. [Object creation via New item menu]")
	  public void TC_1_4_8_1_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		  driver = null; 

		  try {



			  driver = WebDriverUtils.getDriver();

			  //Launch the MFWA with valid credentials
			  //--------------------------------------
			  ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			  HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			  //Step-1: Click the new object link from task pane
			  //---------------------------------------------------
			  homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//New object item is clicked from new item menu

			  Log.message("1. " + dataPool.get("ObjectType") + " is clicked from New item menu");

			  //Step-2: Sets the configuration rule in the metadatacard
			  //--------------------------------------------------------------------
			  MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadata card

			  metadataCard.setInfo(dataPool.get("Properties"));//Sets the property value in the metadatacard

			  String[] properties = dataPool.get("Properties").split("::");

			  if (!metadataCard.getPropertyValue(properties[0]).equalsIgnoreCase(properties[1]))
				  throw new Exception("Property("+properties[0]+") is not set with value("+properties[1]+" for the configuration rule in the metadatacard");

			  Log.message("2. Property("+properties[0]+") is set with value("+properties[1]+" for the configuration rule in the metadatacard", driver);

			  //Checks the configuration behavior in the metadatacard
			  //-----------------------------------------------------
			  metadataCard = new MetadataCard(driver);

			  String ExpectedMetadata = "";

			  //Checks if properties are exisit in the metadatacard [Outside the group]
			  if (!metadataCard.propertyExists(dataPool.get("Property1")))
				  ExpectedMetadata += " Property(" + dataPool.get("Property1") + ") is not added in the metadatacard while using the behavior isAdditional:true & isHidden:false [Outside the group].";

			  if (metadataCard.propertyExists(dataPool.get("Property2")))
				  ExpectedMetadata += " Property(" + dataPool.get("Property2") + ") is displayed in the metadatacard while using the behavior isAdditional:true & isHidden:true [Outside the group].";

			  //Checks if property group is exisit in the metadatacard [Inside the group]
			  if (!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("GroupText")))//Checks if proeprty group is displayed in the metadatacard
				  throw new Exception("Property Group is not displayed in the metadatacard.");

			  metadataCard.expandPropertyGroup(1, true);//Expands the property group in the metadatacard

			  //Checks if properties are exisit in the metadatacard [Inside the group]
			  if (!metadataCard.propertyExistsInGroup(1, dataPool.get("GrpProperty1")))
				  ExpectedMetadata += " Property(" + dataPool.get("GrpProperty2") + ") is not added in the metadatacard while using the behavior isAdditional:true & isHidden:false [Inside the group].";

			  if (metadataCard.propertyExistsInGroup(1, dataPool.get("GrpProperty2")))
				  ExpectedMetadata += " Property(" + dataPool.get("GrpProperty2") + ") is displayed in the metadatacard while using the behavior isAdditional:true & isHidden:true [Inside the group].";

			  if (ExpectedMetadata.equalsIgnoreCase(""))
				  Log.pass("Test case passed. Metadata configurability is working as expected when isAdditional:true/false & isHidden:false/true is used. [Object creation via New item menu]", driver);
			  else
				  Log.fail("Test case failed. Metadata configurability is not working as expected when isAdditional:true/false & isHidden:false/true is used. [Object creation via New item menu]. FOr more details: "+ ExpectedMetadata, driver);

		  }//End try
		  catch(Exception e){
			  Log.exception(e, driver);
		  }//End catch
		  finally {
			  Utility.quitDriver(driver);
		  }//End finally
	  }//End TC_1_4_8_1_1B

	  /**
	   * 1.4.8.1.1C : Verify if Property is visible in metadata card when isAdditional:true/false & isHidden:false/true is used. [Object creation via Object property]
	   */
	  @Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability"}, 
			  description = "Verify if Property is visible in metadata card when isAdditional:true/false & isHidden:false/true is used. [Object creation via Object property]")
	  public void TC_1_4_8_1_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		  driver = null; 

		  try {



			  driver = WebDriverUtils.getDriver();

			  //Launch the MFWA with valid credentials
			  //--------------------------------------
			  ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			  HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			  //Step-1 : Navigate to specified view
			  //-----------------------------------
			  SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");//Navigates to the specific view

			  if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				  throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			  MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard of existing object

			  metadataCard.clickAddValueButton(dataPool.get("ObjectType"));//Clicks the Add value button to open the new customer object metadatacard

			  Log.message("1. Opened the new object(" + dataPool.get("ObjectType") +  ") metadatacard via object type property");


			  //Step-2: Sets the configuration rule in the metadatacard
			  //--------------------------------------------------------------------
			  metadataCard = new MetadataCard(driver);//Instantiates the metadata card

			  metadataCard.setInfo(dataPool.get("Properties"));//Sets the property value in the metadatacard

			  String[] properties = dataPool.get("Properties").split("::");

			  if (!metadataCard.getPropertyValue(properties[0]).equalsIgnoreCase(properties[1]))
				  throw new Exception("Property("+properties[0]+") is not set with value("+properties[1]+" for the configuration rule in the metadatacard");

			  Log.message("2. Property("+properties[0]+") is set with value("+properties[1]+" for the configuration rule in the metadatacard", driver);

			  //Checks the configuration behavior in the metadatacard
			  //-----------------------------------------------------
			  metadataCard = new MetadataCard(driver);

			  String ExpectedMetadata = "";

			  //Checks if properties are exisit in the metadatacard [Outside the group]
			  if (!metadataCard.propertyExists(dataPool.get("Property1")))
				  ExpectedMetadata += " Property(" + dataPool.get("Property1") + ") is not added in the metadatacard while using the behavior isAdditional:true & isHidden:false [Outside the group].";

			  if (metadataCard.propertyExists(dataPool.get("Property2")))
				  ExpectedMetadata += " Property(" + dataPool.get("Property2") + ") is displayed in the metadatacard while using the behavior isAdditional:true & isHidden:true [Outside the group].";

			  //Checks if property group is exisit in the metadatacard [Inside the group]
			  if (!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("GroupText")))//Checks if proeprty group is displayed in the metadatacard
				  throw new Exception("Property Group is not displayed in the metadatacard.");

			  metadataCard.expandPropertyGroup(1, true);//Expands the property group in the metadatacard

			  //Checks if properties are exisit in the metadatacard [Inside the group]
			  if (!metadataCard.propertyExistsInGroup(1, dataPool.get("GrpProperty1")))
				  ExpectedMetadata += " Property(" + dataPool.get("GrpProperty2") + ") is not added in the metadatacard while using the behavior isAdditional:true & isHidden:false [Inside the group].";

			  if (metadataCard.propertyExistsInGroup(1, dataPool.get("GrpProperty2")))
				  ExpectedMetadata += " Property(" + dataPool.get("GrpProperty2") + ") is displayed in the metadatacard while using the behavior isAdditional:true & isHidden:true [Inside the group].";

			  if (ExpectedMetadata.equalsIgnoreCase(""))
				  Log.pass("Test case passed. Metadata configurability is working as expected when isAdditional:true/false & isHidden:false/true is used. [Object creation via Object property]", driver);
			  else
				  Log.fail("Test case failed. Metadata configurability is not working as expected when isAdditional:true/false & isHidden:false/true is used. [Object creation via Object property]. FOr more details: "+ ExpectedMetadata, driver);

		  }//End try
		  catch(Exception e){
			  Log.exception(e, driver);
		  }//End catch
		  finally {
			  Utility.quitDriver(driver);
		  }//End finally
	  }//End TC_1_4_8_1_1C

	  /**
	   * 1.4.8.1.2A : Verify if Property is visible in metadata card when isAdditional:true/false & isHidden:false/true is used. [Existing object metadatacard]
	   */
	  @Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability"}, 
			  description = "Verify if Property is visible in metadata card when isAdditional:true/false & isHidden:false/true is used. [Existing object metadatacard]")
	  public void TC_1_4_8_1_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		  driver = null; 

		  try {



			  driver = WebDriverUtils.getDriver();

			  //Launch the MFWA with valid credentials
			  //--------------------------------------
			  ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			  HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			  //Step-1 : Navigate to specified view
			  //-----------------------------------
			  String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			  Log.message("1. Navigated to : " + viewToNavigate + " view.");

			  //Step-2 : Select any existing object
			  //------------------------------------------------
			  if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				  throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			  Log.message("2. Selected the specified object : " + dataPool.get("Object") + " in list view.");

			  //Step-3: Sets the configuration rule in the metadatacard
			  //--------------------------------------------------------------------
			  MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadata card

			  metadataCard.setInfo(dataPool.get("Properties"));//Sets the property value in the metadatacard

			  String[] properties = dataPool.get("Properties").split("::");

			  if (!metadataCard.getPropertyValue(properties[0]).equalsIgnoreCase(properties[1]))
				  throw new Exception("Property("+properties[0]+") is not set with value("+properties[1]+" for the configuration rule in the metadatacard");

			  Log.message("3. Property("+properties[0]+") is set with value("+properties[1]+" for the configuration rule in the metadatacard", driver);

			  //Checks the configuration behavior in the metadatacard
			  //-----------------------------------------------------
			  driver.switchTo().defaultContent();
			  metadataCard = new MetadataCard(driver, true);

			  String ExpectedMetadata = "";

			  //Checks if properties are exisit in the metadatacard [Outside the group]
			  if (!metadataCard.propertyExists(dataPool.get("Property1")))
				  ExpectedMetadata += " Property(" + dataPool.get("Property1") + ") is not added in the metadatacard while using the behavior isAdditional:true & isHidden:false [Outside the group].";

			  if (metadataCard.propertyExists(dataPool.get("Property2")))
				  ExpectedMetadata += " Property(" + dataPool.get("Property2") + ") is displayed in the metadatacard while using the behavior isAdditional:true & isHidden:true [Outside the group].";

			  //Checks if property group is exisit in the metadatacard [Inside the group]
			  if (!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("GroupText")))//Checks if proeprty group is displayed in the metadatacard
				  throw new Exception("Property Group is not displayed in the metadatacard.");

			  metadataCard.expandPropertyGroup(1, true);//Expands the property group in the metadatacard

			  //Checks if properties are exisit in the metadatacard [Inside the group]
			  if (!metadataCard.propertyExistsInGroup(1, dataPool.get("GrpProperty1")))
				  ExpectedMetadata += " Property(" + dataPool.get("GrpProperty2") + ") is not added in the metadatacard while using the behavior isAdditional:true & isHidden:false [Inside the group].";

			  if (metadataCard.propertyExistsInGroup(1, dataPool.get("GrpProperty2")))
				  ExpectedMetadata += " Property(" + dataPool.get("GrpProperty2") + ") is displayed in the metadatacard while using the behavior isAdditional:true & isHidden:true [Inside the group].";

			  if (ExpectedMetadata.equalsIgnoreCase(""))
				  Log.pass("Test case passed. Metadata configurability is working as expected when isAdditional:true/false & isHidden:false/true is used. [Existing object metadatacard]", driver);
			  else
				  Log.fail("Test case failed. Metadata configurability is not working as expected when isAdditional:true/false & isHidden:false/true is used. [Existing object metadatacard]. FOr more details: "+ ExpectedMetadata, driver);

		  }//End try
		  catch(Exception e){
			  Log.exception(e, driver);
		  }//End catch
		  finally {
			  Utility.quitDriver(driver);
		  }//End finally
	  }//End TC_1_4_8_1_2A

	  /**
	   * 1.4.8.1.2B : Verify if Property is visible in metadata card when isAdditional:true/false & isHidden:false/true is used. [Existing object popout metadatacard]
	   */
	  @Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability"}, 
			  description = "Verify if Property is visible in metadata card when isAdditional:true/false & isHidden:false/true is used. [Existing object popout metadatacard]")
	  public void TC_1_4_8_1_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		  driver = null; 

		  try {



			  driver = WebDriverUtils.getDriver();

			  //Launch the MFWA with valid credentials
			  //--------------------------------------
			  ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			  HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			  //Step-1 : Navigate to specified view
			  //-----------------------------------
			  String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			  Log.message("1. Navigated to : " + viewToNavigate + " view.");

			  //Step-2 : Select any existing object
			  //------------------------------------------------
			  if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				  throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			  Log.message("2. Selected the specified object : " + dataPool.get("Object") + " in list view.");

			  //Step-3 : Select the "Properties" option from the taskpanel
			  //----------------------------------------------------------
			  homePage.taskPanel.clickItem("Properties");//Clicks the Properties option from task pane

			  Log.message("3. Selected the 'Properties' option from the task pane.");

			  //Step-4: Sets the configuration rule in the metadatacard
			  //--------------------------------------------------------------------
			  MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadata card

			  metadataCard.setInfo(dataPool.get("Properties"));//Sets the property value in the metadatacard

			  String[] properties = dataPool.get("Properties").split("::");

			  if (!metadataCard.getPropertyValue(properties[0]).equalsIgnoreCase(properties[1]))
				  throw new Exception("Property("+properties[0]+") is not set with value("+properties[1]+" for the configuration rule in the metadatacard");

			  Log.message("4. Property("+properties[0]+") is set with value("+properties[1]+" for the configuration rule in the metadatacard", driver);

			  //Checks the configuration behavior in the metadatacard
			  //-----------------------------------------------------
			  metadataCard = new MetadataCard(driver);

			  String ExpectedMetadata = "";

			  //Checks if properties are exisit in the metadatacard [Outside the group]
			  if (!metadataCard.propertyExists(dataPool.get("Property1")))
				  ExpectedMetadata += " Property(" + dataPool.get("Property1") + ") is not added in the metadatacard while using the behavior isAdditional:true & isHidden:false [Outside the group].";

			  if (metadataCard.propertyExists(dataPool.get("Property2")))
				  ExpectedMetadata += " Property(" + dataPool.get("Property2") + ") is displayed in the metadatacard while using the behavior isAdditional:true & isHidden:true [Outside the group].";

			  //Checks if property group is exisit in the metadatacard [Inside the group]
			  if (!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("GroupText")))//Checks if proeprty group is displayed in the metadatacard
				  throw new Exception("Property Group is not displayed in the metadatacard.");

			  metadataCard.expandPropertyGroup(1, true);//Expands the property group in the metadatacard

			  //Checks if properties are exisit in the metadatacard [Inside the group]
			  if (!metadataCard.propertyExistsInGroup(1, dataPool.get("GrpProperty1")))
				  ExpectedMetadata += " Property(" + dataPool.get("GrpProperty2") + ") is not added in the metadatacard while using the behavior isAdditional:true & isHidden:false [Inside the group].";

			  if (metadataCard.propertyExistsInGroup(1, dataPool.get("GrpProperty2")))
				  ExpectedMetadata += " Property(" + dataPool.get("GrpProperty2") + ") is displayed in the metadatacard while using the behavior isAdditional:true & isHidden:true [Inside the group].";

			  if (ExpectedMetadata.equalsIgnoreCase(""))
				  Log.pass("Test case passed. Metadata configurability is working as expected when isAdditional:true/false & isHidden:false/true is used. [Existing object popout metadatacard]", driver);
			  else
				  Log.fail("Test case failed. Metadata configurability is not working as expected when isAdditional:true/false & isHidden:false/true is used. [Existing object popout metadatacard]. FOr more details: "+ ExpectedMetadata, driver);

		  }//End try
		  catch(Exception e){
			  Log.exception(e, driver);
		  }//End catch
		  finally {
			  Utility.quitDriver(driver);
		  }//End finally
	  }//End TC_1_4_8_1_2B

	  /**
	   * 1.4.8.1.3A : Verify if Property is visible in metadata card when isAdditional:true/false & isHidden:false/true is used. [Multiple Existing objects metadatacard]
	   */
	  @Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "MFN119MetadataConfigurability"}, 
			  description = "Verify if Property is visible in metadata card when isAdditional:true/false & isHidden:false/true is used. [Multiple Existing objects metadatacard]")
	  public void TC_1_4_8_1_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		  if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			  throw new SkipException(driverType + " driver does not supports Multi-select.");

		  driver = null; 

		  try {



			  driver = WebDriverUtils.getDriver();

			  //Launch the MFWA with valid credentials
			  //--------------------------------------
			  ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			  HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			  //Step-1 : Navigate to specified view
			  //-----------------------------------
			  String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			  Log.message("1. Navigated to : " + viewToNavigate + " view.");

			  //Step-2 : Select any existing object
			  //------------------------------------------------
			  homePage.listView.clickMultipleItems(dataPool.get("Object"));//Select the specified objects

			  Log.message("2. Selected the specified objects : " + dataPool.get("Object") + " in list view.");
			  //Step-3: Sets the configuration rule in the metadatacard
			  //--------------------------------------------------------------------
			  MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadata card

			  metadataCard.setInfo(dataPool.get("Properties"));//Sets the property value in the metadatacard

			  String[] properties = dataPool.get("Properties").split("::");

			  if (!metadataCard.getPropertyValue(properties[0]).equalsIgnoreCase(properties[1]))
				  throw new Exception("Property("+properties[0]+") is not set with value("+properties[1]+" for the configuration rule in the metadatacard");

			  Log.message("3. Property("+properties[0]+") is set with value("+properties[1]+" for the configuration rule in the metadatacard", driver);

			  //Checks the configuration behavior in the metadatacard
			  //-----------------------------------------------------
			  driver.switchTo().defaultContent();
			  metadataCard = new MetadataCard(driver, true);

			  String ExpectedMetadata = "";

			  //Checks if properties are exisit in the metadatacard [Outside the group]
			  if (!metadataCard.propertyExists(dataPool.get("Property1")))
				  ExpectedMetadata += " Property(" + dataPool.get("Property1") + ") is not added in the metadatacard while using the behavior isAdditional:true & isHidden:false [Outside the group].";

			  if (metadataCard.propertyExists(dataPool.get("Property2")))
				  ExpectedMetadata += " Property(" + dataPool.get("Property2") + ") is displayed in the metadatacard while using the behavior isAdditional:true & isHidden:true [Outside the group].";

			  //Checks if property group is exisit in the metadatacard [Inside the group]
			  if (!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("GroupText")))//Checks if proeprty group is displayed in the metadatacard
				  throw new Exception("Property Group is not displayed in the metadatacard.");

			  metadataCard.expandPropertyGroup(1, true);//Expands the property group in the metadatacard

			  //Checks if properties are exisit in the metadatacard [Inside the group]
			  if (!metadataCard.propertyExistsInGroup(1, dataPool.get("GrpProperty1")))
				  ExpectedMetadata += " Property(" + dataPool.get("GrpProperty2") + ") is not added in the metadatacard while using the behavior isAdditional:true & isHidden:false [Inside the group].";

			  if (metadataCard.propertyExistsInGroup(1, dataPool.get("GrpProperty2")))
				  ExpectedMetadata += " Property(" + dataPool.get("GrpProperty2") + ") is displayed in the metadatacard while using the behavior isAdditional:true & isHidden:true [Inside the group].";

			  if (ExpectedMetadata.equalsIgnoreCase(""))
				  Log.pass("Test case passed. Metadata configurability is working as expected when isAdditional:true/false & isHidden:false/true is used. [Multiple Existing objects metadatacard]", driver);
			  else
				  Log.fail("Test case failed. Metadata configurability is not working as expected when isAdditional:true/false & isHidden:false/true is used. [Multiple Existing objects metadatacard]. FOr more details: "+ ExpectedMetadata, driver);

		  }//End try
		  catch(Exception e){
			  Log.exception(e, driver);
		  }//End catch
		  finally {
			  Utility.quitDriver(driver);
		  }//End finally
	  }//End TC_1_4_8_1_3A

	  /**
	   * 1.4.8.1.3B : Verify if Property is visible in metadata card when isAdditional:true/false & isHidden:false/true is used. [Multiple Existing objects popout metadatacard]
	   */
	  @Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "MFN119MetadataConfigurability"}, 
			  description = "Verify if Property is visible in metadata card when isAdditional:true/false & isHidden:false/true is used. [Multiple Existing objects popout metadatacard]")
	  public void TC_1_4_8_1_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		  if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			  throw new SkipException(driverType + " driver does not supports Multi-select.");

		  driver = null; 

		  try {



			  driver = WebDriverUtils.getDriver();

			  //Launch the MFWA with valid credentials
			  //--------------------------------------
			  ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			  HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			  //Step-1 : Navigate to specified view
			  //-----------------------------------
			  String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			  Log.message("1. Navigated to : " + viewToNavigate + " view.");

			  //Step-2 : Select any existing object
			  //------------------------------------------------
			  homePage.listView.clickMultipleItems(dataPool.get("Object"));//Select the specified objects

			  Log.message("2. Selected the specified objects : " + dataPool.get("Object") + " in list view.");

			  //Step-3 : Select the "Properties" option from the taskpanel
			  //----------------------------------------------------------
			  homePage.taskPanel.clickItem("Properties");//Clicks the Properties option from task pane

			  Log.message("3. Selected the 'Properties' option from the task pane.");

			  //Step-4: Sets the configuration rule in the metadatacard
			  //--------------------------------------------------------------------
			  MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadata card

			  metadataCard.setInfo(dataPool.get("Properties"));//Sets the property value in the metadatacard

			  String[] properties = dataPool.get("Properties").split("::");

			  if (!metadataCard.getPropertyValue(properties[0]).equalsIgnoreCase(properties[1]))
				  throw new Exception("Property("+properties[0]+") is not set with value("+properties[1]+" for the configuration rule in the metadatacard");

			  Log.message("4. Property("+properties[0]+") is set with value("+properties[1]+" for the configuration rule in the metadatacard", driver);

			  //Checks the configuration behavior in the metadatacard
			  //-----------------------------------------------------
			  metadataCard = new MetadataCard(driver);

			  String ExpectedMetadata = "";

			  //Checks if properties are exisit in the metadatacard [Outside the group]
			  if (!metadataCard.propertyExists(dataPool.get("Property1")))
				  ExpectedMetadata += " Property(" + dataPool.get("Property1") + ") is not added in the metadatacard while using the behavior isAdditional:true & isHidden:false [Outside the group].";

			  if (metadataCard.propertyExists(dataPool.get("Property2")))
				  ExpectedMetadata += " Property(" + dataPool.get("Property2") + ") is displayed in the metadatacard while using the behavior isAdditional:true & isHidden:true [Outside the group].";

			  //Checks if property group is exisit in the metadatacard [Inside the group]
			  if (!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("GroupText")))//Checks if proeprty group is displayed in the metadatacard
				  throw new Exception("Property Group is not displayed in the metadatacard.");

			  metadataCard.expandPropertyGroup(1, true);//Expands the property group in the metadatacard

			  //Checks if properties are exisit in the metadatacard [Inside the group]
			  if (!metadataCard.propertyExistsInGroup(1, dataPool.get("GrpProperty1")))
				  ExpectedMetadata += " Property(" + dataPool.get("GrpProperty2") + ") is not added in the metadatacard while using the behavior isAdditional:true & isHidden:false [Inside the group].";

			  if (metadataCard.propertyExistsInGroup(1, dataPool.get("GrpProperty2")))
				  ExpectedMetadata += " Property(" + dataPool.get("GrpProperty2") + ") is displayed in the metadatacard while using the behavior isAdditional:true & isHidden:true [Inside the group].";

			  if (ExpectedMetadata.equalsIgnoreCase(""))
				  Log.pass("Test case passed. Metadata configurability is working as expected when isAdditional:true/false & isHidden:false/true is used. [Multiple Existing objects popout metadatacard]", driver);
			  else
				  Log.fail("Test case failed. Metadata configurability is not working as expected when isAdditional:true/false & isHidden:false/true is used. [Multiple Existing objects popout metadatacard]. FOr more details: "+ ExpectedMetadata, driver);

		  }//End try
		  catch(Exception e){
			  Log.exception(e, driver);
		  }//End catch
		  finally {
			  Utility.quitDriver(driver);
		  }//End finally
	  }//End TC_1_4_8_1_3B

	  /**
	   *  TC_38260 : Verify if IsAdditional properties stay on metadatacard when set the specified class in metadatacard.
	   *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability"}, 
			description = "Verify if IsAdditional properties stay on metadatacard when set the specified class in metadatacard.")
	public void TC_38260(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Create the new document from the menu bar
			//--------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Document.Value);//Click the new menu document item

			Log.message("1. Created the new 'Document' object from the menu bar.", driver);

			//Step-2 : Select the template from the documnent 
			//-----------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);//instantiate the right pane metadatacard
			metadatacard.selectClassInTemplate(dataPool.get("Template1"));//set the template in metadatacard

			Log.message("2. Selected the template " +dataPool.get("Template1")+" in metadatacard.");

			String ExpectedProperty = "";

			//Verify if metadatacard description is set as expected
			//-----------------------------------------------------
			metadatacard = new MetadataCard(driver);
			if(!metadatacard.getMetadataDescriptionText().trim().equals(dataPool.get("DescriptionText").trim()))//Get the description text from the metdatacard
				ExpectedProperty = "Description text is not set as expected" + metadatacard.getMetadataDescriptionText() ;

			//Verify if property is exist in metadatacard
			//-------------------------------------------
			if(!metadatacard.propertyExists(dataPool.get("PropertyName")))//Verify if property exist in metadatacard
				ExpectedProperty = ExpectedProperty+ "Property Name : "+dataPool.get("PropertyName")+" is not exist in the metadatacard." ;

			//Step-3 : Select the different template
			//--------------------------------------
			metadatacard.setPropertyValue("Class",dataPool.get("Template2"));//Set template in metadatacard

			Log.message("3. Changed the template as : " + dataPool.get("Template2"));

			//Verify if property is not exist in metadatacard
			//-----------------------------------------------
			metadatacard = new MetadataCard(driver);
			if(metadatacard.propertyExists(dataPool.get("PropertyName")))//Verify if property exists in metadatacard 
				ExpectedProperty = ExpectedProperty+ "Property name : " +dataPool.get("PropertyName")+" is exist in the metadatacard.";

			//Verify if metadatacard description is not set
			//---------------------------------------------
			if(metadatacard.getMetadataDescriptionText().trim().equals(dataPool.get("DescriptionText").trim()))//Get the metadatacard description text
				ExpectedProperty = ExpectedProperty+ "Description text is set in metadatacard" + metadatacard.getMetadataDescriptionText() ;

			//Verification : Verify if Metadatacard configuration is set as expected
			//----------------------------------------------------------------------
			if(ExpectedProperty.equals(""))//Verify if metadata rule is work as expected
				Log.pass("Test Case Passed.Metadata configurability is working as expected when isAdditional:true/false & isHidden:false/true is used.", driver);
			else
				Log.fail("Test Case Failed.Metadata configurability is not working as expected when isAdditional:true/false & isHidden:false/true is used. [Multiple Existing objects popout metadatacard]. FOr more details: "+ ExpectedProperty, driver);

			}//End try
		catch(Exception e){
			Log.exception(e, driver);
			}//End catch
		finally{
			Utility.quitDriver(driver);
			}//End finally
		}//End TC_38260
	    */	


	  /**
	   *  TC_38273 : Verify if alias inside an array is work as expected(isRequired property is set).
	   */
	  @Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability","bugcases"}, 
			  description = "Verify if alias inside an array is work as expected(isRequired property is set).")
	  public void TC_38273(HashMap<String,String> dataValues, String driverType) throws Exception {


		  driver = null; 

		  try {



			  driver = WebDriverUtils.getDriver();

			  //Launch the MFWA with valid credentials
			  //--------------------------------------
			  ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			  HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			  //Step-1 : Select the new 'Contact Person' from the menu bar
			  //-----------------------------------------------------------
			  homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.ContactPerson.Value);

			  Log.message("1. Selected the new  'Contact Person' object in new menu bar.");

			  //Verify if metadatacard is display with description
			  //--------------------------------------------------
			  MetadataCard metadatacard = new MetadataCard(driver);//instantiate the right pane metadatacard

			  String ExpectedProperty = "";

			  String ActualVaule = dataPool.get("DescriptionText").trim().replaceAll("\n", "").replaceAll("\r", "");
			  ActualVaule = ActualVaule.replaceAll(" ", "");


			  //Verify if metadatacard description text is set as expected
			  //----------------------------------------------------------
			  if(!metadatacard.getMetadataDescriptionText().trim().equals(ActualVaule.trim()))//get the metadatacard description text
				  ExpectedProperty = "Metadata Property Description : " + dataPool.get("DescriptionText")+ " is not set as expected:"+ metadatacard.getMetadataDescriptionText();

			  //Verify if property exists in the Metadatacard
			  //---------------------------------------------
			  if(!metadatacard.getPropertyValue(dataPool.get("PropertyName")).trim().equals(dataPool.get("PropertyValue")))//get the specified property value
				  ExpectedProperty = ExpectedProperty+ "Property "+ dataPool.get("PropertyName")+" does not set with the "+ dataPool.get("PropertyValue")+ " Proeprty value"+metadatacard.getPropertyValue(dataPool.get("PropertyName"));

			  //Verify if Property is set as required or not
			  //--------------------------------------------
			  if(!metadatacard.isRequiredProperty(dataPool.get("PropertyName")))//Verify if isRequired property is displayed
				  ExpectedProperty = ExpectedProperty+ "Property : "+dataPool.get("PropertyName")+" is not set as the required property.";

			  //Verification : Verify if Metadata Configuration is set as expected
			  //------------------------------------------------------------------
			  if(ExpectedProperty.equals(""))
				  Log.pass("Test Case Passed.Metadata configurability is working as expected when aliases specified as inside of array.");
			  else
				  Log.fail("Test Case Failed.Metadata configurability is not working as expected when isRequired:ture is used."+ ExpectedProperty, driver);

		  }//End try
		  catch(Exception e){
			  Log.exception(e, driver);
		  }//End catch
		  finally{
			  Utility.quitDriver(driver);
		  }//End finally
	  }//End TC_38273


	  /**
	   *  TC_38272 : Verify if alias inside an array is work as expected.
	   */
	  @Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability"}, 
			  description = "Verify if property is set as required when isRequired:true is set.")
	  public void TC_38272(HashMap<String,String> dataValues, String driverType) throws Exception {

		  driver = null; 

		  try {



			  driver = WebDriverUtils.getDriver();

			  //Launch the MFWA with valid credentials
			  //--------------------------------------
			  ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			  HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			  //Step-1 : Select the new 'Contact Person' from the menu bar
			  //-----------------------------------------------------------
			  homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Project.Value);//Select the document object from the Menu bar

			  Log.message("1. Selected the new  'Contact Person' object in new menu bar.");

			  //Step-2 : Set the property name and property value
			  //--------------------------------------------------
			  MetadataCard metadatacard = new MetadataCard(driver);//instantiate the right pane metadatacard
			  metadatacard.setInfo(dataPool.get("AddProperty"));

			  Log.message("2. Added the Property " + dataPool.get("AddProperty") + " in opened metadatacard.",driver);

			  String ExpectedProperty = "";

			  String ActualVaule = dataPool.get("MetadataDescription").trim().replaceAll("\n", "").replaceAll("\r", "");
			  ActualVaule = ActualVaule.replaceAll(" ", "");

			  //Verify if metadatacard description text is set as expected
			  //----------------------------------------------------------
			  if(!metadatacard.getMetadataDescriptionText().trim().equals(ActualVaule.trim()))//get the metadatacard description text
				  ExpectedProperty = "Metadata Property Description : " + dataPool.get("MetadataDescription")+ " is not set as expected:"+ metadatacard.getMetadataDescriptionText();

			  //Verify if Property is set as required or not
			  //--------------------------------------------
			  if(!metadatacard.isRequiredProperty(dataPool.get("PropertyName")))//Verify if isRequired property is displayed
				  ExpectedProperty = ExpectedProperty+ "Property : "+dataPool.get("PropertyName")+" is not set as the required property.";

			  //Verify if property description text is set as expected
			  //------------------------------------------------------
			  if(!metadatacard.getPropertyDescriptionValue(dataPool.get("PropertyName")).equals(dataPool.get("PropertyDescription")))
				  ExpectedProperty = ExpectedProperty+ "Property : "+dataPool.get("PropertyName")+" is not set with the expected description : "+dataPool.get("PropertyDescription");

			  //Verification : Verify if Metadata configuration is set as expected
			  //------------------------------------------------------------------
			  if(ExpectedProperty.equals(""))
				  Log.pass("Test Case Passed.Metadata configurability is working as expected when isRequired:ture is used",driver);
			  else
				  Log.fail("Test Case Failed.Metadata configurability is not working as expected when isRequired:ture is used."+ ExpectedProperty, driver);

		  }//End try
		  catch(Exception e){
			  Log.exception(e, driver);
		  }//End catch
		  finally{
			  Utility.quitDriver(driver);
		  }//End finally
	  }//End TC_38272


	  /**
	   *  TC_38258 : Verify if property placeholders is work as expected
	   */
	  @Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability","Placeholders"}, 
			  description = "Verify if property placeholders is work as expected")
	  public void TC_38258(HashMap<String,String> dataValues, String driverType) throws Exception {

		  driver = null; 

		  try {



			  driver = WebDriverUtils.getDriver();

			  //Launch the MFWA with valid credentials
			  //--------------------------------------
			  ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			  HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			  //Select the 'Project' object type in task pane
			  //---------------------------------------------
			  homePage.taskPanel.clickItem(Caption.ObjecTypes.Project.Value);

			  Log.message("1. Selected the 'Project' object type from the task pane");

			  //Step-2 : Set the specified property in metadatacard
			  //---------------------------------------------------
			  MetadataCard metadatacard = new MetadataCard(driver);
			  metadatacard.setInfo(dataPool.get("Props"));

			  Log.message("2. Specified properties are set in the new metadatacard.");

			  //Step-3 : Add the new property in metadatacard
			  //---------------------------------------------
			  metadatacard.addNewProperty(dataPool.get("PropertyName"));

			  Log.message("3. Added the new property : "+dataPool.get("PropertyName")+" in Project metadatacard.");

			  //Verification : Verify if property value is set as expected
			  //----------------------------------------------------------
			  if(metadatacard.getPropertyValue(dataPool.get("PropertyName")).equals(dataPool.get("PropertyValue")))
				  Log.pass("Test Case Passed.Property : "+dataPool.get("PropertyName")+" placeholders is work as expected");
			  else
				  Log.fail("Test Case Failed.Property : "+dataPool.get("PropertyName")+" placeholders is not work as expected", driver);

		  }//End try
		  catch(Exception e){
			  Log.exception(e, driver);
		  }//End catch
		  finally{
			  Utility.quitDriver(driver);
		  }//End finally
	  }//End TC_38258


	  /**
	   *  TC_38130 :  Verify if FN119 Rule is set when using an array of GUIDs in a property condition
	   */
	  @Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability","Placeholders"}, 
			  description = "Verify if FN119 Rule is set when using an array of GUIDs in a property condition")
	  public void TC_38130(HashMap<String,String> dataValues, String driverType) throws Exception {

		  driver = null; 

		  try {



			  driver = WebDriverUtils.getDriver();

			  //Launch the MFWA with valid credentials
			  //--------------------------------------
			  ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			  HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			  //Step-1 : Select the new project object from the menu bar
			  //--------------------------------------------------------
			  homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Project.Value);//Click the new menu item

			  Log.message("1. Selected the new 'Project' object type from the new menu bar.", driver);

			  //Step-2 : Add the new propety in the Project metadatacard
			  //--------------------------------------------------------
			  MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the metadatacard
			  metadatacard.addNewProperty(dataPool.get("PropertyName"));//Add the new property in metadatacard

			  Log.message("2. Added the property "+ dataPool.get("PropertyName") + " in project metadatacard.", driver);

			  //Step-3 : Set the propety value in the project metadatacard
			  //----------------------------------------------------------
			  metadatacard.setPropertyValue(dataPool.get("PropertyName"),dataPool.get("Employee1"));//Set the property value in metadatacard

			  Log.message("3. Property : "+dataPool.get("PropertyName")+" is set by property value : "+dataPool.get("PropertyValue"));

			  String ExpectedProperty = "";

			  String ActualVaule = dataPool.get("DescriptionText").trim().replaceAll("\n", "").replaceAll("\r", "");
			  ActualVaule = ActualVaule.replaceAll(" ", "");

			  //Verify if metadatacard description is set as expected
			  //-----------------------------------------------------
			  if(!metadatacard.getMetadataDescriptionText().trim().equals(ActualVaule.trim()))//get the metadatacard description text
				  ExpectedProperty = "Metadata Property Description : " + dataPool.get("DescriptionText")+ " is not set as expected:"+ metadatacard.getMetadataDescriptionText();

			  //Verify if expected property is set as required
			  //----------------------------------------------
			  if(!metadatacard.isRequiredProperty(dataPool.get("RequiredProperty")))//Verify if property is set as required
				  ExpectedProperty = "Property : "+dataPool.get("RequiredProperty")+" is not set as the required property.";

			  //Verify if property description is set as expected
			  //-----------------------------------------------------
			  if(!metadatacard.getPropertyDescriptionValue(dataPool.get("RequiredProperty")).equals(dataPool.get("PropertyDescription")))//verify if property description value is set as expected
				  ExpectedProperty = ExpectedProperty+ "Property : "+dataPool.get("RequiredProperty")+" is not set with the expected description : "+dataPool.get("PropertyDescription");

			  //Step-4 : Set the propety value in the project metadatacard
			  //----------------------------------------------------------
			  metadatacard.setPropertyValue(dataPool.get("PropertyName"),dataPool.get("Employee2"));//Set the property value in metadatacard

			  Log.message("4. Property : "+dataPool.get("PropertyName")+" is changed by property value : "+dataPool.get("Employee2"));



			  //Verify if metadatacard description is set as expected
			  //-----------------------------------------------------
			  if(!metadatacard.getMetadataDescriptionText().trim().equals(ActualVaule.trim()))//get the metadatacard description text
				  ExpectedProperty = "Metadata Property Description : " + dataPool.get("DescriptionText")+ " is not set as expected:"+ metadatacard.getMetadataDescriptionText();

			  //Verify if expected property is set as required
			  //----------------------------------------------
			  if(!metadatacard.isRequiredProperty(dataPool.get("RequiredProperty")))//Verify if property is set as required
				  ExpectedProperty = "Property : "+dataPool.get("RequiredProperty")+" is not set as the required property.";

			  //Verify if property description is set as expected
			  //-----------------------------------------------------
			  if(!metadatacard.getPropertyDescriptionValue(dataPool.get("RequiredProperty")).equals(dataPool.get("PropertyDescription")))//verify if property description value is set as expected
				  ExpectedProperty = ExpectedProperty+ "Property : "+dataPool.get("RequiredProperty")+" is not set with the expected description : "+dataPool.get("PropertyDescription");

			  //Verification : Verify if metadataconfigurability rule is work as expected
			  //-------------------------------------------------------------------------
			  if(ExpectedProperty.equals(""))//verify if metadata configurability is set as expected
				  Log.pass("Test Case Passed.Metadata configurability is working as expected when using an array of GUIDs in a property condition.");
			  else
				  Log.fail("Test Case Failed.Metadata configurability is not working as expected when using an array of GUIDs in a property condition."+ExpectedProperty, driver);

		  }//End try
		  catch(Exception e){
			  Log.exception(e, driver);
		  }//End catch
		  finally{
			  Utility.quitDriver(driver);
		  }//End finally
	  }//End TC_38130



}// End of IsAdditionalAndRequiredProperty 

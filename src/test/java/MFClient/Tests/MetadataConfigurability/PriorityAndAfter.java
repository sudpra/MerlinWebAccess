package MFClient.Tests.MetadataConfigurability;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.Utils;
import genericLibrary.WebDriverUtils;

import java.util.ArrayList;
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
public class PriorityAndAfter {

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
	 * 1.4.5.1A: Verify if Metadata configurability is working as expected when using priority & after configuration in the metadatacard [Object creation via task pane]
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability, Priority&After"}, 
			description = "Verify if Metadata configurability is working as expected when using priority & after configuration in the metadatacard [Object creation via task pane]")
	public void TC_1_4_5_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1: Click the new object link from task pane
			//---------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Customer.Value);//New object item is clicked from task pane

			Log.message("1. Customer is clicked from New item in task pane");

			//Step-2: Checks the metadatacard properties order outside the group
			//------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card

			ArrayList<String> actualProperties = new ArrayList<String>(); 
			String[] expectedProperties = null;
			int ExpectedPropertyOrder = 1;
			String ExpectedMetadata = "";

			if (!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("GroupText")))//Checks if proeprty group is displayed in the metadatacard
				throw new Exception("Property Group is not displayed in the metadatacard.");

			metadataCard.expandPropertyGroup(1, true);//Expands the property group in the metadatacard

			//Checks if properties are exisit in the metadatacard [Inside the group]
			if (!metadataCard.propertyExistsInGroup(1, dataPool.get("GrpProperty1")))
				ExpectedMetadata += " Property(" + dataPool.get("GrpProperty1") + ") is not added in the metadatacard. ";

			if (!metadataCard.propertyExistsInGroup(1, dataPool.get("GrpProperty2")))
				ExpectedMetadata += " Property(" + dataPool.get("GrpProperty2") + ") is displayed in the metadatacard. ";

			actualProperties = metadataCard.getMetadatacardProperties();//Gets the actual properties displayed in the metadatacard

			expectedProperties = dataPool.get("Properties").split(",");//Gets the expected properties order from the test data

			for (int i=0; i < actualProperties.size(); i++){
				if (!actualProperties.get(i).trim().equalsIgnoreCase(expectedProperties[i].trim()))//Checks the property in correct order
					ExpectedPropertyOrder = 0;
			}

			//Verification if properties are listed as per the priority order in the metadatacard
			//-----------------------------------------------------------------------------------
			if (ExpectedPropertyOrder != 0 & ExpectedMetadata.equalsIgnoreCase(""))//Checks if properties in expected order
				Log.pass("Test case passed. Properites in the expected order as per the priority in the new object metadatacard via task pane", driver);
			else
				Log.fail("Test case failed. Properties are not listed as per the priority order in the new object metadatacard via task pane", driver);


		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_5_1A

	/**
	 * 1.4.5.1B: Verify if Metadata configurability is working as expected when using priority & after configuration in the metadatacard [Object creation via new item menu]
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability, Priority&After"}, 
			description = "Verify if Metadata configurability is working as expected when using priority & after configuration in the metadatacard [Object creation via task pane]")
	public void TC_1_4_5_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1: Click the new object link from new item menu
			//---------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Customer.Value);//New object item is clicked from task pane

			Log.message("1. Customer is clicked from New item in new item menu");

			//Step-2: Checks the metadatacard properties order outside the group
			//------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card

			ArrayList<String> actualProperties = new ArrayList<String>(); 
			String[] expectedProperties = null;
			int ExpectedPropertyOrder = 1;
			String ExpectedMetadata = "";

			if (!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("GroupText")))//Checks if proeprty group is displayed in the metadatacard
				throw new Exception("Property Group is not displayed in the metadatacard.");

			metadataCard.expandPropertyGroup(1, true);//Expands the property group in the metadatacard

			//Checks if properties are exisit in the metadatacard [Inside the group]
			if (!metadataCard.propertyExistsInGroup(1, dataPool.get("GrpProperty1")))
				ExpectedMetadata += " Property(" + dataPool.get("GrpProperty1") + ") is not added in the metadatacard. ";

			if (!metadataCard.propertyExistsInGroup(1, dataPool.get("GrpProperty2")))
				ExpectedMetadata += " Property(" + dataPool.get("GrpProperty2") + ") is displayed in the metadatacard. ";

			actualProperties = metadataCard.getMetadatacardProperties();//Gets the actual properties displayed in the metadatacard

			expectedProperties = dataPool.get("Properties").split(",");//Gets the expected properties order from the test data

			for (int i=0; i < actualProperties.size(); i++){
				if (!actualProperties.get(i).trim().equalsIgnoreCase(expectedProperties[i].trim()))//Checks the property in correct order
					ExpectedPropertyOrder = 0;
			}

			//Verification if properties are listed as per the priority order in the metadatacard
			//-----------------------------------------------------------------------------------
			if (ExpectedPropertyOrder != 0 & ExpectedMetadata.equalsIgnoreCase(""))//Checks if properties in expected order
				Log.pass("Test case passed. Properites in the expected order as per the priority in the new object metadatacard via new item menu", driver);
			else
				Log.fail("Test case failed. Properties are not listed as per the priority order in the new object metadatacard via new item menu", driver);


		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_5_1B


	/**
	 * 1.4.5.1C: Verify if Metadata configurability is working as expected when using priority & after configuration in the metadatacard [Object creation via object property]
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability, Priority&After"}, 
			description = "Verify if Metadata configurability is working as expected when using priority & after configuration in the metadatacard [Object creation via object property]")
	public void TC_1_4_5_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-3 : Clicks the new field link in the object lookup property in the metadatacard
			//------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard of existing object

			metadataCard.clickAddValueButton(dataPool.get("ObjectType"));//Clicks the Add value button to open the new customer object metadatacard

			Log.message("3. Opened the new object(" + dataPool.get("ObjectType") +  ") metadatacard via object type property");


			//Step-4: Checks the metadatacard properties order outside the group
			//------------------------------------------------------------------
			metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card

			ArrayList<String> actualProperties = new ArrayList<String>(); 
			String[] expectedProperties = null;
			int ExpectedPropertyOrder = 1;
			String ExpectedMetadata = "";

			if (!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("GroupText")))//Checks if proeprty group is displayed in the metadatacard
				throw new Exception("Property Group is not displayed in the metadatacard.");

			metadataCard.expandPropertyGroup(1, true);//Expands the property group in the metadatacard

			//Checks if properties are exisit in the metadatacard [Inside the group]
			if (!metadataCard.propertyExistsInGroup(1, dataPool.get("GrpProperty1")))
				ExpectedMetadata += " Property(" + dataPool.get("GrpProperty1") + ") is not added in the metadatacard. ";

			if (!metadataCard.propertyExistsInGroup(1, dataPool.get("GrpProperty2")))
				ExpectedMetadata += " Property(" + dataPool.get("GrpProperty2") + ") is displayed in the metadatacard. ";

			actualProperties = metadataCard.getMetadatacardProperties();//Gets the actual properties displayed in the metadatacard

			expectedProperties = dataPool.get("Properties").split(",");//Gets the expected properties order from the test data

			for (int i=0; i < actualProperties.size(); i++){
				if (!actualProperties.get(i).trim().equalsIgnoreCase(expectedProperties[i].trim()))//Checks the property in correct order
					ExpectedPropertyOrder = 0;
			}

			//Verification if properties are listed as per the priority order in the metadatacard
			//-----------------------------------------------------------------------------------
			if (ExpectedPropertyOrder != 0 & ExpectedMetadata.equalsIgnoreCase(""))//Checks if properties in expected order
				Log.pass("Test case passed. Properites in the expected order as per the priority in the new object metadatacard via object property", driver);
			else
				Log.fail("Test case failed. Properties are not listed as per the priority order in the new object metadatacard via object property", driver);


		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_5_1C

	/**
	 * 1.4.5.2A: Verify if Metadata configurability is working as expected when using priority & after configuration in the metadatacard [Existing object]
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability, Priority&After"}, 
			description = "Verify if Metadata configurability is working as expected when using priority & after configuration in the metadatacard [Existing object]")
	public void TC_1_4_5_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-3: Checks the metadatacard properties order outside the group
			//------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the new object metadata card

			ArrayList<String> actualProperties = new ArrayList<String>(); 
			String[] expectedProperties = null;
			int ExpectedPropertyOrder = 1;
			String ExpectedMetadata = "";

			if (!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("GroupText")))//Checks if proeprty group is displayed in the metadatacard
				throw new Exception("Property Group is not displayed in the metadatacard.");

			metadataCard.expandPropertyGroup(1, true);//Expands the property group in the metadatacard

			//Checks if properties are exisit in the metadatacard [Inside the group]
			if (!metadataCard.propertyExistsInGroup(1, dataPool.get("GrpProperty1")))
				ExpectedMetadata += " Property(" + dataPool.get("GrpProperty1") + ") is not added in the metadatacard. ";

			if (!metadataCard.propertyExistsInGroup(1, dataPool.get("GrpProperty2")))
				ExpectedMetadata += " Property(" + dataPool.get("GrpProperty2") + ") is displayed in the metadatacard. ";

			actualProperties = metadataCard.getMetadatacardProperties();//Gets the actual properties displayed in the metadatacard

			expectedProperties = dataPool.get("Properties").split(",");//Gets the expected properties order from the test data

			for (int i=0; i < actualProperties.size(); i++){
				if (!actualProperties.get(i).trim().equalsIgnoreCase(expectedProperties[i].trim()))//Checks the property in correct order
					ExpectedPropertyOrder = 0;
			}

			//Verification if properties are listed as per the priority order in the metadatacard
			//-----------------------------------------------------------------------------------
			if (ExpectedPropertyOrder != 0 & ExpectedMetadata.equalsIgnoreCase(""))//Checks if properties in expected order
				Log.pass("Test case passed. Properites in the expected order as per the priority in the existing object metadatacard", driver);
			else
				Log.fail("Test case failed. Properties are not listed as per the priority order in the existing object metadatacard", driver);


		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_5_2A


	/**
	 * 1.4.5.2B: Verify if Metadata configurability is working as expected when using priority & after configuration in the metadatacard [Existing object popped out metadatacard]
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability, Priority&After"}, 
			description = "Verify if Metadata configurability is working as expected when using priority & after configuration in the metadatacard [Existing object popped out metadatacard]")
	public void TC_1_4_5_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-3: Opens the popout metadatacard of the object
			//---------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);//Clicks the properties option from task pane

			Log.message("3. Popped out metadatacard of the selected object : " + dataPool.get("Object") + " is opened via task pane.");

			//Step-4: Checks the metadatacard properties order outside the group
			//------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card

			ArrayList<String> actualProperties = new ArrayList<String>(); 
			String[] expectedProperties = null;
			int ExpectedPropertyOrder = 1;
			String ExpectedMetadata = "";

			if (!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("GroupText")))//Checks if proeprty group is displayed in the metadatacard
				throw new Exception("Property Group is not displayed in the metadatacard.");

			metadataCard.expandPropertyGroup(1, true);//Expands the property group in the metadatacard

			//Checks if properties are exisit in the metadatacard [Inside the group]
			if (!metadataCard.propertyExistsInGroup(1, dataPool.get("GrpProperty1")))
				ExpectedMetadata += " Property(" + dataPool.get("GrpProperty1") + ") is not added in the metadatacard. ";

			if (!metadataCard.propertyExistsInGroup(1, dataPool.get("GrpProperty2")))
				ExpectedMetadata += " Property(" + dataPool.get("GrpProperty2") + ") is displayed in the metadatacard. ";

			actualProperties = metadataCard.getMetadatacardProperties();//Gets the actual properties displayed in the metadatacard

			expectedProperties = dataPool.get("Properties").split(",");//Gets the expected properties order from the test data

			for (int i=0; i < actualProperties.size(); i++){
				if (!actualProperties.get(i).trim().equalsIgnoreCase(expectedProperties[i].trim()))//Checks the property in correct order
					ExpectedPropertyOrder = 0;
			}

			//Verification if properties are listed as per the priority order in the metadatacard
			//-----------------------------------------------------------------------------------
			if (ExpectedPropertyOrder != 0 & ExpectedMetadata.equalsIgnoreCase(""))//Checks if properties in expected order
				Log.pass("Test case passed. Properites in the expected order as per the priority in the existing object popped out metadatacard", driver);
			else
				Log.fail("Test case failed. Properties are not listed as per the priority order in the existing object popped out metadatacard", driver);


		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_5_2B


	/**
	 * 1.4.5.3A: Verify if Metadata configurability is working as expected when using priority & after configuration in the metadatacard [Multiple Existing objects]
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "MFN119MetadataConfigurability, Priority&After"}, 
			description = "Verify if Metadata configurability is working as expected when using priority & after configuration in the metadatacard [Multiple Existing objects]")
	public void TC_1_4_5_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));//Select the specified objects

			Log.message("2. Selected the specified objects in list view.");

			//Step-3: Checks the metadatacard properties order outside the group
			//------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the new object metadata card

			ArrayList<String> actualProperties = new ArrayList<String>(); 
			String[] expectedProperties = null;
			int ExpectedPropertyOrder = 1;
			String ExpectedMetadata = "";

			if (!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("GroupText")))//Checks if proeprty group is displayed in the metadatacard
				throw new Exception("Property Group is not displayed in the metadatacard.");

			metadataCard.expandPropertyGroup(1, true);//Expands the property group in the metadatacard

			//Checks if properties are exisit in the metadatacard [Inside the group]
			if (!metadataCard.propertyExistsInGroup(1, dataPool.get("GrpProperty1")))
				ExpectedMetadata += " Property(" + dataPool.get("GrpProperty1") + ") is not added in the metadatacard. ";

			if (!metadataCard.propertyExistsInGroup(1, dataPool.get("GrpProperty2")))
				ExpectedMetadata += " Property(" + dataPool.get("GrpProperty2") + ") is displayed in the metadatacard. ";

			actualProperties = metadataCard.getMetadatacardProperties();//Gets the actual properties displayed in the metadatacard

			expectedProperties = dataPool.get("Properties").split(",");//Gets the expected properties order from the test data

			for (int i=0; i < actualProperties.size(); i++){
				if (!actualProperties.get(i).trim().equalsIgnoreCase(expectedProperties[i].trim()))//Checks the property in correct order
					ExpectedPropertyOrder = 0;
			}

			//Verification if properties are listed as per the priority order in the metadatacard
			//-----------------------------------------------------------------------------------
			if (ExpectedPropertyOrder != 0 & ExpectedMetadata.equalsIgnoreCase(""))//Checks if properties in expected order
				Log.pass("Test case passed. Properites in the expected order as per the priority in the mult-selected existing objects metadatacard", driver);
			else
				Log.fail("Test case failed. Properties are not listed as per the priority order in the multi-selected existing objects metadatacard", driver);


		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_5_3A


	/**
	 * 1.4.5.3B: Verify if Metadata configurability is working as expected when using priority & after configuration in the metadatacard [Multiple Existing objects popped out metadatacard]
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "MFN119MetadataConfigurability, Priority&After"}, 
			description = "Verify if Metadata configurability is working as expected when using priority & after configuration in the metadatacard [Multiple Existing objects popped out metadatacard]")
	public void TC_1_4_5_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));//Select the specified objects

			Log.message("2. Selected the specified objects in list view.");

			//Step-3: Opens the popout metadatacard of the object
			//---------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);//Clicks the properties option from task pane

			Log.message("3. Popped out metadatacard of the selected object : " + dataPool.get("Object") + " is opened via task pane.");

			//Step-4: Checks the metadatacard properties order outside the group
			//------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card

			ArrayList<String> actualProperties = new ArrayList<String>(); 
			String[] expectedProperties = null;
			int ExpectedPropertyOrder = 1;
			String ExpectedMetadata = "";

			if (!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("GroupText")))//Checks if proeprty group is displayed in the metadatacard
				throw new Exception("Property Group is not displayed in the metadatacard.");

			metadataCard.expandPropertyGroup(1, true);//Expands the property group in the metadatacard

			//Checks if properties are exisit in the metadatacard [Inside the group]
			if (!metadataCard.propertyExistsInGroup(1, dataPool.get("GrpProperty1")))
				ExpectedMetadata += " Property(" + dataPool.get("GrpProperty1") + ") is not added in the metadatacard. ";

			if (!metadataCard.propertyExistsInGroup(1, dataPool.get("GrpProperty2")))
				ExpectedMetadata += " Property(" + dataPool.get("GrpProperty2") + ") is displayed in the metadatacard. ";

			actualProperties = metadataCard.getMetadatacardProperties();//Gets the actual properties displayed in the metadatacard

			expectedProperties = dataPool.get("Properties").split(",");//Gets the expected properties order from the test data

			for (int i=0; i < actualProperties.size(); i++){
				if (!actualProperties.get(i).trim().equalsIgnoreCase(expectedProperties[i].trim()))//Checks the property in correct order
					ExpectedPropertyOrder = 0;
			}

			//Verification if properties are listed as per the priority order in the metadatacard
			//-----------------------------------------------------------------------------------
			if (ExpectedPropertyOrder != 0 & ExpectedMetadata.equalsIgnoreCase(""))//Checks if properties in expected order
				Log.pass("Test case passed. Properites in the expected order as per the priority in the multi-selected existing objects popped out metadatacard", driver);
			else
				Log.fail("Test case failed. Properties are not listed as per the priority order in the existing object popped out metadatacard", driver);


		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_5_3B


	/**
	 * 1.5.3.1A: Verify if Property groups are ordered based on the priority on the metadata card so that the uppermost group has lowest priority. [Object creation via task pane]
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability, Priority&After"}, 
			description = "Verify if Property groups are ordered based on the priority on the metadata card so that the uppermost group has lowest priority. [Object creation via task pane]")
	public void TC_1_5_3_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1: Click the new object link from task pane
			//---------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Project.Value);//New object item is clicked from task pane

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card

			metadataCard.setInfo(dataPool.get("Properties"));//Sets the required class in the metadatacard

			Log.message("1. Project object type metadatacard with class("+ dataPool.get("Properties").split("::")[1] +") is opened from New item in task pane");

			//Step-2: Checks the metadatacard property grouping and priority
			//--------------------------------------------------------------
			ArrayList<String> actualGroups = new ArrayList<String>();
			ArrayList<String> actualProperties = new ArrayList<String>();
			String[] expectedGroups, expectedProperties = null;
			int ExpectedPropertyOrder = 1, ExpectedGroupOrder = 1;

			if (!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("Group1Text")) || !metadataCard.isPropertyGroupDisplayed(2, dataPool.get("Group2Text")) )//Checks if proeprty group is displayed in the metadatacard
				throw new Exception("Property Group is not displayed in the metadatacard.");

			Log.message("2. Expected property groups are displayed in the metadatacard");

			//Checks the property groups order in the metadatacard
			//-----------------------------------------------------
			actualGroups = metadataCard.getMetadatacardPropertyGroups();//Gets the available property groups name from the metadatacard
			expectedGroups = dataPool.get("Groups").split(",");//Gets the expected property groups in order from the test data

			if(actualGroups.size() == expectedGroups.length)//Checks the expected and actual property group counts are 
				for (int i=0; i < actualGroups.size(); i++){
					if (!actualGroups.get(i).trim().equalsIgnoreCase(expectedGroups[i].trim()))//Checks the property groups in correct order
						ExpectedGroupOrder = 0;
				}
			else
				ExpectedGroupOrder = 0;

			//Checks the properties order in the metadatacard
			//------------------------------------------------
			actualProperties = metadataCard.getMetadatacardProperties();//Gets the available properties from the metadatacard
			expectedProperties = dataPool.get("PropertiesOrder").split(",");//Gets the expected properties in order from the test data

			if(actualProperties.size() == expectedProperties.length)//Checks the expected and actual property group counts are 
				for (int i=0; i < actualProperties.size(); i++){
					if (!actualProperties.get(i).trim().equalsIgnoreCase(expectedProperties[i].trim()))//Checks the property groups in correct order
						ExpectedPropertyOrder = 0;
				}
			else
				ExpectedPropertyOrder = 0;


			//Verification if properties are listed as per the priority order in the metadatacard
			//-----------------------------------------------------------------------------------
			if (ExpectedPropertyOrder != 0 & ExpectedGroupOrder !=0)//Checks if properties & property groups are in expected order
				Log.pass("Test case passed. Properites & Property groups are in the expected order as per the priority in the new object metadatacard via task pane", driver);
			else
				Log.fail("Test case failed. Properites/Property groups are not listed as per the priority order in the new object metadatacard via task pane", driver);


		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_5_3_1A

	/**
	 * 1.5.3.1B: Verify if Property groups are ordered based on the priority on the metadata card so that the uppermost group has lowest priority. [Object creation via new item menu]
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability, Priority&After"}, 
			description = "Verify if Property groups are ordered based on the priority on the metadata card so that the uppermost group has lowest priority. [Object creation via new item menu]")
	public void TC_1_5_3_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1: Click the new object link from new item menu
			//---------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Project.Value);//New object item is clicked from new item menu

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card

			metadataCard.setInfo(dataPool.get("Properties"));//Sets the required class in the metadatacard

			Log.message("1. Project object type metadatacard with class("+ dataPool.get("Properties").split("::")[1] +") is opened via New item menu");

			//Step-2: Checks the metadatacard property grouping and priority
			//--------------------------------------------------------------
			ArrayList<String> actualGroups = new ArrayList<String>();
			ArrayList<String> actualProperties = new ArrayList<String>();
			String[] expectedGroups, expectedProperties = null;
			int ExpectedPropertyOrder = 1, ExpectedGroupOrder = 1;

			if (!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("Group1Text")) || !metadataCard.isPropertyGroupDisplayed(2, dataPool.get("Group2Text")) )//Checks if proeprty group is displayed in the metadatacard
				throw new Exception("Property Group is not displayed in the metadatacard.");

			Log.message("2. Expected property groups are displayed in the metadatacard");

			//Checks the property groups order in the metadatacard
			//-----------------------------------------------------
			actualGroups = metadataCard.getMetadatacardPropertyGroups();//Gets the available property groups name from the metadatacard
			expectedGroups = dataPool.get("Groups").split(",");//Gets the expected property groups in order from the test data

			if(actualGroups.size() == expectedGroups.length)//Checks the expected and actual property group counts are 
				for (int i=0; i < actualGroups.size(); i++){
					if (!actualGroups.get(i).trim().equalsIgnoreCase(expectedGroups[i].trim()))//Checks the property groups in correct order
						ExpectedGroupOrder = 0;
				}
			else
				ExpectedGroupOrder = 0;

			//Checks the properties order in the metadatacard
			//------------------------------------------------
			actualProperties = metadataCard.getMetadatacardProperties();//Gets the available properties from the metadatacard
			expectedProperties = dataPool.get("PropertiesOrder").split(",");//Gets the expected properties in order from the test data

			if(actualProperties.size() == expectedProperties.length)//Checks the expected and actual property group counts are 
				for (int i=0; i < actualProperties.size(); i++){
					if (!actualProperties.get(i).trim().equalsIgnoreCase(expectedProperties[i].trim()))//Checks the property groups in correct order
						ExpectedPropertyOrder = 0;
				}
			else
				ExpectedPropertyOrder = 0;


			//Verification if properties are listed as per the priority order in the metadatacard
			//-----------------------------------------------------------------------------------
			if (ExpectedPropertyOrder != 0 & ExpectedGroupOrder !=0)//Checks if properties & property groups are in expected order
				Log.pass("Test case passed. Properites & Property groups are in the expected order as per the priority in the new object metadatacard via new item menu", driver);
			else
				Log.fail("Test case failed. Properites/Property groups are not listed as per the priority order in the new object metadatacard via new item menu", driver);


		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_5_3_1B


	/**
	 * 1.5.3.1C: Verify if Property groups are ordered based on the priority on the metadata card so that the uppermost group has lowest priority. [Object creation via object property]
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability, Priority&After"}, 
			description = "Verify if Property groups are ordered based on the priority on the metadata card so that the uppermost group has lowest priority. [Object creation via object property]")
	public void TC_1_5_3_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-3 : Clicks the new field link in the object lookup property in the metadatacard
			//------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard of existing object

			metadataCard.clickAddValueButton(dataPool.get("ObjectType"));//Clicks the Add value button to open the new customer object metadatacard
			Utils.fluentWait(driver);

			metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card

			metadataCard.setInfo(dataPool.get("Properties"));//Sets the required class in the metadatacard

			Log.message("3. Opened the new object(" + dataPool.get("ObjectType") +  ") metadatacard with class("+ dataPool.get("Properties").split("::")[1] +") via object type property", driver);

			//Step-4: Checks the metadatacard property grouping and priority
			//--------------------------------------------------------------
			ArrayList<String> actualGroups = new ArrayList<String>();
			ArrayList<String> actualProperties = new ArrayList<String>();
			String[] expectedGroups, expectedProperties = null;
			int ExpectedPropertyOrder = 1, ExpectedGroupOrder = 1;

			metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card

			if (!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("Group1Text")) || !metadataCard.isPropertyGroupDisplayed(2, dataPool.get("Group2Text")) )//Checks if proeprty group is displayed in the metadatacard
				throw new Exception("Property Group is not displayed in the metadatacard.");

			Log.message("4. Expected property groups are displayed in the metadatacard");

			//Checks the property groups order in the metadatacard
			//-----------------------------------------------------
			actualGroups = metadataCard.getMetadatacardPropertyGroups();//Gets the available property groups name from the metadatacard
			expectedGroups = dataPool.get("Groups").split(",");//Gets the expected property groups in order from the test data

			if(actualGroups.size() == expectedGroups.length)//Checks the expected and actual property group counts are 
				for (int i=0; i < actualGroups.size(); i++){
					if (!actualGroups.get(i).trim().equalsIgnoreCase(expectedGroups[i].trim()))//Checks the property groups in correct order
						ExpectedGroupOrder = 0;
				}
			else
				ExpectedGroupOrder = 0;

			//Checks the properties order in the metadatacard
			//------------------------------------------------
			actualProperties = metadataCard.getMetadatacardProperties();//Gets the available properties from the metadatacard
			expectedProperties = dataPool.get("PropertiesOrder").split(",");//Gets the expected properties in order from the test data

			if(actualProperties.size() == expectedProperties.length)//Checks the expected and actual property group counts are 
				for (int i=0; i < actualProperties.size(); i++){
					if (!actualProperties.get(i).trim().equalsIgnoreCase(expectedProperties[i].trim()))//Checks the property groups in correct order
						ExpectedPropertyOrder = 0;
				}
			else
				ExpectedPropertyOrder = 0;


			//Verification if properties are listed as per the priority order in the metadatacard
			//-----------------------------------------------------------------------------------
			if (ExpectedPropertyOrder != 0 & ExpectedGroupOrder !=0)//Checks if properties & property groups are in expected order
				Log.pass("Test case passed. Properites & Property groups are in the expected order as per the priority in the new object metadatacard via object property", driver);
			else
				Log.fail("Test case failed. Properites/Property groups are not listed as per the priority order in the new object metadatacard via object property", driver);


		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_5_3_1C

	/**
	 * 1.5.3.2A: Verify if Property groups are ordered based on the priority on the metadata card so that the uppermost group has lowest priority. [Existing object metadata card]
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability, Priority&After"}, 
			description = "Verify if Property groups are ordered based on the priority on the metadata card so that the uppermost group has lowest priority. [Existing object metadata card]")
	public void TC_1_5_3_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-3: Checks the metadatacard properties order outside the group
			//------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the new object metadata card

			ArrayList<String> actualGroups = new ArrayList<String>();
			ArrayList<String> actualProperties = new ArrayList<String>();
			String[] expectedGroups, expectedProperties = null;
			int ExpectedPropertyOrder = 1, ExpectedGroupOrder = 1;

			if (!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("Group1Text")) || !metadataCard.isPropertyGroupDisplayed(2, dataPool.get("Group2Text")) )//Checks if proeprty group is displayed in the metadatacard
				throw new Exception("Property Group is not displayed in the metadatacard.");

			Log.message("2. Expected property groups are displayed in the metadatacard");

			//Checks the property groups order in the metadatacard
			//-----------------------------------------------------
			actualGroups = metadataCard.getMetadatacardPropertyGroups();//Gets the available property groups name from the metadatacard
			expectedGroups = dataPool.get("Groups").split(",");//Gets the expected property groups in order from the test data

			if(actualGroups.size() == expectedGroups.length)//Checks the expected and actual property group counts are 
				for (int i=0; i < actualGroups.size(); i++){
					if (!actualGroups.get(i).trim().equalsIgnoreCase(expectedGroups[i].trim()))//Checks the property groups in correct order
						ExpectedGroupOrder = 0;
				}
			else
				ExpectedGroupOrder = 0;

			//Checks the properties order in the metadatacard
			//------------------------------------------------
			actualProperties = metadataCard.getMetadatacardProperties();//Gets the available properties from the metadatacard
			expectedProperties = dataPool.get("PropertiesOrder").split(",");//Gets the expected properties in order from the test data

			if(actualProperties.size() == expectedProperties.length)//Checks the expected and actual property group counts are 
				for (int i=0; i < actualProperties.size(); i++){
					if (!actualProperties.get(i).trim().equalsIgnoreCase(expectedProperties[i].trim()))//Checks the property groups in correct order
						ExpectedPropertyOrder = 0;
				}
			else
				ExpectedPropertyOrder = 0;


			//Verification if properties are listed as per the priority order in the metadatacard
			//-----------------------------------------------------------------------------------
			if (ExpectedPropertyOrder != 0 & ExpectedGroupOrder !=0)//Checks if properties & property groups are in expected order
				Log.pass("Test case passed. Properites & Property groups are in the expected order as per the priority in the existing object metadatacard", driver);
			else
				Log.fail("Test case failed. Properites/Property groups are not listed as per the priority order in the existing object metadatacard", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_5_3_2A


	/**
	 * 1.5.3.2B: Verify if Property groups are ordered based on the priority on the metadata card so that the uppermost group has lowest priority. [Existing object popped out metadata card]
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability, Priority&After"}, 
			description = "Verify if Property groups are ordered based on the priority on the metadata card so that the uppermost group has lowest priority. [Existing object popped out metadata card]")
	public void TC_1_5_3_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-3: Opens the popout metadatacard of the object
			//---------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);//Clicks the properties option from task pane

			Log.message("3. Popped out metadatacard of the selected object : " + dataPool.get("Object") + " is opened via task pane.");

			//Step-4: Checks the metadatacard properties order outside the group
			//------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card


			ArrayList<String> actualGroups = new ArrayList<String>();
			ArrayList<String> actualProperties = new ArrayList<String>();
			String[] expectedGroups, expectedProperties = null;
			int ExpectedPropertyOrder = 1, ExpectedGroupOrder = 1;

			if (!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("Group1Text")) || !metadataCard.isPropertyGroupDisplayed(2, dataPool.get("Group2Text")) )//Checks if proeprty group is displayed in the metadatacard
				throw new Exception("Property Group is not displayed in the metadatacard.");

			Log.message("4. Expected property groups are displayed in the metadatacard");

			//Checks the property groups order in the metadatacard
			//-----------------------------------------------------
			actualGroups = metadataCard.getMetadatacardPropertyGroups();//Gets the available property groups name from the metadatacard
			expectedGroups = dataPool.get("Groups").split(",");//Gets the expected property groups in order from the test data

			if(actualGroups.size() == expectedGroups.length)//Checks the expected and actual property group counts are 
				for (int i=0; i < actualGroups.size(); i++){
					if (!actualGroups.get(i).trim().equalsIgnoreCase(expectedGroups[i].trim()))//Checks the property groups in correct order
						ExpectedGroupOrder = 0;
				}
			else
				ExpectedGroupOrder = 0;

			//Checks the properties order in the metadatacard
			//------------------------------------------------
			actualProperties = metadataCard.getMetadatacardProperties();//Gets the available properties from the metadatacard
			expectedProperties = dataPool.get("PropertiesOrder").split(",");//Gets the expected properties in order from the test data

			if(actualProperties.size() == expectedProperties.length)//Checks the expected and actual property group counts are 
				for (int i=0; i < actualProperties.size(); i++){
					if (!actualProperties.get(i).trim().equalsIgnoreCase(expectedProperties[i].trim()))//Checks the property groups in correct order
						ExpectedPropertyOrder = 0;
				}
			else
				ExpectedPropertyOrder = 0;


			//Verification if properties are listed as per the priority order in the metadatacard
			//-----------------------------------------------------------------------------------
			if (ExpectedPropertyOrder != 0 & ExpectedGroupOrder !=0)//Checks if properties & property groups are in expected order
				Log.pass("Test case passed. Properites & Property groups are in the expected order as per the priority in the existing object poppedout metadatacard", driver);
			else
				Log.fail("Test case failed. Properites/Property groups are not listed as per the priority order in the existing object poppedout metadatacard", driver);


		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_5_3_2B


	/**
	 * 1.5.3.3A: Verify if Property groups are ordered based on the priority on the metadata card so that the uppermost group has lowest priority. [Multiple Existing objects metadata card]
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "MFN119MetadataConfigurability, Priority&After"}, 
			description = "Verify if Property groups are ordered based on the priority on the metadata card so that the uppermost group has lowest priority. [Multiple Existing objects metadata card]")
	public void TC_1_5_3_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));//Select the specified objects

			Log.message("2. Selected the specified objects in list view.");

			//Step-3: Checks the metadatacard properties order outside the group
			//------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the new object metadata card

			ArrayList<String> actualGroups = new ArrayList<String>();
			ArrayList<String> actualProperties = new ArrayList<String>();
			String[] expectedGroups, expectedProperties = null;
			int ExpectedPropertyOrder = 1, ExpectedGroupOrder = 1;

			if (!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("Group1Text")) || !metadataCard.isPropertyGroupDisplayed(2, dataPool.get("Group2Text")) )//Checks if proeprty group is displayed in the metadatacard
				throw new Exception("Property Group is not displayed in the metadatacard.");

			Log.message("3. Expected property groups are displayed in the metadatacard");

			//Checks the property groups order in the metadatacard
			//-----------------------------------------------------
			actualGroups = metadataCard.getMetadatacardPropertyGroups();//Gets the available property groups name from the metadatacard
			expectedGroups = dataPool.get("Groups").split(",");//Gets the expected property groups in order from the test data

			if(actualGroups.size() == expectedGroups.length)//Checks the expected and actual property group counts are 
				for (int i=0; i < actualGroups.size(); i++){
					if (!actualGroups.get(i).trim().equalsIgnoreCase(expectedGroups[i].trim()))//Checks the property groups in correct order
						ExpectedGroupOrder = 0;
				}
			else
				ExpectedGroupOrder = 0;

			//Checks the properties order in the metadatacard
			//------------------------------------------------
			actualProperties = metadataCard.getMetadatacardProperties();//Gets the available properties from the metadatacard
			expectedProperties = dataPool.get("PropertiesOrder").split(",");//Gets the expected properties in order from the test data

			if(actualProperties.size() == expectedProperties.length)//Checks the expected and actual property group counts are 
				for (int i=0; i < actualProperties.size(); i++){
					if (!actualProperties.get(i).trim().equalsIgnoreCase(expectedProperties[i].trim()))//Checks the property groups in correct order
						ExpectedPropertyOrder = 0;
				}
			else
				ExpectedPropertyOrder = 0;


			//Verification if properties are listed as per the priority order in the metadatacard
			//-----------------------------------------------------------------------------------
			if (ExpectedPropertyOrder != 0 & ExpectedGroupOrder !=0)//Checks if properties & property groups are in expected order
				Log.pass("Test case passed. Properites & Property groups are in the expected order as per the priority in the multiple existing objects metadatacard", driver);
			else
				Log.fail("Test case failed. Properites/Property groups are not listed as per the priority order in the multiple existing objects metadatacard", driver);


		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_5_3_3A


	/**
	 * 1.5.3.3B: Verify if Property groups are ordered based on the priority on the metadata card so that the uppermost group has lowest priority. [Multiple Existing objects popped out metadata card]
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "MFN119MetadataConfigurability, Priority&After"}, 
			description = "Verify if Property groups are ordered based on the priority on the metadata card so that the uppermost group has lowest priority. [Multiple Existing objects popped out metadata card]")
	public void TC_1_5_3_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));//Select the specified objects

			Log.message("2. Selected the specified objects in list view.");

			//Step-3: Opens the popout metadatacard of the object
			//---------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);//Clicks the properties option from task pane

			Log.message("3. Popped out metadatacard of the selected object : " + dataPool.get("Object") + " is opened via task pane.");

			//Step-4: Checks the metadatacard properties order outside the group
			//------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card

			ArrayList<String> actualGroups = new ArrayList<String>();
			ArrayList<String> actualProperties = new ArrayList<String>();
			String[] expectedGroups, expectedProperties = null;
			int ExpectedPropertyOrder = 1, ExpectedGroupOrder = 1;

			if (!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("Group1Text")) || !metadataCard.isPropertyGroupDisplayed(2, dataPool.get("Group2Text")) )//Checks if proeprty group is displayed in the metadatacard
				throw new Exception("Property Group is not displayed in the metadatacard.");

			Log.message("4. Expected property groups are displayed in the metadatacard");

			//Checks the property groups order in the metadatacard
			//-----------------------------------------------------
			actualGroups = metadataCard.getMetadatacardPropertyGroups();//Gets the available property groups name from the metadatacard
			expectedGroups = dataPool.get("Groups").split(",");//Gets the expected property groups in order from the test data

			if(actualGroups.size() == expectedGroups.length)//Checks the expected and actual property group counts are 
				for (int i=0; i < actualGroups.size(); i++){
					if (!actualGroups.get(i).trim().equalsIgnoreCase(expectedGroups[i].trim()))//Checks the property groups in correct order
						ExpectedGroupOrder = 0;
				}
			else
				ExpectedGroupOrder = 0;

			//Checks the properties order in the metadatacard
			//------------------------------------------------
			actualProperties = metadataCard.getMetadatacardProperties();//Gets the available properties from the metadatacard
			expectedProperties = dataPool.get("PropertiesOrder").split(",");//Gets the expected properties in order from the test data

			if(actualProperties.size() == expectedProperties.length)//Checks the expected and actual property group counts are 
				for (int i=0; i < actualProperties.size(); i++){
					if (!actualProperties.get(i).trim().equalsIgnoreCase(expectedProperties[i].trim()))//Checks the property groups in correct order
						ExpectedPropertyOrder = 0;
				}
			else
				ExpectedPropertyOrder = 0;


			//Verification if properties are listed as per the priority order in the metadatacard
			//-----------------------------------------------------------------------------------
			if (ExpectedPropertyOrder != 0 & ExpectedGroupOrder !=0)//Checks if properties & property groups are in expected order
				Log.pass("Test case passed. Properites & Property groups are in the expected order as per the priority in the multiple existing objects poppedout metadatacard", driver);
			else
				Log.fail("Test case failed. Properites/Property groups are not listed as per the priority order in the multiple existing objects poppedout metadatacard", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_5_3_3B

}//End PriorityAndAfter

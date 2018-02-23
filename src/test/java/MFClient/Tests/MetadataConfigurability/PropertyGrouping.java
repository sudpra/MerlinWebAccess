package MFClient.Tests.MetadataConfigurability;
import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
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
public class PropertyGrouping {

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
	 * TC.1.2.1.1A : Verify if http link is displayed as plain text in Property grouping header [Object creation via Task pane]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability", "MetadataConfigurability", "PropertyGrouping"}, 
			description = "Verify if http link is displayed as plain text in Property grouping header [Object creation via Task pane]")
	public void TC_1_2_1_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Click the new object link from task pane
			//------------------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));//Clicks the new object link from task pane

			Log.message("1." + dataPool.get("ObjectType") + " is clicked from New item in task pane");

			//Step-2:Set the required values in the metadatacard
			//--------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.setInfo(dataPool.get("Properties"));//Sets the department value as Sales in the metadatacard

			Log.message("2. Property("+dataPool.get("Properties").split("::")[0]+") is set with value("+dataPool.get("Properties").split("::")[1]+") in new object metadatacard", driver);

			//Checks if properties are added and hidden in the metadatacard
			//-------------------------------------------------------------
			String ExpectedMetadata="";

			if (!metadataCard.getPropertyGroupText(1).equalsIgnoreCase(dataPool.get("GroupText")))
				ExpectedMetadata = "Expected property group text("+dataPool.get("GroupText")+" is not displayed.[Actual text:"+metadataCard.getPropertyGroupText(1)+"].";

			//Verification : Verify if metadata configurability is working as expected 
			//------------------------------------------------------------------------
			if(ExpectedMetadata.equalsIgnoreCase(""))
				Log.pass("Test Case Passed. Http link is displayed as plain text in Property grouping header [Object creation via Task pane]");
			else
				Log.fail("Test Case Failed. Http link is not displayed as plain text in Property grouping header [Object creation via Task pane]. For more details:"+ExpectedMetadata, driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_1_1A

	/**
	 * TC.1.2.1.1B : Verify if http link is displayed as plain text in Property grouping header [Object creation via New item menu]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if http link is displayed as plain text in Property grouping header  [Object creation via New item menu]")
	public void TC_1_2_1_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Click the new object link from new item menu
			//---------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//Clicks the new menu item

			Log.message("1." + dataPool.get("ObjectType") + " is clicked from new item menu bar");

			//Step-2:Set the required values in the metadatacard
			//--------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.setInfo(dataPool.get("Properties"));//Sets the department value as Sales in the metadatacard

			Log.message("2. Property("+dataPool.get("Properties").split("::")[0]+") is set with value("+dataPool.get("Properties").split("::")[1]+") in new object metadatacard", driver);

			//Checks if properties are added and hidden in the metadatacard
			//-------------------------------------------------------------
			String ExpectedMetadata="";

			if (!metadataCard.getPropertyGroupText(1).equalsIgnoreCase(dataPool.get("GroupText")))
				ExpectedMetadata = "Expected property group text("+dataPool.get("GroupText")+" is not displayed.[Actual text:"+metadataCard.getPropertyGroupText(1)+"].";

			//Verification : Verify if metadata configurability is working as expected 
			//------------------------------------------------------------------------
			if(ExpectedMetadata.equalsIgnoreCase(""))
				Log.pass("Test Case Passed. Http link is displayed as plain text in Property grouping header [Object creation via New item menu]");
			else
				Log.fail("Test Case Failed. Http link is not displayed as plain text in Property grouping header [Object creation via New item menu]. For more details:"+ExpectedMetadata, driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_1_1B

	/**
	 * TC.1.2.1.1C : Verify if http link is displayed as plain text in Property grouping header [Object creation via Object property]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if http link is displayed as plain text in Property grouping header. [Object creation via Object property]")
	public void TC_1_2_1_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

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

			//Step-4:Set the required values in the metadatacard
			//--------------------------------------------------
			metadataCard.setInfo(dataPool.get("Properties"));//Sets the department value as Sales in the metadatacard

			Log.message("4. Property("+dataPool.get("Properties").split("::")[0]+") is set with value("+dataPool.get("Properties").split("::")[1]+") in new object metadatacard", driver);

			//Checks if properties are added and hidden in the metadatacard
			//-------------------------------------------------------------
			String ExpectedMetadata="";

			if (!metadataCard.getPropertyGroupText(1).equalsIgnoreCase(dataPool.get("GroupText")))
				ExpectedMetadata = "Expected property group text("+dataPool.get("GroupText")+" is not displayed.[Actual text:"+metadataCard.getPropertyGroupText(1)+"].";

			//Verification : Verify if metadata configurability is working as expected 
			//------------------------------------------------------------------------
			if(ExpectedMetadata.equalsIgnoreCase(""))
				Log.pass("Test Case Passed. Http link is displayed as plain text in Property grouping header [Object creation via Object property]");
			else
				Log.fail("Test Case Failed. Http link is not displayed as plain text in Property grouping header [Object creation via Object property]. For more details:"+ExpectedMetadata, driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_1_1C

	/**
	 * TC_1.2.1.2A : Verify if http link is displayed as plain text in Property grouping header. [Existing object metadatacard]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if http link is displayed as plain text in Property grouping header [Existing object metadatacard].")
	public void TC_1_2_1_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to : " + viewToNavigate + " view.");

			//Step-2 : Select any existing object
			//------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			Log.message("2. Selected the specified object : " + dataPool.get("Object") + " in list view.");

			//Step-3:Set the required values in the metadatacard
			//--------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard

			metadataCard.setInfo(dataPool.get("Properties"));//Sets the department value as Sales in the metadatacard

			Log.message("3. Property("+dataPool.get("Properties").split("::")[0]+") is set with value("+dataPool.get("Properties").split("::")[1]+") in existing object metadatacard in rightpane", driver);

			//Checks if properties are added and hidden in the metadatacard
			//-------------------------------------------------------------
			String ExpectedMetadata="";

			if (!metadataCard.getPropertyGroupText(1).equalsIgnoreCase(dataPool.get("GroupText")))
				ExpectedMetadata = "Expected property group text("+dataPool.get("GroupText")+" is not displayed.[Actual text:"+metadataCard.getPropertyGroupText(1)+"].";

			//Verification : Verify if metadata configurability is working as expected 
			//------------------------------------------------------------------------
			if(ExpectedMetadata.equalsIgnoreCase(""))
				Log.pass("Test Case Passed. Http link is displayed as plain text in Property grouping header [Existing object metadatacard]");
			else
				Log.fail("Test Case Failed. Http link is not displayed as plain text in Property grouping header [Existing object metadatacard]. For more details:"+ExpectedMetadata, driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_1_2A

	/**
	 * TC.1.2.1.2B : Verify if http link is displayed as plain text in Property grouping header. [Existing object popout metadata card].
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if http link is displayed as plain text in Property grouping header. [Existing object popout metadata card].")
	public void TC_1_2_1_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to : " + viewToNavigate + " view.");

			//Step-2 : Select any existing object
			//------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			Log.message("2. Selected the specified object : " + dataPool.get("Object") + " in list view.");

			//Step-3: Open the popout metadatacard of the object
			//--------------------------------------------------
			homePage.taskPanel.clickItem("Properties");//Clicks the properties option from task pane

			Log.message("3. Existing object popped out metadatacard is opened via task pane");

			//Step-4:Set the required values in the metadatacard
			//--------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.setInfo(dataPool.get("Properties"));//Sets the department value as Sales in the metadatacard

			Log.message("4. Property("+dataPool.get("Properties").split("::")[0]+") is set with value("+dataPool.get("Properties").split("::")[1]+") in existing object popout metadatacard", driver);

			//Checks if properties are added and hidden in the metadatacard
			//-------------------------------------------------------------
			String ExpectedMetadata="";

			if (!metadataCard.getPropertyGroupText(1).equalsIgnoreCase(dataPool.get("GroupText")))
				ExpectedMetadata = "Expected property group text("+dataPool.get("GroupText")+" is not displayed.[Actual text:"+metadataCard.getPropertyGroupText(1)+"].";

			//Verification : Verify if metadata configurability is working as expected 
			//------------------------------------------------------------------------
			if(ExpectedMetadata.equalsIgnoreCase(""))
				Log.pass("Test Case Passed. Http link is displayed as plain text in Property grouping header [Existing object popout metadata card]");
			else
				Log.fail("Test Case Failed. Http link is not displayed as plain text in Property grouping header [Existing object popout metadata card]. For more details:"+ExpectedMetadata, driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_1_2B

	/**
	 * TC_1.2.1.3A : Verify if http link is displayed as plain text in Property grouping header [Multiple Existing objects metadata card].
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if http link is displayed as plain text in Property grouping header [Multiple Existing objects metadata card].")
	public void TC_1_2_1_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to : " + viewToNavigate + " view.");

			//Step-2 : Select any existing object
			//------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));//Selects the multiple objects in the view

			Log.message("2. Multiple objects selected in the list view.");

			//Step-3: Set the required values in the metadatacard
			//----------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard of existing object

			metadataCard.setInfo(dataPool.get("Properties"));//Sets the department value as Sales in the metadatacard

			Log.message("3. Property("+dataPool.get("Properties").split("::")[0]+") is set with value("+dataPool.get("Properties").split("::")[1]+") in existing objects metadatacard in rightpane", driver);

			//Checks if properties are added and hidden in the metadatacard
			//-------------------------------------------------------------
			String ExpectedMetadata="";

			driver.switchTo().defaultContent();

			if (!metadataCard.getPropertyGroupText(1).equalsIgnoreCase(dataPool.get("GroupText")))
				ExpectedMetadata = "Expected property group text("+dataPool.get("GroupText")+" is not displayed.[Actual text:"+metadataCard.getPropertyGroupText(1)+"].";

			//Verification : Verify if metadata configurability is working as expected 
			//------------------------------------------------------------------------
			if(ExpectedMetadata.equalsIgnoreCase(""))
				Log.pass("Test Case Passed. Http link is displayed as plain text in Property grouping header [Multiple Existing objects metadata card]");
			else
				Log.fail("Test Case Failed. Http link is not displayed as plain text in Property grouping header [Multiple Existing objects metadata card]. For more details:"+ExpectedMetadata, driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_1_3A

	/**
	 * TC_1.2.1.3B : Verify if http link is displayed as plain text in Property grouping header [Multiple Existing objects popout metadata card].
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if http link is displayed as plain text in Property grouping header [Multiple Existing objects popout metadata card].")
	public void TC_1_2_1_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to : " + viewToNavigate + " view.");

			//Step-2 : Select multiple existing objects
			//------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));//Selects the multiple objects in the view

			Log.message("2. Multiple objects selected in the list view.");

			//Step-3: Open the popout metadatacard of the object
			//--------------------------------------------------
			homePage.taskPanel.clickItem("Properties");//Clicks the properties option from task pane

			Log.message("3. Existing object popped out metadatacard is opened via task pane");

			//Step-4: Set the required values in the metadatacard
			//----------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard of existing object

			metadataCard.setInfo(dataPool.get("Properties"));//Sets the department value as Sales in the metadatacard

			Log.message("4. Property("+dataPool.get("Properties").split("::")[0]+") is set with value("+dataPool.get("Properties").split("::")[1]+") in existing objects popout metadatacard", driver);

			//Checks if properties are added and hidden in the metadatacard
			//-------------------------------------------------------------
			String ExpectedMetadata="";

			if (!metadataCard.getPropertyGroupText(1).equalsIgnoreCase(dataPool.get("GroupText")))
				ExpectedMetadata = "Expected property group text("+dataPool.get("GroupText")+" is not displayed.[Actual text:"+metadataCard.getPropertyGroupText(1)+"].";

			//Verification : Verify if metadata configurability is working as expected 
			//------------------------------------------------------------------------
			if(ExpectedMetadata.equalsIgnoreCase(""))
				Log.pass("Test Case Passed. Http link is displayed as plain text in Property grouping header [Multiple Existing objects popout metadata card]");
			else
				Log.fail("Test Case Failed. Http link is not displayed as plain text in Property grouping header [Multiple Existing objects popout metadata card]. For more details:"+ExpectedMetadata, driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_1_3B

	/**
	 * TC.1.4.6.2.1A : Verify if property is displayed when isRequired:false with existing required property [Object creation via Task pane].
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isRequired"}, 
			description = "Verify if property is displayed when isRequired:false with existing required property [Object creation via Task pane].")
	public void TC_1_4_6_2_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Select the lookup property in the metadatacard
			//-------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Customer.Value);

			Log.message("1. Selected the new 'Customer' object from the task panel.", driver);

			//Step - 2 : Instantiate the metadatacard
			//----------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the new metadatacard

			String propValue = " ";

			//Get the group text from the metadatacard
			//----------------------------------------
			String groupText = metadataCard.getPropertyGroupText(1);

			//Verify if group text is displayed correctly or not
			//--------------------------------------------------
			if(!groupText.equalsIgnoreCase("First"))
				throw new Exception("Metadatacard is not set with the specified Group : 'First' Group.");

			//Verify if group is collapsed or not
			//----------------------------------
			metadataCard.expandPropertyGroup(1,true);//Expand the group if it is collapsed

			Log.message("2. Expanded the group in metadatacard.", driver);

			//Verify if property is existing under the group header
			//-----------------------------------------------------
			if(!metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName1")))
				propValue = propValue+"Expected property : " +dataPool.get("PropertyName1")+ " is not existing in the metadatacard grouping.";

			if(!metadataCard.isRequiredProperty(dataPool.get("PropertyName1")))
				propValue  = propValue+ "Expected propety :  " +dataPool.get("PropertyName1")+  " is not set as required property.";

			if(propValue.equals(" "))
				Log.pass("Test Case Passed.All the Properties are not displayed when isHidden:(true/false) is set and its work as expected.");
			else
				Log.fail("Test Case Failed.Properties are displayed even though isHidden rule is set." +propValue , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_6_2_1A

	/**
	 * TC.1.4.6.2.1B : Verify if property is displayed when isRequired:false with existing required property [Object creation via Task pane].
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isRequired"}, 
			description = "Verify if property is displayed when  isRequired:false with existing required property [Object creation via New item menu].")
	public void TC_1_4_6_2_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Select the lookup property in the metadatacard
			//-------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Customer.Value);

			Log.message("1. Selected the new 'Customer' object from the Menu bar.", driver);

			//Step - 2 : Instantiate the metadatacard
			//----------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the new metadatacard

			String propValue = " ";

			//Get the group text from the metadatacard
			//----------------------------------------
			String groupText = metadataCard.getPropertyGroupText(1);

			//Verify if group text is displayed correctly or not
			//--------------------------------------------------
			if(!groupText.equalsIgnoreCase("First"))
				throw new Exception("Metadatacard is not set with the specified Group : 'First' Group.");

			//Verify if group is collapsed or not
			//----------------------------------
			metadataCard.expandPropertyGroup(1,true);//Expand the group if it is collapsed

			Log.message("2. Expanded the group in metadatacard.", driver);

			//Verify if property is existing under the group header
			//-----------------------------------------------------
			if(!metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName1")))
				propValue = propValue+"Expected property : " +dataPool.get("PropertyName1")+ " is not existing in the metadatacard grouping.";

			if(!metadataCard.isRequiredProperty(dataPool.get("PropertyName1")))
				propValue  = propValue+ "Expected propety :  " +dataPool.get("PropertyName1")+  " is not set as required property.";

			if(propValue.equals(" "))
				Log.pass("Test Case Passed.All the Properties are not displayed when isHidden:(true/false) is set and its work as expected.");
			else
				Log.fail("Test Case Failed.Properties are displayed even though isHidden rule is set." +propValue , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_6_2_1B

	/**
	 * TC.1.4.6.2.1C : Verify if property is displayed when isRequired:false with existing required property [Object creation via Task pane].
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isRequired"}, 
			description = "Verify if property is displayed when isRequired:false with existing required property [Object creation via New item menu].")
	public void TC_1_4_6_2_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.addNewProperty("Customer");//if property is not existing add the new customer property

			Log.message("3. 'Customer' Property is added to the new metadatacard", driver);

			//Step-4 : Select the existing property in metadatacard
			//-----------------------------------------------------
			metadatacard.clickAddValueButton("Customer");//Clicks the Add value button

			Log.message("4. Selected the Addfield property in the metadatacard.", driver);

			//Step - 5 : Instantiate the metadatacard
			//----------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the new metadatacard

			String propValue = " ";//Instantiate the string prop value

			//Get the group text from the metadatacard
			//----------------------------------------
			String groupText = metadataCard.getPropertyGroupText(1);//Get the group text in the from the metadatacard

			//Verify if group text is displayed correctly or not
			//--------------------------------------------------
			if(!groupText.equalsIgnoreCase("First"))
				throw new Exception("Metadatacard is not set with the specified Group : 'First' Group.");

			//Verify if group is collapsed or not
			//----------------------------------
			metadataCard.expandPropertyGroup(1,true);//Expand the group if it is collapsed

			Log.message("5. Expanded the group which displayed in the metadatacard.", driver);

			//Verify if property is existing under the group header
			//-----------------------------------------------------
			if(!metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName1")))//Verify if property is exists or not
				propValue = propValue+"Expected property : " +dataPool.get("PropertyName1")+ " is not existing in the metadatacard grouping.";

			if(!metadataCard.isRequiredProperty(dataPool.get("PropertyName1")))//Verify if property is set as required or not
				propValue  = propValue+ "Expected propety :  " +dataPool.get("PropertyName1")+  " is not set as required property.";

			//Verification : Verify if property values are set as expected
			//------------------------------------------------------------
			if(propValue.equals(" "))
				Log.pass("Test Case Passed.All the Properties are not displayed when isHidden:(true/false) is set and its work as expected.");
			else
				Log.fail("Test Case Failed.Properties are displayed even though isHidden rule is set." +propValue , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_6_2_1C

	/**
	 * TC.1.4.6.2.2A : Verify if property is displayed when isRequired:false with existing required property [Object creation via Task pane].
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isRequired"}, 
			description = "Verify if property is displayed when  isRequired:false with existing required property [Existing object].")
	public void TC_1_4_6_2_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to any view and select the any existing 
			//---------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), ""); 

			Log.message("1. Navigated to Specified '" + viewToNavigate + "' search view.");

			//Step-2 : Select the specified object type from the search view
			//--------------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectType"));

			Log.message("2. Selected the specified object (" + dataPool.get("ObjectType") +") from the search view.", driver);

			//Step - 3 : Instantiate the metadatacard
			//----------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver,true);//Instantiate the new metadatacard

			String propValue = " ";//Instantiate the string prop value

			//Get the group text from the metadatacard
			//----------------------------------------
			String groupText = metadataCard.getPropertyGroupText(1);//Get the group text in the from the metadatacard

			//Verify if group text is displayed correctly or not
			//--------------------------------------------------
			if(!groupText.equalsIgnoreCase("First"))
				throw new Exception("Metadatacard is not set with the specified Group : 'First' Group.");

			//Verify if group is collapsed or not
			//----------------------------------
			metadataCard.expandPropertyGroup(1,true);//Expand the group if it is collapsed

			Log.message("3. Expanded the group which displayed in the metadatacard.", driver);

			//Verify if property is existing under the group header
			//-----------------------------------------------------
			if(!metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName1")))//Verify if property is exists or not
				propValue = propValue+"Expected property : " +dataPool.get("PropertyName1")+ " is not existing in the metadatacard grouping.";

			if(!metadataCard.isRequiredProperty(dataPool.get("PropertyName1")))//Verify if property is set as required or not
				propValue  = propValue+ "Expected propety :  " +dataPool.get("PropertyName1")+  " is not set as required property";

			//Verification : Verify if property values are set as expected
			//------------------------------------------------------------
			if(propValue.equals(" "))
				Log.pass("Test Case Passed.All the Properties are not displayed when isHidden:(true/false) is set and its work as expected.");
			else
				Log.fail("Test Case Failed.Properties are displayed even though isHidden rule is set." +propValue , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC.1.4.6.2.2A

	/**
	 * TC.1.4.6.2.2B : Verify if property is displayed when  isRequired:false with existing required property [Existing object popout metadata card].
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isRequired"}, 
			description = "Verify if property is displayed when  isRequired:false with existing required property [Existing object popout metadata card].")
	public void TC_1_4_6_2_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to any view and select the any existing 
			//---------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), ""); 

			Log.message("1. Navigate to Specified '" + viewToNavigate + "' search view.");

			//Step-2 : Select the specified object type from the search view
			//--------------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectType"));

			Log.message("2. Selected the specified object (" + dataPool.get("ObjectType") +") from the search view.", driver);

			//Step - 3 : Select the 'Properties' option from the task panel
			//------------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			Log.message("3. Selected the 'Properties' option from the task panel.");

			//Step-4 : Instantiate the popout metadatacard
			//--------------------------------------------
			MetadataCard  metadataCard = new MetadataCard(driver);//Instantiate the pop-out metadatacard
			String propValue = " ";//Instantiate the string prop value

			//Get the group text from the metadatacard
			//----------------------------------------
			String groupText = metadataCard.getPropertyGroupText(1);//Get the group text in the from the metadatacard

			//Verify if group text is displayed correctly or not
			//--------------------------------------------------
			if(!groupText.equalsIgnoreCase("First"))
				throw new Exception("Metadatacard is not set with the specified Group : 'First' Group.");

			//Verify if group is collapsed or not
			//----------------------------------
			metadataCard.expandPropertyGroup(1,true);//Expand the group if it is collapsed

			Log.message("4. Expanded the group which displayed in the metadatacard.", driver);

			//Verify if property is existing under the group header
			//-----------------------------------------------------
			if(!metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName1")))//Verify if property is exists or not
				propValue = propValue+"Expected property : " +dataPool.get("PropertyName1")+ " is not existing in the metadatacard grouping.";

			if(!metadataCard.isRequiredProperty(dataPool.get("PropertyName1")))//Verify if property is set as required or not
				propValue  = propValue+ "Expected propety :  " +dataPool.get("PropertyName1")+  " is not set as required property";

			//Verification : Verify if property values are set as expected
			//------------------------------------------------------------
			if(propValue.equals(" "))
				Log.pass("Test Case Passed.All the Properties are not displayed when isHidden:(true/false) is set and its work as expected.");
			else
				Log.fail("Test Case Failed.Properties are displayed even though isHidden rule is set." +propValue , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC.1.4.6.2.2B

	/**
	 * TC.1.4.6.2.3A : Verify if property is displayed when  isRequired:false with existing required property [Existing object popout metadata card].
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "FN119MetadataConfigurability - isRequired"}, 
			description = "Verify if property is displayed when  isRequired:false with existing required property [Multiple Existing objects].")
	public void TC_1_4_6_2_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to any view and select the any existing 
			//---------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), ""); 

			Log.message("1. Navigate to Specified '" + viewToNavigate + "' search view.");

			//Step-2 : Select the specified object type from the search view
			//--------------------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));

			Log.message("2. Selected the specified object (" + dataPool.get("Objects") +") from the search view.", driver);

			//Step - 3 : Select the 'Properties' option from the task panel
			//------------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			Log.message("3. Selected the 'Properties' option from the task panel.");

			//Step-4 : Instantiate the popout metadatacard
			//--------------------------------------------
			MetadataCard  metadataCard = new MetadataCard(driver);//Instantiate the pop-out metadatacard
			String propValue = " ";//Instantiate the string prop value

			//Get the group text from the metadatacard
			//----------------------------------------
			String groupText = metadataCard.getPropertyGroupText(1);//Get the group text in the from the metadatacard

			//Verify if group text is displayed correctly or not
			//--------------------------------------------------
			if(!groupText.equalsIgnoreCase("First"))
				throw new Exception("Metadatacard is not set with the specified Group : 'First' Group.");

			//Verify if group is collapsed or not
			//----------------------------------
			metadataCard.expandPropertyGroup(1,true);//Expand the group if it is collapsed

			Log.message("4. Expanded the group which displayed in the metadatacard.", driver);

			//Verify if property is existing under the group header
			//-----------------------------------------------------
			if(!metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName1")))//Verify if property is exists or not
				propValue = propValue+"Expected property : " +dataPool.get("PropertyName1")+ " is not existing in the metadatacard grouping.";

			if(!metadataCard.isRequiredProperty(dataPool.get("PropertyName1")))//Verify if property is set as required or not
				propValue  = propValue+ "Expected propety :  " +dataPool.get("PropertyName1")+  " is not set as required property.";

			//Verification : Verify if property values are set as expected
			//------------------------------------------------------------
			if(propValue.equals(" "))
				Log.pass("Test Case Passed.All the Properties are not displayed when isHidden:(true/false) is set and its work as expected.");
			else
				Log.fail("Test Case Failed.Properties are displayed even though isHidden rule is set." +propValue , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC.1.4.6.2.3A

	/**
	 * TC.1.4.6.2.3B : Verify if property is displayed when  isRequired:false with existing required property [Multiple Existing objects popout metadata card].
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "FN119MetadataConfigurability - isRequired"}, 
			description = "Verify if property is displayed when  isRequired:false with existing required property [Multiple Existing objects popout metadata card].")
	public void TC_1_4_6_2_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to any view and select the any existing 
			//---------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), ""); 

			Log.message("1. Navigate to Specified '" + viewToNavigate + "' search view.");

			//Step-2 : Select the specified object type from the search view
			//--------------------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));

			Log.message("2. Selected the specified object (" + dataPool.get("Objects") +") from the search view.", driver);

			//Step - 3 : Select the 'Properties' option from the task panel
			//------------------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value);

			Log.message("3. Selected the 'Properties' option from the Operations menu.");

			//Step-4 : Instantiate the popout metadatacard
			//--------------------------------------------
			MetadataCard  metadataCard = new MetadataCard(driver);//Instantiate the pop-out metadatacard
			String propValue = " ";//Instantiate the string prop value

			//Get the group text from the metadatacard
			//----------------------------------------
			String groupText = metadataCard.getPropertyGroupText(1);//Get the group text in the from the metadatacard

			//Verify if group text is displayed correctly or not
			//--------------------------------------------------
			if(!groupText.equalsIgnoreCase("First"))
				throw new Exception("Metadatacard is not set with the specified Group : 'First' Group.");

			//Verify if group is collapsed or not
			//----------------------------------
			metadataCard.expandPropertyGroup(1,true);//Expand the group if it is collapsed

			Log.message("4. Expanded the group which displayed in the metadatacard.", driver);

			//Verify if property is existing under the group header
			//-----------------------------------------------------
			if(!metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName1")))//Verify if property is exists or not
				propValue = propValue+"Expected property : " +dataPool.get("PropertyName1")+ " is not existing in the metadatacard grouping.";

			if(!metadataCard.isRequiredProperty(dataPool.get("PropertyName1")))//Verify if property is set as required or not
				propValue  = propValue+ "Expected propety :  " +dataPool.get("PropertyName1")+  " is not set as required property.";

			//Verification : Verify if property values are set as expected
			//------------------------------------------------------------
			if(propValue.equals(" "))
				Log.pass("Test Case Passed.All the Properties are not displayed when isHidden:(true/false) is set and its work as expected.");
			else
				Log.fail("Test Case Failed.Properties are displayed even though isHidden rule is set." +propValue , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC.1.4.6.2.3B

	/**
	 * TC.1.4.7.1.1A : Verify if  property in property group is changed as required or not when isRequired:(true/false) [Object creation via Task pane] is set.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isRequired"}, 
			description = "Verify if  property in property group is changed as required or not when isRequired:(true/false) [Object creation via Task pane] is set.")
	public void TC_1_4_7_1_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Select the lookup property in the metadatacard
			//-------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Customer.Value);

			Log.message("1. Selected the new 'Customer' object from the task panel.", driver);

			//Step - 2 : Instantiate the metadatacard
			//----------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the new metadatacard

			String propValue = " ";

			//Get the group text from the metadatacard
			//----------------------------------------
			String groupText = metadataCard.getPropertyGroupText(1);

			//Verify if group text is displayed correctly or not
			//--------------------------------------------------
			if(!groupText.equalsIgnoreCase("First"))
				throw new Exception("Metadatacard is not set with the specified Group : 'First' Group.");


			//Verify if group is collapsed or not
			//----------------------------------
			metadataCard.expandPropertyGroup(1,true);//Expand the group if it is collapsed

			Log.message("2. Expanded the group in metadatacard.", driver);


			//Verify if property is existing under the group header
			//-----------------------------------------------------
			if(!metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName1")))
				propValue = propValue+"Expected property : " +dataPool.get("PropertyName1")+ " is not existing in the metadatacard grouping.";

			//Verify if specified property is set as not required
			//------------------------------------------------
			if(!metadataCard.isRequiredProperty(dataPool.get("PropertyName1")))
				propValue  = propValue+ "Expected propety :  " +dataPool.get("PropertyName1")+  " is not set as required property";

			//Verify if specified property is set as required
			//---------------------------------------------------
			if(metadataCard.isRequiredProperty(dataPool.get("PropertyName2")))
				propValue = propValue + "Expected property : "+ dataPool.get("PropertyName2") + " is set as required property.";

			if(propValue.equals(" "))
				Log.pass("Test Case Passed. Metadata configurability is working as expected when isRequired:(true/false) is set.[Object creation via Task pane]");
			else
				Log.fail("Test Case Failed. Metadata configurability is not working as expected when isRequired:(true/false) is set.[Object creation via Task pane] For more details:" +propValue , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_7_1_1A

	/**
	 * TC.1.4.7.1.1B : Verify if  property in property group is changed as required or not when isRequired:(true/false) [Object creation via Task pane] is set.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isRequired"}, 
			description = "Verify if property in property group is changed as required or not when isRequired:(true/false) [Object creation via Task pane] is set.")
	public void TC_1_4_7_1_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Select the lookup property in the metadatacard
			//-------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Customer.Value);

			Log.message("1. Selected the new 'Customer' object from the Operations menu.", driver);

			//Step - 2 : Instantiate the metadatacard
			//----------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the new metadatacard

			String propValue = " ";

			//Get the group text from the metadatacard
			//----------------------------------------
			String groupText = metadataCard.getPropertyGroupText(1);

			//Verify if group text is displayed correctly or not
			//--------------------------------------------------
			if(!groupText.equalsIgnoreCase("First"))
				throw new Exception("Metadatacard is not set with the specified Group : 'First' Group.");

			//Verify if group is collapsed or not
			//----------------------------------
			metadataCard.expandPropertyGroup(1,true);//Expand the group if it is collapsed

			Log.message("2. Expanded the group in metadatacard.", driver);

			//Verify if property is existing under the group header
			//-----------------------------------------------------
			if(!metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName1")))
				propValue = propValue+"Expected property : " +dataPool.get("PropertyName1")+ " is not existing in the metadatacard grouping.";

			if(!metadataCard.isRequiredProperty(dataPool.get("PropertyName1")))
				propValue  = propValue+ "Expected propety :  " +dataPool.get("PropertyName1")+  " is not set as required property.";

			if(metadataCard.isRequiredProperty(dataPool.get("PropertyName2")))
				propValue = propValue + "Expected property : "+ dataPool.get("PropertyName2") + " is set as required property.";

			if(propValue.equals(" "))
				Log.pass("Test Case Passed. Metadata configurability is working as expected when isRequired:(true/false) is set.[Object creation via New item menu]");
			else
				Log.fail("Test Case Failed. Metadata configurability is not working as expected when isRequired:(true/false) is set.[Object creation via New item menu] For more details:" +propValue , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_7_1_1B

	/**
	 * TC.1.4.7.1.1C : Verify if property in property group is changed as required or not when isRequired:(true/false) [Object creation via Object Property].
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isRequired"}, 
			description = "Verify if property in property group is changed as required or not when isRequired:(true/false) [Object creation via Object Property].")
	public void TC_1_4_7_1_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to any view and select the any existing 
			//---------------------------------------------------------
			homePage.searchPanel.clickSearchBtn(driver);//Click the search button

			Log.message("1. Navigate to any search view.");

			//Step-2 : Select the specified object type from the search view
			//--------------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectType"));

			Log.message("2. Selected the specified object (" + dataPool.get("ObjectType") +") from the search view.", driver);

			//Step-3 : Select the lookup property in the metadatacard
			//-------------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver,true);//Instantiate the new metadatacard
			metadatacard.addNewProperty("Customer");//if property is not existing add the new customer property

			Log.message("3. 'Customer' Property is added to the new metadatacard", driver);

			//Step-4 : Select the existing property in metadatacard
			//-----------------------------------------------------
			metadatacard.clickAddValueButton("Customer");//Clicks the Add value button

			Log.message("4. Selected the Addfield property in the metadatacard.", driver);

			//Step - 5 : Instantiate the metadatacard
			//----------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the new metadatacard

			String propValue = " ";

			//Get the group text from the metadatacard
			//----------------------------------------
			String groupText = metadataCard.getPropertyGroupText(1);

			//Verify if group text is displayed correctly or not
			//--------------------------------------------------
			if(!groupText.equalsIgnoreCase("First"))
				throw new Exception("Metadatacard is not set with the specified Group : 'First' Group.");

			//Verify if group is collapsed or not
			//----------------------------------
			metadataCard.expandPropertyGroup(1,true);//Expand the group if it is collapsed

			Log.message("5. Expanded the group in metadatacard.", driver);

			//Verify if property is existing under the group header
			//-----------------------------------------------------
			if(!metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName1")))
				propValue = propValue+"Expected property : " +dataPool.get("PropertyName1")+ " is not existing in the metadatacard grouping.";

			//Verify if property is existing under the group header
			//-----------------------------------------------------
			if(!metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName2")))
				propValue = propValue+"Expected property : " +dataPool.get("PropertyName1")+ " is not existing in the metadatacard grouping.";

			//Verify if property name is set as is required or not
			//----------------------------------------------------
			if(!metadataCard.isRequiredProperty(dataPool.get("PropertyName1")))
				propValue  = propValue+ "Expected propety :  " +dataPool.get("PropertyName1")+  " is not set as required property.";

			//Verify if property name is set as is required or not
			//----------------------------------------------------
			if(metadataCard.isRequiredProperty(dataPool.get("PropertyName2")))
				propValue = propValue + "Expected property : "+ dataPool.get("PropertyName2") + " is set as required property.";

			//Verification : Verify if property is set as expected
			//----------------------------------------------------
			if(propValue.equals(" "))
				Log.pass("Test Case Passed. Metadata configurability is working as expected when isRequired:(true/false) is set.[Object creation via Object property]");
			else
				Log.fail("Test Case Failed. Metadata configurability is not working as expected when isRequired:(true/false) is set.[Object creation via Object property] For more details:" +propValue , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_7_1_1C

	/**
	 * TC.1.4.7.1.2A : Verify if property in property group is changed as required or not when isRequired:(true/false) [Existing object] is set.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isRequired"}, 
			description = "Verify if property in property group is changed as required or not when isRequired:(true/false) [Existing object] is set.")
	public void TC_1_4_7_1_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to any view and select the any existing 
			//---------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), ""); 

			Log.message("1. Navigate to Specified '" + viewToNavigate + "' search view.");

			//Step-2 : Select the specified object type from the search view
			//--------------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectType"));

			Log.message("2. Selected the specified object (" + dataPool.get("ObjectType") +") from the search view.", driver);

			//Step - 3 : Instantiate the metadatacard
			//----------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver,true);//Instantiate the new metadatacard

			String propValue = " ";

			//Get the group text from the metadatacard
			//----------------------------------------
			String groupText = metadataCard.getPropertyGroupText(1);

			//Verify if group text is displayed correctly or not
			//--------------------------------------------------
			if(!groupText.equalsIgnoreCase("First"))
				throw new Exception("Metadatacard is not set with the specified Group : 'First' Group.");

			//Verify if group is collapsed or not
			//----------------------------------
			metadataCard.expandPropertyGroup(1,true);//Expand the group if it is collapsed

			Log.message("3. Expanded the group in metadatacard.", driver);

			//Verify if property is existing under the group header
			//-----------------------------------------------------
			if(!metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName1")))
				propValue = propValue+"Expected property : " +dataPool.get("PropertyName1")+ " is not existing in the metadatacard grouping.";

			//Verify if property is existing under the group header
			//-----------------------------------------------------
			if(!metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName2")))
				propValue = propValue+"Expected property : " +dataPool.get("PropertyName1")+ " is not existing in the metadatacard grouping.";

			//Verify if property name is set as is required or not
			//----------------------------------------------------
			if(!metadataCard.isRequiredProperty(dataPool.get("PropertyName1")))
				propValue  = propValue+ "Expected propety :  " +dataPool.get("PropertyName1")+  " is not set as required property.";

			//Verify if property name is set as is required or not
			//----------------------------------------------------
			if(metadataCard.isRequiredProperty(dataPool.get("PropertyName2")))
				propValue = propValue + "Expected property : "+ dataPool.get("PropertyName2") + " is set as required property.";

			//Verification : Verify if property is set as expected
			//----------------------------------------------------
			if(propValue.equals(" "))
				Log.pass("Test Case Passed. Metadata configurability is working as expected when isRequired:(true/false) is set.[Existing object]");
			else
				Log.fail("Test Case Failed. Metadata configurability is not working as expected when isRequired:(true/false) is set.[Existing object] For more details:" +propValue , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC.1.4.7.1.2A

	/**
	 * TC.1.4.7.1.2B : Verify if property in property group is changed as required or not when isRequired:(true/false) [Existing object popped out metadatacard] is set.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isRequired"}, 
			description = "Verify if property in property group is changed as required or not when isRequired:(true/false) [Existing object popped out metadatacard] is set.")
	public void TC_1_4_7_1_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to any view and select the any existing 
			//---------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), ""); 

			Log.message("1. Navigate to Specified '" + viewToNavigate + "' search view.");

			//Step-2 : Select the specified object type from the search view
			//--------------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectType")))//Selects the object in the view
				throw new SkipException("Object("+dataPool.get("ObjectType")+") is not selected");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);//Clicks the properties option from task pane

			Log.message("2. Selected the specified object (" + dataPool.get("ObjectType") +") from the search view.", driver);

			//Step - 3 : Instantiate the metadatacard
			//----------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the new metadatacard

			String propValue = " ";

			//Get the group text from the metadatacard
			//----------------------------------------
			String groupText = metadataCard.getPropertyGroupText(1);//Gets the group text in the metadatacard

			//Verify if group text is displayed correctly or not
			//--------------------------------------------------
			if(!groupText.equalsIgnoreCase("First"))
				throw new Exception("Metadatacard is not set with the specified Group : 'First' Group.");

			//Verify if group is collapsed or not
			//----------------------------------
			metadataCard.expandPropertyGroup(1,true);//Expand the group if it is collapsed

			Log.message("3. Expanded the group in metadatacard.", driver);

			//Verify if property is existing under the group header
			//-----------------------------------------------------
			if(!metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName1")))
				propValue = propValue+"Expected property : " +dataPool.get("PropertyName1")+ " is not existing in the metadatacard grouping.";

			//Verify if property is existing under the group header
			//-----------------------------------------------------
			if(!metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName2")))
				propValue = propValue+"Expected property : " +dataPool.get("PropertyName1")+ " is not existing in the metadatacard grouping.";

			//Verify if property name is set as is required or not
			//----------------------------------------------------
			if(!metadataCard.isRequiredProperty(dataPool.get("PropertyName1")))
				propValue  = propValue+ "Expected propety :  " +dataPool.get("PropertyName1")+  " is not set as required property.";

			//Verify if property name is set as is required or not
			//----------------------------------------------------
			if(metadataCard.isRequiredProperty(dataPool.get("PropertyName2")))
				propValue = propValue + "Expected property : "+ dataPool.get("PropertyName2") + " is set as required property.";

			//Verification : Verify if property is set as expected
			//----------------------------------------------------
			if(propValue.equals(" "))
				Log.pass("Test Case Passed. Metadata configurability is working as expected when isRequired:(true/false) is set.[Existing object popped out metadatacard]");
			else
				Log.fail("Test Case Failed. Metadata configurability is not working as expected when isRequired:(true/false) is set.[Existing object popped out metadatacard] For more details:" +propValue , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC.1.4.7.1.2B

	/**
	 * TC.1.4.7.1.3A : Verify if property in property group is changed as required or not when isRequired:(true/false) [Multiple Existing objects metadatacard] is set.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "FN119MetadataConfigurability - isRequired"}, 
			description = "Verify if property in property group is changed as required or not when isRequired:(true/false) [Multiple Existing objects metadatacard] is set.")
	public void TC_1_4_7_1_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to any view and select the any existing 
			//---------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), ""); 

			Log.message("1. Navigate to Specified '" + viewToNavigate + "' search view.");

			//Step-2 : Select the specified object type from the search view
			//--------------------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("ObjectType"));

			Log.message("2. Multiple objects is selected in the view.", driver);

			//Step - 3 : Instantiate the metadatacard
			//----------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver,true);//Instantiate the new metadatacard

			String propValue = " ";

			//Get the group text from the metadatacard
			//----------------------------------------
			String groupText = metadataCard.getPropertyGroupText(1);

			//Verify if group text is displayed correctly or not
			//--------------------------------------------------
			if(!groupText.equalsIgnoreCase("First"))
				throw new Exception("Metadatacard is not set with the specified Group : 'First' Group.");

			//Verify if group is collapsed or not
			//----------------------------------
			metadataCard.expandPropertyGroup(1,true);//Expand the group if it is collapsed

			Log.message("3. Expanded the group in metadatacard.", driver);

			//Verify if property is existing under the group header
			//-----------------------------------------------------
			if(!metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName1")))
				propValue = propValue+"Expected property : " +dataPool.get("PropertyName1")+ " is not existing in the metadatacard grouping.";

			//Verify if property is existing under the group header
			//-----------------------------------------------------
			if(!metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName2")))
				propValue = propValue+"Expected property : " +dataPool.get("PropertyName1")+ " is not existing in the metadatacard grouping.";

			//Verify if property name is set as is required or not
			//----------------------------------------------------
			if(!metadataCard.isRequiredProperty(dataPool.get("PropertyName1")))
				propValue  = propValue+ "Expected propety :  " +dataPool.get("PropertyName1")+  " is not set as required ";

			//Verify if property name is set as is required or not
			//----------------------------------------------------
			if(metadataCard.isRequiredProperty(dataPool.get("PropertyName2")))
				propValue = propValue + "Expected property : "+ dataPool.get("PropertyName2") + " is set as required property.";

			//Verification : Verify if property is set as expected
			//----------------------------------------------------
			if(propValue.equals(" "))
				Log.pass("Test Case Passed. Metadata configurability is working as expected when isRequired:(true/false) is set.[Multiple Existing objects metadatacard]");
			else
				Log.fail("Test Case Failed. Metadata configurability is not working as expected when isRequired:(true/false) is set.[Multiple Existing objects metadatacard] For more details:" +propValue , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC.1.4.7.1.3A

	/**
	 * TC.1.4.7.1.3B : Verify if property in property group is changed as required or not when isRequired:(true/false) [Multiple existing objects popped out metadatacard] is set.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "FN119MetadataConfigurability - isRequired"}, 
			description = "Verify if property in property group is changed as required or not when isRequired:(true/false) [Multiple existing objects popped out metadatacard] is set.")
	public void TC_1_4_7_1_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to any view and select the any existing 
			//---------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), ""); 

			Log.message("1. Navigate to Specified '" + viewToNavigate + "' search view.");

			//Step-2 : Select the specified object type from the search view
			//--------------------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("ObjectType"));//Selects the multiple objects in the view

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value);//Clicks the properties option from task pane

			Log.message("2. Multiple objects is selected in the view.", driver);

			//Step - 3 : Instantiate the metadatacard
			//----------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the new metadatacard

			String propValue = " ";

			//Get the group text from the metadatacard
			//----------------------------------------
			String groupText = metadataCard.getPropertyGroupText(1);//Gets the group text in the metadatacard

			//Verify if group text is displayed correctly or not
			//--------------------------------------------------
			if(!groupText.equalsIgnoreCase("First"))
				throw new Exception("Metadatacard is not set with the specified Group : 'First' Group.");

			//Verify if group is collapsed or not
			//----------------------------------
			metadataCard.expandPropertyGroup(1,true);//Expand the group if it is collapsed

			Log.message("3. Expanded the group in metadatacard.", driver);

			//Verify if property is existing under the group header
			//-----------------------------------------------------
			if(!metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName1")))
				propValue = propValue+"Expected property : " +dataPool.get("PropertyName1")+ " is not existing in the metadatacard grouping.";

			//Verify if property is existing under the group header
			//-----------------------------------------------------
			if(!metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName2")))
				propValue = propValue+"Expected property : " +dataPool.get("PropertyName1")+ " is not existing in the metadatacard grouping.";

			//Verify if property name is set as is required or not
			//----------------------------------------------------
			if(!metadataCard.isRequiredProperty(dataPool.get("PropertyName1")))
				propValue  = propValue+ "Expected propety :  " +dataPool.get("PropertyName1")+  " is not set as required property.";

			//Verify if property name is set as is required or not
			//----------------------------------------------------
			if(metadataCard.isRequiredProperty(dataPool.get("PropertyName2")))
				propValue = propValue + "Expected property : "+ dataPool.get("PropertyName2") + " is set as required property.";

			//Verification : Verify if property is set as expected
			//----------------------------------------------------
			if(propValue.equals(" "))
				Log.pass("Test Case Passed. Metadata configurability is working as expected when isRequired:(true/false) is set.[Multiple existing objects popped out metadatacard]");
			else
				Log.fail("Test Case Failed. Metadata configurability is not working as expected when isRequired:(true/false) is set.[Multiple existing objects popped out metadatacard] For more details:" +propValue , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC.1.4.7.1.3B

	/**
	 * TC.1.4.7.2.1A : Verify if  property is changed as required or not when isRequired:(true/false) [Object creation via Task pane] is set.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isRequired"}, 
			description = "Verify if  property is changed as required or not when isRequired:(true/false) [Object creation via Task pane] is set.")
	public void TC_1_4_7_2_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Select the lookup property in the metadatacard
			//-------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Customer.Value);

			Log.message("1. Selected the new 'Customer' object from the task panel.", driver);

			//Checks if properties is required or not
			//--------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the new metadatacard
			String propValue = "";

			if(!metadataCard.isRequiredProperty(dataPool.get("PropertyName1")))
				propValue  = propValue+ "Expected propety :  " +dataPool.get("PropertyName1")+  " is not set as required property.";

			if(metadataCard.isRequiredProperty(dataPool.get("PropertyName2")))
				propValue = propValue + "Expected property : "+ dataPool.get("PropertyName2") + " is set as required property.";

			//Verifies if configuration is working as expected
			//------------------------------------------------
			if(propValue.equals(""))
				Log.pass("Test Case Passed. Metadata configurability is working as expected when isRequired:(true/false) is set.[Object creation via Task pane]");
			else
				Log.fail("Test Case Failed. Metadata configurability is not working as expected when isRequired:(true/false) is set.[Object creation via Task pane] For more details:" +propValue , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_7_2_1A

	/**
	 * TC.1.4.7.2.1B : Verify if  property is changed as required or not when isRequired:(true/false) [Object creation via Task pane] is set.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isRequired"}, 
			description = "Verify if property is changed as required or not when isRequired:(true/false) [Object creation via Task pane] is set.")
	public void TC_1_4_7_2_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Select the lookup property in the metadatacard
			//-------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Customer.Value);

			Log.message("1. Selected the new 'Customer' object from the Operations menu.", driver);

			//Checks if properties is required or not
			//--------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the new metadatacard

			String propValue = " ";

			if(!metadataCard.isRequiredProperty(dataPool.get("PropertyName1")))
				propValue  = propValue+ "Expected propety :  " +dataPool.get("PropertyName1")+  " is not set as required property.";

			if(metadataCard.isRequiredProperty(dataPool.get("PropertyName2")))
				propValue = propValue + "Expected property : "+ dataPool.get("PropertyName2") + " is set as required property.";

			//Verification if metadata configuration is working as expected
			//-------------------------------------------------------------
			if(propValue.equals(" "))
				Log.pass("Test Case Passed. Metadata configurability is working as expected when isRequired:(true/false) is set.[Object creation via New item menu]");
			else
				Log.fail("Test Case Failed. Metadata configurability is not working as expected when isRequired:(true/false) is set.[Object creation via New item menu] For more details:" +propValue , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_7_2_1B

	/**
	 * TC.1.4.7.2.1C : Verify if property is changed as required or not when isRequired:(true/false) [Object creation via Object Property].
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isRequired"}, 
			description = "Verify if property is changed as required or not when isRequired:(true/false) [Object creation via Object Property].")
	public void TC_1_4_7_2_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to any view and select the any existing 
			//---------------------------------------------------------
			homePage.searchPanel.clickSearchBtn(driver);//Click the search button

			Log.message("1. Navigate to any search view.");

			//Step-2 : Select the specified object type from the search view
			//--------------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectType"));

			Log.message("2. Selected the specified object (" + dataPool.get("ObjectType") +") from the search view.", driver);

			//Step-3 : Select the lookup property in the metadatacard
			//-------------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver,true);//Instantiate the new metadatacard
			metadatacard.addNewProperty("Customer");//if property is not existing add the new customer property

			Log.message("3. 'Customer' Property is added to the new metadatacard", driver);

			//Step-4 : Select the existing property in metadatacard
			//-----------------------------------------------------
			metadatacard.clickAddValueButton("Customer");//Clicks the Add value button

			Log.message("4. Selected the Addfield property in the metadatacard.", driver);

			//Checks if properties is required or not
			//----------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the new metadatacard

			String propValue = " ";

			if(!metadataCard.isRequiredProperty(dataPool.get("PropertyName1")))
				propValue  = propValue+ "Expected propety :  " +dataPool.get("PropertyName1")+  " is not set as required property.";

			if(metadataCard.isRequiredProperty(dataPool.get("PropertyName2")))
				propValue = propValue + "Expected property : "+ dataPool.get("PropertyName2") + " is set as required property.";

			//Verification : Verify if metadata configuration is working as expected
			//-----------------------------------------------------------------------
			if(propValue.equals(" "))
				Log.pass("Test Case Passed. Metadata configurability is working as expected when isRequired:(true/false) is set.[Object creation via Object property]");
			else
				Log.fail("Test Case Failed. Metadata configurability is not working as expected when isRequired:(true/false) is set.[Object creation via Object property] For more details:" +propValue , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_7_2_1C

	/**
	 * TC.1.4.7.2.2A : Verify if property is changed as required or not when isRequired:(true/false) [Existing object] is set.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isRequired"}, 
			description = "Verify if property is changed as required or not when isRequired:(true/false) [Existing object] is set.")
	public void TC_1_4_7_2_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to any view and select the any existing 
			//---------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), ""); 

			Log.message("1. Navigate to Specified '" + viewToNavigate + "' search view.");

			//Step-2 : Select the specified object type from the search view
			//--------------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectType"));

			Log.message("2. Selected the specified object (" + dataPool.get("ObjectType") +") from the search view.", driver);

			//Checks if properties is required or not
			//----------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver,true);//Instantiate the new metadatacard

			String propValue = " ";

			if(!metadataCard.isRequiredProperty(dataPool.get("PropertyName1")))
				propValue  = propValue+ "Expected propety :  " +dataPool.get("PropertyName1")+  " is not set as required property.";

			if(metadataCard.isRequiredProperty(dataPool.get("PropertyName2")))
				propValue = propValue + "Expected property : "+ dataPool.get("PropertyName2") + " is set as required property.";

			//Verification : Verify if metadata configuration is working as expected
			//-----------------------------------------------------------------------
			if(propValue.equals(" "))
				Log.pass("Test Case Passed. Metadata configurability is working as expected when isRequired:(true/false) is set.[Existing object]");
			else
				Log.fail("Test Case Failed. Metadata configurability is not working as expected when isRequired:(true/false) is set.[Existing object] For more details:" +propValue , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC.1.4.7.2.2A

	/**
	 * TC.1.4.7.2.2B : Verify if property is changed as required or not when isRequired:(true/false) [Existing object popped out metadatacard] is set.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isRequired"}, 
			description = "Verify if property is changed as required or not when isRequired:(true/false) [Existing object popped out metadatacard] is set.")
	public void TC_1_4_7_2_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to any view and select the any existing 
			//---------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), ""); 

			Log.message("1. Navigate to Specified '" + viewToNavigate + "' search view.");

			//Step-2 : Select the specified object type from the search view
			//--------------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectType")))//Selects the object in the view
				throw new SkipException("Object("+dataPool.get("ObjectType")+") is not selected");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);//Clicks the properties option from task pane

			Log.message("2. Selected the specified object (" + dataPool.get("ObjectType") +") from the search view.", driver);

			//Checks if properties is required or not
			//----------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the new metadatacard
			String propValue = " ";

			if(!metadataCard.isRequiredProperty(dataPool.get("PropertyName1")))
				propValue  = propValue+ "Expected propety :  " +dataPool.get("PropertyName1")+  " is not set as required property.";

			if(metadataCard.isRequiredProperty(dataPool.get("PropertyName2")))
				propValue = propValue + "Expected property : "+ dataPool.get("PropertyName2") + " is set as required property.";

			//Verification : Verify if metadata configuration is working as expected
			//-----------------------------------------------------------------------
			if(propValue.equals(" "))
				Log.pass("Test Case Passed. Metadata configurability is working as expected when isRequired:(true/false) is set.[Existing object popped out metadatacard]");
			else
				Log.fail("Test Case Failed. Metadata configurability is not working as expected when isRequired:(true/false) is set.[Existing object popped out metadatacard] For more details:" +propValue , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC.1.4.7.2.2B

	/**
	 * TC.1.4.7.2.3A : Verify if property is changed as required or not when isRequired:(true/false) [Multiple Existing objects metadatacard] is set.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "FN119MetadataConfigurability - isRequired"}, 
			description = "Verify if property is changed as required or not when isRequired:(true/false) [Multiple Existing objects metadatacard] is set.")
	public void TC_1_4_7_2_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to any view and select the any existing 
			//---------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), ""); 

			Log.message("1. Navigate to Specified '" + viewToNavigate + "' search view.");

			//Step-2 : Select the specified object type from the search view
			//--------------------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("ObjectType"));

			Log.message("2. Multiple objects is selected in the view.", driver);

			//Checks if properties is required or not
			//----------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver,true);//Instantiate the new metadatacard

			String propValue = " ";

			if(!metadataCard.isRequiredProperty(dataPool.get("PropertyName1")))
				propValue  = propValue+ "Expected propety :  " +dataPool.get("PropertyName1")+  " is not set as required property.";

			if(metadataCard.isRequiredProperty(dataPool.get("PropertyName2")))
				propValue = propValue + "Expected property : "+ dataPool.get("PropertyName2") + " is set as required property.";

			//Verification : Verify if metadata configuration is working as expected
			//-----------------------------------------------------------------------
			if(propValue.equals(" "))
				Log.pass("Test Case Passed. Metadata configurability is working as expected when isRequired:(true/false) is set.[Multiple Existing objects metadatacard]");
			else
				Log.fail("Test Case Failed. Metadata configurability is not working as expected when isRequired:(true/false) is set.[Multiple Existing objects metadatacard] For more details:" +propValue , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC.1.4.7.2.3A

	/**
	 * TC.1.4.7.2.3B : Verify if property is changed as required or not when isRequired:(true/false) [Multiple existing objects popped out metadatacard] is set.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "FN119MetadataConfigurability - isRequired"}, 
			description = "Verify if property is changed as required or not when isRequired:(true/false) [Multiple existing objects popped out metadatacard] is set.")
	public void TC_1_4_7_2_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to any view and select the any existing 
			//---------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), ""); 

			Log.message("1. Navigate to Specified '" + viewToNavigate + "' search view.");

			//Step-2 : Select the specified object type from the search view
			//--------------------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("ObjectType"));//Selects the multiple objects in the view

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value);//Clicks the properties option from task pane

			Log.message("2. Multiple objects is selected in the view.", driver);

			//Checks if properties is required or not
			//----------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the new metadatacard

			String propValue = " ";

			if(!metadataCard.isRequiredProperty(dataPool.get("PropertyName1")))
				propValue  = propValue+ "Expected propety :  " +dataPool.get("PropertyName1")+  " is not set as required property.";

			if(metadataCard.isRequiredProperty(dataPool.get("PropertyName2")))
				propValue = propValue + "Expected property : "+ dataPool.get("PropertyName2") + " is set as required property.";

			//Verification : Verify if metadata configuration is working as expected
			//-----------------------------------------------------------------------
			if(propValue.equals(" "))
				Log.pass("Test Case Passed. Metadata configurability is working as expected when isRequired:(true/false) is set.[Multiple existing objects popped out metadatacard]");
			else
				Log.fail("Test Case Failed. Metadata configurability is not working as expected when isRequired:(true/false) is set.[Multiple existing objects popped out metadatacard] For more details:" +propValue , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC.1.4.7.2.3B

	/**
	 * TC.1.4.8.2.1A : Verify if metadata configurability is working as expected when IsAdditional&IsHidden (false/true) is used in properties [Object creation via Task pane]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if metadata configurability is working as expected when IsAdditional&IsHidden (false/true) is used in properties [Object creation via Task pane]")
	public void TC_1_4_8_2_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Click the new object link from task pane
			//------------------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));//Clicks the new object link from task pane

			Log.message("1." + dataPool.get("ObjectType") + " is clicked from New item in task pane");

			//Step-2:Set the required values in the metadatacard
			//--------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.setInfo(dataPool.get("Properties"));//Sets the department value as Sales in the metadatacard

			Log.message("2. Property("+dataPool.get("Properties").split("::")[0]+") is set with value("+dataPool.get("Properties").split("::")[1]+") in new object metadatacard", driver);

			//Checks if properties are added and hidden in the metadatacard
			//-------------------------------------------------------------
			String ExpectedMetadata="";

			ArrayList<String> availableProperties = metadataCard.getAvailableAddProperties();

			if(!metadataCard.propertyExists(dataPool.get("Property1")))
				ExpectedMetadata= "Property is not added in the metadatacard when isAdditional: true & isHidden: false is set.";

			for (int i=0; i< availableProperties.size(); i++)
				if(availableProperties.get(i).equalsIgnoreCase(dataPool.get("Property2")))
					ExpectedMetadata = " Property is not added in the metadatacard and displayed in the add property list when isAdditional is set to true.";

			if(metadataCard.propertyExists(dataPool.get("Property2")))
				ExpectedMetadata= " Property is displayed in the metadatacard when isHidden is set to true";

			//Verification : Verify if metadata configurability is working as expected 
			//------------------------------------------------------------------------
			if(ExpectedMetadata.equalsIgnoreCase(""))
				Log.pass("Test Case Passed. Metadata configurability is working as expected while using IsAdditional & IsHidden attributes used in properties. [Object creation via Task pane]");
			else
				Log.fail("Test Case Failed. Metadata configurability is not working as expected while using IsAdditional & IsHidden attributes used in properties. [Object creation via Task pane]. For more details: "+ExpectedMetadata, driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC.1.4.8.2.1A

	/**
	 * TC.1.4.8.2.1B : Verify if metadata configurability is working as expected when IsAdditional&IsHidden (false/true) is used in properties  [Object creation via New item menu]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if metadata configurability is working as expected when IsAdditional&IsHidden (false/true) is used in properties  [Object creation via New item menu]")
	public void TC_1_4_8_2_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Click the new object link from new item menu
			//---------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//Clicks the new menu item

			Log.message("1." + dataPool.get("ObjectType") + " is clicked from new item menu bar");

			//Step-2:Set the required values in the metadatacard
			//--------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.setInfo(dataPool.get("Properties"));//Sets the department value as Sales in the metadatacard

			Log.message("2. Property("+dataPool.get("Properties").split("::")[0]+") is set with value("+dataPool.get("Properties").split("::")[1]+") in new object metadatacard", driver);

			//Checks if properties are added and hidden in the metadatacard
			//-------------------------------------------------------------
			String ExpectedMetadata="";

			ArrayList<String> availableProperties = metadataCard.getAvailableAddProperties();

			if(!metadataCard.propertyExists(dataPool.get("Property1")))
				ExpectedMetadata= "Property is not added in the metadatacard when isAdditional: true & isHidden: false is set.";

			for (int i=0; i< availableProperties.size(); i++)
				if(availableProperties.get(i).equalsIgnoreCase(dataPool.get("Property2")))
					ExpectedMetadata = " Property is not added in the metadatacard and displayed in the add property list when isAdditional is set to true.";

			if(metadataCard.propertyExists(dataPool.get("Property2")))
				ExpectedMetadata= " Property is displayed in the metadatacard when isHidden is set to true";

			//Verification : Verify if metadata configurability is working as expected 
			//------------------------------------------------------------------------
			if(ExpectedMetadata.equalsIgnoreCase(""))
				Log.pass("Test Case Passed. Metadata configurability is working as expected while using IsAdditional & IsHidden attributes used in properties. [Object creation via New item menu]");
			else
				Log.fail("Test Case Failed. Metadata configurability is not working as expected while using IsAdditional & IsHidden attributes used in properties. [Object creation via New item menu]. For more details: "+ExpectedMetadata, driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_8_2_1B

	/**
	 * TC.1.4.8.2.1C : Verify if metadata configurability is working as expected when IsAdditional&IsHidden (false/true) is used in properties. [Object creation via Object property]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if metadata configurability is working as expected when IsAdditional&IsHidden (false/true) is used in properties. [Object creation via Object property]")
	public void TC_1_4_8_2_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

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

			//Step-4:Set the required values in the metadatacard
			//--------------------------------------------------
			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.setInfo(dataPool.get("Properties"));//Sets the department value as Sales in the metadatacard

			Log.message("4. Property("+dataPool.get("Properties").split("::")[0]+") is set with value("+dataPool.get("Properties").split("::")[1]+") in new object metadatacard", driver);

			//Checks if properties are added and hidden in the metadatacard
			//-------------------------------------------------------------
			String ExpectedMetadata="";

			ArrayList<String> availableProperties = metadataCard.getAvailableAddProperties();

			if(!metadataCard.propertyExists(dataPool.get("Property1")))
				ExpectedMetadata= "Property is not added in the metadatacard when isAdditional: true & isHidden: false is set.";

			for (int i=0; i< availableProperties.size(); i++)
				if(availableProperties.get(i).equalsIgnoreCase(dataPool.get("Property2")))
					ExpectedMetadata = " Property is not added in the metadatacard and displayed in the add property list when isAdditional is set to true.";

			if(metadataCard.propertyExists(dataPool.get("Property2")))
				ExpectedMetadata= " Property is displayed in the metadatacard when isHidden is set to true";

			//Verification : Verify if metadata configurability is working as expected 
			//------------------------------------------------------------------------
			if(ExpectedMetadata.equalsIgnoreCase(""))
				Log.pass("Test Case Passed. Metadata configurability is working as expected while using IsAdditional & IsHidden attributes used in properties. [Object creation via Object property]");
			else
				Log.fail("Test Case Failed. Metadata configurability is not working as expected while using IsAdditional & IsHidden attributes used in properties. [Object creation via Object property] For more details: "+ExpectedMetadata, driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_8_2_1C

	/**
	 * TC_1.4.8.2.2A : Verify if metadata configurability is working as expected when IsAdditional&IsHidden (false/true) is used in properties. [Existing object metadatacard]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if metadata configurability is working as expected when IsAdditional&IsHidden (false/true) is used in properties. [Existing object metadatacard].")
	public void TC_1_4_8_2_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to : " + viewToNavigate + " view.");

			//Step-2 : Select any existing object
			//------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			Log.message("2. Selected the specified object : " + dataPool.get("Object") + " in list view.");

			//Step-3:Set the required values in the metadatacard
			//--------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard

			metadataCard.setInfo(dataPool.get("Properties"));//Sets the department value as Sales in the metadatacard

			Log.message("3. Property("+dataPool.get("Properties").split("::")[0]+") is set with value("+dataPool.get("Properties").split("::")[1]+") in existing object metadatacard in rightpane", driver);

			//Checks if properties are added and hidden in the metadatacard
			//-------------------------------------------------------------
			String ExpectedMetadata="";

			ArrayList<String> availableProperties = metadataCard.getAvailableAddProperties();

			if(!metadataCard.propertyExists(dataPool.get("Property1")))
				ExpectedMetadata= "Property is not added in the metadatacard when isAdditional: true & isHidden: false is set.";

			for (int i=0; i< availableProperties.size(); i++)
				if(availableProperties.get(i).equalsIgnoreCase(dataPool.get("Property2")))
					ExpectedMetadata = " Property is not added in the metadatacard and displayed in the add property list when isAdditional is set to true.";

			if(metadataCard.propertyExists(dataPool.get("Property2")))
				ExpectedMetadata= " Property is displayed in the metadatacard when isHidden is set to true";

			//Verification : Verify if metadata configurability is working as expected 
			//------------------------------------------------------------------------
			if(ExpectedMetadata.equalsIgnoreCase(""))
				Log.pass("Test Case Passed. Metadata configurability is working as expected while using IsAdditional & IsHidden attributes used in properties. [Existing object metadatacard]");
			else
				Log.fail("Test Case Failed. Metadata configurability is not working as expected while using IsAdditional & IsHidden attributes used in properties. [Existing object metadatacard] For more details: "+ExpectedMetadata, driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_8_2_2A

	/**
	 * TC.1.4.8.2.2B : Verify if metadata configurability is working as expected when IsAdditional&IsHidden (false/true) is used in properties. [Existing object popout metadata card].
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if metadata configurability is working as expected when IsAdditional&IsHidden (false/true) is used in properties. [Existing object popout metadata card].")
	public void TC_1_4_8_2_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to : " + viewToNavigate + " view.");

			//Step-2 : Select any existing object
			//------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			Log.message("2. Selected the specified object : " + dataPool.get("Object") + " in list view.");

			//Step-3: Open the popout metadatacard of the object
			//--------------------------------------------------
			homePage.taskPanel.clickItem("Properties");//Clicks the properties option from task pane

			Log.message("3. Existing object popped out metadatacard is opened via task pane");

			//Step-4:Set the required values in the metadatacard
			//--------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.setInfo(dataPool.get("Properties"));//Sets the department value as Sales in the metadatacard

			Log.message("4. Property("+dataPool.get("Properties").split("::")[0]+") is set with value("+dataPool.get("Properties").split("::")[1]+") in existing object popout metadatacard", driver);

			//Checks if properties are added and hidden in the metadatacard
			//-------------------------------------------------------------
			String ExpectedMetadata="";

			ArrayList<String> availableProperties = metadataCard.getAvailableAddProperties();

			if(!metadataCard.propertyExists(dataPool.get("Property1")))
				ExpectedMetadata= "Property is not added in the metadatacard when isAdditional: true & isHidden: false is set.";

			for (int i=0; i< availableProperties.size(); i++)
				if(availableProperties.get(i).equalsIgnoreCase(dataPool.get("Property2")))
					ExpectedMetadata = " Property is not added in the metadatacard and displayed in the add property list when isAdditional is set to true.";

			if(metadataCard.propertyExists(dataPool.get("Property2")))
				ExpectedMetadata= " Property is displayed in the metadatacard when isHidden is set to true";

			//Verification : Verify if metadata configurability is working as expected 
			//------------------------------------------------------------------------
			if(ExpectedMetadata.equalsIgnoreCase(""))
				Log.pass("Test Case Passed. Metadata configurability is working as expected while using IsAdditional & IsHidden attributes used in properties. [Existing object popout metadata card]");
			else
				Log.fail("Test Case Failed. Metadata configurability is not working as expected while using IsAdditional & IsHidden attributes used in properties. [Existing object popout metadata card] For more details: "+ExpectedMetadata, driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_8_2_2B

	/**
	 * TC_1.4.8.2.3A : Verify if metadata configurability is working as expected when IsAdditional&IsHidden (false/true) is used in properties. [Multiple Existing objects metadata card].
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if metadata configurability is working as expected when IsAdditional&IsHidden (false/true) is used in properties. [Multiple Existing objects metadata card].")
	public void TC_1_4_8_2_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to : " + viewToNavigate + " view.");

			//Step-2 : Select any existing object
			//------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));//Selects the multiple objects in the view

			Log.message("2. Multiple objects selected in the list view.");

			//Step-3: Set the required values in the metadatacard
			//----------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard of existing object

			metadataCard.setInfo(dataPool.get("Properties"));//Sets the department value as Sales in the metadatacard

			Log.message("3. Property("+dataPool.get("Properties").split("::")[0]+") is set with value("+dataPool.get("Properties").split("::")[1]+") in existing objects metadatacard in rightpane", driver);

			//Checks if properties are added and hidden in the metadatacard
			//-------------------------------------------------------------
			String ExpectedMetadata="";

			ArrayList<String> availableProperties = metadataCard.getAvailableAddProperties();

			if(!metadataCard.propertyExists(dataPool.get("Property1")))
				ExpectedMetadata= "Property is not added in the metadatacard when isAdditional: true & isHidden: false is set.";

			for (int i=0; i< availableProperties.size(); i++)
				if(availableProperties.get(i).equalsIgnoreCase(dataPool.get("Property2")))
					ExpectedMetadata = " Property is not added in the metadatacard and displayed in the add property list when isAdditional is set to true.";

			if(metadataCard.propertyExists(dataPool.get("Property2")))
				ExpectedMetadata= " Property is displayed in the metadatacard when isHidden is set to true";

			//Verification : Verify if metadata configurability is working as expected 
			//------------------------------------------------------------------------
			if(ExpectedMetadata.equalsIgnoreCase(""))
				Log.pass("Test Case Passed. Metadata configurability is working as expected while using IsAdditional & IsHidden attributes used in properties. [Multiple Existing objects metadata card]");
			else
				Log.fail("Test Case Failed. Metadata configurability is not working as expected while using IsAdditional & IsHidden attributes used in properties. [Multiple Existing objects metadata card] For more details: "+ExpectedMetadata, driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_8_2_3A

	/**
	 * TC_1.4.8.2.3B : Verify if metadata configurability is working as expected when IsAdditional&IsHidden (false/true) is used in properties. [Multiple Existing objects popout metadata card].
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if metadata configurability is working as expected when IsAdditional&IsHidden (false/true) is used in properties. [Multiple Existing objects popout metadata card].")
	public void TC_1_4_8_2_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to : " + viewToNavigate + " view.");

			//Step-2 : Select multiple existing objects
			//------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));//Selects the multiple objects in the view

			Log.message("2. Multiple objects selected in the list view.");

			//Step-3: Open the popout metadatacard of the object
			//--------------------------------------------------
			homePage.taskPanel.clickItem("Properties");//Clicks the properties option from task pane

			Log.message("3. Existing object popped out metadatacard is opened via task pane");

			//Step-4: Set the required values in the metadatacard
			//----------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard of existing object

			metadataCard.setInfo(dataPool.get("Properties"));//Sets the department value as Sales in the metadatacard

			Log.message("4. Property("+dataPool.get("Properties").split("::")[0]+") is set with value("+dataPool.get("Properties").split("::")[1]+") in existing objects popout metadatacard", driver);

			//Checks if properties are added and hidden in the metadatacard
			//-------------------------------------------------------------
			String ExpectedMetadata="";

			ArrayList<String> availableProperties = metadataCard.getAvailableAddProperties();

			if(!metadataCard.propertyExists(dataPool.get("Property1")))
				ExpectedMetadata= "Property is not added in the metadatacard when isAdditional: true & isHidden: false is set.";

			for (int i=0; i< availableProperties.size(); i++)
				if(availableProperties.get(i).equalsIgnoreCase(dataPool.get("Property2")))
					ExpectedMetadata = " Property is not added in the metadatacard and displayed in the add property list when isAdditional is set to true.";

			if(metadataCard.propertyExists(dataPool.get("Property2")))
				ExpectedMetadata= " Property is displayed in the metadatacard when isHidden is set to true";

			//Verification : Verify if metadata configurability is working as expected 
			//------------------------------------------------------------------------
			if(ExpectedMetadata.equalsIgnoreCase(""))
				Log.pass("Test Case Passed. Metadata configurability is working as expected while using IsAdditional & IsHidden attributes used in properties. [Multiple Existing objects popout metadata card]");
			else
				Log.fail("Test Case Failed. Metadata configurability is not working as expected while using IsAdditional & IsHidden attributes used in properties. [Multiple Existing objects popout metadata card] For more details: "+ExpectedMetadata, driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_8_2_3B

	/**
	 * TC.1.4.9.2.1A : Verify if property is displayed when isAdditional:(true/false) [Object creation via Task pane] is set.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isAdditional"}, 
			description = "Verify if property is displayed when isAdditional:(true/false) [Object creation via Task pane] is set.")
	public void TC_1_4_9_2_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Select the lookup property in the metadatacard
			//-------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Customer.Value);

			Log.message("1. Selected the new 'Customer' object from the task panel.", driver);

			//Step - 2 : Instantiate the metadatacard
			//----------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the new metadatacard

			String propValue = " ";

			//Get the group text from the metadatacard
			//----------------------------------------
			String groupText = metadataCard.getPropertyGroupText(1);

			//Verify if group text is displayed correctly or not
			//--------------------------------------------------
			if(!groupText.equalsIgnoreCase("First"))
				throw new Exception("Metadatacard is not set with the specified Group : 'First' Group.");

			//Verify if group is collapsed or not
			//----------------------------------
			metadataCard.expandPropertyGroup(1,true);//Expand the group if it is collapsed

			Log.message("2. Expanded the group in metadatacard.", driver);

			//Verify if property is existing under the group header
			//-----------------------------------------------------
			if(!metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName1")))
				propValue = propValue+"Expected property : " +dataPool.get("PropertyName1")+ " is not existing in the metadatacard grouping.";

			if(metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName2")))
				propValue = propValue+"Expected property : " +dataPool.get("PropertyName2")+ " is not existing in the metadatacard grouping.";


			//Verification : Verify if all properties are displayed when isAdditional property is set
			//---------------------------------------------------------------------------------------
			if(propValue.equals(" "))
				Log.pass("Test Case Passed.All the Properties are displayed when isAdditional:(true/false) is set and its work as expected.");
			else
				Log.fail("Test Case Failed.Properties are displayed even though isHidden rule is set." +propValue , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End TC_1_4_9_2_1A

	/**
	 * TC.1.4.9.2.1B : Verify if property is displayed when isAdditional:(true/false) [Object creation via New item menu] is set.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isAdditional"}, 
			description = "Verify if property is displayed when isAdditional:(true/false) [Object creation via New item menu] is set.")
	public void TC_1_4_9_2_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Select the lookup property in the metadatacard
			//-------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Customer.Value);

			Log.message("1. Selected the new 'Customer' object from the Menu bar.", driver);

			//Step - 2 : Instantiate the metadatacard
			//----------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the new metadatacard

			String propValue = " ";

			//Get the group text from the metadatacard
			//----------------------------------------
			String groupText = metadataCard.getPropertyGroupText(1);

			//Verify if group text is displayed correctly or not
			//--------------------------------------------------
			if(!groupText.equalsIgnoreCase("First"))
				throw new Exception("Metadatacard is not set with the specified Group : 'First' Group.");

			//Verify if group is collapsed or not
			//----------------------------------
			metadataCard.expandPropertyGroup(1,true);//Expand the group if it is collapsed

			Log.message("2. Expanded the group in metadatacard.", driver);

			//Verify if property is existing under the group header
			//-----------------------------------------------------
			if(!metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName1")))
				propValue = propValue+"Expected property : " +dataPool.get("PropertyName1")+ " is not existing in the metadatacard grouping.";

			if(metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName2")))
				propValue = propValue+"Expected property : " +dataPool.get("PropertyName2")+ " is not existing in the metadatacard grouping.";

			//Verification : Verify if all properties are displayed when isAdditional propery is set
			//--------------------------------------------------------------------------------------
			if(propValue.equals(" "))
				Log.pass("Test Case Passed.All the Properties are displayed when isAdditional:(true/false) is set and its work as expected.");
			else
				Log.fail("Test Case Failed.Properties are displayed even though isHidden rule is set." +propValue , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End TC_1_4_6_2_1B

	/**
	 * TC_1.4.9.2.1C : Verify if property is not displayed  when isHidden:(true/false) [Object creation via Object Property] is set.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isAdditional"}, 
			description = "Verify if property is not displayed  when isHidden:(true/false) [Object creation via Object Property] is set.")
	public void TC_1_4_9_2_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			Log.message("4. Selected the Addfield property in the metadatacard", driver);

			//Get the group text from the metadatacard
			//----------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);
			String groupText = metadataCard.getPropertyGroupText(1);

			//Verify if group text is displayed correctly or not
			//--------------------------------------------------
			if(!groupText.equalsIgnoreCase("First"))
				throw new Exception("Metadatacard is not set with the specified Group : 'First' Group.");

			//Verify if group is collapsed or not
			//----------------------------------
			metadataCard.expandPropertyGroup(1,true);//Expand the group if it is collapsed

			Log.message("5. Expanded the group in metadatacard.", driver);

			String propValue = " ";

			//Verify if property is existing under the group header
			//-----------------------------------------------------
			if(!metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName1")))
				propValue = propValue+"Expected property : " +dataPool.get("PropertyName1")+ " is not existing in the metadatacard grouping.";

			//Verify if property is not exists under the groupheader
			//------------------------------------------------------
			if(metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName2")))
				propValue = propValue+"Expected propety : " + dataPool.get("PropertyName2") + " is existing in the metadatacard grouping.";

			//Verification : Verify if all properties are displayed when isAdditional propery is set
			//--------------------------------------------------------------------------------------
			if(propValue.equals(" "))
				Log.pass("Test Case Passed.All the Properties are displayed when isAdditional:(true/false) is set and its work as expected.");
			else
				Log.fail("Test Case Failed.Properties are displayed even though isAdditional rule is set." +propValue , driver);


		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1.4.9.2.1C

	/**
	 * TC_1.4.9.2.2A : Verify if property is displayed when isAdditional:(true/false) [Existing object] is set.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isAdditional"}, 
			description = "Verify if property is displayed when isAdditional:(true/false) [Existing object] is set.")
	public void TC_1_4_9_2_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to any view and select the any existing 
			//---------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), ""); 

			Log.message("1. Navigate to Specified '" + viewToNavigate + "' search view.");

			//Step-2 : Select the specified object type from the search view
			//--------------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectType"));

			Log.message("2. Selected the specified object (" + dataPool.get("ObjectType") +") from the search view.", driver);

			//Step - 3 : Instantiate the metadatacard
			//----------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver,true);//Instantiate the new metadatacard

			String propValue = " ";//Instantiate the string prop value

			//Get the group text from the metadatacard
			//----------------------------------------
			String groupText = metadataCard.getPropertyGroupText(1);//Get the group text in the from the metadatacard

			//Verify if group text is displayed correctly or not
			//--------------------------------------------------
			if(!groupText.equalsIgnoreCase("First"))
				throw new Exception("Metadatacard is not set with the specified Group : 'First' Group.");

			//Verify if group is collapsed or not
			//----------------------------------
			metadataCard.expandPropertyGroup(1,true);//Expand the group if it is collapsed

			Log.message("3. Expanded the group which displayed in the metadatacard.", driver);

			//Verify if property is existing under the group header
			//-----------------------------------------------------
			if(!metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName1")))//Verify if property is exists or not
				propValue = propValue+"Expected property : " +dataPool.get("PropertyName1")+ " is not existing in the metadatacard grouping.";

			//Verify if property is not exists under the groupheader
			//------------------------------------------------------
			if(metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName2")))
				propValue = propValue+"Expected propety : " + dataPool.get("PropertyName2") + " is existing in the metadatacard grouping.";

			//Verification : Verify if all properties are displayed when isAdditional propery is set
			//--------------------------------------------------------------------------------------
			if(propValue.equals(" "))
				Log.pass("Test Case Passed.All the Properties are displayed when isAdditional:(true/false) is set and its work as expected.");
			else
				Log.fail("Test Case Failed.Properties are displayed even though isAdditional rule is set." +propValue , driver);


		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1.4.9.2.2A

	/**
	 * TC_1.4.9.2.2B : Verify if property is displayed when isAdditional:(true/false) [Existing object popout metadata card].
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isAdditional"}, 
			description = "Verify if property is displayed when isAdditional:(true/false) [Existing object popout metadata card].")
	public void TC_1_4_9_2_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to any view and select the any existing 
			//---------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), ""); 

			Log.message("1. Navigate to Specified '" + viewToNavigate + "' search view.");

			//Step-2 : Select the specified object type from the search view
			//--------------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectType"));

			Log.message("2. Selected the specified object (" + dataPool.get("ObjectType") +") from the search view.", driver);

			//Step-3 : Selected the 'Popout' option from the settings menu item
			//---------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver,true);
			metadataCard.popOutMetadatacard();

			Log.message("3. Selected the pop-out metadatacard from the settings menu in rightpane metadatacard.", driver);

			//Step - 3 : Instantiate the metadatacard
			//----------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the new metadatacard

			String propValue = " ";//Instantiate the string prop value

			//Get the group text from the metadatacard
			//----------------------------------------
			String groupText = metadatacard.getPropertyGroupText(1);//Get the group text in the from the metadatacard

			//Verify if group text is displayed correctly or not
			//--------------------------------------------------
			if(!groupText.equalsIgnoreCase("First"))
				throw new Exception("Metadatacard is not set with the specified Group : 'First' Group.");

			//Verify if group is collapsed or not
			//----------------------------------
			metadatacard.expandPropertyGroup(1,true);//Expand the group if it is collapsed

			Log.message("4. Expanded the group which displayed in the metadatacard.", driver);

			//Verify if property is existing under the group header
			//-----------------------------------------------------
			if(!metadatacard.propertyExistsInGroup(1,dataPool.get("PropertyName1")))//Verify if property is exists or not
				propValue = propValue+"Expected property : " +dataPool.get("PropertyName1")+ " is not existing in the metadatacard grouping.";

			//Verify if property is not exists under the groupheader
			//------------------------------------------------------
			if(metadatacard.propertyExistsInGroup(1,dataPool.get("PropertyName2")))
				propValue = propValue+"Expected propety : " + dataPool.get("PropertyName2") + " is existing in the metadatacard grouping.";

			//Verification : Verify if all properties are displayed when isAdditional propery is set
			//--------------------------------------------------------------------------------------
			if(propValue.equals(" "))
				Log.pass("Test Case Passed.All the Properties are displayed when isAdditional:(true/false) is set and its work as expected.");
			else
				Log.fail("Test Case Failed.Properties are displayed even though isAdditional rule is set." +propValue , driver);


		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1.4.9.2.2B

	/**
	 * TC.1.4.9.2.3A : Verify if property is displayed when isAdditional:(true/false) [Multiple Existing objects metadata card] is set.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "FN119MetadataConfigurability - isAdditional"}, 
			description = "Verify if property is displayed when isAdditional:(true/false) [Multiple Existing objects metadata card] is set.")
	public void TC_1_4_9_2_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to any view and select the any existing 
			//---------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), ""); 

			Log.message("1. Navigate to Specified '" + viewToNavigate + "' search view.");

			//Step-2 : Select the specified object type from the search view
			//--------------------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));

			Log.message("2. Selected the specified objects (" + dataPool.get("Objects") +") from the navigated view.", driver);

			//Step-3 : Click the 'Properties' option from the task pane
			//---------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			Log.message("3. Selected the 'Properties' option from the task panel.");

			//Step-3 : Select the lookup property in the metadatacard
			//-------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the new metadatacard

			String propValue = " ";

			//Get the group text from the metadatacard
			//----------------------------------------
			String groupText = metadataCard.getPropertyGroupText(1);

			//Verify if group text is displayed correctly or not
			//--------------------------------------------------
			if(!groupText.equalsIgnoreCase("First"))
				throw new Exception("Metadatacard is not set with the specified Group : 'First' Group.");

			//Verify if group is collapsed or not
			//----------------------------------
			metadataCard.expandPropertyGroup(1,true);//Expand the group if it is collapsed

			Log.message("4. Expanded the group in metadatacard.", driver);

			//Verify if property is existing under the group header
			//-----------------------------------------------------
			if(!metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName1")))
				propValue = propValue+"Expected property : " +dataPool.get("PropertyName1")+ " is not existing in the metadatacard grouping.";

			//Verify if property is not existing under the group header
			//---------------------------------------------------------
			if(metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName2")))
				propValue = propValue+"Expected propety : " + dataPool.get("PropertyName2") + " is existing in the metadatacard grouping.";

			//Verification : verify if property values are set as expected
			//------------------------------------------------------------
			if(propValue.equals(" "))
				Log.pass("Test Case Passed.All the Properties are displayed when isAdditional:(true/false) is set and its work as expected.");
			else
				Log.fail("Test Case Failed.Properties are displayed even though isAdditional rule is set." +propValue , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_9_2_3A

	/**
	 * TC.1.4.9.2.3B : Verify if property is displayed when isAdditional:(true/false) [Multiple Existing objects metadata card] is set.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "FN119MetadataConfigurability - isAdditional"}, 
			description = "Verify if property is displayed when isAdditional:(true/false) [Multiple Existing objects popout metadata card] is set.")
	public void TC_1_4_9_2_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to any view and select the any existing 
			//---------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), ""); 

			Log.message("1. Navigated to the Specified '" + viewToNavigate + "' search view.");

			//Step-2 : Select the specified object type from the search view
			//--------------------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));

			Log.message("2. Selected the specified objects (" + dataPool.get("Objects") +") from the navigated view.", driver);

			//Step-3 : Click the 'Properties' option from the task pane
			//---------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			Log.message("3. Selected the 'Properties' option from the task panel.");

			//Step-3 : Select the lookup property in the metadatacard
			//-------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the new metadatacard

			String propValue = " ";

			//Get the group text from the metadatacard
			//----------------------------------------
			String groupText = metadataCard.getPropertyGroupText(1);

			//Verify if group text is displayed correctly or not
			//--------------------------------------------------
			if(!groupText.equalsIgnoreCase("First"))
				throw new Exception("Metadatacard is not set with the specified Group : 'First' Group.");

			//Verify if group is collapsed or not
			//----------------------------------
			metadataCard.expandPropertyGroup(1,true);//Expand the group if it is collapsed

			Log.message("4. Expanded the group in metadatacard.", driver);

			//Verify if property is existing under the group header
			//-----------------------------------------------------
			if(!metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName1")))
				propValue = propValue+"Expected property : " +dataPool.get("PropertyName1")+ " is not existing in the metadatacard grouping.";

			//Verify if property is not existing under the group header
			//---------------------------------------------------------
			if(metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName2")))
				propValue = propValue+"Expected propety : " + dataPool.get("PropertyName2") + " is existing in the metadatacard grouping.";

			//Verification : verify if property values are set as expected
			//------------------------------------------------------------
			if(propValue.equals(" "))
				Log.pass("Test Case Passed.All the Properties are displayed when isAdditional:(true/false) is set and its work as expected.");
			else
				Log.fail("Test Case Failed.Properties are displayed even though isAdditional rule is set." +propValue , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_9_2_3B

	/**
	 * 1.4.10.2.1A : Verify if property is not displayed when isHidden:(true/false) [Object creation via Task pane] is set.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isHidden"}, 
			description = "Verify if property is not displayed when isHidden:(true/false) [Object creation via Task pane] is set.")
	public void TC_1_4_10_2_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Open the new object object metadatacard
			//--------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Customer.Value);

			Log.message("1. Clicked the New 'Customer' object type link from the task panel.");

			//Step-2 : Verify if property is existing or not in metadatacard
			//--------------------------------------------------------------
			String propValue = " ";

			//Instantiate the newly created metadatacard
			//------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);//Instantiates the metadatacard

			if(metadatacard.propertyExists(dataPool.get("PropertyName1")))//Verify if specified property not exist in the metadatacard or not
				propValue = "Property " + dataPool.get("PropertyName1") + " is existing in the metadatacard.";

			if(!metadatacard.propertyExists(dataPool.get("PropertyName2")))//Verify if property is exist in the metadatacard
				propValue = propValue+ "Property " +  dataPool.get("PropertyName2") + " is not existing the metadatacard as expected.";

			//Get the group text from the metadatacard
			//----------------------------------------
			String groupText = metadatacard.getPropertyGroupText(1);

			//Verify if group text is displayed correctly or not
			//--------------------------------------------------
			if(!groupText.equalsIgnoreCase("First"))
				throw new Exception("Metadatacard is not set with the specified Group : 'First' Group.");

			//Verify if group is collapsed or not
			//----------------------------------
			metadatacard.expandPropertyGroup(1,true);//Expand the group if it is collapsed

			Log.message("2. Expanded the group in metadatacard.", driver);

			//Verify if property is existing under the group header
			//-----------------------------------------------------
			if(!metadatacard.propertyExistsInGroup(1,dataPool.get("PropertyName3")))//Verify if property is existing in metadatacard
				propValue = propValue+"Expected property : " +dataPool.get("PropertyName3")+ " is not existing in the metadatacard grouping.";

			if(metadatacard.propertyExistsInGroup(1,dataPool.get("PropertyName4")))//Verify if property is not existing in the metadatacard
				propValue = propValue+"Expected propety : " + dataPool.get("PropertyName4") + " is existing in the metadatacard grouping.";

			//Verification : Verify if property values are displayed as expected
			//------------------------------------------------------------------
			if(propValue.equals(" "))
				Log.pass("Test Case Passed.Metadata configurability is working as expected when isHidden:(true/false) is set.[Object creation via Task pane]");
			else
				Log.fail("Test Case Failed. Metadata configurability is not working as expected when isHidden:(true/false) is set.[Object creation via Task pane] For more details:" +propValue , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_10_2_1A

	/**
	 * 1.4.10.2.1B : Verify if property is not displayed  when isHidden:(true/false) [Object creation via New item menu] is set.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isHidden"}, 
			description = "Verify if property is not displayed  when isHidden:(true/false) [Object creation via New item menu] is set.")
	public void TC_1_4_10_2_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Create the new 'Document' object
			//-----------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Customer.Value);

			Log.message("1. Clicked the New 'Customer' object type link from New Menu item.", driver);

			//Step-2 : Verify if property is existing or not in metadatacard
			//--------------------------------------------------------------
			String propValue = " ";

			//Instantiate the newly created metadatacard
			//------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);
			if(metadatacard.propertyExists(dataPool.get("PropertyName1")))//Verify if specified property not exist in the metadatacard or not
				propValue = "Property " + dataPool.get("PropertyName1") + " is existing in the metadatacard.";

			if(!metadatacard.propertyExists(dataPool.get("PropertyName2")))//Verify if property is exist in the metadatacard
				propValue = propValue+ "Property " +  dataPool.get("PropertyName2") + " is not existing the metadatacard as expected.";

			//Get the group text from the metadatacard
			//----------------------------------------
			String groupText = metadatacard.getPropertyGroupText(1);

			//Verify if group text is displayed correctly or not
			//--------------------------------------------------
			if(!groupText.equalsIgnoreCase("First"))
				throw new Exception("Metadatacard is not set with the specified Group : 'First' Group.");

			//Verify if group is collapsed or not
			//----------------------------------
			metadatacard.expandPropertyGroup(1,true);//Expand the group if it is collapsed

			Log.message("2. Expanded the group in metadatacard.", driver);

			//Verify if property is existing under the group header
			//-----------------------------------------------------
			if(!metadatacard.propertyExistsInGroup(1,dataPool.get("PropertyName3")))//Verify if property is existing in Group 
				propValue = propValue+"Expected property : " +dataPool.get("PropertyName3")+ " is not existing in the metadatacard grouping.";

			if(metadatacard.propertyExistsInGroup(1,dataPool.get("PropertyName4")))//Verify if property is not existing in group 
				propValue = propValue+"Expected propety : " + dataPool.get("PropertyName4") + " is existing in the metadatacard grouping.";

			//Verification : Verify if property value is set as expected
			//----------------------------------------------------------
			if(propValue.equals(" "))
				Log.pass("Test Case Passed.Metadata configurability is working as expected when isHidden:(true/false) is set.[Object creation via New item menu]");
			else
				Log.fail("Test Case Failed. Metadata configurability is not working as expected when isHidden:(true/false) is set.[Object creation via New item menu] For more details:" +propValue , driver);


		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_10_2_1B

	/**
	 * 1.4.10.2.1C : Verify if property is not displayed  when isHidden:(true/false) [Object creation via Object Property] is set.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isHidden"}, 
			description = "Verify if property is not displayed  when isHidden:(true/false) [Object creation via Object Property] is set.")
	public void TC_1_4_10_2_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to any view and select the any existing 
			//---------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), ""); //Navigate to specified view

			Log.message("1. Navigated to "+ viewToNavigate  +" search view.");

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

			Log.message("4. Selected the Addfield property in the metadatacard", driver);

			//Step-5 : Verify if property is existing or not in metadatacard
			//--------------------------------------------------------------
			String propValue = " ";

			//Instantiate the newly created metadatacard
			//------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);
			if(metadataCard.propertyExists(dataPool.get("PropertyName1")))//Verify if specified property not exist in the metadatacard or not
				propValue = "Property " + dataPool.get("PropertyName1") + " is existing in the metadatacard.";

			if(!metadataCard.propertyExists(dataPool.get("PropertyName2")))//Verify if property is exist in the metadatacard
				propValue = propValue+ "Property " +  dataPool.get("PropertyName2") + " is not existing the metadatacard as expected.";

			//Get the group text from the metadatacard
			//----------------------------------------
			String groupText = metadataCard.getPropertyGroupText(1);

			//Verify if group text is displayed correctly or not
			//--------------------------------------------------
			if(!groupText.equalsIgnoreCase("First"))
				throw new Exception("Metadatacard is not set with the specified Group : 'First' Group.");

			//Verify if group is collapsed or not
			//----------------------------------
			metadataCard.expandPropertyGroup(1,true);//Expand the group if it is collapsed

			Log.message("5. Expanded the group in metadatacard.", driver);

			//Verify if property is existing under the group header
			//-----------------------------------------------------
			if(!metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName3")))//Verify if property is existing in metadatacard
				propValue = propValue+"Expected property : " +dataPool.get("PropertyName3")+ " is not existing in the metadatacard grouping.";

			if(metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName4")))//Verify if property is not existing in metadatacard
				propValue = propValue+"Expected propety : " + dataPool.get("PropertyName4") + " is existing in the metadatacard grouping.";

			//Verification : Verify if properties are set as expected
			//-------------------------------------------------------
			if(propValue.equals(" "))
				Log.pass("Test Case Passed.Metadata configurability is working as expected when isHidden:(true/false) is set.[Object creation via Object property]");
			else
				Log.fail("Test Case Failed. Metadata configurability is not working as expected when isHidden:(true/false) is set.[Object creation via Object property] For more details:" +propValue , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_10_2_1C

	/**
	 * TC.1.4.10.2.2A : Verify if property is not displayed  when isHidden:(true/false)  [Existing object metadata card] is set.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isHidden"}, 
			description = "Verify if property is not displayed  when isHidden:(true/false) [Existing object metadata card] is set.")
	public void TC_1_4_10_2_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to any view and select the any existing 
			//---------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), ""); //Navigate to specified view

			Log.message("1. Navigated to Specified '" + viewToNavigate + "' search view.");

			//Step-2 : Select the specified object type from the search view
			//--------------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectType"));//Selected the specified object in navigate view

			Log.message("2. Selected the specified object (" + dataPool.get("ObjectType") +") from the search view.", driver);

			//Step-3 : Selected the 'property' in task panel
			//----------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);//Select the 'Properties' option from the task panel

			Log.message("3. Selected the 'Properties' option in task pane.", driver);

			String propValue = " ";//Initiate the string variable

			//Instantiate the newly created metadatacard
			//------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate new metadatacard
			if(metadataCard.propertyExists(dataPool.get("PropertyName1")))//Verify if specified property not exist in the metadatacard or not
				propValue = "Property " + dataPool.get("PropertyName1") + " is existing in the metadatacard.";

			if(!metadataCard.propertyExists(dataPool.get("PropertyName2")))//Verify if property is exist in the metadatacard
				propValue = propValue+ "Property " +  dataPool.get("PropertyName2") + " is not existing the metadatacard as expected.";

			//Get the group text from the metadatacard
			//----------------------------------------
			String groupText = metadataCard.getPropertyGroupText(1);//Get the group text in the metadatacards

			//Verify if group text is displayed correctly or not
			//--------------------------------------------------
			if(!groupText.equalsIgnoreCase("First"))//Verify if group title is set as expected
				throw new Exception("Metadatacard is not set with the specified Group : 'First' Group.");

			//Verify if group is collapsed or not
			//----------------------------------
			metadataCard.expandPropertyGroup(1,true);//Expand the group if it is collapsed

			Log.message("4. Expanded the group in metadatacard.", driver);


			//Verify if property is existing under the group header
			//-----------------------------------------------------
			if(!metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName3")))//Verify if property is exists in the metadatacard
				propValue = propValue+"Expected property : " +dataPool.get("PropertyName3")+ " is not existing in the metadatacard grouping.";

			//Verify if property is not existing under the group header
			//---------------------------------------------------------
			if(metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName4")))//Verify if property is not exists in the metadatacard
				propValue = propValue+"Expected propety : " + dataPool.get("PropertyName4") + " is existing in the metadatacard grouping.";

			//Verification  : Verify if property value is set as expected
			//-----------------------------------------------------------
			if(propValue.equals(" "))
				Log.pass("Test Case Passed.Metadata configurability is working as expected when isHidden:(true/false) is set.[Existing object metadata card]");
			else
				Log.fail("Test Case Failed. Metadata configurability is not working as expected when isHidden:(true/false) is set.[Existing object metadata card] For more details:" +propValue , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_10_2_2A

	/**
	 * TC.1.4.10.2.2B : Verify if property is not displayed  when isHidden:(true/false) [Existing object popout metadata card] is set.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isHidden"}, 
			description = "Verify if property is not displayed  when isHidden:(true/false) [Existing object popout metadata card] is set.")
	public void TC_1_4_10_2_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to any view and select the any existing 
			//---------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), ""); 

			Log.message("1. Navigated to Specified '" + viewToNavigate + "' search view.");

			//Step-2 : Select the specified object type from the search view
			//--------------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectType"));

			Log.message("2. Selected the specified object (" + dataPool.get("ObjectType") +") from the search view.", driver);

			//Step-3 : Select the lookup property in the metadatacard
			//-------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver,true);//Instantiate the new metadatacard

			String propValue = " ";

			//Instantiate the newly created metadatacard
			//------------------------------------------
			if(metadataCard.propertyExists(dataPool.get("PropertyName1")))//Verify if specified property not exist in the metadatacard or not
				propValue = "Property " + dataPool.get("PropertyName1") + " is existing in the metadatacard.";

			if(!metadataCard.propertyExists(dataPool.get("PropertyName2")))//Verify if property is exist in the metadatacard
				propValue = propValue+ "Property " +  dataPool.get("PropertyName2") + " is not existing the metadatacard as expected.";

			//Get the group text from the metadatacard
			//----------------------------------------
			String groupText = metadataCard.getPropertyGroupText(1);//Get the group text from the group title

			//Verify if group text is displayed correctly or not
			//--------------------------------------------------
			if(!groupText.equalsIgnoreCase("First"))//Verify if group text is set as expected
				throw new Exception("Metadatacard is not set with the specified Group : 'First' Group.");

			//Verify if group is collapsed or not
			//----------------------------------
			metadataCard.expandPropertyGroup(1,true);//Expand the group if it is collapsed

			Log.message("3. Expanded the group in metadatacard.", driver);

			//Verify if property is existing under the group header
			//-----------------------------------------------------
			if(!metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName3")))//Verify if property is exists in the metadatacard
				propValue = propValue+"Expected property : " +dataPool.get("PropertyName3")+ " is not existing in the metadatacard grouping.";

			if(metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName4")))//Verify if property is not exists in the metadatacard
				propValue = propValue+"Expected propety : " + dataPool.get("PropertyName4") + " is existing in the metadatacard grouping.";

			//Verification : Verify if property value is set as expected
			//----------------------------------------------------------
			if(propValue.equals(" "))
				Log.pass("Test Case Passed.Metadata configurability is working as expected when isHidden:(true/false) is set.[Existing object popout metadata card]");
			else
				Log.fail("Test Case Failed. Metadata configurability is not working as expected when isHidden:(true/false) is set.[Existing object popout metadata card] For more details:" +propValue , driver);


		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_10_2_2B

	/**
	 * TC.1.4.10.2.3A : Verify if property is not displayed  when isHidden:(true/false) [Multiple Existing objects metadata card] is set.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "FN119MetadataConfigurability - isHidden"}, 
			description = "Verify if property is not displayed  when isHidden:(true/false) [Multiple Existing objects metadata card] is set.")
	public void TC_1_4_10_2_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to any view and select the any existing 
			//---------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), ""); 

			Log.message("1. Navigated to the Specified '" + viewToNavigate + "' search view.");

			//Step-2 : Select the specified object type from the search view
			//--------------------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));

			Log.message("2. Selected the specified objects (" + dataPool.get("Objects") +") from the navigated view.", driver);

			//Step-3 : Click the 'Properties' option from the task pane
			//---------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			Log.message("3. Selected the 'Properties' option from the task panel.");

			//Step-3 : Select the lookup property in the metadatacard
			//-------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the new metadatacard

			String propValue = " ";

			//Instantiate the newly created metadatacard
			//------------------------------------------
			if(metadataCard.propertyExists(dataPool.get("PropertyName1")))//Verify if specified property not exist in the metadatacard or not
				propValue = "Property " + dataPool.get("PropertyName1") + " is existing in the metadatacard.";

			if(!metadataCard.propertyExists(dataPool.get("PropertyName2")))//Verify if property is exist in the metadatacard
				propValue = propValue+ "Property " +  dataPool.get("PropertyName2") + " is not existing the metadatacard as expected.";

			//Get the group text from the metadatacard
			//----------------------------------------
			String groupText = metadataCard.getPropertyGroupText(1);

			//Verify if group text is displayed correctly or not
			//--------------------------------------------------
			if(!groupText.equalsIgnoreCase("First"))
				throw new Exception("Metadatacard is not set with the specified Group : 'First' Group.");

			//Verify if group is collapsed or not
			//----------------------------------
			metadataCard.expandPropertyGroup(1,true);//Expand the group if it is collapsed

			Log.message("4. Expanded the group in metadatacard.", driver);

			//Verify if property is existing under the group header
			//-----------------------------------------------------
			if(!metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName3")))
				propValue = propValue+"Expected property : " +dataPool.get("PropertyName3")+ " is not existing in the metadatacard grouping.";

			if(metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName4")))
				propValue = propValue+"Expected propety : " + dataPool.get("PropertyName4") + " is existing in the metadatacard grouping.";

			//Verification if metadata configurability is working as expected
			//---------------------------------------------------------------
			if(propValue.equals(" "))
				Log.pass("Test Case Passed.Metadata configurability is working as expected when isHidden:(true/false) is set.[Multiple Existing objects metadata card]");
			else
				Log.fail("Test Case Failed. Metadata configurability is not working as expected when isHidden:(true/false) is set.[Multiple Existing objects metadata card] For more details:" +propValue , driver);


		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_10_2_3A

	/**
	 * TC.1.4.10.2.3B : Verify if property is not displayed  when isHidden:(true/false) [Multiple Existing objects popout metadata card].
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "FN119MetadataConfigurability - isHidden"}, 
			description = "Verify if property is not displayed  when isHidden:(true/false) [Multiple Existing objects popout metadata card].")
	public void TC_1_4_10_2_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to any view and select the any existing 
			//---------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), ""); 

			Log.message("1. Navigated to Specified '" + viewToNavigate + "' search view.");

			//Step-2 : Select the specified object type from the search view
			//--------------------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));

			Log.message("2. Selected the specified objects (" + dataPool.get("Objects") +") from the navigated view.", driver);

			//Step-4 : Selected the 'Properties' in task pane
			//-----------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			Log.message("3. Selected the 'Properties' option from taskpane.", driver);

			//Step-3 : Select the lookup property in the metadatacard
			//-------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the new metadatacard

			String propValue = " ";

			//Instantiate the newly created metadatacard
			//------------------------------------------
			if(metadataCard.propertyExists(dataPool.get("PropertyName1")))//Verify if specified property not exist in the metadatacard or not
				propValue = "Property " + dataPool.get("PropertyName1") + " is existing in the metadatacard.";

			if(!metadataCard.propertyExists(dataPool.get("PropertyName2")))//Verify if property is exist in the metadatacard
				propValue = propValue+ "Property " +  dataPool.get("PropertyName2") + " is not existing the metadatacard as expected.";

			//Get the group text from the metadatacard
			//----------------------------------------
			String groupText = metadataCard.getPropertyGroupText(1);

			//Verify if group text is displayed correctly or not
			//--------------------------------------------------
			if(!groupText.equalsIgnoreCase("First"))
				throw new Exception("Metadatacard is not set with the specified Group : 'First' Group.");

			//Verify if group is collapsed or not
			//----------------------------------
			metadataCard.expandPropertyGroup(1,true);//Expand the group if it is collapsed

			Log.message("4. Expanded the group in metadatacard.", driver);

			//Verify if property is existing under the group header
			//-----------------------------------------------------
			if(!metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName3")))
				propValue = propValue+"Expected property : " +dataPool.get("PropertyName3")+ " is not existing in the metadatacard grouping.";

			if(metadataCard.propertyExistsInGroup(1,dataPool.get("PropertyName4")))
				propValue = propValue+"Expected propety : " + dataPool.get("PropertyName4") + " is existing in the metadatacard grouping.";

			//Verification if metadata configurability is working as expected
			//---------------------------------------------------------------
			if(propValue.equals(" "))
				Log.pass("Test Case Passed.Metadata configurability is working as expected when isHidden:(true/false) is set.[Multiple Existing objects popout metadata card]");
			else
				Log.fail("Test Case Failed. Metadata configurability is not working as expected when isHidden:(true/false) is set.[Multiple Existing objects popout metadata card]For more details:" +propValue , driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_10_2_3B

	/**
	 * TC.1.5.1.1A : Verify if all properties without the group are added to default group. [Object creation via menu bar].
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if all properties without the group are added to default group. [Object creation via menu bar].")
	public void TC_1_5_1_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Selected the Employee object type from the menu bar
			//------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Employee.Value);//Selected the employee object in menu bar

			Log.message("1. Selected 'Employee' object from the menu bar.", driver);

			//Step-2 : Verify if groups text is set as expected
			//-------------------------------------------------
			String groupValue = " ";//Initiate the group value

			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the metadatacard
			String groupText1 = metadatacard.getPropertyGroupText(1);//get the group text value

			//Verify if group1 text is set as expected
			//----------------------------------------
			if(!groupText1.equals(dataPool.get("grouptext1")))//Verify if group text is set as expected
				groupValue = "Group text is not set as expected.";

			String groupText2 = metadatacard.getPropertyGroupText(2);//get the group text value

			//Verify if group2 text is set as expected
			//----------------------------------------
			if(!groupText2.equals(dataPool.get("grouptext2")))//Verify if group text is set as expected
				groupValue = groupValue + "Group text is not set as expected.";

			//verify if property is exists in the metadatacard default group
			//--------------------------------------------------------------
			ArrayList<String> properties = new ArrayList<String>();
			properties = metadatacard.getMetadatacardProperties();//get the properties from the metadatacard
			for (int i=0;i < properties.size(); i++) {
				if(!metadatacard.propertyExistsInGroup(2,properties.get(i)))//Verify if property if property is exists or not 
					groupValue = groupValue+ "Existing property : "+ properties.get(i) + " does not exists in the group2.";
			}
			//Step-2 : Added the new property in the metadatacard
			//---------------------------------------------------
			metadatacard.addNewProperty(dataPool.get("PropertyName"));

			Log.message("2. Added the Property : " + dataPool.get("PropertyName") + " in the metadatacard.");

			//Verify if property is exists in the specified group or not
			//----------------------------------------------------------
			if(!metadatacard.propertyExistsInGroup(1, dataPool.get("PropertyName")))
				groupValue = groupValue + "Property : " + dataPool.get("PropertyName") + " does not exist in the specified group.";

			//Verification : Verify if all properties are set as expected in the default group 
			//--------------------------------------------------------------------------------
			if(groupValue.equals(" "))//Verify if group value is equals to empty
				Log.pass("Test Case Passed.All properties are inside default-group(group1) excluding the property " + dataPool.get("PropertyName") + "which was added to non-default group(group2).");
			else
				Log.fail("Test Case Failed.All properties are not inside the default-group."+ groupValue, driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC.1.5.1.1A

	/**
	 * TC.1.5.1.1B : Verify if group is displayed when isDefault is [Object creation via Object Property] set.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if group is displayed when isDefault is [Object creation via Object Property] set.")
	public void TC_1_5_1_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to any view and select the any existing 
			//---------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), ""); 

			Log.message("1. Navigated to Specified '" + viewToNavigate + "' search view.");

			//Step-2 : Selected the object type from the navigated view
			//---------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectName"));//Selected the object item in the list view

			Log.message("2. Selected the Object : " +dataPool.get("ObjectName")+" from the list view.", driver);

			//Step-3 : Instantiate the right pane metadatacard
			//-------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver,true);
			metadataCard.addNewProperty("Employee");

			Log.message("3. Added the 'Employee' property ", driver);

			//Step-4 : Click the 'Employee' property in selected object right pane metadatacard
			//---------------------------------------------------------------------------------
			metadataCard.clickAddValueButton("Employee");//Select the add value button in the right pane metadatacard

			Log.message("4. Selected the Add field for 'Employee' property in right pane metadatacard.", driver);

			//Step-5 : Verify if groups text is set as expected
			//-------------------------------------------------
			String groupValue = " ";//Initiate the group value

			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the metadatacard
			String groupText1 = metadatacard.getPropertyGroupText(1);//get the group text value

			//Verify if group1 text is set as expected
			//----------------------------------------
			if(!groupText1.equals(dataPool.get("grouptext1")))//Verify if group text is set as expected
				groupValue = "Group text is not set as expected.";

			String groupText2 = metadatacard.getPropertyGroupText(2);//get the group text value

			//Verify if group2 text is set as expected
			//----------------------------------------
			if(!groupText2.equals(dataPool.get("grouptext2")))//Verify if group text is set as expected
				groupValue = groupValue + "Group text is not set as expected.";

			//verify if property is exists in the metadatacard default group
			//--------------------------------------------------------------
			ArrayList<String> properties = new ArrayList<String>();
			properties = metadatacard.getMetadatacardProperties();//get the properties from the metadatacard
			for (int i=0;i < properties.size(); i++) {
				if(!metadatacard.propertyExistsInGroup(2,properties.get(i)))//Verify if property if property is exists or not 
					groupValue = groupValue+ "Existing property : "+ properties.get(i) + " does not exists in the group2.";
			}
			//Step-6 : Added the new property in the metadatacard
			//---------------------------------------------------
			metadatacard.addNewProperty(dataPool.get("PropertyName"));

			Log.message("5. Added the Property : " + dataPool.get("PropertyName") + " in the metadatacard.");

			//Verify if property is exists in the specified group or not
			//----------------------------------------------------------
			if(!metadatacard.propertyExistsInGroup(1, dataPool.get("PropertyName")))
				groupValue = groupValue + "Property : " + dataPool.get("PropertyName") + " does not exist in the specified group.";

			//Verification : Verify if all properties are set as expected in the default group 
			//--------------------------------------------------------------------------------
			if(groupValue.equals(" "))//Verify if group value is empty
				Log.pass("Test Case Passed.All properties are inside default-group(group1)  excluding the property " + dataPool.get("PropertyName") + "which was added to non-default group(group2).");
			else
				Log.fail("Test Case Failed.All properties are not inside the default-group."+ groupValue, driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC.1.5.1.1B

	/**
	 * TC_1.5.1.2A : Verify if all properties without the group are added to default group. [Existing object metadatacard].
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if all properties without the group are added to default group. [Existing object metadatacard].")
	public void TC_1_5_1_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to any view and select the any existing 
			//---------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), ""); 

			Log.message("1. Navigated to Specified '" + viewToNavigate + "' search view.");

			//Step-2 : Select the specified object type from the search view
			//--------------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectType"));

			Log.message("2. Selected the specified object (" + dataPool.get("ObjectType") +") from the search view.", driver);

			//Step-2 : Verify if groups text is set as expected
			//-------------------------------------------------
			String groupValue = " ";//Initiate the group value

			MetadataCard metadatacard = new MetadataCard(driver,true);//Instantiate the metadatacard
			String groupText1 = metadatacard.getPropertyGroupText(1);//get the group text value

			//Verify if group1 text is set as expected
			//----------------------------------------
			if(!groupText1.equals(dataPool.get("grouptext1")))//Verify if group text is set as expected
				groupValue = "Group text is not set as expected.";

			String groupText2 = metadatacard.getPropertyGroupText(2);//get the group text value

			//Verify if group2 text is set as expected
			//----------------------------------------
			if(!groupText2.equals(dataPool.get("grouptext2")))//Verify if group text is set as expected
				groupValue = groupValue + "Group text is not set as expected.";

			//verify if property is exists in the metadatacard default group
			//--------------------------------------------------------------
			ArrayList<String> properties = new ArrayList<String>();
			properties = metadatacard.getMetadatacardProperties();//get the properties from the metadatacard
			for (int i=0;i < properties.size(); i++) {
				if(!metadatacard.propertyExistsInGroup(2,properties.get(i)))//Verify if property if property is exists or not 
					groupValue = groupValue+ "Existing property : "+ properties.get(i) + " does not exists in the group2.";
			}
			//Step-6 : Added the new property in the metadatacard
			//---------------------------------------------------
			metadatacard.addNewProperty(dataPool.get("PropertyName"));

			Log.message("3. Added the Property : " + dataPool.get("PropertyName") + " in the metadatacard.");

			//Verify if property is exists in the specified group or not
			//----------------------------------------------------------
			if(!metadatacard.propertyExistsInGroup(1, dataPool.get("PropertyName")))
				groupValue = groupValue + "Property : " + dataPool.get("PropertyName") + " does not exist in the specified group.";

			//Verification : Verify if all properties are set as expected in the default group 
			//--------------------------------------------------------------------------------
			if(groupValue.equals(" "))//Verify if group value is set as empty
				Log.pass("Test Case Passed.All properties are inside default-group(group1) excluding the property " + dataPool.get("PropertyName") + "which was added to non-default group(group2).");
			else
				Log.fail("Test Case Failed.All properties are not inside the default-group." + groupValue, driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1.4.9.2.2A

	/**
	 * TC_1.5.1.2B : Verify if all properties without the group are added to default group. [Existing object popout metadata card].
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if all properties without the group are added to default group. [Existing object popout metadata card].")
	public void TC_1_5_1_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to any view and select the any existing 
			//---------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), ""); 

			Log.message("1. Navigate to Specified '" + viewToNavigate + "' search view.");

			//Step-2 : Select the specified object type from the search view
			//--------------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectType"));

			Log.message("2. Selected the specified object (" + dataPool.get("ObjectType") +") from the search view.", driver);

			//Step-3 : Click the 'Properties' item in the task pane
			//-----------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value);

			Log.message("3. Selected the 'Properties' option from the operations menu.", driver);

			//Step-2 : Verify if groups text is set as expected
			//-------------------------------------------------
			String groupValue = " ";//Initiate the group value

			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the metadatacard
			String groupText1 = metadatacard.getPropertyGroupText(1);//get the group text value

			//Verify if group1 text is set as expected
			//----------------------------------------
			if(!groupText1.equals(dataPool.get("grouptext1")))//Verify if group text is set as expected
				groupValue = "Group text is not set as expected.";

			String groupText2 = metadatacard.getPropertyGroupText(2);//get the group text value

			//Verify if group2 text is set as expected
			//----------------------------------------
			if(!groupText2.equals(dataPool.get("grouptext2")))//Verify if group text is set as expected
				groupValue = groupValue + "Group text is not set as expected.";

			//verify if property is exists in the metadatacard default group
			//--------------------------------------------------------------
			ArrayList<String> properties = new ArrayList<String>();
			properties = metadatacard.getMetadatacardProperties();//get the properties from the metadatacard
			for (int i=0;i < properties.size(); i++) {
				if(!metadatacard.propertyExistsInGroup(2,properties.get(i)))//Verify if property if property is exists or not 
					groupValue = groupValue+ "Existing property : "+ properties.get(i) + " does not exists in the group2.";
			}
			//Step-6 : Added the new property in the metadatacard
			//---------------------------------------------------
			metadatacard.addNewProperty(dataPool.get("PropertyName"));

			Log.message("4. Added the Property : " + dataPool.get("PropertyName") + " in the metadatacard.");

			//Verify if property is exists in the specified group or not
			//----------------------------------------------------------
			if(!metadatacard.propertyExistsInGroup(1, dataPool.get("PropertyName")))
				groupValue = groupValue + "Property : " + dataPool.get("PropertyName") + " does not exist in the specified group.";

			//Verification : Verify if all properties are set as expected in the default group 
			//--------------------------------------------------------------------------------
			if(groupValue.equals(" "))//Verify if group value is set as empty
				Log.pass("Test Case Passed.All properties are inside default-group(group1) excluding the property " + dataPool.get("PropertyName") + "which was added to non-default group(group2).");
			else
				Log.fail("Test Case Failed.All properties are not inside the default-group.", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1.4.9.2.2B

	/**
	 * TC_1.5.1.3A : Verify if all properties without the group are added to default group. [Multiple Existing objects metadata card].
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if all properties without the group are added to default group. [Multiple Existing objects metadata card].")
	public void TC_1_5_1_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to any view and select the any existing 
			//---------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), ""); 

			Log.message("1. Navigate to Specified '" + viewToNavigate + "' search view.");

			//Step-2 : Select the specified object type from the search view
			//--------------------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));

			Log.message("2. Selected the specified object (" + dataPool.get("objects") +") from the search view.", driver);

			//Step-3 : Click the 'Properties' item in the task pane
			//-----------------------------------------------------
			homePage.previewPane.popoutRightPaneMetadataTab();//Open the right pane metadatacard

			Log.message("3. Selected the 'Popout metadatacard' from the right pane metadata.", driver);

			//Step-2 : Verify if groups text is set as expected
			//-------------------------------------------------
			String groupValue = " ";//Initiate the group value

			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the metadatacard
			String groupText1 = metadatacard.getPropertyGroupText(1);//get the group text value

			//Verify if group1 text is set as expected
			//----------------------------------------
			if(!groupText1.equals(dataPool.get("grouptext1")))//Verify if group text is set as expected
				groupValue = "Group text is not set as expected.";

			String groupText2 = metadatacard.getPropertyGroupText(2);//get the group text value

			//Verify if group2 text is set as expected
			//----------------------------------------
			if(!groupText2.equals(dataPool.get("grouptext2")))//Verify if group text is set as expected
				groupValue = groupValue + "Group text is not set as expected.";

			//verify if property is exists in the metadatacard default group
			//--------------------------------------------------------------
			ArrayList<String> properties = new ArrayList<String>();
			properties = metadatacard.getMetadatacardProperties();//get the properties from the metadatacard
			for (int i=0;i < properties.size(); i++) {
				if(!metadatacard.propertyExistsInGroup(2,properties.get(i)))//Verify if property if property is exists or not 
					groupValue = groupValue+ "Existing property : "+ properties.get(i) + " does not exists in the group2.";
			}
			//Step-6 : Added the new property in the metadatacard
			//---------------------------------------------------
			metadatacard.addNewProperty(dataPool.get("PropertyName"));

			Log.message("4. Added the Property : " + dataPool.get("PropertyName") + " in the metadatacard.");

			//Verify if property is exists in the specified group or not
			//----------------------------------------------------------
			if(!metadatacard.propertyExistsInGroup(1, dataPool.get("PropertyName")))
				groupValue = groupValue + "Property : " + dataPool.get("PropertyName") + " does not exist in the specified group.";

			//Verification : Verify if all properties are set as expected in the default group 
			//--------------------------------------------------------------------------------
			if(groupValue.equals(" "))//Verify if group value is set as empty
				Log.pass("Test Case Passed.All properties are inside default-group(group1) excluding the property " + dataPool.get("PropertyName") + "which was added to non-default group(group2).");
			else
				Log.fail("Test Case Failed.All properties are not inside the default-group.", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1.4.9.2.3A

	/**
	 * TC_1.5.1.3B :Verify if all properties without the group are added to default group. [Multiple Existing objects popout metadata card].
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if all properties without the group are added to default group. [Multiple Existing objects popout metadata card].")
	public void TC_1_5_1_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to any view and select the any existing 
			//---------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), ""); 

			Log.message("1. Navigate to Specified '" + viewToNavigate + "' search view.");

			//Step-2 : Select the specified object type from the search view
			//--------------------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));

			Log.message("2. Selected the specified object (" + dataPool.get("objects") +") from the search view.", driver);

			//Step-3 : Click the 'Properties' item in the task pane
			//-----------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);//Open the right pane metadatacard

			Log.message("3. Selected the 'Properties' option from the task panel & pop out the metadatacard.", driver);

			//Step-2 : Verify if groups text is set as expected
			//-------------------------------------------------
			String groupValue = " ";//Initiate the group value

			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the metadatacard
			String groupText1 = metadatacard.getPropertyGroupText(1);//get the group text value

			//Verify if group1 text is set as expected
			//----------------------------------------
			if(!groupText1.equals(dataPool.get("grouptext1")))//Verify if group text is set as expected
				groupValue = "Group text is not set as expected.";

			String groupText2 = metadatacard.getPropertyGroupText(2);//get the group text value

			//Verify if group2 text is set as expected
			//----------------------------------------
			if(!groupText2.equals(dataPool.get("grouptext2")))//Verify if group text is set as expected
				groupValue = groupValue + "Group text is not set as expected.";

			//verify if property is exists in the metadatacard default group
			//--------------------------------------------------------------
			ArrayList<String> properties = new ArrayList<String>();
			properties = metadatacard.getMetadatacardProperties();//get the properties from the metadatacard
			for (int i=0;i < properties.size(); i++) {
				if(!metadatacard.propertyExistsInGroup(2,properties.get(i)))//Verify if property if property is exists or not 
					groupValue = groupValue+ "Existing property : "+ properties.get(i) + " does not exists in the group2.";
			}
			//Step-6 : Added the new property in the metadatacard
			//---------------------------------------------------
			metadatacard.addNewProperty(dataPool.get("PropertyName"));

			Log.message("4. Added the Property : " + dataPool.get("PropertyName") + " in the metadatacard.");

			//Verify if property is exists in the specified group or not
			//----------------------------------------------------------
			if(!metadatacard.propertyExistsInGroup(1, dataPool.get("PropertyName")))
				groupValue = groupValue + "Property : " + dataPool.get("PropertyName") + " does not exist in the specified group.";

			//Verification : Verify if all properties are set as expected in the default group 
			//--------------------------------------------------------------------------------
			if(groupValue.equals(" "))//Verify if group value is set as empty
				Log.pass("Test Case Passed.All properties are inside default-group(group1) excluding the property " + dataPool.get("PropertyName") + "which was added to non-default group(group2).");
			else
				Log.fail("Test Case Failed.All properties are not inside the default-group.", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1.4.9.2.3B

	/**
	 * TC.1.5.2.1A : Verify if proeprty grouping with IsHidden (false/true) behaviour [Object creation via Task pane]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if proeprty grouping with IsHidden (false/true) behaviour [Object creation via Task pane]")
	public void TC_1_5_2_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Click the new object link from task pane
			//------------------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));//Clicks the new object link from task pane

			Log.message("1. " + dataPool.get("ObjectType") + " is clicked from New item in task pane");

			//Step-2:Instantiate the metadatacard
			//------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			Log.message("2. New object(" + dataPool.get("ObjectType") + ") Metadatacard is displayed",driver);

			//Verification : Verify if metadata configurability is working as expected 
			//------------------------------------------------------------------------
			if(!metadataCard.isPropertyGroupDisplayed(2, dataPool.get("GroupText")) && !metadataCard.propertyExists(dataPool.get("Property")))
				Log.pass("Test Case Passed. Metadata configurability is working as expected while using IsHidden attribute. [Object creation via Task pane]");
			else
				Log.fail("Test Case Failed. Metadata configurability is not working as expected while using IsHidden attribute. [Object creation via Task pane]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC.1.5.2.1A

	/**
	 * TC.1.5.2.1B : Verify if proeprty grouping with IsHidden (false/true) behaviour [Object creation via New item menu]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if proeprty grouping with IsHidden (false/true) behaviour [Object creation via New item menu]")
	public void TC_1_5_2_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Click the new object link from new item menu
			//---------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//Clicks the new menu item

			Log.message("1. " + dataPool.get("ObjectType") + " is clicked from new item menu bar");

			//Step-2:Instantiate the metadatacard
			//------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			Log.message("2. New object(" + dataPool.get("ObjectType") + ") Metadatacard is displayed", driver);

			//Verification : Verify if metadata configurability is working as expected 
			//------------------------------------------------------------------------
			if(!metadataCard.isPropertyGroupDisplayed(2, dataPool.get("GroupText")) && !metadataCard.propertyExists(dataPool.get("Property")))
				Log.pass("Test Case Passed. Metadata configurability is working as expected while using IsHidden attribute. [Object creation via New item menu]");
			else
				Log.fail("Test Case Failed. Metadata configurability is not working as expected while using IsHidden attribute. [Object creation via New item menu]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC.1.5.2.1B

	/**
	 * TC.1.5.1.1C : Verify if proeprty grouping with IsHidden (false/true) behaviour [Object creation via Object property]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if proeprty grouping with IsHidden (false/true) behaviour [Object creation via Object property]")
	public void TC_1_5_2_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

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

			//Step-4: Instantiate the new object metadatacard
			//-----------------------------------------------
			metadataCard = new MetadataCard(driver);//Instantiate the metadatacard

			Log.message("4. New object(" + dataPool.get("ObjectType") +  ") metadatacard is displayed");

			//Verification : Verify if metadata configurability is working as expected 
			//------------------------------------------------------------------------
			if(!metadataCard.isPropertyGroupDisplayed(2, dataPool.get("GroupText")) && !metadataCard.propertyExists(dataPool.get("Property")))
				Log.pass("Test Case Passed. Metadata configurability is working as expected while using IsHidden attribute. [Object creation via Object property]");
			else
				Log.fail("Test Case Failed. Metadata configurability is not working as expected while using IsHidden attribute. [Object creation via Object property]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC.1.5.2.1C

	/**
	 * TC_1.5.2.2A : Verify if proeprty grouping with IsHidden (false/true) behaviour [Existing object metadatacard]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if proeprty grouping with IsHidden (false/true) behaviour [Existing object metadatacard].")
	public void TC_1_5_2_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to : " + viewToNavigate + " view.");

			//Step-2 : Select any existing object
			//------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			Log.message("2. Selected the specified object : " + dataPool.get("Object") + " in list view.");

			//Step-3: Instantiates the metadatacard
			//-------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard of existing object

			Log.message("3. Existing object metadatacard is displayed");

			//Verification : Verify if metadata configurability is working as expected 
			//------------------------------------------------------------------------
			if(!metadataCard.isPropertyGroupDisplayed(2, dataPool.get("GroupText")) && !metadataCard.propertyExists(dataPool.get("Property")))
				Log.pass("Test Case Passed. Metadata configurability is working as expected while using IsHidden attribute. [Existing object metadatacard]");
			else
				Log.fail("Test Case Failed. Metadata configurability is not working as expected while using IsHidden attribute. [Existing object metadatacard]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1.5.2.2A

	/**
	 * TC_1.5.2.2B : Verify if proeprty grouping with IsHidden (false/true) behaviour. [Existing object popout metadata card].
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if proeprty grouping with IsHidden (false/true) behaviour. [Existing object popout metadata card].")
	public void TC_1_5_2_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to : " + viewToNavigate + " view.");

			//Step-2 : Select any existing object
			//------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			Log.message("2. Selected the specified object : " + dataPool.get("Object") + " in list view.");

			//Step-3: Open the popout metadatacard of the object
			//--------------------------------------------------
			homePage.taskPanel.clickItem("Properties");//Clicks the properties option from task pane

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard of existing object

			Log.message("3. Existing object popped out metadatacard is opened via task pane");

			//Verification : Verify if metadata configurability is working as expected 
			//------------------------------------------------------------------------
			if(!metadataCard.isPropertyGroupDisplayed(2, dataPool.get("GroupText")) && !metadataCard.propertyExists(dataPool.get("Property")))
				Log.pass("Test Case Passed. Metadata configurability is working as expected while using IsHidden attribute. [Existing object popout metadata card]");
			else
				Log.fail("Test Case Failed. Metadata configurability is not working as expected while using IsHidden attribute. [Existing object popout metadata card]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1.5.2.2B

	/**
	 * TC_1.5.2.3A : Verify if proeprty grouping with IsHidden (false/true) behaviour [Multiple Existing objects metadata card].
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if proeprty grouping with IsHidden (false/true) behaviour. [Multiple Existing objects metadata card].")
	public void TC_1_5_2_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to : " + viewToNavigate + " view.");

			//Step-2 : Select any existing object
			//------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));//Selects the multiple objects in the view

			Log.message("2. Multiple objects selected in the list view.");

			//Step-3: Instantiates the metadatacard
			//-------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard of existing object

			Log.message("3. Existing objects metadatacard is displayed");

			//Verification : Verify if metadata configurability is working as expected 
			//------------------------------------------------------------------------
			if(!metadataCard.isPropertyGroupDisplayed(2, dataPool.get("GroupText")) && !metadataCard.propertyExists(dataPool.get("Property")))
				Log.pass("Test Case Passed. Metadata configurability is working as expected while using IsHidden attribute. [Multiple Existing objects metadata card]");
			else
				Log.fail("Test Case Failed. Metadata configurability is not working as expected while using IsHidden attribute. [Multiple Existing objects metadata card]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1.5.2.3A

	/**
	 * TC_1.5.2.3B :Verify if proeprty grouping with IsHidden (false/true) behaviour. [Multiple Existing objects popout metadata card].
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if proeprty grouping with IsHidden (false/true) behaviour. [Multiple Existing objects popout metadata card].")
	public void TC_1_5_2_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to : " + viewToNavigate + " view.");

			//Step-2 : Select multiple existing objects
			//------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));//Selects the multiple objects in the view

			Log.message("2. Multiple objects selected in the list view.");

			//Step-3: Open the popout metadatacard of the object
			//--------------------------------------------------
			homePage.taskPanel.clickItem("Properties");//Clicks the properties option from task pane

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard of existing object

			Log.message("3. Existing object popped out metadatacard is opened via task pane");

			//Verification : Verify if metadata configurability is working as expected 
			//------------------------------------------------------------------------
			if(!metadataCard.isPropertyGroupDisplayed(2, dataPool.get("GroupText")) && !metadataCard.propertyExists(dataPool.get("Property")))
				Log.pass("Test Case Passed. Metadata configurability is working as expected while using IsHidden attribute. [Multiple Existing objects popout metadata card]");
			else
				Log.fail("Test Case Failed. Metadata configurability is not working as expected while using IsHidden attribute. [Multiple Existing objects popout metadata card]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1.5.2.3B

	/** 
	 * 1.5.4.1A : Verify the behavior of metadata configurability for property grouping with isCollapsible & isCollapsedByDefault attributes. [Object creation - Group has only "title" definition.] 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"FN119_MetadataConfigurability","Property Grouping"},description = "Verify the behavior of metadata configurability for property grouping with isCollapsible & isCollapsedByDefault attributes. [Object creation - Group has only title definition.]" )
	public void TC_1_5_4_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();
			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Open the new object metadatacard
			//----------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));//Clicks the new object link from task pane

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.setTemplate(dataPool.get("Template"));//Selects the template in the template selector

			String[] classValue= dataPool.get("Properties").split("::");//Gets the property and property value from test data

			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard
			metadataCard.setPropertyValue(classValue[0], classValue[1]);//Sets the property value in the metadatacard

			Log.message("1. " + dataPool.get("ObjectType") + " object type with class("+classValue[1]+") metadatacard is opened via New item in task pane");

			//Step-2: Checks the property group with header text is displayed in the metadatacard
			//----------------------------------------------------------------------------------------
			String ExpectedPropertyGroup = "";

			if(!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("GroupHeaderText")))
				throw new Exception("Test case failed. Property group with header text("+dataPool.get("GroupHeaderText")+") is not displayed in the metadatacard");

			Log.message("2. Property group with header text("+dataPool.get("GroupHeaderText")+") is displayed in the metadatcard", driver);

			//Step-3: Checks the property group behavior in the metadatacard
			//---------------------------------------------------------------------------------
			if(metadataCard.isPropertyGroupCollapsed(1))//Checks if group is expanded successfully in the metadatacard
				ExpectedPropertyGroup += " Property group is not expanded by default when isCollapsible & isCollapsedByDefault is not used.";

			if (!metadataCard.propertyExistsInGroup(1, dataPool.get("Property")))
				ExpectedPropertyGroup += " Property("+ dataPool.get("Property")+") is not displayed in the group.";

			metadataCard.expandPropertyGroup(1, false);//Collapse the property group in the metadatcard

			if(!metadataCard.isPropertyGroupCollapsed(1))//Checks if group is collapsed in the metadatacard 
				ExpectedPropertyGroup += " Property group is not collapsed while perform the click action in the header.";


			//Verifies if metadatacard is displayed with expected property group behavior in the metadatacard
			//-----------------------------------------------------------------------------------------------
			if (ExpectedPropertyGroup.equalsIgnoreCase(""))
				Log.pass("Test case passed. Metadata configurability is working as expected without isCollapsible & isCollapsedByDefault attributes.[Object creation - Group has only title definition.]", driver);
			else
				Log.fail("Test case failed. Metadata configurability is not working as expected without isCollapsible & isCollapsedByDefault attributes [Object creation - Group has only title definition.].[For more details:"+ExpectedPropertyGroup+"]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_5_4_1A

	/** 
	 * 1.5.4.1B : Verify the behavior of metadata configurability for property grouping with isCollapsible & isCollapsedByDefault attributes.  [Object creation - Add "isCollapsible":true and "isCollapsedByDefault":true definitions.] 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"FN119_MetadataConfigurability","Property Grouping"},description = "Verify the behavior of metadata configurability for property grouping with isCollapsible & isCollapsedByDefault attributes.  [Object creation - Add isCollapsible:true and isCollapsedByDefault:true definitions.]]" )
	public void TC_1_5_4_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();
			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Open the new object metadatacard
			//----------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//Clicks the new object link from new item menu

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.setTemplate(dataPool.get("Template"));//Selects the template in the template selector

			String[] classValue= dataPool.get("Properties").split("::");//Gets the property and property value from test data

			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard
			metadataCard.setPropertyValue(classValue[0], classValue[1]);//Sets the property value in the metadatacard

			Log.message("1. " + dataPool.get("ObjectType") + " object type with class("+classValue[1]+") metadatacard is opened via New item menu");

			//Step-2: Checks the property group with header text is displayed in the metadatacard
			//----------------------------------------------------------------------------------------
			String ExpectedPropertyGroup = "";

			if(!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("GroupHeaderText")))
				throw new Exception("Test case failed. Property group with header text("+dataPool.get("GroupHeaderText")+") is not displayed in the metadatacard");

			Log.message("2. Property group with header text("+dataPool.get("GroupHeaderText")+") is displayed in the metadatcard", driver);

			//Step-3: Checks the property group behavior in the metadatacard
			//---------------------------------------------------------------------------------
			if(!metadataCard.isPropertyGroupCollapsed(1))//Checks if group is expanded successfully in the metadatacard
				ExpectedPropertyGroup += " Property group is not collapsed by default when isCollapsedByDefault is set to true.";

			metadataCard.expandPropertyGroup(1, true);//Expands the property group in the metadatcard

			if(metadataCard.isPropertyGroupCollapsed(1))//Checks if group is collapsed in the metadatacard 
				ExpectedPropertyGroup += " Property group is not expanded while perform the click action in the header.";

			if (!metadataCard.propertyExistsInGroup(1, dataPool.get("Property")))
				ExpectedPropertyGroup += " Property("+ dataPool.get("Property")+") is not displayed in the group.";

			metadataCard.expandPropertyGroup(1, false);//Collapse the property group in the metadatcard

			if(!metadataCard.isPropertyGroupCollapsed(1))//Checks if group is collapsed in the metadatacard 
				ExpectedPropertyGroup += " Property group is not collapsed while perform the click action in the header.";


			//Verifies if metadatacard is displayed with expected property group behavior in the metadatacard
			//-----------------------------------------------------------------------------------------------
			if (ExpectedPropertyGroup.equalsIgnoreCase(""))
				Log.pass("Test case passed. Metadata configurability is working as expected without isCollapsible & isCollapsedByDefault attributes. [Object creation - Add isCollapsible:true and isCollapsedByDefault:true definitions.]", driver);
			else
				Log.fail("Test case failed. Metadata configurability is not working as expected without isCollapsible & isCollapsedByDefault attributes [Object creation - Add isCollapsible:true and isCollapsedByDefault:true definitions.].[For more details:"+ExpectedPropertyGroup+"]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_5_4_1B

	/** 
	 * 1.5.4.1C : Verify the behavior of metadata configurability for property grouping with isCollapsible & isCollapsedByDefault attributes.   [Object creation - "isCollapsible":true and "isCollapsedByDefault":false] 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"FN119_MetadataConfigurability","Property Grouping"},description = "Verify the behavior of metadata configurability for property grouping with isCollapsible & isCollapsedByDefault attributes.   [Object creation - isCollapsible:true and isCollapsedByDefault:false]" )
	public void TC_1_5_4_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Open the new object metadatacard via object property
			//-----------------------------------
			SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard of existing object
			metadataCard.clickAddValueButton(dataPool.get("ObjectType"));//Clicks the Add value button to open the new customer object metadatacard

			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.setTemplate(dataPool.get("Template"));//Selects the template in the template selector

			String[] classValue= dataPool.get("Properties").split("::");//Gets the property and property value from test data

			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard
			metadataCard.setPropertyValue(classValue[0], classValue[1]);//Sets the property value in the metadatacard

			Log.message("1. " + dataPool.get("ObjectType") + " object type with class("+classValue[1]+") metadatacard is opened via Object property");

			//Step-2: Checks the property group with header text is displayed in the metadatacard
			//----------------------------------------------------------------------------------------
			String ExpectedPropertyGroup = "";

			if(!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("GroupHeaderText")))
				throw new Exception("Test case failed. Property group with header text("+dataPool.get("GroupHeaderText")+") is not displayed in the metadatacard");

			Log.message("2. Property group with header text("+dataPool.get("GroupHeaderText")+") is displayed in the metadatcard", driver);

			//Step-3: Checks the property group behavior in the metadatacard
			//---------------------------------------------------------------------------------
			if(metadataCard.isPropertyGroupCollapsed(1))//Checks if group is expanded successfully in the metadatacard
				ExpectedPropertyGroup += " Property group is not expanded by default when isCollapsedByDefault is set to false.";

			metadataCard.expandPropertyGroup(1, true);//Expands the property group in the metadatcard

			if(metadataCard.isPropertyGroupCollapsed(1))//Checks if group is collapsed in the metadatacard 
				ExpectedPropertyGroup += " Property group is not expanded while perform the click action in the header.";

			if (!metadataCard.propertyExistsInGroup(1, dataPool.get("Property")))
				ExpectedPropertyGroup += " Property("+ dataPool.get("Property")+") is not displayed in the group.";

			metadataCard.expandPropertyGroup(1, false);//Collapse the property group in the metadatcard

			if(!metadataCard.isPropertyGroupCollapsed(1))//Checks if group is collapsed in the metadatacard 
				ExpectedPropertyGroup += " Property group is not collapsed while perform the click action in the header.";


			//Verifies if metadatacard is displayed with expected property group behavior in the metadatacard
			//-----------------------------------------------------------------------------------------------
			if (ExpectedPropertyGroup.equalsIgnoreCase(""))
				Log.pass("Test case passed. Metadata configurability is working as expected without isCollapsible & isCollapsedByDefault attributes. [Object creation - isCollapsible:true and isCollapsedByDefault:false]", driver);
			else
				Log.fail("Test case failed. Metadata configurability is not working as expected without isCollapsible & isCollapsedByDefault attributes [Object creation - isCollapsible:true and isCollapsedByDefault:false].[For more details:"+ExpectedPropertyGroup+"]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_5_4_1C

	/** 
	 * 1.5.4.1D : Verify the behavior of metadata configurability for property grouping with isCollapsible & isCollapsedByDefault attributes.   [Object creation - "isCollapsible":false and "isCollapsedByDefault":true] 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"FN119_MetadataConfigurability","Property Grouping"},description = "Verify the behavior of metadata configurability for property grouping with isCollapsible & isCollapsedByDefault attributes.   [Object creation - isCollapsible:false and isCollapsedByDefault:true]" )
	public void TC_1_5_4_1D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Open the new object metadatacard
			//----------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));//Clicks the new object link from task pane

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.setTemplate(dataPool.get("Template"));//Selects the template in the template selector

			String[] classValue= dataPool.get("Properties").split("::");//Gets the property and property value from test data

			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard
			metadataCard.setPropertyValue(classValue[0], classValue[1]);//Sets the property value in the metadatacard

			Log.message("1. " + dataPool.get("ObjectType") + " object type with class("+classValue[1]+") metadatacard is opened via New item in task pane");

			//Step-2: Checks the property group with header text is displayed in the metadatacard
			//----------------------------------------------------------------------------------------
			String ExpectedPropertyGroup = "";

			if(!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("GroupHeaderText")))
				throw new Exception("Test case failed. Property group with header text("+dataPool.get("GroupHeaderText")+") is not displayed in the metadatacard");

			Log.message("2. Property group with header text("+dataPool.get("GroupHeaderText")+") is displayed in the metadatcard", driver);

			//Step-3: Checks the property group behavior in the metadatacard
			//---------------------------------------------------------------------------------
			if(metadataCard.isPropertyGroupCollapsed(1))//Checks if group is expanded successfully in the metadatacard
				ExpectedPropertyGroup += " Property group is not collapsed by default when isCollapsedByDefault is set to true.";

			metadataCard.expandPropertyGroup(1, false);//Expands the property group in the metadatcard

			if (!metadataCard.propertyExistsInGroup(1, dataPool.get("Property")))
				ExpectedPropertyGroup += " Property("+ dataPool.get("Property")+") is not displayed in the group.";

			if(metadataCard.isPropertyGroupCollapsed(1))//Checks if group is collapsed in the metadatacard 
				ExpectedPropertyGroup += " Property group is collapsed while perform the click action in the header when IsCollapsible is set to false.";

			//Verifies if metadatacard is displayed with expected property group behavior in the metadatacard
			//-----------------------------------------------------------------------------------------------
			if (ExpectedPropertyGroup.equalsIgnoreCase(""))
				Log.pass("Test case passed. Metadata configurability is working as expected without isCollapsible & isCollapsedByDefault attributes. [Object creation - isCollapsible:false and isCollapsedByDefault:true]", driver);
			else
				Log.fail("Test case failed. Metadata configurability is not working as expected without isCollapsible & isCollapsedByDefault attributes [Object creation - isCollapsible:false and isCollapsedByDefault:true].[For more details:"+ExpectedPropertyGroup+"]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_5_4_1D

	/** 
	 * 1.5.4.1E : Verify the behavior of metadata configurability for property grouping with isCollapsible & isCollapsedByDefault attributes.   [Object creation - "isCollapsible":false and "isCollapsedByDefault":false] 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"FN119_MetadataConfigurability","Property Grouping"},description = "Verify the behavior of metadata configurability for property grouping with isCollapsible & isCollapsedByDefault attributes.   [Object creation - isCollapsible:false and isCollapsedByDefault:false]" )
	public void TC_1_5_4_1E(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try {


			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Open the new object metadatacard via object property
			//-----------------------------------
			SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard of existing object

			metadataCard.clickAddValueButton(dataPool.get("ObjectType"));//Clicks the Add value button to open the new customer object metadatacard

			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.setTemplate(dataPool.get("Template"));//Selects the template in the template selector

			String[] classValue= dataPool.get("Properties").split("::");//Gets the property and property value from test data

			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.setPropertyValue(classValue[0], classValue[1]);//Sets the property value in the metadatacard

			Log.message("1. " + dataPool.get("ObjectType") + " object type with class("+classValue[1]+") metadatacard is opened via Object property");

			//Step-2: Checks the property group with header text is displayed in the metadatacard
			//----------------------------------------------------------------------------------------
			String ExpectedPropertyGroup = "";

			if(!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("GroupHeaderText")))
				throw new Exception("Test case failed. Property group with header text("+dataPool.get("GroupHeaderText")+") is not displayed in the metadatacard");

			Log.message("2. Property group with header text("+dataPool.get("GroupHeaderText")+") is displayed in the metadatcard", driver);

			//Step-3: Checks the property group behavior in the metadatacard
			//---------------------------------------------------------------------------------
			if(metadataCard.isPropertyGroupCollapsed(1))//Checks if group is expanded successfully in the metadatacard
				ExpectedPropertyGroup += " Property group is not expanded by default when isCollapsedByDefault is set to false.";

			metadataCard.expandPropertyGroup(1, false);//Collapse the property group in the metadatcard

			if(metadataCard.isPropertyGroupCollapsed(1))//Checks if group is collapsed in the metadatacard 
				ExpectedPropertyGroup += " Property group is collapsed while perform the click action in the header whne IsCollapsible in set to false.";

			if (!metadataCard.propertyExistsInGroup(1, dataPool.get("Property")))
				ExpectedPropertyGroup += " Property("+ dataPool.get("Property")+") is not displayed in the group.";

			//Verifies if metadatacard is displayed with expected property group behavior in the metadatacard
			//-----------------------------------------------------------------------------------------------
			if (ExpectedPropertyGroup.equalsIgnoreCase(""))
				Log.pass("Test case passed. Metadata configurability is working as expected without isCollapsible & isCollapsedByDefault attributes. [Object creation - isCollapsible:false and isCollapsedByDefault:false]", driver);
			else
				Log.fail("Test case failed. Metadata configurability is not working as expected without isCollapsible & isCollapsedByDefault attributes [Object creation - isCollapsible:false and isCollapsedByDefault:false].[For more details:"+ExpectedPropertyGroup+"]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_5_4_1E

	/**
	 * 1.5.4.2A : Verify the behavior of metadata configurability for property grouping with isCollapsible & isCollapsedByDefault attributes. [Existing object - Group has only title definition.] 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"FN119_MetadataConfigurability","Property Grouping"},description = "Verify the behavior of metadata configurability for property grouping with isCollapsible & isCollapsedByDefault attributes. [Existing object - Group has only title definition.]" )
	public void TC_1_5_4_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Open the new object metadatacard via object property
			//-----------------------------------
			SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard

			metadataCard.setInfo(dataPool.get("Properties"));//Sets the class in the metadatacard

			Log.message("1. Existing Object(" + dataPool.get("Object") + ")  with class("+dataPool.get("Class")+") is selected in the view");

			//Step-2: Checks the property group with header text is displayed in the metadatacard
			//----------------------------------------------------------------------------------------
			String ExpectedPropertyGroup = "";

			if(!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("GroupHeaderText")))
				throw new Exception("Test case failed. Property group with header text("+dataPool.get("GroupHeaderText")+") is not displayed in the metadatacard");

			Log.message("2. Property group with header text("+dataPool.get("GroupHeaderText")+") is displayed in the metadatcard", driver);

			//Step-3: Checks the property group behavior in the metadatacard
			//---------------------------------------------------------------------------------
			if(metadataCard.isPropertyGroupCollapsed(1))//Checks if group is expanded successfully in the metadatacard
				ExpectedPropertyGroup += " Property group is not expanded by default when isCollapsible & isCollapsedByDefault is not used.";

			if (!metadataCard.propertyExistsInGroup(1, dataPool.get("Property")))
				ExpectedPropertyGroup += " Property("+ dataPool.get("Property")+") is not displayed in the group.";

			metadataCard.expandPropertyGroup(1, false);//Collapse the property group in the metadatcard

			if(!metadataCard.isPropertyGroupCollapsed(1))//Checks if group is collapsed in the metadatacard 
				ExpectedPropertyGroup += " Property group is not collapsed while perform the click action in the header.";


			//Verifies if metadatacard is displayed with expected property group behavior in the metadatacard
			//-----------------------------------------------------------------------------------------------
			if (ExpectedPropertyGroup.equalsIgnoreCase(""))
				Log.pass("Test case passed. Metadata configurability is working as expected without isCollapsible & isCollapsedByDefault attributes.[Existing object - Group has only title definition.]", driver);
			else
				Log.fail("Test case failed. Metadata configurability is not working as expected without isCollapsible & isCollapsedByDefault attributes [Existing object - Group has only title definition.].[For more details:"+ExpectedPropertyGroup+"]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_5_4_2A

	/** 
	 * 1.5.4.2B : Verify the behavior of metadata configurability for property grouping with isCollapsible & isCollapsedByDefault attributes.  [Existing object - Add "isCollapsible":true and "isCollapsedByDefault":true definitions.] 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"FN119_MetadataConfigurability","Property Grouping"},description = "Verify the behavior of metadata configurability for property grouping with isCollapsible & isCollapsedByDefault attributes.  [Existing object - Add isCollapsible:true and isCollapsedByDefault:true definitions.]]" )
	public void TC_1_5_4_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Open the new object metadatacard via object property
			//-----------------------------------
			SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			Log.message("1. Existing Object(" + dataPool.get("Object") + ")  with class("+dataPool.get("Class")+") is selected in the view");

			//Step-2: Open the popout metadatacard of the object
			//---------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);//Clicks the property option from task pane

			Log.message("2. Popout metadatacard of the object is opened via task pane");

			//Step-3: Checks the property group with header text is displayed in the metadatacard
			//----------------------------------------------------------------------------------------
			String ExpectedPropertyGroup = "";

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			if(!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("GroupHeaderText")))
				throw new Exception("Test case failed. Property group with header text("+dataPool.get("GroupHeaderText")+") is not displayed in the metadatacard");

			Log.message("3. Property group with header text("+dataPool.get("GroupHeaderText")+") is displayed in the metadatcard", driver);

			//Step-3: Checks the property group behavior in the metadatacard
			//---------------------------------------------------------------------------------
			if(!metadataCard.isPropertyGroupCollapsed(1))//Checks if group is expanded successfully in the metadatacard
				ExpectedPropertyGroup += " Property group is not collapsed by default when isCollapsedByDefault is set to true.";

			metadataCard.expandPropertyGroup(1, true);//Expands the property group in the metadatcard

			if(metadataCard.isPropertyGroupCollapsed(1))//Checks if group is collapsed in the metadatacard 
				ExpectedPropertyGroup += " Property group is not expanded while perform the click action in the header.";

			if (!metadataCard.propertyExistsInGroup(1, dataPool.get("Property")))
				ExpectedPropertyGroup += " Property("+ dataPool.get("Property")+") is not displayed in the group.";

			metadataCard.expandPropertyGroup(1, false);//Collapse the property group in the metadatcard

			if(!metadataCard.isPropertyGroupCollapsed(1))//Checks if group is collapsed in the metadatacard 
				ExpectedPropertyGroup += " Property group is not collapsed while perform the click action in the header.";


			//Verifies if metadatacard is displayed with expected property group behavior in the metadatacard
			//-----------------------------------------------------------------------------------------------
			if (ExpectedPropertyGroup.equalsIgnoreCase(""))
				Log.pass("Test case passed. Metadata configurability is working as expected without isCollapsible & isCollapsedByDefault attributes. [Existing object- Add isCollapsible:true and isCollapsedByDefault:true definitions.]", driver);
			else
				Log.fail("Test case failed. Metadata configurability is not working as expected without isCollapsible & isCollapsedByDefault attributes [Existing object - Add isCollapsible:true and isCollapsedByDefault:true definitions.].[For more details:"+ExpectedPropertyGroup+"]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_5_4_2B

	/** 
	 * 1.5.4.2C : Verify the behavior of metadata configurability for property grouping with isCollapsible & isCollapsedByDefault attributes.   [Existing object - "isCollapsible":true and "isCollapsedByDefault":false] 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"FN119_MetadataConfigurability","Property Grouping"},description = "Verify the behavior of metadata configurability for property grouping with isCollapsible & isCollapsedByDefault attributes.   [Existing object - isCollapsible:true and isCollapsedByDefault:false]" )
	public void TC_1_5_4_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Open the new object metadatacard via object property
			//-----------------------------------
			SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			if (!homePage.listView.clickItem(dataPool.get("Object")))//Selects the specified object
				throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			Log.message("1. Existing Object(" + dataPool.get("Object") + ")  with class("+dataPool.get("Class")+") is selected in the view");

			//Step-2: Checks the property group with header text is displayed in the metadatacard
			//----------------------------------------------------------------------------------------
			String ExpectedPropertyGroup = "";

			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if(!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("GroupHeaderText")))
				throw new Exception("Test case failed. Property group with header text("+dataPool.get("GroupHeaderText")+") is not displayed in the metadatacard");

			Log.message("2. Property group with header text("+dataPool.get("GroupHeaderText")+") is displayed in the metadatcard", driver);

			//Step-3: Checks the property group behavior in the metadatacard
			//---------------------------------------------------------------------------------
			if(metadataCard.isPropertyGroupCollapsed(1))//Checks if group is expanded successfully in the metadatacard
				ExpectedPropertyGroup += " Property group is not expanded by default when isCollapsedByDefault is set to false.";

			metadataCard.expandPropertyGroup(1, true);//Expands the property group in the metadatcard

			if(metadataCard.isPropertyGroupCollapsed(1))//Checks if group is collapsed in the metadatacard 
				ExpectedPropertyGroup += " Property group is not expanded while perform the click action in the header.";

			if (!metadataCard.propertyExistsInGroup(1, dataPool.get("Property")))
				ExpectedPropertyGroup += " Property("+ dataPool.get("Property")+") is not displayed in the group.";

			metadataCard.expandPropertyGroup(1, false);//Collapse the property group in the metadatcard

			if(!metadataCard.isPropertyGroupCollapsed(1))//Checks if group is collapsed in the metadatacard 
				ExpectedPropertyGroup += " Property group is not collapsed while perform the click action in the header.";


			//Verifies if metadatacard is displayed with expected property group behavior in the metadatacard
			//-----------------------------------------------------------------------------------------------
			if (ExpectedPropertyGroup.equalsIgnoreCase(""))
				Log.pass("Test case passed. Metadata configurability is working as expected without isCollapsible & isCollapsedByDefault attributes. [Existing object - isCollapsible:true and isCollapsedByDefault:false]", driver);
			else
				Log.fail("Test case failed. Metadata configurability is not working as expected without isCollapsible & isCollapsedByDefault attributes [Existing object - isCollapsible:true and isCollapsedByDefault:false].[For more details:"+ExpectedPropertyGroup+"]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_5_4_2C

	/** 
	 * 1.5.4.2D : Verify the behavior of metadata configurability for property grouping with isCollapsible & isCollapsedByDefault attributes.   [Object creation - "isCollapsible":false and "isCollapsedByDefault":true] 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"FN119_MetadataConfigurability","Property Grouping"},description = "Verify the behavior of metadata configurability for property grouping with isCollapsible & isCollapsedByDefault attributes.   [Existing object - isCollapsible:false and isCollapsedByDefault:true]" )
	public void TC_1_5_4_2D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Open the new object metadatacard via object property
			//-----------------------------------
			SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			if (!homePage.listView.clickItem(dataPool.get("Object")))//Selects the specified object
				throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			Log.message("1. Existing Object(" + dataPool.get("Object") + ")  with class("+dataPool.get("Class")+") is selected in the view");

			//Step-2: Checks the property group with header text is displayed in the metadatacard
			//----------------------------------------------------------------------------------------
			String ExpectedPropertyGroup = "";

			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if(!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("GroupHeaderText")))
				throw new Exception("Test case failed. Property group with header text("+dataPool.get("GroupHeaderText")+") is not displayed in the metadatacard");

			Log.message("2. Property group with header text("+dataPool.get("GroupHeaderText")+") is displayed in the metadatcard", driver);

			//Step-3: Checks the property group behavior in the metadatacard
			//---------------------------------------------------------------------------------
			if(metadataCard.isPropertyGroupCollapsed(1))//Checks if group is expanded successfully in the metadatacard
				ExpectedPropertyGroup += " Property group is not collapsed by default when isCollapsedByDefault is set to true.";

			metadataCard.expandPropertyGroup(1, false);//Expands the property group in the metadatcard

			if (!metadataCard.propertyExistsInGroup(1, dataPool.get("Property")))
				ExpectedPropertyGroup += " Property("+ dataPool.get("Property")+") is not displayed in the group.";

			if(metadataCard.isPropertyGroupCollapsed(1))//Checks if group is collapsed in the metadatacard 
				ExpectedPropertyGroup += " Property group is collapsed while perform the click action in the header when IsCollapsible is set to false.";

			//Verifies if metadatacard is displayed with expected property group behavior in the metadatacard
			//-----------------------------------------------------------------------------------------------
			if (ExpectedPropertyGroup.equalsIgnoreCase(""))
				Log.pass("Test case passed. Metadata configurability is working as expected without isCollapsible & isCollapsedByDefault attributes. [Existing object - isCollapsible:false and isCollapsedByDefault:true]", driver);
			else
				Log.fail("Test case failed. Metadata configurability is not working as expected without isCollapsible & isCollapsedByDefault attributes [Existing object - isCollapsible:false and isCollapsedByDefault:true].[For more details:"+ExpectedPropertyGroup+"]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_5_4_2D

	/** 
	 * 1.5.4.2E : Verify the behavior of metadata configurability for property grouping with isCollapsible & isCollapsedByDefault attributes.   [Existing object - "isCollapsible":false and "isCollapsedByDefault":false] 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"FN119_MetadataConfigurability","Property Grouping"},description = "Verify the behavior of metadata configurability for property grouping with isCollapsible & isCollapsedByDefault attributes.   [Existing object - isCollapsible:false and isCollapsedByDefault:false]" )
	public void TC_1_5_4_2E(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Open the new object metadatacard via object property
			//-----------------------------------
			SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			if (!homePage.listView.clickItem(dataPool.get("Object")))//Selects the specified object
				throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			Log.message("1. Existing Object(" + dataPool.get("Object") + ")  with class("+dataPool.get("Class")+") is selected in the view");

			//Step-2: Checks the property group with header text is displayed in the metadatacard
			//----------------------------------------------------------------------------------------
			String ExpectedPropertyGroup = "";

			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if(!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("GroupHeaderText")))
				throw new Exception("Test case failed. Property group with header text("+dataPool.get("GroupHeaderText")+") is not displayed in the metadatacard");

			Log.message("2. Property group with header text("+dataPool.get("GroupHeaderText")+") is displayed in the metadatcard", driver);

			//Step-3: Checks the property group behavior in the metadatacard
			//---------------------------------------------------------------------------------
			if(metadataCard.isPropertyGroupCollapsed(1))//Checks if group is expanded successfully in the metadatacard
				ExpectedPropertyGroup += " Property group is not expanded by default when isCollapsedByDefault is set to false.";

			metadataCard.expandPropertyGroup(1, true);//Expands the property group in the metadatcard

			if(metadataCard.isPropertyGroupCollapsed(1))//Checks if group is collapsed in the metadatacard 
				ExpectedPropertyGroup += " Property group is not expanded while perform the click action in the header.";

			if (!metadataCard.propertyExistsInGroup(1, dataPool.get("Property")))
				ExpectedPropertyGroup += " Property("+ dataPool.get("Property")+") is not displayed in the group.";

			//Verifies if metadatacard is displayed with expected property group behavior in the metadatacard
			//-----------------------------------------------------------------------------------------------
			if (ExpectedPropertyGroup.equalsIgnoreCase(""))
				Log.pass("Test case passed. Metadata configurability is working as expected without isCollapsible & isCollapsedByDefault attributes. [Existing object - isCollapsible:false and isCollapsedByDefault:false]", driver);
			else
				Log.fail("Test case failed. Metadata configurability is not working as expected without isCollapsible & isCollapsedByDefault attributes [Existing object - isCollapsible:false and isCollapsedByDefault:false].[For more details:"+ExpectedPropertyGroup+"]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_5_4_2E

	/**
	 * 1.5.5.1A : Verify the metadata configurability of property grouping - Title text of the group. [Object Creation - property group which has empty title, no other group definitions.] 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"FN119_MetadataConfigurability","Property Grouping"},description = "Verify the metadata configurability of property grouping - Title text of the group. [Object Creation - property group which has empty title, no other group definitions.]" )
	public void TC_1_5_5_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Open the new object metadatacard
			//----------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));//Clicks the new object link from task pane

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.setTemplate(dataPool.get("Template"));//Selects the template in the template selector

			String[] classValue= dataPool.get("Properties").split("::");//Gets the property and property value from test data

			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard
			metadataCard.setPropertyValue(classValue[0], classValue[1]);//Sets the property value in the metadatacard

			Log.message("1. " + dataPool.get("ObjectType") + " object type with class("+classValue[1]+") metadatacard is opened via New item in task pane");

			//Step-2: Checks the property group with blank header is displayed in the metadatacard
			//----------------------------------------------------------------------------------------
			String ExpectedPropertyGroup = "";

			if(!metadataCard.isPropertyGroupDisplayed(1, ""))
				throw new Exception("Test case failed. Property group with blank header is not displayed in the metadatacard");

			Log.message("2. Property group with blank header is displayed in the metadatcard", driver);

			//Step-3: Checks the property group with blank header behavior in the metadatacard
			//---------------------------------------------------------------------------------
			metadataCard.expandPropertyGroup(1, true);//Expands the property group in the metadatcard

			if(metadataCard.isPropertyGroupCollapsed(1))//Checks if group is expanded successfully in the metadatacard
				ExpectedPropertyGroup += " Property group is not expanded while perform the click action in the header.";

			if (!metadataCard.propertyExistsInGroup(1, dataPool.get("Property")))
				ExpectedPropertyGroup += " Property("+ dataPool.get("Property")+") is not displayed in the group with blank header.";

			metadataCard.expandPropertyGroup(1, false);//Collapse the property group in the metadatcard

			if(!metadataCard.isPropertyGroupCollapsed(1))//Checks if group is collapsed in the metadatacard 
				ExpectedPropertyGroup += " Property group is not collapsed while perform the click action in the header.";

			//Verifies if metadatacard is displayed with expected property group behavior in the metadatacard
			//-----------------------------------------------------------------------------------------------
			if (ExpectedPropertyGroup.equalsIgnoreCase(""))
				Log.pass("Test case passed. Metadata configurability is working as expected.[Object Creation - property group which has empty title, no other group definitions.]", driver);
			else
				Log.fail("Test case failed. Metadata configurability is not working as expected (Object Creation - property group which has empty title, no other group definitions.).[For more details:"+ExpectedPropertyGroup+"]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_5_5_1A

	/**
	 * 1.5.5.1B : Verify the metadata configurability of property grouping - The title text of the group. [Object Creation - Some text in title & no other group definitions.] 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"FN119_MetadataConfigurability","Property Grouping"},description = "Verify the metadata configurability of property grouping - The title text of the group. [Object Creation - Some text in title & no other group definitions.]" )
	public void TC_1_5_5_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Open the new object metadatacard
			//----------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//Clicks the new object link from new item menu

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.setTemplate(dataPool.get("Template"));//Selects the template in the template selector

			Log.message("1. " + dataPool.get("ObjectType") + " object type metadatacard is opened via New item menu in MenuBar");

			//Step-2: Sets the property value in the metadatacard
			//---------------------------------------------------
			String[] properties = dataPool.get("Properties").split("\n");

			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.setPropertyValue(properties[0].split("::")[0], properties[0].split("::")[1]);//Sets the property value in the metadatacard

			metadataCard.setPropertyValue(properties[1].split("::")[0],properties[1].split("::")[1]);//Sets the property value in the metadatacard

			Log.message("2. Required values is set in the metadatacard");

			//Step-3: Checks the property group with header is displayed in the metadatacard
			//----------------------------------------------------------------------------------------
			String ExpectedPropertyGroup = "";

			if(!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("GroupText")))
				throw new Exception("Test case failed. Property group with header text("+ dataPool.get("GroupText") +") is not displayed in the metadatacard");

			Log.message("3. Property group with header text("+dataPool.get("GroupText")+") is displayed in the metadatcard", driver);

			//Step-4: Checks the property group behavior in the metadatacard
			//---------------------------------------------------------------------------------
			if(!metadataCard.isPropertyGroupCollapsed(1))//Checks if group is expanded successfully in the metadatacard
				ExpectedPropertyGroup += " Property group is not collapsed by default when IsCollapsedByDefault is used.";

			metadataCard.expandPropertyGroup(1, true);//Expands the property group in the metadatcard

			if(!metadataCard.isPropertyGroupCollapsed(1)) {//Checks if group is expanded successfully in the metadatacard

				if (!metadataCard.propertyExistsInGroup(1, dataPool.get("Property1")))
					ExpectedPropertyGroup += " Property("+ dataPool.get("Property1")+") is not displayed in the group.";

				if (!metadataCard.propertyExistsInGroup(1, dataPool.get("Property2")))
					ExpectedPropertyGroup += " Property("+ dataPool.get("Property2")+") is not displayed in the group.";
			}
			else
				ExpectedPropertyGroup += " Property group is not expanded while perform the click action in the header.";

			metadataCard.expandPropertyGroup(1, false);//Collapse the property group in the metadatcard

			if(!metadataCard.isPropertyGroupCollapsed(1))//Checks if group is collapsed in the metadatacard 
				ExpectedPropertyGroup += " Property group is not collapsed while perform the click action in the header.";


			//Verifies if metadatacard is displayed with expected property group behavior in the metadatacard
			//-----------------------------------------------------------------------------------------------
			if (ExpectedPropertyGroup.equalsIgnoreCase(""))
				Log.pass("Test case passed. Metadata configurability is working as expected. The title text of the group. [Object Creation - Some text in title & no other group definitions.]", driver);
			else
				Log.fail("Test case failed. Metadata configurability is not working as expected. The title text of the group. [Object Creation - Some text in title & no other group definitions.).[For more details:"+ExpectedPropertyGroup+"]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_5_5_1B

	/**
	 * 1.5.5.1C : Verify the metadata configurability of property grouping - The title text of the group. [Object Creation - with hasHeader:false definition.] 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"FN119_MetadataConfigurability","Property Grouping"},description = "Verify the metadata configurability of property grouping - The title text of the group. [Object Creation - with hasHeader:false definition.]" )
	public void TC_1_5_5_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Open the new object metadatacard
			//----------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//Clicks the new object link from new item menu

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.setTemplate(dataPool.get("Template"));//Selects the template in the template selector

			Log.message("1. " + dataPool.get("ObjectType") + " object type metadatacard is opened via New item menu in MenuBar");

			//Step-2: Sets the property value in the metadatacard
			//---------------------------------------------------
			String[] properties = dataPool.get("Properties").split("\n");

			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.setPropertyValue(properties[0].split("::")[0], properties[0].split("::")[1]);//Sets the property value in the metadatacard

			metadataCard.setPropertyValue(properties[1].split("::")[0],properties[1].split("::")[1]);//Sets the property value in the metadatacard

			Log.message("2. Required values is set in the metadatacard");

			//Verifies if metadatacard is displayed with expected property group behavior in the metadatacard
			//-----------------------------------------------------------------------------------------------
			if ((!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("GroupText"))) && (metadataCard.propertyExists(dataPool.get("Property"))))
				Log.pass("Test case passed. Metadata configurability is working as expected. The title text of the group. [Object Creation - with hasHeader:false definition.]", driver);
			else
				Log.fail("Test case failed. Metadata configurability is not working as expected. The title text of the group. [Object Creation - with hasHeader:false definition.]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_5_5_1C

	/**
	 * 1.5.5.1D : Verify the metadata configurability of property grouping - The title text of the group. [Object Creation - with hasHeader:true definition.] 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"FN119_MetadataConfigurability","Property Grouping"},description = "Verify the metadata configurability of property grouping - The title text of the group. [Object Creation - with hasHeader:true definition.]" )
	public void TC_1_5_5_1D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Open the new object metadatacard
			//----------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//Clicks the new object link from new item menu

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.setTemplate(dataPool.get("Template"));//Selects the template in the template selector

			Log.message("1. " + dataPool.get("ObjectType") + " object type metadatacard is opened via New item menu in MenuBar");

			//Step-2: Sets the property value in the metadatacard
			//---------------------------------------------------
			String[] properties = dataPool.get("Properties").split("\n");

			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.setPropertyValue(properties[0].split("::")[0], properties[0].split("::")[1]);//Sets the property value in the metadatacard

			metadataCard.setPropertyValue(properties[1].split("::")[0],properties[1].split("::")[1]);//Sets the property value in the metadatacard

			Log.message("2. Required values is set in the metadatacard");

			//Step-3: Checks the property group with header is displayed in the metadatacard
			//----------------------------------------------------------------------------------------
			String ExpectedPropertyGroup = "";

			if(!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("GroupText")))
				throw new Exception("Test case failed. Property group with header text("+ dataPool.get("GroupText") +") is not displayed in the metadatacard");

			Log.message("3. Property group with header text("+dataPool.get("GroupText")+") is displayed in the metadatcard", driver);

			//Step-4: Checks the property group behavior in the metadatacard
			//---------------------------------------------------------------------------------
			if(metadataCard.isPropertyGroupCollapsed(1))//Checks if group is expanded successfully in the metadatacard
				ExpectedPropertyGroup += " Property group is not in expanded state by default when no behavior is mentioned.";

			metadataCard.expandPropertyGroup(1, true);//Expands the property group in the metadatcard

			if(!metadataCard.isPropertyGroupCollapsed(1)) {//Checks if group is expanded successfully in the metadatacard

				if (!metadataCard.propertyExistsInGroup(1, dataPool.get("Property1")))
					ExpectedPropertyGroup += " Property("+ dataPool.get("Property1")+") is not displayed in the group.";
			}
			else
				ExpectedPropertyGroup += " Property group is not expanded while perform the click action in the header.";

			metadataCard.expandPropertyGroup(1, false);//Collapse the property group in the metadatcard

			if(!metadataCard.isPropertyGroupCollapsed(1))//Checks if group is collapsed in the metadatacard 
				ExpectedPropertyGroup += " Property group is not collapsed while perform the click action in the header.";


			//Verifies if metadatacard is displayed with expected property group behavior in the metadatacard
			//-----------------------------------------------------------------------------------------------
			if (ExpectedPropertyGroup.equalsIgnoreCase(""))
				Log.pass("Test case passed. Metadata configurability is working as expected. The title text of the group. [Object Creation - with hasHeader:true definition.]", driver);
			else
				Log.fail("Test case failed. Metadata configurability is not working as expected. The title text of the group. (Object Creation - with hasHeader:true definition.).[For more details:"+ExpectedPropertyGroup+"]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_5_5_1D

	/**
	 * 1.5.5.2A : Verify the metadata configurability of property grouping - The title text of the group. [Existing Object metadatacard - property group which has empty title, no other group definitions.] 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"FN119_MetadataConfigurability","Property Grouping"},description = "Verify the metadata configurability of property grouping - The title text of the group. [Existing Object metadatacard - property group which has empty title, no other group definitions.]" )
	public void TC_1_5_5_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to : " + viewToNavigate + " view.");

			//Step-2 : Select any existing object
			//------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			Log.message("2. Selected the specified object : " + dataPool.get("Object") + " in list view.");

			//Step-3: Checks the property group with blank header is displayed in the metadatacard
			//----------------------------------------------------------------------------------------
			String ExpectedPropertyGroup = "";

			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard in right pane

			if(!metadataCard.isPropertyGroupDisplayed(1, ""))
				throw new Exception("Test case failed. Property group with blank header is not displayed in the metadatacard");

			Log.message("3. Property group with blank header is displayed in the metadatcard", driver);

			//Step-4: Checks the property group with blank header behavior in the metadatacard
			//---------------------------------------------------------------------------------
			metadataCard.expandPropertyGroup(1, true);//Expands the property group in the metadatcard

			if(metadataCard.isPropertyGroupCollapsed(1))//Checks if group is expanded successfully in the metadatacard
				ExpectedPropertyGroup += " Property group is not expanded while perform the click action in the header.";

			if (!metadataCard.propertyExistsInGroup(1, dataPool.get("Property")))
				ExpectedPropertyGroup += " Property("+ dataPool.get("Property")+") is not displayed in the group with blank header.";

			metadataCard.expandPropertyGroup(1, false);//Collapse the property group in the metadatcard

			if(!metadataCard.isPropertyGroupCollapsed(1))//Checks if group is collapsed in the metadatacard 
				ExpectedPropertyGroup += " Property group is not collapsed while perform the click action in the header.";

			//Verifies if metadatacard is displayed with expected property group behavior in the metadatacard
			//-----------------------------------------------------------------------------------------------
			if (ExpectedPropertyGroup.equalsIgnoreCase(""))
				Log.pass("Test case passed. Metadata configurability is working as expected.The title text of the group. [Existing Object metadatacard - property group which has empty title, no other group definitions.]", driver);
			else
				Log.fail("Test case failed. Metadata configurability is not working as expected The title text of the group. (Existing Object metadatacard - property group which has empty title, no other group definitions.).[For more details:"+ExpectedPropertyGroup+"]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_5_5_2A

	/**
	 * 1.5.5.2B : Verify the metadata configurability of property grouping - The title text of the group. [Existing object metadata card - Add some text to title, no other group definitions.] 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"FN119_MetadataConfigurability","Property Grouping"},description = "Verify the metadata configurability of property grouping - The title text of the group. [Existing object metadata card - Add some text to title, no other group definitions.]" )
	public void TC_1_5_5_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to : " + viewToNavigate + " view.");

			//Step-2 : Select any existing object
			//------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			homePage.menuBar.ClickOperationsMenu("Properties");//Opens the popped out metadatacard

			Log.message("2. Selected the specified object : " + dataPool.get("Object") + " in list view and popped out metadatacard of that object is opened via operation menu.");

			//Step-3: Sets the property value in the metadatacard
			//---------------------------------------------------
			String[] properties = dataPool.get("Properties").split("::");

			MetadataCard metadataCard = new MetadataCard(driver);

			metadataCard.setPropertyValue(properties[0], properties[1]);//Sets the property value in the metadatacard

			Log.message("3. Required value is set in the metadatacard");

			//Step-4: Checks the property group with header is displayed in the metadatacard
			//----------------------------------------------------------------------------------------
			String ExpectedPropertyGroup = "";

			if(!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("GroupText")))
				throw new Exception("Test case failed. Property group with header text("+ dataPool.get("GroupText") +") is not displayed in the metadatacard");

			Log.message("4. Property group with header text("+dataPool.get("GroupText")+") is displayed in the metadatcard", driver);

			//Step-4: Checks the property group behavior in the metadatacard
			//---------------------------------------------------------------------------------
			if(!metadataCard.isPropertyGroupCollapsed(1))//Checks if group is expanded successfully in the metadatacard
				ExpectedPropertyGroup += " Property group is not collapsed by default when IsCollapsedByDefault is used.";

			metadataCard.expandPropertyGroup(1, true);//Expands the property group in the metadatcard

			if(!metadataCard.isPropertyGroupCollapsed(1)) {//Checks if group is expanded successfully in the metadatacard

				if (!metadataCard.propertyExistsInGroup(1, dataPool.get("Property1")))
					ExpectedPropertyGroup += " Property("+ dataPool.get("Property1")+") is not displayed in the group.";

				if (!metadataCard.propertyExistsInGroup(1, dataPool.get("Property2")))
					ExpectedPropertyGroup += " Property("+ dataPool.get("Property2")+") is not displayed in the group.";
			}
			else
				ExpectedPropertyGroup += " Property group is not expanded while perform the click action in the header.";

			metadataCard.expandPropertyGroup(1, false);//Collapse the property group in the metadatcard

			if(!metadataCard.isPropertyGroupCollapsed(1))//Checks if group is collapsed in the metadatacard 
				ExpectedPropertyGroup += " Property group is not collapsed while perform the click action in the header.";


			//Verifies if metadatacard is displayed with expected property group behavior in the metadatacard
			//-----------------------------------------------------------------------------------------------
			if (ExpectedPropertyGroup.equalsIgnoreCase(""))
				Log.pass("Test case passed. Metadata configurability is working as expected. The title text of the group. [Existing object metadata card - Add some text to title, no other group definitions.]", driver);
			else
				Log.fail("Test case failed. Metadata configurability is not working as expected. The title text of the group. (Existing object metadata card - Add some text to title, no other group definitions.).[For more details:"+ExpectedPropertyGroup+"]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_5_5_2B

	/**
	 * 1.5.5.2C : Verify the metadata configurability of property grouping - The title text of the group. [Existing object metadata card - with hasHeader:false definition.] 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"FN119_MetadataConfigurability","Property Grouping"},description = "Verify the metadata configurability of property grouping - The title text of the group. [Existing object metadata card - with hasHeader:false definition.]" )
	public void TC_1_5_5_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try { 



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to : " + viewToNavigate + " view.");

			//Step-2 : Select any existing object
			//------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			Log.message("2. Selected the specified object : " + dataPool.get("Object") + " in list view");

			//Step-3: Sets the property value in the metadatacard
			//---------------------------------------------------
			String[] properties = dataPool.get("Properties").split("::");

			MetadataCard metadataCard = new MetadataCard(driver, true);

			metadataCard.setPropertyValue(properties[0], properties[1]);//Sets the property value in the metadatacard

			Log.message("3. Required value is set in the metadatacard");

			//Verifies if metadatacard is displayed with expected property group behavior in the metadatacard
			//-----------------------------------------------------------------------------------------------
			if ((!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("GroupText"))) && (metadataCard.propertyExists(dataPool.get("Property"))))
				Log.pass("Test case passed. Metadata configurability is working as expected. The title text of the group. [Existing object metadata card - with hasHeader:false definition.]", driver);
			else
				Log.fail("Test case failed. Metadata configurability is not working as expected. The title text of the group. [Existing object metadata card - with hasHeader:false definition.]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_5_5_2C

	/**
	 * 1.5.5.2D : Verify the metadata configurability of property grouping - The title text of the group. [Existing object metadata card - with hasHeader:true definition.] 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"FN119_MetadataConfigurability","Property Grouping"},description = "Verify the metadata configurability of property grouping - The title text of the group. [Existing object metadata card - with hasHeader:true definition.]" )
	public void TC_1_5_5_2D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to : " + viewToNavigate + " view.");

			//Step-2 : Select any existing object
			//------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			Log.message("2. Selected the specified object : " + dataPool.get("Object") + " in list view");

			//Step-3: Sets the property value in the metadatacard
			//---------------------------------------------------
			String[] properties = dataPool.get("Properties").split("::");

			MetadataCard metadataCard = new MetadataCard(driver, true);

			metadataCard.setPropertyValue(properties[0], properties[1]);//Sets the property value in the metadatacard

			Log.message("3. Required value is set in the metadatacard");

			//Step-4: Checks the property group with header is displayed in the metadatacard
			//----------------------------------------------------------------------------------------
			String ExpectedPropertyGroup = "";

			if(!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("GroupText")))
				throw new Exception("Test case failed. Property group with header text("+ dataPool.get("GroupText") +") is not displayed in the metadatacard");

			Log.message("4. Property group with header text("+dataPool.get("GroupText")+") is displayed in the metadatcard", driver);

			//Step-5: Checks the property group behavior in the metadatacard
			//---------------------------------------------------------------------------------

			if(metadataCard.isPropertyGroupCollapsed(1))//Checks if group is expanded successfully in the metadatacard
				ExpectedPropertyGroup += " Property group is not in expanded state by default when no behavior is mentioned.";

			metadataCard.expandPropertyGroup(1, true);//Expands the property group in the metadatcard

			if(!metadataCard.isPropertyGroupCollapsed(1)) {//Checks if group is expanded successfully in the metadatacard

				if (!metadataCard.propertyExistsInGroup(1, dataPool.get("Property1")))
					ExpectedPropertyGroup += " Property("+ dataPool.get("Property1")+") is not displayed in the group.";
			}
			else
				ExpectedPropertyGroup += " Property group is not expanded while perform the click action in the header.";

			metadataCard.expandPropertyGroup(1, false);//Collapse the property group in the metadatcard

			if(!metadataCard.isPropertyGroupCollapsed(1))//Checks if group is collapsed in the metadatacard 
				ExpectedPropertyGroup += " Property group is not collapsed while perform the click action in the header.";


			//Verifies if metadatacard is displayed with expected property group behavior in the metadatacard
			//-----------------------------------------------------------------------------------------------
			if (ExpectedPropertyGroup.equalsIgnoreCase(""))
				Log.pass("Test case passed. Metadata configurability is working as expected. The title text of the group. [Existing object metadata card - with hasHeader:true definition.]", driver);
			else
				Log.fail("Test case failed. Metadata configurability is not working as expected. The title text of the group. (Existing object metadata card - with hasHeader:true definition.).[For more details:"+ExpectedPropertyGroup+"]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_5_5_2D

	/**
	 *  TC_38252 :  Verify if FN119 Workflow alias names are showing correctly in fetched alias structure.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability","workflows","bugcases"}, 
			description = "Verify if FN119 Workflow alias names are showing correctly in fetched alias structure.")
	public void TC_38252(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Select the new project object from the menu bar
			//--------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Document.Value);//Click the project object from the new menu bar 

			Log.message("1. Selected the new 'Project' object type from the new menu bar.", driver);

			//Step-2 : Set the workflow and state as specified in configuration rule
			//----------------------------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard.setTemplate(dataPool.get("Template"));//Set the metadatacard template


			Log.message("2. Template : "+dataPool.get("Template")+"is selected in metadatacard.", driver);

			//Step-3 : set the specified property value in the document metadatacard
			//----------------------------------------------------------------------
			metadatacard = new MetadataCard(driver);//Instantiates the metadatacard
			metadatacard.setInfo(dataPool.get("Props"));//Set the property name and value in the metadatacard
			metadatacard.setCheckInImmediately(true);//Set the check in immediately 

			Log.message("3. Required property is entered in the selected metadatacard.");

			//Step-4 : Set the workflow and state for the specified metadatacard
			//------------------------------------------------------------------
			metadatacard.setWorkflow(dataPool.get("Workflow"));//Set the workflow in metadatacard

			Log.message("4. Workflow : "+dataPool.get("Workflow")+" and state : "+dataPool.get("State")+" is set as expected.");

			//Step-5 : Create the document object metadatacard
			//------------------------------------------------
			metadatacard.clickCreateBtn();//Click the created button for metadatacard

			Log.message("5. Created the document object from new menu bar.", driver);

			//Verify if Property is set in expected group 
			//-------------------------------------------
			metadatacard = new MetadataCard(driver,true);//Instantiate the right pane metadatacard

			if (!metadatacard.isPropertyGroupDisplayed(1, dataPool.get("Group1Text")) || !metadatacard.isPropertyGroupDisplayed(2, dataPool.get("Group2Text"))|| !metadatacard.isPropertyGroupDisplayed(3, dataPool.get("Group3Text")))//Checks if proeprty group is displayed in the metadatacard
				throw new Exception("Property Group is not displayed in the metadatacard.");

			Log.message("6. Expected property groups are displayed in the metadatacard");

			String ExpectedPropertyGroup = "";

			//Verify if specified property is exist in the group
			//--------------------------------------------------
			if (!metadatacard.propertyExistsInGroup(1, dataPool.get("Property1")))//Verify if specified property is exist in the group
				ExpectedPropertyGroup += " Property("+ dataPool.get("Property1")+") is not displayed in the group.";

			//Verify if specified property is exist in the group
			//--------------------------------------------------
			if (!metadatacard.propertyExistsInGroup(2, dataPool.get("Property2")))//Verify if specified property is exist in the group
				ExpectedPropertyGroup += " Property("+ dataPool.get("Property2")+") is not displayed in the group.";

			//Verify if specified property is exist in the group
			//--------------------------------------------------
			if (!metadatacard.propertyExistsInGroup(3, dataPool.get("Property3")))//Verify if specified property is exist in the group
				ExpectedPropertyGroup += " Property("+ dataPool.get("Property3")+") is not displayed in the group.";

			if(ExpectedPropertyGroup.equals(""))
				Log.pass("Test Case Passed.Metadata configurability is working as expected when set the rule with workflow alias names.", driver);
			else
				Log.fail("Test Case Failed.Metadata configurability is not working as expected when set the rule with workflow alias names.", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally{
			Utility.quitDriver(driver);
		}//End finally

	}//End TC_38252




}//End property grouping

package MFClient.Tests.Permissions;


import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.WebDriverUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.openqa.selenium.WebDriver;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import MFClient.Pages.HomePage;
import MFClient.Pages.LoginPage;
import MFClient.Wrappers.Caption;
import MFClient.Wrappers.ListView;
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class Permissions {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String userName = null;
	public static String password = null;
	public static String testVault = null;
	public static String driverType = null;
	public String methodName = null;
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
			password = xmlParameters.getParameter("Password");
			driverType = xmlParameters.getParameter("driverType");
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
	 * getMethodName : Gets the name of current executing method
	 */
	@BeforeMethod (alwaysRun=true)
	public void getMethodName(Method method) throws Exception {

		try {

			methodName = method.getName();

		} //End try

		catch(Exception e) {
			if (e.getClass().toString().contains("NullPointerException")) 
				throw new Exception ("Test data sheet does not exists.");
			else
				throw e;
		} //End catch		
	} //End getMethodName

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
	 * Permission_18325 : Check the permission text after selecting a permission class for existing document.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Automatic Permission"},
			description = "Check the permission text after selecting a permission class for existing document.")
	public void Permission_18325(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), "");//Navigate to the specific view

			homePage.listView.clickItem(dataPool.get("ObjectName"));//Selects the object in the view

			Log.message("1. Navigated to " + viewtonavigate + " and selected the object \"" + dataPool.get("ObjectName") +  "\" in the view.", driver);

			//Verification: Check if automatic permission popoup is displayed or not while selecting the automatic permission class
			//---------------------------------------------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadatacard
			String Expected = "";

			String bubbleMessage = metadataCard.setPropertyValue("Class", dataPool.get("ClassValue"));//Selects the automatic permission class in the metadatacard and Gets the bubble message from the metadatacard

			if (!bubbleMessage.equalsIgnoreCase(dataPool.get("ExpectedMessage")))
				Expected = " Expected bubble message("+ dataPool.get("ExpectedMessage") +") is not displayed after selecting the automatic permission class;";

			if (!metadataCard.getPermission().equalsIgnoreCase(dataPool.get("Permission")))
				Expected += " Permission text is not displayed as expected in the metadatacard after selecting the automatic permission class";

			if (Expected.equalsIgnoreCase(""))
				Log.pass("Test case passed. Automation permission bubble message is displayed with expected message while selecting the automatic permission class for existing object.", driver);
			else
				Log.fail("Test case failed. Automatic permission bubble message is not displayed while selecting the automatic permission class for existing object.[Additional Info : "+ Expected +"]", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_18325

	/**
	 * Permission_18326 : Check the permission text after selecting a permission property value for existing document.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Automatic Permission"},
			description = "Check the permission text after selecting a permission property value for existing document")
	public void Permission_18326(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), "");//Navigate to the specific view

			homePage.listView.clickItem(dataPool.get("ObjectName"));//Selects the object in the view

			Log.message("1. Navigated to " + viewtonavigate + " and selected the object \"" + dataPool.get("ObjectName") +  "\" in the view.", driver);

			//Verification: Check if automatic permission popoup is displayed or not while selecting the automatic permission class
			//---------------------------------------------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadatacard
			String Expected = "";

			String bubbleMessage = metadataCard.setPropertyValue(dataPool.get("AutomaticPermissionProp"), dataPool.get("AutomaticPermissionPropValue"));//Selects the automatic permission class in the metadatacard

			if (!bubbleMessage.equalsIgnoreCase(dataPool.get("ExpectedMessage")))
				Expected = " Expected bubble message("+ dataPool.get("ExpectedMessage") +") is not displayed after selecting the automatic permission class;";

			if (!metadataCard.getPermission().equalsIgnoreCase(dataPool.get("Permission")))
				Expected += " Permission text is not displayed as expected in the metadatacard after selecting the automatic permission class";

			if (Expected.equalsIgnoreCase(""))
				Log.pass("Test case passed. Automation permission bubble message is displayed with expected message while selecting the automatic permission value for the property in existing object metadata card.", driver);
			else
				Log.fail("Test case failed. Automatic permission bubble message is not displayed while selecting the automatic permission value for the property in existing object metadata card.[Additional Info : "+ Expected +"]", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_18326

	/**
	 * Permission_18327 : Check the permission text after selecting a newly created class with automatic permission for new document.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Automatic Permission"},
			description = "Check the permission text after selecting a newly created class with automatic permission for new document.")
	public void Permission_18327(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Open the new object metadatacard
			//----------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Document.Value);//Clicks the new document object link from task pane

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the popout metadatacard

			metadataCard.setTemplate(dataPool.get("Template"));//sets the template in the template selector

			Log.message("1. New document object metadatacard is opened via task panel.", driver);

			//Verification: Check if automatic permission popoup is displayed or not while selecting the automatic permission class
			//---------------------------------------------------------------------------------------------------------------------
			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard
			String Expected = "";

			String bubbleMessage = metadataCard.setPropertyValue("Class", dataPool.get("ClassValue"));//Selects the automatic permission class in the metadatacard

			if (!bubbleMessage.equalsIgnoreCase(dataPool.get("ExpectedMessage")))
				Expected = " Expected bubble message("+ dataPool.get("ExpectedMessage") +") is not displayed after selecting the automatic permission class;";

			if (!metadataCard.getPermission().equalsIgnoreCase(dataPool.get("Permission")))
				Expected += " Permission text is not displayed as expected in the metadatacard after selecting the automatic permission class";

			if (Expected.equalsIgnoreCase(""))
				Log.pass("Test case passed. Automation permission bubble message is displayed with expected message while selecting the automatic permission class ", driver);
			else
				Log.fail("Test case failed. Automatic permission bubble message is not displayed while selecting the automatic permission class.[Additional Info : "+ Expected +"]", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_18327

	/**
	 * Permission_18328 : Create a template with automatic permission class for multi class objects [eg: Document]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Automatic Permission"},
			description = "Create a template with automatic permission class for multi class objects [eg: Document]")
	public void Permission_18328(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//--------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Open the new object metadatacard
			//----------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Document.Value);//Clicks the new document object link from task pane

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the popout metadatacard

			metadataCard.setTemplate(dataPool.get("Template"));//sets the template in the template selector

			Log.message("1. New document object metadatacard is opened via task panel.", driver);

			//Step-2: Select the automatic permission class and then verify the automatic permission behavior
			//----------------------------------------------------------------------------------------------
			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			String bubbleMessage  = metadataCard.setPropertyValue("Class", dataPool.get("ClassValue"));//Selects the automatic permission class in the metadatacard

			Log.message("2. Automatic permission bubble message ("+ bubbleMessage +") is displayed while selecting the automatic permission class("+ dataPool.get("ClassValue") +") in the metadatacard.", driver);

			//Step-3: Create the template object with automatic permission class
			//------------------------------------------------------------------
			String objName = Utility.getObjectName(methodName);//Gets the object name

			metadataCard.setPropertyValue(dataPool.get("Title"), objName);//Sets the object name in the metadatacard

			metadataCard.setPropertyValue(dataPool.get("Properties").split("::")[0], dataPool.get("Properties").split("::")[1]);

			metadataCard.setCheckInImmediately(true);

			metadataCard.saveAndClose();//Creates the object

			Log.message("3. Template Object("+ objName +") is created with the automatic permission class("+dataPool.get("ClassValue")+") in the view", driver);

			//Verification if object successfully created
			//-------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), "");//Navigate to the specific view

			if (homePage.listView.isItemExists(objName+".txt"))
				Log.pass("Test case passed. Template object with automatic permission class for multi class object is created successfully [eg: Document]", driver);
			else
				Log.fail("Test case failed. Template object with automatic permission class for multi class object is not created successfully [eg: Document]", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_18328

	/**
	 * Permission_18329 : Create a new document using template which have automatic permission class.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Automatic Permission"},
			description = "Create a new document using template which have automatic permission class.", dependsOnMethods = "Permission_18328")
	public void Permission_18329(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//--------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Open the new object metadatacard
			//----------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Document.Value);//Clicks the new document object link from task pane

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the popout metadatacard

			metadataCard.setTemplate(dataPool.get("Template"));//sets the template in the template selector

			Log.message("1. Template with automatic permission class is selected and new document object metadatacard is opened via task panel.", driver);

			//Step-2: Create the template object with automatic permission class
			//------------------------------------------------------------------
			String objName = Utility.getObjectName(methodName);//Gets the object name

			metadataCard = new MetadataCard(driver);

			metadataCard.setPropertyValue(dataPool.get("Title"), objName);//Sets the object name in the metadatacard

			metadataCard.setCheckInImmediately(true);

			metadataCard.saveAndClose();//Creates the object

			Log.message("2. New Object("+ objName +") is created with the template which has automatic permission class("+dataPool.get("ClassValue")+") in the view", driver);

			//Verification if object successfully created
			//-------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), objName);//Navigate to the specific view

			if (homePage.listView.isItemExists(objName+".txt"))
				Log.pass("Test case passed. New object is successfully created using Template with automatic permission class for multi class object.", driver);
			else
				Log.fail("Test case failed. New object is not created using Template with automatic permission class for multi class object.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_18329

	/**
	 * Permission_18332 : Create a template with automatic permission property
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Automatic Permission"},
			description = "Create a template with automatic permission property")
	public void Permission_18332(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//-----------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Open the new object metadatacard
			//----------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Document.Value);//Clicks the new document object link from new item menu

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the popout metadatacard

			metadataCard.setTemplate(dataPool.get("Template"));//sets the template in the template selector

			Log.message("1. New document object metadatacard is opened via menu bar.", driver);

			//Step-2: Adds the automatic permission property and then verify the automatic permission behavior
			//----------------------------------------------------------------------------------------------
			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.setPropertyValue(dataPool.get("Properties").split("::")[0], dataPool.get("Properties").split("::")[1]);//Sets the Is template = yes in the metadatacard

			String bubbleMessage = metadataCard.setPropertyValue(dataPool.get("Property"), dataPool.get("PropValue"));//Sets the automatic permission value in the metadatacard

			Log.message("2. Automatic permission bubble message ("+ bubbleMessage +") is displayed while adding the automatic permission value("+ dataPool.get("PropValue") +") for the property("+ dataPool.get("Property") +") in the metadatacard.", driver);

			//Step-3: Create the template object with automatic permission property
			//---------------------------------------------------------------------
			String objName = Utility.getObjectName(methodName);//Gets the object name

			metadataCard.setPropertyValue(dataPool.get("Title"), objName);//Sets the object name in the metadatacard

			metadataCard.setCheckInImmediately(true);

			metadataCard.saveAndClose();//Creates the object

			Log.message("3. Template Object("+ objName +") is created with the automatic permission value("+dataPool.get("Property")+") for the property("+dataPool.get("PropValue")+") in the view", driver);

			//Verification if object successfully created
			//-------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), "");//Navigate to the specific view

			if (homePage.listView.isItemExists(objName+".doc"))
				Log.pass("Test case passed. Template object with automatic permission property is created successfully.", driver);
			else
				Log.fail("Test case failed. Template object with automatic permission property is not created successfully.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_18332

	/**
	 * Permission_18333 : Create a new document using template which have automatic permission property
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Automatic Permission"},
			description = "Create a new document using template which have automatic permission property", dependsOnMethods = "Permission_18332")
	public void Permission_18333(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//--------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Open the new object metadatacard
			//----------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Document.Value);//Clicks the new document object link from new item menu

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the popout metadatacard

			metadataCard.setTemplate(dataPool.get("Template"));//sets the template in the template selector

			Log.message("1. Template with automatic permission property is selected and new document object metadatacard is opened via menu bar.", driver);

			//Step-2: Create the template object with automatic permission class
			//------------------------------------------------------------------
			metadataCard = new MetadataCard(driver);//Instantiates the popout metadatacard

			String objName = Utility.getObjectName(methodName);//Gets the object name

			metadataCard.setPropertyValue(dataPool.get("Title"), objName);//Sets the object name in the metadatacard

			metadataCard.setCheckInImmediately(true);

			metadataCard.saveAndClose();//Creates the object

			Log.message("2. New Object("+ objName +") is created with the template which has automatic permission property in the view", driver);

			//Verification if object successfully created
			//-------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), objName);//Navigate to the specific view

			if (homePage.listView.isItemExists(objName+".doc"))
				Log.pass("Test case passed. New object is successfully created using Template with automatic permission property for multi class object.", driver);
			else
				Log.fail("Test case failed. New object is not created using Template with automatic permission property for multi class object.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_18333

	/**
	 * Permission_18334 : Create a template with automatic permission class for single class objects [eg: customer].
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Automatic Permission"},
			description = "Create a template with automatic permission class for single class objects [eg: customer].")
	public void Permission_18334(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//--------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Open the new object metadatacard
			//----------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Customer.Value);//Clicks the new customer object link from task pane

			Log.message("1. New customer object metadatacard is opened via menu bar.", driver);

			//Step-2: Select the automatic permission class and then verify the automatic permission behavior
			//----------------------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the popout metadatacard

			String bubbleMessage = metadataCard.setPropertyValue("Class", dataPool.get("ClassValue"));//Selects the automatic permission class in the metadatacard and Gets the bubble message from the metadatacard

			Log.message("2. Automatic permission bubble message ("+ bubbleMessage +") is displayed while selecting the automatic permission class("+ dataPool.get("ClassValue") +") in the metadatacard.", driver);

			//Step-3: Create the template object with automatic permission class
			//------------------------------------------------------------------
			String objName = Utility.getObjectName(methodName);//Gets the object name

			metadataCard.setPropertyValue(dataPool.get("Title"), objName);//Sets the object name in the metadatacard

			metadataCard.setPropertyValue(dataPool.get("Properties").split("::")[0], dataPool.get("Properties").split("::")[1]);

			metadataCard.saveAndClose();//Creates the object

			Log.message("3. Template Object("+ objName +") is created with the automatic permission class("+dataPool.get("ClassValue")+") in the view", driver);

			//Verification if object successfully created
			//-------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), "");//Navigate to the specific view

			if (homePage.listView.isItemExists(objName))
				Log.pass("Test case passed. Template object with automatic permission class for multi class object is created successfully [eg: Customer]", driver);
			else
				Log.fail("Test case failed. Template object with automatic permission class for multi class object is not created successfully [eg: Customer]", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_18334

	/**
	 * Permission_18360 : Verify whether the non-admin user can see the denied class when creating new object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Permissions", "NonAdminUser"},
			description = "Verify whether the non-admin user can see the denied class when creating new object")
	public void Permission_18360(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Step-1 : Login to MFiles web access
			//--------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("Username"), dataPool.get("Password"), testVault); //Launched driver and logged in

			Log.message("1. Logged into MFWA as non admin user : "+ dataPool.get("Username") +".", driver);

			//Step-2: Open the new object metadatacard
			//----------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Document.Value);//Clicks the new document object link from new item menu

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the popout metadatacard

			metadataCard.setTemplate(dataPool.get("Template"));//sets the template in the template selector

			Log.message("2. Opened the new document object metadatacard with Template("+ dataPool.get("Template") +")", driver);

			//Step-3: Gets the class property values
			//--------------------------------------
			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard in the rightpane

			ArrayList<String> availableClasses = metadataCard.getAvailablePropertyValues("Class");

			Log.message("3. Class property values is obtained from the metadata card.", driver);

			//Verification if Denied class is displayed or not in the class property
			//-----------------------------------------------------------------------
			Boolean result = true;

			for (int loop = 0; loop < availableClasses.size(); loop++)
				if (availableClasses.get(loop).equals(dataPool.get("DeniedClass")))
					result = false;

			if (result)
				Log.pass("Test case passed. Denied class ("+ dataPool.get("DeniedClass") +") is not displayed for the non-admin user("+ dataPool.get("Username") +") in the new object metadatacard.");
			else
				Log.fail("Test case failed. Denied class  ("+ dataPool.get("DeniedClass") +") is displayed for the non-admin user("+ dataPool.get("Username") +") in the new object metadatacard.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_18360

	/**
	 * Permission_18361 : Verify whether the non-admin user can see the denied class in exisiting object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Permissions", "NonAdminUser"},
			description = "Verify whether the non-admin user can see the denied class in exisiting object")
	public void Permission_18361(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Step-1: Login to MFiles web access as non admin user
			//----------------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("Username"), dataPool.get("Password"), testVault); //Launched driver and logged in

			Log.message("1. Logged into MFWA as non admin user : "+ dataPool.get("Username") +".", driver);

			//Step-2 : Navigate to template folder and select any template object in the view
			//--------------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), dataPool.get("SearchWord"));//Navigate to the specific view

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new SkipException(dataPool.get("ObjectName")+" does not exist in the view");

			Log.message("2. Navigated to '" + viewtonavigate + "' view and selected the object("+ dataPool.get("ObjectName") +") in the view.", driver);

			//Step-3: Gets the class property values
			//--------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard in the rightpane

			ArrayList<String> availableClasses = metadataCard.getAvailablePropertyValues("Class");

			Log.message("3. Class property values is obtained from the metadata card.", driver);

			//Verification if Denied class is displayed or not in the class property
			//-----------------------------------------------------------------------
			Boolean result = true;

			for (int loop = 0; loop < availableClasses.size(); loop++)
				if (availableClasses.get(loop).equals(dataPool.get("DeniedClass")))
					result = false;

			if (result)
				Log.pass("Test case passed. Denied class ("+ dataPool.get("DeniedClass") +") is not displayed for the non-admin user("+ dataPool.get("Username") +") in the existing object metadatacard.");
			else
				Log.fail("Test case failed. Denied class  ("+ dataPool.get("DeniedClass") +") is displayed for the non-admin user("+ dataPool.get("Username") +") in the existing object metadatacard.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_18361

	/**
	 * Permission_18362 : Verify whether a non-admin can add value to the denied value list in new object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Permissions", "NonAdminUser"},
			description = "Verify whether a non-admin can add value to the denied value list in new object")
	public void Permission_18362(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Step-1: Login to MFiles web access as non admin user
			//-----------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("Username"), dataPool.get("Password"), testVault); //Launched driver and logged in

			Log.message("1. Logged into MFWA as non admin user : "+ dataPool.get("Username") +".", driver);

			//Step-2: Open the new object metadatacard
			//----------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Customer.Value);//Clicks the new Customer object link from new item menu

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the popout metadatacard

			if(metadataCard.isTemplateDialogExists())
			{
				metadataCard.setTemplateUsingClass(Caption.Classes.Customer.Value);
				metadataCard = new MetadataCard(driver);//Instantiates the popout metadatacard
			}

			Log.message("2. Opened the new object metadatacard via Task pane", driver);

			//Verification: If new icon is displayed for the value list property which has the custom permission for the non-admin user in the metadatacard
			//---------------------------------------------------------------------------------------------------------------------------------------------
			if (!metadataCard.isPropAddValueDisplayed(dataPool.get("Property")))//Checks if Add value icon is displayed or not in the property
				Log.pass("Test case passed. Add value icon is not displayed in the denied value list property("+ dataPool.get("Property")+") for the non-admin user("+ dataPool.get("Username") +") in the new object metadatacard.", driver);
			else
				Log.fail("Test case failed. Add value icon is displayed in the denied value list property("+ dataPool.get("Property")+") for the non-admin user("+ dataPool.get("Username") +") in the new object metadatacard.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_18362

	/**
	 * Permission_18363 : Verify whether a non-admin can add value to the denied value list in existing object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Permissions", "NonAdminUser"},
			description = "Verify whether a non-admin can add value to the denied value list in existing object")
	public void Permission_18363(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Step-1: Login to MFiles web access as non admin user
			//----------------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("Username"), dataPool.get("Password"), testVault); //Launched driver and logged in

			Log.message("1. Logged into MFWA as non admin user : "+ dataPool.get("Username") +".", driver);

			//Step-2 : Navigate to template folder and select any template object in the view
			//--------------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), dataPool.get("SearchWord"));//Navigate to the specific view

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new SkipException(dataPool.get("ObjectName")+" does not exist in the view");

			Log.message("2. Navigated to '" + viewtonavigate + "' view and selected the object("+ dataPool.get("ObjectName") +") in the view.", driver);

			//Verification: If new icon is displayed for the value list property which has the custom permission for the non-admin user in the metadatacard
			//---------------------------------------------------------------------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadatacard
			if (!metadataCard.isPropAddValueDisplayed(dataPool.get("Property")))//Checks if Add value icon is displayed or not in the property
				Log.pass("Test case passed. Add value icon is not displayed in the denied value list property("+ dataPool.get("Property")+") for the non-admin user("+ dataPool.get("Username") +") in the existing object metadatacard.", driver);
			else
				Log.fail("Test case failed. Add value icon is displayed in the denied value list property("+ dataPool.get("Property")+") for the non-admin user("+ dataPool.get("Username") +") in the existing object metadatacard.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_18363

	/**
	 * Permission_24714 : Verify if the object is viewable when it has the permission to read only
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Permissions", "NonAdminUser"},
			description = "Verify if the object is viewable when it has the permission to read only")
	public void Permission_24714(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Step-1: Login to MFiles web access as non admin user
			//----------------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("Username"), dataPool.get("Password"), testVault); //Launched driver and logged in

			Log.message("1. Logged into MFWA as non admin user : "+ dataPool.get("Username") +".", driver);

			//Step-2 : Navigate to template folder and select any template object in the view
			//--------------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), dataPool.get("ObjectName"));//Navigate to the specific view

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new SkipException(dataPool.get("ObjectName")+" does not exist in the view");

			Log.message("2. Navigated to '" + viewtonavigate + "' view and selected the object("+ dataPool.get("ObjectName") +") in the view.", driver);

			//Verification: If new icon is displayed for the value list property which has the custom permission for the non-admin user in the metadatacard
			//---------------------------------------------------------------------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadatacard
			if (!metadataCard.isPropAddValueDisplayed(dataPool.get("Property")))//Checks if Add value icon is displayed or not in the property
				Log.pass("Test case passed. Object("+dataPool.get("ObjectName")+") is viewable when it has the permission to read only for the non-admin user("+ dataPool.get("Username") +").", driver);
			else
				Log.fail("Test case failed. Object("+dataPool.get("ObjectName")+") is viewable and editable when it has the permission to read only for the non-admin user("+ dataPool.get("Username") +").", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_24714

	/**
	 * Permission_24716 : Verify Selecting 'Checkout' option for the denied object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Permissions", "NonAdminUser"},
			description = "Verify Selecting 'Checkout' option for the denied object")
	public void Permission_24716(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Step-1: Login to MFiles web access as non admin user
			//-----------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("Username"), dataPool.get("Password"), testVault); //Launched driver and logged in

			Log.message("1. Logged into MFWA as non admin user : "+ dataPool.get("Username") +".", driver);

			//Step-2 : Navigate to template folder and select any template object in the view
			//--------------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), dataPool.get("ObjectName"));//Navigate to the specific view

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName")))
				throw new SkipException(dataPool.get("ObjectName")+" does not exist in the view");

			Log.message("2. Navigated to '" + viewtonavigate + "' view and Right clicked the read only object("+ dataPool.get("ObjectName") +") in the view.", driver);

			//Step-3: Click the menu item in the context menu
			//-----------------------------------------------
			homePage.listView.clickContextMenuItem(dataPool.get("Option"));//Clicks the option in the context menu

			Log.message("3. Clicked the option("+ dataPool.get("Option") +") in the conext menu of the read only object", driver);

			//Verification: If new icon is displayed for the value list property which has the custom permission for the non-admin user in the metadatacard
			//---------------------------------------------------------------------------------------------------------------------------------------------
			MFilesDialog mfDialog = new MFilesDialog(driver);//Instantiates the MFiles Dialog
			String actualDialogMsg = mfDialog.getMessage();

			if (actualDialogMsg.contains(dataPool.get("ExpectedDialogMsg")))
				Log.pass("Test case passed. Expected warning message("+ actualDialogMsg  +") is displayed while perform '"+dataPool.get("Option")+"' using the context menu in the object("+dataPool.get("ObjectName")+") is viewable which has the permission to read only for the non-admin user("+ dataPool.get("Username") +").", driver);
			else
				Log.fail("Test case failed. Expected warning message("+ dataPool.get("ExpectedDialogMsg")  +") is not displayed while perform '"+dataPool.get("Option")+"' using the context menu in the object("+dataPool.get("ObjectName")+") is viewable which has the permission to read only for the non-admin user("+ dataPool.get("Username") +").[Actual value: '"+actualDialogMsg+"']", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_24716

	/**
	 * Permission_26461 : Check if Object version changed while changing the permission after checking out the document from History
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"ObjectVersion"},
			description = "Check if Object version changed while changing the permission after checking out the document from History")
	public void Permission_26461(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), dataPool.get("SearchWord"));//Navigate to the specific view

			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))//Selects the object in the view
				throw new Exception("'" + dataPool.get("ObjectName") + "' is not selected in the navigated " + viewtonavigate + " view.");

			Log.message("1. Navigated to " + viewtonavigate + " and selected the object \"" + dataPool.get("ObjectName") +  "\" in the view.", driver);

			//Step-2: Navigate to the history view of the object
			//--------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.History.Value);//Clicks the history option in the operation menu

			Log.message("2. Navigated to the history view of the selected object", driver);

			//Step-3: Check out the latest version of object in the view
			//-----------------------------------------------------------
			if(!homePage.listView.rightClickItemByIndex(0))
				throw new Exception("Latest version of object is not right clicked in the list view.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.CheckOut.Value);

			Log.message("3. Checkout is clicked after selecting the latest version of the object in history view.", driver);

			//Step-4: Open the metadata card of the checked out latest version of object
			//--------------------------------------------------------------------------
			if(!homePage.listView.clickItemByIndex(0))
				throw new Exception("Latest version of object is not right clicked in the list view.");

			if(!homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value))
				throw new Exception("Properties is not clicked from the task pane for the selected latest version of object");

			Log.message("4. Pop-out metadata card opened for the latest version of the object in history view.", driver);

			//Step-5: Change the permission of the object in the metadata card and click save button
			//--------------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the popped out metadata card
			int initlaVersion = metadataCard.getVersion();
			metadataCard.setPermission(dataPool.get("Permission"));
			metadataCard.saveAndClose();

			Log.message("5. Permission '" + dataPool.get("Permission") + "' is set and saved in the checked out object metadata card.");

			//Step-6: Open the metadata card of the checked out latest version of object
			//--------------------------------------------------------------------------
			if(!homePage.listView.clickItemByIndex(0))
				throw new Exception("Latest version of object is not right clicked in the list view.");

			if(!homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value))
				throw new Exception("Properties is not clicked from the task pane for the selected latest version of object");

			Log.message("6. Pop-out metadata card opened for the latest version of the object after changing the permission in history view.", driver);

			//Verification: Check if object version is not changed
			//-----------------------------------------------------
			metadataCard = new MetadataCard(driver);//Instantiates the popped out metadata card
			if(initlaVersion == metadataCard.getVersion())
				Log.pass("Test Case Passed. Object version is not changed while changing the permission after checking out the document from History", driver);
			else
				Log.fail("Test Case Failed. Object version is changed while changing the permission after checking out the document from History", driver);

			metadataCard.clickCancelBtn();//Closes the metadatacard
			homePage.listView.clickItemByIndex(0);//Clicks the latest version of object in the view
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);//UndoCheckouts the object in the view
			MFilesDialog mfDialog = new MFilesDialog(driver);//Instantiates the MFilesDialog
			mfDialog.confirmUndoCheckOut(true);//Clicks Yes button

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_26461

	/**
	 * Permission_26474 : Check if Object version changed on adding comments and changing the permission
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"ObjectVersion"},
			description = "Check if Object version changed on adding comments and changing the permission")
	public void Permission_26474(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Step-1: Login to MFiles web access
			//----------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("Username"), dataPool.get("Password"), testVault); //Launched driver and logged in

			Log.message("1. Logged into MFWA as user : " + dataPool.get("Username") , driver);

			//Step-2 : Navigate to template folder and select any template object in the view
			//--------------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), "");//Navigate to the specific view

			homePage.listView.clickItem(dataPool.get("ObjectName"));//Selects the object in the view

			Log.message("2. Navigated to " + viewtonavigate + " and selected the object \"" + dataPool.get("ObjectName") +  "\" in the view.", driver);

			//Step-3: Adding comment and save the changes in the right pane metadatacard
			//--------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the right pane metadatacard

			int actualObjVersion = metadataCard.getVersion();//Gets the object version from the metadatacard

			String comments = Utility.getObjectName(methodName).toString();//Forms the comment string using object name

			metadataCard.setComments(comments);//Sets the comments value in the right pane metadatacard

			if (metadataCard.getComments().size() < 0 )
				throw new Exception("Expected comment(" + comments + ") is not set in the metadatacard.");

			metadataCard.setPermission(dataPool.get("Permission"));//Sets the permission in the metadatacard

			if (!metadataCard.getPermission().equalsIgnoreCase(dataPool.get("Permission")))
				throw new Exception("Expetced permission("+ dataPool.get("Permission") +") is not set in the metadatacard.[Actual value in metadatacard : " + metadataCard.getPermission() + "]");

			metadataCard.saveAndClose();//Saves the changes in the metadata card

			Log.message("3. Comment(" + comments + ") and permission(" + dataPool.get("Permission") + ") is set in the rightpane metadatacard.", driver);

			//Verification if object version is incremented after adding the comment in the metadatacard
			//------------------------------------------------------------------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard
			if (metadataCard.getVersion() == actualObjVersion+1)
				Log.pass("Test case passed. Object version is changed on adding comments and changing the permission");
			else
				Log.fail("Test case passed. Object version is not changed on adding comments and changing the permission. [Expected Version : " + metadataCard.getVersion() + " , Actual Version : " + actualObjVersion + "]", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_26474

	/**
	 * Permission_26475 : Check if Object version changed on adding comments
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"ObjectVersion"},
			description = "Check if Object version changed on adding comments")
	public void Permission_26475(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Step-1: Login to MFiles web access as user
			//--------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("Username"), dataPool.get("Password"), testVault); //Launched driver and logged in

			Log.message("1. Logged into MFWA as user : " + dataPool.get("Username") , driver);

			//Step-2 : Navigate to template folder and select any template object in the view
			//--------------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), "");//Navigate to the specific view

			homePage.listView.clickItem(dataPool.get("ObjectName"));//Selects the object in the view

			Log.message("2. Navigated to " + viewtonavigate + " and selected the object \"" + dataPool.get("ObjectName") +  "\" in the view.", driver);

			//Step-3: Adding comment and save the changes in the right pane metadatacard
			//--------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the right pane metadatacard

			int actualObjVersion = metadataCard.getVersion();//Gets the object version from the metadatacard

			String comments = Utility.getObjectName(methodName).toString();//Forms the comment string using object name

			metadataCard.setComments(comments);//Sets the comments value in the right pane metadatacard

			if (metadataCard.getComments().size() < 0 )
				throw new Exception("Expected comment(" + comments + ") is not set in the metadatacard.");

			metadataCard.saveAndClose();//Saves the changes in the metadata card

			Log.message("3. Comment (" + comments + ") is set in the rightpane metadatacard.", driver);

			//Verification if object version is incremented after adding the comment in the metadatacard
			//------------------------------------------------------------------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard
			if (metadataCard.getVersion() == actualObjVersion+1)
				Log.pass("Test case passed. Object version is changed on adding comments");
			else
				Log.fail("Test case failed. Object version is not changed on adding comments. [Expected Version : " + metadataCard.getVersion() + " , Actual Version : " + actualObjVersion + "]", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_26475

	/**
	 * Permission_26478 : Check if Object version changed while changing any property value [Eg :Description] other than permission
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"ObjectVersion"},
			description = "Check if Object version changed while changing any property value [Eg :Description] other than permission")
	public void Permission_26478(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Step-1: Login to MFiles web access
			//----------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			Log.message("1. Logged into MFWA as user : " + dataPool.get("Username") , driver);

			//Step-2 : Navigate to template folder and select any template object in the view
			//--------------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), "");//Navigate to the specific view

			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))//Selects the object in the view
				throw new Exception("Object '" + dataPool.get("ObjectName") + "' is not selected in the navigated view.");

			Log.message("2. Navigated to " + viewtonavigate + " and selected the object \"" + dataPool.get("ObjectName") +  "\" in the view.", driver);

			//Step-3: Adding comment and save the changes in the right pane metadatacard
			//--------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the right pane metadatacard

			int actualObjVersion = metadataCard.getVersion();//Gets the object version from the metadatacard

			metadataCard.setPropertyValue(dataPool.get("Property"), dataPool.get("PropertyValue"));

			if(!metadataCard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyValue")))
				throw new Exception("Property '" + dataPool.get("Property") + "' is not set with the value '" + dataPool.get("PropertyValue") + "' in the metadata card.");

			metadataCard.saveAndClose();

			Log.message("3. Property '" + dataPool.get("Property") + "' is  set with the value '" + dataPool.get("PropertyValue") + "' in the metadata card.", driver);

			//Verification if object version is incremented after adding the comment in the metadatacard
			//------------------------------------------------------------------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard
			if (metadataCard.getVersion() == actualObjVersion+1)
				Log.pass("Test case passed. Object version is changed while changing any property value other than permission");
			else
				Log.fail("Test case passed. Object version is not changed while changing any property value other than permission. [Expected Version : " + metadataCard.getVersion() + " , Actual Version : " + actualObjVersion + "]", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_26478

	/**
	 * Permission_26451 : Check if Object version changed while changing the permission after checking out the document
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Permissions"},	description = "Check if Object version changed while changing the permission after checking out the document")
	public void Permission_26451(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite: Login to MFiles web access as user
			//--------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to template folder and select any template object in the view
			//--------------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), "");//Navigate to the specific view

			homePage.listView.clickItem(dataPool.get("ObjectName"));//Selects the object in the view

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))//Checks if object is checked out in the  view
				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);//Clicks the checkout option from the task pane

			Log.message("1. Navigated to " + viewtonavigate + " and Checked out object \"" + dataPool.get("ObjectName") +  "\" is selected in the view.", driver);

			//Step-2: Changing the permission and save the changes in the right pane metadatacard
			//-----------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the right pane metadatacard

			int actualObjVersion = metadataCard.getVersion();//Gets the object version from the metadatacard

			metadataCard.setPermission(dataPool.get("Permission"));//Sets the permission in the right pane metadatacard

			if (!metadataCard.getPermission().equalsIgnoreCase(dataPool.get("Permission")) )
				throw new Exception("Expected permission(" + dataPool.get("Permission") + ") is not set in the metadatacard.");

			metadataCard.saveAndClose();//Saves the changes in the metadata card

			Log.message("2. Permission (" + dataPool.get("Permission") + ") is set in the rightpane metadatacard for the checked out object and saved the changes.", driver);

			//Verification if object version is incremented after adding the comment in the metadatacard
			//------------------------------------------------------------------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (metadataCard.getVersion() == actualObjVersion)
				Log.pass("Test case passed. Object version is not changed while changing the permission of checked out object.");
			else
				Log.fail("Test case failed. Object version is changed while changing the permission of checked out object. [Additional info. : After checkout object version : "+ actualObjVersion +" & After changing permission object version : "+metadataCard.getVersion()+"]", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_26451

	/**
	 * Permission_26452 : Check if Object version changed while checking out and checking in an object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Permissions"},	description = "Check if Object version changed while checking out and checking in an object")
	public void Permission_26452(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite: Login to MFiles web access as user
			//--------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to template folder and select any template object in the view
			//--------------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), "");//Navigate to the specific view

			homePage.listView.clickItem(dataPool.get("ObjectName"));//Selects the object in the view

			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the right pane metadatacard

			int actualObjVersion = metadataCard.getVersion();//Gets the object version from the metadatacard

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))//Checks if object is checked out in the  view
				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);//Clicks the checkout option from the task pane

			Log.message("1. Navigated to " + viewtonavigate + " and Object \"" + dataPool.get("ObjectName") +  "\" is checked out in the view.", driver);

			//Step-2: Changing the permission and save the changes in the right pane metadatacard
			//-----------------------------------------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.CheckIn.Value);//Clicks the check-in option from operation menu

			Log.message("2. Object \"" + dataPool.get("ObjectName") +  "\" is checked IN in the view.", driver);

			//Verification if object version is not incremented while check out and check in the object
			//------------------------------------------------------------------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (metadataCard.getVersion() == actualObjVersion)
				Log.pass("Test case passed. Object version is not changed while checking out and checking in the object.");
			else
				Log.fail("Test case failed. Object version is changed while checking out and checking in the object.[Additional info. : Before checkout object version : "+ actualObjVersion +" & After Checkin object version : "+metadataCard.getVersion()+"]", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_26452

	/**
	 * Permission_26660 : Check the Accessed by me column value get changes while changing the permission
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Permissions"},
			description = "Check the Accessed by me column value get changes while changing the permission")
	public void Permission_26660(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite: Login to MFiles web access as user
			//--------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to template folder and select any template object in the view
			//--------------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), "");//Navigate to the specific view

			homePage.listView.clickItem(dataPool.get("ObjectName"));//Selects the object in the view

			String accessedByMe = homePage.listView.getColumnValueByItemName(dataPool.get("ObjectName"), dataPool.get("Column"));

			Log.message("1. Navigated to " + viewtonavigate + " and selected the object \"" + dataPool.get("ObjectName") +  "\" in the view.", driver);

			//Step-2: Changing the permission and save the changes in the right pane metadatacard
			//-----------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the right pane metadatacard

			metadataCard.setPermission(dataPool.get("Permission"));//Sets the permission in the right pane metadatacard

			if (!metadataCard.getPermission().equalsIgnoreCase(dataPool.get("Permission")) )
				throw new Exception("Expected permission(" + dataPool.get("Permission") + ") is not set in the metadatacard.");

			metadataCard.saveAndClose();//Saves the changes in the metadata card

			Log.message("2. Permission (" + dataPool.get("Permission") + ") is set in the rightpane metadatacard and saved the changes.", driver);

			//Verification if object version is incremented after adding the comment in the metadatacard
			//------------------------------------------------------------------------------------------
			String result = "";

			if (homePage.listView.getColumnValueByItemName(dataPool.get("ObjectName"), dataPool.get("Column")).equalsIgnoreCase(accessedByMe))
				result += "Accessed by Me column value is not updated after modifing the object permission.[Before modification : "+accessedByMe+" & After modification : "+homePage.listView.getColumnValueByItemName(dataPool.get("ObjectName"), dataPool.get("Column"))+"]";

			if (result.equals(""))
				Log.pass("Test case passed. Accessed by me column value get changed while changing the permission.");
			else
				Log.fail("Test case failed. Accessed by me column value not get changed while changing the permission. [Additional info. : "+ result +"]", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_26660

	/**
	 * Permission_38037 : Check Edit buttons is disabled for users with read-only licenses
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Permissions", "ReadOnlyUser"},
			description = "Check Edit buttons is disabled for users with read-only licenses")
	public void Permission_38037(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Step-1: Login to MFiles web access as read only user
			//----------------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("Username"), dataPool.get("Password"), testVault); //Launched driver and logged in

			Log.message("1. Logged into MFWA as read only user : "+dataPool.get("Username")+".", driver);

			//Step-2 : Navigate to template folder and select any template object in the view
			//--------------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), "");//Navigate to the specific view

			homePage.listView.clickItem(dataPool.get("ObjectName"));//Selects the object in the view

			Log.message("2. Navigated to " + viewtonavigate + " and selected the object \"" + dataPool.get("ObjectName") +  "\" in the view.", driver);

			//Step-3: Verifies the expected behaviors in the metadatacard
			//------------------------------------------------------------
			String Expected = "";
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard

			metadataCard.clickProperty(dataPool.get("Property"));//Clicks the property in the metadatacard

			if (metadataCard.isPropertyInFocus(dataPool.get("Property")))
				Expected = "Property(" + dataPool.get("Property") + ") is editable for the read only user in the metadatacard.";

			if (metadataCard.isAddPropertyLinkDisplayed())
				Expected += " Add property link is displayed in the metadata card for the read only user.";

			Log.message("3. Checked the expected behavior for the read only user.", driver);

			//Verification: Check Accesss denied error is shown when read only double click on template object
			//------------------------------------------------------------------------------------------------
			if (Expected.equalsIgnoreCase(""))
				Log.pass("Test case passed. Edit buttons is disabled for users with read-only licenses");
			else
				Log.fail("Test case failed. Edit buttons is disabled for users with read-only licenses. Additional Info: [ " + Expected + " ]", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_38037

	/**
	 * Permission_38046 : Check if read only user able to add comments for workflow state change
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"ObjectVersion"},
			description = "Check if read only user able to add comments for workflow state change")
	public void Permission_38046(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//--------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Create new Assignment object
			//------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Assignment.Value);//Clicks the assignment link from the taskpane

			String objName = Utility.getObjectName(methodName);//Gets the object name

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.setPropertyValue(dataPool.get("Title"), objName);//Sets the object title

			metadataCard.setInfo(dataPool.get("Properties"));//Sets the property values in the metadatacard

			metadataCard.saveAndClose();//Creates the new object

			Log.message("1. New assignment object(" + objName + ") is created in the view", driver);

			//Step-2: Sets the workflow for the object
			//----------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);//Opens the workflow dialog of the object

			if(!MFilesDialog.exists(driver))
				throw new Exception("The Workflow dialog did not appear.");

			MFilesDialog mFilesDialog = new MFilesDialog(driver, "Workflow");
			mFilesDialog.setWorkflow(dataPool.get("Workflow"));
			mFilesDialog.clickOkButton();

			Log.message("2. Workflow(" + dataPool.get("Workflow")  + ") is set for the object(" + objName + ").");

			//Step-3: Logout and Login as Read only user
			//------------------------------------------
			Utility.logoutFromWebAccess(driver);//Logout from webaccess

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("Username"), dataPool.get("Password"), testVault); //Launched driver and logged in

			Log.message("3. Logged in as the read only user("+ dataPool.get("Username") +")", driver);

			//Step-4: Navigate to the view and then select the object
			//--------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.Taskpanel.AssignedToMe.Value);

			homePage.listView.clickItem(objName);//Selects the object in the view

			Log.message("4. Navigated to Assigned to me and selected the object \"" + objName +  "\" in the view.", driver);

			//Step-5: Perform state transition via task panel
			//------------------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("WorkflowState"));//Clicks the workflow state in the task pane

			if(!MFilesDialog.exists(driver))
				throw new Exception("The Workflow dialog did not appear.");

			mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.setWorkflowComments(objName+"_Comment");//Sets the comment in workflow dialog
			mFilesDialog.clickOkButton();

			Log.message("5. Workflow state("+ dataPool.get("WorkflowState") +") is set with comment("+ objName+"_Comment"  +")");

			//Verification: If object is exist or not
			//---------------------------------------
			if(!homePage.listView.isItemExists(objName))
				Log.pass("Test case passed. Read only user able to perform workflow state change with comments.");
			else
				Log.fail("Test case failed. Read only user not able to perform workflow state change with comments.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_38046

	/**
	 * Permission_38047 : Check if read only user able to without any comments for workflow state change
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"ObjectVersion"},
			description = "Check if read only user able to without any comments for workflow state change")
	public void Permission_38047(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//--------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Create new Assignment object
			//------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Assignment.Value);//Clicks the assignment link from the taskpane

			String objName = Utility.getObjectName(methodName);//Gets the object name

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.setPropertyValue(dataPool.get("Title"), objName);//Sets the object title

			metadataCard.setInfo(dataPool.get("Properties"));//Sets the property values in the metadatacard

			metadataCard.saveAndClose();//Creates the new object

			Log.message("1. New assignment object(" + objName + ") is created in the view", driver);

			//Step-2: Sets the workflow for the object
			//----------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);//Opens the workflow dialog of the object

			if(!MFilesDialog.exists(driver))
				throw new Exception("The Workflow dialog did not appear.");

			MFilesDialog mFilesDialog = new MFilesDialog(driver, "Workflow");
			mFilesDialog.setWorkflow(dataPool.get("Workflow"));
			mFilesDialog.clickOkButton();

			Log.message("2. Workflow(" + dataPool.get("Workflow")  + ") is set for the object(" + objName + ").");

			//Step-3: Logout and Login as Read only user
			//------------------------------------------
			Utility.logoutFromWebAccess(driver);//Logout from webaccess

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("Username"), dataPool.get("Password"), testVault); //Launched driver and logged in

			Log.message("3. Logged in as the read only user("+ dataPool.get("Username") +")", driver);

			//Step-4: Navigate to the view and then select the object
			//--------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.Taskpanel.AssignedToMe.Value);

			homePage.listView.clickItem(objName);//Selects the object in the view

			Log.message("4. Navigated to Assigned to me and selected the object \"" + objName +  "\" in the view.", driver);

			//Step-5: Perform state transition via task panel
			//------------------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("WorkflowState"));//Clicks the workflow state in the task pane

			if(!MFilesDialog.exists(driver))
				throw new Exception("The Workflow dialog did not appear.");

			mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.clickOkButton();

			Log.message("5. Workflow state("+ dataPool.get("WorkflowState") +") is set for the object(" + objName + ")");

			//Verification: If object is exist or not
			//---------------------------------------
			if(!homePage.listView.isItemExists(objName))
				Log.pass("Test case passed. Read only user able to perform workflow state change without any comments.");
			else
				Log.fail("Test case failed. Read only user not able to perform workflow state change without any comments.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_38047

	/**
	 * Permission_38048 : Check Access denied error is shown when read only double click on template object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Permissions", "ReadOnlyUser"},
			description = "Check Access denied error is shown when read only double click on template object")
	public void Permission_38048(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Step-1 : Login to MFiles web access as read only user
			//-------------------------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("Username"), dataPool.get("Password"), testVault); //Launched driver and logged in

			Log.message("1. Logged into MFWA as read only user : "+dataPool.get("Username")+".", driver);

			//Step-2 : Navigate to template folder and select any template object in the view
			//--------------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), "");//Navigate to the specific view

			Log.message("2. Navigated to " + viewtonavigate + " view.", driver);

			//Step-3 : Double click the object in the view
			//--------------------------------------------
			homePage.listView.doubleClickItem(dataPool.get("ObjectName"));//Double clicks the object in the view

			Log.message("3. Double clicked the object \"" + dataPool.get("ObjectName") +  "\" in the view", driver);

			//Step-4: Verifies the expected behaviors in the view
			//---------------------------------------------------
			String Expected = "";
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.clickProperty(dataPool.get("Property"));//Clicks the property in the metadatacard

			if (metadataCard.isPropertyInFocus(dataPool.get("Property")))
				Expected = "Property(" + dataPool.get("Property") + ") is editable for the read only user in the metadatacard";

			metadataCard.clickOKBtn(driver);//Clicks the create button in the metadatacard

			if (!MFilesDialog.exists(driver))
				throw new Exception("Test case failed. Access denied error dialog is not displayed while clicking create button in the metadatacard");

			MFilesDialog mfDialog = new MFilesDialog(driver);

			if (!mfDialog.getMessage().equalsIgnoreCase(dataPool.get("WarningMessage")))
				Expected = " Expected warning message(" + dataPool.get("WarningMessage") + ") is not displayed.[Actual value : "+ mfDialog.getMessage() +"]";

			Log.message("4. Checked the expected behavior for the read only user.", driver);

			//Verification: Check Accesss denied error is shown when read only double click on template object
			//------------------------------------------------------------------------------------------------
			if (Expected.equalsIgnoreCase(""))
				Log.pass("Test case passed. Access denied error is shown when read only double click on template object");
			else
				Log.fail("Test case failed. Access denied error is not shown when read only double click on template object. Additional Info: [ " + Expected + " ]", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_38048

	/**
	 * Permission_26470 : Check if Object version changed while changing the permission from metadata pane for a newly created object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Object Version - Permission"},
			description = "Check if Object version changed while changing the permission from metadata pane for a newly created object")
	public void Permission_26470(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//--------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Create new Assignment object
			//------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//Clicks the new object type link from the menuoption

			String objName = Utility.getObjectName(methodName);//Gets the object name

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			if(dataPool.get("ObjectType").contains("Document") || dataPool.get("ObjectType").contains("Document"))
				if(Utility.isSelectTemplateDisplayed(driver))
				{
					metadataCard.setTemplate(dataPool.get("Template"));
					metadataCard = new MetadataCard(driver);//Instantiates the metadatacard
				}

			metadataCard.setPropertyValue(dataPool.get("Title"), objName);//Sets the object title

			if(dataPool.get("ObjectType").contains("Document"))
				objName += dataPool.get("Template").split("\\.")[1];

			if(dataPool.get("ObjectType").contains("Contact person"))
			{
				metadataCard.setPropertyValue(dataPool.get("Properties").split("::")[0], dataPool.get("Properties").split("::")[1]);
				if(MFilesDialog.exists(driver, "Confirm Autofill"))
				{
					MFilesDialog dialog = new MFilesDialog(driver, "Confirm Autofill");//Instantiates the M-Files Dialog
					dialog.clickButton("no");
				}
			}
			else
			{
				metadataCard.setInfo(dataPool.get("Properties"));//Sets the property values in the metadatacard
				metadataCard.setCheckInImmediately(true);//Checking the 'Check In Immmediately' check box 
			}
			metadataCard.saveAndClose();//Creates the new object

			Log.message("1. Created new " + dataPool.get("ObjectType") + "object(" + objName + ") from menu option and entered the required property values.", driver);

			//Step-2 : Select the created project metadatacard 
			//------------------------------------------------
			if(!homePage.listView.isItemSelected(objName))//verify if created object is selected or not
				homePage.listView.clickItem(objName);//		

			if(dataPool.get("ObjectType").contains("Report"))
				homePage.previewPane.clickMetadataTab();

			metadataCard = new MetadataCard(driver, true);//Instantiates the right pane metadatacard
			int expectedObjectVersion = metadataCard.getVersion();//get the expected object version before changing permission 	

			Log.message("2. Fetched the object version " + expectedObjectVersion + " for newly created : " + dataPool.get("ObjectType")  + " object : " + objName + " before changing the permission from right pane metadatacard.", driver);

			//Step-3 : Change the permission for created object and discard the changes in right pane metadatacard
			//----------------------------------------------------------------------------------------------------
			String defaultPermission = metadataCard.getPermission();

			metadataCard.setPermission(dataPool.get("ChangePermission"));//Change the permission for created object
			metadataCard.clickDiscardButton();//Click the discard button

			Log.message("3. Changed permission '" + defaultPermission + "' from to new permission " +  dataPool.get("ChangePermission") + " and discard the changes using Metadata discard button." , driver);

			//Step-4 : get the version after discarding the permission changes
			//----------------------------------------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiates the right pane metadatacard
			int actualObjectVersion = metadataCard.getVersion();//get the object version

			Log.message("4. Fetched object version " + actualObjectVersion + " from right pane metadatacard after changing the permission value.", driver);

			//Verification : Verify if Object version not changed while changing the permission from metadata pane
			//----------------------------------------------------------------------------------------------------
			if(expectedObjectVersion==actualObjectVersion)	
				Log.pass("Test Case Passed. Object version is not modified while discarding the permission changing for created new objects : " + dataPool.get("ObjectType"), driver);
			else
				Log.fail("Test Case Failed. Object version is modified while discarding the permission changing for created new objects : " + dataPool.get("ObjectType"), driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_26470

	/**
	 * Permission_26472 : Check if Object version changed while changing the workflow state
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Object Version - Permission"},
			description = "Check if Object version changed while changing the workflow state")
	public void Permission_26472(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//--------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Create new Assignment object
			//------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//Clicks the new object type link from the menuoption

			String objName = Utility.getObjectName(methodName);//Gets the object name

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.setPropertyValue(dataPool.get("Title"), objName);//Sets the object title

			metadataCard.setInfo(dataPool.get("Properties"));//Sets the property values in the metadatacard
			metadataCard.setCheckInImmediately(true);//Checking the 'Check In Immmediately' check box 

			metadataCard.saveAndClose();//Creates the new object

			Log.message("1. Created new " + dataPool.get("ObjectType") + "object(" + objName + ") from menu option and entered the required property values.", driver);

			//Step-2 : Select the created project metadatacard 
			//------------------------------------------------
			if(!homePage.listView.isItemSelected(objName))//verify if created object is selected or not
				homePage.listView.clickItem(objName);//		

			metadataCard = new MetadataCard(driver, true);//Instantiates the right pane metadatacard
			int expectedObjectVersion = metadataCard.getVersion();//get the expected object version before changing permission 	

			Log.message("2. Fetched the object version " + expectedObjectVersion + " for newly created : " + dataPool.get("ObjectType")  + " object : '" + objName + "' before changing the workflow.", driver);

			//Step-3 : Change the Workflow & State for created object 
			//-------------------------------------------------------
			metadataCard.setWorkflow(dataPool.get("Workflow"));

			metadataCard.saveAndClose();

			Log.message("3. Workflow (" + dataPool.get("Workflow") + ") & State (" + dataPool.get("WorkflowState") + ") is set for newly created object.", driver);

			//Step-4 : get the version after discarding the permission changes
			//----------------------------------------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiates the right pane metadatacard
			int actualObjectVersion = metadataCard.getVersion();//get the object version

			Log.message("4. Fetched object version " + actualObjectVersion + " from right pane metadatacard after set the workflow & state.");

			//Verification : Verify if Object version not changed while changing the permission from metadata pane
			//----------------------------------------------------------------------------------------------------
			if((expectedObjectVersion+1)==actualObjectVersion)	
				Log.pass("Test Case Passed. Object version is not modified while discarding the permission changing for created new objects : " + dataPool.get("ObjectType"), driver);
			else
				Log.fail("Test Case Failed. Object version is modified while discarding the permission changing for created new objects : " + dataPool.get("ObjectType"), driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_26470

	/**
	 * Permission_26144 : Verify user able to create a new document in webaccess
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Permissions"},
			description = "Verify user able to create a new document in webaccess")
	public void Permission_26144(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//--------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Create new Assignment object
			//------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Document.Value);//Clicks the new object type link from the menuoption

			String objName = Utility.getObjectName(methodName);//Gets the object name from test name

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard			
			metadataCard.setTemplate(dataPool.get("Template"));//Select the specified template

			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard
			metadataCard.setPropertyValue(dataPool.get("Title"), objName);//Sets the object title

			metadataCard.setInfo(dataPool.get("Properties"));//Sets the property values in the metadatacard
			metadataCard.setCheckInImmediately(true);//Checking the 'Check In Immmediately' check box 			

			metadataCard.saveAndClose();//Save metadatacard

			Log.message("1. Created new " + Caption.ObjecTypes.Document.Value + "object(" + objName + ") from task pane and entered the required property values.", driver);

			//Step-2 : Select the created document metadatacard 
			//------------------------------------------------
			objName = objName+".doc" ;
			if(!homePage.listView.isItemSelected(objName))//verify if created object is selected or not
				homePage.listView.clickItem(objName);//	if item is not selected select the item

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value);//Click the operation menu for newly created object

			Log.message("2. Opened the metadatacard for newly created object : " + objName + " using operation menu.");

			//Step-3 : Fetched the permission value from right pane metadatacard
			//------------------------------------------------------------------
			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard
			String actualPermission = metadataCard.getPermission();//get the permission value for newly created document metadatacard

			Log.message("3. Fetched the permission '" + actualPermission + "' value set in right pane metadatacard for created document object : " + objName, driver);

			//Verification : Verify if Object version not changed while changing the permission from metadata pane
			//----------------------------------------------------------------------------------------------------
			if(actualPermission.equalsIgnoreCase(dataPool.get("ExpectedPermission")))	
				Log.pass("Test Case Passed. Object version is not modified while discarding the permission changing for created new objects : " + dataPool.get("ObjectType"), driver);
			else
				Log.fail("Test Case Failed. Object version is modified while discarding the permission changing for created new objects : " + dataPool.get("ObjectType"), driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_26144

	/**
	 * Permission_26150 : Verify permission for history version of the permission assigned document
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"ObjectVersion"},
			description = "Verify permission for history version of the permission assigned document")
	public void Permission_26150(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), dataPool.get("SearchWord"));//Navigate to the specific view

			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))//Selects the object in the view
				throw new Exception("'" + dataPool.get("ObjectName") + "' is not selected in the navigated " + viewtonavigate + " view.");

			Log.message("1. Navigated to " + viewtonavigate + " and selected the object \"" + dataPool.get("ObjectName") +  "\" in the view.", driver);

			//Step-2: Update the metadatacard for creating history for object
			//---------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard

			metadataCard.setPropertyValue(dataPool.get("Property"), dataPool.get("PropertyValue"));

			if(!metadataCard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyValue")))
				throw new Exception("Property '" + dataPool.get("Property") + "' is not set with the value '" + dataPool.get("PropertyValue") + "' in the metadatacard.");

			metadataCard.saveAndClose();

			metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard
			metadataCard.setPropertyValue(dataPool.get("Property"), "");

			if(!metadataCard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(""))
				throw new Exception("Property '" + dataPool.get("Property") + "' is not emptied in the metadatacard.");

			metadataCard.saveAndClose();

			Log.message("2. Older version of object is created with Automatic permission.");

			//Step-3: Navigate to the history view of the object
			//--------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.History.Value);//Clicks the history option in the operation menu

			int itemCount = 0;
			itemCount = homePage.listView.getAllItemNames().length;

			if(itemCount <= 1)
				throw new Exception("Older version objects not available to select in the history view");

			Log.message("3. Navigated to the history view of the selected object", driver);

			//Step-4: Open the metadatacard of the older version object
			//---------------------------------------------------------
			if(!homePage.listView.clickItemByIndex(1))
				throw new Exception("Older version of object which has automatic permission is not selected in the list view.");

			if(!homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value))
				throw new Exception("Properties is not clicked from the task pane for the selected older version of object");

			Log.message("4. Pop-out metadata card opened for the older version of the object which has automatic permission in history view.", driver);

			//Verification: Check permission displayed in the metadatacard
			//-------------------------------------------------------------
			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard
			if(metadataCard.getPermission().equalsIgnoreCase(dataPool.get("ExpectedPermission")))
				Log.pass("Test case passed. Expected permission '" + dataPool.get("ExpectedPermission") + "' is displayed in the metadata card for the older version of object which has automatic permission in history view.");
			else
				Log.fail("Test case failed. Expected permission '" + dataPool.get("ExpectedPermission") + "' is not displayed in the metadata card for the older version of object which has automatic permission in history view.[Actual permission displayed: '" + metadataCard.getPermission() + "']", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_26150

	/**
	 * Permission_26454 : Check if Object version changed while changing the permission from properties displayed under View and Modify
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Object Version - Permission"},
			description = "Check if Object version changed while  changing the permission from properties displayed under View and Modify")
	public void Permission_26454(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//--------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Create new Assignment object
			//------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//Clicks the new object type link from the menuoption

			String objName = Utility.getObjectName(methodName);//Gets the object name

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			if(dataPool.get("ObjectType").contains("Document")) {//Verify if object type is document or not
				metadataCard.setTemplate(dataPool.get("Template"));//Select the specifed template
				metadataCard = new MetadataCard(driver);//Instantiates the metadatacard
			}					

			metadataCard.setPropertyValue(dataPool.get("Title"), objName);//Sets the name or title for created object

			metadataCard.setInfo(dataPool.get("Properties"));//Sets the property values in the metadatacard
			metadataCard.setCheckInImmediately(true);//Checking the 'Check In Immmediately' check box 

			metadataCard.saveAndClose();//Creates the new object

			Log.message("1. Created new " + dataPool.get("ObjectType") + "object(" + objName + ") from menu option and entered the required property values.", driver);

			//Step-2 : Select the created project metadatacard 
			//------------------------------------------------
			if(dataPool.get("ObjectType").contains("Document"))//Verify if object type is document or not
				objName = objName+".doc" ;//Fetch the object name with Extension if created object is document 

			if(!homePage.listView.isItemSelected(objName))//verify if created object is selected or not
				homePage.listView.clickItem(objName);//select the object name if its not selected

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);//Select the 'Properties' option from task pane 

			metadataCard = new MetadataCard(driver);//Instantiates the right pane metadatacard
			int expectedObjectVersion = metadataCard.getVersion();//get the expected object version before changing permission 	

			Log.message("2. Fetched the object version " + expectedObjectVersion + " for newly created : " + dataPool.get("ObjectType")  + " object : " + objName + " before changing the permission from right pane metadatacard.", driver);

			//Step-3 : Change the permission for created object and discard the changes in right pane metadatacard
			//----------------------------------------------------------------------------------------------------
			String defaultPermission = metadataCard.getPermission();

			metadataCard.setPermission(dataPool.get("ChangePermission"));//Change the permission for created object
			metadataCard.saveAndClose();

			Log.message("3. Changed permission '" + defaultPermission + "' to new permission " +  dataPool.get("ChangePermission"), driver);

			//Step-4 : get the version after discarding the permission changes
			//----------------------------------------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiates the right pane metadatacard
			int actualObjectVersion = metadataCard.getVersion();//get the object version

			Log.message("4. Fetched object version " + actualObjectVersion + " from right pane metadatacard after changing the permission value.", driver);

			//Verification : Verify if Object version not changed while changing the permission from metadata pane
			//----------------------------------------------------------------------------------------------------
			if(expectedObjectVersion==actualObjectVersion)	
				Log.pass("Test Case Passed. Object version is not modified while changing the permission in newly created : " +  dataPool.get("ObjectType") + " object." , driver);
			else
				Log.fail("Test Case Failed. Object version is modified while changing the permission in newly created : " +  dataPool.get("ObjectType") + " object.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_26454

	/**
	 * Permission_26284 : Verify whether the non-admin user can see the denied workflow when creating new object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"ObjectVersion"},
			description = "Verify whether the non-admin user can see the denied workflow when creating new object.")
	public void Permission_26284(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//--------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("User"), dataPool.get("Password"), testVault); //Launched driver and logged in

			//Step-1 : Create new Assignment object
			//------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));//Clicks the assignment link from the taskpane

			String objName = Utility.getObjectName(methodName);//Gets the object name

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.setPropertyValue(dataPool.get("Title"), objName);//Sets the object title

			metadataCard.setInfo(dataPool.get("Properties"));//Sets the property values in the metadatacard

			metadataCard.saveAndClose();//Creates the new object

			Log.message("1. New " + dataPool.get("ObjectType") + " object(" + objName + ") is created in list view", driver);

			//Step-2 : Set the denied workflow with non-admin user
			//----------------------------------------------------
			metadataCard = new MetadataCard(driver, true); //Instantiate the right pane metadatacard 
			metadataCard.setWorkflow(dataPool.get("Workflow"));//Set the denied workflow in newly cerated object
			metadataCard.saveAndClose();//Save the workflow changes

			Log.message("2. Denied workflow is set in newly created : " + objName + " right pane metadatacard.", driver);

			//Verification : Verify if workflow value is set as expected
			//----------------------------------------------------------
			metadataCard = new MetadataCard(driver, true); //Instantiate the right pane metadatacard 
			if(metadataCard.getWorkflow().equalsIgnoreCase(dataPool.get("Workflow")))
				Log.pass("Test Case Passed. Denied workflow : " + dataPool.get("Workflow") + " is visible to the non-admin user : " + dataPool.get("User"), driver);
			else
				Log.fail("Test Case Failed. Denied workflow : " + dataPool.get("Workflow") + " is not visible to the non-admin user : " + dataPool.get("User"), driver);


		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_26284

	/**
	 * Permission_26285 : Verify whether the non-admin user can see the denied workflow in existing object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"ObjectVersion"},
			description = "Verify whether the non-admin user can see the denied workflow in existing object.")
	public void Permission_26285(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//--------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("User"), dataPool.get("Password"), testVault); //Launched driver and logged in

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), dataPool.get("ObjectName"));//Navigate to the specific view

			homePage.listView.clickItem(dataPool.get("ObjectName"));//Selects the object in the view

			Log.message("1. Navigated to " + viewtonavigate + " and selected the object \"" + dataPool.get("ObjectName") +  "\" in search view.", driver);

			//Step-2 : Set the denied workflow with non-admin user
			//----------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true); //Instantiate the right pane metadatacard 
			metadataCard.setWorkflow(dataPool.get("Workflow"));//Set the denied workflow in newly cerated object
			metadataCard.saveAndClose();//Save the workflow changes

			Log.message("2. Denied workflow is set in selected : " + dataPool.get("ObjectName") + " for right pane metadatacard.", driver);

			//Verification : Verify if workflow value is set as expected
			//----------------------------------------------------------
			metadataCard = new MetadataCard(driver, true); //Instantiate the right pane metadatacard 
			if(metadataCard.getWorkflow().equalsIgnoreCase(dataPool.get("Workflow")))
				Log.pass("Test Case Passed. Denied workflow : " + dataPool.get("Workflow") + " is visible to the non-admin user : " + dataPool.get("User"), driver);
			else
				Log.fail("Test Case Failed. Denied workflow : " + dataPool.get("Workflow") + " is not visible to the non-admin user : " + dataPool.get("User"), driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_26285

	/**
	 * Permission_26453 : Check if Object version changed on checking out, adding comments and checking in
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"ObjectVersion"},
			description = "Check if Object version changed on checking out, adding comments and checking in.")
	public void Permission_26453(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//--------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), dataPool.get("ObjectName"));//Navigate to the specific view

			homePage.listView.rightClickItem(dataPool.get("ObjectName"));//Selects the object in the view

			Log.message("1. Navigated to " + viewtonavigate + " and selected the object \"" + dataPool.get("ObjectName") +  "\" in search view.", driver);

			//Step-2 : Instantiate right pane metadatacard & fetched version of selected object
			//---------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiate the right pane metadatacard
			int versionBeforeCheckin = metadataCard.getVersion();//Fetch the version of the selected object from right pane metadatacard

			Log.message("2. Fetched object version for selected object : " + dataPool.get("ObjectName") + " from right pane metadatacard.", driver);

			//Step-3 : Check out the selected object and set the comments 
			//-----------------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.CheckOut.Value);

			Log.message("3. Checked out selected object : " + dataPool.get("ObjectName") + " using Context menu.", driver);

			//Step-4 : set the comments in right pane metadatacard
			//----------------------------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiate the right pane metadatacard
			metadataCard.setComments(Utility.getObjectName(methodName));//Gets the object name);//set the comments in metadatacard
			metadataCard.saveAndClose();//Save the metadatacard

			Log.message("4. Comment is set in right pane metadatacard & fetched the metadatacard version after saving the comments.", driver);

			//Step-4 : Checkin the object which is in checked out mode 
			//--------------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.CheckIn.Value);//check in the checked out object using operation menu
			metadataCard = new MetadataCard(driver, true);
			int versionAfterCheckIn = metadataCard.getVersion();

			Log.message("4. Fetched the version from right pane metadatacard after check in the selected object : " + dataPool.get("ObjectName"), driver);

			//Verification : Verify if object version is increased after checkin the object
			//-----------------------------------------------------------------------------
			if((versionBeforeCheckin+1)==versionAfterCheckIn)
				Log.pass("Test Case Passed. Object " + dataPool.get("ObjectName") + " version is incremented as expected after adding the comments & checked in.", driver);
			else
				Log.fail("Test Case Failed. Object " + dataPool.get("ObjectName") + " version is not incremented as expected after adding the comments & checked in.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_26453

	/**
	 * Permission_26458 : Check if Object version changed while changing the permission from History (Properties from task pane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Object Version - Permission"},
			description = "Check if Object version changed while changing the permission from History (Properties from task pane)")
	public void Permission_26458(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), dataPool.get("ObjectName"));//Navigate to the specific view

			homePage.listView.clickItem(dataPool.get("ObjectName"));//Selects the object in the view

			Log.message("1. Navigated to " + viewtonavigate + " and selected the object \"" + dataPool.get("ObjectName") +  "\" in search view.", driver);

			//Step-2 : Select history option from task pane
			//---------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.History.Value);//Select the history option from task pane
			homePage.listView.clickItemByIndex(0);//Select the latest version for the object
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value);//Click properties option from operations menu

			Log.message("2. Selected 'History' option from task pane & selected latest version of the object : " + dataPool.get("ObjectName") , driver);

			//Step-3 : Open metadatacard for latest object version in history view
			//--------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the opened Metadatacard 
			int versionbeforeChangePermission = metadataCard.getVersion();// get version for selected object 
			metadataCard.setPermission(dataPool.get("Permission"));//Set permission in metadatacard
			metadataCard.saveAndClose();//Save metadatacard & close

			Log.message("3. Set Permission : " + dataPool.get("Permission") + " in right pane metadatacard for selected object : " + dataPool.get("ObjectName") + " in latest history view." , driver);

			//Step-4 : Select latest version of specified object in history view
			//------------------------------------------------------------------
			homePage.listView.clickItemByIndex(0);//Select the latest version for the object
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);//Click properties option from task pane

			metadataCard = new MetadataCard(driver);//Instantiate the opened Metadatacard 
			int versionAfterChangePermission = metadataCard.getVersion();// get version for selected object

			Log.message("4. Opened the latest version in history view for selected object " + dataPool.get("ObjectName") + " metadatcard.", driver);

			//Verification : Verify if Object version changed while changing the permission from History (Properties from task pane)
			if(versionbeforeChangePermission==versionAfterChangePermission)
				Log.pass("Test Case Passed. Object verison is not changed while changing the permission from history view for selected object : " + dataPool.get("ObjectName"), driver);
			else
				Log.fail("Test Case Failed. Object verison is changed while changing the permission from history view for selected object : " + dataPool.get("ObjectName"), driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_26458

	/**
	 * Permission_26462 : Check if Object version unchanged without changing the permission from metadata pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Object Version - Permission"},
			description = "Check if Object version unchanged without changing the permission from metadata pane")
	public void Permission_26462(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), dataPool.get("ObjectName"));//Navigate to the specific view
			homePage.listView.clickItem(dataPool.get("ObjectName"));//Selects the object in the view

			Log.message("1. Navigated to " + viewtonavigate + " and selected the object \"" + dataPool.get("ObjectName") +  "\" in search view.", driver);

			//Step-2 : Select the permission which is already set in opened metadatacard
			//--------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiate the right pane metadatacard

			String defaultPermission = metadataCard.getPermission();//Get the permission set in right pane metadatacard
			int beforePermissionChange = metadataCard.getVersion();//Get version from metadatacard

			metadataCard.setPermission(defaultPermission);//Set default permission in right pane metadatacard
			int AfterPermissionChange = metadataCard.getVersion();//Get version from metadatacard

			Log.message("2. Fetched the version of metadatacard Version : " + beforePermissionChange + " value from right pane metadatacard without changing  permission value for selected object : " + dataPool.get("ObjectName"), driver);

			//Verification : Check if Object version unchanged without changing the permission from metadata pane 
			//---------------------------------------------------------------------------------------------------
			if(!metadataCard.isSaveButtonDisplayed() && (beforePermissionChange==AfterPermissionChange))
				Log.pass("Test Case Passed. Object version is unchanged & save button is not displayed without changing the permission in right pane metadatacard.", driver);
			else
				Log.fail("Test Case Failed. Object version is changed & save button is displayed without changing the permission in right pane metadatacard.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_26462

	/**
	 * Permission_26463 : Check if Object version changed while changing the permission from metadata pane and discarding the operation
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Object Version - Permission"},
			description = "Check if Object version changed while changing the permission from metadata pane and discarding the operation")
	public void Permission_26463(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), dataPool.get("ObjectName"));//Navigate to the specific view
			homePage.listView.clickItem(dataPool.get("ObjectName"));//Selects the object in the view

			Log.message("1. Navigated to " + viewtonavigate + " and selected the object \"" + dataPool.get("ObjectName") +  "\" in search view.", driver);

			//Step-2 : Select the permission which is already set in opened metadatacard
			//--------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiate the right pane metadatacard
			int beforePermissionChange = metadataCard.getVersion();//Get version from metadatacard

			metadataCard.setPermission(dataPool.get("Permission"));//Set default permission in right pane metadatacard
			metadataCard.clickDiscardButton();//Discards the changes

			Log.message("2. Fetched the version of metadatacard Version : " + beforePermissionChange + " value from right pane metadatacard without changing  permission value for selected object : " + dataPool.get("ObjectName"), driver);

			//Verification : Check if Object version unchanged without changing the permission from metadata pane 
			//---------------------------------------------------------------------------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiate the right pane metadatacard
			int AfterPermissionChange = metadataCard.getVersion();//Get version from metadatacard
			if(beforePermissionChange == AfterPermissionChange)
				Log.pass("Test Case Passed. Object version is unchanged while changing the permission from metadata pane and discarding the operation.", driver);
			else
				Log.fail("Test Case Failed. Object version is changed while changing the permission from metadata pane and discarding the operation.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_26463


	/**
	 * Permission_26473 : Check if Object version changed while changing and assigning the previous value of workflow
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Object Version - Permission"},
			description = "Check if Object version changed while changing and assigning the previous value of workflow")
	public void Permission_26473(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), dataPool.get("ObjectName"));//Navigate to the specific view
			homePage.listView.clickItem(dataPool.get("ObjectName"));//Selects the object in the view

			Log.message("1. Navigated to " + viewtonavigate + " and selected the object \"" + dataPool.get("ObjectName") +  "\" in search view.", driver);

			//Step-2 : Change the workflow in opened metadatacard
			//---------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiate the right pane metadatacard
			int beforePermissionChange = metadataCard.getVersion();//Get version from metadatacard
			String workflow = metadataCard.getWorkflow();

			metadataCard.setWorkflow(dataPool.get("Workflow"));//Sets the workflow in the metadata card

			Log.message("2. Workflow '" + dataPool.get("Workflow") + "' is set in the metadata card.");

			//Step-3: Set the initial workflow set in the metadata card
			//----------------------------------------------------------
			metadataCard.setWorkflow(workflow);//Sets the initial workflow in the metadata card

			Log.message("3. Initial workflow '" + workflow + "' is again set in the metadata card.");

			//Step-4: Click the save button
			//-----------------------------
			metadataCard.saveAndClose();//Saves the changes

			//Verification : Check if Object version unchanged without changing the permission from metadata pane 
			//---------------------------------------------------------------------------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiate the right pane metadatacard
			int AfterPermissionChange = metadataCard.getVersion();//Get version from metadatacard
			if(beforePermissionChange == AfterPermissionChange)
				Log.pass("Test Case Passed. Object version is unchanged while changing and assigning the previous value of workflow.", driver);
			else
				Log.fail("Test Case Failed. Object version is changed while changing and assigning the previous value of workflow.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_26473

	/**
	 * Permission_26477 : Check if Object version changed on adding comments and clearing the workflow and state
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Object Version - Permission"},
			description = "Check if Object version changed on adding comments and clearing the workflow and state")
	public void Permission_26477(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), dataPool.get("ObjectName"));//Navigate to the specific view
			homePage.listView.clickItem(dataPool.get("ObjectName"));//Selects the object in the view

			Log.message("1. Navigated to " + viewtonavigate + " and selected the object \"" + dataPool.get("ObjectName") +  "\" in search view.", driver);

			//Step-2 : Set the workflow in opened metadatacard
			//---------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiate the right pane metadatacard
			metadataCard.setWorkflowState(dataPool.get("WorkflowState"));
			metadataCard.saveAndClose();

			Log.message("2. Workflow State '" + dataPool.get("WorkflowState") + "' is set in the metadata card.");

			//Step-3: Set the initial workflow set in the metadata card
			//----------------------------------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiate the right pane metadatacard
			int beforePermissionChange = metadataCard.getVersion();
			metadataCard.setWorkflow("");//Removes the workflow in the metadata card
			metadataCard.setComments(Utility.getObjectName(methodName));

			Log.message("3. Comment is set and workflow is removed in the metadatacard..");

			//Step-4: Click the save button
			//-----------------------------
			metadataCard.saveAndClose();//Saves the changes

			Log.message("4. Clicked Save button after setting comments and workflow.");

			//Verification : Check if Object version unchanged without changing the permission from metadata pane 
			//---------------------------------------------------------------------------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiate the right pane metadatacard
			int AfterPermissionChange = metadataCard.getVersion();//Get version from metadatacard

			if(beforePermissionChange+1 == AfterPermissionChange)
				Log.pass("Test Case Passed. Object version changed on adding comments and clearing the workflow and state.", driver);
			else
				Log.fail("Test Case Failed. Object version is not changed on adding comments and clearing the workflow and state.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_26477

	/**
	 * Permission_26479 : Check if Object version changed while changing any metadata card details and then changing the permission 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Object Version - Permission"},
			description = "Check if Object version changed while changing any metadata card details and then changing the permission")
	public void Permission_26479(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), dataPool.get("SearchWord"));//Navigate to the specific view
			homePage.listView.clickItem(dataPool.get("ObjectName"));//Selects the object in the view

			Log.message("1. Navigated to " + viewtonavigate + " and selected the object \"" + dataPool.get("ObjectName") +  "\" in search view.", driver);

			//Step-2 : Select the permission which is already set in opened metadatacard
			//--------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiate the right pane metadatacard

			int beforePermissionChange = metadataCard.getVersion();//Get version from metadatacard
			metadataCard.setInfo(dataPool.get("Properties"));//Set the property value & name
			
			metadataCard.setPermission(dataPool.get("Permission"));//Enter the permission in right pane metadatacard
			metadataCard.saveAndClose();//Save metadatacard		

			metadataCard = new MetadataCard(driver, true);//Instantiate the right pane metadatacard
			int AfterPermissionChange = metadataCard.getVersion();//Get version from metadatacard after changing the properties value

			Log.message("2. Entered the property values & change the permission for selected object : " + dataPool.get("ObjectName"), driver);

			//Verification : Check if Object version changed while changing any metadata card details and then changing the permission
			//------------------------------------------------------------------------------------------------------------------------
			if((beforePermissionChange+1)==AfterPermissionChange)
				Log.pass("Test Case Passed. Object version is changed while changing the metadatacard details.", driver);
			else
				Log.fail("Test Case Failed. Object version is not changed while changing the metadatacard details.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_26479

	/**
	 * Permission_38049 : Check workflow dialog gets closed when read only user clicks on Access denied error message 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Workflow - Permissions"},
			description = "Check workflow dialog gets closed when read only user clicks on Access denied error message")
	public void Permission_38049(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("User"), dataPool.get("Password"), testVault); //Launched driver and logged in

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), dataPool.get("ObjectName"));//Navigate to the specific view
			homePage.listView.clickItem(dataPool.get("ObjectName"));//Selects the object in the view

			Log.message("1. Navigated to " + viewtonavigate + " and selected the object \"" + dataPool.get("ObjectName") +  "\" in search view.", driver);

			//Step-2 : Select the workflow option from operations menu
			//--------------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);//Select the workflow option from operations menu for selelcted object

			MFilesDialog mfilesDialog = new MFilesDialog(driver);//Instantiate the workflow M-files dialog
			mfilesDialog.setWorkflow(dataPool.get("Workflow"));//Set the workflow for selected object
			mfilesDialog.clickOkButton();//Click the 'Ok' button in M-files dialog

			Log.message("2. Opened the workflow dialog & set the workflow : " + dataPool.get("Workflow") + " for selected object : " + dataPool.get("ObjectName"), driver);

			String ExpectedResults = "";

			//Step-3 : Click ok button in 'Access denied error message'
			//---------------------------------------------------------
			mfilesDialog = new MFilesDialog(driver);//Instantiate the workflow M-files dialog

			if(!mfilesDialog.getMessage().equalsIgnoreCase(dataPool.get("ErrorMessage")))//Verify if error message is displayed or not
				ExpectedResults = "Error message is not displayed as expected : " + mfilesDialog.getMessage();

			mfilesDialog.clickOkButton();//Click 'Ok' button in M-files dialog

			Log.message("3. Clicked 'Ok' button in 'Access denied error message' M-files dialog.", driver);

			mfilesDialog.clickCloseButton();//Click close button in M-files dialog

			if(mfilesDialog.isWorkflowDialogDisplayed())//Verify if workflow dialog is displayed
				ExpectedResults += "Workflow dialog is displayed after selecting close button ";

			Log.message("4. Closed the workflow dialog after selecting the 'Ok' button in Read-only user 'Access denied error message' M-files dialog.");

			//Verification : Check workflow dialog gets closed when read only user clicks on Access denied error message
			//----------------------------------------------------------------------------------------------------------
			if(ExpectedResults.equals(""))
				Log.pass("Test Case Passed. Workflow dialog is closed when read only user clicks 'Ok' button in Access denied error message.", driver);
			else
				Log.fail("Test Case Failed. Workflow dialog is not closed when read only user clicks 'Ok' button in Access denied error message; AddInfo : " + ExpectedResults, driver);


		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_38049	

	/**
	 * Permission_38044 : Check read only user able to add comments 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Workflow - Permissions"},
			description = "Check read only user able to add comments")
	public void Permission_38044(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("User"), dataPool.get("Password"), testVault); //Launched driver and logged in

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), dataPool.get("ObjectName"));//Navigate to the specific view
			homePage.listView.clickItem(dataPool.get("ObjectName"));//Selects the object in the view

			Log.message("1. Navigated to " + viewtonavigate + " and selected the object \"" + dataPool.get("ObjectName") +  "\" in search view.", driver);

			//Step-2 : Enter comments for selected object
			//-------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiate the right pane metadatacard
			metadataCard.setComments(dataPool.get("Comments"));//Click comments in right pane metadatacard
			metadataCard.saveAndClose();//Save the metadatacard

			Log.message("2. Entered the comments for selected object : " + dataPool.get("ObjectName") + " in right pane metadatacard.", driver);

			//Verification : Verify if 'Access denied error' message should be displayed for read-only user
			//---------------------------------------------------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver);//Instantiate the workflow M-files dialog
			if(mfilesDialog.getMessage().contains(dataPool.get("ErrorMessage")))
				Log.pass("Test Case Passed. Access denied error message is displayed while read-only user " + dataPool.get("User") + " try to set comments", driver);
			else
				Log.fail("Test Case Failed. Access denied error message is not displayed while read-only user " + dataPool.get("User") + " try to set comments", driver);				

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_38044

	/**
	 * Permission_38034 : Check if Read only user should not be able to create an object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Read only user"},
			description = "Check if Read only user should not be able to create an object")
	public void Permission_38034(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("User"), dataPool.get("Password"), testVault); //Launched driver and logged in

			//Step-1 : Select new object type with read-only user from Menu bar/Task pane
			//---------------------------------------------------------------------------
			if(dataPool.get("Operation").equalsIgnoreCase("TaskPane")){//Verify if given operation is Task pane or not

				homePage.taskPanel.clickItem(dataPool.get("ObjectType"));//Select new option from menu bar

				Log.message("1. Select New : " + dataPool.get("ObjectType") + " option from menu bar.", driver);

			}//End if
			else{//If given operation is not task pane, select menubar in else part

				homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//Select new option from menu bar

				Log.message("1. Select New : " + dataPool.get("ObjectType") + " option from menu bar.", driver);
			}

			//Verification : Verify if 'Access denied error' message should be displayed for read-only user
			//---------------------------------------------------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver, "M-Files Web");//Instantiate the workflow M-files dialog
			if(mfilesDialog.getMessage().contains(dataPool.get("ErrorMessage")))
				Log.pass("Test Case Passed. Access denied error message is displayed while read-only user " + dataPool.get("User") + " try to Create new objects : " + dataPool.get("ObjectType"), driver);
			else
				Log.fail("Test Case Failed. Access denied error message is not displayed while read-only user " + dataPool.get("User") + " try to Create new objects : " + dataPool.get("ObjectType"), driver);				

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_38034

	/**
	 * Permission_38032 : Check if read only/ read license user able to perform mark complete operation for combination
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Read only user"},
			description = "Check if read only/ read license user able to perform mark complete operation for combination")
	public void Permission_38032(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create the new object with read-only user
			//--------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);//Select new option from menu bar

			String objName = Utility.getObjectName(methodName);//Gets object name 

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the metadatacard
			metadataCard.setPropertyValue("Name or title", objName);//Set name or title value in new assignment metadatacard

			metadataCard.setInfo(dataPool.get("Properties1"));//Set required property values in metadatacard
			metadataCard.saveAndClose();//created new assignment object

			Log.message("1. Created new Assignment (" + objName + ") object from menu bar & Entered the required property values.", driver);

			//Step-2 : Select 'Mark complete' icon in right pane metadatacard for created new assignment object
			//-------------------------------------------------------------------------------------------------
			if(!homePage.listView.isItemSelected(objName))//Verify if object is selected or not
				homePage.listView.clickItem(objName);//Click object in listview

			metadataCard = new MetadataCard(driver, true);//Instantiate the right pane metadatacard
			metadataCard.markApproveByUser(dataPool.get("UserfullName"));//Select Mark approve icon from Username
			metadataCard.saveAndClose();//Save metadatacard changes

			Log.message("2. Clicked 'Mark approve icon' in right pane metadatacard for created assignment : " + objName, driver);

			//Step-3 : Create another new 'Assignment' object from task pane
			//--------------------------------------------------------------
			driver.navigate().refresh();//Refresh web page
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Assignment.Value);//Select new assignment option from task menu

			String objName1 = Utility.getObjectName(methodName);//Get object name

			metadataCard = new MetadataCard(driver);//Instantiate the metadatacard
			metadataCard.setPropertyValue("Name or title", objName1);//Set name or title value in new assignment metadatacard

			metadataCard.setInfo(dataPool.get("Properties1"));//Set required property values in metadatacard
			metadataCard.saveAndClose();//created new assignment object

			Log.message("3. Created antoher new Assignment (" + objName1 + ") object from task pane & Entered the required property values.", driver);

			//Step-4 : Log out from admin user & login as read-only user
			//----------------------------------------------------------
			homePage.menuBar.clickLogOut();//Click logout from admin user
			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("User"), dataPool.get("Password"), testVault); //Launched driver and logged in

			Log.message("4. Logged out from admin user (" + userName + ") and logged in as read-only user (" + dataPool.get("User") + ").", driver);

			//Step-5 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), "");//Navigate to the specific view

			String assignNames = objName + "\n" + objName1;//Upend the created assignment object with names
			homePage.listView.clickMultipleItems(assignNames);//Selects the specified object in the search view

			Log.message("5. Navigated to " + viewtonavigate + " and selected the multiple assignment object \"" + assignNames +  "\" in search view.", driver);

			//Step-6 : Select the varies icon in mulitple assignment objects
			//--------------------------------------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiate the right pane metadatacard
			metadataCard.clickVariesIcon(dataPool.get("UserfullName"));
			metadataCard.saveAndClose();//Save the changes in metadatacard

			Log.message("6. Clicked the varies icon in multiple assignments by Read-only user : " + dataPool.get("User"), driver);

			//Verification : Check if read only/read license user able to perform mark complete operation for combination
			//------------------------------------------------------------------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver);//Instantiate the workflow M-files dialog
			if(mfilesDialog.getMessage().contains(dataPool.get("ErrorMessage")))
				Log.pass("Test Case Passed. Access denied error message is displayed while read-only user " + dataPool.get("User") + " try to Mark complete the created assignment.", driver);
			else
				Log.fail("Test Case Failed. Access denied error message is not displayed while read-only user " + dataPool.get("User") + " try to Mark complete the created assignment.", driver);				

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_38036

	/**
	 * Permission_38033 : Check if read only/ read license user able to perform mark complete
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Read only user"},
			description = "Check if read only/ read license user able to perform mark complete")
	public void Permission_38033(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create the new object with read-only user
			//--------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);//Select new option from menu bar

			String objName = Utility.getObjectName(methodName);//Gets object name 

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the metadatacard
			metadataCard.setPropertyValue("Name or title", objName);//Set name or title value in new assignment metadatacard

			metadataCard.setInfo(dataPool.get("Properties1"));//Set required property values in metadatacard
			metadataCard.saveAndClose();//created new assignment object

			Log.message("1. Created new Assignment (" + objName + ") object from menu bar & Entered the required property values.", driver);

			//Step-2 : Create another new 'Assignment' object from task pane
			//--------------------------------------------------------------
			driver.navigate().refresh();//Refresh web page
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Assignment.Value);//Select new assignment option from task menu

			String objName1 = Utility.getObjectName(methodName);//Get object name

			metadataCard = new MetadataCard(driver);//Instantiate the metadatacard
			metadataCard.setPropertyValue("Name or title", objName1);//Set name or tina onutle value in new assignment metadatacard

			metadataCard.setInfo(dataPool.get("Properties1"));//Set required property values in metadatacard
			metadataCard.saveAndClose();//created new assignment object

			Log.message("2. Created antoher new Assignment (" + objName1 + ") object from task pane & Entered the required property values.", driver);

			//Step-4 : Log out from admin user & login as read-only user
			//----------------------------------------------------------
			homePage.menuBar.clickLogOut();//Click logout from admin user
			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("User"), dataPool.get("Password"), testVault); //Launched driver and logged in

			Log.message("4. Logged out from admin user (" + userName + ") and logged in as read-only user (" + dataPool.get("User") + ").", driver);

			//Step-5 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), "");//Navigate to the specific view

			String assignNames = objName + "\n" + objName1;//Upend the created assignment object with names
			homePage.listView.clickMultipleItems(assignNames);//Selects the specified object in the search view

			Log.message("5. Navigated to " + viewtonavigate + " and selected the multiple assignment object \"" + assignNames +  "\" in search view.", driver);

			//Step-6 : Select the varies icon in mulitple assignment objects
			//--------------------------------------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiate the right pane metadatacard
			metadataCard.markApproveByUser(dataPool.get("UserfullName"));
			metadataCard.saveAndClose();//Save the changes in metadatacard

			Log.message("6. Clicked the varies icon in multiple assignments by Read-only user : " + dataPool.get("User"), driver);

			//Verification : Check if read only/read license user able to perform mark complete operation for combination
			//------------------------------------------------------------------------------------------------------------
			if(metadataCard.markApproveByUser(dataPool.get("UserfullName")))
				Log.pass("Test Case Passed. Selected assignments : " + assignNames + " are completed by read only user : " + dataPool.get("User") + " successfully.", driver);
			else
				Log.fail("Test Case Failed. Selected assignments : " + assignNames + " are not completed by read only user : " + dataPool.get("User"), driver);				

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_38036

	/**
	 * Permission_26662 : Check the Date created/Date modified column value get change while changing the permission
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Date Created - Permission"},
			description = "Check the Date created column value get change while changing the permission")
	public void Permission_26662(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), dataPool.get("ObjectName"));//Navigate to the specific view
			homePage.listView.clickItem(dataPool.get("ObjectName"));//Selects the object in the view

			Log.message("1. Navigated to " + viewtonavigate + " and selected the object \"" + dataPool.get("ObjectName") +  "\" in search view.", driver);

			//Step-2 : Get specified column value before change the permission
			//----------------------------------------------------------------
			String beforeChngngPrmsnColumnValue = homePage.listView.getColumnValueByItemName(dataPool.get("ObjectName"), dataPool.get("ColumnName"));

			Log.message("2. Fetched " + dataPool.get("ColumnName") + " column value " + beforeChngngPrmsnColumnValue + " for selected object : " + dataPool.get("ObjectName") + " before changing the permission.", driver);

			//Step-3 : Change permission for selected object
			//----------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiate the right pane metadatacard
			int versionBeforeChangingPermission = metadataCard.getVersion();//get metadatacard version for selected object

			Log.message("3. Fetched version from selected object : " + versionBeforeChangingPermission + " before changing permission.", driver);

			//Step-4 : Set new permission value in right pane metadatacard
			//-------------------------------------------------------------
			metadataCard.setPermission(dataPool.get("Permission"));//Change the permission for selected object
			metadataCard.saveAndClose();//Save right pane metadatacard

			Log.message("4. Changed the permission for selected object : " + dataPool.get("ObjectName") + " in right pane metadatacard.", driver);

			//Step-5 : Fetch version of selected object after modified permission value
			//-------------------------------------------------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiate the right pane metadatacard
			int versionAfterChangingPermission = metadataCard.getVersion();//Get version of selected object metadatacard

			Log.message("5. Fetched version : "+ versionAfterChangingPermission + "for selected object : " + dataPool.get("ObjectName") + " after changing permission value : " + dataPool.get("Permission"));

			String afterChngngPrmsnColumnValue = homePage.listView.getColumnValueByItemName(dataPool.get("ObjectName"), dataPool.get("ColumnName"));

			String ExpectedResults = "";

			//Verify if version is retaining same value after changing the permission value
			if(!(versionBeforeChangingPermission==versionAfterChangingPermission))
				ExpectedResults = "Object version is not same after changing the permission value for selected object : " + dataPool.get("ObjectName") ;

			//Verify if column value retaining same value after changing permission value
			if(!beforeChngngPrmsnColumnValue.equals(afterChngngPrmsnColumnValue))
				ExpectedResults += "Column value is not same after changing the permission value for selected object : " + dataPool.get("ObjectName") ;

			//Verification : Check the Date created/Date modified column value get change while changing the permission
			//---------------------------------------------------------------------------------------------------------
			if(ExpectedResults.equals(""))
				Log.pass("Test Case Passed. Column value : " + dataPool.get("ColumnName") + "  and object version : " + versionAfterChangingPermission + " is not modifying after changing permission value.", driver);
			else
				Log.fail("Test Case Failed. Column value : " + dataPool.get("ColumnName") + "  and object version : " + versionAfterChangingPermission + " is modifying after changing permission value. AddInfo : " + ExpectedResults, driver);				

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_26662

	/**
	 * Permission_30015 : Check the version remains unchanged on changing the class and changing the permission
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Object Version - Permission"},
			description = "Check the version remains unchanged on changing the class and changing the permission")
	public void Permission_30015(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			Log.testCaseInfo(Utility.getMethodDescription() + "[" + driverType.toUpperCase() + "]", className + " - " + Utility.getMethodName(), className, productVersion);

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), dataPool.get("ObjectName"));//Navigate to the specific view

			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))//Selects the object in the view
				throw new Exception("Object '" + dataPool.get("ObjectName") + "' is not selected in the view.");

			Log.message("1. Navigated to " + viewtonavigate + " and selected the object \"" + dataPool.get("ObjectName") +  "\" in search view.", driver);

			//Step-2 : Change the class in the metadata card
			//-----------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiate metadatacard
			int initialVersion = metadataCard.getVersion();
			metadataCard.setPropertyValue(dataPool.get("Property"), dataPool.get("PropertyValue"));//Set the class property value in metadatacard
			metadataCard.saveAndClose();//Save metadatacard changes

			Log.message("2. Class changed and object saved.");

			//Step-3: Change permission and save
			//----------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiate metadatacard

			if(initialVersion == metadataCard.getVersion())
				throw new Exception("Object version is not increased while changing the class of the object");
			initialVersion = metadataCard.getVersion();
			metadataCard.setPermission(dataPool.get("Permission"));//Sets the permission
			metadataCard.saveAndClose();//Saves the changes

			Log.message("3. Permission is set and saved the changes.");

			//Verification: Check if object version is changed
			//------------------------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiate metadatacard
			if(initialVersion == metadataCard.getVersion())
				Log.pass("Test case passed. Object version remains unchanged on changing the class and changing the permission.");
			else
				Log.fail("Test case failed. Object version is changed on changing the class and changing the permission", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_30015

	/**
	 * Permission_30016 : Check the version remains unchanged on making a copy
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Make copy - Permission"},
			description = "Check the version remains unchanged on making a copy")
	public void Permission_30016(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			Log.testCaseInfo(Utility.getMethodDescription() + "[" + driverType.toUpperCase() + "]", className + " - " + Utility.getMethodName(), className, productVersion);

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			String objName = Utility.getObjectName(methodName);

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), dataPool.get("ObjectName"));//Navigate to the specific view
			homePage.listView.clickItem(dataPool.get("ObjectName"));//Selects the object in the view

			Log.message("1. Navigated to " + viewtonavigate + " and selected the object \"" + dataPool.get("ObjectName") +  "\" in search view.", driver);

			//Step-2 : create document using make copy option
			//-----------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.MakeCopy.Value);//Set the Make Copy option from task pane

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate metadatacard
			metadataCard.setPropertyValue(dataPool.get("Title"), objName);//Set the name property value in metadatacard
			metadataCard.setCheckInImmediately(true);//Set the Metadatacard checkin immetidately

			metadataCard.saveAndClose();//Save metadatacard changes

			Log.message("2. Created the new object : " + objName + " using make copy option for selected object : " + dataPool.get("ObjectName"), driver);

			//Step-3 : Change the permission in right pane metadatacard
			//---------------------------------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiate right pane metadatacard
			int beforeChangePermissison = metadataCard.getVersion();//Get version for created object before changing the permission

			metadataCard.setPermission(dataPool.get("Permission"));//Set new permission in right pane metadatacard
			metadataCard.saveAndClose();//Save the metadatacard changes

			metadataCard = new MetadataCard(driver, true);//Instantiate right pane metadatacard
			int afterChangePermissison = metadataCard.getVersion();//Get version for created object before changing the permission

			Log.message("3. Changed the Permission : " + dataPool.get("Permission") + " for selected metadatacard object : " + objName, driver);

			//Verification : Check the version remains unchanged on making a copy
			//-------------------------------------------------------------------
			if(beforeChangePermissison==afterChangePermissison)
				Log.pass("Test Case Passed. Object version is not increased while changing the permission : " + dataPool.get("Permission") + " using 'Make Copy' option.", driver);
			else
				Log.fail("Test Case Failed. Object version is increased while changing the permission : " + dataPool.get("Permission") + " using 'Make Copy' option.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_30016

	/**
	 * Permission_28480 : Verify admin user able to assign workflow with place holder option
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Read only user"},
			description = "Verify admin user able to assign workflow with place holder option.")
	public void Permission_28480(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			Log.testCaseInfo(Utility.getMethodDescription() + "[" + driverType.toUpperCase() + "]", className + " - " + Utility.getMethodName(), className, productVersion);

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), dataPool.get("ObjectName"));//Navigate to the specific view
			homePage.listView.clickItem(dataPool.get("ObjectName"));//Selects the object in the view

			Log.message("1. Navigated to " + viewtonavigate + " and selected the object \"" + dataPool.get("ObjectName") +  "\" in search view.", driver);

			//Step-2 : Perform the state transition for selected workflow document
			//--------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiate the right pane metadatacard

			//Verify if workflow value is set as expected
			//-------------------------------------------
			if(!metadataCard.getWorkflow().equalsIgnoreCase(dataPool.get("Workflow")))
				throw new Exception("Invalid test data. Select the workflow document.Selected document workflow  : " + metadataCard.getWorkflow());

			//Set workflow for the selected object 
			//------------------------------------
			metadataCard.setWorkflowState(dataPool.get("WorkflowState"));
			metadataCard.saveAndClose();//Save the metadatacard changes	

			Log.message("2. Expected workflow state : " + dataPool.get("WorkflowState") + "is set in selected object : " + dataPool.get("ObjectName"), driver);

			String ExpectedResults = "";

			metadataCard = new MetadataCard(driver, true);//Instantiate the right pane metadatacard

			//Verify if expected property is displayed in metadatacard
			if(!metadataCard.getPropertyValue(dataPool.get("PropName1")).equalsIgnoreCase(dataPool.get("PropValue1")))
				ExpectedResults = "Place holder Property : " + dataPool.get("PropName1") + " value is not set as expected in selected object metadatacard : " + dataPool.get("ObjectName");

			if(!metadataCard.getPropertyValue(dataPool.get("PropName2")).equalsIgnoreCase(dataPool.get("PropValue2")))
				ExpectedResults += "Place holder Property : " + dataPool.get("PropName2") + " value is not set as expected in selected object metadatacard : " + dataPool.get("ObjectName");

			//Verification : Verify admin user able to assign workflow with place holder option
			//---------------------------------------------------------------------------------
			if(ExpectedResults.equals(""))
				Log.pass("Test Case Passed. Place holder options : " + dataPool.get("PropName1") + "," + dataPool.get("PropName2") + " are set as expected with assigned workflow in selected object : " + dataPool.get("ObjectName"), driver);
			else
				Log.fail("Test Case Failed. Place holder options : " + dataPool.get("PropName1") + "," + dataPool.get("PropName2") + " are not set as expected with assigned workflow in selected object : " + dataPool.get("ObjectName"), driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_28480

	/**
	 * Permission_28481 : Verify Read only license type user able to assign workflow with place holder option
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Read only user"},
			description = "Verify Read only license type user able to assign workflow with place holder option.")
	public void Permission_28481(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			Log.testCaseInfo(Utility.getMethodDescription() + "[" + driverType.toUpperCase() + "]", className + " - " + Utility.getMethodName(), className, productVersion);

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("User"), dataPool.get("Password"), testVault); //Launched driver and logged in

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), dataPool.get("ObjectName"));//Navigate to the specific view
			homePage.listView.clickItem(dataPool.get("ObjectName"));//Selects the object in the view

			Log.message("1. Navigated to " + viewtonavigate + " and selected the object \"" + dataPool.get("ObjectName") +  "\" in search view.", driver);

			//Step-2 : Perform the state transition for selected workflow document
			//--------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiate the right pane metadatacard

			//Verify if workflow value is set as expected
			//-------------------------------------------
			if(!metadataCard.getWorkflow().equalsIgnoreCase(dataPool.get("Workflow")))
				throw new Exception("Invalid test data. Select the workflow document.Selected document workflow  : " + metadataCard.getWorkflow());

			//Set workflow for the selected object 
			//------------------------------------
			metadataCard.setWorkflowState(dataPool.get("WorkflowState"));
			metadataCard.saveAndClose();//Save the metadatacard changes	

			Log.message("2. Expected workflow state : " + dataPool.get("WorkflowState") + "is set in selected object : " + dataPool.get("ObjectName"), driver);

			//Verification : Verify Read only license type user able to assign workflow with place holder option
			//--------------------------------------------------------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver, "M-Files Web");//Instantiate M-files dialog
			if(mfilesDialog.getMessage().equalsIgnoreCase(dataPool.get("ErrorMessage")))
				Log.pass("Test Case Passed. Accessed Denied error is displayed while setting workflow state : " + dataPool.get("WorkflowState") + " by read only user : " + dataPool.get("User"), driver);
			else
				Log.fail("Test Case Failed. Read-only user : " + dataPool.get("User") + " have permission for set the workflow state : " + dataPool.get("WorkflowState"), driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_28481

	/**
	 * Permission_31759 : Verify object version on changing permission of multiple objects having same workflow and different state
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Read only user", "Bug --> 135053"},
			description = "Verify object version on changing permission of multiple objects having same workflow and different state.")
	public void Permission_31759(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			Log.testCaseInfo(Utility.getMethodDescription() + "[" + driverType.toUpperCase() + "]", className + " - " + Utility.getMethodName(), className, productVersion);

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), dataPool.get("MultiObjectNames"));//Navigate to the specific view
			homePage.listView.clickMultipleItems(dataPool.get("MultiObjectNames"));//Selects the object in the view

			Log.message("1. Navigated to " + viewtonavigate + " and selected the multiple objects \"" + dataPool.get("MultiObjectNames") +  "\" with different workflow state in search view.", driver);

			//Step-2 : Change the permission for selected objects
			//---------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiate the right pane metadatacard

			if(!metadataCard.getWorkflowState().equalsIgnoreCase("Varies"))//Verify if workflow state is different or not
				throw new Exception("Invalid Test Data. Selected object dont have the different workflow state.");

			int versionBeforeChngePermission = metadataCard.getVersion();//get the version of selected objects 

			//Change the permission for selected objects
			//------------------------------------------
			metadataCard.setPermission(dataPool.get("Permission"));
			metadataCard.saveAndClose();//Save the metadatacard changes

			int versionAfterChngePermission = metadataCard.getVersion();//get the version of selected objects after changing the permission

			Log.message("2. Changed the Permission : " + dataPool.get("Permission") + " for the selected objects : " + dataPool.get("MultiObjectNames"), driver);

			//Verification : Verify if object version on changing permission of multiple objects having same workflow and different state
			//---------------------------------------------------------------------------------------------------------------------------
			if(versionBeforeChngePermission==versionAfterChngePermission)
				Log.pass("Test Case Passed. Object version is not changed while changing the permission of multiple objects having same workflow and different state.", driver);
			else 
				Log.fail("Test Case Failed. Object version is changed for multiple objects while changing permission having same workflow and different state.", driver);


		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_31759

	/**
	 * Permission_31755 : Verify object version on changing permission of multiple objects with empty workflow
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Read only user"},
			description = "Verify object version on changing permission of multiple objects with empty workflow.")
	public void Permission_31755(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			Log.testCaseInfo(Utility.getMethodDescription() + "[" + driverType.toUpperCase() + "]", className + " - " + Utility.getMethodName(), className, productVersion);

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), "");//Navigate to the specific view

			Log.message("1. Navigated to " + viewtonavigate + " view.", driver);

			//Step-2: Get the object versions
			//-------------------------------
			int[] beforeChange = new int[5];
			MetadataCard metadataCard = null;

			for(int i = 0; i < dataPool.get("MultiObjectNames").split("\n").length; i++ )
			{
				if(!homePage.listView.clickItem(dataPool.get("MultiObjectNames").split("\n")[i]))
					throw new Exception("Object '" + dataPool.get("MultiObjectNames").split("\n")[i] + "' is not selected in the view");

				metadataCard = new MetadataCard(driver, true);//Instantiate the right pane metadatacard
				beforeChange[i] = metadataCard.getVersion();				
			}

			Log.message("2. Got the version of the objects before permission change.");

			//Step-3 : Change the permission for selected objects
			//---------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("MultiObjectNames"));//Selects the object in the view

			metadataCard = new MetadataCard(driver, true);//Instantiate the right pane metadatacard
			if(!metadataCard.getWorkflow().equalsIgnoreCase("Varies"))//Verify if workflow state is different or not
				throw new Exception("Invalid Test Data. Selected object dont have the different workflow.");

			metadataCard = new MetadataCard(driver, true);//Instantiate the right pane metadatacard
			String initalPermission= metadataCard.getPermission();
			metadataCard.setPermission(dataPool.get("Permission"));
			metadataCard.saveAndClose();//Save the metadatacard changes

			Log.message("3. Changed the Permission : " + dataPool.get("Permission") + " for the selected objects", driver);

			//Step-4: Get the object Versions
			//-------------------------------
			if(!homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value))
				throw new Exception("Home is not clicked from the task panel.");

			SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), "");//Navigate to the specific view

			int[] afterChange = new int[5];
			for(int i = 0; i < dataPool.get("MultiObjectNames").split("\n").length; i++ )
			{
				if(!homePage.listView.clickItem(dataPool.get("MultiObjectNames").split("\n")[i]))
					throw new Exception("Object '" + dataPool.get("MultiObjectNames").split("\n")[i] + "' is not selected in the view");

				metadataCard = new MetadataCard(driver, true);//Instantiate the right pane metadatacard
				afterChange[i] = metadataCard.getVersion();				
			}

			Log.message("4. Got the version of the objects after permission change.");

			//Verification : Verify if object version on changing permission of multiple objects having empty workflow
			//--------------------------------------------------------------------------------------------------------
			String result = ""; 

			for(int i = 0; i < dataPool.get("MultiObjectNames").split("\n").length; i++)
				if(beforeChange[i] != afterChange[i])
					result += "Object ' "+ dataPool.get("MultiObjectNames").split("\n") +" ' version is increased.";

			//Verification : Verify if object version on changing permission of multiple objects having empty workflow
			//--------------------------------------------------------------------------------------------------------
			if(result.equals(""))
				Log.pass("Test Case Passed. Object version is not changed while changing the permission of multiple objects having empty workflow.", driver);
			else 
				Log.fail("Test Case Failed. Object version is changed for multiple objects while changing permission having empty workflow.", driver);

			homePage.listView.clickMultipleItems(dataPool.get("MultiObjectNames"));//Selects the object in the view
			metadataCard = new MetadataCard(driver, true);//Instantiate the right pane metadatacard
			metadataCard.setPermission(initalPermission);
			metadataCard.saveAndClose();//Save the metadatacard changes

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Permission_31755

}//End Permissions

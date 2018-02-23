package MFClient.Tests.Assignments;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.ExtentReporter;
import genericLibrary.Log;
import genericLibrary.TestMethodWebDriverManager;
import genericLibrary.Utils;

import java.lang.reflect.Method;
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
public class MultiSelectNewProperty {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String configURL = null;
	public static String userName = null;
	public static String password = null;
	public static String testVault = null;
	public static String productVersion = null;
	public static String userFullName = null;
	public static String className = null;

	private TestMethodWebDriverManager driverManager = null;

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
			testVault = xmlParameters.getParameter("VaultName");			
			userFullName = xmlParameters.getParameter("UserFullName");
			className = this.getClass().getSimpleName().toString().trim();

			driverManager = new TestMethodWebDriverManager();

			if (xmlParameters.getParameter("driverType").equalsIgnoreCase("IE"))
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim() + xmlParameters.getParameter("driverVersion").trim();
			else
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim();

			if (xmlParameters.getParameter("driverType").equalsIgnoreCase("IE") || xmlParameters.getParameter("driverType").equalsIgnoreCase("SAFARI")){
				Log.testCaseInfo(xmlParameters.getParameter("driverType").toUpperCase() + " driver does not supports Multi-select.", className, className, productVersion);
				ExtentReporter.skip(xmlParameters.getParameter("driverType").toUpperCase() + " driver does not supports Multi-select.");

				throw new SkipException(xmlParameters.getParameter("driverType").toUpperCase() + " driver does not supports Multi-select.");
			}

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
	 * quitDrivers : Quits and closes all web drivers started by the test method
	 */
	@AfterMethod (alwaysRun=true)
	public void quitDrivers(Method method) throws Exception {

		driverManager.quitTestMethodWebDrivers(method.getName());
		Log.endTestCase();//Ends the test case
	}

	/**
	 * 104.1.34.1A : User should be able to save multiselected (SHIFT key) assignment object with newly added empty value property - Context menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "User should be able to save multiselected (SHIFT key) assignment object with newly added empty value property - Context menu Properties")
	public void SprintTest104_1_34_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			int noOfItems = Utility.getRandomNumber(3, 5);
			String[] assignName = new String[noOfItems];
			String objName = Utility.getObjectName(Utility.getMethodName()).toString();

			for (int i=0; i<noOfItems; i++) {
				homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
				assignName[i] =  objName + "_" + i; //Name of the object with current method date & time
				MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
				metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
				metadatacard.setPropertyValue("Name or title", assignName[i]); //Sets the Assignment name
				Utils.fluentWait(driver);
				metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
				metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			}

			Log.message("1. New " + noOfItems + " Assignments are created.", driver);

			//Step-2 : Open the Properties dialog of the new assignments in context menu
			//------------------------------------------------------------------------------
			homePage.searchPanel.search(objName, Caption.Search.SearchOnlyAssignments.Value);
			homePage.listView.shiftclickMultipleItemsByIndex(0, noOfItems-1); //Select multiple items by shift key
			homePage.listView.rightClickItemByIndex(noOfItems-1); //Right clicks the item
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. Properties dialog of multi selected assignments using SHIFT key is opened through context menu.");

			//Step-3 : Add new property to the multi selected assignment objects
			//------------------------------------------------------------------
			metadatacard.addNewProperty(dataPool.get("NewProperty"));

			Log.message("3. New Property (" + dataPool.get("NewProperty") + ") is added to the multi-selected assignments.", driver);

			//Step-4 : Save the metadatacard 
			//-------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.
			Utils.fluentWait(driver);

			metadatacard = new MetadataCard(driver, true); //Instantiates the metadatacard in rightpane

			Log.message("4. Metadatacard is saved after adding new property.");

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			if (metadatacard.propertyExists(dataPool.get("NewProperty"))) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Empty value property is added successfully to multi-selected assignment object.");
			else
				Log.fail("Test case Failed. Empty value property is not added to multi-selected assignment object.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_34_1A

	/**
	 * 104.1.34.1B : User should be able to save multiselected (SHIFT key) assignment object with newly added empty value property - Operations menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "User should be able to save multiselected (SHIFT key) assignment object with newly added empty value property - Operations menu Properties")
	public void SprintTest104_1_34_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			int noOfItems = Utility.getRandomNumber(3, 5);
			String[] assignName = new String[noOfItems];
			String objName = Utility.getObjectName(Utility.getMethodName()).toString();

			for (int i=0; i<noOfItems; i++) {
				homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
				assignName[i] =  objName + "_" + i; //Name of the object with current method date & time
				MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
				metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
				metadatacard.setPropertyValue("Name or title", assignName[i]); //Sets the Assignment name
				Utils.fluentWait(driver);
				metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
				metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			}

			Log.message("1. New " + noOfItems + " Assignments are created.", driver);

			//Step-2 : Open the Properties dialog of the new assignments in context menu
			//------------------------------------------------------------------------------
			homePage.searchPanel.search(objName, Caption.Search.SearchOnlyAssignments.Value);
			homePage.listView.shiftclickMultipleItemsByIndex(0, noOfItems-1); //Select multiple items by shift key
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from operations menu

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. Properties dialog of multi selected assignments using SHIFT key is opened through operations menu.");

			//Step-3 : Add new property to the multi selected assignment objects
			//------------------------------------------------------------------
			metadatacard.addNewProperty(dataPool.get("NewProperty"));

			Log.message("3. New Property (" + dataPool.get("NewProperty") + ") is added to the multi-selected assignments.", driver);

			//Step-4 : Save the metadatacard 
			//-------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.
			Utils.fluentWait(driver);

			metadatacard = new MetadataCard(driver, true); //Instantiates the metadatacard in rightpane

			Log.message("4. Metadatacard is saved after adding new property.");

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			if (metadatacard.propertyExists(dataPool.get("NewProperty"))) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Empty value property is added successfully to multi-selected assignment object.");
			else
				Log.fail("Test case Failed. Empty value property is not added to multi-selected assignment object.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_34_1B

	/**
	 * 104.1.34.1C : User should be able to save multiselected (SHIFT key) assignment object with newly added empty value property - Taskpanel Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "User should be able to save multiselected (SHIFT key) assignment object with newly added empty value property - Taskpanel Properties")
	public void SprintTest104_1_34_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			int noOfItems = Utility.getRandomNumber(3, 5);
			String[] assignName = new String[noOfItems];
			String objName = Utility.getObjectName(Utility.getMethodName()).toString();

			for (int i=0; i<noOfItems; i++) {
				homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
				assignName[i] =  objName + "_" + i; //Name of the object with current method date & time
				MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
				metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
				metadatacard.setPropertyValue("Name or title", assignName[i]); //Sets the Assignment name
				Utils.fluentWait(driver);
				metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
				metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			}

			Log.message("1. New " + noOfItems + " Assignments are created.", driver);

			//Step-2 : Open the Properties dialog of the new assignments in context menu
			//------------------------------------------------------------------------------
			homePage.searchPanel.search(objName, Caption.Search.SearchOnlyAssignments.Value);
			homePage.listView.shiftclickMultipleItemsByIndex(0, noOfItems-1); //Select multiple items by shift key
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from taskpanel

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. Properties dialog of multi selected assignments using SHIFT key is opened through taskpanel.");

			//Step-3 : Add new property to the multi selected assignment objects
			//------------------------------------------------------------------
			metadatacard.addNewProperty(dataPool.get("NewProperty"));

			Log.message("3. New Property (" + dataPool.get("NewProperty") + ") is added to the multi-selected assignments.", driver);

			//Step-4 : Save the metadatacard 
			//-------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.
			Utils.fluentWait(driver);

			metadatacard = new MetadataCard(driver, true); //Instantiates the metadatacard in rightpane

			Log.message("4. Metadatacard is saved after adding new property.");

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			if (metadatacard.propertyExists(dataPool.get("NewProperty"))) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Empty value property is added successfully to multi-selected assignment object.");
			else
				Log.fail("Test case Failed. Empty value property is not added to multi-selected assignment object.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_34_1C

	/**
	 * 104.1.34.1D : User should be able to save multiselected (SHIFT key) assignment object with newly added empty value property - Rightpane Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "User should be able to save multiselected (SHIFT key) assignment object with newly added empty value property - Rightpane Properties")
	public void SprintTest104_1_34_1D(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			int noOfItems = Utility.getRandomNumber(3, 5);
			String[] assignName = new String[noOfItems];
			String objName = Utility.getObjectName(Utility.getMethodName()).toString();

			for (int i=0; i<noOfItems; i++) {
				homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
				assignName[i] =  objName + "_" + i; //Name of the object with current method date & time
				MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
				metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
				metadatacard.setPropertyValue("Name or title", assignName[i]); //Sets the Assignment name
				Utils.fluentWait(driver);
				metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
				metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			}

			Log.message("1. New " + noOfItems + " Assignments are created.", driver);

			//Step-2 : Open the Properties dialog of the new assignments in context menu
			//------------------------------------------------------------------------------
			homePage.searchPanel.search(objName, Caption.Search.SearchOnlyAssignments.Value);
			homePage.listView.shiftclickMultipleItemsByIndex(0, noOfItems-1); //Select multiple items by shift key

			MetadataCard metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("2. Properties dialog of multi selected assignments using SHIFT key is opened in rightpane.");

			//Step-3 : Add new property to the multi selected assignment objects
			//------------------------------------------------------------------
			metadatacard.addNewProperty(dataPool.get("NewProperty"));

			Log.message("3. New Property (" + dataPool.get("NewProperty") + ") is added to the multi-selected assignments.", driver);

			//Step-4 : Save the metadatacard 
			//-------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.
			Utils.fluentWait(driver);

			metadatacard = new MetadataCard(driver, true); //Instantiates the metadatacard in rightpane

			Log.message("4. Metadatacard is saved after adding new property.");

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			if (metadatacard.propertyExists(dataPool.get("NewProperty"))) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Empty value property is added successfully to multi-selected assignment object.");
			else
				Log.fail("Test case Failed. Empty value property is not added to multi-selected assignment object.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_34_1D

	/**
	 * 104.1.34.2A : User should be able to save multiselected (CTRL key) assignment object with newly added empty value property - Context menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "User should be able to save multiselected (CTRL key) assignment object with newly added empty value property - Context menu Properties")
	public void SprintTest104_1_34_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			int noOfItems = Utility.getRandomNumber(2, 5);
			String[] assignName = new String[noOfItems];
			String objName = Utility.getObjectName(Utility.getMethodName()).toString();

			for (int i=0; i<noOfItems; i++) {
				homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
				assignName[i] =  objName + "_" + i; //Name of the object with current method date & time
				MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
				metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
				metadatacard.setPropertyValue("Name or title", assignName[i]); //Sets the Assignment name
				Utils.fluentWait(driver);
				metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
				metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			}

			Log.message("1. New " + noOfItems + " Assignments are created.", driver);

			//Step-2 : Open the Properties dialog of the new assignments in context menu
			//--------------------------------------------------------------------------
			homePage.searchPanel.search(objName, Caption.Search.SearchOnlyAssignments.Value);
			homePage.listView.clickMultipleItems(assignName); //Select multiple items by ctrl key
			homePage.listView.rightClickItem(assignName[noOfItems-1]);
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. Properties dialog of multi selected a ssignments using CTRL key is opened through context menu.");

			//Step-3 : Add new property to the multi selected assignment objects
			//------------------------------------------------------------------
			metadatacard.addNewProperty(dataPool.get("NewProperty"));

			Log.message("3. New Property (" + dataPool.get("NewProperty") + ") is added to the multi-selected assignments.", driver);

			//Step-4 : Save the metadatacard 
			//-------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.
			Utils.fluentWait(driver);

			metadatacard = new MetadataCard(driver, true); //Instantiates the metadatacard in rightpane

			Log.message("4. Metadatacard is saved after adding new property.");

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			if (metadatacard.propertyExists(dataPool.get("NewProperty"))) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Empty value property is added successfully to multi-selected assignment object.");
			else
				Log.fail("Test case Failed. Empty value property is not added to multi-selected assignment object.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_34_2A

	/**
	 * 104.1.34.2B : User should be able to save multiselected (CTRL key) assignment object with newly added empty value property - Operations menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "User should be able to save multiselected (CTRL key) assignment object with newly added empty value property - Operations menu Properties")
	public void SprintTest104_1_34_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			int noOfItems = Utility.getRandomNumber(3, 5);
			String[] assignName = new String[noOfItems];
			String objName = Utility.getObjectName(Utility.getMethodName()).toString();

			for (int i=0; i<noOfItems; i++) {
				homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
				assignName[i] =  objName + "_" + i; //Name of the object with current method date & time
				MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
				metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
				metadatacard.setPropertyValue("Name or title", assignName[i]); //Sets the Assignment name
				Utils.fluentWait(driver);
				metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
				metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			}

			Log.message("1. New " + noOfItems + " Assignments are created.", driver);

			//Step-2 : Open the Properties dialog of the new assignments in context menu
			//------------------------------------------------------------------------------
			homePage.searchPanel.search(objName, Caption.Search.SearchOnlyAssignments.Value);
			homePage.listView.clickMultipleItems(assignName); //Select multiple items by ctrl key
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from operations menu

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. Properties dialog of multi selected assignments using CTRL key is opened through operations menu.");

			//Step-3 : Add new property to the multi selected assignment objects
			//------------------------------------------------------------------
			metadatacard.addNewProperty(dataPool.get("NewProperty"));

			Log.message("3. New Property (" + dataPool.get("NewProperty") + ") is added to the multi-selected assignments.", driver);

			//Step-4 : Save the metadatacard 
			//-------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.
			Utils.fluentWait(driver);

			metadatacard = new MetadataCard(driver, true); //Instantiates the metadatacard in rightpane

			Log.message("4. Metadatacard is saved after adding new property.");

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			if (metadatacard.propertyExists(dataPool.get("NewProperty"))) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Empty value property is added successfully to multi-selected assignment object.");
			else
				Log.fail("Test case Failed. Empty value property is not added to multi-selected assignment object.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_34_2B

	/**
	 * 104.1.34.2C : User should be able to save multiselected (CTRL key) assignment object with newly added empty value property - Taskpanel Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "User should be able to save multiselected (CTRL key) assignment object with newly added empty value property - Taskpanel Properties")
	public void SprintTest104_1_34_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			int noOfItems = Utility.getRandomNumber(3, 5);
			String[] assignName = new String[noOfItems];
			String objName = Utility.getObjectName(Utility.getMethodName()).toString();

			for (int i=0; i<noOfItems; i++) {
				homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
				assignName[i] =  objName + "_" + i; //Name of the object with current method date & time
				MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
				metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
				metadatacard.setPropertyValue("Name or title", assignName[i]); //Sets the Assignment name
				Utils.fluentWait(driver);
				metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
				metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			}

			Log.message("1. New " + noOfItems + " Assignments are created.", driver);

			//Step-2 : Open the Properties dialog of the new assignments in context menu
			//------------------------------------------------------------------------------
			homePage.searchPanel.search(objName, Caption.Search.SearchOnlyAssignments.Value);
			homePage.listView.clickMultipleItems(assignName); //Select multiple items by ctrl key
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from taskpanel

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. Properties dialog of multi selected assignments using CTRL key is opened through taskpanel.");

			//Step-3 : Add new property to the multi selected assignment objects
			//------------------------------------------------------------------
			metadatacard.addNewProperty(dataPool.get("NewProperty"));

			Log.message("3. New Property (" + dataPool.get("NewProperty") + ") is added to the multi-selected assignments.", driver);

			//Step-4 : Save the metadatacard 
			//-------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.
			Utils.fluentWait(driver);

			metadatacard = new MetadataCard(driver, true); //Instantiates the metadatacard in rightpane

			Log.message("4. Metadatacard is saved after adding new property.");

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			if (metadatacard.propertyExists(dataPool.get("NewProperty"))) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Empty value property is added successfully to multi-selected assignment object.");
			else
				Log.fail("Test case Failed. Empty value property is not added to multi-selected assignment object.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_34_2C

	/**
	 * 104.1.34.2D : User should be able to save multiselected (CTRL key) assignment object with newly added empty value property - Rightpane Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "User should be able to save multiselected (CTRL key) assignment object with newly added empty value property - Rightpane Properties")
	public void SprintTest104_1_34_2D(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			int noOfItems = Utility.getRandomNumber(3, 5);
			String[] assignName = new String[noOfItems];
			String objName = Utility.getObjectName(Utility.getMethodName()).toString();

			for (int i=0; i<noOfItems; i++) {
				homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
				assignName[i] =  objName + "_" + i; //Name of the object with current method date & time
				MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
				metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
				metadatacard.setPropertyValue("Name or title", assignName[i]); //Sets the Assignment name
				Utils.fluentWait(driver);
				metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
				metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			}

			Log.message("1. New " + noOfItems + " Assignments are created.", driver);

			//Step-2 : Open the Properties dialog of the new assignments in context menu
			//------------------------------------------------------------------------------
			homePage.searchPanel.search(objName, Caption.Search.SearchOnlyAssignments.Value);
			homePage.listView.clickMultipleItems(assignName); //Select multiple items by ctrl key

			MetadataCard metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("2. Properties dialog of multi selected assignments using CTRL key is opened in rightpane.");

			//Step-3 : Add new property to the multi selected assignment objects
			//------------------------------------------------------------------
			metadatacard.addNewProperty(dataPool.get("NewProperty"));

			Log.message("3. New Property (" + dataPool.get("NewProperty") + ") is added to the multi-selected assignments.", driver);

			//Step-4 : Save the metadatacard 
			//-------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.
			Utils.fluentWait(driver);

			metadatacard = new MetadataCard(driver, true); //Instantiates the metadatacard in rightpane

			Log.message("4. Metadatacard is saved after adding new property.");

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			if (metadatacard.propertyExists(dataPool.get("NewProperty"))) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Empty value property is added successfully to multi-selected assignment object.");
			else
				Log.fail("Test case Failed. Empty value property is not added to multi-selected assignment object.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_34_2D

	/**
	 * 104.1.34.3A : User should be able to save multiselected (SHIFT key) assignment object with newly added property and value - Context menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "User should be able to save multiselected (SHIFT key) assignment object with newly added property and value - Context menu Properties")
	public void SprintTest104_1_34_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			int noOfItems = Utility.getRandomNumber(3, 5);
			String[] assignName = new String[noOfItems];
			String objName = Utility.getObjectName(Utility.getMethodName()).toString();

			for (int i=0; i<noOfItems; i++) {
				homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
				assignName[i] =  objName + "_" + i; //Name of the object with current method date & time
				MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
				metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
				metadatacard.setPropertyValue("Name or title", assignName[i]); //Sets the Assignment name
				Utils.fluentWait(driver);
				metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
				metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			}

			Log.message("1. New " + noOfItems + " Assignments are created.", driver);

			//Step-2 : Open the Properties dialog of the new assignments in context menu
			//------------------------------------------------------------------------------
			homePage.searchPanel.search(objName, Caption.Search.SearchOnlyAssignments.Value);
			homePage.listView.shiftclickMultipleItemsByIndex(0, noOfItems-1); //Select multiple items by shift key
			homePage.listView.rightClickItemByIndex(noOfItems-1); //Right clicks the item
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. Properties dialog of multi selected assignments using SHIFT key is opened through context menu.");

			//Step-3 : Add new property and value to the multi selected assignment objects
			//-----------------------------------------------------------------------------
			metadatacard.addNewProperty(dataPool.get("NewProperty"));
			metadatacard.setPropertyValue(dataPool.get("NewProperty"), dataPool.get("PropertyValue"));

			Log.message("3. New Property (" + dataPool.get("NewProperty") + ") and value (" + dataPool.get("PropertyValue") + ") is added to the multi-selected assignments.", driver);

			//Step-4 : Save the metadatacard 
			//-------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.
			Utils.fluentWait(driver);

			metadatacard = new MetadataCard(driver, true); //Instantiates the metadatacard in rightpane

			Log.message("4. Metadatacard is saved after adding new property.");

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			if (metadatacard.propertyExists(dataPool.get("NewProperty")) && //Checks if new property and its value exists in metadatacard.
					metadatacard.getPropertyValue(dataPool.get("NewProperty")).equalsIgnoreCase(dataPool.get("PropertyValue")))
				Log.pass("Test case Passed. New property and its value is added successfully to multi-selected assignment object.");
			else
				Log.fail("Test case Failed. New property and its value is not added to multi-selected assignment object.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_34_3A

	/**
	 * 104.1.34.3B : User should be able to save multiselected (SHIFT key) assignment object with newly added property and value - Operations menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "User should be able to save multiselected (SHIFT key) assignment object with newly added property and value - Operations menu Properties")
	public void SprintTest104_1_34_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			int noOfItems = Utility.getRandomNumber(3, 5);
			String[] assignName = new String[noOfItems];
			String objName = Utility.getObjectName(Utility.getMethodName()).toString();

			for (int i=0; i<noOfItems; i++) {
				homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
				assignName[i] =  objName + "_" + i; //Name of the object with current method date & time
				MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
				metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
				metadatacard.setPropertyValue("Name or title", assignName[i]); //Sets the Assignment name
				Utils.fluentWait(driver);
				metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
				metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			}

			Log.message("1. New " + noOfItems + " Assignments are created.", driver);

			//Step-2 : Open the Properties dialog of the new assignments in context menu
			//------------------------------------------------------------------------------
			homePage.searchPanel.search(objName, Caption.Search.SearchOnlyAssignments.Value);
			homePage.listView.shiftclickMultipleItemsByIndex(0, noOfItems-1); //Select multiple items by shift key
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from operations menu

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. Properties dialog of multi selected assignments using SHIFT key is opened through operations menu.");

			//Step-3 : Add new property and value to the multi selected assignment objects
			//-----------------------------------------------------------------------------
			metadatacard.addNewProperty(dataPool.get("NewProperty"));
			metadatacard.setPropertyValue(dataPool.get("NewProperty"), dataPool.get("PropertyValue"));

			Log.message("3. New Property (" + dataPool.get("NewProperty") + ") and value (" + dataPool.get("PropertyValue") + ") is added to the multi-selected assignments.", driver);

			//Step-4 : Save the metadatacard 
			//-------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.
			Utils.fluentWait(driver);

			metadatacard = new MetadataCard(driver, true); //Instantiates the metadatacard in rightpane

			Log.message("4. Metadatacard is saved after adding new property.");

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			if (metadatacard.propertyExists(dataPool.get("NewProperty")) && //Checks if new property and its value exists in metadatacard.
					metadatacard.getPropertyValue(dataPool.get("NewProperty")).equalsIgnoreCase(dataPool.get("PropertyValue")))
				Log.pass("Test case Passed. New property and its value is added successfully to multi-selected assignment object.");
			else
				Log.fail("Test case Failed. New property and its value is not added to multi-selected assignment object.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_34_3B

	/**
	 * 104.1.34.3C : User should be able to save multiselected (SHIFT key) assignment object with newly added property and value - Taskpanel Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint104"}, 
			description = "User should be able to save multiselected (SHIFT key) assignment object with newly added property and value - Taskpanel Properties")
	public void SprintTest104_1_34_3C(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			int noOfItems = Utility.getRandomNumber(3, 5);
			String[] assignName = new String[noOfItems];
			String objName = Utility.getObjectName(Utility.getMethodName()).toString();

			for (int i=0; i<noOfItems; i++) {
				homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
				assignName[i] =  objName + "_" + i; //Name of the object with current method date & time
				MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
				metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
				metadatacard.setPropertyValue("Name or title", assignName[i]); //Sets the Assignment name
				Utils.fluentWait(driver);
				metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
				metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			}

			Log.message("1. New " + noOfItems + " Assignments are created.", driver);

			//Step-2 : Open the Properties dialog of the new assignments in context menu
			//------------------------------------------------------------------------------
			homePage.searchPanel.search(objName, Caption.Search.SearchOnlyAssignments.Value);
			homePage.listView.shiftclickMultipleItemsByIndex(0, noOfItems-1); //Select multiple items by shift key
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from taskpanel

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. Properties dialog of multi selected assignments using SHIFT key is opened through taskpanel.");

			//Step-3 : Add new property and value to the multi selected assignment objects
			//-----------------------------------------------------------------------------
			metadatacard.addNewProperty(dataPool.get("NewProperty"));
			metadatacard.setPropertyValue(dataPool.get("NewProperty"), dataPool.get("PropertyValue"));

			Log.message("3. New Property (" + dataPool.get("NewProperty") + ") and value (" + dataPool.get("PropertyValue") + ") is added to the multi-selected assignments.", driver);

			//Step-4 : Save the metadatacard 
			//-------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.
			Utils.fluentWait(driver);

			metadatacard = new MetadataCard(driver, true); //Instantiates the metadatacard in rightpane

			Log.message("4. Metadatacard is saved after adding new property.");

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			if (metadatacard.propertyExists(dataPool.get("NewProperty")) && //Checks if new property and its value exists in metadatacard.
					metadatacard.getPropertyValue(dataPool.get("NewProperty")).equalsIgnoreCase(dataPool.get("PropertyValue")))
				Log.pass("Test case Passed. New property and its value is added successfully to multi-selected assignment object.");
			else
				Log.fail("Test case Failed. New property and its value is not added to multi-selected assignment object.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_34_3C

	/**
	 * 104.1.34.3D : User should be able to save multiselected (SHIFT key) assignment object with newly added property and value - Rightpane Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "User should be able to save multiselected (SHIFT key) assignment object with newly added property and value - Rightpane Properties")
	public void SprintTest104_1_34_3D(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			int noOfItems = Utility.getRandomNumber(3, 5);
			String[] assignName = new String[noOfItems];
			String objName = Utility.getObjectName(Utility.getMethodName()).toString();

			for (int i=0; i<noOfItems; i++) {
				homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
				assignName[i] =  objName + "_" + i; //Name of the object with current method date & time
				MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
				metadatacard = metadatacard.setTemplate("Blank");//Sets Blank template if template dialog exists in the view
				metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
				metadatacard.setPropertyValue("Name or title", assignName[i]); //Sets the Assignment name
				Utils.fluentWait(driver);
				metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
				metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			}

			Log.message("1. New " + noOfItems + " Assignments are created.", driver);

			//Step-2 : Open the Properties dialog of the new assignments in context menu
			//------------------------------------------------------------------------------
			homePage.searchPanel.search(objName, Caption.Search.SearchOnlyAssignments.Value);
			homePage.listView.shiftclickMultipleItemsByIndex(0, noOfItems-1); //Select multiple items by shift key

			MetadataCard metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("2. Properties dialog of multi selected assignments using SHIFT key is opened in rightpane.");

			//Step-3 : Add new property and value to the multi selected assignment objects
			//-----------------------------------------------------------------------------
			metadatacard.addNewProperty(dataPool.get("NewProperty"));
			metadatacard.setPropertyValue(dataPool.get("NewProperty"), dataPool.get("PropertyValue"));

			Log.message("3. New Property (" + dataPool.get("NewProperty") + ") and value (" + dataPool.get("PropertyValue") + ") is added to the multi-selected assignments.", driver);

			//Step-4 : Save the metadatacard 
			//-------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.
			Utils.fluentWait(driver);

			metadatacard = new MetadataCard(driver, true); //Instantiates the metadatacard in rightpane

			Log.message("4. Metadatacard is saved after adding new property.");

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			if (metadatacard.propertyExists(dataPool.get("NewProperty")) && //Checks if new property and its value exists in metadatacard.
					metadatacard.getPropertyValue(dataPool.get("NewProperty")).equalsIgnoreCase(dataPool.get("PropertyValue")))
				Log.pass("Test case Passed. New property and its value is added successfully to multi-selected assignment object.");
			else
				Log.fail("Test case Failed. New property and its value is not added to multi-selected assignment object.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_34_3D

	/**
	 * 104.1.34.4A : User should be able to save multiselected (CTRL key) assignment object with newly added property and value - Context menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "User should be able to save multiselected (CTRL key) assignment object with newly added property and value - Context menu Properties")
	public void SprintTest104_1_34_4A(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			int noOfItems = Utility.getRandomNumber(3, 5);
			String[] assignName = new String[noOfItems];
			String objName = Utility.getObjectName(Utility.getMethodName()).toString();

			for (int i=0; i<noOfItems; i++) {
				homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
				assignName[i] =  objName + "_" + i; //Name of the object with current method date & time
				MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
				metadatacard = metadatacard.setTemplate("Blank");//Sets Blank template if template dialog exists in the view
				metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
				metadatacard.setPropertyValue("Name or title", assignName[i]); //Sets the Assignment name
				Utils.fluentWait(driver);
				metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
				metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			}

			Log.message("1. New " + noOfItems + " Assignments are created.", driver);

			//Step-2 : Open the Properties dialog of the new assignments in context menu
			//------------------------------------------------------------------------------
			homePage.searchPanel.search(objName, Caption.Search.SearchOnlyAssignments.Value);
			homePage.listView.multiSelectRightClickByCtrlKey(noOfItems); //Select multiple items by ctrl key
			//homePage.listView.rightClickItem(assignName[noOfItems-1]);
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. Properties dialog of multi selected assignments using CTRL key is opened through context menu.");

			//Step-3 : Add new property and value to the multi selected assignment objects
			//-----------------------------------------------------------------------------
			metadatacard.addNewProperty(dataPool.get("NewProperty"));
			metadatacard.setPropertyValue(dataPool.get("NewProperty"), dataPool.get("PropertyValue"));

			Log.message("3. New Property (" + dataPool.get("NewProperty") + ") and value (" + dataPool.get("PropertyValue") + ") is added to the multi-selected assignments.", driver);

			//Step-4 : Save the metadatacard 
			//-------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.
			Utils.fluentWait(driver);

			metadatacard = new MetadataCard(driver, true); //Instantiates the metadatacard in rightpane

			Log.message("4. Metadatacard is saved after adding new property.");

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			if (metadatacard.propertyExists(dataPool.get("NewProperty")) && //Checks if new property and its value exists in metadatacard.
					metadatacard.getPropertyValue(dataPool.get("NewProperty")).equalsIgnoreCase(dataPool.get("PropertyValue")))
				Log.pass("Test case Passed. New property and its value is added successfully to multi-selected assignment object.");
			else
				Log.fail("Test case Failed. New property and its value is not added to multi-selected assignment object.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_34_4A

	/**
	 * 104.1.34.4B : User should be able to save multiselected (CTRL key) assignment object with newly added property and value - Operations menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "User should be able to save multiselected (CTRL key) assignment object with newly added property and value - Operations menu Properties")
	public void SprintTest104_1_34_4B(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			int noOfItems = Utility.getRandomNumber(3, 5);
			String[] assignName = new String[noOfItems];
			String objName = Utility.getObjectName(Utility.getMethodName()).toString();

			for (int i=0; i<noOfItems; i++) {
				homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
				assignName[i] =  objName + "_" + i; //Name of the object with current method date & time
				MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
				metadatacard = metadatacard.setTemplate("Blank");//Sets Blank template if template dialog exists in the view
				metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
				metadatacard.setPropertyValue("Name or title", assignName[i]); //Sets the Assignment name
				Utils.fluentWait(driver);
				metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
				metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			}

			Log.message("1. New " + noOfItems + " Assignments are created.", driver);

			//Step-2 : Open the Properties dialog of the new assignments in context menu
			//------------------------------------------------------------------------------
			homePage.searchPanel.search(objName, Caption.Search.SearchOnlyAssignments.Value);
			homePage.listView.clickMultipleItems(assignName); //Select multiple items by ctrl key
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from operations menu

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. Properties dialog of multi selected assignments using CTRL key is opened through operations menu.");

			//Step-3 : Add new property and value to the multi selected assignment objects
			//-----------------------------------------------------------------------------
			metadatacard.addNewProperty(dataPool.get("NewProperty"));
			metadatacard.setPropertyValue(dataPool.get("NewProperty"), dataPool.get("PropertyValue"));

			Log.message("3. New Property (" + dataPool.get("NewProperty") + ") and value (" + dataPool.get("PropertyValue") + ") is added to the multi-selected assignments.", driver);

			//Step-4 : Save the metadatacard 
			//-------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.
			Utils.fluentWait(driver);

			metadatacard = new MetadataCard(driver, true); //Instantiates the metadatacard in rightpane

			Log.message("4. Metadatacard is saved after adding new property.");

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			if (metadatacard.propertyExists(dataPool.get("NewProperty")) && //Checks if new property and its value exists in metadatacard.
					metadatacard.getPropertyValue(dataPool.get("NewProperty")).equalsIgnoreCase(dataPool.get("PropertyValue")))
				Log.pass("Test case Passed. New property and its value is added successfully to multi-selected assignment object.");
			else
				Log.fail("Test case Failed. New property and its value is not added to multi-selected assignment object.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_34_4B

	/**
	 * 104.1.34.4C : User should be able to save multiselected (CTRL key) assignment object with newly added property and value - Taskpanel Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "User should be able to save multiselected (CTRL key) assignment object with newly added property and value - Taskpanel Properties")
	public void SprintTest104_1_34_4C(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			int noOfItems = Utility.getRandomNumber(3, 5);
			String[] assignName = new String[noOfItems];
			String objName = Utility.getObjectName(Utility.getMethodName()).toString();

			for (int i=0; i<noOfItems; i++) {
				homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
				assignName[i] =  objName + "_" + i; //Name of the object with current method date & time
				MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
				metadatacard = metadatacard.setTemplate("Blank");//Sets Blank template if template dialog exists in the view
				metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
				metadatacard.setPropertyValue("Name or title", assignName[i]); //Sets the Assignment name
				Utils.fluentWait(driver);
				metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
				metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			}

			Log.message("1. New " + noOfItems + " Assignments are created.", driver);

			//Step-2 : Open the Properties dialog of the new assignments in context menu
			//------------------------------------------------------------------------------
			homePage.searchPanel.search(objName, Caption.Search.SearchOnlyAssignments.Value);
			homePage.listView.clickMultipleItems(assignName); //Select multiple items by ctrl key
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from taskpanel

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. Properties dialog of multi selected assignments using CTRL key is opened through taskpanel.");

			//Step-3 : Add new property and value to the multi selected assignment objects
			//-----------------------------------------------------------------------------
			metadatacard.addNewProperty(dataPool.get("NewProperty"));
			metadatacard.setPropertyValue(dataPool.get("NewProperty"), dataPool.get("PropertyValue"));

			Log.message("3. New Property (" + dataPool.get("NewProperty") + ") and value (" + dataPool.get("PropertyValue") + ") is added to the multi-selected assignments.", driver);

			//Step-4 : Save the metadatacard 
			//-------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.
			Utils.fluentWait(driver);

			metadatacard = new MetadataCard(driver, true); //Instantiates the metadatacard in rightpane

			Log.message("4. Metadatacard is saved after adding new property.");

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			if (metadatacard.propertyExists(dataPool.get("NewProperty")) && //Checks if new property and its value exists in metadatacard.
					metadatacard.getPropertyValue(dataPool.get("NewProperty")).equalsIgnoreCase(dataPool.get("PropertyValue")))
				Log.pass("Test case Passed. New property and its value is added successfully to multi-selected assignment object.");
			else
				Log.fail("Test case Failed. New property and its value is not added to multi-selected assignment object.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_34_4C

	/**
	 * 104.1.34.4D : User should be able to save multiselected (CTRL key) assignment object with newly added property and value - Rightpane Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "User should be able to save multiselected (CTRL key) assignment object with newly added property and value - Rightpane Properties")
	public void SprintTest104_1_34_4D(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			int noOfItems = Utility.getRandomNumber(3, 5);
			String[] assignName = new String[noOfItems];
			String objName = Utility.getObjectName(Utility.getMethodName()).toString();

			for (int i=0; i<noOfItems; i++) {
				homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
				assignName[i] =  objName + "_" + i; //Name of the object with current method date & time
				MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
				metadatacard = metadatacard.setTemplate("Blank");//Sets Blank template if template dialog exists in the view
				metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
				metadatacard.setPropertyValue("Name or title", assignName[i]); //Sets the Assignment name
				Utils.fluentWait(driver);
				metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
				metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			}

			Log.message("1. New " + noOfItems + " Assignments are created.", driver);

			//Step-2 : Open the Properties dialog of the new assignments in context menu
			//------------------------------------------------------------------------------
			homePage.searchPanel.search(objName, Caption.Search.SearchOnlyAssignments.Value);
			homePage.listView.clickMultipleItems(assignName); //Select multiple items by ctrl key

			MetadataCard metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("2. Properties dialog of multi selected assignments using CTRL key is opened in rightpane.");

			//Step-3 : Add new property and value to the multi selected assignment objects
			//-----------------------------------------------------------------------------
			metadatacard.addNewProperty(dataPool.get("NewProperty"));
			metadatacard.setPropertyValue(dataPool.get("NewProperty"), dataPool.get("PropertyValue"));

			Log.message("3. New Property (" + dataPool.get("NewProperty") + ") and value (" + dataPool.get("PropertyValue") + ") is added to the multi-selected assignments.", driver);

			//Step-4 : Save the metadatacard 
			//-------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.
			Utils.fluentWait(driver);

			metadatacard = new MetadataCard(driver, true); //Instantiates the metadatacard in rightpane

			Log.message("4. Metadatacard is saved after adding new property.");

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			if (metadatacard.propertyExists(dataPool.get("NewProperty")) && //Checks if new property and its value exists in metadatacard.
					metadatacard.getPropertyValue(dataPool.get("NewProperty")).equalsIgnoreCase(dataPool.get("PropertyValue")))
				Log.pass("Test case Passed. New property and its value is added successfully to multi-selected assignment object.");
			else
				Log.fail("Test case Failed. New property and its value is not added to multi-selected assignment object.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_34_4D

} //End Class Assignments
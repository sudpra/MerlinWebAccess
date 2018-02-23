package MFClient.Tests.Assignments;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
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
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class BasicApproveReject {

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
	 * 104.1.24.1A : Not Approved and Not Rejected buttons should be displayed after selecting assigned to user in metadatacard - Context menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Not Approved and Not Rejected buttons should be displayed after selecting assigned to user in metadatacard - Context menu Properties")
	public void SprintTest104_1_24_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select New Assignment from New menu
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(Utility.getMethodName()).toString(); //Name of the object with current method date & time
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment details are entered and saved the metadatacard.", driver);

			//Step-3 : Open the Properties dialog of the new assignment through context menu
			//------------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Taskpanel.AssignedToMe.Value, ""); //Navigates to Assigned to me view

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in Assigned to me view.");

			if (!homePage.listView.rightClickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of the new assignment (" + assigName + ") is opened through context menu.");

			//Verification : Verifies if Assignment is mark completed
			//----------------------------------------------------------
			if (metadatacard.isNotApprovedDisplayed() && metadatacard.isNotRejectedDisplayed()) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Not Approved and Not Rejected buttons are displayed in metadatacard..");
			else
				Log.fail("Test case Failed. Not Approved or Not Rejected buttons is not displayed in metadatacard..", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_24_1A

	/**
	 * 104.1.24.1B : Not Approved and Not Rejected buttons should be displayed after selecting assigned to user in metadatacard - Operations menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Not Approved and Not Rejected buttons should be displayed after selecting assigned to user in metadatacard - Operations menu Properties")
	public void SprintTest104_1_24_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select New Assignment from New menu
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(Utility.getMethodName()).toString(); //Name of the object with current method date & time
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment details are entered and saved the metadatacard.", driver);

			//Step-3 : Open the Properties dialog of the new assignment through operations menu
			//------------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of the new assignment (" + assigName + ") is opened through operations menu.");

			//Verification : Verifies if Not Approved and Not Rejected button gets displayed
			//------------------------------------------------------------------------------
			if (metadatacard.isNotApprovedDisplayed() && metadatacard.isNotRejectedDisplayed()) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Not Approved and Not Rejected buttons are displayed in metadatacard..");
			else
				Log.fail("Test case Failed. Not Approved or Not Rejected buttons is not displayed in metadatacard..", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_24_1B

	/**
	 * 104.1.24.1C : Not Approved and Not Rejected buttons should be displayed after selecting assigned to user in metadatacard - Taskpane Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Not Approved and Not Rejected buttons should be displayed after selecting assigned to user in metadatacard - Taskpane Properties")
	public void SprintTest104_1_24_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select New Assignment from New menu
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(Utility.getMethodName()).toString(); //Name of the object with current method date & time
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment details are entered and saved the metadatacard.", driver);

			//Step-3 : Open the Properties dialog of the new assignment through Taskpane
			//------------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of the new assignment (" + assigName + ") is opened through taskpane.");

			//Verification : Verifies if Not Approved and Not Rejected button gets displayed
			//------------------------------------------------------------------------------
			if (metadatacard.isNotApprovedDisplayed() && metadatacard.isNotRejectedDisplayed()) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Not Approved and Not Rejected buttons are displayed in metadatacard..");
			else
				Log.fail("Test case Failed. Not Approved or Not Rejected buttons is not displayed in metadatacard..", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_24_1C

	/**
	 * 104.1.24.1D : Not Approved and Not Rejected buttons should be displayed after selecting assigned to user in metadatacard - Rightpane Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Not Approved and Not Rejected buttons should be displayed after selecting assigned to user in metadatacard - Rightpane Properties")
	public void SprintTest104_1_24_1D(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select New Assignment from New menu
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(Utility.getMethodName()).toString(); //Name of the object with current method date & time
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment details are entered and saved the metadatacard.", driver);

			//Step-3 : Open the Properties dialog of the new assignment through Taskpane
			//------------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of the new assignment (" + assigName + ") is opened through rightpane.");

			//Verification : Verifies if Not Approved and Not Rejected button gets displayed
			//------------------------------------------------------------------------------
			if (metadatacard.isNotApprovedDisplayed() && metadatacard.isNotRejectedDisplayed()) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Not Approved and Not Rejected buttons are displayed in metadatacard..");
			else
				Log.fail("Test case Failed. Not Approved or Not Rejected buttons is not displayed in metadatacard..", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_24_1D

	/**
	 * 104.1.24.1E : Not Approved and Not Rejected buttons should be available in taskpanel after selecting assignment from listview.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Not Approved and Not Rejected buttons should be available in taskpanel after selecting assignment from listview.")
	public void SprintTest104_1_24_1E(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select New Assignment from New menu
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(Utility.getMethodName()).toString(); //Name of the object with current method date & time
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment details are entered and saved the metadatacard.", driver);

			//Step-3 : Open the Properties dialog of the new assignment through Taskpane
			//------------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of the new assignment (" + assigName + ") is opened through rightpane.");

			//Verification : Verifies if Not Approved and Not Rejected button gets displayed
			//------------------------------------------------------------------------------
			if (metadatacard.isNotApprovedDisplayed() && metadatacard.isNotRejectedDisplayed()) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Not Approved and Not Rejected buttons are displayed in metadatacard..");
			else
				Log.fail("Test case Failed. Not Approved or Not Rejected buttons is not displayed in metadatacard..", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_24_1E

	/**
	 * 104.1.24.5A : Approve and Reject icon should be available for all the assigned users - Context menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Approve and Reject icon should be available for all the assigned users - Context menu Properties")
	public void SprintTest104_1_24_5A(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select New Assignment from New menu
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(Utility.getMethodName()).toString(); //Name of the object with current method date & time
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName1")); //Sets the assigned to property
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName2"), 2); //Sets the assigned to property
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName3"), 3); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment details are entered and saved the metadatacard.", driver);

			//Step-3 : Open the Properties dialog of the new assignment through context menu
			//------------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in Assigned to me view.");

			if (!homePage.listView.rightClickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of the new assignment (" + assigName + ") is opened through context menu.");

			//Verification : Verifies if Approve and Reject icon is available for all users
			//------------------------------------------------------------------------------
			Boolean isApproveDisplayed = metadatacard.isNotApprovedDisplayed() && metadatacard.isNotApprovedDisplayed(1) && metadatacard.isNotApprovedDisplayed(2); 
			Boolean isRejectDisplayed = metadatacard.isNotRejectedDisplayed() && metadatacard.isNotRejectedDisplayed(1) && metadatacard.isNotRejectedDisplayed(2);

			if (isApproveDisplayed && isRejectDisplayed) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Approved and Rejected icon is displayed for all users.");
			else
				Log.fail("Test case Failed. Approved and Rejected icon is not displayed for all users.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_24_5A

	/**
	 * 104.1.24.5B : Approve and Reject icon should be available for all the assigned users - Operations menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Approve and Reject icon should be available for all the assigned users - Operations menu Properties")
	public void SprintTest104_1_24_5B(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select New Assignment from New menu
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(Utility.getMethodName()).toString(); //Name of the object with current method date & time
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName1")); //Sets the assigned to property
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName2"), 2); //Sets the assigned to property
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName3"), 3); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment details are entered and saved the metadatacard.", driver);

			//Step-3 : Open the Properties dialog of the new assignment through oeprations menu
			//------------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in Assigned to me view.");

			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from operations menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of the new assignment (" + assigName + ") is opened through operations menu.");

			//Verification : Verifies if Approve and Reject icon is available for all users
			//------------------------------------------------------------------------------
			Boolean isApproveDisplayed = metadatacard.isNotApprovedDisplayed() && metadatacard.isNotApprovedDisplayed(1) && metadatacard.isNotApprovedDisplayed(2); 
			Boolean isRejectDisplayed = metadatacard.isNotRejectedDisplayed() && metadatacard.isNotRejectedDisplayed(1) && metadatacard.isNotRejectedDisplayed(2);

			if (isApproveDisplayed && isRejectDisplayed) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Approved and Rejected icon is displayed for all users.");
			else
				Log.fail("Test case Failed. Approved and Rejected icon is not displayed for all users.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_24_5B

	/**
	 * 104.1.24.5C : Approve and Reject icon should be available for all the assigned users - Taskpane Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Approve and Reject icon should be available for all the assigned users - Taskpane Properties")
	public void SprintTest104_1_24_5C(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select New Assignment from New menu
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(Utility.getMethodName()).toString(); //Name of the object with current method date & time
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName1")); //Sets the assigned to property
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName2"), 2); //Sets the assigned to property
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName3"), 3); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment details are entered and saved the metadatacard.", driver);

			//Step-3 : Open the Properties dialog of the new assignment through taskpanel
			//------------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in Assigned to me view.");

			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from taskpanel

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of the new assignment (" + assigName + ") is opened through taskpanel.");

			//Verification : Verifies if Approve and Reject icon is available for all users
			//------------------------------------------------------------------------------
			Boolean isApproveDisplayed = metadatacard.isNotApprovedDisplayed() && metadatacard.isNotApprovedDisplayed(1) && metadatacard.isNotApprovedDisplayed(2); 
			Boolean isRejectDisplayed = metadatacard.isNotRejectedDisplayed() && metadatacard.isNotRejectedDisplayed(1) && metadatacard.isNotRejectedDisplayed(2);

			if (isApproveDisplayed && isRejectDisplayed) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Approved and Rejected icon is displayed for all users.");
			else
				Log.fail("Test case Failed. Approved and Rejected icon is not displayed for all users.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_24_5C

	/**
	 * 104.1.24.5D : Approve and Reject icon should be available for all the assigned users - Rightpane Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Approve and Reject icon should be available for all the assigned users - Rightpane Properties")
	public void SprintTest104_1_24_5D(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select New Assignment from New menu
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(Utility.getMethodName()).toString(); //Name of the object with current method date & time
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName1")); //Sets the assigned to property
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName2"), 2); //Sets the assigned to property
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName3"), 3); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment details are entered and saved the metadatacard.", driver);

			//Step-3 : Open the Properties dialog of the new assignment through taskpanel
			//------------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in Assigned to me view.");

			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from taskpanel

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of the new assignment (" + assigName + ") is opened through taskpanel.");

			//Verification : Verifies if Approve and Reject icon is available for all users
			//------------------------------------------------------------------------------
			Boolean isApproveDisplayed = metadatacard.isNotApprovedDisplayed() && metadatacard.isNotApprovedDisplayed(1) && metadatacard.isNotApprovedDisplayed(2); 
			Boolean isRejectDisplayed = metadatacard.isNotRejectedDisplayed() && metadatacard.isNotRejectedDisplayed(1) && metadatacard.isNotRejectedDisplayed(2);

			if (isApproveDisplayed && isRejectDisplayed) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Approved and Rejected icon is displayed for all users.");
			else
				Log.fail("Test case Failed. Approved and Rejected icon is not displayed for all users.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_24_5D

	/**
	 * SprintTest_38134 : Verify if Mark approved and reject icon is displayed in task pane when login with the assigned to user credentials.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Verify if Mark approved and reject icon is displayed in task pane when login with the assigned to user credentials.")
	public void SprintTest_38134(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {
			//Logged into MFWA with valid credentials
			//---------------------------------------


			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Created the new assignment object from the menu bar
			//------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);

			Log.message("1. Created the new Assignment object from the menu bar.", driver);

			//Step-2 : Set the metadata property value & save the metadatacard
			//-----------------------------------------------------------------
			String assigName =  Utility.getObjectName(Utility.getMethodName()).toString(); //Name of the object with current method date & time
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName1")); //Sets the assigned to property
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName2"), 2); //Sets the assigned to property
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName3"), 3); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment details are entered and saved the metadatacard.", driver);			


			//Step-3 : Logout and login with new user
			//--------------------------------------
			if (!Utility.logOut(driver)) //Logs out from the vault
				throw new Exception("Logout is not successful");

			//Login with new user and password
			//--------------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), testVault); //Launched driver and logged in

			Log.message("3. Logged out and logged in with new user (" + dataPool.get("UserFullName2") + ").");

			//Step-4 : Open the Properties dialog of the new assignment through context menu
			//------------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Taskpanel.AssignedToMe.Value, ""); //Navigates to Assigned to me view

			Log.message("4. Navigate to " + Caption.Taskpanel.AssignedToMe.Value + " view.");

			//Verify if item exists in the navigate specified view
			//----------------------------------------------------
			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in Assigned to me view.");

			//Step-5 : Select the newly created assignment 
			//----------------------------------------------
			homePage.listView.clickItem(assigName);//Selected the created assignment

			Log.message("5. Selected the newly created assignment : " + assigName);

			//Verification : Verify if mark approved and mark rejected is displayed in task pane
			//----------------------------------------------------------------------------------
			if((homePage.taskPanel.isItemExists(Caption.MenuItems.MarkApproved.Value))&& (homePage.taskPanel.isItemExists(Caption.MenuItems.MarkRejected.Value)))
				Log.pass("Test Case Passed.Mark approved and rejected is displayed in task pane.");
			else
				Log.fail("Test Case Failed.Mark approved and rejected is not displayed in task pane.", driver);


		}//End try
		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally { 
			Utility.quitDriver(driver);
		}//End finally
	}//End SprintTest_38134

} //End Class Assignments
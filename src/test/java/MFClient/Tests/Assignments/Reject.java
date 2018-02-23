package MFClient.Tests.Assignments;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.Utils;
import genericLibrary.WebDriverUtils;

import java.lang.reflect.Method;
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
public class Reject {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String configURL = null;
	public static String userName = null;
	public static String password = null;
	public static String testVault = null;
	public static String productVersion = null;
	public static WebDriver driver = null;
	public static String userFullName = null;
	public String methodName = null;
	public static String className = null;

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
	 * 104.1.24.3A : Create assignment by assigning it to other user and Reject it by assigned user in metadatacard - context menu properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Create assignment by assigning it to other user and Reject it by assigned user in metadatacard - context menu properties")
	public void SprintTest104_1_24_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");


			//Step-2 : Select New Assignment from New menu
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName).toString();
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. New Assignment metadatacard is opened from New menu.");

			//Step-3 : Add new property with empty value to metadatacard
			//----------------------------------------------------------
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName")); //Sets the assigned to property

			if (!metadatacard.isNotApprovedDisplayed() || !metadatacard.isNotRejectedDisplayed())
				throw new Exception("Not Approved or Not Rejected icon is not displayed in metadata after selecting assigned to user.");

			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Assignment details are entered and saved the metadatacard.", driver);

			//Step-3 : Logout and login with new user
			//--------------------------------------
			if (!Utility.logOut(driver)) //Logs out from the vault
				throw new Exception("Logout is not successful");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), testVault); //Launched driver and logged in

			Log.message("4. Logged out and logged in with new user (" + dataPool.get("UserName") + ").");

			//Step-4 : Open the Properties dialog of the new assignment through context menu
			//------------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Taskpanel.AssignedToMe.Value, ""); //Navigates to Assigned to me view

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in Assigned to me view.");

			if (!homePage.listView.rightClickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("5. Properties dialog of the new assignment (" + assigName + ") is opened through context menu.");

			//Step-5 : Click Reject button in the metadatacard
			//-------------------------------------------------
			if (!metadatacard.clickRejectIcon())
				throw new Exception("Reject Icon is not clicked.");

			Log.message("6. Reject icon in the Assignment is clicked.");

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (metadatacard.isSelectedObjectRejectedByUser(dataPool.get("UserFullName"))) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment is rejected in metadatacard properties.");
			else
				Log.fail("Test case Failed. Assignment is not rejected in metadatacard properties.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_24_3A

	/**
	 * 104.1.24.3B : Create assignment by assigning it to other user and Reject it by assigned user in metadatacard - context menu properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Create assignment by assigning it to other user and Reject it by assigned user in metadatacard - context menu properties")
	public void SprintTest104_1_24_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select New Assignment from New menu
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName).toString();
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. New Assignment metadatacard is opened from New menu.");

			//Step-3 : Add new property with empty value to metadatacard
			//----------------------------------------------------------
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName")); //Sets the assigned to property

			if (!metadatacard.isNotApprovedDisplayed() || !metadatacard.isNotRejectedDisplayed())
				throw new Exception("Not Approved or Not Rejected icon is not displayed in metadata after selecting assigned to user.");

			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Assignment details are entered and saved the metadatacard.", driver);

			//Step-3 : Logout and login with new user
			//--------------------------------------
			if (!Utility.logOut(driver)) //Logs out from the vault
				throw new Exception("Logout is not successful");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), testVault); //Launched driver and logged in

			Log.message("4. Logged out and logged in with new user (" + dataPool.get("UserName") + ").");

			//Step-4 : Open the Properties dialog of the new assignment through operations menu
			//------------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Taskpanel.AssignedToMe.Value, ""); //Navigates to Assigned to me view

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in Assigned to me view.");

			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of the new assignment (" + assigName + ") is opened through operations menu.");

			//Step-5 : Click Reject button in the metadatacard
			//-------------------------------------------------
			if (!metadatacard.clickRejectIcon())
				throw new Exception("Reject Icon is not clicked.");

			Log.message("5. Reject icon in the Assignment is clicked.");

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (metadatacard.isSelectedObjectRejectedByUser(dataPool.get("UserFullName"))) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment is rejected in metadatacard properties.");
			else
				Log.fail("Test case Failed. Assignment is not rejected in metadatacard properties.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_24_3B

	/**
	 * 104.1.24.3C : Create assignment by assigning it to other user and Reject it by assigned user in metadatacard - taskpane properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Create assignment by assigning it to other user and Reject it by assigned user in metadatacard - taskpane properties")
	public void SprintTest104_1_24_3C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select New Assignment from New menu
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName).toString();
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. New Assignment metadatacard is opened from New menu.");

			//Step-3 : Add new property with empty value to metadatacard
			//----------------------------------------------------------
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName")); //Sets the assigned to property

			if (!metadatacard.isNotApprovedDisplayed() || !metadatacard.isNotRejectedDisplayed())
				throw new Exception("Not Approved or Not Rejected icon is not displayed in metadata after selecting assigned to user.");

			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Assignment details are entered and saved the metadatacard.", driver);

			//Step-3 : Logout and login with new user
			//--------------------------------------
			if (!Utility.logOut(driver)) //Logs out from the vault
				throw new Exception("Logout is not successful");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), testVault); //Launched driver and logged in

			Log.message("4. Logged out and logged in with new user (" + dataPool.get("UserName") + ").");

			//Step-4 : Open the Properties dialog of the new assignment through taskpane
			//------------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Taskpanel.AssignedToMe.Value, ""); //Navigates to Assigned to me view

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in Assigned to me view.");

			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of the new assignment (" + assigName + ") is opened through taskpanel.");

			//Step-5 : Click Reject button in the metadatacard
			//-------------------------------------------------
			if (!metadatacard.clickRejectIcon())
				throw new Exception("Reject Icon is not clicked.");

			Log.message("5. Reject icon in the Assignment is clicked.");

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (metadatacard.isSelectedObjectRejectedByUser(dataPool.get("UserFullName"))) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment is rejected in metadatacard properties.");
			else
				Log.fail("Test case Failed. Assignment is not rejected in metadatacard properties.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_24_3C

	/**
	 * 104.1.24.3D : Create assignment by assigning it to other user and Reject it by assigned user in metadatacard - rightpane properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Create assignment by assigning it to other user and Reject it by assigned user in metadatacard - rightpane properties")
	public void SprintTest104_1_24_3D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select New Assignment from New menu
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName).toString();
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. New Assignment metadatacard is opened from New menu.");

			//Step-3 : Add new property with empty value to metadatacard
			//----------------------------------------------------------
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName")); //Sets the assigned to property

			if (!metadatacard.isNotApprovedDisplayed() || !metadatacard.isNotRejectedDisplayed())
				throw new Exception("Not Approved or Not Rejected icon is not displayed in metadata after selecting assigned to user.");

			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Assignment details are entered and saved the metadatacard.", driver);

			//Step-3 : Logout and login with new user
			//--------------------------------------
			if (!Utility.logOut(driver)) //Logs out from the vault
				throw new Exception("Logout is not successful");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), testVault); //Launched driver and logged in

			Log.message("4. Logged out and logged in with new user (" + dataPool.get("UserName") + ").");

			//Step-4 : Open the Properties dialog of the new assignment through rightpane
			//------------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Taskpanel.AssignedToMe.Value, ""); //Navigates to Assigned to me view

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in Assigned to me view.");

			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("5. Properties dialog of the new assignment (" + assigName + ") is opened through rightpane.");

			//Step-5 : Click Reject button in the metadatacard
			//-------------------------------------------------
			if (!metadatacard.clickRejectIcon())
				throw new Exception("Reject Icon is not clicked.");

			Log.message("6. Reject icon in the Assignment is clicked.");

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (metadatacard.isSelectedObjectRejectedByUser(dataPool.get("UserFullName"))) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment is rejected in metadatacard properties.");
			else
				Log.fail("Test case Failed. Assignment is not rejected in metadatacard properties.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_24_3D

	/**
	 * 104.1.24.3E : Create assignment by assigning it to other user and Reject it by assigned user from taskpane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Create assignment by assigning it to other user and Reject it by assigned user from taskpane")
	public void SprintTest104_1_24_3E(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select New Assignment from New menu
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName).toString();
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. New Assignment metadatacard is opened from New menu.");

			//Step-3 : Add new property with empty value to metadatacard
			//----------------------------------------------------------
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName")); //Sets the assigned to property

			if (!metadatacard.isNotApprovedDisplayed() || !metadatacard.isNotRejectedDisplayed())
				throw new Exception("Not Approved or Not Rejected icon is not displayed in metadata after selecting assigned to user.");

			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Assignment details are entered and saved the metadatacard.", driver);

			//Step-3 : Logout and login with new user
			//--------------------------------------
			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in view.");

			if (!Utility.logOut(driver)) //Logs out from the vault
				throw new Exception("Logout is not successful");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), testVault); //Launched driver and logged in

			Log.message("4. Logged out and logged in with new user (" + dataPool.get("UserName") + ").");

			//Step-4 : Open the Properties dialog of the new assignment through rightpane
			//------------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Taskpanel.AssignedToMe.Value, ""); //Navigates to Assigned to me view

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in Assigned to me view.");

			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.markApproveReject("Reject"); //Selects Properties from context menu

			Log.message("5. Properties dialog of the new assignment (" + assigName + ") is opened through rightpane.");

			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyAssignments.Value, ""); //Navigates to Assigned to me view

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			if (ListView.getRejectedByItemName(driver, assigName).equalsIgnoreCase(dataPool.get("UserFullName"))) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment is rejected in metadatacard properties.");
			else
				Log.fail("Test case Failed. Assignment is not rejected in metadatacard properties.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_24_3E

	/**
	 * 104.1.32.2A : Verify if Reject option is removed in task pane when user perform Reject operation - Context meu properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Verify if Reject option is removed in task pane when user perform Reject operation - Context meu properties")
	public void SprintTest104_1_32_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select New Assignment from New menu
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName).toString();
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. New Assignment metadatacard is opened from New menu.");

			//Step-3 : Add new property with empty value to metadatacard
			//----------------------------------------------------------
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property

			if (!metadatacard.isNotApprovedDisplayed() || !metadatacard.isNotRejectedDisplayed())
				throw new Exception("Not Approved or Not Rejected icon is not displayed in metadata after selecting assigned to user.");

			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Assignment details are entered and saved the metadatacard.", driver);

			//Step-4 : Open the Properties dialog of the new assignment through context menu
			//------------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Taskpanel.AssignedToMe.Value, ""); //Navigates to Assigned to me view

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in Assigned to me view.");

			if (!homePage.listView.rightClickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of the new assignment (" + assigName + ") is opened through context menu.");

			//Step-5 : Reject the assignment in metadatacard
			//-------------------------------------------------
			if (!metadatacard.clickRejectIcon()) //Clicks Approve icon in metadatacard
				throw new Exception("Reject Icon is not clicked.");

			metadatacard.saveAndClose(); //Saves and closes the metadatacard.
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (!metadatacard.isSelectedObjectRejectedByUser(userFullName)) //Checks if Assignment completed icon is displayed
				throw new Exception("Assignment is not rejected in metadatacard properties.");

			Log.message("5. Assignment is rejected in metadatacard properties.");

			//Verification : To verify if Approve is not displayed in taskpanel
			//--------------------------------------------------------------------
			if (!homePage.taskPanel.isItemExists(Caption.MenuItems.Reject.Value))
				Log.pass("Test case Passed. Reject is not displayed in taskpane after rejecting the assignment.");
			else
				Log.fail("Test case Failed. Reject is getting displayed after rejecting the assignment.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_32_2A

	/**
	 * 104.1.32.2B : Verify if Reject option is removed in task pane when user perform Reject operation - Operations meu properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Verify if Reject option is removed in task pane when user perform Reject operation - Operations meu properties")
	public void SprintTest104_1_32_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select New Assignment from New menu
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName).toString();
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. New Assignment metadatacard is opened from New menu.");

			//Step-3 : Add new property with empty value to metadatacard
			//----------------------------------------------------------
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property

			if (!metadatacard.isNotApprovedDisplayed() || !metadatacard.isNotRejectedDisplayed())
				throw new Exception("Not Approved or Not Rejected icon is not displayed in metadata after selecting assigned to user.");

			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Assignment details are entered and saved the metadatacard.", driver);

			//Step-4 : Open the Properties dialog of the new assignment through operations menu
			//---------------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Taskpanel.AssignedToMe.Value, ""); //Navigates to Assigned to me view

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in Assigned to me view.");

			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of the new assignment (" + assigName + ") is opened through operations menu.");

			//Step-5 : Reject the assignment in metadatacard
			//-------------------------------------------------
			if (!metadatacard.clickRejectIcon()) //Clicks Approve icon in metadatacard
				throw new Exception("Reject Icon is not clicked.");

			metadatacard.saveAndClose(); //Saves and closes the metadatacard.
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (!metadatacard.isSelectedObjectRejectedByUser(userFullName)) //Checks if Assignment completed icon is displayed
				throw new Exception("Assignment is not rejected in metadatacard properties.");

			Log.message("5. Assignment is rejected in metadatacard properties.");

			//Verification : To verify if Approve is not displayed in taskpanel
			//--------------------------------------------------------------------
			if (!homePage.taskPanel.isItemExists(Caption.MenuItems.Reject.Value))
				Log.pass("Test case Passed. Reject is not displayed in taskpane after rejecting the assignment.");
			else
				Log.fail("Test case Failed. Reject is getting displayed after rejecting the assignment.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_32_2B

	/**
	 * 104.1.32.2C : Verify if Reject option is removed in task pane when user perform Reject operation - Taskpane properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Verify if Reject option is removed in task pane when user perform Reject operation - Taskpane properties")
	public void SprintTest104_1_32_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select New Assignment from New menu
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName).toString();
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. New Assignment metadatacard is opened from New menu.");

			//Step-3 : Add new property with empty value to metadatacard
			//----------------------------------------------------------
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property

			if (!metadatacard.isNotApprovedDisplayed() || !metadatacard.isNotRejectedDisplayed())
				throw new Exception("Not Approved or Not Rejected icon is not displayed in metadata after selecting assigned to user.");

			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Assignment details are entered and saved the metadatacard.", driver);

			//Step-4 : Open the Properties dialog of the new assignment through taskpane
			//---------------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Taskpanel.AssignedToMe.Value, ""); //Navigates to Assigned to me view

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in Assigned to me view.");

			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from taskpane

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of the new assignment (" + assigName + ") is opened through taskpane.");

			//Step-5 : Reject the assignment in metadatacard
			//-------------------------------------------------
			if (!metadatacard.clickRejectIcon()) //Clicks Approve icon in metadatacard
				throw new Exception("Reject Icon is not clicked.");

			metadatacard.saveAndClose(); //Saves and closes the metadatacard.
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (!metadatacard.isSelectedObjectRejectedByUser(userFullName)) //Checks if Assignment completed icon is displayed
				throw new Exception("Assignment is not rejected in metadatacard properties.");

			Log.message("5. Assignment is rejected in metadatacard properties.");

			//Verification : To verify if Approve is not displayed in taskpanel
			//--------------------------------------------------------------------
			if (!homePage.taskPanel.isItemExists(Caption.MenuItems.Reject.Value))
				Log.pass("Test case Passed. Reject is not displayed in taskpane after rejecting the assignment.");
			else
				Log.fail("Test case Failed. Reject is getting displayed after rejecting the assignment.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_32_2C

	/**
	 * 104.1.32.2D : Verify if Reject option is removed in task pane when user perform Reject operation - Rightpane properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Verify if Reject option is removed in task pane when user perform Reject operation - Rightpane properties")
	public void SprintTest104_1_32_2D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select New Assignment from New menu
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName).toString();
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. New Assignment metadatacard is opened from New menu.");

			//Step-3 : Add new property with empty value to metadatacard
			//----------------------------------------------------------
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property

			if (!metadatacard.isNotApprovedDisplayed() || !metadatacard.isNotRejectedDisplayed())
				throw new Exception("Not Approved or Not Rejected icon is not displayed in metadata after selecting assigned to user.");

			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Assignment details are entered and saved the metadatacard.", driver);

			//Step-4 : Open the Properties dialog of the new assignment through rightpane
			//---------------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Taskpanel.AssignedToMe.Value, ""); //Navigates to Assigned to me view

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in Assigned to me view.");

			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");


			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of the new assignment (" + assigName + ") is opened in rightpane.");

			//Step-5 : Reject the assignment in metadatacard
			//-------------------------------------------------
			if (!metadatacard.clickRejectIcon()) //Clicks Approve icon in metadatacard
				throw new Exception("Reject Icon is not clicked.");

			metadatacard.saveAndClose(); //Saves and closes the metadatacard.
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (!metadatacard.isSelectedObjectRejectedByUser(userFullName)) //Checks if Assignment completed icon is displayed
				throw new Exception("Assignment is not rejected in metadatacard properties.");

			Log.message("5. Assignment is rejected in metadatacard properties.");

			//Verification : To verify if Approve is not displayed in taskpanel
			//--------------------------------------------------------------------
			if (!homePage.taskPanel.isItemExists(Caption.MenuItems.Reject.Value))
				Log.pass("Test case Passed. Reject is not displayed in taskpane after rejecting the assignment.");
			else
				Log.fail("Test case Failed. Reject is getting displayed after rejecting the assignment.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_32_2D

	/**
	 * 104.1.32.2E : Verify if Approve option is removed in task pane after approving assignment through taskpanel.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Verify if Reject option is removed in task pane when user perform Reject operation - Rightpane properties")
	public void SprintTest104_1_32_2E(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select New Assignment from New menu
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName).toString();
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. New Assignment metadatacard is opened from New menu.");

			//Step-3 : Add new property with empty value to metadatacard
			//----------------------------------------------------------
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property

			if (!metadatacard.isNotApprovedDisplayed() || !metadatacard.isNotRejectedDisplayed())
				throw new Exception("Not Approved or Not Rejected icon is not displayed in metadata after selecting assigned to user.");

			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Assignment details are entered and saved the metadatacard.", driver);

			//Step-4 : Approve the assignment in taskapanel
			//-------------------------------------------------
			homePage.taskPanel.markApproveReject("Reject"); //Selects Reject from taskpanel//Clicks Approve from taskpanel
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (!metadatacard.isSelectedObjectRejectedByUser(userFullName)) //Checks if Assignment completed icon is displayed
				throw new Exception("Assignment is not rejected in metadatacard properties.");

			Log.message("4. Assignment is rejected from taskpanel.");

			//Verification : To verify if Approve is not displayed in taskpanel
			//--------------------------------------------------------------------
			if (!homePage.taskPanel.isItemExists(Caption.MenuItems.Reject.Value))
				Log.pass("Test case Passed. Reject is not displayed in taskpane after rejecting the assignment.");
			else
				Log.fail("Test case Failed. Reject is getting displayed after rejecting the assignment.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_32_2E

	/**
	 * 104.1.52A : Adding comments should be possible in metadatacard for single assignment opened through context menu after rejecting the assignment
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Adding comments should be possible in metadatacard for single assignment opened through context menu after rejecting the assignment")
	public void SprintTest104_1_52A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName);
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment (" + assigName + ") is created.", driver);

			//Step-3 : Open the Properties dialog of the assignment through context menu
			//---------------------------------------------------------------------------
			homePage.listView.rightClickItem(assigName);
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of an assignment is opened through context menu.");

			//Step-4 : Select Reject icon for the assignment
			//-----------------------------------------------
			if (!metadatacard.clickRejectIcon())
				throw new Exception("Reject icon is not selected.");

			Log.message("4. Reject icon is selected");

			//Step-5 : Add comments to the assignment after selecting Reject icon
			//--------------------------------------------------------------------
			String comments = Utility.getObjectName(methodName);
			metadatacard.setComments(comments);

			Log.message("5. Comments is entered assignment after selecting Reject icon.", driver);

			//Step-6 : Save the assignment object
			//-----------------------------------
			metadatacard.saveAndClose(); 

			Log.message("6. Metadatacard is saved after selecting Reject icon and entering comments.");

			//Verification : Verify if assignment is Rejected and comments are as entered
			//-----------------------------------------------------------------------------
			if (!ListView.isRejectedByItemName(driver, assigName)) //Verifies if assignment is Rejected
				throw new Exception("Test case Failed. Assignment is not Rejected after saving the changes to metadatacard.");

			if (ListView.getCommentsByItemName(driver, assigName).equalsIgnoreCase(comments)) //Verifies if comments are as entered
				Log.pass("Test case Passed. Comments entered in metadatacard opened through context menu after clicking Reject icon is saved successfully.");
			else
				Log.fail("Test case Failed. Comments entered in metadatacard opened through context menu after clicking Reject icon is not saved.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_52A

	/**
	 * 104.1.52B : Adding comments should be possible in metadatacard for single assignment opened through operations menu after rejecting the assignment
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Adding comments should be possible in metadatacard for single assignment opened through operations menu after rejecting the assignment")
	public void SprintTest104_1_52B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName);
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment (" + assigName + ") is created.", driver);

			//Step-3 : Open the Properties dialog of the assignment through operations menu
			//---------------------------------------------------------------------------
			homePage.listView.clickItem(assigName);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from operations menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of an assignment is opened through operations menu.");

			//Step-4 : Select Reject icon for the assignment
			//-----------------------------------------------
			if (!metadatacard.clickRejectIcon())
				throw new Exception("Reject icon is not selected.");

			Log.message("4. Reject icon is selected");

			//Step-5 : Add comments to the assignment after selecting Reject icon
			//--------------------------------------------------------------------
			String comments = Utility.getObjectName(methodName);
			metadatacard.setComments(comments);

			Log.message("5. Comments is entered assignment after selecting Reject icon.", driver);

			//Step-6 : Save the assignment object
			//-----------------------------------
			metadatacard.saveAndClose(); 

			Log.message("6. Metadatacard is saved after selecting Reject icon and entering comments.");

			//Verification : Verify if assignment is Rejected and comments are as entered
			//-----------------------------------------------------------------------------
			if (!ListView.isRejectedByItemName(driver, assigName)) //Verifies if assignment is Rejected
				throw new Exception("Test case Failed. Assignment is not Rejected after saving the changes to metadatacard.");

			if (ListView.getCommentsByItemName(driver, assigName).equalsIgnoreCase(comments)) //Verifies if comments are as entered
				Log.pass("Test case Passed. Comments entered in metadatacard opened through operations menu after clicking Reject icon is saved successfully.");
			else
				Log.fail("Test case Failed. Comments entered in metadatacard opened through operations menu after clicking Reject icon is not saved.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_52B

	/**
	 * 104.1.52C : Adding comments should be possible in metadatacard for single assignment opened through taskpanel menu after rejecting the assignment
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Adding comments should be possible in metadatacard for single assignment opened through taskpanel menu after rejecting the assignment")
	public void SprintTest104_1_52C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName);
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment (" + assigName + ") is created.", driver);

			//Step-3 : Open the Properties dialog of the assignment through taskpanel menu
			//---------------------------------------------------------------------------
			homePage.listView.clickItem(assigName);
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from taskpanel menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of an assignment is opened through taskpanel menu.");

			//Step-4 : Select Reject icon for the assignment
			//-----------------------------------------------
			if (!metadatacard.clickRejectIcon())
				throw new Exception("Reject icon is not selected.");

			Log.message("4. Reject icon is selected");

			//Step-5 : Add comments to the assignment after selecting Reject icon
			//--------------------------------------------------------------------
			String comments = Utility.getObjectName(methodName);
			metadatacard.setComments(comments);

			Log.message("5. Comments is entered assignment after selecting Reject icon.", driver);

			//Step-6 : Save the assignment object
			//-----------------------------------
			metadatacard.saveAndClose(); 

			Log.message("6. Metadatacard is saved after selecting Reject icon and entering comments.");

			//Verification : Verify if assignment is Rejected and comments are as entered
			//-----------------------------------------------------------------------------
			if (!ListView.isRejectedByItemName(driver, assigName)) //Verifies if assignment is Rejected
				throw new Exception("Test case Failed. Assignment is not Rejected after saving the changes to metadatacard.");

			if (ListView.getCommentsByItemName(driver, assigName).equalsIgnoreCase(comments)) //Verifies if comments are as entered
				Log.pass("Test case Passed. Comments entered in metadatacard opened through taskpanel menu after clicking Reject icon is saved successfully.");
			else
				Log.fail("Test case Failed. Comments entered in metadatacard opened through taskpanel menu after clicking Reject icon is not saved.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_52C

	/**
	 * 104.1.52D : Adding comments should be possible in metadatacard for single assignment opened in rightpane after rejecting the assignment
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Adding comments should be possible in metadatacard for single assignment opened in rightpane after rejecting the assignment")
	public void SprintTest104_1_52D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName);
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment (" + assigName + ") is created.", driver);

			//Step-3 : Open the Properties dialog of the assignment in rightpane
			//---------------------------------------------------------------------------
			homePage.listView.clickItem(assigName);
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of an assignment is opened in rightpane.");

			//Step-4 : Select Reject icon for the assignment
			//-----------------------------------------------
			if (!metadatacard.clickRejectIcon())
				throw new Exception("Reject icon is not selected.");

			Log.message("4. Reject icon is selected");

			//Step-5 : Add comments to the assignment after selecting Reject icon
			//--------------------------------------------------------------------
			String comments = Utility.getObjectName(methodName);
			metadatacard.setComments(comments);

			Log.message("5. Comments is entered assignment after selecting Reject icon.", driver);

			//Step-6 : Save the assignment object
			//-----------------------------------
			metadatacard.saveAndClose(); 

			Log.message("6. Metadatacard is saved after selecting Reject icon and entering comments.");

			//Verification : Verify if assignment is Rejected and comments are as entered
			//-----------------------------------------------------------------------------
			if (!ListView.isRejectedByItemName(driver, assigName)) //Verifies if assignment is Rejected
				throw new Exception("Test case Failed. Assignment is not Rejected after saving the changes to metadatacard.");

			if (ListView.getCommentsByItemName(driver, assigName).equalsIgnoreCase(comments)) //Verifies if comments are as entered
				Log.pass("Test case Passed. Comments entered in metadatacard opened in rightpane after clicking Reject icon is saved successfully.");
			else
				Log.fail("Test case Failed. Comments entered in metadatacard opened in rightpane after clicking Reject icon is not saved.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_52D

	/**
	 * 104.1.59.2A : Reject option should not available in metadatacard opened through context menu properties for non-assigned users.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Reject option should not available in metadatacard opened through context menu properties for non-assigned users.")
	public void SprintTest104_1_59_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create an assignment 
			//------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName);
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("1. New Assignment (" + assigName + ") is created.", driver);

			//Step-2 : Log out and login with other user
			//------------------------------------------
			if (!Utility.logOut(driver))
				throw new Exception("Log out is not successful.");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), testVault);

			Log.message("2. Logs in with " + dataPool.get("UserName") + " user.");

			//Step-4 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyAssignments.Value, assigName);

			Log.message("3. Navigated to '" + viewToNavigate + "' view.");

			//Step-5 : Open the Properties dialog of the assignment through context menu
			//---------------------------------------------------------------------------
			homePage.listView.rightClickItem(assigName);
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of an assignment is opened through context menu.");

			//Step-6 : Click Reject icon by non-assigned user
			//-----------------------------------------------------
			if (!metadatacard.isNotRejectedDisplayed()) //Verifies if Approve icon is available
				throw new Exception("Not Approve icon is not displayed for non-assigned users.");

			if (!metadatacard.clickRejectIcon())
				throw new Exception("Reject icon is not selected by non-assigned user.");

			Log.message("5. Reject icon is selected by non-assigned user.");

			//Step-7 : Click Save button
			//---------------------------
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("6. Save button is clicked.");

			//Verification : Verify if M-Files dialog with Access denied message is displayed
			//-------------------------------------------------------------------------------
			String message = metadatacard.getMfilesDialogMessage();

			if (message.toUpperCase().contains("ACCESS DENIED"))
				Log.pass("Test case Passed. Access denied message dialog opened on rejected assignment through context menu properties for non-assigned users. Message : " + message);
			else
				Log.fail("Test case Failed.  Access denied message dialog does not opened on rejecting assignment through context menu properties for non-assigned users. Message : " + message, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_59_2A

	/**
	 * 104.1.59.2B : Reject option should not available in metadatacard opened through operations menu properties for non-assigned users.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Reject option should not available in metadatacard opened through operations menu properties for non-assigned users.")
	public void SprintTest104_1_59_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create an assignment 
			//------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName);
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("1. New Assignment (" + assigName + ") is created.", driver);

			//Step-2 : Log out and login with other user
			//------------------------------------------
			if (!Utility.logOut(driver))
				throw new Exception("Log out is not successful.");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), testVault);

			Log.message("2. Logs in with " + dataPool.get("UserName") + " user.");

			//Step-4 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyAssignments.Value, assigName);

			Log.message("3. Navigated to '" + viewToNavigate + "' view.");

			//Step-5 : Open the Properties dialog of the assignment through operations menu
			//---------------------------------------------------------------------------
			homePage.listView.clickItem(assigName);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of an assignment is opened through operations menu.");

			//Step-6 : Click Reject icon by non-assigned user
			//-----------------------------------------------------
			if (!metadatacard.isNotRejectedDisplayed()) //Verifies if Approve icon is available
				throw new Exception("Not Approve icon is not displayed for non-assigned users.");

			if (!metadatacard.clickRejectIcon())
				throw new Exception("Reject icon is not selected by non-assigned user.");

			Log.message("5. Reject icon is selected by non-assigned user.");

			//Step-7 : Click Save button
			//---------------------------
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("6. Save button is clicked.");

			//Verification : Verify if M-Files dialog with Access denied message is displayed
			//-------------------------------------------------------------------------------
			String message = metadatacard.getMfilesDialogMessage();

			if (message.toUpperCase().contains("ACCESS DENIED"))
				Log.pass("Test case Passed. Access denied message dialog opened on rejected assignment through operations menu properties for non-assigned users. Message : " + message);
			else
				Log.fail("Test case Failed.  Access denied message dialog does not opened on rejecting assignment through operations menu properties for non-assigned users. Message : " + message, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_59_2B

	/**
	 * 104.1.59.2C : Reject option should not available in metadatacard opened through taskpanel menu properties for non-assigned users.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Reject option should not available in metadatacard opened through taskpanel menu properties for non-assigned users.")
	public void SprintTest104_1_59_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create an assignment 
			//------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName);
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("1. New Assignment (" + assigName + ") is created.", driver);

			//Step-2 : Log out and login with other user
			//------------------------------------------
			if (!Utility.logOut(driver))
				throw new Exception("Log out is not successful.");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), testVault);

			Log.message("2. Logs in with " + dataPool.get("UserName") + " user.");

			//Step-4 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyAssignments.Value, assigName);

			Log.message("3. Navigated to '" + viewToNavigate + "' view.");

			//Step-5 : Open the Properties dialog of the assignment through taskpanel menu
			//---------------------------------------------------------------------------
			homePage.listView.clickItem(assigName);
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from taskpanel menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of an assignment is opened through taskpanel menu.");

			//Step-6 : Click Reject icon by non-assigned user
			//-----------------------------------------------------
			if (!metadatacard.isNotRejectedDisplayed()) //Verifies if Approve icon is available
				throw new Exception("Not Approve icon is not displayed for non-assigned users.");

			if (!metadatacard.clickRejectIcon())
				throw new Exception("Reject icon is not selected by non-assigned user.");

			Log.message("5. Reject icon is selected by non-assigned user.");

			//Step-7 : Click Save button
			//---------------------------
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("6. Save button is clicked.");

			//Verification : Verify if M-Files dialog with Access denied message is displayed
			//-------------------------------------------------------------------------------
			String message = metadatacard.getMfilesDialogMessage();

			if (message.toUpperCase().contains("ACCESS DENIED"))
				Log.pass("Test case Passed. Access denied message dialog opened on rejected assignment through taskpanel menu properties for non-assigned users. Message : " + message);
			else
				Log.fail("Test case Failed.  Access denied message dialog does not opened on rejecting assignment through taskpanel menu properties for non-assigned users. Message : " + message, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_59_2C

	/**
	 * 104.1.59.2D : Reject option should not available in metadatacard opened in rightpane metadatacard for non-assigned users.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Reject option should not available in metadatacard opened in rightpane metadatacard for non-assigned users.")
	public void SprintTest104_1_59_2D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create an assignment 
			//------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName);
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("1. New Assignment (" + assigName + ") is created.", driver);

			//Step-2 : Log out and login with other user
			//------------------------------------------
			if (!Utility.logOut(driver))
				throw new Exception("Log out is not successful.");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), testVault);

			Log.message("2. Logs in with " + dataPool.get("UserName") + " user.");

			//Step-4 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyAssignments.Value, assigName);

			Log.message("3. Navigated to '" + viewToNavigate + "' view.");

			//Step-5 : Open the Properties dialog of the assignment in rightpane metadatacard
			//---------------------------------------------------------------------------
			homePage.listView.clickItem(assigName);
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of an assignment is opened in rightpane metadatacard.");

			//Step-6 : Click Reject icon by non-assigned user
			//-----------------------------------------------------
			if (!metadatacard.isNotRejectedDisplayed()) //Verifies if Approve icon is available
				throw new Exception("Not Approve icon is not displayed for non-assigned users.");

			if (!metadatacard.clickRejectIcon())
				throw new Exception("Reject icon is not selected by non-assigned user.");

			Log.message("5. Reject icon is selected by non-assigned user.");

			//Step-7 : Click Save button
			//---------------------------
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("6. Save button is clicked.");

			//Verification : Verify if M-Files dialog with Access denied message is displayed
			//-------------------------------------------------------------------------------
			String message = metadatacard.getMfilesDialogMessage();

			if (message.toUpperCase().contains("ACCESS DENIED"))
				Log.pass("Test case Passed. Access denied message dialog opened on rejected assignment in rightpane properties for non-assigned users. Message : " + message);
			else
				Log.fail("Test case Failed.  Access denied message dialog does not opened on rejecting assignment in rightpane properties for non-assigned users. Message : " + message, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_59_2D

	/**
	 * 104.1.59.2E : Reject option should not available in taskpanel for non-assigned users.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Reject option should not available in taskpanel for non-assigned users.")
	public void SprintTest104_1_59_2E(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create an assignment 
			//------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName);
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("1. New Assignment (" + assigName + ") is created.", driver);

			//Step-2 : Log out and login with other user
			//------------------------------------------
			if (!Utility.logOut(driver))
				throw new Exception("Log out is not successful.");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), testVault);

			Log.message("2. Logs in with " + dataPool.get("UserName") + " user.");

			//Step-4 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyAssignments.Value, assigName);

			Log.message("3. Navigated to '" + viewToNavigate + "' view.");

			//Step-5 : Select the assignment
			//------------------------------
			homePage.listView.clickItem(assigName);

			Log.message("4. Assignment (" + assigName + ") is selected.");

			//Verification : Verify if Reject icon is available
			//--------------------------------------------------
			if (!homePage.taskPanel.isItemExists(Caption.MenuItems.Reject.Value)) //Verifies if Reject icon is available
				Log.pass("Test case Passed. Reject is not available in taskpanel for non-assigned users.");
			else
				Log.fail("Test case Failed. Reject is available in taskpanel for non-assigned users.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_59_2E

	/**
	 * 172015.3.61.1A : Assignment should get Rejected in metadatadatacard after checking out.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Assignment should get Rejected in metadatadatacard after checking out.")
	public void SprintTest172015_3_61_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select New Assignment from New menu
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName).toString(); //Name of the object with current method date & time
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("Class")); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			Log.message("2. New Assignment(" + assigName + ") is created with class (" + dataPool.get("Class") + ").", driver);

			//Step-3 : Check out the newly created assignment
			//-----------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from taskpanel menu

			if (!ListView.isCheckedOutByItemName(driver, assigName))
				throw new Exception("Assignment (" + assigName + ") is not checked out.");

			Log.message("3. Assignment (" + assigName + ") is checked out.");

			//Step-4 : Open the Properties dialog of the new assignment from context menu
			//-----------------------------------------------------------------------
			if (!homePage.listView.rightClickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of the new assignment (" + assigName + ") is opened through context menu.");

			//Step-5 : Reject the checked out assignment
			//----------------------------------------------------
			if (!metadatacard.clickRejectIcon()) //Clicks Reject Assignment icon.
				throw new Exception("Reject icon is not clicked.");

			metadatacard.saveAndClose(); //Saves the metdatacard

			Log.message("5. Reject icon for assignment (" + assigName + ") in metadatacard is selected and saved.");

			//Verification : Verify if checked out assignment is in Rejected state
			//----------------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (metadatacard.isSelectedObjectRejectedByUser(userFullName)) //Checks if Assignment is Rejected
				Log.pass("Test case Passed. Checked out Assignment (" + assigName + ") is in Rejected state.");
			else
				Log.fail("Test case Failed. Checked out Assignment (" + assigName + ") is not in Rejected state.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_61_1A

	/**
	 * 172015.3.61.1B : Assignment should get Rejected in rightpane after checking out.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Assignment should get Rejected in rightpane after checking out.")
	public void SprintTest172015_3_61_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select New Assignment from New menu
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName).toString(); //Name of the object with current method date & time
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("Class")); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			Log.message("2. New Assignment(" + assigName + ") is created with class (" + dataPool.get("Class") + ").", driver);

			//Step-3 : Check out the newly created assignment
			//-----------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from taskpanel menu

			if (!ListView.isCheckedOutByItemName(driver, assigName))
				throw new Exception("Assignment (" + assigName + ") is not checked out.");

			Log.message("3. Assignment (" + assigName + ") is checked out.");

			//Step-4 : Open the Properties dialog of the new assignment in right pane
			//-----------------------------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of the new assignment (" + assigName + ") is opened in right pane.");

			//Step-5 : Reject the checked out assignment
			//----------------------------------------------------
			if (!metadatacard.clickRejectIcon()) //Clicks Reject Assignment icon.
				throw new Exception("Reject icon is not clicked.");

			metadatacard.saveAndClose(); //Saves the metdatacard

			Log.message("5. Reject icon for assignment (" + assigName + ") in rightpane is selected and saved.");

			//Verification : Verify if checked out assignment is in Rejected state
			//----------------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (metadatacard.isSelectedObjectRejectedByUser(userFullName)) //Checks if Assignment is Rejected
				Log.pass("Test case Passed. Checked out Assignment (" + assigName + ") is in Rejected state.");
			else
				Log.fail("Test case Failed. Checked out Assignment (" + assigName + ") is not in Rejected state.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_61_1B

	/**
	 * 172015.3.61.1C : Assignment should get Rejected in taskpanel after checking out.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Assignment should get Rejected in taskpanel after checking out.")
	public void SprintTest172015_3_61_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select New Assignment from New menu
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName).toString(); //Name of the object with current method date & time
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("Class")); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			Log.message("2. New Assignment(" + assigName + ") is created with class (" + dataPool.get("Class") + ").", driver);

			//Step-3 : Check out the newly created assignment
			//-----------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from taskpanel menu

			if (!ListView.isCheckedOutByItemName(driver, assigName))
				throw new Exception("Assignment (" + assigName + ") is not checked out.");

			Log.message("3. Assignment (" + assigName + ") is checked out.");

			//Step-4 : Open the Properties dialog of the new assignment in right pane
			//-----------------------------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.markApproveReject("Reject"); //Selects Reject from taskpanel

			Log.message("4. Reject assignment (" + assigName + ") in taskpanel is selected.");

			//Verification : Verify if checked out assignment is in Rejected state
			//----------------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (metadatacard.isSelectedObjectRejectedByUser(userFullName)) //Checks if Assignment is Rejected
				Log.pass("Test case Passed. Checked out Assignment (" + assigName + ") is in Rejected state.");
			else
				Log.fail("Test case Failed. Checked out Assignment (" + assigName + ") is not in Rejected state.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_61_1C

	/**
	 * 172015.3.61.1D : Assignment that is created in checked-out mode should get Rejected in metadatacard.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Assignment that is created in checked-out mode should get Rejected in metadatacard.")
	public void SprintTest172015_3_61_1D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select New Assignment from New menu
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName).toString(); //Name of the object with current method date & time
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("Class")); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.setCheckInImmediately(false);
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			Log.message("2. New Assignment(" + assigName + ") is created with class (" + dataPool.get("Class") + ") in checked out mode.", driver);

			//Step-3 : Open the metadatacard of the new assignment in right pane
			//-----------------------------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("3. Metadatacard of the new assignment (" + assigName + ") is opened in right pane.");

			//Step-4 : Reject the checked out assignment
			//----------------------------------------------------
			if (!metadatacard.clickRejectIcon()) //Clicks Reject Assignment icon.
				throw new Exception("Reject icon is not clicked.");

			metadatacard.saveAndClose(); //Saves the metdatacard

			Log.message("4. Reject icon for assignment (" + assigName + ") in rightpane is selected and saved.");

			//Verification : Verify if checked out assignment is in Rejected state
			//----------------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (metadatacard.isSelectedObjectRejectedByUser(userFullName)) //Checks if Assignment is Rejected
				Log.pass("Test case Passed. Checked out Assignment (" + assigName + ") is in Rejected state.");
			else
				Log.fail("Test case Failed. Checked out Assignment (" + assigName + ") is not in Rejected state.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_61_1D

	/**
	 * 172015.3.61.1E : Assignment that is created in checked-out mode should get Rejected in taskpanel
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Assignment that is created in checked-out mode should get Rejected in taskpanel")
	public void SprintTest172015_3_61_1E(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select New Assignment from New menu
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName).toString(); //Name of the object with current method date & time
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("Class")); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.setCheckInImmediately(false);
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			Log.message("2. New Assignment(" + assigName + ") is created with class (" + dataPool.get("Class") + ") in checked out mode.", driver);

			//Step-3 : Reject the checked out assignment
			//--------------------------------------------
			homePage.taskPanel.markApproveReject("Reject"); //Selects Reject from taskpanel

			Log.message("3. Reject for assignment (" + assigName + ") in taskpanel is selected.");

			//Verification : Verify if checked out assignment is in Rejected state
			//----------------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (metadatacard.isSelectedObjectRejectedByUser(userFullName)) //Checks if Assignment is Rejected
				Log.pass("Test case Passed. Checked out Assignment (" + assigName + ") is in Rejected state.");
			else
				Log.fail("Test case Failed. Checked out Assignment (" + assigName + ") is not in Rejected state.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_61_1E

	/**
	 * 172015.3.61.2A : Checked out Rejected Assignment through metadatacard should be in Rejected state after performing check in operation.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Checked out Rejected Assignment through metadatacard should be in Rejected state after performing check in operation.")
	public void SprintTest172015_3_61_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select New Assignment from New menu
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName).toString(); //Name of the object with current method date & time
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("Class")); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			Log.message("2. New Assignment(" + assigName + ") is created with class (" + dataPool.get("Class") + ").");

			//Step-3 : Check out the newly created assignment
			//-----------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from taskpanel menu

			if (!ListView.isCheckedOutByItemName(driver, assigName))
				throw new Exception("Assignment (" + assigName + ") is not checked out.");

			Log.message("3. Assignment (" + assigName + ") is checked out.");

			//Step-4 : Open the Properties dialog of the new assignment in right pane
			//-----------------------------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of the new assignment (" + assigName + ") is opened in right pane.");

			//Step-5 : Reject the checked out assignment
			//----------------------------------------------------
			if (!metadatacard.clickRejectIcon()) //Clicks Reject Assignment icon.
				throw new Exception("Reject icon is not clicked.");

			metadatacard.saveAndClose(); //Saves the metdatacard
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (!metadatacard.isSelectedObjectRejectedByUser(userFullName)) //Checks if Assignment is Rejected
				throw new Exception("Checked out assignment (" + assigName + ") is not Rejected by user (" + userFullName + ").");

			Log.message("5. Assignment (" + assigName + ") is Rejected in rightpane.");

			//Step-6 : Check in the Rejected assignment
			//-----------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value); //Selects Checkout from taskpanel menu

			if (ListView.isCheckedOutByItemName(driver, assigName))
				throw new Exception("Assignment (" + assigName + ") is not checked in.");

			Log.message("6. Rejected assignment (" + assigName + ") is checked in.");

			//Verification : Verify if checked out assignment is in Rejected state
			//----------------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (metadatacard.isSelectedObjectRejectedByUser(userFullName)) //Checks if Assignment is Rejected
				Log.pass("Test case Passed. Checked out Rejected assignment (" + assigName + ") through metadatacard is in Rejected state after performing check-in operation.");
			else
				Log.fail("Test case Failed. Checked out Rejected assignment (" + assigName + ") through metadatacard is not in Rejected state after performing check-in operation.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_61_2A

	/**
	 * 172015.3.61.2B : Checked out Rejected Assignment through taskpanel should be in Rejected state after performing check in operation.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Checked out Rejected Assignment through taskpanel should be in Rejected state after performing check in operation.")
	public void SprintTest172015_3_61_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select New Assignment from New menu
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName).toString(); //Name of the object with current method date & time
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("Class")); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			Log.message("2. New Assignment(" + assigName + ") is created with class (" + dataPool.get("Class") + ").", driver);

			//Step-3 : Check out the newly created assignment
			//-----------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from taskpanel menu

			if (!ListView.isCheckedOutByItemName(driver, assigName))
				throw new Exception("Assignment (" + assigName + ") is not checked out.");

			Log.message("3. Assignment (" + assigName + ") is checked out.");

			//Step-4 : Reject the checked out assignment through taskapanel
			//--------------------------------------------------------------
			homePage.taskPanel.markApproveReject("Reject"); //Selects Reject from taskpanel//Clicks Reject from taskpanel
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (!metadatacard.isSelectedObjectRejectedByUser(userFullName)) //Checks if Assignment is Rejected
				throw new Exception("Checked out assignment (" + assigName + ") is not Rejected by user (" + userFullName + ").");

			Log.message("4. Checked out assignment (" + assigName + "). is Rejected through taskpanel.");

			//Step-5 : Check in the Rejected assignment
			//-----------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value); //Selects Checkout from taskpanel menu

			if (ListView.isCheckedOutByItemName(driver, assigName))
				throw new Exception("Assignment (" + assigName + ") is not checked in.");

			Log.message("5. Rejected assignment (" + assigName + ") is checked in.");

			//Verification : Verify if checked out assignment is in Rejected state
			//----------------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (metadatacard.isSelectedObjectRejectedByUser(userFullName)) //Checks if Assignment is Rejected
				Log.pass("Test case Passed. Checked out Rejected assignment (" + assigName + ") through taskpanel is in Rejected state after performing check-in operation.");
			else
				Log.fail("Test case Failed. Checked out Rejected assignment (" + assigName + ") through taskpanel is not in Rejected state after performing check-in operation.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_61_2B

	/**
	 * 172015.3.61.2C : Assignment that is created in checked-out that is Rejected in metadatacard and remain same after perfomring check-in operation
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Assignment that is created in checked-out that is Rejected in metadatacard and remain same after perfomring check-in operation.")
	public void SprintTest172015_3_61_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select New Assignment from New menu
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName).toString(); //Name of the object with current method date & time
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("Class")); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.setCheckInImmediately(false);
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			Log.message("2. New Assignment(" + assigName + ") is created with class (" + dataPool.get("Class") + ") in checked out mode.", driver);

			//Step-3 : Open the Properties dialog of the new assignment in right pane
			//-----------------------------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of the new assignment (" + assigName + ") is opened in right pane.");

			//Step-4 : Reject the checked out assignment
			//----------------------------------------------------
			if (!metadatacard.clickRejectIcon()) //Clicks Reject Assignment icon.
				throw new Exception("Reject icon is not clicked.");

			metadatacard.saveAndClose(); //Saves the metdatacard
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (!metadatacard.isSelectedObjectRejectedByUser(userFullName)) //Checks if Assignment is Rejected
				throw new Exception("Checked out assignment (" + assigName + ") is not Rejected by user (" + userFullName + ").");

			Log.message("4. Checked out assignment (" + assigName + ") is Rejected in rightpane.");

			//Step-5 : Check in the Rejected assignment
			//-----------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value); //Selects Checkout from taskpanel menu

			if (ListView.isCheckedOutByItemName(driver, assigName))
				throw new Exception("Assignment (" + assigName + ") is not checked in.");

			Log.message("5. Rejected assignment (" + assigName + ") is checked in.");

			//Verification : Verify if checked out assignment is in Rejected state
			//----------------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (metadatacard.isSelectedObjectRejectedByUser(userFullName)) //Checks if Assignment is Rejected
				Log.pass("Test case Passed. Assignment(" + assigName + ") created in checked out mode and Rejected through metadatacard is in Rejected state after performing check-in operation.");
			else
				Log.fail("Test case Failed. Assignment(" + assigName + ") created in checked out mode and Rejected through metadatacard is not in Rejected state after performing check-in operation.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_61_2C

	/**
	 * 172015.3.61.2D : Assignment that is created in checked-out that is Rejected in taskpanel and remain same after perfomring check-in operation
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Assignment that is created in checked-out that is Rejected in taskpanel and remain same after perfomring check-in operation.")
	public void SprintTest172015_3_61_2D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select New Assignment from New menu
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName).toString(); //Name of the object with current method date & time
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("Class")); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.setCheckInImmediately(false);
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			Log.message("2. New Assignment(" + assigName + ") is created with class (" + dataPool.get("Class") + ") in checked out mode.", driver);

			//Step-3 : Reject the checked out assignment through taskapanel
			//--------------------------------------------------------------
			homePage.taskPanel.markApproveReject("Reject"); //Selects Reject from taskpanel//Clicks Reject from taskpanel
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (!metadatacard.isSelectedObjectRejectedByUser(userFullName)) //Checks if Assignment is Rejected
				throw new Exception("Checked out assignment (" + assigName + ") is not Rejected by user (" + userFullName + ").");

			Log.message("3. Checked out assignment (" + assigName + "). is Rejected through taskpanel.");

			//Step-4 : Check in the Rejected assignment
			//------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value); //Selects Checkout from taskpanel menu

			if (ListView.isCheckedOutByItemName(driver, assigName))
				throw new Exception("Assignment (" + assigName + ") is not checked in.");

			Log.message("4. Rejected assignment (" + assigName + ") is checked in.");

			//Verification : Verify if checked out assignment is in Rejected state
			//----------------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (metadatacard.isSelectedObjectRejectedByUser(userFullName)) //Checks if Assignment is Rejected
				Log.pass("Test case Passed. Assignment(" + assigName + ") created in checked out mode and Rejected through metadatacard is in Rejected state after performing check-in operation.");
			else
				Log.fail("Test case Failed. Assignment(" + assigName + ") created in checked out mode and Rejected through metadatacard is not in Rejected state after performing check-in operation.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_61_2D

	/**
	 * 172015.3.61.3A : Checked out Rejected Assignment through metadatacard should be in un-Rejected state after performing undo-checkout operation.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Checked out Rejected Assignment through metadatacard should be in un-Rejected state after performing undo-checkout operation.")
	public void SprintTest172015_3_61_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select New Assignment from New menu
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName).toString(); //Name of the object with current method date & time
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("Class")); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			Log.message("2. New Assignment(" + assigName + ") is created with class (" + dataPool.get("Class") + ").", driver);

			//Step-3 : Check out the newly created assignment
			//-----------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from taskpanel menu

			if (!ListView.isCheckedOutByItemName(driver, assigName))
				throw new Exception("Assignment (" + assigName + ") is not checked out.");

			Log.message("3. Assignment (" + assigName + ") is checked out.");

			//Step-4 : Open the Properties dialog of the new assignment in right pane
			//-----------------------------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("4. Metadatacard of the new assignment (" + assigName + ") is opened in right pane.");

			//Step-5 : Reject the checked out assignment
			//----------------------------------------------------
			if (!metadatacard.clickRejectIcon()) //Clicks Reject Assignment icon.
				throw new Exception("Reject icon is not clicked.");

			metadatacard.saveAndClose(); //Saves the metdatacard
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (!metadatacard.isSelectedObjectRejectedByUser(userFullName)) //Checks if Assignment is Rejected
				throw new Exception("Checked out assignment (" + assigName + ") is not Rejected by user (" + userFullName + ").");

			Log.message("5. Assignment (" + assigName + ") is Rejected in rightpane.");

			//Step-6 : Undo-Checkout the Rejected assignment
			//-----------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Checkout from taskpanel menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo-checkout operation

			if (ListView.isCheckedOutByItemName(driver, assigName))
				throw new Exception("Assignment (" + assigName + ") is not undo checked out.");

			Log.message("6. Rejected assignment (" + assigName + ") is undo-checked out.");

			//Verification : Verify if checked out assignment is in Rejected state
			//----------------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (!metadatacard.isSelectedObjectRejectedByUser(userFullName)) //Checks if Assignment is Rejected
				Log.pass("Test case Passed. Checked out Rejected assignment (" + assigName + ") through metadatacard is in un-Rejected state after performing undo check out operation.");
			else
				Log.fail("Test case Failed. Checked out Rejected assignment (" + assigName + ") through metadatacard is in Rejected state after performing undo check out operation.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_61_3A

	/**
	 * 172015.3.61.3B : Checked out Rejected Assignment in taskpanel should be in un-Rejected state after performing undo-checkout operation.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Checked out Rejected Assignment in taskpanel should be in un-Rejected state after performing undo-checkout operation.")
	public void SprintTest172015_3_61_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select New Assignment from New menu
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName).toString(); //Name of the object with current method date & time
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("Class")); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			Log.message("2. New Assignment(" + assigName + ") is created with class (" + dataPool.get("Class") + ").", driver);

			//Step-3 : Check out the newly created assignment
			//-----------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from taskpanel menu

			if (!ListView.isCheckedOutByItemName(driver, assigName))
				throw new Exception("Assignment (" + assigName + ") is not checked out.");

			Log.message("3. Assignment (" + assigName + ") is checked out.");

			//Step-4 : Open the Properties dialog of the new assignment in right pane
			//-----------------------------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.markApproveReject("Reject"); //Selects Reject from taskpanel //Selects Reject from taskpanel
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (!metadatacard.isSelectedObjectRejectedByUser(userFullName)) //Checks if Assignment is Rejected
				throw new Exception("Checked out assignment (" + assigName + ") is not Rejected by user (" + userFullName + ").");

			Log.message("4. Assignment (" + assigName + ") is Rejected from taskpanel.");

			//Step-6 : Undo-Checkout the Rejected assignment
			//-----------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects undo checkout from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo-checkout operation

			if (ListView.isCheckedOutByItemName(driver, assigName))
				throw new Exception("Assignment (" + assigName + ") is not undo checked out.");

			Log.message("5. Rejected assignment (" + assigName + ") is undo-checked out.");

			//Verification : Verify if checked out assignment is in Rejected state
			//----------------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (!metadatacard.isSelectedObjectRejectedByUser(userFullName)) //Checks if Assignment is Rejected
				Log.pass("Test case Passed. Checked out Rejected assignment (" + assigName + ") through metadatacard is in un-Rejected state after performing undo check out operation.");
			else
				Log.fail("Test case Failed. Checked out Rejected assignment (" + assigName + ") through metadatacard is in Rejected state after performing undo check out operation.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_61_3B

	/**
	 * 172015.3.61.3C : Assignment that is created in checked-out mode that is Rejected through rightpane gets disappeared on performing undo-checkout operation
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Assignment that is created in checked-out mode that is Rejected through rightpane gets disappeared on performing undo-checkout operation.")
	public void SprintTest172015_3_61_3C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select New Assignment from New menu
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName).toString(); //Name of the object with current method date & time
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("Class")); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.setCheckInImmediately(false);
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			Log.message("2. New Assignment(" + assigName + ") is created with class (" + dataPool.get("Class") + ") in checked out mode.", driver);

			//Step-3 : Open the Properties dialog of the new assignment in right pane
			//-----------------------------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of the new assignment (" + assigName + ") is opened in right pane.");

			//Step-4 : Reject the checked out assignment
			//----------------------------------------------------
			if (!metadatacard.clickRejectIcon()) //Clicks Reject Assignment icon.
				throw new Exception("Reject icon is not clicked.");

			metadatacard.saveAndClose(); //Saves the metdatacard
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (!metadatacard.isSelectedObjectRejectedByUser(userFullName)) //Checks if Assignment is Rejected
				throw new Exception("Checked out assignment (" + assigName + ") is not Rejected by user (" + userFullName + ").");

			Log.message("4. Checked out assignment (" + assigName + ") is Rejected in rightpane.");

			//Step-5 : Undo-Checkout the Rejected assignment
			//-----------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects undo checkout from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo-checkout operation

			Log.message("5. Rejected assignment (" + assigName + ") is undo-checked out.");

			//Verification : Verify if checked out assignment is in Rejected state
			//----------------------------------------------------------------------
			homePage.searchPanel.search(assigName, Caption.Search.SearchOnlyAssignments.Value);

			if (!homePage.listView.isItemExists(assigName)) //Checks if Assignment is Rejected
				Log.pass("Test case Passed. Assignment(" + assigName + ") created in checked out mode and Rejected through metadatacard does not exists in list after performing undo-checkout operation.");
			else
				Log.fail("Test case Failed. Assignment(" + assigName + ") created in checked out mode and Rejected through metadatacard exists in list after performing undo-checkout operation.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_61_3C

	/**
	 * 172015.3.61.3D: Assignment that is created in checked-out mode that is Rejected in taskpanel gets disappeared on performing undo-checkout operation
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Assignment that is created in checked-out mode that is Rejected in taskpanel gets disappeared on performing undo-checkout operation.")
	public void SprintTest172015_3_61_3D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select New Assignment from New menu
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName).toString(); //Name of the object with current method date & time
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("Class")); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.setCheckInImmediately(false);
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			Log.message("2. New Assignment(" + assigName + ") is created with class (" + dataPool.get("Class") + ") in checked out mode.", driver);

			//Step-3 : Open the Properties dialog of the new assignment in right pane
			//-----------------------------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.markApproveReject("Reject"); //Selects Reject from taskpanel
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (!metadatacard.isSelectedObjectRejectedByUser(userFullName)) //Checks if Assignment is Rejected
				throw new Exception("Checked out assignment (" + assigName + ") is not Rejected by user (" + userFullName + ").");

			Log.message("3. Checked out assignment (" + assigName + ") is Rejected through taskpanel.");

			//Step-4 : Undo-Checkout the Rejected assignment
			//-----------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects undo checkout from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo-checkout operation

			Log.message("4. Rejected assignment (" + assigName + ") is undo-checked out.");

			//Verification : Verify if checked out assignment is in Rejected state
			//----------------------------------------------------------------------
			homePage.searchPanel.search(assigName, Caption.Search.SearchOnlyAssignments.Value);

			if (!homePage.listView.isItemExists(assigName)) //Checks if Assignment is Rejected
				Log.pass("Test case Passed. Assignment(" + assigName + ") created in checked out mode and Rejected through metadatacard does not exists in list after performing undo-checkout operation.");
			else
				Log.fail("Test case Failed. Assignment(" + assigName + ") created in checked out mode and Rejected through metadatacard exists in list after performing undo-checkout operation.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_61_3D

} //End Class Assignments
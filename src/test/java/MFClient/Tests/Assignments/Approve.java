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
public class Approve {

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
	 * 104.1.24.2A : Create assignment by assigning it to other user and Approve it by assigned user in metadatacard - context menu properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Create assignment by assigning it to other user and Approve it by assigned user in metadatacard - context menu properties")
	public void SprintTest104_1_24_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-4 : Logout and login with new user
			//--------------------------------------
			if (!Utility.logOut(driver)) //Logs out from the vault
				throw new Exception("Logout is not successful");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), testVault); //Launched driver and logged in

			Log.message("4. Logged out and logged in with new user (" + dataPool.get("UserName") + ").");

			//Step-5 : Open the Properties dialog of the new assignment through context menu
			//------------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Taskpanel.AssignedToMe.Value, ""); //Navigates to Assigned to me view

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in Assigned to me view.");

			if (!homePage.listView.rightClickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			MetadataCard metadatacard1 = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("5. Properties dialog of the new assignment (" + assigName + ") is opened through context menu.");

			//Step-6 : Click Approve button in the metadatacard
			//-------------------------------------------------
			if (!metadatacard1.clickApproveIcon())
				throw new Exception("Approve Icon is not clicked.");

			Log.message("6. Approve icon in the Assignment is clicked.");

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			metadatacard1.saveAndClose(); //Saves and closes the metadatacard.
			metadatacard1 = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (metadatacard1.isSelectedObjectApprovedByUser(dataPool.get("UserFullName"))) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment is approved in metadatacard properties.");
			else
				Log.fail("Test case Failed. Assignment is not approved in metadatacard properties.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_24_2A

	/**
	 * 104.1.24.2B : Create assignment by assigning it to other user and Approve it by assigned user in metadatacard - operations menu properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Create assignment by assigning it to other user and Approve it by assigned user in metadatacard - operations menu properties")
	public void SprintTest104_1_24_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-4 : Logout and login with new user
			//--------------------------------------
			if (!Utility.logOut(driver)) //Logs out from the vault
				throw new Exception("Logout is not successful");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), testVault); //Launched driver and logged in

			Log.message("4. Logged out and logged in with new user (" + dataPool.get("UserName") + ").");

			//Step-5 : Open the Properties dialog of the new assignment through operations menu
			//------------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Taskpanel.AssignedToMe.Value, ""); //Navigates to Assigned to me view

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in Assigned to me view.");

			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("5. Properties dialog of the new assignment (" + assigName + ") is opened through operations menu.");

			//Step-6 : Click Approve button in the metadatacard
			//-------------------------------------------------
			if (!metadatacard.clickApproveIcon())
				throw new Exception("Approve Icon is not clicked.");

			Log.message("6. Approve icon in the Assignment is clicked.");

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.
			metadatacard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (metadatacard.isSelectedObjectApprovedByUser(dataPool.get("UserFullName"))) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment is approved in metadatacard properties.");
			else
				Log.fail("Test case Failed. Assignment is not approved in metadatacard properties.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_24_2B

	/**
	 * 104.1.24.2C : Create assignment by assigning it to other user and Approve it by assigned user in metadatacard - taskpane properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Create assignment by assigning it to other user and Approve it by assigned user in metadatacard - taskpane properties")
	public void SprintTest104_1_24_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			Log.message("5. Properties dialog of the new assignment (" + assigName + ") is opened through taskpane.");

			//Step-5 : Click Approve button in the metadatacard
			//-------------------------------------------------
			if (!metadatacard.clickApproveIcon())
				throw new Exception("Approve Icon is not clicked.");

			Log.message("6. Approve icon in the Assignment is clicked.");

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.
			metadatacard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (metadatacard.isSelectedObjectApprovedByUser(dataPool.get("UserFullName"))) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment is approved in metadatacard properties.");
			else
				Log.fail("Test case Failed. Assignment is not approved in metadatacard properties.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_24_2C

	/**
	 * 104.1.24.2D : Create assignment by assigning it to other user and Approve it by assigned user in metadatacard - Rightpane properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Create assignment by assigning it to other user and Approve it by assigned user in metadatacard - Rightpane properties")
	public void SprintTest104_1_24_2D(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-5 : Click Approve button in the metadatacard
			//-------------------------------------------------
			if (!metadatacard.clickApproveIcon())
				throw new Exception("Approve Icon is not clicked.");

			Log.message("6. Approve icon in the Assignment is clicked.");

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.
			metadatacard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (metadatacard.isSelectedObjectApprovedByUser(dataPool.get("UserFullName"))) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment is approved in metadatacard properties.");
			else
				Log.fail("Test case Failed. Assignment is not approved in metadatacard properties.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_24_2D

	/**
	 * 104.1.24.2E : Create assignment by assigning it to other user and Approve it by assigned user from taskpane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Create assignment by assigning it to other user and Approve it by assigned user from taskpane")
	public void SprintTest104_1_24_2E(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyAssignments.Value, assigName); //Navigates to Assigned to me view

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in Assigned to me view.");

			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.markApproveReject("Approve"); //Marks Approve from Taskpanel //Selects Properties from context menu

			Log.message("5. Properties dialog of the new assignment (" + assigName + ") is opened through taskpane.");

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true);//Instantiates the metadatacard
			if (metadatacard.isSelectedObjectApprovedByUser(dataPool.get("UserFullName"))) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment is approved in metadatacard properties.");
			else
				Log.fail("Test case Failed. Assignment is not approved in metadatacard properties.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_24_2E

	/**
	 * 104.1.24.4A : Approved assignee should be listed below when there are more assigned to users - Context menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Approved assignee should be listed below when there are more assigned to users - Context menu Properties")
	public void SprintTest104_1_24_4A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName1")); //Sets the assigned to property
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName2"), 2); //Sets the assigned to property
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName3"), 3); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment details are entered and saved the metadatacard.", driver);

			//Step-3 : Logout and login with first user
			//--------------------------------------
			if (!Utility.logOut(driver)) //Logs out from the vault
				throw new Exception("Logout is not successful");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName1"), dataPool.get("Password1"), testVault); //Launched driver and logged in

			Log.message("3. Logged out and logged in with new user (" + dataPool.get("UserName1") + ").");

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

			//Step-5 : Mark Complete the newly created assignment
			//----------------------------------------------------
			System.out.println(metadatacard.getPropertyValue("Class"));
			if (!metadatacard.clickApproveIcon(0)) //Clicks Approve Icon of the first user.
				throw new Exception("Mark Complete icon is not clicked.");

			metadatacard.saveAndClose(); //Clicked Save button in metadatacard.

			Log.message("5. Mark complete icon is selected and assignment is saved.", driver);

			//Step-6 : Open the Properties of the assignment through context menu and get the order of completed assignment
			//-------------------------------------------------------------------------------------------------------------
			if (!homePage.listView.rightClickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("6. Properties dialog of the assignment (" + assigName + ") is opened through context menu.");

			//Verification : Verifies if Assignment is mark completed
			//----------------------------------------------------------
			if (!metadatacard.getPropertyValue("Assigned to", 3).equalsIgnoreCase(dataPool.get("UserFullName1")))
				throw new Exception("Test case Failed. Approved user is not moved as last value in the assigned to property");

			if (!metadatacard.isApprovedSelected(2) && !metadatacard.isRejectedSelected(2))
				throw new Exception("Test case Failed.User (" + dataPool.get("UserFullName1") + ") status is not in approved status after approval.");

			if (!metadatacard.isApprovedSelected(0)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment with user (" + dataPool.get("UserFullName1") + ") at the top is not in approved or rejected state.");
			else
				Log.fail("Test case Failed. Assignment with user (" + dataPool.get("UserFullName1") + ") at the top is in approved or rejected state.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_24_4A

	/**
	 * 104.1.24.4B : Approved assignee should be listed below when there are more assigned to users - Operations menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Approved assignee should be listed below when there are more assigned to users - Operations menu Properties")
	public void SprintTest104_1_24_4B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName1")); //Sets the assigned to property
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName2"), 2); //Sets the assigned to property
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName3"), 3); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment details are entered and saved the metadatacard.", driver);

			//Step-3 : Logout and login with first user
			//--------------------------------------
			if (!Utility.logOut(driver)) //Logs out from the vault
				throw new Exception("Logout is not successful");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName1"), dataPool.get("Password1"), testVault); //Launched driver and logged in

			Log.message("3. Logged out and logged in with new user (" + dataPool.get("UserName1") + ").");

			//Step-4 : Open the Properties dialog of the new assignment through operations menu
			//---------------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Taskpanel.AssignedToMe.Value, ""); //Navigates to Assigned to me view

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in Assigned to me view.");

			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from operations menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of the new assignment (" + assigName + ") is opened through operations menu.");

			//Step-5 : Mark Complete the newly created assignment
			//----------------------------------------------------
			System.out.println(metadatacard.getPropertyValue("Class"));
			if (!metadatacard.clickApproveIcon(0)) //Clicks Approve Icon of the first user.
				throw new Exception("Mark Complete icon is not clicked.");

			metadatacard.saveAndClose(); //Clicked Save button in metadatacard.

			Log.message("5. Mark complete icon is selected and assignment is saved.", driver);

			//Step-6 : Open the Properties of the assignment through context menu and get the order of completed assignment
			//-------------------------------------------------------------------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from operations menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("6. Properties dialog of the assignment (" + assigName + ") is opened through operations menu.");

			//Verification : Verifies if Assignment is mark completed
			//----------------------------------------------------------
			if (!metadatacard.getPropertyValue("Assigned to", 3).equalsIgnoreCase(dataPool.get("UserFullName1")))
				throw new Exception("Test case Failed. Approved user is not moved as last value in the assigned to property");

			if (!metadatacard.isApprovedSelected(2) && !metadatacard.isRejectedSelected(2))
				throw new Exception("Test case Failed.User (" + dataPool.get("UserFullName1") + ") status is not in approved status after approval.");

			if (!metadatacard.isApprovedSelected(0)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment with user (" + dataPool.get("UserFullName1") + ") at the top is not in approved or rejected state.");
			else
				Log.fail("Test case Failed. Assignment with user (" + dataPool.get("UserFullName1") + ") at the top is in approved or rejected state.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_24_4B

	/**
	 * 104.1.24.4C : Approved assignee should be listed below when there are more assigned to users - Taskpane Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Approved assignee should be listed below when there are more assigned to users - Taskpane Properties")
	public void SprintTest104_1_24_4C(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName1")); //Sets the assigned to property
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName2"), 2); //Sets the assigned to property
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName3"), 3); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment details are entered and saved the metadatacard.", driver);

			//Step-3 : Logout and login with first user
			//--------------------------------------
			if (!Utility.logOut(driver)) //Logs out from the vault
				throw new Exception("Logout is not successful");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName1"), dataPool.get("Password1"), testVault); //Launched driver and logged in

			Log.message("3. Logged out and logged in with new user (" + dataPool.get("UserName1") + ").");

			//Step-4 : Open the Properties dialog of the new assignment through taskpanel
			//---------------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Taskpanel.AssignedToMe.Value, ""); //Navigates to Assigned to me view

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in Assigned to me view.");

			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from taskpanel

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of the new assignment (" + assigName + ") is opened through taskpanel.");

			//Step-5 : Mark Complete the newly created assignment
			//----------------------------------------------------
			System.out.println(metadatacard.getPropertyValue("Class"));

			if (!metadatacard.clickApproveIcon(0)) //Clicks Approve Icon of the first user.
				throw new Exception("Mark Complete icon is not clicked.");

			metadatacard.saveAndClose(); //Clicked Save button in metadatacard.

			Log.message("5. Mark complete icon is selected and assignment is saved.", driver);

			//Step-6 : Open the Properties of the assignment through context menu and get the order of completed assignment
			//-------------------------------------------------------------------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from taskpanel

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("6. Properties dialog of the assignment (" + assigName + ") is opened through taskpanel.");

			//Verification : Verifies if Assignment is mark completed
			//----------------------------------------------------------
			if (!metadatacard.getPropertyValue("Assigned to", 3).equalsIgnoreCase(dataPool.get("UserFullName1")))
				throw new Exception("Test case Failed. Approved user is not moved as last value in the assigned to property");

			if (!metadatacard.isApprovedSelected(2) && !metadatacard.isRejectedSelected(2))
				throw new Exception("Test case Failed.User (" + dataPool.get("UserFullName1") + ") status is not in approved status after approval.");

			if (!metadatacard.isApprovedSelected(0)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment with user (" + dataPool.get("UserFullName1") + ") at the top is not in approved or rejected state.");
			else
				Log.fail("Test case Failed. Assignment with user (" + dataPool.get("UserFullName1") + ") at the top is in approved or rejected state.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_24_4C

	/**
	 * 104.1.24.4D : Approved assignee should be listed below when there are more assigned to users - Right pane Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Approved assignee should be listed below when there are more assigned to users - Right pane Properties")
	public void SprintTest104_1_24_4D(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName1")); //Sets the assigned to property
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName2"), 2); //Sets the assigned to property
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName3"), 3); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment details are entered and saved the metadatacard.", driver);

			//Step-3 : Logout and login with first user
			//--------------------------------------
			if (!Utility.logOut(driver)) //Logs out from the vault
				throw new Exception("Logout is not successful");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName1"), dataPool.get("Password1"), testVault); //Launched driver and logged in

			Log.message("3. Logged out and logged in with new user (" + dataPool.get("UserName1") + ").");

			//Step-4 : Open the Properties dialog of the new assignment through taskpanel
			//---------------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Taskpanel.AssignedToMe.Value, ""); //Navigates to Assigned to me view

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in Assigned to me view.");

			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of the new assignment (" + assigName + ") is opened in right pane.");

			//Step-5 : Mark Complete the newly created assignment
			//----------------------------------------------------
			System.out.println(metadatacard.getPropertyValue("Class"));
			if (!metadatacard.clickApproveIcon(0)) //Clicks Approve Icon of the first user.
				throw new Exception("Mark Complete icon is not clicked.");

			metadatacard.saveAndClose(); //Clicked Save button in metadatacard.

			Log.message("5. Mark complete icon is selected and assignment is saved.", driver);

			//Step-6 : Open the Properties of the assignment through context menu and get the order of completed assignment
			//-------------------------------------------------------------------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("6. Properties dialog of the assignment (" + assigName + ") is opened in right pane.");

			//Verification : Verifies if Assignment is mark completed
			//----------------------------------------------------------
			if (!metadatacard.getPropertyValue("Assigned to", 3).equalsIgnoreCase(dataPool.get("UserFullName1")))
				throw new Exception("Test case Failed. Approved user is not moved as last value in the assigned to property");

			if (!metadatacard.isApprovedSelected(2) && !metadatacard.isRejectedSelected(2))
				throw new Exception("Test case Failed.User (" + dataPool.get("UserFullName1") + ") status is not in approved status after approval.");

			if (!metadatacard.isApprovedSelected(0)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment with user (" + dataPool.get("UserFullName1") + ") at the top is not in approved or rejected state.");
			else
				Log.fail("Test case Failed. Assignment with user (" + dataPool.get("UserFullName1") + ") at the top is in approved or rejected state.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_24_4D

	/**
	 * 104.1.32.1A : Verify if Approve option is removed in task pane when user perform Approve operation - Context meu properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Verify if Approve option is removed in task pane when user perform Approve operation - Context meu properties")
	public void SprintTest104_1_32_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-5 : Approve the assignment in metadatacard
			//-------------------------------------------------
			if (!metadatacard.clickApproveIcon()) //Clicks Approve icon in metadatacard
				throw new Exception("Approve Icon is not clicked.");

			metadatacard.saveAndClose(); //Saves and closes the metadatacard.

			SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), assigName);

			Log.message("5. Navigated to specified : "+ dataPool.get("NavigateToView")+"  view.");

			if (!ListView.getMarkedAsCompleteByItemName(driver, assigName).equalsIgnoreCase(userFullName)) //Checks if Assignment completed icon is displayed
				throw new Exception("Assignment is not approved in metadatacard properties.");

			Log.message("6. Assignment is approved in metadatacard properties.");

			//Verification : To verify if Approve is not displayed in taskpanel
			//--------------------------------------------------------------------
			if (!homePage.taskPanel.isItemExists(Caption.MenuItems.Approve.Value))
				Log.pass("Test case Passed. Approve is not displayed in taskpane after approving the assignment.");
			else
				Log.fail("Test case Failed. Approve is getting displayed after approving the assignment.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_32_1A

	/**
	 * 104.1.32.1B : Verify if Approve option is removed in task pane when user perform Approve operation - Operations meu properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Verify if Approve option is removed in task pane when user perform Approve operation - Operations meu properties")
	public void SprintTest104_1_32_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-5 : Approve the assignment in metadatacard
			//-------------------------------------------------
			if (!metadatacard.clickApproveIcon()) //Clicks Approve icon in metadatacard
				throw new Exception("Approve Icon is not clicked.");

			metadatacard.saveAndClose(); //Saves and closes the metadatacard.

			SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), assigName);

			Log.message("5. Navigated to specified : "+ dataPool.get("NavigateToView")+"  view.");

			if (!ListView.getMarkedAsCompleteByItemName(driver, assigName).equalsIgnoreCase(userFullName)) //Checks if Assignment completed icon is displayed
				throw new Exception("Assignment is not approved in metadatacard properties.");

			Log.message("6. Assignment is approved in metadatacard properties.");

			//Verification : To verify if Approve is not displayed in taskpanel
			//--------------------------------------------------------------------
			if (!homePage.taskPanel.isItemExists(Caption.MenuItems.Approve.Value))
				Log.pass("Test case Passed. Approve is not displayed in taskpane after approving the assignment.");
			else
				Log.fail("Test case Failed. Approve is getting displayed after approving the assignment.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_32_1B

	/**
	 * 104.1.32.1C : Verify if Approve option is removed in task pane when user perform Approve operation - Taskpanel properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Verify if Approve option is removed in task pane when user perform Approve operation - Taskpanel properties")
	public void SprintTest104_1_32_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-4 : Open the Properties dialog of the new assignment through taskpanel
			//------------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Taskpanel.AssignedToMe.Value, ""); //Navigates to Assigned to me view

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in Assigned to me view.");

			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from operations menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of the new assignment (" + assigName + ") is opened through taskpanel.");

			//Step-5 : Approve the assignment in metadatacard
			//-------------------------------------------------
			if (!metadatacard.clickApproveIcon()) //Clicks Approve icon in metadatacard
				throw new Exception("Approve Icon is not clicked.");

			metadatacard.saveAndClose(); //Saves and closes the metadatacard.
			metadatacard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (!metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment completed icon is displayed
				throw new Exception("Assignment is not approved in metadatacard properties.");

			Log.message("5. Assignment is approved in metadatacard properties.");

			//Verification : To verify if Approve is not displayed in taskpanel
			//--------------------------------------------------------------------
			if (!homePage.taskPanel.isItemExists(Caption.MenuItems.Approve.Value))
				Log.pass("Test case Passed. Approve is not displayed in taskpane after approving the assignment.");
			else
				Log.fail("Test case Failed. Approve is getting displayed after approving the assignment.", driver);

		} //End try
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_32_1C

	/**
	 * 104.1.32.1D : Verify if Approve option is removed in task pane when user perform Approve operation - Rightpane properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Verify if Approve option is removed in task pane when user perform Approve operation - Rightpane properties")
	public void SprintTest104_1_32_1D(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-4 : Open the Properties dialog of the new assignment through taskpanel
			//------------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Taskpanel.AssignedToMe.Value, ""); //Navigates to Assigned to me view

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in Assigned to me view.");

			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of the new assignment (" + assigName + ") is opened in rightpane.");

			//Step-5 : Approve the assignment in metadatacard
			//-------------------------------------------------
			if (!metadatacard.clickApproveIcon()) //Clicks Approve icon in metadatacard
				throw new Exception("Approve Icon is not clicked.");

			metadatacard.saveAndClose(); //Saves and closes the metadatacard.
			metadatacard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (!metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment completed icon is displayed
				throw new Exception("Assignment is not approved in metadatacard properties.");

			Log.message("5. Assignment is approved in metadatacard properties.");

			//Verification : To verify if Approve is not displayed in taskpanel
			//--------------------------------------------------------------------
			if (!homePage.taskPanel.isItemExists(Caption.MenuItems.Approve.Value))
				Log.pass("Test case Passed. Approve is not displayed in taskpane after approving the assignment.");
			else
				Log.fail("Test case Failed. Approve is getting displayed after approving the assignment.", driver);

		} //End try
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_32_1D

	/**
	 * 104.1.32.1E : Verify if Approve option is removed in task pane after approving assignment through taskpanel.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Verify if Approve option is removed in task pane after approving assignment through taskpanel.")
	public void SprintTest104_1_32_1E(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			homePage.taskPanel.markApproveReject("Approve"); //Marks Approve from Taskpanel //Clicks Approve from taskpanel
			metadatacard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (!metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment completed icon is displayed
				throw new Exception("Assignment is not approved in metadatacard properties.");

			Log.message("4. Assignment is approved from taskpanel.");

			//Verification : To verify if Approve is not displayed in taskpanel
			//--------------------------------------------------------------------
			if (!homePage.taskPanel.isItemExists(Caption.MenuItems.Approve.Value))
				Log.pass("Test case Passed. Approve is not displayed in taskpane after approving the assignment.");
			else
				Log.fail("Test case Failed. Approve is getting displayed after approving the assignment.", driver);

		} //End try
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_32_1E

	/**
	 * 104.1.35A : Verify if user can able to approve newly created assignment object - Context menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Verify if assignment can be marked as complete - Context menu Properties")
	public void SprintTest104_1_35A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment details are entered and saved the metadatacard.", driver);

			//Step-3 : Open the Properties dialog of the new assignment through context menu
			//------------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			if (!homePage.listView.rightClickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of the new assignment (" + assigName + ") is opened through context menu.");

			//Step-4 : Mark Complete the newly created assignment
			//----------------------------------------------------
			if (!metadatacard.clickApproveIcon()) //Clicks Complete Assignment icon.
				throw new Exception("Approve icon is not clicked.");

			Log.message("4. Approve icon is selected.");

			//Verification : Verifies if Mark Complete Icon is displayed
			//----------------------------------------------------------
			metadatacard.saveAndClose(); //Saves the metdatacard
			metadatacard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				Log.pass("Test case Passed. Newly created assignment is approved.");
			else
				Log.fail("Test case Failed. Newly created assignment is not approved.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_35A

	/**
	 * 104.1.35B : Verify if user can able to approve newly created assignment object - Operations menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Verify if assignment can be marked as complete - Operations menu Properties")
	public void SprintTest104_1_35B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Class name
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

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from operations menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of the new assignment (" + assigName + ") is opened through operations menu.");

			//Step-4 : Mark Complete the newly created assignment
			//----------------------------------------------------
			if (!metadatacard.clickApproveIcon()) //Clicks Complete Assignment icon.
				throw new Exception("Approve icon is not clicked.");

			Log.message("4. Approve icon is selected.");

			//Verification : Verifies if Mark Complete Icon is displayed
			//----------------------------------------------------------
			metadatacard.saveAndClose(); //Saves the metdatacard
			metadatacard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				Log.pass("Test case Passed. Newly created assignment is approved.");
			else
				Log.fail("Test case Failed. Newly created assignment is not approved.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_35B

	/**
	 * 104.1.35C : Verify if user can able to approve newly created assignment object - Taskpanel Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Verify if assignment can be marked as complete - Taskpanel Properties")
	public void SprintTest104_1_35C(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment details are entered and saved the metadatacard.", driver);

			//Step-3 : Open the Properties dialog of the new assignment through taskpanel menu
			//------------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from taskpanel menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of the new assignment (" + assigName + ") is opened through taskpanel menu.");

			//Step-4 : Mark Complete the newly created assignment
			//----------------------------------------------------
			if (!metadatacard.clickApproveIcon()) //Clicks Complete Assignment icon.
				throw new Exception("Approve icon is not clicked.");

			Log.message("4. Approve icon is selected.");

			//Verification : Verifies if Mark Complete Icon is displayed
			//----------------------------------------------------------
			metadatacard.saveAndClose(); //Saves the metdatacard
			metadatacard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				Log.pass("Test case Passed. Newly created assignment is approved.");
			else
				Log.fail("Test case Failed. Newly created assignment is not approved.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_35C

	/**
	 * 104.1.35D : Verify if user can able to approve newly created assignment object - Rightpane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Verify if assignment can be marked as complete - Rightpane")
	public void SprintTest104_1_35D(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment details are entered and saved the metadatacard.", driver);

			//Step-3 : Open the Properties dialog of the new assignment through right pane
			//------------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of the new assignment (" + assigName + ") is opened through right pane.");

			//Step-4 : Mark Complete the newly created assignment
			//----------------------------------------------------
			if (!metadatacard.clickApproveIcon()) //Clicks Complete Assignment icon.
				throw new Exception("Approve icon is not clicked.");

			Log.message("4. Approve icon is selected.");

			//Verification : Verifies if Mark Complete Icon is displayed
			//----------------------------------------------------------
			metadatacard.saveAndClose(); //Saves the metdatacard
			metadatacard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				Log.pass("Test case Passed. Newly created assignment is approved.");
			else
				Log.fail("Test case Failed. Newly created assignment is not approved.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_35D

	/**
	 * 104.1.35E : Verify if user can able to approve newly created assignment object - Taskpanel
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Verify if assignment can be marked as complete - Taskpanel")
	public void SprintTest104_1_35E(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment details are entered and saved the metadatacard.", driver);

			//Step-3 : Select Approve icon from taskpanel
			//-------------------------------------------
			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.markApproveReject("Approve"); //Marks Approve from Taskpanel //Selects Approve from taskpanel menu

			Log.message("3. Approve is selected for the new assignment (" + assigName + ") from taskpanel.");

			//Verification : Verifies if Mark Complete Icon is displayed
			//----------------------------------------------------------
			metadatacard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				Log.pass("Test case Passed. Newly created assignment is approved.");
			else
				Log.fail("Test case Failed. Newly created assignment is not approved.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_35E

	/**
	 * 104.1.50A : Adding comments should be possible in metadatacard for single assignment opened through context menu after approving the assignment
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Adding comments should be possible in metadatacard for single assignment opened through context menu after approving the assignment")
	public void SprintTest104_1_50A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-4 : Select Approve icon for the assignment
			//-----------------------------------------------
			if (!metadatacard.clickApproveIcon())
				throw new Exception("Approve icon is not selected.");

			Log.message("4. Approve icon is selected");

			//Step-5 : Add comments to the assignment after selecting approve icon
			//--------------------------------------------------------------------
			String comments = Utility.getObjectName(methodName);
			metadatacard.setComments(comments);

			Log.message("5. Comments is entered assignment after selecting approve icon.", driver);

			//Step-6 : Save the assignment object
			//-----------------------------------
			metadatacard.saveAndClose(); 

			Log.message("6. Metadatacard is saved after selecting approve icon and entering comments.");

			//Verification : Verify if assignment is approved and comments are as entered
			//-----------------------------------------------------------------------------
			if (!ListView.isApprovedByItemName(driver, assigName)) //Verifies if assignment is approved
				throw new Exception("Test case Failed. Assignment is not approved after saving the changes to metadatacard.");

			if (ListView.getCommentsByItemName(driver, assigName).equalsIgnoreCase(comments)) //Verifies if comments are as entered
				Log.pass("Test case Passed. Comments entered in metadatacard opened through context menu after clicking approve icon is saved successfully.");
			else
				Log.fail("Test case Failed. Comments entered in metadatacard opened through context menu after clicking approve icon is not saved.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_50A

	/**
	 * 104.1.50B : Adding comments should be possible in metadatacard for single assignment opened through operations menu after approving the assignment
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Adding comments should be possible in metadatacard for single assignment opened through operations menu after approving the assignment")
	public void SprintTest104_1_50B(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-4 : Select Approve icon for the assignment
			//-----------------------------------------------
			if (!metadatacard.clickApproveIcon())
				throw new Exception("Approve icon is not selected.");

			Log.message("4. Approve icon is selected");

			//Step-5 : Add comments to the assignment after selecting approve icon
			//--------------------------------------------------------------------
			String comments = Utility.getObjectName(methodName);
			metadatacard.setComments(comments);

			Log.message("5. Comments is entered assignment after selecting approve icon.", driver);

			//Step-6 : Save the assignment object
			//-----------------------------------
			metadatacard.saveAndClose(); 

			Log.message("6. Metadatacard is saved after selecting approve icon and entering comments.");

			//Verification : Verify if assignment is approved and comments are as entered
			//-----------------------------------------------------------------------------
			if (!ListView.isApprovedByItemName(driver, assigName)) //Verifies if assignment is approved
				throw new Exception("Test case Failed. Assignment is not approved after saving the changes to metadatacard.");

			if (ListView.getCommentsByItemName(driver, assigName).equalsIgnoreCase(comments)) //Verifies if comments are as entered
				Log.pass("Test case Passed. Comments entered in metadatacard opened through operations menu after clicking approve icon is saved successfully.");
			else
				Log.fail("Test case Failed. Comments entered in metadatacard opened through operations menu after clicking approve icon is not saved.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_50B

	/**
	 * 104.1.50C : Adding comments should be possible in metadatacard for single assignment opened through taskpanel menu after approving the assignment
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Adding comments should be possible in metadatacard for single assignment opened through taskpanel menu after approving the assignment")
	public void SprintTest104_1_50C(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-4 : Select Approve icon for the assignment
			//-----------------------------------------------
			if (!metadatacard.clickApproveIcon())
				throw new Exception("Approve icon is not selected.");

			Log.message("4. Approve icon is selected");

			//Step-5 : Add comments to the assignment after selecting approve icon
			//--------------------------------------------------------------------
			String comments = Utility.getObjectName(methodName);
			metadatacard.setComments(comments);

			Log.message("5. Comments is entered assignment after selecting approve icon.", driver);

			//Step-6 : Save the assignment object
			//-----------------------------------
			metadatacard.saveAndClose(); 

			Log.message("6. Metadatacard is saved after selecting approve icon and entering comments.");

			//Verification : Verify if assignment is approved and comments are as entered
			//-----------------------------------------------------------------------------
			if (!ListView.isApprovedByItemName(driver, assigName)) //Verifies if assignment is approved
				throw new Exception("Test case Failed. Assignment is not approved after saving the changes to metadatacard.");

			if (ListView.getCommentsByItemName(driver, assigName).equalsIgnoreCase(comments)) //Verifies if comments are as entered
				Log.pass("Test case Passed. Comments entered in metadatacard opened through taskpanel menu after clicking approve icon is saved successfully.");
			else
				Log.fail("Test case Failed. Comments entered in metadatacard opened through taskpanel menu after clicking approve icon is not saved.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_50C

	/**
	 * 104.1.50D : Adding comments should be possible in metadatacard for single assignment opened in rightpane after approving the assignment
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Adding comments should be possible in metadatacard for single assignment opened in rightpane after approving the assignment")
	public void SprintTest104_1_50D(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-4 : Select Approve icon for the assignment
			//-----------------------------------------------
			if (!metadatacard.clickApproveIcon())
				throw new Exception("Approve icon is not selected.");

			Log.message("4. Approve icon is selected");

			//Step-5 : Add comments to the assignment after selecting approve icon
			//--------------------------------------------------------------------
			String comments = Utility.getObjectName(methodName);
			metadatacard.setComments(comments);

			Log.message("5. Comments is entered assignment after selecting approve icon.", driver);

			//Step-6 : Save the assignment object
			//-----------------------------------
			metadatacard.saveAndClose(); 

			Log.message("6. Metadatacard is saved after selecting approve icon and entering comments.");

			//Verification : Verify if assignment is approved and comments are as entered
			//-----------------------------------------------------------------------------
			if (!ListView.isApprovedByItemName(driver, assigName)) //Verifies if assignment is approved
				throw new Exception("Test case Failed. Assignment is not approved after saving the changes to metadatacard.");

			if (ListView.getCommentsByItemName(driver, assigName).equalsIgnoreCase(comments)) //Verifies if comments are as entered
				Log.pass("Test case Passed. Comments entered in metadatacard opened in rightpane after clicking approve icon is saved successfully.");
			else
				Log.fail("Test case Failed. Comments entered in metadatacard opened in rightpane after clicking approve icon is not saved.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_50D

	/**
	 * 104.1.59.1A : Approve option should not available in metadatacard opened through context menu properties for non-assigned users.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Approve option should not available in metadatacard opened through context menu properties for non-assigned users.")
	public void SprintTest104_1_59_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-6 : Click Approve icon by non-assigned user
			//-----------------------------------------------------
			if (!metadatacard.isNotApprovedDisplayed()) //Verifies if Approve icon is available
				throw new Exception("Not Approve icon is not displayed for non-assigned users.");

			if (!metadatacard.clickApproveIcon())
				throw new Exception("Approve icon is not selected by non-assigned user.");

			Log.message("5. Approve icon is selected by non-assigned user.", driver);

			//Step-7 : Click Save button
			//---------------------------
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("6. Save button is clicked.");

			//Verification : Verify if M-Files dialog with Access denied message is displayed
			//-------------------------------------------------------------------------------
			String message = metadatacard.getMfilesDialogMessage();

			if (message.toUpperCase().contains("ACCESS DENIED"))
				Log.pass("Test case Passed. Access denied message dialog opened on approving assignment through context menu properties for non-assigned users. Message : " + message);
			else
				Log.fail("Test case Failed.  Access denied message dialog does not opened on approving assignment through context menu properties for non-assigned users. Message : " + message, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_59_1A

	/**
	 * 104.1.59.1B : Approve option should not available in metadatacard opened through operations menu properties for non-assigned users.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Approve option should not available in metadatacard opened through operations menu properties for non-assigned users.")
	public void SprintTest104_1_59_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-6 : Click Approve icon by non-assigned user
			//-----------------------------------------------------
			if (!metadatacard.isNotApprovedDisplayed()) //Verifies if Approve icon is available
				throw new Exception("Not Approve icon is not displayed for non-assigned users.");

			if (!metadatacard.clickApproveIcon())
				throw new Exception("Approve icon is not selected by non-assigned user.");

			Log.message("5. Approve icon is selected by non-assigned user.", driver);

			//Step-7 : Click Save button
			//---------------------------
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("6. Save button is clicked.");

			//Verification : Verify if M-Files dialog with Access denied message is displayed
			//-------------------------------------------------------------------------------
			String message = metadatacard.getMfilesDialogMessage();

			if (message.toUpperCase().contains("ACCESS DENIED"))
				Log.pass("Test case Passed. Access denied message dialog opened on approving assignment through operations menu properties for non-assigned users. Message : " + message);
			else
				Log.fail("Test case Failed.  Access denied message dialog does not opened on approving assignment through operations menu properties for non-assigned users. Message : " + message, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_59_1B

	/**
	 * 104.1.59.1C : Approve option should not available in metadatacard opened through taskpanel menu properties for non-assigned users.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Approve option should not available in metadatacard opened through taskpanel menu properties for non-assigned users.")
	public void SprintTest104_1_59_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-6 : Click Approve icon by non-assigned user
			//-----------------------------------------------------
			if (!metadatacard.isNotApprovedDisplayed()) //Verifies if Approve icon is available
				throw new Exception("Not Approve icon is not displayed for non-assigned users.");

			if (!metadatacard.clickApproveIcon())
				throw new Exception("Approve icon is not selected by non-assigned user.");

			Log.message("5. Approve icon is selected by non-assigned user.", driver);

			//Step-7 : Click Save button
			//---------------------------
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("6. Save button is clicked.");

			//Verification : Verify if M-Files dialog with Access denied message is displayed
			//-------------------------------------------------------------------------------
			String message = metadatacard.getMfilesDialogMessage();

			if (message.toUpperCase().contains("ACCESS DENIED"))
				Log.pass("Test case Passed. Access denied message dialog opened on approving assignment through taskpane properties for non-assigned users. Message : " + message);
			else
				Log.fail("Test case Failed.  Access denied message dialog does not opened on approving assignment through taskpane properties for non-assigned users. Message : " + message, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_59_1C

	/**
	 * 104.1.59.1D : Approve option should not available in metadatacard opened in rightpane metadatacard for non-assigned users.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Approve option should not available in metadatacard opened in rightpane metadatacard for non-assigned users.")
	public void SprintTest104_1_59_1D(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-6 : Click Approve icon by non-assigned user
			//-----------------------------------------------------
			if (!metadatacard.isNotApprovedDisplayed()) //Verifies if Approve icon is available
				throw new Exception("Not Approve icon is not displayed for non-assigned users.");

			if (!metadatacard.clickApproveIcon())
				throw new Exception("Approve icon is not selected by non-assigned user.");

			Log.message("5. Approve icon is selected by non-assigned user.", driver);

			//Step-7 : Click Save button
			//---------------------------
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("6. Save button is clicked.");

			//Verification : Verify if M-Files dialog with Access denied message is displayed
			//-------------------------------------------------------------------------------
			String message = metadatacard.getMfilesDialogMessage();

			if (message.toUpperCase().contains("ACCESS DENIED"))
				Log.pass("Test case Passed. Access denied message dialog opened on approving assignment in right pane properties for non-assigned users. Message : " + message);
			else
				Log.fail("Test case Failed.  Access denied message dialog does not opened on approving assignment in rightpane properties for non-assigned users. Message : " + message, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_59_1D

	/**
	 * 104.1.59.1E : Approve option should not available in taskpanel for non-assigned users.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Approve option should not available in taskpanel for non-assigned users.")
	public void SprintTest104_1_59_1E(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Verification : Verify if Approve icon is available
			//--------------------------------------------------
			if (!homePage.taskPanel.isItemExists(Caption.MenuItems.Approve.Value)) //Verifies if Approve icon is available
				Log.pass("Test case Passed. Approve is not available in taskpanel for non-assigned users.");
			else
				Log.fail("Test case Failed. Approve is available in taskpanel for non-assigned users.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_59_1E

	/**
	 * 172015.3.60.1A : Assignment should get approved in metadatadatacard after checking out.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Assignment should get approved in metadatadatacard after checking out.")
	public void SprintTest172015_3_60_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-5 : Approve the checked out assignment
			//----------------------------------------------------
			if (!metadatacard.clickApproveIcon()) //Clicks Complete Assignment icon.
				throw new Exception("Approve icon is not clicked.");

			metadatacard.saveAndClose(); //Saves the metdatacard

			Log.message("5. Approve icon for assignment (" + assigName + ") in metadatacard is selected and saved.");

			//Verification : Verify if checked out assignment is in approved state
			//----------------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				Log.pass("Test case Passed. Checked out Assignment (" + assigName + ") is in approved state.");
			else
				Log.fail("Test case Failed. Checked out Assignment (" + assigName + ") is not in approved state.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_60_1A

	/**
	 * 172015.3.60.1B : Assignment should get approved in rightpane after checking out.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Assignment should get approved in rightpane after checking out.")
	public void SprintTest172015_3_60_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-5 : Approve the checked out assignment
			//----------------------------------------------------
			if (!metadatacard.clickApproveIcon()) //Clicks Complete Assignment icon.
				throw new Exception("Approve icon is not clicked.");

			metadatacard.saveAndClose(); //Saves the metdatacard

			Log.message("5. Approve icon for assignment (" + assigName + ") in rightpane is selected and saved.");

			//Verification : Verify if checked out assignment is in approved state
			//----------------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				Log.pass("Test case Passed. Checked out Assignment (" + assigName + ") is in approved state.");
			else
				Log.fail("Test case Failed. Checked out Assignment (" + assigName + ") is not in approved state.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_60_1B

	/**
	 * 172015.3.60.1C : Assignment should get approved in taskpanel after checking out.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Assignment should get approved in taskpanel after checking out.")
	public void SprintTest172015_3_60_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			homePage.taskPanel.markApproveReject("Approve"); //Marks Approve from Taskpanel

			Log.message("4. Approve assignment (" + assigName + ") in taskpanel is selected.");

			//Verification : Verify if checked out assignment is in approved state
			//----------------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				Log.pass("Test case Passed. Checked out Assignment (" + assigName + ") is in approved state.");
			else
				Log.fail("Test case Failed. Checked out Assignment (" + assigName + ") is not in approved state.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_60_1C

	/**
	 * 172015.3.60.1D : Assignment that is created in checked-out mode should get approved in metadatacard.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Assignment that is created in checked-out mode should get approved in metadatacard.")
	public void SprintTest172015_3_60_1D(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-4 : Approve the checked out assignment
			//----------------------------------------------------
			if (!metadatacard.clickApproveIcon()) //Clicks Complete Assignment icon.
				throw new Exception("Approve icon is not clicked.");

			metadatacard.saveAndClose(); //Saves the metdatacard

			Log.message("4. Approve icon for assignment (" + assigName + ") in rightpane is selected and saved.");

			//Verification : Verify if checked out assignment is in approved state
			//----------------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				Log.pass("Test case Passed. Checked out Assignment (" + assigName + ") is in approved state.");
			else
				Log.fail("Test case Failed. Checked out Assignment (" + assigName + ") is not in approved state.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_60_1D

	/**
	 * 172015.3.60.1E : Assignment that is created in checked-out mode should get approved in taskpanel
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Assignment that is created in checked-out mode should get approved in taskpanel")
	public void SprintTest172015_3_60_1E(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-3 : Approve the checked out assignment
			//--------------------------------------------
			homePage.taskPanel.markApproveReject("Approve"); //Marks Approve from Taskpanel

			Log.message("3. Approve for assignment (" + assigName + ") in taskpanel is selected.");

			//Verification : Verify if checked out assignment is in approved state
			//----------------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				Log.pass("Test case Passed. Checked out Assignment (" + assigName + ") is in approved state.");
			else
				Log.fail("Test case Failed. Checked out Assignment (" + assigName + ") is not in approved state.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_60_1E

	/**
	 * 172015.3.60.2A : Checked out Approved Assignment through metadatacard should be in approved state after performing check in operation.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Checked out Approved Assignment through metadatacard should be in approved state after performing check in operation.")
	public void SprintTest172015_3_60_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-5 : Approve the checked out assignment
			//----------------------------------------------------
			if (!metadatacard.clickApproveIcon()) //Clicks Complete Assignment icon.
				throw new Exception("Approve icon is not clicked.");

			metadatacard.saveAndClose(); //Saves the metdatacard
			metadatacard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (!metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				throw new Exception("Checked out assignment (" + assigName + ") is not approved by user (" + userFullName + ").");

			Log.message("5. Assignment (" + assigName + ") is approved in rightpane.");

			//Step-6 : Check in the approved assignment
			//-----------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value); //Selects Checkout from taskpanel menu

			if (ListView.isCheckedOutByItemName(driver, assigName))
				throw new Exception("Assignment (" + assigName + ") is not checked in.");

			Log.message("6. Approved assignment (" + assigName + ") is checked in.");

			//Verification : Verify if checked out assignment is in approved state
			//----------------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				Log.pass("Test case Passed. Checked out approved assignment (" + assigName + ") through metadatacard is in approved state after performing check-in operation.");
			else
				Log.fail("Test case Failed. Checked out approved assignment (" + assigName + ") through metadatacard is not in approved state after performing check-in operation.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_60_2A

	/**
	 * 172015.3.60.2B : Checked out Approved Assignment through taskpanel should be in approved state after performing check in operation.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Checked out Approved Assignment through taskpanel should be in approved state after performing check in operation.")
	public void SprintTest172015_3_60_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-4 : Approve the checked out assignment through taskapanel
			//--------------------------------------------------------------
			homePage.taskPanel.markApproveReject("Approve"); //Marks Approve from Taskpanel//Clicks Approve from taskpanel

			metadatacard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (!metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				throw new Exception("Checked out assignment (" + assigName + ") is not approved by user (" + userFullName + ").");

			Log.message("4. Checked out assignment (" + assigName + "). is approved through taskpanel.");

			//Step-5 : Check in the approved assignment
			//-----------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value); //Selects Checkout from taskpanel menu

			if (ListView.isCheckedOutByItemName(driver, assigName))
				throw new Exception("Assignment (" + assigName + ") is not checked in.");

			Log.message("5. Approved assignment (" + assigName + ") is checked in.");

			//Verification : Verify if checked out assignment is in approved state
			//----------------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				Log.pass("Test case Passed. Checked out approved assignment (" + assigName + ") through taskpanel is in approved state after performing check-in operation.");
			else
				Log.fail("Test case Failed. Checked out approved assignment (" + assigName + ") through taskpanel is not in approved state after performing check-in operation.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_60_2B

	/**
	 * 172015.3.60.2C : Assignment that is created in checked-out that is approved in metadatacard and remain same after perfomring check-in operation
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Assignment that is created in checked-out that is approved in metadatacard and remain same after perfomring check-in operation.")
	public void SprintTest172015_3_60_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-4 : Approve the checked out assignment
			//----------------------------------------------------
			if (!metadatacard.clickApproveIcon()) //Clicks Complete Assignment icon.
				throw new Exception("Approve icon is not clicked.");

			metadatacard.saveAndClose(); //Saves the metdatacard
			metadatacard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (!metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				throw new Exception("Checked out assignment (" + assigName + ") is not approved by user (" + userFullName + ").");

			Log.message("4. Checked out assignment (" + assigName + ") is approved in rightpane.");

			//Step-5 : Check in the approved assignment
			//-----------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value); //Selects Checkout from taskpanel menu

			if (ListView.isCheckedOutByItemName(driver, assigName))
				throw new Exception("Assignment (" + assigName + ") is not checked in.");

			Log.message("5. Approved assignment (" + assigName + ") is checked in.");

			//Verification : Verify if checked out assignment is in approved state
			//----------------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				Log.pass("Test case Passed. Assignment(" + assigName + ") created in checked out mode and approved through metadatacard is in approved state after performing check-in operation.");
			else
				Log.fail("Test case Failed. Assignment(" + assigName + ") created in checked out mode and approved through metadatacard is not in approved state after performing check-in operation.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_60_2C

	/**
	 * 172015.3.60.2D : Assignment that is created in checked-out that is approved in taskpanel and remain same after perfomring check-in operation
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Assignment that is created in checked-out that is approved in taskpanel and remain same after perfomring check-in operation.")
	public void SprintTest172015_3_60_2D(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-3 : Approve the checked out assignment through taskapanel
			//--------------------------------------------------------------
			homePage.taskPanel.markApproveReject("Approve"); //Marks Approve from Taskpanel//Clicks Approve from taskpanel

			if (!ListView.getMarkedAsCompleteByItemName(driver, assigName).equalsIgnoreCase(userFullName)) //Checks if Assignment is approved
				throw new Exception("Checked out assignment (" + assigName + ") is not approved by user (" + userFullName + ").");

			Log.message("3. Checked out assignment (" + assigName + "). is approved through taskpanel.");

			//Step-4 : Check in the approved assignment
			//------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value); //Selects Checkout from taskpanel menu

			if (ListView.isCheckedOutByItemName(driver, assigName))
				throw new Exception("Assignment (" + assigName + ") is not checked in.");

			Log.message("4. Approved assignment (" + assigName + ") is checked in.");

			//Verification : Verify if checked out assignment is in approved state
			//----------------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				Log.pass("Test case Passed. Assignment(" + assigName + ") created in checked out mode and approved through metadatacard is in approved state after performing check-in operation.");
			else
				Log.fail("Test case Failed. Assignment(" + assigName + ") created in checked out mode and approved through metadatacard is not in approved state after performing check-in operation.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_60_2D

	/**
	 * 172015.3.60.3A : Checked out Approved Assignment through metadatacard should be in un-approved state after performing undo-checkout operation.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Checked out Approved Assignment through metadatacard should be in un-approved state after performing undo-checkout operation.")
	public void SprintTest172015_3_60_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-5 : Approve the checked out assignment
			//----------------------------------------------------
			if (!metadatacard.clickApproveIcon()) //Clicks Complete Assignment icon.
				throw new Exception("Approve icon is not clicked.");

			metadatacard.saveAndClose(); //Saves the metdatacard
			metadatacard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (!metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				throw new Exception("Checked out assignment (" + assigName + ") is not approved by user (" + userFullName + ").");

			Log.message("5. Assignment (" + assigName + ") is approved in rightpane.");

			//Step-6 : Undo-Checkout the approved assignment
			//-----------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Checkout from taskpanel menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo-checkout operation

			if (ListView.isCheckedOutByItemName(driver, assigName))
				throw new Exception("Assignment (" + assigName + ") is not undo checked out.");

			Log.message("6. Approved assignment (" + assigName + ") is undo-checked out.");

			//Verification : Verify if checked out assignment is in approved state
			//----------------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (!metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				Log.pass("Test case Passed. Checked out approved assignment (" + assigName + ") through metadatacard is in un-approved state after performing undo check out operation.");
			else
				Log.fail("Test case Failed. Checked out approved assignment (" + assigName + ") through metadatacard is in approved state after performing undo check out operation.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_60_3A

	/**
	 * 172015.3.60.3B : Checked out Approved Assignment in taskpanel should be in un-approved state after performing undo-checkout operation.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Checked out Approved Assignment in taskpanel should be in un-approved state after performing undo-checkout operation.")
	public void SprintTest172015_3_60_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			homePage.taskPanel.markApproveReject("Approve"); //Marks Approve from Taskpanel  //Selects Approve from taskpanel

			if (!ListView.getMarkedAsCompleteByItemName(driver, assigName).equalsIgnoreCase(userFullName)) //Checks if Assignment is approved
				throw new Exception("Checked out assignment (" + assigName + ") is not approved by user (" + userFullName + ").");

			Log.message("4. Assignment (" + assigName + ") is approved from taskpanel.");

			//Step-6 : Undo-Checkout the approved assignment
			//-----------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Undo Checkout from taskpanel menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo-checkout operation

			if (ListView.isCheckedOutByItemName(driver, assigName))
				throw new Exception("Assignment (" + assigName + ") is not undo checked out.");

			Log.message("5. Approved assignment (" + assigName + ") is undo-checked out.");

			//Verification : Verify if checked out assignment is in approved state
			//----------------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (!metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				Log.pass("Test case Passed. Checked out approved assignment (" + assigName + ") through metadatacard is in un-approved state after performing undo check out operation.");
			else
				Log.fail("Test case Failed. Checked out approved assignment (" + assigName + ") through metadatacard is in approved state after performing undo check out operation.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_60_3B

	/**
	 * 172015.3.60.3C : Assignment that is created in checked-out mode that is approved through rightpane gets disappeared on performing undo-checkout operation
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Assignment that is created in checked-out mode that is approved through rightpane gets disappeared on performing undo-checkout operation.")
	public void SprintTest172015_3_60_3C(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-4 : Approve the checked out assignment
			//----------------------------------------------------
			if (!metadatacard.clickApproveIcon()) //Clicks Complete Assignment icon.
				throw new Exception("Approve icon is not clicked.");

			metadatacard.saveAndClose(); //Saves the metdatacard
			metadatacard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (!metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				throw new Exception("Checked out assignment (" + assigName + ") is not approved by user (" + userFullName + ").");

			Log.message("4. Checked out assignment (" + assigName + ") is approved in rightpane.");

			//Step-5 : Undo-Checkout the approved assignment
			//-----------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Checkout from taskpanel menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo-checkout operation

			Log.message("5. Approved assignment (" + assigName + ") is undo-checked out.");

			//Verification : Verify if checked out assignment is in approved state
			//----------------------------------------------------------------------
			homePage.searchPanel.search(assigName, Caption.Search.SearchOnlyAssignments.Value);

			if (!homePage.listView.isItemExists(assigName)) //Checks if Assignment is approved
				Log.pass("Test case Passed. Assignment(" + assigName + ") created in checked out mode and approved through metadatacard does not exists in list after performing undo-checkout operation.");
			else
				Log.fail("Test case Failed. Assignment(" + assigName + ") created in checked out mode and approved through metadatacard exists in list after performing undo-checkout operation.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_60_3C

	/**
	 * 172015.3.60.3D: Assignment that is created in checked-out mode that is approved in taskpanel gets disappeared on performing undo-checkout operation
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Assignment that is created in checked-out mode that is approved in taskpanel gets disappeared on performing undo-checkout operation.")
	public void SprintTest172015_3_60_3D(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			homePage.taskPanel.markApproveReject("Approve"); //Marks Approve from Taskpanel 

			if (!ListView.getMarkedAsCompleteByItemName(driver, assigName).equalsIgnoreCase(userFullName)) //Checks if Assignment is approved
				throw new Exception("Checked out assignment (" + assigName + ") is not approved by user (" + userFullName + ").");

			Log.message("3. Checked out assignment (" + assigName + ") is approved through taskpanel.");

			//Step-4 : Undo-Checkout the approved assignment
			//-----------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Checkout from taskpanel menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo-checkout operation

			Log.message("4. Approved assignment (" + assigName + ") is undo-checked out.");

			//Verification : Verify if checked out assignment is in approved state
			//----------------------------------------------------------------------
			homePage.searchPanel.search(assigName, Caption.Search.SearchOnlyAssignments.Value);

			if (!homePage.listView.isItemExists(assigName)) //Checks if Assignment is approved
				Log.pass("Test case Passed. Assignment(" + assigName + ") created in checked out mode and approved through metadatacard does not exists in list after performing undo-checkout operation.");
			else
				Log.fail("Test case Failed. Assignment(" + assigName + ") created in checked out mode and approved through metadatacard exists in list after performing undo-checkout operation.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_60_3D

} //End Class Assignments
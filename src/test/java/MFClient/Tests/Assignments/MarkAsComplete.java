package MFClient.Tests.Assignments;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
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
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class MarkAsComplete {

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
	 * 104.1.1A : Verify if Mark complete icon is displayed in Metadata card for Single assignee - Context menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Verify if Mark complete icon is displayed in Metadata card for Single assignee - Context menu Properties")
	public void SprintTest104_1_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentBasicClass.Value); //Sets the class
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

			//Verification : Verifies if Mark Complete Icon is displayed
			//----------------------------------------------------------
			if (!metadatacard.isAssignmentCompleted())
				Log.pass("Test case Passed. Mark Complete Icon is displayed in metadatacard.");
			else
				Log.fail("Test case Failed. Mark Complete Icon is not displayed in metadatacard.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_1A

	/**
	 * 104.1.1B : Verify if Mark complete icon is displayed in Metadata card for Single assignee - Operations menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Verify if Mark complete icon is displayed in Metadata card for Single assignee - Operations menu Properties")
	public void SprintTest104_1_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentBasicClass.Value); //Sets the class
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

			//Verification : Verifies if Mark Complete Icon is displayed
			//----------------------------------------------------------
			if (!metadatacard.isAssignmentCompleted())
				Log.pass("Test case Passed. Mark Complete Icon is displayed in metadatacard.");
			else
				Log.fail("Test case Failed. Mark Complete Icon is not displayed in metadatacard.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_1B

	/**
	 * 104.1.1C : Verify if Mark complete icon is displayed in Metadata card for Single assignee - Taskpane Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Verify if Mark complete icon is displayed in Metadata card for Single assignee - Taskpane Properties")
	public void SprintTest104_1_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentBasicClass.Value); //Sets the class
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment details are entered and saved the metadatacard.", driver);

			//Step-3 : Open the Properties dialog of the new assignment through taskpane
			//------------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of the new assignment (" + assigName + ") is opened through taskpane.");

			//Verification : Verifies if Mark Complete Icon is displayed
			//----------------------------------------------------------
			if (!metadatacard.isAssignmentCompleted())
				Log.pass("Test case Passed. Mark Complete Icon is displayed in metadatacard.");
			else
				Log.fail("Test case Failed. Mark Complete Icon is not displayed in metadatacard.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_1C

	/**
	 * 104.1.1D : Verify if Mark complete icon is displayed in Metadata card for Single assignee - Right pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Verify if Mark complete icon is displayed in Metadata card for Single assignee - Right pane")
	public void SprintTest104_1_1D(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentBasicClass.Value); //Sets the class
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment details are entered and saved the metadatacard.", driver);

			//Step-3 : Open the Properties dialog of the new assignment through rightpane
			//------------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of the new assignment (" + assigName + ") is opened through rightpane.");

			//Verification : Verifies if Mark Complete Icon is displayed
			//----------------------------------------------------------
			if (!metadatacard.isAssignmentCompleted())
				Log.pass("Test case Passed. Mark Complete Icon is displayed in metadatacard.");
			else
				Log.fail("Test case Failed. Mark Complete Icon is not displayed in metadatacard.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_1D

	/**
	 * 104.1.2A : Verify if assignment can be marked as complete - Context menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Verify if assignment can be marked as complete - Context menu Properties")
	public void SprintTest104_1_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentBasicClass.Value); //Sets the class
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
				throw new Exception("Mark Complete icon is not clicked.");

			Log.message("4. Mark complete icon is selected.");

			//Verification : Verifies if Mark Complete Icon is displayed
			//----------------------------------------------------------
			if (metadatacard.isAssignmentCompleted()) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Mark Completed Icon is displayed in metadatacard.");
			else
				Log.fail("Test case Failed. Mark Completed Icon is not displayed in metadatacard.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_2A

	/**
	 * 104.1.2B : Verify if assignment can be marked as complete - Operations menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Verify if assignment can be marked as complete - Operations menu Properties")
	public void SprintTest104_1_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentBasicClass.Value); //Sets the class
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

			//Step-4 : Mark Complete the newly created assignment
			//----------------------------------------------------
			if (!metadatacard.clickApproveIcon()) //Clicks Complete Assignment icon.
				throw new Exception("Mark Complete icon is not clicked.");

			Log.message("4. Mark complete icon is selected.");

			//Verification : Verifies if Mark Complete Icon is displayed
			//----------------------------------------------------------
			if (metadatacard.isAssignmentCompleted()) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Mark Completed Icon is displayed in metadatacard.");
			else
				Log.fail("Test case Failed. Mark Completed Icon is not displayed in metadatacard.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_2B

	/**
	 * 104.1.2C : Verify if assignment can be marked as complete - Taskpane Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Verify if assignment can be marked as complete - Taskpane Properties")
	public void SprintTest104_1_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentBasicClass.Value); //Sets the class
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment details are entered and saved the metadatacard.", driver);

			//Step-3 : Open the Properties dialog of the new assignment through taskpane
			//------------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of the new assignment (" + assigName + ") is opened through taskpane.");

			//Step-4 : Mark Complete the newly created assignment
			//----------------------------------------------------
			if (!metadatacard.clickApproveIcon()) //Clicks Complete Assignment icon.
				throw new Exception("Mark Complete icon is not clicked.");

			Log.message("4. Mark complete icon is selected.");

			//Verification : Verifies if Mark Complete Icon is displayed
			//----------------------------------------------------------
			if (metadatacard.isAssignmentCompleted()) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Mark Completed Icon is displayed in metadatacard.");
			else
				Log.fail("Test case Failed. Mark Completed Icon is not displayed in metadatacard.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_2C

	/**
	 * 104.1.2D : Verify if assignment can be marked as complete - Rightpane Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Verify if assignment can be marked as complete - Rightpane Properties")
	public void SprintTest104_1_2D(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentBasicClass.Value); //Sets the class
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment details are entered and saved the metadatacard.", driver);

			//Step-3 : Open the Properties dialog of the new assignment through rightpane
			//------------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of the new assignment (" + assigName + ") is opened through rightpane.");

			//Step-4 : Mark Complete the newly created assignment
			//----------------------------------------------------
			if (!metadatacard.clickApproveIcon()) //Clicks Complete Assignment icon.
				throw new Exception("Mark Complete icon is not clicked.");

			Log.message("4. Mark complete icon is selected.");

			//Verification : Verifies if Mark Complete Icon is displayed
			//----------------------------------------------------------
			if (metadatacard.isAssignmentCompleted()) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Mark Completed Icon is displayed in metadatacard.");
			else
				Log.fail("Test case Failed. Mark Completed Icon is not displayed in metadatacard.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_2D

	/**
	 * 104.1.23A : Verify if one assignee can be marked as complete for Assignment objects - Context menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Verify if one assignee can be marked as complete for Assignment objects - Context menu Properties")
	public void SprintTest104_1_23A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentBasicClass.Value); //Sets the class
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName")); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			driver.switchTo().defaultContent();

			Log.message("2. New Assignment details are entered and saved the metadatacard.", driver);

			//Step-3 : Logout and login with new user
			//--------------------------------------
			if (!Utility.logOut(driver)) //Logs out from the vault
				throw new Exception("Logout is not successful");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), testVault); //Launched driver and logged in

			Log.message("3. Logged out and logged in with new user (" + dataPool.get("UserName") + ").");

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
			if (!metadatacard.clickApproveIcon()) //Clicks Complete Assignment icon.
				throw new Exception("Mark Complete icon is not clicked.");

			metadatacard.saveAndClose(); //Clicked Save button in metadatacard.

			Log.message("5. Mark complete icon is selected.");

			//Verification : Verifies if Assignment is mark completed
			//----------------------------------------------------------
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (metadatacard.isSelectedObjectCompletedByUser(dataPool.get("UserFullName"))) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment (" + assigName + ") is marked as completed.");
			else
				Log.fail("Test case Failed. Assignment (" + assigName + ") is not marked as completed.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_23A

	/**
	 * 104.1.23B : Verify if one assignee can be marked as complete for Assignment objects - Operations menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Verify if one assignee can be marked as complete for Assignment objects - Operations menu Properties")
	public void SprintTest104_1_23B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentBasicClass.Value); //Sets the class
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName")); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment details are entered and saved the metadatacard.", driver);

			//Step-3 : Logout and login with new user
			//--------------------------------------
			if (!Utility.logOut(driver)) //Logs out from the vault
				throw new Exception("Logout is not successful");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), testVault); //Launched driver and logged in

			Log.message("3. Logged out and logged in with new user (" + dataPool.get("UserName") + ").");

			//Step-4 : Open the Properties dialog of the new assignment through operations menu
			//------------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Taskpanel.AssignedToMe.Value, "");

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of the new assignment (" + assigName + ") is opened through operations menu.");

			//Step-5 : Mark Complete the newly created assignment
			//----------------------------------------------------
			if (!metadatacard.clickApproveIcon()) //Clicks Complete Assignment icon.
				throw new Exception("Mark Complete icon is not clicked.");

			metadatacard.saveAndClose(); //Clicked Save button in metadatacard.

			Log.message("5. Mark complete icon is selected.");

			//Verification : Verifies if Assignment is mark completed
			//----------------------------------------------------------
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper
			if (metadatacard.isSelectedObjectCompletedByUser(dataPool.get("UserFullName"))) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment (" + assigName + ") is marked as completed.");
			else
				Log.fail("Test case Failed. Assignment (" + assigName + ") is not marked as completed.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_23B

	/**
	 * 104.1.23C : Verify if one assignee can be marked as complete for Assignment objects - Taskpanel Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Verify if one assignee can be marked as complete for Assignment objects - Taskpanel Properties")
	public void SprintTest104_1_23C(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentBasicClass.Value); //Sets the class
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName")); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment details are entered and saved the metadatacard.", driver);

			//Step-3 : Logout and login with new user
			//--------------------------------------
			if (!Utility.logOut(driver)) //Logs out from the vault
				throw new Exception("Logout is not successful");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), testVault); //Launched driver and logged in

			Log.message("3. Logged out and logged in with new user (" + dataPool.get("UserName") + ").");

			//Step-4 : Open the Properties dialog of the new assignment through operations menu
			//------------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Taskpanel.AssignedToMe.Value, "");

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from task panel

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of the new assignment (" + assigName + ") is opened through task panel.");

			//Step-5 : Mark Complete the newly created assignment
			//----------------------------------------------------
			if (!metadatacard.clickApproveIcon()) //Clicks Complete Assignment icon.
				throw new Exception("Mark Complete icon is not clicked.");

			metadatacard.saveAndClose(); //Clicked Save button in metadatacard.

			Log.message("5. Mark complete icon is selected.");

			//Verification : Verifies if Assignment is mark completed
			//----------------------------------------------------------
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (metadatacard.isSelectedObjectCompletedByUser(dataPool.get("UserFullName"))) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment (" + assigName + ") is marked as completed.");
			else
				Log.fail("Test case Failed. Assignment (" + assigName + ") is not marked as completed.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_23C

	/**
	 * 104.1.23D : Verify if one assignee can be marked as complete for Assignment objects - Right pane Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Verify if one assignee can be marked as complete for Assignment objects - Right pane Properties")
	public void SprintTest104_1_23D(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentBasicClass.Value); //Sets the class
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName")); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment details are entered and saved the metadatacard.", driver);

			//Step-3 : Logout and login with new user
			//--------------------------------------
			if (!Utility.logOut(driver)) //Logs out from the vault
				throw new Exception("Logout is not successful");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), testVault); //Launched driver and logged in

			Log.message("3. Logged out and logged in with new user (" + dataPool.get("UserName") + ").");

			//Step-4 : Open the Properties dialog of the new assignment through rightpane
			//------------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Taskpanel.AssignedToMe.Value, "");

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of the new assignment (" + assigName + ") is opened through rightpane.");

			//Step-5 : Mark Complete the newly created assignment
			//----------------------------------------------------
			if (!metadatacard.clickApproveIcon()) //Clicks Complete Assignment icon.
				throw new Exception("Mark Complete icon is not clicked.");

			metadatacard.saveAndClose(); //Clicked Save button in metadatacard.

			Log.message("5. Mark complete icon is selected.");

			//Verification : Verifies if Assignment is mark completed
			//----------------------------------------------------------
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (metadatacard.isSelectedObjectCompletedByUser(dataPool.get("UserFullName"))) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment (" + assigName + ") is marked as completed.");
			else
				Log.fail("Test case Failed. Assignment (" + assigName + ") is not marked as completed.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_23D

	/**
	 * 104.1.23E : Verify if one assignee can be marked as complete for Assignment objects - Taskpanel Mark Complete
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Verify if one assignee can be marked as complete for Assignment objects - Taskpanel Mark Complete")
	public void SprintTest104_1_23E(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentBasicClass.Value); //Sets the class
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName")); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment details are entered and saved the metadatacard.", driver);

			//Step-3 : Logout and login with new user
			//--------------------------------------
			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			if (!Utility.logOut(driver)) //Logs out from the vault
				throw new Exception("Logout is not successful");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), testVault); //Launched driver and logged in

			Log.message("3. Logged out and logged in with new user (" + dataPool.get("UserName") + ").");

			//Step-4 : Open the Properties dialog of the new assignment through operations menu
			//------------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Taskpanel.AssignedToMe.Value, "");

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.markApproveReject("Approve"); //Marks Approve from Taskpanel 

			Log.message("4. New assignment (" + assigName + ") is selected and Mark Complete is clicked through task panel.");

			//Step-5 : Navigate to the 'Search only:Assignments' view
			//-------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyAssignments.Value, "");

			Log.message("5. Navigate to the " + Caption.Search.SearchOnlyAssignments.Value + " view.");

			homePage.listView.clickItem(assigName);

			//Verification : Verifies if Assignment is mark completed
			//----------------------------------------------------------
			metadatacard = new MetadataCard(driver, true);//Instantiates the metadata card

			if (metadatacard.isSelectedObjectCompletedByUser(dataPool.get("UserFullName"))) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment (" + assigName + ") is marked as completed.");
			else
				Log.fail("Test case Failed. Assignment (" + assigName + ") is not marked as completed.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_23E

} //End Class Assignments
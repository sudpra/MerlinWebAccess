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
public class ChangeClass {

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
	 * 172015.3.58A : Approved assignment should remain same on changing the class in metdatacard through context menu.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Approved assignment should remain same on changing the class in metdatacard through context menu.")
	public void SprintTest172015_3_58A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", dataPool.get("Class1")); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			Log.message("2. New Assignment(" + assigName + ") is created with class (" + dataPool.get("Class1") + ").", driver);

			//Step-3 : Open the Properties dialog of the new assignment from context menu
			//-----------------------------------------------------------------------
			if (!homePage.listView.rightClickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of the new assignment (" + assigName + ") is opened through context menu.");

			//Step-4 : Approve the newly created assignment
			//----------------------------------------------------
			if (!metadatacard.clickApproveIcon()) //Clicks Complete Assignment icon.
				throw new Exception("Approve icon is not clicked.");

			metadatacard.saveAndClose(); //Saves the metdatacard
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if(!metadatacard.isSelectedObjectApprovedByUser(userFullName))
				throw new Exception("Assignment (" + assigName + ") is not approved by user (" + userFullName + ").");

			Log.message("4. Assignment (" + assigName + ") is approved by user (" + userFullName + ").");

			//Step-5 : Change the class of the approved assignment
			//----------------------------------------------------
			if (!homePage.listView.rightClickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			metadatacard.setPropertyValue("Class", dataPool.get("Class2")); //Sets the Class name
			metadatacard.saveAndClose(); //Saves the metdatacard

			Log.message("5. Approved assignment (" + assigName + ") class is changed to '" + dataPool.get("Class2") + "' and saved.", driver);

			//Verification : Verify if copied assignment is in approved state
			//----------------------------------------------------------
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if(metadatacard.isSelectedObjectApprovedByUser(userFullName))
				Log.pass("Test case Passed. Approved assignment is in same state after changing its class in metadatacard opened through context menu.");
			else
				Log.fail("Test case Failed. Approved assignment is not in same state after changing its class in metadatacard opened through context menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_58A

	/**
	 * 172015.3.58B : Approved assignment should remain same on changing the class in metdatacard through operations menu.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Approved assignment should remain same on changing the class in metdatacard through operations menu.")
	public void SprintTest172015_3_58B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", dataPool.get("Class1")); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			Log.message("2. New Assignment(" + assigName + ") is created with class (" + dataPool.get("Class1") + ").", driver);

			//Step-3 : Open the Properties dialog of the new assignment from operations menu
			//-----------------------------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from operations menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of the new assignment (" + assigName + ") is opened through operations menu.");

			//Step-4 : Approve the newly created assignment
			//----------------------------------------------------
			if (!metadatacard.clickApproveIcon()) //Clicks Complete Assignment icon.
				throw new Exception("Approve icon is not clicked.");

			metadatacard.saveAndClose(); //Saves the metdatacard
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if(!metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				throw new Exception("Assignment (" + assigName + ") is not approved by user (" + userFullName + ").");

			Log.message("4. Assignment (" + assigName + ") is approved by user (" + userFullName + ").");

			//Step-5 : Change the class of the approved assignment
			//----------------------------------------------------
			if (!homePage.listView.rightClickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from operations menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			metadatacard.setPropertyValue("Class", dataPool.get("Class2")); //Sets the Class name
			metadatacard.saveAndClose(); //Saves the metdatacard

			Log.message("5. Approved assignment (" + assigName + ") class is changed to '" + dataPool.get("Class2") + "' and saved.", driver);

			//Verification : Verify if copied assignment is in approved state
			//----------------------------------------------------------
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if(metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				Log.pass("Test case Passed. Approved assignment is in same state after changing its class in metadatacard opened through operations menu.");
			else
				Log.fail("Test case Failed. Approved assignment is not in same state after changing its class in metadatacard opened through operations menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_58B

	/**
	 * 172015.3.58C : Approved assignment should remain same on changing the class in metdatacard through taskpanel menu.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Approved assignment should remain same on changing the class in metdatacard through taskpanel menu.")
	public void SprintTest172015_3_58C(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", dataPool.get("Class1")); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			Log.message("2. New Assignment(" + assigName + ") is created with class (" + dataPool.get("Class1") + ").", driver);

			//Step-3 : Open the Properties dialog of the new assignment from taskpanel menu
			//-----------------------------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from taskpanel menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of the new assignment (" + assigName + ") is opened through taskpanel menu.");

			//Step-4 : Approve the newly created assignment
			//----------------------------------------------------
			if (!metadatacard.clickApproveIcon()) //Clicks Complete Assignment icon.
				throw new Exception("Approve icon is not clicked.");

			metadatacard.saveAndClose(); //Saves the metdatacard
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if(!metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				throw new Exception("Assignment (" + assigName + ") is not approved by user (" + userFullName + ").");

			Log.message("4. Assignment (" + assigName + ") is approved by user (" + userFullName + ").");

			//Step-5 : Change the class of the approved assignment
			//----------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from taskpanel menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			metadatacard.setPropertyValue("Class", dataPool.get("Class2")); //Sets the Class name
			metadatacard.saveAndClose(); //Saves the metdatacard

			Log.message("5. Approved assignment (" + assigName + ") class is changed to '" + dataPool.get("Class2") + "' and saved.", driver);

			//Verification : Verify if copied assignment is in approved state
			//----------------------------------------------------------
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper
			if(metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				Log.pass("Test case Passed. Approved assignment is in same state after changing its class in metadatacard opened through taskpanel menu.");
			else
				Log.fail("Test case Failed. Approved assignment is not in same state after changing its class in metadatacard opened through taskpanel menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_58C

	/**
	 * 172015.3.58D : Approved assignment should remain same on changing the class in metdatacard in rightpane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Approved assignment should remain same on changing the class in metdatacard through  in rightpane.")
	public void SprintTest172015_3_58D(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", dataPool.get("Class1")); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			Log.message("2. New Assignment(" + assigName + ") is created with class (" + dataPool.get("Class1") + ").", driver);

			//Step-3 : Open the Properties dialog of the new assignment  in rightpane
			//-----------------------------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of the new assignment (" + assigName + ") is opened  in rightpane.");

			//Step-4 : Approve the newly created assignment
			//----------------------------------------------------
			if (!metadatacard.clickApproveIcon()) //Clicks Complete Assignment icon.
				throw new Exception("Approve icon is not clicked.");

			metadatacard.saveAndClose(); //Saves the metdatacard
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if(!metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				throw new Exception("Assignment (" + assigName + ") is not approved by user (" + userFullName + ").");

			Log.message("4. Assignment (" + assigName + ") is approved by user (" + userFullName + ").");

			//Step-5 : Change the class of the approved assignment
			//----------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			metadatacard.setPropertyValue("Class", dataPool.get("Class2")); //Sets the Class name
			metadatacard.saveAndClose(); //Saves the metdatacard

			Log.message("5. Approved assignment (" + assigName + ") class is changed to '" + dataPool.get("Class2") + "' and saved.", driver);

			//Verification : Verify if copied assignment is in approved state
			//----------------------------------------------------------
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if(metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				Log.pass("Test case Passed. Approved assignment is in same state after changing its class in metadatacard opened in rightpane.");
			else
				Log.fail("Test case Failed. Approved assignment is not in same state after changing its class in metadatacard opened in rightpane.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_58D

	/**
	 * 172015.3.58E : Assignment should remain same on changing the class after approving through taskpanel
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Assignment should remain same on changing the class after approving through taskpanel.")
	public void SprintTest172015_3_58E(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", dataPool.get("Class1")); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			Log.message("2. New Assignment(" + assigName + ") is created with class (" + dataPool.get("Class1") + ").", driver);

			//Step-3 : Approve the newly created assignment through taskpanel
			//--------------------------------------------------------------
			homePage.taskPanel.markApproveReject("Approve"); //Marks Approve from Taskpanel //Selects Approve from taskpanel
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if(!metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				throw new Exception("Assignment (" + assigName + ") is not approved by user (" + userFullName + ").");

			Log.message("3. Assignment (" + assigName + ") is approved by user (" + userFullName + ").");

			//Step-5 : Change the class of the approved assignment
			//----------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			metadatacard.setPropertyValue("Class", dataPool.get("Class2")); //Sets the Class name
			metadatacard.saveAndClose(); //Saves the metdatacard

			Log.message("4. Approved assignment (" + assigName + ") class is changed to '" + dataPool.get("Class2") + "' and saved.", driver);

			//Verification : Verify if copied assignment is in approved state
			//----------------------------------------------------------
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if(metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				Log.pass("Test case Passed. Approved assignment is in same state after changing its class in metadatacard opened in rightpane.");
			else
				Log.fail("Test case Failed. Approved assignment is not in same state after changing its class in metadatacard opened in rightpane.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_58E

	/**
	 * 172015.3.59A : Rejected assignment should remain same on changing the class in metdatacard through context menu.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Rejected assignment should remain same on changing the class in metdatacard through context menu.")
	public void SprintTest172015_3_59A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", dataPool.get("Class1")); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			Log.message("2. New Assignment(" + assigName + ") is created with class (" + dataPool.get("Class1") + ").", driver);

			//Step-3 : Open the Properties dialog of the new assignment from context menu
			//-----------------------------------------------------------------------
			if (!homePage.listView.rightClickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of the new assignment (" + assigName + ") is opened through context menu.");

			//Step-4 : Reject the newly created assignment
			//----------------------------------------------------
			if (!metadatacard.clickRejectIcon()) //Clicks Complete Assignment icon.
				throw new Exception("Reject icon is not clicked.");

			metadatacard.saveAndClose(); //Saves the metdatacard
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (!metadatacard.isSelectedObjectRejectedByUser(userFullName)) //Checks if Assignment is Rejected
				throw new Exception("Assignment (" + assigName + ") is not Rejected by user (" + userFullName + ").");

			Log.message("4. Assignment (" + assigName + ") is Rejected by user (" + userFullName + ").");

			//Step-5 : Change the class of the Rejected assignment
			//----------------------------------------------------
			if (!homePage.listView.rightClickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			metadatacard.setPropertyValue("Class", dataPool.get("Class2")); //Sets the Class name
			metadatacard.saveAndClose(); //Saves the metdatacard

			Log.message("5. Rejected assignment (" + assigName + ") class is changed to '" + dataPool.get("Class2") + "' and saved.", driver);

			//Verification : Verify if copied assignment is in Rejected state
			//----------------------------------------------------------
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (metadatacard.isSelectedObjectRejectedByUser(userFullName)) //Checks if Assignment is Rejected
				Log.pass("Test case Passed. Rejected assignment is in same state after changing its class in metadatacard opened through context menu.");
			else
				Log.fail("Test case Failed. Rejected assignment is not in same state after changing its class in metadatacard opened through context menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_59A

	/**
	 * 172015.3.59B : Rejected assignment should remain same on changing the class in metdatacard through operations menu.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Rejected assignment should remain same on changing the class in metdatacard through operations menu.")
	public void SprintTest172015_3_59B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", dataPool.get("Class1")); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			Log.message("2. New Assignment(" + assigName + ") is created with class (" + dataPool.get("Class1") + ").", driver);

			//Step-3 : Open the Properties dialog of the new assignment from operations menu
			//-----------------------------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from operations menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of the new assignment (" + assigName + ") is opened through operations menu.");

			//Step-4 : Reject the newly created assignment
			//----------------------------------------------------
			if (!metadatacard.clickRejectIcon()) //Clicks Complete Assignment icon.
				throw new Exception("Reject icon is not clicked.");

			metadatacard.saveAndClose(); //Saves the metdatacard
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (!metadatacard.isSelectedObjectRejectedByUser(userFullName)) //Checks if Assignment is Rejected
				throw new Exception("Assignment (" + assigName + ") is not Rejected by user (" + userFullName + ").");

			Log.message("4. Assignment (" + assigName + ") is Rejected by user (" + userFullName + ").");

			//Step-5 : Change the class of the Rejected assignment
			//----------------------------------------------------
			if (!homePage.listView.rightClickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from operations menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			metadatacard.setPropertyValue("Class", dataPool.get("Class2")); //Sets the Class name
			metadatacard.saveAndClose(); //Saves the metdatacard

			Log.message("5. Rejected assignment (" + assigName + ") class is changed to '" + dataPool.get("Class2") + "' and saved.", driver);

			//Verification : Verify if copied assignment is in Rejected state
			//----------------------------------------------------------
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (metadatacard.isSelectedObjectRejectedByUser(userFullName)) //Checks if Assignment is Rejected
				Log.pass("Test case Passed. Rejected assignment is in same state after changing its class in metadatacard opened through operations menu.");
			else
				Log.fail("Test case Failed. Rejected assignment is not in same state after changing its class in metadatacard opened through operations menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_59B

	/**
	 * 172015.3.59C : Rejected assignment should remain same on changing the class in metdatacard through taskpanel menu.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Rejected assignment should remain same on changing the class in metdatacard through taskpanel menu.")
	public void SprintTest172015_3_59C(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", dataPool.get("Class1")); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			Log.message("2. New Assignment(" + assigName + ") is created with class (" + dataPool.get("Class1") + ").", driver);

			//Step-3 : Open the Properties dialog of the new assignment from taskpanel menu
			//-----------------------------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from taskpanel menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of the new assignment (" + assigName + ") is opened through taskpanel menu.");

			//Step-4 : Reject the newly created assignment
			//----------------------------------------------------
			if (!metadatacard.clickRejectIcon()) //Clicks Complete Assignment icon.
				throw new Exception("Reject icon is not clicked.");

			metadatacard.saveAndClose(); //Saves the metdatacard
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (!metadatacard.isSelectedObjectRejectedByUser(userFullName)) //Checks if Assignment is Rejected
				throw new Exception("Assignment (" + assigName + ") is not Rejected by user (" + userFullName + ").");

			Log.message("4. Assignment (" + assigName + ") is Rejected by user (" + userFullName + ").");

			//Step-5 : Change the class of the Rejected assignment
			//----------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from taskpanel menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			metadatacard.setPropertyValue("Class", dataPool.get("Class2")); //Sets the Class name
			metadatacard.saveAndClose(); //Saves the metdatacard

			Log.message("5. Rejected assignment (" + assigName + ") class is changed to '" + dataPool.get("Class2") + "' and saved.", driver);

			//Verification : Verify if copied assignment is in Rejected state
			//----------------------------------------------------------
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (metadatacard.isSelectedObjectRejectedByUser(userFullName)) //Checks if Assignment is Rejected
				Log.pass("Test case Passed. Rejected assignment is in same state after changing its class in metadatacard opened through taskpanel menu.");
			else
				Log.fail("Test case Failed. Rejected assignment is not in same state after changing its class in metadatacard opened through taskpanel menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_59C

	/**
	 * 172015.3.59D : Rejected assignment should remain same on changing the class in metdatacard in rightpane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Rejected assignment should remain same on changing the class in metdatacard through  in rightpane.")
	public void SprintTest172015_3_59D(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", dataPool.get("Class1")); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			Log.message("2. New Assignment(" + assigName + ") is created with class (" + dataPool.get("Class1") + ").", driver);

			//Step-3 : Open the Properties dialog of the new assignment  in rightpane
			//-----------------------------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of the new assignment (" + assigName + ") is opened  in rightpane.");

			//Step-4 : Reject the newly created assignment
			//----------------------------------------------------
			if (!metadatacard.clickRejectIcon()) //Clicks Complete Assignment icon.
				throw new Exception("Reject icon is not clicked.");

			metadatacard.saveAndClose(); //Saves the metdatacard
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (!metadatacard.isSelectedObjectRejectedByUser(userFullName)) //Checks if Assignment is Rejected
				throw new Exception("Assignment (" + assigName + ") is not Rejected by user (" + userFullName + ").");

			Log.message("4. Assignment (" + assigName + ") is Rejected by user (" + userFullName + ").");

			//Step-5 : Change the class of the Rejected assignment
			//----------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			metadatacard.setPropertyValue("Class", dataPool.get("Class2")); //Sets the Class name
			metadatacard.saveAndClose(); //Saves the metdatacard

			Log.message("5. Rejected assignment (" + assigName + ") class is changed to '" + dataPool.get("Class2") + "' and saved.", driver);

			//Verification : Verify if copied assignment is in Rejected state
			//----------------------------------------------------------
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (metadatacard.isSelectedObjectRejectedByUser(userFullName)) //Checks if Assignment is Rejected
				Log.pass("Test case Passed. Rejected assignment is in same state after changing its class in metadatacard opened in rightpane.");
			else
				Log.fail("Test case Failed. Rejected assignment is not in same state after changing its class in metadatacard opened in rightpane.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_59D

	/**
	 * 172015.3.59E : Assignment should remain same on changing the class after rejecting through taskpanel
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Assignment should remain same on changing the class after rejecting through taskpanel.")
	public void SprintTest172015_3_59E(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", dataPool.get("Class1")); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			Log.message("2. New Assignment(" + assigName + ") is created with class (" + dataPool.get("Class1") + ").", driver);

			//Step-3 : Reject the newly created assignment through taskpanel
			//--------------------------------------------------------------
			homePage.taskPanel.markApproveReject("Reject"); //Selects Reject from taskpanel//Selects Reject from taskpanel
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (!metadatacard.isSelectedObjectRejectedByUser(userFullName)) //Checks if Assignment is Rejected
				throw new Exception("Assignment (" + assigName + ") is not Rejected by user (" + userFullName + ").");

			Log.message("3. Assignment (" + assigName + ") is Rejected by user (" + userFullName + ").");

			//Step-5 : Change the class of the Rejected assignment
			//----------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			metadatacard.setPropertyValue("Class", dataPool.get("Class2")); //Sets the Class name
			metadatacard.saveAndClose(); //Saves the metdatacard

			Log.message("4. Rejected assignment (" + assigName + ") class is changed to '" + dataPool.get("Class2") + "' and saved.", driver);

			//Verification : Verify if copied assignment is in Rejected state
			//----------------------------------------------------------
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper
			if (metadatacard.isSelectedObjectRejectedByUser(userFullName)) //Checks if Assignment is Rejected
				Log.pass("Test case Passed. Rejected assignment is in same state after changing its class in metadatacard opened in rightpane.");
			else
				Log.fail("Test case Failed. Rejected assignment is not in same state after changing its class in metadatacard opened in rightpane.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_59E

} //End Class Assignments
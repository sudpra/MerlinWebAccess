package MFClient.Tests.Assignments;

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
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class AnyAssigneeApprove {

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
	 * 172015.3.23.1A : User should be able to un-complete assignment in Metadatacard opened through context menu when assignment is created with ‘Anyone can approve’ class.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "User should be able to un-complete assignment in Metadatacard opened through context menu when assignment is created with ‘Anyone can approve’ class.")
	public void SprintTest172015_3_23_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentAnyoneCanApprove.Value); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			String userNames = userFullName + "," + dataPool.get("AssignedUsers");
			String[] userList = userNames.split(",");

			for (int loopIdx=0; loopIdx<userList.length; loopIdx++) //Assigned with multiple users. 
				metadatacard.setPropertyValue("Assigned to", userList[loopIdx].trim(), loopIdx+1); //Sets the assigned to property

			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			Log.message("2. New Assignment(" + assigName + ") is created with mutliple assigned to users.", driver);

			//Step-3 : Open the Properties dialog of the new assignment through context menu
			//------------------------------------------------------------------------------
			if (!homePage.listView.rightClickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of the new assignment (" + assigName + ") is opened through context menu.");

			//Step-4 : Mark Complete the newly created assignment
			//----------------------------------------------------
			int propIndex = metadatacard.getPropertyValueIndex("Assigned to", userFullName);

			if (!metadatacard.clickApproveIcon(propIndex)) //Clicks Complete Assignment icon.
				throw new Exception("Approve icon is not clicked.");

			metadatacard.saveAndClose(); //Saves the metdatacard
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (!metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				throw new Exception("Assignment (" + assigName + ") is not approved by user (" + userFullName + ").");

			Log.message("4. Assignment (" + assigName + ") is approved by user (" + userFullName + ").", driver);

			//Step-5 : Open the Approved assignment through context menu
			//----------------------------------------------------------
			if (!homePage.listView.rightClickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("5. Properties dialog of the approved assignment (" + assigName + ") is opened through context menu.");

			//Step-6 : Click Approve icon again to un-approve assignment
			//-----------------------------------------------------------
			propIndex = metadatacard.getPropertyValueIndex("Assigned to", userFullName);

			if (!metadatacard.clickApproveIcon(false, propIndex)) //Clicks Complete Assignment icon.
				throw new Exception("Approve icon is not clicked.");

			Log.message("6. Approve icon is selected again to un-approve assignment.");

			//Verification : Verifies if Mark Complete Icon is displayed
			//----------------------------------------------------------
			metadatacard.saveAndClose(); //Saves the metdatacard
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (!metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				Log.pass("Test case Passed. Assignment is un-approved on clicking approved Approve icon in metadatacard opened through context menu.");
			else
				Log.fail("Test case Failed. Assignment is not un-approved on clicking approved Approve icon in metadatacard opened through context menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_23_1A

	/**
	 * 172015.3.23.1B : User should be able to un-complete assignment in Metadatacard opened through operations menu when assignment is created with ‘Anyone can approve’ class.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "User should be able to un-complete assignment in Metadatacard opened through operations menu when assignment is created with ‘Anyone can approve’ class.")
	public void SprintTest172015_3_23_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentAnyoneCanApprove.Value); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			String userNames = userFullName + "," + dataPool.get("AssignedUsers");
			String[] userList = userNames.split(",");

			for (int loopIdx=0; loopIdx<userList.length; loopIdx++) //Assigned with multiple users. 
				metadatacard.setPropertyValue("Assigned to", userList[loopIdx].trim(), loopIdx+1); //Sets the assigned to property

			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			Log.message("2. New Assignment(" + assigName + ") is created with mutliple assigned to users.", driver);

			//Step-3 : Open the Properties dialog of the new assignment through operations menu
			//------------------------------------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from operations menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of the new assignment (" + assigName + ") is opened through operations menu.");

			//Step-4 : Mark Complete the newly created assignment
			//----------------------------------------------------
			int propIndex = metadatacard.getPropertyValueIndex("Assigned to", userFullName);

			if (!metadatacard.clickApproveIcon(propIndex)) //Clicks Complete Assignment icon.
				throw new Exception("Approve icon is not clicked.");

			metadatacard.saveAndClose(); //Saves the metdatacard

			if (!ListView.getMarkedAsCompleteByItemName(driver, assigName).equalsIgnoreCase(userFullName)) //Checks if Assignment is approved
				throw new Exception("Assignment (" + assigName + ") is not approved by user (" + userFullName + ").");

			Log.message("4. Assignment (" + assigName + ") is approved by user (" + userFullName + ").", driver);

			//Step-5 : Open the Approved assignment through operations menu
			//----------------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from operations menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("5. Properties dialog of the approved assignment (" + assigName + ") is opened through operations menu.");

			//Step-6 : Click Approve icon again to un-approve assignment
			//-----------------------------------------------------------
			propIndex = metadatacard.getPropertyValueIndex("Assigned to", userFullName);

			if (!metadatacard.clickApproveIcon(false, propIndex)) //Clicks Complete Assignment icon.
				throw new Exception("Approve icon is not clicked.");

			Log.message("6. Approve icon is selected again to un-approve assignment.");

			//Verification : Verifies if Mark Complete Icon is displayed
			//----------------------------------------------------------
			metadatacard.saveAndClose(); //Saves the metdatacard

			if (!ListView.getMarkedAsCompleteByItemName(driver, assigName).equalsIgnoreCase(userFullName)) //Checks if Assignment is approved
				Log.pass("Test case Passed. Assignment is un-approved on clicking approved Approve icon in metadatacard opened through operations menu.");
			else
				Log.fail("Test case Failed. Assignment is not un-approved on clicking approved Approve icon in metadatacard opened through operations menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_23_1B

	/**
	 * 172015.3.23.1C : User should be able to un-complete assignment in Metadatacard opened through taskpanel menu when assignment is created with ‘Anyone can approve’ class.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "User should be able to un-complete assignment in Metadatacard opened through taskpanel menu when assignment is created with ‘Anyone can approve’ class.")
	public void SprintTest172015_3_23_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentAnyoneCanApprove.Value); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			String userNames = userFullName + "," + dataPool.get("AssignedUsers");
			String[] userList = userNames.split(",");

			for (int loopIdx=0; loopIdx<userList.length; loopIdx++) //Assigned with multiple users. 
				metadatacard.setPropertyValue("Assigned to", userList[loopIdx].trim(), loopIdx+1); //Sets the assigned to property

			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			Log.message("2. New Assignment(" + assigName + ") is created with mutliple assigned to users.", driver);

			//Step-3 : Open the Properties dialog of the new assignment through taskpanel menu
			//------------------------------------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from taskpanel menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of the new assignment (" + assigName + ") is opened through taskpanel menu.");

			//Step-4 : Mark Complete the newly created assignment
			//----------------------------------------------------
			int propIndex = metadatacard.getPropertyValueIndex("Assigned to", userFullName);

			if (!metadatacard.clickApproveIcon(propIndex)) //Clicks Complete Assignment icon.
				throw new Exception("Approve icon is not clicked.");

			metadatacard.saveAndClose(); //Saves the metdatacard
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper
			if (!metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				throw new Exception("Assignment (" + assigName + ") is not approved by user (" + userFullName + ").");

			Log.message("4. Assignment (" + assigName + ") is approved by user (" + userFullName + ").", driver);

			//Step-5 : Open the Approved assignment through taskpanel menu
			//----------------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from taskpanel menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("5. Properties dialog of the approved assignment (" + assigName + ") is opened through taskpanel menu.");

			//Step-6 : Click Approve icon again to un-approve assignment
			//-----------------------------------------------------------
			propIndex = metadatacard.getPropertyValueIndex("Assigned to", userFullName);

			if (!metadatacard.clickApproveIcon(false, propIndex)) //Clicks Complete Assignment icon.
				throw new Exception("Approve icon is not clicked.");

			Log.message("6. Approve icon is selected again to un-approve assignment.");

			//Verification : Verifies if Mark Complete Icon is displayed
			//----------------------------------------------------------
			metadatacard.saveAndClose(); //Saves the metdatacard
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper
			if (!metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				Log.pass("Test case Passed. Assignment is un-approved on clicking approved Approve icon in metadatacard opened through taskpanel menu.");
			else
				Log.fail("Test case Failed. Assignment is not un-approved on clicking approved Approve icon in metadatacard opened through taskpanel menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_23_1C

	/**
	 * 172015.3.23.1D : User should be able to un-complete assignment in Metadatacard opened in rightpane when assignment is created with ‘Anyone can approve’ class.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "User should be able to un-complete assignment in Metadatacard opened in rightpane when assignment is created with ‘Anyone can approve’ class.")
	public void SprintTest172015_3_23_1D(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentAnyoneCanApprove.Value); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			String userNames = userFullName + "," + dataPool.get("AssignedUsers");
			String[] userList = userNames.split(",");

			for (int loopIdx=0; loopIdx<userList.length; loopIdx++) //Assigned with multiple users. 
				metadatacard.setPropertyValue("Assigned to", userList[loopIdx].trim(), loopIdx+1); //Sets the assigned to property

			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			Log.message("2. New Assignment(" + assigName + ") is created with mutliple assigned to users.", driver);

			//Step-3 : Open the Properties dialog of the new assignment in right pane
			//-----------------------------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of the new assignment (" + assigName + ") is opened in rightpane.");

			//Step-4 : Mark Complete the newly created assignment
			//----------------------------------------------------
			int propIndex = metadatacard.getPropertyValueIndex("Assigned to", userFullName);

			if (!metadatacard.clickApproveIcon(propIndex)) //Clicks Complete Assignment icon.
				throw new Exception("Approve icon is not clicked.");

			metadatacard.saveAndClose(); //Saves the metdatacard

			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (!metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				throw new Exception("Assignment (" + assigName + ") is not approved by user (" + userFullName + ").");

			Log.message("4. Assignment (" + assigName + ") is approved by user (" + userFullName + ").", driver);

			//Step-5 : Open the Approved assignment in right pane
			//---------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("5. Properties dialog of the approved assignment (" + assigName + ") is opened in rightpane.");

			//Step-6 : Click Approve icon again to un-approve assignment
			//-----------------------------------------------------------
			propIndex = metadatacard.getPropertyValueIndex("Assigned to", userFullName);

			if (!metadatacard.clickApproveIcon(false, propIndex)) //Clicks Complete Assignment icon.
				throw new Exception("Approve icon is not clicked.");

			Log.message("6. Approve icon is selected again to un-approve assignment.");

			//Verification : Verifies if Mark Complete Icon is displayed
			//----------------------------------------------------------
			metadatacard.saveAndClose(); //Saves the metdatacard
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (!metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				Log.pass("Test case Passed. Assignment is un-approved on clicking approved Approve icon in metadatacard opened in rightpane.");
			else
				Log.fail("Test case Failed. Assignment is not un-approved on clicking approved Approve icon in metadatacard opened in rightpane.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_23_1D

	/**
	 * 172015.3.25.1A : Approve should be available in taskpanel after un-approving assignment in Metadatacard when assignment is created with ‘Any can approve’ class.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Approve should be available in taskpanel after un-approving assignment in Metadatacard when assignment is created with ‘Any can approve’ class.")
	public void SprintTest172015_3_25_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentAnyoneCanApprove.Value); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			String userNames = userFullName + "," + dataPool.get("AssignedUsers");
			String[] userList = userNames.split(",");

			for (int loopIdx=0; loopIdx<userList.length; loopIdx++) //Assigned with multiple users. 
				metadatacard.setPropertyValue("Assigned to", userList[loopIdx].trim(), loopIdx+1); //Sets the assigned to property

			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			Log.message("2. New Assignment(" + assigName + ") with class " + Caption.Classes.AssignmentAnyoneCanApprove.Value + " is created with mutliple assigned to users.", driver);

			//Step-3 : Open the Properties dialog of the new assignment in right pane
			//-----------------------------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of the new assignment (" + assigName + ") is opened in rightpane.");

			//Step-4 : Mark Complete the newly created assignment
			//----------------------------------------------------
			int propIndex = metadatacard.getPropertyValueIndex("Assigned to", userFullName);

			if (!metadatacard.clickApproveIcon(propIndex)) //Clicks Complete Assignment icon.
				throw new Exception("Approve icon is not clicked.");

			metadatacard.saveAndClose(); //Saves the metdatacard
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (!metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				throw new Exception("Assignment (" + assigName + ") is not approved by user (" + userFullName + ").");

			if (homePage.taskPanel.isItemExists(Caption.MenuItems.Approve.Value))
				throw new Exception("'" + Caption.MenuItems.Approve.Value + "' is available in taskpanel after approving assignment."); 

			Log.message("4. Assignment (" + assigName + ") is approved by user (" + userFullName + ").", driver);

			//Step-5 : Open the Approved assignment in right pane
			//---------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("5. Properties dialog of the approved assignment (" + assigName + ") is opened in rightpane.");

			//Step-6 : Click Approve icon again to un-approve assignment
			//-----------------------------------------------------------
			propIndex = metadatacard.getPropertyValueIndex("Assigned to", userFullName);

			if (!metadatacard.clickApproveIcon(false, propIndex)) //Clicks Complete Assignment icon.
				throw new Exception("Approve icon is not clicked.");

			Log.message("6. Approve icon is selected again to un-approve assignment.");

			//Verification : Verifies if Mark Complete Icon is displayed
			//----------------------------------------------------------
			metadatacard.saveAndClose(); //Saves the metdatacard

			if (homePage.taskPanel.isItemExists(Caption.MenuItems.Approve.Value)) //Checks if Assignment is approved
				Log.pass("Test case Passed. '" + Caption.MenuItems.Approve.Value + "' is available in taskpanel after un-approving assignment.");
			else
				Log.fail("Test case Failed. '" + Caption.MenuItems.Approve.Value + "' is not available in taskpanel after un-approving assignment..", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_25_1A

	/**
	 * 172015.3.38A : Making copy for an approved assignment with Any one can approve class should also be in an approved state.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Making copy for an approved assignment with Any one can approve class should also be in an approved state.")
	public void SprintTest172015_3_38A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentAnyoneCanApprove.Value); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			String userNames = userFullName + "," + dataPool.get("AssignedUsers");
			String[] userList = userNames.split(",");

			for (int loopIdx=0; loopIdx<userList.length; loopIdx++) //Assigned with multiple users. 
				metadatacard.setPropertyValue("Assigned to", userList[loopIdx].trim(), loopIdx+1); //Sets the assigned to property

			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			Log.message("2. New Assignment(" + assigName + ") is created with mutliple assigned to users.", driver);

			//Step-3 : Open the Properties dialog of the new assignment in right pane
			//-----------------------------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("3. Properties dialog of the new assignment (" + assigName + ") is opened in rightpane.");

			//Step-4 : Mark Complete the newly created assignment
			//----------------------------------------------------
			int propIndex = metadatacard.getPropertyValueIndex("Assigned to", userFullName);

			if (!metadatacard.clickApproveIcon(propIndex)) //Clicks Complete Assignment icon.
				throw new Exception("Approve icon is not clicked.");

			metadatacard.saveAndClose(); //Saves the metdatacard
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (!metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				throw new Exception("Assignment (" + assigName + ") is not approved by user (" + userFullName + ").");

			Log.message("4. Assignment (" + assigName + ") is approved by user (" + userFullName + ").", driver);

			//Step-5 : Select the approved assignment and select Make copy from Taskpanel
			//---------------------------------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.MakeCopy.Value); //Selects Make copy from task panel

			Log.message("5. Approved assignment (" + assigName + ") is selected and Make copy is selected from taskpanel.");

			//Step-6 : Save the metadatacard
			//------------------------------
			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			String newAssigName = assigName + "-Copy";
			metadatacard.setPropertyValue("Name or title", newAssigName); //Sets Name or title to new assignment
			metadatacard.saveAndClose(); //Saves and Close assignment.

			if (!homePage.listView.isItemExists(newAssigName))
				throw new Exception("New copied assignment (" + newAssigName + ") is not created.");

			Log.message("6. New copied assignment (" + newAssigName + ") is created.");

			//Verification : Verify if copied assignment is in approved state
			//----------------------------------------------------------
			if (ListView.getMarkedAsCompleteByItemName(driver, newAssigName).equalsIgnoreCase(userFullName)) //Checks if Assignment is approved
				Log.pass("Test case Passed. Copied assignment is approved state with 'Any one can approve' class.");
			else
				Log.fail("Test case Failed. Copied assignment is not in approved state with 'Any one can approve' class.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_38A

	/**
	 * 172015.3.38C : Making copy for an un-approved/un-rejected assignment with Any one can approve class should also be in the same state.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Making copy for an un-approved/un-rejected assignment with Any one can approve class should also be in the same state.")
	public void SprintTest172015_3_38C(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentAnyoneCanApprove.Value); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			String userNames = userFullName + "," + dataPool.get("AssignedUsers");
			String[] userList = userNames.split(",");

			for (int loopIdx=0; loopIdx<userList.length; loopIdx++) //Assigned with multiple users. 
				metadatacard.setPropertyValue("Assigned to", userList[loopIdx].trim(), loopIdx+1); //Sets the assigned to property

			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			Log.message("2. New Assignment(" + assigName + ") is created with mutliple assigned to users.", driver);

			//Step-3 : Select the new assignment and select Make copy from Taskpanel
			//---------------------------------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.MakeCopy.Value); //Selects Make copy from task panel

			Log.message("3. Rejected assignment (" + assigName + ") is selected and Make copy is selected from taskpanel.");

			//Step-4 : Save the metadatacard
			//------------------------------
			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			String newAssigName = assigName + "-Copy";
			metadatacard.setPropertyValue("Name or title", newAssigName); //Sets Name or title to new assignment
			metadatacard.saveAndClose(); //Saves and Close assignment.

			if (!homePage.listView.isItemExists(newAssigName))
				throw new Exception("New copied assignment (" + newAssigName + ") is not created.");

			Log.message("4. New copied assignment (" + newAssigName + ") is created.");

			//Verification : Verify if copied assignment is as source assignment
			//-------------------------------------------------------------------
			if (ListView.getRejectedByItemName(driver, newAssigName).equalsIgnoreCase("") && 
					ListView.getMarkedAsCompleteByItemName(driver, newAssigName).equalsIgnoreCase("")) //Checks if Assignment is approved
				Log.pass("Test case Passed. Copied assignment is neither in approved nor in rejected state with 'Any one can approve' class.");
			else
				Log.fail("Test case Failed. Copied assignment is either in approved or in rejected state with 'Any one can approve' class.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_38C

	/**
	 * 172015.3.67.1A : Other Assignees should be removed in metadatacard opened through context menu on approving the assignment with one user on using Assignment any can approve class
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Other Assignees should be removed in metadatacard opened through context menu on approving the assignment with one user on using Assignment any can approve class.")
	public void SprintTest172015_3_67_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentAnyoneCanApprove.Value); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			String userNames = userFullName + "," + dataPool.get("AssignedUsers");
			String[] userList = userNames.split(",");

			for (int loopIdx=0; loopIdx<userList.length; loopIdx++) //Assigned with multiple users. 
				metadatacard.setPropertyValue("Assigned to", userList[loopIdx].trim(), loopIdx+1); //Sets the assigned to property

			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			Log.message("2. New Assignment(" + assigName + ") is created with mutliple assigned to users and " + Caption.Classes.AssignmentAnyoneCanApprove.Value + " class.", driver);

			//Step-3 : Open the Properties dialog of the new assignment through context menu
			//-------------------------------------------------------------------------------
			if (!homePage.listView.rightClickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Open metadatacard through context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("3. Metadatacard dialog of the new assignment (" + assigName + ") is opened through context menu.");

			//Step-4 : Mark Complete the newly created assignment
			//----------------------------------------------------
			int propIndex = metadatacard.getPropertyValueIndex("Assigned to", userFullName);

			if (!metadatacard.clickApproveIcon(propIndex)) //Clicks Complete Assignment icon.
				throw new Exception("Approve icon is not clicked.");

			metadatacard.saveAndClose(); //Saves the metdatacard
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (!metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				throw new Exception("Assignment (" + assigName + ") is not approved by user (" + userFullName + ").");

			Log.message("4. Assignment (" + assigName + ") is approved by user (" + userFullName + ").", driver);

			//Step-5 : Open the Properties dialog of the new assignment through context menu
			//-------------------------------------------------------------------------------
			if (!homePage.listView.rightClickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Open metadatacard through context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("5. Metadatacard dialog of the new assignment (" + assigName + ") is opened through context menu.");

			//Verification : Verify if copied assignment is in approved state
			//----------------------------------------------------------
			ArrayList<String> assignedToValues = metadatacard.getPropertyValues("Assigned to");

			if (assignedToValues.size() != 1)
				throw new Exception("Test case Failed. Multiple values to property assigned to are available after approving the assignment. No of users listed : " + assignedToValues.size());

			if (assignedToValues.get(0).equalsIgnoreCase(userFullName)) //Checks if Assignment is approved
				Log.pass("Test case Passed. Other Assignes are successfully removed on approving assignment by the user.");
			else
				Log.fail("Test case Failed. Assigned user name in metadatacard is different after approving the assingnment.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_67_1A

	/**
	 * 172015.3.67.1B : Other Assignees should be removed in metadatacard opened through operations menu on approving the assignment with one user on using Assignment any can approve class
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Other Assignees should be removed in metadatacard opened through operations menu on approving the assignment with one user on using Assignment any can approve class.")
	public void SprintTest172015_3_67_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentAnyoneCanApprove.Value); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			String userNames = userFullName + "," + dataPool.get("AssignedUsers");
			String[] userList = userNames.split(",");

			for (int loopIdx=0; loopIdx<userList.length; loopIdx++) //Assigned with multiple users. 
				metadatacard.setPropertyValue("Assigned to", userList[loopIdx].trim(), loopIdx+1); //Sets the assigned to property

			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			Log.message("2. New Assignment(" + assigName + ") is created with mutliple assigned to users and " + Caption.Classes.AssignmentAnyoneCanApprove.Value + " class.", driver);

			//Step-3 : Open the Properties dialog of the new assignment through operations menu
			//-------------------------------------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Open metadatacard through operation menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("3. Metadatacard dialog of the new assignment (" + assigName + ") is opened through operations menu.");

			//Step-4 : Mark Complete the newly created assignment
			//----------------------------------------------------
			int propIndex = metadatacard.getPropertyValueIndex("Assigned to", userFullName);

			if (!metadatacard.clickApproveIcon(propIndex)) //Clicks Complete Assignment icon.
				throw new Exception("Approve icon is not clicked.");

			metadatacard.saveAndClose(); //Saves the metdatacard

			if (!ListView.getMarkedAsCompleteByItemName(driver, assigName).equalsIgnoreCase(userFullName)) //Checks if Assignment is approved
				throw new Exception("Assignment (" + assigName + ") is not approved by user (" + userFullName + ").");

			Log.message("4. Assignment (" + assigName + ") is approved by user (" + userFullName + ").", driver);

			//Step-5 : Open the Properties dialog of the new assignment through operations menu
			//-------------------------------------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Open metadatacard through operation menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("5. Metadatacard dialog of the new assignment (" + assigName + ") is opened through operations menu.");

			//Verification : Verify if copied assignment is in approved state
			//----------------------------------------------------------
			ArrayList<String> assignedToValues = metadatacard.getPropertyValues("Assigned to");

			if (assignedToValues.size() != 1)
				throw new Exception("Test case Failed. Multiple values to property assigned to are available after approving the assignment. No of users listed : " + assignedToValues.size());

			if (assignedToValues.get(0).equalsIgnoreCase(userFullName)) //Checks if Assignment is approved
				Log.pass("Test case Passed. Other Assignes are successfully removed on approving assignment by the user.");
			else
				Log.fail("Test case Failed. Assigned user name in metadatacard is different after approving the assingnment.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_67_1B

	/**
	 * 172015.3.67.1C : Other Assignees should be removed in metadatacard opened through taskpanel menu on approving the assignment with one user on using Assignment any can approve class
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Other Assignees should be removed in metadatacard opened through taskpanel menu on approving the assignment with one user on using Assignment any can approve class.")
	public void SprintTest172015_3_67_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentAnyoneCanApprove.Value); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			String userNames = userFullName + "," + dataPool.get("AssignedUsers");
			String[] userList = userNames.split(",");

			for (int loopIdx=0; loopIdx<userList.length; loopIdx++) //Assigned with multiple users. 
				metadatacard.setPropertyValue("Assigned to", userList[loopIdx].trim(), loopIdx+1); //Sets the assigned to property

			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			Log.message("2. New Assignment(" + assigName + ") is created with mutliple assigned to users and " + Caption.Classes.AssignmentAnyoneCanApprove.Value + " class.", driver);

			//Step-3 : Open the Properties dialog of the new assignment through taskpanel menu
			//-------------------------------------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Open metadatacard through taskpanel menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("3. Metadatacard dialog of the new assignment (" + assigName + ") is opened through taskpanel menu.");

			//Step-4 : Mark Complete the newly created assignment
			//----------------------------------------------------
			int propIndex = metadatacard.getPropertyValueIndex("Assigned to", userFullName);

			if (!metadatacard.clickApproveIcon(propIndex)) //Clicks Complete Assignment icon.
				throw new Exception("Approve icon is not clicked.");

			metadatacard.saveAndClose(); //Saves the metdatacard
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (!metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				throw new Exception("Assignment (" + assigName + ") is not approved by user (" + userFullName + ").");

			Log.message("4. Assignment (" + assigName + ") is approved by user (" + userFullName + ").", driver);

			//Step-5 : Open the Properties dialog of the new assignment through taskpanel menu
			//-------------------------------------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Open metadatacard through taskpanel menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("5. Metadatacard dialog of the new assignment (" + assigName + ") is opened through taskpanel menu.");

			//Verification : Verify if copied assignment is in approved state
			//----------------------------------------------------------
			ArrayList<String> assignedToValues = metadatacard.getPropertyValues("Assigned to");

			if (assignedToValues.size() != 1)
				throw new Exception("Test case Failed. Multiple values to property assigned to are available after approving the assignment. No of users listed : " + assignedToValues.size());

			if (assignedToValues.get(0).equalsIgnoreCase(userFullName)) //Checks if Assignment is approved
				Log.pass("Test case Passed. Other Assignes are successfully removed on approving assignment by the user.");
			else
				Log.fail("Test case Failed. Assigned user name in metadatacard is different after approving the assingnment.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_67_1C

	/**
	 * 172015.3.67.1D : Other Assignees should be removed in metadatacard opened in right pane on approving the assignment with one user on using Assignment any can approve class
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Other Assignees should be removed in metadatacard opened in right pane on approving the assignment with one user on using Assignment any can approve class")
	public void SprintTest172015_3_67_1D(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentAnyoneCanApprove.Value); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			String userNames = userFullName + "," + dataPool.get("AssignedUsers");
			String[] userList = userNames.split(",");

			for (int loopIdx=0; loopIdx<userList.length; loopIdx++) //Assigned with multiple users. 
				metadatacard.setPropertyValue("Assigned to", userList[loopIdx].trim(), loopIdx+1); //Sets the assigned to property

			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			Log.message("2. New Assignment(" + assigName + ") is created with mutliple assigned to users and " + Caption.Classes.AssignmentAnyoneCanApprove.Value + " class.", driver);

			//Step-3 : Open the Properties dialog of the new assignment in right pane
			//-------------------------------------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("3. Metadatacard dialog of the new assignment (" + assigName + ") is opened in right pane.");

			//Step-4 : Mark Complete the newly created assignment
			//----------------------------------------------------
			int propIndex = metadatacard.getPropertyValueIndex("Assigned to", userFullName);

			if (!metadatacard.clickApproveIcon(propIndex)) //Clicks Complete Assignment icon.
				throw new Exception("Approve icon is not clicked.");

			metadatacard.saveAndClose(); //Saves the metdatacard
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (!metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				throw new Exception("Assignment (" + assigName + ") is not approved by user (" + userFullName + ").");

			Log.message("4. Assignment (" + assigName + ") is approved by user (" + userFullName + ").", driver);

			//Step-5 : Open the Properties dialog of the new assignment in right pane
			//-------------------------------------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Open metadatacard through taskpanel menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("5. Metadatacard dialog of the new assignment (" + assigName + ") is opened in right pane.");

			//Verification : Verify if copied assignment is in approved state
			//----------------------------------------------------------
			ArrayList<String> assignedToValues = metadatacard.getPropertyValues("Assigned to");

			if (assignedToValues.size() != 1)
				throw new Exception("Test case Failed. Multiple values to property assigned to are available after approving the assignment. No of users listed : " + assignedToValues.size());

			if (assignedToValues.get(0).equalsIgnoreCase(userFullName)) //Checks if Assignment is approved
				Log.pass("Test case Passed. Other Assignes are successfully removed on approving assignment by the user.");
			else
				Log.fail("Test case Failed. Assigned user name in metadatacard is different after approving the assingnment.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_67_1D

	/**
	 * 172015.3.67.1E : Other Assignees should be removed from metadatacard after approving the assignment with one user in taskpanel on using Assignment any can approve class
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Other Assignees should be removed from metadatacard after approving the assignment with one user in taskpanel on using Assignment any can approve class")
	public void SprintTest172015_3_67_1E(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentAnyoneCanApprove.Value); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			String userNames = userFullName + "," + dataPool.get("AssignedUsers");
			String[] userList = userNames.split(",");

			for (int loopIdx=0; loopIdx<userList.length; loopIdx++) //Assigned with multiple users. 
				metadatacard.setPropertyValue("Assigned to", userList[loopIdx].trim(), loopIdx+1); //Sets the assigned to property

			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			Log.message("2. New Assignment(" + assigName + ") is created with mutliple assigned to users and " + Caption.Classes.AssignmentAnyoneCanApprove.Value + " class.", driver);

			//Step-3 : Approve the assignment from taskpanel
			//----------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			homePage.taskPanel.markApproveReject("Approve");

			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			if (!metadatacard.isSelectedObjectApprovedByUser(userFullName)) //Checks if Assignment is approved
				throw new Exception("Assignment (" + assigName + ") is not approved by user (" + userFullName + ").");

			Log.message("3. Metadatacard dialog of the new assignment (" + assigName + ") is opened through taskpanel menu.");

			//Step-4 : Open the Properties dialog of the new assignment in right pane
			//-------------------------------------------------------------------------------
			if (!homePage.listView.clickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not selected.");

			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("4. Metadatacard dialog of the new assignment (" + assigName + ") is opened in right pane.");

			//Verification : Verify if copied assignment is in approved state
			//----------------------------------------------------------
			ArrayList<String> assignedToValues = metadatacard.getPropertyValues("Assigned to");

			if (assignedToValues.size() != 1)
				throw new Exception("Test case Failed. Multiple values to property assigned to are available after approving the assignment. No of users listed : " + assignedToValues.size());

			if (assignedToValues.get(0).equalsIgnoreCase(userFullName)) //Checks if Assignment is approved
				Log.pass("Test case Passed. Other Assignes are successfully removed on approving assignment by the user.");
			else
				Log.fail("Test case Failed. Assigned user name in metadatacard is different after approving the assingnment.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_67_1E

} //End Class Assignments
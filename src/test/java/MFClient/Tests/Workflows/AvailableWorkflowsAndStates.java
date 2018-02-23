package MFClient.Tests.Workflows;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.Utils;
import genericLibrary.WebDriverUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class AvailableWorkflowsAndStates {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String userName = null;
	public static String password = null;
	public static String testVault = null;
	public static String configURL = null;
	public static String userFullName = null;
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
			configURL = xmlParameters.getParameter("ConfigurationURL");
			className = this.getClass().getSimpleName().toString().trim();
			if (xmlParameters.getParameter("driverType").equalsIgnoreCase("IE"))
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim() + xmlParameters.getParameter("driverVersion").trim();
			else
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim();
			userName = xmlParameters.getParameter("UserName");
			password = xmlParameters.getParameter("Password");
			testVault = xmlParameters.getParameter("VaultName");		
			userFullName = xmlParameters.getParameter("UserFullName");
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
	 * 39.1.2A : Currently applied state of an object should not be displayed in taskpane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Currently applied state of an object should not be displayed in taskpane.")
	public void SprintTest39_1_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Utils.fluentWait(driver);

			//1. Navigate to the view
			//-----------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("SearchType"), dataPool.get("Object"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.", driver);

			//2. Click on the object with workflow and state set
			//--------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("Object '" + dataPool.get("Object") + "' does not exist in the vault.");

			if (!homePage.listView.clickItem(dataPool.get("Object")))
				throw new Exception("Object '" + dataPool.get("Object") + "' is not selected.");

			MetadataCard metadatacard = new MetadataCard(driver,true);//Instantiate the metadata card
			String currentState = metadatacard.getWorkflowState();//get the current workflow state

			Log.message("2. Object (" + dataPool.get("Object") + ") with workflow is selected.", driver);

			//3. Get the current workflow state of an object through Change workflow dialog
			//-----------------------------------------------------------------------------
			/*MFilesDialog mFilesDialog = new MFilesDialog(driver);
			String currentState = mFilesDialog.getWorkflowState();*/

			if(currentState.equals(""))
				throw new SkipException("Invalid Test Data. The Specified object does not have any workflow state set to it.");

			//Verification: To verify if the current state of the object is not listed in the task panel
			//-------------------------------------------------------------------------------------------
			if(!homePage.taskPanel.isItemExists(currentState)) 
				Log.pass("Test Case Passed. Currently selected workflow state (" + currentState + ") of an object is not displayed in taskpane.", driver);
			else
				Log.fail("Test Case Failed. Currently selected workflow state (" + currentState + ") of an object is getting displayed in taskpane.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}	

	/**
	 * 39.1.2B : The Currently Applied state should be removed from the task pane once the state change is applied
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "The Currently Applied state should be removed from the task pane once the state change is applied.")
	public void SprintTest39_1_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			//1. Navigate to the view
			//-----------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("SearchType"), dataPool.get("Object"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.", driver);

			//2. Click on the object with workflow and state set.
			//------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			if (!homePage.listView.clickItem(dataPool.get("Object")))
				throw new Exception("Object '" + dataPool.get("Object") + "' is not selected.");

			Utils.fluentWait(driver);

			Log.message("2. Clicked on the object with workflow and state set.", driver);

			//3. Set a state from the task pane
			//----------------------------------
			if(!homePage.taskPanel.isItemExists(dataPool.get("State")))
				throw new Exception("The State '" + dataPool.get("State") + "' was not found in the task pane");

			homePage.taskPanel.clickItem(dataPool.get("State"));
			Utils.fluentWait(driver);

			MFilesDialog mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.clickOkButton();
			Utils.fluentWait(driver);

			Log.message("3. Set a state from the task pane.", driver);

			//Verification: To verify if the current state of the object is not listed in the task panel
			//-------------------------------------------------------------------------------------------
			if(!homePage.taskPanel.isItemExists(dataPool.get("State"))) 
				Log.pass("Test Case Passed. The current State was not listed n the Task Pane.", driver);
			else
				Log.fail("Test Case Failed. The current State was listed in the task pane.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}	

	/**
	 * 39.1.4 : When automatic state transition is enabled the target state should be removed from the Task pane as soon as the source state is set to the object
	 */
	/*@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", dependsOnMethods={"SprintTest39_1_1B"}, groups = {"Sprint39", "Workflows"}, 
			description = "When automatic state transition is enabled the target state should be removed from the Task pane as soon as the source state is set to the object.")*/
	public void SprintTest39_1_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.", driver);

			//2. Search for an object with workflow
			//--------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object with workflow.", driver);

			//3. Click on the object with workflow and state set.
			//------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);

			Log.message("3. Clicked on the object with workflow and state set.", driver);

			//4. Set a state from the task pane
			//----------------------------------
			if(!homePage.taskPanel.isItemExists(dataPool.get("State")))
				throw new Exception("The State '" + dataPool.get("State") + "' was not found in the task pane");

			homePage.taskPanel.clickItem(dataPool.get("State"));
			Utils.fluentWait(driver);

			MFilesDialog mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.clickOkButton();
			Utils.fluentWait(driver);

			Log.message("4. Set a state from the task pane.", driver);

			//Verification: To verify if the target state of auto-transition is set to the object
			//------------------------------------------------------------------------------------
			if(!homePage.taskPanel.isItemExists(dataPool.get("TargetState"))) 
				Log.pass("Test Case Passed. The Target state of automatic transition is not available in taskpanel.", driver);
			else
				Log.fail("Test Case Failed. Target state (" + dataPool.get("TargetState") + ") is available in taskpanel.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}	

	/**
	 * 39.1.5A : Set Workflow to an object with no workflow (Task Pane Verificaiton)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows","Bug"}, 
			description = "Set Workflow to an object with no workflow (Task Pane Verificaiton).")
	public void SprintTest39_1_5A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.", driver);

			//2. Search for an object with workflow
			//--------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object with workflow.", driver);

			//3. Click on the object with workflow and state set.
			//------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);

			if(homePage.taskPanel.isItemExists(Caption.MenuItems.StateTransition.Value))
				throw new SkipException("Invalid Test data. The Object has workfoow set to it.");

			Log.message("3. Clicked on the object with workflow and state set.", driver);

			//4. Set a workflow to the object
			//----------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);
			Utils.fluentWait(driver);

			if(!MFilesDialog.exists(driver))
				throw new Exception("The Workflow dialog did not appear.");

			MFilesDialog mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.setWorkflow(dataPool.get("Workflow"));
			mFilesDialog.clickOkButton();
			Utils.fluentWait(driver);

			Log.message("4. Set a workflow to the object.", driver);

			//Verification: To verify if all the states of the workflow are displayed
			//------------------------------------------------------------------------
			Utils.fluentWait(driver);
			if(!homePage.taskPanel.isItemExists(Caption.MenuItems.StateTransition.Value))
				throw new Exception("The Move into State option was not displayed in the task pane even when the object has workflow.");

			String[] allStates = dataPool.get("States").split("\n");
			for(int count = 0; count < allStates.length; count++) {
				if(!homePage.taskPanel.isItemExists(allStates[count]))
					Log.fail("Test Case Failed. The expected state of the workflow - '" + allStates[count] + "' was not listed in the task pane.", driver);
			}

			Log.pass("Test Case Passed. All the stated of the workflow was listed in the task pane.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}	

	/**
	 * 39.1.5B : Set Workflow and state to an object with no workflow (Task Pane verification)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Set Workflow and state to an object with no workflow (Task Pane verification).")
	public void SprintTest39_1_5B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.", driver);

			//2. Search for an object with workflow
			//--------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object with workflow.", driver);

			//3. Click on the object with workflow and state set.
			//------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);

			Log.message("3. Clicked on the object with workflow and state set.", driver);

			//4. Set a workflow to the object
			//----------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);
			Utils.fluentWait(driver);

			if(!MFilesDialog.exists(driver))
				throw new Exception("The Workflow dialog did not appear.");

			MFilesDialog mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.setWorkflow(dataPool.get("Workflow"));
			mFilesDialog.setWorkflowState(dataPool.get("State"));
			mFilesDialog.clickOkButton();
			Utils.fluentWait(driver);

			Log.message("4. Set a workflow to the object.", driver);

			//Verification: To verify if all the states of the workflow are displayed
			//------------------------------------------------------------------------
			Utils.fluentWait(driver);
			if(!homePage.taskPanel.isItemExists(Caption.MenuItems.StateTransition.Value))
				throw new Exception("The Move into State option was not displayed in the task pane even when the object has workflow.");

			String[] allStates = dataPool.get("States").split("\n");
			for(int count = 0; count < allStates.length; count++) {
				if(!homePage.taskPanel.isItemExists(allStates[count]))
					throw new Exception("The expected state of the workflow - '" + allStates[count] + "' was not listed in the task pane.");
			}

			if(!homePage.taskPanel.isItemExists(dataPool.get("State")))
				Log.pass("Test Case Passed. All the stated of the workflow was listed in the task pane.", driver);
			else
				Log.fail("Test Case Failed. The State set to the object was also displayed in the Task Pane.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}	

	/**
	 * 39.1.6 : The Currently Applied state should be removed from the task pane once the state change is applied - through workflow dialog
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "The Currently Applied state should be removed from the task pane once the state change is applied - through workflow dialog.")
	public void SprintTest39_1_6(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.", driver);

			//2. Search for an object with workflow
			//--------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object with workflow.", driver);

			//3. Click on the object with workflow and state set.
			//------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);
			Utils.fluentWait(driver);

			if(!MFilesDialog.exists(driver))
				throw new Exception("The Workflow dialog did not appear.");

			MFilesDialog mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.setWorkflow(dataPool.get("Workflow"));
			mFilesDialog.clickOkButton();
			Utils.fluentWait(driver);

			if(!homePage.taskPanel.isItemExists(dataPool.get("State")))
				throw new SkipException("Invalid Test Data. The State was not listed in the task pane before applying it to the object.");

			Log.message("3. Clicked on the object with workflow and state set.", driver);

			//4. Set a workflow to the object
			//----------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);
			Utils.fluentWait(driver);

			if(!MFilesDialog.exists(driver))
				throw new Exception("The Workflow dialog did not appear.");

			mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.setWorkflowState(dataPool.get("State"));
			mFilesDialog.clickOkButton();
			Utils.fluentWait(driver);

			Log.message("4. Set a workflow to the object.", driver);

			//Verification: To verify if all the states of the workflow are displayed
			//------------------------------------------------------------------------
			if(!homePage.taskPanel.isItemExists(dataPool.get("State")))
				Log.pass("Test Case Passed. The State was removed from the task pane as expected.", driver);
			else
				Log.fail("Test Case Failed. The State was not removed from the Task pane even after it was applied to the object.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}	


	/**
	 * 39.1.10A : All possible transition states should be displayed for workflow state property in Metadatacard opened through task panel.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows", "Smoke"}, 
			description = "All possible transition states should be displayed for workflow state property in Metadatacard opened through task panel.")
	public void SprintTest39_1_10A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("SearchType"), dataPool.get("Object"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.", driver);

			//2. Open the properties of a document
			//-------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			if (!homePage.listView.clickItem(dataPool.get("Object")))
				throw new Exception("The specified object '" + dataPool.get("Object") + "' is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the metadata card

			Log.message("2. Metadatacard of the object (" + dataPool.get("Object") + ") is opened.", driver);

			//3. Set the action with state to the object
			//--------------------------------------------
			metadatacard.setWorkflow(dataPool.get("Workflow"));
			ArrayList<String> actualStates = metadatacard.getAvailableStates();

			metadatacard.cancelAndConfirm();

			Log.message("3. Workflow (" + dataPool.get("Workflow") + ") is set to the object in metadatcard.", driver);

			//Verification: To verify if the available states are listed
			//----------------------------------------------------------
			String[] expectedStates = dataPool.get("States").split("\n");
			String addlInfo = "Expected Workflow States : " + expectedStates.toString() + "(" + expectedStates.length + ")" + 
					"; Actual Workflow States : " + actualStates + "(" + actualStates.size() + ")";

			if (expectedStates.length != actualStates.size())
				throw new Exception("Test Case Failed. Workflow states are not displayed as expected. Refer additional information : " + addlInfo);

			addlInfo = "";

			for (int count = 0; count < expectedStates.length; count++) 
				if(actualStates.indexOf(expectedStates[count]) < 0)
					addlInfo  = addlInfo + ";";

			if (addlInfo.equalsIgnoreCase(""))
				Log.pass("Test Case Passed. All the Expected States were listed.", driver);
			else
				Log.fail("Test Case Failed. The one or more workflow states are not listed in taskpanel as follows, Not listed workflow states : " + addlInfo, driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}	


	/**
	 * 39.1.10B : All possible transition states should be displayed for workflow state property in Metadatacard opened through Context menu.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows", "Smoke"}, 
			description = "All possible transition states should be displayed for workflow state property in Metadatacard opened through Context menu.")
	public void SprintTest39_1_10B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("SearchType"), dataPool.get("Object"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.", driver);

			//2. Open the properties of a document through the Context menu option
			//--------------------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			if (!homePage.listView.clickItem(dataPool.get("Object")))
				throw new Exception("The specified object '" + dataPool.get("Object") + "' is not selected.");

			homePage.listView.rightClickItem(dataPool.get("Object"));//Right click the object 
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value);//click the Properties option in context menu

			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the metadata card

			Log.message("2. Metadatacard of the object (" + dataPool.get("Object") + ") is opened through the context menu.", driver);

			//3. Set the action with state to the object
			//--------------------------------------------
			metadatacard.setWorkflow(dataPool.get("Workflow"));
			ArrayList<String> actualStates = metadatacard.getAvailableStates();

			metadatacard.saveAndClose();

			Log.message("3. Workflow (" + dataPool.get("Workflow") + ") is set to the object in metadatcard.", driver);

			//Verification: To verify if the available states are listed
			//----------------------------------------------------------
			String[] expectedStates = dataPool.get("States").split("\n");
			String addlInfo = "Expected Workflow States - " + expectedStates.length + "; Actual Workflow States - " + actualStates.size();

			if (expectedStates.length != actualStates.size())
				throw new Exception("Test Case Failed. Workflow states are not displayed as expected. Refer additional information : " + addlInfo);

			addlInfo = "";

			for (int count = 0; count < expectedStates.length; count++) 
				if(actualStates.indexOf(expectedStates[count]) < 0)
					addlInfo  = addlInfo + ";";

			if (addlInfo.equalsIgnoreCase(""))
				Log.pass("Test Case Passed. All the Expected States were listed.", driver);
			else
				Log.fail("Test Case Failed. The one or more workflow states are not listed in taskpanel as follows, Not listed workflow states : " + addlInfo, driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}	


	/**
	 * 39.1.10C : All possible transition states should be displayed for workflow state property in Metadatacard opened through Operations menu.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows", "Smoke"}, 
			description = "All possible transition states should be displayed for workflow state property in Metadatacard opened through Operations menu.")
	public void SprintTest39_1_10C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("SearchType"), dataPool.get("Object"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.", driver);

			//2. Open the properties of a document
			//-------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			if (!homePage.listView.clickItem(dataPool.get("Object")))
				throw new Exception("The specified object '" + dataPool.get("Object") + "' is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value);//Click the properties option from the operations menu

			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the metadata card

			Log.message("2. Metadatacard of the object (" + dataPool.get("Object") + ") is opened through the operations menu.", driver);

			//3. Set the action with state to the object
			//--------------------------------------------
			metadatacard.setWorkflow(dataPool.get("Workflow"));
			ArrayList<String> actualStates = metadatacard.getAvailableStates();

			metadatacard.saveAndClose();

			Log.message("3. Workflow (" + dataPool.get("Workflow") + ") is set to the object in metadatcard.", driver);

			//Verification: To verify if the available states are listed
			//----------------------------------------------------------
			String[] expectedStates = dataPool.get("States").split("\n");
			String addlInfo = "Expected Workflow States - " + expectedStates.length + "; Actual Workflow States - " + actualStates.size();

			if (expectedStates.length != actualStates.size())
				throw new Exception("Test Case Failed. Workflow states are not displayed as expected. Refer additional information : " + addlInfo);

			addlInfo = "";

			for (int count = 0; count < expectedStates.length; count++) 
				if(actualStates.indexOf(expectedStates[count]) < 0)
					addlInfo  = addlInfo + ";";

			if (addlInfo.equalsIgnoreCase(""))
				Log.pass("Test Case Passed. All the Expected States were listed.", driver);
			else
				Log.fail("Test Case Failed. The one or more workflow states are not listed in taskpanel as follows, Not listed workflow states : " + addlInfo, driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}	

	/**
	 * 39.1.10D : All possible transition states should be displayed for workflow state property in Metadatacard opened through settings menu - 'Pop-out the metadata card'.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows", "Smoke"}, 
			description = "All possible transition states should be displayed for workflow state property in Metadatacard opened through settings menu - 'Pop-out the metadata card'.")
	public void SprintTest39_1_10D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("SearchType"), dataPool.get("Object"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.", driver);

			//2. Open the properties of a document
			//-------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			if (!homePage.listView.clickItem(dataPool.get("Object")))
				throw new Exception("The specified object '" + dataPool.get("Object") + "' is not selected.");

			MetadataCard metadatacard = new MetadataCard(driver,true);

			metadatacard.popOutMetadatacard();//Pop-out the metadata card option

			Log.message("2. Metadatacard of the object (" + dataPool.get("Object") + ") is opened through the Pop-out the metadata card.", driver);

			//3. Set the action with state to the object
			//--------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);
			metadataCard.setWorkflow(dataPool.get("Workflow"));
			ArrayList<String> actualStates = metadataCard.getAvailableStates();

			metadataCard.saveAndClose();

			Log.message("3. Workflow (" + dataPool.get("Workflow") + ") is set to the object in metadatcard.", driver);

			//Verification: To verify if the available states are listed
			//----------------------------------------------------------
			String[] expectedStates = dataPool.get("States").split("\n");
			String addlInfo = "Expected Workflow States - " + expectedStates.length + "; Actual Workflow States - " + actualStates.size();

			if (expectedStates.length != actualStates.size())
				throw new Exception("Test Case Failed. Workflow states are not displayed as expected. Refer additional information : " + addlInfo);

			addlInfo = "";

			for (int count = 0; count < expectedStates.length; count++) 
				if(actualStates.indexOf(expectedStates[count]) < 0)
					addlInfo  = addlInfo + ";";

			if (addlInfo.equalsIgnoreCase(""))
				Log.pass("Test Case Passed. All the Expected States were listed.", driver);
			else
				Log.fail("Test Case Failed. The one or more workflow states are not listed in taskpanel as follows, Not listed workflow states : " + addlInfo, driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}	

	/**
	 * 39.1.10E : All possible transition states should be displayed for workflow state property in Metadatacard opened through  'Pop-out the metadata card' in right pane.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows", "Smoke"}, 
			description = "All possible transition states should be displayed for workflow state property in Metadatacard opened through  'Pop-out the metadata card' in right pane..")
	public void SprintTest39_1_10E(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("SearchType"), dataPool.get("Object"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.", driver);

			//2. Open the properties of a document
			//-------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			if (!homePage.listView.clickItem(dataPool.get("Object")))
				throw new Exception("The specified object '" + dataPool.get("Object") + "' is not selected.");

			driver.switchTo().defaultContent();//Move to default content in browser
			homePage.previewPane.popoutRightPaneMetadataTab();//Click the right pane 'Pop-out metadatacard'

			Log.message("2. Metadatacard of the object (" + dataPool.get("Object") + ") is opened through the Pop-out the metadata card.", driver);

			//3. Set the action with state to the object
			//--------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);
			metadataCard.setWorkflow(dataPool.get("Workflow"));
			ArrayList<String> actualStates = metadataCard.getAvailableStates();

			metadataCard.saveAndClose();

			Log.message("3. Workflow (" + dataPool.get("Workflow") + ") is set to the object in metadatcard.", driver);

			//Verification: To verify if the available states are listed
			//----------------------------------------------------------
			String[] expectedStates = dataPool.get("States").split("\n");
			String addlInfo = "Expected Workflow States - " + expectedStates.length + "; Actual Workflow States - " + actualStates.size();

			if (expectedStates.length != actualStates.size())
				throw new Exception("Test Case Failed. Workflow states are not displayed as expected. Refer additional information : " + addlInfo);

			addlInfo = "";

			for (int count = 0; count < expectedStates.length; count++) 
				if(actualStates.indexOf(expectedStates[count]) < 0)
					addlInfo  = addlInfo + ";";

			if (addlInfo.equalsIgnoreCase(""))
				Log.pass("Test Case Passed. All the Expected States were listed.", driver);
			else
				Log.fail("Test Case Failed. The one or more workflow states are not listed in taskpanel as follows, Not listed workflow states : " + addlInfo, driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}	

	/**
	 * 39.1.16 : Listing available states in Change Workflow dialog
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Listing available states in Change Workflow dialog")
	public void SprintTest39_1_16(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.", driver);

			//2. Search for an object with workflow
			//--------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object with workflow.", driver);

			//3. Open the Change State dialog of a document
			//-----------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);
			Utils.fluentWait(driver);

			Log.message("3. Open the Change State dialog of a document.", driver);

			//4. Set the Workflow to the Object
			//----------------------------------
			Utils.fluentWait(driver);
			MFilesDialog mFilesDialog = new MFilesDialog(driver);

			mFilesDialog.setWorkflow(dataPool.get("Workflow"));
			Utils.fluentWait(driver);

			List<String> availableStates = mFilesDialog.getWorkflowStates();
			String[] expectedStates = dataPool.get("States").split("\n");

			Log.message("4. Set the Workflow to the Object.", driver);

			//Verification: To verify if all the available states of the object are listed
			//------------------------------------------------------------------------------
			if(availableStates.size() != expectedStates.length)
				throw new Exception("The Availble states did not match the expected count.");

			for(int count = 0; count < expectedStates.length; count++) {
				if(availableStates.indexOf(expectedStates[count]) < 0)
					Log.fail("Test Case Failed. The expected state = '" + expectedStates[count] + "' was not listed", driver);

			}

			Log.pass("Test Case Passed. All the available states of the workflow were listed.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}	



	/**
	 * 39.1.41 : Listing of Workflow that is enabled only for a particular class (Metadatacard)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Listing of Workflow that is enabled only for a particular class (Metadatacard)")
	public void SprintTest39_1_41(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Search for an object
			//------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("1. Search for an object.", driver);

			//2. Open the Metadatacard of the object
			//---------------------------------------
			homePage.listView.rightClickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Open the Metadatacard of the object.", driver);

			//Verification: To verify if a Workflow that is available for a different class is not available for this object
			//---------------------------------------------------------------------------------------------------------------
			List<String> workflows = metadatacard.getAvailableWorkflows();
			if(!workflows.contains(dataPool.get("RestrictedWorkflow")))
				Log.pass("Test Case Passed. The Workflows restricted to other classes were not displayed.", driver);
			else
				Log.fail("Test Case Failed. The Workflows restricted to other classes were also available.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 39.1.42 : Listing of Workflow that is enabled only for a particular class (Metadatacard-Side Pane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Listing of Workflow that is enabled only for a particular class (Metadatacard-Side Pane)")
	public void SprintTest39_1_42(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Search for an object
			//------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("1. Search for an object.", driver);

			//2. Open the Metadatacard of the object
			//---------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("2. Open the Metadatacard of the object.", driver);

			//Verification: To verify if a Workflow that is available for a different class is not available for this object
			//---------------------------------------------------------------------------------------------------------------
			List<String> workflows = metadatacard.getAvailableWorkflows();
			if(!workflows.contains(dataPool.get("RestrictedWorkflow")))
				Log.pass("Test Case Passed. The Workflows restricted to other classes were not displayed.", driver);
			else
				Log.fail("Test Case Failed. The Workflows restricted to other classes were also available.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}	

	/**
	 * 39.1.43 : Listing of Workflow that is enabled only for a particular class (Workflow dialog)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Listing of Workflow that is enabled only for a particular class (Workflow dialog)")
	public void SprintTest39_1_43(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Search for an object
			//------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("1. Search for an object.", driver);

			//2. Open the Metadatacard of the object
			//---------------------------------------
			homePage.listView.rightClickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Workflow.Value);
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			MFilesDialog mFilesDialog = new MFilesDialog(driver);

			Log.message("2. Open the Metadatacard of the object.", driver);

			//Verification: To verify if a Workflow that is available for a different class is not available for this object
			//---------------------------------------------------------------------------------------------------------------
			List<String> workflows = mFilesDialog.getWorkflows();
			if(!workflows.contains(dataPool.get("RestrictedWorkflow")))
				Log.pass("Test Case Passed. The Workflows restricted to other classes were not displayed.", driver);
			else
				Log.fail("Test Case Failed. The Workflows restricted to other classes were also available.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}


	/**
	 * 39.1.63 : As a normal M-Files user try to perfrom a state transition that is not allowed (Metadatacard)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "As a normal M-Files user try to perfrom a state transition that is not allowed (Metadatacard)")
	public void SprintTest39_1_63(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			driver.get(loginURL);
			Utils.fluentWait(driver);
			LoginPage loginPage = new LoginPage(driver);
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("User"), dataPool.get("Password"), testVault);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.", driver);

			//2. Search for an object with workflow
			//--------------------------------------
			Utils.fluentWait(driver);
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object with workflow.", driver);

			//3.Open the properties of an object
			//-----------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			Log.message("3.Open the properties of an object.", driver);

			//4. Set the Workflow to the object
			//----------------------------------
			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver);

			Utils.fluentWait(driver);
			metadatacard.setWorkflow(dataPool.get("Workflow"));
			Utils.fluentWait(driver);
			metadatacard.saveAndClose();
			Log.message("4. Set the Workflow to the object.", driver);

			//Verification: To verify if only the allowed states are enabled
			//--------------------------------------------------------------
			Utils.fluentWait(driver);

			String[] disabledStates = dataPool.get("DisabledStates").split("\n");
			String[] enabledStates = dataPool.get("EnabledStates").split("\n");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			metadatacard = new MetadataCard(driver);

			List <String> availableStates = metadatacard.getAvailableStates();

			//Verifying that all expected states are available
			for(int i = 0; i < enabledStates.length; ++i)
			{
				Boolean found = false;

				//What state transitions are available
				for(int j = 0; j < availableStates.size(); ++j)
				{	
					//Verifying that expected state transition is found in available state transitions
					if(enabledStates[i].equals(availableStates.get(j)))
					{
						found = true;
					}
				}

				if(!found)
				{
					Log.fail("Test Case Failed. A state transition that should be allowed is not available. - " + enabledStates[i], driver);
				}
			}

			//Verifying that no unexpected state transitions are available
			for(int i = 0; i < disabledStates.length; ++i)
			{
				//What state transitions are available
				for(int j = 0; j < availableStates.size(); ++j)
				{
					if(disabledStates[i].equals(availableStates.get(j)))
					{
						Log.fail("Test Case Failed. A state transition that should not be allowed is available. - " + disabledStates[i], driver);
					}
				}
			}

			//Verifying that no unexpected state transitiosn are available
			if(availableStates.size() > enabledStates.length)
			{
				Log.fail("Test Case Failed. Unexpected state transitions were available: Expected state transitions: " 
						+ enabledStates.length + ". Available state transitions: " + availableStates.size(), driver);
			}
			else
			{
				Log.pass("Test Case Passed. Only the allowed state transitions are available.", driver);
			}

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 39.1.64 : As a normal M-Files user try to perfrom a state transition that is not allowed (Metadatacard-side pane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "As a normal M-Files user try to perfrom a state transition that is not allowed (Metadatacard-side pane)")
	public void SprintTest39_1_64(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			driver.get(loginURL);
			Utils.fluentWait(driver);
			LoginPage loginPage = new LoginPage(driver);
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("User"), dataPool.get("Password"), testVault);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.", driver);

			//2. Search for an object with workflow
			//--------------------------------------
			Utils.fluentWait(driver);
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object with workflow.", driver);

			//3.Open the properties of an object
			//-----------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);

			Log.message("3.Open the properties of an object.", driver);

			//4. Set the Workflow to the object
			//----------------------------------
			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver, true);

			Utils.fluentWait(driver);
			metadatacard.setWorkflow(dataPool.get("Workflow"));
			Utils.fluentWait(driver);
			metadatacard.saveAndClose();

			Log.message("4. Set the Workflow to the object.", driver);

			//Verification: To verify if only the allowed states are enabled
			//--------------------------------------------------------------
			Utils.fluentWait(driver);

			String[] disabledStates = dataPool.get("DisabledStates").split("\n");
			String[] enabledStates = dataPool.get("EnabledStates").split("\n");

			metadatacard = new MetadataCard(driver, true);
			List <String> availableStates = metadatacard.getAvailableStates();

			//Verifying that all expected states are available
			for(int i = 0; i < enabledStates.length; ++i)
			{
				Boolean found = false;

				//What state transitions are available
				for(int j = 0; j < availableStates.size(); ++j)
				{	
					//Verifying that expected state transition is found in available state transitions
					if(enabledStates[i].equals(availableStates.get(j)))
					{
						found = true;
					}
				}

				if(!found)
				{
					Log.fail("Test Case Failed. A state transition that should be allowed is not available. - " + enabledStates[i], driver);
				}
			}

			//Verifying that no unexpected state transitions are available
			for(int i = 0; i < disabledStates.length; ++i)
			{
				//What state transitions are available
				for(int j = 0; j < availableStates.size(); ++j)
				{
					if(disabledStates[i].equals(availableStates.get(j)))
					{
						Log.fail("Test Case Failed. A state transition that should not be allowed is available. - " + disabledStates[i], driver);
					}
				}
			}

			//Verifying that no unexpected state transitiosn are available
			if(availableStates.size() > enabledStates.length)
			{
				Log.fail("Test Case Failed. Unexpected state transitions were available: Expected state transitions: " 
						+ enabledStates.length + ". Available state transitions: " + availableStates.size(), driver);
			}
			else
			{
				Log.pass("Test Case Passed. Only the allowed state transitions are available.", driver);
			}

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 39.1.65 : As a normal M-Files user try to perfrom a state transition that is not allowed (Workflow Dialog)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "As a normal M-Files user try to perfrom a state transition that is not allowed (Workflow Dialog)")
	public void SprintTest39_1_65(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			driver.get(loginURL);
			Utils.fluentWait(driver);
			LoginPage loginPage = new LoginPage(driver);
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("User"), dataPool.get("Password"), testVault);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.", driver);

			//2. Search for an object with workflow
			//--------------------------------------
			Utils.fluentWait(driver);
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object with workflow.", driver);

			//3.Open the Workflow dialog of an object
			//----------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);
			Utils.fluentWait(driver);

			Log.message("3.Open the Workflow dialog of an object.", driver);

			//4. Set the Workflow to the object
			//----------------------------------
			Utils.fluentWait(driver);
			MFilesDialog mFilesDialog = new MFilesDialog(driver);

			Utils.fluentWait(driver);
			mFilesDialog.setWorkflow(dataPool.get("Workflow"));
			Utils.fluentWait(driver);
			mFilesDialog.setWorkflowState(dataPool.get("State"));

			mFilesDialog.clickOkButton();

			Log.message("4. Set the Workflow to the object.", driver);

			//Verification: To verify if only the allowed states are enabled
			//--------------------------------------------------------------
			Utils.fluentWait(driver);

			String[] disabledStates = dataPool.get("DisabledStates").split("\n");
			String[] enabledStates = dataPool.get("EnabledStates").split("\n");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);

			mFilesDialog = new MFilesDialog(driver);
			List<String> availableStates = mFilesDialog.getWorkflowStates();

			mFilesDialog.clickCloseButton();

			//Verifying that all expected states are available
			for(int i = 0; i < enabledStates.length; ++i)
			{
				Boolean found = false;

				//What state transitions are available
				for(int j = 0; j < availableStates.size(); ++j)
				{	
					//Verifying that expected state transition is found in available state transitions
					if(enabledStates[i].equals(availableStates.get(j)))
					{
						found = true;
					}
				}

				if(!found)
				{
					Log.fail("Test Case Failed. A state transition that should be allowed is not available. - " + enabledStates[i], driver);
				}
			}

			//Verifying that no unallowed state transitions are available
			for(int i = 0; i < disabledStates.length; ++i)
			{
				//What state transitions are available
				for(int j = 0; j < availableStates.size(); ++j)
				{
					if(disabledStates[i].equals(availableStates.get(j)))
					{
						Log.fail("Test Case Failed. A state transition that should not be allowed is available. - " + disabledStates[i], driver);
					}
				}
			}

			//Verifying that no unexpected state transitiosn are available
			if(availableStates.size() > enabledStates.length)
			{
				Log.fail("Test Case Failed. Unexpected state transitions were available: Expected state transitions: " 
						+ enabledStates.length + ". Available state transitions: " + availableStates.size(), driver);
			}
			else
			{
				Log.pass("Test Case Passed. Only the allowed state transitions are available.", driver);
			}

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 39.1.66 : As a normal M-Files user try to perfrom a state transition that is not allowed (Task Pane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows","Bug"}, 
			description = "As a normal M-Files user try to perfrom a state transition that is not allowed (Task Pane)")
	public void SprintTest39_1_66(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			driver.get(loginURL);
			Utils.fluentWait(driver);
			LoginPage loginPage = new LoginPage(driver);
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("User"), dataPool.get("Password"), testVault);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.", driver);

			//2. Search for an object with workflow
			//--------------------------------------
			Utils.fluentWait(driver);
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object with workflow.", driver);

			//3.Open the Workflow dialog of an object
			//----------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);
			Utils.fluentWait(driver);

			Log.message("3.Open the Workflow dialog of an object.", driver);

			//4. Set the Workflow to the object
			//----------------------------------
			Utils.fluentWait(driver);
			MFilesDialog mFilesDialog = new MFilesDialog(driver);

			Utils.fluentWait(driver);
			mFilesDialog.setWorkflow(dataPool.get("Workflow"));
			Utils.fluentWait(driver);

			mFilesDialog.clickOkButton();
			Utils.fluentWait(driver);

			Log.message("4. Set the Workflow to the object.", driver);

			//Verification: To verify if only the allowed states are displayed
			//----------------------------------------------------------------
			Utils.fluentWait(driver);
			String[] disabledStates = dataPool.get("DisabledStates").split("\n");
			String[] enabledStates = dataPool.get("EnabledStates").split("\n");
			Utils.fluentWait(driver);

			for(int count = 0; count < disabledStates.length; count++)
				if(homePage.taskPanel.isItemExists(disabledStates[count]))
					Log.fail("Test Case Failed. A state that is not allowed is displayed. - " + disabledStates[count], driver);

			Utils.fluentWait(driver);
			for(int count = 0; count < enabledStates.length; count++)
				if(!homePage.taskPanel.isItemExists(enabledStates[count]))
					Log.fail("Test Case Failed. A state that is allowed is not displayed. - " + enabledStates[count], driver);

			Log.pass("Test Case Passed. Only the allowed states are in enabled state.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);

		}

		finally {

			Utility.quitDriver(driver);
		}
	}

}

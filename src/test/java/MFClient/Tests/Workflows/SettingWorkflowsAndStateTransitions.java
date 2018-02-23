package MFClient.Tests.Workflows;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.Utils;
import genericLibrary.WebDriverUtils;

import java.util.ArrayList;
import java.util.Arrays;
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
import MFClient.Wrappers.ListView;
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.MenuBar;
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class SettingWorkflowsAndStateTransitions {

	public String xlTestDataWorkBook = null;
	public String loginURL = null;
	public String userName = null;
	public String password = null;
	public String testVault = null;
	public String configURL = null;
	public String userFullName = null;
	public String className = null;
	public String productVersion = null;
	public WebDriver driver = null;

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
	 * 39.1.1A : All related workflow states should be displayed in taskpane after selecting an object with workflow
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows", "Smoke"}, 
			description = "All related workflow states should be displayed in taskpane after selecting an object with workflow.")
	public void SprintTest39_1_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			//1. Navigate to the view
			//-----------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("SearchType"), dataPool.get("Object"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//2. Click on the object with workflow and state set
			//--------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("Object '" + dataPool.get("Object") + "' does not exist in the vault.");

			if (!homePage.listView.clickItem(dataPool.get("Object")))
				throw new Exception("Object '" + dataPool.get("Object") + "' is not selected..");

			Utils.fluentWait(driver);

			Log.message("2. Object (" + dataPool.get("Object") + ") with workflow is selected.");

			//3. Read the available states in taskpane
			//-----------------------------------------
			if(!homePage.taskPanel.isItemExists(Caption.MenuItems.StateTransition.Value))
				throw new Exception("The category '" + Caption.MenuItems.StateTransition.Value + "' was not found in the task pane");

			String[] expectedStates = dataPool.get("ExpectedStates").split("\n");

			Log.message("3. Workflow states displayed in taskpane after selecting an object.");

			//Verification: To verify if the expected States are present in the Task Pane
			//-----------------------------------------------------------------------------
			String addlInfo = "";

			for(int count =0; count < expectedStates.length; count++) 
				if(!homePage.taskPanel.isItemExists(expectedStates[count])) {
					addlInfo = addlInfo + "; " + expectedStates[count];
					break;
				}

			if(addlInfo.equals("")) 
				Log.pass("Test Case Passed. The expected workflow states are displayed in taskpane. Workflow States : " + Arrays.toString(expectedStates));
			else
				Log.fail("Test Case Failed. One or more workflow states are missing. Additional Information : " +
						"</br>Required Workflow States : " + Arrays.toString(expectedStates) + "</br> Missing Workflow states : " + addlInfo, driver);

		}//End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest39_1_1A

	/**
	 * 39.1.1B : Change the workflow state of an object through taskpane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows", "Smoke"}, 
			description = "Change the workflow state of an object through taskpane.")
	public void SprintTest39_1_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			//1. Navigate to the view
			//-----------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("SearchType"), dataPool.get("Object"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//2. Click on the object with workflow and state set
			//--------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("Object '" + dataPool.get("Object") + "' does not exist in the vault.");

			if (!homePage.listView.clickItem(dataPool.get("Object")))
				throw new Exception("Object '" + dataPool.get("Object") + "' is not selected.");

			Utils.fluentWait(driver);

			Log.message("2. Object (" + dataPool.get("Object") + ") with workflow is selected.");

			//3. Select workflow state of an object from taskpane
			//----------------------------------------------------
			if(!homePage.taskPanel.isItemExists(dataPool.get("State")))
				throw new Exception("The State '" + dataPool.get("State") + "' was not found in the task pane");

			homePage.taskPanel.clickItem(dataPool.get("State"));
			Utils.fluentWait(driver);

			Log.message("3. Workflow state (" + dataPool.get("State") + ") is selected from taskpane.");

			//4. Save the changes in workflow state dialog
			//--------------------------------------------
			if(!MFilesDialog.exists(driver))
				throw new Exception("Change Workflow state dialog does not appear on selecting the workflow state from task pane");

			MFilesDialog mFilesDialog = new MFilesDialog(driver); 

			if (!mFilesDialog.getWorkflowState().equals(dataPool.get("State")))
				throw new Exception("Workflow state (" + dataPool.get("State") + ") has not got selected in change state dialog.");

			mFilesDialog.clickOkButton();
			Utils.fluentWait(driver);

			Log.message("4. Workflow state changes are saved in Change workflow dialog.");

			//Verification: To verify if the expected Workflow and State are set in the Change Workflow dialog
			//-------------------------------------------------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("Object")))
				throw new Exception("Object '" + dataPool.get("Object") + "' is not selected.");

			MetadataCard metadatacard = new MetadataCard(driver, true);

			if(metadatacard.getWorkflowState().equals(dataPool.get("State")))
				Log.pass("Test Case Passed. Workflow state change is successful through taskpane.");
			else
				Log.fail("Test Case Failed. The State was not set after saving changes in Change workflow dialog.", driver);
		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest39_1_1B



	/**
	 * 39.1.3 : When automatic state transition is enabled the target state should be set to the object as soon as the source state is set
	 */
	/*@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", dependsOnMethods={"SprintTest39_1_2B"}, groups = {"Sprint39", "Workflows"}, 
			description = "When automatic state transition is enabled the target state should be set to the object as soon as the source state is set.")*/
	public void SprintTest39_1_3(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		MFilesDialog mFilesDialog = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object with workflow
			//--------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object with workflow.");

			//3. Click on the object with workflow and state set.
			//------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);

			Log.message("3. Clicked on the object with workflow and state set.");

			//4. Set a state from the task pane
			//----------------------------------
			String states[] = dataPool.get("State").split("\n");

			for (int i=0; i<states.length; i++) {

				if(!homePage.taskPanel.isItemExists(states[i]))
					throw new Exception("The State '" + states[i] + "' was not found in the task pane");

				homePage.taskPanel.clickItem(states[i]);
				Utils.fluentWait(driver);

				mFilesDialog = new MFilesDialog(driver);
				mFilesDialog.clickOkButton();
				Utils.fluentWait(driver);
			}

			Log.message("4. Set a state from the task pane.");

			//5. Open the Change Workflow dialog for the object
			//-------------------------------------------------
			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);
			Utils.fluentWait(driver);

			mFilesDialog = new MFilesDialog(driver);

			//Verification: To verify if the target state of auto-transition is set to the object
			//------------------------------------------------------------------------------------
			if(mFilesDialog.getWorkflowState().equals(dataPool.get("TargetState"))) 
				Log.pass("Test Case Passed. The Target state of automatic transition was set to the object as expected.");
			else
				Log.fail("Test Case Failed. Instead of the target state - '" + dataPool.get("TargetState") + "', '" + mFilesDialog.getWorkflowState() + "' was set to the object.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if (driver != null)
			{
				try
				{
					if(MFilesDialog.exists(driver))
						mFilesDialog.clickCancelButton();
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		}

	}	

	/**
	 * 39.1.7 : Set Workflow and state for an object without workflow
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows", "Smoke"}, 
			description = "Set Workflow and state for an object without workflow.")
	public void SprintTest39_1_7(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object with workflow
			//--------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object with workflow.");

			//3. Click on the object without workflow and state set.
			//------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);

			if(homePage.taskPanel.isItemExists(Caption.MenuItems.StateTransition.Value))
				throw new SkipException("Invalid Test Data. Specify an object without worklfow.");

			Log.message("3. Clicked on the object with workflow and state set.");

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

			Log.message("4. Set a workflow to the object.");

			//Verification: To verify if all the states of the workflow are displayed
			//------------------------------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver, true);
			Utils.fluentWait(driver);

			if(metadatacard.getWorkflowState().equals(dataPool.get("State")) && metadatacard.getWorkflow().equals(dataPool.get("Workflow")))
				Log.pass("Test Case Passed. Workflow state change is successful through workflow dialog.");
			else
				Log.fail("Test Case Failed. The State was not set after saving changes in Change workflow dialog.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}	

	/**
	 * 39.1.11 : Set Workflow and clear state (Metadatacard)
	 */
	/*@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Set Workflow and clear state (Metadatacard)")*/
	public void SprintTest39_1_11(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object with workflow
			//--------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object with workflow.");

			//3. Open the properties of a document
			//-------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			Log.message("3. Open the properties of a document.");

			//4. Set the Workflow and clear the state
			//----------------------------------------
			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard( driver);

			metadatacard.setWorkflow(dataPool.get("Workflow"));
			Utils.fluentWait(driver);
			metadatacard.setWorkflowState("");
			Utils.fluentWait(driver);

			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("4. Set the Workflow and clear the state");

			//Verification: To verify if the workflow is set and state remains empty
			//-----------------------------------------------------------------------
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			metadatacard = new MetadataCard( driver);

			if(!metadatacard.getWorkflow().equals(dataPool.get("Workflow")))
				throw new Exception("The Workflow did not remain set after the State was cleared.");

			if(metadatacard.getWorkflowState().equals(""))
				Log.pass("Test Case Passed. The Workflow was set and the state remained empty.");
			else
				Log.fail("Test Case Failed. The state did not remain empty after it was cleared.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}	

	/**
	 * 39.1.12 : Automatic state transition (metadatacard)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Automatic state transition (metadatacard)")
	public void SprintTest39_1_12(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object with workflow
			//--------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object with workflow.");

			//3. Open the properties of a document that already has a workflow
			//-----------------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("Object")))
				throw new Exception("The specified object '" + dataPool.get("Object") + "' is not selected.");

			Utils.fluentWait(driver);
			Log.message("3. Open the properties of a document in right pane.");

			MetadataCard metadatacard = null;
			String states[] = dataPool.get("State").split("\n");

			for (int i=0; i<states.length; i++) {
				metadatacard = new MetadataCard(driver, true);
				metadatacard.setWorkflowState(states[i]);
				Utils.fluentWait(driver);
				metadatacard.saveAndClose();
			}

			Log.message("4. Set the Workflow and state with automatic transition.");

			//Verification: To verify if automatic state transition works in metadatacard
			//----------------------------------------------------------------------------
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			metadatacard = new MetadataCard(driver);

			if(!metadatacard.getWorkflow().equals(dataPool.get("Workflow")))
				throw new Exception("The Workflow did not remain set after the State was cleared.");

			if(metadatacard.getWorkflowState().equalsIgnoreCase(dataPool.get("TargetState")))
				Log.pass("Test Case Passed. Automatic Transition works as expected.");
			else
				Log.fail("Test Case Failed. Automatic transition to the target state did not happen.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}	

	/**
	 * 39.1.14A : Removing Workflow from an object (Change State dialog)
	 */
	/*@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Removing Workflow from an object (Change State dialog)")*/
	public void SprintTest39_1_14A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object with workflow
			//--------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object with workflow.");

			//3. Open the Change State dialog of a document
			//-----------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);
			Utils.fluentWait(driver);

			Log.message("3. Open the Change State dialog of a document.");

			//4. Remove the Workflow from the Object
			//---------------------------------------
			Utils.fluentWait(driver);
			MFilesDialog mFilesDialog = new MFilesDialog(driver);

			if(mFilesDialog.getWorkflow().equals(""))
				throw new SkipException("Invalid Test Data. Specify an object with workflow.");

			mFilesDialog.setWorkflow("");
			Utils.fluentWait(driver);

			mFilesDialog.clickOkButton();
			Utils.fluentWait(driver);

			Log.message("4. Remove the Workflow from the Object");

			//5. Re-open the Workflow dialog
			//-------------------------------
			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			mFilesDialog = new MFilesDialog(driver);

			Log.message("5. Re-open the Workflow dialog");

			//Verification: To verify if the Workflow and state are removed from the object
			//------------------------------------------------------------------------------
			if(mFilesDialog.getWorkflowState().equals("") && mFilesDialog.getWorkflow().equals(""))
				Log.pass("Test Case Passed. The Workflow and state were empty as the workflow was removed.");
			else
				Log.fail("Test Case Failed. The workflow and state field were not empty.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}	

	/**
	 * 39.1.14B : Removing Workflow from an object (Metadatacard)
	 */
	/*@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Removing Workflow from an object (Metadatacard)")*/
	public void SprintTest39_1_14B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object with workflow
			//--------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object with workflow.");

			//3. Open the Change State dialog of a document
			//-----------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			Log.message("3. Open the Change State dialog of a document.");

			//4. Remove the Workflow from the Object
			//---------------------------------------
			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver);

			if(metadatacard.getWorkflow().equals(""))
				throw new SkipException("Invalid Test Data. Specify an object with workflow.");

			metadatacard.setWorkflow("");
			Utils.fluentWait(driver);

			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("4. Remove the Workflow from the Object.");

			//5. Re-open the matadatacard
			//----------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			metadatacard = new MetadataCard(driver);

			Log.message("5. Re-open the matadatacard.");

			//Verification: To verify if the Workflow and state are removed from the object
			//------------------------------------------------------------------------------
			if(metadatacard.getWorkflowState().equals("") && metadatacard.getWorkflow().equals(""))
				Log.pass("Test Case Passed. The Workflow and state were empty as the workflow was removed.");
			else
				Log.fail("Test Case Failed. The workflow and state field were not empty.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}	

	/**
	 * 39.1.15A : Set Workflow to an object with no workflow (Metadatacard Verificaiton)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Set Workflow to an object with no workflow (Metadatacard Verificaiton)")
	public void SprintTest39_1_15A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object with workflow
			//--------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object with workflow.");

			//3. Open the Properties dialog of a document
			//-----------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			Log.message("3. Open the Properties dialog of a document.");

			//4. Set the Workflow from the Object
			//---------------------------------------
			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver);


			/*	if(!metadatacard.getWorkflow().equals(""))
				throw new SkipException("Invalid Test Data. Specify an object without workflow.");*/

			metadatacard.setWorkflow(dataPool.get("Workflow"));
			Utils.fluentWait(driver);

			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("4. Set the Workflow from the Object");

			//5. Re-open the matadatacard
			//----------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			metadatacard = new MetadataCard(driver);

			Log.message("5. Re-open the matadatacard.");


			//Verification: To verify if the Workflow and state are removed from the object
			//------------------------------------------------------------------------------
			if(metadatacard.getWorkflowState().equals(dataPool.get("FirstState")) && metadatacard.getWorkflow().equals(dataPool.get("Workflow")))
				Log.pass("Test Case Passed. The Workflow and first state was set to the object.");
			else
				Log.fail("Test Case Failed. The Workflow and state were not set as expected.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}	

	/**
	 * 39.1.15B : Set Workflow and state to an object with no workflow (Metadatacard verification)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Set Workflow and state to an object with no workflow (Metadatacard verification)")
	public void SprintTest39_1_15B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object with workflow
			//--------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object with workflow.");

			//3. Open the Properties dialog of a document
			//-----------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			Log.message("3. Open the Properties dialog of a document.");

			//4. Set the Workflow from the Object
			//---------------------------------------
			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver);

			if(!metadatacard.getWorkflow().equals(""))
				throw new SkipException("Invalid Test Data. Specify an object without workflow.");

			metadatacard.setWorkflow(dataPool.get("Workflow"));
			Utils.fluentWait(driver);

			metadatacard.setWorkflowState(dataPool.get("State"));
			Utils.fluentWait(driver);

			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("4. Set the Workflow from the Object.");

			//5. Re-open the matadatacard
			//----------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			metadatacard = new MetadataCard(driver);

			Log.message("5. Re-open the matadatacard.");

			//Verification: To verify if the Workflow and state are removed from the object
			//------------------------------------------------------------------------------
			if(metadatacard.getWorkflowState().equals(dataPool.get("State")) && metadatacard.getWorkflow().equals(dataPool.get("Workflow")))
				Log.pass("Test Case Passed. The Workflow and first state was set to the object.");
			else
				Log.fail("Test Case Failed. The Workflow and state were not set as expected.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}	

	/**
	 * 39.1.17 : Open the Change Workflow dialog using Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Open the Change Workflow dialog using Operations menu")
	public void SprintTest39_1_17(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object
			//------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object.");

			//3. Click on the object
			//-----------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);

			Log.message("3. Clicked on the object.");

			//4. Perfrom the operation menu click
			//------------------------------------
			MenuBar menuBar = new MenuBar(driver);
			menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);
			Utils.fluentWait(driver);

			Log.message("4. Perfromed the operation menu click.");

			//Verification: To verify if the Workflow dialog is opened
			//---------------------------------------------------------
			if(!MFilesDialog.exists(driver))
				throw new Exception("The Workflow Dialog did not appear, when tried through Operations menu");

			MFilesDialog mFilesDialog = new MFilesDialog(driver);

			if(mFilesDialog.getTitle().equals(Caption.MenuItems.Workflow.Value +" - " + dataPool.get("Object").split("\\.")[0]))
				Log.pass("Test Case Passed. The Workflow dialog was opened when tried through operations menu.");
			else
				Log.fail("Test Case Failed. The Workflow Dialog did not appear, when tried through Operations menu.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}	

	/**
	 * 39.1.18 : Open the Change Workflow dialog using Operations menu (Object without workflow)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Open the Change Workflow dialog using Operations menu (Object without workflow)")
	public void SprintTest39_1_18(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object
			//------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object.");

			//3. Click on the object
			//-----------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);

			Log.message("3. Clicked on the object.");

			//4. Perfrom the operation menu click
			//------------------------------------
			MenuBar menuBar = new MenuBar(driver);
			menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);
			Utils.fluentWait(driver);

			Log.message("4. Perfromed the operation menu click.");

			//Verification: To verify if the Workflow dialog is opened
			//---------------------------------------------------------
			if(!MFilesDialog.exists(driver))
				throw new Exception("The Workflow Dialog did not appear, when tried through Operations menu");

			MFilesDialog mFilesDialog = new MFilesDialog(driver);

			if(!mFilesDialog.getTitle().equals(Caption.MenuItems.Workflow.Value +" - " + dataPool.get("Object").split("\\.")[0]))
				throw new Exception("The Workflow Dialog did not appear, when tried through Operations menu.");

			if(mFilesDialog.getWorkflow().equals("") && mFilesDialog.getWorkflowState().equals(""))
				Log.pass("Test Case Passed. Thw Workflow dialog appeared with empty workflow and state fields.");
			else
				Log.fail("Test Case Failed. The Workflow dialog appeared but the workflow and state field were not empty.", driver);


		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}	

	/**
	 * 39.1.19 : Open the Change Workflow dialog using Operations menu (Object with workflow)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows", "Smoke"}, 
			description = "Open the Change Workflow dialog using Operations menu (Object with workflow)")
	public void SprintTest39_1_19(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object
			//------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			MetadataCard metadatacard = new MetadataCard(driver, true);

			metadatacard.setWorkflow(dataPool.get("Workflow"));
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("2. Searched for an object.");

			//3. Perfrom the operation menu click
			//------------------------------------
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);
			Utils.fluentWait(driver);

			Log.message("3. Perfromed the operation menu click.");

			//Verification: To verify if the Workflow dialog is opened
			//---------------------------------------------------------
			Utils.fluentWait(driver);
			if(!MFilesDialog.exists(driver))
				throw new Exception("The Workflow Dialog did not appear, when tried through Operations menu");

			MFilesDialog mFilesDialog = new MFilesDialog(driver);

			if(!mFilesDialog.getTitle().equals(Caption.MenuItems.Workflow.Value +" - " + dataPool.get("Object").split("\\.")[0]))
				throw new Exception("The Workflow Dialog did not appear, when tried through Operations menu.");

			if(mFilesDialog.getWorkflow().equals(dataPool.get("Workflow")))
				Log.pass("Test Case Passed. Thw Workflow dialog appeared with empty workflow and state fields.");
			else
				Log.fail("Test Case Failed. The Workflow dialog appeared but the workflow and state field were not empty.", driver);


		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}	

	/**
	 * 39.1.20 : Set the Workflow, State and Comments for an object (Context Menu)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Set the Workflow, State and Comments for an object (Context Menu)")
	public void SprintTest39_1_20(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object
			//------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object.");

			//3. Set the Workflow. state and comments to the object
			//------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			MenuBar menuBar = new MenuBar(driver);
			menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);
			Utils.fluentWait(driver);

			MFilesDialog mFilesDialog = new MFilesDialog(driver);

			mFilesDialog.setWorkflow(dataPool.get("Workflow"));
			mFilesDialog.setWorkflowState(dataPool.get("State"));
			mFilesDialog.setWorkflowComments(dataPool.get("Comments"));
			mFilesDialog.clickOkButton();
			Utils.fluentWait(driver);

			Log.message("3. Set the Workflow. state and comments to the object");

			//Verification: To verify if the Workflow dialog is opened
			//---------------------------------------------------------
			Utils.fluentWait(driver);

			MetadataCard metadatacard = new MetadataCard(driver, true);
			ArrayList<String> comments = metadatacard.getComments();

			if(metadatacard.getWorkflow().equals(dataPool.get("Workflow")) && metadatacard.getWorkflowState().equals(dataPool.get("State"))
					&& comments.get(comments.size()-1).equals(dataPool.get("Comments")))
				Log.pass("Test Case Passed. The Workflow, state and comments were set through the Workflow dialog.");
			else
				Log.fail("Test Case Failed. The Workflow, state and the comments were not set through the Workflow dialog..", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}	

	/**
	 * 39.1.21 : Set Workflow and state to an object while it is checked out (Task Pane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Set Workflow and state to an object while it is checked out (Task Pane)")
	public void SprintTest39_1_21(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object
			//------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);
			Utils.fluentWait(driver);

			MFilesDialog mFilesDialog = new MFilesDialog(driver);

			mFilesDialog.setWorkflow(dataPool.get("Workflow"));
			mFilesDialog.clickOkButton();
			Utils.fluentWait(driver);

			Log.message("2. Searched for an object.");

			//3. Check out the object
			//-----------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);

			Log.message("3. Check out the object");

			//4. Change the state of the object by clicking the listed state in the task pane
			//-------------------------------------------------------------------------------
			if (!homePage.taskPanel.isItemExists(dataPool.get("State")))
				throw new SkipException("Workflow State (" + dataPool.get("State") + ") does not exists in the taskpanel.");

			homePage.taskPanel.clickItem(dataPool.get("State"));
			Utils.fluentWait(driver);
			mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.clickOkButton();
			Utils.fluentWait(driver);

			Log.message("4. Change the state of the object by clicking the listed state in the task pane.");

			//5. Check In the object
			//-----------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);
			Utils.fluentWait(driver);

			Log.message("5. Check In the object");

			//Verification: To verify if the State change is persistent
			//----------------------------------------------------------
			Utils.fluentWait(driver);

			MetadataCard metadatacard = new MetadataCard(driver, true);

			if(metadatacard.getWorkflow().equals(dataPool.get("Workflow")) && metadatacard.getWorkflowState().equals(dataPool.get("State")))
				Log.pass("Test Case Passed. Changing Workflow state for a checkedout object through task pane works as expected.");
			else
				Log.fail("Test Case Failed. Changing Workflow state for a checkedout object through task pane did not work as expected.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}	

	/**
	 * 39.1.22 : Set Workflow and state to an object while it is checked out (Workflow Dialog)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Set Workflow and state to an object while it is checked out (Workflow Dialog)")
	public void SprintTest39_1_22(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object
			//------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object.");

			//3. Check out the object
			//-----------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);

			Log.message("3. Check out the object");

			//4. Change the state of the object through the workflow dialog
			//--------------------------------------------------------------
			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);
			Utils.fluentWait(driver);
			MFilesDialog mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.setWorkflow(dataPool.get("Workflow"));
			mFilesDialog.setWorkflowState(dataPool.get("State"));
			mFilesDialog.clickOkButton();
			Utils.fluentWait(driver);

			Log.message("4. Change the state of the object through the workflow dialog.");

			//5. Check In the object
			//-----------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);
			Utils.fluentWait(driver);

			Log.message("5. Check In the object");

			//Verification: To verify if the State change is persistent
			//----------------------------------------------------------
			Utils.fluentWait(driver);

			MetadataCard metadatacard = new MetadataCard(driver, true);

			if(metadatacard.getWorkflow().equals(dataPool.get("Workflow")) && metadatacard.getWorkflowState().equals(dataPool.get("State")))
				Log.pass("Test Case Passed. Changing Workflow state for a checkedout object through task pane works as expected.");
			else
				Log.fail("Test Case Failed. Changing Workflow state for a checkedout object through task pane did not work as expected.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}	

	/**
	 * 39.1.23 : Set Workflow and state to an object while it is checked out (Metadatacard)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Set Workflow and state to an object while it is checked out (Metadatacard)")
	public void SprintTest39_1_23(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object
			//------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object.");

			//3. Check out the object
			//-----------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("Object")))
				throw new Exception("Object (" + dataPool.get("Object") + ") is not checked out.");

			Log.message("3. Check out the object");

			//4. Change the state of the object by through the metadatacard
			//--------------------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver, true);

			metadatacard.setWorkflow(dataPool.get("Workflow"));
			metadatacard.setWorkflowState(dataPool.get("State"));
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("4. Change the state of the object by through the metadatacard.");

			//5. Check In the object
			//-----------------------
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);
			Utils.fluentWait(driver);

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("Object")))
				throw new Exception("Object (" + dataPool.get("Object") + ") is not checked in.");

			Log.message("5. Check In the object");

			//Verification: To verify if the State change is persistent
			//----------------------------------------------------------
			Utils.fluentWait(driver);
			metadatacard = new MetadataCard(driver, true);

			if(metadatacard.getWorkflow().equals(dataPool.get("Workflow")) && metadatacard.getWorkflowState().equals(dataPool.get("State")))
				Log.pass("Test Case Passed. Changing Workflow state for a checkedout object through task pane works as expected.");
			else
				Log.fail("Test Case Failed. Changing Workflow state for a checkedout object through task pane did not work as expected.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 39.1.24 : Set Workflow and state to an object while it is checked out (Task Pane) (Undo Checkout)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Set Workflow and state to an object while it is checked out (Task Pane) (Undo Checkout)")
	public void SprintTest39_1_24(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object
			//------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			MFilesDialog mFilesDialog = new MFilesDialog(driver);

			mFilesDialog.setWorkflow(dataPool.get("Workflow"));
			Utils.fluentWait(driver);

			String expectedState = mFilesDialog.getWorkflowState();

			if(expectedState.equals(dataPool.get("State")))
				throw new SkipException("Invalid Test data. The Workflow and state has already been set to the object");

			mFilesDialog.clickOkButton();
			Utils.fluentWait(driver);

			Log.message("2. Searched for an object.");

			//3. Check out the object
			//-----------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);

			Log.message("3. Check out the object");

			//4. Change the state of the object by clicking the listed state in the task pane
			//-------------------------------------------------------------------------------
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(dataPool.get("State"));
			Utils.fluentWait(driver);
			mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.clickOkButton();
			Utils.fluentWait(driver);

			Log.message("4. Change the state of the object by clicking the listed state in the task pane.");

			//5. Undo Checkout the object
			//------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);
			Utils.fluentWait(driver);

			mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.confirmUndoCheckOut(true);
			Utils.fluentWait(driver);

			Log.message("5. Undo Checkout the object.");

			//Verification: To verify if the State change is persistent
			//----------------------------------------------------------
			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver, true);
			Utils.fluentWait(driver);

			if(metadatacard.getWorkflowState().equals(expectedState))
				Log.pass("Test Case Passed. Changing Workflow state for a checkedout object through task pane works as expected.");
			else
				Log.fail("Test Case Failed. Changing Workflow state for a checkedout object through task pane did not work as expected.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}	

	/**
	 * 39.1.25 : Set Workflow and state to an object while it is checked out (Workflow Dialog)(Undo Checkout)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Set Workflow and state to an object while it is checked out (Workflow Dialog)(Undo Checkout)")
	public void SprintTest39_1_25(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object
			//------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object.");

			//3. Check out the object
			//-----------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("Object")))
				throw new Exception("Object (" + dataPool.get("Object") + ") is not checked out.");

			Log.message("3. Check out the object");

			//4. Change the state of the object through the workflow dialog
			//--------------------------------------------------------------
			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);
			Utils.fluentWait(driver);
			MFilesDialog mFilesDialog = new MFilesDialog(driver);

			String expectedWorkflow = mFilesDialog.getWorkflow();
			String expectedState = mFilesDialog.getWorkflowState();

			if(expectedWorkflow.equals(dataPool.get("Workflow")) && expectedState.equals(dataPool.get("State")))
				throw new SkipException("Invalid Test data. The Workflow and state has already been set to the object");

			mFilesDialog.setWorkflow(dataPool.get("Workflow"));
			mFilesDialog.setWorkflowState(dataPool.get("State"));
			mFilesDialog.clickOkButton();
			Utils.fluentWait(driver);

			Log.message("4. Change the state of the object through the workflow dialog.");

			//5. Undo Checkout the object
			//------------------------------
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);

			mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.confirmUndoCheckOut(true);
			Utils.fluentWait(driver);

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("Object")))
				throw new Exception("Object (" + dataPool.get("Object") + ") is not undo checked out.");

			Log.message("5. Undo Checkout the object.");

			//Verification: To verify if the State change is persistent
			//----------------------------------------------------------
			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver, true);

			if(metadatacard.getWorkflow().equals(expectedWorkflow) && metadatacard.getWorkflowState().equals(expectedState))
				Log.pass("Test Case Passed. Changing Workflow state for a checkedout object through task pane works as expected.");
			else
				Log.fail("Test Case Failed. Changing Workflow state for a checkedout object through task pane did not work as expected.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}	

	/**
	 * 39.1.26 : Set Workflow and state to an object while it is checked out (Metadatacard-Side Pane)(Undo Checkout)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Set Workflow and state to an object while it is checked out (Metadatacard-Side Pane)(Undo Checkout)")
	public void SprintTest39_1_26(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object
			//------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object.");

			//3. Check out the object
			//-----------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("Object")))
				throw new Exception("Object (" + dataPool.get("Object") + ") is not checked out.");

			Log.message("3. Check out the object");

			//4. Change the state of the object by through the metadatacard
			//--------------------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver, true);

			String expectedWorkflow = metadatacard.getWorkflow();
			String expectedState = metadatacard.getWorkflowState();

			if(expectedWorkflow.equals(dataPool.get("Workflow")) && expectedState.equals(dataPool.get("State")))
				throw new SkipException("Invalid Test data. The Workflow and state has already been set to the object");

			metadatacard.setWorkflow(dataPool.get("Workflow"));
			metadatacard.setWorkflowState(dataPool.get("State"));
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("4. Change the state of the object by through the metadatacard.");

			//5. Undo Checkout the object
			//------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);
			Utils.fluentWait(driver);

			MFilesDialog mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.confirmUndoCheckOut(true);
			Utils.fluentWait(driver);

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("Object")))
				throw new Exception("Object (" + dataPool.get("Object") + ") is not undo checked out.");

			Log.message("5. Undo Checkout the object.");

			//Verification: To verify if the State change is persistent
			//----------------------------------------------------------
			Utils.fluentWait(driver);
			metadatacard = new MetadataCard(driver, true);

			if(metadatacard.getWorkflow().equals(expectedWorkflow) && metadatacard.getWorkflowState().equals(expectedState))
				Log.pass("Test Case Passed. Changing Workflow state for a checkedout object through task pane works as expected.");
			else
				Log.fail("Test Case Failed. Changing Workflow state for a checkedout object through task pane did not work as expected.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}


	/**
	 * 39.1.34 : Set Workflow and state to an object while it is checked out (Metadatacard-Popout)(Undo Checkout)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Set Workflow and state to an object while it is checked out (Metadatacard-Side Pane)(Undo Checkout)")
	public void SprintTest39_1_34(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object
			//------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object.");

			//3. Check out the object
			//-----------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);

			Log.message("3. Check out the object");

			//4. Change the state of the object by through the metadatacard
			//--------------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver);

			String expectedWorkflow = metadatacard.getWorkflow();
			String expectedState = metadatacard.getWorkflowState();

			if(expectedWorkflow.equals(dataPool.get("Workflow")) && expectedState.equals(dataPool.get("State")))
				throw new SkipException("Invalid Test data. The Workflow and state has already been set to the object");

			metadatacard.setWorkflow(dataPool.get("Workflow"));
			metadatacard.setWorkflowState(dataPool.get("State"));
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("4. Change the state of the object by through the metadatacard.");

			//5. Undo checkout the object
			//-----------------------------
			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);
			Utils.fluentWait(driver);

			MFilesDialog mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.confirmUndoCheckOut(true);
			Utils.fluentWait(driver);

			Log.message("5. Undo checkout the object.");

			//Verification: To verify if the State change is persistent
			//----------------------------------------------------------
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			metadatacard = new MetadataCard(driver, true);

			if(metadatacard.getWorkflow().equals(expectedWorkflow) && metadatacard.getWorkflowState().equals(expectedState))
				Log.pass("Test Case Passed. Changing Workflow state for a checkedout object through task pane works as expected.");
			else
				Log.fail("Test Case Failed. Changing Workflow state for a checkedout object through task pane did not work as expected.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}



	/**
	 * 39.1.48 : When automatic state transition is enabled the target state should be set to the checked out object as soon as the object is checked in
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "When automatic state transition is enabled the target state should be set to the checked out object as soon as the object is checked in.")
	public void SprintTest39_1_48(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object with workflow
			//--------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object with workflow.");

			//3. Check out the object and set state 
			//--------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);

			MetadataCard metadatacard = new MetadataCard(driver, true);
			metadatacard.setWorkflow(dataPool.get("Workflow"));
			metadatacard.setInfo(dataPool.get("Property"));
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			String states[] = dataPool.get("State").split("\n");

			for (int i=0; i<states.length; i++) {
				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("Object")))
					throw new Exception("Object (" + dataPool.get("Object") + ") is not checked out.");

				metadatacard = new MetadataCard(driver, true);
				metadatacard.setWorkflowState(states[i]);
				Utils.fluentWait(driver);
				metadatacard.saveAndClose();

				//The last check in is done later because it causes the automatic state transition
				if(i < states.length - 1)
				{
					homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);
				}
			}

			Log.message("3. Set workflow and state to the object.");

			//5. Check-In the object
			//-----------------------
			Utils.fluentWait(driver);
			metadatacard = new MetadataCard(driver, true);

			if(metadatacard.getWorkflowState().equals(dataPool.get("TargetState")))
				throw new Exception("The state changed before checking in the object.");

			driver.switchTo().defaultContent();

			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);
			Utils.fluentWait(driver);

			Log.message("4. Check in the object to cause automatic state transition.");

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("TargetObject")))
				throw new Exception("Object (" + dataPool.get("TargetObject") + ") is not checked in.");

			Utils.fluentWait(driver);
			metadatacard = new MetadataCard(driver, true);
			Utils.fluentWait(driver);

			//Verification: To verify if the target state of auto-transition is set to the object
			//------------------------------------------------------------------------------------
			if(metadatacard.getWorkflowState().equals(dataPool.get("TargetState"))) 
				Log.pass("Test Case Passed. The Target state of automatic transition was set to the object as expected.");
			else
				Log.fail("Test Case Failed. Instead of the target state - '" + dataPool.get("TargetState") + "', '" + metadatacard.getWorkflowState() + "' was set to the object.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}	

	/**
	 * 39.1.54 : Automatic state transition (metadatacard-Side Pane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Automatic state transition (metadatacard-Side Pane)")
	public void SprintTest39_1_54(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object
			//------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object.");

			//3. Click the object
			//--------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);

			Log.message("3. Click the object.");

			//4. Set the Workflow and state with automatic transition
			//--------------------------------------------------------
			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard( driver, true);

			metadatacard.setWorkflow(dataPool.get("Workflow"));
			Utils.fluentWait(driver);
			metadatacard.setInfo(dataPool.get("Property"));
			Utils.fluentWait(driver);

			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			String states[] = dataPool.get("State").split("\n");

			for (int i=0; i<states.length; i++) {
				metadatacard = new MetadataCard(driver, true);
				metadatacard.setWorkflowState(states[i]);
				Utils.fluentWait(driver);
				metadatacard.saveAndClose();
			}

			Log.message("4. Set the Workflow and state with automatic transition.");

			//Verification: To verify if automatic state transition works in metadatacard
			//----------------------------------------------------------------------------
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			metadatacard = new MetadataCard(driver);

			if(!metadatacard.getWorkflow().equals(dataPool.get("Workflow")))
				throw new Exception("The Workflow did not remain set after the automatic state transition.");

			if(metadatacard.getWorkflowState().equals(dataPool.get("TargetState")))
				Log.pass("Test Case Passed. Automatic transition to the target state worked as expected.");
			else
				Log.fail("Test Case Failed. Automatic transition to the target state did not work as expected.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}	

}

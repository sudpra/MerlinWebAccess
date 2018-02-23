package MFClient.Tests.Workflows;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.Utils;
import genericLibrary.WebDriverUtils;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.openqa.selenium.By;
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
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class WorkflowStateActions {

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
	 * 39.1.8 : Set a state with action to an object - TaskPane (Eg. Convert to PDF)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Set a state with action to an object - TaskPane (Eg. Convert to PDF).")
	public void SprintTest39_1_8(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			MetadataCard metadatacard = new MetadataCard(driver, true);
			metadatacard.setWorkflow(dataPool.get("Workflow"));
			metadatacard.setInfo(dataPool.get("Property"));
			metadatacard.saveAndClose();

			String states[] = dataPool.get("State").split("\n");

			for (int i=0; i<states.length; i++) {

				if(!homePage.taskPanel.isItemExists(states[i]))
					throw new SkipException("Invalid Test Data. The state " + states[i] + " was not listed in the task pane.");
				else
				{
					homePage.taskPanel.clickItem(states[i]);
					Utils.fluentWait(driver);

					if(!MFilesDialog.exists(driver))
						throw new Exception("The Workflow dialog did not appear.");

					MFilesDialog mFilesDialog = new MFilesDialog(driver);

					if(!mFilesDialog.getWorkflowState().equals(states[i]))
						throw new Exception("The state was not set to the object");

					mFilesDialog.clickOkButton();
					Utils.fluentWait(driver);

				}
			}


			Log.message("3. Set the action with state to the object.");

			//Verification: To verify if the state action works
			//--------------------------------------------------
			//Utils.fluentWait(driver);
			homePage.searchPanel.search("pdf", dataPool.get("SearchType"));
			Utils.fluentWait(driver);
			if(homePage.listView.isItemExists(dataPool.get("Object").split("\\.")[0]+".pdf") && !homePage.listView.isItemExists(dataPool.get("Object")))
				Log.pass("Test Case Passed. The state action works as expeceted.");
			else
				Log.fail("Test Case Failed. The state action did not work as expeceted.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}	

	/**
	 * 39.1.9 : Set a state with action to an object - Properties dialog (Eg. Convert to PDF)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Set a state with action to an object - Properties dialog (Eg. Convert to PDF)")
	public void SprintTest39_1_9(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//4. Set the action with state to the object
			//--------------------------------------------
			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard( driver);

			metadatacard.setWorkflow(dataPool.get("Workflow"));
			metadatacard.setInfo(dataPool.get("Property"));
			metadatacard.saveAndClose();

			String states[] = dataPool.get("State").split("\n");

			for (int i=0; i<states.length; i++) {
				homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
				metadatacard = new MetadataCard(driver);
				metadatacard.setWorkflowState(states[i]);
				Utils.fluentWait(driver);
				metadatacard.saveAndClose();

			}

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			metadatacard = new MetadataCard(driver);

			if(!metadatacard.getWorkflow().equals(dataPool.get("Workflow")) || !metadatacard.getWorkflowState().equals(states[states.length - 1]))
				throw new Exception("The Workflow and state were not set to the object.");

			metadatacard.cancelAndConfirm();

			Log.message("4. Set the action with state to the object");

			//Verification: To verify if the state action works
			//--------------------------------------------------

			homePage.searchPanel.search(dataPool.get("Object").split("\\.")[0], dataPool.get("SearchType"));
			if(homePage.listView.isItemExists(dataPool.get("Object").split("\\.")[0]+".pdf") && !homePage.listView.isItemExists(dataPool.get("Object")))
				Log.pass("Test Case Passed. The state action works as expeceted.");
			else
				Log.fail("Test Case Failed. The state action did not work as expeceted.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}	


	/**
	 * 39.1.13 : Set a state with action to an object - Workflow dialog (Eg. Assigned to user)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Set a state with action to an object - Workflow dialog (Eg. Assigned to user)")
	public void SprintTest39_1_13(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//--------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object with workflow
			//--------------------------------------
			Utils.fluentWait(driver);
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object with workflow.");

			//3. Open the Worklfow dialog of a document
			//--------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);
			Utils.fluentWait(driver);

			Log.message("3. Open the Worklfow dialog of a document.");

			//4. Set the Workflow and state with automatic transition
			//--------------------------------------------------------
			Utils.fluentWait(driver);
			MFilesDialog mFilesDialog = new MFilesDialog(driver);

			mFilesDialog.setWorkflow(dataPool.get("Workflow"));
			Utils.fluentWait(driver);
			mFilesDialog.setWorkflowState(dataPool.get("State"));
			Utils.fluentWait(driver);

			mFilesDialog.clickOkButton();
			Utils.fluentWait(driver);

			Log.message("4. Set the Workflow and state with automatic transition.");

			//Verification: To verify if automatic state transition works in metadatacard
			//----------------------------------------------------------------------------
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver, true);

			Utils.fluentWait(driver);
			if(!metadatacard.propertyExists(dataPool.get("ReferenceProperty")))
				throw new SkipException("Invalid Test Data. Specify an object which has '" + dataPool.get("ReferenceProperty") + "' property.");

			Utils.fluentWait(driver);
			if(!metadatacard.propertyExists(dataPool.get("PropertyName")))
				throw new Exception("The '" + dataPool.get("PropertyName") + "' was not added to the object.");

			if(metadatacard.getPropertyValue(dataPool.get("PropertyName")).equals(metadatacard.getPropertyValue(dataPool.get("ReferenceProperty"))))
				Log.pass("Test Case Passed. The Assign to action was executed successfully.");
			else
				Log.fail("Test Case Failed. The state action was not successful", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}	

	/**
	 * 39.1.49 : Set a state with action to an object - Properties dialog (Eg. Convert to PDF) (Side Pane)
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Set a state with action to an object - Properties dialog (Eg. Convert to PDF) (Side Pane)")
	public void SprintTest39_1_49(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			Log.message("3. Open the properties of a document.");

			//4. Set the action with state to the object
			//--------------------------------------------
			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver, true);
			metadatacard.setInfo(dataPool.get("Property"));

			metadatacard.setWorkflow(dataPool.get("Workflow"));
			Utils.fluentWait(driver);
			metadatacard.saveAndClose();

			String states[] = dataPool.get("State").split("\n");

			for (int i=0; i<states.length; i++) {
				metadatacard = new MetadataCard(driver, true);
				metadatacard.setWorkflowState(states[i]);
				Utils.fluentWait(driver);
				metadatacard.saveAndClose();
			}

			metadatacard = new MetadataCard(driver, true);

			if(!metadatacard.getWorkflow().equals(dataPool.get("Workflow")) || !metadatacard.getWorkflowState().equals(states[states.length-1]))
				throw new Exception("The Workflow and state were not set to the object.");

			Log.message("4. Set the action with state to the object");

			//Verification: To verify if the state action works
			//--------------------------------------------------
			driver.switchTo().defaultContent();
			homePage.searchPanel.search(dataPool.get("Object").split("\\.")[0], dataPool.get("SearchType"));

			if(homePage.listView.isItemExists(dataPool.get("Object").split("\\.")[0] + ".pdf") && !homePage.listView.isItemExists(dataPool.get("Object")))
				Log.pass("Test Case Passed. The state action works as expeceted.");
			else
				Log.fail("Test Case Failed. The state action did not work as expeceted.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}              


	/**
	 * 39.1.50 : Set a state with action to an checked out object - Properties dialog (Eg. Convert to PDF) (Side Pane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Set a state with action to an checked out object - Properties dialog (Eg. Convert to PDF) (Side Pane).")
	public void SprintTest39_1_50(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object with workflow
			//--------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object with workflow.");

			//3. Check out the object
			//------------------------
			homePage.listView.clickItem(dataPool.get("Object"));

			Log.message("3. Check out the object.");

			//4. Set a state to the object
			//-----------------------------
			MetadataCard metadatacard = new MetadataCard(driver, true);
			metadatacard.setWorkflow(dataPool.get("Workflow"));
			metadatacard.setInfo(dataPool.get("Property"));
			metadatacard.saveAndClose();

			String states[] = dataPool.get("State").split("\n");

			for (int i=0; i<states.length; i++) {
				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
				metadatacard = new MetadataCard(driver, true);
				metadatacard.setWorkflowState(states[i]);
				Utils.fluentWait(driver);
				metadatacard.saveAndClose();

				//The last check in is done later because it causes the state action.
				if(i < states.length - 1)
				{
					homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);
				}
			}

			Log.message("4. Set a state to the object.");

			//5. Check-In the object
			//-----------------------
			Utils.fluentWait(driver);
			driver.switchTo().defaultContent();
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new Exception("The action was executed before the object was checked in.");

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);
			Utils.fluentWait(driver);

			metadatacard = new MetadataCard(driver, true);

			if(!metadatacard.getWorkflow().equals(dataPool.get("Workflow")) || !metadatacard.getWorkflowState().equals(states[states.length-1]))
				throw new Exception("The Workflow and state were not set to the object.");

			driver.switchTo().defaultContent();

			//Verification: To verify if the state action works
			//--------------------------------------------------

			homePage.searchPanel.search(dataPool.get("Object").split("\\.")[0], dataPool.get("SearchType"));

			if(homePage.listView.isItemExists(dataPool.get("Object").split("\\.")[0]+".pdf") && !homePage.listView.isItemExists(dataPool.get("Object")))
				Log.pass("Test Case Passed. The state action works as expeceted.");
			else
				Log.fail("Test Case Failed. The state action did not work as expeceted.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}	

	/**
	 * 39.1.51 : Set a state with action to an object - Properties dialog (Object that cannot be converted to PDF) (Metadatacard)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Set a state with action to an object - Properties dialog (Object that cannot be converted to PDF) (Metadatacard).")
	public void SprintTest39_1_51(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//3. Open the metadatacard of the object
			//--------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			Log.message("3. Open the metadatacard of the object.");

			//4. Set the worklfow and state to the object
			//--------------------------------------------
			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver);

			metadatacard.setInfo(dataPool.get("Property"));

			metadatacard.setWorkflow(dataPool.get("Workflow"));
			Utils.fluentWait(driver);
			metadatacard.saveAndClose();
			String states[] = dataPool.get("State").split("\n");

			for (int i=0; i<states.length; i++) {
				metadatacard = new MetadataCard(driver, true);
				metadatacard.setWorkflowState(states[i]);
				Utils.fluentWait(driver);
				metadatacard.saveAndClose();
			}


			/*	if(!metadatacard.getWorkflow().equals(dataPool.get("Workflow")) || !metadatacard.getWorkflowState().equals(states[states.length-1]))
				throw new Exception("The Workflow and state were not set to the object.");*/

			Utils.fluentWait(driver);

			Log.message("4. Set the worklfow and state to the object.");

			//Verification: To verify if Warning appears
			//-------------------------------------------
			Utils.fluentWait(driver);
			if(driver.findElement(By.cssSelector("div[class='shortErrorArea']")).getText().contains(dataPool.get("ExpectedMessage")))
				Log.pass("Test Case Passed. The Warning dialog appeared as expected.");
			else
				Log.fail("Test Case Failed. The warning dialog appeared, but it did not have the expected message.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}	

	/**
	 * 39.1.52 : Set a state with action to an object - Properties dialog (Object that cannot be converted to PDF) (Metadatacard-side pane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Set a state with action to an object - Properties dialog (Object that cannot be converted to PDF) (Metadatacard-side pane).")
	public void SprintTest39_1_52(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			Log.message("3. Object is selected in the view.");

			//4. Set the pre-condition (Owner)
			//----------------------------------

			MetadataCard metadatacard = new MetadataCard(driver, true);
			metadatacard.setInfo(dataPool.get("Property"));
			metadatacard.saveAndClose();

			Log.message("4. Pre-Condition for the workflow is set in the metadata card");

			//5. Set the worklfow and state to the object
			//--------------------------------------------
			metadatacard = new MetadataCard(driver, true);
			metadatacard.setWorkflow(dataPool.get("Workflow"));
			metadatacard.saveAndClose();

			String states[] = dataPool.get("State").split("\n");

			for (int i=0; i<states.length; i++) {
				metadatacard = new MetadataCard(driver, true);
				metadatacard.setWorkflowState(states[i]);
				Utils.fluentWait(driver);
				metadatacard.saveAndClose();
			}

			Log.message("5. Set the worklfow and state to the object.");

			//Verification: To verify if Warning appears
			//-------------------------------------------
			Utils.fluentWait(driver);
			if(driver.findElement(By.cssSelector("div[class='shortErrorArea']")).getText().contains(dataPool.get("ExpectedMessage")))
				Log.pass("Test Case Passed. The Warning dialog appeared as expected.");
			else
				Log.fail("Test Case Failed. The warning dialog appeared, but it did not have the expected message.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 39.1.53 : Set a state with action to an object - Properties dialog (Object that cannot be converted to PDF) (Worklfow dialog)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows","Bug"}, 
			description = "Set a state with action to an object - Properties dialog (Object that cannot be converted to PDF) (Worklfow dialog).")
	public void SprintTest39_1_53(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//3. Select the object in the view
			//--------------------------------

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);

			Log.message("3. Object is selected in the view.");

			//4. Set the pre-condition (Owner)
			//----------------------------------

			MetadataCard metadatacard = new MetadataCard(driver, true);
			metadatacard.setInfo(dataPool.get("Property"));
			metadatacard.saveAndClose();

			Log.message("4. Pre-Condition for the workflow is set in the metadata card");

			//5. Open the metadatacard of the object
			//--------------------------------------

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);
			Utils.fluentWait(driver);

			Log.message("5. Open the metadatacard of the object.");

			//6. Set the worklfow and state to the object
			//--------------------------------------------
			Utils.fluentWait(driver);
			MFilesDialog mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.setWorkflow(dataPool.get("Workflow"));
			Utils.fluentWait(driver);
			mFilesDialog.clickOkButton();

			String states[] = dataPool.get("State").split("\n");

			for (int i=0; i<states.length; i++) {
				Utils.fluentWait(driver);
				homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);
				Utils.fluentWait(driver);
				mFilesDialog = new MFilesDialog(driver);
				mFilesDialog.setWorkflowState(states[i]);
				Utils.fluentWait(driver);
				mFilesDialog.clickOkButton();
			}
			Log.message("6. Set the worklfow and state to the object.");

			//Verification: To verify if Warning appears
			//-------------------------------------------
			if(driver.findElement(By.cssSelector("div[class='shortErrorArea']")).getText().contains(dataPool.get("ExpectedMessage")))
				Log.pass("Test Case Passed. The Warning dialog appeared as expected.");
			else
				Log.fail("Test Case Failed. The warning dialog appeared, but it did not have the expected message.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}


	/**
	 * 39.1.55 : Set a state with action to an object - Metadatacard dialog (Eg. Assigned to user)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Set a state with action to an object - Workflow dialog (Eg. Assigned to user)")
	public void SprintTest39_1_55(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			Utils.fluentWait(driver);
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

			//4. Set the Workflow and state with automatic transition
			//--------------------------------------------------------
			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver);

			Utils.fluentWait(driver);
			if(!metadatacard.propertyExists(dataPool.get("ReferenceProperty")))
				throw new SkipException("Invalid Test Data. Specify an object which has '" + dataPool.get("ReferenceProperty") + "' property.");

			metadatacard.setWorkflow(dataPool.get("Workflow"));
			Utils.fluentWait(driver);
			metadatacard.setWorkflowState(dataPool.get("State"));
			Utils.fluentWait(driver);

			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			Log.message("4. Set the Workflow and state with automatic transition.");

			//Verification: To verify if automatic state transition works in metadatacard
			//----------------------------------------------------------------------------
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			metadatacard = new MetadataCard(driver, true);

			Utils.fluentWait(driver);
			if(!metadatacard.propertyExists(dataPool.get("PropertyName")))
				throw new Exception("The '" + dataPool.get("PropertyName") + "' was not added to the object.");

			if(metadatacard.getPropertyValue(dataPool.get("PropertyName")).equals(metadatacard.getPropertyValue(dataPool.get("ReferenceProperty"))))
				Log.pass("Test Case Passed. The Assign to action was executed successfully.");
			else
				Log.fail("Test Case Failed. The state action was not successful", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 39.1.56 : Set a state with action to an object - Metadatacard-side pane (Eg. Assigned to user)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Set a state with action to an object - Metadatacard-side pane (Eg. Assigned to user)")
	public void SprintTest39_1_56(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			Utils.fluentWait(driver);
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object with workflow.");

			//3. Open the properties of a document
			//-------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);

			Log.message("3. Open the properties of a document.");

			//4. Set the Workflow and state with automatic transition
			//--------------------------------------------------------
			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver, true);

			Utils.fluentWait(driver);
			if(!metadatacard.propertyExists(dataPool.get("ReferenceProperty")))
				throw new SkipException("Invalid Test Data. Specify an object which has '" + dataPool.get("ReferenceProperty") + "' property.");

			metadatacard.setWorkflow(dataPool.get("Workflow"));
			Utils.fluentWait(driver);
			metadatacard.setWorkflowState(dataPool.get("State"));
			Utils.fluentWait(driver);

			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("4. Set the Workflow and state with automatic transition.");

			//Verification: To verify if automatic state transition works in metadatacard
			//----------------------------------------------------------------------------
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			metadatacard = new MetadataCard(driver, true);

			Utils.fluentWait(driver);
			if(!metadatacard.propertyExists(dataPool.get("PropertyName")))
				throw new Exception("The '" + dataPool.get("PropertyName") + "' was not added to the object.");

			if(metadatacard.getPropertyValue(dataPool.get("PropertyName")).equals(metadatacard.getPropertyValue(dataPool.get("ReferenceProperty"))))
				Log.pass("Test Case Passed. The Assign to action was executed successfully.");
			else
				Log.fail("Test Case Failed. The state action was not successful", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 39.1.57 : Set a state with action to a checked out object - Metadatacard (Eg. Convert to PDF) (Undo Check out)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Set a state with action to a checked out object - Metadatacard (Eg. Convert to PDF) (Undo Check out)")
	public void SprintTest39_1_57(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//3. Check out the object Open its properties
			//--------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);

			Log.message("3. Check out the object Open its properties.");

			//4. Set the action with state to the object
			//--------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard.setInfo(dataPool.get("Property"));
			metadatacard.setWorkflow(dataPool.get("Workflow"));
			Utils.fluentWait(driver);
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);
			Utils.fluentWait(driver);
			String states[] = dataPool.get("State").split("\n");

			for (int i=0; i<states.length; i++) {
				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
				Utils.fluentWait(driver);
				metadatacard = new MetadataCard(driver, true);
				metadatacard.setWorkflowState(states[i]);
				Utils.fluentWait(driver);
				metadatacard.saveAndClose();
				if(i < states.length - 1)
					homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);
			}

			/*metadatacard.saveAndClose();*/
			Utils.fluentWait(driver);

			Log.message("4. Set the action with state to the object");

			//5. Undo checkout
			//-----------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);
			MFilesDialog mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.confirmUndoCheckOut(true);
			Utils.fluentWait(driver);

			Log.message("5. Undo checkout the checkout object.");

			/*if(!metadatacard.getWorkflow().equals(dataPool.get("Workflow")) || !metadatacard.getWorkflowState().equals(dataPool.get("State")))
				throw new Exception("The Workflow and state were not set to the object.");*/

			//Verification: To verify if the state action works
			//--------------------------------------------------
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			homePage.searchPanel.search(dataPool.get("Object").split("\\.")[0], dataPool.get("SearchType"));
			Utils.fluentWait(driver);
			if(!homePage.listView.isItemExists(dataPool.get("Object").split("\\.")[0]+".pdf") && homePage.listView.isItemExists(dataPool.get("Object")))
				Log.pass("Test Case Passed. The state action was not executed as expected.");
			else
				Log.fail("Test Case Failed. The state action was executed even after undo checkout was done.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}	

	/**
	 * 39.1.58 : Set a state with action to a checked out object - Metadatacard - side pane (Eg. Convert to PDF) (Undo Check out)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Set a state with action to a checked out object - Metadatacard - side pane (Eg. Convert to PDF) (Undo Check out)")
	public void SprintTest39_1_58(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//3. Check out the object Open its properties
			//--------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);

			Log.message("3. Check out the object Open its properties.");

			//4. Set the action with state to the object
			//--------------------------------------------
			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver, true);

			metadatacard.setWorkflow(dataPool.get("Workflow"));
			metadatacard.setInfo(dataPool.get("Property"));
			metadatacard.saveAndClose();

			String states[] = dataPool.get("State").split("\n");

			for (int i=0; i<states.length; i++) {
				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
				metadatacard = new MetadataCard(driver, true);
				metadatacard.setWorkflowState(states[i]);
				Utils.fluentWait(driver);
				metadatacard.saveAndClose();

				//Check in is not done for last state transition because undo checkout is used later instead
				if(i < states.length - 1)
				{
					homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);
				}
			}


			Log.message("4. Set the action with state to the object");

			//5. Undo checkout
			//-----------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);

			MFilesDialog mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.confirmUndoCheckOut(true);

			Log.message("5. Undo checkout.");

			//Verification: To verify if the state action works
			//--------------------------------------------------

			homePage.searchPanel.search(dataPool.get("Object").split("\\.")[0], dataPool.get("SearchType"));

			if(!homePage.listView.isItemExists(dataPool.get("Object").split("\\.")[0]+".pdf") && homePage.listView.isItemExists(dataPool.get("Object")))
				Log.pass("Test Case Passed. The state action was not executed as expected.");
			else
				Log.fail("Test Case Failed. The state action was executed even after undo checkout was done.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}	

	/**
	 * 39.1.59 : Set a state with action to a checked out object - Workflow Dialog (Eg. Convert to PDF) (Undo Check out)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Set a state with action to a checked out object - Workflow Dialog (Eg. Convert to PDF) (Undo Check out)")
	public void SprintTest39_1_59(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//3. Check out the object Open its properties
			//--------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			MetadataCard metadatacard = new MetadataCard(driver, true);
			metadatacard.setInfo(dataPool.get("Property"));
			metadatacard.saveAndClose();

			Log.message("3. Check out the object Open its properties.");

			//4. Set the action with state to the object
			//--------------------------------------------

			String states[] = dataPool.get("State").split("\n");

			for (int i=0; i<states.length; i++) {
				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
				homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);

				MFilesDialog mFilesDialog = new MFilesDialog(driver);
				mFilesDialog.setWorkflow(dataPool.get("Workflow"));
				mFilesDialog.setWorkflowState(states[i]);
				mFilesDialog.clickOkButton();

				//Check in is not done for last state transition because undo checkout is used later instead
				if(i < states.length - 1)
				{
					homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);
				}
			}

			Utils.fluentWait(driver);

			Log.message("4. Set the action with state to the object");

			//5. Undo checkout
			//-----------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);
			Utils.fluentWait(driver);
			MFilesDialog mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.confirmUndoCheckOut(true);
			Utils.fluentWait(driver);

			Log.message("5. Undo checkout.");

			//Verification: To verify if the state action works
			//--------------------------------------------------
			Utils.fluentWait(driver);

			homePage.searchPanel.search(dataPool.get("Object").split("\\.")[0], dataPool.get("SearchType"));
			Utils.fluentWait(driver);
			if(!homePage.listView.isItemExists(dataPool.get("Object").split("\\.")[0]+".pdf") && homePage.listView.isItemExists(dataPool.get("Object")))
				Log.pass("Test Case Passed. The state action was not executed as expected.");
			else
				Log.fail("Test Case Failed. The state action was executed even after undo checkout was done.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}	

	/**
	 * 39.1.60 : Set a state with action to a checked out object - Metadatacard (Eg. Assign to user) (Undo Check out)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Set a state with action to a checked out object - Metadatacard (Eg. Assign to user) (Undo Check out)")
	public void SprintTest39_1_60(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			Utils.fluentWait(driver);
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object with workflow.");

			//3. Checout and open the properties of an object
			//------------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			Log.message("3. Checout and open the properties of an object.");

			//4. Set the Workflow and state with automatic transition
			//--------------------------------------------------------
			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver);

			Utils.fluentWait(driver);
			if(!metadatacard.propertyExists(dataPool.get("ReferenceProperty")))
				throw new SkipException("Invalid Test Data. Specify an object which has '" + dataPool.get("ReferenceProperty") + "' property.");

			metadatacard.setWorkflow(dataPool.get("Workflow"));
			Utils.fluentWait(driver);
			metadatacard.setWorkflowState(dataPool.get("State"));
			Utils.fluentWait(driver);

			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			Log.message("4. Set the Workflow and state with automatic transition.");

			//5. Perform Undo Checkout
			//-------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);


			MFilesDialog mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.confirmUndoCheckOut(true);
			Utils.fluentWait(driver);

			Log.message("5. Perform Undo Checkout");

			//Verification: To verify if automatic state transition works in metadatacard
			//----------------------------------------------------------------------------
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			metadatacard = new MetadataCard(driver, true);

			if(!metadatacard.propertyExists(dataPool.get("PropertyName"))) {
				Log.pass("Test Case Passed. The Assigned to property was not added to the object.");
				return;
			}

			Utils.fluentWait(driver);
			if(!metadatacard.getPropertyValue(dataPool.get("PropertyName")).equals(metadatacard.getPropertyValue(dataPool.get("ReferenceProperty"))))
				Log.pass("Test Case Passed. The Assign to action was not executed.");
			else
				Log.fail("Test Case Failed. The Assign to action was executed even after undo checkout was done.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 39.1.61 : Set a state with action to a checked out object - Metadatacard - side pane (Eg. Assign to user) (Undo Check out)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Set a state with action to a checked out object - Metadatacard - side pane (Eg. Assign to user) (Undo Check out)")
	public void SprintTest39_1_61(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			Utils.fluentWait(driver);
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object with workflow.");

			//3. Checout and open the properties of an object
			//------------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);

			Log.message("3. Checout and open the properties of an object.");

			//4. Set the Workflow and state with automatic transition
			//--------------------------------------------------------
			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver, true);

			Utils.fluentWait(driver);
			if(!metadatacard.propertyExists(dataPool.get("ReferenceProperty")))
				throw new SkipException("Invalid Test Data. Specify an object which has '" + dataPool.get("ReferenceProperty") + "' property.");

			metadatacard.setWorkflow(dataPool.get("Workflow"));
			Utils.fluentWait(driver);
			metadatacard.setWorkflowState(dataPool.get("State"));
			Utils.fluentWait(driver);

			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			Log.message("4. Set the Workflow and state with automatic transition.");

			//5. Perform Undo Checkout
			//-------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);


			MFilesDialog mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.confirmUndoCheckOut(true);

			Log.message("5. Perform Undo Checkout");

			//Verification: To verify if automatic state transition works in metadatacard
			//----------------------------------------------------------------------------
			Utils.fluentWait(driver);
			metadatacard = new MetadataCard(driver, true);

			if(!metadatacard.propertyExists(dataPool.get("PropertyName"))) {
				Log.pass("Test Case Passed. The Assigned to property was not added to the object.");
				return;
			}

			Utils.fluentWait(driver);
			if(!metadatacard.getPropertyValue(dataPool.get("PropertyName")).equals(metadatacard.getPropertyValue(dataPool.get("ReferenceProperty"))))
				Log.pass("Test Case Passed. The Assign to action was not executed.");
			else
				Log.fail("Test Case Failed. The Assign to action was executed even after undo checkout was done.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 39.1.62 : Set a state with action to a checked out object - Metadatacard (Eg. Assign to user) (Undo Check out)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Set a state with action to a checked out object - Metadatacard (Eg. Assign to user) (Undo Check out)")
	public void SprintTest39_1_62(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			Utils.fluentWait(driver);
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object with workflow.");

			//3. Checout and open the properties of an object
			//------------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);
			Utils.fluentWait(driver);

			Log.message("3. Checout and open the properties of an object.");

			//4. Set the Workflow and state with automatic transition
			//--------------------------------------------------------
			Utils.fluentWait(driver);
			MFilesDialog mFilesDialog = new MFilesDialog(driver);

			Utils.fluentWait(driver);
			mFilesDialog.setWorkflow(dataPool.get("Workflow"));
			Utils.fluentWait(driver);
			mFilesDialog.setWorkflowState(dataPool.get("State"));
			Utils.fluentWait(driver);

			mFilesDialog.clickOkButton();
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			Log.message("4. Set the Workflow and state with automatic transition.");

			//5. Perform Undo Checkout
			//-------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);


			mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.confirmUndoCheckOut(true);
			Utils.fluentWait(driver);

			Log.message("5. Perform Undo Checkout");

			//Verification: To verify if automatic state transition works in metadatacard
			//----------------------------------------------------------------------------
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver, true);

			if(!metadatacard.propertyExists(dataPool.get("PropertyName"))) {
				Log.pass("Test Case Passed. The Assigned to property was not added to the object.");
				return;
			}

			Utils.fluentWait(driver);
			if(!metadatacard.getPropertyValue(dataPool.get("PropertyName")).equals(metadatacard.getPropertyValue(dataPool.get("ReferenceProperty"))))
				Log.pass("Test Case Passed. The Assign to action was not executed.");
			else
				Log.fail("Test Case Failed. The Assign to action was executed even after undo checkout was done.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}


	/**
	 * 39.1.77 : Set a state with action to an object - Metadatacard (Eg. Set Permission)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Set a state with action to an object - Metadatacard (Eg. Set Permission)")
	public void SprintTest39_1_77(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			Utils.fluentWait(driver);
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			homePage.taskPanel.clickItem(Caption.MenuItems.MakeCopy.Value);
			Utils.fluentWait(driver);

			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard.setCheckInImmediately(true);
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("2. Searched for an object with workflow.");

			//3. Open the Worklfow dialog of a document
			//--------------------------------------------
			homePage.listView.clickItemByIndex(homePage.listView.itemCount()-1);
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			Log.message("3. Open the Worklfow dialog of a document.");

			//4. Set the Workflow and state with automatic permssion
			//--------------------------------------------------------
			Utils.fluentWait(driver);
			metadatacard = new MetadataCard(driver);

			metadatacard.setWorkflow(dataPool.get("Workflow"));
			Utils.fluentWait(driver);
			metadatacard.setWorkflowState(dataPool.get("State"));
			Utils.fluentWait(driver);

			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("4. Set the Workflow and state with automatic permssion.");

			//Verification: To verify if automatic state transition works in metadatacard
			//----------------------------------------------------------------------------
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			metadatacard = new MetadataCard(driver, true);

			if(metadatacard.getPermission().equals(dataPool.get("ExpectedPermission")))
				Log.pass("Test Case Passed. The expected permission was set to the object.");
			else
				Log.fail("Test Case Failed. The expected permission was not set to the object", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 39.1.78 : Set a state with action to an object - Metadatacard - Side Pane(Eg. Set Permission)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Set a state with action to an object - Metadatacard - Side Pane(Eg. Set Permission)")
	public void SprintTest39_1_78(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			Utils.fluentWait(driver);
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			homePage.taskPanel.clickItem(Caption.MenuItems.MakeCopy.Value);
			Utils.fluentWait(driver);

			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard.setCheckInImmediately(true);
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("2. Searched for an object with workflow.");

			//3. Open the Worklfow dialog of a document
			//--------------------------------------------
			homePage.listView.clickItemByIndex(homePage.listView.itemCount()-1);
			Utils.fluentWait(driver);

			Log.message("3. Open the Worklfow dialog of a document.");

			//4. Set the Workflow and state with automatic permssion
			//--------------------------------------------------------
			Utils.fluentWait(driver);
			metadatacard = new MetadataCard(driver, true);

			metadatacard.setWorkflow(dataPool.get("Workflow"));
			Utils.fluentWait(driver);
			metadatacard.setWorkflowState(dataPool.get("State"));
			Utils.fluentWait(driver);

			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("4. Set the Workflow and state with automatic permssion.");

			//Verification: To verify if automatic state transition works in metadatacard
			//----------------------------------------------------------------------------
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			metadatacard = new MetadataCard(driver, true);

			if(metadatacard.getPermission().equals(dataPool.get("ExpectedPermission")))
				Log.pass("Test Case Passed. The expected permission was set to the object.");
			else
				Log.fail("Test Case Failed. The expected permission was not set to the object", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 39.1.79 : Set a state with action to an object - Workflow Dialog(Eg. Set Permission)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Set a state with action to an object - Workflow Dialog(Eg. Set Permission)")
	public void SprintTest39_1_79(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			Utils.fluentWait(driver);
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			homePage.taskPanel.clickItem(Caption.MenuItems.MakeCopy.Value);
			Utils.fluentWait(driver);

			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard.setCheckInImmediately(true);
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("2. Searched for an object with workflow.");

			//3. Open the Worklfow dialog of a document
			//--------------------------------------------
			homePage.listView.clickItemByIndex(homePage.listView.itemCount()-1);
			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);
			Utils.fluentWait(driver);

			Log.message("3. Open the Worklfow dialog of a document.");

			//4. Set the Workflow and state with automatic permssion
			//--------------------------------------------------------
			Utils.fluentWait(driver);
			MFilesDialog mFilesDialog = new MFilesDialog(driver);

			mFilesDialog.setWorkflow(dataPool.get("Workflow"));
			Utils.fluentWait(driver);
			mFilesDialog.setWorkflowState(dataPool.get("State"));
			Utils.fluentWait(driver);

			mFilesDialog.clickOkButton();
			Utils.fluentWait(driver);

			Log.message("4. Set the Workflow and state with automatic permssion.");

			//Verification: To verify if automatic state transition works in metadatacard
			//----------------------------------------------------------------------------
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			metadatacard = new MetadataCard(driver, true);

			if(metadatacard.getPermission().equals(dataPool.get("ExpectedPermission")))
				Log.pass("Test Case Passed. The expected permission was set to the object.");
			else
				Log.fail("Test Case Failed. The expected permission was not set to the object", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 39.1.80 : Set a state with action to an object (Eg. Set Permission) (Object is hidden from current user)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint39", "Workflows"}, 
			description = "Set a state with action to an object (Eg. Set Permission) (Object is hidden from current user)")
	public void SprintTest39_1_80(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			Log.message("1. Logged into the Home View.");

			//2. Search for an object with workflow
			//--------------------------------------
			Utils.fluentWait(driver);
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object with workflow.");

			//3. Open the Worklfow dialog of a document
			//--------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			Log.message("3. Open the Worklfow dialog of a document.");

			//4. Set the Workflow and state with automatic permssion
			//--------------------------------------------------------
			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver);

			metadatacard.setWorkflow(dataPool.get("Workflow"));
			Utils.fluentWait(driver);
			metadatacard.setWorkflowState(dataPool.get("State"));
			Utils.fluentWait(driver);
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("4. Set the Workflow and state with automatic permission.");

			//Verification: To verify if automatic state transition works in metadatacard
			//----------------------------------------------------------------------------
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				Log.pass("Test Case Passed. The expected permission was set to the object.");
			else
				Log.fail("Test Case Failed. The expected permission was not set to the object", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

}

package MFClient.Tests.ElectronicSignature;

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
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class ParallelWorkflow {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String userName = null;
	public static String userFullName = null;
	public static String password = null;
	public static String windowsUserName = null;
	public static String windowsUserFullName = null;
	public static String windowsPassword = null;
	public static String domainName = null;
	public static String testVault = null;
	public static String className = null;
	public static String productVersion = null;
	public static WebDriver driver = null;
	public String methodName = null;

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
			userFullName = xmlParameters.getParameter("UserFullName");
			password = xmlParameters.getParameter("Password");
			windowsUserName = xmlParameters.getParameter("WindowsUserName");
			windowsUserFullName = xmlParameters.getParameter("WindowsUserFullName");
			windowsPassword = xmlParameters.getParameter("WindowsPassword");
			domainName = xmlParameters.getParameter("DomainName");
			testVault = xmlParameters.getParameter("VaultName");
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
			Utility.destroyUsers(xlTestDataWorkBook);//Destroys the user in MFServer
			Utility.destroyTestVault();//Destroys the Vault in MFServer

		}//End try

		catch(Exception e){
			throw e;
		}//End Catch
	}//End cleanApp

	/**
	 * PWorkflow_30990 : Verify if approval assignment created on assigning E-sign state change in Non-Assignment object 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"AutomaticStateTransition", "E-Sign"},
			description = "Verify if approval assignment created on assigning E-sign state change in Non-Assignment object")
	public void PWorkflow_30990(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launches login page and logging in

			//Step-1 : Creates the new object with Automatic state transition required property value in the view
			//---------------------------------------------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Employee.Value);//Opens the new Employee object metadata card via New menu bar

			String objName = Utility.getObjectName(methodName) +"_Employee";//Frames the object name

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card
			metadataCard.setInfo(dataPool.get("Properties"));//Sets the required property values in the metadata card
			metadataCard.setPropertyValue("Employee name", objName);//Sets the object name in the metadata card

			if (!metadataCard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyValue")))
				throw new Exception("'" + dataPool.get("Property") + "' is not set with the value '" + dataPool.get("Property") + "' in the metadata card");

			metadataCard.saveAndClose();//Creates the object

			Log.message("1. Employee object('" + objName + "') successfully created with the automatic state transition required property(" + dataPool.get("Property") + ") with value(" + dataPool.get("PropertyValue") + ") in the view.", driver);

			//Step-2 : Sets the ESign worklfow in the metadata card
			//-----------------------------------------------------
			if (!homePage.listView.clickItem(objName))//Checks if object is exists in the list view
				throw new Exception("Object '" + objName + "' is not selected in the view");

			metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadatacard
			metadataCard.setWorkflow(dataPool.get("Workflow"));//Sets the Worklfow in the right pane metadata card
			metadataCard.setWorkflowState(dataPool.get("WorkflowState"));//Sets the workflow state in the metadatacard
			metadataCard.saveAndClose();//Saves the changes in rightpane metadata card

			Log.message("2. Workflow (" + dataPool.get("Workflow") + ") is set and saved the changes in right pane metadata card for the newly created object.", driver);

			//Step-3: Perform State transition which triggers automatic E-Sgin required state transition for the newly created object
			//-----------------------------------------------------------------------------------------------------------------------
			if (!homePage.listView.clickItem(objName))//Selects the object in the view
				throw new Exception("Object '" + objName + "' is not selected in the view");

			if (!homePage.taskPanel.clickItem(dataPool.get("InitialState")))//Clicks the workflow state in the state transition section in the task panel
				throw new Exception("'" + dataPool.get("InitialState") + "' is not clicked from the task pane for the selected object in the view.");

			Log.message("3.1. '" + dataPool.get("InitialState") + "' is clicked from the task pane for the selected object.", driver);

			if (!MFilesDialog.exists(driver, Caption.MenuItems.Workflow.Value))//Checks if workflow state transition dialog is exist or not
				throw new Exception("State transition workflow dialog is not opened while click on the '" + dataPool.get("InitialState") + "' workflow state from task panel.");

			Log.message("3.2. State transition dialog is opened via task panel.", driver);

			MFilesDialog mfDialog = new MFilesDialog(driver, Caption.MenuItems.Workflow.Value);//Instantiates the Workflow M-Files dialog in the view
			mfDialog.clickOkButton();//Clicks OK button in the Workflow dialog for the E-Sign required State transition

			Log.message("3.3. OK button is clicked in the State transition dialog for the state trainsition from '" + dataPool.get("WorkflowState") + "' to '" + dataPool.get("InitialState") + "'.", driver);

			//Step-4: Check if object is E-Signed successfully in the view
			//------------------------------------------------------------
			String result = "";

			if (!MFilesDialog.isESignDialogExist(driver))//Waits and Checks if Electronic Signature dialog is exists or not
				throw new Exception("Automatic state transition which requires electronic signature('" + dataPool.get("InitialState") + "' to '" + dataPool.get("ESignState") + "') is not triggered while performing state transition to the state '" + dataPool.get("InitialState") + "'.");

			Log.message("4.1. E-Sign dialog is displayed after clicking OK button in the state transition dialog.", driver);

			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog
			mfDialog.eSign(userName, password, "", "");//E-Signs the state transition

			Log.message("4.2. Credentials are entered and clicked the sign button.", driver);

			metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadata card

			if (!metadataCard.getWorkflowState().equalsIgnoreCase(dataPool.get("ESignState")))//Checks if required state is set in the metadata card
				result += "Object(" + objName + ") is not E-Signed successfully. Expected workflow state(" + dataPool.get("ESignState") + ") is not set in the metadatacard(Actual : "+ metadataCard.getWorkflowState() + ").";		

			if (!metadataCard.propertyExists(dataPool.get("SignatureProperty")))//Checks if signature property is added into the metadata card
				result += "Object(" + objName + ") is not E-Signed successfully. "+ dataPool.get("SignatureProperty") +" is not exist in the metadata card.";

			if (!result.equals(""))
				throw new Exception("Automatic state transition('" + dataPool.get("InitialState") + "' to '" + dataPool.get("ESignState") + "') which requires electronic signature is not performed successfully while performing state transition to the state '" + dataPool.get("InitialState") + "'.[Additional info. : "+ result.trim() +"]");

			Log.message("4.3. Automatic state transition('" + dataPool.get("InitialState") + "' to '" + dataPool.get("ESignState") + "') which requires electronic signature is performed successfully while performing state transition to the state '" + dataPool.get("InitialState") + "'.", driver);

			//Step-5 : Logout from the M-Files Web Access
			//--------------------------------------------
			if (!Utility.logoutFromWebAccess(driver))//Logs out from the M-Files Web Access
				throw new Exception("Error while logging out from M-Files Web Access.");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("AssignedUserName"), dataPool.get("AssignedUserPassword"), testVault);//Login as the Assigned user into the MFWA

			Log.message("5. Logged out and Logged in as the Assigned user : '" + dataPool.get("AssignedUser") + "' to M-Files Web Access.", driver);

			//Step-6 : Login as Assigned user and Navigate to the Assigned to me view and select the newly created assignment object
			//----------------------------------------------------------------------------------------------------------------------
			if (!homePage.taskPanel.clickItem(Caption.Taskpanel.AssignedToMe.Value))
				throw new Exception("Error while navigating to '" + Caption.Taskpanel.AssignedToMe.Value + "' view.");

			if (!homePage.listView.clickItem(objName +" - "+ dataPool.get("AssignmentName")))
				throw new Exception("Seperate assignment(" + objName +" - "+ dataPool.get("AssignmentName") + ") is not created and not exist in the Assigned to me view while performing automatic electronic signature required state transition ('" + dataPool.get("InitialState") + "' to '" + dataPool.get("ESignState") + "').");

			Log.message("6. Navigated to the '" + Caption.Taskpanel.AssignedToMe.Value + "' view and selected the seperate assignment(" + objName +" - "+ dataPool.get("AssignmentName") + ") created while the automatic state transition.", driver);

			//Verification if Seperate assignment assigned to the user who performed the state transition successfully
			//--------------------------------------------------------------------------------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiates the right pane metadata card in the view

			if (metadataCard.getPropertyValue(dataPool.get("AssignmentProperty")).equalsIgnoreCase(dataPool.get("AssignedUser")))
				Log.pass("Test case passed. Approval assignment is created successfully while performing automatic state transition which requires electronic signature in Non-Assignment object.");
			else
				Log.fail("Test case failed. Approval assignment is not created successfully while performing automatic state transition which requires electronic signature in Non-Assignment object.[Additional info.: Property(" + dataPool.get("AssignmentProperty") + ") is not set with the User(" + dataPool.get("AssignedUser") + ") who performed the state transition", driver); 

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try{
				MFilesDialog.closeMFilesDialog(driver);//Closes the M-Files dialog in the driver
			}
			catch(Exception e0) {Log.exception(e0, driver);}	

			Utility.quitDriver(driver);
		}//End finally

	}//End PWorkflow_30990

	/**
	 * PWorkflow_30991 : Verify if assignment class that has any can approve object able to create automatic state change
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"AutomaticStateTransition", "E-Sign"},
			description = "Verify if assignment class that has any can approve object able to create automatic state change")
	public void PWorkflow_30991(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launches login page and logging in

			//Step-1 : Creates the new object with Automatic state transition required property value in the view
			//---------------------------------------------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);//Opens the new Assignment object metadata card via New menu bar

			String objName = Utility.getObjectName(methodName) +"_Assignment";//Frames the object name

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card
			metadataCard.setInfo(dataPool.get("Properties"));//Sets the required property values in the metadata card
			metadataCard.setPropertyValue("Name or title", objName);//Sets the object name in the metadata card
			metadataCard.saveAndClose();//Creates the object

			Log.message("1. Assignment object('" + objName + "') successfully created with the automatic state transition required property(" + dataPool.get("Property") + ") with value(" + dataPool.get("PropertyValue") + ") in the view.", driver);

			//Step-2 : Sets the ESign worklfow in the metadata card
			//------------------------------------------------------------------------------------------------------------
			if (!homePage.listView.clickItem(objName))//Checks if object is exists in the list view
				throw new Exception("Object '" + objName + "' is not selected in the view");

			metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadatacard
			metadataCard.setWorkflow(dataPool.get("Workflow"));//Sets the Worklfow in the right pane metadata card
			metadataCard.setWorkflowState(dataPool.get("WorkflowState"));//Sets the workflow state in the metadatacard
			metadataCard.saveAndClose();//Saves the changes in rightpane metadata card

			Log.message("2. Workflow (" + dataPool.get("Workflow") + ") is set and saved the changes in right pane metadata card for the newly created object.", driver);

			//Step-3: Perform State transition which triggers automatic E-Sgin required state transition for the newly created object
			//-----------------------------------------------------------------------------------------------------------------------
			if (!homePage.listView.clickItem(objName))//Selects the object in the view
				throw new Exception("Object '" + objName + "' is not selected in the view");

			if (!homePage.taskPanel.clickItem(dataPool.get("InitialState")))//Clicks the workflow state in the state transition section in the task panel
				throw new Exception("'" + dataPool.get("InitialState") + "' is not clicked from the task pane for the selected object in the view.");

			if (!MFilesDialog.exists(driver, Caption.MenuItems.Workflow.Value))//Checks if workflow state transition dialog is exist or not
				throw new Exception("State transition workflow dialog is not opened while click on the '" + dataPool.get("InitialState") + "' workflow state from task panel.");

			Log.message("3.1. '" + dataPool.get("InitialState") + "' is clicked from the task pane and workflow dialog is opened for the selected object in the view.", driver);

			MFilesDialog mfDialog = new MFilesDialog(driver, Caption.MenuItems.Workflow.Value);//Instantiates the Workflow M-Files dialog in the view
			mfDialog.clickOkButton();//Clicks OK button in the Workflow dialog for the E-Sign required State transition

			Log.message("3.2. Performed state transition which triggers automatic state transition in the view", driver);

			//Step-4: Check if automatic transition is performed successfully
			//---------------------------------------------------------------
			String result = "";

			metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadata card

			if (!metadataCard.getWorkflowState().equalsIgnoreCase(dataPool.get("AutomaticState")))//Checks if required state is set in the metadata card
				result += "Expected workflow state(" + dataPool.get("AutomaticState") + ") is not set in the metadatacard(Actual : "+ metadataCard.getWorkflowState() + ").";		

			if (!result.equals(""))
				throw new Exception("Automatic state transition ('" + dataPool.get("InitialState") + "' to '" + dataPool.get("AutomaticState") + "') is not performed successfully while performing state transition to the state '" + dataPool.get("InitialState") + "'.[Additional info. : "+ result.trim() +"]");

			Log.message("4. Automatic state transition ('" + dataPool.get("InitialState") + "' to '" + dataPool.get("AutomaticState") + "') is performed successfully while performing state transition to the state '" + dataPool.get("InitialState") + "'.", driver);

			//Step-5 : Logout from the M-Files Web Access
			//--------------------------------------------
			if (!Utility.logoutFromWebAccess(driver))//Logs out from the M-Files Web Access
				throw new Exception("Error while logging out from M-Files Web Access.");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("AssignedUserName"), dataPool.get("AssignedUserPassword"), testVault);//Login as the Assigned user into the MFWA

			Log.message("5. Loogged out and Logged in as the Assigned user : '" + dataPool.get("AssignedUser") + "' into M-Files Web Access.", driver);

			//Step-6 : Login as Assigned user and Navigate to the Assigned to me view and select the newly created assignment object
			//----------------------------------------------------------------------------------------------------------------------
			if (!homePage.taskPanel.clickItem(Caption.Taskpanel.AssignedToMe.Value))
				throw new Exception("Error while navigating to '" + Caption.Taskpanel.AssignedToMe.Value + "' view.");

			if (!homePage.listView.clickItem(objName +" - "+ dataPool.get("AssignmentName")))
				throw new Exception("Seperate assignment(" + objName +" - "+ dataPool.get("AssignmentName") + ") is not created and not exist in the Assigned to me view while performing automatic electronic signature required state transition ('" + dataPool.get("InitialState") + "' to '" + dataPool.get("ESignState") + "').");

			Log.message("6. Navigated to the '" + Caption.Taskpanel.AssignedToMe.Value + "' view and selected the seperate assignment(" + objName +" - "+ dataPool.get("AssignmentName") + ") created while the automatic state transition.", driver);

			//Verification if Seperate assignment assigned to the user who performed the state transition successfully
			//--------------------------------------------------------------------------------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiates the right pane metadata card in the view

			if (metadataCard.getPropertyValue(dataPool.get("AssignmentProperty")).equalsIgnoreCase(dataPool.get("AssignedUser")))
				Log.pass("Test case passed. Approval assignment is created successfully while performing automatic state transition in the assignment which has assignment that has any can approve class.");
			else
				Log.fail("Test case failed. Approval assignment is not created successfully while performing automatic state transition in the assignment which has assignment that has any can approve class.[Additional info.: Property(" + dataPool.get("AssignmentProperty") + ") is not set with the User(" + dataPool.get("AssignedUser") + ") who performed the state transition", driver); 

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try{
				MFilesDialog.closeMFilesDialog(driver);//Closes the M-Files dialog in the driver
			}
			catch(Exception e0) {Log.exception(e0, driver);}	

			Utility.quitDriver(driver);
		}//End finally

	}//End PWorkflow_30991

	/**
	 * PWorkflow_30992 : Verify if assignment class that has anyone can approve object able to assign Esign state change
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"AutomaticStateTransition", "E-Sign"},
			description = "Verify if assignment class that has anyone can approve object able to assign Esign state change")
	public void PWorkflow_30992(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launches login page and logging in

			//Step-1 : Creates the new object with Automatic state transition required property value in the view
			//---------------------------------------------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);//Opens the new Assignment object metadata card via New menu bar

			String objName = Utility.getObjectName(methodName) +"_Assignment";//Frames the object name

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card
			metadataCard.setInfo(dataPool.get("Properties"));//Sets the required property values in the metadata card
			metadataCard.setPropertyValue("Name or title", objName);//Sets the object name in the metadata card
			metadataCard.saveAndClose();//Creates the object

			Log.message("1. Assignment object('" + objName + "') with anyone can approve class successfully created with the automatic state transition required property(" + dataPool.get("Property") + ") with value(" + dataPool.get("PropertyValue") + ") in the view.", driver);

			//Step-2 : Sets the ESign worklfow in the metadata card
			//------------------------------------------------------------------------------------------------------------
			if (!homePage.listView.clickItem(objName))//Checks if object is exists in the list view
				throw new Exception("Object '" + objName + "' is not selected in the view");

			metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadatacard
			metadataCard.setWorkflow(dataPool.get("Workflow"));//Sets the Worklfow in the right pane metadata card
			metadataCard.setWorkflowState(dataPool.get("WorkflowState"));//Sets the workflow state in the metadatacard
			metadataCard.saveAndClose();//Saves the changes in rightpane metadata card

			Log.message("2. Workflow (" + dataPool.get("Workflow") + ") is set and saved the changes in right pane metadata card for the newly created object.", driver);

			//Step-3: Perform State transition which triggers automatic E-Sgin required state transition for the newly created object
			//-----------------------------------------------------------------------------------------------------------------------
			if (!homePage.listView.clickItem(objName))//Selects the object in the view
				throw new Exception("Object '" + objName + "' is not selected in the view");

			if (!homePage.taskPanel.clickItem(dataPool.get("InitialState")))//Clicks the workflow state in the state transition section in the task panel
				throw new Exception("'" + dataPool.get("InitialState") + "' is not clicked from the task pane for the selected object in the view.");

			if (!MFilesDialog.exists(driver, Caption.MenuItems.Workflow.Value))//Checks if workflow state transition dialog is exist or not
				throw new Exception("State transition workflow dialog is not opened while click on the '" + dataPool.get("InitialState") + "' workflow state from task panel.");

			Log.message("3.1. State transition workflow dialog is not opened while click on the '" + dataPool.get("InitialState") + "' workflow state from task panel.", driver);

			MFilesDialog mfDialog = new MFilesDialog(driver, Caption.MenuItems.Workflow.Value);//Instantiates the Workflow M-Files dialog in the view
			mfDialog.clickOkButton();//Clicks OK button in the Workflow dialog for the E-Sign required State transition

			if (!MFilesDialog.isESignDialogExist(driver))//Waits and Checks if Electronic Signature dialog is exists or not
				throw new Exception("Automatic electronic signature required state transition ('" + dataPool.get("InitialState") + "' to '" + dataPool.get("ESignState") + "') is not triggered while performing state transition to the state '" + dataPool.get("InitialState") + "'.");

			Log.message("3.2. Electronic signature dialog is opened for triggered automatic state transition ('" + dataPool.get("InitialState") + "' to '" + dataPool.get("ESignState") + "') while performing state transition to the state '" + dataPool.get("InitialState") + "'.", driver);

			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog
			mfDialog.eSign(userName, password, "", "");//E-Signs the state transition

			Log.message("3.3. Performed E-Sign operation for the newly created object in the view", driver);

			//Step-4: Check if object is E-Signed successfully in the view
			//------------------------------------------------------------
			String result = "";

			metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadata card

			if (!metadataCard.getWorkflowState().equalsIgnoreCase(dataPool.get("ESignState")))//Checks if required state is set in the metadata card
				result += "Object(" + objName + ") is not E-Signed successfully. Expected workflow state(" + dataPool.get("ESignState") + ") is not set in the metadatacard(Actual : "+ metadataCard.getWorkflowState() + ").";		

			if (!metadataCard.propertyExists(dataPool.get("SignatureProperty")))//Checks if signature property is added into the metadata card
				result += "Object(" + objName + ") is not E-Signed successfully. "+ dataPool.get("SignatureProperty") +" is not exist in the metadata card.";

			if (!result.equals(""))
				throw new Exception("Automatic state transition which requires electronic signature('" + dataPool.get("InitialState") + "' to '" + dataPool.get("ESignState") + "') is not performed successfully while performing state transition to the state '" + dataPool.get("InitialState") + "'.[Additional info. : "+ result.trim() +"]");

			Log.message("4. Automatic state transition which requires electronic signature('" + dataPool.get("InitialState") + "' to '" + dataPool.get("ESignState") + "') is performed successfully while performing state transition to the state '" + dataPool.get("InitialState") + "'.", driver);

			//Step-5 : Logout from the M-Files Web Access
			//--------------------------------------------
			if (!Utility.logoutFromWebAccess(driver))//Logs out from the M-Files Web Access
				throw new Exception("Error while logging out from M-Files Web Access.");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("AssignedUserName"), dataPool.get("AssignedUserPassword"), testVault);//Login as the Assigned user into the MFWA

			Log.message("5. Logged out and Logged in as the Assigned user : '" + dataPool.get("AssignedUser") + "' into M-Files Web Access.", driver);

			//Step-6 : Navigate to the Assigned to me view and select the newly created assignment object
			//-------------------------------------------------------------------------------------------
			if (!homePage.taskPanel.clickItem(Caption.Taskpanel.AssignedToMe.Value))
				throw new Exception("Error while navigating to '" + Caption.Taskpanel.AssignedToMe.Value + "' view.");

			if (!homePage.listView.clickItem(objName +" - "+ dataPool.get("AssignmentName")))
				throw new Exception("Seperate assignment(" + objName +" - "+ dataPool.get("AssignmentName") + ") is not created and not exist in the Assigned to me view while performing automatic electronic signature required state transition ('" + dataPool.get("InitialState") + "' to '" + dataPool.get("ESignState") + "').");

			Log.message("6. Navigated to the '" + Caption.Taskpanel.AssignedToMe.Value + "' view and selected the seperate assignment(" + objName +" - "+ dataPool.get("AssignmentName") + ") created while the automatic state transition.", driver);

			//Verification if Seperate assignment assigned to the user who performed the state transition successfully
			//--------------------------------------------------------------------------------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiates the right pane metadata card in the view

			if (metadataCard.getPropertyValue(dataPool.get("AssignmentProperty")).equalsIgnoreCase(dataPool.get("AssignedUser")))
				Log.pass("Test case passed. Approval assignment is created successfully while performing automatic state transition in the assignment which has assignment that has anyone can approve class.");
			else
				Log.fail("Test case failed. Approval assignment is not created successfully while performing automatic state transition in the assignment which has assignment that has anyone can approve class.[Additional info.: Property(" + dataPool.get("AssignmentProperty") + ") is not set with the User(" + dataPool.get("AssignedUser") + ") who performed the state transition", driver); 

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try{
				MFilesDialog.closeMFilesDialog(driver);//Closes the M-Files dialog in the driver
			}
			catch(Exception e0) {Log.exception(e0, driver);}	

			Utility.quitDriver(driver);
		}//End finally

	}//End PWorkflow_30992

	/**
	 * PWorkflow_30994 : Verify if Esign Approval assignment is assigned to pseudo user when assignment has All must approve class
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"AutomaticStateTransition", "E-Sign"},
			description = "Verify if Esign Approval assignment is assigned to pseudo user when assignment has All must approve class")
	public void PWorkflow_30994(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launches login page and logging in

			//Step-1 : Creates the new object with Automatic state transition required property value in the view
			//---------------------------------------------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);//Opens the new Assignment object metadata card via New menu bar

			String objName = Utility.getObjectName(methodName) +"_Assignment";//Frames the object name

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card
			metadataCard.setInfo(dataPool.get("Properties"));//Sets the required property values in the metadata card
			metadataCard.setPropertyValue("Name or title", objName);//Sets the object name in the metadata card
			metadataCard.saveAndClose();//Creates the object

			Log.message("1. Assignment object('" + objName + "') with All must approve class successfully created with the automatic state transition required property(" + dataPool.get("Property") + ") with value(" + dataPool.get("PropertyValue") + ") in the view.", driver);

			//Step-2 : Sets the ESign worklfow in the metadata card
			//------------------------------------------------------------------------------------------------------------
			if (!homePage.listView.clickItem(objName))//Checks if object is exists in the list view
				throw new Exception("Object '" + objName + "' is not selected in the view");

			metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadatacard
			metadataCard.setWorkflow(dataPool.get("Workflow"));//Sets the Worklfow in the right pane metadata card
			metadataCard.saveAndClose();//Saves the changes in rightpane metadata card

			Log.message("2. Workflow(" + dataPool.get("Workflow") + ") with state(" + dataPool.get("InitialState") + ") is set and saved the changes in right pane metadata card for the newly created object.", driver);

			//Step-3: Perform State transition which triggers E-Sgin required state transition for the newly created object
			//-------------------------------------------------------------------------------------------------------------
			if (!homePage.listView.clickItem(objName))//Selects the object in the view
				throw new Exception("Object '" + objName + "' is not selected in the view");

			if (!homePage.taskPanel.clickItem(dataPool.get("ESignState")))//Clicks the workflow state in the state transition section in the task panel
				throw new Exception("'" + dataPool.get("ESignState") + "' is not clicked from the task pane for the selected object in the view.");

			if (!MFilesDialog.exists(driver, Caption.MenuItems.Workflow.Value))//Checks if workflow state transition dialog is exist or not
				throw new Exception("State transition workflow dialog is not opened while click on the '" + dataPool.get("InitialState") + "' workflow state from task panel.");

			Log.message("3.1. State transition workflow dialog is opened while click on the '" + dataPool.get("InitialState") + "' workflow state from task panel.", driver);

			MFilesDialog mfDialog = new MFilesDialog(driver, Caption.MenuItems.Workflow.Value);//Instantiates the Workflow M-Files dialog in the view
			mfDialog.clickOkButton();//Clicks OK button in the Workflow dialog for the E-Sign required State transition

			Log.message("3.2. OK button is clicked in the State transition dialog for the E-Sign required state trainsition.", driver);

			//Step-4: Perform E-Sign and Check if object is E-Signed successfully in the view
			//-------------------------------------------------------------------------------
			String result = "";

			if (!MFilesDialog.isESignDialogExist(driver))//Waits and Checks if Electronic Signature dialog is exists or not
				throw new Exception("Electronic signature dialog is not opened while performing state transition ('" + dataPool.get("InitialState") + "' to '" + dataPool.get("ESignState") + "') which requires E-Sign.");

			Log.message("4.1. Electronic signature dialog is opened while performing state transition ('" + dataPool.get("InitialState") + "' to '" + dataPool.get("ESignState") + "') which requires E-Sign.", driver);

			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog
			mfDialog.eSign(userName, password, "", "");//E-Signs the state transition

			Log.message("4.2. Valid credentials entered and clicked the sign button.", driver);

			metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadata card

			if (!metadataCard.getWorkflowState().equalsIgnoreCase(dataPool.get("ESignState")))//Checks if required state is set in the metadata card
				result += "Object(" + objName + ") is not E-Signed successfully. Expected workflow state(" + dataPool.get("ESignState") + ") is not set in the metadatacard(Actual : "+ metadataCard.getWorkflowState() + ").";		

			if (!metadataCard.propertyExists(dataPool.get("SignatureProperty")))//Checks if signature property is added into the metadata card
				result += "Object(" + objName + ") is not E-Signed successfully. "+ dataPool.get("SignatureProperty") +" is not exist in the metadata card.";

			if (!result.equals(""))
				throw new Exception("State transition ('" + dataPool.get("InitialState") + "' to '" + dataPool.get("ESignState") + "') which requires E-Sign is not performed successfully. Additional info. : "+ result);

			Log.message("4.3. State transition ('" + dataPool.get("InitialState") + "' to '" + dataPool.get("ESignState") + "') is performed successfully.", driver);

			//Step-5 : Logout from the M-Files Web Access and Login as Assigned user
			//-----------------------------------------------------------------------
			if (!Utility.logoutFromWebAccess(driver))//Logs out from the M-Files Web Access
				throw new Exception("Error while logging out from M-Files Web Access.");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("AssignedUserName"), dataPool.get("AssignedUserPassword"), testVault);//Login as the Assigned user into the MFWA

			Log.message("5. Logged out from the MFiles Web Access and Logged in as the Assigned user : '" + dataPool.get("AssignedUser") + "' into M-Files Web Access.", driver);

			//Step-6 : Navigate to the Assigned to me view and check the approval assignments
			//--------------------------------------------------------------------------------
			result = "";

			if (!homePage.taskPanel.clickItem(Caption.Taskpanel.AssignedToMe.Value))//Clicks the Assigned to me view in the task pane
				throw new Exception("Error while navigating to ' " + Caption.Taskpanel.AssignedToMe.Value + " ' view...");

			if (!homePage.listView.isItemExists(objName + " - " + dataPool.get("AssignmentName1")))
				result += "Assignemnt(" +  objName + " - " + dataPool.get("AssignmentName1")  + ") is not exist in the Assigned to me view for the user : '" + dataPool.get("AssignedUser") + "'. ";

			if (!homePage.listView.isItemExists(objName + " - " + dataPool.get("AssignmentName2")))
				result += "Assignemnt(" +  objName + " - " + dataPool.get("AssignmentName2")  + ") is not exist in the Assigned to me view for the user : '" + dataPool.get("AssignedUser") + "'. ";

			if (!result.equals(""))
				throw new Exception("Approval assignments are not created and not exist in the Assigned to me for the user : '" +  dataPool.get("AssignedUser")  + "'. Additional info. : " + result + ".");

			Log.message("6. Approval assignments created successfully and Assigned to the user : "+ dataPool.get("AssignedUser") +" successfully.", driver);

			//Step-7:  Perform Mark Approve the approval assignments:
			//--------------------------------------------------------
			if (!homePage.listView.clickItem(objName + " - " + dataPool.get("AssignmentName1")))
				throw new Exception("Object(" + objName + " - " + dataPool.get("AssignmentName1") + ") is not selected in the view.");

			if (!homePage.taskPanel.clickItem(Caption.MenuItems.MarkApproved.Value))//Clicks the MarkApproved in the task pane
				throw new Exception("' " + Caption.MenuItems.MarkApproved.Value + " ' not clicked from the task pane for the selected object(" + objName + " - " + dataPool.get("AssignmentName1") + ").");

			mfDialog = new MFilesDialog(driver, Caption.MenuItems.MarkApproved.Value);//Instantiates the MarkApproved dialog
			mfDialog.clickOkButton();//Clicks the OK button in the MarkApproved dialog

			Log.message("7.1.1. OK button is clicked in the MarkApproved dialog for the E-Sign required action.", driver);

			if (!MFilesDialog.isESignDialogExist(driver))//Waits and Checks if Electronic Signature dialog is exists or not
				throw new Exception("Electronic signature dialog is not opened while mark aprrove the assignment(" + objName + " - " + dataPool.get("AssignmentName1") + ") which requires E-Sign.");

			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog
			mfDialog.eSign(dataPool.get("AssignedUserName"), dataPool.get("AssignedUserPassword"), "", "");//E-Signs the state transition

			if (homePage.listView.isItemExists(objName + " - " + dataPool.get("AssignmentName1")))
				throw new Exception("Approval assignment(" + objName + " - " + dataPool.get("AssignmentName1") + ") is not mark approved by the user : '" + dataPool.get("AssignedUser") + "'");

			Log.message("7.1.2. Assignment(" + objName + " - " + dataPool.get("AssignmentName1") + ") is Mark approved by the user : '" + dataPool.get("AssignedUser") + "'", driver);

			if (!homePage.listView.clickItem(objName + " - " + dataPool.get("AssignmentName2")))
				throw new Exception("Object(" + objName + " - " + dataPool.get("AssignmentName2") + ") is not selected in the view.");

			if (!homePage.taskPanel.clickItem(Caption.MenuItems.MarkApproved.Value))//Clicks the MarkApproved in the task pane
				throw new Exception("' " + Caption.MenuItems.MarkApproved.Value + " ' not clicked from the task pane for the selected object(" + objName + " - " + dataPool.get("AssignmentName2") + ").");

			mfDialog = new MFilesDialog(driver, Caption.MenuItems.MarkApproved.Value);//Instantiates the MarkApproved dialog
			mfDialog.clickOkButton();//Clicks the OK button in the MarkApproved dialog

			Log.message("7.2.1. OK button is clicked in the MarkApproved dialog for the E-Sign required action.", driver);

			if (!MFilesDialog.isESignDialogExist(driver))//Waits and Checks if Electronic Signature dialog is exists or not
				throw new Exception("Electronic signature dialog is not opened while mark aprrove the assignment(" + objName + " - " + dataPool.get("AssignmentName2") + ") which requires E-Sign.");

			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog
			mfDialog.eSign(dataPool.get("AssignedUserName"), dataPool.get("AssignedUserPassword"), "", "");//E-Signs the state transition

			if (homePage.listView.isItemExists(objName + " - " + dataPool.get("AssignmentName2")))
				throw new Exception("Approval assignment(" + objName + " - " + dataPool.get("AssignmentName2") + ") is not mark approved by the user : '" + dataPool.get("AssignedUser") + "'");

			Log.message("7.2.2. Assignment(" + objName + " - " + dataPool.get("AssignmentName2") + ") is Mark approved by the user : '" + dataPool.get("AssignedUser") + "'", driver);

			//Step-8 : Logout from the M-Files Web Access and Login as Admin user
			//-----------------------------------------------------------------------
			if (!Utility.logoutFromWebAccess(driver))//Logs out from the M-Files Web Access
				throw new Exception("Error while logging out from M-Files Web Access.");

			homePage = LoginPage.launchDriverAndLogin(driver, false);//Login as the Admin user into the MFWA

			Log.message("8. Logged out from the MFiles Web Access and Logged in as the Admin user : '" + userFullName + "' into M-Files Web Access.", driver);

			//Step-9 : Navigate to the search view
			//------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), objName);//Navigate to the specific view

			if (!homePage.listView.clickItem(objName))
				throw new Exception(objName  +" is not selected in the view.");

			Log.message("9. Navigated to " + viewtonavigate + " and selected the object \"" + objName +  "\" in the view.", driver);

			//Verification: Check if Automatic state transition is performed while approving/rejecting the seperate assignments
			//-----------------------------------------------------------------------------------------------------------------
			result = "";

			metadataCard = new MetadataCard(driver, true);//Instantiates the right pane metadatacard

			if (metadataCard.getWorkflowState().equalsIgnoreCase(dataPool.get("AutomaticState")))
				Log.pass("Test case passed. Automatic state transition ('" + dataPool.get("ESignState") + "' to '" + dataPool.get("AutomaticState") + "') is performed successfully while approving/rejecting the seperate assignments of thhe object.");
			else
				Log.fail("Test case failed. Automatic state transition is not performed while approving/rejecting the seperate assignments of thhe object.");

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try{
				MFilesDialog.closeMFilesDialog(driver);//Closes the M-Files dialog in the driver
			}
			catch(Exception e0) {Log.exception(e0, driver);}	

			Utility.quitDriver(driver);
		}//End finally

	}//End PWorkflow_30994

	/**
	 * PWorkflow_31003 : Verify if user able to view Esign dialog when creating an approval assignment 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"AssignmentApproval", "E-Sign"},
			description = "Verify if user able to view Esign dialog when creating an approval assignment")
	public void PWorkflow_31003(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launches login page and logging in

			//Step-1 : Open the new object metadata card
			//------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);//Opens the new Assignment object metadata card via New menu bar

			Log.message("1. New Assignment object link clicked from new menu bar.", driver);

			//Step-2 : Sets the required values in the metadata card
			//------------------------------------------------------
			String objName = Utility.getObjectName(methodName) +"_Assignment";//Frames the object name

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card
			metadataCard.setInfo(dataPool.get("Properties"));//Sets the required property values in the metadata card
			metadataCard.setPropertyValue("Name or title", objName);//Sets the object name in the metadata card

			Log.message("2. Required values set in the metadata card.", driver);

			//Step-3 : Clicks the Approve icon in the metadata card
			//------------------------------------------------------
			metadataCard.clickApproveIcon(true);//Clicks the approve icon in the metadata card

			Log.message("3. Approve icon is clicked in the metadata card for the assigned to user.", driver);

			//Verification: Verify if user able to create approval assignment
			//----------------------------------------------------------------
			metadataCard.saveAndClose();//Creates the object

			if (!MFilesDialog.exists(driver))
				throw new Exception("Warning dialog not displayed while creating the approval assignment which requires Electronic Signature.");

			MFilesDialog mfDialog = new MFilesDialog(driver, "M-Files Web");

			if (mfDialog.getMessage().equalsIgnoreCase(dataPool.get("ExpectedWarningMessage")))
				Log.pass("Test case passed. Expected warning dialog displayed while creating approval assignment which requires electroinc signature.", driver);
			else
				Log.fail("Test case failed. Expected warning dialog with message(" + dataPool.get("ExpectedWarningMessage") + ") is not displayed while creating approval assignment which requires electroinc signature. Actual warning message: '" + mfDialog.getMessage() + "'", driver);

			mfDialog.close();//Close the warning dialog
			metadataCard.cancelAndConfirm();//Closes the metadatacard

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try{
				MFilesDialog.closeMFilesDialog(driver);//Closes the M-Files dialog in the driver
			}
			catch(Exception e0) {Log.exception(e0, driver);}	

			Utility.quitDriver(driver);
		}//End finally

	}//End PWorkflow_31003

	/**
	 * PWorkflow_31004 : Verify if user able to view Esign dialog when creating an reject assignment 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"AssignmentReject", "E-Sign"},
			description = "Verify if user able to view Esign dialog when creating an reject assignment")
	public void PWorkflow_31004(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launches login page and logging in

			//Step-1 : Open the new object metadata card
			//------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);//Opens the new Assignment object metadata card via New menu bar

			Log.message("1. New Assignment object link clicked from new menu bar.", driver);

			//Step-2 : Sets the required values in the metadata card
			//------------------------------------------------------
			String objName = Utility.getObjectName(methodName) +"_Assignment";//Frames the object name

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card
			metadataCard.setInfo(dataPool.get("Properties"));//Sets the required property values in the metadata card
			metadataCard.setPropertyValue("Name or title", objName);//Sets the object name in the metadata card

			Log.message("2. Required values set in the metadata card.", driver);

			//Step-3 : Clicks the Reject icon in the metadata card
			//------------------------------------------------------
			metadataCard.clickRejectIcon(true);//Clicks the Reject icon in the metadata card

			Log.message("3. Reject icon is clicked in the metadata card for the assigned to user.", driver);

			//Verification: Verify if user able to create reject assignment
			//----------------------------------------------------------------
			metadataCard.saveAndClose();//Creates the object

			if (!MFilesDialog.exists(driver))
				throw new Exception("Warning dialog not displayed while creating the reject assignment which requires Electronic Signature.");

			MFilesDialog mfDialog = new MFilesDialog(driver, "M-Files Web");

			if (mfDialog.getMessage().equalsIgnoreCase(dataPool.get("ExpectedWarningMessage")))
				Log.pass("Test case passed. Expected warning dialog displayed while creating Reject assignment which requires electroinc signature.", driver);
			else
				Log.fail("Test case failed. Expected warning dialog with message(" + dataPool.get("ExpectedWarningMessage") + ") is not displayed while creating Reject assignment which requires electroinc signature. Actual warning message: '" + mfDialog.getMessage() + "'", driver);

			mfDialog.close();//Close the warning dialog
			metadataCard.cancelAndConfirm();//Closes the metadatacard

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try{
				MFilesDialog.closeMFilesDialog(driver);//Closes the M-Files dialog in the driver
			}
			catch(Exception e0) {Log.exception(e0, driver);}	

			Utility.quitDriver(driver);
		}//End finally

	}//End PWorkflow_31004

	/**
	 * PWorkflow_31006 : Verify if user able to view Esign dialog when approving existing assignment via metadata card
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"AssignmentApproval", "E-Sign"},
			description = "Verify if user able to view Esign dialog when approving existing assignment")
	public void PWorkflow_31006(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launches login page and logging in

			//Step-1 : Create the new Assignment object
			//-----------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);//Opens the new Assignment object metadata card via New menu bar
			String objName = Utility.getObjectName(methodName) +"_Assignment";//Frames the object name

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card
			metadataCard.setInfo(dataPool.get("Properties"));//Sets the required property values in the metadata card
			metadataCard.setPropertyValue("Name or title", objName);//Sets the object name in the metadata card
			metadataCard.saveAndClose();//Creates the object

			Log.message("1. Object(" + objName + ") is created.", driver);

			//Step-2: Perform the approve operation in right pane metadatacard
			//----------------------------------------------------------------
			if (!homePage.listView.clickItem(objName))
				throw new Exception("Object(" + objName + ") is not selected in the view.");

			metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadata card
			metadataCard.clickApproveIcon(true);//Clicks the apporve icon in the metadatacard
			metadataCard.saveAndClose();//clicks the save button in the metadata card

			Log.message("2. Approve operation performed via metadata card for the object(" + objName + ") in the view.", driver);

			//Verification: Verify if warning dialog displayed while approving the assignment via metadata card
			//------------------------------------------------------------------------------------------------
			if (!MFilesDialog.exists(driver))
				throw new Exception("Warning dialog not displayed while creating the approval assignment which requires Electronic Signature.");

			MFilesDialog mfDialog = new MFilesDialog(driver, "M-Files Web");

			if (mfDialog.getMessage().equalsIgnoreCase(dataPool.get("ExpectedWarningMessage")))
				Log.pass("Test case passed. Expected warning dialog displayed while approving the exisiting assignment via metadata card which requires electroinc signature.", driver);
			else
				Log.fail("Test case failed. Expected warning dialog with message(" + dataPool.get("ExpectedWarningMessage") + ") is not displayed while approving the existing assignment via metadata card which requires electroinc signature. Actual warning message: '" + mfDialog.getMessage() + "'", driver); 

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try{
				MFilesDialog.closeMFilesDialog(driver);//Closes the M-Files dialog in the driver
			}
			catch(Exception e0) {Log.exception(e0, driver);}	

			Utility.quitDriver(driver);
		}//End finally

	}//End PWorkflow_31006

	/**
	 * PWorkflow_44132 : Verify if user able to view Esign dialog when approving existing assignment via Task pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"BasicESign"},
			description = "Verify if user able to view Esign dialog when approving existing assignment via Task pane")
	public void PWorkflow_44132(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launches driver and logging in

			//Step-1 : Create the new Assignment object
			//-----------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);//Opens the new Assignment object metadata card via New menu bar
			String objName = Utility.getObjectName(methodName) +"_Assignment";//Frames the object name

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card
			metadataCard.setInfo(dataPool.get("Properties"));//Sets the required property values in the metadata card
			metadataCard.setPropertyValue("Name or title", objName);//Sets the object name in the metadata card
			metadataCard.saveAndClose();//Creates the object

			Log.message("1. Object(" + objName + ") is created.", driver);

			//Step-2: Click Mark Approved from the task pane for the selected object
			//----------------------------------------------------------------------
			if (!homePage.listView.clickItem(objName))//Clicks the object in the view
				throw new Exception("Object(" + objName + ") not selected in the view.");

			if (!homePage.taskPanel.clickItem(Caption.MenuItems.MarkApproved.Value))//Clicks the Mark approve option in the task pane
				throw new Exception("'" + Caption.MenuItems.MarkApproved.Value + "' is not clicked from the task pane for the selected object.");

			if (!MFilesDialog.exists(driver, Caption.MenuItems.MarkApproved.Value))//Checks if Comment dialog exist in the  view
				throw new Exception("Comment dialog is not opened while click on the '" + Caption.MenuItems.MarkApproved.Value + "' from the task pane.");

			Log.message("2. Clicked '" + Caption.MenuItems.MarkApproved.Value + "' from the task pane for the selected object(" + objName + ").", driver);

			//Step-3: Click OK in the comment dialog and then check if E-Sign dialog is displayed for the selected object
			//-----------------------------------------------------------------------------------------------------------
			MFilesDialog mfDialog = new MFilesDialog(driver, Caption.MenuItems.MarkApproved.Value);//Instantiates the M-Files dialog in the view
			mfDialog.clickOkButton();//Clicks OK button in the Comment dialog for the E-Sign required approval

			Log.message("3.1. OK button is clicked in the Mark approve comment dialog.", driver);

			if (!MFilesDialog.isESignDialogExist(driver))//Checks if Electronic Signature dialog is exists or not
				throw new Exception("Electronic signature dialog is not opened while performing mark approve via task pane.");

			Log.message("3.2. E-Sign dialog is displayed after click OK button in the Mark approve dialog for the E-Sign required action.", driver);

			//Step-4: E-Sign the object in M-Files Dialog
			//--------------------------------------------------
			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog
			mfDialog.eSign(userName, password, "", "");//E-Signs the state transition

			Log.message("4. Assignment is Mark approved with Electronic Signature by the User: '" + userName + "'.", driver);

			//Verification: If Assignment is approved successfully  with E-Sign
			//-----------------------------------------------------------------
			if(!homePage.taskPanel.clickItem(Caption.MenuItems.RecentlyAccessedByMe.Value))
				throw new Exception("Error while navigating to Recently Accessed By Me view");

			if (!homePage.listView.clickItem(objName))//Clicks the object in the view
				throw new Exception("Object(" + objName + ") not selected in the view.");

			metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadata card

			if (metadataCard.isApprovedSelected(0))
				Log.pass("Test case passed. Assignment is approved successfully with E-Sign.", driver);
			else
				Log.fail("Test case failed. Assignment is not approved successfully while approving from task pane which requires electronic signature.", driver);


		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try{
				MFilesDialog.closeMFilesDialog(driver);//Closes the M-Files dialog in the driver
			}
			catch(Exception e0) {Log.exception(e0, driver);}	

			Utility.quitDriver(driver);
		}//End finally

	}//End PWorkflow_44132

	/**
	 * PWorkflow_31078 : Verify if external user able to perform mark complete operation for the selected assignment object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			description = "Verify if external user able to perform mark complete operation for the selected assignment object")
	public void PWorkflow_31078(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//-----------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), testVault);//Logging into the vault

			Log.message("Pre-requsite : Browser is opened and logged into MFWA as External User. ( User Name : " + dataPool.get("UserName") + "; Vault : " + testVault + ")", driver);

			//Step-1 : Navigate to Assigned to me view and selects the assignment object
			//-----------------------------------------------------------------------------
			if (!homePage.taskPanel.clickItem(Caption.Taskpanel.AssignedToMe.Value))//Clicks the option from the task panel
				throw new Exception("Error while navigating to '" + Caption.Taskpanel.AssignedToMe.Value + "' view.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))//Selects the object in the view
				throw new Exception("Assignment(" + dataPool.get("ObjectName") + ") is not  available for the External user in the Assigned to me view.");

			Log.message("1. Navigated to '" + Caption.Taskpanel.AssignedToMe.Value + "' and selected the object \"" + dataPool.get("ObjectName") +  "\" in the view.", driver);

			//Step-2: Click Mark Complete option from task pane
			//-------------------------------------------------
			if (!homePage.taskPanel.clickItem(Caption.MenuItems.MarkComplete.Value))//Clicks the option from the task pane
				throw new Exception("'" + Caption.MenuItems.MarkComplete.Value + "' option is not clicked from task pane for the selected object in the view.");

			Log.message("2. '" + Caption.MenuItems.MarkComplete.Value + "' option is clicked from task pane for the selected object in the view.", driver);

			//Step-3: Click ok button in the Mark complete dialog
			//---------------------------------------------------
			if (!MFilesDialog.exists(driver, Caption.MenuItems.MarkComplete.Value))
				throw new Exception("'" + Caption.MenuItems.MarkComplete.Value + "' dialog is not opened after click the Mark complete option from task pane for the selected object in the view.");

			MFilesDialog mfDialog = new MFilesDialog(driver, Caption.MenuItems.MarkComplete.Value);//Instantiates the Mark complete dialog
			mfDialog.clickOkButton();//Clicks ok button in the MFiles dialog
			Utils.fluentWait(driver);

			Log.message("3. OK button is clicked in the Mark Complete dialog.", driver);

			//Verification: Check if assignment is approved as external user
			//--------------------------------------------------------------
			if (!homePage.taskPanel.clickItem(Caption.MenuItems.RecentlyAccessedByMe.Value))
				throw new Exception("Error while navigating to '" + Caption.MenuItems.RecentlyAccessedByMe.Value + "' view.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new Exception("Assignment(" + dataPool.get("ObjectName") + ") is not  available in the Recently accessed by me view.");

			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadata card

			if (metadataCard.isCompleteSelected(2))
				Log.pass("Test case passed. Assignment which has read only access to the External user is Mark completed successfully by the External user.", driver);
			else
				Log.fail("Test case failed. Assignment which has read only access to the External user is not Mark completed by the External user.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try{
				MFilesDialog.closeMFilesDialog(driver);//Closes the M-Files dialog in the driver
			}
			catch(Exception e0) {Log.exception(e0, driver);}	

			Utility.quitDriver(driver);
		}//End finally

	}//End PWorkflow_31078

	/**
	 * PWorkflow_31079 : Verify if Read access user able to perform mark complete operation for selected assignment object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			description = "Verify if Read access user able to perform mark complete operation for selected assignment object")
	public void PWorkflow_31079(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//-----------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), testVault);//Logging into the vault

			Log.message("Pre-requsite : Browser is opened and logged into MFWA as Read only User. ( User Name : " + dataPool.get("UserName") + "; Vault : " + testVault + ")", driver);

			//Step-1 : Navigate to Assigned to me view and selects the assignment object
			//-----------------------------------------------------------------------------
			if (!homePage.taskPanel.clickItem(Caption.Taskpanel.AssignedToMe.Value))//Clicks the option from the task panel
				throw new Exception("Error while navigating to '" + Caption.Taskpanel.AssignedToMe.Value + "' view.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))//Selects the object in the view
				throw new Exception("Assignment(" + dataPool.get("ObjectName") + ") is not  available for the Read only user in the Assigned to me view.");

			Log.message("1. Navigated to '" + Caption.Taskpanel.AssignedToMe.Value + "' and selected the object \"" + dataPool.get("ObjectName") +  "\" in the view.", driver);

			//Step-2: Click Mark Complete option from task pane
			//-------------------------------------------------
			if (!homePage.taskPanel.clickItem(Caption.MenuItems.MarkComplete.Value))//Clicks the option from the task pane
				throw new Exception("'" + Caption.MenuItems.MarkComplete.Value + "' option is not clicked from task pane for the selected object in the view.");

			Log.message("2. '" + Caption.MenuItems.MarkComplete.Value + "' option is clicked from task pane for the selected object in the view.", driver);

			//Step-3: Click ok button in the Mark complete dialog
			//---------------------------------------------------
			if (!MFilesDialog.exists(driver, Caption.MenuItems.MarkComplete.Value))
				throw new Exception("'" + Caption.MenuItems.MarkComplete.Value + "' dialog is not opened after click the Mark complete option from task pane for the selected object in the view.");

			MFilesDialog mfDialog = new MFilesDialog(driver, Caption.MenuItems.MarkComplete.Value);//Instantiates the Mark complete dialog
			mfDialog.clickOkButton();//Clicks ok button in the MFiles dialog
			Utils.fluentWait(driver);

			Log.message("3. OK button is clicked in the Mark Complete dialog.", driver);

			//Verification: Check if assignment is approved as Read only user
			//---------------------------------------------------------------
			if (!homePage.taskPanel.clickItem(Caption.MenuItems.RecentlyAccessedByMe.Value))
				throw new Exception("Error while navigating to '" + Caption.MenuItems.RecentlyAccessedByMe.Value + "' view.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new Exception("Assignment(" + dataPool.get("ObjectName") + ") is not  available in the Recently accessed by me view.");

			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the right pane metadatacard

			if (metadataCard.isCompleteSelected(1))
				Log.pass("Test case passed. Assignment which has read only access to the Read only user is Mark completed successfully by the Read only user.", driver);
			else
				Log.fail("Test case failed. Assignment which has read only access to the Read only user is not Mark completed by the Read only user.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try{
				MFilesDialog.closeMFilesDialog(driver);//Closes the M-Files dialog in the driver
			}
			catch(Exception e0) {Log.exception(e0, driver);}	

			Utility.quitDriver(driver);
		}//End finally

	}//End PWorkflow_31079

	/**
	 * PWorkflow_31080 : Verify if all rights user able to perform mark complete operation for the selected assignment object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			description = "Verify if all rights user able to perform mark complete operation for the selected assignment object")
	public void PWorkflow_31080(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//-----------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);//Launch login page and login

			//Step-1 : Navigate to Assigned to me view and selects the assignment object
			//-----------------------------------------------------------------------------
			if (!homePage.taskPanel.clickItem(Caption.Taskpanel.AssignedToMe.Value))//Clicks the option from the task panel
				throw new Exception("Error while navigating to '" + Caption.Taskpanel.AssignedToMe.Value + "' view.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))//Selects the object in the view
				throw new Exception("Assignment(" + dataPool.get("ObjectName") + ") is not available for the Admin user in the Assigned to me view.");

			Log.message("1. Navigated to '" + Caption.Taskpanel.AssignedToMe.Value + "' and selected the object \"" + dataPool.get("ObjectName") +  "\" in the view.", driver);

			//Step-2: Click Mark Complete option from task pane
			//-------------------------------------------------
			if (!homePage.taskPanel.clickItem(Caption.MenuItems.MarkComplete.Value))//Clicks the option from the task pane
				throw new Exception("'" + Caption.MenuItems.MarkComplete.Value + "' option is not clicked from task pane for the selected object in the view.");

			Log.message("2. '" + Caption.MenuItems.MarkComplete.Value + "' option is clicked from task pane for the selected object in the view.", driver);

			//Step-3: Click ok button in the Mark complete dialog
			//---------------------------------------------------
			if (!MFilesDialog.exists(driver, Caption.MenuItems.MarkComplete.Value))
				throw new Exception("'" + Caption.MenuItems.MarkComplete.Value + "' dialog is not opened after click the Mark complete option from task pane for the selected object in the view.");

			MFilesDialog mfDialog = new MFilesDialog(driver, Caption.MenuItems.MarkComplete.Value);//Instantiates the Mark complete dialog
			mfDialog.clickOkButton();//Clicks ok button in the MFiles dialog
			Utils.fluentWait(driver);

			Log.message("3. OK button is clicked in the Mark Complete dialog.", driver);

			//Verification: Check if assignment is approved as Read only user
			//---------------------------------------------------------------
			if (!homePage.taskPanel.clickItem(Caption.MenuItems.RecentlyAccessedByMe.Value))
				throw new Exception("Error while navigating to '" + Caption.MenuItems.RecentlyAccessedByMe.Value + "' view.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new Exception("Assignment(" + dataPool.get("ObjectName") + ") is not  available in the Recently accessed by me view.");

			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the right pane metadatacard

			if (metadataCard.isCompleteSelected(0))
				Log.pass("Test case passed. Assignment which has read only access to the Read only user is Mark completed successfully by the Admin user.", driver);
			else
				Log.fail("Test case failed. Assignment which has read only access to the Read only user is not Mark completed by the Admin user.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try{
				MFilesDialog.closeMFilesDialog(driver);//Closes the M-Files dialog in the driver
			}
			catch(Exception e0) {Log.exception(e0, driver);}	

			Utility.quitDriver(driver);
		}//End finally

	}//End PWorkflow_31080


	/**
	 * PWorkflow_31095 : Verify if User able to E-Sign the multiple objects
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect"},
			description = "Verify if User able to E-Sign the multiple objects")
	public void PWorkflow_31095(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("Safari"))
			throw new SkipException(driverType.toUpperCase() +" driver does not support multi select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);//Launch login page and login

			//Step-1 : Cretae Multiple Assignment Objects
			//-------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);//Opens the new Assignment object metadata card via New menu bar

			String objName = Utility.getObjectName(methodName) +"_Assignment1";//Frames the object name
			String objects = objName;

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card
			metadataCard.setInfo(dataPool.get("Properties"));//Sets the required property values in the metadata card
			metadataCard.setPropertyValue("Name or title", objName);//Sets the object name in the metadata card
			metadataCard.saveAndClose();//Creates the object

			Log.message("1.1. First object(" + objName + ") is created.", driver);

			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);//Opens the new Assignment object metadata card via New menu bar

			objName = Utility.getObjectName(methodName) +"_Assignment2";//Frames the object name
			objects += "\n"+ objName;

			metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card
			metadataCard.setInfo(dataPool.get("Properties"));//Sets the required property values in the metadata card
			metadataCard.setPropertyValue("Name or title", objName);//Sets the object name in the metadata card
			metadataCard.saveAndClose();//Creates the object

			Log.message("1.2. Second object(" + objName + ") is created.", driver);

			//Step-2: Navigate to Assigned to me view and then select the multiple objects in the view
			//----------------------------------------------------------------------------------------
			if (!homePage.taskPanel.clickItem(Caption.Taskpanel.AssignedToMe.Value))
				throw new Exception("Error while navigating to '" + Caption.Taskpanel.AssignedToMe.Value + "' view.");

			homePage.listView.clickMultipleItems(objects);//Multiselects the object in the view

			Log.message("2. Navigated to 'Assigned to me' view and multi selected the objects in the view.", driver);

			//Step-3: Perform Mark approve for the selected objects
			//------------------------------------------------------
			if(!homePage.taskPanel.clickItem(Caption.MenuItems.MarkApproved.Value))
				throw new Exception("Mark approved is not clicked from the task pane for the selected objects.");

			if (!MFilesDialog.exists(driver))
				throw new Exception("Mark Approve comment dialog is not opened after clicking the mark approve from task pane.");

			Log.message("3.1. Mark approve is clicked from task pane and comment dialog is opened in the view.", driver);

			MFilesDialog mfDialog = new MFilesDialog(driver);//Instantiates the MFiles Dialog
			mfDialog.clickOkButton();

			if (!MFilesDialog.isESignDialogExist(driver))
				throw new Exception("E-Sign dialog is not opened after click ok button in the Mark approve dialog for the selected objects.");

			Log.message("3.2. E-Sign dialog is opened successfully while click ok in the mark approve comment dialog.", driver);

			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog
			mfDialog.eSign(userName, password, "", "");//E-Signs the state transition

			Log.message("3.3. Performed E-Sign operation in the E-Sign dialog using the User: '" + userFullName + "' for the first object", driver);

			if (!MFilesDialog.isESignDialogExist(driver))
				throw new Exception("E-Sign dialog is not opened after click ok button in the Mark approve dialog for the selected objects.");

			Log.message("3.4. E-Sign dialog is opened successfully for the second object after E-Signing the object in the view.", driver);

			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog
			mfDialog.eSign(userName, password, "", "");//E-Signs the state transition

			Log.message("3.5. Performed E-Sign operation in the E-Sign dialog using the User: '" + userFullName + "' for the second object", driver);

			//Check Points
			//-------------
			//Navigate to recently accessed by me view
			//----------------------------------------
			if(!homePage.taskPanel.clickItem(Caption.MenuItems.RecentlyAccessedByMe.Value))
				throw new Exception("Error while navigating to Recently Accessed By Me view");

			String result = "";

			//Select the object in the view
			//-----------------------------
			if (!homePage.listView.clickItem(objects.split("\n")[0]))//Checks if the object is exist the view
				throw new Exception("'" + objects.split("\n")[0] + "' is not exist in the view");

			metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadata card

			//Check if signature property is exist in the selected object metadata card
			//-------------------------------------------------------------------------
			if (!metadataCard.propertyExists(dataPool.get("SignatureProperty")))
				result = "Signature property(" + dataPool.get("SignatureProperty") + ") is not exist in the object(" + objects.split("\n")[0] + ") metadata card.";

			//Check if approve icon is selected in the metadata card
			//-------------------------------------------------------
			if (!metadataCard.isApprovedSelected())
				result += " Object(" + objects.split("\n")[0] + ") is not Mark approved by the User: '" + userFullName + "'";

			//Select the object in the view
			//-----------------------------
			if (!homePage.listView.clickItem(objects.split("\n")[1]))//Checks if the object is exist the view
				throw new Exception("'" + objects.split("\n")[1] + "' is not exist in the view");

			metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadata card

			//Check if signature property is exist in the selected object metadata card
			//-------------------------------------------------------------------------
			if (!metadataCard.propertyExists(dataPool.get("SignatureProperty")))
				result += " Signature property(" + dataPool.get("SignatureProperty") + ") is not exist in the object(" + objects.split("\n")[1] + ") metadata card.";

			//Check if approve icon is selected in the metadata card
			//-------------------------------------------------------
			if (!metadataCard.isApprovedSelected())
				result += " Object(" + objects.split("\n")[1] + ") is not Mark approved by the User: '" + userFullName + "'";		

			//Verification: Check if Selected objects are E-Signed successfully
			//-----------------------------------------------------------------
			if (result.equals(""))
				Log.pass("Test case passed. Multi selected objects are Mark approved with Electronic Signature successfully.", driver);
			else
				Log.fail("Test case failed. Multi selected objects are not Mark approved with Electronic Signature. Additiona info. : "+ result.trim() +".", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try{
				MFilesDialog.closeMFilesDialog(driver);//Closes the M-Files dialog in the driver
			}
			catch(Exception e0) {Log.exception(e0, driver);}	

			Utility.quitDriver(driver);
		}//End finally

	}//End PWorkflow_31095

	/**
	 * PWorkflow_31097 : Verify if user able to perform esign operation for the other user checkedout object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			description = "Verify if user able to perform esign operation for the other user checkedout object")
	public void PWorkflow_31097(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);//Launch login page and login

			//Step-1 : Cretae Multiple Assignment Objects
			//-------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);//Opens the new Assignment object metadata card via New menu bar

			String objName = Utility.getObjectName(methodName) +"_Assignment";//Frames the object name

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card
			metadataCard.setInfo(dataPool.get("Properties"));//Sets the required property values in the metadata card
			metadataCard.setPropertyValue("Name or title", objName);//Sets the object name in the metadata card
			metadataCard.saveAndClose();//Creates the object

			if (!homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value))//Clicks the check out option from task pane
				throw new Exception("Checkout is not clicked for the selected object from the task pane.");

			Log.message("1. Object(" + objName + ") is created successfully and Object is checked out to the current user.", driver);

			//Step-2 : Logout from the M-Files Web Access and Login as Assigned user
			//-----------------------------------------------------------------------
			if (!Utility.logoutFromWebAccess(driver))//Logs out from the M-Files Web Access
				throw new Exception("Error while logging out from M-Files Web Access.");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("AssignedUserName"), dataPool.get("AssignedUserPassword"), testVault);//Login as the Assigned user into the MFWA

			Log.message("2. Logged out from the MFiles Web Access and Logged in as the Assigned user : '" + dataPool.get("AssignedUser") + "' into M-Files Web Access.", driver);

			//Step-3: Navigate to Assigned to me view and then select the object in the view
			//----------------------------------------------------------------------------------------
			if (!homePage.taskPanel.clickItem(Caption.Taskpanel.AssignedToMe.Value))
				throw new Exception("Error while navigating to '" + Caption.Taskpanel.AssignedToMe.Value + "' view.");

			if (!homePage.listView.clickItem(objName))//Selects the item in the view
				throw new Exception("'" + objName + "' is not selected in the view");

			Log.message("3. Navigated to 'Assigned to me' view and multi selected the objects in the view.", driver);

			//Step-4: Check Mark approve/reject option is available in the task pane for the other user checkedout object
			//-----------------------------------------------------------------------------------------------------
			if(homePage.taskPanel.isItemExists(Caption.MenuItems.MarkApproved.Value) || homePage.taskPanel.isItemExists(Caption.MenuItems.MarkRejected.Value))
				throw new Exception("Test case failed. Mark Approve/Reject option is displayed in task pane for the other user checked out object.");

			Log.message("4. Checked mark approve/reject option is not displayed in the task pane for the other user checked out object.", driver);

			//Verification: Check if expected warning message displayed while perform mark approve/reject from the metadata card
			//------------------------------------------------------------------------------------------------------------
			String result = "";
			metadataCard = new MetadataCard(driver, true);//Instantiates the right pane metadata card

			//Click Approve icon and save the changes in the metadatacard
			//-----------------------------------------------------------
			metadataCard.clickApproveIcon(true);//Clicks the approve icon in the metadata card
			metadataCard.saveAndClose();//Clicks the save button in the metadata card

			//Check if warning dialog is displayed
			//-------------------------------------
			if (!MFilesDialog.exists(driver, "M-Files Web"))
				throw new Exception("Test case failed. Warning dialog is not displayed while approve the other user checked out object via metadata card");

			//Checks if Expected message is displayed in the warning dialog
			//-------------------------------------------------------------
			MFilesDialog mfDialog = new MFilesDialog(driver, "M-Files Web");//Instantiates the MFiles dialog

			if (!mfDialog.getMessage().equalsIgnoreCase(dataPool.get("ExpectedWarningMessage")))
				result += " Expected warning message(" + dataPool.get("ExpectedWarningMessage") + ") is not displayed while approve the assignment via metadata card. [Actual message: '" + mfDialog.getMessage() + "']";

			//Close the M-Files Dialog
			//------------------------
			mfDialog.close();

			//Click Reject icon and save the changes in the metadatacard
			//-----------------------------------------------------------
			metadataCard.clickRejectIcon(true);//Clicks the Reject icon in the metadata card
			metadataCard.saveAndClose();//Clicks the save button in the metadata card

			//Check if warning dialog is displayed
			//-------------------------------------
			if (!MFilesDialog.exists(driver, "M-Files Web"))
				throw new Exception("Test case failed. Warning dialog is not displayed while reject the other user checked out object via metadata card");

			//Checks if Expected message is displayed in the warning dialog
			//-------------------------------------------------------------
			mfDialog = new MFilesDialog(driver, "M-Files Web");//Instantiates the MFiles dialog

			if (!mfDialog.getMessage().equalsIgnoreCase(dataPool.get("ExpectedWarningMessage")))
				result += " Expected warning message(" + dataPool.get("ExpectedWarningMessage") + ") is not displayed while reject the assignment via metadata card. [Actual message: '" + mfDialog.getMessage() + "']";

			//Verification: If user able to perform esign operation for the other user checkedout object
			//------------------------------------------------------------------------------------------
			if (result.equals(""))
				Log.pass("Test case passed. Expected warning displayed while approve/reject the assignment which is checked out by other user.", driver);
			else
				Log.fail("Test case failed. Expected warning displayed while approve/reject the assignment which is checked out by other user. [Additional info.: "+ result.trim() +"]", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try{
				MFilesDialog.closeMFilesDialog(driver);//Closes the M-Files dialog in the driver
			}
			catch(Exception e0) {Log.exception(e0, driver);}	

			Utility.quitDriver(driver);
		}//End finally

	}//End PWorkflow_31097

	/**
	 * PWorkflow_31099 : Verify if varies user able to approve the assignment object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect"},
			description = "Verify if varies user able to approve the assignment object")
	public void PWorkflow_31099(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("Safari"))
			throw new SkipException(driverType.toUpperCase() +" driver does not support multi select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);//Launch login page and login

			//Step-1 : Cretae Multiple Assignment Objects
			//-------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);//Opens the new Assignment object metadata card via New menu bar

			String objName = Utility.getObjectName(methodName) +"_Assignment1";//Frames the object name
			String objects = objName;

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card
			metadataCard.setInfo(dataPool.get("Properties1"));//Sets the required property values in the metadata card
			metadataCard.setPropertyValue("Name or title", objName);//Sets the object name in the metadata card
			metadataCard.saveAndClose();//Creates the object

			Log.message("1.1. First object(" + objName + ") is created.", driver);

			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);//Opens the new Assignment object metadata card via New menu bar

			objName = Utility.getObjectName(methodName) +"_Assignment2";//Frames the object name
			objects += "\n"+ objName;

			metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card
			metadataCard.setInfo(dataPool.get("Properties2"));//Sets the required property values in the metadata card
			metadataCard.setPropertyValue("Name or title", objName);//Sets the object name in the metadata card
			metadataCard.saveAndClose();//Creates the object

			Log.message("1.2. Second object(" + objName + ") is created.", driver);

			//Step-2: Navigate to Assigned to me view and then select the multiple objects in the view
			//----------------------------------------------------------------------------------------
			if (!homePage.taskPanel.clickItem(Caption.MenuItems.RecentlyAccessedByMe.Value))
				throw new Exception("Error while navigating to '" + Caption.MenuItems.RecentlyAccessedByMe.Value + "' view.");

			homePage.listView.clickMultipleItems(objects);//Multiselects the object in the view

			Log.message("2. Navigated to 'Recently accessed by me' view and multi selected the objects in the view.", driver);

			//Step-3: Perform Mark approve for the selected objects via metadata card
			//-----------------------------------------------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiates the right pane metadata card
			metadataCard.clickApproveIcon(true);//Clicks the approve icon in the metadata card
			metadataCard.saveAndClose();//Saves the changes in rigth pane metadata card

			Log.message("3. Varies user approved the assignment in the right pane metadata card", driver);

			//Verification: Check if Selected objects successfully in the view
			//----------------------------------------------------------------
			homePage.listView.clickRefresh();//Refreshes the view

			String result = "";

			//Select the object in the view
			//-----------------------------
			if(!homePage.listView.clickItem(objects.split("\n")[0]))
				throw new Exception(objects.split("\n")[0] +" is not selected in the view");

			//Check if Approve is selected in the metadatacard
			//-------------------------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiates the right pane metadata card

			if (!metadataCard.isApprovedSelected())
				result = objects.split("\n")[0] +" is not approved by the varies user. ";

			//Select the object in the view
			//-----------------------------
			if(!homePage.listView.clickItem(objects.split("\n")[1]))
				throw new Exception(objects.split("\n")[1] +" is not selected in the view");

			//Check if Approve is selected in the metadatacard
			//-------------------------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiates the right pane metadata card

			if (!metadataCard.isApprovedSelected())
				result += objects.split("\n")[1] +" is not approved by the varies user.";

			//Verification: If varies user able to approve the assignment object
			//------------------------------------------------------------------
			if (result.equals(""))
				Log.pass("Test case passed. Varies user approved the assignments successfully.", driver);
			else
				Log.fail("Test case failed. Varies user not approved the assignments successfully. Additional info.: "+ result, driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try{
				MFilesDialog.closeMFilesDialog(driver);//Closes the M-Files dialog in the driver
			}
			catch(Exception e0) {Log.exception(e0, driver);}	

			Utility.quitDriver(driver);
		}//End finally

	}//End PWorkflow_31099

	/**
	 * PWorkflow_31135 : Verify if user able to perform esign operation for the other user checkedout object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			description = "Verify if user able to perform esign operation for the other user checkedout object")
	public void PWorkflow_31135(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);//Launch login page and login

			//Step-1 : Cretae Assignment Object
			//---------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);//Opens the new Assignment object metadata card via New menu bar

			String objName = Utility.getObjectName(methodName) +"_Assignment";//Frames the object name

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card
			metadataCard.setInfo(dataPool.get("Properties"));//Sets the required property values in the metadata card
			metadataCard.setPropertyValue("Name or title", objName);//Sets the object name in the metadata card
			metadataCard.saveAndClose();//Creates the object

			Log.message("1. Object '" + objName + "' is created successfully.", driver);

			//Step-2: Perform Mark complete operation
			//---------------------------------------
			if (!homePage.listView.clickItem(objName))
				throw new Exception("Object(" + objName + ") is not selected in the view.");

			if (!homePage.taskPanel.clickItem(Caption.MenuItems.MarkComplete.Value))//Clicks the MarkApproved in the task pane
				throw new Exception("' " + Caption.MenuItems.MarkComplete.Value + " ' not clicked from the task pane for the selected object(" + objName + ").");

			Log.message("2.1. Mark Complete is clicked from the task pane for the E-Sign required action.", driver);

			MFilesDialog mfDialog = new MFilesDialog(driver, Caption.MenuItems.MarkComplete.Value);//Instantiates the MarkApproved dialog
			mfDialog.clickOkButton();//Clicks the OK button in the MarkApproved dialog

			Log.message("2.2. OK button is clicked in the Mark Complete dialog for the E-Sign required action.", driver);

			if (!MFilesDialog.isESignDialogExist(driver))//Waits and Checks if Electronic Signature dialog is exists or not
				throw new Exception("Electronic signature dialog is not opened while mark aprrove the assignment(" + objName + ") which requires E-Sign.");

			Log.message("2.3. E-Sign dialog is displayed after clicking OK button in the Mark Complete dialog", driver);

			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog
			mfDialog.eSign(userName, password, "", "");//E-Signs the state transition

			metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadatacard

			if (!metadataCard.isCompleteSelected(0))
				throw new Exception("Approval assignment(" + objName + ") is not mark completed by the user : '" + userFullName + "'");

			Log.message("2.4. Assignment(" + objName + ") is Mark completed by the user : '" + userFullName + "'", driver);

			//Step-3: Check if Signature object is created for the object successfully
			//------------------------------------------------------------------------
			if (!metadataCard.propertyExists(dataPool.get("SignatureProperty")))
				throw new Exception(dataPool.get("SignatureProperty") +" property is not added in the metadata card after Mark complete the object with E-Sign");

			String approvalObjName = dataPool.get("ApprovalAssignment") +": "+ userFullName;

			if (!metadataCard.getPropertyValue(dataPool.get("SignatureProperty")).contains(approvalObjName))
				throw new Exception("Approval assignment with name(" + approvalObjName + ") is not added in the signature property. [Actual value: '" + metadataCard.getPropertyValue(dataPool.get("SignatureProperty")) + "']");

			approvalObjName = metadataCard.getPropertyValue(dataPool.get("SignatureProperty"));

			Log.message("3. Object(" + objName + ") is mark completed and signature object property is added with expected value in the metadata card.", driver);

			//Verification: Check if signature object is created with correct signature reason and signature meaning
			//------------------------------------------------------------------------------------------------------
			String result = "";

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Relationships.Value);//Navigates to the relationship view of the object

			if (!homePage.listView.clickItem(approvalObjName))//Clicks the object in the view
				throw new Exception("Signature Object(" + approvalObjName + ") not selected in the view.");

			if (!homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value))//Clicks the option in the task pane
				throw new Exception("Properties is not clicked from the task pane for the selected object.");

			metadataCard = new MetadataCard(driver);//Instantiates the metadata card

			if (!metadataCard.getPropertyValue(dataPool.get("SignatureReasonProperty")).equalsIgnoreCase(dataPool.get("SignatureReasonPropertyValue")))
				result = "Expected signature reason(" + dataPool.get("SignatureReasonPropertyValue") + ") is not displayed in the signature object.[Actual value: '" + metadataCard.getPropertyValue(dataPool.get("SignatureReasonProperty")) + "']. ";

			if (!metadataCard.getPropertyValue(dataPool.get("SignatureMeaningProperty")).equalsIgnoreCase(dataPool.get("SignatureMeaningPropertyValue")))
				result += " Expected signature meaning(" + dataPool.get("SignatureMeaningPropertyValue") + ") is not displayed in the signature object.[Actual value: '" + metadataCard.getPropertyValue(dataPool.get("SignatureMeaningProperty")) + "']. ";

			if (!metadataCard.getPropertyValue(dataPool.get("SignatureIdentifierProperty")).equalsIgnoreCase(dataPool.get("SignatureIdentifierPropertyValue")))
				result += " Expected signature identifier(" + dataPool.get("SignatureIdentifierPropertyValue") + ") is not displayed in the signature object.[Actual value: '" + metadataCard.getPropertyValue(dataPool.get("SignatureIdentifierProperty")) + "']. ";

			if (result.equals(""))
				Log.pass("Test case passed. Signature object is created with expected values while perform mark complete action.", driver);
			else
				Log.fail("Test case failed. Signature object is not created with expected values while perform mark complete action.Additional info. : "+ result.trim(), driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try{
				MFilesDialog.closeMFilesDialog(driver);//Closes the M-Files dialog in the driver
			}
			catch(Exception e0) {Log.exception(e0, driver);}	

			Utility.quitDriver(driver);
		}//End finally

	}//End PWorkflow_31135


	/**
	 * PWorkflow_32151 : Verify if substitute user able to view Esign dialog when approving existing assignment with Any can approve class
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			description = "Verify if substitute user able to view Esign dialog when approving existing assignment with Any can approve class")
	public void PWorkflow_32151(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);//Launch login page and login

			//Step-1 : Cretae Assignment Object
			//---------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);//Opens the new Assignment object metadata card via New menu bar

			String objName = Utility.getObjectName(methodName) +"_Assignment";//Frames the object name

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card
			metadataCard.setInfo(dataPool.get("Properties"));//Sets the required property values in the metadata card
			metadataCard.setPropertyValue("Name or title", objName);//Sets the object name in the metadata card
			metadataCard.saveAndClose();//Creates the object

			Log.message("1. Object '" + objName + "' is created successfully.", driver);

			//Step-2 : Logout from the M-Files Web Access and Login as Substitute user
			//-----------------------------------------------------------------------
			if (!Utility.logoutFromWebAccess(driver))//Logs out from the M-Files Web Access
				throw new Exception("Error while logging out from M-Files Web Access.");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("SubstituteUserName"), dataPool.get("SubstituteUserPassword"), testVault);//Login as the Assigned user into the MFWA

			Log.message("2. Logged out from the MFiles Web Access and Logged in as the Substitute user : '" + dataPool.get("SubstituteUser") + "' into M-Files Web Access.", driver);

			//Step-3: Perform Mark approve via metadatacard
			//---------------------------------------------
			if(!homePage.taskPanel.clickItem(Caption.Taskpanel.AssignedToMe.Value))
				throw new Exception("Error while navigating to Assigned To Me view");

			if (!homePage.listView.clickItem(objName))//Clicks the object in the view
				throw new Exception("Object(" + objName + ") not selected in the view.");

			metadataCard = new MetadataCard(driver, true);//Instantiates the new object metadata card in rightpane
			metadataCard.clickApproveIcon(true);//Clicks the approve icon in the metadata card
			metadataCard.saveAndClose();//Saves the changes

			if (!MFilesDialog.exists(driver, "M-Files Web"))
				throw new Exception("Wwarning dialog is not displayed while approving the assignment via metadata card which requires electroinc signature.");

			MFilesDialog mfDialog = new MFilesDialog(driver, "M-Files Web");//Instantiates the M-Files dialog

			if (!mfDialog.getMessage().equalsIgnoreCase(dataPool.get("ExpectedWarningMesssage")))
				throw new Exception("Expected warning message(" + dataPool.get("ExpectedWarningMesssage") + ") is not displayed while approving the assignment which requires electroinc signature via metadata card. [Actual message: '" + mfDialog.getMessage().trim() + "']"); 

			Log.message("3. Warning dialog is displayed as expected while approving the assignment which requires electronic signature via metadata card", driver);

			mfDialog.close();//Closes the M-Files dialog in the view
			metadataCard.cancelAndConfirm();//Discard the changes in the rightpane metadatacard

			//Step-4: Perform Mark approve operation via task pane
			//-----------------------------------------------------
			if (!homePage.listView.clickItem(objName))
				throw new Exception("Object(" + objName + ") is not selected in the view.");

			if (!homePage.taskPanel.clickItem(Caption.MenuItems.MarkApproved.Value))//Clicks the MarkApproved in the task pane
				throw new Exception("' " + Caption.MenuItems.MarkApproved.Value + " ' not clicked from the task pane for the selected object(" + objName + ").");

			Log.message("4.1. Mark Approve is clicked from the task pane for the E-Sign required action.", driver);

			mfDialog = new MFilesDialog(driver, Caption.MenuItems.MarkApproved.Value);//Instantiates the MarkApproved dialog
			mfDialog.clickOkButton();//Clicks the OK button in the MarkApproved dialog

			Log.message("4.2. OK button is clicked in the Mark Approve dialog for the E-Sign required action.", driver);

			if (!MFilesDialog.isESignDialogExist(driver))//Waits and Checks if Electronic Signature dialog is exists or not
				throw new Exception("Electronic signature dialog is not opened while mark approve the assignment(" + objName + ") which requires E-Sign.");

			Log.message("4.3. E-Sign dialog is displayed after clicking OK button in the Mark Approve dialog", driver);

			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog
			mfDialog.eSign(dataPool.get("SubstituteUserName"), dataPool.get("SubstituteUserPassword"), "", "");//E-Signs the state transition

			if(!homePage.taskPanel.clickItem(Caption.MenuItems.RecentlyAccessedByMe.Value))
				throw new Exception("Error while navigating to Recently Accessed By Me view");

			if (!homePage.listView.clickItem(objName))//Clicks the object in the view
				throw new Exception("Object(" + objName + ") not selected in the view.");

			metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadatacard

			if (!metadataCard.isApprovedSelected(0))
				throw new Exception("Assignment(" + objName + ") is not mark approved by the user : '" + dataPool.get("SubstituteUser") + "'");

			Log.message("4.4. Assignment(" + objName + ") is Mark Approved by the user : '" + dataPool.get("SubstituteUser") + "'", driver);

			//Step-5: Check if Signature object is created for the object successfully
			//------------------------------------------------------------------------
			if (!metadataCard.propertyExists(dataPool.get("SignatureProperty")))
				throw new Exception(dataPool.get("SignatureProperty") +" property is not added in the metadata card after Mark approve the object with E-Sign");

			String approvalObjName = dataPool.get("ApprovalAssignment") +": "+ dataPool.get("SubstituteUser");

			if (!metadataCard.getPropertyValue(dataPool.get("SignatureProperty")).contains(approvalObjName))
				throw new Exception("Approval assignment with name(" + approvalObjName + ") is not added in the signature property. [Actual value: '" + metadataCard.getPropertyValue(dataPool.get("SignatureProperty")) + "']");

			approvalObjName = metadataCard.getPropertyValue(dataPool.get("SignatureProperty"));

			Log.message("5. Object(" + objName + ") is mark approved and signature object property is added with expected value in the metadata card.", driver);

			//Verification: Check if signature object is created with correct signature reason and signature meaning
			//------------------------------------------------------------------------------------------------------
			String result = "";

			//Navigate to Relationships view
			//------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Relationships.Value);//Navigates to the relationship view of the object

			//Selects the object in the view
			//------------------------------
			if (!homePage.listView.clickItem(approvalObjName))//Clicks the object in the view
				throw new Exception("Signature Object(" + approvalObjName + ") not selected in the view.");

			//Open the Popout metadata card via task pane
			//-------------------------------------------
			if (!homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value))//Clicks the option in the task pane
				throw new Exception("Properties is not clicked from the task pane for the selected object.");

			//Check the signature reason in the metadatacard
			//-----------------------------------------------
			metadataCard = new MetadataCard(driver);//Instantiates the metadata card

			if (!metadataCard.getPropertyValue(dataPool.get("SignatureReasonProperty")).equalsIgnoreCase(dataPool.get("SignatureReasonPropertyValue")))
				result = "Expected signature reason(" + dataPool.get("SignatureReasonPropertyValue") + ") is not displayed in the signature object.[Actual value: '" + metadataCard.getPropertyValue(dataPool.get("SignatureReasonProperty")) + "']. ";

			//Check the signature meaning in the metadatacard
			//-----------------------------------------------
			if (!metadataCard.getPropertyValue(dataPool.get("SignatureMeaningProperty")).equalsIgnoreCase(dataPool.get("SignatureMeaningPropertyValue")))
				result += " Expected signature meaning(" + dataPool.get("SignatureMeaningPropertyValue") + ") is not displayed in the signature object.[Actual value: '" + metadataCard.getPropertyValue(dataPool.get("SignatureMeaningProperty")) + "']. ";

			//Check the signature identifier in the metadatacard
			//-----------------------------------------------
			if (!metadataCard.getPropertyValue(dataPool.get("SignatureIdentifierProperty")).equalsIgnoreCase(dataPool.get("SignatureIdentifierPropertyValue")))
				result += " Expected signature identifier(" + dataPool.get("SignatureIdentifierPropertyValue") + ") is not displayed in the signature object.[Actual value: '" + metadataCard.getPropertyValue(dataPool.get("SignatureIdentifierProperty")) + "']. ";

			//Verification: If substitute user able to view Esign dialog when approving existing assignment with Any can approve class
			//------------------------------------------------------------------------------------------------------------------------		
			if (result.equals(""))
				Log.pass("Test case passed. Substitute user able to view Esign dialog when approving existing assignment with Any can approve class and Signature object is created with expected values while perform mark approve action.", driver);
			else
				Log.fail("Test case failed. Signature object is created with expected values while perform mark approve action. Additional info. : "+ result.trim(), driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try{
				MFilesDialog.closeMFilesDialog(driver);//Closes the M-Files dialog in the driver
			}
			catch(Exception e0) {Log.exception(e0, driver);}	

			Utility.quitDriver(driver);
		}//End finally

	}//End PWorkflow_32151

	/**
	 * PWorkflow_32152 : Verify if substitute user able to view Esign dialog when approving existing assignment with All must approve class
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			description = "Verify if substitute user able to view Esign dialog when approving existing assignment with All must approve class")
	public void PWorkflow_32152(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);//Launch login page and login

			//Step-1 : Cretae Assignment Object
			//---------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);//Opens the new Assignment object metadata card via New menu bar

			String objName = Utility.getObjectName(methodName) +"_Assignment";//Frames the object name

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card
			metadataCard.setInfo(dataPool.get("Properties"));//Sets the required property values in the metadata card
			metadataCard.setPropertyValue("Name or title", objName);//Sets the object name in the metadata card
			metadataCard.saveAndClose();//Creates the object

			Log.message("1. Object '" + objName + "' is created successfully.", driver);

			//Step-2 : Logout from the M-Files Web Access and Login as Substitute user
			//-----------------------------------------------------------------------
			if (!Utility.logoutFromWebAccess(driver))//Logs out from the M-Files Web Access
				throw new Exception("Error while logging out from M-Files Web Access.");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("SubstituteUserName"), dataPool.get("SubstituteUserPassword"), testVault);//Login as the Assigned user into the MFWA

			Log.message("2. Logged out from the MFiles Web Access and Logged in as the Substitute user : '" + dataPool.get("SubstituteUser") + "' into M-Files Web Access.", driver);

			//Step-3: Perform Mark approve via metadatacard
			//---------------------------------------------
			if(!homePage.taskPanel.clickItem(Caption.Taskpanel.AssignedToMe.Value))
				throw new Exception("Error while navigating to Assigned To Me view");

			if (!homePage.listView.clickItem(objName))//Clicks the object in the view
				throw new Exception("Object(" + objName + ") not selected in the view.");

			metadataCard = new MetadataCard(driver, true);//Instantiates the new object metadata card in rightpane
			metadataCard.clickApproveIcon(true);//Clicks the approve icon in the metadata card
			metadataCard.saveAndClose();//Saves the changes

			if (!MFilesDialog.exists(driver, "M-Files Web"))
				throw new Exception("Wwarning dialog is not displayed while approving the assignment via metadata card which requires electroinc signature.");

			MFilesDialog mfDialog = new MFilesDialog(driver, "M-Files Web");//Instantiates the M-Files dialog

			if (!mfDialog.getMessage().equalsIgnoreCase(dataPool.get("ExpectedWarningMesssage")))
				throw new Exception("Expected warning message(" + dataPool.get("ExpectedWarningMesssage") + ") is not displayed while approving the assignment which requires electroinc signature via metadata card. [Actual message: '" + mfDialog.getMessage().trim() + "']"); 

			Log.message("3. Warning dialog is displayed as expected while approving the assignment which requires electronic signature via metadata card", driver);

			mfDialog.close();//Closes the M-Files dialog in the view
			metadataCard.cancelAndConfirm();//Discard the changes in the rightpane metadatacard

			//Step-4: Perform Mark approve operation via task pane
			//-----------------------------------------------------
			if (!homePage.listView.clickItem(objName))
				throw new Exception("Object(" + objName + ") is not selected in the view.");

			if (!homePage.taskPanel.clickItem(Caption.MenuItems.MarkApproved.Value))//Clicks the MarkApproved in the task pane
				throw new Exception("' " + Caption.MenuItems.MarkApproved.Value + " ' not clicked from the task pane for the selected object(" + objName + ").");

			Log.message("4.1. Mark Approve is clicked from the task pane for the E-Sign required action.", driver);

			mfDialog = new MFilesDialog(driver, Caption.MenuItems.MarkApproved.Value);//Instantiates the MarkApproved dialog
			mfDialog.clickOkButton();//Clicks the OK button in the MarkApproved dialog

			Log.message("4.2. OK button is clicked in the Mark Approve dialog for the E-Sign required action.", driver);

			if (!MFilesDialog.isESignDialogExist(driver))//Waits and Checks if Electronic Signature dialog is exists or not
				throw new Exception("Electronic signature dialog is not opened while mark approve the assignment(" + objName + ") which requires E-Sign.");

			Log.message("4.3. E-Sign dialog is displayed after clicking OK button in the Mark Approve dialog", driver);

			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog
			mfDialog.eSign(dataPool.get("SubstituteUserName"), dataPool.get("SubstituteUserPassword"), "", "");//E-Signs the state transition

			if(!homePage.taskPanel.clickItem(Caption.MenuItems.RecentlyAccessedByMe.Value))
				throw new Exception("Error while navigating to Recently Accessed By Me view");

			if (!homePage.listView.clickItem(objName))//Clicks the object in the view
				throw new Exception("Object(" + objName + ") not selected in the view.");

			metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadatacard

			if (!metadataCard.isApprovedSelected(0))
				throw new Exception("Assignment(" + objName + ") is not mark approved by the user : '" + dataPool.get("SubstituteUser") + "'");

			Log.message("4.4. Assignment(" + objName + ") is Mark approved by the user : '" + dataPool.get("SubstituteUser") + "'", driver);

			//Step-5: Check if Signature object is created for the object successfully
			//------------------------------------------------------------------------
			if (!metadataCard.propertyExists(dataPool.get("SignatureProperty")))
				throw new Exception(dataPool.get("SignatureProperty") +" property is not added in the metadata card after Mark approve the object with E-Sign");

			String approvalObjName = dataPool.get("ApprovalAssignment") +": "+ dataPool.get("SubstituteUser");

			if (!metadataCard.getPropertyValue(dataPool.get("SignatureProperty")).contains(approvalObjName))
				throw new Exception("Approval assignment with name(" + approvalObjName + ") is not added in the signature property. [Actual value: '" + metadataCard.getPropertyValue(dataPool.get("SignatureProperty")) + "']");

			approvalObjName = metadataCard.getPropertyValue(dataPool.get("SignatureProperty"));

			Log.message("5. Object(" + objName + ") is mark approved and signature object property is added with expected value in the metadata card.", driver);

			//Verification: Check if signature object is created with correct signature reason and signature meaning
			//------------------------------------------------------------------------------------------------------
			String result = "";

			//Navigate to the Relationship view
			//---------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Relationships.Value);//Navigates to the relationship view of the object

			//Select the object in the view
			//------------------------------
			if (!homePage.listView.clickItem(approvalObjName))//Clicks the object in the view
				throw new Exception("Signature Object(" + approvalObjName + ") not selected in the view.");

			//Open the popout metadata card via task pane
			//--------------------------------------------
			if (!homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value))//Clicks the option in the task pane
				throw new Exception("Properties is not clicked from the task pane for the selected object.");

			//Check if Signature reason is set as expected in the metadata card
			//-----------------------------------------------------------------
			metadataCard = new MetadataCard(driver);//Instantiates the metadata card

			if (!metadataCard.getPropertyValue(dataPool.get("SignatureReasonProperty")).equalsIgnoreCase(dataPool.get("SignatureReasonPropertyValue")))
				result = "Expected signature reason(" + dataPool.get("SignatureReasonPropertyValue") + ") is not displayed in the signature object.[Actual value: '" + metadataCard.getPropertyValue(dataPool.get("SignatureReasonProperty")) + "']. ";

			//Check if Signature meaning is set as expected in the metadata card
			//-----------------------------------------------------------------
			if (!metadataCard.getPropertyValue(dataPool.get("SignatureMeaningProperty")).equalsIgnoreCase(dataPool.get("SignatureMeaningPropertyValue")))
				result += " Expected signature meaning(" + dataPool.get("SignatureMeaningPropertyValue") + ") is not displayed in the signature object.[Actual value: '" + metadataCard.getPropertyValue(dataPool.get("SignatureMeaningProperty")) + "']. ";

			//Check if Signature identifier is set as expected in the metadata card
			//-----------------------------------------------------------------
			if (!metadataCard.getPropertyValue(dataPool.get("SignatureIdentifierProperty")).equalsIgnoreCase(dataPool.get("SignatureIdentifierPropertyValue")))
				result += " Expected signature identifier(" + dataPool.get("SignatureIdentifierPropertyValue") + ") is not displayed in the signature object.[Actual value: '" + metadataCard.getPropertyValue(dataPool.get("SignatureIdentifierProperty")) + "']. ";

			//Verification: If substitute user able to view Esign dialog when approving existing assignment with All must approve class
			//-------------------------------------------------------------------------------------------------------------------------
			if (result.equals(""))
				Log.pass("Test case passed. Substitute user able to view Esign dialog when approving existing assignment with All must approve class and Signature object is created with expected values while perform mark approve action.", driver);
			else
				Log.fail("Test case failed. Signature object is created with expected values while perform mark approve action. Additional info. : "+ result.trim(), driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try{
				MFilesDialog.closeMFilesDialog(driver);//Closes the M-Files dialog in the driver
			}
			catch(Exception e0) {Log.exception(e0, driver);}	

			Utility.quitDriver(driver);
		}//End finally

	}//End PWorkflow_32152

	/**
	 * PWorkflow_32153 : Verify if substitute user able to view Esign dialog when rejecting existing assignment with Any can approve class
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			description = "Verify if substitute user able to view Esign dialog when rejecting existing assignment with Any can approve class")
	public void PWorkflow_32153(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);//Launch login page and login

			//Step-1 : Cretae Assignment Object
			//---------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);//Opens the new Assignment object metadata card via New menu bar

			String objName = Utility.getObjectName(methodName) +"_Assignment";//Frames the object name

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card
			metadataCard.setInfo(dataPool.get("Properties"));//Sets the required property values in the metadata card
			metadataCard.setPropertyValue("Name or title", objName);//Sets the object name in the metadata card
			metadataCard.saveAndClose();//Creates the object

			Log.message("1. Object '" + objName + "' is created successfully.", driver);

			//Step-2 : Logout from the M-Files Web Access and Login as Substitute user
			//-----------------------------------------------------------------------
			if (!Utility.logoutFromWebAccess(driver))//Logs out from the M-Files Web Access
				throw new Exception("Error while logging out from M-Files Web Access.");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("SubstituteUserName"), dataPool.get("SubstituteUserPassword"), testVault);//Login as the Assigned user into the MFWA

			Log.message("2. Logged out from the MFiles Web Access and Logged in as the Substitute user : '" + dataPool.get("SubstituteUser") + "' into M-Files Web Access.", driver);

			//Step-3: Perform Mark Reject via metadatacard
			//---------------------------------------------
			if(!homePage.taskPanel.clickItem(Caption.Taskpanel.AssignedToMe.Value))
				throw new Exception("Error while navigating to Assigned To Me view");

			if (!homePage.listView.clickItem(objName))//Clicks the object in the view
				throw new Exception("Object(" + objName + ") not selected in the view.");

			metadataCard = new MetadataCard(driver, true);//Instantiates the new object metadata card in rightpane
			metadataCard.clickRejectIcon(true);//Clicks the reject icon in the metadata card
			metadataCard.saveAndClose();//Saves the changes

			if (!MFilesDialog.exists(driver, "M-Files Web"))
				throw new Exception("Wwarning dialog is not displayed while rejecting the assignment via metadata card which requires electroinc signature.");

			MFilesDialog mfDialog = new MFilesDialog(driver, "M-Files Web");//Instantiates the M-Files dialog

			if (!mfDialog.getMessage().equalsIgnoreCase(dataPool.get("ExpectedWarningMesssage")))
				throw new Exception("Expected warning message(" + dataPool.get("ExpectedWarningMesssage") + ") is not displayed while rejecting the assignment which requires electroinc signature via metadata card. [Actual message: '" + mfDialog.getMessage().trim() + "']"); 

			Log.message("3. Warning dialog is displayed as expected while rejecting the assignment which requires electronic signature via metadata card", driver);

			mfDialog.close();//Closes the M-Files dialog in the view
			metadataCard.cancelAndConfirm();//Discard the changes in the rightpane metadatacard

			//Step-4: Perform Mark Reject operation via task pane
			//-----------------------------------------------------
			if (!homePage.listView.clickItem(objName))
				throw new Exception("Object(" + objName + ") is not selected in the view.");

			if (!homePage.taskPanel.clickItem(Caption.MenuItems.MarkRejected.Value))//Clicks the MarkRejected in the task pane
				throw new Exception("' " + Caption.MenuItems.MarkRejected.Value + " ' not clicked from the task pane for the selected object(" + objName + ").");

			Log.message("4.1. Mark Reject is clicked from the task pane for the E-Sign required action.", driver);

			mfDialog = new MFilesDialog(driver, Caption.MenuItems.MarkRejected.Value);//Instantiates the MarkRejected dialog
			mfDialog.clickOkButton();//Clicks the OK button in the MarkRejected dialog

			Log.message("4.2. OK button is clicked in the Mark Reject dialog for the E-Sign required action.", driver);

			if (!MFilesDialog.isESignDialogExist(driver))//Waits and Checks if Electronic Signature dialog is exists or not
				throw new Exception("Electronic signature dialog is not opened while mark reject the assignment(" + objName + ") which requires E-Sign.");

			Log.message("4.3. E-Sign dialog is displayed after clicking OK button in the Mark Reject dialog", driver);

			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog
			mfDialog.eSign(dataPool.get("SubstituteUserName"), dataPool.get("SubstituteUserPassword"), "", "");//E-Signs the state transition

			if(!homePage.taskPanel.clickItem(Caption.MenuItems.RecentlyAccessedByMe.Value))
				throw new Exception("Error while navigating to Recently Accessed By Me view");

			if (!homePage.listView.clickItem(objName))//Clicks the object in the view
				throw new Exception("Object(" + objName + ") not selected in the view.");

			metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadatacard

			if (!metadataCard.isRejectedSelected(0))
				throw new Exception("Assignment(" + objName + ") is not mark rejected by the user : '" + dataPool.get("SubstituteUser") + "'");

			Log.message("4.4. Assignment(" + objName + ") is Mark rejected by the user : '" + dataPool.get("SubstituteUser") + "'", driver);

			//Step-5: Check if Signature object is created for the object successfully
			//------------------------------------------------------------------------
			if (!metadataCard.propertyExists(dataPool.get("SignatureProperty")))
				throw new Exception(dataPool.get("SignatureProperty") +" property is not added in the metadata card after Mark reject the object with E-Sign");

			String approvalObjName = dataPool.get("ApprovalAssignment") +": "+ dataPool.get("SubstituteUser");

			if (!metadataCard.getPropertyValue(dataPool.get("SignatureProperty")).contains(approvalObjName))
				throw new Exception("Approval assignment with name(" + approvalObjName + ") is not added in the signature property. [Actual value: '" + metadataCard.getPropertyValue(dataPool.get("SignatureProperty")) + "']");

			approvalObjName = metadataCard.getPropertyValue(dataPool.get("SignatureProperty"));

			Log.message("5. Object(" + objName + ") is mark rejected and signature object property is added with expected value in the metadata card.", driver);

			//Verification: Check if signature object is created with correct signature reason and signature meaning
			//------------------------------------------------------------------------------------------------------
			String result = "";

			//Navigate to the Relationship view
			//---------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Relationships.Value);//Navigates to the relationship view of the object

			//Select the object in the view
			//------------------------------
			if (!homePage.listView.clickItem(approvalObjName))//Clicks the object in the view
				throw new Exception("Signature Object(" + approvalObjName + ") not selected in the view.");

			//Open the popout metadata card via task pane
			//--------------------------------------------
			if (!homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value))//Clicks the option in the task pane
				throw new Exception("Properties is not clicked from the task pane for the selected object.");

			//Check if Signature reason is set as expected in the metadata card
			//-----------------------------------------------------------------
			metadataCard = new MetadataCard(driver);//Instantiates the metadata card

			if (!metadataCard.getPropertyValue(dataPool.get("SignatureReasonProperty")).equalsIgnoreCase(dataPool.get("SignatureReasonPropertyValue")))
				result = "Expected signature reason(" + dataPool.get("SignatureReasonPropertyValue") + ") is not displayed in the signature object.[Actual value: '" + metadataCard.getPropertyValue(dataPool.get("SignatureReasonProperty")) + "']. ";

			//Check if Signature meaning is set as expected in the metadata card
			//-----------------------------------------------------------------
			if (!metadataCard.getPropertyValue(dataPool.get("SignatureMeaningProperty")).equalsIgnoreCase(dataPool.get("SignatureMeaningPropertyValue")))
				result += " Expected signature meaning(" + dataPool.get("SignatureMeaningPropertyValue") + ") is not displayed in the signature object.[Actual value: '" + metadataCard.getPropertyValue(dataPool.get("SignatureMeaningProperty")) + "']. ";

			//Check if Signature identifier is set as expected in the metadata card
			//-----------------------------------------------------------------
			if (!metadataCard.getPropertyValue(dataPool.get("SignatureIdentifierProperty")).equalsIgnoreCase(dataPool.get("SignatureIdentifierPropertyValue")))
				result += " Expected signature identifier(" + dataPool.get("SignatureIdentifierPropertyValue") + ") is not displayed in the signature object.[Actual value: '" + metadataCard.getPropertyValue(dataPool.get("SignatureIdentifierProperty")) + "']. ";

			//Verification: If substitute user able to view Esign dialog when rejecting existing assignment with Any can approve class
			//------------------------------------------------------------------------------------------------------------------------
			if (result.equals(""))
				Log.pass("Test case passed. Substitute user able to view Esign dialog when rejecting existing assignment with Any can approve class and Signature object is created with expected values while perform mark reject action.", driver);
			else
				Log.fail("Test case failed. Signature object is created with expected values while perform mark reject action. Additional info. : "+ result.trim(), driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try{
				MFilesDialog.closeMFilesDialog(driver);//Closes the M-Files dialog in the driver
			}
			catch(Exception e0) {Log.exception(e0, driver);}	

			Utility.quitDriver(driver);
		}//End finally

	}//End PWorkflow_32153

	/**
	 * PWorkflow_32154 : Verify if substitute user able to view Esign dialog when rejecting existing assignment with All must approve class
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			description = "Verify if substitute user able to view Esign dialog when rejecting existing assignment with All must approve class")
	public void PWorkflow_32154(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);//Launch login page and login

			//Step-1 : Cretae Assignment Object
			//---------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);//Opens the new Assignment object metadata card via New menu bar

			String objName = Utility.getObjectName(methodName) +"_Assignment";//Frames the object name

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card
			metadataCard.setInfo(dataPool.get("Properties"));//Sets the required property values in the metadata card
			metadataCard.setPropertyValue("Name or title", objName);//Sets the object name in the metadata card
			metadataCard.saveAndClose();//Creates the object

			Log.message("1. Object '" + objName + "' is created successfully.", driver);

			//Step-2 : Logout from the M-Files Web Access and Login as Substitute user
			//-----------------------------------------------------------------------
			if (!Utility.logoutFromWebAccess(driver))//Logs out from the M-Files Web Access
				throw new Exception("Error while logging out from M-Files Web Access.");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("SubstituteUserName"), dataPool.get("SubstituteUserPassword"), testVault);//Login as the Assigned user into the MFWA

			Log.message("2. Logged out from the MFiles Web Access and Logged in as the Substitute user : '" + dataPool.get("SubstituteUser") + "' into M-Files Web Access.", driver);

			//Step-3: Perform Mark Reject via metadatacard
			//---------------------------------------------
			if(!homePage.taskPanel.clickItem(Caption.Taskpanel.AssignedToMe.Value))
				throw new Exception("Error while navigating to Assigned To Me view");

			if (!homePage.listView.clickItem(objName))//Clicks the object in the view
				throw new Exception("Object(" + objName + ") not selected in the view.");

			metadataCard = new MetadataCard(driver, true);//Instantiates the new object metadata card in rightpane
			metadataCard.clickRejectIcon(true);//Clicks the Reject icon in the metadata card
			metadataCard.saveAndClose();//Saves the changes

			if (!MFilesDialog.exists(driver, "M-Files Web"))
				throw new Exception("Warning dialog is not displayed while rejecting the assignment via metadata card which requires electroinc signature.");

			MFilesDialog mfDialog = new MFilesDialog(driver, "M-Files Web");//Instantiates the M-Files dialog

			if (!mfDialog.getMessage().equalsIgnoreCase(dataPool.get("ExpectedWarningMesssage")))
				throw new Exception("Expected warning message(" + dataPool.get("ExpectedWarningMesssage") + ") is not displayed while rejecting the assignment which requires electroinc signature via metadata card. [Actual message: '" + mfDialog.getMessage().trim() + "']"); 

			Log.message("3. Warning dialog is displayed as expected while approving the assignment which requires electronic signature via metadata card", driver);

			mfDialog.close();//Closes the M-Files dialog in the view
			metadataCard.cancelAndConfirm();//Discard the changes in the rightpane metadatacard

			//Step-4: Perform Mark reject operation via task pane
			//-----------------------------------------------------
			if (!homePage.listView.clickItem(objName))
				throw new Exception("Object(" + objName + ") is not selected in the view.");

			if (!homePage.taskPanel.clickItem(Caption.MenuItems.MarkRejected.Value))//Clicks the MarkRejected in the task pane
				throw new Exception("' " + Caption.MenuItems.MarkRejected.Value + " ' not clicked from the task pane for the selected object(" + objName + ").");

			Log.message("4.1. Mark Reject is clicked from the task pane for the E-Sign required action.", driver);

			mfDialog = new MFilesDialog(driver, Caption.MenuItems.MarkRejected.Value);//Instantiates the MarkRejected dialog
			mfDialog.clickOkButton();//Clicks the OK button in the MarkRejected dialog

			Log.message("4.2. OK button is clicked in the Mark Reject dialog for the E-Sign required action.", driver);

			if (!MFilesDialog.isESignDialogExist(driver))//Waits and Checks if Electronic Signature dialog is exists or not
				throw new Exception("Electronic signature dialog is not opened while mark reject the assignment(" + objName + ") which requires E-Sign.");

			Log.message("4.3. E-Sign dialog is displayed after clicking OK button in the Mark Reject dialog", driver);

			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog
			mfDialog.eSign(dataPool.get("SubstituteUserName"), dataPool.get("SubstituteUserPassword"), "", "");//E-Signs the state transition

			if(!homePage.taskPanel.clickItem(Caption.MenuItems.RecentlyAccessedByMe.Value))
				throw new Exception("Error while navigating to Recently Accessed By Me view");

			if (!homePage.listView.clickItem(objName))//Clicks the object in the view
				throw new Exception("Object(" + objName + ") not selected in the view.");

			metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadatacard

			if (!metadataCard.isRejectedSelected(0))
				throw new Exception("Assignment(" + objName + ") is not mark rejected by the user : '" + dataPool.get("SubstituteUser") + "'");

			Log.message("4.4. Assignment(" + objName + ") is Mark rejected by the user : '" + dataPool.get("SubstituteUser") + "'", driver);

			//Step-5: Check if Signature object is created for the object successfully
			//------------------------------------------------------------------------
			if (!metadataCard.propertyExists(dataPool.get("SignatureProperty")))
				throw new Exception(dataPool.get("SignatureProperty") +" property is not added in the metadata card after Mark reject the object with E-Sign");

			String approvalObjName = dataPool.get("ApprovalAssignment") +": "+ dataPool.get("SubstituteUser");

			if (!metadataCard.getPropertyValue(dataPool.get("SignatureProperty")).contains(approvalObjName))
				throw new Exception("Assignment with name(" + approvalObjName + ") is not added in the signature property. [Actual value: '" + metadataCard.getPropertyValue(dataPool.get("SignatureProperty")) + "']");

			approvalObjName = metadataCard.getPropertyValue(dataPool.get("SignatureProperty"));

			Log.message("5. Object(" + objName + ") is mark rejected and signature object property is added with expected value in the metadata card.", driver);

			//Verification: Check if signature object is created with correct signature reason and signature meaning
			//------------------------------------------------------------------------------------------------------
			String result = "";

			//Navigate to the Relationship view
			//---------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Relationships.Value);//Navigates to the relationship view of the object

			//Select the object in the view
			//------------------------------
			if (!homePage.listView.clickItem(approvalObjName))//Clicks the object in the view
				throw new Exception("Signature Object(" + approvalObjName + ") not selected in the view.");

			//Open the popout metadata card via task pane
			//--------------------------------------------
			if (!homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value))//Clicks the option in the task pane
				throw new Exception("Properties is not clicked from the task pane for the selected object.");

			//Check if Signature reason is set as expected in the metadata card
			//-----------------------------------------------------------------
			metadataCard = new MetadataCard(driver);//Instantiates the metadata card

			if (!metadataCard.getPropertyValue(dataPool.get("SignatureReasonProperty")).equalsIgnoreCase(dataPool.get("SignatureReasonPropertyValue")))
				result = "Expected signature reason(" + dataPool.get("SignatureReasonPropertyValue") + ") is not displayed in the signature object.[Actual value: '" + metadataCard.getPropertyValue(dataPool.get("SignatureReasonProperty")) + "']. ";

			//Check if Signature meaning is set as expected in the metadata card
			//-----------------------------------------------------------------
			if (!metadataCard.getPropertyValue(dataPool.get("SignatureMeaningProperty")).equalsIgnoreCase(dataPool.get("SignatureMeaningPropertyValue")))
				result += " Expected signature meaning(" + dataPool.get("SignatureMeaningPropertyValue") + ") is not displayed in the signature object.[Actual value: '" + metadataCard.getPropertyValue(dataPool.get("SignatureMeaningProperty")) + "']. ";

			//Check if Signature identifier is set as expected in the metadata card
			//---------------------------------------------------------------------
			if (!metadataCard.getPropertyValue(dataPool.get("SignatureIdentifierProperty")).equalsIgnoreCase(dataPool.get("SignatureIdentifierPropertyValue")))
				result += " Expected signature identifier(" + dataPool.get("SignatureIdentifierPropertyValue") + ") is not displayed in the signature object.[Actual value: '" + metadataCard.getPropertyValue(dataPool.get("SignatureIdentifierProperty")) + "']. ";

			//Verification: If substitute user able to view Esign dialog when rejecting existing assignment with All must approve class
			//-------------------------------------------------------------------------------------------------------------------------
			if (result.equals(""))
				Log.pass("Test case passed. Substitute user able to view Esign dialog when rejecting existing assignment with All must approve class and Signature object is created with expected values while perform mark reject action.", driver);
			else
				Log.fail("Test case failed. Signature object is created with expected values while perform mark reject action. Additional info. : "+ result.trim(), driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try{
				MFilesDialog.closeMFilesDialog(driver);//Closes the M-Files dialog in the driver
			}
			catch(Exception e0) {Log.exception(e0, driver);}	

			Utility.quitDriver(driver);
		}//End finally

	}//End PWorkflow_32154

	/**
	 * PWorkflow_32155 : Verify if substitute user able to view Esign when completing existing assignment for Any can complete
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			description = "Verify if substitute user able to view Esign when completing existing assignment for Any can complete")
	public void PWorkflow_32155(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);//Launch login page and login

			//Step-1 : Cretae Assignment Object
			//---------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);//Opens the new Assignment object metadata card via New menu bar

			String objName = Utility.getObjectName(methodName) +"_Assignment";//Frames the object name

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card
			metadataCard.setInfo(dataPool.get("Properties"));//Sets the required property values in the metadata card
			metadataCard.setPropertyValue("Name or title", objName);//Sets the object name in the metadata card
			metadataCard.saveAndClose();//Creates the object

			Log.message("1. Object '" + objName + "' is created successfully.", driver);

			//Step-2 : Logout from the M-Files Web Access and Login as Substitute user
			//-----------------------------------------------------------------------
			if (!Utility.logoutFromWebAccess(driver))//Logs out from the M-Files Web Access
				throw new Exception("Error while logging out from M-Files Web Access.");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("SubstituteUserName"), dataPool.get("SubstituteUserPassword"), testVault);//Login as the Assigned user into the MFWA

			Log.message("2. Logged out from the MFiles Web Access and Logged in as the Substitute user : '" + dataPool.get("SubstituteUser") + "' into M-Files Web Access.", driver);

			//Step-3: Perform Mark Complete via metadatacard
			//----------------------------------------------
			if(!homePage.taskPanel.clickItem(Caption.Taskpanel.AssignedToMe.Value))
				throw new Exception("Error while navigating to Assigned To Me view");

			if (!homePage.listView.clickItem(objName))
				throw new Exception("Object(" + objName + ") is not selected in the view.");

			if (!homePage.taskPanel.clickItem(Caption.MenuItems.MarkComplete.Value))//Clicks the MarkApproved in the task pane
				throw new Exception("' " + Caption.MenuItems.MarkComplete.Value + " ' not clicked from the task pane for the selected object(" + objName + ").");

			Log.message("3.1. Mark Complete is clicked from the task pane for the E-Sign required action.", driver);

			MFilesDialog mfDialog = new MFilesDialog(driver, Caption.MenuItems.MarkComplete.Value);//Instantiates the MarkApproved dialog
			mfDialog.clickOkButton();//Clicks the OK button in the MarkApproved dialog

			Log.message("3.2. OK button is clicked in the Mark Complete dialog for the E-Sign required action.", driver);

			if (!MFilesDialog.isESignDialogExist(driver))//Waits and Checks if Electronic Signature dialog is exists or not
				throw new Exception("Electronic signature dialog is not opened while mark aprrove the assignment(" + objName + ") which requires E-Sign.");

			Log.message("3.3. E-Sign dialog is displayed after clicking OK button in the Mark Complete dialog", driver);

			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog
			mfDialog.eSign(dataPool.get("SubstituteUserName"), dataPool.get("SubstituteUserPassword"), "", "");//E-Signs the state transition

			if(!homePage.taskPanel.clickItem(Caption.MenuItems.RecentlyAccessedByMe.Value))
				throw new Exception("Error while navigating to Recently Accessed By Me view");

			if (!homePage.listView.clickItem(objName))//Clicks the object in the view
				throw new Exception("Object(" + objName + ") not selected in the view.");

			metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadatacard

			if (!metadataCard.isCompleteSelected(0))
				throw new Exception("Approval assignment(" + objName + ") is not mark completed by the user : '" + dataPool.get("SubstituteUser") + "'");

			Log.message("3.4. Assignment(" + objName + ") is Mark completed by the user : '" + dataPool.get("SubstituteUser") + "'", driver);

			//Step-4: Check if Signature object is created for the object successfully
			//------------------------------------------------------------------------
			if (!metadataCard.propertyExists(dataPool.get("SignatureProperty")))
				throw new Exception(dataPool.get("SignatureProperty") +" property is not added in the metadata card after Mark complete the object with E-Sign");

			String approvalObjName = dataPool.get("ApprovalAssignment") +": "+ dataPool.get("SubstituteUser");

			if (!metadataCard.getPropertyValue(dataPool.get("SignatureProperty")).contains(approvalObjName))
				throw new Exception("Approval assignment with name(" + approvalObjName + ") is not added in the signature property. [Actual value: '" + metadataCard.getPropertyValue(dataPool.get("SignatureProperty")) + "']");

			approvalObjName = metadataCard.getPropertyValue(dataPool.get("SignatureProperty"));

			Log.message("4. Object(" + objName + ") is mark completed and signature object property is added with expected value in the metadata card.", driver);

			//Verification: Check if signature object is created with correct signature reason and signature meaning
			//------------------------------------------------------------------------------------------------------
			String result = "";

			//Navigate to the Relationship view
			//---------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Relationships.Value);//Navigates to the relationship view of the object

			//Select the object in the view
			//------------------------------
			if (!homePage.listView.clickItem(approvalObjName))//Clicks the object in the view
				throw new Exception("Signature Object(" + approvalObjName + ") not selected in the view.");

			//Open the popout metadata card via task pane
			//--------------------------------------------
			if (!homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value))//Clicks the option in the task pane
				throw new Exception("Properties is not clicked from the task pane for the selected object.");

			//Check if Signature reason is set as expected in the metadata card
			//-----------------------------------------------------------------
			metadataCard = new MetadataCard(driver);//Instantiates the metadata card

			if (!metadataCard.getPropertyValue(dataPool.get("SignatureReasonProperty")).equalsIgnoreCase(dataPool.get("SignatureReasonPropertyValue")))
				result = "Expected signature reason(" + dataPool.get("SignatureReasonPropertyValue") + ") is not displayed in the signature object.[Actual value: '" + metadataCard.getPropertyValue(dataPool.get("SignatureReasonProperty")) + "']. ";

			//Check if Signature meaning is set as expected in the metadata card
			//-------------------------------------------------------------------
			if (!metadataCard.getPropertyValue(dataPool.get("SignatureMeaningProperty")).equalsIgnoreCase(dataPool.get("SignatureMeaningPropertyValue")))
				result += " Expected signature meaning(" + dataPool.get("SignatureMeaningPropertyValue") + ") is not displayed in the signature object.[Actual value: '" + metadataCard.getPropertyValue(dataPool.get("SignatureMeaningProperty")) + "']. ";

			//Check if Signature identifier is set as expected in the metadata card
			//---------------------------------------------------------------------
			if (!metadataCard.getPropertyValue(dataPool.get("SignatureIdentifierProperty")).equalsIgnoreCase(dataPool.get("SignatureIdentifierPropertyValue")))
				result += " Expected signature identifier(" + dataPool.get("SignatureIdentifierPropertyValue") + ") is not displayed in the signature object.[Actual value: '" + metadataCard.getPropertyValue(dataPool.get("SignatureIdentifierProperty")) + "']. ";

			//Verification: If substitute user able to view Esign when completing existing assignment for Any can complete
			//------------------------------------------------------------------------------------------------------------
			if (result.equals(""))
				Log.pass("Test case passed. Substitute user able to view Esign dialog when completing existing assignment with Any can complete and Signature object is created with expected values while perform mark complete action.", driver);
			else
				Log.fail("Test case failed. Signature object is created with expected values while perform mark complete action. Additional info. : "+ result.trim(), driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try{
				MFilesDialog.closeMFilesDialog(driver);//Closes the M-Files dialog in the driver
			}
			catch(Exception e0) {Log.exception(e0, driver);}	

			Utility.quitDriver(driver);
		}//End finally

	}//End PWorkflow_32155

	/**
	 * PWorkflow_32799 : Verify if substitute user able to view Esign when completing existing assignment for All must complete
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			description = "Verify if substitute user able to view Esign when completing existing assignment for All must complete")
	public void PWorkflow_32799(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);//Launch login page and login

			//Step-1 : Cretae Assignment Object
			//---------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);//Opens the new Assignment object metadata card via New menu bar

			String objName = Utility.getObjectName(methodName) +"_Assignment";//Frames the object name

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card
			metadataCard.setInfo(dataPool.get("Properties"));//Sets the required property values in the metadata card
			metadataCard.setPropertyValue("Name or title", objName);//Sets the object name in the metadata card
			metadataCard.saveAndClose();//Creates the object

			Log.message("1. Object '" + objName + "' is created successfully.", driver);

			//Step-2 : Logout from the M-Files Web Access and Login as Substitute user
			//-----------------------------------------------------------------------
			if (!Utility.logoutFromWebAccess(driver))//Logs out from the M-Files Web Access
				throw new Exception("Error while logging out from M-Files Web Access.");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("SubstituteUserName"), dataPool.get("SubstituteUserPassword"), testVault);//Login as the Assigned user into the MFWA

			Log.message("2. Logged out from the MFiles Web Access and Logged in as the Substitute user : '" + dataPool.get("SubstituteUser") + "' into M-Files Web Access.", driver);

			//Step-3: Perform Mark Complete via metadatacard
			//----------------------------------------------
			if(!homePage.taskPanel.clickItem(Caption.Taskpanel.AssignedToMe.Value))
				throw new Exception("Error while navigating to Assigned To Me view");

			if (!homePage.listView.clickItem(objName))
				throw new Exception("Object(" + objName + ") is not selected in the view.");

			if (!homePage.taskPanel.clickItem(Caption.MenuItems.MarkComplete.Value))//Clicks the MarkApproved in the task pane
				throw new Exception("' " + Caption.MenuItems.MarkComplete.Value + " ' not clicked from the task pane for the selected object(" + objName + ").");

			Log.message("3.1. Mark Complete is clicked from the task pane for the E-Sign required action.", driver);

			MFilesDialog mfDialog = new MFilesDialog(driver, Caption.MenuItems.MarkComplete.Value);//Instantiates the MarkApproved dialog
			mfDialog.clickOkButton();//Clicks the OK button in the MarkApproved dialog

			Log.message("3.2. OK button is clicked in the Mark Complete dialog for the E-Sign required action.", driver);

			if (!MFilesDialog.isESignDialogExist(driver))//Waits and Checks if Electronic Signature dialog is exists or not
				throw new Exception("Electronic signature dialog is not opened while mark aprrove the assignment(" + objName + ") which requires E-Sign.");

			Log.message("3.3. E-Sign dialog is displayed after clicking OK button in the Mark Complete dialog", driver);

			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog
			mfDialog.eSign(dataPool.get("SubstituteUserName"), dataPool.get("SubstituteUserPassword"), "", "");//E-Signs the state transition

			if(!homePage.taskPanel.clickItem(Caption.MenuItems.RecentlyAccessedByMe.Value))
				throw new Exception("Error while navigating to Recently Accessed By Me view");

			if (!homePage.listView.clickItem(objName))//Clicks the object in the view
				throw new Exception("Object(" + objName + ") not selected in the view.");

			metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadatacard

			if (!metadataCard.isCompleteSelected(0))
				throw new Exception("Approval assignment(" + objName + ") is not mark completed by the user : '" + dataPool.get("SubstituteUser") + "'");

			Log.message("3.4. Assignment(" + objName + ") is Mark completed by the user : '" + dataPool.get("SubstituteUser") + "'", driver);

			//Step-4: Check if Signature object is created for the object successfully
			//------------------------------------------------------------------------
			if (!metadataCard.propertyExists(dataPool.get("SignatureProperty")))
				throw new Exception(dataPool.get("SignatureProperty") +" property is not added in the metadata card after Mark complete the object with E-Sign");

			String approvalObjName = dataPool.get("ApprovalAssignment") +": "+ dataPool.get("SubstituteUser");

			if (!metadataCard.getPropertyValue(dataPool.get("SignatureProperty")).contains(approvalObjName))
				throw new Exception("Approval assignment with name(" + approvalObjName + ") is not added in the signature property. [Actual value: '" + metadataCard.getPropertyValue(dataPool.get("SignatureProperty")) + "']");

			approvalObjName = metadataCard.getPropertyValue(dataPool.get("SignatureProperty"));

			Log.message("4. Object(" + objName + ") is mark completed and signature object property is added with expected value in the metadata card.", driver);

			//Verification: Check if signature object is created with correct signature reason and signature meaning
			//------------------------------------------------------------------------------------------------------
			String result = "";

			//Navigate to the Relationship view
			//---------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Relationships.Value);//Navigates to the relationship view of the object

			//Select the object in the view
			//------------------------------
			if (!homePage.listView.clickItem(approvalObjName))//Clicks the object in the view
				throw new Exception("Signature Object(" + approvalObjName + ") not selected in the view.");

			//Open the popout metadata card via task pane
			//--------------------------------------------
			if (!homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value))//Clicks the option in the task pane
				throw new Exception("Properties is not clicked from the task pane for the selected object.");

			//Check if Signature reason is set as expected in the metadata card
			//-----------------------------------------------------------------
			metadataCard = new MetadataCard(driver);//Instantiates the metadata card

			if (!metadataCard.getPropertyValue(dataPool.get("SignatureReasonProperty")).equalsIgnoreCase(dataPool.get("SignatureReasonPropertyValue")))
				result = "Expected signature reason(" + dataPool.get("SignatureReasonPropertyValue") + ") is not displayed in the signature object.[Actual value: '" + metadataCard.getPropertyValue(dataPool.get("SignatureReasonProperty")) + "']. ";

			//Check if Signature meaning is set as expected in the metadata card
			//-----------------------------------------------------------------
			if (!metadataCard.getPropertyValue(dataPool.get("SignatureMeaningProperty")).equalsIgnoreCase(dataPool.get("SignatureMeaningPropertyValue")))
				result += " Expected signature meaning(" + dataPool.get("SignatureMeaningPropertyValue") + ") is not displayed in the signature object.[Actual value: '" + metadataCard.getPropertyValue(dataPool.get("SignatureMeaningProperty")) + "']. ";

			//Check if Signature identifier is set as expected in the metadata card
			//-----------------------------------------------------------------
			if (!metadataCard.getPropertyValue(dataPool.get("SignatureIdentifierProperty")).equalsIgnoreCase(dataPool.get("SignatureIdentifierPropertyValue")))
				result += " Expected signature identifier(" + dataPool.get("SignatureIdentifierPropertyValue") + ") is not displayed in the signature object.[Actual value: '" + metadataCard.getPropertyValue(dataPool.get("SignatureIdentifierProperty")) + "']. ";

			if (result.equals(""))
				Log.pass("Test case passed. Substitute user able to view Esign dialog when completing existing assignment with All must complete and Signature object is created with expected values while perform mark complete action.", driver);
			else
				Log.fail("Test case failed. Signature object is created with expected values while perform mark complete action. Additional info. : "+ result.trim(), driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try{
				MFilesDialog.closeMFilesDialog(driver);//Closes the M-Files dialog in the driver
			}
			catch(Exception e0) {Log.exception(e0, driver);}	

			Utility.quitDriver(driver);
		}//End finally

	}//End PWorkflow_32799

	/**
	 * PWorkflow_32800 : Verify if Substitute user able to complete the assignment.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			description = "Verify if Substitute user able to complete the assignment.")
	public void PWorkflow_32800(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);//Launch login page and login

			//Step-1 : Cretae Assignment Object
			//---------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);//Opens the new Assignment object metadata card via New menu bar

			String objName = Utility.getObjectName(methodName) +"_Assignment";//Frames the object name

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card
			metadataCard.setInfo(dataPool.get("Properties"));//Sets the required property values in the metadata card
			metadataCard.setPropertyValue("Name or title", objName);//Sets the object name in the metadata card
			metadataCard.saveAndClose();//Creates the object

			Log.message("1. Object '" + objName + "' is created successfully.", driver);

			//Step-2 : Logout from the M-Files Web Access and Login as Substitute user
			//-----------------------------------------------------------------------
			if (!Utility.logoutFromWebAccess(driver))//Logs out from the M-Files Web Access
				throw new Exception("Error while logging out from M-Files Web Access.");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("SubstituteUserName"), dataPool.get("SubstituteUserPassword"), testVault);//Login as the Assigned user into the MFWA

			Log.message("2. Logged out from the MFiles Web Access and Logged in as the Substitute user : '" + dataPool.get("SubstituteUser") + "' into M-Files Web Access.", driver);

			//Step-3: Perform Mark Complete via metadatacard
			//----------------------------------------------
			if(!homePage.taskPanel.clickItem(Caption.Taskpanel.AssignedToMe.Value))
				throw new Exception("Error while navigating to Assigned To Me view");

			if (!homePage.listView.clickItem(objName))
				throw new Exception("Object(" + objName + ") is not selected in the view.");

			if (!homePage.taskPanel.clickItem(Caption.MenuItems.MarkComplete.Value))//Clicks the MarkApproved in the task pane
				throw new Exception("' " + Caption.MenuItems.MarkComplete.Value + " ' not clicked from the task pane for the selected object(" + objName + ").");

			Log.message("3.1. Mark Complete is clicked from the task pane for the E-Sign required action.", driver);

			MFilesDialog mfDialog = new MFilesDialog(driver, Caption.MenuItems.MarkComplete.Value);//Instantiates the MarkApproved dialog
			mfDialog.clickOkButton();//Clicks the OK button in the MarkApproved dialog

			Log.message("3.2. OK button is clicked in the Mark Complete dialog for the E-Sign required action.", driver);

			if (!MFilesDialog.isESignDialogExist(driver))//Waits and Checks if Electronic Signature dialog is exists or not
				throw new Exception("Electronic signature dialog is not opened while mark aprrove the assignment(" + objName + ") which requires E-Sign.");

			Log.message("3.3. E-Sign dialog is displayed after clicking OK button in the Mark Complete dialog", driver);

			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog
			mfDialog.eSign(dataPool.get("SubstituteUserName"), dataPool.get("SubstituteUserPassword"), "", "");//E-Signs the state transition

			Log.message("3.4. Valid credentials entered and clicked sign button.", driver);

			//Verification: Check if warning dialog is displayed with expected message
			//------------------------------------------------------------------------
			String result = "";

			//Navigate to the RecentlyAccessedByMe view
			//-----------------------------------------
			if (!homePage.taskPanel.clickItem(Caption.MenuItems.RecentlyAccessedByMe.Value))//Clicks the option from task pane
				throw new Exception("Error while navigating to the Recently accessed by me view.");

			//Select the object in the view
			//------------------------------
			if (!homePage.listView.clickItem(objName))
				throw new Exception("Object(" + objName + ") is not selected in the view.");

			//Check if Complete is selected in the metadata card
			//--------------------------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (!metadataCard.isCompleteSelected(0))//Checks if complete is selected in the metadata card for the selected object
				result = "Assignment(" + objName + ") is not mark completed by the substitute user.";

			//Verification:If Substitute user able to complete the assignment
			//---------------------------------------------------------------
			if (result.equals(""))
				Log.pass("Test case passed. Substitute user completed the assignment successfully.", driver);
			else
				Log.fail("Test case failed. Substitute user not completed the assignment successfully. Additional info. : "+ result.trim(), driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try{
				MFilesDialog.closeMFilesDialog(driver);//Closes the M-Files dialog in the driver
			}
			catch(Exception e0) {Log.exception(e0, driver);}	

			Utility.quitDriver(driver);
		}//End finally

	}//End PWorkflow_32800

	/**
	 * PWorkflow_32809 : Verify the e-sign process for invalid substitute user credentials
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			description = "Verify the e-sign process for invalid substitute user credentials")
	public void PWorkflow_32809(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);//Launch login page and login

			//Step-1 : Cretae Assignment Object
			//---------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);//Opens the new Assignment object metadata card via New menu bar

			String objName = Utility.getObjectName(methodName) +"_Assignment";//Frames the object name

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card
			metadataCard.setInfo(dataPool.get("Properties"));//Sets the required property values in the metadata card
			metadataCard.setPropertyValue("Name or title", objName);//Sets the object name in the metadata card
			metadataCard.saveAndClose();//Creates the object

			Log.message("1. Object '" + objName + "' is created successfully.", driver);

			//Step-2 : Logout from the M-Files Web Access and Login as Substitute user
			//-----------------------------------------------------------------------
			if (!Utility.logoutFromWebAccess(driver))//Logs out from the M-Files Web Access
				throw new Exception("Error while logging out from M-Files Web Access.");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("SubstituteUserName"), dataPool.get("SubstituteUserPassword"), testVault);//Login as the Assigned user into the MFWA

			Log.message("2. Logged out from the MFiles Web Access and Logged in as the Substitute user : '" + dataPool.get("SubstituteUser") + "' into M-Files Web Access.", driver);

			//Step-3: Perform Mark Complete via metadatacard
			//----------------------------------------------
			if(!homePage.taskPanel.clickItem(Caption.Taskpanel.AssignedToMe.Value))
				throw new Exception("Error while navigating to Assigned To Me view");

			if (!homePage.listView.clickItem(objName))
				throw new Exception("Object(" + objName + ") is not selected in the view.");

			if (!homePage.taskPanel.clickItem(Caption.MenuItems.MarkComplete.Value))//Clicks the MarkApproved in the task pane
				throw new Exception("' " + Caption.MenuItems.MarkComplete.Value + " ' not clicked from the task pane for the selected object(" + objName + ").");

			Log.message("3.1. Mark Complete is clicked from the task pane for the E-Sign required action.", driver);

			MFilesDialog mfDialog = new MFilesDialog(driver, Caption.MenuItems.MarkComplete.Value);//Instantiates the MarkApproved dialog
			mfDialog.clickOkButton();//Clicks the OK button in the MarkApproved dialog

			Log.message("3.2. OK button is clicked in the Mark Complete dialog for the E-Sign required action.", driver);

			if (!MFilesDialog.isESignDialogExist(driver))//Waits and Checks if Electronic Signature dialog is exists or not
				throw new Exception("Electronic signature dialog is not opened while mark aprrove the assignment(" + objName + ") which requires E-Sign.");

			Log.message("3.3. E-Sign dialog is displayed after clicking OK button in the Mark Complete dialog", driver);

			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog
			mfDialog.eSign(dataPool.get("SubstituteUserName1"), dataPool.get("SubstituteUserPassword1"), "", "");//E-Signs the state transition

			Log.message("3.4. Invalid credentials[Username: '" + dataPool.get("SubstituteUserName1") + "' and password: '" + dataPool.get("SubstituteUserPassword1") + "'] entered and clicked sign button.", driver);

			//Verification: Check if warning dialog is displayed with expected message
			//------------------------------------------------------------------------
			String result = "";

			//Checks if dialog is exist in the view
			//--------------------------------------
			if (!MFilesDialog.exists(driver, "M-Files Web"))
				throw new Exception("Warning dialog is not displayed while signing the object with invalid substitute user credentials(Username: '" + dataPool.get("SubstituteUserName1") + "', Password: '" + dataPool.get("SubstituteUserPassword1") + "')");

			//Check if expected warning message is displayed in the dialog
			//------------------------------------------------------------
			mfDialog = new MFilesDialog(driver, "M-Files Web");//Instantiates the M-Files Dialog

			if (!mfDialog.getMessage().equalsIgnoreCase(dataPool.get("ExpectedWarningMessage")))
				result = "Expected warning message(" + dataPool.get("ExpectedWarningMessage") + ") is not displayed while signing the object with invalid substitute user credentials(Username: '" + dataPool.get("SubstituteUserName1") + "', Password: '" + dataPool.get("SubstituteUserPassword1") + "'). [Actual message: '" + mfDialog.getMessage() + "']";

			//Verification: E-sign process for invalid substitute user credentials
			//---------------------------------------------------------------------
			if (result.equals(""))
				Log.pass("Test case passed. Warning dialog is displayed as expected while performing e-sign with invalid substitute credentials.", driver);
			else
				Log.fail("Test case failed. Warning dialog is not displayed as expected while performing e-sign with invalid substitute credentials. Additional info. : "+ result.trim(), driver);

			mfDialog.clickOkButton();//Clicks ok button in the warning dialog
			Utils.fluentWait(driver);

			if (MFilesDialog.exists(driver, Caption.MFilesDialog.ElectronicSignature.Value))
			{
				mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog
				mfDialog.close();//Closes the MFiles E-Sign Dialog
			}

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try{
				MFilesDialog.closeMFilesDialog(driver);//Closes the M-Files dialog in the driver
			}
			catch(Exception e0) {Log.exception(e0, driver);}	

			Utility.quitDriver(driver);
		}//End finally

	}//End PWorkflow_32809

	/**
	 * PWorkflow_32810 : Verify if substitute user able to perform 2 consecutive signatures for a document object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			description = "Verify if substitute user able to perform 2 consecutive signatures for a document object")
	public void PWorkflow_32810(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);//Launch login page and login

			//Step-1 : Cretae Customer Object
			//---------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Document.Value);//Opens the new Document object metadata card via New menu bar

			String objName = Utility.getObjectName(methodName) +"_Document";//Frames the object name

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card
			metadataCard.setTemplateUsingClass(dataPool.get("Class"));//Selects the template
			metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card
			metadataCard.setPropertyValue("Name or title", objName);//Sets the object name in the metadata card
			metadataCard.setCheckInImmediately(true);//Checks the Checkin immediately check box
			metadataCard.saveAndClose();//Creates the object

			Log.message("1. Document Object '" + objName + "' is created successfully.", driver);

			//Step-2: Sets the workflow in the metadata card
			//----------------------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadata card
			metadataCard.setWorkflow(dataPool.get("Workflow"));//Sets the workflow in the metadata card
			metadataCard.saveAndClose();//Saves the changes

			Log.message("2. '" + dataPool.get("Workflow") + "' workflow and '" + dataPool.get("WorkflowState") + "' workflow state is set in the metadata card of the object.", driver); 

			//Step-3 : Logout from the M-Files Web Access and Login as Substitute user
			//-----------------------------------------------------------------------
			if (!Utility.logoutFromWebAccess(driver))//Logs out from the M-Files Web Access
				throw new Exception("Error while logging out from M-Files Web Access.");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("SubstituteUserName"), dataPool.get("SubstituteUserPassword"), testVault);//Login as the Assigned user into the MFWA

			Log.message("3. Logged out from the MFiles Web Access and Logged in as the Substitute user : '" + dataPool.get("SubstituteUser") + "' into M-Files Web Access.", driver);

			//Step-4: Check if Seperate assignment is exist for the substitute user
			//----------------------------------------------------------------------
			String seperateAssignment = objName + " - " + dataPool.get("ApprovalAssignment");

			if(!homePage.taskPanel.clickItem(Caption.Taskpanel.AssignedToMe.Value))
				throw new Exception("Error while navigating to Assigned To Me view");

			if (!homePage.listView.clickItem(seperateAssignment))
				throw new Exception("Object(" + seperateAssignment + ") is not selected in the view for the substitute user.");

			Log.message("4. '" + seperateAssignment + "' is created successfully and exist in the assigned to me view for the substitute user.", driver);

			//Step-5: Perform Mark complete with E-Sign
			//------------------------------------------
			if (!homePage.taskPanel.clickItem(Caption.MenuItems.MarkComplete.Value))//Clicks the MarkApproved in the task pane
				throw new Exception("' " + Caption.MenuItems.MarkComplete.Value + " ' not clicked from the task pane for the selected object(" + objName + ").");

			Log.message("5.1. Mark Complete is clicked from the task pane for the E-Sign required action.", driver);

			MFilesDialog mfDialog = new MFilesDialog(driver, Caption.MenuItems.MarkComplete.Value);//Instantiates the MarkApproved dialog
			mfDialog.clickOkButton();//Clicks the OK button in the MarkApproved dialog

			Log.message("5.2. OK button is clicked in the Mark Complete dialog for the E-Sign required action.", driver);

			if (!MFilesDialog.isESignDialogExist(driver))//Waits and Checks if Electronic Signature dialog is exists or not
				throw new Exception("Electronic signature dialog is not opened while mark aprrove the assignment(" + objName + ") which requires E-Sign.");

			Log.message("5.3. E-Sign dialog is displayed after clicking OK button in the Mark Complete dialog", driver);

			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog
			mfDialog.eSign(dataPool.get("SubstituteUserName"), dataPool.get("SubstituteUserPassword"), "", "");//E-Signs the state transition

			Log.message("5.4. Performed E-Sign operation using Substitute user for Mark complete operation.", driver);

			if (!MFilesDialog.isESignDialogExist(driver))//Waits and Checks if Electronic Signature dialog is exists or not
				throw new Exception("Electronic signature dialog is not opened of automatic state transition('" + dataPool.get("WorkflowState") + "' to '" + dataPool.get("AutomaticWorkflowState") + "') while mark complete of approval assignment.");

			Log.message("5.5. E-Sign dialog is displayed for automatic state transition('" + dataPool.get("WorkflowState") + "' to '" + dataPool.get("AutomaticWorkflowState") + "') after perform the Mark complete of approval assignment.", driver);

			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog
			mfDialog.eSign(dataPool.get("SubstituteUserName"), dataPool.get("SubstituteUserPassword"), "", "");//E-Signs the state transition

			Log.message("5.6.  Performed E-Sign operation using Substitute user for Automatic state transition operation.", driver);

			//Verification: Check if objects E-Signed successfully
			//----------------------------------------------------
			String result = "";

			//Navigate to the RecentlyAccessedByMe view
			//-----------------------------------------
			if (!homePage.taskPanel.clickItem(Caption.MenuItems.RecentlyAccessedByMe.Value))//Clicks the option from task pane
				throw new Exception("Error while navigating to the Recently accessed by me view.");

			//Select the object in the view
			//------------------------------
			if (!homePage.listView.clickItem(seperateAssignment))
				throw new Exception("Object(" + seperateAssignment + ") is not selected in the view.");

			//Check if Complete is selected in the metadatacard
			//-------------------------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (!metadataCard.isCompleteSelected(0))//Checks if complete is selected in the metadata card for the selected object
				result = "Approval assignment(" + seperateAssignment + ") is not mark completed by the substitute user.";

			//Navigate to the Relationship view
			//---------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Relationships.Value);//Clicks the option from menu bar

			//Select the object in the view
			//-----------------------------
			if (!homePage.listView.clickItem(objName+".doc"))
				throw new Exception("Object(" + objName + ") is not selected in the view.");

			//Open the popout metadata card via task pane
			//--------------------------------------------
			if (!homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value))//Clicks the option from task pane
				throw new Exception("Properties is not clicked from the task pane.");

			//Check if expected workflow state is set in the metadatacard for the selected object
			//------------------------------------------------------------------------------------
			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			if (!metadataCard.getWorkflowState().equalsIgnoreCase(dataPool.get("AutomaticWorkflowState")))//Checks if automatic workflow state is set
				result += "Automatic workflow state(" + dataPool.get("AutomaticWorkflowState") + ") is not set in the document object(" + objName + ").";

			//Verification: If substitute user able to perform 2 consecutive signatures for a document object
			//-----------------------------------------------------------------------------------------------
			if (result.equals(""))
				Log.pass("Test case passed. Substitute user performed 2 consecutive signatures successfully for a document object.", driver);
			else
				Log.fail("Test case failed. Substitute user not performed 2 consecutive signatures successfully for a document object.. Additional info. : "+ result.trim(), driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try{
				MFilesDialog.closeMFilesDialog(driver);//Closes the M-Files dialog in the driver
			}
			catch(Exception e0) {Log.exception(e0, driver);}	

			Utility.quitDriver(driver);
		}//End finally

	}//End PWorkflow_32810

} 
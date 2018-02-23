package MFClient.Tests.ElectronicSignature;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.Utils;
import genericLibrary.WebDriverUtils;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
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
public class BasicESign {

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
				productVersion = "M-Files " +  xmlParameters.getParameter("productVersion").trim()  + " - " +  xmlParameters.getParameter("driverType").toUpperCase().trim() + xmlParameters.getParameter("driverVersion").trim();
			else
				productVersion = "M-Files " +  xmlParameters.getParameter("productVersion").trim()  + " - " +  xmlParameters.getParameter("driverType").toUpperCase().trim();

			Utility.restoreTestVault();
			Utility.configureUsers(xlTestDataWorkBook);
			try {Utility.configureUsers(testVault, windowsUserName, windowsUserFullName, "windows", "named", "none", domainName, "admin", "FullControl", "internal");}catch(Exception e0) {Log.exception(e0, driver);}

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
	 * ESign_811 : Signing with a custom object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"BasicESign"},
			description = "Signing with a custom object")
	public void ESign_811(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//-------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launches driver and logging in

			//Step-1 : Create two new objects in the view
			//-----------------------------------------------------------------------------
			if (!homePage.taskPanel.clickItem(Caption.ObjecTypes.Customer.Value))//Opens the new Customer object metadata card
				throw new Exception("'" + Caption.ObjecTypes.Customer.Value + "'  is not clicked from task pane.");

			String objName = Utility.getObjectName(methodName) + "_Customer";//Frames the object name

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card
			metadataCard.setPropertyValue("Customer name", objName);//Sets the object name in the metadata card
			metadataCard.saveAndClose();//Creats the object

			Log.message("1. Custom object('" + objName + "') successfully created in the view.", driver);

			//Step-2 : Sets the ESign worklfow in the metadata card
			//-----------------------------------------------------
			if (!homePage.listView.clickItem(objName))//Checks if object is exists in the list view
				throw new Exception("Object '" + objName + "' is not selected in the view");

			metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadatacard
			metadataCard.setWorkflow(dataPool.get("Workflow"));//Sets the Worklfow in the right pane metadata card
			metadataCard.saveAndClose();//Saves the changes in rightpane metadata card

			Log.message("2. E-Sign Workflow (" + dataPool.get("Workflow") + ") is set and saved the changes in right pane metadata card for the newly created custom object.", driver);

			//Step-3: Perform E-Sgin required state transition for the newly created objects via Task pane
			//--------------------------------------------------------------------------------------------
			if (!homePage.listView.clickItem(objName))//Selects the object in the view
				throw new Exception("Object '" + objName + "' is not selected in the view");

			if (!homePage.taskPanel.clickItem(dataPool.get("ESignState")))//Clicks the workflow state in the state transition section in the task panel
				throw new Exception("Required E-Sign Workflow is not set for the selected object in the view.");

			if (!MFilesDialog.exists(driver, Caption.MenuItems.Workflow.Value))//Checks if workflow state transition dialog is exist or not
				throw new Exception("State transition workflow dialog is not opened while click on the '" + dataPool.get("ESignState") + "' workflow state from task panel.");

			Log.message("3.1. State transition workflow dialog is opened while click on the '" + dataPool.get("ESignState") + "' workflow state from task panel.", driver);

			MFilesDialog mfDialog = new MFilesDialog(driver, Caption.MenuItems.Workflow.Value);//Instantiates the Workflow M-Files dialog in the view
			mfDialog.clickOkButton();//Clicks OK button in the Workflow dialog for the E-Sign required State transition

			if (!MFilesDialog.isESignDialogExist(driver))//Waits and Checks if Electronic Signature dialog is exists or not
				throw new Exception("Electronic signature dialog is not opened while performing state transition to the state '" + dataPool.get("ESignState") + "'.");

			Log.message("3.2. Electronic signature dialog is opened while performing state transition to the state '" + dataPool.get("ESignState") + "'.", driver);

			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog
			mfDialog.eSign(userName, password, "", userFullName);//E-Signs the state transition

			Log.message("3.3. Performed E-Sign operation for the newly created custom object in the view", driver);

			//Checks if required state is set in the metadata card
			//-----------------------------------------------------
			String result = "";

			metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadata card

			if (!metadataCard.getWorkflowState().equalsIgnoreCase(dataPool.get("ESignState")))//Checks if required state is set in the metadata card
				result += "Object(" + objName + ") is not E-Signed successfully. Expected workflow state(" + dataPool.get("ESignState") + ") is not set in the metadatacard(Actual : " + metadataCard.getWorkflowState() + ").";		

			//Checks if signature property is added into the metadata card
			//------------------------------------------------------------
			if (!metadataCard.propertyExists(dataPool.get("Property")))//Checks if signature property is added into the metadata card
				result += "Object(" + objName + ") is not E-Signed successfully. " + dataPool.get("Property") + " is not exist in the metadata card.";

			//Verification if Object E-Signed correctly
			//-------------------------------------------
			if (result.equals(""))
				Log.pass("Test case passed. Custom object E-Signed successfully.", driver);
			else
				Log.fail("Test case failed. Custom object is not E-Signed successfully.[Additional info. : " + result + "]", driver);

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

	}//End ESign_811

	/**
	 * ESign_812 : Signing that creates another object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"BasicESign"},
			description = "Signing that creates another object")
	public void ESign_812(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);//Logging into the vault

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), "");//Navigate to the specific view

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))//Selects the object in the view
				throw new Exception("'" + dataPool.get("ObjectName")  + "' is not selected in the view.");

			Log.message("1. Navigated to " +  viewtonavigate  + " and selected the object \"" +  dataPool.get("ObjectName") +  "\" in the view.", driver);

			//Step-2 : Sets the ESign worklfow in the metadata card
			//-----------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadatacard
			metadataCard.setWorkflow(dataPool.get("Workflow"));//Sets the Worklfow in the right pane metadata card
			metadataCard.saveAndClose();//Saves the changes in rightpane metadata card

			Log.message("2. E-Sign Workflow (" + dataPool.get("Workflow") + ") is set and saved the changes in right pane metadata card.", driver);

			//Step-3: Perform E-Sgin required state transition via Task pane
			//--------------------------------------------------------------
			if (!homePage.taskPanel.clickItem(dataPool.get("ESignState")))//Clicks the workflow state in the state transition section in the task panel
				throw new Exception("Required E-Sign Workflow is not set for the selected object in the view.");

			if (!MFilesDialog.exists(driver, Caption.MenuItems.Workflow.Value))//Checks if workflow state transition dialog is exist or not
				throw new Exception("State transition workflow dialog is not opened while click on the '" + dataPool.get("ESignState") + "' workflow state from task panel.");

			Log.message("3. State transition dialog is opened via task panel for the E-Sign required state(" + dataPool.get("ESignState") + ") transition.", driver);

			//Step-4: Clicks OK button in the State transition workflow dialog
			//----------------------------------------------------------------
			MFilesDialog mfDialog = new MFilesDialog(driver, Caption.MenuItems.Workflow.Value);//Instantiates the Workflow M-Files dialog in the view
			mfDialog.clickOkButton();//Clicks OK button in the Workflow dialog for the E-Sign required State transition

			if (!MFilesDialog.isESignDialogExist(driver))//Checks if Electronic Signature dialog is exists or not
				throw new Exception("Electronic signature dialog is not opened while performing state transition to the state '" + dataPool.get("ESignState") + "'.");

			Log.message("4. OK button is clicked in the State transition dialog for the E-Sign required state trainsition.", driver);
			//Step-5: E-Sign the object in M-Files Dialog
			//--------------------------------------------------
			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog

			mfDialog.eSign(userName, password, "", "");//E-Signs the state transition

			metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadata card

			if (!metadataCard.getWorkflowState().equalsIgnoreCase(dataPool.get("ESignState")))
				throw new Exception("Object is not E-Signed Successfully.[Additional info. : Workflow state '" + dataPool.get("ESignState") + "' is not set in the ESigned object]");

			String signatureName = metadataCard.getPropertyValue("Signature");//Gets the signature object name

			Log.message("5. State transition is E-Signed by the User: '" + userName + "'. in E-Sign dialog", driver);

			//Verification if signature object is created for the E-Signed object
			//-------------------------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Relationships.Value);//Opens the relationship view of the current object

			if (homePage.listView.isItemExists(signatureName))
				Log.pass("Test case passed. Signature object(" + signatureName + ") created and exist in the relationship view of the E-Signed object(" + dataPool.get("ObjectName") + ").", driver);
			else
				Log.fail("Test case failed. Signature object(" + signatureName + ") is not created and not exist in the relationship view of the E-Signed object(" + dataPool.get("ObjectName") + ").", driver);

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

	}//End ESign_812

	/**
	 * ESign_813 : Signing with Windows user
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"BasicESign", "WindowsUser"},
			description = "Signing with Windows user")
	public void ESign_813(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, windowsUserName, windowsPassword, testVault);//Logging into the vault

			Log.message("Pre-requsite : Browser is opened and logged into MFWA as Windows user. ( User Name : " +  dataPool.get("UserName")  + "; Vault : " +  testVault  + ")", driver);

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), "");//Navigate to the specific view

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))//Selects the object in the view
				throw new Exception("'" + dataPool.get("ObjectName")  + "' is not selected in the view.");

			Log.message("1. Navigated to " +  viewtonavigate  + " and selected the object \"" +  dataPool.get("ObjectName") +  "\" in the view.", driver);

			//Step-2 : Sets the ESign worklfow in the metadata card
			//-----------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadatacard
			metadataCard.setWorkflow(dataPool.get("Workflow"));//Sets the Worklfow in the right pane metadata card
			metadataCard.saveAndClose();//Saves the changes in rightpane metadata card

			Log.message("2. E-Sign Workflow (" + dataPool.get("Workflow") + ") is set and saved the changes in right pane metadata card.", driver);

			//Step-3: Perform E-Sgin required state transition via Task pane
			//--------------------------------------------------------------
			if (!homePage.taskPanel.clickItem(dataPool.get("ESignState")))//Clicks the workflow state in the state transition section in the task panel
				throw new Exception("Required E-Sign Workflow is not set for the selected object in the view.");

			if (!MFilesDialog.exists(driver, Caption.MenuItems.Workflow.Value))//Checks if workflow state transition dialog is exist or not
				throw new Exception("State transition workflow dialog is not opened while click on the '" + dataPool.get("ESignState") + "' workflow state from task panel.");

			Log.message("3. State transition dialog is opened via task panel for the E-Sign required state(" + dataPool.get("ESignState") + ") transition.", driver);

			//Step-4: Clicks OK button in the State transition workflow dialog
			//----------------------------------------------------------------
			MFilesDialog mfDialog = new MFilesDialog(driver, Caption.MenuItems.Workflow.Value);//Instantiates the Workflow M-Files dialog in the view
			mfDialog.clickOkButton();//Clicks OK button in the Workflow dialog for the E-Sign required State transition

			if (!MFilesDialog.isESignDialogExist(driver))//Checks if Electronic Signature dialog is exists or not
				throw new Exception("Electronic signature dialog is not opened while performing state transition to the state '" + dataPool.get("ESignState") + "'.");

			Log.message("4. OK button is clicked in the State transition dialog for the E-Sign required state trainsition.", driver);
			//Step-5: E-Sign the object in M-Files Dialog
			//--------------------------------------------------
			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog

			mfDialog.eSign(windowsUserName, windowsPassword, domainName, windowsUserFullName);//E-Signs the state transition

			Log.message("5. State transition is E-Signed by the Windows User: '" + windowsUserName + "'. in E-Sign dialog", driver);

			//Step-6: Gets the Property value from the metadatacard
			//--------------------------------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadata card

			if (!metadataCard.getWorkflowState().equalsIgnoreCase(dataPool.get("ESignState")))
				throw new Exception("Object is not E-Signed Successfully.[Additional info. : Workflow state '" + dataPool.get("ESignState") + "' is not set in the ESigned object]");

			if (!metadataCard.propertyExists(dataPool.get("Property")))
				throw new Exception(dataPool.get("Property")  + " is not added in the E-Signed object");

			String actualTimeStamp = metadataCard.getPropertyValue(dataPool.get("Property"));

			Log.message("6. Property(" + dataPool.get("Property") + ") value is get from the metadatacard.", driver);

			//Verification if object is e-signed successfully for the windows user
			//-----------------------------------------------------------------
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
			Date date = new Date();
			String currentDate = dateFormat.format(date);
			String result = "";

			//Check if Expected date is present in the signature property
			//-----------------------------------------------------------
			if (!actualTimeStamp.contains(currentDate))
				result = "Expected date('" + currentDate + "') is not present in the signature property.";

			//Check if Expected Username is present in the signature property
			//---------------------------------------------------------------
			if (!actualTimeStamp.toLowerCase().contains((windowsUserFullName  + " (" + domainName + "\\" + windowsUserName + ")").toLowerCase()))
				result += "Expected user signature('" + windowsUserFullName + " (" + domainName + "\\" + windowsUserName + ")" + "') is not present in the signature property.";

			//verification if ESign operation completed successfully
			//------------------------------------------------------
			if (result.equals(""))
				Log.pass("Test case passed. Object (" + dataPool.get("ObjectName") + ") is successfully E-Signed with correct workflow state, timestamp and other details using Windows user (" + windowsUserName + ").", driver);
			else
				Log.fail("Test case failed. Object (" + dataPool.get("ObjectName") + ") is not E-Signed successfully with correct timestamp using Windows user (" + windowsUserName + ").Additional info. : "+ result, driver);

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

	}//End ESign_813

	/**
	 * ESign_815 : Signing with read-only user
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"BasicESign"},
			description = "Signing with read-only user")
	public void ESign_815(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), testVault);//Logging into the vault

			Log.message("Pre-requsite : Browser is opened and logged into MFWA as Read only user. ( User Name : " +  dataPool.get("UserName")  + "; Vault : " +  testVault  + ")", driver);

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), "");//Navigate to the specific view

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))//Selects the object in the view
				throw new Exception("'" + dataPool.get("ObjectName")  + "' is not selected in the view.");

			Log.message("1. Navigated to " +  viewtonavigate  + " and selected the object \"" +  dataPool.get("ObjectName") +  "\" in the view.", driver);

			//Step-2 : Sets the ESign worklfow in the workflow dialog
			//--------------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);//Opens the Workflow dialog using operation menu

			if (!MFilesDialog.exists(driver, Caption.MenuItems.Workflow.Value))//Checks if workflow state transition dialog is exist or not
				throw new Exception("Workflow dialog is not opened while click on the Workflow option from operation menu.");

			Log.message("2. Workflow dialog is opened via operation menu for the selected object.", driver);

			//Step-3: Try to set the workflow for the object and save the changes as read only user
			//-------------------------------------------------------------------------------------
			MFilesDialog mfDialog = new MFilesDialog(driver, Caption.MenuItems.Workflow.Value);//Instantiates the workflow dialog
			mfDialog.setWorkflow(dataPool.get("Workflow"));//Sets the workflow in the dialog
			mfDialog.setWorkflowState(dataPool.get("WorkflowState"));//Sets the workflow state in the dialog
			mfDialog.clickOkButton();//Clicks the OK button in the workflows dialog
			Utils.fluentWait(driver);

			Log.message("3. Workflow (" + dataPool.get("Workflow") + ") and state (" + dataPool.get("WorkflowState") + ") is set in the workflow dialog and save button clicked.", driver);

			//Checks if warning dialog is displayed for the read only user
			//------------------------------------------------------------
			if (!MFilesDialog.exists(driver, "M-Files Web"))
				throw new Exception("Warning dialog is not displayed with error message while setting the workflow as read only user.");

			mfDialog = new MFilesDialog(driver, "M-Files Web");//Instantiates the error message dialog

			//Verification: If expected warning message is displayed or not
			//--------------------------------------------------------------
			if (mfDialog.getMessage().contains(dataPool.get("ErrorMessage")))
				Log.pass("Test case passed. Expected warning message is displayed while setting workflow as a ready only user.", driver);
			else
				Log.fail("Test case failed. Expected error message is not displayed while setting workflow as a read only user.", driver);

			mfDialog.close();//Closes the MFiles Dialog

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

	}//End ESign_815

	/**
	 * ESign_818 : Cancel the signing process and then try to sign it
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"BasicESign"},
			description = "Cancel the signing process and then try to sign it")
	public void ESign_818(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launches driver and logging in

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), "");//Navigate to the specific view

			homePage.listView.clickItem(dataPool.get("ObjectName"));//Selects the object in the view

			Log.message("1. Navigated to " +  viewtonavigate  + " and selected the object \"" +  dataPool.get("ObjectName") +  "\" in the view.", driver);

			//Step-2 : Sets the ESign worklfow in the metadata card
			//-----------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadatacard
			metadataCard.setWorkflow(dataPool.get("Workflow"));//Sets the Worklfow in the right pane metadata card
			metadataCard.saveAndClose();//Saves the changes in rightpane metadata card

			Log.message("2. E-Sign Workflow (" + dataPool.get("Workflow") + ") is set and saved the changes in right pane metadata card.", driver);

			//Step-3: Perform E-Sgin required state transition via Task pane
			//--------------------------------------------------------------
			if (!homePage.taskPanel.clickItem(dataPool.get("ESignState")))//Clicks the workflow state in the state transition section in the task panel
				throw new Exception("Required E-Sign Workflow is not set for the selected object in the view.");

			if (!MFilesDialog.exists(driver, Caption.MenuItems.Workflow.Value))//Checks if workflow state transition dialog is exist or not
				throw new Exception("State transition workflow dialog is not opened while click on the '" + dataPool.get("ESignState") + "' workflow state from task panel.");

			Log.message("3. State transition dialog is opened via task panel for the E-Sign required state(" + dataPool.get("ESignState") + ") transition.", driver);

			//Step-4: Clicks OK button in the State transition workflow dialog
			//----------------------------------------------------------------
			MFilesDialog mfDialog = new MFilesDialog(driver, Caption.MenuItems.Workflow.Value);//Instantiates the Workflow M-Files dialog in the view
			mfDialog.clickOkButton();//Clicks OK button in the Workflow dialog for the E-Sign required State transition

			if (!MFilesDialog.isESignDialogExist(driver))//Waits and Checks if Electronic Signature dialog is exists or not
				throw new Exception("Electronic signature dialog is not opened while performing state transition to the state '" + dataPool.get("ESignState") + "'.");

			Log.message("4. OK button is clicked in the State transition dialog for the E-Sign required state trainsition.", driver);
			//Step-5: Clicks cancel in the E-Sign M-Files Dialog
			//--------------------------------------------------
			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog

			mfDialog.clickCancelButton();//Clicks the cancel button in the E-Sign M-Files Dialog
			Utils.fluentWait(driver);

			Log.message("5. CANCEL button is clicked in the Electronic Signature MFiles Dialog.", driver);

			//Step-6: Again click on the OK button in the state transition workflow dialog
			//-----------------------------------------------------------------------------
			if (MFilesDialog.isESignDialogExist(driver))//Checks if e-sign dialog is exist or not
				throw new Exception("Test case failed. Electronic Signature/Workflow dialog is not closed after click cancel button on the Electronic Signature dialog.");

			Log.message("6.1. Electronic Signature dialog is closed after click cancel button on the Electronic Signature dialog.", driver);

			if (!MFilesDialog.exists(driver, Caption.MenuItems.Workflow.Value))//Checks if workflow state transition dialog is exist or not
				throw new Exception("State transition workflow dialog is closed after click cancel in the e-sign dialog.");

			mfDialog = new MFilesDialog(driver, Caption.MenuItems.Workflow.Value);//Instantiates the Workflow M-Files dialog in the view
			mfDialog.clickOkButton();//Clicks OK button in the Workflow dialog for the E-Sign required State transition

			Log.message("6.2. OK button is clicked in the state transition dialog.", driver);

			if (!MFilesDialog.isESignDialogExist(driver))//Checks if Electronic Signature dialog is exists or not
				throw new Exception("Electronic signature dialog is not opened while performing state transition to the state '" + dataPool.get("ESignState") + "'.");

			Log.message("6.3. E-Sign dialog is opened again after cancel the E-Signature first time.", driver);

			//Step-7: E-Sign the object in the Electronic Signature MFiles Dialog
			//-------------------------------------------------------------------
			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog
			mfDialog.eSign(userName, password, "", userFullName);//E-Signs the state transition

			Log.message("7. State transition is E-Signed by the User: '" + userName + "' in the E-Sign dialog.", driver);

			//Verification if state transition is completed successfully using Electronic signature
			//--------------------------------------------------------------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadata card

			if (metadataCard.getWorkflowState().equalsIgnoreCase(dataPool.get("ESignState")))
				Log.pass("Test case passed. Object is E-Signed successfully by Cancel the signing process and then again signing the same.", driver);
			else
				Log.fail("Test case failed. Object is not E-Signed successfully by Cancel the signing process and then again signing the same", driver);

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

	}//End ESign_818

	/**
	 * ESign_819 : Electronic signing with one state transition and signing with another state transition
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"BasicESign"},
			description = "Electronic signing with one state transition and signing with another state transition")
	public void ESign_819(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launches driver and logging in

			//Step-1 : Create two new objects in the view
			//-----------------------------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Assignment.Value);//Opens the new Assignment object metadata card

			String firstObjName = Utility.getObjectName(methodName) + "_Assignment";//Frames the object name

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card
			metadataCard.setInfo(dataPool.get("FirstObjProperties"));//Sets the required values in the metadatacard
			metadataCard.setPropertyValue("Name or title", firstObjName);//Sets the object name in the metadata card
			metadataCard.saveAndClose();//Creates the object in the view

			if (!homePage.listView.isItemExists(firstObjName))//Checks if object is exist in the view
				throw new Exception("'" + firstObjName + "' is not created successfully");

			Log.message("1.1. First object(" + firstObjName + ") is successfully created in the view.", driver);

			homePage.taskPanel.clickItem(Caption.ObjecTypes.Customer.Value);//Opens the new Customer object metadata card

			String secondObjName = Utility.getObjectName(methodName) + "_Customer";//Frames the object name

			metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card
			metadataCard.setPropertyValue("Customer name", secondObjName);//Sets the object name in the metadata card
			metadataCard.saveAndClose();//Creates the object in the view

			if (!homePage.listView.isItemExists(secondObjName))//Checks if object exist in the view
				throw new Exception("'" + secondObjName + "' is not created successfully");

			Log.message("1.2. Second object(" + secondObjName + ") is successfully created in the view.", driver);

			//Step-2 : Sets the ESign worklfow in the metadata card
			//-----------------------------------------------------
			if (!homePage.listView.clickItem(firstObjName))//Checks if object is selected in the view
				throw new Exception("Object '" + firstObjName + "' is not selected in the view");

			metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadatacard
			metadataCard.setWorkflow(dataPool.get("Workflow"));//Sets the Worklfow in the right pane metadata card
			metadataCard.saveAndClose();//Saves the changes in rightpane metadata card

			Log.message("2.1. E-Sign Workflow (" + dataPool.get("Workflow") + ") is set and saved the changes in right pane metadata card for the newly created object(" + firstObjName + ").", driver);

			if (!homePage.listView.clickItem(secondObjName))//Checks if object is selected in the view
				throw new Exception("Object '" + secondObjName + "' is not selected in the view");

			metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadatacard
			metadataCard.setWorkflow(dataPool.get("Workflow"));//Sets the Worklfow in the right pane metadata card
			metadataCard.saveAndClose();//Saves the changes in rightpane metadata card

			Log.message("2.2. E-Sign Workflow (" + dataPool.get("Workflow") + ") is set and saved the changes in right pane metadata card for the newly created object(" + secondObjName + ").", driver);

			//Step-3: Perform E-Sgin required state transition for the newly created objects via Task pane
			//--------------------------------------------------------------------------------------------
			if (!homePage.listView.clickItem(firstObjName))//Selects the object in the view
				throw new Exception("Object '" + firstObjName + "' is not selected in the view");

			Log.message("3.1. '" + firstObjName + "' is selected in the view.", driver);

			if (!homePage.taskPanel.clickItem(dataPool.get("ESignState")))//Clicks the workflow state in the state transition section in the task panel
				throw new Exception("Required E-Sign Workflow is not set for the selected object in the view.");

			Log.message("3.1.1. '" + dataPool.get("ESignState") + "' is clicked from task pane for the selected object(" + firstObjName + ") in the view.", driver);

			if (!MFilesDialog.exists(driver, Caption.MenuItems.Workflow.Value))//Checks if workflow state transition dialog is exist or not
				throw new Exception("State transition workflow dialog is not opened while click on the '" + dataPool.get("ESignState") + "' workflow state from task panel.");

			Log.message("3.1.2. State transition workflow dialog is opened while click on the '" + dataPool.get("ESignState") + "' workflow state from task panel.", driver);

			MFilesDialog mfDialog = new MFilesDialog(driver, Caption.MenuItems.Workflow.Value);//Instantiates the Workflow M-Files dialog in the view
			mfDialog.clickOkButton();//Clicks OK button in the Workflow dialog for the E-Sign required State transition

			Log.message("3.1.3. OK button is clicked in the state transition dialog.", driver);

			if (!MFilesDialog.isESignDialogExist(driver))//Waits and Checks if Electronic Signature dialog is exists or not
				throw new Exception("Electronic signature dialog is not opened while performing state transition to the state '" + dataPool.get("ESignState") + "'.");

			Log.message("3.1.4. Electronic signature dialog is opened while performing state transition to the state '" + dataPool.get("ESignState") + "'.", driver);

			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog
			mfDialog.eSign(userName, password, "", userFullName);//E-Signs the state transition

			Log.message("3.1.5. Performed E-Sign for the selected object(" + firstObjName + ").", driver);

			if (!homePage.listView.clickItem(secondObjName))//Selects the object in the view
				throw new Exception("Object '" + secondObjName + "' is not selected in the view");

			Log.message("3.2. '" + secondObjName + "' is selected in the view.", driver);

			if (!homePage.taskPanel.clickItem(dataPool.get("ESignState")))//Clicks the workflow state in the state transition section in the task panel
				throw new Exception("Required E-Sign Workflow is not set for the selected object in the view.");

			Log.message("3.2.1. '" + dataPool.get("ESignState") + "' is clicked from task pane for the selected object(" + secondObjName + ") in the view.", driver);

			if (!MFilesDialog.exists(driver, Caption.MenuItems.Workflow.Value))//Checks if workflow state transition dialog is exist or not
				throw new Exception("State transition workflow dialog is not opened while click on the '" + dataPool.get("ESignState") + "' workflow state from task panel.");

			Log.message("3.2.2. State transition workflow dialog is opened while click on the '" + dataPool.get("ESignState") + "' workflow state from task panel.", driver);

			mfDialog = new MFilesDialog(driver, Caption.MenuItems.Workflow.Value);//Instantiates the Workflow M-Files dialog in the view
			mfDialog.clickOkButton();//Clicks OK button in the Workflow dialog for the E-Sign required State transition

			Log.message("3.2.3. OK button is clicked in the state transition dialog.", driver);

			if (!MFilesDialog.isESignDialogExist(driver))//Waits and Checks if Electronic Signature dialog is exists or not
				throw new Exception("Electronic signature dialog is not opened while performing state transition to the state '" + dataPool.get("ESignState") + "'.");

			Log.message("3.2.4. Electronic signature dialog is opened while performing state transition to the state '" + dataPool.get("ESignState") + "'.", driver);

			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog
			mfDialog.eSign(userName, password, "", userFullName);//E-Signs the state transition

			Log.message("3.2.5. Performed E-Sign operation for the selected object(" + secondObjName + ") in the view", driver);

			//Check points
			//-------------
			String result = "";

			//Select the first object
			//-----------------------
			if (!homePage.listView.clickItem(firstObjName))//Selects the object in the view
				throw new Exception("Object '" + firstObjName + "' is not selected in the view");

			metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadata card

			//Checks if expected workflow state is set and Signature manifesto property added in the metadatacard
			//----------------------------------------------------------------------------------------------------
			if (!metadataCard.getWorkflowState().equalsIgnoreCase(dataPool.get("ESignState")) || !metadataCard.propertyExists(dataPool.get("Property")) )
				result += "Object(" + firstObjName + ") is not E-Signed successfully.";

			//Select the second object
			//-----------------------
			if (!homePage.listView.clickItem(secondObjName))//Selects the object in the view
				throw new Exception("Object '" + secondObjName + "' is not selected in the view");

			metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadata card

			//Checks if expected workflow state is set and Signature manifesto property added in the metadatacard
			//----------------------------------------------------------------------------------------------------
			if (!metadataCard.getWorkflowState().equalsIgnoreCase(dataPool.get("ESignState")) || !metadataCard.propertyExists(dataPool.get("Property")) )
				result += " Object(" + secondObjName + ") is not E-Signed successfully.";

			//Verification if Objects E-Signed correctly
			//-------------------------------------------
			if (result.equals(""))
				Log.pass("Test case passed. Newly created objects E-Signed successfully.", driver);
			else
				Log.fail("Test case failed. Newly created objects not E-Signed successfully.[Additional info. : " + result + "]", driver);

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

	}//End ESign_819

	/**
	 * ESign_820 : Signatures have correct timestamps.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"BasicESign"},
			description = "Signatures have correct timestamps.")
	public void ESign_820(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launches driver and logging in

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), "");//Navigate to the specific view

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))//Selects the object in the view
				throw new Exception("'" + dataPool.get("ObjectName")  + "' is not selected in the view.");

			Log.message("1. Navigated to " +  viewtonavigate  + " and selected the object \"" +  dataPool.get("ObjectName") +  "\" in the view.", driver);

			//Step-2 : Sets the ESign worklfow in the metadata card
			//-----------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadatacard
			metadataCard.setWorkflow(dataPool.get("Workflow"));//Sets the Worklfow in the right pane metadata card
			metadataCard.saveAndClose();//Saves the changes in rightpane metadata card

			Log.message("2. E-Sign Workflow (" + dataPool.get("Workflow") + ") is set and saved the changes in right pane metadata card.", driver);

			//Step-3: Perform E-Sgin required state transition via Task pane
			//--------------------------------------------------------------
			if (!homePage.taskPanel.clickItem(dataPool.get("ESignState")))//Clicks the workflow state in the state transition section in the task panel
				throw new Exception("Required E-Sign Workflow is not set for the selected object in the view.");

			if (!MFilesDialog.exists(driver, Caption.MenuItems.Workflow.Value))//Checks if workflow state transition dialog is exist or not
				throw new Exception("State transition workflow dialog is not opened while click on the '" + dataPool.get("ESignState") + "' workflow state from task panel.");

			Log.message("3. State transition dialog is opened via task panel for the E-Sign required state(" + dataPool.get("ESignState") + ") transition.", driver);

			//Step-4: Clicks OK button in the State transition workflow dialog
			//----------------------------------------------------------------
			MFilesDialog mfDialog = new MFilesDialog(driver, Caption.MenuItems.Workflow.Value);//Instantiates the Workflow M-Files dialog in the view
			mfDialog.clickOkButton();//Clicks OK button in the Workflow dialog for the E-Sign required State transition

			if (!MFilesDialog.isESignDialogExist(driver))//Checks if Electronic Signature dialog is exists or not
				throw new Exception("Electronic signature dialog is not opened while performing state transition to the state '" + dataPool.get("ESignState") + "'.");

			Log.message("4. OK button is clicked in the State transition dialog for the E-Sign required state trainsition.", driver);
			//Step-5: E-Sign the object in M-Files Dialog
			//--------------------------------------------------
			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog

			String expectedTimeStamp = mfDialog.eSign(userName, password, "", userFullName);//E-Signs the state transition

			Log.message("5. State transition is E-Signed by the User: '" + userName + "'. in E-Sign dialog", driver);

			//Step-6: Gets the Property value from the metadatacard
			//--------------------------------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadata card

			if (!metadataCard.getWorkflowState().equalsIgnoreCase(dataPool.get("ESignState")))
				throw new Exception("Object is not E-Signed Successfully.[Additional info. : Workflow state '" + dataPool.get("ESignState") + "' is not set in the ESigned object]");

			if (!metadataCard.propertyExists(dataPool.get("Property")))
				throw new Exception(dataPool.get("Property")  + " is not added in the E-Signed object");

			String actualTimeStamp = metadataCard.getPropertyValue(dataPool.get("Property"));

			Log.message("6. Property(" + dataPool.get("Property") + ") value is get from the metadatacard.", driver);

			//Verification if signature has valid time stamps
			//-----------------------------------------------
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
			Date date = new Date();
			String currentDate = dateFormat.format(date);
			String result = "";

			//Check if Expected date is present in the signature property
			//-----------------------------------------------------------
			if (!actualTimeStamp.contains(currentDate))
				result = "Expected date('" + currentDate + "') is not present in the signature property.";

			//Check if Expected Username is present in the signature property
			//---------------------------------------------------------------
			if (!actualTimeStamp.toLowerCase().contains((userFullName  + " (" + userName + ")").toLowerCase()))
				result += "Expected user signature('" + userFullName  + " (" + userName + ")" + ")" + "') is not present in the signature property.";

			//verification Signatures have correct timestamps
			//------------------------------------------------------
			if (result.equals(""))
				Log.pass("Test case passed. Timestamp is displayed as expected(" + expectedTimeStamp + ") format in the " +  dataPool.get("Property")  + " for the E-Signed object (" + dataPool.get("ObjectName") + ").", driver);
			else
				Log.fail("Test case failed. Timestamp is not displayed as expected(" + expectedTimeStamp + ") format in the " +  dataPool.get("Property")  + " for the E-Signed object (" + dataPool.get("ObjectName") + "). Additional info. : " + result, driver);

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

	}//End ESign_820

	/**
	 * ESign_822 : Scrolling of electronic signature dialog
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"BasicESign"},
			description = "Scrolling of electronic signature dialog")
	public void ESign_822(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launches driver and logging in

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), "");//Navigate to the specific view

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))//Selects the object in the view
				throw new Exception("'" + dataPool.get("ObjectName")  + "' is not selected in the view.");

			Log.message("1. Navigated to " +  viewtonavigate  + " and selected the object \"" +  dataPool.get("ObjectName") +  "\" in the view.", driver);

			//Step-2 : Sets the ESign worklfow in the metadata card
			//-----------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadatacard
			metadataCard.setWorkflow(dataPool.get("Workflow"));//Sets the Worklfow in the right pane metadata card
			metadataCard.saveAndClose();//Saves the changes in rightpane metadata card

			Log.message("2. E-Sign Workflow (" + dataPool.get("Workflow") + ") is set and saved the changes in right pane metadata card.", driver);

			//Step-3: Perform E-Sgin required state transition via Task pane
			//--------------------------------------------------------------
			if (!homePage.taskPanel.clickItem(dataPool.get("ESignState")))//Clicks the workflow state in the state transition section in the task panel
				throw new Exception("Required E-Sign Workflow is not set for the selected object in the view.");			

			if (!MFilesDialog.exists(driver, Caption.MenuItems.Workflow.Value))//Checks if workflow state transition dialog is exist or not
				throw new Exception("State transition workflow dialog is not opened while click on the '" + dataPool.get("ESignState") + "' workflow state from task panel.");

			Log.message("3. State transition dialog is opened via task panel for the E-Sign required state(" + dataPool.get("ESignState") + ") transition.", driver);

			//Step-4: Clicks OK button in the State transition workflow dialog
			//----------------------------------------------------------------
			MFilesDialog mfDialog = new MFilesDialog(driver, Caption.MenuItems.Workflow.Value);//Instantiates the Workflow M-Files dialog in the view
			mfDialog.clickOkButton();//Clicks OK button in the Workflow dialog for the E-Sign required State transition

			if (!MFilesDialog.isESignDialogExist(driver))//Checks if Electronic Signature dialog is exists or not
				throw new Exception("Electronic signature dialog is not opened while performing state transition to the state '" + dataPool.get("ESignState") + "'.");

			Log.message("4. OK button is clicked in the State transition dialog for the E-Sign required state trainsition.", driver);

			//Step-5: Get the ESign Message from the E-Sign dialog
			//----------------------------------------------------
			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog
			mfDialog.selectESignReason(dataPool.get("ESignReason"));//Selects the reason in the E-Sign dialog
			String eSignMessage = mfDialog.getESignDialogMessage();//Gets the E-Sign dialog message text

			Log.message("5. Long reason is selected and got the long message from the E-Sign dialog.", driver);

			//Verification if long reason displayed as expected in the E-Sign dialog
			//------------------------------------------------------------------
			if (eSignMessage.equalsIgnoreCase(dataPool.get("ESignMessage")))
				Log.pass("Test case passed. Long text is displayed as expected in the E-Sign dialog.", driver);
			else
				Log.fail("Test case failed. Long text is not displayed as expected in the E-Sign dialog.[Add info. : Actual message : '" + eSignMessage + "' and Expected message : '" + dataPool.get("ESignMessage") + "']", driver);

			mfDialog.close();//Closes the E-Sign dialog

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

	}//End ESign_822

	/**
	 * ESign_823 : Encoding of the texts in the signing dialog
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"BasicESign"},
			description = "Encoding of the texts in the signing dialog")
	public void ESign_823(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launches driver and logging in

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), "");//Navigate to the specific view

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))//Selects the object in the view
				throw new Exception("'" + dataPool.get("ObjectName")  + "' is not selected in the view.");

			Log.message("1. Navigated to " +  viewtonavigate  + " and selected the object \"" +  dataPool.get("ObjectName") +  "\" in the view.", driver);

			//Step-2 : Sets the ESign worklfow in the metadata card
			//-----------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadatacard
			metadataCard.setWorkflow(dataPool.get("Workflow"));//Sets the Worklfow in the right pane metadata card
			metadataCard.saveAndClose();//Saves the changes in rightpane metadata card

			Log.message("2. E-Sign Workflow (" + dataPool.get("Workflow") + ") is set and saved the changes in right pane metadata card.", driver);

			//Step-3: Perform E-Sgin required state transition via Task pane
			//--------------------------------------------------------------
			if (!homePage.taskPanel.clickItem(dataPool.get("ESignState")))//Clicks the workflow state in the state transition section in the task panel
				throw new Exception("Required E-Sign Workflow is not set for the selected object in the view.");			

			if (!MFilesDialog.exists(driver, Caption.MenuItems.Workflow.Value))//Checks if workflow state transition dialog is exist or not
				throw new Exception("State transition workflow dialog is not opened while click on the '" + dataPool.get("ESignState") + "' workflow state from task panel.");

			Log.message("3. State transition dialog is opened via task panel for the E-Sign required state(" + dataPool.get("ESignState") + ") transition.", driver);

			//Step-4: Clicks OK button in the State transition workflow dialog
			//----------------------------------------------------------------
			MFilesDialog mfDialog = new MFilesDialog(driver, Caption.MenuItems.Workflow.Value);//Instantiates the Workflow M-Files dialog in the view
			mfDialog.clickOkButton();//Clicks OK button in the Workflow dialog for the E-Sign required State transition

			if (!MFilesDialog.isESignDialogExist(driver))//Checks if Electronic Signature dialog is exists or not
				throw new Exception("Electronic signature dialog is not opened while performing state transition to the state '" + dataPool.get("ESignState") + "'.");

			Log.message("4. OK button is clicked in the State transition dialog for the E-Sign required state trainsition.", driver);

			//Step-5: Get the ESign Message from the E-Sign dialog
			//----------------------------------------------------
			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog

			String eSignMessage = mfDialog.getESignDialogMessage();

			Log.message("5. E-Sign dialog is displayed and got the message from the E-Sign dialog.", driver);

			//Verification if Encoded texts displayed as expected in the E-Sign dialog
			//------------------------------------------------------------------------
			if (eSignMessage.equalsIgnoreCase(dataPool.get("ESignMessage")))
				Log.pass("Test case passed. Encoding text is displayed as expected in the E-Sign dialog.", driver);
			else
				Log.fail("Test case failed. Encoding text is not displayed as expected in the E-Sign dialog.[Add info. : Actual message : '" + eSignMessage + "' and Expected message : '" + dataPool.get("ESignMessage") + "']", driver);

			mfDialog.close();//Closes the MFiles Dialog

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

	}//End ESign_823

	/**
	 * ESign_824 : Copying object does not copy signatures
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"BasicESign"},
			description = "Copying object does not copy signatures")
	public void ESign_824(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launches driver and logging in

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), "");//Navigate to the specific view

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))//Selects the object in the view
				throw new Exception("'" + dataPool.get("ObjectName")  + "' is not selected in the view.");

			Log.message("1. Navigated to " +  viewtonavigate  + " and selected the object \"" +  dataPool.get("ObjectName") +  "\" in the view.", driver);

			//Step-2 : Sets the ESign worklfow in the metadata card
			//-----------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadatacard
			metadataCard.setWorkflow(dataPool.get("Workflow"));//Sets the Worklfow in the right pane metadata card
			metadataCard.saveAndClose();//Saves the changes in rightpane metadata card

			Log.message("2. E-Sign Workflow (" + dataPool.get("Workflow") + ") is set and saved the changes in right pane metadata card.", driver);

			//Step-3: Perform E-Sgin required state transition via Task pane
			//--------------------------------------------------------------
			if (!homePage.taskPanel.clickItem(dataPool.get("ESignState")))//Clicks the workflow state in the state transition section in the task panel
				throw new Exception("Required E-Sign Workflow is not set for the selected object in the view.");

			if (!MFilesDialog.exists(driver, Caption.MenuItems.Workflow.Value))//Checks if workflow state transition dialog is exist or not
				throw new Exception("State transition workflow dialog is not opened while click on the '" + dataPool.get("ESignState") + "' workflow state from task panel.");

			Log.message("3. State transition dialog is opened via task panel for the E-Sign required state(" + dataPool.get("ESignState") + ") transition.", driver);

			//Step-4: Clicks OK button in the State transition workflow dialog
			//----------------------------------------------------------------
			MFilesDialog mfDialog = new MFilesDialog(driver, Caption.MenuItems.Workflow.Value);//Instantiates the Workflow M-Files dialog in the view
			mfDialog.clickOkButton();//Clicks OK button in the Workflow dialog for the E-Sign required State transition

			if (!MFilesDialog.isESignDialogExist(driver))//Checks if Electronic Signature dialog is exists or not
				throw new Exception("Electronic signature dialog is not opened while performing state transition to the state '" + dataPool.get("ESignState") + "'.");

			Log.message("4. OK button is clicked in the State transition dialog for the E-Sign required state trainsition.", driver);

			//Step-5: E-Sign the object in M-Files Dialog
			//--------------------------------------------------
			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog

			mfDialog.eSign(userName, password, "", userFullName);//E-Signs the state transition

			Log.message("5. State transition is E-Signed by the User: '" + userName + "'.", driver);

			//Step-6: Make copy the E-Signed object in the view
			//--------------------------------------------------------------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadata card

			if (!metadataCard.getWorkflowState().equalsIgnoreCase(dataPool.get("ESignState")))
				throw new Exception("Object is not E-Signed Successfully.[Additional info. : Workflow state '" + dataPool.get("ESignState") + "' is not set in the ESigned object]");

			if (!metadataCard.propertyExists(dataPool.get("Property")))
				throw new Exception(dataPool.get("Property")  + " is not added in the E-Signed object");

			homePage.taskPanel.clickItem(Caption.MenuItems.MakeCopy.Value);//Clicks the Make copy option from task pane

			Log.message("6. Make copy option clicked from Task pane for the E-Signed object(" + dataPool.get("ObjectName") + ")", driver);

			//Verification if signature exists in the make copied metadata card
			//------------------------------------------------------------------
			metadataCard = new MetadataCard(driver);//Instantiates the make copied metadata card
			
			if (metadataCard.propertyExists(dataPool.get("Property")))
				throw new Exception("Signature is copied to new object while make copy the E-Signed object.");
			
			metadataCard.setCheckInImmediately(true);
			metadataCard.closeMetadataCard();//Closes the popout metadata card

			metadataCard = new MetadataCard(driver, true);
			
			if (!metadataCard.propertyExists(dataPool.get("Property")))
				Log.pass("Test case passed. Signature is not copied while make copy the E-Signed object.", driver);
			else
				Log.fail("Test case failed. Signature is copied while make copy the E-Signed object.", driver);
			
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

	}//End ESign_824

	/**
	 * ESign_826 : Signing as Windows user with different invalid data
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"BasicESign", "WindowsUser"},
			description = "Signing as Windows user with different invalid data")
	public void ESign_826(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, windowsUserName, windowsPassword, testVault); //Launches driver and logging in

			Log.message("Pre-Requiste: Logged into MFWA as Windows user. '" + windowsUserName + "'", driver);

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), "");//Navigate to the specific view

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))//Selects the object in the view
				throw new Exception("'" + dataPool.get("ObjectName")  + "' is not selected in the view.");

			Log.message("1. Navigated to " +  viewtonavigate  + " and selected the object \"" +  dataPool.get("ObjectName") +  "\" in the view.", driver);

			//Step-2 : Sets the ESign worklfow in the metadata card
			//-----------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadatacard
			metadataCard.setWorkflow(dataPool.get("Workflow"));//Sets the Worklfow in the right pane metadata card
			metadataCard.saveAndClose();//Saves the changes in rightpane metadata card

			Log.message("2. E-Sign Workflow (" + dataPool.get("Workflow") + ") is set and saved the changes in right pane metadata card.", driver);

			//Step-3: Perform E-Sgin required state transition via Task pane
			//--------------------------------------------------------------
			if (!homePage.taskPanel.clickItem(dataPool.get("ESignState")))//Clicks the workflow state in the state transition section in the task panel
				throw new Exception("Required E-Sign Workflow is not set for the selected object in the view.");

			if (!MFilesDialog.exists(driver, Caption.MenuItems.Workflow.Value))//Checks if workflow state transition dialog is exist or not
				throw new Exception("State transition workflow dialog is not opened while click on the '" + dataPool.get("ESignState") + "' workflow state from task panel.");

			Log.message("3. State transition dialog is opened via task panel for the E-Sign required state(" + dataPool.get("ESignState") + ") transition.", driver);

			//Step-4: Clicks OK button in the State transition workflow dialog
			//----------------------------------------------------------------
			MFilesDialog mfDialog = new MFilesDialog(driver, Caption.MenuItems.Workflow.Value);//Instantiates the Workflow M-Files dialog in the view
			mfDialog.clickOkButton();//Clicks OK button in the Workflow dialog for the E-Sign required State transition

			if (!MFilesDialog.isESignDialogExist(driver))//Checks if Electronic Signature dialog is exists or not
				throw new Exception("Electronic signature dialog is not opened while performing state transition to the state '" + dataPool.get("ESignState") + "'.");

			Log.message("4. OK button is clicked in the State transition dialog for the E-Sign required state trainsition.", driver);

			//Step-5: E-Sign the object in M-Files Dialog
			//--------------------------------------------------
			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog

			mfDialog.eSign(dataPool.get("UserName"), dataPool.get("Password"), "", "");//E-Signs the state transition

			Log.message("5. Tried to perform State transition using the invalid credentials - DomainName: '" + dataPool.get("DomainName") + "', Username: '" + dataPool.get("UserName") + "' & Password: '" + dataPool.get("Password") + "'.", driver);

			//Check if warning dialog is displayed while e-signing using invalid credentials
			//-------------------------------------------------------------------------------
			if (!MFilesDialog.exists(driver, "M-Files Web"))
				throw new Exception("Test case failed. Error message dialog is not displayed while performing E-Sign operation using invalid credentials (DomainName: '" + dataPool.get("DomainName") + "', Username: '" + dataPool.get("UserName") + "' & Password: '" + dataPool.get("Password") + "').");

			mfDialog = new MFilesDialog(driver, "M-Files Web");//Instantiates the M-Files dialog in the view

			//Verification: Checks if E-Sign fails for the invalid credentials with expected warning message
			//----------------------------------------------------------------------------------------------
			if (mfDialog.getMessage().contains(dataPool.get("ErrorMessage")))
				Log.pass("Test case passed. Expected error message is displayed while performing E-Sign operation using invalid Windows user credentials (DomainName: '" + dataPool.get("DomainName") + "', Username: '" + dataPool.get("UserName") + "' & Password: '" + dataPool.get("Password") + "').", driver);
			else
				Log.fail("Test case failed. Expected error message is not displayed while performing E-Sign operation using invalid Windows user credentials (DomainName: '" + dataPool.get("DomainName") + "', Username: '" + dataPool.get("UserName") + "' & Password: '" + dataPool.get("Password") + "').", driver);

			mfDialog.clickOkButton();//Clicks ok button in the warning dialog

			if (MFilesDialog.exists(driver, Caption.MFilesDialog.ElectronicSignature.Value))//Checks if ESign dialog exists in the view
			{
				mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog
				mfDialog.close();//Closes the ESign Dialog
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

	}//End ESign_826

	/**
	 * ESign_827 : Signing as M-Files user with different invalid data
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"BasicESign"},
			description = "Signing as M-Files user with different invalid data")
	public void ESign_827(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launches driver and logging in

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), "");//Navigate to the specific view

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))//Selects the object in the view
				throw new Exception("'" + dataPool.get("ObjectName")  + "' is not selected in the view.");

			Log.message("1. Navigated to " +  viewtonavigate  + " and selected the object \"" +  dataPool.get("ObjectName") +  "\" in the view.", driver);

			//Step-2 : Sets the ESign worklfow in the metadata card
			//-----------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadatacard
			metadataCard.setWorkflow(dataPool.get("Workflow"));//Sets the Worklfow in the right pane metadata card
			metadataCard.saveAndClose();//Saves the changes in rightpane metadata card

			Log.message("2. E-Sign Workflow (" + dataPool.get("Workflow") + ") is set and saved the changes in right pane metadata card.", driver);

			//Step-3: Perform E-Sgin required state transition via Task pane
			//--------------------------------------------------------------
			if (!homePage.taskPanel.clickItem(dataPool.get("ESignState")))//Clicks the workflow state in the state transition section in the task panel
				throw new Exception("Required E-Sign Workflow is not set for the selected object in the view.");

			if (!MFilesDialog.exists(driver, Caption.MenuItems.Workflow.Value))//Checks if workflow state transition dialog is exist or not
				throw new Exception("State transition workflow dialog is not opened while click on the '" + dataPool.get("ESignState") + "' workflow state from task panel.");

			Log.message("3. State transition dialog is opened via task panel for the E-Sign required state(" + dataPool.get("ESignState") + ") transition.", driver);

			//Step-4: Clicks OK button in the State transition workflow dialog
			//----------------------------------------------------------------
			MFilesDialog mfDialog = new MFilesDialog(driver, Caption.MenuItems.Workflow.Value);//Instantiates the Workflow M-Files dialog in the view
			mfDialog.clickOkButton();//Clicks OK button in the Workflow dialog for the E-Sign required State transition

			if (!MFilesDialog.isESignDialogExist(driver))//Checks if Electronic Signature dialog is exists or not
				throw new Exception("Electronic signature dialog is not opened while performing state transition to the state '" + dataPool.get("ESignState") + "'.");

			Log.message("4. OK button is clicked in the State transition dialog for the E-Sign required state trainsition.", driver);

			//Step-5: E-Sign the object in M-Files Dialog
			//--------------------------------------------------
			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog

			mfDialog.eSign(dataPool.get("UserName"), dataPool.get("Password"), "", "");//E-Signs the state transition

			Log.message("5. Tried to perform State transition using the invalid credentials - Username: '" + dataPool.get("UserName") + "' & Password: '" + dataPool.get("Password") + "'.", driver);

			//Verification if E-Sign fails for the invalid credentials
			//--------------------------------------------------------
			if (!MFilesDialog.exists(driver, "M-Files Web"))
				throw new Exception("Test case failed. Error message dialog is not displayed while performing E-Sign operation using invalid credentials (Username: '" + dataPool.get("UserName") + "' & Password: '" + dataPool.get("Password") + "').");

			mfDialog = new MFilesDialog(driver, "M-Files Web");//Instantiates the M-Files dialog in the view

			if (mfDialog.getMessage().contains(dataPool.get("ErrorMessage")))
				Log.pass("Test case passed. Expected error message is displayed while performing E-Sign operation using invalid M-Files user credentials (Username: '" + dataPool.get("UserName") + "' & Password: '" + dataPool.get("Password") + "').", driver);
			else
				Log.fail("Test case failed. Expected error message is not displayed while performing E-Sign operation using invalid M-Files user credentials (Username: '" + dataPool.get("UserName") + "' & Password: '" + dataPool.get("Password") + "').", driver);

			mfDialog.clickOkButton();//Clicks the OK button in the warning dialog

			if (MFilesDialog.exists(driver, Caption.MFilesDialog.ElectronicSignature.Value))//Checks if ESign dialog exists in the view
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

	}//End ESign_827

	/**
	 * ESign_828 : No rights to commit the state transition
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"BasicESign"},
			description = "No rights to commit the state transition")
	public void ESign_828(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), testVault);//Logging into the vault

			Log.message("Pre-requsite : Browser is opened and logged into MFWA. ( User Name : " +  dataPool.get("UserName")  + "; Vault : " +  testVault  + ")", driver);

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), "");//Navigate to the specific view

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))//Selects the object in the view
				throw new Exception("'" + dataPool.get("ObjectName")  + "' is not selected in the view.");

			Log.message("1. Navigated to " +  viewtonavigate  + " and selected the object \"" +  dataPool.get("ObjectName") +  "\" in the view.", driver);

			//Step-2 : Sets the ESign worklfow in the metadata card
			//-----------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadatacard
			metadataCard.setWorkflow(dataPool.get("Workflow"));//Sets the Worklfow in the right pane metadata card
			metadataCard.saveAndClose();//Saves the changes in rightpane metadata card

			Log.message("2. E-Sign Workflow (" + dataPool.get("Workflow") + ") is set and saved the changes in right pane metadata card.", driver);

			//Verification: If denied state transition is not displayed for denied user
			//---------------------------------------------------------------------
			if (!homePage.taskPanel.isItemExists(dataPool.get("ESignState")))//Checks if denied workflow state transition is exist or not in the task panel
				Log.pass("Test case passed. No rights to commit the state transition is not displayed as expected for the denied user. (Username: '" + dataPool.get("UserName") + "' & Password: '" + dataPool.get("Password") + "').", driver);
			else
				Log.fail("Test case failed. No rights to commit the state transition is displayed as expected for the denied user(Username: '" + dataPool.get("UserName") + "' & Password: '" + dataPool.get("Password") + "').", driver);

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

	}//End ESign_828

	/**
	 * ESign_829 : Logged in as user X and trying to sign the state transition as user Y
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"BasicESign"},
			description = "Logged in as user X and trying to sign the state transition as user Y")
	public void ESign_829(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launches driver and logging in

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), "");//Navigate to the specific view

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))//Selects the object in the view
				throw new Exception("'" + dataPool.get("ObjectName")  + "' is not selected in the view.");

			Log.message("1. Navigated to " +  viewtonavigate  + " and selected the object \"" +  dataPool.get("ObjectName") +  "\" in the view.", driver);

			//Step-2 : Sets the ESign worklfow in the metadata card
			//-----------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadatacard
			metadataCard.setWorkflow(dataPool.get("Workflow"));//Sets the Worklfow in the right pane metadata card
			metadataCard.saveAndClose();//Saves the changes in rightpane metadata card

			Log.message("2. E-Sign Workflow (" + dataPool.get("Workflow") + ") is set and saved the changes in right pane metadata card.", driver);

			//Step-3: Perform E-Sgin required state transition via Task pane
			//--------------------------------------------------------------
			if (!homePage.taskPanel.clickItem(dataPool.get("ESignState")))//Clicks the workflow state in the state transition section in the task panel
				throw new Exception("Required E-Sign Workflow is not set for the selected object in the view.");

			if (!MFilesDialog.exists(driver, Caption.MenuItems.Workflow.Value))//Checks if workflow state transition dialog is exist or not
				throw new Exception("State transition workflow dialog is not opened while click on the '" + dataPool.get("ESignState") + "' workflow state from task panel.");

			Log.message("3. State transition dialog is opened via task panel for the E-Sign required state(" + dataPool.get("ESignState") + ") transition.", driver);

			//Step-4: Clicks OK button in the State transition workflow dialog
			//----------------------------------------------------------------
			MFilesDialog mfDialog = new MFilesDialog(driver, Caption.MenuItems.Workflow.Value);//Instantiates the Workflow M-Files dialog in the view
			mfDialog.clickOkButton();//Clicks OK button in the Workflow dialog for the E-Sign required State transition

			if (!MFilesDialog.isESignDialogExist(driver))//Checks if Electronic Signature dialog is exists or not
				throw new Exception("Electronic signature dialog is not opened while performing state transition to the state '" + dataPool.get("ESignState") + "'.");

			Log.message("4. OK button is clicked in the State transition dialog for the E-Sign required state trainsition.", driver);

			//Step-5: E-Sign the object in M-Files Dialog
			//--------------------------------------------------
			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog

			mfDialog.eSign(dataPool.get("UserName"), dataPool.get("Password"), "", "");//E-Signs the state transition

			Log.message("5. Tried to perform State transition using the non-logged in user credentials - Username: '" + dataPool.get("UserName") + "' & Password: '" + dataPool.get("Password") + "'.", driver);

			//Verification if E-Sign is failed for different user
			//---------------------------------------------------
			if (!MFilesDialog.exists(driver, "M-Files Web"))
				throw new Exception("Test case failed. Error message dialog is not displayed while performing E-Sign operation using invalid credentials (Username: '" + dataPool.get("UserName") + "' & Password: '" + dataPool.get("Password") + "').");

			mfDialog = new MFilesDialog(driver, "M-Files Web");//Instantiates the M-Files dialog in the view

			if (mfDialog.getMessage().contains(dataPool.get("ErrorMessage")))
				Log.pass("Test case passed. Logged in as user X and trying to sign the state transition as user Y failed as Expected.", driver);
			else
				Log.fail("Test case failed. Expected error message is not displayed while trying to E-Sign as non-logged in user(Username: '" + dataPool.get("UserName") + "' & Password: '" + dataPool.get("Password") + "').", driver);

			mfDialog.clickOkButton();//Clicks the OK button in the warning dialog

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

	}//End ESign_829

	/**
	 * ESign_26772 : Verify if user is able to assign esign state by re-entering valid credentials in esign window
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"BasicESign"},
			description = "Verify if user is able to assign esign state by re-entering valid credentials in esign window")
	public void ESign_26772(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launches driver and logging in

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), "");//Navigate to the specific view

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))//Selects the object in the view
				throw new Exception("'" + dataPool.get("ObjectName")  + "' is not selected in the view.");

			Log.message("1. Navigated to " +  viewtonavigate  + " and selected the object \"" +  dataPool.get("ObjectName") +  "\" in the view.", driver);

			//Step-2 : Sets the ESign worklfow in the metadata card
			//-----------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadatacard
			metadataCard.setWorkflow(dataPool.get("Workflow"));//Sets the Worklfow in the right pane metadata card
			metadataCard.saveAndClose();//Saves the changes in rightpane metadata card

			Log.message("2. E-Sign Workflow (" + dataPool.get("Workflow") + ") is set and saved the changes in right pane metadata card.", driver);

			//Step-3: Perform E-Sgin required state transition via Task pane
			//--------------------------------------------------------------
			if (!homePage.taskPanel.clickItem(dataPool.get("ESignState")))//Clicks the workflow state in the state transition section in the task panel
				throw new Exception("Required E-Sign Workflow is not set for the selected object in the view.");

			if (!MFilesDialog.exists(driver, Caption.MenuItems.Workflow.Value))//Checks if workflow state transition dialog is exist or not
				throw new Exception("State transition workflow dialog is not opened while click on the '" + dataPool.get("ESignState") + "' workflow state from task panel.");

			Log.message("3. State transition dialog is opened via task panel for the E-Sign required state(" + dataPool.get("ESignState") + ") transition.", driver);

			//Step-4: Clicks OK button in the State transition workflow dialog
			//----------------------------------------------------------------
			MFilesDialog mfDialog = new MFilesDialog(driver, Caption.MenuItems.Workflow.Value);//Instantiates the Workflow M-Files dialog in the view
			mfDialog.clickOkButton();//Clicks OK button in the Workflow dialog for the E-Sign required State transition

			if (!MFilesDialog.isESignDialogExist(driver))//Checks if Electronic Signature dialog is exists or not
				throw new Exception("Electronic signature dialog is not opened while performing state transition to the state '" + dataPool.get("ESignState") + "'.");

			Log.message("4. OK button is clicked in the State transition dialog for the E-Sign required state trainsition.", driver);

			//Step-5: E-Sign the object in M-Files Dialog
			//--------------------------------------------------
			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog

			mfDialog.eSign(dataPool.get("UserName"), dataPool.get("Password"), "", "");//E-Signs the state transition

			Log.message("5. Tried to perform State transition using the invalid credentials - Username: '" + dataPool.get("UserName") + "' & Password: '" + dataPool.get("Password") + "'.", driver);

			//Step-6: Checks and Closes the MFiles warning dialog
			//---------------------------------------
			if (!MFilesDialog.exists(driver, "M-Files Web"))
				throw new Exception("Error message dialog is not displayed while performing E-Sign operation using invalid credentials (Username: '" + dataPool.get("UserName") + "' & Password: '" + dataPool.get("Password") + "').");

			mfDialog = new MFilesDialog(driver, "M-Files Web");//Instantiates the M-Files dialog in the view

			if (!mfDialog.getMessage().contains(dataPool.get("ErrorMessage")))
				throw new Exception("Expected error message('" +  dataPool.get("ErrorMessage")  + "') is not displayed while performing E-Sign operation using invalid M-Files user credentials (Username: '" + dataPool.get("UserName") + "' & Password: '" + dataPool.get("Password") + "'). Actual message: '" +  mfDialog.getMessage()  + "'");

			mfDialog.clickOkButton();//Clicks OK button in the MFiles Dialog
			Utils.fluentWait(driver);

			Log.message("6. Closed the warning dialog displayed for invalid ESign authentication.", driver);

			//Step-7: Perform the ESign again using the valid credentials
			//-----------------------------------------------------------
			if (!MFilesDialog.isESignDialogExist(driver))//Checks if Electronic Signature dialog is exists or not
				throw new Exception("Electronic signature dialog is closed after performing the invalid ESign operation.");

			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the ESign dialog in the view

			mfDialog.eSign(userName, password, "", "");//Performs the ESign operation

			Log.message("7. Performed ESign operation using the valid credentials in the Same ESign dialog.", driver);

			//verification if ESign operation completed successfully
			//------------------------------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiates the right pane metadatacard

			if (!metadataCard.getWorkflowState().equalsIgnoreCase(dataPool.get("ESignState")))
				throw new Exception("Test case failed. Object is not E-Signed Successfully.[Additional info. : Workflow state '" + dataPool.get("ESignState") + "' is not set in the ESigned object]");

			if (!metadataCard.propertyExists(dataPool.get("Property")))
				throw new Exception("Test case failed. " + dataPool.get("Property")  + " is not added in the E-Signed object");

			String actualTimeStamp = metadataCard.getPropertyValue(dataPool.get("Property"));

			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
			Date date = new Date();
			String currentDate = dateFormat.format(date);
			String result = "";

			//Check if Expected date is present in the signature property
			//-----------------------------------------------------------
			if (!actualTimeStamp.contains(currentDate))
				result = "Expected date('" + currentDate + "') is not present in the signature property.";

			//Check if Expected Username is present in the signature property
			//---------------------------------------------------------------
			if (!actualTimeStamp.contains(userFullName  + " (" + (userName + ")")))
				result += "Expected user signature('" + userFullName  + " (" + (userName + ")") + "') is not present in the signature property.";

			//verification if ESign operation completed successfully
			//------------------------------------------------------
			if (result.equals(""))
				Log.pass("Test case passed. Object (" + dataPool.get("ObjectName") + ") is successfully E-Signed with correct workflow state, timestamp and other details by re-entering valid credentials in esign window.", driver);
			else
				Log.fail("Test case failed. Object (" + dataPool.get("ObjectName") + ") is not E-Signed successfully with correct timestamp by re-entering valid credentials in esign window.[Additional info.: " + result + " & Signature propery value-" + actualTimeStamp + " ]", driver);


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

	}//End ESign_26772

	/**
	 * ESign_44631 : Verify if esign is successfully while selecting from multiple meanings
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"BasicESign"},
			description = "Verify if esign is successfully while selecting from multiple meanings")
	public void ESign_44631(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launches driver and logging in

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), "");//Navigate to the specific view

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))//Selects the object in the view
				throw new Exception("'" + dataPool.get("ObjectName")  + "' is not selected in the view.");

			Log.message("1. Navigated to " +  viewtonavigate  + " and selected the object \"" +  dataPool.get("ObjectName") +  "\" in the view.", driver);

			//Step-2 : Sets the ESign worklfow in the metadata card
			//-----------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadatacard
			metadataCard.setWorkflow(dataPool.get("Workflow"));//Sets the Worklfow in the right pane metadata card
			metadataCard.saveAndClose();//Saves the changes in rightpane metadata card

			Log.message("2. E-Sign Workflow (" + dataPool.get("Workflow") + ") is set and saved the changes in right pane metadata card.", driver);

			//Step-3: Perform E-Sgin required state transition via Task pane
			//--------------------------------------------------------------
			if (!homePage.taskPanel.clickItem(dataPool.get("ESignState")))//Clicks the workflow state in the state transition section in the task panel
				throw new Exception("Required E-Sign Workflow is not set for the selected object in the view.");			

			if (!MFilesDialog.exists(driver, Caption.MenuItems.Workflow.Value))//Checks if workflow state transition dialog is exist or not
				throw new Exception("State transition workflow dialog is not opened while click on the '" + dataPool.get("ESignState") + "' workflow state from task panel.");

			Log.message("3. State transition dialog is opened via task panel for the E-Sign required state(" + dataPool.get("ESignState") + ") transition.", driver);

			//Step-4: Clicks OK button in the State transition workflow dialog
			//----------------------------------------------------------------
			MFilesDialog mfDialog = new MFilesDialog(driver, Caption.MenuItems.Workflow.Value);//Instantiates the Workflow M-Files dialog in the view
			mfDialog.clickOkButton();//Clicks OK button in the Workflow dialog for the E-Sign required State transition

			if (!MFilesDialog.isESignDialogExist(driver))//Checks if Electronic Signature dialog is exists or not
				throw new Exception("Electronic signature dialog is not opened while performing state transition to the state '" + dataPool.get("ESignState") + "'.");

			Log.message("4. OK button is clicked in the State transition dialog for the E-Sign required state trainsition.", driver);

			//Step-5: Get the ESign Message from the E-Sign dialog
			//----------------------------------------------------
			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog
			mfDialog.selectESignReason(dataPool.get("ESignReason"));//Selects the reason in the E-Sign dialog
			String eSignMessage = mfDialog.getESignDialogMessage();//Gets the E-Sign dialog message text

			if (!eSignMessage.equalsIgnoreCase(dataPool.get("ESignMessage")))
				throw new Exception("Expected message('" + dataPool.get("ESignMessage") + "') is not displayed while selecting the reason('" + dataPool.get("ESignReason") + "'). Actual message displayed: '" + eSignMessage + "'");

			Log.message("5. Reason('" + dataPool.get("ESignReason") + "') is selected and the message('"+ dataPool.get("ESignMessage") +"') is displayed in the E-Sign dialog.", driver);

			//Step-6: ES-ign the object
			//--------------------------
			mfDialog.eSign(userName, password, "", userFullName);//E-Signs the state transition

			Log.message("6. Performed E-Sign operation.", driver);

			//Check points
			//------------
			String result = "";

			metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadata card

			//Checks if expected workflow state is set for the object
			//-------------------------------------------------------
			if (!metadataCard.getWorkflowState().equalsIgnoreCase(dataPool.get("ESignState")))//Checks if required state is set in the metadata card
				result += "Object(" + dataPool.get("ObjectName") + ") is not E-Signed successfully. Expected workflow state(" + dataPool.get("ESignState") + ") is not set in the metadatacard(Actual : " + metadataCard.getWorkflowState() + ").";		

			//Checks if Signature property is added in the metadatacard
			//---------------------------------------------------------
			if (!metadataCard.propertyExists(dataPool.get("Property")))//Checks if signature property is added into the metadata card
				result += "Object(" + dataPool.get("ObjectName") + ") is not E-Signed successfully. " + dataPool.get("Property") + " is not exist in the metadata card.";

			//Verification if Object E-Signed correctly
			//-------------------------------------------
			if (result.equals(""))
				Log.pass("Test case passed. E-Sign is successful when selecting reasons from multiple meanings.", driver);
			else
				Log.fail("Test case failed. E-Sign is not successful when selecting reasons from multiple meanings.[Additional info. : " + result + "]", driver);

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

	}//End ESign_44631

}
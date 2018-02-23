package MFClient.Tests.Authentications;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.Utils;
import genericLibrary.WebDriverUtils;

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
public class OAuthAuthentication {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String userName = null;
	public static String userFullName = null;
	public static String password = null;
	public static String domainName = null;
	public static String testVault = null;
	public static String testVault1 = null;
	public static String className = null;
	public static String productVersion = null;
	public static String oauthLoginURL = null;
	public static String oauthUserName = null;
	public static String oauthPassword = null;
	public static String oauthUserFullName = null;
	public static String oauthDomainName = null;
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
			oauthUserName = xmlParameters.getParameter("oauthUserName");
			oauthPassword = xmlParameters.getParameter("oauthPassword");
			oauthUserFullName = xmlParameters.getParameter("oauthUserFullName");
			oauthDomainName = xmlParameters.getParameter("oauthDomainName");
			userName = xmlParameters.getParameter("UserName");
			userFullName = xmlParameters.getParameter("UserFullName");
			password = xmlParameters.getParameter("Password");
			domainName = xmlParameters.getParameter("DomainName");
			testVault = xmlParameters.getParameter("VaultName");
			testVault1 = testVault + "1";
			className = this.getClass().getSimpleName().toString().trim();
			oauthLoginURL = xmlParameters.getParameter("oauthLoginURL");
			if (xmlParameters.getParameter("driverType").equalsIgnoreCase("IE"))
				productVersion = "M-Files " +  xmlParameters.getParameter("productVersion").trim()  + " - " +  xmlParameters.getParameter("driverType").toUpperCase().trim() + xmlParameters.getParameter("driverVersion").trim();
			else
				productVersion = "M-Files " +  xmlParameters.getParameter("productVersion").trim()  + " - " +  xmlParameters.getParameter("driverType").toUpperCase().trim();

			Utility.removeRegistryKey("HKEY_LOCAL_MACHINE\\SOFTWARE\\Motive\\M-Files\\PRODUCTVERSION\\Server\\MFServer\\Authentication");//Removes the registry settings if exists
			Utility.restoreTestVault();//Restores the vault in MFServer
			Utility.restoreTestVault(testVault1, "");
			Utility.configureUsers(xlTestDataWorkBook);	//Configures the user in server and restored vault
			Utility.configureUsers(xlTestDataWorkBook, "Users", testVault1);	//Configures the user in server and restored vault
			Utility.configureUsers(testVault, oauthUserFullName, oauthUserFullName, "windows", "named", "none", oauthDomainName, "admin", "none", "internal");//Configure the windows user in server and restored vault
			Utility.configureUsers(testVault1, oauthUserFullName, oauthUserFullName, "windows", "named", "none", oauthDomainName, "admin", "none", "internal");//Configure the windows user in server and restored vault
			Utility.configureSAMLorOAuthRegistrySettings();//Configures the oauth authentication in the machine

			if(!Utility.checkSAMLorOAuthIsConfigured("OAuth"))//Checks whether the OAuth login link is displayed in the default login page
				throw new Exception("OAuth is not configured and oauth link is not displayed in the login page.");

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
			try
			{
				Utility.removeRegistryKey("HKEY_LOCAL_MACHINE\\SOFTWARE\\Motive\\M-Files\\PRODUCTVERSION\\Server\\MFServer\\Authentication");//Removes the registry settings
				Utility.resetIIS();//Resets the iis
			}
			catch(Exception e0){}
			Utility.destroyUsers(xlTestDataWorkBook);//Destroys the user in MFServer
			Utility.destroyTestVault();//Destroys the Vault in MFServer
			Utility.destroyTestVault(testVault1);//Destroys the Vault in MFServer

		}//End try

		catch(Exception e){
			throw e;
		}//End Catch
	}//End cleanApp

	/**
	 * TC_27501: Login with correct & incorrect OAuth username/password
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "OAuth"}, 
			description = "Login with correct & incorrect OAuth username/password")
	public void TC_27501(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Gets the driver
			//---------------
			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1: Logging in using a user with no license
			//-----------------------------------------------
			driver.get(loginURL);//Launches the URL
			Utils.fluentWait(driver);//Waits for the page load

			Log.message("1. M-Files default login page is launched.");

			//Step-2: Click the OAuth authetication link in the default login page
			//-------------------------------------------------------------------
			LoginPage loginPage = new LoginPage(driver);//Instantaites the login page
			loginPage.clickLoginLink(dataPool.get("LoginType"));//Selects the login type in the login page

			if(!driver.getCurrentUrl().contains(oauthLoginURL))
				throw new Exception("oauth login page url("+ oauthLoginURL +") is not loaded.[Loaded URL: "+ driver.getCurrentUrl() +"]");

			Log.message("2. Clicked the oauth login link in the login page and Entered into the oauth login page", driver);

			if(dataPool.get("Verification").equalsIgnoreCase("Valid"))
			{
				//Step-3: Login with the oauth credentials
				//---------------------------------------
				loginPage = new LoginPage(driver);//Re-Instantiates the login page
				HomePage homePage = loginPage.loginToWebApplicationUsingSAMLorOAuth(oauthUserName, oauthPassword, testVault);

				Log.message("3. Logged into the MFWA Default page using the oauth credentials.");

				//Verification: Check if correct user name is displayed in the user menu
				//----------------------------------------------------------------------
				if(homePage.menuBar.verifyLoggedInUser(oauthUserFullName))
					Log.pass("Test case passed. Default login with oauth is working as expected.");
				else
					Log.fail("Test case failed. Default login with oauth is not working as expected.[Logged in user name is different. Expected name: '" + oauthDomainName+"\\"+oauthUserFullName + "' & Actual name: '" + homePage.menuBar.getLoggedInUserName() + "']", driver);
			}
			else
			{
				//Step-3: Enter the invalid user id/password
				//------------------------------------------
				loginPage = new LoginPage(driver);//Re-Instantiates the login page
				loginPage.setSAMLorOAuthUserName(dataPool.get("UserName"));//Enter the invalid user id
				if(!dataPool.get("UserName").contains("domain"))
				{
					loginPage.setSAMLorOAuthPassword(dataPool.get("Password"));//Clicks on the password field
					loginPage.clickSAMLorOAuthLoginBtn();//Clicks the login button
				}
				Log.message("3. Entered the invalid credentials and clicked the login button.");

				//Verification: Check if correct user name is displayed in the user menu
				//----------------------------------------------------------------------
				String expectedErrMsg = dataPool.get("ErrorMessage");

				if(loginPage.getSAMLorOAuthLoginErrorMessage().equalsIgnoreCase(expectedErrMsg))
					Log.pass("Test case passed. Error message is displayed as expected('" + expectedErrMsg + "') for the invalid credentials(UserName: '" + dataPool.get("UserName") + "' and Password: '" + dataPool.get("Password") + "').", driver);
				else
					Log.fail("Test case failed. Error message is not displayed as expected('" + expectedErrMsg + "') for the invalid credentials(UserName: '" + dataPool.get("UserName") + "' and Password: '" + dataPool.get("Password") + "'). Actual message displayed: '" + loginPage.getSAMLorOAuthLoginErrorMessage() + "'", driver);
			}
		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try
			{
				Utility.quitDriver(driver);//Quits the driver
			}
			catch(Exception e0){Log.exception(e0);}
		}//End finally

	}//End TC_27501

	/**
	 * TC_27541: Close OAuth login dialog triggered by e-signing
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "OAuth"}, 
			description = "Close OAuth login dialog triggered by e-signing")
	public void TC_27541(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Gets the driver
			//---------------
			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1: Logging in using a user with no license
			//-----------------------------------------------
			driver.get(loginURL);//Launches the URL
			Utils.fluentWait(driver);//Waits for the page load

			Log.message("1. M-Files default login page is launched.");

			//Step-2: Navigate to the OAuth/SAML login page and login
			//--------------------------------------------------------
			LoginPage loginPage = new LoginPage(driver);//Instantiates the login page
			loginPage.clickLoginLink(dataPool.get("LoginType"));//Clicks the login link in the login page

			if(!driver.getCurrentUrl().toLowerCase().contains(oauthLoginURL.toLowerCase()))
				throw new Exception("OAuth login page is not loaded. [Current URL : " + driver.getCurrentUrl() + "]");

			loginPage = new LoginPage(driver);//Instantiates the login page
			HomePage homePage = loginPage.loginToWebApplicationUsingSAMLorOAuth(oauthUserName, oauthPassword, testVault);//Logins into the vault using OAuth credentials

			Log.message("2. Logged into the MFWA using OAuth Credentials.", driver);

			//Step-3: Navigate to any view and select the object in the view
			//---------------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("SearchView"), "");//Navigate to the search view

			if(!homePage.listView.clickItem(dataPool.get("Object")))//Selects the item in the list view
				throw new Exception("Object '" + dataPool.get("Object") + "' is not selected in the navigated '" + viewToNavigate + "' view");

			Log.message("3. Navigated to the view '" + viewToNavigate + "' and selected the object '" + dataPool.get("Object") + "' in the view.");

			//Step-4: Set the workflow for the object
			//---------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard
			metadataCard.setWorkflow(dataPool.get("Workflow"));//Sets the workflow for the object
			metadataCard.saveAndClose();//Saves the changes
			metadataCard = new MetadataCard(driver, true);//Reinstantiates the metadatacard

			if(!metadataCard.getWorkflow().equalsIgnoreCase(dataPool.get("Workflow")))//Checks if workflow is set in the metadata card
				throw new Exception("Expected workflow '" + dataPool.get("Workflow") + "' is not set in the metadata card after saving the changes.");

			Log.message("4. Expected workflow '" + dataPool.get("Workflow") + "' is set in the metadata card.");

			//Step-5: Perform state transition which invokes e-sign
			//-----------------------------------------------------
			if(!homePage.taskPanel.clickItem(dataPool.get("WorkflowState")))
				throw new Exception("State '" + dataPool.get("WorkflowState") + "' is not clicked from the task panel.");

			if(!MFilesDialog.exists(driver, "Workflow"))
				throw new Exception("Workflow dialog is not displayed after click on the workflow state '" + dataPool.get("WorkflowState") + "' from task pane.");

			Log.message("5. Clicked on the workflow state '" + dataPool.get("WorkflowState") + "' from task pane and Workflow dialog is displayed.", driver);

			//Step-6: Click Ok button in the workflow state transition dialog
			//---------------------------------------------------------------
			MFilesDialog mfDialog = new MFilesDialog(driver, "Workflow");//Instantiates the MFiles Dialog
			mfDialog.clickOkButton();//Clicks ok button in the mfdialog

			if(!MFilesDialog.isESignDialogExist(driver))
				throw new Exception("E-Sign dialog is not displayed after click ok in the state transition dialog.");

			Log.message("6. Electronic signature dialog is displayed after click ok in the state transition dialog.");

			//Step-7: Click Sign button in the E-Sign dialog
			//----------------------------------------------
			mfDialog = new MFilesDialog(driver, "Electronic Signature");//Instantiates the MFiles Dialog
			mfDialog.clickButton("sign");//Clicks sign button in the mfdialog

			if(!MFilesDialog.exists(driver, "M-Files Web"))
				throw new Exception("Waiting for the electronic signature information message is not displayed after click sign button in the e-sign dialog.");

			if(!Utility.tabExists(driver, "Sign in to your account", "title"))
				throw new Exception("OAuth login dialog is not opened in the seperate tab.");

			driver = Utility.switchToTab(driver, "Sign in to your account", "title");//Navigates to the OAuth login tab

			Log.message("7. Sign button is clicked in the e-sign dialog and OAuth login dialog is opened in another tab.", driver);

			//Step-8: Close the OAuth login tab without login
			//-----------------------------------------------
			if (!Utility.closeTab(driver, "Sign in to your account", "title"))//Closes the OAuth login tab
				throw new Exception("OAuth login tab is not closed.");

			driver = Utility.switchToTab(driver, "M-Files Web", "title");//Navigates to the default tab

			Log.message("8. Closed the OAuth login tab without login.");

			//Step-9: Close the workflow dialog in the default page
			//-----------------------------------------------------
			if(MFilesDialog.exists(driver, "Workflow"))//Checks if Workflow dialog is exists in the view
				MFilesDialog.closeMFilesDialog(driver);//Closes the M-Files Dialog

			Log.message("9. Closed the Workflow dialog in the view.");

			//Verification: Check if state transition is not happened and signature property is not added in the metadatacard
			//----------------------------------------------------------------------------------------------------------------
			String result = "";
			metadataCard = new MetadataCard(driver, true);//Reinstantiates the metadatacard

			if(metadataCard.getWorkflowState().equalsIgnoreCase(dataPool.get("WorkflowState")))//Checks if workflow is set in the metadata card
				result = "Workflow state'" + dataPool.get("WorkflowState") + "' is set in the metadata card. ";

			if(metadataCard.propertyExists(dataPool.get("SignatureProperty")))//Checks if property is exists in the metadata card
				result += "Signature property('" + dataPool.get("SignatureProperty") + "') is added in the metadatacard.";

			if(result.equals(""))
				Log.pass("Test case passed. Close OAuth login dialog triggered by e-signing is working as expected.");
			else
				Log.fail("Test case failed. Close OAuth login dialog triggered by e-signing is not working as expected. [Additional info. : '" + result + "']", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try
			{
				Utility.quitDriver(driver);//Quits the driver
			}
			catch(Exception e0){Log.exception(e0);}
		}//End finally

	}//End TC_27541

	/**
	 * TC_27542: Cancel and close "Change State" and "Electronic Signature" dialogs
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "OAuth"}, 
			description = "Cancel and close \"Change State\" and \"Electronic Signature\" dialogs")
	public void TC_27542(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Gets the driver
			//---------------
			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1: Logging in using a user with no license
			//-----------------------------------------------
			driver.get(loginURL);//Launches the URL
			Utils.fluentWait(driver);//Waits for the page load

			Log.message("1. M-Files default login page is launched.");

			//Step-2: Navigate to the OAuth/SAML login page and login
			//--------------------------------------------------------
			LoginPage loginPage = new LoginPage(driver);//Instantiates the login page
			loginPage.clickLoginLink(dataPool.get("LoginType"));//Clicks the login link in the login page

			if(!driver.getCurrentUrl().toLowerCase().contains(oauthLoginURL.toLowerCase()))
				throw new Exception("OAuth login page is not loaded. [Current URL : " + driver.getCurrentUrl() + "]");

			loginPage = new LoginPage(driver);//Instantiates the login page
			HomePage homePage = loginPage.loginToWebApplicationUsingSAMLorOAuth(oauthUserName, oauthPassword, testVault);//Logins into the vault using OAuth credentials

			Log.message("2. Logged into the MFWA using OAuth Credentials.", driver);

			//Step-3: Navigate to any view and select the object in the view
			//---------------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("SearchView"), "");//Navigate to the search view

			if(!homePage.listView.clickItem(dataPool.get("Object")))//Selects the item in the list view
				throw new Exception("Object '" + dataPool.get("Object") + "' is not selected in the navigated '" + viewToNavigate + "' view");

			Log.message("3. Navigated to the view '" + viewToNavigate + "' and selected the object '" + dataPool.get("Object") + "' in the view.");

			//Step-4: Set the workflow for the object
			//---------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard
			metadataCard.setWorkflow(dataPool.get("Workflow"));//Sets the workflow for the object
			metadataCard.saveAndClose();//Saves the changes
			metadataCard = new MetadataCard(driver, true);//Reinstantiates the metadatacard

			if(!metadataCard.getWorkflow().equalsIgnoreCase(dataPool.get("Workflow")))//Checks if workflow is set in the metadata card
				throw new Exception("Expected workflow '" + dataPool.get("Workflow") + "' is not set in the metadata card after saving the changes.");

			Log.message("4. Expected workflow '" + dataPool.get("Workflow") + "' is set in the metadata card.");

			//Step-5: Perform state transition which invokes e-sign
			//-----------------------------------------------------
			if(!homePage.taskPanel.clickItem(dataPool.get("WorkflowState")))
				throw new Exception("State '" + dataPool.get("WorkflowState") + "' is not clicked from the task panel.");

			if(!MFilesDialog.exists(driver, "Workflow"))
				throw new Exception("Workflow dialog is not displayed after click on the workflow state '" + dataPool.get("WorkflowState") + "' from task pane.");

			Log.message("5. Clicked on the workflow state '" + dataPool.get("WorkflowState") + "' from task pane and Workflow dialog is displayed.", driver);

			//Step-6: Cancel/Closes the workflow dialog
			//-----------------------------------------
			MFilesDialog mfDialog = new MFilesDialog(driver, "Workflow");//Instantiates the MFiles Dialog

			if(dataPool.get("CancelType").equalsIgnoreCase("WorkflowCancel"))
			{
				mfDialog.clickCancelButton();//Clicks cancel button in the mfdialog

				Log.message("6. Cancel button is clicked on the workflow state transition dialog.");
			}
			else if(dataPool.get("CancelType").equalsIgnoreCase("WorkflowClose"))
			{
				mfDialog.clickCloseButton();//Clicks close icon in the MFDialog

				Log.message("6. Close icon is clicked on the workflow state transition dialog.");				
			}
			else if(dataPool.get("CancelType").toLowerCase().contains("esign"))
			{
				mfDialog.clickOkButton();//Clicks ok button in the mfdialog

				if(!MFilesDialog.isESignDialogExist(driver))
					throw new Exception("E-Sign dialog is not displayed after click ok in the state transition dialog.");

				mfDialog = new MFilesDialog(driver, "Electronic Signature");//Instantiates the MFiles Dialog
				if(dataPool.get("CancelType").equalsIgnoreCase("ESignClose"))
				{
					mfDialog.clickCloseButton();//Clicks the close icon in the e-sign dialog
					mfDialog = new MFilesDialog(driver, "Workflow");//Instantiates the MFiles Dialog
					mfDialog.clickCloseButton();//Closes the workflow dialog

					Log.message("6. Close icon is clicked on the e-sign and state transition dialogs.");
				}
				else
				{
					mfDialog.clickCancelButton();//Clicks the close icon in the e-sign dialog
					mfDialog = new MFilesDialog(driver, "Workflow");//Instantiates the MFiles Dialog
					mfDialog.clickCancelButton();//Closes the workflow dialog

					Log.message("6. Cancel button is clicked on the e-sign and state transition dialogs.");
				}
			}

			//Verification: Check if state transition is not happened and signature property is not added in the metadatacard
			//----------------------------------------------------------------------------------------------------------------
			String result = "";
			metadataCard = new MetadataCard(driver, true);//Reinstantiates the metadatacard

			if(metadataCard.getWorkflowState().equalsIgnoreCase(dataPool.get("WorkflowState")))//Checks if workflow is set in the metadata card
				result = "Workflow state'" + dataPool.get("WorkflowState") + "' is set in the metadata card. ";

			if(metadataCard.propertyExists(dataPool.get("SignatureProperty")))//Checks if property is exists in the metadata card
				result += "Signature property('" + dataPool.get("SignatureProperty") + "') is added in the metadatacard.";

			if(result.equals(""))
				Log.pass("Test case passed. Cancel and close \"Change State\" and \"Electronic Signature\" dialogs is working as expected.");
			else
				Log.fail("Test case failed. Cancel and close \"Change State\" and \"Electronic Signature\" dialogs is not working as expected. [Additional info. : '" + result + "']", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try
			{
				Utility.quitDriver(driver);//Quits the driver
			}
			catch(Exception e0){Log.exception(e0);}
		}//End finally

	}//End TC_27542

	/**
	 * TC_27543: Adding state change comments before cancel and close upcoming dialogs.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "OAuth"}, 
			description = "Adding state change comments before cancel and close upcoming dialogs.")
	public void TC_27543(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Gets the driver
			//---------------
			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1: Logging in using a user with no license
			//-----------------------------------------------
			driver.get(loginURL);//Launches the URL
			Utils.fluentWait(driver);//Waits for the page load

			Log.message("1. M-Files default login page is launched.");

			//Step-2: Navigate to the OAuth/SAML login page and login
			//--------------------------------------------------------
			LoginPage loginPage = new LoginPage(driver);//Instantiates the login page
			loginPage.clickLoginLink(dataPool.get("LoginType"));//Clicks the login link in the login page

			if(!driver.getCurrentUrl().toLowerCase().contains(oauthLoginURL.toLowerCase()))
				throw new Exception("OAuth login page is not loaded. [Current URL : " + driver.getCurrentUrl() + "]");

			loginPage = new LoginPage(driver);//Instantiates the login page
			HomePage homePage = loginPage.loginToWebApplicationUsingSAMLorOAuth(oauthUserName, oauthPassword, testVault);//Logins into the vault using OAuth credentials

			Log.message("2. Logged into the MFWA using OAuth Credentials.", driver);

			//Step-3: Navigate to any view and select the object in the view
			//---------------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("SearchView"), "");//Navigate to the search view

			if(!homePage.listView.clickItem(dataPool.get("Object")))//Selects the item in the list view
				throw new Exception("Object '" + dataPool.get("Object") + "' is not selected in the navigated '" + viewToNavigate + "' view");

			Log.message("3. Navigated to the view '" + viewToNavigate + "' and selected the object '" + dataPool.get("Object") + "' in the view.");

			//Step-4: Set the workflow for the object
			//---------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard
			metadataCard.setWorkflow(dataPool.get("Workflow"));//Sets the workflow for the object
			metadataCard.saveAndClose();//Saves the changes
			metadataCard = new MetadataCard(driver, true);//Reinstantiates the metadatacard

			if(!metadataCard.getWorkflow().equalsIgnoreCase(dataPool.get("Workflow")))//Checks if workflow is set in the metadata card
				throw new Exception("Expected workflow '" + dataPool.get("Workflow") + "' is not set in the metadata card after saving the changes.");

			Log.message("4. Expected workflow '" + dataPool.get("Workflow") + "' is set in the metadata card.");

			//Step-5: Perform state transition which invokes e-sign
			//-----------------------------------------------------
			if(!homePage.taskPanel.clickItem(dataPool.get("WorkflowState")))
				throw new Exception("State '" + dataPool.get("WorkflowState") + "' is not clicked from the task panel.");

			if(!MFilesDialog.exists(driver, "Workflow"))
				throw new Exception("Workflow dialog is not displayed after click on the workflow state '" + dataPool.get("WorkflowState") + "' from task pane.");

			Log.message("5. Clicked on the workflow state '" + dataPool.get("WorkflowState") + "' from task pane and Workflow dialog is displayed.", driver);

			//Step-6: Click Ok button in the workflow state transition dialog
			//---------------------------------------------------------------
			MFilesDialog mfDialog = new MFilesDialog(driver, "Workflow");//Instantiates the MFiles Dialog
			String comment = Utility.getObjectName(methodName);//Frames the comment
			mfDialog.setWorkflowComments(comment);//Sets the comment in the state transition dialog
			mfDialog.clickOkButton();//Clicks ok button in the mfdialog

			if(!MFilesDialog.isESignDialogExist(driver))
				throw new Exception("E-Sign dialog is not displayed after click ok in the state transition dialog.");

			Log.message("6. Electronic signature dialog is displayed after setting comment and clicks ok in the state transition dialog.");

			//Step-7: Click Sign button in the E-Sign dialog
			//----------------------------------------------
			mfDialog = new MFilesDialog(driver, "Electronic Signature");//Instantiates the MFiles Dialog
			if(dataPool.get("CancelType").equalsIgnoreCase("ESignCancel"))
			{
				mfDialog.clickButton("cancel");//Clicks cancel button in the mfdialog
				Log.message("7. Cancel button is clicked from the E-Sign dialog");
			}
			else
			{
				mfDialog.clickButton("sign");//Clicks sign button in the mfdialog

				if(!Utility.closeTab(driver, "Sign in to your account", "title"))
					throw new Exception("OAuth login dialog is not closed.");

				Log.message("7. After click sign button from the E-Sign dialog, OAuth login window is closed.");
			}

			//Step-8: Close the workflow dialog
			//---------------------------------
			if(MFilesDialog.exists(driver, "Workflow"))//Checks if Workflow dialog is exists in the view
				MFilesDialog.closeMFilesDialog(driver);//Closes the M-Files Dialog

			Log.message("8. Closed the Workflow dialog in the view.");

			//Verification: Check if state transition is not happened and signature property is not added in the metadatacard
			//----------------------------------------------------------------------------------------------------------------
			String result = "";
			metadataCard = new MetadataCard(driver, true);//Reinstantiates the metadatacard
			ArrayList<String> comments = metadataCard.getComments();
			if(metadataCard.getWorkflowState().equalsIgnoreCase(dataPool.get("WorkflowState")))//Checks if workflow is set in the metadata card
				result = "Workflow state'" + dataPool.get("WorkflowState") + "' is set in the metadata card. ";

			if(comments.size() > 0)//Checks if comment is added in the metadata card
				if(comments.get(comments.size()-1).equalsIgnoreCase(comment))
					result += "Comment('" + comment + "') is added in the metadatacard.";

			//Verification: Confirms if cancelling e-sign works as expected
			//--------------------------------------------------------------
			if(result.equals(""))
				Log.pass("Test case passed. Adding state change comments before cancel and close upcoming dialogs is working as expected.");
			else
				Log.fail("Test case failed. Adding state change comments before cancel and close upcoming dialogs is not working as expected. [Additional info. : '" + result + "']", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try
			{
				Utility.quitDriver(driver);//Quits the driver
			}
			catch(Exception e0){Log.exception(e0);}
		}//End finally

	}//End TC_27543

	/**
	 * TC_27544: E-signature with incorrect OAuth credentials
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "OAuth"}, 
			description = "E-signature with incorrect OAuth credentials")
	public void TC_27544(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Gets the driver
			//---------------
			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1: Logging in using a user with no license
			//-----------------------------------------------
			driver.get(loginURL);//Launches the URL
			Utils.fluentWait(driver);//Waits for the page load

			Log.message("1. M-Files default login page is launched.");

			//Step-2: Navigate to the OAuth/SAML login page and login
			//--------------------------------------------------------
			LoginPage loginPage = new LoginPage(driver);//Instantiates the login page
			loginPage.clickLoginLink(dataPool.get("LoginType"));//Clicks the login link in the login page

			if(!driver.getCurrentUrl().toLowerCase().contains(oauthLoginURL.toLowerCase()))
				throw new Exception("OAuth login page is not loaded. [Current URL : " + driver.getCurrentUrl() + "]");

			loginPage = new LoginPage(driver);//Instantiates the login page
			HomePage homePage = loginPage.loginToWebApplicationUsingSAMLorOAuth(oauthUserName, oauthPassword, testVault);//Logins into the vault using OAuth credentials

			Log.message("2. Logged into the MFWA using OAuth Credentials.", driver);

			//Step-3: Navigate to any view and select the object in the view
			//---------------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("SearchView"), "");//Navigate to the search view

			if(!homePage.listView.clickItem(dataPool.get("Object")))//Selects the item in the list view
				throw new Exception("Object '" + dataPool.get("Object") + "' is not selected in the navigated '" + viewToNavigate + "' view");

			Log.message("3. Navigated to the view '" + viewToNavigate + "' and selected the object '" + dataPool.get("Object") + "' in the view.");

			//Step-4: Set the workflow for the object
			//---------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard
			metadataCard.setWorkflow(dataPool.get("Workflow"));//Sets the workflow for the object
			metadataCard.saveAndClose();//Saves the changes
			metadataCard = new MetadataCard(driver, true);//Reinstantiates the metadatacard

			if(!metadataCard.getWorkflow().equalsIgnoreCase(dataPool.get("Workflow")))//Checks if workflow is set in the metadata card
				throw new Exception("Expected workflow '" + dataPool.get("Workflow") + "' is not set in the metadata card after saving the changes.");

			Log.message("4. Expected workflow '" + dataPool.get("Workflow") + "' is set in the metadata card.");

			//Step-5: Perform state transition which invokes e-sign
			//-----------------------------------------------------
			if(!homePage.taskPanel.clickItem(dataPool.get("WorkflowState")))
				throw new Exception("State '" + dataPool.get("WorkflowState") + "' is not clicked from the task panel.");

			if(!MFilesDialog.exists(driver, "Workflow"))
				throw new Exception("Workflow dialog is not displayed after click on the workflow state '" + dataPool.get("WorkflowState") + "' from task pane.");

			Log.message("5. Clicked on the workflow state '" + dataPool.get("WorkflowState") + "' from task pane and Workflow dialog is displayed.", driver);

			//Step-6: Click Ok button in the workflow state transition dialog
			//---------------------------------------------------------------
			MFilesDialog mfDialog = new MFilesDialog(driver, "Workflow");//Instantiates the MFiles Dialog
			mfDialog.clickOkButton();//Clicks ok button in the mfdialog

			if(!MFilesDialog.isESignDialogExist(driver))
				throw new Exception("E-Sign dialog is not displayed after click ok in the state transition dialog.");

			Log.message("6. Electronic signature dialog is displayed after click ok in the state transition dialog.", driver);

			//Step-7: Click Sign button in the E-Sign dialog
			//----------------------------------------------
			mfDialog = new MFilesDialog(driver, "Electronic Signature");//Instantiates the MFiles Dialog
			mfDialog.clickButton("sign");//Clicks sign button in the mfdialog

			if(!MFilesDialog.exists(driver, "M-Files Web"))
				throw new Exception("Waiting for the electronic signature information message is not displayed after click sign button in the e-sign dialog.");

			if(!Utility.tabExists(driver, "Sign in to your account", "title"))
				throw new Exception("OAuth login dialog is not opened in the seperate tab.");

			driver = Utility.switchToTab(driver, "Sign in to your account", "title");//Navigates to the OAuth login tab

			Log.message("7. Sign button is clicked in the e-sign dialog and OAuth login dialog is opened in another tab.", driver);

			//Step-8: Login using invalid credentials
			//---------------------------------------
			loginPage = new LoginPage(driver);//Instantiates the login page
			loginPage.setSAMLorOAuthUserName(oauthUserName+"invalid");//Sets the user name
			//In New MicroSoft login page, Without entering valid username its not possible to move to the password entering field
			//loginPage.setSAMLorOAuthPassword(oauthPassword+"invalid");//Sets the password
			//loginPage.clickSAMLorOAuthLoginBtn();//Clicks the login button

			Log.message("8. Invalid username entered and clicked the next button.", driver);

			//Step-9: Close the workflow dialog in the default page
			//-----------------------------------------------------
			driver = Utility.switchToTab(driver, "M-Files Web", "title");//Navigates to the M-Files tab

			mfDialog = new MFilesDialog(driver, "M-Files Web");//Instantiates the MFiles Dialog
			mfDialog.clickButton("cancel");//Clicks the cancel button

			if(!MFilesDialog.exists(driver, "Workflow"))//Checks if Workflow dialog is exists in the view
				MFilesDialog.closeMFilesDialog(driver);//Closes the M-Files Dialog

			Log.message("9. Closed the Workflow dialog in the view.", driver);

			//Verification: Check if state transition is not happened and signature property is not added in the metadatacard
			//----------------------------------------------------------------------------------------------------------------
			String result = "";
			metadataCard = new MetadataCard(driver, true);//Reinstantiates the metadatacard

			if(metadataCard.getWorkflowState().equalsIgnoreCase(dataPool.get("WorkflowState")))//Checks if workflow is set in the metadata card
				result = "Workflow state'" + dataPool.get("WorkflowState") + "' is set in the metadata card. ";

			if(metadataCard.propertyExists(dataPool.get("SignatureProperty")))//Checks if property is exists in the metadata card
				result += "Signature property('" + dataPool.get("SignatureProperty") + "') is added in the metadatacard.";

			if(result.equals(""))
				Log.pass("Test case passed. E-signature with incorrect OAuth credentials is working as expected.");
			else
				Log.fail("Test case failed. E-signature with incorrect OAuth credentials is not working as expected. [Additional info. : '" + result + "']", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try
			{
				Utility.quitDriver(driver);//Quits the driver
			}
			catch(Exception e0){Log.exception(e0);}
		}//End finally

	}//End TC_27544

	/**
	 * TC_27545: No rights to make state transition
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "OAuth"}, 
			description = "No rights to make state transition")
	public void TC_27545(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Gets the driver
			//---------------
			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-Requisite: Clear the registry settings (Due to issue #142993)
			//-----------------------------------------------------------------
			Utility.removeRegistryKey("HKEY_LOCAL_MACHINE\\SOFTWARE\\Motive\\M-Files\\PRODUCTVERSION\\Server\\MFServer\\Authentication");//Removes the registry settings

			//Pre-Requisite: Update the user as non-sysadmin user
			//---------------------------------------------------
			Utility.markLoginAccountAsAdmin(oauthDomainName + "\\" + oauthUserFullName, "nonadmin");//Marks the user as non-admin in the server

			Log.message("Pre-Requisite: Marked the user '" + oauthDomainName + "\\" + oauthUserFullName + "' as non-admin in the server.");

			//Pre-Requisite: Configure the SAML
			//---------------------------------
			Utility.configureSAMLorOAuthRegistrySettings();//Configures the SAML authentication in the machine

			//Step-1: Logging in using a user with no license
			//-----------------------------------------------
			driver.get(loginURL);//Launches the URL
			Utils.fluentWait(driver);//Waits for the page load

			Log.message("1. M-Files default login page is launched.");

			//Step-2: Navigate to the OAuth login page and login
			//--------------------------------------------------
			LoginPage loginPage = new LoginPage(driver);//Instantiates the login page
			loginPage.clickLoginLink(dataPool.get("LoginType"));//Clicks the login link in the login page

			if(!driver.getCurrentUrl().toLowerCase().contains(oauthLoginURL.toLowerCase()))
				throw new Exception("OAuth login page is not loaded. [Current URL : " + driver.getCurrentUrl() + "]");

			loginPage = new LoginPage(driver);//Instantiates the login page
			HomePage homePage = loginPage.loginToWebApplicationUsingSAMLorOAuth(oauthUserName, oauthPassword, testVault);//Logins into the vault using OAuth credentials

			Log.message("2. Logged into the MFWA using OAuth Credentials.", driver);

			//Step-3: Navigate to any view and select the object in the view
			//---------------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("SearchView"), "");//Navigate to the search view

			if(!homePage.listView.clickItem(dataPool.get("Object")))//Selects the item in the list view
				throw new Exception("Object '" + dataPool.get("Object") + "' is not selected in the navigated '" + viewToNavigate + "' view");

			Log.message("3. Navigated to the view '" + viewToNavigate + "' and selected the object '" + dataPool.get("Object") + "' in the view.");

			//Step-4: Set the workflow for the object
			//---------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard
			metadataCard.setWorkflow(dataPool.get("Workflow"));//Sets the workflow for the object
			metadataCard.saveAndClose();//Saves the changes
			metadataCard = new MetadataCard(driver, true);//Reinstantiates the metadatacard

			if(!metadataCard.getWorkflow().equalsIgnoreCase(dataPool.get("Workflow")))//Checks if workflow is set in the metadata card
				throw new Exception("Expected workflow '" + dataPool.get("Workflow") + "' is not set in the metadata card after saving the changes.");

			Log.message("4. Expected workflow '" + dataPool.get("Workflow") + "' is set in the metadata card.");

			//Verification: Check if denied state is displayed for the user in task pane and metadata card
			//---------------------------------------------------------------------------------------------
			String result = "";

			if(homePage.taskPanel.isItemExists(dataPool.get("WorkflowState")))//Checks if denied state is displayed in the task pane
				result = "Denied workflow state(" + dataPool.get("WorkflowState") + ") is displayed in the task pane for the user; ";

			if(metadataCard.isWorkflowStateEnabled(dataPool.get("WorkflowState")))//Checks if denied state is displayed in the metadata card
				result += "Denied workflow state(" + dataPool.get("WorkflowState") + ") is enabled in the metadata card for the user,";

			if(result.equals(""))
				Log.pass("Test case passed. Denied workflow state transition is not displayed for the user logged in using OAuth as expected.");
			else
				Log.fail("Test case failed. Denied workflow state transition is displayed for the user logged in using OAuth. [Additional info.: " + result.trim() + "]", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try
			{
				Utility.removeRegistryKey("HKEY_LOCAL_MACHINE\\SOFTWARE\\Motive\\M-Files\\PRODUCTVERSION\\Server\\MFServer\\Authentication");//Removes the registry settings
				Utility.configureUsers(testVault, oauthUserFullName, oauthUserFullName, "windows", "named", "none", oauthDomainName, "admin", "none", "internal");//Configure the windows user in server and restored vault
				Utility.configureUsers(testVault1, oauthUserFullName, oauthUserFullName, "windows", "named", "none", oauthDomainName, "admin", "none", "internal");//Configure the windows user in server and restored vault
				Utility.configureSAMLorOAuthRegistrySettings();//Configures the SAML authentication in the machine
				Utility.quitDriver(driver);//Quits the driver
			}
			catch(Exception e0){Log.exception(e0);}
		}//End finally

	}//End TC_27545

	/**
	 * TC_29112: E-Sign multiple objects at the same time with multiselect
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "OAuth", "SKIP_MultiSelect"}, 
			description = "E-signature with incorrect OAuth credentials")
	public void TC_29112(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Gets the driver
			//---------------
			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1: Logging in using a user with no license
			//-----------------------------------------------
			driver.get(loginURL);//Launches the URL
			Utils.fluentWait(driver);//Waits for the page load

			Log.message("1. M-Files default login page is launched.");

			//Step-2: Navigate to the OAuth/SAML login page and login
			//--------------------------------------------------------
			LoginPage loginPage = new LoginPage(driver);//Instantiates the login page
			loginPage.clickLoginLink(dataPool.get("LoginType"));//Clicks the login link in the login page

			if(!driver.getCurrentUrl().toLowerCase().contains(oauthLoginURL.toLowerCase()))
				throw new Exception("OAuth login page is not loaded. [Current URL : " + driver.getCurrentUrl() + "]");

			loginPage = new LoginPage(driver);//Instantiates the login page
			HomePage homePage = loginPage.loginToWebApplicationUsingSAMLorOAuth(oauthUserName, oauthPassword, testVault);//Logins into the vault using OAuth credentials

			Log.message("2. Logged into the MFWA using OAuth Credentials.", driver);

			//Step-3: Navigate to any view and select the object in the view
			//---------------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("SearchView"), "");//Navigate to the search view

			homePage.listView.clickMultipleItems(dataPool.get("Objects"));//Selects the item in the list view

			Log.message("3. Navigated to the view '" + viewToNavigate + "' and selected the objects '" + dataPool.get("Objects") + "' in the view.");

			//Step-4: Set the workflow for the object
			//---------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard
			metadataCard.setWorkflow(dataPool.get("Workflow"));//Sets the workflow for the object
			metadataCard.saveAndClose();//Saves the changes
			metadataCard = new MetadataCard(driver, true);//Reinstantiates the metadatacard

			if(!metadataCard.getWorkflow().equalsIgnoreCase(dataPool.get("Workflow")))//Checks if workflow is set in the metadata card
				throw new Exception("Expected workflow '" + dataPool.get("Workflow") + "' is not set in the metadata card after saving the changes.");

			Log.message("4. Expected workflow '" + dataPool.get("Workflow") + "' is set in the metadata card.");

			//Step-5: Perform state transition which invokes e-sign
			//-----------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);//Clicks the workflow option from the operation menu

			if(!MFilesDialog.exists(driver, "Workflow"))
				throw new Exception("Workflow dialog is not displayed after click on the workflow option from operation menu.");

			Log.message("5. Workflow dialog is opened via operation menu for the selected objects.", driver);

			//Step-6: Perform state transition which triggers electronic signature
			//--------------------------------------------------------------------
			MFilesDialog mfDialog = new MFilesDialog(driver, "Workflow");//Instantiates the MFiles Dialog
			mfDialog.setWorkflowState(dataPool.get("WorkflowState"));//Sets the workflow state in the workflow dialog
			mfDialog.clickOkButton();//Clicks ok button in the mfdialog

			Log.message("6. Performed state transition to the workflow state '' via workflow dialog which triggers electronic signature.");

			//Step-7: Perform E-Sgin operation
			//--------------------------------
			for (int i = 0; i < (dataPool.get("Objects").split("\n")).length; i++)
			{
				if(!MFilesDialog.isESignDialogExist(driver))
					throw new Exception("E-Sign dialog is not displayed for the '" + i + "' object .");

				mfDialog = new MFilesDialog(driver, "Electronic Signature");//Instantiates the MFiles Dialog
				mfDialog.clickButton("sign");//Clicks sign button in the mfdialog

				if(!MFilesDialog.exists(driver, "M-Files Web"))
					throw new Exception("Waiting for the electronic signature information message is not displayed after click sign button in the e-sign dialog for the '" + i + "' object.");

				if(!Utility.tabExists(driver, "Sign in to your account", "title"))
					throw new Exception("OAuth login dialog is not opened in the seperate tab.");

				driver = Utility.switchToTab(driver, "Sign in to your account", "title");//Navigates to the OAuth login tab

				loginPage = new LoginPage(driver);//Instantiates the login page
				loginPage.setSAMLorOAuthUserName(oauthUserName);//Sets the user name
				loginPage.setSAMLorOAuthPassword(oauthPassword);//Sets the password
				try
				{
					loginPage.clickSAMLorOAuthLoginBtn();//Clicks the login button
					Utils.fluentWait(driver);
				}catch(Exception e0){}				

				driver = Utility.switchToTab(driver, "M-Files Web", "title");//Navigates to the M-Files tab

				Log.message("7." + i + " Object '" + (dataPool.get("Objects").split("\n"))[i] + "' is e-signed using OAuth Credentials.");				
			}

			//Verification: Check if state transition is happened and signature property is added in the metadatacard
			//----------------------------------------------------------------------------------------------------------------
			String result = "";
			String object = "";
			homePage.taskPanel.clickItem(Caption.MenuItems.RecentlyAccessedByMe.Value);//Navigates to the recently accessed by me view

			for (int j = 0; j < (dataPool.get("Objects").split("\n")).length; j++)
			{
				object = (dataPool.get("Objects").split("\n"))[j];

				if(!homePage.listView.clickItem(object))
					throw new Exception("Object '" + object + "' is not selected from the list view.");

				metadataCard = new MetadataCard(driver, true);//Reinstantiates the metadatacard

				if(!metadataCard.getWorkflowState().equalsIgnoreCase(dataPool.get("WorkflowState")))//Checks if workflow is set in the metadata card
					result += "Workflow state'" + dataPool.get("WorkflowState") + "' is not set in the metadata card of object '" + object + "'. ";

				if(!metadataCard.propertyExists(dataPool.get("SignatureProperty")))//Checks if property is exists in the metadata card
					result += "Signature property('" + dataPool.get("SignatureProperty") + "') is not added in the metadatacard of object '" + object + "'.";

			}	

			//Verification: Confirm whether state transition with e-sign successfully completed
			//---------------------------------------------------------------------------------
			if(result.equals(""))
				Log.pass("Test case passed. E-Sign multiple objects at the same time with multiselect is working as expected.");
			else
				Log.fail("Test case failed. E-Sign multiple objects at the same time with multiselect is not working as expected. [Additional info. : '" + result + "']", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try
			{
				Utility.quitDriver(driver);//Quits the driver
			}
			catch(Exception e0){Log.exception(e0);}
		}//End finally

	}//End TC_29112

}//End of OAuthAuthentication
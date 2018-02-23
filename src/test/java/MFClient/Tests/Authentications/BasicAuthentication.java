package MFClient.Tests.Authentications;

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
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import MFClient.Pages.HomePage;
import MFClient.Pages.LoginPage;
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class BasicAuthentication {

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
	public static String testVault1 = null;
	public static String testVault2 = null;
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
			testVault1 = testVault + "1";
			testVault2 = testVault + "2";
			className = this.getClass().getSimpleName().toString().trim();
			if (xmlParameters.getParameter("driverType").equalsIgnoreCase("IE"))
				productVersion = "M-Files " +  xmlParameters.getParameter("productVersion").trim()  + " - " +  xmlParameters.getParameter("driverType").toUpperCase().trim() + xmlParameters.getParameter("driverVersion").trim();
			else
				productVersion = "M-Files " +  xmlParameters.getParameter("productVersion").trim()  + " - " +  xmlParameters.getParameter("driverType").toUpperCase().trim();

			Utility.restoreTestVault();//Restores the test vault
			Utility.restoreTestVault(testVault1, "");//Restores the test vault 1
			Utility.configureUsers(xlTestDataWorkBook);//Configure the users in test vault
			Utility.configureUsers(xlTestDataWorkBook, "Users", testVault1);//Configure the users in test vault1
			try {Utility.configureUsers(testVault, windowsUserName, windowsUserFullName, "windows", "named", "none", domainName, "admin", "FullControl", "internal");}catch(Exception e0) {Log.exception(e0, driver);}
			try {Utility.configureUsers(testVault1, windowsUserName, windowsUserFullName, "windows", "named", "none", domainName, "admin", "FullControl", "internal");}catch(Exception e0) {Log.exception(e0, driver);}

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
			Utility.destroyUsers(xlTestDataWorkBook);//Destroys the user in MFServer
			Utility.destroyTestVault();//Destroys the testVault in MFServer
			Utility.destroyTestVault(testVault1);//Destroys the testVault1 in MFServer

		}//End try

		catch(Exception e){
			throw e;
		}//End Catch
	}//End cleanApp

	/**
	 * TC_023 : Login/Logout as User with No license
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "NoLicense"}, 
			description = "Login/Logout as User with No license")
	public void TC_023(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			LoginPage loginPage = new LoginPage(driver);//Instantiates the login page
			loginPage.setUserName(dataPool.get("UserName"));//Sets the user name
			loginPage.setPassword(dataPool.get("Password"));//Sets the password
			Utils.fluentWait(driver);
			loginPage.clickLoginBtn();//Clicks the login button
			Utils.fluentWait(driver);

			Log.message("1. Clicked the login button after entering the credentials of the user with no license.");

			//Verification : Verify if log in is successful 
			//----------------------------------------------
			if (loginPage.getErrorMessage().equalsIgnoreCase(dataPool.get("ErrorMessage")))
				Log.pass("Test case Passed. Login is not successfully for the user with no license.", driver);
			else
				Log.fail("Test case Failed. Expected error message is displayed in the login page for the user with no license.", driver);

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

	}//End TC_023

	/**
	 * TC_025 : Try to login in to Web Access while the vault is in offline mode
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Offline"}, 
			description = "Try to login in to Web Access while the vault is in offline mode")
	public void TC_025(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Gets the driver
			//---------------
			driver = WebDriverUtils.getDriver();
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-Requisite: Take the vault to offline
			//----------------------------------------
			Utility.takeVaultOfflineAndBringOnline(testVault, "Offline");//Takes the vault to offline
			Utility.takeVaultOfflineAndBringOnline(testVault1, "Offline");//Takes the vault to offline

			Log.message("Pre-Requisite: Vault '" + testVault + "' is moved to offline successfully");

			//Step-1: Logging in using a user with no license
			//-----------------------------------------------
			driver.get(loginURL);//Launches the URL
			Utils.fluentWait(driver);//Waits for the page load
			LoginPage loginPage = new LoginPage(driver);//Instantiates the login page
			loginPage.setUserName(userName);//Sets the user name
			loginPage.setPassword(password);//Sets the password
			Utils.fluentWait(driver);
			loginPage.clickLoginBtn();//Clicks the login button
			Utils.fluentWait(driver);

			Log.message("1. Clicked the login button after entering the credentials of the user when vault is offline.");

			//Verification : Verify if log in is successful 
			//----------------------------------------------
			if (loginPage.getErrorMessage().equalsIgnoreCase(dataPool.get("ErrorMessage")))
				Log.pass("Test case Passed. Login is not successful for the user when vault is offline.", driver);
			else
				Log.fail("Test case Failed. Expected error message is displayed in the login page for the user when vault is offline.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try
			{
				Utility.takeVaultOfflineAndBringOnline(testVault, "Online");//Takes the vault to online
				Utility.takeVaultOfflineAndBringOnline(testVault1, "Online");//Takes the vault to online
				Utility.quitDriver(driver);
			}
			catch(Exception e0){Log.exception(e0);}

		}//End finally

	}//End TC_025

	/**
	 * TC_026 : Login in to Web Access while one vault is in offline mode and there is multiple vaults on server
	 */
	@Test(groups = { "Offline"}, 
			description = "Login in to Web Access while one vault is in offline mode and there is multiple vaults on server")
	public void TC_026() throws Exception {

		driver = null; 

		try {

			//Gets the driver
			//---------------
			driver = WebDriverUtils.getDriver();

			//Pre-Requisite: Take the vault to offline
			//----------------------------------------
			Utility.restoreTestVault(testVault2, "");//Restores the test vault 2
			Utility.configureUsers(xlTestDataWorkBook, "Users", testVault1);//Configure the users in test vault1
			Utility.takeVaultOfflineAndBringOnline(testVault2, "Offline");//Takes the vault to offline

			Log.message("Pre-Requisite: Vault '" + testVault2 + "' is moved to offline successfully");

			//Step-1: Logging in using a user with no license
			//-----------------------------------------------
			driver.get(loginURL);//Launches the URL
			Utils.fluentWait(driver);//Waits for the page load
			LoginPage loginPage = new LoginPage(driver);//Instantiates the login page
			loginPage.setUserName(userName);//Sets the user name
			loginPage.setPassword(password);//Sets the password
			Utils.fluentWait(driver);
			loginPage.clickLoginBtn();//Clicks the login button
			Utils.fluentWait(driver);

			Log.message("1. Clicked the login button after entering the credentials of the user when vault is offline.");

			//Verification : Verify if log in is successful 
			//----------------------------------------------
			String[] vaults = loginPage.getVaultList();//Gets the vault list displayed
			boolean exists = false;

			for(int i = 0; i < vaults.length; i++)
				if(vaults[i].equalsIgnoreCase(testVault2))
				{
					exists = true;
					break;
				}
			if(!exists)
				Log.pass("Test case Passed. Offline vault is not displayed.", driver);
			else
				Log.fail("Test case Failed. Offline vault is displayed in the login page select vault list.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try
			{
				Utility.takeVaultOfflineAndBringOnline(testVault2, "Online");//Takes the vault to online
				Utility.destroyTestVault(testVault2);
				Utility.quitDriver(driver);
			}
			catch(Exception e0){Log.exception(e0);}

		}//End finally

	}//End TC_026

	/**
	 * TC_027: Login in to Web Access while the user does not have account in every vault in Server
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { ""}, 
			description = "Login in to Web Access while the user does not have account in every vault in Server")
	public void TC_027(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {

			//Gets the driver
			//---------------
			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1: Launch the login URL
			//----------------------------
			driver.get(loginURL);//Launches the URL
			Utils.fluentWait(driver);//Waits for the page load

			Log.message("1. M-Files default login page is launched.");

			//Pre-Requisite: Move vault to offline
			//------------------------------------
			if(dataPool.get("VaultOffline").equalsIgnoreCase("Offline"))
			{
				Utility.takeVaultOfflineAndBringOnline(testVault1, "Offline");
				Log.message("Pre-Requisite: Vault '" + testVault1 + "' is moved to offline.");
			}			

			//Step-2: Login with the credentials
			//----------------------------------
			LoginPage loginPage = new LoginPage(driver);//Instantaites the login page
			loginPage = new LoginPage(driver);//Re-Instantiates the login page
			loginPage.setUserName(userName);//Enter the user id
			loginPage.setPassword(password);//enter the password
			Utils.fluentWait(driver);
			loginPage.clickLoginBtn();//Clicks the login button
			Utils.fluentWait(driver, 750);//Waits for the page load

			Log.message("2. Logged in using Default credentials in default Login page.");

			if(dataPool.get("VaultOffline").equalsIgnoreCase("Online"))
			{
				//Step-3: Select the vault in the vault list in login page
				//--------------------------------------------------------
				loginPage = new LoginPage(driver);//Re-Instantiates the login page

				if(!loginPage.isVaultListDisplayed())
					throw new Exception("Test case failed. Vault list is not displayed in login page when user have access to several vaults.");

				loginPage.selectVault(testVault1);//Selects the vault in the login page

				Log.message("3. Vault list is displayed when user have access to several vaults and Vault - '" + testVault1 + "' is selected in the login page.");
			}
			//Verification: Check if correct user name is displayed in the user menu
			//----------------------------------------------------------------------
			HomePage homePage = new HomePage(driver);//Instantiates the Home page
			if(homePage.menuBar.verifyLoggedInUser(userFullName))
				Log.pass("Test case passed. Default login and access to several vaults is working as expected.");
			else
				Log.fail("Test case failed. Default login and access to several vaults is not working as expected.[Logged in user name is different.]", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try
			{
				if(dataPool.get("VaultOffline").equalsIgnoreCase("Offline"))
					Utility.takeVaultOfflineAndBringOnline(testVault1, "Online");

				Utility.quitDriver(driver);//Quits the driver
			}
			catch(Exception e0){Log.exception(e0);}
		}//End finally

	}//End TC_027

	/**
	 * TC_895 : Log in to Web Access as Windows user
	 */
	@Test(groups = { "WindowsUser"}, description = "Log in to Web Access as Windows user")
	public void TC_895() throws Exception {

		driver = null; 

		try {

			//Gets the driver
			//---------------
			driver = WebDriverUtils.getDriver();

			//Step-1: Login using windows user credentials
			//---------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, windowsUserName, windowsPassword, testVault);

			Log.message("1. Logged into the MFWA using windows user '" + windowsUserName + "'");

			//Step-2: Logout from the webaccess
			//---------------------------------
			homePage.menuBar.logOutFromMenuBar();//Logs out from the MFWA

			Log.message("2. Logged out from the MFWA");

			//Verification: Check if user is logged out successfully
			//------------------------------------------------------
			if(driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				Log.pass("Test case passed. Log in and log out to Web Access as Windows user successfully.");
			else
				Log.fail("Test case failed. Logout is not successful for the windows user.");


		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try
			{
				Utility.quitDriver(driver);
			}
			catch(Exception e0){Log.exception(e0);}

		}//End finally

	}//End TC_895

	/**
	 * TC_901 : Possibility to change password for M-Files users (std / ext).
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "ChangePassword"}, 
			description = "Possibility to change password for M-Files users (std / ext).")
	public void TC_901(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Gets the driver
			//---------------
			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), testVault);

			Log.message("Pre-requsite : Browser is opened and logged into MFWA. ( User Name : " + dataPool.get("UserName") + "; Vault : " + testVault + ")");

			//Step-1 : Change the password
			//-----------------------------------------
			homePage.menuBar.changePassword(dataPool.get("Password"), dataPool.get("NewPassword")); //Selects log out from user display

			Log.message("1. Change password dialog is opened and new password is set to the user.");

			//Step-2 : Check if the expected message dialog is displayed 
			//----------------------------------------------------------
			if(!MFilesDialog.exists(driver))
				throw new Exception("MFiles Password change confirmation message dialog is not displayed.");

			MFilesDialog mfilesDialog = new MFilesDialog (driver);

			if(!mfilesDialog.getMessage().equalsIgnoreCase("Your password has been changed. You will be logged out automatically. Please log in to M-Files with the new password."))
				throw new Exception("MFiles dialog is not displayed with the message 'Your password has been changed. You will be logged out automatically. Please log in to M-Files with the new password.'.");

			Log.message("2.1. Change password confirmation dialog is displayed with the expected message('Your password has been changed. You will be logged out automatically. Please log in to M-Files with the new password.')", driver);

			mfilesDialog.clickOkButton();//Click the ok button M-files dialog
			Utils.fluentWait(driver);

			Log.message("2.2. OK button is clicked in the message dialog");

			//Step-3 : Login with new password
			//--------------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("NewPassword"), testVault);

			Log.message("3. Logged into the MFWA using a newly changed credentials.");

			//Verification : Verify if log in is successful with new password
			//---------------------------------------------------------------
			if (driver.getCurrentUrl().toUpperCase().contains("DEFAULT.ASPX"))
				Log.pass("Test case Passed. M-Files web access is logged in successfully with new password for the user '" + dataPool.get("UserName") + "'.");
			else
				Log.fail("Test case Failed. M-Files web access is not logged in with new password for the user '" + dataPool.get("UserName") + "'.", driver);

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

	}//End TC_901

	/**
	 * TC_902 : Changing the password is prevented via registry setting
	 */
	@Test(groups = {"ChangePassword"}, description = "Changing the password is prevented via registry setting")
	public void TC_902() throws Exception {

		driver = null; 

		try {

			//Gets the driver
			//---------------
			driver = WebDriverUtils.getDriver();

			//Pre-Requisite: Set the registry key to disable the show/hide the reset password option
			//--------------------------------------------------------------------------------------
			Utility.setShowChangePasswordRegistry(0);//Disables the ShowChangePassword

			Log.message("Pre-Requisite: 'ShowChangePassword' regsitry key value is set as 0.");

			//Step-1 : Login to MFiles web access
			//-------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches driver and logging in

			Log.message("1. Logged into MFWA.");

			//Verification: Check if reset password is available in the menu
			//---------------------------------------------------------------
			if(!homePage.menuBar.isItemEnabledInUserSettings("Change Password"))
				Log.pass("Test case passed. Changing the password is prevented via registry setting successfully.", driver);
			else
				Log.fail("Test case failed. Changing the password is not prevented via registry setting.[Change password option is displayed even though it was restricted in registry]", driver);


		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try
			{
				Utility.setShowChangePasswordRegistry(1);//Enables the ShowChangePassword
				Utility.quitDriver(driver);//Quits the driver
			}
			catch(Exception e0){Log.exception(e0);}
		}//End finally

	}//End TC_902

	/**
	 * TC_18148 : Try to login in to Web Access while all of several vaults are in offline mode
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Offline"}, 
			description = "Try to login in to Web Access while all of several vaults are in offline mode")
	public void TC_18148(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		String[] serverVaults = null;
		try {

			//Gets the driver
			//---------------
			driver = WebDriverUtils.getDriver();
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-Requisite: Take the vault to offline
			//----------------------------------------
			serverVaults = Utility.getServerVaults().split("::");//Gets the server vaults
			String vaults = "";

			for(int i = 0; i < serverVaults.length; i++)
			{
				vaults += serverVaults[i].trim();
				Utility.takeVaultOfflineAndBringOnline(serverVaults[i].trim(), "Offline");//Takes the vault to offline

				if(i < (serverVaults.length-1))
					vaults += ",";
			}

			Log.message("Pre-Requisite: Vaults :'" + vaults + "' is moved to offline successfully");

			//Step-1: Logging in using a user with no license
			//-----------------------------------------------
			driver.get(loginURL);//Launches the URL
			Utils.fluentWait(driver);//Waits for the page load
			LoginPage loginPage = new LoginPage(driver);//Instantiates the login page
			loginPage.setUserName(userName);//Sets the user name
			loginPage.setPassword(password);//Sets the password
			Utils.fluentWait(driver);
			loginPage.clickLoginBtn();//Clicks the login button
			Utils.fluentWait(driver);

			Log.message("1. Clicked the login button after entering the credentials of the user when all the vaults in the server is offline.");

			//Verification : Verify if log in is successful 
			//----------------------------------------------
			if (loginPage.getErrorMessage().equalsIgnoreCase(dataPool.get("ErrorMessage")))
				Log.pass("Test case Passed. Login is not successful for the user when all the vaults in the server is offline.", driver);
			else
				Log.fail("Test case Failed. Expected error message is displayed in the login page for the user when all the vaults in the server is offline.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try
			{
				for(int j = 0; j < serverVaults.length; j++)
					Utility.takeVaultOfflineAndBringOnline(serverVaults[j].trim(), "Online");//Takes the vault to Online

				Utility.quitDriver(driver);
			}
			catch(Exception e0){Log.exception(e0);}

		}//End finally

	}//End TC_18148

	/**
	 * TC_28088 : Verify Authentication failed validation message is display when user is not assigned to any vault
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Offline"}, 
			description = "Verify Authentication failed validation message is display when user is not assigned to any vault")
	public void TC_28088(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {

			//Gets the driver
			//---------------
			driver = WebDriverUtils.getDriver();
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-Requisite: Remove the user from the vault
			//---------------------------------------------
			Utility.removeUserAccountInVault(dataPool.get("UserName"), testVault);//Removes the login account from vault
			Utility.removeUserAccountInVault(dataPool.get("UserName"), testVault1);//Removes the login account from vault

			Log.message("Pre-Requisite: Vaults : User '" + dataPool.get("UserName") + "' is removed from the vaults successfully.");

			//Step-1: Logging in using a user with no license
			//-----------------------------------------------
			driver.get(loginURL);//Launches the URL
			Utils.fluentWait(driver);//Waits for the page load
			LoginPage loginPage = new LoginPage(driver);//Instantiates the login page
			loginPage.setUserName(dataPool.get("UserName"));//Sets the user name
			loginPage.setPassword(dataPool.get("Password"));//Sets the password
			Utils.fluentWait(driver);
			loginPage.clickLoginBtn();//Clicks the login button
			Utils.fluentWait(driver);

			Log.message("1. Clicked the login button after entering the credentials of the user whom not exists in any vault in the server.");

			//Verification : Verify if log in is successful 
			//----------------------------------------------
			if (loginPage.getErrorMessage().equalsIgnoreCase(dataPool.get("ErrorMessage")))
				Log.pass("Test case Passed. Login is not successful for the user when all the user account is not exists in the vaults but exists in the server.", driver);
			else
				Log.fail("Test case Failed. Expected error message is displayed in the login page for the user when all the user account is not exists in the vaults but exists in the server.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try
			{
				Utility.configureUsers(xlTestDataWorkBook);//Configure the users in test vault
				Utility.configureUsers(xlTestDataWorkBook, "Users", testVault1);//Configure the users in test vault1
				Utility.quitDriver(driver);
			}
			catch(Exception e0){Log.exception(e0);}

		}//End finally

	}//End TC_28088

}//End of Class BasicAuthentication

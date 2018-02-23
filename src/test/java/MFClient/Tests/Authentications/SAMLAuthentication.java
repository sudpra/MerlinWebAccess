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
import MFClient.Wrappers.Caption;
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class SAMLAuthentication {

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
	public static String samlLoginURL = null;
	public static String samlFirstUserName = null;
	public static String samlSecondUserName = null;
	public static String samlFirstUserPassword = null;
	public static String samlSecondUserPassword = null;
	public static String samlFirstUserFullName = null;
	public static String samlSecondUserFullName = null;
	public static String samlDomainName = null;
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
			samlFirstUserName = xmlParameters.getParameter("samlFirstUserName");
			samlSecondUserName = xmlParameters.getParameter("samlSecondUserName");
			samlFirstUserPassword = xmlParameters.getParameter("samlFirstUserPassword");
			samlSecondUserPassword = xmlParameters.getParameter("samlSecondUserPassword");
			samlFirstUserFullName = xmlParameters.getParameter("samlFirstUserFullName");
			samlSecondUserFullName = xmlParameters.getParameter("samlSecondUserFullName");
			samlDomainName = xmlParameters.getParameter("samlDomainName");
			userName = xmlParameters.getParameter("UserName");
			userFullName = xmlParameters.getParameter("UserFullName");
			password = xmlParameters.getParameter("Password");
			domainName = xmlParameters.getParameter("DomainName");
			testVault = xmlParameters.getParameter("VaultName");
			testVault1 = testVault + "1";
			className = this.getClass().getSimpleName().toString().trim();
			samlLoginURL = xmlParameters.getParameter("SAMLLoginURL");
			if (xmlParameters.getParameter("driverType").equalsIgnoreCase("IE"))
				productVersion = "M-Files " +  xmlParameters.getParameter("productVersion").trim()  + " - " +  xmlParameters.getParameter("driverType").toUpperCase().trim() + xmlParameters.getParameter("driverVersion").trim();
			else
				productVersion = "M-Files " +  xmlParameters.getParameter("productVersion").trim()  + " - " +  xmlParameters.getParameter("driverType").toUpperCase().trim();

			Utility.removeRegistryKey("HKEY_LOCAL_MACHINE\\SOFTWARE\\Motive\\M-Files\\PRODUCTVERSION\\Server\\MFServer\\Authentication");//Removes the registry settings if exists
			Utility.restoreTestVault();//Restores the vault in MFServer
			Utility.restoreTestVault(testVault1, "");
			Utility.configureUsers(xlTestDataWorkBook);	//Configures the user in server and restored vault
			Utility.configureUsers(xlTestDataWorkBook, "Users", testVault1);	//Configures the user in server and restored vault
			Utility.configureUsers(testVault, samlFirstUserFullName, samlFirstUserFullName, "windows", "named", "none", samlDomainName, "admin", "FullControl", "internal");//Configure the windows user in server and restored vault
			Utility.configureUsers(testVault, samlSecondUserFullName, samlSecondUserFullName, "windows", "named", "none", samlDomainName, "admin", "FullControl", "internal");//Configure the windows user in server and restored vault
			Utility.configureUsers(testVault1, samlFirstUserFullName, samlFirstUserFullName, "windows", "named", "none", samlDomainName, "admin", "FullControl", "internal");//Configure the windows user in server and restored vault
			Utility.configureUsers(testVault1, samlSecondUserFullName, samlSecondUserFullName, "windows", "named", "none", samlDomainName, "admin", "FullControl", "internal");//Configure the windows user in server and restored vault
			Utility.configureSAMLorOAuthRegistrySettings();//Configures the SAML authentication in the machine

			if(!Utility.checkSAMLorOAuthIsConfigured("SAML"))
				throw new Exception("SAML is not configured and SAML link is not displayed in the login page.");

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
			try
			{
				Utility.removeRegistryKey("HKEY_LOCAL_MACHINE\\SOFTWARE\\Motive\\M-Files\\PRODUCTVERSION\\Server\\MFServer\\Authentication");//Removes the registry settings
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
	 * TC_17126: Default login with SAML 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SAML"}, 
			description = "Default login with SAML")
	public void TC_17126(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-2: Click the SAML authetication link in the default login page
			//-------------------------------------------------------------------
			LoginPage loginPage = new LoginPage(driver);//Instantaites the login page
			loginPage.clickLoginLink(dataPool.get("LoginType"));//Selects the login type in the login page

			if(!driver.getCurrentUrl().contains(samlLoginURL))
				throw new Exception("SAML login page url("+ samlLoginURL +") is not loaded.[Loaded URL: "+ driver.getCurrentUrl() +"]");

			Log.message("2. Clicked the SAML login link in the login page and Entered into the SAML login page", driver);

			//Step-3: Login with the SAML credentials
			//---------------------------------------
			loginPage = new LoginPage(driver);//Re-Instantiates the login page
			HomePage homePage = loginPage.loginToWebApplicationUsingSAMLorOAuth(samlFirstUserName, samlFirstUserPassword, testVault);

			Log.message("3. Logged into the MFWA Default page using the SAML credentials.");

			//Verification: Check if correct user name is displayed in the user menu
			//----------------------------------------------------------------------
			if(homePage.menuBar.verifyLoggedInUser(samlFirstUserFullName))
				Log.pass("Test case passed. Default login with SAML is working as expected.");
			else
				Log.fail("Test case failed. Default login with SAML is not working as expected.[Logged in user name is different. Expected name: '" + samlDomainName+"\\"+samlFirstUserFullName + "' & Actual name: '" + homePage.menuBar.getLoggedInUserName() + "']", driver);

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

	}//End TC_17126

	/**
	 * TC_17127: Login using incorrect user id - SAML
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SAML"}, 
			description = "Login using incorrect user id - SAML")
	public void TC_17127(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-2: Click the SAML authetication link in the default login page
			//-------------------------------------------------------------------
			LoginPage loginPage = new LoginPage(driver);//Instantaites the login page
			loginPage.clickLoginLink(dataPool.get("LoginType"));//Selects the login type in the login page

			if(!driver.getCurrentUrl().contains(samlLoginURL))
				throw new Exception("SAML login page url("+ samlLoginURL +") is not loaded.[Loaded URL: "+ driver.getCurrentUrl() +"]");

			Log.message("2. Clicked the SAML login link in the login page and Entered into the SAML login page.", driver);

			//Step-3: Enter the invalid user id
			//---------------------------------------
			loginPage = new LoginPage(driver);//Re-Instantiates the login page
			loginPage.setSAMLorOAuthUserName(dataPool.get("InvalidUserID"));//Enter the invalid user id
			//In New MicroSoftLoginpage, without entering the valid username its not possible to enter the password
			//loginPage.setSAMLorOAuthPassword("");//Clicks on the password field

			Log.message("3. Entered the invalid user id in the login page.");

			//Verification: Check if correct user name is displayed in the user menu
			//----------------------------------------------------------------------
			if(loginPage.getSAMLorOAuthLoginErrorMessage().equalsIgnoreCase(dataPool.get("ErrorMessage")))
				Log.pass("Test case passed. Error message is displayed as expected('" + dataPool.get("ErrorMessage") + "') for the invalid domain user id.", driver);
			else
				Log.fail("Test case failed. Error message is not displayed as expected('" + dataPool.get("ErrorMessage") + "') for the invalid domain user id. Actual Error message displayed: '" + loginPage.getSAMLorOAuthLoginErrorMessage() + "'", driver);

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

	}//End TC_17127

	/**
	 * TC_17128: Login using incorrect user password - SAML
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SAML"}, 
			description = "Login using incorrect user password - SAML")
	public void TC_17128(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-2: Click the SAML authetication link in the default login page
			//-------------------------------------------------------------------
			LoginPage loginPage = new LoginPage(driver);//Instantaites the login page
			loginPage.clickLoginLink(dataPool.get("LoginType"));//Selects the login type in the login page

			if(!driver.getCurrentUrl().contains(samlLoginURL))
				throw new Exception("SAML login page url("+ samlLoginURL +") is not loaded while click on the SAML login URL in default login page.[Loaded URL: "+ driver.getCurrentUrl() +"]");

			Log.message("2. Clicked the SAML login link in the login page and Entered into the SAML login page.", driver);

			//Step-3: Enter the invalid user id
			//---------------------------------------
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
				Log.fail("Test case failed. Error message is not displayed as expected('" + expectedErrMsg + "') for the invalid credentials(UserName: '" + dataPool.get("UserName") + "' and Password: '" + dataPool.get("Password") + "'). Actual Error message displayed: '" + loginPage.getSAMLorOAuthLoginErrorMessage() + "'", driver);

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

	}//End TC_17128

	/**
	 * TC_17129: Logout after using SAML authentication with credentials.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SAML"}, 
			description = "Logout after using SAML authentication with credentials.")
	public void TC_17129(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;
		try {

			//Gets the driver
			//---------------
			driver = WebDriverUtils.getDriver();
			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-requisites:
			//---------------
			Utility.setRegistryValue(dataPool.get("RegistryPath"), dataPool.get("RegistryKey1"), dataPool.get("RegistryType"), dataPool.get("RegistryValue1"));//Sets the registry key value
			Utility.setRegistryValue(dataPool.get("RegistryPath"), dataPool.get("RegistryKey2"), dataPool.get("RegistryType"), dataPool.get("RegistryValue2"));//Sets the registry key value
			Utility.resetIIS();//Restarts the IIS server

			Log.message("Pre-Requisite: Registry keys('" + dataPool.get("RegistryKey1") + ", "+ dataPool.get("RegistryKey2") +"') is set with values('" + dataPool.get("RegistryValue1") + ", " + dataPool.get("RegistryValue2") + "') and IIS Server is restarted successfully.");

			//Step-1: Lauch the default login page URL
			//----------------------------------------
			String defaultLoginPage = dataPool.get("LoginPage");
			driver.get(loginURL);//Launches the URL
			Utils.fluentWait(driver);//Waits for the page load

			if(defaultLoginPage.equalsIgnoreCase("SAML"))
			{
				if(!driver.getCurrentUrl().contains(samlLoginURL))
					throw new Exception("SAML login page is not launched when registry keys('" + dataPool.get("RegistryKey1") + ", "+ dataPool.get("RegistryKey2") +"') is set with the values('" + dataPool.get("RegistryValue1") + ", " + dataPool.get("RegistryValue2") + "'). Current URL: '" + driver.getCurrentUrl() + "'");

				Log.message("1. SAML login page is launched successfully when registry keys('" + dataPool.get("RegistryKey1") + ", "+ dataPool.get("RegistryKey2") +"') is set with the values('" + dataPool.get("RegistryValue1") + ", " + dataPool.get("RegistryValue2") + "').");
			}
			else
			{
				if(!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
					throw new Exception("Default login page is not launched when registry keys('" + dataPool.get("RegistryKey1") + ", "+ dataPool.get("RegistryKey2") +"') is set with the values('" + dataPool.get("RegistryValue1") + ", " + dataPool.get("RegistryValue2") + "'). Current URL: '" + driver.getCurrentUrl() + "'");

				Log.message("1. Default login page is launched successfully when registry keys('" + dataPool.get("RegistryKey1") + ", "+ dataPool.get("RegistryKey2") +"') is set with the values('" + dataPool.get("RegistryValue1") + ", " + dataPool.get("RegistryValue2") + "').");
			}

			//Step-2: Login using SAML
			//------------------------
			LoginPage loginPage = null;

			if(defaultLoginPage.equalsIgnoreCase("DEFAULT"))
			{
				loginPage = new LoginPage(driver);//Instantaites the login page
				loginPage.clickLoginLink("SAML");//Clicks the SAML login link in the default login page
				Utils.fluentWait(driver);
			}

			if(!driver.getCurrentUrl().contains(samlLoginURL))
				throw new Exception("SAML login page is not loaded while click on SAML login link in default login page. Current URL: '" + driver.getCurrentUrl() + "'");

			loginPage = new LoginPage(driver);//Instantaites the login page
			loginPage.loginToWebApplicationUsingSAMLorOAuth(samlFirstUserName, samlFirstUserPassword, testVault);//Login using SAML credentials in SAML login page

			Log.message("2. Logged into MFWA successfully using SAML login.");

			//Step-3: Logout from MFWA
			//------------------------
			Utility.logOut(driver);//Logouts from the MFWA

			Log.message("3. Logout is clicked from the home page.");

			//Verification: Check if correct login page is displayed while logout from MFWA
			//-----------------------------------------------------------------------------
			if(dataPool.get("RegistryValue1").trim().equals("true") && dataPool.get("RegistryValue2").trim().equals(""))
			{
				loginPage = new LoginPage(driver);//Instantaites the login page
				loginPage.selectVault(testVault);//Selects the vault
			}
			if(driver.getCurrentUrl().toLowerCase().contains(dataPool.get("URL").toLowerCase()))
				Log.pass("Test case passed. Logout after using SAML authentication with credentials is working as expected.", driver);
			else
				Log.fail("Test case failed. Logout after using SAML authentication with credentials is not working as expected. Additional info. : Not navigated to the page('" + dataPool.get("URL") + "') while logout from MFWA. [Current URL: '" + driver.getCurrentUrl() + "']", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try
			{
				Utility.setRegistryValue(dataPool.get("RegistryPath"), dataPool.get("RegistryKey1"), dataPool.get("RegistryType"), dataPool.get("ResetRegistryValue1"));//Sets the registry key value
				Utility.setRegistryValue(dataPool.get("RegistryPath"), dataPool.get("RegistryKey2"), dataPool.get("RegistryType"), dataPool.get("ResetRegistryValue2"));//Sets the registry key value
				Utility.resetIIS();//Restarts the IIS server
				Utility.quitDriver(driver);//Quits the driver
			}
			catch(Exception e0){Log.exception(e0);}
		}//End finally

	}//End TC_17129

	/**
	 * TC_17130: Logout after using SAML authentication with credentials, then login with different user.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SAML"}, 
			description = "Logout after using SAML authentication with credentials, then login with different user.")
	public void TC_17130(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;
		try {

			//Gets the driver
			//---------------
			driver = WebDriverUtils.getDriver();
			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-requisites:
			//---------------
			Utility.setRegistryValue(dataPool.get("RegistryPath"), dataPool.get("RegistryKey1"), dataPool.get("RegistryType"), dataPool.get("RegistryValue1"));//Sets the registry key value
			Utility.setRegistryValue(dataPool.get("RegistryPath"), dataPool.get("RegistryKey2"), dataPool.get("RegistryType"), dataPool.get("RegistryValue2"));//Sets the registry key value
			Utility.resetIIS();//Restarts the IIS server

			Log.message("Pre-Requisite: Registry keys('" + dataPool.get("RegistryKey1") + ", "+ dataPool.get("RegistryKey2") +"') is set with values('" + dataPool.get("RegistryValue1") + ", " + dataPool.get("RegistryValue2") + "') and IIS Server is restarted successfully.");

			//Step-1: Lauch the default login page URL
			//----------------------------------------
			String defaultLoginPage = dataPool.get("LoginPage");
			driver.get(loginURL);//Launches the URL
			Utils.fluentWait(driver);//Waits for the page load

			if(defaultLoginPage.equalsIgnoreCase("SAML"))
			{
				if(!driver.getCurrentUrl().contains(samlLoginURL))
					throw new Exception("SAML login page is not launched when registry keys('" + dataPool.get("RegistryKey1") + ", "+ dataPool.get("RegistryKey2") +"') is set with the values('" + dataPool.get("RegistryValue1") + ", " + dataPool.get("RegistryValue2") + "'). Current URL: '" + driver.getCurrentUrl() + "'");

				Log.message("1. SAML login page is launched successfully when registry keys('" + dataPool.get("RegistryKey1") + ", "+ dataPool.get("RegistryKey2") +"') is set with the values('" + dataPool.get("RegistryValue1") + ", " + dataPool.get("RegistryValue2") + "').");
			}
			else
			{
				if(!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
					throw new Exception("Default login page is not launched when registry keys('" + dataPool.get("RegistryKey1") + ", "+ dataPool.get("RegistryKey2") +"') is set with the values('" + dataPool.get("RegistryValue1") + ", " + dataPool.get("RegistryValue2") + "'). Current URL: '" + driver.getCurrentUrl() + "'");

				Log.message("1. Default login page is launched successfully when registry keys('" + dataPool.get("RegistryKey1") + ", "+ dataPool.get("RegistryKey2") +"') is set with the values('" + dataPool.get("RegistryValue1") + ", " + dataPool.get("RegistryValue2") + "').");
			}

			//Step-2: Login using SAML
			//------------------------
			LoginPage loginPage = new LoginPage(driver);//Instantaites the login page

			if(defaultLoginPage.equalsIgnoreCase("DEFAULT"))
			{
				loginPage.clickLoginLink("SAML");//Clicks the SAML login link in the default login page
				loginPage = new LoginPage(driver);//Instantaites the login page
			}

			if(!driver.getCurrentUrl().contains(samlLoginURL))
				throw new Exception("SAML login page is not loaded while click on SAML login link in default login page. Current URL: '" + driver.getCurrentUrl() + "'");

			loginPage.loginToWebApplicationUsingSAMLorOAuth(samlFirstUserName, samlFirstUserPassword, testVault);//Login using SAML credentials in SAML login page

			Log.message("2. Logged into MFWA successfully using SAML login.");

			//Step-3: Logout from MFWA
			//------------------------
			Utility.logOut(driver);//Logouts from the MFWA

			Log.message("3. Logout is clicked from the home page.");

			//Step-4: Login as M-Files user after logout
			//-----------------------------------------------------------------------------
			if(dataPool.get("RegistryValue1").trim().equals("true") && dataPool.get("RegistryValue2").trim().equals(""))
			{
				loginPage = new LoginPage(driver);//Instantaites the login page
				loginPage.selectVault(testVault);//Selects the vault
			}

			if(!driver.getCurrentUrl().toLowerCase().contains(dataPool.get("URL").toLowerCase()))
				throw new Exception("Not navigated to the page('" + dataPool.get("URL") + "') while logout from MFWA. [Current URL: '" + driver.getCurrentUrl() + "']");

			if(driver.getCurrentUrl().toUpperCase().contains("DEFAULT.ASPX"))
			{
				driver.quit();//Quits the driver
				driver = WebDriverUtils.getDriver();//Re-Launches the driver
				driver.get(dataPool.get("LoginURL"));
				Utils.fluentWait(driver);//Waits for the page load
			}
			else if(driver.getCurrentUrl().toUpperCase().contains("SAML"))
			{
				driver.get(dataPool.get("LoginURL"));
				Utils.fluentWait(driver);//Waits for the page load
			}

			if(dataPool.get("RegistryValue1").trim().equals("false") && dataPool.get("RegistryValue2").trim().equals(""))
			{
				driver.quit();//Quits the driver
				driver = WebDriverUtils.getDriver();//Re-Launches the driver
				driver.get(dataPool.get("LoginURL"));
				Utils.fluentWait(driver);//Waits for the page load
			}

			loginPage = new LoginPage(driver);//Instantaites the login page
			loginPage.clickLoginLink("SAML");//Clicks the SAML login link in the default login page
			loginPage = new LoginPage(driver);//Instantaites the login page
			loginPage.loginToWebApplicationUsingSAMLorOAuth(samlSecondUserName, samlSecondUserPassword, testVault);//Login to the web application as M-Files user

			Log.message("4. Log into the MFWA as different SAML user.");

			//Verification: Check if correct login page is displayed while logout from MFWA
			//-----------------------------------------------------------------------------
			HomePage homePage = new HomePage(driver);//Instantiates the homepage
			if(homePage.menuBar.getLoggedInUserName().equalsIgnoreCase(samlSecondUserFullName))
				Log.pass("Test case passed. Logout after using SAML authentication with credentials, then login with different user is working as expected.", driver);
			else
				Log.fail("Test case failed. Logout after using SAML authentication with credentials, then login with different user is not working as expected. Additional info. : Logged in user name is different. Expected: '" + samlSecondUserFullName + "' & Actual: '" + homePage.menuBar.getLoggedInUserName() + "'.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try
			{
				Utility.setRegistryValue(dataPool.get("RegistryPath"), dataPool.get("RegistryKey1"), dataPool.get("RegistryType"), dataPool.get("ResetRegistryValue1"));//Sets the registry key value
				Utility.setRegistryValue(dataPool.get("RegistryPath"), dataPool.get("RegistryKey2"), dataPool.get("RegistryType"), dataPool.get("ResetRegistryValue2"));//Sets the registry key value
				Utility.resetIIS();//Restarts the IIS server
				Utility.quitDriver(driver);//Quits the driver
			}
			catch(Exception e0){Log.exception(e0);}
		}//End finally

	}//End TC_17130

	/**
	 * TC_17132: Login and close browser and then login again.
	 */
	@Test(groups = { "SAML"}, 
			description = "Login and close browser and then login again.")
	public void TC_17132() throws Exception {

		driver = null; 
		try {

			//Gets the driver
			//---------------
			driver = WebDriverUtils.getDriver();

			//Step-1: Lauch the default login page URL
			//----------------------------------------
			driver.get(loginURL);//Launches the URL
			Utils.fluentWait(driver);//Waits for the page load

			if(!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception("Default login page is not launched . Current URL: '" + driver.getCurrentUrl() + "'");

			Log.message("1. Default login page is launched successfully.");

			//Step-2: Login using SAML
			//------------------------
			LoginPage loginPage = new LoginPage(driver);//Instantaites the login page
			loginPage.clickLoginLink("SAML");//Clicks the SAML login link in the default login page

			if(!driver.getCurrentUrl().contains(samlLoginURL))
				throw new Exception("SAML login page is not loaded while click on SAML login link in default login page. Current URL: '" + driver.getCurrentUrl() + "'");

			loginPage = new LoginPage(driver);//Instantaites the login page
			loginPage.loginToWebApplicationUsingSAMLorOAuth(samlFirstUserName, samlFirstUserPassword, testVault);//Login using SAML credentials in SAML login page

			Log.message("2. Logged into MFWA successfully using SAML login.");

			//Step-3: Close the browser
			//------------------------
			driver.quit();//Closes the browser

			Log.message("3. Browser is closed after login into the MFWA.");

			//Step-4 : Re-Launch the driver and login to the MFWA
			//---------------------------------------------------
			driver = WebDriverUtils.getDriver();
			driver.get(loginURL);//Launches the URL
			Utils.fluentWait(driver);//Waits for the page load

			if(!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception("Default login page is not launched . Current URL: '" + driver.getCurrentUrl() + "'");

			loginPage = new LoginPage(driver);//Instantaites the login page
			loginPage.clickLoginLink("SAML");//Clicks the SAML login link in the default login page

			if(!driver.getCurrentUrl().contains(samlLoginURL))
				throw new Exception("SAML login page is not loaded while click on SAML login link in default login page after re-launch the driver. Current URL: '" + driver.getCurrentUrl() + "'");

			loginPage = new LoginPage(driver);//Instantaites the login page
			HomePage homePage = loginPage.loginToWebApplicationUsingSAMLorOAuth(samlFirstUserName, samlFirstUserPassword, testVault);//Login using SAML credentials in SAML login page

			Log.message("4. Re-Launched the browser and logged into MFWA");

			//Verification: Check re-login is successfull
			//-------------------------------------------
			if(homePage.menuBar.getLoggedInUserName().equalsIgnoreCase(samlFirstUserFullName))
				Log.pass("Test case passed. Login and close browser and then login again is working as expected.");
			else
				Log.fail("Test case failed. Login and close browser and then login again is not working as expected. [Logged in user name is different. Expected: '" + samlFirstUserFullName + "' and Actual : '" + homePage.menuBar.getLoggedInUserName() + "']", driver);
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

	}//End TC_17132

	/**
	 * TC_17133: Login and close browser and then login again as different user.
	 */
	@Test(groups = { "SAML"}, description = "Login and close browser and then login again as different user.")
	public void TC_17133() throws Exception {

		driver = null; 
		try {

			//Gets the driver
			//---------------
			driver = WebDriverUtils.getDriver();

			//Step-1: Lauch the default login page URL
			//----------------------------------------
			driver.get(loginURL);//Launches the URL
			Utils.fluentWait(driver);//Waits for the page load

			if(!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception("Default login page is not launched . Current URL: '" + driver.getCurrentUrl() + "'");

			Log.message("1. Default login page is launched successfully.");

			//Step-2: Login using SAML
			//------------------------
			LoginPage loginPage = new LoginPage(driver);//Instantaites the login page
			loginPage.clickLoginLink("SAML");//Clicks the SAML login link in the default login page

			if(!driver.getCurrentUrl().contains(samlLoginURL))
				throw new Exception("SAML login page is not loaded while click on SAML login link in default login page.Current URL: '" + driver.getCurrentUrl() + "'");

			loginPage = new LoginPage(driver);//Instantaites the login page
			loginPage.loginToWebApplicationUsingSAMLorOAuth(samlFirstUserName, samlFirstUserPassword, testVault);//Login using SAML credentials in SAML login page

			Log.message("2. Logged into MFWA successfully using SAML login.");

			//Step-3: Close the browser
			//------------------------
			driver.quit();//Closes the browser

			Log.message("3. Browser is closed after login into the MFWA.");

			//Step-4 : Re-Launch the driver and login to the MFWA
			//---------------------------------------------------
			driver = WebDriverUtils.getDriver();
			driver.get(loginURL);//Launches the URL
			Utils.fluentWait(driver);//Waits for the page load

			if(!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception("Default login page is not launched . Current URL: '" + driver.getCurrentUrl() + "'");

			loginPage = new LoginPage(driver);//Instantaites the login page
			loginPage.clickLoginLink("SAML");//Clicks the SAML login link in the default login page

			if(!driver.getCurrentUrl().contains(samlLoginURL))
				throw new Exception("SAML login page is not loaded while click on SAML login link in default login page after re-launch the driver. Current URL: '" + driver.getCurrentUrl() + "'");

			loginPage = new LoginPage(driver);//Instantaites the login page
			HomePage homePage = loginPage.loginToWebApplicationUsingSAMLorOAuth(samlFirstUserName, samlFirstUserPassword, testVault);//Login using SAML credentials in SAML login page

			Log.message("4. Re-Launched the browser and logged into MFWA");

			//Verification: Check re-login is successfull
			//-------------------------------------------
			if(homePage.menuBar.getLoggedInUserName().equalsIgnoreCase(samlFirstUserFullName))
				Log.pass("Test case passed. Login and close browser and then login again as different user is working as expected.");
			else
				Log.fail("Test case failed. Login and close browser and then login again as different user is not working as expected. [Logged in user name is different. Expected: '" + samlFirstUserFullName + "' and Actual : '" + homePage.menuBar.getLoggedInUserName() + "']", driver);

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

	}//End TC_17133

	/**
	 * TC_17139: SAML login page is shown correctly when using showLogin= true/false
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SAML"}, 
			description = "SAML login page is shown correctly when using showLogin= true/false")
	public void TC_17139(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;
		try {

			//Gets the driver
			//---------------
			driver = WebDriverUtils.getDriver();
			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-requisites:
			//---------------
			Utility.setRegistryValue(dataPool.get("RegistryPath"), dataPool.get("RegistryKey"), dataPool.get("RegistryType"), dataPool.get("RegistryValue"));//Sets the registry key value
			Utility.resetIIS();//Restarts the IIS server

			//Step-1: Lauch the default login page URL
			//----------------------------------------
			driver.get(dataPool.get("URL1"));//Launches the URL
			Utils.fluentWait(driver);//Waits for the page load

			if(!driver.getCurrentUrl().contains(samlLoginURL))
				throw new Exception("SAML login page is not launched while launching default login page url with showLogin=false. Current URL: '" + driver.getCurrentUrl() + "'");

			Log.message("1. SAML login page is launched successfully while launching the url: '"+ dataPool.get("URL1") +"'.");

			//Step-2: Login to MFWA using SAML
			//--------------------------------
			LoginPage loginPage = new LoginPage(driver);//Re-Instantiates the login page
			loginPage.loginToWebApplicationUsingSAMLorOAuth(samlFirstUserName, samlFirstUserPassword, testVault);

			Log.message("2. Logged into MFWA using SAML login");

			//Step-3: Logout from MFWA and Check Default login page is not shown
			//------------------------------------------------------------------
			Utility.logOut(driver);//Logging out from MFWA

			if(!driver.getCurrentUrl().contains(samlLoginURL))
				throw new Exception("SAML login page is not shown while logging out from MFWA when logged in using SAML.[Current URL: '"+ driver.getCurrentUrl() +"']");

			Log.message("3. SAML login page is shown while logout from MFWA.");

			//Step-4: Re-launch the driver with the URL have query string showlogin=true
			//---------------------------------------------------------------------------
			driver.quit();//Quits the driver
			driver = WebDriverUtils.getDriver();//Re-launches the driver again
			driver.get(dataPool.get("URL2"));//Launches the URL
			Utils.fluentWait(driver);//Waits for the page load

			Log.message("4. Driver is re-launched with the URL '" + dataPool.get("URL2") + "'");

			//Step-5: Check if default login page is launched
			//------------------------------------------------
			if(!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception("Default login page is not launched while launching the URL '" + dataPool.get("URL2") + "'. Current URL: '" + driver.getCurrentUrl() + "'");

			Log.message("5. Default login page is displayed as expected.");

			//Step-6: Login using M-Files user
			//--------------------------------
			loginPage = new LoginPage(driver);//Instantiates the login page
			loginPage.setUserName(userName);//Sets the username
			loginPage.setPassword(password);//Sets the password
			loginPage.clickLoginBtn();//Clicks the login button
			Utils.fluentWait(driver);
			loginPage.selectVault(testVault);//Selects the vault
			Utils.fluentWait(driver);

			if(!driver.getCurrentUrl().toUpperCase().contains("DEFAULT.ASPX"))
				throw new Exception("Default page is not opened while login using M-Files user in default login page. Current URL: '" + driver.getCurrentUrl() + "'");

			Log.message("6. Successfully logged in using the M-Files user in the default login page.");

			//Step-7: Logout from the MFWA
			//----------------------------
			Utility.logOut(driver);//Logout from the web access

			Log.message("7. Logged out from the MFWA.");

			//Verification: Check if default login page is launched while using query string showLogin=true
			//----------------------------------------------------------------------------------------------
			if(driver.getCurrentUrl().contains(samlLoginURL))
				Log.pass("Test case passed. SAML login page is shown correctly when using showLogin= true/false.");
			else
				Log.fail("Test case failed. SAML login page is not shown correctly when using showLogin= true/false. Additional info. : SAML login page is not launched while logout from the web access. [Current URL: '" + driver.getCurrentUrl() + "']", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try
			{
				Utility.setRegistryValue(dataPool.get("RegistryPath"), dataPool.get("RegistryKey"), dataPool.get("RegistryType"), dataPool.get("ResetRegistryValue"));//Sets the registry key value
				Utility.resetIIS();//Restarts the IIS server
				Utility.quitDriver(driver);//Quits the driver
			}
			catch(Exception e0){Log.exception(e0);}
		}//End finally

	}//End TC_17139

	/**
	 * TC_17140: SAML login page is shown directly according to registry setting
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SAML"}, 
			description = "SAML login page is shown directly according to registry setting.")
	public void TC_17140(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;
		try {

			//Gets the driver
			//---------------
			driver = WebDriverUtils.getDriver();
			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-requisites:
			//---------------
			Utility.setRegistryValue(dataPool.get("RegistryPath"), dataPool.get("RegistryKey"), dataPool.get("RegistryType"), dataPool.get("RegistryValue"));//Sets the registry key value
			Utility.resetIIS();//Restarts the IIS server

			Log.message("Pre-Requisite: Registry key('" + dataPool.get("RegistryKey") + "') is set with value('" + dataPool.get("RegistryValue") + "') and IIS Server is restarted successfully.");

			//Step-1: Lauch the default login page URL
			//----------------------------------------
			driver.get(loginURL);//Launches the URL
			Utils.fluentWait(driver);//Waits for the page load

			if(!driver.getCurrentUrl().contains(samlLoginURL))
				throw new Exception("SAML login page is not launched when registry key('" + dataPool.get("RegistryKey") + "') is set with the value('" + dataPool.get("RegistryValue") + "'). Current URL: '" + driver.getCurrentUrl() + "'");

			Log.message("1. SAML login page is launched successfully when registry key('" + dataPool.get("RegistryKey") + "') is set with the value('" + dataPool.get("RegistryValue") + "').");

			//Step-2: Login using SAML
			//------------------------
			LoginPage loginPage = new LoginPage(driver);//Instantaites the login page
			loginPage.loginToWebApplicationUsingSAMLorOAuth(samlFirstUserName, samlFirstUserPassword, testVault);

			Log.message("2. Logged into MFWA successfully using SAML login.");

			//Step-3: Loggout from MFWA:
			//--------------------------
			Utility.logOut(driver);//Logouts from the MFWA

			if(!driver.getCurrentUrl().contains(samlLoginURL))
				throw new Exception("SAML login page is not loaded while logout from the MFWA. [Current URL: '" + driver.getCurrentUrl() + "']");

			Log.message("3. Successfully logged out from the MFWA");

			//Pre-requisites:
			//---------------
			Utility.setRegistryValue(dataPool.get("RegistryPath"), dataPool.get("RegistryKey"), dataPool.get("RegistryType"), dataPool.get("RegistryValue1"));//Sets the registry key value
			Utility.resetIIS();//Restarts the IIS server

			Log.message("Pre-Requisite: Registry key('" + dataPool.get("RegistryKey") + "') is set with value('" + dataPool.get("RegistryValue1") + "') and IIS Server is restarted successfully.");

			//Step-4:  Launch the MFWA login page
			//------------------------------------
			driver.get(loginURL);//Launches the URL
			Utils.fluentWait(driver);//Waits for the page load

			if(!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception("Default login page is not launched when registry key('" + dataPool.get("RegistryKey") + "') is set with the value('" + dataPool.get("RegistryValue") + "'). Current URL: '" + driver.getCurrentUrl() + "'");

			Log.message("4. M-Files default login page is launched successfully when registry key('" + dataPool.get("RegistryKey") + "') is set with the value('" + dataPool.get("RegistryValue") + "').");

			//Step-5: Login to MFWA
			//---------------------
			loginPage = new LoginPage(driver);//Instantiates the login page
			loginPage.loginToWebApplication(userName, password, testVault);//Login to the MFWA as M-Files user

			if(!driver.getCurrentUrl().toUpperCase().contains("DEFAULT.ASPX"))
				throw new Exception("Default page is not displayed while logging into the MFWA as M-Files user. Current URL: '" + driver.getCurrentUrl() + "'");

			Log.message("5. Logged into the default web page as M-Files user successfully.");

			//Step-6: Logout from the MFWA
			//----------------------------
			if(!Utility.logOut(driver))//Logouts from the MFWA
				throw new Exception("Logout from MFWA is unsuccessfull.[Current URL: '" + driver.getCurrentUrl() + "']");

			Log.message("6. Successfully logged out from the MFWA");

			//Step-7: Launch the login page 
			//------------------------------
			driver.get(loginURL);//Launches the URL
			Utils.fluentWait(driver);//Waits for the page load

			Log.message("6. Launched the URL: '" + loginURL + "'");

			//Verification: Check if default login page is launched while using query string showLogin=false
			//----------------------------------------------------------------------------------------------
			if(driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				Log.pass("Test case passed. SAML login page is shown directly according to registry setting.");
			else
				Log.fail("Test case failed. Default login page is not shown directly according to registry setting. Additional info. : Default login page is not launched. [Current URL: '" + driver.getCurrentUrl() + "']", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try
			{
				Utility.setRegistryValue(dataPool.get("RegistryPath"), dataPool.get("RegistryKey"), dataPool.get("RegistryType"), dataPool.get("RegistryValue1"));//Sets the registry key value
				Utility.resetIIS();//Restarts the IIS server
				Utility.quitDriver(driver);//Quits the driver
			}
			catch(Exception e0){Log.exception(e0);}
		}//End finally

	}//End TC_17140

	/**
	 * TC_17141: SAML login page behaves correctly when redirect on login is disabled via registry setting.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SAML"}, 
			description = "SAML login page behaves correctly when redirect on login is disabled via registry setting.")
	public void TC_17141(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Gets the driver
			//---------------
			driver = WebDriverUtils.getDriver();
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-requisites:
			//---------------
			Utility.setRegistryValue(dataPool.get("RegistryPath"), dataPool.get("RegistryKey"), dataPool.get("RegistryType"), dataPool.get("RegistryValue"));//Sets the registry key value
			Utility.resetIIS();//Restarts the IIS server

			Log.message("Pre-Requisite: Registry key('" + dataPool.get("RegistryKey") + "') is set with value('" + dataPool.get("RegistryValue") + "') and IIS Server is restarted successfully.");

			//Step-1: Lauch the default login page URL
			//----------------------------------------
			driver.get(loginURL);//Launches the URL
			Utils.fluentWait(driver);//Waits for the page load

			if(!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception("Default login page is not launched when registry key('" + dataPool.get("RegistryKey") + "') is set with the value('" + dataPool.get("RegistryValue") + "'). Current URL: '" + driver.getCurrentUrl() + "'");

			Log.message("1. M-Files default login page is launched successfully when registry key('" + dataPool.get("RegistryKey") + "') is set with the value('" + dataPool.get("RegistryValue") + "').");

			//Step-2:  Login as the m-files user
			//----------------------------------
			LoginPage.launchDriverAndLogin(driver, false);//Login into the default webpage

			Log.message("2. Logged into the default web page as M-Files user successfully.");

			//Step-3: Logout from the MFWA
			//----------------------------
			Utility.logOut(driver);//Logouts from the MFWA

			Log.message("3. Successfully logged out from the MFWA");

			//Step-4: Login using SAML
			//------------------------
			LoginPage loginPage = new LoginPage(driver);//Instantaites the login page
			loginPage.clickLoginLink(dataPool.get("LoginType"));//Selects the login type in the login page

			if(!driver.getCurrentUrl().contains(samlLoginURL))
				throw new Exception("SAML login page url("+ samlLoginURL +") is not loaded while click on the SAML login URL in default login page.[Loaded URL: "+ driver.getCurrentUrl() +"]");

			loginPage = new LoginPage(driver);//Re-Instantiates the login page
			loginPage.loginToWebApplicationUsingSAMLorOAuth(samlFirstUserName, samlFirstUserPassword, testVault);

			Log.message("4. Logged into MFWA successfully using SAML login.");

			//Step-5: Loggout from MFWA:
			//--------------------------
			Utility.logOut(driver);//Logouts from the MFWA

			if(!driver.getCurrentUrl().contains(samlLoginURL))
				throw new Exception("SAML login page is not loaded while logout from the MFWA. [Current URL: '" + driver.getCurrentUrl() + "']");

			Log.message("5. Successfully logged out from the MFWA");

			//Step-6: Launch the login page with showlogin=false query string
			//---------------------------------------------------------------
			driver.get(dataPool.get("URL"));//Launches the URL
			Utils.fluentWait(driver);//Waits for the page load

			Log.message("6. Launched the URL: '" + dataPool.get("URL") + "'");

			//Verification: Check if default login page is launched while using query string showLogin=false
			//----------------------------------------------------------------------------------------------
			if(driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				Log.pass("Test case passed. SAML login page behaves correctly when redirect on login is disabled via registry setting.");
			else
				Log.fail("Test case failed. SAML login page not behaves correctly when redirect on login is disabled via registry setting. Additional info. : Default login page is launched while launch the URL with 'showLogin=False' query string. [Current URL: '" + driver.getCurrentUrl() + "']", driver);

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

	}//End TC_17141

	/**
	 * TC_17142: SAML login page behaves correctly when redirect on login is enabled via registry setting.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SAML"}, 
			description = "SAML login page behaves correctly when redirect on login is enabled via registry setting.")
	public void TC_17142(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;
		try {

			//Gets the driver
			//---------------
			driver = WebDriverUtils.getDriver();
			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-requisites:
			//---------------
			Utility.setRegistryValue(dataPool.get("RegistryPath"), dataPool.get("RegistryKey"), dataPool.get("RegistryType"), dataPool.get("RegistryValue"));//Sets the registry key value
			Utility.resetIIS();//Restarts the IIS server

			Log.message("Pre-Requisite: Registry key('" + dataPool.get("RegistryKey") + "') is set with value('" + dataPool.get("RegistryValue") + "') and IIS Server is restarted successfully.");

			//Step-1: Lauch the default login page URL
			//----------------------------------------
			driver.get(loginURL);//Launches the URL
			Utils.fluentWait(driver);//Waits for the page load

			if(!driver.getCurrentUrl().contains(samlLoginURL))
				throw new Exception("SAML login page is not launched while launching default login page url when registry key('" + dataPool.get("RegistryKey") + "') is set with the value('" + dataPool.get("RegistryValue") + "'). Current URL: '" + driver.getCurrentUrl() + "'");

			Log.message("1. SAML login page is launched successfully when registry key('" + dataPool.get("RegistryKey") + "') is set with the value('" + dataPool.get("RegistryValue") + "').");

			//Step-2: Login to MFWA using SAML
			//--------------------------------
			LoginPage loginPage = new LoginPage(driver);//Re-Instantiates the login page
			loginPage.loginToWebApplicationUsingSAMLorOAuth(samlFirstUserName, samlFirstUserPassword, testVault);

			Log.message("2. Logged into MFWA using SAML login");

			//Step-3: Logout from MFWA and Check Default login page is not shown
			//------------------------------------------------------------------
			Utility.logOut(driver);

			if(!driver.getCurrentUrl().contains(samlLoginURL))
				throw new Exception("SAML login page is shown while logging out from MFWA when logged in using SAML.[Current URL: '"+ driver.getCurrentUrl() +"']");

			Log.message("3. SAML login page is shown while logout from MFWA.");

			//Step-4: Re-launch the driver with the URL have query string showlogin=true
			//---------------------------------------------------------------------------
			driver.quit();//Quits the driver
			driver = WebDriverUtils.getDriver();//Re-launches the driver again
			driver.get(dataPool.get("URL"));//Launches the URL
			Utils.fluentWait(driver);//Waits for the page load

			Log.message("4. Driver is re-launched with the URL '" + dataPool.get("URL") + "'");

			//Verification: Check if default login page is launched while using query string showLogin=true
			//----------------------------------------------------------------------------------------------
			if(driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				Log.pass("Test case passed. SAML login page behaves correctly when redirect on login is enabled via registry setting.", driver);
			else
				Log.fail("Test case failed. SAML login page not behaves correctly when redirect on login is enabled via registry setting. Additional info. : Default login page is not launched while launch the URL with 'showLogin=True' query string. [Current URL: '" + driver.getCurrentUrl() + "']", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try
			{
				Utility.setRegistryValue(dataPool.get("RegistryPath"), dataPool.get("RegistryKey"), dataPool.get("RegistryType"), dataPool.get("ResetRegistryValue"));//Sets the registry key value
				Utility.resetIIS();//Restarts the IIS server
				Utility.quitDriver(driver);//Quits the driver
			}
			catch(Exception e0){Log.exception(e0);}
		}//End finally

	}//End TC_17142

	/**
	 * TC_17143: Get M-Files Web URL - different actions without logging out
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SAML"}, 
			description = "Get M-Files Web URL - different actions after logging out")
	public void TC_17143(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;
		try {

			//Gets the driver
			//---------------
			driver = WebDriverUtils.getDriver();
			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1: Login using SAML
			//------------------------
			driver.get(loginURL);//Launches the URL
			Utils.fluentWait(driver);//Waits for the page load

			LoginPage loginPage = new LoginPage(driver);//Instantiates the login page
			loginPage.clickLoginLink(dataPool.get("LoginType"));//Clicks the login link 

			if(!driver.getCurrentUrl().toLowerCase().contains(samlLoginURL.toLowerCase()))
				throw new Exception("SAML login page URL is not loaded after click on the '"+ dataPool.get("LoginType") +"' link from the default login page.[Current URL: "+ driver.getCurrentUrl() +"]");

			loginPage = new LoginPage(driver);//Instantiates the SAML login page
			HomePage homePage = loginPage.loginToWebApplicationUsingSAMLorOAuth(samlFirstUserName, samlFirstUserPassword, testVault);//Login using SAML

			Log.message("1. Logged into the MFWA using SAML credentials.");

			//Step-2: Navigate to any view and get the web url for the object
			//---------------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("viewToNavigate"), "");//Navigates to the specific view

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new Exception("Object '" + dataPool.get("Object") + "' is not selected from the list view.");

			Log.message("2. Navigated to the '" + viewToNavigate + "' view and selected the object '" + dataPool.get("Object") + "' in the view.");

			//Step-3: Get the Web URL for the object
			//--------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value);//Clicks the Get-Mfiles Web URL option in the operation menu

			if(!MFilesDialog.exists(driver, Caption.MenuItems.GetMFilesWebURL.Value))
				throw new Exception(Caption.MenuItems.GetMFilesWebURL.Value+" dialog is not displayed after open it from the operation menu.");

			MFilesDialog mfDialog = new MFilesDialog(driver, Caption.MenuItems.GetMFilesWebURL.Value);//Instantiates the M-Files dialog

			if(!mfDialog.setHyperLinkAction(dataPool.get("HyperlinkAction")))//Sets the Hyperlink action
				throw new Exception(dataPool.get("HyperlinkAction") + " is not set in the Get M-Files Web URL dialog");

			String hyperlink = mfDialog.getHyperlink();//Gets the URL from the MFiles dialog
			mfDialog.close();//Closes the Get M-Files Web URL dialog

			Log.message("3. Got the GetMFilesWebURL with the option '" + dataPool.get("HyperlinkAction") + "' in GetM-FilesWebURL dialog.");

			//Step-5: Launch the obtained URL
			//-------------------------------
			driver.get(hyperlink);//Launches the Hyperlink
			Utils.fluentWait(driver);

			Log.message("4. Launched the copied hyperlink url.");

			//Verification: Check if the selected object/view is exists
			//---------------------------------------------------------
			String result = "";

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				result += "Item '" + dataPool.get("Object") + "' is not exists in the view. ";

			if(!driver.getCurrentUrl().toLowerCase().equals(hyperlink.toLowerCase()))
				result += "Navigated URL("+ driver.getCurrentUrl() +") is not same as obtained hyperlink url("+ hyperlink +"). Current URL: '" + driver.getCurrentUrl() + "'";

			if(result.equals(""))
				Log.pass("Test case passed. Get M-Files Web URL - different actions without logging out is working as expected.");
			else
				Log.fail("Test case failed. Get M-Files Web URL - different actions without logging out is not working as expected.[Additional info. : " + result + "]", driver);

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

	}//End TC_17143

	/**
	 * TC_17144: Get M-Files Web URL - different actions after logging out
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SAML"}, 
			description = "Get M-Files Web URL - different actions after logging out")
	public void TC_17144(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;
		try {

			//Gets the driver
			//---------------
			driver = WebDriverUtils.getDriver();
			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1: Login using SAML
			//------------------------
			driver.get(loginURL);//Launches the URL
			Utils.fluentWait(driver);//Waits for the page load

			LoginPage loginPage = new LoginPage(driver);//Instantiates the login page
			loginPage.clickLoginLink(dataPool.get("LoginType"));//Clicks the login link 

			if(!driver.getCurrentUrl().toLowerCase().contains(samlLoginURL.toLowerCase()))
				throw new Exception("SAML login page URL is not loaded after click on the '"+ dataPool.get("LoginType") +"' link from the default login page.[Current URL: "+ driver.getCurrentUrl() +"]");

			loginPage = new LoginPage(driver);//Instantiates the SAML login page
			HomePage homePage = loginPage.loginToWebApplicationUsingSAMLorOAuth(samlFirstUserName, samlFirstUserPassword, testVault);//Login using SAML

			Log.message("1. Logged into the MFWA using SAML credentials.");

			//Step-2: Navigate to any view and get the web url for the object
			//---------------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("viewToNavigate"), "");//Navigates to the specific view

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new Exception("Object '" + dataPool.get("Object") + "' is not selected from the list view.");

			Log.message("2. Navigated to the '" + viewToNavigate + "' view and selected the object '" + dataPool.get("Object") + "' in the view.");

			//Step-3: Get the Web URL for the object
			//--------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value);//Clicks the Get-Mfiles Web URL option in the operation menu

			if(!MFilesDialog.exists(driver, Caption.MenuItems.GetMFilesWebURL.Value))
				throw new Exception(Caption.MenuItems.GetMFilesWebURL.Value+" dialog is not displayed after open it from the operation menu.");

			MFilesDialog mfDialog = new MFilesDialog(driver, Caption.MenuItems.GetMFilesWebURL.Value);//Instantiates the M-Files dialog

			if(!mfDialog.setHyperLinkAction(dataPool.get("HyperlinkAction")))//Sets the Hyperlink action
				throw new Exception(dataPool.get("HyperlinkAction") + " is not set in the Get M-Files Web URL dialog");

			String hyperlink = mfDialog.getHyperlink();//Gets the URL from the MFiles dialog
			mfDialog.close();//Closes the Get M-Files Web URL dialog

			Log.message("3. Got the GetMFilesWebURL with the option '" + dataPool.get("HyperlinkAction") + "' in GetM-FilesWebURL dialog.");

			//Step-4: Logout from the MFWA
			//-----------------------------
			Utility.logOut(driver);//Logs out from MFWA

			Log.message("4. Logged out from the MFWA.");

			//Step-5: Launch the obtained URL and login using SAML
			//-----------------------------------------------------
			driver.get(hyperlink);//Launches the Hyperlink
			Utils.fluentWait(driver);//Waits for the page load

			loginPage = new LoginPage(driver);//Instantiates the login page
			loginPage.clickLoginLink(dataPool.get("LoginType"));//Clicks the login link 

			if(!driver.getCurrentUrl().toLowerCase().equals(samlLoginURL.toLowerCase()))
				throw new Exception("SAML login page URL is not loaded after click on the '"+ dataPool.get("LoginType") +"' link from the default login page.[Current URL: "+ driver.getCurrentUrl() +"]");

			loginPage = new LoginPage(driver);//Instantiates the SAML login page
			homePage = loginPage.loginToWebApplicationUsingSAMLorOAuth(samlFirstUserName, samlFirstUserPassword, testVault);//Login using SAML

			Log.message("5. Logged into the MFWA using obtained hyperlink url with SAML credentials.");

			//Verification: Check if the selected object/view is exists
			//---------------------------------------------------------
			String result = "";

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				result += "Item '" + dataPool.get("Object") + "' is not exists in the view. ";

			if(!driver.getCurrentUrl().toLowerCase().equals(hyperlink.toLowerCase()))
				result += "Navigated URL("+ driver.getCurrentUrl() +") is not same as obtained hyperlink url("+ hyperlink +"). Current URL: '" + driver.getCurrentUrl() + "'";

			if(result.equals(""))
				Log.pass("Test case passed. Get M-Files Web URL - different actions after logging out is working as expected.");
			else
				Log.fail("Test case failed. Get M-Files Web URL - different actions after logging out is not working as expected.[Additional info. : " + result + "]", driver);

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

	}//End TC_17144

	/**
	 * TC_17146: SAML configurations can be removed and standard mfiles login accounts can be used(saml to regular).
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SAML"}, 
			description = "SAML configurations can be removed and standard mfiles login accounts can be used(saml to regular).")
	public void TC_17146(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;
		try {

			//Gets the driver
			//---------------
			driver = WebDriverUtils.getDriver();
			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-Requisite: Set the registry key values
			//------------------------------------------
			Utility.setRegistryValue(dataPool.get("RegistryPath"), dataPool.get("RegistryKey"), dataPool.get("RegistryType"), dataPool.get("RegistryValue"));//Sets the registry key value
			Utility.resetIIS();//Restarts the IIS server

			Log.message("Pre-Requisite: Registry key('" + dataPool.get("RegistryKey") + "') is set with value('" + dataPool.get("RegistryValue") + "') and IIS Server is restarted successfully.");

			//Step-1: Lauch the default login page URL
			//----------------------------------------
			driver.get(loginURL);//Launches the URL
			Utils.fluentWait(driver);//Waits for the page load

			if(!driver.getCurrentUrl().contains(samlLoginURL))
				throw new Exception("SAML login page is not launched while launching default login page url when registry key('" + dataPool.get("RegistryKey") + "') is set with the value('" + dataPool.get("RegistryValue") + "'). Current URL: '" + driver.getCurrentUrl() + "'");

			Log.message("1. SAML login page is launched successfully when launching the default login page url.");

			//Step-2: Remove the Authentication key from the registry
			//-------------------------------------------------------
			Utility.removeRegistryKey("HKEY_LOCAL_MACHINE\\SOFTWARE\\Motive\\M-Files\\PRODUCTVERSION\\Server\\MFServer\\Authentication");//Removes the registry settings

			Log.message("2. Removed the 'HKEY_LOCAL_MACHINE\\SOFTWARE\\Motive\\M-Files\\PRODUCTVERSION\\Server\\MFServer\\Authentication' key from the machine and restarted the iis.");

			//Step-3: Launch the default login URL
			//------------------------------------
			driver.get(loginURL);//Launches the URL
			Utils.fluentWait(driver);//Waits for the page load

			if(!driver.getCurrentUrl().contains(loginURL))
				throw new Exception("Default login page is not launched after removing the registry key. Current URL: '" + driver.getCurrentUrl() + "'");

			LoginPage loginPage = new LoginPage(driver);//Instantiates the login page

			if(loginPage.isSAMLorOAuthLinkDisplayed("SAML"))
				throw new Exception("SAML link is still displayed after removing the authentication key from registry settings.");

			Log.message("3. Default login page is loaded successfully.");

			//Step-4: Login into the application using M-Files Credentials
			//------------------------------------------------------------
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);//Login into the web application

			Log.message("4. Logged into the M-Files web application successfully.");

			//Verification: Check if user is logged in successfully
			//-----------------------------------------------------
			if(homePage.menuBar.getLoggedInUserName().equalsIgnoreCase(userFullName))
				Log.pass("Test case passed. SAML configurations can be removed and standard mfiles login accounts can be used(saml to regular) successfully.");
			else
				Log.fail("Test case failed. After removing the SAML configurations and standard mfiles login('" + userFullName + "') is unsuccessful.(saml to regular) [Actual value: '" + homePage.menuBar.getLoggedInUserName() + "']", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try
			{
				Utility.configureSAMLorOAuthRegistrySettings();//Configures the SAML authentication in the machine
				Utility.quitDriver(driver);//Quits the driver
			}
			catch(Exception e0){Log.exception(e0);}
		}//End finally

	}//End TC_17146

	/**
	 * TC_17147: SAML configurations can be removed and standard mfiles login accounts can be used.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SAML"},
			description = "SAML configurations can be set(Regular to SAML).")
	public void TC_17147(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;
		try {

			//Gets the driver
			//---------------
			driver = WebDriverUtils.getDriver();
			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-Requisite: Set the registry key values
			//------------------------------------------
			Utility.removeRegistryKey("HKEY_LOCAL_MACHINE\\SOFTWARE\\Motive\\M-Files\\PRODUCTVERSION\\Server\\MFServer\\Authentication");//Removes the registry settings

			Log.message("Pre-Requisite: Removed the 'HKEY_LOCAL_MACHINE\\SOFTWARE\\Motive\\M-Files\\PRODUCTVERSION\\Server\\MFServer\\Authentication' key from the machine and restarted the iis.");

			//Step-1: Lauch the default login page URL
			//----------------------------------------
			driver.get(loginURL);//Launches the URL
			Utils.fluentWait(driver);//Waits for the page load

			if(!driver.getCurrentUrl().contains(loginURL))
				throw new Exception("Default login page is not launched while launching default login page url after removing authentication key. Current URL: '" + driver.getCurrentUrl() + "'");

			Log.message("1. Default login page is launched successfully.");

			//Step-2: Remove the Authentication key from the registry
			//-------------------------------------------------------
			Utility.configureSAMLorOAuthRegistrySettings();//Configures the SAML authentication in the machine
			Utility.setRegistryValue(dataPool.get("RegistryPath"), dataPool.get("RegistryKey"), dataPool.get("RegistryType"), dataPool.get("RegistryValue"));//Sets the registry key value
			Utility.resetIIS();//Resets the iis

			Log.message("2. Configured the SAML registry settings and restarted the iis.");

			//Step-3: Launch the default login URL
			//------------------------------------
			driver.get(loginURL);//Launches the URL
			Utils.fluentWait(driver);//Waits for the page load

			Log.message("3. Default login page is launched.");

			//Verification: Check if user is logged in successfully
			//-----------------------------------------------------
			if(driver.getCurrentUrl().equalsIgnoreCase(samlLoginURL))
				Log.pass("Test case passed. SAML configurations is set successfully.");
			else
				Log.fail("Test case failed. SAML login page is not displayed. [Actual URL loaded: '" + driver.getCurrentUrl() + "']", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try
			{
				Utility.setRegistryValue(dataPool.get("RegistryPath"), dataPool.get("RegistryKey"), dataPool.get("RegistryType"), dataPool.get("ResetRegistryValue"));//Sets the registry key value
				Utility.resetIIS();//Restarts the iis
				Utility.quitDriver(driver);//Quits the driver
			}
			catch(Exception e0){Log.exception(e0);}
		}//End finally

	}//End TC_17147

	/**
	 * TC_17148: Check correct credentials in SAML authentication service, but user account is missing (login account exists)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SAML"}, 
			description = "Check correct credentials in SAML authentication service, but user account is missing (login account exists)")
	public void TC_17148(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			Utility.markLoginAccountAsAdmin(samlDomainName + "\\" + samlFirstUserFullName, "nonadmin");//Marks the user as non-admin in the server

			Log.message("Pre-Requisite: Marked the user '" + samlDomainName + "\\" + samlFirstUserFullName + "' as non-admin in the server.");

			if(dataPool.get("UserAccountOperation").equalsIgnoreCase("Disabled"))
			{
				//Step-1: Dsiable the user account in the vault
				//----------------------------------------------
				Utility.enableORdisableUserAccountInVault(samlDomainName + "\\" + samlFirstUserFullName, "False", testVault);//Disable the user from the testVault
				Utility.enableORdisableUserAccountInVault(samlDomainName + "\\" + samlFirstUserFullName, "False", testVault1);//Disable the user from the testVault1

				Log.message("1. Disabled the user '" + samlDomainName + "\\" + samlFirstUserFullName + "' in the vaults('" + testVault + "' & '" + testVault1 + "')");
			}
			else
			{
				//Step-1: Remove the user account from the vault
				//----------------------------------------------
				Utility.removeUserAccountInVault(samlDomainName + "\\" + samlFirstUserFullName, testVault);//Removes the user from the testVault
				Utility.removeUserAccountInVault(samlDomainName + "\\" + samlFirstUserFullName, testVault1);//Removes the user from the testVault

				Log.message("1. Removed the user '" + samlDomainName + "\\" + samlFirstUserFullName + "' in the vaults('" + testVault + "' & '" + testVault1 + "')");
			}

			//Pre-Requisite: Configure the SAML
			//---------------------------------
			Utility.configureSAMLorOAuthRegistrySettings();//Configures the SAML authentication in the machine

			//Step-2: Launch the default login page url and navigate to the saml login page
			//------------------------------------------------------------------------------
			driver.get(loginURL);//Launches the default login page URL
			Utils.fluentWait(driver);//Waits for the page load

			if(!driver.getCurrentUrl().toLowerCase().contains("login.aspx"))
				throw new Exception("Default login page is not loaded. [Current URL: " + driver.getCurrentUrl() + "]");

			LoginPage loginPage = new LoginPage(driver);//Instantiate the login page
			loginPage.clickLoginLink(dataPool.get("LoginType"));

			if(!driver.getCurrentUrl().toLowerCase().contains(samlLoginURL.toLowerCase()))
				throw new Exception("SAML login page is not loaded after click saml link from default login page. [Current URL: " + driver.getCurrentUrl() + "]");

			Log.message("2. Navigated to the SAML login page from default login page.");

			//Step-3: Login using the user account which is not available in the vault
			//------------------------------------------------------------------------
			loginPage = new LoginPage(driver);//Instantiates the login page
			loginPage.setSAMLorOAuthUserName(samlFirstUserName);//Sets the saml user name
			loginPage.setSAMLorOAuthPassword(samlFirstUserPassword);//Sets the saml user password
			loginPage.clickSAMLorOAuthLoginBtn();//Clicks the saml login button

			Log.message("3. Loggedin in the SAML login page using the user credentials which is " + dataPool.get("UserAccountOperation") + " in/from the vault.", driver);

			//Verification: Check if error message is displayed in the default login page
			//---------------------------------------------------------------------------
			loginPage = new LoginPage(driver);//Instantiates the login page

			if(!MFilesDialog.exists(driver))//Instantiate the M-Files dialog
				throw new Exception("Test case failed. Warning dialog is not displayed after login as SAML user which user is " + dataPool.get("UserAccountOperation") + " in/from the vault.");

			MFilesDialog mfDialog = new MFilesDialog(driver);//Instantiates the M-Files Dialog
			if(mfDialog.getMessage().equalsIgnoreCase(dataPool.get("ErrorMessage")))
				Log.pass("Test case passed. Expected warning message is displayed when user account is " + dataPool.get("UserAccountOperation") + " in/from the vault (login account exists).");
			else
				Log.fail("Test case failed. Expected warning message(" + dataPool.get("ErrorMessage") + ") is not displayed when user account is " + dataPool.get("UserAccountOperation") + " in/from the vault(login account exists). [Actual message: '" + mfDialog.getMessage() + "']", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try
			{
				Utility.removeRegistryKey("HKEY_LOCAL_MACHINE\\SOFTWARE\\Motive\\M-Files\\PRODUCTVERSION\\Server\\MFServer\\Authentication");//Removes the registry settings
				Utility.configureUsers(testVault, samlFirstUserFullName, samlFirstUserFullName, "windows", "named", "none", samlDomainName, "admin", "FullControl", "internal");//Configure the windows user in server and restored vault
				Utility.configureUsers(testVault1, samlFirstUserFullName, samlFirstUserFullName, "windows", "named", "none", samlDomainName, "admin", "FullControl", "internal");//Configure the windows user in server and restored vault
				Utility.configureSAMLorOAuthRegistrySettings();//Configures the SAML authentication in the machine
				Utility.quitDriver(driver);//Quits the driver
			}
			catch(Exception e0){Log.exception(e0);}
		}//End finally

	}//End TC_17148


	/**
	 * TC_17149: Check correct credentials in SAML authentication service, but login account is either disabled or deleted 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SAML"}, 
			description = "Check correct credentials in SAML authentication service, but login account is either disabled or deleted")
	public void TC_17149(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			Utility.markLoginAccountAsAdmin(samlDomainName + "\\" + samlFirstUserFullName, "nonadmin");//Marks the user as non-admin in the server

			Log.message("Pre-Requisite: Marked the user '" + samlDomainName + "\\" + samlFirstUserFullName + "' as non-admin in the server.");

			if(dataPool.get("UserAccountOperation").equalsIgnoreCase("Disabled"))
			{
				//Step-1: Dsiable the user account in the vault
				//----------------------------------------------
				Utility.enableORdisableLoginAccountInServer(samlDomainName + "\\" + samlFirstUserFullName, "False");//Disable the user from the server

				Log.message("1. Disabled the user '" + samlDomainName + "\\" + samlFirstUserFullName + "' in the server.");
			}
			else
			{
				//Step-1: Remove the user account from the server
				//----------------------------------------------
				Utility.removeLoginAccountInServer(samlFirstUserFullName, "windows", samlDomainName);//Removes the user from the server

				Log.message("1. Removed the user '" + samlDomainName + "\\" + samlFirstUserFullName + "' in the server");
			}

			//Pre-Requisite: Configure the SAML
			//---------------------------------
			Utility.configureSAMLorOAuthRegistrySettings();//Configures the SAML authentication in the machine

			//Step-2: Launch the default login page url and navigate to the saml login page
			//------------------------------------------------------------------------------
			driver.get(loginURL);//Launches the default login page URL
			Utils.fluentWait(driver);//Waits for the page load

			if(!driver.getCurrentUrl().toLowerCase().contains("login.aspx"))
				throw new Exception("Default login page is not loaded. [Current URL: " + driver.getCurrentUrl() + "]");

			LoginPage loginPage = new LoginPage(driver);//Instantiate the login page
			loginPage.clickLoginLink(dataPool.get("LoginType"));

			if(!driver.getCurrentUrl().toLowerCase().contains(samlLoginURL.toLowerCase()))
				throw new Exception("SAML login page is not loaded after click saml link from default login page. [Current URL: " + driver.getCurrentUrl() + "]");

			Log.message("2. Navigated to the SAML login page from default login page.");

			//Step-3: Login using the user account which is not available in the vault
			//------------------------------------------------------------------------
			loginPage = new LoginPage(driver);//Instantiates the login page
			loginPage.setSAMLorOAuthUserName(samlFirstUserName);//Sets the saml user name
			loginPage.setSAMLorOAuthPassword(samlFirstUserPassword);//Sets the saml user password
			loginPage.clickSAMLorOAuthLoginBtn();//Clicks the saml login button

			Log.message("3. Loggedin in the SAML login page using the user credentials which is " + dataPool.get("UserAccountOperation") + " in/from the server.", driver);

			//Verification: Check if error message is displayed in the default login page
			//---------------------------------------------------------------------------
			loginPage = new LoginPage(driver);//Instantiates the login page

			if(!MFilesDialog.exists(driver))//Instantiate the M-Files dialog
				throw new Exception("Test case failed. Warning dialog is not displayed after login as SAML user which user is " + dataPool.get("UserAccountOperation") + "ed in/from the server.");

			MFilesDialog mfDialog = new MFilesDialog(driver);//Instantiates the M-Files Dialog
			if(mfDialog.getMessage().equalsIgnoreCase(dataPool.get("ErrorMessage")))
				Log.pass("Test case passed. Expected warning message is displayed when user account is " + dataPool.get("UserAccountOperation") + " in/from the server.");
			else
				Log.fail("Test case failed. Expected warning message(" + dataPool.get("ErrorMessage") + ") is not displayed when user account is " + dataPool.get("UserAccountOperation") + " in/from the server. [Actual message: '" + mfDialog.getMessage() + "']", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try
			{
				Utility.removeRegistryKey("HKEY_LOCAL_MACHINE\\SOFTWARE\\Motive\\M-Files\\PRODUCTVERSION\\Server\\MFServer\\Authentication");//Removes the registry settings
				Utility.configureUsers(testVault, samlFirstUserFullName, samlFirstUserFullName, "windows", "named", "none", samlDomainName, "admin", "FullControl", "internal");//Configure the windows user in server and restored vault
				Utility.configureUsers(testVault1, samlFirstUserFullName, samlFirstUserFullName, "windows", "named", "none", samlDomainName, "admin", "FullControl", "internal");//Configure the windows user in server and restored vault
				Utility.configureSAMLorOAuthRegistrySettings();//Configures the SAML authentication in the machine
				Utility.quitDriver(driver);//Quits the driver
			}
			catch(Exception e0){Log.exception(e0);}
		}//End finally

	}//End TC_17149

	/**
	 * TC_17150: Default login with SAML and access to several vaults
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SAML"}, 
			description = "Default login with SAML and access to several vaults")
	public void TC_17150(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Gets the driver
			//---------------
			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1: Launch the login URL
			//----------------------------
			driver.get(loginURL);//Launches the URL
			Utils.fluentWait(driver);//Waits for the page load

			Log.message("1. M-Files default login page is launched.");

			//Step-2: Click the SAML authetication link in the default login page
			//-------------------------------------------------------------------
			LoginPage loginPage = new LoginPage(driver);//Instantaites the login page
			loginPage.clickLoginLink(dataPool.get("LoginType"));//Selects the login type in the login page

			if(!driver.getCurrentUrl().contains(samlLoginURL))
				throw new Exception("SAML login page url("+ samlLoginURL +") is not loaded.[Loaded URL: "+ driver.getCurrentUrl() +"]");

			Log.message("2. Clicked the SAML login link in the login page and Entered into the SAML login page", driver);

			//Step-3: Login with the SAML credentials
			//---------------------------------------
			loginPage = new LoginPage(driver);//Re-Instantiates the login page
			loginPage.setSAMLorOAuthUserName(samlFirstUserName);//Enter the user id
			loginPage.setSAMLorOAuthPassword(samlFirstUserPassword);//enter the password
			loginPage.clickSAMLorOAuthLoginBtn();//Clicks the login button

			Log.message("3. Logged in using SAML credentials in SAML Login page.");

			//Step-4: Select the vault in the vault list in login page
			//--------------------------------------------------------
			loginPage = new LoginPage(driver);//Re-Instantiates the login page

			if(!loginPage.isVaultListDisplayed())
				throw new Exception("Test case failed. Vault list is not displayed in login page when user have access to several vaults.");

			loginPage.selectVault(testVault1);//Selects the vault in the login page

			Log.message("4. Vault list is displayed when user have access to several vaults and Vault - '" + testVault1 + "' is selected in the login page.");

			//Verification: Check if correct user name is displayed in the user menu
			//----------------------------------------------------------------------
			HomePage homePage = new HomePage(driver);//Instantiates the Home page
			if(homePage.menuBar.verifyLoggedInUser(samlFirstUserFullName))
				Log.pass("Test case passed. Default login with SAML and access to several vaults is working as expected.");
			else
				Log.fail("Test case failed. Default login with SAML and access to several vaults is not working as expected.[Logged in user name is different.]", driver);

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

	}//End TC_17150

	/**
	 * TC_17151: Correct accounts in mfiles but not in saml
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SAML"}, 
			description = "Correct accounts in mfiles but not in saml")
	public void TC_17151(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Gets the driver
			//---------------
			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1: Launch the login URL
			//-----------------------------------------------
			driver.get(loginURL);//Launches the URL
			Utils.fluentWait(driver);//Waits for the page load

			Log.message("1. M-Files default login page is launched.");

			//Step-2: Click the SAML authetication link in the default login page
			//-------------------------------------------------------------------
			LoginPage loginPage = new LoginPage(driver);//Instantaites the login page
			loginPage.clickLoginLink(dataPool.get("LoginType"));//Selects the login type in the login page

			if(!driver.getCurrentUrl().contains(samlLoginURL))
				throw new Exception("SAML login page url("+ samlLoginURL +") is not loaded.[Loaded URL: "+ driver.getCurrentUrl() +"]");

			Log.message("2. Clicked the SAML login link in the login page and Entered into the SAML login page", driver);

			//Step-3: Login with the SAML credentials
			//---------------------------------------
			loginPage = new LoginPage(driver);//Re-Instantiates the login page
			loginPage.setSAMLorOAuthUserName(userName);//Enter the user id
			//loginPage.setSAMLorOAuthPassword(password);//enter the password
			//loginPage.clickSAMLorOAuthLoginBtn();//Clicks the login button

			Log.message("3. Enterted the correct M-Files credentials in the SAML login page.");

			//Verification: Check if user is not able to login
			//------------------------------------------------
			if(loginPage.getSAMLorOAuthLoginErrorMessage().equalsIgnoreCase(dataPool.get("ErrorMessage")))
				Log.pass("Test case passed. Correct accounts in mfiles but not in saml is working as expected.");
			else
				Log.fail("Test case failed. Correct accounts in mfiles but not in saml is not working as expected.[Expected error message('" + dataPool.get("ErrorMessage") + "') is not displayed. [Actual error message: " + loginPage.getSAMLorOAuthLoginErrorMessage() + "].", driver);

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

	}//End TC_17151

}//End SAMLAuthentication Class

package MFClient.Tests;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.Utils;
import genericLibrary.WebDriverUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import MFClient.Pages.ConfigurationPage;
import MFClient.Pages.HomePage;
import MFClient.Pages.LoginPage;
import MFClient.Wrappers.Caption;
import MFClient.Wrappers.ConfigurationPanel;
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class ConfigSettings {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String userName = null;
	public static String password = null;
	public static String testVault = null;
	public static String configURL = null;
	public static String productVersion = null;
	public static String userFullName = null;
	public static String driverType = null;
	public static String className = null;
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
			userName = xmlParameters.getParameter("UserName");
			password = xmlParameters.getParameter("Password");
			testVault = xmlParameters.getParameter("VaultName");	
			configURL = xmlParameters.getParameter("ConfigurationURL");
			driverType = xmlParameters.getParameter("driverType");

			className = this.getClass().getSimpleName().toString().trim();
			if (driverType.equalsIgnoreCase("IE"))
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + driverType.toUpperCase().trim() + xmlParameters.getParameter("driverVersion").trim();
			else
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + driverType.toUpperCase().trim();

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
	 * 25.7.1: Password should be in encrypted value
	 */
	/*@Test(groups = {"Sprint25", "Password"}, 
			description = "Password should be in encrpted value")*/
	public void SprintTest25_7_1() throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();



			//1. Navigate to Login Page
			//--------------------------
			driver.get(loginURL);
			LoginPage loginPage = new LoginPage(driver);
			loginPage.setUserName(userName);
			loginPage.setPassword(password);

			Log.message("1. Navigate to Login Page");

			//Verification: To Verify if the password is not visible when accessed through the debugger
			//-------------------------------------------------------------------------------------------
			String pass = loginPage.getPassword();

			if(!pass.equals(password))
				Log.pass("Test Case Passed. The Password was not visible through debugging");
			else
				Log.fail("Test Case Failed. The Password was visible through debugging.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 25.7.2: Password should be in encrypted value for auto-login password
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "Password"}, 
			description = "Password should be in encrpted value for auto-login password")
	public void SprintTest25_7_2(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;  ConfigurationPage configurationPage = null;

		try {



			driver = WebDriverUtils.getDriver();

			//1. Login to Configuration
			//--------------------------
			configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			/*driver.get(configURL);

			LoginPage loginPage = new LoginPage(driver);
			ConfigurationPage configurationPage = loginPage.loginToConfigurationUI(userName, password);*/

			Log.message("1. Logged in to Configuiration");

			//2. Set the configuration for automatic login
			//--------------------------------------------

			configurationPage.treeView.clickTreeViewItem("General settings>>General");
			//.chooseGeneralSettings("General settings>>General");

			configurationPage.configurationPanel.setAutoLogin(true);
			configurationPage.configurationPanel.setDefaultAuthType("M-FILES USER");
			configurationPage.configurationPanel.setAutoLoginUserName(userName);
			configurationPage.configurationPanel.setAutoLoginPassword(password);
			//configurationPage.configurationPanel.setAutoLoginDomain(dataPool.get("Domain"));
			configurationPage.configurationPanel.setAutoLoginVault(testVault);

			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved successfully.");

			if (!configurationPage.logOut())
				throw new Exception("Error while logout from configuration web page.");

			Log.message("2. Set the configuration for automatic login and logged out from Configuration webpage.");

			//3. Navigate to Login Page
			//--------------------------
			driver.get(loginURL);

			LoginPage loginPage = new LoginPage(driver);

			Log.message("3. Navigated to Login Page");

			//Verification: To Verify if the password is not visible when accessed through the debugger
			//-------------------------------------------------------------------------------------------
			String pass = loginPage.getPassword();

			if(pass != password)
				Log.pass("Test Case Passed. The Password was not visible through debugging");
			else
				Log.fail("Test Case Failed. The Password was visible through debugging.", driver);



			/*driver.get(configURL);

			loginPage = new LoginPage(driver);
			configurationPage = loginPage.loginToConfigurationUI(userName, password);

			configurationPage.treeView.clickTreeViewItem("General settings>>General");
			ConfigGeneral config = new ConfigGeneral(driver); 
			config.SetAutoLogin(false);

			configurationPage.clickSaveButton();

			configurationPage.clickOKBtnOnSaveDialog();*/

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if (driver != null)
			{
				try
				{
					configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

					configurationPage.treeView.clickTreeViewItem("General settings>>General");

					configurationPage.configurationPanel.setAutoLogin(false);

					configurationPage.configurationPanel.saveSettings();
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
	 * 25.7.3: Login should fail for pasted the encrypted password
	 */
	/*@Test(groups = {"Sprint25", "Password"}, 
			description = "Login should fail for pasted the encrypted password")*/
	public void SprintTest25_7_3() throws Exception {

		driver = null; 


		try {



			driver = WebDriverUtils.getDriver();

			//1. Navigate to Login Page
			//--------------------------
			driver.get(loginURL);
			LoginPage loginPage = new LoginPage(driver);
			loginPage.setUserName(userName);
			loginPage.setPassword(password);

			Log.message("1. Navigate to Login Page");

			//2. Copy the Encrypted password and use it for login
			//-----------------------------------------------------
			String pass = loginPage.getPassword();
			loginPage.setPassword(pass);
			loginPage.clickLoginBtn();


			Log.message("2. Copied the Encrypted password and used it for login");

			//Verification: To Verify if the password is not visible when accessed through the debugger
			//-------------------------------------------------------------------------------------------
			if(driver.getCurrentUrl().equals(loginURL) && loginPage.getErrorMessage().equals("Authentication failed."))
				Log.pass("Test Case Passed. Login failed when copy of encrypted password was used.");
			else
				Log.fail("Test Case Failed. Login was successful when encrypted password was copied and used.", driver); 

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 25.9.1 : Check for applet area
	 */
	@Test(groups = {"Sprint25", "applet", "SKIP_JavaApplet"}, 
			description = "Check for applet area")
	public void SprintTest25_9_1() throws Exception {

		driver = null; 


		try {



			driver = WebDriverUtils.getDriver();

			//1. Login to Configuration
			//--------------------------
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			Log.message("1. Logged in to Configuiration");

			//2. Set the required settings
			//----------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);

			ConfigurationPanel configSettingsPanel = new  ConfigurationPanel(driver);
			configSettingsPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);
			configSettingsPanel.setJavaApplet(Caption.ConfigSettings.Config_Enable.Value);

			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			if (!configurationPage.logOut())
				throw new Exception("Configuration page is not logged out to login page.");

			Log.message("2. Set the required settings");

			//3. Login to the Test vault
			//---------------------------
			//driver.get(loginURL);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false);

			Log.message("3. Login to the Test Vault.");

			//Verification: To verify if the Java applet is not loaded
			//---------------------------------------------------------
			if(homePage.taskPanel.isAppletEnabled())
				Log.pass("Test Case Passed. Java applet was enabled as expected.");
			else
				Log.fail("Test Case Failed. Java applet was not enabled.", driver);


		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {

			if(driver != null){
				try
				{
					driver.get(configURL);
					ConfigurationPage configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);

					ConfigurationPanel configSettingsPanel = new  ConfigurationPanel(driver);
					configSettingsPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);
					configurationPage.configurationPanel.saveSettings();
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
	 * 25.9.3: No Applet in Web Access Configuration page
	 */
	@Test(groups = {"Sprint25", "Password"}, 
			description = "No Applet in Web Access Configuration page")
	public void SprintTest25_9_3() throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//1. Login to the application
			//-----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false);

			Log.message("1. Logged in to the application");

			//Verificaiton: To verify if the Java applet is loaded
			//-----------------------------------------------------
			if(!homePage.taskPanel.isAppletEnabled())
				Log.pass("Test Case Passed. Java applet was not enabled as expected.");
			else
				Log.fail("Test Case Failed. Java applet was enabled.", driver);


		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 25.9.4: No Applet and task pane view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "Password"}, 
			description = "No Applet and task area view")
	public void SprintTest25_9_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to Configuration
			//--------------------------
			driver.get(configURL);

			LoginPage loginPage = new LoginPage(driver);
			ConfigurationPage configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Set the required settings
			//----------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);

			ConfigurationPanel configSettingsPanel = new  ConfigurationPanel(driver);
			configSettingsPanel.setLayout(dataPool.get("Layout"));

			if (configSettingsPanel.isJavaAppletEnabled())
				configSettingsPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);

			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			configurationPage.logOut();

			Log.message("2. Set the required settings");

			//3. Login to the Test vault
			//---------------------------
			driver.get(loginURL);
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);

			Log.message("3. Login to the Test Vault.");

			//Verification: To verify if the Java applet is not loaded
			//---------------------------------------------------------
			if(!homePage.taskPanel.isAppletEnabled() && !homePage.isTaskPaneDisplayed())
				Log.pass("Test Case Passed. The task pane was not displayed and Java applet was not enabled as expected.");
			else if(!homePage.taskPanel.isAppletEnabled() && homePage.isTaskPaneDisplayed())
				Log.fail("Test Case Failed. Java applet was disabled but task pane was visible.", driver);
			else
				Log.fail("Test Case Failed. The Java applet was enabled and the task pane was displayed.", driver);

			driver.get(configURL);

			configurationPage = new ConfigurationPage(driver);
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);

			configSettingsPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);
			configSettingsPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);

			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 25.9.5: Task area view but GOTO items
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "Password"}, 
			description = "Task area with GOTO items only")
	public void SprintTest25_9_5(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

		try {

			//1. Login to Configuration
			//--------------------------


			driver = WebDriverUtils.getDriver();

			driver.get(configURL);

			LoginPage loginPage = new LoginPage(driver);
			ConfigurationPage configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Set the required settings
			//----------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);

			ConfigurationPanel configSettingsPanel = new  ConfigurationPanel(driver);
			configSettingsPanel.setLayout(dataPool.get("Layout"));

			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			configurationPage.logOut();

			Log.message("2. Set the required settings");

			//3. Login to the Test vault
			//---------------------------
			driver.get(loginURL);
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);


			Log.message("3. Login to the Test Vault.");

			//Verification: To verify if the Java applet is not loaded
			//---------------------------------------------------------
			boolean flag = false;
			try {
				WebElement GoToBar = driver.findElement(By.cssSelector("body[class='ui-widget ui-layout-container']>div[class='ui-layout-west ui-layout-pane ui-layout-pane-west']"));
				if(GoToBar.isDisplayed() && GoToBar.isEnabled())
					flag = true;
			}

			catch (Exception e) {
				if (e.getClass().toString().contains("NoSuchElementException")) 
					flag = false;
			} //End catch

			if(!flag)
				Log.fail("Tets Case Failed. The GoTo pane was not visible.", driver);
			else if(!homePage.isTaskPaneDisplayed())
				Log.pass("Test Case Passed. The task pane was not displayed, Java applet was not enabled as expected.");
			else
				Log.fail("Test Case Failed. The task pane was displayed.", driver);

			driver.get(configURL);

			configurationPage = new ConfigurationPage(driver);
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);

			configSettingsPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);

			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 25.9.7: No Applet in Web Access Configuration page
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "Password", "SKIP_JavaApplet"}, 
			description = "No Applet in Web Access Configuration page")
	public void SprintTest25_9_7(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//1. Login to Configuration
			//--------------------------
			driver.get(configURL);

			LoginPage loginPage = new LoginPage(driver);
			ConfigurationPage configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Set the configuration as no applet
			//--------------------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);

			ConfigurationPanel configSettingsPanel = new  ConfigurationPanel(driver);
			configSettingsPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);

			configSettingsPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);

			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			configurationPage.logOut();

			Log.message("2. Set the configuration as no applet.");

			//3. Login to the Test vault
			//---------------------------
			driver.get(loginURL);
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);

			if(homePage.taskPanel.isAppletEnabled())
				Log.fail("Test Case Failed. The Applet was enabled before setting the layout as default.", driver);

			Log.message("3. Login to the Test Vault.");

			//4. Set the configuration as 'Default'
			//--------------------------------------
			driver.get(configURL);

			configurationPage = new ConfigurationPage(driver);
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);

			configSettingsPanel.setJavaApplet(Caption.ConfigSettings.Config_Enable.Value);


			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			configurationPage.logOut();

			Log.message("4. Set the configuration as 'Default'");

			//Verification: To verify if the Java applet is loaded
			//-----------------------------------------------------
			driver.get(loginURL);
			homePage = loginPage.loginToWebApplication(userName, password, testVault);
			if(homePage.taskPanel.isAppletEnabled())
				Log.pass("Test Case Passed. Java applet was enabled as expected when Java applet is enabled.");
			else
				Log.fail("Test Case Failed. Java applet was not enabled when Java applet is enabled", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 25.10.1A: General settings 'General - General Settings'
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "UI"}, 
			description = "General settings 'General - General Settings'")
	public void SprintTest25_10_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to Configuration
			//--------------------------
			driver.get(configURL);

			LoginPage loginPage = new LoginPage(driver);
			ConfigurationPage configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Click the 'General' link 
			//----------------------------
			configurationPage.treeView.clickTreeViewItem("General");


			Log.message("2. Clicked the 'General' link.");

			//Verification: To verify the configuration title
			//------------------------------------------------
			WebElement configTitle = driver.findElement(By.id("configurationTitle"));

			if(configTitle.isDisplayed() && configTitle.getText().equals(dataPool.get("ConfigurationTitle")))
				Log.pass("Test Case Passed. The configuration title has the expected text.");
			else
				Log.fail("Test Case Failed. The configuration title did not display the expected text.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 25.10.1B: General settings 'Server Information'
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "UI"}, 
			description = "General settings 'Server Information'")
	public void SprintTest25_10_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to Configuration
			//--------------------------
			driver.get(configURL);

			LoginPage loginPage = new LoginPage(driver);
			ConfigurationPage configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Click the 'General' link 
			//----------------------------
			configurationPage.treeView.clickTreeViewItem("General");


			Log.message("2. Clicked the 'General' link.");

			//Verification: To verify the 'Server Information' title
			//--------------------------------------------------------
			WebElement configTitle = driver.findElement(By.cssSelector("div[id='configgeneral']>table>tbody>tr>td[class='listSubTitle']"));

			if(configTitle.isDisplayed() && configTitle.getText().contains(dataPool.get("ConfigurationTitle")))
				Log.pass("Test Case Passed. The 'Server Information' text was not displayed.");
			else
				Log.fail("Test Case Failed. The 'Server Information' text was not displayed.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 25.10.1C: General settings 'Page Title'
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "UI"}, 
			description = "General settings 'Page Title'")
	public void SprintTest25_10_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {

			//1. Login to Configuration
			//--------------------------



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			driver.get(configURL);

			LoginPage loginPage = new LoginPage(driver);
			ConfigurationPage configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Click the 'General' link 
			//----------------------------
			configurationPage.treeView.clickTreeViewItem("General");


			Log.message("2. Clicked the 'General' link.");

			//Verification: To verify the 'Page title' label
			//------------------------------------------------
			WebElement configTitle = driver.findElement(By.xpath("//td[contains(text(), '" + dataPool.get("Caption") + "')]"));

			if(configTitle.isDisplayed())
				Log.pass("Test Case Passed. The 'Page title' label was displayed.");
			else
				Log.fail("Test Case Failed. The 'Page title' label was not displayed.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 25.10.1D: General settings 'Force M-files user login'
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "UI"}, 
			description = "General settings 'Force M-files user login'")
	public void SprintTest25_10_1D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {

			//1. Login to Configuration
			//--------------------------


			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			int count =0;
			driver.get(configURL);

			LoginPage loginPage = new LoginPage(driver);
			ConfigurationPage configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Click the 'General' link 
			//----------------------------
			configurationPage.treeView.clickTreeViewItem("General");


			Log.message("2. Clicked the 'General' link.");

			//Verification: To verify the 'Force M-files user login' label
			//-------------------------------------------------------------
			List<WebElement> label = driver.findElements(By.className("listelement"));

			for(count =0; count < label.size(); count++) {
				if(label.get(count).getText().trim().equals(dataPool.get("Caption")))
					break;
			}

			if(count != label.size() && label.get(count).isDisplayed())
				Log.pass("Test Case Passed. The 'Force M-files user login' label was displayed.");
			else
				Log.fail("Test Case Failed. The 'Force M-files user login' label was not displayed.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 25.10.1E: General settings 'Automatic login'
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "UI"}, 
			description = "General settings 'Automatic login'")
	public void SprintTest25_10_1E(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;


		try {

			//1. Login to Configuration
			//--------------------------


			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			driver.get(configURL);

			LoginPage loginPage = new LoginPage(driver);
			ConfigurationPage configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Click the 'General' link 
			//----------------------------
			configurationPage.treeView.clickTreeViewItem("General");

			Log.message("2. Clicked the 'General' link.");

			//Verification: To verify the 'Automatic login' label
			//-----------------------------------------------------
			List<WebElement> label = driver.findElements(By.className("listelement"));

			int count =0;
			for(count =0; count < label.size(); count++) {
				if(label.get(count).getText().trim().equals(dataPool.get("Caption")))
					break;
			}

			if(count != label.size() && label.get(count).isDisplayed())
				Log.pass("Test Case Passed. The 'Automatic login' label was displayed.");
			else
				Log.fail("Test Case Failed. The 'Automatic login' label was not displayed.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 25.10.1F: General settings 'Default Authentication type'
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "UI"}, 
			description = "General settings 'Default Authentication type'")
	public void SprintTest25_10_1F(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {

			//1. Login to Configuration
			//--------------------------


			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);
			driver.get(configURL);

			LoginPage loginPage = new LoginPage(driver);
			ConfigurationPage configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Click the 'General' link 
			//----------------------------
			configurationPage.treeView.clickTreeViewItem("General");


			Log.message("2. Clicked the 'General' link.");

			//Verification: To verify the 'Default Authentication type' label
			//----------------------------------------------------------------
			WebElement configTitle = driver.findElement(By.xpath("//td[contains(text(), '" + dataPool.get("Caption") + "')]"));

			if(configTitle.isDisplayed())
				Log.pass("Test Case Passed. The 'Default Authentication type' label was displayed.");
			else
				Log.fail("Test Case Failed. The 'Default Authentication type' label was not displayed.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 25.10.1G: General settings 'Allowed IP range'
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "UI"}, 
			description = "General settings 'Allowed IP range'")
	public void SprintTest25_10_1G(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {

			//1. Login to Configuration
			//--------------------------


			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			driver.get(configURL);

			LoginPage loginPage = new LoginPage(driver);
			ConfigurationPage configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Click the 'General' link 
			//----------------------------
			configurationPage.treeView.clickTreeViewItem("General");


			Log.message("2. Clicked the 'General' link.");

			//Verification: To verify the 'Allowed IP range' label
			//-------------------------------------------------------
			WebElement configTitle = driver.findElement(By.xpath("//td[contains(text(), '" + dataPool.get("Caption") + "')]"));

			if(configTitle.isDisplayed())
				Log.pass("Test Case Passed. The 'Allowed IP range' label was displayed.");
			else
				Log.fail("Test Case Failed. The 'Allowed IP range' label was not displayed.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 25.10.1H: General settings 'Save'
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "UI"}, 
			description = "General settings 'Save'")
	public void SprintTest25_10_1H(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {

			//1. Login to Configuration
			//--------------------------


			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			driver.get(configURL);

			LoginPage loginPage = new LoginPage(driver);
			ConfigurationPage configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Click the 'General' link 
			//----------------------------
			configurationPage.treeView.clickTreeViewItem("General");


			Log.message("2. Clicked the 'General' link.");

			//Verification: To verify the 'Save' button
			//-----------------------------------------
			WebElement configTitle = driver.findElement(By.id("saveSettings"));

			if(!configTitle.isDisplayed())
			{
				JavascriptExecutor jse = (JavascriptExecutor) driver;
				jse.executeScript("arguments[0].scrollIntoView();", configTitle);
			}

			if(configTitle.isDisplayed() && configTitle.getAttribute("value").contains(dataPool.get("Caption")))
				Log.pass("Test Case Passed. The 'Save' button was displayed.");
			else
				Log.fail("Test Case Failed. The 'Save' button was not displayed.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 25.10.1I: General settings 'Reset'
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "UI"}, 
			description = "General settings 'Reset'")
	public void SprintTest25_10_1I(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {

			//1. Login to Configuration
			//--------------------------


			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			driver.get(configURL);

			LoginPage loginPage = new LoginPage(driver);
			ConfigurationPage configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Click the 'General' link 
			//----------------------------
			configurationPage.treeView.clickTreeViewItem("General");


			Log.message("2. Clicked the 'General' link.");

			//Verification: To verify the 'Reset' button
			//-----------------------------------------
			WebElement configTitle = driver.findElement(By.id("reloadSettings"));

			if(!configTitle.isDisplayed())
			{
				JavascriptExecutor jse = (JavascriptExecutor) driver;
				jse.executeScript("arguments[0].scrollIntoView();", configTitle);
			}

			if(configTitle.isDisplayed() && configTitle.getAttribute("value").contains(dataPool.get("Caption")))
				Log.pass("Test Case Passed. The 'Reset' button was displayed.");
			else
				Log.fail("Test Case Failed. The 'Reset' button was not displayed.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 25.10.2A: Vault general settings 'Default View'
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "UI"}, 
			description = "Vault general settings 'Default View'")
	public void SprintTest25_10_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {

			//1. Login to Configuration
			//--------------------------


			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			driver.get(configURL);

			LoginPage loginPage = new LoginPage(driver);
			ConfigurationPage configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Click the vault link 
			//----------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);


			Log.message("2. Clicked the vault link.");

			//Verification: To verify the 'Default View' button
			//-----------------------------------------
			WebElement configTitle = driver.findElement(By.xpath("//td[contains(text(), '" + dataPool.get("Caption") + "')]"));

			if(configTitle.isDisplayed())
				Log.pass("Test Case Passed. The 'Default View' label was displayed.");
			else
				Log.fail("Test Case Failed. The 'Default View' label was not displayed.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 25.10.2B: Vault general settings 'Other [specify view ID]'
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "UI"}, 
			description = "Vault general settings 'Other [specify view ID]'")
	public void SprintTest25_10_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {

			//1. Login to Configuration
			//--------------------------


			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			driver.get(configURL);

			LoginPage loginPage = new LoginPage(driver);
			ConfigurationPage configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Click the vault link 
			//----------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);


			Log.message("2. Clicked the vault link.");

			//Verification: To verify the caption "Vault general settings 'Other [specify view ID]'"for other views
			//------------------------------------------------------------------------------------------------------
			ConfigurationPanel configSettingsPanel = new ConfigurationPanel(driver);

			//Variable Declaration
			List<WebElement> defaultViewOptions; //Stores the web element of the default view select box
			int optionCount; //Stores the number of options that are available in the list
			int itemIdx; //Stores the item index

			List<WebElement> vaultTableRows=driver.findElements(By.cssSelector("div[id='configvault'][style*='display: block;']>table[class='fullLength']"));
			WebElement defaultViewRow = configSettingsPanel.getWebElement(vaultTableRows, "Default view:"); //Web Element of default view row

			defaultViewOptions = defaultViewRow.findElements(By.cssSelector("select[id='gotoItemSel']>option")); //Web Element options of the default view list


			optionCount = defaultViewOptions.size(); //Number of available default view options in the list

			for (itemIdx=0; itemIdx<optionCount; itemIdx++)  {//Loops to identify the web element of the default view to be selected
				if (defaultViewOptions.get(itemIdx).getAttribute("value").toUpperCase().trim().contains("OTHER")) 
					break;
			}

			if(defaultViewOptions.get(itemIdx).getText().equals(dataPool.get("Caption")))
				Log.pass("Test Case Passed. The text 'Other [specify view ID]' was present as expected.");
			else
				Log.fail("Test Case Failed. The text " + defaultViewOptions.get(itemIdx).getText() + " was present instead of 'Other [specify view ID]'.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 25.10.2C: Vault general settings - Layout dropdown
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "UI"}, 
			description = "Vault general settings - Layout dropdown")
	public void SprintTest25_10_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

		try {

			//1. Login to Configuration
			//--------------------------


			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			driver.get(configURL);

			LoginPage loginPage = new LoginPage(driver);
			ConfigurationPage configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Click the vault link 
			//----------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);


			Log.message("2. Clicked the vault link.");

			//Verification: To verify the caption "Vault general settings 'Other [specify view ID]'"for other views
			//------------------------------------------------------------------------------------------------------
			ConfigurationPanel configSettingsPanel = new ConfigurationPanel(driver);

			/*//Variable Declaration
			List<WebElement> layoutOptions; //Stores the web element of the default view select box
			int optionCount; //Stores the number of options that are available in the list
			int itemIdx; //Stores the item index
			int count = 0;

			List<WebElement> vaultTableRows=driver.findElements(By.cssSelector("div[id='configvault'][style*='display: block;']>table[class='fullLength']"));
			WebElement defaultViewRow = configSettingsPanel.getWebElement(vaultTableRows, "Layout:"); //Web Element of default view row

			layoutOptions = defaultViewRow.findElements(By.cssSelector("select[id='ddlLayout']>option")); //Web Element options of the default view list

			optionCount = layoutOptions.size(); //Number of available default view options in the list*/
			int i,j = 1;
			String[] expectedOptions = dataPool.get("Options").split("\n");
			String[] actualOptions = new String[configSettingsPanel.getallLayouts().length];
			actualOptions = configSettingsPanel.getallLayouts();
			for (int k=0; k < actualOptions.length ;k++)
				System.out.println(actualOptions[k]);
			Arrays.sort(expectedOptions);	
			Arrays.sort(actualOptions);	

			/*for (itemIdx=0; itemIdx<expectedOptions.length; itemIdx++)  {//Loops to identify the web element of the default view to be selected
				for(count = 0; count < optionCount; count++) {
					if(layoutOptions.get(count).getText().contains(expectedOptions[itemIdx]))
						break;
				}

				if(count == optionCount)
					break;

			}*/

			for( i=0;i< actualOptions.length;i++)
			{
				if  (!(actualOptions[i].equalsIgnoreCase(expectedOptions[i])))
				{
					j=0;
					break;
				}
			}



			if(j !=0)
				Log.pass("Test Case Passed. The expected options were found in the layout dropdown box.");
			else
				Log.fail("Test Case Failed. The expected option " + expectedOptions[i] + " was not found in the layout dropdown.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 25.10.4A: Controls 'Save View settings'
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "UI"}, 
			description = "Controls 'Save view settings'")
	public void SprintTest25_10_4A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {

			//1. Login to Configuration
			//--------------------------


			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			driver.get(configURL);

			LoginPage loginPage = new LoginPage(driver);
			ConfigurationPage configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Click the controls link 
			//----------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);


			Log.message("2. Clicked the controls link.");

			//Verification: To verify the 'Save view settings'
			//--------------------------------------------------
			List<WebElement> row = driver.findElements(By.cssSelector("div[id='configControls']>table>tbody>tr>td"));
			int count = 0;
			for(count = 0; count < row.size(); count++) {
				if(row.get(count).getText().contains(dataPool.get("Label")))
					break;
			}

			if(count != row.size()) {
				row = row.get(count+1).findElements(By.cssSelector("span"));
				if(row.get(0).getText().contains(dataPool.get("Allow")) && row.get(1).getText().contains(dataPool.get("Disallow")))
					Log.pass("Test Case Passed. The Text changes for Save View settings was verified successfully.");
				else
					Log.fail("Test Case Failed. The Text changes for the 'Allow & Disallow' of save view settings was not as expected.", driver);
			}
			else
				Log.fail("Test Case Failed. The option " + dataPool.get("Label") + " was not found.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 25.10.4B: Controls 'Checkout'
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "UI"}, 
			description = "Controls 'Checkout'")
	public void SprintTest25_10_4B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {

			//1. Login to Configuration
			//--------------------------


			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			driver.get(configURL);

			LoginPage loginPage = new LoginPage(driver);
			ConfigurationPage configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Click the controls link 
			//----------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);


			Log.message("2. Clicked the controls link.");

			//Verification: To verify the 'Checkout' settings
			//-------------------------------------------------
			List<WebElement> row = driver.findElements(By.cssSelector("div[id='configControls']>table>tbody>tr>td"));
			int count = 0;
			for(count = 0; count < row.size(); count++) {
				if(row.get(count).getText().contains(dataPool.get("Label")))
					break;
			}

			if(count != row.size())
				Log.pass("Test Case Passed. The Option was changed to "+dataPool.get("Label"));
			else
				Log.fail("Test Case Failed. The option " + dataPool.get("Label") + " was not found.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 25.10.4C: Controls 'Document card'
	 */
	/*@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "UI"}, 
			description = "Controls 'Document card'")*/
	public void SprintTest25_10_4C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;
		try {

			//1. Login to Configuration
			//--------------------------


			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			driver.get(configURL);

			LoginPage loginPage = new LoginPage(driver);
			ConfigurationPage configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Click the controls link 
			//----------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);


			Log.message("2. Clicked the controls link.");

			//Verification: To verify the 'Document card' settings
			//-------------------------------------------------
			List<WebElement> row = driver.findElements(By.cssSelector("div[id='configControls']>table>tbody>tr>td"));
			int count = 0;
			for(count = 0; count < row.size(); count++) {
				if(row.get(count).getText().contains(dataPool.get("Label")))
					break;
			}

			if(count != row.size())
				Log.pass("Test Case Passed. The Option was changed to "+dataPool.get("Label"));
			else
				Log.fail("Test Case Failed. The option " + dataPool.get("Label") + " was not found.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 25.10.5A: Task area 'New commands'
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "UI"}, 
			description = "Task area 'New commands'")
	public void SprintTest25_10_5A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to Configuration
			//--------------------------
			driver.get(configURL);

			LoginPage loginPage = new LoginPage(driver);
			ConfigurationPage configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Click the Task area link 
			//----------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_TaskArea.Value);

			Log.message("2. Clicked the Task area link.");

			//Verification: To verify the 'New commands' label
			//-------------------------------------------------
			WebElement label = driver.findElement(By.xpath("//td[contains(text(), '" + dataPool.get("Label") + "')]"));
			if(label.isDisplayed())
				Log.pass("Test Case Passed. The Option was changed to "+dataPool.get("Label"));
			else
				Log.fail("Test Case Failed. The option " + dataPool.get("Label") + " was not found.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 25.10.5B: Task area 'Move into state commands'
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "UI"}, 
			description = "Task area 'Move into state commands'")
	public void SprintTest25_10_5B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {

			//1. Login to Configuration
			//--------------------------


			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			driver.get(configURL);

			LoginPage loginPage = new LoginPage(driver);
			ConfigurationPage configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Click the Task area link 
			//----------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_TaskArea.Value);

			Log.message("2. Clicked the Task area link.");

			//Verification: To verify the 'Move into state commands' label
			//-------------------------------------------------------------
			WebElement label = driver.findElement(By.xpath("//td[contains(text(), '" + dataPool.get("Label") + "')]"));
			if(label.isDisplayed())
				Log.pass("Test Case Passed. The Option was changed to "+dataPool.get("Label"));
			else
				Log.fail("Test Case Failed. The option " + dataPool.get("Label") + " was not found.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 25.10.5C: Task area 'View and Modify' 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "UI"}, 
			description = "Task area 'View and Modify'")
	public void SprintTest25_10_5C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {

			//1. Login to Configuration
			//--------------------------


			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			driver.get(configURL);

			LoginPage loginPage = new LoginPage(driver);
			ConfigurationPage configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Click the Task area link 
			//----------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_TaskArea.Value);

			Log.message("2. Clicked the Task area link.");

			//Verification: To verify the 'View and Modify' label
			//-------------------------------------------------------------
			WebElement label = driver.findElement(By.xpath("//td[contains(text(), '" + dataPool.get("Label") + "')]"));
			if(label.isDisplayed())
				Log.pass("Test Case Passed. The Option was changed to "+dataPool.get("Label"));
			else
				Log.fail("Test Case Failed. The option " + dataPool.get("Label") + " was not found.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 25.10.5D: Task area 'Make Copy' 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "UI"}, 
			description = "Task area 'Make Copy'")
	public void SprintTest25_10_5D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {

			//1. Login to Configuration
			//--------------------------


			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			driver.get(configURL);

			LoginPage loginPage = new LoginPage(driver);
			ConfigurationPage configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Click the Task area link 
			//----------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_TaskArea.Value);

			Log.message("2. Clicked the Task area link.");

			//Verification: To verify the 'Make Copy' label
			//----------------------------------------------
			WebElement label = driver.findElement(By.xpath("//td[contains(text(), '" + dataPool.get("Label") + "')]"));
			if(label.isDisplayed())
				Log.pass("Test Case Passed. The Option was changed to "+dataPool.get("Label"));
			else
				Log.fail("Test Case Failed. The option " + dataPool.get("Label") + " was not found.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 25.10.5E: Task area 'Go To shortcuts' 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "UI"}, 
			description = "Task area 'Go To shortcuts'")
	public void SprintTest25_10_5E(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {




			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to Configuration
			//--------------------------
			driver.get(configURL);

			LoginPage loginPage = new LoginPage(driver);
			ConfigurationPage configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Click the Task area link 
			//----------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_TaskArea.Value);


			Log.message("2. Clicked the Task area link.");

			//Verification: To verify the 'Go To shortcuts' label
			//----------------------------------------------------
			WebElement label = driver.findElement(By.xpath("//td[contains(text(), '" + dataPool.get("Label") + "')]"));
			if(label.isDisplayed())
				Log.pass("Test Case Passed. The Option was changed to "+dataPool.get("Label"));
			else
				Log.fail("Test Case Failed. The option " + dataPool.get("Label") + " was not found.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 29.1.1: Verify the default 'View' displayed when select the 'Root view' as Default view in MFWA Configuration settings.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIPIE11_MultiDriver", "Sprint25", "Password"}, 
			description = "Verify the default 'View' displayed when select the 'Root view' as Default view in MFWA Configuration settings.")
	public void SprintTest29_1_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE"))
			throw new SkipException("Second instance of browser will not be opened with active first session for '" + driverType + "' driver type.");

		driver = null; 
		WebDriver driver2 = null;
		ConcurrentHashMap <String, String> dataPool = null;
		ConfigurationPage configurationPage  = null;

		try {

			//1. Login to Configuration
			//--------------------------


			driver = WebDriverUtils.getDriver();
			driver2 = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			driver.get(configURL);

			LoginPage loginPage = new LoginPage(driver);
			configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Set the required settings
			//----------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);

			ConfigurationPanel configSettingsPanel = new  ConfigurationPanel(driver);
			configSettingsPanel.setDefaultView(dataPool.get("View"));

			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			configurationPage.logOut();

			Log.message("2. Set the required settings");

			//3. Login to the Test vault
			//---------------------------
			driver.get(loginURL);
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);

			Log.message("3. Login to the Test Vault.");

			//4. Navigate to any view
			//------------------------
			homePage.listView.navigateThroughView(dataPool.get("Path"));

			Log.message("4. Navigate to any view");

			//5. Login to the vault through another browser
			//----------------------------------------------
			HomePage homePage2 = LoginPage.launchDriverAndLogin(driver2, false);
			Utils.fluentWait(driver2);

			Log.message("5. Login to the vault through another browser");

			//Verification: To verify if the specified default view is loaded
			//----------------------------------------------------------------
			if(driver2.getCurrentUrl().endsWith("/views/") && homePage2.menuBar.GetBreadCrumbItem().trim().equals(testVault))
				Log.pass("Test case Passed. The Default view was loaded on login as expected.");
			else
				Log.fail("Test Case Failed. The default view was not loaded on login to vault", driver2);


		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {

			if (driver != null)
			{
				try
				{
					driver.get(configURL);

					configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);

					configurationPage.configurationPanel.setDefaultView("Home");

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}

			try {try {driver2.quit();} catch(Exception e0){}} catch(Exception e0){}
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 29.1.2: Verify the default 'View' displayed when select any view as Default view in MFWA Configuration settings.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIPIE11_MultiDriver", "Sprint25", "Password"}, 
			description = "Verify the default 'View' displayed when select the any view as Default view in MFWA Configuration settings.")
	public void SprintTest29_1_2(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE"))
			throw new SkipException("Second instance of browser will not be opened with active first session for '" + driverType + "' driver type.");

		driver = null; 
		WebDriver driver2 = null;
		ConcurrentHashMap <String, String> dataPool = null;
		ConfigurationPage configurationPage  = null;

		try {



			driver = WebDriverUtils.getDriver();
			driver2= WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to Configuration
			//--------------------------
			driver.get(configURL);

			LoginPage loginPage = new LoginPage(driver);
			configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Set the required settings
			//----------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);

			ConfigurationPanel configSettingsPanel = new  ConfigurationPanel(driver);
			configSettingsPanel.setDefaultView(dataPool.get("View"));

			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			configurationPage.logOut();

			Log.message("2. Set the required settings");

			//3. Login to the Test vault
			//---------------------------
			driver.get(loginURL);
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);


			Log.message("3. Login to the Test Vault.");

			//4. Navigate to any view
			//------------------------
			homePage.listView.navigateThroughView(dataPool.get("Path"));


			Log.message("4. Navigate to any view");

			//5. Login to the vault through another browser
			//----------------------------------------------
			HomePage homePage2 = LoginPage.launchDriverAndLogin(driver2, false);
			Utils.fluentWait(driver2);

			Log.message("5. Login to the vault through another browser");

			//Verification: To verify if the specified default view is loaded
			//---------------------------------------------------------
			if(!driver2.getCurrentUrl().endsWith("/views/") && homePage2.menuBar.GetBreadCrumbItem().trim().equals(testVault+">"+dataPool.get("View")))
				Log.pass("Test case Passed. The Default view was loaded on login as expected.");
			else
				Log.fail("Test Case Failed. The default view was not loaded on login to vault", driver2);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {

			if (driver != null)
			{
				try
				{
					driver.get(configURL);

					configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);

					configurationPage.configurationPanel.setDefaultView("Home");

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}

			try {driver2.quit();} catch(Exception e0){}
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 29.1.3: Verify the default 'View' displayed when select any view as Default view in MFWA Configuration settings.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIPIE11_MultiDriver", "Sprint25", "Password"}, 
			description = "Verify the default 'View' displayed when select the any view as Default view in MFWA Configuration settings.")
	public void SprintTest29_1_3(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE"))
			throw new SkipException("Second instance of browser will not be opened with active first session for '" + driverType + "' driver type.");

		driver = null; 
		WebDriver driver2 = null;
		ConcurrentHashMap <String, String> dataPool = null;
		ConfigurationPage configurationPage  = null;

		try {




			driver = WebDriverUtils.getDriver();
			driver2 = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to Configuration
			//--------------------------
			driver.get(configURL);

			LoginPage loginPage = new LoginPage(driver);
			configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Set the required settings
			//----------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);

			ConfigurationPanel configSettingsPanel = new  ConfigurationPanel(driver);
			configSettingsPanel.setDefaultView(dataPool.get("View"));

			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			configurationPage.logOut();

			Log.message("2. Set the required settings");

			//3. Login to the Test vault
			//---------------------------
			driver.get(loginURL);
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);


			Log.message("3. Login to the Test Vault.");

			//4. Navigate to any view
			//------------------------
			homePage.listView.navigateThroughView(dataPool.get("Path"));


			Log.message("4. Navigate to any view");

			//5. Login to the vault through another browser
			//----------------------------------------------
			HomePage homePage2 = LoginPage.launchDriverAndLogin(driver2, false);
			Utils.fluentWait(driver2);

			Log.message("5. Login to the vault through another browser");

			//Verification: To verify if the specified default view is loaded
			//---------------------------------------------------------
			if(driver2.getCurrentUrl().endsWith(dataPool.get("View")) && homePage2.menuBar.GetBreadCrumbItem().trim().equals(testVault+">"+dataPool.get("ViewName")))
				Log.pass("Test case Passed. The Default view was loaded on login as expected.");
			else
				Log.fail("Test Case Failed. The default view was not loaded on login to vault", driver2);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {

			if (driver != null)
			{
				try
				{
					driver.get(configURL);

					configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);

					configurationPage.configurationPanel.setDefaultView("Home");

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}

			try {driver2.quit();} catch(Exception e0){}
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 33.1.5 : Verify to Hiding GOTO items
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint33", "Breadcrumb"}, 
			description = "Verify to Hiding GOTO items.")
	public void SprintTest33_1_5(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		LoginPage loginPage = null;
		ConfigurationPage configurationPage = null;
		String[] options = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to Configuration
			//--------------------------
			driver.get(configURL);

			loginPage = new LoginPage(driver);
			configurationPage = loginPage.loginToConfigurationUI(userName, password);


			Log.message("1. Logged in to Configuiration");

			//2. Click the Task area link 
			//----------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_TaskArea.Value);


			Log.message("2. Clicked the Task area link.");

			//3. Hide all GoTo shortcuts
			//---------------------------
			options = dataPool.get("Options").split("\n");


			for(int count = 0; count < options.length; count++) 
				configurationPage.configurationPanel.setVaultCommands(options[count],"Hide");

			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			configurationPage.logOut();


			Log.message("3. Hide all GoTo shortcuts");

			//Step-4: Login to the vault
			//---------------------------
			driver.get(loginURL);

			loginPage = new LoginPage(driver);
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);


			Log.message("Step-4: Login to the vault.");

			//Verification: To verify if GoTo option is not displayed in the task pane
			//------------------------------------------------------------------------

			if(!homePage.taskPanel.isItemExists(dataPool.get("MenuItem")))
				Log.pass("Test case Passed. The Task Panel item " + dataPool.get("MenuItem") + " did not exist.");
			else
				Log.fail("Test Case Failed. The Task Panel item " + dataPool.get("MenuItem") + " still exists", driver);


		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			if(driver != null){
				try
				{
					driver.get(configURL);

					configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_TaskArea.Value);

					for(int count = 0; count < options.length; count++) 
						configurationPage.configurationPanel.setVaultCommands(options[count],"Show");

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}

			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest33_1_5

	/**
	 * 36.1.4 : Check the 'Prevent navigation outside of default view' option
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint36", "Breadcrumb"}, 
			description = "Check the 'Prevent navigation outside of default view' option.")
	public void SprintTest36_1_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		LoginPage loginPage = null;
		ConfigurationPage configurationPage = null;
		ConfigurationPanel configSettingsPanel = null;

		try {




			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to Configuration
			//--------------------------
			driver.get(configURL);

			loginPage = new LoginPage(driver);
			configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Click the Vault folder 
			//--------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);


			Log.message("2. Clicked the Vault folder.");

			//3. Select the layout
			//---------------------

			configSettingsPanel = new  ConfigurationPanel(driver);
			configSettingsPanel.setLayout(dataPool.get("Layout"));

			Log.message("3. Selected the layout.");

			//4. Check the Prevent Navigation check box
			//------------------------------------------
			configSettingsPanel.setPreventNavigation(true);


			Log.message("4. Checked the Prevent Navigation check box.");

			//5. Save the changes
			//--------------------
			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			Log.message("5. Saved the changes.");

			//Verification: To verify if the Prevent Navigation check box remains checked
			//----------------------------------------------------------------------------
			if(configSettingsPanel.getPreventNavigation())
				Log.pass("Test case Passed. The Prevent Navigation Check box remains checked after clicking the save button."); 
			else
				Log.fail("Test Case Failed. The Prevent Navigation Check box does not remain checked after clicking the save button.", driver);


		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {

			if(driver != null){
				try
				{
					configSettingsPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);
					configSettingsPanel.setPreventNavigation(false);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");

				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}

			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest36_1_4

	/**
	 * 36.1.7A : Check the view path displayed in the breadcrumb 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint36", "Breadcrumb"}, 
			description = "Check the view path displayed in the breadcrumb .")
	public void SprintTest36_1_7A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		LoginPage loginPage = null;
		ConfigurationPage configurationPage = null;
		ConfigurationPanel configSettingsPanel = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to Configuration
			//--------------------------
			driver.get(configURL);

			loginPage = new LoginPage(driver);
			configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Click the Vault folder 
			//--------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);


			Log.message("2. Clicked the Vault folder.");

			//4. Check the Prevent Navigation check box
			//------------------------------------------
			configSettingsPanel = new  ConfigurationPanel(driver);
			configSettingsPanel.setPreventNavigation(true);


			Log.message("4. Checked the Prevent Navigation check box.");

			//5. Save the changes
			//--------------------
			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			configurationPage.logOut();

			Log.message("5. Saved the changes.");

			//6. Login to the Test vault
			//---------------------------
			driver.get(loginURL);
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);

			Log.message("6. Login to the Test Vault.");

			//7. Navigate to the specified view
			//----------------------------------
			homePage.listView.navigateThroughView(dataPool.get("View"));


			Log.message("7. Navigate to the specified view");

			//Verification: To verify if the Breadcrumb shows the right path
			//---------------------------------------------------------------
			if(homePage.menuBar.GetBreadCrumbItem().equals(testVault+">"+dataPool.get("View").replace(">>", ">")))
				Log.pass("Test case Passed. The Prevent Navigation Check box remains checked after clicking the save button."); 
			else
				Log.fail("Test Case Failed. The Prevent Navigation Check box does not remain checked after clicking the save button.", driver);


		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			if(driver != null){
				try
				{
					driver.get(configURL);

					configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);

					configSettingsPanel.setPreventNavigation(false);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}

			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest36_1_7A

	/**
	 * 36.1.7B : Check the view path displayed in the breadcrumb 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint36", "Breadcrumb"}, 
			description = "Check the view path displayed in the breadcrumb .")
	public void SprintTest36_1_7B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		LoginPage loginPage = null;
		ConfigurationPage configurationPage = null;
		ConfigurationPanel configSettingsPanel = null;

		try {




			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to Configuration
			//--------------------------
			driver.get(configURL);

			loginPage = new LoginPage(driver);
			configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Click the Vault folder 
			//--------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);

			Log.message("2. Clicked the Vault folder.");

			//4. Check the Prevent Navigation check box
			//------------------------------------------
			configSettingsPanel = new  ConfigurationPanel(driver);
			configSettingsPanel.setPreventNavigation(true);


			Log.message("4. Checked the Prevent Navigation check box.");

			//5. Save the changes
			//--------------------
			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			configurationPage.logOut();

			Log.message("5. Saved the changes.");

			//6. Login to the Test vault
			//---------------------------
			driver.get(loginURL);
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);

			Log.message("6. Login to the Test Vault.");

			//7. Navigate to the specified view
			//----------------------------------
			homePage.listView.navigateThroughView(dataPool.get("View"));

			Log.message("7. Navigate to the specified view");

			//8. Click the Parent view in the breadcrumb
			//--------------------------------------------
			if(!homePage.menuBar.clickBreadcrumbItem(dataPool.get("ParentView")))
				throw new Exception("Unable to click the Parent View in the breadcrumb.");


			Log.message("8. Clicked the Parent view in the breadcrumb.");

			//Verification: To verify if navigated to the parent view
			//--------------------------------------------------------
			if(driver.getCurrentUrl().endsWith(dataPool.get("ViewID")))
				throw new Exception("Navigation to parent view through breadcrumb failed.");

			if(!homePage.menuBar.GetBreadCrumbItem().equals(testVault+">"+dataPool.get("ExpectedPath")))
				throw new Exception("The Expected Breadcrumb text was not displayed after navigating to the parent view.");

			if(homePage.listView.isItemExists(dataPool.get("ChildView")))
				Log.pass("Test case Passed. Navigation to parent view through breadcrumb was successful."); 
			else
				Log.fail("Test Case Failed. Navigation to parent view through breadcrumb was not successful.", driver);


		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {

			if(driver != null){
				try
				{
					driver.get(configURL);

					configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);

					configSettingsPanel.setPreventNavigation(false);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}

			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest36_1_7B

	/**
	 * 36.1.10A : Check the view path displayed in the breadcrumb 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint36", "Breadcrumb"}, 
			description = "Check the view path displayed in the breadcrumb .")
	public void SprintTest36_1_10A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		LoginPage loginPage = null;
		ConfigurationPage configurationPage = null;
		ConfigurationPanel configSettingsPanel = null;

		try {




			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to Configuration
			//--------------------------
			driver.get(configURL);

			loginPage = new LoginPage(driver);
			configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Click the Vault folder 
			//--------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);

			Log.message("2. Clicked the Vault folder.");

			//3. Set the view in configuration page
			//------------------------------------------
			configSettingsPanel = new  ConfigurationPanel(driver);
			configSettingsPanel.setDefaultView(dataPool.get("ViewID"));

			Log.message("3. The view was specified in the configuration page.");

			//4. Uncheck the Prevent Navigation check box
			//------------------------------------------
			configSettingsPanel = new  ConfigurationPanel(driver);
			configSettingsPanel.setPreventNavigation(false);


			Log.message("4. Unchecked the Prevent Navigation check box.");

			//5. Save the changes
			//--------------------
			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			configurationPage.logOut();

			Log.message("5. Saved the changes.");

			//6. Login to the Test vault
			//---------------------------
			driver.get(loginURL);
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);

			Log.message("7. Login to the Test Vault.");

			//Verification: To verify if the Breadcrumb shows the right path
			//---------------------------------------------------------------
			if(homePage.menuBar.GetBreadCrumbItem().equals(testVault+">"+dataPool.get("ExpectedPath")))
				Log.pass("Test case Passed. The breadcrumb showed the right path when logged in to a different default view."); 
			else
				Log.fail("Test Case Failed. The breadcrumb did not show the right path when logged in to a different default view.", driver);


		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			if(driver != null){
				try
				{					
					driver.get(configURL);

					configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);

					configSettingsPanel.setDefaultView(Caption.MenuItems.Home.Value);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest36_1_10A

	/**
	 * 36.1.10B : Check the view path displayed in the breadcrumb 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint36", "Breadcrumb"}, 
			description = "Check the view path displayed in the breadcrumb .")
	public void SprintTest36_1_10B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConfigurationPage configurationPage = null;
		ConfigurationPanel configSettingsPanel = null;

		try {




			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true);

			//1. Click the Vault folder 
			//--------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);

			Log.message("1. Clicked the Vault folder.");

			//2. Set the ViewID in Default View
			//------------------------------------------
			configurationPage.configurationPanel.setDefaultView(dataPool.get("ViewID"));

			Log.message("2. View ID was specified in the Defauly view.");

			//3. Uncheck the Prevent Navigation check box
			//------------------------------------------
			configSettingsPanel = new  ConfigurationPanel(driver);
			configSettingsPanel.setPreventNavigation(false);

			Log.message("3. Unchecked the Prevent Navigation check box.");

			//4. Save the changes
			//--------------------
			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			if (!configurationPage.logOut())
				throw new Exception("Logout is not successful.");

			Log.message("4. Saved the changes.");

			//5. Login to the Test vault
			//---------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false);

			Log.message("5. Login to the Test Vault.");

			//6. Navigate back to the parent vault though breadcrumb
			//-------------------------------------------------------
			homePage.menuBar.clickBreadcrumbItem(dataPool.get("ExpectedPath"));


			Log.message("6. Navigate back to the parent vault though breadcrumb.");

			//Verification: To verify if the Breadcrumb shows the right path
			//---------------------------------------------------------------
			if(driver.getCurrentUrl().endsWith(dataPool.get("ExpectedID")))
				throw new Exception("Navigation to parent view through breadcrumb failed.");

			if(!homePage.menuBar.GetBreadCrumbItem().equals(testVault+">"+dataPool.get("ExpectedPath")))
				throw new Exception("The Expected Breadcrumb text was not displayed after navigating to the parent view.");

			if(homePage.listView.isItemExists(dataPool.get("ChildItem")))
				Log.pass("Test case Passed. Navigation to Parent View through Breadcrumb was successful."); 
			else
				Log.fail("Test Case Failed. Navigation to parent view through breadcrumb failed.", driver);


		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			if(driver != null){
				try
				{
					driver.get(configURL);
					Utils.fluentWait(driver);
					configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);

					configurationPage.configurationPanel.setDefaultView(Caption.MenuItems.Home.Value);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}

			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest36_1_10B

	/**
	 * 36.1.11A : Check the view path displayed in the breadcrumb  
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint36", "Breadcrumb"}, 
			description = "Check the view path displayed in the breadcrumb.")
	public void SprintTest36_1_11A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		LoginPage loginPage = null;
		ConfigurationPage configurationPage = null;
		ConfigurationPanel configSettingsPanel = null;

		try {




			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to Configuration
			//--------------------------
			driver.get(configURL);

			loginPage = new LoginPage(driver);
			configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Click the Vault folder 
			//--------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);


			Log.message("2. Clicked the Vault folder.");

			//3. Check the Prevent Navigation check box
			//------------------------------------------
			configSettingsPanel = new  ConfigurationPanel(driver);
			configSettingsPanel.setLayout(dataPool.get("Layout"));

			Log.message("3. Layout is selected");

			//4. Save the changes
			//--------------------
			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			configurationPage.logOut();

			Log.message("4. Saved the changes.");

			//5. Login to the Test vault
			//---------------------------
			driver.get(loginURL);
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);

			Log.message("5. Login to the Test Vault.");

			//6. Navigate to the View
			//------------------------
			homePage.listView.navigateThroughView(dataPool.get("ExpectedPath").replace(">", ">>"));

			Log.message("6. Navigated to the View");

			//Verification: To verify if the Breadcrumb shows the right path
			//---------------------------------------------------------------
			if(homePage.menuBar.GetBreadCrumbItem().equals(testVault+">"+dataPool.get("ExpectedPath")) && driver.getCurrentUrl().endsWith(dataPool.get("ViewID")))
				Log.pass("Test case Passed. The breadcrumb showed the right path when the layout was set as " + dataPool.get("Layout")); 
			else
				Log.fail("Test Case Failed. The breadcrumb did not show the right path when the layout was set as " + dataPool.get("Layout"), driver);


		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			if(driver != null){
				try
				{
					driver.get(configURL);

					configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);

					configSettingsPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest36_1_11A

	/**
	 * 36.1.11B : Check the view path displayed in the breadcrumb 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint36", "Breadcrumb"}, 
			description = "Check the view path displayed in the breadcrumb .")
	public void SprintTest36_1_11B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		LoginPage loginPage = null;
		ConfigurationPage configurationPage = null;
		ConfigurationPanel configSettingsPanel = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to Configuration
			//--------------------------
			driver.get(configURL);

			loginPage = new LoginPage(driver);
			configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Click the Vault folder 
			//--------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);


			Log.message("2. Clicked the Vault folder.");

			//3. Check the Prevent Navigation check box
			//------------------------------------------
			configSettingsPanel = new  ConfigurationPanel(driver);
			configSettingsPanel.setLayout(dataPool.get("Layout"));

			Log.message("3. Layout is selected");

			//4. Save the changes
			//--------------------
			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			configurationPage.logOut();

			Log.message("4. Saved the changes.");

			//5. Login to the Test vault and navigate to a view
			//--------------------------------------------------
			driver.get(loginURL);
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);

			homePage.listView.navigateThroughView(dataPool.get("View"));


			Log.message("5. Logged in to the Test vault and navigated to a view.");

			//6. Navigate back to the parent vault though breadcrumb
			//-------------------------------------------------------
			homePage.menuBar.clickBreadcrumbItem(dataPool.get("ExpectedPath"));


			Log.message("6. Navigate back to the parent vault though breadcrumb.");

			//Verification: To verify if the Breadcrumb shows the right path
			//---------------------------------------------------------------
			if(driver.getCurrentUrl().endsWith(dataPool.get("ExpectedID")))
				throw new Exception("Navigation to parent view through breadcrumb failed.");

			if(!homePage.menuBar.GetBreadCrumbItem().equals(testVault+">"+dataPool.get("ExpectedPath")))
				throw new Exception("The Expected Breadcrumb text was not displayed after navigating to the parent view.");

			if(homePage.listView.isItemExists(dataPool.get("ChildItem")))
				Log.pass("Test case Passed. Navigation to Parent View through Breadcrumb was successful."); 
			else
				Log.fail("Test Case Failed. Navigation to parent view through breadcrumb failed.", driver);


		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			if(driver != null){
				try
				{
					driver.get(configURL);

					configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);

					configSettingsPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}

			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest36_1_11B

	/**
	 * 36.1.12A : Check the view path displayed in the breadcrumb ['Prevent navigation outside of the default view' - Checked]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint36", "Breadcrumb"}, 
			description = "Check the view path displayed in the breadcrumb ['Prevent navigation outside of the default view' - Checked].")
	public void SprintTest36_1_12A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		LoginPage loginPage = null;
		ConfigurationPage configurationPage = null;
		ConfigurationPanel configSettingsPanel = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to Configuration
			//--------------------------
			driver.get(configURL);

			loginPage = new LoginPage(driver);
			configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Click the Vault folder 
			//--------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);


			Log.message("2. Clicked the Vault folder.");

			//3. Select the Default View
			//------------------------------

			configSettingsPanel = new  ConfigurationPanel(driver);
			configSettingsPanel.setDefaultView(dataPool.get("View"));

			Log.message("3. Selected the Default View.");

			//4. Check the Prevent Navigation check box
			//------------------------------------------
			configSettingsPanel.setPreventNavigation(true);

			Log.message("4. Checked the Prevent Navigation check box.");

			//5. Save the changes
			//--------------------
			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			configurationPage.logOut();

			Log.message("5. Saved the changes.");

			//6. Login to the Vault
			//----------------------
			driver.get(loginURL);
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);

			Log.message("6. logged in to the vault.");

			//Verification: To verify if the Breadcrumb show the right path to the default view
			//----------------------------------------------------------------------------------
			if(homePage.menuBar.GetBreadCrumbItem().equals(testVault+">"+dataPool.get("View")))
				Log.pass("Test case Passed. The Bread crumb showed the correct path to the default view."); 
			else
				Log.fail("Test Case Failed. The Bread crumb showed '" + homePage.menuBar.GetBreadCrumbItem() + "' instead of '" + testVault+">"+dataPool.get("View") + "'", driver);


		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			if(driver != null){
				try
				{					
					driver.get(configURL);

					configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);

					configSettingsPanel.setDefaultView(Caption.MenuItems.Home.Value);
					configSettingsPanel.setPreventNavigation(false);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}

			}

			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest36_1_12A

	/**
	 * 36.1.12B : Check the view path displayed in the breadcrumb ['Prevent navigation outside of the default view' - Checked, Other View(ID)]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint36", "Breadcrumb"}, 
			description = "Check the view path displayed in the breadcrumb ['Prevent navigation outside of the default view' - Checked, Other View(ID)].")
	public void SprintTest36_1_12B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		LoginPage loginPage = null;
		ConfigurationPage configurationPage = null;
		ConfigurationPanel configSettingsPanel = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to Configuration
			//--------------------------
			driver.get(configURL);

			loginPage = new LoginPage(driver);
			configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Click the Vault folder 
			//--------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);

			Log.message("2. Clicked the Vault folder.");

			//3. Select the Default View
			//------------------------------
			configSettingsPanel = new  ConfigurationPanel(driver);
			configSettingsPanel.setDefaultView(dataPool.get("ViewID"));

			Log.message("3. Selected the Default View.");

			//4. Check the Prevent Navigation check box
			//------------------------------------------
			configSettingsPanel.setPreventNavigation(true);

			Log.message("4. Checked the Prevent Navigation check box.");

			//5. Save the changes
			//--------------------
			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			configurationPage.logOut();

			Log.message("5. Saved the changes.");

			//6. Login to the Vault
			//----------------------
			driver.get(loginURL);

			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);

			Log.message("6. logged in to the vault.");

			//Verification: To verify if the Breadcrumb show the right path to the default view
			//----------------------------------------------------------------------------------
			if(!driver.getCurrentUrl().endsWith(dataPool.get("ViewID")))
				throw new Exception("The Default view was not loaded upon logging into the vault.");

			if(homePage.menuBar.GetBreadCrumbItem().equals(testVault))
				Log.pass("Test case Passed. The Bread crumb showed the correct path to the default view."); 
			else
				Log.fail("Test Case Failed. The Bread crumb showed '" + homePage.menuBar.GetBreadCrumbItem() + "' instead of '" + testVault+">"+dataPool.get("View") + "'", driver);


		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			if(driver != null){
				try
				{
					driver.get(configURL);

					configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);

					configSettingsPanel.setDefaultView(Caption.MenuItems.Home.Value);
					configSettingsPanel.setPreventNavigation(false);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}

			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest36_1_12B

	/**
	 * 36.1.12C : Navigation to Parent View through breadcrumb ['Prevent navigation outside of the default view' - Checked]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint36", "Breadcrumb"}, 
			description = "Navigation to Parent View through breadcrumb ['Prevent navigation outside of the default view' - Checked].")
	public void SprintTest36_1_12C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		LoginPage loginPage = null;
		ConfigurationPage configurationPage = null;
		ConfigurationPanel configSettingsPanel = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to Configuration
			//--------------------------
			driver.get(configURL);

			loginPage = new LoginPage(driver);
			configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Click the Vault folder 
			//--------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);

			Log.message("2. Clicked the Vault folder.");

			//3. Select the Default View
			//------------------------------
			configSettingsPanel = new  ConfigurationPanel(driver);
			configSettingsPanel.setDefaultView(dataPool.get("View"));

			Log.message("3. Selected the Default View.");

			//4. Check the Prevent Navigation check box
			//------------------------------------------
			configSettingsPanel.setPreventNavigation(true);

			Log.message("4. Checked the Prevent Navigation check box.");

			//5. Save the changes
			//--------------------
			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			configurationPage.logOut();

			Log.message("5. Saved the changes.");

			//6. Login to the Vault
			//----------------------

			driver.get(loginURL);

			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);

			Log.message("6. logged in to the vault.");

			//7. Click on the parent view breadcrumb item
			//--------------------------------------------
			String expectedURL = driver.getCurrentUrl();
			homePage.menuBar.clickBreadcrumbItem(testVault);

			Log.message("7. Clicked on the parent view breadcrumb item");

			//Verification: To verify if the Breadcrumb show the right path to the default view
			//----------------------------------------------------------------------------------
			if(expectedURL.equals(driver.getCurrentUrl()))
				Log.pass("Test case Passed. Navigation to previous view was not possible as expected."); 
			else
				Log.fail("Test Case Failed. The URL showed '" + driver.getCurrentUrl() + "' instead of '" + expectedURL + "'", driver);


		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			if(driver != null){
				try
				{
					driver.get(configURL);

					configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);

					configSettingsPanel.setDefaultView(Caption.MenuItems.Home.Value);
					configSettingsPanel.setPreventNavigation(false);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest36_1_12C

	/**
	 * 36.1.12D : Navigation to Parent View through breadcrumb ['Prevent navigation outside of the default view' - Checked, Other View(ID)]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint36", "Breadcrumb"}, 
			description = "Navigation to Parent View through breadcrumb ['Prevent navigation outside of the default view' - Checked, Other View(ID)].")
	public void SprintTest36_1_12D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		LoginPage loginPage = null;
		ConfigurationPage configurationPage = null;
		ConfigurationPanel configSettingsPanel = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to Configuration
			//--------------------------
			driver.get(configURL);

			loginPage = new LoginPage(driver);
			configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Click the Vault folder 
			//--------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);

			Log.message("2. Clicked the Vault folder.");

			//3. Select the Default View
			//------------------------------
			configSettingsPanel = new  ConfigurationPanel(driver);
			configSettingsPanel.setDefaultView(dataPool.get("ViewID"));

			Log.message("3. Selected the Default View.");

			//4. Check the Prevent Navigation check box
			//------------------------------------------
			configSettingsPanel.setPreventNavigation(true);

			Log.message("4. Checked the Prevent Navigation check box.");

			//5. Save the changes
			//--------------------
			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			configurationPage.logOut();

			Log.message("5. Saved the changes.");

			//6. Login to the Vault
			//----------------------
			driver.get(loginURL);
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);

			Log.message("6. logged in to the vault.");

			//7. Click on the parent view breadcrumb item
			//--------------------------------------------
			String expectedURL = driver.getCurrentUrl();
			homePage.menuBar.clickBreadcrumbItem(testVault);

			Log.message("7. Clicked on the parent view breadcrumb item");

			//Verification: To verify if the Breadcrumb show the right path to the default view
			//----------------------------------------------------------------------------------
			if(expectedURL.equals(driver.getCurrentUrl()) && driver.getCurrentUrl().endsWith(dataPool.get("ViewID")))
				Log.pass("Test case Passed. Navigation to previous view was not possible as expected."); 
			else
				Log.fail("Test Case Failed. The URL showed '" + driver.getCurrentUrl() + "' instead of '" + expectedURL + "'", driver);


		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			if(driver != null){
				try
				{
					driver.get(configURL);

					configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);

					configSettingsPanel.setDefaultView(Caption.MenuItems.Home.Value);
					configSettingsPanel.setPreventNavigation(false);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest36_1_12D

	/**
	 * 36.1.12E : Navigation to Parent View through TaskPanel ['Prevent navigation outside of the default view' - Checked]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint36", "Breadcrumb"}, 
			description = "Navigation to Parent View through TaskPanel ['Prevent navigation outside of the default view' - Checked].")
	public void SprintTest36_1_12E(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		LoginPage loginPage = null;
		ConfigurationPage configurationPage = null;
		ConfigurationPanel configSettingsPanel = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to Configuration
			//--------------------------
			driver.get(configURL);

			loginPage = new LoginPage(driver);
			configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Click the Vault folder 
			//--------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);

			Log.message("2. Clicked the Vault folder.");

			//3. Select the Default View
			//------------------------------
			configSettingsPanel = new  ConfigurationPanel(driver);
			configSettingsPanel.setDefaultView(dataPool.get("View"));

			Log.message("3. Selected the Default View.");

			//4. Check the Prevent Navigation check box
			//------------------------------------------
			configSettingsPanel.setPreventNavigation(true);

			Log.message("4. Checked the Prevent Navigation check box.");

			//5. Save the changes
			//--------------------
			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			configurationPage.logOut();

			Log.message("5. Saved the changes.");

			//6. Login to the Vault
			//----------------------
			driver.get(loginURL);
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);

			Log.message("6. logged in to the vault.");

			//7. Navigate to Home View using the Task Panel
			//--------------------------------------------
			String expectedURL = driver.getCurrentUrl();
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);

			Log.message("7. Navigated to Home View using the Task Panel");

			//Verification: To verify if the Breadcrumb show the right path to the default view
			//----------------------------------------------------------------------------------
			if(expectedURL.equals(driver.getCurrentUrl()))
				Log.pass("Test case Passed. Navigation to previous view was not possible as expected."); 
			else
				Log.fail("Test Case Failed. The URL showed '" + driver.getCurrentUrl() + "' instead of '" + expectedURL + "'", driver);


		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			if(driver != null){
				try
				{

					driver.get(configURL);

					configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);

					configSettingsPanel.setDefaultView(Caption.MenuItems.Home.Value);
					configSettingsPanel.setPreventNavigation(false);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}

			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest36_1_12E

	/**
	 * 36.1.12F : Navigation to Parent View through TaskPanel ['Prevent navigation outside of the default view' - Checked, Other View(ID)]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint36", "Breadcrumb"}, 
			description = "Navigation to Parent View through TaskPanel ['Prevent navigation outside of the default view' - Checked, Other View(ID)].")
	public void SprintTest36_1_12F(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		LoginPage loginPage = null;
		ConfigurationPage configurationPage = null;
		ConfigurationPanel configSettingsPanel = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to Configuration
			//--------------------------
			driver.get(configURL);

			loginPage = new LoginPage(driver);
			configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Click the Vault folder 
			//--------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);


			Log.message("2. Clicked the Vault folder.");

			//3. Select the Default View
			//------------------------------

			configSettingsPanel = new  ConfigurationPanel(driver);
			configSettingsPanel.setDefaultView(dataPool.get("ViewID"));

			Log.message("3. Selected the Default View.");

			//4. Check the Prevent Navigation check box
			//------------------------------------------
			configSettingsPanel.setPreventNavigation(true);

			Log.message("4. Checked the Prevent Navigation check box.");

			//5. Save the changes
			//--------------------
			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			configurationPage.logOut();

			Log.message("5. Saved the changes.");

			//6. Login to the Vault
			//----------------------
			driver.get(loginURL);
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);

			Log.message("6. logged in to the vault.");

			//7. Navigate to Home View using the Task Panel
			//--------------------------------------------
			String expectedURL = driver.getCurrentUrl();
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);

			Log.message("7. Navigated to Home View using the Task Panel");

			//Verification: To verify if the Breadcrumb show the right path to the default view
			//----------------------------------------------------------------------------------
			if(expectedURL.equals(driver.getCurrentUrl()) && driver.getCurrentUrl().endsWith(dataPool.get("ViewID")))
				Log.pass("Test case Passed. Navigation to previous view was not possible as expected."); 
			else
				Log.fail("Test Case Failed. The URL showed '" + driver.getCurrentUrl() + "' instead of '" + expectedURL + "'", driver);


		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			if(driver != null){
				try
				{
					driver.get(configURL);

					configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);

					configSettingsPanel.setDefaultView(Caption.MenuItems.Home.Value);
					configSettingsPanel.setPreventNavigation(false);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest36_1_12F

	/**
	 * 36.1.12G : Navigation to Parent View through breadcrumb  After Navigating to any other view['Prevent navigation outside of the default view' - Checked]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint36", "Breadcrumb"}, 
			description = "Navigation to Parent View through breadcrumb  After Navigating to any other view['Prevent navigation outside of the default view' - Checked].")
	public void SprintTest36_1_12G(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		LoginPage loginPage = null;
		ConfigurationPage configurationPage = null;
		ConfigurationPanel configSettingsPanel = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to Configuration
			//--------------------------
			driver.get(configURL);

			loginPage = new LoginPage(driver);
			configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Click the Vault folder 
			//--------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);


			Log.message("2. Clicked the Vault folder.");

			//3. Select the Default View
			//------------------------------
			configSettingsPanel = new  ConfigurationPanel(driver);
			configSettingsPanel.setDefaultView(dataPool.get("View"));

			Log.message("3. Selected the Default View.");

			//4. Check the Prevent Navigation check box
			//------------------------------------------
			configSettingsPanel.setPreventNavigation(true);

			Log.message("4. Checked the Prevent Navigation check box.");

			//5. Save the changes
			//--------------------
			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			configurationPage.logOut();

			Log.message("5. Saved the changes.");

			//6. Login to the Vault
			//----------------------
			driver.get(loginURL);
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);

			Log.message("6. logged in to the vault.");

			//7. Navigate to any other view
			//------------------------------
			String expectedURL = driver.getCurrentUrl();
			homePage.taskPanel.clickItem(dataPool.get("NewView"));

			Log.message("7. Navigated to any other view.");

			//8. Click on the Home view breadcrumb item
			//--------------------------------------------
			if (!homePage.menuBar.clickBreadcrumbItem(testVault))
				throw new Exception(testVault + " is not clicked in the breadcrumb");

			Log.message("8. Clicked on the Home view breadcrumb item");

			//Verification: To verify if the Breadcrumb show the right path to the default view
			//----------------------------------------------------------------------------------
			if(expectedURL.equals(driver.getCurrentUrl()))
				Log.pass("Test case Passed. Navigation to previous view was not possible as expected."); 
			else
				Log.fail("Test Case Failed. The URL showed '" + driver.getCurrentUrl() + "' instead of '" + expectedURL + "'", driver);


		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			if(driver != null){
				try
				{
					driver.get(configURL);

					configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);

					configSettingsPanel.setDefaultView(Caption.MenuItems.Home.Value);
					configSettingsPanel.setPreventNavigation(false);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest36_1_12G

	/**
	 * 36.1.12H : Navigation to Parent View through TaskPanel After Navigating to any other view ['Prevent navigation outside of the default view' - Checked]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint36", "Breadcrumb"}, 
			description = "Navigation to Parent View through TaskPanel After Navigating to any other view ['Prevent navigation outside of the default view' - Checked].")
	public void SprintTest36_1_12H(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		LoginPage loginPage = null;
		ConfigurationPage configurationPage = null;
		ConfigurationPanel configSettingsPanel = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to Configuration
			//--------------------------
			driver.get(configURL);

			loginPage = new LoginPage(driver);
			configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Click the Vault folder 
			//--------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);


			Log.message("2. Clicked the Vault folder.");

			//3. Select the Default View
			//------------------------------
			configSettingsPanel = new  ConfigurationPanel(driver);
			configSettingsPanel.setDefaultView(dataPool.get("View"));

			Log.message("3. Selected the Default View.");

			//4. Check the Prevent Navigation check box
			//------------------------------------------
			configSettingsPanel.setPreventNavigation(true);

			Log.message("4. Checked the Prevent Navigation check box.");

			//5. Save the changes
			//--------------------
			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			configurationPage.logOut();

			Log.message("5. Saved the changes.");

			//6. Login to the Vault
			//----------------------
			driver.get(loginURL);
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);

			Log.message("6. logged in to the vault.");

			//7. Navigate to any other view
			//------------------------------
			String expectedURL = driver.getCurrentUrl();
			homePage.taskPanel.clickItem(dataPool.get("NewView"));

			Log.message("7. Navigated to any other view.");

			//8. Click on the Home link in Task Panel
			//--------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);

			Log.message("7. Clicked on the Home link in Task Panel.");

			//Verification: To verify if the Breadcrumb show the right path to the default view
			//----------------------------------------------------------------------------------
			if(expectedURL.equals(driver.getCurrentUrl()))
				Log.pass("Test case Passed. Navigation to previous view was not possible as expected."); 
			else
				Log.fail("Test Case Failed. The URL showed '" + driver.getCurrentUrl() + "' instead of '" + expectedURL + "'", driver);


		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			if(driver != null){
				try
				{
					driver.get(configURL);

					configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);

					configSettingsPanel.setDefaultView(Caption.MenuItems.Home.Value);
					configSettingsPanel.setPreventNavigation(false);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest36_1_12H

	/**
	 * 36.1.18: Check the view path displayed in the breadcrumb [Listing Pane and Properties Pane only, Listing Pane Only]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint36", "Breadcrumb"}, 
			description = "Check the view path displayed in the breadcrumb [Listing Pane and Properties Pane only, Listing Pane Only].")
	public void SprintTest36_1_18(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		LoginPage loginPage = null;
		ConfigurationPage configurationPage = null;
		ConfigurationPanel configSettingsPanel = null;

		try {




			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to Configuration
			//--------------------------
			driver.get(configURL);

			loginPage = new LoginPage(driver);
			configurationPage = loginPage.loginToConfigurationUI(userName, password);

			Log.message("1. Logged in to Configuiration");

			//2. Click the Vault folder 
			//--------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);


			Log.message("2. Clicked the Vault folder.");

			//3. Select the Default View
			//------------------------------
			configSettingsPanel = new  ConfigurationPanel(driver);
			configSettingsPanel.setLayout(dataPool.get("Layout"));

			Log.message("3. Selected the Default View.");

			//4. Save the changes
			//--------------------
			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			configurationPage.logOut();

			Log.message("4. Saved the changes.");

			//5. Login to the Vault
			//----------------------
			driver.get(loginURL);
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);

			Log.message("5. logged in to the vault.");

			//6. Navigate to any view
			//------------------------
			homePage.listView.navigateThroughView(dataPool.get("View"));


			Log.message("6. Navigated to any view.");

			//Verification: The Breadcrumb should not be displayed
			//-----------------------------------------------------
			if(!homePage.isBreadcrumbDisplayed())
				Log.pass("Test case Passed. The Bread crumb was not displayed when the layout was set as '" + dataPool.get("Layout")+"'"); 
			else
				Log.fail("Test Case Failed. The Bread crumb was displayed when the layout was set as '" + dataPool.get("Layout")+"'", driver);


		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			if(driver != null){
				try
				{
					driver.get(configURL);

					configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);

					configSettingsPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");

				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest36_1_12H

	/**
	 * 41.8.1A : Enabling Automatic Login checkbox should enable user name, password, domain and document vault fields
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Sprint41"}, description = "Enabling Automatic Login checkbox should enable user name, password, domain and document vault fields based on the default authentication type")
	public void SprintTest41_8_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {




			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to General in tree view
			//-----------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.GeneralSettings.Value + ">>" + Caption.ConfigSettings.General.Value);


			if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase("GENERAL - GENERAL SETTINGS"))
				throw new Exception("General settings page in configuration is not opened.");

			Log.message("1. Navigated to General page.");

			//Step-2 : Enable Default authentication type 
			//-------------------------------------------

			configurationPage.configurationPanel.setDefaultAuthType(dataPool.get("AuthenticationType")); //Enable automatic login checkbox

			if (!configurationPage.configurationPanel.getDefaultAuthType().equals(dataPool.get("AuthenticationType")))
				throw new Exception("Default authentication type (" + dataPool.get("AuthenticationType") + ") check box is not enabled.");

			Log.message("2. Default authentication type (" + dataPool.get("AuthenticationType") + ") check box is enabled.");

			//Step-3 : Enable Automatic Login check box
			//-----------------------------------------
			configurationPage.configurationPanel.setAutoLogin(true); //Enable automatic login checkbox

			if (!configurationPage.configurationPanel.getAutoLogin())
				throw new Exception("Automatic login check box is not enabled.");

			Log.message("3. Automatic login check box is enabled.");

			//Verification : Verify that User name, password, domain and vault fields are in enabled state based on the authentication type
			//-----------------------------------------------------------------------------------------------------------------------------
			String addlInfo = "";
			String passInfo = "";

			if (!configurationPage.configurationPanel.isAutoLoginUserNameEnabled()) //Checks if User name enabled
				addlInfo = "Auto Login User Name is not enabled;";
			else
				passInfo = "User name,";

			if (!configurationPage.configurationPanel.isAutoLoginPasswordEnabled()) //Checks if User name enabled
				addlInfo += "Auto Login Password is not enabled;";
			else
				passInfo += " Password,";

			if (configurationPage.configurationPanel.getDefaultAuthType().equalsIgnoreCase("Windows user"))
				if (!configurationPage.configurationPanel.isAutoLoginDomainEnabled()) //Checks if User name enabled
					addlInfo += "Auto Login Domain is not enabled;";
				else
					passInfo += " Domain,";

			if (!configurationPage.configurationPanel.isAutoLoginVaultEnabled()) //Checks if User name enabled
				addlInfo += "Auto Login Vault is not enabled;";
			else
				passInfo += " Select vault";

			if (addlInfo.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Enabling auto login enabled (" + passInfo + ") for the default authentication type (" + dataPool.get("AuthenticationType") + ").");
			else
				Log.fail("Test case Failed. Enabling auto login is not as expected. Additional information : " 
						+ addlInfo, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest41_8_1A

	/**
	 * 41.8.1B : Disabling Automatic Login checkbox should disable user name, password, domain and document vault fields
	 */
	@Test(groups = {"Sprint41"}, description = "Disabling Automatic Login checkbox should disable user name, password, domain and document vault fields")
	public void SprintTest41_8_1B() throws Exception {

		driver = null; 

		try {




			driver = WebDriverUtils.getDriver();

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to General in tree view
			//-----------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.GeneralSettings.Value + ">>" + Caption.ConfigSettings.General.Value);


			if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase("GENERAL - GENERAL SETTINGS"))
				throw new Exception("General settings page in configuration is not opened.");

			Log.message("1. Navigated to General page.");

			//Step-2 : Enable Automatic Login check box
			//-----------------------------------------
			configurationPage.configurationPanel.setAutoLogin(false); //Disable automatic login checkbox

			if (configurationPage.configurationPanel.getAutoLogin())
				throw new Exception("Automatic login check box is not disabled.");

			Log.message("2. Automatic login check box is disabled.");

			//Verification : Verify that User name, password, domain and vault fields are in enabled state
			//--------------------------------------------------------------------------------------------
			String addlInfo = "";

			if (configurationPage.configurationPanel.isAutoLoginUserNameEnabled()) //Checks if User name enabled
				addlInfo = "Auto Login User Name is enabled;";

			if (configurationPage.configurationPanel.isAutoLoginPasswordEnabled()) //Checks if User name enabled
				addlInfo = "Auto Login Password is enabled;";

			if (configurationPage.configurationPanel.isAutoLoginDomainEnabled()) //Checks if User name enabled
				addlInfo = "Auto Login Domain is enabled;";

			if (configurationPage.configurationPanel.isAutoLoginVaultEnabled()) //Checks if User name enabled
				addlInfo = "Auto Login Vault is enabled;";

			if (addlInfo.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Disabling auto login disabled User name, Password, Domain and Select vault.");
			else
				Log.fail("Test case Failed. Disabling auto login is not as expected. Additional information : " 
						+ addlInfo, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest41_8_1B

	/**
	 * 41.8.2A : Windows user radio button should be disabled on selecting Force M-Files user login
	 */
	@Test(groups = {"Sprint41"}, description = "Windows user radio button should be disabled on selecting Force M-Files user login")
	public void SprintTest41_8_2A() throws Exception {

		driver = null; 

		try {




			driver = WebDriverUtils.getDriver();

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to General in tree view
			//-----------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.GeneralSettings.Value + ">>" + Caption.ConfigSettings.General.Value);


			if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase("GENERAL - GENERAL SETTINGS"))
				throw new Exception("General settings page in configuration is not opened.");

			Log.message("1. Navigated to General page.");

			//Step-2 : Select 'Force M-Files user login'
			//-------------------------------------------
			configurationPage.configurationPanel.setForceMFilesUserLogin(true); //Enable Force MFiles user login

			if (!configurationPage.configurationPanel.getForceMFilesUserLogin())
				throw new Exception("Force M-Files user login is not enabled.");

			Log.message("2. Force M-Files user is enabled.");

			//Verification : Verify that User name, password, domain and vault fields are in enabled state
			//--------------------------------------------------------------------------------------------
			if (!configurationPage.configurationPanel.isDefaultAuthTypeEnabled("WINDOWS USER")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Enabling Force M-Files user login disabled windows user.");
			else
				Log.fail("Test case Failed. Windows User is in enabled state after enabling Force M-Files user login.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest41_8_2A

	/**
	 * 41.8.2B : Windows user radio button should be enabled on de-selecting Force M-Files user login
	 */
	@Test(groups = {"Sprint41"}, description = "Windows user radio button should be enabled on de-selecting Force M-Files user login")
	public void SprintTest41_8_2B() throws Exception {

		driver = null; 

		try {




			driver = WebDriverUtils.getDriver();

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to General in tree view
			//-----------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.GeneralSettings.Value + ">>" + Caption.ConfigSettings.General.Value);

			if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase("GENERAL - GENERAL SETTINGS"))
				throw new Exception("General settings page in configuration is not opened.");

			Log.message("1. Navigated to General page.");

			//Step-2 : De-select 'Force M-Files user login'
			//-------------------------------------------
			configurationPage.configurationPanel.setForceMFilesUserLogin(false); //Enable Force MFiles user login

			if (configurationPage.configurationPanel.getForceMFilesUserLogin())
				throw new Exception("Force M-Files user login is enabled.");

			Log.message("2. Force M-Files user is disabled.");

			//Verification : Verify that User name, password, domain and vault fields are in enabled state
			//--------------------------------------------------------------------------------------------
			if (configurationPage.configurationPanel.isDefaultAuthTypeEnabled("WINDOWS USER")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Disabling Force M-Files user login enabled windows user.");
			else
				Log.fail("Test case Failed. Windows User is in disabled state after disabling Force M-Files user login.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest41_8_2B

	/**
	 * 41.8.3 : Prevent Navigation outside default view should be in enabled state on selecting default layout
	 */
	@Test(groups = {"Sprint41"}, description = "Prevent Navigation outside default view should be in enabled state on selecting default layout")
	public void SprintTest41_8_3() throws Exception {

		driver = null; 

		try {




			driver = WebDriverUtils.getDriver();

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to Vault in tree view
			//-----------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault);


			if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase()))
				throw new Exception("Vault settings page in configuration is not opened.");

			Log.message("1. Navigated to Vault settings page.");

			//Step-2 : Select 'Default' layout
			//--------------------------------
			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value); //Enable Force MFiles user login

			if (!configurationPage.configurationPanel.getLayout().equalsIgnoreCase(Caption.ConfigSettings.Config_Default.Value))
				throw new Exception("Default Layout is not selected.");

			Log.message("2. Default layout is selected.");

			//Verification : Verify if Prevent Navigation outside default view should be in enabled state.
			//--------------------------------------------------------------------------------------------
			if (configurationPage.configurationPanel.isPreventNavigationEnabled()) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Prevent Navigation outside default view is in enabled state.");
			else
				Log.fail("Test case Failed. Prevent Navigation outside default view is not in enabled state.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest41_8_3

	/**
	 * 41.8.4A : 'Default search criteria' dropdown list should be disabled on selecting the 'Retain latest selection done by user'
	 */
	@Test(groups = {"Sprint41"}, description = "'Default search criteria' dropdown list should be disabled on selecting the 'Retain latest selection done by user'")
	public void SprintTest41_8_4A() throws Exception {

		driver = null; 

		try {




			driver = WebDriverUtils.getDriver();

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to Vault in tree view
			//-----------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault);


			if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase()))
				throw new Exception("Vault settings page in configuration is not opened.");

			Log.message("1. Navigated to Vault settings page.");

			//Step-2 : Enable 'Retain latest selection made by user' checkbox
			//----------------------------------------------------------------
			configurationPage.configurationPanel.setRetainLatestSearchCriteria(true); //Enable Retain latest search criteria

			if (!configurationPage.configurationPanel.getRetainLatestSearchCriteria())
				throw new Exception("Retain latest selection made by user is not selected.");

			Log.message("2. Retain latest selection made by user is enabled.");

			//Verification : Verify if Default search criteria dropdown list is disabled
			//---------------------------------------------------------------------------
			if (!configurationPage.configurationPanel.isDefaultSearchCriteriaEnabled()) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Default Search Criteria is disabled on selecting Retain latest selection made by user checkbox.");
			else
				Log.fail("Test case Failed. Default Search Criteria is in enabled state on selecting Retain latest selection made by user checkbox.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest41_8_4A

	/**
	 * 41.8.4B : 'Default search criteria' dropdown list should be enabled on un-selecting the 'Retain latest selection done by user'
	 */
	@Test(groups = {"Sprint41"}, description = "'Default search criteria' dropdown list should be enabled on un-selecting the 'Retain latest selection done by user'")
	public void SprintTest41_8_4B() throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to Vault in tree view
			//-----------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault);


			if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase()))
				throw new Exception("Vault settings page in configuration is not opened.");

			Log.message("1. Navigated to Vault settings page.");

			//Step-2 : Disable 'Retain latest selection made by user' checkbox
			//----------------------------------------------------------------
			configurationPage.configurationPanel.setRetainLatestSearchCriteria(false); //Disable Retain latest search criteria

			if (configurationPage.configurationPanel.getRetainLatestSearchCriteria())
				throw new Exception("Retain latest selection made by user is not disabled.");

			Log.message("2. Retain latest selection made by user is disabled.");

			//Verification : Verify if Default search criteria dropdown list is disabled
			//---------------------------------------------------------------------------
			if (configurationPage.configurationPanel.isDefaultSearchCriteriaEnabled()) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Default Search Criteria is enabled on un-selecting Retain latest selection made by user checkbox.");
			else
				Log.fail("Test case Failed. Default Search Criteria is in disabled state on un-selecting Retain latest selection made by user checkbox.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest41_8_4B

	/**
	 * 54.1.20.1 : Advanced search bar should be opened in Default layout.
	 */
	@Test(groups = {"Sprint54"}, description = "Advanced search bar should be opened in Default layout.")
	public void SprintTest54_1_20_1() throws Exception {

		driver = null; 

		try {			



			driver = WebDriverUtils.getDriver();

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to Vault in tree view
			//-----------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item


			if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase())) //Checks if navigated to vault settings page
				throw new Exception("Vault settings page in configuration is not opened.");

			Log.message("1. Navigated to Vault settings page.");

			//Step-2 : Select Default layout and save the settings
			//----------------------------------------------------
			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value); //Sets as default layout

			if (!configurationPage.configurationPanel.getLayout().equalsIgnoreCase(Caption.ConfigSettings.Config_Default.Value)) //Verifies if default layout is selected
				throw new Exception("Default layout is not selected.");

			if (!configurationPage.configurationPanel.saveSettings()) //Saves the settings
				throw new Exception("Settings are not saved properly.");

			if (!configurationPage.logOut()) //Logs out from configuration page
				throw new Exception("Log out is not successful after saving the settings in configuration page.");

			Log.message("2. Default layout is selected and settings are saved.");

			//Step-3 : Login to the default page and click Advanced Search in search pane
			//--------------------------------------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches and logs into the default page
			homePage.searchPanel.clickAdvancedSearch(true); //Clicks Advanced search button

			Log.message("3. Logged into the default page and advanced search button is clicked.");

			//Verification : Verify if advanced search is displayed
			//-----------------------------------------------------
			if (homePage.searchPanel.isAdvancedSearchDisplayed()) //Verifies if Advanced search is displayed in default layout
				Log.pass("Test case Passed. Advanced search is displayed in default layout.");
			else
				Log.fail("Test case Failed. Advanced search is not displayed in default layout.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_1_20_1

	/**
	 * 54.1.20.2 : Advanced search bar should be opened in Default and Navigation pane layout.
	 */
	@Test(groups = {"Sprint54"}, description = "Advanced search bar should be opened in Default and Navigation pane layout.")
	public void SprintTest54_1_20_2() throws Exception {

		driver = null; 

		try {




			driver = WebDriverUtils.getDriver();

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to Vault in tree view
			//-----------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item


			if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase())) //Checks if navigated to vault settings page
				throw new Exception("Vault settings page in configuration is not opened.");

			Log.message("1. Navigated to Vault settings page.");

			//Step-2 : Select Default and navigation pane layout and save the settings
			//-------------------------------------------------------------------------
			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_DefaultAndNavigation.Value); //Sets as Default and Navigation layout

			if (!configurationPage.configurationPanel.getLayout().equalsIgnoreCase(Caption.ConfigSettings.Config_DefaultAndNavigation.Value)) //Verifies if default layout is selected
				throw new Exception("Default layout is not selected.");

			if (!configurationPage.configurationPanel.saveSettings()) //Saves the settings
				throw new Exception("Settings are not saved properly.");

			if (!configurationPage.logOut()) //Logs out from configuration page
				throw new Exception("Log out is not successful after saving the settings in configuration page.");

			Log.message("2. " + Caption.ConfigSettings.Config_DefaultAndNavigation.Value + " layout is selected and settings are saved.");

			//Step-3 : Login to the default page and click Advanced Search in search pane
			//--------------------------------------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches and logs into the default page
			homePage.searchPanel.clickAdvancedSearch(true); //Clicks Advanced search button

			Log.message("3. Logged into the default page and advanced search button is clicked.");

			//Verification : Verify if advanced search is displayed
			//-----------------------------------------------------
			if (homePage.searchPanel.isAdvancedSearchDisplayed()) //Verifies if Advanced search is displayed in default layout
				Log.pass("Test case Passed. Advanced search is displayed in " +  Caption.ConfigSettings.Config_DefaultAndNavigation.Value + " layout.");
			else
				Log.fail("Test case Failed. Advanced search is not displayed in " +  Caption.ConfigSettings.Config_DefaultAndNavigation.Value + " layout.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			if(driver != null){

				try
				{
					driver.get(configURL);

					ConfigurationPage configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item

					configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}

			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_1_20_2

	/**
	 * 54.1.20.3 : Advanced search bar should be opened in No Java Applet layout.
	 */
	@Test(groups = {"Sprint54"}, description = "Advanced search bar should be opened in No Java Applet layout.")
	public void SprintTest54_1_20_3() throws Exception {

		driver = null; 

		try {




			driver = WebDriverUtils.getDriver();

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to Vault in tree view
			//-----------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item


			if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase())) //Checks if navigated to vault settings page
				throw new Exception("Vault settings page in configuration is not opened.");

			Log.message("1. Navigated to Vault settings page.");

			//Step-2 : Select No Java Applet layout and save the settings
			//-------------------------------------------------------------------------
			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);//Disabled the java applet in configuration page

			if(configurationPage.configurationPanel.isJavaAppletEnabled())//Verify if java applet is disabled or not
				throw new Exception("Java applet is enabled.");

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings in configuration page
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. Java applet is disabled and Configuration settings are saved.");

			if (!configurationPage.logOut()) //Logs out from configuration page
				throw new Exception("Log out is not successful after saving the settings in configuration page.");

			Log.message("2. No Java applet layout is selected and settings are saved.");

			//Step-3 : Login to the default page and click Advanced Search in search pane
			//--------------------------------------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches and logs into the default page
			homePage.searchPanel.clickAdvancedSearch(true); //Clicks Advanced search button

			Log.message("3. Logged into the default page and advanced search button is clicked.");

			//Verification : Verify if advanced search is displayed
			//-----------------------------------------------------
			if (homePage.searchPanel.isAdvancedSearchDisplayed()) //Verifies if Advanced search is displayed in default layout
				Log.pass("Test case Passed. Advanced search is displayed in No java applet layout.");
			else
				Log.fail("Test case Failed. Advanced search is not displayed in No java applet layout.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			if(driver != null){
				try
				{
					driver.get(configURL);

					ConfigurationPage configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item

					configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}

			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_1_20_3

	/**
	 * 54.1.20.4 : Advanced search bar should be opened in No Java applet, no task area layout.
	 */
	@Test(groups = {"Sprint54"}, description = "Advanced search bar should be opened in No Java applet, no task area layout.")
	public void SprintTest54_1_20_4() throws Exception {

		driver = null; 

		try {




			driver = WebDriverUtils.getDriver();

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to Vault in tree view
			//-----------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item


			if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase())) //Checks if navigated to vault settings page
				throw new Exception("Vault settings page in configuration is not opened.");

			Log.message("1. Navigated to Vault settings page.");

			//Step-2 : Select No Java applet, no task area layout and save the settings
			//-------------------------------------------------------------------------
			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);

			if(configurationPage.configurationPanel.isJavaAppletEnabled())
				throw new Exception("Java applet is enabled");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_NoTaskArea.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. No java applet and no task area layout is selected and saved.");


			if (!configurationPage.logOut()) //Logs out from configuration page
				throw new Exception("Log out is not successful after saving the settings in configuration page.");

			Log.message("2. Log out from the configuration page.");

			//Step-3 : Login to the default page and click Advanced Search in search pane
			//--------------------------------------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches and logs into the default page
			homePage.searchPanel.clickAdvancedSearch(true); //Clicks Advanced search button

			Log.message("3. Logged into the default page and advanced search button is clicked.");

			//Verification : Verify if advanced search is displayed
			//-----------------------------------------------------
			if (homePage.searchPanel.isAdvancedSearchDisplayed()) //Verifies if Advanced search is displayed in default layout
				Log.pass("Test case Passed. Advanced search is displayed in No java applet and No task area layout.");
			else
				Log.fail("Test case Failed. Advanced search is not displayed in No java applet and No task area layout.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			if(driver != null){
				try
				{
					driver.get(configURL);

					ConfigurationPage configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item

					configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}

			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_1_20_4

	/**
	 * 54.1.20.5 : Advanced search bar should be opened in No Java applet, no task area, but show Go To shortcuts layout.
	 */
	@Test(groups = {"Sprint54"}, description = "Advanced search bar should be opened in No Java applet, no task area, but show Go To shortcuts layout.")
	public void SprintTest54_1_20_5() throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();


			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to Vault in tree view
			//-----------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item


			if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase())) //Checks if navigated to vault settings page
				throw new Exception("Vault settings page in configuration is not opened.");

			Log.message("1. Navigated to Vault settings page.");

			//Step-2 : Select No Java applet, no task area layout and save the settings
			//-------------------------------------------------------------------------
			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);

			if(configurationPage.configurationPanel.isJavaAppletEnabled())
				throw new Exception("Java applet is enabled");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_TaskAreaWithShowGoTo.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. No java applet with task area with go to layout is selected and saved.");

			if (!configurationPage.logOut()) //Logs out from configuration page
				throw new Exception("Log out is not successful after saving the settings in configuration page.");

			Log.message("2. No java applet with task area with go to layout is selected and settings are saved.");

			//Step-3 : Login to the default page and click Advanced Search in search pane
			//--------------------------------------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches and logs into the default page
			homePage.searchPanel.clickAdvancedSearch(true); //Clicks Advanced search button

			Log.message("3. Logged into the default page and advanced search button is clicked.");

			//Verification : Verify if advanced search is displayed
			//-----------------------------------------------------
			if (homePage.searchPanel.isAdvancedSearchDisplayed()) //Verifies if Advanced search is displayed in No Java applet, no task area, but show Go To shortcuts layout
				Log.pass("Test case Passed. Advanced search is displayed in No java applet with Task area only layout.");
			else
				Log.fail("Test case Failed. Advanced search is not displayed in No java applet with Task area only layout.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			if(driver != null){
				try
				{
					driver.get(configURL);

					ConfigurationPage configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item

					configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}

			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_1_20_5

	/**
	 * 54.1.20.6 : Search bar should not be availble in Listing pane and properties pane layout.
	 */
	@Test(groups = {"Sprint54"}, description = "Search bar should not be availble in Listing pane and properties pane layout.")
	public void SprintTest54_1_20_6() throws Exception {

		driver = null; 

		try {




			driver = WebDriverUtils.getDriver();



			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to Vault in tree view
			//-----------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item

			if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase())) //Checks if navigated to vault settings page
				throw new Exception("Vault settings page in configuration is not opened.");

			Log.message("1. Navigated to Vault settings page.");

			//Step-2 : Select No Java applet, no task area layout and save the settings
			//-------------------------------------------------------------------------
			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_ListingPropertiesPaneOnly.Value); //Sets as Listing pane and properties pane layout

			if (!configurationPage.configurationPanel.getLayout().equalsIgnoreCase(Caption.ConfigSettings.Config_ListingPropertiesPaneOnly.Value)) //Verifies if Listing pane and properties pane layout is selected
				throw new Exception("Default layout is not selected.");

			if (!configurationPage.configurationPanel.saveSettings()) //Saves the settings
				throw new Exception("Settings are not saved properly.");

			if (!configurationPage.logOut()) //Logs out from configuration page
				throw new Exception("Log out is not successful after saving the settings in configuration page.");

			Log.message("2. " + Caption.ConfigSettings.Config_ListingPropertiesPaneOnly.Value + " layout is selected and settings are saved.");

			//Step-3 : Login to the default page and click Advanced Search in search pane
			//--------------------------------------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches and logs into the default page

			Log.message("3. Logged into the default page.");

			//Verification : Verify if advanced search is displayed
			//-----------------------------------------------------
			if (!homePage.isSearchbarPresent()) //Verifies if Advanced search is displayed in No Java applet, no task area, but show Go To shortcuts layout
				Log.pass("Test case Passed. Search is not displayed in " +  Caption.ConfigSettings.Config_ListingPropertiesPaneOnly.Value + " layout.");
			else
				Log.fail("Test case Failed. Search bar is displayed in " +  Caption.ConfigSettings.Config_ListingPropertiesPaneOnly.Value + " layout.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			if(driver != null){
				try
				{
					driver.get(configURL);

					ConfigurationPage configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item

					configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}

			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_1_20_6

	/**
	 * 54.1.20.7 : Search bar should not be availble in Listing pane only layout.
	 */
	@Test(groups = {"Sprint54"}, description = "Search bar should not be availble in Listing pane only layout.")
	public void SprintTest54_1_20_7() throws Exception {

		driver = null; 

		try {




			driver = WebDriverUtils.getDriver();

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to Vault in tree view
			//-----------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item


			if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase())) //Checks if navigated to vault settings page
				throw new Exception("Vault settings page in configuration is not opened.");

			Log.message("1. Navigated to Vault settings page.");

			//Step-2 : Select Listing pane only and save the settings
			//-------------------------------------------------------------------------
			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_ListingPaneOnly.Value); //Sets as Listing pane only layout

			if (!configurationPage.configurationPanel.getLayout().equalsIgnoreCase(Caption.ConfigSettings.Config_ListingPaneOnly.Value)) //Verifies if Listing pane only layout is selected
				throw new Exception("Default layout is not selected.");

			if (!configurationPage.configurationPanel.saveSettings()) //Saves the settings
				throw new Exception("Settings are not saved properly.");

			if (!configurationPage.logOut()) //Logs out from configuration page
				throw new Exception("Log out is not successful after saving the settings in configuration page.");

			Log.message("2. " + Caption.ConfigSettings.Config_ListingPaneOnly.Value + " layout is selected and settings are saved.");

			//Step-3 : Login to the default page and click Advanced Search in search pane
			//--------------------------------------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches and logs into the default page

			Log.message("3. Logged into the default page.");

			//Verification : Verify if advanced search is displayed
			//-----------------------------------------------------
			if (!homePage.isSearchbarPresent()) //Verifies if Advanced search is displayed in No Java applet, no task area, but show Go To shortcuts layout
				Log.pass("Test case Passed. Search is not displayed in " +  Caption.ConfigSettings.Config_ListingPaneOnly.Value + " layout.");
			else
				Log.fail("Test case Failed. Search bar is displayed in " +  Caption.ConfigSettings.Config_ListingPaneOnly.Value + " layout.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			if(driver != null){

				try
				{
					driver.get(configURL);

					ConfigurationPage configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item

					configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");

				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}

			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_1_20_7

	/**
	 * 54.1.22.1 : 'Reset all' should reset search conditions in default layout.
	 */
	@Test(groups = {"Sprint54"}, description = "'Reset all' should reset search conditions in default layout.")
	public void SprintTest54_1_22_1() throws Exception {

		driver = null; 

		try {




			driver = WebDriverUtils.getDriver();

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to Vault in tree view
			//-----------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item


			if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase())) //Checks if navigated to vault settings page
				throw new Exception("Vault settings page in configuration is not opened.");

			Log.message("1. Navigated to Vault settings page.");

			//Step-2 : Select Default layout and save the settings
			//----------------------------------------------------
			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value); //Sets as default layout

			if (!configurationPage.configurationPanel.getLayout().equalsIgnoreCase(Caption.ConfigSettings.Config_Default.Value)) //Verifies if default layout is selected
				throw new Exception("Default layout is not selected.");

			if (!configurationPage.configurationPanel.saveSettings()) //Saves the settings
				throw new Exception("Settings are not saved properly.");

			if (!configurationPage.logOut()) //Logs out from configuration page
				throw new Exception("Log out is not successful after saving the settings in configuration page.");

			Log.message("2. Default layout is selected and settings are saved.");

			//Step-3 : Login to the default page and change the search word
			//-------------------------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches and logs into the default page

			Log.message("3. Logged into the default page.");

			//Step-4 : Change the search option to Any word
			//----------------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true); //Clicks Advanced search button
			String prevOption = homePage.searchPanel.getSelectedSearchOption(); //Gets the selected search option
			homePage.searchPanel.setSearchOption(Caption.Search.SearchAnyWord.Value); //Sets Search option to Search any word

			Log.message("4. Search option is modified from '" + prevOption + "' to '" + Caption.Search.SearchAnyWord.Value + "'.");

			//Step-5 : Click Reset all button
			//-------------------------------
			homePage.searchPanel.resetAll(); //Clicks Reset all button

			Log.message("5. Reset all button is clicked.");

			//Verification : Verify if Reset all button reset the conditions
			//---------------------------------------------------------------
			if (homePage.searchPanel.getSelectedSearchOption().equalsIgnoreCase(prevOption)) //Verifies if reset all has reset the conditions
				Log.pass("Test case Passed. Reset all has reset the conditions in default layout.");
			else
				Log.fail("Test case Failed. Reset all has not reset the conditions in default layout.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			if(driver != null){
				try
				{
					driver.get(configURL);

					ConfigurationPage configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item

					configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest54_1_22_1

	/**
	 * 54.1.22.2 : 'Reset all' should reset search conditions in Default and Navigation pane layout.
	 */
	@Test(groups = {"Sprint54"}, description = "'Reset all' should reset search conditions in Default and Navigation pane layout.")
	public void SprintTest54_1_22_2() throws Exception {

		driver = null; 

		try {




			driver = WebDriverUtils.getDriver();

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to Vault in tree view
			//-----------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item


			if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase())) //Checks if navigated to vault settings page
				throw new Exception("Vault settings page in configuration is not opened.");

			Log.message("1. Navigated to Vault settings page.");

			//Step-2 : Select Default and navigation pane layout and save the settings
			//-------------------------------------------------------------------------
			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_DefaultAndNavigation.Value); //Sets as Default and Navigation layout

			if (!configurationPage.configurationPanel.getLayout().equalsIgnoreCase(Caption.ConfigSettings.Config_DefaultAndNavigation.Value)) //Verifies if default layout is selected
				throw new Exception("Default layout is not selected.");

			if (!configurationPage.configurationPanel.saveSettings()) //Saves the settings
				throw new Exception("Settings are not saved properly.");

			if (!configurationPage.logOut()) //Logs out from configuration page
				throw new Exception("Log out is not successful after saving the settings in configuration page.");

			Log.message("2. " + Caption.ConfigSettings.Config_DefaultAndNavigation.Value + " layout is selected and settings are saved.");

			//Step-3 : Login to the default page and change the search word
			//-------------------------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches and logs into the default page

			Log.message("3. Logged into the default page.");

			//Step-4 : Change the search option to Any word
			//----------------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true); //Clicks Advanced search button
			String prevOption = homePage.searchPanel.getSelectedSearchOption(); //Gets the selected search option
			homePage.searchPanel.setSearchOption(Caption.Search.SearchAnyWord.Value); //Sets Search option to Search any word

			Log.message("4. Search option is modified from '" + prevOption + "' to '" + Caption.Search.SearchAnyWord.Value + "'.");

			//Step-5 : Click Reset all button
			//-------------------------------
			homePage.searchPanel.resetAll(); //Clicks Reset all button

			Log.message("5. Reset all button is clicked.");

			//Verification : Verify if Reset all button reset the conditions
			//---------------------------------------------------------------
			if (homePage.searchPanel.getSelectedSearchOption().equalsIgnoreCase(prevOption)) //Verifies if reset all has reset the conditions
				Log.pass("Test case Passed. Reset all has reset the conditions in " + Caption.ConfigSettings.Config_DefaultAndNavigation.Value + " layout.");
			else
				Log.fail("Test case Failed. Reset all has not reset the conditions in " + Caption.ConfigSettings.Config_DefaultAndNavigation.Value + " layout.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			if(driver != null){
				try
				{
					driver.get(configURL);

					ConfigurationPage configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item

					configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}

			}
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest54_1_22_2

	/**
	 * 54.1.22.3 : 'Reset all' should reset search conditions in No Java Applet layout.
	 */
	@Test(groups = {"Sprint54"}, description = "'Reset all' should reset search conditions in No Java Applet layout.")
	public void SprintTest54_1_22_3() throws Exception {

		driver = null; 

		try {




			driver = WebDriverUtils.getDriver();

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to Vault in tree view
			//-----------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item


			if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase())) //Checks if navigated to vault settings page
				throw new Exception("Vault settings page in configuration is not opened.");

			Log.message("1. Navigated to Vault settings page.");

			//Step-2 : Select No Java Applet layout and save the settings
			//-------------------------------------------------------------------------
			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);//Disabled the java applet in configuration page

			if(configurationPage.configurationPanel.isJavaAppletEnabled())//Verify if java applet is disabled or not
				throw new Exception("Java applet is enabled.");

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings in configuration page
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. Java applet is disabled and Configuration settings are saved.");

			if (!configurationPage.logOut()) //Logs out from configuration page
				throw new Exception("Log out is not successful after saving the settings in configuration page.");

			Log.message("2. No java applet layout is selected and settings are saved.");

			//Step-3 : Login to the default page and change the search word
			//-------------------------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches and logs into the default page

			Log.message("3. Logged into the default page.");

			//Step-4 : Change the search option to Any word
			//----------------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true); //Clicks Advanced search button
			String prevOption = homePage.searchPanel.getSelectedSearchOption(); //Gets the selected search option
			homePage.searchPanel.setSearchOption(Caption.Search.SearchAnyWord.Value); //Sets Search option to Search any word

			Log.message("4. Search option is modified from '" + prevOption + "' to '" + Caption.Search.SearchAnyWord.Value + "'.");

			//Step-5 : Click Reset all button
			//-------------------------------
			homePage.searchPanel.resetAll(); //Clicks Reset all button

			Log.message("5. Reset all button is clicked.");

			//Verification : Verify if Reset all button reset the conditions
			//---------------------------------------------------------------
			if (homePage.searchPanel.getSelectedSearchOption().equalsIgnoreCase(prevOption)) //Verifies if reset all has reset the conditions
				Log.pass("Test case Passed. Reset all has reset the conditions in No java applet layout.");
			else
				Log.fail("Test case Failed. Reset all has not reset the conditions in  No java applet layout.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			if(driver != null){
				try
				{
					driver.get(configURL);

					ConfigurationPage configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item

					configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}

			}
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest54_1_22_3

	/**
	 * 54.1.22.4 : 'Reset all' should reset search conditions in No Java applet, no task area layout.
	 */
	@Test(groups = {"Sprint54"}, description = "'Reset all' should reset search conditions in No Java applet, no task area layout.")
	public void SprintTest54_1_22_4() throws Exception {

		driver = null; 

		try {




			driver = WebDriverUtils.getDriver();

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to Vault in tree view
			//-----------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item


			if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase())) //Checks if navigated to vault settings page
				throw new Exception("Vault settings page in configuration is not opened.");

			Log.message("1. Navigated to Vault settings page.");

			//Step-2 : Select No Java applet, no task area layout and save the settings
			//-------------------------------------------------------------------------
			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);

			if(configurationPage.configurationPanel.isJavaAppletEnabled())
				throw new Exception("Java applet is enabled");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_NoTaskArea.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("2. No java applet and no task area layout is selected and saved.");


			if (!configurationPage.logOut()) //Logs out from configuration page
				throw new Exception("Log out is not successful after saving the settings in configuration page.");


			//Step-3 : Login to the default page and change the search word
			//-------------------------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches and logs into the default page

			Log.message("3. Logged into the default page.");

			//Step-4 : Change the search option to Any word
			//----------------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true); //Clicks Advanced search button
			String prevOption = homePage.searchPanel.getSelectedSearchOption(); //Gets the selected search option
			homePage.searchPanel.setSearchOption(Caption.Search.SearchAnyWord.Value); //Sets Search option to Search any word

			Log.message("4. Search option is modified from '" + prevOption + "' to '" + Caption.Search.SearchAnyWord.Value + "'.");

			//Step-5 : Click Reset all button
			//-------------------------------
			homePage.searchPanel.resetAll(); //Clicks Reset all button

			Log.message("5. Reset all button is clicked.");

			//Verification : Verify if Reset all button reset the conditions
			//---------------------------------------------------------------
			if (homePage.searchPanel.getSelectedSearchOption().equalsIgnoreCase(prevOption)) //Verifies if reset all has reset the conditions
				Log.pass("Test case Passed. Reset all has reset the conditions in No java applet and No task area layout.");
			else
				Log.fail("Test case Failed. Reset all has not reset the conditions in No java applet and No task area layout.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			if(driver != null){
				try
				{

					driver.get(configURL);

					ConfigurationPage configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item

					configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");

				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}

			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest54_1_22_4

	/**
	 * 54.1.22.5 : 'Reset all' should reset search conditions in No Java applet, no task area, but show Go To shortcuts layout.
	 */
	@Test(groups = {"Sprint54"}, description = "'Reset all' should reset search conditions in No Java applet, no task area, but show Go To shortcuts layout.")
	public void SprintTest54_1_22_5() throws Exception {

		driver = null; 

		try {




			driver = WebDriverUtils.getDriver();

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to Vault in tree view
			//-----------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item


			if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase())) //Checks if navigated to vault settings page
				throw new Exception("Vault settings page in configuration is not opened.");

			Log.message("1. Navigated to Vault settings page.");

			//Step-2 : Select No Java applet, no task area layout and save the settings
			//-------------------------------------------------------------------------
			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);

			if(configurationPage.configurationPanel.isJavaAppletEnabled())
				throw new Exception("Java applet is enabled");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_TaskAreaWithShowGoTo.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. No java applet with task area with go to layout is selected and saved.");

			if (!configurationPage.logOut()) //Logs out from configuration page
				throw new Exception("Log out is not successful after saving the settings in configuration page.");

			Log.message("2. No java applet with task area layout is selected and settings are saved.");

			//Step-3 : Login to the default page and change the search word
			//-------------------------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches and logs into the default page

			Log.message("3. Logged into the default page.");

			//Step-4 : Change the search option to Any word
			//----------------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true); //Clicks Advanced search button
			String prevOption = homePage.searchPanel.getSelectedSearchOption(); //Gets the selected search option
			homePage.searchPanel.setSearchOption(Caption.Search.SearchAnyWord.Value); //Sets Search option to Search any word

			Log.message("4. Search option is modified from '" + prevOption + "' to '" + Caption.Search.SearchAnyWord.Value + "'.");

			//Step-5 : Click Reset all button
			//-------------------------------
			homePage.searchPanel.resetAll(); //Clicks Reset all button

			Log.message("5. Reset all button is clicked.");

			//Verification : Verify if Reset all button reset the conditions
			//---------------------------------------------------------------
			if (homePage.searchPanel.getSelectedSearchOption().equalsIgnoreCase(prevOption)) //Verifies if reset all has reset the conditions
				Log.pass("Test case Passed. Reset all has reset the conditions in ."+Caption.ConfigSettings.Config_TaskAreaWithShowGoTo.Value);
			else
				Log.fail("Test case Failed. Reset all has not reset the conditions in " + Caption.ConfigSettings.Config_TaskAreaWithShowGoTo.Value+ " layout.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			if(driver != null){
				try
				{
					driver.get(configURL);

					ConfigurationPage configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item

					configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}

			}
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest54_1_22_5

	/**
	 * 54.1.30.1A.1A : Default layout will be displayed for hyperlink url on selecting default layout in configuration and in hyperlink dialog - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "Get Hyperlink", "SKIP_JavaApplet"}, 
			description = "Default layout will be displayed for hyperlink url on selecting default layout in configuration and in hyperlink dialog - Context menu.")
	public void SprintTest54_1_30_1A_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to Vault in tree view
			//-----------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item


			if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase())) //Checks if navigated to vault settings page
				throw new Exception("Vault settings page in configuration is not opened.");

			Log.message("1. Navigated to Vault settings page.");

			//Step-2 : Select Default layout and save the settings
			//----------------------------------------------------
			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value); //Sets as default layout

			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Enable.Value);


			if (!configurationPage.configurationPanel.getLayout().equalsIgnoreCase(Caption.ConfigSettings.Config_Default.Value)) //Verifies if default layout is selected
				throw new Exception("Default layout is not selected.");

			if (!configurationPage.configurationPanel.saveSettings()) //Saves the settings
				throw new Exception("Settings are not saved properly.");

			if (!configurationPage.logOut()) //Logs out from configuration page
				throw new Exception("Log out is not successful after saving the settings in configuration page.");

			Log.message("2. Default layout is selected and settings are saved.");

			//Step-3 : Login to the default page and navigate to any view
			//--------------------------------------------------------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches and logs into the default page

			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("3. Logged into the default page and navigated to '" + viewToNavigate + "' view.");

			//Step-4 : Open Get Hyperlink dialog for the object from context menu
			//-------------------------------------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get Hyperlink from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get Hyperlink' title is not opened.");

			Log.message("4. Hyperlink dialog of an object (" + dataPool.get("ObjectName") + ") is opened through context menu.");

			//Step-5 : Copy the link from text box and close the Get Hyperlink dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get Hyperlink dialog

			Log.message("5. Hyperlink is copied and Get Hyperlink dialog is closed.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkText, userName, password, ""); //Navigates to the hyperlink url

			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied object Hyperlink.[Expected URL: "+hyperlinkText+" & Current URL : "+ driver.getCurrentUrl() +"]");

			Log.message("6. Object Hyperlink is opened in the browser.");

			//Verification : Verify if default layout is displayed
			//----------------------------------------------------
			String unAvailableLayouts = "";

			if (!homePage.isSearchbarPresent()) //Checks if Search bar present
				unAvailableLayouts = "Search Bar";

			if (!homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				unAvailableLayouts = unAvailableLayouts + "Task Pane;";

			if (!homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value))
				unAvailableLayouts = unAvailableLayouts + "Properties Pane;";

			if (!homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				unAvailableLayouts = unAvailableLayouts + "Menu Bar;";

			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				unAvailableLayouts = unAvailableLayouts + "Breadcrumb;";

			if (!homePage.taskPanel.isAppletEnabled())
				unAvailableLayouts = unAvailableLayouts + "Java Applet;";

			if (!homePage.isListViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "Metadatacard;";

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page is displayed as Default layout.");
			else
				Log.fail("Test case Failed. Few layots are missing on selecting 'Default' custom layout. Missed layots : " 
						+ unAvailableLayouts, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_1_30_1A_1A

	/**
	 * 54.1.30.1A.1B : Default layout will be displayed for hyperlink url on selecting default layout in configuration and in hyperlink dialog - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "Get Hyperlink", "SKIP_JavaApplet"}, 
			description = "Default layout will be displayed for hyperlink url on selecting default layout in configuration and in hyperlink dialog - Operations menu.")
	public void SprintTest54_1_30_1A_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {




			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to Vault in tree view
			//-----------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item


			if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase())) //Checks if navigated to vault settings page
				throw new Exception("Vault settings page in configuration is not opened.");

			Log.message("1. Navigated to Vault settings page.");

			//Step-2 : Select Default layout and save the settings
			//----------------------------------------------------
			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value); //Sets as default layout

			if (!configurationPage.configurationPanel.getLayout().equalsIgnoreCase(Caption.ConfigSettings.Config_Default.Value)) //Verifies if default layout is selected
				throw new Exception("Default layout is not selected.");

			if (!configurationPage.configurationPanel.saveSettings()) //Saves the settings
				throw new Exception("Settings are not saved properly.");

			if (!configurationPage.logOut()) //Logs out from configuration page
				throw new Exception("Log out is not successful after saving the settings in configuration page.");

			Log.message("2. Default layout is selected and settings are saved.");

			//Step-3 : Login to the default page and navigate to any view
			//--------------------------------------------------------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches and logs into the default page

			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("3. Logged into the default page and navigated to '" + viewToNavigate + "' view.");

			//Step-4 : Open Get Hyperlink dialog for the object from operations menu
			//-----------------------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get Hyperlink from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get Hyperlink' title is not opened.");

			Log.message("4. Hyperlink dialog of an object (" + dataPool.get("ObjectName") + ") is opened through operations menu.");

			//Step-5 : Copy the link from text box and close the Get Hyperlink dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get Hyperlink dialog

			Log.message("5. Hyperlink is copied and Get Hyperlink dialog is closed.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkText, userName, password, ""); //Navigates to the hyperlink url

			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied object Hyperlink.[Expected URL: "+hyperlinkText+" & Current URL : "+ driver.getCurrentUrl() +"]");

			Log.message("6. Object Hyperlink is opened in the browser.");

			//Verification : Verify if default layout is displayed
			//----------------------------------------------------
			String unAvailableLayouts = "";

			if (!homePage.isSearchbarPresent()) //Checks if Search bar present
				unAvailableLayouts = "Search Bar";

			if (!homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				unAvailableLayouts = unAvailableLayouts + "Task Pane;";

			if (!homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value))
				unAvailableLayouts = unAvailableLayouts + "Properties Pane;";

			if (!homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				unAvailableLayouts = unAvailableLayouts + "Menu Bar;";

			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				unAvailableLayouts = unAvailableLayouts + "Breadcrumb;";

			if (!homePage.taskPanel.isAppletEnabled())
				unAvailableLayouts = unAvailableLayouts + "Java Applet;";

			if (!homePage.isListViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "Metadatacard;";

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page is displayed as Default layout.");
			else
				Log.fail("Test case Failed. Few layots are missing on selecting 'Default' custom layout. Missed layots : " 
						+ unAvailableLayouts, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_1_30_1A_1B

	/**
	 * 54.1.30.2A.1A : Default layout with navigation pane will be displayed for hyperlink url on selecting Default layout with navigation pane layout in configuration and default in hyperlink dialog - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "Get Hyperlink", "SKIP_JavaApplet"}, 
			description = "Default layout with navigation pane will be displayed for hyperlink url on selecting Default layout with navigation pane layout in configuration and default in hyperlink dialog - Context menu.")
	public void SprintTest54_1_30_2A_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {




			driver = WebDriverUtils.getDriver();


			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to Vault in tree view
			//-----------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item


			if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase())) //Checks if navigated to vault settings page
				throw new Exception("Vault settings page in configuration is not opened.");

			Log.message("1. Navigated to Vault settings page.");

			//Step-2 : Select Default and navigate pane layout and save the settings
			//--------------------------------------------------------------------
			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_DefaultAndNavigation.Value); //Sets as Default layout with navigation pane layout

			if (!configurationPage.configurationPanel.getLayout().equalsIgnoreCase(Caption.ConfigSettings.Config_DefaultAndNavigation.Value)) //Verifies if Default layout with navigation pane is selected
				throw new Exception(Caption.ConfigSettings.Config_DefaultAndNavigation.Value + " layout is not selected.");

			if (!configurationPage.configurationPanel.saveSettings()) //Saves the settings
				throw new Exception("Settings are not saved properly.");

			if (!configurationPage.logOut()) //Logs out from configuration page
				throw new Exception("Log out is not successful after saving the settings in configuration page.");

			Log.message("2. " + Caption.ConfigSettings.Config_DefaultAndNavigation.Value + " layout is selected and settings are saved.");

			//Step-3 : Login to the default page and navigate to any view
			//--------------------------------------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches and logs into the default page

			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("3. Logged into the default page and navigated to '" + viewToNavigate + "' view.");

			//Step-4 : Open Get Hyperlink dialog for the object from context menu
			//-------------------------------------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get Hyperlink from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get Hyperlink' title is not opened.");

			Log.message("4. Hyperlink dialog of an object (" + dataPool.get("ObjectName") + ") is opened through context menu.");

			//Step-5 : Copy the link from text box and close the Get Hyperlink dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get Hyperlink dialog

			Log.message("5. Hyperlink is copied and Get Hyperlink dialog is closed.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkText, userName, password, ""); //Navigates to the hyperlink url

			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied object Hyperlink.[Expected URL: "+hyperlinkText+" & Current URL : "+ driver.getCurrentUrl() +"]");

			Log.message("6. Object Hyperlink is opened in the browser.");

			//Verification : Verify if default layout is displayed
			//----------------------------------------------------
			String unAvailableLayouts = "";

			if (!homePage.isSearchbarPresent()) //Checks if Search bar present
				unAvailableLayouts = "Search Bar";

			if (!homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				unAvailableLayouts = unAvailableLayouts + "Task Pane;";

			if (!homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value))
				unAvailableLayouts = unAvailableLayouts + "Properties Pane;";

			if (!homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				unAvailableLayouts = unAvailableLayouts + "Menu Bar;";

			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				unAvailableLayouts = unAvailableLayouts + "Breadcrumb;";

			if (!homePage.taskPanel.isAppletEnabled())
				unAvailableLayouts = unAvailableLayouts + "Java Applet;";

			if (!homePage.isListViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "Metadatacard;";

			if (!homePage.isTreeViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "Tree View;";

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page is displayed as Default layout with Navigation pane.");
			else
				Log.fail("Test case Failed. Few layots are missing on selecting 'Default layout and navigation pane' layout. Missed layots : " 
						+ unAvailableLayouts, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			if(driver != null){
				try
				{
					driver.get(configURL);

					ConfigurationPage configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item

					configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}

			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest54_1_30_2A_1A

	/**
	 * 54.1.30.2A.1B : Default layout with navigation pane will be displayed for hyperlink url on selecting Default layout with navigation pane layout in configuration and default in hyperlink dialog - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "Get Hyperlink", "SKIP_JavaApplet"}, 
			description = "Default layout with navigation pane will be displayed for hyperlink url on selecting Default layout with navigation pane layout in configuration and default in hyperlink dialog - Operations menu.")
	public void SprintTest54_1_30_2A_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to Vault in tree view
			//-----------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item


			if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase())) //Checks if navigated to vault settings page
				throw new Exception("Vault settings page in configuration is not opened.");

			Log.message("1. Navigated to Vault settings page.");

			//Step-2 : Select Default and navigate pane layout and save the settings
			//--------------------------------------------------------------------
			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_DefaultAndNavigation.Value); //Sets as Default and navigate pane layout

			if (!configurationPage.configurationPanel.getLayout().equalsIgnoreCase(Caption.ConfigSettings.Config_DefaultAndNavigation.Value)) //Verifies if Default and navigate pane layout is selected
				throw new Exception(Caption.ConfigSettings.Config_DefaultAndNavigation.Value + " layout is not selected.");

			if (!configurationPage.configurationPanel.saveSettings()) //Saves the settings
				throw new Exception("Settings are not saved properly.");

			if (!configurationPage.logOut()) //Logs out from configuration page
				throw new Exception("Log out is not successful after saving the settings in configuration page.");

			Log.message("2. " + Caption.ConfigSettings.Config_DefaultAndNavigation.Value + " layout is selected and settings are saved.");

			//Step-3 : Login to the default page and navigate to any view
			//--------------------------------------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches and logs into the default page

			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("3. Logged into the default page and navigated to '" + viewToNavigate + "' view.");

			//Step-4 : Open Get Hyperlink dialog for the object from operations menu
			//-----------------------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get Hyperlink from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get Hyperlink' title is not opened.");

			Log.message("4. Hyperlink dialog of an object (" + dataPool.get("ObjectName") + ") is opened through operations menu.");

			//Step-5 : Copy the link from text box and close the Get Hyperlink dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get Hyperlink dialog

			Log.message("5. Hyperlink is copied and Get Hyperlink dialog is closed.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkText, userName, password, ""); //Navigates to the hyperlink url

			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied object Hyperlink.[Expected URL: "+hyperlinkText+" & Current URL : "+ driver.getCurrentUrl() +"]");

			Log.message("6. Object Hyperlink is opened in the browser.");

			//Verification : Verify if default layout is displayed
			//----------------------------------------------------
			String unAvailableLayouts = "";

			if (!homePage.isSearchbarPresent()) //Checks if Search bar present
				unAvailableLayouts = "Search Bar";

			if (!homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				unAvailableLayouts = unAvailableLayouts + "Task Pane;";

			if (!homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value))
				unAvailableLayouts = unAvailableLayouts + "Properties Pane;";

			if (!homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				unAvailableLayouts = unAvailableLayouts + "Menu Bar;";

			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				unAvailableLayouts = unAvailableLayouts + "Breadcrumb;";

			if (!homePage.taskPanel.isAppletEnabled())
				unAvailableLayouts = unAvailableLayouts + "Java Applet;";

			if (!homePage.isListViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "Metadatacard;";

			if (!homePage.isTreeViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "Tree View;";

			if(!homePage.isNavigationPaneDisplayed())
				unAvailableLayouts = unAvailableLayouts + "Navigation Pane;";

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page is displayed as Default layout with Navigation pane.");
			else
				Log.fail("Test case Failed. Few layots are missing on selecting 'Default layout and navigation pane' layout. Missed layots : " 
						+ unAvailableLayouts, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			if(driver != null){
				try
				{
					driver.get(configURL);

					ConfigurationPage configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item

					configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);
					configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}

			}
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest54_1_30_2A_1B

	/**
	 * 54.1.30.3A.1A : No Java applet layout will be displayed for hyperlink url on selecting No Java applet in configuration and default in hyperlink dialog - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "Get Hyperlink"}, 
			description = "No Java applet layout will be displayed for hyperlink url on selecting No Java applet in configuration and default in hyperlink dialog - Context menu.")
	public void SprintTest54_1_30_3A_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {




			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to Vault in tree view
			//-----------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item


			if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase())) //Checks if navigated to vault settings page
				throw new Exception("Vault settings page in configuration is not opened.");

			Log.message("1. Navigated to Vault settings page.");

			//Step-2 : Select No Java applet layout layout and save the settings
			//--------------------------------------------------------------------
			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);//Disabled the java applet in configuration page

			if(configurationPage.configurationPanel.isJavaAppletEnabled())//Verify if java applet is disabled or not
				throw new Exception("Java applet is enabled.");

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings in configuration page
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. Java applet is disabled and Configuration settings are saved.");

			if (!configurationPage.logOut()) //Logs out from configuration page
				throw new Exception("Log out is not successful after saving the settings in configuration page.");

			Log.message("2. No java applet layout is selected and settings are saved.");

			//Step-3 : Login to the default page and navigate to any view
			//--------------------------------------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches and logs into the default page

			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("3. Logged into the default page and navigated to '" + viewToNavigate + "' view.");

			//Step-4 : Open Get Hyperlink dialog for the object from context menu
			//-------------------------------------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get Hyperlink from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get Hyperlink' title is not opened.");

			Log.message("4. Hyperlink dialog of an object (" + dataPool.get("ObjectName") + ") is opened through context menu.");

			//Step-5 : Copy the link from text box and close the Get Hyperlink dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get Hyperlink dialog

			Log.message("5. Hyperlink is copied and Get Hyperlink dialog is closed.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkText, userName, password, ""); //Navigates to the hyperlink url


			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied object Hyperlink.[Expected URL: "+hyperlinkText+" & Current URL : "+ driver.getCurrentUrl() +"]");

			Log.message("6. Object Hyperlink is opened in the browser.");

			//Verification : Verify if default layout is displayed
			//----------------------------------------------------
			String unAvailableLayouts = "";

			if (!homePage.isSearchbarPresent()) //Checks if Search bar present
				unAvailableLayouts = "Search Bar is not available;";

			if (!homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				unAvailableLayouts = unAvailableLayouts + "Task Pane is not available;";

			if (!homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value))
				unAvailableLayouts = unAvailableLayouts + "Properties Pane is not available;";

			if (!homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				unAvailableLayouts = unAvailableLayouts + "Menu Bar is not available;";

			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				unAvailableLayouts = unAvailableLayouts + "Breadcrumb is not available;";

			if (homePage.taskPanel.isAppletEnabled())
				unAvailableLayouts = unAvailableLayouts + "Java Applet is available;";

			if (!homePage.isListViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "List View is not available;";

			if (homePage.isTreeViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "Tree View is available;";

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page is displayed as No Java Applet layout.");
			else
				Log.fail("Test case Failed. Few layots are not as expected on selecting 'No Java Applet layout'. Refer additional information : " 
						+ unAvailableLayouts, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			if(driver != null){
				try
				{

					driver.get(configURL);

					ConfigurationPage configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item

					configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest54_1_30_3A_1A

	/**
	 * 54.1.30.3A.1B : No Java applet layout will be displayed for hyperlink url on selecting No Java applet in configuration and default in hyperlink dialog - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "Get Hyperlink"}, 
			description = "No Java applet layout will be displayed for hyperlink url on selecting No Java applet in configuration and default in hyperlink dialog - Operations menu.")
	public void SprintTest54_1_30_3A_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {




			driver = WebDriverUtils.getDriver();


			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to Vault in tree view
			//-----------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item


			if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase())) //Checks if navigated to vault settings page
				throw new Exception("Vault settings page in configuration is not opened.");

			Log.message("1. Navigated to Vault settings page.");

			//Step-2 : Select No Java applet layout layout and save the settings
			//--------------------------------------------------------------------
			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);//Disabled the java applet in configuration page

			if(configurationPage.configurationPanel.isJavaAppletEnabled())//Verify if java applet is disabled or not
				throw new Exception("Java applet is enabled.");

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings in configuration page
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. Java applet is disabled and Configuration settings are saved.");

			if (!configurationPage.logOut()) //Logs out from configuration page
				throw new Exception("Log out is not successful after saving the settings in configuration page.");

			Log.message("2. No java applet layout is selected and settings are saved.");

			//Step-3 : Login to the default page and navigate to any view
			//--------------------------------------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches and logs into the default page

			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("3. Logged into the default page and navigated to '" + viewToNavigate + "' view.");

			//Step-4 : Open Get Hyperlink dialog for the object from operations menu
			//-----------------------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get Hyperlink from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get Hyperlink' title is not opened.");

			Log.message("4. Hyperlink dialog of an object (" + dataPool.get("ObjectName") + ") is opened through operations menu.");

			//Step-5 : Copy the link from text box and close the Get Hyperlink dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get Hyperlink dialog

			Log.message("5. Hyperlink is copied and Get Hyperlink dialog is closed.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkText, userName, password, ""); //Navigates to the hyperlink url


			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied object Hyperlink.[Expected URL: "+hyperlinkText+" & Current URL : "+ driver.getCurrentUrl() +"]");

			Log.message("6. Object Hyperlink is opened in the browser.");

			//Verification : Verify if default layout is displayed
			//----------------------------------------------------
			String unAvailableLayouts = "";

			if (!homePage.isSearchbarPresent()) //Checks if Search bar present
				unAvailableLayouts = "Search Bar is not available;";

			if (!homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				unAvailableLayouts = unAvailableLayouts + "Task Pane is not available;";

			if (!homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value))
				unAvailableLayouts = unAvailableLayouts + "Properties Pane is not available;";

			if (!homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				unAvailableLayouts = unAvailableLayouts + "Menu Bar is not available;";

			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				unAvailableLayouts = unAvailableLayouts + "Breadcrumb is not available;";

			if (homePage.taskPanel.isAppletEnabled())
				unAvailableLayouts = unAvailableLayouts + "Java Applet is available;";

			if (!homePage.isListViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "List View is not available;";

			if (homePage.isTreeViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "Tree View is available;";

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page is displayed as No Java Applet layout.");
			else
				Log.fail("Test case Failed. Few layots are not as expected on selecting 'No Java Applet layout'. Refer additional information : " 
						+ unAvailableLayouts, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			if(driver != null){
				try
				{
					driver.get(configURL);

					ConfigurationPage configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item

					configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}

			}
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest54_1_30_3A_1B

	/**
	 * 54.1.30.4A.1A : No Java applet and no task area layout will be displayed for hyperlink url on selecting No Java applet and no task area layout in configuration and default in hyperlink dialog - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "Get Hyperlink"}, 
			description = "No Java applet and no task area layout will be displayed for hyperlink url on selecting No Java applet and no task area layout in configuration and default in hyperlink dialog - Context menu.")
	public void SprintTest54_1_30_4A_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to Vault in tree view
			//---------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item


			if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase())) //Checks if navigated to vault settings page
				throw new Exception("Vault settings page in configuration is not opened.");

			Log.message("1. Navigated to Vault settings page.");

			//Step-2 : Select No Java applet and No task area layout layout and save the settings
			//--------------------------------------------------------------------
			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);

			if(configurationPage.configurationPanel.isJavaAppletEnabled())
				throw new Exception("Java applet is enabled");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_NoTaskArea.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. No java applet and no task area layout is selected and saved.");

			if (!configurationPage.logOut()) //Logs out from configuration page
				throw new Exception("Log out is not successful after saving the settings in configuration page.");

			Log.message("2. Logout from the configuration page.");

			//Step-3 : Login to the default page and navigate to any view
			//--------------------------------------------------------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches and logs into the default page

			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("3. Logged into the default page and navigated to '" + viewToNavigate + "' view.");

			//Step-4 : Open Get Hyperlink dialog for the object from context menu
			//-------------------------------------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get Hyperlink from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get Hyperlink' title is not opened.");

			Log.message("4. Hyperlink dialog of an object (" + dataPool.get("ObjectName") + ") is opened through context menu.");

			//Step-5 : Copy the link from text box and close the Get Hyperlink dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get Hyperlink dialog

			Log.message("5. Hyperlink is copied and Get Hyperlink dialog is closed.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkText, userName, password, ""); //Navigates to the hyperlink url


			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied object Hyperlink.[Expected URL: "+hyperlinkText+" & Current URL : "+ driver.getCurrentUrl() +"]");

			Log.message("6. Object Hyperlink is opened in the browser.");

			//Verification : Verify if default layout is displayed
			//----------------------------------------------------
			String unAvailableLayouts = "";

			if (!homePage.isSearchbarPresent()) //Checks if Search bar present
				unAvailableLayouts = "Search Bar is not available;";

			if (homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				unAvailableLayouts = unAvailableLayouts + "Task Pane is available;";

			if (!homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value))
				unAvailableLayouts = unAvailableLayouts + "Properties Pane is not available;";

			if (!homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				unAvailableLayouts = unAvailableLayouts + "Menu Bar is not available;";

			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				unAvailableLayouts = unAvailableLayouts + "Breadcrumb is not available;";

			if (homePage.taskPanel.isAppletEnabled())
				unAvailableLayouts = unAvailableLayouts + "Java Applet is available;";

			if (!homePage.isListViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "List View is not available;";

			if (homePage.isTreeViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "Tree View is available;";

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page is displayed as No java applet and No task area layout");
			else
				Log.fail("Test case Failed. Few layots are not as expected on selecting No java applet and No task area layout"
						+ unAvailableLayouts, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			if(driver != null){
				try
				{
					driver.get(configURL);

					ConfigurationPage configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item

					configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}

			}
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest54_1_30_4A_1A

	/**
	 * 54.1.30.4A.1B : No Java applet and no task area layout will be displayed for hyperlink url on selecting No Java applet and no task area layout in configuration and default in hyperlink dialog - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "Get Hyperlink"}, 
			description = "No Java applet and no task area layout will be displayed for hyperlink url on selecting No Java applet and no task area layout in configuration and default in hyperlink dialog - Operations menu.")
	public void SprintTest54_1_30_4A_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {





			driver = WebDriverUtils.getDriver();


			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to Vault in tree view
			//---------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item


			if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase())) //Checks if navigated to vault settings page
				throw new Exception("Vault settings page in configuration is not opened.");

			Log.message("1. Navigated to Vault settings page.");

			//Step-2 : Select No Java applet and No task area layout layout and save the settings
			//--------------------------------------------------------------------
			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);

			if(configurationPage.configurationPanel.isJavaAppletEnabled())
				throw new Exception("Java applet is enabled");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_NoTaskArea.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. No java applet and no task area layout is selected and saved.");

			if (!configurationPage.logOut()) //Logs out from configuration page
				throw new Exception("Log out is not successful after saving the settings in configuration page.");

			Log.message("2. No Java applet and no task area layout is selected and settings are saved in configuration page & log out from the configuration page.");

			//Step-3 : Login to the default page and navigate to any view
			//--------------------------------------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches and logs into the default page

			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("3. Logged into the default page and navigated to '" + viewToNavigate + "' view.");

			//Step-4 : Open Get Hyperlink dialog for the object from operations menu
			//-----------------------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get Hyperlink from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get Hyperlink' title is not opened.");

			Log.message("4. Hyperlink dialog of an object (" + dataPool.get("ObjectName") + ") is opened through operations menu.");

			//Step-5 : Copy the link from text box and close the Get Hyperlink dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get Hyperlink dialog

			Log.message("5. Hyperlink is copied and Get Hyperlink dialog is closed.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkText, userName, password, ""); //Navigates to the hyperlink url


			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied object Hyperlink.[Expected URL: "+hyperlinkText+" & Current URL : "+ driver.getCurrentUrl() +"]");

			Log.message("6. Object Hyperlink is opened in the browser.");

			//Verification : Verify if default layout is displayed
			//----------------------------------------------------
			String unAvailableLayouts = "";

			if (!homePage.isSearchbarPresent()) //Checks if Search bar present
				unAvailableLayouts = "Search Bar is not available;";

			if (homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				unAvailableLayouts = unAvailableLayouts + "Task Pane is available;";

			if (!homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value))
				unAvailableLayouts = unAvailableLayouts + "Properties Pane is not available;";

			if (!homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				unAvailableLayouts = unAvailableLayouts + "Menu Bar is not available;";

			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				unAvailableLayouts = unAvailableLayouts + "Breadcrumb is not available;";

			if (homePage.taskPanel.isAppletEnabled())
				unAvailableLayouts = unAvailableLayouts + "Java Applet is available;";

			if (!homePage.isListViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "List View is not available;";

			if (homePage.isTreeViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "Tree View is available;";

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page is displayed as No java applet and No task area layout.");
			else
				Log.fail("Test case Failed. Few layots are not as expected on selecting No java applet and No task area layout."
						+ unAvailableLayouts, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			if(driver != null){
				try
				{
					driver.get(configURL);

					ConfigurationPage configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item

					configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest54_1_30_4A_1B

	/**
	 * 54.1.30.5A.1A : No Java applet and no task area (but show 'Go To' shortcuts) layout will be displayed for hyperlink url on selecting No Java applet and no task area (but show 'Go To' shortcuts) layout in configuration and default in hyperlink dialog - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "Get Hyperlink"}, 
			description = "No Java applet and no task area (but show 'Go To' shortcuts) layout will be displayed for hyperlink url on selecting No Java applet and no task area (but show 'Go To' shortcuts) layout in configuration and default in hyperlink dialog - Context menu.")
	public void SprintTest54_1_30_5A_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {




			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to Vault in tree view
			//---------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item


			if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase())) //Checks if navigated to vault settings page
				throw new Exception("Vault settings page in configuration is not opened.");

			Log.message("1. Navigated to Vault settings page.");

			//Step-2 : Select No Java applet and No task area but show Go to shortcuts layout layout and save the settings
			//------------------------------------------------------------------------------------------------------------
			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);

			if(configurationPage.configurationPanel.isJavaAppletEnabled())
				throw new Exception("Java applet is enabled");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_TaskAreaWithShowGoTo.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("2. No java applet with task area with go to layout is selected and saved.");

			if (!configurationPage.logOut()) //Logs out from configuration page
				throw new Exception("Log out is not successful after saving the settings in configuration page.");

			Log.message("2. " + Caption.ConfigSettings.Config_TaskAreaWithShowGoTo.Value + " layout is selected and settings are saved.");

			//Step-3 : Login to the default page and navigate to any view
			//-------------------------------------------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches and logs into the default page

			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("3. Logged into the default page and navigated to '" + viewToNavigate + "' view.");

			//Step-4 : Open Get Hyperlink dialog for the object from context menu
			//-------------------------------------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get Hyperlink from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get Hyperlink' title is not opened.");

			Log.message("4. Hyperlink dialog of an object (" + dataPool.get("ObjectName") + ") is opened through context menu.");

			//Step-5 : Copy the link from text box and close the Get Hyperlink dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get Hyperlink dialog

			Log.message("5. Hyperlink is copied and Get Hyperlink dialog is closed.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkText, userName, password, ""); //Navigates to the hyperlink url


			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied object Hyperlink.[Expected URL: "+hyperlinkText+" & Current URL : "+ driver.getCurrentUrl() +"]");

			Log.message("6. Object Hyperlink is opened in the browser.");

			//Verification : Verify if default layout is displayed
			//----------------------------------------------------
			String unAvailableLayouts = "";

			if (!homePage.isSearchbarPresent()) //Checks if Search bar present
				unAvailableLayouts = "Search Bar is not available;";

			if (homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				unAvailableLayouts = unAvailableLayouts + "Task Pane is available;";

			if (!homePage.taskPanel.isTaskPaneGoToDisplayed()) //Checks if Task Pane GoTo present
				unAvailableLayouts = unAvailableLayouts + "Task Pane GoTo is not available;";

			if (!homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value))
				unAvailableLayouts = unAvailableLayouts + "Properties Pane is not available;";

			if (!homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				unAvailableLayouts = unAvailableLayouts + "Menu Bar is not available;";

			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				unAvailableLayouts = unAvailableLayouts + "Breadcrumb is not available;";

			if (homePage.taskPanel.isAppletEnabled())
				unAvailableLayouts = unAvailableLayouts + "Java Applet is available;";

			if (!homePage.isListViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "List View is not available;";

			if (homePage.isTreeViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "Tree View is available;";

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page is displayed as ." + Caption.ConfigSettings.Config_TaskAreaWithShowGoTo.Value);
			else
				Log.fail("Test case Failed. Few layots are not as expected on selecting " + Caption.ConfigSettings.Config_TaskAreaWithShowGoTo.Value + ". Refer additional information : " 
						+ unAvailableLayouts, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			if(driver != null){
				try
				{
					driver.get(configURL);

					ConfigurationPage configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item

					configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}

			}
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest54_1_30_5A_1A

	/**
	 * 54.1.30.5A.1B : No Java applet and no task area (but show 'Go To' shortcuts) layout will be displayed for hyperlink url on selecting No Java applet and no task area (but show 'Go To' shortcuts) layout in configuration and default in hyperlink dialog - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "Get Hyperlink"}, 
			description = "No Java applet and no task area (but show 'Go To' shortcuts) layout will be displayed for hyperlink url on selecting No Java applet and no task area (but show 'Go To' shortcuts) layout in configuration and default in hyperlink dialog - Operations menu.")
	public void SprintTest54_1_30_5A_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {




			driver = WebDriverUtils.getDriver();


			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to Vault in tree view
			//---------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item


			if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase())) //Checks if navigated to vault settings page
				throw new Exception("Vault settings page in configuration is not opened.");

			Log.message("1. Navigated to Vault settings page.");

			//Step-2 : Select No Java applet and No task area but show Go to shortcuts layout layout and save the settings
			//------------------------------------------------------------------------------------------------------------
			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);

			if(configurationPage.configurationPanel.isJavaAppletEnabled())
				throw new Exception("Java applet is enabled");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_TaskAreaWithShowGoTo.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. No java applet with task area with go to layout is selected and saved.");

			if (!configurationPage.logOut()) //Logs out from configuration page
				throw new Exception("Log out is not successful after saving the settings in configuration page.");

			Log.message("2. " + Caption.ConfigSettings.Config_TaskAreaWithShowGoTo.Value + " layout is selected and settings are saved.");

			//Step-3 : Login to the default page and navigate to any view
			//-------------------------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches and logs into the default page

			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("3. Logged into the default page and navigated to '" + viewToNavigate + "' view.");

			//Step-4 : Open Get Hyperlink dialog for the object from operations menu
			//-----------------------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get Hyperlink from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get Hyperlink' title is not opened.");

			Log.message("4. Hyperlink dialog of an object (" + dataPool.get("ObjectName") + ") is opened through operations menu.");

			//Step-5 : Copy the link from text box and close the Get Hyperlink dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get Hyperlink dialog

			Log.message("5. Hyperlink is copied and Get Hyperlink dialog is closed.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkText, userName, password, ""); //Navigates to the hyperlink url


			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied object Hyperlink.[Expected URL: "+hyperlinkText+" & Current URL : "+ driver.getCurrentUrl() +"]");

			Log.message("6. Object Hyperlink is opened in the browser.");

			//Verification : Verify if default layout is displayed
			//----------------------------------------------------
			String unAvailableLayouts = "";

			if (!homePage.isSearchbarPresent()) //Checks if Search bar present
				unAvailableLayouts = "Search Bar is not available;";

			if (homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				unAvailableLayouts = unAvailableLayouts + "Task Pane is available;";

			if (!homePage.taskPanel.isTaskPaneGoToDisplayed()) //Checks if Task Pane GoTo present
				unAvailableLayouts = unAvailableLayouts + "Task Pane GoTo is not available;";

			if (!homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value))
				unAvailableLayouts = unAvailableLayouts + "Properties Pane is not available;";

			if (!homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				unAvailableLayouts = unAvailableLayouts + "Menu Bar is not available;";

			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				unAvailableLayouts = unAvailableLayouts + "Breadcrumb is not available;";

			if (homePage.taskPanel.isAppletEnabled())
				unAvailableLayouts = unAvailableLayouts + "Java Applet is available;";

			if (!homePage.isListViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "List View is not available;";

			if (homePage.isTreeViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "Tree View is available;";

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page is displayed as ." + Caption.ConfigSettings.Config_TaskAreaWithShowGoTo.Value);
			else
				Log.fail("Test case Failed. Few layots are not as expected on selecting " + Caption.ConfigSettings.Config_TaskAreaWithShowGoTo.Value + ". Refer additional information : " 
						+ unAvailableLayouts, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			if(driver != null){
				try
				{
					driver.get(configURL);

					ConfigurationPage configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item

					configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}

			}
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest54_1_30_5A_1B

	/**
	 * 54.1.30.6A.1 : Listing pane and properties pane only layout will be displayed for hyperlink url on selecting Listing pane and properties pane only layout in configuration and default in hyperlink dialog - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "Get Hyperlink"}, 
			description = "Listing pane and properties pane only layout will be displayed for hyperlink url on selecting Listing pane and properties pane only layout in configuration and default in hyperlink dialog - Context menu.")
	public void SprintTest54_1_30_6A_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {




			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to Vault in tree view
			//---------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item


			if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase())) //Checks if navigated to vault settings page
				throw new Exception("Vault settings page in configuration is not opened.");

			Log.message("1. Navigated to Vault settings page.");

			//Step-2 : Select Listing pane and properties pane layout layout and save the settings
			//------------------------------------------------------------------------------------------------------------
			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_ListingPropertiesPaneOnly.Value); //Sets as Listing pane and properties pane layout

			if (!configurationPage.configurationPanel.getLayout().equalsIgnoreCase(Caption.ConfigSettings.Config_ListingPropertiesPaneOnly.Value)) //Verifies if Listing pane and properties pane layout is selected
				throw new Exception(Caption.ConfigSettings.Config_ListingPropertiesPaneOnly.Value + " layout is not selected.");

			if (!configurationPage.configurationPanel.saveSettings()) //Saves the settings
				throw new Exception("Settings are not saved properly.");

			if (!configurationPage.logOut()) //Logs out from configuration page
				throw new Exception("Log out is not successful after saving the settings in configuration page.");

			Log.message("2. " + Caption.ConfigSettings.Config_ListingPropertiesPaneOnly.Value + " layout is selected and settings are saved.");

			//Step-3 : Login to the default page and navigate to any view
			//-------------------------------------------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches and logs into the default page

			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("3. Logged into the default page and navigated to '" + viewToNavigate + "' view.");

			//Step-4 : Open Get Hyperlink dialog for the object from context menu
			//-------------------------------------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get Hyperlink from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get Hyperlink' title is not opened.");

			Log.message("4. Hyperlink dialog of an object (" + dataPool.get("ObjectName") + ") is opened through context menu.");

			//Step-5 : Copy the link from text box and close the Get Hyperlink dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get Hyperlink dialog

			Log.message("5. Hyperlink is copied and Get Hyperlink dialog is closed.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkText, userName, password, ""); //Navigates to the hyperlink url


			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied object Hyperlink.[Expected URL: "+hyperlinkText+" & Current URL : "+ driver.getCurrentUrl() +"]");

			Log.message("6. Object Hyperlink is opened in the browser.");

			//Verification : Verify if default layout is displayed
			//----------------------------------------------------
			String unAvailableLayouts = "";

			if (homePage.isSearchbarPresent()) //Checks if Search bar present
				unAvailableLayouts = "Search Bar is available;";

			if (homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				unAvailableLayouts = unAvailableLayouts + "Task Pane is available;";

			if (!homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value))
				unAvailableLayouts = unAvailableLayouts + "Properties Pane is not available;";

			if (homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				unAvailableLayouts = unAvailableLayouts + "Menu Bar is available;";

			if (homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				unAvailableLayouts = unAvailableLayouts + "Breadcrumb is available;";

			if (homePage.taskPanel.isAppletEnabled())
				unAvailableLayouts = unAvailableLayouts + "Java Applet is available;";

			if (!homePage.isListViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "List View is not available;";

			if (homePage.isTreeViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "Tree View is available;";

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page is displayed as ." + Caption.ConfigSettings.Config_ListingPropertiesPaneOnly.Value);
			else
				Log.fail("Test case Failed. Few layots are not as expected on selecting " + Caption.ConfigSettings.Config_ListingPropertiesPaneOnly.Value + ". Refer additional information : " 
						+ unAvailableLayouts, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			if(driver != null){
				try
				{
					driver.get(configURL);

					ConfigurationPage configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item

					configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}

			}
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest54_1_30_6A_1

	/**
	 * 54.1.30.7A.1 : Listing pane only layout will be displayed for hyperlink url on selecting Listing pane only layout in configuration and default in hyperlink dialog - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "Get Hyperlink"}, 
			description = "Listing pane only layout will be displayed for hyperlink url on selecting Listing pane only layout in configuration and default in hyperlink dialog - Context menu.")
	public void SprintTest54_1_30_7A_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {




			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to Vault in tree view
			//---------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item


			if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase())) //Checks if navigated to vault settings page
				throw new Exception("Vault settings page in configuration is not opened.");

			Log.message("1. Navigated to Vault settings page.");

			//Step-2 : Select Listing pane layout layout and save the settings
			//--------------------------------------------------------------------------------------
			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_ListingPaneOnly.Value); //Sets as Listing pane layout

			if (!configurationPage.configurationPanel.getLayout().equalsIgnoreCase(Caption.ConfigSettings.Config_ListingPaneOnly.Value)) //Verifies if Listing pane and properties pane layout is selected
				throw new Exception(Caption.ConfigSettings.Config_ListingPaneOnly.Value + " layout is not selected.");

			if (!configurationPage.configurationPanel.saveSettings()) //Saves the settings
				throw new Exception("Settings are not saved properly.");

			if (!configurationPage.logOut()) //Logs out from configuration page
				throw new Exception("Log out is not successful after saving the settings in configuration page.");

			Log.message("2. " + Caption.ConfigSettings.Config_ListingPaneOnly.Value + " layout is selected and settings are saved.");

			//Step-3 : Login to the default page and navigate to any view
			//-------------------------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches and logs into the default page

			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("3. Logged into the default page and navigated to '" + viewToNavigate + "' view.");

			//Step-4 : Open Get Hyperlink dialog for the object from context menu
			//-------------------------------------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get Hyperlink from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get Hyperlink' title is not opened.");

			Log.message("4. Hyperlink dialog of an object (" + dataPool.get("ObjectName") + ") is opened through context menu.");

			//Step-5 : Copy the link from text box and close the Get Hyperlink dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get Hyperlink dialog

			Log.message("5. Hyperlink is copied and Get Hyperlink dialog is closed.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkText, userName, password, ""); //Navigates to the hyperlink url


			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied object Hyperlink.[Expected URL: "+hyperlinkText+" & Current URL : "+ driver.getCurrentUrl() +"]");

			Log.message("6. Object Hyperlink is opened in the browser.");

			//Verification : Verify if default layout is displayed
			//----------------------------------------------------
			String unAvailableLayouts = "";

			if (homePage.isSearchbarPresent()) //Checks if Search bar present
				unAvailableLayouts = "Search Bar is available;";

			if (homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				unAvailableLayouts = unAvailableLayouts + "Task Pane is available;";

			if (homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value))
				unAvailableLayouts = unAvailableLayouts + "Properties Pane is available;";

			if (homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				unAvailableLayouts = unAvailableLayouts + "Menu Bar is available;";

			if (homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				unAvailableLayouts = unAvailableLayouts + "Breadcrumb is available;";

			if (homePage.taskPanel.isAppletEnabled())
				unAvailableLayouts = unAvailableLayouts + "Java Applet is available;";

			if (!homePage.isListViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "List View is not available;";

			if (homePage.isTreeViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "Tree View is available;";

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page is displayed as ." + Caption.ConfigSettings.Config_ListingPaneOnly.Value);
			else
				Log.fail("Test case Failed. Few layots are not as expected on selecting " + Caption.ConfigSettings.Config_ListingPaneOnly.Value + ". Refer additional information : " 
						+ unAvailableLayouts, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			if(driver != null){
				try
				{
					driver.get(configURL);

					ConfigurationPage configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item

					configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);

					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}

			}
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest54_1_30_7A_1

	/**
	 * 54.1.30.1B.1A : Default layout will be displayed for hyperlink url on selecting default layout in configuration and simple listing in hyperlink dialog - Context menu
	 *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "Get Hyperlink"}, 
			description = "Default layout will be displayed for hyperlink url on selecting default layout in configuration and simple listing in hyperlink dialog - Context menu.")
	public void SprintTest54_1_30_1B_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

  		//Step-1 : Navigate to Vault in tree view
  		//-----------------------------------------
  		configurationPage.treeView.clickTreeViewItem(Caption.Configurationsettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item


  		if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase())) //Checks if navigated to vault settings page
  			throw new Exception("Vault settings page in configuration is not opened.");

  		Log.message("1. Navigated to Vault settings page.");

  		//Step-2 : Select Default layout and save the settings
  		//----------------------------------------------------
  		configurationPage.configurationPanel.setLayout(Caption.Configurationsettings.Config_Default.Value); //Sets as default layout

  		if (!configurationPage.configurationPanel.getLayout().equalsIgnoreCase(Caption.Configurationsettings.Config_Default.Value)) //Verifies if default layout is selected
  			throw new Exception("Default layout is not selected.");

  		if (!configurationPage.configurationPanel.saveSettings()) //Saves the settings
  			throw new Exception("Settings are not saved properly.");

  		if (!configurationPage.logOut()) //Logs out from configuration page
  			throw new Exception("Log out is not successful after saving the settings in configuration page.");

  		Log.message("2. Default layout is selected and settings are saved.");

  		//Step-3 : Login to the default page and navigate to any view
  		//-----------------------------------------------------------
  		HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches and logs into the default page

  		String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

  		Log.message("3. Logged into the default page and navigated to '" + viewToNavigate + "' view.");

  		//Step-4 : Open Get Hyperlink dialog for the object from context menu
			//-------------------------------------------------------------------
  		if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get Hyperlink from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get Hyperlink' title is not opened.");

			Log.message("4. Hyperlink dialog of an object (" + dataPool.get("ObjectName") + ") is opened through context menu.");

			//Step-5 : Select simple listing, Copy the link from text box and close the Get Hyperlink dialog
			//-----------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkLayoutOption(Caption.GetHyperlink.SimpleListing.Value))
				throw new Exception("Simple Listing is not selected in Get Hyperlink dialog.");

			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get Hyperlink dialog

			Log.message("5. Hyperlink is copied and Get Hyperlink dialog is closed.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkText, userName, password, ""); //Navigates to the hyperlink url


			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied object Hyperlink.[Expected URL: "+hyperlinkText+" & Current URL : "+ driver.getCurrentUrl() +"]");

			Log.message("6. Object Hyperlink is opened in the browser.");

			//Verification : Verify if default layout is displayed
  		//----------------------------------------------------
			String unAvailableLayouts = "";

			if (!homePage.isSearchbarPresent()) //Checks if Search bar present
				unAvailableLayouts = "Search Bar";

			if (!homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				unAvailableLayouts = unAvailableLayouts + "Task Pane;";

			if (!homePage.previewPane.isTabExists(Caption.Previewpane.MetadataTab.Value))
				unAvailableLayouts = unAvailableLayouts + "Properties Pane;";

			if (!homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				unAvailableLayouts = unAvailableLayouts + "Menu Bar;";

			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				unAvailableLayouts = unAvailableLayouts + "Breadcrumb;";

			if (!homePage.taskPanel.isAppletEnabled())
				unAvailableLayouts = unAvailableLayouts + "Java Applet;";

			if (!homePage.isListViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "Metadatacard;";

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page is displayed as Default layout.");
			else
				Log.fail("Test case Failed. Few layots are missing on selecting 'Default' custom layout. Missed layots : " 
								+ unAvailableLayouts, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_1_30_1A_1B

	  *//**
	  * 54.1.30.1B.1B : Default layout will be displayed for hyperlink url on selecting default layout in configuration and simple listing in hyperlink dialog - Operations menu
	  *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "Get Hyperlink"}, 
			description = "Default layout will be displayed for hyperlink url on selecting default layout in configuration and simple listing in hyperlink dialog - Operations menu.")
	public void SprintTest54_1_30_1B_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

  		//Step-1 : Navigate to Vault in tree view
  		//-----------------------------------------
  		configurationPage.treeView.clickTreeViewItem(Caption.Configurationsettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item


  		if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase())) //Checks if navigated to vault settings page
  			throw new Exception("Vault settings page in configuration is not opened.");

  		Log.message("1. Navigated to Vault settings page.");

  		//Step-2 : Select Default layout and save the settings
  		//----------------------------------------------------
  		configurationPage.configurationPanel.setLayout(Caption.Configurationsettings.Config_Default.Value); //Sets as default layout

  		if (!configurationPage.configurationPanel.getLayout().equalsIgnoreCase(Caption.Configurationsettings.Config_Default.Value)) //Verifies if default layout is selected
  			throw new Exception("Default layout is not selected.");

  		if (!configurationPage.configurationPanel.saveSettings()) //Saves the settings
  			throw new Exception("Settings are not saved properly.");

  		if (!configurationPage.logOut()) //Logs out from configuration page
  			throw new Exception("Log out is not successful after saving the settings in configuration page.");

  		Log.message("2. Default layout is selected and settings are saved.");

  		//Step-3 : Login to the default page and navigate to any view
  		//--------------------------------------------------------------------------
  		HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches and logs into the default page

  		String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

  		Log.message("3. Logged into the default page and navigated to '" + viewToNavigate + "' view.");

  		//Step-4 : Open Get Hyperlink dialog for the object from operations menu
			//-----------------------------------------------------------------------
  		if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get Hyperlink from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get Hyperlink' title is not opened.");

			Log.message("4. Hyperlink dialog of an object (" + dataPool.get("ObjectName") + ") is opened through operations menu.");

			//Step-5 : Select simple listing, Copy the link from text box and close the Get Hyperlink dialog
			//-----------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkLayoutOption(Caption.GetHyperlink.SimpleListing.Value))
				throw new Exception("Simple Listing is not selected in Get Hyperlink dialog.");

			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get Hyperlink dialog

			Log.message("5. Hyperlink is copied and Get Hyperlink dialog is closed.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkText, userName, password, ""); //Navigates to the hyperlink url


			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied object Hyperlink.[Expected URL: "+hyperlinkText+" & Current URL : "+ driver.getCurrentUrl() +"]");

			Log.message("6. Object Hyperlink is opened in the browser.");

			//Verification : Verify if default layout is displayed
  		//----------------------------------------------------
			String unAvailableLayouts = "";

			if (!homePage.isSearchbarPresent()) //Checks if Search bar present
				unAvailableLayouts = "Search Bar";

			if (!homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				unAvailableLayouts = unAvailableLayouts + "Task Pane;";

			if (!homePage.previewPane.isTabExists(Caption.Previewpane.MetadataTab.Value))
				unAvailableLayouts = unAvailableLayouts + "Properties Pane;";

			if (!homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				unAvailableLayouts = unAvailableLayouts + "Menu Bar;";

			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				unAvailableLayouts = unAvailableLayouts + "Breadcrumb;";

			if (!homePage.taskPanel.isAppletEnabled())
				unAvailableLayouts = unAvailableLayouts + "Java Applet;";

			if (!homePage.isListViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "Metadatacard;";

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page is displayed as Default layout.");
			else
				Log.fail("Test case Failed. Few layots are missing on selecting 'Default' custom layout. Missed layots : " 
								+ unAvailableLayouts, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_1_30_1B_1B

	   *//**
	   * 54.1.30.2B.1A : Default layout with navigation pane will be displayed for hyperlink url on selecting Default layout with navigation pane layout in configuration and simple listing in hyperlink dialog - Context menu
	   *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "Get Hyperlink"}, 
			description = "Default layout with navigation pane will be displayed for hyperlink url on selecting Default layout with navigation pane layout in configuration and simple listing in hyperlink dialog - Context menu.")
	public void SprintTest54_1_30_2B_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

  		//Step-1 : Navigate to Vault in tree view
  		//-----------------------------------------
  		configurationPage.treeView.clickTreeViewItem(Caption.Configurationsettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item


  		if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase())) //Checks if navigated to vault settings page
  			throw new Exception("Vault settings page in configuration is not opened.");

  		Log.message("1. Navigated to Vault settings page.");

  		//Step-2 : Select Default and navigate pane layout and save the settings
  		//--------------------------------------------------------------------
  		configurationPage.configurationPanel.setLayout(Caption.Configurationsettings.Config_DefaultAndNavigation.Value); //Sets as Default and navigate pane layout

  		if (!configurationPage.configurationPanel.getLayout().equalsIgnoreCase(Caption.Configurationsettings.Config_DefaultAndNavigation.Value)) //Verifies if Default and navigate pane layout is selected
  			throw new Exception(Caption.Configurationsettings.Config_DefaultAndNavigation.Value + " layout is not selected.");

  		if (!configurationPage.configurationPanel.saveSettings()) //Saves the settings
  			throw new Exception("Settings are not saved properly.");

  		if (!configurationPage.logOut()) //Logs out from configuration page
  			throw new Exception("Log out is not successful after saving the settings in configuration page.");

  		Log.message("2. " + Caption.Configurationsettings.Config_DefaultAndNavigation.Value + " layout is selected and settings are saved.");

  		//Step-3 : Login to the default page and navigate to any view
  		//--------------------------------------------------------------------------
  		HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches and logs into the default page

  		String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

  		Log.message("3. Logged into the default page and navigated to '" + viewToNavigate + "' view.");

  		//Step-4 : Open Get Hyperlink dialog for the object from context menu
			//-------------------------------------------------------------------
  		if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get Hyperlink from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get Hyperlink' title is not opened.");

			Log.message("4. Hyperlink dialog of an object (" + dataPool.get("ObjectName") + ") is opened through context menu.");

			//Step-5 : Select simple listing, Copy the link from text box and close the Get Hyperlink dialog
			//-----------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkLayoutOption(Caption.GetHyperlink.SimpleListing.Value))
				throw new Exception("Simple Listing is not selected in Get Hyperlink dialog.");

			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get Hyperlink dialog

			Log.message("5. Hyperlink is copied and Get Hyperlink dialog is closed.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkText, userName, password, ""); //Navigates to the hyperlink url


			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied object Hyperlink.[Expected URL: "+hyperlinkText+" & Current URL : "+ driver.getCurrentUrl() +"]");

			Log.message("6. Object Hyperlink is opened in the browser.");

			//Verification : Verify if default layout is displayed
  		//----------------------------------------------------
			String unAvailableLayouts = "";

			if (!homePage.isNavigationPaneDisplayed())
				unAvailableLayouts = unAvailableLayouts + "Navigation pane;";

			if (!homePage.isSearchbarPresent()) //Checks if Search bar present
				unAvailableLayouts = "Search Bar";

			if (!homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				unAvailableLayouts = unAvailableLayouts + "Task Pane;";

			if (!homePage.previewPane.isTabExists(Caption.Previewpane.MetadataTab.Value))
				unAvailableLayouts = unAvailableLayouts + "Properties Pane;";

			if (!homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				unAvailableLayouts = unAvailableLayouts + "Menu Bar;";

			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				unAvailableLayouts = unAvailableLayouts + "Breadcrumb;";

			if (!homePage.taskPanel.isAppletEnabled())
				unAvailableLayouts = unAvailableLayouts + "Java Applet;";

			if (!homePage.isListViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "Metadatacard;";

			if (!homePage.isTreeViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "Tree View;";


			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page is displayed as Default layout with Navigation pane.");
			else
				Log.fail("Test case Failed. Few layots are missing on selecting 'Default layout and navigation pane' layout. Missed layots : " 
								+ unAvailableLayouts, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			driver.get(configURL);

			ConfigurationPage configurationPage = new ConfigurationPage(driver);
			configurationPage.treeView.clickTreeViewItem(Caption.Configurationsettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item

			configurationPage.configurationPanel.setLayout(Caption.Configurationsettings.Config_Default.Value);

			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest54_1_30_2B_1A

	    *//**
	    * 54.1.30.2B.1B : Default layout with navigation pane will be displayed for hyperlink url on selecting Default layout with navigation pane layout in configuration and simple listing in hyperlink dialog - Operations menu
	    *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "Get Hyperlink"}, 
			description = "Default layout with navigation pane will be displayed for hyperlink url on selecting Default layout with navigation pane layout in configuration and simple listing in hyperlink dialog - Operations menu.")
	public void SprintTest54_1_30_2B_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

  		//Step-1 : Navigate to Vault in tree view
  		//-----------------------------------------
  		configurationPage.treeView.clickTreeViewItem(Caption.Configurationsettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item


  		if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase())) //Checks if navigated to vault settings page
  			throw new Exception("Vault settings page in configuration is not opened.");

  		Log.message("1. Navigated to Vault settings page.");

  		//Step-2 : Select Default and navigate pane layout and save the settings
  		//--------------------------------------------------------------------
  		configurationPage.configurationPanel.setLayout(Caption.Configurationsettings.Config_DefaultAndNavigation.Value); //Sets as default layout

  		if (!configurationPage.configurationPanel.getLayout().equalsIgnoreCase(Caption.Configurationsettings.Config_DefaultAndNavigation.Value)) //Verifies if default layout is selected
  			throw new Exception(Caption.Configurationsettings.Config_DefaultAndNavigation.Value + " layout is not selected.");

  		if (!configurationPage.configurationPanel.saveSettings()) //Saves the settings
  			throw new Exception("Settings are not saved properly.");

  		if (!configurationPage.logOut()) //Logs out from configuration page
  			throw new Exception("Log out is not successful after saving the settings in configuration page.");

  		Log.message("2. " + Caption.Configurationsettings.Config_DefaultAndNavigation.Value + " layout is selected and settings are saved.");

  		//Step-3 : Login to the default page and navigate to any view
  		//--------------------------------------------------------------------------
  		HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches and logs into the default page

  		String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

  		Log.message("3. Logged into the default page and navigated to '" + viewToNavigate + "' view.");

  		//Step-4 : Open Get Hyperlink dialog for the object from operations menu
			//-----------------------------------------------------------------------
  		if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get Hyperlink from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get Hyperlink' title is not opened.");

			Log.message("4. Hyperlink dialog of an object (" + dataPool.get("ObjectName") + ") is opened through operations menu.");

			//Step-5 : Select simple listing, Copy the link from text box and close the Get Hyperlink dialog
			//-----------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkLayoutOption(Caption.GetHyperlink.SimpleListing.Value))
				throw new Exception("Simple Listing is not selected in Get Hyperlink dialog.");

			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get Hyperlink dialog

			Log.message("5. Hyperlink is copied and Get Hyperlink dialog is closed.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkText, userName, password, ""); //Navigates to the hyperlink url


			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied object Hyperlink.[Expected URL: "+hyperlinkText+" & Current URL : "+ driver.getCurrentUrl() +"]");

			Log.message("6. Object Hyperlink is opened in the browser.");

			//Verification : Verify if default layout is displayed
  		//----------------------------------------------------
			String unAvailableLayouts = "";

			if (!homePage.isSearchbarPresent()) //Checks if Search bar present
				unAvailableLayouts = "Search Bar";

			if (!homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				unAvailableLayouts = unAvailableLayouts + "Task Pane;";

			if (!homePage.previewPane.isTabExists(Caption.Previewpane.MetadataTab.Value))
				unAvailableLayouts = unAvailableLayouts + "Properties Pane;";

			if (!homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				unAvailableLayouts = unAvailableLayouts + "Menu Bar;";

			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				unAvailableLayouts = unAvailableLayouts + "Breadcrumb;";

			if (!homePage.taskPanel.isAppletEnabled())
				unAvailableLayouts = unAvailableLayouts + "Java Applet;";

			if (!homePage.isListViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "Metadatacard;";

			if (!homePage.isTreeViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "Tree View;";

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page is displayed as Default layout with Navigation pane.");
			else
				Log.fail("Test case Failed. Few layots are missing on selecting 'Default layout and navigation pane' layout. Missed layots : " 
								+ unAvailableLayouts, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			driver.get(configURL);

			ConfigurationPage configurationPage = new ConfigurationPage(driver);
			configurationPage.treeView.clickTreeViewItem(Caption.Configurationsettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item

			configurationPage.configurationPanel.setLayout(Caption.Configurationsettings.Config_Default.Value);

			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest54_1_30_2B_1B

	     *//**
	     * 54.1.30.3B.1A : No Java applet layout will be displayed for hyperlink url on selecting No Java applet in configuration and simple listing in hyperlink dialog - Context menu
	     *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "Get Hyperlink"}, 
			description = "No Java applet layout will be displayed for hyperlink url on selecting No Java applet in configuration and simple listing in hyperlink dialog - Context menu.")
	public void SprintTest54_1_30_3B_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

  		//Step-1 : Navigate to Vault in tree view
  		//-----------------------------------------
  		configurationPage.treeView.clickTreeViewItem(Caption.Configurationsettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item


  		if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase())) //Checks if navigated to vault settings page
  			throw new Exception("Vault settings page in configuration is not opened.");

  		Log.message("1. Navigated to Vault settings page.");

  		//Step-2 : Select No Java applet layout layout and save the settings
  		//--------------------------------------------------------------------
  		configurationPage.configurationPanel.setLayout(Caption.Configurationsettings.Config_NoJavaApplet.Value); //Sets as default layout

  		if (!configurationPage.configurationPanel.getLayout().equalsIgnoreCase(Caption.Configurationsettings.Config_NoJavaApplet.Value)) //Verifies if default layout is selected
  			throw new Exception(Caption.Configurationsettings.Config_NoJavaApplet.Value + " layout is not selected.");

  		if (!configurationPage.configurationPanel.saveSettings()) //Saves the settings
  			throw new Exception("Settings are not saved properly.");

  		if (!configurationPage.logOut()) //Logs out from configuration page
  			throw new Exception("Log out is not successful after saving the settings in configuration page.");

  		Log.message("2. " + Caption.Configurationsettings.Config_NoJavaApplet.Value + " layout is selected and settings are saved.");

  		//Step-3 : Login to the default page and navigate to any view
  		//--------------------------------------------------------------------------
  		HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches and logs into the default page

  		String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

  		Log.message("3. Logged into the default page and navigated to '" + viewToNavigate + "' view.");

  		//Step-4 : Open Get Hyperlink dialog for the object from context menu
			//-------------------------------------------------------------------
  		if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get Hyperlink from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get Hyperlink' title is not opened.");

			Log.message("4. Hyperlink dialog of an object (" + dataPool.get("ObjectName") + ") is opened through context menu.");

			//Step-5 : Select simple listing, Copy the link from text box and close the Get Hyperlink dialog
			//-----------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkLayoutOption(Caption.GetHyperlink.SimpleListing.Value))
				throw new Exception("Simple Listing is not selected in Get Hyperlink dialog.");

			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get Hyperlink dialog

			Log.message("5. Hyperlink is copied and Get Hyperlink dialog is closed.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkText, userName, password, ""); //Navigates to the hyperlink url


			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied object Hyperlink.[Expected URL: "+hyperlinkText+" & Current URL : "+ driver.getCurrentUrl() +"]");

			Log.message("6. Object Hyperlink is opened in the browser.");

			//Verification : Verify if default layout is displayed
  		//----------------------------------------------------
			String unAvailableLayouts = "";

			if (!homePage.isSearchbarPresent()) //Checks if Search bar present
				unAvailableLayouts = "Search Bar is not available;";

			if (!homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				unAvailableLayouts = unAvailableLayouts + "Task Pane is not available;";

			if (!homePage.previewPane.isTabExists(Caption.Previewpane.MetadataTab.Value))
				unAvailableLayouts = unAvailableLayouts + "Properties Pane is not available;";

			if (!homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				unAvailableLayouts = unAvailableLayouts + "Menu Bar is not available;";

			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				unAvailableLayouts = unAvailableLayouts + "Breadcrumb is not available;";

			if (homePage.taskPanel.isAppletEnabled())
				unAvailableLayouts = unAvailableLayouts + "Java Applet is available;";

			if (!homePage.isListViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "List View is not available;";

			if (homePage.isTreeViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "Tree View is available;";

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page is displayed as No Java Applet layout.");
			else
				Log.fail("Test case Failed. Few layots are not as expected on selecting 'No Java Applet layout'. Refer additional information : " 
								+ unAvailableLayouts, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			driver.get(configURL);

			ConfigurationPage configurationPage = new ConfigurationPage(driver);
			configurationPage.treeView.clickTreeViewItem(Caption.Configurationsettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item

			configurationPage.configurationPanel.setLayout(Caption.Configurationsettings.Config_Default.Value);

			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest54_1_30_3B_1A

	      *//**
	      * 54.1.30.3B.1B : No Java applet layout will be displayed for hyperlink url on selecting No Java applet in configuration and simple listing in hyperlink dialog - Operations menu
	      *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "Get Hyperlink"}, 
			description = "No Java applet layout will be displayed for hyperlink url on selecting No Java applet in configuration and simple listing in hyperlink dialog - Operations menu.")
	public void SprintTest54_1_30_3B_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

  		//Step-1 : Navigate to Vault in tree view
  		//-----------------------------------------
  		configurationPage.treeView.clickTreeViewItem(Caption.Configurationsettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item


  		if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase())) //Checks if navigated to vault settings page
  			throw new Exception("Vault settings page in configuration is not opened.");

  		Log.message("1. Navigated to Vault settings page.");

  		//Step-2 : Select No Java applet layout layout and save the settings
  		//--------------------------------------------------------------------
  		configurationPage.configurationPanel.setLayout(Caption.Configurationsettings.Config_NoJavaApplet.Value); //Sets as default layout

  		if (!configurationPage.configurationPanel.getLayout().equalsIgnoreCase(Caption.Configurationsettings.Config_NoJavaApplet.Value)) //Verifies if default layout is selected
  			throw new Exception(Caption.Configurationsettings.Config_NoJavaApplet.Value + " layout is not selected.");

  		if (!configurationPage.configurationPanel.saveSettings()) //Saves the settings
  			throw new Exception("Settings are not saved properly.");

  		if (!configurationPage.logOut()) //Logs out from configuration page
  			throw new Exception("Log out is not successful after saving the settings in configuration page.");

  		Log.message("2. " + Caption.Configurationsettings.Config_NoJavaApplet.Value + " layout is selected and settings are saved.");

  		//Step-3 : Login to the default page and navigate to any view
  		//--------------------------------------------------------------------------
  		HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches and logs into the default page

  		String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

  		Log.message("3. Logged into the default page and navigated to '" + viewToNavigate + "' view.");

  		//Step-4 : Open Get Hyperlink dialog for the object from operations menu
			//-----------------------------------------------------------------------
  		if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get Hyperlink from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get Hyperlink' title is not opened.");

			Log.message("4. Hyperlink dialog of an object (" + dataPool.get("ObjectName") + ") is opened through operations menu.");

			//Step-5 : Select simple listing, Copy the link from text box and close the Get Hyperlink dialog
			//-----------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkLayoutOption(Caption.GetHyperlink.SimpleListing.Value))
				throw new Exception("Simple Listing is not selected in Get Hyperlink dialog.");

			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get Hyperlink dialog

			Log.message("5. Hyperlink is copied and Get Hyperlink dialog is closed.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkText, userName, password, ""); //Navigates to the hyperlink url


			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied object Hyperlink.[Expected URL: "+hyperlinkText+" & Current URL : "+ driver.getCurrentUrl() +"]");

			Log.message("6. Object Hyperlink is opened in the browser.");

			//Verification : Verify if default layout is displayed
  		//----------------------------------------------------
			String unAvailableLayouts = "";

			if (!homePage.isSearchbarPresent()) //Checks if Search bar present
				unAvailableLayouts = "Search Bar is not available;";

			if (!homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				unAvailableLayouts = unAvailableLayouts + "Task Pane is not available;";

			if (!homePage.previewPane.isTabExists(Caption.Previewpane.MetadataTab.Value))
				unAvailableLayouts = unAvailableLayouts + "Properties Pane is not available;";

			if (!homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				unAvailableLayouts = unAvailableLayouts + "Menu Bar is not available;";

			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				unAvailableLayouts = unAvailableLayouts + "Breadcrumb is not available;";

			if (homePage.taskPanel.isAppletEnabled())
				unAvailableLayouts = unAvailableLayouts + "Java Applet is available;";

			if (!homePage.isListViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "List View is not available;";

			if (homePage.isTreeViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "Tree View is available;";

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page is displayed as No Java Applet layout.");
			else
				Log.fail("Test case Failed. Few layots are not as expected on selecting 'No Java Applet layout'. Refer additional information : " 
								+ unAvailableLayouts, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			driver.get(configURL);

			ConfigurationPage configurationPage = new ConfigurationPage(driver);
			configurationPage.treeView.clickTreeViewItem(Caption.Configurationsettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item

			configurationPage.configurationPanel.setLayout(Caption.Configurationsettings.Config_Default.Value);

			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest54_1_30_3B_1B

	       *//**
	       * 54.1.30.4B.1A : No Java applet and no task area layout will be displayed for hyperlink url on selecting No Java applet and no task area layout in configuration and simple listing in hyperlink dialog - Context menu
	       *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "Get Hyperlink"}, 
			description = "No Java applet and no task area layout will be displayed for hyperlink url on selecting No Java applet and no task area layout in configuration and simple listing in hyperlink dialog - Context menu.")
	public void SprintTest54_1_30_4B_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

  		//Step-1 : Navigate to Vault in tree view
  		//---------------------------------------
  		configurationPage.treeView.clickTreeViewItem(Caption.Configurationsettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item


  		if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase())) //Checks if navigated to vault settings page
  			throw new Exception("Vault settings page in configuration is not opened.");

  		Log.message("1. Navigated to Vault settings page.");

  		//Step-2 : Select No Java applet and No task area layout layout and save the settings
  		//--------------------------------------------------------------------
  		configurationPage.configurationPanel.setLayout(Caption.Configurationsettings.Config_NoJavaAppletTaskArea.Value); //Sets as default layout

  		if (!configurationPage.configurationPanel.getLayout().equalsIgnoreCase(Caption.Configurationsettings.Config_NoJavaAppletTaskArea.Value)) //Verifies if default layout is selected
  			throw new Exception(Caption.Configurationsettings.Config_NoJavaAppletTaskArea.Value + " layout is not selected.");

  		if (!configurationPage.configurationPanel.saveSettings()) //Saves the settings
  			throw new Exception("Settings are not saved properly.");

  		if (!configurationPage.logOut()) //Logs out from configuration page
  			throw new Exception("Log out is not successful after saving the settings in configuration page.");

  		Log.message("2. " + Caption.Configurationsettings.Config_NoJavaAppletTaskArea.Value + " layout is selected and settings are saved.");

  		//Step-3 : Login to the default page and navigate to any view
  		//--------------------------------------------------------------------------
  		HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches and logs into the default page

  		String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

  		Log.message("3. Logged into the default page and navigated to '" + viewToNavigate + "' view.");

  		//Step-4 : Open Get Hyperlink dialog for the object from context menu
			//-------------------------------------------------------------------
  		if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get Hyperlink from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get Hyperlink' title is not opened.");

			Log.message("4. Hyperlink dialog of an object (" + dataPool.get("ObjectName") + ") is opened through context menu.");

			//Step-5 : Select simple listing, Copy the link from text box and close the Get Hyperlink dialog
			//-----------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkLayoutOption(Caption.GetHyperlink.SimpleListing.Value))
				throw new Exception("Simple Listing is not selected in Get Hyperlink dialog.");

			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get Hyperlink dialog

			Log.message("5. Hyperlink is copied and Get Hyperlink dialog is closed.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkText, userName, password, ""); //Navigates to the hyperlink url


			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied object Hyperlink.[Expected URL: "+hyperlinkText+" & Current URL : "+ driver.getCurrentUrl() +"]");

			Log.message("6. Object Hyperlink is opened in the browser.");

			//Verification : Verify if default layout is displayed
  		//----------------------------------------------------
			String unAvailableLayouts = "";

			if (!homePage.isSearchbarPresent()) //Checks if Search bar present
				unAvailableLayouts = "Search Bar is not available;";

			if (homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				unAvailableLayouts = unAvailableLayouts + "Task Pane is available;";

			if (!homePage.previewPane.isTabExists(Caption.Previewpane.MetadataTab.Value))
				unAvailableLayouts = unAvailableLayouts + "Properties Pane is not available;";

			if (!homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				unAvailableLayouts = unAvailableLayouts + "Menu Bar is not available;";

			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				unAvailableLayouts = unAvailableLayouts + "Breadcrumb is not available;";

			if (homePage.taskPanel.isAppletEnabled())
				unAvailableLayouts = unAvailableLayouts + "Java Applet is available;";

			if (!homePage.isListViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "List View is not available;";

			if (homePage.isTreeViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "Tree View is available;";

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page is displayed as ." + Caption.Configurationsettings.Config_NoJavaAppletTaskArea.Value);
			else
				Log.fail("Test case Failed. Few layots are not as expected on selecting " + Caption.Configurationsettings.Config_NoJavaAppletTaskArea.Value + ". Refer additional information : " 
								+ unAvailableLayouts, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			driver.get(configURL);

			ConfigurationPage configurationPage = new ConfigurationPage(driver);
			configurationPage.treeView.clickTreeViewItem(Caption.Configurationsettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item

			configurationPage.configurationPanel.setLayout(Caption.Configurationsettings.Config_Default.Value);

			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest54_1_30_4B_1A

	        *//**
	        * 54.1.30.4B.1B : No Java applet and no task area layout will be displayed for hyperlink url on selecting No Java applet and no task area layout in configuration and simple listing in hyperlink dialog - Operations menu
	        *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "Get Hyperlink"}, 
			description = "No Java applet and no task area layout will be displayed for hyperlink url on selecting No Java applet and no task area layout in configuration and simple listing in hyperlink dialog - Operations menu.")
	public void SprintTest54_1_30_4B_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

  		//Step-1 : Navigate to Vault in tree view
  		//---------------------------------------
  		configurationPage.treeView.clickTreeViewItem(Caption.Configurationsettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item


  		if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase())) //Checks if navigated to vault settings page
  			throw new Exception("Vault settings page in configuration is not opened.");

  		Log.message("1. Navigated to Vault settings page.");

  		//Step-2 : Select No Java applet and No task area layout layout and save the settings
  		//--------------------------------------------------------------------
  		configurationPage.configurationPanel.setLayout(Caption.Configurationsettings.Config_NoJavaAppletTaskArea.Value); //Sets as default layout

  		if (!configurationPage.configurationPanel.getLayout().equalsIgnoreCase(Caption.Configurationsettings.Config_NoJavaAppletTaskArea.Value)) //Verifies if default layout is selected
  			throw new Exception(Caption.Configurationsettings.Config_NoJavaAppletTaskArea.Value + " layout is not selected.");

  		if (!configurationPage.configurationPanel.saveSettings()) //Saves the settings
  			throw new Exception("Settings are not saved properly.");

  		if (!configurationPage.logOut()) //Logs out from configuration page
  			throw new Exception("Log out is not successful after saving the settings in configuration page.");

  		Log.message("2. " + Caption.Configurationsettings.Config_NoJavaAppletTaskArea.Value + " layout is selected and settings are saved.");

  		//Step-3 : Login to the default page and navigate to any view
  		//--------------------------------------------------------------------------
  		HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches and logs into the default page

  		String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

  		Log.message("3. Logged into the default page and navigated to '" + viewToNavigate + "' view.");

  		//Step-4 : Open Get Hyperlink dialog for the object from operations menu
			//-----------------------------------------------------------------------
  		if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get Hyperlink from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get Hyperlink' title is not opened.");

			Log.message("4. Hyperlink dialog of an object (" + dataPool.get("ObjectName") + ") is opened through operations menu.");

			//Step-5 : Select simple listing, Copy the link from text box and close the Get Hyperlink dialog
			//-----------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkLayoutOption(Caption.GetHyperlink.SimpleListing.Value))
				throw new Exception("Simple Listing is not selected in Get Hyperlink dialog.");

			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get Hyperlink dialog

			Log.message("5. Hyperlink is copied and Get Hyperlink dialog is closed.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkText, userName, password, ""); //Navigates to the hyperlink url


			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied object Hyperlink.[Expected URL: "+hyperlinkText+" & Current URL : "+ driver.getCurrentUrl() +"]");

			Log.message("6. Object Hyperlink is opened in the browser.");

			//Verification : Verify if default layout is displayed
  		//----------------------------------------------------
			String unAvailableLayouts = "";

			if (!homePage.isSearchbarPresent()) //Checks if Search bar present
				unAvailableLayouts = "Search Bar is not available;";

			if (homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				unAvailableLayouts = unAvailableLayouts + "Task Pane is available;";

			if (!homePage.previewPane.isTabExists(Caption.Previewpane.MetadataTab.Value))
				unAvailableLayouts = unAvailableLayouts + "Properties Pane is not available;";

			if (!homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				unAvailableLayouts = unAvailableLayouts + "Menu Bar is not available;";

			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				unAvailableLayouts = unAvailableLayouts + "Breadcrumb is not available;";

			if (homePage.taskPanel.isAppletEnabled())
				unAvailableLayouts = unAvailableLayouts + "Java Applet is available;";

			if (!homePage.isListViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "List View is not available;";

			if (homePage.isTreeViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "Tree View is available;";

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page is displayed as ." + Caption.Configurationsettings.Config_NoJavaAppletTaskArea.Value);
			else
				Log.fail("Test case Failed. Few layots are not as expected on selecting " + Caption.Configurationsettings.Config_NoJavaAppletTaskArea.Value + ". Refer additional information : " 
								+ unAvailableLayouts, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			driver.get(configURL);

			ConfigurationPage configurationPage = new ConfigurationPage(driver);
			configurationPage.treeView.clickTreeViewItem(Caption.Configurationsettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item

			configurationPage.configurationPanel.setLayout(Caption.Configurationsettings.Config_Default.Value);

			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest54_1_30_4B_1B

	         *//**
	         * 54.1.30.5B.1A : No Java applet and no task area (but show 'Go To' shortcuts) layout will be displayed for hyperlink url on selecting No Java applet and no task area (but show 'Go To' shortcuts) layout in configuration and simple listing in hyperlink dialog - Context menu
	         *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "Get Hyperlink"}, 
			description = "No Java applet and no task area (but show 'Go To' shortcuts) layout will be displayed for hyperlink url on selecting No Java applet and no task area (but show 'Go To' shortcuts) layout in configuration and simple listing in hyperlink dialog - Context menu.")
	public void SprintTest54_1_30_5B_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

  		//Step-1 : Navigate to Vault in tree view
  		//---------------------------------------
  		configurationPage.treeView.clickTreeViewItem(Caption.Configurationsettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item


  		if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase())) //Checks if navigated to vault settings page
  			throw new Exception("Vault settings page in configuration is not opened.");

  		Log.message("1. Navigated to Vault settings page.");

  		//Step-2 : Select No Java applet and No task area but show Go to shortcuts layout layout and save the settings
  		//------------------------------------------------------------------------------------------------------------
  		configurationPage.configurationPanel.setLayout(Caption.Configurationsettings.Config_NoJavaAppletTaskAreaShowGoTo.Value); //Sets as default layout

  		if (!configurationPage.configurationPanel.getLayout().equalsIgnoreCase(Caption.Configurationsettings.Config_NoJavaAppletTaskAreaShowGoTo.Value)) //Verifies if default layout is selected
  			throw new Exception(Caption.Configurationsettings.Config_NoJavaAppletTaskAreaShowGoTo.Value + " layout is not selected.");

  		if (!configurationPage.configurationPanel.saveSettings()) //Saves the settings
  			throw new Exception("Settings are not saved properly.");

  		if (!configurationPage.logOut()) //Logs out from configuration page
  			throw new Exception("Log out is not successful after saving the settings in configuration page.");

  		Log.message("2. " + Caption.Configurationsettings.Config_NoJavaAppletTaskAreaShowGoTo.Value + " layout is selected and settings are saved.");

  		//Step-3 : Login to the default page and navigate to any view
  		//-------------------------------------------------------------
  		HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches and logs into the default page

  		String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

  		Log.message("3. Logged into the default page and navigated to '" + viewToNavigate + "' view.");

  		//Step-4 : Open Get Hyperlink dialog for the object from context menu
			//-------------------------------------------------------------------
  		if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get Hyperlink from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get Hyperlink' title is not opened.");

			Log.message("4. Hyperlink dialog of an object (" + dataPool.get("ObjectName") + ") is opened through context menu.");

			//Step-5 : Select simple listing, Copy the link from text box and close the Get Hyperlink dialog
			//-----------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkLayoutOption(Caption.GetHyperlink.SimpleListing.Value))
				throw new Exception("Simple Listing is not selected in Get Hyperlink dialog.");

			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get Hyperlink dialog

			Log.message("5. Hyperlink is copied and Get Hyperlink dialog is closed.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkText, userName, password, ""); //Navigates to the hyperlink url


			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied object Hyperlink.[Expected URL: "+hyperlinkText+" & Current URL : "+ driver.getCurrentUrl() +"]");

			Log.message("6. Object Hyperlink is opened in the browser.");

			//Verification : Verify if default layout is displayed
  		//----------------------------------------------------
			String unAvailableLayouts = "";

			if (!homePage.isSearchbarPresent()) //Checks if Search bar present
				unAvailableLayouts = "Search Bar is not available;";

			if (homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				unAvailableLayouts = unAvailableLayouts + "Task Pane is available;";

			if (!homePage.taskPanel.isTaskPaneGoToDisplayed()) //Checks if Task Pane GoTo present
				unAvailableLayouts = unAvailableLayouts + "Task Pane GoTo is not available;";

			if (!homePage.previewPane.isTabExists(Caption.Previewpane.MetadataTab.Value))
				unAvailableLayouts = unAvailableLayouts + "Properties Pane is not available;";

			if (!homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				unAvailableLayouts = unAvailableLayouts + "Menu Bar is not available;";

			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				unAvailableLayouts = unAvailableLayouts + "Breadcrumb is not available;";

			if (homePage.taskPanel.isAppletEnabled())
				unAvailableLayouts = unAvailableLayouts + "Java Applet is available;";

			if (!homePage.isListViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "List View is not available;";

			if (homePage.isTreeViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "Tree View is available;";

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page is displayed as ." + Caption.Configurationsettings.Config_NoJavaAppletTaskAreaShowGoTo.Value);
			else
				Log.fail("Test case Failed. Few layots are not as expected on selecting " + Caption.Configurationsettings.Config_NoJavaAppletTaskAreaShowGoTo.Value + ". Refer additional information : " 
								+ unAvailableLayouts, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			driver.get(configURL);

			ConfigurationPage configurationPage = new ConfigurationPage(driver);
			configurationPage.treeView.clickTreeViewItem(Caption.Configurationsettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item

			configurationPage.configurationPanel.setLayout(Caption.Configurationsettings.Config_Default.Value);

			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest54_1_30_5B_1A

	          *//**
	          * 54.1.30.5B.1B : No Java applet and no task area (but show 'Go To' shortcuts) layout will be displayed for hyperlink url on selecting No Java applet and no task area (but show 'Go To' shortcuts) layout in configuration and simple listing in hyperlink dialog - Operations menu
	          *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "Get Hyperlink"}, 
			description = "No Java applet and no task area (but show 'Go To' shortcuts) layout will be displayed for hyperlink url on selecting No Java applet and no task area (but show 'Go To' shortcuts) layout in configuration and simple listing in hyperlink dialog - Operations menu.")
	public void SprintTest54_1_30_5B_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

  		//Step-1 : Navigate to Vault in tree view
  		//---------------------------------------
  		configurationPage.treeView.clickTreeViewItem(Caption.Configurationsettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item


  		if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase())) //Checks if navigated to vault settings page
  			throw new Exception("Vault settings page in configuration is not opened.");

  		Log.message("1. Navigated to Vault settings page.");

  		//Step-2 : Select No Java applet and No task area but show Go to shortcuts layout layout and save the settings
  		//------------------------------------------------------------------------------------------------------------
  		configurationPage.configurationPanel.setLayout(Caption.Configurationsettings.Config_NoJavaAppletTaskAreaShowGoTo.Value); //Sets as default layout

  		if (!configurationPage.configurationPanel.getLayout().equalsIgnoreCase(Caption.Configurationsettings.Config_NoJavaAppletTaskAreaShowGoTo.Value)) //Verifies if default layout is selected
  			throw new Exception(Caption.Configurationsettings.Config_NoJavaAppletTaskAreaShowGoTo.Value + " layout is not selected.");

  		if (!configurationPage.configurationPanel.saveSettings()) //Saves the settings
  			throw new Exception("Settings are not saved properly.");

  		if (!configurationPage.logOut()) //Logs out from configuration page
  			throw new Exception("Log out is not successful after saving the settings in configuration page.");

  		Log.message("2. " + Caption.Configurationsettings.Config_NoJavaAppletTaskAreaShowGoTo.Value + " layout is selected and settings are saved.");

  		//Step-3 : Login to the default page and navigate to any view
  		//-------------------------------------------------------------
  		HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches and logs into the default page

  		String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

  		Log.message("3. Logged into the default page and navigated to '" + viewToNavigate + "' view.");

  		//Step-4 : Open Get Hyperlink dialog for the object from operations menu
			//-----------------------------------------------------------------------
  		if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get Hyperlink from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get Hyperlink' title is not opened.");

			Log.message("4. Hyperlink dialog of an object (" + dataPool.get("ObjectName") + ") is opened through operations menu.");

			//Step-5 : Select simple listing, Copy the link from text box and close the Get Hyperlink dialog
			//-----------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkLayoutOption(Caption.GetHyperlink.SimpleListing.Value))
				throw new Exception("Simple Listing is not selected in Get Hyperlink dialog.");

			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get Hyperlink dialog

			Log.message("5. Hyperlink is copied and Get Hyperlink dialog is closed.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkText, userName, password, ""); //Navigates to the hyperlink url


			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied object Hyperlink.[Expected URL: "+hyperlinkText+" & Current URL : "+ driver.getCurrentUrl() +"]");

			Log.message("6. Object Hyperlink is opened in the browser.");

			//Verification : Verify if default layout is displayed
  		//----------------------------------------------------
			String unAvailableLayouts = "";

			if (!homePage.isSearchbarPresent()) //Checks if Search bar present
				unAvailableLayouts = "Search Bar is not available;";

			if (homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				unAvailableLayouts = unAvailableLayouts + "Task Pane is available;";

			if (!homePage.taskPanel.isTaskPaneGoToDisplayed()) //Checks if Task Pane GoTo present
				unAvailableLayouts = unAvailableLayouts + "Task Pane GoTo is not available;";

			if (!homePage.previewPane.isTabExists(Caption.Previewpane.MetadataTab.Value))
				unAvailableLayouts = unAvailableLayouts + "Properties Pane is not available;";

			if (!homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				unAvailableLayouts = unAvailableLayouts + "Menu Bar is not available;";

			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				unAvailableLayouts = unAvailableLayouts + "Breadcrumb is not available;";

			if (homePage.taskPanel.isAppletEnabled())
				unAvailableLayouts = unAvailableLayouts + "Java Applet is available;";

			if (!homePage.isListViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "List View is not available;";

			if (homePage.isTreeViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "Tree View is available;";

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page is displayed as ." + Caption.Configurationsettings.Config_NoJavaAppletTaskAreaShowGoTo.Value);
			else
				Log.fail("Test case Failed. Few layots are not as expected on selecting " + Caption.Configurationsettings.Config_NoJavaAppletTaskAreaShowGoTo.Value + ". Refer additional information : " 
								+ unAvailableLayouts, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			driver.get(configURL);

			ConfigurationPage configurationPage = new ConfigurationPage(driver);
			configurationPage.treeView.clickTreeViewItem(Caption.Configurationsettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item

			configurationPage.configurationPanel.setLayout(Caption.Configurationsettings.Config_Default.Value);

			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest54_1_30_5B_1B

	           *//**
	           * 54.1.30.6B.1 : Listing pane and properties pane only layout will be displayed for hyperlink url on selecting Listing pane and properties pane only layout in configuration and simple listing in hyperlink dialog - Context menu
	           *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "Get Hyperlink"}, 
			description = "Listing pane and properties pane only layout will be displayed for hyperlink url on selecting Listing pane and properties pane only layout in configuration and simple listing in hyperlink dialog - Context menu.")
	public void SprintTest54_1_30_6B_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

  		//Step-1 : Navigate to Vault in tree view
  		//---------------------------------------
  		configurationPage.treeView.clickTreeViewItem(Caption.Configurationsettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item


  		if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase())) //Checks if navigated to vault settings page
  			throw new Exception("Vault settings page in configuration is not opened.");

  		Log.message("1. Navigated to Vault settings page.");

  		//Step-2 : Select Listing pane and properties pane layout layout and save the settings
  		//------------------------------------------------------------------------------------------------------------
  		configurationPage.configurationPanel.setLayout(Caption.Configurationsettings.Config_ListingPropertiesPaneOnly.Value); //Sets as Listing pane and properties pane layout

  		if (!configurationPage.configurationPanel.getLayout().equalsIgnoreCase(Caption.Configurationsettings.Config_ListingPropertiesPaneOnly.Value)) //Verifies if Listing pane and properties pane layout is selected
  			throw new Exception(Caption.Configurationsettings.Config_ListingPropertiesPaneOnly.Value + " layout is not selected.");

  		if (!configurationPage.configurationPanel.saveSettings()) //Saves the settings
  			throw new Exception("Settings are not saved properly.");

  		if (!configurationPage.logOut()) //Logs out from configuration page
  			throw new Exception("Log out is not successful after saving the settings in configuration page.");

  		Log.message("2. " + Caption.Configurationsettings.Config_ListingPropertiesPaneOnly.Value + " layout is selected and settings are saved.");

  		//Step-3 : Login to the default page and navigate to any view
  		//-------------------------------------------------------------
  		HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches and logs into the default page

  		String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

  		Log.message("3. Logged into the default page and navigated to '" + viewToNavigate + "' view.");

  		//Step-4 : Open Get Hyperlink dialog for the object from context menu
			//-------------------------------------------------------------------
  		if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get Hyperlink from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get Hyperlink' title is not opened.");

			Log.message("4. Hyperlink dialog of an object (" + dataPool.get("ObjectName") + ") is opened through context menu.");

			//Step-5 : Select simple listing, Copy the link from text box and close the Get Hyperlink dialog
			//-----------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkLayoutOption(Caption.GetHyperlink.SimpleListing.Value))
				throw new Exception("Simple Listing is not selected in Get Hyperlink dialog.");

			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get Hyperlink dialog

			Log.message("5. Hyperlink is copied and Get Hyperlink dialog is closed.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkText, userName, password, ""); //Navigates to the hyperlink url


			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied object Hyperlink.[Expected URL: "+hyperlinkText+" & Current URL : "+ driver.getCurrentUrl() +"]");

			Log.message("6. Object Hyperlink is opened in the browser.");

			//Verification : Verify if default layout is displayed
  		//----------------------------------------------------
			String unAvailableLayouts = "";

			if (homePage.isSearchbarPresent()) //Checks if Search bar present
				unAvailableLayouts = "Search Bar is available;";

			if (homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				unAvailableLayouts = unAvailableLayouts + "Task Pane is available;";

			if (!homePage.previewPane.isTabExists(Caption.Previewpane.MetadataTab.Value))
				unAvailableLayouts = unAvailableLayouts + "Properties Pane is not available;";

			if (homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				unAvailableLayouts = unAvailableLayouts + "Menu Bar is available;";

			if (homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				unAvailableLayouts = unAvailableLayouts + "Breadcrumb is available;";

			if (homePage.taskPanel.isAppletEnabled())
				unAvailableLayouts = unAvailableLayouts + "Java Applet is available;";

			if (!homePage.isListViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "List View is not available;";

			if (homePage.isTreeViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "Tree View is available;";

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page is displayed as ." + Caption.Configurationsettings.Config_ListingPropertiesPaneOnly.Value);
			else
				Log.fail("Test case Failed. Few layots are not as expected on selecting " + Caption.Configurationsettings.Config_ListingPropertiesPaneOnly.Value + ". Refer additional information : " 
								+ unAvailableLayouts, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			driver.get(configURL);

			ConfigurationPage configurationPage = new ConfigurationPage(driver);
			configurationPage.treeView.clickTreeViewItem(Caption.Configurationsettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item

			configurationPage.configurationPanel.setLayout(Caption.Configurationsettings.Config_Default.Value);

			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest54_1_30_6B_1

	            *//**
	            * 54.1.30.7B.1 : Listing pane only layout will be displayed for hyperlink url on selecting Listing pane only layout in configuration and simple listing in hyperlink dialog - Context menu
	            *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "Get Hyperlink"}, 
			description = "Listing pane only layout will be displayed for hyperlink url on selecting Listing pane only layout in configuration and simple listing in hyperlink dialog - Context menu.")
	public void SprintTest54_1_30_7B_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

  		//Step-1 : Navigate to Vault in tree view
  		//---------------------------------------
  		configurationPage.treeView.clickTreeViewItem(Caption.Configurationsettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item


  		if (!configurationPage.configurationPanel.getPageName().toUpperCase().equalsIgnoreCase(testVault.toUpperCase())) //Checks if navigated to vault settings page
  			throw new Exception("Vault settings page in configuration is not opened.");

  		Log.message("1. Navigated to Vault settings page.");

  		//Step-2 : Select Listing pane layout layout and save the settings
  		//--------------------------------------------------------------------------------------
  		configurationPage.configurationPanel.setLayout(Caption.Configurationsettings.Config_ListingPaneOnly.Value); //Sets as Listing pane layout

  		if (!configurationPage.configurationPanel.getLayout().equalsIgnoreCase(Caption.Configurationsettings.Config_ListingPaneOnly.Value)) //Verifies if Listing pane and properties pane layout is selected
  			throw new Exception(Caption.Configurationsettings.Config_ListingPaneOnly.Value + " layout is not selected.");

  		if (!configurationPage.configurationPanel.saveSettings()) //Saves the settings
  			throw new Exception("Settings are not saved properly.");

  		if (!configurationPage.logOut()) //Logs out from configuration page
  			throw new Exception("Log out is not successful after saving the settings in configuration page.");

  		Log.message("2. " + Caption.Configurationsettings.Config_ListingPaneOnly.Value + " layout is selected and settings are saved.");

  		//Step-3 : Login to the default page and navigate to any view
  		//-------------------------------------------------------------
  		HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches and logs into the default page

  		String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

  		Log.message("3. Logged into the default page and navigated to '" + viewToNavigate + "' view.");

  		//Step-4 : Open Get Hyperlink dialog for the object from context menu
			//-------------------------------------------------------------------
  		if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get Hyperlink from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get Hyperlink' title is not opened.");

			Log.message("4. Hyperlink dialog of an object (" + dataPool.get("ObjectName") + ") is opened through context menu.");

			//Step-5 : Select simple listing, Copy the link from text box and close the Get Hyperlink dialog
			//-----------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkLayoutOption(Caption.GetHyperlink.SimpleListing.Value))
				throw new Exception("Simple Listing is not selected in Get Hyperlink dialog.");

			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get Hyperlink dialog

			Log.message("5. Hyperlink is copied and Get Hyperlink dialog is closed.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkText, userName, password, ""); //Navigates to the hyperlink url

			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied object Hyperlink.[Expected URL: "+hyperlinkText+" & Current URL : "+ driver.getCurrentUrl() +"]");

			Log.message("6. Object Hyperlink is opened in the browser.");

			//Verification : Verify if default layout is displayed
  		//----------------------------------------------------
			String unAvailableLayouts = "";

			if (homePage.isSearchbarPresent()) //Checks if Search bar present
				unAvailableLayouts = "Search Bar is available;";

			if (homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				unAvailableLayouts = unAvailableLayouts + "Task Pane is available;";

			if (homePage.previewPane.isTabExists(Caption.Previewpane.MetadataTab.Value))
				unAvailableLayouts = unAvailableLayouts + "Properties Pane is available;";

			if (homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				unAvailableLayouts = unAvailableLayouts + "Menu Bar is available;";

			if (homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				unAvailableLayouts = unAvailableLayouts + "Breadcrumb is available;";

			if (homePage.taskPanel.isAppletEnabled())
				unAvailableLayouts = unAvailableLayouts + "Java Applet is available;";

			if (!homePage.isListViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "List View is not available;";

			if (homePage.isTreeViewDisplayed())
				unAvailableLayouts = unAvailableLayouts + "Tree View is available;";

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page is displayed as ." + Caption.Configurationsettings.Config_ListingPaneOnly.Value);
			else
				Log.fail("Test case Failed. Few layots are not as expected on selecting " + Caption.Configurationsettings.Config_ListingPaneOnly.Value + ". Refer additional information : " 
								+ unAvailableLayouts, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch


		finally {

			driver.get(configURL);

			ConfigurationPage configurationPage = new ConfigurationPage(driver);
			configurationPage.treeView.clickTreeViewItem(Caption.Configurationsettings.VaultSpecificSettings.Value + ">>" + testVault); //Clicks tree view item

			configurationPage.configurationPanel.setLayout(Caption.Configurationsettings.Config_Default.Value);

			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest54_1_30_7B_1
	             */

	/**
	 * 162015.18.1A: Verify Vault/Logout link is not displayed in task pane while selecting hide in configuration page for Vault option.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint16-2015", "Password"}, 
			description = "Verify Vault/Logout link is not displayed in task pane while selecting hide in configuration page for Vault option.")
	public void SprintTest162015_18_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		String menuItem = null;
		String prevCommand = null;
		ConfigurationPage configurationPage = null;

		try {




			driver = WebDriverUtils.getDriver();


			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);


			//1. Click the Task area link 
			//----------------------------
			configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_TaskArea.Value);


			Log.message("1. Navigated to task specific settings");

			//2. Hide "Vault" shortcut in task area

			menuItem = dataPool.get("MenuItem");


			prevCommand = configurationPage.configurationPanel.getVaultCommands(menuItem);
			configurationPage.configurationPanel.setVaultCommands(menuItem,"Hide");

			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			Log.message("2. Vault option is hided from task area and settings are saved in configuration page");

			//3. Logging out from configuration page and lauch the default webpage

			configurationPage.logOut();


			LoginPage loginPage = new LoginPage(driver);
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);

			Log.message("3. Logged out from configuration page and Default webpage is launched");

			//Verification: To verify if Vault option is not displayed in the task pane
			//------------------------------------------------------------------------

			if(!homePage.taskPanel.isItemExists(dataPool.get("MenuItem")))
				Log.pass("Test case Passed. The Task Panel item " + dataPool.get("MenuItem") + " is not displayed.", driver);
			else
				Log.fail("Test Case Failed. The Task Panel item " + dataPool.get("MenuItem") + " still displayed", driver);

		}//End Try
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (driver != null)
			{
				try
				{
					configurationPage.configurationPanel.resetVaultCommands(menuItem, prevCommand, testVault);
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);

		}//End Finally

	}//End SprintTest162015_18_1A


	/**
	 * 162015.18.1B: Verify Vault/Logout link is displayed in task pane while selecting show in configuration page for Vault option.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint16-2015", "Password"}, 
			description = "Verify Vault/Logout link is displayed in task pane while selecting Show in configuration page for Vault option.")
	public void SprintTest162015_18_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		String menuItem = null;
		String prevCommand = null;
		ConfigurationPage configurationPage = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);


			//1. Click the Task area link 
			//----------------------------
			configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_TaskArea.Value);


			Log.message("1. Navigated to task specific settings");

			//2. Hide "Vault" shortcut in task area

			menuItem = dataPool.get("MenuItem");


			prevCommand = configurationPage.configurationPanel.getVaultCommands(menuItem);
			configurationPage.configurationPanel.setVaultCommands(menuItem,"Show");

			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			Log.message("2. Vault option is hided from task area and settings are saved in configuration page");

			//3. Logging out from configuration page and lauch the default webpage

			configurationPage.logOut();

			LoginPage loginPage = new LoginPage(driver);
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);


			Log.message("3. Logged out from configuration page and Default webpage is launched");

			//Verification: To verify if Vault option is not displayed in the task pane
			//------------------------------------------------------------------------
			boolean result = homePage.taskPanel.isItemExists(dataPool.get("MenuItem"));

			String passMsg = "Test case Passed. The Task Panel item " + dataPool.get("MenuItem") + " is displayed while selecting show in configuration page.";

			if(dataPool.get("MenuItem").contains("Vaults") && result == false)
			{
				result = true;
				passMsg = "Test case Passed. The Task Panel item " + dataPool.get("MenuItem") + " is not displayed when user account exist in single vault, even though show selected in the configuration page.";
			}

			if(result)
				Log.pass(passMsg, driver);
			else
				Log.fail("Test Case Failed. The Task Panel item " + dataPool.get("MenuItem") + " is not displayed while selecting show in configuration page.", driver);

		}//End Try
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (driver != null)
			{
				try
				{
					configurationPage.configurationPanel.resetVaultCommands(menuItem, prevCommand, testVault);
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}

			Utility.quitDriver(driver);

		}//End Finally

	}//End SprintTest162015_18_1B

	/*
	 * 95.1.1: Verify if logout option is displayed in context menu while selecting Listing pane only layout 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug"}, 
			description = "Verify if logout option is displayed in context menu while selecting Listing pane only layout")
	public void SprintTest95_1_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		String prevLayout = null;
		ConfigurationPage configurationPage = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			Log.message("1. Logged in to Configuiration");

			//2. Set the layout
			//----------------------------
			configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault); // Clicks Vault item

			prevLayout = configurationPage.configurationPanel.getLayout(); // Gets the current layout selection
			configurationPage.configurationPanel.setLayout(dataPool.get("Layout")); // Sets the Listing pane only layout

			Log.message("2. "+dataPool.get("Layout") +" is selected");

			//3. Save the changes in configuration page
			//-------------------------------------------
			if (!configurationPage.configurationPanel.saveSettings()) // Verifies the Changes has been saved in configuration page
				throw new Exception("Configuration settings are not saved.");

			Log.message("3. Settings are saved in configuration page");

			//4. Logout from configurationpage and launch the default webpage
			//--------------------------------------------------------------
			if (!configurationPage.logOut()) // Verified if user is successfully logged out from configuration page
				throw new Exception("Configuration page is not logged out to login page.");

			HomePage homePage1= LoginPage.launchDriverAndLogin(driver, false);

			Log.message("4. Default webpage is launched");

			//5. Right click on the default webpage

			homePage1.listView.rightClickListview();
			Log.message("5. Right clicked on the list view");

			//6. Verification if Logout option is displayed in context menu			
			if(homePage1.listView.isItemAvailableinContextmenu(dataPool.get("MenuItem")))
			{
				Log.message("6. Logout option is displayed in context menu");

				//7.Logout from default webpage using context menu
				homePage1.listView.clickContextMenuItem(dataPool.get("MenuItem"));


				if(driver.getCurrentUrl().toUpperCase().trim().contains("LOGIN.ASPX"))
				{
					Log.message("7. Logged out from default webpage using context menu");
					Log.pass("Test case Passed. The " + dataPool.get("MenuItem") + " option is displayed in context menu in Listing area only layout and successfully logged out using context menu");
				}
				else
					Log.fail("Test case failed. Error while logging out from webaccess using context menu", driver);
			}
			else
				Log.fail("Test case Failed. The " + dataPool.get("MenuItem") + " option is not displayed in Context menu in Listing area only layout.", driver);

		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (driver != null)
			{
				try
				{
					configurationPage.configurationPanel.resetLayout(prevLayout, testVault);

				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);

		}//End Finally

	}//End SprintTest95_1_1

	/*
	 * 95.1.2: Verify if logout option is displayed in context menu while selecting Listing pane and properties pane only layout 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug"}, 
			description = "Verify if logout option is displayed in context menu while selecting Listing area and right pane only layout")
	public void SprintTest95_1_2(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		String prevLayout = null;
		ConfigurationPage configurationPage = null; //Launched driver and logged in

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launched driver and logged in

			Log.message("1. Logged in to Configuiration");

			//2. Set the layout
			//----------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault); // Clicks Vault item

			prevLayout = configurationPage.configurationPanel.getLayout(); // Gets the current layout selection
			configurationPage.configurationPanel.setLayout(dataPool.get("Layout")); // Sets the Listing pane only layout

			Log.message("2. "+dataPool.get("Layout") +" is selected");

			//3. Save the changes in configuration page
			//-------------------------------------------
			if (!configurationPage.configurationPanel.saveSettings()) // Verifies the Changes has been saved in configuration page
				throw new Exception("Configuration settings are not saved.");

			Log.message("3. Settings are saved in configuration page");

			//4. Logout from configurationpage and launch the default webpage
			//--------------------------------------------------------------
			if (!configurationPage.logOut()) // Verified if user is successfully logged out from configuration page
				throw new Exception("Configuration page is not logged out to login page.");

			Log.message("4. Default webpage is launched");

			//5. Right click on the default webpage
			HomePage homePage1= LoginPage.launchDriverAndLogin(driver, false);
			homePage1.listView.rightClickListview();
			Log.message("5. Right clicked on the list view");

			//6. Verification for logout option is displayed in context menu
			if(homePage1.listView.isItemAvailableinContextmenu(dataPool.get("MenuItem")))
			{

				Log.message("6. Logout option is displayed in context menu");

				//7.Logout from default webpage using context menu
				homePage1.listView.clickContextMenuItem(dataPool.get("MenuItem"));


				if(driver.getCurrentUrl().toUpperCase().trim().contains("LOGIN.ASPX"))
				{
					Log.message("7. Logged out from default webpage");
					Log.pass("Test case Passed. The " + dataPool.get("MenuItem") + " option is displayed in context menu in Listing area and right pane only layout. and successfully logged out using context menu");
				}
				else
					Log.fail("Test case failed. Error while logging out from webaccess using context menu", driver);
			}
			else
				Log.fail("Test case Failed. The " + dataPool.get("MenuItem") + " option is not displayed in Context menu in Listing area and right pane only layout.", driver);

		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			if (driver != null)
			{
				try
				{
					configurationPage.configurationPanel.resetLayout(prevLayout, testVault);

				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);

		}//End Finally

	}//End SprintTest95_1_2

	/**
	 * TC_2107 : Verify if proper error message is thrown on saving for Invalid IP range
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"IP"}, 
			description = "Verify if proper error message is thrown on saving for Invalid IP range")
	public void TC_2107(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Step-1 : Login to MFiles Configuration Webpage
			//----------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configPage = LoginPage.launchDriverAndLoginToConfig(driver, true);//Launch the Configruation page

			Log.message("1. Logged into the configuration webpage", driver);

			//Step-2 : Navigate to the General settings
			//-----------------------------------------
			configPage.treeView.clickTreeViewItem("General");//Clicks the General settings in the configuration web page

			Log.message("2. Navigated to the General settings in the configuration webpage", driver);

			//Step-3: Set the Page title in the configuration web page
			//--------------------------------------------------------
			if (!configPage.configurationPanel.enableIPRange(true))//Enable the IP range access field in the configuration webpage
				throw new Exception("IP range field is not enabled in the configuration webpage.");

			configPage.configurationPanel.setAllowedIPRange(dataPool.get("IPRange").replace("\"", ""));//Enters the page title value in the configuration webpage

			configPage.configurationPanel.setAutoLogin(false);//Disables the automatic login if its enabled

			Log.message("3. '"+ dataPool.get("IPRange").replace("\"", "") +"' is set in the IP Range field in the configuration webpage.", driver);

			//Verification: IF warning dialog is displayed while saving blank value in Page title field
			//------------------------------------------------------------------------------------------
			configPage.saveSettings();//Clicks the save button in the configuration webpage

			if (!MFilesDialog.exists(driver))//Checks if MFiles dialog is displayed or not
				throw new Exception("Warning dialog is not displayed while saving invalid IP range in the Allowed IP Range field");

			MFilesDialog mfDialog = new MFilesDialog(driver);//Instantiates the MFiles Dialog

			String actualMsg = mfDialog.getMessage();//Gets the message from MFiles Dialog

			if (actualMsg.equalsIgnoreCase(dataPool.get("ExpectedMsg")))
				Log.pass("Test case passed. Expected warning dialog is displayed while saving invalid value("+dataPool.get("IPRange").replace("\"", "")+") in the IP range field in the configuration webpage.", driver);
			else
				Log.fail("Test case failed. Expected warning dialog is not displayed while saving invalid value("+dataPool.get("IPRange").replace("\"", "")+") in the page title field in the configuration webpage.", driver);

			mfDialog.close();//Closes the MFiles dialog in the view

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End TC_2107

	/**
	 * TC_38635 : Verify the page title field for Blank Value
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"PageTitle"}, 
			description = "Verify the page title field for Blank value")
	public void TC_38635(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;  ConfigurationPage configPage = null; ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			//Step-1 : Login to MFiles Configuration Webpage
			//----------------------------------------------
			dataPool = new ConcurrentHashMap <String, String>(dataValues);
			configPage = LoginPage.launchDriverAndLoginToConfig(driver, true);//Launch the Configruation page

			Log.message("1. Logged into the configuration webpage", driver);

			//Step-2 : Navigate to the General settings
			//-----------------------------------------
			configPage.treeView.clickTreeViewItem("General");//Clicks the General settings in the configuration web page

			Log.message("2. Navigated to the General settings in the configuration webpage", driver);

			//Step-3: Set the Page title in the configuration web page
			//--------------------------------------------------------
			configPage.configurationPanel.setPageTitle(dataPool.get("PageTitle").replace("\"", ""));//Enters the page title value in the configuration webpage

			Log.message("3. '"+ dataPool.get("PageTitle").replace("\"", "") +"' is set in the page title field in the configuration webpage.", driver);

			//Verification: IF warning dialog is displayed while saving blank value in Page title field
			//------------------------------------------------------------------------------------------
			configPage.configurationPanel.setAutoLogin(false);//Sets the auto login off in the configuration webpage
			configPage.saveSettings();//Clicks the save button in the configuration webpage

			if (!MFilesDialog.exists(driver))//Checks if MFiles dialog is displayed or not
				throw new Exception("Warning dialog is not displayed while saving blank value in the Page title field");

			MFilesDialog mfDialog = new MFilesDialog(driver);//Instantiates the MFiles Dialog

			String actualMsg = mfDialog.getMessage();//Gets the message from MFiles Dialog

			if (actualMsg.equalsIgnoreCase(dataPool.get("ExpectedMsg")))
				Log.pass("Test case passed. Expected warning dialog is displayed while saving invalid value("+dataPool.get("PageTitle").replace("\"", "")+") in the page title field in the configuration webpage.", driver);
			else
				Log.fail("Test case failed. Expected warning dialog is not displayed while saving invalid value("+dataPool.get("PageTitle").replace("\"", "")+") in the page title field in the configuration webpage.", driver);

			mfDialog.close();//Closes the MFiles dialog in the view

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			if (driver != null)
			{
				try
				{
					configPage.treeView.clickTreeViewItem("General");//Clicks the General settings in the configuration web page
					configPage.configurationPanel.setPageTitle(dataPool.get("DefaultPageTitle").replace("\"", ""));//Enters the page title value in the configuration webpage
					configPage.saveSettings();//Clicks the save button in the configuration webpage
					MFilesDialog.closeMFilesDialog(driver);//Closes the MFiles confirmation dialog
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		}//End finally

	}//End TC_38635

	/**
	 * TC_38635_1 : Verify the page title field for Script Value
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"PageTitle"}, 
			description = "Verify the page title field for Script value")
	public void TC_38635_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;  ConfigurationPage configPage = null; ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			//Step-1 : Login to MFiles Configuration Webpage
			//----------------------------------------------
			dataPool = new ConcurrentHashMap <String, String>(dataValues);
			configPage = LoginPage.launchDriverAndLoginToConfig(driver, true);//Launch the Configruation page

			Log.message("1. Logged into the configuration webpage", driver);

			//Step-2 : Navigate to the General settings
			//-----------------------------------------
			configPage.treeView.clickTreeViewItem("General");//Clicks the General settings in the configuration web page

			Log.message("2. Navigated to the General settings in the configuration webpage", driver);

			//Step-3: Set the Page title in the configuration web page
			//--------------------------------------------------------
			configPage.configurationPanel.setPageTitle(dataPool.get("PageTitle").replace("\"", ""));//Enters the page title value in the configuration webpage

			Log.message("3. '"+ (dataPool.get("PageTitle").replace("\"", "").replaceAll("<", "{")).replaceAll(">", "}") +"' is set in the page title field in the configuration webpage.", driver);

			//Step-4: Save the changes
			//------------------------------------------------------------------------------------------
			configPage.configurationPanel.setAutoLogin(false);//Sets the auto login off in the configuration webpage
			configPage.saveSettings();//Clicks the save button in the configuration webpage

			if (!MFilesDialog.exists(driver))//Checks if MFiles dialog is displayed or not
				throw new Exception("Warning dialog is not displayed while saving blank value in the Page title field");

			MFilesDialog mfDialog = new MFilesDialog(driver);//Instantiates the MFiles Dialog

			String actualMsg = mfDialog.getMessage();//Gets the message from MFiles Dialog

			if (!actualMsg.contains(dataPool.get("SaveSuccessMsg")))
				throw new Exception("Save success warning dialog is not displayed");

			Log.message("4. Script value is saved in the Configuration web page in the Page title field", driver);

			//Verification: If Script value is displayed in the page title field
			//--------------------------------------------------------------------
			driver.navigate().refresh();//Refresh the configuration webpage

			if (driver.getTitle().equalsIgnoreCase(dataPool.get("PageTitle")))
				Log.pass("Test case passed. Script value("+ (dataPool.get("PageTitle").replaceAll("<", "{")).replaceAll(">", "}") +") is displayed as expected in the page title.", driver);
			else
				Log.fail("Test case failed. Script value("+ (dataPool.get("PageTitle").replaceAll("<", "{")).replaceAll(">", "}") +") is not displayed as expected in the page title. [Actual page title : "+driver.getTitle()+"]", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			if (driver != null)
			{
				try
				{
					configPage.treeView.clickTreeViewItem("General");//Clicks the General settings in the configuration web page
					configPage.configurationPanel.setPageTitle(dataPool.get("DefaultPageTitle").replace("\"", ""));//Enters the page title value in the configuration webpage
					configPage.saveSettings();//Clicks the save button in the configuration webpage
					MFilesDialog.closeMFilesDialog(driver);//Closes the MFiles confirmation dialog
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		}//End finally

	}//End TC_38635_1	


} //End class ConfigSettings
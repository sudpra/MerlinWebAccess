package MFClient.Tests;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import genericLibrary.DataProviderUtils;
import genericLibrary.Log;
import genericLibrary.Utils;
import genericLibrary.WebDriverUtils;
import genericLibrary.EmailReport;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestContext;
import org.testng.annotations.*;

import MFClient.Pages.ConfigurationPage;
import MFClient.Pages.HomePage;
import MFClient.Pages.LoginPage;
import MFClient.Wrappers.Caption;
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.MFilesObjectList;
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.PropertiesPane;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.TaskPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class ConfigurationUI {

	public static String xlTestDataWorkBook;
	public static String xlTestDataWorkSheet;
	public static String configSite=null;
	public static String webSite=null;
	public static String defaultSite = null;
	public static String userName=null;
	public static String password=null;
	public static String windowsUserName=null;
	public static String windowsPassword=null;
	public static String windowsUserDomain = null;
	public static String documentVault=null;
	public static String vaultGUID=null;
	public static String driverType=null;
	public static String currDateTime=null;
	public static String propName=null;
	public static String docName=null;
	public static String configPageTime=null;
	public static String className = null;
	public static String productVersion = null;
	public static WebDriver driver = null;

	Boolean isSuccess=false;

	@BeforeSuite
	public void cleanScreenShots(ITestContext context) throws Exception {
		Log.cleanScreenShotFolder(context);
		Utility.installApplication();
	}

	@BeforeTest
	public void init(ITestContext context) throws Exception
	{
		ConfigurationUI.xlTestDataWorkBook = "ConfigurationUI.xls";
		configSite = System.getProperty("ConfigurationURL")!=null?System.getProperty("ConfigurationURL"):context.getCurrentXmlTest().getParameter("ConfigurationURL");
		webSite = System.getProperty("webSite")!=null?System.getProperty("webSite"):context.getCurrentXmlTest().getParameter("webSite");
		defaultSite = context.getCurrentXmlTest().getParameter("defaultSite");
		userName=context.getCurrentXmlTest().getParameter("UserName");
		password=context.getCurrentXmlTest().getParameter("Password");
		windowsUserName=context.getCurrentXmlTest().getParameter("WindowsUser");
		windowsPassword=context.getCurrentXmlTest().getParameter("WindowsPassword");
		windowsUserDomain=context.getCurrentXmlTest().getParameter("WindowsUserDomain");
		documentVault=context.getCurrentXmlTest().getParameter("VaultName");
		propName=context.getCurrentXmlTest().getParameter("PropName");
		driverType=context.getCurrentXmlTest().getParameter("driverType");
		//docName=context.getCurrentXmlTest().getParameter("DocumentName");
		className = this.getClass().getSimpleName().toString().trim();
		if (context.getCurrentXmlTest().getParameter("driverType").equalsIgnoreCase("IE"))
			productVersion = "M-Files " + context.getCurrentXmlTest().getParameter("productVersion").trim() + " - " + context.getCurrentXmlTest().getParameter("driverType").toUpperCase().trim() + context.getCurrentXmlTest().getParameter("driverVersion").trim();
		else
			productVersion = "M-Files " + context.getCurrentXmlTest().getParameter("productVersion").trim() + " - " + context.getCurrentXmlTest().getParameter("driverType").toUpperCase().trim();


		Utility.restoreTestVault();
		Utility.restoreTestVault("My Vault", "MyConfigUI Vault");
		Utility.configureUsers(xlTestDataWorkBook);
		Utility.configureUsers(xlTestDataWorkBook, "MyVaultUsers", "My Vault");

		vaultGUID = Utility.getVaultGUID(documentVault);
	}

	/**
	 * <br>Description get the 'Current timestamp'</br>
	 * @return
	 */
	public static String getTime() {

		//Gets current date and time
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		currDateTime = dateFormat.format(date);
		return currDateTime;
	}

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
			Utility.destroyTestVault();
			Utility.destroyTestVault("My Vault");
			Utility.destroyUsers(xlTestDataWorkBook, "DestroyUsers");

		}//End try

		catch(Exception e){
			throw e;
		}//End Catch
	}//End cleanApp

	/**
	 * TestCase ID: Test_1_1_1
	 * <br>Description:  Open /configuration.aspx page instead of /login.aspx</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_1_1",description=" Open /configuration.aspx page instead of /login.aspx")
	public void Test1_1_1(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		driver = WebDriverUtils.getDriver();
		

		try {

			//Step-1: Launch configuration page with URL '/Configuration.aspx'
			driver.get(configSite);
			Log.message("1. Launched configuration page with URL '/Configuration.aspx'.", driver);

			if (driver.getCurrentUrl().toUpperCase().trim().contains("/LOGIN.ASPX?"))
				Log.pass("Test Passed. Launching Configuration.aspx page launched login page.");
			else
				Log.fail("Test Failed. Launching Configuration.aspx page does not launched login page..", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			driver.quit();
		} //End finally

	} //End Test1_1_1

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_1_2",description="Test1_1_2: Try to login without System administrator permission")
	public void Test1_1_2(HashMap<String,String> dataValues, String currentDriver,ITestContext context) throws Exception {

		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData=null;
		try {
			testData = new ConcurrentHashMap <String, String>(dataValues);
			//Step-1: Launch configuration page
			driver.get(configSite);
			Log.message("1. Launched configuration page login page.", driver);

			//Instantiate configuration page
			LoginPage loginPage=new LoginPage(driver);
			//Step-2: Login with user without system admin permission
			loginPage.setUserName(testData.get("UserName"));
			loginPage.setPassword(testData.get("Password"));
			loginPage.clickLoginBtn();
			Thread.sleep(500);

			Log.message("2. Loggedin with Non-admin permission", driver);

			//Verify the display of 'You are not authorized to view this page. Only system administrators can access the configuration page.' error message appears 
			if (loginPage.getErrorMessage().toUpperCase().contains("YOU ARE NOT AUTHORIZED TO VIEW THIS PAGE. ONLY SYSTEM ADMINISTRATORS CAN ACCESS THE CONFIGURATION PAGE."))
				Log.pass("Test Passed!!!..with an Error message '"+loginPage.getErrorMessage().toUpperCase()+"', when login with NonAdmin credentials.");
			else
				Log.fail("Test Failed!!!... Login is un-successful but error message is not as expected.<br/> " + loginPage.getErrorMessage(), driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch 

		finally {
			driver.quit();
		} //End finally

	} //End Test1_1_2

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_1_3",description="Test1_1_3 : Login with System administrator permission")
	public void Test1_1_3(HashMap<String,String> dataValues, String currentDriver,ITestContext context) throws Exception {

		driver = WebDriverUtils.getDriver();
		

		try {

			//Step-1: Launch configuration page
			driver.get(configSite);
			Log.message("1. Launched configuration page login page.", driver);

			//Step-2 : Login with 'Admin' user previleges
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToConfigurationUI(userName,password);
			Log.message("2. Loggedin with user with System admin permission", driver);

			//Verify if 'logon attempt failed.'
			if (!loginPage.getErrorMessage().isEmpty())
				throw new Exception("Unable to login to Configuration Page, with Error :"+loginPage.getErrorMessage());

			ConfigurationPage configPage=new ConfigurationPage(driver);
			//Step-3: Verify if the ConfigurationUI tree view displayed
			if (!configPage.isSettingsTreeDisplayed())
				Log.fail("Test Failed!!!.. Unable to Login to Configuration application page using Admin previleges with Error:<br/> " + configPage.getErrorMessage(),driver);
			else
				Log.pass("Test Passed!!!.. Successfully Logged in to Configuration application page using Admin previleges.");

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			driver.quit();				
		} //End finally

	} //End Test1_1_3
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_1_4",description="Test1_1_4 : Try to view pages for vaults where the user doesn't have a user account")
	public void Test1_1_4(HashMap<String,String> dataValues, String currentDriver,ITestContext context) throws Exception {

		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData = new ConcurrentHashMap <String, String>(dataValues);

		try {

			//Step-1: Launch configuration page
			driver.get(configSite);
			Log.message("1. Launched configuration page login page.", driver);

			//Instantiate configuration page
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToConfigurationUI(testData.get("UserName"),testData.get("Password"));

			//Step-2 : Login with user who does not have account in the vault
			ConfigurationPage configPage=new ConfigurationPage(driver);
			Log.message("2. Loggedin with non-admin permission", driver);

			//Verify if Login failed
			if (!loginPage.getErrorMessage().isEmpty())
				throw new Exception("Unable to login to Configuration Page, with Error :"+configPage.getErrorMessage());

			configPage.clickVaultFolder(documentVault);
			Thread.sleep(300);
			//Click the vault where user doenot have user account
			configPage.clickVaultFolder(documentVault); //Selects General in the tree view of configuration page
			Log.message("3. Clicked on Vault folder in configuration Page", driver);

			//Verifies if Default view is in disabled state for non-admin user
			if (!configPage.configurationPanel.isDefaultViewEnabled() && !configPage.configurationPanel.isDefaultSearchCriteriaEnabled())
				Log.pass("Test Passed. Default home view drop down and Default search criteria checkbox are disabled for Non-vault users.");
			else if (configPage.configurationPanel.isDefaultViewEnabled() && configPage.configurationPanel.isDefaultSearchCriteriaEnabled())
				Log.fail("Test Failed. Default view & Search criteria checkbox field is enabled for non-admin user", driver);
			else if (configPage.configurationPanel.isDefaultViewEnabled())
				Log.fail("Test Failed. Default view is enabled for non-admin user", driver);
			else
				Log.fail("Test Failed. Default Search criteria checkbox field is enabled for non-admin user", driver);

		} //End try

		catch (Exception e) {
			if(e.getClass().toString().contains("NullPointerException"))
				Log.exception(new Exception("No Error message displayed for Non-Vault user"), driver);
			else
				Log.exception(e, driver);

		} //End catch

		finally {
			driver.quit();				
		} //End finally
	} //End Test1_1_4

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_1_5",description="Test1_1_5 : Log Out button Exists")
	public void Test1_1_5(HashMap<String,String> dataValues, String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		try {

			//Step-1: Launch configuration page
			driver.get(configSite);
			Log.message("1. Launched configuration page login page.", driver);

			//Instantiate configuration page
			LoginPage loginPage=new LoginPage(driver);

			loginPage.loginToConfigurationUI(userName,password);
			Log.message("2. Loggedin with system admin credentials", driver);

			//Step-2 : Login with system admin credentials
			ConfigurationPage configPage=new ConfigurationPage(driver);
			//Verify if Login failed
			if (!configPage.getErrorMessage().isEmpty())
				throw new Exception("Unable to login to Configuration Page, with Error :"+configPage.getErrorMessage());

			//Step-3: Checks if logout button exists in the configuration page after logging in
			if (configPage.isLogOutExists())
				Log.pass("Test Passed. Log out button exists after loggin in to configuration page.");
			else
				Log.fail("Test Failed. Log out button does not exists after loggin in to configuration page.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally{
			driver.quit();
		}
	} //End Test1_1_5

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_1_8",description="Test1_1_8 : Verify if the start page of MFWA configuration is empty")
	public void Test1_1_8(HashMap<String,String> dataValues, String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		


		try {

			//Step-1: Launch configuration page
			driver.get(configSite);
			Log.message("1. Launched configuration page login page.", driver);

			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToConfigurationUI(userName,password);

			//Step-2 : Login with system admin credentials
			ConfigurationPage configPage=new ConfigurationPage(driver);
			Log.message("2. Logged in with system admin credentials", driver);

			//Verify if Login failed
			if (!configPage.getErrorMessage().isEmpty())
				throw new Exception("Unable to login to Configuration Page, with Error :"+configPage.getErrorMessage());

			//Step-3: Start page of configuration page is empty.
			if (configPage.isGeneralSettingPageEmpty())
				Log.pass("Test Passed. Start page of configuration page is empty.");
			else
				Log.fail("Test Failed. Start page of configuration page is not empty..", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			driver.quit();
		} //End finally

	} //End Test1_1_8

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_2_1A",description="Test1_2_1A : Verify if 'Last Modified' timestamp is correctly displayed in general settings")
	public void Test1_2_1A(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		String oldPageTitle = null;
		String newPageTitle = null;
		//Instantiate Webdriver
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData=new ConcurrentHashMap <String, String>(dataValues);
		ConfigurationPage configPage=null;

		try {

			//Step-1: Launch configuration page
			driver.get(configSite);			
			Log.message("1. Launched configuration page login page.", driver);

			//Step-2 : Login with system admin credentials
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToConfigurationUI(userName,password);

			configPage=new ConfigurationPage(driver);
			//Verify if Login failed
			if (!configPage.getErrorMessage().isEmpty()) {
				throw new Exception("Unable to login to Configuration Page, with Error :"+configPage.getErrorMessage());
			}
			Log.message("2. Logged in with system admin credentials", driver);
			//Step-3: Click the 'General Settings-General' Folder
			configPage.clickTreeViewItem(testData.get("GeneralSettings"));
			Log.message("3. Clicked the 'General Settings-General' Folder", driver);

			//Step-4 : Modify the page title

			oldPageTitle = configPage.configurationPanel.getPageTitle();// genConfig.GetPageTitle(); //Gets the Page title before modifying
			newPageTitle = "Web Access_" + Utils.getCurrentDateTime();
			configPage.configurationPanel.setPageTitle(newPageTitle); //Sets the new page title
			if(configPage.configurationPanel.getAutoLogin())
				configPage.configurationPanel.setAutoLogin(false);
			//Click 'Save' button

			String dateformat = configPage.configurationPanel.clickSavebtn();

			//Gets current date and time
			currDateTime=dateformat;
			configPage.clickOKBtnOnSaveDialog();
			Log.message("4. Updated the Page Title to :"+newPageTitle, driver);

			//Step-5: Refresh the 'General Settings-General' Folder by reselecting
			driver.navigate().refresh();//Refresh the browser 
			configPage.clickTreeViewItem(testData.get("GeneralSettings"));
			Log.message("5. Refreshed the 'General Settings-General' Folder by reselecting", driver);

			configPage=new ConfigurationPage(driver);
			String modifiedDate=configPage.configurationPanel.getLastModifiedDateTime();

			//Step-6: Verifies the time in vault configuration page is same as the system date and time at the time of modification
			if (modifiedDate.compareTo(currDateTime)==1 || modifiedDate.compareTo(currDateTime)==0)
				Log.pass("Test Passed. Actual Last modified time displayed as "+configPage.configurationPanel.getLastModifiedDateTime()+", but expected Last modified time is "+currDateTime);
			else
				Log.fail("Test Failed. Actual Last modified time displayed as "+configPage.configurationPanel.getLastModifiedDateTime()+", but expected Last modified time is "+currDateTime, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try{
				//Reset the default page title
				configPage=new ConfigurationPage(driver);
				configPage.clickTreeViewItem(testData.get("GeneralSettings"));
				configPage.configurationPanel.setPageTitle(oldPageTitle);
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}
			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch
			finally {
				driver.quit();
			} //End finally
		}
	} //End ConfigurationUI1_2_1A



	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_2_1B",description="Test1_2_1A : Verify if 'Last Modified' timestamp is correctly displayed in vault specific settings")
	public void Test1_2_1B(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData=new ConcurrentHashMap <String, String>(dataValues);
		ConfigurationPage configPage=null;

		try {

			//Step-1: Launch configuration page
			driver.get(configSite);			
			Log.message("1. Launched configuration page login page.", driver);

			//Step-2 : Login with system admin credentials
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToConfigurationUI(userName,password);

			configPage=new ConfigurationPage(driver);
			//Verify if Login failed
			if (!configPage.getErrorMessage().isEmpty()) {
				throw new Exception("Unable to login to Configuration Page, with Error :"+configPage.getErrorMessage());
			}
			Log.message("2. Logged in with system admin credentials", driver);
			//Step-3: Click the 'vault specific settings-vault' Folder
			configPage.clickTreeViewItem(testData.get("VaultSettings"));
			configPage=new ConfigurationPage(driver);
			configPage.expandVaultFolder(documentVault);
			Log.message("3. Clicked the 'vault specific settings' Folder", driver);

			//Step-4 : click the 'controls' in vault specific settings
			//--------------------------------------------------------
			configPage.clickSettingsFolder("Controls");
			Log.message("4. Clicked the 'Controls' in vault specific settings", driver);

			String dateformat = "";

			//Step-5 : Set the controls command in vault specific settings
			//------------------------------------------------------------
			configPage.chooseConfigurationVaultSettings(driver, "Context menu","controls","Hide");
			dateformat = configPage.configurationPanel.clickSavebtn();
			configPage.clickOKBtnOnSaveDialog();

			if(!configPage.configurationPanel.getVaultCommands("Context menu").equalsIgnoreCase("Hide"))
				throw new Exception("Unable to save 'Context menu' hide property.");
			else
				Log.message("5. Show 'Context menu' is enabled and settings are saved.", driver);


			//Gets current date and time
			currDateTime=dateformat;

			//Step-5: Refresh the 'General Settings-General' Folder by reselecting
			driver.navigate().refresh();//Refresh the browser 
			configPage=new ConfigurationPage(driver);
			configPage.clickTreeViewItem(testData.get("VaultSettings"));
			configPage.expandVaultFolder(documentVault);
			configPage.clickSettingsFolder("Controls");
			Log.message("5. Refreshed the 'vault specific settings' browser", driver);

			configPage=new ConfigurationPage(driver);
			String modifiedDate=configPage.configurationPanel.getLastModifiedDateTime();

			//Step-6: Verifies the time in vault configuration page is same as the system date and time at the time of modification
			if (modifiedDate.compareTo(currDateTime)==1 || modifiedDate.compareTo(currDateTime)==0)
				Log.pass("Test Passed. Actual Last modified time displayed as "+configPage.configurationPanel.getLastModifiedDateTime()+", but expected Last modified time is "+currDateTime);
			else
				Log.fail("Test Failed. Actual Last modified time displayed as "+configPage.configurationPanel.getLastModifiedDateTime()+", but expected Last modified time is "+currDateTime, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try{
				//Reset the default page title
				configPage=new ConfigurationPage(driver);
				configPage.clickTreeViewItem(testData.get("VaultSettings"));
				configPage.expandVaultFolder(documentVault);
				configPage.clickSettingsFolder("Controls");
				configPage.chooseConfigurationVaultSettings(driver, "Context menu","controls","Show");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}
			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch
			finally {
				driver.quit();
			} //End finally
		}

	}//End Test1_2_1B
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_2_6",description="Test1_2_6 : Display options: Page title")
	public void Test1_2_6(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		String oldPageTitle=null;
		String newPageTitle = null;
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData = new ConcurrentHashMap <String, String>(dataValues);
		ConfigurationPage configPage=null;
		try {

			//Step-1: Launch configuration page
			driver.get(configSite);
			Log.message("1. Launched configuration page login page.", driver);

			//Instantiate configuration page
			LoginPage loginPage=new LoginPage(driver);

			//Step-2 : Login with system admin credentials
			loginPage.loginToConfigurationUI(userName,password);
			configPage=new ConfigurationPage(driver);
			Log.message("2. Logged in with system admin credentials", driver);

			//Verify if Login failed
			if (!configPage.getErrorMessage().isEmpty())
				throw new Exception("Unable to login to Configuration Page, with Error :"+configPage.getErrorMessage());

			//Step-3: Click the 'General Settings-General' Folder
			configPage.clickTreeViewItem(testData.get("GeneralSettings"));
			Log.message("3. Navigated to the General page in Web Access Configuration.", driver);

			//Step-2 : Modify the page title

			oldPageTitle = configPage.configurationPanel.getPageTitle();// genConfig.GetPageTitle(); //Gets the Page title before modifying
			newPageTitle = "Web Access_" + Utils.getCurrentDateTime();
			configPage.configurationPanel.setPageTitle(newPageTitle); //Sets the new page title

			//Set AutoLogin to false
			if(configPage.configurationPanel.getAutoLogin()) 
				configPage.configurationPanel.setAutoLogin(false);

			configPage.clickSaveButton();
			configPage.clickOKBtnOnSaveDialog();//Save Modified Settings

			Log.message("4. Page title modified to " + configPage.configurationPanel.getPageTitle(), driver);

			newPageTitle=configPage.configurationPanel.getPageTitle();
			configPage.clickLogOut();	//logOut of configuration Page

			//Step-4: Launch configuration page
			driver.get(configSite);
			loginPage.loginToConfigurationUI(userName,password);
			Log.message("5. Re-Loggedin to configuration page.", driver);

			configPage=new ConfigurationPage(driver);
			configPage.clickTreeViewItem(testData.get("GeneralSettings"));
			Log.message("6. Navigated to the General page in Web Access Configuration.", driver);

			if (driver.getTitle().substring(0, 27).toUpperCase().trim().equalsIgnoreCase(newPageTitle.substring(0, 27).toUpperCase()))
				Log.pass("Test Passed. Page title is changed as :"+newPageTitle.toUpperCase());
			else
				Log.fail("Test Failed. Expected the Page tile is :"+newPageTitle.toUpperCase()+" but Actual Page title is :"+driver.getTitle(), driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {
				configPage=new ConfigurationPage(driver);

				configPage.clickTreeViewItem(testData.get("GeneralSettings"));
				configPage.configurationPanel.setPageTitle(oldPageTitle);
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}
			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {
				driver.quit();
			}
		} //End finally

	} //End Test1_2_6


	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_2_8A",description="Test1_2_8A	: Windows SSO - 'Disabled' is default value")
	public void Test1_2_8A(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		String defaultWinSSOValue ="Disabled (Default)";
		String actualWinSSOValue;
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData=new ConcurrentHashMap <String, String>(dataValues);
		LoginPage loginPage=null;
		ConfigurationPage configPage = null;

		try {

			//Step-1: Launch configuration page
			driver.get(configSite);
			Log.message("1. Launched configuration page login page.", driver);

			//Instantiate configuration page
			loginPage=new LoginPage(driver);

			//Step-2 : Login with system admin credentials
			loginPage.loginToConfigurationUI(userName,password);
			Log.message("2. Logged in with system admin credentials", driver);

			//Verify if Login failed
			if (!loginPage.getErrorMessage().isEmpty())
				throw new Exception("Unable to login to Configuration Page, with Error :"+loginPage.getErrorMessage());

			configPage = new ConfigurationPage(driver);
			//Step-3:  Navigated to the General page in Web Access Configuration.
			configPage.clickTreeViewItem(testData.get("GeneralSettings")); //Selects General in the tree view of configuration page
			Log.message("3. Navigated to the General page in Web Access Configuration.", driver);

			actualWinSSOValue = configPage.configurationPanel.getWindowsSSO(); //Gets the actual value selected in window sso
			Log.message("4. Fetched the 'WindowsSSO' default value as '"+actualWinSSOValue+"' in Web Access Configuration.", driver);

			//Step-4: Verify 'Default' is the default value in windows SSO
			if (actualWinSSOValue.trim().toUpperCase().contains(defaultWinSSOValue.trim().toUpperCase()))
				Log.pass("Test Passed. Expected Default value of Windows SSO is '" + defaultWinSSOValue + "' and Actual Default value of Windows SSO is '"+actualWinSSOValue+"'");
			else
				Log.fail("Test Failed. Expected Default value of Windows SSO is '" + defaultWinSSOValue + "',But Actual Default value of Windows SSO is '"+actualWinSSOValue+"'", driver);
		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch
		finally {
			try {
				configPage = new ConfigurationPage(driver);
				if(!configPage.configurationPanel.getWindowsSSO().equals(defaultWinSSOValue)) {
					configPage.configurationPanel.setWindowsSSO(defaultWinSSOValue);
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				}
			}

			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch
			finally {
				driver.quit();
			} } //End finally
	} //End Test1_2_8A

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_2_8B",description="Verify the Windows SSO - 'Disabled'(default) functionality")
	public void Test1_2_8B(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		String defaultWinSSOValue ="Disabled (Default)";
		String prevWindowSso = null;
		String treeItem = null;
		LoginPage loginPage=null;


		driver = WebDriverUtils.getDriver();
		
		ConcurrentHashMap <String, String> testData = new ConcurrentHashMap <String, String>(dataValues);

		ConfigurationPage configPage=null;
		try {

			//Step-1: Launch configuration page
			driver.get(configSite);
			Log.message("1. Launched configuration page login page.", driver);

			//Step-2 : Login with system admin credentials
			loginPage=new LoginPage(driver);
			loginPage.loginToConfigurationUI(userName,password);

			configPage=new ConfigurationPage(driver);
			Log.message("2. Logged in to Configuration page Successfully", driver);

			//Verify if Login failed
			if (!configPage.getErrorMessage().isEmpty())
				throw new Exception("Unable to login to Configuration Page, with Error :"+configPage.getErrorMessage());

			treeItem=testData.get("GeneralSettings");
			configPage.clickTreeViewItem(treeItem); //Selects General in the tree view of configuration page

			//Step-3: Navigate to the General page in Web Access Configuration
			Log.message("3. Clicked on '"+treeItem+"' screen in Web Access Configuration Page.", driver);

			//Step-4: Modify the Windows SSO details

			prevWindowSso = configPage.configurationPanel.getWindowsSSO(); //Gets the previous window sso status
			Log.message("4.  Modified the Windows SSO details", driver);

			//Step-5: Verify if Windows SSO value is selected properly
			if (!prevWindowSso.toUpperCase().equals("Disabled (Default)")) {//Checks if settings are saved
				configPage.configurationPanel.setWindowsSSO("Disabled (Default)"); //Sets Disabled as the window sso option
				if (configPage.configurationPanel.getAutoLogin()) {
					configPage.configurationPanel.setAutoLogin(false);
					//Step-6: Save the Configuration Setting Changes
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
					Log.message("5. Saved the Configuration Settings correctly");
				}
			}
			Log.message("5. '"+configPage.configurationPanel.getWindowsSSO()+"' is selected as Window SSO in general settings.", driver);

			//Step-7: LogOut of application after changing settings
			configPage.clickLogOut(); //Logs out from the Configuration page
			Log.message("6. Logged out from the Configuration page", driver);

			//Step-8:Relaunch the Configuration Page
			driver.get(configSite);
			Log.message("7. Relaunched configuration page with URL :"+driver.getCurrentUrl()+"  and logged in ", driver);

			if (!loginPage.isWindowLoginDisplayed()) 
				Log.pass("Test Passed. Option to login with window credential does not exists in login page.");
			else 
				Log.fail("Test Failed. Option to login with window credential exists in login page.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try{
				loginPage=new LoginPage(driver);
				loginPage.navigateToApplication(configSite,userName,password,"");	
				configPage=new ConfigurationPage(driver);
				configPage.clickTreeViewItem(treeItem); //Selects General in the tree view of configuration page

				configPage.configurationPanel.setWindowsSSO(defaultWinSSOValue);
				if(configPage.configurationPanel.getAutoLogin()) {
					configPage.configurationPanel.setAutoLogin(false); //Selects Auto login option to true
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				} }

			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch
			finally {
				driver.quit();
			}}

	} //End Test1_2_8B

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_2_8C",description= "Verify Windows SSO - 'Show on login page'")
	public void Test1_2_8C(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		String defaultWinSSOValue ="Disabled (Default)";
		String prevWindowSso = null;
		String treeItem = null;

		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData = new ConcurrentHashMap <String, String>(dataValues);
		LoginPage loginPage=null;

		ConfigurationPage configPage=null;
		try {

			//Step-1: Launch configuration page
			driver.get(configSite);
			Log.message("1. Launched configuration page login page.", driver);

			//Instantiate configuration page
			loginPage=new LoginPage(driver);

			//Step-2 : Login with system admin credentials
			loginPage.navigateToApplication(configSite,userName,password,"");	
			configPage=new ConfigurationPage(driver);
			Log.message("2. Successfully logged in to Configuration page", driver);

			//Verify if Login failed
			if (!configPage.getErrorMessage().isEmpty())
				throw new Exception("Unable to login to Configuration Page, with Error :"+configPage.getErrorMessage());

			//Step-3: Navigate to the General page in Web Access Configuration
			Log.message("3. Navigated to the General page in Web Access Configuration.", driver);
			treeItem=testData.get("GeneralSettings");
			configPage.clickTreeViewItem(treeItem); //Selects General in the tree view of configuration page

			//Step-4: Modify the Windows SSO details
			Log.message("4.  Modified the Windows SSO details", driver);

			prevWindowSso = configPage.configurationPanel.getWindowsSSO(); //Gets the previous window sso status

			//Step-2 : Modify the page title
			if (!prevWindowSso.toUpperCase().equals("SHOW ON LOGIN PAGE")) {//Checks if settings are saved
				configPage.configurationPanel.setWindowsSSO("SHOW ON LOGIN PAGE"); //Sets Disabled as the window sso option
				if (configPage.configurationPanel.getAutoLogin())
					configPage.configurationPanel.setAutoLogin(false);
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				//Step-6: Save the Configuration Setting Changes
				Log.message("5. Saved the Configuration Setting Changes", driver);
			}

			//Step-3 : Open Login page
			configPage.clickLogOut(); //Logs out from the Configuration page
			Log.message("6. Logged out from the Configuration page", driver);
			Utils.waitForPageLoad(driver);
			driver.get(webSite);
			//Verifies if 'Log in with current Windows credentials' option is displayed
			if (!loginPage.isWindowLoginDisplayed()) 
				Log.fail("Test Failed. Link to login with window credential does not exists in login page.", driver);
			else 
				Log.pass("Test Passed. Login to login with window credential exists in login page.");
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {
				// configPage=new ConfigurationPage(driver);
				driver.get(configSite);
				int snooze = 0;

				while (snooze < 10 && !driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX") && !driver.getCurrentUrl().toUpperCase().contains("DEFAULT.ASPX") && !driver.getCurrentUrl().toUpperCase().contains("CONFIGURATION.ASPX"))
				{
					Thread.sleep(500);
					snooze++;
				}

				loginPage=new LoginPage(driver);
				configPage=loginPage.loginToConfigurationUI(userName,password);

				configPage=new ConfigurationPage(driver);
				configPage.clickTreeViewItem(treeItem); //Selects General in the tree view of configuration page

				configPage.configurationPanel.setWindowsSSO(defaultWinSSOValue);
				configPage.configurationPanel.setAutoLogin(false); //Selects Auto login option to false
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}

			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch
			finally {
				driver.quit();
			}} //End finally

	} //End Test1_2_8C

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_2_9",description="Verify 'Force M-Files User Login")
	public void Test1_2_9(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData=new ConcurrentHashMap <String, String>(dataValues);
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;

		try {

			//Step-1: Launch configuration page
			driver.get(configSite);
			Log.message("1. Launched configuration page login page.", driver);

			//Step-2 : Login with system admin credentials
			loginPage=new LoginPage(driver);
			loginPage.loginToConfigurationUI(userName,password);
			configPage=new ConfigurationPage(driver);
			Log.message("2. Logged in with system admin credentials", driver);

			configPage.clickVaultFolder(documentVault);
			Log.message("3. Clicked Vault folder in Configuration Page", driver);

			if(!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true);
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess()) 
					throw new Exception("Access to "+documentVault+ "not saved");
			}

			//Verify if Login failed
			if (!configPage.getErrorMessage().isEmpty())
				throw new Exception("Unable to login to Configuration Page, with Error :"+configPage.getErrorMessage());

			//Step-3: Navigate to the General page in Web Access Configuration
			configPage.clickTreeViewItem(testData.get("GeneralSettings")); //Selects General in the tree view of configuration page
			Log.message("3. Navigated to the General page in Web Access Configuration.", driver);

			//Step-4:  Enable Force M-Files user login in General page

			if (!configPage.configurationPanel.getWindowsSSO().equalsIgnoreCase("Disabled (Default)")) {
				configPage.configurationPanel.setWindowsSSO("Disabled (Default)");
			}
			Log.message("4. 'WindowsSSO' is set as 'Disabled(default)'", driver);

			if (!configPage.configurationPanel.getForceMFilesUserLogin()) {
				configPage.configurationPanel.setForceMFilesUserLogin(true); //Sets Force M-Files user login
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}
			Log.message("5. 'Force M-Files user login' is enabled", driver);
			//Step-5: Logged out from Configuration page and launch WebAccess Site.");
			configPage.clickLogOut();
			driver.get(webSite);
			//Step-6: Enter login details using 'MFiles user' credentials
			loginPage.setUserName(windowsUserName);
			loginPage.setPassword(windowsPassword);
			loginPage.clickLoginBtn();
			Log.message("6. M-Files user login credentails are provided to login when 'Force M-Files user login' is enabled.", driver);

			//Verifies 'Authentication Failed' error message appears in login page while logging in with windows user 
			if (!loginPage.getErrorMessage().toUpperCase().contains("AUTHENTICATION FAILED")) {
				throw new Exception("Authentication Failed message is not displayed when trying to login with Windows User");
			}
			Log.message("7. Authentication Failed message displayed as :'"+loginPage.getErrorMessage()+"' for windows user login.", driver);

			if (!loginPage.isLoginPageDisplayed()) {
				throw new Exception("Web Access LoginPage not displayed.");
			}
			else {
				loginPage.loginToWebApplication(userName,password,documentVault);
				Utils.waitForPageLoad(driver);
			}
			//Step-8: Verifies URL has default.aspx page and the breadcrumb with the vault name
			if (driver.getCurrentUrl().toUpperCase().trim().contains("DEFAULT.ASPX"))
				Log.pass("Test Passed. Web access login success only for M-Files user login.");
			else
				Log.fail("Test Failed. M-Files user login is un-successful.", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {
				HomePage homePage=new HomePage(driver);
				loginPage=new LoginPage(driver);
				homePage.menuBar.logOutFromMenuBar();
				driver.get(configSite);
				int snooze = 0;

				while (snooze < 10 && !driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX") && !driver.getCurrentUrl().toUpperCase().contains("DEFAULT.ASPX") && !driver.getCurrentUrl().toUpperCase().contains("CONFIGURATION.ASPX"))
				{
					Thread.sleep(500);
					snooze++;
				}
				loginPage.loginToConfigurationUI(userName,password);
				configPage=new ConfigurationPage(driver);
				configPage.clickTreeViewItem(testData.get("GeneralSettings"));

				if (configPage.configurationPanel.getForceMFilesUserLogin()) {
					configPage.configurationPanel.setForceMFilesUserLogin(false); //De-Selects Force M-Files user login
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				}
				configPage.clickVaultFolder(documentVault);
				if (!configPage.configurationPanel.getVaultAccess()) {
					configPage.configurationPanel.setVaultAccess(true);
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				}
			}

			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch
			finally {
				driver.quit();	
			}} //End finally

	} //End Test1_2_9

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_2_13",description="Test1_2_13: Reset 'Force M-Files User Login' settings")
	public void Test1_2_13(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		String treeItem = null;
		Boolean prevStatus = null;

		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> 	testData= new ConcurrentHashMap <String, String>(dataValues);

		try {

			//Step-1: Launch configuration page
			driver.get(configSite);
			Log.message("1. Launched configuration page login page.", driver);

			//Step-2 : Login with system admin credentials
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToConfigurationUI(userName,password);
			Log.message("2. Logged in to Configuration page using admin credentials", driver);

			ConfigurationPage configPage=new ConfigurationPage(driver);
			//Verify if Login failed
			if (!configPage.getErrorMessage().isEmpty())
				throw new Exception("Unable to login to Configuration Page, with Error :"+configPage.getErrorMessage());

			//Step-3: Navigate to the General page in Web Access Configuration
			treeItem=testData.get("GeneralSettings");
			configPage.clickTreeViewItem(treeItem); //Selects General in the tree view of configuration page
			Log.message("3. Navigated to the General page in Web Access Configuration.", driver);

			//Step-4:  Enable Force M-Files user login in General page

			//Step-2 : Modify Force M-Files user login in General page and click reset button
			prevStatus = configPage.configurationPanel.getForceMFilesUserLogin(); //Gets M-Files user login

			//Modifies the settings in the configuration page
			if (prevStatus) 
				configPage.configurationPanel.setForceMFilesUserLogin(false);
			else 
				configPage.configurationPanel.setForceMFilesUserLogin(true);

			if (configPage.configurationPanel.getForceMFilesUserLogin()==(prevStatus)) {//Checks if settings are modified
				throw new Exception("Settings is not modified. ");
			}
			//Verification : To verify clicking Reset button resets the modified settings
			configPage.clickResetButton(); //Clicks reset button
			Log.message("4. 'Force M-Files user login' is modified and Reset button is clicked.", driver);

			//Verifies the modified settings are reset.
			if (configPage.configurationPanel.getForceMFilesUserLogin().equals(prevStatus))
				Log.pass("Test Passed. Reset button resets the modified settings successfully.");
			else
				Log.fail("Test Failed. Reset button does not resets the modified settings.", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			driver.quit();
		}

	} //End Test1_2_13

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_3_1",description="Test1_3_1: Last modified date and time in vault configuration settings")
	public void Test1_3_1(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		//		ConcurrentHashMap <String, String> testData= new ConcurrentHashMap <String, String>(dataValues);
		ConfigurationPage configPage=null;
		try {

			//Step-1: Launch configuration page
			driver.get(configSite);
			Log.message("1. Launched configuration page login page.", driver);

			//Instantiate configuration page
			LoginPage loginPage=new LoginPage(driver);

			//Step-2 : Login with system admin credentials
			loginPage.loginToConfigurationUI(userName,password);
			configPage=new ConfigurationPage(driver);
			Log.message("2. Loggedin with system admin credentials", driver);

			//Verify if Login failed
			if (!configPage.getErrorMessage().isEmpty())
				throw new Exception("Unable to login to Configuration Page, with Error :"+configPage.getErrorMessage());

			//Step-3: Navigate to the General page in Web Access Configuration
			Log.message("3. Navigate to the General page in Web Access Configuration.", driver);
			configPage.clickTreeViewItem(documentVault); //Selects General in the tree view of configuration page

			//Step-4:  Enable Force M-Files user login in General page
			Log.message("4.  Enable Force M-Files user login in General page", driver);

			//Step-2 : Select the 'Allow access to this vault'
			if (!configPage.configurationPanel.getVaultAccess()) 
				configPage.configurationPanel.setVaultAccess(true); //Sets Allow Access to this vault
			//			else
			//				configPage.configurationPanel.setVaultAccess(false); //Sets Allow Access to this vault

			driver.findElement(By.cssSelector("input[id='saveSettings']")).click();
			currDateTime=getTime();
			//Gets current date and time
			configPage.clickOKBtnOnSaveDialog();//click Ok button on Save dialog
			Log.message("5. Allowed access to this vault is selected and settings are saved.", driver);

			//			//Verification : To verify last modified date and time are right in the vault configuration page
			if (!documentVault.equals("My Vault")) 
				configPage.clickTreeViewItem("My Vault"); //Selects General in the tree view of configuration page
			else 
				configPage.clickTreeViewItem(documentVault); //Selects General in the tree view of configuration page

			configPage.clickTreeViewItem(documentVault);

			configPageTime=configPage.configurationPanel.getLastModifiedDateTime();
			//Verifies the time in vault configuration page is same as the system date and time at the time of modification
			if (configPageTime.compareTo(currDateTime)<=5 || configPageTime.compareTo(currDateTime)>=0)
				Log.pass("Test Passed. Last modified time gets updated as :"+configPageTime+" but Expected 'last modified Date' is :"+currDateTime);
			else
				Log.fail("Test Failed. Last modified time gets updated as :"+configPageTime+" but Expected 'last modified Date' is :"+currDateTime, driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {
				configPage=new ConfigurationPage(driver);
				if (!configPage.configurationPanel.getVaultAccess()) {
					configPage.configurationPanel.setVaultAccess(true);
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				} }

			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch
			finally {

				driver.quit();
			}}

	} //End Test1_3_1

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_3_2",description="Test1_3_2: Verify the Name of the vault in the vault configuration page")
	public void Test1_3_2(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		driver = WebDriverUtils.getDriver();
		

		//		ConcurrentHashMap <String, String> testData= new ConcurrentHashMap <String, String>(dataValues);

		try {
			//Step-1: Launch configuration page
			Log.message("1. Launch configuration page login page.", driver);
			driver.get(configSite);

			//Instantiate configuration page
			LoginPage loginPage=new LoginPage(driver);

			//Step-2 : Login with system admin credentials
			Log.message("2. Login with system admin credentials", driver);

			loginPage.loginToConfigurationUI(userName,password);
			ConfigurationPage configPage=new ConfigurationPage(driver);
			Log.message("3. Configuration page is launched and details provided for login", driver);

			//Verify if Login failed
			if (!configPage.getErrorMessage().isEmpty())
				throw new Exception("Unable to login to Configuration Page, with Error :"+configPage.getErrorMessage());

			//Step-3: click 'VaultName' folder in Web Access Configuration
			Log.message("4. Click 'VaultName' folder in Web Access Configuration.", driver);
			configPage.clickVaultFolder(documentVault); //Selects General in the tree view of configuration page

			//Step-4: Verifies if the name of vault in selected in tree view and in the page is correctly displayed
			Log.message("5. Verifies if the name of vault in selected in tree view and in the page is correctly displayed", driver);
			if (configPage.configurationPanel.getVaultName().toUpperCase().contains(documentVault.toUpperCase()))
				Log.pass("Test Passed. Selected Vault name is displayed as :"+configPage.configurationPanel.getVaultName()+" and expected 'VaultName' is :"+documentVault);
			else
				Log.fail("Test Failed. Selected Vault name is displayed as :"+configPage.configurationPanel.getVaultName()+" but expected 'VaultName' is :"+documentVault, driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			driver.quit();
		} //End finally

	} //End Test1_3_2

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_3_3",description="Test1_3_2: Verify the 'Unique ID' of the vault in the vault configuration page")
	public void Test1_3_3(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		driver = WebDriverUtils.getDriver();
		


		try {
			//Step-1: Launch configuration page
			driver.get(configSite);
			Log.message("1. Launched configuration page login page.", driver);

			//Step-2 : Login with system admin credentials
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToConfigurationUI(userName,password);
			ConfigurationPage configPage=new ConfigurationPage(driver);
			Log.message("2. Navigated to the Configuration page '"+userName+"' credentials ", driver);

			//Verify if Login failed
			if (!configPage.getErrorMessage().isEmpty())
				throw new Exception("Unable to login to Configuration Page, with Error :"+configPage.getErrorMessage());

			//Step-4: click 'VaultName' folder in Web Access Configuration
			configPage.clickVaultFolder(documentVault); //Selects General in the tree view of configuration page;
			Log.message("3. Clicked 'VaultName' folder in Web Access Configuration.", driver);

			//Step-5 : To verify whether unique ID of the vault displayed correctly
			if (configPage.configurationPanel.getVaultUniqueID().toUpperCase().trim().equals(vaultGUID.toUpperCase().trim()))
				Log.pass("Test Passed. Vault Unique ID is displayed as :"+configPage.configurationPanel.getVaultUniqueID()+" and expected UniqueID is :"+vaultGUID);
			else
				Log.fail("Test Failed. Vault Unique ID is displayed as :"+configPage.configurationPanel.getVaultUniqueID()+" but expected UniqueID is :"+vaultGUID, driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			driver.quit();
		} //End finally

	} //End Test1_3_3

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_3_4A",description="Test1_3_4A: Verify the 'Enable Allow access to this vault' setting")
	public void Test1_3_4A(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		driver = WebDriverUtils.getDriver();
		

		LoginPage loginPage=null;
		ConfigurationPage configPage=null;
		try {

			//Step-1: Launch configuration page
			driver.get(configSite);
			Log.message("1. Launched configuration page login page.", driver);

			//Instantiate configuration page
			loginPage=new LoginPage(driver);

			//Step-2 : Login with system admin credentials
			loginPage.loginToConfigurationUI(userName,password);
			configPage=new ConfigurationPage(driver);
			Log.message("2. Logged in with system admin credentials", driver);

			//Verify if Login failed
			if (!configPage.getErrorMessage().isEmpty())
				throw new Exception("Unable to login to Configuration Page, with Error :"+configPage.getErrorMessage());

			//Step-4: click 'VaultName' folder in Web Access Configuration
			configPage.clickVaultFolder(documentVault);//Selects General in the tree view of configuration page;
			Log.message("3. Clicked 'VaultName' folder in Web Access Configuration.", driver);

			//Step-5 : Select the 'Allow access to this vault'

			Thread.sleep(500);
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Sets Allow Access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}

			Utils.fluentWait(driver);
			if (!configPage.configurationPanel.getVaultAccess()) {
				throw new Exception("Some problem in enabling Vault access");
			}
			Log.message("4. Allowed access to this vault is selected.", driver);
			configPage.clickLogOut(); //Logs out from the Configuration page
			Log.message("5. Logged out from the Configuration page", driver);

			loginPage.navigateToApplication(webSite,userName,password,documentVault);	
			Utils.waitForPageLoad(driver);
			Log.message("6. LogIn to Web access page", driver);

			HomePage homePage = new HomePage(driver); //Instantiates HomePage wrapper
			//Step-8: Verifies URL has default.aspx page and the breadcrumb with the vault name
			if (driver.getCurrentUrl().toUpperCase().trim().contains("DEFAULT.ASPX") 
					&& homePage.getBreadcrumbText().toUpperCase().contains(documentVault.toUpperCase())) 
				Log.pass("Test Passed. Login to the vault is successful after allowing access to the vault.");
			else
				Log.fail("Test Failed. Login to the vault is not successful after allowing access to the vault", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try{
				loginPage=new LoginPage(driver);
				driver.get(configSite);
				configPage=new ConfigurationPage(driver);
				configPage.clickVaultFolder(documentVault);

				Thread.sleep(100);
				if (!configPage.configurationPanel.getVaultAccess()) {
					configPage.configurationPanel.setVaultAccess(true);
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				}}

			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch
			finally {
				driver.quit();	
			}	} //End finally

	} //End Test1_3_4A

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_3_4B",description="Test1_3_4B: Disable Allow access to this vault")
	public void Test1_3_4B(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		Boolean isVaultDisplayed=false;

		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData= new ConcurrentHashMap <String, String>(dataValues);
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;
		try {

			driver.get(configSite);
			Log.message("1. Launched Configuration Page login page", driver);
			loginPage=new LoginPage(driver);
			loginPage.loginToConfigurationUI(userName,password);
			configPage=new ConfigurationPage(driver);
			Log.message("2. Logged in to Configuration Page", driver);
			configPage.clickVaultFolder(documentVault);
			Log.message("3. Clicked the "+documentVault, driver);
			Thread.sleep(100);

			//Step-2 : DeSelect the 'Allow access to this vault'
			Thread.sleep(100);
			if (configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(false); //Sets Allow Access to this vault to false
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}
			Thread.sleep(100);
			if(configPage.configurationPanel.getVaultAccess()) {
				throw new Exception("Unable to 'Deny' the Vault Access permissions.");
			}
			Log.message("4. Allowed access to the vault :"+documentVault+" privilege is denied.", driver);
			//Step-3 : Login to the vault
			configPage.clickLogOut(); //Logs out from the Configuration page
			Log.message("5. Logged out from the Configuration page", driver);

			driver.get(webSite);
			loginPage.setUserName(testData.get("UserName"));
			loginPage.setPassword(testData.get("Password"));
			loginPage.clickLoginBtn();
			Log.message("6. Logged in to the Web Access page", driver);

			//Fetch the list of vaults displayed
			List<WebElement> vaultList=driver.findElements(By.cssSelector("div[id='vaults']>div[class*='vault']"));
			for(int i=0;i<vaultList.size();i++) {
				if (vaultList.get(i).equals(documentVault)) {
					//Log.exception(new Exception("User is still displayed with '"+documentVault+"' even after denying the permission"),driver);
					isVaultDisplayed=true;
				}
			}

			//Verifies URL has default.aspx page and the breadcrumb with the vault name
			if(!isVaultDisplayed) 
				Log.pass("Test  Passed. '"+documentVault+"' is not displayed for the user after denying the access to the vault.");
			else 
				Log.fail("Test Failed. User is still displayed with '"+documentVault+"' even after denying the permission", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {
				loginPage=new LoginPage(driver);
				driver.get(configSite);
				configPage=new ConfigurationPage(driver);
				configPage.clickVaultFolder(documentVault);

				Thread.sleep(500);
				if (!configPage.configurationPanel.getVaultAccess()) {
					configPage.configurationPanel.setVaultAccess(true);
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				}}

			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch
			finally {
				driver.quit();	
			}} //End finally

	} //End Test1_3_4B

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_3_5",description="Verify the display of 'Default view' in vault")
	public void Test1_3_5(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();

		ConcurrentHashMap <String, String> testData= new ConcurrentHashMap <String, String>(dataValues);
		String prevDefView=null;
		String viewName=null;
		LoginPage loginPage=null;
		HomePage homePage=null;
		ConfigurationPage configPage=null;
		String homeView="Home";
		

		try {
			driver.get(configSite);
			Log.message("1. Launched Configuration Page login page", driver);

			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.loginToConfigurationUI(userName,password);	
			configPage=new ConfigurationPage(driver);
			Log.message("2. Logged in to Configuration Page", driver);

			//Step-2: Click Vault from left panel of Configuration Page
			configPage.clickVaultFolder(documentVault);
			Log.message("3. Clicked Vault folder in Configuration Page", driver);

			//Step-3 : Set default view for the vault and save the changes
			prevDefView = configPage.configurationPanel.getDefaultView(); //Gets the status of the allow access to this vault
			Log.message("4. Default view displayed as :"+prevDefView, driver);

			if(!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true);
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess()) 
					throw new Exception("Access to "+documentVault+ "not saved");
			}

			Log.message("Verified access to '"+documentVault + "' and confirmed it is set to 'True'.", driver);

			if (!prevDefView.toUpperCase().trim().equalsIgnoreCase(testData.get("ViewName").toUpperCase().trim())){
				configPage.configurationPanel.setDefaultView(testData.get("ViewName")); //Sets Allow Access to this vault to false
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();//Save changes

			}
			Log.message("5. '"+testData.get("ViewName") + "' is set as Default view for the vault.", driver);

			//Step-4 : LogOut of Configuration Page
			configPage.clickLogOut(); //Logs out from the Configuration page
			//Step-5: Login to Web Access Site
			loginPage.loginToWebApplication(userName, password, documentVault);
			Utils.waitForPageLoad(driver);
			homePage=new HomePage(driver);
			//verify if user logged in
			if (!homePage.isLoggedIn(testData.get("UserName"))) 
				throw new Exception("Login to the vault is un-successful.");

			Log.message("6. Logged in to the vault (" + documentVault + ").", driver);
			//Step-6:  Verify whether the correct default view is displayed after successful login to Web Access Site
			viewName=homePage.getBreadcrumbText();

			if(testData.get("ViewName").trim().equalsIgnoreCase(homeView.trim())) 
				viewName=documentVault;
			else
				viewName=viewName.split("\n")[1];

			Log.message("7. Default view is displayed as :"+viewName+" after successful login to Web Access Site", driver);

			if (viewName.toUpperCase().contains(viewName.toUpperCase()))
				Log.pass("Test Passed. '" + viewName + "' is the default view.");
			else 
				Log.fail("Test Failed. '" + viewName+ "' is not the default view.", driver);

		} //End try

		catch (Exception e) {
			if(e.getClass().toString().contains("ArrayIndexOutOfBoundsException"))
				throw new Exception("Default View is not displayed as '"+testData.get("ViewName")+"', but displayed as '"+viewName);
			else
				Log.exception(e, driver);
		} //End catch

		finally {
			try {
				homePage=new HomePage(driver);
				homePage.menuBar.logOutFromMenuBar();
				loginPage=new LoginPage(driver);
				loginPage.navigateToApplication(configSite,userName,password,"");	
				configPage=new ConfigurationPage(driver);
				configPage.clickVaultFolder(documentVault);

				if(!configPage.configurationPanel.getDefaultView().equals("Home")) {
					configPage.configurationPanel.setDefaultView("Home");
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				}
			}
			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally
		} //End finally

	} //End Test1_3_5

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_3_6_1",description="Test1_3_6_1: Verify the display of 'Default Layout' in vault")
	public void Test1_3_6_1(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData = new ConcurrentHashMap <String, String>(dataValues);
		String prevLayout=null;
		LoginPage loginPage=null;

		ConfigurationPage configPage=null;
		HomePage homepage=null;
		try {

			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");	
			Log.message("1. Logged in to Configuration Page", driver);

			//Step-2: Click Vault from left panel of Configuration Page
			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);
			Log.message("2. Clicked 'Sample vault' from left panel of Configuration Page", driver);


			//Step-3 : Set layout for the vault 
			prevLayout = configPage.configurationPanel.getLayout(); //Gets the layout selected
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
			}
			if (!prevLayout.trim().equalsIgnoreCase(testData.get("Layout"))) {
				configPage.configurationPanel.setLayout(testData.get("Layout")); //Sets Default as layout for the vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}

			//Checks if vault configuration settings has been saved properly
			if (!configPage.configurationPanel.getLayout().equalsIgnoreCase(testData.get("Layout"))) {
				throw new Exception("Vault is not modified to default layout after saving");
			}
			Log.message("3. '"+testData.get("Layout")+" is set as layout for the vault.", driver);

			//Step-4: LogOut of configurationpage
			configPage.clickLogOut(); //Logs out from the Configuration page
			Log.message("4. Logged Out of configuration Page.", driver);

			//Step-5: Login to Web Access Vault
			loginPage.loginToWebApplication(userName, password, documentVault);
			Utils.waitForPageLoad(driver);
			homepage=new HomePage(driver);

			if (!homepage.isLoggedIn(userName))
				throw new Exception("Some Error encountered while logging..check testdata..");
			Log.message("5. Logged in to the vault (" + documentVault + ").", driver);

			//Step-8: Verify if applet is enabled in the default layout
			if (homepage.taskPanel.isAppletEnabled(currentDriver) || (homepage.isTaskPaneDisplayed() && homepage.isSearchbarPresent() && homepage.isRightPaneDisplayed())) 
				Log.pass("Test Passed. In Default Layout, Applet is enabled, Tree view and task pane are displayed.");
			else
				Log.fail("Test Failed. In Default layout Applet is not enabled", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {
				driver.get(configSite);
				configPage=new ConfigurationPage(driver);
				configPage.clickVaultFolder(documentVault);

				if (!configPage.configurationPanel.getLayout().equalsIgnoreCase("Default layout")) {
					configPage.configurationPanel.setLayout("Default layout");
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				}
				driver.quit();
			}
			catch (Exception e) 
			{
				Log.exception(e, driver);
				driver.quit();				
			}
		} //End finally

	} //End Test1_3_6_1

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_3_6_2",description="Test1_3_6_2 :Verify the Layout 'Verify 'No Java applet' in vault")
	public void Test1_3_6_2(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData= new ConcurrentHashMap <String, String>(dataValues);
		String prevLayout=null;
		LoginPage loginPage=null;
		HomePage homePage=null;
		ConfigurationPage configPage=null;


		try {

			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");	
			Log.message("1. Logged in to Configuration Page", driver);

			//Step-2: Click Vault from left panel of Configuration Page
			configPage=new ConfigurationPage(driver);
			Thread.sleep(200);
			configPage.clickVaultFolder(documentVault);
			Log.message("2. Clicked '"+documentVault+"' from left panel of Configuration Page", driver);

			//Step-2 : Set layout for the vault as default
			prevLayout = configPage.configurationPanel.getLayout(); //Gets the layout selected
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
			}
			if ((!prevLayout.trim().equalsIgnoreCase(testData.get("Layout"))) || (configPage.configurationPanel.isJavaAppletEnabled())) {
				configPage.configurationPanel.setLayout(testData.get("Layout")); //Sets Default as layout for the vault
				configPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}
			Log.message("3. Set 'No Java Applet Layout' layout as default in the vault", driver);
			//Checks if vault configuration settings has been saved properly
			if (!configPage.configurationPanel.getLayout().equalsIgnoreCase(testData.get("Layout"))) {
				throw new Exception("Test Failed. Vault is not modified to No Java applet layout after saving");
			}

			Log.message("4. "+testData.get("Layout")+" is set as layout for the vault.", driver);

			//Step-3 : Login to the vault
			configPage.clickLogOut(); //Logs out from the Configuration page
			Log.message("5. Logged out from the Configuration page", driver);
			loginPage.loginToWebApplication(userName, password, documentVault);
			Utils.waitForPageLoad(driver);
			homePage=new HomePage(driver);

			if (!homePage.isLoggedIn(userName)) {
				throw new Exception("Login to the vault is un-successful.");
			}

			Log.message("6. Logged in to the vault (" + documentVault+ ").", driver);

			//Verification : To verify default layout exists in MFWA default page
			TaskPanel taskPanel = new TaskPanel(driver); //Instantiate TaskPanel wrapper
			if (!homePage.isTaskPaneDisplayed()) { //Verifies Taskpane is displayed in the default layout
				throw new Exception("In No Java applet layout Task Panel is not displayed.");
			}
			//Verifies applet is enabled in the default layout
			if (!taskPanel.isAppletEnabled(currentDriver)) 
				Log.pass("Test Passed. Applet is not enabled in No Applet Layout.");
			else
				Log.fail("Test Failed. Test case Failed. In No Applet Layout Applet is enabled", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try{
				homePage=new HomePage(driver);
				homePage.menuBar.logOutFromMenuBar();
				loginPage=new LoginPage(driver);
				driver.get(configSite);
				loginPage.navigateToApplication(configSite,userName,password,"");	
				configPage=new ConfigurationPage(driver);
				configPage.clickVaultFolder(documentVault);

				if (!configPage.configurationPanel.getLayout().equalsIgnoreCase("Default layout")) {
					configPage.configurationPanel.setLayout("Default layout");
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				}
			}
			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally
		} //End finally

	} //End ConfigurationUI1_3_6_2

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_3_6_3",description="Test1_3_6_3: Verify the Layout 'Verify 'No Java applet and no task area' in vault")
	public void Test1_3_6_3(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData= new ConcurrentHashMap <String, String>(dataValues);
		String prevLayout=null;
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;
		String defaultView="Home";

		try {

			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");	
			Log.message("1. Logged in to Configuration Page", driver);

			//Step-2: Click Vault from left panel of Configuration Page
			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);

			Log.message("2. Select the "+documentVault+" to modify the settings", driver);

			//Step-3 : Set layout for the vault as 'No Java applet, no task area'
			prevLayout = configPage.configurationPanel.getLayout(); //Gets the layout selected
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true);
			}

			if (!configPage.configurationPanel.getDefaultView().trim().equalsIgnoreCase(defaultView.trim())) {
				configPage.configurationPanel.setDefaultView(defaultView);
			}

			if (!prevLayout.equalsIgnoreCase(testData.get("Layout"))) {
				configPage.configurationPanel.setLayout(testData.get("Layout")); //Sets the layout for the vault
			}
			configPage.clickSaveButton();
			configPage.clickOKBtnOnSaveDialog();

			Log.message("3. Set layout for the vault as :"+testData.get("Layout"), driver);

			//Step-4: Checks if vault configuration settings has been saved properly
			if (!configPage.configurationPanel.getLayout().toUpperCase().equalsIgnoreCase(testData.get("Layout").toUpperCase())) {
				throw new Exception("Vault is not modified to 'No Java applet, no task area' , but displayed as '"+configPage.configurationPanel.getLayout()+"'");
			}
			Log.message("4. "+testData.get("Layout")+" 'No Java applet, no task area' is set as layout for the vault.", driver);

			//Step-5 : LogOut of configurationPage
			configPage.clickLogOut(); //Logs out from the Configuration page
			Log.message("5. Logged Out of configurationPage", driver);

			//Step-6 : Login to Web Access Vault
			loginPage.loginToWebApplication(userName, password, documentVault);
			Utils.waitForPageLoad(driver);
			HomePage homePage=new HomePage(driver);
			if (!homePage.isLoggedIn(userName)) {
				throw new Exception("Login to the vault is un-successful.");
			}
			Log.message("6. Logged in to the Web Access with vault (" + documentVault + ").", driver);

			//Step-7: Verifies Applet is disabled
			if (!homePage.isTaskPaneDisplayed()) 
				Log.pass("Test Passed. In 'No task area' layout task area are not enabled.");
			else
				Log.fail("Test Failed. In 'No task area' layout task area is enabled.", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try{
				driver.get(configSite);
				loginPage=new LoginPage(driver);
				configPage=new ConfigurationPage(driver);
				configPage.clickVaultFolder(documentVault);

				if (!configPage.configurationPanel.getLayout().equalsIgnoreCase("Default layout")) {
					configPage.configurationPanel.setLayout("Default layout");
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				}
			}
			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally
		} //End finally

	} //End Test1_3_6_3

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_3_6_4",description="Test1_3_6_4: Verify the Layout 'No Java applet, no task area, but show Go To shortcuts")
	public void Test1_3_6_4(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {
		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData= new ConcurrentHashMap <String, String>(dataValues);
		String prevLayout=null;
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;
		HomePage homePage=null;
		String defaultView="Home";
		try {

			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");	
			Log.message("1. Logged in to Configuration Page", driver);

			//Step-2: Click Vault from left panel of Configuration Page
			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);
			Log.message("2. Clicked 'Sample vault' from left panel of Configuration Page", driver);


			//Step-2 : Set layout for the vault as default
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
			}

			if (!configPage.configurationPanel.getDefaultView().trim().equalsIgnoreCase(defaultView.trim())) {
				configPage.configurationPanel.setDefaultView(defaultView);
			}

			prevLayout = configPage.configurationPanel.getLayout(); //Gets the layout selected
			if (!prevLayout.trim().equalsIgnoreCase(testData.get("Layout"))) {
				configPage.configurationPanel.setLayout(testData.get("Layout")); //Sets the layout for the vault

			}

			configPage.clickSaveButton();
			Utils.fluentWait(driver);
			configPage.clickOKBtnOnSaveDialog();
			if (prevLayout.trim().equalsIgnoreCase(testData.get("Layout")))
				throw new Exception("Unable to save the layout settings");

			Log.message("3. Task area with Go To shortcuts' Layout is set as Default Layout", driver);
			//Checks if vault configuration settings has been saved properly
			if (!configPage.configurationPanel.getLayout().equalsIgnoreCase(testData.get("Layout"))) {
				throw new Exception("Vault is not modified to 'No Java applet, no task area, but show Go To shortcuts' layout after saving");
			}
			Log.message("4. '"+testData.get("Layout")+"', but show Go To shortcuts' is set as layout for the vault.", driver);

			//Step-3 : Logout of configuration page
			configPage.clickLogOut(); //Logs out from the Configuration page
			Log.message("5. Logged out of configuration page", driver);

			//Step-4 : Login to the Web Access vault
			loginPage.loginToWebApplication(userName, password, documentVault);
			Utils.fluentWait(driver);
			homePage=new HomePage(driver);
			if (!homePage.isLoggedIn(userName)) {
				throw new Exception("Login to the vault is un-successful.");
			}
			Log.message("6. Logged in to the vault (" +documentVault+ ").", driver);

			//Step-5: Verify if Taskpane displayed in the Vault

			if (homePage.isTaskPaneDisplayed()) { 
				throw new Exception(" In 'No Java applet, no task area, but show Go To shortcuts' layout Task Panel is displayed.");
			}
			Log.message("7. Taskpane not displayed in the Vault as expected.", driver);

			//Step-7. Verify if Task Panel Goto shortcuts displayed
			if (homePage.getGotoItemList(driver).size()>1)
				Log.pass("Test Passed. Taskpane GoTo items is displayed in Task area with Go To shortcuts' only.");
			else
				Log.fail("Test Failed. Taskpane GoTo items is not displayed in Task area with Go To shortcuts' only.", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch 

		finally {
			try {
				homePage=new HomePage(driver);
				homePage.menuBar.logOutFromMenuBar();
				loginPage=new LoginPage(driver);
				loginPage.navigateToApplication(configSite,userName,password,"");	
				driver.get(configSite);
				configPage=new ConfigurationPage(driver);
				configPage.clickVaultFolder(documentVault);

				if (!configPage.configurationPanel.getLayout().equalsIgnoreCase("Default layout")) {
					configPage.configurationPanel.setLayout("Default layout");
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				} }
			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally
		} //End finally

	} //End Test1_3_6_4


	//	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_3_6_6",description="Test1_3_6_6: Verify the Layout 'Default layout with navigation pane'")
	//	public void Test1_3_6_5(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {
	//		//Variable Declaration
	//		driver = WebDriverUtils.getDriver();
	//		Verify the Layout 'Listing pane and properties pane only'");
	//		
	//		ConcurrentHashMap <String, String> testData= new ConcurrentHashMap <String, String>(dataValues);
	//		String prevLayout=null;
	//		LoginPage loginPage=null;
	//		ConfigurationPage configPage=null;
	//		
	//		HomePage homePage=null;
	//	//	Boolean isError=false;
	//		try {
	//			
	//			//Step-1: Login to Configuration Page
	//			loginPage=new LoginPage(driver);
	//			loginPage.navigateToApplication(configSite,userName,password,"");	
	//			Log.message("1. Logged in to Configuration Page", driver);
	//			
	//			//Step-2: Click Vault from left panel of Configuration Page
	//			configPage=new ConfigurationPage(driver);
	//			configPage.clickVaultFolder(documentVault);
	//			Log.message("2. Clicked 'Sample Vault' from left panel of Configuration Page");
	//			
	//			
	//			//Step-2 : Set layout for the vault as default
	//			prevLayout = configPage.configurationPanel.getLayout(); //Gets the layout selected
	//			if (!configPage.configurationPanel.getVaultAccess()) {
	//				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
	//			}
	//			
	//			if (!prevLayout.trim().equalsIgnoreCase(testData.get("Layout"))) {
	//				configPage.configurationPanel.setLayout(testData.get("Layout")); //Sets the layout for the vault
	//				configPage.clickSaveButton();
	//				configPage.clickOKBtnOnSaveDialog();
	//			}
	//			Log.message("3.'"+testData.get("Layout")+"', but show Go To shortcuts' is set as layout for the vault.");
	//			//Step-3 : Login to the vault
	//			configPage.clickLogOut(); //Logs out from the Configuration page
	//			Log.message("4. LoggedOut from configuration page.");
	//			
	//			loginPage.loginToWebApplication(userName, password, documentVault);
	//			Utils.waitForPageLoad(driver);
	//			homePage=new HomePage(driver);
	//					
	//			Log.message("5. Logged in to Web Access using the vault (" +documentVault+ ").");
	//			if(homePage.isSearchbarPresent()) 
	//				throw new Exception("Search pane is displayed");
	//			
	//			//Verification : To verify default layout exists in MFWA default page
	//			if (homePage.isTaskPaneDisplayed())  //Verifies Taskpane is displayed
	//				throw new Exception("Task Panel is displayed.");
	//			
	//			
	//			if(homePage.isRightPaneDisplayed()) 
	//				throw new Exception("In 'Default & navigation pane' layout  RightPane Metacard is not displayed.");
	//			
	//			if(homePage.isMetadataDisplayed()) 
	//				throw new Exception("In 'Default & navigation pane' layout  Metadata is not displayed.");
	//			
	//			//Verifies Tree view icon is not displayed
	//			if (homePage.isListViewDisplayed()) 
	//				Log.pass("Test Passed. 'Listing pane' layout is enabled successfully.");
	//			else
	//				Log.fail("Test Failed. Unable to display 'Listing pane' layout", driver);
	//			
	//		} //End try
	//		
	//		catch (Exception e) {
	//			Log.exception(e, driver);
	//		} //End catch
	//		
	//		finally {
	//			// homePage.menuBar.logOutFromMenuBar();
	//			homePage.listView.rightClickItemByIndex(1);
	//			homePage.listView.clickContextMenuItem("Log Out");
	//			Utils.waitForPageLoad(driver);
	//			loginPage=new LoginPage(driver);
	//			loginPage.navigateToApplication(configSite,userName,password,"");
	//			configPage=new ConfigurationPage(driver);
	//			configPage.clickVaultFolder(documentVault);
	//			
	//			if (!configPage.configurationPanel.getLayout().equalsIgnoreCase("Default layout")) {
	//				configPage.configurationPanel.setLayout("Default layout");
	//				configPage.clickSaveButton();
	//				configPage.clickOKBtnOnSaveDialog();
	//			}
	//			
	//			driver.quit();	
	//		} //End finally
	//		
	//	} //End Test1_3_6_5
	//	
	//	
	//	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_3_6_6",description="Test1_3_6_6: Verify the Layout 'Default layout with navigation pane'")
	//	public void Test1_3_6_6(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {
	//		//Variable Declaration
	//		driver = WebDriverUtils.getDriver();
	//		Verify the Layout 'Listing pane and properties pane only'");
	//		
	//		ConcurrentHashMap <String, String> testData= new ConcurrentHashMap <String, String>(dataValues);
	//		String prevLayout=null;
	//		LoginPage loginPage=null;
	//		ConfigurationPage configPage=null;
	//		
	//		HomePage homePage=null;
	//	//	Boolean isError=false;
	//		try {
	//			
	//			//Step-1: Login to Configuration Page
	//			loginPage=new LoginPage(driver);
	//			loginPage.navigateToApplication(configSite,userName,password,"");	
	//			Log.message("1. Logged in to Configuration Page", driver);
	//			
	//			//Step-2: Click Vault from left panel of Configuration Page
	//			configPage=new ConfigurationPage(driver);
	//			configPage.clickVaultFolder(documentVault);
	//			Log.message("2. Clicked 'Sample Vault' from left panel of Configuration Page");
	//			
	//			
	//			//Step-2 : Set layout for the vault as default
	//			prevLayout = configPage.configurationPanel.getLayout(); //Gets the layout selected
	//			if (!configPage.configurationPanel.getVaultAccess()) {
	//				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
	//			}
	//			
	//			if (!prevLayout.trim().equalsIgnoreCase(testData.get("Layout"))) {
	//				configPage.configurationPanel.setLayout(testData.get("Layout")); //Sets the layout for the vault
	//				configPage.clickSaveButton();
	//				configPage.clickOKBtnOnSaveDialog();
	//			}
	//			Log.message("3.'"+testData.get("Layout")+"', but show Go To shortcuts' is set as layout for the vault.");
	//			//Step-3 : Login to the vault
	//			configPage.clickLogOut(); //Logs out from the Configuration page
	//			Log.message("4. LoggedOut from configuration page.");
	//			
	//			loginPage.loginToWebApplication(userName, password, documentVault);
	//			Utils.waitForPageLoad(driver);
	//			homePage=new HomePage(driver);
	//					
	//			Log.message("5. Logged in to Web Access using the vault (" +documentVault+ ").");
	//			if(homePage.isSearchbarPresent()) 
	//				throw new Exception("Search pane is displayed");
	//			
	//			//Verification : To verify default layout exists in MFWA default page
	//			if (homePage.isTaskPaneDisplayed())  //Verifies Taskpane is displayed
	//				throw new Exception("Task Panel is displayed.");
	//			
	//			
	//			if(!homePage.isRightPaneDisplayed()) 
	//				throw new Exception("In 'Default & navigation pane' layout  RightPane Metacard is not displayed.");
	//			
	//			
	//			//Verifies Tree view icon is not displayed
	//			if (homePage.isListViewDisplayed() && homePage.isMetadataDisplayed()) 
	//				Log.pass("Test Passed. 'Listing and properties pane' layout is enabled successfully.");
	//			else
	//				Log.fail("Test Failed. Unable to display Listing and properties pane' layout", driver);
	//			
	//		} //End try
	//		
	//		catch (Exception e) {
	//			Log.exception(e, driver);
	//		} //End catch
	//		
	//		finally {
	//			// homePage.menuBar.logOutFromMenuBar();
	//			homePage.listView.rightClickItemByIndex(1);
	//			homePage.listView.clickContextMenuItem("Log Out");
	//			Utils.waitForPageLoad(driver);
	//			loginPage=new LoginPage(driver);
	//			loginPage.navigateToApplication(configSite,userName,password,"");
	//			configPage=new ConfigurationPage(driver);
	//			configPage.clickVaultFolder(documentVault);
	//			
	//			if (!configPage.configurationPanel.getLayout().equalsIgnoreCase("Default layout")) {
	//				configPage.configurationPanel.setLayout("Default layout");
	//				configPage.clickSaveButton();
	//				configPage.clickOKBtnOnSaveDialog();
	//			}
	//			
	//			driver.quit();	
	//		} //End finally
	//		
	//	} //End Test1_3_6_6
	//	
	//	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_3_6_7",description="Test1_3_6_7: Verify Layout 'No navigation pane, no Java applet, no task area'")
	//	public void Test1_3_6_7(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {
	//	
	//	//Variable Declaration
	//	driver = WebDriverUtils.getDriver();
	//	Verify Layout 'No navigation pane, no Java applet, no task area'");
	//	
	//	ConcurrentHashMap <String, String> testData= new ConcurrentHashMap <String, String>(dataValues);
	//	String prevLayout=null;
	//	
	//	try {
	//		
	//		//Step-1: Login to Configuration Page
	//		LoginPage loginPage=new LoginPage(driver);
	//		loginPage.navigateToApplication(configSite,userName,password,"");
	//		Log.message("1. Logged in to Configuration Page", driver);
	//		
	//		//Step-2: Click Vault from left panel of Configuration Page
	//		ConfigurationPage configPage=new ConfigurationPage(driver);
	//		configPage.clickVaultFolder(documentVault);
	//		Log.message("2. Clicked 'Sample vault' from left panel of Configuration Page");
	//		
	//		
	//		if (!configPage.configurationPanel.getVaultAccess()) {
	//			configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
	//		}
	//		//Step-2 : Set layout for the vault as default
	//		prevLayout = configPage.configurationPanel.getLayout(); //Gets the layout selected
	//		if (!prevLayout.trim().equalsIgnoreCase(testData.get("Layout"))) {
	//			configPage.configurationPanel.setLayout("No navigation pane, no Java applet, no task area"); //Sets the layout for the vault
	//			configPage.configurationPanel.saveSettings();
	//		}
	//						
	//		//Checks if vault configuration settings has been saved properly
	//		if (!configPage.configurationPanel.getLayout().equalsIgnoreCase("NO NAVIGATION PANE, NO JAVA APPLET, NO TASK AREA")) {
	//			throw new Exception("Vault is not modified to 'No navigation pane, no Java applet, no task area' layout after saving");
	//		}
	//		
	//		Log.message("3. 'No navigation pane, no Java applet, no task area' is set as layout for the vault.");
	//		
	//		//Step-3 : Login to the vault
	//		configPage.clickLogOut(); //Logs out from the Configuration page
	//		Log.message("4. Logged Out from configuration Page");
	//
	//		loginPage.navigateToApplication(webSite,userName,password,documentVault);	
	//		Utils.waitForPageLoad(driver);
	//		Log.message("5. Logged in to the vault (" +documentVault + ").");
	//		
	//		//Verification : To verify default layout exists in MFWA default page
	//		HomePage homePage=new HomePage(driver);//Instantiate HomePage wrapper
	//		if (homePage.isTaskPaneDisplayed()) { //Verifies Taskpane is displayed
	//			throw new Exception("In 'No navigation pane, no Java applet, no task area' layout Task Panel is displayed.");
	//		}
	//	
	//		TaskPanel taskpanel = new TaskPanel(driver); //Instantiate TaskPanel wrapper
	//		if (taskpanel.isAppletEnabled(driverType)) { //Verifies Java Applet is enabled
	//			throw new Exception("In 'No navigation pane, no Java applet, no task area' layout applet is enabled.");
	//		}
	//		
	//		MenuBar menuBar = new MenuBar(driver); //Instantiates MenuBar wrapper
	//		//Verifies Tree view icon is not displayed
	//		if (!menuBar.isTreeViewBtnDisplayed()) {
	//			isSuccess=true;
	//		}
	//		
	//		menuBar.logOutFromMenuBar();
	//		loginPage.navigateToApplication(configSite,userName,password,"");
	//		configPage.clickVaultFolder(documentVault);
	//		if (!configPage.configurationPanel.getVaultAccess()) {
	//			configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
	//		}
	//		if (!configPage.configurationPanel.getLayout().equalsIgnoreCase("Default")) {
	//			configPage.configurationPanel.setLayout("Default");
	//			configPage.configurationPanel.saveSettings();
	//		}
	//		
	//		if(isSuccess)
	//			Log.pass("Test Passed. Show Tree view icon to show tree view is not displayed in 'No navigation pane, no Java applet, no task area' layout.");
	//		else
	//			Log.fail("Test Failed. Show Tree view icon to show tree view is displayed in 'No navigation pane, no Java applet, no task area' layout", driver);
	//		
	//	} //End try
	//	
	//	catch (Exception e) {
	//		Log.exception(e, driver);
	//	} //End catch
	//	
	//	finally {
	//		driver.quit();	
	//	} //End finally
	//	
	//	} //End Test1_3_6_7

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_3_6_8",description="Test1_3_6_8: Verify the Layout 'Listing pane and properties pane only'")
	public void Test1_3_6_8(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {
		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData= new ConcurrentHashMap <String, String>(dataValues);
		String prevLayout=null;
		Boolean isDisplayed=false;
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;

		HomePage homePage=null;
		try {

			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");	
			Log.message("1. Logged in to Configuration Page", driver);

			//Step-2: Click Vault from left panel of Configuration Page
			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);
			Log.message("2. Clicked 'Sample Vault' from left panel of Configuration Page", driver);


			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess()) 
					throw new Exception("Error while allowing access to Vault");
			}
			//Step-2 : Set layout for the vault as default
			prevLayout = configPage.configurationPanel.getLayout(); //Gets the layout selected
			if (!prevLayout.trim().equalsIgnoreCase(testData.get("Layout"))) {
				configPage.configurationPanel.setLayout(testData.get("Layout").trim()); //Sets the layout for the vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}


			//Checks if vault configuration settings has been saved properly
			if (!configPage.configurationPanel.getLayout().toUpperCase().equalsIgnoreCase(testData.get("Layout").toUpperCase())) {
				throw new Exception("Vault is not modified to 'Listing pane and properties pane only', but with '"+configPage.configurationPanel.getLayout());
			}
			Log.message("3. 'Listing pane and properties pane only' is set as layout for the vault.", driver);

			//Make sure 'Advanced search' option enabled
			configPage.expandVaultFolder(documentVault);
			Log.message("4. Clicked '"+documentVault+"' from left panel of Configuration Page", driver);

			configPage.clickSettingsFolder("Controls");
			if (configPage.configurationPanel.getVaultCommands("Context menu").equalsIgnoreCase("Hide")) {
				configPage.chooseConfigurationVaultSettings(driver, "Context menu","controls","Show");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}
			if(!configPage.configurationPanel.getVaultCommands("Context menu").equalsIgnoreCase("Show"))
				throw new Exception("Unable to save 'Context menu' Show property.");
			else
				Log.message("5. Show 'Context menu' is enabled and settings are saved.", driver);

			//Step-3 : Logs out from the Configuration page
			configPage.clickLogOut(); //
			Log.message("6. Logged out from the Configuration page", driver);

			//Step-4: Login to Web Access
			loginPage.loginToWebApplication(userName, password, documentVault);
			Utils.waitForPageLoad(driver);
			Log.message("7. Logged in to Web Access", driver);

			homePage = new HomePage(driver);

			if(homePage.isListViewDisplayed()){
				Log.message("8. Listing Pane displayed", driver);
				isDisplayed=true;
			}
			else
				throw new Exception("Listing Pane does not displayed.");

			homePage= new HomePage(driver);
			while(!homePage.isDataInListView(driver,testData.get("SearchTerm"),"Name")) {
				if(!homePage.listView.doubleClickItemByIndex(0))
					throw new Exception("No Object in the current view");
				else {
					Thread.sleep(200);
					if(homePage.isDataInListView(driver,testData.get("SearchTerm"),"Name"))
						break;
				}
			}

			Log.message("9. Searched Object found in listview", driver);
			if(!homePage.openContextMenuDialog(driver))
				throw new Exception("Unable to open context menu");
			else {
				Thread.sleep(800);
				homePage.selectContextMenuItemFromListView("Properties");
				Log.message("10. Selected 'Properties' context option for the Object found in listview", driver);
			}

			Thread.sleep(2500);
			Boolean isError=true;
			//Step 5: Verify if 'Properties Pane' is displayed, when selecting 'Listing pane and properties pane only' layout
			MFilesDialog mfilesDialog=new MFilesDialog(driver);
			if (MFilesDialog.exists(driver)) { //Verifies Properties pane is displayed {
				Log.message("Properties dialog displayed", driver);
				mfilesDialog.close();
				isError=false;
			}

			//Step-6. Verify if 'List View' is displayed, when selecting 'Listing pane and properties pane only' layout
			if (isDisplayed && !isError) 
				Log.pass("Test Passed. List View and Properties Pane displayed in 'Listing pane and properties pane only' layout.");
			else
				Log.fail("Test Failed. List View and Properties Pane is not displayed in 'Listing pane and properties pane only' layout.", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try
			{
				driver.get(configSite);
				configPage=new ConfigurationPage(driver);
				configPage.clickVaultFolder(documentVault);

				if (!configPage.configurationPanel.getLayout().equalsIgnoreCase("Default layout")) {
					configPage.configurationPanel.setLayout("Default layout");
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				}
			}

			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally
		} //End finally


	} //End Test1_3_6_8

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_3_6_9",description="Test1_3_6_9: Verify the Layout 'Listing pane only'")
	public void Test1_3_6_9(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData= new ConcurrentHashMap <String, String>(dataValues);
		String prevLayout=null;
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;


		try {

			//Step-1: Login to Configuration Page
			Log.message("1. Login to Configuration Page", driver);
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");	

			//Step-2: Click Vault from left panel of Configuration Page
			Log.message("2. Click 'Sample vault' from left panel of Configuration Page", driver);
			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);

			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess()) 
					throw new Exception("Access to '"+documentVault+"' not saved");

			}			//Step-3 : Set layout for the vault as default
			Log.message("3 Set layout for the vault as 'Listing Pane Only'", driver);
			prevLayout = configPage.configurationPanel.getLayout(); //Gets the layout selected
			if (!prevLayout.trim().equalsIgnoreCase(testData.get("Layout"))) {
				configPage.configurationPanel.setLayout("Listing area only"); //Sets the layout for the vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}

			//Step-4: Checks if vault configuration settings has been saved properly
			Log.message("4. Checks if vault configuration settings has been saved properly", driver);
			if (!configPage.configurationPanel.getLayout().equalsIgnoreCase("LISTING AREA ONLY")) {
				throw new Exception("Test case Failed. Vault is not modified to 'Listing pane only' layout after saving");
			}

			Log.message("5. 'Listing pane only' is set as layout for the vault.", driver);

			//Step-5: LogOUt of COnfigurationPage
			configPage.clickLogOut(); //Logs out from the Configuration page
			Log.message("5. Logged Out of COnfigurationPage", driver);

			//Step-6: Login to Web Access Application
			loginPage.loginToWebApplication(userName, password, documentVault);
			Utils.waitForPageLoad(driver);
			Log.message("6. Logged in to the vault (" +documentVault+ ").", driver);

			//Step-7: Verifies Properties pane is displayed
			PropertiesPane	propPane = new PropertiesPane(driver); //Instantiating PropertiesPane wrapper class
			if (propPane.isPropertyPaneExists()) {
				throw new Exception("In 'Listing pane only' layout properties pane is displayed.");
			}
			Log.message("7. Properties pane is not displayed, when selecting 'Listing pane only' layout", driver);

			//Step-8 : Verify if List View is displayed
			HomePage homePage = new HomePage(driver);
			Utils.fluentWait(driver);

			if (homePage.isListViewDisplayed())
				Log.pass("Test Passed. List View is displayed in 'Listing pane only' layout.");
			else
				Log.fail("Test Failed. List View is not displayed in 'Listing pane only' layout.", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {
				driver.get(configSite);
				configPage=new ConfigurationPage(driver);
				configPage.clickVaultFolder(documentVault);

				if (!configPage.configurationPanel.getVaultAccess()) {
					configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				}
				if (!configPage.configurationPanel.getLayout().equalsIgnoreCase("Default layout")) {
					configPage.configurationPanel.setLayout("Default layout");
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				}
			}
			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally
		} //End finally

	} //End ConfigurationUI1_3_6_9

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_3_8",description="Test1_3_8: Default search criteria: Retain latest selection done by user")
	public void Test1_3_8(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData= new ConcurrentHashMap <String, String>(dataValues);
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;

		HomePage homePage=null;
		try {

			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");	
			Log.message("1. Logged in to Configuration Page", driver);

			//Step-2: Click Vault from left panel of Configuration Page
			configPage=new ConfigurationPage(driver);


			//Make sure 'Advanced search' option enabled
			configPage.expandVaultFolder(documentVault);
			configPage.clickSettingsFolder("Controls");

			if (!configPage.configurationPanel.getVaultCommands("Advanced Search").equalsIgnoreCase("Show")) {
				configPage.chooseConfigurationVaultSettings(driver, "Advanced Search","controls","Show");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}

			configPage.clickVaultFolder(documentVault);	
			Thread.sleep(200);
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess())
					throw new Exception("Allow access to "+documentVault+" not saved correctly");
			}
			Log.message("2. Allowed access to 'Sample Vault'", driver);

			//if Force selection is enabled, uncheck the option
			if(configPage.configurationPanel.getForceFollowingSelection()) {
				configPage.configurationPanel.setForceFollowingSelection(false);
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}
			//Step-3 : Enable retain latest search criteria
			if (!configPage.configurationPanel.getRetainLatestSearchCriteria()) {
				configPage.configurationPanel.setRetainLatestSearchCriteria(true);//Sets Retain latest selection made by user
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}
			Log.message("3. Enabled retain latest search criteria", driver);

			//Step-4: Checks if vault configuration settings has been saved properly
			if (!configPage.configurationPanel.getRetainLatestSearchCriteria()) {
				Log.exception(new Exception("Retain selection for latest search criteria is not enabled after saving."),driver);
			}
			Log.message("4. Retain selection for latest search criteria is enabled for the vault.", driver);

			//Step-5: Logs out from the Configuration page
			configPage.clickLogOut(); 

			//Step-6: Login to Web Access Application
			loginPage.loginToWebApplication(userName, password, documentVault);
			Utils.waitForPageLoad(driver);
			Log.message("5. Logged in to WebAccess with the vault (" +documentVault+ ").", driver);

			//Step-7 : Perform search operation with setting criteria
			SearchPanel.searchOrNavigatetoView(driver, testData.get("SearchCriteria"), "");
			Log.message("6. Performed search operation with given criteria (" + testData.get("SearchCriteria") + ").", driver);
			Utils.fluentWait(driver);

			//Step-8 : Logout and Login back to the vault
			homePage=new HomePage(driver);
			homePage.menuBar.logOutFromMenuBar();
			loginPage.loginToWebApplication(userName,password,documentVault);
			Utils.waitForPageLoad(driver);
			if (!homePage.isLoggedIn(userName)) {
				throw new Exception("Login to the vault is un-successful.");
			}

			Log.message("7. Re-Logged in to the Web Access '"+documentVault+"' vault.", driver);

			//Verification : To verify the latest selection has been retained in search criteria
			SearchPanel searchPanel = new SearchPanel(driver);//Instantiates the search panel
			searchPanel.showAdvancedSearchOptions(driver); //Opens Advanced search
			String selectedCriteria = searchPanel.getSearchType().toUpperCase();
			Log.message("8. Selected the Search criteria set in the previous step", driver);

			//Verifies the latest selection is retained
			if (selectedCriteria.trim().toUpperCase().equals(testData.get("SearchCriteria").toUpperCase()))
				Log.pass("Test Passed. Latest search critera retained in the vault.");
			else
				Log.fail("Test Failed. Latest search critera is not retained in the vault", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {	
				loginPage=new LoginPage(driver);
				driver.get(configSite);
				configPage=new ConfigurationPage(driver);

				configPage.clickVaultFolder(documentVault);	
				if (!configPage.configurationPanel.getVaultAccess()) {
					configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				}
				if (!configPage.configurationPanel.getRetainLatestSearchCriteria()) {
					configPage.configurationPanel.setRetainLatestSearchCriteria(true);//Sets Retain latest selection made by user
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				}
			}
			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally
		} //End finally


	} //End Test1_3_8

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_3_8_1",description="Test1_3_8_1: Verify 'Set default criteria'")
	public void Test1_3_8_1(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData=new ConcurrentHashMap <String, String>(dataValues);

		try {

			//Step-1: Login to Configuration Page
			LoginPage loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");	
			Log.message("1. Logged in to Configuration Page", driver);

			//Step-2: Click Vault from left panel of Configuration Page
			ConfigurationPage configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);
			Log.message("2. Clicked 'Sample Vault' from left panel of Configuration Page", driver);


			Thread.sleep(500);
			//Step-2 : Set default view for the vault
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess())
					throw new Exception("Allow access to "+documentVault+" not saved correctly");
			}
			configPage.configurationPanel.setRetainLatestSearchSettingsInSearchCriteria(false);//Sets Retain latest selection made by user

			if (!configPage.configurationPanel.getDefaultSearchCriteria().toUpperCase().trim().equals(testData.get("SearchCriteria").toUpperCase())) {
				configPage.configurationPanel.setDefaultSearchCriteria(testData.get("SearchCriteria"));//Sets Retain latest selection made by user
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}

			String defautlSearch=configPage.configurationPanel.getDefaultSearchCriteria();
			Log.message("3. Default search criteria displayed as : "+defautlSearch, driver);
			//Checks if vault configuration settings has been saved properly
			if (!defautlSearch.toUpperCase().trim().equals(testData.get("SearchCriteria").toUpperCase())) {
				throw new Exception("['" +testData.get("SearchCriteria") + "'] is not set as default search criteria after saving.");
			}
			Log.message("4. ['"+testData.get("SearchCriteria") + "'] is set as default search criteria for the vault.", driver);

			//Make sure 'Advanced search' option enabled
			configPage.expandVaultFolder(documentVault);
			Log.message("5. Clicked '"+documentVault+"' from left panel of Configuration Page", driver);

			configPage.clickSettingsFolder("Controls");
			if (configPage.configurationPanel.getVaultCommands("Advanced Search").equalsIgnoreCase("Hide")) {
				configPage.chooseConfigurationVaultSettings(driver, "Advanced Search","controls","Show");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}
			Log.message("6. 'Set default criteria' is enabled and settings are saved.", driver);

			//Step-3 : Login to the vault
			configPage.clickLogOut(); //Logs out from the Configuration page

			loginPage.loginToWebApplication(userName,password,documentVault);
			Utils.waitForPageLoad(driver);
			Log.message("7. Logged in to the vault (" +documentVault+ ").", driver);

			//Verification : To verify the latest selection has been retained in search criteria
			SearchPanel searchPanel = new SearchPanel(driver); //Instantiating SearchPanel wrapper class
			searchPanel.showAdvancedSearchOptions(driver); //Opens Advanced search
			Log.message("8. Displayed advanced search options.", driver);

			//Verifies the latest selection is retained
			if (!searchPanel.isSearchTypeBoxEnabled())
				Log.pass("Test Passed. Latest search critera retained in the vault.");
			else
				Log.fail("Test Failed. Latest search critera is not retained in the vault", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			driver.quit();
		} //End finally

	} //End Test1_3_8_1

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_3_9",description="Test1_3_9: Verfy 'Default search setting: Retain latest selection done by user'")
	public void Test1_3_9(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData=new ConcurrentHashMap <String, String>(dataValues);

		try {

			//Step-1: Login to Configuration Page
			LoginPage loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");	
			Log.message("1. Logged in to Configuration Page", driver);

			//Step-2: Click Vault from left panel of Configuration Page
			ConfigurationPage configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);
			Log.message("2. Clicked 'Sample Vault' from left panel of Configuration Page", driver);


			//Step-2 : Set Retain selection latest search settings to the vault
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess())
					throw new Exception("Allow access to "+documentVault+" not saved correctly");
			}
			if (!configPage.configurationPanel.getDefaultView().equalsIgnoreCase("Home")) {
				configPage.configurationPanel.setDefaultView("Home");
			}
			configPage.configurationPanel.setRetainLatestSearchSettings(true);//Sets Retain latest selection made by user
			configPage.clickSaveButton();
			configPage.clickOKBtnOnSaveDialog();
			//Checks if vault configuration settings has been saved properly
			if (!configPage.configurationPanel.getRetainLatestSearchSettings()) {
				throw new Exception("Retain selection for latest search settings is not enabled after saving.");
			}
			Log.message("3. Retain selection for latest search settings is enabled.", driver);

			//Make sure 'Advanced search' option enabled
			configPage.expandVaultFolder(documentVault);
			Log.message("4. Clicked '"+documentVault+"' from left panel of Configuration Page", driver);

			configPage.clickSettingsFolder("Controls");
			if (configPage.configurationPanel.getVaultCommands("Advanced Search").equalsIgnoreCase("Hide")) {
				configPage.chooseConfigurationVaultSettings(driver, "Advanced Search","controls","Show");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}
			Log.message("5. 'Default search setting: Retain latest selection done by user' is enabled and settings are saved.", driver);

			//Step-3 : Login to the vault
			configPage.clickLogOut(); //Logs out from the Configuration page
			Log.message("6. Logs out from the Configuration page", driver);

			loginPage.loginToWebApplication(userName, password, documentVault);
			Utils.waitForPageLoad(driver);

			HomePage homePage=new HomePage(driver);
			Log.message("7. Logged in to the vault (" +documentVault+ ").", driver);

			//Step-4 : Set Search setting in the web access
			SearchPanel searchPanel = new SearchPanel(driver); //Instantiating SearchPanel wrapper class
			searchPanel.setSearchInType(testData.get("SearchSetting")); //Sets the Search type
			Log.message("8. Search setting (" +testData.get("SearchSetting") + ") is set in default web access.", driver);

			//Step-5 : Logout and Login back to the vault
			homePage.menuBar.logOutFromMenuBar();
			loginPage.loginToWebApplication(userName,password,documentVault);
			Utils.waitForPageLoad(driver);
			Log.message("9. Logged out and Logged in back to the vault.", driver);

			//Verification : To verify the latest selection has been retained in search criteria
			if (searchPanel.getSearchInType().toUpperCase().equals(testData.get("SearchSetting").toUpperCase()))
				Log.pass("Test Passed. Latest search setting retained in the vault.");
			else
				Log.fail("Test Failed. Latest search setting is not retained in the vault", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			driver.quit();		

		} //End finally
	} //End Test1_3_9

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_3_9_1",description="Test1_3_9_1: Verify 'Set default setting'")
	public void Test1_3_9_1(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData= new ConcurrentHashMap <String, String>(dataValues);

		try {

			//Step-1: Login to Configuration Page
			LoginPage loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");
			Log.message("1. Loggedin to Configuration Page", driver);

			//Step-2: Click Vault from left panel of Configuration Page
			ConfigurationPage configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);
			Log.message("2. Clicked 'Sample Vault' from left panel of Configuration Page", driver);


			//Step-2 : Set default view for the vault
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess())
					throw new Exception("Allow access to "+documentVault+" not saved correctly");
			}

			Log.message("3. Allowed access to this vault", driver);
			configPage.configurationPanel.setDefaultSearchSettings(testData.get("SearchSetting"));//Sets Retain latest selection made by user
			configPage.clickSaveButton();
			configPage.clickOKBtnOnSaveDialog();
			//Checks if vault configuration settings has been saved properly
			if (configPage.configurationPanel.getDefaultSearchSettings().toUpperCase().trim().equals(testData.get("SearchSetting").toUpperCase())) {
				throw new Exception("Default search settings is not enabled after saving.");
			}
			Log.message("4. '"+testData.get("SearchSetting") + "' is set as default search setting for the vault.", driver);

			//Make sure 'Advanced search' option enabled
			configPage.expandVaultFolder(documentVault);
			Log.message("5. Clicked '"+documentVault+"' from left panel of Configuration Page", driver);

			configPage.clickSettingsFolder("Controls");
			if (configPage.configurationPanel.getVaultCommands("Advanced Search").equalsIgnoreCase("Hide")) {
				configPage.chooseConfigurationVaultSettings(driver, "Advanced Search","controls","Show");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}
			Log.message("6. Show " + testData.get("SettingName") + " is enabled and settings are saved.", driver);

			//Step-3 : Login to the vault
			configPage.clickLogOut(); //Logs out from the Configuration page
			Log.message("7. Logged out from the Configuration page", driver);

			loginPage.loginToWebApplication(userName, password, documentVault);
			Utils.waitForPageLoad(driver);
			Log.message("8. Logged in to the vault (" +documentVault+ ").", driver);

			//Verification : To verify the default search setting has been retained in search criteria
			SearchPanel searchPanel = new SearchPanel(driver); //Instantiating SearchPanel wrapper class

			if (searchPanel.getSearchInType().toUpperCase().equals(testData.get("SearchSetting").toUpperCase()))
				Log.pass("Test case Passed." + testData.get("SearchSetting") + " is the default search setting.");
			else
				Log.fail("Test case Failed." + testData.get("SearchSetting") + " is not the default search setting.", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			driver.quit();				
		} //End finally

	} //End Test1_3_9_1

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_3_10A",description="Test1_3_10A: Verify 'Top menu: Show'")
	public void Test1_3_10A(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		String prevStatus=null;
		LoginPage loginPage=null;

		ConfigurationPage configPage=null;
		HomePage homePage=null;

		try {

			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");	
			Log.message("1. Logged in to Configuration Page", driver);

			//Step-2: Click Vault from left panel of Configuration Page
			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);
			Log.message("2. Clicked '"+documentVault+"' from left panel of Configuration Page", driver);


			prevStatus=configPage.configurationPanel.getTopMenu();
			if(!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
			}
			Log.message("3. Allowed access to this vault", driver);
			//Step-2 : Select Show top menu in the vault configuration page
			if (!prevStatus.equalsIgnoreCase("Show")) {
				configPage.configurationPanel.setTopMenu("Show");//Sets Show Top menu for the vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}

			//Checks if vault configuration settings has been saved properly
			if (!configPage.configurationPanel.getTopMenu().toUpperCase().equals("SHOW")) {
				throw new Exception("Show Top Menu is not enabled after saving.");
			}

			Log.message("4. Show Top Menu is enabled in the vault configuration settings page.", driver);

			//Step-3 : Login to the vault
			configPage.clickLogOut();//Logs out from the Configuration page
			Log.message("5. Logged out from the Configuration page", driver);
			loginPage.loginToWebApplication(userName, password, documentVault);
			Utils.waitForPageLoad(driver);
			homePage=new HomePage(driver);
			if (!homePage.isLoggedIn(userName)) {
				throw new Exception("Login to the vault is un-successful.");
			}

			Log.message("6. Logged in to the vault (" + documentVault + ").", driver);

			//Verification : To verify Top Menu is shown in the vault default page
			if (homePage.isMenubarDisplayed())
				Log.pass("Test Passed. Menu bar is displayed in the vault default page.");
			else
				Log.fail("Test Failed. Menu bar is not displayed in the vault default page.",driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {
				homePage=new HomePage(driver);
				homePage.menuBar.logOutFromMenuBar();
				loginPage=new LoginPage(driver);
				loginPage.navigateToApplication(configSite,userName,password,"");	
				configPage=new ConfigurationPage(driver);
				configPage.clickVaultFolder(documentVault);

				if(!configPage.configurationPanel.getTopMenu().equalsIgnoreCase(prevStatus)) {
					configPage.configurationPanel.setTopMenu(prevStatus); //Allows access to this vault
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				}
			}
			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally
		} //End finally

	} //End Test1_3_10A

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_3_10B",description="Verify 'Top menu: Hide'")
	public void Test1_3_10B(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		HomePage homePage=null;
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;

		try {


			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");	
			Log.message("1. Loggedin to Configuration Page", driver);

			//Step-2: Click Vault from left panel of Configuration Page
			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);
			Log.message("2. Clicked '"+documentVault+"' from left panel of Configuration Page", driver);

			//Step-2 : Select Hide top menu in the vault configuration page
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess()) 
					throw new Exception("Error while allowing access to Vault");
			}

			if (!configPage.configurationPanel.getTopMenu().equalsIgnoreCase("Hide")) {
				configPage.configurationPanel.setTopMenu("Hide");//Sets Show Top menu for the vault
			}

			configPage.clickSaveButton();
			configPage.clickOKBtnOnSaveDialog();

			//Checks if vault configuration settings has been saved properly
			if (!configPage.configurationPanel.getTopMenu().toUpperCase().equals("HIDE")) {
				throw new Exception("Hide Top Menu is not enabled after saving.");
			}

			Log.message("3. Hide Top Menu is enabled in the vault configuration settings page.", driver);

			//Step-4 : LogOut of configuration Page
			configPage.clickLogOut(); //Logs out from the Configuration page
			Log.message("4. LogOut of configuration Page", driver);

			//Step-5 : Login to Web Access
			loginPage.loginToWebApplication(userName, password, documentVault);
			Utils.waitForPageLoad(driver);
			homePage=new HomePage(driver);

			if (!homePage.isLoggedIn(userName)) {
				throw new Exception("Login to the vault is un-successful.");
			}

			Log.message("5. Logged in to the vault (" + documentVault + ").", driver);

			//Step-6 : To verify Top Menu is shown in the vault default page
			if (!homePage.isTopMenuExists())
				Log.pass("Test Passed. Top Menu bar is hidden in the vault default page.");
			else
				Log.fail("Test Failed. Top Menu bar is not hidden in the vault default page.", driver);
		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {
				homePage=new HomePage(driver);
				homePage.menuBar.logOutFromMenuBar();
				loginPage=new LoginPage(driver);
				driver.get(configSite);
				loginPage.loginToConfigurationUI(userName,password);	
				configPage=new ConfigurationPage(driver);
				configPage.clickVaultFolder(documentVault);

				if(!configPage.configurationPanel.getVaultAccess()) {
					configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				}
				if(!configPage.configurationPanel.getTopMenu().equalsIgnoreCase("Show")) {
					configPage.configurationPanel.setTopMenu("Show"); //Allows access to this vault
				}
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();

			}

			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally
		} //End finally

	} //End Test1_3_10B

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_3_11A",description="Test1_3_11A: Verify 'Breadcrumb: Show'")
	public void Test1_3_11A(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		


		LoginPage loginPage=null;
		HomePage homePage=null;
		ConfigurationPage configPage=null;

		try {

			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");	
			Log.message("1. Logged in to Configuration Page", driver);

			//Step-2: Click Vault from left panel of Configuration Page
			Log.message("2. Clicked 'Sample vault' from left panel of Configuration Page", driver);
			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);
			//ALlow access to vault
			if(!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true);
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess()) 
					throw new Exception("Access to "+documentVault+ "not saved");
			}
			Log.message("3. Allowed access to this vault", driver);
			//Step-3 : Select Show Breadcrumb in the vault configuration page
			if (!configPage.configurationPanel.getBreadCrumb().equalsIgnoreCase("Show")) {
				configPage.configurationPanel.setBreadCrumb("Show");//Sets Show Breadcrumb for the vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}

			//Checks if vault configuration settings has been saved properly
			if (!configPage.configurationPanel.getBreadCrumb().toUpperCase().equals("SHOW")) {
				throw new Exception("Show Breadcrumb is not enabled after saving.");
			}

			Log.message("4. 'Show Breadcrumb' is enabled in the vault configuration settings page.", driver);

			//Step-3 : Login to the vault
			configPage.clickLogOut();//Logs out from the Configuration page
			Log.message("5. Logs out from the Configuration page", driver);
			loginPage.loginToWebApplication(userName, password, documentVault);
			Utils.waitForPageLoad(driver);
			homePage=new HomePage(driver);

			if (!homePage.isLoggedIn(userName)) {
				throw new Exception("Login to the vault is un-successful.");
			}

			Log.message("6. Logged in to the vault (" +documentVault+ ").", driver);

			//Verification : To verify breadcrumb bar is shown in the vault default page
			if (homePage.isBreadcrumbDisplayed())
				Log.pass("Test Passed. Breadcrumb bar is displayed in the vault default page.");
			else
				Log.fail("Test Failed. Breadcrumb bar is not displayed in the vault default page.", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {
				homePage=new HomePage(driver);
				homePage.menuBar.logOutFromMenuBar();
				loginPage=new LoginPage(driver);
				loginPage.navigateToApplication(configSite,userName,password,"");	
				configPage=new ConfigurationPage(driver);
				configPage.clickVaultFolder(documentVault);

				if(!configPage.configurationPanel.getVaultAccess()) {
					configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				}
				if (!configPage.configurationPanel.getBreadCrumb().equalsIgnoreCase("Show")) {
					configPage.configurationPanel.setBreadCrumb("Show");//Sets Show Breadcrumb for the vault
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				}
			}
			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally
		} //End finally

	} //End Test1_3_11A

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_3_11B",description="Test1_3_11B: Verify 'Breadcrumb: Hide'")
	public void Test1_3_11B(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		HomePage homePage=null;
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;


		try {

			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");	
			Log.message("1. Logged in to Configuration Page", driver);

			//Step-2: Click Vault from left panel of Configuration Page
			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);
			Log.message("2. Clicked 'Sample vault' from left panel of Configuration Page", driver);

			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess()) 
					throw new Exception("Error while allowing access to Vault");
			}

			Log.message("3. Allowed Access to 'Sample vault' from Configuration Page", driver);
			//Step-2 : Select Show Breadcrumb in the vault configuration page
			if (!configPage.configurationPanel.getBreadCrumb().equalsIgnoreCase("Hide")) {
				configPage.configurationPanel.setBreadCrumb("Hide");//Sets Show Breadcrumb for the vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}

			//Checks if vault configuration settings has been saved properly
			if (!configPage.configurationPanel.getBreadCrumb().toUpperCase().equals("HIDE")) {
				throw new Exception("Hide Breadcrumb is not enabled after saving.");
			}
			Log.message("4. Hide Breadcrumb is enabled in the vault configuration settings page.", driver);

			//Step-3 : Login to the vault
			configPage.clickLogOut(); //Logs out from the Configuration page
			Log.message("5. Logged out from the Configuration page", driver);
			loginPage.loginToWebApplication(userName, password, documentVault);
			Utils.waitForPageLoad(driver);
			homePage=new HomePage(driver);

			if (!homePage.isLoggedIn(userName)) {
				throw new Exception("Login to the vault is un-successful.");
			}

			Log.message("6. Logged in to the vault (" +documentVault + ").", driver);

			//Verification : To verify breadcrumb bar is shown in the vault default page
			if (!homePage.isBreadcrumbDisplayed())
				Log.pass("Test Passed. Breadcrumb bar is hidden in the vault default page.");
			else
				Log.fail("Test Failed. Breadcrumb bar is not hidden in the vault default page.", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {
				homePage=new HomePage(driver);
				homePage.menuBar.logOutFromMenuBar();
				loginPage=new LoginPage(driver);
				loginPage.navigateToApplication(configSite,userName,password,"");	
				configPage=new ConfigurationPage(driver);
				configPage.clickVaultFolder(documentVault);

				if(!configPage.configurationPanel.getVaultAccess()) {
					configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				}
				if (!configPage.configurationPanel.getBreadCrumb().equalsIgnoreCase("Show")) {
					configPage.configurationPanel.setBreadCrumb("Show");//Sets Show Breadcrumb for the vault
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				}
			}
			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally
		} //End finally

	} //End Test1_3_11B

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_3_14",description="Test1_3_14: Verify 'Reset' for the last modified search setting")
	public void Test1_3_14(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		Boolean prevStatus;
		Boolean currStatus;
		try {

			//Step-1: Login to Configuration Page
			LoginPage loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");	
			Log.message("1. Logged in to Configuration Page", driver);

			//Step-2: Click Vault from left panel of Configuration Page
			ConfigurationPage configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);
			Log.message("2. Clicked 'Sample Vault' from left panel of Configuration Page", driver);


			//Step-3 : Modify the 'Retain latest Settings' Setting value
			prevStatus = configPage.configurationPanel.getRetainLatestSearchSettings(); //Gets the status of retail latest search settings
			Thread.sleep(100);
			Log.message("previous statue : " + prevStatus);
			if(!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}

			if (prevStatus) {
				configPage.configurationPanel.setRetainLatestSearchSettings(false);
				Utils.fluentWait(driver);
			}
			else{ 
				configPage.configurationPanel.setRetainLatestSearchSettings(true);
				Utils.fluentWait(driver);
			}
			Log.message("3. 'Retain latest Search Criteria' option is modified for the vault.", driver);

			//Step:4 : Verify the Reset button resets the last made changes 
			configPage.clickResetButton(); //Clicks Reset button
			Utils.fluentWait(driver);
			currStatus = configPage.configurationPanel.getRetainLatestSearchSettings(); //Gets the status of retail latest search settings
			Utils.fluentWait(driver);
			Log.message("current statue : " + currStatus);
			Log.message("4. Clicked 'Reset' button to reset the last made changes", driver);

			//Verifies the latest selection is retained
			if (currStatus.equals(prevStatus))
				Log.pass("Test Passed. Reset button resets the last modified search setting, CurrentStatus shows :"+currStatus+" PreviousStatus shows :"+prevStatus);
			else
				Log.fail("Test Failed. Reset button does not reset the last modified search setting,, CurrentStatus shows :"+currStatus+" and PreviousStatus shows :"+prevStatus, driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			//close driver
			driver.quit();		

		} //End finally

	} //End Test1_3_14

	//	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_4_1",description="Test1_4_1: Last modified date and time in vault-Controls configuration settings")
	//	public void Test1_4_1(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {
	//	
	//		//Variable Declaration
	//		driver = WebDriverUtils.getDriver();
	//		
	//		
	//		ConcurrentHashMap <String, String> testData= new ConcurrentHashMap <String, String>(dataValues);
	//		String prevStatus=null;
	//		ConfigurationPage configPage=null;
	//		try {
	//			
	//			
	//			//Step-1: Login to Configuration Page
	//			LoginPage loginPage=new LoginPage(driver);
	//			loginPage.navigateToApplication(configSite,userName,password,"");	
	//			Log.message("1. Logged in to Configuration Page", driver);
	//			
	//			//Step-2: Click Vault from left panel of Configuration Page
	//			configPage=new ConfigurationPage(driver);
	//			configPage.expandVaultFolder(documentVault);
	//			Log.message("2. Expanded '"+documentVault+"' from left panel of Configuration Page");
	//			configPage.clickSettingsFolder(testData.get("SettingFolder"));
	//			
	//			
	//			//Step-2 : Modify some setting in control page and save it
	//			prevStatus = configPage.configurationPanel.getVaultCommands(testData.get("SettingName"));
	//			Log.message("3. Read the existing value of the setting in control page and save it");
	//			
	//			if (prevStatus.equalsIgnoreCase("Disallow")) 
	//				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Show");
	//			else 
	//				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Hide");
	//			
	//			driver.findElement(By.cssSelector("input[id='saveSettings']")).click();
	//			Thread.sleep(100);
	//			//Gets current date and time
	//			currDateTime=getTime();
	//			configPage.clickOKBtnOnSaveDialog();
	//			
	//			Log.message("4. Modified setting in control page and settings are saved.");
	//			
	//			//Verification : To verify last modified date and time are right in the vault configuration page
	//			configPage.clickTreeViewItem("General settings"); //Selects General in the tree view of configuration page
	//			Thread.sleep(100);
	////			configPage.clickVaultFolder(documentVault);
	//			configPage.expandVaultFolder(documentVault);
	//			configPage.clickSettingsFolder(testData.get("SettingFolder")); //Selects General in the tree view of configuration page
	//			Log.message("5. Navigated to other vault folders and clicked the '"+testData.get("SettingFolder")+"' folder.");
	//
	//			String modifiedDate=configPage.configurationPanel.getLastModifiedDateTime();
	//			Log.message("6. Read Last modified date of the Vault");
	//			System.out.println(modifiedDate.compareTo(currDateTime));
	//			
	//			//Verifies the time in vault configuration page is same as the system date and time at the time of modification
	//			if (modifiedDate.compareTo(currDateTime)==0) 
	//				Log.pass("Test Passed. Last modified time gets updated as '"+configPage.configurationPanel.getLastModifiedDateTime()+"' on modifying the configuration-controls page.");
	//			else 
	//				Log.fail("Test Failed. Expected Last modified time is '"+currDateTime+"' , but displayed as '"+modifiedDate+"' does not gets updated on modifying the configuration-controls page.", driver);
	//			
	//		} //End try
	//		
	//		catch (Exception e) {
	//			Log.exception(e, driver);
	//		} //End catch
	//		
	//		finally {
	//				configPage=new ConfigurationPage(driver);
	//				configPage.expandVaultFolder(documentVault);
	//				configPage.clickSettingsFolder(testData.get("SettingFolder"));
	//				
	//				if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {
	//					configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Show");
	//					configPage.clickSaveButton();
	//					configPage.clickOKBtnOnSaveDialog();
	//				}
	//			driver.quit();		
	//				
	//			} //End finally
	//		
	//	} //End Test1_4_1
	//	
	//	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_4_2A",description="Test1_4_2A: Save column settings : Show")
	//	public void Test1_4_2A(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {
	//		
	//		//Variable Declaration
	//		driver = WebDriverUtils.getDriver();
	//		
	//		ConcurrentHashMap <String, String> testData=new ConcurrentHashMap <String, String>(dataValues);
	//		String prevStatus=null;
	//		HomePage homePage=null;
	//		LoginPage loginPage=null;
	//		ConfigurationPage configPage=null;
	//		
	//		try {
	//			
	//			
	//			//Step-1: Login to Configuration Page
	//
	//			loginPage=new LoginPage(driver);
	//			loginPage.navigateToApplication(configSite, userName, password,"");
	//			Log.message("1. Logged in to Configuration Page", driver);
	//			
	//			configPage=new ConfigurationPage(driver);
	//			configPage.clickVaultFolder(documentVault);
	//			
	//			if (!configPage.configurationPanel.getVaultAccess()) {
	//				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
	//				configPage.clickSaveButton();
	//				configPage.clickOKBtnOnSaveDialog();
	//				if (!configPage.configurationPanel.getVaultAccess())
	//					throw new Exception("Allow access to "+documentVault+" not saved correctly");
	//			}
	//			
	//			if (!configPage.configurationPanel.getLayout().equalsIgnoreCase("Default layout")) {
	//				configPage.configurationPanel.setLayout("Default layout");
	//				configPage.clickSaveButton();
	//				configPage.clickOKBtnOnSaveDialog();
	//			}
	//			
	//			//Step-2: Click Vault from left panel of Configuration Page
	//			configPage.expandVaultFolder(documentVault);
	//			configPage.clickSettingsFolder(testData.get("SettingFolder"));
	//			Log.message("2. Clicked '"+documentVault+"' from left panel of Configuration Page");
	//			
	//			//Step-2 : Select Show Metadata card and save the settings 
	//			prevStatus = configPage.configurationPanel.getVaultCommands(testData.get("SettingName")); //Gets the previous status of the command
	//			if (!prevStatus.equalsIgnoreCase("Allow")) {
	//				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Allow");
	//				configPage.clickSaveButton();
	//				configPage.clickOKBtnOnSaveDialog();
	//			}
	//			
	//			Thread.sleep(100);
	//			if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("ALLOW")) {//Checks for the modified settings
	//				throw new Exception("Allow " + testData.get("SettingName") + " is not enabled after saving.");
	//			}
	//			Log.message("3. Allow " + testData.get("SettingName") + " are saved.");
	//			
	//			//Make sure 'Advanced search' option enabled
	//			configPage.expandVaultFolder(documentVault);
	//			Log.message("2. Clicked '"+documentVault+"' from left panel of Configuration Page");
	//			
	//			configPage.clickSettingsFolder("Controls");
	//			if (configPage.configurationPanel.getVaultCommands("Advanced Search").equalsIgnoreCase("Hide")) {
	//				configPage.chooseConfigurationVaultSettings(driver, "Advanced Search","controls","Show");
	//				configPage.configurationPanel.saveSettings();
	//			}
	//			Log.message("6. Show " + testData.get("SettingName") + " is enabled and settings are saved.");
	//			//Step-3 : Login to the vault
	//			configPage.clickLogOut(); //Logs out from the Configuration page
	//			loginPage.loginToWebApplication(userName, password, documentVault);
	//			Utils.waitForPageLoad(driver);
	//
	//			homePage=new HomePage(driver);
	//			Log.message("4. Logged in to Web Access with the vault (" + documentVault + ").");
	//			//Step-4 : Perform search operation and insert the column
	//			homePage.clickSearchBtn(driver); //Clicks Search button
	//			
	//			if(!homePage.isRightPaneHidden())
	//				throw new Exception("Unable to hide RightPane");
	//			
	//			
	//			if (!homePage.readListViewHeaderNames(driver,testData.get("ColumnName"))) {
	//				homePage.listView.insertColumn(testData.get("ColumnName"));
	//				Log.message("'"+testData.get("ColumnName")+"' column is displayed in HomePage");
	//			}
	//			Thread.sleep(500);
	//				
	//			if (homePage.readListViewHeaderNames(driver,testData.get("ColumnName"))) 
	//				Log.pass("Test Passed. Allow '" + testData.get("SettingName") + "' is enabled successfully.");
	//			else
	//				Log.fail("Test Failed. Column inserted is not saved after enabling Allow '" + testData.get("SettingName") + "'.", driver);
	//			
	//		} //End try
	//		
	//		catch (Exception e) {
	//			Log.exception(e, driver);
	//		} //End catch
	//		
	//		finally {
	//					driver.get(configSite);
	//					configPage=new ConfigurationPage(driver);
	//					configPage.expandVaultFolder(documentVault);
	//					configPage.clickSettingsFolder(testData.get("SettingFolder"));
	//					
	//					if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW") ||!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("Allow")) {
	//						configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Show");
	//						configPage.clickSaveButton();
	//						configPage.clickOKBtnOnSaveDialog();
	//					}
	//			driver.quit();
	//		} //End finally
	//		
	//	} //End ConfigurationUI1_4_2A
	//	
	//	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_4_2B",description="Test1_4_2B: Save column settings : Hide/DisAllow")
	//	public void Test1_4_2B(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {
	//	
	//		//Variable Declaration
	//		driver = WebDriverUtils.getDriver();
	//		
	//		ConcurrentHashMap <String, String> testData= new ConcurrentHashMap <String, String>(dataValues);
	//		String prevStatus=null;
	//		LoginPage loginPage=null;
	//		HomePage homePage=null;
	//		ConfigurationPage configPage=null;
	//		
	//		try {
	//			
	//			
	//			//Step-1: Login to Configuration Page
	//			loginPage=new LoginPage(driver);
	//			loginPage.navigateToApplication(configSite,userName,password,"");	
	//			Log.message("1. Logged in to Configuration Page", driver);
	//			
	//			configPage=new ConfigurationPage(driver);
	//			configPage.clickVaultFolder(documentVault);
	//			
	//			if (!configPage.configurationPanel.getVaultAccess()) {
	//				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
	//				configPage.clickSaveButton();
	//				configPage.clickOKBtnOnSaveDialog();
	//				if (!configPage.configurationPanel.getVaultAccess())
	//					throw new Exception("Allow access to "+documentVault+" not saved correctly");
	//			}
	//			
	//			if (!configPage.configurationPanel.getLayout().equalsIgnoreCase("Default layout")) {
	//				configPage.configurationPanel.setLayout("Default layout");
	//				configPage.clickSaveButton();
	//				configPage.clickOKBtnOnSaveDialog();
	//			}
	//			
	//			//Step-2: Click Vault from left panel of Configuration Page
	//			configPage.expandVaultFolder(documentVault);
	//			configPage.clickSettingsFolder(testData.get("SettingFolder"));
	//			Log.message("2. Clicked '"+documentVault+"' from left panel of Configuration Page");
	//			
	//			
	//			//Step-2 : Select Show Metadata card and save the settings 
	//			prevStatus = configPage.configurationPanel.getVaultCommands(testData.get("SettingName")); //Gets the previous status of the command
	//			if (!prevStatus.equalsIgnoreCase("Disallow")) {
	//				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Hide");
	//				configPage.clickSaveButton();
	//				configPage.clickOKBtnOnSaveDialog();
	//			}
	//			Thread.sleep(1000);
	//			if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("DISALLOW")) {//Checks for the modified settings
	//				throw new Exception("Hide " + testData.get("SettingName") + " is not enabled after saving.");
	//			}
	//			
	//			Log.message("3. Hide " + testData.get("SettingName") + " is enabled settings are saved.");
	//			
	//			//Step-3 : Login to the vault
	//			configPage.clickLogOut();//Logs out from the Configuration page
	//			Log.message("4. Logs out from the Configuration page");
	//			loginPage.loginToWebApplication(userName, password, documentVault);
	//			Utils.waitForPageLoad(driver);
	//			homePage=new HomePage(driver);
	//			
	//			if (!homePage.isLoggedIn(userName)) {
	//				throw new Exception("Unable to login to Web Access");
	//			}
	//			Log.message("5. Logged in to the vault (" + documentVault + ").");
	//			
	//			//Step-4 : Perform search operation and insert the column
	//			homePage.clickSearchBtn(driver); //Clicks Search button
	//			
	//			if (!homePage.readListViewHeaderNames(driver,testData.get("ColumnName"))) 
	//				homePage.listView.insertColumn(testData.get("ColumnName"));
	//			
	//			Log.message("6. Performed search operation and column (" + testData.get("ColumnName") + ") is removed.");
	//			Utils.fluentWait(driver);
	//			//Verifies New Menu is displayed after enabling Allow Save Column settings
	//			if (!homePage.readListViewHeaderNames(driver,testData.get("ColumnName")))
	//				Log.pass("Test Passed. Disallow " + testData.get("SettingName") + " is enabled successfully.");
	//			else
	//				Log.fail("Test Failed. ['"+testData.get("ColumnName")+"'] Column inserted enabling Disallow " + testData.get("SettingName") + ".",driver);
	//			
	//		} //End try
	//		
	//		catch (Exception e) {
	//			Log.exception(e, driver);
	//		} //End catch
	//		
	//		finally {
	//				homePage=new HomePage(driver);
	//				homePage.menuBar.logOutFromMenuBar();
	//				loginPage=new LoginPage(driver);
	//				loginPage.navigateToApplication(configSite,userName,password,"");	
	//				configPage=new ConfigurationPage(driver);
	//				configPage.expandVaultFolder(documentVault);
	//				configPage.clickSettingsFolder(testData.get("SettingFolder"));
	//				
	//				if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")|!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("ALLOW")) {
	//					configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Show");
	//					configPage.clickSaveButton();
	//					configPage.clickOKBtnOnSaveDialog();
	//				}
	//			driver.quit();				
	//			
	//		} //End finally
	//		
	//	} //End Test1_4_2B
	//	
	//	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_4_3A",description="Test1_4_3A: Metadata card : Show")
	//	public void Test1_4_3A(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {
	//		
	//		//Variable Declaration
	//		driver = WebDriverUtils.getDriver();
	//		
	//		
	//		ConcurrentHashMap <String, String> testData=new ConcurrentHashMap <String, String>(dataValues);
	//		String prevStatus=null;
	//		MenuBar	menuBar =null;
	//		LoginPage loginPage=null;
	//		ConfigurationPage configPage=null;
	//		
	//		
	//		try {
	//			//Step-1: Login to Configuration Page
	//			loginPage=new LoginPage(driver);
	//			loginPage.navigateToApplication(configSite,userName,password,"");
	//			Log.message("1. Logged in to Configuration Page", driver);
	//			
	//			//Step-2: Click Vault from left panel of Configuration Page
	//			configPage=new ConfigurationPage(driver);
	//			configPage.expandVaultFolder(documentVault);
	//			configPage.clickSettingsFolder(testData.get("SettingFolder"));
	//			Log.message("2. Clicked '"+documentVault+"' from left panel of Configuration Page");
	//			
	//			//Step-2 : Select Show Metadata card and save the settings 
	//			prevStatus = configPage.configurationPanel.getVaultCommands(testData.get("SettingName")); //Gets the previous status of the command
	//			if (!prevStatus.equalsIgnoreCase("Show")) {
	//				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Show");
	//				configPage.clickSaveButton();
	//				configPage.clickOKBtnOnSaveDialog();
	//			}
	//			Thread.sleep(1000);
	//			if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {//Checks for the modified settings
	//				throw new Exception("Show " + testData.get("SettingName") + " is not enabled after saving.");
	//			}
	//			
	//			Log.message("3. Show " + testData.get("SettingName") + " is enabled settings are saved.");
	//			
	//			//Step-3 : Login to the vault
	//			configPage.clickLogOut();//Logs out from the Configuration page
	//			Log.message("4. Logs out from the Configuration page");
	//			loginPage.navigateToApplication(webSite, userName, password,documentVault);
	//			Utils.waitForPageLoad(driver);
	//			HomePage homePage=new HomePage(driver);
	//			
	//			Log.message("5. Logged in to the vault (" + documentVault + ").");
	//			
	//			//Verification : To verify Show Command settings is reflected in MFWA page
	//			TaskPanel taskPanel = new TaskPanel(driver); //Instantiating TaskPanel wrapper class
	//				
	//			//Verifies if New menu item is available in task pane
	//			if (!taskPanel.isItemExists("New")) {
	//				throw new Exception("New menu does not exists after enabling show " + testData.get("SettingName") + ".");
	//			}
	//			
	//			homePage.clickSearchBtn(driver); //Perform search operation
	//				
	//			
	//			//Verifies Properties menu option is displayed for an object after enabling show metadatacard
	//			if (homePage.listView.itemCount() > 0) {
	//				homePage.listView.clickItemByIndex(0);
	//				Thread.sleep(500);
	//				if (!taskPanel.isItemExists("Properties")) { //Checks if New menu item is available
	//					throw new Exception("Properties menu in Task Pane does not exists after enabling show " + testData.get("SettingName") + ".");
	//				}
	//			}
	//			else
	//				Log.message("No objects in the vault to verify if Properties option exists in taskpane");
	//				
	//			menuBar = new MenuBar(driver);
	//			//Verifies New Menu is displayed after enabling show metadatacard 
	//			if (menuBar.IsNewMenuDisplayed())
	//				Log.pass("Test Passed. Show " + testData.get("SettingName") + " is enabled and New Menu bar is displayed successfully.");
	//			else
	//				Log.fail("Test Failed. New Menu bar is not displayed after enabling show " + testData.get("SettingName") + ".",driver);
	//				
	//			} //End try
	//			
	//			catch (Exception e) {
	//				Log.exception(e, driver);
	//			} //End catch
	//			
	//			finally {
	////					menuBar = new MenuBar(driver);
	////					menuBar.logOutFromMenuBar();
	////					loginPage=new LoginPage(driver);
	////					loginPage.navigateToApplication(configSite,userName,password,"");
	//					driver.get(configSite);
	//					configPage=new ConfigurationPage(driver);
	//					configPage.expandVaultFolder(documentVault);
	//					configPage.clickSettingsFolder(testData.get("SettingFolder"));
	//					
	//					if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {
	//						configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Show");
	//						configPage.clickSaveButton();
	//						configPage.clickOKBtnOnSaveDialog();
	//					}
	//				driver.quit();				
	//			} //End finally
	//		
	//	} //End Test1_4_3A
	//	
	//	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_4_3B",description="Test1_4_3B: Metadata card : Hide")
	//	public void Test1_4_3B(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {
	//	
	//		//Variable Declaration
	//		driver = WebDriverUtils.getDriver();
	//		
	//		
	//		ConcurrentHashMap <String, String> testData=new ConcurrentHashMap <String, String>(dataValues);
	//		String prevStatus=null;
	//		LoginPage loginPage=null;
	//		ConfigurationPage configPage=null;
	//		
	//		HomePage homePage=null;
	//		try {
	//			
	//			//Step-1: Login to Configuration Page
	//			loginPage=new LoginPage(driver);
	//			loginPage.navigateToApplication(configSite,userName,password,"");	
	//			Log.message("1. Logged in to Configuration Page", driver);
	//			
	//			//Step-2: Click Vault from left panel of Configuration Page
	//			configPage=new ConfigurationPage(driver);
	//			configPage.expandVaultFolder(documentVault);
	//			configPage.clickSettingsFolder(testData.get("SettingFolder"));
	//			Log.message("2. Clicked '"+documentVault+"' from left panel of Configuration Page");
	//			
	//			
	//				
	//			//Step-2 : Select Show Metadata card and save the settings 
	//			prevStatus = configPage.configurationPanel.getVaultCommands(testData.get("SettingName")); //Gets the previous status of the command
	//			if (!prevStatus.equalsIgnoreCase("Hide")) {
	//				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Hide");
	//				configPage.clickSaveButton();
	//				configPage.clickOKBtnOnSaveDialog();
	//			}
	//			Thread.sleep(1000);
	//			if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("HIDE")) {//Checks for the modified settings
	//				throw new Exception("Hide " + testData.get("SettingName") + " is not enabled after saving.");
	//			}
	//			
	//			Log.message("3. Hide " + testData.get("SettingName") + " is enabled for '"+configPage.configurationPanel.getVaultCommands(testData.get("SettingName"))+"' settings are saved.");
	//			
	//			//Step-3 : Login to the vault
	//			configPage.clickLogOut();//Logs out from the Configuration page
	//			Log.message("4. Logged out from the Configuration page");
	//			loginPage.navigateToApplication(webSite, userName, password,documentVault);
	//			Utils.waitForPageLoad(driver);
	//			homePage=new HomePage(driver);
	//			
	//			Log.message("5. Logged in to the vault (" + documentVault + ").");
	//				
	//			//Verification : To verify Show Command settings is reflected in MFWA page
	//			TaskPanel taskPanel = new TaskPanel(driver); //Instantiating TaskPanel wrapper class
	//			//Verifies if New menu item is not available in task pane
	//			if (taskPanel.isItemExists("New")) {
	//				throw new Exception("New menu exists after enabling hide " +  testData.get("SettingName")  + ".");
	//			}
	//			
	//			homePage.clickSearchBtn(driver); //Perform search operation
	//			Log.message("6. Searched with default search options");
	//
	//			
	//			//Verifies Properties menu option is not displayed for an object after enabling hide metadatacard
	//			if (homePage.listView.itemCount() > 0) {
	//				homePage.listView.clickItemByIndex(0);
	//				if (taskPanel.isItemExists("Properties")) {
	//					throw new Exception("Properties option in Task Pane exists after enabling hide " +  testData.get("SettingName")  + ".");
	//				} 
	//				Log.message("7. Properties option is not displayed in Task Pane exists after enabling hide");
	//			}
	//			else
	//				Log.message("7. No objects in the vault to verify if Properties option does not exists in taskpane");
	//			
	//			MenuBar menuBar = new MenuBar(driver);
	//			//Verifies New Menu is not displayed after enabling hide metadata card 
	//			if (!menuBar.IsNewMenuDisplayed())
	//				Log.pass("Test Passed. Hide " +  testData.get("SettingName")  + " is enabled successfully.");
	//			else
	//				Log.fail("Test Failed. New Menu bar is displayed after enabling hide " +  testData.get("SettingName")  + ".", driver);
	//			
	//		} //End try
	//		
	//		catch (Exception e) {
	//			Log.exception(e, driver);
	//		} //End catch
	//		
	//		finally {
	//					homePage=new HomePage(driver);
	//					homePage.clickLogOut();
	//					loginPage=new LoginPage(driver);
	//					loginPage.navigateToApplication(configSite,userName,password,"");	
	//					configPage=new ConfigurationPage(driver);
	//					configPage.expandVaultFolder(documentVault);
	//					configPage.clickSettingsFolder(testData.get("SettingFolder"));
	//					
	//					if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {
	//						configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Show");
	//						configPage.clickSaveButton();
	//						configPage.clickOKBtnOnSaveDialog();
	//					}
	//				driver.quit();
	//		} //End finally
	//	
	//	} //End Test1_4_3B
	//		
	//	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_4_4A",description="Test1_4_4A: Workflow shortcut in properties pane : Show")
	//	public void Test1_4_4A(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {
	//		//Variable Declaration
	//		driver = WebDriverUtils.getDriver();
	//		
	//		
	//		ConcurrentHashMap <String, String> testData=new ConcurrentHashMap <String, String>(dataValues);
	//		String prevStatus=null;
	//		String propPane="ShowMetadataBottomPane";
	//		LoginPage loginPage=null;
	//		ConfigurationPage configPage=null;
	//		
	//		HomePage homePage=null;
	//		try {
	//			
	//			//Step-1: Login to Configuration Page
	//			loginPage=new LoginPage(driver);
	//			loginPage.navigateToApplication(configSite,userName,password,"");	
	//			Log.message("1. Logged in to Configuration Page", driver);
	//			
	//			//Step-2: Click Vault from left panel of Configuration Page
	//			configPage=new ConfigurationPage(driver);
	//			configPage.expandVaultFolder(documentVault);
	//			configPage.clickSettingsFolder(testData.get("SettingFolder"));
	//			Log.message("2. Clicked '"+documentVault+"' from left panel of Configuration Page");
	//			
	//			
	//				
	//			//Step-2 : Select Show Metadata card and save the settings 
	//			prevStatus = configPage.configurationPanel.getVaultCommands(testData.get("SettingName")); //Gets the previous status of the command
	//			if (!prevStatus.equalsIgnoreCase("Show")) {
	//				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Show");
	//				configPage.clickSaveButton();
	//				configPage.clickOKBtnOnSaveDialog();
	//			}
	//			Thread.sleep(1000);
	//			if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {//Checks for the modified settings
	//				throw new Exception("Show " + testData.get("SettingName") + " is not enabled after saving.");
	//			}
	//			
	//			Log.message("3. Show " + testData.get("SettingName") + " is enabled for '"+configPage.configurationPanel.getVaultCommands(testData.get("SettingName"))+"' and settings are saved.");
	//			
	//			//Step-3 : Login to the vault
	//			configPage.clickLogOut();//Logs out from the Configuration page
	//			Log.message("4. Logged out from the Configuration page");
	//			loginPage.navigateToApplication(webSite, userName, password,documentVault);
	//			Utils.waitForPageLoad(driver);
	//			homePage=new HomePage(driver);
	//			
	//			Log.message("5. Logged in to the vault (" + documentVault + ").");
	//		
	//			//Step-4 : Perform search operation and click the document object
	//			homePage.enterSearchText(testData.get("DocumentName"));
	//			homePage.clickSearchBtn(driver); //Clicks Search button
	//			
	//			if (!homePage.isDataInListView(driver,testData.get("DocumentName"),"Name")) {//Launches and logs into the login page
	//				throw new Exception("Invalid test data. Document (" + testData.get("DocumentName") +") does not exists in list.");
	//			}
	//			Log.message("6. Performed search operation using "+testData.get("DocumentName")+" and Found document object");
	//			
	//			
	//			homePage.listView.clickItem(testData.get("DocumentName")); //Selects the document
	//			Thread.sleep(2000);
	//			Log.message("7. Clicked the document object found");
	//				
	//			//Verification : To verify Show Command settings is reflected in MFWA page
	//			PropertiesPane propPanel = new PropertiesPane(driver);
	//			
	//			if (!propPanel.isPropertyPaneExists()) {
	//				MenuBar menuBar=new MenuBar(driver);
	//				menuBar.selectDisplayModeSettingOptions(propPane.trim());
	//			}
	//			if (!propPanel.isPropertyExists("Workflow")) {
	//				throw new Exception("Workflow Property is not displayed in properties pane for the selected document.");
	//			}
	//			Log.message("8. Workflow Property is displayed in the properties pane for the selected document ");
	//			
	//			//Verifies Worflow property value is displayed as link
	//			if (propPanel.PropertyValueIsLink("Workflow"))
	//				Log.pass("Test Passed. Workflow shortcut is displayed in properties pane after enabling Show " + testData.get("SettingName") + ".");
	//			else
	//				Log.fail("Test Failed. Workflow is not displayed as shortcut in properties pane after  after enabling Show " + testData.get("SettingName") + ".",driver);
	//			
	//		} //End try
	//		
	//		catch (Exception e) {
	//			Log.exception(e, driver);
	//		} //End catch
	//		
	//		finally {
	//				homePage=new HomePage(driver);
	//				loginPage=new LoginPage(driver);
	//				driver.get(configSite);
	//				configPage=new ConfigurationPage(driver);
	//				configPage.expandVaultFolder(documentVault);
	//				configPage.clickSettingsFolder(testData.get("SettingFolder"));
	//				
	//				if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {
	//					configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Show");
	//					configPage.clickSaveButton();
	//					configPage.clickOKBtnOnSaveDialog();
	//				}
	//			driver.quit();				
	//		} //End finally
	//		
	//	} //End Test1_4_4A
	//	
	//	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_4_4B",description="Test1_4_4B: Workflow shortcut in properties pane : Hide")
	//	public void Test1_4_4B(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {
	//		//Variable Declaration
	//		driver = WebDriverUtils.getDriver();
	//		
	//		
	//		ConcurrentHashMap <String, String> testData= new ConcurrentHashMap <String, String>(dataValues);
	//		String prevStatus=null;
	//		String propPane="ShowMetadataBottomPane";
	//		LoginPage loginPage=null;
	//		ConfigurationPage configPage=null;
	//		
	//		HomePage homePage=null;
	//		try {
	//			
	//			//Step-1: Login to Configuration Page
	//			loginPage=new LoginPage(driver);
	//			loginPage.navigateToApplication(configSite,userName,password,"");	
	//			Log.message("1. Logged in to Configuration Page", driver);
	//			//Step-2: Click Vault from left panel of Configuration Page
	//			configPage=new ConfigurationPage(driver);
	//			configPage.expandVaultFolder(documentVault);
	//			configPage.clickSettingsFolder(testData.get("SettingFolder"));
	//			Log.message("2. Clicked '"+documentVault+"' from left panel of Configuration Page");
	//			
	//			
	//			//Step-2 : Select Show Metadata card and save the settings 
	//			prevStatus = configPage.configurationPanel.getVaultCommands(testData.get("SettingName")); //Gets the previous status of the command
	//			if (!prevStatus.equalsIgnoreCase("Hide")) {
	//				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Hide");
	//				configPage.clickSaveButton();
	//				configPage.clickOKBtnOnSaveDialog();
	//			}
	//			Thread.sleep(1000);
	//			if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("HIDE")) {//Checks for the modified settings
	//				throw new Exception("Hide " + testData.get("SettingName") + " is not enabled after saving.");
	//			}
	//			Log.message("3. Hide " + testData.get("SettingName") + " is enabled as "+configPage.configurationPanel.getVaultCommands(testData.get("SettingName")));
	//			
	//			//Make sure 'Advanced search' option enabled
	//			configPage.expandVaultFolder(documentVault);
	//			Log.message("2. Clicked '"+documentVault+"' from left panel of Configuration Page");
	//			
	//			configPage.clickSettingsFolder("Controls");
	//			if (configPage.configurationPanel.getVaultCommands("Advanced Search").equalsIgnoreCase("Hide")) {
	//				configPage.chooseConfigurationVaultSettings(driver, "Advanced Search","controls","Show");
	//				configPage.configurationPanel.saveSettings();
	//			}
	//			Log.message("6. Show " + testData.get("SettingName") + " is enabled and settings are saved.");
	//			
	//			//Step-3 : Login to the vault
	//			configPage.clickLogOut();//Logs out from the Configuration page
	//			Log.message("4. Logged out from the Configuration page");
	//			loginPage.navigateToApplication(webSite, userName, password,documentVault);
	//			Utils.waitForPageLoad(driver);
	//			homePage=new HomePage(driver);
	//			
	//			Log.message("5. Logged in to the vault (" + documentVault + ").");
	//			
	//			//Step-4 : Perform search operation and click the document
	//			homePage.enterSearchText(testData.get("DocumentName"));
	//			homePage.clickSearchBtn(driver); //Clicks Search button
	//			Log.message("6. Perform search operation with default criteria");
	//				
	//			if (!homePage.isDataInListView(driver,testData.get("DocumentName"),"Name")) {//Launches and logs into the login page
	//				throw new Exception("Invalid test data. Document (" + testData.get("DocumentName") +") does not exists in list.");
	//			}
	//							
	//			homePage.listView.clickItem(testData.get("DocumentName")); //Selects the document
	//			Log.message("7. Selected the document(" +testData.get("DocumentName") +").");
	//			
	//			//Verification : To verify Show Command settings is reflected in MFWA page
	//			PropertiesPane	propPanel = new PropertiesPane(driver);
	//			if (!propPanel.isPropertyPaneExists()) {
	//				MenuBar menuBar=new MenuBar(driver);
	//				menuBar.selectDisplayModeSettingOptions(propPane.trim());
	//			}
	//			Log.message("8. Properties pane displayed");
	//			if (!propPanel.isPropertyExists("Workflow")) {
	//				throw new Exception("Invalid Test data. Workflow Property is not displayed in properties pane for the selected document.");
	//			}
	//			
	//			//Verifies Worflow property value is displayed as link
	//			if (!propPanel.PropertyValueIsLink("Workflow"))
	//				Log.pass("Test Passed. Workflow is not displayed as shortcut in properties pane after enabling Hide " + testData.get("SettingName") + ".");
	//			else
	//				Log.fail("Test Failed. Workflow is displayed as shortcut in properties pane after enabling Hide " + testData.get("SettingName") + ".", driver);
	//				
	//			} //End try
	//			
	//			catch (Exception e) {
	//				Log.exception(e, driver);
	//			} //End catch
	//			
	//			finally {
	//					homePage=new HomePage(driver);
	//					homePage.clickLogOut();
	//					loginPage=new LoginPage(driver);
	//					loginPage.navigateToApplication(configSite,userName,password,"");	
	//					configPage=new ConfigurationPage(driver);
	//					configPage.expandVaultFolder(documentVault);
	//					configPage.clickSettingsFolder(testData.get("SettingFolder"));
	//
	//					
	//					if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {
	//						configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Show");
	//						configPage.clickSaveButton();
	//						configPage.clickOKBtnOnSaveDialog();
	//					}
	//				driver.quit();				
	//			} //End finally
	//		
	//	} //End Test1_4_4B

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_4_5A",description="Test1_4_5A: Context menu : Show")
	public void Test1_4_5A(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData=new ConcurrentHashMap <String, String>(dataValues);
		String prevStatus=null;
		HomePage homePage=null;
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;


		try {
			//Step-1: Login to Configuration Page
			driver.get(configSite);
			Log.message("1. Launched Configuration Page login page", driver);

			loginPage=new LoginPage(driver);
			loginPage.loginToConfigurationUI(userName,password);
			Log.message("2. Logged in to Configuration Page", driver);
			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);	
			Thread.sleep(200);
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess())
					throw new Exception("Allow access to "+documentVault+" not saved correctly");
			}
			Log.message("3. Allowed access to 'Sample Vault'", driver);


			//Step-2: Click Vault from left panel of Configuration Page

			configPage.expandVaultFolder(documentVault);
			configPage.clickSettingsFolder(testData.get("SettingFolder"));
			Log.message("4. Clicked '"+documentVault+"' from left panel of Configuration Page", driver);



			//Step-2 : Select Show Metadata card and save the settings 
			prevStatus = configPage.configurationPanel.getVaultCommands(testData.get("SettingName")); //Gets the previous status of the command
			if (!prevStatus.equalsIgnoreCase("Show")) {
				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Show");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}
			Thread.sleep(1000);
			if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {//Checks for the modified settings
				throw new Exception("Show " + testData.get("SettingName") + " is not enabled after saving.");
			}

			Log.message("5. 'Show' of " + testData.get("SettingName") + " is enabled settings are saved.", driver);

			//Make sure 'Advanced search' option enabled
			configPage.expandVaultFolder(documentVault);
			Log.message("6. Clicked '"+documentVault+"' from left panel of Configuration Page", driver);

			configPage.clickSettingsFolder("Controls");
			if (configPage.configurationPanel.getVaultCommands("Advanced Search").equalsIgnoreCase("Hide")) {
				configPage.chooseConfigurationVaultSettings(driver, "Advanced Search","controls","Show");
				configPage.configurationPanel.saveSettings();
			}
			Log.message("7. Show " + testData.get("SettingName") + " is enabled and settings are saved.", driver);

			//Step-3 : Login to the vault
			configPage.clickLogOut();//Logs out from the Configuration page
			Log.message("8. Logged out from the Configuration page", driver);
			loginPage.loginToWebApplication(userName, password, documentVault);
			Utils.waitForPageLoad(driver);
			Log.message("9. Logged in to '"+documentVault+"'", driver);
			homePage=new HomePage(driver);
			homePage.clickSearchBtn(driver);
			Log.message("10. Searched with Default search criteria", driver);
			Utils.fluentWait(driver);

			//Step-4 : Select any view and perform right click operation

			if (homePage.listView.itemCount() <=0) //Launches and logs into the login page
				throw new Exception("No items in the list to perform right click operation.");

			Log.message("11. Search results are successful for the default criteria", driver);
			//Verifies Context menu is displayed after enabling Show context menu
			if (homePage.openContextMenuDialog(driver))
				Log.pass("Test Passed. Context menu is diplayed after enabling Show of '" + testData.get("SettingName") + "'.");
			else
				Log.fail("Test Failed. Context menu is not displayed after enabling Show of '" + testData.get("SettingName") + "'.", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try{
				homePage=new HomePage(driver);
				homePage.menuBar.logOutFromMenuBar();
				loginPage=new LoginPage(driver);
				loginPage.navigateToApplication(configSite,userName,password,"");	
				configPage=new ConfigurationPage(driver);
				configPage.expandVaultFolder(documentVault);
				configPage.clickSettingsFolder(testData.get("SettingFolder"));

				if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {
					configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Show");
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				}
			}
			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally
		} //End finally

	} //End Test1_4_5A

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_4_5B",description="Test1_4_5B: Context menu : Hide")
	public void Test1_4_5B(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {
		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData=new ConcurrentHashMap <String, String>(dataValues);
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;

		try {

			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");
			Log.message("1. Logged in to Configuration Page", driver);

			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);	
			Thread.sleep(200);
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess())
					throw new Exception("Allow access to "+documentVault+" not saved correctly");
			}
			Log.message("2. Allowed access to 'Sample Vault'", driver);

			//Step-2: Click Vault from left panel of Configuration Page
			// configPage=new ConfigurationPage(driver);
			configPage.expandVaultFolder(documentVault);
			configPage.clickSettingsFolder(testData.get("SettingFolder"));
			Log.message("3. Clicked '"+documentVault+"' from left panel of Configuration Page", driver);


			//Step-2 : Select Show Metadata card and save the settings 
			if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("Hide")) {
				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Hide");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}
			Thread.sleep(1000);
			if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("HIDE")) {//Checks for the modified settings
				throw new Exception("Hide " + testData.get("SettingName") + " is not enabled after saving.");
			}

			Log.message("4. Hide " + testData.get("SettingName") + " is enabled as "+configPage.configurationPanel.getVaultCommands(testData.get("SettingName"))+"'", driver);

			//Step-3 : Login to the vault
			configPage.clickLogOut();//Logs out from the Configuration page
			Log.message("5. Logged out from the Configuration page", driver);
			loginPage.loginToWebApplication(userName, password,documentVault);
			Utils.waitForPageLoad(driver);
			Log.message("6. Logged in to "+ documentVault, driver);
			HomePage homePage=new HomePage(driver);
			homePage.clickSearchBtn(driver);
			Utils.fluentWait(driver);
			Log.message("7. Searched with default search criteria", driver);
			//Step-4 : Select any view and perform right click operation

			if (homePage.listView.itemCount() <=0) {//Launches and logs into the login page
				throw new Exception("No items in the list to perform right click operation.");
			}

			//Verification : To verify Show Command settings is reflected in MFWA page
			if (!homePage.openContextMenuDialog(driver))
				Log.pass("Test Passed. Context menu is not diplayed after enabling Hide " + testData.get("SettingName") + ".");
			else
				Log.fail("Test Failed. Context menu is displayed after enabling Hide " + testData.get("SettingName") + ".", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {
				loginPage=new LoginPage(driver);
				driver.get(configSite);
				configPage=new ConfigurationPage(driver);
				configPage.expandVaultFolder(documentVault);
				configPage.clickSettingsFolder(testData.get("SettingFolder"));

				if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {
					configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Show");
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				} }
			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally
		} //End finally

	} //End Test1_4_5B

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_4_6A",description="Test1_4_6A: Checkout prompt : Show", groups={"SKIP_JavaApplet"})
	public void Test1_4_6A(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {
		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData= new ConcurrentHashMap <String, String>(dataValues);
		String prevStatus=null;
		LoginPage loginPage=null;
		HomePage homePage=null;
		ConfigurationPage configPage=null;


		try {

			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");
			Log.message("1. Logged in to Configuration Page", driver);

			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);	
			Thread.sleep(200);
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				Utils.fluentWait(driver);

				configPage.clickSaveButton();
				Utils.fluentWait(driver);
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess())
					throw new Exception("Allow access to "+documentVault+" not saved correctly");
			}
			Log.message("2. Allowed access to 'Sample Vault'", driver);

			if (!configPage.configurationPanel.isJavaAppletEnabled())
				configPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Enable.Value);
			Utils.fluentWait(driver);

			Log.message("3. Java applet enabled in the view", driver);

			//Step-2: Click Vault from left panel of Configuration Page
			// configPage=new ConfigurationPage(driver);
			configPage.expandVaultFolder(documentVault);
			configPage.clickSettingsFolder(testData.get("SettingFolder"));
			Log.message("4. Clicked '"+documentVault+"' from left panel of Configuration Page", driver);


			//Step-2 : Select Show Metadata card and save the settings 
			prevStatus = configPage.configurationPanel.getVaultCommands(testData.get("SettingName")); //Gets the previous status of the command
			if (!prevStatus.equalsIgnoreCase("Show")) {
				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Show");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}
			Thread.sleep(1000);
			if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {//Checks for the modified settings
				throw new Exception("Show " + testData.get("SettingName") + " is not enabled after saving.");
			}

			Log.message("5. Show " + testData.get("SettingName") + " is enabled as "+configPage.configurationPanel.getVaultCommands(testData.get("SettingName")), driver);			
			//Step-3 : Login to the vault
			configPage.clickLogOut();//Logs out from the Configuration page
			Log.message("6. Logs out from the Configuration page", driver);

			loginPage.loginToWebApplication(userName, password, documentVault);
			Utils.waitForPageLoad(driver);
			homePage=new HomePage(driver);
			Log.message("7. Logged in to the vault (" + documentVault + ").", driver);

			//			//Step-4 : Perform search operation and Double click the document
			//			TaskPanel taskPanel = new TaskPanel(driver);
			//			if (!taskPanel.isAppletEnabled(currentDriver)) {
			//				throw new Exception("Applet is not enabled to proceed with verifying checkout prompt.");
			//			}
			//			Log.message("6. Applet is enabled to proceed with verifying checkout prompt");

			homePage.enterSearchText(testData.get("DocumentName"));
			homePage.clickSearchBtn(driver); //Clicks Search button
			Log.message("8. Search done for '"+testData.get("DocumentName")+"'", driver);
			if (!homePage.isDataInListView(driver,testData.get("DocumentName"),"Name")) {//Launches and logs into the login page
				throw new Exception("Invalid test data. Document (" + testData.get("DocumentName") +") does not exists in list.");
			}
			Log.message("9. Document found in search results", driver);

			homePage.doubleClickObjectFromListView(testData.get("DocumentName")) ; //Double clicks on the first item
			Log.message("10. Double clicked on the document found in search results", driver);
			Utils.fluentWait(driver);
			Utils.fluentWait(driver);
			//Verification : To verify Show Command settings is reflected in MFWA page
			if (homePage.isCheckOutPromptDisplayed())
				Log.pass("Test Passed. Checkout prompt is diplayed after enabling 'Show' " + testData.get("SettingName") + ".");
			else
				Log.fail("Test Failed. Checkout prompt is not displayed after enabling 'Show'" + testData.get("SettingName") + ".",driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {
				driver.get(configSite);
				configPage=new ConfigurationPage(driver);
				configPage.clickVaultFolder(documentVault);	
				Utils.fluentWait(driver);
				configPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);
				Utils.fluentWait(driver);
				configPage.clickSaveButton();
				Utils.fluentWait(driver);
				configPage.clickOKBtnOnSaveDialog();

				configPage.expandVaultFolder(documentVault);
				configPage.clickSettingsFolder(testData.get("SettingFolder"));


				if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {
					configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Show");
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				} }
			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally
		} //End finally

	} //End ConfigurationUI1_4_6A

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_4_6B",description="Test1_4_6B: Checkout prompt : Hide")
	public void Test1_4_6B(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {
		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData= new ConcurrentHashMap <String, String>(dataValues);
		String prevStatus=null;
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;

		HomePage homePage=null;

		try {

			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");	
			Log.message("1. Logged in to Configuration Page", driver);

			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);	
			Thread.sleep(200);
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess())
					throw new Exception("Allow access to "+documentVault+" not saved correctly");
			}
			Log.message("2. Allowed access to 'Sample Vault'", driver);


			//Step-2: Click Vault from left panel of Configuration Page
			configPage=new ConfigurationPage(driver);
			configPage.expandVaultFolder(documentVault);
			configPage.clickSettingsFolder(testData.get("SettingFolder"));
			Log.message("2. Clicked '"+documentVault+"' from left panel of Configuration Page", driver);


			//Step-2 : Select Show Metadata card and save the settings 
			prevStatus = configPage.configurationPanel.getVaultCommands(testData.get("SettingName")); //Gets the previous status of the command
			if (!prevStatus.equalsIgnoreCase("Hide")) {
				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Hide");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}
			Thread.sleep(1000);
			if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("HIDE")) {//Checks for the modified settings
				throw new Exception("Hide " + testData.get("SettingName") + " is not enabled after saving.");
			}
			Log.message("3. Hide " + testData.get("SettingName") + " is enabled as "+configPage.configurationPanel.getVaultCommands(testData.get("SettingName")), driver);

			//Step-3 : Login to the vault
			configPage.clickLogOut(); //Logs out from the Configuration page
			Log.message("4. Logged out from the Configuration page", driver);
			loginPage.loginToWebApplication(userName, password,documentVault);
			Utils.waitForPageLoad(driver);
			homePage=new HomePage(driver);
			Log.message("5. Logged in to the vault (" + documentVault + ").", driver);

			/*//Step-4 : Perform search operation and Double click the document
			TaskPanel taskPanel = new TaskPanel(driver);
			if (!taskPanel.isAppletEnabled(currentDriver)) {
				throw new Exception("Applet is not enabled to proceed with verifying checkout prompt.");
			}
			Log.message("6.Applet is enabled to proceed with verifying checkout prompt");*/

			//Search for document in Web Access
			homePage.enterSearchText(testData.get("DocumentName"));
			homePage.clickSearchBtn(driver); //Clicks Search button
			Log.message("6. Searched for '"+testData.get("DocumentName"), driver);

			//check if document exists in list view
			if (!homePage.listView.isItemExists(testData.get("DocumentName"), "Name")) {
				throw new Exception("Invalid test data. Document (" + testData.get("DocumentName") +") does not exists in list.");
			}
			//Double click on the document in list view
			homePage.doubleClickObjectFromListView(testData.get("DocumentName")) ; //Double clicks on the first item
			Log.message("7. Double clicked the document from search results view.", driver);

			//Verifies checkOut prompt dialog is displayed after enabling Show context menu
			if (!homePage.isCheckOutPromptDisplayed())
				Log.pass("Test Passed. Checkout prompt is diplayed after enabling Show " + testData.get("SettingName") + ".");
			else
				Log.fail("Test Failed. Checkout prompt is not displayed after enabling Show " + testData.get("SettingName") + ".", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {
				loginPage=new LoginPage(driver);
				driver.get(configSite);
				configPage=new ConfigurationPage(driver);
				configPage.expandVaultFolder(documentVault);
				configPage.clickSettingsFolder(testData.get("SettingFolder"));

				if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {
					configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Show");
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				} }
			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally
		} //End finally

	} //End Test1_4_6B

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_4_7A",description="Test1_4_7A: Advanced search : Show")
	public void Test1_4_7A(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData= new ConcurrentHashMap <String, String>(dataValues);
		String prevStatus=null;
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;


		try {

			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");	
			Log.message("1. Logged in to Configuration Page", driver);


			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);	
			Thread.sleep(200);
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess())
					throw new Exception("Allow access to "+documentVault+" not saved correctly");
			}
			Log.message("2. Allowed access to 'Sample Vault'", driver);


			//Step-2: Click Vault from left panel of Configuration Page
			configPage=new ConfigurationPage(driver);
			configPage.expandVaultFolder(documentVault);
			configPage.clickSettingsFolder(testData.get("SettingFolder"));
			Log.message("3. Clicked Vault from left panel of Configuration Page", driver);


			//Step-2 : Select Show Metadata card and save the settings 
			prevStatus = configPage.configurationPanel.getVaultCommands(testData.get("SettingName")); //Gets the previous status of the command
			if (!prevStatus.equalsIgnoreCase("Show")) {
				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Show");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}
			Thread.sleep(1000);
			if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {//Checks for the modified settings
				throw new Exception("Show " + testData.get("SettingName") + " is not enabled after saving.");
			}
			Log.message("4. Show " + testData.get("SettingName") + " is enabled & settings are saved.", driver);

			//Make sure 'Advanced search' option enabled
			configPage.expandVaultFolder(documentVault);
			Log.message("5. Clicked '"+documentVault+"' from left panel of Configuration Page", driver);

			configPage.clickSettingsFolder("Controls");
			if (configPage.configurationPanel.getVaultCommands("Advanced Search").equalsIgnoreCase("Hide")) {
				configPage.chooseConfigurationVaultSettings(driver, "Advanced Search","controls","Show");
				configPage.configurationPanel.saveSettings();
			}
			Log.message("6. Show " + testData.get("SettingName") + " is enabled and settings are saved.", driver);

			//Step-3 : Login to the vault
			configPage.clickLogOut(); //Logs out from the Configuration page
			Log.message("7.Logged out from the Configuration page", driver);

			loginPage.loginToWebApplication(userName, password, documentVault);
			Utils.waitForPageLoad(driver);
			Log.message("8. Logged in to the vault (" +documentVault + ").", driver);

			//Verifies Advances Search button is displayed after enabling Show Advanced Search 
			SearchPanel searchPanel = new SearchPanel(driver); //Instantiating SearchPanel wrapper class
			if (searchPanel.isAdvancedSearchIconDisplayed())
				Log.pass("Test Passed. Show " + testData.get("SettingName") + " is enabled successfully.");
			else
				Log.fail("Test Failed. Advanced Search button is not displayed after enabling show " + testData.get("SettingName") + ".", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {
				HomePage homePage=new HomePage(driver);
				homePage.menuBar.logOutFromMenuBar();
				loginPage=new LoginPage(driver);
				loginPage.navigateToApplication(configSite,userName,password,"");	
				configPage=new ConfigurationPage(driver);
				configPage.expandVaultFolder(documentVault);
				configPage.clickSettingsFolder(testData.get("SettingFolder"));

				if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {
					configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Show");
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				}
			}
			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally
		} //End finally


	} //End Test1_4_7A

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_4_7B",description="Test1_4_7B: Advanced search : Hide")
	public void Test1_4_7B(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData=new ConcurrentHashMap <String, String>(dataValues);
		String prevStatus=null;
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;


		try {

			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");	
			Log.message("1. Logged in to Configuration Page", driver);

			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);	
			Thread.sleep(200);
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess())
					throw new Exception("Allow access to "+documentVault+" not saved correctly");
			}
			Log.message("2. Allowed access to 'Sample Vault'", driver);


			//Step-2: Click Vault from left panel of Configuration Page
			configPage=new ConfigurationPage(driver);
			configPage.expandVaultFolder(documentVault);
			configPage.clickSettingsFolder(testData.get("SettingFolder"));
			Log.message("3. Clicked '"+documentVault+"' from left panel of Configuration Page", driver);


			//Step-2 : Select Show Metadata card and save the settings 
			prevStatus = configPage.configurationPanel.getVaultCommands(testData.get("SettingName")); //Gets the previous status of the command
			if (!prevStatus.equalsIgnoreCase("Hide")) {
				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Hide");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}
			Thread.sleep(1000);
			if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("HIDE")) {//Checks for the modified settings
				throw new Exception("Hide " + testData.get("SettingName") + " is not enabled after saving.");
			}

			Log.message("4. Hide " + testData.get("SettingName") + " is enabled & settings are saved.", driver);

			//Step-3 : Login to the vault
			configPage.clickLogOut(); //Logs out from the Configuration page
			Log.message("5. Logged out from the Configuration page", driver);

			loginPage.loginToWebApplication(userName, password, documentVault);
			Utils.waitForPageLoad(driver);
			Log.message("6. Logged in to the vault (" +documentVault + ").", driver);

			//Verifies Advances Search button is displayed after enabling Show Advanced Search 
			SearchPanel searchPanel = new SearchPanel(driver); //Instantiating SearchPanel wrapper class
			if (!searchPanel.isAdvancedSearchIconDisplayed()) 
				Log.pass("Test Passed. Hide " + testData.get("SettingName") + " is enabled successfully.");
			else
				Log.fail("Test Failed. Advanced Search button is displayed after enabling hide " + testData.get("SettingName") + ".", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {
				HomePage homePage=new HomePage(driver);
				homePage.menuBar.logOutFromMenuBar();
				loginPage=new LoginPage(driver);
				loginPage.navigateToApplication(configSite,userName,password,"");	
				configPage=new ConfigurationPage(driver);
				configPage.expandVaultFolder(documentVault);
				configPage.clickSettingsFolder(testData.get("SettingFolder"));
				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),prevStatus);
				if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {
					configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Show");
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				} }
			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally
		} //End finally


	} //End Test1_4_7B

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_4_8A",description="Test1_4_8A: Hidden properties : Show")
	public void Test1_4_8A(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {
		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData=new ConcurrentHashMap <String, String>(dataValues);
		String prevStatus=null;
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;

		HomePage homePage=null;
		try {

			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");	
			Log.message("1. Logged in to Configuration Page", driver);

			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);	
			Thread.sleep(200);
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess())
					throw new Exception("Allow access to "+documentVault+" not saved correctly");
			}
			Log.message("2. Allowed access to 'Sample Vault'", driver);


			//Step-2: Click Vault from left panel of Configuration Page 
			configPage=new ConfigurationPage(driver);
			configPage.expandVaultFolder(documentVault);
			configPage.clickSettingsFolder(testData.get("SettingFolder"));
			Log.message("2. Clicked '"+documentVault+"' from left panel of Configuration Page", driver);


			//Step-2 : Select Show Metadata card and save the settings 
			prevStatus = configPage.configurationPanel.getVaultCommands(testData.get("SettingName")); //Gets the previous status of the command
			if (!prevStatus.equalsIgnoreCase("Show")) {
				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Show");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}
			Thread.sleep(1000);
			if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {//Checks for the modified settings
				throw new Exception("Show " + testData.get("SettingName") + " is not enabled after saving.");
			}

			Log.message("3. Show " + testData.get("SettingName") + " is enabled as "+configPage.configurationPanel.getVaultCommands(testData.get("SettingName")), driver);

			//Step-3 : Login to the vault
			configPage.clickLogOut(); //Logs out from the Configuration page
			Log.message("4. Logs out from the Configuration page", driver);

			loginPage.navigateToApplication(webSite, testData.get("NonAdminUserName"), testData.get("NonAdminUserPassword"), documentVault);
			Utils.waitForPageLoad(driver);
			homePage=new HomePage(driver);
			Log.message("5. Logged in to the vault (" +documentVault + ").", driver);
			docName = testData.get("DocumentName");
			homePage.enterSearchText(docName);
			homePage.clickSearchBtn(driver); //Clicks Search button;

			if (!homePage.isDataInListView(driver,docName,"Name")) {//Launches and logs into the login page
				throw new Exception("Document (" +docName +") does not exists in list.");
			}

			homePage.listView.clickItem(docName); //Selects the document
			Log.message("6. Clicked object (" + docName +") found in search results.", driver);
			Thread.sleep(1000);

			Log.message("7. Performed search operation and selected the document(" + docName +").", driver);
			homePage.taskPanel.selectObjectActionsFromTaskPane("Properties");
			Log.message("8. 'Properties' action of the document(" + docName+") is selected from task pane.", driver);
			Thread.sleep(1500);

			docName=docName.substring(0,docName.length()-4);
			System.out.println(docName);

			//Select the display mode->Show metadata in Bottom pane
			if(!homePage.verifyMetadataCardDisplay(docName))
				throw new Exception("Metadatacard dialog not displayed");
			Log.message("9. Verified if '"+propName+"' Properties is displayed in the Properties pane", driver);

			MetadataCard metadataCard=new MetadataCard(driver);

			//Verifies Hidden property value is displayed
			if(metadataCard.getPropertyValue(propName).toUpperCase().contains("(HIDDEN)"))
				Log.pass("Test Passed. Hidden Property (" + propName + ") is displayed with value hidden in properties pane after enabling Show " +  testData.get("SettingName")  + ".");
			else 
				Log.fail("Test Failed. Hidden Property (" +propName + ") does not has value as hidden in properties pane after enabling Show " +  testData.get("SettingName")  + ".", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {
				loginPage=new LoginPage(driver);
				loginPage.navigateToApplication(configSite,userName,password,"");
				configPage=new ConfigurationPage(driver);
				configPage.expandVaultFolder(documentVault);
				configPage.clickSettingsFolder(testData.get("SettingFolder"));

				if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {
					configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Show");
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				} }
			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally
		} //End finally


	} //End Test1_4_8A

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_4_8B",description="Test1_4_8B: Hidden properties : Hide")
	public void Test1_4_8B(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {
		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData=new ConcurrentHashMap <String, String>(dataValues);
		String prevStatus=null;
		HomePage homePage=null;
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;

		try {
			driver.get(configSite);
			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.loginToConfigurationUI(userName,password);	
			Log.message("1. Logged in to Configuration Page", driver);

			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);	

			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess())
					throw new Exception("Allow access to "+documentVault+" not saved correctly");
			}
			Log.message("2. Allowed access to 'Sample Vault'", driver);


			//Step-2: Click Vault from left panel of Configuration Page
			configPage=new ConfigurationPage(driver);
			configPage.expandVaultFolder(documentVault);
			configPage.clickSettingsFolder(testData.get("SettingFolder"));
			Log.message("3. Clicked '"+documentVault+"' from left panel of Configuration Page", driver);


			//Step-2 : Select Show Metadata card and save the settings 
			prevStatus = configPage.configurationPanel.getVaultCommands(testData.get("SettingName")); //Gets the previous status of the command
			if (!prevStatus.equalsIgnoreCase("Hide")) {
				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Hide");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}

			if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("HIDE")) {//Checks for the modified settings
				throw new Exception("Hide " + testData.get("SettingName") + " is not enabled after saving.");
			}
			Log.message("4. Hide " + testData.get("SettingName") + " is enabled and settings are saved.", driver);

			//Make sure 'Advanced search' option enabled
			configPage.expandVaultFolder(documentVault);
			Log.message("5. Clicked '"+documentVault+"' from left panel of Configuration Page", driver);

			configPage.clickSettingsFolder("Controls");
			if (configPage.configurationPanel.getVaultCommands("Advanced Search").equalsIgnoreCase("Hide")) {
				configPage.chooseConfigurationVaultSettings(driver, "Advanced Search","controls","Show");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}
			Log.message("6. Show " + testData.get("SettingName") + " is enabled and settings are saved.", driver);

			//Step-3 : Login to the vault
			configPage.clickLogOut(); //Logs out from the Configuration page
			Log.message("7. Logged out from the Configuration page", driver);
			loginPage.navigateToApplication(webSite, testData.get("NonAdminUserName"), testData.get("NonAdminUserPassword"), documentVault);
			Utils.waitForPageLoad(driver);
			Log.message("8. Logged in to the vault (" +documentVault + ").", driver);			
			docName = testData.get("DocumentName");
			homePage=new HomePage(driver);
			//Step-4 : Perform search operation and Select the document
			homePage.enterSearchText(docName);
			homePage.clickSearchBtn(driver); //Clicks Search button;

			if (!homePage.listView.isItemExists(docName,"Name")) {//Launches and logs into the login page
				throw new Exception("Document (" +docName +") does not exists in list.");
			}

			homePage.listView.clickItem(docName); //Selects the document
			Utils.fluentWait(driver);

			Log.message("9. Performed search operation and selected the document(" + docName +").", driver);
			homePage.taskPanel.selectObjectActionsFromTaskPane("Properties");
			Log.message("10. 'Properties' action of the document(" + docName+") is selected from task pane.", driver);
			Utils.fluentWait(driver);
			docName=docName.substring(0,docName.length()-4);
			System.out.println(docName);

			//Select the display mode->Show metadata in Bottom pane
			if(!homePage.verifyMetadataCardDisplay(docName))
				throw new Exception("Metadatacard dialog not displayed");
			Log.message("10. Verified if '"+propName+"' Properties is displayed in the Properties pane");

			MetadataCard metadataCard=new MetadataCard(driver);

			//Verifies Hidden property value is displayed
			if(!metadataCard.propertyExists(propName))
				Log.pass("Test Passed. Hidden Property (" + testData.get("PropertyName") + ") is not displayed in properties pane after enabling Hide " + testData.get("SettingName") + ".");
			else
				Log.fail("Test Failed. Hidden Property (" + testData.get("PropertyName") + ") is displayed in properties pane after enabling Hide " + testData.get("SettingName") + ".", driver);	

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {
				loginPage=new LoginPage(driver);
				loginPage.navigateToApplication(configSite,userName,password,"");
				configPage=new ConfigurationPage(driver);
				configPage.expandVaultFolder(documentVault);
				configPage.clickSettingsFolder(testData.get("SettingFolder"));

				if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {
					configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Show");
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				} }
			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally
		} //End finally

	} //End Test1_4_8B

	//	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_4_10",description="Test1_4_10: Verify 'Reset' button for the last modified setting ")
	//	public void Test1_4_10(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {
	//		//Variable Declaration
	//		driver = WebDriverUtils.getDriver();
	//		Verify 'Reset' button for the last modified setting ");
	//		
	//		ConcurrentHashMap <String, String> testData=new ConcurrentHashMap <String, String>(dataValues);
	//		String prevStatus=null;
	//		String currStatus=null;
	//		LoginPage loginPage=null;
	//		ConfigurationPage configPage=null;
	//		
	//		try {
	//			
	//			//Step-1: Login to Configuration Page
	//			loginPage=new LoginPage(driver);
	//			loginPage.navigateToApplication(configSite,userName,password,"");	
	//			Log.message("1. Logged in to Configuration Page", driver);
	//			
	//			configPage=new ConfigurationPage(driver);
	//			configPage.clickVaultFolder(documentVault);	
	//			Thread.sleep(200);
	//			if (!configPage.configurationPanel.getVaultAccess()) {
	//				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
	//				configPage.clickSaveButton();
	//				configPage.clickOKBtnOnSaveDialog();
	//				if (!configPage.configurationPanel.getVaultAccess())
	//					throw new Exception("Allow access to "+documentVault+" not saved correctly");
	//			}
	//			Log.message("2. Allowed access to 'Sample Vault'");
	//			
	//			
	//			//Step-2: Click Vault from left panel of Configuration Page
	//			configPage=new ConfigurationPage(driver);
	//			configPage.expandVaultFolder(documentVault);
	//			configPage.clickSettingsFolder(testData.get("SettingFolder"));
	//			Log.message("2. Clicked '"+documentVault+"' from left panel of Configuration Page");
	//					
	//			
	//			//Step-2 : Select Show Metadata card and save the settings 
	//			prevStatus = configPage.configurationPanel.getVaultCommands(testData.get("SettingName")); //Gets the previous status of the command
	//			
	//			if (prevStatus.toUpperCase().equalsIgnoreCase("SHOW"))
	//				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Hide");
	//			else
	//				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Show");
	//			
	//			currStatus = configPage.configurationPanel.getVaultCommands(testData.get("SettingName") ); //Gets the status of retail latest search settings
	//			
	//			if (currStatus.equalsIgnoreCase(prevStatus)) { //Checks if settings are modified
	//				throw new Exception("Settings are not modified.");
	//			}
	//			
	//			Log.message("3. Context menu setting value is modified as '"+currStatus+"' in the vault.");
	//			
	//			//Verification : To verify the Reset button resets the last made changes 
	//			configPage.clickResetButton(); //Clicks Reset button
	//		
	//			//Verifies the latest selection is reset
	//			if (configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).trim().equalsIgnoreCase(prevStatus))
	//				Log.pass("Test Passed. Reset button resets the last modified setting as Previous Status:"+prevStatus+" and Current Status:"+currStatus+".");
	//			else
	//				Log.fail("Test Failed. Reset button does not reset the last modified setting as Previous Status:"+prevStatus+" and Current Status:"+currStatus+".",driver);
	//			
	//			} //End try
	//			
	//			catch (Exception e) {
	//				Log.exception(e, driver);
	//			} //End catch
	//			
	//			finally {
	//					configPage=new ConfigurationPage(driver);
	//					configPage.expandVaultFolder(documentVault);
	//					configPage.clickSettingsFolder(testData.get("SettingFolder"));
	//					
	//					if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {
	//						configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Show");
	//						configPage.clickSaveButton();
	//						configPage.clickOKBtnOnSaveDialog();
	//					}
	//					
	//				driver.quit();				
	//			} //End finally
	//		
	//	
	//	} //End Test1_4_10

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_5_1",description="Last modified in Task Area")
	public void Test1_5_1(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData=new ConcurrentHashMap <String, String>(dataValues);
		String prevStatus=null;
		ConfigurationPage configPage=null;

		try {
			driver.get(configSite);
			//Step-1: Login to Configuration Page
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToConfigurationUI(userName,password);
			Log.message("1. Logged in to Configuration Page", driver);

			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);	
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess())
					throw new Exception("Allow access to "+documentVault+" not saved correctly");
			}
			Log.message("2. Allowed access to 'Sample Vault'", driver);


			//Step-2: Click Vault from left panel of Configuration Page
			configPage=new ConfigurationPage(driver);
			configPage.expandVaultFolder(documentVault);
			configPage.clickSettingsFolder(testData.get("SettingFolder"));
			Log.message("3. Clicked '"+documentVault+"' from left panel of Configuration Page", driver);

			//Step-2 : Select Show Metadata card and save the settings 
			prevStatus = configPage.configurationPanel.getVaultCommands(testData.get("SettingName")); //Gets the previous status of the command

			if (prevStatus.toUpperCase().equalsIgnoreCase("SHOW")) 
				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Hide");
			else 
				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Show");

			driver.findElement(By.cssSelector("input[id='saveSettings']")).click();
			Thread.sleep(200);
			//Gets current date and time
			currDateTime=getTime();
			configPage.clickOKBtnOnSaveDialog();
			Log.message("4. " + testData.get("SettingName") + " is modified and settings are saved.", driver);

			//Step-3: Click the 'General Settings-General' Folder
			configPage.clickTreeViewItem("General settings");
			//Verification : To verify last modified date and time are right in the vault configuration page
			configPage.clickSettingsFolder(testData.get("SettingFolder"));

			configPageTime=configPage.configurationPanel.getLastModifiedDateTime();
			//Verifies the time in vault configuration page is same as the system date and time at the time of modification
			if (configPageTime.compareTo(currDateTime)<=5 || configPageTime.compareTo(currDateTime)>=0)
				Log.pass("Test Passed. Last modified date and time is '"+currDateTime+"', and Actual Last modified date and time is displayed as '"+configPageTime+"' on modifying the configuration-Task Area page.");
			else
				Log.fail("Test Failed. Last modified date and time is '"+currDateTime+"', but Actual Last modified date and time is displayed as '"+configPageTime+"'", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			try {
				configPage=new ConfigurationPage(driver);
				configPage.expandVaultFolder(documentVault);
				configPage.clickSettingsFolder(testData.get("SettingFolder"));

				if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {
					configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"),testData.get("OptionHeader"),"Show");
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				} }

			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally
		} //End finally

	} //End Test1_5_1	

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_5_2A",description="Test1_5_2A: New commands : Show")
	public void Test1_5_2A(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData= new ConcurrentHashMap <String, String>(dataValues);
		String prevStatus=null;
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;
		HomePage homePage=null;
		TaskPanel taskPanel = null;

		try {

			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");
			Log.message("1. Logged in to Configuration Page", driver);

			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);	
			Thread.sleep(200);
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess())
					throw new Exception("Allow access to "+documentVault+" not saved correctly");
			}
			Log.message("2. Allowed access to 'Sample Vault'", driver);


			//Step-2: Click Vault from left panel of Configuration Page
			configPage=new ConfigurationPage(driver);
			configPage.expandVaultFolder(documentVault);
			configPage.clickSettingsFolder(testData.get("SettingFolder"));
			Log.message("3. Clicked '"+documentVault+"' from left panel of Configuration Page", driver);


			prevStatus = configPage.configurationPanel.getVaultCommands(testData.get("SettingName")); //Gets the previous status of the command
			//Step-2 : Select Show New commands and save the settings 
			if (!prevStatus.equalsIgnoreCase("Show")) {
				configPage.configurationPanel.setVaultCommands(testData.get("SettingName"), "Show");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}
			if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {//Checks for the modified settings
				throw new Exception("Show " + testData.get("SettingName") + " is not enabled after saving.");
			}
			Log.message("4. Show " + testData.get("SettingName") + " is enabled and settings are saved.", driver);

			//Step-3 : Login to the vault
			configPage.clickLogOut(); //Logs out from the Configuration page
			Log.message("5. Logged out from the Configuration page", driver);
			loginPage.loginToWebApplication(userName, password, documentVault);
			Utils.waitForPageLoad(driver);
			Log.message("6. Logged in to the vault (" +documentVault + ").", driver);

			//Verification : To verify Show Command settings is reflected in MFWA page
			taskPanel = new TaskPanel(driver); //Instantiating TaskPanel wrapper class

			//Verifies New Menu is not displayed after enabling hide New Menu in task pane 
			if (taskPanel.isTaskPaneNewDisplayed())
				Log.pass("Test Passed. '" + testData.get("SettingName") + "' in TaskPane area are displayed successfully, after enabling Showing " + testData.get("SettingName") + ".");
			else
				Log.fail("Test Failed. " + testData.get("SettingName") + " doesnot exists in task area after enabling Showing " + testData.get("SettingName") + ".", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {
				taskPanel = new TaskPanel(driver); //Instantiating TaskPanel wrapper class
				homePage = new HomePage(driver);
				homePage.menuBar.logOutFromMenuBar();
				loginPage=new LoginPage(driver);
				loginPage.navigateToApplication(configSite,userName,password,"");		
				configPage=new ConfigurationPage(driver);
				configPage.expandVaultFolder(documentVault);
				configPage.clickSettingsFolder(testData.get("SettingFolder"));

				if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {
					configPage.configurationPanel.setVaultCommands(testData.get("SettingName"), "Show");
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				}
			}
			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally
		} //End finally


	} //End Test1_5_2A

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_5_2B",description="Test1_5_2B: New commands : Hide")
	public void Test1_5_2B(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();

		ConcurrentHashMap <String, String> testData= new ConcurrentHashMap <String, String>(dataValues);
		
		String prevStatus=null;
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;
		HomePage homePage=null;
		TaskPanel taskPanel = null;
		try {

			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");	
			Log.message("1. Logged in to Configuration Page", driver);

			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);	
			Thread.sleep(200);
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess())
					throw new Exception("Allow access to "+documentVault+" not saved correctly");
			}
			Log.message("2. Allowed access to 'Sample Vault'", driver);


			//Step-2: Click Vault from left panel of Configuration Page

			configPage=new ConfigurationPage(driver);
			configPage.expandVaultFolder(documentVault);
			configPage.clickSettingsFolder(testData.get("SettingFolder"));
			Log.message("3. Clicked '"+documentVault+"' from left panel of Configuration Page", driver);


			prevStatus = configPage.configurationPanel.getVaultCommands(testData.get("SettingName")); //Gets the previous status of the command
			//Step-2 : Select Show New commands and save the settings 
			if (!prevStatus.equalsIgnoreCase("Hide")) {
				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"), testData.get("OptionHeader"), "Hide");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}
			if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("HIDE")) {//Checks for the modified settings
				throw new Exception("Hide " + testData.get("SettingName") + " is not enabled after saving.");
			}
			Log.message("4. Hide " + testData.get("SettingName") + " is enabled and settings are saved.", driver);

			//Step-3 : Login to the vault
			configPage.clickLogOut(); //Logs out from the Configuration page
			Log.message("5. Logs out from the Configuration page", driver);
			loginPage.loginToWebApplication(userName, password, documentVault);
			Utils.waitForPageLoad(driver);
			Log.message("6. Logged in to the vault (" +documentVault + ").", driver);

			//Verification : To verify Show Command settings is reflected in MFWA page
			taskPanel = new TaskPanel(driver); //Instantiating TaskPanel wrapper class
			//Verifies New Menu is not displayed after enabling hide New Menu in task pane 
			if (!taskPanel.isTaskPaneNewDisplayed())
				Log.pass("Test Passed. '" + testData.get("SettingName") + "' in TaskPane area are Hidden successfully, after enabling Hiding " + testData.get("SettingName") + ".");
			else
				Log.fail("Test Failed. " + testData.get("SettingName") + " exists in task area after enabling Hiding " + testData.get("SettingName") + ".", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {
				taskPanel = new TaskPanel(driver); //Instantiating TaskPanel wrapper class
				homePage=new HomePage(driver);
				homePage.menuBar.logOutFromMenuBar();
				loginPage=new LoginPage(driver);
				loginPage.navigateToApplication(configSite,userName,password,"");		
				configPage=new ConfigurationPage(driver);
				configPage.expandVaultFolder(documentVault);
				configPage.clickSettingsFolder(testData.get("SettingFolder"));


				if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {
					configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"), testData.get("OptionHeader"), "Show");
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				} }
			catch(Exception e)
			{	
				Log.exception(e, driver);
			}
			finally {
				driver.quit();
			}
		} //End finally

	} //End Test1_5_2B
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_5_2C",description="Test1_5_2C: Verify Task Pane->Go To commands : Show")
	public void Test1_5_2C(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();

		ConcurrentHashMap <String, String> testData=new ConcurrentHashMap <String, String>(dataValues);
		
		String prevStatus=null;
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;

		HomePage homePage = null;
		try {

			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");
			Log.message("1. Logged in to Configuration Page", driver);

			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);	
			Thread.sleep(200);
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess())
					throw new Exception("Allow access to "+documentVault+" not saved correctly");
			}
			Log.message("2. Allowed access to 'Sample Vault'", driver);


			//Step-2: Click Vault from left panel of Configuration Page
			configPage=new ConfigurationPage(driver);
			configPage.expandVaultFolder(documentVault);
			configPage.clickSettingsFolder(testData.get("SettingFolder"));
			Log.message("3. Clicked '"+documentVault+"' from left panel of Configuration Page", driver);


			prevStatus = configPage.configurationPanel.getVaultCommands(testData.get("SettingName")); //Gets the previous status of the command
			//Step-2 : Select Show New commands and save the settings 
			if (!prevStatus.equalsIgnoreCase("Show")) {
				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"), testData.get("OptionHeader"), "Show");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}
			if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {//Checks for the modified settings
				throw new Exception("Show " + testData.get("SettingName") + " is not enabled after saving.");
			}
			Log.message("4. Show " + testData.get("SettingName") + " is enabled and settings are saved.", driver);

			//Step-3 : Login to the vault
			configPage.clickLogOut(); //Logs out from the Configuration page
			Log.message("5. Logged out from the Configuration page", driver);	
			loginPage.loginToWebApplication(userName, password, documentVault);
			Utils.waitForPageLoad(driver);
			Log.message("6. Logged in to the vault (" +documentVault + ").", driver);

			homePage = new HomePage(driver); //Instantiating TaskPanel wrapper class
			//Verifies GoTo Menu is not displayed after enabling hide GoTo Menu in task pane 
			if (homePage.isGoToItemExists(driver,testData.get("SettingName"))) 
				Log.pass("Test Passed. '" + testData.get("SettingName") + "' in TaskPane area is displayed successfully, after enabling Show " + testData.get("SettingName") + ".");
			else 
				Log.fail("Test Failed. " + testData.get("SettingName") + " does not exists in task area after enabling Show option of '" + testData.get("SettingName") + "'.", driver);


		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {
				homePage = new HomePage(driver); //Instantiating TaskPanel wrapper class
				loginPage=new LoginPage(driver);
				driver.get(configSite);
				configPage=new ConfigurationPage(driver);
				configPage.expandVaultFolder(documentVault);
				configPage.clickSettingsFolder(testData.get("SettingFolder"));


				if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {
					configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"), testData.get("OptionHeader"), "Show");
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				} }
			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally
		} //End finally

	} //End Test1_5_2C

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_5_2D",description="Test1_5_2D: Verify Task Pane->Go To commands : Hide")
	public void Test1_5_2D(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();

		ConcurrentHashMap <String, String> testData=new ConcurrentHashMap <String, String>(dataValues);
		String prevStatus=null;
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;
		String defaultView="Home";

		HomePage homePage =null;
		
		try {


			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");	
			Log.message("1. Logged in to Configuration Page", driver);

			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);	
			Thread.sleep(200);
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess())
					throw new Exception("Allow access to "+documentVault+" not saved correctly");
			}
			Log.message("2. Allowed access to 'Sample Vault'", driver);


			//Step-2: Click Vault from left panel of Configuration Page
			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);
			Log.message("3. Clicked 'Sample vault' from left panel of Configuration Page", driver);


			//Step-2 : Set layout for the vault as default
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess()) 
					throw new Exception("Error while allowing access to Vault");
			}

			if (!configPage.configurationPanel.getDefaultView().trim().equalsIgnoreCase(defaultView.trim())) {
				configPage.configurationPanel.setDefaultView(defaultView);
			}
			configPage.clickSaveButton();
			configPage.clickOKBtnOnSaveDialog();

			configPage.expandVaultFolder(documentVault);
			configPage.clickSettingsFolder(testData.get("SettingFolder"));
			Log.message("4. Clicked '"+documentVault+"' from left panel of Configuration Page", driver);

			prevStatus = configPage.configurationPanel.getVaultCommands(testData.get("SettingName")); //Gets the previous status of the command
			//Step-2 : Select Show New commands and save the settings 
			if (!prevStatus.equalsIgnoreCase("Hide")) {
				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"), testData.get("OptionHeader"), "Hide");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}
			if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("HIDE")) {//Checks for the modified settings
				throw new Exception("Hide " + testData.get("SettingName") + " is not enabled after saving.");
			}
			Log.message("5. Hide " + testData.get("SettingName") + " is enabled settings are saved.", driver);

			//Step-3 : Login to the vault
			configPage.clickLogOut(); //Logs out from the Configuration page
			Log.message("6.Logged out from the Configuration page", driver);

			loginPage.loginToWebApplication(userName, password, documentVault);
			Utils.waitForPageLoad(driver);
			Log.message("7. Logged in to the vault (" +documentVault + ").", driver);

			//Verification : To verify Show Command settings is reflected in MFWA page
			homePage = new HomePage(driver); //Instantiating TaskPanel wrapper class
			//Verifies New Menu is not displayed after enabling hide New Menu in task pane 
			if (!homePage.isGoToItemExists(driver, testData.get("SettingName"))) 
				Log.pass("Test Passed. '" + testData.get("SettingName") + "' does not exists in task area after enabling Hide option of '" + testData.get("SettingName") + "'.");
			else 
				Log.fail("Test Failed. '"+testData.get("SettingName") + "' in TaskPane area is displayed , after enabling Hide " + testData.get("SettingName") + "." , driver);


		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {
				driver.get(configSite);
				configPage=new ConfigurationPage(driver);
				configPage.expandVaultFolder(documentVault);
				configPage.clickSettingsFolder(testData.get("SettingFolder"));


				if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {
					configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"), testData.get("OptionHeader"), "Show");
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				} }
			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {
				driver.quit();	
			} //End finally
		} //End finally

	} //End Test1_5_2D

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_5_2E",description="Test1_5_2E: Verify Task Pane->LogOut : Show")
	public void Test1_5_2E(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData=new ConcurrentHashMap <String, String>(dataValues);
		String prevStatus=null;
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;


		try {
			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");	
			Log.message("1. Logged in to Configuration Page", driver);

			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);	
			Thread.sleep(200);
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess())
					throw new Exception("Allow access to "+documentVault+" not saved correctly");
			}
			Log.message("2. Allowed access to 'Sample Vault'", driver);


			//Step-2: Click Vault from left panel of Configuration Page
			configPage=new ConfigurationPage(driver);
			configPage.expandVaultFolder(documentVault);
			configPage.clickSettingsFolder(testData.get("SettingFolder"));
			Log.message("3. Clicked '"+documentVault+"' from left panel of Configuration Page", driver);


			prevStatus = configPage.configurationPanel.getVaultCommands(testData.get("SettingName")); //Gets the previous status of the command
			//Step-2 : Select Show New commands and save the settings 
			if (!prevStatus.equalsIgnoreCase("Show")) {
				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"), testData.get("OptionHeader"), "Show");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}

			if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {//Checks for the modified settings
				throw new Exception("Show " + testData.get("SettingName") + " is not enabled after saving.");
			}
			Log.message("4. Show " + testData.get("SettingName") + " is enabled and settings are saved.", driver);

			//Step-3 : Login to the vault
			configPage.clickLogOut(); //Logs out from the Configuration page
			Log.message("5. Logged out from the Configuration page", driver);

			loginPage.loginToWebApplication(userName, password, documentVault);
			Utils.waitForPageLoad(driver);
			Log.message("6. Logged in to the vault (" +documentVault + ").", driver);

			//Verification : To verify Show Command settings is reflected in MFWA page
			TaskPanel taskPanel = new TaskPanel(driver); //Instantiating TaskPanel wrapper class
			//Verifies New Menu is not displayed after enabling hide New Menu in task pane 
			if (taskPanel.isItemExists(testData.get("SettingName"))) 
				Log.pass("Test Passed. '" + testData.get("SettingName") + "  exists in task area after enabling Show option of '" + testData.get("SettingName") + "'.");
			else 
				Log.fail("Test Failed. "+testData.get("SettingName") + "' in TaskPane area doesnot displayed, after enabling Show " + testData.get("SettingName") + "." , driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {

				loginPage=new LoginPage(driver);
				driver.get(configSite);
				configPage=new ConfigurationPage(driver);
				configPage.expandVaultFolder(documentVault);
				configPage.clickSettingsFolder(testData.get("SettingFolder"));


				if (configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {
					configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"), testData.get("OptionHeader"), "Hide");
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				}}
			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally
		} //End finally

	} //End Test1_5_2E
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_5_2E",description="Test1_5_2E: Verify Task Pane->LogOut : Hide")
	public void Test1_5_2F(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData=new ConcurrentHashMap <String, String>(dataValues);
		String prevStatus=null;
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;

		TaskPanel taskPanel =null;

		try {
			driver.get(configSite);
			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.loginToConfigurationUI(userName,password);	
			Log.message("1. Logged in to Configuration Page", driver);

			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);	
			Thread.sleep(200);
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess())
					throw new Exception("Allow access to "+documentVault+" not saved correctly");
			}
			Log.message("2. Allowed access to 'Sample Vault'", driver);


			//Step-2: Click Vault from left panel of Configuration Page
			configPage=new ConfigurationPage(driver);
			configPage.expandVaultFolder(documentVault);
			configPage.clickSettingsFolder(testData.get("SettingFolder"));
			Log.message("3. Clicked '"+documentVault+"' from left panel of Configuration Page", driver);


			prevStatus = configPage.configurationPanel.getVaultCommands(testData.get("SettingName")); //Gets the previous status of the command
			//Step-2 : Select Show New commands and save the settings 
			if (!prevStatus.equalsIgnoreCase("Hide")) {
				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"), testData.get("OptionHeader"), "Hide");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();

			}
			if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("HIDE")) {//Checks for the modified settings
				throw new Exception("Hide " + testData.get("SettingName") + " is not enabled after saving.");
			}
			Log.message("4. Hide " + testData.get("SettingName") + " is enabled and settings are saved.", driver);

			//Step-3 : Login to the vault
			configPage.clickLogOut(); //Logs out from the Configuration page
			Log.message("5. Logs out from the Configuration page", driver);

			loginPage.loginToWebApplication(userName, password, documentVault);
			Utils.waitForPageLoad(driver);
			Log.message("6. Logged in to the vault (" +documentVault + ").", driver);

			//Verification : To verify Show Command settings is reflected in MFWA page
			taskPanel = new TaskPanel(driver); //Instantiating TaskPanel wrapper class
			//Verifies New Menu is not displayed after enabling hide New Menu in task pane 
			if (!taskPanel.isItemExists(testData.get("SettingName"))) 
				Log.pass("Test Passed. '" + testData.get("SettingName") + " doesnot exists in task area after enabling Hide option of '" + testData.get("SettingName") + "'.");
			else 
				Log.fail("Test Failed. "+testData.get("SettingName") + "' in TaskPane area is displayed, after enabling Hide " + testData.get("SettingName") + "." , driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {
				driver.get(configSite);
				configPage=new ConfigurationPage(driver);
				configPage.expandVaultFolder(documentVault);
				configPage.clickSettingsFolder(testData.get("SettingFolder"));


				if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {
					configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"), testData.get("OptionHeader"), "Show");
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				} }
			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally
		} //End finally

	} //End Test1_5_2F

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_5_3A",description="Test1_5_3A: Properties/ History/ Show Members/"+
			"Show Subobjects/ Make Copy/ Open (Download)/ Download File : Show")
	public void Test1_5_3A(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();

		ConcurrentHashMap <String, String> testData=new ConcurrentHashMap <String, String>(dataValues);
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;
		try {

			
			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");
			Log.message("1. Logged in to Configuration Page", driver);

			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);	
			Thread.sleep(200);
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess())
					throw new Exception("Allow access to "+documentVault+" not saved correctly");
			}
			Log.message("2. Allowed access to 'Sample Vault'", driver);


			//Step-2: Click Vault from left panel of Configuration Page
			configPage=new ConfigurationPage(driver);
			configPage.clickTreeViewItem(documentVault);

			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess()) 
					throw new Exception("Error while allowing access to Vault");
			}

			//if Force selection is enabled, uncheck the option
			if(configPage.configurationPanel.getForceFollowingSelection()) {
				configPage.configurationPanel.setForceFollowingSelection(false);
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}

			//Make sure 'Advanced search' option enabled
			configPage.expandVaultFolder(documentVault);
			Log.message("3. Clicked '"+documentVault+"' from left panel of Configuration Page", driver);

			configPage.clickSettingsFolder("Controls");

			if (configPage.configurationPanel.getVaultCommands("Advanced Search").equalsIgnoreCase("Hide")) {
				configPage.chooseConfigurationVaultSettings(driver, "Advanced Search","controls","Show");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}
			Log.message("4. Show " + testData.get("SettingName") + " is enabled and settings are saved.", driver);

			configPage.expandVaultFolder(documentVault);
			configPage.clickSettingsFolder(testData.get("SettingFolder"));

			//Step-2 : Select Show Properties and save the settings 
			if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("Show")) {
				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"), testData.get("OptionHeader"), "Show");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}

			if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {//Checks for the modified settings
				throw new Exception("Show " + testData.get("SettingName") + " is not enabled after saving.");
			}

			//Step-3 : Login to the vault
			configPage.clickLogOut(); //Logs out from the Configuration page
			Log.message("5. Logged out from the Configuration page");
			loginPage.loginToWebApplication(userName, password, documentVault);
			Utils.waitForPageLoad(driver);
			HomePage homePage=new HomePage(driver);
			Log.message("6. Logged in to the vault (" + documentVault + ").", driver);

			//Step-4 : Search for documents and select any document
			SearchPanel searchPanel=new SearchPanel(driver);
			searchPanel.showAdvancedSearchOptions(driver);

			if (testData.get("ObjectType").trim().equalsIgnoreCase("Documents")) {
				searchPanel.selectSearchOptionsUsingObject(driver, MFilesObjectList.DOCUMENT, testData.get("ObjectType")).contains("Documents");
			}
			else if (testData.get("ObjectType").trim().equalsIgnoreCase("Customers")) {
				searchPanel.selectSearchOptionsUsingObject(driver, MFilesObjectList.CUSTOMER, testData.get("ObjectType")).contains("Customers");
			}
			else if (testData.get("ObjectType").trim().equalsIgnoreCase("Contact persons")) {
				searchPanel.selectSearchOptionsUsingObject(driver, MFilesObjectList.CONTACT_PERSON, testData.get("ObjectType")).contains("Contact persons");
			}
			else if (testData.get("ObjectType").trim().equalsIgnoreCase("Projects")) {
				searchPanel.selectSearchOptionsUsingObject(driver, MFilesObjectList.PROJECT, testData.get("ObjectType")).contains("Projects");
			}
			else if (testData.get("ObjectType").trim().equalsIgnoreCase("Assignments")) {
				searchPanel.selectSearchOptionsUsingObject(driver, MFilesObjectList.ASSIGNMENT, testData.get("ObjectType")).contains("Assignments");
			}
			Log.message("7. Opened Advanced Search options", driver);
			//Search for the document
			homePage.enterSearchText(testData.get("ObjectName"));
			homePage.clickSearchBtn(driver); //Perform search operation

			//Verify if document found
			if (!homePage.listView.isItemExists(testData.get("ObjectName"), "Name")) {//Checks if object exists in the list
				throw new Exception("Object (" + testData.get("ObjectName") + ") does not exists in the list.");
			}
			Log.message("8. Search done with object '"+testData.get("ObjectName")+"' and object displayed", driver);

			homePage.listView.clickItem(testData.get("ObjectName"));
			//Verify if 'Checkout icon displayed
			if(homePage.isCheckedOutOverLayDisplayed())
			{
				homePage.selectContextMenuItemFromListView("UndoCheckout");
				//Click Yes button
				homePage.clickYesButton();
				//Wait for invisibility of 'wait overlay' 
				new WebDriverWait(driver,60).ignoring(NoSuchElementException.class) 
				.pollingEvery(125,TimeUnit.MILLISECONDS)
				.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div[class='list_overlay'][style*='waiting_overlay.png']")));
			}
			Log.message("9. Verified if object is not checkedout", driver);
			//Select the object from list view
			if (!homePage.listView.clickItem(testData.get("ObjectName"))) {//Checks if object selected in the list
				throw new Exception("Object (" + testData.get("ObjectName") + ") is not selected in the list.");
			}
			Log.message("10. Object (" + testData.get("ObjectName") + ") is selected in the list.", driver);

			//Verification : To verify Show Command settings is reflected in MFWA page
			TaskPanel taskPanel = new TaskPanel(driver); //Instantiating TaskPanel wrapper class

			//Verifies New Menu is not displayed after enabling hide New Menu in task pane 
			if (testData.get("SettingName").equalsIgnoreCase("Move into State commands")) {
				if (taskPanel.isMoveToStateOptionsAvailable(testData.get("TaskPaneName")))
					Log.pass("Test Passed. Show '" + testData.get("SettingName") + "' is enabled successfully.");
				else
					Log.fail("Test Failed. " + testData.get("SettingName") + " does not exists in task area after enabling show " + testData.get("SettingName") + ".", driver);

			}
			else {
				if (taskPanel.isItemExists(testData.get("SettingName")))
					Log.pass("Test Passed. Show '" + testData.get("SettingName") + "' is enabled successfully.");
				else
					Log.fail("Test Failed. " + testData.get("SettingName") + " is disabled in operation menu after enabling show " + testData.get("SettingName") + ".", driver);
			}

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {
				Utility.navigateToPage(driver, configSite);
				configPage=new ConfigurationPage(driver);
				configPage.expandVaultFolder(documentVault);
				configPage.clickSettingsFolder(testData.get("SettingFolder"));

				if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {
					configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"), testData.get("OptionHeader"), "Show");
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				} }
			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally
		} //End finally
	} //End Test1_5_3A

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_5_3B",description="Test1_5_3B: Properties/ History/Show Members/"+
			"Show Subobjects/ Make Copy/ Open (Download)/ Download File : Hide")
	public void Test1_5_3B(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();

		ConcurrentHashMap <String, String> testData=new ConcurrentHashMap <String, String>(dataValues);
		String prevStatus=null;
		
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;
		try {


			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");	
			Log.message("1. Logged in to Configuration Page", driver);



			//Step-2: Click Vault from left panel of Configuration Page
			configPage=new ConfigurationPage(driver);
			configPage.clickTreeViewItem(documentVault);
			//Allow access to Vault
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess()) 
					throw new Exception("Error while allowing access to Vault");
			}
			//if Force selection is enabled, uncheck the option
			if(configPage.configurationPanel.getForceFollowingSelection()) {
				configPage.configurationPanel.setForceFollowingSelection(false);
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}

			configPage.expandVaultFolder(documentVault);
			configPage.clickSettingsFolder("Controls");

			Log.message("2. Clicked '"+documentVault+"' from left panel of Configuration Page", driver);

			if (configPage.configurationPanel.getVaultCommands("Advanced Search").equalsIgnoreCase("Hide")) {
				configPage.chooseConfigurationVaultSettings(driver, "Advanced Search","controls","Show");
				configPage.configurationPanel.saveSettings();
			}

			configPage.expandVaultFolder(documentVault);
			configPage.clickSettingsFolder(testData.get("SettingFolder"));

			//Step-2 : Select Show Properties and save the settings 
			prevStatus = configPage.configurationPanel.getVaultCommands(testData.get("SettingName")); //Gets the previous status of the command
			if (!prevStatus.equalsIgnoreCase("Hide")) {
				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"), testData.get("OptionHeader"), "Hide");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}

			if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("HIDE")) {//Checks for the modified settings
				throw new Exception("Hide " + testData.get("SettingName") + " is not enabled after saving.");
			}
			Log.message("3. Hide " + testData.get("SettingName") + " is enabled settings are saved.", driver);

			//Step-3 : Login to the vault
			configPage.clickLogOut(); //Logs out from the Configuration page
			Log.message("4. Logs out from the Configuration page", driver);

			loginPage.loginToWebApplication(userName, password, documentVault);
			Utils.waitForPageLoad(driver);
			HomePage homePage=new HomePage(driver);
			Log.message("5. Logged in to the vault (" + documentVault + ").", driver);

			//Step-4 : Search for documents and select any document
			SearchPanel.searchOrNavigatetoView(driver, testData.get("ObjectType"), testData.get("ObjectName"));

			Log.message("6. Searched '"+testData.get("ObjectName")+"' with advanced search options", driver);
			if (!homePage.listView.isItemExists(testData.get("ObjectName"), "Name")) {//Checks if object exists in the list
				throw new Exception("Object (" + testData.get("ObjectName") + ") does not exists in the list.");
			}
			if (testData.get("SettingName").equalsIgnoreCase("Check Out")) {
				if (homePage.isObjectCheckedOut("Check Out", userName)) {
					throw new Exception("Selected Object CheckedOut Already, So 'CheckOut' option wonot be available.");
				}
			}
			if (!homePage.listView.clickItem(testData.get("ObjectName"))) {//Checks if object selected in the list
				throw new Exception("Object (" + testData.get("ObjectName") + ") is not selected in the list.");
			}
			Log.message("7. Object (" + testData.get("ObjectName") + ") found in search and is selected.", driver);

			//Verification : To verify Show Command settings is reflected in MFWA page
			TaskPanel taskPanel = new TaskPanel(driver); //Instantiating TaskPanel wrapper class
			if (taskPanel.isObjectActionsDisplayedOnTaskPane(testData.get("TaskPaneName")))
				Log.fail("Test Failed. '" + testData.get("SettingName") + "' still exists in task area after enabling HIDE option of " + testData.get("SettingName") + ".", driver);
			else
				Log.pass("Test Passed. Hide '" + testData.get("SettingName") + "' is enabled successfully.");
		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {
				Utility.navigateToPage(driver, configSite);
				configPage=new ConfigurationPage(driver);
				configPage.expandVaultFolder(documentVault);
				configPage.clickSettingsFolder(testData.get("SettingFolder"));


				if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {
					configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"), testData.get("OptionHeader"), "Show");
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				} }

			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally
		} //End finally

	} //End Test1_5_3B

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_5_3C",description="Test1_5_3C:'Mark Complete' : Show")
	public void Test1_5_3C(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();

		ConcurrentHashMap <String, String> testData=new ConcurrentHashMap <String, String>(dataValues);
		String prevStatus=null;
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;
		HomePage homePage=null;
		try {

			
			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");
			Log.message("1. Loggedin to Configuration Page", driver);
			configPage=new ConfigurationPage(driver);
			configPage.clickTreeViewItem(documentVault);

			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);	
			Thread.sleep(200);
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess())
					throw new Exception("Allow access to "+documentVault+" not saved correctly");
			}
			Log.message("2. Allowed access to 'Sample Vault'", driver);


			//if Force selection is enabled, uncheck the option
			if(configPage.configurationPanel.getForceFollowingSelection()) {
				configPage.configurationPanel.setForceFollowingSelection(false);
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}
			Log.message("3. 'Force M-Files user login' is enabled", driver);

			//Step-2: Click Vault from left panel of Configuration Page
			configPage.expandVaultFolder(documentVault);
			configPage.clickSettingsFolder(testData.get("SettingFolder"));
			Log.message("4. Clicked '"+documentVault+"' from left panel of Configuration Page", driver);


			//Step-2 : Select Show Properties and save the settings 
			prevStatus = configPage.configurationPanel.getVaultCommands(testData.get("SettingName")); //Gets the previous status of the command
			if (!prevStatus.equalsIgnoreCase("Show")) {
				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"), testData.get("OptionHeader"), "Show");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}

			if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {//Checks for the modified settings
				throw new Exception("Show " + testData.get("SettingName") + " is not enabled after saving.");
			}
			Log.message("5. Show " + testData.get("SettingName") + " is enabled settings are saved.", driver);

			//Step-3 : Login to the vault
			configPage.clickLogOut(); //Logs out from the Configuration page
			Log.message("6. Logged out from the Configuration page", driver);

			loginPage.loginToWebApplication(userName, password, documentVault);
			Utils.waitForPageLoad(driver);
			homePage=new HomePage(driver);
			Log.message("7. Logged in to the vault (" + documentVault + ").", driver);

			//Step-6: Create New Assignment Object
			//------------------------------------
			homePage.taskPanel.clickItem("Assignment");//Clicks the New Assignment object link from task pane

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.setInfo(testData.get("Properties"));//Sets the required value in the metadatacard

			metadataCard.saveAndClose();//Clicks the save button in the metadatacard

			Log.message("8. New Assignment object is created for Verification", driver);

			//Verification: Verify if mark complete is displayed in the task pane for the selected assignment
			//------------------------------------------------------------------------------------------------
			if(homePage.taskPanel.isItemExists("Mark Complete"))
				Log.pass("Test Passed. Show '" + testData.get("SettingName") + "' is enabled successfully.");
			else
				Log.fail("Test Failed. " + testData.get("SettingName") + " doesnot exists in taskpane after enabling Show " + testData.get("SettingName") + ".", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {
				loginPage=new LoginPage(driver);
				Utility.navigateToPage(driver, configSite);
				//				loginPage.navigateToApplication(configSite,userName,password,"");
				configPage=new ConfigurationPage(driver);
				configPage.expandVaultFolder(documentVault);
				configPage.clickSettingsFolder(testData.get("SettingFolder"));


				if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {
					configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"), testData.get("OptionHeader"), "Show");
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				} }
			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally
		} //End finally

	} //End Test1_5_3C

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_5_3D",description="Test1_5_3D: 'Replace with File (Upload)' : Show")
	public void Test1_5_3D(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();

		ConcurrentHashMap <String, String> testData = new ConcurrentHashMap <String, String>(dataValues);
		String prevStatus=null;
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;
		
		try {

			//Step-1: Login to Configuration Page
			Log.message("1. Login to Configuration Page", driver);
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");	

			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);	
			Thread.sleep(200);
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess())
					throw new Exception("Allow access to "+documentVault+" not saved correctly");
			}
			Log.message("2. Allowed access to 'Sample Vault'", driver);

			configPage=new ConfigurationPage(driver);
			configPage.clickTreeViewItem(documentVault);

			//if Force selection is enabled, uncheck the option
			if(configPage.configurationPanel.getForceFollowingSelection()) {
				configPage.configurationPanel.setForceFollowingSelection(false);
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}

			//Step-2: Click Vault from left panel of Configuration Page
			Log.message("3. Click '"+documentVault+"' from left panel of Configuration Page", driver);
			configPage=new ConfigurationPage(driver);
			configPage.expandVaultFolder(documentVault);
			configPage.clickSettingsFolder(testData.get("SettingFolder"));

			//Step-2 : Select Show Properties and save the settings 
			prevStatus = configPage.configurationPanel.getVaultCommands(testData.get("SettingName")); //Gets the previous status of the command
			if (!prevStatus.equalsIgnoreCase("Show")) {
				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"), testData.get("OptionHeader"), "Show");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}

			if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {//Checks for the modified settings
				throw new Exception("Show " + testData.get("SettingName") + " is not enabled after saving.");
			}
			Log.message("4. Show " + testData.get("SettingName") + " is enabled settings are saved.", driver);

			//Step-3 : Login to the vault
			configPage.clickLogOut(); //Logs out from the Configuration page
			loginPage.loginToWebApplication(userName, password, documentVault);
			Utils.waitForPageLoad(driver);
			HomePage homePage=new HomePage(driver);
			Log.message("5. Logged in to the vault (" + documentVault + ").", driver);

			//Step-4 : Search for documents and select any document
			SearchPanel searchPanel=new SearchPanel(driver);
			searchPanel.showAdvancedSearchOptions(driver);

			searchPanel.selectSearchOptionsUsingObject(driver, MFilesObjectList.DOCUMENT, testData.get("ObjectType")).contains("Documents");

			searchPanel.setSearchWord(testData.get("ObjectName"));
			searchPanel.clickSearchBtn(driver); //Perform search operation

			if (!homePage.listView.isItemExists(testData.get("ObjectName"), "Name")) {//Checks if object exists in the list
				throw new Exception("Object (" + testData.get("ObjectName") + ") does not exists in the list.");
			}

			//Read List view column Headers
			if (!homePage.readListViewHeaderNames(driver,"Checked Out To")) {	        
				//Insert New Column to List view 
				homePage.clickAndInsertListViewColumns(driver,"Insert Column->Standard Columns->Checked Out To");
				//Verify if newly inserted column exists
				if (!homePage.readListViewHeaderNames(driver,"Checked Out To")) 
					throw new Exception("'Checked Out To' Column Not added.");
			}//End of if..loop

			TaskPanel taskPanel = new TaskPanel(driver); //Instantiating TaskPanel wrapper class
			homePage.listView.clickItem(testData.get("ObjectName"));
			Thread.sleep(500);
			if (!homePage.isObjectCheckedOut("Checked Out To", userName)) {
				taskPanel.selectObjectActionsFromTaskPane("CheckOut");
				Thread.sleep(100);
			}

			Log.message(" 6. Object (" + testData.get("ObjectName") + ") is selected in the list.", driver);

			//Verification : To verify Show Command settings is reflected in MFWA page
			//if (taskPanel.isItemExists(testData.get("SettingName")))
			if (homePage.menuBar.IsItemEnabledInOperationsMenu(testData.get("SettingName")))
				Log.pass("Test Passed. Show '" + testData.get("SettingName") + "' is enabled successfully.");
			else
				Log.fail("Test Failed. " + testData.get("SettingName") + " does not exists in operation menu after enabling show " + testData.get("SettingName") + ".", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {
				loginPage=new LoginPage(driver);
				Utility.navigateToPage(driver, configSite);
				configPage=new ConfigurationPage(driver);
				configPage.expandVaultFolder(documentVault);
				configPage.clickSettingsFolder(testData.get("SettingFolder"));


				if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {
					configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"), testData.get("OptionHeader"), "Show");
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				} }
			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally
		} //End finally

	} //End Test1_5_3D
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_5_3E",description="Test1_5_3E:'Move into State commands' : Show")
	public void Test1_5_3E(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();

		ConcurrentHashMap <String, String> testData = new ConcurrentHashMap <String, String>(dataValues);
		String prevStatus=null;
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;
		
		try {


			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");	
			Log.message("1. Logged in to Configuration Page", driver);

			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);	
			Thread.sleep(200);
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess())
					throw new Exception("Allow access to "+documentVault+" not saved correctly");
			}
			Log.message("2. Allowed access to 'Sample Vault'", driver);

			//Step-2: Click Vault from left panel of Configuration Page
			configPage=new ConfigurationPage(driver);
			configPage.expandVaultFolder(documentVault);
			configPage.clickSettingsFolder(testData.get("SettingFolder"));
			Log.message("3. Clicked '"+documentVault+"' from left panel of Configuration Page", driver);


			//Step-2 : Select Show Properties and save the settings 
			prevStatus = configPage.configurationPanel.getVaultCommands(testData.get("SettingName")); //Gets the previous status of the command
			if (!prevStatus.equalsIgnoreCase("Show")) {
				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"), testData.get("OptionHeader"), "Show");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}

			if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {//Checks for the modified settings
				throw new Exception("Show " + testData.get("SettingName") + " is not enabled after saving.");
			}
			Log.message("4. Show " + testData.get("SettingName") + " is enabled settings are saved.", driver);

			//Step-3 : Login to the vault
			configPage.clickLogOut(); //Logs out from the Configuration page
			Log.message("5. Logs out from the Configuration page", driver);
			loginPage.loginToWebApplication(userName, password, documentVault);
			Utils.waitForPageLoad(driver);
			HomePage homePage=new HomePage(driver);
			Log.message("6. Logged in to the vault (" + documentVault + ").", driver);

			//Step-4 : Search for documents and select any document
			SearchPanel searchPanel=new SearchPanel(driver);
			searchPanel.showAdvancedSearchOptions(driver);

			searchPanel.selectSearchOptionsUsingObject(driver, MFilesObjectList.DOCUMENT, testData.get("ObjectType")).contains("Documents");

			searchPanel.setSearchWord(testData.get("ObjectName"));
			searchPanel.clickSearchBtn(driver); //Perform search operation


			if (!homePage.listView.isItemExists(testData.get("ObjectName"), "Name")) {//Checks if object exists in the list
				throw new Exception("Object (" + testData.get("ObjectName") + ") does not exists in the list.");
			}

			if (!homePage.listView.clickItem(testData.get("ObjectName"))) {	//Checks if object selected in the list
				throw new Exception("Object (" + testData.get("ObjectName") + ") is not selected in the list.");
			}
			Log.message("7. Object (" + testData.get("ObjectName") + ") is selected in the list.", driver);

			//Verification : To verify Show Command settings is reflected in MFWA page
			TaskPanel taskPanel = new TaskPanel(driver); //Instantiating TaskPanel wrapper class

			homePage.menuBar.ClickOperationsMenu("Workflow");
			MFilesDialog mfilesDialog=new MFilesDialog(driver);

			if(!MFilesDialog.exists(driver))
				throw new Exception("Workflow dialog not displayed");

			mfilesDialog.setWorkflow(testData.get("Workflow"),currentDriver);
			mfilesDialog.clickOkButton();
			Utils.fluentWait(driver);

			//Verifies New Menu is not displayed after enabling hide New Menu in task pane 
			if (taskPanel.isMoveToStateOptionsAvailable(testData.get("TaskPaneName"))) 
				Log.pass("Test Passed. Show '" + testData.get("SettingName") + "' is enabled successfully.");
			else 
				Log.fail("Test Failed. " + testData.get("SettingName") + " does not exists in task area after enabling show " + testData.get("SettingName") + ".", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {
				loginPage=new LoginPage(driver);
				Utility.navigateToPage(driver, configSite);
				configPage=new ConfigurationPage(driver);
				configPage.expandVaultFolder(documentVault);
				configPage.clickSettingsFolder(testData.get("SettingFolder"));

				if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {
					configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"), testData.get("OptionHeader"), "Show");
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				} }
			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally
		} //End finally

	} //End Test1_5_3E
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_5_3F",description="Test1_5_3F: Move into State commands: Hide")
	public void Test1_5_3F(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();

		ConcurrentHashMap <String, String> testData = new ConcurrentHashMap <String, String>(dataValues);
		
		String prevStatus=null;
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;
		try {

			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");
			Log.message("1. Logged in to Configuration Page", driver);

			//Step-2: Click Vault from left panel of Configuration Page
			configPage=new ConfigurationPage(driver);
			configPage.clickTreeViewItem(documentVault);
			//Allow access to Vault
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess()) 
					throw new Exception("Error while allowing access to Vault");
			}

			configPage.expandVaultFolder(documentVault);
			configPage.clickSettingsFolder(testData.get("SettingFolder"));
			Log.message("2. Clicked '"+documentVault+"' from left panel of Configuration Page", driver);


			//Step-2 : Select Show Properties and save the settings 
			prevStatus = configPage.configurationPanel.getVaultCommands(testData.get("SettingName")); //Gets the previous status of the command
			if (!prevStatus.equalsIgnoreCase("Hide")) {
				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"), testData.get("OptionHeader"), "Hide");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}

			if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("HIDE")) {//Checks for the modified settings
				throw new Exception("Hide " + testData.get("SettingName") + " is not enabled after saving.");
			}
			Log.message("3. Hide " + testData.get("SettingName") + " is enabled settings are saved.", driver);

			//Step-3 : Login to the vault
			configPage.clickLogOut(); //Logs out from the Configuration page
			Log.message("4. Logs out from the Configuration page", driver);
			loginPage.loginToWebApplication(userName, password, documentVault);
			Utils.waitForPageLoad(driver);
			HomePage homePage=new HomePage(driver);
			Log.message("5. Logged in to the vault (" + documentVault + ").", driver);

			//Step-4 : Search for documents and select any document

			SearchPanel searchPanel=new SearchPanel(driver);
			searchPanel.showAdvancedSearchOptions(driver);

			if (testData.get("ObjectType").trim().equalsIgnoreCase("Documents")) {
				searchPanel.selectSearchOptionsUsingObject(driver, MFilesObjectList.DOCUMENT, testData.get("ObjectType")).contains("Documents");
			}
			else if (testData.get("ObjectType").trim().equalsIgnoreCase("Customers")) {
				searchPanel.selectSearchOptionsUsingObject(driver, MFilesObjectList.CUSTOMER, testData.get("ObjectType")).contains("Customers");
			}
			else if (testData.get("ObjectType").trim().equalsIgnoreCase("Contact persons")) {
				searchPanel.selectSearchOptionsUsingObject(driver, MFilesObjectList.CONTACT_PERSON, testData.get("ObjectType")).contains("Contact persons");
			}
			else if (testData.get("ObjectType").trim().equalsIgnoreCase("Projects")) {
				searchPanel.selectSearchOptionsUsingObject(driver, MFilesObjectList.PROJECT, testData.get("ObjectType")).contains("Projects");
			}
			else if (testData.get("ObjectType").trim().equalsIgnoreCase("Assignments")) {
				searchPanel.selectSearchOptionsUsingObject(driver, MFilesObjectList.ASSIGNMENT, testData.get("ObjectType")).contains("Assignments");
			}

			homePage.enterSearchText(testData.get("ObjectName"));
			homePage.clickSearchBtn(driver); //Perform search operation


			if (!homePage.listView.isItemExists(testData.get("ObjectName"), "Name")) {//Checks if object exists in the list
				throw new Exception("Object (" + testData.get("ObjectName") + ") does not exists in the list.");
			}
			if (testData.get("SettingName").equalsIgnoreCase("Check Out")) {
				if (homePage.isObjectCheckedOut("Check Out", userName)) {
					throw new Exception("Selected Object CheckedOut Already, So 'CheckOut' option wonot be available.");
				}
			}
			if (!homePage.listView.clickItem(testData.get("ObjectName"))) {//Checks if object selected in the list
				throw new Exception("Object (" + testData.get("ObjectName") + ") is not selected in the list.");
			}
			Log.message("6. Object (" + testData.get("ObjectName") + ") is selected in the list.", driver);

			//Verification : To verify Show Command settings is reflected in MFWA page
			TaskPanel taskPanel = new TaskPanel(driver); //Instantiating TaskPanel wrapper class

			homePage.menuBar.ClickOperationsMenu("Workflow");
			MFilesDialog mfilesDialog=new MFilesDialog(driver);

			if(!MFilesDialog.exists(driver))
				throw new Exception("Workflow dialog not displayed");

			mfilesDialog.setWorkflow(testData.get("Workflow"),currentDriver);
			mfilesDialog.clickOkButton();
			Thread.sleep(1000);

			//Verifies New Menu is not displayed after enabling hide New Menu in task pane 
			if (taskPanel.isMoveToStateOptionsAvailable(testData.get("TaskPaneName")))
				Log.fail("Test Failed. " + testData.get("SettingName") + " still exists in task area after enabling HIDE option of " + testData.get("SettingName") + ".", driver);
			else
				Log.pass("Test Passed. Hide '" + testData.get("SettingName") + "' is enabled successfully.");	
		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {
				loginPage=new LoginPage(driver);
				Utility.navigateToPage(driver, configSite);
				configPage=new ConfigurationPage(driver);
				configPage.expandVaultFolder(documentVault);
				configPage.clickSettingsFolder(testData.get("SettingFolder"));


				if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {
					configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"), testData.get("OptionHeader"), "Show");
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				} }

			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally
		} //End finally

	} //End Test1_5_3F

	//	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_5_3G",description="Test1_5_3G:'Mark Complete' : Hide")
	//	public void Test1_5_3G(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {
	//
	//		//Variable Declaration
	//		driver = WebDriverUtils.getDriver();
	//		
	//		ConcurrentHashMap <String, String> testData = new ConcurrentHashMap <String, String>(dataValues);
	//		String prevStatus=null;
	//		LoginPage loginPage=null;
	//		ConfigurationPage configPage=null;
	//		HomePage homePage=null;
	//		try {
	//		
	//			
	//			//Step-1: Login to Configuration Page
	//			loginPage=new LoginPage(driver);
	//			loginPage.navigateToApplication(configSite,userName,password,"");	
	//			Log.message("1. Logged in to Configuration Page", driver);
	//			
	//			configPage=new ConfigurationPage(driver);
	//			configPage.clickVaultFolder(documentVault);	
	//			Thread.sleep(200);
	//			if (!configPage.configurationPanel.getVaultAccess()) {
	//				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
	//				configPage.clickSaveButton();
	//				configPage.clickOKBtnOnSaveDialog();
	//				if (!configPage.configurationPanel.getVaultAccess())
	//					throw new Exception("Allow access to "+documentVault+" not saved correctly");
	//			}
	//			Log.message("2. Allowed access to 'Sample Vault'");
	//			
	//			
	//			//Step-2: Click Vault from left panel of Configuration Page
	//			configPage=new ConfigurationPage(driver);
	//			configPage.expandVaultFolder(documentVault);
	//			configPage.clickSettingsFolder(testData.get("SettingFolder"));
	//			Log.message("2. Clicked '"+documentVault+"' from left panel of Configuration Page");
	//			
	//			
	//			//Step-2 : Select Show Properties and save the settings 
	//			prevStatus = configPage.configurationPanel.getVaultCommands(testData.get("SettingName")); //Gets the previous status of the command
	//			if (!prevStatus.equalsIgnoreCase("Hide")) {
	//				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"), testData.get("OptionHeader"), "Hide");
	//				configPage.clickSaveButton();
	//				configPage.clickOKBtnOnSaveDialog();
	//			}
	//			
	//			if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("HIDE")) {//Checks for the modified settings
	//				throw new Exception("Hide " + testData.get("SettingName") + " is not enabled after saving.");
	//			}
	//			Log.message("3. Hide " + testData.get("SettingName") + " is enabled settings are saved.");
	//			
	//			//Step-3 : Login to the vault
	//			configPage.clickLogOut(); //Logs out from the Configuration page
	//			Log.message("4. Logs out from the Configuration page");
	//			
	//			loginPage.loginToWebApplication(userName, password, documentVault);
	//			Utils.waitForPageLoad(driver);
	//			homePage=new HomePage(driver);
	//			Log.message("5. Logged in to the vault (" + documentVault + ").");
	//			
	//				//Step-4 : Search for documents and select any document
	//				SearchPanel searchPanel=new SearchPanel(driver);
	//				searchPanel.showAdvancedSearchOptions(driver);
	//				searchPanel.selectSearchOptionsUsingObject(driver, MFilesObjectList.ASSIGNMENT, testData.get("ObjectType")).contains("Assignments");
	//				homePage.enterSearchText(testData.get("ObjectName"));
	//				homePage.clickSearchBtn(driver); //Perform search operation
	//				Log.message("6. Search done for '"+testData.get("ObjectName")+"' using advanced search options");
	//				
	//			if (!homePage.listView.isItemExists(testData.get("ObjectName"), "Name")) {//Checks if object exists in the list
	//				throw new Exception("Object (" + testData.get("ObjectName") + ") does not exists in the list.");
	//			}
	//			if (!homePage.listView.clickItem(testData.get("ObjectName"))) {	//Checks if object selected in the list
	//				throw new Exception("Object (" + testData.get("ObjectName") + ") is not selected in the list.");
	//			}
	//
	//			TaskPanel taskPane=new TaskPanel(driver);
	//			if(!taskPane.isItemExists("Mark Complete"))
	//				Log.pass("Test Passed. Hide '" + testData.get("SettingName") + "' is enabled successfully.");
	//			else
	//				Log.fail("Test Failed. " + testData.get("SettingName") + " exists in metadatacard after enabling hide " + testData.get("SettingName") + ".", driver);
	//				
	//		} //End try
	//		
	//		catch (Exception e) {
	//			Log.exception(e, driver);
	//		} //End catch
	//		
	//		finally {
	//		try {
	//				loginPage=new LoginPage(driver);
	//				driver.get(configSite);
	//				configPage=new ConfigurationPage(driver); 
	//				configPage.expandVaultFolder(documentVault);
	//				configPage.clickSettingsFolder(testData.get("SettingFolder"));
	//						
	//				
	//				if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {
	//					configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"), testData.get("OptionHeader"), "Show");
	//					configPage.clickSaveButton();
	//					configPage.clickOKBtnOnSaveDialog();
	//				} }
	//		catch (Exception e) {
	//			Log.exception(e, driver);
	//		} //End catch
	//		
	//		finally {
	//						
	//			driver.quit();	
	//		} //End finally
	//	} //End finally
	//	} //End Test1_5_3G

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_5_3H",description="Test1_5_3H: 'Replace with File (Upload)' : Hide")
	public void Test1_5_3H(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();

		ConcurrentHashMap <String, String> testData = new ConcurrentHashMap <String, String>(dataValues);
		String prevStatus=null;
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;
		

		try {

			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");	
			Log.message("1. Logged in to Configuration Page", driver);
			configPage=new ConfigurationPage(driver);
			configPage.clickTreeViewItem(documentVault);

			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);	
			Thread.sleep(200);
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess())
					throw new Exception("Allow access to "+documentVault+" not saved correctly");
			}
			Log.message("2. Allowed access to 'Sample Vault'", driver);


			//if Force selection is enabled, uncheck the option
			if(configPage.configurationPanel.getForceFollowingSelection()) {
				configPage.configurationPanel.setForceFollowingSelection(false);
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}

			//Step-2: Click Vault from left panel of Configuration Page
			configPage.expandVaultFolder(documentVault);
			configPage.clickSettingsFolder(testData.get("SettingFolder"));
			Log.message("3. Clicked '"+documentVault+"' from left panel of Configuration Page", driver);


			//Step-2 : Select Show Properties and save the settings 
			prevStatus = configPage.configurationPanel.getVaultCommands(testData.get("SettingName")); //Gets the previous status of the command
			if (!prevStatus.equalsIgnoreCase("Hide")) {
				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"), testData.get("OptionHeader"), "Hide");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}

			if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("HIDE")) {//Checks for the modified settings
				throw new Exception("Hide " + testData.get("SettingName") + " is not enabled after saving.");
			}
			Log.message("4. Hide " + testData.get("SettingName") + " is enabled settings are saved.", driver);

			//Step-3 : Login to the vault
			configPage.clickLogOut(); //Logs out from the Configuration page
			Log.message("5. Logs out from the Configuration page", driver);
			loginPage.loginToWebApplication(userName, password, documentVault);
			Utils.waitForPageLoad(driver);
			HomePage homePage=new HomePage(driver);
			Log.message("6. Logged in to the vault (" + documentVault + ").", driver);

			//Step-4 : Search for documents and select any document
			SearchPanel searchPanel=new SearchPanel(driver);
			searchPanel.showAdvancedSearchOptions(driver);

			searchPanel.selectSearchOptionsUsingObject(driver, MFilesObjectList.DOCUMENT, testData.get("ObjectType")).contains("Documents");
			searchPanel.setSearchWord(testData.get("ObjectName"));
			searchPanel.clickSearchBtn(driver); //Perform search operation


			if (!homePage.listView.isItemExists(testData.get("ObjectName"), "Name")) {//Checks if object exists in the list
				throw new Exception("Object (" + testData.get("ObjectName") + ") does not exists in the list.");
			}

			//Read List view column Headers
			if (!homePage.readListViewHeaderNames(driver,"Checked Out To")) {	        
				//Insert New Column to List view 
				homePage.clickAndInsertListViewColumns(driver,"Insert Column->Standard Columns->Checked Out To");
				//Verify if newly inserted column exists
				if (!homePage.readListViewHeaderNames(driver,"Checked Out To")) 
					throw new Exception("'Checked Out To' Column Not added.");
			}//End of if..loop

			TaskPanel taskPanel = new TaskPanel(driver); //Instantiating TaskPanel wrapper class
			homePage.listView.clickItem(testData.get("ObjectName"));//Select the object from list
			if (!homePage.isObjectCheckedOut("Checked Out To", userName)) {
				taskPanel.selectObjectActionsFromTaskPane("CheckOut");
			}

			Log.message("7. Object (" + testData.get("ObjectName") + ") is selected in the list.", driver);

			//Verification : To verify Show Command settings is reflected in MFWA page
			if (homePage.menuBar.IsItemEnabledInOperationsMenu(testData.get("SettingName")))
				Log.fail("Test Failed. " + testData.get("SettingName") + " exists in task area after enabling Hide option of '" + testData.get("SettingName") + "'.", driver);
			else 
				Log.pass("Test Passed. Hide '" + testData.get("SettingName") + "' is enabled successfully.");

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {
				loginPage=new LoginPage(driver);
				driver.get(configSite);
				configPage=new ConfigurationPage(driver);
				configPage.expandVaultFolder(documentVault);
				configPage.clickSettingsFolder(testData.get("SettingFolder"));


				if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {
					configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"), testData.get("OptionHeader"), "Show");
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				} }

			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally
		} //End finally

	} //End Test1_5_3H
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_5_4A",description="Test1_5_4A: Check out : Show")
	public void Test1_5_4A(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData=new ConcurrentHashMap <String, String>(dataValues);
		String prevStatus=null;
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;

		try {

			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");	
			Log.message("1. Logged in to Configuration Page", driver);

			//Step-2: Click Vault from left panel of Configuration Page
			configPage=new ConfigurationPage(driver);
			configPage.clickTreeViewItem(documentVault);
			//Allow access to Vault
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess()) 
					throw new Exception("Error while allowing access to Vault");
			}
			//Update the configuration settings
			configPage.expandVaultFolder(documentVault);
			configPage.clickSettingsFolder(testData.get("SettingFolder"));
			Log.message("2. Clicked '"+documentVault+"' from left panel of Configuration Page", driver);


			//Step-2 : Select Show Properties and save the settings 
			prevStatus = configPage.configurationPanel.getVaultCommands(testData.get("SettingName")); //Gets the previous status of the command
			if (!prevStatus.equalsIgnoreCase("Show")) {
				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"), testData.get("OptionHeader"), "Show");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}

			if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {//Checks for the modified settings
				throw new Exception("Show " +  testData.get("SettingName") + " is not enabled after saving.");
			}

			Log.message("3. Show " +  testData.get("SettingName") + " is enabled settings are saved.", driver);

			configPage.clickLogOut(); //Logs out from the Configuration page
			Log.message("4. Logs out from the Configuration page", driver);
			//Step-3 : Login to the vault
			loginPage.loginToWebApplication(userName, password, documentVault);
			Utils.waitForPageLoad(driver);
			HomePage homePage=new HomePage(driver);
			Log.message("5. Logged in to the vault (" +documentVault + ").", driver);

			//Step-4 : Search for documents and select any document
			homePage.enterSearchText(testData.get("ObjectName"));
			homePage.clickSearchBtn(driver); //Perform search operation


			if (!homePage.listView.isItemExists(testData.get("ObjectName"), "Name")) {//Checks if object exists in the list
				throw new Exception("Object (" +testData.get("ObjectName") + ") does not exists in the list.");
			}
			if (!homePage.listView.clickItem(testData.get("ObjectName"))) {//Checks if object selected in the list
				throw new Exception("Object (" + testData.get("ObjectName") + ") is not selected in the list.");
			}
			Log.message("6. Object (" +  testData.get("ObjectName") + ") is selected in the list.", driver);

			if (!homePage.readListViewHeaderNames(driver,"Checked Out To")){
				homePage.clickAndInsertListViewColumns(driver,"Insert Column->Standard Columns->Checked Out To");
			}

			if (homePage.isObjectCheckedOut("Checked Out To", userName)) {
				homePage.openContextMenuDialog(driver);
				homePage.checkInObjectFromContextMenu();
			}

			//Verification : To verify Show Command settings is reflected in MFWA page
			TaskPanel taskPanel = new TaskPanel(driver); //Instantiating TaskPanel wrapper class
			//Verifies New Menu is not displayed after enabling hide New Menu in task pane 
			if (taskPanel.isItemExists(testData.get("SettingName")))
				Log.pass("Test Passed. Show '" + testData.get("SettingName") + "' is enabled successfully.");
			else if(taskPanel.isObjectActionsDisplayedOnTaskPane("CheckIn")) 
				throw new Exception("Selected object :'"+testData.get("ObjectName") +"' is not checked in Properly, so could not display '"+testData.get("SettingName")+".'");
			else 
				Log.fail("Test Failed. " + testData.get("SettingName") + " does not exists in task area after enabling show " + testData.get("SettingName") + ".", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {
				loginPage=new LoginPage(driver);
				driver.get(configSite);
				configPage=new ConfigurationPage(driver);
				configPage.expandVaultFolder(documentVault);
				configPage.clickSettingsFolder(testData.get("SettingFolder"));


				if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {
					configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"), testData.get("OptionHeader"), "Show");
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				} }

			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally
		} //End finally

	} //End ConfigurationUI1_5_4A

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_5_4B",description="Test1_5_4B: Check out : Hide")
	public void Test1_5_4B(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String>	testData = new ConcurrentHashMap <String, String>(dataValues);
		String prevStatus=null;
		HomePage homePage=null;
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;

		try {

			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");
			Log.message("1. Logged in to Configuration Page", driver);

			//Step-2: Click Vault from left panel of Configuration Page
			configPage=new ConfigurationPage(driver);
			configPage.clickTreeViewItem(documentVault);
			//Allow access to Vault
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess()) 
					throw new Exception("Error while allowing access to Vault");
			}
			//Update the configuration settings
			configPage.expandVaultFolder(documentVault);
			configPage.clickSettingsFolder(testData.get("SettingFolder"));
			Log.message("2. Clicked '"+documentVault+"' from left panel of Configuration Page", driver);


			//Step-2 : Select Show Properties and save the settings 
			prevStatus = configPage.configurationPanel.getVaultCommands(testData.get("SettingName")); //Gets the previous status of the command
			if (!prevStatus.equalsIgnoreCase("Hide")) {
				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"), testData.get("OptionHeader"), "Hide");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}

			if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("HIDE")) {//Checks for the modified settings
				throw new Exception("Hide " +  testData.get("SettingName") + " is not enabled after saving.");
			}

			Log.message("3. Hide " +  testData.get("SettingName") + " is enabled settings are saved.", driver);

			configPage.clickLogOut(); //Logs out from the Configuration page
			Log.message("4. Logs out from the Configuration page", driver);
			//Step-3 : Login to the vault
			loginPage.loginToWebApplication(userName, password, documentVault);
			Utils.waitForPageLoad(driver);
			homePage=new HomePage(driver);
			Log.message("5. Logged in to the vault (" +documentVault + ").", driver);

			//Step-4 : Search for documents and select any document
			homePage.enterSearchText(testData.get("ObjectName"));
			homePage.clickSearchBtn(driver); //Perform search operation


			if (!homePage.listView.isItemExists(testData.get("ObjectName"), "Name")) {//Checks if object exists in the list
				throw new Exception("Object (" +testData.get("ObjectName") + ") does not exists in the list.");
			}
			if (!homePage.listView.clickItem(testData.get("ObjectName"))) {//Checks if object selected in the list
				throw new Exception("Object (" + testData.get("ObjectName") + ") is not selected in the list.");
			}
			Log.message("6. Object (" +  testData.get("ObjectName") + ") is selected in the list.", driver);

			if (!homePage.readListViewHeaderNames(driver,"Checked Out To")){
				homePage.clickAndInsertListViewColumns(driver,"Insert Column->Standard Columns->Checked Out To");
			}
			if (homePage.isObjectCheckedOut("Checked out To", userName)) {
				homePage.openContextMenuDialog(driver);
				homePage.checkInObjectFromContextMenu();
			}

			//Verification : To verify Show Command settings is reflected in MFWA page
			TaskPanel taskPanel = new TaskPanel(driver); //Instantiating TaskPanel wrapper class

			//Verifies New Menu is not displayed after enabling hide New Menu in task pane 
			if (taskPanel.isObjectActionsDisplayedOnTaskPane(testData.get("TaskPaneName")))
				Log.fail("Test Failed. " + testData.get("SettingName") + " exists in task area after enabling Hide options of " + testData.get("SettingName") + ".", driver);
			else
				Log.pass("Test Passed. Hide '" + testData.get("SettingName") + "' is enabled successfully.");

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {
				loginPage=new LoginPage(driver);
				driver.get(configSite);
				configPage=new ConfigurationPage(driver);
				configPage.expandVaultFolder(documentVault);
				configPage.clickSettingsFolder(testData.get("SettingFolder"));

				if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {
					configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"), testData.get("OptionHeader"), "Show");
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				} }

			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally
		} //End finally
	} //End ConfigurationUI1_5_4B

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_5_5A",description="Test1_5_5A: Check out/Check In/Add File/Assigned to me/Checked Out to Me/Home/Favorites : Show")
	public void Test1_5_5A(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData = new ConcurrentHashMap <String, String>(dataValues);
		String prevStatus=null;
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;
		try {

			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");	
			Log.message("1. Logged in to Configuration Page", driver);

			//Step-2: Click Vault from left panel of Configuration Page
			configPage=new ConfigurationPage(driver);
			configPage.clickTreeViewItem(documentVault);
			//Allow access to Vault
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess()) 
					throw new Exception("Error while allowing access to Vault");
			}
			//Update the configuration settings
			configPage.expandVaultFolder(documentVault);
			configPage.clickSettingsFolder(testData.get("SettingFolder"));
			Log.message("2. Clicked '"+documentVault+"' from left panel of Configuration Page", driver);

			//Step-2 : Select Show Properties and save the settings 
			prevStatus = configPage.configurationPanel.getVaultCommands(testData.get("SettingName")); //Gets the previous status of the command
			if (!prevStatus.equalsIgnoreCase("Show")) {
				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"), testData.get("OptionHeader"), "Show");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}

			if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {//Checks for the modified settings
				throw new Exception("Show " +  testData.get("SettingName") + " is not enabled after saving.");
			}

			Log.message("---Show " +  testData.get("SettingName") + " is enabled settings are saved.", driver);

			configPage.clickLogOut(); //Logs out from the Configuration page
			//Step-3 : Login to the vault
			loginPage.loginToWebApplication("admin", "admin", documentVault);
			Utils.fluentWait(driver);
			HomePage homePage=new HomePage(driver);
			Log.message("---Logged in to the vault (" +documentVault + ").", driver);


			//Step-4 : Search for documents and select any document
			homePage.enterSearchText(testData.get("ObjectName"));
			homePage.clickSearchBtn(driver); //Perform search operation

			if (!homePage.listView.isItemExists(testData.get("ObjectName"), "Name")) {//Checks if object exists in the list
				throw new Exception("Object (" +testData.get("ObjectName") + ") does not exists in the list.");
			}

			if (!homePage.listView.clickItem(testData.get("ObjectName"))) {//Checks if object selected in the list
				throw new Exception("Object (" + testData.get("ObjectName") + ") is not selected in the list.");
			}
			Log.message("---Object (" +  testData.get("ObjectName") + ") is selected in the list.", driver);

			if(!testData.get("TaskPaneName").equalsIgnoreCase("Check out")){

				if (!homePage.readListViewHeaderNames(driver,"Checked Out To")){
					homePage.clickAndInsertListViewColumns(driver,"Insert Column->Standard Columns->Checked Out To");
				}
				if (!homePage.isObjectCheckedOut("Checked out To", "admin")) {
					TaskPanel taskPanel=new TaskPanel(driver);
					taskPanel.selectObjectActionsFromTaskPane("CheckOut");
					Thread.sleep(100);
				}
			}

			//Verification : To verify Show Command settings is reflected in MFWA page
			TaskPanel taskPanel = new TaskPanel(driver); //Instantiating TaskPanel wrapper class

			//Verifies New Menu is not displayed after enabling hide New Menu in task pane 
			if (!taskPanel.isItemExists(testData.get("TaskPaneName")))
				Log.fail("Test Failed. '" + testData.get("SettingName") + "' doesnot exists in task area after enabling Show options of " + testData.get("SettingName") + ".", driver);
			else
				Log.pass("Test Passed. Show '" + testData.get("SettingName") + "' is enabled successfully.");

		} //End try
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {
				loginPage=new LoginPage(driver);
				Utility.navigateToPage(driver, configSite);
				configPage=new ConfigurationPage(driver);
				configPage.expandVaultFolder(documentVault);
				configPage.clickSettingsFolder(testData.get("SettingFolder"));


				if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {
					configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"), testData.get("OptionHeader"), "Show");
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				}}

			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally
		} //End finally
	} //End Test1_5_5A

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_5_5B",description="Test1_5_5B: Check out/Check In/Add File/Assigned to me/Checked Out to Me : Hide")
	public void Test1_5_5B(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData = new ConcurrentHashMap <String, String>(dataValues);
		String prevStatus=null;
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;
		try {

			//Step-1: Login to Configuration Page
			Log.message("1. Login to Configuration Page", driver);
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");	
			//Step-2: Click Vault from left panel of Configuration Page
			Log.message("2. Click '"+documentVault+"' from left panel of Configuration Page", driver);
			configPage=new ConfigurationPage(driver);
			configPage.clickTreeViewItem(documentVault);
			//Allow access to Vault
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess()) 
					throw new Exception("Error while allowing access to Vault");
			}

			configPage.expandVaultFolder(documentVault);
			configPage.clickSettingsFolder(testData.get("SettingFolder"));

			//Step-2 : Select Show Properties and save the settings 
			prevStatus = configPage.configurationPanel.getVaultCommands(testData.get("SettingName")); //Gets the previous status of the command
			if (!prevStatus.equalsIgnoreCase("Hide")) {
				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"), testData.get("OptionHeader"), "Hide");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}

			if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("HIDE")) {//Checks for the modified settings
				throw new Exception("Hide " +  testData.get("SettingName") + " is not enabled after saving.");
			}
			Log.message("---Hide " +  testData.get("SettingName") + " is enabled settings are saved.", driver);

			configPage.clickLogOut(); //Logs out from the Configuration page
			//Step-3 : Login to the vault
			loginPage.loginToWebApplication(userName, password, documentVault);
			Utils.waitForPageLoad(driver);
			HomePage homePage=new HomePage(driver);
			Log.message("---Logged in to the vault (" +documentVault + ").", driver);


			//Step-4 : Search for documents and select any document
			homePage.enterSearchText(testData.get("ObjectName"));
			homePage.clickSearchBtn(driver); //Perform search operation


			if (!homePage.listView.isItemExists(testData.get("ObjectName"), "Name")) {//Checks if object exists in the list
				throw new Exception("Object (" +testData.get("ObjectName") + ") does not exists in the list.");
			}

			if (!homePage.listView.clickItem(testData.get("ObjectName"))) {//Checks if object selected in the list
				throw new Exception("Object (" + testData.get("ObjectName") + ") is not selected in the list.");
			}
			Log.message("---Object (" +  testData.get("ObjectName") + ") is selected in the list.", driver);

			if (!homePage.readListViewHeaderNames(driver,"Checked Out To")){
				//				 homePage.listView.insertColumn("Checked Out To");
				homePage.clickAndInsertListViewColumns(driver,"Insert Column->Standard Columns->Checked Out To");
			}
			if (!homePage.isObjectCheckedOut("Checked out To", userName)) {
				homePage.taskPanel.selectObjectActionsFromTaskPane("CheckOut");
			}

			//Verification : To verify Show Command settings is reflected in MFWA page
			TaskPanel taskPanel = new TaskPanel(driver); //Instantiating TaskPanel wrapper class

			//Verifies New Menu is not displayed after enabling hide New Menu in task pane 
			if (taskPanel.isItemExists(testData.get("TaskPaneName")))
				Log.fail("Test Failed. " + testData.get("SettingName") + " exists in task area after enabling Hide options of " + testData.get("SettingName") + ".", driver);
			else
				Log.pass("Test Passed. Hide of '" + testData.get("SettingName") + "' is enabled successfully.");

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {
				loginPage=new LoginPage(driver);
				Utility.navigateToPage(driver, configSite);
				configPage=new ConfigurationPage(driver);
				configPage.expandVaultFolder(documentVault);
				configPage.clickSettingsFolder(testData.get("SettingFolder"));

				if (!configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase("SHOW")) {
					configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"), testData.get("OptionHeader"), "Show");
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				}
			}
			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally

		} //End finally


	} //End Test1_5_5B

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_5_24_1",description="Don't show Go To group title if none of the shortcuts are visible")
	public void Test1_5_24_1(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData = new ConcurrentHashMap <String, String>(dataValues);
		String[] gotoShortcuts=null;
		Boolean isShow=false;
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;

		try {

			//Step-1: Login to Configuration Page
			Log.message("1. Login to Configuration Page", driver);
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");	

			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);	
			Thread.sleep(200);
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess())
					throw new Exception("Allow access to "+documentVault+" not saved correctly");
			}
			Log.message("2. Allowed access to 'Sample Vault'", driver);

			//Step-2: Click Vault from left panel of Configuration Page
			Log.message("2. Click '"+documentVault+"' from left panel of Configuration Page");
			configPage=new ConfigurationPage(driver);
			configPage.expandVaultFolder(documentVault);
			configPage.clickSettingsFolder(testData.get("SettingFolder"));


			//Step-3 : Navigate to the Task area page in Web Access Configuration
			Log.message("Step-1 : Navigated to the Task area " + documentVault + " page in Web Access Configuration.", driver);

			//Step-4 : Select Hide to all GoTo Shortcuts 
			gotoShortcuts = testData.get("SettingName").split(",");

			for (int i=0; i<gotoShortcuts.length; i++)
			{
				if (!configPage.configurationPanel.getVaultCommands(gotoShortcuts[i]).equalsIgnoreCase("Hide")) {
					configPage.chooseConfigurationVaultSettings(driver, gotoShortcuts[i], testData.get("OptionHeader"),"Hide");
				}
			}

			configPage.clickSaveButton();
			configPage.clickOKBtnOnSaveDialog(); //Save the settings

			for (int i=0; i<gotoShortcuts.length; i++) {
				if (!configPage.configurationPanel.getVaultCommands(gotoShortcuts[i]).equalsIgnoreCase("HIDE")) {//Checks for the modified settings
					isShow = true;
					break;
				}

				if (isShow) {
					throw new Exception("Hide " + gotoShortcuts[i] + " is not enabled after saving.");
				}
			}
			Log.message("Step-2 : Hide is enabled to all Go To commands and settings are saved.", driver);

			//Step-3 : Login to the vault
			configPage.clickLogOut(); //Logs out from the Configuration page
			loginPage.loginToWebApplication(userName, password, documentVault);
			Utils.waitForPageLoad(driver);

			//Verification : To verify Show Command settings is reflected in MFWA page
			TaskPanel taskPanel = new TaskPanel(driver); //Instantiating TaskPanel wrapper class

			//Verifies New Menu is not displayed after enabling hide New Menu in task pane 
			if (!taskPanel.isItemExists("Go To"))
				Log.pass("Test Passed. Go To is not shown after hiding all Go To commands.");
			else
				Log.fail("Test Failed. Go To is shown after hiding all Go To commands", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {
				loginPage=new LoginPage(driver);
				Utility.navigateToPage(driver, configSite);
				configPage=new ConfigurationPage(driver);
				configPage.expandVaultFolder(documentVault);
				configPage.clickSettingsFolder(testData.get("SettingFolder"));

				for (int i=0; i<gotoShortcuts.length; i++) {
					if (!configPage.configurationPanel.getVaultCommands(gotoShortcuts[i]).equalsIgnoreCase("SHOW")) {
						configPage.chooseConfigurationVaultSettings(driver, gotoShortcuts[i], testData.get("OptionHeader"),"Show");
					}
				}
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog(); //Save the settings
			}
			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally
		} //End finally


	} //End Test1_5_24_1

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_5_27",description="Test1_5_27: Reset in Task Area")
	public void Test1_5_27(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData = new ConcurrentHashMap <String, String>(dataValues);
		String prevStatus=null;
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;
		try {

			//Step-1: Login to Configuration Page
			Log.message("1. Login to Configuration Page", driver);
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");	

			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);	
			Thread.sleep(200);
			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				if (!configPage.configurationPanel.getVaultAccess())
					throw new Exception("Allow access to "+documentVault+" not saved correctly");
			}
			Log.message("2. Allowed access to 'Sample Vault'", driver);


			//Step-2: Click Vault from left panel of Configuration Page
			Log.message("3. Click '"+documentVault+"' from left panel of Configuration Page", driver);
			configPage=new ConfigurationPage(driver);
			configPage.expandVaultFolder(documentVault);
			configPage.clickSettingsFolder(testData.get("SettingFolder"));


			Log.message("Step-1 : Navigated to the Task area " + documentVault + " page in Web Access Configuration.", driver);

			//Step-2 : Modify some setting in Task Area page and click reset button
			prevStatus = configPage.configurationPanel.getVaultCommands(testData.get("SettingName")); //Gets the previous status of the command

			if (prevStatus.equalsIgnoreCase("SHOW"))
				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"), testData.get("OptionHeader"),"Hide");				
			else
				configPage.chooseConfigurationVaultSettings(driver, testData.get("SettingName"), testData.get("OptionHeader"),"Show");	 //Sets Show to the command

			if (configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase(prevStatus)) {//Checks for the modified settings
				throw new Exception(testData.get("SettingName") + " is not modified to test reset functionality.");
			}

			configPage.clickResetButton();
			Thread.sleep(500);
			Log.message("'" + testData.get("SettingName") + "' is modified and Reset button is clicked.");

			//Verifies the the settings are same before modifiying and after clicking reset button
			if (configPage.configurationPanel.getVaultCommands(testData.get("SettingName")).equalsIgnoreCase(prevStatus))
				Log.pass("Test Passed. Reset button resets modified settings successfully.");
			else
				Log.fail("Test Failed. Reset button does not reset modified settings.", driver);


		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			driver.quit();
		} //End finally

	} //End Test1_5_27

	/**
	 * TestCase ID: Test1_6_1 Verify Maximum number of search results option from configuration page 
	 * <br>Description:  Open /configuration.aspx page instead of /login.aspx</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_6_1",description="Test1_6_1: Maximum number of search results")
	public void Test1_6_1(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData = new ConcurrentHashMap <String, String>(dataValues);
		LoginPage loginPage=null;

		ConfigurationPage configPage=null;
		HomePage homepage=null;
		try {

			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");	
			Log.message("1. Logged in to Configuration Page", driver);

			//Step-2: Click Vault from left panel of Configuration Page
			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(testData.get("VaultName"));
			Log.message("2. Clicked 'Sample vault' from left panel of Configuration Page", driver);


			//Step-3 : Set value for Maximum number of search results

			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
			}

			configPage.configurationPanel.setValueSearchmaximumResult(testData.get("Maximum search results"));
			configPage.clickSaveButton();

			// Check any warning dialog displayed
			if(configPage.isWarningDialogDisplayed())
			{
				configPage.clickResetButton();
				configPage.configurationPanel.setValueSearchmaximumResult(testData.get("Maximum search results"));
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();  
			}
			else
				configPage.clickOKBtnOnSaveDialog();

			Log.message("3. Value set for Maximum number of search results", driver);

			//Step-4: LogOut of configurationpage
			configPage.clickLogOut(); //Logs out from the Configuration page
			Log.message("4. Logged Out of configuration Page.", driver);

			//Step-5: Login to Web Access Vault
			loginPage.navigateToApplication(webSite,userName,password,testData.get("VaultName"));
			Utils.waitForPageLoad(driver);
			homepage=new HomePage(driver);

			if (!homepage.isLoggedIn(userName))
				throw new Exception("Some Error encountered while logging..check testdata..");
			Log.message("5. Logged in to the vault (" + testData.get("VaultName") + ").", driver);

			//Step-6: Select Advanced Search Options
			SearchPanel.searchOrNavigatetoView(driver, testData.get("SearchType"), "");

			//Step-7: Verify number of objects displayed in listing view
			if (homepage.listView.itemCount() ==  Integer.parseInt(testData.get("Maximum search results")))
				Log.pass("Test Passed. Search results display less than or equal to Maximum number of search results value.");
			else
				Log.fail("Test Failed. Search results display greater than to Maximum number of search results value.", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			driver.quit();	
		} //End finally
	}

	/**
	 * TestCase ID: Test1_6_2 Verify Search in right pane option from configuration page 
	 * <br>Description:  Open /configuration.aspx page instead of /login.aspx</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_6_2",description="Test1_6_2 : Search in right pane ")
	public void Test1_6_2(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData = new ConcurrentHashMap <String, String>(dataValues);
		LoginPage loginPage=null;

		ConfigurationPage configPage=null;
		HomePage homepage=null;
		try {

			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");	
			Log.message("1. Logged in to Configuration Page", driver);

			//Step-2: Click Vault from left panel of Configuration Page
			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(testData.get("VaultName"));
			Log.message("2. Clicked 'Sample vault' from left panel of Configuration Page", driver);


			//Step-3 : Set value for Maximum number of search results

			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
			}

			configPage.expandVaultFolder(documentVault);
			Log.message("3. Clicked '"+documentVault+"' from left panel of Configuration Page", driver);

			//Step-4 : Set the control values

			configPage.clickSettingsFolder("Controls");
			if (configPage.configurationPanel.getVaultCommands(testData.get("Control")).equalsIgnoreCase("Hide")) {
				configPage.chooseConfigurationVaultSettings(driver, testData.get("Control"),"controls","Show");
				configPage.clickSaveButton();

				// Check any warning dialog displayed
				if(configPage.isWarningDialogDisplayed())
				{
					configPage.clickResetButton();
					configPage.chooseConfigurationVaultSettings(driver, testData.get("Control"),"controls","Show");
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();  
				}
				else
					configPage.clickOKBtnOnSaveDialog();

			}
			Log.message("4. Show " + testData.get("Control") + " is enabled and settings are saved.", driver);
			//Step-3 : Login to the vault
			configPage.clickLogOut(); //Logs out from the Configuration page
			Log.message("5. LoggedOut from configuration page.", driver);

			//Step-5: Login to Web Access Vault
			loginPage.navigateToApplication(webSite,userName,password,testData.get("VaultName"));
			Utils.waitForPageLoad(driver);
			homepage=new HomePage(driver);

			if (!homepage.isLoggedIn(userName))
				throw new Exception("Some Error encountered while logging..check testdata..");
			Log.message("6. Logged in to the vault (" + testData.get("VaultName") + ").", driver);

			//Step-7: Verify if search tab is displayed in right pane
			if (homepage.previewPane.isTabExists("Search"))
				Log.pass("Test Passed. Search is displayed in right pane .");
			else
				Log.fail("Test Failed. Search is not displayed in right pane.", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try {
				loginPage=new LoginPage(driver);
				driver.get(configSite);
				configPage=new ConfigurationPage(driver);
				configPage.clickVaultFolder(testData.get("VaultName"));		
				configPage.expandVaultFolder(documentVault);
				configPage.clickSettingsFolder("Controls");
				if (configPage.configurationPanel.getVaultCommands(testData.get("Control")).equalsIgnoreCase("Show")) {
					configPage.chooseConfigurationVaultSettings(driver, testData.get("Control"),"controls","Hide");
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				}}

			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch

			finally {

				driver.quit();	
			} //End finally
		} //End finally
	}


	/**
	 * TestCase ID: Test1_6_3 Verify State transition prompt option from configuration page 
	 * <br>Description:  Open /configuration.aspx page instead of /login.aspx</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_6_3",description="Test1_6_3 : State transition prompt ")
	public void Test1_6_3(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData = new ConcurrentHashMap <String, String>(dataValues);
		LoginPage loginPage=null;

		ConfigurationPage configPage=null;
		HomePage homepage=null;
		try {

			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");	
			Log.message("1. Logged in to Configuration Page", driver);

			//Step-2: Click Vault from left panel of Configuration Page
			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(testData.get("VaultName"));
			Log.message("2. Clicked 'Sample vault' from left panel of Configuration Page", driver);


			//Step-3 : Set value for Maximum number of search results

			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
			}

			configPage.expandVaultFolder(documentVault);
			Log.message("3. Clicked '"+documentVault+"' from left panel of Configuration Page", driver);

			//Step-4 : Set the control values

			configPage.clickSettingsFolder("Controls");
			if (configPage.configurationPanel.getVaultCommands(testData.get("Control")).equalsIgnoreCase("Hide")) {
				configPage.chooseConfigurationVaultSettings(driver, testData.get("Control"),"controls","Show");
				configPage.clickSaveButton();

				// Check any warning dialog displayed
				if(configPage.isWarningDialogDisplayed())
				{
					configPage.clickResetButton();
					configPage.chooseConfigurationVaultSettings(driver, testData.get("Control"),"controls","Show");
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();  
				}
				else
					configPage.clickOKBtnOnSaveDialog();
			}
			Log.message("4. Show " + testData.get("Control") + " is enabled and settings are saved.", driver);
			//Step-3 : Login to the vault
			configPage.clickLogOut(); //Logs out from the Configuration page
			Log.message("5. LoggedOut from configuration page.", driver);

			//Step-5: Login to Web Access Vault
			loginPage.navigateToApplication(webSite,userName,password,testData.get("VaultName"));
			Utils.waitForPageLoad(driver);
			homepage=new HomePage(driver);

			if (!homepage.isLoggedIn(userName))
				throw new Exception("Some Error encountered while logging..check testdata..");
			Log.message("6. Logged in to the vault (" + testData.get("VaultName") + ").", driver);

			//Step-6 : To perform all object search
			//---------------------------------------
			SearchPanel searchpanel = new SearchPanel(driver);
			searchpanel.clickSearchBtn(driver);

			Log.message("7. Perform all object search.", driver); 

			//Step-7 : Select any existing object
			//-------------------------------------
			homepage.listView.clickItemByIndex(0);
			Utils.fluentWait(driver);
			String selecteddocument = homepage.listView.getItemNameByItemIndex(0);
			Utils.fluentWait(driver);
			Log.message("8. Select any existing object." + selecteddocument, driver);

			//Step-8 : Click workflow option from operation menu
			//------------------------------------------
			homepage.menuBar.ClickOperationsMenu("Workflow");
			Utils.fluentWait(driver);
			Log.message("9 : Click Workflow option from operation menu.", driver);

			//Step-9: Verify number of objects displayed in listing view
			if (homepage.isWorkflowdialogDisplayed())
				Log.pass("Test Passed. Workflow dialog is Displayed in MFWA.");
			else
				Log.fail("Test Failed. Workflow dialog is not Displayed in MFWA.", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			driver.quit();	
		} //End finally
	}


	/**
	 * TestCase ID: Test1_6_4 Verify Force to Use UTC date time for all users option from configuration page
	 * <br>Description:  Open /configuration.aspx page instead of /login.aspx</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test1_6_4",description="Test1_6_3 : State transition prompt ")
	public void Test1_6_4(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData = new ConcurrentHashMap <String, String>(dataValues);
		LoginPage loginPage=null;

		ConfigurationPage configPage=null;
		HomePage homepage=null;
		try {

			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");	
			Log.message("1. Logged in to Configuration Page", driver);

			//Step-2: Click Vault from left panel of Configuration Page
			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(testData.get("VaultName"));
			Log.message("2. Select 'Sample vault' from left panel of Configuration Page", driver);


			//Step-3 : Set value for Maximum number of search results

			if (!configPage.configurationPanel.getVaultAccess()) {
				configPage.configurationPanel.setVaultAccess(true); //Allows access to this vault
			}

			configPage.expandVaultFolder(documentVault);
			Log.message("3. Clicked '"+documentVault+"' from left panel of Configuration Page", driver);

			//Step-4 : Set the control values

			configPage.setUTCdate(true); 
			configPage.clickSaveButton();
			// Check any warning dialog displayed
			if(configPage.isWarningDialogDisplayed())
			{
				configPage.clickResetButton();
				configPage.setUTCdate(true); 
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();  
			}
			else
				configPage.clickOKBtnOnSaveDialog();

			Log.message("4. UTC date option is enabled and settings are saved.", driver);
			//Step-3 : Login to the vault
			configPage.clickLogOut(); //Logs out from the Configuration page
			Log.message("5. LoggedOut from configuration page.", driver);

			//Step-5: Login to Web Access Vault
			loginPage.navigateToApplication(webSite,userName,password,testData.get("VaultName"));
			Utils.waitForPageLoad(driver);
			homepage=new HomePage(driver);

			if (!homepage.isLoggedIn(userName))
				throw new Exception("Some Error encountered while logging..check testdata..");
			Log.message("6. Logged in to the vault (" + testData.get("VaultName") + ").", driver);

			//Step-6 : To perform all object search
			//---------------------------------------
			SearchPanel searchpanel = new SearchPanel(driver);
			searchpanel.clickSearchBtn(driver);

			Log.message("7. Perform all object search.", driver); 

			//Step-7 : Select any existing object
			//-------------------------------------
			homepage.listView.clickItemByIndex(0);
			Utils.fluentWait(driver);
			String selecteddocument = homepage.listView.getItemNameByItemIndex(0);
			Utils.fluentWait(driver);
			Log.message("8. Select any existing object." + selecteddocument, driver);

			//Step-8 : Click workflow option from task pane area
			//------------------------------------------

			MetadataCard metadata = new MetadataCard(driver, true);
			String createddate = metadata.getCreatedDate();

			//Step-9: Verify number of objects displayed in listing view
			if (createddate.contains("GMT"))
				Log.pass("Test Passed. Created date displayed in UTC format.");
			else
				Log.fail("Test Failed. Created date is not displayed in UTC format.", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			driver.quit();	
		} //End finally
	}//End Test1_6_4

	/**
	 * Test_42213 : Check if automatic login credentials field is enabled/disabled while enable and disabled the Automatic login checkbox
	 * 
	 * @param dataValues
	 * @param currentDriver
	 * @param context
	 * @throws Exception
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test_42213",description="Check if automatic login credentials field is enabled/disabled while enable and disabled the Automatic login checkbox")
	public void Test_42213(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		WebDriver driver= null;
		
		ConcurrentHashMap <String, String> testData=new ConcurrentHashMap <String, String>(dataValues);
		LoginPage loginPage=null;
		ConfigurationPage configPage = null;

		try {

			//Step-1: Launch configuration page
			//---------------------------------
			driver = WebDriverUtils.getDriver(currentDriver,2);
			driver.get(configSite);
			loginPage=new LoginPage(driver);//Instantiate Login page
			loginPage.loginToConfigurationUI(userName,password);//login with admin credentials

			Log.message("1. Logged in to the Configuration page with admin credentials.", driver);

			//Verify if Login failed
			if (!loginPage.getErrorMessage().isEmpty())//Verify if error message is displayed
				throw new Exception("Unable to login to Configuration Page, with Error :"+loginPage.getErrorMessage());

			//Step-2:  Navigated to the General page in Web Access Configuration.
			//-------------------------------------------------------------------
			configPage = new ConfigurationPage(driver);
			configPage.clickTreeViewItem(testData.get("GeneralSettings")); //Selects General in the tree view of configuration page

			Log.message("2. Navigated to the General settings in Web Access Configuration.", driver);

			//Step-3 : Set the automatic login credentials
			//--------------------------------------------
			configPage.configurationPanel.setDefaultAuthType(testData.get("AuthenticationType")+" user");//Sets the default authentication type as Windows/M-Files user

			configPage.configurationPanel.setAutoLogin(true);//Select the auto login in configuration page

			Log.message("3. Automatic login is enabled for "+testData.get("AuthenticationType")+" authentication type in the configuration page.", driver);

			//Verification: Check corresponding fields are enabled or not based on the Authentication type
			//--------------------------------------------------------------------------------------------
			String result = "";

			if (testData.get("AuthenticationType").contains("Windows"))
			{

				if (!configPage.configurationPanel.isAutoLoginUserNameEnabled())
					result = " User name field is not enabled for windows user automatic login;";

				if (!configPage.configurationPanel.isAutoLoginPasswordEnabled())
					result += " Password field is not enabled for windows user automatic login;";

				if (!configPage.configurationPanel.isAutoLoginDomainEnabled())
					result += " Domain name field is not enabled for windows user automatic login;";
			}
			else
			{
				if (!configPage.configurationPanel.isAutoLoginUserNameEnabled())
					result = " User name field is not enabled for M-Files user automatic login;";

				if (!configPage.configurationPanel.isAutoLoginPasswordEnabled())
					result += " Password field is not enabled for M-Files user automatic login;";

				if (configPage.configurationPanel.isAutoLoginDomainEnabled())
					result += " Domain name field is enabled for M-Files user automatic login;";
			}

			if(result.equals(""))
				Log.pass("Test case passed. While enable Automatic login for "+testData.get("AuthenticationType")+" user, UserName/DomainName/Password fields are enabled as expected", driver);
			else
				Log.fail("Test case failed. While enable Automatic login for "+testData.get("AuthenticationType")+" user, UserName/DomainName/Password fields are not enabled as expected. [Additional Info.: "+ result +"]", driver);


		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End Finally
	} //End Test_42213

	/**
	 * Test_42213_1 : Check if proper warning message is displayed while giving invalid credentials in the Credentials field and selecting the vault
	 * 
	 * @param dataValues
	 * @param currentDriver
	 * @param context
	 * @throws Exception
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test_42213_1",description="Check if proper warning message is displayed while giving invalid credentials in the Credentials field and selecting the vault")
	public void Test_42213_1(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		WebDriver driver= null;
		
		ConcurrentHashMap <String, String> testData=new ConcurrentHashMap <String, String>(dataValues);
		LoginPage loginPage=null;
		ConfigurationPage configPage = null;

		try {

			//Step-1: Launch configuration page
			//---------------------------------
			driver = WebDriverUtils.getDriver(currentDriver,2);
			driver.get(configSite);
			loginPage=new LoginPage(driver);//Instantiate Login page
			loginPage.loginToConfigurationUI(userName,password);//login with admin credentials

			Log.message("1. Logged in to the Configuration page with admin credentials.", driver);

			//Verify if Login failed
			if (!loginPage.getErrorMessage().isEmpty())//Verify if error message is displayed
				throw new Exception("Unable to login to Configuration Page, with Error :"+loginPage.getErrorMessage());

			//Step-2:  Navigated to the General page in Web Access Configuration.
			//-------------------------------------------------------------------
			configPage = new ConfigurationPage(driver);
			configPage.clickTreeViewItem(testData.get("GeneralSettings")); //Selects General in the tree view of configuration page

			Log.message("2. Navigated to the General settings in Web Access Configuration.", driver);

			//Step-3 : Set the automatic login credentials
			//--------------------------------------------
			configPage.configurationPanel.setDefaultAuthType(testData.get("AuthenticationType")+" user");//Sets the default authentication type as Windows/M-Files user

			configPage.configurationPanel.setAutoLogin(true);//Select the auto login in configuration page

			Log.message("3. Automatic login is enabled for "+testData.get("AuthenticationType")+" authentication type in the configuration page.", driver);

			//Step-4: Sets the Invalid credentials and selects the vault
			//----------------------------------------------------------
			if (testData.get("AuthenticationType").contains("Windows"))
			{
				configPage.configurationPanel.setDefaultAuthType(testData.get("AuthenticationType")+" user");//Sets the default authentication type as Windows user
				configPage.configurationPanel.setAutoLoginUserName(windowsUserName+" Test");//Sets the Windows user name
				configPage.configurationPanel.setAutoLoginPassword(windowsPassword);//Sets the Windows user password
				configPage.configurationPanel.setAutoLoginDomain(windowsUserDomain);//Sets the Windows user domain
				configPage.configurationPanel.setAutoLoginVault(documentVault);//Selects the vault
			}
			else
			{
				configPage.configurationPanel.setDefaultAuthType(testData.get("AuthenticationType")+" user");//Sets the default authentication type as M-Files user
				configPage.configurationPanel.setAutoLoginUserName(userName+" Test");//Sets the M-Files user name
				configPage.configurationPanel.setAutoLoginPassword(password);//Sets the M-Files user password
				configPage.configurationPanel.setAutoLoginVault(documentVault);//Selects the vault
			}

			Log.message("4. Invalid credentials are set and vault is selected in the configuration webpage.", driver);

			//Verification if warning dialog is displayed for invalid credentials
			//-------------------------------------------------------------------
			configPage.saveSettings();//Clicks the save the button

			if (!MFilesDialog.exists(driver))//Checks if MFiles dialog is displayed or not
				throw new Exception("Warning dialog is not displayed while saving invalid credentials.");

			MFilesDialog mfDialog = new MFilesDialog(driver);//Instantiates the MFiles Dialog

			if(mfDialog.getMessage().equals(testData.get("ExpectedMsg")))
				Log.pass("Test case passed. Warning dialog is displayed as expected with the warning message("+testData.get("ExpectedMsg")+") while saving invalid credentials with vault selection in the configuration webpage.", driver);
			else
				Log.fail("Test case failed. Warning dialog is not displayed as expected with the warning message("+testData.get("ExpectedMsg")+") while saving invalid credentials with vault selection in the configuration webpage.", driver);

			mfDialog.close();//Closes the MFiles dialog in the view

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End Finally
	} //End Test_42213_1

	/**
	 * Test_42213_2 : Check if login page is displayed while launch the default.aspx for the invalid crednetials saved
	 * 
	 * @param dataValues
	 * @param currentDriver
	 * @param context
	 * @throws Exception
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test_42213_2",description="Check if login page is displayed while launch the default.aspx for the invalid crednetials saved")
	public void Test_42213_2(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		WebDriver driver= null;
		
		ConcurrentHashMap <String, String> testData=new ConcurrentHashMap <String, String>(dataValues);
		LoginPage loginPage=null;
		ConfigurationPage configPage = null;

		try {

			//Step-1: Launch configuration page
			//---------------------------------
			driver = WebDriverUtils.getDriver(currentDriver,2);
			driver.get(configSite);
			loginPage=new LoginPage(driver);//Instantiate Login page
			loginPage.loginToConfigurationUI(userName,password);//login with admin credentials

			Log.message("1. Logged in to the Configuration page with admin credentials.", driver);

			//Verify if Login failed
			if (!loginPage.getErrorMessage().isEmpty())//Verify if error message is displayed
				throw new Exception("Unable to login to Configuration Page, with Error :"+loginPage.getErrorMessage());

			//Step-2:  Navigated to the General page in Web Access Configuration.
			//-------------------------------------------------------------------
			configPage = new ConfigurationPage(driver);
			configPage.clickTreeViewItem(testData.get("GeneralSettings")); //Selects General in the tree view of configuration page

			Log.message("2. Navigated to the General settings in Web Access Configuration.", driver);

			//Step-3 : Set the automatic login credentials
			//--------------------------------------------
			configPage.configurationPanel.setDefaultAuthType(testData.get("AuthenticationType")+" user");//Sets the default authentication type as Windows/M-Files user

			configPage.configurationPanel.setAutoLogin(true);//Select the auto login in configuration page

			Log.message("3. Automatic login is enabled for "+testData.get("AuthenticationType")+" authentication type in the configuration page.", driver);

			//Step-4: Sets the Invalid credentials and selects the vault
			//----------------------------------------------------------
			if (testData.get("AuthenticationType").contains("Windows"))
			{
				configPage.configurationPanel.setDefaultAuthType(testData.get("AuthenticationType")+" user");//Sets the default authentication type as Windows user
				configPage.configurationPanel.setAutoLoginUserName(windowsUserName+" Test");//Sets the Windows user name
				configPage.configurationPanel.setAutoLoginPassword(windowsPassword);//Sets the Windows user password
				configPage.configurationPanel.setAutoLoginDomain(windowsUserDomain);//Sets the Windows user domain
				configPage.configurationPanel.setAutoLoginVault("");
			}
			else
			{
				configPage.configurationPanel.setDefaultAuthType(testData.get("AuthenticationType")+" user");//Sets the default authentication type as M-Files user
				configPage.configurationPanel.setAutoLoginUserName(userName+" Test");//Sets the M-Files user name
				configPage.configurationPanel.setAutoLoginPassword(password);//Sets the M-Files user password
				configPage.configurationPanel.setAutoLoginVault("");
			}

			Log.message("4. Invalid credentials are set in the configuration webpage.", driver);

			//Step-5: Save the changes
			//-------------------------
			configPage.saveSettings();//Clicks the save the button

			if (!MFilesDialog.exists(driver))//Checks if MFiles dialog is displayed or not
				throw new Exception("Warning dialog is not displayed while saving invalid credentials.");

			Log.message("5. Saved the changes.", driver);

			//Step-6: Logout from Web Access
			//-------------------------------
			MFilesDialog.closeMFilesDialog(driver);//Closes the MFiles Dialog

			configPage.logOut();//Logs out from Configuration webpage

			Log.message("6. Logged out from Configuration Webpage.", driver);

			//Verification IF login page is displayed for invalid credentials
			//---------------------------------------------------------------
			driver.get(defaultSite);
			loginPage=new LoginPage(driver);//Instantiate Login page

			if(loginPage.isUserNameFieldDisplayed())
				Log.pass("Test case passed. Login page is displayed while launching the default url with invalid credentials saved for auto login in the configuration webpage.", driver);
			else
				Log.fail("Test case failed. Login page is not displayed while launching the default url with invalid credentials saved for auto login in the configuration webpage.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End Finally
	} //End Test_42213_2


	/**
	 * Test_42213_3 : Check if default page is displayed while launch the default.aspx for the valid crednetials saved
	 * 
	 * @param dataValues
	 * @param currentDriver
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test_42213_3",description="Test_42213_3 : Check if default page is displayed while launch the default.aspx for the valid crednetials saved")
	public void Test_42213_3(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		WebDriver driver= null;
		
		ConcurrentHashMap <String, String> testData=new ConcurrentHashMap <String, String>(dataValues);
		LoginPage loginPage=null;
		ConfigurationPage configPage = null;

		try {

			//Step-1: Launch configuration page
			//---------------------------------
			driver = WebDriverUtils.getDriver(currentDriver,2);
			driver.get(configSite);
			loginPage=new LoginPage(driver);//Instantiate Login page
			loginPage.loginToConfigurationUI(userName,password);//login with admin credentials

			Log.message("1. Logged in to the Configuration page with admin credentials.", driver);

			//Verify if Login failed
			if (!loginPage.getErrorMessage().isEmpty())//Verify if error message is displayed
				throw new Exception("Unable to login to Configuration Page, with Error :"+loginPage.getErrorMessage());

			//Step-2:  Navigated to the General page in Web Access Configuration.
			//-------------------------------------------------------------------
			configPage = new ConfigurationPage(driver);
			configPage.clickTreeViewItem(testData.get("GeneralSettings")); //Selects General in the tree view of configuration page

			Log.message("2. Navigated to the General settings in Web Access Configuration.", driver);

			//Step-3 : Set the automatic login credentials
			//--------------------------------------------
			configPage.configurationPanel.setDefaultAuthType(testData.get("AuthenticationType")+" user");//Sets the default authentication type as Windows/M-Files user

			configPage.configurationPanel.setAutoLogin(true);//Select the auto login in configuration page

			Log.message("3. Automatic login is enabled for "+testData.get("AuthenticationType")+" authentication type in the configuration page.", driver);

			//Step-4: Sets the Invalid credentials and selects the vault
			//----------------------------------------------------------
			if (testData.get("AuthenticationType").contains("Windows"))
			{
				configPage.configurationPanel.setDefaultAuthType(testData.get("AuthenticationType")+" user");//Sets the default authentication type as Windows user
				configPage.configurationPanel.setAutoLoginUserName(windowsUserName);//Sets the Windows user name
				configPage.configurationPanel.setAutoLoginPassword(windowsPassword);//Sets the Windows user password
				configPage.configurationPanel.setAutoLoginDomain(windowsUserDomain);//Sets the Windows user domain
				configPage.configurationPanel.setAutoLoginVault(documentVault);
			}
			else
			{
				configPage.configurationPanel.setDefaultAuthType(testData.get("AuthenticationType")+" user");//Sets the default authentication type as M-Files user
				configPage.configurationPanel.setAutoLoginUserName(userName);//Sets the M-Files user name
				configPage.configurationPanel.setAutoLoginPassword(password);//Sets the M-Files user password
				configPage.configurationPanel.setAutoLoginVault(documentVault);
			}

			Log.message("4. Valid credentials are set and vault is selected in the configuration webpage.", driver);

			//Step-5: Save the changes
			//-------------------------
			configPage.saveSettings();//Clicks the save the button

			if (!MFilesDialog.exists(driver))//Checks if MFiles dialog is displayed or not
				throw new Exception("Warning dialog is not displayed while saving invalid credentials.");

			Log.message("5. Saved the changes.", driver);

			//Step-6: Logout from Web Access
			//-------------------------------
			MFilesDialog.closeMFilesDialog(driver);//Closes the MFiles Dialog

			configPage.logOut();//Logs out from Configuration webpage

			Log.message("6. Logged out from Configuration Webpage.", driver);

			//Verification IF login page is displayed for invalid credentials
			//---------------------------------------------------------------
			driver.get(defaultSite);
			int snooze = 0;

			while (snooze < 10 && !driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX") && !driver.getCurrentUrl().toUpperCase().contains("DEFAULT.ASPX"))
			{
				Thread.sleep(500);
				snooze++;
			}

			if(!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				Log.pass("Test case passed. Default page is displayed while launching the default url with valid credentials saved for auto login in the configuration webpage.", driver);
			else
				Log.fail("Test case failed. Default page is not displayed while launching the default url with valid credentials saved for auto login in the configuration webpage.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch
		finally {
			if (driver != null)
			{
				Utility.logOut(driver);
				driver.get(configSite);
				loginPage=new LoginPage(driver);//Instantiate Login page
				loginPage.loginToConfigurationUI(userName,password);//login with admin credentials
				configPage = new ConfigurationPage(driver);
				configPage.clickTreeViewItem(testData.get("GeneralSettings")); //Selects General in the tree view of configuration page
				configPage.configurationPanel.setAutoLogin(false);//Select the auto login in configuration page
				configPage.saveSettings();//Click the save button
				MFilesDialog.closeMFilesDialog(driver);//Closes the M-Files dialog in the view
				configPage.clickLogOut(); //Logs out from the Configuration page
			}
			Utility.quitDriver(driver);
		} //End Finally
	} //End Test_42213_3


	/**
	 * Test_42213_4 : Check if default page is displayed while launch the default.aspx when automatic login enabled with valid credentials
	 * 
	 * @param dataValues
	 * @param currentDriver
	 * @param context
	 * @throws Exception
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test_42213",description="Check if default page is displayed while launch the default.aspx when automatic login enabled with valid credentials")
	public void Test_42213_4(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		WebDriver driver= null;
		
		ConcurrentHashMap <String, String> testData=new ConcurrentHashMap <String, String>(dataValues);
		LoginPage loginPage=null;
		ConfigurationPage configPage = null;

		try {

			//Step-1: Launch configuration page
			//---------------------------------
			driver = WebDriverUtils.getDriver(currentDriver,2);
			driver.get(configSite);
			loginPage=new LoginPage(driver);//Instantiate Login page
			loginPage.loginToConfigurationUI(userName,password);//login with admin credentials

			Log.message("1. Logged in to the Configuration page with admin credentials.", driver);

			//Verify if Login failed
			if (!loginPage.getErrorMessage().isEmpty())//Verify if error message is displayed
				throw new Exception("Unable to login to Configuration Page, with Error :"+loginPage.getErrorMessage());

			//Step-2:  Navigated to the General page in Web Access Configuration.
			//-------------------------------------------------------------------
			configPage = new ConfigurationPage(driver);
			configPage.clickTreeViewItem(testData.get("GeneralSettings")); //Selects General in the tree view of configuration page

			Log.message("2. Navigated to the General settings in Web Access Configuration.", driver);

			//Step-3 : Set the automatic login credentials
			//--------------------------------------------
			configPage.configurationPanel.setAutoLogin(true);//Select the auto login in configuration page

			if (testData.get("AuthenticationType").contains("Windows"))
			{
				configPage.configurationPanel.setDefaultAuthType(testData.get("AuthenticationType")+" user");//Sets the default authentication type as Windows user
				configPage.configurationPanel.setAutoLoginUserName(windowsUserName);//Sets the Windows user name
				configPage.configurationPanel.setAutoLoginPassword(windowsPassword);//Sets the Windows user password
				configPage.configurationPanel.setAutoLoginDomain(windowsUserDomain);//Sets the Windows user domain
			}
			else
			{
				configPage.configurationPanel.setDefaultAuthType(testData.get("AuthenticationType")+" user");//Sets the default authentication type as M-Files user
				configPage.configurationPanel.setAutoLoginUserName(userName);//Sets the M-Files user name
				configPage.configurationPanel.setAutoLoginPassword(password);//Sets the M-Files user password
			}

			Log.message("3. Automatic login credentials are set for "+testData.get("AuthenticationType")+" authentication type in the configuration page.", driver);

			//Step-4 : Save the settings in configuration page
			//------------------------------------------------
			configPage.clickSaveButton();//Click the save button

			if (!MFilesDialog.exists(driver))
				throw new Exception("Error while saving changes");

			Log.message("4. Clicked the save button in configuration page.", driver);

			//Step-5 : Logout from the configuration page
			//-------------------------------------------
			MFilesDialog.closeMFilesDialog(driver);//Closes the M-Files dialog in the view
			configPage.clickLogOut(); //Logs out from the Configuration page

			Log.message("5. Logged out from the configuration page.", driver);

			//Step-8 : select the vault list in the login page
			//------------------------------------------------
			driver.get(defaultSite);
			int snooze = 0;

			while (snooze < 10 && !driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX") && !driver.getCurrentUrl().toUpperCase().contains("DEFAULT.ASPX"))
			{
				Thread.sleep(500);
				snooze++;
			}

			String result = "";

			if (driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
			{
				loginPage = new LoginPage(driver);
				if (loginPage.isVaultListDisplayed())
					result = "";
				else
					result = "Automatic login is not success.";
			}
			else if (driver.getCurrentUrl().toUpperCase().contains("DEFAULT.ASPX") || !driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				result = "";
			else
				result = "Automatic login is not success.";


			if(result.equals(""))
				Log.pass("Test case passed. Automatic login is working as expected for "+testData.get("AuthenticationType")+" user.", driver);
			else
				Log.fail("Test case failed. Automatic login is not working as expected for "+testData.get("AuthenticationType")+" user.", driver);


		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch
		finally {
			if (driver != null)
			{
				driver.quit();
				driver = WebDriverUtils.getDriver(currentDriver,2);
				driver.get(configSite);
				loginPage=new LoginPage(driver);//Instantiate Login page
				loginPage.loginToConfigurationUI(userName,password);//login with admin credentials
				configPage = new ConfigurationPage(driver);
				configPage.clickTreeViewItem(testData.get("GeneralSettings")); //Selects General in the tree view of configuration page
				configPage.configurationPanel.setAutoLogin(false);//Select the auto login in configuration page
				configPage.clickSaveButton();//Click the save button
				MFilesDialog.closeMFilesDialog(driver);//Closes the M-Files dialog in the view
				configPage.clickLogOut(); //Logs out from the Configuration page
			}
			Utility.quitDriver(driver);

		}
	} //End Test_42213_4

	/**
	 * Test_38304 : Enable the Windows SSO - 'Show on login page' in configuration page
	 * 
	 * @param dataValues
	 * @param currentDriver
	 * @param context
	 * @throws Exception
	 *//*

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test_38304",description="Test_38304 : Windows SSO - 'Show on login page'")
	public void Test_38304(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		String ShowonloginWinSSOValue ="Show on login page";
		String defaultWinSSOValue ="Disabled (Default)";
		String actualWinSSOValue;
		driver = WebDriverUtils.getDriver();
		Windows SSO - 'Set the Show on login page");

		ConcurrentHashMap <String, String> testData=new ConcurrentHashMap <String, String>(dataValues);
		LoginPage loginPage=null;
		ConfigurationPage configPage = null;

		try {

			//Step-1: Launch configuration page
			//---------------------------------
			driver.get(configSite);
			loginPage=new LoginPage(driver);//Instantiate configuration page
			loginPage.loginToConfigurationUI(userName,password);//login with admin credentials

			Log.message("1. Logged in to the Configuration page with admin credentials.");

			//Verify if Login failed
			if (!loginPage.getErrorMessage().isEmpty())//Verify if error message is displayed
				throw new Exception("Unable to login to Configuration Page, with Error :"+loginPage.getErrorMessage());

			//Step-2:  Navigated to the General page in Web Access Configuration.
			//-------------------------------------------------------------------
			configPage = new ConfigurationPage(driver);
			configPage.clickTreeViewItem(testData.get("GeneralSettings")); //Selects General in the tree view of configuration page

			Log.message("2. Navigated to the General settings in Web Access Configuration.");

			//Step-3 : Get the WindowsSSO value
			//---------------------------------
			actualWinSSOValue = configPage.configurationPanel.getWindowsSSO(); //Gets the actual value selected in window sso

			Log.message("3. Fetched the 'WindowsSSO' default value as '"+actualWinSSOValue+"' in Web Access Configuration.");

			//Step-4 : Set the Windows SSO as 'Show on login page' radio button
			//-----------------------------------------------------------------
			configPage.configurationPanel.setWindowsSSO(ShowonloginWinSSOValue);//set the Show on login page in the configuration page

			Log.message("4. Set the " + ShowonloginWinSSOValue + " value in Window SSO option.", driver);

			//Step-5 : Set the automatic login credentials
			//--------------------------------------------
			configPage.configurationPanel.setAutoLogin(true);//Select the auto login in configuration page
			configPage.configurationPanel.setAutoLoginUserName("gowrimeena.sethu");
			configPage.configurationPanel.setAutoLoginPassword("merlinqa@321");

			Log.message("5. Set the automatic login credentials in configuration page.", driver);

			//Step-5 : Save the settings in configuration page
			//------------------------------------------------
			configPage.clickSaveButton();//Click the save button

			Log.message("5. Clicked the save button in configuration page.",driver);

			//Step-6 : Logout from the configuration page
			//-------------------------------------------
			configPage.clickLogOut(); //Logs out from the Configuration page

			Log.message("6. Clicked the log out from the configuration page.");

			//Step-7: login to the web access default page
			//--------------------------------------------
			if(!loginPage.isWindowLoginDisplayed())
				throw new Exception("'Log in with current windows credentials' is not displayed in loging page.");

			loginPage.clickWindowLoginlink();
			Utils.waitForPageLoad(driver);

			Log.message("7. Selected the 'Log in with current windows credentials' in login page.", driver);

			//Step-8 : select the vault list in the login page
			//------------------------------------------------
			loginPage.selectWindowsUserVault(documentVault);

			Log.message("8. Logged in with windows credentials.", driver);

			}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch
		finally {
			try {
				Utility.logOut(driver);
				driver.get(configSite);
				loginPage.loginToConfigurationUI(userName,password);//login with admin credentials

				Log.message("Logged in with the admin user credentials.", driver);

				configPage.clickTreeViewItem(testData.get("GeneralSettings")); //Selects General in the tree view of configuration page

				Log.message("Selected the general settings in the configuration page.",driver);

				if(!configPage.configurationPanel.getWindowsSSO().equals(defaultWinSSOValue)) {
					configPage.configurationPanel.setWindowsSSO(defaultWinSSOValue);
					configPage.configurationPanel.setAutoLogin(false);//un select the auto login
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				}
			}

			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch
			finally {
				driver.quit();
			} } //End finally
	} //End Test_38304


	  *//**
	  * Test_38301 : Enable the Windows SSO - 'Show on login page' in configuration page
	  * 
	  * @param dataValues
	  * @param currentDriver
	  * @param context
	  * @throws Exception
	  *//*

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test_38301",description="Test_38301	: Windows SSO - 'Show on login page'")
	public void Test_38301(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		String ShowonloginWinSSOValue ="Show on login page";
		String defaultWinSSOValue ="Disabled (Default)";
		String actualWinSSOValue;
		driver = WebDriverUtils.getDriver();
		Windows SSO - 'Set the Show on login page");

		ConcurrentHashMap <String, String> testData=new ConcurrentHashMap <String, String>(dataValues);
		LoginPage loginPage=null;
		ConfigurationPage configPage = null;

		try {

			//Step-1: Launch configuration page
			//---------------------------------
			driver.get(configSite);
			loginPage=new LoginPage(driver);//Instantiate configuration page
			loginPage.loginToConfigurationUI(userName,password);//login with admin credentials

			Log.message("1. Logged in to the Configuration page with admin credentials.");

			//Verify if Login failed
			if (!loginPage.getErrorMessage().isEmpty())//Verify if error message is displayed
				throw new Exception("Unable to login to Configuration Page, with Error :"+loginPage.getErrorMessage());

			//Step-2:  Navigated to the General page in Web Access Configuration.
			//-------------------------------------------------------------------
			configPage = new ConfigurationPage(driver);
			configPage.clickTreeViewItem(testData.get("GeneralSettings")); //Selects General in the tree view of configuration page

			Log.message("2. Navigated to the General settings in Web Access Configuration.");

			//Step-3 : Get the WindowsSSO value
			//---------------------------------
			actualWinSSOValue = configPage.configurationPanel.getWindowsSSO(); //Gets the actual value selected in window sso

			Log.message("3. Fetched the 'WindowsSSO' default value as '"+actualWinSSOValue+"' in Web Access Configuration.");

			//Step-4 : Set the Windows SSO as 'Show on login page' radio button
			//-----------------------------------------------------------------
			configPage.configurationPanel.setWindowsSSO(ShowonloginWinSSOValue);
			configPage.configurationPanel.setAutoLogin(false);//un select the auto login

			Log.message("4. Set the " + ShowonloginWinSSOValue + " value in Window SSO option.", driver);

			//Step-5 : Save the settings in configuration page
			//------------------------------------------------
			configPage.clickSaveButton();//Click the save button

			Log.message("5. Clicked the save button in configuration page.",driver);

			//Step-6 : Logout from the configuration page
			//-------------------------------------------
			configPage.clickLogOut(); //Logs out from the Configuration page

			Log.message("6. Clicked the log out from the configuration page.");


			//Step-7: login to the web access default page
			//--------------------------------------------
			if(!loginPage.isWindowLoginDisplayed())
				throw new Exception("'Log in with current windows credentials' is not displayed in loging page.");

			loginPage.clickWindowLoginlink();
			Utils.waitForPageLoad(driver);

			Log.message("7. Selected the 'Log in with current windows credentials' in login page.", driver);

			//Step-8 : select the vault list in the login page
			//------------------------------------------------
			loginPage.selectWindowsUserVault(documentVault);

			Log.message("8. Logged in with windows credentials.", driver);

			if(driver.getCurrentUrl().contains("Default.aspx"))
				Log.pass("Test Case Passed. Windows user Successfully logged in with the home page");
			else
				Log.fail("Test Case Failed.Windows user not logged in with the home page", driver);


		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch
		finally {
			try {
				Utility.logOut(driver);
				driver.get(configSite);
				loginPage.loginToConfigurationUI(userName,password);//login with admin credentials

				Log.message("Logged in with the admin user credentials.", driver);

				configPage.clickTreeViewItem(testData.get("GeneralSettings")); //Selects General in the tree view of configuration page

				Log.message("Selected the general settings in the configuration page.",driver);

				if(!configPage.configurationPanel.getWindowsSSO().equals(defaultWinSSOValue)) {
					configPage.configurationPanel.setWindowsSSO(defaultWinSSOValue);
					configPage.configurationPanel.setAutoLogin(false);//un select the auto login
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				}
			}

			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch
			finally {
				driver.quit();
			} } //End finally
	} //End Test_38301


	   *//**
	   * Test_38301 : Verify if authentication error message is displayed when login with the invalid credentials
	   * 
	   * @param dataValues
	   * @param currentDriver
	   * @param context
	   * @throws Exception
	   *//*

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test_38291",description="Test_38291 : Verify if authentication error message is displayed when login with the invalid credentials'")
	public void Test_38291(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {


		driver = WebDriverUtils.getDriver();
		Verify if authentication error message is displayed when login with the invalid credentials");

		ConcurrentHashMap <String, String> testData=new ConcurrentHashMap <String, String>(dataValues);
		LoginPage loginPage=null;

		try {
			//Step-1 : Launch the configuration page 
			//--------------------------------------
			driver.get(configSite);
			loginPage=new LoginPage(driver);//Instantiate configuration page
			loginPage.setUserName(testData.get("UserName"));
			loginPage.setPassword(testData.get("Password"));
			loginPage.clickLoginBtn();

			Log.message("1. Logged in to the Configuration page with admin credentials.");

			//Verification : Verify if invalid user credentials did not login to the vault
			//----------------------------------------------------------------------------
			if (loginPage.getErrorMessage().toUpperCase().contains("AUTHENTICATION FAILED."))
				Log.pass("Test Passed!!!.Error message '"+loginPage.getErrorMessage().toUpperCase()+"', is displayed when login with invalid credentials.");
			else
				Log.fail("Test Failed!!!... Login is un-successful but error message is not displayed as expected." + loginPage.getErrorMessage(), driver);


		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally{
			Utility.quitDriver(driver);
		}//End finally
	}//End Test_38291


	    *//**
	    * Test_38270 : Verify if authentication error message is displayed when login with the invalid credentials
	    * 
	    * Pre-requisite : Need to set the specified user in testdata account details as disabled in vault
	    * @param dataValues
	    * @param currentDriver
	    * @param context
	    * @throws Exception
	    *//*

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test_38270",description="Test_38270 : Verify if authentication error message is displayed when login with the invalid credentials'")
	public void Test_38270(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		driver = WebDriverUtils.getDriver();
		Verify if authentication error message is displayed when login with the invalid credentials");

		ConcurrentHashMap <String, String> testData=new ConcurrentHashMap <String, String>(dataValues);
		LoginPage loginPage=null;
		HomePage homepage=null;

		try {
			//Step-1 : Launch the configuration page 
			//--------------------------------------
			driver.get(webSite);
			loginPage=new LoginPage(driver);//Instantiate configuration page
			loginPage.loginToWebApplication(testData.get("UserName"), testData.get("Password"));

			Log.message("1. Logged in with the specified Username : " + testData.get("UserName") + " and password : " + testData.get("Password"));

			//Verification : Verify if vault name is displayed correctly 
			//----------------------------------------------------------
			homepage=new HomePage(driver);//Initiate the home page
			if(homepage.getBreadcrumbText().trim().equals(testData.get("VaultName").trim()))
				Log.pass("Test Case Passed.Specified vault is displayed which had user credentials.");
			else
				Log.fail("Test Case Failed.Vault displayed is not correctly logged in.", driver);

			}//End try

		catch(Exception e){
				Log.exception(e, driver);
			}//End catch
		finally{
			Utility.quitDriver(driver);
		}//End finally

		}//End Test_38270


	     *//**
	     * Test_38127 : Enable the Windows SSO - 'UseAutomatically' in configuration page
	     * 
	     * @param dataValues
	     * @param currentDriver
	     * @param context
	     * @throws Exception
	     *//*

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName="Test_38127",description="Test_38127 : Windows SSO - 'Use automatically'")
	public void Test_38127(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Variable Declaration
		String windowsUseAutomatically ="Use automatically";
		String defaultWinSSOValue ="Disabled (Default)";
		String actualWinSSOValue;
		driver = WebDriverUtils.getDriver();
		

		ConcurrentHashMap <String, String> testData=new ConcurrentHashMap <String, String>(dataValues);
		LoginPage loginPage=null;
		ConfigurationPage configPage = null;

		try {

			//Step-1: Launch configuration page
			//---------------------------------
			driver.get(configSite);
			loginPage=new LoginPage(driver);//Instantiate configuration page
			loginPage.loginToConfigurationUI(userName,password);//login with admin credentials

			Log.message("1. Logged in to the Configuration page with admin credentials.");

			//Verify if Login failed
			if (!loginPage.getErrorMessage().isEmpty())//Verify if error message is displayed
				throw new Exception("Unable to login to Configuration Page, with Error :"+loginPage.getErrorMessage());

			//Step-2:  Navigated to the General page in Web Access Configuration.
			//-------------------------------------------------------------------
			configPage = new ConfigurationPage(driver);
			configPage.clickTreeViewItem(testData.get("GeneralSettings")); //Selects General in the tree view of configuration page

			Log.message("2. Navigated to the General settings in Web Access Configuration.");

			//Step-3 : Get the WindowsSSO value
			//---------------------------------
			actualWinSSOValue = configPage.configurationPanel.getWindowsSSO(); //Gets the actual value selected in window sso

			Log.message("3. Fetched the 'WindowsSSO' default value as '"+actualWinSSOValue+"' in Web Access Configuration.");

			//Step-4 : Set the Windows SSO as 'Show on login page' radio button
			//-----------------------------------------------------------------
			configPage.configurationPanel.setWindowsSSO(windowsUseAutomatically);
			configPage.configurationPanel.setAutoLogin(false);//un select the auto login
			configPage.configurationPanel.setForceMFilesUserLogin(false);//un select the force m-files user login

			Log.message("4. Set the " + windowsUseAutomatically + " value in Window SSO option.", driver);

			//Step-5 : Save the settings in configuration page
			//------------------------------------------------
			configPage.clickSaveButton();//Click the save button

			Log.message("5. Clicked the save button in configuration page.",driver);

			//Step-6 : Logout from the configuration page
			//-------------------------------------------
			configPage.clickLogOut(); //Logs out from the Configuration page

			Log.message("6. Clicked the log out from the configuration page.");

			//Step-8 : select the vault list in the login page
			//------------------------------------------------
			loginPage.selectWindowsUserVault(documentVault);

			Log.message("8. Logged in with windows credentials.", driver);

			//Verification : Verify if user navigate to the Default aspx page
			//---------------------------------------------------------------
			if(driver.getCurrentUrl().contains("Default.aspx"))
				Log.pass("Test Case Passed. Windows user Successfully logged in with the home page");
			else
				Log.fail("Test Case Failed.Windows user not logged in with the home page", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch
		finally {
			try {
				Utility.logOut(driver);
				driver.get(configSite);
				if(!driver.getCurrentUrl().equals("http://localhost/Configuration.aspx")){
					driver.get(configSite);
						}

				configPage.clickTreeViewItem(testData.get("GeneralSettings")); //Selects General in the tree view of configuration page

				Log.message("Selected the general settings in the configuration page.",driver);

				if(configPage.configurationPanel.getWindowsSSO().equals(windowsUseAutomatically)) {
					configPage.configurationPanel.setWindowsSSO(defaultWinSSOValue);
					configPage.configurationPanel.setAutoLogin(false);//un select the auto login
					configPage.clickSaveButton();
					configPage.clickOKBtnOnSaveDialog();
				}
			}

			catch (Exception e) {
				Log.exception(e, driver);
			} //End catch
			finally {
				driver.quit();
			} } //End finally
	} //End Test_38127
	      */	      	




} //End ConfigurationUI

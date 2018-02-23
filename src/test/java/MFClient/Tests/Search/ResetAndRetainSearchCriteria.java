package MFClient.Tests.Search;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.Utils;
import genericLibrary.WebDriverUtils;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.openqa.selenium.By;
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
import MFClient.Wrappers.ConfigurationPanel;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class ResetAndRetainSearchCriteria {

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
			userName = xmlParameters.getParameter("UserName");
			password = xmlParameters.getParameter("Password");
			testVault = xmlParameters.getParameter("VaultName");		
			userFullName = xmlParameters.getParameter("UserFullName");
			Utility.restoreTestVault();
			Utility.configureUsers(xlTestDataWorkBook);
			className = this.getClass().getSimpleName().toString().trim();
			if (xmlParameters.getParameter("driverType").equalsIgnoreCase("IE"))
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim() + xmlParameters.getParameter("driverVersion").trim();
			else
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim();

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
	}

	/**
	 * 28.3.1: Verify the persistence of search criteria filter upon relogin into MFWA
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "Password","ResetAndRetainSearchCriteria"}, 
			description = "Verify the persistence of search criteria filter upon relogin into MFWA")
	public void SprintTest28_3_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {


			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to Configuration
			//--------------------------
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			Log.message("1. Logged in to Configuiration");

			//2. Set the required settings
			//----------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);
			Utils.fluentWait(driver);
			ConfigurationPanel configurationPanel = new  ConfigurationPanel(driver);
			configurationPanel.setRetainLatestSearchCriteria(false);

			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");
			configurationPage.logOut();

			Log.message("2. Set the required settings");

			//3. Login to the Test vault
			//---------------------------
			LoginPage loginPage = new LoginPage(driver);
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);

			Log.message("3. Login to the Test Vault.");

			//4. Perfrom the Search for a specific object type
			//-------------------------------------------------
			homePage.searchPanel.search(dataPool.get("SearchString"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			Log.message("4. Perfrom the Search for a specific object type");

			//5. Logout and relogin to the vault
			//-----------------------------------
			Utility.logOut(driver);			
			Utils.fluentWait(driver);
			homePage = loginPage.loginToWebApplication(userName, password, testVault);
			Utils.fluentWait(driver);

			Log.message("5. Logout and relogin to the vault");

			//Verification: To verify if the previously selected search options are not preserved
			//------------------------------------------------------------------------------------
			if(!homePage.searchPanel.getSearchWord().trim().equals(dataPool.get("SearchString")) && !homePage.searchPanel.getSearchType().trim().equals(dataPool.get("SearchType")))
				Log.pass("Test Case Passed. The Search option changes were not preserved as expected.");
			else
				Log.fail("Test Case Failed. The Search option changes were preserved.", driver);

			driver.get(configURL);
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			configurationPage = new ConfigurationPage(driver);
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);
			Utils.fluentWait(driver);
			configurationPanel.setRetainLatestSearchCriteria(true);
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
	 * 28.3.2: Verify the persistence of search criteria upon relogin into MFWA after closing the browser
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "Password","ResetAndRetainSearchCriteria"}, 
			description = "Verify the persistence of search criteria upon relogin into MFWA after closing the browser")
	public void SprintTest28_3_2(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to Configuration
			//--------------------------
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			Log.message("1. Logged in to Configuiration");

			//2. Set the required settings
			//----------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);
			Utils.fluentWait(driver);
			ConfigurationPanel configurationPanel = new  ConfigurationPanel(driver);
			configurationPanel.setRetainLatestSearchCriteria(false);
			Utils.fluentWait(driver);
			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			Utils.fluentWait(driver);
			configurationPage.logOut();
			Utils.fluentWait(driver);

			Log.message("2. Set the required settings");

			//3. Login to the Test vault
			//---------------------------
			LoginPage loginPage = new LoginPage(driver);
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);

			Log.message("3. Login to the Test Vault.");

			//4. Perfrom the Search for a specific object type
			//-------------------------------------------------
			homePage.searchPanel.search(dataPool.get("SearchString"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			Log.message("4. Perfrom the Search for a specific object type");

			//5. Close and reaunch the browser
			//-----------------------------------
			homePage.taskPanel.clickItem("Log Out");
			Utils.fluentWait(driver);
			driver.quit();
			driver = WebDriverUtils.getDriver();

			Log.message("5. Close and reaunch the browser");

			//6. Login to the vault
			//----------------------
			Utils.fluentWait(driver);
			homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("6. Login to the vault");

			//Verification: To verify if the previously selected search options are not preserved
			//------------------------------------------------------------------------------------
			if(!homePage.searchPanel.getSearchWord().trim().equals(dataPool.get("SearchString")) && !homePage.searchPanel.getSearchType().trim().equals(dataPool.get("SearchType")))
				Log.pass("Test Case Passed. The Search string changes were not preserved as expected.");
			else
				Log.fail("Test Case Failed. The Search string changes were preserved.", driver);

			driver.get(configURL);
			Utils.fluentWait(driver);
			configurationPage = new ConfigurationPage(driver);
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);
			Utils.fluentWait(driver);
			configurationPanel = new  ConfigurationPanel(driver);
			configurationPanel.setRetainLatestSearchCriteria(true);
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
	 * 39.1.67: Reset All button - Search criteria (Default Setting)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "Password","ResetAndRetainSearchCriteria"}, 
			description = "Reset All button - Search criteria (Default Setting)")
	public void SprintTest39_1_67(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			//1. Select any option in the Search criteria
			//--------------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			if(homePage.searchPanel.getSearchType().equals(dataPool.get("SearchType")))
				throw new SkipException("Invalid Test Data. The Search type is already set.");

			homePage.searchPanel.setSearchType(dataPool.get("SearchType"));
			Utils.fluentWait(driver);
			homePage.searchPanel.clickSearch();
			Utils.fluentWait(driver);

			Log.message("1. Select any option in the Search criteria");

			//2. Click the Reset Button and confirm the action
			//-------------------------------------------------
			Utils.fluentWait(driver);
			homePage.searchPanel.resetAll();
			Utils.fluentWait(driver);

			Log.message("2. Click the Reset Button and confirm the action");

			//Verification: To verify if the previously selected typeis  not preserved
			//-------------------------------------------------------------------------
			if(homePage.searchPanel.getSearchType().trim().equals(dataPool.get("SearchType")))
				Log.pass("Test Case Passed. The Search option changes was not preserved as expected.");
			else
				Log.fail("Test Case Failed. The Search option changes was preserved.", driver);

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
	 * 39.1.68: Reset All button - Search criteria (No Retaining the last selection made by user Setting)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "Password","ResetAndRetainSearchCriteria"}, 
			description = "Reset All button - Search criteria (No Retaining the last selection made by user Setting)")
	public void SprintTest39_1_68(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to Configuration
			//--------------------------
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			Log.message("1. Logged in to Configuiration");

			//2. Set the required settings
			//----------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);
			Utils.fluentWait(driver);

			ConfigurationPanel configurationPanel = new  ConfigurationPanel(driver);
			configurationPanel.setRetainLatestSearchCriteria(false);

			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");
			configurationPage.logOut();

			Log.message("2. Set the required settings");

			//3. Login to the Test vault
			//---------------------------
			LoginPage loginPage = new LoginPage(driver);
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);

			Log.message("3. Login to the Test Vault.");

			//4. Select any option in the Search criteria
			//--------------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			if(homePage.searchPanel.getSearchType().equals(dataPool.get("SearchType")))
				throw new SkipException("Invalid Test Data. The Search type is already set.");

			homePage.searchPanel.setSearchType(dataPool.get("SearchType"));
			Utils.fluentWait(driver);
			homePage.searchPanel.clickSearch();
			Utils.fluentWait(driver);

			Log.message("4. Select any option in the Search criteria");

			//5. Click the Reset Button and confirm the action
			//-------------------------------------------------
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			homePage.searchPanel.resetAll();
			Utils.fluentWait(driver);

			Log.message("5. Click the Reset Button and confirm the action");

			//Verification: To verify if the previously selected typeis  not preserved
			//-------------------------------------------------------------------------
			if(homePage.searchPanel.getSearchType().trim().equals(dataPool.get("ExpectedSearchType")))
				Log.pass("Test Case Passed. The Search option changes was not preserved as expected.");
			else
				Log.fail("Test Case Failed. The Search option changes was preserved.", driver);

			driver.get(configURL);
			Utils.fluentWait(driver);
			configurationPage = new ConfigurationPage(driver);
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);
			Utils.fluentWait(driver);
			configurationPanel = new  ConfigurationPanel(driver);
			configurationPanel.setRetainLatestSearchCriteria(true);
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
	 * 39.1.69: Reset All button - Search criteria (Force the following selection)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "ResetAndRetainSearchCriteria"}, 
			description = "Reset All button - Search criteria (Force the following selection)")
	public void SprintTest39_1_69(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to Configuration
			//--------------------------
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);
			Utils.fluentWait(driver);

			Log.message("1. Logged in to Configuiration");

			//2. Set the required settings
			//----------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);
			Utils.fluentWait(driver);

			ConfigurationPanel configurationPanel = new  ConfigurationPanel(driver);
			configurationPanel.setDefaultSearchCriteria(dataPool.get("ExpectedSearchType"));

			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");
			configurationPage.logOut();

			Log.message("2. Set the required settings");

			//3. Login to the Test vault
			//---------------------------
			LoginPage loginPage = new LoginPage(driver);
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);
			Utils.fluentWait(driver);

			Log.message("3. Login to the Test Vault.");

			//4. Select any option in the Search criteria
			//--------------------------------------------
			Utils.fluentWait(driver);
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			WebElement field = driver.findElement(By.cssSelector("input[id='fbsearchObjectType_input']"));

			//Verification: To verify if the Object type field is disabled
			//-------------------------------------------------------------
			if(!field.isEnabled())
				Log.pass("Test Case Passed. The Search option changes was not preserved as expected.");
			else
				Log.fail("Test Case Failed. The Search option changes was preserved.", driver);

			driver.get(configURL);
			Utils.fluentWait(driver);
			configurationPage = new ConfigurationPage(driver);
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);
			Utils.fluentWait(driver);
			configurationPanel = new  ConfigurationPanel(driver);
			configurationPanel.setFoceFollowingSelection(false);
			configurationPanel.setRetainLatestSearchCriteria(true);
			configurationPanel.setRetainLatestSearchSettings(true);
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
	 * 39.1.70: Reset All button - Search setting (Default setting)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "Password","ResetAndRetainSearchCriteria"}, 
			description = "Reset All button - Search setting (Default setting)")
	public void SprintTest39_1_70(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);	

			Utils.fluentWait(driver);

			//1. Select any option in the Search criteria
			//--------------------------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);

			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			if(homePage.searchPanel.getSearchInType().equals(dataPool.get("SearchInType")))
				throw new SkipException("Invalid Test Data. The Search type is already set.");

			Utils.fluentWait(driver);
			homePage.searchPanel.setSearchInType(dataPool.get("SearchInType"));
			Utils.fluentWait(driver);
			homePage.searchPanel.setSearchWord(dataPool.get("SearchInType"));
			Utils.fluentWait(driver);
			homePage.searchPanel.clickSearch();
			Utils.fluentWait(driver);

			Log.message("1. Select any option in the Search criteria");

			//2. Click the Reset Button and confirm the action
			//-------------------------------------------------
			Utils.fluentWait(driver);
			homePage.searchPanel.resetAll();
			Utils.fluentWait(driver);

			Log.message("2. Click the Reset Button and confirm the action");

			//Verification: To verify if the previously selected type is not preserved
			//-------------------------------------------------------------------------
			if(homePage.searchPanel.getSearchInType().trim().equals(dataPool.get("SearchInType")))
				Log.pass("Test Case Passed. The Search option changes was not preserved as expected.");
			else
				Log.fail("Test Case Failed. The Search option changes was preserved.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {			
			if(!(driver.equals(null) && homePage.equals(null))){
				try
				{
					homePage.searchPanel.setSearchInType("Search in metadata and file contents");
					homePage.searchPanel.setSearchWord("Search in metadata and file contents");
					homePage.searchPanel.clickSearch();
					Utils.fluentWait(driver);
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
	 * 39.1.83: Reset All button - Search Word
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "Password","ResetAndRetainSearchCriteria"}, 
			description = "Reset All button - Search Word")
	public void SprintTest39_1_83(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {


			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);			

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			//1. Set the Search string and perfrom a search
			//----------------------------------------------
			homePage.searchPanel.search(dataPool.get("ObjectName"), dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			Log.message("1. Set the Search string and perfrom a search.");

			//2. Click the Reset All button and confirm the action
			//------------------------------------------------------
			homePage.searchPanel.resetAll();
			Utils.fluentWait(driver);

			Log.message("2. Click the Reset All button and confirm the action.");

			//Verification: To verify if the Search keyword is removed
			//---------------------------------------------------------
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			if(homePage.searchPanel.getSearchWord().equals(""))
				Log.pass("Test Case Passed. The Reset All button works as expected.");
			else
				Log.fail("Test Case Failed. The Reset All button does not work as expected. The Search keyword was not cleared.", driver);

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
	 * 39.1.84: Reset All button - Search Option
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "Password","ResetAndRetainSearchCriteria"}, 
			description = "Reset All button - Search Option")
	public void SprintTest39_1_84(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			//1. Change the Search Option
			//----------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);
			homePage.searchPanel.setSearchOption(dataPool.get("SearchOption"));
			Utils.fluentWait(driver);

			Log.message("1. Change the Search Option");			

			//2. Set the Search string and perfrom a search
			//----------------------------------------------
			homePage.searchPanel.search(dataPool.get("ObjectName"), dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			Log.message("2. Set the Search string and perfrom a search.");

			//3. Click the Reset All button and confirm the action
			//------------------------------------------------------
			homePage.searchPanel.resetAll();
			Utils.fluentWait(driver);

			Log.message("3. Click the Reset All button and confirm the action.");

			//Verification: To verify if the Search option is reset
			//---------------------------------------------------------
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			if(homePage.searchPanel.getSelectedSearchOption().equals(dataPool.get("ExpectedSearchOption")))
				Log.pass("Test Case Passed. The Reset All button works as expected.");
			else
				Log.fail("Test Case Failed. The Reset All button does not work as expected. The Search option was not reset.", driver);

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
	 * 39.1.85: Reset All button - Search within this folder checkbox
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "Password","ResetAndRetainSearchCriteria"}, 
			description = "Reset All button - Search within this folder checkbox")
	public void SprintTest39_1_85(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			//1. navigate to any view or perfrom a search
			//--------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, dataPool.get("View"), "");
			Utils.fluentWait(driver);

			Log.message("1. navigate to any view or perfrom a search");

			//2. Click the Advanced Search link
			//----------------------------------
			Utils.fluentWait(driver);
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("2. Click the Advanced Search link");

			//3. Check the Search within this folder check box
			//--------------------------------------------------
			homePage.searchPanel.setSearchWithInThisFolder(true);

			Log.message("3. Check the Search within this folder check box");

			//4. Click the Search button
			//---------------------------
			homePage.searchPanel.clickSearch();
			Utils.fluentWait(driver);

			Log.message("4. Click the Search button");

			//5. Click the Reset All link and confirm the action
			//---------------------------------------------------
			homePage.searchPanel.resetAll();
			Utils.fluentWait(driver);

			Log.message("5. Click the Reset All link and confirm the action");

			//Verification: To verify if the Search within this folder checkbox is enabled
			//-----------------------------------------------------------------------------
			if(!homePage.searchPanel.getSearchWithInThisFolder())
				Log.pass("Test Case Passed. The search within this folder checkbox is unchecked.");
			else
				Log.fail("Test Case Failed. The search within this folder checkbox is still checked.", driver);

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
	 * 39.1.88: Working of the Default Search Setting (Configuration)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Search","ResetAndRetainSearchCriteria"}, 
			description = "Working of the Default Search Setting (Configuration)")
	public void SprintTest39_1_88(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to Configuration
			//--------------------------
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			Log.message("1. Logged in to Configuiration");

			//2. Set the required settings
			//----------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			ConfigurationPanel configurationPanel = new  ConfigurationPanel(driver);
			configurationPanel.setDefaultSearchSettings(dataPool.get("ExpectedSearchInType"));

			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");
			configurationPage.logOut();

			Log.message("2. Set the required settings");

			//3. Login to the Test vault
			//---------------------------
			Utils.fluentWait(driver);
			LoginPage loginPage = new LoginPage(driver);
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);
			Utils.fluentWait(driver);

			Log.message("3. Login to the Test Vault.");

			//Verification: To verify if the previously selected typeis  not preserved
			//-------------------------------------------------------------------------
			if(homePage.searchPanel.getSearchInType().trim().equals(dataPool.get("ExpectedSearchInType")))
				Log.pass("Test Case Passed. The Search option changes was not preserved as expected.");
			else
				Log.fail("Test Case Failed. The Search option changes was preserved.", driver);

			driver.get(configURL);
			Utils.fluentWait(driver);
			configurationPage = new ConfigurationPage(driver);
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);
			Utils.fluentWait(driver);
			configurationPanel = new  ConfigurationPanel(driver);
			configurationPanel.setRetainLatestSearchCriteria(true);
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
	 * 39.1.89: Reset All button - 'Retain last selection made by user'.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "Password","ResetAndRetainSearchCriteria"}, 
			description = "Reset All button - 'Retain last selection made by user'.")
	public void SprintTest39_1_89(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		HomePage homePage = null;
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to Configuration
			//--------------------------
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);
			Utils.fluentWait(driver);

			Log.message("1. Logged in to Configuiration");

			//2. Set the required settings
			//----------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);
			Utils.fluentWait(driver);

			ConfigurationPanel configurationPanel = new  ConfigurationPanel(driver);
			configurationPanel.setRetainLatestSearchSettings(true);

			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");
			configurationPage.logOut();

			Log.message("2. Set the required settings");

			//3. Login to the Test vault
			//---------------------------
			LoginPage loginPage = new LoginPage(driver);
			homePage = loginPage.loginToWebApplication(userName, password, testVault);

			Log.message("3. Login to the Test Vault.");

			//4. Select any option in the Search criteria
			//--------------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			if(homePage.searchPanel.getSearchInType().equals(dataPool.get("SearchInType")))
				throw new SkipException("Invalid Test Data. The Search type is already set.");

			Utils.fluentWait(driver);
			homePage.searchPanel.setSearchInType(dataPool.get("SearchInType"));
			Utils.fluentWait(driver);
			homePage.searchPanel.setSearchWord(dataPool.get("SearchInType"));
			Utils.fluentWait(driver);
			homePage.searchPanel.clickSearch();
			Utils.fluentWait(driver);

			Log.message("4. Select any option in the Search criteria");

			//5. Click the Reset Button and confirm the action
			//-------------------------------------------------
			Utils.fluentWait(driver);
			homePage.searchPanel.resetAll();
			Utils.fluentWait(driver);

			Log.message("5. Click the Reset Button and confirm the action");

			//Verification: To verify if the previously selected typeis  not preserved
			//-------------------------------------------------------------------------
			if(homePage.searchPanel.getSearchInType().trim().equals(dataPool.get("SearchInType")))
				Log.pass("Test Case Passed. The Search option changes was not preserved as expected.");
			else
				Log.fail("Test Case Failed. The Search option changes was preserved.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			if(!(driver.equals(null) && homePage.equals(null))){
				try
				{
					homePage.searchPanel.setSearchInType("Search in metadata and file contents");
					Utils.fluentWait(driver);
					homePage.searchPanel.clickSearch();
					Utils.fluentWait(driver);
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
	 * 39.1.90: Reset All button - Search setting (set Default Search setting)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "ResetAndRetainSearchCriteria"}, 
			description = "Reset All button - Search setting (set Default Search setting)")
	public void SprintTest39_1_90(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;
		ConfigurationPage configurationPage = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to Configuration
			//--------------------------
			configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			Log.message("1. Logged in to Configuiration");

			//2. Set the required settings
			//----------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);
			Utils.fluentWait(driver);

			ConfigurationPanel configurationPanel = new  ConfigurationPanel(driver);
			configurationPanel.setDefaultSearchSettings(dataPool.get("ExpectedSearchInType"));

			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");
			configurationPage.logOut();

			Log.message("2. Set the required settings");

			//3. Login to the Test vault
			//---------------------------
			LoginPage loginPage = new LoginPage(driver);
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);
			Utils.fluentWait(driver);

			Log.message("3. Login to the Test Vault.");

			//4. Select any option in the Search criteria
			//--------------------------------------------
			Utils.fluentWait(driver);
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			if(homePage.searchPanel.getSearchType().equals(dataPool.get("SearchInType")))
				throw new SkipException("Invalid Test Data. The Search type is already set.");

			homePage.searchPanel.setSearchInType(dataPool.get("SearchInType"));
			Utils.fluentWait(driver);
			homePage.searchPanel.clickSearch();
			Utils.fluentWait(driver);

			Log.message("4. Select any option in the Search criteria");

			//5. Click the Reset Button and confirm the action
			//-------------------------------------------------
			Utils.fluentWait(driver);
			homePage.searchPanel.resetAll();
			Utils.fluentWait(driver);

			Log.message("5. Click the Reset Button and confirm the action");

			//Verification: To verify if the previously selected typeis  not preserved
			//-------------------------------------------------------------------------
			if(homePage.searchPanel.getSearchInType().trim().equals(dataPool.get("ExpectedSearchInType")))
				Log.pass("Test Case Passed. The Search option changes was not preserved as expected.");
			else
				Log.fail("Test Case Failed. The Search option changes was preserved.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {

			if(!(driver.equals(null) && configurationPage.equals(null))){
				try
				{					
					driver.get(configURL);
					Utils.fluentWait(driver);
					configurationPage = new ConfigurationPage(driver);
					configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault);
					Utils.fluentWait(driver);
					ConfigurationPanel configurationPanel = new  ConfigurationPanel(driver);
					configurationPanel.setRetainLatestSearchCriteria(true);
					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");

					configurationPage.logOut();

					LoginPage loginPage = new LoginPage(driver);
					HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);
					Utils.fluentWait(driver);
					homePage.searchPanel.setSearchInType("Search in metadata and file contents");
					Utils.fluentWait(driver);
					homePage.searchPanel.clickSearch();
					Utils.fluentWait(driver);
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
	 * 105.5.6 : Check if multiple search conditions are reset when clicking 'Reset All' link
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint100", "Comment","ResetAndRetainSearchCriteria"}, 
			description = "Check if multiple search conditions are reset when clicking 'Reset All' link.")
	public void SprintTest105_5_6(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Set the advanced search condition using multiple property
			//-------------------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property1"), dataPool.get("Condition1"), dataPool.get("Value1"));
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property2"), dataPool.get("Condition2"), dataPool.get("Value2"), 2);
			homePage.searchPanel.search("", "Search all objects");

			Log.message("2. Set the advanced search condition using multiple property.");

			//3. click Reset All and confirm the action
			//------------------------------------------
			homePage.searchPanel.resetAll();
			Utils.fluentWait(driver);

			Log.message("3. click Reset All and confirm the action.");

			//Verification: To verify if the conditions are cleared
			//------------------------------------------------------
			if(homePage.searchPanel.getAdditionalConditions().equals(""))
				Log.pass("Test Case Passed. Search using multi-line text property works as expected.");
			else
				Log.fail("Test Case Failed. Search using multi-line text property works as expected.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}


}


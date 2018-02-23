package MFClient.Tests.ExternalConnectors;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.Utils;
import genericLibrary.WebDriverUtils;

import java.util.HashMap;
import java.util.List;
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

import MFClient.Pages.ConfigurationPage;
import MFClient.Pages.HomePage;
import MFClient.Pages.LoginPage;
import MFClient.Wrappers.Caption;
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class ExternalStorageSearch {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String configURL = null;
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
	public static String extnViewName = null;
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
			configURL = xmlParameters.getParameter("ConfigurationURL");
			userName = xmlParameters.getParameter("UserName");
			userFullName = xmlParameters.getParameter("UserFullName");
			password = xmlParameters.getParameter("Password");
			windowsUserName = xmlParameters.getParameter("WindowsUserName");
			windowsUserFullName = xmlParameters.getParameter("WindowsUserFullName");
			windowsPassword = xmlParameters.getParameter("WindowsPassword");
			domainName = xmlParameters.getParameter("DomainName");
			testVault = xmlParameters.getParameter("VaultName");
			extnViewName = xmlParameters.getParameter("ExternalViewName");
			className = this.getClass().getSimpleName().toString().trim();
			if (xmlParameters.getParameter("driverType").equalsIgnoreCase("IE"))
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim() + xmlParameters.getParameter("driverVersion").trim();
			else
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim();

			Utility.restoreTestVault();
			Utility.configureUsers(xlTestDataWorkBook);
			Utility.setExternalRepository(extnViewName, className);
			String extRepoAppName = xmlParameters.getParameter("ExternalRepositoryApp");
			Utility.installVaultApplication(extRepoAppName);
			Utility.setFileShareNamedValue(className);
			Utility.takeVaultOfflineAndBringOnline(testVault, "OfflineAndOnline");

			if (!Utility.isExternalViewExists(extnViewName))
				throw new Exception("isExternalViewExists : " + extnViewName + " is not exist in the home view.");

		} //End try

		catch(Exception e) {
			if (e instanceof SkipException || e.getMessage().contains("isExternalViewExists")) 
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

			Utility.clearExternalRepository(className);//Clears the external repository created for this class
			Utility.destroyUsers(xlTestDataWorkBook);//Destroys the user in MFServer
			Utility.destroyTestVault();//Destroys the Vault in MFServer

		}//End try

		catch(Exception e){
			throw e;
		}//End Catch
	}//End cleanApp

	/**
	 *  ExtnStorageSearch_43069: Verify traditional search with out entering any values
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Verify traditional search with out entering any values")
	public void ExtnStorageSearch_43069(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launches login page and logging in

			//Step-1 : Perform traditional search without entering any values
			//---------------------------------------------------------------
			homePage.searchPanel.clickSearch();//Search button is clicked in the view

			Log.message("1. Clicked the search button with out entering any values.", driver);

			//Check if all the internal object types displayed in the search view
			//-------------------------------------------------------------------
			String[] objectTypes = dataPool.get("ObjectTypes").split("\n");
			String result = "";

			for (int i = 0; i < objectTypes.length; i++)
			{
				if (!homePage.listView.isGroupHeaderAvailable(objectTypes[i]))
					if (result.equals(""))
						result =  objectTypes[i];
					else
						result += ", " + objectTypes[i];
			}

			//Verification: If All internal objects available in M-files server
			//-----------------------------------------------------------------
			if (result.equals(""))
				Log.pass("Test Case passed. All internal objects available in M-files server is displayed in the traditional search view with out enetering any values.");
			else
				Log.fail("Test case failed. All internal objects available in M-files server is not displayed in the traditional search view with out enetering any values. Additional info. : Following object types not listed in the search view - " + result + ".", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End ExtnStorageSearch_43069

	/**
	 *  ExtnStorageSearch_43070: Verify search in right pane with out entering any values
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Verify search in right pane with out entering any values")
	public void ExtnStorageSearch_43070(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConfigurationPage configPage = null;

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			configPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launches login page and logging into the configuration page

			//1 : Navigate to the controls in the configuration page for the test vault
			//-------------------------------------------------------------------------
			configPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);

			Log.message("1. Navigated to '"+Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value+"' in Configuration Page", driver);

			//2 : Enable search in right pane option in configuration page
			//------------------------------------------------------------
			configPage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Show.Value);

			if (!configPage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Show.Value))
				throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Show.Value);

			configPage.clickSaveButton();//Clicks the save button

			Log.message("2. 'Search in right pane' is enabled and settings are saved in the configuration page.", driver);

			//Step-4 : Logout from configuration page
			//---------------------------------------
			configPage.clickOKBtnOnSaveDialog();//Clicks OK button in the MFiles Dialog
			configPage.clickLogOut(); //Logs out from the Configuration page

			Log.message("3. Logged out from Configuration Webpage");

			//Login to MFiles web access
			//--------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches login page and logging in

			Log.message("--------------------------------Steps---------------------------------------------");
			Log.message("1. Logged into M-Files Web access.", driver);

			//Step-1 : Perform traditional search without entering any values in the right pane
			//---------------------------------------------------------------------------------
			homePage.searchPanel.clickRightPaneSearchButton();//Search button is clicked in the view

			Log.message("2. Clicked the search button with out entering any values in the right pane search.", driver);

			//Check if all the internal object types displayed in the search view
			//-------------------------------------------------------------------
			String[] objectTypes = dataPool.get("ObjectTypes").split("\n");
			String result = "";

			for (int i = 0; i < objectTypes.length; i++)
			{
				if (!homePage.listView.isGroupHeaderAvailable(objectTypes[i]))
					if (result.equals(""))
						result =  objectTypes[i];
					else
						result += ", " + objectTypes[i];
			}

			//Verification: If All internal objects available in M-files server
			//-----------------------------------------------------------------
			if (result.equals(""))
				Log.pass("Test Case passed. All internal objects available in M-files server is displayed in search in right pane with out enetering any values.");
			else
				Log.fail("Test case failed. All internal objects available in M-files server is not displayed in search in right pane with out enetering any values. Additional info. : Following object types not listed in the search view - " + result + ".", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			if (driver != null)
			{
				try
				{
					driver.get(configURL);//Launches the configuration page
					configPage = LoginPage.launchDriverAndLoginToConfig(driver, false); //Launches login page and logging into the configuration page
					configPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);
					configPage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Hide.Value);
					if (!configPage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Hide.Value))
						throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Show.Value);
					configPage.clickSaveButton();//Clicks the save button
					configPage.clickOKBtnOnSaveDialog();//Clicks OK button in the MFiles Dialog
				}
				catch(Exception e0){Log.exception(e0, driver);}
			}
			Utility.quitDriver(driver);
		}//End finally

	}//End ExtnStorageSearch_43070

	/**
	 *  ExtnStorageSearch_43071: Check search for managed objects in traditional search
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Check search for managed objects in traditional search")
	public void ExtnStorageSearch_43071(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launches login page and logging in

			//Step-1 : Perform traditional search without entering any values
			//---------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, "", dataPool.get("SearchWord"));//Performs the search

			Log.message("1. Navigated to the search view using traditional search with the searchword('" + dataPool.get("SearchWord") + "').", driver);

			//Check If object matches the search keyword is listed in the list view
			//---------------------------------------------------------------------
			String[] availableObjects = homePage.listView.getAllItemNames();
			String result = "";
			for (int i = 0; i < availableObjects.length; i++)
				if (!availableObjects[i].toLowerCase().contains(dataPool.get("SearchWord").toLowerCase()))
					result += "'" + availableObjects[i] + "' not matches with the search word('" + dataPool.get("SearchWord") + "') also listed in the search view.";

			//Verification: Managed objects lised as expected in traditional search
			//---------------------------------------------------------------------
			if (result.equals(""))
				Log.pass("Test Case passed. Managed objects available in M-files server is displayed while performing traditional search with the search word.");
			else
				Log.fail("Test case failed. Managed objects available in M-files server is not displayed as expected while performing the traditional search with the search word. Additional info. : Following object types is listed which not matches the search key word in the search view - " + result + ".", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End ExtnStorageSearch_43071

	/**
	 *  ExtnStorageSearch_43072: Check search for un-managed objects in traditional search
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Check search for un-managed objects in traditional search")
	public void ExtnStorageSearch_43072(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launches login page and logging in

			//Step-1 : Perform traditional search without entering any values
			//---------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, "", dataPool.get("SearchWord"));//Performs the search

			Log.message("1. Navigated to the search view using traditional search with the searchword('" + dataPool.get("SearchWord") + "').", driver);

			//Check If object matches the search keyword is listed in the list view
			//---------------------------------------------------------------------
			String[] availableObjects = homePage.listView.getAllItemNames();
			String[] expectedObjects = dataPool.get("Objects").split("\n");

			boolean objExists = false;
			String result = "";
			for (int i = 0; i < expectedObjects.length; i++)
			{
				for (int j = 0; j < availableObjects.length; j++)
					if (availableObjects[j].equalsIgnoreCase(expectedObjects[i]))
						objExists = true;

				if (!objExists)
					result += "'" + expectedObjects[i] + "' is not listed in the list view.";
			}
			//Verification: Unmanaged objects lised as expected in traditional search
			//-----------------------------------------------------------------------
			if (result.equals(""))
				Log.pass("Test Case passed. Managed & Unmanaged objects available in M-files server is displayed while performing traditional search with the search word.");
			else
				Log.fail("Test case failed. Managed & Unmanaged objects available in M-files server is not displayed as expected while performing the traditional search with the search word. Additional info. : Following managed/unmanaged objects not listed in search view - " + result + ".", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End ExtnStorageSearch_43072

	/**
	 *  ExtnStorageSearch_43073: Check search for managed objects in right pane search
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Check search for managed objects in right pane search")
	public void ExtnStorageSearch_43073(HashMap<String,String> dataValues, String driverType) throws Exception {

		ConfigurationPage configPage = null;

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			configPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launches login page and logging into the configuration page

			//1 : Navigate to the controls in the configuration page for the test vault
			//-------------------------------------------------------------------------
			configPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);

			Log.message("1. Navigated to '"+Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value+"' in Configuration Page", driver);

			//2 : Enable search in right pane option in configuration page
			//------------------------------------------------------------
			configPage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Show.Value);

			if (!configPage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Show.Value))
				throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Show.Value);

			configPage.clickSaveButton();//Clicks the save button

			Log.message("2. 'Search in right pane' is enabled and settings are saved in the configuration page.", driver);

			//Step-4 : Logout from configuration page
			//---------------------------------------
			configPage.clickOKBtnOnSaveDialog();//Clicks OK button in the MFiles Dialog
			configPage.clickLogOut(); //Logs out from the Configuration page

			Log.message("3. Logged out from Configuration Webpage");

			//Login to MFiles web access
			//--------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches login page and logging in

			Log.message("--------------------------------Steps---------------------------------------------");
			Log.message("1. Logged into M-Files Web access.", driver);

			//Step-1 : Perform traditional search without entering any values
			//---------------------------------------------------------------
			homePage.searchPanel.setSearchWordInRightPane(dataPool.get("SearchWord"), false);//Sets the search word in the right pane
			homePage.searchPanel.clickRightPaneSearchButton();//Clicks the search button in the right pane

			Log.message("1. Navigated to the search view using traditional search with the searchword('" + dataPool.get("SearchWord") + "').", driver);

			//Check If object matches the search keyword is listed in the list view
			//---------------------------------------------------------------------
			String[] availableObjects = homePage.listView.getAllItemNames();
			String result = "";
			for (int i = 0; i < availableObjects.length; i++)
				if (!availableObjects[i].toLowerCase().contains(dataPool.get("SearchWord").toLowerCase()))
					result += "'" + availableObjects[i] + "' not matches with the search word('" + dataPool.get("SearchWord") + "') also listed in the search view.";

			//Verification: Managed objects lised as expected in traditional search
			//---------------------------------------------------------------------
			if (result.equals(""))
				Log.pass("Test Case passed. Managed objects available in M-files server is displayed while performing traditional search with the search word.");
			else
				Log.fail("Test case failed. Managed objects available in M-files server is not displayed as expected while performing the traditional search with the search word. Additional info. : Following object types is listed which not matches the search key word in the search view - " + result + ".", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			if (driver != null)
			{
				try
				{
					driver.get(configURL);//Launches the configuration page
					configPage = LoginPage.launchDriverAndLoginToConfig(driver, false); //Launches login page and logging into the configuration page
					configPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);
					configPage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Hide.Value);
					if (!configPage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Hide.Value))
						throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Show.Value);
					configPage.clickSaveButton();//Clicks the save button
					configPage.clickOKBtnOnSaveDialog();//Clicks OK button in the MFiles Dialog
				}
				catch(Exception e0){Log.exception(e0, driver);}
			}
			Utility.quitDriver(driver);
		}//End finally

	}//End ExtnStorageSearch_43073

	/**
	 *  ExtnStorageSearch_43074: Check search for unmanaged objects in right pane search
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Check search for unmanaged objects in right pane search")
	public void ExtnStorageSearch_43074(HashMap<String,String> dataValues, String driverType) throws Exception {

		ConfigurationPage configPage = null;

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			configPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launches login page and logging into the configuration page

			//1 : Navigate to the controls in the configuration page for the test vault
			//-------------------------------------------------------------------------
			configPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);

			Log.message("1. Navigated to '"+Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value+"' in Configuration Page", driver);

			//2 : Enable search in right pane option in configuration page
			//------------------------------------------------------------
			configPage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Show.Value);

			if (!configPage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Show.Value))
				throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Show.Value);

			configPage.clickSaveButton();//Clicks the save button

			Log.message("2. 'Search in right pane' is enabled and settings are saved in the configuration page.", driver);

			//Step-4 : Logout from configuration page
			//---------------------------------------
			configPage.clickOKBtnOnSaveDialog();//Clicks OK button in the MFiles Dialog
			configPage.clickLogOut(); //Logs out from the Configuration page

			Log.message("3. Logged out from Configuration Webpage");

			//Login to MFiles web access
			//--------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches login page and logging in

			Log.message("--------------------------------Steps---------------------------------------------");
			Log.message("1. Logged into M-Files Web access.", driver);

			//Step-1 : Perform traditional search without entering any values
			//---------------------------------------------------------------
			homePage.searchPanel.setSearchWordInRightPane(dataPool.get("SearchWord"), false);//Sets the search word in the right pane
			homePage.searchPanel.clickRightPaneSearchButton();//Clicks the search button in the right pane

			Log.message("1. Navigated to the search view using right pane search with the searchword('" + dataPool.get("SearchWord") + "').", driver);

			//Check If object matches the search keyword is listed in the list view
			//---------------------------------------------------------------------
			String[] availableObjects = homePage.listView.getAllItemNames();
			String[] expectedObjects = dataPool.get("Objects").split("\n");

			boolean objExists = false;
			String result = "";
			for (int i = 0; i < expectedObjects.length; i++)
			{
				for (int j = 0; j < availableObjects.length; j++)
					if (availableObjects[j].equalsIgnoreCase(expectedObjects[i]))
						objExists = true;

				if (!objExists)
					result += "'" + expectedObjects[i] + "' is not listed in the list view.";
			}
			//Verification: Unmanaged objects lised as expected in traditional search
			//-----------------------------------------------------------------------
			if (result.equals(""))
				Log.pass("Test Case passed. Managed & Unmanaged objects available in M-files server is displayed while performing right pane search with the search word.");
			else
				Log.fail("Test case failed. Managed & Unmanaged objects available in M-files server is not displayed as expected while performing the right pane search with the search word. Additional info. : Following managed/unmanaged objects not listed in search view - " + result + ".", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			if (driver != null)
			{
				try
				{
					driver.get(configURL);//Launches the configuration page
					configPage = LoginPage.launchDriverAndLoginToConfig(driver, false); //Launches login page and logging into the configuration page
					configPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);
					configPage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Hide.Value);
					if (!configPage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Hide.Value))
						throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Show.Value);
					configPage.clickSaveButton();//Clicks the save button
					configPage.clickOKBtnOnSaveDialog();//Clicks OK button in the MFiles Dialog
				}
				catch(Exception e0){Log.exception(e0, driver);}
			}
			Utility.quitDriver(driver);
		}//End finally

	}//End ExtnStorageSearch_43074

	/**
	 *  ExtnStorageSearch_43079: Verify the list of search criteria present in traditional search
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Verify the list of search criteria present in traditional search")
	public void ExtnStorageSearch_43079(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launches login page and logging in

			//Step-1: Get the list of search criteria available in the traditional search
			//---------------------------------------------------------------------------
			List<String> availableCriterias = homePage.searchPanel.getSearchCriterias();//Gets the search criterias

			Log.message("1. Available search criteria options is got from the Traditional search.", driver);

			//Check the search criterias in the traditional search
			//----------------------------------------------------
			String[] expectedCriterias = dataPool.get("SearchCriterias").split("\n");
			boolean criteriaExists = false;
			String result = "";

			for (int i = 0; i < expectedCriterias.length; i++)
			{
				for (int j = 0; j < availableCriterias.size(); j++)
					if (expectedCriterias[i].toLowerCase().contains(availableCriterias.get(j).toLowerCase()))
						criteriaExists = true;

				if (!criteriaExists)
					result += "'" + expectedCriterias[i] + "' is not avaialble in traditional search.";
			}

			//Verification: If search criterias displayed as expected
			//-------------------------------------------------------
			if (result.equals(""))
				Log.pass("Test case passed. Search criterias displayed as expected in the traditional search.");
			else
				Log.fail("Test case failed. Search criterias is not displayed as expected in the traditional search. Additional info. :- "+result, driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End ExtnStorageSearch_43079

	/**
	 *  ExtnStorageSearch_43080: Verify the list of search criteria present in right pane search
	 */
	@Test(groups = {}, description = "Verify the list of search criteria present in right pane search")
	public void ExtnStorageSearch_43080(String driverType) throws Exception {

		driver = null;
		ConfigurationPage configPage = null;

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			configPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launches login page and logging into the configuration page

			//1 : Navigate to the controls in the configuration page for the test vault
			//-------------------------------------------------------------------------
			configPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);

			Log.message("1. Navigated to '"+Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value+"' in Configuration Page", driver);

			//2 : Enable search in right pane option in configuration page
			//------------------------------------------------------------
			configPage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Show.Value);

			if (!configPage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Show.Value))
				throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Show.Value);

			configPage.clickSaveButton();//Clicks the save button

			Log.message("2. 'Search in right pane' is enabled and settings are saved in the configuration page.", driver);

			//Step-4 : Logout from configuration page
			//---------------------------------------
			configPage.clickOKBtnOnSaveDialog();//Clicks OK button in the MFiles Dialog
			configPage.clickLogOut(); //Logs out from the Configuration page

			Log.message("3. Logged out from Configuration Webpage");

			//Login to MFiles web access
			//--------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches login page and logging in

			Log.message("--------------------------------Steps---------------------------------------------");
			Log.message("1. Logged into M-Files Web access.", driver);

			//Verification: If search criterias displayed is not displayed
			//-------------------------------------------------------
			if (homePage.searchPanel.isSearchCriteriasDisplayedInRightPane())
				Log.pass("Test case passed. Search criterias is not displayed as expected in the right pane search.");
			else
				Log.fail("Test case failed. Search criterias is displayed in the right pane search.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			if (driver != null)
			{
				try
				{
					driver.get(configURL);//Launches the configuration page
					configPage = LoginPage.launchDriverAndLoginToConfig(driver, false); //Launches login page and logging into the configuration page
					configPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);
					configPage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Hide.Value);
					if (!configPage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Hide.Value))
						throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Show.Value);
					configPage.clickSaveButton();//Clicks the save button
					configPage.clickOKBtnOnSaveDialog();//Clicks OK button in the MFiles Dialog
				}
				catch(Exception e0){Log.exception(e0, driver);}
			}
			Utility.quitDriver(driver);
		}//End finally

	}//End ExtnStorageSearch_43080

	/**
	 *  ExtnStorageSearch_43081: Search objects based on "search in metadata" in traditional search
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Search objects based on \"search in metadata\" in traditional search")
	public void ExtnStorageSearch_43081(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			homePage = LoginPage.launchDriverAndLogin(driver, true); //Launches login page and logging in

			//Step-1: Set the search type in the traditioanl search
			//-----------------------------------------------------
			homePage.searchPanel.setSearchInType(dataPool.get("SearchType"));//Sets the search in type

			Log.message("1. Search type - '" + dataPool.get("SearchType") + "' is set in the traditional search.", driver);

			//Step-2: Enter the unmanaged object keyword
			//-------------------------------------------
			homePage.searchPanel.setSearchWord(dataPool.get("SearchWord"));//Sets the searchword in the traditional search

			Log.message("2. Search word - '" + dataPool.get("SearchWord") + "' is set in the traditional search.", driver);

			//Step-3: Click the search button
			//-------------------------------
			homePage.searchPanel.clickSearchBtn(driver);//Clicks the search button

			Log.message("3. Search button is clicked in the view.", driver);

			//Verification if search objects is present in the search view
			//------------------------------------------------------------
			if (homePage.listView.isItemExists(dataPool.get("ObjectName")))
				Log.pass("Test case passed. Search objects based on \"search in metadata\" in traditional search is working as expected.");
			else
				Log.fail("Test case failed. Search objects based on \"search in metadata\" in traditional search is not working as expected. Additona info.: '" + dataPool.get("ObjectName") + "' is not exist in the search view", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			if (driver != null)
			{
				try
				{
					homePage.searchPanel.setSearchInType("Search in metadata and file contents");//Resets the search in type to default
					homePage.searchPanel.clickSearchBtn(driver);
				}
				catch(Exception e0){Log.exception(e0, driver);}
			}
			Utility.quitDriver(driver);
		}//End finally

	}//End ExtnStorageSearch_43081

	/**
	 *  ExtnStorageSearch_43082: Search objects based on "search in file contents" in traditional search
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Search objects based on \"search in file contents\" in traditional search")
	public void ExtnStorageSearch_43082(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		HomePage homePage = null;
		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			homePage = LoginPage.launchDriverAndLogin(driver, true); //Launches login page and logging in

			//Step-1: Set the search type in the traditioanl search
			//-----------------------------------------------------
			homePage.searchPanel.setSearchInType(dataPool.get("SearchType"));//Sets the search in type

			Log.message("1. Search type - '" + dataPool.get("SearchType") + "' is set in the traditional search.", driver);

			//Step-2: Enter the unmanaged object keyword
			//-------------------------------------------
			homePage.searchPanel.setSearchWord(dataPool.get("SearchWord"));//Sets the searchword in the traditional search

			Log.message("2. Search word - '" + dataPool.get("SearchWord") + "' is set in the traditional search.", driver);

			//Step-3: Click the search button
			//-------------------------------
			homePage.searchPanel.clickSearchBtn(driver);//Clicks the search button

			Log.message("3. Search button is clicked in the view.", driver);

			//Verification if search objects is present in the search view
			//------------------------------------------------------------
			if (homePage.listView.isItemExists(dataPool.get("ObjectName")))
				Log.pass("Test case passed. Search objects based on \"search in file contents\" in traditional search is working as expected.");
			else
				Log.fail("Test case failed. Search objects based on \"search in file contents\" in traditional search is not working as expected. Additona info.: '" + dataPool.get("ObjectName") + "' is not exist in the search view", driver);


		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			if (driver != null)
			{
				try
				{
					homePage.searchPanel.setSearchInType("Search in metadata and file contents");//Resets the search in type to default
					homePage.searchPanel.clickSearchBtn(driver);
				}
				catch(Exception e0){Log.exception(e0, driver);}
			}
			Utility.quitDriver(driver);
		}//End finally

	}//End ExtnStorageSearch_43082

	/**
	 *  ExtnStorageSearch_43083: Search obejcts based on "search in metadata and file contents" keyword in traditional search
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Search obejcts based on \"search in metadata and file contents\" keyword in traditional search")
	public void ExtnStorageSearch_43083(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		HomePage homePage = null;
		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			homePage = LoginPage.launchDriverAndLogin(driver, true); //Launches login page and logging in

			//Step-1: Set the search type in the traditioanl search
			//-----------------------------------------------------
			homePage.searchPanel.setSearchInType(dataPool.get("SearchType"));//Sets the search in type

			Log.message("1. Search type - '" + dataPool.get("SearchType") + "' is set in the traditional search.", driver);

			//Step-2: Enter the unmanaged object keyword
			//-------------------------------------------
			homePage.searchPanel.setSearchWord(dataPool.get("SearchWord"));//Sets the searchword in the traditional search

			Log.message("2. Search word - '" + dataPool.get("SearchWord") + "' is set in the traditional search.", driver);

			//Step-3: Click the search button
			//-------------------------------
			homePage.searchPanel.clickSearchBtn(driver);//Clicks the search button

			Log.message("3. Search button is clicked in the view.", driver);

			//Verification if search objects is present in the search view
			//------------------------------------------------------------
			String result = "";

			//Check if Unmanaged object is exists in the view
			//------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName").split("\n")[0]))
				result = "Unmanaged object - '" + dataPool.get("ObjectName").split("\n")[0] + "' is not exist in the search view.";

			//Check if Managed object is exists in the view
			//------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName").split("\n")[1]))
				result = "Managed object - '" + dataPool.get("ObjectName").split("\n")[1] + "' is not exist in the search view.";

			if (result.equals(""))
				Log.pass("Test case passed. Search objects based on \"search in metadata and file contents\" in traditional search is working as expected.");
			else
				Log.fail("Test case failed. Search objects based on \"search in metadata and file contents\" in traditional search is not working as expected. Additional info. : "+result, driver);


		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End ExtnStorageSearch_43083

	/**
	 *  ExtnStorageSearch_43084: Check for clear history in traditional search
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Check for clear history in traditional search")
	public void ExtnStorageSearch_43084(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launches login page and logging in

			//Step-1: Create some search history
			//-----------------------------
			String[] searchKeys = dataPool.get("SearchWords").split("\n");
			String history = "";
			for(int counter = 0; counter < searchKeys.length; counter++) {
				homePage.searchPanel.setSearchWord(searchKeys[counter], true);
				homePage.searchPanel.clickSearch();
				if (counter == 0)
					history = searchKeys[counter];
				else
					history += ", "+ searchKeys[counter];
				Utils.fluentWait(driver);
			}

			Log.message("1. Search performed using the Search words: '" + history + "' to create the search history.", driver);

			//Step-2: Check the search history
			//--------------------------------
			List<String> searchWords = homePage.searchPanel.getSearchHistory();

			if(searchWords.size() <= 0)
				throw new Exception("Search history is created as expected with the given search words.");

			Log.message("2. Checked search history is created as expected with the given search words.", driver);

			//Step-3: Clear the search history
			//--------------------------------
			homePage.searchPanel.clearHistory();//Clears the search history

			Log.message("3. Clear history is clicked from the search word drop down list.", driver);

			//Step-4: Click Yes in the confirmation dialog
			//---------------------------------------------
			if (!MFilesDialog.exists(driver, "M-Files"))
				throw new Exception("Confirmation dialog is not opened to clear the search history.");

			MFilesDialog mfDialog = new MFilesDialog(driver, "M-Files");//Instantiate the M-Files Dialog

			if (!mfDialog.confirmDeleteHistory())//Clicks yes button in the confirmation dialog
				throw new Exception("Error while confirming the clear the search history.");

			Log.message("4. Confirmed the clear the search history action.", driver);

			//Verification: Check if history is cleared successfully
			//------------------------------------------------------			
			if (homePage.searchPanel.getSearchHistory().size() == 0)
				Log.pass("Test case passed. Clear history in traditional search is working as expected.");
			else
				Log.fail("Test case failed. Clear history is not cleared the search history in the traditional search.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End ExtnStorageSearch_43084

	/**
	 *  ExtnStorageSearch_43085: Check for clear history in right pane search
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Check for clear history in right pane search")
	public void ExtnStorageSearch_43085(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;
		ConfigurationPage configPage = null;

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			configPage = LoginPage.launchDriverAndLoginToConfig(driver, true); //Launches login page and logging into the configuration page

			//1 : Navigate to the controls in the configuration page for the test vault
			//-------------------------------------------------------------------------
			configPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);

			Log.message("1. Navigated to '"+Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value+"' in Configuration Page", driver);

			//2 : Enable search in right pane option in configuration page
			//------------------------------------------------------------
			configPage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Show.Value);

			if (!configPage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Show.Value))
				throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Show.Value);

			configPage.clickSaveButton();//Clicks the save button

			Log.message("2. 'Search in right pane' is enabled and settings are saved in the configuration page.", driver);

			//Step-4 : Logout from configuration page
			//---------------------------------------
			configPage.clickOKBtnOnSaveDialog();//Clicks OK button in the MFiles Dialog
			configPage.clickLogOut(); //Logs out from the Configuration page

			Log.message("3. Logged out from Configuration Webpage");

			//Login to MFiles web access
			//--------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launches login page and logging in

			Log.message("--------------------------------Steps---------------------------------------------");
			Log.message("1. Logged into M-Files Web access.", driver);

			//Step-1: Create some search history
			//----------------------------------
			String[] searchKeys = dataPool.get("SearchWords").split("\n");
			String history = "";
			for(int counter = 0; counter < searchKeys.length; counter++) {
				homePage.searchPanel.setSearchWordInRightPane(searchKeys[counter], true);//Sets the search key word in the right pane
				homePage.searchPanel.clickRightPaneSearchButton();//Clicks the search button in the right pane
				if (counter == 0)
					history = searchKeys[counter];
				else
					history += ", "+ searchKeys[counter];
				Utils.fluentWait(driver);
			}

			Log.message("2. Search performed using the Search words: '" + history + "' to create the search history.", driver);

			//Step-2: Check the search history
			//--------------------------------
			List<String> searchWords = homePage.searchPanel.getSearchHistoryInRightPane();//Gets the search history from the right pane

			if(searchWords.size() <= 0)
				throw new Exception("Search history is created as expected with the given search words.");

			Log.message("3. Checked search history is created as expected with the given search words.", driver);

			//Step-3: Clear the search history
			//--------------------------------
			homePage.searchPanel.clearSearchHistoryInRightPane();//Clears the search history in right pane

			Log.message("4. Clear history is clicked from the search word drop down list.", driver);

			//Step-4: Click Yes in the confirmation dialog
			//---------------------------------------------
			if (!MFilesDialog.exists(driver, "M-Files"))
				throw new Exception("Confirmation dialog is not opened to clear the search history.");

			MFilesDialog mfDialog = new MFilesDialog(driver, "M-Files");//Instantiate the M-Files Dialog

			if (!mfDialog.confirmDeleteHistory())//Clicks yes button in the confirmation dialog
				throw new Exception("Error while confirming the clear the search history.");

			Log.message("5. Confirmed the clear the search history action.", driver);

			//Verification: Check if history is cleared successfully
			//------------------------------------------------------			
			if (homePage.searchPanel.getSearchHistoryInRightPane().size() == 0)
				Log.pass("Test case passed. Clear history in traditional search is working as expected.");
			else
				Log.fail("Test case failed. Clear history is not cleared the search history in the right pane search.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			if (driver != null)
			{
				try
				{
					driver.get(configURL);//Launches the configuration page
					configPage = LoginPage.launchDriverAndLoginToConfig(driver, false); //Launches login page and logging into the configuration page
					configPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);
					configPage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Hide.Value);
					if (!configPage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Hide.Value))
						throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Show.Value);
					configPage.clickSaveButton();//Clicks the save button
					configPage.clickOKBtnOnSaveDialog();//Clicks OK button in the MFiles Dialog
				}
				catch(Exception e0){Log.exception(e0, driver);}
			}
			Utility.quitDriver(driver);
		}//End finally

	}//End ExtnStorageSearch_43085

	/**
	 *  ExtnStorageSearch_43369: Check if javascript is executed when searching
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Check if javascript is executed when searching")
	public void ExtnStorageSearch_43369(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launches login page and logging in

			//Step-1: Create new object in the home view wit script value
			//-----------------------------------------------------------
			if (!homePage.taskPanel.clickItem(Caption.ObjecTypes.Customer.Value))
				throw new Exception("'" + Caption.ObjecTypes.Customer.Value + "' is not clicked from the task pane.");

			Log.message("1.1. '" + Caption.ObjecTypes.Customer.Value + "' is clicked from task pane.", driver);

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadata card
			metadataCard.setInfo(dataPool.get("Properties"));//Sets the required values in the metadatacard
			metadataCard.saveAndClose();//Clicks the save button in the metadata card

			Log.message("1.2. Required fields entered and save button is clicked in the new object metadata card.", driver);

			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))
				throw new Exception(dataPool.get("ObjectName") + "is not created successfully.");

			Log.message("1.3. New object is created with script value and exist in the home view.");

			//Step-2 : Perform traditional search with javascript value
			//---------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, dataPool.get("Search"), dataPool.get("ObjectName"));//Navigates to the specific view

			Log.message("2. Clicked the search button with script value value.", driver);

			//Verification: if javascript value named object is exist in the search view and not executed
			//-------------------------------------------------------------------------------------------
			if (homePage.listView.isItemExists(dataPool.get("ObjectName")))
				Log.pass("Test Case passed. Javascript is not executed when searching.");
			else
				Log.fail("Test case failed. Object which contains javascript value as name not exist in the search view.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End ExtnStorageSearch_43369

}

package MFClient.Tests;

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
import MFClient.Wrappers.ListView;
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class GetHyperlink {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String configURL = null;
	public static String userName = null;
	public static String password = null;
	public static String testVault = null;
	public static String className = null;
	public static String productVersion = null;
	public static WebDriver driver = null;
	public String methodName = null;
	public String userFullName = null;

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
	 * 101_1_2_1A : Verify if Combo URL dialog get closed while click X button from context menu for admin user
	 * @param itemName - Name of an item
	 * @return true if item is right clicked; false if item is not right clicked
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint101", "Get Combo URL"}, 
			description = "Verify if Combo URL dialog get closed while click X button from context menu for admin user")
	public void SprintTest101_1_2_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));
			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Right click on the object and select GetComboURL from context menu
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");
			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetHyperlink.Value); //Selects Get ComboURL from Context menu
			Log.message("2. 'Get ComboURL' is selected from Context menu.");

			//Step-3 : Open Get ComboURL dialog for the object from Context menu
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class
			if (!mfilesDialog.isGetComboURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with " + Caption.MenuItems.GetHyperlink.Value + " title is not opened.");
			Log.message("3. Get Combo URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-4 : Click x button in the dialog
			mfilesDialog.close(); //Clicks X button in the dialog
			Log.message("4. Close (X) button is clicked in the dialog.");

			//Verification : To Verify MFiles dialog is closed on clicking x button
			if (!MFilesDialog.exists(driver))
				Log.pass("Test case Passed. The Get Combo URL dialog get closed while click X button from context menu for admin user.");
			else
				Log.fail("Test case Failed. The Get Combo URL dialog is not get closed while click X button from context menu for admin user", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest101_1_2_1A

	/**
	 * 101_1_2_1B : Verify if Combo URL dialog get closed while click X button from operation menu for admin user
	 * @param itemName - Name of an item
	 * @return true if item is right clicked; false if item is not right clicked
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint101", "Get Combo URL"}, 
			description = "Click X button in Combo URL dialog  opened through Operation menu for admin user")
	public void SprintTest101_1_2_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));
			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select Get Combo URL from Operations menu
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetHyperlink.Value); //Selects Get ComboURL from operations menu
			Log.message("2. 'Get ComboURL' is selected from operations menu.");

			//Step-3 : Open Get ComboURL dialog for the object from operations menu
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class
			if (!mfilesDialog.isGetComboURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with " + Caption.MenuItems.GetHyperlink.Value + " title is not opened.");
			Log.message("3. Get Combo URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-4 : Click x button in the dialog
			mfilesDialog.close(); //Clicks X button in the dialog
			Log.message("4. Close (X) button is clicked in the dialog.");

			//Verification : To Verify MFiles dialog is closed on clicking x button
			if (!MFilesDialog.exists(driver))
				Log.pass("Test case Passed. MFiles dialog is closed on clicking close (X) button.");
			else
				Log.fail("Test case Failed. MFiles dialog is not closed on clicking close (X) button.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest101_1_2_1B

	/**
	 * 101_1_2_2A : Verify if Combo URL dialog get closed while click X button from context menu for non admin user
	 * @param itemName - Name of an item
	 * @return true if item is right clicked; false if item is not right clicked
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint101", "Get Combo URL"}, 
			description = "Verify if Combo URL dialog get closed while click X button from context menu for non admin user")
	public void SprintTest101_1_2_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1 : Login to MFWA
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String vault = xmlParameters.getParameter("VaultName");
			driver.get(loginURL); //Launches with the URL
			Utils.fluentWait(driver);
			//Checks if Login.aspx page is loaded
			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX")) 
				throw new Exception ("Browser is not navigated to the Login page.");
			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), vault); //Logs into application
			Utils.fluentWait(driver);
			Log.message("1. Logged into MFWA as " + dataPool.get("Admin") +" user.");

			//Step-2 : Navigate to specified View
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));
			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-3 : Select Get Combo URL from Operations menu
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");
			//Selects Get ComboURL from operations menu
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetHyperlink.Value); 
			Log.message("3. 'Get ComboURL' is selected from operations menu.");

			//Step-4 : Check the Get ComboURL dialog for the object from Context menu
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class
			if (!mfilesDialog.isGetComboURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with " + Caption.MenuItems.GetHyperlink.Value + " title is not opened.");
			Log.message("4. Get Combo URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-5 : Click x button in the dialog
			mfilesDialog.close(); //Clicks X button in the dialog
			Log.message("5. Close (X) button is clicked in the dialog.");

			//Verification : To Verify MFiles dialog is closed on clicking x button
			if (!MFilesDialog.exists(driver))
				Log.pass("Test case Passed. Combo URL dialog get closed while click X button from context menu for "+ dataPool.get("Admin") + " user.");
			else
				Log.fail("Test case Failed. Combo URL dialog not get closed while click X button from context menu for "+ dataPool.get("Admin") + " user.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest101_1_2_2A

	/**
	 * 101_1_2_2B : Verify if Combo URL dialog get closed while click X button from operation menu for non admin user
	 * @param itemName - Name of an item
	 * @return true if item is right clicked; false if item is not right clicked
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint101", "Get Combo URL"}, 
			description = "Verify if Combo URL dialog get closed while click X button from operation menu for non admin user")
	public void SprintTest101_1_2_2B(HashMap<String,String> dataValues, String driverType) throws Exception {
		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1 : Login to MFWA
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String vault = xmlParameters.getParameter("VaultName");
			driver.get(loginURL); //Launches with the URL
			Utils.fluentWait(driver);
			//Checks if Login.aspx page is loaded
			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Browser is not navigated to the Login page.");
			//Instantiates LoginPage class
			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), vault); //Logs into application
			Utils.fluentWait(driver);
			Log.message("1. Logged into MFWA as " + dataPool.get("Admin") +" user.");

			//Step-2 : Navigate to specified View
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));
			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-3 : Right click on the object and select Get ComboURL from context menu
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");
			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetHyperlink.Value); //Selects Get ComboURL from Context menu
			Log.message("2. 'Get ComboURL' is selected from Context menu.");

			//Step-3 : Open Get ComboURL dialog for the object from Context menu
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class
			if (!mfilesDialog.isGetComboURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with " + Caption.MenuItems.GetHyperlink.Value + " title is not opened.");
			Log.message("3. Get Combo URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-4 : Click x button in the dialog
			mfilesDialog.close(); //Clicks X button in the dialog
			Log.message("4. Close (X) button is clicked in the dialog.");

			//Verification : To Verify MFiles dialog is closed on clicking x button
			if (!MFilesDialog.exists(driver))
				Log.pass("Test case Passed. Combo URL dialog get closed while click X button from operation menu for "+ dataPool.get("Admin") + " user.");
			else
				Log.fail("Test case Failed. Combo URL dialog not get closed while click X button from operation menu for "+ dataPool.get("Admin") + " user.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest101_1_2_2B

	/**
	 * 101_1_2_3A : Verify the Get Combo URL option is displayed in context menu for Home view
	 * @param itemName - Name of an item
	 * @return true if item is right clicked; false if item is not right clicked
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint101", "Get Combo URL"}, 
			description = "Verify the Get Combo URL option is displayed in context menu for Home view")
	public void SprintTest101_1_2_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1 : Login to MFWA
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String vault = xmlParameters.getParameter("VaultName");
			driver.get(loginURL); //Launches with the URL
			Utils.fluentWait(driver);
			//Checks if Login.aspx page is loaded
			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Browser is not navigated to the Login page.");
			//Instantiates LoginPage class
			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), vault); //Logs into application
			Utils.fluentWait(driver);
			Log.message("1. Logged into MFWA as " + dataPool.get("Admin") +" user.");

			//Step-2 : Navigate to specified View
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));
			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-3 : Right click on the object and select Get ComboURL from context menu
			homePage.listView.rightClickListview(); //Selects Get ComboURL from Context menu
			Log.message("3. Right click on list view and opened Context menu.");

			//Verification : To Verify Get combo URL option is displayed in context menu
			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.GetHyperlink.Value))
				Log.pass("Test case Passed. The Get Combo URL option is enabled in context menu for Home view for ." + dataPool.get("Admin") + " user");
			else
				Log.fail("Test case Failed. The Get Combo URL option is disabled in context menu for Home view for ." + dataPool.get("Admin") + " user.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest101_1_2_3A

	/**
	 * 101_1_2_3B : Verify the Get Combo URL option is displayed in operation menu for Home view
	 * @param itemName - Name of an item
	 * @return true if item is right clicked; false if item is not right clicked
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint101", "Get Combo URL"}, 
			description = "Verify the Get Combo URL option is displayed in operation menu for Home view")
	public void SprintTest101_1_2_3B(HashMap<String,String> dataValues, String driverType) throws Exception {
		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1 : Login to MFWA
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String vault = xmlParameters.getParameter("VaultName");
			driver.get(loginURL); //Launches with the URL
			Utils.fluentWait(driver);
			//Checks if Login.aspx page is loaded
			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Browser is not navigated to the Login page.");
			//Instantiates LoginPage class
			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), vault); //Logs into application
			Utils.fluentWait(driver);
			Log.message("1. Logged into MFWA as " + dataPool.get("Admin") +" user.");

			//Step-2 : Navigate to specified View
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));
			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Verification : To Verify Get combo URL option is displayed in operation menu
			if (!homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.GetHyperlink.Value))
				Log.pass("Test case Passed. The Get Combo URL option is disabled in operation menu for Home view for ." + dataPool.get("Admin") + " user");
			else
				Log.fail("Test case Failed. The Get Combo URL option is enabled in operation menu for Home view for ." + dataPool.get("Admin") + " user.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest101_1_2_3B

	/**
	 * 101_1_2_4A : Verify if Get Combo URL option is displayed in context menu while selecting any folder view
	 * @param 
	 * @return
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint101", "Get Combo URL"}, 
			description = "Verify if Get Combo URL option is displayed in context menu while selecting any folder view")
	public void SprintTest101_1_2_4A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1 : Login to MFWA
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String vault = xmlParameters.getParameter("VaultName");
			driver.get(loginURL); //Launches with the URL
			Utils.fluentWait(driver);
			//Checks if Login.aspx page is loaded
			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Browser is not navigated to the Login page.");
			//Instantiates LoginPage class
			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), vault); //Logs into application
			Utils.fluentWait(driver);
			Log.message("1. Logged into MFWA as " + dataPool.get("Admin") +" user.");

			//Step-2 : Navigate to specified View
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));
			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-3 : Right click on the object and select Get ComboURL from context menu
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");
			homePage.listView.rightClickListview(); //Selects Get ComboURL from Context menu
			Log.message("3. Right click on " + dataPool.get("ObjectName") + "folder in list view.");

			//Verification : To Verify Get combo URL option is displayed in context menu
			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.GetHyperlink.Value))
				Log.pass("Test case Passed. The Get Combo URL option is disabled in context menu for folder views for ." + dataPool.get("Admin") + " user");
			else
				Log.fail("Test case Failed. The Get Combo URL option is enabled in context menu for folder views for ." + dataPool.get("Admin") + " user", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest101_1_2_4A

	/**
	 * 101_1_2_4B : Verify if Get Combo URL option is displayed in operation menu while selecting any folder view
	 * @param 
	 * @return
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint101", "Get Combo URL"}, 
			description = "Verify if Get Combo URL option is displayed in operation menu while selecting any folder view")
	public void SprintTest101_1_2_4B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1 : Login to MFWA
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String vault = xmlParameters.getParameter("VaultName");
			driver.get(loginURL); //Launches with the URL
			Utils.fluentWait(driver);
			//Checks if Login.aspx page is loaded
			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Browser is not navigated to the Login page.");
			//Instantiates LoginPage class
			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), vault); //Logs into application
			Utils.fluentWait(driver);
			Log.message("1. Logged into MFWA as " + dataPool.get("Admin") +" user.");

			//Step-2 : Navigate to specified View
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));
			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-3 : Select any existing folder in Home view
			homePage.listView.clickItem(dataPool.get("ObjectName"));
			Log.message("3. " + viewToNavigate + "' folder selected in list view.");

			//Verification : To Verify Get combo URL option is displayed in operation menu
			if (!homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.GetHyperlink.Value))
				Log.pass("Test case Passed. The Get Combo URL option is disabled in operation menu for folder views for " + dataPool.get("Admin") + " user");
			else
				Log.fail("Test case Failed. The Get Combo URL option is enabled in operation menu for folder views for " + dataPool.get("Admin") + " user", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest101_1_2_4B

	/**
	 * 101_1_2_5A : Verify if Get Combo URL option is enabled in context menu while user multi select objects using control key in list view
	 * @param 
	 * @return
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint101", "Get Combo URL"}, 
			description = "Verify if Get Combo URL option is displayed in context menu while user multi select objects in list view")
	public void SprintTest101_1_2_5A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports multi-select.");

		driver = null; 

		try { 



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1 : Login to MFWA
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String vault = xmlParameters.getParameter("VaultName");
			driver.get(loginURL); //Launches with the URL
			Utils.fluentWait(driver);
			//Checks if Login.aspx page is loaded
			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Browser is not navigated to the Login page.");
			//Instantiates LoginPage class
			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), vault); //Logs into application
			Utils.fluentWait(driver);
			Log.message("1. Logged into MFWA as " + dataPool.get("Admin") +" user.");

			//Step-2 : Navigate to specified View
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));
			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-3 : Right click on the Multi select object in listing view
			if (!homePage.listView.rightClickOnMultiSelectedItem(dataPool.get("Objects"))) //Selects the Object header in the list
				throw new Exception("Object (" + dataPool.get("ObjectHeader") + ") is not got selected.");

			Log.message("3. Right clicked on an object header and context menu is opened.");

			//Verification : To Verify Get Combo URL option is available in context menu
			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.GetHyperlink.Value))
				Log.pass("Test case Passed. Get Combo URL is disabled for multi slelected objects in context menu for  " + dataPool.get("Admin") + " user .");
			else
				Log.fail("Test case Failed. Get Combo URL is not disabled for multi slelected objects in context menu for  " + dataPool.get("Admin") + " user.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest101_1_2_5A

	/**
	 * 101_1_2_5B : Verify if Get Combo URL option is displayed in operation menu while user multi select objects in list view
	 * @param 
	 * @return
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint101", "Get Combo URL"}, 
			description = "Verify the Get Combo URL for object group headers in operation menu for non-admin user")
	public void SprintTest101_1_2_5B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("SAFARI") || driverType.equalsIgnoreCase("IE"))
			throw new SkipException(driverType+" does not support multi select.");

		driver = null; 

		try { 



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1 : Login to MFWA
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String vault = xmlParameters.getParameter("VaultName");
			driver.get(loginURL); //Launches with the URL
			Utils.fluentWait(driver);
			//Checks if Login.aspx page is loaded
			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Browser is not navigated to the Login page.");
			//Instantiates LoginPage class
			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), vault); //Logs into application
			Utils.fluentWait(driver);
			Log.message("1. Logged into MFWA as " + dataPool.get("Admin") +" user.");

			//Step-2 : Navigate to specified View
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));
			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-3 : Right click on the Multi select object in listing view
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));
			Log.message("3. " + dataPool.get("Objects") +"Objects are multi selected using control key in list view.");

			//Verification : To Verify Get Combo URL option is available in operation menu
			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.GetHyperlink.Value))
				Log.pass("Test case Passed. Get Combo URL is disabled for multi slelected objects in operation menu for  " + dataPool.get("Admin") + " user .");
			else
				Log.fail("Test case Failed. Get Combo URL is not disabled for multi slelected objects in operation menu for  " + dataPool.get("Admin") + " user.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest101_1_2_5B

	/**
	 * 101_1_2_6A : Verify if Get Combo URL option is displayed in context menu while user multi select objects in list view
	 * @param 
	 * @return
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint101", "Get Combo URL"}, 
			description = "Verify if Get Combo URL option is displayed in context menu while user multi select objects in list view")
	public void SprintTest101_1_2_6A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try { 



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1 : Login to MFWA
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String vault = xmlParameters.getParameter("VaultName");
			driver.get(loginURL); //Launches with the URL
			Utils.fluentWait(driver);
			//Checks if Login.aspx page is loaded
			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Browser is not navigated to the Login page.");
			//Instantiates LoginPage class
			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), vault); //Logs into application
			Utils.fluentWait(driver);
			Log.message("1. Logged into MFWA as " + dataPool.get("Admin") +" user.");

			//Step-2 : Navigate to specified View
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));
			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-3 : Expand any exiting object in listing view
			homePage.listView.expandItemByName(dataPool.get("ObjectName"));
			Utils.fluentWait(driver);
			Log.message("3. Expanded '" + viewToNavigate + "' object in listing view.");

			//Step-4 : Right click on the object and select Get Combo URL from context menu
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectHeader"))) //Selects the Object header in the list
				throw new Exception("Object (" + dataPool.get("ObjectHeader") + ") is not got selected.");
			Log.message("4. Right clicked on an object header and context menu is opened.");

			//Verification : To Verify Get Combo URL option is available in context menu
			if (!homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.GetHyperlink.Value))
				Log.pass("Test case Passed. The Get Combo URL option is disabled in context menu while user multi select objects in list view for" + dataPool.get("Admin")+ "user.");
			else
				Log.fail("Test case Failed. The Get Combo URL option is enabled in context menu while user multi select objects in list view for" + dataPool.get("Admin")+ "user.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest101_1_2_6A

	/**
	 * 101_1_2_6B : Verify if Get Combo URL option is displayed in operation menu while user multi select objects in list view
	 * @param 
	 * @return
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint101", "Get Combo URL"}, 
			description = "Verify if Get Combo URL option is displayed in operation menu while user multi select objects in list view")
	public void SprintTest101_1_2_6B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try { 



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1 : Login to MFWA
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String vault = xmlParameters.getParameter("VaultName");
			driver.get(loginURL); //Launches with the URL
			Utils.fluentWait(driver);
			//Checks if Login.aspx page is loaded
			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Browser is not navigated to the Login page.");
			//Instantiates LoginPage class
			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), vault); //Logs into application
			Utils.fluentWait(driver);
			Log.message("1. Logged into MFWA as " + dataPool.get("Admin") +" user.");

			//Step-2 : Navigate to specified View
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));
			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-3 : Expand any exiting object in listing view
			homePage.listView.expandItemByName(dataPool.get("ObjectName"));
			Utils.fluentWait(driver);
			Log.message("3. Expanded '" + viewToNavigate + "' object in listing view.");

			//Step-4 : Select object header in expanded object
			homePage.listView.clickItem(dataPool.get("ObjectHeader"));
			Log.message("4. Object group header selected in listing view.");

			//Verification : To Verify Get Combo URL option is available in context menu
			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.GetHyperlink.Value))
				Log.pass("Test case Passed. The Get Combo URL option is disabled in operation menu while user multi select objects in list view for" + dataPool.get("Admin")+ "user.");
			else
				Log.fail("Test case Failed. The Get Combo URL option is enabled in operation menu while user multi select objects in list view for" + dataPool.get("Admin")+ "user.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest101_1_2_6B

	/**
	 * 101_1_2_7A : Verify if Get Combo URL option is displayed in context menu while user selects virtual folder in MFWA
	 * @param 
	 * @return
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint101", "Get Combo URL"}, 
			description = "Verify if Get Combo URL option is displayed in context menu while user selects virtual folder in MFWA")
	public void SprintTest101_1_2_7A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try { 



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1 : Login to MFWA
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String vault = xmlParameters.getParameter("VaultName");
			driver.get(loginURL); //Launches with the URL
			Utils.fluentWait(driver);
			//Checks if Login.aspx page is loaded
			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Browser is not navigated to the Login page.");
			//Instantiates LoginPage class
			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), vault); //Logs into application
			Utils.fluentWait(driver);
			Log.message("1. Logged into MFWA as " + dataPool.get("Admin") +" user.");

			//Step-2 : Navigate to specified View
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));
			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-3: Select the object and open Get Combo URL dialog from context menu
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. View/Virtual Folder (" + dataPool.get("ObjectName") + ") does not exists in the listView.");
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Right Clicks on an object
				throw new Exception("View/Virutal folder (" + dataPool.get("ObjectName") + ") is not right clicked.");
			Log.message("3. Right clicked on the virtual folder and context menu is opened.");

			//Verification : To Verify Get Combo URL option is available in context menu
			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.GetHyperlink.Value))
				Log.pass("Test case Passed. The Get Combo URL option is disabled in context menu for selected virtual folder for" + dataPool.get("Admin")+ "user.");
			else
				Log.fail("Test case Failed. The Get Combo URL option is enabled in context menu for selected virtual folder for" + dataPool.get("Admin")+ "user.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest101_1_2_7A

	/**
	 * 101_1_2_7B : Verify if Get Combo URL option is displayed in operation menu while user selects virtual folder in MFWA
	 * @param 
	 * @return
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint101", "Get Combo URL"}, 
			description = "Verify if Get Combo URL option is displayed in operatin menu while user selects virtual folder in MFWA")
	public void SprintTest101_1_2_7B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try { 



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1 : Login to MFWA
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String vault = xmlParameters.getParameter("VaultName");
			driver.get(loginURL); //Launches with the URL
			Utils.fluentWait(driver);

			//Checks if Login.aspx page is loaded
			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Browser is not navigated to the Login page.");

			//Instantiates LoginPage class
			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), vault); //Logs into application
			Utils.fluentWait(driver);
			Log.message("1. Logged into MFWA as " + dataPool.get("Admin") +" user.");

			//Step-2 : Navigate to specified View
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));
			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-3: Select the object and open Get Combo URL dialog from operation menu
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. View/Virtual Folder (" + dataPool.get("ObjectName") + ") does not exists in the listView.");
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("View/Virutal folder (" + dataPool.get("ObjectName") + ") is not right clicked.");
			Log.message("3. Object selected in list view and operation menu is opened.");

			//Verification : To Verify Get Combo URL option is available in context menu
			if (!homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.GetHyperlink.Value))
				Log.pass("Test case Passed. The Get Combo URL option is disabled in context menu for selected virtual folder for" + dataPool.get("Admin")+ "user.");
			else
				Log.fail("Test case Failed. The Get Combo URL option is enabled in context menu for selected virtual folder for" + dataPool.get("Admin")+ "user.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest101_1_2_7B

	/**
	 * 101_1_2_8A : Verify Get Combo URL contents displayed syntax in Get Combo URL dialog from context menu
	 * @param 
	 * @return
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint101", "Get Combo URL"}, 
			description = "Verify Get Combo URL contents displayed syntax in Get Combo URL dialog from context menu")
	public void SprintTest101_1_2_8A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try { 



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1 : Login to MFWA
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String vault = xmlParameters.getParameter("VaultName");
			driver.get(loginURL); //Launches with the URL
			Utils.fluentWait(driver);

			//Checks if Login.aspx page is loaded
			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Browser is not navigated to the Login page.");

			//Instantiates LoginPage class
			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), vault); //Logs into application
			Utils.fluentWait(driver);
			Log.message("1. Logged into MFWA as " + dataPool.get("Admin") +" user.");

			//Step-2 : Navigate to specified View
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));
			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-3: Select the object and open Get Combo URL dialog from context menu
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Right Clicks on an object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");
			Log.message("3. Right clicked on the object and context menu is opened.");

			//Step-4: Verify Get Combo URL option is enabled in context menu
			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.GetHyperlink.Value))
				throw new Exception("Get Combo URL is not enabled in Context menu.");
			Log.message("4. Get Combo URL is enabled in Context menu.");

			//Step-5: Select Get Combo URL from context menu
			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetHyperlink.Value);
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class
			if (!mfilesDialog.isGetComboURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with " + Caption.MenuItems.GetHyperlink.Value + " title is not opened.");
			Log.message("5. Get Combo URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Verification : To Verify Get Combo URL displayed syntax in Get Combo URL dialog
			if (mfilesDialog.getTextFromComboURLDialog(dataPool.get("CheckItem")).equalsIgnoreCase(dataPool.get("ContentDisplay")))
				Log.pass("Test case Passed. The Combo URL displayed with proper syntax in Get Combo URL dialog from context menu for " + dataPool.get("Admin")+ "user.");
			else
				Log.fail("Test case Failed. The Combo URL is not displayed with proper syntax in Get Combo URL dialog from context menu for" + dataPool.get("Admin")+ "user.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest101_1_2_8A

	/**
	 * 101_1_2_8B : Verify Get Combo URL contents displayed syntax in Get Combo URL dialog from operation menu
	 * @param 
	 * @return
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint101", "Get Combo URL"}, 
			description = "Verify Get Combo URL contents displayed syntax in Get Combo URL dialog from operation menu")
	public void SprintTest101_1_2_8B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try { 



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1 : Login to MFWA
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String vault = xmlParameters.getParameter("VaultName");
			driver.get(loginURL); //Launches with the URL
			Utils.fluentWait(driver);
			//Checks if Login.aspx page is loaded
			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Browser is not navigated to the Login page.");
			//Instantiates LoginPage class
			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), vault); //Logs into application
			Utils.fluentWait(driver);
			Log.message("1. Logged into MFWA as " + dataPool.get("Admin") +" user.");

			//Step-2 : Navigate to specified View
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));
			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-3: Select the object and open Get Combo URL dialog from operation menu
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");
			Log.message("3. Object selected in list view.");

			//Step-4: Select Get Combo URL from operation menu
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetHyperlink.Value);
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class
			if (!mfilesDialog.isGetComboURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with " + Caption.MenuItems.GetHyperlink.Value + " title is not opened.");
			Log.message("4. Get Combo URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Verification : To Verify Get Combo URL displayed syntax in Get Combo URL dialog
			if (mfilesDialog.getTextFromComboURLDialog(dataPool.get("CheckItem")).equalsIgnoreCase(dataPool.get("ContentDisplay")))
				Log.pass("Test case Passed. The Combo URL displayed with proper syntax in Get Combo URL dialog from context menu for " + dataPool.get("Admin")+ "user.");
			else
				Log.fail("Test case Failed. The Combo URL is not displayed with proper syntax in Get Combo URL dialog from context menu for" + dataPool.get("Admin")+ "user.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest101_1_2_8B

	/**
	 * 101_1_2_9A : Verify if Combo URL text box and text area are editable mode in Get combo URL dialog from context menu
	 * @param 
	 * @return
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint101", "Get Combo URL"}, 
			description = "Verify if Combo URL text box and text area are editable mode in Get combo URL dialog from context menu")
	public void SprintTest101_1_2_9A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try { 



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1 : Login to MFWA
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String vault = xmlParameters.getParameter("VaultName");
			driver.get(loginURL); //Launches with the URL
			Utils.fluentWait(driver);
			//Checks if Login.aspx page is loaded
			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Browser is not navigated to the Login page.");
			//Instantiates LoginPage class
			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), vault); //Logs into application
			Utils.fluentWait(driver);
			Log.message("1. Logged into MFWA as " + dataPool.get("Admin") +" user.");

			//Step-2 : Navigate to specified View
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));
			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-3: Select the object and open Get Combo URL dialog from context menu
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Right Clicks on an object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");
			Log.message("3. Right clicked on the object and context menu is opened.");

			//Step-4: Verify Get Combo URL option is enabled in context menu
			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.GetHyperlink.Value))
				throw new Exception("Get Combo URL is not enabled in Context menu.");
			Log.message("4. Get Combo URL is enabled in Context menu.");

			//Step-5: Select Get Combo URL from context menu
			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetHyperlink.Value);
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class
			if (!mfilesDialog.isGetComboURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with " + Caption.MenuItems.GetHyperlink.Value + " title is not opened.");
			Log.message("5. Get Combo URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Verification : To Verify Get Combo URL displayed syntax in Get Combo URL dialog
			if (!mfilesDialog.isComboURLEditable())
				Log.pass("Test case Passed. The Combo URL text box & text area are not in editable mode in Get Combo URL dialog from context menu for " + dataPool.get("Admin")+ " user.");
			else
				Log.fail("Test case Failed. The Combo URL text box & text area are in editable mode in Get Combo URL dialog from context menu for " + dataPool.get("Admin")+ " user.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest101_1_2_9A

	/**
	 * 101_1_2_9B : Verify if Combo URL text box and text area are editable mode in Get combo URL dialog from operation menu
	 * @param 
	 * @return
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint101", "Get Combo URL"}, 
			description = "Verify if Combo URL text box and text area are editable mode in Get combo URL dialog from operaiton menu")
	public void SprintTest101_1_2_9B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try { 



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1 : Login to MFWA
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String vault = xmlParameters.getParameter("VaultName");
			driver.get(loginURL); //Launches with the URL
			Utils.fluentWait(driver);
			//Checks if Login.aspx page is loaded
			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Browser is not navigated to the Login page.");
			//Instantiates LoginPage class
			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), vault); //Logs into application
			Utils.fluentWait(driver);
			Log.message("1. Logged into MFWA as " + dataPool.get("Admin") +" user.");

			//Step-2 : Navigate to specified View
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));
			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-3: Select the object and open Get Combo URL dialog from operation menu
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");
			Log.message("3. Object selected in list view.");

			//Step-4: Select Get Combo URL from operation menu
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetHyperlink.Value);
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class
			if (!mfilesDialog.isGetComboURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with " + Caption.MenuItems.GetHyperlink.Value + " title is not opened.");
			Log.message("4. Get Combo URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Verification : To Verify Get Combo URL displayed syntax in Get Combo URL dialog
			if (!mfilesDialog.isComboURLEditable())
				Log.pass("Test case Passed. The Combo URL text box & text area are not in editable mode in Get Combo URL dialog from operation menu for " + dataPool.get("Admin")+ " user.");
			else
				Log.fail("Test case Failed. The Combo URL text box & text area are in editable mode in Get Combo URL dialog from operation menu for " + dataPool.get("Admin")+ " user.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest101_1_2_9B

	/**
	 * 101_1_2_10A : Verify Get Combo URL text area contents displayed in Get Combo URL dialog from context menu
	 * @param 
	 * @return
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint101", "Get Combo URL"}, 
			description = "Verify Get Combo URL text area contents displayed in Get Combo URL dialog from context menu")
	public void SprintTest101_1_2_10A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try { 



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1 : Login to MFWA
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String vault = xmlParameters.getParameter("VaultName");
			driver.get(loginURL); //Launches with the URL
			Utils.fluentWait(driver);
			//Checks if Login.aspx page is loaded
			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Browser is not navigated to the Login page.");
			//Instantiates LoginPage class
			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), vault); //Logs into application
			Utils.fluentWait(driver);
			Log.message("1. Logged into MFWA as " + dataPool.get("Admin") +" user.");

			//Step-2 : Navigate to specified View
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));
			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-3: Select the object and open Get Combo URL dialog from context menu
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Right Clicks on an object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");
			Log.message("3. Right clicked on the object and context menu is opened.");

			//Step-4: Verify Get Combo URL option is enabled in context menu
			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.GetHyperlink.Value))
				throw new Exception("Get Combo URL is not enabled in Context menu.");
			Log.message("4. Get Combo URL is enabled in Context menu.");

			//Step-5: Select Get Combo URL from context men
			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetHyperlink.Value);
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class
			if (!mfilesDialog.isGetComboURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with " + Caption.MenuItems.GetHyperlink.Value + " title is not opened.");
			Log.message("5. Get Combo URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened.");

			String[] textareaValue = mfilesDialog.getTextFromComboURLDialog(dataPool.get("CheckItem")).split("\n");
			String[] contentDisplay = dataPool.get("ContentDisplay").split("\n");
			int index =0;
			for(index=0;index<textareaValue.length;index++)
			{
				Log.message(textareaValue[index]);
				Log.message(contentDisplay[index]);
				if(!textareaValue[index].startsWith(contentDisplay[index]))
					Log.fail("Test case Failed. The Combo URL is not displayed with proper syntax in Get Combo URL dialog from context menu for" + dataPool.get("Admin")+ "user.", driver);
			}

			//Verification : To Verify Get Combo URL displayed syntax in Get Combo URL dialog
			if (index == textareaValue.length)
				Log.pass("Test case Passed. The Combo URL displayed with proper syntax in Get Combo URL dialog from context menu for " + dataPool.get("Admin")+ "user.");
			else
				Log.fail("Test case Failed. The Combo URL is not displayed with proper syntax in Get Combo URL dialog from context menu for" + dataPool.get("Admin")+ "user.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest101_1_2_10A

	/**
	 * 101_1_2_10B : Verify Get Combo URL text area contents displayed in Get Combo URL dialog from operation menu
	 * @param 
	 * @return
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint101", "Get Combo URL"}, 
			description = "Verify Get Combo URL text area contents displayed in Get Combo URL dialog from operaiton menu")
	public void SprintTest101_1_2_10B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try { 



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1 : Login to MFWA
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String vault = xmlParameters.getParameter("VaultName");
			driver.get(loginURL); //Launches with the URL
			Utils.fluentWait(driver);
			//Checks if Login.aspx page is loaded
			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Browser is not navigated to the Login page.");
			//Instantiates LoginPage class
			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), vault); //Logs into application
			Utils.fluentWait(driver);
			Log.message("1. Logged into MFWA as " + dataPool.get("Admin") +" user.");

			//Step-2 : Navigate to specified View
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));
			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-3: Select the object and open Get Combo URL dialog from operation menu
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");
			Log.message("3. Object selected in list view.");

			//Step-4: Select Get Combo URL from operation menu
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetHyperlink.Value);
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class
			if (!mfilesDialog.isGetComboURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with " + Caption.MenuItems.GetHyperlink.Value + " title is not opened.");
			Log.message("4. Get Combo URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened.");

			String[] textareaValue = mfilesDialog.getTextFromComboURLDialog(dataPool.get("CheckItem")).split("\n");
			String[] contentDisplay = dataPool.get("ContentDisplay").split("\n");
			int index;
			for(index=0;index<textareaValue.length;index++)
			{
				Log.message(textareaValue[index]);
				Log.message(contentDisplay[index]);
				if(!textareaValue[index].startsWith(contentDisplay[index]))
					Log.fail("Test case Failed. The Combo URL is not displayed with proper syntax in Get Combo URL dialog from operation menu for" + dataPool.get("Admin")+ "user.", driver);
			}

			//Verification : To Verify Get Combo URL displayed syntax in Get Combo URL dialog
			if (index == textareaValue.length)
				Log.pass("Test case Passed. The Combo URL displayed with proper syntax in Get Combo URL dialog from operaiotn menu for " + dataPool.get("Admin")+ "user.");
			else
				Log.fail("Test case Failed. The Combo URL is not displayed with proper syntax in Get Combo URL dialog from operation menu for" + dataPool.get("Admin")+ "user.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest101_1_2_10B

	/**
	 * 101_1_2_11A : Verify the new tab URL is same as link URL Combo url dialog from context menu
	 * @param 
	 * @return
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint101", "Get Combo URL"}, 
			description = "Verify the new tab URL & object displyaed while click on Web link in Combo url dialog from context menu")
	public void SprintTest101_1_2_11A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try { 



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Right click the object
			//-------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is right clicked.");

			//Step-3: Select Get Combo URL from operation menu
			//------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetHyperlink.Value); //Select GetHyperlink from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetComboURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with " + Caption.MenuItems.GetHyperlink.Value + " title is not opened.");

			Log.message("3." + Caption.MenuItems.GetHyperlink.Value + " is opened for object (" + dataPool.get("ObjectName") + ") from context menu.");

			//Step-4: Get link URL value from Combo URL dialog
			//------------------------------------------------
			String URL = mfilesDialog.getURLFromComboURLDialog(dataPool.get("ItemToClick"));

			Log.message("4. " + dataPool.get("ItemToClick") + " link URL value is retrived from Get Combo URL dialog. WEB URL : " + URL);

			//Step-5: Click on Web link on Combo URL dialog
			//------------------------------------------------
			mfilesDialog.clickLinksOnComboURLDialog(dataPool.get("ItemToClick")); //Clicks link in the URL
			Utils.fluentWait(driver);

			Log.message("5. (" + dataPool.get("ItemToClick") + ") link is clicked from Get Combo URL dialog.");

			//Verification : To Verify the new tab URL is same as URL retrieved from Combo URL dialog
			//-----------------------------------------------------------------------------------------
			String tabURL = Utility.getNewTabURL(driver, 1);

			if (URL.trim().equalsIgnoreCase(tabURL.trim()))
				Log.pass("Test case Passed. The the new tab URL is same as URL retrieved from Combo URL dialog from operation menu.");
			else
				Log.fail("Test case Failed. The the new tab URL is not same as URL retrieved from Combo URL dialog from operation menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest101_1_2_11A

	/**
	 * 101_1_2_11B : Verify the new tab URL & object displayed while clicking on Web link in Combo url dialog from operation menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint101", "Get Combo URL"}, 
			description = "Verify the new tab URL & object displayed while clicking on Web link in Combo url dialog from operation menu")
	public void SprintTest101_1_2_11B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try { 



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-: Select the object and open operation menu
			//-------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-3: Select Get Combo URL from operation menu
			//------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetHyperlink.Value); //Select GetHyperlink from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetComboURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with " + Caption.MenuItems.GetHyperlink.Value + " title is not opened.");

			Log.message("3." + Caption.MenuItems.GetHyperlink.Value + " is opened for object (" + dataPool.get("ObjectName") + ") from operations menu.");

			//Step-4: Get link URL value from Combo URL dialog
			//------------------------------------------------
			String URL = mfilesDialog.getURLFromComboURLDialog(dataPool.get("ItemToClick"));

			Log.message("4. " + dataPool.get("ItemToClick") + " link URL value is retrived from Get Combo URL dialog. WEB URL : " + URL);

			//Step-5: Click on Web link on Combo URL dialog
			//------------------------------------------------
			mfilesDialog.clickLinksOnComboURLDialog(dataPool.get("ItemToClick")); //Clicks link in the URL
			Utils.fluentWait(driver);

			Log.message("5. (" + dataPool.get("ItemToClick") + ") link is clicked from Get Combo URL dialog.");

			//Verification : To Verify the new tab URL is same as URL retrieved from Combo URL dialog
			//-----------------------------------------------------------------------------------------
			String tabURL = Utility.getNewTabURL(driver, 1);

			if (URL.trim().equalsIgnoreCase(tabURL.trim()))
				Log.pass("Test case Passed. The the new tab URL is same as URL retrieved from Combo URL dialog from operation menu.");
			else
				Log.fail("Test case Failed. The the new tab URL is not same as URL retrieved from Combo URL dialog from operation menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest101_1_2_11B

	/**
	 * 101_1_2_12A : Verify if the object displayed in new tab while user click Web link in Combo url dialog from context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint101", "Get Combo URL"}, 
			description = "Verify if the object displayed in new tab while user click Web link in Combo url dialog from context menu")
	public void SprintTest101_1_2_12A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try { 



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Right click the object
			//-------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is right clicked.");

			//Step-3: Select Get Combo URL from operation menu
			//------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetHyperlink.Value); //Select GetHyperlink from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetComboURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with " + Caption.MenuItems.GetHyperlink.Value + " title is not opened.");

			Log.message("3." + Caption.MenuItems.GetHyperlink.Value + " is opened for object (" + dataPool.get("ObjectName") + ") from context menu.");

			//Step-4: Get link URL value from Combo URL dialog
			//------------------------------------------------
			String URL = mfilesDialog.getURLFromComboURLDialog(dataPool.get("ItemToClick"));

			Log.message("4. " + dataPool.get("ItemToClick") + " link URL value is retrived from Get Combo URL dialog. WEB URL : " + URL);

			//Step-5: Click on Web link on Combo URL dialog
			//------------------------------------------------
			mfilesDialog.clickLinksOnComboURLDialog(dataPool.get("ItemToClick")); //Clicks link in the URL
			Utils.fluentWait(driver);

			Log.message("5. (" + dataPool.get("ItemToClick") + ") link is clicked from Get Combo URL dialog.");

			//Verification : To Verify the new tab URL is same as URL retrieved from Combo URL dialog
			//-----------------------------------------------------------------------------------------
			String tabURL = Utility.getNewTabURL(driver, 1);
			Log.message(tabURL.trim());

			if (!URL.trim().equalsIgnoreCase(tabURL.trim()))
				throw new Exception("Test case Failed. The the new tab URL is not same as URL retrieved from GetHyperlink dialog from operation menu for user.");

			if (homePage.listView.itemCount() == 1 && homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				Log.pass("Test case Passed. The the new tab URL is same as URL retrieved from Combo URL dialog from operation menu.");
			else
				Log.fail("Test case Failed. The the new tab URL is not same as URL retrieved from Combo URL dialog from operation menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest101_1_2_12A

	/**
	 * 101_1_2_12B : Verify if the object displayed in new tab while user click Web link in Combo url dialog from operation menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint101", "Get Combo URL"}, 
			description = "Verify if the object displayed in new tab while user click Web link in Combo url dialog from operation menu")
	public void SprintTest101_1_2_12B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try { 



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-: Select the object and open operation menu
			//-------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-3: Select Get Combo URL from operation menu
			//------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetHyperlink.Value); //Select GetHyperlink from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetComboURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with " + Caption.MenuItems.GetHyperlink.Value + " title is not opened.");

			Log.message("3." + Caption.MenuItems.GetHyperlink.Value + " is opened for object (" + dataPool.get("ObjectName") + ") from operations menu.");

			//Step-4: Get link URL value from Combo URL dialog
			//------------------------------------------------
			String URL = mfilesDialog.getURLFromComboURLDialog(dataPool.get("ItemToClick"));

			Log.message("4. " + dataPool.get("ItemToClick") + " link URL value is retrived from Get Combo URL dialog. WEB URL : " + URL);

			//Step-5: Click on Web link on Combo URL dialog
			//------------------------------------------------
			mfilesDialog.clickLinksOnComboURLDialog(dataPool.get("ItemToClick")); //Clicks link in the URL
			Utils.fluentWait(driver);

			Log.message("5. (" + dataPool.get("ItemToClick") + ") link is clicked from Get Combo URL dialog.");

			//Verification : To Verify the new tab URL is same as URL retrieved from Combo URL dialog
			//-----------------------------------------------------------------------------------------
			String tabURL = Utility.getNewTabURL(driver, 1);
			Log.message(tabURL.trim());

			if (!URL.trim().equalsIgnoreCase(tabURL.trim()))
				throw new Exception("Test case Failed. The the new tab URL is not same as URL retrieved from Combo URL dialog from operation menu for user.");

			if (homePage.listView.itemCount() == 1 && homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				Log.pass("Test case Passed. The selected object is displayed in new tab URL.");
			else
				Log.fail("Test case Failed. The selected object is not displayed in new tab URL.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest101_1_2_12B

	/**
	 * SprintTest_42107 : Verify the redirect MFWA page while case change in GUID (To upper case)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Get Combo URL","Bug"}, 
			description = "Verify the redirect MFWA page while case change in GUID (To upper case)")
	public void SprintTest_42107(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try { 



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-: Select the object and open operation menu
			//-------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-3: Select Get Combo URL from operation menu
			//------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetHyperlink.Value); //Select GetHyperlink from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetComboURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with " + Caption.MenuItems.GetHyperlink.Value + " title is not opened.");

			Log.message("3." + Caption.MenuItems.GetHyperlink.Value + " is opened for object (" + dataPool.get("ObjectName") + ") from operations menu.");

			//Step-4: Get link URL value from Combo URL dialog
			//------------------------------------------------
			String URL = mfilesDialog.getURLFromComboURLDialog(dataPool.get("ItemToClick"));
			mfilesDialog.clickCloseButton();//Click the close button in mfiles dialog

			Log.message("4. " + dataPool.get("ItemToClick") + " link URL value is retrived from Get Combo URL dialog. WEB URL : " + URL);

			//Step-5 : Get the Object GUID from the getHyperlink dialog
			//---------------------------------------------------------
			String[] getHyperlink = URL.split("/");
			String GUID = getHyperlink[getHyperlink.length-4];
			String[] GUID1 =  GUID.split("#");
			String vaultGUID = GUID1[GUID1.length-1];
			String objectGUID = getHyperlink[getHyperlink.length-2].trim();

			String upperCaseVaultGUID = vaultGUID.toUpperCase();//convert the upper case vault GUID to lower case
			String upperCaseObjectGUID = objectGUID.toUpperCase();//convert the upper case vault GUID to lower case

			Log.message("5. Fetched the Vault " + testVault + " GUID :  " + vaultGUID +" from the Gethyperlink URL" + URL);

			String modifiedURL = URL.replace(objectGUID,upperCaseObjectGUID);
			modifiedURL = URL.replaceAll(vaultGUID,upperCaseVaultGUID);

			//Verification : To Verify the new tab URL is same as URL retrieved from Combo URL dialog
			//-----------------------------------------------------------------------------------------
			if (!Utility.logOut(driver)) //Logs out from the vault
				throw new Exception ("Log out from vault (" + testVault + ") is unsuccessful.");

			Log.message("5. Log out from vault (" + testVault + ") is successful.");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, modifiedURL, userName, password, ""); //Navigates to the URL in the hyperlink dialog

			Log.message("6. Pasted the URL copied from hyperlink dialog and logged in with valid credentials.");

			String tabURL = driver.getCurrentUrl();

			Log.message("6. Actual URL Loaded : "+tabURL.trim());

			if (!URL.trim().equalsIgnoreCase(tabURL.trim()))
				throw new Exception("Test case Failed. Launched URL is not same as URL retrieved from Combo URL dialog from operation menu for user.");

			if (homePage.listView.itemCount() == 1 && homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				Log.pass("Test case Passed. The selected object is displayed in launched URL which is modified to upper case..", driver);
			else
				Log.fail("Test case Failed. The selected object is not displayed in launched URL which is modified to upper case..", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest_42107

	/**
	 * SprintTest_30387 : Verify the redirect MFWA page while case change in GUID (To Lower case)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Get Combo URL","Bug"}, 
			description = "Verify the redirect MFWA page while case change in GUID (To Lower case)")
	public void SprintTest_30387(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try { 



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-: Select the object and open operation menu
			//-------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-3: Select Get Combo URL from operation menu
			//------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetHyperlink.Value); //Select GetHyperlink from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetComboURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with " + Caption.MenuItems.GetHyperlink.Value + " title is not opened.");

			Log.message("3." + Caption.MenuItems.GetHyperlink.Value + " is opened for object (" + dataPool.get("ObjectName") + ") from operations menu.");

			//Step-4: Get link URL value from Combo URL dialog
			//------------------------------------------------
			String URL = mfilesDialog.getURLFromComboURLDialog(dataPool.get("ItemToClick"));
			mfilesDialog.clickCloseButton();//Click the close button in mfiles dialog

			Log.message("4. " + dataPool.get("ItemToClick") + " link URL value is retrived from Get Combo URL dialog. WEB URL : " + URL);

			//Step-5 : Get the Object GUID from the getHyperlink dialog
			//---------------------------------------------------------
			String[] getHyperlink = URL.split("/");
			String GUID = getHyperlink[getHyperlink.length-4];
			String[] GUID1 =  GUID.split("#");
			String vaultGUID = GUID1[GUID1.length-1];
			String objectGUID = getHyperlink[getHyperlink.length-2].trim();

			String lowerVaultGUID = vaultGUID.toLowerCase();//convert the upper case vault GUID to lower case
			String lowerObjectGUID = objectGUID.toLowerCase();//convert the upper case vault GUID to lower case

			Log.message("5. Fetched the Vault " + testVault + " GUID :  " + vaultGUID +" from the Gethyperlink URL" + URL);

			String modifiedURL = URL.trim().replaceAll(vaultGUID, lowerVaultGUID);
			modifiedURL = modifiedURL.replaceAll(objectGUID, lowerObjectGUID);

			//Step-5 : Log out from current session
			//-------------------------------------
			if (!Utility.logOut(driver)) //Logs out from the vault
				throw new Exception ("Log out from vault (" + testVault + ") is unsuccessful.");

			Log.message("5. Log out from vault (" + testVault + ") is successful.");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, modifiedURL, userName, password, ""); //Navigates to the URL in the hyperlink dialog

			Log.message("6. Pasted the URL copied from hyperlink dialog and logged in with valid credentials.");

			//Verification : To Verify the new tab URL is same as URL retrieved from Combo URL dialog
			//-----------------------------------------------------------------------------------------
			String tabURL = driver.getCurrentUrl();

			Log.message("6. Actual URL Loaded : "+tabURL.trim());

			if (!URL.trim().equalsIgnoreCase(tabURL.trim()))
				throw new Exception("Test case Failed. Launched URL is not same as URL retrieved from Combo URL dialog from operation menu for user.");

			if (homePage.listView.itemCount() == 1 && homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				Log.pass("Test case Passed. The selected object is displayed in launched URL which is modified to lower case..", driver);
			else
				Log.fail("Test case Failed. The selected object is not displayed in launched URL which is modified to lower case..", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest_30387

	/**
	 * SprintTest_30388 : Verify the redirect MFWA page while case change in GUID (Upper case To Lower case and Lower case To Upper case)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Get Combo URL","Bug"}, 
			description = "Verify the redirect MFWA page while case change in GUID (Upper case To Lower case and Lower case To Upper case)")
	public void SprintTest_30388(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try { 



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-: Select the object and open operation menu
			//-------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-3: Select Get Combo URL from operation menu
			//------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetHyperlink.Value); //Select GetHyperlink from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetComboURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with " + Caption.MenuItems.GetHyperlink.Value + " title is not opened.");

			Log.message("3." + Caption.MenuItems.GetHyperlink.Value + " is opened for object (" + dataPool.get("ObjectName") + ") from operations menu.");

			//Step-4: Get link URL value from Combo URL dialog
			//------------------------------------------------
			String URL = mfilesDialog.getURLFromComboURLDialog(dataPool.get("ItemToClick"));
			mfilesDialog.clickCloseButton();//Click the close button in mfiles dialog

			Log.message("4. " + dataPool.get("ItemToClick") + " link URL value is retrived from Get Combo URL dialog. WEB URL : " + URL);

			//Step-5 : Get the Object GUID from the getHyperlink dialog
			//---------------------------------------------------------
			String[] getHyperlink = URL.split("/");
			String GUID = getHyperlink[getHyperlink.length-4];
			String[] GUID1 =  GUID.split("#");
			String vaultGUID = GUID1[GUID1.length-1];

			//Step-5: Click on Web link on Combo URL dialog
			//------------------------------------------------
			String convertedGUID = "";

			for (int i = 0; i < vaultGUID.length(); i++)
				if (Character.isUpperCase(vaultGUID.charAt(i)))
					convertedGUID += Character.toLowerCase(vaultGUID.charAt(i));
				else if(Character.isLowerCase(vaultGUID.charAt(i)))
					convertedGUID += Character.toUpperCase(vaultGUID.charAt(i));
				else
					convertedGUID += URL.charAt(i);

			String modifiedURL = URL.replaceAll(vaultGUID,convertedGUID);

			driver.get(modifiedURL);//Launches the convertedURL is get from Get Hyperlink Dialog

			Log.message("5. (" + dataPool.get("ItemToClick") + ") URL : '"+ modifiedURL +"' is launched after modified (Upper case To Lower case and Lower case To Upper case)  which is Get from GetHyperlink dialog.");

			//Verification : To Verify the new tab URL is same as URL retrieved from Combo URL dialog
			//-----------------------------------------------------------------------------------------
			String tabURL = driver.getCurrentUrl();

			Log.message("6. Actual URL Loaded : "+tabURL.trim());

			if (!URL.trim().equalsIgnoreCase(tabURL.trim()))
				throw new Exception("Test case Failed. Launched URL is not same as URL retrieved from Combo URL dialog from operation menu for user.");

			if (homePage.listView.itemCount() == 1 && homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				Log.pass("Test case Passed. The selected object is displayed in launched URL which is modified (Upper case To Lower case and Lower case To Upper case).", driver);
			else
				Log.fail("Test case Failed. The selected object is not displayed in launched URL which is modified (Upper case To Lower case and Lower case To Upper case)..", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest_30388

	/**
	 * 101_1_2_13A : Verify if Get Combo URL option is enabled in context menu while user multi select objects using shift key in listing view
	 * @param 
	 * @return
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint101", "Get Combo URL"}, 
			description = "Verify if Get Combo URL option is enabled in context menu while user multi select objects using shift key in listing view")
	public void SprintTest101_1_2_13A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports multi-select.");

		driver = null; 

		try { 



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1 : Login to MFWA
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String vault = xmlParameters.getParameter("VaultName");
			driver.get(loginURL); //Launches with the URL
			Utils.fluentWait(driver);
			//Checks if Login.aspx page is loaded
			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Browser is not navigated to the Login page.");
			//Instantiates LoginPage class
			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), vault); //Logs into application
			Utils.fluentWait(driver);
			Log.message("1. Logged into MFWA as " + dataPool.get("Admin") +" user.");

			//Step-2 : Navigate to specified View
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));
			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-3 : Right click on the Multi select object in listing view
			homePage.listView.shiftclickMultipleItemsByIndex(Integer.parseInt(dataPool.get("StartIndex")),Integer.parseInt(dataPool.get("EndIndex")));
			Utils.fluentWait(driver);
			homePage.listView.rightClickItemByIndex(Integer.parseInt(dataPool.get("EndIndex")));
			Utils.fluentWait(driver);
			Log.message("3. Right clicked on an object header and context menu is opened.");

			//Verification : To Verify Get Combo URL option is available in context menu
			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.GetHyperlink.Value))
				Log.pass("Test case Passed. Get Combo URL is disabled for multi slelected objects in context menu for  " + dataPool.get("Admin") + " user .");
			else
				Log.fail("Test case Failed. Get Combo URL is not disabled for multi slelected objects in context menu for  " + dataPool.get("Admin") + " user.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest101_1_2_13A

	/**
	 * 101_1_2_13B : Verify if Get Combo URL option is enabled in operation menu while user multi select objects using shift key in listing view
	 * @param 
	 * @return
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint101", "Get Combo URL"}, 
			description = "Verify if Get Combo URL option is enabled in operation menu while user multi select objects using shift key in listing view")
	public void SprintTest101_1_2_13B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("SAFARI") || driverType.equalsIgnoreCase("IE"))
			throw new SkipException(driverType +" driver does not support multi select");

		driver = null; 

		try { 



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1 : Login to MFWA
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String vault = xmlParameters.getParameter("VaultName");
			driver.get(loginURL); //Launches with the URL
			Utils.fluentWait(driver);
			//Checks if Login.aspx page is loaded
			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Browser is not navigated to the Login page.");
			//Instantiates LoginPage class
			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), vault); //Logs into application
			Utils.fluentWait(driver);
			Log.message("1. Logged into MFWA as " + dataPool.get("Admin") +" user.");

			//Step-2 : Navigate to specified View
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));
			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-3 : Right click on the Multi select object in listing view
			homePage.listView.shiftclickMultipleItemsByIndex(Integer.parseInt(dataPool.get("StartIndex")),Integer.parseInt(dataPool.get("EndIndex")));
			Utils.fluentWait(driver);
			Log.message("3. Object multi selected using Shift Key.");

			//Verification : To Verify Get Combo URL option is available in operation menu
			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.GetHyperlink.Value))
				Log.pass("Test case Passed. Get Combo URL is disabled for multi slelected objects in operation menu for  " + dataPool.get("Admin") + " user .");
			else
				Log.fail("Test case Failed. Get Combo URL is not disabled for multi slelected objects in operation menu for  " + dataPool.get("Admin") + " user.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest101_1_2_13B

	/**
	 * 101_1_2_14A : Verify if GetComboURL dialog from context menu get closed while user press Escape key
	 * @param 
	 * @return
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint101", "Get Combo URL"}, 
			description = "Verify if GetComboURL dialog from context menu get closed while user press Escape key")
	public void SprintTest101_1_2_14A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try { 



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1 : Login to MFWA
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String vault = xmlParameters.getParameter("VaultName");
			driver.get(loginURL); //Launches with the URL
			Utils.fluentWait(driver);
			//Checks if Login.aspx page is loaded
			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Browser is not navigated to the Login page.");
			//Instantiates LoginPage class
			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), vault); //Logs into application
			Utils.fluentWait(driver);
			Log.message("1. Logged into MFWA as " + dataPool.get("Admin") +" user.");

			//Step-2 : Navigate to specified View
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));
			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-3: Select the object and open Get Combo URL dialog from context menu
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Right Clicks on an object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");
			Log.message("3. Right clicked on the object and context menu is opened.");

			//Step-4: Verify Get Combo URL option is enabled in context menu
			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.GetHyperlink.Value))
				throw new Exception("Get Combo URL is not enabled in Context menu.");
			Log.message("4. Get Combo URL is enabled in Context menu.");

			//Step-5: Select Get Combo URL option from context menu
			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetHyperlink.Value);
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class	
			if (!mfilesDialog.isGetComboURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with " + Caption.MenuItems.GetHyperlink.Value + " title is not opened.");
			Log.message("5. Get Combo URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-6: Press Escape key
			mfilesDialog.clickEscapeKey();
			Utils.fluentWait(driver);
			Log.message("6. Escape key is pressed.");

			//Verification : To Verify the new tab URL is same as URL retrieved from Combo URL dialog
			if (!mfilesDialog.isGetComboURLDialogOpened())
				Log.pass("Test case Passed. The Combo URl dialog is get closed while pressing Escape key from context menu for " + dataPool.get("Admin")+ " user.");
			else
				Log.fail("Test case Failed. The Combo URl dialog is not get closed while pressing Escape key from context menu for " + dataPool.get("Admin")+ " user.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest101_1_2_14A

	/**
	 * 101_1_2_14B : Verify if GetComboURL dialog from operation menu get closed while user press Escape key
	 * @param 
	 * @return
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint101", "Get Combo URL"}, 
			description = "Verify if GetComboURL dialog from operation menu get closed while user press Escape key")
	public void SprintTest101_1_2_14B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try { 



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1 : Login to MFWA
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String vault = xmlParameters.getParameter("VaultName");
			driver.get(loginURL); //Launches with the URL
			Utils.fluentWait(driver);
			//Checks if Login.aspx page is loaded
			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Browser is not navigated to the Login page.");
			//Instantiates LoginPage class
			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), vault); //Logs into application
			Utils.fluentWait(driver);
			Log.message("1. Logged into MFWA as " + dataPool.get("Admin") +" user.");

			//Step-2 : Navigate to specified View
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));
			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-3: Select the object in listing view
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");
			Log.message("3. Object selected in list view.");

			//Step-4: Select Get Combo URL from operation menu
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetHyperlink.Value);
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class
			if (!mfilesDialog.isGetComboURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with " + Caption.MenuItems.GetHyperlink.Value + " title is not opened.");
			Log.message("4. Get Combo URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-5: Press Escape key
			mfilesDialog.clickEscapeKey();
			Utils.fluentWait(driver);
			Log.message("5. Escape key is pressed.");

			//Verification : To Verify the new tab URL is same as URL retrieved from Combo URL dialog
			if (!mfilesDialog.isGetComboURLDialogOpened())
				Log.pass("Test case Passed. The Combo URl dialog is get closed while pressing Escape key from operation menu for " + dataPool.get("Admin")+ " user.");
			else
				Log.fail("Test case Failed. The Combo URl dialog is not get closed while pressing Escape key from operation menu for " + dataPool.get("Admin")+ " user.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest101_1_2_14B

	/**
	 * SprintTest34317 : Verify the SFD GUID in Recently Accessed by Me & Checked out to me view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "GetHyperlink","US-9920"}, 
			description = "Verify the SFD GUID in Recently Accessed by Me & Checked out to me view.")
	public void SprintTest34317(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to the 'Search only: documents' view
			//------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"),dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select object & checkout the object in that search view
			//----------------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectName"));//Select the object from the list view
			if(!(homePage.listView.getIconURLByItemName(dataPool.get("ObjectName")).toUpperCase().contains("CHECKEDOUT")))
				homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.CheckOut.Value);//Select the check out option from the operations menu

			Log.message("2. Checked out the selected object " + dataPool.get("ObjectName") + " in the search view.");

			//Step-3 : Navigate to the Specified view
			//---------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("SpecifiedView"));

			Log.message("3. Navigated to the " + dataPool.get("SpecifiedView") + " view.");

			//Step-4 : Select the object in Specified view
			//--------------------------------------------
			homePage.listView.rightClickItem(dataPool.get("ObjectName"));//Select the object in specified view

			Log.message("4. Right clicked :  " + dataPool.get("ObjectName") + " object in specified : " + dataPool.get("SpecifiedView")  + " view.");

			//Step-5 : Click the 'GetHyperlink' option from the Context menu
			//--------------------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetHyperlink.Value);

			Log.message("5. Selected the " + Caption.MenuItems.GetHyperlink.Value +" option from the context menu.");

			//Step-6 : Get the current url from the gethyperlink
			//--------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetComboURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with " + Caption.MenuItems.GetHyperlink.Value + " title is not opened.");

			Log.message("6." + Caption.MenuItems.GetHyperlink.Value + " is opened for object (" + dataPool.get("ObjectName") + ") from context menu.");

			//Step-7: Get link URL value from Combo URL dialog
			//------------------------------------------------
			String URL = mfilesDialog.getURLFromComboURLDialog(dataPool.get("ItemToClick"));

			Log.message("7. " + dataPool.get("ItemToClick") + "  URL value is retrived from Get Hyperlink dialog. WEB URL : " + URL);

			//Step-8 : Get the Object GUID from the getHyperlink dialog
			//---------------------------------------------------------
			String[] getHyperlink = URL.split("/");
			String ObjectGUID1 = getHyperlink[getHyperlink.length-2];

			Log.message("8. Specified object " + dataPool.get("ObjectName") + " GUID is :  " + ObjectGUID1);

			//Step-9 : Close the 'GetHyperlink' dialog
			//----------------------------------------
			mfilesDialog.clickCloseButton();

			Log.message("9. Closed the 'GetHyperlink' m-files dialog.");

			//Step-10 : Select the 'Get M-fiels web url' dialog for the specified object
			//--------------------------------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value);

			Log.message("10. Selected the " + Caption.MenuItems.GetMFilesWebURL.Value + " from the Operations menu.");

			//Step-11 : Get the current url from the GetM-files web URL
			//---------------------------------------------------------
			mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			//verify if get-mfiles web url dialog is opened or not
			//----------------------------------------------------
			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with " + Caption.MenuItems.GetHyperlink.Value + " title is not opened.");

			Log.message("11." + Caption.MenuItems.GetHyperlink.Value + " is opened for object (" + dataPool.get("ObjectName") + ") from context menu.");

			//Verify the default layout is selected in the Get M-files web url
			//----------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			//Get the URL from the M-files web url
			//------------------------------------
			String hyperlinkURL = mfilesDialog.getHyperlink();// Get hyperlink from GetM-files web url dialog

			String[] getMfilesURL = hyperlinkURL.split("/");
			String ObjectGUID2  = getMfilesURL[getMfilesURL.length-2];

			//Verification : Verify if Object GUID is same in both GetHyperlink & GetMfiles web url links
			//-------------------------------------------------------------------------------------------
			if(ObjectGUID1.trim().equals(ObjectGUID2.trim()))
				Log.pass("Test Case Passed.Selected object : " + dataPool.get("ObjectName") + " is displayed with same GUID in both Gethyperlink & GetM-fiels web url.", driver);
			else
				Log.fail("Test Case Failed.Selected object : " + dataPool.get("ObjectName") + " is not displayed with same GUID in both Gethyperlink & GetM-fiels web url. expeceted : " + ObjectGUID1 + " actual : " + ObjectGUID2, driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest34317

	/**
	 * SprintTest34316 : Verify the SFD GUID in favorites & Common view.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "GetHyperlink"}, 
			description = "Verify the SFD GUID in favorites & Common view.")
	public void SprintTest34316(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to the 'Search only: documents' view
			//------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"),dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select object & checkout the object in that search view
			//----------------------------------------------------------------
			homePage.listView.rightClickItem(dataPool.get("ObjectName"));//Select the object from the list view
			if(dataPool.get("SpecifiedView").equalsIgnoreCase("Favorites")){
				homePage.listView.clickContextMenuItem(Caption.MenuItems.AddToFavorites.Value);

				Log.message("2. Selected object " + dataPool.get("ObjectName") + " and the object in favorites view using " + Caption.MenuItems.AddToFavorites.Value + " option in context menu.",driver);

				//Verify if 'One object was affected' favorites dialog is displayed or not
				//------------------------------------------------------------------------
				if(MFilesDialog.exists(driver)){
					MFilesDialog mfilesdialog = new MFilesDialog(driver,"M-Files Web");
					mfilesdialog.clickOkButton();//Click the ok button in M-files dialog
				}

				//Step-3 : Navigate to the Specified view
				//---------------------------------------
				homePage.taskPanel.clickItem(dataPool.get("SpecifiedView"));

				Log.message("3. Navigated to the " + dataPool.get("SpecifiedView") + " view.");

				//Step-4 : Select the object in Specified view
				//--------------------------------------------
				homePage.listView.rightClickItem(dataPool.get("ObjectName"));//Select the object in specified view

				Log.message("4. Right clicked :  " + dataPool.get("ObjectName") + " object in specified : " + dataPool.get("SpecifiedView")  + " view.");

			}

			//Step-5 : Click the 'GetHyperlink' option from the Context menu
			//--------------------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetHyperlink.Value);

			Log.message("5. Selected the " + Caption.MenuItems.GetHyperlink.Value +" option from the context menu.");

			//Step-6 : Get the current url from the gethyperlink
			//--------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetComboURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with " + Caption.MenuItems.GetHyperlink.Value + " title is not opened.");

			Log.message("6." + Caption.MenuItems.GetHyperlink.Value + " is opened for object (" + dataPool.get("ObjectName") + ") from context menu.");

			//Step-7: Get link URL value from Combo URL dialog
			//------------------------------------------------
			String URL = mfilesDialog.getURLFromComboURLDialog(dataPool.get("ItemToClick"));

			Log.message("7. " + dataPool.get("ItemToClick") + "  URL value is retrived from Get Hyperlink dialog. WEB URL : " + URL);

			//Step-8 : Get the Object GUID from the getHyperlink dialog
			//---------------------------------------------------------
			String[] getHyperlink = URL.split("/");
			String ObjectGUID1 = getHyperlink[getHyperlink.length-2];

			Log.message("8. Specified object " + dataPool.get("ObjectName") + " GUID is :  " + ObjectGUID1);

			//Step-9 : Close the 'GetHyperlink' dialog
			//----------------------------------------
			mfilesDialog.clickCloseButton();//Close button in m-files dialog

			Log.message("9. Closed the 'GetHyperlink' m-files dialog.");

			//Step-10 : Select the 'Get M-fiels web url' dialog for the specified object
			//--------------------------------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value);

			Log.message("10. Selected the " + Caption.MenuItems.GetMFilesWebURL.Value + " from the Operations menu.");

			//Step-11 : Get the current url from the GetM-files web URL
			//---------------------------------------------------------
			mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			//verify if get-mfiles web url dialog is opened or not
			//----------------------------------------------------
			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with " + Caption.MenuItems.GetMFilesWebURL.Value + " title is not opened.");

			Log.message("11." + Caption.MenuItems.GetMFilesWebURL.Value + " is opened for object (" + dataPool.get("ObjectName") + ") from context menu.");

			//Verify the default layout is selected in the Get M-files web url
			//----------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			//Get the URL from the M-files web url
			//------------------------------------
			String hyperlinkURL = mfilesDialog.getHyperlink();// Get hyperlink from GetM-files web url dialog

			String[] getMfilesURL = hyperlinkURL.split("/");
			String ObjectGUID2  = getMfilesURL[getMfilesURL.length-2];

			//Verification : Verify if Object GUID is same in both GetHyperlink & GetMfiles web url links
			//-------------------------------------------------------------------------------------------
			if(ObjectGUID1.trim().equals(ObjectGUID2.trim()))
				Log.pass("Test Case Passed.Selected object : " + dataPool.get("ObjectName") + " is displayed with same GUID in both Gethyperlink & GetM-fiels web url.", driver);
			else
				Log.fail("Test Case Failed.Selected object : " + dataPool.get("ObjectName") + " is not displayed with same GUID in both Gethyperlink & GetM-fiels web url. expeceted : " + ObjectGUID1 + " actual link :  " + ObjectGUID2, driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest34316

	/**
	 * SprintTest34314 : Verify the SFD GUID in Assigned to me view.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "GetHyperlink"}, 
			description = "Verify the SFD GUID in Assigned to me view.")
	public void SprintTest34314(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Select New Assignment from New menu
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName).toString(); //Name of the object with current method date & time
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentBasicClass.Value); //Sets the Class name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("1. Created the new Assignment from menubar.", driver);

			//Step-2 : Navigate to the Specified view
			//---------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("SpecifiedView"));

			Log.message("2. Navigated to the " + dataPool.get("SpecifiedView") + " view.");

			//Step-5 : Click the 'GetHyperlink' option from the Context menu
			//--------------------------------------------------------------
			homePage.listView.clickItem(assigName);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetHyperlink.Value);

			Log.message("5. Selected the " + Caption.MenuItems.GetHyperlink.Value +" option from the context menu.");

			//Step-6 : Get the current url from the gethyperlink
			//--------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetComboURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with " + Caption.MenuItems.GetHyperlink.Value + " title is not opened.");

			Log.message("6." + Caption.MenuItems.GetHyperlink.Value + " is opened for object (" + dataPool.get("ObjectName") + ") from context menu.");

			//Step-7: Get link URL value from Combo URL dialog
			//------------------------------------------------
			String URL = mfilesDialog.getURLFromComboURLDialog(dataPool.get("ItemToClick"));

			Log.message("7. " + dataPool.get("ItemToClick") + "  URL value is retrived from Get Hyperlink dialog. WEB URL : " + URL);

			//Step-8 : Get the Object GUID from the getHyperlink dialog
			//---------------------------------------------------------
			String[] getHyperlink = URL.split("/");
			String ObjectGUID1 = getHyperlink[getHyperlink.length-2];

			Log.message("8. Specified object " + dataPool.get("ObjectName") + " GUID is :  " + ObjectGUID1);

			//Step-9 : Close the 'GetHyperlink' dialog
			//----------------------------------------
			mfilesDialog.clickEscapeKey();

			Log.message("9. Closed the 'GetHyperlink' m-files dialog.");

			//Step-10 : Select the 'Get M-fiels web url' dialog for the specified object
			//--------------------------------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value);

			Log.message("10. Selected the " + Caption.MenuItems.GetMFilesWebURL.Value + " from the Operations menu.");

			//Step-11 : Get the current url from the GetM-files web URL
			//---------------------------------------------------------
			mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			//verify if get-mfiles web url dialog is opened or not
			//----------------------------------------------------
			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with " + Caption.MenuItems.GetMFilesWebURL.Value + " title is not opened.");

			Log.message("11." + Caption.MenuItems.GetMFilesWebURL.Value + " is opened for object (" + dataPool.get("ObjectName") + ") from context menu.");

			//Verify the default layout is selected in the Get M-files web url
			//----------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			//Get the URL from the M-files web url
			//------------------------------------
			String hyperlinkURL = mfilesDialog.getHyperlink();// Get hyperlink from GetM-files web url dialog

			String[] getMfilesURL = hyperlinkURL.split("/");
			String ObjectGUID2  = getMfilesURL[getMfilesURL.length-2];

			//Verification : Verify if Object GUID is same in both GetHyperlink & GetMfiles web url links
			//-------------------------------------------------------------------------------------------
			if(ObjectGUID1.trim().equals(ObjectGUID2.trim()))
				Log.pass("Test Case Passed.Selected object : " + dataPool.get("ObjectName") + " is displayed with same GUID in both Gethyperlink & GetM-fiels web url.", driver);
			else
				Log.fail("Test Case Failed.Selected object : " + dataPool.get("ObjectName") + " is not displayed with same GUID in both Gethyperlink & GetM-fiels web url. expeceted : " + ObjectGUID1 + " actual link :  " + ObjectGUID2, driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest34316


	/**
	 * SprintTest34312 : Verify if version number of latest SFD is displayed in history view get hyperlink [gear menu]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "GetHyperlink"}, 
			description = "Verify if version number of latest SFD is displayed in history view get hyperlink [gear menu].")
	public void SprintTest34312(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Select any object from the  from New menu
			//--------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to the " +  viewToNavigate + " view.");

			//Step-2 : Click the 'history' option from the operations menu
			//------------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectName"));//Selected the object name 
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.History.Value);//Click the history option from the operations menu

			Log.message("2. Selected the object " + dataPool.get("ObjectName") + " from the operations menu.");

			//Step-3: Select the latest version of the object from the histroy view
			//---------------------------------------------------------------------
			if (!homePage.listView.rightClickItemByIndex(0)) //click the latest version
				throw new Exception("Latest version of an object is not selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetHyperlink.Value);//Select the gethyperlink option from the context menu

			Log.message("3. Selected the " + Caption.MenuItems.GetHyperlink.Value +" option from the context menu for the object." + dataPool.get("ObjectName"));

			//Step-4 : Get the current url from the gethyperlink
			//--------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetComboURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with " + Caption.MenuItems.GetHyperlink.Value + " title is not opened.");

			Log.message("3." + Caption.MenuItems.GetHyperlink.Value + " is opened for object (" + dataPool.get("ObjectName") + ") from context menu.");

			//Step-7: Get link URL value from Combo URL dialog
			//------------------------------------------------
			String URL = mfilesDialog.getURLFromComboURLDialog(dataPool.get("ItemToClick"));

			Log.message("4. " + dataPool.get("ItemToClick") + "  URL value is retrived from Get Hyperlink dialog. WEB URL : " + URL);

			//Step-8 : Get the Object GUID from the getHyperlink dialog
			//---------------------------------------------------------
			String[] getHyperlink = URL.split("/");
			String latestVersion1 = getHyperlink[getHyperlink.length-1];

			Log.message("5. Selected object " + dataPool.get("ObjectName") + " latest version is :  " + latestVersion1);

			//Step-9 : Close the 'GetHyperlink' dialog
			//----------------------------------------
			mfilesDialog.clickCloseButton();

			Log.message("9. Closed the 'GetHyperlink' m-files dialog.");

			//Verification : Verify if Object GUID is same in both GetHyperlink & GetMfiles web url links
			//-------------------------------------------------------------------------------------------
			if(latestVersion1.trim().equalsIgnoreCase("latest"))
				Log.pass("Test Case Passed.Latest version of the selected object : " + dataPool.get("ObjectName") + " is displayed as expected ." + latestVersion1 , driver);
			else
				Log.fail("Test Case Failed.Selected object : " + dataPool.get("ObjectName") + " is not displayed with same GUID in both Gethyperlink & GetM-fiels web url. expeceted : " + latestVersion1 + " actual link :  " + latestVersion1, driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest34312


	/**
	 * SprintTest34326 : Verify if SFD is opened in checked out mode in new tab with GetHyperlink.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "GetHyperlink"}, 
			description = "Verify if SFD is opened in checked out mode in new tab with GetHyperlink.")
	public void SprintTest34326(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Select any object from the  from New menu
			//--------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to the " +  viewToNavigate + " view.");

			//Step-2 : Click the 'check out' option from the operations menu
			//------------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectName"));//Selected the object name 
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);//Click the history option from the operations menu

			Log.message("2. Checked out the selected the object " + dataPool.get("ObjectName") + " from the task pane.");

			//Step-3 : Click the gethyperlink from the context menu
			//-----------------------------------------------------
			homePage.listView.rightClickItem(dataPool.get("ObjectName"));//right click the object name 
			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetHyperlink.Value);//Select the gethyperlink option from the context menu

			Log.message("3. Selected the " + Caption.MenuItems.GetHyperlink.Value +" option from the context menu for an object : " + dataPool.get("ObjectName"));

			//Step-4 : Get the current url from the gethyperlink
			//--------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetComboURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with " + Caption.MenuItems.GetHyperlink.Value + " title is not opened.");

			Log.message("4." + Caption.MenuItems.GetHyperlink.Value + " is opened for object (" + dataPool.get("ObjectName") + ") from context menu.");

			//Step7: Get link URL value from Combo URL dialog
			//------------------------------------------------
			String URL = mfilesDialog.getURLFromComboURLDialog(dataPool.get("ItemToClick"));

			Log.message("5. " + dataPool.get("ItemToClick") + " link URL value is retrived from Get Combo URL dialog. WEB URL : " + URL);

			//Step-5: Click on Web link on Combo URL dialog
			//------------------------------------------------
			mfilesDialog.clickLinksOnComboURLDialog(dataPool.get("ItemToClick")); //Clicks link in the URL
			Utils.fluentWait(driver);

			Log.message("5. (" + dataPool.get("ItemToClick") + ") link is clicked from GetHyperlink dialog.");

			//Step-6 : To Verify the new tab URL is same as URL retrieved from Combo URL dialog
			//-----------------------------------------------------------------------------------------
			String tabURL = Utility.getNewTabURL(driver, 1);
			Log.message(tabURL.trim());

			//Verify if URL is same as 
			if (!URL.trim().equalsIgnoreCase(tabURL.trim()))
				throw new Exception("Test case Failed. The the new tab URL is not same as URL retrieved from Combo URL dialog from operation menu for user.");

			Log.message("6. Hyperlink URL is launched in new web page. URL :  " +tabURL , driver);

			//Verify if selected item is displayed in the new web page 
			if (!(homePage.listView.itemCount() == 1 && homePage.listView.isItemExists(dataPool.get("ObjectName"))))
				throw new Exception("Selected object : " + dataPool.get("ObjectName") + " is not displayed in launched URL.");

			//Verification : Verify if selected object is in Checked out mode or not
			//----------------------------------------------------------------------
			if(ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.message("Test Case Passed. Selected SFD is object : " + dataPool.get("ObjectName") + " is opened in checked out mode when hyperlink is opened in new tab." + tabURL , driver);
			else
				Log.message("Test Case Failed.Selected SFD is object : " + dataPool.get("ObjectName") + " not in checked out mode when hyperlink is opened in new tab.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest34326


	/**
	 * SprintTest34320 : Verify if old hyperlink URL value with SFD ID is working correctly with get combo URL
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "GetHyperlink","US-9920"}, 
			description = "Verify if old hyperlink URL value with SFD ID is working correctly with GetHyperlink.")
	public void SprintTest34320(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to the 'Search only: documents' view
			//------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"),dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select object & checkout the object in that search view
			//----------------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectName"));//Select the object from the list view
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value);//Select the check out option from the operations menu

			Log.message("2. Opened the metadatacard for the selected object : " + dataPool.get("ObjectName"));

			//Step-3 : Fetch the values from the selected object metadatacard
			//---------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the pop-out metadatacard
			int objectVersion = metadataCard.getVersion();
			int objectID = metadataCard.getObjectID();
			metadataCard.clickCancelBtn();

			String objectIDVersion =  " " +dataPool.get("ObjectType") +"/" + objectID + "/"+ objectVersion;

			Log.message("3. Object type,Version and ID values are fetched from the selected object : "+ dataPool.get("ObjectName") + " opened metadatacard. ", driver);

			//Step-4 : Select the object in Specified view
			//--------------------------------------------
			homePage.listView.rightClickItem(dataPool.get("ObjectName"));//Select the object in specified view

			Log.message("4. Right clicked the selected object :  " + dataPool.get("ObjectName") );

			//Step-5 : Click the 'GetHyperlink' option from the Context menu
			//--------------------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetHyperlink.Value);

			Log.message("5. Selected the " + Caption.MenuItems.GetHyperlink.Value +" option from the context menu.");

			//Step-6 : Get the current url from the gethyperlink
			//--------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetComboURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with " + Caption.MenuItems.GetHyperlink.Value + " title is not opened.");

			Log.message("6." + Caption.MenuItems.GetHyperlink.Value + " is opened for object (" + dataPool.get("ObjectName") + ") from context menu.");

			//Step-7: Get link URL value from Combo URL dialog
			//------------------------------------------------
			String URL = mfilesDialog.getURLFromComboURLDialog(dataPool.get("ItemToClick"));

			Log.message("7. " + dataPool.get("ItemToClick") + "  URL value is retrived from Get Hyperlink dialog. WEB URL : " + URL);

			//Step-8 : Get the Object GUID from the getHyperlink dialog
			//---------------------------------------------------------
			String[] getHyperlink = URL.split("/");
			String ObjectGUID = getHyperlink[getHyperlink.length-2] + "/" + getHyperlink[getHyperlink.length-1];

			Log.message("8. Fetched the object " + dataPool.get("ObjectName") + " GUID :  " + ObjectGUID +" from the Gethyperlink URL" + URL);

			//Step-9 : Close the 'GetHyperlink' dialog
			//----------------------------------------
			mfilesDialog.clickCloseButton();

			Log.message("9. Closed the 'GetHyperlink' m-files dialog.");

			//Step-10 : Remove the Object GUID and replace with the object type/id/version
			//----------------------------------------------------------------------------
			String modifiedURL = URL.replaceAll(ObjectGUID,objectIDVersion.trim());
			driver.get(modifiedURL);

			Log.message("10. Launched the modified URL : " + modifiedURL + " with object ID,version & type in new tab.", driver);

			//Verification : Verify if selected object is displayed in the gethyperlink url as expected
			//-----------------------------------------------------------------------------------------
			if (homePage.listView.itemCount() == 1 && homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				Log.pass("Test case Passed. SFD object : " + dataPool.get("ObjectName") + " is displayed successfully after replacing the object GUID with Object ID,type & version.");
			else
				Log.fail("Test case Failed.SFD object : " + dataPool.get("ObjectName") + " is not displayed successfully after replacing the object GUID with Object ID,type & version. .", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest34320


	/**
	 * SprintTest34328 : Verify if the SFD GUID with get combo URL is not changed on checking in an SFD
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "GetHyperlink"}, 
			description = "Verify if the SFD GUID with get combo URL is not changed on checking in an SFD.")
	public void SprintTest34328(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Select any object from the  from New menu
			//--------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to the " +  viewToNavigate + " view.");

			//Step-2 : Click the 'history' option from the operations menu
			//------------------------------------------------------------
			homePage.listView.rightClickItem(dataPool.get("ObjectName"));//Selected the object name 

			Log.message("2. Right clicked the object " + dataPool.get("ObjectName") + " in the search view.");

			//Step-3: Select the latest version of the object from the histroy view
			//---------------------------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetHyperlink.Value);//Select the gethyperlink option from the context menu

			Log.message("3. Selected the " + Caption.MenuItems.GetHyperlink.Value +" option from the context menu for the object." + dataPool.get("ObjectName"));

			//Step-4 : Get the current url from the gethyperlink
			//--------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetComboURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with " + Caption.MenuItems.GetHyperlink.Value + " title is not opened.");

			Log.message("4." + Caption.MenuItems.GetHyperlink.Value + " is opened for object (" + dataPool.get("ObjectName") + ") from context menu.");

			//Step-7: Get link URL value from Combo URL dialog
			//------------------------------------------------
			String URL = mfilesDialog.getURLFromComboURLDialog(dataPool.get("ItemToClick"));
			mfilesDialog.clickCloseButton();

			Log.message("5. " + dataPool.get("ItemToClick") + "  URL value is retrived from Get Hyperlink dialog. WEB URL : " + URL);

			//Step-6 : Get the Object GUID from the getHyperlink dialog
			//---------------------------------------------------------
			String[] getHyperlink = URL.split("/");
			String ObjectGUID1 = getHyperlink[getHyperlink.length-2];

			Log.message("6. Selected object " + dataPool.get("ObjectName") + " GUID is :  " + ObjectGUID1);

			//Step-7 : Select the object in list view
			//----------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectName"));//Selected the object name
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);//Check out the object

			Log.message("7. Selected the object & Checkout the object from the list view." + dataPool.get("ObjectName") );

			//Step-8 : Modified the selected object
			//-------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectName"));//Selected the object name
			MetadataCard metadataCard = new MetadataCard(driver,true);//Instantiate the right pane metadatacard
			metadataCard.addNewProperty(dataPool.get("addProperty"));//Add the new property in opened metadatacard
			metadataCard.saveAndClose();//Save the metadatacard
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.CheckIn.Value);

			Log.message("8. Modified the selected object : " + dataPool.get("ObjectName") + " by adding the new property & Checkin the object.");

			//Step-9 : Fetch the object GUID for the selected object from the gethyperlink url  
			//--------------------------------------------------------------------------------
			homePage.listView.rightClickItem(dataPool.get("ObjectName"));//Selected the object name
			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetHyperlink.Value);

			Log.message("9. Right clicked the " +  Caption.MenuItems.GetHyperlink.Value + " from the context menu.");

			//Step-10 : fetch the object GUID from the gethyperlink url
			//---------------------------------------------------------
			String modifiedObjectURL = mfilesDialog.getURLFromComboURLDialog(dataPool.get("ItemToClick"));//Click the web link in gethyperlink dialog
			mfilesDialog.clickCloseButton();//close the m-files dialog

			String[] hyperlink = modifiedObjectURL.split("/");//Split the gethyperlink url 
			String ObjectGUID2 = hyperlink[hyperlink.length-2];//fetch the object GUID 

			Log.message("10. Fetched the object GUID : " + ObjectGUID2 + " from the gethyperlink url.");

			//Verification : Verify if Object GUID is same in both GetHyperlink & GetMfiles web url links
			//-------------------------------------------------------------------------------------------
			if(ObjectGUID1.trim().equalsIgnoreCase(ObjectGUID2.trim()))
				Log.pass("Test Case Passed.Selected object : " + dataPool.get("ObjectName") + " GUID is not changed after checking the SFD ." + ObjectGUID1 , driver);
			else
				Log.fail("Test Case Failed.Selected object : " + dataPool.get("ObjectName") + " GUID is changed when checking the SFD.expected : " + ObjectGUID1 + " actual GUID :  " + ObjectGUID2 , driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest34328


	/**
	 * SprintTest129541 : Verify if hyperlink is working as expected for object name as coding characters.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"GetHyperlink"}, 
			description = "Verify if hyperlink is working as expected for object name as coding characters.")
	public void SprintTest129541(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);
			homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Select any object from the  from New menu
			//--------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to the " +  viewToNavigate + " view.");

			//Step-2 : Click the 'history' option from the operations menu
			//------------------------------------------------------------
			homePage.listView.rightClickItem(dataPool.get("ObjectName"));//Selected the object name 

			String extension = "";

			if(dataPool.get("ObjectName").contains("."))
				extension = "." + dataPool.get("ObjectName").split("\\.")[1];

			Log.message("2. Right clicked the object " + dataPool.get("ObjectName") + " in the search view.");

			//Step-3 : Renamed the selected object in the list view
			//-----------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Rename.Value);//Select the Rename option in context menu
			MFilesDialog mfilesdialog = new MFilesDialog(driver,"Rename");//Instantiate the mfiles dialog
			mfilesdialog.rename(dataPool.get("ObjectRename"));//Rename the selected object

			Log.message("3. Renamed the selected object with scripting value  : " + dataPool.get("ObjectRename"), driver);

			//Step-4 : Select the latest version of the object from the histroy view
			//---------------------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectRename")+ extension);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetHyperlink.Value);//Select the gethyperlink option from the context menu

			Log.message("4. Selected the " + Caption.MenuItems.GetHyperlink.Value +" option from the context menu for the object." + dataPool.get("ObjectName"));

			//Step-5 : Get the current url from the gethyperlink
			//--------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			String URL = mfilesDialog.getURLFromComboURLDialog(dataPool.get("ItemToClick"));

			Log.message("5. " + dataPool.get("ItemToClick") + " link URL value is retrived from Get Combo URL dialog. WEB URL : " + URL);

			//Verify if gethyperlink url is displayed as expected
			if (!mfilesDialog.isGetComboURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with " + Caption.MenuItems.GetHyperlink.Value + " title is not opened.");

			//Step-5: Click on Web link on Combo URL dialog
			//------------------------------------------------
			mfilesDialog.clickLinksOnComboURLDialog(dataPool.get("ItemToClick")); //Clicks link in the URL
			Utils.fluentWait(driver);

			Log.message("5. (" + dataPool.get("ItemToClick") + ") link is clicked from Get Combo URL dialog.");

			//Verification : To Verify the new tab URL is same as URL retrieved from Combo URL dialog
			//-----------------------------------------------------------------------------------------
			String tabURL = Utility.getNewTabURL(driver, 1);
			Log.message(tabURL.trim());

			if (!URL.trim().equalsIgnoreCase(tabURL.trim()))
				throw new Exception("Test case Failed. The the new tab URL is not same as URL retrieved from GetHyperlink dialog from operation menu for user.");

			if (homePage.listView.itemCount() == 1 && homePage.listView.isItemExists(dataPool.get("ObjectRename")+extension)) //Checks if item exists in the list
				Log.pass("Test case Passed. The the new tab URL opened and selected object is displayed with the coding characters.");
			else
				Log.fail("Test case Failed. The the new tab URL is not opened and selected object is not displayed with the coding characters.", driver);

		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (driver != null) {
				try
				{
					homePage.listView.clickItem(dataPool.get("ObjectRename")+ "." + dataPool.get("ObjectName").split("\\.")[1]);
					homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Rename.Value);//Select the Rename option in context menu
					MFilesDialog mfilesdialog = new MFilesDialog(driver,"Rename");//Instantiate the mfiles dialog
					mfilesdialog.rename(dataPool.get("ObjectName").split("\\.")[0]);//Rename the selected object
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}

			}

			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest34328



} //End Class GetComboURL
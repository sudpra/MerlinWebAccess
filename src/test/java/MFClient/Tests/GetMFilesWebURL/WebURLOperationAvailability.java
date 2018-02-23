package MFClient.Tests.GetMFilesWebURL;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.Utils;
import genericLibrary.WebDriverUtils;

import java.util.HashMap;
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

import MFClient.Pages.HomePage;
import MFClient.Pages.LoginPage;
import MFClient.Wrappers.Caption;
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class WebURLOperationAvailability {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String configURL = null;
	public static String userName = null;
	public static String password = null;
	public static String testVault = null;
	public static String productVersion = null;
	public static WebDriver driver = null;
	public static String userFullName = null;
	public static String className = null;

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
			Utility.restoreTestVault();
			Utility.configureUsers(xlTestDataWorkBook);

			userName = xmlParameters.getParameter("UserName");
			password = xmlParameters.getParameter("Password");
			testVault = xmlParameters.getParameter("VaultName");		
			userFullName = xmlParameters.getParameter("UserFullName");
			className = this.getClass().getSimpleName().toString().trim();
			if (xmlParameters.getParameter("driverType").equalsIgnoreCase("IE"))
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim() + xmlParameters.getParameter("driverVersion").trim();
			else
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim();

			/*	if(userConfig != "") {
				String[] userDetails = userConfig.split(",");
				userName = userDetails[0];
				password = userDetails[1];
				userFullName = userDetails[2];
			}
			 */
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

			Log.endTestCase();

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
	 * 101_2_1_1A : Verify if GetMFilesWebURL option is available in context menu for non Admin users
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "GetMFilesWebURL"}, 
			description = "Verify if GetMFilesWebURL option is available in context menu for non Admin users")
	public void SprintTest101_2_1_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try { 



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login as non admin user
			//--------------------------------
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String vault = xmlParameters.getParameter("VaultName");
			driver.get(loginURL); //Launches with the URL
			Utils.fluentWait(driver);

			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX")) //Checks if Login.aspx page is loaded
				throw new Exception ("Browser is not navigated to the Login page.");

			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), vault); //Logs into application
			Utils.fluentWait(driver);

			Log.message("1. Login as the non admin user.");

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Right click on the object and select GetMFilesWebURL from context menu
			//-----------------------------------------------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Right clicked on an object  and context menu is opened.");


			//Verification : To Verify " + Caption.MenuItems.GetMFilesWebURL.Value + " option is available in context menu
			//----------------------------------------------------------------------------

			//Verifies that MFiles dialog does not exists
			if (!homePage.listView.itemExistsInContextMenu(Caption.MenuItems.GetMFilesWebURL.Value))
				Log.pass("Test case Passed. " + Caption.MenuItems.GetMFilesWebURL.Value + " option is not available in context menu for non Admin user.");
			else
				Log.fail("Test case Failed. " + Caption.MenuItems.GetMFilesWebURL.Value + " option is available in context menu for non Admin user.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest101_2_1_1A

	/**
	 * 101_2_1_1B : Verify if GetMFilesWebURL option is available in Operations menu for non Admin users 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint101", "GetMFilesWebURL"}, 
			description = "Verify if GetMFilesWebURL option is available in Operations menu for non Admin users")
	public void SprintTest101_2_1_1B(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			//1. Login as non admin user
			//--------------------------------
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String vault = xmlParameters.getParameter("VaultName");
			driver.get(loginURL); //Launches with the URL
			Utils.fluentWait(driver);

			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX")) //Checks if Login.aspx page is loaded
				throw new Exception ("Browser is not navigated to the Login page.");

			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), vault); //Logs into application
			Utils.fluentWait(driver);

			Log.message("1. Login as the non admin user.");
			//Step-1 : Navigate to specified View
			//-----------------------------------

			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select any existing object inlisting view
			//--------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("3. Object selected in listing view");


			//Verification : To Verify MFiles dialog is closed on clicking x button
			//---------------------------------------------------------------------

			//Verifies that MFiles dialog does not exists
			if (homePage.menuBar.IsOperationMenuItemExists(Caption.MenuItems.GetMFilesWebURL.Value))
				Log.pass("Test case Passed. " + Caption.MenuItems.GetMFilesWebURL.Value + " option is not available in operation menu for non Admin user..");
			else
				Log.fail("Test case Failed. " + Caption.MenuItems.GetMFilesWebURL.Value + " option is available in operation menu for non Admin user.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest101_2_1_1B

	/**
	 * 101_2_1_2A : Verify GetMFilesWebURL URL for object group headers from context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIPIE11_MultiDriver", "Sprint101", "GetMFilesWebURL"}, 
			description = "Verify GetMFilesWebURL functionality in Operation menu for object group headers")
	public void SprintTest101_2_1_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE"))
			throw new SkipException("Second instance of browser will not be opened with active first session for '" + driverType + "' driver type.");

		driver = null; 
		WebDriver driver2 = null;
		try { 



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Login as the Admin user.");

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Expand any exiting object in listing view
			//--------------------------------------------------
			homePage.listView.expandItemByName(dataPool.get("ObjectName"));
			Utils.fluentWait(driver);
			Log.message("2. Expanded '" + viewToNavigate + "' onject in listing view.");

			//Step-3 : Right click on the object and select GetMFilesWebURL from context menu
			//-----------------------------------------------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectHeader"))) //Selects the Object header in the list
				throw new Exception("Object (" + dataPool.get("ObjectHeader") + ") is not got selected.");

			Log.message("3. Right clicked on an object header and context menu is opened.");

			//Step-4 : To Verify " + Caption.MenuItems.GetMFilesWebURL.Value + " option is available in context menu
			//-----------------------------------------------------------------------------

			if (!homePage.listView.itemExistsInContextMenu(Caption.MenuItems.GetMFilesWebURL.Value))
				throw new Exception("GetMFilesWebURL option is not displayed for the (" + dataPool.get("ObjectHeader") + ") group header.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from operations menu
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("4. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectHeader") + ") is opened from context menu.");

			//Step-5 : Select 'Show the Selected object' option in GetMFilesWebURL dialog
			//-------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show object
				throw new Exception("Show Selected object is not selected in GetMFilesWebURL dialog.");

			if (!mfilesDialog.setHyperLinkLayoutOption("SIMPLE LISTING")) //Selects Simple Listing
				throw new Exception("Simple Listing is not selected in GetMFilesWebURL dialog.");

			Log.message("5. Show Selected Object and Simple Listing is selected in GetMFilesWebURL dialog.");

			//Step-6 : Copy the link from text box and close the GetMFilesWebURL dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the GetMFilesWebURL dialog

			Log.message("6. Hyperlink is copied and GetMFilesWebURL dialog is closed.");

			//Step-7 : Open new window and open the hyperlink URL in the page
			//---------------------------------------------------------------
			driver2 = WebDriverUtils.getDriver();
			Utils.fluentWait(driver2);
			Log.message("7. New browser window is opened.");

			//Step-8 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver2.get(hyperlinkText); //Navigates to the hyperlink url
			Utils.fluentWait(driver2);
			Log.message("8. Object Hyperlink is pasted in browser and log in page is displayed.");

			//Step-9 : Log in to the hyperlink
			//--------------------------------
			LoginPage loginPage = new LoginPage(driver2);
			homePage = loginPage.loginToWebApplication(userName, password, "");
			Log.message("9. Logged in with valid credentials.");

			//Verification : To Verify " + Caption.MenuItems.GetMFilesWebURL.Value + " option is available in context menu
			//----------------------------------------------------------------------------

			//Verifies that MFiles dialog does not exists
			if (homePage.listView.isItemExists(dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Selected objects is displayed in " + Caption.MenuItems.GetMFilesWebURL.Value + " list view.");
			else
				Log.fail("Test case Failed. Selected objects is not displayed in " + Caption.MenuItems.GetMFilesWebURL.Value + " list view..", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
			if (driver2 != null) {Utility.quitDriver(driver2);}
		} //End finally

	} //End SprintTest101_2_1_2A

	/**
	 * 101_2_1_2B: Verify GetMFilesWebURL URL for object group headers from Operation menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIPIE11_MultiDriver", "Sprint101", "GetMFilesWebURL"}, 
			description = "Verify if GetMFilesWebURL option is available in Operation menu for object group headers")
	public void SprintTest101_2_1_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE"))
			throw new SkipException("Second instance of browser will not be opened with active first session for '" + driverType + "' driver type.");

		driver = null; 
		WebDriver driver2 = null;
		try { 



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Login as the Admin user.");

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Expand any exiting object in listing view
			//--------------------------------------------------
			homePage.listView.expandItemByName(dataPool.get("ObjectName"));
			Utils.fluentWait(driver);
			Log.message("3. Expanded '" + viewToNavigate + "' onject in listing view.");

			//Step-3 : Select object header in expanded object
			//-------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectHeader"));

			Log.message("4. Object group header selected in listing view.");

			//Step-4 : To Verify " + Caption.MenuItems.GetMFilesWebURL.Value + " option is available in context menu
			//-----------------------------------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from operations menu
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("5. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectHeader") + ") is opened from context menu.");

			//Step-5 : Select 'Show the Selected object' option in GetMFilesWebURL dialog
			//-------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show object
				throw new Exception("Show Selected object is not selected in GetMFilesWebURL dialog.");

			if (!mfilesDialog.setHyperLinkLayoutOption("SIMPLE LISTING")) //Selects Simple Listing
				throw new Exception("Simple Listing is not selected in GetMFilesWebURL dialog.");

			Log.message("6. Show Selected Object and Simple Listing is selected in GetMFilesWebURL dialog.");

			//Step-6 : Copy the link from text box and close the GetMFilesWebURL dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the GetMFilesWebURL dialog

			Log.message("7. Hyperlink is copied and GetMFilesWebURL dialog is closed.");

			//Step-7 : Open new window and open the hyperlink URL in the page
			//---------------------------------------------------------------
			driver2 = WebDriverUtils.getDriver();
			Utils.fluentWait(driver2);
			Log.message("8. New browser window is opened.");

			//Step-8 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver2.get(hyperlinkText); //Navigates to the hyperlink url
			Utils.fluentWait(driver2);
			Log.message("9. Object Hyperlink is pasted in browser and log in page is displayed.");

			//Step-9 : Log in to the hyperlink
			//--------------------------------
			LoginPage loginPage = new LoginPage(driver2);
			homePage = loginPage.loginToWebApplication(userName, password, "");
			Log.message("10. Logged in with valid credentials.");

			//Verification : To Verify " + Caption.MenuItems.GetMFilesWebURL.Value + " option is available in context menu
			//----------------------------------------------------------------------------

			//Verifies that MFiles dialog does not exists
			if (homePage.listView.isItemExists(dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Selected objects is displayed in " + Caption.MenuItems.GetMFilesWebURL.Value + " list view.");
			else
				Log.fail("Test case Failed. Selected objects is not displayed in " + Caption.MenuItems.GetMFilesWebURL.Value + " list view..", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
			if (driver2 != null) {Utility.quitDriver(driver2);}
		} //End finally

	} //End SprintTest101_2_1_2B

	/**
	 * 101_2_1_3A : Verify Get M-Files Web URL option in home view from Context menu for admin user
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIPIE11_MultiDriver", "Sprint101", "GetMFilesWebURL"}, 
			description = "Verify Get M-Files Web URL option in home view from Context menu for admin user")
	public void SprintTest101_2_1_3A(HashMap<String,String> dataValues, String driverType) throws Exception {


		if (driverType.equalsIgnoreCase("IE"))
			throw new SkipException("Second instance of browser will not be opened with active first session for '" + driverType + "' driver type.");

		driver = null; 
		WebDriver driver2 = null;

		try { 



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Login as the Admin user.");

			//Step-1 : Navigate to Home View
			//-----------------------------------
			//String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));
			homePage.taskPanel.clickItem(dataPool.get("NavigateToView"));

			Log.message("2. Navigated to '" + dataPool.get("NavigateToView") + "' view.");

			//Step-2 : Right click and select GetMFilesWebURL from context menu
			//-----------------------------------------------------------------------------
			homePage.listView.rightClickListview(); //Selects the Object in the list
			Log.message("3. Right clicked in the listing view and context menu is opened.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from operations menu
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("4. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectHeader") + ") is opened from context menu.");

			//Step-5 : Select 'Show the Selected object' option in GetMFilesWebURL dialog
			//-------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("SHOW VIEW")) //Selects Show object
				throw new Exception("Show Selected object is not selected in GetMFilesWebURL dialog.");

			Log.message("5. Show the current view in GetMFilesWebURL dialog.");

			//Step-6 : Copy the link from text box and close the GetMFilesWebURL dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the GetMFilesWebURL dialog

			Log.message("6. Hyperlink is copied and GetMFilesWebURL dialog is closed.");

			//Step-7 : Open new window and open the hyperlink URL in the page
			//---------------------------------------------------------------
			driver2 = WebDriverUtils.getDriver();
			Utils.fluentWait(driver2);
			Log.message("7. New browser window is opened.");

			//Step-8 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver2.get(hyperlinkText); //Navigates to the hyperlink url
			Utils.fluentWait(driver2);
			Log.message("8. Object Hyperlink is pasted in browser and log in page is displayed.");

			String groupHeader = dataPool.get("HomeViewItems");
			//Step-9 : Log in to the hyperlink
			//--------------------------------
			LoginPage loginPage = new LoginPage(driver2);
			homePage = loginPage.loginToWebApplication(userName, password, "");
			Log.message("9. Logged in with valid credentials.");

			//Verification : To Verify " + Caption.MenuItems.GetMFilesWebURL.Value + " option is available in context menu
			//----------------------------------------------------------------------------

			//Verifies that MFiles dialog does not exists
			if (homePage.listView.isGroupHeaderAvailable(groupHeader))
				Log.pass("Test case Passed. " + Caption.MenuItems.GetMFilesWebURL.Value + " option is available in context menu for Home view.");
			else
				Log.fail("Test case Failed. " + Caption.MenuItems.GetMFilesWebURL.Value + " option is not available in context menu for Home view.", driver2);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (driver2 != null) {Utility.quitDriver(driver2);}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest101_2_1_3A

	/**
	 * 101_2_1_3B : Verify Get M-Files Web URL option in home view from Context menu for non admin user
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIPIE11_MultiDriver", "Sprint101", "GetMFilesWebURL"}, 
			description = "Verify Get M-Files Web URL option in home view from Context menu for non admin user")
	public void SprintTest101_2_1_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE"))
			throw new SkipException("Second instance of browser will not be opened with active first session for '" + driverType + "' driver type.");

		driver = null; 
		WebDriver driver2 = null;
		try { 



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Login as the Admin user.");

			//Step-1 : Navigate to Home View
			//-----------------------------------
			//String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));
			homePage.taskPanel.clickItem(dataPool.get("NavigateToView"));

			Log.message("2. Navigated to '" + dataPool.get("NavigateToView") + "' view.");

			//Step-2 : To Verify " + Caption.MenuItems.GetMFilesWebURL.Value + " option is available in context menu
			//-----------------------------------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from operations menu
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectHeader") + ") is opened from context menu.");

			//Step-5 : Select 'Show the Selected object' option in GetMFilesWebURL dialog
			//-------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("SHOW VIEW")) //Selects Show object
				throw new Exception("Show Selected object is not selected in GetMFilesWebURL dialog.");

			Log.message("4. Show the current view in GetMFilesWebURL dialog.");

			//Step-6 : Copy the link from text box and close the GetMFilesWebURL dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the GetMFilesWebURL dialog

			Log.message("5. Hyperlink is copied and GetMFilesWebURL dialog is closed.");

			//Step-7 : Open new window and open the hyperlink URL in the page
			//---------------------------------------------------------------
			driver2 = WebDriverUtils.getDriver();
			Utils.fluentWait(driver2);
			Log.message("6. New browser window is opened.");

			//Step-8 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver2.get(hyperlinkText); //Navigates to the hyperlink url
			Utils.fluentWait(driver2);
			Log.message("7. Object Hyperlink is pasted in browser and log in page is displayed.");

			String groupHeader = dataPool.get("HomeViewItems");
			//Step-9 : Log in to the hyperlink
			//--------------------------------
			LoginPage loginPage = new LoginPage(driver2);
			homePage = loginPage.loginToWebApplication(userName, password, "");
			Log.message("8. Logged in with valid credentials.");

			//Verification : To Verify " + Caption.MenuItems.GetMFilesWebURL.Value + " option is available in context menu
			//----------------------------------------------------------------------------

			//Verifies that MFiles dialog does not exists
			if (homePage.listView.isGroupHeaderAvailable(groupHeader))
				Log.pass("Test case Passed. " + Caption.MenuItems.GetMFilesWebURL.Value + " option is available in Operation menu for Home view.");
			else
				Log.fail("Test case Failed. " + Caption.MenuItems.GetMFilesWebURL.Value + " option is not available in Operation menu for Home view.", driver2);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			if (driver2 != null) {Utility.quitDriver(driver2);}
			Utility.quitDriver(driver);

		} //End finally
	}	

	/**
	 * 101_2_1_4A : Verify if GetMFilesWebURL option is available in context menu for non Admin users
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "GetMFilesWebURL"}, 
			description = "Verify if GetMFilesWebURL option is available in context menu in Home view for non Admin users")
	public void SprintTest101_2_1_4A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try { 



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login as non admin user
			//--------------------------------
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String vault = xmlParameters.getParameter("VaultName");
			driver.get(loginURL); //Launches with the URL
			Utils.fluentWait(driver);

			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX")) //Checks if Login.aspx page is loaded
				throw new Exception ("Browser is not navigated to the Login page.");

			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), vault); //Logs into application
			Utils.fluentWait(driver);

			Log.message("1. Login as the non admin user.");
			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Right click on the object and select GetMFilesWebURL from context menu
			//-----------------------------------------------------------------------------
			homePage.listView.rightClickListview(); //Selects the Object in the list
			Log.message("3. Right clicked in the listing view and context menu is opened.");

			//Verification : To Verify " + Caption.MenuItems.GetMFilesWebURL.Value + " option is available in context menu
			//----------------------------------------------------------------------------

			if (!homePage.listView.itemExistsInContextMenu(Caption.MenuItems.GetMFilesWebURL.Value))
				Log.pass("Test case Passed. " + Caption.MenuItems.GetMFilesWebURL.Value + " option is not available in context menu for non Admin user in Home view.");
			else
				Log.fail("Test case Failed. " + Caption.MenuItems.GetMFilesWebURL.Value + " option is available in context menu for non Admin user in Home view.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest101_2_1_4A

	/**
	 * 101_2_1_4B : Verify if GetMFilesWebURL option is available in Operation menu for non Admin users
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "GetMFilesWebURL"}, 
			description = "Verify if GetMFilesWebURL option is available in Operation menu for non Admin users")
	public void SprintTest101_2_1_4B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try { 



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login as non admin user
			//--------------------------------
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String vault = xmlParameters.getParameter("VaultName");
			driver.get(loginURL); //Launches with the URL
			Utils.fluentWait(driver);

			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX")) //Checks if Login.aspx page is loaded
				throw new Exception ("Browser is not navigated to the Login page.");

			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), vault); //Logs into application
			Utils.fluentWait(driver);

			Log.message("1. Login as the non admin user.");

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("2. Navigated to '" + viewToNavigate + "' view.");


			//Verification : To Verify " + Caption.MenuItems.GetMFilesWebURL.Value + " option is available in operation menu
			//----------------------------------------------------------------------------

			if (homePage.menuBar.IsOperationMenuItemExists(Caption.MenuItems.GetMFilesWebURL.Value))
				Log.pass("Test case Passed. " + Caption.MenuItems.GetMFilesWebURL.Value + " option is not available in operations menu for non Admin user in Home view.");
			else
				Log.fail("Test case Failed. " + Caption.MenuItems.GetMFilesWebURL.Value + " option is available in operations menu for non Admin user in Home view.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest101_2_1_4B


}

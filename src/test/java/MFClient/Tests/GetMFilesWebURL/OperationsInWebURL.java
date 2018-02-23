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
public class OperationsInWebURL {

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
			xlTestDataWorkBook = xmlParameters.getParameter("TestData");;
			loginURL = xmlParameters.getParameter("webSite");
			configURL = xmlParameters.getParameter("ConfigurationURL");
			Utility.restoreTestVault();
			Utility.configureUsers(xlTestDataWorkBook);
			className = this.getClass().getSimpleName().toString().trim();
			if (xmlParameters.getParameter("driverType").equalsIgnoreCase("IE"))
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim() + xmlParameters.getParameter("driverVersion").trim();
			else
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim();

			userName = xmlParameters.getParameter("UserName");
			password = xmlParameters.getParameter("Password");
			testVault = xmlParameters.getParameter("VaultName");		
			userFullName = xmlParameters.getParameter("UserFullName");

			/*if(userConfig != "") {
				String[] userDetails = userConfig.split(",");
				userName = userDetails[0];
				password = userDetails[1];
				userFullName = userDetails[2];
			}*/

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
	 * 54.1.33.1A : 'Reset all' should reset search conditions of selected object with default layout in hyperlink URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "'Reset all' should reset search conditions of selected object with default layout in hyperlink URL - Context menu")
	public void SprintTest54_1_33_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open GetMFilesWebURL dialog from context menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("2. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-3 : Select 'Show the selected file', 'Default' Custom layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT"))
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Show the selected File and Default custom layout are selected and hyperlink URL is obtained.");

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog
			Utils.fluentWait(driver);

			Log.message("4. Navigated to the URL copied from hyperlink dialog.");			

			//Step-5 : Change the search option to Any word in hyperlink URL page
			//--------------------------------------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true); //Clicks Advanced search button
			String prevOption = homePage.searchPanel.getSelectedSearchOption(); //Gets the selected search option
			homePage.searchPanel.setSearchOption(Caption.Search.SearchAnyWord.Value); //Sets Search option to Search any word

			Log.message("5. Search option is modified from '" + prevOption + "' to '" + Caption.Search.SearchAnyWord.Value + "'. in hyperlink URL page.");

			//Step-5 : Click Reset all button
			//-------------------------------
			homePage.searchPanel.resetAll(); //Clicks Reset all button

			Log.message("5. Reset all button is clicked.");

			//Verification : Verify if Reset all button reset the conditions
			//---------------------------------------------------------------
			if (homePage.searchPanel.getSelectedSearchOption().equalsIgnoreCase(prevOption)) //Verifies if reset all has reset the conditions
				Log.pass("Test case Passed. Reset all has reset the conditions in hyperlink URL page.");
			else
				Log.fail("Test case Failed. Reset all has not reset the conditions in hyperlink URL page.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_1_33_1A

	/**
	 * 54.1.33.1B : 'Reset all' should reset search conditions of selected object with default layout in hyperlink URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "'Reset all' should reset search conditions of selected object with default layout in hyperlink URL - Operations menu")
	public void SprintTest54_1_33_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open GetMFilesWebURL dialog from operations menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not clicked.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("2. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-3 : Select 'Show the selected file', 'Default' Custom layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT"))
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Show the selected File and Default custom layout are selected and hyperlink URL is obtained.");

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog
			Utils.fluentWait(driver);

			Log.message("4. Navigated to the URL copied from hyperlink dialog.");			

			//Step-5 : Change the search option to Any word in hyperlink URL page
			//--------------------------------------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true); //Clicks Advanced search button
			String prevOption = homePage.searchPanel.getSelectedSearchOption(); //Gets the selected search option
			homePage.searchPanel.setSearchOption(Caption.Search.SearchAnyWord.Value); //Sets Search option to Search any word

			Log.message("5. Search option is modified from '" + prevOption + "' to '" + Caption.Search.SearchAnyWord.Value + "'. in hyperlink URL page.");

			//Step-5 : Click Reset all button
			//-------------------------------
			homePage.searchPanel.resetAll(); //Clicks Reset all button

			Log.message("6. Reset all button is clicked.");

			//Verification : Verify if Reset all button reset the conditions
			//---------------------------------------------------------------
			if (homePage.searchPanel.getSelectedSearchOption().equalsIgnoreCase(prevOption)) //Verifies if reset all has reset the conditions
				Log.pass("Test case Passed. Reset all has reset the conditions in hyperlink URL page.");
			else
				Log.fail("Test case Failed. Reset all has not reset the conditions in hyperlink URL page.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_1_33_1B

	/**
	 * 54.1.33.2A : 'Reset all' should reset search conditions of selected object with simple listing layout in hyperlink URL  - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "'Reset all' should reset search conditions of selected object with simple listing layout in hyperlink URL  - Context menu")
	public void SprintTest54_1_33_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open GetMFilesWebURL dialog from context menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("2. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-3 : Select 'Show the selected file', 'SimpleListing' Custom layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT"))
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Show the selected File and Simple Listing custom layout are selected and hyperlink URL is obtained.");

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog
			Utils.fluentWait(driver);

			Log.message("4. Navigated to the URL copied from hyperlink dialog.");			

			//Step-5 : Change the search option to Any word in hyperlink URL page
			//--------------------------------------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true); //Clicks Advanced search button
			String prevOption = homePage.searchPanel.getSelectedSearchOption(); //Gets the selected search option
			homePage.searchPanel.setSearchOption(Caption.Search.SearchAnyWord.Value); //Sets Search option to Search any word

			Log.message("5. Search option is modified from '" + prevOption + "' to '" + Caption.Search.SearchAnyWord.Value + "'. in hyperlink URL page.");

			//Step-5 : Click Reset all button
			//-------------------------------
			homePage.searchPanel.resetAll(); //Clicks Reset all button

			Log.message("5. Reset all button is clicked.");

			//Verification : Verify if Reset all button reset the conditions
			//---------------------------------------------------------------
			if (homePage.searchPanel.getSelectedSearchOption().equalsIgnoreCase(prevOption)) //Verifies if reset all has reset the conditions
				Log.pass("Test case Passed. Reset all has reset the conditions in hyperlink URL page.");
			else
				Log.fail("Test case Failed. Reset all has not reset the conditions in hyperlink URL page.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_1_33_2A

	/**
	 * 54.1.33.2B : 'Reset all' should reset search conditions of selected object with simple listing layout in hyperlink URL  - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "'Reset all' should reset search conditions of selected object with simple listing layout in hyperlink URL  - Operations menu")
	public void SprintTest54_1_33_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open GetMFilesWebURL dialog from operations menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not clicked.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("2. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-3 : Select 'Show the selected file', 'SimpleListing' Custom layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT"))
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Show the selected File and Simple Listing custom layout are selected and hyperlink URL is obtained.");

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog
			Utils.fluentWait(driver);

			Log.message("4. Navigated to the URL copied from hyperlink dialog.");			

			//Step-5 : Change the search option to Any word in hyperlink URL page
			//--------------------------------------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true); //Clicks Advanced search button
			String prevOption = homePage.searchPanel.getSelectedSearchOption(); //Gets the selected search option
			homePage.searchPanel.setSearchOption(Caption.Search.SearchAnyWord.Value); //Sets Search option to Search any word

			Log.message("5. Search option is modified from '" + prevOption + "' to '" + Caption.Search.SearchAnyWord.Value + "'. in hyperlink URL page.");

			//Step-5 : Click Reset all button
			//-------------------------------
			homePage.searchPanel.resetAll(); //Clicks Reset all button

			Log.message("5. Reset all button is clicked.");

			//Verification : Verify if Reset all button reset the conditions
			//---------------------------------------------------------------
			if (homePage.searchPanel.getSelectedSearchOption().equalsIgnoreCase(prevOption)) //Verifies if reset all has reset the conditions
				Log.pass("Test case Passed. Reset all has reset the conditions in hyperlink URL page.");
			else
				Log.fail("Test case Failed. Reset all has not reset the conditions in hyperlink URL page.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_1_33_2B

	/**
	 * 54.1.33.3A : 'Reset all' should reset search conditions of selected view with default layout in hyperlink URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "'Reset all' should reset search conditions of selected view with default layout in hyperlink URL - Context menu")
	public void SprintTest54_1_33_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open GetMFilesWebURL dialog from context menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("2. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-3 : Select 'Show the selected file', 'Default' Custom layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW VIEW"))
				throw new Exception("Show Selected View is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Show the selected view and Default custom layout are selected and hyperlink URL is obtained.");

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog
			Utils.fluentWait(driver);

			Log.message("4. Navigated to the URL copied from hyperlink dialog.");			

			//Step-5 : Change the search option to Any word in hyperlink URL page
			//--------------------------------------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true); //Clicks Advanced search button
			String prevOption = homePage.searchPanel.getSelectedSearchOption(); //Gets the selected search option
			homePage.searchPanel.setSearchOption(Caption.Search.SearchAnyWord.Value); //Sets Search option to Search any word

			Log.message("5. Search option is modified from '" + prevOption + "' to '" + Caption.Search.SearchAnyWord.Value + "'. in hyperlink URL page.");

			//Step-5 : Click Reset all button
			//-------------------------------
			homePage.searchPanel.resetAll(); //Clicks Reset all button

			Log.message("6. Reset all button is clicked.");

			//Verification : Verify if Reset all button reset the conditions
			//---------------------------------------------------------------
			if (homePage.searchPanel.getSelectedSearchOption().equalsIgnoreCase(prevOption)) //Verifies if reset all has reset the conditions
				Log.pass("Test case Passed. Reset all has reset the conditions in hyperlink URL page.");
			else
				Log.fail("Test case Failed. Reset all has not reset the conditions in hyperlink URL page.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_1_33_3A

	/**
	 * 54.1.33.3B : 'Reset all' should reset search conditions of selected view with default layout in hyperlink URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "'Reset all' should reset search conditions of selected view with default layout in hyperlink URL - Context menu")
	public void SprintTest54_1_33_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open GetMFilesWebURL dialog from operations menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not clicked.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("2. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-3 : Select 'Show the selected file', 'Default' Custom layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW VIEW"))
				throw new Exception("Show Selected View is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Show the selected view and Default custom layout are selected and hyperlink URL is obtained.");

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog
			Utils.fluentWait(driver);

			Log.message("4. Navigated to the URL copied from hyperlink dialog.");			

			//Step-5 : Change the search option to Any word in hyperlink URL page
			//--------------------------------------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true); //Clicks Advanced search button
			String prevOption = homePage.searchPanel.getSelectedSearchOption(); //Gets the selected search option
			homePage.searchPanel.setSearchOption(Caption.Search.SearchAnyWord.Value); //Sets Search option to Search any word

			Log.message("5. Search option is modified from '" + prevOption + "' to '" + Caption.Search.SearchAnyWord.Value + "'. in hyperlink URL page.");

			//Step-5 : Click Reset all button
			//-------------------------------
			homePage.searchPanel.resetAll(); //Clicks Reset all button

			Log.message("6. Reset all button is clicked.");

			//Verification : Verify if Reset all button reset the conditions
			//---------------------------------------------------------------
			if (homePage.searchPanel.getSelectedSearchOption().equalsIgnoreCase(prevOption)) //Verifies if reset all has reset the conditions
				Log.pass("Test case Passed. Reset all has reset the conditions in hyperlink URL page.");
			else
				Log.fail("Test case Failed. Reset all has not reset the conditions in hyperlink URL page.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_1_33_3B

	/**
	 * 54.1.33.4A : 'Reset all' should reset search conditions of selected view with simple listing layout in hyperlink URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "'Reset all' should reset search conditions of selected view with simple listing layout in hyperlink URL - Context menu")
	public void SprintTest54_1_33_4A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open GetMFilesWebURL dialog from context menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("2. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-3 : Select 'Show the selected file', 'SimpleListing' Custom layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW VIEW"))
				throw new Exception("Show Selected View is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Show the selected view and Simple Listing custom layout are selected and hyperlink URL is obtained.");

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog
			Utils.fluentWait(driver);

			Log.message("4. Navigated to the URL copied from hyperlink dialog.");			

			//Step-5 : Change the search option to Any word in hyperlink URL page
			//--------------------------------------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true); //Clicks Advanced search button
			String prevOption = homePage.searchPanel.getSelectedSearchOption(); //Gets the selected search option
			homePage.searchPanel.setSearchOption(Caption.Search.SearchAnyWord.Value); //Sets Search option to Search any word

			Log.message("5. Search option is modified from '" + prevOption + "' to '" + Caption.Search.SearchAnyWord.Value + "'. in hyperlink URL page.");

			//Step-5 : Click Reset all button
			//-------------------------------
			homePage.searchPanel.resetAll(); //Clicks Reset all button

			Log.message("6. Reset all button is clicked.");

			//Verification : Verify if Reset all button reset the conditions
			//---------------------------------------------------------------
			if (homePage.searchPanel.getSelectedSearchOption().equalsIgnoreCase(prevOption)) //Verifies if reset all has reset the conditions
				Log.pass("Test case Passed. Reset all has reset the conditions in hyperlink URL page.");
			else
				Log.fail("Test case Failed. Reset all has not reset the conditions in hyperlink URL page.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_1_33_4A

	/**
	 * 54.1.33.4B : 'Reset all' should reset search conditions of selected view with simple listing layout in hyperlink URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "'Reset all' should reset search conditions of selected view with simple listing layout in hyperlink URL - Operations menu")
	public void SprintTest54_1_33_4B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open GetMFilesWebURL dialog from operations menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not clicked.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("2. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-3 : Select 'Show the selected file', 'SimpleListing' Custom layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW VIEW"))
				throw new Exception("Show Selected View is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Show the selected view and Simple Listing custom layout are selected and hyperlink URL is obtained.");

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog
			Utils.fluentWait(driver);

			Log.message("4. Navigated to the URL copied from hyperlink dialog.");			

			//Step-5 : Change the search option to Any word in hyperlink URL page
			//--------------------------------------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true); //Clicks Advanced search button
			String prevOption = homePage.searchPanel.getSelectedSearchOption(); //Gets the selected search option
			homePage.searchPanel.setSearchOption(Caption.Search.SearchAnyWord.Value); //Sets Search option to Search any word

			Log.message("5. Search option is modified from '" + prevOption + "' to '" + Caption.Search.SearchAnyWord.Value + "'. in hyperlink URL page.");

			//Step-5 : Click Reset all button
			//-------------------------------
			homePage.searchPanel.resetAll(); //Clicks Reset all button

			Log.message("6. Reset all button is clicked.");

			//Verification : Verify if Reset all button reset the conditions
			//---------------------------------------------------------------
			if (homePage.searchPanel.getSelectedSearchOption().equalsIgnoreCase(prevOption)) //Verifies if reset all has reset the conditions
				Log.pass("Test case Passed. Reset all has reset the conditions in hyperlink URL page.");
			else
				Log.fail("Test case Failed. Reset all has not reset the conditions in hyperlink URL page.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_1_33_4B



	/**
	 * 54.1.35.1A : Clicking search button should search the objects in the hyperlink URL of the selected object in default layout - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Clicking search button should search the objects in the hyperlink URL of the selected object in default layout - Context menu")
	public void SprintTest54_1_35_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open GetMFilesWebURL dialog from context menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("2. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-3 : Select 'Show the selected file', 'Default' Custom layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT"))
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Show the selected File and Default custom layout are selected and hyperlink URL is obtained.");

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog
			Utils.fluentWait(driver);

			Log.message("4. Navigated to the URL copied from hyperlink dialog.");

			//Step-5 : Perform simple search operation in the hyperlink URL
			//-------------------------------------------------------------
			int hyperlinkURLCt = homePage.listView.itemCount(); //Gets the number of items in the Hyperlink URL
			homePage.searchPanel.clickSearch(); //Clicks Search button
			int searchCt = homePage.listView.itemCount(); //Gets the number of items in the list after search operation

			//Verification : Verify if Rightpane with metadata and preview exists
			//-------------------------------------------------------------------
			if (!driver.getCurrentUrl().toUpperCase().contains("TEMPSEARCH"))
				throw new Exception("Test case Failed, URL after performing search does not contains 'tempsearch'.");

			if (hyperlinkURLCt < searchCt)
				Log.pass("Test case Passed. Search operation is performed successful in hyperlink URL page.");
			else
				Log.fail("Test case Failed. Number of objects after performing search is not greater than hyperlink URL.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_1_35_1A

	/**
	 * 54.1.35.1B : Clicking search button should search the objects in the hyperlink URL of the selected object in default layout - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Clicking search button should search the objects in the hyperlink URL of the selected object in default layout - Operations menu")
	public void SprintTest54_1_35_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open GetMFilesWebURL dialog from operations menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not clicked.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("2. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-3 : Select 'Show the selected file', 'Default' Custom layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT"))
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Show the selected File and Default custom layout are selected and hyperlink URL is obtained.");

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog
			Utils.fluentWait(driver);

			Log.message("4. Navigated to the URL copied from hyperlink dialog.");

			//Step-5 : Perform simple search operation in the hyperlink URL
			//-------------------------------------------------------------
			int hyperlinkURLCt = homePage.listView.itemCount(); //Gets the number of items in the Hyperlink URL
			homePage.searchPanel.clickSearch(); //Clicks Search button
			int searchCt = homePage.listView.itemCount(); //Gets the number of items in the list after search operation

			//Verification : Verify if Rightpane with metadata and preview exists
			//-------------------------------------------------------------------
			if (!driver.getCurrentUrl().toUpperCase().contains("TEMPSEARCH"))
				throw new Exception("Test case Failed, URL after performing search does not contains 'tempsearch'.");

			if (hyperlinkURLCt < searchCt)
				Log.pass("Test case Passed. Search operation is performed successful in hyperlink URL page.");
			else
				Log.fail("Test case Failed. Number of objects after performing search is not greater than hyperlink URL.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_1_35_1B

	/**
	 * 54.1.35.2A : Clicking search button should search the objects in the hyperlink URL of the selected object in simple listing layout - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Clicking search button should search the objects in the hyperlink URL of the selected object in simple listing layout - Context menu")
	public void SprintTest54_1_35_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open GetMFilesWebURL dialog from context menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("2. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-3 : Select 'Show the selected file', 'Simplet listing' layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT"))
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Show the selected File and simple listing layout are selected and hyperlink URL is obtained.");

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog
			Utils.fluentWait(driver);

			Log.message("4. Navigated to the URL copied from hyperlink dialog.");

			//Step-5 : Perform simple search operation in the hyperlink URL
			//-------------------------------------------------------------
			int hyperlinkURLCt = homePage.listView.itemCount(); //Gets the number of items in the Hyperlink URL
			homePage.searchPanel.clickSearch(); //Clicks Search button
			int searchCt = homePage.listView.itemCount(); //Gets the number of items in the list after search operation

			//Verification : Verify if Rightpane with metadata and preview exists
			//-------------------------------------------------------------------
			if (!driver.getCurrentUrl().toUpperCase().contains("TEMPSEARCH"))
				throw new Exception("Test case Failed, URL after performing search does not contains 'tempsearch'.");

			if (hyperlinkURLCt < searchCt)
				Log.pass("Test case Passed. Search operation is performed successful in hyperlink URL page.");
			else
				Log.fail("Test case Failed. Number of objects after performing search is not greater than hyperlink URL.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_1_35_2A

	/**
	 * 54.1.35.2B : Clicking search button should search the objects in the hyperlink URL of the selected object in simple listing layout - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Clicking search button should search the objects in the hyperlink URL of the selected object in simple listing layout - Operations menu")
	public void SprintTest54_1_35_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open GetMFilesWebURL dialog from operations menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not clicked.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("2. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-3 : Select 'Show the selected file', 'Simplet listing' layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT"))
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Show the selected File and simple listing layout are selected and hyperlink URL is obtained.");

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog
			Utils.fluentWait(driver);

			Log.message("4. Navigated to the URL copied from hyperlink dialog.");

			//Step-5 : Perform simple search operation in the hyperlink URL
			//-------------------------------------------------------------
			int hyperlinkURLCt = homePage.listView.itemCount(); //Gets the number of items in the Hyperlink URL
			homePage.searchPanel.clickSearch(); //Clicks Search button
			int searchCt = homePage.listView.itemCount(); //Gets the number of items in the list after search operation

			//Verification : Verify if Rightpane with metadata and preview exists
			//-------------------------------------------------------------------
			if (!driver.getCurrentUrl().toUpperCase().contains("TEMPSEARCH"))
				throw new Exception("Test case Failed, URL after performing search does not contains 'tempsearch'.");

			if (hyperlinkURLCt < searchCt)
				Log.pass("Test case Passed. Search operation is performed successful in hyperlink URL page.");
			else
				Log.fail("Test case Failed. Number of objects after performing search is not greater than hyperlink URL.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_1_35_2B

	/**
	 * 54.1.35.3A : Clicking search button should search the objects in the hyperlink URL of the selected view in default layout - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Clicking search button should search the objects in the hyperlink URL of the selected view in default layout - Context menu")
	public void SprintTest54_1_35_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open GetMFilesWebURL dialog from context menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("2. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-3 : Select 'Show the selected file', 'Default' Custom layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW VIEW"))
				throw new Exception("Show View is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Show the selected View and Default custom layout are selected and hyperlink URL is obtained.");

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog
			Utils.fluentWait(driver);

			Log.message("4. Navigated to the URL copied from hyperlink dialog.");

			//Step-5 : Perform simple search operation in the hyperlink URL
			//-------------------------------------------------------------
			int hyperlinkURLCt = homePage.listView.itemCount(); //Gets the number of items in the Hyperlink URL
			homePage.searchPanel.clickSearch(); //Clicks Search button
			int searchCt = homePage.listView.itemCount(); //Gets the number of items in the list after search operation

			//Verification : Verify if Rightpane with metadata and preview exists
			//-------------------------------------------------------------------
			if (!driver.getCurrentUrl().toUpperCase().contains("TEMPSEARCH"))
				throw new Exception("Test case Failed, URL after performing search does not contains 'tempsearch'.");

			if (hyperlinkURLCt < searchCt)
				Log.pass("Test case Passed. Search operation is performed successful in hyperlink URL page.");
			else
				Log.fail("Test case Failed. Number of objects after performing search is not greater than hyperlink URL.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_1_35_3A

	/**
	 * 54.1.35.3B : Clicking search button should search the objects in the hyperlink URL of the selected view in default layout - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Clicking search button should search the objects in the hyperlink URL of the selected view in default layout - Operations menu")
	public void SprintTest54_1_35_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open GetMFilesWebURL dialog from operations menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not clicked.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("2. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-3 : Select 'Show the selected file', 'Default' Custom layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW VIEW"))
				throw new Exception("Show View is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Show the selected View and Default custom layout are selected and hyperlink URL is obtained.");

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog
			Utils.fluentWait(driver);

			Log.message("4. Navigated to the URL copied from hyperlink dialog.");

			//Step-5 : Perform simple search operation in the hyperlink URL
			//-------------------------------------------------------------
			int hyperlinkURLCt = homePage.listView.itemCount(); //Gets the number of items in the Hyperlink URL
			homePage.searchPanel.clickSearch(); //Clicks Search button
			int searchCt = homePage.listView.itemCount(); //Gets the number of items in the list after search operation

			//Verification : Verify if Rightpane with metadata and preview exists
			//-------------------------------------------------------------------
			if (!driver.getCurrentUrl().toUpperCase().contains("TEMPSEARCH"))
				throw new Exception("Test case Failed, URL after performing search does not contains 'tempsearch'.");

			if (hyperlinkURLCt < searchCt)
				Log.pass("Test case Passed. Search operation is performed successful in hyperlink URL page.");
			else
				Log.fail("Test case Failed. Number of objects after performing search is not greater than hyperlink URL.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_1_35_3B

	/**
	 * 54.1.35.4A : Clicking search button should search the objects in the hyperlink URL of the selected view in simple listing layout - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Clicking search button should search the objects in the hyperlink URL of the selected view in simple listing layout - Context menu")
	public void SprintTest54_1_35_4A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open GetMFilesWebURL dialog from context menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("2. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-3 : Select 'Show the selected file', 'simple listing' layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW VIEW"))
				throw new Exception("Show View is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Show the selected View and simple listing custom layout are selected and hyperlink URL is obtained.");

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog
			Utils.fluentWait(driver);

			Log.message("4. Navigated to the URL copied from hyperlink dialog.");

			//Step-5 : Perform simple search operation in the hyperlink URL
			//-------------------------------------------------------------
			int hyperlinkURLCt = homePage.listView.itemCount(); //Gets the number of items in the Hyperlink URL
			homePage.searchPanel.clickSearch(); //Clicks Search button
			int searchCt = homePage.listView.itemCount(); //Gets the number of items in the list after search operation

			//Verification : Verify if Rightpane with metadata and preview exists
			//-------------------------------------------------------------------
			if (!driver.getCurrentUrl().toUpperCase().contains("TEMPSEARCH"))
				throw new Exception("Test case Failed, URL after performing search does not contains 'tempsearch'.");

			if (hyperlinkURLCt < searchCt)
				Log.pass("Test case Passed. Search operation is performed successful in hyperlink URL page.");
			else
				Log.fail("Test case Failed. Number of objects after performing search is not greater than hyperlink URL.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_1_35_4A

	/**
	 * 54.1.35.4B : Clicking search button should search the objects in the hyperlink URL of the selected view in simple listing layout - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Clicking search button should search the objects in the hyperlink URL of the selected view in simple listing layout - Operations menu")
	public void SprintTest54_1_35_4B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open GetMFilesWebURL dialog from operations menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not clicked.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("2. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-3 : Select 'Show the selected file', 'simple listing' layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW VIEW"))
				throw new Exception("Show View is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Show the selected View and simple listing custom layout are selected and hyperlink URL is obtained.");

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog
			Utils.fluentWait(driver);

			Log.message("4. Navigated to the URL copied from hyperlink dialog.");

			//Step-5 : Perform simple search operation in the hyperlink URL
			//-------------------------------------------------------------
			int hyperlinkURLCt = homePage.listView.itemCount(); //Gets the number of items in the Hyperlink URL
			homePage.searchPanel.clickSearch(); //Clicks Search button
			int searchCt = homePage.listView.itemCount(); //Gets the number of items in the list after search operation

			//Verification : Verify if Rightpane with metadata and preview exists
			//-------------------------------------------------------------------
			if (!driver.getCurrentUrl().toUpperCase().contains("TEMPSEARCH"))
				throw new Exception("Test case Failed, URL after performing search does not contains 'tempsearch'.");

			if (hyperlinkURLCt < searchCt)
				Log.pass("Test case Passed. Search operation is performed successful in hyperlink URL page.");
			else
				Log.fail("Test case Failed. Number of objects after performing search is not greater than hyperlink URL.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_1_35_4B


}

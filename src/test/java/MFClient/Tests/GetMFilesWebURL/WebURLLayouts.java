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
public class WebURLLayouts {

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
			userName = xmlParameters.getParameter("UserName");
			password = xmlParameters.getParameter("Password");
			testVault = xmlParameters.getParameter("VaultName");		
			userFullName = xmlParameters.getParameter("UserFullName");
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
	 * 25.2.16.2A : Hyperlink URL page should have search pane, properties pane, top menu, breadcrumb, java applet and metadatacard visible and task pane, java applet disabled on selecting Simple Listing layout  - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL"}, 
			description = "Hyperlink URL page should have search pane, properties pane, top menu, breadcrumb, java applet and metadatacard visible and task pane, java applet disabled on selecting Simple Listing layout  - Context menu")
	public void SprintTest25_2_16_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from context menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-3 : Select 'Simple Listing' Custom layout if not selected and copy the hyperlink url
			//----------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Simple Listing custom layout is selected and hyperlink URL is obtained.");

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("4. Navigated to the URL copied from hyperlink dialog.");			

			//Verification : Verify if all required layouts are available in the page
			//-----------------------------------------------------------------------
			String unAvailableLayouts = "";

			if (!homePage.isSearchbarPresent()) //Checks if Search bar present
				unAvailableLayouts = "Search Bar is not available;";

			if (homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				unAvailableLayouts = unAvailableLayouts + "Task Pane is available;";

			if (!homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value))
				unAvailableLayouts = unAvailableLayouts + ", Properties Pane is not available;";

			if (!homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				unAvailableLayouts = unAvailableLayouts + "Menu Bar is not available;";

			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				unAvailableLayouts = unAvailableLayouts + "Breadcrumb is not available;";

			//			if (homePage.taskPanel.isAppletEnabled())
			//				unAvailableLayouts = unAvailableLayouts + "Java Applet is available;";

			if (!homePage.listView.isMetadataPropertiesEnabled())
				unAvailableLayouts = unAvailableLayouts + "Metadatacard is not available;";

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page had task pane and java applet not available and search pane, properties pane, top menu, breadcrumb and metadatacard visible on selecting simple listing layout.");
			else
				Log.fail("Test case Failed. Few layots are missing on selecting 'Simple Listing' custom layout. Missed layots : " 
						+ unAvailableLayouts, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_16_2A

	/**
	 * 25.2.16.2B : Hyperlink URL page should have search pane, properties pane, top menu, breadcrumb, java applet and metadatacard visible and task pane, java applet disabled on selecting Simple Listing layout  - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL"}, 
			description = "Hyperlink URL page should have search pane, properties pane, top menu, breadcrumb, java applet and metadatacard visible and task pane, java applet disabled on selecting Simple Listing layout  - Operations menu")
	public void SprintTest25_2_16_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from operations menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-3 : Select 'Simple Listing' Custom layout if not selected and copy the hyperlink url
			//----------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Simple Listing custom layout is selected and hyperlink URL is obtained.");

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("4. Navigated to the URL copied from hyperlink dialog.");			

			//Verification : Verify if all required layouts are available in the page
			//-----------------------------------------------------------------------
			String unAvailableLayouts = "";

			if (!homePage.isSearchbarPresent()) //Checks if Search bar present
				unAvailableLayouts = "Search Bar is not available;";

			if (homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				unAvailableLayouts = unAvailableLayouts + "Task Pane is available;";

			if (!homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value))
				unAvailableLayouts = unAvailableLayouts + ", Properties Pane is not available;";

			if (!homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				unAvailableLayouts = unAvailableLayouts + "Menu Bar is not available;";

			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				unAvailableLayouts = unAvailableLayouts + "Breadcrumb is not available;";

			//			if (homePage.taskPanel.isAppletEnabled())
			//				unAvailableLayouts = unAvailableLayouts + "Java Applet is available;";

			if (!homePage.listView.isMetadataPropertiesEnabled())
				unAvailableLayouts = unAvailableLayouts + "Metadatacard is not available;";

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page had task pane and java applet not available and search pane, properties pane, top menu, breadcrumb and metadatacard visible on selecting simple listing layout.");
			else
				Log.fail("Test case Failed. Few layots are missing on selecting 'Simple Listing' custom layout. Missed layots : " 
						+ unAvailableLayouts, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_16_2B

	/**
	 * 25.2.17.1A : Search area layout only should be available on selecting Search area and on de-selecting other layouts - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL"}, 
			description = "Search area layout only should be available on selecting Search area and on de-selecting other layouts - Context menu")
	public void SprintTest25_2_17_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from context menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-3 : Select 'Search Pane' Custom layout and deselect other layouts and copy the hyperlink url
			//----------------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkLayoutItem(Caption.GetMFilesWebURL.SearchArea.Value, true))//Verifies if member object exists in the hyperlink
				throw new Exception("Search Area layout is selected and other layouts are un-selected");

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Search area custom layout is selected and hyperlink URL is obtained.", driver);

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("4. Navigated to the URL copied from hyperlink dialog.");			

			//Verification : Verify if all required layouts are available in the page
			//-----------------------------------------------------------------------
			String addlInfo = "";

			if (!homePage.isSearchbarPresent()) //Checks if Search bar present
				addlInfo = "Search Bar is not available;";

			if (homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				addlInfo = addlInfo + "Task Pane is available;";

			if (homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value))
				addlInfo = addlInfo + ", Metadata tab is available;";

			if (homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				addlInfo = addlInfo + "Menu Bar is available;";

			if (homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				addlInfo = addlInfo + "Breadcrumb is available;";

			//			if (homePage.taskPanel.isAppletEnabled()) //Checks if Applet is enabled
			//				addlInfo = addlInfo + "Java Applet is available;";

			if (addlInfo.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page has only search area layout visible.");
			else
				Log.fail("Test case Failed. Layouts are not displayed as expected. Additional information : " 
						+ addlInfo, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_17_1A

	/**
	 * 25.2.17.1B : Search area layout only should be available on selecting Search area and on de-selecting other layouts - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL"}, 
			description = "Search area layout only should be available on selecting Search area and on de-selecting other layouts - Operations menu")
	public void SprintTest25_2_17_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from operations menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-3 : Select 'Search Pane' Custom layout and deselect other layouts and copy the hyperlink url
			//----------------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkLayoutItem(Caption.GetMFilesWebURL.SearchArea.Value, true))//Verifies if member object exists in the hyperlink
				throw new Exception("Search Area layout is selected and other layouts are un-selected");

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Search area custom layout is selected and hyperlink URL is obtained.", driver);

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("4. Navigated to the URL copied from hyperlink dialog.");			

			//Verification : Verify if all required layouts are available in the page
			//-----------------------------------------------------------------------
			String addlInfo = "";

			if (!homePage.isSearchbarPresent()) //Checks if Search bar present
				addlInfo = "Search Bar is not available;";

			if (homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				addlInfo = addlInfo + "Task Pane is available;";

			if (homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value))
				addlInfo = addlInfo + ", Metadatacard tab is available;";

			if (homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				addlInfo = addlInfo + "Menu Bar is available;";

			if (homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				addlInfo = addlInfo + "Breadcrumb is available;";

			//			if (homePage.taskPanel.isAppletEnabled()) //Checks if Applet is enabled
			//				addlInfo = addlInfo + "Java Applet is available;";

			if (addlInfo.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page has only search area layout visible.");
			else
				Log.fail("Test case Failed. Layouts are not displayed as expected. Additional information : " 
						+ addlInfo, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_17_1B

	/**
	 * 25.2.18.1A : Task area layout only should be available on selecting Task area and on de-selecting other layouts - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL"}, 
			description = "Task area layout only should be available on selecting Task area and on de-selecting other layouts - Context menu")
	public void SprintTest25_2_18_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from context menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-3 : Select 'Task area' Custom layout and deselect other layouts and copy the hyperlink url
			//----------------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkLayoutItem(Caption.GetMFilesWebURL.TaskArea.Value, true))//Verifies if member object exists in the hyperlink
				throw new Exception("Task Area layout is selected and other layouts are un-selected");

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Task Area custom layout is selected and hyperlink URL is obtained.", driver);

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("4. Navigated to the URL copied from hyperlink dialog.");			

			//Verification : Verify if all required layouts are available in the page
			//-----------------------------------------------------------------------
			String addlInfo = "";

			if (homePage.isSearchbarPresent()) //Checks if Search bar present
				addlInfo = "Search area is available;";

			if (!homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				addlInfo = addlInfo + "Task area is not available;";

			if (homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value))
				addlInfo = addlInfo + ", Metadatacard tab is available;";

			if (homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				addlInfo = addlInfo + "Menu Bar is available;";

			if (homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				addlInfo = addlInfo + "Breadcrumb is available;";

			//			if (homePage.taskPanel.isAppletEnabled()) //Checks if Applet is enabled
			//				addlInfo = addlInfo + "Java Applet is available;";

			if (addlInfo.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page has only task area layout visible.");
			else
				Log.fail("Test case Failed. Layouts are not displayed as expected. Additional information : " 
						+ addlInfo, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_18_1A

	/**
	 * 25.2.18.1B : Task area layout only should be available on selecting Task area and on de-selecting other layouts - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL"}, 
			description = "Task area layout only should be available on selecting Task area and on de-selecting other layouts - Operations menu")
	public void SprintTest25_2_18_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from operations menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-3 : Select 'Task area' Custom layout and deselect other layouts and copy the hyperlink url
			//----------------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkLayoutItem(Caption.GetMFilesWebURL.TaskArea.Value, true))//Verifies if member object exists in the hyperlink
				throw new Exception("Task Area layout is selected and other layouts are un-selected");

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Task area custom layout is selected and hyperlink URL is obtained.", driver);

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("4. Navigated to the URL copied from hyperlink dialog.");			

			//Verification : Verify if all required layouts are available in the page
			//-----------------------------------------------------------------------
			String addlInfo = "";

			if (homePage.isSearchbarPresent()) //Checks if Search bar present
				addlInfo = "Search area is available;";

			if (!homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				addlInfo = addlInfo + "Task area is not available;";

			if (homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value))
				addlInfo = addlInfo + ", Metadatacard tab is available;";

			if (homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				addlInfo = addlInfo + "Menu Bar is available;";

			if (homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				addlInfo = addlInfo + "Breadcrumb is available;";

			//			if (homePage.taskPanel.isAppletEnabled()) //Checks if Applet is enabled
			//				addlInfo = addlInfo + "Java Applet is available;";

			if (addlInfo.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page has only task area layout visible.");
			else
				Log.fail("Test case Failed. Layouts are not displayed as expected. Additional information : " 
						+ addlInfo, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_18_1B

	/**
	 * 25.2.19.1A : Properties pane layout only should be available on selecting Properties pane and on de-selecting other layouts - Context menu
	 */
	@Deprecated
	/*@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL"}, 
			description = "Properties pane layout only should be available on selecting Properties pane and on de-selecting other layouts - Context menu")*/
	public void SprintTest25_2_19_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from context menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-3 : Select 'Properties pane' Custom layout and deselect other layouts and copy the hyperlink url
			//----------------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkLayoutItem(Caption.GetMFilesWebURL.PropertiesPane.Value, true))//Verifies if member object exists in the hyperlink
				throw new Exception("Properties pane layout is selected and other layouts are un-selected");

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Properties pane custom layout is selected and hyperlink URL is obtained.", driver);

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("4. Navigated to the URL copied from hyperlink dialog.");			

			//Verification : Verify if all required layouts are available in the page
			//-----------------------------------------------------------------------
			String addlInfo = "";

			if (homePage.isSearchbarPresent()) //Checks if Search bar present
				addlInfo = "Search area is not available;";

			if (homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				addlInfo = addlInfo + "Task area is available;";

			if (!homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value)) //Checks if properties pane is available
				addlInfo = addlInfo + "Properties Pane is not available;";

			if (homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				addlInfo = addlInfo + "Menu Bar is available;";

			if (homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				addlInfo = addlInfo + "Breadcrumb is available;";

			//			if (homePage.taskPanel.isAppletEnabled()) //Checks if Applet is enabled
			//				addlInfo = addlInfo + "Java Applet is available;";

			if (homePage.listView.isMetadataPropertiesEnabled()) //Checks if Metadata properties is enabled
				addlInfo = addlInfo + "Metadatacard is available;";

			if (addlInfo.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page has only Properties pane layout visible.");
			else
				Log.fail("Test case Failed. Layouts are not displayed as expected. Additional information : " 
						+ addlInfo, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_19_1A

	/**
	 * 25.2.19.1B : Properties pane layout only should be available on selecting Properties pane and on de-selecting other layouts - Operations menu
	 */
	@Deprecated
	/*@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL"}, 
			description = "Properties pane layout only should be available on selecting Properties pane and on de-selecting other layouts - Operations menu")*/
	public void SprintTest25_2_19_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from operations menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-3 : Select 'Properties Pane' Custom layout and deselect other layouts and copy the hyperlink url
			//----------------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkLayoutItem(Caption.GetMFilesWebURL.PropertiesPane.Value, true))//Verifies if member object exists in the hyperlink
				throw new Exception("Properties Pane layout is selected and other layouts are un-selected");

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Properties Pane custom layout is selected and hyperlink URL is obtained.", driver);

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("4. Navigated to the URL copied from hyperlink dialog.");			

			//Verification : Verify if all required layouts are available in the page
			//-----------------------------------------------------------------------
			String addlInfo = "";

			if (homePage.isSearchbarPresent()) //Checks if Search bar present
				addlInfo = "Search area is not available;";

			if (homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				addlInfo = addlInfo + "Task area is available;";

			if (!homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value)) //Checks if properties pane is available
				addlInfo = addlInfo + "Properties Pane is not available;";

			if (homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				addlInfo = addlInfo + "Menu Bar is available;";

			if (homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				addlInfo = addlInfo + "Breadcrumb is available;";

			//			if (homePage.taskPanel.isAppletEnabled()) //Checks if Applet is enabled
			//				addlInfo = addlInfo + "Java Applet is available;";

			if (homePage.listView.isMetadataPropertiesEnabled()) //Checks if Metadata properties is enabled
				addlInfo = addlInfo + "Metadatacard is available;";

			if (addlInfo.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page has only Properties pane layout visible.");
			else
				Log.fail("Test case Failed. Layouts are not displayed as expected. Additional information : " 
						+ addlInfo, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_19_1B

	/**
	 * 25.2.20.1A : Top menu layout only should be available on selecting Top menu and on de-selecting other layouts - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug", "Sprint25", "Get M-Files Web URL"}, 
			description = "Top menu layout only should be available on selecting Top menu and on de-selecting other layouts - Context menu")
	public void SprintTest25_2_20_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from context menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-3 : Select 'Top menu' Custom layout and deselect other layouts and copy the hyperlink url
			//----------------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkLayoutItem(Caption.GetMFilesWebURL.TopMenu.Value, true))//Verifies if member object exists in the hyperlink
				throw new Exception("Top menu layout is selected and other layouts are un-selected");

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Top menu custom layout is selected and hyperlink URL is obtained.", driver);

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("4. Navigated to the URL copied from hyperlink dialog.");			

			//Verification : Verify if all required layouts are available in the page
			//-----------------------------------------------------------------------
			String addlInfo = "";

			if (homePage.isSearchbarPresent()) //Checks if Search bar present
				addlInfo = "Search area is not available;";

			if (homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				addlInfo = addlInfo + "Task area is available;";

			if (homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value)) //Checks if properties pane is available
				addlInfo = addlInfo + ", Metadatacard tab is available;";

			if (!homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				addlInfo = addlInfo + "Menu Bar is not available;";

			if (homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				addlInfo = addlInfo + "Breadcrumb is available;";

			//			if (homePage.taskPanel.isAppletEnabled()) //Checks if Applet is enabled
			//				addlInfo = addlInfo + "Java Applet is available;";

			if (addlInfo.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page has only top menu layout visible.");
			else
				Log.fail("Test case Failed. Layouts are not displayed as expected. Additional information : " 
						+ addlInfo, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_20_1A

	/**
	 * 25.2.20.1B : Top menu layout only should be available on selecting Top menu and on de-selecting other layouts - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug", "Sprint25", "Get M-Files Web URL"}, 
			description = "Top menu layout only should be available on selecting Top menu and on de-selecting other layouts - Operations menu")
	public void SprintTest25_2_20_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from operations menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-3 : Select 'Top menu' Custom layout and deselect other layouts and copy the hyperlink url
			//----------------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkLayoutItem(Caption.GetMFilesWebURL.TopMenu.Value, true))//Verifies if member object exists in the hyperlink
				throw new Exception("Top menu layout is selected and other layouts are un-selected");

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Top menu custom layout is selected and hyperlink URL is obtained.", driver);

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("4. Navigated to the URL copied from hyperlink dialog.");			

			//Verification : Verify if all required layouts are available in the page
			//-----------------------------------------------------------------------
			String addlInfo = "";

			if (homePage.isSearchbarPresent()) //Checks if Search bar present
				addlInfo = "Search area is not available;";

			if (homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				addlInfo = addlInfo + "Task area is available;";

			if (homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value)) //Checks if properties pane is available
				addlInfo = addlInfo + ", Metadatacard tab is available;";

			if (!homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				addlInfo = addlInfo + "Menu Bar is not available;";

			if (homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				addlInfo = addlInfo + "Breadcrumb is available;";

			//			if (homePage.taskPanel.isAppletEnabled()) //Checks if Applet is enabled
			//				addlInfo = addlInfo + "Java Applet is available;";

			if (addlInfo.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page has only top menu layout visible.");
			else
				Log.fail("Test case Failed. Layouts are not displayed as expected. Additional information : " 
						+ addlInfo, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_20_1B

	/**
	 * 25.2.21.1A : Breadcrumb layout only should be available on selecting Breadcrumb and on de-selecting other layouts - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug", "Sprint25", "Get M-Files Web URL"}, 
			description = "Breadcrumb layout only should be available on selecting Breadcrumb and on de-selecting other layouts - Context menu")
	public void SprintTest25_2_21_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from context menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-3 : Select 'Breadcrumb' Custom layout and deselect other layouts and copy the hyperlink url
			//----------------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkLayoutItem(Caption.GetMFilesWebURL.Breadcrumb.Value, true))//Verifies if member object exists in the hyperlink
				throw new Exception("Breadcrumb layout is selected and other layouts are un-selected");

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Breadcrumb custom layout is selected and hyperlink URL is obtained.", driver);

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("4. Navigated to the URL copied from hyperlink dialog.");			

			//Verification : Verify if all required layouts are available in the page
			//-----------------------------------------------------------------------
			String addlInfo = "";

			if (homePage.isSearchbarPresent()) //Checks if Search bar present
				addlInfo = "Search area is not available;";

			if (homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				addlInfo = addlInfo + "Task area is available;";

			if (homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value)) //Checks if properties pane is available
				addlInfo = addlInfo + ", Metadatacard tab is available;";

			if (homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				addlInfo = addlInfo + "Menu Bar is available;";

			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				addlInfo = addlInfo + "Breadcrumb is not available;";

			//			if (homePage.taskPanel.isAppletEnabled()) //Checks if Applet is enabled
			//				addlInfo = addlInfo + "Java Applet is available;";

			if (addlInfo.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page has only Breadcrumb layout visible.");
			else
				Log.fail("Test case Failed. Layouts are not displayed as expected. Additional information : " 
						+ addlInfo, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_21_1A

	/**
	 * 25.2.21.1B : Breadcrumb layout only should be available on selecting Breadcrumb and on de-selecting other layouts - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug", "Sprint25", "Get M-Files Web URL"}, 
			description = "Breadcrumb layout only should be available on selecting Breadcrumb and on de-selecting other layouts - Operations menu")
	public void SprintTest25_2_21_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from operations menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-3 : Select 'Breadcrumb' Custom layout and deselect other layouts and copy the hyperlink url
			//----------------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkLayoutItem(Caption.GetMFilesWebURL.Breadcrumb.Value, true))//Verifies if member object exists in the hyperlink
				throw new Exception("Breadcrumb layout is selected and other layouts are un-selected");

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Breadcrumb custom layout is selected and hyperlink URL is obtained.", driver);

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("4. Navigated to the URL copied from hyperlink dialog.");			

			//Verification : Verify if all required layouts are available in the page
			//-----------------------------------------------------------------------
			String addlInfo = "";

			if (homePage.isSearchbarPresent()) //Checks if Search bar present
				addlInfo = "Search area is not available;";

			if (homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				addlInfo = addlInfo + "Task area is available;";

			if (homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value)) //Checks if properties pane is available
				addlInfo = addlInfo + ", Metadatacard tab is available;";

			if (homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				addlInfo = addlInfo + "Menu Bar is available;";

			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				addlInfo = addlInfo + "Breadcrumb is not available;";

			//			if (homePage.taskPanel.isAppletEnabled()) //Checks if Applet is enabled
			//				addlInfo = addlInfo + "Java Applet is available;";

			if (addlInfo.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page has only Breadcrumb layout visible.");
			else
				Log.fail("Test case Failed. Layouts are not displayed as expected. Additional information : " 
						+ addlInfo, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_21_1B

	/**
	 * 25.2.22.1A : Java applet layout only should be available on selecting Java applet and on de-selecting other layouts - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "SKIP_JavaApplet"}, 
			description = "Java applet layout only should be available on selecting Java applet and on de-selecting other layouts - Context menu")
	public void SprintTest25_2_22_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			if (!homePage.taskPanel.isAppletEnabled())
				throw new SkipException("Java Applet is not enabled.");

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from context menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-3 : Select 'Java Applet' Custom layout and deselect other layouts and copy the hyperlink url
			//----------------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkLayoutItem(Caption.GetMFilesWebURL.JavaApplet.Value, true))//Verifies if member object exists in the hyperlink
				throw new Exception("Java Applet layout is selected and other layouts are un-selected");

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Java Applet custom layout is selected and hyperlink URL is obtained.", driver);

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("4. Navigated to the URL copied from hyperlink dialog.");			

			//Verification : Verify if all required layouts are available in the page
			//-----------------------------------------------------------------------
			String addlInfo = "";

			if (homePage.isSearchbarPresent()) //Checks if Search bar present
				addlInfo = "Search area is not available;";

			if (homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				addlInfo = addlInfo + "Task area is available;";

			if (homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value)) //Checks if properties pane is available
				addlInfo = addlInfo + ", Metadatacard tab is available;";

			if (homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				addlInfo = addlInfo + "Menu Bar is available;";

			if (homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				addlInfo = addlInfo + "Breadcrumb is available;";

			if (!homePage.taskPanel.isAppletEnabled()) //Checks if Applet is enabled
				addlInfo = addlInfo + "Java Applet is not available;";

			if (addlInfo.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page has only Java Applet layout visible.");
			else
				Log.fail("Test case Failed. Layouts are not displayed as expected. Additional information : " 
						+ addlInfo, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_22_1A

	/**
	 * 25.2.22.1B: Java applet layout only should be available on selecting Java applet and on de-selecting other layouts - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "SKIP_JavaApplet"}, 
			description = "Java applet layout only should be available on selecting Java applet and on de-selecting other layouts - Operations menu")
	public void SprintTest25_2_22_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			if (!homePage.taskPanel.isAppletEnabled())
				throw new SkipException("Java Applet is not enabled.");

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from operations menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-3 : Select 'Java Applet' Custom layout and deselect other layouts and copy the hyperlink url
			//----------------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkLayoutItem(Caption.GetMFilesWebURL.JavaApplet.Value, true))//Verifies if member object exists in the hyperlink
				throw new Exception("Java Applet layout is selected and other layouts are un-selected");

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Java Applet custom layout is selected and hyperlink URL is obtained.", driver);

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("4. Navigated to the URL copied from hyperlink dialog.");			

			//Verification : Verify if all required layouts are available in the page
			//-----------------------------------------------------------------------
			String addlInfo = "";

			if (homePage.isSearchbarPresent()) //Checks if Search bar present
				addlInfo = "Search area is not available;";

			if (homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				addlInfo = addlInfo + "Task area is available;";

			if (homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value)) //Checks if properties pane is available
				addlInfo = addlInfo + ", Metadatacard tab is available;";

			if (homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				addlInfo = addlInfo + "Menu Bar is available;";

			if (homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				addlInfo = addlInfo + "Breadcrumb is available;";

			if (!homePage.taskPanel.isAppletEnabled()) //Checks if Applet is enabled
				addlInfo = addlInfo + "Java Applet is not available;";

			if (addlInfo.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page has only Java Applet layout visible.");
			else
				Log.fail("Test case Failed. Layouts are not displayed as expected. Additional information : " 
						+ addlInfo, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_22_1B

	/**
	 * 25.2.23.1A : Metadat card layout only should be available on selecting Metadat card and on de-selecting other layouts - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL"}, 
			description = "Metadat card layout only should be available on selecting Metadat card and on de-selecting other layouts - Context menu")
	public void SprintTest25_2_23_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from context menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-3 : Select 'Metadatacard' Custom layout and deselect other layouts and copy the hyperlink url
			//----------------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkLayoutItem(Caption.GetMFilesWebURL.Metadatacard.Value, true))//Verifies if member object exists in the hyperlink
				throw new Exception("Metadatacard layout is selected and other layouts are un-selected");

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Metadatacard custom layout is selected and hyperlink URL is obtained.", driver);

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("4. Navigated to the URL copied from hyperlink dialog.");			

			//Verification : Verify if all required layouts are available in the page
			//-----------------------------------------------------------------------
			String addlInfo = "";

			if (homePage.isSearchbarPresent()) //Checks if Search bar present
				addlInfo = "Search area is available;";

			if (homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				addlInfo = addlInfo + "Task area is available;";

			if (!homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value)) //Checks if properties pane is available
				addlInfo = addlInfo + ", Metadatacard tab is not available;";

			if (homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				addlInfo = addlInfo + "Menu Bar is available;";

			if (homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				addlInfo = addlInfo + "Breadcrumb is available;";

			//			if (homePage.taskPanel.isAppletEnabled()) //Checks if Applet is enabled
			//				addlInfo = addlInfo + "Java Applet is available;";

			if (addlInfo.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page has only Metadatacard layout visible.");
			else
				Log.fail("Test case Failed. Layouts are not displayed as expected. Additional information : " 
						+ addlInfo, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_23_1A

	/**
	 * 25.2.23.1B : Metadat card layout only should be available on selecting Metadat card and on de-selecting other layouts - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL"}, 
			description = "Metadat card layout only should be available on selecting Metadat card and on de-selecting other layouts - Operations menu")
	public void SprintTest25_2_23_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from operations menu
			//-------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-3 : Select 'Metadatacard' Custom layout and deselect other layouts and copy the hyperlink url
			//----------------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkLayoutItem(Caption.GetMFilesWebURL.Metadatacard.Value, true))//Verifies if member object exists in the hyperlink
				throw new Exception("Metadatacard layout is selected and other layouts are un-selected");

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Metadatacard custom layout is selected and hyperlink URL is obtained.", driver);

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("4. Navigated to the URL copied from hyperlink dialog.");			

			//Verification : Verify if all required layouts are available in the page
			//-----------------------------------------------------------------------
			String addlInfo = "";

			if (homePage.isSearchbarPresent()) //Checks if Search bar present
				addlInfo = "Search area is available;";

			if (homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				addlInfo = addlInfo + "Task area is available;";

			if (!homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value)) //Checks if properties pane is available
				addlInfo = addlInfo + ", Metadatacard tab is not available;";

			if (homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				addlInfo = addlInfo + "Menu Bar is available;";

			if (homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				addlInfo = addlInfo + "Breadcrumb is available;";

			//			if (homePage.taskPanel.isAppletEnabled()) //Checks if Applet is enabled
			//				addlInfo = addlInfo + "Java Applet is available;";

			if (addlInfo.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page has only Metadatacard layout visible.");
			else
				Log.fail("Test case Failed. Layouts are not displayed as expected. Additional information : " 
						+ addlInfo, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest25_2_23_1B


	/**
	 * 41.2.1.2A : Default layout should be available opening hyperlink on selecting 'Show the selected file' after logging out from default page - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint41", "Get M-Files Web URL"}, 
			description = "Default layout should be available opening hyperlink on selecting 'Show the selected file' after logging out from default page - Context menu")
	public void SprintTest41_2_1_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from context menu
			//-------------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0) //Checks if item exists in the list
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") does not have items.");

			String itemName = homePage.listView.getItemNameByItemIndex(0);

			if(!homePage.listView.rightClickItemByIndex(0)) //Clicks on related object
				throw new Exception("First Object in the list is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + itemName + ") is opened from context menu.");

			//Step-3 : Select 'Show the Selected object' option in Get M-Files Web URL dialog
			//-------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show object in Hyperlink dialog
				throw new Exception("Show Selected object is not selected in Get M-Files Web URL dialog.");

			if (!mfilesDialog.setHyperLinkLayoutOption("DEFAULT")) //Selects Default layout
				throw new Exception("Simple Listing is not selected in Get M-Files Web URL dialog.");

			Log.message("3. Show Selected Object is selected in Get M-Files Web URL dialog.");

			//Step-4 : Copy the link from text box and close the Get M-Files Web URL dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied and Get M-Files Web URL dialog is closed. Hyperlink URL : " + hyperlinkText, driver);

			//Step-5 : Log out from default page of MFWA
			//------------------------------------------
			if (!Utility.logOut(driver))
				throw new Exception("Log out is not successful.");

			Log.message("5. Logged out from MFiles web access default page.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver.get(hyperlinkText); //Navigates to the hyperlink url


			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Page is not navigated to login page.");

			Log.message("6. Object Hyperlink is pasted in browser and log in page is displayed.");

			//Step-7 : Log in to the hyperlink
			//--------------------------------
			LoginPage loginPage = new LoginPage(driver);
			homePage = loginPage.loginToWebApplication(userName, password, "");

			Log.message("7. Login credentials provided and login button is clicked.");

			//Verification : To Verify if default layouts is available in hyperlink URL
			//--------------------------------------------------------------------------
			if (!driver.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkText.toUpperCase())) //Checks if hyperlink URL is same as in the dialog
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");			

			if (homePage.listView.itemCount() != 1) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. One item is not getting displayed on opening the hyperlink.");

			if (!homePage.listView.isItemExists(itemName)) //Verifies if object exists in the hyperlink
				throw new Exception("Test case Failed. Object (" + itemName + ") does not exists in the hyperlink.");

			String unAvailableLayouts = Utility.isDefaultLayout(homePage);

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page had search pane, task pane, properties pane, top menu, breadcrumb, java applet and metadatacard visible on selecting Default layout.");
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

	} //End SprintTest41_2_1_2A

	/**
	 * 41.2.1.2B : Default layout should be available opening hyperlink on selecting 'Show the selected file' after logging out from default page - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint41", "Get M-Files Web URL"}, 
			description = "Default layout should be available opening hyperlink on selecting 'Show the selected file' after logging out from default page - Operations menu")
	public void SprintTest41_2_1_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from operations menu
			//----------------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0) //Checks if item exists in the list
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") does not have items.");

			String itemName = homePage.listView.getItemNameByItemIndex(0);

			if(!homePage.listView.clickItemByIndex(0)) //Clicks on object
				throw new Exception("First Object in the list is not right clicked.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + itemName + ") is opened from operations menu.");

			//Step-3 : Select 'Show the Selected object' option in Get M-Files Web URL dialog
			//-------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show object in Hyperlink dialog
				throw new Exception("Show Selected object is not selected in Get M-Files Web URL dialog.");

			if (!mfilesDialog.setHyperLinkLayoutOption("DEFAULT")) //Selects Default layout
				throw new Exception("Simple Listing is not selected in Get M-Files Web URL dialog.");

			Log.message("3. Show Selected Object is selected in Get M-Files Web URL dialog.");

			//Step-4 : Copy the link from text box and close the Get M-Files Web URL dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied and Get M-Files Web URL dialog is closed. Hyperlink URL : " + hyperlinkText, driver);

			//Step-5 : Log out from default page of MFWA
			//------------------------------------------
			if (!Utility.logOut(driver))
				throw new Exception("Log out is not successful.");

			Log.message("5. Logged out from MFiles web access default page.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver.get(hyperlinkText); //Navigates to the hyperlink url


			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Page is not navigated to login page.");

			Log.message("6. Object Hyperlink is pasted in browser and log in page is displayed.");

			//Step-7 : Log in to the hyperlink
			//--------------------------------
			LoginPage loginPage = new LoginPage(driver);
			homePage = loginPage.loginToWebApplication(userName, password, "");

			Log.message("7. Login credentials provided and login button is clicked.");

			//Verification : To Verify if default layouts is available in hyperlink URL
			//--------------------------------------------------------------------------
			if (!driver.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkText.toUpperCase())) //Checks if hyperlink URL is same as in the dialog
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");			

			if (homePage.listView.itemCount() != 1) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. One item is not getting displayed on opening the hyperlink.");

			if (!homePage.listView.isItemExists(itemName)) //Verifies if object exists in the hyperlink
				throw new Exception("Test case Failed. Object (" + itemName + ") does not exists in the hyperlink.");

			String unAvailableLayouts = Utility.isDefaultLayout(homePage);

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page had search pane, task pane, properties pane, top menu, breadcrumb, java applet and metadatacard visible on selecting Default layout.");
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

	} //End SprintTest41_2_1_2B

	/**
	 * 41.2.1.2C : Default layout should be available opening hyperlink on selecting 'Show the selected view' after logging out from default page - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint41", "Get M-Files Web URL"}, 
			description = "Default layout should be available opening hyperlink on selecting 'Show the selected view' after logging out from default page - Context menu")
	public void SprintTest41_2_1_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from context menu
			//-------------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0) //Checks if item exists in the list
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") does not have items.");

			int itemCt = homePage.listView.itemCount();

			if(!homePage.listView.rightClickItemByIndex(0)) //Clicks on related object
				throw new Exception("First Object in the list is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object is opened from context menu.");

			//Step-3 : Select 'Show the current view' option in Get M-Files Web URL dialog
			//-------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("SHOW VIEW")) //Selects Show View from Get M-Files Web URL dialog
				throw new Exception("Show the current view is not selected in Get M-Files Web URL dialog.");

			if (!mfilesDialog.setHyperLinkLayoutOption("DEFAULT")) //Selects Default layout
				throw new Exception("Simple Listing is not selected in Get M-Files Web URL dialog.");

			Log.message("3. Show current view is selected in Get M-Files Web URL dialog.");

			//Step-4 : Copy the link from text box and close the Get M-Files Web URL dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied and Get M-Files Web URL dialog is closed. Hyperlink URL : " + hyperlinkText, driver);

			//Step-5 : Log out from default page of MFWA
			//------------------------------------------
			if (!Utility.logOut(driver))
				throw new Exception("Log out is not successful.");

			Log.message("5. Logged out from MFiles web access default page.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver.get(hyperlinkText); //Navigates to the hyperlink url


			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Page is not navigated to login page.");

			Log.message("6. Object Hyperlink is pasted in browser and log in page is displayed.");

			//Step-7 : Log in to the hyperlink
			//--------------------------------
			LoginPage loginPage = new LoginPage(driver);
			homePage = loginPage.loginToWebApplication(userName, password, "");

			Log.message("7. Login credentials provided and login button is clicked.");

			//Verification : To Verify if default layouts is available in hyperlink URL
			//--------------------------------------------------------------------------
			if (!driver.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkText.toUpperCase())) //Checks if hyperlink URL is same as in the dialog
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");			

			if (homePage.listView.itemCount() != itemCt) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. One item is not getting displayed on opening the hyperlink.");

			String unAvailableLayouts = Utility.isDefaultLayout(homePage);

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page had search pane, task pane, properties pane, top menu, breadcrumb, java applet and metadatacard visible on selecting Default layout.");
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

	} //End SprintTest41_2_1_2C

	/**
	 * 41.2.1.2D : Default layout should be available opening hyperlink on selecting 'Show the selected view' after logging out from default page - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint41", "Get M-Files Web URL"}, 
			description = "Default layout should be available opening hyperlink on selecting 'Show the selected view' after logging out from default page - Operations menu")
	public void SprintTest41_2_1_2D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from operations menu
			//----------------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0) //Checks if item exists in the list
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") does not have items.");

			int itemCt = homePage.listView.itemCount(); 

			if(!homePage.listView.clickItemByIndex(0)) //Clicks on object
				throw new Exception("First Object in the list is not right clicked.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object is opened from operations menu.");

			//Step-3 : Select 'Show the current view' option in Get M-Files Web URL dialog
			//-------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("SHOW VIEW")) //Selects Show View from Get M-Files Web URL dialog
				throw new Exception("Show the current view is not selected in Get M-Files Web URL dialog.");

			if (!mfilesDialog.setHyperLinkLayoutOption("DEFAULT")) //Selects Default layout
				throw new Exception("Simple Listing is not selected in Get M-Files Web URL dialog.");

			Log.message("3. Show current view is selected in Get M-Files Web URL dialog.");

			//Step-4 : Copy the link from text box and close the Get M-Files Web URL dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied and Get M-Files Web URL dialog is closed. Hyperlink URL : " + hyperlinkText, driver);

			//Step-5 : Log out from default page of MFWA
			//------------------------------------------
			if (!Utility.logOut(driver))
				throw new Exception("Log out is not successful.");

			Log.message("5. Logged out from MFiles web access default page.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver.get(hyperlinkText); //Navigates to the hyperlink url

			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Page is not navigated to login page.");

			Log.message("6. Object Hyperlink is pasted in browser and log in page is displayed.");

			//Step-7 : Log in to the hyperlink
			//--------------------------------
			LoginPage loginPage = new LoginPage(driver);
			homePage = loginPage.loginToWebApplication(userName, password, "");

			Log.message("7. Login credentials provided and login button is clicked.");

			//Verification : To Verify if default layouts is available in hyperlink URL
			//--------------------------------------------------------------------------
			if (!driver.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkText.toUpperCase())) //Checks if hyperlink URL is same as in the dialog
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");			

			if (homePage.listView.itemCount() != itemCt) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. One item is not getting displayed on opening the hyperlink.");

			String unAvailableLayouts = Utility.isDefaultLayout(homePage);

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page had search pane, task pane, properties pane, top menu, breadcrumb, java applet and metadatacard visible on selecting Default layout.");
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

	} //End SprintTest41_2_1_2D

	/**
	 * 41.2.1.3A : 'Simple Listing' layout should be available opening hyperlink on selecting 'Show the selected file' after logging out from default page - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint41", "Get M-Files Web URL", "Bug"}, 
			description = "'Simple Listing' layout should be available opening hyperlink on selecting 'Show the selected file' after logging out from default page - Context menu")
	public void SprintTest41_2_1_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from context menu
			//-------------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0) //Checks if item exists in the list
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") does not have items.");

			String itemName = homePage.listView.getItemNameByItemIndex(0);

			if(!homePage.listView.rightClickItemByIndex(0)) //Clicks on related object
				throw new Exception("First Object in the list is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + itemName + ") is opened from context menu.");

			//Step-3 : Select 'Show the Selected object' option in Get M-Files Web URL dialog
			//-------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show object
				throw new Exception("Show Selected object is not selected in Get M-Files Web URL dialog.");

			if (!mfilesDialog.setHyperLinkLayoutOption("SIMPLE LISTING")) //Selects Simple Listing
				throw new Exception("Simple Listing is not selected in Get M-Files Web URL dialog.");

			Log.message("3. Show Selected Object and Simple Listing is selected in Get M-Files Web URL dialog.");

			//Step-4 : Copy the link from text box and close the Get M-Files Web URL dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied and Get M-Files Web URL dialog is closed. Hyperlink URL : " + hyperlinkText, driver);

			//Step-5 : Log out from default page of MFWA
			//------------------------------------------
			if (!Utility.logOut(driver))
				throw new Exception("Log out is not successful.");

			Log.message("5. Logged out from MFiles web access default page.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver.get(hyperlinkText); //Navigates to the hyperlink url


			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Page is not navigated to login page.");

			Log.message("6. Object Hyperlink is pasted in browser and log in page is displayed.");

			//Step-7 : Log in to the hyperlink
			//--------------------------------
			LoginPage loginPage = new LoginPage(driver);
			homePage = loginPage.loginToWebApplication(userName, password, "");

			Log.message("7. Login credentials provided and login button is clicked.");

			//Verification : To Verify if default layouts is available in hyperlink URL
			//--------------------------------------------------------------------------
			if (!driver.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkText.toUpperCase())) //Checks if hyperlink URL is same as in the dialog
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");			

			if (homePage.listView.itemCount() != 1) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. One item is not getting displayed on opening the hyperlink.");

			if (!homePage.listView.isItemExists(itemName)) //Verifies if object exists in the hyperlink
				throw new Exception("Test case Failed. Object (" + itemName + ") does not exists in the hyperlink.");

			String unAvailableLayouts = Utility.isSimpleListingLayout(homePage);

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page had task pane and java applet not available and search pane, properties pane, top menu, breadcrumb and metadatacard visible on selecting simple listing layout.");
			else
				Log.fail("Test case Failed. Few layots are missing on selecting 'Simple Listing' custom layout. Missed layots : " 
						+ unAvailableLayouts, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest41_2_1_3A

	/**
	 * 41.2.1.3B : 'Simple Listing' layout should be available opening hyperlink on selecting 'Show the selected file' after logging out from default page - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint41", "Get M-Files Web URL", "Bug"}, 
			description = "'Simple Listing' layout should be available opening hyperlink on selecting 'Show the selected file' after logging out from default page - Operations menu")
	public void SprintTest41_2_1_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from operations menu
			//----------------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0) //Checks if item exists in the list
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") does not have items.");

			String itemName = homePage.listView.getItemNameByItemIndex(0);

			if(!homePage.listView.clickItemByIndex(0)) //Clicks on object
				throw new Exception("First Object in the list is not right clicked.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + itemName + ") is opened from operations menu.");

			//Step-3 : Select 'Show the Selected object' option in Get M-Files Web URL dialog
			//-------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object
				throw new Exception("Show Selected object is not selected in Get M-Files Web URL dialog.");

			if (!mfilesDialog.setHyperLinkLayoutOption("SIMPLE LISTING")) //Selects Simple Listing
				throw new Exception("Simple Listing is not selected in Get M-Files Web URL dialog.");

			Log.message("3. Show Selected Object is selected in Get M-Files Web URL dialog.");

			//Step-4 : Copy the link from text box and close the Get M-Files Web URL dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied and Get M-Files Web URL dialog is closed. Hyperlink URL : " + hyperlinkText, driver);

			//Step-5 : Log out from default page of MFWA
			//------------------------------------------
			if (!Utility.logOut(driver))
				throw new Exception("Log out is not successful.");

			Log.message("5. Logged out from MFiles web access default page.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver.get(hyperlinkText); //Navigates to the hyperlink url


			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Page is not navigated to login page.");

			Log.message("6. Object Hyperlink is pasted in browser and log in page is displayed.");

			//Step-7 : Log in to the hyperlink
			//--------------------------------
			LoginPage loginPage = new LoginPage(driver);
			homePage = loginPage.loginToWebApplication(userName, password, "");

			Log.message("7. Login credentials provided and login button is clicked.");

			//Verification : To Verify if default layouts is available in hyperlink URL
			//--------------------------------------------------------------------------
			String currURL =  driver.getCurrentUrl();
			if (!currURL.toUpperCase().equalsIgnoreCase(hyperlinkText.toUpperCase())) //Checks if hyperlink URL is same as in the dialog
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same. Current URL : " + currURL + " ; Hyperlink URL : " + hyperlinkText);			

			if (homePage.listView.itemCount() != 1) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. One item is not getting displayed on opening the hyperlink.");

			if (!homePage.listView.isItemExists(itemName)) //Verifies if object exists in the hyperlink
				throw new Exception("Test case Failed. Object (" + itemName + ") does not exists in the hyperlink.");

			String unAvailableLayouts = Utility.isSimpleListingLayout(homePage);

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page had task pane and java applet not available and search pane, properties pane, top menu, breadcrumb and metadatacard visible on selecting simple listing layout.");
			else
				Log.fail("Test case Failed. Few layots are missing on selecting 'Simple Listing' custom layout. Missed layots : " 
						+ unAvailableLayouts, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest41_2_1_3B

	/**
	 * 41.2.1.3C : 'Simple Listing' layout should be available opening hyperlink on selecting 'Show the selected view' after logging out from default page - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint41", "Get M-Files Web URL", "Bug"}, 
			description = "'Simple Listing' layout should be available opening hyperlink on selecting 'Show the selected view' after logging out from default page - Context menu")
	public void SprintTest41_2_1_3C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from context menu
			//-------------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0) //Checks if item exists in the list
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") does not have items.");

			int itemCt = homePage.listView.itemCount();

			if(!homePage.listView.rightClickItemByIndex(0)) //Clicks on related object
				throw new Exception("First Object in the list is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object is opened from context menu.");

			//Step-3 : Select 'Show the current view' option in Get M-Files Web URL dialog
			//-------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("SHOW VIEW")) //Selects Show View
				throw new Exception("Show the current view is not selected in Get M-Files Web URL dialog.");

			if (!mfilesDialog.setHyperLinkLayoutOption("SIMPLE LISTING")) //Selects Simple Listing
				throw new Exception("Simple Listing is not selected in Get M-Files Web URL dialog.");

			Log.message("3. Show current view is selected in Get M-Files Web URL dialog.");

			//Step-4 : Copy the link from text box and close the Get M-Files Web URL dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied and Get M-Files Web URL dialog is closed. Hyperlink URL : " + hyperlinkText, driver);

			//Step-5 : Log out from default page of MFWA
			//------------------------------------------
			if (!Utility.logOut(driver))
				throw new Exception("Log out is not successful.");

			Log.message("5. Logged out from MFiles web access default page.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver.get(hyperlinkText); //Navigates to the hyperlink url


			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Page is not navigated to login page.");

			Log.message("6. Object Hyperlink is pasted in browser and log in page is displayed.");

			//Step-7 : Log in to the hyperlink
			//--------------------------------
			LoginPage loginPage = new LoginPage(driver);
			homePage = loginPage.loginToWebApplication(userName, password, "");

			Log.message("7. Login credentials provided and login button is clicked.");

			//Verification : To Verify if default layouts is available in hyperlink URL
			//--------------------------------------------------------------------------
			if (!driver.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkText.toUpperCase())) //Checks if hyperlink URL is same as in the dialog
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");			

			if (homePage.listView.itemCount() != itemCt) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. One item is not getting displayed on opening the hyperlink.");

			String unAvailableLayouts = Utility.isSimpleListingLayout(homePage);

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page had task pane and java applet not available and search pane, properties pane, top menu, breadcrumb and metadatacard visible on selecting simple listing layout.");
			else
				Log.fail("Test case Failed. Few layots are missing on selecting 'Simple Listing' custom layout. Missed layots : " 
						+ unAvailableLayouts, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest41_2_1_3C

	/**
	 * 41.2.1.3D : 'Simple Listing' layout should be available opening hyperlink on selecting 'Show the selected view' after logging out from default page - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint41", "Get M-Files Web URL", "Bug"}, 
			description = "'Simple Listing' layout should be available opening hyperlink on selecting 'Show the selected view' after logging out from default page - Operations menu")
	public void SprintTest41_2_1_3D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from operations menu
			//----------------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0) //Checks if item exists in the list
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") does not have items.");

			int itemCt = homePage.listView.itemCount(); 

			if(!homePage.listView.clickItemByIndex(0)) //Clicks on object
				throw new Exception("First Object in the list is not right clicked.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object is opened from operations menu.");

			//Step-3 : Select 'Show the current view' option in Get M-Files Web URL dialog
			//-------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("SHOW VIEW")) //Selects Show View
				throw new Exception("Show the current view is not selected in Get M-Files Web URL dialog.");

			if (!mfilesDialog.setHyperLinkLayoutOption("SIMPLE LISTING")) //Selects Simple Listing
				throw new Exception("Simple Listing is not selected in Get M-Files Web URL dialog.");


			Log.message("3. Show current view is selected in Get M-Files Web URL dialog.");

			//Step-4 : Copy the link from text box and close the Get M-Files Web URL dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied and Get M-Files Web URL dialog is closed. Hyperlink URL : " + hyperlinkText, driver);

			//Step-5 : Log out from default page of MFWA
			//------------------------------------------
			if (!Utility.logOut(driver))
				throw new Exception("Log out is not successful.");

			Log.message("5. Logged out from MFiles web access default page.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver.get(hyperlinkText); //Navigates to the hyperlink url


			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Page is not navigated to login page.");

			Log.message("6. Object Hyperlink is pasted in browser and log in page is displayed.");

			//Step-7 : Log in to the hyperlink
			//--------------------------------
			LoginPage loginPage = new LoginPage(driver);
			homePage = loginPage.loginToWebApplication(userName, password, "");

			Log.message("7. Login credentials provided and login button is clicked.");

			//Verification : To Verify if simple listing layouts is available in hyperlink URL
			//--------------------------------------------------------------------------
			if (!driver.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkText.toUpperCase())) //Checks if hyperlink URL is same as in the dialog
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");			

			if (homePage.listView.itemCount() != itemCt) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. One item is not getting displayed on opening the hyperlink.");

			String unAvailableLayouts = Utility.isSimpleListingLayout(homePage);

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page had task pane and java applet not available and search pane, properties pane, top menu, breadcrumb and metadatacard visible on selecting simple listing layout.");
			else
				Log.fail("Test case Failed. Few layots are missing on selecting 'Simple Listing' custom layout. Missed layots : " 
						+ unAvailableLayouts, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest41_2_1_3D


	/**
	 * 41.2.11.2A : Default layout should be available opening hyperlink on selecting 'Show the selected file' in new window - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint41", "Get M-Files Web URL", "SKIPIE11_MultiDriver"}, 
			description = "Default layout should be available opening hyperlink on selecting 'Show the selected file' in new window - Context menu")
	public void SprintTest41_2_11_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("EDGE"))
			throw new SkipException("Second instance of browser will not be opened with active first session for '" + driverType + "' driver type.");

		driver = null; 
		WebDriver driver2 = null;

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from context menu
			//-------------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0) //Checks if item exists in the list
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") does not have items.");

			String itemName = homePage.listView.getItemNameByItemIndex(0);

			if(!homePage.listView.rightClickItemByIndex(0)) //Clicks on related object
				throw new Exception("First Object in the list is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + itemName + ") is opened from context menu.");

			//Step-3 : Select 'Show the Selected object' option in Get M-Files Web URL dialog
			//-------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object in dialog
				throw new Exception("Show Selected object is not selected in Get M-Files Web URL dialog.");

			if (!mfilesDialog.setHyperLinkLayoutOption("DEFAULT")) //Selects Default layout
				throw new Exception("Simple Listing is not selected in Get M-Files Web URL dialog.");

			mfilesDialog.setHyperLinkLayoutItem(Caption.GetMFilesWebURL.JavaApplet.Value);

			Log.message("3. Show Selected Object is selected in Get M-Files Web URL dialog.");

			//Step-4 : Copy the link from text box and close the Get M-Files Web URL dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied and Get M-Files Web URL dialog is closed. Hyperlink URL : " + hyperlinkText, driver);

			//Step-5 : Open new window and open the hyperlink URL in the page
			//---------------------------------------------------------------
			driver2 = WebDriverUtils.getDriver();
			Utils.fluentWait(driver2);

			Log.message("5. New browser window is opened.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver2.get(hyperlinkText);
			Utils.fluentWait(driver2);

			if (!driver2.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Page is not navigated to login page.");

			Log.message("6. Object Hyperlink is pasted in new window and log in page is displayed.");

			//Step-7 : Log in to the hyperlink
			//--------------------------------
			LoginPage loginPage = new LoginPage(driver2);
			homePage = loginPage.loginToWebApplication(userName, password, "");

			Log.message("7. Login credentials provided and login button is clicked.");


			//Verification : To Verify if default layouts is available in hyperlink URL
			//--------------------------------------------------------------------------
			if (!driver2.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkText.replace(".aspx?", ".aspx").toUpperCase())) //Checks if hyperlink URL is same as in the dialog
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");					

			if (homePage.listView.itemCount() != 1) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. One item is not getting displayed on opening the hyperlink.");

			if (!homePage.listView.isItemExists(itemName)) //Verifies if object exists in the hyperlink
				throw new Exception("Test case Failed. Object (" + itemName + ") does not exists in the hyperlink.");

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

			if (homePage.taskPanel.isAppletEnabled())
				unAvailableLayouts = unAvailableLayouts + "Java Applet is enabled;";

			if (!homePage.listView.isMetadataPropertiesEnabled())
				unAvailableLayouts = unAvailableLayouts + "Metadatacard;";

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page had search pane, task pane, properties pane, top menu, breadcrumb, java applet and metadatacard visible on selecting Default layout.");
			else
				Log.fail("Test case Failed. Few layots are missing on selecting 'Default' custom layout. Missed layots : " 
						+ unAvailableLayouts, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
			if (driver2 != null) {Utility.quitDriver(driver2);}
		} //End finally

	} //End SprintTest41_2_11_2A

	/**
	 * 41.2.11.2B : Default layout should be available opening hyperlink on selecting 'Show the selected file' in new window - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint41", "Get M-Files Web URL", "SKIPIE11_MultiDriver"}, 
			description = "Default layout should be available opening hyperlink on selecting 'Show the selected file' in new window - Operations menu")
	public void SprintTest41_2_11_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("EDGE"))
			throw new SkipException("Second instance of browser will not be opened with active first session for '" + driverType + "' driver type.");

		driver = null; 
		WebDriver driver2 = null;

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from operations menu
			//----------------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0) //Checks if item exists in the list
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") does not have items.");

			String itemName = homePage.listView.getItemNameByItemIndex(0);

			if(!homePage.listView.clickItemByIndex(0)) //Clicks on object
				throw new Exception("First Object in the list is not right clicked.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + itemName + ") is opened from operations menu.");

			//Step-3 : Select 'Show the Selected object' option in Get M-Files Web URL dialog
			//-------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object in dialog
				throw new Exception("Show Selected object is not selected in Get M-Files Web URL dialog.");

			if (!mfilesDialog.setHyperLinkLayoutOption("DEFAULT")) //Selects Default layout
				throw new Exception("Simple Listing is not selected in Get M-Files Web URL dialog.");

			mfilesDialog.setHyperLinkLayoutItem(Caption.GetMFilesWebURL.JavaApplet.Value);

			Log.message("3. Show Selected Object is selected in Get M-Files Web URL dialog.");

			//Step-4 : Copy the link from text box and close the Get M-Files Web URL dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied and Get M-Files Web URL dialog is closed. Hyperlink URL : " + hyperlinkText, driver);

			//Step-5 : Open new window and open the hyperlink URL in the page
			//---------------------------------------------------------------
			driver2 = WebDriverUtils.getDriver();
			Utils.fluentWait(driver2);

			Log.message("5. New browser window is opened.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver2.get(hyperlinkText);
			Utils.fluentWait(driver2);

			if (!driver2.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Page is not navigated to login page.");

			Log.message("6. Object Hyperlink is pasted in new window and log in page is displayed.");

			//Step-7 : Log in to the hyperlink
			//--------------------------------
			LoginPage loginPage = new LoginPage(driver2);
			homePage = loginPage.loginToWebApplication(userName, password, "");

			Log.message("7. Login credentials provided and login button is clicked.");

			//Verification : To Verify if default layouts is available in hyperlink URL
			//--------------------------------------------------------------------------
			if (!driver2.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkText.replace(".aspx?", ".aspx").toUpperCase())) //Checks if hyperlink URL is same as in the dialog
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");

			if (homePage.listView.itemCount() != 1) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. One item is not getting displayed on opening the hyperlink.");

			if (!homePage.listView.isItemExists(itemName)) //Verifies if object exists in the hyperlink
				throw new Exception("Test case Failed. Object (" + itemName + ") does not exists in the hyperlink.");

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

			if (homePage.taskPanel.isAppletEnabled())
				unAvailableLayouts = unAvailableLayouts + "Java Applet is enabled;";

			if (!homePage.listView.isMetadataPropertiesEnabled())
				unAvailableLayouts = unAvailableLayouts + "Metadatacard;";

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page had search pane, task pane, properties pane, top menu, breadcrumb, java applet and metadatacard visible on selecting Default layout.");
			else
				Log.fail("Test case Failed. Few layots are missing on selecting 'Default' custom layout. Missed layots : " 
						+ unAvailableLayouts, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
			if (driver2 != null) {Utility.quitDriver(driver2);}
		} //End finally

	} //End SprintTest41_2_11_2B

	/**
	 * 41.2.11.2C : Default layout should be available opening hyperlink on selecting 'Show the selected view' in new window - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint41", "Get M-Files Web URL", "SKIPIE11_MultiDriver"}, 
			description = "Default layout should be available opening hyperlink on selecting 'Show the selected view' in new window - Context menu")
	public void SprintTest41_2_11_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("EDGE"))
			throw new SkipException("Second instance of browser will not be opened with active first session for '" + driverType + "' driver type.");

		driver = null; 
		WebDriver driver2 = null;

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from context menu
			//-------------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0) //Checks if item exists in the list
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") does not have items.");

			int itemCt = homePage.listView.itemCount();

			if(!homePage.listView.rightClickItemByIndex(0)) //Clicks on related object
				throw new Exception("First Object in the list is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object is opened from context menu.");

			//Step-3 : Select 'Show the current view' option in Get M-Files Web URL dialog
			//-------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("SHOW VIEW")) //Selects Show View from Get M-Files Web URL dialog
				throw new Exception("Show the current view is not selected in Get M-Files Web URL dialog.");

			if (!mfilesDialog.setHyperLinkLayoutOption("DEFAULT")) //Selects Default layout
				throw new Exception("Simple Listing is not selected in Get M-Files Web URL dialog.");

			mfilesDialog.setHyperLinkLayoutItem(Caption.GetMFilesWebURL.JavaApplet.Value);

			Log.message("3. Show current view is selected in Get M-Files Web URL dialog.");

			//Step-4 : Copy the link from text box and close the Get M-Files Web URL dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied and Get M-Files Web URL dialog is closed. Hyperlink URL : " + hyperlinkText, driver);

			//Step-5 : Open new window and open the hyperlink URL in the page
			//---------------------------------------------------------------
			driver2 = WebDriverUtils.getDriver();
			Utils.fluentWait(driver2);

			Log.message("5. New browser window is opened.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver2.get(hyperlinkText);
			Utils.fluentWait(driver2);

			if (!driver2.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Page is not navigated to login page.");

			Log.message("6. Object Hyperlink is pasted in new window and log in page is displayed.");

			//Step-7 : Log in to the hyperlink
			//--------------------------------
			LoginPage loginPage = new LoginPage(driver2);
			homePage = loginPage.loginToWebApplication(userName, password, "");

			Log.message("7. Login credentials provided and login button is clicked.");

			//Verification : To Verify if default layouts is available in hyperlink URL
			//--------------------------------------------------------------------------
			if (!driver2.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkText.replace(".aspx?", ".aspx").toUpperCase())) //Checks if hyperlink URL is same as in the dialog
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");


			if (homePage.listView.itemCount() != itemCt) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. One item is not getting displayed on opening the hyperlink.");

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

			if (homePage.taskPanel.isAppletEnabled())
				unAvailableLayouts = unAvailableLayouts + "Java Applet is enabled;";

			if (!homePage.listView.isMetadataPropertiesEnabled())
				unAvailableLayouts = unAvailableLayouts + "Metadatacard;";

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page had search pane, task pane, properties pane, top menu, breadcrumb, java applet and metadatacard visible on selecting Default layout.");
			else
				Log.fail("Test case Failed. Few layots are missing on selecting 'Default' custom layout. Missed layots : " 
						+ unAvailableLayouts, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
			if (driver2 != null) {Utility.quitDriver(driver2);}
		} //End finally

	} //End SprintTest41_2_11_2C

	/**
	 * 41.2.11.2D : Default layout should be available opening hyperlink on selecting 'Show the selected view' in new window - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint41", "Get M-Files Web URL", "SKIPIE11_MultiDriver"}, 
			description = "Default layout should be available opening hyperlink on selecting 'Show the selected view' in new window - Operations menu")
	public void SprintTest41_2_11_2D(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("EDGE"))
			throw new SkipException("Second instance of browser will not be opened with active first session for '" + driverType + "' driver type.");

		driver = null; 
		WebDriver driver2 = null;

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from operations menu
			//----------------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0) //Checks if item exists in the list
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") does not have items.");

			int itemCt = homePage.listView.itemCount(); 

			if(!homePage.listView.clickItemByIndex(0)) //Clicks on object
				throw new Exception("First Object in the list is not right clicked.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object is opened from operations menu.");

			//Step-3 : Select 'Show the current view' option in Get M-Files Web URL dialog
			//-------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("SHOW VIEW")) //Selects Show View from Get M-Files Web URL dialog
				throw new Exception("Show the current view is not selected in Get M-Files Web URL dialog.");

			if (!mfilesDialog.setHyperLinkLayoutOption("DEFAULT")) //Selects Default layout
				throw new Exception("Simple Listing is not selected in Get M-Files Web URL dialog.");

			mfilesDialog.setHyperLinkLayoutItem(Caption.GetMFilesWebURL.JavaApplet.Value);

			Log.message("3. Show current view is selected in Get M-Files Web URL dialog.");

			//Step-4 : Copy the link from text box and close the Get M-Files Web URL dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied and Get M-Files Web URL dialog is closed. Hyperlink URL : " + hyperlinkText, driver);

			//Step-5 : Open new window and open the hyperlink URL in the page
			//---------------------------------------------------------------
			driver2 = WebDriverUtils.getDriver();
			Utils.fluentWait(driver2);

			Log.message("5. New browser window is opened.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver2.get(hyperlinkText);
			Utils.fluentWait(driver2);

			if (!driver2.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Page is not navigated to login page.");

			Log.message("6. Object Hyperlink is pasted in new window and log in page is displayed.");

			//Step-7 : Log in to the hyperlink
			//--------------------------------
			LoginPage loginPage = new LoginPage(driver2);
			homePage = loginPage.loginToWebApplication(userName, password, "");

			Log.message("7. Login credentials provided and login button is clicked.");

			//Verification : To Verify if default layouts is available in hyperlink URL
			//--------------------------------------------------------------------------
			if (!driver2.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkText.replace(".aspx?", ".aspx").toUpperCase())) //Checks if hyperlink URL is same as in the dialog
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");		

			if (homePage.listView.itemCount() != itemCt) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. One item is not getting displayed on opening the hyperlink.");

			String unAvailableLayouts = Utility.isDefaultLayout(homePage);

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page had search pane, task pane, properties pane, top menu, breadcrumb, java applet and metadatacard visible on selecting Default layout.");
			else
				Log.fail("Test case Failed. Few layots are missing on selecting 'Default' custom layout. Missed layots : " 
						+ unAvailableLayouts, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
			if (driver2 != null) {Utility.quitDriver(driver2);}
		} //End finally

	} //End SprintTest41_2_11_2D

	/**
	 * 41.2.11.3A : Simple Listing layout should be available opening hyperlink on selecting 'Show the selected file' in new window - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint41", "Get M-Files Web URL","Bug", "SKIPIE11_MultiDriver"}, 
			description = "Simple Listing layout should be available opening hyperlink on selecting 'Show the selected file' in new window - Context menu")
	public void SprintTest41_2_11_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("EDGE"))
			throw new SkipException("Second instance of browser will not be opened with active first session for '" + driverType + "' driver type.");

		driver = null; 
		WebDriver driver2 = null;

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from context menu
			//-------------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0) //Checks if item exists in the list
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") does not have items.");

			String itemName = homePage.listView.getItemNameByItemIndex(0);

			if(!homePage.listView.rightClickItemByIndex(0)) //Clicks on related object
				throw new Exception("First Object in the list is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + itemName + ") is opened from context menu.");

			//Step-3 : Select 'Show the Selected object' option in Get M-Files Web URL dialog
			//-------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show object
				throw new Exception("Show Selected object is not selected in Get M-Files Web URL dialog.");

			if (!mfilesDialog.setHyperLinkLayoutOption("SIMPLE LISTING")) //Selects Simple Listing
				throw new Exception("Simple Listing is not selected in Get M-Files Web URL dialog.");

			Log.message("3. Show Selected Object and Simple Listing is selected in Get M-Files Web URL dialog.");

			//Step-4 : Copy the link from text box and close the Get M-Files Web URL dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied and Get M-Files Web URL dialog is closed. Hyperlink URL : " + hyperlinkText, driver);

			//Step-5 : Open new window and open the hyperlink URL in the page
			//---------------------------------------------------------------
			driver2 = WebDriverUtils.getDriver();
			Utils.fluentWait(driver2);

			Log.message("5. New browser window is opened.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver2.get(hyperlinkText);
			Utils.fluentWait(driver2);

			if (!driver2.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Page is not navigated to login page.");

			Log.message("6. Object Hyperlink is pasted in new window and log in page is displayed.");

			//Step-7 : Log in to the hyperlink
			//--------------------------------
			LoginPage loginPage = new LoginPage(driver2);
			homePage = loginPage.loginToWebApplication(userName, password, "");

			Log.message("7. Login credentials provided and login button is clicked.");

			//Verification : To Verify if default layouts is available in hyperlink URL
			//--------------------------------------------------------------------------
			if (!driver2.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkText.toUpperCase())) //Checks if hyperlink URL is same as in the dialog
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");			

			if (homePage.listView.itemCount() != 1) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. One item is not getting displayed on opening the hyperlink.");

			if (!homePage.listView.isItemExists(itemName)) //Verifies if object exists in the hyperlink
				throw new Exception("Test case Failed. Object (" + itemName + ") does not exists in the hyperlink.");

			String unAvailableLayouts = "";

			if (!homePage.isSearchbarPresent()) //Checks if Search bar present
				unAvailableLayouts = "Search Bar is not available;";

			if (homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				unAvailableLayouts = unAvailableLayouts + "Task Pane is available;";

			if (!homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value))
				unAvailableLayouts = unAvailableLayouts + ", Properties Pane is not available;";

			if (!homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				unAvailableLayouts = unAvailableLayouts + "Menu Bar is not available;";

			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				unAvailableLayouts = unAvailableLayouts + "Breadcrumb is not available;";

			if (!homePage.taskPanel.isAppletEnabled())
				unAvailableLayouts = unAvailableLayouts + "Java Applet is available;";

			if (!homePage.listView.isMetadataPropertiesEnabled())
				unAvailableLayouts = unAvailableLayouts + "Metadatacard is not available;";

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page had task pane and java applet not available and search pane, properties pane, top menu, breadcrumb and metadatacard visible on selecting simple listing layout.");
			else
				Log.fail("Test case Failed. Few layots are missing on selecting 'Simple Listing' custom layout. Missed layots : " 
						+ unAvailableLayouts, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
			if (driver2 != null) {Utility.quitDriver(driver2);}
		} //End finally

	} //End SprintTest41_2_11_3A

	/**
	 * 41.2.11.3B : Simple Listing layout should be available opening hyperlink on selecting 'Show the selected file' in new window - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint41", "Get M-Files Web URL","Bug", "SKIPIE11_MultiDriver"}, 
			description = "Simple Listing layout should be available opening hyperlink on selecting 'Show the selected file' in new window - Operations menu")
	public void SprintTest41_2_11_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("EDGE"))
			throw new SkipException("Second instance of browser will not be opened with active first session for '" + driverType + "' driver type.");

		driver = null; 
		WebDriver driver2 = null;

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from operations menu
			//----------------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0) //Checks if item exists in the list
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") does not have items.");

			String itemName = homePage.listView.getItemNameByItemIndex(0);

			if(!homePage.listView.clickItemByIndex(0)) //Clicks on object
				throw new Exception("First Object in the list is not right clicked.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object (" + itemName + ") is opened from operations menu.");

			//Step-3 : Select 'Show the Selected object' option in Get M-Files Web URL dialog
			//-------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show object
				throw new Exception("Show Selected object is not selected in Get M-Files Web URL dialog.");

			if (!mfilesDialog.setHyperLinkLayoutOption("SIMPLE LISTING")) //Selects Simple Listing
				throw new Exception("Simple Listing is not selected in Get M-Files Web URL dialog.");

			Log.message("3. Show Selected Object and Simple Listing is selected in Get M-Files Web URL dialog.");

			//Step-4 : Copy the link from text box and close the Get M-Files Web URL dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied and Get M-Files Web URL dialog is closed. Hyperlink URL : " + hyperlinkText, driver);

			//Step-5 : Open new window and open the hyperlink URL in the page
			//---------------------------------------------------------------
			driver2 = WebDriverUtils.getDriver();
			Utils.fluentWait(driver2);

			Log.message("5. New browser window is opened.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver2.get(hyperlinkText);
			Utils.fluentWait(driver2);

			if (!driver2.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Page is not navigated to login page.");

			Log.message("6. Object Hyperlink is pasted in new window and log in page is displayed.");

			//Step-7 : Log in to the hyperlink
			//--------------------------------
			LoginPage loginPage = new LoginPage(driver2);
			homePage = loginPage.loginToWebApplication(userName, password, "");

			Log.message("7. Login credentials provided and login button is clicked.");

			//Verification : To Verify if default layouts is available in hyperlink URL
			//--------------------------------------------------------------------------
			if (!driver2.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkText.toUpperCase())) //Checks if hyperlink URL is same as in the dialog
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");			

			if (homePage.listView.itemCount() != 1) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. One item is not getting displayed on opening the hyperlink.");

			if (!homePage.listView.isItemExists(itemName)) //Verifies if object exists in the hyperlink
				throw new Exception("Test case Failed. Object (" + itemName + ") does not exists in the hyperlink.");

			String unAvailableLayouts = "";

			if (!homePage.isSearchbarPresent()) //Checks if Search bar present
				unAvailableLayouts = "Search Bar is not available;";

			if (homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				unAvailableLayouts = unAvailableLayouts + "Task Pane is available;";

			if (!homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value))
				unAvailableLayouts = unAvailableLayouts + ", Properties Pane is not available;";

			if (!homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				unAvailableLayouts = unAvailableLayouts + "Menu Bar is not available;";

			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				unAvailableLayouts = unAvailableLayouts + "Breadcrumb is not available;";

			//			if (homePage.taskPanel.isAppletEnabled())
			//				unAvailableLayouts = unAvailableLayouts + "Java Applet is available;";

			if (!homePage.listView.isMetadataPropertiesEnabled())
				unAvailableLayouts = unAvailableLayouts + "Metadatacard is not available;";

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page had task pane and java applet not available and search pane, properties pane, top menu, breadcrumb and metadatacard visible on selecting simple listing layout.");
			else
				Log.fail("Test case Failed. Few layots are missing on selecting 'Simple Listing' custom layout. Missed layots : " 
						+ unAvailableLayouts, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
			if (driver2 != null) {Utility.quitDriver(driver2);}
		} //End finally

	} //End SprintTest41_2_11_3B

	/**
	 * 41.2.11.3C : Simple Listing layout should be available opening hyperlink on selecting 'Show the selected view' in new window - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint41", "Get M-Files Web URL","Bug", "SKIPIE11_MultiDriver"}, 
			description = "Simple Listing layout should be available opening hyperlink on selecting 'Show the selected view' in new window - Context menu")
	public void SprintTest41_2_11_3C(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("EDGE"))
			throw new SkipException("Second instance of browser will not be opened with active first session for '" + driverType + "' driver type.");

		driver = null; 
		WebDriver driver2 = null;

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from context menu
			//-------------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0) //Checks if item exists in the list
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") does not have items.");

			int itemCt = homePage.listView.itemCount();

			if(!homePage.listView.rightClickItemByIndex(0)) //Clicks on related object
				throw new Exception("First Object in the list is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object is opened from context menu.");

			//Step-3 : Select 'Show the current view' option in Get M-Files Web URL dialog
			//-------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("SHOW VIEW")) //Selects Show View
				throw new Exception("Show the current view is not selected in Get M-Files Web URL dialog.");

			if (!mfilesDialog.setHyperLinkLayoutOption("SIMPLE LISTING")) //Selects Simple Listing
				throw new Exception("Simple Listing is not selected in Get M-Files Web URL dialog.");

			Log.message("3. Show current view and Simple Listing is selected in Get M-Files Web URL dialog.");

			//Step-4 : Copy the link from text box and close the Get M-Files Web URL dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied and Get M-Files Web URL dialog is closed. Hyperlink URL : " + hyperlinkText, driver);

			//Step-5 : Open new window and open the hyperlink URL in the page
			//---------------------------------------------------------------
			driver2 = WebDriverUtils.getDriver();
			Utils.fluentWait(driver2);

			Log.message("5. New browser window is opened.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver2.get(hyperlinkText);
			Utils.fluentWait(driver2);

			if (!driver2.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Page is not navigated to login page.");

			Log.message("6. Object Hyperlink is pasted in new window and log in page is displayed.");

			//Step-7 : Log in to the hyperlink
			//--------------------------------
			LoginPage loginPage = new LoginPage(driver2);
			homePage = loginPage.loginToWebApplication(userName, password, "");

			Log.message("7. Login credentials provided and login button is clicked.");

			//Verification : To Verify if default layouts is available in hyperlink URL
			//--------------------------------------------------------------------------
			if (!driver2.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkText.toUpperCase())) //Checks if hyperlink URL is same as in the dialog
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");			

			if (homePage.listView.itemCount() != itemCt) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. No if items are not getting displayed as same as in the view.");

			String unAvailableLayouts = "";

			if (!homePage.isSearchbarPresent()) //Checks if Search bar present
				unAvailableLayouts = "Search Bar is not available;";

			if (homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				unAvailableLayouts = unAvailableLayouts + "Task Pane is available;";

			if (!homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value))
				unAvailableLayouts = unAvailableLayouts + ", Properties Pane is not available;";

			if (!homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				unAvailableLayouts = unAvailableLayouts + "Menu Bar is not available;";

			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				unAvailableLayouts = unAvailableLayouts + "Breadcrumb is not available;";

			//			if (homePage.taskPanel.isAppletEnabled())
			//				unAvailableLayouts = unAvailableLayouts + "Java Applet is available;";

			if (!homePage.listView.isMetadataPropertiesEnabled())
				unAvailableLayouts = unAvailableLayouts + "Metadatacard is not available;";

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page had task pane and java applet not available and search pane, properties pane, top menu, breadcrumb and metadatacard visible on selecting simple listing layout.");
			else
				Log.fail("Test case Failed. Few layots are missing on selecting 'Simple Listing' custom layout. Missed layots : " 
						+ unAvailableLayouts, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
			if (driver2 != null) {Utility.quitDriver(driver2);}
		} //End finally

	} //End SprintTest41_2_11_3C

	/**
	 * 41.2.11.3D : Simple Listing layout should be available opening hyperlink on selecting 'Show the selected view' in new window - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint41", "Get M-Files Web URL","Bug", "SKIPIE11_MultiDriver"}, 
			description = "Simple Listing layout should be available opening hyperlink on selecting 'Show the selected view' in new window - Operations menu")
	public void SprintTest41_2_11_3D(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("EDGE"))
			throw new SkipException("Second instance of browser will not be opened with active first session for '" + driverType + "' driver type.");

		driver = null; 
		WebDriver driver2 = null;

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and open Get M-Files Web URL dialog from operations menu
			//----------------------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0) //Checks if item exists in the list
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") does not have items.");

			int itemCt = homePage.listView.itemCount(); 

			if(!homePage.listView.clickItemByIndex(0)) //Clicks on object
				throw new Exception("First Object in the list is not right clicked.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("2. Get M-Files Web URL dialog of an object is opened from operations menu.");

			//Step-3 : Select 'Show the current view' option in Get M-Files Web URL dialog
			//-------------------------------------------------------------------------
			if (!mfilesDialog.setHyperLinkAction("SHOW VIEW")) //Selects Show View
				throw new Exception("Show the current view is not selected in Get M-Files Web URL dialog.");

			if (!mfilesDialog.setHyperLinkLayoutOption("SIMPLE LISTING")) //Selects Simple Listing
				throw new Exception("Simple Listing is not selected in Get M-Files Web URL dialog.");

			Log.message("3. Show current view is selected in Get M-Files Web URL dialog.");

			//Step-4 : Copy the link from text box and close the Get M-Files Web URL dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied and Get M-Files Web URL dialog is closed. Hyperlink URL : " + hyperlinkText, driver);

			//Step-5 : Open new window and open the hyperlink URL in the page
			//---------------------------------------------------------------
			driver2 = WebDriverUtils.getDriver();
			Utils.fluentWait(driver2);

			Log.message("5. New browser window is opened.");

			//Step-6 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver2.get(hyperlinkText);
			Utils.fluentWait(driver2);

			if (!driver2.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				throw new Exception ("Page is not navigated to login page.");

			Log.message("6. Object Hyperlink is pasted in new window and log in page is displayed.");

			//Step-7 : Log in to the hyperlink
			//--------------------------------
			LoginPage loginPage = new LoginPage(driver2);
			homePage = loginPage.loginToWebApplication(userName, password, "");

			Log.message("7. Login credentials provided and login button is clicked.");

			//Verification : To Verify if default layouts is available in hyperlink URL
			//--------------------------------------------------------------------------
			if (!driver2.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkText.toUpperCase())) //Checks if hyperlink URL is same as in the dialog
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");			

			if (homePage.listView.itemCount() != itemCt) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. One item is not getting displayed on opening the hyperlink.");

			String unAvailableLayouts = "";

			if (!homePage.isSearchbarPresent()) //Checks if Search bar present
				unAvailableLayouts = "Search Bar is not available;";

			if (homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				unAvailableLayouts = unAvailableLayouts + "Task Pane is available;";

			if (!homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value))
				unAvailableLayouts = unAvailableLayouts + ", Properties Pane is not available;";

			if (!homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				unAvailableLayouts = unAvailableLayouts + "Menu Bar is not available;";

			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				unAvailableLayouts = unAvailableLayouts + "Breadcrumb is not available;";

			//			if (homePage.taskPanel.isAppletEnabled())
			//				unAvailableLayouts = unAvailableLayouts + "Java Applet is available;";

			if (!homePage.listView.isMetadataPropertiesEnabled())
				unAvailableLayouts = unAvailableLayouts + "Metadatacard is not available;";

			if (unAvailableLayouts.equals("")) //Verifies default layout have selected default custom layout
				Log.pass("Test case Passed. Hyperlink URL page had task pane and java applet not available and search pane, properties pane, top menu, breadcrumb and metadatacard visible on selecting simple listing layout.");
			else
				Log.fail("Test case Failed. Few layots are missing on selecting 'Simple Listing' custom layout. Missed layots : " 
						+ unAvailableLayouts, driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
			if (driver2 != null) {Utility.quitDriver(driver2);}
		} //End finally

	} //End SprintTest41_2_11_3D


	/**
	 * 54.1.34.1A : Right pane with Metadata and Preview tab should be available in the selected object of default layout of hyperlink URL - context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Right pane with Metadata and Preview tab should be available in the selected object of default layout of hyperlink URL - context menu")
	public void SprintTest54_1_34_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

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


			Log.message("4. Navigated to the URL copied from hyperlink dialog.");			

			//Verification : Verify if Rightpane with metadata and preview exists
			//-------------------------------------------------------------------
			if (!homePage.isPreviewPaneDisplayed())
				throw new Exception("Test case Failed, Right pane is not displayed.");

			if (!homePage.previewPane.isTabExists(Caption.PreviewPane.PreviewTab.Value))
				throw new Exception("Test case Failed, Preview tab in right pane is not displayed.");

			if (homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value))
				Log.pass("Test case Passed. Right pane with Metadata and Preview tab exists in hyperlink URL page.");
			else
				Log.fail("Test case Failed. Right pane with Metadata and Preview tab exists does not exists hyperlink URL page.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_1_34_1A

	/**
	 * 54.1.34.1B: Right pane with Metadata and Preview tab should be available in the selected object of default layout of hyperlink URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Right pane with Metadata and Preview tab should be available in the selected object of default layout of hyperlink URL - Operations menu")
	public void SprintTest54_1_34_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

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


			Log.message("4. Navigated to the URL copied from hyperlink dialog.");			

			//Verification : Verify if Rightpane with metadata and preview exists
			//-------------------------------------------------------------------
			if (!homePage.isPreviewPaneDisplayed())
				throw new Exception("Test case Failed, Right pane is not displayed.");

			if (!homePage.previewPane.isTabExists(Caption.PreviewPane.PreviewTab.Value))
				throw new Exception("Test case Failed, Preview tab in right pane is not displayed.");

			if (homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value))
				Log.pass("Test case Passed. Right pane with Metadata and Preview tab exists in hyperlink URL page.");
			else
				Log.fail("Test case Failed. Right pane with Metadata and Preview tab exists does not exists hyperlink URL page.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_1_34_1B

	/**
	 * 54.1.34.2A : Right pane with Metadata and Preview tab should be available in the selected object of simple listing layout of hyperlink URL - context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Right pane with Metadata and Preview tab should be available in the selected object of simple listing layout of hyperlink URL - context menu")
	public void SprintTest54_1_34_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

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


			Log.message("4. Navigated to the URL copied from hyperlink dialog.");			

			//Verification : Verify if Rightpane with metadata and preview exists
			//-------------------------------------------------------------------
			if (!homePage.isPreviewPaneDisplayed())
				throw new Exception("Test case Failed, Right pane is not displayed.");

			if (!homePage.previewPane.isTabExists(Caption.PreviewPane.PreviewTab.Value))
				throw new Exception("Test case Failed, Preview tab in right pane is not displayed.");

			if (homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value))
				Log.pass("Test case Passed. Right pane with Metadata and Preview tab exists in hyperlink URL page.");
			else
				Log.fail("Test case Failed. Right pane with Metadata and Preview tab exists does not exists hyperlink URL page.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_1_34_2A

	/**
	 * 54.1.34.2B : Right pane with Metadata and Preview tab should be available in the selected object of simple listing layout of hyperlink URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Right pane with Metadata and Preview tab should be available in the selected object of simple listing layout of hyperlink URL - Operations menu")
	public void SprintTest54_1_34_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT"))
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Show the selected File and Simple Listing custom layout are selected and hyperlink URL is obtained.");

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("4. Navigated to the URL copied from hyperlink dialog.");			

			//Verification : Verify if Rightpane with metadata and preview exists
			//-------------------------------------------------------------------
			if (!homePage.isPreviewPaneDisplayed())
				throw new Exception("Test case Failed, Right pane is not displayed.");

			if (!homePage.previewPane.isTabExists(Caption.PreviewPane.PreviewTab.Value))
				throw new Exception("Test case Failed, Preview tab in right pane is not displayed.");

			if (homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value))
				Log.pass("Test case Passed. Right pane with Metadata and Preview tab exists in hyperlink URL page.");
			else
				Log.fail("Test case Failed. Right pane with Metadata and Preview tab exists does not exists hyperlink URL page.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_1_34_2B

	/**
	 * 54.1.34.3A : Right pane with Metadata and Preview tab should be available in the selected view of default layout of hyperlink URL - context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Right pane with Metadata and Preview tab should be available in the selected view of default layout of hyperlink URL - context menu")
	public void SprintTest54_1_34_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-3 : Select 'Show the selected View', 'Default' Custom layout if not selected and copy the hyperlink url
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


			Log.message("4. Navigated to the URL copied from hyperlink dialog.");			

			//Verification : Verify if Rightpane with metadata and preview exists
			//-------------------------------------------------------------------
			if (!homePage.isPreviewPaneDisplayed())
				throw new Exception("Test case Failed, Right pane is not displayed.");

			if (!homePage.previewPane.isTabExists(Caption.PreviewPane.PreviewTab.Value))
				throw new Exception("Test case Failed, Preview tab in right pane is not displayed.");

			if (homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value))
				Log.pass("Test case Passed. Right pane with Metadata and Preview tab exists in hyperlink URL page.");
			else
				Log.fail("Test case Failed. Right pane with Metadata and Preview tab exists does not exists hyperlink URL page.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_1_34_3A

	/**
	 * 54.1.34.3B : Right pane with Metadata and Preview tab should be available in the selected view of default layout of hyperlink URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Right pane with Metadata and Preview tab should be available in the selected view of default layout of hyperlink URL - Operations menu")
	public void SprintTest54_1_34_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-3 : Select 'Show the selected View', 'Default' Custom layout if not selected and copy the hyperlink url
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


			Log.message("4. Navigated to the URL copied from hyperlink dialog.");			

			//Verification : Verify if Rightpane with metadata and preview exists
			//-------------------------------------------------------------------
			if (!homePage.isPreviewPaneDisplayed())
				throw new Exception("Test case Failed, Right pane is not displayed.");

			if (!homePage.previewPane.isTabExists(Caption.PreviewPane.PreviewTab.Value))
				throw new Exception("Test case Failed, Preview tab in right pane is not displayed.");

			if (homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value))
				Log.pass("Test case Passed. Right pane with Metadata and Preview tab exists in hyperlink URL page.");
			else
				Log.fail("Test case Failed. Right pane with Metadata and Preview tab exists does not exists hyperlink URL page.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_1_34_3B

	/**
	 * 54.1.34.4A : Right pane with Metadata and Preview tab should be available in the selected view of simple listing layout of hyperlink URL - context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Right pane with Metadata and Preview tab should be available in the selected view of simple listing layout of hyperlink URL - context menu")
	public void SprintTest54_1_34_4A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-3 : Select 'Show the selected View', 'simple listing' layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW VIEW"))
				throw new Exception("Show View is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Show the selected View and simple listing layout are selected and hyperlink URL is obtained.");

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("4. Navigated to the URL copied from hyperlink dialog.");			

			//Verification : Verify if Rightpane with metadata and preview exists
			//-------------------------------------------------------------------
			if (!homePage.isPreviewPaneDisplayed())
				throw new Exception("Test case Failed, Right pane is not displayed.");

			if (!homePage.previewPane.isTabExists(Caption.PreviewPane.PreviewTab.Value))
				throw new Exception("Test case Failed, Preview tab in right pane is not displayed.");

			if (homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value))
				Log.pass("Test case Passed. Right pane with Metadata and Preview tab exists in hyperlink URL page.");
			else
				Log.fail("Test case Failed. Right pane with Metadata and Preview tab exists does not exists hyperlink URL page.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_1_34_4A

	/**
	 * 54.1.34.4B : Right pane with Metadata and Preview tab should be available in the selected view of simple listing layout of hyperlink URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Right pane with Metadata and Preview tab should be available in the selected view of simple listing layout of hyperlink URL - Operations menu")
	public void SprintTest54_1_34_4B(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-3 : Select 'Show the selected View', 'simple listing' layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW VIEW"))
				throw new Exception("Show View is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink();

			Log.message("3. Show the selected View and simple listing layout are selected and hyperlink URL is obtained.");

			//Step-4 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("4. Navigated to the URL copied from hyperlink dialog.");			

			//Verification : Verify if Rightpane with metadata and preview exists
			//-------------------------------------------------------------------
			if (!homePage.isPreviewPaneDisplayed())
				throw new Exception("Test case Failed, Right pane is not displayed.");

			if (!homePage.previewPane.isTabExists(Caption.PreviewPane.PreviewTab.Value))
				throw new Exception("Test case Failed, Preview tab in right pane is not displayed.");

			if (homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value))
				Log.pass("Test case Passed. Right pane with Metadata and Preview tab exists in hyperlink URL page.");
			else
				Log.fail("Test case Failed. Right pane with Metadata and Preview tab exists does not exists hyperlink URL page.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_1_34_4B



}

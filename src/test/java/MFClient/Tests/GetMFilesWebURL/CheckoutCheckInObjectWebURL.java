package MFClient.Tests.GetMFilesWebURL;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.WebDriverUtils;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
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
import MFClient.Wrappers.ListView;
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class CheckoutCheckInObjectWebURL {

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
			//Utility.setEmbedAuthenticationToken();

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
	 * 54.4.3.1.1A : Copy the default layout hyperlink URL of checked in document and Check out the object (context menu) before navigating to the URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the default layout hyperlink URL of checked in document and Check out the object (context menu) before navigating to the URL - Context menu")
	public void SprintTest54_4_3_1_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("3. Show the selected File and Default custom layout are selected and hyperlink URL is obtained.");

			//Step-4 : Check out the document through context menu
			//----------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.CheckOut.Value); //Selects Check out from context menu

			Log.message("4. Object (" + dataPool.get("ObjectName") + ") is checked out through context menu");

			//Step-5 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("5. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked out state in the hyperlink URL.",driver);
			else
				Log.fail("Test case Failed. Object is not checked out in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_3_1_1A

	/**
	 * 54.4.3.1.1B : Copy the default layout hyperlink URL of checked in document and Check out the object (context menu) before navigating to the URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the default layout hyperlink URL of checked in document and Check out the object (context menu) before navigating to the URL - Operations menu")
	public void SprintTest54_4_3_1_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open GetMFilesWebURL dialog for the object from operations menu
			//-----------------------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from operations menu

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

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("3. Show the selected File and Default custom layout are selected and hyperlink URL is obtained.");

			//Step-4 : Check out the document through context menu
			//----------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.CheckOut.Value); //Selects Check out from context menu

			Log.message("4. Object (" + dataPool.get("ObjectName") + ") is checked out through context menu");

			//Step-5 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("5. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked out state in the hyperlink URL.",driver);
			else
				Log.fail("Test case Failed. Object is not checked out in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_3_1_1B

	/**
	 * 54.4.3.1.2A : Copy the default layout hyperlink URL of checked in document and Check out the object (operations menu) before navigating to the URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the default layout hyperlink URL of checked in document and Check out the object (operations menu) before navigating to the URL - Context menu")
	public void SprintTest54_4_3_1_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("3. Show the selected File and Default custom layout are selected and hyperlink URL is obtained.");

			//Step-4 : Check out the document through operations menu
			//--------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.CheckOut.Value); //Selects Check out from operations menu

			Log.message("4. Object (" + dataPool.get("ObjectName") + ") is checked out through operations menu");

			//Step-5 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("5. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked out state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked out in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_3_1_2A

	/**
	 * 54.4.3.1.2B : Copy the default layout hyperlink URL of checked in document and Check out the object (operations menu) before navigating to the URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the default layout hyperlink URL of checked in document and Check out the object (operations menu) before navigating to the URL - Operations menu")
	public void SprintTest54_4_3_1_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open GetMFilesWebURL dialog for the object from operations menu
			//-----------------------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from operations menu

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

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("3. Show the selected File and Default custom layout are selected and hyperlink URL is obtained.");

			//Step-4 : Check out the document through operations menu
			//--------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.CheckOut.Value); //Selects Check out from operations menu

			Log.message("4. Object (" + dataPool.get("ObjectName") + ") is checked out through operations menu");

			//Step-5 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("5. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked out state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked out in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_3_1_2B

	/**
	 * 54.4.3.1.3A : Copy the default layout hyperlink URL of checked in document and Check out the object (Task Panel) before navigating to the URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the default layout hyperlink URL of checked in document and Check out the object (Task Panel) before navigating to the URL - Context menu")
	public void SprintTest54_4_3_1_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("3. Show the selected File and Default custom layout are selected and hyperlink URL is obtained.");

			//Step-4 : Check out the document through task panel
			//--------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Check out from task panel

			Log.message("4. Object (" + dataPool.get("ObjectName") + ") is checked out through task panel.");

			//Step-5 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("5. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked out state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked out in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_3_1_3A

	/**
	 * 54.4.3.1.3B : Copy the default layout hyperlink URL of checked in document and Check out the object (Task Panel) before navigating to the URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the default layout hyperlink URL of checked in document and Check out the object (Task Panel) before navigating to the URL - Operations menu")
	public void SprintTest54_4_3_1_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open GetMFilesWebURL dialog for the object from operations menu
			//-----------------------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from operations menu

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

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("3. Show the selected File and Default custom layout are selected and hyperlink URL is obtained.");

			//Step-4 : Check out the document through task panel
			//--------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Check out from task panel

			Log.message("4. Object (" + dataPool.get("ObjectName") + ") is checked out through task panel");

			//Step-5 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("5. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked out state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked out in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_3_1_3B

	/**
	 * 54.4.3.2.1A : Copy the simple listing layout hyperlink URL of checked in document and Check out the object (context menu) before navigating to the URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the simple listing layout hyperlink URL of checked in document and Check out the object (context menu) before navigating to the URL - Context menu")
	public void SprintTest54_4_3_2_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT"))
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("3. Show the selected File and simple listing layout are selected and hyperlink URL is obtained.");

			//Step-4 : Check out the document through context menu
			//----------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.CheckOut.Value); //Selects Check out from context menu

			Log.message("4. Object (" + dataPool.get("ObjectName") + ") is checked out through context menu");

			//Step-5 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("5. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked out state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked out in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_3_2_1A

	/**
	 * 54.4.3.2.1B : Copy the simple listing layout hyperlink URL of checked in document and Check out the object (context menu) before navigating to the URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the simple listing layout hyperlink URL of checked in document and Check out the object (context menu) before navigating to the URL - Operations menu")
	public void SprintTest54_4_3_2_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open GetMFilesWebURL dialog for the object from operations menu
			//-----------------------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("2. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-3 : Select 'Show the selected file', 'simple listing' layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT"))
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("3. Show the selected File and simple listing layout are selected and hyperlink URL is obtained.");

			//Step-4 : Check out the document through context menu
			//----------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.CheckOut.Value); //Selects Check out from context menu

			Log.message("4. Object (" + dataPool.get("ObjectName") + ") is checked out through context menu");

			//Step-5 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("5. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked out state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked out in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_3_2_1B

	/**
	 * 54.4.3.2.2A : Copy the simple listing layout hyperlink URL of checked in document and Check out the object (operations menu) before navigating to the URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the simple listing layout hyperlink URL of checked in document and Check out the object (operations menu) before navigating to the URL - Context menu")
	public void SprintTest54_4_3_2_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT"))
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("3. Show the selected File and simple listing layout are selected and hyperlink URL is obtained.");

			//Step-4 : Check out the document through operations menu
			//--------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.CheckOut.Value); //Selects Check out from operations menu

			Log.message("4. Object (" + dataPool.get("ObjectName") + ") is checked out through operations menu");

			//Step-5 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("5. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked out state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked out in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_3_2_2A

	/**
	 * 54.4.3.2.2B : Copy the simple listing layout hyperlink URL of checked in document and Check out the object (operations menu) before navigating to the URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the simple listing layout hyperlink URL of checked in document and Check out the object (operations menu) before navigating to the URL - Operations menu")
	public void SprintTest54_4_3_2_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open GetMFilesWebURL dialog for the object from operations menu
			//-----------------------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("2. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-3 : Select 'Show the selected file', 'simple listing' layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT"))
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("3. Show the selected File and simple listing layout are selected and hyperlink URL is obtained.");

			//Step-4 : Check out the document through operations menu
			//--------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.CheckOut.Value); //Selects Check out from operations menu

			Log.message("4. Object (" + dataPool.get("ObjectName") + ") is checked out through operations menu");

			//Step-5 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("5. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked out state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked out in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_3_2_2B

	/**
	 * 54.4.3.2.3A : Copy the simple listing layout hyperlink URL of checked in document and Check out the object (Task Panel) before navigating to the URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the simple listing layout hyperlink URL of checked in document and Check out the object (Task Panel) before navigating to the URL - Context menu")
	public void SprintTest54_4_3_2_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT"))
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("3. Show the selected File and simple listing layout are selected and hyperlink URL is obtained.");

			//Step-4 : Check out the document through task panel
			//--------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Check out from task panel

			Log.message("4. Object (" + dataPool.get("ObjectName") + ") is checked out through task panel");

			//Step-5 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("5. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked out state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked out in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_3_1_3A

	/**
	 * 54.4.3.2.3B : Copy the simple listing layout hyperlink URL of checked in document and Check out the object (Task Panel) before navigating to the URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the default layout hyperlink URL of checked in document and Check out the object (Task Panel) before navigating to the URL - Operations menu")
	public void SprintTest54_4_3_2_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open GetMFilesWebURL dialog for the object from operations menu
			//-----------------------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("2. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-3 : Select 'Show the selected file', 'simple listing' layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT"))
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("3. Show the selected File and simple listing layout are selected and hyperlink URL is obtained.");

			//Step-4 : Check out the document through task panel
			//--------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Check out from task panel

			Log.message("4. Object (" + dataPool.get("ObjectName") + ") is checked out through task panel");

			//Step-5 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("5. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked out state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked out in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_3_1_3B

	/**
	 * 54.4.4.1.1A : Copy the default layout hyperlink URL of checked out document and Check in the object (context menu) before navigating to the URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the default layout hyperlink URL of checked out document and Check in the object (context menu) before navigating to the URL - Context menu")
	public void SprintTest54_4_4_1_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from context menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-4 : Select 'Show the selected file', 'Default' Custom layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File and Default custom layout are selected and hyperlink URL is obtained.");

			//Step-5 : Check in the document through context menu
			//----------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.CheckIn.Value); //Selects Check out from context menu

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked in.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is checked in through context menu");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked in state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked in in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_4_1_1A

	/**
	 * 54.4.4.1.1B : Copy the default layout hyperlink URL of checked out document and Check in the object (context menu) before navigating to the URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the default layout hyperlink URL of checked out document and Check in the object (context menu) before navigating to the URL - Operations menu")
	public void SprintTest54_4_4_1_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from operations menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-4 : Select 'Show the selected file', 'Default' Custom layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT"))
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File and Default custom layout are selected and hyperlink URL is obtained.");

			//Step-5 : Check in the document through context menu
			//----------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.CheckIn.Value); //Selects Check out from context menu

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked in.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is checked in through context menu");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked in state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked in in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_4_1_1B

	/**
	 * 54.4.4.1.2A : Copy the default layout hyperlink URL of checked out document and Check in the object (operations menu) before navigating to the URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the default layout hyperlink URL of checked out document and Check in the object (operations menu) before navigating to the URL - Context menu")
	public void SprintTest54_4_4_1_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from context menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-4 : Select 'Show the selected file', 'Default' Custom layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File and Default custom layout are selected and hyperlink URL is obtained.");

			//Step-5 : Check in the document through operations menu
			//----------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.CheckIn.Value); //Selects Check out from operations menu

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked in.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is checked in through operations menu");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked in state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked in in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_4_1_2A

	/**
	 * 54.4.4.1.2B : Copy the default layout hyperlink URL of checked out document and Check in the object (operations menu) before navigating to the URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the default layout hyperlink URL of checked out document and Check in the object (operations menu) before navigating to the URL - Operations menu")
	public void SprintTest54_4_4_1_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from operations menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-4 : Select 'Show the selected file', 'Default' Custom layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File and Default custom layout are selected and hyperlink URL is obtained.");

			//Step-5 : Check in the document through operations menu
			//----------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.CheckIn.Value); //Selects Check out from operations menu

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is checked not in.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is checked in through operations menu");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked in state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked in in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_4_1_2B

	/**
	 * 54.4.4.1.3A : Copy the default layout hyperlink URL of checked out document and Check in the object (Task panel) before navigating to the URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the default layout hyperlink URL of checked out document and Check in the object (Task panel) before navigating to the URL - Context menu")
	public void SprintTest54_4_4_1_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from context menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-4 : Select 'Show the selected file', 'Default' Custom layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File and Default custom layout are selected and hyperlink URL is obtained.");

			//Step-5 : Check in the document through taskpanel
			//----------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value); //Selects Check out from taskpanel

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked in.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is checked in through taskpanel.");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked in state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked in in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_4_1_3A

	/**
	 * 54.4.4.1.3B : Copy the default layout hyperlink URL of checked out document and Check in the object (Task panel) before navigating to the URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the default layout hyperlink URL of checked out document and Check in the object (Task panel) before navigating to the URL - Operations menu")
	public void SprintTest54_4_4_1_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from operations menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-4 : Select 'Show the selected file', 'Default' Custom layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File and Default custom layout are selected and hyperlink URL is obtained.");

			//Step-5 : Check in the document through taskpanel
			//----------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value); //Selects Check out from taskpanel

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked in.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is checked in through taskpanel.");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked in state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked in in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_4_1_3B

	/**
	 * 54.4.4.1.4A : Copy the default layout hyperlink URL of checked out document and Check in the object (CTRL+I key) before navigating to the URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_KeyActions", "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the default layout hyperlink URL of checked out document and Check in the object (CTRL+I key) before navigating to the URL - Context menu")
	public void SprintTest54_4_4_1_4A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports key actions.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from context menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-4 : Select 'Show the selected file', 'Default' Custom layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File and Default custom layout are selected and hyperlink URL is obtained.");

			//Step-5 : Check in the document by pressing CTRL+I key
			//-------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			/*Robot robot=new Robot();
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_I);
            robot.keyRelease(KeyEvent.VK_I);
            robot.keyRelease(KeyEvent.VK_CONTROL);*/

			Actions action = new Actions(driver);
			action.keyDown(Keys.CONTROL).sendKeys("i").perform();


			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked in.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is checked in by pressing CTRL+I key.");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked in state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked in in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_4_1_4A

	/**
	 * 54.4.4.1.4B : Copy the default layout hyperlink URL of checked out document and Check in the object (CTRL+I key) before navigating to the URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_KeyActions", "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the default layout hyperlink URL of checked out document and Check in the object (CTRL+I key) before navigating to the URL - Operations menu")
	public void SprintTest54_4_4_1_4B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports key actions.");

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from operations menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-4 : Select 'Show the selected file', 'Default' Custom layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File and Default custom layout are selected and hyperlink URL is obtained.");

			//Step-5 : Check in the document by pressing CTRL+I key
			//-------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");
			/* 
			Robot robot=new Robot();
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_I);
            robot.keyRelease(KeyEvent.VK_I);
            robot.keyRelease(KeyEvent.VK_CONTROL);*/
			Actions action = new Actions(driver);
			action.keyDown(Keys.CONTROL).sendKeys("i").perform();


			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked in.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is checked in by pressing CTRL+I key.");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked in state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked in in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_4_1_4B

	/**
	 * 54.4.4.2.1A : Copy the simple listing layout hyperlink URL of checked out document and Check in the object (context menu) before navigating to the URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the simple listing layout hyperlink URL of checked out document and Check in the object (context menu) before navigating to the URL - Context menu")
	public void SprintTest54_4_4_2_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from context menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-4 : Select 'Show the selected file', SimpleListing layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File and simple listing layout are selected and hyperlink URL is obtained.");

			//Step-5 : Check in the document through context menu
			//----------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.CheckIn.Value); //Selects Check out from context menu

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked in.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is checked in through context menu");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked in state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked in in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_4_2_1A

	/**
	 * 54.4.4.2.1B : Copy the simple listing layout hyperlink URL of checked out document and Check in the object (context menu) before navigating to the URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the simple listing layout hyperlink URL of checked out document and Check in the object (context menu) before navigating to the URL - Operations menu")
	public void SprintTest54_4_4_2_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from operations menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-4 : Select 'Show the selected file', 'Default' Custom layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT"))
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File and Default custom layout are selected and hyperlink URL is obtained.");

			//Step-5 : Check in the document through context menu
			//----------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.CheckIn.Value); //Selects Check out from context menu

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked in.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is checked in through context menu");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked in state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked in in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_4_2_1B

	/**
	 * 54.4.4.2.2A : Copy the simple listing layout hyperlink URL of checked out document and Check in the object (operations menu) before navigating to the URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the simple listing layout hyperlink URL of checked out document and Check in the object (operations menu) before navigating to the URL - Context menu")
	public void SprintTest54_4_4_2_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from context menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-4 : Select 'Show the selected file', SimpleListing layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File and simple listing layout are selected and hyperlink URL is obtained.");

			//Step-5 : Check in the document through operations menu
			//----------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.CheckIn.Value); //Selects Check out from operations menu

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked in.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is checked in through operations menu");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked in state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked in in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_4_2_2A

	/**
	 * 54.4.4.2.2B : Copy the simple listing layout hyperlink URL of checked out document and Check in the object (operations menu) before navigating to the URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the simple listing layout hyperlink URL of checked out document and Check in the object (operations menu) before navigating to the URL - Operations menu")
	public void SprintTest54_4_4_2_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from operations menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-4 : Select 'Show the selected file', SimpleListing layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File and simple listing layout are selected and hyperlink URL is obtained.");

			//Step-5 : Check in the document through operations menu
			//----------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.CheckIn.Value); //Selects Check out from operations menu

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is checked not in.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is checked in through operations menu");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked in state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked in in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_4_2_2B

	/**
	 * 54.4.4.2.3A : Copy the simple listing layout hyperlink URL of checked out document and Check in the object (Task panel) before navigating to the URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the simple listing layout hyperlink URL of checked out document and Check in the object (Task panel) before navigating to the URL - Context menu")
	public void SprintTest54_4_4_2_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from context menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-4 : Select 'Show the selected file', SimpleListing layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File and simple listing layout are selected and hyperlink URL is obtained.");

			//Step-5 : Check in the document through taskpanel
			//----------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value); //Selects Check out from taskpanel

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked in.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is checked in through taskpanel.");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked in state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked in in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_4_2_3A

	/**
	 * 54.4.4.2.3B : Copy the simple listing layout hyperlink URL of checked out document and Check in the object (Task panel) before navigating to the URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the simple listing layout hyperlink URL of checked out document and Check in the object (Task panel) before navigating to the URL - Operations menu")
	public void SprintTest54_4_4_2_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from operations menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-4 : Select 'Show the selected file', SimpleListing layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File and simple listing layout are selected and hyperlink URL is obtained.");

			//Step-5 : Check in the document through taskpanel
			//----------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value); //Selects Check out from taskpanel

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked in.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is checked in through taskpanel.");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked in state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked in in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_4_2_3B

	/**
	 * 54.4.4.2.4A : Copy the simple listing layout hyperlink URL of checked out document and Check in the object (CTRL+I key) before navigating to the URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_KeyActions", "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the simple listing layout hyperlink URL of checked out document and Check in the object (CTRL+I key) before navigating to the URL - Context menu")
	public void SprintTest54_4_4_2_4A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports key actions.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from context menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-4 : Select 'Show the selected file', SimpleListing layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File and simple listing layout are selected and hyperlink URL is obtained.");

			//Step-5 : Check in the document by pressing CTRL+I key
			//-------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			Actions action = new Actions(driver);
			action.keyDown(Keys.CONTROL).sendKeys("i").perform();


			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked in.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is checked in by pressing CTRL+I key.");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked in state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked in in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_4_1_4A

	/**
	 * 54.4.4.2.4B : Copy the simple listing layout hyperlink URL of checked out document and Check in the object (CTRL+I key) before navigating to the URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_KeyActions", "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the simple listing layout hyperlink URL of checked out document and Check in the object (CTRL+I key) before navigating to the URL - Operations menu")
	public void SprintTest54_4_4_2_4B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports key actions.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from operations menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-4 : Select 'Show the selected file', SimpleListing layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File and simple listing layout are selected and hyperlink URL is obtained.");

			//Step-5 : Check in the document by pressing CTRL+I key
			//-------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			/*Robot robot=new Robot();
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_I);
            robot.keyRelease(KeyEvent.VK_I);
            robot.keyRelease(KeyEvent.VK_CONTROL);*/

			Actions action = new Actions(driver);
			action.keyDown(Keys.CONTROL).sendKeys("i").perform();


			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked in.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is checked in by pressing CTRL+I key.");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked in state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked in in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_4_2_4B

	/**
	 * 54.4.5.1.1A : Copy the default layout hyperlink URL of checked out document and Undo check out the object (context menu) before navigating to the URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the default layout hyperlink URL of checked out document and Undo check out the object (context menu) before navigating to the URL - Context menu")
	public void SprintTest54_4_5_1_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from context menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-4 : Select 'Show the selected file', 'Default' Custom layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File and Default custom layout are selected and hyperlink URL is obtained.");

			//homePage.undoCheckOutObject("contextMenu");

			//Step-5 : Undo-Check out the document through context menu
			//---------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.UndoCheckOut.Value); //Selects undo check out from context menu

			mfilesDialog = new MFilesDialog(driver); //Instantiates MFilesDialog wrapper


			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo checkout dialog


			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked in.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is undo checked out through context menu");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked in state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked in in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_5_1_1A

	/**
	 * 54.4.5.1.1B : Copy the default layout hyperlink URL of checked out document and Undo check out the object (context menu) before navigating to the URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the default layout hyperlink URL of checked out document and Undo check out the object (context menu) before navigating to the URL - Operations menu")
	public void SprintTest54_4_5_1_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from operations menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-4 : Select 'Show the selected file', 'Default' Custom layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File and Default custom layout are selected and hyperlink URL is obtained.");

			//Step-5 : Undo-Check out the document through context menu
			//---------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.UndoCheckOut.Value); //Selects undo check out from context menu

			mfilesDialog = new MFilesDialog(driver); //Instantiates MFilesDialog wrapper

			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo checkout dialog


			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked in.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is undo checked out through context menu");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked in state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked in in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_5_1_1B

	/**
	 * 54.4.5.1.2A : Copy the default layout hyperlink URL of checked out document and Undo check out the object (operations menu) before navigating to the URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the default layout hyperlink URL of checked out document and Undo check out the object (operations menu) before navigating to the URL - Context menu")
	public void SprintTest54_4_5_1_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from context menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-4 : Select 'Show the selected file', 'Default' Custom layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File and Default custom layout are selected and hyperlink URL is obtained.");

			//Step-5 : Undo-Check out the document through operations menu
			//-------------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects undo check out from operations menu

			mfilesDialog = new MFilesDialog(driver); //Instantiates MFilesDialog wrapper

			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo checkout dialog


			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked in.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is undo checked out through operations menu");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked in state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked in in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_5_1_2A

	/**
	 * 54.4.5.1.2B : Copy the default layout hyperlink URL of checked out document and Undo check out the object (operations menu) before navigating to the URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the default layout hyperlink URL of checked out document and Undo check out the object (operations menu) before navigating to the URL - Operations menu")
	public void SprintTest54_4_5_1_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from operations menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-4 : Select 'Show the selected file', 'Default' Custom layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File and Default custom layout are selected and hyperlink URL is obtained.");

			//Step-5 : Undo-Check out the document through operations menu
			//-------------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects undo check out from operations menu

			mfilesDialog = new MFilesDialog(driver); //Instantiates MFilesDialog wrapper

			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo checkout dialog


			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked in.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is undo checked out through operations menu");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked in state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked in in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_5_1_2B

	/**
	 * 54.4.5.1.3A : Copy the default layout hyperlink URL of checked out document and Undo check out the object (task panel) before navigating to the URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the default layout hyperlink URL of checked out document and Undo check out the object (task panel) before navigating to the URL - Context menu")
	public void SprintTest54_4_5_1_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from context menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-4 : Select 'Show the selected file', 'Default' Custom layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File and Default custom layout are selected and hyperlink URL is obtained.");

			//Step-5 : Undo-Check out the document through task panel
			//-------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects undo check out from task panel

			mfilesDialog = new MFilesDialog(driver); //Instantiates MFilesDialog wrapper

			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo checkout dialog


			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked in.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is undo checked out through task panel");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked in state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked in in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_5_1_3A

	/**
	 * 54.4.5.1.3B : Copy the default layout hyperlink URL of checked out document and Undo check out the object (task panel) before navigating to the URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the default layout hyperlink URL of checked out document and Undo check out the object (task panel) before navigating to the URL - Operations menu")
	public void SprintTest54_4_5_1_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from operations menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-4 : Select 'Show the selected file', 'Default' Custom layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File and Default custom layout are selected and hyperlink URL is obtained.");

			//Step-5 : Undo-Check out the document through task panel
			//-------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects undo check out from task panel

			mfilesDialog = new MFilesDialog(driver); //Instantiates MFilesDialog wrapper

			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo checkout dialog


			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked in.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is undo checked out through task panel");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked in state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked in in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_5_1_3B

	/**
	 * 54.4.5.2.1A : Copy the simple listing layout hyperlink URL of checked out document and Undo check out the object (context menu) before navigating to the URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the simple listing layout hyperlink URL of checked out document and Undo check out the object (context menu) before navigating to the URL - Context menu")
	public void SprintTest54_4_5_2_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from context menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-4 : Select 'Show the selected file', SimpleListing layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File and simple listing layout are selected and hyperlink URL is obtained.");

			//Step-5 : Undo-Check out the document through context menu
			//---------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.UndoCheckOut.Value); //Selects undo check out from context menu

			mfilesDialog = new MFilesDialog(driver); //Instantiates MFilesDialog wrapper

			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo checkout dialog


			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked in.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is undo checked out through context menu");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked in state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked in in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_5_2_1A

	/**
	 * 54.4.5.2.1B : Copy the simple listing layout hyperlink URL of checked out document and Undo check out the object (context menu) before navigating to the URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the simple listing layout hyperlink URL of checked out document and Undo check out the object (context menu) before navigating to the URL - Operations menu")
	public void SprintTest54_4_5_2_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from operations menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-4 : Select 'Show the selected file', SimpleListing layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File and simple listing layout are selected and hyperlink URL is obtained.");

			//Step-5 : Undo-Check out the document through context menu
			//---------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.UndoCheckOut.Value); //Selects undo check out from context menu

			mfilesDialog = new MFilesDialog(driver); //Instantiates MFilesDialog wrapper

			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo checkout dialog


			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked in.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is undo checked out through context menu");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked in state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked in in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_5_2_1B

	/**
	 * 54.4.5.2.2A : Copy the simple listing layout hyperlink URL of checked out document and Undo check out the object (operations menu) before navigating to the URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the simple listing layout hyperlink URL of checked out document and Undo check out the object (operations menu) before navigating to the URL - Context menu")
	public void SprintTest54_4_5_2_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from context menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-4 : Select 'Show the selected file', SimpleListing layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File and simple listing layout are selected and hyperlink URL is obtained.");

			//Step-5 : Undo-Check out the document through operations menu
			//-------------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects undo check out from operations menu

			mfilesDialog = new MFilesDialog(driver); //Instantiates MFilesDialog wrapper

			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo checkout dialog


			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked in.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is undo checked out through operations menu");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked in state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked in in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_5_2_2A

	/**
	 * 54.4.5.2.2B : Copy the simple listing layout hyperlink URL of checked out document and Undo check out the object (operations menu) before navigating to the URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the simple listing layout hyperlink URL of checked out document and Undo check out the object (operations menu) before navigating to the URL - Operations menu")
	public void SprintTest54_4_5_2_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from operations menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-4 : Select 'Show the selected file', SimpleListing layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File and simple listing layout are selected and hyperlink URL is obtained.");

			//Step-5 : Undo-Check out the document through operations menu
			//-------------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects undo check out from operations menu

			mfilesDialog = new MFilesDialog(driver); //Instantiates MFilesDialog wrapper

			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo checkout dialog


			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked in.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is undo checked out through operations menu");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked in state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked in in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_5_2_2B

	/**
	 * 54.4.5.2.3A : Copy the simple listing layout hyperlink URL of checked out document and Undo check out the object (task panel) before navigating to the URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the simple listing layout hyperlink URL of checked out document and Undo check out the object (task panel) before navigating to the URL - Context menu")
	public void SprintTest54_4_5_2_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from context menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-4 : Select 'Show the selected file', SimpleListing layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File and simple listing layout are selected and hyperlink URL is obtained.");

			//Step-5 : Undo-Check out the document through task panel
			//-------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects undo check out from task panel

			mfilesDialog = new MFilesDialog(driver); //Instantiates MFilesDialog wrapper

			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo checkout dialog


			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked in.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is undo checked out through task panel");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked in state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked in in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_5_2_3A

	/**
	 * 54.4.5.2.3B : Copy the simple listing layout hyperlink URL of checked out document and Undo check out the object (task panel) before navigating to the URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Copy the simple listing layout hyperlink URL of checked out document and Undo check out the object (task panel) before navigating to the URL - Operations menu")
	public void SprintTest54_4_5_2_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {




			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from operations menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-4 : Select 'Show the selected file', SimpleListing layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File and simple listing layout are selected and hyperlink URL is obtained.");

			//Step-5 : Undo-Check out the document through task panel
			//-------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects undo check out from task panel

			mfilesDialog = new MFilesDialog(driver); //Instantiates MFilesDialog wrapper

			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo checkout dialog


			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked in.");

			Log.message("5. Object (" + dataPool.get("ObjectName") + ") is undo checked out through task panel");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Navigated to the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URl
			//-----------------------------------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is in checked in state in the hyperlink URL.");
			else
				Log.fail("Test case Failed. Object is not checked in in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_5_2_3B

	/**
	 * Commented cases are obselte due to the -	Functionality "EmbedAuthenticationToken" is deprecated due to User story #M-10068: [PO] I want to deprecate supporting "Embed my username and password to the link" from Get M-Files Web URL in M-Files 2017
	 * 
	 * 
	 * 54.4.10.1A : Checked out object with 'Default Layout', 'Show the selected Object', without selecting Embed authentication details in GetMFilesWebURL for current user should show the object - Context menu.
	 *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Checked out object with 'Default Layout', 'Show the selected Object', without selecting Embed authentication details in GetMFilesWebURL for current user should show the object - Context menu.")
	public void SprintTest54_4_10_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from context menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-4 : Select 'Show the selected file', 'Default' Custom layout if not selected, un selected embed authetication and copy the hyperlink url
			//---------------------------------------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			if (!mfilesDialog.setHyperLinkEmbedAuthentication(false)) //UnSelects the Embed authentication details
				throw new Exception("Embed authentication details is unselected from GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File, Default layout are selected, Embed authentication is unselected and hyperlink URL is obtained.");

			//Step-5 : Log out from current session
			//-------------------------------------
			if (!Utility.logOut(driver)) //Logs out from the vault
				throw new Exception ("Log out from vault (" + testVault + ") is unsuccessful.");

			Log.message("5. Log out from vault (" + testVault + ") is successful.");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL, userName, password, ""); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Pasted the URL copied from hyperlink dialog and logged in with valid credentials.");

			//Verification : Verify object got checked out in the hyperlink URL
			//-----------------------------------------------------------------
			if (!driver.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkURL.toUpperCase())) //Checks if hyperlink URL is same as in the dialog
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");			

			if (homePage.listView.itemCount() != 1) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. One item is not getting displayed on opening the hyperlink.");

			String unAvailableLayouts = Utility.isDefaultLayout(homePage);

			if (!unAvailableLayouts.equals("")) //Verifies if default layout is getting displayed
				throw new Exception("Test case Failed. Default layout is not getting displayed.");

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if object is in checked out state
				Log.pass("Test case Passed. Checked out object is displayed for the current user.");
			else
				Log.fail("Test case Failed. Object is not checked out in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_10_1A

	  *//**
	  * 54.4.10.1B : Checked out object with 'Default Layout', 'Show the selected Object', without selecting Embed authentication details in GetMFilesWebURL for current user should show the object - Operations menu.
	  *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Checked out object with 'Default Layout', 'Show the selected Object', without selecting Embed authentication details in GetMFilesWebURL for current user should show the object - Operations menu.")
	public void SprintTest54_4_10_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from operations menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-4 : Select 'Show the selected file', 'Default' Custom layout if not selected, un selected embed authetication and copy the hyperlink url
			//---------------------------------------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			if (!mfilesDialog.setHyperLinkEmbedAuthentication(false)) //UnSelects the Embed authentication details
				throw new Exception("Embed authentication details is unselected from GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File, Default layout are selected, Embed authentication is unselected and hyperlink URL is obtained.");

			//Step-5 : Log out from current session
			//-------------------------------------
			if (!Utility.logOut(driver)) //Logs out from the vault
				throw new Exception ("Log out from vault (" + testVault + ") is unsuccessful.");

			Log.message("5. Log out from vault (" + testVault + ") is successful.");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL, userName, password, ""); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Pasted the URL copied from hyperlink dialog and logged in with valid credentials.");

			//Verification : Verify object got checked out in the hyperlink URL
			//-----------------------------------------------------------------
			if (!driver.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkURL.toUpperCase())) //Checks if hyperlink URL is same as in the dialog
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");			

			if (homePage.listView.itemCount() != 1) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. One item is not getting displayed on opening the hyperlink.");

			String unAvailableLayouts = Utility.isDefaultLayout(homePage);

			if (!unAvailableLayouts.equals("")) //Verifies if default layout is getting displayed
				throw new Exception("Test case Failed. Default layout is not getting displayed.");

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if object is in checked out state
				Log.pass("Test case Passed. Checked out object is displayed for the current user.");
			else
				Log.fail("Test case Failed. Object is not checked out in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_10_1B

	   *//**
	   * 54.4.10.2A : Checked out object with 'Simple listing Layout', 'Show the selected Object', without selecting Embed authentication details in GetMFilesWebURL for current user should show the object - Context menu.
	   *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Checked out object with 'Simple listing Layout', 'Show the selected Object', without selecting Embed authentication details in GetMFilesWebURL for current user should show the object - Context menu.")
	public void SprintTest54_4_10_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from context menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-4 : Select 'Show the selected file', 'Simple listing' layout if not selected, un selected embed authetication and copy the hyperlink url
			//---------------------------------------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			if (!mfilesDialog.setHyperLinkEmbedAuthentication(false)) //UnSelects the Embed authentication details
				throw new Exception("Embed authentication details is unselected from GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File, Simple listing layout are selected, Embed authentication is unselected and hyperlink URL is obtained.");

			//Step-5 : Log out from current session
			//-------------------------------------
			if (!Utility.logOut(driver)) //Logs out from the vault
				throw new Exception ("Log out from vault (" + testVault + ") is unsuccessful.");

			Log.message("5. Log out from vault (" + testVault + ") is successful.");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL, userName, password, ""); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Pasted the URL copied from hyperlink dialog and logged in with valid credentials.");

			//Verification : Verify object got checked out in the hyperlink URL
			//-----------------------------------------------------------------
			if (!driver.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkURL.toUpperCase())) //Checks if hyperlink URL is same as in the dialog
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");			


			if (homePage.listView.itemCount() != 1) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. One item is not getting displayed on opening the hyperlink.");

			String unAvailableLayouts = Utility.isSimpleListingLayout(homePage);

			if (!unAvailableLayouts.equals("")) //Verifies if simple listing layout is getting displayed
				throw new Exception("Test case Failed. Default layout is not getting displayed.");

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if object is in checked out state
				Log.pass("Test case Passed. Checked out object is displayed for the current user.");
			else
				Log.fail("Test case Failed. Object is not checked out in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_10_2A

	    *//**
	    * 54.4.10.2B : Checked out object with 'Simple listing Layout', 'Show the selected Object', without selecting Embed authentication details in GetMFilesWebURL for current user should show the object - Operations menu.
	    *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Checked out object with 'Simple listing Layout', 'Show the selected Object', without selecting Embed authentication details in GetMFilesWebURL for current user should show the object - Operations menu.")
	public void SprintTest54_4_10_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from operations menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-4 : Select 'Show the selected file', 'Simple listing' layout if not selected, un selected embed authetication and copy the hyperlink url
			//---------------------------------------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			if (!mfilesDialog.setHyperLinkEmbedAuthentication(false)) //UnSelects the Embed authentication details
				throw new Exception("Embed authentication details is unselected from GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File, Simple listing layout are selected, Embed authentication is unselected and hyperlink URL is obtained.");

			//Step-5 : Log out from current session
			//-------------------------------------
			if (!Utility.logOut(driver)) //Logs out from the vault
				throw new Exception ("Log out from vault (" + testVault + ") is unsuccessful.");

			Log.message("5. Log out from vault (" + testVault + ") is successful.");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL, userName, password, ""); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Pasted the URL copied from hyperlink dialog and logged in with valid credentials.");

			//Verification : Verify object got checked out in the hyperlink URL
			//-----------------------------------------------------------------
			if (!driver.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkURL.toUpperCase())) //Checks if hyperlink URL is same as in the dialog
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");			

			if (homePage.listView.itemCount() != 1) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. One item is not getting displayed on opening the hyperlink.");

			String unAvailableLayouts = Utility.isSimpleListingLayout(homePage);

			if (!unAvailableLayouts.equals("")) //Verifies if simple listing layout is getting displayed
				throw new Exception("Test case Failed. Default layout is not getting displayed.");

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if object is in checked out state
				Log.pass("Test case Passed. Checked out object is displayed for the current user.");
			else
				Log.fail("Test case Failed. Object is not checked out in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_10_2B

	     *//**
	     * 54.4.10.3A : Checked out object with 'Default Layout', 'Show the selected View', without selecting Embed authentication details in GetMFilesWebURL for current user should show the object - Context menu.
	     *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL","Bug"}, 
			description = "Checked out object with 'Default Layout', 'Show the selected View', without selecting Embed authentication details in GetMFilesWebURL for current user should show the object - Context menu.")
	public void SprintTest54_4_10_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			int prevCt = homePage.listView.itemCount(); //Gets the number of objects in the view

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from context menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-4 : Select 'Show the selected view', 'Default' Custom layout if not selected, un selected embed authetication and copy the hyperlink url
			//---------------------------------------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW VIEW")) //Selects Show view
				throw new Exception("Show View is not selected in GetMFilesWebURL dialog.");

			if (!mfilesDialog.setHyperLinkEmbedAuthentication(false)) //UnSelects the Embed authentication details
				throw new Exception("Embed authentication details is unselected from GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected View, Default layout are selected, Embed authentication is unselected and hyperlink URL is obtained.");

			//Step-5 : Log out from current session
			//-------------------------------------
			if (!Utility.logOut(driver)) //Logs out from the vault
				throw new Exception ("Log out from vault (" + testVault + ") is unsuccessful.");

			Log.message("5. Log out from vault (" + testVault + ") is successful.");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL, userName, password, ""); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Pasted the URL copied from hyperlink dialog and logged in with valid credentials.");

			//Verification : Verify object got checked out in the hyperlink URL
			//-----------------------------------------------------------------
			if (!driver.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkURL.toUpperCase())) //Checks if hyperlink URL is same as in the dialog
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");			

			if (homePage.listView.itemCount() != prevCt) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. Number of objects in the view and in the hyperlink URL are not same.");

			String unAvailableLayouts = Utility.isDefaultLayout(homePage);

			if (!unAvailableLayouts.equals("")) //Verifies if default layout is getting displayed
				throw new Exception("Test case Failed. Default layout is not getting displayed.");

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if object is in checked out state
				Log.pass("Test case Passed. Checked out object is displayed for the current user.");
			else
				Log.fail("Test case Failed. Object is not checked out in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_10_3A

	      *//**
	      * 54.4.10.3B : Checked out object with 'Default Layout', 'Show the selected View', without selecting Embed authentication details in GetMFilesWebURL for current user should show the object - Operations menu.
	      *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL","Bug"}, 
			description = "Checked out object with 'Default Layout', 'Show the selected View', without selecting Embed authentication details in GetMFilesWebURL for current user should show the object - Operations menu.")
	public void SprintTest54_4_10_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			int prevCt = homePage.listView.itemCount(); //Gets the number of objects in the view

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from operations menu
			//-----------------------------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-4 : Select 'Show the selected view', 'Default' Custom layout if not selected, un selected embed authetication and copy the hyperlink url
			//---------------------------------------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW VIEW")) //Selects Show view
				throw new Exception("Show View is not selected in GetMFilesWebURL dialog.");

			if (!mfilesDialog.setHyperLinkEmbedAuthentication(false)) //UnSelects the Embed authentication details
				throw new Exception("Embed authentication details is unselected from GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected View, Default layout are selected, Embed authentication is unselected and hyperlink URL is obtained.");

			//Step-5 : Log out from current session
			//-------------------------------------
			if (!Utility.logOut(driver)) //Logs out from the vault
				throw new Exception ("Log out from vault (" + testVault + ") is unsuccessful.");

			Log.message("5. Log out from vault (" + testVault + ") is successful.");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL, userName, password, ""); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Pasted the URL copied from hyperlink dialog and logged in with valid credentials.");

			//Verification : Verify object got checked out in the hyperlink URL
			//-----------------------------------------------------------------
			if (!driver.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkURL.toUpperCase())) //Checks if hyperlink URL is same as in the dialog
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");			

			if (homePage.listView.itemCount() != prevCt) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. Number of objects in the view and in the hyperlink URL are not same.");

			String unAvailableLayouts = Utility.isDefaultLayout(homePage);

			if (!unAvailableLayouts.equals("")) //Verifies if default layout is getting displayed
				throw new Exception("Test case Failed. Default layout is not getting displayed.");

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if object is in checked out state
				Log.pass("Test case Passed. Checked out object is displayed for the current user.");
			else
				Log.fail("Test case Failed. Object is not checked out in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_10_3B

	       *//**
	       * 54.4.10.4A : Checked out object with 'Simple listing Layout', 'Show the selected View', without selecting Embed authentication details in GetMFilesWebURL for current user should show the object - Context menu.
	       *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Checked out object with 'Simple listing Layout', 'Show the selected View', without selecting Embed authentication details in GetMFilesWebURL for current user should show the object - Context menu.")
	public void SprintTest54_4_10_4A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			int prevCt = homePage.listView.itemCount(); //Gets the number of objects in the view

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from context menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-4 : Select 'Show the selected view', 'Simple listing' layout if not selected, un selected embed authetication and copy the hyperlink url
			//---------------------------------------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW VIEW")) //Selects Show view
				throw new Exception("Show View is not selected in GetMFilesWebURL dialog.");

			if (!mfilesDialog.setHyperLinkEmbedAuthentication(false)) //UnSelects the Embed authentication details
				throw new Exception("Embed authentication details is unselected from GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected View, Simple listing layout are selected, Embed authentication is unselected and hyperlink URL is obtained.");

			//Step-5 : Log out from current session
			//-------------------------------------
			if (!Utility.logOut(driver)) //Logs out from the vault
				throw new Exception ("Log out from vault (" + testVault + ") is unsuccessful.");

			Log.message("5. Log out from vault (" + testVault + ") is successful.");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL, userName, password, ""); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Pasted the URL copied from hyperlink dialog and logged in with valid credentials.");

			//Verification : Verify object got checked out in the hyperlink URL
			//-----------------------------------------------------------------
			if (!driver.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkURL.toUpperCase())) //Checks if hyperlink URL is same as in the dialog
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");			

			if (homePage.listView.itemCount() != prevCt) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. Number of objects in the view and in the hyperlink URL are not same.");

			String unAvailableLayouts = Utility.isSimpleListingLayout(homePage);

			if (!unAvailableLayouts.equals("")) //Verifies if SimpleListing layout is getting displayed
				throw new Exception("Test case Failed. Default layout is not getting displayed.");

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if object is in checked out state
				Log.pass("Test case Passed. Checked out object is displayed for the current user.");
			else
				Log.fail("Test case Failed. Object is not checked out in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_10_4A

	        *//**
	        * 54.4.10.4B : Checked out object with 'Simple listing Layout', 'Show the selected View', without selecting Embed authentication details in GetMFilesWebURL for current user should show the object - Opearations menu.
	        *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Checked out object with 'Simple listing Layout', 'Show the selected View', without selecting Embed authentication details in GetMFilesWebURL for current user should show the object - Opearations menu.")
	public void SprintTest54_4_10_4B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			int prevCt = homePage.listView.itemCount(); //Gets the number of objects in the view

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from operations menu
			//-----------------------------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-4 : Select 'Show the selected view', 'Simple listing' layout if not selected, un selected embed authetication and copy the hyperlink url
			//---------------------------------------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW VIEW")) //Selects Show view
				throw new Exception("Show View is not selected in GetMFilesWebURL dialog.");

			if (!mfilesDialog.setHyperLinkEmbedAuthentication(false)) //UnSelects the Embed authentication details
				throw new Exception("Embed authentication details is unselected from GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected View, Simple listing layout are selected, Embed authentication is unselected and hyperlink URL is obtained.");

			//Step-5 : Log out from current session
			//-------------------------------------
			if (!Utility.logOut(driver)) //Logs out from the vault
				throw new Exception ("Log out from vault (" + testVault + ") is unsuccessful.");

			Log.message("5. Log out from vault (" + testVault + ") is successful.");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL, userName, password, ""); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Pasted the URL copied from hyperlink dialog and logged in with valid credentials.");

			//Verification : Verify object got checked out in the hyperlink URL
			//-----------------------------------------------------------------
			if (!driver.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkURL.toUpperCase())) //Checks if hyperlink URL is same as in the dialog
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");			

			if (homePage.listView.itemCount() != prevCt) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. Number of objects in the view and in the hyperlink URL are not same.");

			String unAvailableLayouts = Utility.isSimpleListingLayout(homePage);

			if (!unAvailableLayouts.equals("")) //Verifies if SimpleListing layout is getting displayed
				throw new Exception("Test case Failed. Default layout is not getting displayed.");

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if object is in checked out state
				Log.pass("Test case Passed. Checked out object is displayed for the current user.");
			else
				Log.fail("Test case Failed. Object is not checked out in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_10_4B

	         *//**
	         * 54.4.11.1A : Checked out object with 'Default Layout', 'Show the selected Object', 'Embed authentication details' in GetMFilesWebURL for current user should show the object - Context menu.
	         *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL","Bug"}, 
			description = "Checked out object with 'Default Layout',  'Show the selected Object', 'Embed authentication details' in GetMFilesWebURL for current user should show the object - Context menu.")
	public void SprintTest54_4_11_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from context menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-4 : Select 'Show the selected file', 'Default' layout, 'Embed authetication' if not selected and copy the hyperlink url
			//----------------------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			if (!mfilesDialog.setHyperLinkEmbedAuthentication(true)) //Selects the Embed authentication details
				throw new Exception("Embed authentication details is not selected from GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File, Default layout, Embed authentication are selected and hyperlink URL is obtained.");

			//Step-5 : Log out from current session
			//-------------------------------------
			if (!Utility.logOut(driver)) //Logs out from the vault
				throw new Exception ("Log out from vault (" + testVault + ") is unsuccessful.");

			Log.message("5. Log out from vault (" + testVault + ") is successful.");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Pasted the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URL
			//-----------------------------------------------------------------
			if (!driver.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkURL.toUpperCase())) //Checks if hyperlink URL is same as in the dialog
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");		

			if (homePage.listView.itemCount() != 1) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. One item is not getting displayed on opening the hyperlink.");

			String unAvailableLayouts = Utility.isDefaultLayout(homePage);

			if (!unAvailableLayouts.equals("")) //Verifies if default layout is getting displayed
				throw new Exception("Test case Failed. Default layout is not getting displayed.");

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if object is in checked out state
				Log.pass("Test case Passed. Checked out object is displayed for the current user.");
			else
				Log.fail("Test case Failed. Object is not checked out in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_11_1A

	          *//**
	          * 54.4.11.1B : Checked out object with 'Default Layout', 'Show the selected Object', 'Embed authentication details' in GetMFilesWebURL for current user should show the object - Operations menu.
	          *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL","Bug"}, 
			description = "Checked out object with 'Default Layout',  'Show the selected Object', 'Embed authentication details' in GetMFilesWebURL for current user should show the object - Operations menu.")
	public void SprintTest54_4_11_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from operations menu
			//-----------------------------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-4 : Select 'Show the selected file', 'Default' layout, 'Embed authetication' if not selected and copy the hyperlink url
			//----------------------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			if (!mfilesDialog.setHyperLinkEmbedAuthentication(true)) //Selects the Embed authentication details
				throw new Exception("Embed authentication details is not selected from GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File, Default layout, Embed authentication are selected and hyperlink URL is obtained.");

			//Step-5 : Log out from current session
			//-------------------------------------
			if (!Utility.logOut(driver)) //Logs out from the vault
				throw new Exception ("Log out from vault (" + testVault + ") is unsuccessful.");

			Log.message("5. Log out from vault (" + testVault + ") is successful.");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Pasted the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URL
			//-----------------------------------------------------------------
			if (!driver.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkURL.toUpperCase())) //Checks if hyperlink URL is same as in the dialog
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");			

			if (homePage.listView.itemCount() != 1) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. One item is not getting displayed on opening the hyperlink.");

			String unAvailableLayouts = Utility.isDefaultLayout(homePage);

			if (!unAvailableLayouts.equals("")) //Verifies if default layout is getting displayed
				throw new Exception("Test case Failed. Default layout is not getting displayed.");

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if object is in checked out state
				Log.pass("Test case Passed. Checked out object is displayed for the current user.");
			else
				Log.fail("Test case Failed. Object is not checked out in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_11_1B

	           *//**
	           * 54.4.11.2A : Checked out object with 'Simple listing Layout','Show the selected Object', 'Embed authentication details' in GetMFilesWebURL for current user should show the object - Context menu.
	           *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Checked out object with 'Simple listing Layout','Show the selected Object', 'Embed authentication details' in GetMFilesWebURL for current user should show the object - Context menu.")
	public void SprintTest54_4_11_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from context menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-4 : Select 'Show the selected file', 'SimpleListing' layout, 'Embed authetication' if not selected and copy the hyperlink url
			//----------------------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			if (!mfilesDialog.setHyperLinkEmbedAuthentication(true)) //Selects the Embed authentication details
				throw new Exception("Embed authentication details is not selected from GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File, SimpleListing layout, Embed authentication are selected and hyperlink URL is obtained.");

			//Step-5 : Log out from current session
			//-------------------------------------
			if (!Utility.logOut(driver)) //Logs out from the vault
				throw new Exception ("Log out from vault (" + testVault + ") is unsuccessful.");

			Log.message("5. Log out from vault (" + testVault + ") is successful.");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Pasted the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URL
			//-----------------------------------------------------------------
			if (!driver.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkURL.toUpperCase())) //Checks if hyperlink URL is same as in the dialog
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");			

			if (homePage.listView.itemCount() != 1) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. One item is not getting displayed on opening the hyperlink.");

			String unAvailableLayouts = Utility.isSimpleListingLayout(homePage);

			if (!unAvailableLayouts.equals("")) //Verifies if Simple listing layout is getting displayed
				throw new Exception("Test case Failed. Simple listing layout is not getting displayed.");

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if object is in checked out state
				Log.pass("Test case Passed. Checked out object is displayed for the current user.");
			else
				Log.fail("Test case Failed. Object is not checked out in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_11_2A

	            *//**
	            * 54.4.11.2B : Checked out object with 'Simple listing Layout','Show the selected Object', 'Embed authentication details' in GetMFilesWebURL for current user should show the object - Operations menu.
	            *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Checked out object with 'Simple listing Layout','Show the selected Object', 'Embed authentication details' in GetMFilesWebURL for current user should show the object - Operations menu.")
	public void SprintTest54_4_11_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from operations menu
			//-----------------------------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-4 : Select 'Show the selected file', 'SimpleListing' layout, 'Embed authetication' if not selected and copy the hyperlink url
			//----------------------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT")) //Selects Show Object
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			if (!mfilesDialog.setHyperLinkEmbedAuthentication(true)) //Selects the Embed authentication details
				throw new Exception("Embed authentication details is not selected from GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File, SimpleListing layout, Embed authentication are selected and hyperlink URL is obtained.");

			//Step-5 : Log out from current session
			//-------------------------------------
			if (!Utility.logOut(driver)) //Logs out from the vault
				throw new Exception ("Log out from vault (" + testVault + ") is unsuccessful.");

			Log.message("5. Log out from vault (" + testVault + ") is successful.");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Pasted the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URL
			//-----------------------------------------------------------------
			if (!driver.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkURL.toUpperCase())) //Checks if hyperlink URL is same as in the dialog
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");			

			if (homePage.listView.itemCount() != 1) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. One item is not getting displayed on opening the hyperlink.");

			String unAvailableLayouts = Utility.isSimpleListingLayout(homePage);

			if (!unAvailableLayouts.equals("")) //Verifies if Simple listing layout is getting displayed
				throw new Exception("Test case Failed. Simple listing layout is not getting displayed.");

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if object is in checked out state
				Log.pass("Test case Passed. Checked out object is displayed for the current user.");
			else
				Log.fail("Test case Failed. Object is not checked out in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_11_2B

	             *//**
	             * 54.4.11.3A : Checked out object with 'Default Layout',  'Show the selected View', 'Embed authentication details' in GetMFilesWebURL for current user should show the object - Context menu.
	             *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL","Bug"}, 
			description = "Checked out object with 'Default Layout',  'Show the selected View', 'Embed authentication details' in GetMFilesWebURL for current user should show the object - Context menu.")
	public void SprintTest54_4_11_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			int prevCt = homePage.listView.itemCount(); //Number of items in the view

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from context menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-4 : Select 'Show the selected View', 'Default' layout, 'Embed authetication' if not selected and copy the hyperlink url
			//----------------------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW VIEW")) //Selects Show View
				throw new Exception("Show View is not selected in GetMFilesWebURL dialog.");

			if (!mfilesDialog.setHyperLinkEmbedAuthentication(true)) //Selects the Embed authentication details
				throw new Exception("Embed authentication details is not selected from GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File, Default layout, Embed authentication are selected and hyperlink URL is obtained.");

			//Step-5 : Log out from current session
			//-------------------------------------
			if (!Utility.logOut(driver)) //Logs out from the vault
				throw new Exception ("Log out from vault (" + testVault + ") is unsuccessful.");

			Log.message("5. Log out from vault (" + testVault + ") is successful.");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Pasted the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URL
			//-----------------------------------------------------------------
			if (!driver.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkURL.toUpperCase())) //Checks if hyperlink URL is same as in the dialog
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");		

			if (homePage.listView.itemCount() != prevCt) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. Number of objects in the view and in the hyperlink URL are not same.");

			String unAvailableLayouts = Utility.isDefaultLayout(homePage);

			if (!unAvailableLayouts.equals("")) //Verifies if default layout is getting displayed
				throw new Exception("Test case Failed. Default layout is not getting displayed.");

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if object is in checked out state
				Log.pass("Test case Passed. Checked out object is displayed for the current user.");
			else
				Log.fail("Test case Failed. Object is not checked out in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_11_3A

	              *//**
	              * 54.4.11.3B : Checked out object with 'Default Layout',  'Show the selected View', 'Embed authentication details' in GetMFilesWebURL for current user should show the object - Operations menu.
	              *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL","Bug"}, 
			description = "Checked out object with 'Default Layout',  'Show the selected View', 'Embed authentication details' in GetMFilesWebURL for current user should show the object - Operations menu.")
	public void SprintTest54_4_11_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			int prevCt = homePage.listView.itemCount(); //Number of items in the view

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from operations menu
			//-----------------------------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-4 : Select 'Show the selected View', 'Default' layout, 'Embed authetication' if not selected and copy the hyperlink url
			//----------------------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.DefaultLayout.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.DefaultLayout.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW VIEW")) //Selects Show View
				throw new Exception("Show View is not selected in GetMFilesWebURL dialog.");

			if (!mfilesDialog.setHyperLinkEmbedAuthentication(true)) //Selects the Embed authentication details
				throw new Exception("Embed authentication details is not selected from GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File, Default layout, Embed authentication are selected and hyperlink URL is obtained.");

			//Step-5 : Log out from current session
			//-------------------------------------
			if (!Utility.logOut(driver)) //Logs out from the vault
				throw new Exception ("Log out from vault (" + testVault + ") is unsuccessful.");

			Log.message("5. Log out from vault (" + testVault + ") is successful.");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Pasted the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URL
			//-----------------------------------------------------------------
			if (!driver.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkURL.toUpperCase())) //Checks if hyperlink URL is same as in the dialog
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");			

			if (homePage.listView.itemCount() != prevCt) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. Number of objects in the view and in the hyperlink URL are not same.");

			String unAvailableLayouts = Utility.isDefaultLayout(homePage);

			if (!unAvailableLayouts.equals("")) //Verifies if default layout is getting displayed
				throw new Exception("Test case Failed. Default layout is not getting displayed.");

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if object is in checked out state
				Log.pass("Test case Passed. Checked out object is displayed for the current user.");
			else
				Log.fail("Test case Failed. Object is not checked out in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_11_3B

	               *//**
	               * 54.4.11.4A : Checked out object with 'Simple listing Layout',  'Show the selected View', 'Embed authentication details' in GetMFilesWebURL for current user should show the object - Context menu.
	               *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Checked out object with 'Simple listing Layout',  'Show the selected View', 'Embed authentication details' in GetMFilesWebURL for current user should show the object - Context menu.")
	public void SprintTest54_4_11_4A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			int prevCt = homePage.listView.itemCount(); //Number of items in the view

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from context menu
			//-------------------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//Step-4 : Select 'Show the selected View', 'Simple listing' layout, 'Embed authetication' if not selected and copy the hyperlink url
			//----------------------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW VIEW")) //Selects Show View
				throw new Exception("Show View is not selected in GetMFilesWebURL dialog.");

			if (!mfilesDialog.setHyperLinkEmbedAuthentication(true)) //Selects the Embed authentication details
				throw new Exception("Embed authentication details is not selected from GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File, SimpleListing layout, Embed authentication are selected and hyperlink URL is obtained.");

			//Step-5 : Log out from current session
			//-------------------------------------
			if (!Utility.logOut(driver)) //Logs out from the vault
				throw new Exception ("Log out from vault (" + testVault + ") is unsuccessful.");

			Log.message("5. Log out from vault (" + testVault + ") is successful.");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog


			Log.message("6. Pasted the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URL
			//-----------------------------------------------------------------
			if (!driver.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkURL.toUpperCase())) //Checks if hyperlink URL is same as in the dialog
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");			

			if (homePage.listView.itemCount() != prevCt) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. Number of objects in the view and in the hyperlink URL are not same.");

			String unAvailableLayouts = Utility.isSimpleListingLayout(homePage);

			if (!unAvailableLayouts.equals("")) //Verifies if SimpleListing layout is getting displayed
				throw new Exception("Test case Failed. SimpleListing layout is not getting displayed.");

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if object is in checked out state
				Log.pass("Test case Passed. Checked out object is displayed for the current user.");
			else
				Log.fail("Test case Failed. Object is not checked out in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_11_4A

	                *//**
	                * 54.4.11.4B : Checked out object with 'Simple listing Layout',  'Show the selected View', 'Embed authentication details' in GetMFilesWebURL for current user should show the object - Operations menu.
	                *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Checked out object with 'Simple listing Layout',  'Show the selected View', 'Embed authentication details' in GetMFilesWebURL for current user should show the object - Operations menu.")
	public void SprintTest54_4_11_4B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Select the object and perform check out operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the listView.");

			int prevCt = homePage.listView.itemCount(); //Number of items in the view

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {

				if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from task panel

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception ("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") is checked out.");
			}
			else
				Log.message("2. Object ( " + dataPool.get("ObjectName") + ") already is in checked out state.");

			//Step-3: Select the object and open GetMFilesWebURL dialog from operations menu
			//-----------------------------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("3. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-4 : Select 'Show the selected View', 'Simple listing' layout, 'Embed authetication' if not selected and copy the hyperlink url
			//----------------------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Selects layout
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW VIEW")) //Selects Show View
				throw new Exception("Show View is not selected in GetMFilesWebURL dialog.");

			if (!mfilesDialog.setHyperLinkEmbedAuthentication(true)) //Selects the Embed authentication details
				throw new Exception("Embed authentication details is not selected from GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton(); //Closes M-Files dialog

			Log.message("4. Show the selected File, SimpleListing layout, Embed authentication are selected and hyperlink URL is obtained.");

			//Step-5 : Log out from current session
			//-------------------------------------
			if (!Utility.logOut(driver)) //Logs out from the vault
				throw new Exception ("Log out from vault (" + testVault + ") is unsuccessful.");

			Log.message("5. Log out from vault (" + testVault + ") is successful.");

			//Step-6 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog

			Log.message("6. Pasted the URL copied from hyperlink dialog.");

			//Verification : Verify object got checked out in the hyperlink URL
			//-----------------------------------------------------------------
			if (!driver.getCurrentUrl().toUpperCase().equalsIgnoreCase(hyperlinkURL.toUpperCase())) //Checks if hyperlink URL is same as in the dialog
				throw new Exception("Test case Failed. Hyperlink URL and browser URL are not same.");			

			if (homePage.listView.itemCount() != prevCt) //Verifies if only one object is displayed in the list
				throw new Exception("Test case Failed. Number of objects in the view and in the hyperlink URL are not same.");

			String unAvailableLayouts = Utility.isSimpleListingLayout(homePage);

			if (!unAvailableLayouts.equals("")) //Verifies if SimpleListing layout is getting displayed
				throw new Exception("Test case Failed. SimpleListing layout is not getting displayed.");

			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if object is in checked out state
				Log.pass("Test case Passed. Checked out object is displayed for the current user.",driver);
			else
				Log.fail("Test case Failed. Object is not checked out in the hyperlink URL.", driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_11_4B
	                 */
}

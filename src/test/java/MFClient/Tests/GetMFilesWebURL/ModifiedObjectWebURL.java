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
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class ModifiedObjectWebURL {

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

	/* 54.4.2.1.1A : Modification in object properties by opening metadatacard (context menu) should get reflected in the default layout hyperlink URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Modification in object properties by opening metadatacard (context menu) should get reflected in the default layout hyperlink URL - Context menu")
	public void SprintTest54_4_2_1_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			mfilesDialog.clickCloseButton();// close(); //Closes M-Files dialog

			Log.message("3. Show the selected File and Default custom layout are selected and hyperlink URL is obtained.");

			//Step-4 : Open object metadatacard through context menu
			//------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiating Metadatacard wrapper
			metadatacard.setInfo(dataPool.get("Property")); //Sets the value to the property
			ConcurrentHashMap <String, String> prevPropInfo = metadatacard.getInfo(); 
			metadatacard.saveAndClose(); //Clicks ok button

			Log.message("4. Properties are modified in object metadatacard opened through context menu and saved.");

			//Step-5 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog
			Utils.fluentWait(driver);

			Log.message("5. Navigated to the URL copied from hyperlink dialog.");

			//Step-6 : Open hyperlink object metadatacard through context menu
			//-----------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiating Metadatacard wrapper
			ConcurrentHashMap <String, String> currPropInfo = metadatacard.getInfo(); 
			metadatacard.clickDiscardButton(); //Clicks Cancel button

			Log.message("6. Hyperlink object (" + dataPool.get("ObjectName") + ") metadatacard is opened and properties are obtained.");

			//Verification : Verify if modified properties gets reflected in the hyperlink object
			//-----------------------------------------------------------------------------------
			String diff = Utility.compareObjects(prevPropInfo, currPropInfo);

			if (diff.equals(""))
				Log.pass("Test case Passed. Modified properties got reflected in the hyperlink url.");
			else
				Log.fail("Test case Failed. Modified properties are not reflected in the hyperlink url. Additional info : " + diff, driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_2_1_1A

	/**
	 * 54.4.2.1.1B : Modification in object properties by opening metadatacard (context menu) should get reflected in the default layout hyperlink URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Modification in object properties by opening metadatacard (context menu) should get reflected in the default layout hyperlink URL - Operations menu")
	public void SprintTest54_4_2_1_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			mfilesDialog.clickCloseButton();// close(); //Closes M-Files dialog

			Log.message("3. Show the selected File and Default custom layout are selected and hyperlink URL is obtained.");

			//Step-4 : Open object metadatacard through context menu
			//------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiating Metadatacard wrapper
			metadatacard.setInfo(dataPool.get("Property")); //Sets the value to the property
			ConcurrentHashMap <String, String> prevPropInfo = metadatacard.getInfo(); 
			metadatacard.saveAndClose(); //Clicks ok button

			Log.message("4. Properties are modified in object metadatacard opened through context menu and saved.");

			//Step-5 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog
			Utils.fluentWait(driver);

			Log.message("5. Navigated to the URL copied from hyperlink dialog.");

			//Step-6 : Open hyperlink object metadatacard through context menu
			//-----------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiating Metadatacard wrapper
			ConcurrentHashMap <String, String> currPropInfo = metadatacard.getInfo(); 
			metadatacard.clickDiscardButton(); //Clicks Cancel button

			Log.message("6. Hyperlink object (" + dataPool.get("ObjectName") + ") metadatacard is opened and properties are obtained.");

			//Verification : Verify if modified properties gets reflected in the hyperlink object
			//-----------------------------------------------------------------------------------
			String diff = Utility.compareObjects(prevPropInfo, currPropInfo);

			if (diff.equals(""))
				Log.pass("Test case Passed. Modified properties got reflected in the hyperlink url.");
			else
				Log.fail("Test case Failed. Modified properties are not reflected in the hyperlink url. Additional info : " + diff, driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_2_1_1B

	/**
	 * 54.4.2.1.2A : Modification in object properties by opening metadatacard (operations menu) should get reflected in the default layout hyperlink URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Modification in object properties by opening metadatacard (operations menu) should get reflected in the default layout hyperlink URL - Context menu")
	public void SprintTest54_4_2_1_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			mfilesDialog.clickCloseButton();// close(); //Closes M-Files dialog

			Log.message("3. Show the selected File and Default custom layout are selected and hyperlink URL is obtained.");

			//Step-4 : Open object metadatacard through operations menu
			//------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from operations menu

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiating Metadatacard wrapper
			metadatacard.setInfo(dataPool.get("Property")); //Sets the value to the property
			ConcurrentHashMap <String, String> prevPropInfo = metadatacard.getInfo(); 
			metadatacard.saveAndClose(); //Clicks ok button

			Log.message("4. Properties are modified in object metadatacard opened through operations menu and saved.");

			//Step-5 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog
			Utils.fluentWait(driver);

			Log.message("5. Navigated to the URL copied from hyperlink dialog.");

			//Step-6 : Open hyperlink object metadatacard through context menu
			//-----------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiating Metadatacard wrapper
			ConcurrentHashMap <String, String> currPropInfo = metadatacard.getInfo(); 
			metadatacard.clickDiscardButton(); //Clicks Cancel button

			Log.message("6. Hyperlink object (" + dataPool.get("ObjectName") + ") metadatacard is opened and properties are obtained.");

			//Verification : Verify if modified properties gets reflected in the hyperlink object
			//-----------------------------------------------------------------------------------
			String diff = Utility.compareObjects(prevPropInfo, currPropInfo);

			if (diff.equals(""))
				Log.pass("Test case Passed. Modified properties got reflected in the hyperlink url.");
			else
				Log.fail("Test case Failed. Modified properties are not reflected in the hyperlink url. Additional info : " + diff, driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_2_1_2A

	/**
	 * 54.4.2.1.2B : Modification in object properties by opening metadatacard (operations menu) should get reflected in the default layout hyperlink URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Modification in object properties by opening metadatacard (operations menu) should get reflected in the default layout hyperlink URL - Operations menu")
	public void SprintTest54_4_2_1_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			mfilesDialog.clickCloseButton();// close(); //Closes M-Files dialog

			Log.message("3. Show the selected File and Default custom layout are selected and hyperlink URL is obtained.");

			//Step-4 : Open object metadatacard through operations menu
			//------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from operations menu

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiating Metadatacard wrapper
			metadatacard.setInfo(dataPool.get("Property")); //Sets the value to the property
			ConcurrentHashMap <String, String> prevPropInfo = metadatacard.getInfo(); 
			metadatacard.saveAndClose(); //Clicks ok button

			Log.message("4. Properties are modified in object metadatacard opened through operations menu and saved.");

			//Step-5 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog
			Utils.fluentWait(driver);

			Log.message("5. Navigated to the URL copied from hyperlink dialog.");

			//Step-6 : Open hyperlink object metadatacard through context menu
			//-----------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiating Metadatacard wrapper
			ConcurrentHashMap <String, String> currPropInfo = metadatacard.getInfo(); 
			metadatacard.clickDiscardButton(); //Clicks Cancel button

			Log.message("6. Hyperlink object (" + dataPool.get("ObjectName") + ") metadatacard is opened and properties are obtained.");

			//Verification : Verify if modified properties gets reflected in the hyperlink object
			//-----------------------------------------------------------------------------------
			String diff = Utility.compareObjects(prevPropInfo, currPropInfo);

			if (diff.equals(""))
				Log.pass("Test case Passed. Modified properties got reflected in the hyperlink url.");
			else
				Log.fail("Test case Failed. Modified properties are not reflected in the hyperlink url. Additional info : " + diff, driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_2_1_2B

	/**
	 * 54.4.2.1.3A : Modification in object properties by opening metadatacard (right pane) should get reflected in the default layout hyperlink URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Modification in object properties by opening metadatacard (right pane) should get reflected in the default layout hyperlink URL - Context menu")
	public void SprintTest54_4_2_1_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			mfilesDialog.clickCloseButton();// close(); //Closes M-Files dialog

			Log.message("3. Show the selected File and Default custom layout are selected and hyperlink URL is obtained.");

			//Step-4 : Modify properties in right pane object metadatacard
			//------------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			MetadataCard metadatacard = new MetadataCard(driver, true); //Instantiating Metadatacard wrapper
			metadatacard.setInfo(dataPool.get("Property")); //Sets the value to the property
			ConcurrentHashMap <String, String> prevPropInfo = metadatacard.getInfo(); 
			metadatacard.saveAndClose(); //Clicks ok button

			Log.message("4. Properties are modified in the right pane object metadatacard and saved.");

			//Step-5 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog
			Utils.fluentWait(driver);

			Log.message("5. Navigated to the URL copied from hyperlink dialog.");

			//Step-6 : Open hyperlink object metadatacard through context menu
			//-----------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiating Metadatacard wrapper
			ConcurrentHashMap <String, String> currPropInfo = metadatacard.getInfo(); 
			metadatacard.clickDiscardButton(); //Clicks Cancel button

			Log.message("6. Hyperlink object (" + dataPool.get("ObjectName") + ") metadatacard is opened and properties are obtained.");

			//Verification : Verify if modified properties gets reflected in the hyperlink object
			//-----------------------------------------------------------------------------------
			String diff = Utility.compareObjects(prevPropInfo, currPropInfo);

			if (diff.equals(""))
				Log.pass("Test case Passed. Modified properties got reflected in the hyperlink url.");
			else
				Log.fail("Test case Failed. Modified properties are not reflected in the hyperlink url. Additional info : " + diff, driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_2_1_3A

	/**
	 * 54.4.2.1.3B : Modification in object properties by opening metadatacard (right pane) should get reflected in the default layout hyperlink URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Modification in object properties by opening metadatacard (right pane) should get reflected in the default layout hyperlink URL - Operations menu")
	public void SprintTest54_4_2_1_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			mfilesDialog.clickCloseButton();// close(); //Closes M-Files dialog

			Log.message("3. Show the selected File and Default custom layout are selected and hyperlink URL is obtained.");

			//Step-4 : Modify properties in right pane object metadatacard
			//------------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			MetadataCard metadatacard = new MetadataCard(driver, true); //Instantiating Metadatacard wrapper
			metadatacard.setInfo(dataPool.get("Property")); //Sets the value to the property
			ConcurrentHashMap <String, String> prevPropInfo = metadatacard.getInfo(); 
			metadatacard.saveAndClose(); //Clicks ok button

			Log.message("4. Properties are modified in the right pane object metadatacard and saved.");

			//Step-5 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog
			Utils.fluentWait(driver);

			Log.message("5. Navigated to the URL copied from hyperlink dialog.");

			//Step-6 : Open hyperlink object metadatacard through context menu
			//-----------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiating Metadatacard wrapper
			ConcurrentHashMap <String, String> currPropInfo = metadatacard.getInfo(); 
			metadatacard.clickDiscardButton(); //Clicks Cancel button

			Log.message("6. Hyperlink object (" + dataPool.get("ObjectName") + ") metadatacard is opened and properties are obtained.");

			//Verification : Verify if modified properties gets reflected in the hyperlink object
			//-----------------------------------------------------------------------------------
			String diff = Utility.compareObjects(prevPropInfo, currPropInfo);

			if (diff.equals(""))
				Log.pass("Test case Passed. Modified properties got reflected in the hyperlink url.");
			else
				Log.fail("Test case Failed. Modified properties are not reflected in the hyperlink url. Additional info : " + diff, driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_2_1_3B

	/**
	 * 54.4.2.1.4A : Modification in object properties by opening metadatacard (Task panel) should get reflected in the default layout hyperlink URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Modification in object properties by opening metadatacard (Task panel) should get reflected in the default layout hyperlink URL - Context menu")
	public void SprintTest54_4_2_1_4A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			mfilesDialog.clickCloseButton();// close(); //Closes M-Files dialog

			Log.message("3. Show the selected File and Default custom layout are selected and hyperlink URL is obtained.");

			//Step-4 : Open object metadatacard through task panel
			//------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from task panel

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiating Metadatacard wrapper
			metadatacard.setInfo(dataPool.get("Property")); //Sets the value to the property
			ConcurrentHashMap <String, String> prevPropInfo = metadatacard.getInfo(); 
			metadatacard.saveAndClose(); //Clicks ok button

			Log.message("4. Properties are modified in object metadatacard opened through task panel and saved.");

			//Step-5 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog
			Utils.fluentWait(driver);

			Log.message("5. Navigated to the URL copied from hyperlink dialog.");

			//Step-6 : Open hyperlink object metadatacard through context menu
			//-----------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiating Metadatacard wrapper
			ConcurrentHashMap <String, String> currPropInfo = metadatacard.getInfo(); 
			metadatacard.clickDiscardButton(); //Clicks Cancel button

			Log.message("6. Hyperlink object (" + dataPool.get("ObjectName") + ") metadatacard is opened and properties are obtained.");

			//Verification : Verify if modified properties gets reflected in the hyperlink object
			//-----------------------------------------------------------------------------------
			String diff = Utility.compareObjects(prevPropInfo, currPropInfo);

			if (diff.equals(""))
				Log.pass("Test case Passed. Modified properties got reflected in the hyperlink url.");
			else
				Log.fail("Test case Failed. Modified properties are not reflected in the hyperlink url. Additional info : " + diff, driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_2_1_4A

	/**
	 * 54.4.2.1.4B : Modification in object properties by opening metadatacard (Task panel) should get reflected in the default layout hyperlink URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Modification in object properties by opening metadatacard (Task panel) should get reflected in the default layout hyperlink URL - Operations menu")
	public void SprintTest54_4_2_1_4B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			mfilesDialog.clickCloseButton();// close(); //Closes M-Files dialog

			Log.message("3. Show the selected File and Default custom layout are selected and hyperlink URL is obtained.");

			//Step-4 : Open object metadatacard through task panel
			//------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from task panel

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiating Metadatacard wrapper
			metadatacard.setInfo(dataPool.get("Property")); //Sets the value to the property
			ConcurrentHashMap <String, String> prevPropInfo = metadatacard.getInfo(); 
			metadatacard.saveAndClose(); //Clicks ok button

			Log.message("4. Properties are modified in object metadatacard opened through task panel and saved.");

			//Step-5 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog
			Utils.fluentWait(driver);

			Log.message("5. Navigated to the URL copied from hyperlink dialog.");

			//Step-6 : Open hyperlink object metadatacard through context menu
			//-----------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiating Metadatacard wrapper
			ConcurrentHashMap <String, String> currPropInfo = metadatacard.getInfo(); 
			metadatacard.clickDiscardButton(); //Clicks Cancel button

			Log.message("6. Hyperlink object (" + dataPool.get("ObjectName") + ") metadatacard is opened and properties are obtained.");

			//Verification : Verify if modified properties gets reflected in the hyperlink object
			//-----------------------------------------------------------------------------------
			String diff = Utility.compareObjects(prevPropInfo, currPropInfo);

			if (diff.equals(""))
				Log.pass("Test case Passed. Modified properties got reflected in the hyperlink url.");
			else
				Log.fail("Test case Failed. Modified properties are not reflected in the hyperlink url. Additional info : " + diff, driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_2_1_4B

	/**
	 * 54.4.2.2.1A : Modification in object properties by opening metadatacard (context menu) should get reflected in the simple listing layout hyperlink URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Modification in object properties by opening metadatacard (context menu) should get reflected in the simple listing layout hyperlink URL - Context menu")
	public void SprintTest54_4_2_2_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-3 : Select 'Show the selected file', 'SimpleListing' layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT"))
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton();// close(); //Closes M-Files dialog

			Log.message("3. Show the selected File and simple listing layout are selected and hyperlink URL is obtained.");

			//Step-4 : Open object metadatacard through context menu
			//------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiating Metadatacard wrapper
			metadatacard.setInfo(dataPool.get("Property")); //Sets the value to the property
			ConcurrentHashMap <String, String> prevPropInfo = metadatacard.getInfo(); 
			metadatacard.saveAndClose(); //Clicks ok button

			Log.message("4. Properties are modified in object metadatacard opened through context menu and saved.");

			//Step-5 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog
			Utils.fluentWait(driver);

			Log.message("5. Navigated to the URL copied from hyperlink dialog.");

			//Step-6 : Open hyperlink object metadatacard through context menu
			//-----------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiating Metadatacard wrapper
			ConcurrentHashMap <String, String> currPropInfo = metadatacard.getInfo(); 
			metadatacard.clickDiscardButton(); //Clicks Cancel button

			Log.message("6. Hyperlink object (" + dataPool.get("ObjectName") + ") metadatacard is opened and properties are obtained.");

			//Verification : Verify if modified properties gets reflected in the hyperlink object
			//-----------------------------------------------------------------------------------
			String diff = Utility.compareObjects(prevPropInfo, currPropInfo);

			if (diff.equals(""))
				Log.pass("Test case Passed. Modified properties got reflected in the hyperlink url.");
			else
				Log.fail("Test case Failed. Modified properties are not reflected in the hyperlink url. Additional info : " + diff, driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_2_2_1A

	/**
	 * 54.4.2.2.1B : Modification in object properties by opening metadatacard (context menu) should get reflected in the simple listing layout hyperlink URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Modification in object properties by opening metadatacard (context menu) should get reflected in the simple listing layout hyperlink URL - Operations menu")
	public void SprintTest54_4_2_2_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-3 : Select 'Show the selected file', 'SimpleListing' layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT"))
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton();// close(); //Closes M-Files dialog

			Log.message("3. Show the selected File and Simple listing layout are selected and hyperlink URL is obtained.");

			//Step-4 : Open object metadatacard through context menu
			//------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiating Metadatacard wrapper
			metadatacard.setInfo(dataPool.get("Property")); //Sets the value to the property
			ConcurrentHashMap <String, String> prevPropInfo = metadatacard.getInfo(); 
			metadatacard.saveAndClose(); //Clicks ok button

			Log.message("4. Properties are modified in object metadatacard opened through context menu and saved.");

			//Step-5 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog
			Utils.fluentWait(driver);

			Log.message("5. Navigated to the URL copied from hyperlink dialog.");

			//Step-6 : Open hyperlink object metadatacard through context menu
			//-----------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiating Metadatacard wrapper
			ConcurrentHashMap <String, String> currPropInfo = metadatacard.getInfo(); 
			metadatacard.clickDiscardButton(); //Clicks Cancel button

			Log.message("6. Hyperlink object (" + dataPool.get("ObjectName") + ") metadatacard is opened and properties are obtained.");

			//Verification : Verify if modified properties gets reflected in the hyperlink object
			//-----------------------------------------------------------------------------------
			String diff = Utility.compareObjects(prevPropInfo, currPropInfo);

			if (diff.equals(""))
				Log.pass("Test case Passed. Modified properties got reflected in the hyperlink url.");
			else
				Log.fail("Test case Failed. Modified properties are not reflected in the hyperlink url. Additional info : " + diff, driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_2_2_1B

	/**
	 * 54.4.2.2.2A : Modification in object properties by opening metadatacard (operations menu) should get reflected in the simple listing layout hyperlink URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Modification in object properties by opening metadatacard (operations menu) should get reflected in the simple listing layout hyperlink URL - Context menu")
	public void SprintTest54_4_2_2_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-3 : Select 'Show the selected file', 'SimpleListing' layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT"))
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton();// close(); //Closes M-Files dialog

			Log.message("3. Show the selected File and Simple listing layout are selected and hyperlink URL is obtained.");

			//Step-4 : Open object metadatacard through operations menu
			//------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from operations menu

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiating Metadatacard wrapper
			metadatacard.setInfo(dataPool.get("Property")); //Sets the value to the property
			ConcurrentHashMap <String, String> prevPropInfo = metadatacard.getInfo(); 
			metadatacard.saveAndClose(); //Clicks ok button

			Log.message("4. Properties are modified in object metadatacard opened through operations menu and saved.");

			//Step-5 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog
			Utils.fluentWait(driver);

			Log.message("5. Navigated to the URL copied from hyperlink dialog.");

			//Step-6 : Open hyperlink object metadatacard through context menu
			//-----------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiating Metadatacard wrapper
			ConcurrentHashMap <String, String> currPropInfo = metadatacard.getInfo(); 
			metadatacard.clickDiscardButton(); //Clicks Cancel button

			Log.message("6. Hyperlink object (" + dataPool.get("ObjectName") + ") metadatacard is opened and properties are obtained.");

			//Verification : Verify if modified properties gets reflected in the hyperlink object
			//-----------------------------------------------------------------------------------
			String diff = Utility.compareObjects(prevPropInfo, currPropInfo);

			if (diff.equals(""))
				Log.pass("Test case Passed. Modified properties got reflected in the hyperlink url.");
			else
				Log.fail("Test case Failed. Modified properties are not reflected in the hyperlink url. Additional info : " + diff, driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_2_2_2A

	/**
	 * 54.4.2.2.2B : Modification in object properties by opening metadatacard (operations menu) should get reflected in the simple listing layout hyperlink URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Modification in object properties by opening metadatacard (operations menu) should get reflected in the simple listing layout hyperlink URL - Operations menu")
	public void SprintTest54_4_2_2_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-3 : Select 'Show the selected file', 'SimpleListing' layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT"))
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton();// close(); //Closes M-Files dialog

			Log.message("3. Show the selected File and Simple listing layout are selected and hyperlink URL is obtained.");

			//Step-4 : Open object metadatacard through operations menu
			//------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from operations menu

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiating Metadatacard wrapper
			metadatacard.setInfo(dataPool.get("Property")); //Sets the value to the property
			ConcurrentHashMap <String, String> prevPropInfo = metadatacard.getInfo(); 
			metadatacard.saveAndClose(); //Clicks ok button

			Log.message("4. Properties are modified in object metadatacard opened through operations menu and saved.");

			//Step-5 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog
			Utils.fluentWait(driver);

			Log.message("5. Navigated to the URL copied from hyperlink dialog.");

			//Step-6 : Open hyperlink object metadatacard through context menu
			//-----------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiating Metadatacard wrapper
			ConcurrentHashMap <String, String> currPropInfo = metadatacard.getInfo(); 
			metadatacard.clickDiscardButton(); //Clicks Cancel button

			Log.message("6. Hyperlink object (" + dataPool.get("ObjectName") + ") metadatacard is opened and properties are obtained.");

			//Verification : Verify if modified properties gets reflected in the hyperlink object
			//-----------------------------------------------------------------------------------
			String diff = Utility.compareObjects(prevPropInfo, currPropInfo);

			if (diff.equals(""))
				Log.pass("Test case Passed. Modified properties got reflected in the hyperlink url.");
			else
				Log.fail("Test case Failed. Modified properties are not reflected in the hyperlink url. Additional info : " + diff, driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_2_2_2B

	/**
	 * 54.4.2.2.3A : Modification in object properties by opening metadatacard (right pane) should get reflected in the simple listing layout hyperlink URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Modification in object properties by opening metadatacard (right pane) should get reflected in the simple listing layout hyperlink URL - Context menu")
	public void SprintTest54_4_2_2_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-3 : Select 'Show the selected file', 'SimpleListing' layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT"))
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton();// close(); //Closes M-Files dialog

			Log.message("3. Show the selected File and Simple listing layout are selected and hyperlink URL is obtained.");

			//Step-4 : Modify properties in right pane object metadatacard
			//------------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			MetadataCard metadatacard = new MetadataCard(driver, true); //Instantiating Metadatacard wrapper
			metadatacard.setInfo(dataPool.get("Property")); //Sets the value to the property
			ConcurrentHashMap <String, String> prevPropInfo = metadatacard.getInfo(); 
			metadatacard.saveAndClose(); //Clicks ok button

			Log.message("4. Properties are modified in the right pane object metadatacard and saved.");

			//Step-5 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog
			Utils.fluentWait(driver);

			Log.message("5. Navigated to the URL copied from hyperlink dialog.");

			//Step-6 : Open hyperlink object metadatacard through context menu
			//-----------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiating Metadatacard wrapper
			ConcurrentHashMap <String, String> currPropInfo = metadatacard.getInfo(); 
			metadatacard.clickDiscardButton(); //Clicks Cancel button

			Log.message("6. Hyperlink object (" + dataPool.get("ObjectName") + ") metadatacard is opened and properties are obtained.");

			//Verification : Verify if modified properties gets reflected in the hyperlink object
			//-----------------------------------------------------------------------------------
			String diff = Utility.compareObjects(prevPropInfo, currPropInfo);

			if (diff.equals(""))
				Log.pass("Test case Passed. Modified properties got reflected in the hyperlink url.");
			else
				Log.fail("Test case Failed. Modified properties are not reflected in the hyperlink url. Additional info : " + diff, driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_2_2_3A

	/**
	 * 54.4.2.2.3B : Modification in object properties by opening metadatacard (right pane) should get reflected in the simple listing layout hyperlink URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Modification in object properties by opening metadatacard (right pane) should get reflected in the simple listing layout hyperlink URL - Operations menu")
	public void SprintTest54_4_2_2_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects GetMFilesWebURL from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'GetMFilesWebURL' title is not opened.");

			Log.message("2. GetMFilesWebURL dialog of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//Step-3 : Select 'Show the selected file', 'SimpleListing' layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT"))
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton();// close(); //Closes M-Files dialog

			Log.message("3. Show the selected File and Simple listing layout are selected and hyperlink URL is obtained.");

			//Step-4 : Modify properties in right pane object metadatacard
			//------------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			MetadataCard metadatacard = new MetadataCard(driver, true); //Instantiating Metadatacard wrapper
			metadatacard.setInfo(dataPool.get("Property")); //Sets the value to the property
			ConcurrentHashMap <String, String> prevPropInfo = metadatacard.getInfo(); 
			metadatacard.saveAndClose(); //Clicks ok button

			Log.message("4. Properties are modified in the right pane object metadatacard and saved.");

			//Step-5 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog
			Utils.fluentWait(driver);

			Log.message("5. Navigated to the URL copied from hyperlink dialog.");

			//Step-6 : Open hyperlink object metadatacard through context menu
			//-----------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiating Metadatacard wrapper
			ConcurrentHashMap <String, String> currPropInfo = metadatacard.getInfo(); 
			metadatacard.clickDiscardButton(); //Clicks Cancel button

			Log.message("6. Hyperlink object (" + dataPool.get("ObjectName") + ") metadatacard is opened and properties are obtained.");

			//Verification : Verify if modified properties gets reflected in the hyperlink object
			//-----------------------------------------------------------------------------------
			String diff = Utility.compareObjects(prevPropInfo, currPropInfo);

			if (diff.equals(""))
				Log.pass("Test case Passed. Modified properties got reflected in the hyperlink url.");
			else
				Log.fail("Test case Failed. Modified properties are not reflected in the hyperlink url. Additional info : " + diff, driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_2_2_3B

	/**
	 * 54.4.2.2.4A : Modification in object properties by opening metadatacard (Task panel) should get reflected in the simple listing layout hyperlink URL - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Modification in object properties by opening metadatacard (Task panel) should get reflected in the simple listing layout hyperlink URL - Context menu")
	public void SprintTest54_4_2_2_4A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-3 : Select 'Show the selected file', 'SimpleListing' layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT"))
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton();// close(); //Closes M-Files dialog

			Log.message("3. Show the selected File and Simple listing layout are selected and hyperlink URL is obtained.");

			//Step-4 : Open object metadatacard through task panel
			//------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from task panel

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiating Metadatacard wrapper
			metadatacard.setInfo(dataPool.get("Property")); //Sets the value to the property
			ConcurrentHashMap <String, String> prevPropInfo = metadatacard.getInfo(); 
			metadatacard.saveAndClose(); //Clicks ok button

			Log.message("4. Properties are modified in object metadatacard opened through task panel and saved.");

			//Step-5 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog
			Utils.fluentWait(driver);

			Log.message("5. Navigated to the URL copied from hyperlink dialog.");

			//Step-6 : Open hyperlink object metadatacard through context menu
			//-----------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiating Metadatacard wrapper
			ConcurrentHashMap <String, String> currPropInfo = metadatacard.getInfo(); 
			metadatacard.clickDiscardButton(); //Clicks Cancel button

			Log.message("6. Hyperlink object (" + dataPool.get("ObjectName") + ") metadatacard is opened and properties are obtained.");

			//Verification : Verify if modified properties gets reflected in the hyperlink object
			//-----------------------------------------------------------------------------------
			String diff = Utility.compareObjects(prevPropInfo, currPropInfo);

			if (diff.equals(""))
				Log.pass("Test case Passed. Modified properties got reflected in the hyperlink url.");
			else
				Log.fail("Test case Failed. Modified properties are not reflected in the hyperlink url. Additional info : " + diff, driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_2_2_4A

	/**
	 * 54.4.2.2.4B : Modification in object properties by opening metadatacard (Task panel) should get reflected in the simple listing layout hyperlink URL - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint54", "GetMFilesWebURL"}, 
			description = "Modification in object properties by opening metadatacard (Task panel) should get reflected in the simple listing layout hyperlink URL - Operations menu")
	public void SprintTest54_4_2_2_4B(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-3 : Select 'Show the selected file', 'SimpleListing' layout if not selected and copy the hyperlink url
			//------------------------------------------------------------------------------------------------------------
			if (!mfilesDialog.isHyperLinkLayoutOptionSelected(Caption.GetMFilesWebURL.SimpleListing.Value))//Verifies if member object exists in the hyperlink
				mfilesDialog.setHyperLinkLayoutOption(Caption.GetMFilesWebURL.SimpleListing.Value);

			if (!mfilesDialog.setHyperLinkAction("SHOW OBJECT"))
				throw new Exception("Show object is not selected in GetMFilesWebURL dialog.");

			String hyperlinkURL = mfilesDialog.getHyperlink(); //Gets the hyper link
			mfilesDialog.clickCloseButton();// close(); //Closes M-Files dialog

			Log.message("3. Show the selected File and Simple listing layout are selected and hyperlink URL is obtained.");

			//Step-4 : Open object metadatacard through task panel
			//------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from task panel

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiating Metadatacard wrapper
			metadatacard.setInfo(dataPool.get("Property")); //Sets the value to the property
			ConcurrentHashMap <String, String> prevPropInfo = metadatacard.getInfo(); 
			metadatacard.saveAndClose(); //Clicks ok button

			Log.message("4. Properties are modified in object metadatacard opened through task panel and saved.");

			//Step-5 : Navigate the page to hyperlink URL
			//--------------------------------------------
			homePage = Utility.navigateToPage(driver, hyperlinkURL); //Navigates to the URL in the hyperlink dialog
			Utils.fluentWait(driver);

			Log.message("5. Navigated to the URL copied from hyperlink dialog.");

			//Step-6 : Open hyperlink object metadatacard through context menu
			//-----------------------------------------------------------------
			if(!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Clicks on related object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiating Metadatacard wrapper
			ConcurrentHashMap <String, String> currPropInfo = metadatacard.getInfo(); 
			metadatacard.clickDiscardButton(); //Clicks Cancel button

			Log.message("6. Hyperlink object (" + dataPool.get("ObjectName") + ") metadatacard is opened and properties are obtained.");

			//Verification : Verify if modified properties gets reflected in the hyperlink object
			//-----------------------------------------------------------------------------------
			String diff = Utility.compareObjects(prevPropInfo, currPropInfo);

			if (diff.equals(""))
				Log.pass("Test case Passed. Modified properties got reflected in the hyperlink url.");
			else
				Log.fail("Test case Failed. Modified properties are not reflected in the hyperlink url. Additional info : " + diff, driver);

		}
		catch (Exception e) { 
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest54_4_2_2_4B


}

package MFClient.Tests;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.Utils;
import genericLibrary.WebDriverUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;

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
import MFClient.Wrappers.ListView;
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;


@Listeners(EmailReport.class)

public class RightPaneOperations {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String userName = null;
	public static String userFullName = null;
	public static String password = null;
	public static String testVault = null;
	public static String configURL = null;
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
			userName = xmlParameters.getParameter("UserName");
			userFullName = xmlParameters.getParameter("UserFullName");
			password = xmlParameters.getParameter("Password");
			configURL = xmlParameters.getParameter("ConfigurationURL");
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
	 * 105.10.1 : Verify if Name field value is pre-filled in new meta data card from task pane in Other views 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint105", "RightPaneOperations"}, 
			description = "Verify preview and metadata tab in home page.")
	public void SprintTest105_10_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.

			//Step-1 : Check the Right pane is displayed 
			//------------------------------------------
			if (!homePage.isRightPaneDisplayed())
				throw new Exception("Right pane is not displayed.");

			Log.message("1. Right Pane is displayed in Selected vault.", driver);

			//Verification : Verify the Metadata Tab & Preview pane is displayed
			//-----------------------------------------------------------
			if (homePage.previewPane.isTabExists("Metadata") && homePage.previewPane.isTabExists("Preview"))
				Log.pass("Test Case Passed. Metadata & Preview tab is displayed in Right Pane.", driver);
			else
				Log.fail("Test Case Failed. Metadata & Preview tab is not displayed in Right Pane.", driver);

		}//End try

		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally{
			Utility.quitDriver(driver);
		}//End finally

	}//End SprintTest105_10_1

	/**
	 * 105.10.2 : Verify preview tab in home page
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint105", "RightPaneOperations"}, 
			description = "Verify preview tab in home page")
	public void SprintTest105_10_2(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.

			//Step-1 : Click the Preview Tab in Right Pane 
			//------------------------------------------
			if (!homePage.isRightPaneDisplayed())
				throw new Exception("Right pane is not displayed.");

			homePage.previewPane.clickPreviewTab();

			Log.message("1.Preview tab is clicked in right pane.");

			//Verification : Verify if Preview tab has got selected in right pane 
			//-----------------------------------------------------------------
			if (homePage.isPreviewTabSelected()) 
				Log.pass("Test Case Passed. Preview tab is selected in Home Page.", driver);
			else
				Log.fail("Test Case Failed. Preview tab is not selected in Home Page.", driver);

		}//End try
		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End SprintTest105_10_2
	/**
	 * 105.10.3 : Verify metadata tab is highlighted automatically in search view
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint105", "RightPaneOperations"}, 
			description = "Verify metadata tab is highlighted automatically in search view")
	public void SprintTest105_10_3(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFiles configuration page
			//-------------------------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configpage  = LoginPage.launchDriverAndLoginToConfig(driver, false); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA configuration page.");

			//Step-2: Click Vault from left panel of Configuration Page
			//-------------------------------------------
			configpage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);

			Log.message("2. Clicked 'Sample vault' from left panel and expand in Configuration Page", driver);


			//Step-3 : Enable search in right pane option in configuration page
			//------------------------------------------------------------------
			configpage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Show.Value);

			if (!configpage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Show.Value))
				throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Show.Value);

			configpage.clickSaveButton();
			configpage.clickOKBtnOnSaveDialog();

			Log.message("3. Show " + dataPool.get("Control") + " is enabled and settings are saved.", driver);

			//Step-4 : Logout from configuration page
			//---------------------------------------
			configpage.clickLogOut(); //Logs out from the Configuration page
			Log.message("4. Logged out from Configuration Webpage");

			//Step-5 : Login to MFWA
			//-----------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); // Launch the URL and login to MFWA.
			Log.message("-----------------------------------------------------------------------------");
			Log.message("1. Logged into MFWA.", driver);

			//Step-6 : Check the Right pane is displayed 
			//------------------------------------------
			if (!homePage.isRightPaneDisplayed())
				throw new Exception("Right pane is not displayed in home view.");

			//Ste-3 : Verify the Metadata Tab & Preview pane is displayed
			//-----------------------------------------------------------
			if (homePage.previewPane.isTabExists("Metadata") && homePage.previewPane.isTabExists("Preview") && homePage.previewPane.isTabExists("Search"))
				Log.pass("Test Case Passed. 'Metadata,Preview & Search' tab is displayed in Right Pane.", driver);
			else
				Log.fail("Test Case Failed. 'Metadata,Preview & Search' tab is not displayed in Right Pane.", driver);
		}
		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {

			try{

				if (driver != null){
					ConfigurationPage configpage  = LoginPage.launchDriverAndLoginToConfig(driver, false); // Launch the URL and login to MFWA.
					Log.message("Logged into MFWA configuration page." );
					configpage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);
					configpage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Hide.Value);
					if (!configpage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Hide.Value))
						throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Hide.Value);

					configpage.clickSaveButton();
					configpage.clickOKBtnOnSaveDialog();
					configpage.clickLogOut(); //Logs out from the Configuration page
				}
			}
			catch(Exception e0) {Log.exception(e0, driver);}
			finally{	
				Utility.quitDriver(driver);
			}
		}//End finally
	}//End SprintTest105_10_3

	/**
	 * 105.10.4 : Verify metadata tab is highlighted automatically in search view
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint105", "RightPaneOperations"}, 
			description = "Verify metadata tab is highlighted automatically in search view")
	public void SprintTest105_10_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Step-1 : Login to MFWA
			//-----------------------


			driver = WebDriverUtils.getDriver();

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.

			//Step-2 : Check the Right pane is displayed 
			//------------------------------------------
			Utils.fluentWait(driver);
			if (!(homePage.isRightPaneDisplayed()))
				throw new Exception ("Right pane operations is not displayed in home view.");


			//Step-3 : Click the Preview Tab in Right Pane
			//--------------------------------------------
			homePage.previewPane.clickPreviewTab();
			if (!homePage.isPreviewTabSelected()) 
				throw new Exception("Test case Failed. Preview tab is not selected in home page.");

			Log.message("1. Preview tab is selected in home page.", driver);

			//Step-2 : Click the Search icon & Navigate to search view
			//--------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyDocuments.Value, "");
			Log.message("2. Navigated to Search view.", driver);


			//Step-3 : Verify Metadata tab should be highlighted automatically
			//----------------------------------------------------------------
			if (homePage.searchPanel.isMetadataTabSelected()) 
				Log.pass("Test Case Passed. Metadata Tab is automatically selected in Search view.", driver);
			else
				Log.fail("Test Case Failed. Metadata Tab is not selected in Search view.", driver);
		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End SprintTest105_10_4

	/**
	 * 105.10.5 : Verify loading symbol is displayed on clicking preview tab
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint105", "RightPaneOperations"}, 
			description = "Verify loading symbol is displayed on clicking preview tab")
	public void SprintTest105_10_5(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Step-1 : Login to MFWA
			//-----------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.

			//Step-1 : Click the Search icon & Navigate to search view
			//--------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyDocuments.Value, dataPool.get("Object"));
			Log.message("1. Click Search icon and Navigate to Search view.");

			//Step-2 : Select any existing object from list view
			//---------------------------------------------------
			homePage.listView.clickItem( dataPool.get("Object"));
			Log.message("2. Select the " + dataPool.get("Object") + " from the list view.", driver);


			//Step-3 : Click the Preview tab in Right Pane
			//--------------------------------------------
			homePage.previewPane.clickPreviewTab();//Click the preview tab in right pane

			if (!homePage.isPreviewTabSelected())//Check if the preview tab is selected or not
				throw new Exception("Preview tab is not got selected.");

			//Verification: Verify the loading symbol after clicking preview tab
			//-------------------------------------------------------------
			if (homePage.previewPane.isLoadingSymbolDisplayed())
				Log.pass("Test Case Passed. Loading symbol is displayed in preview tab.", driver);
			else
				Log.fail("Test Case Failed. Loading symbol is not displayed in preview tab.", driver);

		}//End try
		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally{
			Utility.quitDriver(driver);
		}//End finally

	}//End SprintTest105_10_5

	/**
	 * 105.10.6 : Verify if preview tab is loaded on clicking another object
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint105", "RightPaneOperations"}, 
			description = "Verify if preview tab is loaded on clicking another object")
	public void SprintTest105_10_6(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Logged into MFWA
			//-----------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.

			//Step-1 : Click the Search icon & Navigate to search view
			//--------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyDocuments.Value, dataPool.get("SearchWord"));

			Log.message("1. Click Search icon and Navigate to Search view.", driver);
			//Step-2 : Verify the Object is existing or not in list view
			//----------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("Object"))) 
				throw new Exception(dataPool.get("Object") + " is Exists in list view.");

			Log.message("2. " + dataPool.get("Object") + " is Exists in list view.", driver);

			//Step-4 : Select any existing object from list view
			//---------------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Log.message("3. Select the " + dataPool.get("Object") + " from the list view.", driver);


			//Step-5 : Click the Preview tab in Right Pane
			//--------------------------------------------
			homePage.previewPane.clickPreviewTab();
			if (!homePage.isPreviewTabSelected()) 
				throw new Exception(" Preview tab is not selected in home page.");

			Log.message("4. Preview tab is selected in home page.");

			//Step-6 : Select other object from list view
			//-------------------------------------------
			homePage.listView.clickItem(dataPool.get("AnotherObject"));

			Log.message("5. Select the " + dataPool.get("AnotherObject") + " from the list view.", driver);


			//Step-7 : Verify the loading symbol after clicking preview tab
			//-------------------------------------------------------------
			if (homePage.previewPane.isLoadingSymbolDisplayed())
				Log.pass("Test Case Passed. Loading symbol is displayed in preview tab.", driver);
			else
				Log.fail("Test Case Failed. Loading symbol is not displayed in preview tab.", driver);

		}//End try
		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally{
			Utility.quitDriver(driver);
		}//End finally
	}//End SprintTest105_10_6


	/**
	 * 105.10.7 : Verify preview tab while clicking any document object
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint105", "RightPaneOperations"}, 
			description = "Verify preview tab while clicking any document object")
	public void SprintTest105_10_7(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Step-1 : Login to MFWA
			//-----------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");


			//Step-2 : Click the Search icon & Navigate to search view
			//--------------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "");
			Log.message("2. Navigated to Search view.", driver);

			//Step-3 : Verify the Object is existing or not in list view
			//----------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new Exception(dataPool.get("Object") + " is not Exists in list view.");

			Log.message("3. " + dataPool.get("Object") + " is Exists in list view.");

			//Step-4 : Select any existing object from list view
			//---------------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));

			Log.message("4. Selected the " + dataPool.get("Object") + " from the list view.", driver);

			//Step-5 : Click the Preview tab in Right Pane
			//--------------------------------------------
			homePage.previewPane.clickPreviewTab();
			if (homePage.previewPane.isContentDisplayed()) 
				Log.pass("Test Case Passed. Content is displayed for the " + dataPool.get("Object") +" Object.", driver);
			else
				Log.fail("Test Case Failed. Content is not displayed for the "+ dataPool.get("Object") +" Object.",driver);


		}//End try
		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End SprintTest105_10_7

	/**
	 * 105.10.8 : Verify preview tab for an opened MFD document
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint105", "RightPaneOperations"}, 
			description = "Verify preview tab for an opened MFD document")
	public void SprintTest105_10_8(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Step-1 : Login to MFWA
			//-----------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Click the Search icon & Navigate to search view
			//--------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyDocuments.Value, dataPool.get("Object"));
			Log.message("2. Navigated to Search view.", driver);

			//Step-2a : Verify Selected object exists in the list
			//---------------------------------------------------
			if (!(homePage.listView.isItemExists(dataPool.get("Object")))) 
				throw new Exception(dataPool.get("Object") + " MFD Object does not exists in the list.");


			//Step-3 : Expand the MFD document
			//--------------------------------
			if (!ListView.openMFDByItemName(driver,dataPool.get("Object")))
				throw new Exception("MFD Document: "  + dataPool.get("Object") + " is not Expanded.");

			Log.message("3. Expanded the " + dataPool.get("Object") + " MFD Object", driver);

			//Step-4 : Select the Child documents of MFD Object
			//-------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ChildDocument"));
			Log.message("4. Click the '" + dataPool.get("ChildDocument") + "'  in the " + dataPool.get("Object") + " MFD Object.", driver);


			//Step-5 : Verify the MFD Child Object displayed in Preview Pane
			//--------------------------------------------------------------
			Utils.fluentWait(driver);
			if (!homePage.previewPane.isContentDisplayed()) 
				throw new Exception(dataPool.get("ChildDocument") +  " is not displayed in Preview Pane");

			Log.message("5. MFD Added file : "  + dataPool.get("ChildDocument") +  " is displayed in Preview Pane", driver);

			//Step-6 : Verify the MFD Object displayed in Metadata card
			//---------------------------------------------------------
			homePage.previewPane.clickMetadataTab(); //Click the metadata card
			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver,true);
			if (metadatacard.getTitle().equals(dataPool.get("Object"))) 
				Log.pass("Test Case Passed. MFD Object : " + dataPool.get("Object") + " is displayed in Preview pane Metadata card.", driver);
			else 
				Log.fail("Test Case Failed. MFD Object : " + dataPool.get("Object") + " is not displayed in Preview pane.", driver);

		}//End try
		catch(Exception e) { 
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End SprintTest105_10_8


	/**
	 * 105_10_9 : Verify preview tab for a file in an expanded MFD document
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint105", "RightPaneOperations"}, 
			description = "Verify preview tab for a file in an expanded MFD document")
	public void SprintTest105_10_9(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		try {

			//Step-1 : Login to MFWA
			//-----------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");


			//Step-2 : Click the Search icon & Navigate to search view
			//--------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyDocuments.Value, dataPool.get("Object"));
			Log.message("2. Navigated to Search view.", driver);

			//Step-3 : Expand the MFD document
			//--------------------------------
			if (!homePage.listView.expandItemByName(dataPool.get("Object")))
				throw new Exception("MFD Document: "  + dataPool.get("Object") + " is not Expanded.");

			Log.message("3. Expanded the " + dataPool.get("Object") + " MFD Object", driver);

			//Step-4 : Select the Child documents of MFD Object
			//-------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ChildDocument"));
			Log.message("4. Click the '" + dataPool.get("ChildDocument") + "'  in the " + dataPool.get("Object") + " MFD Object.", driver);


			//Step-5 : Verify the MFD Child Object displayed in Preview Pane
			//--------------------------------------------------------------
			Utils.fluentWait(driver);
			if (homePage.previewPane.isContentDisplayed()) 
				Log.pass("Test Case Passed. MFD Added file : "  + dataPool.get("ChildDocument") +  " is displayed in Preview Pane.", driver);
			else 
				Log.fail("Test Case failed "  + dataPool.get("ChildDocument") +  " is not displayed in Preview Pane.", driver);
		}//End try

		catch(Exception e) { 
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End SprintTest105_10_9

	/**
	 * 105.10.10A : Verify preview tab for a document in history view-in Task pane
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint105", "RightPaneOperations"}, 
			description = "Verify preview tab for a document in history view-in Task pane(latest version)")
	public void SprintTest105_10_10A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Step-1 : Login to MFWA
			//-----------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Click the Search icon & Navigate to search view
			//--------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyDocuments.Value, dataPool.get("Object"));
			Log.message("2. Navigated to Search view.", driver);

			//Step-3 : Select any existing object from List view
			//--------------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Log.message("3. Click " + dataPool.get("Object") +" is existing in the List view.", driver);

			//Step-4 : Click 'History' option from Task pane
			//----------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.History.Value);
			Log.message("4. Click 'History' view in task pane for Selected Object.");

			//Step-5 : Select the latest version for the Object
			//-------------------------------------------------
			if (!homePage.listView.clickItemByIndex(0)) //Selects the latest version of the object 
				throw new Exception("Latest version of an object (" + dataPool.get("Object") + ") is not selected.");

			//Step-6 : Click the Preview tab in sidepane
			//------------------------------------------
			homePage.previewPane.clickPreviewTab();
			Utils.fluentWait(driver);
			Log.message("5. Latest version of " + dataPool.get("Object") + " is selected & side pane Preview tab is clicked.", driver);

			//Step-7 : Verify the Object displayed in Preview tab
			//---------------------------------------------------
			if (homePage.previewPane.isContentDisplayed()) 
				Log.pass("Test Case Passed. Preview of the selected histroy view "+ dataPool.get("Object") + " document is displayed.", driver);
			else
				Log.fail("Test Case Failed. Preview of the selected histroy view "+ dataPool.get("Object") + " document is not displayed.", driver);

		}
		catch(Exception e) { 
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End SprintTest105_10_10A

	/**
	 * 105.10.10B : Verify preview tab for a document in history view-in Context menu
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint105", "RightPaneOperations"}, 
			description = "Verify preview tab for a document in history view-in Context menu(latest version)")
	public void SprintTest105_10_10B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Step-1 : Login to MFWA
			//-----------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Click the Search icon & Navigate to search view
			//--------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyDocuments.Value, dataPool.get("Object"));
			Log.message("2. Navigated to Search view.", driver);

			//Step-3 : Right click the any existing object from List view
			//--------------------------------------------------
			homePage.listView.rightClickItem(dataPool.get("Object"));
			Log.message("3. Click " + dataPool.get("Object") +" is existing in the List view.", driver);

			//Step-4 : Click the History from Context menu
			//---------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.History.Value);
			Log.message("4. Click the 'History' option in Context menu.", driver);

			if (homePage.listView.itemCount() <= 1)
				throw new SkipException("Invalid test data. Object (" + dataPool.get("Object") + ") does not have older versions.");

			//Step-5 : 	Select the older version for the Object
			//-------------------------------------------------
			if (!(homePage.listView.clickItemByIndex(1))) //Selects the latest version of the object 
				throw new Exception("Older version of an object (" + dataPool.get("Object") + ") is not selected.");	

			//Step-6 : Click the Preview tab in sidepane
			//------------------------------------------
			homePage.previewPane.clickPreviewPaneTabs("Preview");
			Utils.fluentWait(driver);
			Log.message("5. Older version of " + dataPool.get("Object") + " is selected & side pane Preview tab is clicked.", driver);

			//Step-7 : Verify the Object displayed in Preview tab
			//---------------------------------------------------
			if (homePage.previewPane.isContentDisplayed())
				Log.pass("Test Case Passed. Preview of the selected histroy view "+ dataPool.get("Object") + " document is displayed.", driver);
			else
				Log.fail("Test Case Failed. Preview of the selected histroy view "+ dataPool.get("Object") + " document is not displayed.", driver);

		}
		catch(Exception e) { 
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End SprintTest105_10_10B

	/**
	 * 105.10.10C : Verify preview tab for a document in history view-in Operations menu
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint105", "RightPaneOperations"}, 
			description = "Verify preview tab for a document in history view-in Operations menu(latest version)")
	public void SprintTest105_10_10C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Step-1 : Login to MFWA
			//-----------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Click the Search icon & Navigate to search view
			//--------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyDocuments.Value, dataPool.get("Object"));
			Log.message("2. Navigated to Search view.", driver);	

			//Step-3 : Select the any existing object in list view
			//----------------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Log.message("3. Select the " + dataPool.get("Object") + " from the list view.", driver);

			//Step-4 : Click the History view from the 'operations/gear' menu
			//-------------------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.History.Value);
			Log.message("4. Click the 'Histroy' option from the Operations menu.", driver);

			if (homePage.listView.itemCount() <= 1)
				throw new SkipException("Invalid test data. Object (" + dataPool.get("Object") + ") does not have older versions.");

			//Step-5 : 	Select the older version for the Object
			//-------------------------------------------------
			if (!(homePage.listView.clickItemByIndex(0))) //Selects the latest version of the object 
				throw new Exception("latest version of an object (" + dataPool.get("Object") + ") is not selected.");	

			//Step-6 : Click the Preview tab in sidepane
			//------------------------------------------
			homePage.previewPane.clickPreviewPaneTabs("Preview");
			Utils.fluentWait(driver);
			Log.message("5. latest version of " + dataPool.get("Object") + " is selected & side pane Preview tab is clicked.", driver);

			//Step-7 : Verify the Object displayed in Preview tab
			//---------------------------------------------------
			if (homePage.previewPane.isContentDisplayed()) 
				Log.pass("Test Case Passed. Preview of the selected histroy view "+ dataPool.get("Object") + " document is displayed.", driver);
			else
				Log.fail("Test Case Failed. Preview of the selected histroy view "+ dataPool.get("Object") + " document is not displayed.", driver);

		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End 105_10_10C	


	/**
	 * 105.10.11A : Metadata card should be displayed  in Checked out to me view
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint105", "RightPaneOperations"}, 
			description = "Metadata card should be displayed  in Checked out to me view")
	public void SprintTest105_10_11A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Step-1 : Login to MFWA
			//-----------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Click the Search icon & Navigate to search view
			//--------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyDocuments.Value, dataPool.get("Object"));
			Log.message("2. Navigated to Search view.", driver);	

			//Step-3 : Select the any existing object in list view
			//----------------------------------------------------
			homePage.listView.rightClickItem(dataPool.get("Object")+"."+dataPool.get("Extension"));
			Log.message("3. Select " + dataPool.get("Object") + " from the list view.", driver);

			//Step-4 : Click the CheckOut from Context menu
			//---------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.CheckOut.Value);
			Log.message("4. Check out the object " + dataPool.get("Object")+"."+dataPool.get("Extension")+ " from the list view.", driver);

			//Step-5 : Click the 'check out to me' from the task pane
			//-------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckedOutToMe.Value);
			homePage.listView.clickItem( dataPool.get("Object")+"."+dataPool.get("Extension"));
			Log.message("5. Navigate to 'check out to me' view and the Select the existing object.", driver);


			//Step-6 : Click the metadata tab in Side Pane
			//--------------------------------------------
			homePage.previewPane.clickMetadataTab(); //Click the metadata card
			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver,true); //Instantiate the Right pane metadata card
			if (metadatacard.getTitle().trim().contains(dataPool.get("Object"))) 
				Log.pass("Test Case Passed. Object : " + dataPool.get("Object")+"."+dataPool.get("Extension") + " is displayed in side pane Metadata card.", driver);
			else 
				Log.fail("Test Case Failed. Object : " + dataPool.get("Object")+"."+dataPool.get("Extension") + " is not displayed in side pane Metadata card.", driver);

		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End 105.10.11A	


	/**
	 * 105.10.11A : preview tab should be displayed in Checked out to me view
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint105", "RightPaneOperations"}, 
			description = "preview tab should be displayed in Checked out to me view")
	public void SprintTest105_10_11B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Step-1 : Login to MFWA
			//-----------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Click the Search icon & Navigate to search view
			//--------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyDocuments.Value, dataPool.get("Object"));
			Log.message("2. Navigated to Search view.", driver);	

			//Step-3 : Select the any existing object in list view
			//----------------------------------------------------
			homePage.listView.rightClickItem(dataPool.get("Object")+"."+dataPool.get("Extension"));
			Log.message("3. Select " + dataPool.get("Object") + " from the list view.", driver);

			//Step-4 : Click the CheckOut from Context menu
			//---------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.CheckOut.Value);
			Log.message("4. Check out the object " + dataPool.get("Object")+"."+dataPool.get("Extension")+ " from the list view.", driver);

			//Step-5 : Click the 'check out to me' from the task pane
			//-------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckedOutToMe.Value);
			homePage.listView.clickItem( dataPool.get("Object")+"."+dataPool.get("Extension"));
			Log.message("5. Navigate to 'check out to me' view and the Select the existing object.", driver);

			//Step-6 : Click the Preview tab in sidepane
			//------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object")+"."+dataPool.get("Extension"));
			homePage.previewPane.clickPreviewPaneTabs("Preview");
			Utils.fluentWait(driver);
			Log.message("6. Object in check out to me " + dataPool.get("Object")+"."+dataPool.get("Extension") + " is selected & side pane Preview tab is clicked.", driver);


			//Step-8 : Verify the Object displayed in Preview tab
			//---------------------------------------------------
			if (homePage.previewPane.isContentDisplayed())
				Log.pass("Test Case Passed. Checked out object: "+ dataPool.get("Object")+"."+dataPool.get("Extension") + " is displayed in preview pane.", driver);
			else
				Log.fail("Test Case Failed. Checked out object: "+ dataPool.get("Object")+"."+dataPool.get("Extension") + " is not displayed in preview pane.", driver);

		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End 105.10.11B	


	/**
	 * 105.10.12A : Metadata tab should be available in Favorites view after selecting an object.
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint105", "RightPaneOperations"}, 
			description = "Metadata tab should be available in Favorites view after selecting an object.")
	public void SprintTest105_10_12A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Step-1 : Login to MFWA
			//-----------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Click the Search button and Navigate to search view
			//--------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyDocuments.Value, dataPool.get("Object"));
			Log.message("2. Click Search buttton and Navigate to Search view.", driver);	

			//Step-3 : Select the any existing object in list view
			//----------------------------------------------------
			homePage.listView.rightClickItem( dataPool.get("Object")+"."+dataPool.get("Extension"));
			Log.message("3. Select " + dataPool.get("Object") + " from the list view.", driver);

			//Step-4 : Click 'Add to favorites' in Context menu
			//-------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.AddToFavorites.Value);//Add the Object in favorites item
			MFilesDialog mfiledialog = new MFilesDialog(driver);
			mfiledialog.clickButton("ok");
			Log.message("4. Mark the object : " + dataPool.get("Object")+"."+dataPool.get("Extension")+ " as Favorites.");

			//Step-5 : Navigate to favorites view
			//-----------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Favorites.Value);
			Log.message("5. Navigate to Favorites view.", driver);

			//Step-6 : Click the metadata tab in Side Pane
			//--------------------------------------------
			homePage.listView.clickItem( dataPool.get("Object")+"."+dataPool.get("Extension"));
			homePage.previewPane.clickPreviewPaneTabs("Metadata"); //Click the metadata card

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver,true); //Instantiate the Right pane metadata card

			if (metadatacard.getTitle().trim().contains( dataPool.get("Object")))
				Log.pass("Test Case Passed. Object : " + dataPool.get("Object")+"."+dataPool.get("Extension") + " is displayed in side pane Metadata card.", driver);
			else 
				Log.fail("Test Case Failed. Object : " + dataPool.get("Object")+"."+dataPool.get("Extension") + " is not displayed in side pane Metadata card.", driver);

		}//End try

		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End 105.10.12A


	/**
	 * 105.10.12B :Preview tab should be available in Favorites view after selecting an object.
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint105", "RightPaneOperations"}, 
			description = "Preview tab should be available in Favorites view after selecting an object.")
	public void SprintTest105_10_12B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Step-1 : Login to MFWA
			//-----------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Click the Search button and Navigate to search view
			//--------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyDocuments.Value, dataPool.get("Object"));
			Log.message("2. Click Search buttton and Navigate to Search view.", driver);	

			//Step-3 : Select the any existing object in list view
			//----------------------------------------------------
			homePage.listView.rightClickItem( dataPool.get("Object")+"."+dataPool.get("Extension"));
			Log.message("3. Select " + dataPool.get("Object") + " from the list view.", driver);

			//Step-4 : Click 'Add to favorites' in Context menu
			//-------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.AddToFavorites.Value);//Add the Object in favorites item
			MFilesDialog mfiledialog = new MFilesDialog(driver);
			mfiledialog.clickButton("ok");
			Log.message("4. Mark the object : " + dataPool.get("Object")+"."+dataPool.get("Extension")+ " as Favorites.");

			//Step-5 : Navigate to favorites view
			//-----------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Favorites.Value);
			Log.message("5. Navigate to Favorites view.", driver);

			//Step-6 : Click the Preview tab in sidepane
			//------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object")+"."+dataPool.get("Extension"));
			homePage.previewPane.clickPreviewPaneTabs("Preview");
			Utils.fluentWait(driver);

			Log.message("6. Object in check out to me " +  dataPool.get("Object")+"."+dataPool.get("Extension") + " is selected & side pane Preview tab is clicked.", driver);

			//Step-7 : Verify the Object displayed in Preview tab
			//---------------------------------------------------
			if (homePage.previewPane.isContentDisplayed())
				Log.pass("Test Case Passed. Favorites view object : "+  dataPool.get("Object")+"."+dataPool.get("Extension")+ " is displayed in preview pane.", driver);
			else
				Log.fail("Test Case Failed. Favorites view object : "+  dataPool.get("Object")+"."+dataPool.get("Extension") + " is not displayed in preview pane.", driver);

		}//End try

		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End 105.10.12B


	/**
	 * 105.10.13A : metadata tab should be available in Recently Accessed by Me view after selecting an object.
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint105", "RightPaneOperations"}, 
			description = "metadata tab should be available in Recently Accessed by Me view after selecting an object.")
	public void SprintTest105_10_13A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Step-1 : Login to MFWA
			//-----------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Click the Search button and Navigate to search view
			//--------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyDocuments.Value, dataPool.get("Object"));
			Log.message("2. Click Search buttton and Navigate to Search view.", driver);	

			//Step-3 : Click the Check Out & Check in the Object for Recently access
			//----------------------------------------------------------------------
			homePage.listView.rightClickItem( dataPool.get("Object")+"."+dataPool.get("Extension"));//Right click the Object 
			homePage.listView.clickContextMenuItem(Caption.MenuItems.CheckOut.Value);//Click the Check out option
			Log.message("3. Click the Object : " +  dataPool.get("Object")+"."+dataPool.get("Extension") + " and Checked out.", driver);

			//Step-4 : Click the 'Recently Access by me' view
			//-----------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.RecentlyAccessedByMe.Value);
			Log.message("4. Click the 'Recently Accessed by me' view.", driver);

			//Step-5 : Verify the Recently accessed by me view in preview pane
			//----------------------------------------------------------------
			if (!(homePage.listView.isItemExists( dataPool.get("Object")+"."+dataPool.get("Extension"))))
				throw new Exception("Object does not exists in the list.");

			//Step-6 : Click the metadata tab in Side Pane
			//--------------------------------------------
			homePage.listView.clickItem( dataPool.get("Object")+"."+dataPool.get("Extension"));
			homePage.previewPane.clickPreviewPaneTabs("Metadata"); //Click the metadata card
			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver,true); //Instantiate the Right pane metadata card

			//Verify the selected object is displayed in side pane metadata card
			if (metadatacard.getTitle().trim().contains( dataPool.get("Object"))) 
				Log.pass("Test Case Passed. Object : " + dataPool.get("Object")+"."+dataPool.get("Extension") + " is displayed in side pane Metadata card.", driver);
			else 
				Log.fail("Test Case Failed. Object : " + dataPool.get("Object")+"."+dataPool.get("Extension") + " is not displayed in side pane Metadata card.", driver);

		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}//End SprintTest105_10_13A


	/**
	 * 105.10.13B : Preview tab should be available in Recently Accessed by Me view after selecting an object.
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint105", "RightPaneOperations"}, 
			description = "Preview tab should be available in Recently Accessed by Me view after selecting an object.")
	public void SprintTest105_10_13B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Step-1 : Login to MFWA
			//-----------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Click the Search button and Navigate to search view
			//--------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyDocuments.Value, dataPool.get("Object"));
			Log.message("2. Click Search buttton and Navigate to Search view.", driver);	

			//Step-3 : Click the Check Out & Check in the Object for Recently access
			//----------------------------------------------------------------------
			homePage.listView.rightClickItem( dataPool.get("Object")+"."+dataPool.get("Extension"));//Right click the Object 
			homePage.listView.clickContextMenuItem(Caption.MenuItems.CheckOut.Value);//Click the Check out option
			Log.message("2a. Click the Object : " +  dataPool.get("Object")+"."+dataPool.get("Extension") + " and Checked out.", driver);

			//Step-4 : Click the 'Recently Access by me' view
			//-----------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.RecentlyAccessedByMe.Value);
			Log.message("3. Click the 'Recently Accessed by me' view.", driver);

			//Step-5 : Verify the Recently accessed by me view in preview pane
			//----------------------------------------------------------------
			if (!(homePage.listView.isItemExists( dataPool.get("Object")+"."+dataPool.get("Extension"))))
				throw new SkipException("Object does not exists in the list.");

			//Step-6 : Click the Preview tab in sidepane
			//------------------------------------------
			homePage.listView.clickItem( dataPool.get("Object")+"."+dataPool.get("Extension"));
			homePage.previewPane.clickPreviewPaneTabs("Preview");
			Utils.fluentWait(driver);
			Log.message("4. Object in check out to me " + dataPool.get("Object") + " is selected & side pane Preview tab is clicked.", driver);


			//Step-7 : Verify the Object displayed in Preview tab
			//---------------------------------------------------
			if (homePage.previewPane.isContentDisplayed()) 
				Log.pass("Test Case Passed. Recently Access by view object : "+  dataPool.get("Object")+"."+dataPool.get("Extension") + " is displayed in Preview Pane.", driver);
			else
				Log.fail("Test Case Failed. Recently Access by view object : "+  dataPool.get("Object")+"."+dataPool.get("Extension") + " is not displayed in Preview Pane.", driver);

		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}//End SprintTest105_10_13B



	/**
	 * 105.10.14 : Preview tab should be available in Recently Accessed by Me view after selecting an object.
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint105", "RightPaneOperations"}, 
			description = "preview tab should be available while clicking any objects other than document object.")
	public void SprintTest105_10_14(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Step-1 : Login to MFWA
			//-----------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to specified view
			//-----------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), "");
			Log.message("2. Navigate to "+ viewtonavigate + " view.", driver);

			//Step-3 : Select the any existing object
			//---------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Log.message("3. Select any existing Object : " + dataPool.get("Object") + " from the "+ viewtonavigate +" view", driver);

			//Step-4 : Select the Preview tab for Existing object
			//---------------------------------------------------
			homePage.previewPane.clickPreviewPaneTabs("Preview");
			Utils.fluentWait(driver);

			Log.message("4. Object " + dataPool.get("Object") + " is selected & side pane Preview tab is clicked.", driver);


			//Step-5 : Verify the Object displayed in Preview tab
			//---------------------------------------------------
			if (homePage.previewPane.isPreviewTabObjectNotDisplayed())
				Log.pass("Test Case Passed. Object : "+  dataPool.get("Object") + " is not displayed in Preview Pane.", driver);
			else
				Log.fail("Test Case Failed. Object : "+  dataPool.get("Object")+" is displayed in Preview Pane.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End SprintTest105_10_14

	/**
	 * 105.10.15 : Report tab should be  displayed as default on clicking a report object.
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint105", "RightPaneOperations"}, 
			description = "Report tab should be displayed as default on clicking a report object.")
	public void SprintTest105_10_15(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Step-1 : Login to MFWA
			//-----------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Create the new 'Report' by menubar
			//-------------------------------------------
			homePage.menuBar.clickNewMenuItem("Report");
			Log.message("2. New 'Report' object is created from the menu bar.", driver);

			//Step-3 : Instantiate the metadata card and Set the Properties
			//-------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);
			metadataCard.setInfo(dataPool.get("Properties")+dataPool.get("Object"));
			metadataCard.clickCreateBtn();
			Log.message("3. New Report object : '" + dataPool.get("Title") + "' is created.");

			//Step-4 : Navigate to 'Search only:Reports' view
			//--------------------------------------------------
			homePage.searchPanel.search(" ", dataPool.get("SearchType"));
			Log.message("4. Navigate to " + dataPool.get("SearchType") + " view.", driver);

			//Step-5 : Select the newly created 'Report' object
			//-------------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Log.message("5. Selected the Report object : " +dataPool.get("Object")+ " from the list view.", driver);

			//Step-6 : Verify the Report tab is displayed as default in Side pane
			//-------------------------------------------------------------------
			if (homePage.previewPane.isTabExists("Report"))
				Log.pass("Test Case Passed. Report tab is displayed default in side pane.", driver);
			else
				Log.fail("Test Case failed. Report tab is not displayed by default in side pane.", driver);
		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End SprintTest105_10_15

	/**
	 * 105.10.16 : preview tab should be displayed for a newly created document object.
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint105", "RightPaneOperations"}, 
			description = "preview tab should be displayed for a newly created document object.")
	public void SprintTest105_10_16(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Step-1 : Login to MFWA
			//-----------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Create the new Document object from the task pane
			//----------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Document.Value);
			if(!Utility.selectTemplate(dataPool.get("Template"), driver)) 
				throw new Exception("The " + dataPool.get("Template") + "Template not available in document metadata card.");
			Log.message("2. Select the New 'Document' object from the task pane.", driver);


			//Step-3 : Set the Metadata Property values in metadata card
			//----------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);
			metadataCard.setInfo(dataPool.get("Properties")+dataPool.get("Object"));
			metadataCard.clickCreateBtn();
			Log.message("3. Set the Properties for the specified document in metadata card.", driver);

			//Step-4 : Navigate to 'Search only:Documents' view
			//--------------------------------------------------
			homePage.searchPanel.search(" ", "Search only: Documents");
			Log.message("4. Navigate to " + dataPool.get("SearchType") + " view.", driver);

			//Step-5 : Select the newly created 'Report' object
			//-------------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object")+dataPool.get("Extension"));
			Log.message("5. Selected the Document object : " +dataPool.get("Object")+ " from the list view.", driver);

			//Step-6 : Click the Preview tab & verify the Document is displayed in Preview tab
			//---------------------------------------------------------------------------------
			homePage.previewPane.clickPreviewPaneTabs("Preview");
			Utils.fluentWait(driver);

			Log.message("6. Preview tab is Clicked from the side pane.", driver);

			//Step-7 : Verify the Document displayed in preview tab
			//-----------------------------------------------------
			if (homePage.previewPane.isContentDisplayed()) 
				Log.pass("Test Case Passed. Selected document is displayed in Preview tab.", driver);
			else
				Log.fail("Test Case Failed. Selected document is not displayed in Preview tab.",driver);


		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End SprintTest105_10_16


	/**
	 * 105.10.18A : Metadata tab should be displayed with right pane search.
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint105", "RightPaneOperations"}, 
			description = "Metadata tab should be displayed with right pane search.")
	public void SprintTest105_10_18A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Step-1 : Login to MFiles configuration page
			//-------------------------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configpage  = LoginPage.launchDriverAndLoginToConfig(driver, false); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA configuration page." );

			//Step-2: Click Vault from left panel of Configuration Page
			//-------------------------------------------
			configpage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);

			Log.message("2. Clicked 'Sample vault' from left panel and expand in Configuration Page", driver);

			//Step-3 : Enable search in right pane option in configuration page
			//------------------------------------------------------------------
			configpage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Show.Value);

			if (!configpage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Show.Value))
				throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Show.Value);

			configpage.clickSaveButton();
			configpage.clickOKBtnOnSaveDialog();

			Log.message("3. Show " + dataPool.get("Control") + " is enabled and settings are saved.", driver);

			//Step-4 : Logout from configuration page
			//---------------------------------------
			configpage.clickLogOut(); //Logs out from the Configuration page
			Log.message("4. Logout from the Configuration page.", driver);

			//Step-5 : Login to MFWA
			//-----------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); // Launch the URL and login to MFWA.
			Log.message("------------------------------------------------------------------------------" );
			Log.message("1. Logged into MFWA.", driver);

			//Step-6 : Check the Right pane is displayed 
			//-------------------------------------------
			if (! (homePage.isRightPaneDisplayed()))
				throw new SkipException ("Right pane menu is not displayed in home view.");

			//Step-7 : Navigate to specified view
			//-----------------------------------
			homePage.searchPanel.clickRightPaneSearchButton();

			Log.message("2. Right pane Search button is clicked.", driver);

			//Step-8 : Select the any existing object
			//---------------------------------------
			homePage.listView.clickItem(dataPool.get("Object")+dataPool.get("Extension"));

			Log.message("3. Select any existing Object : " + dataPool.get("Object")+dataPool.get("Extension") + " from the List view.", driver);

			//Step-9 : Select the Preview tab for Existing object
			//---------------------------------------------------
			homePage.previewPane.clickPreviewPaneTabs("Metadata");
			Utils.fluentWait(driver);

			Log.message("4. Object " + dataPool.get("Object") + " is selected & side pane Preview tab is clicked.", driver);

			MetadataCard metadatacard = new MetadataCard(driver,true); //Instantiate the Right pane metadata card

			//Verification: Verify the metadata card title is displayed or not
			//----------------------------------------------------------------
			if (metadatacard.getTitle().trim().contains( dataPool.get("Object")))
				Log.pass("Test Case Passed. Object : " + dataPool.get("Object")+"."+dataPool.get("Extension") + " is displayed in side pane Metadata card.", driver);
			else 
				Log.fail("Test Case Failed. Object : " + dataPool.get("Object")+"."+dataPool.get("Extension") + " is not displayed in side pane Metadata card.", driver);

		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			try{
				if (driver != null)
				{
					ConfigurationPage configpage  = LoginPage.launchDriverAndLoginToConfig(driver,false); // Launch the URL and login to MFWA.
					configpage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);
					configpage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Hide.Value);

					if (!configpage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Hide.Value))
						throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Hide.Value);

					configpage.clickSaveButton();
					configpage.clickOKBtnOnSaveDialog();

					configpage.clickLogOut(); //Logs out from the Configuration page
				}

			}

			catch(Exception e0) {Log.exception(e0, driver);}
			finally{
				Utility.quitDriver(driver);
			}
		} //End finally
	}//End SprintTest105_10_18A


	/**
	 * 105.10.18B : Preview tab should be displayed with right pane search.
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint105", "RightPaneOperations"}, 
			description = "Preview tab should be displayed with right pane search.")
	public void SprintTest105_10_18B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Step-1 : Login to MFiles configuration page
			//-------------------------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configpage  = LoginPage.launchDriverAndLoginToConfig(driver, false); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA configuration page." );

			//Step-2: Click Vault from left panel of Configuration Page
			//-------------------------------------------
			configpage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);

			Log.message("2. Clicked 'Sample vault' from left panel and expand in Configuration Page", driver);

			//Step-3 : Enable search in right pane option in configuration page
			//------------------------------------------------------------------
			configpage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Show.Value);

			if (!configpage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Show.Value))
				throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Show.Value);

			configpage.clickSaveButton();
			configpage.clickOKBtnOnSaveDialog();

			Log.message("3. Show " + dataPool.get("Control") + " is enabled and settings are saved.", driver);

			//Step-4 : Logout from configuration page
			//---------------------------------------
			configpage.clickLogOut(); //Logs out from the Configuration page
			Log.message("4. LoggedOut from configuration page.");

			//Step-5 : Login to MFWA
			//----------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); // Launch the URL and login to MFWA.
			Log.message("-------------------------------------------------------------------------");
			Log.message("1. Logged into MFWA." );

			//Step-6 : Check the Right pane is displayed 
			//-------------------------------------------
			if (!(homePage.isRightPaneDisplayed()))
				throw new Exception("Right pane operation is displayed in home view.");

			//Step-7 : Navigate to specified view
			//-----------------------------------
			homePage.searchPanel.clickRightPaneSearchButton();
			Log.message("2. Right pane Search button is clicked.", driver);

			//Step-8 : Select the any existing object
			//---------------------------------------
			homePage.listView.clickItem(dataPool.get("Object")+dataPool.get("Extension"));
			Log.message("3. Select any existing Object : " + dataPool.get("Object")+dataPool.get("Extension") + " from the List view." , driver);

			//Step-9 : Select the Preview tab for Existing object
			//---------------------------------------------------
			homePage.previewPane.clickPreviewPaneTabs("Preview");
			Utils.fluentWait(driver);
			Log.message("4. Object " + dataPool.get("Object") + " is selected & side pane Preview tab is clicked.", driver);

			//Step-10 : Click the Preview tab in sidepane
			//------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object")+"."+dataPool.get("Extension"));
			homePage.previewPane.clickPreviewPaneTabs("Preview");
			Utils.fluentWait(driver);

			Log.message("5. Object : " +  dataPool.get("Object")+"."+dataPool.get("Extension") + " is selected & side pane Preview tab is clicked.", driver);

			//Step-11 : Verify the Object displayed in Preview tab
			//---------------------------------------------------
			if (homePage.previewPane.isContentDisplayed())
				Log.pass("Test Case Passed. Object : "+  dataPool.get("Object")+"."+dataPool.get("Extension")+ " is displayed in preview pane.", driver);
			else
				Log.fail("Test Case Failed. Object : "+  dataPool.get("Object")+"."+dataPool.get("Extension") + " is not displayed in preview pane.", driver);

		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			try{
				if (driver != null)
				{
					ConfigurationPage configpage  = LoginPage.launchDriverAndLoginToConfig(driver, false); // Launch the URL and login to MFWA.
					configpage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);
					configpage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Hide.Value);
					if (!configpage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Hide.Value))
						throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Hide.Value);
					configpage.clickSaveButton();
					configpage.clickOKBtnOnSaveDialog();
					configpage.clickLogOut(); //Logs out from the Configuration page
				}
			}

			catch(Exception e0) {Log.exception(e0, driver);}
			finally{
				Utility.quitDriver(driver);
			}
		} //End finally
	}//End SprintTest105_10_18B


	/**
	 * 105.10.19A : Metadata tab should be displayed with right pane search in Checked out to be view.
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint105", "RightPaneOperations"}, 
			description = "Metadata tab should be displayed with right pane search in Checked out to be view.")
	public void SprintTest105_10_19A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Step-1 : Login to MFiles configuration page
			//-------------------------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configpage  = LoginPage.launchDriverAndLoginToConfig(driver, false); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA configuration page." );

			//Step-2: Click Vault from left panel of Configuration Page
			//-------------------------------------------
			configpage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);

			Log.message("2. Clicked 'Sample vault' from left panel and expand in Configuration Page", driver);


			//Step-3 : Enable search in right pane option in configuration page
			//------------------------------------------------------------------
			configpage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Show.Value);
			if (!configpage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Show.Value))
				throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Show.Value);

			configpage.clickSaveButton();
			configpage.clickOKBtnOnSaveDialog();

			Log.message("3. Show " + dataPool.get("Control") + " is enabled and settings are saved.", driver);

			//Step-4 : Logout from configuration page
			//---------------------------------------
			configpage.clickLogOut(); //Logs out from the Configuration page
			Log.message("4. LoggedOut from configuration page.");

			//Step-5 : Login to MFWA
			//----------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); // Launch the URL and login to MFWA.
			Log.message("-----------------------------------------------------------------------");
			Log.message("1. Logged into MFWA." );

			//Step-6 : Check the Right pane is displayed 
			//-------------------------------------------
			if (!homePage.isRightPaneDisplayed())
				throw new Exception("Right pane is not displayed in home view.");

			//Step-7 : Navigate to specified view
			//-----------------------------------
			homePage.searchPanel.clickRightPaneSearchButton();
			Log.message("2. Right pane Search button is clicked.", driver); 

			//Step-8 : Select the Document 'Object' from the 
			//---------------------------------------------------
			homePage.listView.rightClickItem(dataPool.get("Object")+"."+dataPool.get("Extension"));
			Log.message("3. Select " + dataPool.get("Object") + " from the list view.", driver);

			//Step-4 : Click the CheckOut from Context menu
			//---------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.CheckOut.Value);
			Log.message("4. Check out the object " + dataPool.get("Object")+"."+dataPool.get("Extension")+ " from the list view.", driver);

			//Step-5 : Click the 'check out to me' from the task pane
			//-------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckedOutToMe.Value);
			homePage.listView.clickItem( dataPool.get("Object")+"."+dataPool.get("Extension"));
			Log.message("5. Navigate to 'check out to me' view and the Select the existing object.", driver);

			//Step-6 : Verify the Metadata tab is displayed in Search in Right pane
			//---------------------------------------------------------------------
			if (!homePage.previewPane.isTabExists("Metadata") && homePage.previewPane.isTabExists("Search"))
				throw new Exception("Metadata tab is not displayed with Search in Right pane.");

			Log.message("6. Meatadata tab is displayed with Seach in Right pane.", driver);

			//Step-7 : Click the metadata tab in Side Pane
			//--------------------------------------------
			homePage.previewPane.clickMetadataTab(); //Click the metadata card
			Utils.fluentWait(driver);

			//Verification : Verify the metadata title & selected object are same
			//-------------------------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver,true); //Instantiate the Right pane metadata card
			if (metadatacard.getTitle().trim().contains(dataPool.get("Object")))
				Log.pass("Test Case Passed. Object : " + dataPool.get("Object")+"."+dataPool.get("Extension") + " is displayed in Search in right pane Metadata card.", driver);
			else 
				Log.fail("Test Case Failed. Object : " + dataPool.get("Object")+"."+dataPool.get("Extension") + " is not displayed in Search in right pane Metadata card.", driver);


		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			try{

				if (driver != null)
				{
					ConfigurationPage configpage  = LoginPage.launchDriverAndLoginToConfig(driver, false); // Launch the URL and login to MFWA.
					configpage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);
					configpage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Hide.Value);
					if (!configpage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Hide.Value))
						throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Hide.Value);
					configpage.clickSaveButton();
					configpage.clickOKBtnOnSaveDialog();
					configpage.clickLogOut(); //Logs out from the Configuration page
				}

			}
			catch(Exception e0) {Log.exception(e0, driver);}
			finally{
				Utility.quitDriver(driver);
			}
		} //End finally
	}//End SprintTest105_10_19A


	/**
	 * 105.10.19B : Preview tab should be displayed with right pane search in Checked out to be view.
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint105", "RightPaneOperations"}, 
			description = "Preview tab should be displayed with right pane search in Checked out to be view.")
	public void SprintTest105_10_19B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Step-1 : Login to MFiles configuration page
			//-------------------------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configpage  = LoginPage.launchDriverAndLoginToConfig(driver, false); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA configuration page." );

			//Step-2: Click Vault from left panel of Configuration Page
			//-------------------------------------------
			configpage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);

			Log.message("2. Clicked 'Sample vault' from left panel and expand in Configuration Page", driver);


			//Step-3 : Enable search in right pane option in configuration page
			//------------------------------------------------------------------
			configpage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Show.Value);
			if (!configpage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Show.Value))
				throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Show.Value);

			configpage.clickSaveButton();
			configpage.clickOKBtnOnSaveDialog();

			Log.message("3. Show " + dataPool.get("Control") + " is enabled and settings are saved.", driver);

			//Step-4 : Logout from configuration page
			//---------------------------------------
			configpage.clickLogOut(); //Logs out from the Configuration page
			Log.message("4. LoggedOut from configuration page.");

			//Step-5 : Login to MFWA
			//----------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); // Launch the URL and login to MFWA.
			Log.message("------------------------------------------------------------------------------");
			Log.message("1. Logged into MFWA." );

			//Step-6 : Check the Right pane is displayed 
			//------------------------------------------
			if (!homePage.isRightPaneDisplayed())
				throw new Exception("Test Case Failed. Right Pane is not displayed as default in Home Page.");

			Log.message("2. Right Pane is displayed as default in Home Page.", driver);

			//Step-7 : Navigate to specified view
			//-----------------------------------
			homePage.searchPanel.clickRightPaneSearchButton();
			Log.message("3. Right pane Search button is clicked.", driver); 

			//Step-8 : Select the Document 'Object' from the 
			//---------------------------------------------------
			homePage.listView.rightClickItem(dataPool.get("Object")+"."+dataPool.get("Extension"));
			Log.message("4. Select " + dataPool.get("Object") + " from the list view.", driver);

			//Step-4 : Click the CheckOut from Context menu
			//---------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.CheckOut.Value);
			Log.message("5. Check out the object " + dataPool.get("Object")+"."+dataPool.get("Extension")+ " from the list view.", driver);

			//Step-5 : Click the 'check out to me' from the task pane
			//-------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckedOutToMe.Value);
			homePage.listView.clickItem( dataPool.get("Object")+"."+dataPool.get("Extension"));
			Log.message("6. Navigate to 'check out to me' view and the Select the existing object.", driver);

			//Step-6 : Verify the Metadata tab is displayed in Search in Right pane
			//---------------------------------------------------------------------
			if (!homePage.previewPane.isTabExists("Preview") && homePage.previewPane.isTabExists("Search"))
				throw new Exception("Metadata tab is not displaye with Search in Right pane.");

			Log.message("7. Meatadata tab is displayed with Seach in Right pane.", driver);


			//Step-7 : Click the Preview tab in sidepane
			//------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object")+"."+dataPool.get("Extension"));
			homePage.previewPane.clickPreviewPaneTabs("Preview");
			Utils.fluentWait(driver);
			Log.message("8. Object in check out to me " + dataPool.get("Object")+"."+dataPool.get("Extension") + " is selected & side pane Preview tab is clicked.", driver);


			//Step-8 : Verify the Object displayed in Preview tab
			//---------------------------------------------------
			if (homePage.previewPane.isContentDisplayed())
				Log.pass("Test Case Passed. Checked out object: "+ dataPool.get("Object")+"."+dataPool.get("Extension") + " is displayed in preview pane.", driver);
			else
				Log.fail("Test Case Failed. Checked out object: "+ dataPool.get("Object")+"."+dataPool.get("Extension") + " is not displayed in preview pane.", driver);


		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			try{
				if (driver != null)
				{
					ConfigurationPage configpage  = LoginPage.launchDriverAndLoginToConfig(driver, false); // Launch the URL and login to MFWA.
					configpage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);
					configpage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Hide.Value);

					if (!configpage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Hide.Value))
						throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Hide.Value);
					configpage.clickSaveButton();
					configpage.clickOKBtnOnSaveDialog();
					configpage.clickLogOut(); //Logs out from the Configuration page
				}

			}
			catch(Exception e0) {Log.exception(e0, driver);}
			finally{
				Utility.quitDriver(driver);
			}
		} //End finally
	}//End SprintTest105_10_19B

	/**
	 * 105.10.20A : Metadata tab should be displayed with right pane search in Favourites view.
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint105", "RightPaneOperations"}, 
			description = "Metadata tab should be displayed with right pane search in Favourites view.")
	public void SprintTest105_10_20A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Step-1 : Login to MFiles configuration page
			//-------------------------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configpage  = LoginPage.launchDriverAndLoginToConfig(driver, false); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA configuration page." );

			//Step-2: Click Vault from left panel of Configuration Page
			//-------------------------------------------
			configpage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);

			Log.message("2. Clicked 'Sample vault' from left panel and expand in Configuration Page", driver);

			//Step-3 : Enable search in right pane option in configuration page
			//------------------------------------------------------------------
			configpage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Show.Value);

			if (!configpage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Show.Value))
				throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Show.Value);

			configpage.clickSaveButton();
			configpage.clickOKBtnOnSaveDialog();

			Log.message("3. Show " + dataPool.get("Control") + " is enabled and settings are saved.", driver);

			//Step-4 : Logout from configuration page
			//---------------------------------------
			configpage.clickLogOut(); //Logs out from the Configuration page
			Log.message("4. LoggedOut from configuration page.", driver);

			//Step-5 : Login to MFWA
			//----------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); // Launch the URL and login to MFWA.
			Log.message("----------------------------------------------------------------");
			Log.message("1. Logged into MFWA." );

			//Step-6 : Check the Right pane is displayed 
			//-------------------------------------------
			if (!homePage.isRightPaneDisplayed())
				throw new SkipException("Right pane menu is not displayed in home view.");

			//Step-7 : Navigate to specified view
			//-----------------------------------
			homePage.searchPanel.clickRightPaneSearchButton();
			Log.message("2. Right pane Search button is clicked.", driver); 

			//Step-3 : Select the any existing object in list view
			//----------------------------------------------------
			homePage.listView.rightClickItem( dataPool.get("Object")+"."+dataPool.get("Extension"));
			Log.message("3. Select " + dataPool.get("Object") + " from the list view.", driver);

			//Step-4 : Click 'Add to favorites' in Context menu
			//-------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.AddToFavorites.Value);//Add the Object in favorites item
			MFilesDialog mfiledialog = new MFilesDialog(driver);
			mfiledialog.clickButton("ok");
			Log.message("4. Mark the object : " + dataPool.get("Object")+"."+dataPool.get("Extension")+ " as Favorites.", driver);

			//Step-5 : Navigate to favorites view
			//-----------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Favorites.Value);
			Log.message("5. Navigate to Favorites view.", driver);

			//Step-6 : Click the metadata tab in Side Pane
			//--------------------------------------------
			homePage.listView.clickItem( dataPool.get("Object")+"."+dataPool.get("Extension"));
			homePage.previewPane.clickPreviewPaneTabs("Metadata"); //Click the metadata card

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver,true); //Instantiate the Right pane metadata card

			//Verification: Verify the object title in metadata card & selected object should be same
			//---------------------------------------------------------------------------------------
			if (metadatacard.getTitle().trim().contains( dataPool.get("Object")))
				Log.pass("Test Case Passed. Object : " + dataPool.get("Object")+"."+dataPool.get("Extension") + " is displayed in side pane Metadata card.", driver);
			else 
				Log.fail("Test Case Failed. Object : " + dataPool.get("Object")+"."+dataPool.get("Extension") + " is not displayed in side pane Metadata card.", driver);

		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			try{
				if (driver != null)
				{
					ConfigurationPage configpage  = LoginPage.launchDriverAndLoginToConfig(driver, false); // Launch the URL and login to MFWA.
					configpage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);
					configpage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Hide.Value);

					if (!configpage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Hide.Value))
						throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Hide.Value);
					configpage.clickSaveButton();
					configpage.clickOKBtnOnSaveDialog();

					configpage.clickLogOut(); //Logs out from the Configuration page
				}

			}
			catch (Exception e){
				Log.exception(e, driver);
			}
			finally{
				Utility.quitDriver(driver);
			}
		} //End finally
	}//End SprintTest105_10_20A


	/**
	 * 105.10.20B : preview tab should be displayed with right pane search in Favourites view.
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint105", "RightPaneOperations"}, 
			description = "preview tab should be displayed with right pane search in Favourites view")
	public void SprintTest105_10_20B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Step-1 : Login to MFiles configuration page
			//-------------------------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configpage  = LoginPage.launchDriverAndLoginToConfig(driver, false); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA configuration page.");

			//Step-2: Click Vault from left panel of Configuration Page
			//-------------------------------------------
			configpage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);

			Log.message("2. Clicked 'Sample vault' from left panel and expand in Configuration Page", driver);


			//Step-3 : Enable search in right pane option in configuration page
			//------------------------------------------------------------------
			configpage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Show.Value);

			if (!configpage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Show.Value))
				throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Show.Value);

			configpage.clickSaveButton();
			configpage.clickOKBtnOnSaveDialog();

			Log.message("3. Show " + dataPool.get("Control") + " is enabled and settings are saved.", driver);

			//Step-4 : Logout from configuration page
			//---------------------------------------
			configpage.clickLogOut(); //Logs out from the Configuration page
			Log.message("4. LoggedOut from configuration page.");

			//Step-5 : Login to MFWA
			//----------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); // Launch the URL and login to MFWA.
			Log.message("----------------------------------------------------------------------------------");
			Log.message("1. Logged into MFWA.");

			//Step-6 : Check the Right pane is displayed 
			//-------------------------------------------
			if (!homePage.isRightPaneDisplayed())
				throw new Exception("Right pane menu is not displayed in home view.");

			//Step-7 : Navigate to specified view
			//-----------------------------------
			homePage.searchPanel.clickRightPaneSearchButton();
			Log.message("2. Right pane Search button is clicked.", driver); 

			//Step-3 : Select the any existing object in list view
			//----------------------------------------------------
			homePage.listView.rightClickItem( dataPool.get("Object")+"."+dataPool.get("Extension"));
			Log.message("3. Select " + dataPool.get("Object") + " from the list view.", driver);

			//Step-4 : Click 'Add to favorites' in Context menu
			//-------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.AddToFavorites.Value);//Add the Object in favorites item
			MFilesDialog mfiledialog = new MFilesDialog(driver);
			mfiledialog.clickButton("ok");
			Log.message("4. Mark the object : " + dataPool.get("Object")+"."+dataPool.get("Extension")+ " as Favorites.", driver);

			//Step-5 : Navigate to favorites view
			//-----------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Favorites.Value);
			Log.message("5. Navigate to Favorites view.", driver);

			//Step-6 : Click the Preview tab in sidepane
			//------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object")+"."+dataPool.get("Extension"));
			homePage.previewPane.clickPreviewPaneTabs("Preview");
			Utils.fluentWait(driver);

			Log.message("6. Object in Favorites view : " +  dataPool.get("Object")+"."+dataPool.get("Extension") + " is selected & side pane Preview tab is clicked.", driver);

			//Step-7 : Verify the Object displayed in Preview tab
			//---------------------------------------------------
			if (homePage.previewPane.isContentDisplayed())
				Log.pass("Test Case Passed. Favorites view object : "+  dataPool.get("Object")+"."+dataPool.get("Extension")+ " is displayed in Search in right pane preview pane.", driver);
			else
				Log.fail("Test Case Failed. Favorites view object : "+  dataPool.get("Object")+"."+dataPool.get("Extension") + " is not displayed in Search in right pane preview pane.", driver);


		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			try{
				if(driver != null)
				{
					ConfigurationPage configpage  = LoginPage.launchDriverAndLoginToConfig(driver, false); // Launch the URL and login to MFWA.
					configpage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);
					configpage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Hide.Value);

					if (!configpage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Hide.Value))
						throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Hide.Value);
					configpage.clickSaveButton();
					configpage.clickOKBtnOnSaveDialog();
					configpage.clickLogOut(); //Logs out from the Configuration page
				}
			}
			catch(Exception e0) {Log.exception(e0, driver);}
			finally{
				Utility.quitDriver(driver);
			}
		} //End finally
	}//End SprintTest105_10_20B

	/**
	 * 105.10.21A : Metadata tab should be displayed with right pane search in Recently Accessed by Me view.
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint105", "RightPaneOperations"}, 
			description = "Metadata tab should be displayed with right pane search in Recently Accessed by Me view.")
	public void SprintTest105_10_21A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Step-1 : Login to MFiles configuration page
			//-------------------------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configpage  = LoginPage.launchDriverAndLoginToConfig(driver, false); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA configuration page.");

			//Step-2: Click Vault from left panel of Configuration Page
			//-------------------------------------------
			configpage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);

			Log.message("2. Clicked 'Sample vault' from left panel and expand in Configuration Page", driver);

			//Step-3 : Enable search in right pane option in configuration page
			//------------------------------------------------------------------
			configpage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Show.Value);

			if (!configpage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Show.Value))
				throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Show.Value);

			configpage.clickSaveButton();
			configpage.clickOKBtnOnSaveDialog();

			Log.message("3. Show " + dataPool.get("Control") + " is enabled and settings are saved.", driver);

			//Step-4 : Logout from configuration page
			//---------------------------------------
			configpage.clickLogOut(); //Logs out from the Configuration page
			Log.message("4. LoggedOut from configuration page.");

			//Step-5 : Login to MFWA
			//----------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); // Launch the URL and login to MFWA.
			Log.message("-------------------------------------------------------------------------");
			Log.message("1. Logged into MFWA." );

			//Step-6 : Check the Right pane is displayed 
			//-------------------------------------------
			if (!homePage.isRightPaneDisplayed())
				throw new Exception("Right Pane is not displayed as default in Home Page.");

			Log.message("2. Right Pane is displayed as default in Home Page.", driver);

			//Step-7 : Navigate to specified view
			//-----------------------------------
			homePage.searchPanel.clickRightPaneSearchButton();
			Log.message("3. Right pane Search button is clicked.", driver); 

			//Step-8 : Click the Check Out & Check in the Object for Recently access
			//----------------------------------------------------------------------
			homePage.listView.rightClickItem( dataPool.get("Object")+"."+dataPool.get("Extension"));//Right click the Object 
			homePage.listView.clickContextMenuItem(Caption.MenuItems.CheckOut.Value);//Click the Check out option
			Log.message("4. Click the Object : " +  dataPool.get("Object")+"."+dataPool.get("Extension") + " and Checked out.", driver);

			//Step-9 : Click the 'Recently Access by me' view
			//-----------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.RecentlyAccessedByMe.Value);
			Log.message("5. Click the 'Recently Accessed by me' view.", driver);

			//Step-10 : Verify the Recently accessed by me view in preview pane
			//----------------------------------------------------------------
			if (!(homePage.listView.isItemExists( dataPool.get("Object")+"."+dataPool.get("Extension"))))
				throw new Exception("Object does not exists in the list.");

			//Step-11 : Click the metadata tab in Side Pane
			//---------------------------------------------
			homePage.listView.clickItem( dataPool.get("Object")+"."+dataPool.get("Extension"));
			homePage.previewPane.clickPreviewPaneTabs("Metadata"); //Click the metadata card
			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver,true); //Instantiate the Right pane metadata card

			//Verify the selected object is displayed in side pane metadata card
			//------------------------------------------------------------------
			if (metadatacard.getTitle().trim().contains( dataPool.get("Object"))) 
				Log.pass("Test Case Passed. Object : " + dataPool.get("Object")+"."+dataPool.get("Extension") + " is displayed in side pane Metadata card.", driver);
			else 
				Log.fail("Test Case Failed. Object : " + dataPool.get("Object")+"."+dataPool.get("Extension") + " is not displayed in side pane Metadata card.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {

			try{
				if(driver != null)
				{
					ConfigurationPage configpage  = LoginPage.launchDriverAndLoginToConfig(driver, false); // Launch the URL and login to MFWA.
					configpage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);
					configpage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Hide.Value);
					if (!configpage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Hide.Value))							throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Hide.Value);
					configpage.clickSaveButton();
					configpage.clickOKBtnOnSaveDialog();
					configpage.clickLogOut(); //Logs out from the Configuration page
				}

			}
			catch(Exception e0) {Log.exception(e0, driver);}
			finally{
				Utility.quitDriver(driver);
			}
		}
	}//End SprintTest105.10.21A


	/**
	 * 105.10.21B : Preview tab should be displayed with right pane search in Recently Accessed by Me view.
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint105", "RightPaneOperations"}, 
			description = "Preview tab should be displayed with right pane search in Recently Accessed by Me view.")
	public void SprintTest105_10_21B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Step-1 : Login to MFiles configuration page
			//-------------------------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configpage  = LoginPage.launchDriverAndLoginToConfig(driver, false); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA configuration page.");

			//Step-2: Click Vault from left panel of Configuration Page
			//-------------------------------------------
			configpage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);

			Log.message("2. Clicked 'Sample vault' from left panel and expand in Configuration Page", driver);


			//Step-3 : Enable search in right pane option in configuration page
			//------------------------------------------------------------------
			configpage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Show.Value);

			if (!configpage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Show.Value))
				throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Show.Value);

			configpage.clickSaveButton();
			configpage.clickOKBtnOnSaveDialog();

			Log.message("3. Show " + dataPool.get("Control") + " is enabled and settings are saved.", driver);

			//Step-4 : Logout from configuration page
			//---------------------------------------
			configpage.clickLogOut(); //Logs out from the Configuration page
			Log.message("4. LoggedOut from configuration page.", driver);

			//Step-5 : Login to MFWA
			//----------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); // Launch the URL and login to MFWA.
			Log.message("-------------------------------------------------------------");
			Log.message("1. Logged into MFWA.");

			//Step-6 : Check the Right pane is displayed 
			//-------------------------------------------
			if (!homePage.isRightPaneDisplayed())
				throw new Exception("Right pane menu is not displayed in home view.");

			//Step-7 : Navigate to specified view
			//-----------------------------------
			homePage.searchPanel.clickRightPaneSearchButton();
			Log.message("2. Right pane Search button is clicked.", driver); 

			//Step-8 : Click the Check Out & Check in the Object for Recently access
			//----------------------------------------------------------------------
			homePage.listView.rightClickItem( dataPool.get("Object")+"."+dataPool.get("Extension"));//Right click the Object 
			homePage.listView.clickContextMenuItem(Caption.MenuItems.CheckOut.Value);//Click the Check out option
			Log.message("3. Click the Object : " +  dataPool.get("Object")+"."+dataPool.get("Extension") + " and Checked out.", driver);

			//Step-9 : Click the 'Recently Access by me' view
			//-----------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.RecentlyAccessedByMe.Value);
			Log.message("4. Click the 'Recently Accessed by me' view.");

			//Step-10 : Verify the Recently accessed by me view in preview pane
			//----------------------------------------------------------------
			if (!(homePage.listView.isItemExists( dataPool.get("Object")+"."+dataPool.get("Extension"))))
				throw new Exception("Object does not exists in the list.");

			//Step-11 : Click the metadata tab in Side Pane
			//---------------------------------------------
			homePage.listView.clickItem( dataPool.get("Object")+"."+dataPool.get("Extension"));
			homePage.previewPane.clickPreviewPaneTabs("Preview"); //Click the Preview tab
			Utils.fluentWait(driver);

			//Verification : Verify the Object displayed in Preview tab
			//---------------------------------------------------
			if (homePage.previewPane.isContentDisplayed())
				Log.pass("Test Case Passed. Recently Access by me view object : "+  dataPool.get("Object")+"."+dataPool.get("Extension")+ " is displayed in Search in right pane preview pane.", driver);
			else
				Log.fail("Test Case Failed. Recently Access by me view object : "+  dataPool.get("Object")+"."+dataPool.get("Extension") + " is not displayed in Search in right pane preview pane.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {

			try{

				if (driver != null)
				{
					ConfigurationPage configpage  = LoginPage.launchDriverAndLoginToConfig(driver, false); // Launch the URL and login to MFWA.
					configpage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);
					configpage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Hide.Value);
					if (!configpage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Hide.Value))							throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Hide.Value);
					configpage.clickSaveButton();
					configpage.clickOKBtnOnSaveDialog();
					configpage.clickLogOut(); //Logs out from the Configuration page
				}

			}
			catch(Exception e0) {Log.exception(e0, driver);}
			finally{
				Utility.quitDriver(driver);
			}
		}
	}//End SprintTest105.10.21B


	/**
	 * 105.10.23A : Metadata tab should be displayed for a document object in document view.
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint105", "RightPaneOperations"}, 
			description = "Metadata tab should be displayed for a document object in document view.")
	public void SprintTest105_10_23A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Step-1 : Login to MFWA
			//-----------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to specified 'Documents' view
			//-----------------------------------------------
			homePage.taskPanel.clickItem("Home");
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver,dataPool.get("ViewToNavigate") ,"");
			Log.message("2. Navigate to " + viewtonavigate + " view.", driver);

			//Step-3 : Select the Object in Navigate to list view
			//---------------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object")+"."+dataPool.get("Extension"));
			Log.message("3. Select "+dataPool.get("Object")+ " object in "  + viewtonavigate + " view.", driver);

			//Step-4 : Click the Metadata tab in right pane 
			//---------------------------------------------
			homePage.previewPane.clickPreviewPaneTabs("Metadata");
			Log.message("4. Click the Metadata tab in Right pane.", driver);
			MetadataCard metadatacard = new MetadataCard(driver,true); //Instantiate the Right pane metadata card

			//Verify the selected object is displayed in side pane metadata card
			//------------------------------------------------------------------
			if (metadatacard.getTitle().trim().contains( dataPool.get("Object"))) 
				Log.pass("Test Case Passed. Object : " + dataPool.get("Object")+"."+dataPool.get("Extension") + " is displayed in side pane Metadata card.", driver);
			else 
				Log.fail("Test Case Failed. Object : " + dataPool.get("Object")+"."+dataPool.get("Extension") + " is not displayed in side pane Metadata card.", driver);


		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End SprintTest105.10.23A


	/**
	 * 105.10.23B : Preview tab for a document object should be displayed in document view.
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint105", "RightPaneOperations"}, 
			description = "Preview tab for a document object should be displayed in document view.")
	public void SprintTest105_10_23B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Step-1 : Login to MFWA
			//-----------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to specified 'Documents' view
			//-----------------------------------------------
			homePage.taskPanel.clickItem("Home");
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver,dataPool.get("ViewToNavigate") ,"");
			Log.message("2. Navigate to " + viewtonavigate + " view.", driver);

			//Step-3 : Select the Object in Navigate to list view
			//---------------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object")+"."+dataPool.get("Extension"));
			Log.message("3. Select "+dataPool.get("Object")+ " object in "  + viewtonavigate + " view.", driver);

			//Step-4 : Click the Metadata tab in right pane 
			//---------------------------------------------
			homePage.previewPane.clickPreviewPaneTabs("Preview");
			Log.message("4. Click the Preview tab in Right pane for selected object.", driver);

			//Step-5 : Verify the Object displayed in Preview tab
			//---------------------------------------------------
			if (homePage.previewPane.isContentDisplayed())
				Log.pass("Test Case Passed. Recently Access by me view object : "+  dataPool.get("Object")+"."+dataPool.get("Extension")+ " is displayed in Search in right pane preview pane.", driver);
			else
				Log.fail("Test Case Failed. Recently Access by me view object : "+  dataPool.get("Object")+"."+dataPool.get("Extension") + " is not displayed in Search in right pane preview pane.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End SprintTest105.10.23B	

}//End RightPaneOperation

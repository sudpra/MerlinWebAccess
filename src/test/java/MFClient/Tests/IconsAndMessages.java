package MFClient.Tests;

import genericLibrary.ActionEventUtils;
import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.Utils;
import genericLibrary.WebDriverUtils;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.openqa.selenium.Keys;
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
import MFClient.Wrappers.ListView;
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class IconsAndMessages {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String configURL = null;
	public static String userName = null;
	public static String password = null;
	public static String testVault = null;
	public static String className = null;
	public static String productVersion = null;
	public static WebDriver driver = null;
	public static String driverType = null;

	/**
	 * init : Before Class method to perform initial operations.
	 */
	@BeforeClass
	public void init() throws Exception {

		try {

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			xlTestDataWorkBook = xmlParameters.getParameter("TestData");
			loginURL = xmlParameters.getParameter("webSite");
			configURL = xmlParameters.getParameter("ConfigurationURL");
			userName = xmlParameters.getParameter("UserName");
			password = xmlParameters.getParameter("Password");
			testVault = xmlParameters.getParameter("VaultName");
			className = this.getClass().getSimpleName().toString().trim();
			driverType = xmlParameters.getParameter("driverType").toUpperCase().trim();

			if (driverType.equalsIgnoreCase("IE"))
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + driverType + xmlParameters.getParameter("driverVersion").trim();
			else
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + driverType;

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
	 * 56.2.53A : Information Icon should be displayed after adding objects to Favorites view - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint56", "Icon"}, 
			description = "Information Icon should be displayed after adding objects to Favorites view - Context menu")
	public void SprintTest56_2_53A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));
			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select Add to Favorites from Context menu
			//---------------------------------------------------
			if(homePage.listView.itemCount() <= 0) //Checks if object is available
				throw new SkipException("There are no objects in the view.");

			String objectName = ListView.getRandomObject(driver);

			if (!homePage.listView.rightClickItem(objectName)) //Selects the object
				throw new SkipException("The object (" + objectName + ") is not selected.");

			Utils.fluentWait(driver);
			homePage.listView.clickContextMenuItem(Caption.MenuItems.AddToFavorites.Value); //Selects Add to Favorites from context menu
			Utils.fluentWait(driver);

			Log.message("2. Add to Favorites is selected from context menu.");

			//Step-3 : Get the icon displayed in the M-Files Dialog
			//-----------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper			
			String iconName = mfilesDialog.getIcon(); //Gets icon name of the dialog

			Log.message("3. Icon from the M-Files dialog is obtained.");

			//Verification: To verify if the Column is sorted as expected
			//------------------------------------------------------------
			if(iconName.equalsIgnoreCase("INFORMATION")) 
				Log.pass("Test Case Passed. 'Information' icon is displayed in MFiles dialog on adding objects to Favorites view through context menu.", driver);
			else
				Log.fail("Test Case Failed. 'Information' icon is not displayed in MFiles dialog on adding objects to Favorites view through context menu.", driver);

			mfilesDialog.clickOkButton();

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_2_53A

	/**
	 * 56.2.53B : Information Icon should be displayed after adding objects to Favorites view - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint56", "Icon"}, 
			description = "Information Icon should be displayed after adding objects to Favorites view - Operations menu")
	public void SprintTest56_2_53B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select Add to Favorites from Operations menu
			//---------------------------------------------------
			if(homePage.listView.itemCount() <= 0) //Checks if object is available
				throw new SkipException("There are no objects in the view.");

			String objectName = ListView.getRandomObject(driver);

			if (!homePage.listView.clickItem(objectName)) //Selects the object
				throw new SkipException("The object (" + objectName + ") is not selected.");

			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.AddToFavorites.Value); //Selects Add to Favorites from context menu
			Utils.fluentWait(driver);

			Log.message("2. Add to Favorites is selected from operations menu.");

			//Step-3 : Get the icon displayed in the M-Files Dialog
			//-----------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper			
			String iconName = mfilesDialog.getIcon(); //Gets icon name of the dialog

			Log.message("3. Icon from the M-Files dialog is obtained.");

			//Verification: To verify if the Column is sorted as expected
			//------------------------------------------------------------
			if(iconName.equalsIgnoreCase("INFORMATION")) 
				Log.pass("Test Case Passed. 'Information' icon is displayed in MFiles dialog on adding objects to Favorites view through operations menu.", driver);
			else
				Log.fail("Test Case Failed. 'Information' icon is not displayed in MFiles dialog on adding objects to Favorites view through operations menu.", driver);

			mfilesDialog.clickOkButton();

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_2_53B

	/**
	 * 56.2.54A : Information Icon should be displayed after removing objects from Favorites view - Context menu
	 */
	@Test(dependsOnMethods = {"SprintTest56_2_53A"}, groups = {"Sprint56", "Icon"},	
			description = "Information Icon should be displayed after removing objects from Favorites view - Context menu")
	public void SprintTest56_2_54A() throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to Favorites View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, Caption.MenuItems.Favorites.Value, "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select Add to Favorites from Context menu
			//---------------------------------------------------
			if(homePage.listView.itemCount() <= 0) //Checks if object is available
				throw new SkipException("There are no objects in the view.");

			if (!homePage.listView.rightClickItemByIndex(0)) //Selects the object
				throw new SkipException("The first object is not selected.");

			Utils.fluentWait(driver);
			homePage.listView.clickContextMenuItem(Caption.MenuItems.RemoveFromFavorites.Value); //Selects Remove from Favorites from context menu
			Utils.fluentWait(driver);

			Log.message("2. Remove from Favorites is selected from context menu.");

			//Step-3 : Get the icon displayed in the M-Files Dialog
			//-----------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper			
			String iconName = mfilesDialog.getIcon(); //Gets icon name of the dialog

			if (!iconName.equalsIgnoreCase("WARNING"))
				throw new Exception("Warning icon in the confirmation dialog is not displayed.");

			mfilesDialog.clickOkButton();
			mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper			
			iconName = mfilesDialog.getIcon(); //Gets icon name of the dialog

			Log.message("3. Icon from the M-Files dialog is obtained.");

			//Verification: To verify if the Column is sorted as expected
			//------------------------------------------------------------
			if(iconName.equalsIgnoreCase("INFORMATION")) 
				Log.pass("Test Case Passed. 'Information' icon is displayed in MFiles dialog on removing objects from Favorites view through context menu.", driver);
			else
				Log.fail("Test Case Failed. 'Information' icon is not displayed in MFiles dialog on removing objects from Favorites view through context menu.", driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_2_54A

	/**
	 * 56.2.54B : Information Icon should be displayed after removing objects from Favorites view - Operations menu
	 */
	@Test(dependsOnMethods = {"SprintTest56_2_53B"}, groups = {"Sprint56", "Icon"},	
			description = "Information Icon should be displayed after removing objects from Favorites view - Operations menu")
	public void SprintTest56_2_54B() throws Exception {

		driver = null; 

		try {


			driver = WebDriverUtils.getDriver();

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to Favorites View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, Caption.MenuItems.Favorites.Value, "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select Add to Favorites from Context menu
			//---------------------------------------------------
			if(homePage.listView.itemCount() <= 0) //Checks if object is available
				throw new SkipException("There are no objects in the view.");

			if (!homePage.listView.clickItemByIndex(0)) //Selects the object
				throw new SkipException("The first object is not selected.");

			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.RemoveFromFavorites.Value); //Selects Remove from Favorites from context menu
			Utils.fluentWait(driver);

			Log.message("2. Remove from Favorites is selected from operations menu.");

			//Step-3 : Get the icon displayed in the M-Files Dialog
			//-----------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper			
			String iconName = mfilesDialog.getIcon(); //Gets icon name of the dialog

			if (!iconName.equalsIgnoreCase("WARNING"))
				throw new Exception("Warning icon in the confirmation dialog is not displayed.");

			mfilesDialog.clickOkButton();
			mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper			
			iconName = mfilesDialog.getIcon(); //Gets icon name of the dialog

			Log.message("3. Icon from the M-Files dialog is obtained.");

			//Verification: To verify if the Column is sorted as expected
			//------------------------------------------------------------
			if(iconName.equalsIgnoreCase("INFORMATION")) 
				Log.pass("Test Case Passed. 'Information' icon is displayed in MFiles dialog on removing objects from Favorites view through operations menu.", driver);
			else
				Log.fail("Test Case Failed. 'Information' icon is not displayed in MFiles dialog on removing objects from Favorites view through operations menu.", driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_2_54B

	/**
	 * 56_2_54C : Information Icon should be displayed after following the document 
	 */
	@Test(groups = {"Sprint56", "Icon"},description = "Information Icon should be displayed after following the document")
	public void SprintTest56_2_54C() throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to Search Only Documents View
			//-----------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyDocuments.Value, "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select Properties option from Context menu
			//---------------------------------------------------
			if(homePage.listView.itemCount() <= 0) //Checks if object is available
				throw new SkipException("There are no objects in the view.");

			if (!homePage.listView.rightClickItemByIndex(0)) //Selects the object
				throw new SkipException("The first object is not selected.");

			Log.message("2. Right clicked the first object in list view.",driver);

			//Step-3 : Select the 'Properties' option form the context menu
			//-------------------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Remove from Favorites from context menu

			Log.message("3. Selected the " + Caption.MenuItems.Properties.Value + " option from the context menu.");

			//Step-4 : Click on the flag icon in Metadatacard
			//------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the metadatacard
			metadataCard.setFollowFlag(true);//Set the flag in opened metadatacard
			Utils.fluentWait(driver);

			Log.message("4. Click on the flag icon in the opened metadatacard.");

			//Step-5 : Get the icon displayed in the M-Files Dialog
			//-----------------------------------------------------
			Utils.fluentWait(driver);
			MFilesDialog mfilesDialog = new MFilesDialog(driver,"M-Files Web"); //Instantiating MFilesDialog wrapper			
			String iconName = mfilesDialog.getIcon(); //Gets icon name of the dialog

			Log.message("5. Icon from the M-Files dialog is obtained.");

			//Verification: To verify if the Column is sorted as expected
			//------------------------------------------------------------
			if(iconName.equalsIgnoreCase("INFORMATION")) 
				Log.pass("Test Case Passed. 'Information' icon is displayed in MFiles dialog for the following the document.", driver);
			else
				Log.fail("Test Case Failed.'Information' icon is not displayed in MFiles dialog for the following the document.", driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_2_54C

	/**
	 * 56_2_54D : Information Icon should be displayed after unfollowing the document 
	 */
	@Test(groups = {"Sprint56", "Icon"},description = "Information Icon should be displayed after unfollowing the document")
	public void SprintTest56_2_54D() throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to 'Search Only Documents' View
			//-------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyDocuments.Value, "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select Properties option from Context menu
			//---------------------------------------------------
			if(homePage.listView.itemCount() <= 0) //Checks if object is available
				throw new SkipException("There are no objects in the view.");

			if (!homePage.listView.rightClickItemByIndex(0)) //Selects the object
				throw new SkipException("The first object is not selected.");

			Log.message("2. Right clicked the first object in list view.", driver);

			//Step-3 : Select the 'Properties' option form the context menu
			//-------------------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Remove from Favorites from context menu

			Log.message("3. Selected the " + Caption.MenuItems.Properties.Value + " option from the context menu.");

			//Step-4 : Click on the flag icon in Metadatacard
			//------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the metadatacard
			if(!metadataCard.getFollowFlag()){
				metadataCard.setFollowFlag(true);//Set the flag in opened metadatacard

				MFilesDialog mfilesDialog = new MFilesDialog(driver,"M-Files Web");
				mfilesDialog.clickOkButton();
			}

			metadataCard.setFollowFlag(false);

			Log.message("4. Click on the flag icon in the opened metadatacard.");

			//Step-5 : Get the icon displayed in the M-Files Dialog
			//-----------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver,"M-Files Web"); //Instantiating MFilesDialog wrapper			
			String iconName = mfilesDialog.getIcon(); //Gets icon name of the dialog

			Log.message("5. Icon from the M-Files dialog is obtained.");

			//Verification: To verify if the Column is sorted as expected
			//------------------------------------------------------------
			if(iconName.equalsIgnoreCase("INFORMATION")) 
				Log.pass("Test Case Passed. 'Information' icon is displayed in MFiles dialog for the unfollowing the document.", driver);
			else
				Log.fail("Test Case Failed.'Information' icon is not displayed in MFiles dialog for the unfollowing the document.", driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_2_54D


	/**
	 * 56_2_54E : Information Icon should be displayed after changing the password.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Sprint56", "Icon"},
			description = "Information Icon should be displayed after changing the password.")
	public void SprintTest56_2_54E(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Log in to the MFWA
			//------------------
			HomePage homePage =LoginPage.launchDriverAndLogin(driver, dataPool.get("Username"),dataPool.get("Password"),testVault);

			//Step-1 : Navigate to 'Search only documents' View
			//-------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyDocuments.Value, "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select Log out from user display
			//-----------------------------------------
			homePage.menuBar.changePassword(dataPool.get("Password"), "testing@123"); //Selects log out from user display

			Log.message("2. Change password dialog is opened and new password is changed to the user.");

			//Step-3 : Get the icon displayed in the M-Files Dialog
			//-----------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper			
			String iconName = mfilesDialog.getIcon(); //Gets icon name of the dialog

			Log.message("3. Icon from the M-Files dialog is obtained.");

			//Verification: To verify if the Column is sorted as expected
			//------------------------------------------------------------
			if(iconName.equalsIgnoreCase("INFORMATION")) 
				Log.pass("Test Case Passed. 'Information' icon is displayed after changing the password.", driver);
			else
				Log.fail("Test Case Failed.'Information' icon is not displayed after changing the password.", driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_2_54E

	/**
	 * 56_2_61 : Adding objects to favorites using star icon.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Sprint56", "Icon"},
			description = "Adding objects to favorites using star icon.")
	public void SprintTest56_2_61(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Log in to the MFWA
			//------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to 'Search only documents' View
			//-------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyDocuments.Value, "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object from the search view
			//-----------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectName"));

			Log.message("2. Selected the object : " + dataPool.get("ObjectName") + " from the list view.");

			//Step-3 : Click the star icon in open metadatacard
			//-------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver,true);//Instantiate the right pane metadatacard
			metadataCard.setFavorite(true);

			Log.message("3. Clicked the Star icon in opened metadatacard.");

			//Step-4 : Click the 'Ok' button in favorites dialog
			//--------------------------------------------------
			MFilesDialog mfilesdialog = new MFilesDialog(driver);
			mfilesdialog.clickOkButton();

			Log.message("4. Clicked the 'ok' button in warning dialog.", driver);

			//Step-5 : Navigate to the 'Favorites' view
			//-----------------------------------------
			String viewNavigate = SearchPanel.searchOrNavigatetoView(driver, Caption.MenuItems.Favorites.Value, "");

			Log.message("5. Navigated to '" + viewNavigate + "' view.");

			//Verification : Verify if selected object should be displayed in the Favorites view
			//----------------------------------------------------------------------------------
			if(homePage.listView.isItemExists(dataPool.get("ObjectName")))
				Log.pass("Test Case Passed.Object is added to the favorites view using star icon.");
			else
				Log.fail("Test Case Failed.Object is not added to the favorites view.");		


		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_2_61



	/**
	 * 56.2.55A : Warning Icon should be displayed after Deleting an object - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint56", "Icon"}, 
			description = "Warning Icon should be displayed after Deleting an object - Context menu.")
	public void SprintTest56_2_55A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the item 
			//-------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-3 Select Delete from context menu
			//--------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Delete.Value); //Selects Delete from context menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class
			String iconName = mfilesDialog.getIcon();			

			Log.message("3. Delete from context menu is selected.");

			//Verification : To Verify if warning icon is displaying in MFiles dialog
			//---------------------------------------------------------------------------
			if(iconName.equalsIgnoreCase("WARNING")) 
				Log.pass("Test Case Passed. 'Warning' icon is displayed in MFiles dialog on deleting object from context menu.", driver);
			else
				Log.fail("Test Case Failed. 'Warning' icon is not displayed in MFiles dialog on deleting object from context menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_2_55A

	/**
	 * 56.2.55B : Warning Icon should be displayed after Deleting an object - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint56", "Icon"}, 
			description = "Warning Icon should be displayed after Deleting an object - Operations menu.")
	public void SprintTest56_2_55B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the item 
			//-------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-3 Select Delete from context menu
			//--------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Delete.Value); //Selects Delete from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class
			String iconName = mfilesDialog.getIcon();			

			Log.message("3. Delete from operations menu is selected.");

			//Verification : To Verify if warning icon is displaying in MFiles dialog
			//------------------------------------------------------------------------
			if(iconName.equalsIgnoreCase("WARNING")) 
				Log.pass("Test Case Passed. 'Warning' icon is displayed in MFiles dialog on deleting object from Operations menu.", driver);
			else
				Log.fail("Test Case Failed. 'Warning' icon is not displayed in MFiles dialog on deleting object from Operations menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_2_55B

	/**
	 * 56.2.55C : Warning Icon should be displayed after Deleting an object - DEL key
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint56", "Icon"}, 
			description = "Warning Icon should be displayed after Deleting an object - DEL key.")
	public void SprintTest56_2_55C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the item 
			//-------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-3 Select Delete from context menu
			//--------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Delete.Value); //Selects Delete from operations menu

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class
			String iconName = mfilesDialog.getIcon();			

			Log.message("3. Delete from operations menu is selected.");

			//Verification : To Verify if warning icon is displaying in MFiles dialog
			//------------------------------------------------------------------------
			if(iconName.equalsIgnoreCase("WARNING")) 
				Log.pass("Test Case Passed. 'Warning' icon is displayed in MFiles dialog on deleting object from Operations menu.", driver);
			else
				Log.fail("Test Case Failed. 'Warning' icon is not displayed in MFiles dialog on deleting object from Operations menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_2_55C

	/**
	 * 56.2.56 : Warning Icon should be displayed in checkout confirmation dialog after double clicking the document
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint56", "Icon","SKIP_JavaApplet"}, 
			description = " Warning Icon should be displayed in checkout confirmation dialog after double clicking the document.")
	public void SprintTest56_2_56(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Double click the item 
			//-------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.doubleClickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-3 Check if Checkout confirmation dialog has opened
			//--------------------------------------------------------
			if (!MFilesDialog.exists(driver))
				throw new Exception("Checkout prompt is not displayed. Please check Java applet/Chrome extension is enabled or not");

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			String iconName = mfilesDialog.getIcon();			

			Log.message("3. Checkout prompt is opened.");

			//Verification : To Verify if warning icon is displaying in MFiles dialog
			//------------------------------------------------------------------------
			if(iconName.equalsIgnoreCase("WARNING")) 
				Log.pass("Test Case Passed. 'Warning' icon is displayed in Checkout prompt on double clicking the document.", driver);
			else
				Log.fail("Test Case Failed. 'Warning' icon is not displayed in Checkout prompt on double clicking the document.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_2_56

	/**
	 * 56.2.57A : Warning Icon should be displayed in Undo-checkout confirmation dialog after performing undo-checkout through context menu.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint56", "Icon"}, 
			description = "Warning Icon should be displayed in Undo-checkout confirmation dialog after performing undo-checkout through context menu.")
	public void SprintTest56_2_57A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform checkout operation 
			//---------------------------------------------------------
			String objectName = ListView.getRandomObject(driver);

			if (!homePage.listView.isItemExists(objectName)) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + objectName + ") does not exists in the homePage.listView.");

			if (!homePage.listView.rightClickItem(objectName)) //Rightclicks the item
				throw new Exception("Object (" + objectName + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from context menu

			if (!ListView.isCheckedOutByItemName(driver, objectName))
				throw new Exception("Object (" + objectName + ") is not checked out.");

			Log.message("2. Object (" + objectName + ") is selected and checked out.");

			//Step-3 Perform undo-checkout operation through context menu
			//-----------------------------------------------------------
			if (!homePage.listView.rightClickItem(objectName)) //Rightclicks the item
				throw new Exception("Object (" + objectName + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.UndoCheckOut.Value); //Selects Checkout from context menu

			Log.message("3. Undo-Checkout is selected from context menu.", driver);

			//Step-4 : Check if confirmation prompt for undo-checkout operation is displayed.
			//-------------------------------------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.IsUndoCheckOutPromtDisplayed())
				throw new Exception("Confirmation prompt for undo-checkout prompt is not displayed.");

			String iconName = mfilesDialog.getIcon();			

			Log.message("4. Confirmation prompt for undo-checkout operation is opened.");

			//Verification : To Verify if warning icon is displaying in MFiles dialog
			//------------------------------------------------------------------------
			if(iconName.equalsIgnoreCase("WARNING")) 
				Log.pass("Test Case Passed. 'Warning' icon is displayed in undo-Checkout prompt on performing undo-checkout operation through context menu.", driver);
			else
				Log.fail("Test Case Failed. 'Warning' icon is not displayed in undo-Checkout prompt on performing undo-checkout operation through context menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_2_57A

	/**
	 * 56.2.57B : Warning Icon should be displayed in Undo-checkout confirmation dialog after performing undo-checkout through operations menu.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint56", "Icon"}, 
			description = "Warning Icon should be displayed in Undo-checkout confirmation dialog after performing undo-checkout through operations menu.")
	public void SprintTest56_2_57B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform checkout operation 
			//---------------------------------------------------------
			String objectName = ListView.getRandomObject(driver);

			if (!homePage.listView.isItemExists(objectName)) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + objectName + ") does not exists in the homePage.listView.");

			if (!homePage.listView.rightClickItem(objectName)) //Rightclicks the item
				throw new Exception("Object (" + objectName + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from context menu

			if (!ListView.isCheckedOutByItemName(driver, objectName))
				throw new Exception("Object (" + objectName + ") is not checked out.");

			Log.message("2. Object (" + objectName + ") is selected and checked out.");

			//Step-3 Perform undo-checkout operation through operations menu
			//-----------------------------------------------------------
			if (!homePage.listView.clickItem(objectName)) //Rightclicks the item
				throw new Exception("Object (" + objectName + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Checkout from context menu

			Log.message("3. Undo-Checkout is selected from operations menu.", driver);

			//Step-4 : Check if confirmation prompt for undo-checkout operation is displayed.
			//-------------------------------------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.IsUndoCheckOutPromtDisplayed())
				throw new Exception("Confirmation prompt for undo-checkout prompt is not displayed.");

			String iconName = mfilesDialog.getIcon();			

			Log.message("4. Confirmation prompt for undo-checkout operation is opened.");

			//Verification : To Verify if warning icon is displaying in MFiles dialog
			//------------------------------------------------------------------------
			if(iconName.equalsIgnoreCase("WARNING")) 
				Log.pass("Test Case Passed. 'Warning' icon is displayed in undo-Checkout prompt on performing undo-checkout operation through operations menu.", driver);
			else
				Log.fail("Test Case Failed. 'Warning' icon is not displayed in undo-Checkout prompt on performing undo-checkout operation through operations menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_2_57B

	/**
	 * 56.2.57C : Warning Icon should be displayed in Undo-checkout confirmation dialog after performing undo-checkout through taskpanel menu.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint56", "Icon"}, 
			description = "Warning Icon should be displayed in Undo-checkout confirmation dialog after performing undo-checkout through taskpanel menu.")
	public void SprintTest56_2_57C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform checkout operation 
			//---------------------------------------------------------
			String objectName = ListView.getRandomObject(driver);

			if (!homePage.listView.isItemExists(objectName)) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + objectName + ") does not exists in the homePage.listView.");

			if (!homePage.listView.rightClickItem(objectName)) //Rightclicks the item
				throw new Exception("Object (" + objectName + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from context menu

			if (!ListView.isCheckedOutByItemName(driver, objectName))
				throw new Exception("Object (" + objectName + ") is not checked out.");

			Log.message("2. Object (" + objectName + ") is selected and checked out.");

			//Step-3 Perform undo-checkout operation through taskpanel menu
			//-----------------------------------------------------------
			if (!homePage.listView.clickItem(objectName)) //Rightclicks the item
				throw new Exception("Object (" + objectName + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Checkout from taskpanel menu

			Log.message("3. Undo-Checkout is selected from taskpanel menu.", driver);

			//Step-4 : Check if confirmation prompt for undo-checkout operation is displayed.
			//-------------------------------------------------------------------------------
			if (!MFilesDialog.exists(driver))
				throw new Exception("Confirmation prompt for undo-checkout prompt is not displayed.");

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.IsUndoCheckOutPromtDisplayed())
				throw new Exception("Confirmation prompt for undo-checkout prompt is not displayed.");

			String iconName = mfilesDialog.getIcon();			

			Log.message("4. Confirmation prompt for undo-checkout operation is opened.");

			//Verification : To Verify if warning icon is displaying in MFiles dialog
			//------------------------------------------------------------------------
			if(iconName.equalsIgnoreCase("WARNING")) 
				Log.pass("Test Case Passed. 'Warning' icon is displayed in undo-Checkout prompt on performing undo-checkout taskpanel through operations menu.", driver);
			else
				Log.fail("Test Case Failed. 'Warning' icon is not displayed in undo-Checkout prompt on performing undo-checkout operation through taskpanel menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_2_57C


	/**
	 * 56.2.59A :  Warning Icon should be displayed in Undo-checkout confirmation dialog after performing undo-checkout through context menu by other user
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint56", "Icon"}, 
			description = "Warning Icon should be displayed in Undo-checkout confirmation dialog after performing undo-checkout through context menu by other user.")
	public void SprintTest56_2_59A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform checkout operation 
			//---------------------------------------------------------
			String objectName = ListView.getRandomObject(driver);

			if (!homePage.listView.isItemExists(objectName)) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + objectName + ") does not exists in the homePage.listView.");

			if (!homePage.listView.rightClickItem(objectName)) //Rightclicks the item
				throw new Exception("Object (" + objectName + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from context menu

			if (!ListView.isCheckedOutByItemName(driver, objectName))
				throw new Exception("Object (" + objectName + ") is not checked out.");

			Log.message("2. Object (" + objectName + ") is selected and checked out.");

			//Step-3 Logout and login with new user and navigate to the view
			//--------------------------------------------------------------
			if (!Utility.logOut(driver)) //Logs out from web access default page
				throw new Exception("Log out is not successful.");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), testVault); //Logs in with other user
			viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord")); //Navigates to the specified view

			Log.message("3. Logged out and logged into web access with user '" + dataPool.get("UserName") + "'; and navigated to the view '" + viewToNavigate + "'");

			//Step-4 Perform undo-checkout operation through context menu
			//-----------------------------------------------------------
			if (!homePage.listView.rightClickItem(objectName)) //Rightclicks the item
				throw new Exception("Object (" + objectName + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.UndoCheckOut.Value); //Selects Checkout from context menu

			Log.message("4. Undo-Checkout is selected from context menu.", driver);

			//Step-5 : Check if confirmation prompt for undo-checkout operation is displayed.
			//-------------------------------------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.IsUndoCheckOutPromtDisplayed())
				throw new Exception("Confirmation prompt for undo-checkout prompt is not displayed.");

			String iconName = mfilesDialog.getIcon();			

			Log.message("5. Confirmation prompt for undo-checkout operation is opened.");

			//Verification : To Verify if warning icon is displaying in MFiles dialog
			//------------------------------------------------------------------------
			if(iconName.equalsIgnoreCase("WARNING")) 
				Log.pass("Test Case Passed. 'Warning' icon is displayed in undo-Checkout prompt on performing undo-checkout operation through context menu by the user " + dataPool.get("UserName"), driver);
			else
				Log.fail("Test Case Failed. 'Warning' icon is not displayed in undo-Checkout prompt on performing undo-checkout operation through context menu by the user " + dataPool.get("UserName"), driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_2_59A

	/**
	 * 56.2.59B :  Warning Icon should be displayed in Undo-checkout confirmation dialog after performing undo-checkout through operations menu by other user
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint56", "Icon"}, 
			description = "Warning Icon should be displayed in Undo-checkout confirmation dialog after performing undo-checkout through operations menu by other user.")
	public void SprintTest56_2_59B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform checkout operation 
			//---------------------------------------------------------
			String objectName = ListView.getRandomObject(driver);

			if (!homePage.listView.isItemExists(objectName)) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + objectName + ") does not exists in the homePage.listView.");

			if (!homePage.listView.rightClickItem(objectName)) //Rightclicks the item
				throw new Exception("Object (" + objectName + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from context menu

			if (!ListView.isCheckedOutByItemName(driver, objectName))
				throw new Exception("Object (" + objectName + ") is not checked out.");

			Log.message("2. Object (" + objectName + ") is selected and checked out.");

			//Step-3 Logout and login with new user and navigate to the view
			//--------------------------------------------------------------
			if (!Utility.logOut(driver)) //Logs out from web access default page
				throw new Exception("Log out is not successful.");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), testVault); //Logs in with other user
			viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord")); //Navigates to the specified view

			Log.message("3. Logged out and logged into web access with user '" + dataPool.get("UserName") + "'; and navigated to the view '" + viewToNavigate + "'");

			//Step-4 Perform undo-checkout operation through operations menu
			//-----------------------------------------------------------
			if (!homePage.listView.clickItem(objectName)) //Rightclicks the item
				throw new Exception("Object (" + objectName + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Checkout from context menu

			Log.message("4. Undo-Checkout is selected from operations menu.", driver);

			//Step-5 : Check if confirmation prompt for undo-checkout operation is displayed.
			//-------------------------------------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.IsUndoCheckOutPromtDisplayed())
				throw new Exception("Confirmation prompt for undo-checkout prompt is not displayed.");

			String iconName = mfilesDialog.getIcon();			

			Log.message("5. Confirmation prompt for undo-checkout operation is opened.");

			//Verification : To Verify if warning icon is displaying in MFiles dialog
			//------------------------------------------------------------------------
			if(iconName.equalsIgnoreCase("WARNING")) 
				Log.pass("Test Case Passed. 'Warning' icon is displayed in undo-Checkout prompt on performing undo-checkout operation through operations menu by the user " + dataPool.get("UserName"), driver);
			else
				Log.fail("Test Case Failed. 'Warning' icon is not displayed in undo-Checkout prompt on performing undo-checkout operation through operations menu by the user " + dataPool.get("UserName"), driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_2_59B

	/**
	 * 56.2.59C :  Warning Icon should be displayed in Undo-checkout confirmation dialog after performing undo-checkout through taskpanel menu by other user
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint56", "Icon"}, 
			description = "Warning Icon should be displayed in Undo-checkout confirmation dialog after performing undo-checkout through taskpanel menu by other user.")
	public void SprintTest56_2_59C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform checkout operation 
			//---------------------------------------------------------
			String objectName = ListView.getRandomObject(driver);

			if (!homePage.listView.isItemExists(objectName)) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + objectName + ") does not exists in the homePage.listView.");

			if (!homePage.listView.rightClickItem(objectName)) //Rightclicks the item
				throw new Exception("Object (" + objectName + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.CheckOut.Value); //Selects Checkout from context menu

			if (!ListView.isCheckedOutByItemName(driver, objectName))
				throw new Exception("Object (" + objectName + ") is not checked out.");

			Log.message("2. Object (" + objectName + ") is selected and checked out.");

			//Step-3 Logout and login with new user and navigate to the view
			//--------------------------------------------------------------
			if (!Utility.logOut(driver)) //Logs out from web access default page
				throw new Exception("Log out is not successful.");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), testVault); //Logs in with other user
			viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord")); //Navigates to the specified view

			Log.message("3. Logged out and logged into web access with user '" + dataPool.get("UserName") + "'; and navigated to the view '" + viewToNavigate + "'");

			//Step-4 Perform undo-checkout operation through taskpanel menu
			//-----------------------------------------------------------
			if (!homePage.listView.clickItem(objectName)) //Rightclicks the item
				throw new Exception("Object (" + objectName + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Checkout from taskpanel menu

			Log.message("4. Undo-Checkout is selected from taskpanel menu.", driver);

			//Step-5 : Check if confirmation prompt for undo-checkout operation is displayed.
			//-------------------------------------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.IsUndoCheckOutPromtDisplayed())
				throw new Exception("Confirmation prompt for undo-checkout prompt is not displayed.");

			String iconName = mfilesDialog.getIcon();			

			Log.message("5. Confirmation prompt for undo-checkout operation is opened.");

			//Verification : To Verify if warning icon is displaying in MFiles dialog
			//------------------------------------------------------------------------
			if(iconName.equalsIgnoreCase("WARNING")) 
				Log.pass("Test Case Passed. 'Warning' icon is displayed in undo-Checkout prompt on performing undo-checkout operation through taskpanel menu by the user " + dataPool.get("UserName"), driver);
			else
				Log.fail("Test Case Failed. 'Warning' icon is not displayed in undo-Checkout prompt on performing undo-checkout operation through taskpanel menu by the user " + dataPool.get("UserName"), driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_2_59C

	/**
	 * 56.2.60A : Warning Icon should be displayed on renaming the object through context menu that has automatic generated name
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint56", "Icon"}, 
			description = "Warning Icon should be displayed on renaming the object through context menu that has automatic generated name.")
	public void SprintTest56_2_60A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform Rename operation 
			//---------------------------------------------------------
			String objectName = dataPool.get("ObjectName");

			if (!homePage.listView.isItemExists(objectName)) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + objectName + ") does not exists in the homePage.listView.");

			if (!homePage.listView.rightClickItem(objectName)) //Rightclicks the item
				throw new Exception("Object (" + objectName + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Rename.Value); //Selects Checkout from context menu

			Log.message("2. Rename is selected for object (" + objectName + ") from context menu.");

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isRenameDialogOpened()) //Checks if rename dialog is opened
				throw new Exception("Rename dialog is not opened.");

			//Step-3 Enter new name and click Ok button
			//--------------------------------------------
			mfilesDialog.rename("Test"); //Enters new name

			mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class
			String iconName = mfilesDialog.getIcon();			

			Log.message("3. New name is entered and ok button is clicked.", driver);

			//Verification : To Verify if warning icon is displaying in MFiles dialog
			//------------------------------------------------------------------------
			if(iconName.equalsIgnoreCase("WARNING")) 
				Log.pass("Test Case Passed. 'Warning' icon is displayed on renaming object that has automatic names through context menu.", driver);
			else
				Log.fail("Test Case Failed. 'Warning' icon is not displayed on renaming object that has automatic names through context menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_2_60A

	/**
	 * 56.2.60B : Warning Icon should be displayed on renaming the object through operations menu that has automatic generated name
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint56", "Icon"}, 
			description = "Warning Icon should be displayed on renaming the object through operations menu that has automatic generated name.")
	public void SprintTest56_2_60B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform Rename operation 
			//---------------------------------------------------------
			String objectName = dataPool.get("ObjectName");

			if (!homePage.listView.isItemExists(objectName)) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + objectName + ") does not exists in the homePage.listView.");

			if (!homePage.listView.rightClickItem(objectName)) //Rightclicks the item
				throw new Exception("Object (" + objectName + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Rename.Value); //Selects Checkout from operations menu

			Log.message("2. Rename is selected for object (" + objectName + ") from operations menu.");

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isRenameDialogOpened()) //Checks if rename dialog is opened
				throw new Exception("Rename dialog is not opened.");

			//Step-3 Enter new name and click Ok button
			//--------------------------------------------
			mfilesDialog.rename("Test"); //Enters new name

			mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class
			String iconName = mfilesDialog.getIcon();			

			Log.message("3. New name is entered and ok button is clicked.", driver);

			//Verification : To Verify if warning icon is displaying in MFiles dialog
			//------------------------------------------------------------------------
			if(iconName.equalsIgnoreCase("WARNING")) 
				Log.pass("Test Case Passed. 'Warning' icon is displayed on renaming object that has automatic names through operations menu.", driver);
			else
				Log.fail("Test Case Failed. 'Warning' icon is not displayed on renaming object that has automatic names through operations menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_2_60B

	/**
	 * 56.2.60C : Warning Icon should be displayed on renaming the object usng F2 key that has automatic generated name
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint56", "Icon"}, 
			description = "Warning Icon should be displayed on renaming the object usng F2 key that has automatic generated name.")
	public void SprintTest56_2_60C(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("Safari") || driverType.equalsIgnoreCase("IE"))
			throw new SkipException(driverType.toUpperCase() +" does not support key actions");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform Rename operation 
			//---------------------------------------------------------
			String objectName = dataPool.get("ObjectName");

			if (!homePage.listView.clickItem(objectName)) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + objectName + ") does not exists in the homePage.listView.");

			ActionEventUtils.pressKey(driver,Keys.F2);//Press the F2 key

			Log.message("2. F2 key is pressed for the selected object (" + objectName + ").");

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isRenameDialogOpened()) //Checks if rename dialog is opened
				throw new Exception("Rename dialog is not opened.");

			//Step-3 Enter new name and click Ok button
			//--------------------------------------------
			mfilesDialog.rename("Test"); //Enters new name

			mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class
			String iconName = mfilesDialog.getIcon();			

			Log.message("3. New name is entered and ok button is clicked.", driver);

			//Verification : To Verify if warning icon is displaying in MFiles dialog
			//------------------------------------------------------------------------
			if(iconName.equalsIgnoreCase("WARNING")) 
				Log.pass("Test Case Passed. 'Warning' icon is displayed on renaming object that has automatic name using F2 key.", driver);
			else
				Log.fail("Test Case Failed. 'Warning' icon is not displayed on renaming object that has automatic name using F2 key.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_2_60C

} //End Class IconsAndMessages
package MFClient.Tests.SharingPublicLinks;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
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
import MFClient.Pages.SharedLinkPage;
import MFClient.Wrappers.Caption;
import MFClient.Wrappers.ListView;
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class SharingFiles {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String configURL = null;
	public static String userName = null;
	public static String password = null;
	public static String testVault = null;
	public static String productVersion = null;
	public static WebDriver driver = null;
	public static String userFullName = null;
	public static String driverType = null;

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
			driverType = xmlParameters.getParameter("driverType");
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
	 * Sharing_36104 : Downloading after file is deleted (context menu)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"", "PublicSharing"}, 
			description = "Downloading after file is deleted (context menu)")
	public void Sharing_36104(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = WebDriverUtils.getDriver();
		WebDriver driver2 = null;

		try {


			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Set the additional condition search
			//--------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), dataPool.get("PropValue"));
			homePage.searchPanel.clickSearch();//Click the search button

			Log.message("2. Performed the advanced search for the single file document.");
			//Step-3 : Select the document and click share links from context menu
			//--------------------------------------------------------------------
			String documentName = ListView.getRandomObject(driver);

			if (!homePage.listView.rightClickItem(documentName))
				throw new Exception("Document (" + documentName + ") is not selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.SharePublicLink.Value);
			MFilesDialog mfilesDialog = MFilesDialog.isSharePublicLinkDlgOpened(driver);

			Log.message("3. Share public Links dialog is opened from context menu.");

			//Step-3 : Copy public link from public links dialog
			//--------------------------------------------------
			mfilesDialog.clickSharePublicLinkBtn(); //Clicks Share public link button
			String publicLink = mfilesDialog.getPublicLink();
			mfilesDialog.close(); //Closes MFiles dialog

			Log.message("4. Link is copied from Share Public Link dialog.");

			//Step-4 : Open public link in new browser window
			//----------------------------------------------
			driver2 = Utility.openSharedLinkPage(publicLink);

			Log.message("5. Public link URL is opened : '" + publicLink + "'");

			//Step-5 : Verify if public link has shared object
			//------------------------------------------------------
			SharedLinkPage sharedLinkPage = new SharedLinkPage(driver2);

			if (!sharedLinkPage.getSharedDocumentName().equals(documentName))
				throw new Exception("Document ( " + documentName + ") is not avaible in the shared link page.");

			Log.message("6. Shared link page has document (" + documentName + ") visible");

			//Step-6 : Delete the file from the list
			//--------------------------------------
			if (!homePage.deleteObjectInView(documentName))
				throw new Exception("Document (" + documentName + ") is not deleted.");

			Log.message("7. Document (" + documentName + ") is deleted from the list");

			//Step-7 : Open the shared link after deleting shared document
			//------------------------------------------------------------
			driver2 = Utility.openSharedLinkPage(publicLink,  driver2);
			sharedLinkPage = new SharedLinkPage(driver2);

			Log.message("8. Public link URL is opened again after deleting document.");

			//Verification : To verify if Link is expired or invalid is displayed
			//--------------------------------------------------------------------
			if (sharedLinkPage.getSharedDocumentName().equals(Caption.MFilesDialog.ExpiredLink.Value))
				Log.pass("Test case Passed. Document is shared with public link created from context menu.", driver);
			else
				Log.fail("Test case failed. Document is not shared with public link created from operations menu.", driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			Utility.quitDriver(driver2);
			Utility.quitDriver(driver);
		} //End Finally

	}	//End Sharing_36104

	/**
	 * Sharing_36105 : Downloading after file is deleted (operations menu)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"", "PublicSharing"}, 
			description = "Downloading after file is deleted (operations menu)")
	public void Sharing_36105(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = WebDriverUtils.getDriver();
		WebDriver driver2 = null;

		try {


			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Set the additional condition search
			//--------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), dataPool.get("PropValue"));
			homePage.searchPanel.clickSearch();//Click the search button

			Log.message("2. Performed the advanced search for the single file document.");

			//Step-3 : Select the document and click share links from operations menu
			//--------------------------------------------------------------------
			String documentName = ListView.getRandomObject(driver);

			if (!homePage.listView.clickItem(documentName))
				throw new Exception("Document (" + documentName + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.SharePublicLink.Value);
			MFilesDialog mfilesDialog = MFilesDialog.isSharePublicLinkDlgOpened(driver);

			Log.message("3. Share public Links dialog is opened from operations menu.");

			//Step-4 : Copy public link from public links dialog
			//--------------------------------------------------
			mfilesDialog.clickSharePublicLinkBtn(); //Clicks Share public link button
			String publicLink = mfilesDialog.getPublicLink();
			mfilesDialog.close(); //Closes MFiles dialog

			Log.message("4. Link is copied from Share Public Link dialog.");

			//Step-5 : Open public link in new browser window
			//----------------------------------------------
			driver2 = Utility.openSharedLinkPage(publicLink);

			Log.message("5. Public link URL is opened : '" + publicLink + "'");

			//Step-6 : Verify if public link has shared object
			//------------------------------------------------------
			SharedLinkPage sharedLinkPage = new SharedLinkPage(driver2);

			if (!sharedLinkPage.getSharedDocumentName().equals(documentName))
				throw new Exception("Document ( " + documentName + ") is not avaible in the shared link page.");

			Log.message("6. Shared link page has document (" + documentName + ") visible");

			//Step-7 : Delete the file from the list
			//--------------------------------------
			if (!homePage.deleteObjectInView(documentName))
				throw new Exception("Document (" + documentName + ") is not deleted.");

			Log.message("7. Document (" + documentName + ") is deleted from the list");

			//Step-8 : Open the shared link after deleting shared document
			//------------------------------------------------------------
			driver2 = Utility.openSharedLinkPage(publicLink,  driver2);
			sharedLinkPage = new SharedLinkPage(driver2);

			Log.message("8. Public link URL is opened again after deleting document.");

			//Verification : To verify if Link is expired or invalid is displayed
			//--------------------------------------------------------------------
			if (sharedLinkPage.getSharedDocumentName().equals(Caption.MFilesDialog.ExpiredLink.Value))
				Log.pass("Test case Passed. Document is shared with public link created from context menu.", driver);
			else
				Log.fail("Test case failed. Document is not shared with public link created from operations menu.", driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver2);
			Utility.quitDriver(driver);
		} //End Finally

	}	//End Sharing_36105

	/**
	 * Sharing_36106 : Share Public Link option in context menu for multiple document objects is not enabled
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "PublicSharing"}, 
			description = "Share Public Link option in context menu for multiple document objects is not enabled")
	public void Sharing_36106(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = WebDriverUtils.getDriver();

		try {


			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Set the additional condition search
			//--------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), dataPool.get("PropValue"));
			homePage.searchPanel.clickSearch();//Click the search button

			Log.message("2. Performed the advanced search for the single file document.");

			//Step-3 : Select the document and click share links from context menu
			//--------------------------------------------------------------------
			homePage.listView.shiftclickMultipleItemsByIndex(0, Utility.getRandomNumber(1, homePage.listView.itemCount())); //
			homePage.listView.rightClickItemByIndex(0); //Right clicks by index

			Log.message("3. Multiple document objects in the list are selected by rightclicking.");

			//Verification : To verify if Share Public Link is enabled in context menu
			//------------------------------------------------------------------------
			if (!homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.SharePublicLink.Value))
				Log.pass("Test case Passed. " + Caption.MenuItems.SharePublicLink.Value + " is disabled in context menu for multi-selected document object.", driver);
			else
				Log.fail("Test case failed. " + Caption.MenuItems.SharePublicLink.Value + " is not disabled in context menu for multi-selected document object", driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End Finally

	}	//End Sharing_36106

	/**
	 * Sharing_36107 : Share Public Link option in operations menu for multiple document objects is not enabled
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "PublicSharing"}, 
			description = "Share Public Link option in operations menu for multiple document objects is not enabled")
	public void Sharing_36107(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = WebDriverUtils.getDriver();

		try {


			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Set the additional condition search
			//--------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), dataPool.get("PropValue"));
			homePage.searchPanel.clickSearch();//Click the search button

			Log.message("2. Performed the advanced search for the single file document.");

			//Step-3 : Select the document and click share links from context menu
			//--------------------------------------------------------------------
			homePage.listView.shiftclickMultipleItemsByIndex(0, Utility.getRandomNumber(1, homePage.listView.itemCount() - 1)); //

			Log.message("3. Multiple document objects in the list are selected.");

			//Verification : To verify if Share Public Link is enabled in operations menu
			//----------------------------------------------------------------------------
			if (!homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.SharePublicLink.Value))
				Log.pass("Test case Passed. " + Caption.MenuItems.SharePublicLink.Value + " is disabled in operations menu for multi-selected document object.", driver);
			else
				Log.fail("Test case failed. " + Caption.MenuItems.SharePublicLink.Value + " is not disabled in if Share Public Link is enabled in operations menu menu for multi-selected document object", driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End Finally

	}	//End Sharing_36107


	/**
	 * Sharing_134624 : Selected file name should be displayed with full name as expected in the stop sharing dialog.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"PublicSharing"}, 
			description = "Selected file name should be displayed with full name as expected in the stop sharing dialog.")
	public void Sharing_134624(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = WebDriverUtils.getDriver();

		try {


			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);//Launch the MFWA with valid credentials

			//Step-1 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));//Navigate to the search view

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Set the additional condition search
			//--------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), dataPool.get("PropValue"));//Set the additional conditions in search area
			homePage.searchPanel.clickSearch();//Click the search button

			Log.message("2. Set the advance additional search for the single file document.");

			//Step-3 : Select the document and click share links from context menu
			//--------------------------------------------------------------------
			String docName = ListView.getRandomObject(driver);//Get the random object in the list view
			homePage.listView.clickItem(docName);//Selected the random object
			
			String extension = "";

			if(docName.contains("."))
			{
				extension = "." + docName.split("\\.")[1];
				docName = docName.split("\\.")[0];
			}

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Rename.Value);//Rename the selected object
			MFilesDialog mfilesdialog = new MFilesDialog(driver,"Rename");//Instantiate the M-files dialog
			mfilesdialog.rename(docName.concat("??"), true);//Rename the selected object

			Log.message("3. Selected object : " + docName + " and renamed the object as : " + docName.concat("??") );

			//Step-4 : Right click the renamed object
			//---------------------------------------
			homePage.listView.rightClickItem(docName.concat("??")+extension);//Right click the object in list view

			Log.message("4. Right clicked the renamed object : " + docName.concat("??"), driver);

			//Step-5 : Select the share public link option from the context menu & Click the create public link 
			//-------------------------------------------------------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.SharePublicLink.Value);
			mfilesdialog = new MFilesDialog(driver,"Share Public Link");//open the share public link option in the M-files dialog
			mfilesdialog.clickCreatePublicLink();//Create the public link in share link dialog
			mfilesdialog.clickCloseButton();//Close the M-files dialog

			Log.message("5. Clicked the create public link button in Share by me dialog.", driver);

			//Step-6 : Select the 'Shared files' option from the Menu bar
			//-----------------------------------------------------------
			homePage.menuBar.ClickUserInfo(Caption.MenuItems.SharedFilesAllUsers.Value);//Select the 

			Log.message("6. Selected the 'Shared files(All users)' option from the menu bar.", driver);

			//Step-7 : Select the Shared files option in menu bar
			//---------------------------------------------------
			mfilesdialog = new MFilesDialog(driver,"Shared Files (All Users)");//open the share public link option in the M-files dialog
			mfilesdialog.clickStopSharing(docName.concat("??")+extension);//Select the stop sharing for the selected object

			Log.message("7. Selected the stop sharing for object : " + docName.concat("??"), driver);

			//Verification : Verify if selected object full name is displayed
			//----------------------------------------------------------------
			mfilesdialog = new MFilesDialog(driver,"Stop sharing");//Instantiate the Stop sharing dialog
			if(mfilesdialog.getMessage().contains(docName.concat("??")+extension))//Verify if object full name is displayed in the stop sharing dialog
				Log.pass("Test case Passed.Selected file name is displayed with full name in Stop sharing dialog as expected successfully.", driver);
			else
				Log.fail("Test case failed.Selected file name is not displayed with full name in Stop sharing dialog.", driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	}//End Sharing_134624

} //End SharingFiles
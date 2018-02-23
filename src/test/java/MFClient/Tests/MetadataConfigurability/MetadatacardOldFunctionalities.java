package MFClient.Tests.MetadataConfigurability;

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
import MFClient.Wrappers.ListView;
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;


@Listeners(EmailReport.class)

public class MetadatacardOldFunctionalities {

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
	 * 1.1.1.1A : Convert Single document to Multi File Document(MFD) using Context Menu.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability"}, 
			description = "Convert Single document to Multi File Document(MFD) using Context Menu.")
	public void TC_1_1_1_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform check out operation
			//----------------------------------------------------------
			if(!homePage.listView.isSFDBasedOnObjectIcon(dataPool.get("ObjectName")))
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not Single file document.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel


			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if it is in checked in state
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-3 : Select Convert to MFD from context menu
			//---------------------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.ConvertToMFD_C.Value); //Clicks Convert SFD to MFD option from operations menu


			String mfdName = dataPool.get("ObjectName").split("\\.")[0];

			ListView refreshedListView = homePage.listView.clickRefresh();//Refresh the listing view

			Log.message("3. Convert SFD to MFD option is selected from context menu for SFD (" + dataPool.get("ObjectName") + ").");

			//Verification : To Verify if SFD object is converted to MFD object
			//-------------------------------------------------------------------			
			if (!refreshedListView.isSFDBasedOnObjectIcon(mfdName))
				Log.pass("Test case Passed. SFD is converted to MFD (" + mfdName + ") through context menu.");
			else
				Log.fail("Test case Failed. SFD is not converted to MFD through context menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_1_1_1_1A

	/**
	 * 1.1.1.1B : Convert Single document to Multi File Document(MFD) using Operation Menu.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MFN119MetadataConfigurability"}, 
			description = "Convert Single document to Multi File Document(MFD) using Operation Menu.")
	public void TC_1_1_1_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (!homePage.listView.isSFDBasedOnObjectIcon(dataPool.get("ObjectName"))) //Checks if this is SFD
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not Single file document.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel


			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if it is in checked in state
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-3 : Select Convert to MFD from operation menu
			//---------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value); //Clicks Convert SFD to MFD option from operations menu


			String mfdName = dataPool.get("ObjectName").split("\\.")[0];

			ListView refreshedListView = homePage.listView.clickRefresh();//Refresh the listing view

			Log.message("3. Convert SFD to MFD option is selected from context menu for SFD (" + dataPool.get("ObjectName") + ").");

			//Verification : To Verify if SFD object is converted to MFD object
			//-------------------------------------------------------------------			
			if (!refreshedListView.isSFDBasedOnObjectIcon(mfdName))
				Log.pass("Test case Passed. SFD is converted to MFD (" + mfdName + ") through context menu.");
			else
				Log.fail("Test case Failed. SFD is not converted to MFD through context menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_1_1_1_1B


	/**
	 * 1.1.3A : Modify properties and save the changes in right pane metadatacard of multiple documents
	 *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "M6450M6450MetadataConfigurability"}, 
			description = "Modify properties and save the changes in right pane metadatacard of multiple documents")
	public void TC_1_1_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//2. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("2. Multiple Objects is selected in the view");

			//Step-3 : Modify the metadata card
			//---------------------------------
			MetadataCard metadatacard = new MetadataCard(driver, true);//Instantiate the metadata card in right pane
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("PropertyValue"));//Modify the value in metadata card
			metadatacard.clickOKBtn(driver);//Clicks save button in the metadatacard

			Log.message("4. Object is modified in the metadatacard and save button is clicked");
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_1_1_3A
	  */	

	/**
	 * 34196.1A : Trying to edit when user has read only license in Right pane metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"M6450M6450MetadataConfigurability"}, 
			description = "Trying to edit when user has read only license in Right pane metadatacard")
	public void TC_34196_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();

			//Step- 1: Login to homepage
			//-------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), xmlParameters.getParameter("VaultName")); //Launched driver and logged in

			Log.message("1. Logged into Default Webpage as a Read only user");

			//Step- 2 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-3 : Select the object in the view
			//----------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not selected in the homePage.listView.");

			Log.message("3. Object is selected in the view");

			//Step-4 : Click the property in the metadata card
			//------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver, true);//Instantiate the metadata card in right pane
			metadatacard.clickProperty(dataPool.get("Property")); //Clicks the property in the metadatacard


			Log.message("4. Property is clicked in the metadatacard");

			//Verification : Verify if metadata card is editable for read only user in right pane
			//-------------------------------------------------------------------------------------
			if (!metadatacard.isEditMode())
				Log.pass("Test case Passed. Metadata Card is not editable in right pane metadatacard for read only user", driver);
			else
				Log.fail("Test case Failed. Metadata Card is editable in right pane metadatacard for read only user", driver);

		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_34196_1A


	/**
	 * 34196.1B : Trying to edit when user has read only license in popout metadatacard via Conext menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"M6450M6450MetadataConfigurability"}, 
			description = "Trying to edit when user has read only license in popout metadatacard via Conext menu")
	public void TC_34196_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();

			//Step- 1: Login to homepage
			//-------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), xmlParameters.getParameter("VaultName")); //Launched driver and logged in

			Log.message("1. Logged into Default Webpage as a Read only user");

			//Step- 2 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-3 : Open the popout metadata card of the object using context menu
			//----------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");


			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Right clicks the Object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the metadata card

			Log.message("3. Object is selected and pop out metadatacard of the object is opened via context menu");

			//Step-4 : Modify the metadata card
			//---------------------------------
			metadatacard.clickProperty(dataPool.get("Property")); //Clicks the property in the metadatacard


			Log.message("4. Property is clicked in the metadatacard");

			//Verification : Verify if metadata card is editable for read only user in popout metadatacard via Conext menu
			//-------------------------------------------------------------------------------------
			if (!metadatacard.isEditMode())
				Log.pass("Test case Passed. Metadata Card is not editable in popout metadatacard via Conext menu for read only user", driver);
			else
				Log.fail("Test case Failed. Metadata Card is editable in popout metadatacard via Conext menu for read only user", driver);

		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_34196_1B

	/**
	 * 34196.1C : Trying to edit when user has read only license in popout metadatacard via Operation menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"M6450M6450MetadataConfigurability"}, 
			description = "Trying to edit when user has read only license in popout metadatacard via Operation menu")
	public void TC_34196_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();

			//Step- 1: Login to homepage
			//-------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), xmlParameters.getParameter("VaultName")); //Launched driver and logged in

			Log.message("1. Logged into Default Webpage as a Read only user");

			//Step- 2 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-3 : Open the popout metadata card of the object using operation menu
			//----------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Clicks properties option via operation menu


			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the metadata card

			Log.message("3. Object is selected and pop out metadatacard of the object is opened via operation menu");

			//Step-4 : Modify the metadata card
			//---------------------------------
			metadatacard.clickProperty(dataPool.get("Property")); //Clicks the property in the metadatacard


			Log.message("4. Property is clicked in the metadatacard");

			//Verification : Verify if metadata card is editable for read only user popout metadatacard via Operation menu
			//-------------------------------------------------------------------------------------
			if (!metadatacard.isEditMode())
				Log.pass("Test case Passed. Metadata Card is not editable in popout metadatacard via Operation menu for read only user", driver);
			else
				Log.fail("Test case Failed. Metadata Card is editable in popout metadatacard via Operation menu for read only user", driver);

		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_34196_1C

	/**
	 * 34196.1D : Trying to edit when user has read only license in popout metadatacard via Task pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"M6450MetadataConfigurability"}, 
			description = "Trying to edit when user has read only license in popout metadatacard via Operation menu")
	public void TC_34196_1D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();

			//Step- 1: Login to homepage
			//-------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), xmlParameters.getParameter("VaultName")); //Launched driver and logged in

			Log.message("1. Logged into Default Webpage as a Read only user");

			//Step- 2 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-3 : Open the popout metadata card of the object using context menu
			//----------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Clicks properties option via task pane


			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the metadata card

			Log.message("3. Object is selected and pop out metadatacard of the object is opened via operation menu");

			//Step-4 : Modify the metadata card
			//---------------------------------
			metadatacard.clickProperty(dataPool.get("Property")); //Clicks the property in the metadatacard


			Log.message("4. Property is clicked in the metadatacard");

			//Verification : Verify if metadata card is editable for read only user in popout metadatacard via Task pane
			//-------------------------------------------------------------------------------------
			if (!metadatacard.isEditMode())
				Log.pass("Test case Passed. Metadata Card is not editable in popout metadatacard via Task pane for read only user", driver);
			else
				Log.fail("Test case Failed. Metadata Card is editable in popout metadatacard via Task pane for read only user", driver);

		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_34196_1D


	/**
	 * 34196.2A : Trying to edit when user has read only license in Right pane metadatacard for MultiSelected Objects
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "M6450MetadataConfigurability"}, 
			description = "Trying to edit when user has read only license in Right pane metadatacard for MultiSelected Objects")
	public void TC_34196_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();

			//Step- 1: Login to homepage
			//-------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), xmlParameters.getParameter("VaultName")); //Launched driver and logged in

			Log.message("1. Logged into Default Webpage as a Read only user");

			//Step- 2 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Multiple Objects is selected in the view");

			//Step-4 : Modify the metadata card
			//---------------------------------
			MetadataCard metadatacard = new MetadataCard(driver, true);//Instantiate the metadata card in right pane
			metadatacard.setPermission(dataPool.get("Permission"));//Modify the permission value in metadata card
			metadatacard.clickOKBtn(driver);//Clicks save button in the metadatacard
			Utils.fluentWait(driver);

			Log.message("4. Object is modified in the metadatacard and save button is clicked");

			if (!MFilesDialog.exists(driver))
				throw new Exception("Test case Failed. Expected warning dialog is not displayed");

			MFilesDialog MFDialog= new MFilesDialog(driver);

			String warningMessage=MFDialog.getMessage();//Gets the warning message from the warning dialog

			//Verification : Verify if read only user able to edit the metadata card in right pane
			//-------------------------------------------------------------------------------------
			if (warningMessage.equalsIgnoreCase(dataPool.get("ExpectedMessage")))
				Log.pass("Test case Passed. User unable to save the changes in right pane metadatacard for MultiSelected Objects", driver);
			else
				Log.fail("Test case Failed. Expected warning message is not displayed", driver);

		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_34196_2A


	/**
	 * 34196.2B : Trying to edit when user has read only license in popout metadatacard via Conext menu for MultiSelected Objects
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "M6450MetadataConfigurability"}, 
			description = "Trying to edit when user has read only license in popout metadatacard via Conext menu for MultiSelected Objects")
	public void TC_34196_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();

			//Step- 1: Login to homepage
			//-------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), xmlParameters.getParameter("VaultName")); //Launched driver and logged in

			Log.message("1. Logged into Default Webpage as a Read only user");

			//Step- 2 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Multiple Objects is selected in the view");

			//4. Right click the last object and Click the Properties option in the context menu
			//------------------------------------------------------------------------------------
			homePage.listView.rightClickItem(dataPool.get("Objects").split("\n")[dataPool.get("Objects").split("\n").length-1]);


			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value);


			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the metadata card

			Log.message("4. Pop out metadatacard of the object is opened via context menu");

			//Step-4 : Modify the metadata card
			//---------------------------------
			metadatacard.setPermission(dataPool.get("Permission"));//Modify the permission value in metadata card
			metadatacard.clickOKBtn(driver);//Clicks save button in the metadatacard


			Log.message("5. Object is modified in the metadatacard and save button is clicked");

			if (!MFilesDialog.exists(driver))
				throw new Exception("Test case Failed. Expected warning dialog is not displayed");

			MFilesDialog MFDialog= new MFilesDialog(driver);

			String warningMessage=MFDialog.getMessage();//Gets the warning message from the warning dialog

			//Verification : Verify if read only user able to edit the metadata card in popout metadatacard via context menu
			//-------------------------------------------------------------------------------------
			if (warningMessage.equalsIgnoreCase(dataPool.get("ExpectedMessage")))
				Log.pass("Test case Passed. User unable to save the changes in popout metadatacard via context menu for MultiSelected Objects", driver);
			else
				Log.fail("Test case Failed. Expected warning message is not displayed", driver);

		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_34196_2B

	/**
	 * 34196.2C : Trying to edit when user has read only license in popout metadatacard via Operation menu for MultiSelected Objects
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "M6450MetadataConfigurability"}, 
			description = "Trying to edit when user has read only license in popout metadatacard via Operation menu for MultiSelected Objects")
	public void TC_34196_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();

			//Step- 1: Login to homepage
			//-------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), xmlParameters.getParameter("VaultName")); //Launched driver and logged in

			Log.message("1. Logged into Default Webpage as a Read only user");

			//Step- 2 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Multiple Objects is selected in the view");

			//Step-4 : Open the popout metadata card of the object using operation menu
			//----------------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Clicks properties option via operation menu


			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the metadata card

			Log.message("4. Pop out metadatacard of the object is opened via operation menu");

			//Step-5 : Modify the metadata card
			//---------------------------------
			metadatacard.setPermission(dataPool.get("Permission"));//Modify the permission value in metadata card
			metadatacard.clickOKBtn(driver);//Clicks save button in the metadatacard


			Log.message("5. Multiple Objects is modified in the metadatacard and save button is clicked");

			if (!MFilesDialog.exists(driver))
				throw new Exception("Test case Failed. Expected warning dialog is not displayed");

			MFilesDialog MFDialog= new MFilesDialog(driver);

			String warningMessage=MFDialog.getMessage();//Gets the warning message from the warning dialog

			//Verification : Verify if read only user able to edit the metadata card in popout metadatacard via context menu
			//-------------------------------------------------------------------------------------
			if (warningMessage.equalsIgnoreCase(dataPool.get("ExpectedMessage")))
				Log.pass("Test case Passed. User unable to save the changes in popout metadatacard via operation menu for MultiSelected Objects", driver);
			else
				Log.fail("Test case Failed. Expected warning message is not displayed", driver);

		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_34196_2C

	/**
	 * 34196.2D : Trying to edit when user has read only license in popout metadatacard via Task pane for MultiSelected Objects
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "M6450MetadataConfigurability"}, 
			description = "Trying to edit when user has read only license in popout metadatacard via Operation menu for MultiSelected Objects")
	public void TC_34196_2D(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();

			//Step- 1: Login to homepage
			//-------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), xmlParameters.getParameter("VaultName")); //Launched driver and logged in

			Log.message("1. Logged into Default Webpage as a Read only user");

			//Step- 2 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Multiple Objects is selected in the view");

			//Step-4 : Open the popout metadata card of the object using context menu
			//----------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Clicks properties option via task pane


			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the metadata card

			Log.message("4. Pop out metadatacard of the object is opened via operation menu");

			//Step-5 : Modify the metadata card
			//---------------------------------
			metadatacard.setPermission(dataPool.get("Permission"));//Modify the permission value in metadata card
			metadatacard.clickOKBtn(driver);//Clicks save button in the metadatacard



			Log.message("5. Multiple Objects is modified in the metadatacard and save button is clicked");

			if (!MFilesDialog.exists(driver))
				throw new Exception("Test case Failed. Expected warning dialog is not displayed");

			MFilesDialog MFDialog= new MFilesDialog(driver);

			String warningMessage=MFDialog.getMessage();//Gets the warning message from the warning dialog

			//Verification : Verify if read only user able to edit the metadata card in popout metadatacard via context menu
			//-------------------------------------------------------------------------------------
			if (warningMessage.equalsIgnoreCase(dataPool.get("ExpectedMessage")))
				Log.pass("Test case Passed. User unable to save the changes in popout metadatacard via task pane for MultiSelected Objects", driver);
			else
				Log.fail("Test case Failed. Expected warning message is not displayed", driver);

		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_34196_2D


	/**
	 * 34193.1A : User with edit right can edit the metadata in right pane metadata card [Discard changes]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"M6450MetadataConfigurability"}, 
			description = "User with edit right can edit the metadata and discard the changes and discard the changes in right pane metadata card")
	public void TC_34193_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();

			//Step- 1: Login to homepage
			//-------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), xmlParameters.getParameter("VaultName")); //Launched driver and logged in

			Log.message("1. Logged into Default Webpage as a user with edit rights");

			//Step- 2 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-3 : Select the object in the view
			//----------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not selected in the homePage.listView.");

			Log.message("3. Object is selected in the view");

			//Step-4 : Modify the metadata card
			//---------------------------------
			MetadataCard metadatacard = new MetadataCard(driver, true);//Instantiate the metadata card in right pane
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("PropertyValue"));//Modify the value in metadata card
			metadatacard.clickDiscardButton();//Clicks Discard button in the metadatacard


			Log.message("4. Object is modified and discard button is clicked in the metadatacard");

			//Verification : Verify if changes are discarded successfully in the right pane metadata card
			//-------------------------------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true);//Instantiates the metadatacard
			if (!metadatacard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyValue")))
				Log.pass("Test case Passed. Changes are discarded successfully in rightpane metadatacard", driver);
			else
				Log.fail("Test case Failed. Changes are not discarded in rightpane metadatacard", driver);

		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_34193_1A

	/**
	 * 34193.1B : User with edit right can edit the metadata in popout metadata card via context menu [Discard changes]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"M6450MetadataConfigurability"}, 
			description = "User with edit right can edit the metadata and discard the changes in popout metadata card via context menu [Discard changes]")
	public void TC_34193_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();

			//Step- 1: Login to homepage
			//-------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), xmlParameters.getParameter("VaultName")); //Launched driver and logged in

			Log.message("1. Logged into Default Webpage as a user with edit rights");

			//Step- 2 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-3 : Select the object in the view
			//----------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");


			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Right clicks the Object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu


			Log.message("3. Object is selected and pop out metadatacard of the object is opened via context menu");

			//Step-4 : Modify the metadata card
			//---------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the metadata card
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("PropertyValue"));//Modify the value in metadata card
			metadatacard.clickDiscardButton();//Clicks Discard button in the metadatacard


			Log.message("4. Object is modified and discard button is clicked in the popout metadatacard via context menu");

			//Step-5 : Open the popout metadatacard of the object
			//---------------------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Right clicks the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			Log.message("5.Pop out metadatacard of the object is opened via context menu");

			metadatacard = new MetadataCard(driver);//Instantiate the metadata card

			//Verification : Verify if changes are discarded successfully in the popout metadata card via context menu
			//--------------------------------------------------------------------------------------------------------
			if (!metadatacard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyValue")))
				Log.pass("Test case Passed. Changes are discarded successfully in popout metadatacard via context menu", driver);
			else
				Log.fail("Test case Failed. Changes are not discarded in popout metadatacard via context menu", driver);

		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_34193_1B

	/**
	 * 34193.1C : User with edit right can edit the metadata in popout metadata card via operation menu [Discard changes]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"M6450MetadataConfigurability"}, 
			description = "User with edit right can edit the metadata and discard the changes in popout metadata card via operation menu [Discard changes]")
	public void TC_34193_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();

			//Step- 1: Login to homepage
			//-------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), xmlParameters.getParameter("VaultName")); //Launched driver and logged in

			Log.message("1. Logged into Default Webpage as a user with edit rights");

			//Step- 2 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-3 : Select the object in the view
			//----------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Clicks properties option via operation menu


			Log.message("3. Object is selected and pop out metadatacard of the object is opened via operation menu");

			//Step-4 : Modify the metadata card
			//---------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the metadata card
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("PropertyValue"));//Modify the value in metadata card
			metadatacard.clickDiscardButton();//Clicks Discard button in the metadatacard


			Log.message("4. Object is modified and discard button is clicked in the popout metadatacard via context menu");

			//Step-5 : Open the popout metadatacard of the object
			//---------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Clicks properties option via operation menu

			Log.message("5.Pop out metadatacard of the object is opened via operation menu");

			metadatacard = new MetadataCard(driver);//Instantiate the metadata card

			//Verification : Verify if changes are discarded successfully in the popout pane metadata card via operation menu
			//----------------------------------------------------------------------------------------------------------------
			if (!metadatacard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyValue")))
				Log.pass("Test case Passed. Changes are discarded successfully in popout metadatacard via operation menu", driver);
			else
				Log.fail("Test case Failed. Changes are not discarded in popout metadatacard via operation menu", driver);

		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_34193_1C

	/**
	 * 34193.1D : User with edit right can edit the metadata in popout metadata card via Task pane [Discard changes]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"M6450MetadataConfigurability"}, 
			description = "User with edit right can edit the metadata and discard the changes in popout metadata card via Task pane")
	public void TC_34193_1D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();

			//Step- 1: Login to homepage
			//-------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), xmlParameters.getParameter("VaultName")); //Launched driver and logged in

			Log.message("1. Logged into Default Webpage as a user with edit rights");

			//Step- 2 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-3 : Select the object in the view
			//----------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Clicks properties option via task pane


			Log.message("3. Object is selected and pop out metadatacard of the object is opened via task pane");

			//Step-4 : Modify the metadata card
			//---------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the metadata card
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("PropertyValue"));//Modify the value in metadata card
			metadatacard.clickDiscardButton();//Clicks Discard button in the metadatacard


			Log.message("4. Object is modified and discard button is clicked in the popout metadatacard via context menu");

			//Step-5 : Open the popout metadatacard of the object
			//---------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Clicks properties option via task pane

			Log.message("5.Pop out metadatacard of the object is opened via task pane");

			metadatacard = new MetadataCard(driver);//Instantiate the metadata card

			//Verification : Verify if changes are discarded successfully in the popout pane metadata card via task pane
			//----------------------------------------------------------------------------------------------------------------
			if (!metadatacard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyValue")))
				Log.pass("Test case Passed. Changes are discarded successfully in popout metadatacard via Task pane", driver);
			else
				Log.fail("Test case Failed. Changes are not discarded in popout metadatacard via task pane", driver);

		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_34193_1D

	/**
	 * 34193.2A : User with edit right can edit the metadata in right pane metadata card [Save changes]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"M6450MetadataConfigurability"}, 
			description = "User with edit right can edit the metadata and save the changes in right pane metadata card")
	public void TC_34193_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();

			//Step- 1: Login to homepage
			//-------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), xmlParameters.getParameter("VaultName")); //Launched driver and logged in

			Log.message("1. Logged into Default Webpage as a user with edit rights");

			//Step- 2 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-3 : Select the object in the view
			//----------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not selected in the homePage.listView.");

			Log.message("3. Object is selected in the view");

			//Step-4 : Modify the metadata card
			//---------------------------------
			MetadataCard metadatacard = new MetadataCard(driver, true);//Instantiate the metadata card in right pane
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("PropertyValue"));//Modify the value in metadata card
			metadatacard.clickOKBtn(driver);//Clicks Save button in the metadatacard


			Log.message("4. Object is modified and Save button is clicked in the metadatacard");

			metadatacard = new MetadataCard(driver, true);//Instantiates the metadatacard


			//Verification : Verify if changes are saved successfully in the right pane metadata card
			//-------------------------------------------------------------------------------------
			if (metadatacard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyValue")))
				Log.pass("Test case Passed. Changes are saved successfully in rightpane metadatacard", driver);
			else
				Log.fail("Test case Failed. Changes are not saved in rightpane metadatacard", driver);

		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_34193_2A

	/**
	 * 34193.2B : User with edit right can edit the metadata in popout metadata card via context menu [Save changes]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"M6450MetadataConfigurability"}, 
			description = "User with edit right can edit the metadata and save the changes in popout metadata card via context menu [Discard changes]")
	public void TC_34193_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();

			//Step- 1: Login to homepage
			//-------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), xmlParameters.getParameter("VaultName")); //Launched driver and logged in

			Log.message("1. Logged into Default Webpage as a user with edit rights");

			//Step- 2 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-3 : Select the object in the view
			//----------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");


			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Right clicks the Object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu


			Log.message("3. Object is selected and pop out metadatacard of the object is opened via context menu");

			//Step-4 : Modify the metadata card
			//---------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the metadata card
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("PropertyValue"));//Modify the value in metadata card
			metadatacard.clickOKBtn(driver);//Clicks Save button in the metadatacard

			Log.message("4. Object is modified and save button is clicked in the popout metadatacard via context menu");

			//Step-5 : Open the popout metadatacard of the object
			//---------------------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Right clicks the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			Log.message("5.Pop out metadatacard of the object is opened via context menu");

			metadatacard = new MetadataCard(driver);//Instantiate the metadata card

			//Verification : Verify if changes are Saved successfully in the popout metadata card via context menu
			//--------------------------------------------------------------------------------------------------------
			if (metadatacard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyValue")))
				Log.pass("Test case Passed. Changes are Saved successfully in popout metadatacard via context menu", driver);
			else
				Log.fail("Test case Failed. Changes are not Saved in popout metadatacard via context menu", driver);

		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_34193_2B

	/**
	 * 34193.2C : User with edit right can edit the metadata in popout metadata card via operation menu [Save changes]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"M6450MetadataConfigurability"}, 
			description = "User with edit right can edit the metadata and save the changes in popout metadata card via operation menu [Discard changes]")
	public void TC_34193_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();

			//Step- 1: Login to homepage
			//-------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), xmlParameters.getParameter("VaultName")); //Launched driver and logged in

			Log.message("1. Logged into Default Webpage as a user with edit rights");

			//Step- 2 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-3 : Select the object in the view
			//----------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Clicks properties option via operation menu


			Log.message("3. Object is selected and pop out metadatacard of the object is opened via operation menu");

			//Step-4 : Modify the metadata card
			//---------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the metadata card
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("PropertyValue"));//Modify the value in metadata card
			metadatacard.clickOKBtn(driver);//Clicks Save button in the metadatacard


			Log.message("4. Object is modified and save button is clicked in the popout metadatacard via context menu");

			//Step-5 : Open the popout metadatacard of the object
			//---------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Clicks properties option via operation menu

			Log.message("5.Pop out metadatacard of the object is opened via operation menu");

			metadatacard = new MetadataCard(driver);//Instantiate the metadata card

			//Verification : Verify if changes are saved successfully in the popout pane metadata card via operation menu
			//----------------------------------------------------------------------------------------------------------------
			if (metadatacard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyValue")))
				Log.pass("Test case Passed. Changes are saved successfully in popout metadatacard via operation menu", driver);
			else
				Log.fail("Test case Failed. Changes are not saved in popout metadatacard via operation menu", driver);

		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_34193_2C

	/**
	 * 34193.2D : User with edit right can edit the metadata in popout metadata card via Task pane [Save changes]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"M6450MetadataConfigurability"}, 
			description = "User with edit right can edit the metadata and save the changes in popout metadata card via Task pane")
	public void TC_34193_2D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();

			//Step- 1: Login to homepage
			//-------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), xmlParameters.getParameter("VaultName")); //Launched driver and logged in

			Log.message("1. Logged into Default Webpage as a user with edit rights");

			//Step- 2 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-3 : Select the object in the view
			//----------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Clicks properties option via task pane


			Log.message("3. Object is selected and pop out metadatacard of the object is opened via task pane");

			//Step-4 : Modify the metadata card
			//---------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the metadata card
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("PropertyValue"));//Modify the value in metadata card
			metadatacard.clickOKBtn(driver);//Clicks save button in the metadatacard


			Log.message("4. Object is modified and save button is clicked in the popout metadatacard via context menu");

			//Step-5 : Open the popout metadatacard of the object
			//---------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Clicks properties option via task pane

			Log.message("5.Pop out metadatacard of the object is opened via task pane");

			metadatacard = new MetadataCard(driver);//Instantiate the metadata card

			//Verification : Verify if changes are saved successfully in the popout pane metadata card via task pane
			//----------------------------------------------------------------------------------------------------------------
			if (metadatacard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyValue")))
				Log.pass("Test case Passed. Changes are saved successfully in popout metadatacard via Task pane", driver);
			else
				Log.fail("Test case Failed. Changes are not saved in popout metadatacard via task pane", driver);

		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_34193_2D

	/**
	 * 34193.3 : User with edit right can edit the metadata in popout metadata card via Task pane [Save changes]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"M6450MetadataConfigurability"}, 
			description = "User with edit right can edit the metadata and save the changes in popout metadata card via Task pane")
	public void TC_34193_3(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();

			//Step- 1: Login to homepage
			//-------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), xmlParameters.getParameter("VaultName")); //Launched driver and logged in

			Log.message("1. Logged into Default Webpage as a user with edit rights");

			//Step- 2 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-3 : Select the object in the view
			//----------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Clicks properties option via task pane


			Log.message("3. Object is selected and pop out metadatacard of the object is opened via task pane");

			//Step-4 : Add the property in metadata card
			//---------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the metadata card
			metadatacard.addNewProperty(dataPool.get("Property"));//Add the property in metadata card


			if (!metadatacard.propertyExists(dataPool.get("Property")))
				throw new Exception("Test case failed. Property ( " + dataPool.get("Property") + " ) is not added in the metadatacard");

			metadatacard.clickOKBtn(driver);//Clicks save button in the metadatacard


			Log.message("4. New Property ( " + dataPool.get("Property") + " ) is added and saved in the metadatacard");

			//Step-5 : Open the popout metadatacard of the object
			//---------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Clicks properties option via task pane


			Log.message("5.Pop out metadatacard of the object is opened via task pane");

			//Step-6 : Remove the property in the metadatacard
			//-------------------------------------------------
			metadatacard = new MetadataCard(driver);//Instantiate the metadata card
			metadatacard.removeProperty(dataPool.get("Property"));//Remove the property in metadata card


			if (metadatacard.propertyExists(dataPool.get("Property")))
				throw new Exception("Test case Failed. Property ( " + dataPool.get("Property") + " ) is not removed from the metadatacard");

			metadatacard.clickOKBtn(driver);//Clicks save button in the metadatacard


			Log.message("6. Property ( " + dataPool.get("Property") + " ) is removed and saved in the metadatacard");


			//Verification : Verify if changes are saved successfully in the popout pane metadata card via task pane
			//----------------------------------------------------------------------------------------------------------------
			if (!metadatacard.propertyExists(dataPool.get("Property")))
				Log.pass("Test case Passed. Property is successfully added and remvoed in popout metadatacard", driver);
			else
				Log.fail("Test case Failed. Property is not removed from popout metadatacard", driver);

		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_34193_3


	/**
	 * 34193.4A : User with edit right can edit the metadata in popped out metadata card [History View]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"M6450MetadataConfigurability"}, 
			description = "User with edit right can edit the metadata and discard the changes in popout metadata card in history view")
	public void TC_34193_4A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();

			//Step- 1: Login to homepage
			//-------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), xmlParameters.getParameter("VaultName")); //Launched driver and logged in

			Log.message("1. Logged into Default Webpage as a user with edit rights");

			//Step- 2 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-3 : Navigate to the History view
			//---------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.History.Value); //Clicks History option via task pane


			Log.message("3. Object is selected in the view and History option is clicked via task pane");

			//Step-4 : Select the object in history view
			//---------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Clicks properties option via task pane


			Log.message("4. Object is selected in the History view and popout metadata card of the object is opened via task pane");

			//Step-5 : Modify the metadata card
			//---------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the metadata card
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("PropertyValue"));//Modify the value in metadata card
			metadatacard.clickDiscardButton();//Clicks Discard button in the metadatacard


			Log.message("5. Object is modified and discard button is clicked in the popout metadatacard via context menu");

			//Step-6 : Open the popout metadatacard of the object
			//---------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Clicks properties option via task pane

			Log.message("6.Pop out metadatacard of the object is opened via task pane");

			metadatacard = new MetadataCard(driver);//Instantiate the metadata card

			//Verification : Verify if changes are discarded successfully in the popout pane metadata card via task pane
			//----------------------------------------------------------------------------------------------------------------
			if (!metadatacard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyValue")))
				Log.pass("Test case Passed. Changes are discarded successfully in popout metadatacard in History view", driver);
			else
				Log.fail("Test case Failed. Changes are not discarded in popout metadatacard in History view", driver);

		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_34193_4A

	/**
	 * 34193.4B : User with edit right can edit the metadata in popped out metadata card [Relationships View]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"M6450MetadataConfigurability"}, 
			description = "User with edit right can edit the metadata and discard the changes in popout metadata card in relationship view")
	public void TC_34193_4B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();

			//Step- 1: Login to homepage
			//-------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), xmlParameters.getParameter("VaultName")); //Launched driver and logged in

			Log.message("1. Logged into Default Webpage as a user with edit rights");

			//Step- 2 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//Step-3 : Navigate to the History view
			//---------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Relationships.Value); //Clicks Relationships option via task pane


			Log.message("3. Object is selected in the view and Relationships option is clicked via task pane");

			//Step-4 : Select the object in history view
			//---------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName1")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName1") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName1"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName1") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Clicks properties option via task pane


			Log.message("4. Object is selected in the relationship view and popout metadata card of the object is opened via task pane");

			//Step-5 : Modify the metadata card
			//---------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the metadata card
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("PropertyValue"));//Modify the value in metadata card
			metadatacard.clickOKBtn(driver);//Clicks Save button in the metadatacard


			Log.message("5. Object is modified and discard button is clicked in the popout metadatacard via context menu");

			//Step-6 : Open the popout metadatacard of the object
			//---------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName1"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName1") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Clicks properties option via task pane

			Log.message("6.Pop out metadatacard of the object is opened via task pane");

			metadatacard = new MetadataCard(driver);//Instantiate the metadata card

			//Verification : Verify if changes are saved successfully in the popout pane metadata card via task pane
			//----------------------------------------------------------------------------------------------------------------
			if (metadatacard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyValue")))
				Log.pass("Test case Passed. Changes are saved successfully in popout metadatacard in relationships view", driver);
			else
				Log.fail("Test case Failed. Changes are not saved in popout metadatacard in relationships view", driver);

		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_34193_4B

	/**
	 * TC1.1.7A : Pop Out  Metadata Card using context menu with changes in Right pane metadata card
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MetadataConfigurability"}, 
			description = "Pop Out  Metadata Card using context menu with changes in Right pane metadata card")
	public void TC_1_1_7A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			homePage.searchPanel.search("", dataPool.get("SearchType"));//Navigate to 'Search only:Documents' view

			Log.message("1. Navigate to specified : " + dataPool.get("SearchType") + " search view.");

			//Step-2 : Select the any existing document object
			//------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectName"));//Selected the specified object type

			Log.message("2. Selected the specified object : " + dataPool.get("ObjectName") + " in list view.");

			//Step-3 : Modified the metadata property value in right pane metadatacard 
			//------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver,true);//Instantiate the right pane metadatacard
			metadataCard.setInfo(dataPool.get("Properties")+dataPool.get("Value"));

			Log.message("3. Modified the property value as : " + dataPool.get("Properties")+dataPool.get("Value") + " in the right pane metadatacard.");

			//Step-4 : Click the 'Properties' option in context menu 
			//------------------------------------------------------
			driver.switchTo().defaultContent();
			homePage.listView.rightClickItem(dataPool.get("ObjectName"));
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value);


			metadataCard = new MetadataCard(driver);//Instantiate the metadatacard

			Log.message("4. Opened the Metadatacard through the context menu 'Properties' option.");

			//Verification : Verify if property is set as expected
			//----------------------------------------------------
			if((dataPool.get("HeaderColor").toUpperCase().contains(metadataCard.getHeaderColor().toUpperCase())) &&  (metadataCard.getPropertyValue(dataPool.get("Properties").replace("::", "")).equals(dataPool.get("Value"))))
				Log.pass("Test Case Passed. Changes are present in popped out metadatacard opened via context menu with changes in rightpane metadatacard ");
			else
				Log.fail("Test Case Failed.Changes are not present in popped out metadatacard opened via context menu with changes in rightpane metadatacard" , driver);
		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End 1.1.7A


	/**
	 * TC1.1.7B : Pop Out  Metadata Card using operation menu with changes in Right pane metadata card
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MetadataConfigurability"}, 
			description = "Pop Out  Metadata Card using operation menu with changes in Right pane metadata card.")
	public void TC_1_1_7B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			homePage.searchPanel.search("", dataPool.get("SearchType"));//Navigate to 'Search only:Documents' view

			Log.message("1. Navigate to specified : " + dataPool.get("SearchType") + " search view.");

			//Step-2 : Select the any existing document object
			//------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectName"));//Selected the specified object type

			Log.message("2. Selected the specified object : " + dataPool.get("ObjectName") + " in list view.");

			//Step-3 : Modified the metadata property value in right pane metadatacard 
			//------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver,true);//Instantiate the right pane metadatacard
			metadataCard.setInfo(dataPool.get("Properties")+dataPool.get("Value"));//Set the metadata property value

			driver.switchTo().defaultContent();//Switching to the default content

			Log.message("3. Modified the property value as : " + dataPool.get("Properties")+dataPool.get("Value") + " in the right pane metadatacard.");

			//Step-4 : Click the 'Properties' option in operations menu
			//----------------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value);//Click the 'Properties' in operations menu


			metadataCard = new MetadataCard(driver);//Instantiate the metadatacard

			Log.message("4. Opened the Metadatacard through the operations menu 'Properties' option.");

			//Verification : Verify if property is set as expected
			//----------------------------------------------------
			if((dataPool.get("HeaderColor").toUpperCase().contains(metadataCard.getHeaderColor().toUpperCase())) &&  (metadataCard.getPropertyValue(dataPool.get("Properties").replace("::", "")).equals(dataPool.get("Value"))))
				Log.pass("Test Case Passed. Changes are present in popped out metadatacard opened via operation menu with changes in rightpane metadatacard ");
			else
				Log.fail("Test Case Failed.Changes are not present in popped out metadatacard opened via operation menu with changes in rightpane metadatacard" , driver);
		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End 1.1.7B


	/**
	 * TC1.1.7C : Pop Out Metadata Card using task pane with changes in Right pane metadata card
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MetadataConfigurability"}, 
			description = "Pop Out  Metadata Card using task pane with changes in Right pane metadata card.")
	public void TC_1_1_7C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			homePage.searchPanel.search("", dataPool.get("SearchType"));//Navigate to 'Search only:Documents' view

			Log.message("1. Navigate to specified : " + dataPool.get("SearchType") + " search view.");

			//Step-2 : Select the any existing document object
			//------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectName"));//Selected the specified object type

			Log.message("2. Selected the specified object : " + dataPool.get("ObjectName") + " in list view.");

			//Step-3 : Modified the metadata property value in right pane metadatacard 
			//------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver,true);//Instantiate the right pane metadatacard

			metadataCard.setInfo(dataPool.get("Properties")+dataPool.get("Value"));//Set the metadata property value

			driver.switchTo().defaultContent();//Switching to the default content

			Log.message("3. Modified the property value as : " + dataPool.get("Properties")+dataPool.get("Value") + " in the right pane metadatacard.");

			//Step-4 : Click the 'Properties' option in task pane
			//------------------------------------------------------
			if (!homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value))//Click the 'Properties' in operations menu
				throw new Exception("Properties is not clicked from task pane");



			metadataCard = new MetadataCard(driver);//Instantiate the metadatacard

			Log.message("4. Opened the Metadatacard through the taskpanel 'Properties' option.");

			//Verification : Verify if property is set as expected
			//----------------------------------------------------
			if((dataPool.get("HeaderColor").toUpperCase().contains(metadataCard.getHeaderColor().toUpperCase())) &&  (metadataCard.getPropertyValue(dataPool.get("Properties").replace("::", "")).equals(dataPool.get("Value"))))
				Log.pass("Test Case Passed. Changes are present in popped out metadatacard opened via task pane with changes in rightpane metadatacard ");
			else
				Log.fail("Test Case Failed.Changes are not present in popped out metadatacard opened via task pane with changes in rightpane metadatacard" , driver);
		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End 1.1.7C

	/**
	 * TC1.1.7D : Pop Out  Metadata Card using settings menu with changes in Right pane metadata card
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MetadataConfigurability"}, 
			description = "Pop Out  Metadata Card using settings menu with changes in Right pane metadata card.")
	public void TC_1_1_7D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			homePage.searchPanel.search("", dataPool.get("SearchType"));//Navigate to 'Search only:Documents' view

			Log.message("1. Navigate to specified : " + dataPool.get("SearchType") + " search view.");

			//Step-2 : Select the any existing document object
			//------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectName"));//Selected the specified object type

			Log.message("2. Selected the specified object : " + dataPool.get("ObjectName") + " in list view.");

			//Step-3 : Modified the metadata property value in right pane metadatacard 
			//------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver,true);//Instantiate the right pane metadatacard

			metadataCard.setInfo(dataPool.get("Properties")+dataPool.get("Value"));//Set the metadata property value

			Log.message("3. Modified the property value as : " + dataPool.get("Properties")+dataPool.get("Value") + " in the right pane metadatacard.");

			//Step-4 : Opens the popout metadata card via settings menu 
			//------------------------------------------------------
			metadataCard.popOutMetadatacard();//Click the settings menu and pop-out the metadatacard


			metadataCard = new MetadataCard(driver);//Instantiate the metadatacard

			Log.message("4. Opened the Metadatacard through the settings menu in metadata card.");

			//Verification : Verify if property is set as expected
			//----------------------------------------------------
			if((dataPool.get("HeaderColor").toUpperCase().contains(metadataCard.getHeaderColor().toUpperCase())) &&  (metadataCard.getPropertyValue(dataPool.get("Properties").replace("::", "")).equals(dataPool.get("Value"))))
				Log.pass("Test Case Passed. Changes are present in popped out metadatacard opened via settings menu with changes in rightpane metadatacard ");
			else
				Log.fail("Test Case Failed.Changes are not present in popped out metadatacard opened via settings menu with changes in rightpane metadatacard" , driver);
		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End 1.1.7D

	/**
	 * TC1.1.7E : Pop Out Metadata Card using previewpane with changes in Right pane metadata card
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MetadataConfigurability"}, 
			description = "Pop Out  Metadata Card using task pane with changes in Right pane metadata card.")
	public void TC_1_1_7E(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			homePage.searchPanel.search("", dataPool.get("SearchType"));//Navigate to 'Search only:Documents' view

			Log.message("1. Navigate to specified : " + dataPool.get("SearchType") + " search view.");

			//Step-2 : Select the any existing document object
			//------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectName"));//Selected the specified object type

			Log.message("2. Selected the specified object : " + dataPool.get("ObjectName") + " in list view.");

			//Step-3 : Modified the metadata property value in right pane metadatacard 
			//------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver,true);//Instantiate the right pane metadatacard

			metadataCard.setInfo(dataPool.get("Properties")+dataPool.get("Value"));//Set the metadata property value

			driver.switchTo().defaultContent();//Switching to the default content

			Log.message("3. Modified the property value as : " + dataPool.get("Properties")+dataPool.get("Value") + " in the right pane metadatacard.");

			//Step-4 : Opens the popout metadata card via previewpane
			//------------------------------------------------------
			homePage.previewPane.popoutRightPaneMetadataTab();//Open the popout metadatacard via previewpane


			metadataCard = new MetadataCard(driver);//Instantiate the metadatacard

			Log.message("4. Opened the Metadatacard through the previewpane");

			//Verification : Verify if property is set as expected
			//----------------------------------------------------
			if((dataPool.get("HeaderColor").toUpperCase().contains(metadataCard.getHeaderColor().toUpperCase())) &&  (metadataCard.getPropertyValue(dataPool.get("Properties").replace("::", "")).equals(dataPool.get("Value"))))
				Log.pass("Test Case Passed. Changes are present in popped out metadatacard opened via previewpane with changes in rightpane metadatacard ");
			else
				Log.fail("Test Case Failed.Changes are not present in popped out metadatacard opened via previewpane with changes in rightpane metadatacard" , driver);
		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End 1.1.7E

	/**
	 * TC_1.1.3A : Change properties of multiple objects in right pane metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "MetadataConfigurability"},
			description = "Change properties of multiple objects in right pane metadatacard.")
	public void TC_1_1_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("SearchType")," ");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 :  Select the many document in metadatacard
			//--------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("ObjectName"));

			Log.message("2. Selected the mulipleitems in the list view : "+dataPool.get("ObjectName"));

			//Step-3 : Instantiate the right pane metadatacard and modify the property values
			//-------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver,true);
			metadataCard.setInfo(dataPool.get("Properties")+"::"+dataPool.get("Value"));
			metadataCard.saveAndClose();//Save the metadata card

			Log.message("3. Modified the property value as : " + dataPool.get("Properties")+dataPool.get("Value") + " in the right pane metadatacard.");

			//Verification: To verify if the property is added to the multi-selected objects
			//-------------------------------------------------------------------------------

			SearchPanel.searchOrNavigatetoView(driver, dataPool.get("SearchType")," ");

			String[] objects = dataPool.get("ObjectName").split("\n");
			int count = 0;
			for(count = 0; count < objects.length; count++) {
				homePage.listView.clickItem(objects[count]);

				metadataCard = new MetadataCard(driver, true);
				if(!metadataCard.propertyExists(dataPool.get("Properties"))&& metadataCard.getPropertyValue(dataPool.get("Properties")).equalsIgnoreCase(dataPool.get("Value")))
					Log.fail("Test Case Failed. The Property was not added to the object - " + objects[count], driver);
				driver.switchTo().defaultContent();
			}//end for

			//Verify if all muliselected property is set as expected
			//------------------------------------------------------
			if(count == objects.length)
				Log.pass("Test Case Passed. The Modified Property value was added to all the multi-selected objects.");
			else 
				Log.fail("Test Case Failed. The Modified property was not set as expected in all multi-selected objects.", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}
		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End 1.1.3A

}
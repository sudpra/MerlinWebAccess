package MFClient.Tests;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import genericLibrary.EmailReport;
import genericLibrary.Utils;
import genericLibrary.WebDriverUtils;
import genericLibrary.DataProviderUtils;
import genericLibrary.Log;
import MFClient.Pages.ConfigurationPage;
import MFClient.Pages.HomePage;
import MFClient.Pages.LoginPage;
import MFClient.Wrappers.Caption;
import MFClient.Wrappers.ListView;
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.MFilesObjectList;
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class MFClientSmokeTests {


	HomePage MFClienthomePage=null;
	public static String webSite=null;
	public static String configSite=null;
	public static String driverType=null;
	public static String documentVault=null;
	public static String mfilesAdminTestPassword=null;
	public static String xlTestDataWorkSheet;
	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String configURL = null;
	public static String userName = null;
	public static String password = null;
	public static String testVault = null;
	public static String productVersion = null;
	public static WebDriver driver = null;
	public static String userFullName = null;
	public String methodName = null;

	@BeforeClass
	public void initt(ITestContext context) throws IOException {
		MFClientSmokeTests.xlTestDataWorkBook = context.getCurrentXmlTest().getParameter("TestData");

	}

	@BeforeTest
	public void init(ITestContext context) throws Exception
	{

		XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
		xlTestDataWorkBook = xmlParameters.getParameter("TestData");
		loginURL = xmlParameters.getParameter("webSite");
		configURL = xmlParameters.getParameter("ConfigurationURL");
		Utility.restoreTestVault();
		//String userConfig = Utility.configureUsers(xlTestDataWorkBook, loginURL);

		userName = xmlParameters.getParameter("UserName");
		password = xmlParameters.getParameter("Password");
		testVault = xmlParameters.getParameter("VaultName");		
		userFullName = xmlParameters.getParameter("UserFullName");

	}

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

			Utility.destroyTestVault();

		}//End try

		catch(Exception e){
			throw e;
		}//End Catch
	}//End cleanApp

	/**
	 * TC001 : Login to WebAccess with different credentials
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Smoke"}, 
			description = "Login to WebAccess with different credentials")
	public void TC_001(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = WebDriverUtils.getDriver();

		try {

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1 : Launch login page
			driver.get(webSite);

			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN"))
				throw new Exception("Login page is not launched.");

			Log.message("1.Login page URL (" + webSite + ") is launched.");

			//Step-2 : Login with user credentials
			//------------------------------------
			LoginPage loginPage=new LoginPage(driver);
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), documentVault);

			Log.message("2. Login credentials are entered in the login page.");

			//Verification : Verify if login is successful with the user
			//----------------------------------------------------------
			if (!driver.getCurrentUrl().toUpperCase().contains("DEFAULT.ASPX")) //Verifies if default page is loaded
				throw new Exception("Test case Failed. Default page of an application is not loaded");

			if (homePage.getLoggedinUserName().equalsIgnoreCase(dataPool.get("UserName")))
				Log.pass("Test case Passed. M-Files web access is logged in successfully with provided user credentials.");
			else
				Log.fail("Test case Failed. M-Files web access is logged in not successful with provided user credentials.", driver);

		}//End of try

		catch(Exception e) {
			Log.exception(e, driver);
		}//End of Catch

		finally{
			//Close the browser
			driver.quit();
		}//End of Finally
	}//End of TC_001

	/**
	 * TC002 : Create New 'Document' From TaskPane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Smoke"}, 
			description = "Create New 'Document' From TaskPane")
	public void TC_002(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = WebDriverUtils.getDriver();

		try {

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Select New Document from menubar
			//--------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Document.Value); //Clicks New Document from the taskpanel
			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard = metadatacard.setTemplateUsingSearchkey(dataPool.get("TemplateOrExtension"), dataPool.get("TemplateOrExtension"));

			Log.message("1. New Document is selected from taskpanel.", driver);

			//Step-2 : Enter the metadatacard details
			//---------------------------------------
			metadatacard.setInfo(dataPool.get("Properties"));
			ConcurrentHashMap <String, String> prevInfo = metadatacard.getInfo(); //Gets the property information			

			Log.message("2. New Document details are entered in metadatacard.", driver);

			//Step-3 : Save the metadatacard
			//------------------------------
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			String docName = "";

			if (dataPool.get("TemplateOrExtension").equalsIgnoreCase("Multi-File Document"))
				docName = prevInfo.get("Name or title");
			else
				docName = prevInfo.get("Name or title") + "." + prevInfo.get("Extension");

			Log.message("3. New Document metadatacard is saved.", driver);

			//Step-4 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5 : Open the Properties dialog of the new document through context menu
			//-----------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(docName)) //Checks if Item exists in the list
				throw new Exception("Newly created document (" + docName + ") does not exists in the list.");

			if (!homePage.listView.rightClickItem(docName)) //Right clicks the project
				throw new Exception("Newly created document (" + docName + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("5. Properties dialog of the new document (" + docName + ") is opened through context menu.", driver);

			//Step-6 : Obtain metadatacard information and compare the properties before and after saving metadatacard
			//---------------------------------------------------------------------------------------------------------
			ConcurrentHashMap <String, String> currInfo = metadatacard.getInfo(); //Gets the property information
			String diff = Utility.compareObjects(currInfo, prevInfo, "Extension");

			Log.message("6. Current Document metadatacard details are obtained and compared with the entered values.");

			//Verification : Verifies if Mark Complete Icon is displayed
			//----------------------------------------------------------
			if (diff.equalsIgnoreCase(""))
				Log.pass("Test case Passed. New Document (" + docName + ") is created successfully from taskpanel.");
			else
				Log.fail("Test case Failed. New Document (" + docName + ") is creation is not successful. Additonal Information : " + diff, driver);

			metadatacard.closeMetadataCard(); //Closes Metadatacard

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_002

	/**
	 * TestCase ID: TC_003
	 * <br>Description: Create New 'Assignment' From TaskPane</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_003",description="Create New 'Assignment' From homePage.taskPanel.")
	public void TC_003(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception{

		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo("Create New 'Assignment' From homePage.taskPanel.");
		Log.addTestRunMachineInfo(driver,currentDriver,context);//get execution environment details to print on Log
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);

		try{

			//Step-1: Launch M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Step-2: Login to Application
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			//Wait for the invisibility of loading image
			Utils.fluentWait(driver);
			//Create the HomePage object
			HomePage homePage=new HomePage(driver);
			//Step-5: Verify if TaskPane displayed
			if(!homePage.isTaskPaneDisplayed())
				throw new Exception("Task pane not displayed.");
			Log.message("4. Task Pane displayed in HomePage.");

			//Step-6: Click 'Assignment' link from task pane and create 'Assignment' object
			homePage.clickTaskPaneNewLink("Assignment");
			Log.message("5. Clicked 'Assignment' link from task pane of HomePage.");

			//Step-7: Verify the Metadatacard display
			if (!homePage.verifyMetadataCardDisplay("Assignment"))
				throw new Exception("Failed. MetadataCard Not displayed. ");
			Log.message("6. MetadataCard displayed");
			Thread.sleep(2000);

			//Instantiate the Metadatacard
			MetadataCard metadataCard=new MetadataCard(driver);
			//Step-7: Create 'Assignment' object 
			metadataCard.createAssignmentObject(driver,inputDataValues);
			Log.message("7. Successfully created Assignment from homePage.taskPanel.");

			//Wait for Page load
			Utils.waitForPageLoad(driver);
			//Wait for invisibility of loading image
			Utils.fluentWait(driver);

			//Step-8: Select Advanced Search Options
			homePage.searchPanel.showAdvancedSearchOptions(driver);
			//Select search options 
			if (!homePage.searchPanel.selectSearchOptionsUsingObject(driver, MFilesObjectList.ASSIGNMENT,"Assignments").contains("Assignments"))
				throw new Exception("Could not select Advanced Search options");
			Log.message("8. Successfully Selected Advanced Search Options");

			//Step-9:Verify if newly created object exists in search results
			String searchedFile=homePage.searchAFileName(inputDataValues.get("Title"));

			if (!homePage.isDataInListView(driver,inputDataValues.get("Type"),"Name") && searchedFile.isEmpty())
				throw new Exception("Test Failed..!!! Could not find the newly created Document.");

			Log.message("9. Searched with Filename: "+inputDataValues.get("Title"));

			if(!homePage.listView.getItemNameByItemIndex(0).contains(inputDataValues.get("Title")))
				throw new Exception("Test Failed..!!!Could not find object in Search list.");

			homePage.listView.clickItemByIndex(0);
			homePage.taskPanel.clickItem("Properties");
			Utils.waitForPageLoad(driver);
			Utils.fluentWait(driver);

			metadataCard=new MetadataCard(driver);

			if (metadataCard.getPropertyValue("Name or title").equals(inputDataValues.get("Title")))
				Log.pass("Test Case Passed. The Assignment Object was created with the expected properties.");
			else
				Log.fail("Test Case Failed. The Assignment Object was created, but not with the expected properties.", driver);

		}//End of try
		catch(Exception e){
			Log.exception(e, driver);
		}//End of Catch
		finally{
			//Close the browser
			driver.quit();
		}//End of finally
	}//End of TC_003

	/**
	 * TestCase ID: TC_004
	 * <br> Description: Create New 'Customer' From TaskPane</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_004",description="Create New 'Customer' From homePage.taskPanel.")
	public void TC_004(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception{

		//Instantiate the Webdriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" Create New 'Customer' From homePage.taskPanel.");
		Log.addTestRunMachineInfo(driver,currentDriver,context); //get execution environment details to print on Log
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);

		try{

			//Step-1: And now use this to visit M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			LoginPage loginPage=new LoginPage(driver);
			//Step-3: Login to Application
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			//Step-4 : Verify the Page title
			if (!driver.getTitle().contains("Web")) {
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			}
			Log.message("3. Verify the Page title after Successfully LoggedIn to Application.");

			Utils.fluentWait(driver);
			HomePage homePage=new HomePage(driver);

			//Step-5: Verify if TaskPane displayed
			if(!homePage.isTaskPaneDisplayed())
				throw new Exception("Task Pane is not displayed in HomePage.");

			Log.message("4. TaskPane displayed in HomePage.");
			//Step-6: Click 'Customer' link from task pane of HomePage
			Utils.fluentWait(driver);
			homePage.clickTaskPaneNewLink("Customer");
			Log.message("5. Clicked 'Customer' link from task pane of HomePage.");

			//Step-7: Verify the Metadatacard display
			if (!homePage.verifyMetadataCardDisplay("Customer"))
				throw new Exception("Failed. MetadataCard Not displayed. ");
			Log.message("6. MetadataCard displayed");
			Thread.sleep(2000);

			//Step-8: Verify the 'Customer' object creation
			MetadataCard metadataCard=new MetadataCard(driver);
			homePage=metadataCard.createCustomerObject(driver,inputDataValues);
			Log.message("7. Successfully created Customer from homePage.taskPanel.");

			Utils.waitForPageLoad(driver);
			Utils.fluentWait(driver);

			//Step-8: Select Advanced Search Options
			homePage.searchPanel.showAdvancedSearchOptions(driver);
			//Select search options 
			if (!homePage.searchPanel.selectSearchOptionsUsingObject(driver, MFilesObjectList.CUSTOMER,"Customers").contains("Customers"))
				throw new Exception("Could not select Advanced Search options");
			Log.message("8. Successfully Selected Advanced Search Options");

			//Step-9:Verify if newly created object exists in search results
			String searchedFile=homePage.searchAFileName(inputDataValues.get("Title"));

			if (!homePage.isDataInListView(driver,inputDataValues.get("Type"),"Name") && searchedFile.isEmpty())
				throw new Exception("Test Failed..!!! Could not find the newly created Customer.");
			Log.message("9. Searched with Filename: "+inputDataValues.get("Title"));

			if(!homePage.listView.getItemNameByItemIndex(0).contains(inputDataValues.get("Title")))
				Log.fail("Test Failed..!!!Could not find object in Search list.", driver);

			homePage.listView.clickItemByIndex(0);
			homePage.taskPanel.clickItem("Properties");
			Utils.waitForPageLoad(driver);
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			metadataCard=new MetadataCard(driver);

			if (metadataCard.getPropertyValue("Customer name").equals(inputDataValues.get("Title")))
				Log.pass("Test Case Passed. The Customer Object was created with the expected properties.");
			else
				Log.fail("Test Case Failed. The Customer Object was created, but not with the expected properties.", driver);

		}//End of try
		catch(Exception e){
			Log.exception(e, driver);
		}//End of catch
		finally{
			//Close the browser
			driver.quit();
		}//End of finally
	}

	/**
	 * TC005 : Create New 'Project' From TaskPane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Smoke"}, 
			description = "Create New 'Project' From TaskPane")
	public void TC_005(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = WebDriverUtils.getDriver();

		try {

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Select New Project from Taskpanel
			//--------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Project.Value); //Clicks New Assignment from the menu bar
			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("1. New Project is selected from taskpanel.", driver);

			//Step-2 : Enter the metadatacard details
			//---------------------------------------
			metadatacard.setInfo(dataPool.get("Properties"));
			ConcurrentHashMap <String, String> prevInfo = metadatacard.getInfo(); //Gets the property information			

			Log.message("2. New Project details are entered in metadatacard.", driver);

			//Step-3 : Save the metadatacard
			//------------------------------
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Project metadatacard is saved.", driver);

			//Step-4 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5 : Open the Properties dialog of the new project through context menu
			//------------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(prevInfo.get("Name or title"))) //Checks if Item exists in the list
				throw new Exception("Newly created project (" + prevInfo.get("Name or title") + ") does not exists in the list.");

			if (!homePage.listView.rightClickItem(prevInfo.get("Name or title"))) //Right clicks the project
				throw new Exception("Newly created project (" + prevInfo.get("Name or title") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("5. Properties dialog of the new project (" + prevInfo.get("Name or title") + ") is opened through context menu.", driver);

			//Step-6 : Obtain metadatacard information and compare the properties before and after saving metadatacard
			//---------------------------------------------------------------------------------------------------------
			ConcurrentHashMap <String, String> currInfo = metadatacard.getInfo(); //Gets the property information
			String diff = Utility.compareObjects(currInfo, prevInfo);

			Log.message("6. Current Project metadatacard details are obtained and compared with the entered values.");

			//Verification : Verifies if Mark Complete Icon is displayed
			//----------------------------------------------------------
			if (diff.equalsIgnoreCase(""))
				Log.pass("Test case Passed. New Project (" + prevInfo.get("Name or title") + ") is created successfully from taskpanel.");
			else
				Log.fail("Test case Failed. New Project (" + prevInfo.get("Name or title") + ") is creation is not successful. Additonal Information : " + diff, driver);

			metadatacard.closeMetadataCard(); //Closes Metadatacard

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_005

	/**
	 * TestCase ID: TC_006
	 * <br>Description: Search for a file containing text</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_006",description="Search for a fileName containing text")
	public void TC_006(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{
		//Instantiate Webdriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" Search for a fileName containing text.");
		Log.addTestRunMachineInfo(driver,currentDriver,context);//get execution environment details and print on Log report
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);

		try{

			// Step-1: And now use this to visit M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Step-2: Fetch Login details from Testdata sheet
			LoginPage loginPage=new LoginPage(driver);

			//Step-3: Login to Application
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			//Step-4: Verify the Page title
			if (!driver.getTitle().contains("Web")) {
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			}
			Log.message("3. Verified the Page title after Successfully LoggedIn to Application.");

			Utils.fluentWait(driver);
			HomePage homePage=new HomePage(driver);

			//Step-5: Verify if TaskPane displayed
			if(!homePage.isTaskPaneDisplayed())
				throw new Exception("Task pane not displayed");
			Log.message("4. Task Pane displayed in HomePage.");

			//6. Verify the display of SearchBar in HomePage
			if (!homePage.isSearchbarPresent(driver)) {
				throw new Exception("Search Panel not displayed.");
			}
			Log.message("5. Search Panel displayed in HomePage.");

			//Step-7: Search the object with its name
			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));
			Log.message("6. Search done for the object '"+inputDataValues.get("SearchTerm")+"'.");

			if (!homePage.isDataInListView(driver,inputDataValues.get("Type"),"Name") && searchedFile.isEmpty()) 
				Log.fail("Test Failed..!!!Object/File not found in Search results.", driver);
			else 
				Log.pass("Test Passed..!!!Search results contains the Object/File");
		}//End of try
		catch(Exception e) {
			Log.exception(e, driver);
		} //ENd of catch

		finally{
			//Close the browser
			driver.quit();
		}//End of finally
	}

	/**
	 * TC007 : Create New 'Document' From MenuBar
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Smoke"}, 
			description = "Create New 'Document' From MenuBar")
	public void TC_007(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = WebDriverUtils.getDriver();

		try {

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Select New Document from menubar
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Document.Value); //Clicks New Document from the menu bar
			Utils.fluentWait(driver);
			Utility.selectTemplate(dataPool.get("TemplateOrExtension"), driver);
			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("1. New Document is selected from menubar.", driver);

			//Step-2 : Enter the metadatacard details
			//---------------------------------------
			metadatacard.setInfo(dataPool.get("Properties"));
			ConcurrentHashMap <String, String> prevInfo = metadatacard.getInfo(); //Gets the property information			

			Log.message("2. New Document details are entered in metadatacard.", driver);

			//Step-3 : Save the metadatacard
			//------------------------------
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			String docName = "";

			if (dataPool.get("TemplateOrExtension").equalsIgnoreCase("Multi-File Document"))
				docName = prevInfo.get("Name or title");
			else
				docName = prevInfo.get("Name or title") + "." + prevInfo.get("Extension");

			Log.message("3. New Document metadatacard is saved.", driver);

			//Step-4 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5 : Open the Properties dialog of the new document through context menu
			//-----------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(docName)) //Checks if Item exists in the list
				throw new Exception("Newly created document (" + docName + ") does not exists in the list.");

			if (!homePage.listView.rightClickItem(docName)) //Right clicks the project
				throw new Exception("Newly created document (" + docName + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("5. Properties dialog of the new document (" + docName + ") is opened through context menu.", driver);

			//Step-6 : Obtain metadatacard information and compare the properties before and after saving metadatacard
			//---------------------------------------------------------------------------------------------------------
			ConcurrentHashMap <String, String> currInfo = metadatacard.getInfo(); //Gets the property information
			String diff = Utility.compareObjects(currInfo, prevInfo, "Extension");

			Log.message("6. Current Document metadatacard details are obtained and compared with the entered values.");

			//Verification : Verifies if Mark Complete Icon is displayed
			//----------------------------------------------------------
			if (diff.equalsIgnoreCase(""))
				Log.pass("Test case Passed. New Document (" + docName + ") is created successfully from taskpanel.");
			else
				Log.fail("Test case Failed. New Document (" + docName + ") is creation is not successful. Additonal Information : " + diff, driver);

			metadatacard.closeMetadataCard(); //Closes Metadatacard

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_007

	/**
	 * TestCase ID: TC_008
	 * <br.Description: Create New 'Assignment' From Menubar</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_008",description="Create New 'Assignment' From Menubar.")
	public void TC_008(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception{

		//Instantiate Webdriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" Create New 'Assignment' From Menubar.");
		Log.addTestRunMachineInfo(driver,currentDriver,context);//get the execution environment details and print on Log report
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);
		try{


			//Step-1: And now use this to visit M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Step-2: Login to Application
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),documentVault);
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			//Step-3:Verify the Page title
			HomePage homePage=new HomePage(driver);
			if (!driver.getTitle().contains("Web")&&!homePage.isLoggedIn(inputDataValues.get("UserName"))) 
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			Log.message("3. Verify the Page title after Successfully LoggedIn to Application.");

			Utils.fluentWait(driver);
			//Verify if Menubar displayed
			homePage.isMenubarDisplayed();
			Log.message("4. Menu Bar displayed");

			//Step-5:  Click 'Assignment' link from Menubar of HomePage
			homePage.menuBar.clickNewMenuItem(inputDataValues.get("ObjectType"));
			Utils.fluentWait(driver);
			Log.message("5. Clicked 'Assignment' link from menubar of HomePage.");

			//Step-6: Verify the Metadatacard display
			if (!homePage.verifyMetadataCardDisplay(inputDataValues.get("ObjectType")))
				throw new Exception("Failed. MetadataCard Not displayed. ");
			Log.message("6. MetadataCard displayed");
			Thread.sleep(2000);

			//Step-7: Verify creation of 'Assignment' object.
			MetadataCard metadataCard=new MetadataCard(driver);
			homePage=metadataCard.createAssignmentObject(driver,inputDataValues);
			Log.message("7. Successfully created Assignment from Menubar.");

			Utils.waitForPageLoad(driver);
			Utils.fluentWait(driver);

			//Step-8: Select Advanced Search Options
			homePage.searchPanel.showAdvancedSearchOptions(driver);
			//Select search options 
			if (!homePage.searchPanel.selectSearchOptionsUsingObject(driver, MFilesObjectList.ASSIGNMENT,"Assignments").contains("Assignments"))
				throw new Exception("Could not select Advanced Search options");
			Log.message("8. Successfully Selected Advanced Search Options");

			//Step-9:Verify if newly created object exists in search results
			String searchedFile=homePage.searchAFileName(inputDataValues.get("Title"));

			if (!homePage.isDataInListView(driver,inputDataValues.get("Type"),"Name") && searchedFile.isEmpty())
				throw new Exception("Test Failed..!!! Could not find the newly created Assignment.");

			if(!homePage.listView.getItemNameByItemIndex(0).contains(inputDataValues.get("Title")))
				throw new Exception("Test Failed..!!!Could not find 'Document' of '"+inputDataValues.get("TemplateName")+"' in Search list.");

			homePage.listView.clickItemByIndex(0);
			homePage.taskPanel.clickItem("Properties");
			Utils.waitForPageLoad(driver);
			Utils.fluentWait(driver);
			metadataCard=new MetadataCard(driver);

			if (!metadataCard.getPropertyValue("Name or title").equalsIgnoreCase(inputDataValues.get("Title"))) 
				Log.fail("Test Failed..!!!Could not find the newly created Assignment.", driver);
			else 
				Log.pass("Test Passed..!!!Newly created Assignment is Found in the List.");
		}//End of try
		catch(Exception e){
			Log.exception(e, driver);
		}//End of catch
		finally{ 
			//Close the browser
			driver.quit();
		}//End of finally
	}

	/**
	 * TestCase ID: TC_009
	 * <br>Description: Create New 'Customer' From Menubar</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_009",description="Create New 'Customer' From Menubar.")
	public void TC_009(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception{

		//Instantiate Webdriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" Create New 'Customer' From Menubar.");
		Log.addTestRunMachineInfo(driver,currentDriver,context);//get the execution environment details and print on log report
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);
		try{


			//Step-1: And now use this to visit M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");
			LoginPage loginPage=new LoginPage(driver);

			//Step-3: Login to Application
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),documentVault);
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			//Step-4: Verify the Page title
			if (!driver.getTitle().contains("Web")) {
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			}
			Log.message("3. Verified the Page title after Successfully LoggedIn to Application.");

			Utils.fluentWait(driver);
			HomePage homePage=new HomePage(driver);

			homePage.isMenubarDisplayed();
			Log.message("4. Menu Bar displayed");

			//Step-5:  Click 'Assignment' link from Menubar of HomePage
			homePage.menuBar.clickNewMenuItem(inputDataValues.get("ObjectType"));
			Utils.fluentWait(driver);
			Log.message("5. Clicked 'Assignment' link from menubar of HomePage.");

			Log.message("5. Clicked 'Customer' link from menubar of HomePage.");

			//Step-6: Verify the Metadatacard display
			if (!homePage.verifyMetadataCardDisplay("Customer"))
				throw new Exception("Failed. MetadataCard Not displayed. ");
			Log.message("6. MetadataCard displayed");
			Thread.sleep(2000);

			//Step-7: Verify creation of 'Customer' object
			MetadataCard metadataCard=new MetadataCard(driver);
			homePage=metadataCard.createCustomerObject(driver,inputDataValues);
			Log.message("7. Successfully created Customer from Menubar.");

			Utils.waitForPageLoad(driver);
			Utils.fluentWait(driver);

			//Step-8: Select Advanced Search Options
			homePage.searchPanel.showAdvancedSearchOptions(driver);
			//Select the search options	
			if (!homePage.searchPanel.selectSearchOptionsUsingObject(driver, MFilesObjectList.CUSTOMER,"Customers").contains("Customers")) 
				throw new Exception("Could not select Advanced Search options");
			Log.message("8. Successfully Selected Advanced Search Options");

			//Step-9: Verify if newly created object displayed in the search results
			String searchedFile=homePage.searchAFileName(inputDataValues.get("Title"));
			if (!homePage.isDataInListView(driver,inputDataValues.get("Type"),"Name") && searchedFile.isEmpty()) 
				throw new Exception("Test Failed..!!! Could not find the newly created Customer.");

			if(!homePage.listView.getItemNameByItemIndex(0).contains(inputDataValues.get("Title")))
				throw new Exception("Test Failed..!!!Could not find 'Document' of '"+inputDataValues.get("TemplateName")+"' in Search list.");

			homePage.listView.clickItemByIndex(0);
			homePage.taskPanel.clickItem("Properties");
			Utils.waitForPageLoad(driver);
			Utils.fluentWait(driver);
			metadataCard=new MetadataCard(driver);

			if (!metadataCard.getPropertyValue("Customer name").equalsIgnoreCase(inputDataValues.get("Title"))) 
				Log.fail("Test Failed..!!!Could not find the newly created Customer.", driver);
			else 
				Log.pass("Test Passed..!!!Newly created Customer is Found in the List.");

		}//End of try
		catch(Exception e){
			Log.exception(e, driver);
		}//End of catch
		finally{
			//Close the browser
			driver.quit();
		}//End of finally
	}

	/**
	 * TC010 : Create New 'Project' From Menubar
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Smoke"}, 
			description = "Create New 'Project' From Menubar")
	public void TC_010(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = WebDriverUtils.getDriver();

		try {

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Select New Project from Menubar
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Project.Value); //Clicks New Project from the menu bar
			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("1. New Project is selected from menubar.", driver);

			//Step-2 : Enter the metadatacard details
			//---------------------------------------
			metadatacard.setInfo(dataPool.get("Properties"));
			ConcurrentHashMap <String, String> prevInfo = metadatacard.getInfo(); //Gets the property information			

			Log.message("2. New Project details are entered in metadatacard.", driver);

			//Step-3 : Save the metadatacard
			//------------------------------
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Project metadatacard is saved.", driver);

			//Step-4 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5 : Open the Properties dialog of the new project through context menu
			//------------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(prevInfo.get("Name or title"))) //Checks if Item exists in the list
				throw new Exception("Newly created project (" + prevInfo.get("Name or title") + ") does not exists in the list.");

			if (!homePage.listView.rightClickItem(prevInfo.get("Name or title"))) //Right clicks the project
				throw new Exception("Newly created project (" + prevInfo.get("Name or title") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("5. Properties dialog of the new project (" + prevInfo.get("Name or title") + ") is opened through context menu.", driver);

			//Step-6 : Obtain metadatacard information and compare the properties before and after saving metadatacard
			//---------------------------------------------------------------------------------------------------------
			ConcurrentHashMap <String, String> currInfo = metadatacard.getInfo(); //Gets the property information
			String diff = Utility.compareObjects(currInfo, prevInfo);

			Log.message("6. Current Project metadatacard details are obtained and compared with the entered values.");

			//Verification : Verifies if Mark Complete Icon is displayed
			//----------------------------------------------------------
			if (diff.equalsIgnoreCase(""))
				Log.pass("Test case Passed. New Project (" + prevInfo.get("Name or title") + ") is created successfully from menubar.");
			else
				Log.fail("Test case Failed. New Project (" + prevInfo.get("Name or title") + ") is creation is not successful. Additonal Information : " + diff, driver);

			metadatacard.closeMetadataCard(); //Closes Metadatacard

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_010

	/**
	 * TestCase ID: TC_011
	 * <br>Description: Create New 'Document Collection' From Menubar</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_011",description="Create New 'Document Collection' From Menubar.")
	public void TC_011(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception{
		//Instantiate Webdriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" Create New 'Document Collection' From Menubar.");
		Log.addTestRunMachineInfo(driver,currentDriver,context);//get execution environment details and prints on log report
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);

		try{

			//Step-1: Launch M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Step-3: Login to Application
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			//Step-4: Verify the Page title
			if (!driver.getTitle().contains("Web")) 
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			Log.message("3. Verified the Page title after Successfully LoggedIn to Application.");

			Utils.fluentWait(driver);
			HomePage homePage=new HomePage(driver);
			//Verify if MenuBar displayed
			homePage.isMenubarDisplayed();
			Log.message("4. Menu Bar displayed");

			//Step-5: Click 'Document Collection' link from Menubar of HomePage
			homePage.menuBar.clickNewMenuItem(inputDataValues.get("ObjectType"));
			Utils.fluentWait(driver);
			Log.message("5. Clicked 'Document Collection' link from menubar of HomePage.");

			//Step-6: Verify the Metadatacard display
			if (!homePage.verifyMetadataCardDisplay("Document collection"))
				throw new Exception("Failed. MetadataCard Not displayed. ");
			Log.message("6. MetadataCard displayed");
			Thread.sleep(2000);

			//Step-7: Verify creation of 'Document collection' object
			MetadataCard metadataCard=new MetadataCard(driver);
			homePage=metadataCard.createDocumentCollectionObject(driver,inputDataValues);
			Utils.fluentWait(driver);
			Log.message("7. Successfully created Document Collection from Menubar.");

			//Step-8: Verify if newly created 'Document collection' exists in Recently accessed list
			if (!homePage.navigateToGotoViews("Recently Accessed by Me"))
				throw new Exception("Could not navigate to 'Recently Accessed by Me' view");
			Log.message("8. Successfully navigated to 'Recently Accessed by Me' view.");

			if (!homePage.isDataInListView(driver,inputDataValues.get("Title"),"Name")) 
				throw new Exception("Test Failed..!!! Could not find the newly created Document.");

			if(!homePage.listView.getItemNameByItemIndex(0).contains(inputDataValues.get("Title")))
				throw new Exception("Test Failed..!!!Could not find 'Document' of '"+inputDataValues.get("TemplateName")+"' in Search list.");

			homePage.listView.clickItemByIndex(0);
			homePage.taskPanel.clickItem("Properties");
			Utils.waitForPageLoad(driver);
			Utils.fluentWait(driver);
			metadataCard=new MetadataCard(driver);

			if (!metadataCard.getPropertyValue("Name or title").equalsIgnoreCase(inputDataValues.get("Title"))) 
				Log.fail("Test Failed..!!!Could not find the newly created Document Collection.", driver);
			else 
				Log.pass("Test Passed..!!!Newly created Document Collection is Found in the List.");
		}//End of try
		catch(Exception e){
			Log.exception(e, driver);
		}//End of catch
		finally{
			//Close the browser
			driver.quit();
		}//End of finally
	}

	/**
	 * TestCase ID: TC_012
	 * <br>Description: Create New 'Employee' From Menubar</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_012",description="Create New 'Employee' From Menubar.")
	public void TC_012(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception{
		//Instantiate Webdriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" Create New 'Employee' From Menubar.");
		Log.addTestRunMachineInfo(driver,currentDriver,context);//get the execution environment details and prints on Log report
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);

		try{

			//Step-1:  And now use this to visit M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Step-3: Login to Application
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			//Step-4: Verify the Page title
			HomePage homePage=new HomePage(driver);
			if (!driver.getTitle().contains("Web")&&!homePage.isLoggedIn(inputDataValues.get("UserName"))) 
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			Log.message("3. Verified the Page title after Successfully LoggedIn to Application.");

			//Verify if MenuBar displayed
			Utils.fluentWait(driver);
			homePage.isMenubarDisplayed();
			Log.message("4. Menu Bar displayed");

			//Step-5:  Click 'Employee' link from Menubar of HomePage
			homePage.menuBar.clickNewMenuItem(inputDataValues.get("ObjectType"));
			Utils.fluentWait(driver);
			Log.message("5. Clicked 'Employee' link from menubar of HomePage.");

			//Step-6: Verify the Metadatacard display
			if (!homePage.verifyMetadataCardDisplay("Employee")) {
				throw new Exception("Failed. MetadataCard Not displayed. ");
			}
			Log.message("6. MetadataCard displayed");
			Thread.sleep(2000);

			//Step-7: Verify creation of 'Employee' object
			MetadataCard metadataCard=new MetadataCard(driver);
			// homePage=metadataCard.createEmployeeObject(driver,inputDataValues);
			Log.message("7. Successfully Created Employee from Menubar.");

			Utils.waitForPageLoad(driver);
			Utils.fluentWait(driver);

			//Step-8: Select Advanced Search Options
			homePage.searchPanel.showAdvancedSearchOptions(driver);
			//Select search options 
			if (!homePage.searchPanel.selectSearchOptionsUsingObject(driver, MFilesObjectList.EMPLOYEE,"Employees").contains("Employees"))
				throw new Exception("Could not select Advanced Search options");
			Log.message("8. Successfully Selected Advanced Search Options");

			//Step-9:Verify if newly created object exists in search results
			String searchedFile=homePage.searchAFileName(inputDataValues.get("Title"));

			if (!homePage.isDataInListView(driver,inputDataValues.get("Type"),"Name") && searchedFile.isEmpty())
				throw new Exception("Test Failed..!!! Could not find the newly created Employee.");
			Log.message("9. Searched with Filename: "+inputDataValues.get("Title"));


			if(!homePage.listView.getItemNameByItemIndex(0).contains(inputDataValues.get("Title")))
				throw new Exception("Test Failed..!!!Could not find 'Document' of '"+inputDataValues.get("TemplateName")+"' in Search list.");

			homePage.listView.clickItemByIndex(0);
			homePage.taskPanel.clickItem("Properties");
			Utils.waitForPageLoad(driver);
			Utils.fluentWait(driver);
			metadataCard=new MetadataCard(driver);
			if (!metadataCard.getPropertyValue("Employee name").equalsIgnoreCase(inputDataValues.get("Title"))) 
				Log.fail("Test Failed..!!!Could not find the newly created Employee.", driver);
			else 
				Log.pass("Test Passed..!!!Newly created Employee is Found in the List.");
		}//End of try
		catch(Exception e){
			Log.exception(e, driver);
		}//End of catch
		finally{
			//Close the browser
			driver.quit();
		}//End of finally
	}

	/**
	 * TC013 : Create New 'Contact person' From Menubar
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Smoke"}, 
			description = "Create New 'Contact person' From Menubar")
	public void TC_013(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = WebDriverUtils.getDriver();

		try {

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Select New Contact person from Menubar
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.ContactPerson.Value); //Clicks New Project from the menu bar
			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("1. New Contact person is selected from menubar.", driver);

			//Step-2 : Enter the metadatacard details
			//---------------------------------------
			metadatacard.setInfo(dataPool.get("Properties"));
			ConcurrentHashMap <String, String> prevInfo = metadatacard.getInfo(); //Gets the property information

			Log.message("2. New Contact person details are entered in metadatacard.", driver);

			//Step-3 : Save the metadatacard
			//------------------------------
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			String cpName = prevInfo.get("First name") + " " + prevInfo.get("Last name");

			Log.message("3. New Contact person metadatacard is saved.", driver);

			//Step-4 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5 : Open the Properties dialog of the new Contact person through context menu
			//------------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(cpName)) //Checks if Item exists in the list
				throw new Exception("Newly created Contact person (" + cpName + ") does not exists in the list.");

			if (!homePage.listView.rightClickItem(cpName)) //Right clicks the project
				throw new Exception("Newly created Contact person (" + cpName + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("5. Properties dialog of the new Contact person (" + cpName + ") is opened through context menu.", driver);

			//Step-6 : Obtain metadatacard information and compare the properties before and after saving metadatacard
			//---------------------------------------------------------------------------------------------------------
			ConcurrentHashMap <String, String> currInfo = metadatacard.getInfo(); //Gets the property information
			String diff = Utility.compareObjects(currInfo, prevInfo, "Full name, Owner (Customer)");

			Log.message("6. Current Contact person metadatacard details are obtained and compared with the entered values.");

			//Verification : Verifies if Mark Complete Icon is displayed
			//----------------------------------------------------------
			if (diff.equalsIgnoreCase(""))
				Log.pass("Test case Passed. New Contact person (" + cpName + ") is created successfully from menubar.");
			else
				Log.fail("Test case Failed. New Contact person (" + cpName + ") is creation is not successful. Additonal Information : " + diff, driver);

			metadatacard.closeMetadataCard(); //Closes Metadatacard

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_013

	/**
	 * TestCase ID: TC_014
	 * <br>Description: Create New 'Report' From Menubar</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_014",description="Create New 'Report' From Menubar.")
	public void TC_014(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception{

		//Instantiate the Webdriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" Create New 'Report' From Menubar.");
		Log.addTestRunMachineInfo(driver,currentDriver,context);//get the execution environment details and print on Log report
		ConcurrentHashMap <String, String> inputDataValues= new ConcurrentHashMap <String, String>(dataValues);

		try{

			//Step-1: And now use this to visit M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Step:3 Login to Application
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			//Instantiate the HomePage object
			HomePage homePage=new HomePage(driver);
			//Step:4 Verify the Page title
			if (!driver.getTitle().contains("Web")&& !homePage.isLoggedIn(inputDataValues.get("UserName"))) 
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			Log.message("3. Verified the Page title after Successfully LoggedIn to Application.");

			//verify if menubar displayed
			Utils.fluentWait(driver);
			homePage.isMenubarDisplayed();
			Log.message("4. Menu Bar displayed");

			//Step-5: Click 'Report' link from Menubar of HomePage
			homePage.menuBar.clickNewMenuItem(inputDataValues.get("ObjectType"));
			Utils.fluentWait(driver);
			Log.message("5. Click 'Report' link from menubar of HomePage.");

			if (!homePage.verifyMetadataCardDisplay("Report"))
				throw new Exception("Failed. MetadataCard Not displayed. ");
			Log.message("6. MetadataCard displayed");

			//Step:6 Verify the Metadatacard display and create the Report from menubar options
			MetadataCard metadataCard=new MetadataCard(driver);
			//  homePage=metadataCard.createReportObject(driver,inputDataValues);
			Log.message("6. Successfully Created 'Report' from Menubar.");

			//Wait for the Page load
			Utils.waitForPageLoad(driver);
			//Wait for the Invisibility of the loading Image
			Utils.fluentWait(driver);

			//Step-8: Select Advanced Search Options
			homePage.searchPanel.showAdvancedSearchOptions(driver);
			//Select search options 
			if (!homePage.searchPanel.selectSearchOptionsUsingObject(driver, MFilesObjectList.REPORT,"Reports").contains("Reports"))
				throw new Exception("Could not select Advanced Search options");
			Log.message("8. Successfully Selected Advanced Search Options");

			//Step-9:Verify if newly created object exists in search results
			String searchedFile=homePage.searchAFileName(inputDataValues.get("Title"));

			if (!homePage.isDataInListView(driver,inputDataValues.get("Type"),"Name") && searchedFile.isEmpty())
				throw new Exception("Test Failed..!!! Could not find the newly created Report.");
			Log.message("9. Searched with Filename: "+inputDataValues.get("Title"));


			if(!homePage.listView.getItemNameByItemIndex(0).contains(inputDataValues.get("Title")))
				throw new Exception("Test Failed..!!!Could not find newly created 'Report' in Search list.");

			homePage.listView.clickItemByIndex(0);
			homePage.taskPanel.clickItem("Properties");
			Utils.waitForPageLoad(driver);
			Utils.fluentWait(driver);
			metadataCard=new MetadataCard(driver);

			if (!metadataCard.getPropertyValue("Name or title").equalsIgnoreCase(inputDataValues.get("Title")))
				Log.fail("Test Failed!!!..Could not find the newly created 'Reports'.", driver);
			else 
				Log.pass("Test Passed!!!..Newly created 'Reports' is Found in the List.");	

		}//End of try
		catch(Exception e){
			Log.exception(e, driver);
		}//End of catch
		finally{
			//Close the browser
			driver.quit();
		}//End of finally
	}

	/**
	 * TestCase ID: TC_015
	 * <br>Description: Basic Search with different Options</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_015",description="Advanced Search with 'Search Only : Employees' Options")
	public void TC_015(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{
		//Instantiate Webdriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" Advanced Search with 'Search Only : Employees' Options");
		Log.addTestRunMachineInfo(driver,currentDriver,context);//get execution environment details and print on Log report

		try{
			//Fetch testdata from Excel Data sheet
			ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1: And now use this to visit M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Step-3: Login to Application
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			//Step-4:  Verify the Page title
			if (!driver.getTitle().contains("Web")) {
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			}
			Log.message("4. Verify the Page title after Successfully LoggedIn to Application.");

			Utils.fluentWait(driver);
			HomePage homePage=new HomePage(driver);
			//Step-5: Verify the display of SearchBar in HomePage
			if (!homePage.isSearchbarPresent(driver))
				throw new Exception("Search Panel not displayed.");
			Log.message("5. Search Panel displayed on HomePage.");

			Utils.fluentWait(driver);
			//Step-6: Select Advanced Search Options

			Log.message("6.Select Advanced Search options.");
			homePage.searchPanel.showAdvancedSearchOptions(driver);
			if (!homePage.searchPanel.selectSearchOptionsUsingObject(driver, MFilesObjectList.EMPLOYEE,"Employees").contains("Employees")) 
				throw new Exception("Could not select Advanced Search options");
			Log.message("Successfully Selected Advanced Search Options");
			String searchedFile= homePage.searchAFileName(inputDataValues.get("SearchTerm"));

			//Step-7: Verify if Search results Contains Employees
			if (!homePage.isSearchResults(driver, inputDataValues.get("Type"))&& searchedFile.isEmpty()) 
				Log.fail("Test failed..Newly created 'Employee' is not found in search.", driver);
			else 
				Log.pass("Test Passed..!!!Search results contains Employees");
		}//End of try
		catch(Exception e){
			Log.exception(e, driver);
		}//End of catch
		finally{
			//Close the browser
			driver.quit();
		}//End of finally
	}

	/**
	 * TestCase ID: TC_016
	 * <br>Description: 'Rename' the Document</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_016",description="Rename the Document using context menu options")
	public void TC_016(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{
		//Instantiate Webdriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" Rename the Document using context menu options");
		Log.addTestRunMachineInfo(driver,currentDriver,context);//get the execution environment details and print on Log report
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);


		try{
			//Step-1:  And now use this to visit M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Step-3:  Login to Application
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);

			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));
			//Step-4: Verify the Page title
			if (!driver.getTitle().contains("Web")) {
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			}
			Log.message("3. Verified Page title after Successfully LoggedIn to Application.");

			Utils.fluentWait(driver);
			HomePage homePage=new HomePage(driver);

			//Step-5: verify is user loggedIn to application
			if (!homePage.isLoggedIn(inputDataValues.get("UserName")))
				throw new Exception("Could not login to application due to some problems");
			Log.message("4. Successfully Logged in to application.");

			//Step-8: Select Advanced Search Options
			homePage.searchPanel.showAdvancedSearchOptions(driver);
			//Select search options 
			if (!homePage.searchPanel.selectSearchOptionsUsingObject(driver, MFilesObjectList.EMPLOYEE,"Employees").contains("Employees"))
				throw new Exception("Could not select Advanced Search options");
			Log.message("8. Successfully Selected Advanced Search Options");

			//Step-9:Verify if newly created object exists in search results
			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));

			if (!homePage.isDataInListView(driver,inputDataValues.get("Type"),"Name") && searchedFile.isEmpty())
				throw new Exception("Test Failed..!!! Could not find the newly created Document.");
			Log.message("9. Searched with Filename: "+inputDataValues.get("Title"));

			if(!homePage.listView.getItemNameByItemIndex(0).contains(inputDataValues.get("Title")))
				throw new Exception("Test Failed..!!!Could not find 'Document' of '"+inputDataValues.get("Type")+"' in Search list.");

			//Select the searched file
			homePage.listView.clickItemByIndex(0);

			//check if object is checkedOut
			if (homePage.isObjectCheckedOut("Checked Out To",inputDataValues.get("UserName"))) {
				if (!homePage.openContextMenuDialog(driver)) 
					throw new Exception("Unable to display ContextMenu options");
				if (!homePage.undoCheckOutObject("contextMenu"))
					throw new Exception("Unable to perform 'Undo CheckOut' for already checkedOut object");
			}

			//Step-6: 'Rename' the Document using ContextMenu option
			if (!homePage.openContextMenuDialog(driver))
				throw new Exception("Unable to Open context Menu options");
			Log.message("5. Successfully displayed context Menu options.");

			if (!homePage.renameObjectsFromListView("test",inputDataValues.get("SearchTerm"),"contextMenu")) 
				Log.fail("Rename Failed..!!!!", driver);
			else 
				Log.pass("Test Passed..!!!Rename Success..!!!!");
		}	        //End of try
		catch(Exception e){
			Log.exception(e, driver);
		}//End of catch

		finally{
			//Close the browser
			driver.quit(); 
		}//End of finally
	}

	/**
	 * TestCase ID: TC_017
	 * <br>Description: 'Delete' the Document</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_017",description="Delete the Document using context menu options")
	public void TC_017(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{
		//Instantiate Webdriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" Delete the Document using context menu options");
		Log.addTestRunMachineInfo(driver,currentDriver,context);//get the execution environment details and print on Log report
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);

		try{

			//Step-1: And now use this to visit M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			LoginPage loginPage=new LoginPage(driver);
			//Step-3: Login to Application
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));
			//Step-4:  Verify the Page title
			if (!driver.getTitle().contains("Web")) {
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			}
			Log.message("3.  Verify the Page titleSuccessfully LoggedIn to Application.");

			Utils.fluentWait(driver);
			HomePage homePage=new HomePage(driver);

			//Step-5: Select the Search options
			Log.message("5.Search for'Document' to 'Delete' it.");
			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));
			if (!homePage.isSearchResults(driver,inputDataValues.get("Type")) && searchedFile.isEmpty())	{
				throw new Exception("Error!!.Could not search the file.");
			}
			Log.message("Search Done Successfully.");

			//Step-6: 'Delete' the Document using ContextMenu option
			Log.message("6. 'Delete' the Document using ContextMenu option.");
			if (!homePage.openContextMenuDialog(driver)) {
				throw new Exception("Unable to Open context Menu options");
			}
			Log.message("Successfully displayed context Menu options.");
			//Step-7: Confirm the Delete Operation
			Log.message("7. Confirm the Delete Operation");
			if (!homePage.deleteObjectFromListView("contextMenu")) 
				Log.fail("'Delete' operation Failed from context Menu..!!!!", driver);
			else 
				Log.pass("Test Passed..!!!'Delete' operation Success from context Menu..!!!!");
		}//End of try
		catch(Exception e){
			Log.exception(e, driver);
		}//End of catch
		finally{
			//Close the browser
			driver.quit(); 
		}//End of finally
	}

	/**
	 * TestCase ID: TC_018
	 * <br>Description: 'CheckOut' a Document using context Menu</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_018",description="'CheckOut' a Document using context Menu")
	public void TC_018(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{
		//Instantiate Webdirver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" 'CheckOut' a Document using context Menu");
		Log.addTestRunMachineInfo(driver,currentDriver,context);//get the execution environment details and print log report
		HomePage homePage=null;
		try{
			//Fetch test data from Excel sheet
			ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);
			//Step-1: And now use this to visit M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Step-3: Login to Application
			LoginPage loginPage=new LoginPage(driver);
			Log.message("3. Login to the application using valid credentials.");
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);

			Log.message("Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));
			//Step-4:  Verify the Page title
			Log.message("4. Verify the Page title.");

			if (!driver.getTitle().contains("Web")) {
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			}
			Log.pass("---Successfully LoggedIn to Application.");

			Utils.fluentWait(driver);
			homePage=new HomePage(driver);

			Log.message("5. Verify the display of SearchBar in HomePage.");
			if (!homePage.isSearchbarPresent(driver))
				throw new Exception("Search Panel not displayed.");
			Log.message("---Search Panel displayed on HomePage.");
			Utils.fluentWait(driver);

			//Step-6: Search an object and perform 'CheckOut' operation using 'Context Menu'
			Log.message("6. Enter Search Term as "+inputDataValues.get("SearchTerm")+" and perform 'CheckOut' operation using 'Context Menu'.");
			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));

			if(!homePage.isDataInListView(driver,inputDataValues.get("SearchTerm"),"Name")&&searchedFile.isEmpty())
				throw new Exception("Searched object doesnot exists");

			//Read List view column Headers
			if (!homePage.readListViewHeaderNames(driver,"Checked Out To")) {	        
				//Insert New Column to List view 
				homePage.clickAndInsertListViewColumns(driver,"Insert Column->Standard Columns->Checked Out To");
				//Verify if newly inserted column exists
				if (!homePage.readListViewHeaderNames(driver,"Checked Out To")) 
					throw new Exception("'Checked Out To' Column Not added.");
				Log.message("'Checked Out To' Column inserted in the List view.");
			}//End of if..loop

			//Step-7: Verify if Object checkedOut
			if (homePage.isObjectCheckedOut("Checked Out To",inputDataValues.get("UserName"))) {
				if (!homePage.openContextMenuDialog(driver)) {
					throw new Exception("Unable to display ContextMenu options");
				}
				if (!homePage.undoCheckOutObject("contextMenu"))
					throw new Exception("Unable to perform 'Undo CheckOut' for already checkedOut object");
			}//End of if..loop

			Log.message("7. CheckOut the Document using ContextMenu option.");
			if (!homePage.openContextMenuDialog(driver)) 
				throw new Exception("Unable to display ContextMenu options");

			Log.message("Successfully displayed ContextMenu options.");
			if (homePage.checkOutObjectFromContextMenu())  
				Log.pass("Test Passed. Document 'CheckedOut' Successfully using context Menu..!!!!");
			else
				Log.fail("Test Failed. Could not 'CheckOut' the document from context menu..!!!!", driver);

		}//End of try
		catch(Exception e){
			Log.exception(e, driver);
		}//End of catch
		finally{
			homePage=new HomePage(driver);
			homePage.openContextMenuDialog(driver);
			homePage.checkInObjectFromContextMenu();
			//Close the browser
			driver.quit(); 
		}//End of finally
	}

	/**
	 * TestCase ID: TC_019
	 * <br>Description: 'CheckIn' the Document using context Menu</br>	
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_019",description="'CheckIn' the Document using context Menu")
	public void TC_019(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{
		//Instantiate Webdriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" 'CheckIn' the Document using context Menu");
		Log.addTestRunMachineInfo(driver,currentDriver,context);//get execution environment details and print the log report
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);

		try{
			//Step-1: And now use this to visit M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Step-2: Login to Application
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			//Step-4: Verify the Page title
			if (!driver.getTitle().contains("Web")) 
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			Log.message("3. Verified the Page title after Successfully LoggedIn to Application.");

			Utils.fluentWait(driver);
			HomePage homePage=new HomePage(driver);

			//Step-5: Verify the display of SearchBar in HomePage
			if (!homePage.isSearchbarPresent(driver)) 
				throw new Exception("Search Panel not displayed.");
			Log.message("4. Search Panel displayed on HomePage.");
			Utils.fluentWait(driver);

			//Step-6: Search the object and 'CheckIn' using Context menu options
			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));
			if(searchedFile.isEmpty()) 
				throw new Exception("Could not find document in search results.");
			Log.message("5. '"+inputDataValues.get("SearchTerm")+"' object Found in Search Results.");

			if(!homePage.isRightPaneHidden())
				throw new Exception("Unable to Hide right tab panes");

			//Read List view column Headers
			if (!homePage.readListViewHeaderNames(driver,"Checked Out To")) {	        
				//Insert New Column to List view 
				homePage.clickAndInsertListViewColumns(driver,"Insert Column->Standard Columns->Checked Out To");
				//Verify if newly inserted column exists
				if (!homePage.readListViewHeaderNames(driver,"Checked Out To")) 
					throw new Exception("'Checked Out To' Column Not added.");
			}
			else 
				Log.message("6. 'Checked Out To' Column already added to List View.");

			//Step-7: Verify if Object is CheckedOut
			if (!homePage.isObjectCheckedOut("Checked Out To",inputDataValues.get("UserName"))) {
				if (!homePage.openContextMenuDialog(driver)) 
					throw new Exception("Unable to display ContextMenu options");
				if (!homePage.checkOutObjectFromContextMenu()) 
					throw new Exception("Could not 'CheckOut' the document from context menu..!!!!");
			}
			Log.message("7. Verified if Object is CheckedOut");
			//Step-8: 'CheckIn' the Document using ContextMenu option
			if (!homePage.openContextMenuDialog(driver)) 
				throw new Exception("Unable to display ContextMenu options");
			Log.message("8. Successfully displayed ContextMenu options.");

			//Step-9: Verify if 'CheckIn' operation success
			if (!homePage.checkInObjectFromContextMenu()) 
				Log.fail("'CheckIn' operation Failed..!!!!", driver);
			else 
				Log.pass("Test Passed..!!!'CheckIn' operation Success..!!!!");

		}
		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally{
			//Close the browser
			driver.quit(); 
		}
	}

	/**
	 * TestCase ID: TC_020
	 * <br>Description: 'CheckIn' the Document with Comments using context Menu</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_020",description="CheckIn Document with Comments using context Menu")
	public void TC_020(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{
		//Instantiate Webdriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" 'CheckIn' the Document with Comments using context Menu");
		Log.addTestRunMachineInfo(driver,currentDriver,context);//get the execution environment details and print Log Report
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);

		try{
			//Step-1: And now use this to visit M-Files Application
			driver.get(webSite);
			Log.message("1. Launch the application.");

			//Step-3: Login to Application
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));
			//Step-4: Verify the Page title
			if (!driver.getTitle().contains("Web")) {
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			}
			Log.message("3. Verified the Page title after Successfully LoggedIn to Application.");

			Utils.fluentWait(driver);
			HomePage homePage=new HomePage(driver);

			//Step-5: Verify the display of SearchBar in HomePage
			if (!homePage.isSearchbarPresent(driver))
				throw new Exception("Search Panel not displayed.");
			Log.message("4. Search Panel displayed on HomePage.");
			Utils.fluentWait(driver);

			//Step-6: Search the Object and CheckIn document with COmments
			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));
			if (!homePage.isSearchResults(driver,inputDataValues.get("Type")) && searchedFile.isEmpty()) {
				throw new Exception("Document could not find in Search list.");
			}
			Log.message("5. Object '"+inputDataValues.get("SearchTerm")+"' found in search results.");

			//Read List view column Headers
			if (!homePage.readListViewHeaderNames(driver,"Checked Out To"))
			{	        
				//Insert New Column to List view 
				homePage.clickAndInsertListViewColumns(driver, "Insert Column->Standard Columns->Checked Out To");
				//Verify if newly inserted column exists
				if (!homePage.readListViewHeaderNames(driver,"Checked Out To"))
					throw new Exception("'Checked Out To' Column Not added.");
			}
			Log.message("6. 'Checked Out To' Column already added to List View.");

			//Step-8: CheckOut the Document using ContextMenu option
			if (!homePage.isObjectCheckedOut("Checked Out To",inputDataValues.get("UserName")))
			{
				//throw new Exception("Document is not checkedOut..So Unable to Check-In");
				if (!homePage.openContextMenuDialog(driver))
					throw new Exception("Unable to display ContextMenu options");
				if (!homePage.checkOutObjectFromContextMenu())
					throw new Exception("Could not 'CheckOut' the document from context menu..!!!!");
			}

			Log.message("7. Checked Out the Document using ContextMenu option.");

			//Step-9: 'CheckIn' the Document with Comments using ContextMenu option
			Log.message("8. 'CheckIn' the Document with Comments using ContextMenu option.");
			if (!homePage.openContextMenuDialog(driver))
				throw new Exception("Unable to display ContextMenu options");
			Log.message("9. Successfully displayed ContextMenu options.");

			if (!homePage.checkInObjectWithCommentsFromContextMenu(inputDataValues.get("Comments"))) 
				Log.fail("'CheckIn with Comments' operation Failed..!!!!", driver);
			else 
				Log.pass("Test Passed..!!!'CheckIn with Comments' operation Success..!!!!");

		}//End of try
		catch(Exception e){
			Log.exception(e, driver);
		}//End of catch
		finally{
			//Close the browser
			driver.quit(); 
		}//End of finally
	}

	/**
	 * TestCase ID: TC_021
	 * <br>Description: Hide 'Go To' options on TaskPane using Configuration Page</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_021",description="Hide 'Go To' options on TaskPane using Configuration Page")
	public void TC_021(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception {

		//Instantiate the WebDriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.addTestRunMachineInfo(driver,currentDriver,context);//get the execution environment details
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;
		HomePage homePage=null;
		try{

			Log.testCaseInfo(" Hide '"+dataValues.get("ConfigurationSetting")+"' option on TaskPane using Configuration Page");
			loginPage=new LoginPage(driver);
			//Step-6: Launch the Configuration Page
			driver.get(configSite);

			//Step-7: Login to the Configuration Page	
			loginPage.loginToConfigurationUI(inputDataValues.get("UserName"),inputDataValues.get("Password"));
			//Utils.waitForPageLoad(driver);
			Log.message("7. Successfully LoggedIn to 'Configuration' Application.");
			Utils.fluentWait(driver);

			//Instantiate configuration page
			configPage=new ConfigurationPage(driver);
			//Step-8: Verify the Settings Tree in Configuration Page
			if (!configPage.isSettingsTreeDisplayed())
				throw new Exception("Configuration Page is not displayed.");
			Log.message("8. Settings Tree  displayed in Configuration Page.");

			//Click SampleVault folder in Configuration Page
			configPage.expandVaultFolder("Sample Vault");
			//Select TaskPane->SampleVault folder in Configuration Page
			configPage.clickSampleVaultTaskPane(driver);
			Log.message("9. Navigated to the Taskpane settings in Configuration Page");

			//Step-9:Hide the 'TaskPane' options
			if(!configPage.getConfigSettingValue(inputDataValues.get("SettingSection"), inputDataValues.get("ConfigurationSetting"), "Hide")){
				configPage.chooseConfigurationVaultSettings(driver, inputDataValues.get("ConfigurationSetting"),inputDataValues.get("SettingSection"),"Hide");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				Log.message("10. Hiding the 'TaskPane' options is done");
			}
			configPage.clickLogOut();
			//Step-10: Launch the M-Files Client
			driver.get(webSite);
			//Fetch Login details from Testdata sheet
			loginPage=new LoginPage(driver);
			//Step-11: Login to the 'MFiles' application
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			homePage=new HomePage(driver);

			Log.message("11. Successfully LoggedIn to Application and Verified the Page title.");
			Utils.fluentWait(driver);   

			//Step-12: Verify if 'AssignedToMe' option exists on TaskPane
			if (homePage.isGoToItemExists(driver, inputDataValues.get("ConfigurationSetting"))) 
				Log.fail("GoTo item "+inputDataValues.get("ConfigurationSetting")+" exists on Task Pane.", driver);
			else
				Log.pass("Go to Item "+inputDataValues.get("ConfigurationSetting")+" is not listed on Task Pane");
		}//End of try
		catch(Exception e){
			Log.exception(e,driver);

		}//End of catch
		finally{
			homePage=new HomePage(driver);
			loginPage=new LoginPage(driver);
			loginPage=homePage.menuBar.logOutFromMenuBar();
			Utils.isLogOutPromptDisplayed(driver);

			//Step-7: Login to the Configuration Page	
			loginPage.navigateToApplication(configSite,inputDataValues.get("UserName"),inputDataValues.get("Password"),"");

			configPage=new ConfigurationPage(driver);
			Utils.fluentWait(driver);

			//Step-8: Verify the Settings Tree in Configuration Page
			if (!configPage.isSettingsTreeDisplayed())
				throw new Exception("Configuration Page is not displayed.");

			//Click SampleVault folder in Configuration Page
			configPage.expandVaultFolder("Sample Vault");
			//Select TaskPane->SampleVault folder in Configuration Page
			configPage.clickSampleVaultTaskPane(driver);
			//Step-9:Hide the 'TaskPane' options
			if(!configPage.getConfigSettingValue(inputDataValues.get("SettingSection"), inputDataValues.get("ConfigurationSetting"), "Show")){
				configPage.chooseConfigurationVaultSettings(driver, inputDataValues.get("ConfigurationSetting"),inputDataValues.get("SettingSection"),"Show");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}
			//Close the browser
			driver.quit(); 
		}//End of finally
	}//End of Test

	/**
	 * TestCase ID: TC_022
	 * <br>Description: Show 'GO TO' options on TaskPane using Configuration Page</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_022",description="Show 'GO TO' options on TaskPane using Configuration Page")
	public void TC_022(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception{

		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.addTestRunMachineInfo(driver,currentDriver,context);
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);
		ConfigurationPage configPage=null;
		try{

			Log.testCaseInfo(" Show 'GO TO('"+inputDataValues.get("ConfigurationSetting")+"') options on TaskPane using Configuration Page");
			driver.get(configSite);

			//Login to Application
			LoginPage loginPage=new LoginPage(driver);
			//login to configuration Page
			loginPage.loginToConfigurationUI(inputDataValues.get("UserName"),inputDataValues.get("Password"));
			Log.message("6. Successfully LoggedIn to 'Configuration' Application.");
			Utils.fluentWait(driver);

			//Instantiate configuration page
			configPage=new ConfigurationPage(driver);
			//Verify if confirguration setting page displayed
			if (!configPage.isSettingsTreeDisplayed())
				throw new Exception("Configuration Page is not displayed.");
			Log.message("7. Configuration Page displayed.");
			//Expanding the Sample vault
			configPage.expandVaultFolder(inputDataValues.get("DocumentVault"));

			configPage.clickSampleVaultTaskPane(driver);
			if(!configPage.getConfigSettingValue(inputDataValues.get("SettingSection"), inputDataValues.get("ConfigurationSetting"), "Show")){
				configPage.chooseConfigurationVaultSettings(driver, inputDataValues.get("ConfigurationSetting"),inputDataValues.get("SettingSection"),"Show");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				Log.message("8. SHow option is selected for '"+inputDataValues.get("ConfigurationSetting")+"' under '"+inputDataValues.get("SettingSection")+"'");
			}
			configPage.clickLogOut();
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(webSite,inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);

			//Instantiate HomePage
			HomePage homePage=new HomePage(driver);
			Log.message("9. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));
			Utils.fluentWait(driver);

			if (!homePage.isGoToItemExists(driver, inputDataValues.get("ConfigurationSetting")))
				Log.fail("GoTo item '"+inputDataValues.get("ConfigurationSetting")+"' does not exists on Task Pane.", driver);
			else 
				Log.pass("Go to Item '"+inputDataValues.get("ConfigurationSetting")+"' is listed on Task Pane");

		}
		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally{
			//Close the browser
			driver.quit(); 
		}
	}

	/**
	 * TestCase ID: TC_023
	 * <br>Description: Insert a New Column by Right clicking on Column Header</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_023",description="Insert a New Column by Right clicking on Column Header")
	public void TC_023(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{
		//Instantiate WebDriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" Insert a New Column by Right clicking on Column Header");
		Log.addTestRunMachineInfo(driver,currentDriver,context); //get the execution environment details and print on Log report
		//Fetch testdata from Excel data sheet
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);

		try{

			//Step-1: Launch M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Step-3: Login to Application
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			//Step-4: Verify the Page title
			HomePage homePage=new HomePage(driver);
			if (!driver.getTitle().contains("Web")&&!homePage.isLoggedIn(inputDataValues.get("UserName"))) {
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			}
			Log.message("4. Veified Page title after Successfully LoggedIn to Application.");

			Utils.fluentWait(driver);


			//Step-5: Verify the display of SearchBar in HomePage
			if (!homePage.isSearchbarPresent(driver))
				throw new Exception("Search Panel not displayed.");
			Log.message("5. Search Panel displayed on HomePage.");

			//Step-6: Enter Search Term and verify if exists in search results
			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));

			if (!homePage.isSearchResults(driver, inputDataValues.get("Type")) && searchedFile.isEmpty()) {
				throw new Exception("File not found in Search results.");
			}
			Log.message("Search results contains the File"+inputDataValues.get("SearchTerm"));

			//Step-7: Verify if user can insert new column by right clicking on the column header of view
			Log.message("7. Verify if user can insert new column by right clicking on the column header of view");
			if (!homePage.readListViewHeaderNames(driver,"Checked Out To")) {
				homePage.clickAndInsertListViewColumns(driver,"Insert Column->Standard Columns->Checked Out To");
			}

			if (!homePage.readListViewHeaderNames(driver,"Checked Out To")) 
				Log.fail("'Checked Out To' Column Not added.", driver);
			else 
				Log.pass("Successfully Added 'Checked Out To' Column");
		}
		catch(Exception e)
		{
			Log.exception(e, driver);
		}
		finally
		{
			driver.quit();
		}
	}

	//	/**
	//	 * TestCase ID: TC_024
	//	 * <br>Description: Hide/Show Treeview from Titlebar</br>
	//	 * @param dataValues
	//	 * @param context
	//	 * @throws Exception
	//	 */
	//	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_024",description="Hide/Show Treeview from Titlebar")
	//	public void TC_024(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	//	{
	//		//Instantiate Webdriver
	//		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
	//	    Log.testCaseInfo(" Hide/Show Treeview from Titlebar");
	//	    Log.addTestRunMachineInfo(driver,currentDriver,context);//get the execution environment details and prints on log report
	//		//Fetch testdata from the Excel sheet
	//		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);
	//	    
	//		try{
	//		
	//			//Step-1: Launch M-Files Application
	//			Log.message("1. Launch the application.");
	//			driver.get(webSite);
	//
	//			//Step-2: Fetch Login details from Testdata sheet
	//			Log.message("2. Fetch Data from Testdata sheet.");
	//			LoginPage loginPage=new LoginPage(driver);
	//
	//			//Step-3: Login to Application
	//			Log.message("3. Login to the application using valid credentials.");
	//			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
	//			Utils.waitForPageLoad(driver);
	//			Log.message("Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));
	//			
	//			//Step-4: Verify the Page title
	//			Log.message("4. Verify the Page title.");
	//	        if (!driver.getTitle().contains("Web")) {
	//	        	throw new Exception("Unable to Login. Please verify Login Credentials.");
	//	        }
	//	        Log.message("Successfully LoggedIn to Application.");
	//	        
	//	        Utils.fluentWait(driver);
	//	        HomePage homePage=new HomePage(driver);
	//
	//	        //Step-5: Click 'Show' icon and see if Tree view structure is displayed
	//	        Log.message("5. Click 'Show' icon and see if Tree view structure is displayed.");
	//	        if (!homePage.showTreeView()) {
	//	        	throw new Exception("Unable to display Tree view structure details");
	//	        }
	//	        Log.message("Tree view structure details are displayed.");
	//	        
	//	        //Step-6: Click 'Hide' icon and see if Tree view structure is Hidden
	//	        Log.message("6. Click 'Hide' icon and see if Tree view structure is Hidden.");
	//	        if (homePage.hideTreeView()) {
	//	        	Log.fail("Unable to Hide Tree view structure details", driver);
	//	        }
	//	        else {
	//	        	Log.pass("Test Passed..!!!Tree view structure details are Hidden.");
	//	        }
	//		}
	//		catch(Exception e){
	//			Log.exception(e, driver);
	//		}
	//		finally{
	//			Log.endTaseCase();driver.quit();
	//		}//End of finally
	//	}//End of Test

	/**
	 * TestCase ID: TC_025
	 * <br>Description: Navigate to 'CheckedOUt to Me' from TaskPane</br.
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_025",description="Navigate to 'CheckedOUt to Me' from TaskPane")
	public void TC_025(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{
		//Instantiate Webdriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" Navigate to 'CheckedOUt to Me' from TaskPane");
		Log.addTestRunMachineInfo(driver,currentDriver,context);//get the execution environment details
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);

		try{
			//Step-1: Launch M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Step-2: Fetch Login details from Testdata sheet
			LoginPage loginPage=new LoginPage(driver);

			//Step-3: Login to Application
			HomePage homePage=loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			//Step-4: Verify the Page title
			Log.message("4. Verify the Page title.");
			if (!driver.getTitle().contains("Web")) {
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			}
			Log.message("5. Successfully LoggedIn to Application.");
			Utils.fluentWait(driver);
			//Step-5: Navigate to 'Checked Out to Me' Page
			if(!homePage.isGoToItemExists(driver, "Checked Out to Me"))
				throw new Exception("'Checked Out to Me' item is not displayed under 'GOTO' section");

			if (!homePage.navigateToGotoViews("Checked Out to Me")) 
				Log.fail("Unable to navigate to 'Checked Out to Me' Page.", driver);
			else 
				Log.pass("Successfully navigated to 'Checked Out to Me' Page.");
		}
		catch(Exception e){
			Log.exception(e, driver);
		}
		finally{
			driver.quit();
		}//End of finally
	}//End of Test

	/**
	 * TestCase ID: TC_026
	 * <br>Description: Navigate to 'Assigned to Me' from TaskPane</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */	
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_026",description="Navigate to 'Assigned to Me' from TaskPane")
	public void TC_026(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{
		//Instantiate WebDriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" Navigate to 'Assigned to Me' from TaskPane");
		Log.addTestRunMachineInfo(driver,currentDriver,context);//get the execution environment details and print on log report
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);

		try{
			// And now use this to visit M-Files Application
			Log.message("1. Launch the application.");
			driver.get(webSite);

			//Fetch Login details from Testdata sheet
			Log.message("2. Fetch Data from Testdata sheet.");
			LoginPage loginPage=new LoginPage(driver);

			//Login to Application
			Log.message("3. Login to the application using valid credentials.");
			HomePage homePage=loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);

			Log.message("Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));
			//Verify the Page title
			if (!driver.getTitle().contains("Web")) {
				throw new Exception("Unable to Verify the Page title.");
			}
			Log.message("4. Verify the Page title.");
			Utils.fluentWait(driver);

			if (!homePage.isGoToItemExists(driver, inputDataValues.get("ConfigurationSetting"))) {
				throw new Exception("GoTo item "+inputDataValues.get("ConfigurationSetting")+" already hidden.");
			}

			//Navigate to 'AssignedToMe' Page
			Log.message("5. Navigate to 'Assigned to Me' Page");
			if (!homePage.navigateToGotoViews("Assigned to Me")) 
				Log.fail("Unable to navigate to 'Assigned to Me' Page.", driver);
			else 
				Log.pass("---Successfully navigated to 'Assigned to Me' Page.");


		}catch(Exception e){
			Log.exception(e, driver);
		}
		finally{
			driver.quit();
		}

	}//End of Test




	/**
	 * TestCase ID: TC_027
	 * <br>Description: Navigate to 'HomePage' from TaskPane</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */	
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_027",description="Navigate to 'HomePage' from TaskPane")
	public void TC_027(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{
		//Instantiate webdriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" Navigate to 'HomePage' from TaskPane");
		Log.addTestRunMachineInfo(driver,currentDriver,context);//get execution environment details and print on log report
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);
		try{

			//Step-1:  Launch M-Files Application
			Log.message("1. Launch the application.");
			driver.get(webSite);

			//Step-2:  Fetch Login details from Testdata sheet
			Log.message("2. Fetch Data from Testdata sheet.");
			LoginPage loginPage=new LoginPage(driver);

			if (inputDataValues.get("UserName").isEmpty()||inputDataValues.get("Password").isEmpty()) {
				throw new SkipException("Unable to Fetch Data from Testdata sheet.");
			}

			//Step-3: Login to Application
			Log.message("3. Login to the application using valid credentials.");
			HomePage homePage=loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			//Step-4: Verify the Page title

			if (!driver.getTitle().contains("Web")) {
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			}
			Log.message("5. Successfully LoggedIn to Application.");
			Utils.fluentWait(driver);

			//Step-5: Navigate to 'Recently Accessed by Me' Page
			if(!homePage.isGoToItemExists(driver, "Recently Accessed by Me"))
				throw new Exception("'Recently Accessed by Me' item is not displayed under 'GOTO' section");

			if (!homePage.navigateToGotoViews("Recently Accessed by Me")) 
				throw new Exception("Unable to navigate to 'Recently Accessed by Me' Page.");
			else
				Log.message("6. Successfully navigated to 'Recently Accessed by Me' Page");

			//Step-6:  Navigate to 'Home' Page
			if (!homePage.navigateToHome()) 
				Log.fail("Test Failed. Unable to navigate back to 'HomePage' Page from 'Recently accessed by Me' view.", driver);
			else 
				Log.pass("Test Passed. Successfully navigated back to 'HomePage' Page from 'Recently accessed by Me' view.");
		}
		catch(Exception e){
			Log.exception(e, driver);
		}
		finally{
			driver.quit();
		}

	}//End of Test

	/**
	 * TestCase ID: TC_028
	 * <br>Description: Navigate to 'Favorites' from TaskPane</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_028",description="Navigate to 'Favorites' from TaskPane")
	public void TC_028(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{

		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" Navigate to 'Favorites' from TaskPane");
		Log.addTestRunMachineInfo(driver,currentDriver,context);
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);

		try{

			driver.get(configSite);
			Log.message("1. Launched the COnfiguratin application.");

			LoginPage loginPage=new LoginPage(driver);
			//login to configuration Page
			loginPage.loginToConfigurationUI(inputDataValues.get("UserName"),inputDataValues.get("Password"));
			Log.message("2. Successfully LoggedIn to 'Configuration' Application.");

			//Instantiate configuration page
			ConfigurationPage configPage=new ConfigurationPage(driver);
			if (!configPage.isSettingsTreeDisplayed())
				throw new Exception("Configuration Page is not displayed.");
			Log.message("3. Configuration Page displayed.");

			configPage.expandVaultFolder(inputDataValues.get("DocumentVault"));

			configPage.clickSampleVaultTaskPane(driver);
			if(!configPage.getConfigSettingValue(inputDataValues.get("SettingSection"), inputDataValues.get("ConfigurationSetting"), "Show")){
				configPage.chooseConfigurationVaultSettings(driver, inputDataValues.get("ConfigurationSetting"),inputDataValues.get("SettingSection"),"Show");
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
				Log.message("4. SHow option is selected for '"+inputDataValues.get("ConfigurationSetting")+"' under '"+inputDataValues.get("SettingSection")+"'");
			}

			configPage.logOut();
			//Step-1: And now use this to visit M-Files Application
			Log.message("5. Launched the Web application.");
			driver.get(webSite);

			//Step-3: Login to Application
			Log.message("6. Login to the application using valid credentials.");
			HomePage homePage=loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);

			Log.message("Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));
			//Step-4: Verify the Page title
			Log.message("7. Verify the Page title.");
			System.out.println("Page title is: " + driver.getTitle());

			if (!driver.getTitle().contains("Web")) {
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			}
			Log.message("8. Successfully LoggedIn to Application.");

			Utils.fluentWait(driver);
			//Step-5: Navigate to 'Favorites' Page
			if(!homePage.isGoToItemExists(driver, "Favorites"))
				throw new Exception("'Favorites' item is not displayed under 'GOTO' section");

			if (!homePage.navigateToGotoViews("Favorites")) 
				Log.fail("Unable to navigate to 'Favorites' Page.", driver);
			else 
				Log.pass("Successfully navigated to 'Favorites' Page.");

		}
		catch(Exception e){
			Log.exception(e, driver);
		}
		finally{
			driver.quit();
		}
	}//End of Test

	/**
	 * TestCase ID: TC_029
	 * <br>Description: 'UndoCheckOut' a document from context menu of Document</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_029",description="'UndoCheckOut' a document from context menu of Document")
	public void TC_029(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{

		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo("'UndoCheckOut' a document from context menu of Document");
		Log.addTestRunMachineInfo(driver,currentDriver,context);
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);
		HomePage homePage=null;
		try{

			//Step-1:  And now use this to visit M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Step-2:  Login to Application
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			Utils.fluentWait(driver);
			homePage=new HomePage(driver);

			//Step-4: Verify if TaskPane displayed
			if (!homePage.isTaskPaneDisplayed()) 
				throw new Exception("Task Pane not displayed.");
			Log.message("3. Successfully Task Pane Displayed.");

			//Step-5: Verify the display of SearchBar in HomePage
			if (!homePage.isSearchbarPresent(driver)) {
				throw new Exception("Search Panel not displayed.");
			}
			Log.message("4. Search Panel displayed on HomePage.");
			Utils.fluentWait(driver);

			//Step-6: Search an object and perform 'UndoCheckOut' using context menu options
			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));
			if (!homePage.isSearchResults(driver,inputDataValues.get("Type")) && searchedFile.isEmpty()) 
				throw new Exception("Could not Find document in search list.");
			Log.message("5. Entered Search Term as "+inputDataValues.get("SearchTerm")+" and verified if Search Results correct.");

			//Read List view column Headers
			if (!homePage.readListViewHeaderNames(driver,"Checked Out To")) {	        
				//Insert New Column to List view 
				homePage.clickAndInsertListViewColumns(driver,"Insert Column->Standard Columns->Checked Out To");
				//Verify if newly inserted column exists
				if (!homePage.readListViewHeaderNames(driver,"Checked Out To")) 
					throw new Exception("Column 'Checked Out To' Not added.");
			}

			Log.message("6. 'Checked Out To' Column 'Checked Out To' added to List View.");

			//Read List view column Headers
			if (!homePage.readListViewHeaderNames(driver,"Version")) {	        
				//Insert New Column to List view
				homePage.listView.insertColumn("Version");
				//Verify if newly inserted column exists
				if (!homePage.readListViewHeaderNames(driver,"Version"))
					throw new Exception("Column 'Version' not added.");
			}
			Log.message("7. Column 'Version' added to List View.");
			//Step-7: CheckOut the Document using ContextMenu option
			if (!homePage.isObjectCheckedOut("Checked Out To", inputDataValues.get("UserName"))) {
				if (!homePage.openContextMenuDialog(driver))
					throw new Exception("Unable to display ContextMenu options");
				Log.message("List view Context Menu displayed..!!!!");
				if (!homePage.checkOutObjectFromContextMenu())
					throw new Exception("Could not 'CheckOut' the document from context menu..!!!!");
			}

			Log.message("8. Document checkedout successfully using context menu.");

			//Step-8: 'Undo CheckOut' the Document using ContextMenu option
			if (!homePage.openContextMenuDialog(driver))
				throw new Exception("Unable to display ContextMenu options");
			Log.message("9. Successfully displayed ContextMenu options after right clicking on Object.");

			//Step-9: Verify if 'UndoCheckOut' done
			if (!homePage.undoCheckOutObject("contextMenu")) 
				Log.fail("Could not perform 'UndoCheckOut' from context menu..!!!!", driver);
			else 
				Log.pass("'UndoCheckOut' is done successfully from context menu.");
		}
		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally{
			//Close the browser
			driver.quit(); 
		}
	}

	/**
	 * TestCase ID: TC_030
	 * <br>Description: Click 'Relationships' from context menu of Document</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_030",description="Click 'Relationships' from context menu of Document")
	public void TC_030(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{
		//Instantiate WebDriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" Click 'Relationships' from context menu of Document");
		Log.addTestRunMachineInfo(driver,currentDriver,context);

		try {
			//Fetch test data from Excel sheet
			ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);

			// Step-1: And now use this to visit M-Files Application
			Log.message("1. Launch the application.");
			driver.get(webSite);

			//Step-2: Fetch Login details from Testdata sheet
			Log.message("2. Fetch Data from Testdata sheet.");
			LoginPage loginPage=new LoginPage(driver);

			if (inputDataValues.get("UserName").isEmpty()||inputDataValues.get("Password").isEmpty())
				throw new SkipException("Unable to Fetch Data from Testdata sheet.");

			//Step-3: Login to Application
			Log.message("3. Login to the application using valid credentials.");
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);

			Log.message("Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));
			//Step-4: Verify the Page title
			Log.message("4. Verify the Page title.");
			System.out.println("Page title is: " + driver.getTitle());

			if (!driver.getTitle().contains("Web")) 
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			Log.message("---Successfully LoggedIn to Application.");

			Utils.fluentWait(driver);
			HomePage homePage=new HomePage(driver);

			//Step-5: Search an object to see its relationships
			Log.message("5. Search an object to see its relationships");
			homePage.searchPanel.showAdvancedSearchOptions(driver);

			if (!homePage.searchPanel.selectSearchOptionsUsingObject(driver,MFilesObjectList.PROJECT, inputDataValues.get("Type")).contains("Projects")) 
				throw new Exception("Could not select Basic Search options");

			Log.message("Successfully Selected Basic Search Options");
			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));

			//Click 'Search' button and see if search yieds some objects
			if (!homePage.isSearchResults(driver,inputDataValues.get("Type"))&& searchedFile.isEmpty()) 
				throw new Exception("Could not Find document in search list.");

			if(!homePage.listView.clickItemByIndex(0))
				throw new Exception("Could not select/click document from search list.");

			//Step-6: Check 'Relationships' of the Document using ContextMenu option.
			Log.message("6. Check 'Relationships' of the Document using ContextMenu option.");
			if (!homePage.openContextMenuDialog(driver)) 
				throw new Exception("Unable to Open context Menu options");

			Log.message("Successfully displayed context Menu options.");
			homePage.selectContextMenuItemFromListView("Relationships");

			//Step-7: Verify if user navigated to object 'Relationship' view
			Log.message("7. Verify if user navigated to object 'Relationship' view");
			if (!homePage.isViewPageDisplayed("Relationships")) 
				Log.fail("Test Failed...!!!Could not navigate to Relationships Page..!!!!", driver);
			else 
				Log.pass("Test Passed..!!!Relationships Page Displayed..!!!!");
		}
		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally{
			//Close the browser
			driver.quit(); 
		}
	}//End of Test

	/**
	 * TestCase ID: TC_031
	 * <br>Description: Click 'History' from context menu of Document</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_031",description="Click 'History' from context menu of Document")
	public void TC_031(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{

		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" Click 'History' from context menu of Document");
		Log.addTestRunMachineInfo(driver,currentDriver,context);
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);
		try{

			// And now use this to visit M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Login to Application
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);

			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			if (!driver.getTitle().contains("Web")) {
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			}
			//Verify the Page title
			Log.message("4. Verify the Page title after Successfully LoggedIn to Application.");

			Utils.fluentWait(driver);
			HomePage homePage=new HomePage(driver);

			//Select Advanced Search Options
			homePage.searchPanel.showAdvancedSearchOptions(driver);

			//Step-7: Select the Search options
			if (!homePage.searchPanel.selectSearchOptionsUsingObject(driver, MFilesObjectList.PROJECT, inputDataValues.get("Type")).contains("Projects")) {
				throw new Exception("Could not select Basic Search options");
			}
			Log.message("5. Successfully Selected Basic Search Options");
			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));

			//Click 'Search' button and see if search yieds some objects
			if (!homePage.isSearchResults(driver,inputDataValues.get("Type"))&& searchedFile.isEmpty()) 
				throw new Exception("Could not Find document in search list.");

			if(!homePage.listView.clickItemByIndex(0))
				throw new Exception("Could not select/click document from search list.");

			Log.message("6. Document found in Search results");
			//Step-8: Check 'History' of the Document using ContextMenu option.
			if (!homePage.openContextMenuDialog(driver)) {
				throw new Exception("Unable to Open context Menu options");
			}
			Log.message("7. Successfully displayed context Menu options after right clicking on the object.");

			homePage.selectContextMenuItemFromListView("History");
			Log.message("8. 'History' option is selected from context Menu options after right clicking on the object.");

			//Step-9: Verify if user navigated to Object histroy Page
			if (!homePage.isViewPageDisplayed("History")) 
				Log.fail("Test Failed...!!!Could not navigate to History Page..!!!!", driver);
			else 
				Log.pass("Test Passed..!!!History Page Displayed..!!!!");
		}
		catch(Exception e)
		{
			Log.exception(e, driver);
		}
		finally{
			//Close the browser
			driver.quit(); 
		}
	}

	/**
	 * TC032 : Click 'Add to Favorites' from context menu of Document
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Smoke"}, 
			description = "Click 'Add to Favorites' from context menu of Document")
	public void TC_032(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = WebDriverUtils.getDriver();

		try {

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Right click on any object
			//----------------------------------
			String randObj = ListView.getRandomObject(driver); //Gets random object from the list in view

			if (!homePage.listView.rightClickItem(randObj)) //Right clicks random object
				throw new Exception("Object (" + randObj + ") is not right clicked.");

			Log.message("2.Object (" + randObj + ") is right clicked.", driver);

			//Step-3 : Select Add to Favorites from context menu
			//-------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.AddToFavorites.Value); //Selects Add to Favorites from context menu

			MFilesDialog mfilesDialog = new MFilesDialog (driver); //Instantiates MFilesDialog

			if (!mfilesDialog.getMessage().toUpperCase().contains("ONE OBJECT WAS AFFECTED")) //Checks if One object was affected message appeared
				throw new Exception("One object was affected message does not appear after adding object to favorites");

			mfilesDialog.clickOkButton(); //Clicks Ok button in the MFiles information dialog

			Log.message("3. Add to Favorites is selected from context menu.");

			//Verification : To verify if object is added to favorites view
			//---------------------------------------------------------------
			viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, Caption.MenuItems.Favorites.Value, "");

			if (homePage.listView.isItemExists(randObj))
				Log.pass("Test case Passed. Object (" + randObj + ") is added to Favorites successfully.");
			else
				Log.fail("Test case Failed. Object (" + randObj + ") is not added to Favorites view.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_032

	/**
	 * TC033 : Click 'Remove from Favorites' from context menu of Document
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Smoke"}, 
			description = "Click 'Remove from Favorites' from context menu of Document")
	public void TC_033(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = WebDriverUtils.getDriver();

		try {

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Right click on any object
			//----------------------------------
			String randObj = ListView.getRandomObject(driver); //Gets random object from the list in view

			if (!homePage.listView.rightClickItem(randObj)) //Right clicks random object
				throw new Exception("Object (" + randObj + ") is not right clicked.");

			Log.message("2.Object (" + randObj + ") is right clicked.", driver);

			//Step-3 : Select Add to Favorites from context menu
			//-------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.AddToFavorites.Value); //Selects Add to Favorites from context menu

			MFilesDialog mfilesDialog = new MFilesDialog (driver); //Instantiates MFilesDialog

			if (!mfilesDialog.getMessage().toUpperCase().contains("ONE OBJECT WAS AFFECTED")) //Checks if One object was affected message appeared
				throw new Exception("One object was affected message does not appear after adding object to favorites");

			mfilesDialog.clickOkButton(); //Clicks Ok button in the MFiles information dialog

			Log.message("3. Add to Favorites is selected from context menu.");

			//Step-4 : Navigate to Favorites view
			//-----------------------------------
			viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, Caption.MenuItems.Favorites.Value, "");

			if (!homePage.listView.isItemExists(randObj))
				throw new Exception("Object (" + randObj + ") is not available in Favorites view.");

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5 : Right click on the object
			//---------------------------------
			if (!homePage.listView.rightClickItem(randObj)) //Right clicks random object
				throw new Exception("Object (" + randObj + ") is not right clicked.");

			Log.message("5.Object (" + randObj + ") is right clicked.", driver);

			//Step-6 : Select Remove From Favorites from context menu
			//-------------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.RemoveFromFavorites.Value); //Selects Remove from Favorites from context menu

			mfilesDialog = new MFilesDialog (driver); //Instantiates MFilesDialog

			if (!mfilesDialog.getMessage().toUpperCase().contains("REMOVE THE SELECTED OBJECT FROM THE FAVORITES?")) //Checks if One object was affected message appeared
				throw new Exception("Confirmation message to remove object from favorites is not displayed.");

			mfilesDialog.clickOkButton(); //Clicks Yes button in the MFiles confirmation dialog

			Utils.fluentWait(driver);

			mfilesDialog = new MFilesDialog (driver); //Instantiates MFilesDialog

			if (!mfilesDialog.getMessage().toUpperCase().contains("ONE OBJECT WAS AFFECTED")) //Checks if One object was affected message appeared
				throw new Exception("One object was affected message does not appear after removing object to favorites");

			mfilesDialog.clickOkButton(); //Clicks Ok button in the MFiles information dialog

			Log.message("6. Remove from Favorites is selected from context menu.");

			//Verification : To verify if Object is removed from favorites view
			//-----------------------------------------------------------------
			if (!homePage.listView.isItemExists(randObj))
				Log.pass("Test case Passed. Object (" + randObj + ") is removed from Favorites successfully.");
			else
				Log.fail("Test case Failed. Object (" + randObj + ") is not removed from Favorites view.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_033

	/**
	 * TestCase ID: TC_034
	 * <br>Description: Click 'Properties' from context menu of Document</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_034",description="Click 'Properties' from context menu of Document")
	public void TC_034(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{

		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" Click 'Properties' from context menu of Document");
		Log.addTestRunMachineInfo(driver,currentDriver,context);
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);

		try{

			// Step-1: And now use this to visit M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Login to Application
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			//Verify the Page title
			HomePage homePage=new HomePage(driver);
			Utils.fluentWait(driver);

			//Step-7: Select the Search options
			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));
			Log.message("4. Searched for 'Document' using Basic Search option.");

			if (!homePage.isSearchResults(driver,inputDataValues.get("Type")) && searchedFile.isEmpty()) {
				throw new Exception("Specified document doesnot exists in the Search results.");
			}
			Log.message("5. Specified document exists in the Search results.");

			if (!homePage.openContextMenuDialog(driver))
				throw new Exception("Unable to Open context Menu options");
			Log.message("6. ContextMenu options are displayed in homePage.listView.");

			homePage.selectContextMenuItemFromListView("Properties");
			Utils.waitForPageLoad(driver);
			Utils.fluentWait(driver);
			Log.message("7. 'Properties' is selected for the Document using ContextMenu option.");

			if (!homePage.verifyMetadataCardDisplay(inputDataValues.get("SearchTerm"))) 
				Log.fail("Test Failed...Could not display 'Properties' dialog from context menu",driver);
			else 
				Log.pass("Test Passed..!!!Successfully displayed 'Properties' dialog from context menu.");

		}
		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally{
			//Close the browser
			driver.quit(); 
		}
	}//End of Test

	/**
	 * TestCase ID: TC_035
	 * <br>Description: Click 'Workflow' from context menu of Document</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_035",description="Click 'Workflow' from context menu of Document  and verify the display of Workflow dialog")
	public void TC_035(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{

		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" Click 'Workflow' from context menu of Document and verify the display of Workflow dialog");
		Log.addTestRunMachineInfo(driver,currentDriver,context);
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);

		try{

			// Step-1: And now use this to visit M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Login to Application
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));
			Utils.fluentWait(driver);
			HomePage homePage=new HomePage(driver);
			//Step-7: Select the Search options
			Log.message("5. Search for 'Document' using Basic Search option.");
			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));

			if (!homePage.isSearchResults(driver,inputDataValues.get("Type")) && searchedFile.isEmpty()) {
				throw new Exception("Specified document doesnot exists in the Search results.");
			}
			Log.message("Specified document exists in the Search results.");

			Log.message("6. 'Workflow' the Document using ContextMenu option.");
			if (!homePage.openContextMenuDialog(driver))
				throw new Exception("Unable to Open context Menu options");

			homePage.selectContextMenuItemFromListView("Workflow");
			MFilesDialog mFilesDialog=new MFilesDialog(driver);

			if (!mFilesDialog.isWorkflowDialogDisplayed()) 
				Log.fail("Test Failed...Could not display 'Workflow' dialog from context menu",driver);
			else 
				Log.pass("Test Passed..!!!Successfully displayed 'Workflow' dialog from context menu.");
		}
		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally{
			//Close the browser
			driver.quit(); 
		}
	}//End of Test

	/**
	 * TestCase ID: TC_036
	 * <br>Description: Download a Document from Task Pane</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_036",description="Download Document Object from Task Pane")
	public void TC_036(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{

		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" Download Document Object from Task Pane");
		Log.addTestRunMachineInfo(driver,currentDriver,context);

		try{

			ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);
			// And now use this to visit M-Files Application
			Log.message("1. Launch the application.");
			driver.get(webSite);

			//Login to Application
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);

			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));
			Utils.fluentWait(driver);
			HomePage homePage=new HomePage(driver);

			//Step-7: Select the Search options
			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));
			Log.message("4. 'Document' exists in Search List.");

			if (!homePage.isSearchResults(driver,inputDataValues.get("Type"))&&searchedFile.isEmpty()) 
				throw new Exception("Could not Find Document in Search List.");
			else
				Log.message("5. Selected the Document from List view.");


			if (!homePage.listView.clickItem(inputDataValues.get("SearchTerm"))) //Checks if object selected in the list
				throw new Exception("Object (" + searchedFile + ") is not selected in the list.");

			if (!homePage.downloadObjectFromTaskPane(inputDataValues.get("SearchTerm"))) 
				Log.fail("Test Failed...Could not Download object from taskpane",driver);
			else 
				Log.pass("Test Passed..!!!Successfully Downloaded object from taskPane");


		}
		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally{
			//Close the browser
			driver.quit(); 
		}
	}//End of Test

	/**
	 * Testcase ID:TC_037
	 * <br>Description: 'LogOut' from application using 'View and Modify' section</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_037",description="'LogOut' from application using 'Context Menu' section")
	public void TC_037(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{
		//Instantiate Webdriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.addTestRunMachineInfo(driver,currentDriver,context);//get the execution environment details and print log report
		//Fetch test data from Excel sheet
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);
		Log.testCaseInfo("'LogOut' from application using 'Context Menu' of "+inputDataValues.get("LayOut"));
		LoginPage loginPage=null;
		ConfigurationPage configPage=null;
		String prevLayout=null;

		HomePage homePage=null;
		try{
			//Step-1: Login to Configuration Page
			loginPage=new LoginPage(driver);
			loginPage.navigateToApplication(configSite,userName,password,"");	
			Log.message("1. Logged in to Configuration Page");

			//Step-2: Click Vault from left panel of Configuration Page
			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);
			Log.message("2. Clicked 'Sample Vault' from left panel of Configuration Page");

			//Step-2 : Set layout for the vault as default
			prevLayout = configPage.configurationPanel.getLayout(); //Gets the layout selected
			if (!prevLayout.trim().equalsIgnoreCase(inputDataValues.get("Layout"))) {
				configPage.configurationPanel.setLayout(inputDataValues.get("Layout").trim()); //Sets the layout for the vault
				configPage.clickSaveButton();
				configPage.clickOKBtnOnSaveDialog();
			}
			Utils.fluentWait(driver);
			//Checks if vault configuration settings has been saved properly
			if (!configPage.configurationPanel.getLayout().toUpperCase().equalsIgnoreCase(inputDataValues.get("Layout").toUpperCase())) {
				throw new Exception("Vault is not modified to 'Listing pane and properties pane only', but with '"+configPage.configurationPanel.getLayout());
			}
			Log.message("3. 'Listing pane and properties pane only' is set as layout for the vault.");

			//Make sure 'Advanced search' option enabled
			configPage.expandVaultFolder(documentVault);
			Log.message("4. Clicked '"+documentVault+"' from left panel of Configuration Page");

			//Step-3 : Logs out from the Configuration page
			configPage.clickLogOut(); //
			Log.message("6. Logged out from the Configuration page");

			//Step-3: Login to Application
			loginPage=new LoginPage(driver);
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			Utils.fluentWait(driver);
			homePage= new HomePage(driver);

			//Click context menu anywhere in homepage
			homePage.listView.openListViewContextMenu();

			//Select logout option from context menu
			homePage.selectContextMenuItemFromListView("LogOut");
			Log.message("10. Selected 'LogOut' context option for the Object found in listview");

			Thread.sleep(1000);
			Utils.isLogOutPromptDisplayed(driver);

			//Step-7:Verify if User navigated back to Login form after LoggedOut
			if (!loginPage.isLoginPageDisplayed()) 
				Log.fail("Failed to navigate to LoginPage.", driver);
			else
				Log.pass("Successfully logged Out of MFWA using 'LogOut' context menu options");

		}
		catch(Exception e){
			Log.exception(e, driver);
		}//End of catch
		finally{
			loginPage.navigateToApplication(configSite, userName, password,"");
			configPage=new ConfigurationPage(driver);
			configPage.clickVaultFolder(documentVault);

			if (!configPage.configurationPanel.getLayout().equalsIgnoreCase("Default layout")) 
				configPage.configurationPanel.setLayout("Default layout");

			if (!configPage.configurationPanel.getDefaultView().equalsIgnoreCase("Home")) 
				configPage.configurationPanel.setDefaultView("Home");

			configPage.clickSaveButton();
			configPage.clickOKBtnOnSaveDialog();
			driver.quit();	
		}//End of finally
	}//ENd of Test

	//	/**
	//	 * Testcase ID:TC_038
	//	 * <br>Description: 'Replace with File (Upload) using 'View and Modify' section</br>
	//	 * @param dataValues
	//	 * @param context
	//	 * @throws Exception
	//	 */
	//	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_038",description="'Replace with File (Upload)' using 'View and Modify' section")
	//	public void TC_038(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	//	{
	//		//Instantiate Webdriver
	//		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
	//	    Log.testCaseInfo(" 'Replace with File (Upload) using 'View and Modify' section");
	//	    Log.addTestRunMachineInfo(driver,currentDriver,context);//get the execution environment details and print log report
	//		//Fetch test data from Excel sheet
	//		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);
	//	    
	//		try{
	//		
	//			//Step-1: Launch M-Files Application
	//			driver.get(webSite);
	//			Log.message("1. Launched the application.");
	//			
	//			//Step-3: Login to Application
	//			LoginPage loginPage=new LoginPage(driver);
	//			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
	//			Utils.waitForPageLoad(driver);
	//			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));
	//			
	//			//Step:4 Verify the Page title
	//	        
	//	        if (!driver.getTitle().contains("Web")) 
	//	        	throw new Exception("Unable to Login. Please verify Login Credentials.");
	//	        Log.message("3. Verified the Page title after Successfully LoggedIn to Application.");
	//	        
	//	    	//Step-5: Select the Search options
	//	        Utils.fluentWait(driver);
	//	        HomePage homePage=new HomePage(driver);
	//			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));
	//			
	//			if (!homePage.isSearchResults(driver,inputDataValues.get("Type"))&& searchedFile.isEmpty()) 
	//				throw new Exception("Could not Found the Document in Search list.");
	//	        Log.message("4. Document  found in Search results.");
	//
	//	        //Select Item from listview
	//	        homePage.listView.clickItem(inputDataValues.get("SearchTerm"));
	//	        
	//	        //Read List view column Headers
	//	        if (!homePage.readListViewHeaderNames(driver,"Checked Out To"))
	//	        {	        
	//	        	//Insert New Column to List view 
	//	        	homePage.listView.insertColumn("Checked Out To");
	//	        	//Verify if newly inserted column exists
	//	        	if (!homePage.readListViewHeaderNames(driver,"Checked Out To"))
	//	        		throw new Exception("'Checkout' Column Not added.");
	//	        }
	//	       	Log.message("5. 'Checkout' Column added to List View.");
	//	       
	//	        //Read List view column Headers
	//	        if (!homePage.readListViewHeaderNames(driver,"Version"))
	//	        {	        
	//	        	//Insert New Column to List view 
	//	        	homePage.listView.insertColumn("Version");
	//	        	//Verify if newly inserted column exists
	//	        	if (!homePage.readListViewHeaderNames(driver,"Version"))
	//	        		throw new Exception("'Version' Column Not added.");
	//	        }
	//        	Log.message("6. 'Version' Column added to List View.");
	//	        
	//	        
	//	        //CheckOut the object
	//	        if(!homePage.isObjectCheckedOut("Checked Out To",inputDataValues.get("UserName"))) {
	//	        	 homePage.taskPanel.selectViewAndModifyItemFromTaskPane("CheckOut");
	//	        }
	//	        Log.message("7. Document 'Checkouted' in the List View.");
	//	      
	//	        //get the columnIndex
	//			int colIndex=homePage.getListViewColumnIndex("Version");
	//			//verify CheckOut column value and see if it is checkedOut 
	//			String colValue=homePage.getListViewColumnValue("Version",colIndex);
	//			
	//			//Step-6: Select 'Replace with File (Upload) ' option from Menubar
	//	        if(!homePage.taskPanel.selectViewAndModifyItemFromTaskPane("ReplaceFile"))
	//	        	throw new Exception("Could not click 'Replace with File (Upload) ' button");
	//	        Log.message("8. Selected 'Replace with File (Upload) ' option from Menubar");
	//	        
	//	        //Step-7:Verify if User navigated back to Login form after LoggedOut
	//	        if(!homePage.replaceFile(inputDataValues.get("FileLocation"),inputDataValues.get("FileExtension"),colValue))   
	//	        	Log.fail("Unable to perform 'Replace with File (Upload) using 'View and Modify' section.", driver);
	//	        else 
	//	        	Log.pass("Successfully done 'Replace with File (Upload) using 'View and Modify' section.");
	//	        
	//		}
	//		catch(Exception e){
	//			Log.exception(e, driver);
	//		}//End of catch
	//		finally{
	//	       	//Close the browser
	//	        Log.endTaseCase();driver.quit(); 
	//		}//End of finally
	//	}//ENd of Test

	/**
	 * TestCase ID: TC_039
	 * <br>Description: Enter 'Comments' for the Object using Context Menu</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_039",description="Enter 'Comments' for the Object using Context Menu.")
	public void TC_039(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{

		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" Enter 'Comments' for the Object using Context Menu.");
		Log.addTestRunMachineInfo(driver,currentDriver,context);
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);
		HomePage homePage=null;
		try{

			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Login to Application
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);

			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));
			//Verify the Page title
			if (!driver.getTitle().contains("Web")) 
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			Log.message("3. Verified the Page title after Successfully LoggedIn to Application.");

			Utils.fluentWait(driver);
			homePage=new HomePage(driver);

			//Step-7: Select the Search options
			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));
			Log.message("4. Searched Document '"+inputDataValues.get("SearchTerm")+"' using basic search options.");

			if (!homePage.isSearchResults(driver,inputDataValues.get("Type"))&& searchedFile.isEmpty()) 
				throw new Exception("Could not Found the Document in Search list.");
			Log.message("5. Document '"+inputDataValues.get("SearchTerm")+"' found in Search results.");

			//Read List view column Headers
			if (!homePage.readListViewHeaderNames(driver,"Comment"))
			{	        
				//Insert New Column to List view 
				homePage.listView.insertColumn("Comment");
				//Verify if newly inserted column exists
				if (!homePage.readListViewHeaderNames(driver,"Comment")) 
					throw new Exception("'Comment' Column Not added.");
			}
			Log.message("5. 'Comment' Column added to List View.");
			//get columnID
			int colValue=homePage.getListViewColumnIndex("Comment");
			//Open list view context menu
			homePage.openContextMenuDialog(driver);
			//select 'comments' option from context menu
			homePage.selectContextMenuItemFromListView("Comments");
			//Enter comments for the object
			homePage.enterCommentsToObject(inputDataValues.get("Comments"));
			Log.message("6. 'Comments' added to the object in the List View.");
			homePage.clickRefresh();
			//Verify if comments are added to the object
			if (homePage.getListViewColumnValue("Comment",colValue).isEmpty()) 
				Log.fail("Test Failed...Unable to add comments to the Document", driver);
			else 
				Log.pass("Test Passed...Successfully add comments to the Document");
		}
		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally{
			//Close the browser
			driver.quit(); 
		}
	}//End of Test

	/**
	 * TC_044 : Convert Single document to Multi File Document(MFD) using Context Menu.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Smoke"}, 
			description = "Convert Single document to Multi File Document(MFD) using Context Menu.")
	public void TC_044(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = WebDriverUtils.getDriver();

		try {

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (!ListView.isSFDByItemName(driver, dataPool.get("ObjectName"))) //Checks if this is SFD
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not Single file document.");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if it is in checked in state
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-3 : Select Convert to MFD from context menu
			//---------------------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.ConvertToMFD_C.Value); //Clicks Convert SFD to MFD option from operations menu
			Utils.fluentWait(driver);

			String mfdName = dataPool.get("ObjectName").split("\\.")[0];

			Log.message("3. Convert SFD to MFD option is selected from context menu for SFD (" + dataPool.get("ObjectName") + ").");

			//Verification : To Verify if SFD object is converted to MFD object
			//-------------------------------------------------------------------			
			if (!ListView.isSFDByItemName(driver, mfdName))
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

	} //End TC_044

	/**
	 * TC_045. : Convert MFD document to Single File Document(SFD) using Context Menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Smoke"}, 
			description = "Convert MFD document to Single File Document(SFD) using Context Menu")
	public void TC_045(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = WebDriverUtils.getDriver();

		try {

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!ListView.isSFDByItemName(driver, dataPool.get("ObjectName"))) //Checks if this is SFD
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not Single file document.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if it is in checked in state
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-3 : Select Convert to MFD from context menu
			//---------------------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.ConvertToMFD_C.Value); //Clicks Convert SFD to MFD option from context menu
			Utils.fluentWait(driver);
			String mfdName = dataPool.get("ObjectName").split("\\.")[0];

			if (ListView.isSFDByItemName(driver, mfdName))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not converted to MFD document.");

			Log.message("3. Object (" + dataPool.get("ObjectName") + ") converted to MFD document.");

			//Step-4 : Select MFD and Convert to SFD from context menu
			//---------------------------------------------------------
			if (!homePage.listView.rightClickItem(mfdName)) //Selects the Object in the list
				throw new Exception("Object (" + mfdName + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.ConvertToSFD_C.Value); //Clicks Convert SFD to MFD option from context menu
			Utils.fluentWait(driver);

			Log.message("4. Convert MFD to SFD option is selected from context menu for Single file MFD (" + mfdName + ").");

			//Verification : To Verify if MFD is converted to SFD
			//---------------------------------------------------			
			if (ListView.isSFDByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Single file MFD is converted to SFD through operations menu.");
			else
				Log.fail("Test case Failed. Single file MFD is not converted to SFD through operations menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_045

	/**
	 * TestCase Id: TC_047
	 * <br>Description : 'GetHyperlink' of the object using Context Menu</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_047",description="'GetHyperlink' of the object using Context Menu.")
	public void TC_047(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{
		//Instantiate Webdriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" 'GetHyperlink' of the object using Context Menu.");
		Log.addTestRunMachineInfo(driver,currentDriver,context);
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);

		try{
			//Step-1:  And now use this to visit M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Step-3: Login to Application
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			//Step-4: Verify the Page title
			if (!driver.getTitle().contains("Web")) 
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			Log.message("3. Verified the Page title after Successfully LoggedIn to Application.");
			Utils.fluentWait(driver);
			HomePage homePage=new HomePage(driver);

			//Step-5: Search an object to see its relationships
			homePage.searchPanel.showAdvancedSearchOptions(driver);
			if (!homePage.searchPanel.selectSearchOptionsUsingObject(driver,MFilesObjectList.ASSIGNMENT, inputDataValues.get("Type")).contains("Assignments")) 
				throw new Exception("Could not select Basic Search options");

			//Step-7: Select the Search options
			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));
			if (!homePage.isDataInListView(driver, searchedFile, "Name")&&searchedFile.isEmpty()) 
				throw new Exception("Specified document doesnot exists in the Search results.");
			Log.message("4. Specified document exists in the Search results.");

			if(!homePage.listView.clickItemByIndex(0))
				throw new Exception("Unable to click item from search list");

			//Step-6: Open Context Menu otions
			if (!homePage.openContextMenuDialog(driver)) {
				throw new Exception("Could not open object context menu");
			}
			Log.message("5. Context Menu otions are displayed in Listview");
			//Step-7: Select the 'HyperLink' option from context menu
			homePage.selectContextMenuItemFromListView("GetHyperlink");
			Log.message("6. Selected the 'HyperLink' option from context menu");
			String objectLink=homePage.getObjectHyperLink();
			Log.message("7. Got the object 'HyperLink' using context menu option");
			Thread.sleep(1000);

			//Step-8: Navigate to the object link copied to see if object displayed correctly
			driver.get(objectLink);
			Utils.waitForPageLoad(driver);
			Utils.fluentWait(driver);
			Thread.sleep(1000);

			if (!homePage.isDataInListView(driver, searchedFile, "Name")) 
				Log.fail("Copied Hyperlink is not navigated to the required object while using 'Context' Menu", driver);
			else 
				Log.pass("Copied Hyperlink is successfully navigated to the required object while using 'Context' menu");
		}
		catch(Exception e){
			Log.exception(e, driver);
		}
		finally{
			//Close the browser
			driver.quit(); 
		}
	}

	/**
	 * TestCase Id: TC_048
	 * <br>Description : 'CheckOut' a Document from 'View and Modify' section of Taskpane</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_048",description="'CheckOut' a Document from 'View and Modify' section of homePage.taskPanel.")
	public void TC_048(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{
		//Instantiate Webdriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" 'CheckOut' a Document from 'View and Modify' section of homePage.taskPanel.");
		Log.addTestRunMachineInfo(driver,currentDriver,context);//get the execution environment details and print on Log report
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);

		try{

			//Step-1: And now use this to visit M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Step-3: Login to Application
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			HomePage homePage=new HomePage(driver);
			//Step-4: Verify the Page title
			if (!driver.getTitle().contains("Web")&&!homePage.isLoggedIn(inputDataValues.get("UserName"))) 
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			Log.message("3. Verified the Page title after Successfully LoggedIn to Application.");

			Utils.fluentWait(driver);
			//Step-5: Verify the display of SearchBar in HomePage
			if (!homePage.isSearchbarPresent(driver))
				throw new Exception("Search Panel not displayed.");
			Log.message("4. Search Panel displayed on HomePage.");

			Utils.fluentWait(driver);

			//Step-6: Search for an object and CheckOut the Object
			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));
			if (!homePage.isSearchResults(driver,inputDataValues.get("Type"))&&searchedFile.isEmpty()) 
				throw new Exception("Document/Object not found in Search results");
			Log.message("5. Search done Successfully for object '"+inputDataValues.get("SearchTerm")+"'.");

			//Read List view column Headers
			if (!homePage.readListViewHeaderNames(driver,"Checked Out To")) {	        
				//Insert New Column to List view 
				homePage.clickAndInsertListViewColumns(driver, "Insert Column->Standard Columns->Checked Out To");
				//Verify if newly inserted column exists
				if (!homePage.readListViewHeaderNames(driver,"Checked Out To"))
					throw new Exception("Checked Out To Column Not added.");
			}
			Log.message("7. 'Checked Out To' Column already added to List View.");

			//Step-7: Select the object from list view and CheckOut
			Log.message("8. Select the object from list view and CheckOut");
			if (!homePage.listView.clickItem(searchedFile)) //Checks if object selected in the list
				throw new Exception("Object (" + searchedFile + ") is not selected in the list.");

			if (homePage.isObjectCheckedOut("Checked Out To",inputDataValues.get("UserName"))) {
				if (!homePage.taskPanel.selectViewAndModifyItemFromTaskPane("CheckIn"))
					throw new Exception("Unable to checkIn the object");
			} 
			if (!homePage.taskPanel.selectViewAndModifyItemFromTaskPane("CheckOut"))
				throw new Exception("Could not select 'CheckOut' operation from 'TaskPane'");

			//Step-8:Verify if object is 'CheckeOut'
			if (!homePage.isObjectCheckedOut("Checked Out To",inputDataValues.get("UserName"))) 
				Log.fail("Test Failed..Could not 'CheckOut' Document from 'View and Modify' section ", driver);
			else 
				Log.pass("Test Passed..Document 'CheckedOut' Successfully from 'View and Modify' section ..!!!!");

		}
		catch(Exception e)
		{
			Log.exception(e, driver);
		}//End of catch
		finally{
			//Close the browser
			driver.quit(); 
		}//End of finally
	}//End of Test

	/**
	 * TestCase Id: TC_049
	 * <br>Description : Verify 'Workflow' dialog display from 'View and Modify' section of Taskpane</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */	
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_049",description=" Verify 'Workflow' dialog display from 'View and Modify' section of Taskpane")
	public void TC_049(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{
		//Instantiate Webdriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" Verify 'Workflow' dialog display from 'View and Modify' section of Taskpane");
		Log.addTestRunMachineInfo(driver,currentDriver,context);//get the execution environment details and print on Log report
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);

		try{

			//Step-1: And now use this to visit M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Step-3: Login to Application
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			//Step-4: Verify the Page title
			if (!driver.getTitle().contains("Web")) 
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			Log.message("3. Verify the Page title after Successfully LoggedIn to Application.");

			Utils.fluentWait(driver);
			HomePage homePage=new HomePage(driver);

			//Step-5: Search an object to see its relationships
			homePage.searchPanel.showAdvancedSearchOptions(driver);
			if (!homePage.searchPanel.selectSearchOptionsUsingObject(driver,MFilesObjectList.CUSTOMER, inputDataValues.get("Type")).contains("Customers")) 
				throw new Exception("Could not select Basic Search options");

			//Step-7: Select the Search options
			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));
			if (!homePage.isDataInListView(driver, searchedFile, "Name")&&searchedFile.isEmpty()) 
				throw new Exception("Specified document doesnot exists in the Search results.");
			Log.message("4. Specified document exists in the Search results.");

			//Step-7: Select the object from list view and CheckOut

			if (!homePage.listView.clickItem(searchedFile)) //Checks if object selected in the list
				throw new Exception("Object (" + searchedFile + ") is not selected in the list.");
			Log.message("6. Selected the object from list view ");


			if (!homePage.taskPanel.selectViewAndModifyItemFromTaskPane("Workflow"))
				throw new Exception("Could not select 'Workflow' operation from 'TaskPane'");
			Log.message("7. Object 'Workflow' operation selected.");

			MFilesDialog mFilesDialog=new MFilesDialog(driver);
			//Step-8:Verify if 'workflow' dialog displayed
			if (!mFilesDialog.isWorkflowDialogDisplayed()) 
				Log.fail("Test Failed..Could not display 'Workflow' dialog View and Modify section of Task pane.", driver);
			else 
				Log.pass("Test Passed..Document 'Workflow' dialog displayed successfully from View and Modify section of Task pane...!!!!");
		}
		catch(Exception e)
		{
			Log.exception(e, driver);
		}//End of catch
		finally{
			//Close the browser
			driver.quit(); 
		}//End of finally
	}//End of Test

	/**
	 * TestCase Id: TC_050
	 * <br>Description : 'CheckIn' the Document from 'view and Modify' task Pane</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */	
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_050",description="'CheckIn' the Document from 'view and Modify' task Pane.")
	public void TC_050(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{
		//Instantiate Webdriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" 'CheckIn' the Document from 'view and Modify' task Pane.");
		Log.addTestRunMachineInfo(driver,currentDriver,context);//get execution environment details and print on log report
		ConcurrentHashMap <String, String> inputDataValues = null;

		try{

			inputDataValues=new ConcurrentHashMap <String, String>(dataValues);
			// Step-1: Launch M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Step-3: Login to Application
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			//Step-4: Verify the Page title
			if (!driver.getTitle().contains("Web")) {
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			}
			Log.message("3. Verified the Page title after Successfully LoggedIn to Application.");

			Utils.fluentWait(driver);
			HomePage homePage=new HomePage(driver);

			//Step- 5: Verify the display of SearchBar in HomePage
			if (!homePage.isSearchbarPresent(driver))
				throw new Exception("Search Panel not displayed.");
			else
				Log.message("4. Search Panel displayed on HomePage.");
			Utils.fluentWait(driver);

			//Step-6: Search an object and 'CheckIn' using 'View and Modify' item
			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));
			if (!homePage.isSearchResults(driver,inputDataValues.get("Type"))&&searchedFile.isEmpty()) 
				throw new Exception("Could not find document in search list.");
			Log.message("5. Document Found in Search Results.");

			//Read List view column Headers
			if (!homePage.readListViewHeaderNames(driver,"Checked Out To"))
			{	        
				//Insert New Column to List view 
				homePage.clickAndInsertListViewColumns(driver,"Insert Column->Standard Columns->Checked Out To");
				//Verify if newly inserted column exists
				if (!homePage.readListViewHeaderNames(driver,"Checked Out To"))
					throw new Exception("'CheckedOutTo' Column Not added.");
			}
			Log.message("'CheckedOutTo' Column already added to List View.");

			//Step-7: Verify if object is 'CheckedOut'

			if (!homePage.listView.clickItem(searchedFile)) //Checks if object selected in the list
				throw new Exception("Object (" + searchedFile + ") is not selected in the list.");
			Log.message("6. Selected object to 'CheckedOut'");


			if (!homePage.isObjectCheckedOut("Checked Out To",inputDataValues.get("UserName"))) {
				if (!homePage.taskPanel.selectViewAndModifyItemFromTaskPane("CheckOut")) 
					throw new Exception("Could not select 'CheckOut' operation from 'TaskPane'");
			}       	
			//Step-8: CheckIn object using view and Modiy operations from task pane
			homePage.taskPanel.selectViewAndModifyItemFromTaskPane("CheckIn");
			Log.message("7. Selected 'CheckIn' option using view and Modiy operations from task pane");
			if (homePage.isObjectCheckedOut("Checked Out To",inputDataValues.get("UserName")))
				Log.fail("Could not 'CheckIn' the document", driver);
			else 
				Log.pass("Successfully 'CheckedIn' the document.");
		} 
		catch(Exception e){
			Log.exception(e, driver);
		}
		finally{
			//Close the browser
			driver.quit(); 
		}//End of finally
	}//End of Test

	/**
	 * TestCase ID : TC_051
	 * <br>Description : 'UndoCheckOut' a document from 'view and Modify' task Pane</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_051",description="'UndoCheckOut' a document from 'view and Modify' task Pane.")
	public void TC_051(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{

		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" 'UndoCheckOut' a document from 'view and Modify' task Pane.");
		Log.addTestRunMachineInfo(driver,currentDriver,context);
		ConcurrentHashMap <String, String> inputDataValues= new ConcurrentHashMap <String, String>(dataValues);
		HomePage homePage=null;
		try{

			// And now use this to visit M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Login to Application
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			Utils.fluentWait(driver);
			homePage=new HomePage(driver);

			if (!homePage.isTaskPaneDisplayed())
				throw new Exception("Task Pane not displayed.");
			Log.message("3. Successfully Task Pane Displayed.");

			if (!homePage.isSearchbarPresent(driver))
				throw new Exception("Search Panel not displayed.");
			Log.message("4. Search Panel displayed on HomePage.");

			Utils.fluentWait(driver);
			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));
			if (!homePage.isSearchResults(driver,inputDataValues.get("Type"))&&searchedFile.isEmpty()) 
				throw new Exception("Could not Find document in search list.");

			Log.message("5. Found '"+inputDataValues.get("SearchTerm")+"' object/file in Search Results.");
			//Read List view column Headers
			if (!homePage.readListViewHeaderNames(driver,"Checked Out To"))
			{	        
				//Insert New Column to List view
				homePage.clickAndInsertListViewColumns(driver, "Insert Column->Standard Columns->Checked Out To");
				//Verify if newly inserted column exists
				if (!homePage.readListViewHeaderNames(driver,"Checked Out To"))
					throw new Exception("Column 'Checked Out To' Not added.");
				Log.message("Column 'Checked Out To' inserted in the List view.");
			}
			Log.message("6. Column 'Checked Out To' already added to List View.");

			//Read List view column Headers
			if (!homePage.readListViewHeaderNames(driver,"Version")) {	        
				//Insert New Column to List view
				homePage.listView.insertColumn("Version");
				//Verify if newly inserted column exists
				if (!homePage.readListViewHeaderNames(driver,"Version"))
					throw new Exception("Column 'Version' not added.");
			}
			Log.message("7. Column 'Version' already added to List View.");


			if (!homePage.listView.clickItem(searchedFile)) //Checks if object selected in the list
				throw new Exception("Object (" + searchedFile + ") is not selected in the list.");
			Log.message("8. CheckedOut the Document using operations->CheckOut option.");
			Thread.sleep(500);

			if (!homePage.isObjectCheckedOut("Checked Out To",inputDataValues.get("UserName"))){
				if (!homePage.taskPanel.selectViewAndModifyItemFromTaskPane("CheckOut"))
					throw new Exception("Could not select 'CheckOut' operation from 'TaskPane'");
			}
			Utils.fluentWait(driver);
			if (!homePage.undoCheckOutObject("taskPaneMenu")) 
				Log.fail("Could not perform 'UndoCheckOut' from context menu..!!!!", driver);
			else 
				Log.pass("'UndoCheckOut' is done successfully from context menu.");

		}
		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally{
			//Close the browser
			driver.quit(); 
		}
	}

	/**
	 * TestCase ID: TC_052
	 * <br>Description : Click 'Relationships'  from 'View and Modify' section of TaskPane</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_052",description="Click 'Relationships'  from 'View and Modify' section of homePage.taskPanel.")
	public void TC_052(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{

		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" Click 'Relationships'  from 'View and Modify' section of homePage.taskPanel.");
		Log.addTestRunMachineInfo(driver,currentDriver,context);
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);
		try{

			// And now use this to visit M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Login to Application
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);

			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));
			//Verify the Page title
			if (!driver.getTitle().contains("Web")) {
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			}
			Log.message("4. Verified the Page title after Successfully LoggedIn to Application.");

			Utils.fluentWait(driver);
			HomePage homePage=new HomePage(driver);

			//Select Search Options
			if (!homePage.isSearchbarPresent(driver))
				throw new Exception("Search Panel not displayed.");
			Log.message("5. Search Panel displayed on HomePage.");
			Utils.fluentWait(driver);

			//Step-5: Search an object to see its relationships
			homePage.searchPanel.showAdvancedSearchOptions(driver);
			if (!homePage.searchPanel.selectSearchOptionsUsingObject(driver,MFilesObjectList.CUSTOMER, inputDataValues.get("Type")).contains("Customers")) 
				throw new Exception("Could not select Basic Search options");

			//Step-7: Select the Search options
			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));
			if (!homePage.isDataInListView(driver, searchedFile, "Name")&&searchedFile.isEmpty()) 
				throw new Exception("Specified document doesnot exists in the Search results.");
			Log.message("4. Specified document exists in the Search results.");

			if (!homePage.isSearchResults(driver,inputDataValues.get("Type"))&&searchedFile.isEmpty()) {
				throw new Exception("Could not Find document in search list.");
			}
			Log.message("6. Found '"+inputDataValues.get("SearchTerm")+"' object/file in Search Results.");

			if (!homePage.listView.clickItem(searchedFile)) {//Checks if object selected in the list
				throw new Exception("Object (" + searchedFile + ") is not selected in the list.");
			}

			if (!homePage.taskPanel.selectViewAndModifyItemFromTaskPane("Relationships"))
				throw new Exception("Could not select 'Relationships' operation from 'TaskPane'");
			Log.message("7. Object 'Relationships' operation selected.");

			if (!homePage.isViewPageDisplayed("Relationships"))
				Log.fail("Test Failed...!!!Could not navigate to Relationships Page using 'View and Modify' of homePage.taskPanel..!!!!", driver);
			else
				Log.pass("Test Passed..!!!Relationships Page Displayed using 'View and Modify' of homePage.taskPanel..!!!!");

		}
		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally{
			//Close the browser
			driver.quit(); 
		}
	}

	/**
	 * TestCase ID: TC_053
	 * <br>Description :Click 'History' from 'View and Modify' section of TaskPane</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_053",description="Click 'History'  from 'View and Modify' section of homePage.taskPanel.")
	public void TC_053(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{
		//Instantiate Webdriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" Click 'History' from 'View and Modify' section of homePage.taskPanel.");
		Log.addTestRunMachineInfo(driver,currentDriver,context);//get the execution environment details and print on log report

		try{
			//Fetch testdata from Excel sheet
			ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);
			//Step-1: Launch M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Step-3: Login to Application
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			//Step-4: Verify the Page title
			if (!driver.getTitle().contains("Web")) {
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			}
			Log.message("3. Verified the Page title after Successfully LoggedIn to Application.");

			Utils.fluentWait(driver);
			HomePage homePage=new HomePage(driver);

			//Select Search Options
			if (!homePage.isSearchbarPresent(driver))
				throw new Exception("Search Panel not displayed.");
			Log.message("5. Search Panel displayed on HomePage.");
			Utils.fluentWait(driver);

			//Step-5: Search an object to see its relationships
			homePage.searchPanel.showAdvancedSearchOptions(driver);
			if (!homePage.searchPanel.selectSearchOptionsUsingObject(driver,MFilesObjectList.CUSTOMER, inputDataValues.get("Type")).contains("Customers")) 
				throw new Exception("Could not select Basic Search options");

			//Step-7: Select the Search options
			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));
			if (!homePage.isDataInListView(driver, searchedFile, "Name")&&searchedFile.isEmpty()) 
				throw new Exception("Specified document doesnot exists in the Search results.");
			Log.message("4. Specified document exists in the Search results.");

			if (!homePage.isSearchResults(driver,inputDataValues.get("Type"))&&searchedFile.isEmpty()) {
				throw new Exception("Could not Find document in search list.");
			}
			Log.message("6. Found '"+inputDataValues.get("SearchTerm")+"' object/file in Search Results.");
			//Step-8: Check 'History' of the Document using TaskPane->View and Modify->History
			if (!homePage.listView.clickItem(searchedFile)) {//Checks if object selected in the list
				throw new Exception("Object (" + searchedFile + ") is not selected in the list.");
			}
			Log.message("6. Selected the Document from Search results.");
			//Select the History from 'View and Modify setion of taskpane
			if (!homePage.taskPanel.selectViewAndModifyItemFromTaskPane("History"))
				throw new Exception("Could not select 'History' operation from 'TaskPane'");
			Log.message("7. Object 'History' operation selected.");

			if (!homePage.isViewPageDisplayed("History"))
				Log.fail("Test Failed...!!!Could not navigate to History Page using 'View and Modify' of homePage.taskPanel..!!!!", driver);
			else
				Log.pass("Test Passed..!!!History Page Displayed using 'View and Modify' of homePage.taskPanel..!!!!");
		}catch(Exception e)
		{
			Log.exception(e, driver);
		}		
		finally{
			//Close the browser
			driver.quit(); 
		}//End of finally
	}//End of Test

	/**
	 * TC_054 : Verify 'Make Copy'  from 'View and Modify' section of homePage.taskPanel.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Smoke"}, 
			description = "Verify 'Make Copy'  from 'View and Modify' section of homePage.taskPanel.")
	public void TC_054(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = WebDriverUtils.getDriver();

		try {

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and click Make Copy from Taskpanel
			//-------------------------------------------------------------
			if (homePage.listView.itemCount() <= 0) //Checks if Item exists in the list
				throw new SkipException("No objects are found in the list.");

			if (!homePage.listView.clickItemByIndex(0)) //Right clicks the project
				throw new Exception("First object in the list is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.MakeCopy.Value); //Selects Make copy from taskpanel

			Log.message("2. Make copy from taskpanel is selected from taskpanel.");

			//Step-3 : Open the Properties dialog of the new document through context menu
			//-----------------------------------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			metadatacard.setInfo(dataPool.get("Properties"));
			ConcurrentHashMap <String, String> prevInfo = metadatacard.getInfo(); //Gets the property information			

			Log.message("3. New object details are entered in make copy metadatacard.", driver);

			//Step-4 : Save the metadatacard
			//------------------------------
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			String docName = prevInfo.get("Name or title") + "" + prevInfo.get("Extension");

			Log.message("4. New Document metadatacard is saved.", driver);

			//Step-5 : Open the Properties dialog of the new document through context menu
			//-----------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(docName)) //Checks if Item exists in the list
				throw new Exception("Newly created document (" + docName + ") does not exists in the list.");

			if (!homePage.listView.rightClickItem(docName)) //Right clicks the project
				throw new Exception("Newly created document (" + docName + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("5. Properties dialog of the new document (" + docName + ") is opened through context menu.", driver);

			//Step-6 : Obtain metadatacard information and compare the properties before and after saving metadatacard
			//---------------------------------------------------------------------------------------------------------
			ConcurrentHashMap <String, String> currInfo = metadatacard.getInfo(); //Gets the property information
			String diff = Utility.compareObjects(currInfo, prevInfo);

			Log.message("6. Current Document metadatacard details are obtained and compared with the entered values.");

			//Verification : Verifies if Mark Complete Icon is displayed
			//----------------------------------------------------------
			if (diff.equalsIgnoreCase(""))
				Log.pass("Test case Passed. New Document (" + docName + ") is created successfully from taskpanel.");
			else
				Log.fail("Test case Failed. New Document (" + docName + ") is creation is not successful. Additonal Information : " + diff, driver);

			metadatacard.closeMetadataCard(); //Closes Metadatacard

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_054

	/**
	 * TestCase ID: TC_062
	 * <br>Description : 'CheckOut' a Document from Settings->Operations</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_062",description="'CheckOut' a Document from Settings->Operations->CheckOut")
	public void TC_062(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{

		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" 'CheckOut' a Document from Settings->Operations->CheckOut");
		Log.addTestRunMachineInfo(driver,currentDriver,context);

		try{
			ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);
			// And now use this to visit M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Login to Application
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			//Verify the Page title
			if (!driver.getTitle().contains("Web")) {
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			}
			Log.message("3. Verified the Page title after Successfully LoggedIn to Application.");
			Utils.fluentWait(driver);
			HomePage homePage=new HomePage(driver);

			//Step-5: Search an object to see its relationships
			homePage.searchPanel.showAdvancedSearchOptions(driver);
			if (!homePage.searchPanel.selectSearchOptionsUsingObject(driver,MFilesObjectList.CUSTOMER, inputDataValues.get("Type")).contains("Customers")) 
				throw new Exception("Could not select Basic Search options");

			//Step-7: Select the Search options
			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));
			if (!homePage.isDataInListView(driver, searchedFile, "Name")&&searchedFile.isEmpty()) 
				throw new Exception("Specified document doesnot exists in the Search results.");
			Log.message("4. Specified document exists in the Search results.");

			if (!homePage.isSearchResults(driver,inputDataValues.get("Type"))&&searchedFile.isEmpty()) {
				throw new Exception("Could not Find document in search list.");
			}
			Log.message("6. Found '"+inputDataValues.get("SearchTerm")+"' object/file in Search Results.");

			//Read List view column Headers
			if (!homePage.readListViewHeaderNames(driver,"Checked Out To"))
			{	        
				//Insert New Column to List view 
				homePage.clickAndInsertListViewColumns(driver,"Insert Column->Standard Columns->Checked Out To");
				//Verify if newly inserted column exists
				if (!homePage.readListViewHeaderNames(driver,"Checked Out To"))
					throw new Exception("'Checkout' Column Not added.");
				Log.message("'Checkout' Column inserted in the List view.");
			}else {
				Log.message("'Checkout' Column already added to List View.");
			}


			//Step-7: Select the Object from listview
			Log.message("7. Select the Object from listview");
			if (!homePage.listView.clickItem(searchedFile)) {//Checks if object selected in the list
				throw new Exception("Object (" + searchedFile + ") is not selected in the list.");
			}
			//Step-8: Verify if Object is checkedOut
			Log.message("8. Verify if Object is checkedOut");
			if (homePage.isObjectCheckedOut("Checked Out To",inputDataValues.get("UserName"))) {
				homePage.menuBar.selectFromOperationsMenu("CheckIn");
			}
			else  {    //Select the Operations menu item->CheckOut
				Log.message("Select the Operations menu item->CheckOut  ");
				homePage.menuBar.selectFromOperationsMenu("CheckOut");
			}    

			//Step-9: Verify if Object is checkedOut
			Log.message("9. Verify if Object is checkedOut");
			if (!homePage.isObjectCheckedOut("Checked Out To",inputDataValues.get("UserName")))
				Log.fail("Test Failed..Could not 'CheckOut' Document using Operations->CheckOut options", driver);
			else
				Log.pass("Test Passed..Document 'CheckedOut' Successfully using Operations->CheckOut options..!!!!");
		}
		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally{
			//Close the browser
			driver.quit(); 
		}
	}

	/**
	 * TestCase ID : TC_063
	 * <br>Description : 'CheckIn' the Document from Settings->Operations</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_063",description="'CheckIn' the Document from Settings->Operations->CheckIn")
	public void TC_063(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{

		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" 'CheckIn' the Document from Settings->Operations->CheckIn");
		Log.addTestRunMachineInfo(driver,currentDriver,context);
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);
		try{


			//Step-1 : And now use this to visit M-Files Application
			Log.message("1. Launch the application.");
			driver.get(webSite);

			//Step-2: Fetch Login details from Testdata sheet
			Log.message("2. Fetch Data from Testdata sheet.");
			LoginPage loginPage=new LoginPage(driver);

			if (inputDataValues.get("UserName").isEmpty()||inputDataValues.get("Password").isEmpty())
				throw new SkipException("Unable to Fetch Data from Testdata sheet.");

			//Step-3 : Login to Application
			Log.message("3. Login to the application using valid credentials.");
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);

			Log.message("Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			//Step-4 : Verify the Page title
			Log.message("4. Verify the Page title.");
			if (!driver.getTitle().contains("Web")) {
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			}
			Log.message("---Successfully LoggedIn to Application.");

			//Wait for Pageload
			Utils.fluentWait(driver);
			HomePage homePage=new HomePage(driver);

			//Step-5 :Verify the display of SearchBar in HomePage
			Log.message("5. Verify the display of SearchBar in HomePage.");
			if (!homePage.isSearchbarPresent(driver))
				throw new Exception("Search Panel not displayed.");
			Log.message("---Search Panel displayed on HomePage.");
			Utils.fluentWait(driver);

			//Step-5: Search an object to see its relationships
			homePage.searchPanel.showAdvancedSearchOptions(driver);
			if (!homePage.searchPanel.selectSearchOptionsUsingObject(driver,MFilesObjectList.CUSTOMER, inputDataValues.get("Type")).contains("Customers")) 
				throw new Exception("Could not select Basic Search options");

			//Step-7: Select the Search options
			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));
			if (!homePage.isDataInListView(driver, searchedFile, "Name")&&searchedFile.isEmpty()) 
				throw new Exception("Specified document doesnot exists in the Search results.");
			Log.message("4. Specified document exists in the Search results.");

			if (!homePage.isSearchResults(driver,inputDataValues.get("Type"))&&searchedFile.isEmpty()) {
				throw new Exception("Could not Find document in search list.");
			}
			Log.message("6. Found '"+inputDataValues.get("SearchTerm")+"' object/file in Search Results.");

			//Read List view column Headers
			if (!homePage.readListViewHeaderNames(driver,"Checked Out To"))
			{	        
				//Insert New Column to List view 
				homePage.clickAndInsertListViewColumns(driver,"Insert Column->Standard Columns->Checked Out To");
				//Verify if newly inserted column exists
				if (!homePage.readListViewHeaderNames(driver,"Checked Out To"))
					throw new Exception("'Checked Out To'  Column Not added.");
			}
			Log.message("'Checked Out To' Column already added to List View.");

			//Step-7: Select the Object from List view
			Log.message("7. Select the Object from List view");


			if (!homePage.listView.clickItem(searchedFile)) {//Checks if object selected in the list
				throw new Exception("Object (" + searchedFile + ") is not selected in the list.");
			}

			//Step-8: Verify if object is checkedOut
			Log.message("8. Verify if object is checkedOut, if not CheckOut");
			if (!homePage.isObjectCheckedOut("Checked Out To",inputDataValues.get("UserName")))
				homePage.menuBar.selectFromOperationsMenu("CheckOut");

			//Step-9: Select 'CheckIn' option for selected object
			Log.message("9.  Select 'CheckIn' option for selected object");
			homePage.menuBar.selectFromOperationsMenu("CheckIn");

			//Step-10: Verify if 'CheckIn' is Success
			Log.message("10. Verify if 'CheckIn' is Success");
			if (homePage.isObjectCheckedOut("Checked Out To",inputDataValues.get("UserName")))
				Log.fail("Could not 'CheckIn' the document", driver);
			else
				Log.pass("Successfully 'CheckedIn' the document.");
		}
		catch(Exception e){
			Log.exception(e, driver);
		}		
		finally{

			//Close the browser
			driver.quit(); 
		}
	}//End of Test

	/**
	 * TestCase ID: TC_064
	 * <br>Description :'CheckIn' the Document with Comments from Settings->Operations->CheckIn</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */	
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_064",description="CheckIn Document with Comments from Settings->Operations->CheckIn")
	public void TC_064(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" 'CheckIn' the Document with Comments from Settings->Operations->CheckIn");
		Log.addTestRunMachineInfo(driver,currentDriver,context);

		try{

			ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);
			//Step-1: Launch M-Files Application
			Log.message("1. Launch the application.");
			driver.get(webSite);

			//Step-2: Fetch Login details from Testdata sheet
			Log.message("2. Fetch Data from Testdata sheet.");
			LoginPage loginPage=new LoginPage(driver);

			//Step-3:  Login to Application
			Log.message("3. Login to the application using valid credentials.");
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);

			Log.message("Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));
			//Step-4:  Verify the Page title
			Log.message("4. Verify the Page title.");
			HomePage homePage=new HomePage(driver);

			if (!driver.getTitle().contains("Web")&& !homePage.isLoggedIn(inputDataValues.get("UserName"))) 
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			Log.message("Verified Page title after Successful LoggedIn to Application.");

			Utils.fluentWait(driver);
			//Step-5: Verify the display of SearchBar in HomePage
			Log.message("5. Verify the display of SearchBar in HomePage.");
			if (!homePage.isSearchbarPresent(driver))
				throw new Exception("Search Panel not displayed.");
			Log.message("Search Panel displayed on the HomePage.");
			Utils.fluentWait(driver);
			//Step-5: Search an object to see its relationships
			homePage.searchPanel.showAdvancedSearchOptions(driver);
			if (!homePage.searchPanel.selectSearchOptionsUsingObject(driver,MFilesObjectList.DOCUMENT, inputDataValues.get("Type")).contains("Documents")) 
				throw new Exception("Could not select Basic Search options");

			//Step-7: Select the Search options
			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));
			if (!homePage.isDataInListView(driver, searchedFile, "Name")&&searchedFile.isEmpty()) 
				throw new Exception("Specified document doesnot exists in the Search results.");
			Log.message("4. Specified document exists in the Search results.");

			if (!homePage.isSearchResults(driver,inputDataValues.get("Type"))&&searchedFile.isEmpty()) {
				throw new Exception("Could not Find document in search list.");
			}
			Log.message("6. Found '"+inputDataValues.get("SearchTerm")+"' object/file in Search Results.");
			//Read List view column Headers
			if (!homePage.readListViewHeaderNames(driver,"Checked Out To"))
			{	        
				//Insert New Column to List view 
				homePage.clickAndInsertListViewColumns(driver,"Insert Column->Standard Columns->Checked Out To");
				//Verify if newly inserted column exists
				if (!homePage.readListViewHeaderNames(driver,"Checked Out To"))
					throw new Exception("'Checked Out to' Column Not added.");
				Log.message("'CheckedOut to' Column inserted in the List view.");
			}

			//Step-8: CheckOut the Document using ContextMenu option
			Log.message("8. CheckOut the Document using ContextMenu option.");

			if (!homePage.listView.clickItem(searchedFile)) {//Checks if object selected in the list
				throw new Exception("Object (" + searchedFile + ") is not selected in the list.");
			}


			if (!homePage.isObjectCheckedOut("Checked Out To",inputDataValues.get("UserName")))
				homePage.menuBar.selectFromOperationsMenu("CheckOut");

			Thread.sleep(1000);
			//Step-9: 'CheckIn' the Document with 'Comments' using operations menu option
			Log.message("9. 'CheckIn' the Document with 'Comments' using operations menu option.");
			if(homePage.menuBar.selectFromOperationsMenu("CheckInWithComments"))
				homePage.enterCommentsToObject(inputDataValues.get("Comments"));
			else
				throw new Exception("Unable to select the operations menu items");

			//Step-10: Verify if Object is checkedIn with comments
			if (homePage.isObjectCheckedOut("Checked Out To",inputDataValues.get("UserName")))
				Log.fail("Could not 'CheckIn with Comments'", driver);
			else
				Log.pass("Successfully 'CheckedIn document with Comments.");

		}
		catch(Exception e) {
			Log.exception(e, driver);
		}
		finally{

			//Close the browser
			driver.quit(); 
		}
	}

	/**
	 * TC_065 : 'UndoCheckOut' a document using Settings->Operations->UndoCheckOut
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "'UndoCheckOut' a document using Settings->Operations->UndoCheckOut")
	public void TC_065(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = WebDriverUtils.getDriver();

		try {

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform checkout operation
			//---------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-3 : Select Undo-Checkout from operations menu
			//---------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			Log.message("3. Object (" + dataPool.get("ObjectName") + ") is undo-checked out.", driver);

			//Step-4 : Confirm undo checkout
			//-------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true); //Confirms undo-checkout operation

			Log.message("4. Undo-checkout is confirmed in M-Files dialog.");

			//Verification : To Verify if object undo-checkout is successful
			//---------------------------------------------------------------------------			
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Undo-Checkedout of an object (" + dataPool.get("ObjectName") + ") is successful.");
			else
				Log.fail("Test case Failed. Undo-Checkedout of an object (" + dataPool.get("ObjectName") + ") is not successful.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			Utility.quitDriver(driver);
		} //End finally

	} //End TC_065
	;	
	/**
	 * TestCase ID: TC_067
	 * <br>Description : Click 'Relationships' from Operations->Relationships</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_030",description="Click 'Relationships' from Operations->Relationships")
	public void TC_067(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{

		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" Click 'Relationships' from Operations->Relationships");
		Log.addTestRunMachineInfo(driver,currentDriver,context);
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);

		try{

			//Step-1: And now use this to visit M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Step-2: Fetch Login details from Testdata sheet
			Log.message("2. Fetch Data from Testdata sheet.");
			LoginPage loginPage=new LoginPage(driver);

			//Step-3: Login to Application
			Log.message("3. Login to the application using valid credentials.");
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);

			Log.message("Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));
			//Step-4: Verify the Page title
			Log.message("4. Verify the Page title.");

			if (!driver.getTitle().contains("Web")) {
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			}
			Log.message("---Successfully LoggedIn to Application.");

			Utils.fluentWait(driver);
			HomePage homePage=new HomePage(driver);

			//Step-5: Verify the display of SearchBar in HomePage
			Log.message("5. Verify the display of SearchBar in HomePage.");
			if (!homePage.isSearchbarPresent(driver))
				throw new Exception("Search Panel not displayed.");
			Log.message("---Search Panel displayed on HomePage.");
			Utils.fluentWait(driver);

			//Step-6: Search an object and click 'Relationsipes from operations menu
			Log.message("6. Enter Search Term as "+inputDataValues.get("SearchTerm")+" and verify if Search Results correct.");

			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));

			if (!homePage.isSearchResults(driver,inputDataValues.get("Type"))&&searchedFile.isEmpty()) {
				throw new Exception("Could not Find document in search list.");
			}
			Log.message("8. Check 'Relationships' of the Document using Operations->Relationships.");


			//Step-7: Select object from list view
			Log.message("7. Select object from list view");


			if (!homePage.listView.clickItem(searchedFile)) {//Checks if object selected in the list
				throw new Exception("Object (" + searchedFile + ") is not selected in the list.");
			}

			//Step-8: Select 'Relationships' option from Operations menu
			Log.message("8. Select 'Relationships' option from Operations menu");
			homePage.menuBar.selectFromOperationsMenu("Relationships");

			//Step-9: Verify if 'Relationship' view displayed
			Log.message("9. Verify if 'Relationship' view displayed");
			if (!homePage.isViewPageDisplayed("Relationships"))
				Log.fail("Test Failed...!!!Could not navigate to Relationships Page..!!!!", driver);
			else
				Log.pass("Test Passed..!!!Relationships Page Displayed..!!!!");

		}
		catch(Exception e)		{
			Log.exception(e, driver);
		}

		finally{

			//Close the browser
			driver.quit(); 
		}
	}

	/**
	 * TestCase ID: TC_068
	 * <br>Description: Click 'History' from Operations->History</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_068",description="Click 'History' from Operations->History")
	public void TC_068(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{

		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" Click 'History' from Operations->Histroy");
		Log.addTestRunMachineInfo(driver,currentDriver,context);
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);

		try{

			//Step-1:  And now use this to visit M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Step-2: Fetch Login details from Testdata sheet
			Log.message("2. Fetch Data from Testdata sheet.");
			LoginPage loginPage=new LoginPage(driver);

			//Step-3: Login to Application
			Log.message("3. Login to the application using valid credentials.");
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);

			Log.message("Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));
			//Step-4: Verify the Page title
			Log.message("4. Verify the Page title.");

			if (!driver.getTitle().contains("Web")) {
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			}
			Log.message("---Successfully LoggedIn to Application.");

			Utils.fluentWait(driver);
			HomePage homePage=new HomePage(driver);

			//Step-5: Verify the display of SearchBar in HomePage
			Log.message("5. Verify the display of SearchBar in HomePage");
			if (!homePage.isSearchbarPresent(driver))
				throw new Exception("Search Panel not displayed.");
			Log.message("---Search Panel displayed on HomePage.");
			Utils.fluentWait(driver);

			//Step-6: Search for an object and select 'History from operations menu
			Log.message("6. Enter Search Term as "+inputDataValues.get("SearchTerm")+" and verify if Search Results correct.");

			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));
			if (!homePage.isSearchResults(driver,inputDataValues.get("Type"))&&searchedFile.isEmpty()) {
				throw new Exception("Could not Find document in search list.");
			}
			Log.message("8. Check 'History' of the Document using Operations->History.");


			//Step-7: Select the object from Listview
			Log.message("7. Select the object from Listview");

			if (!homePage.listView.clickItem(searchedFile)) {//Checks if object selected in the list
				throw new Exception("Object (" + searchedFile + ") is not selected in the list.");
			}

			//Step-8: Select 'History' from operations menu
			Log.message("8. Select 'History' from operations menu");
			homePage.menuBar.selectFromOperationsMenu("History");

			//Step-9: Verify if user navigated to 'History view'
			Log.message("9.  Verify if user navigated to 'History view'");
			if (!homePage.isViewPageDisplayed("History")) {
				Log.fail("Test Failed...!!!Could not navigate to History Page..!!!!", driver);
			}
			else {
				Log.pass("Test Passed..!!!History Page Displayed..!!!!");
			}
		}
		catch(Exception e)
		{
			Log.exception(e, driver);
		}
		finally{

			//Close the browser
			driver.quit(); 
		}
	}

	/**
	 * TestCase ID: TC_69
	 * <br>Description : Enter 'Comments' for the Object using Operations->Comments</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_069",description="Enter 'Comments' for the Object using Operations->Comments.")
	public void TC_069(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{
		//Instantiate Webdriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" Enter 'Comments' for the Object using Operations->Comments.");
		Log.addTestRunMachineInfo(driver,currentDriver,context);
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);
		HomePage homePage=null;
		try{

			//Step-1:  And now use this to visit M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Step-2: Fetch Login details from Testdata sheet
			LoginPage loginPage=new LoginPage(driver);
			//Step-3: Login to Application
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			//Step-4: Verify the Page title
			if (!driver.getTitle().contains("Web")) 
				throw new Exception("Unable to Verify the Page title.");
			Log.message("3. Verified the Page title.");

			Utils.fluentWait(driver);
			homePage=new HomePage(driver);

			//Step-5: Select the Search options
			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));
			if (!homePage.isSearchResults(driver,inputDataValues.get("Type"))&&searchedFile.isEmpty()) 
				throw new Exception("Could not Found the Document in Search list.");
			Log.message("4. Document  found in Search results.");

			//Read List view column Headers
			if (!homePage.readListViewHeaderNames(driver,"Comment")) {	        
				//Insert New Column to List view 
				homePage.listView.insertColumn("Comment");
				//Verify if newly inserted column exists
				if (!homePage.readListViewHeaderNames(driver,"Comment"))
					throw new Exception("'Comment Column Not added.");
			}
			Log.message("'Comment'  Column added to List View.");

			int colValue=homePage.getListViewColumnIndex("Name");
			//Step-6: Select the object from listview
			Log.message("6.Select the object from listview");
			//Checks if object selected in the list
			if (!homePage.listView.clickItem(searchedFile)) 
				throw new Exception("Object (" + searchedFile + ") is not selected in the list.");

			//Step-7: Select the 'Operations->Comments' option
			Log.message("7. Select the 'Operations->Comments' option");
			homePage.menuBar.selectFromOperationsMenu("Comments");

			//Step-8: Enter Comments for the Object
			Log.message("8. Enter Comments for the Object");
			homePage.enterCommentsToObject(inputDataValues.get("Comments"));
			homePage.clickRefresh();
			colValue=homePage.getListViewColumnIndex("Comment");

			//Step-9: Verify if 'Comments' are added to the object
			Log.message("9. Verify if 'Comments' are added to the object");
			if (homePage.getListViewColumnValue("Comment",colValue).isEmpty()) 
				Log.fail("Test Failed...Unable to add comments to the Document", driver);
			else 
				Log.pass("Test Passed...Successfully add comments to the Document");
		}
		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally{

			//Close the browser
			driver.quit(); 
		}
	}

	/**
	 * TestCase ID: TC_70
	 * <br>Description : Verify 'Properties' dialog displaye using Operations->Properties</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_070",description="Verify 'Properties' dialog displaye using Operations->Properties")
	public void TC_070(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{
		//Instantiate Webdriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" Verify 'Properties' dialog displayed using Operations->Properties.");
		Log.addTestRunMachineInfo(driver,currentDriver,context);
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);

		try{

			//Step-1:  And now use this to visit M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Step-2: Fetch Login details from Testdata sheet
			LoginPage loginPage=new LoginPage(driver);

			//Step-3: Login to Application
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			//Step-4: Verify the Page title
			if (!driver.getTitle().contains("Web")) 
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			Log.message("3. Successfully LoggedIn to Application & WebPage title verified.");

			Utils.fluentWait(driver);
			HomePage homePage=new HomePage(driver);

			//Step-5: Select the Search options
			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));

			if (!homePage.isSearchResults(driver,inputDataValues.get("Type"))&&searchedFile.isEmpty()) 
				throw new Exception("Could not Found the Document in Search list.");
			Log.message("4. Searched Document  found in Search results.");

			//Step-6: Select the object from listview
			if (!homePage.listView.clickItem(searchedFile)) 
				throw new Exception("Object (" + searchedFile + ") is not selected in the list.");
			Log.message("6.Selected the object from listview");

			//Step-7: Select the 'Operations->Properties' option
			homePage.menuBar.selectFromOperationsMenu("Properties");
			Log.message("7. Selected the 'Operations->Properties' option");

			//Step-8: Verify if 'Properties' displayed for the object
			Log.message("8. Verify if 'Properties' displayed for the object");
			if (!homePage.verifyMetadataCardDisplay(inputDataValues.get("SearchTerm"))) 
				Log.fail("Test Failed...Unable to display 'Properties' dialog for the selected Object using Operations Menu.", driver);
			else 
				Log.pass("Test Passed...Successfully displayed 'Properties' dialog for the selected Object using Operations Menu.");
		}
		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally{

			//Close the browser
			driver.quit(); 
		}
	}

	/**
	 * TestCase ID: TC_71
	 * <br>Description: 'GetHyperLink' of an object Operations->GetHyperLink</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_071",description="'GetHyperLink' of an object Operations->GetHyperLink")
	public void TC_071(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{
		//Instantiate Webdriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" 'GetHyperLink' of an object Operations->GetHyperLink");
		Log.addTestRunMachineInfo(driver,currentDriver,context);//get the execution envronment details and print Log report

		try{
			//Fetch testdata from Excel
			ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);
			// And now use this to visit M-Files Application
			Log.message("1. Launch the application.");
			driver.get(webSite);

			//Fetch Login details from Testdata sheet
			Log.message("2. Fetch Data from Testdata sheet.");
			LoginPage loginPage=new LoginPage(driver);

			if (inputDataValues.get("UserName").isEmpty()||inputDataValues.get("Password").isEmpty())
				throw new SkipException("Unable to Fetch Data from Testdata sheet.");

			//Login to Application
			Log.message("3. Login to the application using valid credentials.");
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);

			Log.message("Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));
			//Verify the Page title
			Log.message("4. Verify the Page title.");

			if (!driver.getTitle().contains("Web")) {
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			}
			Log.message("---Successfully LoggedIn to Application.");

			Utils.fluentWait(driver);
			HomePage homePage=new HomePage(driver);

			//Step-5: Search an object to see its relationships
			homePage.searchPanel.showAdvancedSearchOptions(driver);
			if (!homePage.searchPanel.selectSearchOptionsUsingObject(driver,MFilesObjectList.REPORT, inputDataValues.get("Type")).contains("Report")) 
				throw new Exception("Could not select Basic Search options");

			//Step-7: Select the Search options
			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));
			if (!homePage.isDataInListView(driver, searchedFile, "Name")&&searchedFile.isEmpty()) 
				throw new Exception("Specified document doesnot exists in the Search results.");
			Log.message("4. Specified document exists in the Search results.");

			if (!homePage.isSearchResults(driver,inputDataValues.get("Type"))&&searchedFile.isEmpty()) {
				throw new Exception("Could not Find document in search list.");
			}
			Log.message("6. Found '"+inputDataValues.get("SearchTerm")+"' object/file in Search Results.");


			// GetHyperLink
			Log.message("6. 'GetHyperLink' of the Object using Operations->GetHyperLink.");


			if (!homePage.listView.clickItem(searchedFile)) {//Checks if object selected in the list
				throw new Exception("Object (" + searchedFile + ") is not selected in the list.");
			}

			//Instantiate the MenuBar object

			homePage.menuBar.selectFromOperationsMenu("GetHyperlink");

			String objectLink=homePage.getObjectHyperLink();
			//Launch the object link copied     
			driver.get(objectLink);
			Utils.waitForPageLoad(driver);

			if (!homePage.isDataInListView(driver, searchedFile, "Name")) {
				Log.fail("Copied Hyperlink is not navigated to the required object while using 'Operations' Menu", driver);
			}
			else {
				Log.pass("Copied Hyperlink is successfully navigated to the required object while using 'Operations' menu");
			}

		}
		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally{

			//Close the browser
			driver.quit(); 
		}
	}

	/**
	 * TestCase ID: TC_072
	 * <br>Description: Click 'Add to Favorites' using Operations->Add to Favorites</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_072",description="Click 'Add to Favorites'  using  Operations->Add to Favorites")
	public void TC_072(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{
		//Fetch testdata from Excel sheet
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo("Click 'Add to Favorites' using Operations->Add to Favorites");
		Log.addTestRunMachineInfo(driver,currentDriver,context);//get the execution environment details and print on log report
		//Fetch testdata
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);

		try{

			//Step-1: Launch M-Files Application
			Log.message("1. Launch the application.");
			driver.get(webSite);

			//Step-2: Fetch Login details from Testdata sheet
			LoginPage loginPage=new LoginPage(driver);

			//Step-3:  Login to Application
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);

			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));
			//Step-4:  Verify the Page title
			if (!driver.getTitle().contains("Web")) {
				throw new Exception("Unable to Verify the Page title.");
			}
			Log.message("3. Verified the Page title.");
			Utils.fluentWait(driver);
			HomePage homePage=new HomePage(driver);

			//Step-5: Select the Search options
			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));
			if (!homePage.isSearchResults(driver,inputDataValues.get("Type"))&&searchedFile.isEmpty()) {
				throw new Exception("Specified document doesnot exists in the Search results.");
			}
			Log.message("4. Specified document exists in the Search results.");

			//Step-6: Perform 'Add to Favorites' using  Operations->Add to Favorites
			if (!homePage.listView.clickItem(searchedFile)) {//Checks if object selected in the list
				throw new Exception("Object (" + searchedFile + ") is not selected in the list.");
			}

			//Step-7: Select 'AddToFavorites' from Operations menu
			homePage.menuBar.selectFromOperationsMenu("AddToFavorites");
			Log.message("5. Selected 'AddToFavorites' from Operations menu");

			if (!homePage.isConfirmationDialogDisplayed("affected."))
				throw new Exception("Could not add to AddToFavorites Page..!!!!");
			Log.message("6. AddToFavorites done.");

			//Step-8: Navigate to 'Favorites' view
			if (!homePage.navigateToGotoViews("Favorites"))
				throw new Exception("Unable to navigate to 'Favorites'");
			Log.message("7. Successfully navigated to 'Favorites'");

			//Step-9: Verify if Document moved to 'Add to Favorites' view
			if (!homePage.isDataInListView(driver, inputDataValues.get("SearchTerm"),"Name")) 
				Log.fail("Test Failed...!!!Unable to add document to 'Favorites' using Settings->Operations Menu", driver);
			else
				Log.pass("Test Passed..!!!Document Successfully added to 'Favorites' using Settings->Operations Menu");
		}
		catch(Exception e) {
			Log.exception(e, driver);
		}//End of catch
		finally{

			//Close the browser
			driver.quit(); 
		}//End of finally
	}//End of Test

	/**
	 * TestCase ID: TC_073
	 * <br>Description: Click 'Remove from Favorites' usign Settings->Operations menu</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_073",description="Click 'Remove from Favorites' using Settings->Operations")
	public void TC_073(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{
		//Instantiate WebDriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" Click 'Remove from Favorites' usign Settings->Operations menu");
		Log.addTestRunMachineInfo(driver,currentDriver,context);//get the execution environment details and prints on log report
		//Fetch testdata from Excel sheet
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);

		try{

			//Step-1: And now use this to visit M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Step-3: Login to Application
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			//Step-4: Verify the Page title
			HomePage homePage=new HomePage(driver);
			if (!driver.getTitle().contains("Web")&& !homePage.isLoggedIn(inputDataValues.get("UserName"))) 
				throw new Exception("Unable to Verify the Page title.");
			Log.message("3. Verified the Page title.");

			Utils.fluentWait(driver);
			//Step-5: Search an object and preform 'Remove from 'Favorites'
			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));

			if (!homePage.isSearchResults(driver,inputDataValues.get("Type"))&&searchedFile.isEmpty()) 
				throw new Exception("Specified document doesnot exists in the Search results.");
			Log.message("4. Specified document exists in the Search results.");

			//Step-6: 'Add to Favorites' the Document using Settings->Operations Menu
			if (!homePage.listView.clickItem(searchedFile)) //Checks if object selected in the list
				throw new Exception("Object (" + searchedFile + ") is not selected in the list.");
			Log.message("5. 'Add to Favorites' the Document using Settings->Operations Menu.");

			homePage.menuBar.selectFromOperationsMenu("AddToFavorites");
			if (!homePage.isConfirmationDialogDisplayed("affected."))
				throw new Exception("Could not add to AddToFavorites Page..!!!!");
			Log.message("6. AddToFavorites done.");

			//Step-9: Verify if Document moved to 'Add to Favorites' view
			if (!homePage.navigateToGotoViews("Favorites"))
				throw new Exception("Unable to navigate to 'Favorites'");
			Log.message("7.  Successfully navigated to 'Favorites' view");
			if (!homePage.isDataInListView(driver, inputDataValues.get("SearchTerm"),"Name")) {
				throw new Exception("Could not found document in the 'Favorites'");
			}

			if (!homePage.listView.clickItem(searchedFile)) {//Checks if object selected in the list
				throw new Exception("Object (" + searchedFile + ") is not selected in the list.");
			}
			Log.message("8. Document selected from 'Favorites'");

			//Step-10: Select the Operations menu options
			homePage.menuBar.selectFromOperationsMenu("RemoveFromFavorites");
			Log.message("9. Selected 'Remove from Favorites' from the Operations menu options");

			//Step-11: 'Remove from Favorites' the Document using Settings->Operations Menu
			if (!homePage.removeFromFavorites(inputDataValues.get("SearchTerm"),"Name"))
				Log.fail("Test Failed...Could not remove object from favorites using Settings->Operations Menu",driver);
			else
				Log.pass("Test Passed..!!!Successfully Removed object from favorites using Settings->Operations Menu");
		}catch(Exception e)
		{
			Log.exception(e, driver);
		}//End of catch		
		finally{
			//Close the browser
			driver.quit(); 
		}//End of finally
	}//End of Test

	/**
	 * TestCase ID: TC_074 
	 * <br>Description: Rename the Document using Operations Menu</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_074",description="Rename the Document using Operations Menu")
	public void TC_074(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{
		//Instantiate WebDriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" Rename the Document using Operations Menu");
		Log.addTestRunMachineInfo(driver,currentDriver,context);//get the execution environment Details and prints on Log report

		try{
			//Fetch data from Excel sheet
			ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1: Launch M-Files Application
			Log.message("1. Launch the application.");
			driver.get(webSite);

			//Step-2: Fetch Login details from Testdata sheet
			Log.message("2. Fetch Data from Testdata sheet.");
			LoginPage loginPage=new LoginPage(driver);
			if (inputDataValues.get("UserName").isEmpty()||inputDataValues.get("Password").isEmpty())
				throw new SkipException("Unable to Fetch Data from Testdata sheet.");

			//Step-3: Login to Application
			Log.message("3. Login to the application using valid credentials.");
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			//Step-4: Verify the Page title
			Log.message("4. Verify the Page title.");
			if (!driver.getTitle().contains("Web")) {
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			}
			Log.message("---Successfully LoggedIn to Application.");

			Utils.fluentWait(driver);
			HomePage homePage=new HomePage(driver);

			//Step-5: Search an object and 'Rename' it using operations menu
			Log.message("5. Search an object and 'Rename' it using operations menu.");
			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));
			if (!homePage.isSearchResults(driver,inputDataValues.get("Type"))&&searchedFile.isEmpty()) {
				throw new Exception("Could not find Document in Search list.");
			}
			//Step-6: Select Object from list view
			Log.message("6. Select Object from list view");

			if (!homePage.listView.clickItem(searchedFile)) {//Checks if object selected in the list
				throw new Exception("Object (" + searchedFile + ") is not selected in the list.");
			}

			//Step-7: 'Rename' the Document using Operations Menu option.
			Log.message("7. 'Rename' the Document using Operations Menu option.");
			if (!homePage.renameObjectsFromListView("tMenu",inputDataValues.get("SearchTerm"),"operationsMenu")) 
				Log.fail("Test Failed...!!!Rename Failed using 'Operations' menu options..!!!!", driver);
			else 
				Log.pass("Test Passed..!!!Rename Success using 'Operations' menu options..!!!!");
		}catch(Exception e)
		{
			Log.exception(e, driver);
		}//End of catch
		finally{
			//Close the browser
			driver.quit(); 
		}//End of finally
	}//End of Test

	/**
	 * TestCase ID: TC_075
	 * <br>Description: Delete the Document using Operations menu</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_075",description="Delete the Document using Operations menu")
	public void TC_075(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{
		//Instantiate WebDriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo("Delete the Document using Operations menu");
		Log.addTestRunMachineInfo(driver,currentDriver,context);//get the execution environment details and print Log reports
		ConcurrentHashMap <String, String> inputDataValues =null;

		try{
			//Fetch test data from Excel sheet
			inputDataValues = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1: Launch M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Step-2: Fetch Login details from Testdata sheet
			LoginPage loginPage=new LoginPage(driver);

			//Step-3: Login to Application
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			HomePage homePage=new HomePage(driver);
			//Step-4: Verify the Page title
			if (!driver.getTitle().contains("Web")&& !homePage.isLoggedIn(inputDataValues.get("UserName"))) {
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			}

			Utils.fluentWait(driver);
			//Step-5: Search an object to see its relationships
			homePage.searchPanel.showAdvancedSearchOptions(driver);
			if (!homePage.searchPanel.selectSearchOptionsUsingObject(driver,MFilesObjectList.EMPLOYEE, inputDataValues.get("Type")).contains("Employees")) 
				throw new Exception("Could not select Basic Search options");

			//Step-7: Select the Search options
			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));
			if (!homePage.isDataInListView(driver, searchedFile, "Name")&&searchedFile.isEmpty()) 
				throw new Exception("Specified document doesnot exists in the Search results.");
			Log.message("4. Specified document exists in the Search results.");

			if (!homePage.isSearchResults(driver,inputDataValues.get("Type"))&&searchedFile.isEmpty()) {
				throw new Exception("Could not Find document in search list.");
			}
			Log.message("6. Found '"+inputDataValues.get("SearchTerm")+"' object/file in Search Results.");

			if (!homePage.listView.clickItem(searchedFile)) {//Checks if object selected in the list
				throw new Exception("Object (" + searchedFile + ") is not selected in the list.");
			}

			//Step-8: 'Delete' the Document using Operations Menu option
			Log.message("8. 'Delete' the Document using Operations Menu option.");	         
			if (!homePage.deleteObjectFromListView("operationsMenu")) 
				Log.fail("Test Failed...!!!'Delete' operation Failed from Operations Menu..!!!!", driver);
			else 
				Log.pass("Test Passed..!!!'Delete' operation Success from Operations Menu..!!!!");
		}
		catch(Exception e)
		{
			Log.exception(e, driver);
		}//End of catch
		finally{
			//Close the browser
			driver.quit(); 
		}//End of finally
	}//End of Test

	/**
	 * TC_076. : Convert MFD document to Single File Document(SFD) using Settings->Operations
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Smoke"}, 
			description = "Convert MFD document to Single File Document(SFD) using Settings->Operations")
	public void TC_076(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = WebDriverUtils.getDriver();

		try {

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			if (!ListView.isSFDByItemName(driver, dataPool.get("ObjectName"))) //Checks if this is SFD
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not Single file document.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if it is in checked in state
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-3 : Select Convert to MFD from operations menu
			//---------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value); //Clicks Convert SFD to MFD option from operations menu
			Utils.fluentWait(driver);
			String mfdName = dataPool.get("ObjectName").split("\\.")[0];

			if (ListView.isSFDByItemName(driver, mfdName))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not converted to MFD document.");

			Log.message("3. Object (" + dataPool.get("ObjectName") + ") converted to MFD document.");

			//Step-4 : Select MFD and Convert to SFD from opeartions menu
			//---------------------------------------------------------
			if (!homePage.listView.clickItem(mfdName)) //Selects the Object in the list
				throw new Exception("Object (" + mfdName + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.ConvertToSFD_O.Value); //Clicks Convert SFD to MFD option from operations menu
			Utils.fluentWait(driver);

			Log.message("4. Convert MFD to SFD option is selected from operations menu for Single file MFD (" + mfdName + ").");

			//Verification : To Verify if MFD is converted to SFD
			//---------------------------------------------------			
			if (ListView.isSFDByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Checked out Single file MFD is converted to SFD through operations menu.");
			else
				Log.fail("Test case Failed. Checked out Single file MFD is not converted to SFD through operations menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_076

	/**
	 * TC_077 : Convert Single document to Multi File Document(MFD) using Settings->Operations
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Smoke"}, 
			description = "Convert Single document to Multi File Document(MFD) using Settings->Operations.")
	public void TC_077(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = WebDriverUtils.getDriver();

		try {

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (!ListView.isSFDByItemName(driver, dataPool.get("ObjectName"))) //Checks if this is SFD
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not Single file document.");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if it is in checked in state
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-3 : Select Convert to MFD from operations menu
			//---------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value); //Clicks Convert SFD to MFD option from operations menu
			Utils.fluentWait(driver);

			String mfdName = dataPool.get("ObjectName").split("\\.")[0];

			Log.message("3. Convert SFD to MFD option is selected from context menu for SFD (" + dataPool.get("ObjectName") + ").");

			//Verification : To Verify if SFD object is converted to MFD object
			//-------------------------------------------------------------------			
			if (!ListView.isSFDByItemName(driver, mfdName))
				Log.pass("Test case Passed. SFD is converted to MFD (" + mfdName + ") through operations menu.");
			else
				Log.fail("Test case Failed. SFD is not converted to MFD through operations menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_077

	/**
	 * TestCase ID: TC_78
	 * <br>Description : Verify 'Workflow' dialog displaye using Operations->Properties</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_078",description="Verify 'Workflow' dialog displaye using Operations->Workflow")
	public void TC_078(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{
		//Instantiate Webdriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" Verify 'Workflow' dialog displayed using Operations->Workflow.");
		Log.addTestRunMachineInfo(driver,currentDriver,context);
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);
		try{

			//Step-1:  And now use this to visit M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Step-3: Login to Application
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			Utils.fluentWait(driver);
			HomePage homePage=new HomePage(driver);

			//Step-4: Verify the Page title
			if (!driver.getTitle().contains("Web")&& !homePage.isLoggedIn(inputDataValues.get("UserName"))) 
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			Log.message("3. Verified the Page title and Successfully LoggedIn to Application.");

			//Step-5: Select the Search options
			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));

			if (!homePage.isSearchResults(driver,inputDataValues.get("Type"))&&searchedFile.isEmpty()) 
				throw new Exception("Could not Found the Document in Search list.");
			Log.message("4. Document  found in Search results.");


			//Step-6: Select the object from listview
			if (!homePage.listView.clickItem(searchedFile)) 
				throw new Exception("Object (" + searchedFile + ") is not selected in the list.");

			Log.message("5. Selected the object from listview");

			//Step-7: Select the 'Operations->Workflow' option
			homePage.menuBar.selectFromOperationsMenu("Workflow");
			Log.message("6. Selected the 'Operations->Workflow' option");
			Thread.sleep(1000);

			//Step-8: Verify if 'Workflow' displayed for the object
			Log.message("8. Verify if 'Workflow' displayed for the object");
			if (!MFilesDialog.exists(driver))
				Log.fail("Test Failed...Unable to display 'Workflow' dialog for the selected Object using Operations Menu.", driver);
			else 
				Log.pass("Test Passed...Successfully displayed 'Workflow' dialog for the selected Object using Operations Menu.");
		}
		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally{

			//Close the browser
			driver.quit(); 
		}
	}

	/**
	 * TestCase ID: TC_079
	 * <br>Description: 'LogOut' from application using menuBar option</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_079",description="'LogOut' from application using menuBar option.")
	public void TC_079(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{
		//Instantiate Webdriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" 'LogOut' from application using menuBar option");
		Log.addTestRunMachineInfo(driver,currentDriver,context);//get the execution environment details and print log report
		//Fetch test data from Excel sheet
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);

		try{

			//Step-1: Launch M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			LoginPage loginPage=new LoginPage(driver);

			//Step-2: Login to Application
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			HomePage homePage=new HomePage(driver);
			//Step:4 Verify the Page title
			if (!driver.getTitle().contains("Web")&& !homePage.isLoggedIn(inputDataValues.get("UserName"))) {
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			}
			Log.message("3. WebAccess application Page title displayed as'"+driver.getTitle()+ "'.");
			Utils.fluentWait(driver);


			//Step-4: verify is user loggedIn to application
			if (!homePage.isLoggedIn(inputDataValues.get("UserName")))
				throw new Exception("Could not login to application due to some problems");
			Log.message("Successfully Logged in to application with '"+inputDataValues.get("UserName")+"'.");

			//Step-5: Select 'LogOut' option from Menubar
			loginPage=homePage.menuBar.logOutFromMenuBar();
			Log.message("5. 'LogOut' option selected from Menubar");
			Utils.isLogOutPromptDisplayed(driver);

			//Step-6:Verify if User navigated back to Login form after LoggedOut
			Log.message("6. Verify if User navigated back to Login form after LoggedOut");
			if (!loginPage.isLoginPageDisplayed()) 
				Log.fail("Failed to navigate to LoginPage.", driver);
			else 
				Log.pass("Successfully LoggedOut of Application and user navigated back to 'Login Form'.");

		}catch(Exception e){
			Log.exception(e, driver);
		}//End of catch
		finally{
			//Close the browser
			driver.quit(); 
		}//End of finally
	}//ENd of Test

	/**
	 * TestCase ID: TC_080
	 * <br>Description: 'Change Password' from application using menuBar option</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_080",description="'Change Password' from application using menuBar option")
	public void TC_080(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{
		String newPassword=null;
		HomePage homePage=null;
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);
		//Instantiate Webdriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" 'Change Password' from application using menuBar option");
		Log.addTestRunMachineInfo(driver,currentDriver,context);//get the execution environment details and print log report
		try{

			//Step-1: Launch M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Step-2: Login to Application
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),mfilesAdminTestPassword,inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+mfilesAdminTestPassword);

			homePage=new HomePage(driver);
			//Step:4 Verify the Page title
			if (!driver.getTitle().contains("Web")&& !homePage.isLoggedIn(inputDataValues.get("UserName"))) {
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			}
			Log.message("3. WebAccess application Page title displayed as'"+driver.getTitle()+ "'.");
			Utils.fluentWait(driver);


			//Step-4: verify is user loggedIn to application
			if (!homePage.isLoggedIn(inputDataValues.get("UserName")))
				throw new Exception("Could not login to application due to some problems");
			Log.message("4. Successfully Logged in to application with '"+inputDataValues.get("UserName")+"'.");


			String randomValue=Utils.getCurrentDateTime();
			newPassword= inputDataValues.get("Password")+randomValue.substring(0,0)+"G"+"!#$"+123;

			//Change password
			homePage.menuBar.changePasswordFromMenuBar(inputDataValues.get("Password"),newPassword);
			Log.message("5. Password changed as '"+newPassword+"'");
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),newPassword, documentVault);

			//Step-6:Verify if User navigated back to Login form after LoggedOut
			Log.message("6. Verify if User relogged in to application using new password");
			if (!homePage.isLoggedIn(inputDataValues.get("UserName")))
				Log.fail("Test Failed..!!!Unable to relogin to application using newly changed password.", driver);
			else 
				Log.pass("Test Passed..!!!Successfully reloggedin to application using newly changed password..");

		}catch(Exception e){
			Log.exception(e, driver);
		}//End of catch
		finally{
			//Reset Password
			homePage.menuBar.changePasswordFromMenuBar(newPassword,mfilesAdminTestPassword);
			Thread.sleep(500);
			driver.manage().deleteAllCookies();
			//Close the browser
			driver.quit(); 
		}//End of finally
	}//ENd of Test

	/**
	 * Testcase ID :TC_082
	 * <br>Description : Group Objects by Object Type</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_082",description="Group Objects by Object type")
	public void TC_082(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{
		//Instantiate Webdriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		HomePage homePage = null;
		Log.testCaseInfo(" Group Objects by Object type");
		Log.addTestRunMachineInfo(driver,currentDriver,context);//get the execution environment details and print log report
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);
		try{

			//Step-1: Launch M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Step-3: Login to Application
			LoginPage loginPage=new LoginPage(driver);

			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			//Step:4 Verify the Page title
			if (!driver.getTitle().contains("Web")) {
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			}
			Log.message("3. Verified the Page title after Successfully LoggedIn to Application.");

			Utils.fluentWait(driver);
			homePage=new HomePage(driver);

			//Step-5: verify is user loggedIn to application
			if (!homePage.isLoggedIn(inputDataValues.get("UserName")))
				throw new Exception("Could not login to application due to some problems");
			Log.message("4. Successfully Logged in to application.");

			//Fetch Expected Object type headers to be displayed
			String expectedGrouping = inputDataValues.get("ExpectedGroups");
			String[] expectedGroups = expectedGrouping.split(",");
			int counter = 0;
			for(int i=0; i<expectedGroups.length;i++)
			{
				Log.message("Expected Object Type to be displayed as Group headers :"+expectedGroups[i]);
			}

			//Step-6: Perform a Search for all objects
			homePage.clickSearchBtn(driver);
			Log.message("5. Performed a Search for all objects.");

			//Step-7: Select 'Group Objects By Object Type' option if default view is not Grouped by Object Type
			if (homePage.listView.groupHeaderCount() == 1 && homePage.listView.groupExists("Objects (")){
				//Select 'Group Object By Object Type' options from settings menu
				homePage.menuBar.selectDisplayModeSettingOptions("GroupObjectsByObjectType");
				Log.message("7. 'Group by Object Type' option is selected when default view is not Grouped by Object Type.");
			}

			//Step-8: To Verify if the Objects are grouped by object type in list view
			Log.message(" To Verify if the Objects are grouped by object type in list view");

			if (homePage.listView.groupHeaderCount() == expectedGroups.length) {
				for(counter = 0; counter < expectedGroups.length; counter++){
					if (!homePage.listView.groupExists(expectedGroups[counter]))
						break;
				}
			}

			if (counter == expectedGroups.length)
				Log.pass("Test Case Passed. The 'Group Objects by Object Type option works as expected.");
			else
				Log.fail("Test Case Failed. All the expected Object types were not found as group headers", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally{
			//Close the browser
			driver.quit();
		}
	}

	/**
	 * Testcase ID :TC_083
	 * <br>Description: Display Mode->Group Views and Folders</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_083",description="Display Mode->Group Views and Folders")
	public void TC_083(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{
		//Instantiate Webdriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" Display Mode->Group Views and Folders");
		Log.addTestRunMachineInfo(driver,currentDriver,context);//get the execution environment details and print log report
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);
		HomePage homePage=null;
		try{

			//Step-1: Launch M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Step-3: Login to Application
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			//Step:4 Verify the Page title
			if (!driver.getTitle().contains("Web")) 
				throw new Exception("Unable to Login. Please verify Login Credentials.");

			Log.message("3. Verified the Page title after Successfully LoggedIn to Application.");
			Utils.fluentWait(driver);
			homePage=new HomePage(driver);

			//Step-5: verify is user loggedIn to application
			if (!homePage.isLoggedIn(inputDataValues.get("UserName")))
				throw new Exception("Could not login to application due to some problems");
			Log.message("4. Successfully Logged in to application.");

			//Get the Expected view list from test data sheet
			String expectedGrouping = inputDataValues.get("ExpectedGroups");
			String[] expectedGroups = expectedGrouping.split(",");
			int counter = 0;

			for(int i=0; i<expectedGroups.length;i++) {
				Log.message("Expected View and Folders to be displayed as Group headers :"+expectedGroups[i]);
			}
			if(!homePage.isListViewDisplayed()) {
				homePage.menuBar.selectDisplayModeSettingOptions("ViewList");
				Utils.fluentWait(driver);
			}

			//Step-6: Select the Display Mode->Group Views and Folders option
			Log.message("6. Select the Display Mode->Group Views and Folders option.");
			if (homePage.listView.groupHeaderCount() == 0)
			{
				homePage.menuBar.selectDisplayModeSettingOptions("Details");
				homePage.menuBar.selectDisplayModeSettingOptions("GroupViewsAndFolders");
				Log.message("---Selected 'GroupViewsAndFolders' option of Display mode.");
			}
			//Step-7: Verify if the views and folders are grouped
			Log.message("7. Verify if the views and folders are grouped");
			int groupHeadersDisplayed=homePage.listView.groupHeaderCount();

			if (groupHeadersDisplayed == expectedGroups.length) {
				for(counter = 0; counter < expectedGroups.length; counter++){
					if (!homePage.listView.groupExists(expectedGroups[counter]))
						break;
				}
			}

			if (counter == expectedGroups.length)
				Log.pass("Test Passed. 'Group Views and Folders' option displayed expected.");
			else
				Log.fail("Test Failed. 'Views and Folders' are not grouped as expected.", driver);
		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally{
			//Close the browser
			driver.quit();
		}
	}

	/**
	 * Testcase ID :TC_084
	 * <br>Description: Display Mode->Details</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_084",description="Display Mode->Details")
	public void TC_084(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{
		//Instantiate Webdriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" Display Mode->Details");
		Log.addTestRunMachineInfo(driver,currentDriver,context);//get the execution environment details and print log report
		//Fetch test data from Excel sheet
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);
		HomePage homePage=null;
		try{

			//Step-1: Launch M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application with URL: "+webSite);

			//Step-2: Login to Application
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			Utils.fluentWait(driver);
			homePage=new HomePage(driver);
			//Step-5: verify is user loggedIn to application
			if (!homePage.isLoggedIn(inputDataValues.get("UserName")))
				throw new Exception("Could not login to application due to some problems");

			Log.message("4. Verified the Page title after Successfully LoggedIn to Application.");

			//Select Thumbnail view, as views and folders displayed in 'Details' mode by default
			if(homePage.isListViewDisplayed()) {
				homePage.menuBar.selectDisplayModeSettingOptions("ViewThumbnails");
				Thread.sleep(800);
			}

			homePage.menuBar.selectDisplayModeSettingOptions("ViewList");
			Log.message("6. Selected the Display Mode->Details option.");
			//Step-7: Verify if the views and folders are displayed as List
			if(!homePage.isListViewDisplayed())
				Log.fail("Test Failed. 'List View' is not displayed when clicking Settings->Display Mode->Details Option", driver);
			else
				Log.pass("Test Passed. 'List View' is displayed successfully when clicking Settings->Display Mode->Details Option");
		}
		catch(Exception e) {
			Log.exception(e, driver);
		}
		finally {
			//Close the browser
			driver.quit();
		}
	}

	/**
	 * Testcase ID :TC_085
	 * <br>Description: Display Mode->Thumbnail</br>
	 * @param dataValues
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_085",description="Display Mode->Thumbnail")
	public void TC_085(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{
		//Instantiate Webdriver
		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" Display Mode->Thumbnail");
		Log.addTestRunMachineInfo(driver,currentDriver,context);//get the execution environment details and print log report
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);
		HomePage homePage=null;
		try{

			//Step-1: Launch M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Step-2: Login to Application
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			//Step:3 Verify the Page title
			if (!driver.getTitle().contains("Web")) {
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			}
			Utils.fluentWait(driver);
			homePage=new HomePage(driver);

			//Step-4: verify is user loggedIn to application
			if (!homePage.isLoggedIn(inputDataValues.get("UserName")))
				throw new Exception("Could not login to application due to some problems");
			Log.message("3. Verified WebPage Page title after Successfully LoggedIn.");

			//Select Details view, as views and folders displayed in 'Thumbnail' mode by default
			if(homePage.listView.isThumbnailsView()) {
				homePage.menuBar.selectDisplayModeSettingOptions("ViewList");
				homePage.menuBar.selectDisplayModeSettingOptions("ViewThumbnails");
				Log.message("4. Verified if Details view displayed to change to Thumbnail View");
			}
			else {
				//Step-6: Select the Display Mode->Thumbnail option
				homePage.menuBar.selectDisplayModeSettingOptions("ViewThumbnails");
				Log.message("5. Selected the Display Mode->Thumbnail option.");
			}
			//Step-7: Verify if the views and folders are displayed as Thumbnail
			if(!homePage.listView.isThumbnailsView())
				Log.fail("Test Failed. 'Thumbnail' view is not displayed when clicking Settings->Display Mode->Thumbnail Option", driver);
			else
				Log.pass("Test Passed. 'Thumbnail' view is displayed successfully when clicking Settings->Display Mode->Thumbnail Option");

		}
		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally{
			homePage=new HomePage(driver);
			if(homePage.listView.isThumbnailsView()) {
				homePage.menuBar.selectDisplayModeSettingOptions("ViewList");
			}
			homePage.menuBar.logOutFromMenuBar();

			//Close the browser
			driver.quit();
		}
	}

	//	/**
	//	 * TestCase ID: TC_090
	//	 * <br>Description: Search for a file containing text</br>
	//	 * @param dataValues
	//	 * @param context
	//	 * @throws Exception
	//	 */
	//	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_090",description="Search for a file containing text")
	//	public void TC_090(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	//	{
	//		//Instantiate Webdriver
	//		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
	//	    Log.testCaseInfo(" Search for a file containing text.");
	//	    Log.addTestRunMachineInfo(driver,currentDriver,context);//get execution environment details and print on Log report
	//	    ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);
	//	    
	//		try{
	//
	//			// Step-1: And now use this to visit M-Files Application
	//			driver.get(webSite);
	//			Log.message("1. Launched the application.");
	//
	//			//Step-2: Fetch Login details from Testdata sheet
	//			LoginPage loginPage=new LoginPage(driver);
	//			
	//			//Step-3: Login to Application
	//			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
	//			Utils.waitForPageLoad(driver);
	//	        
	//			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));
	//			
	//			//Step-4: Verify the Page title
	//	        if (!driver.getTitle().contains("Web")) {
	//	        	throw new Exception("Unable to Login. Please verify Login Credentials.");
	//	        }
	//	        Utils.fluentWait(driver);
	//	        HomePage homePage=new HomePage(driver);
	//	        Log.message("3. Verified the Page title after Successfully LoggedIn to Application.");
	//	        
	//	        //Step-5: Verify if TaskPane displayed
	//	        if(!homePage.isTaskPaneDisplayed())
	//	        	throw new Exception("Task pane not displayed");
	//	        Log.message("4. Task Pane displayed in HomePage.");
	//	        
	//	        //6. Verify the display of SearchBar in HomePage
	//	        if (!homePage.isSearchbarPresent(driver)) {
	//	        	throw new Exception("Search Panel not displayed.");
	//	        }
	//	        Log.message("5. Search Panel displayed in HomePage.");
	//	        
	//	        //Step-7: Search the object with its name
	//	        Log.message("6. Search the object with its name");
	//	        String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));
	//	        
	//			
	//			homePage.listView.clickItemByIndex(0);
	//	        
	//			MetadataCard metadataCard=new MetadataCard(driver);
	//			if(!metadataCard.clickPreviewPaneTabs("Preview"))
	//				throw new Exception("Unable to select Preview tab on right pane");
	//			
	//			if (!metadataCard.checkPDFContent(searchedFile)) 
	//	        	Log.fail("Test Failed..!!!Search term not present in the Object/File.", driver);
	//			else 
	//				Log.pass("Test Passed..!!!Search term displayed in the Object/File");
	//		}//End of try
	//		catch(Exception e) {
	//			Log.exception(e, driver);
	//		} //ENd of catch
	//		
	//		finally{
	//			//Close the browser
	//	        Log.endTaseCase();driver.quit();
	//		}//End of finally
	//	}
	//	
	/**
	 * 
	 * @param dataValues
	 * @param currentDriver
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_091",description="Verify 'Workflow' states for the Workflow selected from context menu of Document")
	public void TC_091(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{

		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo(" Verify 'Workflow' states for the Workflow selected from context menu of Document");
		Log.addTestRunMachineInfo(driver,currentDriver,context);
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);
		final List <String> elements = new ArrayList <String>();
		HomePage homePage=null;
		MFilesDialog mFilesDialog=null;
		try{

			// Step-1: And now use this to visit M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Login to Application
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			//Verify the Page title
			homePage=new HomePage(driver);
			if (!driver.getTitle().contains("Web")|| !homePage.isLoggedIn(inputDataValues.get("UserName"))) 
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			Log.message("3. Verified the Page title after Successfully LoggedIn to Application.");

			Utils.fluentWait(driver);

			//Step-7: Select the Search options
			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));

			if (!homePage.isSearchResults(driver,inputDataValues.get("Type")) && searchedFile.isEmpty()) {
				throw new Exception("Specified document doesnot exists in the Search results.");
			}
			Log.message("4. Specified document exists in the Search results.");

			if (!homePage.openContextMenuDialog(driver))
				throw new Exception("Unable to Open context Menu options");

			homePage.selectContextMenuItemFromListView("Workflow");
			Log.message("5. Selected 'Workflow' option of the Document using ContextMenu option.");
			Thread.sleep(1000);
			mFilesDialog=new MFilesDialog(driver);

			if (!mFilesDialog.isWorkflowDialogDisplayed())
				throw new Exception("Workflow dialog is not displayed.");

			mFilesDialog.setWorkflow(inputDataValues.get("Workflow").toUpperCase().trim(),currentDriver);
			mFilesDialog.setWorkflowState(inputDataValues.get("WorkflowState").toUpperCase().trim(),currentDriver);

			Log.message("6. Workflow '"+inputDataValues.get("Workflow")+"' assigned for the document/object");

			int snooze=0;
			while (MFilesDialog.exists(driver) && snooze < 10) {
				mFilesDialog.clickOkButton();
				Thread.sleep(500);
				if(!MFilesDialog.exists(driver))
					break;
				snooze++;
			}
			Utils.waitForPageLoad(driver);
			//Fetching the Workflow States defined for the Workflow
			String[] expectedStates=inputDataValues.get("WorkflowStates").split(">");
			for(String state:expectedStates) {
				elements.add(state);
			}
			Thread.sleep(800);
			//Verify if Workflow states are displayed correctly for the selected Workflow      
			if(homePage.taskPanel.getWorkflowStates().equals(elements))
				Log.pass("Workflow states are displayed correctly in the Workflow dialog for the selected Workflow");
			else
				Log.fail("Workflow states are not displayed correctly in the Workflow dialog for the selected Workflow",driver);
		}
		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally{
			homePage=new HomePage(driver);
			homePage.openContextMenuDialog(driver);
			homePage.selectContextMenuItemFromListView("Workflow");
			mFilesDialog=new MFilesDialog(driver);
			mFilesDialog.setWorkflow("",currentDriver);
			mFilesDialog.clickOkButton();
			Thread.sleep(2000);
			//Close the browser
			driver.quit(); 
		}
	}//End of Test

	/**
	 * 
	 * @param dataValues
	 * @param currentDriver
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_092",description="Verify 'Workflow' states for the Workflow selected are displayed on Task Panel")
	public void TC_092(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	{

		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
		Log.testCaseInfo("Verify 'Workflow' states for the Workflow selected are displayed on Task Panel");
		Log.addTestRunMachineInfo(driver,currentDriver,context);
		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);
		final List <String> elements = new ArrayList <String>();
		MFilesDialog mFilesDialog=null;
		HomePage homePage=null;
		try{

			// Step-1: And now use this to visit M-Files Application
			driver.get(webSite);
			Log.message("1. Launched the application.");

			//Fetch Login details from Testdata sheet
			LoginPage loginPage=new LoginPage(driver);

			//Login to Application
			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
			Utils.waitForPageLoad(driver);
			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));

			//Verify the Page title
			homePage=new HomePage(driver);
			if (!driver.getTitle().contains("Web")|| !homePage.isLoggedIn(inputDataValues.get("UserName"))) 
				throw new Exception("Unable to Login. Please verify Login Credentials.");
			Log.message("3. Verified the Page title Successfully LoggedIn to Application.");

			Utils.fluentWait(driver);

			//Step-7: Select the Search options
			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));
			if (!homePage.isSearchResults(driver,inputDataValues.get("Type")) && searchedFile.isEmpty()) {
				throw new Exception("Specified document doesnot exists in the Search results.");
			}
			Log.message("4. Specified document exists in the Search results.");

			if (!homePage.openContextMenuDialog(driver))
				throw new Exception("Unable to Open context Menu options");

			homePage.selectContextMenuItemFromListView("Workflow");
			Log.message("5. Selected 'Workflow' option of the Document using ContextMenu option.");
			Thread.sleep(500);
			mFilesDialog=new MFilesDialog(driver);

			if (!mFilesDialog.isWorkflowDialogDisplayed())
				throw new Exception("Workflow dialog is not displayed.");

			mFilesDialog.setWorkflow(inputDataValues.get("Workflow").toUpperCase().trim(),currentDriver);
			mFilesDialog.setWorkflowState(inputDataValues.get("WorkflowState").toUpperCase().trim(),currentDriver);

			Log.message("6. Workflow '"+inputDataValues.get("Workflow")+"' assigned for the document/object");

			int snooze=0;
			while (MFilesDialog.exists(driver) && snooze < 5) {
				mFilesDialog.clickOkButton();
				Thread.sleep(500);
				if(!MFilesDialog.exists(driver))
					break;
				snooze++;
			}
			Utils.waitForPageLoad(driver);
			//Fetching the Workflow States defined for the Workflow
			String[] expectedStates=inputDataValues.get("WorkflowStates").split(">");
			for(String state:expectedStates) {
				elements.add(state);
			}
			Thread.sleep(800);
			//Verify if Workflow states are displayed correctly on TaskPanel for the selected Workflow      
			if(homePage.taskPanel.getWorkflowStates().equals(elements))
				Log.pass("Workflow states are displayed correctly on TaskPanel for the selected Workflow");
			else
				Log.fail("Workflow states are not displayed correctly on TaskPanel for the selected Workflow",driver);
		}
		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally{
			driver.quit(); 
		}
	}//End of Test

	//	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",testName = "TC_093",description="Verify 'Workflow' Basic functionality")
	//	public void TC_093(HashMap<String,String> dataValues,String currentDriver,ITestContext context) throws Exception
	//	{
	//		
	//		final WebDriver driver=WebDriverUtils.getDriver(currentDriver,2);
	//	    Log.testCaseInfo("Verify 'Workflow' Basic functionality ");
	//	    Log.addTestRunMachineInfo(driver,currentDriver,context);
	//		ConcurrentHashMap <String, String> inputDataValues = new ConcurrentHashMap <String, String>(dataValues);
	//		try{
	//			
	//			// Step-1: And now use this to visit M-Files Application
	//			driver.get(webSite);
	//			Log.message("1. Launched the application.");
	//
	//			//Fetch Login details from Testdata sheet
	//			LoginPage loginPage=new LoginPage(driver);
	//			if (inputDataValues.get("UserName").isEmpty()||inputDataValues.get("Password").isEmpty())
	//				throw new SkipException("Unable to Fetch Data from Testdata sheet.");
	//			
	//			//Login to Application
	//			loginPage.loginToWebApplication(inputDataValues.get("UserName"),inputDataValues.get("Password"),inputDataValues.get("DocumentVault"));
	//			Utils.waitForPageLoad(driver);
	//			Log.message("2. Logged in with user :"+inputDataValues.get("UserName")+" and Password :"+inputDataValues.get("Password"));
	//			//Verify the Page title
	//	        
	//	        if (!driver.getTitle().contains("Web")) 
	//	        	throw new Exception("Unable to Login. Please verify Login Credentials.");
	//	        Log.message("3. Verified the Page title aSuccessfully LoggedIn to Application.");
	//	      
	//	        Utils.fluentWait(driver);
	//	        HomePage homePage=new HomePage(driver);
	//	        //Step-7: Select the Search options
	//			String searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));
	//			if (!homePage.isSearchResults(driver,inputDataValues.get("Type")) && searchedFile.isEmpty()) {
	//				throw new Exception("Specified document doesnot exists in the Search results.");
	//			}
	//	        Log.message("4. Specified document exists in the Search results.");
	//	        
	//	        if (!homePage.openContextMenuDialog(driver))
	//	        	throw new Exception("Unable to Open context Menu options");
	//	        
	//	        homePage.selectContextMenuItemFromListView("Workflow");
	//	        Log.message("5. Selected 'Workflow' option of the Document using ContextMenu option.");
	//	        
	//	        MFilesDialog mFilesDialog=new MFilesDialog(driver);
	//	        
	//	        if (!mFilesDialog.isWorkflowDialogDisplayed())
	//				throw new Exception("Workflow dialog is not displayed.");
	//	        
	//	        if(!mFilesDialog.setWorkflowType(inputDataValues.get("Workflow").toUpperCase().trim()).equalsIgnoreCase(inputDataValues.get("Workflow").toUpperCase().trim()))
	//	        	throw new Exception("Unable to select Workflow");
	//	        Log.message("6. Workflow '"+inputDataValues.get("Workflow")+"' assigned for the document/object");
	//
	//	        if(!mFilesDialog.setWorkflowState(inputDataValues.get("WorkflowState1").toUpperCase().trim()).equalsIgnoreCase(inputDataValues.get("WorkflowState1").toUpperCase().trim()))
	//	        	throw new Exception("Unable to select Workflow State as '"+inputDataValues.get("WorkflowState1")+"'");
	//	        Log.message("Workflow state selected as '"+inputDataValues.get("WorkflowState1")+"' in the Workflow dialog");
	//	        
	//	        mFilesDialog.ClickOkButton();
	//	        
	//	        
	//	        if(!homePage.taskPanel.isObjectActionsDisplayedOnTaskPane(inputDataValues.get("WorkflowState2")))
	//	        	throw new Exception("Workflow State doesnot exists on homePage.taskPanel.");
	//	        
	//	        homePage.taskPanel.selectObjectActionsFromTaskPane(inputDataValues.get("WorkflowState2"));
	//	        
	//	        homePage.taskPanel.selectObjectActionsFromTaskPane("LogOut");
	//	        homePage=loginPage.loginToWebApplication(userName, password,inputDataValues.get("DocumentVault"));
	//	        
	//	        Utils.fluentWait(driver);
	//	        //Step-7: Select the Search options
	//			searchedFile=homePage.searchAFileName(inputDataValues.get("SearchTerm"));
	//			if (!homePage.isSearchResults(driver,inputDataValues.get("Type")) && searchedFile.isEmpty()) {
	//				throw new Exception("Specified document doesnot exists in the Search results.");
	//			}
	//	        Log.message("4. Specified document exists in the Search results.");
	//	        
	//	        
	//	        homePage.listView.clickItem(inputDataValues.get("SearchTerm"));
	//	        
	//	        homePage.taskPanel.selectObjectActionsFromTaskPane(inputDataValues.get("WorkflowState3"));
	//	        
	//	        //Verify if Workflow states are displayed correctly on TaskPanel for the selected Workflow      
	//	        if(!homePage.taskPanel.isObjectActionsDisplayedOnTaskPane(inputDataValues.get("WorkflowState1")) && (!homePage.taskPanel.isObjectActionsDisplayedOnTaskPane(inputDataValues.get("WorkflowState2")))
	//	        		&&(!homePage.taskPanel.isObjectActionsDisplayedOnTaskPane(inputDataValues.get("WorkflowState3"))))
	//	        	Log.pass("Workflow states are assigned correctly for the selected object");
	//	        else
	//	        	Log.fail("Workflow states are not assigned correctly for the selected object",driver);
	//		}
	//		catch(Exception e)
	//		{
	//			Log.exception(e, driver);
	//		}
	//		
	//		finally{
	//			//Close the browser
	//	        Log.endTaseCase();driver.quit(); 
	//		}
	//	}//End of Test
}

package MFClient.Tests;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.Utils;
import genericLibrary.WebDriverUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.openqa.selenium.WebDriver;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.Listeners;
import org.testng.xml.XmlTest;

import MFClient.Pages.ConfigurationPage;
import MFClient.Pages.HomePage;
import MFClient.Pages.LoginPage;
import MFClient.Pages.SharedLinkPage;
import MFClient.Wrappers.Caption;
import MFClient.Wrappers.ListView;
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class SmokeTestCases {


	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String configURL = null;
	public static String userName = null;
	public static String password = null;
	public static String testVault = null;
	public static String productVersion = null;
	public static String userFullName = null;
	public static String driverType = null;
	public static String className = null;
	public String methodName = null;

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
	 * endTestCase: Ends the test case after the method
	 * @throws Exception
	 */
	@AfterMethod (alwaysRun = true)
	public void endTestCase() throws Exception{

		try {

			Log.endTestCase();//Ends the test case

		}//End try
		catch(Exception e){
			throw e;
		}//End Catch
	}//End endTestCase

	/**
	 * getMethodName : Gets the name of current executing method
	 */
	@BeforeMethod (alwaysRun=true)
	public void getMethodName(Method method) throws Exception {

		try {

			methodName = method.getName();

		} //End try

		catch(Exception e) {
			if (e.getClass().toString().contains("NullPointerException")) 
				throw new Exception ("Test data sheet does not exists.");
			else
				throw e;
		} //End catch		
	} //End getMethodName

	/**
	 * TC001 : Login to WebAccess with different credentials
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Smoke"}, 
			description = "Login to WebAccess with different credentials")
	public void TC_001(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1 : Launch login page
			//-------------------------
			driver.get(loginURL);

			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN"))
				throw new Exception("Login page is not launched.");

			Log.message("1.Login page URL (" + loginURL + ") is launched.");

			//Step-2 : Login with user credentials
			//------------------------------------
			LoginPage loginPage=new LoginPage(driver);
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), testVault);

			Log.message("2. Login credentials are entered in the login page.",driver);

			//Verification : Verify if login is successful with the user
			if (!driver.getCurrentUrl().toUpperCase().contains("DEFAULT.ASPX")) //Verifies if default page is loaded
				throw new Exception("Test case Failed. Default page of an application is not loaded");

			String userName = dataPool.get("UserName");

			if (dataPool.get("UserType").toUpperCase().contains("WINDOWS")) 
				userName = dataPool.get("Domain") + "\\" + dataPool.get("UserName");

			if (homePage.getLoggedinUserName().contains(userName))
				Log.pass("Test case Passed. M-Files web access is logged in successfully with provided user credentials.");
			else
				Log.fail("Test case Failed. M-Files web access is logged in not successful with provided user credentials.",driver);

		}//End of try

		catch(Exception e) {
			Log.exception(e, driver);
		}//End of Catch

		finally {
			Utility.quitDriver(driver);
		}//End of Finally

	}//End of TC_001

	/**
	 * TC002 : Create New 'Document' From TaskPane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Smoke"}, 
			description = "Create New 'Document' From TaskPane")
	public void TC_002(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();	

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Select New Document from menubar
			//--------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Document.Value); //Clicks New Document from the taskpanel
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard = metadatacard.setTemplate(dataPool.get("TemplateOrExtension"));

			Log.message("1. New Document is selected from taskpanel.");

			//Step-2 : Enter the metadatacard details
			//---------------------------------------
			metadatacard = new MetadataCard(driver);
			metadatacard.setInfo(dataPool.get("Properties"));
			ConcurrentHashMap <String, String> prevInfo = metadatacard.getInfo(); //Gets the property information			

			Log.message("2. New Document details are entered in metadatacard.",driver);

			//Step-3 : Save the metadatacard
			//------------------------------
			metadatacard.setCheckInImmediately(true);//Check in the object
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			String docName = "";

			if (dataPool.get("TemplateOrExtension").equalsIgnoreCase("Multi-File Document"))
				docName = prevInfo.get("Name or title");
			else
				docName = prevInfo.get("Name or title") + "." + prevInfo.get("Extension");

			Log.message("3. New Document metadatacard is saved.");

			//Step-4 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), docName);

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5 : Open the Properties dialog of the new document through context menu
			//-----------------------------------------------------------------------------
			if (!homePage.listView.rightClickItem(docName)) //Right clicks the project
				throw new Exception("Newly created document (" + docName + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("5. Properties dialog of the new document (" + docName + ") is opened through context menu.");

			//Step-6 : Obtain metadatacard information and compare the properties before and after saving metadatacard
			//---------------------------------------------------------------------------------------------------------
			ConcurrentHashMap <String, String> currInfo = metadatacard.getInfo(); //Gets the property information
			String diff = Utility.compareObjects(currInfo, prevInfo, "Extension,WorkflowState");

			Log.message("6. Current Document metadatacard details are obtained and compared with the entered values.");

			//Verification : Verifies if Document is created
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
	 * TC003 : Create New 'Assignment' From TaskPane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Smoke"}, 
			description = "Create New 'Assignment' From TaskPane")
	public void TC_003(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();	

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Select New Assignment from menubar
			//--------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Document from the taskpanel

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			if (metadatacard.isTemplateDialogExists()) {
				metadatacard = metadatacard.setTemplate(dataPool.get("TemplateOrExtension"));
				metadatacard = new MetadataCard(driver);
			}

			Log.message("1. New Assignment is selected from taskpanel.", driver);

			//Step-2 : Enter the metadatacard details
			//---------------------------------------
			metadatacard.setInfo(dataPool.get("Properties"));
			ConcurrentHashMap <String, String> prevInfo = metadatacard.getInfo(); //Gets the property information			

			Log.message("2. New Assignment details are entered in metadatacard.", driver);

			//Step-3 : Save the metadatacard
			//------------------------------
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			String assignName = "";
			assignName = prevInfo.get("Name or title");

			Log.message("3. New Assignment metadatacard is saved.", driver);

			//Step-4 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), assignName);

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5 : Open the Properties dialog of the new Assignment through context menu
			//-----------------------------------------------------------------------------
			if (!homePage.listView.rightClickItem(assignName)) //Right clicks the project
				throw new Exception("Newly created document (" + assignName + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("5. Properties dialog of the new Assignment (" + assignName + ") is opened through context menu.", driver);

			//Step-6 : Obtain metadatacard information and compare the properties before and after saving metadatacard
			//--------------------------------------------------------------------------------------------------------
			ConcurrentHashMap <String, String> currInfo = metadatacard.getInfo(); //Gets the property information
			String diff = Utility.compareObjects(currInfo, prevInfo, "Monitored by");

			Log.message("6. Current assignment metadatacard details are obtained and compared with the entered values.");

			//Verification : Verifies if Assignment is created
			//----------------------------------------------------------
			if (diff.equalsIgnoreCase(""))
				Log.pass("Test case Passed. New Assignment (" + assignName + ") is created successfully from taskpanel.");
			else
				Log.fail("Test case Failed. New Assignment (" + assignName + ") is creation is not successful. Additonal Information : " + diff, driver);

			metadatacard.closeMetadataCard(); //Closes Metadatacard


		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			Utility.quitDriver(driver);
		} //End finally

	} //End TC_003

	/**
	 * TC004 : Create New 'Customer' From TaskPane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Smoke"}, 
			description = "Create New 'Customer' From TaskPane")
	public void TC_004(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Select New Customer from menubar
			//--------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Customer.Value); //Clicks New Document from the taskpanel
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			if (metadatacard.isTemplateDialogExists()) {
				metadatacard = metadatacard.setTemplate(dataPool.get("TemplateOrExtension"));
				metadatacard = new MetadataCard(driver);
			}

			Log.message("1. New Customer is selected from taskpanel.",driver);

			//Step-2 : Enter the metadatacard details
			//---------------------------------------
			metadatacard.setInfo(dataPool.get("Properties"));
			ConcurrentHashMap <String, String> prevInfo = metadatacard.getInfo(); //Gets the property information			

			Log.message("2. New Customer details are entered in metadatacard.", driver);

			//Step-3 : Save the metadatacard
			//------------------------------
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			String custName = "";
			custName = prevInfo.get("Customer name");

			Log.message("3. New Customer metadatacard is saved.", driver);

			//Step-4 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), custName);

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5 : Open the Properties dialog of the new customer through context menu
			//-----------------------------------------------------------------------------
			if (!homePage.listView.rightClickItem(custName)) //Right clicks the project
				throw new Exception("Newly created document (" + custName + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("5. Properties dialog of the new customer (" + custName + ") is opened through context menu.", driver);

			//Step-6 : Obtain metadatacard information and compare the properties before and after saving metadatacard
			//--------------------------------------------------------------------------------------------------------
			ConcurrentHashMap <String, String> currInfo = metadatacard.getInfo(); //Gets the property information
			String diff = Utility.compareObjects(currInfo, prevInfo, "Extension");

			Log.message("6. Current customer metadatacard details are obtained and compared with the entered values.");

			//Verification : Verifies Customer is created successfully
			//----------------------------------------------------------
			if (diff.equalsIgnoreCase(""))
				Log.pass("Test case Passed. New Customer (" + custName + ") is created successfully from taskpanel.");
			else
				Log.fail("Test case Failed. New Customer (" + custName + ") is creation is not successful. Additonal Information : " + diff,  driver);

			metadatacard.closeMetadataCard(); //Closes Metadatacard

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			Utility.quitDriver(driver);
		} //End finally

	} //End TC_004

	/**
	 * TC005 : Create New 'Project' From TaskPane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Smoke"}, 
			description = "Create New 'Project' From TaskPane")
	public void TC_005(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Select New Project from menubar
			//--------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Project.Value); //Clicks New Document from the taskpanel

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			if (metadatacard.isTemplateDialogExists()) {
				metadatacard = metadatacard.setTemplate(dataPool.get("TemplateOrExtension"));
				metadatacard = new MetadataCard(driver);
			}

			Log.message("1. New Project is selected from taskpanel.", driver);

			//Step-2 : Enter the metadatacard details
			//---------------------------------------
			metadatacard.setInfo(dataPool.get("Properties"));
			ConcurrentHashMap <String, String> prevInfo = metadatacard.getInfo(); //Gets the property information			

			Log.message("2. New Project details are entered in metadatacard.", driver);

			//Step-3 : Save the metadatacard
			//------------------------------
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			String projName = "";
			projName = prevInfo.get("Name or title");

			Log.message("3. New Customer metadatacard is saved.",driver);

			//Step-4 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), projName);

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5 : Open the Properties dialog of the new Project through context menu
			//-----------------------------------------------------------------------------
			if (!homePage.listView.rightClickItem(projName)) //Right clicks the project
				throw new Exception("Newly created document (" + projName + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("5. Properties dialog of the new Project (" + projName + ") is opened through context menu.", driver);

			//Step-6 : Obtain metadatacard information and compare the properties before and after saving metadatacard
			//--------------------------------------------------------------------------------------------------------
			ConcurrentHashMap <String, String> currInfo = metadatacard.getInfo(); //Gets the property information
			String diff = Utility.compareObjects(currInfo, prevInfo, "Extension");

			Log.message("6. Current Project metadatacard details are obtained and compared with the entered values.");

			//Verification : Verifies Project is created successfully
			//----------------------------------------------------------
			if (diff.equalsIgnoreCase(""))
				Log.pass("Test case Passed. New Project (" + projName + ") is created successfully from taskpanel.");
			else
				Log.fail("Test case Failed. New Project (" + projName + ") is creation is not successful. Additonal Information : " + diff, driver);

			metadatacard.closeMetadataCard(); //Closes Metadatacard

		}
		catch (Exception e) {
			Log.exception(e,  driver);
		} //End catch

		finally {

			Utility.quitDriver(driver);
		} //End finally

	} //End TC_005

	/**
	 * TC006 : Search an object with file name
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Smoke"}, 
			description = "Search an object with file name")
	public void TC_006(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view with search key word as " + dataPool.get("ObjectName") + ".");

			//Verification : Verify if object is availble in list view
			//---------------------------------------------------------
			if (homePage.listView.isItemExists(dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object search using its name is successful.");
			else
				Log.fail("Test case Failed. Object search using its name is not successful", driver);


		} //End try
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_006

	/**
	 * TC007 : Create New 'Document' From Menu Bar
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Smoke"}, 
			description = "Create New 'Document' From Menu bar")
	public void TC_007(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Select New Document from menubar
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Document.Value); //Clicks New Document from the taskpanel
			//
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard = metadatacard.setTemplate(dataPool.get("TemplateOrExtension"));

			Log.message("1. New Document is selected from taskpanel.", driver);

			//Step-2 : Enter the metadatacard details
			//---------------------------------------
			metadatacard = new MetadataCard(driver);
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
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), docName);

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5 : Open the Properties dialog of the new document through context menu
			//-----------------------------------------------------------------------------
			if (!homePage.listView.rightClickItem(docName)) //Right clicks the project
				throw new Exception("Newly created document (" + docName + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("5. Properties dialog of the new document (" + docName + ") is opened through context menu.", driver);

			//Step-6 : Obtain metadatacard information and compare the properties before and after saving metadatacard
			//---------------------------------------------------------------------------------------------------------
			ConcurrentHashMap <String, String> currInfo = metadatacard.getInfo(); //Gets the property information
			String diff = Utility.compareObjects(currInfo, prevInfo, "Extension,WorkflowState");

			Log.message("6. Current Document metadatacard details are obtained and compared with the entered values.",driver);

			//Verification : Verifies if Document is created
			//----------------------------------------------------------
			if (diff.equalsIgnoreCase(""))
				Log.pass("Test case Passed. New Document (" + docName + ") is created successfully from menu bar.");
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
	 * TC008 : Create New 'Assignment' From Menubar
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Smoke"}, 
			description = "Create New 'Assignment' From Menu bar")
	public void TC_008(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Select New Assignment from menubar
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Document from the menu bar
			//	
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			if (metadatacard.isTemplateDialogExists()) {
				metadatacard = metadatacard.setTemplate(dataPool.get("TemplateOrExtension"));
				metadatacard = new MetadataCard(driver);
			}

			Log.message("1. New Assignment is selected from taskpanel.", driver);

			//Step-2 : Enter the metadatacard details
			//---------------------------------------
			metadatacard.setInfo(dataPool.get("Properties"));
			ConcurrentHashMap <String, String> prevInfo = metadatacard.getInfo(); //Gets the property information			

			Log.message("2. New Assignment details are entered in metadatacard.", driver);

			//Step-3 : Save the metadatacard
			//------------------------------
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			String assignName = "";
			assignName = prevInfo.get("Name or title");

			Log.message("3. New Assignment metadatacard is saved.", driver);

			//Step-4 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), assignName);

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5 : Open the Properties dialog of the new Assignment through context menu
			//-----------------------------------------------------------------------------
			if (!homePage.listView.rightClickItem(assignName)) //Right clicks the project
				throw new Exception("Newly created document (" + assignName + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("5. Properties dialog of the new Assignment (" + assignName + ") is opened through context menu.", driver);

			//Step-6 : Obtain metadatacard information and compare the properties before and after saving metadatacard
			//--------------------------------------------------------------------------------------------------------
			ConcurrentHashMap <String, String> currInfo = metadatacard.getInfo(); //Gets the property information
			String diff = Utility.compareObjects(currInfo, prevInfo, "Monitored by");

			Log.message("6. Current assignment metadatacard details are obtained and compared with the entered values.");

			//Verification : Verifies if Assignment is created
			//----------------------------------------------------------
			if (diff.equalsIgnoreCase(""))
				Log.pass("Test case Passed. New Assignment (" + assignName + ") is created successfully from menu bar.");
			else
				Log.fail("Test case Failed. New Assignment (" + assignName + ") is creation is not successful. Additonal Information : " + diff, driver);

			metadatacard.closeMetadataCard(); //Closes Metadatacard

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			Utility.quitDriver(driver);
		} //End finally

	} //End TC_008

	/**
	 * TC009 : Create New 'Customer' From Menu bar
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Smoke"}, 
			description = "Create New 'Customer' From Menu bar")
	public void TC_009(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Select New Customer from menubar
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Customer.Value); //Clicks New Document from the Menu bar
			//	
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			if (metadatacard.isTemplateDialogExists()) {
				metadatacard = metadatacard.setTemplate(dataPool.get("TemplateOrExtension"));
				metadatacard = new MetadataCard(driver);
			}

			Log.message("1. New Customer is selected from taskpanel.", driver);

			//Step-2 : Enter the metadatacard details
			//---------------------------------------
			metadatacard.setInfo(dataPool.get("Properties"));
			ConcurrentHashMap <String, String> prevInfo = metadatacard.getInfo(); //Gets the property information			

			Log.message("2. New Customer details are entered in metadatacard.", driver);

			//Step-3 : Save the metadatacard
			//------------------------------
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			String custName = "";
			custName = prevInfo.get("Customer name");

			Log.message("3. New Customer metadatacard is saved.", driver);

			//Step-4 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), custName);

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5 : Open the Properties dialog of the new customer through context menu
			//-----------------------------------------------------------------------------
			if (!homePage.listView.rightClickItem(custName)) //Right clicks the project
				throw new Exception("Newly created document (" + custName + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("5. Properties dialog of the new customer (" + custName + ") is opened through context menu.", driver);

			//Step-6 : Obtain metadatacard information and compare the properties before and after saving metadatacard
			//--------------------------------------------------------------------------------------------------------
			ConcurrentHashMap <String, String> currInfo = metadatacard.getInfo(); //Gets the property information
			String diff = Utility.compareObjects(currInfo, prevInfo, "Extension");

			Log.message("6. Current customer metadatacard details are obtained and compared with the entered values.");

			//Verification : Verifies Customer is created successfully
			//----------------------------------------------------------
			if (diff.equalsIgnoreCase(""))
				Log.pass("Test case Passed. New Customer (" + custName + ") is created successfully from Menu bar.");
			else
				Log.fail("Test case Failed. New Customer (" + custName + ") is creation is not successful. Additonal Information : " + diff, driver);

			metadatacard.closeMetadataCard(); //Closes Metadatacard


		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			Utility.quitDriver(driver);
		} //End finally

	} //End TC_009

	/**
	 * TC010 : Create New 'Project' From Menu bar
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Smoke"}, 
			description = "Create New 'Project' From Menu bar")
	public void TC_010(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Select New Project from menubar
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Project.Value); //Clicks New Document from the Menu bar

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			if (metadatacard.isTemplateDialogExists()) {
				metadatacard = metadatacard.setTemplate(dataPool.get("TemplateOrExtension"));
				metadatacard = new MetadataCard(driver);

			}

			Log.message("1. New Project is selected from taskpanel.", driver);

			//Step-2 : Enter the metadatacard details
			//---------------------------------------
			metadatacard.setInfo(dataPool.get("Properties"));
			ConcurrentHashMap <String, String> prevInfo = metadatacard.getInfo(); //Gets the property information			

			Log.message("2. New Project details are entered in metadatacard.", driver);

			//Step-3 : Save the metadatacard
			//------------------------------
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			String projName = "";
			projName = prevInfo.get("Name or title");

			Log.message("3. New Customer metadatacard is saved.", driver);

			//Step-4 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), projName);

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5 : Open the Properties dialog of the new Project through context menu
			//-----------------------------------------------------------------------------
			if (!homePage.listView.rightClickItem(projName)) //Right clicks the project
				throw new Exception("Newly created document (" + projName + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("5. Properties dialog of the new Project (" + projName + ") is opened through context menu.", driver);

			//Step-6 : Obtain metadatacard information and compare the properties before and after saving metadatacard
			//--------------------------------------------------------------------------------------------------------
			ConcurrentHashMap <String, String> currInfo = metadatacard.getInfo(); //Gets the property information
			String diff = Utility.compareObjects(currInfo, prevInfo, "Extension");

			Log.message("6. Current Project metadatacard details are obtained and compared with the entered values.");

			//Verification : Verifies Project is created successfully
			//----------------------------------------------------------
			if (diff.equalsIgnoreCase(""))
				Log.pass("Test case Passed. New Project (" + projName + ") is created successfully from Menu bar.");
			else
				Log.fail("Test case Failed. New Project (" + projName + ") is creation is not successful. Additonal Information : " + diff, driver);

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
	 * TC011 : Create New 'Document Collection' From Menubar
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Smoke"}, 
			description = "Create New 'Document Collection' From Menu bar")
	public void TC_011(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Select New Document Collection from menubar
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.DocumentCollection.Value); //Clicks New Document from the menu bar

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			if (metadatacard.isTemplateDialogExists()) {
				metadatacard = metadatacard.setTemplate(dataPool.get("TemplateOrExtension"));
				metadatacard = new MetadataCard(driver);
			}

			Log.message("1. New Document Collection is selected from taskpanel.", driver);

			//Step-2 : Enter the metadatacard details
			//---------------------------------------
			metadatacard.setInfo(dataPool.get("Properties"));
			ConcurrentHashMap <String, String> prevInfo = metadatacard.getInfo(); //Gets the property information			

			Log.message("2. New Document Collection details are entered in metadatacard.", driver);

			//Step-3 : Save the metadatacard
			//------------------------------
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			String docCollectionName = "";
			docCollectionName = prevInfo.get("Name or title");

			Log.message("3. New Document Collection metadatacard is saved.", driver);

			//Step-4 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), docCollectionName);

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5 : Open the Properties dialog of the new Document Collection through context menu
			//-----------------------------------------------------------------------------
			if (!homePage.listView.rightClickItem(docCollectionName)) //Right clicks the project
				throw new Exception("Newly created document (" + docCollectionName + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("5. Properties dialog of the new Document Collection (" + docCollectionName + ") is opened through context menu.", driver);

			//Step-6 : Obtain metadatacard information and compare the properties before and after saving metadatacard
			//--------------------------------------------------------------------------------------------------------
			ConcurrentHashMap <String, String> currInfo = metadatacard.getInfo(); //Gets the property information
			String diff = Utility.compareObjects(currInfo, prevInfo, "Extension");

			Log.message("6. Current Document Collection metadatacard details are obtained and compared with the entered values.");

			//Verification : Verifies if Document Collection is created
			//----------------------------------------------------------
			if (diff.equalsIgnoreCase(""))
				Log.pass("Test case Passed. New Document Collection (" + docCollectionName + ") is created successfully from menu bar.");
			else
				Log.fail("Test case Failed. New Document Collection (" + docCollectionName + ") is creation is not successful. Additonal Information : " + diff, driver);

			metadatacard.closeMetadataCard(); //Closes Metadatacard

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			Utility.quitDriver(driver);
		} //End finally

	} //End TC_0011

	/**
	 * TC012 : Create New 'Employee' From Menubar
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Smoke"}, 
			description = "Create New 'Employee' From Menu bar")
	public void TC_012(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Select New Employee from menubar
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Employee.Value); //Clicks New Employee from the menu bar

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			if (metadatacard.isTemplateDialogExists()) {
				metadatacard = metadatacard.setTemplate(dataPool.get("TemplateOrExtension"));
				metadatacard = new MetadataCard(driver);
			}

			Log.message("1. New Employee is selected from taskpanel.", driver);

			//Step-2 : Enter the metadatacard details
			//---------------------------------------
			metadatacard.setInfo(dataPool.get("Properties"));
			ConcurrentHashMap <String, String> prevInfo = metadatacard.getInfo(); //Gets the property information			

			Log.message("2. New Employee details are entered in metadatacard.", driver);

			//Step-3 : Save the metadatacard
			//------------------------------
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			String empName = "";
			empName = prevInfo.get("Employee name");

			Log.message("3. New Employee metadatacard is saved.", driver);

			//Step-4 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), empName);

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5 : Open the Properties dialog of the new Employee through context menu
			//-----------------------------------------------------------------------------
			if (!homePage.listView.rightClickItem(empName)) //Right clicks the project
				throw new Exception("Newly created employee (" + empName + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("5. Properties dialog of the new Employee (" + empName + ") is opened through context menu.", driver);

			//Step-6 : Obtain metadatacard information and compare the properties before and after saving metadatacard
			//--------------------------------------------------------------------------------------------------------
			ConcurrentHashMap <String, String> currInfo = metadatacard.getInfo(); //Gets the property information
			String diff = Utility.compareObjects(currInfo, prevInfo, "Extension");

			Log.message("6. Current Employee metadatacard details are obtained and compared with the entered values.");

			//Verification : Verifies if Employee is created
			//----------------------------------------------------------
			if (diff.equalsIgnoreCase(""))
				Log.pass("Test case Passed. New Employee (" + empName + ") is created successfully from menu bar.");
			else
				Log.fail("Test case Failed. New Employee (" + empName + ") is creation is not successful. Additonal Information : " + diff, driver);

			metadatacard.closeMetadataCard(); //Closes Metadatacard

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			Utility.quitDriver(driver);
		} //End finally

	} //End TC_0012

	/**
	 * TC013 : Create New 'Contact Person' From Menubar
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Smoke","Bug"}, 
			description = "Create New 'Contact Person' From Menu bar")
	public void TC_013(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Select New Contact Person from menubar
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.ContactPerson.Value); //Clicks New Contact Person from the menu bar

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			if (metadatacard.isTemplateDialogExists()) {
				metadatacard = metadatacard.setTemplate(dataPool.get("TemplateOrExtension"));
				metadatacard = new MetadataCard(driver);
			}

			Log.message("1. New Contact Person is selected from taskpanel.", driver);

			//Step-2 : Enter the metadatacard details
			//---------------------------------------
			metadatacard.setInfo(dataPool.get("Properties"));
			ConcurrentHashMap <String, String> prevInfo = metadatacard.getInfo(); //Gets the property information			

			Log.message("2. New Contact Person details are entered in metadatacard.", driver);

			//Step-3 : Save the metadatacard
			//------------------------------
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			String cpName = "";
			cpName = prevInfo.get("First name") + " " + prevInfo.get("Last name");

			Log.message("3. New Contact Person metadatacard is saved.", driver);

			//Step-4 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), cpName);

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5 : Open the Properties dialog of the new Contact Person through context menu
			//-----------------------------------------------------------------------------
			if (!homePage.listView.rightClickItem(cpName)) //Right clicks the project
				throw new Exception("Newly created Contact Person (" + cpName + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("5. Properties dialog of the new Contact Person (" + cpName + ") is opened through context menu.", driver);

			//Step-6 : Obtain metadatacard information and compare the properties before and after saving metadatacard
			//--------------------------------------------------------------------------------------------------------
			ConcurrentHashMap <String, String> currInfo = metadatacard.getInfo(); //Gets the property information
			String diff = Utility.compareObjects(currInfo, prevInfo, "Full name");

			Log.message("6. Current Contact Person metadatacard details are obtained and compared with the entered values.");

			//Verification : Verifies if Contact Person is created
			//----------------------------------------------------------
			if (diff.equalsIgnoreCase(""))
				Log.pass("Test case Passed. New Contact Person (" + cpName + ") is created successfully from menu bar.");
			else
				Log.fail("Test case Failed. New Contact Person (" + cpName + ") is creation is not successful. Additonal Information : " + diff, driver);

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
	 * TC_014 : Create New 'Report' From Menubar
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Smoke"}, 
			description = "Create New 'Report' From Menu bar")
	public void TC_014(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Select New Report from menubar
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Report.Value); //Clicks New Report from the menu bar

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			if (metadatacard.isTemplateDialogExists()) {
				metadatacard = metadatacard.setTemplate(dataPool.get("TemplateOrExtension"));
				metadatacard = new MetadataCard(driver);
			}

			Log.message("1. New Report is selected from taskpanel.", driver);

			//Step-2 : Enter the metadatacard details
			//---------------------------------------
			metadatacard.setInfo(dataPool.get("Properties"));
			ConcurrentHashMap <String, String> prevInfo = metadatacard.getInfo(); //Gets the property information			

			Log.message("2. New Report details are entered in metadatacard.", driver);

			//Step-3 : Save the metadatacard
			//------------------------------
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			String reportName = "";
			reportName = prevInfo.get("Name or title");

			Log.message("3. New Report metadatacard is saved.", driver);

			//Step-4 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), reportName);

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5 : Open the Properties dialog of the new Report through context menu
			//-----------------------------------------------------------------------------
			if (!homePage.listView.rightClickItem(reportName)) //Right clicks the project
				throw new Exception("Newly created Report (" + reportName + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("5. Properties dialog of the new Report (" + reportName + ") is opened through context menu.", driver);

			//Step-6 : Obtain metadatacard information and compare the properties before and after saving metadatacard
			//--------------------------------------------------------------------------------------------------------
			ConcurrentHashMap <String, String> currInfo = metadatacard.getInfo(); //Gets the property information
			String diff = Utility.compareObjects(currInfo, prevInfo, "Extension");

			Log.message("6. Current Report metadatacard details are obtained and compared with the entered values.");

			//Verification : Verifies if Report is created
			//----------------------------------------------------------
			if (diff.equalsIgnoreCase(""))
				Log.pass("Test case Passed. New Report (" + reportName + ") is created successfully from menu bar.");
			else
				Log.fail("Test case Failed. New Report (" + reportName + ") is creation is not successful. Additonal Information : " + diff, driver);

			metadatacard.closeMetadataCard(); //Closes Metadatacard

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_014

	/**
	 * TC_015 : Search an object with file name
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Smoke"}, 
			description = "Search an object with file name")
	public void TC_015(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view with search key word as " + dataPool.get("ObjectName") + ".");

			//Verification : Verify if object is availble in list view
			//---------------------------------------------------------
			if (homePage.listView.isItemExists(dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object search using its name is successful.");
			else
				Log.fail("Test case Failed. Object search using its name is not successful", driver);


		} //End try
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			Utility.quitDriver(driver);
		} //End finally

	} //End TC_015

	/**
	 * TC_016 : Rename the object using context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint24"}, 
			description = "Rename the object using context menu.")
	public void TC_016(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the item from the list
			//--------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			String extension = "";

			if(dataPool.get("ObjectName").contains("."))
				extension = "." + dataPool.get("ObjectName").split("\\.")[1];

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-3 : Select Rename from context menu
			//----------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Rename.Value);
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isRenameDialogOpened())
				throw new Exception("Rename dialog is not opened.");

			Log.message("3. Rename is selected from context menu.");

			//Step-4 : Enter new name in the rename dialog
			//----------------------------------------------
			String newName = "TC_016" + Utility.getCurrentDateTime(); 
			mfilesDialog.rename(newName, true);

			Log.message("4. Rename dialog is opened and new name (" + newName +") is entered.");

			//Verification : To Verify Rename is sucessful in context menu
			//---------------------------------------------------------------------------
			newName += extension;
			if (homePage.listView.isItemExists(newName))
				Log.pass("Test case Passed. Object is renamed successfully through context menu.");
			else
				Log.fail("Test case Failed. Object is not renamed through context menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_016

	/**
	 * TC_017 : Delete the object using context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint24", "Smoke"}, 
			description = "Delete the object using context menu.")
	public void TC_017(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the item from the list
			//--------------------------------------
			int prevCt = homePage.listView.itemCount(); //Gets the items from list

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-3 Press Delete from context menu
			//--------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Delete.Value);
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.confirmDelete()) //Clicks Yes in the confirmation dialog to perform delete operation
				throw new SkipException("Confirmation dialog has not appeared after pressing DEL key");

			Log.message("3. Delete is selected from context menu and confirmed the delete action.");

			//Verification : To Verify short key for Delete is available in context menu
			//---------------------------------------------------------------------------
			int currCt = homePage.listView.itemCount(); //Gets the number of object in the list after deletion

			if (prevCt != currCt + 1) { //Verifies if number of objects in the list has reduced by 1
				Log.fail("No of objects has not reduced by 1 after deleting document by DEL key.", driver);
				return;
			}

			//Verifies that Delete short cut key is available in operations menu
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object (" + dataPool.get("ObjectName") + ") is deleted by selecting Delete from Context menu.");
			else
				Log.fail("Test case Failed. Object (" + dataPool.get("ObjectName") + ") exists after by performing Delete operation from Context menu.", driver);


		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_017

	/**
	 * TC_018 : 'CheckOut' an object using context Menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint24"}, 
			description = "'CheckOut' an object using context Menu.")
	public void TC_018(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

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
			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))//Checks if object is checked out
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is already in checked out state.");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-3 : Select Check out from context menu
			//------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.CheckOut.Value);


			Log.message("3. " + Caption.MenuItems.CheckOut.Value + ") is selected from context menu.");

			//Verification : To Verify object is checked in by pressing short cut key
			//-----------------------------------------------------------------------
			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is checked out from context menu.");
			else
				Log.fail("Test case Failed. Object is not checked out from context menu.",driver);


		}
		catch (Exception e) {

			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_018

	/**
	 * TC_019 : 'CheckIn' an object using context Menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint24"}, 
			description = "'CheckIn' an object using context Menu")
	public void TC_019(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the checked out object
			//--------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {//Checks if object is checked out
				homePage.listView.clickItem(dataPool.get("ObjectName"));
				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);				


				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new SkipException("Object(" + dataPool.get("ObjectName") + ") is not checked out state.");
			}

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			Log.message("2. Checked out object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-3 : Select Check in from context menu
			//------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.CheckIn.Value);


			Log.message("3. " + Caption.MenuItems.CheckIn.Value + ") is selected from context menu.");

			//Verification : To Verify object is checked in by pressing short cut key
			//-----------------------------------------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is checked in through context menu.");
			else
				Log.fail("Test case Failed. Object is not checked in through context menu.", driver);


		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			Utility.quitDriver(driver);
		} //End finally

	} //End TC_019

	/**
	 * TC_020 : 'CheckIn with comments' an object using context Menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint24"}, 
			description = "'CheckIn with comments' an object using context Menu")
	public void TC_020(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 
		HomePage homePage = null;

		try {

			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the checked out object
			//--------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {//Checks if object is checked out
				homePage.listView.clickItem(dataPool.get("ObjectName"));
				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);				


				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new SkipException("Object(" + dataPool.get("ObjectName") + ") is not checked out state.");
			}

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			Log.message("2. Checked out object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-3 : Select Check in from context menu
			//------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.CheckInWithComments.Value);

			Log.message("3. " + Caption.MenuItems.CheckInWithComments.Value + ") is selected from context menu.");

			//Step-4 : Enter comments in the dialog
			//-------------------------------------
			String comments = "TC_020" + Utility.getCurrentDateTime(); 

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.setComment(comments); //Enters Comments
			mfilesDialog.clickOkButton(); //Clicks Ok button

			Log.message("4. Comments (" + comments + ") are entered in the MFiles dialog.");

			//Verification : To Verify object is checked in by pressing short cut key
			//-----------------------------------------------------------------------
			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Test case Failed. Object is not checked in through context menu on selecting Check-in with comments.");

			String addlInfo = ListView.getCommentsByItemName(driver, dataPool.get("ObjectName"));

			if (addlInfo.equalsIgnoreCase(comments))
				Log.pass("Test case Passed. Object is checked in with comments through context menu.");
			else {
				addlInfo = "Actual Comments : " + addlInfo + "; Expected Comments : " + comments;
				Log.fail("Test case Failed. Object is not checked in with comments through context menu. Additional Information : " + addlInfo , driver);
			}

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			if(driver != null && homePage != null){

				try{
					if(homePage.listView.isColumnExists(Caption.Column.Coln_Comment.Value))
						homePage.listView.removeColumn(Caption.Column.Coln_Comment.Value);
				}
				catch(Exception e0){
					Log.exception(e0, driver);
				}
			}

			Utility.quitDriver(driver);
		} //End finally

	} //End TC_020

	/**
	 * TC_021:  Hide 'Go To' options on TaskPane using Configuration Page
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"smoke", "configuration"}, 
			description = " Hide 'Go To' options on TaskPane using Configuration Page.")
	public void TC_021(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 
		String menuItem = null;
		String prevCommand = null;


		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Click the Task area link 
			//----------------------------
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false); //Launched driver and logged in
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_TaskArea.Value);


			Log.message("1. Navigated to task specific settings");

			//2. Hide goto item shortcut in task area
			//-------------------------------------
			menuItem = dataPool.get("MenuItem");


			prevCommand = configurationPage.configurationPanel.getVaultCommands(menuItem);
			configurationPage.configurationPanel.setVaultCommands(menuItem,"Hide");

			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			Log.message("2. Vault option is hided from task area and settings are saved in configuration page");

			//3. Logging out from configuration page and lauch the default webpage
			//-------------------------------------------------------------------
			configurationPage.logOut();


			LoginPage loginPage = new LoginPage(driver);
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);


			Log.message("3. Logged out from configuration page and Default webpage is launched");

			//Verification: To verify if Vault option is not displayed in the task pane
			//------------------------------------------------------------------------


			if(!homePage.taskPanel.isItemExists(dataPool.get("MenuItem")))
				Log.pass("Test case Passed. The Task Panel item " + dataPool.get("MenuItem") + " is not displayed.");
			else
				Log.fail("Test Case Failed. The Task Panel item " + dataPool.get("MenuItem") + " still displayed", driver);

		}//End Try
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (driver != null){
				try
				{
					ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false); //Launched driver and logged in
					configurationPage.configurationPanel.resetVaultCommands(menuItem, prevCommand, testVault);
				}
				catch(Exception e0){
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		}//End Finally

	}//End TC_021

	/**
	 * TC_022:  Show 'Go To' options on TaskPane using Configuration Page
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"smoke", "configuration"}, 
			description = " Show 'Go To' options on TaskPane using Configuration Page.")
	public void TC_022(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 
		String menuItem = null;
		String prevCommand = null;


		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false); //Launched driver and logged in

			//1. Click the Task area link 
			//----------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_TaskArea.Value);

			Log.message("1. Navigated to task specific settings");

			//2. Hide goto item shortcut in task area
			//-------------------------------------
			menuItem = dataPool.get("MenuItem");

			prevCommand = configurationPage.configurationPanel.getVaultCommands(menuItem);
			configurationPage.configurationPanel.setVaultCommands(menuItem, "Show");

			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");

			Log.message("2. Vault option is shown from task area and settings are saved in configuration page");

			//3. Logging out from configuration page and lauch the default webpage
			//-------------------------------------------------------------------
			configurationPage.logOut();

			LoginPage loginPage = new LoginPage(driver);
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);

			Log.message("3. Logged out from configuration page and Default webpage is launched");

			//Verification: To verify if Vault option is not displayed in the task pane
			//------------------------------------------------------------------------
			if(homePage.taskPanel.isItemExists(dataPool.get("MenuItem")))
				Log.pass("Test case Passed. The Task Panel item " + dataPool.get("MenuItem") + " is displayed.");
			else
				Log.fail("Test Case Failed. The Task Panel item " + dataPool.get("MenuItem") + " is not displayed", driver);



		}//End Try
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (driver != null){
				try
				{
					ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false); //Launched driver and logged in
					configurationPage.configurationPanel.resetVaultCommands(menuItem, prevCommand, testVault);
				}
				catch(Exception e0){
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);

		}//End Finally

	}//End TC_022

	/**
	 * TC_023 :  Insert a New Column by Right clicking on Column Header
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint15", "Columns", "Smoke"}, 
			description = "Insert a New Column by Right clicking on Column Header")
	public void TC_023(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2: Insert new column by right clicking at column header
			//-----------------------------------------------------
			Boolean tcPass = homePage.listView.insertColumn(dataPool.get("ColumnName"));

			Log.message("2. Right clicked at column header and column (" + dataPool.get("ColumnName") + ") is selected.");

			//Verification: To verify if all the inserted columns are displayed as expected
			//------------------------------------------------------------------------------
			if(tcPass)
				Log.pass("Test Case Passed. The Column " + dataPool.get("ColumnName") + " is inserted successfully.");
			else
				Log.fail("Test Case Failed. The Column " + dataPool.get("ColumnName") + " is not inserted.",driver);


		} //End try

		catch(Exception e) {

			Log.exception(e, driver);
		} //End catch

		finally {

			Utility.quitDriver(driver);
		}

	} //End TC_023

	/**
	 * TC_025 : Navigate to 'CheckedOUt to Me' from TaskPane  
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint15", "Columns", "Smoke"}, 
			description = "Navigate to 'CheckedOUt to Me' from TaskPane")
	public void TC_025(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2: Select Checked out to me view from taskpanel
			//-----------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckedOutToMe.Value);

			Log.message("2. View (" + Caption.MenuItems.CheckedOutToMe.Value + ") is selected from taskpanel.");

			//Verification: To verify if view navigation is successful
			//--------------------------------------------------------
			if(homePage.menuBar.getBreadCrumbLastItem().equals(Caption.MenuItems.CheckedOutToMe.Value))
				Log.pass("Test Case Passed. Navigated from " + viewToNavigate + " to " + Caption.MenuItems.CheckedOutToMe.Value + " is successful.");
			else
				Log.fail("Test Case Failed. Navigation from " + viewToNavigate + " to " + Caption.MenuItems.CheckedOutToMe.Value + " is not successful.",driver);


		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			Utility.quitDriver(driver);
		}

	} //End TC_025

	/**
	 * TC_026 : Navigate to 'Assigned to me' from TaskPane  
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint15", "Columns", "Smoke"}, 
			description = "Navigate to 'Assigned to me' from TaskPane")
	public void TC_026(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2: Select AssignedToMe view from taskpanel
			//-----------------------------------------------------
			homePage.taskPanel.clickItem(Caption.Taskpanel.AssignedToMe.Value);

			Log.message("2. View (" + Caption.Taskpanel.AssignedToMe.Value + ") is selected from taskpanel.");

			//Verification: To verify if view navigation is successful
			//--------------------------------------------------------
			if(homePage.menuBar.getBreadCrumbLastItem().equals(Caption.Taskpanel.AssignedToMe.Value))
				Log.pass("Test Case Passed. Navigated from " + viewToNavigate + " to " + Caption.Taskpanel.AssignedToMe.Value + " is successful.");
			else
				Log.fail("Test Case Failed. Navigation from " + viewToNavigate + " to " + Caption.Taskpanel.AssignedToMe.Value + " is not successful.",driver);



		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			Utility.quitDriver(driver);
		}

	} //End TC_026

	/**
	 * TC_027 : Navigate to 'HomePage' from TaskPane  
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint15", "Columns", "Smoke"}, 
			description = "Navigate to 'HomePage' from TaskPane")
	public void TC_027(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2: Select Home view from taskpanel
			//-----------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);

			Log.message("2. View (" + Caption.MenuItems.Home.Value + ") is selected from taskpanel.");

			//Verification: To verify if view navigation is successful
			//--------------------------------------------------------
			if(homePage.menuBar.getBreadCrumbLastItem().equals(testVault))
				Log.pass("Test Case Passed. Navigated from " + viewToNavigate + " to " + Caption.MenuItems.Home.Value + " is successful.");
			else
				Log.fail("Test Case Failed. Navigation from " + viewToNavigate + " to " + Caption.MenuItems.Home.Value + " is not successful.",driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		}

	} //End TC_027

	/**
	 * TC_028 : Navigate to 'Favorites' from TaskPane  
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint15", "Columns", "Smoke"}, 
			description = "Navigate to 'Favorites' from TaskPane")
	public void TC_028(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2: Select Favorites view from taskpanel
			//-----------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Favorites.Value);

			Log.message("2. View (" + Caption.MenuItems.Favorites.Value + ") is selected from taskpanel.");

			//Verification: To verify if view navigation is successful
			//--------------------------------------------------------
			if(homePage.menuBar.getBreadCrumbLastItem().equals(Caption.MenuItems.Favorites.Value))
				Log.pass("Test Case Passed. Navigated from " + viewToNavigate + " to " + Caption.MenuItems.Favorites.Value + " is successful.");
			else
				Log.fail("Test Case Failed. Navigation from " + viewToNavigate + " to " + Caption.MenuItems.Favorites.Value + " is not successful.",driver);


		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		}

	} //End TC_028

	/**
	 * TC_029 : 'Undo Check out' an object using context Menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint24"}, 
			description = "'Undo Check out' an object using context Menu")
	public void TC_029(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the checked out object
			//--------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {//Checks if object is checked out
				homePage.listView.clickItem(dataPool.get("ObjectName"));
				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);				

				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new SkipException("Object(" + dataPool.get("ObjectName") + ") is not checked out state.");
			}

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			Log.message("2. Checked out object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-3 : Select UndoCheckOut from context menu
			//------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.UndoCheckOut.Value);


			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true); //Clicks Undo checkout in MFiles dialog

			Log.message("3. " + Caption.MenuItems.UndoCheckOut.Value + ") is selected from context menu.");

			//Verification : To Verify object is checked in by pressing short cut key
			//-----------------------------------------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is undo checked out through context menu.");
			else
				Log.fail("Test case Failed. Object is not undo checked out through context menu.", driver);



		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_029

	/**
	 * TC_030 : Click 'Relationships' from context menu of an object 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint15", "Columns", "Smoke"}, 
			description = "Click 'Relationships' from context menu of an object")
	public void TC_030(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2 : Select the checked out object
			//--------------------------------------
			if (homePage.listView.itemCount() <= 0)  //Checks if item exists in the list
				throw new SkipException("Invalid test data. View (" + viewToNavigate + ") does not have any objects.");

			if (!homePage.listView.rightClickItemByIndex(0)) 
				throw new Exception("First Object is not selected.");

			String objName = homePage.listView.getItemNameByItemIndex(0);

			Log.message("2. First object in the view is selected.");

			//Step-3 : Open Relationships view from task panel
			//------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Relationships.Value);

			Log.message("2. View (" + Caption.MenuItems.Relationships.Value + ") is selected from context menu.");

			//Verification: To verify if view navigation is successful
			//--------------------------------------------------------
			if(ListView.isRelationshipsViewOpened(driver))
				Log.pass("Test Case Passed. Relationships view of an object (" + objName + ") is opened from context menu.");
			else
				Log.fail("Test Case Failed. Relationships view of an object (" + objName + ") is not opened from context menu.",driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		}

	} //End TC_030

	/**
	 * TC_031 : Click 'History' from context menu of an object 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint15", "Columns", "Smoke"}, 
			description = "Click 'History' from context menu of an object")
	public void TC_031(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2 : Select the checked out object
			//--------------------------------------
			if (homePage.listView.itemCount() <= 0)  //Checks if item exists in the list
				throw new SkipException("Invalid test data. View (" + viewToNavigate + ") does not have any objects.");

			if (!homePage.listView.rightClickItemByIndex(0)) 
				throw new Exception("First Object is not selected.");

			String objName = homePage.listView.getItemNameByItemIndex(0);

			Log.message("2. First object in the view is selected.");

			//Step-3 : Open History view from task panel
			//------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.History.Value);

			Log.message("2. View (" + Caption.MenuItems.History.Value + ") is selected from context menu.");

			//Verification: To verify if view navigation is successful
			//--------------------------------------------------------
			if(ListView.isHistoryViewOpened(driver))
				Log.pass("Test Case Passed. History view of an object (" + objName + ") is opened from context menu.");
			else
				Log.fail("Test Case Failed. History view of an object (" + objName + ") is not opened from context menu.",driver);


		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		}

	} //End TC_031

	/**
	 * TC032 : Click 'Add to Favorites' from context menu of Document
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Smoke"}, 
			description = "Click 'Add to Favorites' from context menu of Document")
	public void TC_032(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

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

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

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
	 * TC_034 : Click 'Properties' from context menu of an object 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint15", "Columns", "Smoke"}, 
			description = "Click 'Properties' from context menu of an object")
	public void TC_034(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2 : Select the object
			//--------------------------------------
			if (homePage.listView.itemCount() <= 0)  //Checks if item exists in the list
				throw new SkipException("Invalid test data. View (" + viewToNavigate + ") does not have any objects.");

			if (!homePage.listView.rightClickItemByIndex(0)) 
				throw new Exception("First Object is not selected.");

			String objName = homePage.listView.getItemNameByItemIndex(0);

			Log.message("2. First object in the view is selected.");

			//Step-3 : Open Properties view from task panel
			//------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value);

			Log.message("2. (" + Caption.MenuItems.Properties.Value + ") is selected from context menu.");

			//Verification: To verify if view navigation is successful
			//--------------------------------------------------------
			if(MetadataCard.isMetadataCardOpened(driver))
				Log.pass("Test Case Passed. Properties of an object (" + objName + ") is opened from context menu.");
			else
				Log.fail("Test Case Failed. Properties of an object (" + objName + ") is not opened from context menu.",driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		}

	} //End TC_034

	/**
	 * TC_035 : Click 'Workflows' from context menu of an object 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint15", "Columns", "Smoke"}, 
			description = "Click 'Workflows' from context menu of an object")
	public void TC_035(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2 : Select the object
			//--------------------------------------
			if (homePage.listView.itemCount() <= 0)  //Checks if item exists in the list
				throw new SkipException("Invalid test data. View (" + viewToNavigate + ") does not have any objects.");

			if (!homePage.listView.rightClickItemByIndex(0)) 
				throw new Exception("First Object is not selected.");

			String objName = homePage.listView.getItemNameByItemIndex(0);

			Log.message("2. First object in the view is selected.");

			//Step-3 : Open Workflows view from task panel
			//------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Workflow.Value);

			Log.message("2. (" + Caption.MenuItems.Workflow.Value + ") is selected from context menu.");

			//Verification: To verify if view navigation is successful
			//--------------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			if(mfilesDialog.isWorkflowDialogDisplayed())
				Log.pass("Test Case Passed. Workflow dialog of an object (" + objName + ") is opened from context menu.",driver);
			else
				Log.fail("Test Case Failed. Workflow dialog of an object (" + objName + ") is not opened from context menu.",driver);


		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		}

	} //End TC_035

	/**
	 * TC_036 : Download a Document from Task Pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint15", "Columns", "Smoke"}, 
			description = "Download a Document from Task Pane")
	public void TC_036(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2 : Select the item from the list
			//--------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-3 : Select Rename from context menu
			//----------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.DownloadFile.Value);

			Log.message("3. DownloadFile is selected from context menu.");

			//Verification: To verify if view navigation is successful
			//--------------------------------------------------------
			if(!homePage.downloadObjectFromTaskPane(dataPool.get("ObjectName")))
				Log.pass("Test Case Passed. Document (" + dataPool.get("ObjectName") + ") is downloaded from taskpanel.");
			else
				Log.fail("Test Case Failed. Document (" + dataPool.get("ObjectName") + ") is not downloaded from taskpanel.",driver);


		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		}

	} //End TC_036

	/**
	 * TC_037 : 'LogOut' from application using 'View and Modify' section
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32","Bug"}, 
			description = "'LogOut' from application using 'View and Modify' section")
	public void TC_037(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Listing Pane only' layout
			//-----------------------------------------------------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_ListingPaneOnly.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. " + Caption.ConfigSettings.Config_ListingPaneOnly.Value + " layout is selected and saved.");

			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("4. Navigated to '" + viewToNavigate + "' view."); 

			//Step-5 : Right click list view and select Log out from context menu
			//-------------------------------------------------------------------
			homePage.listView.rightClickListview();
			homePage.listView.clickContextMenuItem(Caption.MenuItems.LogOut.Value);

			Log.message("5. List view is right click and Log out is selected from context menu.");

			//Verification : To Verify if Log out is successful
			//-------------------------------------------------			
			int snooze = 0;

			while (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX") && snooze<20) {
				snooze++;
				Thread.sleep(500);
			}

			if (driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				Log.pass("Test case Passed. Log out from context menu is successful in listing pane only layout.");
			else
				Log.fail("Test case Failed. Log out from context menu is not successful in listing pane only layout.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try
			{
				Utility.resetToDefaultLayout(driver);
			}
			catch(Exception e0){
				Log.exception(e0, driver);
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_037

	/**
	 * TC_039 : Enter 'Comments' for an object using context Menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint24"}, 
			description = "Enter 'Comments' for an object using context Menu")
	public void TC_039(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Verify if search panel in search type is set as "Search in metadata and file contents"
			if(!homePage.searchPanel.getSearchInType().equalsIgnoreCase("Search in metadata and file contents"))
				homePage.searchPanel.setSearchInType("Search in metadata and file contents");

			//Step-2 : Select the checked out object
			//--------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			Log.message("2. Object is right clicked in the view.");

			//Step-3 : Select Check in from context menu
			//------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Comments.Value);

			Log.message("3. Option (" + Caption.MenuItems.Comments.Value + ") is selected from context menu.");

			//Step-4 : Enter comments in the dialog
			//-------------------------------------
			String comments = "TC_039" + Utility.getCurrentDateTime();

			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard.setComments(comments);
			metadatacard.saveAndClose();
			/*MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.setComment(comments); //Enters Comments
			mfilesDialog.clickOkButton(); //Clicks Ok button
			 */
			Log.message("4. Comments (" + comments + ") are entered in the Metadatacard.");

			//Verification : To Verify object is checked in by pressing short cut key
			//-----------------------------------------------------------------------
			String addlInfo = ListView.getCommentsByItemName(driver, dataPool.get("ObjectName"));

			if (addlInfo.equalsIgnoreCase(comments))
				Log.pass("Test case Passed. Object is entered with comments through context menu.");
			else {
				addlInfo = "Actual Comments : " + addlInfo + "; Expected Comments : " + comments;
				Log.fail("Test case Failed. Object is not entered with comments through context menu. Additional Information : " + addlInfo , driver);
			}

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			if(driver != null && homePage != null){

				try{
					if(homePage.listView.isColumnExists(Caption.Column.Coln_Comment.Value))
						homePage.listView.removeColumn(Caption.Column.Coln_Comment.Value);
				}
				catch(Exception e0){
					Log.exception(e0, driver);
				}
			}

			Utility.quitDriver(driver);
		} //End finally

	} //End TC_039


	/**
	 * TC_044 : Convert Single document to Multi File Document(MFD) using Context Menu.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Smoke","Bug"}, 
			description = "Convert Single document to Multi File Document(MFD) using Context Menu.")
	public void TC_044(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

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
			if (!ListView.isSFDByItemName(driver, dataPool.get("ObjectName"))) //Checks if this is SFD
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not Single file document.");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
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
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Smoke","Bug"}, 
			description = "Convert MFD document to Single File Document(SFD) using Context Menu")
	public void TC_045(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

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


			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if it is in checked in state
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-3 : Select Convert to MFD from context menu
			//-------------------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.ConvertToMFD_C.Value); //Clicks Convert SFD to MFD option from context menu

			String mfdName = dataPool.get("ObjectName").split("\\.")[0];

			if (ListView.isSFDByItemName(driver, mfdName))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not converted to MFD document.");

			Log.message("3. Object (" + dataPool.get("ObjectName") + ") converted to MFD document.");

			//Step-4 : Select MFD and Convert to SFD from context menu
			//---------------------------------------------------------
			if (!homePage.listView.rightClickItem(mfdName)) //Selects the Object in the list
				throw new Exception("Object (" + mfdName + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.ConvertToSFD_C.Value); //Clicks Convert SFD to MFD option from context menu


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
	 * TC_047 : 'Get MFiles Web URL' of the object using Context Menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "Smoke"}, 
			description = "'Get MFiles Web URL' of the object using Context Menu.")
	public void TC_047(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Right click on the object and select Get M-Files Web URL from context menu
			//-----------------------------------------------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			Log.message("2. Right clicked on an object (" + dataPool.get("ObjectName") + ") and Get M-Files Web URL from context menu is selected.");

			//Step-3 : Open Get M-Files Web URL dialog for the object from context menu
			//-------------------------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-4 : Copy the link from text box and close the Get M-Files Web URL dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied and Get M-Files Web URL dialog is closed. Hyperlink URL : " + hyperlinkText, driver);

			//Step-5 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver.get(hyperlinkText); //Navigates to the hyperlink url
			Thread.sleep(5000);//Waits for the page load

			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied object Hyperlink.");

			Log.message("5. Object Hyperlink is opened in the browser.");

			//Verification : To Verify if object exists in the URL
			//----------------------------------------------------
			homePage = new HomePage(driver);

			if (homePage.listView.itemCount() != 1) {  //Verifies if only one object is displayed in the list
				Log.fail("Test case Failed. One item is not getting displayed on opening the hyperlink.", driver);
				return;
			}

			if (homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Verifies if object exists in the hyperlink
				Log.pass("Test case Passed. Object (" + dataPool.get("ObjectName") + ") exists in the hyperlink.");
			else
				Log.fail("Test case Failed. Object (" + dataPool.get("ObjectName") + ") does not exists in the hyperlink.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_047


	/**
	 * TC_048 : 'CheckOut' an object using Taskpanel
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint24"}, 
			description = "'CheckOut' an object using Taskpanel.")
	public void TC_048(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

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
			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))//Checks if object is checked out
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is already in checked out state.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-3 : Select Check out from context menu
			//------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);


			Log.message("3. " + Caption.MenuItems.CheckOut.Value + ") is selected from taskpanel.");

			//Verification : To Verify object is checked in by pressing short cut key
			//-----------------------------------------------------------------------
			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is checked out from taskpanel.");
			else
				Log.fail("Test case Failed. Object is not checked out from taskpanel.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_048


	/**
	 * TC_049 : Click 'Workflows' from Taskpanel of an object 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint15", "Columns", "Smoke"}, 
			description = "Click 'Workflows' from Taskpanel of an object")
	public void TC_049(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2 : Select the object
			//--------------------------------------
			if (homePage.listView.itemCount() <= 0)  //Checks if item exists in the list
				throw new SkipException("Invalid test data. View (" + viewToNavigate + ") does not have any objects.");

			if (!homePage.listView.clickItemByIndex(0)) 
				throw new Exception("First Object is not selected.");

			String objName = homePage.listView.getItemNameByItemIndex(0);

			Log.message("2. First object in the view is selected.");

			//Step-3 : Open Workflows view from task panel
			//------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);

			Log.message("2. (" + Caption.MenuItems.Workflow.Value + ") is selected from taskpanel.");

			//Verification: To verify if view navigation is successful
			//--------------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver);

			if(mfilesDialog.isWorkflowDialogDisplayed())
				Log.pass("Test Case Passed. Workflow dialog of an object (" + objName + ") is opened from taskpanel.");
			else
				Log.fail("Test Case Failed. Workflow dialog of an object (" + objName + ") is not opened from taskpanel.",driver);


		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		}

	} //End TC_049


	/**
	 * TC_050 : 'CheckIn' an object using Taskpanel
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint24"}, 
			description = "'CheckIn' an object using Taskpanel")
	public void TC_050(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the checked out object
			//--------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {//Checks if object is checked out
				homePage.listView.clickItem(dataPool.get("ObjectName"));
				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);				


				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new SkipException("Object(" + dataPool.get("ObjectName") + ") is not checked out state.");
			}

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			Log.message("2. Checked out object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-3 : Select Check in from Taskpanel
			//------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);


			Log.message("3. " + Caption.MenuItems.CheckIn.Value + ") is selected from Taskpanel.");

			//Verification : To Verify object is checked in by pressing short cut key
			//-----------------------------------------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is checked in through Taskpanel.");
			else
				Log.fail("Test case Failed. Object is not checked in through Taskpanel.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_050


	/**
	 * TC_051 : 'Undo Check out' an object using taskpanel
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint24"}, 
			description = "'Undo Check out' an object using taskpanel")
	public void TC_051(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the checked out object
			//--------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {//Checks if object is checked out
				homePage.listView.clickItem(dataPool.get("ObjectName"));
				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);				


				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new SkipException("Object(" + dataPool.get("ObjectName") + ") is not checked out state.");
			}

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			Log.message("2. Checked out object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-3 : Select UndoCheckOut from context menu
			//------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);


			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.confirmUndoCheckOut(true); //Clicks Undo checkout in MFiles dialog

			Log.message("3. " + Caption.MenuItems.UndoCheckOut.Value + ") is selected from taskPanel.");

			//Verification : To Verify object is checked in by pressing short cut key
			//-----------------------------------------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is undo checked out through taskPanel.");
			else
				Log.fail("Test case Failed. Object is not undo checked out through taskPanel.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_051


	/**
	 * TC_052 : Click 'Relationships' from taskpanel of an object 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint15", "Columns", "Smoke"}, 
			description = "Click 'Relationships' from taskpanel of an object")
	public void TC_052(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2 : Select the checked out object
			//--------------------------------------
			if (homePage.listView.itemCount() <= 0)  //Checks if item exists in the list
				throw new SkipException("Invalid test data. View (" + viewToNavigate + ") does not have any objects.");

			if (!homePage.listView.rightClickItemByIndex(0)) 
				throw new Exception("First Object is not selected.");

			String objName = homePage.listView.getItemNameByItemIndex(0);

			Log.message("2. First object in the view is selected.");

			//Step-3 : Open Relationships view from task panel
			//------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Relationships.Value);

			Log.message("2. View (" + Caption.MenuItems.Relationships.Value + ") is selected from task panel.");

			//Verification: To verify if view navigation is successful
			//--------------------------------------------------------
			if(ListView.isRelationshipsViewOpened(driver))
				Log.pass("Test Case Passed. Relationships view of an object (" + objName + ") is opened from taskPanel.");
			else
				Log.fail("Test Case Failed. Relationships view of an object (" + objName + ") is not opened from taskPanel.",driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		}

	} //End TC_052


	/**
	 * TC_053 : Click 'History' from taskpanel of an object 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint15", "Columns", "Smoke"}, 
			description = "Click 'History' from taskpanel of an object")
	public void TC_053(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2 : Select the checked out object
			//--------------------------------------
			if (homePage.listView.itemCount() <= 0)  //Checks if item exists in the list
				throw new SkipException("Invalid test data. View (" + viewToNavigate + ") does not have any objects.");

			if (!homePage.listView.rightClickItemByIndex(0)) 
				throw new Exception("First Object is not selected.");

			String objName = homePage.listView.getItemNameByItemIndex(0);

			Log.message("2. First object in the view is selected.");

			//Step-3 : Open History view from task panel
			//------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.History.Value);

			Log.message("2. View (" + Caption.MenuItems.History.Value + ") is selected from taskpanel.");

			//Verification: To verify if view navigation is successful
			//--------------------------------------------------------
			if(ListView.isHistoryViewOpened(driver))
				Log.pass("Test Case Passed. History view of an object (" + objName + ") is opened from taskpanel.");
			else
				Log.fail("Test Case Failed. History view of an object (" + objName + ") is not opened from taskpanel.",driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		}

	} //End TC_053


	/**
	 * TC_054 : Verify 'Make Copy'  from 'View and Modify' section of homePage.taskPanel.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Smoke","Script"}, 
			description = "Verify 'Make Copy'  from 'View and Modify' section of homePage.taskPanel.")
	public void TC_054(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

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
			metadatacard.setCheckInImmediately(true);
			ConcurrentHashMap <String, String> prevInfo = metadatacard.getInfo(); //Gets the property information			

			Log.message("3. New object details are entered in make copy metadatacard.", driver);

			//Step-4 : Save the metadatacard
			//------------------------------
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			String docName = prevInfo.get("Name or title") + dataPool.get("Extension");

			Log.message("4. New Document metadatacard is saved.", driver);

			//Step-5 : Open the Properties dialog of the new document through context menu
			//-----------------------------------------------------------------------------
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
	 * TC_062 : 'CheckOut' an object using operations Menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint24"}, 
			description = "'CheckOut' an object using operations Menu.")
	public void TC_062(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

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
			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))//Checks if object is checked out
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is already in checked out state.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-3 : Select Check out from operations menu
			//------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.CheckOut.Value);


			Log.message("3. " + Caption.MenuItems.CheckOut.Value + ") is selected from operations menu.");

			//Verification : To Verify object is checked in by pressing short cut key
			//-----------------------------------------------------------------------
			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is checked out from operations menu.");
			else
				Log.fail("Test case Failed. Object is not checked out from operations menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_062


	/**
	 * TC_063 : 'CheckIn' an object using operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint24"}, 
			description = "'CheckIn' an object using operations menu")
	public void TC_063(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the checked out object
			//--------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {//Checks if object is checked out
				homePage.listView.clickItem(dataPool.get("ObjectName"));
				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);				


				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new SkipException("Object(" + dataPool.get("ObjectName") + ") is not checked out state.");
			}

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			Log.message("2. Checked out object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-3 : Select Check in from operations menu
			//------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.CheckIn.Value);


			Log.message("3. " + Caption.MenuItems.CheckIn.Value + ") is selected from operations menu.");

			//Verification : To Verify object is checked in by pressing short cut key
			//-----------------------------------------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is checked in through operations menu.");
			else
				Log.fail("Test case Failed. Object is not checked in through operations menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_063


	/**
	 * TC_064 : 'CheckIn with comments' an object using operations Menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint24"}, 
			description = "'CheckIn with comments' an object using operations Menu")
	public void TC_064(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null;
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the checked out object
			//--------------------------------------
			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {//Checks if object is checked out
				homePage.listView.clickItem(dataPool.get("ObjectName"));
				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);				


				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new SkipException("Object(" + dataPool.get("ObjectName") + ") is not checked out state.");
			}

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			Log.message("2. Checked out object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-3 : Select Check in from operations menu
			//------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.CheckInWithComments.Value);


			Log.message("3. " + Caption.MenuItems.CheckInWithComments.Value + ") is selected from operations menu.");

			//Step-4 : Enter comments in the dialog
			//-------------------------------------
			String comments = "TC_064" + Utility.getCurrentDateTime(); 

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.setComment(comments); //Enters Comments
			mfilesDialog.clickOkButton(); //Clicks Ok button

			Log.message("4. Comments (" + comments + ") are entered in the MFiles dialog.");

			//Verification : To Verify object is checked in by pressing short cut key
			//-----------------------------------------------------------------------
			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Test case Failed. Object is not checked in through context menu on selecting Check-in with comments.");

			String addlInfo = ListView.getCommentsByItemName(driver, dataPool.get("ObjectName"));

			if (addlInfo.equalsIgnoreCase(comments))
				Log.pass("Test case Passed. Object is checked in with comments through operations menu.");
			else {
				addlInfo = "Actual Comments : " + addlInfo + "; Expected Comments : " + comments;
				Log.fail("Test case Failed. Object is not checked in with comments through operations menu. Additional Information : " + addlInfo , driver);
			}

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			if(driver != null && homePage != null){

				try{
					if(homePage.listView.isColumnExists(Caption.Column.Coln_Comment.Value))
						homePage.listView.removeColumn(Caption.Column.Coln_Comment.Value);
				}
				catch(Exception e0){
					Log.exception(e0, driver);
				}
			}

			Utility.quitDriver(driver);
		} //End finally

	} //End TC_064


	/**
	 * TC_065 : 'UndoCheckOut' a document using Settings->Operations->UndoCheckOut
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint32"}, 
			description = "'UndoCheckOut' a document using Settings->Operations->UndoCheckOut")
	public void TC_065(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform checkout operation
			//---------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel


			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-3 : Select Undo-Checkout from operations menu
			//---------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value); //Selects Show Members from task panel


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


	/**
	 * TC_067 : Click 'Relationships' from operations menu of an object 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint15", "Columns", "Smoke"}, 
			description = "Click 'Relationships' from operations menu of an object")
	public void TC_067(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2 : Select the checked out object
			//--------------------------------------
			if (homePage.listView.itemCount() <= 0)  //Checks if item exists in the list
				throw new SkipException("Invalid test data. View (" + viewToNavigate + ") does not have any objects.");

			if (!homePage.listView.clickItemByIndex(0)) 
				throw new Exception("First Object is not selected.");

			String objName = homePage.listView.getItemNameByItemIndex(0);

			Log.message("2. First object in the view is selected.");

			//Step-3 : Open Relationships view from operations
			//------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Relationships.Value);

			Log.message("2. View (" + Caption.MenuItems.Relationships.Value + ") is selected from operations menu.");

			//Verification: To verify if view navigation is successful
			//--------------------------------------------------------
			if(ListView.isRelationshipsViewOpened(driver))
				Log.pass("Test Case Passed. Relationships view of an object (" + objName + ") is opened from operations menu.");
			else
				Log.fail("Test Case Failed. Relationships view of an object (" + objName + ") is not opened from operations menu.",driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		}

	} //End TC_067


	/**
	 * TC_068 : Click 'History' from operations menu of an object 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint15", "Columns", "Smoke"}, 
			description = "Click 'History' from operations menu of an object")
	public void TC_068(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2 : Select the checked out object
			//--------------------------------------
			if (homePage.listView.itemCount() <= 0)  //Checks if item exists in the list
				throw new SkipException("Invalid test data. View (" + viewToNavigate + ") does not have any objects.");

			if (!homePage.listView.clickItemByIndex(0)) 
				throw new Exception("First Object is not selected.");

			String objName = homePage.listView.getItemNameByItemIndex(0);

			Log.message("2. First object in the view is selected.");

			//Step-3 : Open History view from operations
			//------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.History.Value);

			Log.message("2. View (" + Caption.MenuItems.History.Value + ") is selected from operations menu.");

			//Verification: To verify if view navigation is successful
			//--------------------------------------------------------
			if(ListView.isHistoryViewOpened(driver))
				Log.pass("Test Case Passed. History view of an object (" + objName + ") is opened from operations menu.");
			else
				Log.fail("Test Case Failed. History view of an object (" + objName + ") is not opened from operations menu.",driver);


		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		}

	} //End TC_068


	/**
	 * TC_069 : Enter 'Comments' for an object using operations Menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint24"}, 
			description = "Enter 'Comments' for an object using operations Menu")
	public void TC_069(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Verify if search panel in search type is set as "Search in metadata and file contents"
			if(!homePage.searchPanel.getSearchInType().equalsIgnoreCase("Search in metadata and file contents"))
				homePage.searchPanel.setSearchInType("Search in metadata and file contents");

			//Step-2 : Select the checked out object
			//--------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			Log.message("2. Object is right clicked in the ivew.");

			//Step-3 : Select Check in from operations menu
			//------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Comments.Value);

			Log.message("3. (" + Caption.MenuItems.Comments.Value + ") is selected from operations menu.");

			//Step-4 : Enter comments in the dialog
			//-------------------------------------
			String comments = "TC_069" + Utility.getCurrentDateTime(); 

			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard.setComments(comments);
			metadatacard.saveAndClose();

			Log.message("4. Comments (" + comments + ") are entered in the metadatacard.");

			//Verification : To Verify object is checked in by pressing short cut key
			//-----------------------------------------------------------------------
			String addlInfo = ListView.getCommentsByItemName(driver, dataPool.get("ObjectName"));

			if (addlInfo.equalsIgnoreCase(comments))
				Log.pass("Test case Passed. Object is entered with comments through operations menu.");
			else {
				addlInfo = "Actual Comments : " + addlInfo + "; Expected Comments : " + comments;
				Log.fail("Test case Failed. Object is not entered with comments through operations menu. Additional Information : " + addlInfo , driver);
			}


		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			if(driver != null && homePage != null){

				try{
					if(homePage.listView.isColumnExists(Caption.Column.Coln_Comment.Value))
						homePage.listView.removeColumn(Caption.Column.Coln_Comment.Value);
				}
				catch(Exception e0){
					Log.exception(e0, driver);
				}
			}

			Utility.quitDriver(driver);
		} //End finally

	} //End TC_069


	/**
	 * TC_070 : Click 'Properties' from operations menu of an object 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint15", "Columns", "Smoke"}, 
			description = "Click 'Properties' from operations menu of an object")
	public void TC_070(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2 : Select the object
			//--------------------------------------
			if (homePage.listView.itemCount() <= 0)  //Checks if item exists in the list
				throw new SkipException("Invalid test data. View (" + viewToNavigate + ") does not have any objects.");

			if (!homePage.listView.clickItemByIndex(0)) 
				throw new Exception("First Object is not selected.");

			String objName = homePage.listView.getItemNameByItemIndex(0);

			Log.message("2. First object in the view is selected.");

			//Step-3 : Open Properties view from operations
			//------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value);

			Log.message("2. (" + Caption.MenuItems.Properties.Value + ") is selected from operations menu.");

			//Verification: To verify if view navigation is successful
			//--------------------------------------------------------
			if(MetadataCard.isMetadataCardOpened(driver))
				Log.pass("Test Case Passed. Properties of an object (" + objName + ") is opened from operations menu.");
			else
				Log.fail("Test Case Failed. Properties of an object (" + objName + ") is not opened from operations menu.",driver);


		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		}

	} //End TC_070


	/**
	 * TC_071 : 'Get MFiles Web URL' of the object using operations Menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint25", "Get M-Files Web URL", "Smoke"}, 
			description = "'Get MFiles Web URL' of the object using operations Menu.")
	public void TC_071(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Right click on the object and select Get M-Files Web URL from context menu
			//-----------------------------------------------------------------------operations-----
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.GetMFilesWebURL.Value); //Selects Get M-Files Web URL from context menu

			Log.message("2. Right clicked on an object (" + dataPool.get("ObjectName") + ") and Get M-Files Web URL from operations menu is selected.");

			//Step-3 : Open Get M-Files Web URL dialog for the object from operations menu
			//-------------------------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isGetMFilesWebURLDialogOpened()) //Checks for MFiles dialog title
				throw new Exception("M-Files dialog with 'Get M-Files Web URL' title is not opened.");

			Log.message("3. Get M-Files Web URL dialog of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-4 : Copy the link from text box and close the Get M-Files Web URL dialog
			//-----------------------------------------------------------------------
			String hyperlinkText = mfilesDialog.getHyperlink(); //Gets the hyperlink
			mfilesDialog.close(); //Closes the Get M-Files Web URL dialog

			Log.message("4. Hyperlink is copied and Get M-Files Web URL dialog is closed. Hyperlink URL : " + hyperlinkText, driver);

			//Step-5 : Open the copied Hyperlink in the browser
			//-------------------------------------------------
			driver.get(hyperlinkText); //Navigates to the hyperlink url
			Thread.sleep(5000);//Waits for the page load
			if (!driver.getCurrentUrl().equalsIgnoreCase(hyperlinkText))
				throw new Exception ("Browser is not opened with copied object Hyperlink.");

			Log.message("5. Object Hyperlink is opened in the browser.");

			//Verification : To Verify if object exists in the URL
			//----------------------------------------------------
			homePage = new HomePage(driver);

			if (homePage.listView.itemCount() != 1) {  //Verifies if only one object is displayed in the list
				Log.fail("Test case Failed. One item is not getting displayed on opening the hyperlink.", driver);
				return;
			}

			if (homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Verifies if object exists in the hyperlink
				Log.pass("Test case Passed. Object (" + dataPool.get("ObjectName") + ") exists in the hyperlink.");
			else
				Log.fail("Test case Failed. Object (" + dataPool.get("ObjectName") + ") does not exists in the hyperlink.", driver);


		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_071


	/**
	 * TC_072 : Click 'Add to Favorites' from operations menu of Document
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Smoke"}, 
			description = "Click 'Add to Favorites' from operations menu of Document")
	public void TC_072(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Right click on any object
			//----------------------------------
			String randObj = ListView.getRandomObject(driver); //Gets random object from the list in view

			if (!homePage.listView.clickItem(randObj)) //Right clicks random object
				throw new Exception("Object (" + randObj + ") is not right clicked.");

			Log.message("2.Object (" + randObj + ") is right clicked.", driver);

			//Step-3 : Select Add to Favorites from operations menu
			//-------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.AddToFavorites.Value); //Selects Add to Favorites from context menu

			MFilesDialog mfilesDialog = new MFilesDialog (driver); //Instantiates MFilesDialog

			if (!mfilesDialog.getMessage().toUpperCase().contains("ONE OBJECT WAS AFFECTED")) //Checks if One object was affected message appeared
				throw new Exception("One object was affected message does not appear after adding object to favorites");

			mfilesDialog.clickOkButton(); //Clicks Ok button in the MFiles information dialog

			Log.message("3. Add to Favorites is selected from operations menu.");

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

	} //End TC_072


	/**
	 * TC_073 : Click 'Remove from Favorites' from operations menu of Document
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Smoke"}, 
			description = "Click 'Remove from Favorites' from operations menu of Document")
	public void TC_073(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Right click on any object
			//----------------------------------
			String randObj = ListView.getRandomObject(driver); //Gets random object from the list in view

			if (!homePage.listView.clickItem(randObj)) //Right clicks random object
				throw new Exception("Object (" + randObj + ") is not clicked.");

			Log.message("2.Object (" + randObj + ") is selected in the view.", driver);

			//Step-3 : Check and Select Add to Favorites from operations menu
			//-------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the right pane metadatacard

			MFilesDialog mfilesDialog = null;

			if (!metadataCard.isFavorite())//Checks if object is already added to favorite or not
			{
				homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.AddToFavorites.Value); //Selects Add to Favorites from context menu

				mfilesDialog = new MFilesDialog (driver); //Instantiates MFilesDialog

				if (!mfilesDialog.getMessage().toUpperCase().contains("ONE OBJECT WAS AFFECTED")) //Checks if One object was affected message appeared
					throw new Exception("One object was affected message does not appear after adding object to favorites");

				mfilesDialog.clickOkButton(); //Clicks Ok button in the MFiles information dialog
			}
			Log.message("3. Object is added to favorites from operations menu.");

			//Step-4 : Navigate to Favorites view
			//-----------------------------------
			viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, Caption.MenuItems.Favorites.Value, "");

			if (!homePage.listView.isItemExists(randObj))
				throw new Exception("Object (" + randObj + ") is not available in Favorites view.");

			Log.message("4. Navigated to '" + viewToNavigate + "' view.");

			//Step-5 : Right click on the object
			//---------------------------------
			if (!homePage.listView.clickItem(randObj)) //Right clicks random object
				throw new Exception("Object (" + randObj + ") is not clicked.");

			Log.message("5.Object (" + randObj + ") is selected.", driver);

			//Step-6 : Select Remove From Favorites from operations menu
			//-------------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.RemoveFromFavorites.Value); //Selects Remove from Favorites from context menu

			mfilesDialog = new MFilesDialog (driver); //Instantiates MFilesDialog

			if (!mfilesDialog.getMessage().toUpperCase().contains("REMOVE THE SELECTED OBJECT FROM THE FAVORITES?")) //Checks if One object was affected message appeared
				throw new Exception("Confirmation message to remove object from favorites is not displayed.");

			mfilesDialog.clickOkButton(); //Clicks Yes button in the MFiles confirmation dialog

			mfilesDialog = new MFilesDialog (driver); //Instantiates MFilesDialog

			if (!mfilesDialog.getMessage().toUpperCase().contains("ONE OBJECT WAS AFFECTED")) //Checks if One object was affected message appeared
				throw new Exception("One object was affected message does not appear after removing object to favorites");

			mfilesDialog.clickOkButton(); //Clicks Ok button in the MFiles information dialog

			Log.message("6. Remove from Favorites is selected from operations menu.");

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

	} //End TC_073


	/**
	 * TC_074 : Rename the object using operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint24"}, 
			description = "Rename the object using operations menu.")
	public void TC_074(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the item from the list
			//--------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			String extension = "";

			if(dataPool.get("ObjectName").contains("."))
				extension = "."+dataPool.get("ObjectName").split("\\.")[1];

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-3 : Select Rename from operations menu
			//----------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Rename.Value);
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.isRenameDialogOpened())
				throw new Exception("Rename dialog is not opened.");

			Log.message("3. Rename is selected from operations menu.");

			//Step-4 : Enter new name in the rename dialog
			//----------------------------------------------
			String newName = "TC_074" + Utility.getCurrentDateTime(); 
			mfilesDialog.rename(newName, true);


			Log.message("4. Rename dialog is opened and new name (" + newName +") is entered.");

			//Verification : To Verify short key for Rename is available in context menu
			//---------------------------------------------------------------------------
			newName += extension;
			//Verifies that Rename short cut key is available in operations menu
			if (homePage.listView.isItemExists(newName))
				Log.pass("Test case Passed. Object is renamed successfully through operations menu.");
			else
				Log.fail("Test case Failed. Object is not renamed through operations menu", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_074


	/**
	 * TC_075 : Delete the object using operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint24", "Smoke"}, 
			description = "Delete the object using operations menu.")
	public void TC_075(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the item from the list
			//--------------------------------------
			int prevCt = homePage.listView.itemCount(); //Gets the items from list

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is selected.");

			//Step-3 Press Delete from operations menu
			//--------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Delete.Value);
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			if (!mfilesDialog.confirmDelete()) //Clicks Yes in the confirmation dialog to perform delete operation
				throw new SkipException("Confirmation dialog has not appeared after pressing DEL key");

			Log.message("3. Delete is selected from operations menu and confirmed the delete action.");

			//Verification : To Verify short key for Delete is available in context menu
			//---------------------------------------------------------------------------
			int currCt = homePage.listView.itemCount(); //Gets the number of object in the list after deletion

			if (prevCt != currCt + 1) { //Verifies if number of objects in the list has reduced by 1
				Log.fail("No of objects has not reduced by 1 after deleting document by DEL key.", driver);
				return;
			}

			//Verifies that Delete short cut key is available in operations menu
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object (" + dataPool.get("ObjectName") + ") is deleted by selecting Delete from operations menu.");
			else
				Log.fail("Test case Failed. Object (" + dataPool.get("ObjectName") + ") exists after by performing Delete operation from operations menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_075


	/**
	 * TC_076. : Convert MFD document to Single File Document(SFD) using Settings->Operations
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Smoke","Bug"}, 
			description = "Convert MFD document to Single File Document(SFD) using Settings->Operations")
	public void TC_076(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (!ListView.isSFDByItemName(driver, dataPool.get("ObjectName"))) //Checks if this is SFD
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not Single file document.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel


			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if it is in checked in state
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-3 : Select Convert to MFD from operations menu
			//---------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value); //Clicks Convert SFD to MFD option from operations menu

			String mfdName = dataPool.get("ObjectName").split("\\.")[0];

			if (ListView.isSFDByItemName(driver, mfdName))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not converted to MFD document.");

			Log.message("3. Object (" + dataPool.get("ObjectName") + ") converted to MFD document.");

			//Step-4 : Select MFD and Convert to SFD from opeartions menu
			//---------------------------------------------------------
			if (!homePage.listView.clickItem(mfdName)) //Selects the Object in the list
				throw new Exception("Object (" + mfdName + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.ConvertToSFD_O.Value); //Clicks Convert SFD to MFD option from operations menu


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

		WebDriver driver = null; 

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
			if (!ListView.isSFDByItemName(driver, dataPool.get("ObjectName"))) //Checks if this is SFD
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not Single file document.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if it is in checked in state
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-3 : Select Convert to MFD from operations menu
			//---------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value); //Clicks Convert SFD to MFD option from operations menu

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
	 * TC_078 : Click 'Workflows' from operations menu of an object 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint15", "Columns", "Smoke"}, 
			description = "Click 'Workflows' from operations menu of an object")
	public void TC_078(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2 : Select the object
			//--------------------------------------
			if (homePage.listView.itemCount() <= 0)  //Checks if item exists in the list
				throw new SkipException("Invalid test data. View (" + viewToNavigate + ") does not have any objects.");

			if (!homePage.listView.clickItemByIndex(0)) 
				throw new Exception("First Object is not selected.");

			String objName = homePage.listView.getItemNameByItemIndex(0);

			Log.message("2. First object in the view is selected.");

			//Step-3 : Open Workflows view from operations
			//------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);

			Log.message("2. (" + Caption.MenuItems.Workflow.Value + ") is selected from operations menu.");

			//Verification: To verify if view navigation is successful
			//--------------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver);

			if(mfilesDialog.isWorkflowDialogDisplayed())
				Log.pass("Test Case Passed. Workflow dialog of an object (" + objName + ") is opened from operations menu.");
			else
				Log.fail("Test Case Failed. Workflow dialog of an object (" + objName + ") is not opened from operations menu.",driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		}

	} //End TC_078


	/**
	 * TC_079 : 'LogOut' from application from user details
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Smoke"}, 
			description = "'LogOut' from application from user details")
	public void TC_079(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();//Launches the driver
			LoginPage.launchDriverAndLogin(driver, true);//Login to the application

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2 : Select Log out from user display
			//-----------------------------------------
			Utility.logoutFromWebAccess(driver);//Log out from web access.

			Log.message("2. Logout is selected from user display.");

			//Verification : Verify if log out is successful
			//----------------------------------------------------------
			if (driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				Log.pass("Test case Passed. M-Files web access is logged out successfully from user display.");
			else
				Log.fail("Test case Failed. M-Files web access is not logged out from user display.", driver);

		}//End of try

		catch(Exception e) {
			Log.exception(e, driver);
		}//End of Catch

		finally{
			Utility.quitDriver(driver);
		}//End of Finally

	}//End of TC_079


	/**
	 * TC_080 : 'Change Password' from application in user display
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Smoke"}, 
			description = "'Change Password' from application in user display")
	public void TC_080(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), dataPool.get("Password"), testVault);

			Log.message("Pre-requsite : Browser is opened and logged into MFWA. ( User Name : " + dataPool.get("UserName") + "; Vault : " + testVault + ")", driver);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2 : Select Log out from user display
			//-----------------------------------------
			homePage.menuBar.changePassword(dataPool.get("Password"), "testing@123"); //Selects log out from user display

			Log.message("2. Change password dialog is opened and new password is changed to the user.");

			//Step-3 : Get the mfiles dialog message and click the ok button
			//--------------------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog (driver);
			if(!mfilesDialog.getMessage().equalsIgnoreCase("Your password has been changed. You will be logged out automatically. Please log in to M-Files with the new password."))
				throw new Exception("Mfiles dialog is not displayed as expected.");

			mfilesDialog.clickOkButton();//Click the ok button M-files dialog
			Utils.fluentWait(driver);

			Log.message("3. Closed the mfiles dialog for changed the password.");

			//Step-4 : Login with new password
			//--------------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("UserName"), "testing@123", testVault);

			Log.message("4. Enter user details with new password in login page.");

			//Verification : Verify if log in is successful with new password
			//---------------------------------------------------------------
			if (driver.getCurrentUrl().toUpperCase().contains("DEFAULT.ASPX"))
				Log.pass("Test case Passed. M-Files web access is logged in successfully with new password.");
			else
				Log.fail("Test case Failed. M-Files web access is not logged in with new password.", driver);


		}//End of try

		catch(Exception e) {
			Log.exception(e, driver);
		}//End of Catch

		finally{
			Utility.quitDriver(driver);

		}//End of Finally

	}//End of TC_080


	/**
	 * TC_082 : Display Mode -> Group Objects by Object Type
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Smoke"},dependsOnMethods = {"TC_008", "TC_014", "TC_32152"}, 
			description = "Display Mode -> Group Objects by Object Type")
	public void TC_082(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2 : Change display mode to Group objects by object types
			//--------------------------------------------------------------
			String expectedGrouping = dataPool.get("ExpectedGroups");
			String[] expectedGroups = expectedGrouping.split(",");
			int counter = 0;

			if (homePage.listView.groupHeaderCount() == 1 && homePage.listView.groupExists("Objects (")) {
				homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode + ">>" + Caption.MenuItems.GrpObjByObjType);
				Log.message("2. 'Group objects by Object Type' option is selected.");
			}
			else
				Log.message("2. View is already in 'Group objects by Object Type' display mode.");

			//Verification : To verify if display mode is grouped by object types
			//-------------------------------------------------------------------
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

		}//End of try

		catch(Exception e) {
			Log.exception(e, driver);
		}//End of Catch

		finally{
			Utility.quitDriver(driver);
		}//End of Finally

	}//End of TC_082


	/**
	 * TC_083 : Display Mode ->  Group Views and Folders
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Smoke"}, 
			description = "Display Mode -> Group Views and Folders")
	public void TC_083(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Change display mode to Group Views and Folders
			//--------------------------------------------------------------
			String expectedGrouping = dataPool.get("ExpectedGroups");
			String[] expectedGroups = expectedGrouping.split(",");
			int counter = 0;

			if (!homePage.isListViewDisplayed())
				homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode + ">>" + Caption.MenuItems.Details);

			if (homePage.listView.groupHeaderCount() == 0) {
				homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode + ">>" + Caption.MenuItems.Details);
				homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode + ">>" + Caption.MenuItems.GrpViewsAndFolders);
				Log.message("---Selected 'GroupViewsAndFolders' option of Display mode.");
			}

			Log.message("2. View is changed to 'Group Views and Folders' display mode.");

			//Verification : To verify if display mode is Group Views and Folders
			//-------------------------------------------------------------------
			int groupHeadersDisplayed = homePage.listView.groupHeaderCount();

			if (groupHeadersDisplayed == expectedGroups.length) {
				for(counter = 0; counter < expectedGroups.length; counter++) {
					if (!homePage.listView.groupExists(expectedGroups[counter]))
						break;
				}
			}

			if (counter == expectedGroups.length)
				Log.pass("Test Passed. 'Group Views and Folders' option displayed expected.");
			else
				Log.fail("Test Failed. 'Views and Folders' are not grouped as expected.", driver);


		}//End of try

		catch(Exception e) {
			Log.exception(e, driver);
		}//End of Catch

		finally{
			Utility.quitDriver(driver);
		}//End of Finally

	}//End of TC_083


	/**
	 * TC_084 : Display Mode -> Details
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Smoke"}, 
			description = "Display Mode -> Details")
	public void TC_084(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2 : Change display mode to Details
			//-----------------------------------------
			if (!homePage.isListViewDisplayed())
				homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode + ">>" + Caption.MenuItems.Details);

			Log.message("2. View is changed to 'Details' display mode.");

			//Verification : To verify if display mode is Details
			//-------------------------------------------------------------------
			if (homePage.isListViewDisplayed())
				Log.pass("Test Passed. 'Details' Display mode is displayed.");
			else
				Log.fail("Test Failed. 'Details' Display mode is not displayed.", driver);

		}//End of try

		catch(Exception e) {
			Log.exception(e, driver);
		}//End of Catch

		finally{
			Utility.quitDriver(driver);
		}//End of Finally

	}//End of TC_084

	/**
	 * TC_085 : Display Mode -> Thumbnails
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Smoke", "Script"}, 
			description = "Display Mode -> Thumbnails")
	public void TC_085(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2 : Change display mode to Details
			//-----------------------------------------
			if (homePage.isListViewDisplayed())
				homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode + ">>" + Caption.MenuItems.Thumbnails);

			Log.message("2. View is changed to 'Thumbnails' display mode.");

			//Verification : To verify if display mode is Details
			//-------------------------------------------------------------------
			if (homePage.listView.isThumbnailsView())
				Log.pass("Test Passed. 'Thumbnails' Display mode is displayed.");
			else
				Log.fail("Test Failed. 'Thumbnails' Display mode is not displayed.", driver);

		}//End of try

		catch(Exception e) {
			Log.exception(e, driver);
		}//End of Catch

		finally{
			Utility.quitDriver(driver);
		}//End of Finally

	}//End of TC_085

	/**
	 * TC_091 : Verify 'Workflow' states for the Workflow selected from context menu of an object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows", "Smoke"}, 
			description = "Verify 'Workflow' states for the Workflow selected from context menu of an object.")
	public void TC_091(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("SearchType"), dataPool.get("Object"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//2. Open the properties of a document
			//-------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("Object")))
				throw new Exception("The specified object '" + dataPool.get("Object") + "' is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the metadata card

			Log.message("2. Metadatacard of the object (" + dataPool.get("Object") + ") is opened.");

			//3. Set the action with state to the object
			//--------------------------------------------
			metadatacard.setWorkflow(dataPool.get("Workflow"));
			ArrayList<String> actualStates = metadatacard.getAvailableStates();

			metadatacard.saveAndClose();

			Log.message("3. Workflow (" + dataPool.get("Workflow") + ") is set to the object in metadatcard.");

			//Verification: To verify if the available states are listed
			//----------------------------------------------------------
			String[] expectedStates = dataPool.get("States").split("\n");
			String addlInfo = "Expected Workflow States - " + expectedStates.length + "; Actual Workflow States - " + actualStates.size();

			if (expectedStates.length != actualStates.size())
				throw new Exception("Test Case Failed. Workflow states are not displayed as expected. Refer additional information : " + addlInfo);

			addlInfo = "";

			for (int count = 0; count < expectedStates.length; count++) 
				if(actualStates.indexOf(expectedStates[count]) < 0)
					addlInfo  = addlInfo + ";";

			if (addlInfo.equalsIgnoreCase(""))
				Log.pass("Test Case Passed. All the Expected States were listed.");
			else
				Log.fail("Test Case Failed. The one or more workflow states are not listed in taskpanel as follows, Not listed workflow states : " + addlInfo, driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}
		finally {
			Utility.quitDriver(driver);
		}

	}	//End TC_091

	/**
	 * TC_092 : Verify 'Workflow' states for the Workflow selected from operations menu of an object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows", "Smoke"},
			description = "Verify 'Workflow' states for the Workflow selected from operations menu of an object.")
	public void TC_092(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("SearchType"), dataPool.get("Object"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//2. Open the properties of a document
			//-------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("Object")))
				throw new Exception("The specified object '" + dataPool.get("Object") + "' is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the metadata card

			Log.message("2. Metadatacard of the object (" + dataPool.get("Object") + ") is opened.");

			//3. Set the action with state to the object
			//--------------------------------------------
			metadatacard.setWorkflow(dataPool.get("Workflow"));
			ArrayList<String> actualStates = metadatacard.getAvailableStates();

			metadatacard.saveAndClose();

			Log.message("3. Workflow (" + dataPool.get("Workflow") + ") is set to the object in metadatcard.");

			//Verification: To verify if the available states are listed
			//----------------------------------------------------------
			String[] expectedStates = dataPool.get("States").split("\n");
			String addlInfo = "Expected Workflow States - " + expectedStates.length + "; Actual Workflow States - " + actualStates.size();

			if (expectedStates.length != actualStates.size())
				throw new Exception("Test Case Failed. Workflow states are not displayed as expected. Refer additional information : " + addlInfo);

			addlInfo = "";

			for (int count = 0; count < expectedStates.length; count++) 
				if(actualStates.indexOf(expectedStates[count]) < 0)
					addlInfo  = addlInfo + ";";

			if (addlInfo.equalsIgnoreCase(""))
				Log.pass("Test Case Passed. All the Expected States were listed.");
			else
				Log.fail("Test Case Failed. The one or more workflow states are not listed in taskpanel as follows, Not listed workflow states : " + addlInfo, driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}	//End TC_092

	/**
	 * TC_093 : Open shared public link obtained from context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIPIE11_MultiDriver", "PublicSharing"}, 
			description = "Open shared public link obtained from context menu")
	public void TC_093(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; WebDriver driver2 = null;

		try {


			driver = WebDriverUtils.getDriver();
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the document and click share links from context menu
			//--------------------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("object"));
			homePage.listView.rightClickItem(dataPool.get("object"));
			homePage.listView.clickContextMenuItem(Caption.MenuItems.SharePublicLink.Value);
			MFilesDialog mfilesDialog = MFilesDialog.isSharePublicLinkDlgOpened(driver);

			Log.message("2. Share public Links dialog is opened from context menu.");

			//Step-3 : Copy public link from public links dialog
			//--------------------------------------------------
			mfilesDialog.clickSharePublicLinkBtn(); //Clicks Share public link button
			String publicLink = mfilesDialog.getPublicLink();

			Log.message("3. Link is copied from Share Public Link dialog.");

			//Step-4 : Open public link in new browser window
			//----------------------------------------------
			driver2 = Utility.openSharedLinkPage(publicLink);

			Log.message("4. Public link URL is opened : '" + publicLink + "'");

			//Verification : Verify if public link has shared object
			//------------------------------------------------------
			SharedLinkPage sharedLinkPage = new SharedLinkPage(driver2);

			if (sharedLinkPage.getSharedDocumentName().equals(dataPool.get("object")))
				Log.pass("Test case Passed. Document is shared with public link created from context menu.");
			else
				Log.fail("Test case failed. Document is not shared with public link created from operations menu.", driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if(driver2 != null)
				driver2.quit();

			Utility.quitDriver(driver);
		} //End Finally

	}	//End TC_093

	/**
	 * TC_094 : Open shared public link obtained from operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIPIE11_MultiDriver", "PublicSharing"}, 
			description = "Open shared public link obtained from operations menu")
	public void TC_094(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; WebDriver driver2 = null;

		try {


			driver = WebDriverUtils.getDriver();
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the document and click share links from context menu
			//--------------------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("object"));
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.SharePublicLink.Value);
			MFilesDialog mfilesDialog = MFilesDialog.isSharePublicLinkDlgOpened(driver);

			Log.message("2. Share public Links dialog is opened from operations menu.");

			//Step-3 : Copy public link from public links dialog
			//--------------------------------------------------
			mfilesDialog.clickSharePublicLinkBtn(); //Clicks Share public link button
			String publicLink = mfilesDialog.getPublicLink();

			Log.message("3. Link is copied from Share Public Link dialog.");

			//Step-4 : Open public link in new browser window
			//----------------------------------------------
			driver2 = Utility.openSharedLinkPage(publicLink);

			Log.message("4. Public link URL is opened : '" + publicLink + "'");

			//Verification : Verify if public link has shared object
			//------------------------------------------------------
			SharedLinkPage sharedLinkPage = new SharedLinkPage(driver2);

			if (sharedLinkPage.getSharedDocumentName().equals(dataPool.get("object")))
				Log.pass("Test case Passed. Document is shared with public link created from context menu.");
			else
				Log.fail("Test case failed. Document is not shared with public link created from operations menu.", driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if(driver2 != null)
				driver2.quit();

			Utility.quitDriver(driver);
		} //End Finally

	}	//End TC_094

	/**
	 * Test_38310 : Verify if user navigate to the virtual search folder.
	 * 
	 * @param dataValues
	 * @param currentDriver
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"bugcases", "Search", "Smoke","Bug"}, 
			description = "Verify if user navigate to the virtual search folder.")
	public void TC_38310(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"),"");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Verification : Verify if user navigate to virtual search folder
			//---------------------------------------------------------------
			if(homePage.listView.isItemExists(dataPool.get("objectType"))&&!MFilesDialog.exists(driver))
				Log.pass("Test Case Passed.User navigate to the virtual search folder successfully.",driver);
			else
				Log.fail("Test Case Failed.User did not navigate to the virtual folder & Mfiles dialog is exists.", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally{

			Utility.quitDriver(driver);
		}//End finally
	}//Test_38310

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"bugcases", "Workflow E-sign", "Smoke","Bug"}, 
			description = "Verify if workflow with E-sign is set in workflow dialog.")
	public void TC_38302(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Logged in MFWA with valid credentials
			//-------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);//Logged in with valid credentials

			//Step-1 : Navigate to any specified view
			//---------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"),dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the any existing object in the specified view
			//-------------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectType"));//Select the object in specified view

			Log.message("2. Selected the existing object :  "+ dataPool.get("ObjectType")+ " from the specified search view.");

			//Step-3 : Set the workflow and state in workflow dialog
			//------------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver,true);//Instantiate the right pane metadatacard

			if(metadatacard.getWorkflow().equals(""))//Verify if object is empty workflow
				throw new Exception("Invalid test data.Object workflow must not be empty,Select the object with workflow.");

			Log.message("3. Object : "+ dataPool.get("ObjectType")+ " which does not have the workflow.");

			//Step-4 : Set the workflow and state for the selected object through task pane
			//-----------------------------------------------------------------------------
			driver.switchTo().defaultContent();
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);//Click the workflow dialog in task pane

			Log.message("4. Selected the 'Workflow' option from the task pane.");

			//Step-5 : Set the workflow and state from the workflow dialog
			//------------------------------------------------------------
			MFilesDialog mFilesDialog = new MFilesDialog(driver);//Instantiate the M-files dialog
			mFilesDialog.setWorkflow(dataPool.get("Workflow"));//Set the workflow in m-files dialog
			mFilesDialog.setWorkflowState(dataPool.get("State"));//Set the workflow state in the m-files dialog
			mFilesDialog.clickOkButton();//Select the OK button 

			Log.message("5. Set the Workflow and state for the m-files dialog.");

			//Verify if E-sign dialog is displayed
			//-------------------------------------------
			if(!MFilesDialog.exists(driver, Caption.MFilesDialog.ElectronicSignature.Value))//Checks if E-sign dialog exists or not
				throw new Exception("E-sign dialog is not displayed.");

			//Step-6 : E-Sign the state transition
			//-------------------------------------
			mFilesDialog.eSign(userName, password, "", "");//Performs E-Sign operation

			Log.message("6. Set the Password and sign button in the mfiles dialog.");

			//Verify if property is exists in metadatacard 
			//--------------------------------------------
			driver.switchTo().defaultContent();
			metadatacard = new MetadataCard(driver,true);//Instantiate the right pane metadatacard
			if(!metadatacard.propertyExists(dataPool.get("PropertyName")))//Verify if property is exist in metadata
				throw new Exception("Property : "+ dataPool.get("PropertyName") +" is exists in the metadatacard.");

			//Verification : Verify if property value is set as expected
			//----------------------------------------------------------
			if(metadatacard.getPropertyValue(dataPool.get("PropertyName")).equals(dataPool.get("PropertyValue")))//verify if property name and value is set as expected
				Log.pass("Test Case Passed.Property name "+ dataPool.get("PropertyName")+ " set with property value " +dataPool.get("PropertyValue") , driver);
			else
				Log.fail("Test Case Failed.Property name "+ dataPool.get("PropertyName")+ " is not set with property value " +dataPool.get("PropertyValue")  , driver);


		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally{
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_38302

	/**
	 * TC_38164 : Verify if user can create the document with enable the open for editing checkbox
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"bugcases", "Workflow E-sign", "Smoke","Bug"}, 
			description = "Verify if user can create the document with enable the open for editing checkbox")
	public void TC_38164(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Logged in MFWA with valid credentials
			//-------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);//Logged in with valid credentials

			//Step-1 : Create the new document in menu bar
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Document.Value);//Create the new document object from the menu bar

			Log.message("1. Selected new 'Document' object from the menu bar.", driver);

			//Step-2 : Select the class template  in the new document
			//-------------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard.setTemplateUsingClass(dataPool.get("Class"));//Set the metadatacard class 

			Log.message("2. Selected the : "+dataPool.get("Class")+" in new metadatacard.");

			//Step-3 : Set the property values in the metadatacard
			//----------------------------------------------------
			metadatacard = new MetadataCard(driver);
			metadatacard.setInfo(dataPool.get("Props"));//Set the metadatacard properties
			metadatacard.setOpenForEditing(true);//Set the open for editing check box
			metadatacard.clickCreateBtn();//Click the create button

			Log.message("3. All the properties are set in the metadatacard & Set the open for editing check box.");

			//Step-4 : Navigate to checkout to me view
			//----------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckedOutToMe.Value);

			Log.message("4. Navigated to the 'Check out to me' view.");

			//Verification : Verify if created document exist in the navigated view
			//---------------------------------------------------------------------
			if(homePage.listView.isItemExists(dataPool.get("ObjectName")))
				Log.pass("Test Case Passed.Object : "+dataPool.get("ObjectName")+" is exists in the Checked out to me view.");
			else
				Log.fail("Test Case Failed.Object : "+dataPool.get("ObjectName")+" is not exists in the Checked out to me view.", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally{
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_38164



	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"bugcases", "Workflow E-sign", "Smoke","Bug"}, 
			description = "Verify if 'Collection members'- properties should not be shown on properties view for 'Document(Collection)' object")
	public void TC_38141(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Logged in MFWA with valid credentials
			//-------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);//Logged in with valid credentials

			//Step-1 : Select the new 'Documet(Collection)' from the menu bar
			//---------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.DocumentCollection.Value);//Click the 'Document(Collection)' object from the menu bar

			Log.message("1. Selected the 'Document(Collection)' from the new menu bar.");

			//Step-2 : Get the available properties in new add property
			//---------------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the metadatacard

			List<String> availableProps = metadatacard.getAvailableAddProperties();//Get the all available properties in metadatacard

			for(int count = 0; count < availableProps.size(); count++) {
				if(availableProps.get(count).equals(dataPool.get("PropertyName")))//Verify if property name is exists in the metadatacard 
					Log.fail("Test Case Failed. Property :'" +dataPool.get("PropertyName")+"'is listed in 'Document(collection)' metadatacard.", driver);
			}

			Log.pass("Test Case Passed.Property : " + dataPool.get("PropertyName") + " is not listed in 'Document(collection)' metadatacard. ");

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally { 
			Utility.quitDriver(driver); 
		}//End finally
	}//End TC_38141


	/**
	 * TC_134332 : Verify if selected MFD name is displayed correctly in breadcrumb
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Smoke","Bug"}, 
			description = "Verify if selected MFD name is displayed correctly in breadcrumb")
	public void TC_134332(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"),dataPool.get("MFDObject"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver); 

			//Step-2 : Select any MFD document
			//--------------------------------
			homePage.listView.doubleClickItem(dataPool.get("MFDObject"));

			Log.message("2. Selected the MFD object : " + dataPool.get("MFDObject") , driver);

			//Verification : Verify if MFD object is displayed as expected
			//------------------------------------------------------------
			if(homePage.menuBar.GetBreadCrumbItem().equals(testVault+">" +dataPool.get("MFDObject")))
				Log.pass("Test Case Passed.Selected MFD object is displayed as expected in Breadcrumb." + testVault+">>" +dataPool.get("MFDObject"), driver);
			else
				Log.fail("Test Case Failed.Selected MFD object is not displayed as expected in Breadcrumb." +homePage.menuBar.GetBreadCrumbItem(), driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally { 
			Utility.quitDriver(driver); 
		}//End finally
	}//End TC_134332

	/**
	 * 56.1.2.1 : Clicking an item in tree view should expand the item in Default layout
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","Sprint56","ExpandAndCollapseNavigationPane"}, 
			description = "Clicking an item in tree view should expand the item in Default layout")
	public void TC_56_1_2_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Default Layout' layout
			//------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. " + Caption.ConfigSettings.Config_Default.Value + " layout is selected and saved.");

			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Enable navigation pane
			//-------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.NavigationPane.Value); //Enables Navigation Pane from operations menu

			if (!homePage.isTreeViewDisplayed()) //Checks if Navigation Pane is displayed
				throw new Exception("Navigation pane is not displayed after enabling it from display mode.");

			Log.message("4. Navigation pane is enabled from display mode.");

			//Step-5 : Single click the item
			//------------------------------
			homePage.treeView.clickTreeViewItem(dataPool.get("ItemToClick"));

			Log.message("5. Item (" + dataPool.get("ItemToClick") + ") is clicked.");

			//Verification : To Verify if item has expanded on clicking the item
			//-------------------------------------------------------------------
			String status = homePage.treeView.getItemStatus(dataPool.get("ItemToClick"));

			if (status.equalsIgnoreCase("OPEN") || status.equalsIgnoreCase("NO CHILD"))
				Log.pass("Test case Passed. On Clicking the item got expanded in navigation pane.");
			else
				Log.fail("Test case Failed. On Clicking the item does not got expanded in navigation pane. ", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End TC_56_1_2_1

	/**
	 * 58.2.30A : Remove one of multiple related objects
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Relationships"}, 
			description = "Remove one of multiple related objects.")
	public void TC_58_2_30A(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object
			//------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")); //Search for an object

			if(!homePage.listView.clickItem(dataPool.get("Object"))) //Click the Object
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not found.");

			Log.message("2. Search for an object.");

			//3. Open the metadatacard
			//-------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Click the properties option

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiate the metadatacard

			Log.message("3. Open the metadatacard");

			//4. Add the relationship
			//------------------------
			String value = metadatacard.getPropertyValue(dataPool.get("Property"));//Gets the property value in the metadatacard
			metadatacard.removePropertyValue(dataPool.get("Property"), 1); //Remove a value from the property
			metadatacard.saveAndClose(); //Click the Save button

			Log.message("4. Removed the relationship.");

			//5. Open the Relationship view of the object
			//--------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Relationships.Value); //Click the Relationships option

			Log.message("5. Open the Relationship view of the object.");

			//Verification: To Verify if the removed object is not related to the object
			//----------------------------------------------------------------------------
			if(!homePage.listView.isItemExists(value))
				Log.pass("Test Case Passed. The relationship was removed as expected.");
			else
				Log.fail("Test Case Passed", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}//End TC_58_2_30A

	/**
	 * ESign_44631 : Verify if esign is successfully while selecting from multiple meanings
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"BasicESign"},
			description = "Verify if esign is successfully while selecting from multiple meanings")
	public void TC_44631(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launches driver and logging in

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), "");//Navigate to the specific view

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))//Selects the object in the view
				throw new Exception("'" + dataPool.get("ObjectName")  + "' is not selected in the view.");

			Log.message("1. Navigated to " +  viewtonavigate  + " and selected the object \"" +  dataPool.get("ObjectName") +  "\" in the view.", driver);

			//Step-2 : Sets the ESign worklfow in the metadata card
			//-----------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadatacard
			metadataCard.setWorkflow(dataPool.get("Workflow"));//Sets the Worklfow in the right pane metadata card
			metadataCard.saveAndClose();//Saves the changes in rightpane metadata card

			Log.message("2. E-Sign Workflow (" + dataPool.get("Workflow") + ") is set and saved the changes in right pane metadata card.", driver);

			//Step-3: Perform E-Sgin required state transition via Task pane
			//--------------------------------------------------------------
			if (!homePage.taskPanel.clickItem(dataPool.get("ESignState")))//Clicks the workflow state in the state transition section in the task panel
				throw new Exception("Required E-Sign Workflow is not set for the selected object in the view.");			

			if (!MFilesDialog.exists(driver, Caption.MenuItems.Workflow.Value))//Checks if workflow state transition dialog is exist or not
				throw new Exception("State transition workflow dialog is not opened while click on the '" + dataPool.get("ESignState") + "' workflow state from task panel.");

			Log.message("3. State transition dialog is opened via task panel for the E-Sign required state(" + dataPool.get("ESignState") + ") transition.", driver);

			//Step-4: Clicks OK button in the State transition workflow dialog
			//----------------------------------------------------------------
			MFilesDialog mfDialog = new MFilesDialog(driver, Caption.MenuItems.Workflow.Value);//Instantiates the Workflow M-Files dialog in the view
			mfDialog.clickOkButton();//Clicks OK button in the Workflow dialog for the E-Sign required State transition

			if (!MFilesDialog.isESignDialogExist(driver))//Checks if Electronic Signature dialog is exists or not
				throw new Exception("Electronic signature dialog is not opened while performing state transition to the state '" + dataPool.get("ESignState") + "'.");

			Log.message("4. OK button is clicked in the State transition dialog for the E-Sign required state trainsition.", driver);

			//Step-5: Get the ESign Message from the E-Sign dialog
			//----------------------------------------------------
			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog
			mfDialog.selectESignReason(dataPool.get("ESignReason"));//Selects the reason in the E-Sign dialog
			String eSignMessage = mfDialog.getESignDialogMessage();//Gets the E-Sign dialog message text

			if (!eSignMessage.equalsIgnoreCase(dataPool.get("ESignMessage")))
				throw new Exception("Expected message('" + dataPool.get("ESignMessage") + "') is not displayed while selecting the reason('" + dataPool.get("ESignReason") + "'). Actual message displayed: '" + eSignMessage + "'");

			Log.message("5. Reason('" + dataPool.get("ESignReason") + "') is selected and the message('"+ dataPool.get("ESignMessage") +"') is displayed in the E-Sign dialog.", driver);

			//Step-6: ES-ign the object
			//--------------------------
			mfDialog.eSign(userName, password, "", userFullName);//E-Signs the state transition

			Log.message("6. Performed E-Sign operation.", driver);

			//Check points
			//------------
			String result = "";

			metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadata card

			//Checks if expected workflow state is set for the object
			//-------------------------------------------------------
			if (!metadataCard.getWorkflowState().equalsIgnoreCase(dataPool.get("ESignState")))//Checks if required state is set in the metadata card
				result += "Object(" + dataPool.get("ObjectName") + ") is not E-Signed successfully. Expected workflow state(" + dataPool.get("ESignState") + ") is not set in the metadatacard(Actual : " + metadataCard.getWorkflowState() + ").";		

			//Checks if Signature property is added in the metadatacard
			//---------------------------------------------------------
			if (!metadataCard.propertyExists(dataPool.get("Property")))//Checks if signature property is added into the metadata card
				result += "Object(" + dataPool.get("ObjectName") + ") is not E-Signed successfully. " + dataPool.get("Property") + " is not exist in the metadata card.";

			//Verification if Object E-Signed correctly
			//-------------------------------------------
			if (result.equals(""))
				Log.pass("Test case passed. E-Sign is successful when selecting reasons from multiple meanings.", driver);
			else
				Log.fail("Test case failed. E-Sign is not successful when selecting reasons from multiple meanings.[Additional info. : " + result + "]", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try{
				MFilesDialog.closeMFilesDialog(driver);//Closes the M-Files dialog in the driver
			}
			catch(Exception e0) {Log.exception(e0, driver);}	

			Utility.quitDriver(driver);
		}//End finally

	}//End TC_44631

	/**
	 * PWorkflow_32152 : Verify if substitute user able to view Esign dialog when approving existing assignment with All must approve class
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			description = "Verify if substitute user able to view Esign dialog when approving existing assignment with All must approve class")
	public void TC_32152(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);//Launch login page and login

			//Step-1 : Cretae Assignment Object
			//---------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);//Opens the new Assignment object metadata card via New menu bar

			String objName = Utility.getObjectName(methodName) +"_Assignment";//Frames the object name

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the new object metadata card
			metadataCard.setInfo(dataPool.get("Properties"));//Sets the required property values in the metadata card
			metadataCard.setPropertyValue("Name or title", objName);//Sets the object name in the metadata card
			metadataCard.saveAndClose();//Creates the object

			Log.message("1. Object '" + objName + "' is created successfully.", driver);

			//Step-2 : Logout from the M-Files Web Access and Login as Substitute user
			//-----------------------------------------------------------------------
			if (!Utility.logoutFromWebAccess(driver))//Logs out from the M-Files Web Access
				throw new Exception("Error while logging out from M-Files Web Access.");

			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("SubstituteUserName"), dataPool.get("SubstituteUserPassword"), testVault);//Login as the Assigned user into the MFWA

			Log.message("2. Logged out from the MFiles Web Access and Logged in as the Substitute user : '" + dataPool.get("SubstituteUser") + "' into M-Files Web Access.", driver);

			//Step-3: Perform Mark approve via metadatacard
			//---------------------------------------------
			if(!homePage.taskPanel.clickItem(Caption.Taskpanel.AssignedToMe.Value))
				throw new Exception("Error while navigating to Assigned To Me view");

			if (!homePage.listView.clickItem(objName))//Clicks the object in the view
				throw new Exception("Object(" + objName + ") not selected in the view.");

			metadataCard = new MetadataCard(driver, true);//Instantiates the new object metadata card in rightpane
			metadataCard.clickApproveIcon(true);//Clicks the approve icon in the metadata card
			metadataCard.saveAndClose();//Saves the changes

			if (!MFilesDialog.exists(driver, "M-Files Web"))
				throw new Exception("Wwarning dialog is not displayed while approving the assignment via metadata card which requires electroinc signature.");

			MFilesDialog mfDialog = new MFilesDialog(driver, "M-Files Web");//Instantiates the M-Files dialog

			if (!mfDialog.getMessage().equalsIgnoreCase(dataPool.get("ExpectedWarningMesssage")))
				throw new Exception("Expected warning message(" + dataPool.get("ExpectedWarningMesssage") + ") is not displayed while approving the assignment which requires electroinc signature via metadata card. [Actual message: '" + mfDialog.getMessage().trim() + "']"); 

			Log.message("3. Warning dialog is displayed as expected while approving the assignment which requires electronic signature via metadata card", driver);

			mfDialog.close();//Closes the M-Files dialog in the view
			metadataCard.cancelAndConfirm();//Discard the changes in the rightpane metadatacard

			//Step-4: Perform Mark approve operation via task pane
			//-----------------------------------------------------
			if (!homePage.listView.clickItem(objName))
				throw new Exception("Object(" + objName + ") is not selected in the view.");

			if (!homePage.taskPanel.clickItem(Caption.MenuItems.MarkApproved.Value))//Clicks the MarkApproved in the task pane
				throw new Exception("' " + Caption.MenuItems.MarkApproved.Value + " ' not clicked from the task pane for the selected object(" + objName + ").");

			Log.message("4.1. Mark Approve is clicked from the task pane for the E-Sign required action.", driver);

			mfDialog = new MFilesDialog(driver, Caption.MenuItems.MarkApproved.Value);//Instantiates the MarkApproved dialog
			mfDialog.clickOkButton();//Clicks the OK button in the MarkApproved dialog

			Log.message("4.2. OK button is clicked in the Mark Approve dialog for the E-Sign required action.", driver);

			if (!MFilesDialog.isESignDialogExist(driver))//Waits and Checks if Electronic Signature dialog is exists or not
				throw new Exception("Electronic signature dialog is not opened while mark approve the assignment(" + objName + ") which requires E-Sign.");

			Log.message("4.3. E-Sign dialog is displayed after clicking OK button in the Mark Approve dialog", driver);

			mfDialog = new MFilesDialog(driver, Caption.MFilesDialog.ElectronicSignature.Value);//Instantiates the Electronic Signature MFiles Dialog
			mfDialog.eSign(dataPool.get("SubstituteUserName"), dataPool.get("SubstituteUserPassword"), "", "");//E-Signs the state transition

			if(!homePage.taskPanel.clickItem(Caption.MenuItems.RecentlyAccessedByMe.Value))
				throw new Exception("Error while navigating to Recently Accessed By Me view");

			if (!homePage.listView.clickItem(objName))//Clicks the object in the view
				throw new Exception("Object(" + objName + ") not selected in the view.");

			metadataCard = new MetadataCard(driver, true);//Instantiates the rightpane metadatacard

			if (!metadataCard.isApprovedSelected(0))
				throw new Exception("Assignment(" + objName + ") is not mark approved by the user : '" + dataPool.get("SubstituteUser") + "'");

			Log.message("4.4. Assignment(" + objName + ") is Mark approved by the user : '" + dataPool.get("SubstituteUser") + "'", driver);

			//Step-5: Check if Signature object is created for the object successfully
			//------------------------------------------------------------------------
			if (!metadataCard.propertyExists(dataPool.get("SignatureProperty")))
				throw new Exception(dataPool.get("SignatureProperty") +" property is not added in the metadata card after Mark approve the object with E-Sign");

			String approvalObjName = dataPool.get("ApprovalAssignment") +": "+ dataPool.get("SubstituteUser");

			if (!metadataCard.getPropertyValue(dataPool.get("SignatureProperty")).contains(approvalObjName))
				throw new Exception("Approval assignment with name(" + approvalObjName + ") is not added in the signature property. [Actual value: '" + metadataCard.getPropertyValue(dataPool.get("SignatureProperty")) + "']");

			approvalObjName = metadataCard.getPropertyValue(dataPool.get("SignatureProperty"));

			Log.message("5. Object(" + objName + ") is mark approved and signature object property is added with expected value in the metadata card.", driver);

			//Verification: Check if signature object is created with correct signature reason and signature meaning
			//------------------------------------------------------------------------------------------------------
			String result = "";

			//Navigate to the Relationship view
			//---------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Relationships.Value);//Navigates to the relationship view of the object

			//Select the object in the view
			//------------------------------
			if (!homePage.listView.clickItem(approvalObjName))//Clicks the object in the view
				throw new Exception("Signature Object(" + approvalObjName + ") not selected in the view.");

			//Open the popout metadata card via task pane
			//--------------------------------------------
			if (!homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value))//Clicks the option in the task pane
				throw new Exception("Properties is not clicked from the task pane for the selected object.");

			//Check if Signature reason is set as expected in the metadata card
			//-----------------------------------------------------------------
			metadataCard = new MetadataCard(driver);//Instantiates the metadata card

			if (!metadataCard.getPropertyValue(dataPool.get("SignatureReasonProperty")).equalsIgnoreCase(dataPool.get("SignatureReasonPropertyValue")))
				result = "Expected signature reason(" + dataPool.get("SignatureReasonPropertyValue") + ") is not displayed in the signature object.[Actual value: '" + metadataCard.getPropertyValue(dataPool.get("SignatureReasonProperty")) + "']. ";

			//Check if Signature meaning is set as expected in the metadata card
			//-----------------------------------------------------------------
			if (!metadataCard.getPropertyValue(dataPool.get("SignatureMeaningProperty")).equalsIgnoreCase(dataPool.get("SignatureMeaningPropertyValue")))
				result += " Expected signature meaning(" + dataPool.get("SignatureMeaningPropertyValue") + ") is not displayed in the signature object.[Actual value: '" + metadataCard.getPropertyValue(dataPool.get("SignatureMeaningProperty")) + "']. ";

			//Check if Signature identifier is set as expected in the metadata card
			//-----------------------------------------------------------------
			if (!metadataCard.getPropertyValue(dataPool.get("SignatureIdentifierProperty")).equalsIgnoreCase(dataPool.get("SignatureIdentifierPropertyValue")))
				result += " Expected signature identifier(" + dataPool.get("SignatureIdentifierPropertyValue") + ") is not displayed in the signature object.[Actual value: '" + metadataCard.getPropertyValue(dataPool.get("SignatureIdentifierProperty")) + "']. ";

			//Verification: If substitute user able to view Esign dialog when approving existing assignment with All must approve class
			//-------------------------------------------------------------------------------------------------------------------------
			if (result.equals(""))
				Log.pass("Test case passed. Substitute user able to view Esign dialog when approving existing assignment with All must approve class and Signature object is created with expected values while perform mark approve action.", driver);
			else
				Log.fail("Test case failed. Signature object is created with expected values while perform mark approve action. Additional info. : "+ result.trim(), driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try{
				MFilesDialog.closeMFilesDialog(driver);//Closes the M-Files dialog in the driver
			}
			catch(Exception e0) {Log.exception(e0, driver);}	

			Utility.quitDriver(driver);
		}//End finally

	}//End TC_32152

	/**
	 * 105.13.3A : Description should be  available on clicking the properties while creating a new object [eg:customer]-through taskpanel.
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader" , groups = {"Sprint105", "MetadataConfigurability", "PropertyDescription"}, description = "Description should be  available on clicking the properties while creating a new object [eg:customer]-through taskpanel.")
	public void TC_105_13_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Step-1 : Login to MFiles web access
			//-----------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-2 : Click the new 'Customer' object from the Taskpanel
			//-----------------------------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));

			Log.message("1. Click the '" + dataPool.get("ObjectType") + "' object from the Taskpanel.");

			//Step-3 : Click the Customer property in Metadata card
			//-----------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); //Instantiate the metadata card
			metadataCard.setPropertyValue(dataPool.get("Class"), dataPool.get("ClassValue"));//Sets the class value in the metadata card
			metadataCard.addNewProperty(dataPool.get("Property"));//Adds the property in the metadata card
			metadataCard.savePropValue(dataPool.get("Property"));//Clicks the property in the metadata card

			Log.message("2. Class '" + dataPool.get("ClassValue") + "' is selected and Property : '" + dataPool.get("Property") + "' is clicked in metadata card.");
			//Verification : Verify if Description is available in 'Customer name' field
			//--------------------------------------------------------------------------
			String description = metadataCard.getPropertyDescriptionValue(dataPool.get("Property"));
			if (description.equals(dataPool.get("PropertyDescriptionValue")))
				Log.pass("Test Case Passed. Description '" + dataPool.get("PropertyDescriptionValue") + "' is available for '" + dataPool.get("Property") + "' property.");
			else
				Log.fail("Test Case failed. Description '" + dataPool.get("PropertyDescriptionValue") + "' is not available for '" + dataPool.get("Property") + "' property.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End TC_105_13_3A

	/**
	 * 1.4.3B : Hierarchical rules, more than one rule in same level [Existing object] 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"MetadataConfigurability"},description = "Verify the Metadata card functionality with Hierarchical rules, more than one rule in same level [Existing object] " )
	public void TC_1_4_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in
			String ExpectedMetadata = "";

			//Step-1 : Navigate to specified view
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to : " + viewToNavigate + " view.");

			//Step-2 : Select any existing object
			//------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			homePage.previewPane.clickMetadataTab();//Clicks the Metadata tab in the right pane

			Log.message("2. Selected the specified object : " + dataPool.get("Object") + " in list view.");

			//Step-2.1: Cheks the first level behavior in the metadatacard
			//------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (metadataCard.propertyExists(dataPool.get("Property1")))
				ExpectedMetadata += "Property("+ dataPool.get("Property1") + ") is exists in the metadatacard for the first level hierarchy.";

			//Step-3: Sets the level 1.1 hierarchy behavior in the metadatacard
			//--------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("PropName1"), dataPool.get("PropName1Value"));//Sets the class in the metadatacard

			if (!metadataCard.getPropertyValue(dataPool.get("PropName1")).equalsIgnoreCase(dataPool.get("PropName1Value")))
				throw new Exception("Property("+dataPool.get("PropName1")+") is not set with value("+dataPool.get("PropName1Value")+") in the metadatacard for the level 1.1 hierarchy behavior");

			Log.message("3. Property("+dataPool.get("PropName1")+") is set with value("+dataPool.get("PropName1Value")+") in the metadatacard for the level 1.1 hierarchy behavior", driver);

			//Step-3.1: Checks the level 1.1 behavior in the metadatacard
			//------------------------------------------------------------
			if (!metadataCard.propertyExists(dataPool.get("Property1")))
				ExpectedMetadata += " Property("+ dataPool.get("Property1") + ") is not exists in the metadatacard for the 1.1 level hierarchy.";

			//Step-3.2: Checks the level 1.1 behavior in the metadatacard
			//------------------------------------------------------------
			if (!metadataCard.isRequiredProperty(dataPool.get("Property2")))
				ExpectedMetadata += " Property("+ dataPool.get("Property2") + ") is not a required property in the metadatacard for the 1.1 level hierarchy.";

			//Step-4 : Sets the level 1.1.1 hierarchy rule behavior in the metadatacard
			//--------------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("PropName2"), dataPool.get("PropName2Value1"));

			driver.switchTo().defaultContent();

			if (MFilesDialog.exists(driver, Caption.MFilesDialog.ConfirmAutoFill.Value)) {//Checks the autofill dialog is displayed in the view
				MFilesDialog mfilesDialog = new MFilesDialog(driver);
				mfilesDialog.clickCancelButton();//Clicks the cancel button in the MFilesDialog
			}

			metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (!metadataCard.getPropertyValue(dataPool.get("PropName2")).equalsIgnoreCase(dataPool.get("PropName2Value1")))
				throw new Exception("Property("+dataPool.get("PropName2")+") is not set with value("+dataPool.get("PropName2Value1")+") in the metadatacard for level 1.1.1 Hierarchy rule configuration");

			Log.message("4. Property("+dataPool.get("PropName2")+") is set with value("+dataPool.get("PropName2Value1")+") in the metadatacard for level 1.1.1 Hierarchy rule configuration", driver);

			//Step-4.1: Checks the level 1.1 behavior in the metadatacard
			//------------------------------------------------------------
			if (metadataCard.propertyExists(dataPool.get("Property1")))
				ExpectedMetadata += " Property("+ dataPool.get("Property1") + ") is exists in the metadatacard for the 1.1.1 level hierarchy.";

			//Step-5 : Sets the level 1.1.2 hierarchy rule behavior in the metadatacard
			//--------------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("PropName2"), dataPool.get("PropName2Value2"));

			driver.switchTo().defaultContent();

			if (MFilesDialog.exists(driver, Caption.MFilesDialog.ConfirmAutoFill.Value)) {//Checks the autofill dialog is displayed in the view
				MFilesDialog mfilesDialog = new MFilesDialog(driver);
				mfilesDialog.clickCancelButton();//Clicks the cancel button in the MFilesDialog
			}

			metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (!metadataCard.getPropertyValue(dataPool.get("PropName2")).equalsIgnoreCase(dataPool.get("PropName2Value2")))
				throw new Exception("Property("+dataPool.get("PropName2")+") is not set with value("+dataPool.get("PropName2Value2")+") in the metadatacard for level 1.1.2 Hierarchy rule configuration");

			Log.message("5. Property("+dataPool.get("PropName2")+") is set with value("+dataPool.get("PropName2Value2")+") in the metadatacard for level 1.1.2 Hierarchy rule configuration");

			//Step-5.1: Checks the level 1.1.2 behavior in the metadatacard
			//------------------------------------------------------------
			if (metadataCard.isRequiredProperty(dataPool.get("Property2")))
				ExpectedMetadata += " Property("+ dataPool.get("Property2") + ") is set as required property in the metadatacard for the 1.1.2 level hierarchy.";

			//Verifies if hierarchy rules applied correctly in the metadatacard
			//-----------------------------------------------------------------
			if (ExpectedMetadata.equals(""))
				Log.pass("Test Case Passed. Metadata card functionality with Hierarchical rules, more than one rule in same level [Existing object] is working as expected", driver);
			else
				Log.fail("Test Case Failed. Metadata card functionality with Hierarchical rules, more than one rule in same level [Existing object] is not working as expected[For more details: " + ExpectedMetadata + "]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End 	TC_1_4_3B

	/**
	 * 1.4.8.1.1C : Verify if Property is visible in metadata card when isAdditional:true/false & isHidden:false/true is used. [Object creation via Object property]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MetadataConfigurability"}, 
			description = "Verify if Property is visible in metadata card when isAdditional:true/false & isHidden:false/true is used. [Object creation via Object property]")
	public void TC_1_4_8_1_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to specified view
			//-----------------------------------
			SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");//Navigates to the specific view

			if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard of existing object

			metadataCard.clickAddValueButton(dataPool.get("ObjectType"));//Clicks the Add value button to open the new customer object metadatacard

			Log.message("1. Opened the new object(" + dataPool.get("ObjectType") +  ") metadatacard via object type property");


			//Step-2: Sets the configuration rule in the metadatacard
			//--------------------------------------------------------------------
			metadataCard = new MetadataCard(driver);//Instantiates the metadata card

			metadataCard.setInfo(dataPool.get("Properties"));//Sets the property value in the metadatacard

			String[] properties = dataPool.get("Properties").split("::");

			if (!metadataCard.getPropertyValue(properties[0]).equalsIgnoreCase(properties[1]))
				throw new Exception("Property("+properties[0]+") is not set with value("+properties[1]+" for the configuration rule in the metadatacard");

			Log.message("2. Property("+properties[0]+") is set with value("+properties[1]+" for the configuration rule in the metadatacard", driver);

			//Checks the configuration behavior in the metadatacard
			//-----------------------------------------------------
			metadataCard = new MetadataCard(driver);

			String ExpectedMetadata = "";

			//Checks if properties are exisit in the metadatacard [Outside the group]
			if (!metadataCard.propertyExists(dataPool.get("Property1")))
				ExpectedMetadata += " Property(" + dataPool.get("Property1") + ") is not added in the metadatacard while using the behavior isAdditional:true & isHidden:false [Outside the group].";

			if (metadataCard.propertyExists(dataPool.get("Property2")))
				ExpectedMetadata += " Property(" + dataPool.get("Property2") + ") is displayed in the metadatacard while using the behavior isAdditional:true & isHidden:true [Outside the group].";

			//Checks if property group is exisit in the metadatacard [Inside the group]
			if (!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("GroupText")))//Checks if proeprty group is displayed in the metadatacard
				throw new Exception("Property Group is not displayed in the metadatacard.");

			metadataCard.expandPropertyGroup(1, true);//Expands the property group in the metadatacard

			//Checks if properties are exisit in the metadatacard [Inside the group]
			if (!metadataCard.propertyExistsInGroup(1, dataPool.get("GrpProperty1")))
				ExpectedMetadata += " Property(" + dataPool.get("GrpProperty2") + ") is not added in the metadatacard while using the behavior isAdditional:true & isHidden:false [Inside the group].";

			if (metadataCard.propertyExistsInGroup(1, dataPool.get("GrpProperty2")))
				ExpectedMetadata += " Property(" + dataPool.get("GrpProperty2") + ") is displayed in the metadatacard while using the behavior isAdditional:true & isHidden:true [Inside the group].";

			if (ExpectedMetadata.equalsIgnoreCase(""))
				Log.pass("Test case passed. Metadata configurability is working as expected when isAdditional:true/false & isHidden:false/true is used. [Object creation via Object property]", driver);
			else
				Log.fail("Test case failed. Metadata configurability is not working as expected when isAdditional:true/false & isHidden:false/true is used. [Object creation via Object property]. FOr more details: "+ ExpectedMetadata, driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_8_1_1C

	/**
	 * 1.4.4.2B : Rule contains several conditions (classId, objectType and properties) [Object modification in existing object via pop out metadatacard] 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"MetadataConfigurability"},description = "Verify the Metadata card functionality with several conditions (classId, objectType and properties) while Object modification in existing object popped out metadatacard" )
	public void TC_1_4_4_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Open the new object metadatacard via object property
			//-----------------------------------
			SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			homePage.menuBar.ClickOperationsMenu("Properties");//Opens the popped out metadatacard

			Log.message("1. First condition (ObjectType) is set[Existing object " + dataPool.get("Object") + " is selected and popped out metadatacard of that object is opened in the view]");

			//Step-2: Selects the class in the metadatacard
			//---------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard of existing object

			metadataCard.setPropertyValue("Class", dataPool.get("ClassName"));// Sets the second condition in the metadatacard

			Log.message("2. Second condition (Class) is set[" + dataPool.get("ClassName") + " is set in the metadatacard]");

			//Step-3: Sets the necessary properties in the metadatacard for third condition
			//-----------------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Property1"), dataPool.get("Property1Value"));// Adds & sets the Property1 in the metadatacard

			if (!metadataCard.getPropertyValue(dataPool.get("Property1")).equalsIgnoreCase(dataPool.get("Property1Value")))
				throw new Exception("Property("+ dataPool.get("Property1")+ ") is not set with value("+ dataPool.get("Property1Value")+") in the metadatacard for third condition");

			metadataCard.setPropertyValue(dataPool.get("Property2"), dataPool.get("Property2Value"));// Adds & sets the Property1 in the metadatacard

			if (!metadataCard.getPropertyValue(dataPool.get("Property2")).equalsIgnoreCase(dataPool.get("Property2Value")))
				throw new Exception("Property("+ dataPool.get("Property2")+ ") is not set with value("+ dataPool.get("Property2Value")+") in the metadatacard for third condition");

			Log.message("3. Third condition (Properties) is set[Properties with their required values is set in the metadatacard]");
			Log.message("3.1. Property("+ dataPool.get("Property1")+ ") is set with value("+ dataPool.get("Property1Value")+") in the metadatacard");
			Log.message("3.2. Property("+ dataPool.get("Property2")+ ") is set with value("+ dataPool.get("Property2Value")+") in the metadatacard");

			//Verifies the metadatacard configurations applied in the metadatacard
			//-----------------------------------------------------------------------

			String ExpectedMetadata = "";

			String ActualVaule = dataPool.get("MetadataDesc").trim().replaceAll("\n", "").replaceAll("\r", "");
			ActualVaule = ActualVaule.replaceAll(" ", "");

			if (!metadataCard.getMetadataDescriptionText().equalsIgnoreCase(ActualVaule))
				ExpectedMetadata = "Expected Metadatacard Description("+ dataPool.get("MetadataDesc") +") is not in the metadatacard. [Actual value:"+ metadataCard.getMetadataDescriptionText() +"]";

			if (!dataPool.get("HeaderEditColor").toUpperCase().contains(metadataCard.getHeaderColor().toUpperCase()))
				ExpectedMetadata += " Expected header color("+ dataPool.get("HeaderEditColor") +") is not displayed in the metadatacard in Edit mode. [Actual Value:"+ metadataCard.getHeaderColor() +"]"; 

			if (ExpectedMetadata.equals(""))
				Log.pass("Test case Passed. Metadata card functionality with several conditions (classId, objectType and properties) is working as expected while Object modification in existing object poppoed out metadatacard", driver);
			else	
				Log.fail("Test case Failed. Metadata card functionality with several conditions (classId, objectType and properties) is not working as expected while Object modification in existing object popped out metadatacard. For more details:["+ ExpectedMetadata +"]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End 	TC_1_4_4_2B

	/*
	 * 105.12.3.1A: Verify if default value is displayed while adding the defined properties based on the class configuration in metadatacard using task pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint105", "MetadataConfigurability"}, 
			description = "Default value should be set for the defined property of the defined class while adding the defined property in new metadatacard using task pane")
	public void TC_105_12_3_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1:- Click on New object link from task pane
			//---------------------------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));//Clicks the new object link from taskpane

			Log.message("1. New "+ dataPool.get("ObjectType") + " object link is clicked from task pane", driver);

			//Step-2:- Select the template in the template selector
			//-----------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate Metadatacard
			String property= dataPool.get("Properties");
			String className= property.split("::")[1];

			metadatacard = new MetadataCard(driver);
			metadatacard.setPropertyValue(property.split("::")[0], property.split("::")[1]);//Sets the class value in the metadatacard

			if (!metadatacard.getPropertyValue("Class").equalsIgnoreCase(className))//Checks the correct class is selected
				throw new Exception("Defined class is not selected");

			metadatacard.addNewProperty(dataPool.get("Property"));//Adds the defined property in the metadatacard

			if (!metadatacard.propertyExists(dataPool.get("Property")))// Checks the defined property is added to the metadatacard
				throw new Exception("Defined property is not added");

			Log.message("2. Defined Class (" + className + ") is selected and Defined Property (" + dataPool.get("Property") + ")  is added in the metadata card", driver);

			//Verification: Verify if default value is displayed for the added defined property in the metadatacard
			//------------------------------------------------------------------------------------------------
			if (metadatacard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("ExpectedValue")))//Compares the expected value with property value in metadatacard
				Log.pass("Test case Passed. Expected default value (" +dataPool.get("ExpectedValue") + ") is displayed in the added defined(" +dataPool.get("Property") + ") property for the defined(" + className + ") class in the metadatacard ", driver);
			else
				Log.fail("Test case Failed. Expected default value (" +dataPool.get("ExpectedValue") + ") is not displayed in the added defined(" +dataPool.get("Property") + ") property for the defined(" + className + ") class in the metadatacard ", driver);

		}//End Try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End Catch
		finally {
			Utility.quitDriver(driver);
		}//End Finally

	}// End TC_105_12_3_1A

	/**
	 * 1.4.5.2A: Verify if Metadata configurability is working as expected when using priority & after configuration in the metadatacard [Existing object]
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MetadataConfigurability", "Priority&After"}, 
			description = "Verify if Metadata configurability is working as expected when using priority & after configuration in the metadatacard [Existing object]")
	public void TC_1_4_5_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Navigate to specified view
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to : " + viewToNavigate + " view.");

			//Step-2 : Select any existing object
			//------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			Log.message("2. Selected the specified object : " + dataPool.get("Object") + " in list view.");

			//Step-3: Checks the metadatacard properties order outside the group
			//------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the new object metadata card

			ArrayList<String> actualProperties = new ArrayList<String>(); 
			String[] expectedProperties = null;
			int ExpectedPropertyOrder = 1;
			String ExpectedMetadata = "";

			if (!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("GroupText")))//Checks if proeprty group is displayed in the metadatacard
				throw new Exception("Property Group is not displayed in the metadatacard.");

			metadataCard.expandPropertyGroup(1, true);//Expands the property group in the metadatacard

			//Checks if properties are exisit in the metadatacard [Inside the group]
			if (!metadataCard.propertyExistsInGroup(1, dataPool.get("GrpProperty1")))
				ExpectedMetadata += " Property(" + dataPool.get("GrpProperty1") + ") is not added in the metadatacard. ";

			if (!metadataCard.propertyExistsInGroup(1, dataPool.get("GrpProperty2")))
				ExpectedMetadata += " Property(" + dataPool.get("GrpProperty2") + ") is displayed in the metadatacard. ";

			actualProperties = metadataCard.getMetadatacardProperties();//Gets the actual properties displayed in the metadatacard

			expectedProperties = dataPool.get("Properties").split(",");//Gets the expected properties order from the test data

			for (int i=0; i < actualProperties.size(); i++){
				if (!actualProperties.get(i).trim().equalsIgnoreCase(expectedProperties[i].trim()))//Checks the property in correct order
					ExpectedPropertyOrder = 0;
			}

			//Verification if properties are listed as per the priority order in the metadatacard
			//-----------------------------------------------------------------------------------
			if (ExpectedPropertyOrder != 0 & ExpectedMetadata.equalsIgnoreCase(""))//Checks if properties in expected order
				Log.pass("Test case passed. Properites in the expected order as per the priority in the existing object metadatacard", driver);
			else
				Log.fail("Test case failed. Properties are not listed as per the priority order in the existing object metadatacard", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_4_5_2A

	/**
	 * TC.1.5.1.1A : Verify if all properties without the group are added to default group. [Object creation via menu bar].
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MetadataConfigurability", "isDefault"}, 
			description = "Verify if all properties without the group are added to default group. [Object creation via menu bar].")
	public void TC_1_5_1_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged with valid credentials

			//Step-1 : Selected the Employee object type from the menu bar
			//------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Employee.Value);//Selected the employee object in menu bar

			Log.message("1. Selected 'Employee' object from the menu bar.", driver);

			//Step-2 : Verify if groups text is set as expected
			//-------------------------------------------------
			String groupValue = " ";//Initiate the group value

			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the metadatacard
			String groupText1 = metadatacard.getPropertyGroupText(1);//get the group text value

			//Verify if group1 text is set as expected
			//----------------------------------------
			if(!groupText1.equals(dataPool.get("grouptext1")))//Verify if group text is set as expected
				groupValue = "Group text is not set as expected.";

			String groupText2 = metadatacard.getPropertyGroupText(2);//get the group text value

			//Verify if group2 text is set as expected
			//----------------------------------------
			if(!groupText2.equals(dataPool.get("grouptext2")))//Verify if group text is set as expected
				groupValue = groupValue + "Group text is not set as expected.";

			//verify if property is exists in the metadatacard default group
			//--------------------------------------------------------------
			ArrayList<String> properties = new ArrayList<String>();
			properties = metadatacard.getMetadatacardProperties();//get the properties from the metadatacard
			for (int i=0;i < properties.size(); i++) {
				if(!metadatacard.propertyExistsInGroup(2,properties.get(i)))//Verify if property if property is exists or not 
					groupValue = groupValue+ "Existing property : "+ properties.get(i) + " does not exists in the group2.";
			}
			//Step-2 : Added the new property in the metadatacard
			//---------------------------------------------------
			metadatacard.addNewProperty(dataPool.get("PropertyName"));

			Log.message("2. Added the Property : " + dataPool.get("PropertyName") + " in the metadatacard.");

			//Verify if property is exists in the specified group or not
			//----------------------------------------------------------
			if(!metadatacard.propertyExistsInGroup(1, dataPool.get("PropertyName")))
				groupValue = groupValue + "Property : " + dataPool.get("PropertyName") + " does not exist in the specified group.";

			//Verification : Verify if all properties are set as expected in the default group 
			//--------------------------------------------------------------------------------
			if(groupValue.equals(" "))//Verify if group value is equals to empty
				Log.pass("Test Case Passed.All properties are inside default-group(group1) excluding the property " + dataPool.get("PropertyName") + "which was added to non-default group(group2).");
			else
				Log.fail("Test Case Failed.All properties are not inside the default-group."+ groupValue, driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC.1.5.1.1A

	/** 
	 * 1.5.4.1A : Verify the behavior of metadata configurability for property grouping with isCollapsible & isCollapsedByDefault attributes. [Object creation - Group has only "title" definition.] 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"MetadataConfigurability","Property Grouping"},description = "Verify the behavior of metadata configurability for property grouping with isCollapsible & isCollapsedByDefault attributes. [Object creation - Group has only title definition.]" )
	public void TC_1_5_4_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();
			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Open the new object metadatacard
			//----------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));//Clicks the new object link from task pane

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.setTemplate(dataPool.get("Template"));//Selects the template in the template selector

			String[] classValue= dataPool.get("Properties").split("::");//Gets the property and property value from test data

			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard
			metadataCard.setPropertyValue(classValue[0], classValue[1]);//Sets the property value in the metadatacard

			Log.message("1. " + dataPool.get("ObjectType") + " object type with class("+classValue[1]+") metadatacard is opened via New item in task pane");

			//Step-2: Checks the property group with header text is displayed in the metadatacard
			//----------------------------------------------------------------------------------------
			String ExpectedPropertyGroup = "";

			if(!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("GroupHeaderText")))
				throw new Exception("Test case failed. Property group with header text("+dataPool.get("GroupHeaderText")+") is not displayed in the metadatacard");

			Log.message("2. Property group with header text("+dataPool.get("GroupHeaderText")+") is displayed in the metadatcard", driver);

			//Step-3: Checks the property group behavior in the metadatacard
			//---------------------------------------------------------------------------------
			if(metadataCard.isPropertyGroupCollapsed(1))//Checks if group is expanded successfully in the metadatacard
				ExpectedPropertyGroup += " Property group is not expanded by default when isCollapsible & isCollapsedByDefault is not used.";

			if (!metadataCard.propertyExistsInGroup(1, dataPool.get("Property")))
				ExpectedPropertyGroup += " Property("+ dataPool.get("Property")+") is not displayed in the group.";

			metadataCard.expandPropertyGroup(1, false);//Collapse the property group in the metadatcard

			if(!metadataCard.isPropertyGroupCollapsed(1))//Checks if group is collapsed in the metadatacard 
				ExpectedPropertyGroup += " Property group is not collapsed while perform the click action in the header.";


			//Verifies if metadatacard is displayed with expected property group behavior in the metadatacard
			//-----------------------------------------------------------------------------------------------
			if (ExpectedPropertyGroup.equalsIgnoreCase(""))
				Log.pass("Test case passed. Metadata configurability is working as expected without isCollapsible & isCollapsedByDefault attributes.[Object creation - Group has only title definition.]", driver);
			else
				Log.fail("Test case failed. Metadata configurability is not working as expected without isCollapsible & isCollapsedByDefault attributes [Object creation - Group has only title definition.].[For more details:"+ExpectedPropertyGroup+"]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_5_4_1A

	/**
	 * 1.2.3.1A : Metadata card functionality with two layer configuration hierarchy while creating new object via task pane 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"MetadataConfigurability"},description = "Verify the Metadata card functionality with two layer configuration hierarchy rules while creating new object via task pane" )
	public void TC_1_2_3_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Click the new object link from task pane
			//---------------------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));//Clicks the new object link from task pane

			Log.message("1. " + dataPool.get("ObjectType") + " is clicked from New item in task pane");

			//Step-2: Verifies the First layer configurations in the metadatacard
			//--------------------------------------------------------------------
			String ExpectedMetadata = "";

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadata card
			metadataCard.addNewProperty(dataPool.get("Property"));//Adds the property in the metadata card

			if (!metadataCard.getImageSize().equalsIgnoreCase(dataPool.get("Layer1ImageSize")))//Checks the image size sets in the metadatacard description
				ExpectedMetadata += "Expected metadatacard description image size[" + dataPool.get("Layer1ImageSize") + "] is not set for Layer1 configuration in the metadatacard.[Actual image size:" + metadataCard.getImageSize() + "]";

			String ActualVaule = dataPool.get("MetadataDescLayer1").trim().replaceAll("\n", "").replaceAll("\r", "");
			ActualVaule = ActualVaule.replaceAll(" ", "");

			if (!metadataCard.getMetadataDescriptionText().trim().equalsIgnoreCase(ActualVaule.trim()))//Checks the metadatacard description for the layer1 configuration
				ExpectedMetadata += "Expected metadatacard description[" + dataPool.get("MetadataDescLayer1") + "] is not set for Layer1 configuration in the metadatacard.[Actual Metadatacard Description:" + metadataCard.getMetadataDescriptionText() + "]";

			if (!metadataCard.getPropertyDescriptionValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyDescLayer1")))//Checks the property description for layer1 configuration
				ExpectedMetadata += "Expected property description[" + dataPool.get("PropertyDescLayer1") + "] is not set for Layer1 configuration in the metadatacard.[Actual property Description:" + metadataCard.getPropertyDescriptionValue(dataPool.get("Property")) + "]";

			if (!metadataCard.getPropertyGroupText(1).equalsIgnoreCase(dataPool.get("Layer1GroupTitle")))//Checks the property group title for layer1 configuration
				ExpectedMetadata += "Expected group title[" + dataPool.get("Layer1GroupTitle") + "] is not set for Layer1 configuration in the metadatacard.[Actual Group title:" + metadataCard.getPropertyGroupText(1) + "]";

			if (!ExpectedMetadata.equals(""))//Checks the layer1 configuration is set properly
				throw new Exception("Layer1 configuration rule is not set as expected. For more details: "+ ExpectedMetadata);

			Log.message("2. Layer1 rule is set as expected in the new object metadatacard");

			//Step-3: Set the Layer2 rule configuration in the metadata card
			//--------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Layer2Prop"), dataPool.get("Layer2PropValue"));//Sets the required condition for Layer2 configuration

			if (!metadataCard.getPropertyValue(dataPool.get("Layer2Prop")).equalsIgnoreCase(dataPool.get("Layer2PropValue")))//Checks if the required value is set
				throw new Exception("Property( "+ dataPool.get("Layer2Prop") + " ) is not set with the value( "+ dataPool.get("Layer2PropValue") + " ) in the metadatacard");

			Log.message("3. Property( "+ dataPool.get("Layer2Prop") + " ) is set with the value( "+ dataPool.get("Layer2PropValue") + " ) to apply layer2 configuration in the metadatacard");

			//Step-4: Verifies the Second layer configurations in the metadatacard
			//--------------------------------------------------------------------
			ExpectedMetadata = "";

			if (!metadataCard.getImageSize().equalsIgnoreCase(dataPool.get("Layer2ImageSize")))//Checks the image size sets in the metadatacard description
				ExpectedMetadata += "Expected metadatacard description image size[" + dataPool.get("Layer2ImageSize") + "] is not set for Layer2 configuration in the metadatacard.[Actual image size:" + metadataCard.getImageSize() + "]";

			String ActualVaule1 = dataPool.get("MetadataDescLayer2").trim().replaceAll("\n", "").replaceAll("\r", "");
			ActualVaule1 = ActualVaule1.replaceAll(" ", "");

			if (!metadataCard.getMetadataDescriptionText().trim().equalsIgnoreCase(ActualVaule1.trim()))//Checks the metadatacard description for layer2 configuration
				ExpectedMetadata += "Expected metadatacard description[" + dataPool.get("MetadataDescLayer2") + "] is not set for Layer2 configuration in the metadatacard.[Actual Metadatacard Description:" + metadataCard.getMetadataDescriptionText() + "]";

			if (!metadataCard.getPropertyDescriptionValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyDescLayer2")))//Checks the property description for layer2 configuration
				ExpectedMetadata += "Expected property description[" + dataPool.get("PropertyDescLayer2") + "] is not set for Layer2 configuration in the metadatacard.[Actual property Description:" + metadataCard.getPropertyDescriptionValue(dataPool.get("Property")) + "]";

			if (!metadataCard.getPropertyGroupText(1).equalsIgnoreCase(dataPool.get("Layer2GroupTitle")))//Checks the property group title for layer2 configuration
				ExpectedMetadata += "Expected group title[" + dataPool.get("Layer2GroupTitle") + "] is not set for Layer2 configuration in the metadatacard.[Actual Group title:" + metadataCard.getPropertyGroupText(1) + "]";


			if (ExpectedMetadata.equals(""))//Checks the layer2 configuration is set correctly
				Log.pass("Test case Passed. Metadata card functionality with two layer configuration hierarchy is working as expected while creating new object via task pane", driver);
			else
				Log.fail("Test case failed. Layer2 rule is not set as expected in the new object metadatacard. [" + ExpectedMetadata + "]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_3_1A

	/**
	 * TC_142553 : Default workflow-For a new object, Check user able to change state when 2 states possible from noState. 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"Workflow"},description = "Default workflow-For a new object, Check user able to change state when 2 states possible from noState." )
	public void TC_142553(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; //Webdriver instance

		try {

			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Click the new object link from task pane
			//---------------------------------------------------
			if(!homePage.taskPanel.clickItem(dataPool.get("ObjectType")))//Clicks the new object link from task pane
				throw new Exception("'" + dataPool.get("ObjectType") + "' is not selected from the task pane.");

			Log.message("1. " + dataPool.get("ObjectType") + " is clicked from New item in task pane");

			//Step-2: Select the template in the template selector
			//----------------------------------------------------
			if(!Utility.selectTemplate(dataPool.get("Template"), driver))
				throw new Exception("'" + dataPool.get("Template") + "' is not selected from the metadata card");

			Log.message("2. Template is selected in the metadata card");

			//Step-3: Set workflow and perform state transition
			//-------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard
			metadataCard.setPropertyValue("Name or title", Utility.getObjectName(methodName));//Sets the object name
			metadataCard.setCheckInImmediately(true);//Check in the object immediately

			if(!metadataCard.getWorkflow().equalsIgnoreCase(dataPool.get("Workflow")))
				throw new Exception("Workflow '" + dataPool.get("Workflow") + "' is not set in the metadatacard.");

			Log.message("3. Default Workflow '" + dataPool.get("Workflow") + "' is present as expected in the metadatacard.");

			//Step-4: Perform state transition to one of the state from multiple states available for state transition from no state
			//----------------------------------------------------------------------------------------------------------------------
			String[] states = dataPool.get("WorkflowStates").split(",");
			String result = "";

			for(int state = 0;state < states.length; state++)
				if(!metadataCard.isWorkflowStateEnabled(states[state]))
					result += states[state] + ";";

			if(!result.equals(""))
				throw new SkipException("States('" + result + "') not available for state transition from no state.");

			metadataCard.setWorkflowState(dataPool.get("WorkflowStates").split(",")[1]);//Sets the workflow state in the metadata card
			metadataCard.saveAndClose();//Creates the new object

			Log.message("4. Workflow state '" + dataPool.get("WorkflowStates").split(",")[1] + "' is set and clicked the create button in the metadatacard.");

			//Verification: Check if object is created successfully
			//------------------------------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiates the right pane metadata card
			if(metadataCard.getWorkflowState().equalsIgnoreCase(dataPool.get("WorkflowStates").split(",")[1]))
				Log.pass("Test case passed. User able to change the state when 2 states possible in new object default workflow.");
			else
				Log.fail("Test case failed. User not able to change the state when 2 states possible in new object default workflow. [Actual workflow state: '" + metadataCard.getWorkflowState() + "' & Expected workflow state: '" + dataPool.get("WorkflowStates").split(",")[1] + "']", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_142553

	/**
	 * TC_142801 : Check Search text/character is highlighted in listing during search 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"Search"},description = "Check Search text/character is highlighted in listing during search " )
	public void TC_142801(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; //Webdriver instance

		try {

			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Perform keyword search
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("viewToNavigate"), dataPool.get("SearchWord"));

			Log.message("1. Performed keyword '" + dataPool.get("SearchWord") + "' search and navigated to '" + viewToNavigate + "' view.");

			//Verification: Check whether the search keyword is highlighed in the list view
			//------------------------------------------------------------------------------
			String result = homePage.listView.isListviewItemsHighlightedByText(dataPool.get("SearchWord"));

			if(result.equals(""))
				Log.pass("Test case passed. Search text/character is highlighted as expected in listing during search.");
			else
				Log.fail("Test case failed. Search text/character is not highlighted as expected in listing during search.[Not highlighted items: '" + result + "']", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_142801

} //End class SmokeTestCase
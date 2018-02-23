package MFClient.Tests;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
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
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class PrefilledPropertyInViews {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String userName = null;
	public static String userFullName = null;
	public static String password = null;
	public static String testVault = null;
	public static String productVersion = null;
	public static WebDriver driver = null;
	public static String configURL = null;
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
	 * 104.2.1A : Verify if Name field value is pre-filled in new meta data card from task pane in Other views 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if Name field value is prefilled in new metadata card from task pane in Other views.")
	public void SprintTest104_2_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.

			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Click Assignment option from task pane
			//------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Assignment.Value); 

			Log.message("3. Clicked Assignment option from task pane.");

			//Verification: To verify if Name field value in new metadata card
			//-----------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); // Initialize new metadata card.
			if(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).trim().isEmpty())
				Log.pass("Test case Passed. Name field is not prefilled in new metadata card from task pane in "+ dataPool.get("NavigateToView") + "  view.");
			else
				Log.fail("Test case Failed. Name field is prefilled " + metadataCard.getPropertyValue("Name or title") + " in new metadata card from task pane in "+ dataPool.get("NavigateToView") + "  view.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_2_1A


	/**
	 * 104.2.1B : Verify if Name field value is pre-filled in new metadata card from menubar in Other views 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if Name field value is prefilled in new metadata card from menubar in Other views.")
	public void SprintTest104_2_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.

			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Click Assignment option from menu bar
			//-----------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);

			Log.message("3. Clicked Assignment option from menubar.");

			//Verification: To verify if Name field value in new metadata card
			//-----------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); // Initialize new metadata card.
			if(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).trim().isEmpty())
				Log.pass("Test case Passed. Name field is not prefilled in new metadata card from menubar in "+ dataPool.get("NavigateToView") + "  view.");
			else
				Log.fail("Test case Failed. Name field is prefilled " + metadataCard.getPropertyValue("Name or title") + " in new metadata card from menubar in "+ dataPool.get("NavigateToView") + "  view.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_2_1B



	/**
	 * 104.2.2A : Verify if Name field value is pre-filled in new metadata card from task pane in Template view 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if Name field value is prefilled in new metadata card from task pane in Template view.")
	public void SprintTest104_2_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.

			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Click Assignment option from task pane
			//------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Customer.Value);

			Log.message("3. Clicked Assignment option from task pane.");


			//Verification: To verify if Name field value in new metadata card
			//-----------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); // Initialize new metadata card.
			if(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).trim().isEmpty())
				Log.pass("Test case Passed. Name field is not prefilled in new metadata card from task pane in "+ dataPool.get("NavigateToView") + "  view.");
			else
				Log.fail("Test case Failed. Name field is prefilled " + metadataCard.getPropertyValue("Name or title") + " in new metadata card from task pane in "+ dataPool.get("NavigateToView") + "  view.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_2_2A

	/**
	 * 104.2.2B : Verify if Name field value is pre-filled in new metadata card from menubar in Template view 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if Name field value is prefilled in new metadata card from menubar in Template view.")
	public void SprintTest104_2_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.

			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Click Assignment option from menu bar
			//-----------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Customer.Value);

			Log.message("3. Clicked Assignment option from menubar.");

			//Verification: To verify if Name field value in new metadata card
			//-----------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); // Initialize new metadata card.
			if(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).trim().isEmpty())
				Log.pass("Test case Passed. Name field is not prefilled in new metadata card from task pane in "+ dataPool.get("NavigateToView") + "  view.");
			else
				Log.fail("Test case Failed. Name field is prefilled " + metadataCard.getPropertyValue("Name or title") + " in new metadata card from task pane in "+ dataPool.get("NavigateToView") + "  view.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_2_2B


	/**
	 * 104.2.3A : Verify if Name field value is pre-filled in new metadata card from task pane in Folders view 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if Name field value is prefilled in new metadata card from task pane in Folders view.")
	public void SprintTest104_2_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.

			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Click Assignment option from task pane
			//------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Assignment.Value);

			Log.message("3. Clicked Assignment option from task pane.");

			//Verification: To verify if Name field value in new metadata card
			//-----------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); // Initialize new metadata card.
			if(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).trim().isEmpty())
				Log.pass("Test case Passed. Name field is not prefilled in new metadata card from task pane in "+ dataPool.get("NavigateToView") + "  view.");
			else
				Log.fail("Test case Failed. Name field is prefilled " + metadataCard.getPropertyValue("Name or title") + " in new metadata card from task pane in "+ dataPool.get("NavigateToView") + "  view.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_2_3A

	/**
	 * 104.2.3B : Verify if Name field value is pre-filled in new metadata card from menubar in Folders view 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if Name field value is prefilled in new metadata card from menubar in Folders view.")
	public void SprintTest104_2_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//-----------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Click Assignment option from menu bar
			//-----------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);
			Log.message("3. Clicked Assignment option from menubar.");
			MetadataCard metadataCard = new MetadataCard(driver); // Initialize new metadata card.

			//Verification: To verify if Name field value in new metadata card
			//-----------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).trim().isEmpty())
				Log.pass("Test case Passed. Name field is not prefilled in new metadata card from task pane in "+ dataPool.get("NavigateToView") + "  view.");
			else
				Log.fail("Test case Failed. Name field is prefilled " + metadataCard.getPropertyValue("Name or title") + " in new metadata card from task pane in "+ dataPool.get("NavigateToView") + "  view.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_2_3B


	/**
	 * 104.2.4A : Verify if Name field value is pre-filled in new metadata card from task pane in Virtual Folders view 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if Name field value is prefilled in new metadata card from task pane in Virtual Folders view.")
	public void SprintTest104_2_4A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Click Assignment option from task pane
			//------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Assignment.Value);
			Log.message("3. Clicked Assignment option from task pane.");
			MetadataCard metadataCard = new MetadataCard(driver); // Initialize new metadata card.

			//Verification: To verify if Name field value in new metadata card
			//-----------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).trim().isEmpty())
				Log.pass("Test case Passed. Name field is not prefilled in new metadata card from task pane in "+ dataPool.get("NavigateToView") + "  view.");
			else
				Log.fail("Test case Failed. Name field is prefilled " + metadataCard.getPropertyValue("Name or title") + " in new metadata card from task pane in "+ dataPool.get("NavigateToView") + "  view.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_2_4A

	/**
	 * 104.2.4B : Verify if Name field value is pre-filled in new metadata card from menubar in Virtual Folders view 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if Name field value is prefilled in new metadata card from menubar in Virtual Folders view.")
	public void SprintTest104_2_4B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Click Assignment option from menu bar
			//-----------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);
			Log.message("3. Clicked Assignment option from menubar.");
			MetadataCard metadataCard = new MetadataCard(driver); // Initialize new metadata card.

			//Verification: To verify if Name field value in new metadata card
			//-----------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).trim().isEmpty())
				Log.pass("Test case Passed. Name field is not prefilled in new metadata card from task pane in "+ dataPool.get("NavigateToView") + "  view.");
			else
				Log.fail("Test case Failed. Name field is prefilled " + metadataCard.getPropertyValue("Name or title") + " in new metadata card from task pane in "+ dataPool.get("NavigateToView") + "  view.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_2_4B

	/**
	 * 104.2.5A : Verify if Name field value is prefilled in new metadata card from task pane in all object search view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if Name field value is prefilled in new metadata card from task pane in all object search view.")
	public void SprintTest104_2_5A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//------------------------------
			SearchPanel search = new SearchPanel(driver);
			search.clickSearchBtn(driver);
			Log.message("2. To perform all objects search."); 

			//Step-3 : Click Assignment option from task pane
			//------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Assignment.Value);
			Log.message("3. Clicked Assignment option from task pane.");
			MetadataCard metadataCard = new MetadataCard(driver); // Initialize new metadata card.

			//Verification: To verify if Name field value in new metadata card
			//-----------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).trim().isEmpty())
				Log.pass("Test case Passed. Name field is not prefilled in new metadata card from task pane in All object search view.");
			else
				Log.fail("Test case Failed. Name field is prefilled " + metadataCard.getPropertyValue("Name or title") + " in new metadata card from task pane in All object search  view.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_2_5A

	/**
	 * 104.2.5B : Verify if Name field value is prefilled in new metadata card from menubar in all object search view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if Name field value is prefilled in new metadata card from menubar in all object search view.")
	public void SprintTest104_2_5B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//------------------------------
			SearchPanel search = new SearchPanel(driver);
			search.clickSearchBtn(driver);
			Log.message("2. To perform all objects search."); 

			//Step-3 : Click Assignment option from menu bar
			//-----------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);
			Log.message("3. Clicked Assignment option from menubar.");
			MetadataCard metadataCard = new MetadataCard(driver); // Initialize new metadata card.

			//Verification: To verify if Name field value in new metadata card
			//-----------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).trim().isEmpty())
				Log.pass("Test case Passed. Name field is not prefilled in new metadata card from task pane in All object search view.");
			else
				Log.fail("Test case Failed. Name field is prefilled " + metadataCard.getPropertyValue("Name or title") + " in new metadata card from task pane in All object search  view.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_2_5B

	/**
	 * 104.2.6A : Verify if Name field value is prefilled in new metadata card from task pane in advanced search
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if Name field value is prefilled in new metadata card from task pane in any advanced search.")
	public void SprintTest104_2_6A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//------------------------------
			homePage.searchPanel.search("", "Search only: Customers");
			Log.message("2. To perform Advanced search."); 

			//Step-3 : Click Assignment option from task pane
			//------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Assignment.Value);
			Log.message("3. Clicked Assignment option from task pane.");
			MetadataCard metadataCard = new MetadataCard(driver); // Initialize new metadata card.

			//Verification: To verify if Name field value in new metadata card
			//-----------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).trim().isEmpty())
				Log.pass("Test case Passed. Name field is not prefilled in new metadata card from task pane in Advanced search.");
			else
				Log.fail("Test case Failed. Name field is prefilled " + metadataCard.getPropertyValue("Name or title") + " in new metadata card from task pane in Advanced search.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_2_6A

	/**
	 * 104.2.6B : Verify if Name field value is prefilled in new metadata card from menubar in advanced search
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if Name field value is prefilled in new metadata card from menubar in advanced search.")
	public void SprintTest104_2_6B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//--------------------------
			SearchPanel search = new SearchPanel(driver);
			search.clickSearchBtn(driver);
			Log.message("2. To perform Advanced search."); 

			//Step-3 : Click Assignment option from menu bar
			//-----------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);

			Log.message("3. Clicked Assignment option from menubar.");
			MetadataCard metadataCard = new MetadataCard(driver); // Initialize new metadata card.

			//Verification: To verify if Name field value in new metadata card
			//-----------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).trim().isEmpty())
				Log.pass("Test case Passed. Name field is not prefilled in new metadata card from menubar in Advanced search.");
			else
				Log.fail("Test case Failed. Name field is prefilled " + metadataCard.getPropertyValue("Name or title") + " in new metadata card from menubar in Advanced search.", driver);
		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_6B

	/**
	 * 104.2.7A : Verify if Employee is prefilled when opening New Assignment from task pane in Folder views
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if Employee is prefilled when opening New Assignment from task pane in Folder views.")
	public void SprintTest104_2_7A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Select any exiting object
			//----------------------------------
			homePage.listView.clickItemByIndex(2); // Select any exiting item in listing view by index value.
			String selecteddocument = homePage.listView.getItemNameByItemIndex(2);
			Log.message("3. Select any existing object." + selecteddocument);

			//Step-4 : Click Assignment option from task pane
			//------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Assignment.Value);
			Log.message("4. Clicked Assignment option from task pane.");
			MetadataCard metadataCard = new MetadataCard(driver); // Initialize new metadata card.

			//Verification: To verify if Name field value in new metadata card
			//-----------------------------------------------------------------
			if(selecteddocument.trim().contains(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).trim()))
				Log.pass("Test case Passed. " + dataPool.get("PropertyToCheck") + " property is prefilled in new metadata card from task pane in "+ dataPool.get("NavigateToView")+ " view.");
			else
				Log.fail("Test case Failed. " + dataPool.get("PropertyToCheck") + " field is not prefilled in new metadata card from task pane in "+ dataPool.get("NavigateToView")+ ".", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_2_7A

	/**
	 * 104.2.7B : Verify if Employee is prefilled when opening New Assignment from menu bar in Folder views
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if Employee is prefilled when opening New Assignment from menu bar in Folder views.")
	public void SprintTest104_2_7B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Select any exiting object
			//----------------------------------
			homePage.listView.clickItemByIndex(2); // Select exiting item in listing view by index value.
			String selecteddocument = homePage.listView.getItemNameByItemIndex(2);
			Log.message("3. Select any existing object." + selecteddocument);

			//Step-4 : Click Assignment option from menu bar
			//-----------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);
			Log.message("4. Clicked Assignment option from menubar.");
			MetadataCard metadataCard = new MetadataCard(driver); // Initialize new metadata card.

			//Verification: To verify if Name field value in new metadata card
			//-----------------------------------------------------------------
			if(selecteddocument.trim().contains(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).trim()))
				Log.pass("Test case Passed. " + dataPool.get("PropertyToCheck") + " property is prefilled in new metadata card from task pane in "+ dataPool.get("NavigateToView")+ " view.");
			else
				Log.fail("Test case Failed. " + dataPool.get("PropertyToCheck") + " field is not prefilled in new metadata card from task pane in "+ dataPool.get("NavigateToView")+ ".", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_7B


	/**
	 * 104.2.8A : Verify if Employee property value is prefilled when opening New Assignment from task pane in Search type views
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if Employee property value is prefilled when opening New Assignment from task pane in Advanced search views.")
	public void SprintTest104_2_8A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.

			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//------------------------------
			homePage.searchPanel.search("", dataPool.get("SearchType"));
			// String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("2. To performed '" + dataPool.get("SearchType") + "' advanced search."); 

			//Step-3 : Select any exiting object
			//----------------------------------
			homePage.listView.clickItemByIndex(2); // Select exiting item in listing view by index value.
			String selectedObject = homePage.listView.getItemNameByItemIndex(2);
			Log.message("3. " + selectedObject + " Object selected in listing view.");

			//Step-4 : Click Assignment option from task pane
			//------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Assignment.Value);
			Log.message("4. Clicked Assignment option from task pane.");
			MetadataCard metadataCard = new MetadataCard(driver); // Initialize new metadata card.

			//Verification: To verify if Name field value in new metadata card
			//-----------------------------------------------------------------
			if(selectedObject.trim().contains(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).trim()))
				Log.pass("Test case Passed. " + dataPool.get("PropertyToCheck") + " property is prefilled in new metadata card from task pane in "+ dataPool.get("SearchType") + " view.");
			else
				Log.fail("Test case Failed. " + dataPool.get("PropertyToCheck") + " field is not prefilled in new metadata card from task pane in "+ dataPool.get("SearchType") + ".", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_2_8A

	/**
	 * 104.2.8B : Verify if Employee property value is prefilled when opening New Assignment from menubar in Search type views
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if Employee property value is prefilled when opening New Assignment from menubar in Advanced search views.")
	public void SprintTest104_2_8B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//------------------------------
			homePage.searchPanel.search("", dataPool.get("SearchType"));
			Log.message("2. To performed '" + dataPool.get("SearchType") + "' advanced search."); 

			//Step-3 : Select any exiting object
			//----------------------------------
			homePage.listView.clickItemByIndex(2); // Select exiting item in listing view by index value.
			String selectedObject = homePage.listView.getItemNameByItemIndex(2);
			Log.message("3. " + selectedObject + " Object selected in listing view.");

			//Step-4 : Click Assignment option from menu bar
			//-----------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);
			Log.message("4. Clicked Assignment option from menubar.");
			MetadataCard metadataCard = new MetadataCard(driver); // Initialize new metadata card.

			//Verification: To verify if Name field value in new metadata card
			//-----------------------------------------------------------------
			if(selectedObject.trim().contains(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).trim()))
				Log.pass("Test case Passed. " + dataPool.get("PropertyToCheck") + " property is prefilled in new metadata card from task pane in "+ dataPool.get("SearchType") + " view.");
			else
				Log.fail("Test case Failed. " + dataPool.get("PropertyToCheck") + " field is not prefilled in new metadata card from task pane in "+ dataPool.get("SearchType") + ".", driver);
		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_8B

	/**
	 * 104.2.9A : Verify if Employee property value is prefilled when opening New object from task pane in triplet condition search result without selecting any existing object in list view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if Employee property value is prefilled when opening New object from task pane in triplet condition search result without selecting any existing object in list view.")
	public void SprintTest104_2_9A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//-----------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Perform advanced search with triplet condition
			//--------------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"),dataPool.get("Condition"),dataPool.get("PropValue"));
			homePage.searchPanel.clickSearchBtn(driver); // Click search button
			Log.message("2. To performed search with '" + dataPool.get("Property") +"','"+ dataPool.get("Condition") +"','"+ dataPool.get("PropValue") + "' condition."); 

			//Step-3 : Click Document option from task pane
			//---------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Document.Value);
			Log.message("3. Clicked Document option from task pane.");

			//Step-4 : Select template in new document card
			//----------------------------------------------
			if(!Utility.selectTemplate(dataPool.get("Template"), driver))
				throw new Exception("The" + dataPool.get("Template") + "Template not available in document metadata card.");

			MetadataCard metadataCard = new MetadataCard(driver); // Initialize new metadata card.
			Log.message("4."+ dataPool.get("Template") + " Template was selected in new metadata card.");

			//Verification: To verify if Name field value in new metadata card
			//-----------------------------------------------------------------
			if(dataPool.get("PropValue").trim().contains(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).trim()))
				Log.pass("Test case Passed. " + dataPool.get("PropertyToCheck") + " property is prefilled in new metadata card from task pane in triplet condition search result without selecting any existing object in list view.");
			else
				Log.fail("Test case Failed. " + dataPool.get("PropertyToCheck") + " field is not prefilled in new metadata card from task pane in in triplet condition search result without selecting any existing object in list view.", driver);
		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_2_9A

	/**
	 * 104.2.9B : Verify if Employee property value is prefilled when opening New object from menubar in triplet condition search result without selecting any existing object in list view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if Employee property value is prefilled when opening New object from menubar in triplet condition search result without selecting any existing object in list view.")
	public void SprintTest104_2_9B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Perform advanced search with triplet condition
			//-------------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"),dataPool.get("Condition"),dataPool.get("PropValue"));
			homePage.searchPanel.clickSearchBtn(driver);

			Log.message("2. To performed search with '" + dataPool.get("Property") +"','"+ dataPool.get("Condition") +"','"+ dataPool.get("PropValue") + "' condition."); 

			//Step-3 : Click Document option from menubar
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Document.Value);
			Log.message("3. Clicked Document option from menubar.");

			//Step-4 : Select template in new document card
			//----------------------------------------------
			if(!Utility.selectTemplate(dataPool.get("Template"), driver)) {
				throw new Exception("The" + dataPool.get("Template") + "Template not available in document metadata card.");
			}
			MetadataCard metadataCard = new MetadataCard(driver); // Initialize new metadata card.
			Log.message("4."+ dataPool.get("Template") + " Template was selected in new metadata card.");

			//Verification: To verify if Name field value in new metadata card
			//-----------------------------------------------------------------
			if(dataPool.get("PropValue").trim().contains(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).trim()))
				Log.pass("Test case Passed. " + dataPool.get("PropertyToCheck") + " property is prefilled in new metadata card from task pane in triplet condition search result without selecting any existing object in list view.");
			else
				Log.fail("Test case Failed. " + dataPool.get("PropertyToCheck") + " field is not prefilled in new metadata card from task pane in in triplet condition search result without selecting any existing object in list view.", driver);
		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_2_9B


	/** 
	 * 104_2_9C : Verify if Employee property value is prefilled when opening New object from task pane in triplet condition search result with select any existing object in list view.
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if Employee property value is prefilled when opening New object from task pane in triplet condition search result with select any existing object in list view.")
	public void SprintTest104_2_9C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Perform advanced search with triplet condition
			//-------------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"),dataPool.get("Condition"),dataPool.get("PropValue"));
			homePage.searchPanel.clickSearchBtn(driver); // Click Search button.
			Log.message("2. To performed search with '" + dataPool.get("Property") +"','"+ dataPool.get("Condition") +"','"+ dataPool.get("PropValue") + "' condition."); 

			//Step-3 : Select any existing object in list view
			//------------------------------------------------
			homePage.listView.clickItemByIndex(0); // Select exiting item in listing view by index value.
			String selectedObject = homePage.listView.getItemNameByItemIndex(0);
			Log.message("3." + selectedObject + " Object selected in listing view.");

			//Step-4 : Click Document option from task pane
			//------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Assignment.Value);
			Log.message("4. Clicked Assignment option from task pane.");
			MetadataCard metadataCard = new MetadataCard(driver); // Initialize new metadata card.

			//Verification: To verify if Name field value in new metadata card
			//-----------------------------------------------------------------
			if(selectedObject.trim().contains(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).trim()) && !metadataCard.propertyExists(dataPool.get("Property")))
				Log.pass("Test case Passed. " + dataPool.get("PropertyToCheck") + " property value is prefilled in new metadata card from task pane in triplet condition search result without selecting any existing object in list view.");
			else
				Log.fail("Test case Failed. " + dataPool.get("PropertyToCheck") + " field is not prefilled in new metadata card from task pane in in triplet condition search result without selecting any existing object in list view.", driver);
		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_2_9C

	/**
	 * 104.2.9D : Verify if Employee property value is pre-filled when opening new assignment object from menubar in triplet condition search result with select any existing object in list view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if Employee property value is prefilled when opening New object from menubar in triplet condition search result with select any existing object in list view.")
	public void SprintTest104_2_9D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"),dataPool.get("Condition"),dataPool.get("PropValue"));
			homePage.searchPanel.clickSearchBtn(driver);
			Log.message("2. To performed search with '" + dataPool.get("Property") +"','"+ dataPool.get("Condition") +"','"+ dataPool.get("PropValue") + "' condition."); 

			//Step-3 : Select any existing object in list view
			//------------------------------------------------
			homePage.listView.clickItemByIndex(0); // Select exiting item in listing view by index value.
			String selectedObject = homePage.listView.getItemNameByItemIndex(0);
			Log.message("3." + selectedObject + " Object selected in listing view.");

			//Step-4 : Click Document option from menu bar
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);
			Log.message("4. Clicked Assignment option from task pane.");
			MetadataCard metadataCard = new MetadataCard(driver); // Initialize new metadata card.

			//Verification: To verify if Name field value in new metadata card
			//-----------------------------------------------------------------
			if(selectedObject.trim().contains(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).trim()) && !metadataCard.propertyExists(dataPool.get("Property")))
				Log.pass("Test case Passed. " + dataPool.get("PropertyToCheck") + " property is prefilled in new metadata card from task pane in triplet condition search result without selecting any existing object in list view.");
			else
				Log.fail("Test case Failed. " + dataPool.get("PropertyToCheck") + " field is not prefilled in new metadata card from task pane in in triplet condition search result without selecting any existing object in list view.", driver);
		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_2_9D


	/**
	 * 104.2.10A : Verify if Employee property value is pre-filled in Newly created object metadata card from task pane in Folder views
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if Employee property value is pre-filled in Newly created object metadata card from task pane in Folder views.")
	public void SprintTest104_2_10A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//-----------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Select any existing object in list view
			//------------------------------------------------
			homePage.listView.clickItemByIndex(1); // Select exiting item in listing view by index value.
			String selectedObject = homePage.listView.getItemNameByItemIndex(1);
			Log.message("3." + selectedObject + " Object selected in listing view.");	

			//Step-4 : Click Assignment option from task pane
			//------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Assignment.Value);
			MetadataCard metadataCard = new MetadataCard(driver); // Initialize new metadata card.
			metadataCard.setInfo(dataPool.get("Properties")+dataPool.get("Object"));
			metadataCard.setOpenForEditing(false); // Disable open for editing option in new metadata card
			metadataCard.setCheckInImmediately(true);
			metadataCard.saveAndClose();
			Log.message("4. Clicked Assignment option from task pane and set the necessary info and create the "+ dataPool.get("Object") +"  object");

			//Step-5 . Search the created assignment object
			//--------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+Caption.ObjecTypes.Assignment.Value+"s");
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new Exception("The Object was not created.");
			Log.message("5. Search the created assignment object.");
			homePage.listView.clickItem(dataPool.get("Object"));
			metadataCard = new MetadataCard(driver,true);

			//Verification: To verify if Employee field value in new meta data card
			//----------------------------------------------------------------------
			if(selectedObject.trim().contains(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).trim()))
				Log.pass("Test case Passed. Name field is not prefilled in new metadata card from task pane in "+ dataPool.get("NavigateToView") + "  view.");
			else
				Log.fail("Test case Failed. Name field is prefilled " + metadataCard.getPropertyValue("Name or title") + " in new metadata card from task pane in "+ dataPool.get("NavigateToView") + "  view.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_2_10A

	/**
	 * 104.2.10B : Verify if Employee property value is pre-filled in Newly created object meta data card from menu bar in Folder views
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if Employee property value is pre-filled in Newly created object metadata card from menu bar in Folder views.")
	public void SprintTest104_2_10B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Select any existing object in list view
			//------------------------------------------------
			homePage.listView.clickItemByIndex(1);
			String selectedObject = homePage.listView.getItemNameByItemIndex(1);
			Log.message("3." + selectedObject + " Object selected in listing view.");	

			//Step-4 : Click Assignment option from task pane
			//-----------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);
			MetadataCard metadataCard = new MetadataCard(driver); // Initialize new metadata card.
			metadataCard.setInfo(dataPool.get("Properties")+dataPool.get("Object"));
			metadataCard.setOpenForEditing(false);
			metadataCard.setCheckInImmediately(true);
			metadataCard.saveAndClose();
			Log.message("4. Clicked Assignment option from task pane and set the necessary info and create the "+ dataPool.get("Object") +"  object");

			//Step-5 . Search the created assignment object
			//--------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+Caption.ObjecTypes.Assignment.Value+"s");
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new Exception("The Object was not created.");

			Log.message("5. Search the created assignment object.");
			homePage.listView.clickItem(dataPool.get("Object"));
			metadataCard = new MetadataCard(driver,true);

			//Verification: To verify if Employee field value in new meta data card
			//----------------------------------------------------------------------
			if(selectedObject.trim().contains(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).trim()))
				Log.pass("Test case Passed. Name field is not prefilled in new metadata card from task pane in "+ dataPool.get("NavigateToView") + "  view.");
			else
				Log.fail("Test case Failed. Name field is prefilled " + metadataCard.getPropertyValue("Name or title") + " in new metadata card from task pane in "+ dataPool.get("NavigateToView") + "  view.", driver);
		} //End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_10B

	/**
	 * 104.2.11A : Verify if Employee property value is prefilled in Newly created object metadata card from task pane in Search type views
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if Employee property value is prefilled in Newly created object metadata card from task pane in Search type views.")
	public void SprintTest104_2_11A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//-----------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//--------------------------
			homePage.searchPanel.search("", dataPool.get("SearchType"));

			Log.message("2. Navigated to '" + dataPool.get("SearchType") + "' search view."); 

			//Step-3 : Select any existing object in list view
			//------------------------------------------------
			homePage.listView.clickItemByIndex(1);
			String selectedObject = homePage.listView.getItemNameByItemIndex(1);
			Log.message("3." + selectedObject + " Object selected in listing view.");	

			//Step-4 : Click Document option from task pane
			//------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Document.Value);
			if(!Utility.selectTemplate(dataPool.get("Template"), driver)) {
				throw new Exception("The" + dataPool.get("Template") + "Template not available in document metadata card.");
			}
			MetadataCard metadataCard = new MetadataCard(driver); // Initialize new metadata card.
			Log.message("4."+ dataPool.get("Template") + " Template was selected in new metadata card.");
			metadataCard = new MetadataCard(driver);

			//Step-5 : Set default property values in new meta data card
			//---------------------------------------------------------
			metadataCard.setInfo(dataPool.get("Properties")+dataPool.get("Object"));
			metadataCard.setOpenForEditing(false);
			metadataCard.setCheckInImmediately(true);
			metadataCard.saveAndClose();
			Log.message("5. Clicked Document option from task pane and set the necessary info and create the "+ dataPool.get("Object") +"  object");

			//Step-6 . Search the created Document object
			//---------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")+"."+dataPool.get("Extension")))
				throw new Exception("The Object was not created.");
			Log.message("6. Search the created assignment object.");
			homePage.listView.clickItem(dataPool.get("Object")+"."+dataPool.get("Extension"));
			metadataCard = new MetadataCard(driver,true);

			//Verification: To verify if Employee field value in new meta data card
			//----------------------------------------------------------------------
			if(selectedObject.trim().contains(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).trim()))
				Log.pass("Test case Passed. Name field is not prefilled in new metadata card from task pane in "+ dataPool.get("NavigateToView") + "  view.");
			else
				Log.fail("Test case Failed. Name field is prefilled " + metadataCard.getPropertyValue("Name or title") + " in new metadata card from task pane in "+ dataPool.get("NavigateToView") + "  view.", driver);
		} //End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_11A

	/**
	 * 104.2.11B : Verify if Employee property value is prefilled in Newly created object metadata card from menu bar in Search type views
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if Employee property value is prefilled in Newly created object metadata card from menu bar in Search type views.")
	public void SprintTest104_2_11B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//-----------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//--------------------------
			homePage.searchPanel.search("", dataPool.get("SearchType"));
			Log.message("2. Navigated to '" + dataPool.get("SearchType") + "' search view."); 

			//Step-3 : Select any existing object in list view
			//------------------------------------------------
			homePage.listView.clickItemByIndex(1);
			String selectedObject = homePage.listView.getItemNameByItemIndex(1);
			Log.message("3." + selectedObject + " Object selected in listing view.");	

			//Step-4 : Click Assignment option from task pane
			//------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Document.Value);
			if(!Utility.selectTemplate(dataPool.get("Template"), driver)) {
				throw new Exception("The" + dataPool.get("Template") + "Template not available in document metadata card.");
			}
			MetadataCard metadataCard = new MetadataCard(driver); // Initialize new metadata card.
			Log.message("4."+ dataPool.get("Template") + " Template was selected in new metadata card.");
			metadataCard = new MetadataCard(driver);

			//Step-5 : Set default property values in new meta data card
			//-----------------------------------------------------------
			metadataCard.setInfo(dataPool.get("Properties")+dataPool.get("Object"));
			metadataCard.setOpenForEditing(false);
			metadataCard.setCheckInImmediately(true);
			metadataCard.saveAndClose();
			Log.message("5. Clicked Document option from task pane and set the necessary info and create the "+ dataPool.get("Object") +"  object");

			//Step-6 . Search the created assignment object
			//---------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")+"."+dataPool.get("Extension")))
				throw new Exception("The Object was not created.");
			Log.message("6. Search the created assignment object.");
			homePage.listView.clickItem(dataPool.get("Object")+"."+dataPool.get("Extension"));
			metadataCard = new MetadataCard(driver,true);

			//Verification: To verify if Employee field value in new meta data card
			//----------------------------------------------------------------------
			if(selectedObject.trim().contains(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).trim()))
				Log.pass("Test case Passed. Name field is not prefilled in new metadata card from task pane in "+ dataPool.get("NavigateToView") + "  view.");
			else
				Log.fail("Test case Failed. Name field is prefilled " + metadataCard.getPropertyValue("Name or title") + " in new metadata card from task pane in "+ dataPool.get("NavigateToView") + "  view.", driver);
		} //End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_11B

	/**
	 * 104.2.12A : Verify if property value is pre-filled in Newly created object metadata card from task pane in search with condition result
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if property value is prefilled in Newly created object metadata card from task pane in search with condition result.")
	public void SprintTest104_2_12A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//------------------------------
			homePage.searchPanel.search("","");
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"),dataPool.get("Condition"),dataPool.get("PropValue"));
			homePage.searchPanel.clickSearchBtn(driver);

			Log.message("2. To performed search with '" + dataPool.get("Property") +"','"+ dataPool.get("Condition") +"','"+ dataPool.get("PropValue") + "' condition."); 

			//Step-3 : Select any existing object in list view
			//------------------------------------------------
			homePage.listView.clickItemByIndex(1);
			String selectedObject = homePage.listView.getItemNameByItemIndex(1);
			Log.message("3." + selectedObject + " Object selected in listing view.");	

			//Step-4 : Click Document option from task pane
			//------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Document.Value);
			if(!Utility.selectTemplate(dataPool.get("Template"), driver)) {
				throw new Exception("The" + dataPool.get("Template") + "Template not available in document metadata card.");
			}
			MetadataCard metadataCard = new MetadataCard(driver); // Initialize new metadata card.
			Log.message("4."+ dataPool.get("Template") + " Template was selected in new metadata card.");
			metadataCard = new MetadataCard(driver);

			//Step-5 : Set default property values in new meta data card
			//---------------------------------------------------------
			metadataCard.setInfo(dataPool.get("Properties")+dataPool.get("Object"));
			metadataCard.setOpenForEditing(false);
			metadataCard.setCheckInImmediately(true);
			metadataCard.saveAndClose();
			Log.message("5. Clicked Document option from task pane and set the necessary info and create the "+ dataPool.get("Object") +"  object");

			//Step-6 . Search the created Document object
			//---------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")+"."+dataPool.get("Extension")))
				throw new Exception("The Object was not created.");
			Log.message("6. Search the created document object.");
			homePage.listView.clickItem(dataPool.get("Object")+"."+dataPool.get("Extension"));
			metadataCard = new MetadataCard(driver,true);

			//Verification: To verify if Employee field value in new meta data card
			//----------------------------------------------------------------------
			if(dataPool.get("PropValue").trim().contains(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).trim()))
				Log.pass("Test case Passed. Employee property value was pre-filled in Newly created object metadata card from task pane in search with condition result.");
			else
				Log.fail("Test case Failed. Employee property value was not pre-filled in Newly created object metadata card from task pane in search with condition result.", driver);
		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_12A

	/**
	 * 104.2.12B : Verify if Employee property value is pre-filled in Newly created object metadata card from task pane in search with condition result
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if Employee property value is prefilled in Newly created object metadata card from menubar in search with condition result.")
	public void SprintTest104_2_12B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//------------------------------
			homePage.searchPanel.search("","");
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"),dataPool.get("Condition"),dataPool.get("PropValue"));
			homePage.searchPanel.clickSearchBtn(driver);

			Log.message("2. To performed search with '" + dataPool.get("Property") +"','"+ dataPool.get("Condition") +"','"+ dataPool.get("PropValue") + "' condition."); 

			//Step-3 : Select any existing object in list view
			//-------------------------------------------------
			homePage.listView.clickItemByIndex(1);
			String selectedObject = homePage.listView.getItemNameByItemIndex(1);
			Log.message("3." + selectedObject + " Object selected in listing view.");	

			//Step-4 : Click Document option from task pane
			//----------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Document.Value);
			if(!Utility.selectTemplate(dataPool.get("Template"), driver)) {
				throw new Exception("The" + dataPool.get("Template") + "Template not available in document metadata card.");
			}
			MetadataCard metadataCard = new MetadataCard(driver); // Initialize new metadata card.
			Log.message("4."+ dataPool.get("Template") + " Template was selected in new metadata card.");

			//Step-5 : Set default property values in new meta data card
			//-----------------------------------------------------------
			metadataCard.setInfo(dataPool.get("Properties")+dataPool.get("Object"));
			metadataCard.setOpenForEditing(false);
			metadataCard.setCheckInImmediately(true);
			metadataCard.saveAndClose();
			Log.message("5. Clicked Document option from task pane and set the necessary info and create the "+ dataPool.get("Object") +"  object");

			//Step-6 . Search the created Document object
			//--------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")+"."+dataPool.get("Extension")))
				throw new Exception("The Object was not created.");
			Log.message("6. Search the created document object.");
			homePage.listView.clickItem(dataPool.get("Object")+"."+dataPool.get("Extension"));
			metadataCard = new MetadataCard(driver,true);

			//Verification: To verify if pre-filled property value in new meta data card
			//--------------------------------------------------------------------------
			if(dataPool.get("PropValue").trim().contains(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).trim()))
				Log.pass("Test case Passed. "+ dataPool.get("PropertyToCheck") +"  property value was pre-filled in Newly created object metadata card from menubar in search with condition result.");
			else
				Log.fail("Test case Failed. "+ dataPool.get("PropertyToCheck") +"  property value was not pre-filled in Newly created object metadata card from menubar in search with condition result.", driver);
		} //End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_12B

	/**
	 * 104.2.13A : Verify if property is pre-filled by selected multiple objects using control key in Newly created object meta data card from task pane in Folder views
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint104", "PrefilledProperties"}, 
			description = "Verify if property is prefilled by selected multiple objects using control key in Newly created object metadata card from task pane in Folder views.")
	public void SprintTest104_2_13A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("Safari"))
			throw new SkipException(driverType + " driver does not support multiselect.");

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//-----------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Select multiple object in list view using control key
			//--------------------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("ClickItems"));
			Log.message("3. Objects are multi selected using control key in listing view.");		

			//Step-4 : Click Assignment option from task pane
			//-----------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Assignment.Value);
			MetadataCard metadataCard = new MetadataCard(driver); // Initialize new metadata card.
			Log.message("4. New Assignment option was selected from task pane.");

			//Step-5 : Set default property values in new meta data card
			//---------------------------------------------------------
			metadataCard.setInfo(dataPool.get("Properties")+dataPool.get("Object"));
			metadataCard.saveAndClose();
			Log.message("5. Clicked Assignment option from task pane and set the necessary info and create the "+ dataPool.get("Object") +"  object");

			//Step-6 . Search the created Document object
			//---------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"),"");
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new Exception("The Object was not created.");
			Log.message("6. Search the created assigment object.");
			homePage.listView.clickItem(dataPool.get("Object"));
			metadataCard = new MetadataCard(driver,true);
			String[] propValues = metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).split("\n");

			//Verification: To verify if Employee field value in new meta data card
			//----------------------------------------------------------------------
			Boolean flag = false;
			for ( int index=0;index<propValues.length;index++)
			{
				if(dataPool.get("ClickItems").trim().contains(propValues[index]))
					flag =true;
				else 
					Log.fail("Test case Failed. "+ dataPool.get("PropertyToCheck") +"  property was not pre-filled by selected multiple objects using control key in Newly created object meta data card from task pane in "+ dataPool.get("NavigateToView") + "  view.)", driver);
			}     				  
			if(flag)	  
				Log.pass("Test case passed. "+ dataPool.get("PropertyToCheck") +"  property was pre-filled by selected multiple objects using control key in Newly created object meta data card from task pane in "+ dataPool.get("NavigateToView") + "  view.");
			else
				Log.fail("Test case Failed. "+ dataPool.get("PropertyToCheck") +"  property was not pre-filled by selected multiple objects using control key in Newly created object meta data card from task pane in "+ dataPool.get("NavigateToView") + "  view.)", driver);
		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_13A

	/**
	 * 104.2.13B : Verify if property is pre-filled by selected multiple objects using control key in Newly created object meta data card from menubar in Folder views
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint104", "PrefilledProperties"}, 
			description = "Verify if property is prefilled by selected multiple objects using control key in Newly created object metadata card from menubar in Folder views.")
	public void SprintTest104_2_13B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("Safari"))
			throw new SkipException(driverType + " driver does not support multiselect.");

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//-----------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Select multiple object in list view using control key
			//--------------------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("ClickItems"));
			Log.message("3. Objects are multi selected using control key in listing view.");		

			//Step-4 : Click Assignment option from task pane
			//-----------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);
			MetadataCard metadataCard = new MetadataCard(driver); // Initialize new metadata card.
			Log.message("4. New Assignment option was selected from task pane.");

			//Step-5 : Set default property values in new meta data card
			//---------------------------------------------------------
			metadataCard.setInfo(dataPool.get("Properties")+dataPool.get("Object"));
			metadataCard.saveAndClose();
			Log.message("5. Clicked Assignment option from task pane and set the necessary info and create the "+ dataPool.get("Object") +"  object");

			//Step-6 . Search the created Document object
			//---------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"),"");
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new Exception("The Object was not created.");
			Log.message("6. Search the created assigment object.");
			homePage.listView.clickItem(dataPool.get("Object"));
			metadataCard = new MetadataCard(driver,true);
			String[] propValues = metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).split("\n");

			//Verification: To verify if Employee field value in new meta data card
			//----------------------------------------------------------------------
			Boolean flag = false;
			for ( int index=0;index<propValues.length;index++)
			{
				if(dataPool.get("ClickItems").trim().contains(propValues[index]))
					flag =true;
				else 
					Log.fail("Test case Failed. "+ dataPool.get("PropertyToCheck") +"  property was not pre-filled by selected multiple objects using control key in Newly created object meta data card from menubar in "+ dataPool.get("NavigateToView") + "  view.)", driver);
			}     				  
			if(flag)	  
				Log.pass("Test case passed. "+ dataPool.get("PropertyToCheck") +"  property was pre-filled by selected multiple objects using control key in Newly created object meta data card from menubar in "+ dataPool.get("NavigateToView") + "  view.");
			else
				Log.fail("Test case Failed. "+ dataPool.get("PropertyToCheck") +"  property was not pre-filled by selected multiple objects using control key in Newly created object meta data card from menubar in "+ dataPool.get("NavigateToView") + "  view.)", driver);
		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_13B

	/**
	 * 104.2.14A : Verify if property is pre-filled by selected multiple objects using control key in Newly created object meta data card from task pane in search type views
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint104", "PrefilledProperties"}, 
			description = "Verify if property is prefilled by selected multiple objects using control key in Newly created object metadata card from task pane in search type views.")
	public void SprintTest104_2_14A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("Safari"))
			throw new SkipException(driverType + " driver does not support multiselect.");

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//-----------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//--------------------------
			homePage.searchPanel.search("", dataPool.get("SearchType"));
			Log.message("2. Navigated to '" + dataPool.get("SearchType") + "' search view."); 

			//Step-3 : Select multiple object in list view using control key
			//--------------------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("ClickItems"));
			Log.message("3. Objects are multi selected using control key in listing view.");		

			//Step-4 : Click Assignment option from task pane
			//-----------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Assignment.Value);
			MetadataCard metadataCard = new MetadataCard(driver); // Initialize new metadata card.
			Log.message("4. New Assignment option was selected from task pane.");

			//Step-5 : Set default property values in new meta data card
			//---------------------------------------------------------
			metadataCard.setInfo(dataPool.get("Properties")+dataPool.get("Object"));
			metadataCard.saveAndClose();
			Log.message("5. Clicked Assignment option from task pane and set the necessary info and create the "+ dataPool.get("Object") +"  object");

			//Step-6 . Search the created Assignment object
			//---------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"),"");
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new Exception("The Object was not created.");
			Log.message("6. Search the created assigment object.");
			homePage.listView.clickItem(dataPool.get("Object"));
			metadataCard = new MetadataCard(driver,true);
			String[] propValues = metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).split("\n");

			//Verification: To verify if Employee field value in new meta data card
			//----------------------------------------------------------------------
			Boolean flag = false;
			for ( int index=0;index<propValues.length;index++)
			{
				if(dataPool.get("ClickItems").trim().contains(propValues[index]))
					flag =true;
				else 
					Log.fail("Test case Failed. "+ dataPool.get("PropertyToCheck") +"  property was not pre-filled by selected multiple objects using control key in Newly created object meta data card from task pane in "+ dataPool.get("SearchType") + "  view.)", driver);
			}     				  
			if(flag)	  
				Log.pass("Test case passed. "+ dataPool.get("PropertyToCheck") +"  property was pre-filled by selected multiple objects using control key in Newly created object meta data card from task pane in "+ dataPool.get("SearchType") + "  view.");
			else
				Log.fail("Test case Failed. "+ dataPool.get("PropertyToCheck") +"  property was not pre-filled by selected multiple objects using control key in Newly created object meta data card from task pane in "+ dataPool.get("SearchType") + "  view.)", driver);
		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_14A

	/**
	 * 104.2.14B : Verify if property is pre-filled by selected multiple objects using control key in Newly created object meta data card from menubar in search type views
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint104", "PrefilledProperties"}, 
			description = "Verify if property is prefilled by selected multiple objects using control key in Newly created object metadata card from menubar in search type views.")
	public void SprintTest104_2_14B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("Safari"))
			throw new SkipException(driverType + " driver does not support multiselect.");

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//-----------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//--------------------------
			homePage.searchPanel.search("", dataPool.get("SearchType"));
			Log.message("2. Navigated to '" + dataPool.get("SearchType") + "' search view."); 

			//Step-3 : Select multiple object in list view using control key
			//--------------------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("ClickItems"));
			Log.message("3. Objects are multi selected using control key in listing view.");		

			//Step-4 : Click Assignment option from task pane
			//-----------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);
			MetadataCard metadataCard = new MetadataCard(driver); // Initialize new metadata card.
			Log.message("4. New Assignment option was selected from menubar.");

			//Step-5 : Set default property values in new meta data card
			//---------------------------------------------------------
			metadataCard.setInfo(dataPool.get("Properties")+dataPool.get("Object"));
			metadataCard.saveAndClose();
			Log.message("5. Clicked Assignment option from task pane and set the necessary info and create the "+ dataPool.get("Object") +"  object");

			//Step-6 . Search the created Document object
			//---------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"),"");
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new Exception("The Object was not created.");
			Log.message("6. Search the created assigment object.");
			homePage.listView.clickItem(dataPool.get("Object"));
			metadataCard = new MetadataCard(driver,true);
			String[] propValues = metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).split("\n");

			//Verification: To verify if Employee field value in new meta data card
			//----------------------------------------------------------------------
			Boolean flag = false;
			for ( int index=0;index<propValues.length;index++)
			{
				if(dataPool.get("ClickItems").trim().contains(propValues[index].trim()))
					flag = true;
				else 
					Log.fail("Test case Failed. "+ dataPool.get("PropertyToCheck") +"  property was not pre-filled by selected multiple objects using control key in Newly created object meta data card from menubar in "+ dataPool.get("SearchType") + "  view.)", driver);
			}     				  
			if(flag)	  
				Log.pass("Test case passed. "+ dataPool.get("PropertyToCheck") +"  property was pre-filled by selected multiple objects using control key in Newly created object meta data card from menubar in "+ dataPool.get("SearchType") + "  view.");
			else
				Log.fail("Test case Failed. "+ dataPool.get("PropertyToCheck") +"  property was not pre-filled by selected multiple objects using control key in Newly created object meta data card from menubar in "+ dataPool.get("SearchType") + "  view.)", driver);
		} //End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_14B

	/**
	 * 104.2.15A : Verify if property is prefilled the selected employee objects using Shift key in Newly created object metadata card from task pane in search with condition result view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint104", "PrefilledProperties"}, 
			description = "Verify if property is prefilled the selected employee objects using Shift key in Newly created object metadata card from task pane in search with condition result view.")
	public void SprintTest104_2_15A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("Safari"))
			throw new SkipException(driverType + " driver does not support multiselect.");

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//-----------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//--------------------------
			homePage.searchPanel.resetAll(); // Click Reset All option in advanced search.
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"),dataPool.get("Condition"),dataPool.get("PropValue"));
			homePage.searchPanel.clickSearchBtn(driver);

			Log.message("2. To performed search with '" + dataPool.get("Property") +"','"+ dataPool.get("Condition") +"','"+ dataPool.get("PropValue") + "' condition."); 
			int startIndex = Integer.parseInt(dataPool.get("StartIndex"));
			int endIndex = Integer.parseInt(dataPool.get("EndIndex"));

			//Step-3 : Select multiple object in list view using control key
			//--------------------------------------------------------------
			homePage.listView.shiftclickMultipleItemsByIndex(startIndex,endIndex);
			Log.message("3. Objects are multi selected using Shift key in listing view.");		

			//Step-4 : Click Document option from task pane
			//-----------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Document.Value);
			if(!Utility.selectTemplate(dataPool.get("Template"), driver)) {
				throw new Exception("The" + dataPool.get("Template") + "Template not available in document metadata card.");
			}
			MetadataCard metadataCard = new MetadataCard(driver);
			Log.message("4."+ dataPool.get("Template") + " Template was selected in new metadata card.");

			//Step-5 : Set default property values in new meta data card
			//---------------------------------------------------------
			metadataCard.setInfo(dataPool.get("Properties")+dataPool.get("Object"));
			metadataCard.setOpenForEditing(false);
			metadataCard.setCheckInImmediately(true);
			metadataCard.saveAndClose();
			Log.message("5. Clicked document option from task pane and set the necessary info and create the "+ dataPool.get("Object") +"  object");

			//Step-6 . Search the created Document object
			//---------------------------------------------
			homePage.searchPanel.resetAll();
			homePage.searchPanel.search(dataPool.get("Object"),"");
			if(!homePage.listView.isItemExists(dataPool.get("Object")+"."+dataPool.get("Extension")))
				throw new Exception("The Object was not created.");
			Log.message("6. Search the created assigment object.");
			homePage.listView.clickItem(dataPool.get("Object")+"."+dataPool.get("Extension"));
			metadataCard = new MetadataCard(driver,true);

			//Verification: To verify if Employee field value in new meta data card
			if(dataPool.get("PropValue").trim().equalsIgnoreCase(metadataCard.getPropertyValue(dataPool.get("Property"))))	  
				Log.pass("Test case passed."+ dataPool.get("Property") +" property value is pre-filled by selected multiple objects using control key in Newly created object meta data card from task pane in "+ dataPool.get("SearchType") + "  view.");
			else
				Log.fail("Test case Failed. "+ dataPool.get("Property") +" Property was not pre-filled by selected multiple objects using control key in Newly created object meta data card from task pane in "+ dataPool.get("SearchType") + "  view.)", driver);
		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_15A

	/**
	 * 104.2.15B : Verify if property is prefilled the selected objects using Shift key in Newly created object metadata card from menubar in search with condition result view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint104", "PrefilledProperties"}, 
			description = "Verify if property is prefilled the selected employee objects using Shift key in Newly created object metadata card from menubar in search with condition result view.")
	public void SprintTest104_2_15B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("Safari"))
			throw new SkipException(driverType + " driver does not support multiselect.");

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//-----------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//--------------------------
			homePage.searchPanel.resetAll(); // Click Reset All option in advanced search.
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"),dataPool.get("Condition"),dataPool.get("PropValue"));
			homePage.searchPanel.clickSearchBtn(driver);

			Log.message("2. To performed search with '" + dataPool.get("Property") +"','"+ dataPool.get("Condition") +"','"+ dataPool.get("PropValue") + "' condition."); 
			int startIndex = Integer.parseInt(dataPool.get("StartIndex"));
			int endIndex = Integer.parseInt(dataPool.get("EndIndex"));

			//Step-3 : Select multiple object in list view using control key
			//--------------------------------------------------------------
			homePage.listView.shiftclickMultipleItemsByIndex(startIndex,endIndex);
			Log.message("3. Objects are multi selected using Shift key in listing view.");		

			//Step-4 : Click Document option from task pane
			//-----------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Document.Value);
			if(!Utility.selectTemplate(dataPool.get("Template"), driver)) {
				throw new Exception("The" + dataPool.get("Template") + "Template not available in document metadata card.");
			}
			MetadataCard metadataCard = new MetadataCard(driver);
			Log.message("4."+ dataPool.get("Template") + " Template was selected in new metadata card.");

			//Step-5 : Set default property values in new meta data card
			//---------------------------------------------------------
			metadataCard.setInfo(dataPool.get("Properties")+dataPool.get("Object"));
			metadataCard.setOpenForEditing(false); // Disable open for editing option in new metadata card.
			metadataCard.setCheckInImmediately(true); // Enable checkin immediately option in new metadata card.
			metadataCard.saveAndClose(); // Save and close the new metadata card.
			Log.message("5. Clicked document option from task pane and set the necessary info and create the "+ dataPool.get("Object") +"  object");

			//Step-6 . Search the created Document object
			//---------------------------------------------
			homePage.searchPanel.resetAll(); // Click Reset All option in advanced search.
			homePage.searchPanel.search(dataPool.get("Object"),"");
			if(!homePage.listView.isItemExists(dataPool.get("Object")+"."+dataPool.get("Extension")))
				throw new Exception("The Object was not created.");
			Log.message("6. Search the created Document object.");
			homePage.listView.clickItem(dataPool.get("Object")+"."+dataPool.get("Extension"));
			metadataCard = new MetadataCard(driver,true); 

			//Verification: To verify if Employee field value in new meta data card
			if(dataPool.get("PropValue").trim().equalsIgnoreCase(metadataCard.getPropertyValue(dataPool.get("Property"))))	  
				Log.pass("Test case passed. "+ dataPool.get("Property") +" property was pre-filled by selected multiple objects using control key in Newly created object meta data card from task pane in "+ dataPool.get("SearchType") + "  view.");
			else
				Log.fail("Test case Failed. "+ dataPool.get("Property") +" property was not pre-filled by selected multiple objects using control key in Newly created object meta data card from task pane in "+ dataPool.get("SearchType") + "  view.)", driver);
		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_15B



	/**
	 * 104.2.16A : Verify if Employee property prefilled for all other object types except 'Document' & 'Assignment' with select any existing item in Manage Employees view 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if Employee property prefilled for all other object types except 'Document' & 'Assignment' with select any existing item in Manage Employees view.")
	public void SprintTest104_2_16A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Select any existing object in list view
			//------------------------------------------------
			homePage.listView.clickItemByIndex(1);  // Select any exiting item in listing view by index value.
			String selectedObject = homePage.listView.getItemNameByItemIndex(1);
			Log.message("3." + selectedObject + " Object selected in listing view.");	

			//Step-4 : Click new object option from menu bar
			//------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectToCreate"));
			Log.message("4. Clicked "+ dataPool.get("ObjectToCreate") +" option from menu bar.");
			MetadataCard metadataCard = new MetadataCard(driver); // Initialize new metadata card.

			//Verification: To verify if Name field value in new metadata card
			//-----------------------------------------------------------------
			if(!metadataCard.propertyExists(dataPool.get("PropertyToCheck")))
				Log.pass("Test case Passed. "+dataPool.get("PropertyToCheck")+" property is does not exist in new "+dataPool.get("ObjectToCreate")+"  metadata card in "+ dataPool.get("NavigateToView") + "  view.");
			else {
				if(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).trim().isEmpty())
					Log.pass("Test case Passed. "+dataPool.get("PropertyToCheck")+" property value is not pre filled in new "+dataPool.get("ObjectToCreate")+"  metadata card in "+ dataPool.get("NavigateToView") + "  view.");
				else	
					Log.fail("Test case Failed. "+dataPool.get("PropertyToCheck")+" property value is pre filled in new "+dataPool.get("ObjectToCreate")+"  metadata card in "+ dataPool.get("NavigateToView") + "  view.",driver);
			}
		} //End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_16A

	/**
	 * 104.2.16B : Verify if Employee property prefilled for all other object types except 'Document' & 'Assignment' without select any existing item in Manage Employees view 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if Employee property prefilled for all other object types except 'Document' & 'Assignment' without select any existing item in Manage Employees view.")
	public void SprintTest104_2_16B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Click new object option from menu bar
			//------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectToCreate"));
			Log.message("3. Without select any existing object clicked "+ dataPool.get("ObjectToCreate") +" option from menu bar.");
			MetadataCard metadataCard = new MetadataCard(driver); // Initialize new metadata card.

			//Verification: To verify if Name field value in new metadata card
			//-----------------------------------------------------------------
			if(!metadataCard.propertyExists(dataPool.get("PropertyToCheck")))
				Log.pass("Test case Passed. "+dataPool.get("PropertyToCheck")+" property is does not exist in new "+dataPool.get("ObjectToCreate")+"  metadata card in "+ dataPool.get("NavigateToView") + "  view.");
			else {
				if(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).trim().isEmpty())
					Log.pass("Test case Passed. "+dataPool.get("PropertyToCheck")+" property value is not pre filled in new "+dataPool.get("ObjectToCreate")+"  metadata card in "+ dataPool.get("NavigateToView") + "  view.");
				else	
					Log.fail("Test case Failed. "+dataPool.get("PropertyToCheck")+" property value is pre filled in new "+dataPool.get("ObjectToCreate")+"  metadata card in "+ dataPool.get("NavigateToView") + "  view.",driver);
			}
		} //End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_16B

	/**
	 * 104.2.16C : Verify if Employee property prefilled for all other object types except 'Document' & 'Assignment' with multi select existing items in Manage Employees view 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint104", "PrefilledProperties"}, 
			description = "Verify if Employee property prefilled for all other object types except 'Document' & 'Assignment' with select any existing item in Manage Employees view.")
	public void SprintTest104_2_16C(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("Safari"))
			throw new SkipException(driverType + " driver does not support multiselect.");

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Select any existing object in list view
			//------------------------------------------------
			homePage.listView.shiftclickMultipleItemsByIndex(Integer.parseInt(dataPool.get("StartIndex")),Integer.parseInt(dataPool.get("EndIndex")));  // Select multiple items in listing view using Shift key.
			Log.message("3. Selected multpiple objects in listing view using Shift key.");	

			//Step-4 : Click new object option from menu bar
			//------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectToCreate"));
			Log.message("4. Clicked "+ dataPool.get("ObjectToCreate") +" option from menu bar.");
			MetadataCard metadataCard = new MetadataCard(driver); // Initialize new metadata card.

			//Verification: To verify if Name field value in new metadata card
			//-----------------------------------------------------------------
			if(!metadataCard.propertyExists(dataPool.get("PropertyToCheck")))
				Log.pass("Test case Passed. "+dataPool.get("PropertyToCheck")+" property is does not exist in new "+ dataPool.get("ObjectToCreate")+"  metadata card in "+ dataPool.get("NavigateToView") + "  view.");
			else {
				if(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).trim().isEmpty())
					Log.pass("Test case Passed. "+dataPool.get("PropertyToCheck")+" property value is not pre filled in new "+dataPool.get("ObjectToCreate")+"  metadata card in "+ dataPool.get("NavigateToView") + "  view.");
				else	
					Log.fail("Test case Failed. "+dataPool.get("PropertyToCheck")+" property value is pre filled in new "+dataPool.get("ObjectToCreate")+"  metadata card in "+ dataPool.get("NavigateToView") + "  view.",driver);
			}
		} //End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_16C

	/**
	 * 104.2.17A : Verify if Employee property and related object type properties are prefilled in newly created 'Document' object from task pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if Employee property and related object type properties are prefilled in newly created 'Document' object from task pane.")
	public void SprintTest104_2_17A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Select any existing object in list view
			//------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ClickItem"));  // Select any exiting item in listing view.
			String projecValue = dataPool.get("ClickItem");
			MetadataCard metadataCard = new MetadataCard(driver,true);
			String customerValue = metadataCard.getPropertyValue(Caption.ObjecTypes.Customer.Value);
			String contacpersonValue = metadataCard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value);
			Log.message("3." + dataPool.get("ClickItem") + " Object selected in listing view.");	

			//Step-4 : Click new object option from menu bar
			//------------------------------------------------
			driver.switchTo().defaultContent();
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Document.Value);
			if(!Utility.selectTemplate(dataPool.get("Template"), driver)) 
				throw new Exception("The" + dataPool.get("Template") + "Template not available in document metadata card.");
			Log.message("4."+ dataPool.get("Template") + " Template was selected in new metadata card.");

			//Step-5 : Set default property values in new meta data card
			//----------------------------------------------------------

			MFilesDialog mfilesDialog = new MFilesDialog(driver,"Confirm"); //Instantiating MFilesDialog wrapper class
			mfilesDialog.clickOkButton();
			metadataCard = new MetadataCard(driver);
			metadataCard.setInfo(dataPool.get("Properties")+dataPool.get("Object"));
			metadataCard.setOpenForEditing(false); // Disable open for editing option in new metadata card.
			metadataCard.setCheckInImmediately(true); // Enable checkin immediately option in new metadata card.
			metadataCard.saveAndClose(); // Save and close the new metadata card.
			Log.message("5. Clicked document option from task pane and set the necessary info and create the "+ dataPool.get("Object") +"  object");

			//Step-6 . Search the created Document object
			//--------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"),"");
			if(!homePage.listView.isItemExists(dataPool.get("Object")+"."+dataPool.get("Extension")))
				throw new Exception("The Object was not created.");
			Log.message("6. Search the created Document object.");
			homePage.listView.clickItem(dataPool.get("Object")+"."+dataPool.get("Extension"));
			metadataCard = new MetadataCard(driver,true); 		

			//Verification: To verify if properties field value in new metadata card
			//-----------------------------------------------------------------
			if(customerValue.trim().equalsIgnoreCase(metadataCard.getPropertyValue(Caption.ObjecTypes.Customer.Value).trim()) && contacpersonValue.trim().equalsIgnoreCase(metadataCard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).trim()) && projecValue.trim().equalsIgnoreCase(metadataCard.getPropertyValue(Caption.ObjecTypes.Project.Value).trim()))
				Log.pass("Test case Passed. Selected project and related object [customer & contact person] property values are  pre filled in newly created document object from task pane.");
			else	
				Log.fail("Test case Failed. Selected project and related object [customer & contact person] property values are not pre filled in newly created document object from task pane.",driver);
		} //End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_17A

	/**
	 * 104.2.17B : Verify if Employee property and related object type properties are prefilled in newly created 'Document' object from menu bar
	 * @param Hash map values and Driver type
	 * @return 
	 * @throws 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if Employee property and related object type properties are prefilled in newly created 'Document' object from menu bar.")
	public void SprintTest104_2_17B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.

			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Select any existing object in list view
			//------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ClickItem"));  // Select any exiting item in listing view.
			String projecValue = dataPool.get("ClickItem");
			MetadataCard metadataCard = new MetadataCard(driver,true);
			String customerValue = metadataCard.getPropertyValue(Caption.ObjecTypes.Customer.Value);
			String contacpersonValue = metadataCard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value);

			Log.message("3." + dataPool.get("ClickItem") + " Object selected in listing view.");	

			//Step-4 : Click new object option from menu bar
			//------------------------------------------------
			driver.switchTo().defaultContent();
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Document.Value);
			if(!Utility.selectTemplate(dataPool.get("Template"), driver)) 
				throw new Exception("The" + dataPool.get("Template") + "Template not available in document metadata card.");

			Log.message("4."+ dataPool.get("Template") + " template was selected in new metadata card.");

			//Step-5 : Set default property values in new meta data card
			//-----------------------------------------------------------

			MFilesDialog mfilesDialog = new MFilesDialog(driver,"Confirm"); //Instantiating MFilesDialog wrapper class
			mfilesDialog.clickOkButton();
			metadataCard = new MetadataCard(driver);
			metadataCard.setInfo(dataPool.get("Properties")+dataPool.get("Object"));
			metadataCard.setOpenForEditing(false); // Disable open for editing option in new metadata card.
			metadataCard.setCheckInImmediately(true); // Enable checkin immediately option in new metadata card.
			metadataCard.saveAndClose(); // Save and close the new metadata card.

			Log.message("5. Clicked document option from task pane and set the necessary info and create the "+ dataPool.get("Object") +"  object");

			//Step-6 . Search the created Document object
			//--------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"),"");
			if(!homePage.listView.isItemExists(dataPool.get("Object")+"."+dataPool.get("Extension")))
				throw new Exception("The Object was not created.");
			Log.message("6. Search the created Document object.");
			homePage.listView.clickItem(dataPool.get("Object")+"."+dataPool.get("Extension"));
			metadataCard = new MetadataCard(driver,true); 		

			//Verification: To verify if properties field value in new metadata card
			//-----------------------------------------------------------------
			if(customerValue.trim().equalsIgnoreCase(metadataCard.getPropertyValue(Caption.ObjecTypes.Customer.Value).trim()) && contacpersonValue.trim().equalsIgnoreCase(metadataCard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).trim()) && projecValue.trim().equalsIgnoreCase(metadataCard.getPropertyValue(Caption.ObjecTypes.Project.Value).trim()))
				Log.pass("Test case Passed. Selected project and related object [customer & contact person] property values are  pre filled in newly created document object from task pane.");
			else	
				Log.fail("Test case Failed. Selected project and related object [customer & contact person] property values are not pre filled in newly created document object from task pane.",driver);
		} //End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_17B

	/**
	 * 104.2.19A : Verify if customer property value is prefilled in new metadata card from task pane without select any view in Manage projects > Filter by customer folder view
	 * @param Hash map values and Driver type
	 * @return 
	 * @throws 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if customer property value is prefilled in new metadata card from task pane without select any view in Manage projects > Filter by customer folder view .")
	public void SprintTest104_2_19A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Click new object option from menu bar
			//------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Assignment.Value);
			Log.message("3. New Assignment object selected from task pane.");
			MetadataCard metadataCard = new MetadataCard(driver);

			//Verification: To verify if Name field value in new metadata card
			//-----------------------------------------------------------------
			if(!metadataCard.propertyExists(dataPool.get("PropertyToCheck")))
				Log.pass("Test case Passed. "+dataPool.get("PropertyToCheck")+" property is does not exist in new metadata card from task pane without select any view in Manage projects > Filter by customer folder view.");
			else
				Log.fail("Test case Failed. "+dataPool.get("PropertyToCheck")+" property is does not exist in new metadata card from task pane without select any view in Manage projects > Filter by customer folder view.", driver);

		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_19A

	/**
	 * 104.2.19B : Verify if customer property value is prefilled in new metadata card from manubar without select any view in Manage projects > Filter by customer folder view
	 * @param Hash map values and Driver type
	 * @return 
	 * @throws 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if customer property value is prefilled in new metadata card from menubar without select any view in Manage projects > Filter by customer folder view .")
	public void SprintTest104_2_19B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Click new object option from menu bar
			//------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);
			Log.message("3. New Assignment object selected from task pane.");
			MetadataCard metadataCard = new MetadataCard(driver);

			//Verification: To verify if Name field value in new metadata card
			//-----------------------------------------------------------------
			if(!metadataCard.propertyExists(dataPool.get("PropertyToCheck")))
				Log.pass("Test case Passed. "+dataPool.get("PropertyToCheck")+" property is does not exist in new metadata card from menubar without select any view in Manage projects > Filter by customer folder view.");
			else
				Log.fail("Test case Failed. "+dataPool.get("PropertyToCheck")+" property is does not exist in new metadata card from menubar without select any view in Manage projects > Filter by customer folder view.", driver);

		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_19B

	/**
	 * 104.2.20A : Verify if customer property value is prefilled in new metadata card from task pane with select any view in Manage projects > Filter by customer folder view 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if customer property value is prefilled in new metadata card from task pane with select any view in Manage projects > Filter by customer folder view .")
	public void SprintTest104_2_20A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.


			//Step-2 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Select any existing view in list view
			//------------------------------------------------
			homePage.listView.clickItemByIndex(5);  // Select any exiting item in listing view by index value.
			String selectedObject = homePage.listView.getItemNameByItemIndex(5);

			Log.message("2." + selectedObject + " Object selected in listing view.");	

			//Step-4 : Click new assingment object option from menu bar
			//------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Assignment.Value);

			Log.message("3. New Assignment object selected from task pane.");

			/*  String propertyInfo = metadataCard.getInfo().toString();
		    System.out.println("propertyinfo " +propertyInfo);*/

			//Verification: To verify if Name field value in new metadata card
			//-----------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);
			if(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).equalsIgnoreCase(dataPool.get("PropertyValue").trim()))
				Log.pass("Test case Passed. All the default properties are pre-filled in new Assignment metadata card from taskpane in "+ dataPool.get("NavigateToView") + "  view.");
			else 
				Log.fail("Test case Failed. default properties are property value are not displayed / pre-filled in new Assignment metadata card from taskpane in "+ dataPool.get("NavigateToView") + "  view.",driver);
			/*if(propertyInfo.trim().equalsIgnoreCase(dataPool.get("CheckInfo")))
				Log.pass("Test case Passed. All the default properties are pre-filled in new Assignment metadata card from taskpane in "+ dataPool.get("NavigateToView") + "  view.");
			else	
			   	Log.fail("Test case Failed. default properties are property value are not displayed / pre-filled in new Assignment metadata card from taskpane in "+ dataPool.get("NavigateToView") + "  view.",driver);*/

		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_20A

	/**
	 * 104.2.20B : Verify if customer property value is prefilled in new metadata card from menubar with select any view in Manage projects > Filter by customer folder view
	 * @param Hash map values and Driver type
	 * @return 
	 * @throws 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if customer property value is prefilled in new metadata card from menubar with select any view in Manage projects > Filter by customer folder view .")
	public void SprintTest104_2_20B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Select any existing view in list view
			//------------------------------------------------
			homePage.listView.clickItemByIndex(4);  // Select any exiting item in listing view by index value.
			String selectedObject = homePage.listView.getItemNameByItemIndex(4);
			Log.message("3." + selectedObject + " Object selected in listing view.");	

			//Step-4 : Click new assingment object option from menu bar
			//------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);
			Log.message("4. New Assignment object selected from task pane.");
			MetadataCard metadataCard = new MetadataCard(driver);
			/* String propertyInfo = metadataCard.getInfo().toString();
		    System.out.println("propertyinfo " +propertyInfo);*/


			//Verification: To verify if Name field value in new metadata card
			//-----------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).equalsIgnoreCase(dataPool.get("PropertyValue").trim()))
				Log.pass("Test case Passed. All the default properties are pre-filled in new Assignment metadata card from menubar in "+ dataPool.get("NavigateToView") + "  view.");
			else	
				Log.fail("Test case Failed. default properties are property value are not displayed / pre-filled in new Assignment metadata card from menubar in "+ dataPool.get("NavigateToView") + "  view.",driver);
			/*if(propertyInfo.trim().equalsIgnoreCase(dataPool.get("CheckInfo")))
				Log.pass("Test case Passed. All the default properties are pre-filled in new Assignment metadata card from menubar in "+ dataPool.get("NavigateToView") + "  view.");
			else	
			   	Log.fail("Test case Failed. default properties are property value are not displayed / pre-filled in new Assignment metadata card from menubar in "+ dataPool.get("NavigateToView") + "  view.",driver);*/
		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_20B

	/**
	 * 104.2.21A : Verify if customer property is added in Filter by customer view when creating a New Project metadata card from menubar
	 * @param Hash map values and Driver type
	 * @return 
	 * @throws 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if customer property is added in Filter by customer view when creating a Newly created Project from task pane.")
	public void SprintTest104_2_21A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Select any existing view in list view
			//-----------------------------------------------
			homePage.listView.clickItemByIndex(3);  // Select any exiting item in listing view by index value.
			String selectedObject = homePage.listView.getItemNameByItemIndex(3);
			Log.message("3." + selectedObject + " Object selected in listing view.");	

			//Step-4 : Click new contact person object option from menu bar
			//--------------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Project.Value);
			Log.message("4. New Project object selected from menu bar.");
			MetadataCard metadataCard = new MetadataCard(driver);
			/* String propertyInfo = metadataCard.getInfo().toString();
		    System.out.println("Property info:"+propertyInfo);*/

			//Verification: To verify all the default properties value in new metadata card
			//------------------------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).equalsIgnoreCase(dataPool.get("PropertyValue").trim()))
				Log.pass("Test case Passed. All the default properties are pre-filled in new Assignment metadata card from menubar in "+ dataPool.get("NavigateToView") + "  view.");
			else	
				Log.fail("Test case Failed. default properties are property value are not displayed / pre-filled in new Assignment metadata card from menubar in "+ dataPool.get("NavigateToView") + "  view.",driver);

			/* if(propertyInfo.trim().equalsIgnoreCase(dataPool.get("CheckInfo")))
		    	Log.pass("Test case Passed. All the deafult properties dispalyed with default values in new Project metadata card in "+ dataPool.get("NavigateToView") + "  view.");
		    else	
		    	Log.fail("Test case Failed. Customer property value are not pre filled in new Project metadata card in "+ dataPool.get("NavigateToView") + "  view.",driver);*/

		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_21A

	/**
	 * 104.2.21B : Verify if customer property is added in Filter by customer in newly created Contact person object in Manage Projects>>Filter by Customer view
	 * @param Hash map values and Driver type
	 * @return 
	 * @throws 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if customer property is added in Filter by customer in newly created Contact person object in Manage Projects>>Filter by Customer view.")
	public void SprintTest104_2_21B(HashMap<String,String> dataValues, String driverType) throws Exception {
		driver = null; 
		try {
			//Step-1 : Login to MFWA
			//------------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Select any existing view in list view
			//-----------------------------------------------
			homePage.listView.clickItemByIndex(3);  // Select any exiting item in listing view by index value.
			String selectedObject = homePage.listView.getItemNameByItemIndex(3);
			Log.message("3." + selectedObject + " Object selected in listing view.");	

			//Step-4 : Click new contact person object option from menu bar
			//--------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.ContactPerson.Value);
			Log.message("4. New ContactPerson object selected from menu bar.");
			MetadataCard metadataCard = new MetadataCard(driver);

			//Step-5 : Set default property values in new meta data card
			//----------------------------------------------------------
			metadataCard.setInfo(dataPool.get("Properties") + dataPool.get("Object"));
			metadataCard.saveAndClose(); // Save and close the new metadata card.
			Log.message("5. Succesfully created new "+ dataPool.get("Object") +"  object");

			//Step-6 : Search the created object
			//-----------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "");
			homePage.listView.clickItem(dataPool.get("Object"));
			Log.message("6."+ dataPool.get("Object") +"  object got select in listing view.");
			metadataCard = new MetadataCard(driver,true);
			/*String propertyInfo = metadataCard.getInfo().toString();
			System.out.println("Property info:"+propertyInfo);*/

			//Verification: To verify all the default properties value in new metadata card
			//------------------------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).equalsIgnoreCase(dataPool.get("PropertyValue")))
				Log.pass("Test case Passed. All the deafult properties dispalyed with default values in new contact person  metadata card in "+ dataPool.get("NavigateToView") + "  view.");
			else	
				Log.fail("Test case Failed. Customer property value not pre filled in new contact persopn  metadata card in "+ dataPool.get("NavigateToView") + "  view.",driver);

			/*if(propertyInfo.trim().equalsIgnoreCase(dataPool.get("CheckInfo")))
		    	Log.pass("Test case Passed. All the deafult properties dispalyed with default values in new contact person  metadata card in "+ dataPool.get("NavigateToView") + "  view.");
		    else	
		    	Log.fail("Test case Failed. Customer property value not pre filled in new contact persopn  metadata card in "+ dataPool.get("NavigateToView") + "  view.",driver);*/

		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_21B

	/**
	 * 104.2.22A : Verify if customer property is added in Filter by customer view when creating a New Customer from task pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if customer property is added in Filter by customer view when creating a New Customer from task pane.")
	public void SprintTest104_2_22A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Select any existing view in list view
			//------------------------------------------------
			homePage.listView.clickItemByIndex(6);  // Select any exiting item in listing view by index value.
			String selectedObject = homePage.listView.getItemNameByItemIndex(6);
			Log.message("3." + selectedObject + " Object selected in listing view.");	

			//Step-4 : Click new contact person object option from menu bar
			//--------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.ContactPerson.Value);
			Log.message("4. New Contact person object selected from menu bar.");
			MetadataCard metadataCard = new MetadataCard(driver);
			/* String propertyInfo = metadataCard.getInfo().toString();
		    System.out.println("Property info:"+propertyInfo);*/


			//Verification: To verify all the default properties value in new metadata card
			//------------------------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).equalsIgnoreCase(dataPool.get("PropertyValue"))&& !metadataCard.propertyExists(Caption.ObjecTypes.Customer.Value))
				/* if(propertyInfo.trim().equalsIgnoreCase(dataPool.get("CheckInfo")) && !metadataCard.propertyExists(Caption.ObjecTypes.Customer.Value))*/
				Log.pass("Test case Passed. All the deafult properties dispalyed with default values and customer property does not pre-filled in new contact person  metadata card in "+ dataPool.get("NavigateToView") + "  view.");	
			else
				Log.fail("Test case Failed. Customer property value are not pre filled in new customer metadata card in "+ dataPool.get("NavigateToView") + "  view.",driver);
		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_22A

	/**
	 * 104.2.22B : Verify if customer property is added in Filter by customer view when creating a New Customer from menubar
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if customer property is added in Filter by customer view when creating a New Customer from menubar.")
	public void SprintTest104_2_22B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Select any existing view in list view
			//------------------------------------------------
			homePage.listView.clickItemByIndex(7);  // Select any exiting item in listing view by index value.
			String selectedObject = homePage.listView.getItemNameByItemIndex(6);
			Log.message("3." + selectedObject + " Object selected in listing view.");	

			//Step-4 : Click new contact person object option from menu bar
			//--------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.ContactPerson.Value);
			Log.message("4. New Contact person object selected from menu bar.");
			MetadataCard metadataCard = new MetadataCard(driver);
			/* String propertyInfo = metadataCard.getInfo().toString();
		    System.out.println("Property info:"+propertyInfo);*/

			//Verification: To verify all the default properties value in new metadata card
			//------------------------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).equalsIgnoreCase(dataPool.get("PropertyValue")) && !metadataCard.propertyExists(Caption.ObjecTypes.Customer.Value))
				/*   if(propertyInfo.trim().equalsIgnoreCase(dataPool.get("CheckInfo")) && !metadataCard.propertyExists(Caption.ObjecTypes.Customer.Value))*/
				Log.pass("Test case Passed. All the deafult properties dispalyed with default values and customer property does not pre-filled in new contact person  metadata card in "+ dataPool.get("NavigateToView") + "  view.");	
			else
				Log.fail("Test case Failed. Customer property value are not pre filled in new contact persopn  metadata card in "+ dataPool.get("NavigateToView") + "  view.",driver);
		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_22B

	/**
	 * 104.2.23A : Verify if customer property is added in Filter by customer view when opening a New Document from task pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if customer property is added in Filter by customer view when opening a New Document from task pane.")
	public void SprintTest104_2_23A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Select any existing view in list view
			//------------------------------------------------
			homePage.listView.clickItemByIndex(3);  // Select any exiting item in listing view by index value.
			String selectedObject = homePage.listView.getItemNameByItemIndex(3);
			Log.message("3." + selectedObject + " Object selected in listing view.");	

			//Step-4 : Click new Document object option from context menu
			//------------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Document.Value);
			if(!Utility.selectTemplate(dataPool.get("Template"), driver)) 
				throw new Exception("The" + dataPool.get("Template") + "Template not available in document metadata card.");
			Log.message("4."+ dataPool.get("Template") + " template was selected in new docmuent metadata card.");
			MetadataCard metadataCard = new MetadataCard(driver);
			/*  String propertyInfo = metadataCard.getInfo().toString();
		    System.out.println("Property info:"+propertyInfo);*/

			//Verification: To verify all the default properties value in new metadata card
			//------------------------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).equalsIgnoreCase(dataPool.get("PropertyValue"))&& metadataCard.propertyExists(Caption.ObjecTypes.Customer.Value))
				/*    if(propertyInfo.trim().equalsIgnoreCase(dataPool.get("CheckInfo")) && metadataCard.propertyExists(Caption.ObjecTypes.Customer.Value))*/
				Log.pass("Test case Passed. All the deafult properties dispalyed with default values and customer property was pre-filled in new docmuent metadata card from task pane in "+ dataPool.get("NavigateToView") + "  view.");	
			else
				Log.fail("Test case Failed. Customer property value are not pre filled in new Document metadata card from task pane in "+ dataPool.get("NavigateToView") + "  view.",driver);
		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_23A

	/**
	 * 104.2.23B : Verify if customer property is added in Filter by customer view when opening a New Document from menu bar
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if customer property is added in Filter by customer view when opening a New Document from menu bar.")
	public void SprintTest104_2_23B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Select any existing view in list view
			//------------------------------------------------
			homePage.listView.clickItemByIndex(3);  // Select any exiting item in listing view by index value.
			String selectedObject = homePage.listView.getItemNameByItemIndex(3);
			Log.message("3." + selectedObject + " Object selected in listing view.");	

			//Step-4 : Click new Document object option from context menu
			//------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Document.Value);
			if(!Utility.selectTemplate(dataPool.get("Template"), driver)) 
				throw new Exception("The" + dataPool.get("Template") + "Template not available in document metadata card.");
			Log.message("4."+ dataPool.get("Template") + " template was selected in new docmuent metadata card.");
			MetadataCard metadataCard = new MetadataCard(driver);
			/* String propertyInfo = metadataCard.getInfo().toString();
		    System.out.println("Property info:"+propertyInfo);*/

			//Verification: To verify all the default properties value in new metadata card
			//------------------------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).equalsIgnoreCase(dataPool.get("PropertyValue"))&& metadataCard.propertyExists(Caption.ObjecTypes.Customer.Value))
				/*if(propertyInfo.trim().equalsIgnoreCase(dataPool.get("CheckInfo")) && metadataCard.propertyExists(Caption.ObjecTypes.Customer.Value))*/
				Log.pass("Test case Passed. All the deafult properties dispalyed with default values and customer property does not pre-filled in new document from menubar metadata card in "+ dataPool.get("NavigateToView") + "  view.");	
			else
				Log.fail("Test case Failed. "+dataPool.get("PropertyToCheck")+" property value are not pre filled in new document metadata card from menubar in "+ dataPool.get("NavigateToView") + "  view.",driver);
		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_23B

	/**
	 * 104.2.25A : Verify if customer property is added in Filter by customer view when opening a New Document from task pane with blank template option 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if customer property is added in Filter by customer view when opening a New Document from task pane with blank template option .")
	public void SprintTest104_2_25A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Select any existing view in list view
			//------------------------------------------------
			homePage.listView.clickItemByIndex(2);  // Select any exiting item in listing view by index value.
			String selectedObject = homePage.listView.getItemNameByItemIndex(2);
			Log.message("3." + selectedObject + " Object selected in listing view.");	

			//Step-4 : Click new Document object option from context menu
			//------------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Document.Value);
			if(!Utility.selectTemplate(dataPool.get("Template"), driver)) 
				throw new Exception("The" + dataPool.get("Template") + "Template not available in document metadata card.");
			Log.message("4."+ dataPool.get("Template") + " template was selected in new docmuent metadata card.");
			MetadataCard metadataCard = new MetadataCard(driver);
			/*  String propertyInfo = metadataCard.getInfo().toString();
		    System.out.println("Property info:"+propertyInfo);*/

			//Verification: To verify all the default properties value in new metadata card
			//------------------------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).equalsIgnoreCase(dataPool.get("PropertyValue"))&& metadataCard.propertyExists(Caption.ObjecTypes.Customer.Value))
				/*    if(propertyInfo.trim().equalsIgnoreCase(dataPool.get("CheckInfo")) && metadataCard.propertyExists(Caption.ObjecTypes.Customer.Value))*/
				Log.pass("Test case Passed. All the deafult properties dispalyed with default values and customer property was pre-filled in new docmuent  metadata card from taskpane in "+ dataPool.get("NavigateToView") + "  view.");	
			else
				Log.fail("Test case Failed. Customer property value are not pre filled in new contact persopn  metadata card from taskpane in "+ dataPool.get("NavigateToView") + "  view.",driver);
		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_25A

	/**
	 * 104.2.25B : Verify if customer property is added in Filter by customer view when opening a New Document from menu bar with blank template option 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if customer property is added in Filter by customer view when opening a New Document from menu bar with blank template option .")
	public void SprintTest104_2_25B(HashMap<String,String> dataValues, String driverType) throws Exception {
		driver = null; 
		try {
			//Step-1 : Login to MFWA
			//------------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Select any existing view in list view
			//------------------------------------------------
			homePage.listView.clickItemByIndex(2);  // Select any exiting item in listing view by index value.
			String selectedObject = homePage.listView.getItemNameByItemIndex(2);
			Log.message("3." + selectedObject + " Object selected in listing view.");	

			//Step-4 : Click new Document object option from context menu
			//------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Document.Value);
			if(!Utility.selectTemplate(dataPool.get("Template"), driver)) 
				throw new Exception("The" + dataPool.get("Template") + "Template not available in document metadata card.");
			Log.message("4."+ dataPool.get("Template") + " template was selected in new docmuent metadata card.");
			MetadataCard metadataCard = new MetadataCard(driver);
			/*String propertyInfo = metadataCard.getInfo().toString();
			 */
			//Verification: To verify all the default properties value in new metadata card
			//------------------------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).equalsIgnoreCase(dataPool.get("PropertyValue"))&& metadataCard.propertyExists(Caption.ObjecTypes.Customer.Value))
				/*  if(propertyInfo.trim().equalsIgnoreCase(dataPool.get("CheckInfo")) && metadataCard.propertyExists(Caption.ObjecTypes.Customer.Value))*/
				Log.pass("Test case Passed. All the deafult properties dispalyed with default values and customer property was pre-filled in new docmuent  metadata card from menubar in "+ dataPool.get("NavigateToView") + "  view.");	
			else
				Log.fail("Test case Failed. Customer property value are not pre filled in new contact persopn  metadata card from menubar in "+ dataPool.get("NavigateToView") + "  view.",driver);
		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_25B

	/**
	 * 104.2.26A : Verify if customer property is added in Filter by customer view when opening a New Document with selected class from task pane
	 * @param Hash map values and Driver type
	 * @return 
	 * @throws 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if customer property is added in Filter by customer view when opening a New Document with selected class from task pane.")
	public void SprintTest104_2_26A(HashMap<String,String> dataValues, String driverType) throws Exception {
		driver = null; 
		try {
			//Step-1 : Login to MFWA
			//------------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Select any existing view in list view
			//-----------------------------------------------
			homePage.listView.clickItemByIndex(2);  // Select any exiting item in listing view by index value.
			String selectedObject = homePage.listView.getItemNameByItemIndex(2);
			Log.message("3." + selectedObject + " Object selected in listing view.");	

			//Step-4 : Click new Document object option from context menu
			//------------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Document.Value);
			MetadataCard metadataCard = new MetadataCard(driver);
			metadataCard.setTemplateUsingClass(dataPool.get("ClassToSelect"));
			metadataCard = new MetadataCard(driver);
			Log.message("4."+ dataPool.get("ClassToSelect") + " class was selected in new document metadata card.");
			/* String propertyInfo = metadataCard.getInfo().toString();*/


			//Verification: To verify all the default properties value in new metadata card
			//------------------------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).equalsIgnoreCase(dataPool.get("PropertyValue"))&& metadataCard.propertyExists(Caption.ObjecTypes.Customer.Value))
				/*  if(propertyInfo.trim().equalsIgnoreCase(dataPool.get("CheckInfo")) && metadataCard.propertyExists(Caption.ObjecTypes.Customer.Value))*/
				Log.pass("Test case Passed. All the deafult properties dispalyed with default values and customer property was pre-filled in new docmuent  metadata card from taskpane in "+ dataPool.get("NavigateToView") + "  view.");	
			else
				Log.fail("Test case Failed. Customer property value are not pre filled in new contact persopn  metadata card from taskpane in "+ dataPool.get("NavigateToView") + "  view.",driver);
		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_26A

	/**
	 * 104.2.26B : Verify if customer property is added in Filter by customer view when opening a New Document with selected class from manu bar
	 * @param Hash map values and Driver type
	 * @return 
	 * @throws 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if customer property is added in Filter by customer view when opening a New Document with selected class from manubar.")
	public void SprintTest104_2_26B(HashMap<String,String> dataValues, String driverType) throws Exception {
		driver = null; 
		try {
			//Step-1 : Login to MFWA
			//------------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Select any existing view in list view
			//-----------------------------------------------
			homePage.listView.clickItemByIndex(2);  // Select any exiting item in listing view by index value.
			String selectedObject = homePage.listView.getItemNameByItemIndex(2);
			Log.message("3." + selectedObject + " Object selected in listing view.");	

			//Step-4 : Click new Document object option from context menu
			//------------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Document.Value);
			MetadataCard metadataCard = new MetadataCard(driver);
			metadataCard.setTemplateUsingClass(dataPool.get("ClassToSelect"));
			metadataCard = new MetadataCard(driver);
			Log.message("4."+ dataPool.get("ClassToSelect") + " class was selected in new document metadata card.");
			/*String propertyInfo = metadataCard.getInfo().toString();*/
			//ConcurrentHashMap <String, String> propertyInfo = metadataCard.getInfo();



			//Verification: To verify all the default properties value in new metadata card
			//------------------------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).equalsIgnoreCase(dataPool.get("PropertyValue"))&& metadataCard.propertyExists(Caption.ObjecTypes.Customer.Value))
				/*    if(propertyInfo.trim().equalsIgnoreCase(dataPool.get("CheckInfo")) && metadataCard.propertyExists(Caption.ObjecTypes.Customer.Value))*/
				Log.pass("Test case Passed. All the deafult properties are dispalyed with default values and customer property was pre-filled in new docmuent  metadata card from taskpane in "+ dataPool.get("NavigateToView") + "  view.");	
			else
				Log.fail("Test case Failed. Customer property value are not pre filled in new contact persopn  metadata card from taskpane in "+ dataPool.get("NavigateToView") + "  view.",driver);
		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_26B

	/**
	 * 104.2.27 : Verify if customer property is added in Filter by customer view when opening a New Report/Employee/Document collection from menubar
	 * @param Hash map values and Driver type
	 * @return 
	 * @throws 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if customer property is added in Filter by customer view when opening a New Report/Employee/Document collection from taskpane.")
	public void SprintTest104_2_27(HashMap<String,String> dataValues, String driverType) throws Exception {
		driver = null; 
		try {
			//Step-1 : Login to MFWA
			//------------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Select any existing view in list view
			//-----------------------------------------------
			homePage.listView.clickItemByIndex(2);  // Select any exiting item in listing view by index value.
			String selectedObject = homePage.listView.getItemNameByItemIndex(2);
			Log.message("3." + selectedObject + " Object selected in listing view.");	

			//Step-4 : Click new Report object option from manuvbar 
			//-----------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectToCreate"));
			MetadataCard metadataCard = new MetadataCard(driver);
			Log.message("4. Report object was selected .");
			/*String propertyInfo = metadataCard.getInfo().toString();
		    System.out.println("Property info:"+propertyInfo);
			 */
			//Verification: To verify all the default properties value in new metadata card
			//------------------------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).equalsIgnoreCase(dataPool.get("PropertyValue"))&& metadataCard.propertyExists(Caption.ObjecTypes.Customer.Value))
				/*    if(propertyInfo.trim().equalsIgnoreCase(dataPool.get("CheckInfo")) && metadataCard.propertyExists(Caption.ObjecTypes.Customer.Value))*/
				Log.pass("Test case Passed. All the deafult properties are dispalyed with default values and customer property was pre-filled in new Report  metadata card from taskpane in "+ dataPool.get("NavigateToView") + "  view.");	
			else
				Log.fail("Test case Failed. Customer property value are not pre filled in new Report metadata card from taskpane in "+ dataPool.get("NavigateToView") + "  view.",driver);
		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_27

	/**
	 * 104.2.32A : Verify auto fill message displays for selected object and Virtual folder when creating a new object from task pane
	 * @param Hash map values and Driver type
	 * @return 
	 * @throws 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify auto fill message displays for selected object and Virtual folder when creating a new object from task pane.")
	public void SprintTest104_2_32A(HashMap<String,String> dataValues, String driverType) throws Exception {
		driver = null; 
		try {
			//Step-1 : Login to MFWA
			//------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Select any existing object in list view
			//-----------------------------------------------
			homePage.listView.clickItemByIndex(0);  // Select any exiting item in listing view by index value.
			String selectedObject = homePage.listView.getItemNameByItemIndex(0);
			Log.message("3." + selectedObject + " Object selected in listing view.");	

			//Step-4 : Click new Document object option from task pane 
			//--------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Document.Value);
			if(!Utility.selectTemplate(dataPool.get("Template"), driver)) 
				throw new Exception("The" + dataPool.get("Template") + "Template not available in document metadata card.");
			Log.message("4."+ dataPool.get("Template") + " template was selected in new docmuent metadata card.");
			MetadataCard metadataCard = new MetadataCard(driver);

			//Step-5 : Change class value in new document metadata card 
			//----------------------------------------------------------
			metadataCard.setInfo(dataPool.get("Properties"));

			if(!MFilesDialog.exists(driver, "Confirm Autofill"))
				Log.fail("Test case Failed. Autofill dialog not displayed while created new object by selecting existing item in in "+ dataPool.get("NavigateToView") + "  view.",driver);

			Log.message("5. Class changed in new metadata card."); 
			//Step-6 : Click close button in MFiles Auto fill dialog 
			//------------------------------------------------------- 
			MFilesDialog mfilesdialog = new MFilesDialog(driver,"Confirm Autofill");
			mfilesdialog.close();
			Log.message("6. Closed Auto fill dialog.");
			metadataCard = new MetadataCard(driver);
			metadataCard.clickProperty(dataPool.get("Properties").split("::")[0]);
			String propertyInfo = metadataCard.getInfo().toString();

			//Verification: To verify all the default properties value in new metadata card
			//------------------------------------------------------------------------------
			if(propertyInfo.trim().equalsIgnoreCase(dataPool.get("CheckInfo")) && metadataCard.propertyExists(Caption.ObjecTypes.Customer.Value))
				Log.pass("Test case Passed. Autofill dialog is displayed and while close the autofill dialog default properties are pre-filled in new Document metadata card from taskpane in "+ dataPool.get("NavigateToView") + "  view.");	
			else
				Log.fail("Test case Failed. Autofill dialog not displayed while created new object by selecting existing item in in "+ dataPool.get("NavigateToView") + "  view.",driver);
		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_32A

	/**
	 * 104.2.32B : Verify auto fill message displays for selected object and Virtual folder when creating a new object from menubar
	 * @param Hash map values and Driver type
	 * @return 
	 * @throws 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify auto fill message displays for selected object and Virtual folder when creating a new object from menubar.")
	public void SprintTest104_2_32B(HashMap<String,String> dataValues, String driverType) throws Exception {
		driver = null; 
		try {
			//Step-1 : Login to MFWA
			//------------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Select any existing object in list view
			//-----------------------------------------------
			homePage.listView.clickItemByIndex(0);  // Select any exiting item in listing view by index value.
			String selectedObject = homePage.listView.getItemNameByItemIndex(0);
			Log.message("3." + selectedObject + " Object selected in listing view.");	

			//Step-4 : Click new Document object option from menubar 
			//-----------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Document.Value);
			if(!Utility.selectTemplate(dataPool.get("Template"), driver)) 
				throw new Exception("The" + dataPool.get("Template") + "Template not available in document metadata card.");
			Log.message("4."+ dataPool.get("Template") + " template was selected in new docmuent metadata card.");
			MetadataCard metadataCard = new MetadataCard(driver);
			Log.message("5. Class changed in new metadata card."); 

			//Step-5 : Change class value in new document metadata card 
			//----------------------------------------------------------
			metadataCard.setInfo(dataPool.get("Properties"));

			if(!MFilesDialog.exists(driver, "Confirm Autofill"))
				Log.fail("Test case Failed. Autofill dialog not displayed while created new object by selecting existing item in in "+ dataPool.get("NavigateToView") + "  view.",driver);

			//Step-6 : Click close button in MFiles Auto fill dialog 
			//------------------------------------------------------- 
			MFilesDialog mfilesdialog = new MFilesDialog(driver,"Confirm Autofill");
			mfilesdialog.close();
			Log.message("6. Closed Auto fill dialog.");
			metadataCard = new MetadataCard(driver);
			metadataCard.clickProperty(dataPool.get("Properties").split("::")[0]);
			String propertyInfo = metadataCard.getInfo().toString();

			//Verification: To verify all the default properties value in new metadata card
			//------------------------------------------------------------------------------
			if(propertyInfo.trim().equalsIgnoreCase(dataPool.get("CheckInfo")) && metadataCard.propertyExists(Caption.ObjecTypes.Customer.Value))
				Log.pass("Test case Passed. Autofill dialog is displayed and while close the autofill dialog default properties are pre-filled in new Document metadata card from taskpane in "+ dataPool.get("NavigateToView") + "  view.");	
			else
				Log.fail("Test case Failed. Autofill dialog not displayed while created new object by selecting existing item in in "+ dataPool.get("NavigateToView") + "  view.",driver);
		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_32B

	/**
	 * 104.2.33A : Verify if value is are auto filled when clicking 'No' option in auto fill dialog for selected object and Virtual folder when creating a new object from task pane
	 * @param Hash map values and Driver type
	 * @return 
	 * @throws 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if value is are auto filled when clicking 'No' option in auto fill message displays for selected object and Virtual folder when creating a new object from task pane.")
	public void SprintTest104_2_33A(HashMap<String,String> dataValues, String driverType) throws Exception {
		driver = null; 
		try {
			//Step-1 : Login to MFWA
			//------------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Select any existing object in list view
			//-----------------------------------------------
			homePage.listView.clickItemByIndex(0);  // Select any exiting item in listing view by index value.
			String selectedObject = homePage.listView.getItemNameByItemIndex(0);
			Log.message("3." + selectedObject + " Object selected in listing view.");	

			//Step-4 : Click new Document object option from task pane 
			//--------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Document.Value);
			if(!Utility.selectTemplate(dataPool.get("Template"), driver)) 
				throw new Exception("The" + dataPool.get("Template") + "Template not available in document metadata card.");
			Log.message("4."+ dataPool.get("Template") + " template was selected in new docmuent metadata card.");
			MetadataCard metadataCard = new MetadataCard(driver);

			//Step-5 : Change class value in new document metadata card 
			//----------------------------------------------------------
			metadataCard.setInfo(dataPool.get("Properties"));

			if(!MFilesDialog.exists(driver,"Confirm Autofill"))
				Log.fail("Test case Failed. Autofill dialog not displayed while created new object by selecting existing item in in "+ dataPool.get("NavigateToView") + "  view.",driver);

			Log.message("5. Class changed in new metadata card."); 
			//Step-6 : Click close button in MFiles Auto fill dialog 
			//------------------------------------------------------- 
			MFilesDialog mfilesdialog = new MFilesDialog(driver,"Confirm Autofill");
			mfilesdialog.clickCancelButton();
			Log.message("6. Clicked No button in Auto fill dialog.");
			metadataCard = new MetadataCard(driver);
			metadataCard.clickProperty(dataPool.get("Properties").split("::")[0]);

			//Verification: To verify all the default properties value in new metadata card
			//------------------------------------------------------------------------------
			String result = "";

			if(metadataCard.propertyExists(dataPool.get("Property")))
			{
				if(metadataCard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyValue")))
					result = "Property '" + dataPool.get("Property") + "' is set with the value '" + dataPool.get("PropertyValue") + "'";
			}
			else
				result = "Property '" + dataPool.get("Property") + "' is not exists in the metadatacard.";

			if(result.equals(""))
				Log.pass("Test case Passed. Autofill dialog was displayed and while click No button the autofill dialog default properties are not pre-filled in new metadata card from taskpane in "+ dataPool.get("NavigateToView") + "  view.");	
			else
				Log.fail("Test case Failed. Autofill dialog was not displayed and while click No button the autofill dialog default properties are pre-filled in new metadata card from taskpane in "+ dataPool.get("NavigateToView") + "  view. Additional info. : " + result + ".",driver);
		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_33A

	/**
	 * 104.2.33B : Verify if value is are auto filled when clicking 'No' option in auto fill dialog for selected object and Virtual folder when creating a new object from menubar
	 * @param Hash map values and Driver type
	 * @return 
	 * @throws 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if value is are auto filled when clicking 'No' option in auto fill message displays for selected object and Virtual folder when creating a new object from menubar.")
	public void SprintTest104_2_33B(HashMap<String,String> dataValues, String driverType) throws Exception {
		driver = null; 
		try {
			//Step-1 : Login to MFWA
			//------------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Select any existing object in list view
			//-----------------------------------------------
			homePage.listView.clickItemByIndex(0);  // Select any exiting item in listing view by index value.
			String selectedObject = homePage.listView.getItemNameByItemIndex(0);
			Log.message("3." + selectedObject + " Object selected in listing view.");	

			//Step-4 : Click new Document object option from task pane 
			//--------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Document.Value);
			if(!Utility.selectTemplate(dataPool.get("Template"), driver)) 
				throw new Exception("The" + dataPool.get("Template") + "Template not available in document metadata card.");
			Log.message("4."+ dataPool.get("Template") + " template was selected in new docmuent metadata card.");
			MetadataCard metadataCard = new MetadataCard(driver);

			//Step-5 : Change class value in new document metadata card 
			//----------------------------------------------------------
			metadataCard.setInfo(dataPool.get("Properties"));

			if(!MFilesDialog.exists(driver,"Confirm Autofill"))
				Log.fail("Test case Failed. Autofill dialog not displayed while created new object by selecting existing item in in "+ dataPool.get("NavigateToView") + "  view.",driver);

			Log.message("5. Class changed in new metadata card."); 
			//Step-6 : Click close button in MFiles Auto fill dialog 
			//------------------------------------------------------- 
			MFilesDialog mfilesdialog = new MFilesDialog(driver,"Confirm Autofill");
			mfilesdialog.clickCancelButton();
			Log.message("6. Clicked No button in Auto fill dialog.");
			metadataCard = new MetadataCard(driver);
			metadataCard.clickProperty(dataPool.get("Properties").split("::")[0]);

			//Verification: To verify all the default properties value in new metadata card
			//------------------------------------------------------------------------------
			String result = "";

			if(metadataCard.propertyExists(dataPool.get("Property")))
			{
				if(!metadataCard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyValue")))
					result = "Property '" + dataPool.get("Property") + "' is not set with the value '" + dataPool.get("PropertyValue") + "'";
			}
			else
				result = "Property '" + dataPool.get("Property") + "' is not exists in the metadatacard.";

			if(result.equals(""))
				Log.pass("Test case Passed. Autofill dialog was displayed and while click No button the autofill dialog default properties are not pre-filled in new metadata card from taskpane in "+ dataPool.get("NavigateToView") + "  view.");	
			else
				Log.fail("Test case Failed. Autofill dialog was not displayed and while click No button the autofill dialog default properties are pre-filled in new metadata card from taskpane in "+ dataPool.get("NavigateToView") + "  view. Additional info. : " + result + ".",driver);
		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_33B

	/**
	 * 104.2.34A : Verify if value is are auto filled when clicking 'Yes' option in auto fill dialog for selected object and Virtual folder when creating a new object from task pane
	 * @param Hash map values and Driver type
	 * @return 
	 * @throws 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if value is are auto filled when clicking 'Yes' option in auto fill message displays for selected object and Virtual folder when creating a new object from task pane.")
	public void SprintTest104_2_34A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		try {
			//Step-1 : Login to MFWA
			//------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Select any existing object in list view
			//-----------------------------------------------
			homePage.listView.clickItemByIndex(1);  // Select any exiting item in listing view by index value.
			String selectedObject = homePage.listView.getItemNameByItemIndex(1);
			Log.message("3." + selectedObject + " Object selected in listing view.");	

			//Step-4 : Click new Document object option from task pane 
			//--------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Document.Value);
			if(!Utility.selectTemplate(dataPool.get("Template"), driver)) 
				throw new Exception("The" + dataPool.get("Template") + "Template not available in document metadata card.");
			Log.message("4."+ dataPool.get("Template") + " template was selected in new docmuent metadata card.");
			MetadataCard metadataCard = new MetadataCard(driver);

			//Step-5 : Change class value in new document metadata card 
			//----------------------------------------------------------
			metadataCard.setInfo(dataPool.get("Properties"));

			if(!MFilesDialog.exists(driver,"Confirm Autofill"))
				Log.fail("Test case Failed. Autofill dialog not displayed while created new object by selecting existing item in in "+ dataPool.get("NavigateToView") + "  view.",driver);

			Log.message("5. Class changed in new metadata card."); 
			//Step-6 : Click Yes button in MFiles Auto fill dialog 
			//------------------------------------------------------- 
			MFilesDialog mfilesdialog = new MFilesDialog(driver,"Confirm Autofill");
			mfilesdialog.clickOkButton();
			Log.message("6. Clicked Yes button in Auto fill dialog.");
			metadataCard = new MetadataCard(driver);
			metadataCard.clickProperty(dataPool.get("Properties").split("::")[0]);

			//Verification: To verify all the default properties value in new metadata card
			//------------------------------------------------------------------------------
			String result = "";

			if(metadataCard.propertyExists(dataPool.get("Property")))
			{
				if(!metadataCard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyValue")))
					result = "Property '" + dataPool.get("Property") + "' is not set with the value '" + dataPool.get("PropertyValue") + "'";
			}
			else
				result = "Property '" + dataPool.get("Property") + "' is not exists in the metadatacard.";

			if(result.equals(""))
				Log.pass("Test case Passed. Autofill dialog was displayed and while click Yes button the autofill dialog default properties are not pre-filled in new metadata card from taskpane in "+ dataPool.get("NavigateToView") + "  view.");	
			else
				Log.fail("Test case Failed. Autofill dialog was not displayed and while click Yes button the autofill dialog default properties are pre-filled in new metadata card from taskpane in "+ dataPool.get("NavigateToView") + "  view. Additional info. : " + result + ".",driver);
		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_34A

	/**
	 * 104.2.34B : Verify if value is are auto filled when clicking 'Yes' option in auto fill dialog for selected object and Virtual folder when creating a new object from menubar
	 * @param Hash map values and Driver type
	 * @return 
	 * @throws 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if value is are auto filled when clicking 'Yes' option in auto fill message displays for selected object and Virtual folder when creating a new object from menubar.")
	public void SprintTest104_2_34B(HashMap<String,String> dataValues, String driverType) throws Exception {
		driver = null; 
		try {
			//Step-1 : Login to MFWA
			//------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Select any existing object in list view
			//-----------------------------------------------
			homePage.listView.clickItemByIndex(1);  // Select any exiting item in listing view by index value.
			String selectedObject = homePage.listView.getItemNameByItemIndex(1);
			Log.message("3." + selectedObject + " Object selected in listing view.");	

			//Step-4 : Click new Document object option from task pane 
			//--------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Document.Value);
			if(!Utility.selectTemplate(dataPool.get("Template"), driver)) 
				throw new Exception("The" + dataPool.get("Template") + "Template not available in document metadata card.");
			Log.message("4."+ dataPool.get("Template") + " template was selected in new docmuent metadata card.");
			MetadataCard metadataCard = new MetadataCard(driver);

			//Step-5 : Change class value in new document metadata card 
			//----------------------------------------------------------
			metadataCard.setInfo(dataPool.get("Properties"));	    

			if(!MFilesDialog.exists(driver,"Confirm Autofill"))
				Log.fail("Test case Failed. Autofill dialog not displayed while created new object by selecting existing item in in "+ dataPool.get("NavigateToView") + "  view.",driver);

			Log.message("5. Class changed in new metadata card."); 
			//Step-6 : Click close button in MFiles Auto fill dialog 
			//------------------------------------------------------- 
			MFilesDialog mfilesdialog = new MFilesDialog(driver,"Confirm Autofill");
			mfilesdialog.clickOkButton();
			metadataCard = new MetadataCard(driver);
			metadataCard.clickProperty(dataPool.get("Properties").split("::")[0]);
			Log.message("6. Clicked Yes button in Auto fill dialog."); 

			//Verification: To verify all the default properties value in new metadata card
			//------------------------------------------------------------------------------
			String result = "";

			if(metadataCard.propertyExists(dataPool.get("Property")))
			{
				if(!metadataCard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyValue")))
					result = "Property '" + dataPool.get("Property") + "' is not set with the value '" + dataPool.get("PropertyValue") + "'";
			}
			else
				result = "Property '" + dataPool.get("Property") + "' is not exists in the metadatacard.";

			if(result.equals(""))
				Log.pass("Test case Passed. Autofill dialog was displayed and while click Yes button the autofill dialog default properties are not pre-filled in new metadata card from taskpane in "+ dataPool.get("NavigateToView") + "  view.");	
			else
				Log.fail("Test case Failed. Autofill dialog was not displayed and while click Yes button the autofill dialog default properties are pre-filled in new metadata card from taskpane in "+ dataPool.get("NavigateToView") + "  view. Additonal info. : " + result + ".",driver);
		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_34B

	/**
	 * 104.2.35A : Verify new metadata card options from task pane when user multiselects more than one virtual folder in Common view
	 * @param Hash map values and Driver type
	 * @return 
	 * @throws 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint104", "PrefilledProperties"}, 
			description = "Verify new metadata card options for task pane when user multiselects more than one virtual folder in Common view.")
	public void SprintTest104_2_35A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("Safari"))
			throw new SkipException(driverType + " driver does not support multiselect.");

		driver = null; 


		try {
			//Step-1 : Login to MFWA
			//------------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Mulit select existing object in list view using control key
			//--------------------------------------------------------------------
			homePage.listView.clickMultipleItemsByIndex(dataPool.get("Index"));  // Select any exiting item in listing view by index value.
			//String selectedObject = homePage.listView.getItemNameByItemIndex(1);
			Log.message("3. Objects are multi selected in listing view by using control key.");	

			//Step-4 : Click new Document object option from task pane 
			//--------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Document.Value);
			if(!Utility.selectTemplate(dataPool.get("Template"), driver)) 
				throw new Exception("The" + dataPool.get("Template") + "Template not available in document metadata card.");
			Log.message("4."+ dataPool.get("Template") + " template was selected in new docmuent metadata card.");
			MetadataCard metadataCard = new MetadataCard(driver);

			//Step-5 : Change class value in new document metadata card 
			//----------------------------------------------------------
			metadataCard.setInfo(dataPool.get("Properties"));

			if(MFilesDialog.exists(driver,"Confirm Autofill"))
				Log.fail("Test case Failed. Autofill dialog was displayed while created new object by multi select existing items in "+ dataPool.get("NavigateToView") + "  view.",driver);

			Log.message("5. Class changed in new metadata card."); 

			metadataCard = new MetadataCard(driver);
			String propertyInfo = metadataCard.getInfo().toString();
			System.out.println("Property info:"+propertyInfo);

			//Verification: To verify all the default properties value in new metadata card
			//------------------------------------------------------------------------------
			if(propertyInfo.trim().equalsIgnoreCase(dataPool.get("CheckInfo")) && metadataCard.propertyExists(Caption.ObjecTypes.Customer.Value))
				Log.pass("Test case Passed. Default properties are displayed in new metadata card from taskpane with multi selected objects in "+ dataPool.get("NavigateToView") + "  view.");	
			else
				Log.fail("Test case Failed. Default properties are not pre-filled in new metadata card from taskpane in "+ dataPool.get("NavigateToView") + "  view with multi selected objects.",driver);
		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_35A


	/**
	 * 104.2.35B : Verify new metadata card options from menubar when user multiselects more than one virtual folder in Common view
	 * @param Hash map values and Driver type
	 * @return 
	 * @throws 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint104", "PrefilledProperties"}, 
			description = "Verify new metadata card options from manubar when user multiselects more than one virtual folder in Common view.")
	public void SprintTest104_2_35B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("Safari"))
			throw new SkipException(driverType + " driver does not support multiselect.");

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Mulit select existing object in list view using control key
			//--------------------------------------------------------------------
			homePage.listView.clickMultipleItemsByIndex(dataPool.get("Index"));  // Select any exiting item in listing view by index value.
			//String selectedObject = homePage.listView.getItemNameByItemIndex(1);
			Log.message("3. Objects are multi selected in listing view by using control key.");	

			//Step-4 : Click new Document object option from manu bar 
			//--------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Document.Value);
			if(!Utility.selectTemplate(dataPool.get("Template"), driver)) 
				throw new Exception("The" + dataPool.get("Template") + "Template not available in document metadata card.");
			Log.message("4. "+ dataPool.get("Template") + " template was selected in new docmuent metadata card.");
			MetadataCard metadataCard = new MetadataCard(driver);

			//Step-5 : Change class value in new document metadata card 
			//----------------------------------------------------------
			metadataCard.setInfo(dataPool.get("Properties"));

			if(MFilesDialog.exists(driver,"Confirm Autofill"))
				Log.fail("Test case Failed. Autofill dialog was displayed while created new object by multi select existing items in "+ dataPool.get("NavigateToView") + "  view.",driver);

			Log.message("5. Class changed in new metadata card."); 
			metadataCard = new MetadataCard(driver);
			String propertyInfo = metadataCard.getInfo().toString();
			System.out.println("Propertyinfo:"+propertyInfo);

			//Verification: To verify all the default properties value in new metadata card
			//------------------------------------------------------------------------------
			if(propertyInfo.trim().equalsIgnoreCase(dataPool.get("CheckInfo")) && metadataCard.propertyExists(Caption.ObjecTypes.Customer.Value))
				Log.pass("Test case Passed. Default properties are displayed in new metadata card from taskpane with multi selected objects in "+ dataPool.get("NavigateToView") + "  view.");	
			else
				Log.fail("Test case Failed. Default properties are not pre-filled in new metadata card from taskpane in "+ dataPool.get("NavigateToView") + "  view with multi selected objects.",driver);
		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_35B

	/**
	 * 104.2.36 : Verify new document metadata card properties when user multiselects more than one object by shift key in Manage projects view
	 * @param Hash map values and Driver type
	 * @return 
	 * @throws 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint104", "PrefilledProperties"}, 
			description = "Verify new document metadata card properties when user multiselects more than one object by shift key in Manage projects view.")
	public void SprintTest104_2_36(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("Safari"))
			throw new SkipException(driverType + " driver does not support multiselect.");

		driver = null; 
		try {
			//Step-1 : Login to MFWA
			//------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Mulit select existing object in list view using control key
			//--------------------------------------------------------------------
			homePage.listView.shiftclickMultipleItemsByIndex(Integer.parseInt(dataPool.get("StartIndex")),Integer.parseInt(dataPool.get("EndIndex")));  // Select any exiting item in listing view by index value.
			//String selectedObject = homePage.listView.getItemNameByItemIndex(1);
			Log.message("3. Objects are multi selected in listing view by using Shift key.");	

			//Step-4 : Click new Document object option from task pane 
			//--------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Document.Value);
			if(!Utility.selectTemplate(dataPool.get("Template"), driver)) 
				throw new Exception("The" + dataPool.get("Template") + "Template not available in document metadata card.");
			Log.message("4."+ dataPool.get("Template") + " template was selected in new docmuent metadata card.");
			MetadataCard metadataCard = new MetadataCard(driver);

			//Step-5 : Change class value in new document metadata card 
			//----------------------------------------------------------
			metadataCard.setInfo(dataPool.get("Properties"));

			if(MFilesDialog.exists(driver,"Confirm Autofill"))
				Log.fail("Test case Failed. Autofill dialog was displayed while created new object by multi select existing items in "+ dataPool.get("NavigateToView") + "  view.",driver);

			Log.message("5. Class changed in new metadata card."); 

			metadataCard = new MetadataCard(driver);
			String propertyInfo = metadataCard.getInfo().toString();
			System.out.println("propertyInfo" +propertyInfo);

			//Verification: To verify all the default properties value in new metadata card
			//------------------------------------------------------------------------------
			if(propertyInfo.trim().equalsIgnoreCase(dataPool.get("CheckInfo")) && metadataCard.propertyExists(Caption.ObjecTypes.Customer.Value))
				Log.pass("Test case Passed. Default properties are displayed with empty values in new metadata card from taskpane with multi selected objects using shift key in "+ dataPool.get("NavigateToView") + "  view.");	
			else
				Log.fail("Test case Failed. Default properties are not pre-filled in new metadata card from taskpane in "+ dataPool.get("NavigateToView") + "  view with multi selected objects.",driver);
		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_36

	/**
	 * 104.2.37 : Verify created Assignment object properties when user multiselects more than one object using shift key in Manage projects view
	 * @param Hash map values and Driver type
	 * @return 
	 * @throws 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint104", "PrefilledProperties"}, 
			description = "Verify created Assignment object properties when user multiselects more than one object using shift key in Manage projects view.")
	public void SprintTest104_2_37(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("Safari"))
			throw new SkipException(driverType + " driver does not support multiselect.");

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Mulit select existing object in list view using control key
			//--------------------------------------------------------------------
			homePage.listView.shiftclickMultipleItemsByIndex(Integer.parseInt(dataPool.get("StartIndex")),Integer.parseInt(dataPool.get("EndIndex")));  // Select any exiting item in listing view by index value.
			String[] selectedItems = homePage.listView.getSelectedListViewItems().split("\n");//Gets the selected items in the  view
			//String selectedObject = homePage.listView.getItemNameByItemIndex(1);
			Log.message("3. Objects are multi selected in listing view by using Shift key.");	

			//Step-4 : Click new Document object option from task pane 
			//--------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Assignment.Value);
			Log.message("4. New assignemnt option clicked from task pane.");
			MetadataCard metadataCard = new MetadataCard(driver);
			metadataCard.setInfo(dataPool.get("Properties")+dataPool.get("Object"));
			metadataCard.saveAndClose(); // Save and close the new metadata card.
			Log.message("5. Clicked Assignment option from task pane and set the necessary property values.");

			//Step-6 . Search the created Document object
			//--------------------------------------------
			// homePage.searchPanel.search(dataPool.get("Object"),"");
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new Exception("The Object was not created.");
			Log.message("6. Select the created Assignment object.");
			homePage.listView.clickItem(dataPool.get("Object"));
			metadataCard = new MetadataCard(driver,true); 		
			/*String propertyInfo = metadataCard.getInfo().toString();
			System.out.println("propertyInfo" +propertyInfo);*/

			String[] actualItems = metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).split("\n");//Gets the property value in the metadata card
			String result = "";
			for (int i = 0; i < selectedItems.length; i++)
				for (int j = 0; j < actualItems.length; j++)
				{
					if (selectedItems[i].equalsIgnoreCase(actualItems[j]))
						break;

					if (j == (actualItems.length-1))
						result += selectedItems[i] +";";
				}


			//Verification: To verify all the default properties value in new metadata card
			//------------------------------------------------------------------------------
			if(result.equalsIgnoreCase(""))
				/*  if(propertyInfo.trim().equalsIgnoreCase(dataPool.get("CheckInfo")) && metadataCard.propertyExists(dataPool.get("PropertyToCheck")))*/
				Log.pass("Test case Passed. Selected project values are prefilled in new assignment metadata card in "+ dataPool.get("NavigateToView") + "  view.");	
			else
				Log.fail("Test case Failed. Selected project values are not prefilled in new assignment metadata card in "+ dataPool.get("NavigateToView") + "  view with multi selected objects. Additonal info. : Selected items( " + result + " ) not exists in the property( " + dataPool.get("PropertyToCheck") + " ).",driver);
		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_37

	/**
	 * 104.2.41 : Verify if selected value in '1. Documents\Purchase Invoices' view is displayed in newly created object
	 * @param Hash map values and Driver type
	 * @return 
	 * @throws 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties","Bug"}, 
			description = "Verify if selected value in '1. Document>>Purchase Invoices' view is displayed in newly created object.")
	public void SprintTest104_2_41(HashMap<String,String> dataValues, String driverType) throws Exception {
		driver = null; 
		try {
			//Step-1 : Login to MFWA
			//------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Mulit select existing object in list view using control key
			//--------------------------------------------------------------------
			homePage.listView.removeColumn("Workflow State");
			homePage.listView.clickItem(dataPool.get("ItemToClick"));
			//String selectedObject = homePage.listView.getItemNameByItemIndex(1);
			Log.message("3. "+ dataPool.get("ItemToClick") +" object was selected in listing view.");	

			//Step-4 : Click new object option from menubar
			//----------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectToCreate"));
			Log.message("4. New "+ dataPool.get("ObjectToCreate") + " option clicked from task pane.");

			if(MFilesDialog.exists(driver,"Confirm Autofill")) // Verify Auto fill dialog displayed while creating a new object
				Log.fail("Test case Failed. Autofill dialog was displayed while created new object by select existing items in "+ dataPool.get("NavigateToView") + "  view.",driver);

			//Step-5 : Retrive metadacard property details
			//---------------------------------------------			 
			MetadataCard metadataCard = new MetadataCard(driver); // Instantiate metadata card wrapper
			String propertyInfo = metadataCard.getInfo().toString();
			System.out.println("property info " +propertyInfo);

			//Verification: To verify selected object prefilled and default properties value in new metadata card
			//----------------------------------------------------------------------------------------------------
			if(!metadataCard.propertyExists(Caption.ObjecTypes.Document.Value) && propertyInfo.trim().equalsIgnoreCase(dataPool.get("CheckInfo")))
				Log.pass("Test case Passed. "+ dataPool.get("ItemToClick") +" object value was not prefilled in new "+ dataPool.get("ObjectToCreate") +" metadata card in "+ dataPool.get("NavigateToView") + "  view.");	
			else
				Log.fail("Test case Failed. "+ dataPool.get("ItemToClick") +" object value was prefilled in new "+ dataPool.get("ObjectToCreate") +" metadata card in "+ dataPool.get("NavigateToView") + "  view.",driver);
		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_41

	/**
	 * 104.2.42 : Verify if Proposal class value is prefilled  in new document metadata card in '1. Documents\Proposals by Year and Month' view
	 * @param Hash map values and Driver type
	 * @return 
	 * @throws 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if Proposal class value is prefilled  in new document metadata card in '1. Documents>>Proposals by Year and Month' view.")
	public void SprintTest104_2_42(HashMap<String,String> dataValues, String driverType) throws Exception {
		driver = null; 
		try {
			//Step-1 : Login to MFWA
			//------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Mulit select existing object in list view using control key
			//--------------------------------------------------------------------
			homePage.listView.clickItemByIndex(Integer.parseInt(dataPool.get("Index")));
			String selectedObject = homePage.listView.getItemNameByItemIndex(Integer.parseInt(dataPool.get("Index")));
			Log.message("3. "+ selectedObject +" object was selected in listing view.");	

			//Step-4 : Click new object option from menubar
			//----------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectToCreate"));
			Log.message("4. New "+ dataPool.get("ObjectToCreate") + " option clicked from task pane.");


			//Step-5 : Retrive metadacard property details
			//---------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); // Instantiate metadata card wrapper
			metadataCard.clickNextBtn();

			metadataCard = new MetadataCard(driver); // Instantiate new metadata card  
			String propertyInfo = metadataCard.getInfo().toString();

			//Verification: To verify selected object prefilled and default properties value in new metadata card
			//----------------------------------------------------------------------------------------------------
			if(metadataCard.propertyExists(dataPool.get("PropertyToCheck")) && propertyInfo.trim().equalsIgnoreCase(dataPool.get("CheckInfo")))
				Log.pass("Test case Passed. Proposal class and proposal number property was prefilled in new "+ dataPool.get("ObjectToCreate") +" metadata card in "+ dataPool.get("NavigateToView") + "  view.");	
			else
				Log.fail("Test case Failed. Proposal class and proposal number property was not prefilled in new "+ dataPool.get("ObjectToCreate") +" metadata card in "+ dataPool.get("NavigateToView") + "  view.",driver);
		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_42


	/**
	 * 104.2.43 : Verify if class value prefilled base on selected  virtual folder value  in newly created object
	 * @param Hash map values and Driver type
	 * @return 
	 * @throws 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if class value prefilled base on selected  virtual folder value  in newly created object.")
	public void SprintTest104_2_43(HashMap<String,String> dataValues, String driverType) throws Exception {
		driver = null; 
		try {
			//Step-1 : Login to MFWA
			//------------------------
			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Mulit select existing object in list view using control key
			//--------------------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ItemToClick"));
			Log.message("3. "+ dataPool.get("ItemToClick") +" object was selected in listing view.");	

			//Step-4 : Click new object option from menubar
			//----------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectToCreate"));
			Log.message("4. New "+ dataPool.get("ObjectToCreate") + " option clicked from task pane.");

			//Step-5 : Retrive metadacard property details
			//---------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); // Instantiate metadata card wrapper
			metadataCard.clickNextBtn();

			metadataCard = new MetadataCard(driver); // Instantiate new metadata card  

			//Verification: To verify class was prefilled based on selected virtual folder in new metadata card
			//----------------------------------------------------------------------------------------------------
			if(	metadataCard.getPropertyValue(dataPool.get("PropertyToCheck")).equalsIgnoreCase(dataPool.get("ItemToClick")))
				Log.pass("Test case Passed. "+ dataPool.get("PropertyToCheck") +" property was prefilled with "+ dataPool.get("ItemToClick") +" in new "+ dataPool.get("ObjectToCreate") +" metadata card in "+ dataPool.get("NavigateToView") + "  view.");	
			else
				Log.fail("Test case Failed. "+ dataPool.get("ItemToClick") +" class was not prefilled with "+ dataPool.get("ItemToClick") +" in new "+ dataPool.get("ObjectToCreate") +" metadata card in "+ dataPool.get("NavigateToView") + "  view.",driver);
		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_43

	/**
	 * 104.2.47 : Verify prefilled property value for new Assignment when creating via New menu in view folder which has defined filter
	 * @param Hash map values and Driver type 
	 * @return 
	 * @throws 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify prefilled property value for new Assignment when creating via New menu in view folder which has defined filter.")
	public void SprintTest104_2_47(HashMap<String,String> dataValues, String driverType) throws Exception {
		driver = null; 
		try {
			//Step-1 : Login to MFWA
			//------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Mulit select existing object in list view using control key
			//--------------------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ItemToClick"));
			Log.message("3. "+ dataPool.get("ItemToClick") +" object was selected in listing view.");	

			//Step-4 : Click new object option from menubar
			//----------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectToCreate"));
			Log.message("4. New "+ dataPool.get("ObjectToCreate") + " option clicked from task pane.");

			//Step-5 : Retrive metadacard property details
			//---------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); // Instantiate metadata card wrapper
			String propertyInfo = metadataCard.getInfo().toString();
			System.out.println("propertyInfo is : "+propertyInfo);

			//Verification: To verify property prefilled based on selected virtual folder in new metadata card
			//----------------------------------------------------------------------------------------------------
			if(propertyInfo.trim().equalsIgnoreCase(dataPool.get("CheckInfo")) && metadataCard.propertyExists(Caption.ObjecTypes.Document.Value))
				Log.pass("Test case Passed. "+ dataPool.get("ItemToClick") +" property was prefilled and default properties are displayed in new "+ dataPool.get("ObjectToCreate") +" metadata card in "+ dataPool.get("NavigateToView") + "  view.");	
			else
				Log.fail("Test case Failed. "+ dataPool.get("ItemToClick") +" property was not prefilled or default properties are not displayed in new "+ dataPool.get("ObjectToCreate") +" metadata card in "+ dataPool.get("NavigateToView") + "  view.",driver);
		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_47

	/**
	 * 104.2.51A : Verify if MFD object prefilled while creating a new object in MFD view
	 * @param Hash map values and Driver type
	 * @return 
	 * @throws 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if MFD object prefilled while creating a new object in MFD view.")
	public void SprintTest104_2_51A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Select any MFD object and double click on it to open the MFD View
			//---------------------------------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "");
			homePage.listView.doubleClickItem(dataPool.get("Object"));
			Log.message("2. "+ dataPool.get("Object") +"  object was search and double clicked on the item in listing view.");

			//Step-3 : Without select any existing item in listing view click New Assignment option from task pane
			//-----------------------------------------------------------------------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectToCreate"));
			Log.message("3. New "+ dataPool.get("ObjectToCreate") + " option clicked from task pane.");

			//Step-5 : Retrive metadacard property details
			//---------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); // Instantiate metadata card wrapper
			String propertyInfo = metadataCard.getInfo().toString(); 
			System.out.println("propertyInfo : "+propertyInfo);

			//Verification: To verify property was prefilled based on selected object in new metadata card
			//----------------------------------------------------------------------------------------------------
			// If user creates new assignment object the document property value should be prefilled in new metadata card.
			if(dataPool.get("ObjectToCreate").equalsIgnoreCase(Caption.ObjecTypes.Assignment.Value)){
				if(propertyInfo.trim().equalsIgnoreCase(dataPool.get("CheckInfo")) && metadataCard.propertyExists(Caption.ObjecTypes.Document.Value))
					Log.pass("Test case Passed. Document property was prefilled by "+ dataPool.get("Object") +" value and default properties are displayed in new "+ dataPool.get("ObjectToCreate") +" metadata card in "+ dataPool.get("NavigateToView") + "  view.");	
				else
					Log.fail("Test case Failed. Document property was not prefilled by "+ dataPool.get("Object") +" value or default properties are not displayed in new "+ dataPool.get("ObjectToCreate") +" metadata card in "+ dataPool.get("NavigateToView") + "  view.",driver);
			} 
			else { // If user creates other than assignment objects eg: Customer, Project the document property value should not prefilled in new metadata card.
				if(propertyInfo.trim().equalsIgnoreCase(dataPool.get("CheckInfo")) && !metadataCard.propertyExists(Caption.ObjecTypes.Document.Value))
					Log.pass("Test case Passed. Document property was not prefilled by "+ dataPool.get("Object") +" value and default properties are displayed in new "+ dataPool.get("ObjectToCreate") +" metadata card in "+ dataPool.get("NavigateToView") + "  view.");	
				else
					Log.fail("Test case Failed. Document property was prefilled by "+ dataPool.get("Object") +" value or default properties are not displayed in new "+ dataPool.get("ObjectToCreate") +" metadata card in "+ dataPool.get("NavigateToView") + "  view.",driver);
			}
		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_51A

	/**
	 * 104.2.51B : Verify if MFD object prefilled with version while creating a new object in MFD history view
	 * @param Hash map values and Driver type
	 * @return 
	 * @throws 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if MFD object prefilled with version while creating a new object in MFD history .")
	public void SprintTest104_2_51B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		ConcurrentHashMap <String, String> dataPool = null;
		HomePage homePage = null;

		try {
			//Step-1 : Login to MFWA
			//------------------------


			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);
			homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Select any MFD object and Checkout the object for increasing version in history view
			//---------------------------------------------------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "");
			homePage.listView.clickItem(dataPool.get("Object")); // Select any object in list view
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); // Click Checkout option from task pane
			Log.message("2. "+ dataPool.get("Object") +"  object was selected and clicked checkout option from task pane.");

			//Step-3 : Navigate to History view 
			//----------------------------------
			homePage.taskPanel.clickItem("History"); //  Click History option from task pane
			Log.message("3. Navigate to "+ dataPool.get("Object") +" object History view.");

			//Step-4 : Select older version and double click on it for navigate to MFD view  
			//------------------------------------------------------------------------------
			homePage.listView.doubleClickItemByIndex(Integer.parseInt(dataPool.get("Index"))); // Select older version object and double click on it
			Log.message("4. "+ dataPool.get("Object") +"  object older version was selected and double clicked on it in listing view.");

			//Step-5 : Without select any existing item click New object from task pane
			//--------------------------------------------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectToCreate"));
			Log.message("5. New "+ dataPool.get("ObjectToCreate") + " option clicked from task pane.");

			//Step-5 : Retrive metadacard property details
			//---------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); // Instantiate metadata card wrapper
			metadataCard = new MetadataCard(driver); // Instantiate metadata card wrapper
			String propertyInfo = metadataCard.getInfo().toString(); 
			System.out.println("PropertyInfo : "+propertyInfo);

			//Verification: To verify class property value and document property was prefilled with object version in new metadata card
			//----------------------------------------------------------------------------------------------------
			// If user creates new assignment object the document property value should be prefilled in new metadata card.
			if(dataPool.get("ObjectToCreate").equalsIgnoreCase(Caption.ObjecTypes.Assignment.Value)){
				if(propertyInfo.trim().equalsIgnoreCase(dataPool.get("CheckInfo")) && metadataCard.propertyExists(Caption.ObjecTypes.Document.Value))
					Log.pass("Test case Passed. Document property was prefilled by "+ dataPool.get("Object") +" value and default properties are displayed in new "+ dataPool.get("ObjectToCreate") +" metadata card in "+ dataPool.get("NavigateToView") + "  view.");	
				else
					Log.fail("Test case Failed. Document property was not prefilled by "+ dataPool.get("Object") +" value or default properties are not displayed in new "+ dataPool.get("ObjectToCreate") +" metadata card in "+ dataPool.get("NavigateToView") + "  view.",driver);
			} 
			else { // If user creates other than assignment objects eg: Customer, Project the document property value should not prefilled in new metadata card.
				if(propertyInfo.trim().equalsIgnoreCase(dataPool.get("CheckInfo")) && !metadataCard.propertyExists(Caption.ObjecTypes.Document.Value))
					Log.pass("Test case Passed. Document property was not prefilled by "+ dataPool.get("Object") +" value and default properties are displayed in new "+ dataPool.get("ObjectToCreate") +" metadata card in "+ dataPool.get("NavigateToView") + "  view.");	
				else
					Log.fail("Test case Failed. Document property was prefilled by "+ dataPool.get("Object") +" value or default properties are not displayed in new "+ dataPool.get("ObjectToCreate") +" metadata card in "+ dataPool.get("NavigateToView") + "  view.",driver);
			}
		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			try{ // Checkin the selected object
				if(driver!=null && homePage!=null){

					MetadataCard metadataCard = new MetadataCard(driver); // Instantiate metadata card wrapper
					metadataCard = new MetadataCard(driver); // Instantiate metadata card wrapper
					metadataCard.cancelAndConfirm(); // Close the metadata card
					homePage.searchPanel.search(dataPool.get("Object"), "");
					homePage.listView.clickItem(dataPool.get("Object")); // Select any object in list view
					homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value); // Click Checkout option from task pane

				}
			}//End try
			catch(Exception e) 	{
				Log.exception(e, driver);
			} //End catch
			finally {	
				Utility.quitDriver(driver);
			} }//End finally
	} //End SprintTest104_2_51B

	/**
	 * 104.2.56A : Verify if property prefilled in new metadata card based on triplet condition from advanced search - without selecting any existing object
	 * @param Hash map values and Driver type
	 * @return 
	 * @throws 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if search property prefilled in new metadata card in triplet condition from advanced search - without selecting any existing object.")
	public void SprintTest104_2_56A(HashMap<String,String> dataValues, String driverType) throws Exception {
		driver = null; 
		try {
			//Step-1 : Login to MFWA
			//------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Set advanced search condition in triplets
			//---------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("SelecteProperty"), dataPool.get("Condition"), dataPool.get("Value"));
			homePage.searchPanel.clickSearch();

			Log.message("2. Expanded advanced search and triplet condition value assigned.");

			//Step-3 : Without select any existing item in listing view click New Assignment option from task pane
			//-----------------------------------------------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectToCreate"));
			Log.message("3. New "+ dataPool.get("ObjectToCreate") + " option clicked from task pane.");

			//Step-5 : Retrive metadacard property details
			//---------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); // Instantiate metadata card wrapper
			if(dataPool.get("ObjectToCreate").equalsIgnoreCase(Caption.ObjecTypes.Document.Value)){ // Check if user create a document object need to select template dialog
				metadataCard.setTemplate(Caption.Template.Template_Blank.Value);
				metadataCard = new MetadataCard(driver);
			}

			//Verification: To verify property was prefilled based on search condition object in new metadata card
			//-----------------------------------------------------------------------------------------------------
			// If condition values is 'is' the triplet property should be prefilled in new metadata card with respective searched value.
			if(dataPool.get("Condition").equalsIgnoreCase("is")){
				if(metadataCard.getPropertyValue(dataPool.get("SelecteProperty")).equalsIgnoreCase(dataPool.get("Value")))
					Log.pass("Test case Passed. "+ dataPool.get("SelecteProperty") +" property value was prefilled "+ dataPool.get("Value") + " in new "+ dataPool.get("ObjectToCreate") +" metadata card.");	
				else
					Log.fail("Test case Failed. "+ dataPool.get("SelecteProperty") +" property value was not prefilled "+ dataPool.get("Value") + " in new "+ dataPool.get("ObjectToCreate") +" metadata card.",driver);
			}
			else {
				// Else if condition values is other than 'is' [Eg : is not, contains] the triplet property should not prefilled in new metadata card.
				if(!metadataCard.propertyExists(dataPool.get("SelecteProperty")))
					Log.pass("Test case Passed. "+ dataPool.get("SelecteProperty") +" property was not prefilled "+ dataPool.get("Value") + " in new "+ dataPool.get("ObjectToCreate") +" metadata card.");	
				else
					Log.fail("Test case Failed. "+ dataPool.get("SelecteProperty") +" property value was prefilled "+ dataPool.get("Value") + " in new "+ dataPool.get("ObjectToCreate") +" metadata card.",driver);
			}
		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_56A

	/**
	 * 104.2.56B : Verify if search property prefilled in new metadata card in triplet condition from right pane search - without selecting any existing object
	 * @param Hash map values and Driver type
	 * @return 
	 * @throws 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if search property prefilled in new metadata card in triplet condition from right pane search - without selecting any existing object.")
	public void SprintTest104_2_56B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		ConcurrentHashMap <String, String> dataPool = null;

		try {
			//Logged into MFiles configuration page
			//-------------------------------------------


			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configpage  = LoginPage.launchDriverAndLoginToConfig(driver, false); // Launch the URL and login to MFWA.

			//Step-1 : Click Vault from left panel of Configuration Page
			//---------------------------------------------------------
			configpage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);
			Log.message("1.Vault : " + testVault + " is selected and " + Caption.ConfigSettings.Config_Controls.Value + " is expanded.");


			//Step-2 : Set 'Show' in Search in right pane option 
			//--------------------------------------------------
			configpage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Show.Value);

			if (!configpage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Show.Value))
				throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Show.Value);

			configpage.clickSaveButton();//Click the save button in configuration page
			configpage.clickOKBtnOnSaveDialog();//Click the 'ok' button in configuration page

			Log.message("3. Show " + dataPool.get("Control") + " is enabled and settings are saved.");

			//Check if 'Show' is clicked for search in right pane method
			if(!(configpage.configurationPanel.getVaultCommands(dataPool.get("Control")).equalsIgnoreCase("Show")))
				throw new Exception("Show " + dataPool.get("Control") + " is not enabled.");

			//Step-4 : Logout from configuration page
			//---------------------------------------
			configpage.clickLogOut(); //Logs out from the Configuration page

			Log.message("4. LoggedOut from configuration page.");

			//Step-5 : Login to MFWA
			//------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); // Launch the URL and login to MFWA.

			Log.message("5. Logged into MFWA.");

			//Step-6 : Set advanced search condition in triplets
			//---------------------------------------------------
			homePage.searchPanel.setAdditionalConditionsInRightPane(dataPool.get("SelecteProperty"), dataPool.get("Condition"), dataPool.get("Value"));
			homePage.searchPanel.clickRightPaneSearchButton();

			Log.message("6. Triplet condition values are assigned in right pane search.");

			//Step-7 : Without select any existing item in listing view click New Assignment option from task pane
			//-----------------------------------------------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectToCreate"));

			Log.message("7. New "+ dataPool.get("ObjectToCreate") + " option clicked from task pane.");

			//Step-9 : Retrive metadacard property details
			//---------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); // Instantiate metadata card wrapper
			if(dataPool.get("ObjectToCreate").equalsIgnoreCase(Caption.ObjecTypes.Document.Value)){ // Check if user create a document object need to select template dialog
				metadataCard.setTemplate(Caption.Template.Template_Blank.Value);
				metadataCard = new MetadataCard(driver);
			}

			//Verification: To verify property was prefilled based on search condition object in new metadata card
			//-----------------------------------------------------------------------------------------------------							
			if(dataPool.get("Condition").equalsIgnoreCase("is")){ // If condition values is 'is' the triplet property should be prefilled in new metadata card with respective searched value.
				if(metadataCard.getPropertyValue(dataPool.get("SelecteProperty")).equalsIgnoreCase(dataPool.get("Value")))
					Log.pass("Test case Passed. "+ dataPool.get("SelecteProperty")+" property value was prefilled "+ dataPool.get("Value") + " in new "+ dataPool.get("ObjectToCreate") +" metadata card.");	
				else
					Log.fail("Test case Failed. "+ dataPool.get("SelecteProperty")+" property value was not prefilled "+ dataPool.get("Value") + " in new "+ dataPool.get("ObjectToCreate") +" metadata card.",driver);
			}
			else {
				// Else if condition values is other than 'is' [Eg : is not, contains] the triplet property should not prefilled in new metadata card.
				if(!metadataCard.propertyExists(dataPool.get("SelecteProperty")))
					Log.pass("Test case Passed. "+ dataPool.get("SelecteProperty")+" property was not prefilled "+ dataPool.get("Value") + " in new "+ dataPool.get("ObjectToCreate") +" metadata card.");	
				else
					Log.fail("Test case Failed. "+ dataPool.get("SelecteProperty")+" property value was prefilled "+ dataPool.get("Value") + " in new "+ dataPool.get("ObjectToCreate") +" metadata card.",driver);
			}
		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally { // Disable Show in righ pane option form configuration page
			try{
				if (driver != null) {
					//Step-1 : Login to MFiles configuration page
					//-------------------------------------------
					ConfigurationPage configpage  = LoginPage.launchDriverAndLoginToConfig(driver, false); // Launch the URL and login to MFWA.
					//Step-2: Click Vault from left panel of Configuration Page
					//-------------------------------------------
					configpage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);
					//Step-3 : Disable search in right pane option in configuration page
					//------------------------------------------------------------------
					configpage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Hide.Value);

					if (!configpage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Hide.Value))
						throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Hide.Value);

					configpage.clickSaveButton();
					configpage.clickOKBtnOnSaveDialog();

					//Step-4 : Logout from configuration page
					//---------------------------------------
					configpage.clickLogOut(); //Logs out from the Configuration page
				}
			}

			catch (Exception e){
				Log.exception(e, driver);
			}
			finally{
				Utility.quitDriver(driver);
			}} //End finally
	} //End SprintTest104_2_56B

	/**
	 * 104.2.57A : Verify if search property prefilled in new metadata card - selecting any object in triplet condition from advanced search view
	 *
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if search property prefilled in new metadata card - with select any existing object in triplet condition from advanced search view.")
	public void SprintTest104_2_57A(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null; 

		try {

			//Step-1 : Login to MFWA
			//------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Set advanced search condition in triplets
			//---------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("SelecteProperty"), dataPool.get("Condition"), dataPool.get("Value"));
			homePage.searchPanel.clickSearch();

			Log.message("2. Expanded advanced search and triplet condition value assigned.");

			//Step-3 : Select any existing object in listing view
			//----------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ItemToClick"));
			Log.message("3. "+ dataPool.get("ItemToClick") + " object was clicked in list view.");

			//Step-4 : Clicked New object from menu bar 
			//------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectToCreate"));
			Log.message("4. New "+ dataPool.get("ObjectToCreate") + " option clicked from menu bar.");

			//Step-5 : Retrive metadacard property details
			//---------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); // Instantiate metadata card wrapper
			if(dataPool.get("ObjectToCreate").equalsIgnoreCase(Caption.ObjecTypes.Document.Value)){ // Check if user create a document object need to select template dialog
				metadataCard.setTemplate(Caption.Template.Template_Blank.Value);
				metadataCard = new MetadataCard(driver);
			}
			String propertyInfo = metadataCard.getInfo().toString();
			//Verification: To verify property was prefilled based on selected object in list view 
			//-------------------------------------------------------------------------------------
			// If user create a new assignment object the selected object should be prefilled in new metadata card
			if(dataPool.get("ObjectToCreate").equalsIgnoreCase(Caption.ObjecTypes.Assignment.Value)){
				if(propertyInfo.contains(dataPool.get("ItemToClick")) && !metadataCard.propertyExists(dataPool.get("SelecteProperty")))
					Log.pass("Test case Passed. "+ dataPool.get("SelecteProperty") +" property value was prefilled "+ dataPool.get("Value") + " in new "+ dataPool.get("ObjectToCreate") +" metadata card.");	
				else
					Log.fail("Test case Failed. "+ dataPool.get("SelecteProperty") +" property value was not prefilled "+ dataPool.get("Value") + " in new "+ dataPool.get("ObjectToCreate") +" metadata card.",driver);
			}
			else {
				// Check the searched property was prefilled in new metadat card
				if(metadataCard.getPropertyValue(dataPool.get("SelecteProperty")).equalsIgnoreCase(dataPool.get("Value")))
					Log.pass("Test case Passed. "+ dataPool.get("SelecteProperty") +" property was prefilled "+ dataPool.get("Value") + " in new "+ dataPool.get("ObjectToCreate") +" metadata card.");	
				else
					Log.fail("Test case Failed. "+ dataPool.get("SelecteProperty") +" property value was not prefilled "+ dataPool.get("Value") + " in new "+ dataPool.get("ObjectToCreate") +" metadata card.",driver);
			}
		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_57A

	/**
	 * 104.2.57B : Verify if search property prefilled in new metadata card - selecting any object in triplet condition from advanced search view
	 * Description : If user selects any existing object inlist view the new assignment object should be prefilled by the selecte object. Other than
	 *  Assignment objects [Eg : Project,Report] the selected object should not prefilled but search condition value should be prefilled in metadatard
	 * @param Hash map values and Driver type
	 * @return 
	 * @throws 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if search property prefilled in new metadata card - with select any existing object in triplet condition from advanced search view.")
	public void SprintTest104_2_57B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		ConcurrentHashMap <String, String> dataPool = null;

		try {
			//Step-1 : Login to MFiles configuration page
			//-------------------------------------------


			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configpage  = LoginPage.launchDriverAndLoginToConfig(driver, false); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA configuration page.");

			//Step-2: Click Vault from left panel of Configuration Page
			//-------------------------------------------
			configpage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);
			Log.message("2. Clicked 'Sample vault' from left panel and expand in Configuration Page");


			//Step-3 : Enable search in right pane option in configuration page
			//------------------------------------------------------------------
			configpage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Show.Value);

			if (!configpage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Show.Value))
				throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Show.Value);


			//Step-3 : Enable search in right pane option in configuration page
			//------------------------------------------------------------------

			configpage.clickSaveButton();//Click the 'save' button in configuration page
			configpage.clickOKBtnOnSaveDialog();//Click ok button in save setting dialog in configuration page

			Log.message("3. Show " + dataPool.get("Control") + " is enabled and settings are saved.");

			//Step-4 : Logout from configuration page
			//---------------------------------------
			configpage.clickLogOut(); //Logs out from the Configuration page
			Log.message("5. LoggedOut from configuration page.");

			//Step-6 : Login to MFWA
			//------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); // Launch the URL and login to MFWA.
			Log.message("6. Logged into MFWA." );

			//Step-7 : Set advanced search condition in triplets
			//---------------------------------------------------
			homePage.searchPanel.setAdditionalConditionsInRightPane(dataPool.get("SelecteProperty"), dataPool.get("Condition"), dataPool.get("Value"));
			homePage.searchPanel.clickRightPaneSearchButton();

			Log.message("7. Triplet condition values are assigned in right pane search.");
			//Step-8 : Select any existing object in listing view
			//----------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ItemToClick"));
			Log.message("8. "+ dataPool.get("ItemToClick") + " object was clicked in list view.");

			//Step-9 : click New objects from task pane
			//------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectToCreate"));
			Log.message("9. New "+ dataPool.get("ObjectToCreate") + " option clicked from task pane.");

			//Step-10 : Retrive metadacard property details
			//---------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); // Instantiate metadata card wrapper
			if(dataPool.get("ObjectToCreate").equalsIgnoreCase(Caption.ObjecTypes.Document.Value)){ // Check if user create a document object need to select template dialog
				metadataCard.clickNextBtn();
				metadataCard = new MetadataCard(driver);
			}

			String propertyInfo = metadataCard.getInfo().toString();
			//Verification: To verify property was prefilled based on selected object in list view 
			//-------------------------------------------------------------------------------------
			// If user create a new assignment object the selected object should be prefilled in new metadata card
			if(dataPool.get("ObjectToCreate").equalsIgnoreCase(Caption.ObjecTypes.Assignment.Value)){
				if(propertyInfo.contains(dataPool.get("ItemToClick")) && !metadataCard.propertyExists(dataPool.get("SelecteProperty")))
					Log.pass("Test case Passed. "+ dataPool.get("SelecteProperty") +" property value was prefilled "+ dataPool.get("Value") + " in new "+ dataPool.get("ObjectToCreate") +" metadata card.");	
				else
					Log.fail("Test case Failed. "+ dataPool.get("SelecteProperty") +" property value was not prefilled "+ dataPool.get("Value") + " in new "+ dataPool.get("ObjectToCreate") +" metadata card.",driver);
			}
			else {
				//Check the searched property was prefilled in new metadat card
				if(metadataCard.getPropertyValue(dataPool.get("SelecteProperty")).equalsIgnoreCase(dataPool.get("Value")))
					Log.pass("Test case Passed. "+ dataPool.get("SelecteProperty") +" property was prefilled "+ dataPool.get("Value") + " in new "+ dataPool.get("ObjectToCreate") +" metadata card.");	
				else
					Log.fail("Test case Failed. "+ dataPool.get("SelecteProperty") +" property value was not prefilled "+ dataPool.get("Value") + " in new "+ dataPool.get("ObjectToCreate") +" metadata card.",driver);
			}
		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally { // Disable Show in righ pane option form configuration page
			try{
				if (driver != null) {
					//Step-1 : Login to MFiles configuration page
					//-------------------------------------------

					ConfigurationPage configpage  = LoginPage.launchDriverAndLoginToConfig(driver, false); // Launch the URL and login to MFWA.

					Log.message(" Logged into MFWA configuration page.");

					//Step-2: Click Vault from left panel of Configuration Page
					//-------------------------------------------
					configpage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);

					Log.message("2. Clicked 'Sample vault' from left panel and expand in Configuration Page");


					//Step-3 : Enable search in right pane option in configuration page
					//------------------------------------------------------------------
					configpage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Hide.Value);

					if (!configpage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Hide.Value))
						throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Hide.Value);


					//Step-3 : Disable search in right pane option in configuration page
					//------------------------------------------------------------------
					configpage.clickSaveButton();//Click the save button in Configuration page
					configpage.clickOKBtnOnSaveDialog();//Click the ok button in save settings dialog

					Log.message(" Show " + dataPool.get("Control") + " is disabled and settings are saved.");

					//Step-4 : Logout from configuration page
					//---------------------------------------
					configpage.clickLogOut(); //Logs out from the Configuration page

					Log.message(" LoggedOut from configuration page.");
				}
			}

			catch (Exception e){
				Log.exception(e, driver);
			}
			finally{
				Utility.quitDriver(driver);
			}} //End finally
	} //End SprintTest104_2_57B

	/**
	 * 104.2.58A : Verify in search view if Boolean property value is prefilled in Metadata card - Advanced search
	 * @param Hash map values and Driver type
	 * @return 
	 * @throws 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify in search view if Boolean property value is prefilled in Metadata card - Advanced search.")
	public void SprintTest104_2_58A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {
			//Step-1 : Login to MFWA
			//------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Set advanced search condition in triplets
			//---------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("SelecteProperty"), dataPool.get("Condition"), dataPool.get("Value"));
			homePage.searchPanel.clickSearch();

			Log.message("2. Expanded advanced search and triplet condition value assigned.");

			//Step-3 : Without select any existing item in listing view click New Assignment option from task pane
			//-----------------------------------------------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectToCreate"));
			Log.message("3. New "+ dataPool.get("ObjectToCreate") + " option clicked from task pane.");

			//Step-5 : Retrive metadacard property details
			//---------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); // Instantiate metadata card wrapper
			if(dataPool.get("ObjectToCreate").equalsIgnoreCase(Caption.ObjecTypes.Document.Value)){ // Check if user create a document object need to select template dialog
				metadataCard.setTemplate(Caption.Template.Template_Blank.Value);
				metadataCard = new MetadataCard(driver);
			}

			//Verification: To verify property was prefilled based on search condition object in new metadata card
			//-----------------------------------------------------------------------------------------------------
			// If condition values is 'is' the triplet property should be prefilled in new metadata card with respective searched value.
			if(dataPool.get("Condition").equalsIgnoreCase("is")){
				if(metadataCard.getPropertyValue(dataPool.get("SelecteProperty")).equalsIgnoreCase(dataPool.get("Value")))
					Log.pass("Test case Passed. "+ dataPool.get("SelecteProperty") +" property value was prefilled "+ dataPool.get("Value") + " in new "+ dataPool.get("ObjectToCreate") +" metadata card.");	
				else
					Log.fail("Test case Failed. "+ dataPool.get("SelecteProperty") +" property value was not prefilled "+ dataPool.get("Value") + " in new "+ dataPool.get("ObjectToCreate") +" metadata card.",driver);
			}
			else {
				// Else if condition values is other than 'is' [Eg : is not, contains] the triplet property should not prefilled in new metadata card.
				if(!metadataCard.propertyExists(dataPool.get("SelecteProperty")))
					Log.pass("Test case Passed. "+ dataPool.get("SelecteProperty") +" property was not prefilled "+ dataPool.get("Value") + " in new "+ dataPool.get("ObjectToCreate") +" metadata card.");	
				else
					Log.fail("Test case Failed. "+ dataPool.get("SelecteProperty") +" property value was prefilled "+ dataPool.get("Value") + " in new "+ dataPool.get("ObjectToCreate") +" metadata card.",driver);
			}
		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest104_2_58A

	/**
	 * 104.2.58B : Verify in search view if Boolean property value is prefilled in Metadata card - Right Pane
	 * @param Hash map values and Driver type
	 * @return 
	 * @throws 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify in search view if Boolean property value is prefilled in Metadata card - Right Pane.")
	public void SprintTest104_2_58B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {
			//Step-1 : Login to MFiles configuration page
			//-------------------------------------------


			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configpage  = LoginPage.launchDriverAndLoginToConfig(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA configuration page.");

			//Step-2: Click Vault from left panel of Configuration Page
			//-------------------------------------------
			configpage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);

			Log.message("2. Clicked 'Sample vault' from left panel and expand in Configuration Page");


			//Step-3 : Enable search in right pane option in configuration page
			//------------------------------------------------------------------
			configpage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Show.Value);

			if (!configpage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Show.Value))
				throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Show.Value);


			//Step-3 : Enable search in right pane option in configuration page
			//------------------------------------------------------------------
			configpage.clickSaveButton();//Click the save button in Configuration page
			configpage.clickOKBtnOnSaveDialog();//Click the ok button in save settings dialog

			Log.message("3. Show " + dataPool.get("Control") + " is enabled and settings are saved.");

			//Step-4 : Logout from configuration page
			//---------------------------------------
			configpage.clickLogOut(); //Logs out from the Configuration page

			Log.message("5. LoggedOut from configuration page.");

			//Step-6 : Login to MFWA
			//------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); // Launch the URL and login to MFWA.

			Log.message("6. Logged into MFWA." );

			//Step-7 : Set advanced search condition in triplets
			//---------------------------------------------------
			homePage.searchPanel.setAdditionalConditionsInRightPane(dataPool.get("SelecteProperty"), dataPool.get("Condition"), dataPool.get("Value"));
			homePage.searchPanel.clickRightPaneSearchButton();


			Log.message("7. Triplet condition values are assigned in right pane search.");

			//Step-8 : Without select any existing item in listing view click New Assignment option from task pane
			//-----------------------------------------------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectToCreate"));

			Log.message("8. New "+ dataPool.get("ObjectToCreate") + " option clicked from task pane.");

			//Step-9 : Retrive metadacard property details
			//---------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); // Instantiate metadata card wrapper
			if(dataPool.get("ObjectToCreate").equalsIgnoreCase(Caption.ObjecTypes.Document.Value)){ // Check if user create a document object need to select template dialog
				metadataCard.setTemplate(Caption.Template.Template_Blank.Value);
				metadataCard = new MetadataCard(driver);
			}

			//Verification: To verify property was prefilled based on search condition object in new metadata card
			//-----------------------------------------------------------------------------------------------------
			if(dataPool.get("Condition").equalsIgnoreCase("is")){	// If condition values is 'is' the triplet property should be prefilled in new metadata card with respective searched value.
				if(metadataCard.getPropertyValue(dataPool.get("SelecteProperty")).equalsIgnoreCase(dataPool.get("Value")))
					Log.pass("Test case Passed. "+ dataPool.get("SelecteProperty")+" property value was prefilled "+ dataPool.get("Value") + " in new "+ dataPool.get("ObjectToCreate") +" metadata card.");	
				else
					Log.fail("Test case Failed. "+ dataPool.get("SelecteProperty")+" property value was not prefilled "+ dataPool.get("Value") + " in new "+ dataPool.get("ObjectToCreate") +" metadata card.",driver);
			}
			else {
				// Else if condition values is other than 'is' [Eg : is not, contains] the triplet property should not prefilled in new metadata card.
				if(!metadataCard.propertyExists(dataPool.get("SelecteProperty")))
					Log.pass("Test case Passed. "+ dataPool.get("SelecteProperty")+" property was not prefilled "+ dataPool.get("Value") + " in new "+ dataPool.get("ObjectToCreate") +" metadata card.");	
				else
					Log.fail("Test case Failed. "+ dataPool.get("SelecteProperty")+" property value was prefilled "+ dataPool.get("Value") + " in new "+ dataPool.get("ObjectToCreate") +" metadata card.",driver);
			}
		}//End try
		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch
		finally { // Disable Show in righ pane option form configuration page
			try{
				if (driver != null) {
					//Step-1 : Login to MFiles configuration page
					//-------------------------------------------
					ConfigurationPage configpage  = LoginPage.launchDriverAndLoginToConfig(driver, false); // Launch the URL and login to MFWA.
					//Step-2: Click Vault from left panel of Configuration Page
					//-------------------------------------------
					configpage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);
					//Step-3 : Enable search in right pane option in configuration page
					//------------------------------------------------------------------
					configpage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Hide.Value);

					if (!configpage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Hide.Value))
						throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Hide.Value);

					//Step-3 : Disable search in right pane option in configuration page
					//------------------------------------------------------------------
					configpage.clickSaveButton();
					configpage.clickOKBtnOnSaveDialog();

					//Step-4 : Logout from configuration page
					//---------------------------------------
					configpage.clickLogOut(); //Logs out from the Configuration page
				}
			}

			catch (Exception e){
				Log.exception(e, driver);
			}
			finally{
				Utility.quitDriver(driver);
			}} //End finally
	} //End SprintTest104_2_58B


	/**
	 * 104.2.62 : Verify in search view if multiline property value is prefilled in Metadata card - in Advanced Search
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify in search view if multiline property value is prefilled in Metadata card - in Advanced Search")
	public void SprintTest104_2_62(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Step-1 : Login to MFWA
			//------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");


			//Step-2 : Set advanced search condition in triplets
			//---------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("AdvancedSearchCondition"));
			homePage.searchPanel.clickSearch();

			Log.message("2. Expanded advanced search and triplet condition value assigned.");


			//Step-3 : Without select any existing item in listing view click New Document option from task pane
			//-----------------------------------------------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectToCreate"));
			Log.message("3. New "+ dataPool.get("ObjectToCreate") + " option clicked from task pane.");


			//Step-4 : Retrive metadacard property details
			//---------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); // Instantiate metadata card wrapper
			if(dataPool.get("ObjectToCreate").equalsIgnoreCase(Caption.ObjecTypes.Document.Value)){ // Check if user create a document object need to select template dialog
				metadataCard.clickNextBtn();
				metadataCard = new MetadataCard(driver);
			}	Log.message("4. Click the Next button in MetaDataCard operation");


			//Step-5 : Verified the Multiline Property  is displayed in metadata card
			//----------------------------------------------------------------------
			if(metadataCard.propertyExists(dataPool.get("MultilineProperty")))
				Log.pass("Test case Passed. "+ dataPool.get("MultilineProperty") +" property is displayed in new "+ dataPool.get("ObjectToCreate") +" metadata card.");	
			else
				Log.fail("Test case Failed. "+ dataPool.get("MultilineProperty") +" property is not displayed in new "+ dataPool.get("ObjectToCreate") +" metadata card.",driver);
		}catch(Exception e) {
			Log.exception(e, driver);
		}
		finally {
			Utility.quitDriver(driver);
		} //End finally

	}	
	/**
	 * 104_2_63 : Verify in search view if multiline property value is prefilled in Metadata card - in Right Pane
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify in search view if multiline property value is prefilled in Metadata card - in Right Pane")
	public void SprintTest104_2_63(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		ConcurrentHashMap <String, String> dataPool = null;

		try{
			//Login to MFiles configuration page
			//-------------------------------------------


			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configpage  = LoginPage.launchDriverAndLoginToConfig(driver, false); // Launch the URL and login to MFWA.

			//Step-2: Click Vault from left panel of Configuration Page
			//-------------------------------------------
			configpage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);

			Log.message("2. Clicked 'Sample vault' from left panel and expand in Configuration Page");


			//Step-3 : Enable search in right pane option in configuration page
			//------------------------------------------------------------------
			configpage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Show.Value);

			if (!configpage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Show.Value))
				throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Show.Value);


			//Step-3 : Enable search in right pane option in configuration page
			//------------------------------------------------------------------
			configpage.clickSaveButton();//Click the save button in Configuration page 
			configpage.clickOKBtnOnSaveDialog();//Click the 'ok' button in Save settings dialog

			Log.message("3. Show " + dataPool.get("Control") + " is enabled and settings are saved.");

			//Step-4 : Logout from configuration page
			//---------------------------------------
			configpage.clickLogOut(); //Logs out from the Configuration page
			Log.message("5. LoggedOut from configuration page.");

			//Step-6 : Login to MFWA
			//------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); // Launch the URL and login to MFWA.
			Log.message("6. Logged into MFWA.");

			//Step-7 : Set advanced search condition in triplets
			//---------------------------------------------------
			homePage.searchPanel.setAdditionalConditionsInRightPane(dataPool.get("AdvancedSearchCondition"));
			homePage.searchPanel.clickRightPaneSearchButton();

			Log.message("7. Triplet condition values are assigned in right pane search.");

			//Step-8 : Without select any existing item in listing view click New Assignment option from task pane
			//-----------------------------------------------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectToCreate"));
			Log.message("8. New "+ dataPool.get("ObjectToCreate") + " option clicked from task pane.");

			//Step-9 : Retrive metadacard property details
			//---------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); // Instantiate metadata card wrapper
			if(dataPool.get("ObjectToCreate").equalsIgnoreCase(Caption.ObjecTypes.Document.Value)){ // Check if user create a document object need to select template dialog
				metadataCard.clickNextBtn();
				metadataCard = new MetadataCard(driver);
			}

			//Step-10 : Verified the Multiline Property  is displayed in metadata card
			//----------------------------------------------------------------------
			if(metadataCard.propertyExists(dataPool.get("MultilineProperty")))
				Log.pass("Test case Passed. "+ dataPool.get("MultilineProperty") +" property is displayed in new "+ dataPool.get("ObjectToCreate") +" metadata card.");	
			else
				Log.fail("Test case Failed. "+ dataPool.get("MultilineProperty") +" property is not displayed in new "+ dataPool.get("ObjectToCreate") +" metadata card.",driver);	


		}catch(Exception e){
			Log.exception(e, driver);
		}finally{
			try{
				if (driver != null) {
					//Step-1 : Login to MFiles configuration page
					//-------------------------------------------
					ConfigurationPage configpage  = LoginPage.launchDriverAndLoginToConfig(driver, false); // Launch the URL and login to MFWA.

					//Step-2: Click Vault from left panel of Configuration Page
					//-------------------------------------------
					configpage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);

					//Step-3 : Disable search in right pane option in configuration page
					//------------------------------------------------------------------
					configpage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Hide.Value);

					if (!configpage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Hide.Value))
						throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Hide.Value);

					configpage.clickSaveButton();
					configpage.clickOKBtnOnSaveDialog();

					//Step-4 : Logout from configuration page
					//---------------------------------------
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
	}//End SprintTest104_2_63

	/**
	 * 104_2_64A : Verify in search view if Keyword search with triplet condition value is prefilled in Metadata card - AdvancedSearch
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify in search view if Keyword search with triplet condition value is prefilled in Metadata card - AdvancedSearch")
	public void SprintTest104_2_64A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try{
			//Step-1 : Login to MFWA
			//------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Set the Employee in Search quicklink 
			//----------------------------------
			homePage.searchPanel.setSearchWord(dataPool.get("SearchWord"));
			homePage.searchPanel.clickAdvancedSearch(false);
			Log.message("2. Enter the Object" +dataPool.get("SearchWord") + "in Search Quicklink");


			//Step-3 : Set advanced search condition in triplets
			//---------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("SelectProperty"), dataPool.get("Condition"), dataPool.get("Value"));
			homePage.searchPanel.clickSearch();

			Log.message("2. Expanded advanced search and triplet condition value assigned.");


			//Step-4 : Click the New Object : Document from the TaskPane
			//-----------------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectToCreate"));
			Log.message("3. New "+ dataPool.get("ObjectToCreate") + " option clicked from task pane.");



			//Step-4 : Retrive metadacard property details
			//---------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); // Instantiate metadata card wrapper
			if(dataPool.get("ObjectToCreate").equalsIgnoreCase(Caption.ObjecTypes.Document.Value)){ // Check if user create a document object need to select template dialog
				metadataCard.clickNextBtn();
				metadataCard = new MetadataCard(driver);
			}
			Log.message("4. New "+dataPool.get("ObjectToCreate")+ " is selected and Click the Next Button.");

			//Verification: To verify property was prefilled based on search condition object in new metadata card
			//-----------------------------------------------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("SelectProperty")).equalsIgnoreCase(dataPool.get("Value")))
				Log.pass("Test case Passed. "+ dataPool.get("SelectProperty") +" property value was prefilled "+ dataPool.get("Value") + " in new "+ dataPool.get("ObjectToCreate") +" metadata card.");	
			else
				Log.fail("Test case Failed. "+ dataPool.get("SelectProperty") +" property value was not prefilled "+ dataPool.get("Value") + " in new "+ dataPool.get("ObjectToCreate") +" metadata card.",driver);

		}catch(Exception e){
			Log.exception(e, driver);
		}
		finally{
			Utility.quitDriver(driver);
		}
	} //End finally

	/**
	 * 104_2_64B : Verify in search view if Keyword search with triplet condition value is prefilled in Metadata card - RightPaneSearch
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify in search view if Keyword search with triplet condition value is prefilled in Metadata card - RightPaneSearch")
	public void SprintTest104_2_64B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		ConcurrentHashMap <String, String> dataPool = null;
		try{
			//Step-1 : Login to MFiles configuration page
			//-------------------------------------------


			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configpage  = LoginPage.launchDriverAndLoginToConfig(driver, false); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA configuration page.");

			//Step-2: Click Vault from left panel of Configuration Page
			//-------------------------------------------
			/*XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String VaultName = xmlParameters.getParameter("VaultName");
			configpage.clickVaultFolder(VaultName);
			configpage.expandVaultFolder(VaultName);
			Log.message("2. Clicked 'Sample vault' from left panel and expand in Configuration Page");*/
			configpage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);
			Log.message("2. Clicked 'Sample vault' from left panel and expand in Configuration Page");


			//Step-3 : Enable search in right pane option in configuration page
			//------------------------------------------------------------------
			configpage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Show.Value);

			if (!configpage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Show.Value))
				throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Show.Value);


			/*//Step-3 : Enable search in right pane option in configuration page
			//------------------------------------------------------------------
			configpage.clickSettingsFolder("Controls");
			if (configpage.configurationPanel.getVaultCommands(dataPool.get("Control")).equalsIgnoreCase("Hide")) {
				configpage.chooseConfigurationVaultSettings(driver, dataPool.get("Control"),"controls","Show");*/
			configpage.clickSaveButton();
			configpage.clickOKBtnOnSaveDialog();

			//	}
			Log.message("3. Show " + dataPool.get("Control") + " is enabled and settings are saved.");

			//Step-4 : Logout from configuration page
			//---------------------------------------
			configpage.clickLogOut(); //Logs out from the Configuration page
			Log.message("4. LoggedOut from configuration page.");

			//Step-6 : Login to MFWA
			//------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false); // Launch the URL and login to MFWA.
			Log.message("5. Logged into MFWA.");

			//Step-7 : Set advanced search condition in triplets
			//--------------------------------------------------
			homePage.searchPanel.setAdditionalConditionsInRightPane(dataPool.get("SelectProperty"), dataPool.get("Condition"), dataPool.get("Value"));
			homePage.searchPanel.clickRightPaneSearchButton();//Pending- yet to write a method

			Log.message("6. Triplet condition values are assigned in right pane search.");

			//Step-8 : Without select any existing item in listing view click New Assignment option from task pane
			//-----------------------------------------------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectToCreate"));
			Log.message("7. New "+ dataPool.get("ObjectToCreate") + " option clicked from menu bar.");

			//Step-9 : Retrive metadacard property details
			//---------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); // Instantiate metadata card wrapper
			if(dataPool.get("ObjectToCreate").equalsIgnoreCase(Caption.ObjecTypes.Document.Value)){ // Check if user create a document object need to select template dialog
				metadataCard.clickNextBtn();
				metadataCard = new MetadataCard(driver);
			}

			//Verification: To verify property was prefilled based on search condition object in new metadata card
			//-----------------------------------------------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("SelectProperty")).equalsIgnoreCase(dataPool.get("Value")))
				Log.pass("Test case Passed. "+ dataPool.get("SelectProperty") +" property value was prefilled "+ dataPool.get("Value") + " in new "+ dataPool.get("ObjectToCreate") +" metadata card.");	
			else
				Log.fail("Test case Failed. "+ dataPool.get("SelectProperty") +" property value was not prefilled "+ dataPool.get("Value") + " in new "+ dataPool.get("ObjectToCreate") +" metadata card.",driver);
		}
		catch (Exception e){
			Log.exception(e, driver);
		}
		finally{

			try{
				if (driver != null) {
					//Step-1 : Login to MFiles configuration page
					//-------------------------------------------
					ConfigurationPage configpage  = LoginPage.launchDriverAndLoginToConfig(driver, false); // Launch the URL and login to MFWA.
					Log.message(" Logged into MFWA configuration page.");

					//Step-2: Click Vault from left panel of Configuration Page
					//-------------------------------------------
					/*XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
				String VaultName = xmlParameters.getParameter("VaultName");
				configpage.clickVaultFolder(VaultName);
				configpage.expandVaultFolder(VaultName);
				Log.message(" Clicked 'Sample vault' from left panel and expand in Configuration Page");*/
					configpage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);
					Log.message("2. Clicked 'Sample vault' from left panel and expand in Configuration Page");


					//Step-3 : Enable search in right pane option in configuration page
					//------------------------------------------------------------------
					configpage.configurationPanel.setVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value, Caption.ConfigSettings.Config_Hide.Value);

					if (!configpage.configurationPanel.getVaultCommands(Caption.ConfigSettings.Config_SearchInRightPane.Value).equalsIgnoreCase(Caption.ConfigSettings.Config_Hide.Value))
						throw new Exception("Command (" + Caption.ConfigSettings.Config_SearchInRightPane.Value + ") is not set to " + Caption.ConfigSettings.Config_Hide.Value);


					//Step-3 : Disable search in right pane option in configuration page
					//------------------------------------------------------------------
					//				configpage.clickSettingsFolder("Controls");
					//				if (configpage.configurationPanel.getVaultCommands(dataPool.get("Control")).equalsIgnoreCase("Show")) {
					//					configpage.chooseConfigurationVaultSettings(driver, dataPool.get("Control"),"controls","Hide");
					configpage.clickSaveButton();
					configpage.clickOKBtnOnSaveDialog();

					//	}
					Log.message(" Show " + dataPool.get("Control") + " is enabled and settings are saved.");

					//Step-4 : Logout from configuration page
					//---------------------------------------
					configpage.clickLogOut(); //Logs out from the Configuration page
					Log.message(" LoggedOut from configuration page.");
				}
			}

			catch (Exception e){
				Log.exception(e, driver);
			}
			finally{
				Utility.quitDriver(driver);
			}
		} //End finally
	} //End SprintTest104_2_64B

	/**
	 * 104_2_66 : Verify in search view the mulitselected object value is prefilled in Metadata card.
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties", "SKIP_MultiSelect"}, 
			description = "Verify in search view the mulitselected object value is prefilled in Metadata card.")
	public void SprintTest104_2_66(HashMap<String,String> dataValues, String driverType) throws Exception {
		driver = null; 
		try{
			//Step-1 : Login to MFWA
			//------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Select the Search type as : Search only: customers
			//-----------------------------------------------------------
			homePage.searchPanel.search("", dataPool.get("SearchType"));
			Log.message("2. Navigated to '" + dataPool.get("SearchType") + "' search view."); 

			//Step-3 : Select the multiple item using CTRL key
			//----------------------------------------------------------
			homePage.listView.multiSelectByCtrlKey(3);
			Log.message("3. Select the multiple items using the CTRL key  List View");


			//Step-4 : Click the New Object : Document from the TaskPane
			//-----------------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectToCreate"));
			Log.message("4. New "+ dataPool.get("ObjectToCreate") + " option clicked from task pane.");

			//Step-5 : Retrive metadacard property details
			//---------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); // Instantiate metadata card wrapper
			if(dataPool.get("ObjectToCreate").equalsIgnoreCase(Caption.ObjecTypes.Document.Value)){ // Check if user create a document object need to select template dialog
				metadataCard.selectTemplateFilter("All");
				metadataCard.clickNextBtn();
				metadataCard = new MetadataCard(driver);
			}Log.message("5. Click the Next button in MetaData Card");

			//Verification: To verify property was prefilled based on search condition object in new metadata card
			//-----------------------------------------------------------------------------------------------------
			if(!(metadataCard.propertyExists(dataPool.get("SelectProperty")))){
				Log.pass("Test Case Passed. "+dataPool.get("SelectProperty")+" property value was not prefilled in new "+ dataPool.get("ObjectToCreate") + " metadata card." );	
			}else
				Log.fail("Test Case Failed." +dataPool.get("SelectProperty")+" property value was prefilled in new "+ dataPool.get("ObjectToCreate") + " metadata card.", driver);
		}catch(Exception e){
			Log.exception(e, driver);
		}finally{
			Utility.quitDriver(driver);
		}//End finally
	}//End SprintTest104_2_66

	/**
	 * 	104_2_67 : Verify Triplet option in search view the mulitselecting object  is prefilled in Metadata card
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify Triplet option in search view the mulitselecting object  is prefilled in Metadata card")
	public void SprintTest104_2_67(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try{
			//Step-1 : Login to MFWA
			//------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Set the Employee in Search quicklink 
			//----------------------------------
			homePage.searchPanel.setSearchWord(dataPool.get("SearchWord"));
			homePage.searchPanel.clickAdvancedSearch(false);
			Log.message("2. Enter the Object" +dataPool.get("SearchWord") + "in Search Quicklink");


			//Step-3 : Set advanced search condition in triplets
			//---------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("SelectProperty"), dataPool.get("Condition"), dataPool.get("Value"));
			homePage.searchPanel.clickSearch();

			Log.message("2. Expanded advanced search and triplet condition value assigned.");


			//Step-4 : Click the New Object : Document from the TaskPane
			//-----------------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectToCreate"));
			Log.message("3. New "+ dataPool.get("ObjectToCreate") + " option clicked from task pane.");

			//Step-5 : Retrive metadacard property details
			//---------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); // Instantiate metadata card wrapper
			if(dataPool.get("ObjectToCreate").equalsIgnoreCase(Caption.ObjecTypes.Document.Value)){ // Check if user create a document object need to select template dialog
				metadataCard.selectTemplateFilter("All");
				metadataCard.clickNextBtn();
				metadataCard = new MetadataCard(driver);
			}

			//Verification: To verify property was prefilled based on search condition object in new metadata card
			//-----------------------------------------------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("SelectProperty")).equalsIgnoreCase(dataPool.get("Value")))
				Log.pass("Test case Passed. "+ dataPool.get("SelectProperty") +" property value was prefilled as "+ dataPool.get("Value") + " in new "+ dataPool.get("ObjectToCreate") +" metadata card.");	
			else
				Log.fail("Test case Failed. "+ dataPool.get("SelectProperty") +" property value was not prefilled "+ dataPool.get("Value") + " in new "+ dataPool.get("ObjectToCreate") +" metadata card.",driver);

		}catch(Exception e){
			Log.exception(e, driver);
		}finally{
			Utility.quitDriver(driver);
		}//End finally
	}//End SprintTest104_2_67

	/**
	 * 104_2_68 : Verify in search view when searching with same values in same property is prefilled in Metadata card.
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify in search view when searching with same values in same property is prefilled in Metadata card.")
	public void SprintTest104_2_68(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null; 

		try{
			//Step-1 : Login to MFWA
			//------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Set the Employee in Search quicklink 
			//----------------------------------
			homePage.searchPanel.setSearchWord(dataPool.get("SearchWord"));
			homePage.searchPanel.clickAdvancedSearch(false);
			Log.message("2. Enter the Object " +dataPool.get("SearchWord") + " in Search Quicklink");


			//Step-3 : Set advanced search condition in triplets
			//---------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("AdvancedSearchCondition"));
			homePage.searchPanel.clickSearch();

			Log.message("3. Expanded advanced search and triplet condition value assigned.");

			//Step-4 : Click the New Object : Document from the TaskPane
			//-----------------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectToCreate"));
			Log.message("4. New "+ dataPool.get("ObjectToCreate") + " option clicked from task pane.");


			//Step-5 : Retrive metadacard property details
			//---------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); // Instantiate metadata card wrapper
			if(dataPool.get("ObjectToCreate").equalsIgnoreCase(Caption.ObjecTypes.Document.Value)){ // Check if user create a document object need to select template dialog
				metadataCard.selectTemplateFilter("All");
				metadataCard.clickNextBtn();
				metadataCard = new MetadataCard(driver);
			}
			Log.message("5. Verify the "+dataPool.get("ObjectToCreate")+" is Created");

			//Verification: To verify property was prefilled based on search condition object in new metadata card
			//-----------------------------------------------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("SelectProperty")).equalsIgnoreCase(dataPool.get("Value")))
				Log.pass("Test case Passed. "+ dataPool.get("SelectProperty") +" property same value was prefilled as "+ dataPool.get("Value") + " in new "+ dataPool.get("ObjectToCreate") +" metadata card.");	
			else
				Log.fail("Test case Failed. "+ dataPool.get("SelectProperty") +" property same value  was not prefilled "+ dataPool.get("Value") + " in new "+ dataPool.get("ObjectToCreate") +" metadata card.",driver);
		}catch(Exception e){
			Log.exception(e, driver);
		}finally{
			Utility.quitDriver(driver);
		}//End finally
	}	//End SprintTest104_2_68

	/**104_2_69 : Verify in search view when searching with multiple values in same property is prefilled in Metadata card.
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify in search view when searching with multiple values in same property is prefilled in Metadata card.")
	public void SprintTest104_2_69(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try{
			//Step-1 : Login to MFWA
			//------------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Set the Employee in Search quicklink 
			//----------------------------------
			homePage.searchPanel.setSearchWord(dataPool.get("SearchWord"));
			homePage.searchPanel.clickAdvancedSearch(false);
			Log.message("2. Enter the Object " +dataPool.get("SearchWord") + " in Search Quicklink");


			//Step-3 : Set advanced search condition in triplets
			//---------------------------------------------------
			String rowVal[] = dataPool.get("AdvancedSearchCondition").split("\n"); 
			int rowCt = rowVal.length;
			System.out.println("rowCt : " + rowCt);
			String splitValues[][] = new String[rowCt][3];

			for (int i=0; i<rowCt; i++) {
				String colnVal[] = rowVal[i].split("::");
				splitValues[i][0] = colnVal[0];
				System.out.println("split values are : "+splitValues[i][0]);
				splitValues[i][1] = colnVal[1];
				System.out.println("split values are : "+splitValues[i][1]);
				splitValues[i][2] = colnVal[2];
				System.out.println("split values are : "+splitValues[i][2]);
			}

			homePage.searchPanel.setAdditionalConditions(dataPool.get("AdvancedSearchCondition"));
			homePage.searchPanel.clickSearch();

			Log.message("3. Expanded advanced search and triplet condition value assigned for First and Second dropdown.");

			//Step-4 : Click the New Object : Document from the TaskPane
			//-----------------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectToCreate"));
			Log.message("4. New "+ dataPool.get("ObjectToCreate") + " option clicked from task pane.");


			//Step-5 : Retrive metadacard property details
			//---------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); // Instantiate metadata card wrapper

			if (dataPool.get("ObjectToCreate").equalsIgnoreCase(Caption.ObjecTypes.Document.Value)) { // Check if user create a document object need to select template dialog
				metadataCard.selectTemplateFilter("All");
				metadataCard.clickNextBtn();
				metadataCard = new MetadataCard(driver);
			}
			Log.message("5. New object : " + dataPool.get("ObjectToCreate") + " is Created");


			//Step-6 : Get the Drawing Property with multiple values
			//----------------------------------------------------
			String value = "";

			for (int i=0; i<rowCt; i++) {
				int propertyValueCount = metadataCard.getPropertyFieldsCount(splitValues[i][0]);	
				System.out.println("propertyCount is : " +propertyValueCount);
				String propVal = metadataCard.getPropertyValue(splitValues[i][0]);
				System.out.println("Property value is : "+propVal);
				if (propVal.contains(splitValues[i][2]) && propertyValueCount > 1 ) {
					Log.message("Property " + splitValues[i][0] +" is prefilled as " +splitValues[i][2]);
					value = "multiple";
				} else
					value = "";
				Log.message("Property " + splitValues[i][0] +" is not prefilled as " +splitValues[i][2]);
			}//End for	

			//Step-7 : Verified the Drawing property is prefilled with multiple property values
			//---------------------------------------------------------------------------------
			if (value.equals("multiple")) {
				Log.pass("Test Case Passed. " +splitValues[0][0]+ " Property is prefilled with the multiple values.");
			} else
				Log.fail("Test Case Failed. Property is not prefilled with multiple values.",driver);

		}catch(Exception e){
			Log.exception(e, driver);
		}finally{
			Utility.quitDriver(driver);
		}//End finally
	}//End SprintTest104_2_69

	/**
	 * 104_2_71A : Verify user able to Add history version object property as reference object from selection(History object MFD content view-task pane)
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify user able to Add history version object property as reference object from selection(History object MFD content view-task pane)")
	public void SprintTest104_2_71A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try{

			//Step-1 : Login to MFWA
			//-----------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Click the Item to select
			//----------------------------------
			homePage.listView.clickItem(dataPool.get("ItemToClick"));
			Log.message("3. Click Item : "+ dataPool.get("ItemToClick") + " in List view.");

			//Step-4 : Navigate to History view 
			//----------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);//Increase the version of object
			homePage.taskPanel.clickItem("History"); //  Click History option from task pane
			Log.message("4. Navigate to " + dataPool.get("ItemToClick") + " object History view.");

			//Step-5 : Select older version and double click on it for navigate to MFD view  
			//------------------------------------------------------------------------------
			homePage.listView.doubleClickItemByIndex(Integer.parseInt(dataPool.get("Index"))); // Select older version object and double click on it
			Log.message("5. "+ dataPool.get("ItemToClick") +"  object older version was selected and double clicked on it in listing view.");

			//Step-6 : Without select any existing item click New object from task pane
			//--------------------------------------------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectToCreate"));
			Log.message("6. New "+ dataPool.get("ObjectToCreate") + " option clicked from task pane.");


			//Step-7 : Verify the the metadata option with Version of Object
			//---------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); // Instantiate metadata card wrapper
			String propVal = metadataCard.getPropertyValue("Document");
			System.out.println("Property value is : "+propVal);
			if (propVal.contains(dataPool.get("ItemToClick")) && propVal.contains(dataPool.get("Index"))) {
				Log.message("Test case passed. " + propVal + " older version is displayed in "+ dataPool.get("ObjectToCreate") +"new metadata card." );
			} else
				Log.fail("Test case failed."  + propVal + " older version is not displayed in "+ dataPool.get("ObjectToCreate") +"new metadata card.", driver);
		} catch(Exception e){
			Log.exception(e, driver);
		} finally{
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 104_2_71B : Verify user able to Add history version object property as reference object from selection(History object MFD content view-by Right click)
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"},
			description = "Verify user able to Add history version object property as reference object from selection(History object MFD content view-by Right click)")
	public void SprintTest104_2_71B(HashMap<String,String> dataValues, String driverType) throws Exception {
		driver = null; 
		try{

			//Step-1 : Login to MFWA
			//-----------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to View
			//--------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Click the Item to select
			//----------------------------------
			homePage.listView.clickItem(dataPool.get("ItemToClick"));
			Log.message("3. Click Item : "+ dataPool.get("ItemToClick") + " in List view.");

			//Step-4 : Navigate to History view 
			//----------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);//Increase the version of object
			homePage.listView.rightClickItem(dataPool.get("ItemToClick"));
			Log.message("4. Navigate to " + dataPool.get("ItemToClick") + " object History view.");

			//Step-4a : Verified the Context menu opened
			//------------------------------------------
			if (homePage.listView.contextMenuDisplayed())
				Log.message("4a. Context menu is displayed for " + dataPool.get("ItemToClick") + " Selected Item." );
			else
				Log.fail("Context menu is not displayed for " + dataPool.get("ItemToClick") + " Selected Item.", driver);

			//Step-5 : Click the 'History' from the Contextmenu & double click the older version
			//----------------------------------------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.History.Value); 
			homePage.listView.doubleClickItemByIndex(Integer.parseInt(dataPool.get("Index"))); // Select older version object and double click on it
			Log.message("5. "+ dataPool.get("ItemToClick") +"  object older version was selected and double clicked on it in listing view.");

			//Step-6 : Without select any existing item click New object from task pane
			//--------------------------------------------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectToCreate"));
			Log.message("6. New "+ dataPool.get("ObjectToCreate") + " option clicked from task pane.");

			//Step-7 : Verify the the metadata option Version of Object
			//---------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); // Instantiate metadata card wrapper
			String propVal = metadataCard.getPropertyValue("Document");
			System.out.println("Property value is : "+propVal);

			if (propVal.contains(dataPool.get("ItemToClick")) && propVal.contains(dataPool.get("Index"))) {
				Log.message("Test case passed. Document : " + propVal + " older version is displayed in "+ dataPool.get("ObjectToCreate") +"new metadata card.");
			} else
				Log.fail("Test case failed. Document : "  + dataPool.get("ItemToClick") + " older version is not displayed in "+ dataPool.get("ObjectToCreate") +"new metadata card.", driver);

			//pending validation

		}catch(Exception e){
			Log.exception(e, driver);
		}finally{
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 104_2_73 : Verify if newly created checked out object is visible in Checked out to me view
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if newly created checked out object is visible in Checked out to me view - in list view")
	public void SprintTest104_2_73A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try{

			//Step-1 : Login to MFWA
			//-----------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");


			//Step-2 : Select 'Checked Out to me' in List View and click New object from task pane
			//-------------------------------------------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("NavigateToView"));
			Log.message("2. Select the Item : " +dataPool.get("NavigateToView") );

			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Document.Value);
			if(!Utility.selectTemplate(dataPool.get("Template"), driver)) 
				throw new Exception("The " + dataPool.get("Template") + "Template not available in document metadata card.");
			Log.message("4."+ dataPool.get("Template") + " template was selected in new metadata card.");


			//Step-4 : Set the Property value in metadata card & Uncheck the Check In Immediately
			//-----------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); // Instantiate metadata card wrapper
			metadataCard.setInfo(dataPool.get("Properties")+dataPool.get("Object"));// Set the property value
			metadataCard.setCheckInImmediately(false);// Uncheck the Check In Immediately checkbox
			metadataCard.clickCreateBtn();
			Log.message("4. Enter the Property value and " + dataPool.get("Object") + " is Created");


			//Step-5 : Navigate to 'Check Out to me' view
			//-------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("5. Navigated to '" + viewToNavigate + "' view."); 

			//Step-6 : Verification while creating object is displayed in Check out to me list
			//--------------------------------------------------------------------------------
			if(homePage.listView.isItemExists(dataPool.get("Object")+"."+dataPool.get("Extension"))){
				Log.pass("Test Case Passed. "+ dataPool.get("Object")+"."+dataPool.get("Extension") +" is Displayed in Check out to me list");
			}
			else
				Log.fail("Test Case Failed. "+ dataPool.get("Object")+"."+dataPool.get("Extension") +" is not displayed in Check out to me list", driver);


		}//End try
		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End Sprint104_2_73A


	/**
	 * 104_2_73B : Verify if newly created checked out object is visible in Checked out to me view
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if newly created checked out object is visible in Checked out to me view-from Task Panel")
	public void SprintTest104_2_73B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try{

			//Step-1 : Login to MFWA
			//-----------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");


			//Step-2 : Select 'Checked Out to me' in task pane and click New object from task pane
			//-------------------------------------------------------------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("NavigateToView"));
			//	homePage.taskPanel.clickItem(dataPool.get("ObjectToCreate"));
			Log.message("2. Select the Item " + dataPool.get("NavigateToView") + "from the task pane");


			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Document.Value);
			if(!Utility.selectTemplate(dataPool.get("Template"), driver)) 
				throw new Exception("The " + dataPool.get("Template") + "Template not available in document metadata card.");
			Log.message("4."+ dataPool.get("Template") + " template was selected in new metadata card.");


			//Step-4 : Set the Property value in metadata card & Uncheck the Check In Immediately
			//-----------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); // Instantiate metadata card wrapper
			metadataCard.setInfo(dataPool.get("Properties")+dataPool.get("Object"));// Set the property value
			metadataCard.setCheckInImmediately(false);// Uncheck the Check In Immediately checkbox
			metadataCard.clickCreateBtn();
			Log.message("4. Enter the Property value and " + dataPool.get("Object") + " is Created");


			//Step-5 : Navigate to 'Check Out to me' view
			//-------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("5. Navigated to '" + viewToNavigate + "' view."); 

			//Step-6 : Verification while creating object is displayed in Check out to me list
			//--------------------------------------------------------------------------------
			if(homePage.listView.isItemExists(dataPool.get("Object")+"."+dataPool.get("Extension"))){
				Log.pass("Test Case Passed. "+ dataPool.get("Object")+"."+dataPool.get("Extension") +" is Displayed in Check out to me list");
			}
			else
				Log.fail("Test Case Failed. "+ dataPool.get("Object")+"."+dataPool.get("Extension") +" is not displayed in Check out to me list", driver);


		}//End try
		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End Sprint104_2_73B
	/**
	 * 
	 * 104_2_74A : Verify if newly created object is visible in Favorite view-by listing area
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if newly created object is visible in Favorite view-by listing area")
	public void SprintTest104_2_74A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try{

			//Step-1 : Login to MFWA
			//-----------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");


			//Step-2 : Select 'Checked Out to me' in List View and click New object from task pane
			//-------------------------------------------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("NavigateToView"));
			//	homePage.taskPanel.clickItem(dataPool.get("ObjectToCreate"));
			Log.message("2. Select the Item");


			//Step-3 : click New object from task pane 
			//-----------------------------------------
			//homePage.taskPanel.clickItem(dataPool.get("ObjectToCreate"));
			//Log.message("3. New "+ dataPool.get("ObjectToCreate") + " option clicked from task pane.");

			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Document.Value);
			if(!Utility.selectTemplate(dataPool.get("Template"), driver)) 
				throw new Exception("The" + dataPool.get("Template") + "Template not available in document metadata card.");
			Log.message("4."+ dataPool.get("Template") + " template was selected in new metadata card.");

			//Step-4 : Set the Property value in metadata card & Uncheck the Check In Immediately
			//-----------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); // Instantiate metadata card wrapper
			metadataCard.setInfo(dataPool.get("Properties")+dataPool.get("Object"));// Set the property value
			metadataCard.setCheckInImmediately(true);// Uncheck the Check In Immediately checkbox
			metadataCard.clickCreateBtn();
			Log.message("4. Enter the Property value and " + dataPool.get("Object") + " is Created");


			//Step-5 : Navigate to 'Check Out to me' view
			//-------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("5. Navigated to '" + viewToNavigate + "' view."); 

			//Step-6 : Verification while creating object is displayed in Check out to me list
			//--------------------------------------------------------------------------------
			if(homePage.listView.isItemExists(dataPool.get("Object")+"."+dataPool.get("Extension"))){
				Log.pass("Test Case Passed. "+ dataPool.get("Object")+"."+dataPool.get("Extension") +" is Displayed in List");
			}
			else
				Log.fail("Test Case Failed. "+ dataPool.get("Object") +" is not displayed in list", driver);


		}//End try
		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End Sprint104_2_74A

	/**
	 * 104_2_74B : Verify if newly created object is visible in Favorite view-by taskpanel
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if newly created object is visible in Favorite view-by taskpanel")
	public void SprintTest104_2_74B(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null; 


		try{

			//Step-1 : Login to MFWA
			//-----------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");


			//Step-2 : Select 'Checked Out to me' in List View and click New object from task pane
			//-------------------------------------------------------------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("NavigateToView"));
			//	homePage.taskPanel.clickItem(dataPool.get("ObjectToCreate"));
			Log.message("2. Select the Item from the Task pane");


			//Step-3 : click New object from task pane 
			//-----------------------------------------
			//homePage.taskPanel.clickItem(dataPool.get("ObjectToCreate"));
			//Log.message("3. New "+ dataPool.get("ObjectToCreate") + " option clicked from task pane.");

			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Document.Value);
			if(!Utility.selectTemplate(dataPool.get("Template"), driver)) 
				throw new Exception("The" + dataPool.get("Template") + "Template not available in document metadata card.");
			Log.message("4."+ dataPool.get("Template") + " template was selected in new metadata card.");

			//Step-4 : Set the Property value in metadata card & Uncheck the Check In Immediately
			//-----------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); // Instantiate metadata card wrapper
			metadataCard.setInfo(dataPool.get("Properties")+dataPool.get("Object"));// Set the property value
			metadataCard.setCheckInImmediately(true);// Uncheck the Check In Immediately checkbox
			metadataCard.clickCreateBtn();
			Log.message("4. Enter the Property value and " + dataPool.get("Object") + " is Created");


			//Step-5 : Navigate to 'Check Out to me' view
			//-------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("5. Navigated to '" + viewToNavigate + "' view."); 

			//Step-6 : Verification while creating object is displayed in Check out to me list
			//--------------------------------------------------------------------------------
			if (homePage.listView.isItemExists(dataPool.get("Object")+"."+dataPool.get("Extension"))) {
				Log.pass("Test Case Passed. "+ dataPool.get("Object")+"."+dataPool.get("Extension") +" is Displayed in List");
			}
			else
				Log.fail("Test Case Failed. "+ dataPool.get("Object") +" is not displayed in list", driver);


		}//End try
		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End Sprint104_2_74B

	/**
	 * 104_2_75A : Verify if newly created object is visible in Assigned to me view-by listing area
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if newly created object is visible in Assigned to me view-by listing area")
	public void SprintTest104_2_75A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try{

			//Step-1 : Login to MFWA
			//-----------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");


			//Step-2 : Select 'Checked Out to me' in List View and click New object from task pane
			//-------------------------------------------------------------------------------------------
			if (homePage.listView.clickItem(dataPool.get("NavigateToView"))){
				Log.message("2. Select the Item : " +dataPool.get("NavigateToView"));
			}
			else 
				homePage.taskPanel.clickItem(dataPool.get("NavigateToView"));
			Log.message("2. Select the Item : " +dataPool.get("NavigateToView")+ "From task Pane.");


			//Step-3 : click New object from menubar 
			//-----------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);
			Log.message("3. Create the New Object : Assignment from the menubar.");

			//Step-4 : Set the Property value in metadata card & Uncheck the Check In Immediately
			//-----------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); // Instantiate metadata card wrapper
			metadataCard.setInfo(dataPool.get("Properties")+dataPool.get("Object"));// Set the property value
			metadataCard.setCheckInImmediately(true);// Uncheck the Check In Immediately checkbox
			metadataCard.clickCreateBtn();
			Log.message("4. Enter the Property value and " + dataPool.get("Object") + " is Created");


			//Step-5 : Navigate to 'Assigned to me' view
			//-------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("5. Navigated to '" + viewToNavigate + "' view."); 

			//Step-6 : Verification while creating object is displayed in Check out to me list
			//--------------------------------------------------------------------------------
			if(homePage.listView.isItemExists(dataPool.get("Object"))){
				Log.pass("Test Case Passed. "+ dataPool.get("Object")+" is Displayed in List");
			}
			else
				Log.fail("Test Case Failed. "+ dataPool.get("Object") +" is not displayed in list", driver);

		}//End try
		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End Sprint104_2_75A

	/**
	 * 104_2_75B : Verify if newly created object is visible in Assigned to me view-by taskpanel area
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Verify if newly created object is visible in Assigned to me view-by taskpanel area")
	public void SprintTest104_2_75B(HashMap<String,String> dataValues, String driverType) throws Exception {
		driver = null; 
		try{

			//Step-1 : Login to MFWA
			//-----------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");


			//Step-2 : Select 'Assigned to me' in List View and click New object from task pane
			//-------------------------------------------------------------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("NavigateToView"));
			Log.message("2. Select the Item from Task panel.");


			//Step-3 : click New object from menubar 
			//-----------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);
			Log.message("3. Create the New Object : Assignment from the menubar.");

			//Step-4 : Set the Property value in metadata card & Uncheck the Check In Immediately
			//-----------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); // Instantiate metadata card wrapper
			metadataCard.setInfo(dataPool.get("Properties")+dataPool.get("Object"));// Set the property value
			metadataCard.setCheckInImmediately(true);// Uncheck the Check In Immediately checkbox
			metadataCard.clickCreateBtn();
			Log.message("4. Enter the Property value and " + dataPool.get("Object") + " is Created");


			//Step-5 : Navigate to 'Assigned to me' view
			//-------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("5. Navigated to '" + viewToNavigate + "' view."); 


			//Step-6 : Verification while creating object is displayed in Check out to me list
			//--------------------------------------------------------------------------------
			if(homePage.listView.isItemExists(dataPool.get("Object"))){
				Log.pass("Test Case Passed. "+ dataPool.get("Object")+" is Displayed in List");
			}
			else
				Log.fail("Test Case Failed. "+ dataPool.get("Object") +" is not displayed in list", driver);


		}//End try
		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End Sprint104_2_75B

	/**
	 * 104_2_77 : Verify object is created via upload a document over Favorites view
	 * 
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties","Bug"}, 
			description = "Verify object is created via upload a document over Favorites view")
	public void SprintTest104_2_77(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try{

			//Step-1 : Login to MFWA
			//-----------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");


			//Step-2 : Select 'Favorites' in List View and click New object from task pane
			//-------------------------------------------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("NavigateToView"));
			Log.message("2. Select the '" + dataPool.get("NavigateToView") + "' Item from the List View.");

			//Step-3 : Click the 'Document upload link' from the task pane
			//------------------------------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectToCreate"));
			Log.message("3. Click the Object : " + dataPool.get("ObjectToCreate") + " from the task pane.");

			//Step-4 : Upload the file and Click the OK button
			//------------------------------------------------
			homePage.taskPanel.selectFiletoUpload("./Common/Prerequsites/PrefilledPropertyInViews/Test104_2_77.docx");
			homePage.taskPanel.clickOkOnUploadDialog();
			Log.message("4. Upload the file and Click the OK button for confirmation upload dialog");

			//Step-5 : Enter the Property values in Metadata card
			//---------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); // Instantiate metadata card wrapper
			metadataCard.setInfo(dataPool.get("Properties"));
			String propVal = metadataCard.getPropertyValue("Name or title");
			System.out.println("Property value is : " +propVal);
			metadataCard.setCheckInImmediately(true); // Enable the CheckInImmediately button
			metadataCard.clickCreateBtn();
			Log.message("5. Created the New Object : '"+ dataPool.get("ObjectToCreate") + " by new metadata card.");

			//Step-6 : Navigate to 'Favorites' view
			//-------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("6. Navigated to '" + viewToNavigate + "' view."); 

			//Step-7 : Verify the Created Object is Exist in the Favorites view
			//------------------------------------------------------------------
			if(homePage.listView.isItemExists(propVal+"."+dataPool.get("Extension"))){
				Log.pass("Test Case Passed.Created object '"+propVal+"."+dataPool.get("Extension")+"' is displayed in Favorites view.");
			}
			else
				Log.fail("Test Case Failed.Created object '"+propVal+dataPool.get("Extension")+"' is not displayed in Favorites view.", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}

	}//End Sprint104_2_77

	/**
	 * 104_2_82 : Create a new Assignment in Favorites view when any existing object is selected
	 * @param dataValues
	 * @param driverType
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint104", "PrefilledProperties"}, 
			description = "Create a new Assignment in Favorites view when any existing object is selected")
	public void SprintTest104_2_82(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			//Step-1 : Login to MFWA
			//-----------------------


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); // Launch the URL and login to MFWA.
			Log.message("1. Logged into MFWA.");

			//Step-2 : Navigate to 'Manage Customers' view 
			//---------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			Log.message("2. Navigated to '" + viewToNavigate + "' view."); 

			//Step-3 : Select any existing object from the 'Customer' view
			//------------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Log.message("3. Select the Existing item : " +dataPool.get("Object") + " in Navigated view.");

			//Step-4 : Create the new object link from the menu bar
			//-----------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectToCreate"));
			Log.message("4. New "+ dataPool.get("ObjectToCreate") + " Object is clicked from menu bar.");

			//Step-5 : Verify the metadata Option for selected object
			//-------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); // Instantiate metadata card wrapper
			String propVal = metadataCard.getPropertyValue(dataPool.get("SelectProperty"));
			if (propVal.contains(dataPool.get("Object"))) {
				Log.message("5. Property " + dataPool.get("SelectProperty") +" : " + propVal + " is prefilled in new " + dataPool.get("ObjectToCreate") + " metadata card.");
				Log.pass("Test case passed. Property " + dataPool.get("SelectProperty") +" : " + propVal + " is prefilled in new " + dataPool.get("ObjectToCreate") + " metadata card.");
			}else
				Log.fail("5.Property " + dataPool.get("SelectProperty") +" : " + propVal + " is not prefilled in new " + dataPool.get("ObjectToCreate") + " metadata card.", driver);

		} //End try
		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Sprint104_2_82




} //End class PrefilledPropertyInViews
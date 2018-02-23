package MFClient.Tests.Workflows;

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
import MFClient.Wrappers.MenuBar;
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class WorkflowsInSearches {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String userName = null;
	public static String password = null;
	public static String testVault = null;
	public static String configURL = null;
	public static String userFullName = null;
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
			configURL = xmlParameters.getParameter("ConfigurationURL");
			className = this.getClass().getSimpleName().toString().trim();
			if (xmlParameters.getParameter("driverType").equalsIgnoreCase("IE"))
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim() + xmlParameters.getParameter("driverVersion").trim();
			else
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim();
			userName = xmlParameters.getParameter("UserName");
			password = xmlParameters.getParameter("Password");
			testVault = xmlParameters.getParameter("VaultName");		
			userFullName = xmlParameters.getParameter("UserFullName");
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
	 * 39.1.27 : Search keyword as workflow name
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Search keyword as workflow name")
	public void SprintTest39_1_27(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search with the name of a workflow
			//---------------------------------------
			homePage.searchPanel.search(dataPool.get("Workflow"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			Log.message("2. Search with the name of a workflow");

			//Verification: To verify if the object with the workflow are listed
			//-------------------------------------------------------------------
			if(homePage.listView.itemCount() == 0)
				throw new Exception("The Given Search criteria did not return any result.");
			MetadataCard metadatacard = null;
			for(int count = 0; count < homePage.listView.itemCount(); count++) {
				homePage.listView.clickItemByIndex(count);
				Utils.fluentWait(driver);

				Utils.fluentWait(driver);
				metadatacard = new MetadataCard(driver, true);
				Utils.fluentWait(driver);

				if(!metadatacard.getWorkflow().equals(dataPool.get("Workflow")))
					Log.fail("Test Case Failed. The Object without the specified workflow were also listed.", driver);

				driver.switchTo().defaultContent();
				Utils.fluentWait(driver);
			}

			Log.pass("Test Case Passed. Only the objects with the specified workflow were listed.");

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}	

	/**
	 * 39.1.28 : Search keyword as workflow state name
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Search keyword as workflow state name")
	public void SprintTest39_1_28(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search with the name of a workflow state
			//--------------------------------------------
			homePage.searchPanel.search(dataPool.get("State"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			Log.message("2. Search with the name of a workflow state.");

			//Verification: To verify if the object with the workflow state are listed
			//--------------------------------------------------------------------------
			if(homePage.listView.itemCount() == 0)
				throw new Exception("The Given Search criteria did not return any result.");

			MetadataCard metadatacard = null;
			for(int count = 0; count < homePage.listView.itemCount(); count++) {
				homePage.listView.clickItemByIndex(count);
				Utils.fluentWait(driver);

				Utils.fluentWait(driver);
				metadatacard = new MetadataCard(driver, true);
				Utils.fluentWait(driver);

				if(!metadatacard.getWorkflow().equals(dataPool.get("Workflow")) || !metadatacard.getWorkflowState().equals(dataPool.get("State")))
					Log.fail("Test Case Failed. The Object without the specified workflow were also listed.", driver);

				driver.switchTo().defaultContent();
				Utils.fluentWait(driver);
			}

			Log.pass("Test Case Passed. Only the objects with the specified workflow state were listed.");
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}	

	/**
	 * 39.1.29A : Search keyword as workflow name (Non-Checked In Object) (Metadatacard-Right Pane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Search keyword as workflow name (Non-Checked In Object)")
	public void SprintTest39_1_29A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;  HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object
			//------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object.");

			//3. Check out the object
			//-----------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("Object")))
				throw new Exception("Object (" + dataPool.get("Object") + ") is not checked out.");

			Log.message("3. Check out the object");

			//4. Set a workflow through side pane metadatacard
			//-------------------------------------------------
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver, true);

			metadatacard.setWorkflow(dataPool.get("Workflow"));
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("4. Set a workflow through side pane metadatacard.");

			//5. Perfrom a quick search with the workflow name
			//--------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Workflow"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");
			Utils.fluentWait(driver);
			Log.message("5. Perfrom a quick search with the workflow name.");

			//Verification: If the object is listed in the search result
			//----------------------------------------------------------
			if(homePage.listView.isItemExists(dataPool.get("Object")))
				Log.pass("Test Case Passed. The Object was listed in the Search result.");
			else
				Log.fail("Test Case Failed. The Object was not listed in the Search result.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if (homePage != null)
			{
				try
				{
					homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		}
	}	

	/**
	 * 39.1.29B : Search keyword as workflow name (Non-Checked In Object) (Metadatacard-Popup)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Search keyword as workflow name (Non-Checked In Object) (Metadatacard-Popup)")
	public void SprintTest39_1_29B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;  HomePage homePage = null;

		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object
			//------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object.");

			//3. Check out the object
			//-----------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("Object")))
				throw new Exception("Object (" + dataPool.get("Object") + ") is not checked out.");

			Log.message("3. Check out the object");

			//4. Set a workflow through pop metadatacard
			//-------------------------------------------------
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver);

			metadatacard.setWorkflow(dataPool.get("Workflow"));
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("4. Set a workflow through side pane metadatacard.");

			//5. Perfrom a quick search with the workflow name
			//--------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Workflow"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");
			Utils.fluentWait(driver);

			Log.message("5. Perfrom a quick search with the workflow name.");

			//Verification: If the object is listed in the search result
			//----------------------------------------------------------
			if(homePage.listView.isItemExists(dataPool.get("Object")))
				Log.pass("Test Case Passed. The Object was listed in the Search result.");
			else
				Log.fail("Test Case Failed. The Object was not listed in the Search result.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if (homePage != null)
			{
				try
				{
					homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		}
	}	

	/**
	 * 39.1.29C : Search keyword as workflow name (Non-Checked In Object) (Workflow dialog)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Search keyword as workflow name (Non-Checked In Object) (Workflow dialog)")
	public void SprintTest39_1_29C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;  HomePage homePage = null;
		ConcurrentHashMap <String, String> dataPool = null;

		try {


			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object
			//------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object.");

			//3. Check out the object
			//-----------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);

			Log.message("3. Check out the object");

			//4. Set a workflow through pop metadatacard
			//-------------------------------------------------
			Utils.fluentWait(driver);
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			MenuBar menuBar = new MenuBar(driver);
			menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			MFilesDialog mFilesDialog = new MFilesDialog(driver);

			mFilesDialog.setWorkflow(dataPool.get("Workflow"));
			mFilesDialog.clickOkButton();
			Utils.fluentWait(driver);

			Log.message("4. Set a workflow through side pane metadatacard.");

			//5. Perfrom a quick search with the workflow name
			//--------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Workflow"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");
			Utils.fluentWait(driver);

			Log.message("5. Perfrom a quick search with the workflow name.");

			//Verification: If the object is listed in the search result
			//----------------------------------------------------------
			if(homePage.listView.isItemExists(dataPool.get("Object")))
				Log.pass("Test Case Passed. The Object was listed in the Search result.");
			else
				Log.fail("Test Case Failed. The Object was not listed in the Search result.", driver);

			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");
			Utils.fluentWait(driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {

			if (homePage != null && dataPool != null){
				try
				{
					homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
					Utils.fluentWait(driver);

					Utils.fluentWait(driver);
					homePage.listView.clickItem(dataPool.get("Object"));
					Utils.fluentWait(driver);

					Utils.fluentWait(driver);
					if(homePage.taskPanel.isItemExists(Caption.MenuItems.CheckIn.Value))
						homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);
					Utils.fluentWait(driver);
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		}
	}	

	/**
	 * 39.1.30A : Search keyword as workflow state name (Non-Checked In Object)(Metadatacard-Right Pane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Search keyword as workflow state name (Non-Checked In Object)(Metadatacard-Right Pane)")
	public void SprintTest39_1_30A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;  HomePage homePage = null;

		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object
			//------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object.");

			//3. Check out the object
			//-----------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);

			Log.message("3. Check out the object");

			//4. Set a workflow and state through side pane metadatacard
			//------------------------------------------------------------
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver, true);

			metadatacard.setWorkflow(dataPool.get("Workflow"));
			metadatacard.setWorkflowState(dataPool.get("State"));
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("4. Set a workflow and state through side pane metadatacard.");

			//5. Perfrom a quick search with the state name
			//--------------------------------------------------
			homePage.searchPanel.search(dataPool.get("State"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);
			/*		homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");*/


			Log.message("5. Perfrom a quick search with the state name.");

			//Verification: If the object is listed in the search result
			//----------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				Log.pass("Test Case Passed. The Object was not listed in the Search result.");
			else
				Log.fail("Test Case Failed. The Object was listed in the Search result", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			/*if (homePage != null)
				homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");*/
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 39.1.30B : Search keyword as workflow state name (Non-Checked In Object)(Metadatacard-Popup)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Search keyword as workflow state name (Non-Checked In Object)(Metadatacard-Popup)")
	public void SprintTest39_1_30B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;  HomePage homePage = null;

		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object
			//------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object.");

			//3. Check out the object
			//-----------------------
			Utils.fluentWait(driver);
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);

			Log.message("3. Check out the object");

			//4. Set a workflow and state through pop metadatacard
			//-----------------------------------------------------
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver);
			Utils.fluentWait(driver);

			metadatacard.setWorkflow(dataPool.get("Workflow"));
			Utils.fluentWait(driver);
			metadatacard.setWorkflowState(dataPool.get("State"));
			Utils.fluentWait(driver);
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("4. Set a workflow and state through side pane metadatacard.");

			//5. Perfrom a quick search with the state name
			//--------------------------------------------------
			Utils.fluentWait(driver);
			homePage.searchPanel.search(dataPool.get("State"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");
			Utils.fluentWait(driver);

			Log.message("5. Perfrom a quick search with the state name.");

			//Verification: If the object is listed in the search result
			//----------------------------------------------------------
			Utils.fluentWait(driver);
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				Log.pass("Test Case Passed. The Object was not listed in the Search result.");
			else
				Log.fail("Test Case Failed. The Object was listed in the Search result.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if (homePage != null)
			{
				try
				{
					homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		}
	}	

	/**
	 * 39.1.30C : Search keyword as workflow state name (Non-Checked In Object)  (Workflow dialog)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Search keyword as workflow state name (Non-Checked In Object)  (Workflow dialog)")
	public void SprintTest39_1_30C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;  HomePage homePage = null;

		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object
			//------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object.");

			//3. Check out the object
			//-----------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);

			Log.message("3. Check out the object");

			//4. Set a workflow and state through Workflows dialog
			//------------------------------------------------------
			Utils.fluentWait(driver);
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			MenuBar menuBar = new MenuBar(driver);
			menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			MFilesDialog mFilesDialog = new MFilesDialog(driver);

			mFilesDialog.setWorkflow(dataPool.get("Workflow"));
			mFilesDialog.setWorkflowState(dataPool.get("State"));
			mFilesDialog.clickOkButton();
			Utils.fluentWait(driver);

			Log.message("4. Set a workflow and state through Workflows dialog.");

			//5. Perfrom a quick search with the state name
			//--------------------------------------------------
			homePage.searchPanel.search(dataPool.get("State"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");
			Utils.fluentWait(driver);

			Log.message("5. Perfrom a quick search with the state name.");

			//Verification: If the object is listed in the search result
			//----------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				Log.pass("Test Case Passed. The Object was not listed in the Search result.");
			else
				Log.fail("Test Case Failed. The Object was listed in the Search result.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if (homePage != null)
			{
				try
				{
					homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		}
	}	


	/**
	 * 39.1.39 : Search using advanced Search condition with Workflow property
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows,Search"}, 
			description = "Search using advanced Search condition with Workflow property")
	public void SprintTest39_1_39(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;  

		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Set the advanced Search condition with the Workflow property
			//-----------------------------------------------------------------
			Utils.fluentWait(driver);
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), "is", dataPool.get("Workflow"));
			Utils.fluentWait(driver);

			Log.message("1. Set the advanced Search condition with the Workflow property.");

			//2. Click the Search Button
			//---------------------------
			homePage.searchPanel.search("", Caption.Search.SearchAllObjects.Value);
			Utils.fluentWait(driver);
			homePage.searchPanel.resetAll();
			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");
			Utils.fluentWait(driver);

			Log.message("2. Click the Search Button");

			//Verification: To verify if only the objects with the specified Workflow are listed
			//-----------------------------------------------------------------------------------
			MetadataCard metdatacard = null;
			int itemCount = homePage.listView.itemCount();
			if(itemCount == 0)
				throw new SkipException("Invalid Test data. The Given workflow was not set for any objects in the vault");

			for(int count = 0; count < itemCount; count++) {
				Utils.fluentWait(driver);
				homePage.listView.clickItemByIndex(count);
				Utils.fluentWait(driver);

				Utils.fluentWait(driver);
				metdatacard = new MetadataCard(driver, true);
				if(!metdatacard.getWorkflow().equals(dataPool.get("Workflow")))
					Log.fail("Test Case Failed. Object without the spoecified workflow was listed in the Search results", driver);
				driver.switchTo().defaultContent();

			}

			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");
			Utils.fluentWait(driver);

			Log.pass("Test Case Passed. Only objects with the specified workflow were listed.");

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 39.1.40 : Search using advanced Search condition with Workflow state property
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows,Search"}, 
			description = "Search using advanced Search condition with Workflow state property")
	public void SprintTest39_1_40(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;  

		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Set the advanced Search condition with the Workflow state property
			//-----------------------------------------------------------------------
			Utils.fluentWait(driver);
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), "is", dataPool.get("State"));
			Utils.fluentWait(driver);

			Log.message("1. Set the advanced Search condition with the Workflow state property.");

			//2. Click the Search Button
			//---------------------------
			homePage.searchPanel.clickSearch();
			Utils.fluentWait(driver);
			homePage.searchPanel.resetAll();
			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");
			Utils.fluentWait(driver);

			Log.message("2. Click the Search Button");

			//Verification: To verify if only the objects with the specified Workflow state are listed
			//-----------------------------------------------------------------------------------------
			MetadataCard metdatacard = null;
			int itemCount = homePage.listView.itemCount();
			if(itemCount == 0)
				throw new SkipException("Invalid Test data. The Given workflow state was not set for any objects in the vault");

			for(int count = 0; count < itemCount; count++) {
				Utils.fluentWait(driver);
				homePage.listView.clickItemByIndex(count);
				Utils.fluentWait(driver);

				Utils.fluentWait(driver);
				metdatacard = new MetadataCard(driver, true);
				if(!metdatacard.getWorkflowState().equals(dataPool.get("State")))
					Log.fail("Test Case Failed. Object without the specified workflow state was listed in the Search results", driver);
				driver.switchTo().defaultContent();
			}

			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");
			Utils.fluentWait(driver);

			Log.pass("Test Case Passed. Only objects with the specified workflow state were listed.");

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}	

	/**
	 * 39.1.46 : Search using advanced Search condition with Workflow property (is not) condition
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows,Search"}, 
			description = "Search using advanced Search condition with Workflow property (is not) condition")
	public void SprintTest39_1_46(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;  HomePage homePage= null;

		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Set the advanced Search condition with the Workflow property
			//-----------------------------------------------------------------
			Utils.fluentWait(driver);
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), "is not", dataPool.get("Workflow"));
			Utils.fluentWait(driver);

			Log.message("1. Set the advanced Search condition with the Workflow property.");

			//2. Click the Search Button
			//---------------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), Caption.Search.SearchAllObjects.Value);
			homePage.searchPanel.resetAll();
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");

			Log.message("2. Click the Search Button");

			//Verification: To verify if only the objects with the specified Workflow are listed
			//-----------------------------------------------------------------------------------
			MetadataCard metdatacard = null;
			int itemCount = homePage.listView.itemCount();
			if(itemCount == 0)
				throw new SkipException("Invalid Test data. The Given workflow was not set for any objects in the vault");

			for(int count = 0; count < itemCount; count++) {
				homePage.listView.clickItemByIndex(count);

				metdatacard = new MetadataCard(driver, true);
				if(metdatacard.getWorkflow().equals(dataPool.get("Workflow")) || metdatacard.getWorkflow().equals(""))
					Log.fail("Test Case Failed. Object without the specified workflow was listed in the Search results", driver);
				driver.switchTo().defaultContent();

			}

			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");

			Log.pass("Test Case Passed. Only objects with the specified workflow were listed.");

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {



			if(homePage != null)
			{
				try
				{
					Utils.fluentWait(driver);
					if(homePage.taskPanel.isItemExists(Caption.MenuItems.CheckIn.Value))
						homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}				
			}

			Utility.quitDriver(driver);
		}
	}

	/**
	 * 39.1.47 : Search using advanced Search condition with Workflow state property(is not) condition
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows,Search"}, 
			description = "Search using advanced Search condition with Workflow state property(is not) condition")
	public void SprintTest39_1_47(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;  HomePage homePage= null;

		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Set the advanced Search condition with the Workflow state property
			//-----------------------------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), "is not", dataPool.get("State"));

			Log.message("1. Set the advanced Search condition with the Workflow state property.");

			//2. Click the Search Button
			//---------------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), Caption.Search.SearchAllObjects.Value);
			homePage.searchPanel.resetAll();
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");

			Log.message("2. Click the Search Button");

			//Verification: To verify if only the objects with the specified Workflow state are listed
			//-----------------------------------------------------------------------------------------
			MetadataCard metdatacard = null;
			int itemCount = homePage.listView.itemCount();
			if(itemCount == 0)
				throw new SkipException("Invalid Test data. The Given workflow state was not set for any objects in the vault");

			for(int count = 0; count < itemCount; count++) {
				homePage.listView.clickItemByIndex(count);

				metdatacard = new MetadataCard(driver, true);
				if(metdatacard.getWorkflowState().equals(dataPool.get("State")))
					Log.fail("Test Case Failed. Object without the specified workflow state was listed in the Search results", driver);
				driver.switchTo().defaultContent();
			}

			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");

			Log.pass("Test Case Passed. Only objects with the specified workflow state were listed.");

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {


			if(homePage != null)
			{
				try
				{
					Utils.fluentWait(driver);
					if(homePage.taskPanel.isItemExists(Caption.MenuItems.CheckIn.Value))
						homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}

			Utility.quitDriver(driver);
		}
	}


}

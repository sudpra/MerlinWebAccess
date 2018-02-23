package MFClient.Tests.Workflows;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.Utils;
import genericLibrary.WebDriverUtils;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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
public class ForcedDefaultWorkflow {

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
	 * 39.1.31 : Remove class mandated workflow (Workflow dialog)
	 */
	/*@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Remove class mandated workflow (Workflow dialog)")*/
	public void SprintTest39_1_31(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object with class that has forced default workflow
			//---------------------------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Search for an object with class that has forced default workflow.");

			//3. Open the Workflow dialog of the object
			//------------------------------------------
			Utils.fluentWait(driver);
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			MenuBar menuBar = new MenuBar(driver);
			menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			MFilesDialog mFilesDialog = new MFilesDialog(driver);

			Log.message("3. Open the Workflow dialog of the object");

			//4. Remove the Workflow and click the Ok button
			//-----------------------------------------------
			mFilesDialog.setWorkflow("");
			mFilesDialog.clickOkButton();
			Utils.fluentWait(driver);

			Log.message("4. Remove the Workflow and click the Ok button.");

			//Verification: A Warning dialog should appear with the expected text
			//---------------------------------------------------------------------
			mFilesDialog = new MFilesDialog(driver, "M-Files Web");

			if(mFilesDialog.getMessage().equals(dataPool.get("ExpectedMessage")))
				Log.pass("Test Case Passed. The Warning dialog with the expected message appeared.");
			else
				Log.fail("Test Case Failed. The Warning dialog appeared but it did not have the expected result.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {

			Utility.quitDriver(driver);
		}
	}	

	/**
	 * 39.1.32A : Remove class mandated workflow (metadatacard-Side Pane)
	 */
	/*@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Remove class mandated workflow (metadatacard-Side Pane)")*/
	public void SprintTest39_1_32A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object with class that has forced default workflow
			//---------------------------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Search for an object with class that has forced default workflow.");

			//3. Click on the object
			//-----------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("3. Click on the object.");

			//4. Remove the Workflow and click the Ok button
			//-----------------------------------------------
			metadatacard.setWorkflow("");
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("4. Remove the Workflow and click the Ok button.");

			//Verification: A Warning dialog should appear with the expected text
			//---------------------------------------------------------------------
			if(!MFilesDialog.exists(driver))
				throw new Exception("The Warning dialog did not appear, when removing the workflow of an object with forced default workflow");

			MFilesDialog mFilesDialog = new MFilesDialog(driver);

			if(mFilesDialog.getMessage().equals(dataPool.get("ExpectedMessage")))
				Log.pass("Test Case Passed. The Warning dialog with the expected message appeared.");
			else
				Log.fail("Test Case Failed. The Warning dialog appeared but it did not have the expected result.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {

			Utility.quitDriver(driver);
		}
	}	

	/**
	 * 39.1.32B : Remove class mandated workflow (metadatacard-PopUP)
	 */
	/*@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Remove class mandated workflow (metadatacard-PopUP)")*/
	public void SprintTest39_1_32B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object with class that has forced default workflow
			//---------------------------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Search for an object with class that has forced default workflow.");

			//3. Open the Metadatacard of the object
			//---------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("3. Open the Metadatacard of the object.");

			//4. Remove the Workflow and click the Ok button
			//-----------------------------------------------
			metadatacard.setWorkflow("");
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);
			metadatacard = new MetadataCard(driver);
			metadatacard.cancelAndConfirm();
			Utils.fluentWait(driver);

			Log.message("4. Remove the Workflow and click the Ok button.");

			//Verification: A Warning dialog should appear with the expected text
			//---------------------------------------------------------------------
			if(driver.findElement(By.cssSelector("div[class='errorDialog']")).getText().equals(dataPool.get("ExpectedMessage")))
				Log.pass("Test Case Passed. The Warning dialog with the expected message appeared.");
			else
				Log.fail("Test Case Failed. The Warning dialog appeared but it did not have the expected result.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {

			Utility.quitDriver(driver);
		}
	}	

	/**
	 * 39.1.33 : Change class mandated workflow (Workflow dialog)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Change class mandated workflow (Workflow dialog)")
	public void SprintTest39_1_33(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {




			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Search for an object with class that has forced default workflow
			//---------------------------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("1. Search for an object with class that has forced default workflow.");

			//2. Open the Workflow dialog of the object
			//---------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);
			Utils.fluentWait(driver);

			MFilesDialog mFilesDialog = new MFilesDialog(driver);

			Log.message("2. Open the Workflow dialog of the object.");

			//3. Remove the Workflow and click the Ok button
			//-----------------------------------------------
			List<String> workflowList = mFilesDialog.getWorkflows();
			mFilesDialog.clickCancelButton();

			Log.message("3. Get the available workflows.");

			//Verification: A Warning dialog should appear with the expected text
			//---------------------------------------------------------------------
			if(workflowList.size() == 1)
				Log.pass("Test Case Passed. Only one workflow (" + workflowList.get(0) + ") is displayed for the class mandated workflow.");
			else
				Log.fail("Test Case Failed. More than one workflow is displayed for class mandated workflows.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {

			Utility.quitDriver(driver);
		}
	}	


	/**
	 * 39.1.35 : Remove class mandated workflow (Workflow dialog) (Checked Out Object)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Remove class mandated workflow (Workflow dialog) (Checked Out Object)")
	public void SprintTest39_1_35(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object with class that has forced default workflow and check out the object
			//---------------------------------------------------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("Object")))
				throw new Exception("Object (" + dataPool.get("Object") + ") is not checked out.");

			Log.message("2. Search for an object with class that has forced default workflow and check out the object.");

			//3. Open the Workflow dialog of the object
			//------------------------------------------
			Utils.fluentWait(driver);
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			MFilesDialog mFilesDialog = new MFilesDialog(driver);

			Log.message("3. Open the Workflow dialog of the object");

			//3. Remove the Workflow and click the Ok button
			//-----------------------------------------------
			List<String> workflowList = mFilesDialog.getWorkflows();
			mFilesDialog.clickCancelButton();

			Log.message("4. Get the available workflows.");

			//Verification: A Warning dialog should appear with the expected text
			//---------------------------------------------------------------------
			if(workflowList.size() == 1)
				Log.pass("Test Case Passed. Only one workflow (" + workflowList.get(0) + ") is displayed for the class mandated workflow.");
			else
				Log.fail("Test Case Failed. More than one workflow is displayed for class mandated workflows.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}	

	/**
	 * 39.1.36 : Remove class mandated workflow (metadatacard-Side Pane)(Checked Out Object)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Remove class mandated workflow (metadatacard-Side Pane)(Checked Out Object)")
	public void SprintTest39_1_36(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 


		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object with class that has forced default workflow and check out the object
			//----------------------------------------------------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);

			Log.message("2. Search for an object with class that has forced default workflow and check out the object.");

			//3. Click on the object
			//-----------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("3. Click on the object.");

			//4. Remove the Workflow and click the Ok button
			//-----------------------------------------------
			metadatacard.setWorkflow("");
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("4. Remove the Workflow and click the Ok button.");

			//Verification: A Warning dialog should appear with the expected text
			//---------------------------------------------------------------------
			if(!MFilesDialog.exists(driver))
				throw new Exception("The Warning dialog did not appear, when removing the workflow of an object with forced default workflow");

			MFilesDialog mFilesDialog = new MFilesDialog(driver);

			if(mFilesDialog.getMessage().equals(dataPool.get("ExpectedMessage")))
				Log.pass("Test Case Passed. The Warning dialog with the expected message appeared.");
			else
				Log.fail("Test Case Failed. The Warning dialog appeared but it did not have the expected result.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}	

	/**
	 * 39.1.37 : Remove class mandated workflow (metadatacard-PopUP)(Checked Out Object)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Remove class mandated workflow (metadatacard-PopUP)(Checked Out Object)")
	public void SprintTest39_1_37(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 


		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object with class that has forced default workflow and check out the object
			//---------------------------------------------------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);

			Log.message("2. Search for an object with class that has forced default workflow and check out the object.");

			//3. Open the Metadatacard of the object
			//---------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("3. Open the Metadatacard of the object.");

			//4. Remove the Workflow and click the Ok button
			//-----------------------------------------------
			metadatacard.setWorkflow("");
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("4. Remove the Workflow and click the Ok button.");

			//Verification: A Warning dialog should appear with the expected text
			//---------------------------------------------------------------------
			if(driver.findElement(By.cssSelector("div[class='errorDialog']")).getText().equals(dataPool.get("ExpectedMessage")))
				Log.pass("Test Case Passed. The Warning dialog with the expected message appeared.");
			else
				Log.fail("Test Case Failed. The Warning dialog appeared but it did not have the expected result.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}	

	/**
	 * 39.1.38 : Change class mandated workflow (Workflow dialog)(Checked Out Object)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Change class mandated workflow (Workflow dialog)(Checked Out Object)")
	public void SprintTest39_1_38(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;  HomePage homePage = null;


		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object with class that has forced default workflow and check out the object
			//----------------------------------------------------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);

			Log.message("2. Search for an object with class that has forced default workflow and check out the object.");

			//3. Open the Metadatacard of the object
			//---------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);
			Utils.fluentWait(driver);

			MFilesDialog mFilesDialog = new MFilesDialog(driver);

			Log.message("3. Open the Metadatacard of the object.");

			//3. Remove the Workflow and click the Ok button
			//-----------------------------------------------
			List<String> workflowList = mFilesDialog.getWorkflows();
			mFilesDialog.clickCancelButton();

			Log.message("4. Get the available workflows.");

			//Verification: A Warning dialog should appear with the expected text
			//---------------------------------------------------------------------
			if(workflowList.size() == 1)
				Log.pass("Test Case Passed. Only one workflow (" + workflowList.get(0) + ") is displayed for the class mandated workflow.");
			else
				Log.fail("Test Case Failed. More than one workflow is displayed for class mandated workflows.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {


			if (homePage != null){
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
	 * 39.1.44 : Other Workflow not listed when the force workflow for a class is selected (Metadatacard)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Other Workflow not listed when the force workflow for a class is selected (Metadatacard)")
	public void SprintTest39_1_44(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 


		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Search for an object
			//------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("1. Search for an object.");

			//2. Open the Metadatacard of the object
			//---------------------------------------
			homePage.listView.rightClickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Open the Metadatacard of the object.");

			//Verification: To verify if a Workflow that is available for a different class is not available for this object
			//---------------------------------------------------------------------------------------------------------------
			List<String> workflows = metadatacard.getAvailableWorkflows();
			if(workflows.contains(dataPool.get("Workflow")) && workflows.size() == 1)
				Log.pass("Test Case Passed. Only the forced wotkflow was listed.");
			else
				Log.fail("Test Case Failed. Workflows other than the forced workflows were also available.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 39.1.45 : Other Workflow not listed when the force workflow for a class is selected (Metadatacard-SidePane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Other Workflow not listed when the force workflow for a class is selected (Metadatacard-SidePane)")
	public void SprintTest39_1_45(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Search for an object
			//------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("1. Search for an object.");

			//2. Open the Metadatacard of the object
			//---------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("2. Open the Metadatacard of the object.");

			//Verification: To verify if a Workflow that is available for a different class is not available for this object
			//---------------------------------------------------------------------------------------------------------------
			List<String> workflows = metadatacard.getAvailableWorkflows();
			if(workflows.contains(dataPool.get("Workflow")) && workflows.size() == 1)
				Log.pass("Test Case Passed. Only the forced wotkflow was listed.");
			else
				Log.fail("Test Case Failed. Workflows other than the forced workflows were also available.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}	


	/**
	 * 39.1.71 : Change the class of an object to a class with forced default workflow (metadatcard)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Change the class of an object to a class with forced default workflow (metadatcard)")
	public void SprintTest39_1_71(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object with workflow
			//--------------------------------------
			Utils.fluentWait(driver);
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object with workflow.");

			//3.Open the properties of an object
			//-----------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			Log.message("3.Open the properties of an object.");

			//4. Set the class to the object
			//----------------------------------
			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver);

			Utils.fluentWait(driver);
			/*metadatacard.setPropertyValue("Class", dataPool.get("Class"));*/
			metadatacard.setInfo(dataPool.get("Properties"));
			metadatacard.saveAndClose();

			Log.message("4. Set the class to the object.");

			//Verification: To verify is the workflow and the first state are set to the object
			//---------------------------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true);

			if(metadatacard.getWorkflow().equals(dataPool.get("Workflow")) && metadatacard.getWorkflowState().equals(dataPool.get("State")))		
				Log.pass("Test Case Passed. The Forced Workflow was set to the object.");
			else
				Log.fail("Test Case Failed. The Forced Workflow was not set to the object.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 39.1.72 : Change the class of an object to a class with forced default workflow (metadatcard-SidePane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Change the class of an object to a class with forced default workflow (metadatcard-SidePane)")
	public void SprintTest39_1_72(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//--------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object with workflow
			//--------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object with workflow.");

			//3.Open the properties of an object
			//-----------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));

			Log.message("3.Open the properties of an object.");

			//4. Set the class to the object
			//----------------------------------
			MetadataCard metadatacard = new MetadataCard(driver, true);
			metadatacard.setInfo(dataPool.get("Properties"));
			metadatacard.saveAndClose();

			Log.message("4. Set the class to the object.");

			//Verification: To verify is the workflow and the first state are set to the object
			//---------------------------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true);

			if(metadatacard.getWorkflow().equals(dataPool.get("Workflow")) && metadatacard.getWorkflowState().equals(dataPool.get("State")))		
				Log.pass("Test Case Passed. The Forced Workflow was set to the object.");
			else
				Log.fail("Test Case Failed. The Forced Workflow was not set to the object.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 39.1.73 : Change the class of an object to a class with forced default workflow (metadatcard) (After Saving Changes)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Change the class of an object to a class with forced default workflow (metadatcard) (After Saving Changes)")
	public void SprintTest39_1_73(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object with workflow
			//--------------------------------------
			Utils.fluentWait(driver);
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object with workflow.");

			//3.Open the properties of an object
			//-----------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			Log.message("3.Open the properties of an object.");

			//4. Set the class to the object
			//----------------------------------
			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver);

			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Class", dataPool.get("Class"));
			Utils.fluentWait(driver);

			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("4. Set the class to the object.");

			//Verification: To verify is the workflow and the first state are set to the object
			//---------------------------------------------------------------------------------
			Utils.fluentWait(driver);
			metadatacard = new MetadataCard(driver, true);
			if(metadatacard.getWorkflow().equals(dataPool.get("Workflow")) && metadatacard.getWorkflowState().equals(dataPool.get("State")))		
				Log.pass("Test Case Passed. The Forced Workflow was set to the object.");
			else
				Log.fail("Test Case Failed. The Forced Workflow was not set to the object.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 39.1.74 : Change the class of an object to a class with forced default workflow (metadatcard-SidePane) (After Saving Changes)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Change the class of an object to a class with forced default workflow (metadatcard-SidePane) (After Saving Changes)")
	public void SprintTest39_1_74(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object with workflow
			//--------------------------------------
			Utils.fluentWait(driver);
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' does not exist in the vault.");

			Log.message("2. Searched for an object with workflow.");

			//3.Open the properties of an object
			//-----------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);

			Log.message("3.Open the properties of an object.");

			//4. Set the class to the object
			//----------------------------------
			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver, true);

			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Class", dataPool.get("Class"));
			Utils.fluentWait(driver);

			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("4. Set the class to the object.");

			//Verification: To verify is the workflow and the first state are set to the object
			//---------------------------------------------------------------------------------
			Utils.fluentWait(driver);
			metadatacard = new MetadataCard(driver, true);
			if(metadatacard.getWorkflow().equals(dataPool.get("Workflow")) && metadatacard.getWorkflowState().equals(dataPool.get("State")))		
				Log.pass("Test Case Passed. The Forced Workflow was set to the object.");
			else
				Log.fail("Test Case Failed. The Forced Workflow was not set to the object.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 39.1.75 : Set the class with forced default workflow to a new document
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Set the class with forced default workflow to a new document")
	public void SprintTest39_1_75(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Open the New Document dialog
			//--------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			Log.message("2. Open the New Document dialog.");

			//3.Select the extension
			//-----------------------
			Utils.fluentWait(driver);
			Utility.selectTemplate(dataPool.get("Extension"), driver);
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver);

			Utils.fluentWait(driver);
			metadatacard.setInfo(dataPool.get("Properties"));
			metadatacard.setCheckInImmediately(true);
			metadatacard.clickCreateBtn();
			Utils.fluentWait(driver);

			Log.message("3. Set the class to the object.");

			//Verification: To verify is the workflow and the first state are set to the object
			//---------------------------------------------------------------------------------
			Utils.fluentWait(driver);
			metadatacard = new MetadataCard(driver,true);
			if(metadatacard.getWorkflow().equals(dataPool.get("Workflow")) && metadatacard.getWorkflowState().equals(dataPool.get("State")))		
				Log.pass("Test Case Passed. The Forced Workflow was set to the object.");
			else
				Log.fail("Test Case Failed. The Forced Workflow was not set to the object.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 39.1.76 : Try to clear a forced workflow from a new document dialog
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint39", "Workflows"}, 
			description = "Try to clear a forced workflow from a new document dialog")
	public void SprintTest39_1_76(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Open the New Document dialog
			//--------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			Log.message("2. Open the New Document dialog.");

			//3.Select the extension
			//-----------------------
			Utils.fluentWait(driver);
			Utility.selectTemplate(dataPool.get("Extension"), driver);
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver);

			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Class", dataPool.get("Class"));
			Utils.fluentWait(driver);

			Log.message("3. Set the class to the object.");

			//4. Clear the Workflow
			//----------------------
			metadatacard.setInfo(dataPool.get("Properties"));
			Utils.fluentWait(driver);
			metadatacard.setWorkflow("");
			Utils.fluentWait(driver);
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			//Verification: To verify is the workflow and the first state are set to the object
			//---------------------------------------------------------------------------------
			Utils.fluentWait(driver);
			WebElement warningDialog = driver.findElement(By.cssSelector("div[class='errorDialog']>div[class='shortErrorArea']"));
			if(!warningDialog.isDisplayed())
				throw new Exception("The Warning dialog did not appear.");

			if(warningDialog.getText().equals(dataPool.get("WarningMessage")))		
				Log.pass("Test Case Passed. The Expected Warning dialog appeared.");
			else
				Log.fail("Test Case Failed. The Expected Warning dialog did not appear.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

}

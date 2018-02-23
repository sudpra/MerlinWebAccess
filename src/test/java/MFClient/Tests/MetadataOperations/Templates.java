package MFClient.Tests.MetadataOperations;

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
import MFClient.Wrappers.Caption;
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class Templates {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String userName = null;
	public static String password = null;
	public static String testVault = null;
	public static String testVault2 = null;
	public static String configURL = null;
	public static String userFullName = null;
	public static String restoreVault = null;
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
			testVault2 = "MetadataOperations";
			restoreVault = "MetadataOperations";

			Utility.restoreTestVault();
			Utility.configureUsers(xlTestDataWorkBook);
			Utility.restoreTestVault(testVault2, restoreVault);
			Utility.configureUsers(xlTestDataWorkBook, "Users",testVault2);

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

			Utility.destroyTestVault();
			Utility.destroyTestVault(testVault2);

		}//End try

		catch(Exception e){
			throw e;
		}//End Catch
	}//End cleanApp


	/**
	 * 48.1.24A : Creating a new Template for an object type with no template - Select template dialog should appear
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard", "Smoke"}, 
			description = "Creating a new Template for an object type with no template - Select template dialog should appear.")
	public void SprintTest48_1_24A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			homePage.searchPanel.search("", "Search only: "+dataPool.get("ObjectType")+"s");

			Log.message("1. Logged into the Home View.");

			//2. Create a new Template
			//-------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));

			MetadataCard metadatacard = new MetadataCard(driver);

			metadatacard.setInfo(dataPool.get("Properties")+dataPool.get("Template"));
			metadatacard.saveAndClose();

			Log.message("2. Create a new Template");

			//3. Perform the New Object Type click
			//-------------------------------------

			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));

			Log.message("3. Perform the New Object Type click.");

			//Verification: To Verify if the Select template dialog appears
			//--------------------------------------------------------------
			if(Utility.closeSelectTemplate(driver))
				Log.pass("Test Case Passed. The Select Template dialog appeared as expected.");
			else
				Log.fail("Test Case Failed. The Select Template dialog did not appear.", driver);

			homePage.listView.clickItem(dataPool.get("Template"));
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Delete.Value);

			MFilesDialog mFilesDialog = new MFilesDialog(driver, "Confirm Delete");
			mFilesDialog.confirmDelete();

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.24B : Creating a new Template for an object type with no template - Select template dialog should appear (New Menu)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Creating a new Template for an object type with no template - Select template dialog should appear (New Menu).")
	public void SprintTest48_1_24B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			homePage.searchPanel.search("", "Search only: "+dataPool.get("ObjectType")+"s");

			Log.message("1. Logged into the Home View.");

			//2. Create a new Template
			//-------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));

			MetadataCard metadatacard = new MetadataCard(driver);

			metadatacard.setInfo(dataPool.get("Properties")+dataPool.get("Template"));
			metadatacard.saveAndClose();

			Log.message("2. Create a new Template");

			//3. Perform the New Object Type click
			//-------------------------------------

			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));

			Log.message("3. Perform the New Object Type click.");

			//Verification: To Verify if the Select template dialog appears
			//--------------------------------------------------------------
			if(Utility.closeSelectTemplate(driver))
				Log.pass("Test Case Passed. The Select Template dialog appeared as expected.");
			else
				Log.fail("Test Case Failed. The Select Template dialog did not appear.", driver);

			homePage.listView.clickItem(dataPool.get("Template"));
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Delete.Value);

			MFilesDialog mFilesDialog = new MFilesDialog(driver, "Confirm Delete");
			mFilesDialog.confirmDelete();

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.24C : Creating a new Template should be listed in the Select Template dialog
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Creating a new Template should be listed in the Select Template dialog.")
	public void SprintTest48_1_24C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			homePage.searchPanel.search("", "Search only: "+dataPool.get("ObjectType")+"s");

			Log.message("1. Logged into the Home View.");

			//2. Create a new Template
			//-------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));

			MetadataCard  metadatacard = new MetadataCard(driver);

			metadatacard.setInfo(dataPool.get("Properties")+dataPool.get("Template"));
			metadatacard.saveAndClose();

			Log.message("2. Created a new Template");

			//3. Perform the New Object Type click
			//-------------------------------------

			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));

			Log.message("3. Performed the New Object Type click.");

			//Verification: To Verify if the Select template dialog appears
			//--------------------------------------------------------------
			if(Utility.selectTemplate(dataPool.get("Template"), driver))
				Log.pass("Test Case Passed. The Select Template dialog appeared as expected.");
			else
				Log.fail("Test Case Failed. The Select Template dialog did not appear.", driver);


			metadatacard = new MetadataCard(driver);
			metadatacard.cancelAndConfirm();

			homePage.listView.clickItem(dataPool.get("Template"));
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Delete.Value);

			MFilesDialog mFilesDialog = new MFilesDialog(driver, "Confirm Delete");
			mFilesDialog.confirmDelete();

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.25A : Check if 'select template' dialog is displayed when a new template is added for an object type
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Check if 'select template' dialog is displayed when a new template is added for an object type.")
	public void SprintTest48_1_25A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for an object
			//-------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new Exception("The Object does not exist in the vault.");

			Log.message("2. Search for an object");

			//3. Convert the object into a template
			//-------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			MetadataCard metadatacard = new MetadataCard(driver);

			metadatacard.setPropertyValue("Is template", "Yes");
			metadatacard.saveAndClose();

			Log.message("3. Convert the object into a template.");

			//Verification: To Verify if the Select template dialog appears
			//--------------------------------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));

			if(Utility.closeSelectTemplate(driver))
				Log.pass("Test Case Passed. The Select Template dialog appeared as expected.");
			else
				Log.fail("Test Case Failed. The Select Template dialog did not appear.", driver);


			metadatacard = new MetadataCard(driver, true);

			metadatacard.removeProperty("Is template");
			metadatacard.saveAndClose();

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.25B : Check if 'select template' dialog is displayed when a new template is added for an object type
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Check if 'select template' dialog is displayed when a new template is added for an object type.")
	public void SprintTest48_1_25B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for an object
			//-------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new Exception("The Object does not exist in the vault.");

			Log.message("2. Search for an object");

			//3. Convert the object into a template
			//-------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			MetadataCard metadatacard = new MetadataCard(driver);

			metadatacard.setPropertyValue("Is template", "Yes");
			metadatacard.saveAndClose();

			Log.message("3. Convert the object into a template.");

			//Verification: To Verify if the Select template dialog appears
			//--------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));

			if(Utility.closeSelectTemplate(driver))
				Log.pass("Test Case Passed. The Select Template dialog appeared as expected.");
			else
				Log.fail("Test Case Failed. The Select Template dialog did not appear.", driver);


			metadatacard = new MetadataCard(driver, true);

			metadatacard.removeProperty("Is template");
			metadatacard.saveAndClose();

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.25C : Converting an existing object as a template should be listed in the Select Template dialog
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Converting an existing object as a template should be listed in the Select Template dialog.")
	public void SprintTest48_1_25C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for an object
			//-------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new Exception("The Object does not exist in the vault.");

			Log.message("2. Search for an object");

			//3. Convert the object into a template
			//-------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			MetadataCard metadatacard = new MetadataCard(driver);

			metadatacard.setPropertyValue("Is template", "Yes");
			metadatacard.saveAndClose();

			Log.message("3. Convert the object into a template.");

			//Verification: To Verify if the Select template dialog appears
			//--------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));

			if(Utility.selectTemplate(dataPool.get("Object"), driver))
				Log.pass("Test Case Passed. The Select Template dialog appeared as expected.");
			else
				Log.fail("Test Case Failed. The Select Template dialog did not appear.", driver);


			metadatacard = new MetadataCard(driver);
			metadatacard.cancelAndConfirm();

			metadatacard = new MetadataCard(driver, true);
			metadatacard.removeProperty("Is template");
			metadatacard.saveAndClose();

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.25D : Converting an existing object as a template - Select template dialog should appear (SidePane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Converting an existing object as a template - Select template dialog should appear (SidePane).")
	public void SprintTest48_1_25D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for an object
			//-------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new Exception("The Object does not exist in the vault.");

			Log.message("2. Search for an object");

			//3. Convert the object into a template
			//-------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));

			MetadataCard metadatacard = new MetadataCard(driver, true);

			metadatacard.setPropertyValue("Is template", "Yes");
			metadatacard.saveAndClose();

			Log.message("3. Convert the object into a template.");

			//Verification: To Verify if the Select template dialog appears
			//--------------------------------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));

			if(Utility.closeSelectTemplate(driver))
				Log.pass("Test Case Passed. The Select Template dialog appeared as expected.");
			else
				Log.fail("Test Case Failed. The Select Template dialog did not appear.", driver);


			metadatacard = new MetadataCard(driver, true);

			metadatacard.removeProperty("Is template");
			metadatacard.saveAndClose();

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.26A : Values and properties set for the Template should be set for the object that uses the template
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Values and properties set for the Template should be set for the object that uses the template.")
	public void SprintTest48_1_26A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			homePage.searchPanel.search("", "Search only: "+dataPool.get("ObjectType")+"s");

			Log.message("1. Logged into the Home View.");

			//2. Create an object using a template
			//-------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));

			Utility.selectTemplate(dataPool.get("Template"), driver);


			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard.setInfo(dataPool.get("Properties") + dataPool.get("Object"));
			metadatacard.setCheckInImmediately(true);
			metadatacard.saveAndClose();

			Log.message("2. Create an object using a template.");

			//3. Compare the properties of the object and the template
			//---------------------------------------------------------

			metadatacard = new MetadataCard(driver, true);
			ConcurrentHashMap <String, String> actualProps = metadatacard.getInfo();
			driver.switchTo().defaultContent();


			homePage.searchPanel.search(dataPool.get("Template"), "Search only: "+dataPool.get("ObjectType")+"s");

			homePage.listView.clickItem(dataPool.get("Template"));
			metadatacard = new MetadataCard(driver, true);
			ConcurrentHashMap <String, String> expectedProps = metadatacard.getInfo();
			driver.switchTo().defaultContent();

			Log.message("3. Compare the properties of the object and the template");

			//Verification: To verify if the properties and values of the template are inherited to the object
			//-------------------------------------------------------------------------------------------------
			if(Utility.compareObjects(actualProps, expectedProps).trim().equals(dataPool.get("ExpectedDifference")))
				Log.pass("Test Case Passed. The Properties were inherited as expected.");
			else
				Log.fail("Test Case Failed. The properties were not inherited as expected.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.26B : Check Out a Template - Make changes to it's metadata use it to an object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Check Out a Template - Make changes to it's metadata use it to an object.")
	public void SprintTest48_1_26B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Check out the template and make some changes to it.
			//-------------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Template"), "Search only: "+dataPool.get("ObjectType")+"s");

			homePage.listView.clickItem(dataPool.get("Template"));
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);


			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));
			metadatacard.saveAndClose();

			Log.message("2. Check out the template and make some changes to it.");

			//3. Create an object using a template
			//-------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));

			Utility.selectTemplate(dataPool.get("Template"), driver);

			metadatacard = new MetadataCard(driver);
			metadatacard.setInfo(dataPool.get("Properties") + dataPool.get("Object"));
			metadatacard.setCheckInImmediately(true);
			metadatacard.saveAndClose();


			Log.message("3. Create an object using a template.");

			//4. Compare the properties of the object and the template
			//---------------------------------------------------------

			metadatacard = new MetadataCard(driver, true);
			ConcurrentHashMap <String, String> actualProps = metadatacard.getInfo();
			driver.switchTo().defaultContent();

			homePage.listView.clickItem(dataPool.get("Template"));

			metadatacard = new MetadataCard(driver, true);
			ConcurrentHashMap <String, String> expectedProps = metadatacard.getInfo();
			driver.switchTo().defaultContent();

			Log.message("4. Compare the properties of the object and the template");

			//Verification: To verify if the properties and values of the template are inherited to the object
			//-------------------------------------------------------------------------------------------------
			if(Utility.compareObjects(actualProps, expectedProps).trim().equals(dataPool.get("ExpectedDifference")))
				Log.pass("Test Case Passed. The Properties were inherited as expected.");
			else
				Log.fail("Test Case Failed. The properties were not inherited as expected.", driver);

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);
			MFilesDialog mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.confirmUndoCheckOut(true);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.26C : Check Out a Template - Make changes to it's metadata (SidePane) use it to an object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Check Out a Template - Make changes to it's metadata (SidePane) use it to an object.")
	public void SprintTest48_1_26C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Check out the template and make some changes to it.
			//-------------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Template"), "Search only: "+dataPool.get("ObjectType")+"s");

			homePage.listView.clickItem(dataPool.get("Template"));
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);


			MetadataCard metadatacard = new MetadataCard(driver, true);
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));
			metadatacard.saveAndClose();

			Log.message("2. Check out the template and make some changes to it.");

			//3. Create an object using a template
			//-------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));

			Utility.selectTemplate(dataPool.get("Template"), driver);

			metadatacard = new MetadataCard(driver);
			metadatacard.setInfo(dataPool.get("Properties") + dataPool.get("Object"));
			metadatacard.setCheckInImmediately(true);
			metadatacard.saveAndClose();

			Log.message("3. Create an object using a template.");

			//4. Compare the properties of the object and the template
			//---------------------------------------------------------

			metadatacard = new MetadataCard(driver, true);
			ConcurrentHashMap <String, String> actualProps = metadatacard.getInfo();
			driver.switchTo().defaultContent();

			homePage.listView.clickItem(dataPool.get("Template"));
			metadatacard = new MetadataCard(driver, true);
			ConcurrentHashMap <String, String> expectedProps = metadatacard.getInfo();
			driver.switchTo().defaultContent();

			Log.message("4. Compare the properties of the object and the template");

			//Verification: To verify if the properties and values of the template are inherited to the object
			//-------------------------------------------------------------------------------------------------
			if(Utility.compareObjects(actualProps, expectedProps).trim().equals(dataPool.get("ExpectedDifference")))
				Log.pass("Test Case Passed. The Properties were inherited as expected.");
			else
				Log.fail("Test Case Failed. The properties were not inherited as expected.", driver);

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);
			MFilesDialog mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.confirmUndoCheckOut(true);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.26D : Changes done to a Template after it's creation
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Changes done to a Template after it's creation.")
	public void SprintTest48_1_26D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Check out the template and make some changes to it.
			//-------------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Template"), "Search only: "+dataPool.get("ObjectType")+"s");

			homePage.listView.clickItem(dataPool.get("Template"));


			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));
			metadatacard.saveAndClose();

			Log.message("2. Check out the template and make some changes to it.");

			//3. Create an object using a template
			//-------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));

			Utility.selectTemplate(dataPool.get("Template"), driver);

			metadatacard = new MetadataCard(driver);
			metadatacard.setInfo(dataPool.get("Properties") + dataPool.get("Object"));
			metadatacard.setCheckInImmediately(true);
			metadatacard.saveAndClose();

			Log.message("3. Create an object using a template.");

			//4. Compare the properties of the object and the template
			//---------------------------------------------------------

			metadatacard = new MetadataCard(driver, true);
			ConcurrentHashMap <String, String> actualProps = metadatacard.getInfo();
			driver.switchTo().defaultContent();

			homePage.listView.clickItem(dataPool.get("Template"));
			metadatacard = new MetadataCard(driver, true);
			ConcurrentHashMap <String, String> expectedProps = metadatacard.getInfo();
			driver.switchTo().defaultContent();

			Log.message("4. Compare the properties of the object and the template");

			//Verification: To verify if the properties and values of the template are inherited to the object
			//-------------------------------------------------------------------------------------------------
			if(Utility.compareObjects(actualProps, expectedProps).trim().equals(dataPool.get("ExpectedDifference")))
				Log.pass("Test Case Passed. The Properties were inherited as expected.");
			else
				Log.fail("Test Case Failed. The properties were not inherited as expected.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.26E : Changes done to a Template after it's creation (SidePane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Changes done to a Template after it's creation (SidePane).")
	public void SprintTest48_1_26E(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Check out the template and make some changes to it.
			//-------------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Template"), "Search only: "+dataPool.get("ObjectType")+"s");

			homePage.listView.clickItem(dataPool.get("Template"));


			MetadataCard metadatacard = new MetadataCard(driver, true);
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));
			metadatacard.saveAndClose();

			Log.message("2. Check out the template and make some changes to it.");

			//3. Create an object using a template
			//-------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));

			Utility.selectTemplate(dataPool.get("Template"), driver);

			metadatacard = new MetadataCard(driver);
			metadatacard.setInfo(dataPool.get("Properties") + dataPool.get("Object"));
			metadatacard.setCheckInImmediately(true);
			metadatacard.saveAndClose();

			Log.message("3. Create an object using a template.");

			//4. Compare the properties of the object and the template
			//---------------------------------------------------------

			metadatacard = new MetadataCard(driver, true);
			ConcurrentHashMap <String, String> actualProps = metadatacard.getInfo();
			driver.switchTo().defaultContent();

			homePage.listView.clickItem(dataPool.get("Template"));
			metadatacard = new MetadataCard(driver, true);
			ConcurrentHashMap <String, String> expectedProps = metadatacard.getInfo();
			driver.switchTo().defaultContent();

			Log.message("4. Compare the properties of the object and the template");

			//Verification: To verify if the properties and values of the template are inherited to the object
			//-------------------------------------------------------------------------------------------------
			if(Utility.compareObjects(actualProps, expectedProps).trim().equals(dataPool.get("ExpectedDifference")))
				Log.pass("Test Case Passed. The Properties were inherited as expected.");
			else
				Log.fail("Test Case Failed. The properties were not inherited as expected.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.27A : Check Out a Template - Add Properties with no values and use it to create an object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Check Out a Template - Add Properties with no values and use it to create an object.")
	public void SprintTest48_1_27A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Check out the template and make some changes to it.
			//-------------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Template"), "Search only: "+dataPool.get("ObjectType")+"s");

			homePage.listView.clickItem(dataPool.get("Template"));
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);


			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard.addNewProperty(dataPool.get("Property"));
			metadatacard.saveAndClose();

			Log.message("2. Check out the template and make some changes to it.");

			//3. Create an object using a template
			//-------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));

			Utility.selectTemplate(dataPool.get("Template"), driver);

			metadatacard = new MetadataCard(driver);
			metadatacard.setInfo(dataPool.get("Properties") + dataPool.get("Object"));
			metadatacard.setCheckInImmediately(true);
			metadatacard.saveAndClose();

			Log.message("3. Create an object using a template.");

			//Verification: To verify if the Added Properties are also added to the Object
			//-----------------------------------------------------------------------------

			metadatacard = new MetadataCard(driver, true);

			if(metadatacard.propertyExists(dataPool.get("Property")) && metadatacard.getPropertyValue(dataPool.get("Property")).equals(""))
				Log.pass("Test Case Passed. The Properties were inherited as expected.");
			else {
				driver.switchTo().defaultContent();
				homePage.listView.clickItem(dataPool.get("Template"));
				homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);
				MFilesDialog mFilesDialog = new MFilesDialog(driver);
				mFilesDialog.confirmUndoCheckOut(true);
				Log.fail("Test Case Failed. The properties were not inherited as expected.", driver);
			}

			driver.switchTo().defaultContent();
			homePage.listView.clickItem(dataPool.get("Template"));
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);
			MFilesDialog mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.confirmUndoCheckOut(true);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.27B : Check Out a Template - Add Properties with no values and use it to create an object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Check Out a Template - Add Properties with no values and use it to create an object.")
	public void SprintTest48_1_27B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Check out the template and make some changes to it.
			//-------------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Template"), "Search only: "+dataPool.get("ObjectType")+"s");

			homePage.listView.clickItem(dataPool.get("Template"));
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);


			MetadataCard metadatacard = new MetadataCard(driver, true);
			metadatacard.addNewProperty(dataPool.get("Property"));
			metadatacard.saveAndClose();

			Log.message("2. Check out the template and make some changes to it.");

			//3. Create an object using a template
			//-------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));

			Utility.selectTemplate(dataPool.get("Template"), driver);

			metadatacard = new MetadataCard(driver);
			metadatacard.setInfo(dataPool.get("Properties") + dataPool.get("Object"));
			metadatacard.setCheckInImmediately(true);
			metadatacard.saveAndClose();

			Log.message("3. Create an object using a template.");

			//Verification: To verify if the Added Properties are also added to the Object
			//-----------------------------------------------------------------------------

			metadatacard = new MetadataCard(driver, true);

			if(metadatacard.propertyExists(dataPool.get("Property")) && metadatacard.getPropertyValue(dataPool.get("Property")).equals(""))
				Log.pass("Test Case Passed. The Properties were inherited as expected.");
			else {

				driver.switchTo().defaultContent();
				homePage.listView.clickItem(dataPool.get("Template"));
				homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);
				MFilesDialog mFilesDialog = new MFilesDialog(driver);
				mFilesDialog.confirmUndoCheckOut(true);
				Log.fail("Test Case Failed. The properties were not inherited as expected.", driver);
			}

			driver.switchTo().defaultContent();
			homePage.listView.clickItem(dataPool.get("Template"));
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);
			MFilesDialog mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.confirmUndoCheckOut(true);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.27C : Check Out a Template - Add Properties and set values to it and use it to create an object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Check Out a Template - Add Properties and set values to it and use it to create an object.")
	public void SprintTest48_1_27C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Check out the template and make some changes to it.
			//-------------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Template"), "Search only: "+dataPool.get("ObjectType")+"s");

			homePage.listView.clickItem(dataPool.get("Template"));
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);


			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));
			metadatacard.saveAndClose();

			Log.message("2. Check out the template and make some changes to it.");

			//3. Create an object using a template
			//-------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));

			Utility.selectTemplate(dataPool.get("Template"), driver);

			metadatacard = new MetadataCard(driver);
			metadatacard.setInfo(dataPool.get("Properties") + dataPool.get("Object"));
			metadatacard.setCheckInImmediately(true);
			metadatacard.saveAndClose();

			Log.message("3. Create an object using a template.");

			//Verification: To verify if the Added Properties are also added to the Object
			//-----------------------------------------------------------------------------

			metadatacard = new MetadataCard(driver, true);

			if(metadatacard.propertyExists(dataPool.get("Property")) && metadatacard.getPropertyValue(dataPool.get("Property")).equals(dataPool.get("Value")))
				Log.pass("Test Case Passed. The Properties were inherited as expected.");
			else {
				driver.switchTo().defaultContent();
				homePage.listView.clickItem(dataPool.get("Template"));
				homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);
				MFilesDialog mFilesDialog = new MFilesDialog(driver);
				mFilesDialog.confirmUndoCheckOut(true);
				Log.fail("Test Case Failed. The properties were not inherited as expected.", driver);
			}

			driver.switchTo().defaultContent();
			homePage.listView.clickItem(dataPool.get("Template"));
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);
			MFilesDialog mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.confirmUndoCheckOut(true);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.27D : Check Out a Template - Add Properties with no values and use it to create an object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Check Out a Template - Add Properties with no values and use it to create an object.")
	public void SprintTest48_1_27D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Check out the template and make some changes to it.
			//-------------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Template"), "Search only: "+dataPool.get("ObjectType")+"s");

			homePage.listView.clickItem(dataPool.get("Template"));
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);


			MetadataCard metadatacard = new MetadataCard(driver, true);
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));
			metadatacard.saveAndClose();

			Log.message("2. Check out the template and make some changes to it.");

			//3. Create an object using a template
			//-------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));

			Utility.selectTemplate(dataPool.get("Template"), driver);

			metadatacard = new MetadataCard(driver);
			metadatacard.setInfo(dataPool.get("Properties") + dataPool.get("Object"));
			metadatacard.setCheckInImmediately(true);
			metadatacard.saveAndClose();

			Log.message("3. Create an object using a template.");

			//Verification: To verify if the Added Properties are also added to the Object
			//-----------------------------------------------------------------------------

			metadatacard = new MetadataCard(driver, true);

			if(metadatacard.propertyExists(dataPool.get("Property")) && metadatacard.getPropertyValue(dataPool.get("Property")).equals(dataPool.get("Value")))
				Log.pass("Test Case Passed. The Properties were inherited as expected.");
			else {

				driver.switchTo().defaultContent();
				homePage.listView.clickItem(dataPool.get("Template"));
				homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);
				MFilesDialog mFilesDialog = new MFilesDialog(driver);
				mFilesDialog.confirmUndoCheckOut(true);
				Log.fail("Test Case Failed. The properties were not inherited as expected.", driver);
			}

			driver.switchTo().defaultContent();
			homePage.listView.clickItem(dataPool.get("Template"));
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);
			MFilesDialog mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.confirmUndoCheckOut(true);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}


	/**
	 * 55.2.4 : Verify entered keyword value are displayed in the metadata. 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint55", "Metadatacard"}, 
			description = "Verify entered keyword value are displayed in the metadata.")
	public void SprintTest55_2_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String userName = xmlParameters.getParameter("UserName");
			String password = xmlParameters.getParameter("Password");
			driver.get(loginURL); //Launches with the URL

			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX")) //Checks if Login.aspx page is loaded
				throw new Exception ("Browser is not navigated to the Login page.");

			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault2); //Logs into application


			Log.message("1. Logged into the Home View.");

			//2. Open the Select template dialog 
			//------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));

			Log.message("2. Opened the Select template dialog");

			//3. Select the desired extension
			//--------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard = metadatacard.setTemplate(dataPool.get("Extension"));

			Log.message("3. Select the desired extension");

			//4. Set the necessary properties and click the create button
			//------------------------------------------------------------
			metadatacard.setInfo(dataPool.get("Properties")+dataPool.get("Object"));
			metadatacard.addNewProperty(dataPool.get("Property"));
			metadatacard.setCheckInImmediately(true);
			metadatacard.saveAndClose();

			Log.message("4. Set the necessary properties and click the create button.");

			//5. Open the Select Template dialog
			//-----------------------------------
			Utility.quitDriver(driver);
			driver = WebDriverUtils.getDriver();
			driver.get(loginURL); //Launches with the URL

			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX")) //Checks if Login.aspx page is loaded
				throw new Exception ("Browser is not navigated to the Login page.");

			loginPage = new LoginPage(driver); //Instantiates LoginPage class
			homePage = loginPage.loginToWebApplication(userName, password, testVault2); //Logs into application


			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));

			Log.message("5. Open the Select Template dialog");

			//6. Select the Template that was created earlier
			//-----------------------------------------------
			String objectName = dataPool.get("Object")+"."+dataPool.get("Extension");
			Utility.selectTemplate(objectName, driver);

			Log.message("6. Select the Template that was created earlier");

			//Verification: To verify if the Property is added to the metadatacard
			//---------------------------------------------------------------------
			metadatacard = new MetadataCard(driver);

			if(metadatacard.propertyExists(dataPool.get("Property")))
				Log.pass("Test Case Passed. The property was added to the property when the template was selected.");
			else
				Log.fail("Test Case Failed. The property was not added to the property when the template was selected.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 55.2.5 : Verify entered keyword value are displayed in the metadata. 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint55", "Metadatacard"}, 
			description = "Verify entered keyword value are displayed in the metadata.")
	public void SprintTest55_2_5(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String userName = xmlParameters.getParameter("UserName");
			String password = xmlParameters.getParameter("Password");
			driver.get(loginURL); //Launches with the URL

			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX")) //Checks if Login.aspx page is loaded
				throw new Exception ("Browser is not navigated to the Login page.");

			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault2); //Logs into application


			Log.message("1. Logged into the Home View.");

			//2. Open the Select template dialog 
			//------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));

			Log.message("2. Opened the Select template dialog");

			//3. Select the desired extension
			//--------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard = metadatacard.setTemplate(dataPool.get("Extension"));

			Log.message("3. Select the desired extension");

			//4. Set the necessary properties and click the create button
			//------------------------------------------------------------
			metadatacard.setInfo(dataPool.get("Properties")+dataPool.get("Object"));
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));
			metadatacard.setCheckInImmediately(true);
			metadatacard.saveAndClose();

			Log.message("4. Set the necessary properties and click the create button.");

			//5. Open the Select Template dialog
			//-----------------------------------
			Utility.quitDriver(driver);
			driver = WebDriverUtils.getDriver();
			driver.get(loginURL); //Launches with the URL

			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX")) //Checks if Login.aspx page is loaded
				throw new Exception ("Browser is not navigated to the Login page.");

			loginPage = new LoginPage(driver); //Instantiates LoginPage class
			homePage = loginPage.loginToWebApplication(userName, password, testVault2); //Logs into application

			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));

			Log.message("5. Open the Select Template dialog");

			//6. Select the Template that was created earlier
			//-----------------------------------------------
			String objectName = dataPool.get("Object")+"."+dataPool.get("Extension");
			Utility.selectTemplate(objectName, driver);

			Log.message("6. Select the Template that was created earlier");

			//Verification: To verify if the Property is added to the metadatacard
			//---------------------------------------------------------------------
			metadatacard = new MetadataCard(driver);

			if(metadatacard.propertyExists(dataPool.get("Property")) && metadatacard.getPropertyValue(dataPool.get("Property")).equals(dataPool.get("Value")))
				Log.pass("Test Case Passed. The property was added to the property when the template was selected.");
			else
				Log.fail("Test Case Failed. The property was not added to the property when the template was selected.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}


	/**
	 * 55.2.13A : Value for mandatory property Set in Side Pane, Verify in pop up metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint55", "Metadatacard"}, 
			description = "Value for mandatory property Set in Side Pane, Verify in pop up metadatacard")
	public void SprintTest55_2_13A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String userName = xmlParameters.getParameter("UserName");
			String password = xmlParameters.getParameter("Password");
			driver.get(loginURL); //Launches with the URL

			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX")) //Checks if Login.aspx page is loaded
				throw new Exception ("Browser is not navigated to the Login page.");

			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault2); //Logs into application


			Log.message("1. Logged into the Home View.");

			//2. Search for the object with the class
			//----------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType"));

			Log.message("2. Search for the object with the class");

			//3. Set the value for the property in the metadatacard
			//------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("Invalid Test data. The Object was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard.setPropertyValue("Is template", "Yes");
			metadatacard.setPropertyValue(dataPool.get("Property"), "");
			metadatacard.saveAndClose();

			Log.message("3. Set the value for the property in the metadatacard");

			//Verification: To verify if the Value is updated in the Side Pane
			//-----------------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			metadatacard = new MetadataCard(driver);
			if(metadatacard.getPropertyValue(dataPool.get("Property")).equals(""))
				Log.pass("Test Case Passed. The Value was emptied as expected.");
			else
				Log.fail("Test Case Failed. The Value was not emptied", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 55.2.13B : Value for mandatory property Set in Side Pane, Verify in pop up metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint55", "Metadatacard"}, 
			description = "Value for mandatory property Set in Side Pane, Verify in pop up metadatacard")
	public void SprintTest55_2_13B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String userName = xmlParameters.getParameter("UserName");
			String password = xmlParameters.getParameter("Password");
			driver.get(loginURL); //Launches with the URL

			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX")) //Checks if Login.aspx page is loaded
				throw new Exception ("Browser is not navigated to the Login page.");

			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault2); //Logs into application


			Log.message("1. Logged into the Home View.");

			//2. Search for the object with the class
			//----------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType"));

			Log.message("2. Search for the object with the class");

			//3. Set the value for the property in the metadatacard
			//------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("Invalid Test data. The Object was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));

			MetadataCard metadatacard = new MetadataCard(driver, true);
			metadatacard.setPropertyValue("Is template", "Yes");
			metadatacard.setPropertyValue(dataPool.get("Property"), "");
			metadatacard.saveAndClose();

			Log.message("3. Set the value for the property in the metadatacard");

			//Verification: To verify if the Value is updated in the Side Pane
			//-----------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true);
			if(!metadatacard.isEditMode() && metadatacard.getPropertyValue(dataPool.get("Property")).equals(""))
				Log.pass("Test Case Passed. The Value was emptied as expected.");
			else
				Log.fail("Test Case Failed. The Value was not emptied", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 55.2.13C : Mandatory Property can be left empty if 'Is template' value is True (New Object - Task Pane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint55", "Metadatacard"}, 
			description = "Mandatory Property can be left empty if 'Is template' value is True (New Object - Task Pane)")
	public void SprintTest55_2_13C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String userName = xmlParameters.getParameter("UserName");
			String password = xmlParameters.getParameter("Password");
			driver.get(loginURL); //Launches with the URL

			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX")) //Checks if Login.aspx page is loaded
				throw new Exception ("Browser is not navigated to the Login page.");

			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault2); //Logs into application


			Log.message("1. Logged into the Home View.");

			//2. Create a new template with blank Mandatory field
			//---------------------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));
			String objectName = dataPool.get("ObjectName");
			if(Utility.selectTemplate(dataPool.get("Extension"), driver)) 
				objectName = objectName + "." + dataPool.get("Extension");

			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard.setInfo(dataPool.get("Properties")+dataPool.get("ObjectName"));
			metadatacard.setCheckInImmediately(true);
			metadatacard.saveAndClose();

			Log.message("2. Create a new template with blank Mandatory field.");

			//3. Open the properties of the object
			//-------------------------------------
			homePage.searchPanel.search(objectName, "Search only: "+dataPool.get("SearchType"));

			if(!homePage.listView.clickItem(objectName))
				throw new Exception("The Object was not created.");

			metadatacard = new MetadataCard(driver, true);

			Log.message("3. Open the properties of the object");

			//Verification: To verify if the Value of the mandatory property is empty
			//-----------------------------------------------------------------------
			if(metadatacard.getPropertyValue(dataPool.get("Property")).equals(""))
				Log.pass("Test Case Passed. The Value was emptied as expected.");
			else
				Log.fail("Test Case Failed. The Value was not emptied", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 55.2.13D : Mandatory Property can be left empty if 'Is template' value is True (New Object - Operations Menu)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint55", "Metadatacard"}, 
			description = "Mandatory Property can be left empty if 'Is template' value is True (New Object - Operations Menu)")
	public void SprintTest55_2_13D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String userName = xmlParameters.getParameter("UserName");
			String password = xmlParameters.getParameter("Password");
			driver.get(loginURL); //Launches with the URL

			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX")) //Checks if Login.aspx page is loaded
				throw new Exception ("Browser is not navigated to the Login page.");

			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault2); //Logs into application


			Log.message("1. Logged into the Home View.");

			//2. Create a new template with blank Mandatory field
			//---------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));
			String objectName = dataPool.get("ObjectName");
			if(Utility.selectTemplate(dataPool.get("Extension"), driver)) 
				objectName = objectName + "." + dataPool.get("Extension");

			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard.setInfo(dataPool.get("Properties")+dataPool.get("ObjectName"));
			metadatacard.setCheckInImmediately(true);
			metadatacard.saveAndClose();

			Log.message("2. Create a new template with blank Mandatory field.");

			//3. Open the properties of the object
			//-------------------------------------
			homePage.searchPanel.search(objectName, "Search only: "+dataPool.get("SearchType"));

			if(!homePage.listView.clickItem(objectName))
				throw new Exception("The Object was not created.");

			metadatacard = new MetadataCard(driver, true);

			Log.message("3. Open the properties of the object");

			//Verification: To verify if the Value of the mandatory property is empty
			//-----------------------------------------------------------------------
			if(metadatacard.getPropertyValue(dataPool.get("Property")).equals(""))
				Log.pass("Test Case Passed. The Value was emptied as expected.");
			else
				Log.fail("Test Case Failed. The Value was not emptied", driver);

			driver.switchTo().defaultContent();

			homePage.listView.rightClickItem(objectName);

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Delete.Value);


			MFilesDialog mFilesDialog = new MFilesDialog(driver, "Confirm Delete");
			mFilesDialog.confirmDelete();


		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 55.2.14A : Mandatory Property cannot be empty if 'Is template' value is false
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint55", "Metadatacard"}, 
			description = "Mandatory Property cannot be empty if 'Is template' value is false")
	public void SprintTest55_2_14A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String userName = xmlParameters.getParameter("UserName");
			String password = xmlParameters.getParameter("Password");
			driver.get(loginURL); //Launches with the URL

			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX")) //Checks if Login.aspx page is loaded
				throw new Exception ("Browser is not navigated to the Login page.");

			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault2); //Logs into application


			Log.message("1. Logged into the Home View.");

			//2. Search for the object with the class
			//----------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType"));

			Log.message("2. Search for the object with the class");

			//3. Set the value for the property in the metadatacard
			//------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("Invalid Test data. The Object was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard.setPropertyValue("Is template", "No");
			metadatacard.setPropertyValue(dataPool.get("Property"), "");
			metadatacard.saveAndClose();

			Log.message("3. Set the value for the property in the metadatacard");

			//Verification: To verify if the Warning is displayed
			//----------------------------------------------------
			metadatacard = new MetadataCard(driver);
			if(metadatacard.getWarningMessage().equals(dataPool.get("WarningMessage")))
				Log.pass("Test Case Passed. The Warning message appeared as expected.");
			else
				Log.fail("Test Case Failed. The Warning message did not appear.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 55.2.14B : Mandatory Property cannot be empty if 'Is template' value is false (Side Pane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint55", "Metadatacard"}, 
			description = "Mandatory Property cannot be empty if 'Is template' value is false (Side Pane)")
	public void SprintTest55_2_14B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String userName = xmlParameters.getParameter("UserName");
			String password = xmlParameters.getParameter("Password");
			driver.get(loginURL); //Launches with the URL

			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX")) //Checks if Login.aspx page is loaded
				throw new Exception ("Browser is not navigated to the Login page.");

			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault2); //Logs into application


			Log.message("1. Logged into the Home View.");

			//2. Search for the object with the class
			//----------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType"));

			Log.message("2. Search for the object with the class");

			//3. Set the value for the property in the metadatacard
			//------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("Invalid Test data. The Object was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));

			MetadataCard metadatacard = new MetadataCard(driver, true);
			metadatacard.setPropertyValue("Is template", "No");
			metadatacard.setPropertyValue(dataPool.get("Property"), "");
			metadatacard.saveAndClose();

			Log.message("3. Set the value for the property in the metadatacard");

			//Verification: To verify if the Warning is displayed
			//----------------------------------------------------
			metadatacard = new MetadataCard(driver, true);
			if(metadatacard.isEditMode() && metadatacard.getWarningMessage().equals(dataPool.get("WarningMessage")))
				Log.pass("Test Case Passed. The Warning message appeared as expected.");
			else
				Log.fail("Test Case Failed. The Warning message did not appear.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 55.2.14C : Mandatory Property cannot be empty if 'Is template' value is false (New Object)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint55", "Metadatacard"}, 
			description = "Mandatory Property cannot be empty if 'Is template' value is false (New Object)")
	public void SprintTest55_2_14C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String userName = xmlParameters.getParameter("UserName");
			String password = xmlParameters.getParameter("Password");
			driver.get(loginURL); //Launches with the URL

			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX")) //Checks if Login.aspx page is loaded
				throw new Exception ("Browser is not navigated to the Login page.");

			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault2); //Logs into application


			Log.message("1. Logged into the Home View.");

			//2. Create a new template with blank Mandatory field
			//---------------------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));
			String objectName = dataPool.get("ObjectName");
			if(Utility.selectTemplate(dataPool.get("Extension"), driver)) 
				objectName = objectName + "." + dataPool.get("Extension");

			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard.setInfo(dataPool.get("Properties")+dataPool.get("ObjectName"));
			metadatacard.setCheckInImmediately(true);
			metadatacard.saveAndClose();

			Log.message("2. Create a new template with blank Mandatory field.");

			//Verification: To verify if the Warning is displayed
			//----------------------------------------------------
			metadatacard = new MetadataCard(driver);
			if(metadatacard.getWarningMessage().equals(dataPool.get("WarningMessage")))
				Log.pass("Test Case Passed. The Warning message appeared as expected.");
			else
				Log.fail("Test Case Failed. The Warning message did not appear.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 55.2.14D : Mandatory Property cannot be empty if 'Is template' value is false (New Object - Operations Menu)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint55", "Metadatacard"}, 
			description = "Mandatory Property cannot be empty if 'Is template' value is false (New Object - Operations Menu)")
	public void SprintTest55_2_14D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String userName = xmlParameters.getParameter("UserName");
			String password = xmlParameters.getParameter("Password");
			driver.get(loginURL); //Launches with the URL

			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX")) //Checks if Login.aspx page is loaded
				throw new Exception ("Browser is not navigated to the Login page.");

			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault2); //Logs into application


			Log.message("1. Logged into the Home View.");

			//2. Create a new template with blank Mandatory field
			//---------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));
			String objectName = dataPool.get("ObjectName");
			if(Utility.selectTemplate(dataPool.get("Extension"), driver)) 
				objectName = objectName + "." + dataPool.get("Extension");

			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard.setInfo(dataPool.get("Properties")+dataPool.get("ObjectName"));
			metadatacard.setCheckInImmediately(true);
			metadatacard.saveAndClose();

			Log.message("2. Create a new template with blank Mandatory field.");

			//Verification: To verify if the Warning is displayed
			//----------------------------------------------------
			metadatacard = new MetadataCard(driver);
			if(metadatacard.getWarningMessage().equals(dataPool.get("WarningMessage")))
				Log.pass("Test Case Passed. The Warning message appeared as expected.");
			else
				Log.fail("Test Case Failed. The Warning message did not appear.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}
}
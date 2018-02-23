package MFClient.Tests.MetadataOperations;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.ExtentReporter;
import genericLibrary.Log;
import genericLibrary.WebDriverUtils;

import java.util.HashMap;
import java.util.List;
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
public class MultiselectOperations {

	public String xlTestDataWorkBook = null;
	public String loginURL = null;
	public String userName = null;
	public String password = null;
	public String testVault = null;
	public String configURL = null;
	public String userFullName = null;
	public String className = null;
	public String productVersion = null;
	public WebDriver driver = null;

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

			if (xmlParameters.getParameter("driverType").equalsIgnoreCase("IE") || xmlParameters.getParameter("driverType").equalsIgnoreCase("SAFARI")){
				Log.testCaseInfo(xmlParameters.getParameter("driverType").toUpperCase() + " driver does not supports Multi-select.", className, className, productVersion);
				ExtentReporter.skip(xmlParameters.getParameter("driverType").toUpperCase() + " driver does not supports Multi-select.");

				throw new SkipException(xmlParameters.getParameter("driverType").toUpperCase() + " driver does not supports Multi-select.");
			}

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
	 * 100.2.1A : Open Metadatacard after selecting multiple objects (TaskPane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Open Metadatacard after selecting multiple objects (TaskPane).")
	public void SprintTest100_2_1A(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search only: " + dataPool.get("ObjectType"));


			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//--------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//4. Click the Properties option in the task pane
			//------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);


			MetadataCard metadataCard = new MetadataCard(driver, dataPool.get("Objects").split("\n").length + " Objects");

			Log.message("4. Click the Properties option in the task pane.");

			//Verification: To verify if the Metadatacard is displayed as expected
			//---------------------------------------------------------------------
			if(metadataCard.getTitle().equals("(varies)"))
				Log.pass("Test Case Passed. Opening metadatacard after selecting multiple objects works as expected.");
			else 
				Log.fail("Test Case Failed. Metadatacard did not appear as expected on selecting multiple objects.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.1B : Open Metadatacard after selecting multiple objects (Operations menu)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Open Metadatacard after selecting multiple objects (Operations menu).")
	public void SprintTest100_2_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search only: " + dataPool.get("ObjectType"));


			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//4. Click the Properties option in the operations menu
			//------------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value);


			MetadataCard metadataCard = new MetadataCard(driver, dataPool.get("Objects").split("\n").length + " Objects");

			Log.message("4. Click the Properties option in the operations menu.");

			//Verification: To verify if the Metadatacard is displayed as expected
			//---------------------------------------------------------------------
			if(metadataCard.getTitle().equals("(varies)"))
				Log.pass("Test Case Passed. Opening metadatacard after selecting multiple objects works as expected.");
			else 
				Log.fail("Test Case Failed. Metadatacard did not appear as expected on selecting multiple objects.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.1C : Open Metadatacard after selecting multiple objects (Context Menu)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Open Metadatacard after selecting multiple objects (Context Menu).")
	public void SprintTest100_2_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search only: " + dataPool.get("ObjectType"));


			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//4. Right click the last object and Click the Properties option in the context menu
			//------------------------------------------------------------------------------------
			homePage.listView.rightClickItem(dataPool.get("Objects").split("\n")[dataPool.get("Objects").split("\n").length-1]);

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value);


			MetadataCard metadataCard = new MetadataCard(driver, dataPool.get("Objects").split("\n").length + " Objects");

			Log.message("4. Right click the last object and Click the Properties option in the context menu.");

			//Verification: To verify if the Metadatacard is displayed as expected
			//---------------------------------------------------------------------
			if(metadataCard.getTitle().equals("(varies)"))
				Log.pass("Test Case Passed. Opening metadatacard after selecting multiple objects works as expected.");
			else 
				Log.fail("Test Case Failed. Metadatacard did not appear as expected on selecting multiple objects.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.2 : Select multiple object (right pane verification)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Select multiple object (right pane verification).")
	public void SprintTest100_2_2(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search only: " + dataPool.get("ObjectType"));


			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//Verification: To verify if the Metadatacard is displayed as expected
			//---------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);
			if(metadataCard.getTitle().equals("(varies)"))
				Log.pass("Test Case Passed. Right pane after selecting multiple objects works as expected.");
			else 
				Log.fail("Test Case Failed. Right pane was not displayed as expected after selecting multiple objects.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.3 : After multi-select Properties with different values are shown as "(varies)" in metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "After multi-select Properties with different values are shown as '(varies)' in metadatacard.")
	public void SprintTest100_2_3(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			String[] objects = dataPool.get("Objects").split("\n");
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search only: " + dataPool.get("ObjectType"));


			if(!homePage.listView.clickItem(objects[0]))
				throw new SkipException("Invalid Test data. The Specified object was not found in the vault");


			MetadataCard metadatacard = new MetadataCard(driver, true);
			ConcurrentHashMap<String, String> prop1 = metadatacard.getInfo();
			driver.switchTo().defaultContent();

			if(!homePage.listView.clickItem(objects[1]))
				throw new SkipException("Invalid Test data. The Specified object was not found in the vault");


			metadatacard = new MetadataCard(driver, true);
			String[] diff = Utility.compareObjects(prop1, metadatacard.getInfo()).split("\n");
			driver.switchTo().defaultContent();

			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//4. Open the metadatacard
			//-------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);


			metadatacard = new MetadataCard(driver, dataPool.get("Objects").split("\n").length + " Objects");

			Log.message("4. Open the metadatacard.");

			//Verification: To verify if the properties with different properties are displayed as "(Varies)"
			//-------------------------------------------------------------------------------------------------
			ConcurrentHashMap<String, String> actual = metadatacard.getInfo();

			int count = 0;
			for(count = 0; count < diff.length; count++) {
				if(!actual.get(diff[count].split(" : ")[0]).equals("(varies)"))
					break;
			}

			if( count == diff.length)
				Log.pass("Test Case Passed. The properties with different values are displayed as expected.");
			else 
				Log.fail("Test Case Failed. The Properties with different values were not displayed as expected.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.4 : After multi-select values of Properties with different values are shown as "(varies)" in right pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "After multi-select values of Properties with different values are shown as '(varies)' in right pane.")
	public void SprintTest100_2_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			String[] objects = dataPool.get("Objects").split("\n");
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search only: " + dataPool.get("ObjectType"));


			if(!homePage.listView.clickItem(objects[0]))
				throw new SkipException("Invalid Test data. The Specified object was not found in the vault");


			MetadataCard metadatacard = new MetadataCard(driver, true);
			ConcurrentHashMap<String, String> prop1 = metadatacard.getInfo();
			driver.switchTo().defaultContent();

			if(!homePage.listView.clickItem(objects[1]))
				throw new SkipException("Invalid Test data. The Specified object was not found in the vault");


			metadatacard = new MetadataCard(driver, true);
			ConcurrentHashMap<String, String> prop2 = metadatacard.getInfo();
			String[] diff = Utility.compareObjects(prop1, prop2).split("\n");
			driver.switchTo().defaultContent();

			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//4. Check the right pane
			//-------------------------
			metadatacard = new MetadataCard(driver, true);

			Log.message("4. Check the right pane.");

			//Verification: To verify if values of properties with different values are displayed as "(Varies)"
			//-------------------------------------------------------------------------------------------------
			ConcurrentHashMap<String, String> actual = metadatacard.getInfo();

			int count = 0;
			for(count = 0; count < diff.length; count++) {
				if(!actual.get(diff[count].split(" : ")[0]).equals("(varies)"))
					break;
			}

			if( count == diff.length)
				Log.pass("Test Case Passed. The properties with different values are displayed as expected.");
			else 
				Log.fail("Test Case Failed. The Properties with different values were not displayed as expected.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.5 : After multi-select  values of Properties with same values should show the value in metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "After multi-select  values of Properties with same values should show the value in metadatacard.")
	public void SprintTest100_2_5(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			String[] objects = dataPool.get("Objects").split("\n");
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search only: " + dataPool.get("ObjectType"));


			if(!homePage.listView.clickItem(objects[0]))
				throw new SkipException("Invalid Test data. The Specified object was not found in the vault");


			MetadataCard metadatacard = new MetadataCard(driver, true);
			ConcurrentHashMap<String, String> prop1 = metadatacard.getInfo();
			driver.switchTo().defaultContent();

			if(!homePage.listView.clickItem(objects[1]))
				throw new SkipException("Invalid Test data. The Specified object was not found in the vault");


			metadatacard = new MetadataCard(driver, true);
			ConcurrentHashMap<String, String> prop2 = metadatacard.getInfo();
			driver.switchTo().defaultContent();

			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//4. Open the metadatacard
			//-------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			metadatacard = new MetadataCard(driver);

			Log.message("4. Open the metadatacard.");

			//Verification: To verify if the properties with same values are displayed as expected
			//-------------------------------------------------------------------------------------
			ConcurrentHashMap<String, String> actual = metadatacard.getInfo();
			boolean flag = false;

			for (final String key : prop1.keySet()) {
				if (prop1.get(key).equalsIgnoreCase(prop2.get(key))) {
					if(!actual.get(key).equals(prop1.get(key))) {
						flag = false;
						break;
					}
					else
						flag = true;
				}
			}

			if(flag)
				Log.pass("Test Case Passed. The properties with different values are displayed as expected.");
			else 
				Log.fail("Test Case Failed. The Properties with different values were not displayed as expected.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.6 : After multi-select  values of Properties with same values should show the value in right pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "After multi-select  values of Properties with same values should show the value in right pane.")
	public void SprintTest100_2_6(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			String[] objects = dataPool.get("Objects").split("\n");
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search only: " + dataPool.get("ObjectType"));


			if(!homePage.listView.clickItem(objects[0]))
				throw new SkipException("Invalid Test data. The Specified object was not found in the vault");


			MetadataCard metadatacard = new MetadataCard(driver, true);
			ConcurrentHashMap<String, String> prop1 = metadatacard.getInfo();
			driver.switchTo().defaultContent();

			if(!homePage.listView.clickItem(objects[1]))
				throw new SkipException("Invalid Test data. The Specified object was not found in the vault");


			metadatacard = new MetadataCard(driver, true);
			ConcurrentHashMap<String, String> prop2 = metadatacard.getInfo();
			driver.switchTo().defaultContent();

			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//4. Check the right pane
			//-------------------------
			metadatacard = new MetadataCard(driver, true);

			Log.message("4. Check the right pane.");

			//Verification: To verify if the properties with same values are displayed as expected
			//-------------------------------------------------------------------------------------
			ConcurrentHashMap<String, String> actual = metadatacard.getInfo();
			boolean flag = false;

			for (final String key : prop1.keySet()) {
				if (prop1.get(key).equalsIgnoreCase(prop2.get(key))) {
					if(!actual.get(key).equals(prop1.get(key))) {
						flag = false;
						break;
					}
					else
						flag = true;
				}
			}

			if(flag)
				Log.pass("Test Case Passed. The properties with different values are displayed as expected.");
			else 
				Log.fail("Test Case Failed. The Properties with different values were not displayed as expected.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.7A : Displaying permissions of multiple objects in metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Displaying permissions of multiple objects in metadatacard.")
	public void SprintTest100_2_7A(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search only: " + dataPool.get("ObjectType"));


			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//4. Open the metadatacard
			//-------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);


			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("4. Open the metadatacard.");

			//Verification: To verify if the properties with same values are displayed as expected
			//-------------------------------------------------------------------------------------
			if(metadatacard.getPermission().equals(dataPool.get("ExpectedPermission")))
				Log.pass("Test Case Passed. Permission was displayed as expected for multi-selected objects.");
			else 
				Log.fail("Test Case Failed. Permission was not displayed as expected for multi-selected objects.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.7B : Displaying permissions of multiple objects in right pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Displaying permissions of multiple objects in right pane.")
	public void SprintTest100_2_7B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search only: " + dataPool.get("ObjectType"));


			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//4. Check the right pane
			//-------------------------
			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("4. Check the right pane.");

			//Verification: To verify if the properties with same values are displayed as expected
			//-------------------------------------------------------------------------------------
			if(metadatacard.getPermission().equals(dataPool.get("ExpectedPermission")))
				Log.pass("Test Case Passed. Permission was displayed as expected for multi-selected objects.");
			else 
				Log.fail("Test Case Failed. Permission was not displayed as expected for multi-selected objects.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.8A : Displaying Workflows of multiple objects in metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Displaying Workflows of multiple objects in metadatacard.")
	public void SprintTest100_2_8A(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search only: " + dataPool.get("ObjectType"));


			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//4. Open the metadatacard
			//-------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);


			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("4. Open the metadatacard.");

			//Verification: To verify if the properties with same values are displayed as expected
			//-------------------------------------------------------------------------------------
			if(metadatacard.getWorkflow().equals(dataPool.get("Workflow")))
				Log.pass("Test Case Passed. Workflow was displayed as expected for multi-selected objects.");
			else 
				Log.fail("Test Case Failed. Workflow was not displayed as expected for multi-selected objects.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.8B : Displaying Workflows of multiple objects in right pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Displaying Workflows of multiple objects in right pane.")
	public void SprintTest100_2_8B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search only: " + dataPool.get("ObjectType"));


			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//Verification: To verify if the properties with same values are displayed as expected
			//-------------------------------------------------------------------------------------

			MetadataCard metadatacard = new MetadataCard(driver, true);
			if(metadatacard.getWorkflow().equals(dataPool.get("Workflow")))
				Log.pass("Test Case Passed. Workflow was displayed as expected for multi-selected objects.");
			else 
				Log.fail("Test Case Failed. Workflow was not displayed as expected for multi-selected objects.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.9A : Displaying Workflow states of multiple objects in metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "SKIP_MultiSelect", "Sprint44", "Metadatacard","Bug"}, 
			description = "Displaying Workflow states of multiple objects in metadatacard.")
	public void SprintTest100_2_9A(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search only: " + dataPool.get("ObjectType"));


			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//4. Open the metadatacard
			//-------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);


			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("4. Open the metadatacard.");

			//Verification: To verify if the properties with same values are displayed as expected
			//-------------------------------------------------------------------------------------
			if(metadatacard.getWorkflowState().equals(dataPool.get("State")))
				Log.pass("Test Case Passed. Workflow states was displayed as expected for multi-selected objects.");
			else 
				Log.fail("Test Case Failed. Workflow states was not displayed as expected for multi-selected objects.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.9B : Displaying Workflow states of multiple objects in right pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "SKIP_MultiSelect", "Sprint44", "Metadatacard","Bug"}, 
			description = "Displaying Workflow states of multiple objects in right pane.")
	public void SprintTest100_2_9B(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search only: " + dataPool.get("ObjectType"));


			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//Verification: To verify if the properties with same values are displayed as expected
			//-------------------------------------------------------------------------------------

			MetadataCard metadatacard = new MetadataCard(driver, true);
			if(metadatacard.getWorkflowState().equals(dataPool.get("State")))
				Log.pass("Test Case Passed. Workflow states was displayed as expected for multi-selected objects.");
			else 
				Log.fail("Test Case Failed. Workflow states was not displayed as expected for multi-selected objects.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.10A : Adding a property with no value to Multi-object metadatatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Adding a property with no value to Multi-object metadatatacard.")
	public void SprintTest100_2_10A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search only: " + dataPool.get("ObjectType"));


			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//4. Open the metadatacard
			//-------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);


			MetadataCard metadatacard = new MetadataCard(driver);

			if(metadatacard.propertyExists(dataPool.get("Property")))
				throw new SkipException("Invalid Test data. The property already exists in the objects");

			Log.message("4. Open the metadatacard.");

			//5. Add a property
			//-----------------
			if(!metadatacard.addNewProperty(dataPool.get("Property")))
				throw new Exception("The Property was not added to the metadacard.");

			Log.message("5. Add a property.");

			//6. Click the Save button
			//-------------------------
			metadatacard.saveAndClose();

			Log.message("6. Click the Save button.");

			//Verification: To verify if the property is added to the multi-selected objects
			//-------------------------------------------------------------------------------
			String[] objects = dataPool.get("Objects").split("\n");
			int count = 0;
			for(count = 0; count < objects.length; count++) {
				homePage.listView.clickItem(objects[count]);

				metadatacard = new MetadataCard(driver, true);
				if(!metadatacard.propertyExists(dataPool.get("Property")))
					Log.fail("Test Case Failed. The Property was not added to the object - " + objects[count], driver);
				driver.switchTo().defaultContent();
			}

			if(count == objects.length)
				Log.pass("Test Case Passed. The Property was added to all the multi-selected objects.");
			else 
				Log.fail("Test Case Failed. The verification was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.10B : Adding a property with no value to Multi-object right pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Adding a property with no value to Multi-object right pane.")
	public void SprintTest100_2_10B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search only: " + dataPool.get("ObjectType"));


			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//5. Add a property
			//-----------------
			MetadataCard metadatacard = new MetadataCard(driver, true);

			if(metadatacard.propertyExists(dataPool.get("Property")))
				throw new SkipException("Invalid Test data. The property already exists in the objects");

			if(!metadatacard.addNewProperty(dataPool.get("Property")))
				throw new Exception("The Property was not added to the metadacard.");

			Log.message("4. Add a property.");

			//6. Click the Save button
			//-------------------------
			metadatacard.saveAndClose();

			Log.message("5. Click the Save button.");

			//Verification: To verify if the property is added to the multi-selected objects
			//-------------------------------------------------------------------------------
			String[] objects = dataPool.get("Objects").split("\n");
			int count = 0;
			for(count = 0; count < objects.length; count++) {
				homePage.listView.clickItem(objects[count]);

				metadatacard = new MetadataCard(driver, true);
				if(!metadatacard.propertyExists(dataPool.get("Property")))
					Log.fail("Test Case Failed. The Property was not added to the object - " + objects[count], driver);
				driver.switchTo().defaultContent();
			}

			if(count == objects.length)
				Log.pass("Test Case Passed. The Property was added to all the multi-selected objects.");
			else 
				Log.fail("Test Case Failed. The verification was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.11A : Removing a property from Multi-object metadatatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Removing a property from Multi-object metadatatacard.")
	public void SprintTest100_2_11A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search only: " + dataPool.get("ObjectType"));


			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//4. Open the metadatacard
			//-------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);


			MetadataCard metadatacard = new MetadataCard(driver);

			if(!metadatacard.propertyExists(dataPool.get("Property")))
				throw new SkipException("Invalid Test data. The property does not exist in the objects");

			Log.message("4. Open the metadatacard.");

			//5. Remove a property
			//-----------------
			if(!metadatacard.removeProperty(dataPool.get("Property")))
				throw new Exception("The Property was not removed from the metadacard.");

			Log.message("5. Remove a property.");

			//6. Click the Save button
			//-------------------------
			metadatacard.saveAndClose();

			Log.message("6. Click the Save button.");

			//Verification: To verify if the property is added to the multi-selected objects
			//-------------------------------------------------------------------------------
			String[] objects = dataPool.get("Objects").split("\n");
			int count = 0;
			for(count = 0; count < objects.length; count++) {
				homePage.listView.clickItem(objects[count]);

				metadatacard = new MetadataCard(driver, true);
				if(metadatacard.propertyExists(dataPool.get("Property")))
					Log.fail("Test Case Failed. The Property was not removed from the object - " + objects[count], driver);
				driver.switchTo().defaultContent();
			}

			if(count == objects.length)
				Log.pass("Test Case Passed. The Property was removed from all the multi-selected objects.");
			else 
				Log.fail("Test Case Failed. The verification was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.11B : Removing a property from Multi-object right pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Removing a property from Multi-object right pane.")
	public void SprintTest100_2_11B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search only: " + dataPool.get("ObjectType"));


			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//5. Remove a property
			//----------------------
			MetadataCard metadatacard = new MetadataCard(driver, true);

			if(!metadatacard.propertyExists(dataPool.get("Property")))
				throw new SkipException("Invalid Test data. The property does not exist in the objects");

			if(!metadatacard.removeProperty(dataPool.get("Property")))
				throw new Exception("The Property was not removed from the metadacard.");

			Log.message("4. Remove a property.");

			//6. Click the Save button
			//-------------------------
			metadatacard.saveAndClose();

			Log.message("5. Click the Save button.");

			//Verification: To verify if the property is added to the multi-selected objects
			//-------------------------------------------------------------------------------
			String[] objects = dataPool.get("Objects").split("\n");
			int count = 0;
			for(count = 0; count < objects.length; count++) {
				homePage.listView.clickItem(objects[count]);

				metadatacard = new MetadataCard(driver, true);
				if(metadatacard.propertyExists(dataPool.get("Property")))
					Log.fail("Test Case Failed. The Property was not removed from the object - " + objects[count], driver);
				driver.switchTo().defaultContent();
			}

			if(count == objects.length)
				Log.pass("Test Case Passed. The Property was removed from all the multi-selected objects.");
			else 
				Log.fail("Test Case Failed. The verification was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.12A : Properties listed in Add Property list should be available for all the selected items
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Properties listed in Add Property list should be available for all the selected items.")
	public void SprintTest100_2_12A(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null;  HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			homePage = LoginPage.launchDriverAndLogin(driver, true);

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------

			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");

			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");


			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//4. Open the metadatacard
			//-------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);


			Log.message("4. Open the metadatacard.");

			//5. Check the properties available to add
			//-----------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);
			List<String> expected = metadatacard.getAvailableAddProperties();
			metadatacard.clickDiscardButton();

			Log.message("5. Check the properties available to add.");

			//Verification: To verify if only the properties that are available for selected objects are listed
			//--------------------------------------------------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);

			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");

			String[] objects = dataPool.get("Objects").split("\n");
			int count = 0;
			for(count = 0; count < objects.length; count++) {
				homePage.listView.clickItem(objects[count]);

				metadatacard = new MetadataCard(driver, true);
				List<String> actual = metadatacard.getAvailableAddProperties();
				for(int counter = 0; counter < expected.size(); counter++) {
					if(actual.indexOf(expected.get(counter)) == -1)
						Log.fail("Test Case Failed. Property("+ expected.get(counter) +") that were not available for all selected objects was also listed.", driver);
				}
				driver.switchTo().defaultContent();
			}

			if(count == objects.length)
				Log.pass("Test Case Passed. Only properties that were available for all selected objects was listed.");
			else 
				Log.fail("Test Case Failed. The verification was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {

			if (homePage != null)
				try{homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");}catch (Exception e0) {Log.exception(e0, driver);} //End catch

			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.12B : Properties listed in Add Property list should be available for all the selected items
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Properties listed in Add Property list should be available for all the selected items.")
	public void SprintTest100_2_12B(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null;  HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			homePage = LoginPage.launchDriverAndLogin(driver, true);

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");

			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");


			Log.message("1. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("2. Select multiple objects.");

			//4. Check the properties available to add
			//-----------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver, true);
			List<String> expected = metadatacard.getAvailableAddProperties();
			metadatacard.clickDiscardButton();

			Log.message("3. Check the properties available to add.");

			//Verification: To verify if only the properties that are available for selected objects are listed
			//--------------------------------------------------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);

			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");

			String[] objects = dataPool.get("Objects").split("\n");
			int count = 0;
			for(count = 0; count < objects.length; count++) {
				homePage.listView.clickItem(objects[count]);

				metadatacard = new MetadataCard(driver, true);
				List<String> actual = metadatacard.getAvailableAddProperties();
				for(int counter = 0; counter < expected.size(); counter++) {
					if(actual.indexOf(expected.get(counter)) == -1)
						Log.fail("Test Case Failed. Property("+ expected.get(counter) +") that were not available for the selected objects was also listed.", driver);
				}
				driver.switchTo().defaultContent();
			}

			if(count == objects.length)
				Log.pass("Test Case Passed. Only properties that were available for all selected objects was listed.");
			else 
				Log.fail("Test Case Failed. The verification was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if (homePage != null)
				try{homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");}catch (Exception e0) {Log.exception(e0, driver);} //End catch

			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.13A : Setting values to Properties in multi-select metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Setting values to Properties in multi-select metadatacard.")
	public void SprintTest100_2_13A(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null;  HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			homePage = LoginPage.launchDriverAndLogin(driver, true);

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------

			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");

			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//4. Open the metadatacard
			//-------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);


			Log.message("4. Open the metadatacard.");

			//5. Set value to a property
			//---------------------------
			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));
			metadatacard.saveAndClose();

			Log.message("5. Set value to a property.");

			//Verification: To verify if only the properties that are available for selected objects are listed
			//--------------------------------------------------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);

			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");

			String[] objects = dataPool.get("Objects").split("\n");
			int count = 0;
			for(count = 0; count < objects.length; count++) {
				homePage.listView.clickItem(objects[count]);

				metadatacard = new MetadataCard(driver, true);
				if(!metadatacard.getPropertyValue(dataPool.get("Property")).equals(dataPool.get("Value")))
					Log.fail("Test Case Failed. Properties that were not available for all selected objects was also listed.", driver);
				driver.switchTo().defaultContent();
			}

			if(count == objects.length)
				Log.pass("Test Case Passed. Only properties that were available for all selected objects was listed.");
			else 
				Log.fail("Test Case Failed. The verification was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if (homePage != null)
				try{homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");}catch (Exception e0) {Log.exception(e0, driver);} //End catch

			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.13B : Setting values to Properties in multi-select right pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Setting values to Properties in multi-select right pane.")
	public void SprintTest100_2_13B(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null;  HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			homePage = LoginPage.launchDriverAndLogin(driver, true);

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------

			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");

			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//4. Set value to a property
			//---------------------------
			MetadataCard metadatacard = new MetadataCard(driver, true);
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));
			metadatacard.saveAndClose();

			Log.message("4. Set value to a property.");

			//Verification: To verify if only the properties that are available for selected objects are listed
			//--------------------------------------------------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);

			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");

			String[] objects = dataPool.get("Objects").split("\n");
			int count = 0;
			for(count = 0; count < objects.length; count++) {
				homePage.listView.clickItem(objects[count]);

				metadatacard = new MetadataCard(driver, true);
				if(!metadatacard.getPropertyValue(dataPool.get("Property")).equals(dataPool.get("Value")))
					Log.fail("Test Case Failed. Properties that were not available for all selected objects was also listed.", driver);
				driver.switchTo().defaultContent();
			}

			if(count == objects.length)
				Log.pass("Test Case Passed. Only properties that were available for all selected objects was listed.");
			else 
				Log.fail("Test Case Failed. The verification was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {

			if (homePage != null)
				try{homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");}catch (Exception e0) {Log.exception(e0, driver);} //End catch

			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.14A : Setting permission in multi-select metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Setting permission inmulti-select metadatacard.")
	public void SprintTest100_2_14A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;  HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			homePage = LoginPage.launchDriverAndLogin(driver, true);

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------


			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");

			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//4. Open the metadatacard
			//-------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);


			Log.message("4. Open the metadatacard.");

			//5. Change the permission
			//---------------------------
			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard.setPermission(dataPool.get("Permission"));
			metadatacard.saveAndClose();

			Log.message("5. Change the permission.");

			//Verification: To verify if the permission is set to all the selected objects
			//-----------------------------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);

			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");

			String[] objects = dataPool.get("Objects").split("\n");
			int count = 0;
			for(count = 0; count < objects.length; count++) {
				homePage.listView.clickItem(objects[count]);

				metadatacard = new MetadataCard(driver, true);
				if(!metadatacard.getPermission().equals(dataPool.get("Permission"))) {
					driver.switchTo().defaultContent();
					Log.fail("Test Case Failed. Permission was not set to '" + objects[count] + "'.", driver);
				}
				driver.switchTo().defaultContent();
			}

			if(count == objects.length)
				Log.pass("Test Case Passed. Permission was set to all objects as expected.");
			else 
				Log.fail("Test Case Failed. The verification was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {

			if (homePage != null)
				try{homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");}catch (Exception e0) {Log.exception(e0, driver);} //End catch

			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.14B : Setting permission in multi-select metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Setting permission inmulti-select metadatacard.")
	public void SprintTest100_2_14B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;  HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			homePage = LoginPage.launchDriverAndLogin(driver, true);

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------


			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");

			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//4. Change the permission
			//---------------------------
			MetadataCard metadatacard = new MetadataCard(driver, true);
			metadatacard.setPermission(dataPool.get("Permission"));
			metadatacard.saveAndClose();

			Log.message("4. Change the permission.");

			//Verification: To verify if the permission is set to all the selected objects
			//-----------------------------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);

			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");

			String[] objects = dataPool.get("Objects").split("\n");
			int count = 0;
			for(count = 0; count < objects.length; count++) {
				homePage.listView.clickItem(objects[count]);

				metadatacard = new MetadataCard(driver, true);
				if(!metadatacard.getPermission().equals(dataPool.get("Permission"))) {
					driver.switchTo().defaultContent();
					Log.fail("Test Case Failed. Permission was not set to '" + objects[count] + "'.", driver);
				}
				driver.switchTo().defaultContent();
			}

			if(count == objects.length)
				Log.pass("Test Case Passed. Permission was set to all objects as expected.");
			else 
				Log.fail("Test Case Failed. The verification was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {

			if (homePage != null)
				try{homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");}catch (Exception e0) {Log.exception(e0, driver);} //End catch

			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.15A : Set Workflow and state in multi-select metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "SKIP_MultiSelect", "Sprint44", "Metadatacard","Bug"}, 
			description = "Set Workflow and state in multi-select metadatacard.")
	public void SprintTest100_2_15A(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null;  HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			homePage = LoginPage.launchDriverAndLogin(driver, true);

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------

			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");

			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//4. Open the metadatacard
			//-------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);


			Log.message("4. Open the metadatacard.");

			//5. Set Workflow and state
			//---------------------------
			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard.setWorkflow(dataPool.get("Workflow"));
			metadatacard.setWorkflowState(dataPool.get("State"));
			metadatacard.saveAndClose();

			Log.message("5. Set Workflow and state.");

			//Verification: To verify if the Workflow and state is set to all the selected objects
			//--------------------------------------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);

			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");

			String[] objects = dataPool.get("Objects").split("\n");
			int count = 0;
			for(count = 0; count < objects.length; count++) {
				homePage.listView.clickItem(objects[count]);

				metadatacard = new MetadataCard(driver, true);
				if(!metadatacard.getWorkflowState().equals(dataPool.get("State"))) {
					driver.switchTo().defaultContent();
					Log.fail("Test Case Failed. The Workflow state was not set to '" + objects[count] + "'.", driver);
				}
				if(!metadatacard.getWorkflow().equals(dataPool.get("Workflow"))) {
					driver.switchTo().defaultContent();
					Log.fail("Test Case Failed. Workflow was not set to '" + objects[count] + "'.", driver);
				}
				driver.switchTo().defaultContent();
			}

			if(count == objects.length)
				Log.pass("Test Case Passed. Workflow and state was set to all objects as expected.");
			else 
				Log.fail("Test Case Failed. The verification was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {

			if (homePage != null)
				try{homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");}catch (Exception e0) {Log.exception(e0, driver);} //End catch

			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.15B : Set Workflow and state in multi-select right pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "SKIP_MultiSelect", "Sprint44", "Metadatacard","Bug"}, 
			description = "Set Workflow and state in multi-select right pane.")
	public void SprintTest100_2_15B(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null;  HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			homePage = LoginPage.launchDriverAndLogin(driver, true);

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------

			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");

			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//4. Set Workflow and state
			//---------------------------
			MetadataCard metadatacard = new MetadataCard(driver, true);
			metadatacard.setWorkflow(dataPool.get("Workflow"));
			metadatacard.setWorkflowState(dataPool.get("State"));
			metadatacard.saveAndClose();

			Log.message("4. Set Workflow and state.");

			//Verification: To verify if the Workflow and state is set to all the selected objects
			//--------------------------------------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);

			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");

			String[] objects = dataPool.get("Objects").split("\n");
			int count = 0;
			for(count = 0; count < objects.length; count++) {
				homePage.listView.clickItem(objects[count]);

				metadatacard = new MetadataCard(driver, true);
				if(!metadatacard.getWorkflowState().equals(dataPool.get("State"))) {
					driver.switchTo().defaultContent();
					Log.fail("Test Case Failed. The Workflow state was not set to '" + objects[count] + "'.", driver);
				}
				if(!metadatacard.getWorkflow().equals(dataPool.get("Workflow"))) {
					driver.switchTo().defaultContent();
					Log.fail("Test Case Failed. Workflow was not set to '" + objects[count] + "'.", driver);
				}
				driver.switchTo().defaultContent();
			}

			if(count == objects.length)
				Log.pass("Test Case Passed. Workflow and state was set to all objects as expected.");
			else 
				Log.fail("Test Case Failed. The verification was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if (homePage != null)
				try{homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");}catch (Exception e0) {Log.exception(e0, driver);} //End catch

			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.16A : Created by' user displayed in multi-select metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Created by' user displayed in multi-select metadatacard.")
	public void SprintTest100_2_16A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;  HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			homePage = LoginPage.launchDriverAndLogin(driver, true);

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------

			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");

			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//4. Open the metadatacard
			//-------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);


			Log.message("4. Open the metadatacard");

			//5. Fetch created by user
			//---------------------------
			MetadataCard metadatacard = new MetadataCard(driver);
			String actual = metadatacard.getCreatedBy();
			metadatacard.clickDiscardButton();

			Log.message("5. Fetch created by user.");

			//Verification: To verify if the created by property shows the expected value
			//----------------------------------------------------------------------------
			if(actual.equals(dataPool.get("Created")))
				Log.pass("Test Case Passed. The Expected value was shown in the Created by property.");
			else
				Log.fail("Test Case Failed. The Expected value was not shown in Created by property", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if (homePage != null)
				try{homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");}catch (Exception e0) {Log.exception(e0, driver);} //End catch

			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.16B : Created by' user displayed in multi-select metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Created by' user displayed in multi-select metadatacard.")
	public void SprintTest100_2_16B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;  HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			homePage = LoginPage.launchDriverAndLogin(driver, true);

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------

			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");

			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//4. Fetch created by user
			//---------------------------

			MetadataCard metadatacard = new MetadataCard(driver, true);
			String actual = metadatacard.getCreatedBy();
			driver.switchTo().defaultContent();

			Log.message("4. Fetch created by user.");

			//Verification: To verify if the created by property shows the expected value
			//----------------------------------------------------------------------------
			if(actual.equals(dataPool.get("Created")))
				Log.pass("Test Case Passed. The Expected value was shown in the Created by property.");
			else
				Log.fail("Test Case Failed. The Expected value was not shown in Created by property", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {

			if (homePage != null)
				try{homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");}catch (Exception e0) {Log.exception(e0, driver);} //End catch

			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.17A : Object Type Icon displayed in metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard", "Bug"}, 
			description = "Object Type Icon displayed in metadatacard.")
	public void SprintTest100_2_17A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;  HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			homePage = LoginPage.launchDriverAndLogin(driver, true);

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------

			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");

			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//4. Open the metadatacard
			//-------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);


			Log.message("4. Open the metadatacard", driver);

			//5. Fetch the object type icon
			//------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);
			String actual = metadatacard.getObjectIcon();
			metadatacard.clickDiscardButton();

			Log.message("5. Fetch the object type icon.");

			//Verification: To verify if the expected icon is displayed
			//----------------------------------------------------------
			if(actual.contains(dataPool.get("Expected")))
				Log.pass("Test Case Passed. The Expected icon was displayed in the metadatacard.");
			else
				Log.fail("Test Case Failed. The Expected icon was not displayed in the metadatacard, Expected icon: '" + dataPool.get("Expected") + "' vs. actual full icon info: '" + actual + "'" , driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {

			if (homePage != null)
				try{homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");}catch (Exception e0) {Log.exception(e0, driver);} //End catch

			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.17B : Object Type Icon displayed in metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard", "Bug"}, 
			description = "Object Type Icon displayed in metadatacard.")
	public void SprintTest100_2_17B(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null;  HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			homePage = LoginPage.launchDriverAndLogin(driver, true);

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------

			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");

			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//4. Fetch the object type icon
			//------------------------------

			MetadataCard metadatacard = new MetadataCard(driver, true);
			String actual = metadatacard.getObjectIcon();
			driver.switchTo().defaultContent();

			Log.message("4. Fetch the object type icon.", driver);

			//Verification: To verify if the expected icon is displayed
			//----------------------------------------------------------
			if(actual.contains(dataPool.get("Expected")))
				Log.pass("Test Case Passed. The Expected icon was displayed in the right pane.");
			else
				Log.fail("Test Case Failed. The Expected icon was not displayed in the right pane, Expected icon: '" + dataPool.get("Expected") + "' vs. actual full icon info: '" + actual + "'" , driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if (homePage != null)
				try{homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");}catch (Exception e0) {Log.exception(e0, driver);} //End catch

			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.18A : Object Type displayed in metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard", "Bug"}, 
			description = "Object Type displayed in metadatacard.")
	public void SprintTest100_2_18A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;  HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			homePage = LoginPage.launchDriverAndLogin(driver, true);

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------

			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");

			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//4. Open the metadatacard
			//-------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);


			Log.message("4. Open the metadatacard");

			//5. Fetch the object type 
			//-------------------------
			MetadataCard metadatacard = new MetadataCard(driver);
			String actual = metadatacard.getObjectType();
			metadatacard.clickDiscardButton();

			Log.message("5. Fetch the object type.");

			//Verification: To verify if the expected object type is displayed
			//------------------------------------------------------------------
			if(actual.contains(dataPool.get("Expected")))
				Log.pass("Test Case Passed. The Expected object type was displayed in the metadatacard.");
			else
				Log.fail("Test Case Failed. The Expected object type was not displayed in the metadatacard", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if (homePage != null)
				try{homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");}catch (Exception e0) {Log.exception(e0, driver);} //End catch

			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.18B : Object Type displayed in right pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard", "Bug"}, 
			description = "Object Type displayed in right pane.")
	public void SprintTest100_2_18B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;  HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			homePage = LoginPage.launchDriverAndLogin(driver, true);

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			//1. Login to the Home View.
			//----------------------------

			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");

			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//4. Fetch the object type
			//-------------------------

			MetadataCard metadatacard = new MetadataCard(driver, true);
			String actual = metadatacard.getObjectType();
			driver.switchTo().defaultContent();

			Log.message("4. Fetch the object type.");

			//Verification: To verify if the expected object type is displayed
			//------------------------------------------------------------------
			if(actual.contains(dataPool.get("Expected")))
				Log.pass("Test Case Passed. The Expected object type was displayed in the right pane.");
			else
				Log.fail("Test Case Failed. The Expected object type was not displayed in the right pane.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if (homePage != null)
				try{homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");}catch (Exception e0) {Log.exception(e0, driver);} //End catch

			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.19A : Object Title displayed in multi-select metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard", "Bug"}, 
			description = "Object Title displayed in multi-select metadatacard.")
	public void SprintTest100_2_19A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;  HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			homePage = LoginPage.launchDriverAndLogin(driver, true);
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------

			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");

			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//4. Open the metadatacard
			//-------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);


			Log.message("4. Open the metadatacard");

			//5. Collapse the header
			//-------------------------
			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard.collapseHeader(true);

			Log.message("5. Collapse the header.");

			//Verification: To verify if the Object title is displayed as expected
			//---------------------------------------------------------------------
			if(metadatacard.getTitle().equals(dataPool.get("Expected"))) {
				metadatacard.collapseHeader(false);
				metadatacard.clickDiscardButton();
				Log.pass("Test Case Passed. The Expected object title was displayed in the metadatacard.");
			}
			else {
				metadatacard.collapseHeader(false);
				metadatacard.clickDiscardButton();
				Log.fail("Test Case Failed. The Expected object title was not displayed in the metadatacard", driver);
			}

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if (homePage != null)
				try{homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");}catch (Exception e0) {Log.exception(e0, driver);} //End catch

			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.19B : Object Title displayed in multi-select metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard", "Bug"}, 
			description = "Object Title displayed in multi-select metadatacard.")
	public void SprintTest100_2_19B(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null;  HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			homePage = LoginPage.launchDriverAndLogin(driver, true);

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------


			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");

			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//5. Collapse the header
			//-------------------------
			MetadataCard metadatacard = new MetadataCard(driver, true);
			metadatacard.collapseHeader(true);

			Log.message("4. Collapse the header.");

			//Verification: To verify if the Object title is displayed as expected
			//---------------------------------------------------------------------
			if(metadatacard.getTitle().equals(dataPool.get("Expected"))) {
				metadatacard.collapseHeader(false);
				driver.switchTo().defaultContent();
				Log.pass("Test Case Passed. The Expected object title was displayed in the metadatacard.");
			}
			else {
				metadatacard.collapseHeader(false);
				driver.switchTo().defaultContent();
				Log.fail("Test Case Failed. The Expected object title was not displayed in the metadatacard", driver);
			}

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if (homePage != null)
				try{homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");}catch (Exception e0) {Log.exception(e0, driver);} //End catch

			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.20 : Object Title displayed in metadatacard dialog
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard", "Bug"}, 
			description = "Object Title displayed in metadatacard dialog.")
	public void SprintTest100_2_20(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;  HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			homePage = LoginPage.launchDriverAndLogin(driver, true);

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------


			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");

			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//4. Open the metadatacard
			//-------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);


			Log.message("4. Open the metadatacard");

			//5. Check the dialog title
			//--------------------------
			String expectedTitle = dataPool.get("Objects").split("\n").length + " Objects";

			Log.message("5. Check the dialog title.");

			//Verification: To verify if the dialog title is displayed as expected
			//---------------------------------------------------------------------
			if(MFilesDialog.exists(driver, expectedTitle))
				Log.pass("Test Case Passed. The Expected dialog title was displayed in the metadatacard.");
			else
				Log.fail("Test Case Failed. The Expected dialog title was not displayed in the metadatacard", driver);

			MetadataCard metadatacard = new MetadataCard(driver, expectedTitle);
			metadatacard.clickDiscardButton();

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if (homePage != null)
				try{homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");}catch (Exception e0) {Log.exception(e0, driver);} //End catch

			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.21 : Performing Pop-out metadatacard in multi-select right pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard", "Bug"}, 
			description = "Performing Pop-out metadatacard in multi-select right pane.")
	public void SprintTest100_2_21(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;  HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			homePage = LoginPage.launchDriverAndLogin(driver, true);

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------


			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");

			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//4. Click the Pop-out metadatacard option in the  right pane
			//------------------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver, true);
			ConcurrentHashMap<String, String> expected = metadatacard.getInfo();

			metadatacard.popOutMetadatacard();


			Log.message("4. Click the Pop-out metadatacard option in the  right pane.");

			//Verification: To verify if the Metadatacard dialog appears with the same details
			//---------------------------------------------------------------------------------
			metadatacard = new MetadataCard(driver);
			if(Utility.compareObjects(metadatacard.getInfo(), expected).equals(""))
				Log.pass("Test Case Passed. The Expected dialog title was displayed in the metadatacard.");
			else
				Log.fail("Test Case Failed. The Expected dialog title was not displayed in the metadatacard", driver);
			metadatacard.clickDiscardButton();
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if (homePage != null)
				try{homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");}catch (Exception e0) {Log.exception(e0, driver);} //End catch

			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.22A : Setting Comments for multi-selected objects in metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard", "Bug"}, 
			description = "Setting Comments for multi-selected objects in metadatacard.")
	public void SprintTest100_2_22A(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null;  HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			homePage = LoginPage.launchDriverAndLogin(driver, true);

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------


			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");

			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//4. Open the metadatacard
			//-------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("4. Open the metadatacard");

			//5. Set a comment
			//-----------------
			metadatacard.setComments(dataPool.get("Comment"));
			metadatacard.saveAndClose();

			Log.message("5. Set a comment.");

			//Verification: To verify if the Comment is set for all the selected objects
			//---------------------------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);

			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");
			String[] objects = dataPool.get("Objects").split("\n");
			int count = 0;
			for(count = 0; count < objects.length; count++) {
				if(!homePage.listView.getColumnValueByItemName(objects[count], "Comment").equals(dataPool.get("Comment").replace("\n", " ")))
					Log.fail("Test Case Failed. The Comment was not set to all the selected objects.", driver);
			}

			if(count == objects.length)
				Log.pass("Test Case Passed. The given comment was set to all the selected objects.");
			else
				Log.fail("Test Case Failed. Verification was not successful.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if (homePage != null)
				try{homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");}catch (Exception e0) {Log.exception(e0, driver);} //End catch

			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.22B : Setting Comments for multi-selected objects in right pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard", "Bug"}, 
			description = "Setting Comments for multi-selected objects in right pane.")
	public void SprintTest100_2_22B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;  HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			homePage = LoginPage.launchDriverAndLogin(driver, true);

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------


			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");

			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.");

			//4. Set a comment
			//-----------------
			MetadataCard metadatacard = new MetadataCard(driver, true);
			metadatacard.setComments(dataPool.get("Comment"));
			metadatacard.saveAndClose();

			Log.message("4. Set a comment.");

			//Verification: To verify if the Comment is set for all the selected objects
			//---------------------------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);

			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");
			String[] objects = dataPool.get("Objects").split("\n");
			int count = 0;
			for(count = 0; count < objects.length; count++) {
				if(!homePage.listView.getColumnValueByItemName(objects[count], "Comment").equals(dataPool.get("Comment").replace("\n", " ")))
					Log.fail("Test Case Failed. The Comment was not set to all the selected objects.", driver);
			}

			if(count == objects.length)
				Log.pass("Test Case Passed. The given comment was set to all the selected objects.");
			else
				Log.fail("Test Case Failed. Verification was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if (homePage != null)
				try{homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");}catch (Exception e0) {Log.exception(e0, driver);} //End catch

			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.23A : Check out multi selected objects make changes in metadatacard, perfrom undo check out
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Check out multi selected objects make changes in metadatacard, perfrom undo check out.")
	public void SprintTest100_2_23A(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null;  HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			homePage = LoginPage.launchDriverAndLogin(driver, true);

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------


			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");

			Log.message("2. Search for objects.");

			//3. Select multiple objects and check out
			//-----------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);


			Log.message("3. Select multiple objects.");

			//4. Open the metadatacard
			//-------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);


			Log.message("4. Open the metadatacard.");

			//5. Set value to a property
			//---------------------------
			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));
			metadatacard.saveAndClose();

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);
			MFilesDialog mFilesDialog = new MFilesDialog(driver, "Confirm");
			mFilesDialog.confirmUndoCheckOut(true);

			Log.message("5. Set value to a property.");

			//Verification: To verify if only the properties that are available for selected objects are listed
			//--------------------------------------------------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);

			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");

			String[] objects = dataPool.get("Objects").split("\n");
			int count = 0;
			for(count = 0; count < objects.length; count++) {
				homePage.listView.clickItem(objects[count]);

				metadatacard = new MetadataCard(driver, true);
				if(metadatacard.getPropertyValue(dataPool.get("Property")).equals(dataPool.get("Value")))
					Log.fail("Test Case Failed. The value prevailed even after undo check out.", driver);
				driver.switchTo().defaultContent();
			}

			if(count == objects.length)
				Log.pass("Test Case Passed. The value set after check out was reverted.");
			else 
				Log.fail("Test Case Failed. The verification was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if (homePage != null)
				try{homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");}catch (Exception e0) {Log.exception(e0, driver);} //End catch

			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.23B : Check out multi selected objects make changes in right pane, perfrom undo check out
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Check out multi selected objects make changes in right pane, perfrom undo check out.")
	public void SprintTest100_2_23B(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null;  HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			homePage = LoginPage.launchDriverAndLogin(driver, true);

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------

			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");

			Log.message("2. Search for objects.");

			//3. Select multiple objects and check out
			//-----------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);

			Log.message("3. Select multiple objects.");

			//4. Set value to a property
			//---------------------------
			MetadataCard metadatacard = new MetadataCard(driver, true);
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));
			metadatacard.saveAndClose();

			Log.message("4. Set value to a property.");

			//5. perform Undo Check out
			//--------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);
			MFilesDialog mFilesDialog = new MFilesDialog(driver, "Confirm");
			mFilesDialog.confirmUndoCheckOut(true);

			Log.message("5. perform Undo Check out.");

			//Verification: To verify if only the properties that are available for selected objects are listed
			//--------------------------------------------------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");

			String[] objects = dataPool.get("Objects").split("\n");
			int count = 0;
			for(count = 0; count < objects.length; count++) {
				homePage.listView.clickItem(objects[count]);

				metadatacard = new MetadataCard(driver, true);
				if(metadatacard.getPropertyValue(dataPool.get("Property")).equals(dataPool.get("Value")))
					Log.fail("Test Case Failed. The value prevailed even after undo check out.", driver);
				driver.switchTo().defaultContent();
			}

			if(count == objects.length)
				Log.pass("Test Case Passed. The value set after check out was reverted.");
			else 
				Log.fail("Test Case Failed. The verification was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if (homePage != null)
				try{homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");}catch (Exception e0) {Log.exception(e0, driver);} //End catch

			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.24A : Open Metadatacard after selecting multiple objects using Shift Key (TaskPane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Open Metadatacard after selecting multiple objects using Shift Key (TaskPane).")
	public void SprintTest100_2_24A(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search only: " + dataPool.get("ObjectType"));


			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			int start = Integer.parseInt(dataPool.get("StartIndex"));
			int end = Integer.parseInt(dataPool.get("EndIndex"));

			homePage.listView.shiftclickMultipleItemsByIndex(start, end);


			Log.message("3. Select multiple objects.");

			//4. Click the Properties option in the task pane
			//------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);


			MetadataCard metadataCard = new MetadataCard(driver, end-start+1 + " Objects");

			Log.message("4. Click the Properties option in the task pane.");

			//Verification: To verify if the Metadatacard is displayed as expected
			//---------------------------------------------------------------------
			if(metadataCard.getTitle().equals("(varies)"))
				Log.pass("Test Case Passed. Multi-select using shift works as expected.");
			else 
				Log.fail("Test Case Failed. Metadatacard did not appear as expected on selecting multiple objects.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.24B : Open Metadatacard after selecting multiple objects using Shift Key  (Operations menu)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Open Metadatacard after selecting multiple objects using Shift Key  (Operations menu).")
	public void SprintTest100_2_24B(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search only: " + dataPool.get("ObjectType"));


			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			int start = Integer.parseInt(dataPool.get("StartIndex"));
			int end = Integer.parseInt(dataPool.get("EndIndex"));

			homePage.listView.shiftclickMultipleItemsByIndex(start, end);


			Log.message("3. Select multiple objects.");

			//4. Click the Properties option in the Operations menu
			//------------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value);


			MetadataCard metadataCard = new MetadataCard(driver, end-start+1 + " Objects");

			Log.message("4. Click the Properties option in the Operations menu.");

			//Verification: To verify if the Metadatacard is displayed as expected
			//---------------------------------------------------------------------
			if(metadataCard.getTitle().equals("(varies)"))
				Log.pass("Test Case Passed. Multi-select using shift works as expected.");
			else 
				Log.fail("Test Case Failed. Metadatacard did not appear as expected on selecting multiple objects.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.24C : Open Metadatacard after selecting multiple objects using Shift Key (Context Menu)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Open Metadatacard after selecting multiple objects using Shift Key (Context Menu).")
	public void SprintTest100_2_24C(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search only: " + dataPool.get("ObjectType"));


			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			int start = Integer.parseInt(dataPool.get("StartIndex"));
			int end = Integer.parseInt(dataPool.get("EndIndex"));

			homePage.listView.shiftclickMultipleItemsByIndex(start, end);


			Log.message("3. Select multiple objects.");

			//4. Click the Properties option in the Context menu
			//---------------------------------------------------
			homePage.listView.rightClickItemByIndex(end);

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value);


			MetadataCard metadataCard = new MetadataCard(driver, end-start+1 + " Objects");

			Log.message("4. Click the Properties option in the Context menu.");

			//Verification: To verify if the Metadatacard is displayed as expected
			//---------------------------------------------------------------------
			if(metadataCard.getTitle().equals("(varies)"))
				Log.pass("Test Case Passed. Multi-select using shift works as expected.");
			else 
				Log.fail("Test Case Failed. Metadatacard did not appear as expected on selecting multiple objects.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.2.24D : Select multiple objects using Shift Key (right pane verification)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Select multiple objects using Shift Key (right pane verification).")
	public void SprintTest100_2_24D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search only: " + dataPool.get("ObjectType"));


			Log.message("2. Search for objects.");

			//3. Select multiple objects
			//----------------------------
			int start = Integer.parseInt(dataPool.get("StartIndex"));
			int end = Integer.parseInt(dataPool.get("EndIndex"));

			homePage.listView.shiftclickMultipleItemsByIndex(start, end);


			Log.message("3. Select multiple objects.");

			//Verification: To verify if the Metadatacard is displayed as expected
			//---------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);
			if(metadataCard.getTitle().equals("(varies)"))
				Log.pass("Test Case Passed. Multi-select using shift works as expected.");
			else 
				Log.fail("Test Case Failed. Metadatacard did not appear as expected on selecting multiple objects.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}


}

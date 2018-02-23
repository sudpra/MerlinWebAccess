package MFClient.Tests.MetadataOperations;

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
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class RequiredProperties {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String userName = null;
	public static String password = null;
	public static String testVault = null;
	public static String testVault2 = null;
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
			//testVault2 = "MetadataOperations";

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
	 * 55.2.1A : Verify 'mandatory' property for a class
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint55", "Metadatacard"}, 
			description = "Verify 'mandatory' property for a class.")
	public void SprintTest55_2_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault); //Logs into application
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Open the Select template dialog 
			//------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Document.Value);

			Log.message("2. Opened the Select template dialog");

			//3. Select the desired extension
			//--------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard = metadatacard.setTemplate(dataPool.get("Extension"));

			Log.message("3. Select the desired extension");

			//4. Select the class in the metadatacard
			//----------------------------------------
			metadatacard.setPropertyValue("Class", dataPool.get("Class"));

			Log.message("4. Select the class in the metadatacard.");

			//Verification: To verify if the mandatory property is added to the metadatacard
			//--------------------------------------------------------------------------------
			if(metadatacard.propertyExists(dataPool.get("Property")))
				Log.pass("Test Case Passed. The property was added to the property when the class was selected.");
			else
				Log.fail("Test Case Failed. The property was not added to the property when the class was selected.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 55.2.1B : Verify 'mandatory' property for a class
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint55", "Metadatacard"}, 
			description = "Verify 'mandatory' property for a class.")
	public void SprintTest55_2_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault); //Logs into application
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Open the Select template dialog 
			//------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Document.Value);

			Log.message("2. Opened the Select template dialog");

			//3. Select the desired extension
			//--------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard = metadatacard.setTemplate(dataPool.get("Extension"));

			Log.message("3. Select the desired extension");

			//4. Select the class in the metadatacard
			//----------------------------------------
			metadatacard.setPropertyValue("Class", dataPool.get("Class"));

			Log.message("4. Select the class in the metadatacard.");

			//Verification: To verify if the mandatory property is added to the metadatacard
			//--------------------------------------------------------------------------------
			if(metadatacard.propertyExists(dataPool.get("Property")))
				Log.pass("Test Case Passed. The property was added to the property when the class was selected.");
			else
				Log.fail("Test Case Failed. The property was not added to the property when the class was selected.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 55.2.2 : Verify user can able to create a new document in MFWA if you have a mandatory property in-built property 'Keywords' (ID 26) in a class 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint55", "Metadatacard"}, 
			description = "Verify user can able to create a new document in MFWA if you have a mandatory property in-built property 'Keywords' (ID 26) in a class .")
	public void SprintTest55_2_2(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault); //Logs into application
			Utils.fluentWait(driver);

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
			metadatacard.setCheckInImmediately(true);
			metadatacard.saveAndClose();

			Log.message("4. Set the necessary properties and click the create button.");

			//Verification: To verify if the Object is created
			//-------------------------------------------------
			String objectName = dataPool.get("Object")+"."+dataPool.get("Extension");
			homePage.searchPanel.search(objectName, "Search only: "+dataPool.get("ObjectType")+"s");

			if(homePage.listView.isItemExists(objectName))
				Log.pass("Test Case Passed. The Object was created as expected.");
			else
				Log.fail("Test Case Failed. The Object was not found in the vault.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 55.2.3 : Verify to Create new object which keyword is not mandatory 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint55", "Metadatacard"}, 
			description = "Verify to Create new object which keyword is not mandatory.")
	public void SprintTest55_2_3(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault); //Logs into application
			Utils.fluentWait(driver);

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
			metadatacard.setCheckInImmediately(true);
			metadatacard.saveAndClose();

			Log.message("4. Set the necessary properties and click the create button.");

			//Verification: To verify if the Object is created
			//-------------------------------------------------
			String objectName = dataPool.get("Object")+"."+dataPool.get("Extension");
			homePage.searchPanel.search(objectName, "Search only: "+dataPool.get("ObjectType")+"s");

			if(homePage.listView.isItemExists(objectName))
				Log.pass("Test Case Passed. The Object was created as expected.");
			else
				Log.fail("Test Case Failed. The Object was not found in the vault.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 55.2.6A : Modifying an object with the class that has mandatory property  
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint55", "Metadatacard"}, 
			description = "Modifying an object with the class that has mandatory property .")
	public void SprintTest55_2_6A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault); //Logs into application
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object with the class
			//----------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType"));
			Utils.fluentWait(driver);
			Log.message("2. Search for the object with the class");

			//3. Open it's metadatacard
			//--------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("Invalid Test data. The Object was not found in the vault.");

			Utils.fluentWait(driver);
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("3. Open it's metadatacard");

			//4. Make some changes to the metadata
			//-------------------------------------
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));

			Log.message("4. Make some changes to the metadata");

			//5. Click the Save Button
			//-------------------------
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("5. Clicked the Save Button");

			//Verification: To verify if the warning is displayed
			//----------------------------------------------------
			metadatacard = new MetadataCard(driver);

			if(metadatacard.getWarningMessage().equals("The field \""+dataPool.get("MandatoryProperty")+"\" must not be empty."))
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
	 * 55.2.6B : Modifying an object with the class that has mandatory property (SidePane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint55", "Metadatacard"}, 
			description = "Modifying an object with the class that has mandatory property (SidePane).")
	public void SprintTest55_2_6B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault); //Logs into application
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object with the class
			//----------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType"));

			Log.message("2. Search for the object with the class");

			//3. Open it's metadatacard
			//--------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("Invalid Test data. The Object was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("3. Open it's metadatacard");

			//4. Make some changes to the metadata
			//-------------------------------------
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));

			Log.message("4. Make some changes to the metadata");

			//5. Click the Save Button
			//-------------------------
			metadatacard.saveAndClose();

			Log.message("5. Clicked the Save Button");

			//Verification: To verify if the warning is displayed
			//----------------------------------------------------
			metadatacard = new MetadataCard(driver, true);

			if(metadatacard.getWarningMessage().equals("The field \""+dataPool.get("MandatoryProperty")+"\" must not be empty."))
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
	 * 55.2.6C : Modifying an object with the class that has mandatory property  
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint55", "Metadatacard"}, 
			description = "Modifying an object with the class that has mandatory property .")
	public void SprintTest55_2_6C(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault); //Logs into application
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object with the class
			//----------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType"));

			Log.message("2. Search for the object with the class");

			//3. Open it's metadatacard
			//--------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("Invalid Test data. The Object was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("3. Open it's metadatacard");

			//4. Make some changes to the metadata
			//-------------------------------------
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));

			Log.message("4. Make some changes to the metadata");

			//5. Click the Save Button
			//-------------------------
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("5. Clicked the Save Button");

			//Verification: To verify if the warning is displayed
			//----------------------------------------------------
			metadatacard = new MetadataCard(driver, true);

			if(metadatacard.getPropertyValue(dataPool.get("Property")).equals(dataPool.get("Value")))
				Log.pass("Test Case Passed. The Value was set to the mandatory property.");
			else
				Log.fail("Test Case Failed. The Value was not set to the mandatory property.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 55.2.6D : Modifying an object with the class that has mandatory property (SidePane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint55", "Metadatacard"}, 
			description = "Modifying an object with the class that has mandatory property (SidePane).")
	public void SprintTest55_2_6D(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault); //Logs into application
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object with the class
			//----------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType"));

			Log.message("2. Search for the object with the class");

			//3. Open it's metadatacard
			//--------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("Invalid Test data. The Object was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("3. Open it's metadatacard");

			//4. Make some changes to the metadata
			//-------------------------------------
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));

			Log.message("4. Make some changes to the metadata");

			//5. Click the Save Button
			//-------------------------
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("5. Clicked the Save Button");

			//Verification: To verify if the warning is displayed
			//----------------------------------------------------
			metadatacard = new MetadataCard(driver, true);

			if(metadatacard.getPropertyValue(dataPool.get("Property")).equals(dataPool.get("Value")))
				Log.pass("Test Case Passed. The Value was set to the mandatory property.");
			else
				Log.fail("Test Case Failed. The Value was not set to the mandatory property.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 55.2.7A : Changing the class of an existing object  
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint55", "Metadatacard"}, 
			description = "Changing the class of an existing object.")
	public void SprintTest55_2_7A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault); //Logs into application
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object with the class
			//----------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType"));

			Log.message("2. Search for the object with the class");

			//3. Open it's metadatacard
			//--------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("Invalid Test data. The Object was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("3. Open it's metadatacard");

			//4. Make some changes to the metadata
			//-------------------------------------
			metadatacard.setPropertyValue("Class", dataPool.get("Class"));
			Utils.fluentWait(driver);

			Log.message("4. Make some changes to the metadata");

			//Verification: To verify if the Mandatory property is added to the Object
			//-------------------------------------------------------------------------
			if(metadatacard.propertyExists(dataPool.get("Property")))
				Log.pass("Test Case Passed. The Property was added to the object as expected.");
			else
				Log.fail("Test Case Failed. The Property was not added to the object.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 55.2.7B : Changing the class of an existing object (SidePane)  
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint55", "Metadatacard"}, 
			description = "Changing the class of an existing object (SidePane)")
	public void SprintTest55_2_7B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault); //Logs into application
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object with the class
			//----------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType"));

			Log.message("2. Search for the object with the class");

			//3. Open it's metadatacard
			//--------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("Invalid Test data. The Object was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("3. Open it's metadatacard");

			//4. Make some changes to the metadata
			//-------------------------------------
			metadatacard.setPropertyValue("Class", dataPool.get("Class"));
			Utils.fluentWait(driver);

			Log.message("4. Make some changes to the metadata");

			//Verification: To verify if the Mandatory property is added to the Object
			//-------------------------------------------------------------------------
			if(metadatacard.propertyExists(dataPool.get("Property")))
				Log.pass("Test Case Passed. The Property was added to the object as expected.");
			else
				Log.fail("Test Case Failed. The Property was not added to the object.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 55.2.8A : Changing the class of an object that removes the mandatory property  
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint55", "Metadatacard"}, 
			description = "Changing the class of an object and removing the mandatory property.")
	public void SprintTest55_2_8A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault); //Logs into application
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object with the class
			//----------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType"));

			Log.message("2. Search for the object with the class");

			//3. Open it's metadatacard
			//--------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("Invalid Test data. The Object was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("3. Open it's metadatacard");

			//4. Change the class of the object
			//----------------------------------
			metadatacard.setPropertyValue("Class", dataPool.get("Class"));
			//	metadatacard.clickPropertySave("Class");
			Utils.fluentWait(driver);

			Log.message("4. Change the class of the object.");

			//Verification: To verify if the Mandatory property is removed from the Object
			//-----------------------------------------------------------------------------
			if(!metadatacard.propertyExists(dataPool.get("Property")))
				Log.pass("Test Case Passed. The Property was removed as expected.");
			else
				Log.fail("Test Case Failed. The property was not removed from the object.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 55.2.8B : Changing the class of an object that removes the mandatory property (SidePane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint55", "Metadatacard"}, 
			description = "Changing the class of an object and removing the mandatory property (SidePane)")
	public void SprintTest55_2_8B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault); //Logs into application
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object with the class
			//----------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, "Search only: " + dataPool.get("ObjectType"), dataPool.get("Object"));

			Log.message("2. Navigate to the " + viewToNavigate +" search view.");

			//3. Open it's metadatacard
			//--------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("Invalid Test data. The Object was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("3. Open it's metadatacard");

			//4. Change the class of the object
			//----------------------------------
			metadatacard.setPropertyValue("Class", dataPool.get("Class"));
			//	metadatacard.clickPropertySave("Class");
			Utils.fluentWait(driver);

			Log.message("4. Change the class of the object.");

			//Verification: To verify if the Mandatory property is removed from the Object
			//-----------------------------------------------------------------------------
			if(!metadatacard.propertyExists(dataPool.get("Property")))
				Log.pass("Test Case Passed. The Property was removed as expected.");
			else
				Log.fail("Test Case Failed. The property was not removed from the object.", driver);

		}
		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 55.2.9A : Changing the class of an object and removing the mandatory property  
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint55", "Metadatacard"}, 
			description = "Changing the class of an object and removing the mandatory property.")
	public void SprintTest55_2_9A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault); //Logs into application
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object with the class
			//----------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType"));

			Log.message("2. Search for the object with the class");

			//3. Open it's metadatacard
			//--------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("Invalid Test data. The Object was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("3. Open it's metadatacard");

			//4. Change the class of the object
			//----------------------------------
			metadatacard.setPropertyValue("Class", dataPool.get("Class"));
			Utils.fluentWait(driver);

			Log.message("4. Change the class of the object.");

			//5. Remove the Property
			//----------------------
			metadatacard.removeProperty(dataPool.get("Property"));

			Log.message("5. Remove the Property");

			//Verification: To verify if the Mandatory property is removed from the Object
			//-----------------------------------------------------------------------------
			if(!metadatacard.propertyExists(dataPool.get("Property")))
				Log.pass("Test Case Passed. The Property was removed as expected.");
			else
				Log.fail("Test Case Failed. The property was not removed from the object.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 55.2.9B : Changing the class of an object and removing the mandatory property (SidePane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint55", "Metadatacard"}, 
			description = "Changing the class of an object and removing the mandatory property (SidePane)")
	public void SprintTest55_2_9B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault); //Logs into application
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object with the class
			//----------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType"));

			Log.message("2. Search for the object with the class");

			//3. Open it's metadatacard
			//--------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("Invalid Test data. The Object was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("3. Open it's metadatacard");

			//4. Change the class of the object
			//----------------------------------
			metadatacard.setPropertyValue("Class", dataPool.get("Class"));
			Utils.fluentWait(driver);

			Log.message("4. Change the class of the object.");

			//5. Remove the Property
			//----------------------
			metadatacard.removeProperty(dataPool.get("Property"));
			Utils.fluentWait(driver);

			Log.message("5. Remove the Property");

			//Verification: To verify if the Mandatory property is removed from the Object
			//-----------------------------------------------------------------------------
			if(!metadatacard.propertyExists(dataPool.get("Property")))
				Log.pass("Test Case Passed. The Property was removed as expected.");
			else
				Log.fail("Test Case Failed. The property was not removed from the object.", driver);

		}
		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 55.2.10A : Editing the value of the Mandatory Property  
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint55", "Metadatacard"}, 
			description = "Editing the value of the Mandatory Property.")
	public void SprintTest55_2_10A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault); //Logs into application
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object with the class
			//----------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType"));

			Log.message("2. Search for the object with the class");

			//3. Open it's metadatacard
			//--------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("Invalid Test data. The Object was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("3. Open it's metadatacard");

			//4. Set a value to the property and save the object
			//----------------------------------------------------
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value1"));
			metadatacard.saveAndClose();

			Log.message("4. Set a value to the property and save the object.");

			//5. Re-open the metadatacard
			//----------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value);
			metadatacard = new MetadataCard(driver);

			Log.message("5. Re-open the metadatacard");

			//6. Change the value of the property
			//------------------------------------
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value2"));
			metadatacard.saveAndClose();

			Log.message("6. Change the value of the property.");

			//Verification: To verify if the Value change is saved
			//-----------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value);

			metadatacard = new MetadataCard(driver);
			if(metadatacard.getPropertyValue(dataPool.get("Property")).equals(dataPool.get("Value2")))
				Log.pass("Test Case Passed. The Property value was changed as expected.");
			else
				Log.fail("Test Case Failed. The Property value wan not changed.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 55.2.10B : Editing the value of the Mandatory Property (SidePane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint55", "Metadatacard"}, 
			description = "Editing the value of the Mandatory Property (SidePane).")
	public void SprintTest55_2_10B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault); //Logs into application
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object with the class
			//----------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType"));

			Log.message("2. Search for the object with the class");

			//3. Open it's metadatacard
			//--------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("Invalid Test data. The Object was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("3. Open it's metadatacard");

			//4. Set a value to the property and save the object
			//----------------------------------------------------
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value1"));
			metadatacard.saveAndClose();

			Log.message("4. Set a value to the property and save the object.");

			//5. Re-open the metadatacard
			//----------------------------
			metadatacard = new MetadataCard(driver, true);

			Log.message("5. Re-open the metadatacard");

			//6. Change the value of the property
			//------------------------------------
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value2"));
			metadatacard.saveAndClose();

			Log.message("6. Change the value of the property.");

			//Verification: To verify if the Value change is saved
			//-----------------------------------------------------
			metadatacard = new MetadataCard(driver, true);
			if(metadatacard.getPropertyValue(dataPool.get("Property")).equals(dataPool.get("Value2")))
				Log.pass("Test Case Passed. The Property value was changed as expected.");
			else
				Log.fail("Test Case Failed. The Property value wan not changed.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 55.2.11A : Removing Value of Mandatory field when object is checked Out
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint55", "Metadatacard"}, 
			description = "Removing Value of Mandatory field when object is checked Out.")
	public void SprintTest55_2_11A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault); //Logs into application
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object with the class
			//----------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType"));

			Log.message("2. Search for the object with the class");

			//3. Check Out the object
			//--------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("Invalid Test data. The Object was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			MetadataCard metadatacard = new MetadataCard(driver, true);
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);

			Log.message("3. Check Out the object");

			//4. Empty the value of the property
			//-----------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			Utils.fluentWait(driver);
			metadatacard = new MetadataCard(driver);
			metadatacard.setPropertyValue(dataPool.get("Property"), "");
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);
			metadatacard = new MetadataCard(driver);

			Log.message("4. Empty the value of the property.");

			//Verification: To verify if the Warning dialog appears
			//------------------------------------------------------
			if(metadatacard.getWarningMessage().equals(dataPool.get("WarningMessage")))
				Log.pass("Test Case Passed. The Expected warning was diaplayed.");
			else
				Log.fail("Test Case Failed. The Expected warning was not dispalyed.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 55.2.11B : Removing Value of Mandatory field when object is checked Out (SidePane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint55", "Metadatacard"}, 
			description = "Removing Value of Mandatory field when object is checked Out (SidePane).")
	public void SprintTest55_2_11B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault); //Logs into application
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object with the class
			//----------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType"));

			Log.message("2. Search for the object with the class");

			//3. Check Out the object
			//--------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("Invalid Test data. The Object was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			MetadataCard metadatacard = new MetadataCard(driver, true);
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);

			Log.message("3. Open it's metadatacard");

			//4. Empty the value of the property
			//-----------------------------------
			metadatacard = new MetadataCard(driver, true);
			metadatacard.setPropertyValue(dataPool.get("Property"), "");
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);
			metadatacard = new MetadataCard(driver, true);

			Log.message("4. Empty the value of the property.");

			//Verification: To verify if the Warning dialog appears
			//------------------------------------------------------
			if(metadatacard.getWarningMessage().equals(dataPool.get("WarningMessage")))
				Log.pass("Test Case Passed. The Expected warning was diaplayed.");
			else
				Log.fail("Test Case Failed. The Expected warning was not dispalyed.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 55.2.12A : Value for mandatory property Set in Pop Up Metadatacard, Verify in Side pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint55", "Metadatacard"}, 
			description = "Value for mandatory property Set in Pop Up Metadatacard, Verify in Side pane")
	public void SprintTest55_2_12A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault); //Logs into application
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object with the class
			//----------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			Log.message("2. Search for the object with the class");

			//3. Set the value for the property in the metadatacard
			//------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("Invalid Test data. The Object was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("3. Set the value for the property in the metadatacard");

			//Verification: To verify if the Value is updated in the Side Pane
			//-----------------------------------------------------------------
			Utils.fluentWait(driver);
			metadatacard = new MetadataCard(driver, true);
			if(metadatacard.getPropertyValue(dataPool.get("Property")).equals(dataPool.get("Value")))
				Log.pass("Test Case Passed. The Value was updated in the side pane.");
			else
				Log.fail("Test Case Failed. The value was not updated in the side pane.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 55.2.12B : Value for mandatory property Set in Side Pane, Verify in pop up metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint55", "Metadatacard"}, 
			description = "Value for mandatory property Set in Side Pane, Verify in pop up metadatacard")
	public void SprintTest55_2_12B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault); //Logs into application
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for the object with the class
			//----------------------------------------
			Utils.fluentWait(driver);
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			Log.message("2. Search for the object with the class");

			//3. Set the value for the property in the metadatacard
			//------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("Invalid Test data. The Object was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			MetadataCard metadatacard = new MetadataCard(driver, true);
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));
			metadatacard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("3. Set the value for the property in the metadatacard");

			//Verification: To verify if the Value is updated in the Side Pane
			//-----------------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);
			metadatacard = new MetadataCard(driver);
			if(metadatacard.getPropertyValue(dataPool.get("Property")).equals(dataPool.get("Value")))
				Log.pass("Test Case Passed. The Value was updated in the side pane.");
			else
				Log.fail("Test Case Failed. The value was not updated in the side pane.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * SprintTest115087 : Verify if real numbers are getting round off as expected.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint55", "Metadatacard","Bug","#126179"}, 
			description = "Verify if real numbers are getting round off as expected.")
	public void SprintTest115087(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1 : Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//Step-2 : Search for the object with the class
			//---------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Document.Value);

			Log.message("2. Opened the 'Document' object from the task pane.", driver);

			//Step-3 :  Select the desired template
			//-------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the Pop-out metadatacard
			metadatacard.setTemplateUsingClass(dataPool.get("Template"));

			Log.message("3. Selected the desired template " + dataPool.get("Template"), driver);

			//Step-4 : Set the property in the 'Document' metadatacard
			//--------------------------------------------------------
			metadatacard= new MetadataCard(driver);//Instantiate the Pop-out metadatacard
			metadatacard.setInfo(dataPool.get("Properties"));//Set the metadatacard entire properties
			metadatacard.setCheckInImmediately(true);//Set the check in immediately check box
			metadatacard.saveAndClose();//Save the metadatacard

			Log.message("4. Entered the required property values & saved the metadatacard." , driver);

			//Verification : Verify if real number property value is displayed as expected
			//----------------------------------------------------------------------------
			metadatacard =  new MetadataCard(driver,true);//Instantiate the right pane metadatacard
			if(metadatacard.getPropertyValue("Real Number").equals(dataPool.get("ExpectedValue")))
				Log.pass("Test Case Passede.Real number property is round off as expected." + dataPool.get("ExpectedValue"), driver);
			else
				Log.fail("Test Case Failed.Real number property is not getting round off as expected. Expected value : "  + dataPool.get("ExpectedValue") + " Actual value : " +metadatacard.getPropertyValue("Real Number"), driver);


		}//End try
		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//SprintTest115087

	/**
	 * SprintTest134174 : Verify if view column value for a number(real) property shows the correct property value
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint55", "Metadatacard"}, 
			description = " Verify if view column value for a number(real) property shows the correct property value.")
	public void SprintTest134174(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1 : Login to the Home View.
			//-------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//Step-2 : Set the search type in the search view
			//-----------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType"));

			Log.message("2. Navigated to the " + "Search only: "+dataPool.get("ObjectType")+ " view.");

			//Step-3 : Select the object from the list view
			//---------------------------------------------
			homePage.listView.clickItem(dataPool.get("Object"));

			Log.message("3. Selected the specified object " + dataPool.get("Object") + " from the search view.", driver);

			//Step-4 : Add the 'real number' property in object metadatacard
			//--------------------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver,true);//Instantiate the new property
			metadatacard.setPropertyValue(dataPool.get("PropName"),dataPool.get("PropValue"));//added the new property
			metadatacard.saveAndClose();//Save the metadatacard

			Log.message("4. Added the Property : 'Real Number' in opened metadatacard.", driver);

			//Verification  : Verify if Property value is displayed as expected in column
			//---------------------------------------------------------------------------
			if(homePage.listView.getColumnValueByItemName(dataPool.get("Object"), "Real Number").equals(dataPool.get("PropValue")))
				Log.pass("Test Case Passed.Column value for the 'Real Number' property is displayed as expected" +homePage.listView.getColumnValueByItemName(dataPool.get("Object"), "Real Number"), driver);
			else
				Log.fail("Test Case Failed.Column value is not displayed as expected for the real number property" +homePage.listView.getColumnValueByItemName(dataPool.get("Object"), "Real Number"), driver);


		}//End try
		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//SprintTest134174



}
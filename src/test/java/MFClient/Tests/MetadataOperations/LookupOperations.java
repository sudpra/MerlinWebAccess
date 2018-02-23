package MFClient.Tests.MetadataOperations;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.WebDriverUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.openqa.selenium.By;
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
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class LookupOperations {

	public String xlTestDataWorkBook = null;
	public String loginURL = null;
	public String userName = null;
	public String password = null;
	public String testVault = null;
	public String testVault2 = null;
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
			testVault2 = "MetadataOperations";

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
	 * 47.2.13A : Verify to Open the value list dropdown in the metadatacard (SidePane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "Sprint44", "Metadatacard"}, 
			description = "Verify to Open the value list dropdown in the metacard (SidePane).")
	public void SprintTest47_2_13A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");
			//2. Search for an object and open it's properties
			//-------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));
			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and open it's properties.");

			//3. Add the property and get the available values
			//-------------------------------------------------
			metadatacard.addNewProperty(dataPool.get("Property"));
			List<String> actualValues = metadatacard.getAvailablePropertyValues(dataPool.get("Property"));

			Log.message("3. Add the properties and set the values.");

			//Verification: To Verify if all the expected values are listed
			//-------------------------------------------------------------
			String[] expectedValue = dataPool.get("Values").split("\n");

			for(int count = 0; count < expectedValue.length; count++) {
				if(actualValues.indexOf(expectedValue[count]) == -1)
					Log.fail("Test Case Failed. An expected value was not listed.", driver);
			}

			Log.pass("Test Case Passed. All the expected values of the value list are listed.");

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 47.2.13B : Verify to Open the value list dropdown in the metadatacard (SidePane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "Sprint44", "Metadatacard"}, 
			description = "Verify to Open the value list dropdown in the metacard (SidePane).")
	public void SprintTest47_2_13B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");
			//2. Search for an object and open it's properties
			//-------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Search for an object and open it's properties.");
			//3. Add the property and get the available values
			//-------------------------------------------------
			metadatacard.addNewProperty(dataPool.get("Property"));
			ArrayList<String> actualValues = metadatacard.getAvailablePropertyValues(dataPool.get("Property"));

			Log.message("3. Add the properties and set the values.");
			//Verification: To Verify if all the expected values are listed
			//-------------------------------------------------------------
			String[] expectedValue = dataPool.get("Values").split("\n");

			for(int count = 0; count < expectedValue.length; count++) {
				if(actualValues.indexOf(expectedValue[count]) == -1)
					Log.fail("Test Case Failed. An expected value was not listed.", driver);
			}

			Log.pass("Test Case Passed. All the expected values of the value list are listed.");

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 47.2.14A : Verify to Open the value list dropdown in the metadatacard (SidePane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "Sprint44", "Metadatacard"}, 
			description = "Verify to Open the value list dropdown in the metacard (SidePane).")
	public void SprintTest47_2_14A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object and open it's properties
			//-------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));

			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and open it's properties.");

			//3. Add the property and Type in any value
			//------------------------------------------
			metadatacard.addNewProperty(dataPool.get("Property"));
			metadatacard.typeInPropertyValue(dataPool.get("Property"), dataPool.get("Value"));

			ArrayList<String> values = metadatacard.getPropValuesFromDropDown();
			int Expected = 1;
			Log.message("3. Add the property and Type in any value.");

			//Verification: To Verify if the typed in value is listed
			//--------------------------------------------------------
			for (int i = 0; i < values.size(); i++)
				if(values.get(i).trim().equals(dataPool.get("Value")))			
					Expected = 1;
				else
					Expected = 0;

			if (Expected == 1)
				Log.pass("Test Case Passed. The Typed in value was listed in the drop down.");
			else
				Log.fail("Test Case Failed. The typed in value was not listed in the drop down.", driver);
		}


		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 47.2.14B : Verify to Open the value list dropdown in the metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "Sprint44", "Metadatacard"}, 
			description = "Verify to Open the value list dropdown in the metacard.")
	public void SprintTest47_2_14B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object and open it's properties
			//-------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value);

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Search for an object and open it's properties.");
			//3. Add the property and Type in any value
			//------------------------------------------
			metadatacard.addNewProperty(dataPool.get("Property"));
			metadatacard.typeInPropertyValue(dataPool.get("Property"), dataPool.get("Value"));

			ArrayList<String> values = metadatacard.getPropValuesFromDropDown();
			int Expected = 1;
			Log.message("3. Add the property and Type in any value.");

			//Verification: To Verify if the typed in value is listed
			//--------------------------------------------------------
			for (int i = 0; i < values.size(); i++)
				if(values.get(i).trim().equals(dataPool.get("Value")))			
					Expected = 1;
				else
					Expected = 0;	

			if (Expected == 1)
				Log.pass("Test Case Passed. The Typed in value was listed in the drop down.");
			else
				Log.fail("Test Case Failed. The typed in value was not listed in the drop down.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 47.2.15A : Verify to Open the value list dropdown in the metadatacard (SidePane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "Sprint44", "Metadatacard"}, 
			description = "Verify to Open the value list dropdown in the metacard (SidePane).")
	public void SprintTest47_2_15A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");
			//2. Search for an object and open it's properties
			//-------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));
			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and open it's properties.");
			//3. Add the property and Type in any value
			//------------------------------------------
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));

			Log.message("3. Add the property and Type in any value.");
			//4. Click the Save button
			//-------------------------
			metadatacard.saveAndClose();

			Log.message("4. Click the Save button");
			//Verification: To Verify if the value is set to the object
			//---------------------------------------------------------
			metadatacard = new MetadataCard(driver, true);
			if(metadatacard.getPropertyValue(dataPool.get("Property")).equals(dataPool.get("Value")))	
				Log.pass("Test Case Passed. The Value was set to the object as expected.");
			else
				Log.fail("Test Case Failed. The typed in value was not listed in the drop down.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 47.2.15B : Verify to Open the value list dropdown in the metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "Sprint44", "Metadatacard"}, 
			description = "Verify to Open the value list dropdown in the metacard.")
	public void SprintTest47_2_15B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");
			//2. Search for an object and open it's properties
			//-------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value);

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Search for an object and open it's properties.");

			//3. Add the property and Type in any value
			//------------------------------------------
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));

			Log.message("3. Add the property and Type in any value.");
			//4. Click the Save button
			//-------------------------
			metadatacard.saveAndClose();
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			Log.message("4. Click the Save button");

			//Verification: To Verify if the value is set to the object
			//---------------------------------------------------------
			metadatacard = new MetadataCard(driver);
			if(metadatacard.getPropertyValue(dataPool.get("Property")).equals(dataPool.get("Value")))	
				Log.pass("Test Case Passed. The Value was set to the object as expected.");
			else
				Log.fail("Test Case Failed. The typed in value was not listed in the drop down.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 47.2.17A : Verify to Open the value list dropdown in the metadatacard (SidePane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "Sprint44", "Metadatacard"}, 
			description = "Verify to Open the value list dropdown in the metadatacard (SidePane).")
	public void SprintTest47_2_17A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");
			//2. Create an Assignment
			//------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);
			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard.setInfo(dataPool.get("AssignmentProperties")+dataPool.get("Assignment"));
			metadatacard.saveAndClose();

			Log.message("2. Create an Assignment.");
			//2. Search for the assignment and open it's properties
			//------------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Assignment"), "Search only: "+Caption.ObjecTypes.Assignment.Value+"s");
			if(!homePage.listView.isItemExists(dataPool.get("Assignment")))
				throw new SkipException("The specified object '" + dataPool.get("Assignment") + "' was not created.");
			homePage.listView.clickItem(dataPool.get("Assignment"));
			metadatacard = new MetadataCard(driver, true);

			Log.message("3. Search for the assignment and open it's properties.");
			//3. Add a document for the assignment
			//------------------------------------
			metadatacard.setPropertyValue(dataPool.get("ObjectType"), dataPool.get("Object"));
			metadatacard.saveAndClose();

			Log.message("4. Add a document for the assignment");
			//Verification: To Verify if the document is added to the Assignment
			//-------------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true);
			if(metadatacard.getPropertyValue(dataPool.get("ObjectType")).equals(dataPool.get("Object")))	
				Log.pass("Test Case Passed. The Value was set to the object as expected.");
			else
				Log.fail("Test Case Failed. The value was not set to the property.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 47.2.17B : Verify to Open the value list dropdown in the metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "Sprint44", "Metadatacard"}, 
			description = "Verify to Open the value list dropdown in the metadatacard.")
	public void SprintTest47_2_17B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");
			//2. Create an Assignment
			//------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);
			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard.setInfo(dataPool.get("AssignmentProperties")+dataPool.get("Assignment"));
			metadatacard.saveAndClose();

			Log.message("2. Create an Assignment.");
			//2. Search for the assignment and open it's properties
			//------------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Assignment"), "Search only: "+Caption.ObjecTypes.Assignment.Value+"s");

			if(!homePage.listView.isItemExists(dataPool.get("Assignment")))
				throw new SkipException("The specified object '" + dataPool.get("Assignment") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Assignment"));
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value);
			metadatacard = new MetadataCard(driver);

			Log.message("3. Search for the assignment and open it's properties.");
			//3. Add a document for the assignment
			//------------------------------------
			metadatacard.setPropertyValue(dataPool.get("ObjectType"), dataPool.get("Object"));
			metadatacard.saveAndClose();
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			Log.message("4. Add a document for the assignment");
			//Verification: To Verify if the document is added to the Assignment
			//-------------------------------------------------------------------
			metadatacard = new MetadataCard(driver);
			if(metadatacard.getPropertyValue(dataPool.get("ObjectType")).equals(dataPool.get("Object")))	
				Log.pass("Test Case Passed. The Value was set to the object as expected.");
			else
				Log.fail("Test Case Failed. The typed in value was not listed in the drop down.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 47.2.18 : Verify to Open the value list dropdown in the metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "Sprint44", "Metadatacard"}, 
			description = "Verify to Open the value list dropdown in the metacard.")
	public void SprintTest47_2_18(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");
			//2. Search for the assignment and open it's properties
			//------------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Assignment"), "Search only: "+Caption.ObjecTypes.Assignment.Value+"s");

			if(!homePage.listView.isItemExists(dataPool.get("Assignment")))
				throw new SkipException("The specified object '" + dataPool.get("Assignment") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Assignment"));
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value);

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Search for the assignment and open it's properties.");
			//3. Add a document for the assignment
			//------------------------------------
			metadatacard.removePropertyValue(dataPool.get("ObjectType"), 1);
			metadatacard.saveAndClose();

			Log.message("3. Add a document for the assignment");
			//Verification: To Verify if the document is added to the Assignment
			//-------------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true);
			if(metadatacard.getPropertyValue(dataPool.get("ObjectType")).equals(""))	
				Log.pass("Test Case Passed. The Value was removed from the object as expected.");
			else
				Log.fail("Test Case Failed. The Value of the property was not removed.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 47.2.20A : Verify to Open the value list dropdown in the metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "Sprint44", "Metadatacard"}, 
			description = "Verify to Open the value list dropdown in the metacard.")
	public void SprintTest47_2_20A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");
			//2. Search for the assignment and open it's properties
			//------------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Customer"), "Search only: "+Caption.ObjecTypes.Customer.Value+"s");

			if(!homePage.listView.isItemExists(dataPool.get("Customer")))
				throw new SkipException("The specified object '" + dataPool.get("Customer") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Customer"));
			MetadataCard originalObjMetadatacard = new MetadataCard(driver, true);

			Log.message("2. Search for the assignment and open it's properties.");
			//3. Add a document for the assignment
			//------------------------------------
			originalObjMetadatacard.createNewPropertyValue(dataPool.get("ObjectType"), 1);

			MetadataCard newObjTemplateMetadatacard = new MetadataCard(driver);
			MetadataCard newObjMetadatacard = newObjTemplateMetadatacard.setTemplate(dataPool.get("Extension"));

			newObjMetadatacard.setInfo(dataPool.get("Properties")+dataPool.get("DocName"));
			newObjMetadatacard.saveAndClose();

			originalObjMetadatacard.saveAndClose();

			Log.message("3. Add a document for the assignment");
			//Verification: To Verify if the document is added to the Assignment
			//-------------------------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver, true);
			if(metadatacard.getPropertyValue(dataPool.get("ObjectType")).equals(dataPool.get("DocName")))	
				Log.pass("Test Case Passed. The new document was created and added to the assignment.");
			else
				Log.fail("Test Case Failed. The New document was not added to the assignment.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 47.2.20B : Verify to Open the value list dropdown in the metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "Sprint44", "Metadatacard"}, 
			description = "Verify to Open the value list dropdown in the metacard.")
	public void SprintTest47_2_20B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for the assignment and open it's properties
			//------------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Customer"), "Search only: "+Caption.ObjecTypes.Customer.Value+"s");

			if(!homePage.listView.isItemExists(dataPool.get("Customer")))
				throw new SkipException("The specified object '" + dataPool.get("Customer") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Customer"));
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			MetadataCard originalObjMetadatacard = new MetadataCard(driver);

			Log.message("2. Search for the assignment and open it's properties.");

			//3. Add a document for the assignment
			//------------------------------------
			originalObjMetadatacard.createNewPropertyValue(dataPool.get("ObjectType"), 1);

			MetadataCard newObjTemplateMetadatacard = new MetadataCard(driver, "New "+dataPool.get("ObjectType"));
			newObjTemplateMetadatacard.setTemplate(dataPool.get("Extension"));

			MetadataCard newObjMetadatacard = new MetadataCard(driver, "New "+dataPool.get("ObjectType"));
			newObjMetadatacard.setInfo(dataPool.get("Properties")+dataPool.get("DocName"));
			newObjMetadatacard.saveAndClose();

			originalObjMetadatacard.saveAndClose();

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			Log.message("3. Add a document for the assignment");

			//Verification: To Verify if the document is added to the Assignment
			//-------------------------------------------------------------------

			MetadataCard metadatacard = new MetadataCard(driver);
			if(metadatacard.getPropertyValue(dataPool.get("ObjectType")).equals(dataPool.get("DocName")))	
				Log.pass("Test Case Passed. The new document was created and added to the assignment.");
			else
				Log.fail("Test Case Failed. The New document was not added to the assignment.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.1A : Verify to add 'Customer' and 'Contact person' property to selected assignment object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "Sprint44", "Metadatacard"}, 
			description = "Verify to add 'Customer' and 'Contact person' property to selected assignment object.")
	public void SprintTest48_1_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");
			//2. Search for an object and open it's metadatacard
			//---------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));

			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and open it's metadatacard.");

			//3. Add the required properties
			//-------------------------------
			metadatacard.addNewProperty(dataPool.get("Property"));
			metadatacard.addNewProperty(dataPool.get("OwnerProperty"));

			Log.message("3. Add the required properties.");

			//4. Set the value for the child property
			//---------------------------------------
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));
			metadatacard.saveAndClose();

			Log.message("4. Set the value for the child property.");

			//Verify if the metadatacard is in edit mode
			//-------------------------------------------
			metadatacard = new MetadataCard(driver, true);
			if(metadatacard.getPropertyValue(dataPool.get("OwnerProperty")).equals(dataPool.get("ExpectedValue")))
				Log.pass("Test Case Passed. The value was automatically set to the object.");
			else
				Log.fail("Test Case Failed. The value was not set to the object, as expected.", driver);	

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.1B : Verify the values listed for the added property
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "Sprint44", "Metadatacard"}, 
			description = "Verify the values listed for the added property.")
	public void SprintTest48_1_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object and open it's metadatacard
			//---------------------------------------------------
			homePage.searchPanel.search("", "Search only: "+dataPool.get("Property")+"s");

			List<String> expectedValues = homePage.listView.getColumnValues("Name");
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));

			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and open it's metadatacard.");

			//3. Add the required properties
			//-------------------------------
			metadatacard.addNewProperty(dataPool.get("Property"));

			Log.message("3. Add the required properties.");

			//Verify if all the objects in the vault are listes in the metadatacard
			//-----------------------------------------------------------------------

			List<String> actualValues = metadatacard.getAvailablePropertyValues(dataPool.get("Property"));

			if(actualValues.size() != expectedValues.size())
				throw new Exception("The Number of values listed did not match the number of objects.");

			for(int count = 0; count < expectedValues.size(); count++) {
				if(actualValues.indexOf(expectedValues.get(count)) == -1)
					Log.fail("Test Case Failed. The Value '" + expectedValues.get(count) + "' was not found.", driver);
			}

			Log.pass("Test Case Passed. All the values were listed as values for the property.");
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.1C : Verify to add 'Customer' and 'Contact person' property to New Object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "Sprint44", "Metadatacard"}, 
			description = "Verify to add 'Customer' and 'Contact person' property to New Object.")
	public void SprintTest48_1_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Open the new object metadatacard
			//------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));
			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard.setInfo(dataPool.get("Properties"));

			Log.message("2. Open the new object metadatacard");

			//3. Add the required properties and set values
			//-----------------------------------------------
			metadatacard.addNewProperty(dataPool.get("Property"));
			metadatacard.addNewProperty(dataPool.get("OwnerProperty"));
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));

			metadatacard.saveAndClose();

			Log.message("3. Add the required properties and set values.");

			//4. Search for the object and open it's metadatacard
			//----------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));

			metadatacard = new MetadataCard(driver, true);

			Log.message("4. Search for the object and open it's metadatacard.");

			//Verify if the value is set to the object
			//-------------------------------------------
			if(metadatacard.getPropertyValue(dataPool.get("OwnerProperty")).equals(dataPool.get("ExpectedValue")))
				Log.pass("Test Case Passed. The value was automatically set to the object.");
			else
				Log.fail("Test Case Failed. The value was not set to the object, as expected.", driver);	

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.2A : Filtering of the values listed for the added property
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "Sprint44", "Metadatacard"}, 
			description = "Filtering of the values listed for the added property.")
	public void SprintTest48_1_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object and open it's metadatacard
			//---------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("OwnerMetadataProperty"), "is", dataPool.get("Value"));
			homePage.searchPanel.search("", "Search only: "+dataPool.get("Property")+"s");
			homePage.searchPanel.resetAll();

			List<String> expectedValues = homePage.listView.getColumnValues("Name");
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));
			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and open it's metadatacard.");

			//3. Add the required properties
			//-------------------------------
			metadatacard.addNewProperty(dataPool.get("Property"));

			Log.message("3. Add the required properties.");

			//4. Set the value for the Owner Property
			//----------------------------------------
			metadatacard.setPropertyValue(dataPool.get("OwnerProperty"), dataPool.get("Value"));
			metadatacard.saveAndClose();

			Log.message("4. Set the value for the Owner Property");

			//Verify if all the objects in the vault are listes in the metadatacard
			//-----------------------------------------------------------------------

			metadatacard = new MetadataCard(driver, true);
			List<String> actualValues = metadatacard.getAvailablePropertyValues(dataPool.get("Property"));

			if(actualValues.size() != expectedValues.size())
				throw new Exception("The Number of values listed did not match the number of objects.");

			for(int count = 0; count < expectedValues.size(); count++) {
				if(actualValues.indexOf(expectedValues.get(count)) == -1)
					Log.fail("Test Case Failed. The Value '" + expectedValues.get(count) + "' was not found.", driver);
			}

			Log.pass("Test Case Passed. All the values were listed as values for the property.");
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.2B : Verify to add 'Customer' and 'Contact person' property to selected document collection object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "Sprint44", "Metadatacard"}, 
			description = "Verify to add 'Customer' and 'Contact person' property to selected document collection object.")
	public void SprintTest48_1_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object and open it's metadatacard
			//---------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");
			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));

			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and open it's metadatacard.");

			//3. Set the value for the Properties
			//----------------------------------------
			metadatacard.setPropertyValue(dataPool.get("OwnerProperty"), dataPool.get("OwnerValue"));
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));
			metadatacard.saveAndClose();

			Log.message("3. Set the value for the Properties");

			//Verify if the values are related to the object
			//-----------------------------------------------
			homePage.listView.expandRelations(dataPool.get("Object"));

			if(!homePage.listView.isItemExists(dataPool.get("OwnerProperty")+"s (1)") || !homePage.listView.isItemExists(dataPool.get("Property")+"s (1)"))
				throw new Exception("The values were not related to the object.");

			homePage.listView.expandRelations(dataPool.get("OwnerProperty")+"s (1)");
			homePage.listView.expandRelations(dataPool.get("Property")+"s (1)");

			if(homePage.listView.isItemExists(dataPool.get("OwnerValue")) && homePage.listView.isItemExists(dataPool.get("Value")))
				Log.pass("Test Case Passed. The values were related to the object as expected.");
			else
				Log.fail("Test Case Failed. The values were not related to the object", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.5A : Verify to assign a value from filtered list (SidePane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "Sprint44", "Metadatacard"}, 
			description = "Verify to assign a value from filtered list (SidePane).")
	public void SprintTest48_1_5A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object and open it's metadatacard
			//---------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));

			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and open it's metadatacard.");

			//3. Set the value for the property
			//----------------------------------
			metadatacard.typeInPropertyValue(dataPool.get("Property"), dataPool.get("Value").substring(0,  dataPool.get("Value").length()-(dataPool.get("Value").length()/2)));

			metadatacard.selectPropValueFromDropDown(dataPool.get("Value"));

			metadatacard.saveAndClose();

			Log.message("3. Set the value for the property.");

			//Verification: To verify if the valueios set to the property
			//------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true);

			if(metadatacard.getPropertyValue(dataPool.get("Property")).equals(dataPool.get("Value")))
				Log.pass("Test Case Passed. The Value was set to the object as expected.");
			else
				Log.fail("Test Case Failed. The Value was not set to the object.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.5B : Verify to assign a value from filtered list
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "Sprint44", "Metadatacard"}, 
			description = "Verify to assign a value from filtered list.")
	public void SprintTest48_1_5B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object and open it's metadatacard
			//---------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Search for an object and open it's metadatacard.");

			//3. Set the value for the property
			//----------------------------------
			metadatacard.typeInPropertyValue(dataPool.get("Property"), dataPool.get("Value").substring(0,  dataPool.get("Value").length()-(dataPool.get("Value").length()/2)));



			metadatacard.selectPropValueFromDropDown(dataPool.get("Value"));

			metadatacard.saveAndClose();

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			Log.message("3. Set the value for the property.");

			//Verification: To verify if the valueios set to the property
			//------------------------------------------------------------

			metadatacard = new MetadataCard(driver);

			if(metadatacard.getPropertyValue(dataPool.get("Property")).equals(dataPool.get("Value")))
				Log.pass("Test Case Passed. The Value was set to the object as expected.");
			else
				Log.fail("Test Case Failed. The Value was not set to the object.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.7A : Verify the values are auto filled on selecting sub values
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "Sprint44", "Metadatacard"}, 
			description = "Verify the values are auto filled on selecting sub values.")
	public void SprintTest48_1_7A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for an object and open it's metadatacard
			//---------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");


			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);


			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Search for an object and open it's metadatacard.");

			//3. Set the value for the property
			//----------------------------------
			metadatacard.setPropertyValue(dataPool.get("OwnerProperty"), "");
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));


			metadatacard.savePropValue(dataPool.get("Property"));

			Log.message("3. Set the value for the property.");

			//Verification: To verify if the valueios set to the property
			//------------------------------------------------------------
			if(metadatacard.getPropertyValue(dataPool.get("OwnerProperty")).equals(dataPool.get("OwnerValue")))
				Log.pass("Test Case Passed. The Value was set to the object as expected.");
			else
				Log.fail("Test Case Failed. The Value was not set to the object.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.7B : Verify the values are auto filled on selecting sub values (SidePane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "Sprint44", "Metadatacard"}, 
			description = "Verify the values are auto filled on selecting sub values (SidePane).")
	public void SprintTest48_1_7B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for an object and open it's metadatacard
			//---------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");



			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));



			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and open it's metadatacard.");

			//3. Set the value for the property
			//----------------------------------
			metadatacard.setPropertyValue(dataPool.get("OwnerProperty"), "");


			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));


			metadatacard.savePropValue(dataPool.get("Property"));


			Log.message("3. Set the value for the property.");

			//Verification: To verify if the valueios set to the property
			//------------------------------------------------------------
			if(metadatacard.getPropertyValue(dataPool.get("OwnerProperty")).equals(dataPool.get("OwnerValue")))
				Log.pass("Test Case Passed. The Value was set to the object as expected.");
			else
				Log.fail("Test Case Failed. The Value was not set to the object.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.10A : Verify the value filled when autofill action is canceled
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "Sprint44", "Metadatacard"}, 
			description = "Verify the value filled when autofill action is canceled.")
	public void SprintTest48_1_10A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object and open it's metadatacard
			//---------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));

			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and open it's metadatacard.");

			//3. Set the value for the property
			//----------------------------------
			metadatacard.addNewProperty(dataPool.get("OwnerProperty"));

			metadatacard.addNewProperty(dataPool.get("Property1"));

			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));

			Log.message("3. Set the value for the property.");

			//4. Click the No button in the confirmation dialog
			//--------------------------------------------------
			if (!MFilesDialog.exists(driver, "Confirm Autofill"))
				throw new Exception("The auto-fill dialog did not appear.");

			MFilesDialog mFilesDialog = new MFilesDialog(driver, "Confirm Autofill");
			mFilesDialog.clickCancelButton();

			metadatacard = new MetadataCard(driver, true);
			metadatacard.saveAndClose();

			Log.message("4. Click the No button in the confirmation dialog");

			//Verification: To verify if the value is set to the property
			//------------------------------------------------------------

			metadatacard = new MetadataCard(driver, true);
			if(metadatacard.getPropertyValue(dataPool.get("Property1")).equals(""))
				Log.pass("Test Case Passed. The Value was set to the object as expected.");
			else
				Log.fail("Test Case Failed. The Value was not set to the object.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.10B : Verify the value filled when autofill action is accepted
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "Sprint44", "Metadatacard"}, 
			description = "Verify the value filled when autofill action is accepted.")
	public void SprintTest48_1_10B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for an object and open it's metadatacard
			//---------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));

			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and open it's metadatacard.");

			//3. Set the value for the property
			//----------------------------------
			metadatacard.addNewProperty(dataPool.get("OwnerProperty"));

			metadatacard.addNewProperty(dataPool.get("Property1"));

			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));

			driver.switchTo().defaultContent();


			Log.message("3. Set the value for the property.");

			//4. Click the No button in the confirmation dialog
			//--------------------------------------------------
			if (!MFilesDialog.exists(driver, "Confirm Autofill"))
				throw new Exception("The auto-fill dialog did not appear.");

			MFilesDialog mFilesDialog = new MFilesDialog(driver, "Confirm Autofill");
			mFilesDialog.clickOkButton();


			metadatacard = new MetadataCard(driver, true);
			metadatacard.saveAndClose();


			Log.message("4. Click the No button in the confirmation dialog");

			//Verification: To verify if the value is set to the property
			//------------------------------------------------------------

			metadatacard = new MetadataCard(driver, true);
			if(metadatacard.getPropertyValue(dataPool.get("Property1")).equals(dataPool.get("Value1")))
				Log.pass("Test Case Passed. The Value was set to the object as expected.");
			else
				Log.fail("Test Case Failed. The Value was not set to the object.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.11A : Verify the values displayed when entering 'Space' character for added property (SidePane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "Sprint44", "Metadatacard"}, 
			description = "Verify the values displayed when entering 'Space' character for added property (SidePane).")
	public void SprintTest48_1_11A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for an object and open it's metadatacard
			//---------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));



			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and open it's metadatacard.");

			//3. Set the value for the property
			//----------------------------------
			metadatacard.addNewProperty(dataPool.get("Property"));


			metadatacard.typeInPropertyValue(dataPool.get("Property"), dataPool.get("Value").split(" ")[0]+" ");

			ArrayList<String> listedValues = metadatacard.getPropValuesFromDropDown();

			if(listedValues.indexOf(dataPool.get("Value")) == -1)
				throw new Exception("The Expected value was not listed.");

			Log.message("3. Set the value for the property.");

			//Verification: To verify if the value is set to the property
			//------------------------------------------------------------
			for(int count = 0; count < listedValues.size(); count++) {
				if(!listedValues.get(count).contains(dataPool.get("Value").split(" ")[0]+" "))
					Log.fail("Test Case Failed. The Values that did not match the filter were also listed.", driver);
			}

			Log.pass("Test Case Passed. Only the values that match the filter were listed.");

			//Close the metadata card without saving changes
			metadatacard.selectPropValueFromDropDown(dataPool.get("Value"));
			metadatacard.cancelAndConfirm();
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.11B : Verify the values displayed when entering 'Space' character for added property
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1", "Sprint44", "Metadatacard"}, 
			description = "Verify the values displayed when entering 'Space' character for added property.")
	public void SprintTest48_1_11B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for an object and open it's metadatacard
			//---------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");



			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);


			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Search for an object and open it's metadatacard.");

			//3. Set the value for the property
			//----------------------------------
			metadatacard.addNewProperty(dataPool.get("Property"));


			metadatacard.typeInPropertyValue(dataPool.get("Property"), dataPool.get("Value").split(" ")[0]+" ");

			ArrayList<String> listedValues = metadatacard.getPropValuesFromDropDown();

			if(listedValues.indexOf(dataPool.get("Value")) == -1)
				throw new Exception("The Expected value was not listed.");

			Log.message("3. Set the value for the property.");

			//Verification: To verify if the value is set to the property
			//------------------------------------------------------------
			for(int count = 0; count < listedValues.size(); count++) {
				if(!listedValues.get(count).contains(dataPool.get("Value").split(" ")[0]+" "))
					Log.fail("Test Case Failed. The Values that did not match the filter were also listed.", driver);
			}

			Log.pass("Test Case Passed. Only the values that match the filter were listed.");

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.12 : Verify the value filled when autofill action is accepted
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Verify the value filled when autofill action is accepted.")
	public void SprintTest48_1_12(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for an object and open it's metadatacard
			//---------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");



			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));



			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and open it's metadatacard.");

			//3. Set the value for the property
			//----------------------------------
			metadatacard.addNewProperty(dataPool.get("OwnerProperty"));


			metadatacard.addNewProperty(dataPool.get("Property1"));


			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));



			driver.switchTo().defaultContent();


			Log.message("3. Set the value for the property.");

			//4. Verify the autofill confirmation dialog
			//-------------------------------------------

			if(!MFilesDialog.exists(driver))
				throw new Exception("The Autofill confirmation dialog did not appear.");

			MFilesDialog mFilesDialog = new MFilesDialog(driver, "Confirm Autofill");

			Log.message("4. Verify the autofill confirmation dialog.");

			//Verification: To verify if the expected message is displayed in the confirmation dialog
			//----------------------------------------------------------------------------------------
			if(mFilesDialog.getMessage().equals(dataPool.get("ExpectedMessage")))
				Log.pass("Test Case Passed. The Expected Message was displayed in the auto-fill confirmation dialog.");
			else
				Log.fail("Test Case Failed. The Expected Message was not displayed in the auto-fill confirmation dialog.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.13 : Verify the autofill message displayed correctly
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Verify the autofill message displayed correctly.")
	public void SprintTest48_1_13(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Create a new Assignment
			//---------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);



			MetadataCard metadatacard = new MetadataCard(driver);

			metadatacard.setInfo(dataPool.get("AssignmentProperties"));


			metadatacard.saveAndClose();


			Log.message("2. Created a new Assignment.");

			//3. Set the necessary properties to a document
			//----------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");



			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));



			metadatacard = new MetadataCard(driver, true);

			Log.message("3. Set the necessary properties to a document.");

			//3. Set the value for the property
			//----------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Assignment.Value, dataPool.get("AssignmentProperties").split("\n")[0].split("::")[1]);


			metadatacard.setPropertyValue(dataPool.get("OwnerProperty"), dataPool.get("OwnerValue"));


			metadatacard.setPropertyValue(dataPool.get("Property1"), dataPool.get("Value1"));


			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));


			metadatacard.saveAndClose();


			Log.message("4. Set the value for the property.");

			//4. Open the new Object dialog and add the required Properties
			//--------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Report.Value);



			metadatacard = new MetadataCard(driver);

			metadatacard.setPropertyValue("Name or title", dataPool.get("Report"));


			metadatacard.saveAndClose();


			homePage.searchPanel.search(dataPool.get("Report"), "Search only: "+Caption.ObjecTypes.Report.Value+"s");



			if(!homePage.listView.isItemExists(dataPool.get("Report")))
				throw new SkipException("The specified object '" + dataPool.get("Report") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Report"));


			driver.findElement(By.cssSelector("li[id='metadataTab']")).click();



			metadatacard = new MetadataCard(driver, true);

			metadatacard.addNewProperty(Caption.ObjecTypes.Assignment.Value);

			metadatacard.addNewProperty(dataPool.get("OwnerProperty"));

			metadatacard.addNewProperty(dataPool.get("Property1"));

			metadatacard.addNewProperty(dataPool.get("Property"));


			metadatacard.setPropertyValue(dataPool.get("ObjectType"), dataPool.get("Object").split("\\.")[0]);

			driver.switchTo().defaultContent();

			Log.message("5. Open the new Object dialog and add the required Properties");

			//4. Verify the autofill confirmation dialog
			//-------------------------------------------

			if(!MFilesDialog.exists(driver))
				throw new Exception("The Autofill confirmation dialog did not appear.");

			MFilesDialog mFilesDialog = new MFilesDialog(driver, "Confirm Autofill");

			Log.message("6. Verify the autofill confirmation dialog.");

			//Verification: To verify if the expected message is displayed in the confirmation dialog
			//----------------------------------------------------------------------------------------
			if(mFilesDialog.getMessage().toUpperCase().equals(dataPool.get("ExpectedMessage").toUpperCase()))
				Log.pass("Test Case Passed. The Expected Message was displayed in the auto-fill confirmation dialog.");
			else
				Log.fail("Test Case Failed. The Expected Message was not displayed in the auto-fill confirmation dialog.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.14A : Verify the error text displayed upon entering invalid value to the 'Customer' property.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Verify the error text displayed upon entering invalid value to the 'Customer' property..")
	public void SprintTest48_1_14A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//3. Search for an object and open it's properties
			//-------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");



			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);



			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Set the necessary properties to a document.");

			//3. Type in the invalid value for the property
			//----------------------------------------------
			metadatacard.typeInPropertyValue(dataPool.get("Property"), dataPool.get("Value"));

			Log.message("3. Type in the invalid value for the property.");

			//Verification: To verify if the 'No Matches' is displayed
			//---------------------------------------------------------
			if(metadatacard.getPropertyValueListHeader().equals("No matches"))
				Log.pass("Test Case Passed. The Expected Message was displayed in the auto-fill confirmation dialog.");
			else
				Log.fail("Test Case Failed. The Expected Message was not displayed in the auto-fill confirmation dialog.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.14B : Verify the error text displayed upon entering invalid value to the 'Contact Person' property. (Contact Person of a different Customer)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Verify the error text displayed upon entering invalid value to the 'Contact Person' property. (Contact Person of a different Customer).")
	public void SprintTest48_1_14B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//3. Search for an object and open it's properties
			//-------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");



			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);



			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Set the necessary properties to a document.");

			//3. Type in the invalid value for the property
			//----------------------------------------------
			metadatacard.setPropertyValue(dataPool.get("OwnerProperty"), dataPool.get("OwnerValue"));


			metadatacard.typeInPropertyValue(dataPool.get("Property"), dataPool.get("Value"));


			Log.message("3. Type in the invalid value for the property.");

			//Verification: To verify if the 'No Matches' is displayed
			//---------------------------------------------------------
			if(metadatacard.getPropertyValueListHeader().equals(dataPool.get("OwnerProperty")+": "+dataPool.get("OwnerValue")+"\nNo matches"))
				Log.pass("Test Case Passed. The Expected Message was displayed in the auto-fill confirmation dialog.");
			else
				Log.fail("Test Case Failed. The Expected Message was not displayed in the auto-fill confirmation dialog.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.14C : Verify the error text displayed upon entering invalid value to the 'Customer' property. (New Object)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Verify the error text displayed upon entering invalid value to the 'Customer' property. (New Object).")
	public void SprintTest48_1_14C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Perfrom the New Object Type Menu Click
			//------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));



			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Perfrom the New Object Type Menu Click.");

			//3. Type in the invalid value for the property
			//----------------------------------------------
			metadatacard.typeInPropertyValue(dataPool.get("Property"), dataPool.get("Value"));


			Log.message("3. Type in the invalid value for the property.");

			//Verification: To verify if the 'No Matches' is displayed
			//---------------------------------------------------------
			if(metadatacard.getPropertyValueListHeader().equals("No matches"))
				Log.pass("Test Case Passed. The Expected Message was displayed in the auto-fill confirmation dialog.");
			else
				Log.fail("Test Case Failed. The Expected Message was not displayed in the auto-fill confirmation dialog.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.14D : Error upon entering invalid value to the 'Contact Person' property. (Contact Person of a different Customer - New object dialog)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Error upon entering invalid value to the 'Contact Person' property. (Contact Person of a different Customer - New object dialog).")
	public void SprintTest48_1_14D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Perfrom the New Object Type Menu Click
			//------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));



			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Perfrom the New Object Type Menu Click.");

			//3. Type in the invalid value for the property
			//----------------------------------------------
			metadatacard.setPropertyValue(dataPool.get("OwnerProperty"), dataPool.get("OwnerValue"));


			metadatacard.typeInPropertyValue(dataPool.get("Property"), dataPool.get("Value"));


			Log.message("3. Type in the invalid value for the property.");

			//Verification: To verify if the 'No Matches' is displayed
			//---------------------------------------------------------
			if(metadatacard.getPropertyValueListHeader().equals(dataPool.get("OwnerProperty")+": "+dataPool.get("OwnerValue")+"\nNo matches"))
				Log.pass("Test Case Passed. The Expected Message was displayed in the auto-fill confirmation dialog.");
			else
				Log.fail("Test Case Failed. The Expected Message was not displayed in the auto-fill confirmation dialog.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.16A : Verify the autofill message displayed for the selected value in project property (SidePane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Verify the autofill message displayed for the selected value in project property (SidePane).")
	public void SprintTest48_1_16A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object and open it's properties
			//-------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Value1"), "Search only: "+dataPool.get("Property1")+"s");

			if(!homePage.listView.clickItem(dataPool.get("Value1")))
				throw new SkipException("The specified object '" + dataPool.get("Value1") + "' was not found.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value2"));
			metadatacard.saveAndClose();

			driver.switchTo().defaultContent();
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));

			metadatacard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and open it's properties.");

			//3. Add the properties and set the values
			//-----------------------------------------
			metadatacard.addNewProperty(dataPool.get("OwnerProperty"));
			metadatacard.addNewProperty(dataPool.get("Property1"));
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));

			driver.switchTo().defaultContent();

			Log.message("3. Add the properties and set the values.");

			//4. Click ok button in the auto-fill confirmation dialog
			//--------------------------------------------------------
			if (!MFilesDialog.exists(driver, "Confirm Autofill"))
				throw new Exception("The auto-fill dialog did not appear.");

			MFilesDialog mFilesDialog = new MFilesDialog(driver, "Confirm Autofill");
			mFilesDialog.clickOkButton();

			Log.message("4. Click ok button in the auto-fill confirmation dialog");

			//5. Clear the owner property and enter a different value for property1
			//----------------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true);

			metadatacard.setPropertyValue(dataPool.get("OwnerProperty"), "");

			metadatacard.setPropertyValue(dataPool.get("Property1"), dataPool.get("Value1"));

			driver.switchTo().defaultContent();

			Log.message("5. Clear the owner property and enter a different value for property1");

			//Verification: To verify if the auto-fill confirmation dialog appears
			//---------------------------------------------------------------------
			if(MFilesDialog.exists(driver, "Confirm"))
				Log.pass("Test Case Passed. The auto-fill confirmation dialog appears as expected.");
			else
				Log.fail("Test Case Failed. The auto-fill confrimation dialog did not appear.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.16B : Verify the autofill message displayed for the selected value in project property
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Verify the autofill message displayed for the selected value in project property.")
	public void SprintTest48_1_16B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object and open it's properties
			//-------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Value1"), "Search only: "+dataPool.get("Property1")+"s");

			if(!homePage.listView.clickItem(dataPool.get("Value1")))
				throw new SkipException("The specified object '" + dataPool.get("Value1") + "' was not found.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			MetadataCard metadatacard = new MetadataCard(driver);
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value2"));
			metadatacard.saveAndClose();

			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			metadatacard = new MetadataCard(driver);

			Log.message("2. Search for an object and open it's properties.");

			//3. Add the properties and set the values
			//-----------------------------------------
			metadatacard.removeProperty(dataPool.get("OwnerProperty"));
			metadatacard.addNewProperty(dataPool.get("OwnerProperty"));

			metadatacard.removeProperty(dataPool.get("Property1"));
			metadatacard.addNewProperty(dataPool.get("Property1"));

			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));

			driver.switchTo().defaultContent();

			Log.message("3. Add the properties and set the values.");

			//4. Click ok button in the auto-fill confirmation dialog
			//--------------------------------------------------------
			if (!MFilesDialog.exists(driver, "Confirm Autofill"))
				throw new Exception("The auto-fill dialog did not appear.");

			MFilesDialog mFilesDialog = new MFilesDialog(driver, "Confirm");
			mFilesDialog.clickOkButton();

			Log.message("4. Click ok button in the auto-fill confirmation dialog");

			//5. Clear the owner property and enter a different value for property1
			//----------------------------------------------------------------------
			metadatacard = new MetadataCard(driver);

			metadatacard.setPropertyValue(dataPool.get("OwnerProperty"), "");

			metadatacard.setPropertyValue(dataPool.get("Property1"), dataPool.get("Value1"));
			driver.switchTo().defaultContent();

			Log.message("5. Clear the owner property and enter a different value for property1");

			//Verification: To verify if the auto-fill confirmation dialog appears
			//---------------------------------------------------------------------
			if(MFilesDialog.exists(driver, "Confirm"))
				Log.pass("Test Case Passed. The auto-fill confirmation dialog appears as expected.");
			else
				Log.fail("Test Case Failed. The auto-fill confrimation dialog did not appear.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.17A : Verify to clear the value and ensure the value is not displayed
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Verify to clear the value and ensure the value is not displayed.")
	public void SprintTest48_1_17A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for an object and open it's properties
			//-------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Search for an object and open it's properties.");

			//3. Add the properties and set the values
			//-----------------------------------------
			metadatacard.setPropertyValue(dataPool.get("OwnerProperty"), dataPool.get("OwnerValue1"));


			metadatacard.setPropertyValue(dataPool.get("OwnerProperty"), dataPool.get("OwnerValue2"), 2);


			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value1"));


			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value2"), 2);


			Log.message("3. Add the properties and set the values.");

			//4. Remove one of the owner values
			//----------------------------------
			metadatacard.removePropertyValue(dataPool.get("OwnerProperty"), 2);

			metadatacard.saveAndClose();

			homePage.listView.clickItem(dataPool.get("Object"));
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			metadatacard = new MetadataCard(driver);

			Log.message("4. Remove one of the owner values.");

			//Verification: To Verify if the sub Value of the Owner is also removed
			//---------------------------------------------------------------------
			if(metadatacard.getPropertyFieldsCount(dataPool.get("Property")) == 1 && metadatacard.getPropertyValue(dataPool.get("Property")).equals(dataPool.get("Value1")))
				Log.pass("Test Case Passed. The value of the Sub Property was also removed as expected.");
			else
				Log.fail("Test Case Failed. The Value of the sub-property was not removed.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.17B : Verify to clear the value and ensure the value is not displayed (SidePane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Verify to clear the value and ensure the value is not displayed (SidePane).")
	public void SprintTest48_1_17B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for an object and open it's properties
			//-------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));

			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and open it's properties.");

			//3. Add the properties and set the values
			//-----------------------------------------
			metadatacard.setPropertyValue(dataPool.get("OwnerProperty"), dataPool.get("OwnerValue1"));

			metadatacard.setPropertyValue(dataPool.get("OwnerProperty"), dataPool.get("OwnerValue2"), 2);

			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value1"));

			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value2"), 2);

			Log.message("3. Add the properties and set the values.");

			//4. Remove one of the owner values
			//----------------------------------
			metadatacard.removePropertyValue(dataPool.get("OwnerProperty"), 2);

			metadatacard.saveAndClose();

			metadatacard = new MetadataCard(driver, true);

			Log.message("4. Remove one of the owner values.");

			//Verification: To Verify if the sub Value of the Owner is also removed
			//---------------------------------------------------------------------
			if(metadatacard.getPropertyFieldsCount(dataPool.get("Property")) == 1 && metadatacard.getPropertyValue(dataPool.get("Property")).equals(dataPool.get("Value1")))
				Log.pass("Test Case Passed. The value of the Sub Property was also removed as expected.");
			else
				Log.fail("Test Case Failed. The Value of the sub-property was not removed.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.20A : Projects related to selected contact person should get displayed in metadatacard opened through context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Projects related to selected contact person should get displayed in metadatacard opened through context menu.")
	public void SprintTest48_1_20A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Obtain the projects related to contact persons
			//--------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(Caption.ObjecTypes.ContactPerson.Value, "is", dataPool.get("ContactPerson"));
			homePage.searchPanel.search("", Caption.Search.SearchOnlyProjects.Value);
			List<String> expectedValues = homePage.listView.getColumnValues("Name");
			homePage.searchPanel.resetAll();

			Log.message("1. Projects related to contact person are obtained.");

			//Step-2: Navigate to the specified view and select the object
			//------------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' is not selected.");

			Log.message("2. Navigated to view (" + viewToNavigate + ") and object (" + dataPool.get("ObjectName") + ") is right clicked."); 

			//Step-3 : Open object metadatacard from context menu
			//---------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value);
			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("3. Metadatacard of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.");

			//4. Add Customer, Project and Contact person property and set value to the contact person
			//-----------------------------------------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Project.Value, "");
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, "");
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson"));

			Log.message("4." + Caption.ObjecTypes.Project.Value + ", " + Caption.ObjecTypes.Customer.Value + " and " + Caption.ObjecTypes.ContactPerson.Value + " is added and value is set to " +  Caption.ObjecTypes.ContactPerson.Value + " property.");

			//5. Save the metadatacard after setting contact person value
			//----------------------------------------------------------
			if (MFilesDialog.exists(driver, "Confirm Autofill")){
				MFilesDialog mFilesDialog = new MFilesDialog(driver, "Confirm Autofill");
				mFilesDialog.clickCancelButton();
			}

			metadatacard.saveAndClose();

			Log.message("5. Metadatacard is saved and closed");

			//6. Open the metdatacard properties
			//----------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName")))
				throw new SkipException("The specified object '" + dataPool.get("ObjectName") + "' is not selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value);
			metadatacard = new MetadataCard(driver);

			Log.message("6. Metadatacard properties is opened from context menu.");

			//Verify if all the objects in the vault are listes in the metadatacard
			//-----------------------------------------------------------------------
			List<String> actualValues = metadatacard.getAvailablePropertyValues(Caption.ObjecTypes.Project.Value);

			if(actualValues.size() != expectedValues.size())
				throw new Exception("The Number of values listed did not match the number of objects.");

			String addlInfo = "";

			for(int count = 0; count < expectedValues.size(); count++) {
				if(actualValues.indexOf(expectedValues.get(count)) == -1)
					addlInfo = addlInfo + expectedValues.get(count) + ",";
			}

			if (addlInfo.equals(""))
				Log.pass("Test Case Passed. Project values related to contact person are listed in metadatacard opened through context menu.");
			else
				Log.fail("Test Case Failed. Some project values are not listed in metadatacard opened through context menu. Missing Values are : . + addlInfo", driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //SprintTest48_1_20A

	/**
	 * 48.1.20B : Projects related to selected contact person should get displayed in metadatacard opened through operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Projects related to selected contact person should get displayed in metadatacard opened through operations menu.")
	public void SprintTest48_1_20B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Obtain the projects related to contact persons
			//--------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(Caption.ObjecTypes.ContactPerson.Value, "is", dataPool.get("ContactPerson"));
			homePage.searchPanel.search("", Caption.Search.SearchOnlyProjects.Value);
			List<String> expectedValues = homePage.listView.getColumnValues("Name");
			homePage.searchPanel.resetAll();

			Log.message("1. Projects related to contact person are obtained.");

			//Step-2: Navigate to the specified view and select the object
			//------------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' is not selected.");

			Log.message("2. Navigated to view (" + viewToNavigate + ") and object (" + dataPool.get("ObjectName") + ") is right clicked."); 

			//Step-3 : Open object metadatacard from operations menu
			//------------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value);
			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("3. Metadatacard of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.");

			//4. Add Customer, Project and Contact person property and set value to the contact person
			//-----------------------------------------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Project.Value, "");
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, "");
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson"));

			Log.message("4." + Caption.ObjecTypes.Project.Value + ", " + Caption.ObjecTypes.Customer.Value + " and " + Caption.ObjecTypes.ContactPerson.Value + " is added and value is set to " +  Caption.ObjecTypes.ContactPerson.Value + " property.");

			//5. Save the metadatacard after setting contact person value
			//----------------------------------------------------------
			if (MFilesDialog.exists(driver, "Confirm Autofill")){
				MFilesDialog mFilesDialog = new MFilesDialog(driver, "Confirm Autofill");
				mFilesDialog.clickCancelButton();
			}

			metadatacard.saveAndClose();

			Log.message("5. Metadatacard is saved and closed");


			//6. Open the metdatacard properties
			//---------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value);
			metadatacard = new MetadataCard(driver);

			Log.message("6. Metadatacard properties is opened from operations menu.");

			//Verify if all the objects in the vault are listes in the metadatacard
			//-----------------------------------------------------------------------
			List<String> actualValues = metadatacard.getAvailablePropertyValues(Caption.ObjecTypes.Project.Value);

			if(actualValues.size() != expectedValues.size())
				throw new Exception("The Number of values listed did not match the number of objects.");

			String addlInfo = "";

			for(int count = 0; count < expectedValues.size(); count++) {
				if(actualValues.indexOf(expectedValues.get(count)) == -1)
					addlInfo = addlInfo + expectedValues.get(count) + ",";
			}

			if (addlInfo.equals(""))
				Log.pass("Test Case Passed. Project values related to contact person are listed in metadatacard opened through operations menu");
			else
				Log.fail("Test Case Failed. Some project values are not listed in metadatacard opened through operations menu. Missing Values are : . + addlInfo", driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //SprintTest48_1_20B




	/**
	 * 48.1.21A : Verify the value of 'contact person' property upon removing 'customer' property value (SidePane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Verify the value of 'contact person' property upon removing 'customer' property value  (SidePane).")
	public void SprintTest48_1_21A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.");

			//2. Search for an object and open it's properties
			//-------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");



			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));


			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and open it's properties.");

			//3. Add the properties and set the values
			//-----------------------------------------
			metadatacard.setPropertyValue(dataPool.get("OwnerProperty"), dataPool.get("OwnerValue1"));


			metadatacard.setPropertyValue(dataPool.get("OwnerProperty"), dataPool.get("OwnerValue2"), 2);


			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));

			metadatacard.saveAndClose();


			Log.message("3. Add the properties and set the values.");

			//4. Remove one of the owner values
			//----------------------------------
			metadatacard = new MetadataCard(driver, true); 
			metadatacard.removePropertyValue(dataPool.get("OwnerProperty"), 1);

			metadatacard.saveAndClose();


			metadatacard = new MetadataCard(driver, true);

			Log.message("4. Remove one of the owner values.");

			//Verification: To Verify if the sub Value of the Owner is also removed
			//---------------------------------------------------------------------
			if(metadatacard.getPropertyValue(dataPool.get("Property")).equals(""))
				Log.pass("Test Case Passed. The value of the Sub Property was also removed as expected.");
			else
				Log.fail("Test Case Failed. The Value of the sub-property was not removed.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.21B : Verify the value of 'contact person' property upon removing 'customer' property value
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Verify the value of 'contact person' property upon removing 'customer' property value.")
	public void SprintTest48_1_21B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object and open it's properties
			//-------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value);

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Search for an object and open it's properties.");

			//3. Add the properties and set the values
			//-----------------------------------------
			metadatacard.setPropertyValue(dataPool.get("OwnerProperty"), dataPool.get("OwnerValue1"));
			metadatacard.setPropertyValue(dataPool.get("OwnerProperty"), dataPool.get("OwnerValue2"), 2);
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));

			Log.message("3. Add the properties and set the values.");

			//4. Remove one of the owner values
			//----------------------------------
			metadatacard.removePropertyValue(dataPool.get("OwnerProperty"), 1);

			metadatacard.saveAndClose();

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			metadatacard = new MetadataCard(driver);

			Log.message("4. Remove one of the owner values.");

			//Verification: To Verify if the sub Value of the Owner is also removed
			//---------------------------------------------------------------------
			if(metadatacard.getPropertyValue(dataPool.get("Property")).equals(""))
				Log.pass("Test Case Passed. The value of the Sub Property was also removed as expected.");
			else
				Log.fail("Test Case Failed. The Value of the sub-property was not removed.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.22A : Verify the value of 'project' property upon removing 'contact person' property value (SidePane)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Verify the value of 'project' property upon removing 'contact person' property value (SidePane).")
	public void SprintTest48_1_22A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object and open it's properties
			//-------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));

			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and open it's properties.");

			//3. Add the properties and set the values
			//-----------------------------------------
			metadatacard.setPropertyValue(dataPool.get("Property1"), dataPool.get("Value1"));
			metadatacard.addNewProperty(dataPool.get("OwnerProperty"));
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));
			metadatacard.saveAndClose();

			Log.message("3. Add the properties and set the values.");

			//Verification: To Verify if the Value of project property is removed
			//---------------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true);
			if(!metadatacard.getPropertyValue(dataPool.get("OwnerProperty")).equals(dataPool.get("OwnerValue")))
				throw new Exception("The Owner property was not auto-filled when the Sub value was filled.");

			if(metadatacard.getPropertyValue(dataPool.get("Property1")).equals(""))
				Log.pass("Test Case Passed. The value of the project property was emptied as expected.");
			else
				Log.fail("Test Case Failed. The value of the project property was not emptied.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.22B : Verify the value of 'project' property upon removing 'contact person' property value
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Verify the value of 'project' property upon removing 'contact person' property value.")
	public void SprintTest48_1_22B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object and open it's properties
			//-------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Search for an object and open it's properties.");

			//3. Add the properties and set the values
			//-----------------------------------------
			metadatacard.setPropertyValue(dataPool.get("Property1"), dataPool.get("Value1"));
			metadatacard.addNewProperty(dataPool.get("OwnerProperty"));
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));
			metadatacard.saveAndClose();

			Log.message("3. Add the properties and set the values.");

			//Verification: To Verify if the Value of project property is removed
			//---------------------------------------------------------------------

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			metadatacard = new MetadataCard(driver);

			if(!metadatacard.getPropertyValue(dataPool.get("OwnerProperty")).equals(dataPool.get("OwnerValue")))
				throw new Exception("The Owner property was not auto-filled when the Sub value was filled.");

			if(metadatacard.getPropertyValue(dataPool.get("Property1")).equals(""))
				Log.pass("Test Case Passed. The value of the project property was emptied as expected.");
			else
				Log.fail("Test Case Failed. The value of the project property was not emptied.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.23A : Verify to create and add sub values to properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint44", "Metadatacard"}, 
			description = "Verify to create and add sub values to properties.")
	public void SprintTest48_1_23A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object and open it's properties
			//-------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			homePage.listView.clickItem(dataPool.get("Object"));

			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and open it's properties.");

			//3. Add the properties and set the values
			//-----------------------------------------
			metadatacard.setPropertyValue(dataPool.get("OwnerProperty"), dataPool.get("OwnerValue"));
			metadatacard.createNewPropertyValue(dataPool.get("Property"), 1);
			driver.switchTo().defaultContent();

			Log.message("3. Add the properties and set the values.");

			//4. Create the new Sub value
			//----------------------------
			metadatacard = new MetadataCard(driver);

			if(!metadatacard.getPropertyValue(dataPool.get("OwnerMetadataProperty")).equals(dataPool.get("OwnerValue")))
				throw new Exception("The Owner value was not set in the metadatacard of the new sub value");

			metadatacard.setInfo(dataPool.get("Properties"));
			metadatacard.saveAndClose();

			Log.message("4. Create the new Sub value");

			//Verification: To Verify if the Value of project property is removed
			//---------------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true);

			if(metadatacard.getPropertyValue(dataPool.get("Property")).equals(dataPool.get("Value")))
				Log.pass("Test Case Passed. The value of the project property was emptied as expected.");
			else
				Log.fail("Test Case Failed. The value of the project property was not emptied.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 48.1.23B : Verify to create and add sub values to properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Verify to create and add sub values to properties.")
	public void SprintTest48_1_23B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object and open it's properties
			//-------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: "+dataPool.get("ObjectType")+"s");

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("The specified object '" + dataPool.get("Object") + "' was not created.");

			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and open it's properties.");

			//3. Add the properties and set the values
			//-----------------------------------------
			metadatacard.setPropertyValue(dataPool.get("OwnerProperty"), dataPool.get("OwnerValue"));
			metadatacard.setPropertyValue(dataPool.get("OwnerProperty"), dataPool.get("OwnerValue2"), 2);

			Log.message("3. Add the properties and set the values.");

			//4. Click the Add value option in the sub value field
			//-----------------------------------------------------
			metadatacard.createNewPropertyValue(dataPool.get("Property"), 1);
			driver.switchTo().defaultContent();

			Log.message("4. Click the Add value option in the sub value field");

			//Verification: To Verify if the warning dialog appears
			//------------------------------------------------------
			if(!MFilesDialog.exists(driver))
				throw new Exception("The Warning dialog did not appear.");

			MFilesDialog mFilesDialog = new MFilesDialog(driver);
			if(mFilesDialog.getMessage().contains(dataPool.get("ExpectedMessage")))
				Log.pass("Test Case Passed. The value of the project property was emptied as expected.");
			else
				Log.fail("Test Case Failed. The value of the project property was not emptied.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.26A : Customer property auto-fill after changing the Contact person in metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Customer property auto-fill after changing the Contact person in metadatacard.")
	public void SprintTest58_2_26A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object
			//------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");

			Log.message("2. Search for an object.");

			//3. Open it's metadatacard
			//--------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			MetadataCard metadataCard = new MetadataCard(driver);

			Log.message("3. Open it's metadatacard.");

			//4. Add the necessary Properties
			//--------------------------------
			metadataCard.addNewProperty(dataPool.get("OwnerProperty"));
			metadataCard.addNewProperty(dataPool.get("Property"));


			Log.message("4. Add the necessary Properties.");

			//5. Set a value for the sub property
			//------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value1"));

			if(!metadataCard.getPropertyValue(dataPool.get("OwnerProperty")).equals(dataPool.get("Owner1")))
				throw new Exception("The Owner value was not auto-filled once the child value was filled.");

			metadataCard.removePropertyValue(dataPool.get("OwnerProperty"), 1);


			Log.message("5. Set a value for the sub property");

			//6. Change the value of the Sub property
			//---------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value2"));

			Log.message("6. Open the Metadatacard of the object.");

			//Verification: To verify if the Comment count is shown correct
			//--------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("OwnerProperty")).equals(dataPool.get("Owner2"))) 
				Log.pass("Test Case Passed. Changing the value of sub property changes the value of the owner property.");
			else 
				Log.fail("Test Case Failed. Changing the value of sub property does not change the value of the owner property.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.26B : Customer property auto-fill after changing the Contact person in right pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Customer property auto-fill after changing the Contact person in right pane.")
	public void SprintTest58_2_26B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object
			//------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));


			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");

			Log.message("2. Search for an object.");

			//3. Add the necessary Properties
			//--------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);
			metadataCard.addNewProperty(dataPool.get("OwnerProperty"));
			metadataCard.addNewProperty(dataPool.get("Property"));


			Log.message("3. Add the necessary Properties.");

			//4. Set a value for the sub property
			//------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value1"));

			if(!metadataCard.getPropertyValue(dataPool.get("OwnerProperty")).equals(dataPool.get("Owner1")))
				throw new Exception("The Owner value was not auto-filled once the child value was filled.");

			metadataCard.removePropertyValue(dataPool.get("OwnerProperty"), 1);


			Log.message("4. Set a value for the sub property");

			//5. Change the value of the Sub property
			//---------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value2"));

			Log.message("5. Open the Metadatacard of the object.");

			//Verification: To verify if the Comment count is shown correct
			//--------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("OwnerProperty")).equals(dataPool.get("Owner2"))) 
				Log.pass("Test Case Passed. Changing the value of sub property changes the value of the owner property.");
			else 
				Log.fail("Test Case Failed. Changing the value of sub property does not change the value of the owner property.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.27A : Customer property auto-fill after changing the Contact person in metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Customer property auto-fill after changing the Contact person in metadatacard.")
	public void SprintTest58_2_27A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;  HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object and check out
			//--------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);

			Log.message("2. Search for an object and check out.");

			//3. Open it's metadatacard
			//--------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			MetadataCard metadataCard = new MetadataCard(driver);

			Log.message("3. Open it's metadatacard.");

			//4. Add the necessary Properties
			//--------------------------------
			metadataCard.addNewProperty(dataPool.get("OwnerProperty"));
			metadataCard.addNewProperty(dataPool.get("Property"));

			Log.message("4. Add the necessary Properties.");

			//5. Set a value for the sub property
			//------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value1"));

			if(!metadataCard.getPropertyValue(dataPool.get("OwnerProperty")).equals(dataPool.get("Owner1")))
				throw new Exception("The Owner value was not auto-filled once the child value was filled.");

			metadataCard.removePropertyValue(dataPool.get("OwnerProperty"), 1);

			Log.message("5. Set a value for the sub property");

			//6. Change the value of the Sub property
			//---------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value2"));

			Log.message("6. Open the Metadatacard of the object.");

			//Verification: To verify if the Comment count is shown correct
			//--------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("OwnerProperty")).equals(dataPool.get("Owner2"))) 
				Log.pass("Test Case Passed. Changing the value of sub property changes the value of the owner property.");
			else 
				Log.fail("Test Case Failed. Changing the value of sub property does not change the value of the owner property.", driver);

			metadataCard.saveAndClose();

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if (driver != null & homePage != null)
				if(homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.UndoCheckOut.Value)) {
					homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);
					MFilesDialog mFilesDialog = new MFilesDialog(driver);
					mFilesDialog.confirmUndoCheckOut(true);
				}

			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.27B : Customer property auto-fill after changing the Contact person in right pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Customer property auto-fill after changing the Contact person in right pane.")
	public void SprintTest58_2_27B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;  HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			homePage = LoginPage.launchDriverAndLogin(driver, true);

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			Log.message("1. Logged into the Home View.");

			//2. Search for an object and check out
			//--------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);

			Log.message("2. Search for an object and check out.");

			//3. Add the necessary Properties
			//--------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);
			metadataCard.addNewProperty(dataPool.get("OwnerProperty"));
			metadataCard.addNewProperty(dataPool.get("Property"));

			Log.message("3. Add the necessary Properties.");

			//4. Set a value for the sub property
			//------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value1"));

			if(!metadataCard.getPropertyValue(dataPool.get("OwnerProperty")).equals(dataPool.get("Owner1")))
				throw new Exception("The Owner value was not auto-filled once the child value was filled.");

			metadataCard.removePropertyValue(dataPool.get("OwnerProperty"), 1);

			Log.message("4. Set a value for the sub property");

			//5. Change the value of the Sub property
			//---------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value2"));

			Log.message("5. Open the Metadatacard of the object.");

			//Verification: To verify if the Comment count is shown correct
			//--------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("OwnerProperty")).equals(dataPool.get("Owner2"))) 
				Log.pass("Test Case Passed. Changing the value of sub property changes the value of the owner property.");
			else 
				Log.fail("Test Case Failed. Changing the value of sub property does not change the value of the owner property.", driver);

			metadataCard.saveAndClose();

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if (driver != null && homePage != null)
				if(homePage.menuBar.IsItemEnabledInOperationsMenu(Caption.MenuItems.UndoCheckOut.Value)) {
					homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);
					MFilesDialog mFilesDialog = new MFilesDialog(driver);
					mFilesDialog.confirmUndoCheckOut(true);
				}

			Utility.quitDriver(driver);
		}
	}

	/**
	 * SprintTest_132143 : Verify if created object is displayed in metadatacard lookup add-ons.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint44", "Metadatacard"}, 
			description = "Verify if created object is displayed in metadatacard lookup add-ons.")
	public void SprintTest_132143(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;  HomePage homePage = null;

		try {

			//Step-1 : Login to the Home View.
			//-------------------------------


			driver = WebDriverUtils.getDriver();

			homePage = LoginPage.launchDriverAndLogin(driver, true);//launch the MFWA with valid credentials

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			Log.message("1. Logged into the Home View.");

			//Step-2 : Search for an object and selected the object
			//-----------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));//Search to any object

			//Verify if object is listed in list view
			//---------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");

			Log.message("2. Navigated to the search view : " + dataPool.get("ObjectType") + " and selected the object : " + dataPool.get("Object") , driver);

			//Step-3 : Instantiate the right pane metadatacard & add the customer property in right pane metadatacard
			//-------------------------------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiate the right pane metadatacard
			if(!metadataCard.propertyExists(dataPool.get("Property")))//Verify if property is exist or not
				metadataCard.addNewProperty(dataPool.get("Property"));//Add the new property in the Right pane metadatacard

			Log.message("3. Added the : " + dataPool.get("Property") + " property in opened metadatacard.", driver);

			//Step-4 : Select the Add value for the specified property
			//--------------------------------------------------------
			metadataCard.clickAddValueButton(dataPool.get("Property"));//Click the add value button 

			Log.message("4. Selected the add value for the : " + dataPool.get("Property") + " property.", driver);

			//Step-5 : Enter the required value in the specified metadatacard
			//---------------------------------------------------------------
			metadataCard = new MetadataCard(driver);//Instantiate the pop-out metadatacard
			metadataCard.setPropertyValue(dataPool.get("propName"),dataPool.get("propValue"));//Enter the required property value in opened metadatacard
			metadataCard.clickCreateBtn();//Click the create button

			Log.message("5. Created the new customer object '" + dataPool.get("propValue") + "' from opened metadatacard.");

			//Instantiate the right pane metadata card & click the property
			//------------------------------------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiate the right pane metadatacard
			metadataCard.clickProperty(dataPool.get("Property"));//Click the property

			//Verification : Verify if metadata card property value is set as expected or not
			//------------------------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("Property")).equals(dataPool.get("ExpectedpropValue")))//Verify if property value is set as expected
				Log.pass("Test Case Passed.Created : "  + dataPool.get("Property") + " object is displayed in opened object metadatacard lookup.", driver);
			else 
				Log.fail("Test Case Failed.Created : "  + dataPool.get("Property") + " object is not displayed in opened object metadatacard lookup.", driver);


		}//End try
		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End SprintTest132143



}
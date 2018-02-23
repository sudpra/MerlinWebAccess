package MFClient.Tests;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.WebDriverUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.openqa.selenium.WebDriver;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import MFClient.Pages.HomePage;
import MFClient.Pages.LoginPage;
import MFClient.Wrappers.Caption;
import MFClient.Wrappers.ListView;
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class AutoFillPropertyValues {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String configURL = null;
	public static String userName = null;
	public static String password = null;
	public static String testVault = null;
	public static String productVersion = null;
	public static String userFullName = null;
	public String methodName = null;
	public static String driverType = null;
	public static String className = null;
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
			userName = xmlParameters.getParameter("UserName");
			password = xmlParameters.getParameter("Password");
			testVault = xmlParameters.getParameter("VaultName");		
			userFullName = xmlParameters.getParameter("UserFullName");
			Utility.restoreTestVault();
			Utility.configureUsers(xlTestDataWorkBook);
			className = this.getClass().getSimpleName().toString().trim();
			if (xmlParameters.getParameter("driverType").equalsIgnoreCase("IE"))
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim() + xmlParameters.getParameter("driverVersion").trim();
			else
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim();

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
	 * 99.1.1A : Customer property should not be auto-filled in metadatacard opened through context menu on selecting two contact persons of two different customers
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99", "QuickReg"}, 
			description = "Customer property should not be auto-filled in metadatacard opened through context menu on selecting two contact persons of two different customers")
	public void SprintTest99_1_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {




			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Open metadatacard properties from context menu
			//-------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Select Properties from context menu

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Metadatacard properties of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.",driver);

			//Step-3 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("3. Customer & Contact person is added to the metadatacard.",driver);

			//Step-4 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("4. Customer & Contactperson value are set in the metadatacard.",driver);

			//Step-5 : Clear the Value for Customer property
			//----------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, ""); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("5. Value from the customer property is cleared in metadatacard.",driver);

			//Step-6 : Add one more contact person to the metadatacard
			//--------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson2"), 2); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value, 2).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson2"));

			Log.message("6. Contactperson value is added to the metadatacard.",driver);

			//Verification: Verify if Customer is not autofilled
			//--------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(""))
				Log.pass("Test case Passed. Customer property is not autofilled when two contact persons of different customer is added in metadatacard opened through context menu.",driver);
			else
				Log.fail("Test case Failed. Customer property is autofilled when two contact persons of different customer is added in metadatacard opened through context menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_1A

	/**
	 * 99.1.1B : Customer property should not be auto-filled in metadatacard opened through operations menu on selecting two contact persons of two different customers
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99"}, 
			description = "Customer property should not be auto-filled in metadatacard opened through operations menu on selecting two contact persons of two different customers")
	public void SprintTest99_1_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {




			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Open metadatacard properties from operations menu
			//-------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Select Properties from operations menu

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Metadatacard properties of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.",driver);

			//Step-3 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("3. Customer & Contact person is added to the metadatacard.",driver);

			//Step-4 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("4. Customer & Contactperson value are set in the metadatacard.",driver);

			//Step-5 : Clear the Value for Customer property
			//----------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, ""); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("5. Value from the customer property is cleared in metadatacard.",driver);

			//Step-6 : Add one more contact person to the metadatacard
			//--------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson2"), 2); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value, 2).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson2"));

			Log.message("6. Contactperson value is added to the metadatacard.",driver);

			//Verification: Verify if Customer is not autofilled
			//--------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(""))
				Log.pass("Test case Passed. Customer property is not autofilled when two contact persons of different customer is added in metadatacard opened through operations menu.",driver);
			else
				Log.fail("Test case Failed. Customer property is autofilled when two contact persons of different customer is added in metadatacard opened through operations menu.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_1B

	/**
	 * 99.1.1C : Customer property should not be auto-filled in metadatacard opened through taskpanel menu on selecting two contact persons of two different customers
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99"}, 
			description = "Customer property should not be auto-filled in metadatacard opened through taskpanel menu on selecting two contact persons of two different customers")
	public void SprintTest99_1_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Open metadatacard properties from taskpanel menu
			//-------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Select Properties from operations menu

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Metadatacard properties of an object (" + dataPool.get("ObjectName") + ") is opened from taskpanel menu.",driver);

			//Step-3 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("3. Customer & Contact person is added to the metadatacard.",driver);

			//Step-4 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("4. Customer & Contactperson value are set in the metadatacard.",driver);

			//Step-5 : Clear the Value for Customer property
			//----------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, ""); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("5. Value from the customer property is cleared in metadatacard.",driver);

			//Step-6 : Add one more contact person to the metadatacard
			//--------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson2"), 2); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value, 2).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson2"));

			Log.message("6. Contactperson value is added to the metadatacard.",driver);

			//Verification: Verify if Customer is not autofilled
			//--------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(""))
				Log.pass("Test case Passed. Customer property is not autofilled when two contact persons of different customer is added in metadatacard opened through taskpanel menu.",driver);
			else
				Log.fail("Test case Failed. Customer property is autofilled when two contact persons of different customer is added in metadatacard opened through taskpanel menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_1C

	/**
	 * 99.1.1D : Customer property should not be auto-filled in metadatacard opened in right pane on selecting two contact persons of two different customers
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99"}, 
			description = "Customer property should not be auto-filled in metadatacard opened through taskpanel menu on selecting two contact persons of two different customers")
	public void SprintTest99_1_1D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Open metadatacard properties in rightpane
			//-------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("2. Metadatacard properties of an object (" + dataPool.get("ObjectName") + ") is opened in right pane.",driver);

			//Step-3 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("3. Customer & Contact person is added to the metadatacard.",driver);

			//Step-4 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("4. Customer & Contactperson value are set in the metadatacard.",driver);

			//Step-5 : Clear the Value for Customer property
			//----------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, ""); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("5. Value from the customer property is cleared in metadatacard.",driver);

			//Step-6 : Add one more contact person to the metadatacard
			//--------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson2"), 2); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value, 2).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson2"));

			Log.message("6. Contactperson value is added to the metadatacard.",driver);

			//Verification: Verify if Customer is not autofilled
			//--------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(""))
				Log.pass("Test case Passed. Customer property is not autofilled when two contact persons of different customer is added in metadatacard opened in rightpane.",driver);
			else
				Log.fail("Test case Failed. Customer property is autofilled when two contact persons of different customer is added in metadatacard opened in rightpane.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_1D

	/**
	 * 99.1.2.1A : Customer property should be auto-filled in metadatacard opened through context menu on clearing one of two contact persons of two different customers.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99","Bug"}, 
			description = "Customer property should be auto-filled in metadatacard opened through context menu on clearing one of two contact persons of two different customers.")
	public void SprintTest99_1_2_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Open metadatacard properties from context menu
			//-------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Select Properties from context menu

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Metadatacard properties of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.",driver);

			//Step-3 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("3. Customer & Contact person is added to the metadatacard.",driver);

			//Step-4 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("4. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-5 : Clear the value to customer property
			//----------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, ""); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("5. Value from the customer property is cleared in metadatacard.",driver);

			//Step-6 : Add one more contact person to the metadatacard
			//--------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson2"), 2); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value, 2).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson2"));

			Log.message("6. New contactperson value (" + dataPool.get("ContactPerson2") + ") is added to the metadatacard.",driver);

			//Step-7 : Clear the first value of contact person property
			//----------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, ""); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception("Value (" + dataPool.get("ContactPerson2") + ") from property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not cleared.");

			Log.message("7. First Value(" + dataPool.get("ContactPerson1") + ") is cleared for contact person property.",driver);

			//Verification: Verify if Customer of second contact person is autofilled 
			//-----------------------------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer2")))
				Log.pass("Test case Passed. Customer property is autofilled on clearing one of two contact persons of two different customers in metadatacard opened through context menu.",driver);
			else
				Log.fail("Test case Failed. Customer property is not autofilled on clearing one of two contact persons of two different customers in metadatacard opened through context menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_2_1A

	/**
	 * 99.1.2.1B : Customer property should be auto-filled in metadatacard opened through operations menu on clearing one of two contact persons of two different customers.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99","Bug", "QuickReg"}, 
			description = "Customer property should be auto-filled in metadatacard opened through operations menu on clearing one of two contact persons of two different customers.")
	public void SprintTest99_1_2_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Open metadatacard properties from operations menu
			//-------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Select Properties from operations menu

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Metadatacard properties of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.",driver);

			//Step-3 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("3. Customer & Contact person is added to the metadatacard.",driver);

			//Step-4 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("4. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard."
					,driver);

			//Step-5 : Clear the value to customer property
			//----------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, ""); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("5. Value from the customer property is cleared in metadatacard."
					,driver);

			//Step-6 : Add one more contact person to the metadatacard
			//--------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson2"), 2); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value, 2).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson2"));

			Log.message("6. New contactperson value (" + dataPool.get("ContactPerson2") + ") is added to the metadatacard."
					,driver);

			//Step-7 : Clear the first value of contact person property
			//----------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, ""); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception("Value (" + dataPool.get("ContactPerson2") + ") from property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not cleared.");

			Log.message("7. First Value(" + dataPool.get("ContactPerson1") + ") is cleared for contact person property."
					,driver);

			//Verification: Verify if Customer of second contact person is autofilled 
			//-----------------------------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer2")))
				Log.pass("Test case Passed. Customer property is autofilled on clearing one of two contact persons of two different customers in metadatacard opened through operations menu."
						,driver);
			else
				Log.fail("Test case Failed. Customer property is not autofilled on clearing one of two contact persons of two different customers in metadatacard opened through operations menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_2_1B

	/**
	 * 99.1.2.1C : Customer property should be auto-filled in metadatacard opened through taskpanel menu on clearing one of two contact persons of two different customers.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99","Bug"}, 
			description = "Customer property should be auto-filled in metadatacard opened through taskpanel menu on clearing one of two contact persons of two different customers.")
	public void SprintTest99_1_2_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Open metadatacard properties from taskpanel menu
			//-------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Select Properties from taskpanel menu

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Metadatacard properties of an object (" + dataPool.get("ObjectName") + ") is opened from taskpanel menu.",driver);

			//Step-3 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("3. Customer & Contact person is added to the metadatacard.",driver);

			//Step-4 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("4. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-5 : Clear the value to customer property
			//----------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, ""); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("5. Value from the customer property is cleared in metadatacard.",driver);

			//Step-6 : Add one more contact person to the metadatacard
			//--------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson2"), 2); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value, 2).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson2"));

			Log.message("6. New contactperson value (" + dataPool.get("ContactPerson2") + ") is added to the metadatacard.",driver);

			//Step-7 : Clear the first value of contact person property
			//----------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, ""); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception("Value (" + dataPool.get("ContactPerson2") + ") from property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not cleared.");

			Log.message("7. First Value(" + dataPool.get("ContactPerson1") + ") is cleared for contact person property.",driver);

			//Verification: Verify if Customer of second contact person is autofilled 
			//-----------------------------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer2")))
				Log.pass("Test case Passed. Customer property is autofilled on clearing one of two contact persons of two different customers in metadatacard opened through taskpanel menu.",driver);
			else
				Log.fail("Test case Failed. Customer property is not autofilled on clearing one of two contact persons of two different customers in metadatacard opened through taskpanel menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_2_1C

	/**
	 * 99.1.2.1D : Customer property should be auto-filled in metadatacard opened in rightpane on clearing one of two contact persons of two different customers.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99","Bug"}, 
			description = "Customer property should be auto-filled in metadatacard opened in rightpane on clearing one of two contact persons of two different customers.")
	public void SprintTest99_1_2_1D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Open metadatacard properties from rightpane
			//-------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is selected.");

			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("2. Metadatacard properties of an object (" + dataPool.get("ObjectName") + ") is opened in rightpane.",driver);

			//Step-3 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("3. Customer & Contact person is added to the metadatacard.",driver);

			//Step-4 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("4. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-5 : Clear the value to customer property
			//----------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, ""); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("5. Value from the customer property is cleared in metadatacard.",driver);

			//Step-6 : Add one more contact person to the metadatacard
			//--------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson2"), 2); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value, 2).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson2"));

			Log.message("6. New contactperson value (" + dataPool.get("ContactPerson2") + ") is added to the metadatacard.",driver);

			//Step-7 : Clear the first value of contact person property
			//----------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, ""); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception("Value (" + dataPool.get("ContactPerson2") + ") from property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not cleared.");

			Log.message("7. First Value(" + dataPool.get("ContactPerson1") + ") is cleared for contact person property.",driver);

			//Verification: Verify if Customer of second contact person is autofilled 
			//-----------------------------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer2")))
				Log.pass("Test case Passed. Customer property is autofilled on clearing one of two contact persons of two different customers in metadatacard opened in rightpane.",driver);
			else
				Log.fail("Test case Failed. Customer property is not autofilled on clearing one of two contact persons of two different customers in metadatacard opened in rightpane.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_2_1D

	/**
	 * 99.1.2.2A : Customer property should be auto-filled in metadatacard opened through context menu on removing ('-' icon) one of two contact persons of two different customers.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99","Bug"}, 
			description = "Customer property should be auto-filled in metadatacard opened through context menu on clearing one of two contact persons of two different customers.")
	public void SprintTest99_1_2_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Open metadatacard properties from context menu
			//-------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Select Properties from context menu

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Metadatacard properties of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.",driver);

			//Step-3 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("3. Customer & Contact person is added to the metadatacard.",driver);

			//Step-4 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("4. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-5 : Remove the value to customer property using '-' icon
			//-----------------------------------------------------------
			metadatacard.removePropertyValue(Caption.ObjecTypes.Customer.Value); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("5. Value from the customer property is removed in metadatacard using '-' button.",driver);

			//Step-6 : Add one more contact person to the metadatacard
			//--------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson2"), 2); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value, 2).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson2"));

			Log.message("6. New contactperson value (" + dataPool.get("ContactPerson2") + ") is added to the metadatacard.",driver);

			//Step-7 : Clear the first value of contact person property
			//----------------------------------------------------------
			metadatacard.removePropertyValue(Caption.ObjecTypes.ContactPerson.Value); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception("Value (" + dataPool.get("ContactPerson2") + ") from property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not cleared.");

			Log.message("7. First Value(" + dataPool.get("ContactPerson1") + ") is removed from contact person property using '-' button..",driver);

			//Verification: Verify if Customer of second contact person is autofilled 
			//-----------------------------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer2")))
				Log.pass("Test case Passed. Customer property is autofilled on removing one of two contact persons of two different customers in metadatacard opened through context menu.",driver);
			else
				Log.fail("Test case Failed. Customer property is not autofilled on removing one of two contact persons of two different customers in metadatacard opened through context menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_2_2A

	/**
	 * 99.1.2.2B : Customer property should be auto-filled in metadatacard opened through operations menu on removing ('-' icon) one of two contact persons of two different customers.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99","Bug"}, 
			description = "Customer property should be auto-filled in metadatacard opened through operations menu on removing ('-' icon) one of two contact persons of two different customers.")
	public void SprintTest99_1_2_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Open metadatacard properties from operations menu
			//-------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Select Properties from operations menu

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Metadatacard properties of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.",driver);

			//Step-3 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("3. Customer & Contact person is added to the metadatacard.",driver);

			//Step-4 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("4. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-5 : Remove the value to customer property using '-' icon
			//-----------------------------------------------------------
			metadatacard.removePropertyValue(Caption.ObjecTypes.Customer.Value); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("5. Value from the customer property is removed in metadatacard using '-' button.",driver);

			//Step-6 : Add one more contact person to the metadatacard
			//--------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson2"), 2); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value, 2).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson2"));

			Log.message("6. New contactperson value (" + dataPool.get("ContactPerson2") + ") is added to the metadatacard.",driver);

			//Step-7 : Clear the first value of contact person property
			//----------------------------------------------------------
			metadatacard.removePropertyValue(Caption.ObjecTypes.ContactPerson.Value); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception("Value (" + dataPool.get("ContactPerson2") + ") from property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not cleared.");

			Log.message("7. First Value(" + dataPool.get("ContactPerson1") + ") is removed from contact person property using '-' button..",driver);

			//Verification: Verify if Customer of second contact person is autofilled 
			//-----------------------------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer2")))
				Log.pass("Test case Passed. Customer property is autofilled on removing one of two contact persons of two different customers in metadatacard opened through operations menu.",driver);
			else
				Log.fail("Test case Failed. Customer property is not autofilled on removing one of two contact persons of two different customers in metadatacard opened through operations menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_2_2B

	/**
	 * 99.1.2.2C : Customer property should be auto-filled in metadatacard opened through taskpanel menu on removing ('-' icon) one of two contact persons of two different customers.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99", "Bug", "QuickReg"}, 
			description = "Customer property should be auto-filled in metadatacard opened through taskpanel menu on removing ('-' icon) one of two contact persons of two different customers.")
	public void SprintTest99_1_2_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Open metadatacard properties from taskpanel menu
			//-------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Select Properties from taskpanel menu

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Metadatacard properties of an object (" + dataPool.get("ObjectName") + ") is opened from taskpanel menu.",driver);

			//Step-3 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("3. Customer & Contact person is added to the metadatacard.",driver);

			//Step-4 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("4. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-5 : Remove the value to customer property using '-' icon
			//-----------------------------------------------------------
			metadatacard.removePropertyValue(Caption.ObjecTypes.Customer.Value); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("5. Value from the customer property is removed in metadatacard using '-' button.",driver);

			//Step-6 : Add one more contact person to the metadatacard
			//--------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson2"), 2); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value, 2).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson2"));

			Log.message("6. New contactperson value (" + dataPool.get("ContactPerson2") + ") is added to the metadatacard.",driver);

			//Step-7 : Clear the first value of contact person property
			//----------------------------------------------------------
			metadatacard.removePropertyValue(Caption.ObjecTypes.ContactPerson.Value); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception("Value (" + dataPool.get("ContactPerson1") + ") from " + Caption.ObjecTypes.ContactPerson.Value + " property is not cleared.");

			Log.message("7. First Value(" + dataPool.get("ContactPerson1") + ") is removed from contact person property using '-' button..",driver);

			//Verification: Verify if Customer of second contact person is autofilled 
			//-----------------------------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer2")))
				Log.pass("Test case Passed. Customer property is autofilled on removing one of two contact persons of two different customers in metadatacard opened through taskpanel menu.",driver);
			else
				Log.fail("Test case Failed. Customer property is not autofilled on removing one of two contact persons of two different customers in metadatacard opened through taskpanel menu.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_2_2C

	/**
	 * 99.1.2.2D : Customer property should be auto-filled in metadatacard opened in rightpane on removing ('-' icon) one of two contact persons of two different customers.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99","Bug"}, 
			description = "Customer property should be auto-filled in metadatacard opened in rightpane on removing ('-' icon) one of two contact persons of two different customers.")
	public void SprintTest99_1_2_2D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Open metadatacard properties from rightpane
			//-------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is selected.");

			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("2. Metadatacard properties of an object (" + dataPool.get("ObjectName") + ") is opened in rightpane.",driver);

			//Step-3 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("3. Customer & Contact person is added to the metadatacard.",driver);

			//Step-4 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("4. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-5 : Remove the value to customer property using '-' icon
			//-----------------------------------------------------------
			metadatacard.removePropertyValue(Caption.ObjecTypes.Customer.Value); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("5. Value from the customer property is removed in metadatacard using '-' button.",driver);

			//Step-6 : Add one more contact person to the metadatacard
			//--------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson2"), 2); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value, 2).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson2"));

			Log.message("6. New contactperson value (" + dataPool.get("ContactPerson2") + ") is added to the metadatacard.",driver);

			//Step-7 : Clear the first value of contact person property
			//----------------------------------------------------------
			metadatacard.removePropertyValue(Caption.ObjecTypes.ContactPerson.Value); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception("Value (" + dataPool.get("ContactPerson2") + ") from property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not cleared.");

			Log.message("7. First Value(" + dataPool.get("ContactPerson1") + ") is removed from contact person property using '-' button..",driver);

			//Verification: Verify if Customer of second contact person is autofilled 
			//-----------------------------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer2")))
				Log.pass("Test case Passed. Customer property is autofilled on removing one of two contact persons of two different customers in metadatacard opened in rightpane.",driver);
			else
				Log.fail("Test case Failed. Customer property is not autofilled on removing one of two contact persons of two different customers in metadatacard opened in rightpane.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_2_2D

	/**
	 * 99.1.3.1A : Customer property should be autofilled in metadatacard opened through context menu properties on removing existing customer& contact person and adding new contact person.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99"}, 
			description = "Customer property should be autofilled in metadatacard opened through context menu properties on removing existing customer& contact person and adding new contact person.")
	public void SprintTest99_1_3_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Open metadatacard properties from context menu
			//-------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Select Properties from context menu

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Metadatacard properties of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.",driver);

			//Step-3 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("3. Customer & Contact person is added to the metadatacard.",driver);

			//Step-4 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("4. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-5 : Remove the value to customer property 
			//-----------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, ""); //Removes the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("5. Value from the customer property is removed in metadatacard.",driver);

			//Step-6 : Remove the value to contact person property 
			//-----------------------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, ""); //Removes the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not removed.");

			Log.message("6. Value from the contact person property is removed in metadatacard.",driver);

			//Step-7 : Set Value to the customer & contact person
			//---------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson2")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson2"));

			Log.message("7. Values for Contactperson (" + dataPool.get("ContactPerson2") + ") is added in the metadatacard.",driver);

			//Verification: Verify if Customer is autofilled
			//-----------------------------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer2")))
				Log.pass("Test case Passed. Customer property is autofilled on adding contact person in metadatacard opened through context menu.",driver);
			else
				Log.fail("Test case Failed. Customer property is not autofilled on adding contact person in metadatacard opened through context menu..", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_3_1A

	/**
	 * 99.1.3.1B : Customer property should be autofilled in metadatacard opened through operations menu properties on removing existing customer& contact person and adding new contact person.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99"}, 
			description = "Customer property should be autofilled in metadatacard opened through operations menu properties on removing existing customer& contact person and adding new contact person.")
	public void SprintTest99_1_3_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Open metadatacard properties from operations menu
			//-------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Select Properties from operations menu

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Metadatacard properties of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.",driver);

			//Step-3 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("3. Customer & Contact person is added to the metadatacard.",driver);

			//Step-4 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("4. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-5 : Remove the value to customer property 
			//-----------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, ""); //Removes the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("5. Value from the customer property is removed in metadatacard.",driver);

			//Step-6 : Remove the value to contact person property 
			//-----------------------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, ""); //Removes the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not removed.");

			Log.message("6. Value from the contact person property is removed in metadatacard.",driver);

			//Step-7 : Set Value to the customer & contact person
			//---------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson2")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson2"));

			Log.message("7. Values for Contactperson (" + dataPool.get("ContactPerson2") + ") is added in the metadatacard.",driver);

			//Verification: Verify if Customer is autofilled
			//-----------------------------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer2")))
				Log.pass("Test case Passed. Customer property is autofilled on adding contact person in metadatacard opened through operations menu.",driver);
			else
				Log.fail("Test case Failed. Customer property is not autofilled on adding contact person in metadatacard opened through operations menu..", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_3_1B

	/**
	 * 99.1.3.1C : Customer property should be autofilled in metadatacard opened through taskpane menu properties on removing existing customer& contact person and adding new contact person.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99"}, 
			description = "Customer property should be autofilled in metadatacard opened through taskpane menu properties on removing existing customer& contact person and adding new contact person.")
	public void SprintTest99_1_3_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Open metadatacard properties from taskpanel menu
			//-------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Select Properties from taskpanel menu

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Metadatacard properties of an object (" + dataPool.get("ObjectName") + ") is opened from taskpanel menu.",driver);

			//Step-3 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("3. Customer & Contact person is added to the metadatacard.",driver);

			//Step-4 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("4. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-5 : Remove the value to customer property 
			//-----------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, ""); //Removes the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("5. Value from the customer property is removed in metadatacard.",driver);

			//Step-6 : Remove the value to contact person property 
			//-----------------------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, ""); //Removes the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not removed.");

			Log.message("6. Value from the contact person property is removed in metadatacard.",driver);

			//Step-7 : Set Value to the customer & contact person
			//---------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson2")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson2"));

			Log.message("7. Values for Contactperson (" + dataPool.get("ContactPerson2") + ") is added in the metadatacard.",driver);

			//Verification: Verify if Customer is autofilled
			//-----------------------------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer2")))
				Log.pass("Test case Passed. Customer property is autofilled on adding contact person in metadatacard opened through taskpanel menu.",driver);
			else
				Log.fail("Test case Failed. Customer property is not autofilled on adding contact person in metadatacard opened through taskpanel menu..", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_3_1C

	/**
	 * 99.1.3.1D : Customer property should be autofilled in metadatacard opened in rightpane metadatacard on removing existing customer& contact person and adding new contact person.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99", "QuickReg"}, 
			description = "Customer property should be autofilled in metadatacard opened in rightpane metadatacard on removing existing customer& contact person and adding new contact person.")
	public void SprintTest99_1_3_1D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Open metadatacard properties from rightpane
			//-------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is selected.");

			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("2. Metadatacard properties of an object (" + dataPool.get("ObjectName") + ") is opened in rightpane.",driver);

			//Step-3 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("3. Customer & Contact person is added to the metadatacard.",driver);

			//Step-4 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("4. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-5 : Remove the value to customer property
			//-----------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, ""); //Removes the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("5. Value from the customer property is removed in metadatacard.",driver);

			//Step-6 : Remove the value to contact person property
			//-----------------------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, ""); //Removes the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not removed.");

			Log.message("6. Value from the contact person property is removed in metadatacard.",driver);

			//Step-7 : Set Value to the customer & contact person
			//---------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson2")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson2"));

			Log.message("7. Values for Contactperson (" + dataPool.get("ContactPerson2") + ") is added in the metadatacard.",driver);

			//Verification: Verify if Customer is autofilled
			//-----------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer2")))
				Log.pass("Test case Passed. Customer property is autofilled on adding contact person in metadatacard opened in rightpane.",driver);
			else
				Log.fail("Test case Failed. Customer property is not autofilled on adding contact person in metadatacard opened in rightpane.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_3_1D

	/**
	 * 99.1.3.2A : Customer property should be autofilled in metadatacard opened through context menu properties on removing existing customer& contact person using '-' icon and adding new contact person.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99", "QuickReg"}, 
			description = "Customer property should be autofilled in metadatacard opened through context menu properties on removing existing customer& contact person using '-' icon and adding new contact person.")
	public void SprintTest99_1_3_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Open metadatacard properties from context menu
			//-------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Select Properties from context menu

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Metadatacard properties of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.",driver);

			//Step-3 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("3. Customer & Contact person is added to the metadatacard.",driver);

			//Step-4 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("4. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-5 : Remove the value to customer property using '-' icon
			//-----------------------------------------------------------
			metadatacard.removePropertyValue(Caption.ObjecTypes.Customer.Value); //Removes the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("5. Value from the customer property is removed in metadatacard using '-' button.",driver);

			//Step-6 : Remove the value to contact person property using '-' icon
			//-----------------------------------------------------------------------
			metadatacard.removePropertyValue(Caption.ObjecTypes.ContactPerson.Value); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not removed.");

			Log.message("6. Value from the contact person property is removed in metadatacard using '-' button.",driver);

			//Step-7 : Set Value to the customer & contact person
			//---------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson2")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson2"));

			Log.message("7. Values for Contactperson (" + dataPool.get("ContactPerson2") + ") is added in the metadatacard.",driver);

			//Verification: Verify if Customer is autofilled
			//-----------------------------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer2")))
				Log.pass("Test case Passed. Customer property is autofilled on adding contact person in metadatacard opened through context menu.",driver);
			else
				Log.fail("Test case Failed. Customer property is not autofilled on adding contact person in metadatacard opened through context menu..", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_3_2A

	/**
	 * 99.1.3.2B : Customer property should be autofilled in metadatacard opened through operations menu properties on removing existing customer& contact person using ‘-’ icon and adding new contact person.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99"}, 
			description = "Customer property should be autofilled in metadatacard opened through operations menu properties on removing existing customer& contact person using ‘-’ icon and adding new contact person.")
	public void SprintTest99_1_3_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Open metadatacard properties from operations menu
			//-------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Select Properties from operations menu

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Metadatacard properties of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.",driver);

			//Step-3 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("3. Customer & Contact person is added to the metadatacard.",driver);

			//Step-4 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("4. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-5 : Remove the value to customer property using '-' icon
			//-----------------------------------------------------------
			metadatacard.removePropertyValue(Caption.ObjecTypes.Customer.Value); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("5. Value from the customer property is removed in metadatacard using '-' button.",driver);

			//Step-6 : Remove the value to contact person property using '-' icon
			//-----------------------------------------------------------------------
			metadatacard.removePropertyValue(Caption.ObjecTypes.ContactPerson.Value); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not removed.");

			Log.message("6. Value from the contact person property is removed in metadatacard using '-' button.",driver);

			//Step-7 : Set Value to the customer & contact person
			//---------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson2")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson2"));

			Log.message("7. Values for Contactperson (" + dataPool.get("ContactPerson2") + ") is added in the metadatacard.",driver);

			//Verification: Verify if Customer is autofilled
			//-----------------------------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer2")))
				Log.pass("Test case Passed. Customer property is autofilled on adding contact person in metadatacard opened through operations menu.",driver);
			else
				Log.fail("Test case Failed. Customer property is not autofilled on adding contact person in metadatacard opened through operations menu..", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_3_2B

	/**
	 * 99.1.3.2C : Customer property should be autofilled in metadatacard opened through taskpane menu properties on removing existing customer& contact person using ‘-’ icon and adding new contact person.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99"}, 
			description = "Customer property should be autofilled in metadatacard opened through taskpane menu properties on removing existing customer& contact person using ‘-’ icon and adding new contact person.")
	public void SprintTest99_1_3_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Open metadatacard properties from taskpanel menu
			//-------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Select Properties from taskpanel menu

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Metadatacard properties of an object (" + dataPool.get("ObjectName") + ") is opened from taskpanel menu.",driver);

			//Step-3 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("3. Customer & Contact person is added to the metadatacard.",driver);

			//Step-4 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("4. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-5 : Remove the value to customer property using '-' icon
			//-----------------------------------------------------------
			metadatacard.removePropertyValue(Caption.ObjecTypes.Customer.Value); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("5. Value from the customer property is removed in metadatacard using '-' button.",driver);

			//Step-6 : Remove the value to contact person property using '-' icon
			//-----------------------------------------------------------------------
			metadatacard.removePropertyValue(Caption.ObjecTypes.ContactPerson.Value); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not removed.");

			Log.message("6. Value from the contact person property is removed in metadatacard using '-' button.",driver);

			//Step-7 : Set Value to the customer & contact person
			//---------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson2")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson2"));

			Log.message("7. Values for Contactperson (" + dataPool.get("ContactPerson2") + ") is added in the metadatacard.",driver);

			//Verification: Verify if Customer is autofilled
			//-----------------------------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer2")))
				Log.pass("Test case Passed. Customer property is autofilled on adding contact person in metadatacard opened through taskpanel menu.",driver);
			else
				Log.fail("Test case Failed. Customer property is not autofilled on adding contact person in metadatacard opened through taskpanel menu..", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_3_2C

	/**
	 * 99.1.3.2D : Customer property should be autofilled in metadatacard opened in rightpane metadatacard on removing existing customer& contact person using ‘-’ icon and adding new contact person.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99"}, 
			description = "Customer property should be autofilled in metadatacard opened in rightpane metadatacard on removing existing customer& contact person using ‘-’ icon and adding new contact person.")
	public void SprintTest99_1_3_2D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Open metadatacard properties from rightpane
			//-------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is selected.");

			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("2. Metadatacard properties of an object (" + dataPool.get("ObjectName") + ") is opened in rightpane.",driver);

			//Step-3 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("3. Customer & Contact person is added to the metadatacard.",driver);

			//Step-4 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("4. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-5 : Remove the value to customer property using '-' icon
			//-----------------------------------------------------------
			metadatacard.removePropertyValue(Caption.ObjecTypes.Customer.Value); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("5. Value from the customer property is removed in metadatacard using '-' button.",driver);

			//Step-6 : Remove the value to contact person property using '-' icon
			//-----------------------------------------------------------------------
			metadatacard.removePropertyValue(Caption.ObjecTypes.ContactPerson.Value); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not removed.");

			Log.message("6. Value from the contact person property is removed in metadatacard using '-' button.",driver);

			//Step-7 : Set Value to the customer & contact person
			//---------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson2")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson2"));

			Log.message("7. Values for Contactperson (" + dataPool.get("ContactPerson2") + ") is added in the metadatacard.",driver);

			//Verification: Verify if Customer is autofilled
			//-----------------------------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer2")))
				Log.pass("Test case Passed. Customer property is autofilled on adding contact person in metadatacard opened in rightpane.",driver);
			else
				Log.fail("Test case Failed. Customer property is not autofilled on adding contact person in metadatacard opened in rightpane.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_3_2D

	/**
	 * 99.1.7A : Customer property should not be auto-filled in metadatacard opened through context menu on selecting two contact persons of two different customers for latest version of an object in History View
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99"}, 
			description = "Customer property should not be auto-filled in metadatacard opened through context menu on selecting two contact persons of two different customers for latest version of an object in History View.")
	public void SprintTest99_1_7A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Select Object and open its Histoy view
			//-----------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.History.Value); //Select Properties from context menu

			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened from taskpanel.",driver);

			//Step-3 : Open metadatacard properties from context menu
			//-------------------------------------------------------
			if (!homePage.listView.rightClickItemByIndex(0)) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Select Properties from context menu

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("3. Metadatacard properties of latest version of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.",driver);

			//Step-4 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("4. Customer & Contact person is added to the metadatacard.",driver);

			//Step-5 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("5. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-6 : Clear the value to customer property
			//----------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, ""); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("6. Value from the customer property is cleared in metadatacard.",driver);

			//Step-7 : Add one more contact person to the metadatacard
			//--------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson2"), 2); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value, 2).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson2"));

			Log.message("7. New contactperson value (" + dataPool.get("ContactPerson2") + ") is added to the metadatacard.",driver);

			//Verification: Verify if Customer is not autofilled on adding second contact person
			//----------------------------------------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(""))
				Log.pass("Test case Passed. Customer property is not autofilled on adding two contact persons of two different customers in metadatacard opened through context menu for latest version of an object in History view.",driver);
			else
				Log.fail("Test case Failed. Customer property is autofilled on adding two contact persons of two different customers in metadatacard opened through context menu for latest version of an object in History view.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_7A

	/**
	 * 99.1.7B : Customer property should not be auto-filled in metadatacard opened through operations menu on selecting two contact persons of two different customers for latest version of an object in History View
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99"}, 
			description = "Customer property should not be auto-filled in metadatacard opened through operations menu on selecting two contact persons of two different customers for latest version of an object in History View.")
	public void SprintTest99_1_7B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Select Object and open its Histoy view
			//-----------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.History.Value); //Select Properties from context menu

			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened from taskpanel.",driver);

			//Step-3 : Open metadatacard properties from operations menu
			//-------------------------------------------------------
			if (!homePage.listView.clickItemByIndex(0)) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Select Properties from operations menu

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("3. Metadatacard properties of latest version of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.",driver);

			//Step-4 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("4. Customer & Contact person is added to the metadatacard.",driver);

			//Step-5 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("5. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-6 : Clear the value to customer property
			//----------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, ""); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("6. Value from the customer property is cleared in metadatacard.",driver);

			//Step-7 : Add one more contact person to the metadatacard
			//--------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson2"), 2); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value, 2).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson2"));

			Log.message("7. New contactperson value (" + dataPool.get("ContactPerson2") + ") is added to the metadatacard.",driver);

			//Verification: Verify if Customer is not autofilled on adding second contact person
			//----------------------------------------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(""))
				Log.pass("Test case Passed. Customer property is not autofilled on adding two contact persons of two different customers in metadatacard opened through operations menu for latest version of an object in History view.",driver);
			else
				Log.fail("Test case Failed. Customer property is autofilled on adding two contact persons of two different customers in metadatacard opened through operations menu for latest version of an object in History view.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_7B

	/**
	 * 99.1.7C : Customer property should not be auto-filled in metadatacard opened through taskpanel menu on selecting two contact persons of two different customers for latest version of an object in History View
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99"}, 
			description = "Customer property should not be auto-filled in metadatacard opened through taskpanel menu on selecting two contact persons of two different customers for latest version of an object in History View.")
	public void SprintTest99_1_7C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Select Object and open its Histoy view
			//-----------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.History.Value); //Select Properties from context menu

			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened from taskpanel.",driver);

			//Step-3 : Open metadatacard properties from taskpanel menu
			//-------------------------------------------------------
			if (!homePage.listView.clickItemByIndex(0)) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Select Properties from taskpanel menu

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("3. Metadatacard properties of latest version of an object (" + dataPool.get("ObjectName") + ") is opened from taskpanel menu.",driver);

			//Step-4 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("4. Customer & Contact person is added to the metadatacard.",driver);

			//Step-5 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("5. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-6 : Clear the value to customer property
			//----------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, ""); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("6. Value from the customer property is cleared in metadatacard.",driver);

			//Step-7 : Add one more contact person to the metadatacard
			//--------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson2"), 2); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value, 2).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson2"));

			Log.message("7. New contactperson value (" + dataPool.get("ContactPerson2") + ") is added to the metadatacard.",driver);

			//Verification: Verify if Customer is not autofilled on adding second contact person
			//----------------------------------------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(""))
				Log.pass("Test case Passed. Customer property is not autofilled on adding two contact persons of two different customers in metadatacard opened through taskpanel menu for latest version of an object in History view.",driver);
			else
				Log.fail("Test case Failed. Customer property is autofilled on adding two contact persons of two different customers in metadatacard opened through taskpanel menu for latest version of an object in History view.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_7C

	/**
	 * 99.1.8.1A : Customer property should be auto-filled in metadatacard opened through context menu properties on removing any one contact person of two for latest version of an object in History view.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99","Bug"}, 
			description = "Customer property should be auto-filled in metadatacard opened through context menu properties on removing any one contact person of two for latest version of an object in History view.")
	public void SprintTest99_1_8_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Select Object and open its Histoy view
			//-----------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.History.Value); //Select Properties from context menu

			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened from taskpanel.",driver);

			//Step-3 : Open metadatacard properties from context menu
			//-------------------------------------------------------
			if (!homePage.listView.rightClickItemByIndex(0)) //Selects the object
				throw new Exception("Latest version of an object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Select Properties from context menu

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("3. Metadatacard properties of latest version of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.",driver);

			//Step-4 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("4. Customer & Contact person is added to the metadatacard.",driver);

			//Step-5 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("5. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-6 : Clear the value to customer property
			//----------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, ""); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("6. Value from the customer property is cleared in metadatacard.",driver);

			//Step-7 : Add one more contact person to the metadatacard
			//--------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson2"), 2); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value, 2).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson2"));

			Log.message("7. New contactperson value (" + dataPool.get("ContactPerson2") + ") is added to the metadatacard.",driver);

			//Step-8 : Clear the first value of contact person property
			//----------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, ""); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception("Value (" + dataPool.get("ContactPerson2") + ") from property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not cleared.");

			Log.message("8. First Value(" + dataPool.get("ContactPerson1") + ") is cleared for contact person property.",driver);

			//Verification: Verify if Customer of second contact person is autofilled 
			//-----------------------------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer2")))
				Log.pass("Test case Passed. Customer property is autofilled on clearing one of two contact persons of two different customers in metadatacard opened through context menu for latest version in History view.",driver);
			else
				Log.fail("Test case Failed. Customer property is not autofilled on clearing one of two contact persons of two different customers in metadatacard opened through context menu for latest version in History view.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_8_1A

	/**
	 * 99.1.8.1B : Customer property should be auto-filled in metadatacard opened through operations menu properties on removing any one contact person of two for latest version of an object in History view.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99","Bug"}, 
			description = "Customer property should be auto-filled in metadatacard opened through operations menu properties on removing any one contact person of two for latest version of an object in History view.")
	public void SprintTest99_1_8_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Select Object and open its Histoy view
			//-----------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.History.Value); //Select Properties from context menu

			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened from taskpanel.",driver);

			//Step-3 : Open metadatacard properties from operations menu
			//-------------------------------------------------------
			if (!homePage.listView.clickItemByIndex(0)) //Selects the object
				throw new Exception("Latest version of an object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Select Properties from operations menu

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("3. Metadatacard properties of latest version of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.",driver);

			//Step-4 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("4. Customer & Contact person is added to the metadatacard.",driver);

			//Step-5 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("5. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-6 : Clear the value to customer property
			//----------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, ""); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("6. Value from the customer property is cleared in metadatacard.",driver);

			//Step-7 : Add one more contact person to the metadatacard
			//--------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson2"), 2); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value, 2).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson2"));

			Log.message("7. New contactperson value (" + dataPool.get("ContactPerson2") + ") is added to the metadatacard.",driver);

			//Step-8 : Clear the first value of contact person property
			//----------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, ""); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception("Value (" + dataPool.get("ContactPerson2") + ") from property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not cleared.");

			Log.message("8. First Value(" + dataPool.get("ContactPerson1") + ") is cleared for contact person property.",driver);

			//Verification: Verify if Customer of second contact person is autofilled 
			//-----------------------------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer2")))
				Log.pass("Test case Passed. Customer property is autofilled on clearing one of two contact persons of two different customers in metadatacard opened through operations menu for latest version in History view.",driver);
			else
				Log.fail("Test case Failed. Customer property is not autofilled on clearing one of two contact persons of two different customers in metadatacard opened through operations menu for latest version in History view.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_8_1B

	/**
	 * 99.1.8.1C : Customer property should be auto-filled in metadatacard opened through taskpanel menu properties on removing any one contact person of two for latest version of an object in History view.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99","Bug"}, 
			description = "Customer property should be auto-filled in metadatacard opened through taskpanel menu properties on removing any one contact person of two for latest version of an object in History view.")
	public void SprintTest99_1_8_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Select Object and open its Histoy view
			//-----------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.History.Value); //Select Properties from context menu

			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened from taskpanel.",driver);

			//Step-3 : Open metadatacard properties from taskpanel menu
			//-------------------------------------------------------
			if (!homePage.listView.clickItemByIndex(0)) //Selects the object
				throw new Exception("Latest version of an object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Select Properties from taskpanel menu

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("3. Metadatacard properties of latest version of an object (" + dataPool.get("ObjectName") + ") is opened from taskpanel menu.",driver);

			//Step-4 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("4. Customer & Contact person is added to the metadatacard.",driver);

			//Step-5 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("5. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-6 : Clear the value to customer property
			//----------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, ""); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("6. Value from the customer property is cleared in metadatacard.",driver);

			//Step-7 : Add one more contact person to the metadatacard
			//--------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson2"), 2); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value, 2).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson2"));

			Log.message("7. New contactperson value (" + dataPool.get("ContactPerson2") + ") is added to the metadatacard.",driver);

			//Step-8 : Clear the first value of contact person property
			//----------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, ""); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception("Value (" + dataPool.get("ContactPerson2") + ") from property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not cleared.");

			Log.message("8. First Value(" + dataPool.get("ContactPerson1") + ") is cleared for contact person property.",driver);

			//Verification: Verify if Customer of second contact person is autofilled 
			//-----------------------------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer2")))
				Log.pass("Test case Passed. Customer property is autofilled on clearing one of two contact persons of two different customers in metadatacard opened through taskpanel menu for latest version in History view.",driver);
			else
				Log.fail("Test case Failed. Customer property is not autofilled on clearing one of two contact persons of two different customers in metadatacard opened through taskpanel menu for latest version in History view.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_8_1C

	/**
	 * 99.1.8.2A : Customer property should be auto-filled in metadatacard opened through context menu properties on removing any one contact person of two using '-' icon for latest version of an object in History view.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99","Bug"}, 
			description = "Customer property should be auto-filled in metadatacard opened through context menu properties on removing any one contact person of two using '-' icon for latest version of an object in History view.")
	public void SprintTest99_1_8_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Select Object and open its Histoy view
			//-----------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.History.Value); //Select Properties from context menu

			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened from taskpanel.",driver);

			//Step-3 : Open metadatacard properties from context menu
			//-------------------------------------------------------
			if (!homePage.listView.rightClickItemByIndex(0)) //Selects the object
				throw new Exception("Latest version of an object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Select Properties from context menu

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("3. Metadatacard properties of latest version of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.",driver);

			//Step-4 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("4. Customer & Contact person is added to the metadatacard.",driver);

			//Step-5 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("5. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-6 : Clear the value to customer property
			//----------------------------------------------
			metadatacard.removePropertyValue(Caption.ObjecTypes.Customer.Value); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("6. Value from the customer property is cleared in metadatacard using '-' icon.",driver);

			//Step-7 : Add one more contact person to the metadatacard
			//--------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson2"), 2); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value, 2).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson2"));

			Log.message("7. New contactperson value (" + dataPool.get("ContactPerson2") + ") is added to the metadatacard.",driver);

			//Step-8 : Clear the first value of contact person property
			//----------------------------------------------------------
			metadatacard.removePropertyValue(Caption.ObjecTypes.ContactPerson.Value); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception("Value (" + dataPool.get("ContactPerson2") + ") from property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not cleared.");

			Log.message("8. First Value(" + dataPool.get("ContactPerson1") + ") is cleared for contact person property using '-' icon.",driver);

			//Verification: Verify if Customer of second contact person is autofilled 
			//-----------------------------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer2")))
				Log.pass("Test case Passed. Customer property is autofilled on clearing one of two contact persons of two different customers using '-' icon in metadatacard opened through context menu for latest version in History view.",driver);
			else
				Log.fail("Test case Failed. Customer property is not autofilled on clearing one of two contact persons of two different customers using '-' icon in metadatacard opened through context menu for latest version in History view.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_8_2A

	/**
	 * 99.1.8.2B : Customer property should be auto-filled in metadatacard opened through operations menu properties on removing any one contact person of two using '-' icon for latest version of an object in History view.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99","Bug"}, 
			description = "Customer property should be auto-filled in metadatacard opened through operations menu properties on removing any one contact person of two using '-' icon for latest version of an object in History view.")
	public void SprintTest99_1_8_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {




			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Select Object and open its Histoy view
			//-----------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.History.Value); //Select Properties from context menu

			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened from taskpanel.",driver);

			//Step-3 : Open metadatacard properties from operations menu
			//-------------------------------------------------------
			if (!homePage.listView.clickItemByIndex(0)) //Selects the object
				throw new Exception("Latest version of an object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Select Properties from operations menu

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("3. Metadatacard properties of latest version of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.",driver);

			//Step-4 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("4. Customer & Contact person is added to the metadatacard.",driver);

			//Step-5 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("5. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-6 : Clear the value to customer property
			//----------------------------------------------
			metadatacard.removePropertyValue(Caption.ObjecTypes.Customer.Value); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("6. Value from the customer property is cleared in metadatacard using '-' icon.",driver);

			//Step-7 : Add one more contact person to the metadatacard
			//--------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson2"), 2); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value, 2).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson2"));

			Log.message("7. New contactperson value (" + dataPool.get("ContactPerson2") + ") is added to the metadatacard.",driver);

			//Step-8 : Clear the first value of contact person property
			//----------------------------------------------------------
			metadatacard.removePropertyValue(Caption.ObjecTypes.ContactPerson.Value); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception("Value (" + dataPool.get("ContactPerson2") + ") from property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not cleared.");

			Log.message("8. First Value(" + dataPool.get("ContactPerson1") + ") is cleared for contact person property using '-' icon.",driver);

			//Verification: Verify if Customer of second contact person is autofilled 
			//-----------------------------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer2")))
				Log.pass("Test case Passed. Customer property is autofilled on clearing one of two contact persons of two different customers using '-' icon in metadatacard opened through operations menu for latest version in History view.",driver);
			else
				Log.fail("Test case Failed. Customer property is not autofilled on clearing one of two contact persons of two different customers using '-' icon in metadatacard opened through operations menu for latest version in History view.", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_8_2B

	/**
	 * 99.1.8.2C : Customer property should be auto-filled in metadatacard opened through taskpanel menu properties on removing any one contact person of two using '-' icon for latest version of an object in History view.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99","Bug"}, 
			description = "Customer property should be auto-filled in metadatacard opened through taskpanel menu properties on removing any one contact person of two using '-' icon for latest version of an object in History view.")
	public void SprintTest99_1_8_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Select Object and open its Histoy view
			//-----------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.History.Value); //Select Properties from context menu

			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened from taskpanel.",driver);

			//Step-3 : Open metadatacard properties from taskpanel menu
			//-------------------------------------------------------
			if (!homePage.listView.clickItemByIndex(0)) //Selects the object
				throw new Exception("Latest version of an object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Select Properties from taskpanel menu

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("3. Metadatacard properties of latest version of an object (" + dataPool.get("ObjectName") + ") is opened from taskpanel menu.",driver);

			//Step-4 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("4. Customer & Contact person is added to the metadatacard.",driver);

			//Step-5 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("5. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-6 : Clear the value to customer property
			//----------------------------------------------
			metadatacard.removePropertyValue(Caption.ObjecTypes.Customer.Value); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("6. Value from the customer property is cleared in metadatacard using '-' icon.",driver);

			//Step-7 : Add one more contact person to the metadatacard
			//--------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson2"), 2); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value, 2).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson2"));

			Log.message("7. New contactperson value (" + dataPool.get("ContactPerson2") + ") is added to the metadatacard.",driver);

			//Step-8 : Clear the first value of contact person property
			//----------------------------------------------------------
			metadatacard.removePropertyValue(Caption.ObjecTypes.ContactPerson.Value); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception("Value (" + dataPool.get("ContactPerson2") + ") from property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not cleared.");

			Log.message("8. First Value(" + dataPool.get("ContactPerson1") + ") is cleared for contact person property using '-' icon.",driver);

			//Verification: Verify if Customer of second contact person is autofilled 
			//-----------------------------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer2")))
				Log.pass("Test case Passed. Customer property is autofilled on clearing one of two contact persons of two different customers using '-' icon in metadatacard opened through taskpanel menu for latest version in History view.",driver);
			else
				Log.fail("Test case Failed. Customer property is not autofilled on clearing one of two contact persons of two different customers using '-' icon in metadatacard opened through taskpanel menu for latest version in History view.", driver);

		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_8_2C

	/**
	 * 99.1.9.1A : Customer property should be autofilled in metadatacard opened through context menu properties on removing existing customer& contact person and adding new contact person for latest version of an object in History view.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99"}, 
			description = "Customer property should be autofilled in metadatacard opened through context menu properties on removing existing customer& contact person and adding new contact person for latest version of an object in History view.")
	public void SprintTest99_1_9_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Select Object and open its Histoy view
			//-----------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.History.Value); //Select Properties from context menu

			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened from taskpanel.",driver);

			//Step-3 : Open metadatacard properties from context menu
			//-------------------------------------------------------
			if (!homePage.listView.rightClickItemByIndex(0)) //Selects the object
				throw new Exception("Latest version of an object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Select Properties from context menu

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("3. Metadatacard properties of latest version of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.",driver);

			//Step-4 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("4. Customer & Contact person is added to the metadatacard.",driver);

			//Step-5 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("5. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-6 : Remove the value to customer property
			//----------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, ""); //Removes the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("5. Value from the customer property is removed in metadatacard.",driver);

			//Step-6 : Remove the value to contact person 
			//--------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, ""); //Removes the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not removed.");

			Log.message("6. Value from the contact person property is removed in metadatacard.",driver);

			//Step-7 : Set Value to the customer & contact person
			//---------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson2")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson2"));

			Log.message("7. Values for Contactperson (" + dataPool.get("ContactPerson2") + ") is added in the metadatacard.",driver);

			//Verification: Verify if Customer is autofilled
			//------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer2")))
				Log.pass("Test case Passed. Customer property is autofilled on adding contact person in metadatacard opened through context menu for latest version of an object in History view.",driver);
			else
				Log.fail("Test case Failed. Customer property is not autofilled on adding contact person in metadatacard opened through context menu for latest version of an object in History view.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_9_1A

	/**
	 * 99.1.9.1B : Customer property should be autofilled in metadatacard opened through operations menu properties on removing existing customer& contact person and adding new contact person for latest version of an object in History view.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99"}, 
			description = "Customer property should be autofilled in metadatacard opened through operations menu properties on removing existing customer& contact person and adding new contact person for latest version of an object in History view.")
	public void SprintTest99_1_9_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Select Object and open its Histoy view
			//-----------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.History.Value); //Select Properties from context menu

			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened from taskpanel.",driver);

			//Step-3 : Open metadatacard properties from operations menu
			//-------------------------------------------------------
			if (!homePage.listView.clickItemByIndex(0)) //Selects the object
				throw new Exception("Latest version of an object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Select Properties from operations menu

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("3. Metadatacard properties of latest version of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.",driver);

			//Step-4 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("4. Customer & Contact person is added to the metadatacard.",driver);

			//Step-5 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("5. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-6 : Remove the value to customer property
			//----------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, ""); //Removes the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("6. Value from the customer property is removed in metadatacard.",driver);

			//Step-6 : Remove the value to contact person 
			//--------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, ""); //Removes the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not removed.");

			Log.message("7. Value from the contact person property is removed in metadatacard.",driver);

			//Step-7 : Set Value to the customer & contact person
			//---------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson2")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson2"));

			Log.message("8. Values for Contactperson (" + dataPool.get("ContactPerson2") + ") is added in the metadatacard.",driver);

			//Verification: Verify if Customer is autofilled
			//------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer2")))
				Log.pass("Test case Passed. Customer property is autofilled on adding contact person in metadatacard opened through operations menu for latest version of an object in History view.",driver);
			else
				Log.fail("Test case Failed. Customer property is not autofilled on adding contact person in metadatacard opened through operations menu for latest version of an object in History view.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_9_1B

	/**
	 * 99.1.9.1C : Customer property should be autofilled in metadatacard opened through taskpanel menu properties on removing existing customer& contact person and adding new contact person for latest version of an object in History view.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99"}, 
			description = "Customer property should be autofilled in metadatacard opened through taskpanel menu properties on removing existing customer& contact person and adding new contact person for latest version of an object in History view.")
	public void SprintTest99_1_9_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Select Object and open its Histoy view
			//-----------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.History.Value); //Select Properties from context menu

			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened from taskpanel.",driver);

			//Step-3 : Open metadatacard properties from taskpanel menu
			//-------------------------------------------------------
			if (!homePage.listView.clickItemByIndex(0)) //Selects the object
				throw new Exception("Latest version of an object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Select Properties from taskpanel menu

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("3. Metadatacard properties of latest version of an object (" + dataPool.get("ObjectName") + ") is opened from taskpanel menu.",driver);

			//Step-4 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("4. Customer & Contact person is added to the metadatacard.",driver);

			//Step-5 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("5. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-6 : Remove the value to customer property
			//----------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, ""); //Removes the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("5. Value from the customer property is removed in metadatacard.",driver);

			//Step-6 : Remove the value to contact person 
			//--------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, ""); //Removes the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not removed.");

			Log.message("6. Value from the contact person property is removed in metadatacard.",driver);

			//Step-7 : Set Value to the customer & contact person
			//---------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson2")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson2"));

			Log.message("7. Values for Contactperson (" + dataPool.get("ContactPerson2") + ") is added in the metadatacard.",driver);

			//Verification: Verify if Customer is autofilled
			//------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer2")))
				Log.pass("Test case Passed. Customer property is autofilled on adding contact person in metadatacard opened through taskpanel menu for latest version of an object in History view.",driver);
			else
				Log.fail("Test case Failed. Customer property is not autofilled on adding contact person in metadatacard opened through taskpanel menu for latest version of an object in History view.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_9_1C

	/**
	 * 99.1.9.2A : Customer property should be autofilled in metadatacard opened through context menu properties on removing existing customer& contact person and adding new contact person for latest version of an object in History view.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99","Bug"}, 
			description = "Customer property should be autofilled in metadatacard opened through context menu properties on removing existing customer& contact person and adding new contact person for latest version of an object in History view.")
	public void SprintTest99_1_9_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Select Object and open its Histoy view
			//-----------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.History.Value); //Select Properties from context menu

			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened from taskpanel.",driver);

			//Step-3 : Open metadatacard properties from context menu
			//-------------------------------------------------------
			if (!homePage.listView.rightClickItemByIndex(0)) //Selects the object
				throw new Exception("Latest version of an object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Select Properties from context menu

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("3. Metadatacard properties of latest version of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.",driver);

			//Step-4 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("4. Customer & Contact person is added to the metadatacard.",driver);

			//Step-5 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("5. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-6 : Remove the value to customer property
			//----------------------------------------------
			metadatacard.removeProperty(Caption.ObjecTypes.Customer.Value); //Removes the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("5. Value from the customer property is removed in metadatacard.",driver);

			//Step-6 : Remove the value to contact person 
			//--------------------------------------------
			metadatacard.removeProperty(Caption.ObjecTypes.ContactPerson.Value); //Removes the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not removed.");

			Log.message("6. Value from the contact person property is removed in metadatacard.",driver);

			//Step-7 : Set Value to the customer & contact person
			//---------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson2")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson2"));

			Log.message("7. Values for Contactperson (" + dataPool.get("ContactPerson2") + ") is added in the metadatacard.",driver);

			//Verification: Verify if Customer is autofilled
			//------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer2")))
				Log.pass("Test case Passed. Customer property is autofilled on adding contact person in metadatacard opened through context menu for latest version of an object in History view.",driver);
			else
				Log.fail("Test case Failed. Customer property is not autofilled on adding contact person in metadatacard opened through context menu for latest version of an object in History view.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_9_2A

	/**
	 * 99.1.9.2B : Customer property should be autofilled in metadatacard opened through operations menu properties on removing existing customer& contact person and adding new contact person for latest version of an object in History view.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99","Bug"}, 
			description = "Customer property should be autofilled in metadatacard opened through operations menu properties on removing existing customer& contact person and adding new contact person for latest version of an object in History view.")
	public void SprintTest99_1_9_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Select Object and open its Histoy view
			//-----------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.History.Value); //Select Properties from context menu

			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened from taskpanel.",driver);

			//Step-3 : Open metadatacard properties from operations menu
			//-------------------------------------------------------
			if (!homePage.listView.clickItemByIndex(0)) //Selects the object
				throw new Exception("Latest version of an object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Select Properties from operations menu

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("3. Metadatacard properties of latest version of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.",driver);

			//Step-4 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("4. Customer & Contact person is added to the metadatacard.",driver);

			//Step-5 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("5. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-6 : Remove the value to customer property
			//----------------------------------------------
			metadatacard.removeProperty(Caption.ObjecTypes.Customer.Value); //Removes the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("6. Value from the customer property is removed in metadatacard.",driver);

			//Step-6 : Remove the value to contact person 
			//--------------------------------------------
			metadatacard.removeProperty(Caption.ObjecTypes.ContactPerson.Value); //Removes the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not removed.");

			Log.message("6. Value from the contact person property is removed in metadatacard.",driver);

			//Step-7 : Set Value to the customer & contact person
			//---------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson2")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson2"));

			Log.message("7. Values for Contactperson (" + dataPool.get("ContactPerson2") + ") is added in the metadatacard.",driver);

			//Verification: Verify if Customer is autofilled
			//------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer2")))
				Log.pass("Test case Passed. Customer property is autofilled on adding contact person in metadatacard opened through operations menu for latest version of an object in History view.",driver);
			else
				Log.fail("Test case Failed. Customer property is not autofilled on adding contact person in metadatacard opened through operations menu for latest version of an object in History view.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_9_2B

	/**
	 * 99.1.9.2C : Customer property should be autofilled in metadatacard opened through taskpanel menu properties on removing existing customer& contact person and adding new contact person for latest version of an object in History view.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99","Bug"}, 
			description = "Customer property should be autofilled in metadatacard opened through taskpanel menu properties on removing existing customer& contact person and adding new contact person for latest version of an object in History view.")
	public void SprintTest99_1_9_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Select Object and open its Histoy view
			//-----------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.History.Value); //Select Properties from context menu

			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened from taskpanel.",driver);

			//Step-3 : Open metadatacard properties from taskpanel menu
			//-------------------------------------------------------
			if (!homePage.listView.clickItemByIndex(0)) //Selects the object
				throw new Exception("Latest version of an object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Select Properties from taskpanel menu

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("3. Metadatacard properties of latest version of an object (" + dataPool.get("ObjectName") + ") is opened from taskpanel menu.",driver);

			//Step-4 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("4. Customer & Contact person is added to the metadatacard.",driver);

			//Step-5 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("5. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-6 : Remove the value to customer property
			//----------------------------------------------
			metadatacard.removeProperty(Caption.ObjecTypes.Customer.Value); //Removes the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("6. Value from the customer property is removed in metadatacard.",driver);

			//Step-6 : Remove the value to contact person 
			//--------------------------------------------
			metadatacard.removeProperty(Caption.ObjecTypes.ContactPerson.Value); //Removes the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not removed.");

			Log.message("7. Value from the contact person property is removed in metadatacard.",driver);

			//Step-7 : Set Value to the customer & contact person
			//---------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson2")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson2"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson2"));

			Log.message("8. Values for Contactperson (" + dataPool.get("ContactPerson2") + ") is added in the metadatacard.",driver);

			//Verification: Verify if Customer is autofilled
			//------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer2")))
				Log.pass("Test case Passed. Customer property is autofilled on adding contact person in metadatacard opened through taskpanel menu for latest version of an object in History view.",driver);
			else
				Log.fail("Test case Failed. Customer property is not autofilled on adding contact person in metadatacard opened through taskpanel menu for latest version of an object in History view.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_9_2C

	/**
	 * 99.1.13.1A : Removing customer should not remove the contact person in metadatacard properties opened through context menu.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99"}, 
			description = "Removing customer should not remove the contact person in metadatacard properties opened through context menu.")
	public void SprintTest99_1_13_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Open metadatacard properties from context menu
			//-------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Select Properties from context menu

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Metadatacard properties of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.",driver);

			//Step-3 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("3. Customer & Contact person is added to the metadatacard.",driver);

			//Step-4 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("4. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-5 : Remove the value to customer property 
			//-----------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, ""); //Removes the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("5. Value from the customer property is removed in metadatacard.",driver);

			//Verification: Verify if Contact person value is not removed
			//-----------------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1")))
				Log.pass("Test case Passed. Contact person value is not removed on removing customer value in metadatacard opened through context menu.",driver);
			else
				Log.fail("Test case Failed. Contact person value is removed on removing customer value in metadatacard opened through context menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_13_1A

	/**
	 * 99.1.13.1B : Removing customer should not remove the contact person in metadatacard properties opened through operations menu.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99", "QuickReg"}, 
			description = "Removing customer should not remove the contact person in metadatacard properties opened through operations menu.")
	public void SprintTest99_1_13_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Open metadatacard properties from operations menu
			//-------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Select Properties from operations menu

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Metadatacard properties of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.",driver);

			//Step-3 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("3. Customer & Contact person is added to the metadatacard.",driver);

			//Step-4 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("4. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-5 : Remove the value to customer property 
			//-----------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, ""); //Removes the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("5. Value from the customer property is removed in metadatacard.",driver);

			//Verification: Verify if Contact person value is not removed
			//-----------------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1")))
				Log.pass("Test case Passed. Contact person value is not removed on removing customer value in metadatacard opened through operations menu.",driver);
			else
				Log.fail("Test case Failed. Customer property is not autofilled on adding contact person in metadatacard opened through operations menu..", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_13_1B

	/**
	 * 99.1.13.1C : Removing customer should not remove the contact person in metadatacard properties opened through taskpanel menu.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99"}, 
			description = "Removing customer should not remove the contact person in metadatacard properties opened through taskpanel menu.")
	public void SprintTest99_1_13_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {			



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Open metadatacard properties from taskpanel menu
			//-------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Select Properties from taskpanel menu

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Metadatacard properties of an object (" + dataPool.get("ObjectName") + ") is opened from taskpanel menu.",driver);

			//Step-3 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("3. Customer & Contact person is added to the metadatacard.",driver);

			//Step-4 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("4. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-5 : Remove the value to customer property 
			//-----------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, ""); //Removes the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("5. Value from the customer property is removed in metadatacard.",driver);

			//Verification: Verify if Contact person value is not removed
			//-----------------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1")))
				Log.pass("Test case Passed. Contact person value is not removed on removing customer value in metadatacard opened through taskpanel menu.",driver);
			else
				Log.fail("Test case Failed. Customer property is not autofilled on adding contact person in metadatacard opened through taskpanel menu..", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_13_1C

	/**
	 * 99.1.13.1D : Removing customer should not remove the contact person in metadatacard properties opened in rightpane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99"}, 
			description = "Removing customer should not remove the contact person in metadatacard properties opened in rightpane.")
	public void SprintTest99_1_13_1D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Open metadatacard properties from rightpane
			//-------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is selected.");

			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("2. Metadatacard properties of an object (" + dataPool.get("ObjectName") + ") is opened in rightpane.",driver);

			//Step-3 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("3. Customer & Contact person is added to the metadatacard.",driver);

			//Step-4 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("4. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-5 : Remove the value to customer property 
			//-----------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, ""); //Removes the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("5. Value from the customer property is removed in metadatacard.",driver);

			//Verification: Verify if Contact person value is not removed
			//-----------------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1")))
				Log.pass("Test case Passed. Contact person value is not removed on removing customer value in metadatacard opened in rightpane.",driver);
			else
				Log.fail("Test case Failed. Customer property is not autofilled on adding contact person in metadatacard opened in rightpane.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_13_1D

	/**
	 * 99.1.13.2A : Removing customer using '-' icon should not remove the contact person in metadatacard properties opened through context menu.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99"}, 
			description = "Removing customer using '-' icon should not remove the contact person in metadatacard properties opened through context menu.")
	public void SprintTest99_1_13_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Open metadatacard properties from context menu
			//-------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Select Properties from context menu

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Metadatacard properties of an object (" + dataPool.get("ObjectName") + ") is opened from context menu.",driver);

			//Step-3 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("3. Customer & Contact person is added to the metadatacard.",driver);

			//Step-4 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("4. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-5 : Remove the value to customer property 
			//-----------------------------------------------------------
			metadatacard.removePropertyValue(Caption.ObjecTypes.Customer.Value); //Removes the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("5. Value from the customer property is removed in metadatacard using '-' icon",driver);

			//Verification: Verify if Contact person value is not removed
			//-----------------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1")))
				Log.pass("Test case Passed. Contact person value in is not removed on removing customer using '-' icon value in metadatacard opened through context menu.",driver);
			else
				Log.fail("Test case Failed. Contact person value in is removed on removing customer using '-' icon value in metadatacard opened through context menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_13_2A

	/**
	 * 99.1.13.2B : Removing customer using '-' icon should not remove the contact person in metadatacard properties opened through operations menu.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99"}, 
			description = "Removing customer using '-' icon should not remove the contact person in metadatacard properties opened through operations menu.")
	public void SprintTest99_1_13_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Open metadatacard properties from operations menu
			//-------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Select Properties from operations menu

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Metadatacard properties of an object (" + dataPool.get("ObjectName") + ") is opened from operations menu.",driver);

			//Step-3 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("3. Customer & Contact person is added to the metadatacard.",driver);

			//Step-4 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("4. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-5 : Remove the value to customer property 
			//-----------------------------------------------------------
			metadatacard.removePropertyValue(Caption.ObjecTypes.Customer.Value); //Removes the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("5. Value from the customer property is removed in metadatacard using '-' icon",driver);

			//Verification: Verify if Contact person value is not removed
			//-----------------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1")))
				Log.pass("Test case Passed. Contact person value in is not removed on removing customer using '-' icon value in metadatacard opened through operations menu.",driver);
			else
				Log.fail("Test case Failed. Customer property is not autofilled on adding contact person in metadatacard opened through operations menu..", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_13_2B

	/**
	 * 99.1.13.2C : Removing customer using '-' icon should not remove the contact person in metadatacard properties opened through taskpanel menu.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99", "QuickReg"}, 
			description = "Removing customer using '-' icon should not remove the contact person in metadatacard properties opened through taskpanel menu.")
	public void SprintTest99_1_13_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Open metadatacard properties from taskpanel menu
			//-------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Select Properties from taskpanel menu

			MetadataCard metadatacard = new MetadataCard(driver);

			Log.message("2. Metadatacard properties of an object (" + dataPool.get("ObjectName") + ") is opened from taskpanel menu.",driver);

			//Step-3 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("3. Customer & Contact person is added to the metadatacard.",driver);

			//Step-4 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("4. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-5 : Remove the value to customer property 
			//-----------------------------------------------------------
			metadatacard.removePropertyValue(Caption.ObjecTypes.Customer.Value); //Removes the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("5. Value from the customer property is removed in metadatacard using '-' icon",driver);

			//Verification: Verify if Contact person value is not removed
			//-----------------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1")))
				Log.pass("Test case Passed. Contact person value in is not removed on removing customer using '-' icon value in metadatacard opened through taskpanel menu.",driver);
			else
				Log.fail("Test case Failed. Customer property is not autofilled on adding contact person in metadatacard opened through taskpanel menu..", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_13_2C

	/**
	 * 99.1.13.2D : Removing customer using '-' icon should not remove the contact person in metadatacard properties opened in rightpane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint99"}, 
			description = "Removing customer using '-' icon should not remove the contact person in metadatacard properties opened in rightpane.")
	public void SprintTest99_1_13_2D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.",driver);

			//Step-2 : Open metadatacard properties from rightpane
			//-------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Checks if item exists in the list
				throw new SkipException("Invalid Test Data. Object (" + dataPool.get("ObjectName") + ") is not found in the vault");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is selected.");

			MetadataCard metadatacard = new MetadataCard(driver, true);

			Log.message("2. Metadatacard properties of an object (" + dataPool.get("ObjectName") + ") is opened in rightpane.",driver);

			//Step-3 : Add Customer and Contact person Property to the metadatacard
			//----------------------------------------------------------------------
			if (!metadatacard.addNewProperty(Caption.ObjecTypes.Customer.Value)) //Checks if Customer is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.Customer.Value + ") is not added to the metadatacard.");

			if (!metadatacard.addNewProperty(Caption.ObjecTypes.ContactPerson.Value)) //Checks if Contactperson is added to the metadatacard
				throw new Exception("Property (" + Caption.ObjecTypes.ContactPerson.Value + ") is not added to the metadatacard.");

			Log.message("3. Customer & Contact person is added to the metadatacard.",driver);

			//Step-4 : Set Value to the customer & contact person
			//------------------------------------------------------
			metadatacard.setPropertyValue(Caption.ObjecTypes.Customer.Value, dataPool.get("Customer1")); //Sets the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase(dataPool.get("Customer1"))) //Checks if Customer is set with the value
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not set with value " + dataPool.get("Customer1"));

			metadatacard.setPropertyValue(Caption.ObjecTypes.ContactPerson.Value, dataPool.get("ContactPerson1")); //Sets the contact person value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1"))) //Checks with Contact person is set with value
				throw new Exception(Caption.ObjecTypes.ContactPerson.Value + " is not set with value " + dataPool.get("ContactPerson1"));

			Log.message("4. Values for Customer (" + dataPool.get("Customer1") + ") & Contactperson (" + dataPool.get("ContactPerson1") + ") are added in the metadatacard.",driver);

			//Step-5 : Remove the value to customer property 
			//-----------------------------------------------------------
			metadatacard.removePropertyValue(Caption.ObjecTypes.Customer.Value); //Removes the customer value

			if (!metadatacard.getPropertyValue(Caption.ObjecTypes.Customer.Value).equalsIgnoreCase("")) //Checks if Customer value is remvoed
				throw new Exception(Caption.ObjecTypes.Customer.Value + " is not removed.");

			Log.message("5. Value from the customer property is removed in metadatacard using '-' icon",driver);

			//Verification: Verify if Contact person value is not removed
			//-----------------------------------------------------------
			if (metadatacard.getPropertyValue(Caption.ObjecTypes.ContactPerson.Value).equalsIgnoreCase(dataPool.get("ContactPerson1")))
				Log.pass("Test case Passed. Contact person value in is not removed on removing customer using '-' icon value in metadatacard opened in rightpane.",driver);
			else
				Log.fail("Test case Failed. Customer property is not autofilled on adding contact person in metadatacard opened in rightpane.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest99_1_13_2D

} //End Class AutoFillPropertyValues
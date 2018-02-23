package MFClient.Tests.Assignments;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.ExtentReporter;
import genericLibrary.Log;
import genericLibrary.Utils;
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
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class BasicMultiSelect {

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
			password = xmlParameters.getParameter("Password");
			testVault = xmlParameters.getParameter("VaultName");			
			userFullName = xmlParameters.getParameter("UserFullName");
			className = this.getClass().getSimpleName().toString().trim();
			if (xmlParameters.getParameter("driverType").equalsIgnoreCase("IE"))
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim() + xmlParameters.getParameter("driverVersion").trim();
			else
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim();

			if (xmlParameters.getParameter("driverType").equalsIgnoreCase("IE") || xmlParameters.getParameter("driverType").equalsIgnoreCase("SAFARI")){
				Log.testCaseInfo(xmlParameters.getParameter("driverType").toUpperCase() + " driver does not supports Multi-select.", className, className, productVersion);
				ExtentReporter.skip(xmlParameters.getParameter("driverType").toUpperCase() + " driver does not supports Multi-select.");
				ExtentReporter.endTest();		
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
	 * 104.1.36A : Verify the 'Assigned to' property of an assignment that is assigned to multiple users - Context menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint104"}, 
			description = "Verify the 'Assigned to' property of an assignment that is assigned to multiple users - Context menu Properties")
	public void SprintTest104_1_36A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			int rand = Integer.parseInt(dataPool.get("NoOfAssignments"));
			String[] assignName = new String[rand];
			String[] assignUsers = dataPool.get("UserNames").split(",");
			String objName = Utility.getObjectName(methodName).toString();

			for (int i=0; i<rand; i++) {
				homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
				assignName[i] =  objName + "_" + i; //Name of the object with current method date & time
				MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
				metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
				metadatacard.setPropertyValue("Name or title", assignName[i]); //Sets the Assignment nam
				Utils.fluentWait(driver);
				metadatacard.setPropertyValue("Assigned to", assignUsers[i]); //Sets the assigned to property
				metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			}

			Log.message("1. New " + rand + "Assignments are created.", driver);

			//Step-2 : Open the Properties dialog of the new assignments in context menu
			//------------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyAssignments.Value, objName);
			homePage.listView.shiftclickMultipleItemsByIndex(0, rand-1); //Select multiple items by shift key
			homePage.listView.rightClickItemByIndex(rand-1); //Right clicks the item
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. Properties dialog of multi selected assignments with various assigned to users is opened through context menu.");

			//Verification : Verifies if assigned to property has value varies
			//----------------------------------------------------------------
			if (metadatacard.getPropertyValue("Assigned to").toUpperCase().contains("VARIES")) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Varies text is displayed in Assigned to property on selecting assignments with various assigned to property.");
			else
				Log.fail("Test case Failed. Varies text is displayed in Assigned to property on selecting assignments with various assigned to property..", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_36A

	/**
	 * 104.1.36B : Verify the 'Assigned to' property of an assignment that is assigned to multiple users - Operations menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect",  "Sprint104"}, 
			description = "Verify the 'Assigned to' property of an assignment that is assigned to multiple users - Operations menu Properties")
	public void SprintTest104_1_36B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			int rand = Integer.parseInt(dataPool.get("NoOfAssignments"));
			String[] assignName = new String[rand];
			String objName = Utility.getObjectName(methodName).toString();
			String[] assignUsers = dataPool.get("UserNames").split(",");

			for (int i=0; i<rand; i++) {
				homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
				assignName[i] =  objName + "_" + i; //Name of the object with current method date & time
				MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
				metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
				metadatacard.setPropertyValue("Name or title", assignName[i]); //Sets the Assignment name
				Utils.fluentWait(driver);
				metadatacard.setPropertyValue("Assigned to", assignUsers[i]); //Sets the assigned to property
				metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			}

			Log.message("1. New " + rand + "Assignments are created.", driver);

			//Step-2 : Open the Properties dialog of the new assignments in context menu
			//------------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyAssignments.Value, objName);
			homePage.listView.shiftclickMultipleItemsByIndex(0, rand-1); //Select multiple items by shift key
			//homePage.listView.clickItemByIndex(rand-1); //Right clicks the item
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. Properties dialog of multi selected assignments with various assigned to users is opened through operations menu.");

			//Verification : Verifies if assigned to property has value varies
			//----------------------------------------------------------------
			if (metadatacard.getPropertyValue("Assigned to").toUpperCase().contains("VARIES")) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Varies text is displayed in Assigned to property on selecting assignments with various assigned to property.");
			else
				Log.fail("Test case Failed. Varies text is displayed in Assigned to property on selecting assignments with various assigned to property..", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_36B

	/**
	 * 104.1.36C : Verify the 'Assigned to' property of an assignment that is assigned to multiple users - Taskpanel Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect",  "Sprint104"}, 
			description = "Verify the 'Assigned to' property of an assignment that is assigned to multiple users - Taskpanel Properties")
	public void SprintTest104_1_36C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			int rand = Integer.parseInt(dataPool.get("NoOfAssignments"));
			String[] assignName = new String[rand];
			String objName = Utility.getObjectName(methodName).toString();
			String[] assignUsers = dataPool.get("UserNames").split(",");

			for (int i=0; i<rand; i++) {
				homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
				assignName[i] =  objName + "_" + i; //Name of the object with current method date & time
				MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
				metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
				metadatacard.setPropertyValue("Name or title", assignName[i]); //Sets the Assignment name
				Utils.fluentWait(driver);
				metadatacard.setPropertyValue("Assigned to", assignUsers[i]); //Sets the assigned to property
				metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			}

			Log.message("1. New " + rand + "Assignments are created.", driver);

			//Step-2 : Open the Properties dialog of the new assignments in context menu
			//------------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyAssignments.Value, objName);
			homePage.listView.shiftclickMultipleItemsByIndex(0, rand-1); //Select multiple items by shift key
			//homePage.listView.clickItemByIndex(rand-1); //Right clicks the item
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from taskpanel

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. Properties dialog of multi selected assignments with various assigned to users is opened through Taskpanel.");

			//Verification : Verifies if assigned to property has value varies
			//----------------------------------------------------------------
			if (metadatacard.getPropertyValue("Assigned to").toUpperCase().contains("VARIES")) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Varies text is displayed in Assigned to property on selecting assignments with various assigned to property.");
			else
				Log.fail("Test case Failed. Varies text is displayed in Assigned to property on selecting assignments with various assigned to property..", driver);



		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_36C

	/**
	 * 104.1.36D : Verify the 'Assigned to' property of an assignment that is assigned to multiple users - Rightpane Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect",  "Sprint104"}, 
			description = "Verify the 'Assigned to' property of an assignment that is assigned to multiple users - Rightpane Properties")
	public void SprintTest104_1_36D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			int rand = Integer.parseInt(dataPool.get("NoOfAssignments"));
			String[] assignName = new String[rand];
			String objName = Utility.getObjectName(methodName).toString();
			String[] assignUsers = dataPool.get("UserNames").split(",");

			for (int i=0; i<rand; i++) {
				homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
				assignName[i] =  objName + "_" + i; //Name of the object with current method date & time
				MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
				metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
				metadatacard.setPropertyValue("Name or title", assignName[i]); //Sets the Assignment name
				Utils.fluentWait(driver);
				metadatacard.setPropertyValue("Assigned to", assignUsers[i]); //Sets the assigned to property
				metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			}

			Log.message("1. New " + rand + "Assignments are created.", driver);

			//Step-2 : Open the Properties dialog of the new assignments in context menu
			//------------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyAssignments.Value, objName);
			homePage.listView.shiftclickMultipleItemsByIndex(0, rand-1); //Select multiple items by shift key
			homePage.listView.rightClickItemByIndex(rand-1); //Right clicks the item

			MetadataCard metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("2. Properties dialog of multi selected assignment with various assigned to users is opened through rightpane.");

			//Verification : Verifies if assigned to property has value varies
			//----------------------------------------------------------------
			if (metadatacard.getPropertyValue("Assigned to").toUpperCase().contains("VARIES")) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Varies text is displayed in Assigned to property on selecting assignments with various assigned to property.");
			else
				Log.fail("Test case Failed. Varies text is displayed in Assigned to property on selecting assignments with various assigned to property..", driver);



		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_36D

	/**
	 * 104.1.37A : Verify Assignee and varies text is displayed for multiselected assignment which has same&different user - Context menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect",  "Sprint104"}, 
			description = "Verify Assignee and varies text is displayed for multiselected assignment which has same&different user - Context menu Properties")
	public void SprintTest104_1_37A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName1 =  Utility.getObjectName(methodName).toString(); //Name of the object with current method date & time
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName1); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName1")); //Sets the assigned to property
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName2"), 2); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment (" + assigName1 + ") is created with two assigned to users.", driver);

			//Step-3 : Create an assignment with one assigned to user
			//-------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName2 =  Utility.getObjectName(methodName).toString(); //Name of the object with current method date & time
			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName2); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName1")); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Assignment(" + assigName2 + ")  is created with one assigned to user.", driver);

			//Step-4 : Open the Properties dialog of the assignment with various mutlipe assigned to users through context menu
			//-----------------------------------------------------------------------------------------------------------------
			String assignName = assigName1 + "\n" + assigName2;
			homePage.listView.clickMultipleItems(assignName);
			homePage.listView.rightClickItem(assigName2);
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of multi selected assignments with various assigned to users is opened through context menu.");

			//Verification : Verifies if assigned to property has value varies
			//----------------------------------------------------------------
			String propValue = metadatacard.getPropertyValue("Assigned to") + "," + metadatacard.getPropertyValue("Assigned to", 2);

			if (propValue.toUpperCase().contains("VARIES") && propValue.toUpperCase().contains(dataPool.get("UserFullName1").toUpperCase())) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Varies text & Assigned user is displayed in Assigned to property on selecting assignments with same and various assigned to property.");
			else
				Log.fail("Test case Failed. Varies text & Assigned user is not displayed in Assigned to property on selecting assignments with same and various assigned to property. Assigned to Users : " + propValue, driver);



		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_37A

	/**
	 * 104.1.37B : Verify Assignee and varies text is displayed for multiselected assignment which has same&different user - Operations menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect",  "Sprint104"}, 
			description = "Verify Assignee and varies text is displayed for multiselected assignment which has same&different user - Operations menu Properties")
	public void SprintTest104_1_37B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName1 =  Utility.getObjectName(methodName).toString(); //Name of the object with current method date & time
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName1); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName1")); //Sets the assigned to property
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName2"), 2); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment (" + assigName1 + ") is created with two assigned to users.", driver);

			//Step-3 : Create an assignment with one assigned to user
			//-------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName2 =  Utility.getObjectName(methodName).toString(); //Name of the object with current method date & time
			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName2); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName1")); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Assignment(" + assigName2 + ")  is created with one assigned to user.", driver);

			//Step-4 : Open the Properties dialog of the assignment with various mutlipe assigned to users through operations menu
			//-----------------------------------------------------------------------------------------------------------------
			String assignName = assigName1 + "\n" + assigName2;
			homePage.listView.clickMultipleItems(assignName);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from operations menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of multi selected assignments with various assigned to users is opened through operations menu.");

			//Verification : Verifies if assigned to property has value varies
			//----------------------------------------------------------------
			String propValue = metadatacard.getPropertyValue("Assigned to") + "," + metadatacard.getPropertyValue("Assigned to", 2);

			if (propValue.toUpperCase().contains("VARIES") && propValue.toUpperCase().contains(dataPool.get("UserFullName1").toUpperCase())) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Varies text & Assigned user is displayed in Assigned to property on selecting assignments with same and various assigned to property.");
			else
				Log.fail("Test case Failed. Varies text & Assigned user is not displayed in Assigned to property on selecting assignments with same and various assigned to property.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_37B

	/**
	 * 104.1.37C : Verify Assignee and varies text is displayed for multiselected assignment which has same&different user - Taskpanel Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint104"}, 
			description = "Verify Assignee and varies text is displayed for multiselected assignment which has same&different user - Taskpanel Properties")
	public void SprintTest104_1_37C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName1 =  Utility.getObjectName(methodName).toString(); //Name of the object with current method date & time
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName1); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName1")); //Sets the assigned to property
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName2"), 2); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment (" + assigName1 + ") is created with two assigned to users.", driver);

			//Step-3 : Create an assignment with one assigned to user
			//-------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName2 =  Utility.getObjectName(methodName).toString(); //Name of the object with current method date & time
			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName2); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName1")); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Assignment(" + assigName2 + ")  is created with one assigned to user.", driver);

			//Step-4 : Open the Properties dialog of the assignment with various mutlipe assigned to users through taskpanel menu
			//-----------------------------------------------------------------------------------------------------------------
			String assignName = assigName1 + "\n" + assigName2;
			homePage.listView.clickMultipleItems(assignName);
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from taskpanel menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of multi selected assignments with various assigned to users is opened through taskpanel.");

			//Verification : Verifies if assigned to property has value varies
			//----------------------------------------------------------------
			String propValue = metadatacard.getPropertyValue("Assigned to") + "," + metadatacard.getPropertyValue("Assigned to", 2);

			if (propValue.toUpperCase().contains("VARIES") && propValue.toUpperCase().contains(dataPool.get("UserFullName1").toUpperCase())) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Varies text & Assigned user is displayed in Assigned to property on selecting assignments with same and various assigned to property.");
			else
				Log.fail("Test case Failed. Varies text & Assigned user is not displayed in Assigned to property on selecting assignments with same and various assigned to property.", driver);



		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_37C

	/**
	 * 104.1.37D : Verify Assignee and varies text is displayed for multiselected assignment which has same&different user - Rightpane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint104"}, 
			description = "Verify Assignee and varies text is displayed for multiselected assignment which has same&different user - Rightpane")
	public void SprintTest104_1_37D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName1 =  Utility.getObjectName(methodName).toString(); //Name of the object with current method date & time
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName1); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName1")); //Sets the assigned to property
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName2"), 2); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment (" + assigName1 + ") is created with two assigned to users.", driver);

			//Step-3 : Create an assignment with one assigned to user
			//-------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName2 =  Utility.getObjectName(methodName).toString(); //Name of the object with current method date & time
			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName2); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName1")); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Assignment(" + assigName2 + ")  is created with one assigned to user.", driver);

			//Step-4 : Open the Properties dialog of the assignment with various mutlipe assigned to users through rightpane
			//-----------------------------------------------------------------------------------------------------------------
			String assignName = assigName1 + "\n" + assigName2;
			homePage.listView.clickMultipleItems(assignName);
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of multi selected assignments with various assigned to users is opened through rightpane.");

			//Verification : Verifies if assigned to property has value varies
			//----------------------------------------------------------------
			String propValue = metadatacard.getPropertyValue("Assigned to") + "," + metadatacard.getPropertyValue("Assigned to", 2);

			if (propValue.toUpperCase().contains("VARIES") && propValue.toUpperCase().contains(dataPool.get("UserFullName1").toUpperCase())) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Varies text & Assigned user is displayed in Assigned to property on selecting assignments with same and various assigned to property.");
			else
				Log.fail("Test case Failed. Varies text & Assigned user is not displayed in Assigned to property on selecting assignments with same and various assigned to property.", driver);



		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_37D

	/**
	 * 104.1.38A : Verify Approve & Reject icon for multiselected assignment which has same and different assignee - Context menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Verify Approve & Reject icon for multiselected assignment which has same and different assignee - Context menu Properties")
	public void SprintTest104_1_38A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName1 =  Utility.getObjectName(methodName).toString(); //Name of the object with current method date & time
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName1); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName1")); //Sets the assigned to property
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName2"), 2); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment (" + assigName1 + ") is created with two assigned to users.", driver);

			//Step-3 : Create an assignment with one assigned to user
			//-------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName2 =  Utility.getObjectName(methodName).toString(); //Name of the object with current method date & time
			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName2); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName1")); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Assignment(" + assigName2 + ")  is created with one assigned to user.", driver);

			//Step-4 : Open the Properties dialog of the assignment with various mutlipe assigned to users through context menu
			//-----------------------------------------------------------------------------------------------------------------
			String assignName = assigName1 + "\n" + assigName2;
			homePage.listView.clickMultipleItems(assignName);
			homePage.listView.rightClickItem(assigName2);
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of multi selected assignments with various assigned to users is opened through context menu.");

			//Verification : Verifies if assigned to property has value varies
			//----------------------------------------------------------------
			if (metadatacard.isNotApprovedDisplayed(0) && metadatacard.isNotApprovedDisplayed(1) && metadatacard.isNotRejectedDisplayed(0) && metadatacard.isNotRejectedDisplayed(1)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Approved and Rejected icons are displayed on selecting assignments with same and various assigned to property.");
			else
				Log.fail("Test case Failed. Approved and Rejected icons are not displayed on selecting assignments with same and various assigned to property.", driver);



		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_38A

	/**
	 * 104.1.38B : Verify Approve & Reject icon for multiselected assignment which has same and different assignee - Operations menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Verify Approve & Reject icon for multiselected assignment which has same and different assignee - Operations menu Properties")
	public void SprintTest104_1_38B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName1 =  Utility.getObjectName(methodName).toString(); //Name of the object with current method date & time
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName1); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName1")); //Sets the assigned to property
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName2"), 2); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment (" + assigName1 + ") is created with two assigned to users.", driver);

			//Step-3 : Create an assignment with one assigned to user
			//-------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName2 =  Utility.getObjectName(methodName).toString(); //Name of the object with current method date & time
			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName2); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName1")); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Assignment(" + assigName2 + ")  is created with one assigned to user.", driver);

			//Step-4 : Open the Properties dialog of the assignment with various mutlipe assigned to users through operations menu
			//-----------------------------------------------------------------------------------------------------------------
			String assignName = assigName1 + "\n" + assigName2;
			homePage.listView.clickMultipleItems(assignName);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from operations menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of multi selected assignments with various assigned to users is opened through operations menu.");

			//Verification : Verifies if assigned to property has value varies
			//----------------------------------------------------------------
			if (metadatacard.isNotApprovedDisplayed(0) && metadatacard.isNotApprovedDisplayed(1) && metadatacard.isNotRejectedDisplayed(0) && metadatacard.isNotRejectedDisplayed(1)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Approved and Rejected icons are displayed on selecting assignments with same and various assigned to property.");
			else
				Log.fail("Test case Failed. Approved and Rejected icons are not displayed on selecting assignments with same and various assigned to property.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_38B

	/**
	 * 104.1.38C : Verify Approve & Reject icon for multiselected assignment which has same and different assignee - Taskpanel Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Verify Approve & Reject icon for multiselected assignment which has same and different assignee - Taskpanel Properties")
	public void SprintTest104_1_38C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName1 =  Utility.getObjectName(methodName).toString(); //Name of the object with current method date & time
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName1); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName1")); //Sets the assigned to property
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName2"), 2); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment (" + assigName1 + ") is created with two assigned to users.", driver);

			//Step-3 : Create an assignment with one assigned to user
			//-------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName2 =  Utility.getObjectName(methodName).toString(); //Name of the object with current method date & time
			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName2); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName1")); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Assignment(" + assigName2 + ")  is created with one assigned to user.", driver);

			//Step-4 : Open the Properties dialog of the assignment with various mutlipe assigned to users through taskpanel menu
			//-----------------------------------------------------------------------------------------------------------------
			String assignName = assigName1 + "\n" + assigName2;
			homePage.listView.clickMultipleItems(assignName);
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from taskpanel menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of multi selected assignments with various assigned to users is opened through taskpanel menu.");

			//Verification : Verifies if assigned to property has value varies
			//----------------------------------------------------------------
			if (metadatacard.isNotApprovedDisplayed(0) && metadatacard.isNotApprovedDisplayed(1) && metadatacard.isNotRejectedDisplayed(0) && metadatacard.isNotRejectedDisplayed(1)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Approved and Rejected icons are displayed on selecting assignments with same and various assigned to property.");
			else
				Log.fail("Test case Failed. Approved and Rejected icons are not displayed on selecting assignments with same and various assigned to property.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_38C

	/**
	 * 104.1.38D : Verify Approve & Reject icon for multiselected assignment which has same and different assignee - Rightpane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Verify Approve & Reject icon for multiselected assignment which has same and different assignee - Rightpane Properties")
	public void SprintTest104_1_38D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName1 =  Utility.getObjectName(methodName).toString(); //Name of the object with current method date & time
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName1); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName1")); //Sets the assigned to property
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName2"), 2); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment (" + assigName1 + ") is created with two assigned to users.", driver);

			//Step-3 : Create an assignment with one assigned to user
			//-------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName2 =  Utility.getObjectName(methodName).toString(); //Name of the object with current method date & time
			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName2); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName1")); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Assignment(" + assigName2 + ")  is created with one assigned to user.", driver);

			//Step-4 : Open the Properties dialog of the assignment with various mutlipe assigned to users through rightpane
			//-----------------------------------------------------------------------------------------------------------------
			String assignName = assigName1 + "\n" + assigName2;
			homePage.listView.clickMultipleItems(assignName);
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of multi selected assignments with various assigned to users is opened through rightpane.");

			//Verification : Verifies if assigned to property has value varies
			//----------------------------------------------------------------
			if (metadatacard.isNotApprovedDisplayed(0) && metadatacard.isNotApprovedDisplayed(1) && metadatacard.isNotRejectedDisplayed(0) && metadatacard.isNotRejectedDisplayed(1)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Approved and Rejected icons are displayed on selecting assignments with same and various assigned to property.");
			else
				Log.fail("Test case Failed. Approved and Rejected icons are not displayed on selecting assignments with same and various assigned to property.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_38D

	/**
	 * 104.1.39 : Verify Approve & Reject icon for multiselected assignment which has same and different assignee
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Verify Approve & Reject icon for multiselected assignment which has same and different assignee")
	public void SprintTest104_1_39(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName1 =  Utility.getObjectName(methodName).toString(); //Name of the object with current method date & time
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName1); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName1")); //Sets the assigned to property
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName2"), 2); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment (" + assigName1 + ") is created with two assigned to users.", driver);

			//Step-3 : Create an assignment with one assigned to user
			//-------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName2 =  Utility.getObjectName(methodName).toString(); //Name of the object with current method date & time
			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName2); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", dataPool.get("UserFullName1")); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Assignment(" + assigName2 + ")  is created with one assigned to user.", driver);

			//Step-4 : Multi select the above created assignments
			//---------------------------------------------------
			String assignName = assigName1 + "\n" + assigName2;
			homePage.listView.clickMultipleItems(assignName);

			Log.message("4. Assignments with same and various assigned to users is selected.");

			//Verification : Verifies if assigned to property has value varies
			//----------------------------------------------------------------
			if (homePage.taskPanel.isItemExists(Caption.MenuItems.Approve.Value) && homePage.taskPanel.isItemExists(Caption.MenuItems.Reject.Value)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Approve and Reject text are displayed in taskpanel on selecting assignments with same and various assigned to property.");
			else
				Log.fail("Test case Failed. Approve and Reject text are not displayed in taskpanel on selecting assignments with same and various assigned to property.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_39

} //End Class BasicMultiSelect
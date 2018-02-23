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
import MFClient.Wrappers.ListView;
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class MultiSelectApprove {

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
				Log.endTestCase();
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
	 * 104.1.33.2A : Verify if Approve icon is displayed for multiselected  assignment (SHIFT key) object that is assigneed to logged in user  - Context menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint104"}, 
			description = "Verify if Approve icon is displayed for multiselected  assignment (SHIFT key) object that is assigneed to logged in user  - Context menu Properties")
	public void SprintTest104_1_33_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			int rand = Utility.getRandomNumber(2, 5);
			String[] assignName = new String[rand];
			String objName = Utility.getObjectName(methodName).toString();

			for (int i=0; i<rand; i++) {
				homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
				assignName[i] =  objName + "_" + i; //Name of the object with current method date & time
				MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
				metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
				metadatacard.setPropertyValue("Name or title", assignName[i]); //Sets the Assignment name
				Utils.fluentWait(driver);
				metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
				metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			}

			Log.message("1. New " + rand + " Assignments are created.", driver);

			//Step-2 : Open the Properties dialog of the new assignments in context menu
			//------------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyAssignments.Value, objName);
			homePage.listView.shiftclickMultipleItemsByIndex(0, rand-1); //Select multiple items by shift key
			homePage.listView.rightClickItemByIndex(rand-1); //Right clicks the item
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. Properties dialog of multi selected assignments using SHIFT key is opened through context menu.");

			//Step-3 : Click Approve button in the metadatacard
			//-------------------------------------------------
			if (!metadatacard.clickApproveIcon())
				throw new Exception("Approve Icon is not clicked.");

			Log.message("3. Approve icon in the Assignment is clicked in metadatacard for mutli-selected assignments.");

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.

			String strDiff = "";

			for (int i=0; i<rand; i++) 
				if (!ListView.getMarkedAsCompleteByItemIndex(driver, i).equalsIgnoreCase(userFullName))
					strDiff = strDiff + "Assignment : " + homePage.listView.getItemNameByItemIndex(i);

			if (strDiff.equals("")) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Multi selected assignment objects are approved.");
			else
				Log.fail("Test case Failed. One or more multi-selected assignment is not approved. Assignments are " + strDiff, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_33_2A

	/**
	 * 104.1.33.2B : Verify if Approve icon is displayed for multiselected  assignment (SHIFT key) object that is assigneed to logged in user  - Operations menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Verify if Approve icon is displayed for multiselected  assignment (SHIFT key) object that is assigneed to logged in user  - Operations menu Properties")
	public void SprintTest104_1_33_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			int rand = Utility.getRandomNumber(2, 5);
			String[] assignName = new String[rand];
			String objName = Utility.getObjectName(methodName).toString();

			for (int i=0; i<rand; i++) {
				homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
				assignName[i] =  objName + "_" + i; //Name of the object with current method date & time
				MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
				metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
				metadatacard.setPropertyValue("Name or title", assignName[i]); //Sets the Assignment name
				Utils.fluentWait(driver);
				metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
				metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			}

			Log.message("1. New " + rand + " Assignments are created.", driver);

			//Step-2 : Open the Properties dialog of the new assignments in operations menu
			//------------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyAssignments.Value, objName);
			homePage.listView.shiftclickMultipleItemsByIndex(0, rand-1); //Select multiple items by shift key
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from operations menu

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. Properties dialog of multi selected assignments using SHIFT key is opened through operations menu.");

			//Step-3 : Click Approve button in the metadatacard
			//-------------------------------------------------
			if (!metadatacard.clickApproveIcon())
				throw new Exception("Approve Icon is not clicked.");

			Log.message("3. Approve icon in the Assignment is clicked in metadatacard for mutli-selected assignments.");

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.

			String strDiff = "";

			for (int i=0; i<rand; i++) 
				if (!ListView.getMarkedAsCompleteByItemIndex(driver, i).equalsIgnoreCase(userFullName))
					strDiff = strDiff + "Assignment : " + homePage.listView.getItemNameByItemIndex(i);

			if (strDiff.equals("")) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Multi selected assignment objects are approved.");
			else
				Log.fail("Test case Failed. One or more multi-selected assignment is not approved. Assignments are " + strDiff, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_33_2B

	/**
	 * 104.1.33.2C : Verify if Approve icon is displayed for multiselected  assignment (SHIFT key) object that is assigneed to logged in user  - Taskpanel Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Verify if Approve icon is displayed for multiselected  assignment (SHIFT key) object that is assigneed to logged in user  - Taskpanel Properties")
	public void SprintTest104_1_33_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			int rand = Utility.getRandomNumber(2, 5);
			String[] assignName = new String[rand];
			String objName = Utility.getObjectName(methodName).toString();

			for (int i=0; i<rand; i++) {
				homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
				assignName[i] =  objName + "_" + i; //Name of the object with current method date & time
				MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
				metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
				metadatacard.setPropertyValue("Name or title", assignName[i]); //Sets the Assignment name
				Utils.fluentWait(driver);
				metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
				metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			}

			Log.message("1. New " + rand + " Assignments are created.", driver);

			//Step-2 : Open the Properties dialog of the new assignments in taskpanel menu
			//------------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyAssignments.Value, objName);
			homePage.listView.shiftclickMultipleItemsByIndex(0, rand-1); //Select multiple items by shift key
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from taskpanel

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. Properties dialog of multi selected assignments using SHIFT key is opened through taskpanel.");

			//Step-3 : Click Approve button in the metadatacard
			//-------------------------------------------------
			if (!metadatacard.clickApproveIcon())
				throw new Exception("Approve Icon is not clicked.");

			Log.message("3. Approve icon in the Assignment is clicked in metadatacard for mutli-selected assignments.");

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.

			String strDiff = "";

			for (int i=0; i<rand; i++) 
				if (!ListView.getMarkedAsCompleteByItemIndex(driver, i).equalsIgnoreCase(userFullName))
					strDiff = strDiff + "Assignment : " + homePage.listView.getItemNameByItemIndex(i);

			if (strDiff.equals("")) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Multi selected assignment objects are approved.");
			else
				Log.fail("Test case Failed. One or more multi-selected assignment is not approved. Assignments are " + strDiff, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_33_2C

	/**
	 * 104.1.33.2D : Verify if Approve icon is displayed for multiselected  assignment (SHIFT key) object that is assigneed to logged in user  - Rightpane Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Verify if Approve icon is displayed for multiselected  assignment (SHIFT key) object that is assigneed to logged in user  - Rightpane Properties")
	public void SprintTest104_1_33_2D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			int rand = Utility.getRandomNumber(2, 5);
			String[] assignName = new String[rand];
			String objName = Utility.getObjectName(methodName).toString();

			for (int i=0; i<rand; i++) {
				homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
				assignName[i] =  objName + "_" + i; //Name of the object with current method date & time
				MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
				metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
				metadatacard.setPropertyValue("Name or title", assignName[i]); //Sets the Assignment name
				Utils.fluentWait(driver);
				metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
				metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			}

			Log.message("1. New " + rand + " Assignments are created.", driver);

			//Step-2 : Open the Properties dialog of the new assignments in taskpanel menu
			//------------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyAssignments.Value, objName);
			homePage.listView.shiftclickMultipleItemsByIndex(0, rand-1); //Select multiple items by shift key

			MetadataCard metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("2. Properties dialog of multi selected assignments using SHIFT key is opened in rightpane.");

			//Step-3 : Click Approve button in the metadatacard
			//-------------------------------------------------
			if (!metadatacard.clickApproveIcon())
				throw new Exception("Approve Icon is not clicked.");

			Log.message("3. Approve icon in the Assignment is clicked in metadatacard for mutli-selected assignments.");

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.

			String strDiff = "";

			for (int i=0; i<rand; i++) 
				if (!ListView.getMarkedAsCompleteByItemIndex(driver, i).equalsIgnoreCase(userFullName))
					strDiff = strDiff + "Assignment : " + homePage.listView.getItemNameByItemIndex(i);

			if (strDiff.equals("")) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Multi selected assignment objects are approved.");
			else
				Log.fail("Test case Failed. One or more multi-selected assignment is not approved. Assignments are " + strDiff, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_33_2D

	/**
	 * 104.1.33.2E : Verify if Approve is displayed for multiselected  assignment (SHIFT key) object that is assigneed to logged in user  in taskpanel
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Verify if Approve is displayed for multiselected  assignment (SHIFT key) object that is assigneed to logged in user  in taskpanel")
	public void SprintTest104_1_33_2E(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			int rand = Utility.getRandomNumber(2, 5);
			String[] assignName = new String[rand];
			String objName = Utility.getObjectName(methodName).toString();

			for (int i=0; i<rand; i++) {
				homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
				assignName[i] =  objName + "_" + i; //Name of the object with current method date & time
				MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
				metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
				metadatacard.setPropertyValue("Name or title", assignName[i]); //Sets the Assignment name
				Utils.fluentWait(driver);
				metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
				metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			}

			Log.message("1. New " + rand + " Assignments are created.", driver);

			//Step-2 : Open the Properties dialog of the new assignments in taskpanel menu
			//------------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyAssignments.Value, objName);
			homePage.listView.shiftclickMultipleItemsByIndex(0, rand-1); //Select multiple items by shift key

			Log.message("2. Assignments are multi selected using SHIFT key.");

			//Step-3 : Click Reject button in the taskpanel
			//-------------------------------------------------
			homePage.taskPanel.markApproveReject(Caption.MenuItems.Approve.Value);

			Log.message("3. Approve is clicked in taskpanel for mutli-selected assignments.");

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			String strDiff = "";

			for (int i=0; i<rand; i++) 
				if (!ListView.getMarkedAsCompleteByItemIndex(driver, i).equalsIgnoreCase(userFullName))
					strDiff = strDiff + "Assignment : " + homePage.listView.getItemNameByItemIndex(i);

			if (strDiff.equals("")) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Multi selected assignment objects are approved.");
			else
				Log.fail("Test case Failed. One or more multi-selected assignment is not approved. Assignments are " + strDiff, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_33_2E

	/**
	 * 104.1.33.4A : Verify if Approve icon is displayed for multiselected  assignment (CTRL key) object that is assigneed to logged in user  - Context menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Verify if Approve icon is displayed for multiselected  assignment (CTRL key) object that is assigneed to logged in user  - Context menu Properties")
	public void SprintTest104_1_33_4A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			int noOfItems = Utility.getRandomNumber(2, 10);
			String[] assignName = new String[noOfItems];
			String objName = Utility.getObjectName(methodName).toString();

			for (int i=0; i<noOfItems; i++) {
				homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
				assignName[i] =  objName + "_" + i; //Name of the object with current method date & time
				MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
				metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
				metadatacard.setPropertyValue("Name or title", assignName[i]); //Sets the Assignment name
				Utils.fluentWait(driver);
				metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
				metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			}

			Log.message("1. New Assignments are created with two assigned to users.", driver);

			//Step-2 : Open the Properties dialog of the assignment through operations menu
			//-----------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyAssignments.Value, objName);
			String items = homePage.listView.multiSelectRightClickByCtrlKey(noOfItems); //Select multiple items by ctrl key
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. Properties dialog of multi selected assignments using CTRL key is opened through context menu.");

			String[] selectedItems = items.split("::");	

			//Step-3 : Click Approve button in the metadatacard
			//-------------------------------------------------
			if (!metadatacard.clickApproveIcon())
				throw new Exception("Approve Icon is not clicked.");

			Log.message("3. Approve icon in the Assignment is clicked in metadatacard for mutli-selected assignments.");

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.

			String strDiff = "";

			for (int i=0; i<noOfItems; i++) 
				if (!ListView.getMarkedAsCompleteByItemName(driver, selectedItems[i]).equalsIgnoreCase(userFullName))
					strDiff = strDiff + "Assignment : " + homePage.listView.getItemNameByItemIndex(i);

			if (strDiff.equals("")) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Multi selected assignment objects are approved.");
			else
				Log.fail("Test case Failed. One or more multi-selected assignment is not approved. Assignments are " + strDiff, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_33_4A

	/**
	 * 104.1.33.4B : Verify if Approve icon is displayed for multiselected  assignment (CTRL key) object that is assigneed to logged in user  - Operations menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Verify if Approve icon is displayed for multiselected  assignment (CTRL key) object that is assigneed to logged in user  - Operations menu Properties")
	public void SprintTest104_1_33_4B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			int noOfItems = Utility.getRandomNumber(2, 10);
			String[] assignName = new String[noOfItems];
			String objName = Utility.getObjectName(methodName).toString();

			for (int i=0; i<noOfItems; i++) {
				homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
				assignName[i] =  objName + "_" + i; //Name of the object with current method date & time
				MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
				metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
				metadatacard.setPropertyValue("Name or title", assignName[i]); //Sets the Assignment name
				Utils.fluentWait(driver);
				metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
				metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			}

			Log.message("1. New Assignments are created with two assigned to users.", driver);

			//Step-2 : Open the Properties dialog of the assignment through operations menu
			//-----------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyAssignments.Value, objName);
			String items = homePage.listView.multiSelectByCtrlKey(noOfItems); //Select multiple items by ctrl key
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from operations menu

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. Properties dialog of multi selected assignments using CTRL key is opened through operations menu.");

			String[] selectedItems = items.split("::");	

			//Step-3 : Click Approve button in the metadatacard
			//-------------------------------------------------
			if (!metadatacard.clickApproveIcon())
				throw new Exception("Approve Icon is not clicked.");

			Log.message("3. Approve icon in the Assignment is clicked in metadatacard for mutli-selected assignments.");

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.

			String strDiff = "";

			for (int i=0; i<noOfItems; i++) 
				if (!ListView.getMarkedAsCompleteByItemName(driver, selectedItems[i]).equalsIgnoreCase(userFullName))
					strDiff = strDiff + "Assignment : " + homePage.listView.getItemNameByItemIndex(i);

			if (strDiff.equals("")) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Multi selected assignment objects are approved.");
			else
				Log.fail("Test case Failed. One or more multi-selected assignment is not approved. Assignments are " + strDiff, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_33_4B

	/**
	 * 104.1.33.4C : Verify if Approve icon is displayed for multiselected  assignment (CTRL key) object that is assigneed to logged in user  - taskpanel properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Verify if Approve icon is displayed for multiselected  assignment (CTRL key) object that is assigneed to logged in user  - taskpanel Properties")
	public void SprintTest104_1_33_4C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			int noOfItems = Utility.getRandomNumber(2, 10);
			String[] assignName = new String[noOfItems];
			String objName = Utility.getObjectName(methodName).toString();

			for (int i=0; i<noOfItems; i++) {
				homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
				assignName[i] =  objName + "_" + i; //Name of the object with current method date & time
				MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
				metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
				metadatacard.setPropertyValue("Name or title", assignName[i]); //Sets the Assignment name
				Utils.fluentWait(driver);
				metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
				metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			}

			Log.message("1. New Assignments are created with two assigned to users.", driver);

			//Step-2 : Open the Properties dialog of the assignment through taskpanel menu
			//-----------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyAssignments.Value, objName);
			String items  = homePage.listView.multiSelectByCtrlKey(noOfItems); //Select multiple items by ctrl key
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from taskpanel

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. Properties dialog of multi selected assignments using CTRL key is opened through taskpanel.");

			//Step-3 : Click Approve button in the metadatacard
			//-------------------------------------------------
			if (!metadatacard.clickApproveIcon())
				throw new Exception("Approve Icon is not clicked.");

			Log.message("3. Approve icon in the Assignment is clicked in metadatacard for mutli-selected assignments.");

			String[] selectedItems = items.split("::");	

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.

			String strDiff = "";

			for (int i=0; i<noOfItems; i++) 
				if (!ListView.getMarkedAsCompleteByItemName(driver, selectedItems[i]).equalsIgnoreCase(userFullName))
					strDiff = strDiff + "Assignment : " + homePage.listView.getItemNameByItemIndex(i);

			if (strDiff.equals("")) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Multi selected assignment objects are approved.");
			else
				Log.fail("Test case Failed. One or more multi-selected assignment is not approved. Assignments are " + strDiff, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_33_4C

	/**
	 * 104.1.33.4D : Verify if Approve icon is displayed for multiselected  assignment (CTRL key) object that is assigneed to logged in user  - Rightpane properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Verify if Approve icon is displayed for multiselected  assignment (CTRL key) object that is assigneed to logged in user  - Rightpane Properties")
	public void SprintTest104_1_33_4D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			int noOfItems = Utility.getRandomNumber(2, 10);
			String[] assignName = new String[noOfItems];
			String objName = Utility.getObjectName(methodName).toString();

			for (int i=0; i<noOfItems; i++) {
				homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
				assignName[i] =  objName + "_" + i; //Name of the object with current method date & time
				MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
				metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
				metadatacard.setPropertyValue("Name or title", assignName[i]); //Sets the Assignment name
				Utils.fluentWait(driver);
				metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
				metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			}

			Log.message("1. New Assignments are created with two assigned to users.", driver);

			//Step-2 : Open the Properties dialog of the assignment through operations menu
			//-----------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyAssignments.Value, objName);
			String items = homePage.listView.multiSelectByCtrlKey(noOfItems); //Select multiple items by ctrl key

			String[] selectedItems = items.split("::");

			MetadataCard metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("2. Properties dialog of multi selected assignments using CTRL key is opened in rightpane.");

			//Step-3 : Click Approve button in the metadatacard
			//-------------------------------------------------
			if (!metadatacard.clickApproveIcon())
				throw new Exception("Reject Icon is not clicked.");

			Log.message("3. Approve icon in the Assignment is clicked in metadatacard for mutli-selected assignments.");

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.

			String strDiff = "";

			for (int i=0; i<noOfItems; i++) 
				if (!ListView.getMarkedAsCompleteByItemName(driver, selectedItems[i]).equalsIgnoreCase(userFullName))
					strDiff = strDiff + "Assignment : " + homePage.listView.getItemNameByItemIndex(i);

			if (strDiff.equals("")) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Multi selected assignment objects are approved.");
			else
				Log.fail("Test case Failed. One or more multi-selected assignment is not approved. Assignments are " + strDiff, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_33_4D

	/**
	 * 104.1.33.4E : Verify if Approve icon is displayed for multiselected  assignment (CTRL key) object that is assigneed to logged in user in taskpanel
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Verify if Approve icon is displayed for multiselected  assignment (CTRL key) object that is assigneed to logged in user in taskpanel")
	public void SprintTest104_1_33_4E(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			int noOfItems = Utility.getRandomNumber(2, 10);
			String[] assignName = new String[noOfItems];
			String objName = Utility.getObjectName(methodName).toString();

			for (int i=0; i<noOfItems; i++) {
				homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
				assignName[i] =  objName + "_" + i; //Name of the object with current method date & time
				MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
				metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
				metadatacard.setPropertyValue("Name or title", assignName[i]); //Sets the Assignment name
				Utils.fluentWait(driver);
				metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
				metadatacard.saveAndClose(); //Saves and Closes the metadatacard.
			}

			Log.message("1. New Assignments are created with two assigned to users.", driver);

			//Step-2 : Multi-Select the assignments
			//---------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyAssignments.Value, objName);
			String items = homePage.listView.multiSelectByCtrlKey(noOfItems); //Select multiple items by ctrl key

			Log.message("2. Assignments are multi-selected using CTRL key.");

			//Step-3 : Click Approve button in the taskpanel
			//-------------------------------------------------
			homePage.taskPanel.markApproveReject(Caption.MenuItems.Approve.Value); //Marks Approve from Taskpanel
			Utils.fluentWait(driver);
			Log.message("3. Approve button is selected in taskpanel for multi-selected assignments.");

			String[] selectedItems = items.split("::");	

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			String strDiff = "";

			for (int i=0; i<noOfItems; i++) 
				if (!ListView.getMarkedAsCompleteByItemName(driver, selectedItems[i]).equalsIgnoreCase(userFullName))
					strDiff = strDiff + "Assignment : " + homePage.listView.getItemNameByItemIndex(i);

			if (strDiff.equals("")) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Multi selected assignment objects are approved.");
			else
				Log.fail("Test case Failed. One or more multi-selected assignment is not approved. Assignments are " + strDiff, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_33_4E

	/**
	 * 104.1.40A : Approve multiselected assignment object which has same and different user - Context Menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Approve multiselected assignment object which has same and different user - Context Menu Properties")
	public void SprintTest104_1_40A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-5 : Select Approve icon for the multi-selected assignments
			//---------------------------------------------------------------
			int propIndex = metadatacard.getPropertyValueIndex("Assigned to", dataPool.get("UserFullName1"));

			if (!metadatacard.clickApproveIcon(propIndex))
				throw new Exception("Approve icon is not selected.");

			Log.message("5. Approve icon is selected");

			//Verification : Verifies if assigned to property has value varies
			//----------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves the metadatacard

			if (ListView.isApprovedByItemName(driver, assigName1) && ListView.isApprovedByItemName(driver, assigName2)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Mutliselected assignments with same and various assigned to property is approved.");
			else
				Log.fail("Test case Failed. Mutliselected assignments with same and various assigned to property is not approved.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_40A

	/**
	 * 104.1.40B : Approve multiselected assignment object which has same and different user - Operations Menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Approve multiselected assignment object which has same and different user - Operations Menu Properties")
	public void SprintTest104_1_40B(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-4 : Open the Properties dialog of the assignment with various mutlipe assigned to users through Operations menu
			//-----------------------------------------------------------------------------------------------------------------
			String assignName = assigName1 + "\n" + assigName2;
			homePage.listView.clickMultipleItems(assignName);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from Operations menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of multi selected assignments with various assigned to users is opened through Operations menu.");

			//Step-5 : Select Approve icon for the multi-selected assignments
			//---------------------------------------------------------------
			int propIndex = metadatacard.getPropertyValueIndex("Assigned to", dataPool.get("UserFullName1"));

			if (!metadatacard.clickApproveIcon(propIndex))
				throw new Exception("Approve icon is not selected.");

			Log.message("5. Approve icon is selected");

			//Verification : Verifies if assigned to property has value varies
			//----------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves the metadatacard

			if (ListView.isApprovedByItemName(driver, assigName1) && ListView.isApprovedByItemName(driver, assigName2)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Mutliselected assignments with same and various assigned to property is approved.");
			else
				Log.fail("Test case Failed. Mutliselected assignments with same and various assigned to property is not approved.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_40B

	/**
	 * 104.1.40C : Approve multiselected assignment object which has same and different user - Taskpanel Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Approve multiselected assignment object which has same and different user - Taskpanel Properties")
	public void SprintTest104_1_40C(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from Operations menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of multi selected assignments with various assigned to users is opened through taskpanel menu.");

			//Step-5 : Select Approve icon for the multi-selected assignments
			//---------------------------------------------------------------
			int propIndex = metadatacard.getPropertyValueIndex("Assigned to", dataPool.get("UserFullName1"));

			if (!metadatacard.clickApproveIcon(propIndex))
				throw new Exception("Approve icon is not selected.");

			Log.message("5. Approve icon is selected");

			//Verification : Verifies if assigned to property has value varies
			//----------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves the metadatacard

			if (ListView.isApprovedByItemName(driver, assigName1) && ListView.isApprovedByItemName(driver, assigName2)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Mutliselected assignments with same and various assigned to property is approved.");
			else
				Log.fail("Test case Failed. Mutliselected assignments with same and various assigned to property is not approved.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_40C

	/**
	 * 104.1.40D : Approve multiselected assignment object which has same and different user - Rightpane Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Approve multiselected assignment object which has same and different user - Rightpane Properties")
	public void SprintTest104_1_40D(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-4 : Open the Properties dialog of the assignment with various mutlipe assigned to users through rightpane menu
			//-----------------------------------------------------------------------------------------------------------------
			String assignName = assigName1 + "\n" + assigName2;
			homePage.listView.clickMultipleItems(assignName);

			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of multi selected assignments with various assigned to users is opened through rightpane menu.");

			//Step-5 : Select Approve icon for the multi-selected assignments
			//---------------------------------------------------------------
			int propIndex = metadatacard.getPropertyValueIndex("Assigned to", dataPool.get("UserFullName1"));

			if (!metadatacard.clickApproveIcon(propIndex))
				throw new Exception("Approve icon is not selected.");

			Log.message("5. Approve icon is selected");

			//Verification : Verifies if assigned to property has value varies
			//----------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves the metadatacard

			if (ListView.isApprovedByItemName(driver, assigName1) && ListView.isApprovedByItemName(driver, assigName2)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Mutliselected assignments with same and various assigned to property is approved.");
			else
				Log.fail("Test case Failed. Mutliselected assignments with same and various assigned to property is not approved.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_40D

	/**
	 * 104.1.42 : Approve multiselected assignment object which has same and different user from Taskpanel
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Approve multiselected assignment object which has same and different user from Taskpanel")
	public void SprintTest104_1_42(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			String assigName1 =  Utility.getObjectName(methodName).toString();
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

			//Step-4 : Multi select the assignment with various mutlipe assigned to users 
			//---------------------------------------------------------------------------
			String assignName = assigName1 + "\n" + assigName2;
			homePage.listView.clickMultipleItems(assignName);

			Log.message("4. Assignments with various assigned to users is selected.");

			//Step-5 : Select Approve icon for the multi-selected assignments
			//---------------------------------------------------------------
			homePage.taskPanel.markApproveReject(Caption.MenuItems.Approve.Value); //Marks Approve from Taskpanel //Clicks Approve from taskpanel

			Log.message("5. Approve is selected from taskpanel.");

			//Verification : Verifies if assigned to property has value varies
			//----------------------------------------------------------------
			if (ListView.isApprovedByItemName(driver, assigName1) && ListView.isApprovedByItemName(driver, assigName2)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Mutliselected assignments with same and various assigned to property is approved.");
			else
				Log.fail("Test case Failed. Mutliselected assignments with same and various assigned to property is not approved.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_42


	/**
	 * 104.1.48A : Discard the approved multi-selected assignment in metadatacard opened through context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Discard the approved multi-selected assignment in metadatacard opened through context menu")
	public void SprintTest104_1_48A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			String assigName1 =  Utility.getObjectName(methodName);
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName1); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment (" + assigName1 + ") is created.", driver);

			//Step-3 : Create second asssignment with one assigned to user
			//----------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName2 =  Utility.getObjectName(methodName);
			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName2); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Assignment (" + assigName2 + ") is created.", driver);

			//Step-4 : Open the Properties dialog of the assignment and document through context menu
			//----------------------------------------------------------------------------------------
			String mergedName = assigName1 + "\n" + assigName2;
			homePage.listView.clickMultipleItems(mergedName);
			homePage.listView.rightClickItem(assigName2);
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of document and assignment is opened through context menu.");

			//Step-5 : Select Approve icon for the multi-selected assignments
			//---------------------------------------------------------------
			if (!metadatacard.clickApproveIcon())
				throw new Exception("Approve icon is not selected.");

			Log.message("5. Approve icon is selected");

			//Step-6 : Click Discard button
			//-----------------------------
			metadatacard.clickDiscardButton(); //Clicks Discard button

			Log.message("6. Discard button is clicked ");

			//Verification : Verifies if multiselected assignment is not approved
			//-------------------------------------------------------------------
			if (!ListView.isApprovedByItemName(driver, assigName2) && !ListView.isApprovedByItemName(driver, assigName1)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment is not approved on discarding the multi-selected approved assignment in metadatacard properties opened through context menu.");
			else
				Log.fail("Test case Failed. Assignment is approved on discarding the multi-selected approved assignment in metadatacard properties opened through context menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_48A

	/**
	 * 104.1.48B : Discard the approved multi-selected assignment in metadatacard opened through operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Discard the approved multi-selected assignment in metadatacard opened through operations menu")
	public void SprintTest104_1_48B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			String assigName1 =  Utility.getObjectName(methodName);
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName1); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment (" + assigName1 + ") is created.", driver);

			//Step-3 : Create second asssignment with one assigned to user
			//----------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName2 =  Utility.getObjectName(methodName);
			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName2); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Assignment (" + assigName2 + ") is created.", driver);

			//Step-4 : Open the Properties dialog of the assignment and document through context menu
			//----------------------------------------------------------------------------------------
			String mergedName = assigName1 + "\n" + assigName2;
			homePage.listView.clickMultipleItems(mergedName);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from operations menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of document and assignment is opened through operations menu.");

			//Step-5 : Select Approve icon for the multi-selected assignments
			//---------------------------------------------------------------
			if (!metadatacard.clickApproveIcon())
				throw new Exception("Approve icon is not selected.");

			Log.message("5. Approve icon is selected");

			//Step-6 : Click Discard button
			//-----------------------------
			metadatacard.clickDiscardButton(); //Clicks Discard button

			Log.message("6. Discard button is clicked ");

			//Verification : Verifies if multiselected assignment is not approved
			//-------------------------------------------------------------------
			if (!ListView.isApprovedByItemName(driver, assigName2) && !ListView.isApprovedByItemName(driver, assigName1)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment is not approved on discarding the multi-selected approved assignment in metadatacard properties opened through operations menu.");
			else
				Log.fail("Test case Failed. Assignment is approved on discarding the multi-selected approved assignment in metadatacard properties opened through operations menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_48B

	/**
	 * 104.1.48C : Discard the approved multi-selected assignment in metadatacard opened through taskpanel menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Discard the approved multi-selected assignment in metadatacard opened through taskpanel menu")
	public void SprintTest104_1_48C(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			String assigName1 =  Utility.getObjectName(methodName);
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName1); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment (" + assigName1 + ") is created.", driver);

			//Step-3 : Create second asssignment with one assigned to user
			//----------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName2 =  Utility.getObjectName(methodName);
			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName2); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Assignment (" + assigName2 + ") is created.", driver);

			//Step-4 : Open the Properties dialog of the assignment and document through taskpanel menu
			//----------------------------------------------------------------------------------------
			String mergedName = assigName1 + "\n" + assigName2;
			homePage.listView.clickMultipleItems(mergedName);
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from taskpanel menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of document and assignment is opened through taskpanel menu.");

			//Step-5 : Select Approve icon for the multi-selected assignments
			//---------------------------------------------------------------
			if (!metadatacard.clickApproveIcon())
				throw new Exception("Approve icon is not selected.");

			Log.message("5. Approve icon is selected");

			//Step-6 : Click Discard button
			//-----------------------------
			metadatacard.clickDiscardButton(); //Clicks Discard button

			Log.message("6. Discard button is clicked ");

			//Verification : Verifies if multiselected assignment is not approved
			//-------------------------------------------------------------------
			if (!ListView.isApprovedByItemName(driver, assigName2) && !ListView.isApprovedByItemName(driver, assigName1)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment is not approved on discarding the multi-selected approved assignment in metadatacard properties opened through taskpanel menu.");
			else
				Log.fail("Test case Failed. Assignment is approved on discarding the multi-selected approved assignment in metadatacard properties opened through taskpanel menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_48C

	/**
	 * 104.1.48D : Discard the approved multi-selected assignment in metadatacard opened in rightpanel
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Discard the approved multi-selected assignment in metadatacard opened in rightpanel")
	public void SprintTest104_1_48D(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			String assigName1 =  Utility.getObjectName(methodName);
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName1); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment (" + assigName1 + ") is created.", driver);

			//Step-3 : Create second asssignment with one assigned to user
			//----------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName2 =  Utility.getObjectName(methodName);
			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName2); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Assignment (" + assigName2 + ") is created.", driver);

			//Step-4 : Open the Properties dialog of the assignments in rightpanel
			//--------------------------------------------------------------------
			String mergedName = assigName1 + "\n" + assigName2;
			homePage.listView.clickMultipleItems(mergedName);

			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of document and assignment is opened in rightpanel.");

			//Step-5 : Select Approve icon for the multi-selected assignments
			//---------------------------------------------------------------
			if (!metadatacard.clickApproveIcon())
				throw new Exception("Approve icon is not selected.");

			Log.message("5. Approve icon is selected");

			//Step-6 : Click Discard button
			//-----------------------------
			metadatacard.clickDiscardButton(); //Clicks Discard button

			Log.message("6. Discard button is clicked ");

			//Verification : Verifies if multiselected assignment is not approved
			//-------------------------------------------------------------------
			if (!ListView.isApprovedByItemName(driver, assigName2) && !ListView.isApprovedByItemName(driver, assigName1)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment is not approved on discarding the multi-selected approved assignment in metadatacard properties opened in rightpanel.");
			else
				Log.fail("Test case Failed. Assignment is approved on discarding the multi-selected approved assignment in metadatacard properties opened in rightpanel.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_48D

	/**
	 * 104.1.51A : Adding comments should be possible in metadatacard for multi-selected assignments opened through context menu after approving the assignment
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Adding comments should be possible in metadatacard for multi-selected assignments opened through context menu after approving the assignment")
	public void SprintTest104_1_51A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Create an assignment with 
			//-----------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName1 =  Utility.getObjectName(methodName);
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName1); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment (" + assigName1 + ") is created.", driver);

			//Step-3 : Create second asssignment with one assigned to user
			//----------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName2 =  Utility.getObjectName(methodName);
			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName2); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Assignment (" + assigName2 + ") is created.", driver);

			//Step-4 : Open the Properties dialog of the assignment and document through context menu
			//----------------------------------------------------------------------------------------
			String mergedName = assigName1 + "\n" + assigName2;
			homePage.listView.clickMultipleItems(mergedName);
			homePage.listView.rightClickItem(assigName2);
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of document and assignment is opened through context menu.");

			//Step-5 : Select Approve icon for the multi-selected assignments
			//---------------------------------------------------------------
			if (!metadatacard.clickApproveIcon())
				throw new Exception("Approve icon is not selected.");

			Log.message("5. Approve icon is selected");

			//Step-6 : Add comments to the assignment after selecting approve icon
			//--------------------------------------------------------------------
			String comments = Utility.getObjectName(methodName);
			metadatacard.setComments(comments);

			Log.message("5. Comments is entered assignment after selecting approve icon.");

			//Step-7 : Save the assignment object
			//-----------------------------------
			metadatacard.saveAndClose(); 

			Log.message("6. Metadatacard is saved after selecting approve icon and entering comments.");

			//Verification : Verifies if multiselected assignment is not approved
			//-------------------------------------------------------------------
			if (!ListView.isApprovedByItemName(driver, assigName2) || !ListView.isApprovedByItemName(driver, assigName1)) //Checks if Assignment completed icon is displayed
				throw new Exception("Assignments are not in approved state");

			if (ListView.getCommentsByItemName(driver, assigName1).equalsIgnoreCase(comments) && ListView.getCommentsByItemName(driver, assigName2).equalsIgnoreCase(comments)) //Verifies if comments are as entered
				Log.pass("Test case Passed. Comments entered in metadatacard opened through context menu after clicking approve icon is saved successfully for mutli-selected assignments.");
			else
				Log.fail("Test case Failed. Comments entered in metadatacard opened through context menu after clicking approve icon is not saved for mutli-selected assignments.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_51A

	/**
	 * 104.1.51B : Adding comments should be possible in metadatacard for multi-selected assignments opened through operations menu after approving the assignment
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Adding comments should be possible in metadatacard for multi-selected assignments opened through operations menu after approving the assignment")
	public void SprintTest104_1_51B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Create an assignment with 
			//-----------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName1 =  Utility.getObjectName(methodName);
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName1); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment (" + assigName1 + ") is created.", driver);

			//Step-3 : Create second asssignment with one assigned to user
			//----------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName2 =  Utility.getObjectName(methodName);
			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName2); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Assignment (" + assigName2 + ") is created.", driver);

			//Step-4 : Open the Properties dialog of the assignment and document through operations menu
			//----------------------------------------------------------------------------------------
			String mergedName = assigName1 + "\n" + assigName2;
			homePage.listView.clickMultipleItems(mergedName);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from operations menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of document and assignment is opened through operations menu.");

			//Step-5 : Select Approve icon for the multi-selected assignments
			//---------------------------------------------------------------
			if (!metadatacard.clickApproveIcon())
				throw new Exception("Approve icon is not selected.");

			Log.message("5. Approve icon is selected");

			//Step-6 : Add comments to the assignment after selecting approve icon
			//--------------------------------------------------------------------
			String comments = Utility.getObjectName(methodName);
			metadatacard.setComments(comments);

			Log.message("6. Comments is entered assignment after selecting approve icon.", driver);

			//Step-7 : Save the assignment object
			//-----------------------------------
			metadatacard.saveAndClose(); 

			Log.message("7. Metadatacard is saved after selecting approve icon and entering comments.");

			//Verification : Verifies if multiselected assignment is not approved
			//-------------------------------------------------------------------
			if (!ListView.isApprovedByItemName(driver, assigName2) || !ListView.isApprovedByItemName(driver, assigName1)) //Checks if Assignment completed icon is displayed
				throw new Exception("Assignments are not in approved state");

			if (ListView.getCommentsByItemName(driver, assigName1).equalsIgnoreCase(comments) && ListView.getCommentsByItemName(driver, assigName2).equalsIgnoreCase(comments)) //Verifies if comments are as entered
				Log.pass("Test case Passed. Comments entered in metadatacard opened through operations menu after clicking approve icon is saved successfully for mutli-selected assignments.");
			else
				Log.fail("Test case Failed. Comments entered in metadatacard opened through operations menu after clicking approve icon is not saved for mutli-selected assignments.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_51B

	/**
	 * 104.1.51C : Adding comments should be possible in metadatacard for multi-selected assignments opened through taskpanel menu after approving the assignment
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Adding comments should be possible in metadatacard for multi-selected assignments opened through taskpanel menu after approving the assignment")
	public void SprintTest104_1_51C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Create an assignment with 
			//-----------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName1 =  Utility.getObjectName(methodName);
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName1); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment (" + assigName1 + ") is created.", driver);

			//Step-3 : Create second asssignment with one assigned to user
			//----------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName2 =  Utility.getObjectName(methodName);
			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName2); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Assignment (" + assigName2 + ") is created.", driver);

			//Step-4 : Open the Properties dialog of the assignment and document through taskpanel menu
			//----------------------------------------------------------------------------------------
			String mergedName = assigName1 + "\n" + assigName2;
			homePage.listView.clickMultipleItems(mergedName);
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from taskpanel menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of document and assignment is opened through taskpanel menu.");

			//Step-5 : Select Approve icon for the multi-selected assignments
			//---------------------------------------------------------------
			if (!metadatacard.clickApproveIcon())
				throw new Exception("Approve icon is not selected.");

			Log.message("5. Approve icon is selected");

			//Step-6 : Add comments to the assignment after selecting approve icon
			//--------------------------------------------------------------------
			String comments = Utility.getObjectName(methodName);
			metadatacard.setComments(comments);

			Log.message("6. Comments is entered assignment after selecting approve icon.", driver);

			//Step-7 : Save the assignment object
			//-----------------------------------
			metadatacard.saveAndClose(); 

			Log.message("7. Metadatacard is saved after selecting approve icon and entering comments.");

			//Verification : Verifies if multiselected assignment is not approved
			//-------------------------------------------------------------------
			if (!ListView.isApprovedByItemName(driver, assigName2) || !ListView.isApprovedByItemName(driver, assigName1)) //Checks if Assignment completed icon is displayed
				throw new Exception("Assignments are not in approved state");

			if (ListView.getCommentsByItemName(driver, assigName1).equalsIgnoreCase(comments) && ListView.getCommentsByItemName(driver, assigName2).equalsIgnoreCase(comments)) //Verifies if comments are as entered
				Log.pass("Test case Passed. Comments entered in metadatacard opened through taskpanel menu after clicking approve icon is saved successfully for mutli-selected assignments.");
			else
				Log.fail("Test case Failed. Comments entered in metadatacard opened through taskpanel menu after clicking approve icon is not saved for mutli-selected assignments.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_51C

	/**
	 * 104.1.51D : Adding comments should be possible in metadatacard for multi-selected assignments opened in rightpane after approving the assignment
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Adding comments should be possible in metadatacard for multi-selected assignments opened in rightpane after approving the assignment")
	public void SprintTest104_1_51D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Create an assignment with 
			//-----------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName1 =  Utility.getObjectName(methodName);
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName1); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment (" + assigName1 + ") is created.", driver);

			//Step-3 : Create second asssignment with one assigned to user
			//----------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName2 =  Utility.getObjectName(methodName);
			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName2); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Assignment (" + assigName2 + ") is created.", driver);

			//Step-4 : Open the Properties dialog of the assignment in rightpane
			//----------------------------------------------------------------------------------------
			String mergedName = assigName1 + "\n" + assigName2;
			homePage.listView.clickMultipleItems(mergedName);

			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of multiselected assignments is opened in rightpane.");

			//Step-5 : Select Approve icon for the multi-selected assignments
			//---------------------------------------------------------------
			if (!metadatacard.clickApproveIcon())
				throw new Exception("Approve icon is not selected.");

			Log.message("5. Approve icon is selected");

			//Step-6 : Add comments to the assignment after selecting approve icon
			//--------------------------------------------------------------------
			String comments = Utility.getObjectName(methodName);
			metadatacard.setComments(comments);

			Log.message("6. Comments is entered assignment after selecting approve icon.", driver);

			//Step-7 : Save the assignment object
			//-----------------------------------
			metadatacard.saveAndClose(); 

			Log.message("7. Metadatacard is saved after selecting approve icon and entering comments.");

			//Verification : Verifies if multiselected assignment is not approved
			//-------------------------------------------------------------------
			if (!ListView.isApprovedByItemName(driver, assigName2) || !ListView.isApprovedByItemName(driver, assigName1)) //Checks if Assignment completed icon is displayed
				throw new Exception("Assignments are not in approved state");

			if (ListView.getCommentsByItemName(driver, assigName1).equalsIgnoreCase(comments) && ListView.getCommentsByItemName(driver, assigName2).equalsIgnoreCase(comments)) //Verifies if comments are as entered
				Log.pass("Test case Passed. Comments entered in metadatacard opened in rightpane after clicking approve icon is saved successfully for mutli-selected assignments.");
			else
				Log.fail("Test case Failed. Comments entered in metadatacard opened in rightpane after clicking approve icon is not saved for mutli-selected assignments.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_51D

	/**
	 * 172015.3.32A : User should be able to Approve multiple assignments in metadatacard opened through context menu that has both class [All must approve and Any can approve]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "User should be able to Approve multiple assignments in metadatacard opened through context menu that has both class [All must approve and Any can approve]")
	public void SprintTest172015_3_32A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			String assigName1 =  Utility.getObjectName(methodName) + "_1";
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentAllMustApprove.Value); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName1); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment (" + assigName1 + ") is created with class (" + Caption.Classes.AssignmentAllMustApprove.Value + ") is crated.", driver);

			//Step-3 : Create an assignment with one assigned to user
			//-------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName2 =  Utility.getObjectName(methodName).toString() + "_2"; //Name of the object with current method date & time
			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentAnyoneCanApprove.Value); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName2); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Assignment (" + assigName2 + ") is created with class (" + Caption.Classes.AssignmentAnyoneCanApprove.Value + ") is crated.", driver);

			//Step-4 : Open the Properties dialog of the assignment and document through context menu
			//----------------------------------------------------------------------------------------
			String assignName = assigName1 + "\n" + assigName2;
			homePage.listView.clickMultipleItems(assignName);
			homePage.listView.rightClickItem(assigName2);
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of document and assignment is opened through context menu.");

			//Step-5 : Select Approve icon for the multi-selected assignments
			//---------------------------------------------------------------
			if (!metadatacard.clickApproveIcon())
				throw new Exception("Approve icon is not selected.");

			Log.message("5. Approve icon is selected");

			//Verification : Verifies if assigned to property has value varies
			//----------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves the metadatacard

			if (ListView.isApprovedByItemName(driver, assigName1) && ListView.isApprovedByItemName(driver, assigName2)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignments with two different classes are approved in metadatacard opened through context menu.");
			else
				Log.fail("Test case Failed. Assignments with two different classes are not approved in metadatacard opened through context menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_32A

	/**
	 * 172015.3.32B : User should be able to Approve multiple assignments in metadatacard opened through operations menu that has both class [All must approve and Any can approve]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "User should be able to Approve multiple assignments in metadatacard opened through operations menu that has both class [All must approve and Any can approve]")
	public void SprintTest172015_3_32B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			String assigName1 =  Utility.getObjectName(methodName) + "_1";
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentAllMustApprove.Value); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName1); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment (" + assigName1 + ") is created with class (" + Caption.Classes.AssignmentAllMustApprove.Value + ") is crated.", driver);

			//Step-3 : Create an assignment with one assigned to user
			//-------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName2 =  Utility.getObjectName(methodName).toString() + "_2"; //Name of the object with current method date & time
			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentAnyoneCanApprove.Value); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName2); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Assignment (" + assigName2 + ") is created with class (" + Caption.Classes.AssignmentAnyoneCanApprove.Value + ") is crated.", driver);

			//Step-4 : Open the Properties dialog of the assignment and document through operations menu
			//----------------------------------------------------------------------------------------
			String assignName = assigName1 + "\n" + assigName2;
			homePage.listView.clickMultipleItems(assignName);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from operations menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of document and assignment is opened through operations menu.");

			//Step-5 : Select Approve icon for the multi-selected assignments
			//---------------------------------------------------------------
			if (!metadatacard.clickApproveIcon())
				throw new Exception("Approve icon is not selected.");

			Log.message("5. Approve icon is selected");

			//Verification : Verifies if assigned to property has value varies
			//----------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves the metadatacard

			if (ListView.isApprovedByItemName(driver, assigName1) && ListView.isApprovedByItemName(driver, assigName2)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignments with two different classes are approved in metadatacard opened through operations menu.");
			else
				Log.fail("Test case Failed. Assignments with two different classes are not approved in metadatacard opened through operations menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_32B

	/**
	 * 172015.3.32C : User should be able to Approve multiple assignments in metadatacard opened through taskpanel menu that has both class [All must approve and Any can approve]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "User should be able to Approve multiple assignments in metadatacard opened through taskpanel menu that has both class [All must approve and Any can approve]")
	public void SprintTest172015_3_32C(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			String assigName1 =  Utility.getObjectName(methodName) + "_1";
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentAllMustApprove.Value); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName1); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment (" + assigName1 + ") is created with class (" + Caption.Classes.AssignmentAllMustApprove.Value + ") is crated.", driver);

			//Step-3 : Create an assignment with one assigned to user
			//-------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName2 =  Utility.getObjectName(methodName).toString() + "_2"; //Name of the object with current method date & time
			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentAnyoneCanApprove.Value); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName2); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Assignment (" + assigName2 + ") is created with class (" + Caption.Classes.AssignmentAnyoneCanApprove.Value + ") is crated.", driver);

			//Step-4 : Open the Properties dialog of the assignment and document through taskpanel menu
			//----------------------------------------------------------------------------------------
			String assignName = assigName1 + "\n" + assigName2;
			homePage.listView.clickMultipleItems(assignName);
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from taskpanel menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of document and assignment is opened through taskpanel menu.");

			//Step-5 : Select Approve icon for the multi-selected assignments
			//---------------------------------------------------------------
			if (!metadatacard.clickApproveIcon())
				throw new Exception("Approve icon is not selected.");

			Log.message("5. Approve icon is selected");

			//Verification : Verifies if assigned to property has value varies
			//----------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves the metadatacard

			if (ListView.isApprovedByItemName(driver, assigName1) && ListView.isApprovedByItemName(driver, assigName2)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignments with two different classes are approved in metadatacard opened through taskpanel menu.");
			else
				Log.fail("Test case Failed. Assignments with two different classes are not approved in metadatacard opened through taskpanel menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_32C

	/**
	 * 172015.3.32D : User should be able to Approve multiple assignments in metadatacard opened in rightpanel that has both class [All must approve and Any can approve]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "User should be able to Approve multiple assignments in metadatacard opened in rightpanel that has both class [All must approve and Any can approve]")
	public void SprintTest172015_3_32D(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			String assigName1 =  Utility.getObjectName(methodName) + "_1";
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentAllMustApprove.Value); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName1); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment (" + assigName1 + ") is created with class (" + Caption.Classes.AssignmentAllMustApprove.Value + ") is crated.", driver);

			//Step-3 : Create an assignment with one assigned to user
			//-------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName2 =  Utility.getObjectName(methodName).toString() + "_2"; //Name of the object with current method date & time
			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentAnyoneCanApprove.Value); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName2); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Assignment (" + assigName2 + ") is created with class (" + Caption.Classes.AssignmentAnyoneCanApprove.Value + ") is crated.", driver);

			//Step-4 : Open the Properties dialog of the assignment and document through taskpanel menu
			//----------------------------------------------------------------------------------------
			String assignName = assigName1 + "\n" + assigName2;
			homePage.listView.clickMultipleItems(assignName);

			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of document and assignment is opened through taskpanel menu.");

			//Step-5 : Select Approve icon for the multi-selected assignments
			//---------------------------------------------------------------
			if (!metadatacard.clickApproveIcon())
				throw new Exception("Approve icon is not selected.");

			Log.message("5. Approve icon is selected");

			//Verification : Verifies if assigned to property has value varies
			//----------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves the metadatacard

			if (ListView.isApprovedByItemName(driver, assigName1) && ListView.isApprovedByItemName(driver, assigName2)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignments with two different classes are approved in metadatacard opened in rightpane.");
			else
				Log.fail("Test case Failed. Assignments with two different classes are not approved in metadatacard opened in rightpane.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_32D

	/**
	 * 172015.3.32E : User should be able to Approve multiple assignments in metadatacard opened in rightpanel that has both class [All must approve and Any can approve]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "User should be able to Approve multiple assignments in metadatacard opened in rightpanel that has both class [All must approve and Any can approve]")
	public void SprintTest172015_3_32E(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");
			String objName =  Utility.getObjectName(methodName);

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Create an assignment with two assigned to users
			//--------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName1 =  objName + "_1";
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentAllMustApprove.Value); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName1); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment (" + assigName1 + ") is created with class (" + Caption.Classes.AssignmentAllMustApprove.Value + ") is crated.", driver);

			//Step-3 : Create an assignment with one assigned to user
			//-------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName2 =  objName + "_2"; //Name of the object with current method date & time
			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentAnyoneCanApprove.Value); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName2); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Assignment (" + assigName2 + ") is created with class (" + Caption.Classes.AssignmentAnyoneCanApprove.Value + ") is crated.", driver);

			//Step-4 : Select the assignments of different class
			//---------------------------------------------------
			homePage.searchPanel.search(objName, Caption.Search.SearchOnlyAssignments.Value);
			String assignName = assigName1 + "\n" + assigName2;
			homePage.listView.clickMultipleItems(assignName);

			Log.message("4. Assignments of different class are selected.");

			//Step-5 : Select Approve icon for the multi-selected assignments
			//---------------------------------------------------------------
			homePage.taskPanel.markApproveReject(Caption.MenuItems.Approve.Value); //Marks Approve from Taskpanel

			Log.message("5. Approve is selected from taskapnel.");

			//Verification : Verifies if assignments are approved
			//-----------------------------------------------------
			if (ListView.isApprovedByItemName(driver, assigName1) && ListView.isApprovedByItemName(driver, assigName2)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignments with two different classes are approved in metadatacard opened in rightpane.");
			else
				Log.fail("Test case Failed. Assignments with two different classes are not approved in metadatacard opened in rightpane.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_32E

	/**
	 * Commented cases are obsolete due to the CR: Varies will be displayed in the metadata card for the multi selected objects
	 * If property is not exist in all the selected object, metatdata card will display as Varies only	 * 
	 * 
	 * 104.1.47.1A : Approve the assignment in metadatacard on multi-selecting assignment with document through context menu
	 *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Approve the assignment in metadatacard on multi-selecting assignment with document through context menu")
	public void SprintTest104_1_47_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		Boolean displayMode = false;

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
			String objName = Utility.getObjectName(methodName);
			String assigName = objName + "_A";
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment (" + assigName + ") is created.", driver);

			//Step-3 : Create a document with one assigned to user
			//----------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Document.Value); //Clicks New Assignment from the menu bar
			String docName = objName + "_D";
			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setTemplate(dataPool.get("Extension"));
			metadatacard = new MetadataCard(driver);
			metadatacard.setPropertyValue("Class", dataPool.get("DocClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", docName); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Document(" + docName + ")  is created.", driver);

			//Step-4 : Open the Properties dialog of the assignment and document through context menu
			//----------------------------------------------------------------------------------------
			homePage.searchPanel.search(objName, Caption.Search.SearchAllObjects.Value);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.GrpObjByObjType.Value);
			displayMode = true;
			String assignName = assigName + "\n" + docName + "." + dataPool.get("Extension");
			homePage.listView.clickMultipleItems(assignName);
			homePage.listView.rightClickItem(docName+"." + dataPool.get("Extension"));
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of document and assignment is opened through context menu.", driver);

			//Step-5 : Select Approve icon for the multi-selected assignments
			//---------------------------------------------------------------
			if (!metadatacard.clickApproveIcon())
				throw new Exception("Approve icon is not selected.");

			Log.message("5. Approve icon is selected", driver);

			//Verification : Verifies if assigned to property has value varies
			//----------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves the metadatacard

			if (ListView.isApprovedByItemName(driver, assigName)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment is approved on selecting it with document in metadatacard opened through context menu.", driver);
			else
				Log.fail("Test case Failed. Assignment is not approved on selecting it with document in metadatacard opened through context menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			if (displayMode) {
				HomePage homePage = new HomePage(driver);
				homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.GrpObjByObjType.Value);
			}

			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_47_1A

	  *//**
	  * 104.1.47.1B : Approve the assignment in metadatacard on multi-selecting assignment with document through operations menu
	  *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Approve the assignment in metadatacard on multi-selecting assignment with document through operations menu")
	public void SprintTest104_1_47_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		boolean displayMode = false;

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
			String objName = Utility.getObjectName(methodName);
			String assigName = objName + "_A";
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment (" + assigName + ") is created.", driver);

			//Step-3 : Create a document with one assigned to user
			//----------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Document.Value); //Clicks New Assignment from the menu bar
			String docName = objName + "_D";
			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setTemplate(dataPool.get("Extension"));
			metadatacard = new MetadataCard(driver);
			metadatacard.setPropertyValue("Class", dataPool.get("DocClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", docName); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Document(" + docName + ")  is created.", driver);

			//Step-4 : Open the Properties dialog of the assignment and document through context menu
			//----------------------------------------------------------------------------------------
			homePage.searchPanel.search(objName, Caption.Search.SearchAllObjects.Value);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.GrpObjByObjType.Value);
			displayMode = true;
			String assignName = assigName + "\n" + docName+"."+dataPool.get("Extension");
			homePage.listView.clickMultipleItems(assignName);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from operations menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of document and assignment is opened through operations menu.", driver);

			//Step-5 : Select Approve icon for the multi-selected assignments
			//---------------------------------------------------------------
			if (!metadatacard.clickApproveIcon())
				throw new Exception("Approve icon is not selected.");

			Log.message("5. Approve icon is selected", driver);

			//Verification : Verifies if assigned to property has value varies
			//----------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves the metadatacard

			if (ListView.isApprovedByItemName(driver, assigName)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment is approved on selecting it with document in metadatacard opened through operations menu.", driver);
			else
				Log.fail("Test case Failed. Assignment is not approved on selecting it with document in metadatacard opened through operations menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (displayMode) {
				HomePage homePage = new HomePage(driver);
				homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.GrpObjByObjType.Value);
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_47_1B

	   *//**
	   * 104.1.47.1C : Approve the assignment in metadatacard on multi-selecting assignment with document through taskpanel menu
	   *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Approve the assignment in metadatacard on multi-selecting assignment with document through taskpanel menu")
	public void SprintTest104_1_47_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		boolean displayMode = false;

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
			String objName = Utility.getObjectName(methodName);
			String assigName = objName + "_A";
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment (" + assigName + ") is created.", driver);

			//Step-3 : Create a document with one assigned to user
			//----------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Document.Value); //Clicks New Assignment from the menu bar
			String docName = objName + "_D";
			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setTemplate(dataPool.get("Extension"));
			metadatacard = new MetadataCard(driver);
			metadatacard.setPropertyValue("Class", dataPool.get("DocClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", docName); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Document(" + docName + ")  is created.", driver);

			//Step-4 : Open the Properties dialog of the assignment and document through context menu
			//----------------------------------------------------------------------------------------
			homePage.searchPanel.search(objName, Caption.Search.SearchAllObjects.Value);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.GrpObjByObjType.Value);
			displayMode = true;
			String assignName = assigName + "\n" + docName+"."+dataPool.get("Extension");
			homePage.listView.clickMultipleItems(assignName);
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from taskpanel menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of document and assignment is opened through taskpanel menu.", driver);

			//Step-5 : Select Approve icon for the multi-selected assignments
			//---------------------------------------------------------------
			if (!metadatacard.clickApproveIcon())
				throw new Exception("Approve icon is not selected.");

			Log.message("5. Approve icon is selected", driver);

			//Verification : Verifies if assigned to property has value varies
			//----------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves the metadatacard

			if (ListView.isApprovedByItemName(driver, assigName)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment is approved on selecting it with document in metadatacard opened through taskpanel menu.", driver);
			else
				Log.fail("Test case Failed. Assignment is not approved on selecting it with document in metadatacard opened through taskpanel menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (displayMode) {
				HomePage homePage = new HomePage(driver);
				homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.GrpObjByObjType.Value);
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_47_1C

	    *//**
	    * 104.1.47.1D : Approve the assignment in metadatacard on multi-selecting assignment with document in Rightpanel
	    *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Approve the assignment in metadatacard on multi-selecting assignment with document in Rightpanel")
	public void SprintTest104_1_47_1D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		boolean displayMode = false;

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
			String objName = Utility.getObjectName(methodName);
			String assigName = objName + "_A";
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment (" + assigName + ") is created.", driver);

			//Step-3 : Create a document with one assigned to user
			//----------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Document.Value); //Clicks New Assignment from the menu bar
			String docName = objName + "_D";
			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setTemplate(dataPool.get("Extension"));
			metadatacard = new MetadataCard(driver);
			metadatacard.setPropertyValue("Class", dataPool.get("DocClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", docName); //Sets the Assignment name
			metadatacard.setCheckInImmediately(true);
			Utils.fluentWait(driver);

			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Document(" + docName + ")  is created.", driver);

			//Step-4 : Open the Properties dialog of the assignment and document Rightpanel
			//------------------------------------------------------------------------------
			homePage.searchPanel.search(objName, Caption.Search.SearchAllObjects.Value);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.GrpObjByObjType.Value);
			displayMode = true;
			String assignName = assigName + "\n" + docName+"."+dataPool.get("Extension");
			homePage.listView.clickMultipleItems(assignName);
			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of document and assignment is opened in rightpanel.", driver);

			//Step-5 : Select Approve icon for the multi-selected assignments
			//---------------------------------------------------------------
			if (!metadatacard.clickApproveIcon())
				throw new Exception("Approve icon is not selected.");

			Log.message("5. Approve icon is selected", driver);

			//Verification : Verifies if assigned to property has value varies
			//----------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves the metadatacard

			if (ListView.isApprovedByItemName(driver, assigName)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment is approved on selecting it with document in rightpanel metadatacard.", driver);
			else
				Log.fail("Test case Failed. Assignment is not approved on selecting it with document in rightpanel metadatacard.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (displayMode) {
				HomePage homePage = new HomePage(driver);
				homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.GrpObjByObjType.Value);
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_47_1D

	     *//**
	     * 104.1.47.1E : Approve the assignment on multi-selecting assignment with document in taskpanel.
	     *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Approve the assignment on multi-selecting assignment with document in taskpanel.")
	public void SprintTest104_1_47_1E(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		boolean displayMode = false;

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
			String objName = Utility.getObjectName(methodName);
			String assigName = objName + "_A";
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setPropertyValue("Class", dataPool.get("ClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("2. New Assignment (" + assigName + ") is created.", driver);

			//Step-3 : Create a document with one assigned to user
			//----------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Document.Value); //Clicks New Assignment from the menu bar
			String docName = objName + "_D";
			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			metadatacard.setTemplate(dataPool.get("Extension"));
			metadatacard = new MetadataCard(driver);
			metadatacard.setPropertyValue("Class", dataPool.get("DocClassName")); //Sets the Assignment name
			metadatacard.setPropertyValue("Name or title", docName); //Sets the Assignment name
			Utils.fluentWait(driver);
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New Document(" + docName + ")  is created.", driver);

			//Step-4 : Open the Properties dialog of the assignment and document Rightpanel
			//------------------------------------------------------------------------------
			homePage.searchPanel.search(objName, Caption.Search.SearchAllObjects.Value);
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.GrpObjByObjType.Value);
			displayMode = true;
			String assignName = assigName + "\n" + docName+"."+dataPool.get("Extension");
			homePage.listView.clickMultipleItems(assignName);

			Log.message("4. Assignment & document object is selected in the list.", driver);

			//Step-5 : Select Approve from taskpanel
			//--------------------------------------
			homePage.taskPanel.markApproveReject(Caption.MenuItems.Approve.Value); //Marks Approve from Taskpanel

			Log.message("5. Approve icon is selected in taskpanel.", driver);

			//Verification : Verifies if assigned to property has value varies
			//----------------------------------------------------------------
			if (ListView.isApprovedByItemName(driver, assigName)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment is approved through taskpanel on selecting it with document.", driver);
			else
				Log.fail("Test case Failed. Assignment is not approved through taskpanel on selecting it with document.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (displayMode) {
				HomePage homePage = new HomePage(driver);
				homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.GrpObjByObjType.Value);
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_47_1E
	      */

} //End Class Assignments
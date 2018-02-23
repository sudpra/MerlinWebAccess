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
public class MultiSelectReject {

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
	 * 104.1.33.1A : Verify if Reject icon is displayed for multiselected  assignment (SHIFT key) object that is assigneed to logged in user  - Context menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Verify if Reject icon is displayed for multiselected  assignment (SHIFT key) object that is assigneed to logged in user  - Context menu Properties")
	public void SprintTest104_1_33_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			///Step-1 : Create an assignment with two assigned to users
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

			Log.message("1. New " + rand + "Assignments are created.");

			//Step-2 : Open the Properties dialog of the new assignments in right pane
			//------------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyAssignments.Value, objName);
			homePage.listView.shiftclickMultipleItemsByIndex(0, rand-1); //Select multiple items by shift key
			homePage.listView.rightClickItemByIndex(rand-1); //Right clicks the item
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. Properties dialog of multi selected assignments using SHIFT key is opened through context menu.");

			//Step-3 : Click Reject button in the metadatacard
			//-------------------------------------------------
			if (!metadatacard.clickRejectIcon())
				throw new Exception("Reject Icon is not clicked.");

			Log.message("3. Reject icon in the Assignment is clicked in metadatacard for mutli-selected assignments.", driver);

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.

			String strDiff = "";

			for (int i=0; i<rand; i++) 
				if (!ListView.getRejectedByItemIndex(driver, i).equalsIgnoreCase(userFullName))
					strDiff = strDiff + "Assignment : " + homePage.listView.getItemNameByItemIndex(i);

			if (strDiff.equals("")) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Multi selected assignment objects are rejected.");
			else
				Log.fail("Test case Failed. One or more multi-selected assignment is not rejected. Assignments are " + strDiff, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_33_1A

	/**
	 * 104.1.33.1B : Verify if Reject icon is displayed for multiselected  assignment (SHIFT key) object that is assigneed to logged in user  - Operations menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Verify if Reject icon is displayed for multiselected  assignment (SHIFT key) object that is assigneed to logged in user  - Operations menu Properties")
	public void SprintTest104_1_33_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			Log.message("1. New " + rand + "Assignments are created.");

			//Step-2 : Open the Properties dialog of the new assignments in right pane
			//------------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyAssignments.Value, objName);
			homePage.listView.shiftclickMultipleItemsByIndex(0, rand-1); //Select multiple items by shift key
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from operations menu

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. Properties dialog of multi selected assignments using SHIFT key is opened through operations menu.");

			//Step-3 : Click Reject button in the metadatacard
			//-------------------------------------------------
			if (!metadatacard.clickRejectIcon())
				throw new Exception("Reject Icon is not clicked.");

			Log.message("3. Reject icon in the Assignment is clicked in metadatacard for mutli-selected assignments.", driver);

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.

			String strDiff = "";

			for (int i=0; i<rand; i++) 
				if (!ListView.getRejectedByItemIndex(driver, i).equalsIgnoreCase(userFullName))
					strDiff = strDiff + "Assignment : " + homePage.listView.getItemNameByItemIndex(i);

			if (strDiff.equals("")) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Multi selected assignment objects are rejected.");
			else
				Log.fail("Test case Failed. One or more multi-selected assignment is not rejected. Assignments are " + strDiff, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_33_1B

	/**
	 * 104.1.33.1C : Verify if Reject icon is displayed for multiselected  assignment (SHIFT key) object that is assigneed to logged in user  - Taskpanel Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Verify if Reject icon is displayed for multiselected  assignment (SHIFT key) object that is assigneed to logged in user  - Taskpanel Properties")
	public void SprintTest104_1_33_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			Log.message("1. New " + rand + "Assignments are created.");

			//Step-2 : Open the Properties dialog of the new assignments in right pane
			//------------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyAssignments.Value, objName);
			homePage.listView.shiftclickMultipleItemsByIndex(0, rand-1); //Select multiple items by shift key
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from taskpanel

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. Properties dialog of multi selected assignments using SHIFT key is opened through taskpanel.");

			//Step-3 : Click Reject button in the metadatacard
			//-------------------------------------------------
			if (!metadatacard.clickRejectIcon())
				throw new Exception("Reject Icon is not clicked.");

			Log.message("3. Reject icon in the Assignment is clicked in metadatacard for mutli-selected assignments.", driver);

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.

			String strDiff = "";

			for (int i=0; i<rand; i++) 
				if (!ListView.getRejectedByItemIndex(driver, i).equalsIgnoreCase(userFullName))
					strDiff = strDiff + "Assignment : " + homePage.listView.getItemNameByItemIndex(i);

			if (strDiff.equals("")) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Multi selected assignment objects are rejected.");
			else
				Log.fail("Test case Failed. One or more multi-selected assignment is not rejected. Assignments are " + strDiff, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_33_1C

	/**
	 * 104.1.33.1D : Verify if Reject icon is displayed for multiselected  assignment (SHIFT key) object that is assigneed to logged in user  - Rightpane Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Verify if Reject icon is displayed for multiselected  assignment (SHIFT key) object that is assigneed to logged in user  - Rightpane Properties")
	public void SprintTest104_1_33_1D(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			Log.message("1. New " + rand + "Assignments are created.");

			//Step-2 : Open the Properties dialog of the new assignments in right pane
			//------------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyAssignments.Value, objName);
			homePage.listView.shiftclickMultipleItemsByIndex(0, rand-1); //Select multiple items by shift key
			MetadataCard metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("2. Properties dialog of multi selected assignments using SHIFT key is opened in rightpane.");

			//Step-3 : Click Reject button in the metadatacard
			//-------------------------------------------------
			if (!metadatacard.clickRejectIcon())
				throw new Exception("Reject Icon is not clicked.");

			Log.message("3. Reject icon in the Assignment is clicked in metadatacard for mutli-selected assignments.", driver);

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.

			String strDiff = "";

			for (int i=0; i<rand; i++) 
				if (!ListView.getRejectedByItemIndex(driver, i).equalsIgnoreCase(userFullName))
					strDiff = strDiff + "Assignment : " + homePage.listView.getItemNameByItemIndex(i);

			if (strDiff.equals("")) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Multi selected assignment objects are rejected.");
			else
				Log.fail("Test case Failed. One or more multi-selected assignment is not rejected. Assignments are " + strDiff, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_33_1D

	/**
	 * 104.1.33.1E : Verify if Reject text is displayed for multiselected  assignment (SHIFT key) object that is assigneed to logged in user in taskpanel
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Verify if Reject text is displayed for multiselected  assignment (SHIFT key) object that is assigneed to logged in user in taskpanel")
	public void SprintTest104_1_33_1E(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			Log.message("1. New " + rand + "Assignments are created.");

			//Step-2 : Open the Properties dialog of the new assignments in right pane
			//------------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyAssignments.Value, objName);
			homePage.listView.shiftclickMultipleItemsByIndex(0, rand-1); //Select multiple items by shift key

			Log.message("2. Assignments are multi selected using SHIFT key.");

			//Step-3 : Click Reject button in the taskpanel
			//-------------------------------------------------
			homePage.taskPanel.markApproveReject("Reject");

			Log.message("3. Reject is clicked in taskpanel for mutli-selected assignments.", driver);

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			String strDiff = "";

			for (int i=0; i<rand; i++) 
				if (!ListView.getRejectedByItemIndex(driver, i).equalsIgnoreCase(userFullName))
					strDiff = strDiff + "Assignment : " + homePage.listView.getItemNameByItemIndex(i);

			if (strDiff.equals("")) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Multi selected assignment objects are rejected.");
			else
				Log.fail("Test case Failed. One or more multi-selected assignment is not rejected. Assignments are " + strDiff, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_33_1E

	/**
	 * 104.1.33.3A : Verify if Reject icon is displayed for multiselected  assignment (CTRL key) object that is assigneed to logged in user  - Context menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Verify if Reject icon is displayed for multiselected  assignment (CTRL key) object that is assigneed to logged in user  - Context menu Properties")
	public void SprintTest104_1_33_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			Log.message("1. New Assignment are created with two assigned to users.");

			//Step-2 : Open the Properties dialog of the assignment through context menu
			//--------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyAssignments.Value, objName);
			String items = homePage.listView.multiSelectRightClickByCtrlKey(noOfItems); //Select multiple items by ctrl key
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. Properties dialog of multi selected assignments using CTRL key is opened through context menu.");

			String[] selectedItems = items.split("::");

			//Step-3 : Click Reject button in the metadatacard
			//-------------------------------------------------
			if (!metadatacard.clickRejectIcon())
				throw new Exception("Reject Icon is not clicked.");

			Log.message("3. Reject icon in the Assignment is clicked in metadatacard for mutli-selected assignments.", driver);

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.

			String strDiff = "";

			for (int i=0; i<selectedItems.length; i++) 
				if (!ListView.getRejectedByItemName(driver, selectedItems[i]).equalsIgnoreCase(userFullName))
					strDiff = strDiff + "Assignment : " + homePage.listView.getItemNameByItemIndex(i);

			if (strDiff.equals("")) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Multi selected assignment objects are rejected.");
			else
				Log.fail("Test case Failed. One or more multi-selected assignment is not rejected. Assignments are " + strDiff, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_33_3A

	/**
	 * 104.1.33.3B : Verify if Reject icon is displayed for multiselected  assignment (CTRL key) object that is assigneed to logged in user  - Operations menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Verify if Reject icon is displayed for multiselected  assignment (CTRL key) object that is assigneed to logged in user  - Operations menu Properties")
	public void SprintTest104_1_33_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			Log.message("1. New Assignments are created with two assigned to users.");

			//Step-2 : Open the Properties dialog of the assignment through operations menu
			//-----------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyAssignments.Value, objName);
			String items = homePage.listView.multiSelectByCtrlKey(noOfItems); //Select multiple items by ctrl key
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from operations menu

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. Properties dialog of multi selected assignments using CTRL key is opened through operations menu.");

			String[] selectedItems = items.split("::");

			//Step-3 : Click Reject button in the metadatacard
			//-------------------------------------------------
			if (!metadatacard.clickRejectIcon())
				throw new Exception("Reject Icon is not clicked.");

			Log.message("3. Reject icon in the Assignment is clicked in metadatacard for mutli-selected assignments.", driver);

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.

			String strDiff = "";

			for (int i=0; i<noOfItems; i++) 
				if (!ListView.getRejectedByItemName(driver, selectedItems[i]).equalsIgnoreCase(userFullName))
					strDiff = strDiff + "Assignment : " + homePage.listView.getItemNameByItemIndex(i);

			if (strDiff.equals("")) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Multi selected assignment objects are rejected.");
			else
				Log.fail("Test case Failed. One or more multi-selected assignment is not rejected. Assignments are " + strDiff, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_33_3B

	/**
	 * 104.1.33.3C : Verify if Reject icon is displayed for multiselected  assignment (CTRL key) object that is assigneed to logged in user  - Taskpanel Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Verify if Reject icon is displayed for multiselected  assignment (CTRL key) object that is assigneed to logged in user  - Taskpanel Properties")
	public void SprintTest104_1_33_3C(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			Log.message("1. New Assignments are created with two assigned to users.");

			//Step-2 : Open the Properties dialog of the assignment through taskpanel menu
			//-----------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyAssignments.Value, objName);
			String items = homePage.listView.multiSelectByCtrlKey(noOfItems); //Select multiple items by ctrl key
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from taskpanel

			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. Properties dialog of multi selected assignments using CTRL key is opened through taskpanel.");

			String[] selectedItems = items.split("::");			

			//Step-3 : Click Reject button in the metadatacard
			//-------------------------------------------------
			if (!metadatacard.clickRejectIcon())
				throw new Exception("Reject Icon is not clicked.");

			Log.message("3. Reject icon in the Assignment is clicked in metadatacard for mutli-selected assignments.", driver);

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.

			String strDiff = "";

			for (int i=0; i<noOfItems; i++) 
				if (!ListView.getRejectedByItemName(driver, selectedItems[i]).equalsIgnoreCase(userFullName))
					strDiff = strDiff + "Assignment : " + homePage.listView.getItemNameByItemIndex(i);

			if (strDiff.equals("")) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Multi selected assignment objects are rejected.");
			else
				Log.fail("Test case Failed. One or more multi-selected assignment is not rejected. Assignments are " + strDiff, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_33_3C

	/**
	 * 104.1.33.3D : Verify if Reject icon is displayed for multiselected  assignment (CTRL key) object that is assigneed to logged in user  - Rightpane Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint104"}, 
			description = "Verify if Reject icon is displayed for multiselected  assignment (CTRL key) object that is assigneed to logged in user  - Rightpane Properties")
	public void SprintTest104_1_33_3D(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			Log.message("1. New Assignments are created with two assigned to users.");

			//Step-2 : Open the Properties dialog of the assignment through in rightpane
			//-----------------------------------------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyAssignments.Value, objName);
			String items = homePage.listView.multiSelectByCtrlKey(noOfItems); //Select multiple items by ctrl key

			MetadataCard metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("2. Properties dialog of multi selected assignments using CTRL key is opened in rightpane.");

			String[] selectedItems = items.split("::");						

			//Step-3 : Click Reject button in the metadatacard
			//-------------------------------------------------
			if (!metadatacard.clickRejectIcon())
				throw new Exception("Reject Icon is not clicked.");

			Log.message("3. Reject icon in the Assignment is clicked in metadatacard for mutli-selected assignments.", driver);

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves and closes the metadatacard.

			String strDiff = "";

			for (int i=0; i<noOfItems; i++) 
				if (!ListView.getRejectedByItemName(driver, selectedItems[i]).equalsIgnoreCase(userFullName))
					strDiff = strDiff + "Assignment : " + homePage.listView.getItemNameByItemIndex(i);

			if (strDiff.equals("")) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Multi selected assignment objects are rejected.");
			else
				Log.fail("Test case Failed. One or more multi-selected assignment is not rejected. Assignments are " + strDiff, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_33_3D

	/**
	 * 104.1.33.3E : Verify if Reject icon is displayed for multiselected  assignment (CTRL key) object that is assigneed to logged in user in taskpanel
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint104"}, 
			description = "Verify if Reject icon is displayed for multiselected  assignment (CTRL key) object that is assigneed to logged in user in taskpanel")
	public void SprintTest104_1_33_3E(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			Log.message("1. New Assignments are created with two assigned to users.");

			//Step-2 : Multi-Select the assignments
			//-------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchOnlyAssignments.Value, objName);
			String items = homePage.listView.multiSelectByCtrlKey(noOfItems); //Select multiple items by ctrl key

			Log.message("2. Assignments are multi-selected using CTRL key.");

			String[] selectedItems = items.split("::");	

			//Step-3 : Click Reject button in the taskpanel
			//-------------------------------------------------
			homePage.taskPanel.markApproveReject("Reject"); //Selects Reject from taskpanel

			Log.message("3. Reject button is selected in taskpanel for multi-selected assignments.", driver);

			//Verification : Verifies if assignment approved by metadatacard properties
			//-------------------------------------------------------------------------
			String strDiff = "";

			for (int i=0; i<noOfItems; i++) 
				if (!ListView.getRejectedByItemName(driver, selectedItems[i]).equalsIgnoreCase(userFullName))
					strDiff = strDiff + "Assignment : " + homePage.listView.getItemNameByItemIndex(i);

			if (strDiff.equals("")) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Multi selected assignment objects are rejected.");
			else
				Log.fail("Test case Failed. One or more multi-selected assignment is not rejected. Assignments are " + strDiff, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_33_3E

	/**
	 * 104.1.41A : Reject the multiselected assignment object which has same and different user - Context Menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Reject the multiselected assignment object which has same and different user - Context Menu Properties")
	public void SprintTest104_1_41A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-5 : Select Reject icon for the multi-selected assignments
			//---------------------------------------------------------------
			int propIndex = metadatacard.getPropertyValueIndex("Assigned to", dataPool.get("UserFullName1"));

			if (!metadatacard.clickRejectIcon(propIndex))
				throw new Exception("Reject icon is not selected.");

			Log.message("5. Reject icon is selected", driver);

			//Verification : Verifies if assignment is rejected
			//-------------------------------------------------
			metadatacard.saveAndClose(); //Saves the metadatacard

			if (ListView.isRejectedByItemName(driver, assigName1) && ListView.isRejectedByItemName(driver, assigName2)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Mutliselected assignments with same and various assigned to property is rejected.");
			else
				Log.fail("Test case Failed. Mutliselected assignments with same and various assigned to property is not rejected.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_41A

	/**
	 * 104.1.41B : Reject the multiselected assignment object which has same and different user - operations Menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Reject the multiselected assignment object which has same and different user - operations Menu Properties")
	public void SprintTest104_1_41B(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-5 : Select Reject icon for the multi-selected assignments
			//---------------------------------------------------------------
			int propIndex = metadatacard.getPropertyValueIndex("Assigned to", dataPool.get("UserFullName1"));

			if (!metadatacard.clickRejectIcon(propIndex))
				throw new Exception("Reject icon is not selected.");

			Log.message("5. Reject icon is selected");

			//Verification : Verifies if assignment is rejected
			//-------------------------------------------------
			metadatacard.saveAndClose(); //Saves the metadatacard

			if (ListView.isRejectedByItemName(driver, assigName1) && ListView.isRejectedByItemName(driver, assigName2)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Mutliselected assignments with same and various assigned to property is rejected.");
			else
				Log.fail("Test case Failed. Mutliselected assignments with same and various assigned to property is not rejected.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_41B

	/**
	 * 104.1.41C : Reject the multiselected assignment object which has same and different user - Taskpane Menu Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Reject the multiselected assignment object which has same and different user - Taskpane Menu Properties")
	public void SprintTest104_1_41C(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-5 : Select Reject icon for the multi-selected assignments
			//---------------------------------------------------------------
			int propIndex = metadatacard.getPropertyValueIndex("Assigned to", dataPool.get("UserFullName1"));

			if (!metadatacard.clickRejectIcon(propIndex))
				throw new Exception("Reject icon is not selected.");

			Log.message("5. Reject icon is selected");

			//Verification : Verifies if assignment is rejected
			//-------------------------------------------------
			metadatacard.saveAndClose(); //Saves the metadatacard

			if (ListView.isRejectedByItemName(driver, assigName1) && ListView.isRejectedByItemName(driver, assigName2)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Mutliselected assignments with same and various assigned to property is rejected.");
			else
				Log.fail("Test case Failed. Mutliselected assignments with same and various assigned to property is not rejected.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_41C

	/**
	 * 104.1.41D : Reject the multiselected assignment object which has same and different user - Rightpane Properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"},  
			description = "Reject the multiselected assignment object which has same and different user - Rightpane Properties")
	public void SprintTest104_1_41D(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-5 : Select Reject icon for the multi-selected assignments
			//---------------------------------------------------------------
			int propIndex = metadatacard.getPropertyValueIndex("Assigned to", dataPool.get("UserFullName1"));

			if (!metadatacard.clickRejectIcon(propIndex))
				throw new Exception("Reject icon is not selected.");

			Log.message("5. Reject icon is selected");

			//Verification : Verifies if assignment is rejected
			//-------------------------------------------------
			metadatacard.saveAndClose(); //Saves the metadatacard

			if (ListView.isRejectedByItemName(driver, assigName1) && ListView.isRejectedByItemName(driver, assigName2)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Mutliselected assignments with same and various assigned to property is rejected.");
			else
				Log.fail("Test case Failed. Mutliselected assignments with same and various assigned to property is not rejected.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_41D

	/**
	 * 104.1.41E : Reject the multiselected assignment object which has same and different user through taskpanel
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Reject the multiselected assignment object which has same and different user - through taskpanel")
	public void SprintTest104_1_41E(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-4 : Multi select the assignment with various mutlipe assigned to users 
			//---------------------------------------------------------------------------
			String assignName = assigName1 + "\n" + assigName2;
			homePage.listView.clickMultipleItems(assignName);

			Log.message("4. Assignments with various assigned to users is selected.");

			//Step-5 : Select Reject for the multi-selected assignments from taskpanel
			//---------------------------------------------------------------
			homePage.taskPanel.markApproveReject("Reject"); //Selects Reject from taskpanel//Clicks Approve from taskpanel

			Log.message("5. Reject is selected from taskpanel.");

			//Verification : Verifies if assignment is rejected
			//-------------------------------------------------
			if (ListView.isRejectedByItemName(driver, assigName1) && ListView.isRejectedByItemName(driver, assigName2)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Mutliselected assignments with same and various assigned to property is rejected.");
			else
				Log.fail("Test case Failed. Mutliselected assignments with same and various assigned to property is not rejected.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_41E



	/**
	 * 104.1.49A : Discard the rejected multi-selected assignment in metadatacard opened through context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Discard the rejected multi-selected assignment in metadatacard opened through context menu")
	public void SprintTest104_1_49A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-5 : Select Reject icon for the multi-selected assignments
			//---------------------------------------------------------------
			if (!metadatacard.clickRejectIcon())
				throw new Exception("Reject icon is not selected.");

			Log.message("5. Reject icon is selected");

			//Step-6 : Click Discard button
			//-----------------------------
			metadatacard.clickDiscardButton(); //Clicks Discard button

			Log.message("6. Discard button is clicked ", driver);

			//Verification : Verifies if multiselected assignment is not rejected
			//-------------------------------------------------------------------
			if (!ListView.isRejectedByItemName(driver, assigName2) && !ListView.isRejectedByItemName(driver, assigName1)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment is not rejected on discarding the multi-selected rejected assignment in metadatacard properties opened through context menu.");
			else
				Log.fail("Test case Failed. Assignment is rejected on discarding the multi-selected rejected assignment in metadatacard properties opened through context menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_49A

	/**
	 * 104.1.49B : Discard the rejected multi-selected assignment in metadatacard opened through operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint104"}, 
			description = "Discard the rejected multi-selected assignment in metadatacard opened through operations menu")
	public void SprintTest104_1_49B(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-5 : Select Reject icon for the multi-selected assignments
			//---------------------------------------------------------------
			if (!metadatacard.clickRejectIcon())
				throw new Exception("Reject icon is not selected.");

			Log.message("5. Reject icon is selected");

			//Step-6 : Click Discard button
			//-----------------------------
			metadatacard.clickDiscardButton(); //Clicks Discard button

			Log.message("6. Discard button is clicked ");

			//Verification : Verifies if multiselected assignment is not rejected
			//-------------------------------------------------------------------
			if (!ListView.isRejectedByItemName(driver, assigName2) && !ListView.isRejectedByItemName(driver, assigName1)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment is not rejected on discarding the multi-selected rejected assignment in metadatacard properties opened through operations menu.");
			else
				Log.fail("Test case Failed. Assignment is rejected on discarding the multi-selected rejected assignment in metadatacard properties opened through operations menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_49B

	/**
	 * 104.1.49C : Discard the rejected multi-selected assignment in metadatacard opened through taskpanel menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Discard the rejected multi-selected assignment in metadatacard opened through taskpanel menu")
	public void SprintTest104_1_49C(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-5 : Select Reject icon for the multi-selected assignments
			//---------------------------------------------------------------
			if (!metadatacard.clickRejectIcon())
				throw new Exception("Reject icon is not selected.");

			Log.message("5. Reject icon is selected");

			//Step-6 : Click Discard button
			//-----------------------------
			metadatacard.clickDiscardButton(); //Clicks Discard button

			Log.message("6. Discard button is clicked ");

			//Verification : Verifies if multiselected assignment is not rejected
			//-------------------------------------------------------------------
			if (!ListView.isRejectedByItemName(driver, assigName2) && !ListView.isRejectedByItemName(driver, assigName1)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment is not rejected on discarding the multi-selected rejected assignment in metadatacard properties opened through taskpanel menu.");
			else
				Log.fail("Test case Failed. Assignment is rejected on discarding the multi-selected rejected assignment in metadatacard properties opened through taskpanel menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_49C

	/**
	 * 104.1.49D : Discard the rejected multi-selected assignment in metadatacard opened in rightpanel
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Discard the rejected multi-selected assignment in metadatacard opened in rightpanel")
	public void SprintTest104_1_49D(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-5 : Select Reject icon for the multi-selected assignments
			//---------------------------------------------------------------
			if (!metadatacard.clickRejectIcon())
				throw new Exception("Reject icon is not selected.");

			Log.message("5. Reject icon is selected");

			//Step-6 : Click Discard button
			//-----------------------------
			metadatacard.clickDiscardButton(); //Clicks Discard button

			Log.message("6. Discard button is clicked ");

			//Verification : Verifies if multiselected assignment is not rejected
			//-------------------------------------------------------------------
			if (!ListView.isRejectedByItemName(driver, assigName2) && !ListView.isRejectedByItemName(driver, assigName1)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment is not rejected on discarding the multi-selected rejected assignment in metadatacard properties opened in rightpanel.");
			else
				Log.fail("Test case Failed. Assignment is rejected on discarding the multi-selected rejected assignment in metadatacard properties opened in rightpanel.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_49D

	/**
	 * 104.1.53A : Adding comments should be possible in metadatacard for multi-selected assignments opened through context menu after rejecting the assignment
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Adding comments should be possible in metadatacard for multi-selected assignments opened through context menu after rejecting the assignment")
	public void SprintTest104_1_53A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-5 : Select Reject icon for the multi-selected assignments
			//---------------------------------------------------------------
			if (!metadatacard.clickRejectIcon())
				throw new Exception("Reject icon is not selected.");

			Log.message("5. Reject icon is selected");

			//Step-6 : Add comments to the assignment after selecting Reject icon
			//--------------------------------------------------------------------
			String comments = Utility.getObjectName(methodName);
			metadatacard.setComments(comments);

			Log.message("5. Comments is entered assignment after selecting Reject icon.", driver);

			//Step-7 : Save the assignment object
			//-----------------------------------
			metadatacard.saveAndClose(); 

			Log.message("5. Metadatacard is saved after selecting Reject icon and entering comments.");

			//Verification : Verifies if multiselected assignment is not Rejected
			//-------------------------------------------------------------------
			if (!ListView.isRejectedByItemName(driver, assigName2) || !ListView.isRejectedByItemName(driver, assigName1)) //Checks if Assignment completed icon is displayed
				throw new Exception("Assignments are not in Rejected state");

			if (ListView.getCommentsByItemName(driver, assigName1).equalsIgnoreCase(comments) && ListView.getCommentsByItemName(driver, assigName2).equalsIgnoreCase(comments)) //Verifies if comments are as entered
				Log.pass("Test case Passed. Comments entered in metadatacard opened through context menu after clicking Reject icon is saved successfully for mutli-selected assignments.");
			else
				Log.fail("Test case Failed. Comments entered in metadatacard opened through context menu after clicking Reject icon is not saved for mutli-selected assignments.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_53A

	/**
	 * 104.1.53B : Adding comments should be possible in metadatacard for multi-selected assignments opened through operations menu after rejecting the assignment
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Adding comments should be possible in metadatacard for multi-selected assignments opened through operations menu after rejecting the assignment")
	public void SprintTest104_1_53B(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-5 : Select Reject icon for the multi-selected assignments
			//---------------------------------------------------------------
			if (!metadatacard.clickRejectIcon())
				throw new Exception("Reject icon is not selected.");

			Log.message("5. Reject icon is selected", driver);

			//Step-6 : Add comments to the assignment after selecting Reject icon
			//--------------------------------------------------------------------
			String comments = Utility.getObjectName(methodName);
			metadatacard.setComments(comments);

			Log.message("5. Comments is entered assignment after selecting Reject icon.", driver);

			//Step-7 : Save the assignment object
			//-----------------------------------
			metadatacard.saveAndClose(); 

			Log.message("5. Metadatacard is saved after selecting Reject icon and entering comments.");

			//Verification : Verifies if multiselected assignment is not Rejected
			//-------------------------------------------------------------------
			if (!ListView.isRejectedByItemName(driver, assigName2) || !ListView.isRejectedByItemName(driver, assigName1)) //Checks if Assignment completed icon is displayed
				throw new Exception("Assignments are not in Rejected state");

			if (ListView.getCommentsByItemName(driver, assigName1).equalsIgnoreCase(comments) && ListView.getCommentsByItemName(driver, assigName2).equalsIgnoreCase(comments)) //Verifies if comments are as entered
				Log.pass("Test case Passed. Comments entered in metadatacard opened through operations menu after clicking Reject icon is saved successfully for mutli-selected assignments.");
			else
				Log.fail("Test case Failed. Comments entered in metadatacard opened through operations menu after clicking Reject icon is not saved for mutli-selected assignments.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_53B

	/**
	 * 104.1.53C : Adding comments should be possible in metadatacard for multi-selected assignments opened through taskpanel menu after rejecting the assignment
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Adding comments should be possible in metadatacard for multi-selected assignments opened through taskpanel menu after rejecting the assignment")
	public void SprintTest104_1_53C(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-5 : Select Reject icon for the multi-selected assignments
			//---------------------------------------------------------------
			if (!metadatacard.clickRejectIcon())
				throw new Exception("Reject icon is not selected.");

			Log.message("5. Reject icon is selected");

			//Step-6 : Add comments to the assignment after selecting Reject icon
			//--------------------------------------------------------------------
			String comments = Utility.getObjectName(methodName);
			metadatacard.setComments(comments);

			Log.message("5. Comments is entered assignment after selecting Reject icon.", driver);

			//Step-7 : Save the assignment object
			//-----------------------------------
			metadatacard.saveAndClose(); 

			Log.message("6. Metadatacard is saved after selecting Reject icon and entering comments.");

			//Verification : Verifies if multiselected assignment is not Rejected
			//-------------------------------------------------------------------
			if (!ListView.isRejectedByItemName(driver, assigName2) || !ListView.isRejectedByItemName(driver, assigName1)) //Checks if Assignment completed icon is displayed
				throw new Exception("Assignments are not in Rejected state");

			if (ListView.getCommentsByItemName(driver, assigName1).equalsIgnoreCase(comments) && ListView.getCommentsByItemName(driver, assigName2).equalsIgnoreCase(comments)) //Verifies if comments are as entered
				Log.pass("Test case Passed. Comments entered in metadatacard opened through taskpanel menu after clicking Reject icon is saved successfully for mutli-selected assignments.");
			else
				Log.fail("Test case Failed. Comments entered in metadatacard opened through taskpanel menu after clicking Reject icon is not saved for mutli-selected assignments.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_53C

	/**
	 * 104.1.53D : Adding comments should be possible in metadatacard for multi-selected assignments opened in rightpane after rejecting the assignment
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Adding comments should be possible in metadatacard for multi-selected assignments opened in rightpane after rejecting the assignment")
	public void SprintTest104_1_53D(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-5 : Select Reject icon for the multi-selected assignments
			//---------------------------------------------------------------
			if (!metadatacard.clickRejectIcon())
				throw new Exception("Reject icon is not selected.");

			Log.message("5. Reject icon is selected");

			//Step-6 : Add comments to the assignment after selecting Reject icon
			//--------------------------------------------------------------------
			String comments = Utility.getObjectName(methodName);
			metadatacard.setComments(comments);

			Log.message("6. Comments is entered assignment after selecting Reject icon.", driver);

			//Step-7 : Save the assignment object
			//-----------------------------------
			metadatacard.saveAndClose(); 

			Log.message("7. Metadatacard is saved after selecting Reject icon and entering comments.");

			//Verification : Verifies if multiselected assignment is not Rejected
			//-------------------------------------------------------------------
			if (!ListView.isRejectedByItemName(driver, assigName2) || !ListView.isRejectedByItemName(driver, assigName1)) //Checks if Assignment completed icon is displayed
				throw new Exception("Assignments are not in Rejected state");

			if (ListView.getCommentsByItemName(driver, assigName1).equalsIgnoreCase(comments) && ListView.getCommentsByItemName(driver, assigName2).equalsIgnoreCase(comments)) //Verifies if comments are as entered
				Log.pass("Test case Passed. Comments entered in metadatacard opened in rightpane after clicking Reject icon is saved successfully for mutli-selected assignments.");
			else
				Log.fail("Test case Failed. Comments entered in metadatacard opened in rightpane after clicking Reject icon is not saved for mutli-selected assignments.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_53D

	/**
	 * 172015.3.33A : User should be able to Reject multiple assignments in metadatacard opened through context menu that has both class [All must Approve and Any can Approve]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "User should be able to Reject multiple assignments in metadatacard opened through context menu that has both class [All must Approve and Any can Approve]")
	public void SprintTest172015_3_33A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-5 : Select Reject icon for the multi-selected assignments
			//---------------------------------------------------------------
			if (!metadatacard.clickRejectIcon())
				throw new Exception("Reject icon is not selected.");

			Log.message("5. Reject icon is selected");

			//Verification : Verifies if assigned to property has value varies
			//----------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves the metadatacard

			if (ListView.isRejectedByItemName(driver, assigName1) && ListView.isRejectedByItemName(driver, assigName2)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignments with two different classes are Rejected in metadatacard opened through context menu.");
			else
				Log.fail("Test case Failed. Assignments with two different classes are not Rejected in metadatacard opened through context menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_33A

	/**
	 * 172015.3.33B : User should be able to Reject multiple assignments in metadatacard opened through operations menu that has both class [All must Approve and Any can Approve]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "User should be able to Reject multiple assignments in metadatacard opened through operations menu that has both class [All must Approve and Any can Approve]")
	public void SprintTest172015_3_33B(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-5 : Select Reject icon for the multi-selected assignments
			//---------------------------------------------------------------
			if (!metadatacard.clickRejectIcon())
				throw new Exception("Reject icon is not selected.");

			Log.message("5. Reject icon is selected");

			//Verification : Verifies if assigned to property has value varies
			//----------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves the metadatacard

			if (ListView.isRejectedByItemName(driver, assigName1) && ListView.isRejectedByItemName(driver, assigName2)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignments with two different classes are Rejected in metadatacard opened through operations menu.");
			else
				Log.fail("Test case Failed. Assignments with two different classes are not Rejected in metadatacard opened through operations menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_33B

	/**
	 * 172015.3.33C : User should be able to Reject multiple assignments in metadatacard opened through taskpanel menu that has both class [All must Approve and Any can Approve]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "User should be able to Reject multiple assignments in metadatacard opened through taskpanel menu that has both class [All must Approve and Any can Approve]")
	public void SprintTest172015_3_33C(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-5 : Select Reject icon for the multi-selected assignments
			//---------------------------------------------------------------
			if (!metadatacard.clickRejectIcon())
				throw new Exception("Reject icon is not selected.");

			Log.message("5. Reject icon is selected");

			//Verification : Verifies if assigned to property has value varies
			//----------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves the metadatacard

			if (ListView.isRejectedByItemName(driver, assigName1) && ListView.isRejectedByItemName(driver, assigName2)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignments with two different classes are Rejected in metadatacard opened through taskpanel menu.");
			else
				Log.fail("Test case Failed. Assignments with two different classes are not Rejected in metadatacard opened through taskpanel menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_33C

	/**
	 * 172015.3.33D : User should be able to Reject multiple assignments in metadatacard opened in rightpanel that has both class [All must Approve and Any can Approve]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "User should be able to Reject multiple assignments in metadatacard opened in rightpanel that has both class [All must Approve and Any can Approve]")
	public void SprintTest172015_3_33D(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-5 : Select Reject icon for the multi-selected assignments
			//---------------------------------------------------------------
			if (!metadatacard.clickRejectIcon())
				throw new Exception("Reject icon is not selected.");

			Log.message("5. Reject icon is selected");

			//Verification : Verifies if assigned to property has value varies
			//----------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves the metadatacard

			if (ListView.isRejectedByItemName(driver, assigName1) && ListView.isRejectedByItemName(driver, assigName2)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignments with two different classes are Rejected in metadatacard opened in rightpane.");
			else
				Log.fail("Test case Failed. Assignments with two different classes are not Rejected in metadatacard opened in rightpane.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_33D

	/**
	 * 172015.3.33E : User should be able to Reject multiple assignments in metadatacard opened in rightpanel that has both class [All must Approve and Any can Approve]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "User should be able to Reject multiple assignments in metadatacard opened in rightpanel that has both class [All must Approve and Any can Approve]")
	public void SprintTest172015_3_33E(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-4 : Select the assignments of different class
			//---------------------------------------------------
			String assignName = assigName1 + "\n" + assigName2;
			homePage.listView.clickMultipleItems(assignName);

			Log.message("4. Assignments of different class are selected.");

			//Step-5 : Select Reject icon for the multi-selected assignments
			//---------------------------------------------------------------
			homePage.taskPanel.markApproveReject("Reject"); //Selects Reject from taskpanel

			Log.message("5. Reject is selected from taskapnel.");

			//Verification : Verifies if assignments are Rejected
			//-----------------------------------------------------
			if (ListView.isRejectedByItemName(driver, assigName1) && ListView.isRejectedByItemName(driver, assigName2)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignments with two different classes are Rejected in metadatacard opened in rightpane.");
			else
				Log.fail("Test case Failed. Assignments with two different classes are not Rejected in metadatacard opened in rightpane.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest172015_3_33E

	/**
	 * Commented cases are obsolete due to the CR: Varies will be displayed in the metadata card for the multi selected objects
	 * If property is not exist in all the selected object, metatdata card will display as Varies only	 * 
	 * 
	 * 104.1.47.2A : Reject the assignment in metadatacard on multi-selecting assignment with document through context menu
	 *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Reject the assignment in metadatacard on multi-selecting assignment with document through context menu")
	public void SprintTest104_1_47_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			homePage.listView.rightClickItem(docName+".txt");
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of document and assignment is opened through context menu.", driver);

			//Step-5 : Select Reject icon for the multi-selected assignments
			//---------------------------------------------------------------
			if (!metadatacard.clickRejectIcon())
				throw new Exception("Reject icon is not selected.");

			Log.message("5. Reject icon is selected.", driver);

			//Verification : Verifies if assigned to property has value varies
			//----------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves the metadatacard

			if (ListView.isRejectedByItemName(driver, assigName)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment is rejected on selecting it with document in metadatacard opened through context menu.", driver);
			else
				Log.fail("Test case Failed. Assignment is not rejected on selecting it with document in metadatacard opened through context menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if(!driver.equals(null)){
				if (displayMode) {
					HomePage homePage = new HomePage(driver);
					homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.GrpObjByObjType.Value);
				}
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_47_2A

	  *//**
	  * 104.1.47.2B : Reject the assignment in metadatacard on multi-selecting assignment with document through operations menu
	  *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Reject the assignment in metadatacard on multi-selecting assignment with document through operations menu")
	public void SprintTest104_1_47_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value); //Selects Properties from operations menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of document and assignment is opened through operations menu.", driver);

			//Step-5 : Select Reject icon for the multi-selected assignments
			//---------------------------------------------------------------
			if (!metadatacard.clickRejectIcon())
				throw new Exception("Reject icon is not selected.");

			Log.message("5. Reject icon is selected.", driver);

			//Verification : Verifies if assigned to property has value varies
			//----------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves the metadatacard

			if (ListView.isRejectedByItemName(driver, assigName)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment is rejected on selecting it with document in metadatacard opened through operations menu.", driver);
			else
				Log.fail("Test case Failed. Assignment is not rejected on selecting it with document in metadatacard opened through operations menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if(!driver.equals(null)){
				if (displayMode) {
					HomePage homePage = new HomePage(driver);
					homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.GrpObjByObjType.Value);
				}
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_47_2B

	   *//**
	   * 104.1.47.2C : Reject the assignment in metadatacard on multi-selecting assignment with document through taskpanel menu
	   *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Reject the assignment in metadatacard on multi-selecting assignment with document through taskpanel menu")
	public void SprintTest104_1_47_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Selects Properties from operations menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of document and assignment is opened through taskpanel menu.", driver);

			//Step-5 : Select Reject icon for the multi-selected assignments
			//---------------------------------------------------------------
			if (!metadatacard.clickRejectIcon())
				throw new Exception("Reject icon is not selected.");

			Log.message("5. Reject icon is selected.", driver);

			//Verification : Verifies if assigned to property has value varies
			//----------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves the metadatacard

			if (ListView.isRejectedByItemName(driver, assigName)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment is rejected on selecting it with document in metadatacard opened through taskpanel menu.", driver);
			else
				Log.fail("Test case Failed. Assignment is not rejected on selecting it with document in metadatacard opened through taskpanel menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if(!driver.equals(null)){
				if (displayMode) {
					HomePage homePage = new HomePage(driver);
					homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.GrpObjByObjType.Value);
				}
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_47_2C

	    *//**
	    * 104.1.47.2D : Reject the assignment in metadatacard on multi-selecting assignment with document in rightpane
	    *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Reject the assignment in metadatacard on multi-selecting assignment with document in rightpane")
	public void SprintTest104_1_47_2D(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			metadatacard = new MetadataCard(driver, true); //Instantiates Metadatacard wrapper

			Log.message("4. Properties dialog of document and assignment is opened in rightpane.", driver);

			//Step-5 : Select Reject icon for the multi-selected assignments
			//---------------------------------------------------------------
			if (!metadatacard.clickRejectIcon())
				throw new Exception("Reject icon is not selected.");

			Log.message("5. Reject icon is selected.", driver);

			//Verification : Verifies if assigned to property has value varies
			//----------------------------------------------------------------
			metadatacard.saveAndClose(); //Saves the metadatacard

			if (ListView.isRejectedByItemName(driver, assigName)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment is rejected on selecting it with document in metadatacard opened through taskpanel menu.", driver);
			else
				Log.fail("Test case Failed. Assignment is not rejected on selecting it with document in metadatacard opened through taskpanel menu.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if(!driver.equals(null)){
				if (displayMode) {
					HomePage homePage = new HomePage(driver);
					homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.GrpObjByObjType.Value);
				}
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_47_2D

	     *//**
	     * 104.1.47.2E : Reject the assignment on multi-selecting assignment with document in taskpanel.
	     *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "SKIP_MultiSelect", "Sprint104"}, 
			description = "Reject the assignment on multi-selecting assignment with document in taskpanel.")
	public void SprintTest104_1_47_2E(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			Log.message("4. Assignment & document object is selected in the list.", driver);

			//Step-5 : Select Reject from taskpanel
			//--------------------------------------
			homePage.taskPanel.markApproveReject("Reject"); //Selects Reject from taskpanel

			Log.message("5. Reject icon is selected in taskpanel.", driver);

			//Verification : Verifies if assigned to property has value varies
			//----------------------------------------------------------------
			if (ListView.isRejectedByItemName(driver, assigName)) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment is rejected through taskpanel on selecting it with document.", driver);
			else
				Log.fail("Test case Failed. Assignment is not rejected through taskpanel on selecting it with document.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if(!driver.equals(null)){
				if (displayMode) {
					HomePage homePage = new HomePage(driver);
					homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.GrpObjByObjType.Value);
				}
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_47_2E
	      */
} //End Class Assignments
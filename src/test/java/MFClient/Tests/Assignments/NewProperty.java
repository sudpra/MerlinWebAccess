package MFClient.Tests.Assignments;

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
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class NewProperty {

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
	 * 104.1.22A : Verify if user able to save assignment object with newly added property with empty value in metadata card - New menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Verify if user able to save assignment object with newly added property with empty value in metadata card - New menu")
	public void SprintTest104_1_22A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select New Assignment from New menu
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName).toString();
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. New Assignment metadatacard is opened from New menu.", driver);

			//Step-3 : Add new property with empty value to metadatacard
			//----------------------------------------------------------
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentBasicClass.Value); //Sets the class
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.addNewProperty(dataPool.get("PropertyToAdd")); //Adds the property
			ConcurrentHashMap <String, String> prevInfo = metadatacard.getInfo(); //Gets the property information
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New property (" + dataPool.get("PropertyToAdd") + ") with empty value is added to assignment metadatacard and saved.", driver);

			//Step-4 : Open the Properties dialog of the new assignment through context menu
			//------------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			if (!homePage.listView.rightClickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			ConcurrentHashMap <String, String> currInfo = metadatacard.getInfo(); //Gets the property information

			Log.message("4. Properties dialog of the new assignment (" + assigName + ") is opened through context menu.");

			//Verification : Verifies if assignment metadatacard with empty property value is saved successfully
			//---------------------------------------------------------------------------------------------------
			if (Utility.compareObjects(prevInfo, currInfo).equals("")) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment metadatacard with empty property value is saved successfully.");
			else
				Log.fail("Test case Failed. Assignment metadatacard with empty property value is not saved successfully.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_22A

	/**
	 * 104.1.22B : Verify if user able to save assignment object with newly added property with empty value in metadata card - Taskpanel
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Verify if user able to save assignment object with newly added property with empty value in metadata card - Taskpanel")
	public void SprintTest104_1_22B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select New Assignment from taskpane
			//--------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName).toString();
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. New Assignment metadatacard is opened from taskpanel.", driver);

			//Step-3 : Add new property with empty value to metadatacard
			//----------------------------------------------------------
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentBasicClass.Value); //Sets the class
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.addNewProperty(dataPool.get("PropertyToAdd")); //Adds the property
			ConcurrentHashMap <String, String> prevInfo = metadatacard.getInfo(); //Gets the property information
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New property (" + dataPool.get("PropertyToAdd") + ") with empty value is added to assignment metadatacard and saved.", driver);

			//Step-4 : Open the Properties dialog of the new assignment through context menu
			//------------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			if (!homePage.listView.rightClickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			ConcurrentHashMap <String, String> currInfo = metadatacard.getInfo(); //Gets the property information

			Log.message("4. Properties dialog of the new assignment (" + assigName + ") is opened through context menu.");

			//Verification : Verifies if assignment metadatacard with empty property value is saved successfully
			//---------------------------------------------------------------------------------------------------
			if (Utility.compareObjects(prevInfo, currInfo).equals("")) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment metadatacard with empty property value is saved successfully.");
			else
				Log.fail("Test case Failed. Assignment metadatacard with empty property value is not saved successfully.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_22B

	/**
	 * 104.1.22C : Verify if user able to save assignment object with newly added property with filled value in metadata card - New menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Verify if user able to save assignment object with newly added property with filled value in metadata card - New menu")
	public void SprintTest104_1_22C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select New Assignment from New menu
			//--------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName).toString();
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. New Assignment metadatacard is opened from New menu.", driver);

			//Step-3 : Add new property with empty value to metadatacard
			//----------------------------------------------------------
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentBasicClass.Value); //Sets the class
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.addNewProperty(dataPool.get("PropertyToAdd")); //Adds the property
			metadatacard.setPropertyValue(dataPool.get("PropertyToAdd"), dataPool.get("PropertyValue")); //Sets the new property
			ConcurrentHashMap <String, String> prevInfo = metadatacard.getInfo(); //Gets the property information
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New property (" + dataPool.get("PropertyToAdd") + ") with value filled is added to assignment metadatacard and saved.", driver);

			//Step-4 : Open the Properties dialog of the new assignment through context menu
			//------------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			if (!homePage.listView.rightClickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			ConcurrentHashMap <String, String> currInfo = metadatacard.getInfo(); //Gets the property information

			Log.message("4. Properties dialog of the new assignment (" + assigName + ") is opened through context menu.");

			//Verification : Verifies if assignment metadatacard with empty property value is saved successfully
			//---------------------------------------------------------------------------------------------------
			if (Utility.compareObjects(prevInfo, currInfo).equals("")) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment metadatacard with empty property value is saved successfully.");
			else
				Log.fail("Test case Failed. Assignment metadatacard with empty property value is not saved successfully.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_22C

	/**
	 * 104.1.22D : Verify if user able to save assignment object with newly added property with filled value in metadata card - Taskpanel
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint104"}, 
			description = "Verify if user able to save assignment object with newly added property with filled value in metadata card - Taskpanel")
	public void SprintTest104_1_22D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified View
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select New Assignment from taskpanel
			//--------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Assignment.Value); //Clicks New Assignment from the menu bar
			String assigName =  Utility.getObjectName(methodName).toString();
			MetadataCard metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper

			Log.message("2. New Assignment metadatacard is opened from taskpanel.", driver);

			//Step-3 : Add new property with empty value to metadatacard
			//----------------------------------------------------------
			metadatacard.setPropertyValue("Class", Caption.Classes.AssignmentBasicClass.Value); //Sets the class
			metadatacard.setPropertyValue("Name or title", assigName); //Sets the Assignment name
			metadatacard.setPropertyValue("Assigned to", userFullName); //Sets the assigned to property
			metadatacard.addNewProperty(dataPool.get("PropertyToAdd")); //Adds the property
			metadatacard.setPropertyValue(dataPool.get("PropertyToAdd"), dataPool.get("PropertyValue")); //Sets the new property
			ConcurrentHashMap <String, String> prevInfo = metadatacard.getInfo(); //Gets the property information
			metadatacard.saveAndClose(); //Saves and Closes the metadatacard.

			Log.message("3. New property (" + dataPool.get("PropertyToAdd") + ") with value filled is added to assignment metadatacard and saved.", driver);

			//Step-4 : Open the Properties dialog of the new assignment through context menu
			//------------------------------------------------------------------------------
			if (!homePage.listView.isItemExists(assigName)) //Checks if Item exists in the list
				throw new Exception("Newly created assignment (" + assigName + ") does not exists in the list.");

			if (!homePage.listView.rightClickItem(assigName)) //Right clicks the assignment
				throw new Exception("Newly created assignment (" + assigName + ") is not right clicked.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value); //Selects Properties from context menu

			metadatacard = new MetadataCard(driver); //Instantiates Metadatacard wrapper
			ConcurrentHashMap <String, String> currInfo = metadatacard.getInfo(); //Gets the property information

			Log.message("4. Properties dialog of the new assignment (" + assigName + ") is opened through context menu.");

			//Verification : Verifies if assignment metadatacard with empty property value is saved successfully
			//---------------------------------------------------------------------------------------------------
			if (Utility.compareObjects(prevInfo, currInfo).equals("")) //Checks if Assignment completed icon is displayed
				Log.pass("Test case Passed. Assignment metadatacard with empty property value is saved successfully.");
			else
				Log.fail("Test case Failed. Assignment metadatacard with empty property value is not saved successfully.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest104_1_22D

} //End Class Assignments
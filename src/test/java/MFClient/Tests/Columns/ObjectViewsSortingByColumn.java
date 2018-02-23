package MFClient.Tests.Columns;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.Utils;
import genericLibrary.WebDriverUtils;

import java.util.Arrays;
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
import MFClient.Wrappers.ListView;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class ObjectViewsSortingByColumn {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String configURL = null;
	public static String userName = null;
	public static String password = null;
	public static String testVault = null;
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
			userName = xmlParameters.getParameter("UserName");
			password = xmlParameters.getParameter("Password");
			testVault = xmlParameters.getParameter("VaultName");
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
	 * cleanApp : At after class method to destroy the vault used in the class
	 */
	@AfterClass (alwaysRun = true)
	public void cleanApp() throws Exception{

		try {

			Utility.destroyTestVault();

		}//End try

		catch(Exception e){
			throw e;
		}//End Catch
	}//End cleanApp


	/**
	 * 29.7.3A : Verify the column sort of 'Version' column in 'History' view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint29", "Columns"}, 
			description = "Verify the column sort of 'Version' column in 'History' view")
	public void SprintTest29_7_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Search for an object
			//------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			Log.message("1. Searched for an object."); 

			//2. Goto the History view of the specified object
			//-------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object"))) 
				throw new SkipException("The given Object " + dataPool.get("Object") + " was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.History.Value);
			Utils.fluentWait(driver);

			Log.message("2. Goto the History view of the specified object");

			//3. Check the Sort Image
			//------------------------
			if(homePage.listView.itemCount() <= 1)
				throw new SkipException("Invalid Test Data. The given Object " + dataPool.get("Object") + " does not have previous versions.");

			int count = 0;
			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("Column"));

			List<String> versions = homePage.listView.getColumnValues(dataPool.get("Column"));

			for(count = 0; count < versions.size()-1; count++) {
				if(Integer.parseInt(versions.get(count)) < Integer.parseInt(versions.get(count+1)))
					break;
			}

			Log.message("3. Check the Sort Image.");

			//Verification: To verify if the Column is sorted as expected
			//------------------------------------------------------------
			if(sortImage.equals(dataPool.get("ExpectedSortImage")) && count == versions.size()-1)
				Log.pass("Test Case Passed. The Column '" + dataPool.get("Column") + "' had the expected sort.");
			else
				Log.fail("Test Case Failed. The Column '" + dataPool.get("Column") + "' did not have the expected sort.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 29.7.3B : Verify the column sort of 'Version' column in 'History' view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint29", "Columns"}, 
			description = "Verify the column sort of 'Version' column in 'History' view")
	public void SprintTest29_7_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Search for an object
			//------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			Log.message("1. Searched for an object."); 

			//2. Goto the History view of the specified object
			//-------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object"))) 
				throw new SkipException("The given Object " + dataPool.get("Object") + " was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem((Caption.MenuItems.History.Value));
			Utils.fluentWait(driver);

			Log.message("2. Goto the History view of the specified object");

			//3. Click the Column to change the sort order
			//---------------------------------------------
			homePage.listView.clickColumn(dataPool.get("Column"));
			Utils.fluentWait(driver);

			Log.message("3. Click the Column to change the sort order");

			//3. Check the Sort Image
			//------------------------
			if(homePage.listView.itemCount() <= 1)
				throw new SkipException("Invalid Test Data. The given Object " + dataPool.get("Object") + " does not have previous versions.");

			int count = 0;
			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("Column"));

			List<String> versions = homePage.listView.getColumnValues(dataPool.get("Column"));

			for(count = 0; count < versions.size()-1; count++) {
				if(Integer.parseInt(versions.get(count)) > Integer.parseInt(versions.get(count+1)))
					break;
			}

			Log.message("4. Check the Sort Image.");

			//Verification: To verify if the Column is sorted as expected
			//------------------------------------------------------------
			if(sortImage.equals(dataPool.get("ExpectedSortImage")) && count == versions.size()-1)
				Log.pass("Test Case Passed. The Column '" + dataPool.get("Column") + "' had the expected sort.");
			else
				Log.fail("Test Case Failed. The Column '" + dataPool.get("Column") + "' did not have the expected sort.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 29.7.3C : Verify the column sort upon removing 'Version' column in 'History' view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint29", "Columns"}, 
			description = "Verify the column sort upon removing 'Version' column in 'History' view")
	public void SprintTest29_7_3C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Search for an object
			//------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			Log.message("1. Searched for an object."); 

			//2. Goto the History view of the specified object
			//-------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object"))) 
				throw new SkipException("The given Object " + dataPool.get("Object") + " was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem((Caption.MenuItems.History.Value));
			Utils.fluentWait(driver);

			Log.message("2. Goto the History view of the specified object");

			//3. Remove the default sorted column
			//------------------------------------
			if(!homePage.listView.removeColumn(dataPool.get("Column")))
				throw new SkipException("The given column " + dataPool.get("Column") + " was not removed.");

			Utils.fluentWait(driver);

			Log.message("3. Remove the default sorted column");

			//3. Check the Sort Image
			//------------------------
			if(homePage.listView.itemCount() <= 1)
				throw new SkipException("Invalid Test Data. The given Object " + dataPool.get("Object") + " does not have previous versions.");

			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("ExpectedColumn"));

			Log.message("4. Check the Sort Image.");

			//Insert the removed column back
			homePage.listView.insertColumn(dataPool.get("Column"));

			//Verification: To verify if the Column is sorted as expected
			//------------------------------------------------------------
			if(sortImage.equals(dataPool.get("ExpectedSortImage")))
				Log.pass("Test Case Passed. The Column '" + dataPool.get("ExpectedColumn") + "' had the expected sort.");
			else
				Log.fail("Test Case Failed. The Column '" + dataPool.get("ExpectedColumn") + "' did not have the expected sort.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 29.7.3D : Verify the column sort upon adding 'Version' column in 'History' view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint29", "Columns"}, 
			description = "Verify the column sort upon adding 'Version' column in 'History' view")
	public void SprintTest29_7_3D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Search for an object
			//------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			Log.message("1. Searched for an object."); 

			//2. Goto the History view of the specified object
			//-------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object"))) 
				throw new SkipException("The given Object " + dataPool.get("Object") + " was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem((Caption.MenuItems.History.Value));
			Utils.fluentWait(driver);

			Log.message("2. Goto the History view of the specified object");

			//3. Remove the default sorted column
			//------------------------------------
			if(!homePage.listView.removeColumn(dataPool.get("Column")))
				throw new SkipException("The given column " + dataPool.get("Column") + " was not removed.");

			Utils.fluentWait(driver);

			Log.message("3. Remove the default sorted " + dataPool.get("Column") + " column");

			//4. Add the version column back
			//-------------------------------
			if(!homePage.listView.insertColumn(dataPool.get("Column")))
				throw new SkipException("The given column " + dataPool.get("Column") + " was not inserted back.");
			Utils.fluentWait(driver);

			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			homePage.listView.clickItem(dataPool.get("Object"));
			homePage.taskPanel.clickItem((Caption.MenuItems.History.Value));

			Utils.fluentWait(driver);

			Log.message("4. Add the " + dataPool.get("Column") + " column back and re-navigate to the view");

			//5. Check the Sort Image
			//------------------------
			if(homePage.listView.itemCount() <= 1)
				throw new SkipException("Invalid Test Data. The given Object " + dataPool.get("Object") + " does not have previous versions.");

			int count = 0;
			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("Column"));

			List<String> versions = homePage.listView.getColumnValues(dataPool.get("Column"));

			for(count = 0; count < versions.size()-1; count++) {
				if(Integer.parseInt(versions.get(count)) < Integer.parseInt(versions.get(count+1)))
					break;
			}

			Log.message("5. Check the Sort Image.");

			//Verification: To verify if the Column is sorted as expected
			//------------------------------------------------------------
			if(sortImage.equals(dataPool.get("ExpectedSortImage")) && count == versions.size()-1)
				Log.pass("Test Case Passed. The Column '" + dataPool.get("Column") + "' had the expected sort.");
			else
				Log.fail("Test Case Failed. The Column '" + dataPool.get("Column") + "' did not have the expected sort.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 29.7.4A : Verify the columns and sort indicator displayed upon selecting 'Subobjects' link for any Customer object.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint29", "Columns"}, 
			description = "Verify the columns and sort indicator displayed upon selecting 'Subobjects' link for any Customer object.")
	public void SprintTest29_7_4A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Navigate to the view
			//------------------------
			homePage.listView.navigateThroughView(dataPool.get("View"));
			Utils.fluentWait(driver);

			Log.message("1. Navigated to the view - " + dataPool.get("View")); 

			//2. Goto the Sub Objects view of the specified object
			//-------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object"))) 
				throw new SkipException("The given Object " + dataPool.get("Object") + " was not found in the view.");

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.ShowSubObjects.Value);
			Utils.fluentWait(driver);

			Log.message("2. Goto the Sub Objects view of the specified object");

			//3. Check the Sort Image
			//------------------------
			if(homePage.listView.itemCount() <= 1)
				throw new SkipException("Invalid Test Data. The given Object " + dataPool.get("Object") + " does not have multiple sub objects.");

			int count = 0;
			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("Column"));

			List<String> versions = homePage.listView.getColumnValues(dataPool.get("Column"));

			for(count = 0; count < versions.size()-1; count++) {
				if(versions.get(count).compareToIgnoreCase(versions.get(count+1)) > 0)
					break;
			}

			Log.message("3: Check the Sort Image.");

			//Verification: To verify if the Column is sorted as expected
			//------------------------------------------------------------
			if(sortImage.equals(dataPool.get("ExpectedSortImage")) && count == versions.size()-1)
				Log.pass("Test Case Passed. The Column '" + dataPool.get("Column") + "' had the expected sort.");
			else
				Log.fail("Test Case Failed. The Column '" + dataPool.get("Column") + "' did not have the expected sort.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 29.7.4B : Verify the columns and sort indicator displayed upon selecting 'Subobjects' link for any Customer object.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint29", "Columns"}, 
			description = "Verify the columns and sort indicator displayed upon selecting 'Subobjects' link for any Customer object.")
	public void SprintTest29_7_4B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Navigate to the view
			//------------------------
			homePage.listView.navigateThroughView(dataPool.get("View"));
			Utils.fluentWait(driver);

			Log.message("1. Navigated to the view - " + dataPool.get("View")); 

			//2. Goto the Sub Objects view of the specified object
			//-------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object"))) 
				throw new SkipException("The given Object " + dataPool.get("Object") + " was not found in the view.");

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.ShowSubObjects.Value);
			Utils.fluentWait(driver);

			Log.message("2. Goto the Sub Objects view of the specified object");

			//3. Click the sorted column to change the sort order
			//---------------------------------------------------
			homePage.listView.clickColumn(dataPool.get("Column"));
			Utils.fluentWait(driver);

			Log.message("3. Click the sorted column to change the sort order");

			//4. Check the Sort Image
			//------------------------
			if(homePage.listView.itemCount() <= 1)
				throw new SkipException("Invalid Test Data. The given Object " + dataPool.get("Object") + " does not have multiple sub objects.");

			int count = 0;
			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("Column"));

			List<String> versions = homePage.listView.getColumnValues(dataPool.get("Column"));

			for(count = 0; count < versions.size()-1; count++) {
				if(versions.get(count).compareToIgnoreCase(versions.get(count+1)) < 0)
					break;
			}

			Log.message("4: Check the Sort Image.");

			//Click the column again to get back to default sorting
			homePage.listView.clickColumn(dataPool.get("Column"));

			//Verification: To verify if the Column is sorted as expected
			//------------------------------------------------------------
			if(sortImage.equals(dataPool.get("ExpectedSortImage")) && count == versions.size()-1)
				Log.pass("Test Case Passed. The Column '" + dataPool.get("Column") + "' had the expected sort.");
			else
				Log.fail("Test Case Failed. The Column '" + dataPool.get("Column") + "' did not have the expected sort.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 29.7.5A : Verify the columns and sort indicator displayed upon selecting 'Subobjects' link for any Document collection object.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint29", "Columns"}, 
			description = "Verify the columns and sort indicator displayed upon selecting 'Subobjects' link for any Document collection object.")
	public void SprintTest29_7_5A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Navigate to the view
			//------------------------
			homePage.listView.navigateThroughView(dataPool.get("View"));
			Utils.fluentWait(driver);

			Log.message("1. Navigated to the view - " + dataPool.get("View")); 

			//2. Goto the Sub Objects view of the specified object
			//-------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object"))) 
				throw new SkipException("The given Object " + dataPool.get("Object") + " was not found in the view.");

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.ShowMembers.Value);
			Utils.fluentWait(driver);

			Log.message("2. Goto the Sub Objects view of the specified object");

			//3. Check the Sort Image
			//------------------------
			if(homePage.listView.itemCount() <= 1)
				throw new SkipException("Invalid Test Data. The given Object " + dataPool.get("Object") + " does not have multiple sub objects.");

			int count = 0;
			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("Column"));

			List<String> versions = homePage.listView.getColumnValues(dataPool.get("Column"));

			for(count = 0; count < versions.size()-1; count++) {
				if(versions.get(count).compareToIgnoreCase(versions.get(count+1)) > 0)
					break;
			}

			Log.message("3: Check the Sort Image.");

			//Verification: To verify if the Column is sorted as expected
			//------------------------------------------------------------
			if(sortImage.equals(dataPool.get("ExpectedSortImage")) && count == versions.size()-1)
				Log.pass("Test Case Passed. The Column '" + dataPool.get("Column") + "' had the expected sort.");
			else
				Log.fail("Test Case Failed. The Column '" + dataPool.get("Column") + "' did not have the expected sort.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 29.7.5B : Verify the columns and sort indicator displayed upon selecting 'Subobjects' link for any Document collection object.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint29", "Columns"}, 
			description = "Verify the columns and sort indicator displayed upon selecting 'Subobjects' link for any Document collection object.")
	public void SprintTest29_7_5B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Navigate to the view
			//------------------------
			homePage.listView.navigateThroughView(dataPool.get("View"));
			Utils.fluentWait(driver);

			Log.message("1. Navigated to the view - " + dataPool.get("View")); 

			//2. Goto the Sub Objects view of the specified object
			//-------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object"))) 
				throw new SkipException("The given Object " + dataPool.get("Object") + " was not found in the view.");

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem(Caption.MenuItems.ShowMembers.Value);
			Utils.fluentWait(driver);

			Log.message("2. Goto the Sub Objects view of the specified object");

			//3. Click the column to change to the sort order
			//-----------------------------------------------
			homePage.listView.clickColumn(dataPool.get("Column"));
			Utils.fluentWait(driver);

			Log.message("3. Click the column to change to the sort order");
			//4. Check the Sort Image
			//------------------------
			if(homePage.listView.itemCount() <= 1)
				throw new SkipException("Invalid Test Data. The given Object " + dataPool.get("Object") + " does not have multiple sub objects.");

			int count = 0;
			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("Column"));

			List<String> versions = homePage.listView.getColumnValues(dataPool.get("Column"));

			for(count = 0; count < versions.size()-1; count++) {
				if(versions.get(count).compareToIgnoreCase(versions.get(count+1)) < 0)
					break;
			}

			//Reset the column setting back to normal
			homePage.listView.clickColumn(dataPool.get("Column"));

			Log.message("4: Check the Sort Image.");

			//Verification: To verify if the Column is sorted as expected
			//------------------------------------------------------------
			if(sortImage.equals(dataPool.get("ExpectedSortImage")) && count == versions.size()-1)
				Log.pass("Test Case Passed. The Column '" + dataPool.get("Column") + "' had the expected sort.");
			else
				Log.fail("Test Case Failed. The Column '" + dataPool.get("Column") + "' did not have the expected sort.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 33.3.12 : Check the Removing Version column should sort in name after switching to and from other views
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint33", "Columns"}, 
			description = "Check the Removing Version column should sort in name after switching to and from other views")
	public void SprintTest33_3_12(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Search for an object
			//------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			Log.message("1. Searched for an object."); 

			//2. Goto the History view of the specified object
			//-------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object"))) 
				throw new SkipException("The given Object " + dataPool.get("Object") + " was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem((Caption.MenuItems.History.Value));
			Utils.fluentWait(driver);

			Log.message("2. Goto the History view of the specified object");

			//3. Remove the default sorted column
			//------------------------------------
			if(!homePage.listView.removeColumn(dataPool.get("Column")))
				throw new SkipException("The given column " + dataPool.get("Column") + " was not removed.");

			Utils.fluentWait(driver);

			Log.message("3. Remove the default sorted column");

			//4. Navigate to the Home view
			//-----------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);

			Log.message("4. Navigate to the Home view");

			//5. Re-open the history view of the object
			//------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem((Caption.MenuItems.History.Value));
			Utils.fluentWait(driver);

			Log.message("5. Re-open the history view of the object");

			//6. Check the Sort Image
			//-------------------------
			if(homePage.listView.isColumnExists(dataPool.get("Column")))
				throw new SkipException("The Columns " + dataPool.get("Column") + " exists after navigation");

			int count = 0;
			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("ExpectedColumn"));

			List<String> versions = homePage.listView.getColumnValues(dataPool.get("ExpectedColumn"));

			for(count = 0; count < versions.size()-1; count++) {
				if(versions.get(count).compareToIgnoreCase(versions.get(count+1)) > 0)
					break;
			}

			// Restoring the original column setting. Navigating back to home view because saving setting requires navigation out of view.
			homePage.listView.insertColumn(dataPool.get("Column"));
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);

			Log.message("6. Check the Sort Image.");

			//Verification: To verify if the Column is sorted as expected
			//------------------------------------------------------------
			if(sortImage.equals(dataPool.get("ExpectedSortImage")) && count == versions.size()-1)
				Log.pass("Test Case Passed. The Column '" + dataPool.get("ExpectedColumn") + "' had the expected sort.");
			else
				Log.fail("Test Case Failed. The Column '" + dataPool.get("ExpectedColumn") + "' did not have the expected sort.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 33.3.13 : Check the Removing Version column should sort in name after switching to and from other views
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint33", "Columns"}, 
			description = "Check the Removing Version column should sort in name after switching to and from other views")
	public void SprintTest33_3_13(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Search for an object
			//------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			Log.message("1. Searched for an object."); 

			//2. Goto the History view of the specified object
			//-------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object"))) 
				throw new SkipException("The given Object " + dataPool.get("Object") + " was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem((Caption.MenuItems.History.Value));
			Utils.fluentWait(driver);

			Log.message("2. Goto the History view of the specified object");

			//3. Sort based on any column other than default
			//-----------------------------------------------
			homePage.listView.clickColumn(dataPool.get("Column"));

			Log.message("3. Sort based on any column other than default.");

			//4. Navigate to the Home view
			//-----------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);

			Log.message("4. Navigate to the Home view");

			//5. Re-open the history view of the object
			//------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem((Caption.MenuItems.History.Value));
			Utils.fluentWait(driver);

			Log.message("5. Re-open the history view of the object");

			//6. Check the Sort Image
			//-------------------------
			int count = 0;
			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("ExpectedColumn"));

			List<String> versions = homePage.listView.getColumnValues(dataPool.get("ExpectedColumn"));

			for(count = 0; count < versions.size()-1; count++) {
				if(Integer.parseInt(versions.get(count)) < Integer.parseInt(versions.get(count+1)))
					break;
			}

			Log.message("6. Check the Sort Image.");

			//Verification: To verify if the Column is sorted as expected
			//------------------------------------------------------------
			if(sortImage.equals(dataPool.get("ExpectedSortImage")) && count == versions.size()-1)
				Log.pass("Test Case Passed. The Column '" + dataPool.get("ExpectedColumn") + "' had the expected sort.");
			else
				Log.fail("Test Case Failed. The Column '" + dataPool.get("ExpectedColumn") + "' did not have the expected sort.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}


	/**
	 * 33.3.19A : Change the default sort order of Subobject view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint33", "Columns"}, 
			description = "Change the default sort order of Subobject view.")
	public void SprintTest33_3_19A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewName"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//2. Navigate to the Sub-Objects view of an object
			//-------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object"))) 
				throw new SkipException("The given Object " + dataPool.get("Object") + " was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem((Caption.MenuItems.ShowSubObjects.Value));
			Utils.fluentWait(driver);

			Log.message("2. Navigate to the Sub-Objects view of an object.");

			//3. Change the Sort order of the column
			//---------------------------------------
			homePage.listView.clickColumn(dataPool.get("Column"));
			Utils.fluentWait(driver);

			if(!homePage.listView.getColumnSortImage(dataPool.get("Column")).equals(dataPool.get("ExpectedSortImage")))
				throw new SkipException("The Sort order of the Column was not changed.");

			Log.message("3. Changed the Sort order of the column.");

			//4. Navigate to the Home View
			//-----------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);
			Utils.fluentWait(driver);

			Log.message("4. Navigated to the Home View");

			//5. Navigate back to the Sub-objects view
			//-------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewName"), "");
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem((Caption.MenuItems.ShowSubObjects.Value));
			Utils.fluentWait(driver);

			Log.message("5. Navigated back to the Sub-objects view."); 

			//6. Check the Sort Image
			//-------------------------
			int count = 0;
			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("Column"));

			List<String> versions = homePage.listView.getColumnValues(dataPool.get("Column"));

			for(count = 0; count < versions.size()-1; count++) {
				if(versions.get(count).compareToIgnoreCase(versions.get(count+1)) < 0)
					break;
			}

			Log.message("6. Checked the Sort Image.");

			//Verification: To verify if the Column is sorted as expected
			//------------------------------------------------------------
			if(sortImage.equals(dataPool.get("ExpectedSortImage")) && count == versions.size()-1) {
				Log.pass("Test Case Passed. The Column '" + dataPool.get("Column") + "' had the expected sort.");
				homePage.listView.clickColumn(dataPool.get("Column"));
			}
			else
				Log.fail("Test Case Failed. The Column '" + dataPool.get("Column") + "' did not have the expected sort.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 33.3.19B : Sort Subobject view using a new inserted column
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint33", "Columns"}, 
			description = "Sort Subobject view using a new inserted column.")
	public void SprintTest33_3_19B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewName"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//2. Navigate to the Sub-Objects view of an object
			//-------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object"))) 
				throw new SkipException("The given Object " + dataPool.get("Object") + " was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem((Caption.MenuItems.ShowSubObjects.Value));
			Utils.fluentWait(driver);

			Log.message("2. Navigate to the Sub-Objects view of an object.");

			//3. Sort the View based on a newly inserted column
			//--------------------------------------------------
			homePage.listView.insertColumn(dataPool.get("NewColumn"));

			homePage.listView.clickColumn(dataPool.get("NewColumn"));
			Utils.fluentWait(driver);

			if(!homePage.listView.getColumnSortImage(dataPool.get("NewColumn")).equals(dataPool.get("ExpectedSortImage")))
				throw new SkipException("The Sort order of the Column was not changed.");

			Log.message("3. Sorted the View based on a newly inserted column.");

			//4. Navigate to the Home View
			//-----------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);
			Utils.fluentWait(driver);

			Log.message("4. Navigated to the Home View");

			//5. Navigate back to the Sub-objects view
			//-------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewName"), "");
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem((Caption.MenuItems.ShowSubObjects.Value));
			Utils.fluentWait(driver);

			Log.message("5. Navigated back to the Sub-objects view."); 

			//6. Check the Sort Image
			//-------------------------
			int count = 0;
			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("NewColumn"));

			List<String> versions = homePage.listView.getColumnValues(dataPool.get("NewColumn"));

			for(count = 0; count < versions.size()-1; count++) {
				if(Integer.parseInt(versions.get(count)) > Integer.parseInt(versions.get(count+1)))
					break;
			}

			Log.message("6. Checked the Sort Image.");

			//Verification: To verify if the Column is sorted as expected
			//------------------------------------------------------------
			if(sortImage.equals(dataPool.get("ExpectedSortImage")) && count == versions.size()-1) {
				Log.pass("Test Case Passed. The Column '" + dataPool.get("NewColumn") + "' had the expected sort.");
				homePage.listView.removeColumn(dataPool.get("NewColumn"));
			}
			else
				Log.fail("Test Case Failed. The Column '" + dataPool.get("NewColumn") + "' did not have the expected sort.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 33.3.20A : Change the default sort order of Show Members view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint33", "Columns"}, 
			description = "Change the default sort order of Show Members view.")
	public void SprintTest33_3_20A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Perform the Search
			//----------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			Log.message("1. Performed Search for '" + dataPool.get("SearchType") + "'.");

			//2. Navigate to the Show-Members view of an object
			//-------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object"))) 
				throw new SkipException("The given Object " + dataPool.get("Object") + " was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem((Caption.MenuItems.ShowMembers.Value));
			Utils.fluentWait(driver);

			Log.message("2. Navigate to the Show-Members view of an object.");

			//3. Change the Sort order of the column
			//---------------------------------------
			homePage.listView.clickColumn(dataPool.get("Column"));
			Utils.fluentWait(driver);

			if(!homePage.listView.getColumnSortImage(dataPool.get("Column")).equals(dataPool.get("ExpectedSortImage")))
				throw new SkipException("The Sort order of the Column was not changed.");

			Log.message("3. Changed the Sort order of the column.");

			//4. Navigate to the Home View
			//-----------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);
			Utils.fluentWait(driver);

			Log.message("4. Navigated to the Home View");

			//5. Navigate back to the Sub-objects view
			//-------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem((Caption.MenuItems.ShowMembers.Value));
			Utils.fluentWait(driver);

			Log.message("5. Navigated back to the Sub-objects view."); 

			//6. Check the Sort Image
			//-------------------------
			int count = 0;
			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("Column"));

			List<String> versions = homePage.listView.getColumnValues(dataPool.get("Column"));

			for(count = 0; count < versions.size()-1; count++) {
				if(versions.get(count).compareToIgnoreCase(versions.get(count+1)) < 0)
					break;
			}

			Log.message("6. Checked the Sort Image.");

			//Verification: To verify if the Column is sorted as expected
			//------------------------------------------------------------
			if(sortImage.equals(dataPool.get("ExpectedSortImage")) && count == versions.size()-1) {
				Log.pass("Test Case Passed. The Column '" + dataPool.get("Column") + "' had the expected sort.");
				homePage.listView.clickColumn(dataPool.get("Column"));
			}
			else
				Log.fail("Test Case Failed. The Column '" + dataPool.get("Column") + "' did not have the expected sort.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 33.3.20B : Sort Show Members view using a new inserted column
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint33", "Columns"}, 
			description = "Sort Show Members view using a new inserted column.")
	public void SprintTest33_3_20B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Perform the Search
			//----------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			Log.message("1. Performed Search for '" + dataPool.get("SearchType") + "'.");

			//2. Navigate to the Show-Members view of an object
			//-------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object"))) 
				throw new SkipException("The given Object " + dataPool.get("Object") + " was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem((Caption.MenuItems.ShowMembers.Value));
			Utils.fluentWait(driver);

			Log.message("2. Navigate to the Show-Members view of an object.");

			//3. Sort the View based on a newly inserted column
			//--------------------------------------------------
			homePage.listView.insertColumn(dataPool.get("NewColumn"));

			homePage.listView.clickColumn(dataPool.get("NewColumn"));
			Utils.fluentWait(driver);

			if(!homePage.listView.getColumnSortImage(dataPool.get("NewColumn")).equals(dataPool.get("ExpectedSortImage")))
				throw new SkipException("The Sort order of the Column was not changed.");

			Log.message("3. Sorted the View based on a newly inserted column.");

			//4. Navigate to the Home View
			//-----------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);
			Utils.fluentWait(driver);

			Log.message("4. Navigated to the Home View");

			//5. Navigate back to the Sub-objects view
			//-------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem((Caption.MenuItems.ShowMembers.Value));
			Utils.fluentWait(driver);

			Log.message("5. Navigated back to the Sub-objects view."); 

			//6. Check the Sort Image
			//-------------------------
			int count = 0;
			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("NewColumn"));

			List<String> versions = homePage.listView.getColumnValues(dataPool.get("NewColumn"));

			for(count = 0; count < versions.size()-1; count++) {
				if(Integer.parseInt(versions.get(count)) > Integer.parseInt(versions.get(count+1)))
					break;
			}

			Log.message("6. Checked the Sort Image.");

			// Restoring the original column setting. Navigating back to home view because saving setting requires navigation out of view.
			homePage.listView.removeColumn(dataPool.get("NewColumn"));
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);

			//Verification: To verify if the Column is sorted as expected
			//------------------------------------------------------------
			if(sortImage.equals(dataPool.get("ExpectedSortImage")) && count == versions.size()-1) {
				Log.pass("Test Case Passed. The Column '" + dataPool.get("NewColumn") + "' had the expected sort.");
			}
			else
				Log.fail("Test Case Failed. The Column '" + dataPool.get("NewColumn") + "' did not have the expected sort.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 33.3.20C : Verify the Default sorting in  Show Members view should be based on Name column
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint33", "Columns"}, 
			description = "Verify the Default sorting in  Show Members view should be based on Name column.")
	public void SprintTest33_3_20C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Perform the Search
			//----------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			Log.message("1. Performed Search for '" + dataPool.get("SearchType") + "'.");

			//2. Navigate to the Show-Members view of an object
			//-------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object"))) 
				throw new SkipException("The given Object " + dataPool.get("Object") + " was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem((Caption.MenuItems.ShowMembers.Value));
			Utils.fluentWait(driver);

			Log.message("2. Navigate to the Show-Members view of an object.");

			//3. Check the Sort Image
			//-------------------------
			int count = 0;
			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("Column"));

			List<String> versions = homePage.listView.getColumnValues(dataPool.get("Column"));

			for(count = 0; count < versions.size()-1; count++) {
				if(versions.get(count).compareToIgnoreCase(versions.get(count+1)) > 0)
					break;
			}

			Log.message("3. Checked the Sort Image.");

			//Verification: To verify if the Column is sorted as expected
			//------------------------------------------------------------
			if(sortImage.equals(dataPool.get("ExpectedSortImage")) && count == versions.size()-1) 
				Log.pass("Test Case Passed. The Column '" + dataPool.get("Column") + "' had the expected sort.");
			else
				Log.fail("Test Case Failed. The Column '" + dataPool.get("Column") + "' did not have the expected sort.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 33.3.21A : Change the default sort order of Relationships view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint33", "Columns"}, 
			description = "Change the default sort order of Relationships view.")
	public void SprintTest33_3_21A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Perform the Search
			//----------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			Log.message("1. Performed Search for '" + dataPool.get("SearchType") + "'.");

			//2. Navigate to the Show-Members view of an object
			//-------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object"))) 
				throw new SkipException("The given Object " + dataPool.get("Object") + " was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu((Caption.MenuItems.Relationships.Value));
			Utils.fluentWait(driver);

			Log.message("2. Navigate to the Show-Members view of an object.");

			//3. Change the Sort order of the column
			//---------------------------------------
			homePage.listView.clickColumn(dataPool.get("Column"));
			Utils.fluentWait(driver);

			if(!homePage.listView.getColumnSortImage(dataPool.get("Column")).equals(dataPool.get("ExpectedSortImage")))
				throw new SkipException("The Sort order of the Column was not changed.");

			Log.message("3. Changed the Sort order of the column.");

			//4. Navigate to the Home View
			//-----------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);
			Utils.fluentWait(driver);

			Log.message("4. Navigated to the Home View");

			//5. Navigate back to the Sub-objects view
			//-------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu((Caption.MenuItems.Relationships.Value));
			Utils.fluentWait(driver);

			Log.message("5. Navigated back to the Sub-objects view."); 

			//6. Check the Sort Image
			//-------------------------
			int count = 0;
			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("Column"));

			List<String> versions = homePage.listView.getColumnValues(dataPool.get("Column"));

			for(count = 0; count < versions.size()-1; count++) {
				if(versions.get(count).compareToIgnoreCase(versions.get(count+1)) < 0)
					break;
			}

			Log.message("6. Checked the Sort Image.");

			//Verification: To verify if the Column is sorted as expected
			//------------------------------------------------------------
			if(sortImage.equals(dataPool.get("ExpectedSortImage")) && count == versions.size()-1) {
				Log.pass("Test Case Passed. The Column '" + dataPool.get("Column") + "' had the expected sort.");
				homePage.listView.clickColumn(dataPool.get("Column"));
			}
			else
				Log.fail("Test Case Failed. The Column '" + dataPool.get("Column") + "' did not have the expected sort.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 33.3.21B : Sort Relationships view using a new inserted column
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint33", "Columns"}, 
			description = "Sort Relationships view using a new inserted column.")
	public void SprintTest33_3_21B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Perform the Search
			//----------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			Log.message("1. Performed Search for '" + dataPool.get("SearchType") + "'.");

			//2. Navigate to the Show-Members view of an object
			//-------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object"))) 
				throw new SkipException("The given Object " + dataPool.get("Object") + " was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu((Caption.MenuItems.Relationships.Value));
			Utils.fluentWait(driver);

			Log.message("2. Navigate to the Show-Members view of an object.");

			//3. Sort the View based on a newly inserted column
			//--------------------------------------------------
			homePage.listView.insertColumn(dataPool.get("NewColumn"));

			homePage.listView.clickColumn(dataPool.get("NewColumn"));
			Utils.fluentWait(driver);

			if(!homePage.listView.getColumnSortImage(dataPool.get("NewColumn")).equals(dataPool.get("ExpectedSortImage")))
				throw new SkipException("The Sort order of the Column was not changed.");

			Log.message("3. Sorted the View based on a newly inserted column.");

			//4. Navigate to the Home View
			//-----------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);
			Utils.fluentWait(driver);

			Log.message("4. Navigated to the Home View");

			//5. Navigate back to the Sub-objects view
			//-------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);
			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu((Caption.MenuItems.Relationships.Value));
			Utils.fluentWait(driver);

			Log.message("5. Navigated back to the Sub-objects view."); 

			//6. Check the Sort Image
			//-------------------------
			int count = 0;
			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("NewColumn"));

			List<String> versions = homePage.listView.getColumnValues(dataPool.get("NewColumn"));

			for(count = 0; count < versions.size()-1; count++) {
				if(Integer.parseInt(versions.get(count)) > Integer.parseInt(versions.get(count+1)))
					break;
			}

			Log.message("6. Checked the Sort Image.");

			// Restoring the original column setting. Navigating back to home view because saving setting requires navigation out of view.
			homePage.listView.removeColumn(dataPool.get("NewColumn"));
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);

			//Verification: To verify if the Column is sorted as expected
			//------------------------------------------------------------
			if(sortImage.equals(dataPool.get("ExpectedSortImage")) && count == versions.size()-1) {
				Log.pass("Test Case Passed. The Column '" + dataPool.get("NewColumn") + "' had the expected sort.");
			}
			else
				Log.fail("Test Case Failed. The Column '" + dataPool.get("NewColumn") + "' did not have the expected sort.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 33.3.21C : Verify the Default sorting in  Relationships view should be based on Name column
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint33", "Columns"}, 
			description = "Verify the Default sorting in  Relationships view should be based on Name column.")
	public void SprintTest33_3_21C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Perform the Search
			//----------------------------
			homePage.searchPanel.search(dataPool.get("Object"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			Log.message("1. Performed Search for '" + dataPool.get("SearchType") + "'.");

			//2. Navigate to the Show-Members view of an object
			//-------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("Object"))) 
				throw new SkipException("The given Object " + dataPool.get("Object") + " was not found in the vault.");

			homePage.listView.clickItem(dataPool.get("Object"));
			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu((Caption.MenuItems.Relationships.Value));
			Utils.fluentWait(driver);

			Log.message("2. Navigate to the Show-Members view of an object.");

			//3. Check the Sort Image
			//-------------------------
			int count = 0;
			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("Column"));

			List<String> versions = homePage.listView.getColumnValues(dataPool.get("Column"));

			for(count = 0; count < versions.size()-1; count++) {
				if(versions.get(count).compareToIgnoreCase(versions.get(count+1)) > 0)
					break;
			}

			Log.message("3. Checked the Sort Image.");

			//Verification: To verify if the Column is sorted as expected
			//------------------------------------------------------------
			if(sortImage.equals(dataPool.get("ExpectedSortImage")) && count == versions.size()-1) 
				Log.pass("Test Case Passed. The Column '" + dataPool.get("Column") + "' had the expected sort.");
			else
				Log.fail("Test Case Failed. The Column '" + dataPool.get("Column") + "' did not have the expected sort.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 38.1.6 : Default Columns in History view
	 */
	/*@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint38"}, 
			description = "Default Columns in History view")*/
	public void SprintTest38_1_6(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open History View of an object
			//---------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) 
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.History.Value); //Opens History View

			if (!ListView.isHistoryViewOpened(driver))
				throw new Exception("History view of an object (" + dataPool.get("ObjectName") + ") is not opened.");

			Log.message("2. History view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Obtain the columns from History view
			//---------------------------------------------
			String defaultColns[] = homePage.listView.getVisibleColumns();
			Arrays.sort(defaultColns);
			String testDataColns[] = dataPool.get("DefaultColumns").split(",");
			Arrays.sort(testDataColns);

			Log.message("3. Default columns in History view is obtained.");

			//Verification : To Verify if column is inserted in Home view
			//-----------------------------------------------------------			
			if (Arrays.equals(defaultColns, testDataColns))
				Log.pass("Test case Passed. Default columns in History view is available.");
			else
				Log.fail("Test case Failed. Default columns in History view is not available.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest38_1_6


} //End class Columns
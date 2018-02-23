package MFClient.Tests.Columns;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.TestMethodWebDriverManager;
import genericLibrary.Utils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
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
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class ViewsSortingByColumn {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String configURL = null;
	public static String userName = null;
	public static String password = null;
	public static String testVault = null;
	public static String className = null;
	public static String productVersion = null;

	private TestMethodWebDriverManager driverManager = null;

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

			driverManager = new TestMethodWebDriverManager();

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
	 * quitDrivers : Quits and closes all web drivers started by the test method
	 */
	@AfterMethod (alwaysRun=true)
	public void quitDrivers(Method method, ITestResult result) throws Exception {

		driverManager.quitTestMethodWebDrivers(method.getName());
		Log.endTestCase();//Ends the test case
	}


	/**
	 * 29.7.1A : Verify the column sort in 'Accessed by me' column
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint29", "Columns"}, 
			description = "Verify the column sort in 'Accessed by me' column")
	public void SprintTest29_7_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Access few objects to be available in Recently accessed by me view
			//---------------------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchAllObjects.Value, "");

			for (int i=0; i<5;i++) {
				homePage.listView.clickItemByIndex(i);
				homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);
				MFilesDialog.closeMFilesDialog(driver);
			}

			Log.message("1. Few objects in list are accessed to be available in Recently accessed by me view.");

			//2. Navigate to View
			//--------------------
			viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, Caption.MenuItems.RecentlyAccessedByMe.Value, "");

			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//3. Check the Sort Image
			//------------------------
			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("Column"));

			if(homePage.listView.itemCount() <= 1)
				throw new SkipException("Invalid Test Data. The given view " + dataPool.get("ViewName") + " does not multiple objects for verification.");

			int count = 0;
			List<String> versions = homePage.listView.getColumnValues(dataPool.get("Column"));

			for(count = 0; count < versions.size()-1; count++) {
				if(versions.get(count).compareToIgnoreCase(versions.get(count+1)) < 0)
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
	 * 29.7.1B : Verify the column sort upon removing 'Accessed by me' column
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint29", "Columns"}, 
			description = "Verify the column sort upon removing 'Accessed by me' column")
	public void SprintTest29_7_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Access few objects to be available in Recently accessed by me view
			//---------------------------------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchAllObjects.Value, "");

			for (int i=0; i<5;i++) {
				homePage.listView.clickItemByIndex(i);
				homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Workflow.Value);
				MFilesDialog.closeMFilesDialog(driver);
			}

			Log.message("1. Few objects in list are accessed to be available in Recently accessed by me view.");

			//2. Navigate to View
			//--------------------
			viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, Caption.MenuItems.RecentlyAccessedByMe.Value, "");

			Log.message("2. Navigated to '" + viewToNavigate + "' view.");

			//3. Remove the default sorted column
			//------------------------------------
			if(!homePage.listView.removeColumn(dataPool.get("Column")))
				throw new SkipException("The given column " + dataPool.get("Column") + " was not removed.");
			Utils.fluentWait(driver);

			Log.message("3. Removed the default sorted column.");

			//4. Check the Sort Image of the expected sorted column
			//------------------------------------------------------
			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("ExpectedColumn"));

			Log.message("4. Check the Sort Image of the expected sorted column");

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
	 * 29.7.1C: Verify the column sort upon adding 'Accessed by me' column
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint29", "Columns"}, 
			description = "Verify the column sort upon adding 'Accessed by me' column")
	public void SprintTest29_7_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Navigate to View
			//--------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewName"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//2. Remove the default sorted column
			//------------------------------------
			if(!homePage.listView.removeColumn(dataPool.get("Column")))
				throw new SkipException("The given column " + dataPool.get("Column") + " was not removed.");

			Utils.fluentWait(driver);

			Log.message("2. Removed the default sorted " + dataPool.get("Column") + " column.");

			//3. Insert the default sorted column back
			//-----------------------------------------
			if(!homePage.listView.insertColumn(dataPool.get("Column")))
				throw new SkipException("The given column " + dataPool.get("Column") + " was not inserted back.");
			Utils.fluentWait(driver);

			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);
			SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewName"), "");

			Utils.fluentWait(driver);

			Log.message("3. Inserted the default sorted " + dataPool.get("Column") + " column back and re-navigated to the view");

			//4. Check the Sort Image
			//------------------------
			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("Column"));

			if(homePage.listView.itemCount() <= 1)
				throw new SkipException("Invalid Test Data. The given view " + dataPool.get("ViewName") + " does not multiple objects for verification.");

			int count = 0;
			List<String> versions = homePage.listView.getColumnValues(dataPool.get("Column"));

			for(count = 0; count < versions.size()-1; count++) {
				if(versions.get(count).compareToIgnoreCase(versions.get(count+1)) < 0)
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
	 * 29.7.6A: Check the 'column sort' and 'column names' displayed upon performing predefined search operations
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint29", "Columns"}, 
			description = "Check the 'column sort' and 'column names' displayed upon performing predefined search operations.")
	public void SprintTest29_7_6A(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Perform a pre-defined Search
			//--------------------------------
			homePage.searchPanel.search("", dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			Log.message("1. Perform a pre-defined Search - " + dataPool.get("SearchType")); 

			//2. Check the Column sort image
			//-------------------------------
			if(homePage.listView.itemCount() <= 1)
				throw new SkipException("Invalid Test Data. The given search " + dataPool.get("SearchType") + " did not return multiple objects.");

			int count = 0;
			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("Column"));

			List<String> versions = homePage.listView.getColumnValues(dataPool.get("Column"));

			for(count = 0; count < versions.size()-1; count++) {
				if(Integer.parseInt(versions.get(count)) < Integer.parseInt(versions.get(count+1)))
					break;
			}

			Log.message("2. Check the Column sort image.");

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
	 * 29.7.6B : Check the 'column sort' and 'column names' displayed upon performing predefined search operations
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint29", "Columns"}, 
			description = "Check the 'column sort' and 'column names' displayed upon performing predefined search operations.")
	public void SprintTest29_7_6B(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Perform a pre-defined Search
			//--------------------------------
			homePage.searchPanel.search("", dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			Log.message("1. Perform a pre-defined Search - " + dataPool.get("SearchType")); 

			//2. Click the column to change to the sort order
			//-----------------------------------------------
			homePage.listView.clickColumn(dataPool.get("Column"));
			Utils.fluentWait(driver);

			Log.message("2. Click the column to change to the sort order");

			//3. Check the Sort Image
			//------------------------
			if(homePage.listView.itemCount() <= 1)
				throw new SkipException("Invalid Test Data. The given Search " + dataPool.get("SearchType") + " did not return multiple objects.");

			int count = 0;
			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("Column"));

			List<String> versions = homePage.listView.getColumnValues(dataPool.get("Column"));

			for(count = 0; count < versions.size()-1; count++) {
				if(Integer.parseInt(versions.get(count)) > Integer.parseInt(versions.get(count+1)))
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
	 * 33.3.3 : Verify the 'Removing Accessed by me column' should sort in name after switching to and from other views
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint33", "Columns"}, 
			description = "Verify the 'Removing Accessed by me column' should sort in name after switching to and from other views.")
	public void SprintTest33_3_3(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Navigate to the specified view
			//--------------------------------
			homePage.listView.navigateThroughView(dataPool.get("ViewName"));
			Utils.fluentWait(driver);

			Log.message("1. Navigated to the specified view."); 

			//2. Remove the default sorted column
			//------------------------------------
			homePage.listView.removeColumn(dataPool.get("Column"));

			Log.message("2. Removed the default sorted column");

			//3. Navigate to Home View
			//-------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);
			Utils.fluentWait(driver);

			Log.message("3. Navigate to Home View");

			//4. Navigate back to the View
			//-----------------------------
			homePage.listView.navigateThroughView(dataPool.get("ViewName"));
			Utils.fluentWait(driver);

			Log.message("4. Navigated back to the View");

			//5. Check the Sort Image
			//------------------------
			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("Column"));

			if(!homePage.listView.isColumnExists(dataPool.get("Column")))
				throw new SkipException("The Column " + dataPool.get("Column") + " does not exist after navigating out and into the view");

			if(homePage.listView.itemCount() <= 1)
				throw new SkipException("Invalid Test Data. The given view " + dataPool.get("ViewName") + " does not multiple objects for verification.");

			List<String> versions = homePage.listView.getColumnValues(dataPool.get("Column"));

			boolean isValuesSorted = homePage.listView.isColumnValuesSorted(versions, dataPool.get("DataType"), dataPool.get("SortOrder"));

			Log.message("5. Check the Sort Image.");

			//Verification: To verify if the Column is sorted as expected
			//------------------------------------------------------------
			if(sortImage.equals(dataPool.get("ExpectedSortImage")) && isValuesSorted)
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
	 * 33.3.4 : Verify the view Sorted using other columns in recently accessed by me view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint33", "Columns"}, 
			description = "Verify the view Sorted using other columns in recently accessed by me view.")
	public void SprintTest33_3_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Navigate to the specified view
			//--------------------------------
			homePage.listView.navigateThroughView(dataPool.get("ViewName"));
			Utils.fluentWait(driver);

			Log.message("1. Navigated to the specified view."); 

			//2. Sort the items based on a different Column
			//----------------------------------------------
			homePage.listView.clickColumn(dataPool.get("Column"));

			Log.message("2. Sort the items based on a different Column");

			//3. Navigate to Home View
			//-------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);
			Utils.fluentWait(driver);

			Log.message("3. Navigate to Home View");

			//4. Navigate back to the View
			//-----------------------------
			homePage.listView.navigateThroughView(dataPool.get("ViewName"));
			Utils.fluentWait(driver);

			Log.message("4. Navigated back to the View");

			//5. Check the Sort Image
			//------------------------
			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("ExpectedColumn"));

			if(homePage.listView.itemCount() <= 1)
				throw new SkipException("Invalid Test Data. The given view " + dataPool.get("ViewName") + " does not multiple objects for verification.");

			List<String> versions = homePage.listView.getColumnValues(dataPool.get("ExpectedColumn"));

			boolean isValuesSorted = homePage.listView.isColumnValuesSorted(versions, dataPool.get("DataType"), dataPool.get("SortOrder"));

			Log.message("5. Check the Sort Image.");

			//Verification: To verify if the Column is sorted as expected
			//------------------------------------------------------------
			if(sortImage.equals(dataPool.get("ExpectedSortImage")) && isValuesSorted)
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
	 * 33.3.16 : Check the Removing Score column should sort in name after switching to and from other views in search view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint33", "Columns"}, 
			description = "Check the Removing Score column should sort in name after switching to and from other views in search view.")
	public void SprintTest33_3_16(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Perform a search
			//--------------------
			homePage.searchPanel.search("", dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			Log.message("1. Performed a search."); 

			//2. Remove the default sorted column
			//------------------------------------
			homePage.listView.removeColumn(dataPool.get("Column"));

			Log.message("2. Removed the default sorted column");

			//3. Navigate to the Home view
			//-----------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);

			Log.message("3. Navigated to the Home view");

			//4. Perform a search
			//--------------------
			homePage.searchPanel.search("", dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			Log.message("4. Performed a search."); 

			//6. Check the Sort Image
			//-------------------------
			int count = 0;
			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("Column"));

			List<String> versions = homePage.listView.getColumnValues(dataPool.get("Column"));

			for(count = 0; count < versions.size()-1; count++) {
				if(Integer.parseInt(versions.get(count)) < Integer.parseInt(versions.get(count+1)))
					break;
			}

			Log.message("5. Checked the Sort Image.");

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
	 * 33.3.17 : Verify the Sort indicator using other columns in recently Search view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint33", "Columns"}, 
			description = "Verify the Sort indicator using other columns in recently Search view.")
	public void SprintTest33_3_17(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Perform a search
			//--------------------
			homePage.searchPanel.search("", dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			Log.message("1. Performed a search."); 

			//2. Remove the default sorted column
			//------------------------------------
			homePage.listView.clickColumn(dataPool.get("Column"));

			Log.message("2. Removed the default sorted column");

			//3. Navigate to the Home view
			//-----------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);

			Log.message("3. Navigated to the Home view");

			//4. Perform a search
			//--------------------
			homePage.searchPanel.search("", dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			Log.message("4. Performed a search."); 

			//6. Check the Sort Image
			//-------------------------
			int count = 0;
			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("ExpectedColumn"));

			List<String> versions = homePage.listView.getColumnValues(dataPool.get("ExpectedColumn"));

			for(count = 0; count < versions.size()-1; count++) {
				if(Integer.parseInt(versions.get(count)) < Integer.parseInt(versions.get(count+1)))
					break;
			}

			Log.message("5. Checked the Sort Image.");

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
	 * 33.3.22A : Verify the Default sorting in  Home view should be based on Name column
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint33", "Columns"}, 
			description = "Verify the Default sorting in  Home view should be based on Name column.")
	public void SprintTest33_3_22A(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null;
		HomePage homePage = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);
			homePage.menuBar.ClickOperationsMenu("Display Mode>>"+Caption.MenuItems.GroupViews.Value);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Check the Sort Image
			//-------------------------
			int count = 0;
			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("Column"));

			List<String> versions = homePage.listView.getColumnValues(dataPool.get("Column"));

			for(count = 0; count < versions.size()-1; count++) {
				if(versions.get(count).compareToIgnoreCase(versions.get(count+1)) > 0)
					break;
			}

			Log.message("2. Checked the Sort Image.");

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
			if (driver != null && homePage != null)
			{
				try
				{
					homePage.menuBar.ClickOperationsMenu("Display Mode>>"+Caption.MenuItems.GroupViews.Value);
				}

				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}

			Utils.fluentWait(driver);
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 33.3.22B : Change the default sort order of Home view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint33", "Columns"}, 
			description = "Change the default sort order of Home view.")
	public void SprintTest33_3_22B(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null;
		HomePage homePage = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);
			homePage.menuBar.ClickOperationsMenu("Display Mode>>"+Caption.MenuItems.GroupViews.Value);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Change the Sort order of the default sorted column
			//------------------------------------------------------
			homePage.listView.clickColumn(dataPool.get("Column"));
			Utils.fluentWait(driver);

			Log.message("2. Change the Sort order of the default sorted column.");

			//3. Navigate to any other view
			//------------------------------
			homePage.listView.navigateThroughView(dataPool.get("View"));
			Utils.fluentWait(driver);

			Log.message("3. Navigated to any other view");

			//4. Navigate back to Home View
			//------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);
			Utils.fluentWait(driver);

			Log.message("4. Navigated back to Home View.");

			//4. Check the Sort Image
			//-------------------------
			int count = 0;
			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("Column"));

			List<String> versions = homePage.listView.getColumnValues(dataPool.get("Column"));

			for(count = 0; count < versions.size()-1; count++) {
				if(versions.get(count).compareToIgnoreCase(versions.get(count+1)) < 0)
					break;
			}

			Log.message("4. Checked the Sort Image.");

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

			if (driver != null && homePage != null)
			{
				try
				{
					homePage.menuBar.ClickOperationsMenu("Display Mode>>"+Caption.MenuItems.GroupViews.Value);
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}

			Utils.fluentWait(driver);
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 33.3.23A : Verify the Default sorting in Checked out to me view should be based on Name column
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint33", "Columns"}, 
			description = "Verify the Default sorting in Checked out to me view should be based on Name column.")
	public void SprintTest33_3_23A(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null;
		HomePage homePage = null;
		String[] objects = null, searchType = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			objects = dataPool.get("Object").split("\n");
			searchType = dataPool.get("SearchType").split("\n");

			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Check out some objects
			//-------------------------
			for(int count = 0; count < objects.length; count++) {
				homePage.searchPanel.search(objects[count], searchType[count]);
				Utils.fluentWait(driver);

				if(!homePage.listView.isItemExists(objects[count]))
					throw new SkipException("The specified object does not exist in the vault.");

				homePage.listView.clickItem(objects[count]);
				Utils.fluentWait(driver);
				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
				Utils.fluentWait(driver);

			}

			Log.message("2. Checked out some objects.");

			//3. Navigate to Checked Out to me View
			//---------------------------------------
			homePage.taskPanel.clickItem("Checked Out to Me");
			Utils.fluentWait(driver);

			Log.message("3. Navigated to Checked Out to me View");

			//4. Check the Sort Image
			//-------------------------
			int count = 0;
			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("Column"));

			List<String> versions = homePage.listView.getColumnValues(dataPool.get("Column"));

			for(count = 0; count < versions.size()-1; count++) {
				if(versions.get(count).compareToIgnoreCase(versions.get(count+1)) > 0)
					break;
			}

			Log.message("4. Checked the Sort Image");

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

			if (objects != null && homePage != null && driver != null)
			{
				try
				{
					for(int count = 0; count < objects.length; count++) {
						homePage.searchPanel.search(objects[count], searchType[count]);
						Utils.fluentWait(driver);

						if(!homePage.listView.isItemExists(objects[count]))
							throw new SkipException("The specified object does not exist in the vault.");

						homePage.listView.clickItem(objects[count]);
						Utils.fluentWait(driver);
						homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);
						Utils.fluentWait(driver);

					}
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 33.3.23B : Verify Changing Sort order in Checked out to me view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint33", "Columns"}, 
			description = "Verify Changing Sort order in Checked out to me view.")
	public void SprintTest33_3_23B(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null;
		HomePage homePage = null;
		ConcurrentHashMap <String, String> dataPool = null;

		String[] objects = null, searchType = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			objects = dataPool.get("Object").split("\n");
			searchType = dataPool.get("SearchType").split("\n");

			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Check out some objects
			//-------------------------
			for(int count = 0; count < objects.length; count++) {
				homePage.searchPanel.search(objects[count], searchType[count]);
				Utils.fluentWait(driver);

				if(!homePage.listView.isItemExists(objects[count]))
					throw new SkipException("The specified object does not exist in the vault.");

				homePage.listView.clickItem(objects[count]);
				Utils.fluentWait(driver);
				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
				Utils.fluentWait(driver);

			}

			Log.message("2. Checked out some objects.");

			//3. Navigate to Checked Out to me View
			//---------------------------------------
			homePage.taskPanel.clickItem("Checked Out to Me");
			Utils.fluentWait(driver);

			Log.message("3. Navigated to Checked Out to me View");

			//4. Click on the column to change it's sorting order
			//----------------------------------------------------
			homePage.listView.clickColumn(dataPool.get("Column"));
			Utils.fluentWait(driver);

			Log.message("4. Clicked on the column to change it's sorting order");

			//5. Navigate to Home View
			//-------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);
			Utils.fluentWait(driver);

			Log.message("5. Navigated to Home View");

			//5. Navigate back into Checked Out to Me
			//----------------------------------------
			homePage.taskPanel.clickItem("Checked Out to Me");
			Utils.fluentWait(driver);

			Log.message("6. Navigated back into Checked Out to Me");

			//6. Check the Sort Image
			//-------------------------
			int count = 0;
			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("Column"));

			List<String> versions = homePage.listView.getColumnValues(dataPool.get("Column"));

			for(count = 0; count < versions.size()-1; count++) {
				if(versions.get(count).compareToIgnoreCase(versions.get(count+1)) < 0)
					break;
			}

			Log.message("7. Checked the Sort Image");

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
			if (objects != null && homePage != null && driver != null)
			{
				try
				{
					for(int count = 0; count < objects.length; count++) {
						homePage.searchPanel.search(objects[count], searchType[count]);
						Utils.fluentWait(driver);

						if(!homePage.listView.isItemExists(objects[count]))
							throw new SkipException("The specified object does not exist in the vault.");

						homePage.listView.clickItem(objects[count]);
						Utils.fluentWait(driver);
						homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);
						Utils.fluentWait(driver);

					}


					homePage.listView.clickColumn(dataPool.get("Column"));
					Utils.fluentWait(driver);
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 33.3.23C : Sort Checked Out to me view using a new inserted column
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint33", "Columns"}, 
			description = "Sort Checked Out to me view using a new inserted column.")
	public void SprintTest33_3_23C(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null;
		HomePage homePage = null;
		ConcurrentHashMap <String, String> dataPool = null;

		String[] objects = null, searchType = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			objects = dataPool.get("Object").split("\n");
			searchType = dataPool.get("SearchType").split("\n");

			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Check out some objects
			//-------------------------
			for(int count = 0; count < objects.length; count++) {
				homePage.searchPanel.search(objects[count], searchType[count]);
				Utils.fluentWait(driver);

				if(!homePage.listView.isItemExists(objects[count]))
					throw new SkipException("The specified object does not exist in the vault.");

				homePage.listView.clickItem(objects[count]);
				Utils.fluentWait(driver);
				homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);
				Utils.fluentWait(driver);

			}

			Log.message("2. Checked out some objects.");

			//3. Navigate to Checked Out to me View
			//---------------------------------------
			homePage.taskPanel.clickItem("Checked Out to Me");
			Utils.fluentWait(driver);

			Log.message("3. Navigated to Checked Out to me View");

			//4. Sort the View using a newly inserted column
			//-----------------------------------------------
			homePage.listView.insertColumn(dataPool.get("Column"));
			Utils.fluentWait(driver);
			homePage.listView.clickColumn(dataPool.get("Column"));
			Utils.fluentWait(driver);

			Log.message("4. Sorted the View using a newly inserted column");

			//5. Navigate to Home View
			//-------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);
			Utils.fluentWait(driver);

			Log.message("5. Navigated to Home View");

			//5. Navigate back into Checked Out to Me
			//----------------------------------------
			homePage.taskPanel.clickItem("Checked Out to Me");
			Utils.fluentWait(driver);

			Log.message("6. Navigated back into Checked Out to Me");

			//6. Check the Sort Image
			//-------------------------
			int count = 0;
			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("Column"));

			List<String> versions = homePage.listView.getColumnValues(dataPool.get("Column"));

			for(count = 0; count < versions.size()-1; count++) {
				if(Integer.parseInt(versions.get(count)) > Integer.parseInt(versions.get(count+1)))
					break;
			}

			Log.message("7. Checked the Sort Image");

			//Verification: To verify if the Column is sorted as expected
			//------------------------------------------------------------
			if(sortImage.equals(dataPool.get("ExpectedSortImage")) && count == versions.size()-1) 
				Log.pass("Test Case Passed. The Column '" + dataPool.get("Column") + "' had the expected sort.");
			else
				Log.fail("Test Case Failed. The Column '" + dataPool.get("Column") + "' did not have the expected sort.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if (objects != null && homePage != null && driver != null)
			{
				try
				{
					for(int count = 0; count < objects.length; count++) {
						homePage.searchPanel.search(objects[count], searchType[count]);
						Utils.fluentWait(driver);

						if(!homePage.listView.isItemExists(objects[count]))
							throw new SkipException("The specified object does not exist in the vault.");

						homePage.listView.clickItem(objects[count]);
						Utils.fluentWait(driver);
						homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);
						Utils.fluentWait(driver);

					}

					homePage.taskPanel.clickItem("Checked Out to Me");
					Utils.fluentWait(driver);
					homePage.listView.removeColumn(dataPool.get("Column"));
					Utils.fluentWait(driver);
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 33.3.24A : Verify the Default sorting in Favorites view should be based on Name column
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint33", "Columns"}, 
			description = "Verify the Default sorting in Favorites view should be based on Name column.")
	public void SprintTest33_3_24A(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null;
		HomePage homePage = null;
		ConcurrentHashMap <String, String> dataPool = null;

		String[] objects = null, searchType = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			objects = dataPool.get("Object").split("\n");
			searchType = dataPool.get("SearchType").split("\n");


			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Add out some objects
			//-------------------------
			for(int count = 0; count < objects.length; count++) {
				homePage.searchPanel.search(objects[count], searchType[count]);
				Utils.fluentWait(driver);

				if(!homePage.listView.isItemExists(objects[count]))
					throw new SkipException("The specified object does not exist in the vault.");

				homePage.listView.rightClickItem(objects[count]);
				Utils.fluentWait(driver);
				homePage.listView.clickContextMenuItem(Caption.MenuItems.AddToFavorites.Value);
				Utils.fluentWait(driver);
				MFilesDialog mFilesDialog = new MFilesDialog(driver);
				mFilesDialog.clickOkButton();
				Utils.fluentWait(driver);
			}

			Log.message("2. Add some objects to favorites.");

			//3. Navigate to Favorites View
			//------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Favorites.Value);
			Utils.fluentWait(driver);

			Log.message("3. Navigate to Favorites View.");

			//4. Check the Sort Image
			//-------------------------
			int count = 0;
			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("Column"));

			List<String> versions = homePage.listView.getColumnValues(dataPool.get("Column"));

			for(count = 0; count < versions.size()-1; count++) {
				if(versions.get(count).compareToIgnoreCase(versions.get(count+1)) > 0)
					break;
			}

			Log.message("4. Checked the Sort Image");

			//Verification: To verify if the Column is sorted as expected
			//------------------------------------------------------------
			if(sortImage.equals(dataPool.get("ExpectedSortImage")) && count == versions.size()-1) 
				Log.pass("Test Case Passed. The Column '" + dataPool.get("Column") + "' had the expected sort.");
			else
				Log.fail("Test Case Failed. The Column '" + dataPool.get("Column") + "' did not have the expected sort.", driver);

		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if (objects != null && homePage != null && driver != null)
			{
				try
				{
					for(int count = 0; count < objects.length; count++) {
						if(!homePage.listView.isItemExists(objects[count]))
							throw new SkipException("The specified object does not exist in the vault.");

						homePage.listView.rightClickItem(objects[count]);
						Utils.fluentWait(driver);
						homePage.listView.clickContextMenuItem(Caption.MenuItems.RemoveFromFavorites.Value);
						Utils.fluentWait(driver);
						MFilesDialog mFilesDialog = new MFilesDialog(driver);
						mFilesDialog.clickOkButton();
						Utils.fluentWait(driver);
						mFilesDialog = new MFilesDialog(driver);
						mFilesDialog.clickOkButton();
						Utils.fluentWait(driver);
					}
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}

			Utility.quitDriver(driver);
		}

	}

	/**
	 * 33.3.24B: Verify Changing Sort order in Favorites view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint33", "Columns"}, 
			description = "Verify Changing Sort order in Favorites view.")
	public void SprintTest33_3_24B(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null;
		HomePage homePage = null;
		ConcurrentHashMap <String, String> dataPool = null;

		String[] objects = null, searchType = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			objects = dataPool.get("Object").split("\n");
			searchType = dataPool.get("SearchType").split("\n");


			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Add some objects to favorites
			//---------------------------------
			for(int count = 0; count < objects.length; count++) {
				homePage.searchPanel.search(objects[count], searchType[count]);
				Utils.fluentWait(driver);

				if(!homePage.listView.isItemExists(objects[count]))
					throw new SkipException("The specified object does not exist in the vault.");

				homePage.listView.rightClickItem(objects[count]);
				Utils.fluentWait(driver);
				homePage.listView.clickContextMenuItem(Caption.MenuItems.AddToFavorites.Value);
				Utils.fluentWait(driver);
				MFilesDialog mFilesDialog = new MFilesDialog(driver);
				mFilesDialog.clickOkButton();
				Utils.fluentWait(driver);
			}

			Log.message("2. Add some objects to favorites.");

			//3. Navigate to Favorites View
			//------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Favorites.Value);
			Utils.fluentWait(driver);

			Log.message("3. Navigate to Favorites View.");

			//4. Click on the column to change it's sorting order
			//----------------------------------------------------
			homePage.listView.clickColumn(dataPool.get("Column"));
			Utils.fluentWait(driver);

			Log.message("4. Clicked on the column to change it's sorting order");

			//5. Navigate to Home View
			//-------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);
			Utils.fluentWait(driver);

			Log.message("5. Navigated to Home View");

			//5. Navigate back into Favorites
			//----------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Favorites.Value);
			Utils.fluentWait(driver);

			Log.message("6. Navigated back into Favorites");

			//6. Check the Sort Image
			//-------------------------
			int count = 0;
			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("Column"));

			List<String> versions = homePage.listView.getColumnValues(dataPool.get("Column"));

			for(count = 0; count < versions.size()-1; count++) {
				if(versions.get(count).compareToIgnoreCase(versions.get(count+1)) < 0)
					break;
			}

			homePage.listView.clickColumn(dataPool.get("Column"));
			Utils.fluentWait(driver);

			Log.message("7. Checked the Sort Image");

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
			if (objects != null && homePage != null && driver != null)
			{
				try
				{
					for(int count = 0; count < objects.length; count++) {
						if(!homePage.listView.isItemExists(objects[count]))
							throw new SkipException("The specified object does not exist in the vault.");

						homePage.listView.rightClickItem(objects[count]);
						Utils.fluentWait(driver);
						homePage.listView.clickContextMenuItem(Caption.MenuItems.RemoveFromFavorites.Value);
						Utils.fluentWait(driver);
						MFilesDialog mFilesDialog = new MFilesDialog(driver);
						mFilesDialog.clickOkButton();
						Utils.fluentWait(driver);
						mFilesDialog = new MFilesDialog(driver);
						mFilesDialog.clickOkButton();
						Utils.fluentWait(driver);
					}
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 33.3.24C : Sort Favorites view using a new inserted column
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint33", "Columns"}, 
			description = "Sort Favorites view using a new inserted column.")
	public void SprintTest33_3_24C(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null;
		HomePage homePage = null;
		ConcurrentHashMap <String, String> dataPool = null;

		String[] objects = null, searchType = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			objects = dataPool.get("Object").split("\n");
			searchType = dataPool.get("SearchType").split("\n");


			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Add some objects to favorites
			//---------------------------------
			for(int count = 0; count < objects.length; count++) {
				homePage.searchPanel.search(objects[count], searchType[count]);
				Utils.fluentWait(driver);

				if(!homePage.listView.isItemExists(objects[count]))
					throw new SkipException("The specified object does not exist in the vault.");

				homePage.listView.rightClickItem(objects[count]);
				Utils.fluentWait(driver);
				homePage.listView.clickContextMenuItem(Caption.MenuItems.AddToFavorites.Value);
				Utils.fluentWait(driver);
				MFilesDialog mFilesDialog = new MFilesDialog(driver);
				mFilesDialog.clickOkButton();
				Utils.fluentWait(driver);
			}

			Log.message("2. Checked out some objects.");

			//3. Navigate to favorites view
			//------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Favorites.Value);
			Utils.fluentWait(driver);

			Log.message("3. Navigate to favorites view");

			//4. Sort the View using a newly inserted column
			//-----------------------------------------------
			homePage.listView.insertColumn(dataPool.get("Column"));
			Utils.fluentWait(driver);
			homePage.listView.clickColumn(dataPool.get("Column"));
			Utils.fluentWait(driver);

			Log.message("4. Sorted the View using a newly inserted column");

			//5. Navigate to Home View
			//-------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);
			Utils.fluentWait(driver);

			Log.message("5. Navigated to Home View");

			//5. Navigate back into Favorites
			//--------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Favorites.Value);
			Utils.fluentWait(driver);

			Log.message("6. Navigated back into Favorites.");

			//6. Check the Sort Image
			//-------------------------
			int count = 0;
			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("Column"));

			List<String> versions = homePage.listView.getColumnValues(dataPool.get("Column"));

			for(count = 0; count < versions.size()-1; count++) {
				if(Integer.parseInt(versions.get(count)) > Integer.parseInt(versions.get(count+1)))
					break;
			}

			Log.message("7. Checked the Sort Image");

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
			if (objects != null && homePage != null && driver != null)
			{
				try
				{					
					homePage.listView.removeColumn(dataPool.get("Column"));
					for(int count = 0; count < objects.length; count++) {
						if(!homePage.listView.isItemExists(objects[count]))
							throw new SkipException("The specified object does not exist in the vault.");

						homePage.listView.rightClickItem(objects[count]);
						Utils.fluentWait(driver);
						homePage.listView.clickContextMenuItem(Caption.MenuItems.RemoveFromFavorites.Value);
						Utils.fluentWait(driver);
						MFilesDialog mFilesDialog = new MFilesDialog(driver);
						mFilesDialog.clickOkButton();
						Utils.fluentWait(driver);
						mFilesDialog = new MFilesDialog(driver);
						mFilesDialog.clickOkButton();
						Utils.fluentWait(driver);

					}
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}

			Utility.quitDriver(driver);
		}

	}

	/**
	 * 33.3.25A : Verify the Default sorting in Favorites view should be based on Name column
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint33", "Columns"}, 
			description = "Verify the Default sorting in Favorites view should be based on Name column.")
	public void SprintTest33_3_25A(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null;
		HomePage homePage = null;
		ConcurrentHashMap <String, String> dataPool = null;

		String[] objects = null, searchType = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			objects = dataPool.get("Object").split("\n");
			searchType = dataPool.get("SearchType").split("\n");

			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Add out some objects
			//-------------------------
			for(int count = 0; count < objects.length; count++) {
				homePage.searchPanel.search(objects[count], searchType[count]);
				Utils.fluentWait(driver);

				if(!homePage.listView.isItemExists(objects[count]))
					throw new SkipException("The specified object does not exist in the vault.");

				homePage.listView.rightClickItem(objects[count]);
				Utils.fluentWait(driver);
				homePage.listView.clickContextMenuItem(Caption.MenuItems.AddToFavorites.Value);
				Utils.fluentWait(driver);
				MFilesDialog mFilesDialog = new MFilesDialog(driver);
				mFilesDialog.clickOkButton();
				Utils.fluentWait(driver);
			}

			Log.message("2. Add some objects to favorites.");

			//3. Navigate to Recently Accessed by Me View
			//---------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.RecentlyAccessedByMe.Value);
			Utils.fluentWait(driver);

			Log.message("3. Navigate to Recently Accessed by Me View.");

			//4. Check the Sort Image
			//-------------------------
			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("Column"));

			List<String> versions = homePage.listView.getColumnValues(dataPool.get("Column"));

			boolean isValuesSorted = homePage.listView.isColumnValuesSorted(versions, dataPool.get("DataType"), dataPool.get("SortOrder"));

			Log.message("4. Checked the Sort Image");

			//Verification: To verify if the Column is sorted as expected
			//------------------------------------------------------------
			if(sortImage.equals(dataPool.get("ExpectedSortImage")) && isValuesSorted) 
				Log.pass("Test Case Passed. The Column '" + dataPool.get("Column") + "' had the expected sort.");
			else
				Log.fail("Test Case Failed. The Column '" + dataPool.get("Column") + "' did not have the expected sort.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {

			if (objects != null && homePage != null && driver != null)
			{
				try
				{
					homePage.taskPanel.clickItem(Caption.MenuItems.Favorites.Value);
					Utils.fluentWait(driver);

					for(int count = 0; count < objects.length; count++) {
						if(!homePage.listView.isItemExists(objects[count]))
							throw new SkipException("The specified object does not exist in the vault.");

						homePage.listView.rightClickItem(objects[count]);
						Utils.fluentWait(driver);
						homePage.listView.clickContextMenuItem(Caption.MenuItems.RemoveFromFavorites.Value);
						Utils.fluentWait(driver);
						MFilesDialog mFilesDialog = new MFilesDialog(driver);
						mFilesDialog.clickOkButton();
						Utils.fluentWait(driver);
						mFilesDialog = new MFilesDialog(driver);
						mFilesDialog.clickOkButton();
						Utils.fluentWait(driver);
					}
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 33.3.25B: Verify Changing Sort order in Favorites view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint33", "Columns"}, 
			description = "Verify Changing Sort order in Favorites view.")
	public void SprintTest33_3_25B(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null;
		HomePage homePage = null;
		ConcurrentHashMap <String, String> dataPool = null;

		String[] objects = null, searchType = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			objects = dataPool.get("Object").split("\n");
			searchType = dataPool.get("SearchType").split("\n");

			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Add some objects to favorites
			//---------------------------------
			for(int count = 0; count < objects.length; count++) {
				homePage.searchPanel.search(objects[count], searchType[count]);
				Utils.fluentWait(driver);

				if(!homePage.listView.isItemExists(objects[count]))
					throw new SkipException("The specified object does not exist in the vault.");

				homePage.listView.rightClickItem(objects[count]);
				Utils.fluentWait(driver);
				homePage.listView.clickContextMenuItem(Caption.MenuItems.AddToFavorites.Value);
				Utils.fluentWait(driver);
				MFilesDialog mFilesDialog = new MFilesDialog(driver);
				mFilesDialog.clickOkButton();
				Utils.fluentWait(driver);
			}

			Log.message("2. Add some objects to favorites.");

			//3. Navigate to Recently Accessed by Me View
			//---------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.RecentlyAccessedByMe.Value);
			Utils.fluentWait(driver);

			Log.message("3. Navigate to Recently Accessed by Me View.");

			//4. Click on the column to change it's sorting order
			//----------------------------------------------------
			homePage.listView.clickColumn(dataPool.get("Column"));
			Utils.fluentWait(driver);

			Log.message("4. Clicked on the column to change it's sorting order");

			//5. Navigate to Home View
			//-------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);
			Utils.fluentWait(driver);

			Log.message("5. Navigated to Home View");

			//5. Navigate back into Recently Accessed by Me View
			//---------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.RecentlyAccessedByMe.Value);
			Utils.fluentWait(driver);

			Log.message("6. Navigate back into Recently Accessed by Me View.");

			//6. Check the Sort Image
			//-------------------------
			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("Column"));

			List<String> versions = homePage.listView.getColumnValues(dataPool.get("Column"));

			boolean isValuesSorted = homePage.listView.isColumnValuesSorted(versions, dataPool.get("DataType"), dataPool.get("SortOrder"));

			homePage.listView.clickColumn(dataPool.get("Column"));
			Utils.fluentWait(driver);

			Log.message("5. Checked the Sort Image");

			//Verification: To verify if the Column is sorted as expected
			//------------------------------------------------------------
			if(sortImage.equals(dataPool.get("ExpectedSortImage")) && isValuesSorted) 
				Log.pass("Test Case Passed. The Column '" + dataPool.get("Column") + "' had the expected sort.");
			else
				Log.fail("Test Case Failed. The Column '" + dataPool.get("Column") + "' did not have the expected sort.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			if (objects != null && homePage != null && driver != null)
			{
				try
				{
					homePage.taskPanel.clickItem(Caption.MenuItems.Favorites.Value);
					Utils.fluentWait(driver);

					for(int count = 0; count < objects.length; count++) {

						if(!homePage.listView.isItemExists(objects[count]))
							throw new SkipException("The specified object does not exist in the vault.");

						homePage.listView.rightClickItem(objects[count]);
						Utils.fluentWait(driver);
						homePage.listView.clickContextMenuItem(Caption.MenuItems.RemoveFromFavorites.Value);
						Utils.fluentWait(driver);
						MFilesDialog mFilesDialog = new MFilesDialog(driver);
						mFilesDialog.clickOkButton();
						Utils.fluentWait(driver);
						mFilesDialog = new MFilesDialog(driver);
						mFilesDialog.clickOkButton();
						Utils.fluentWait(driver);
					}
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 33.3.25C : Sort Favorites view using a new inserted column
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint33", "Columns"}, 
			description = "Sort Favorites view using a new inserted column.")
	public void SprintTest33_3_25C(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null;
		HomePage homePage = null;
		ConcurrentHashMap <String, String> dataPool = null;

		String[] objects = null, searchType = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			objects = dataPool.get("Object").split("\n");
			searchType = dataPool.get("SearchType").split("\n");

			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Add some objects to favorites
			//---------------------------------
			for(int count = 0; count < objects.length; count++) {
				homePage.searchPanel.search(objects[count], searchType[count]);
				Utils.fluentWait(driver);

				if(!homePage.listView.isItemExists(objects[count]))
					throw new SkipException("The specified object does not exist in the vault.");

				homePage.listView.rightClickItem(objects[count]);
				Utils.fluentWait(driver);
				homePage.listView.clickContextMenuItem(Caption.MenuItems.AddToFavorites.Value);
				Utils.fluentWait(driver);
				MFilesDialog mFilesDialog = new MFilesDialog(driver);
				mFilesDialog.clickOkButton();
				Utils.fluentWait(driver);
			}

			Log.message("2. Checked out some objects.");

			//3. Navigate to Recently Accessed by Me View
			//---------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.RecentlyAccessedByMe.Value);
			Utils.fluentWait(driver);

			Log.message("3. Navigate to Recently Accessed by Me View.");

			//4. Sort the View using a newly inserted column
			//-----------------------------------------------
			homePage.listView.insertColumn(dataPool.get("NewColumn"));
			Utils.fluentWait(driver);
			homePage.listView.clickColumn(dataPool.get("NewColumn"));
			Utils.fluentWait(driver);

			Log.message("4. Sorted the View using a newly inserted column");

			//5. Navigate to Home View
			//-------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);
			Utils.fluentWait(driver);

			Log.message("5. Navigated to Home View");

			//5. Navigate back into Recently Accessed by Me
			//----------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.RecentlyAccessedByMe.Value);
			Utils.fluentWait(driver);

			Log.message("6. Navigate back into Recently Accessed by Me.");

			//6. Check the Sort Image
			//-------------------------
			if(homePage.listView.isColumnExists(dataPool.get("NewColumn")))
				homePage.listView.removeColumn(dataPool.get("NewColumn"));
			else
				throw new Exception("The newly inserted column was not present in the view.");

			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("Column"));

			List<String> versions = homePage.listView.getColumnValues(dataPool.get("Column"));

			boolean isValuesSorted = homePage.listView.isColumnValuesSorted(versions, dataPool.get("DataType"), dataPool.get("SortOrder"));

			Log.message("6. Checked the Sort Image");

			//Verification: To verify if the Column is sorted as expected
			//------------------------------------------------------------
			if(sortImage.equals(dataPool.get("ExpectedSortImage")) && isValuesSorted) 
				Log.pass("Test Case Passed. The Column '" + dataPool.get("Column") + "' had the expected sort.");
			else
				Log.fail("Test Case Failed. The Column '" + dataPool.get("Column") + "' did not have the expected sort.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			if (objects != null && homePage != null && driver != null)
			{
				try
				{
					homePage.taskPanel.clickItem(Caption.MenuItems.Favorites.Value);
					Utils.fluentWait(driver);
					for(int count = 0; count < objects.length; count++) {
						if(!homePage.listView.isItemExists(objects[count]))
							throw new SkipException("The specified object does not exist in the vault.");

						homePage.listView.rightClickItem(objects[count]);
						Utils.fluentWait(driver);
						homePage.listView.clickContextMenuItem(Caption.MenuItems.RemoveFromFavorites.Value);
						Utils.fluentWait(driver);
						MFilesDialog mFilesDialog = new MFilesDialog(driver);
						mFilesDialog.clickOkButton();
						Utils.fluentWait(driver);
						mFilesDialog = new MFilesDialog(driver);
						mFilesDialog.clickOkButton();
						Utils.fluentWait(driver);

					}
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		}

	}

	/**
	 * 33.3.26 : Sort Favorites view using a new inserted column
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint33", "Columns"}, 
			description = "Sort Favorites view using a new inserted column.")
	public void SprintTest33_3_26(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null;
		HomePage homePage = null;
		ConcurrentHashMap <String, String> dataPool = null;

		String[] objects = null, searchType = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			objects = dataPool.get("Object").split("\n");
			searchType = dataPool.get("SearchType").split("\n");

			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Add some objects to favorites
			//---------------------------------
			for(int count = 0; count < objects.length; count++) {
				homePage.searchPanel.search(objects[count], searchType[count]);
				Utils.fluentWait(driver);

				if(!homePage.listView.isItemExists(objects[count]))
					throw new SkipException("The specified object does not exist in the vault.");

				homePage.listView.rightClickItem(objects[count]);
				Utils.fluentWait(driver);
				homePage.listView.clickContextMenuItem(Caption.MenuItems.AddToFavorites.Value);
				Utils.fluentWait(driver);
				MFilesDialog mFilesDialog = new MFilesDialog(driver);
				mFilesDialog.clickOkButton();
				Utils.fluentWait(driver);
			}

			Log.message("2. Checked out some objects.");

			//3. Navigate to Recently Accessed by Me View
			//---------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.RecentlyAccessedByMe.Value);
			Utils.fluentWait(driver);

			Log.message("3. Navigate to Recently Accessed by Me View.");

			//4. Sort the View using a newly inserted column
			//-----------------------------------------------
			homePage.listView.removeColumn(dataPool.get("Column"));
			Utils.fluentWait(driver);

			Log.message("4. Sorted the View using a newly inserted column");

			//5. Navigate to Home View
			//-------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);
			Utils.fluentWait(driver);

			Log.message("5. Navigated to Home View");

			//5. Navigate back into Recently Accessed by Me
			//----------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.RecentlyAccessedByMe.Value);
			Utils.fluentWait(driver);

			Log.message("6. Navigate back into Recently Accessed by Me.");

			//6. Check the Sort Image
			//-------------------------
			if(!homePage.listView.isColumnExists(dataPool.get("Column")))
				throw new Exception("The column '" + dataPool.get("Column") + "' was not present in the view.");

			String sortImage = homePage.listView.getColumnSortImage(dataPool.get("Column"));

			List<String> versions = homePage.listView.getColumnValues(dataPool.get("Column"));

			boolean isValuesSorted = homePage.listView.isColumnValuesSorted(versions, dataPool.get("DataType"), dataPool.get("SortOrder"));

			Log.message("6. Checked the Sort Image");

			//Verification: To verify if the Column is sorted as expected
			//------------------------------------------------------------
			if(sortImage.equals(dataPool.get("ExpectedSortImage")) && isValuesSorted) 
				Log.pass("Test Case Passed. The Column '" + dataPool.get("Column") + "' had the expected sort.");
			else
				Log.fail("Test Case Failed. The Column '" + dataPool.get("Column") + "' did not have the expected sort.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			if (objects != null && homePage != null && driver != null)
			{
				try
				{
					homePage.taskPanel.clickItem(Caption.MenuItems.Favorites.Value);
					Utils.fluentWait(driver);
					for(int count = 0; count < objects.length; count++) {
						if(!homePage.listView.isItemExists(objects[count]))
							throw new SkipException("The specified object does not exist in the vault.");

						homePage.listView.rightClickItem(objects[count]);
						Utils.fluentWait(driver);
						homePage.listView.clickContextMenuItem(Caption.MenuItems.RemoveFromFavorites.Value);
						Utils.fluentWait(driver);
						MFilesDialog mFilesDialog = new MFilesDialog(driver);
						mFilesDialog.clickOkButton();
						Utils.fluentWait(driver);
						mFilesDialog = new MFilesDialog(driver);
						mFilesDialog.clickOkButton();
						Utils.fluentWait(driver);

					}
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		}

	}


	/**
	 * 38.1.20.1B : 'Sort Ascending' cannot be performed on right clicking at empty column header
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint38"}, 
			description = "'Sort Ascending' cannot be performed on right clicking at empty column header.")
	public void SprintTest38_1_20_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Right click on empty column header
			//-------------------------------------------
			homePage.listView.rightClickOnEmptyHeader();

			Log.message("2. Right clicked on empty column header.");

			//Verification : To Verify if Sort Ascending is in disabled state
			//---------------------------------------------------------------			
			if (!homePage.listView.itemEnabledInContextMenu(Caption.Column.SortAscending.Value))
				Log.pass("Test case Passed. 'Sort Ascending' is in disabled state.");
			else
				Log.fail("Test case Failed. 'Sort Ascending' is not in disabled state.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest38_1_20_1B

	/**
	 * 38.1.20.1C : 'Sort Descending' cannot be performed on right clicking at empty column header
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint38"}, 
			description = "'Sort Descending' cannot be performed on right clicking at empty column header.")
	public void SprintTest38_1_20_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Right click on empty column header
			//-------------------------------------------
			homePage.listView.rightClickOnEmptyHeader();

			Log.message("2. Right clicked on empty column header.");

			//Verification : To Verify if Sort Descending is in disabled state
			//---------------------------------------------------------------------------			
			if (!homePage.listView.itemEnabledInContextMenu(Caption.Column.SortDescending.Value))
				Log.pass("Test case Passed. 'Sort Descending' is in disabled state.");
			else
				Log.fail("Test case Failed. 'Sort Descending' is not in disabled state.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest38_1_20_1C


	/**
	 * 38.1.20.2C : 'Sort Descending' cannot be performed on right clicking at refresh icon
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint38"}, 
			description = "'Sort Descending' cannot be performed on right clicking at refresh icon.")
	public void SprintTest38_1_20_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Right click on refresh icon
			//------------------------------------
			homePage.listView.rightClickRefreshIcon();

			Log.message("2. Right clicked on refreh icon.");

			//Verification : To Verify if Sort Descending is in disabled state
			//-----------------------------------------------------------------			
			if (!homePage.listView.itemEnabledInContextMenu(Caption.Column.SortDescending.Value))
				Log.pass("Test case Passed. 'Sort Descending' is in disabled state.");
			else
				Log.fail("Test case Failed. 'Sort Descending' is not in disabled state.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest38_1_20_2C

	/**
	 * 38.1.20.2B : 'Sort Ascending' cannot be performed on right clicking at refresh icon
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint38"}, 
			description = "'Sort Ascending' cannot be performed on right clicking at refresh icon.")
	public void SprintTest38_1_20_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Right click on refresh icon
			//------------------------------------
			homePage.listView.rightClickRefreshIcon();

			Log.message("2. Right clicked on refreh icon.");

			//Verification : To Verify if Sort Ascending is in disabled state
			//---------------------------------------------------------------			
			if (!homePage.listView.itemEnabledInContextMenu(Caption.Column.SortAscending.Value))
				Log.pass("Test case Passed. 'Sort Ascending' is in disabled state.");
			else
				Log.fail("Test case Failed. 'Sort Ascending' is not in disabled state.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest38_1_20_2B

} //End class Columns
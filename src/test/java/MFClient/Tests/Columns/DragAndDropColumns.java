package MFClient.Tests.Columns;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.WebDriverUtils;

import java.util.Arrays;
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
import MFClient.Wrappers.ListView;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class DragAndDropColumns {

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
			Utility.destroyUsers(xlTestDataWorkBook);
			Utility.destroyTestVault();

		}//End try

		catch(Exception e){
			throw e;
		}//End Catch
	}//End cleanApp



	/**
	 * 38.1.8.1A : Drag and Drop the column to the left side
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint38"}, 
			description = "Drag and Drop the column to the left side")
	public void SprintTest38_1_8_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("Safari"))
			throw new SkipException(driverType + " driver does not support Actions.");

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

			//Step-2 : Perform Drag and Drop operation for the column
			//--------------------------------------------------------
			if (!homePage.listView.isColumnExists(dataPool.get("ColumnName")))
				throw new SkipException("Column (" + dataPool.get("ColumnName") + ") does not exists in the list.");

			Boolean isDragged = homePage.listView.dragColumn(dataPool.get("ColumnName"), true);

			Log.message("2. Column (" + dataPool.get("ColumnName") + ") is performed with drag and drop operation.");

			//Verification : To Verify if column is dragged and dropped
			//-----------------------------------------------------------			
			if (isDragged)
				Log.pass("Test case Passed. Column (" + dataPool.get("ColumnName") + ") dragged and dropped to left side successfully.");
			else
				Log.fail("Test case Failed. Column (" + dataPool.get("ColumnName") + ") is not dragged to the left position.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest38_1_8_1A

	/**
	 * 38.1.8.1B : Drag and Drop the column to the right side
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint38"}, 
			description = "Drag and Drop the column to the right side")
	public void SprintTest38_1_8_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("Safari"))
			throw new SkipException(driverType + " driver does not support Actions.");

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

			//Step-2 : Perform Drag and Drop operation for the column
			//--------------------------------------------------------
			if (!homePage.listView.isColumnExists(dataPool.get("ColumnName")))
				throw new SkipException("Column (" + dataPool.get("ColumnName") + ") does not exists in the list.");

			Boolean isDragged = homePage.listView.dragColumn(dataPool.get("ColumnName"), false);

			Log.message("2. Column (" + dataPool.get("ColumnName") + ") is performed with drag and drop operation.");

			//Verification : To Verify if column is dragged and dropped
			//-----------------------------------------------------------			
			if (isDragged)
				Log.pass("Test case Passed. Column (" + dataPool.get("ColumnName") + ") dragged and dropped to right side successfully.");
			else
				Log.fail("Test case Failed. Column (" + dataPool.get("ColumnName") + ") is not dragged to the right position.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest38_1_8_1B

	/**
	 * 38.1.8.2A : Drag and Drop the column should not change the sort indicator for the column
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint38"}, 
			description = "Drag and Drop the column should not change the sort indicator for the column")
	public void SprintTest38_1_8_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("Safari"))
			throw new SkipException(driverType + " driver does not support Actions.");

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

			//Step-2 : Perform Drag and Drop operation for the column
			//--------------------------------------------------------
			if (!homePage.listView.isColumnExists(dataPool.get("ColumnName")))
				throw new SkipException("Column (" + dataPool.get("ColumnName") + ") does not exists in the list.");

			String sortIndicatorColn = homePage.listView.getSortedColumn();

			if (!homePage.listView.dragColumn(dataPool.get("ColumnName"), true))
				throw new Exception("Column (" + dataPool.get("ColumnName") + ") is not dragged.");

			Log.message("2. Column (" + dataPool.get("ColumnName") + ") is dragged and dropped.");

			//Verification : To Verify if column is dragged and dropped
			//-----------------------------------------------------------			
			if (homePage.listView.getSortedColumn().equalsIgnoreCase(sortIndicatorColn))
				Log.pass("Test case Passed. Drag and Drop operation does not change the column's sort indicator.");
			else
				Log.fail("Test case Failed. Drag and Drop operation changed the column's sort indicator.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest38_1_8_2A

	/**
	 * 38.1.8.2B : Drag and Drop the column with sort indicator
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint38"}, 
			description = "Drag and Drop the column should not change the sort indicator for the column")
	public void SprintTest38_1_8_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("Safari"))
			throw new SkipException(driverType + " driver does not support Actions.");

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

			//Step-2 : Perform Drag and Drop operation for the column
			//--------------------------------------------------------
			String sortIndicatorColn = homePage.listView.getSortedColumn();

			if (!homePage.listView.dragColumn(sortIndicatorColn, true))
				throw new Exception("Column (" + sortIndicatorColn + ") is not dragged.");

			Log.message("2. Sort indicator column (" + sortIndicatorColn + ") is dragged and dropped.");

			//Verification : To Verify sort indicator does not changed on performing drag and drop operation
			//----------------------------------------------------------------------------------------------			
			if (homePage.listView.getSortedColumn().equalsIgnoreCase(sortIndicatorColn))
				Log.pass("Test case Passed. Drag and Drop operation in sort indicator column does not change the column's sort indicator.");
			else
				Log.fail("Test case Failed. Drag and Drop operation in sort indicator column changed the column's sort indicator.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest38_1_8_2B

	/**
	 * 38.1.9A : Empty column should not be added on performing drag and drop for column with sort indicator
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint38"}, 
			description = "Empty column should not be added on performing drag and drop for column with sort indicator")
	public void SprintTest38_1_9A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("Safari"))
			throw new SkipException(driverType + " driver does not support Actions.");

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

			//Step-2 : Perform Drag and Drop operation for the column
			//--------------------------------------------------------
			String sortIndicatorColn = homePage.listView.getSortedColumn();

			int colnCt = homePage.listView.getColumnCount(); //Gets the number of columns in the list

			if (!homePage.listView.dragColumn(sortIndicatorColn, false))
				throw new Exception("Column (" + sortIndicatorColn + ") is not dragged.");

			Log.message("2. Column (" + sortIndicatorColn + ") is dragged and dropped.");

			//Verification : To Verify if column is dragged and dropped
			//-----------------------------------------------------------			
			if (homePage.listView.getColumnCount() == colnCt)
				Log.pass("Test case Passed. Drag and Drop operation of sort indicator column does not added the column.");
			else
				Log.fail("Test case Failed. Drag and Drop operation  of sort indicator column added the column to the list.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest38_1_9A

	/**
	 * 38.1.9B : Empty column should not be added on performing drag and drop for column without sort indicator
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint38"}, 
			description = "Empty column should not be added on performing drag and drop for column without sort indicator")
	public void SprintTest38_1_9B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("Safari"))
			throw new SkipException(driverType.toUpperCase() + " driver does not support Actions.");

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

			//Step-2 : Perform Drag and Drop operation for the column
			//--------------------------------------------------------
			if (!homePage.listView.isColumnExists(dataPool.get("ColumnName")))
				throw new SkipException("Column (" + dataPool.get("ColumnName") + ") does not exists in the list.");

			int colnCt = homePage.listView.getColumnCount(); //Gets the number of columns in the list

			if (!homePage.listView.dragColumn(dataPool.get("ColumnName"), true))
				throw new Exception("Column (" + dataPool.get("ColumnName") + ") is not dragged.");

			Log.message("2. Column (" + dataPool.get("ColumnName") + ") is dragged and dropped.");

			//Verification : To Verify if column is dragged and dropped
			//-----------------------------------------------------------			
			if (homePage.listView.getColumnCount() == colnCt)
				Log.pass("Test case Passed. Drag and Drop operation does not added the column.");
			else
				Log.fail("Test case Failed. Drag and Drop operation added the column to the list.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest38_1_9B

	/**
	 * 38.1.10A : Column should able be remove after performing drag and drop operation
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint38"}, 
			description = "Column should able be remove after performing drag and drop operation")
	public void SprintTest38_1_10A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("Safari"))
			throw new SkipException(driverType.toUpperCase() + " driver does not support Actions.");

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

			//Step-2 : Perform Drag and Drop operation for the column
			//--------------------------------------------------------
			if (!homePage.listView.isColumnExists(dataPool.get("ColumnName")))
				throw new SkipException("Column (" + dataPool.get("ColumnName") + ") does not exists in the list.");

			if (!homePage.listView.dragColumn(dataPool.get("ColumnName"), true))
				throw new Exception("Column (" + dataPool.get("ColumnName") + ") is not dragged.");

			Log.message("2. Column (" + dataPool.get("ColumnName") + ") is dragged and dropped.");

			//Step-3 : Remove the dragged column
			//----------------------------------
			Boolean isRemoved = homePage.listView.removeColumn(dataPool.get("ColumnName"));

			Log.message("3. Remove Column is selected for the dragged column.");

			//Insert back the removed column
			homePage.listView.insertColumn(dataPool.get("ColumnName"));

			//Verification : To Verify if column is removed after drag and drop operation
			//---------------------------------------------------------------------------			
			if (isRemoved)
				Log.pass("Test case Passed. Column is able to remove after drag and drop operation.");
			else
				Log.fail("Test case Failed. Column is not able to remove after drag and drop operation.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest38_1_10A

	/**
	 * 38.1.10B : Sort Indicator should be moved to the next column, if column with sort indicator is swapped and removed
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint38"}, 
			description = "Sort Indicator should be moved to the next column, if column with sort indicator is swapped and removed")
	public void SprintTest38_1_10B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("Safari"))
			throw new SkipException(driverType.toUpperCase() + " driver does not support Actions.");

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

			//Step-2 : Perform Drag and Drop operation for the column
			//--------------------------------------------------------
			String sortColn = homePage.listView.getSortedColumn(); //Gets the sort indicator column

			if (!homePage.listView.dragColumn(sortColn, true))
				throw new Exception("Sort indicator column (" + sortColn + ") is not dragged.");

			Log.message("2. Sort indicator column (" + sortColn + ") is dragged and dropped.");

			//Step-3 : Remove the dragged column
			//----------------------------------
			if (!homePage.listView.removeColumn(sortColn))
				throw new Exception("Sort indicator column (" + sortColn + ") is unable to remove after drag and drop operation.");

			Log.message("3. Sort indicator column (" + sortColn + ") is removed after performing drag and drop operation.");

			//Verification : To Verify if sort indicator is moved to some other column
			//------------------------------------------------------------------------		
			String newSortColn = homePage.listView.getSortedColumn();

			if (!newSortColn.equalsIgnoreCase("") && !newSortColn.equalsIgnoreCase(sortColn))
				Log.pass("Test case Passed. Sort Indicator is moved to '" + newSortColn + "' after removing '" + sortColn + "' column.");
			else
				Log.fail("Test case Failed. Sort indicator is not moved to any column after removing '" + sortColn + "' column.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest38_1_10B

	/**
	 * 38.1.11A : Navigating to Home View should be possible from any view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint38"}, 
			description = "Navigating to Home View should be possible from any view.")
	public void SprintTest38_1_11A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-2 : Click Home from task pane
			//----------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);

			Log.message("2. Home is clicked from taskpane.");

			//Verification : Check if view is navigated to Home view
			//-----------------------------------------------------		
			if (ListView.isViewNavigated(driver, Caption.MenuItems.Home.Value))
				Log.pass("Test case Passed. Navigation from '" + dataPool.get("NavigateToView") + "' view to '" + Caption.MenuItems.Home.Value + "' is successful.");
			else 
				Log.fail("Test case Failed. Navigation from '" + dataPool.get("NavigateToView") + "' view to '" + Caption.MenuItems.Home.Value + "' is not successful.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest38_1_11A

	/**
	 * 38.1.11B : Column setting should be retained in a view after navigating from any view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint38"}, 
			description = "Column setting should be retained in a view after navigating from any view.")
	public void SprintTest38_1_11B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String fromView = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("FromView"), dataPool.get("SearchWord1"));
			int prevFromViewColnCt = homePage.listView.getColumnCount();			
			String [] preFromViewColns = homePage.listView.getVisibleColumns();

			Log.message("1. Navigated to '" + fromView + "' view.");

			//Step-2 : Navigate to View
			//-------------------------
			String toView = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ToView"), dataPool.get("SearchWord2"));

			Log.message("2. Navigated to '" + toView + "' view.");

			//Step-3 : Navigate back to first View
			//-------------------------------------
			fromView = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("FromView"), dataPool.get("SearchWord1"));
			int currFromViewColnCt = homePage.listView.getColumnCount();			
			String [] currFromViewColns = homePage.listView.getVisibleColumns();

			Log.message("3. Navigated back to '" + fromView + "' view.");

			//Verification : To verify if column settings is peristent
			//---------------------------------------------------------
			if (prevFromViewColnCt != currFromViewColnCt) //Verifies if number of columns are same
				throw new Exception("Test case Failed. Number of columns in a view (" + fromView + ") is not same before and after navigation");

			if (Arrays.equals(preFromViewColns, currFromViewColns))
				Log.pass("Test case Passed. Column settings are peristent after navigating from '" + fromView + "' view to '" + toView + "' is successful.");
			else 
				Log.fail("Test case Failed. Column settings are not peristent after navigating from '" + fromView + "' view to '" + toView + "'.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest38_1_11B

	/**
	 * 38.1.12 : Resize the column after swapping
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint38"}, 
			description = "Resize the column after swapping.")
	public void SprintTest38_1_12(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("Safari"))
			throw new SkipException(driverType.toUpperCase() + " driver does not support Actions.");

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

			//Step-2 : Perform Drag and Drop operation for the column
			//--------------------------------------------------------
			if (!homePage.listView.isColumnExists(dataPool.get("ColumnName")))
				throw new SkipException("Column (" + dataPool.get("ColumnName") + ") does not exists in the list.");

			int colnSize = homePage.listView.getColumnSize(dataPool.get("ColumnName")); //Gets the size of the column

			if (!homePage.listView.dragColumn(dataPool.get("ColumnName"), true))
				throw new Exception("Column (" + dataPool.get("ColumnName") + ") is not dragged.");

			Log.message("2. Column (" + dataPool.get("ColumnName") + ") is dragged and dropped.");

			//Step-3 : Resize the dragged column
			//----------------------------------
			int newColnSize = homePage.listView.resizeColumn(dataPool.get("ColumnName"), 200);

			Log.message("3. Column is dragged at resizer element.");

			//Verification : To Verify if column is remvoed after drag and drop operation
			//---------------------------------------------------------------------------			
			if (colnSize != newColnSize)
				Log.pass("Test case Passed. Column is resized after drag and drop operation.");
			else
				Log.fail("Test case Failed. Column is not resized after drag and drop operation.", driver);					
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest38_1_12

} //End class Columns
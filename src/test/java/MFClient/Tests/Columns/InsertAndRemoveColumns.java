package MFClient.Tests.Columns;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.Utils;
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




import MFClient.Pages.ConfigurationPage;
import MFClient.Pages.HomePage;
import MFClient.Pages.LoginPage;
import MFClient.Wrappers.Caption;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class InsertAndRemoveColumns {

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
	 * 3.3.1 : Default columns of the view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint3", "Columns"}, 
			description = "Default columns of the view")
	public void SprintTest3_3_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			String[] colnName = dataPool.get("DefaultColumns").split(",");

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewName"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Verification : To Verify if default columns are present
			//---------------------------------------------------------
			Boolean tcPass = true;

			for (int i=0; i<colnName.length; i++) //Checks if default columns are available
				if (!homePage.listView.isColumnExists(colnName[i])) {
					tcPass = false;
					break;
				}

			//Verifies that default columns are available in the view
			if (tcPass)
				Log.pass("Test case Passed. Default Columns are present in " + viewToNavigate + " view.");
			else
				Log.fail("Test case Failed. Default Columns does not exists in " + viewToNavigate + ".", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest3_3_1

	/**
	 * 3.3.2 : Adding new column in view does not remove current selection in listing
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint3", "Columns", "Smoke"}, 
			description = "Adding new column in view does not remove current selection in listing")
	public void SprintTest3_3_2(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			String colnName = dataPool.get("ColumnName");

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewName"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2 : Select the first object 
			//---------------------------------
			if (!homePage.listView.clickItemByIndex(0)) //Clicks the first item in the list
				throw new SkipException("First item in the list is not selected.");

			Log.message("2. First Object in the list is selected.");

			//Step-3 : Insert the column
			//--------------------------
			if (homePage.listView.isColumnExists(colnName))  //Checks if column already exists
				throw new SkipException("Invalid test data. Column (" + colnName + ") already exists.");

			if (!homePage.listView.insertColumn(dataPool.get("ColumnName")))
				throw new Exception("Column (" + colnName + ") does not exists after its insertion.");

			Log.message("3. Column (" + colnName + ") is inserted.");

			//Verification : To Verify if selection persists after the column insertion
			//-------------------------------------------------------------------------
			String objectName = homePage.listView.getItemNameByItemIndex(0);

			//Verifies that focus of the current item is next item of the selected item
			if (homePage.listView.isItemSelected(objectName))
				Log.pass("Test case Passed. Object is in selected state after inserting the column.");
			else
				Log.fail("Test case Failed. Object is not in the selected state after inserting the column.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest3_3_2

	/**
	 * 3.3.3 : Removing column by un-checking in insert column
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint3", "Columns", "Smoke", "Script"}, 
			dependsOnMethods = "SprintTest3_3_2", description = "Removing column by un-checking in insert column")
	public void SprintTest3_3_3(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			String colnName = dataPool.get("ColumnName");

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewName"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2 : Un-Select column by insert column
			//------------------------------------------
			if (!homePage.listView.isColumnExists(colnName)) //Checks if column already exists
				throw new SkipException("Invalid test data. Column (" + colnName + ") does not exists.");

			homePage.listView.insertColumn(dataPool.get("ColumnName"));

			Log.message("2. Column (" + colnName + ") is unselected.");

			// Verification : Verify if the column is removed from the list
			//-----------------------------------------------------------

			//Verifies that removed column does not exists in the list
			if (!homePage.listView.isColumnExists(colnName))
				Log.pass("Test case Passed. Column is removed successfully by unchecking in insert column.");
			else
				Log.fail("Test case Failed. Column is not removed by unchecking in insert column.", driver);
		}

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest3_3_3

	/**
	 * 15.1.1 : Default options in Insert Columns menu - Try this from different views 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint15", "Columns", "Script"}, 
			description = "Default options should be available in Insert Columns menu")
	public void SprintTest15_1_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewName"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2: Right click on the column header
			//-----------------------------------------
			homePage.listView.rightClickColumn();
			Utils.fluentWait(driver);
			homePage.listView.clickColumnHeaderContextMenu(Caption.Column.InsertColumn.Value);
			Utils.fluentWait(driver);

			Log.message("2. Right click on the column header.");

			//Verification: To verify if all the expected options are available in the context menu
			//--------------------------------------------------------------------------------------
			String menu = dataPool.get("Menu");
			String allMenuItems[] = menu.split(",");
			int count = 0;

			for (count = 0; count < allMenuItems.length; count++)
				if(!homePage.listView.itemExistsInContextMenu(allMenuItems[count].trim()))
					break;

			if (count == allMenuItems.length)
				Log.pass("Test Case Passed. All the expected options were available in the context menu.");
			else
				Log.fail("Test Case Failed. The expected context menu " + allMenuItems[count] + " was not found.", driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		}

	} //End SprintTest15_1_1

	/**
	 * 15.1.2 : Default options in Standard Columns  - Try this from different views 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint15", "Columns", "Script"}, 
			description = "Default options in Standard Columns  - Try this from different views")
	public void SprintTest15_1_2(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewName"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2: Right click on the column header
			//-----------------------------------------
			homePage.listView.rightClickColumn();
			Utils.fluentWait(driver);
			homePage.listView.clickColumnHeaderContextMenu(Caption.Column.InsertColumn.Value+">>Standard Columns");
			Utils.fluentWait(driver);

			Log.message("2. Right click on the column header.");

			//Verification: To verify if all the expected options are available in the context menu
			//--------------------------------------------------------------------------------------
			String menu = dataPool.get("Menu");
			String allMenuItems[] = menu.split(",");
			int count=0;

			for(count = 0; count<allMenuItems.length; count++)
				if(!homePage.listView.itemExistsInContextMenu(allMenuItems[count].trim()))
					break;

			if(count == allMenuItems.length)
				Log.pass("Test Case Passed. All the expected options were available in the context menu.");
			else
				Log.fail("Test Case Failed. The expected context menu " + allMenuItems[count] + " was not found.", driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		}

	} //End SprintTest15_1_2

	/**
	 * 15.1.3 :  Add 4 or more columns  - Try this from different views  
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint15", "Columns", "Smoke"}, 
			description = "Add 4 or more columns  - Try this from different views")
	public void SprintTest15_1_3(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewName"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2: Insert more than 4 columns
			//-----------------------------------
			String columns[] = dataPool.get("Columns").split(",");
			int count = 0;

			for(count = 0; count < columns.length; count++) 
				if(!homePage.listView.insertColumn(columns[count]))
					break;

			Log.message("2. Insert more than 4 columns.");

			//Verification: To verify if all the inserted columns are displayed as expected
			//------------------------------------------------------------------------------
			if(count == columns.length)
				Log.pass("Test Case Passed. All the columns were inserted and displayed as expected.");
			else
				Log.fail("Test Case Failed. The Column " + columns[count] + " was not inserted or displayed.", driver);

			for(count = 0; count < columns.length; count++) 
				homePage.listView.removeColumn(columns[count]);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		}

	} //End SprintTest15_1_3

	/**
	 * 15.2.1 : Added column setting should persist after navigating to other views by allowing column settings for user.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint15", "Columns"}, 
			description = "Added column setting should persist after navigating to other views by allowing column settings for user.")
	public void SprintTest15_2_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewName"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2: Insert the required columns
			//------------------------------------
			String column = dataPool.get("Columns");
			String columns[] = column.split(",");
			int count = 0;

			for(count = 0; count < columns.length; count++)
				if(!homePage.listView.insertColumn(columns[count]))
					break;

			if(count != columns.length)
				throw new Exception("The given column " + columns[count] + " is not inserted.");

			Log.message("2. Columns (" + column + ") are inserted.");

			//Step-3: Navigate to a different view
			//--------------------------------------
			homePage.taskPanel.clickItem("Home");
			Utils.fluentWait(driver);

			Log.message("3. Navigate to a Home view.");

			//Step-4: Navigate back to the specified view
			//--------------------------------------------
			viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewName"), "");
			Utils.fluentWait(driver);

			Log.message("4. Navigated back to the (" + viewToNavigate + ") view.");

			//Verification: To verify if the Columns are persistent after navigating away and back to the view in which it was inserted.
			//---------------------------------------------------------------------------------------------------------------------------
			for(count = 0; count < columns.length; count++) {
				if(!homePage.listView.isColumnExists(columns[count]))
					break;
				homePage.listView.removeColumn(columns[count]);
			}

			if(count == columns.length) 
				Log.pass("Test Case Passed. All the inserted columns exists even after navigating away and back to the view.");
			else
				Log.fail("Test Case Failed. The Column " + columns[count] + " was not found when navigating away and back to the view.", driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		}

	} //End SprintTest15_2_1

	/**
	 * 15.2.2 : Removed column setting should persist after navigating to other views by allowing column settings for user.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint15", "Columns"}, 
			description = "Removed column setting should persist after navigating to other views by allowing column settings for user.")
	public void SprintTest15_2_2(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewName"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2: Remove the required columns
			//------------------------------------
			String column = dataPool.get("Columns");
			String columns[] = column.split(",");
			int count = 0;

			for(count = 0; count < columns.length; count++) {
				if(!homePage.listView.removeColumn(columns[count]))
					break;
			}

			if(count != columns.length) 
				throw new Exception("The column " + columns[count] + " were not removed.");

			Log.message("2. Columns (" + column + ") are removed.");

			//Step-3: Navigate to a different view
			//--------------------------------------
			homePage.taskPanel.clickItem("Home");
			Utils.fluentWait(driver);

			Log.message("3. Navigate to a Home view.");

			//Step-4: Navigate back to the specified view
			//--------------------------------------------
			viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewName"), "");
			Utils.fluentWait(driver);

			Log.message("4. Navigated back to the (" + viewToNavigate + ") view.");

			//Verification: To verify if the Columns are persistent after navigating away and back to the view in which it was inserted.
			//---------------------------------------------------------------------------------------------------------------------------
			for(count = 0; count < columns.length; count++) {
				if(homePage.listView.isColumnExists(columns[count]))
					break;
				homePage.listView.insertColumn(columns[count]);
			}

			if(count == columns.length) 
				Log.pass("Test Case Passed. All the removed columns did not exist even after navigating away and back to the view.");
			else
				Log.fail("Test Case Failed. The Column " + columns[count] + " was found when navigating away and back to the view.", driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		}

	} //End SprintTest15_2_3

	/**
	 * 15.2.3 : Column settings should be saved after logging out - Insert Columns
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint15", "Columns", "Script"}, 
			description = "Column settings should be saved after logging out - Insert Columns")
	public void SprintTest15_2_3(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewName"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2: Insert the required columns
			//------------------------------------
			String column = dataPool.get("Columns");
			String columns[] = column.split(",");
			int count = 0;

			for(count = 0; count < columns.length; count++)
				if(!homePage.listView.insertColumn(columns[count]))
					break;

			if(count != columns.length)
				throw new Exception("The given column " + columns[count] + " is not inserted.");

			Log.message("2. Columns (" + column + ") are inserted.");

			//Step-3: Logout and log in as the same user
			//------------------------------------------
			Utility.logoutFromWebAccess(driver); //Logs out from web access
			Utils.fluentWait(driver);

			LoginPage loginPage = new LoginPage(driver);
			homePage = loginPage.loginToWebApplication(userName, password, testVault);
			Utils.fluentWait(driver);

			Log.message("3. Logged out and loggged in as the same user.");

			//Step-4: Navigate back to the specified view
			//--------------------------------------------
			viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewName"), "");
			Utils.fluentWait(driver);

			Log.message("4. Navigated to '" + viewToNavigate + "' view."); 

			//Verification: To verify if the Columns are persistent after mnavigating away and back to the view in which it was inserted.
			//---------------------------------------------------------------------------------------------------------------------------
			for(count = 0; count < columns.length; count++) {
				if(!homePage.listView.isColumnExists(columns[count]))
					break;
				homePage.listView.removeColumn(columns[count]);
			}

			if(count == columns.length) 
				Log.pass("Test Case Passed. All the inserted columns exists even after logging out and back into the vault.");
			else
				Log.fail("Test Case Failed. The Column " + columns[count] + " was not found when logging out and back into the vault.", driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		}

	} //End SprintTest15_2_3

	/**
	 * 15.2.4 : Column settings should be saved after logging out - Remove Columns
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint15", "Columns", "Script"}, 
			description = "Column settings should be saved after logging out - Remove Columns")
	public void SprintTest15_2_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewName"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2: Remove the columns
			//--------------------------
			String column = dataPool.get("Columns");
			String columns[] = column.split(",");
			int count = 0;

			for(count = 0; count < columns.length; count++) {
				if(!homePage.listView.removeColumn(columns[count]))
					break;
			}

			if(count != columns.length) 
				throw new Exception("The column " + columns[count] + " were not removed.");

			Log.message("2. Columns (" + column + ") are removed.");

			//Step-3: Logout and log in as the same user
			//------------------------------------------
			Utility.logoutFromWebAccess(driver); //Logs out from web access
			Utils.fluentWait(driver);

			LoginPage loginPage = new LoginPage(driver);
			homePage = loginPage.loginToWebApplication(userName, password, testVault);
			Utils.fluentWait(driver);

			Log.message("3. Logged out and loggged in as the same user.");

			//Step-4: Navigate back to the specified view
			//--------------------------------------------
			viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewName"), "");
			Utils.fluentWait(driver);

			Log.message("4. Navigated to '" + viewToNavigate + "' view."); 

			//Verification: To verify if the Columns are persistent after mnavigating away and back to the view in which it was inserted.
			//---------------------------------------------------------------------------------------------------------------------------
			for(count = 0; count < columns.length; count++) {
				if(homePage.listView.isColumnExists(columns[count]))
					break;
				homePage.listView.insertColumn(columns[count]);
			}

			if(count == columns.length) 
				Log.pass("Test Case Passed. All the removed columns did not exist even after navigating away and back to the view.");
			else
				Log.fail("Test Case Failed. The Column " + columns[count] + " was found when navigating away and back to the view.", driver);

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
	 * 15.2.5 : Disable Column Settings in configuration page and check in default page - Insert Column
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint15", "Columns", "Configuration", "Script"}, 
			description = "Disable Column Settings in configuration page and check in default page - Insert Column")
	public void SprintTest15_2_5(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1: Login to Configuration page
			//--------------------------------
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			Log.message("1. Logged in to Configuiration");

			//Step-2: Disable column setting for user.
			//----------------------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);
			Utils.fluentWait(driver);
			configurationPage.configurationPanel.setVaultCommands("Save column settings","Hide");
			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");
			configurationPage.logOut();

			Log.message("2. Disable column setting for user.");

			//Step-3: Login to default page
			//--------------------------------
			driver.get(loginURL);
			LoginPage loginPage = new LoginPage(driver);
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);

			//Step-3 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewName"), "");
			Utils.fluentWait(driver);

			Log.message("3. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2: Insert the required columns
			//------------------------------------
			String column = dataPool.get("Columns");
			String columns[] = column.split(",");
			int count = 0;

			for(count = 0; count < columns.length; count++)
				if(!homePage.listView.insertColumn(columns[count]))
					break;

			if(count != columns.length)
				throw new Exception("The given column " + columns[count] + " is not inserted.");

			Log.message("4. Columns (" + column + ") are inserted.");

			//Step-3: Navigate to a different view
			//--------------------------------------
			homePage.taskPanel.clickItem("Home");
			Utils.fluentWait(driver);

			Log.message("5. Navigate to a Home view.");

			//Step-4: Navigate back to the specified view
			//--------------------------------------------
			viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewName"), "");
			Utils.fluentWait(driver);

			Log.message("6. Navigated back to the (" + viewToNavigate + ") view.");

			//Verification: To verify if the Columns are persistent after mnavigating away and back to the view in which it was inserted.
			//---------------------------------------------------------------------------------------------------------------------------
			for(count = 0; count < columns.length; count++) {
				if(homePage.listView.isColumnExists(columns[count])) {
					homePage.listView.removeColumn(columns[count]);
					break;
				}

			}

			if(count == columns.length) 
				Log.pass("Test Case Passed. All the inserted columns did not exist after navigating away and back to the view.");
			else
				Log.fail("Test Case Failed. The Column " + columns[count] + " was found when navigating away and back to the view.", driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			if(driver != null)
			{
				try
				{
					Utility.logOut(driver);
					ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);
					configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + Caption.ConfigSettings.Config_Controls.Value); 
					configurationPage.configurationPanel.setVaultCommands("Save column settings", "Show");
					configurationPage.configurationPanel.saveSettings();
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest15_2_5

	/**
	 * 15.2.6 : 'Disable Column Settings for the users and check it is saved after navigating to other views
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint15", "Columns", "Script"}, 
			description = "Disable Column Settings for the users and check it is saved after logging out")
	public void SprintTest15_2_6(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1: Login to Configuration page
			//--------------------------------
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			Log.message("1. Logged in to Configuiration");

			//Step-2: Disable column setting for user.
			//----------------------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);
			Utils.fluentWait(driver);
			configurationPage.configurationPanel.setVaultCommands("Save column settings","Hide");
			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");
			configurationPage.logOut();

			Log.message("2. Disable column setting for user.");

			//Step-1: Navigate to the specified view
			//---------------------------------------
			driver.get(loginURL);
			LoginPage loginPage = new LoginPage(driver);
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);
			homePage.taskPanel.clickItem("Home");
			Utils.fluentWait(driver);

			homePage.listView.navigateThroughView(dataPool.get("View"));
			Utils.fluentWait(driver);

			Log.message("3. Navigate to the specified view.");

			//Step-2: Insert the required columns
			//------------------------------------
			String column = dataPool.get("Columns");
			String columns[] = column.split(",");
			int count = 0;
			for(count = 0; count < columns.length; count++) {
				if(!homePage.listView.insertColumn(columns[count]))
					break;
			}

			if(count != columns.length) 
				throw new SkipException("The given column " + columns[count] + " was not inserted.");

			Log.message("4. Insert the required columns");

			//Step-3: Logout and login back to the vault
			//-------------------------------------------
			homePage.taskPanel.clickItem("Log Out");

			Utils.fluentWait(driver);
			loginPage = new LoginPage(driver);

			loginPage.loginToWebApplication(userName, password, testVault);
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);

			Log.message("5. Logout and login back to the vault.");

			//Step-4: Navigate back to the specified view
			//--------------------------------------------
			homePage.listView.navigateThroughView(dataPool.get("View"));
			Utils.fluentWait(driver);

			Log.message("6. Navigate back to the specified view.");

			//Verification: To verify if the Columns are persistent after mnavigating away and back to the view in which it was inserted.
			//---------------------------------------------------------------------------------------------------------------------------
			for(count = 0; count < columns.length; count++) {
				if(homePage.listView.isColumnExists(columns[count])) {
					homePage.listView.removeColumn(columns[count]);
					break;
				}

			}

			if(count == columns.length) 
				Log.pass("Test Case Passed. All the inserted columns did not exist after navigating away and back to the view.");
			else
				Log.fail("Test Case Failed. The Column " + columns[count] + " was found when navigating away and back to the view.", driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (driver != null)
			{
				try
				{
					Utility.logoutFromWebAccess(driver);
					driver.get(configURL);
					LoginPage loginPage = new LoginPage(driver);
					ConfigurationPage configurationPage = loginPage.loginToConfigurationUI(userName, password);
					configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);
					Utils.fluentWait(driver);
					Utils.fluentWait(driver);
					configurationPage.configurationPanel.setVaultCommands("Save column settings","Show");
					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest15_2_6

	/**
	 * 15.2.7 : Disable Column Settings for the users and check it is saved after refreshing the page
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint15", "Columns", "Script"}, 
			description = "Disable Column Settings for the users and check it is saved after refreshing the page")
	public void SprintTest15_2_7(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1: Login to Configuration page
			//--------------------------------
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			Log.message("1. Logged in to Configuiration");

			//Step-2: Disable column setting for user.
			//----------------------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);
			Utils.fluentWait(driver);
			Utils.fluentWait(driver);
			configurationPage.configurationPanel.setVaultCommands("Save column settings","Show");
			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");
			configurationPage.logOut();

			Log.message("2. Disable column setting for user.");

			//Step-1: Navigate to the specified view
			//---------------------------------------
			driver.get(loginURL);
			LoginPage loginPage = new LoginPage(driver);
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);

			//Step-3: Navigate to the specified view
			//---------------------------------------
			homePage.taskPanel.clickItem("Home");
			Utils.fluentWait(driver);

			homePage.listView.navigateThroughView(dataPool.get("View"));
			Utils.fluentWait(driver);

			Log.message("3. Navigate to the specified view.");

			//Step-4: Insert the required columns
			//------------------------------------
			String column = dataPool.get("Columns");
			String columns[] = column.split(",");
			int count = 0;

			for(count = 0; count < columns.length; count++) {
				if(!homePage.listView.insertColumn(columns[count]))
					break;
			}

			if(count != columns.length) 
				throw new SkipException("The given column " + columns[count] + " was not inserted.");

			Log.message("4. Insert the required columns");

			//Step-5: Refresh the view
			//-------------------------
			homePage.listView.clickRefresh();

			Log.message("5. Refresh the view.");

			//Verification: To verify if the Columns are persistent after mnavigating away and back to the view in which it was inserted.
			//---------------------------------------------------------------------------------------------------------------------------
			for(count = 0; count < columns.length; count++) {
				if(homePage.listView.isColumnExists(columns[count])) {
					homePage.listView.removeColumn(columns[count]);
					break;
				}

			}

			if(count == columns.length) 
				Log.pass("Test Case Passed. All the inserted columns did not exist after refreshing the view.");
			else
				Log.fail("Test Case Failed. The Column " + columns[count] + " was found when the view was refreshed.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			if (driver != null)
			{
				try
				{
					Utility.logoutFromWebAccess(driver);
					driver.get(configURL);
					LoginPage loginPage = new LoginPage(driver);
					ConfigurationPage configurationPage = loginPage.loginToConfigurationUI(userName, password);
					configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);
					Utils.fluentWait(driver);
					configurationPage.configurationPanel.setVaultCommands("Save column settings","Show");
					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest15_2_7

	/**
	 * 15.2.8 : Disable Column Settings for the users and check it is saved after navigating to other views
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint15", "Columns", "Script"}, 
			description = "Disable Column Settings for the users and check it is saved after navigating to other views")
	public void SprintTest15_2_8(HashMap<String,String> dataValues, String driverType) throws Exception {
		driver = null; 
		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Step-1: Login to Configuration page
			//--------------------------------
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			Log.message("1. Logged in to Configuiration");

			//Step-2: Disable column setting for user.
			//----------------------------------------
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);
			Utils.fluentWait(driver);
			configurationPage.configurationPanel.setVaultCommands("Save column settings","Hide");
			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Configuration settings are not saved.");
			configurationPage.logOut();

			Log.message("2. Disable column setting for user.");

			//Step-1: Navigate to the specified view
			//---------------------------------------
			driver.get(loginURL);
			LoginPage loginPage = new LoginPage(driver);
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault);

			homePage.taskPanel.clickItem("Home");
			Utils.fluentWait(driver);

			homePage.listView.navigateThroughView(dataPool.get("View"));
			Utils.fluentWait(driver);

			Log.message("3. Navigate to the specified view.");

			//Step-4: remove the required columns
			//------------------------------------
			String column = dataPool.get("Columns");
			String columns[] = column.split(",");

			int count = 0;
			for(count = 0; count < columns.length; count++) {
				if(!homePage.listView.removeColumn(columns[count]))
					break;
			}

			if(count != columns.length) 
				throw new SkipException("The given column " + columns[count] + " was not removed.");

			Log.message("4. remove the required columns");

			//Step-5: Navigate to a different view
			//--------------------------------------
			homePage.taskPanel.clickItem("Home");
			Utils.fluentWait(driver);

			Log.message("5. Navigate to a different view.");

			//Step-6: Navigate back to the specified view
			//--------------------------------------------
			homePage.listView.navigateThroughView(dataPool.get("View"));
			Utils.fluentWait(driver);

			Log.message("6. Navigate back to the specified view.");

			//Verification: To verify if the Columns are persistent after mnavigating away and back to the view in which it was inserted.
			//---------------------------------------------------------------------------------------------------------------------------
			for(count = 0; count < columns.length; count++) {
				if(!homePage.listView.isColumnExists(columns[count]))
					break;
			}

			if(count == columns.length) 
				Log.pass("Test Case Passed. All the removed columns exist even after navigating away and back to the view.");
			else
				Log.fail("Test Case Failed. The Column " + columns[count] + " was not found when navigating away and back to the view.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			if(driver != null)
			{
				try
				{
					Utility.logoutFromWebAccess(driver);
					driver.get(configURL);
					LoginPage loginPage = new LoginPage(driver);
					ConfigurationPage configurationPage = loginPage.loginToConfigurationUI(userName, password);
					configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);
					Utils.fluentWait(driver);
					configurationPage.configurationPanel.setVaultCommands("Save column settings","Show");
					if (!configurationPage.configurationPanel.saveSettings())
						throw new Exception("Configuration settings are not saved.");
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest15_2_8


	/**
	 * 38.1.1A : Insert Columns in Home View
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint38", "Columns", "Smoke"}, 
			description = "Insert Columns in Home View")
	public void SprintTest38_1_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to Home view
			//-------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);

			Log.message("1. Navigated to '" + Caption.MenuItems.Home.Value + "' view.");

			//Step-2 : Insert Column in Home view
			//------------------------------------
			if (homePage.listView.isColumnExists(dataPool.get("ColumnName")))
				throw new SkipException("Column (" + dataPool.get("ColumnName") + ") already exists in the list.");

			homePage.listView.insertColumn(dataPool.get("ColumnName"));

			Log.message("2. Column header is right clicked and column is selected from insert column menu.");

			//Verification : To Verify if column is inserted in Home view
			//-----------------------------------------------------------			
			if (homePage.listView.isColumnExists(dataPool.get("ColumnName")))
				Log.pass("Test case Passed. Column (" + dataPool.get("ColumnName") + ") is inserted in Home view successfully.");
			else
				Log.fail("Test case Failed. Column (" + dataPool.get("ColumnName") + ") does not exists in Home view.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest38_1_1A

	/**
	 * 38.1.1B : Remove Columns in Home View using Remove column
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint38", "Columns", "Smoke"}, 
			description = "Remove Columns in Home View using Remove column", dependsOnMethods={"SprintTest38_1_1A"})
	public void SprintTest38_1_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to Home view
			//-------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);

			Log.message("1. Navigated to '" + Caption.MenuItems.Home.Value + "' view.");

			//Step-2 : Right click on the column and select remove column from context menu
			//-----------------------------------
			if (!homePage.listView.isColumnExists(dataPool.get("ColumnName"))) 
				throw new SkipException("Column (" + dataPool.get("ColumnName") + ") does not exists in the list.");

			homePage.listView.removeColumn(dataPool.get("ColumnName"));

			Log.message("2. Column is right clicked and 'Remove this Column' is selected from the menu.");

			//Verification : To Verify if column is inserted in Home view
			//-----------------------------------------------------------			
			if (!homePage.listView.isColumnExists(dataPool.get("ColumnName")))
				Log.pass("Test case Passed. Column (" + dataPool.get("ColumnName") + ") is removed from Home view successfully.");
			else
				Log.fail("Test case Passed. Column (" + dataPool.get("ColumnName") + ") does exists in Home view.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest38_1_1B

	/**
	 * 38.1.1C : Remove column by un-checking the inserted column in Home view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug","Sprint38"}, 
			description = "Remove column by un-checking the inserted column in Home view"/*, dependsOnMethods={"SprintTest38_1_1A"}*/)
	public void SprintTest38_1_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to Home view
			//-------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);

			Log.message("1. Navigated to '" + Caption.MenuItems.Home.Value + "' view.");

			//Step-2 : Insert Column in Home view
			//-----------------------------------
			if (!homePage.listView.isColumnExists(dataPool.get("ColumnName")))
				throw new SkipException("Column (" + dataPool.get("ColumnName") + ") does not exists in the list.");

			homePage.listView.removeColumnByUnCheking(dataPool.get("ColumnName"));

			Log.message("2. Column is right clicked and 'Remove this Column' is selected from the menu.");

			//Verification : To Verify if column is inserted in Home view
			//-----------------------------------------------------------			
			if (!homePage.listView.isColumnExists(dataPool.get("ColumnName")))
				Log.pass("Test case Passed. Column (" + dataPool.get("ColumnName") + ") is removed from Home view successfully.");
			else
				Log.fail("Test case Failed. Column (" + dataPool.get("ColumnName") + ") exists in Home view.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest38_1_1C

	/**
	 * 38.1.4 : Remove a column from the any view by selecting 'Remove this column'
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint38", "Columns", "Smoke"}, 
			description = "Remove a column from the any view by selecting 'Remove this column'")
	public void SprintTest38_1_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Remove Column in Home view
			//-----------------------------------
			if (!homePage.listView.isColumnExists(dataPool.get("ColumnName")))
				throw new SkipException("Column (" + dataPool.get("ColumnName") + ") does not exists in the list.");

			homePage.listView.removeColumn(dataPool.get("ColumnName"));

			Log.message("2. Column is right clicked and 'Remove this Column' is selected from the menu.");

			//Verification : To Verify if column is inserted in Home view
			//-----------------------------------------------------------			
			if (!homePage.listView.isColumnExists(dataPool.get("ColumnName")))
				Log.pass("Test case Passed. Column (" + dataPool.get("ColumnName") + ") is removed from view successfully.");
			else
				Log.fail("Test case Failed. Column (" + dataPool.get("ColumnName") + ") exists in view.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest38_1_4

	/**
	 * 38.1.5 : Column should be visible in Thumbnails view and same as in Details view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint38", "Columns", "Smoke"}, 
			description = "Column should be visible in Thumbnails view and same as in Details view")
	public void SprintTest38_1_5(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-2 : Get the visible columns in the Details view
			//-----------------------------------------------------
			String visibleColnsDetails[] = homePage.listView.getVisibleColumns();

			Log.message("2. Visible columns in details view are obtained.");

			//Step-3 : Change Display mode to Thumbnails
			//------------------------------------------
			homePage.menuBar.clickSettingsMenuItems(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.Thumbnails.Value);

			if (homePage.listView.isDetailsDisplayMode())
				throw new Exception("Display mode is not converted to Thumbnails.");

			Log.message("3. : Display mode is changed to Thumbnails.");

			//Step-4 : Get Visible columns in the thumbnails view
			//---------------------------------------------------
			String visibleColnsThumbnails[] = homePage.listView.getVisibleColumns();

			Log.message("4. Visible columns in Thumbnails view are obtained.");

			//Verification : To Verify if column is inserted in Home view
			//-----------------------------------------------------------			
			if (Arrays.equals(visibleColnsDetails, visibleColnsThumbnails))
				Log.pass("Test case Passed. Columns in Details view are visible in thumbnails view.");
			else
				Log.fail("Test case Passed. Columns in Details view are not same as in thumbnails view.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			if (driver != null &&!homePage.equals(null)) //Changes display mode to Details mode
			{
				try
				{
					homePage.menuBar.clickSettingsMenuItems(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.Details.Value);
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}

			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest38_1_5


	/**
	 * 38.1.14A : Added column should persist after refreshing the view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint38"}, 
			description = "Added column should persist after refreshing the view")
	public void SprintTest38_1_14A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-2 : Insert Column in the view
			//------------------------------------
			if (homePage.listView.isColumnExists(dataPool.get("ColumnName")))
				throw new SkipException("Column (" + dataPool.get("ColumnName") + ") already exists in the list.");

			if (!homePage.listView.insertColumn(dataPool.get("ColumnName")))
				throw new Exception("Column (" + dataPool.get("ColumnName") + ") is not inserted.");

			Log.message("2. Column (" + dataPool.get("ColumnName") + ") is inserted.");

			//Step-3 : Refresh the view listing
			//---------------------------------
			homePage.listView.clickRefresh();
			Utils.fluentWait(driver);

			Log.message("3. List is refreshed by clicking icon in the column header.");

			//Verification : To Verify if column is inserted in Home view
			//-----------------------------------------------------------			
			if (homePage.listView.isColumnExists(dataPool.get("ColumnName")))
				Log.pass("Test case Passed. Inserted column (" + dataPool.get("ColumnName") + ") exists after refreshing view.");
			else
				Log.fail("Test case Failed. Inserted column (" + dataPool.get("ColumnName") + ") does not exists after refreshing view.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest38_1_14A

	/**
	 * 38.1.14B : Added column should persist after refreshing the browser
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Bug", "Sprint38"}, 
			description = "Added column should persist after refreshing the browser")
	public void SprintTest38_1_14B(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-2 : Insert Column in the view
			//------------------------------------
			if (homePage.listView.isColumnExists(dataPool.get("ColumnName")))
				throw new SkipException("Column (" + dataPool.get("ColumnName") + ") already exists in the list.");

			if (!homePage.listView.insertColumn(dataPool.get("ColumnName")))
				throw new Exception("Column (" + dataPool.get("ColumnName") + ") is not inserted.");

			Log.message("2. Column (" + dataPool.get("ColumnName") + ") is inserted.");

			//Step-3 : Refresh the browser
			//---------------------------------
			homePage = Utility.refreshDriver(driver);
			Utils.fluentWait(driver);

			Log.message("3. Browser is refreshed.");

			//Verification : To Verify if column is inserted in Home view
			//-----------------------------------------------------------			
			if (homePage.listView.isColumnExists(dataPool.get("ColumnName")))
				Log.pass("Test case Passed. Inserted column (" + dataPool.get("ColumnName") + ") exists after refreshing browser.");
			else
				Log.fail("Test case Failed. Inserted column (" + dataPool.get("ColumnName") + ") does not exists after refreshing browser.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest38_1_14B

	/**
	 * 38.1.19A : Column should able be remove after performing drag and drop operation
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint38"}, 
			description = "Column should able be remove after performing drag and drop operation")
	public void SprintTest38_1_19A(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-2 : Right Click on Name column
			//-----------------------------------
			String colnName = "Name";

			if (!homePage.listView.isColumnExists(colnName))
				throw new SkipException("Column (" + colnName + ") does not exists in the list.");

			homePage.listView.rightClickColumn(colnName);

			Log.message("2. Column (" + colnName + ") is right clicked.");

			//Verification : To Verify if "Remove this column" is in disabled state
			//---------------------------------------------------------------------			
			if (!homePage.listView.itemEnabledInContextMenu(Caption.Column.RemoveThisColumn.Value))
				Log.pass("Test case Passed. '" + colnName + "' column has 'Remove this column' in disabled state.");
			else
				Log.fail("Test case Failed. '" + colnName + "' column has 'Remove this column' not in disabled state.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest38_1_19A

	/**
	 * 38.1.19B : Clicking disabled 'Remove this column' should not remove the column
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint38"}, 
			description = "Clicking disabled 'Remove this column' should not remove the column.")
	public void SprintTest38_1_19B(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-2 : Select Remove this column for Name column
			//--------------------------------------------------
			String colnName = "Name";

			if (!homePage.listView.isColumnExists(colnName))
				throw new SkipException("Column (" + colnName + ") does not exists in the list.");

			Boolean isRemoved = homePage.listView.removeColumn(colnName);

			Log.message("2. Column (" + colnName + ") is selected with Remove this column.");

			//Verification : To Verify if column is remvoed after drag and drop operation
			//---------------------------------------------------------------------------			
			if (!isRemoved)
				Log.pass("Test case Passed. Clicking disabled 'Remove this column' in '" + colnName + "' column has not removed it.");
			else
				Log.fail("Test case Failed. Clicking disabled 'Remove this column' in '" + colnName + "' column has removed it.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest38_1_19B

	/**
	 * 38.1.20.1A : 'Remove this Column' cannot be performed on right clicking at empty column header
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint38"}, 
			description = "'Remove this Column' cannot be performed on right clicking at empty column header.")
	public void SprintTest38_1_20_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

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

			//Verification : To Verify if Remove this Column is in disabled state
			//---------------------------------------------------------------------------			
			if (!homePage.listView.itemEnabledInContextMenu(Caption.Column.RemoveThisColumn.Value))
				Log.pass("Test case Passed. 'Remove this column' is in disabled state.");
			else
				Log.fail("Test case Failed. 'Remove this column' is not in disabled state.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest38_1_20_1A

	/**
	 * 38.1.20.2A : 'Remove this Column' cannot be performed on right clicking at refresh icon
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint38"}, 
			description = "'Remove this Column' cannot be performed on right clicking at refresh icon.")
	public void SprintTest38_1_20_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

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

			//Verification : To Verify if Remove this Column is in disabled state
			//---------------------------------------------------------------------------			
			if (!homePage.listView.itemEnabledInContextMenu(Caption.Column.RemoveThisColumn.Value))
				Log.pass("Test case Passed. 'Remove this column' is in disabled state.");
			else
				Log.fail("Test case Failed. 'Remove this column' is not in disabled state.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest38_1_20_2A


} //End class Columns
package MFClient.Tests;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.Utils;
import genericLibrary.WebDriverUtils;

import org.openqa.selenium.WebDriver;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.*;
import org.testng.xml.XmlTest;

import MFClient.Pages.HomePage;
import MFClient.Pages.LoginPage;
import MFClient.Wrappers.Caption;
import MFClient.Wrappers.ListView;
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class AdvancedUseCases {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String configURL = null;
	public static String userName = null;
	public static String password = null;
	public static String testVault = null;
	public static WebDriver driver = null;
	public static String className = null;
	public static String productVersion = null;

	/**
	 * init : Before Class method to perform initial operations.
	 */
	@BeforeClass (alwaysRun=true)
	public void init() throws Exception {

		try {

			xlTestDataWorkBook = "AdvancedUseCases.xls";
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
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

		} //End try

		catch(Exception e) {
			if (e.getClass().toString().contains("NullPointerException")) 
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

			Log.endTestCase();

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
	 * 1.1.10A : Traditional folders: View
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint38"}, 
			description = "Traditional folders: View.")
	public void AdvancedUseCaseTest1_1_10A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = WebDriverUtils.getDriver();

		try {

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to Home View
			//------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.MenuItems.Home.Value, "");

			Log.message("1. Navigated to '" + Caption.MenuItems.Home.Value + "' view.");

			//Verification : To verify Traditional Folder is visible in Home View
			//-------------------------------------------------------------------
			if (homePage.listView.isItemExists(dataPool.get("TraditionalFolderName")))
				Log.pass("Test case Passed. Traditional Folder (" + dataPool.get("TraditionalFolderName") + ") is visible in Home view.");
			else
				Log.fail("Test case Failed. Traditional Folder (" + dataPool.get("TraditionalFolderName") + ") is not visible Home view.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End AdvancedUseCaseTest1_1_10A

	/**
	 * 1.1.10B : Traditional folders: View
	 */
	@Test(groups = {"Sprint38"}, description = "Traditional folders: View.")
	public void AdvancedUseCaseTest1_1_10B() throws Exception {

		driver = WebDriverUtils.getDriver();

		try {

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to Home View
			//------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.MenuItems.Home.Value, "");

			Log.message("1. Navigated to '" + Caption.MenuItems.Home.Value + "' view.");

			//Verification : To verify Traditional Folder is visible in Home View
			//-------------------------------------------------------------------
			if (homePage.taskPanel.isItemExists("Traditional Folder"))
				Log.pass("Test case Passed. Option to create new traditional folder does not exists in taskpanel.");
			else
				Log.fail("Test case Failed. Option to create new traditional folder exists in taskpanel.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End AdvancedUseCaseTest1_1_10B

	/**
	 * 1.1.10C : Traditional folders: Rename
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint38"}, 
			description = "Traditional folders: Rename.")
	public void AdvancedUseCaseTest1_1_10C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = WebDriverUtils.getDriver();

		try {

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to Home View
			//------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.MenuItems.Home.Value, "");

			Log.message("1. Navigated to '" + Caption.MenuItems.Home.Value + "' view.");

			//Step-2 : Right click on the Traditional folder
			//----------------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("TraditionalFolderName")))
				throw new Exception ("Traditional folder (" + dataPool.get("TraditionalFolderName") + ") is not right clicked.");

			Log.message("2. Traditional folder (" + dataPool.get("TraditionalFolderName") + ") is right clicked.");

			//Verification : To verify Traditional Folder is visible in Home View
			//-------------------------------------------------------------------
			if (homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.Rename.Value))
				Log.pass("Test case Passed. Rename in context menu is disabled for traditional folder.");
			else
				Log.fail("Test case Failed. Rename in context menu is enabled for traditional folder.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End AdvancedUseCaseTest1_1_10C

	/**
	 * 1.1.10D : Traditional folders: Delete
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint38"}, 
			description = "Traditional folders: Rename.")
	public void AdvancedUseCaseTest1_1_10D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = WebDriverUtils.getDriver();

		try {

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to Home View
			//------------------------------
			SearchPanel.searchOrNavigatetoView(driver, Caption.MenuItems.Home.Value, "");

			Log.message("1. Navigated to '" + Caption.MenuItems.Home.Value + "' view.");

			//Step-2 : Right click on the Traditional folder
			//----------------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("TraditionalFolderName")))
				throw new Exception ("Traditional folder (" + dataPool.get("TraditionalFolderName") + ") is not right clicked.");

			Log.message("2. Traditional folder (" + dataPool.get("TraditionalFolderName") + ") is right clicked.");

			//Verification : To verify Traditional Folder is visible in Home View
			//-------------------------------------------------------------------
			if (homePage.listView.itemEnabledInContextMenu(Caption.MenuItems.Delete.Value))
				Log.pass("Test case Passed. Delete in context menu is disabled for traditional folder.");
			else
				Log.fail("Test case Failed. Delete in context menu is enabled for traditional folder.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End AdvancedUseCaseTest1_1_10D

	/**
	 * 1.1.25 : Deleting objects with sub objects
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint38"}, 
			description = "Deleting objects with sub objects.")
	public void AdvancedUseCaseTest1_1_25(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = WebDriverUtils.getDriver();

		try {

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to Home View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Open Sub-Objects view of an object
			//---------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) 
				throw new SkipException("Invalid Test data. The given object " + dataPool.get("ObjectName") + " was not found in the view.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.ShowSubObjects.Value);
			Utils.fluentWait(driver);

			if (!ListView.isSubObjectsViewOpened(driver)) //Checks if History view is opened
				throw new Exception("Show Subobjects view is not opened.");

			List<String> subObjects = homePage.listView.getColumnValues(Caption.Column.ColumnName.Value);

			Log.message("2. Show Subobjects view of an object (" + dataPool.get("ObjectName") + ") is opened.");

			//Step-3 : Delete the Customer
			//----------------------------
			homePage.listView.clickBackToViewButton(); //Navigates back to the view

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Delete.Value);
			MFilesDialog mfilesDialog = new MFilesDialog(driver);

			if (!mfilesDialog.confirmDelete())
				throw new Exception("Deletion of owner object have not happened.");

			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not deleted.");

			Log.message("Step-3 : Object (" + dataPool.get("ObjectName") + ") is deleted.");

			//Verification : To verify if subobjects have deleted on destroying owner object
			//------------------------------------------------------------------------------
			homePage.searchPanel.search("",  "Search only: " + dataPool.get("SubObjectType") + "s"); //Searches the object
			String addlInfo = "";

			for (int i=0; i<subObjects.size(); i++)
				if (homePage.listView.isItemExists(subObjects.get(i).trim().toString())) 
					addlInfo = addlInfo + ";" + subObjects.get(i).trim();

			if (addlInfo.equals(""))
				Log.pass("Test case Passed. Deleting Object deleted Sub-objects successfully.");
			else
				Log.fail("Test case Failed. Deleting Object does not deleted Sub-objects.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End AdvancedUseCaseTest1_1_25

} //End class AdvancedUseCases

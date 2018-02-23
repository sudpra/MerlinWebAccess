package MFClient.Tests.Views;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.Utils;
import genericLibrary.WebDriverUtils;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
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
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class SelectAndFocusObjectsInListView {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String userName = null;
	public static String userFullName = null;
	public static String password = null;
	public static String testVault = null;
	public static String configURL = null;
	public static String driverType = null;
	public static String productVersion = null;
	public static WebDriver driver = null;
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
			userFullName = xmlParameters.getParameter("UserFullName");
			password = xmlParameters.getParameter("Password");
			configURL = xmlParameters.getParameter("ConfigurationURL");
			testVault = xmlParameters.getParameter("VaultName");
			driverType = xmlParameters.getParameter("driverType");
			className = this.getClass().getSimpleName().toString().trim();

			//driverManager = new TestMethodWebDriverManager();

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
	 * 4.2.1A : Focus of the selected view - Details view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint4", "Listview"}, 
			description = "Focus of the selected view - Details view")
	public void SprintTest4_2_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2 : Select the first object in the view
			//-------------------------------------------
			homePage.listView.clickItemByIndex(0);
			String itemName = homePage.listView.getItemNameByItemIndex(0);

			Log.message("2. Object '" + itemName + "' is selected.");

			//To Verify if focus is in the selected object
			//-------------------------------------------
			if (homePage.listView.isItemSelected(itemName))
				Log.pass("Test case Passed. Item is focussed after its selection.");
			else
				Log.fail("Test case Failed. Item is not focussed after its selection.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest4_2_1A

	/**
	 * 4.2.1B : Focus of the selected view - Thumbnails view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint4", "Listview"}, 
			description = "Focus of the selected view - Thumbnails view")
	public void SprintTest4_2_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2 : Change display mode to Thumbnails view
			//-----------------------------------------------
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Thumbnails"); //Changes display mode to Thumbnails view
			Utils.fluentWait(driver);

			if (!homePage.listView.isThumbnailsView()) //Checks if Thumbnails view is displayed or not
				throw new Exception ("Display mode is not changed to Thumbnails view");

			Log.message("2. Display mode is changed to Thumbnails view.");

			//Step-3 : Select the first object in the view
			//-------------------------------------------
			boolean flag = homePage.listView.clickThumbnailItemByIndex(0);

			Log.message("3. Object is selected.");

			//Verification : To Verify if selected object is focused
			//---------------------------------------------------------
			if (flag) 
				Log.pass("Test case Passed. Item is focussed after its selection.");
			else
				Log.fail("Test case Failed. Item is not focussed after its selection.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {

			try
			{
				HomePage homePage = new HomePage(driver);
				homePage.menuBar.ClickOperationsMenu("Display Mode>>Details"); //Changes display mode to Thumbnails view
			}
			catch(Exception e0)
			{
				Log.exception(e0, driver);
			}
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest4_2_1B

	/**
	 * 4.2.2 : Down Arrow - once
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint4", "Listview", "Smoke", "SKIP_KeyActions"}, 
			description = "Down Arrow - once")
	public void SprintTest4_2_2(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2 : Select the first item and press down arrow key
			//-------------------------------------------------------
			if (!homePage.listView.clickItemByIndex(0)) //Selects the first item
				throw new Exception("First item in the list is not selected.");

			Actions action = new Actions(driver);
			action.sendKeys(Keys.DOWN).build().perform(); //Press down arrow key
			Utils.fluentWait(driver);

			Log.message("2. First item is selected in the list and Down arrow key is pressed.");

			//Verification : To Verify pressing down arrow has moved to next item
			//-------------------------------------------------------------------
			String itemName = homePage.listView.getItemNameByItemIndex(1); //Gets the name of an next item

			//Verifies that if focused item is next to the previously selected item
			if (homePage.listView.getSelectedListViewItem().toUpperCase().equals(itemName.toUpperCase()))
				Log.pass("Test case Passed. Focus is moved to next item on cliking down arrow key.");
			else
				Log.fail("Test case Failed. Focus is not moved to next item on cliking down arrow key.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest4_2_2

	/**
	 * 4.2.3 : Down Arrow - Several
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint4", "Listview", "SKIP_KeyActions"}, 
			description = "Down Arrow - Several")
	public void SprintTest4_2_3(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2 : Select the first item and press down arrow key
			//-------------------------------------------------------
			int itemCount = homePage.listView.itemCount();

			if (itemCount <=0) 
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") does not contain any items.");

			if (!homePage.listView.clickItemByIndex(0)) 
				throw new Exception("First item in the list is not selected.");

			for (int i=0; i<itemCount-1; i++) {
				Actions action = new Actions(driver);
				action.sendKeys(Keys.DOWN).build().perform(); //Press down arrow key
				Utils.fluentWait(driver);
			}

			Log.message("2. First item in the list is selected and down arrow key is pressed till it moves to last item.");

			//Verification : To Verify if pressing down arrow key several time moved focus to last item
			//-----------------------------------------------------------------------------------------
			String itemName = homePage.listView.getItemNameByItemIndex(itemCount-1);

			//Verifies that focus of the current item is the last item of the list
			if (homePage.listView.getSelectedListViewItem().toUpperCase().equals(itemName.toUpperCase()))
				Log.pass("Test case Passed. Focus is moved to last item on cliking down arrow key till it reaches to last item.");
			else
				Log.fail("Test case Failed. Focus is not moved to last item on cliking down arrow key till it reaches to last item.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest4_2_3

	/**
	 * 4.2.4 : Down Arrow - Last Item
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint4", "Listview", "SKIP_KeyActions"}, 
			description = "Down Arrow - Last Item")
	public void SprintTest4_2_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2 : Select the last item and press down arrow key
			//-------------------------------------------------------
			int itemCount = homePage.listView.itemCount();

			if (itemCount <=0) 
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") does not contain any items.");

			if (!homePage.listView.clickItemByIndex(itemCount -1 )) 
				throw new Exception("First item in the list is not selected.");

			Actions action = new Actions(driver);
			action.sendKeys(Keys.DOWN).build().perform(); //Press down arrow key
			Utils.fluentWait(driver);

			Log.message("2. Last item in the list is selected and down arrow key is pressed.");

			//Verification : To Verify if pressing down arrow key from the last does not move focus
			//------------------------------------------------------------------------------------
			String itemName = homePage.listView.getItemNameByItemIndex(itemCount-1);

			//Verifies that focus of the current item is the last item of the list
			if (homePage.listView.getSelectedListViewItem().toUpperCase().equals(itemName.toUpperCase()))
				Log.pass("Test case Passed. Focus is not moved from last item on cliking down arrow key.");
			else
				Log.fail("Test case Failed. Focus is moved from last item on cliking down arrow key.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest4_2_4

	/**
	 * 4.2.5 : Up Arrow - once
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint4", "Listview", "SKIP_KeyActions"}, 
			description = "Up Arrow - once")
	public void SprintTest4_2_5(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2 : Select the last item and press up arrow key
			//-------------------------------------------------------
			int itemIdx = homePage.listView.itemCount() - 1;
			homePage.listView.clickItemByIndex(itemIdx); //Selects the last item
			Actions action = new Actions(driver);
			action.sendKeys(Keys.UP).build().perform(); //Press down arrow key
			Utils.fluentWait(driver);

			Log.message("2. Last item is selected in the list and Up arrow key is pressed.");

			//Verification : To Verify if pressing up arrow key moved focus to previous item
			//-------------------------------------------------------------------------------
			String itemName = homePage.listView.getItemNameByItemIndex(itemIdx - 1);

			//Verifies that current focused item is the previous item of the last item
			if (homePage.listView.getSelectedListViewItem().toUpperCase().equals(itemName.toUpperCase()))
				Log.pass("Test case Passed. Focus is moved to previous item on cliking Up arrow key.");
			else
				Log.fail("Test case Failed. Focus is not moved to previous item on cliking Up arrow key.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest4_2_5

	/**
	 * 4.2.6 : Up Arrow - Several
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint4", "Listview", "SKIP_KeyActions"}, 
			description = "Up Arrow - Several")
	public void SprintTest4_2_6(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2 : Select the first item and press down arrow key
			//-------------------------------------------------------
			int itemCount = homePage.listView.itemCount();

			if (itemCount <=0) 
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") does not contain any items.");

			if (!homePage.listView.clickItemByIndex(itemCount-1)) 
				throw new Exception("First item in the list is not selected.");

			for (int i=itemCount-1; i>=0; i--) {
				Actions action = new Actions(driver);
				action.sendKeys(Keys.UP).build().perform(); //Press Up arrow key
				Utils.fluentWait(driver);
			}

			Log.message("2. Last item in the list is selected and UP arrow key is pressed till it moves to first item.");

			//Verification : To Verify if pressing Up arrow key moved focus to first item
			//-------------------------------------------------------------------------------
			String itemName = homePage.listView.getItemNameByItemIndex(0);

			//Verifies that focus of the current item is the first item of the list
			if (homePage.listView.getSelectedListViewItem().toUpperCase().equals(itemName.toUpperCase()))
				Log.pass("Test case Passed. Focus is moved to first item on cliking up arrow key till it reaches to first item.");
			else
				Log.fail("Test case Failed. Focus is not moved to first item on cliking up arrow key till it reaches to first item.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest4_2_6

	/**
	 * 4.2.7 : Up Arrow - First Item
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint4", "Listview", "SKIP_KeyActions"}, 
			description = "Up Arrow - First Item")
	public void SprintTest4_2_7(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2 : Select the last item and press down arrow key
			//-------------------------------------------------------
			if (homePage.listView.itemCount() <=0) 
				throw new SkipException("Invalid test data. View (" + dataPool.get("NavigateToView") + ") does not contain any items.");

			if (!homePage.listView.clickItemByIndex(0)) 
				throw new Exception("First item in the list is not selected.");

			Actions action = new Actions(driver);
			action.sendKeys(Keys.UP).build().perform(); //Press down arrow key
			Utils.fluentWait(driver);

			Log.message("2. Last item in the list is selected and down arrow key is pressed.");

			//Verification : To Verify if pressing Up arrow key from the first item does not move focus
			//------------------------------------------------------------------------------------
			String itemName = homePage.listView.getItemNameByItemIndex(0);

			//Verifies that focus of the current item is the first item of the list
			if (homePage.listView.getSelectedListViewItem().toUpperCase().equals(itemName.toUpperCase()))
				Log.pass("Test case Passed. Focus is not moved from first item on cliking up arrow key.");
			else
				Log.fail("Test case Failed. Focus is moved from first item on cliking up arrow key.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest4_2_7
}

package MFClient.Tests.Views;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.Utils;
import genericLibrary.WebDriverUtils;

import org.openqa.selenium.By;
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
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class AddAndRemoveFavorites {

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
	 * 6.3.1 : 'Favorites' should be displayed in task area
	 */
	@Test(groups = {"Sprint6", "Favorites", "Taskpane"}, description = "'Favorites' should be displayed in task area")
	public void SprintTest6_3_1() throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Verification : To Verify Favorites exists in task panel
			//-------------------------------------------------------

			//Verifies Favorites exists in task panel
			if (homePage.taskPanel.isItemExists("Favorites"))
				Log.pass("Test case Passed. Favorites is visible in Task Pane.");
			else
				Log.fail("Test case Failed. Favorites is not visible in Task Pane.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest6_3_1

	/**
	 * 6.3.2A : 'One object was affected.' message should appear after adding object to favorites - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint6", "Favorites"}, 
			description = "'One object was affected.' message should appear after adding object to favorites - Operations menu")
	public void SprintTest6_3_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2 : Select the item and click Favorites from operations menu
			//-----------------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Select the item
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu("Add to Favorites"); //Select Add to Favorites option from operations menu

			Log.message("Step-2 : Object (" + dataPool.get("ObjectName") + ") is selected and Add to Favorites is selected from operations menu.");

			//Verification : To Verify Message dialog with One Object affected message appears
			//--------------------------------------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class //Instantiating MFilesDialog wrapper class

			//Verifies MFiles dialog appeared with message 'One Object was affected'
			if (mfilesDialog.getMessage().equalsIgnoreCase("ONE OBJECT WAS AFFECTED."))
				Log.pass("Test case Passed. 'One Object was affected' message has appeared after adding object to favorites.");
			else
				Log.fail("Test case Failed. 'One Object was affected' message does not appeared after adding object to favorites.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest6_3_2A

	/**
	 * 6.3.2B : Add to Favorites should add objects Favorites view - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, groups = { "Sprint6", "Favorites", "Smoke"}, dataProvider = "excelDataReader", 
			description = "Add to Favorites should add objects Favorites view - Operations menu")
	public void SprintTest6_3_2B(HashMap<String,String> dataValues, String driverType) throws Exception  {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2 : Select an object and select Add to Favorites in operations menu
			//--------------------------------------------------------------------------  		
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")) ) //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the list.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))//Selects the item
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu("Add to Favorites"); //Selects Add to Favorites from operations menu

			if (MFilesDialog.exists(driver)) { //Clicks Ok button in the message dialog if it exists
				MFilesDialog mfilesDialog = new MFilesDialog(driver);
				mfilesDialog.clickOkButton(); 
			}

			Log.message("Step-2 : Add to Favorites is selected to the object from operations menu");

			//Verification : To verify if object is listed in favorites view
			//---------------------------------------------------------------
			homePage.taskPanel.clickItem("Favorites"); //Clicks Favorites view 

			if (!homePage.menuBar.GetBreadCrumbItem().toUpperCase().contains("FAVORITES"))
				throw new Exception("Page is not navigated to Favorites view.");

			//Verifies that task pane has default items in it.
			if (homePage.listView.isItemExists(dataPool.get("ObjectName")))
				Log.pass("Test case Passed. Object is added to favorites successfully.");
			else
				Log.fail("Test case Failed. Object does not exists in Favorites view.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest6_3_2B

	/**
	 * 6.3.2C : '0 objects were affected.' message should appear after adding favorites object to favorites - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint6", "Favorites"}, 
			description = "'0 objects were affected.' message should appear after adding favorites object to favorites - Operations menu")
	public void SprintTest6_3_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2 : Select the item and click Favorites from operations menu
			//-----------------------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Select the item
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu("Add to Favorites"); //Select Add to Favorites option from operations menu

			Log.message("Step-2 : Object (" + dataPool.get("ObjectName") + ") is selected and Add to Favorites is selected from operations menu.");

			//Step-3 : Select the same item and click add to favorites
			//--------------------------------------------------------
			if (MFilesDialog.exists(driver)) {//Verifies and Clicks Ok button in MFiles dialog
				MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class 
				mfilesDialog.clickOkButton();				
			}

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Select the item
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.menuBar.ClickOperationsMenu("Add to Favorites"); //Select Add to Favorites option from operations menu

			Log.message("Step-3 : Object (" + dataPool.get("ObjectName") + ") is selected again and Add to Favorites is Clicked.");

			//Verification : To Verify Message dialog with 0 Objects were affected message appears
			//--------------------------------------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class //Instantiating MFilesDialog wrapper class

			//Verifies MFiles dialog appeared with message '0 Objects were affected'
			if (mfilesDialog.getMessage().equalsIgnoreCase("0 OBJECTS WERE AFFECTED."))
				Log.pass("Test case Passed. '0 Objects were affected' message has appeared after adding same object to favorites.");
			else
				Log.fail("Test case Failed. '0 Objects were affected' message does not appeared after same adding object to favorites.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest6_3_3

	/**
	 * 6.3.3A : 'One object was affected.' message should appear after adding object to favorites - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint6", "Favorites"}, 
			description = "'One object was affected.' message should appear after adding object to favorites - Context menu")
	public void SprintTest6_3_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2 : Select the item and click Favorites from operations menu
			//-----------------------------------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Select the item
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.listView.clickContextMenuItem("Add to Favorites"); //Add to Favorites is selected from Context menu

			Log.message("Step-2 : Object (" + dataPool.get("ObjectName") + ") is right clicked and Add to Favorites is selected from context menu.");

			//Verification : To Verify Message dialog with One Object affected message appears
			//--------------------------------------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class //Instantiating MFilesDialog wrapper class

			//Verifies MFiles dialog appeared with message 'One Object was affected'
			if (mfilesDialog.getMessage().equalsIgnoreCase("ONE OBJECT WAS AFFECTED."))
				Log.pass("Test case Passed. 'One Object was affected' message has appeared after adding object to favorites.");
			else
				Log.fail("Test case Failed. 'One Object was affected' message does not appeared after adding object to favorites.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest6_3_3A

	/**
	 * 6.3.3B : Add to Favorites should add objects Favorites view - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = { "Sprint6", "Favorites"},  
			description = "Add to Favorites should add objects Favorites view - Context menu")
	public void SprintTest6_3_3B(HashMap<String,String> dataValues, String driverType) throws Exception  {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2 : Select an object and select Add to Favorites in operations menu
			//--------------------------------------------------------------------------  		
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Select the item
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.listView.clickContextMenuItem("Add to Favorites"); //Add to Favorites is selected from Context menu

			if (MFilesDialog.exists(driver)) { //Clicks Ok button in the message dialog if it exists
				MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class
				mfilesDialog.clickOkButton(); 
			}

			Log.message("Step-2 : Object (" + dataPool.get("ObjectName") + ") is right clicked and Add to Favorites is selected from context menu.");

			//Verification : To verify if object is listed in favorites view
			//---------------------------------------------------------------
			homePage.taskPanel.clickItem("Favorites"); //Clicks Show SubObjects from 

			if (!homePage.menuBar.GetBreadCrumbItem().toUpperCase().contains("FAVORITES"))
				throw new Exception("Page is not navigated to Favorites view.");

			if (homePage.listView.isItemExists(dataPool.get("ObjectName"))) //Verifies that task pane has default items in it.
				Log.pass("Test case Passed. Object is added to favorites successfully.");
			else
				Log.fail("Test case Failed. Object does not exists in Favorites view.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest6_3_3B

	/**
	 * 6.3.3C : '0 objects were affected.' message should appear after adding favorites object to favorites - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint6", "Favorites"}, 
			description = "'0 objects were affected.' message should appear after adding favorites object to favorites - Context menu")
	public void SprintTest6_3_3C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view."); 

			//Step-2 : Select the item and click Favorites from operations menu
			//-----------------------------------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Select the item
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			homePage.listView.clickContextMenuItem("Add to Favorites"); //Add to Favorites is selected from Context menu

			Log.message("Step-2 : Object (" + dataPool.get("ObjectName") + ") is right clicked and Add to Favorites is selected from context menu.");

			//Step-3 : Select the same item and click add to favorites
			//--------------------------------------------------------
			if (MFilesDialog.exists(driver)) {//Verifies and Clicks Ok button in MFiles dialog
				MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class 
				mfilesDialog.clickOkButton();				
			}

			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Select the item
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not right clicked.");

			homePage.listView.clickContextMenuItem("Add to Favorites"); //Add to Favorites is selected from Context menu

			Log.message("Step-3 : Object (" + dataPool.get("ObjectName") + ") is selected again and Add to Favorites is Clicked from context menu.");

			//Verification : To Verify Message dialog with 0 Objects were affected message appears
			//--------------------------------------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class //Instantiating MFilesDialog wrapper class

			//Verifies MFiles dialog appeared with message '0 Objects were affected'
			if (mfilesDialog.getMessage().equalsIgnoreCase("0 OBJECTS WERE AFFECTED."))
				Log.pass("Test case Passed. '0 Objects were affected' message has appeared after adding same object to favorites.");
			else
				Log.fail("Test case Failed. '0 Objects were affected' message does not appeared after same adding object to favorites.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest6_3_3C

	/**
	 * 6.3.4A : Confirmation dialog should be prompted while removing object from favorites - Operations menu
	 */
	@Test(groups = { "Sprint6", "Favorites"}, description = "Confirmation dialog should be prompted while removing object from favorites - Operations menu")
	public void SprintTest6_3_4A() throws Exception  {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to Favorites view
			//-----------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Favorites.Value);

			if (!homePage.menuBar.GetBreadCrumbItem().toUpperCase().contains(Caption.MenuItems.Favorites.Value.toUpperCase()))
				throw new Exception("Favorites view is not opened.");

			if (homePage.listView.itemCount() <=0)
				throw new Exception("No Items found in Favorites view.");

			Log.message("Step-1 : Navigated to Favorites view.");

			//Step-2 : Select an object and select Remove From Favorites in operations menu
			//--------------------------------------------------------------------------  		
			if (!homePage.listView.clickItemByIndex(0)) //Selects the first item
				throw new Exception("First item in the Favorites view is not selected.");

			homePage.menuBar.ClickOperationsMenu("Remove from Favorites"); //Selects Add to Favorites from operations menu

			Log.message("Step-2 : Remove from Favorites is selected to the object from operations menu");

			//Verification : To verify MFiles Confirmation dialog to remove from favorites appear
			//-----------------------------------------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class

			//Verifies confirmation dialog has 'Are you sure you want to remove the selected object from the favorites?' message appeared 
			if (mfilesDialog.getMessage().equalsIgnoreCase("Are you sure you want to remove the selected object from the favorites?"))
				Log.pass("Test case Passed. M-Files confirmation dialog is prompted to remove object from favorites.");
			else
				Log.fail("Test case Failed. M-Files confirmation dialog is prompted with different message. Refer screenshot.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest6_3_4A

	/**
	 * 6.3.4B : 'One object was affected.' message should appear after removing object from favorites - Operations menu
	 */
	@Test(groups = { "Sprint6", "Favorites"}, description = "'One object was affected.' message should appear after removing object from favorites - Operations menu")
	public void SprintTest6_3_4B() throws Exception  {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to Favorites view
			//-----------------------------------
			homePage.taskPanel.clickItem("Favorites");

			if (!homePage.menuBar.GetBreadCrumbItem().toUpperCase().contains("FAVORITES"))
				throw new Exception("Favorites view is not opened.");

			if (homePage.listView.itemCount() <=0)
				throw new Exception("No Items found in Favorites view.");

			Log.message("Step-1 : Navigated to Favorites view.");

			//Step-2 : Select an object and select Remove From Favorites in operations menu
			//--------------------------------------------------------------------------  		
			if (!homePage.listView.clickItemByIndex(0)) //Selects the first item
				throw new Exception("First item in the Favorites view is not selected.");

			homePage.menuBar.ClickOperationsMenu("Remove from Favorites"); //Selects Add to Favorites from operations menu

			Log.message("Step-2 : Remove from Favorites is selected to the object from operations menu");

			//Verification : To verify if 'One object was affected.' message dialog appeared
			//-------------------------------------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.clickOkButton(); //Selects Yes in the confirmation dialog
			Utils.fluentWait(driver);
			mfilesDialog = new MFilesDialog(driver);

			//Verifies 'One object was affected.' message dialog appeared
			/*if (mfilesDialog.getMessage().equalsIgnoreCase("One object was affected."))*/
			if(driver.findElement(By.cssSelector("div[class='shortErrorArea']")).getText().contains("One object was affected."))
				Log.pass("Test case Passed. 'One object was affected.' dialog is prompted after removing object from favorites.");
			else
				Log.fail("Test case Failed. 'One object was affected.' dialog is not prompted after removing object from favorites. Refer screenshot.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest6_3_4B

	/**
	 * 6.3.4C : Remove from favorites should remove object from Favorites view - Operations menu
	 */
	@Test(groups = { "Sprint6", "Favorites"}, description = "Remove from favorites should remove object from Favorites view - Operations menu")
	public void SprintTest6_3_4C() throws Exception  {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to Favorites view
			//-----------------------------------
			homePage.taskPanel.clickItem("Favorites");

			if (!homePage.menuBar.GetBreadCrumbItem().toUpperCase().contains("FAVORITES"))
				throw new Exception("Favorites view is not opened.");

			if (homePage.listView.itemCount() <=0)
				throw new Exception("No Items found in Favorites view.");

			Log.message("Step-1 : Navigated to Favorites view.");

			//Step-2 : Select an object and select Remove From Favorites in operations menu
			//--------------------------------------------------------------------------  		
			String itemName = homePage.listView.getItemNameByItemIndex(0); //Gets the name of the first item

			if (!homePage.listView.clickItem(itemName)) //Selects the first item
				throw new Exception("Item (" + itemName + ") does not exists in the list.");

			homePage.menuBar.ClickOperationsMenu("Remove from Favorites"); //Selects Add to Favorites from operations menu

			Log.message("Step-2 : Remove from Favorites is selected to the object from operations menu");

			//Verification : To verify if object is listed in favorites view
			//---------------------------------------------------------------
			int snooze=0;

			while (MFilesDialog.exists(driver) && snooze < 2) {
				MFilesDialog mfilesDialog = new MFilesDialog(driver);
				mfilesDialog.clickOkButton(); //Clicks Yes button to remove from favorites
				Utils.fluentWait(driver);		
				snooze++;
			}

			driver.navigate().refresh(); //Refresh the page
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem("Favorites"); //Clicks Show SubObjects from 

			//Verifies if object is removed from Favorites view
			if (homePage.listView.isItemExists(itemName)) {
				Log.fail("Test case Failed. Object is not removed from Favorites view.", driver);
				return;
			}

			homePage.searchPanel.search(itemName, ""); // Search for the object

			if (homePage.listView.isItemExists(itemName)) //Verifies object exists in the search view
				Log.pass("Test case Passed. Object is removed from favorites successfully.");
			else
				Log.fail("Test case Failed. Object does not exists in search after removing it from favorites.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest6_3_4C

	/**
	 * 6.3.5A : Confirmation dialog should be prompted while removing object from favorites - Operations menu
	 */
	@Test(groups = { "Sprint6", "Favorites"}, description = "Confirmation dialog should be prompted while removing object from favorites - Context menu")
	public void SprintTest6_3_5A() throws Exception  {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to Favorites view
			//-----------------------------------
			homePage.taskPanel.clickItem("Favorites");

			if (!homePage.menuBar.GetBreadCrumbItem().toUpperCase().contains("FAVORITES"))
				throw new Exception("Favorites view is not opened.");

			if (homePage.listView.itemCount() <=0)
				throw new Exception("No Items found in Favorites view.");

			Log.message("Step-1 : Navigated to Favorites view.");

			//Step-2 : Select an object and select Remove Favorites in Context menu
			//--------------------------------------------------------------------------  		
			if (!homePage.listView.rightClickItemByIndex(0)) //Selects the first item
				throw new Exception("Object is not right clicked");

			homePage.listView.clickContextMenuItem("Remove from Favorites"); //Selects Add to Favorites from operations menu

			Log.message("Step-2 : Remove from Favorites is selected to the object from context menu");

			//Verification : To Verify if confirmation dialog has appeared
			//---------------------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver);

			//Verifies if 'Are you sure you want to remove the selected object from the favorites?' message has appeared 
			if (mfilesDialog.getMessage().equalsIgnoreCase("Are you sure you want to remove the selected object from the favorites?"))
				Log.pass("Test case Passed. M-Files confirmation dialog is prompted to remove object from favorites.");
			else
				Log.fail("Test case Failed. M-Files confirmation dialog is prompted with different message. Refer screenshot.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest6_3_5A

	/**
	 * 6.3.5B : 'One object was affected.' message should appear after removing object from favorites - Context menu
	 */
	@Test(groups = { "Sprint6", "Favorites"}, description = "'One object was affected.' message should appear after removing object from favorites - Context menu")
	public void SprintTest6_3_5B() throws Exception  {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to Favorites view
			//-----------------------------------
			homePage.taskPanel.clickItem("Favorites");

			if (!homePage.menuBar.GetBreadCrumbItem().toUpperCase().contains("FAVORITES"))
				throw new Exception("Favorites view is not opened.");

			if (homePage.listView.itemCount() <=0)
				throw new Exception("No Items found in Favorites view.");

			Log.message("Step-1 : Navigated to Favorites view.");

			//Step-2 : Select an object and select Remove Favorites in Context menu
			//--------------------------------------------------------------------------  		
			if (!homePage.listView.rightClickItemByIndex(0)) //Selects the first item
				throw new Exception("Object is not right clicked");

			homePage.listView.clickContextMenuItem("Remove from Favorites"); //Selects Remove Favorites from Context menu

			Log.message("Step-2 : Remove from Favorites is selected to the object from context menu");

			//Verification : To verify if 'One object was affected.' message is displayed 
			//---------------------------------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.clickOkButton(); //Selects Yes in the confirmation dialog
			Utils.fluentWait(driver);

			//Verifies 'One object was affected.' message got displayed in M-Files dialog
			/*	if (mfilesDialog.getMessage().equalsIgnoreCase("One object was affected."))*/
			if(driver.findElement(By.cssSelector("div[class='shortErrorArea']")).getText().contains("One object was affected."))
				Log.pass("Test case Passed. 'One object was affected.' dialog is prompted after removing object from favorites.");
			else
				Log.fail("Test case Failed. 'One object was affected.' dialog is prompted after removing object from favorites. Refer screenshot.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest6_3_5B

	/**
	 * 6.3.5C : Remove from favorites should remove object from Favorites view - Context menu
	 */
	@Test(groups = { "Sprint6", "Favorites"}, description = "Remove from favorites should remove object from Favorites view - Context menu")
	public void SprintTest6_3_5C() throws Exception  {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to Favorites view
			//-----------------------------------
			homePage.taskPanel.clickItem("Favorites");

			if (!homePage.menuBar.GetBreadCrumbItem().toUpperCase().contains("FAVORITES"))
				throw new Exception("Favorites view is not opened.");

			if (homePage.listView.itemCount() <=0)
				throw new SkipException("No Items found in Favorites view.");

			Log.message("Step-1 : Navigated to Favorites view.");

			//Step-2 : Select an object and select Remove Favorites in Context menu
			//--------------------------------------------------------------------------
			String itemName = homePage.listView.getItemNameByItemIndex(0); //Gets the name of the first item
			homePage.listView.rightClickItem(itemName); //Selects the first item
			homePage.listView.clickContextMenuItem("Remove from Favorites"); //Selects Remove from Favorites from Context menu

			Log.message("Step-2 : Remove from Favorites is selected to the object (" + itemName + ") from context menu");

			//Verification : To verify if object is listed in favorites view
			//---------------------------------------------------------------
			int snooze=0;

			while (MFilesDialog.exists(driver) && snooze < 2) {
				MFilesDialog mfilesDialog = new MFilesDialog(driver);
				mfilesDialog.clickOkButton(); //Clicks Yes button to remove from favorites
				Utils.fluentWait(driver);		
				snooze++;
			}

			driver.navigate().refresh(); //Refresh the page
			Utils.fluentWait(driver);

			homePage.taskPanel.clickItem("Favorites"); //Clicks Show SubObjects from 

			//Verifies object exists in the search view
			if (!homePage.listView.isItemExists(itemName))
				Log.pass("Test case Passed. Object is removed from favorites successfully.");
			else
				Log.fail("Test case Failed. Object is not removed from Favorites view.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest6_3_5C



	/**
	 * 7.1.2A : Delete option for an object in favorites view is disabled. - Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint7", "History"}, 
			description = "Delete option for an object in favorites view is disabled. - Context menu")
	public void SprintTest7_1_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Add the Object to favorites
			//-----------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) 
				throw new SkipException("Invalid Test Data. The specified Object " + dataPool.get("ObjectName") + " was not found in the vault.");

			homePage.listView.rightClickItem(dataPool.get("ObjectName")); //Right click the object
			Utils.fluentWait(driver);
			homePage.listView.clickContextMenuItem("Add to Favorites"); //Adds object to favorites
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class
			mfilesDialog.clickOkButton(); //Clicks Ok button
			Utils.fluentWait(driver);

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is added to favorites.");

			//Step-3: Navigate to 'Favorites' View
			//-------------------------------------
			homePage.taskPanel.clickItem("Favorites");

			Log.message("3. Navigated to 'Favorties' View");

			//Step-3: Right click on the object
			//----------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in favorites view
				throw new SkipException("Invalid Test Data. The specified Object " + dataPool.get("ObjectName") + " was not found in the favorites view.");

			homePage.listView.rightClickItem(dataPool.get("ObjectName")); //Right click the object

			Log.message("4. Object (" + dataPool.get("ObjectName") + ") is right clicked.");

			//Verification: To Verify if the 'Delete' context menu is in disabled state
			//--------------------------------------------------------------------------
			if(!homePage.listView.itemEnabledInContextMenu("Delete"))  //Checks if delete is disabled state.
				Log.pass("Test Case Passed. The Delete item in context menu is in disabled state.");
			else
				Log.fail("Test Case Failed. The Delete item in context menu is not in disabled state.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest7_1_2A

	/**
	 * 7.1.2B : Delete option for an object in favorites view is disabled. - Operations menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint7", "History"}, 
			description = "Delete option for an object in favorites view is disabled. - Operations menu")
	public void SprintTest7_1_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2: Add the Object to favorites
			//-----------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) 
				throw new SkipException("Invalid Test Data. The specified Object " + dataPool.get("ObjectName") + " was not found in the vault.");

			homePage.listView.rightClickItem(dataPool.get("ObjectName")); //Right click the object
			Utils.fluentWait(driver);
			homePage.listView.clickContextMenuItem("Add to Favorites"); //Adds object to favorites
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class
			mfilesDialog.clickOkButton(); //Clicks Ok button
			Utils.fluentWait(driver);

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is added to favorites.");

			//Step-3: Navigate to 'Favorites' View
			//-------------------------------------
			homePage.taskPanel.clickItem("Favorites");

			Log.message("3. Navigated to 'Favorties' View");

			//Step-3: Right click on the object
			//----------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in favorites view
				throw new SkipException("Invalid Test Data. The specified Object " + dataPool.get("ObjectName") + " was not found in the favorites view.");

			if(!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Right click the object
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected.");

			Log.message("4. Object (" + dataPool.get("ObjectName") + ") is right clicked.");

			//Verification: To Verify if the 'Delete' Operations menu is in disabled state
			//--------------------------------------------------------------------------
			if(!homePage.menuBar.IsItemEnabledInOperationsMenu("Delete"))  //Checks if delete is disabled state.
				Log.pass("Test Case Passed. The Delete item in Operations menu is in disabled state.");
			else
				Log.fail("Test Case Failed. The Delete item in Operations menu is not in disabled state.", driver);

		} //End try

		catch(Exception e) 	{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest7_1_2B

}

package MFClient.Tests.ObjectOperations;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.Utils;
import genericLibrary.WebDriverUtils;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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
import MFClient.Wrappers.Caption;
import MFClient.Wrappers.ListView;
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class SelectedObjectsOperations {

	public String xlTestDataWorkBook = null;
	public String loginURL = null;
	public String userName = null;
	public String password = null;
	public String testVault = null;
	public String configURL = null;
	public String userFullName = null;
	public String className = null;
	public String productVersion = null;
	public WebDriver driver = null;

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
	 * 18.2.1A : Check out the object and expand the object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","Sprint18", "Smoke"},  
			description = "Check out the object and expand the object")
	public void SprintTest18_2_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.", driver);

			//Step-2: Check out the object
			//-----------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {
				homePage.listView.clickItem(dataPool.get("ObjectName"));
				homePage.taskPanel.clickItem("Check Out");				


				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");
			}

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.", driver);

			//Step-3: Expand the object to view related objects
			//--------------------------------------------------
			if (!homePage.listView.expandItemByName(dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not expanded.");



			if(!homePage.listView.expandItemByName(dataPool.get("RelatedObjectType")))
				throw new Exception("Related object type (" + dataPool.get("RelatedObjectType") + ") is not expanded.");



			Log.message("3. Object is expanded to view related objects.", driver);

			//Verification: To Verify if the related objects are displayed
			//-------------------------------------------------------------
			String option = dataPool.get("RelatedObjects");
			String options[] = option.split(",");
			int count = 0;

			for(count = 0; count < options.length; count++)
				if(!homePage.listView.isItemExists(options[count]))
					break;

			if (count == options.length)
				Log.pass("Test Case Passed. The Checked out object is expanded to view related objects", driver);
			else
				Log.fail("Test Case Failed. The Checked out object is not expanded to view related objects.", driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest18_2_1A

	/**
	 * 18.2.1B : Check in the object and expand the object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","Sprint18", "Smoke"}, 
			description = "Check in the object and expand the object")
	public void SprintTest18_2_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to View
			//------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.", driver);

			//Step-2: Check out the object
			//-----------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))  //Checks if item exists in the list
				throw new SkipException("Invalid test data. Object (" + dataPool.get("ObjectName") + ") does not exists in the homePage.listView.");

			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) {
				homePage.listView.clickItem(dataPool.get("ObjectName"));
				homePage.taskPanel.clickItem("Check Out");				


				if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
					throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");
			}

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.", driver);

			//Step-3: Check in the object
			//-----------------------------
			homePage.listView.clickItem(dataPool.get("ObjectName"));
			homePage.taskPanel.clickItem("Check In");				


			if (ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked in.");

			Log.message("3. Object (" + dataPool.get("ObjectName") + ") is checked in.", driver);

			//Step-3: Expand the object to view related objects
			//--------------------------------------------------
			if (!homePage.listView.expandItemByName(dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not expanded.");

			if(!homePage.listView.expandItemByName(dataPool.get("RelatedObjectType")))
				throw new Exception("Related object type (" + dataPool.get("RelatedObjectType") + ") is not expanded.");

			Log.message("4. Object is expanded to view related objects.", driver);

			//Verification: To Verify if the related objects are displayed
			//-------------------------------------------------------------
			String option = dataPool.get("RelatedObjects");
			String options[] = option.split(",");
			int count = 0;

			for(count = 0; count < options.length; count++){
				if(!homePage.listView.isItemExists(options[count]))
					break;
			}

			if( count == options.length)
				Log.pass("Test Case Passed. The Checked in object is expanded to view related objects.", driver);
			else
				Log.fail("Test Case Failed. The Checked in object is expanded to view related objects.", driver);

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest18_2_1B


	/**
	 * 105.1.1A : Verify if user able to check-in the multiple objects with comments
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Verify if user able to check-in the multiple objects with comments.")
	public void SprintTest105_1_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("safari"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Login to the Home View.
			//----------------------------
			Log.message("1. Logged into the Home View.", driver);

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");
			homePage.menuBar.setGroupObjectsbyObjectType(false);//Disables the Group objects by object type display mode

			Log.message("2. Search for objects.", driver);

			//3. Select multiple objects and check out
			//-----------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);

			Log.message("3. Select multiple objects and check out.", driver);

			//4. Click Check in with comments options from the context menu
			//--------------------------------------------------------------
			homePage.listView.rightClickItem(dataPool.get("Objects").split("\n")[dataPool.get("Objects").split("\n").length-1]);

			homePage.listView.clickContextMenuItem(Caption.MenuItems.CheckInWithComments.Value);


			Log.message("4. Click Check in with comments options from the context menu", driver);

			//5. Set comment and click the Ok button
			//---------------------------------------
			MFilesDialog mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.setComment(dataPool.get("Comment"));
			mFilesDialog.clickOkButton();

			Log.message("5. Set comment and click the Ok button.", driver);

			//Verification: To verify if comment was set the objects were checked in
			//-----------------------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);

			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");

			String[] objects = dataPool.get("Objects").split("\n");
			int count = 0;
			for(count = 0; count < objects.length; count++) {

				if(ListView.isCheckedOutByItemName(driver, objects[count]))
					throw new Exception("Test Case Failed. The Objects were not checked in.");

				homePage.listView.clickItem(objects[count]);

				MetadataCard metadatacard = new MetadataCard(driver, true);

				if(!metadatacard.getComments().contains(dataPool.get("Comment")))
					throw new Exception("Test Case Failed. The objects were checked in but the comments were not set.");

				driver.switchTo().defaultContent();
			}

			if(count == objects.length)
				Log.pass("Test Case Passed. The comment was set the objects were checked in.", driver);
			else 
				Log.fail("Test Case Failed. The verification was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if(!(driver.equals(null)&& homePage.equals(null)))
				homePage.menuBar.setGroupObjectsbyObjectType(true);//Enables the Group objects by object type display mode
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105.1.1B : Verify if user able to check-in the multiple objects with comments
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Verify if user able to check-in the multiple objects with comments.")
	public void SprintTest105_1_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("safari"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Login to the Home View.
			//----------------------------
			Log.message("1. Logged into the Home View.", driver);

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");
			homePage.menuBar.setGroupObjectsbyObjectType(false);//Disables the Group objects by object type display mode

			Log.message("2. Search for objects.", driver);

			//3. Select multiple objects and check out
			//-----------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);

			Log.message("3. Select multiple objects and check out.", driver);

			//4. Click Check in with comments options from the Operations menu
			//-----------------------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.CheckInWithComments.Value);

			Log.message("4. Click Check in with comments options from the Operations menu", driver);

			//5. Set comment and click the Ok button
			//---------------------------------------
			MFilesDialog mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.setComment(dataPool.get("Comment"));
			mFilesDialog.clickOkButton();

			Log.message("5. Set comment and click the Ok button.", driver);

			//Verification: To verify if comment was set the objects were checked in
			//-----------------------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);

			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");

			String[] objects = dataPool.get("Objects").split("\n");
			int count = 0;
			for(count = 0; count < objects.length; count++) {

				if(ListView.isCheckedOutByItemName(driver, objects[count]))
					throw new Exception("Test Case Failed. The Objects were not checked in.");

				homePage.listView.clickItem(objects[count]);

				MetadataCard metadatacard = new MetadataCard(driver, true);

				if(!metadatacard.getComments().contains(dataPool.get("Comment")))
					throw new Exception("Test Case Failed. The objects were checked in but the comments were not set.");

				driver.switchTo().defaultContent();
			}

			if(count == objects.length)
				Log.pass("Test Case Passed. The comment was set the objects were checked in.", driver);
			else 
				Log.fail("Test Case Failed. The verification was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if(!(driver.equals(null)&& homePage.equals(null)))
				homePage.menuBar.setGroupObjectsbyObjectType(true);//Enables the Group objects by object type display mode
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105.1.2A : Verify if user able to checkout the multiple objects in the view (Operation menu)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Verify if user able to checkout the multiple objects in the view (Operation menu).")
	public void SprintTest105_1_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("safari"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 
		HomePage homePage = null;

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Login to the Home View.
			//----------------------------
			Log.message("1. Logged into the Home View.", driver);

			//2. Navigate to the specified view
			//----------------------------------
			homePage.listView.navigateThroughView(dataPool.get("View"));

			Log.message("2. Navigate to the specified view.", driver);

			//3. Select multiple objects
			//---------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));

			Log.message("3. Select multiple objects.", driver);

			//4. Click the Check out option in the operations menu
			//----------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.CheckOut.Value);

			Log.message("4. Click the Check out option in the operations menu", driver);

			//4. Navigate to Checked Out to Me view
			//--------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckedOutToMe.Value);

			Log.message("5. Navigated to Checked Out to Me view", driver);

			//Verification: To verify if the object were checked out
			//-------------------------------------------------------
			String[] objects = dataPool.get("Objects").split("\n");
			int count = 0;
			for(count = 0; count < objects.length; count++) {
				if(!homePage.listView.clickItem(objects[count]))
					Log.fail("Test Case Failed. The Objects were not checked out.", driver);
				else {
					homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);

				}
			}

			if(count == objects.length) 
				Log.pass("Test Case Passed. The Objects were checked out as expected.", driver);
			else 
				Log.fail("Test Case Failed. The verification was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105.1.2B : Verify if user able to checkout the multiple objects in the view (Context menu)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Verify if user able to checkout the multiple objects in the view (Context menu).")
	public void SprintTest105_1_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("safari"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 
		HomePage homePage = null;

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Login to the Home View.
			//----------------------------


			Log.message("1. Logged into the Home View.", driver);

			//2. Navigate to the specified view
			//----------------------------------
			homePage.listView.navigateThroughView(dataPool.get("View"));

			Log.message("2. Navigate to the specified view.", driver);

			//3. Select multiple objects
			//---------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.", driver);

			//4. Click the Check out option in the context menu
			//----------------------------------------------------
			String[] objects = dataPool.get("Objects").split("\n");
			homePage.listView.rightClickItem(objects[objects.length-1]);


			homePage.listView.clickContextMenuItem(Caption.MenuItems.CheckOut.Value);


			Log.message("4. Click the Check out option in the context menu", driver);

			//4. Navigate to Checked Out to Me view
			//--------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckedOutToMe.Value);


			Log.message("5. Navigate to Checked Out to Me view", driver);

			//Verification: To verify if the object were checked out
			//-------------------------------------------------------
			int count = 0;
			for(count = 0; count < objects.length; count++) {
				if(!homePage.listView.clickItem(objects[count]))
					Log.fail("Test Case Failed. The Objects were not checked out.", driver);
				else {
					homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);

				}
			}

			if(count == objects.length) 
				Log.pass("Test Case Passed. The Objects were checked out as expected.", driver);
			else 
				Log.fail("Test Case Failed. The verification was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105.1.2C : Verify if user able to checkout the multiple objects in the view (taskpane options)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Verify if user able to checkout the multiple objects in the view (taskpane options).")
	public void SprintTest105_1_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("safari"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 
		HomePage homePage = null;

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Login to the Home View.
			//----------------------------
			Log.message("1. Logged into the Home View.", driver);

			//2. Navigate to the specified view
			//----------------------------------
			homePage.listView.navigateThroughView(dataPool.get("View"));

			Log.message("2. Navigate to the specified view.", driver);

			//3. Select multiple objects
			//---------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.", driver);

			//4. Click the Check out option in the operations menu
			//----------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);


			Log.message("4. Click the Check out option in the operations menu", driver);

			//4. Navigate to Checked Out to Me view
			//--------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckedOutToMe.Value);


			Log.message("5. Navigate to Checked Out to Me view", driver);

			//Verification: To verify if the object were checked out
			//-------------------------------------------------------
			String[] objects = dataPool.get("Objects").split("\n");
			int count = 0;
			for(count = 0; count < objects.length; count++) {
				if(!homePage.listView.clickItem(objects[count]))
					Log.fail("Test Case Failed. The Objects were not checked out.", driver);
				else {
					homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);

				}

			}

			if(count == objects.length) 
				Log.pass("Test Case Passed. The Objects were checked out as expected.", driver);
			else 
				Log.fail("Test Case Failed. The verification was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105.1.3A : Verify if user able to checkout the multiple objects when one of the object is checked out by a different user (Operations menu) 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Verify if user able to checkout the multiple objects when one of the object is checked out by a different user (Operations menu) .")
	public void SprintTest105_1_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("safari"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);


			//Pre-requisite
			//--------------
			driver.get(loginURL);


			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX")) //Checks if Login.aspx page is loaded
				throw new Exception ("Browser is not navigated to the Login page.");

			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), testVault); //Logs into application


			homePage.listView.navigateThroughView(dataPool.get("View"));
			String[] objects = dataPool.get("Objects").split("\n");

			if(!homePage.listView.clickItem(objects[0]))
				throw new SkipException("Invalid Test data. The given object was not found in the view.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);


			Utility.quitDriver(driver);
			driver = WebDriverUtils.getDriver();

			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.", driver);

			//2. Navigate to the specified view
			//----------------------------------
			homePage.listView.navigateThroughView(dataPool.get("View"));

			Log.message("2. Navigate to the specified view.", driver);

			//3. Select multiple objects
			//---------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.", driver);

			//4. Click the Check out option in the operations menu
			//----------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.CheckOut.Value);


			Log.message("4. Click the Check out option in the operations menu", driver);

			//4. Navigate to Checked Out to Me view
			//--------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckedOutToMe.Value);


			Log.message("5. Navigate to Checked Out to Me view", driver);

			//Verification: To verify if the objects were checked out
			//-------------------------------------------------------
			if(homePage.listView.isItemExists(objects[0]))
				Log.fail("Test Case Failed. The Object checked out to another user was also Checked out to the current user.", driver);

			int count = 1;
			for(count = 1; count < objects.length; count++) {
				if(!homePage.listView.clickItem(objects[count]))
					Log.fail("Test Case Failed. The Objects were not checked out.", driver);
				else {
					homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);

				}
			}

			if(count == objects.length) 
				Log.pass("Test Case Passed. The Objects were checked out as expected.", driver);
			else 
				Log.fail("Test Case Failed. The verification was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105.1.3B : Verify if user able to checkout the multiple objects when one of the object is checked out by a different user (context menu) 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Verify if user able to checkout the multiple objects when one of the object is checked out by a different user (context menu) .")
	public void SprintTest105_1_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("safari"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-requisite
			//--------------
			driver.get(loginURL);

			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX")) //Checks if Login.aspx page is loaded
				throw new Exception ("Browser is not navigated to the Login page.");

			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), testVault); //Logs into application


			homePage.listView.navigateThroughView(dataPool.get("View"));
			String[] objects = dataPool.get("Objects").split("\n");

			if(!homePage.listView.clickItem(objects[0]))
				throw new SkipException("Invalid Test data. The given object was not found in the view.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);

			Utility.quitDriver(driver);
			driver = WebDriverUtils.getDriver();

			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.", driver);

			//2. Navigate to the specified view
			//----------------------------------
			homePage.listView.navigateThroughView(dataPool.get("View"));

			Log.message("2. Navigate to the specified view.", driver);

			//3. Select multiple objects
			//---------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.", driver);

			//4. Click the Check out option in the context menu
			//----------------------------------------------------
			homePage.listView.rightClickItem(objects[0]);

			homePage.listView.clickContextMenuItem(Caption.MenuItems.CheckOut.Value);
			Utils.fluentWait(driver);
			Log.message("4. Click the Check out option in the context menu", driver);

			//4. Navigate to Checked Out to Me view
			//--------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckedOutToMe.Value);

			Log.message("5. Navigate to Checked Out to Me view", driver);

			//Verification: To verify if the objects were checked out
			//-------------------------------------------------------
			if(homePage.listView.isItemExists(objects[0]))
				Log.fail("Test Case Failed. The Object checked out to another user was also Checked out to the current user.", driver);

			int count = 1;
			for(count = 1; count < objects.length; count++) {
				if(!homePage.listView.clickItem(objects[count]))
					Log.fail("Test Case Failed. The Objects were not checked out.", driver);
				else {
					homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);

				}
			}

			if(count == objects.length) 
				Log.pass("Test Case Passed. The Objects were checked out as expected.", driver);
			else 
				Log.fail("Test Case Failed. The verification was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105.1.3C : Verify if user able to checkout the multiple objects when one of the object is checked out by a different user (Operations menu) 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Verify if user able to checkout the multiple objects when one of the object is checked out by a different user (Operations menu) .")
	public void SprintTest105_1_3C(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("safari"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-requisite
			//--------------
			driver.get(loginURL);


			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX")) //Checks if Login.aspx page is loaded
				throw new Exception ("Browser is not navigated to the Login page.");

			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(dataPool.get("UserName"), dataPool.get("Password"), testVault); //Logs into application


			homePage.listView.navigateThroughView(dataPool.get("View"));
			String[] objects = dataPool.get("Objects").split("\n");

			if(!homePage.listView.clickItem(objects[0]))
				throw new SkipException("Invalid Test data. The given object was not found in the view.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);


			Utility.quitDriver(driver);
			driver = WebDriverUtils.getDriver();

			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.", driver);

			//2. Navigate to the specified view
			//----------------------------------
			homePage.listView.navigateThroughView(dataPool.get("View"));

			Log.message("2. Navigate to the specified view.", driver);

			//3. Select multiple objects
			//---------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple objects.", driver);

			//4. Click the Check out option in the task pane
			//----------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);


			Log.message("4. Click the Check out option in the task pane.", driver);

			//4. Navigate to Checked Out to Me view
			//--------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckedOutToMe.Value);


			Log.message("5. Navigate to Checked Out to Me view", driver);

			//Verification: To verify if the objects were checked out
			//-------------------------------------------------------
			if(homePage.listView.isItemExists(objects[0]))
				Log.fail("Test Case Failed. The Object checked out to another user was also Checked out to the current user.", driver);

			int count = 1;
			for(count = 1; count < objects.length; count++) {
				if(!homePage.listView.clickItem(objects[count]))
					Log.fail("Test Case Failed. The Objects were not checked out.", driver);
				else {
					homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);

				}
			}

			if(count == objects.length) 
				Log.pass("Test Case Passed. The Objects were checked out as expected.", driver);
			else 
				Log.fail("Test Case Failed. The verification was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105.1.4A : Verify if user able to checkout the multiple objects in the view (Operations menu)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Verify if user able to checkout the multiple objects in the view (Operations menu).")
	public void SprintTest105_1_4A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("safari"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Login to the Home View.
			//----------------------------


			Log.message("1. Logged into the Home View.", driver);

			//2. Navigate to the specified view
			//----------------------------------
			homePage.listView.navigateThroughView(dataPool.get("View"));

			Log.message("2. Navigate to the specified view.", driver);

			//3. Select multiple checked out objects
			//---------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.CheckOut.Value);

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckedOutToMe.Value);

			String[] objects = dataPool.get("Objects").split("\n");
			int count = 0;
			for(count = 0; count < objects.length; count++) {
				if(!homePage.listView.isItemExists(objects[count]))
					throw new Exception("The selected objects were not checked out.");
			}

			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);

			homePage.listView.navigateThroughView(dataPool.get("View"));

			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple checked out objects.", driver);

			//4. Click the Check In option in the operations menu
			//----------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.CheckIn.Value);


			Log.message("4. Click the Check out option in the operations menu", driver);

			//Verification: To verify if the object were checked In
			//------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckedOutToMe.Value);

			for(count = 0; count < objects.length; count++) {
				if(homePage.listView.isItemExists(objects[count]))
					throw new Exception("The selected objects were not checked In.");
			}

			if(count == objects.length) 
				Log.pass("Test Case Passed. The Objects were checked In as expected.", driver);
			else 
				Log.fail("Test Case Failed. The verification was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105.1.4B : Verify if user able to checkout the multiple objects in the view (Context menu)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Verify if user able to checkout the multiple objects in the view (Context menu).")
	public void SprintTest105_1_4B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("safari"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.", driver);

			//2. Navigate to the specified view
			//----------------------------------
			homePage.listView.navigateThroughView(dataPool.get("View"));

			Log.message("2. Navigate to the specified view.", driver);

			//3. Select multiple checked out objects
			//---------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.CheckOut.Value);
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckedOutToMe.Value);

			String[] objects = dataPool.get("Objects").split("\n");
			int count = 0;
			for(count = 0; count < objects.length; count++) {
				if(!homePage.listView.isItemExists(objects[count]))
					throw new Exception("The selected objects were not checked out.");
			}

			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);

			homePage.listView.navigateThroughView(dataPool.get("View"));

			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple checked out objects.", driver);

			//4. Click the Check In option in the context menu
			//----------------------------------------------------
			homePage.listView.rightClickItem(objects[0]);

			homePage.listView.clickContextMenuItem(Caption.MenuItems.CheckIn.Value);


			Log.message("4. Click the Check In option in the context menu", driver);

			//Verification: To verify if the object were checked In
			//------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckedOutToMe.Value);

			for(count = 0; count < objects.length; count++) {
				if(homePage.listView.isItemExists(objects[count]))
					throw new Exception("The selected objects were not checked In.");
			}

			if(count == objects.length) 
				Log.pass("Test Case Passed. The Objects were checked In as expected.", driver);
			else 
				Log.fail("Test Case Failed. The verification was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105.1.4C : Verify if user able to checkout the multiple objects in the view (taskpane options)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Verify if user able to checkout the multiple objects in the view (taskpane options).")
	public void SprintTest105_1_4C(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("safari"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.", driver);

			//2. Navigate to the specified view
			//----------------------------------
			homePage.listView.navigateThroughView(dataPool.get("View"));

			Log.message("2. Navigate to the specified view.", driver);

			//3. Select multiple checked out objects
			//---------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.CheckOut.Value);

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckedOutToMe.Value);

			String[] objects = dataPool.get("Objects").split("\n");
			int count = 0;
			for(count = 0; count < objects.length; count++) {
				if(!homePage.listView.isItemExists(objects[count]))
					throw new Exception("The selected objects were not checked out.");
			}

			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);

			homePage.listView.navigateThroughView(dataPool.get("View"));

			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple checked out objects.", driver);

			//4. Click the Check In option in the task pane
			//----------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);


			Log.message("4. Click the Check In option in the task pane", driver);

			//Verification: To verify if the object were checked In
			//------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckedOutToMe.Value);

			for(count = 0; count < objects.length; count++) {
				if(homePage.listView.isItemExists(objects[count]))
					throw new Exception("The selected objects were not checked In.");
			}

			if(count == objects.length) 
				Log.pass("Test Case Passed. The Objects were checked In as expected.", driver);
			else 
				Log.fail("Test Case Failed. The verification was not successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}


	/**
	 * 105.6.1 : Disabled Context menu options when objects of same type are multi-selected inside a view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Disabled Context menu options when objects of same type are multi-selected inside a view.")
	public void SprintTest105_6_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("safari"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.", driver);

			//2. Navigate to the specified view
			//----------------------------------
			homePage.listView.navigateThroughView(dataPool.get("View"));

			Log.message("2. Navigate to the specified view.", driver);

			//3. Select multiple Objects and perform a right click
			//-----------------------------------------------------
			String[] objects = dataPool.get("Objects").split("\n");
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));

			homePage.listView.rightClickItem(objects[objects.length-1]);


			Log.message("3. Select multiple Objects and perform a right click", driver);

			//Verification: To verify if the expected options are disabled
			//------------------------------------------------------------
			String[] options = dataPool.get("Options").split("\n");

			for(int count = 0; count < options.length; count++) {
				if(homePage.listView.itemEnabledInContextMenu(options[count]))
					Log.fail("Test Case Failed. Context menu option " + options[count] + " is enabled.", driver);
			}

			Log.pass("Test Case Passed. The Expected options were disabled in the context menu.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105.6.2 : Disabled Operations menu options when objects of same type are multi-selected inside a view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Disabled Operations menu options when objects of same type are multi-selected inside a view.")
	public void SprintTest105_6_2(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("safari"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.", driver);

			//2. Navigate to the specified view
			//----------------------------------
			homePage.listView.navigateThroughView(dataPool.get("View"));

			Log.message("2. Navigate to the specified view.", driver);

			//3. Select multiple Objects and Click the Operations icon
			//---------------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple Objects and Click the Operations icon.", driver);

			//Verification: To verify if the expected options are disabled
			//------------------------------------------------------------
			String[] options = dataPool.get("Options").split("\n");

			List<String> disabledItems = homePage.menuBar.getEnabledOrDisabledItemsInOperationsMenu(false);

			for(int count = 0; count < options.length; count++) {
				if(!disabledItems.contains(options[count]))
					Log.fail("Test Case Failed. Context menu option '" + options[count] + "' is enabled or it does not exist.", driver);
			}

			Log.pass("Test Case Passed. The Expected options were disabled in the context menu.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105.6.3 : Enabled Context menu options when objects of same type are multi-selected inside a view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Enabled Context menu options when objects of same type are multi-selected inside a view.")
	public void SprintTest105_6_3(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("safari"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.", driver);

			//2. Navigate to the specified view
			//----------------------------------
			homePage.listView.navigateThroughView(dataPool.get("View"));

			Log.message("2. Navigate to the specified view.", driver);

			//3. Select multiple Objects and perform a right click
			//-----------------------------------------------------
			String[] objects = dataPool.get("Objects").split("\n");
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));

			homePage.listView.rightClickItem(objects[objects.length-1]);


			Log.message("3. Select multiple Objects and perform a right click", driver);

			//Verification: To verify if the expected options are enabled
			//------------------------------------------------------------
			String[] options = dataPool.get("Options").split("\n");

			for(int count = 0; count < options.length; count++) {
				if(!homePage.listView.itemEnabledInContextMenu(options[count]))
					Log.fail("Test Case Failed. Context menu option " + options[count] + " is disabled.", driver);
			}

			Log.pass("Test Case Passed. The Expected options were enabled in the context menu.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105.6.4 : Enabled Operations menu options when objects of same type are multi-selected inside a view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Enabled Operations menu options when objects of same type are multi-selected inside a view")
	public void SprintTest105_6_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("safari"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 
		HomePage homePage = null;

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.", driver);

			//2. Navigate to the specified view
			//----------------------------------
			homePage.listView.navigateThroughView(dataPool.get("View"));

			Log.message("2. Navigate to the specified view.", driver);

			//3. Select multiple Objects and Click the Operations icon
			//---------------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));

			Log.message("3. Select multiple Objects and Click the Operations icon.", driver);

			//Verification: To verify if the expected options are enabled
			//------------------------------------------------------------
			String[] options = dataPool.get("Options").split("\n");

			List<String> enabledItems = homePage.menuBar.getEnabledOrDisabledItemsInOperationsMenu(true);

			for(int count = 0; count < options.length; count++) {
				if(!enabledItems.contains(options[count]))
					Log.fail("Test Case Failed. Context menu option " + options[count] + " is disabled or does not exist.", driver);
			}

			Log.pass("Test Case Passed. The Expected options were enabled in the context menu.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105.6.5 : Disabled Context menu options when objects of different type are multi-selected in search results
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Disabled Context menu options when objects of different type are multi-selected in search results.")
	public void SprintTest105_6_5(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("safari"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 
		HomePage homePage = null;

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.", driver);

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");
			homePage.menuBar.setGroupObjectsbyObjectType(false);//Disables the Group objects by object type display mode 

			Log.message("2. Search for objects.", driver);

			//3. Select multiple Objects and perform a right click
			//-----------------------------------------------------
			String[] objects = dataPool.get("Objects").split("\n");
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));
			homePage.listView.rightClickItem(objects[objects.length-1]);

			Log.message("3. Select multiple Objects and perform a right click", driver);

			//Verification: To verify if the expected options are disabled
			//------------------------------------------------------------
			String[] options = dataPool.get("Options").split("\n");

			for(int count = 0; count < options.length; count++) {
				if(homePage.listView.itemEnabledInContextMenu(options[count]))
					Log.fail("Test Case Failed. Context menu option " + options[count] + " is enabled.", driver);
			}

			Log.pass("Test Case Passed. The Expected options were disabled in the context menu.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if(!(driver.equals(null)&& homePage.equals(null)))
				homePage.menuBar.setGroupObjectsbyObjectType(true);//Enables the Group objects by object type display mode

			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105.6.6 : Disabled Operations menu options when objects of different type are multi-selected in search results
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Disabled Operations menu options when objects of different type are multi-selected in search results.")
	public void SprintTest105_6_6(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("safari"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.", driver);

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");
			homePage.menuBar.setGroupObjectsbyObjectType(false);//Disables the Group objects by object type display mode

			Log.message("2. Search for objects.", driver);

			//3. Select multiple Objects and Click the Operations icon
			//---------------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple Objects and Click the Operations icon.", driver);

			//Verification: To verify if the expected options are disabled
			//------------------------------------------------------------
			String[] options = dataPool.get("Options").split("\n");

			for(int count = 0; count < options.length; count++) {
				if(homePage.menuBar.IsItemEnabledInOperationsMenu(options[count]))
					Log.fail("Test Case Failed. Context menu option " + options[count] + " is enabled.", driver);
			}

			Log.pass("Test Case Passed. The Expected options were disabled in the context menu.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if(!(driver.equals(null)&& homePage.equals(null)))
				homePage.menuBar.setGroupObjectsbyObjectType(true);//Enables the Group objects by object type display mode

			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105.6.7 : Enabled Context menu options when objects of different type are multi-selected in search results
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Enabled Context menu options when objects of different type are multi-selected in search results.")
	public void SprintTest105_6_7(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("safari"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 
		HomePage homePage = null;				

		try {

			driver = WebDriverUtils.getDriver();ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			homePage =  LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.", driver);

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");
			homePage.menuBar.setGroupObjectsbyObjectType(false);//Disables the Group objects by object type display mode

			Log.message("2. Search for objects.", driver);

			//3. Select multiple Objects and perform a right click
			//-----------------------------------------------------
			String[] objects = dataPool.get("Objects").split("\n");
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));

			homePage.listView.rightClickItem(objects[objects.length-1]);


			Log.message("3. Select multiple Objects and perform a right click", driver);

			//Verification: To verify if the expected options are enabled
			//------------------------------------------------------------
			String[] options = dataPool.get("Options").split("\n");

			for(int count = 0; count < options.length; count++) {
				if(!homePage.listView.itemEnabledInContextMenu(options[count]))
					Log.fail("Test Case Failed. Context menu option " + options[count] + " is disabled.", driver);
			}

			Log.pass("Test Case Passed. The Expected options were enabled in the context menu.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if(!(driver.equals(null)&& homePage.equals(null)))
				homePage.menuBar.setGroupObjectsbyObjectType(true);//Enables the Group objects by object type display mode
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105.6.8 : Enabled Operations menu options when objects of different type are multi-selected in search results
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Enabled Operations menu options when objects of different type are multi-selected in search results.")
	public void SprintTest105_6_8(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("safari"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);


			//1. Login to the Home View.
			//----------------------------
			homePage =  LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.", driver);

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search all objects");
			homePage.menuBar.setGroupObjectsbyObjectType(false);//Disables the Group objects by object type display mode

			Log.message("2. Search for objects.", driver);

			//3. Select multiple Objects and Click the Operations icon
			//---------------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));


			Log.message("3. Select multiple Objects and Click the Operations icon.", driver);

			//Verification: To verify if the expected options are enabled
			//------------------------------------------------------------
			String[] options = dataPool.get("Options").split("\n");

			for(int count = 0; count < options.length; count++) {
				if(!homePage.menuBar.IsItemEnabledInOperationsMenu(options[count]))
					Log.fail("Test Case Failed. Context menu option " + options[count] + " is disabled.", driver);
				//homePage.menuBar.clickSettingsIcon();
			}

			Log.pass("Test Case Passed. The Expected options were enabled in the context menu.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if(!(driver.equals(null)&&homePage.equals(null)))
				homePage.menuBar.setGroupObjectsbyObjectType(true);//Enables the Group objects by object type display mode

			Utility.quitDriver(driver);
		}
	}
	/**
	 * 105.6.9 : Check if Settings menu bar displays the default options when no object is selected (in views)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Check if Settings menu bar displays the default options when no object is selected (in views).")
	public void SprintTest105_6_9(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("safari"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);


			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			Log.message("1. Logged into the Home View.", driver);

			//2. Navigate to the specified view
			//----------------------------------
			homePage.listView.navigateThroughView(dataPool.get("View"));

			Log.message("2. Navigate to the specified view.", driver);

			//Verification: To verify if the expected options are enabled
			//------------------------------------------------------------
			String[] options = dataPool.get("Options").split("\n");

			for(int count = 0; count < options.length; count++) {
				if(!homePage.menuBar.IsItemEnabledInOperationsMenu(options[count]))
					Log.fail("Test Case Failed. Context menu option " + options[count] + " is disabled.", driver);
			}

			Log.pass("Test Case Passed. The Expected options were enabled in the context menu.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105.6.10 : Check if Settings menu bar displays the default options when no object is selected (in search results)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Check if Settings menu bar displays the default options when no object is selected (in search results).")
	public void SprintTest105_6_10(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("safari"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.", driver);

			//2. Navigate to the specified view
			//----------------------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), dataPool.get("SearchType"));

			Log.message("2. Navigate to the specified view.", driver);

			//Verification: To verify if the expected options are enabled
			//------------------------------------------------------------
			String[] options = dataPool.get("Options").split("\n");

			for(int count = 0; count < options.length; count++) {
				if(!homePage.menuBar.IsItemEnabledInOperationsMenu(options[count]))
					Log.fail("Test Case Failed. Context menu option " + options[count] + " is disabled.", driver);
			}

			Log.pass("Test Case Passed. The Expected options were enabled in the context menu.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105.6.11 : Check if shortcut keys are working properly when no object is selected(in search results)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Check if shortcut keys are working properly when no object is selected(in search results).")
	public void SprintTest105_6_11(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("safari"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.", driver);

			//2. Navigate to the specified view
			//----------------------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), dataPool.get("SearchType"));

			Log.message("2. Navigate to the specified view.", driver);

			//3. Simulate Shortcut key
			//------------------------
			String expectedURl = driver.getCurrentUrl();
			Robot robot=new Robot();
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyRelease(KeyEvent.VK_CONTROL);

			Log.message("3. Simulate Shortcut key", driver);

			//Verification: To verify if no action happens
			//---------------------------------------------
			if(expectedURl.equals(driver.getCurrentUrl()))
				Log.pass("Test Case Passed. No action happened after the key simulation.", driver);
			else
				Log.fail("Test Case Failed. The URL has changed after simulation of the shortcut key.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105.6.12 : Check if shortcut keys are working properly when no object is selected (in search result)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Check if shortcut keys are working properly when no object is selected (in search result).")
	public void SprintTest105_6_12(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("safari"))
			throw new SkipException(driverType + " driver does not supports Actions.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.", driver);

			//2. Navigate to the specified view
			//----------------------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), dataPool.get("SearchType"));


			Log.message("2. Navigate to the specified view.", driver);

			//3. Simulate Shortcut key
			//------------------------
			Actions action = new Actions(driver);
			//			Robot robot=new Robot();
			if(dataPool.get("Control").equals("Yes")) {
				//                robot.keyPress(KeyEvent.VK_CONTROL);
				//                robot.keyPress(KeyEvent.VK_"+dataPool.get("Key"));
				//                robot.keyRelease(KeyEvent.VK_CONTROL);

				action.keyDown(Keys.CONTROL).sendKeys(dataPool.get("Key")).perform();
				action.keyDown(Keys.CONTROL).sendKeys(dataPool.get("Key")).perform();
			}
			else {
				if(dataPool.get("Key").equals("Del"))
					action.sendKeys(Keys.DELETE);
				if(dataPool.get("Key").equals("F2"))
					action.sendKeys(Keys.F2);
			}


			Log.message("3. Simulate Shortcut key", driver);

			//Verification: To verify if no action happens
			//---------------------------------------------
			if(!MFilesDialog.exists(driver, dataPool.get("Caption")))
				Log.pass("Test Case Passed. No action happened after the key simulation.", driver);
			else
				Log.fail("Test Case Failed. The Dialog has appeared after simulation of the shortcut key.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105.6.13 : Check if shortcut keys are working properly when no object is selected (in views)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Check if shortcut keys are working properly when no object is selected (in views).")
	public void SprintTest105_6_13(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("safari"))
			throw new SkipException(driverType + " driver does not supports Actions.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.", driver);

			//2. Navigate to the specified view
			//----------------------------------
			homePage.listView.navigateThroughView(dataPool.get("View"));


			Log.message("2. Navigate to the specified view.", driver);

			//3. Simulate Shortcut key
			//------------------------
			String expectedURl = driver.getCurrentUrl();
			Actions action = new Actions(driver);
			action.keyDown(Keys.CONTROL).sendKeys(dataPool.get("Key")).perform();


			Log.message("3. Simulate Shortcut key", driver);

			//Verification: To verify if no action happens
			//---------------------------------------------
			if(expectedURl.equals(driver.getCurrentUrl()))
				Log.pass("Test Case Passed. No action happened after the key simulation.", driver);
			else
				Log.fail("Test Case Failed. The URL has changed after simulation of the shortcut key.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105.6.14 : Check if shortcut keys are working properly when no object is selected (in views)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint44", "Metadatacard"}, 
			description = "Check if shortcut keys are working properly when no object is selected (in views).")
	public void SprintTest105_6_14(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("safari"))
			throw new SkipException(driverType + " driver does not supports Actions.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.", driver);

			//2. Navigate to the specified view
			//----------------------------------
			homePage.listView.navigateThroughView(dataPool.get("View"));


			Log.message("2. Navigate to the specified view.", driver);

			//3. Simulate Shortcut key
			//------------------------
			Actions action = new Actions(driver);
			if(dataPool.get("Control").equals("Yes")) {
				action.keyDown(Keys.CONTROL).sendKeys(dataPool.get("Key")).perform();
			}
			else {
				if(dataPool.get("Key").equals("Del"))
					action.sendKeys(Keys.DELETE);
				if(dataPool.get("Key").equals("F2"))
					action.sendKeys(Keys.F2);
			}


			Log.message("3. Simulate Shortcut key", driver);

			//Verification: To verify if no action happens
			//---------------------------------------------
			if(!MFilesDialog.exists(driver, dataPool.get("Caption")))
				Log.pass("Test Case Passed. No action happened after the key simulation.", driver);
			else
				Log.fail("Test Case Failed. The Dialog has appeared after simulation of the shortcut key.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105.6.15 : Enabled Context menu options when multiple checked out SFDs are selected
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Enabled Context menu options when multiple checked out SFDs are selected.")
	public void SprintTest105_6_15(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("safari"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.", driver);

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), dataPool.get("SearchType"));

			Log.message("2. Search for objects.", driver);

			//3. Select multiple checked out SFD Objects
			//-------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);

			Log.message("3. Select multiple checked out SFD Objects.", driver);

			//4. Perform a right click
			//-------------------------
			homePage.listView.rightClickItem(dataPool.get("Objects").split("\n")[0]);

			Log.message("4. Perform a right click", driver);

			//Verification: To verify if the expected options are enabled
			//------------------------------------------------------------
			String[] options = dataPool.get("Options").split("\n");

			for(int count = 0; count < options.length; count++) {
				if(!homePage.listView.itemEnabledInContextMenu(options[count]))
					Log.fail("Test Case Failed. Context menu option " + options[count] + " is disabled.", driver);
			}

			Log.pass("Test Case Passed. The Expected options were enabled in the context menu.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if(!(driver.equals(null)&&homePage.equals(null))){			
				homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);
			}
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105.6.16 : Enabled Operations menu options when multiple checked out SFDs are selected
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Enabled Operations menu options when multiple checked out SFDs are selected.")
	public void SprintTest105_6_16(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("safari"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.", driver);

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), dataPool.get("SearchType"));

			Log.message("2. Search for objects.", driver);
			//3. Select multiple checked out SFD Objects
			//-------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);

			Log.message("3. Select multiple checked out SFD Objects.", driver);
			//Verification: To verify if the expected options are enabled
			//------------------------------------------------------------
			String[] options = dataPool.get("Options").split("\n");

			for(int count = 0; count < options.length; count++) {
				if(!homePage.menuBar.IsItemEnabledInOperationsMenu(options[count]))
					Log.fail("Test Case Failed. Context menu option " + options[count] + " is disabled.", driver);
			}
			Log.pass("Test Case Passed. The Expected options were enabled in the context menu.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if(!(driver.equals(null)&&homePage.equals(null))){		
				homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);
			}
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105.6.17 : Multi-select SFD to MFD conversion (Context menu)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Multi-select SFD to MFD conversion (Context menu).")
	public void SprintTest105_6_17(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("safari"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);


			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.", driver);

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), dataPool.get("SearchType"));

			Log.message("2. Search for objects.", driver);

			//3. Select multiple checked out SFD Objects
			//-------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);

			Log.message("3. Select multiple checked out SFD Objects.", driver);

			//4. Perform a right click
			//-------------------------
			String[] objects = dataPool.get("Objects").split("\n");
			homePage.listView.rightClickItem(objects[0]);

			Log.message("4. Perform a right click", driver);

			//5. Click the Convert to Multi-file Docum... context menu
			//---------------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.ConvertToMFD_C.Value);


			Log.message("5. Click the Convert to Multi-file Docum... context menu", driver);

			//6. Check in the objects
			//------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);


			Log.message("6. Check in the objects", driver);

			//Verification: To verify if the SFDs are converted to MFDs
			//------------------------------------------------------------
			for(int count = 0; count < objects.length; count++) {
				if(homePage.listView.getColumnValueByItemName(objects[count].split("\\.")[0], Caption.Column.Coln_SingleFile.Value).equals("Yes"))
					Log.fail("Test Case Failed. The SFD " + objects[count] + " was not converted to MFD.", driver);
			}

			Log.pass("Test Case Passed. Multi-select Conversion using Context menu is successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if(driver != null && homePage != null){
				homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);
				homePage.listView.removeColumn(Caption.Column.Coln_SingleFile.Value);
			}
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105.6.18 : Multi-select MFD to SFD conversion (Context menu)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Multi-select MFD to SFD conversion (Context menu).")
	public void SprintTest105_6_18(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("safari"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);


			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.", driver);

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), dataPool.get("SearchType"));

			Log.message("2. Search for objects.", driver);

			//3. Select multiple checked out SFD Objects
			//-------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);

			Log.message("3. Select multiple checked out SFD Objects.", driver);

			//4. Perform a right click
			//-------------------------
			String[] objects = dataPool.get("Objects").split("\n");
			homePage.listView.rightClickItem(objects[0]);

			Log.message("4. Perform a right click", driver);

			//5. Click the Convert to Multi-file Docum... context menu
			//---------------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.ConvertToSFD_C.Value);

			Log.message("5. Click the Convert to Multi-file Docum... context menu", driver);

			//6. Check in the objects
			//------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);

			Log.message("6. Check in the objects", driver);

			//Verification: To verify if the SFDs are converted to MFDs
			//----------------------------------------------------------
			String[] SFDs = dataPool.get("SFDs").split("\n");
			for(int count = 0; count < objects.length; count++) {
				if(homePage.listView.getColumnValueByItemName(SFDs[count], Caption.Column.Coln_SingleFile.Value).equals("No"))
					Log.fail("Test Case Failed. The SFD " + SFDs[count] + " was not converted to MFD.", driver);
			}

			Log.pass("Test Case Passed. Multi-select Conversion using Context menu is successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if(driver != null && homePage != null){
				homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);
				homePage.listView.removeColumn(Caption.Column.Coln_SingleFile.Value);
			}
			Utility.quitDriver(driver);

		}
	}

	/**
	 * 105.6.19 : Multi-select SFD to MFD conversion (Operations Menu)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Multi-select SFD to MFD conversion (Operations Menu).")
	public void SprintTest105_6_19(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("safari"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.", driver);

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), dataPool.get("SearchType"));

			Log.message("2. Search for objects.", driver);

			//3. Select multiple checked out SFD Objects
			//-------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);


			Log.message("3. Select multiple checked out SFD Objects.", driver);
			//4. Click the Convert to Multi-file Docum... from Operations menu
			//-----------------------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.ConvertToMFD_O.Value);


			Log.message("4. Click the Convert to Multi-file Docum... from Operations menu", driver);

			//5. Check in the objects
			//------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);


			Log.message("5. Check in the objects", driver);

			//Verification: To verify if the SFDs are converted to MFDs
			//------------------------------------------------------------
			String[] objects = dataPool.get("Objects").split("\n");
			for(int count = 0; count < objects.length; count++) {
				if(homePage.listView.getColumnValueByItemName(objects[count].split("\\.")[0], Caption.Column.Coln_SingleFile.Value).equals("Yes"))
					Log.fail("Test Case Failed. The SFD " + objects[count] + " was not converted to MFD.", driver);
			}

			Log.pass("Test Case Passed. Multi-select Conversion using Operations menu is successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if(driver != null && homePage != null){
				homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);
				homePage.listView.removeColumn(Caption.Column.Coln_SingleFile.Value);
			}
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105.6.20 : Multi-select MFD to SFD conversion (Operations Menu)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "Sprint44", "Metadatacard"}, 
			description = "Multi-select MFD to SFD conversion (Operations Menu).")
	public void SprintTest105_6_20(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("safari"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);


			//1. Login to the Home View.
			//----------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.", driver);

			//2. Search for objects
			//----------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), dataPool.get("SearchType"));

			Log.message("2. Search for objects.", driver);

			//3. Select multiple checked out SFD Objects
			//-------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value);


			Log.message("3. Select multiple checked out SFD Objects.", driver);

			//4. Click the Convert to Multi-file Docum... from Operations menu
			//-----------------------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.ConvertToSFD_O.Value);


			Log.message("4. Click the Convert to Multi-file Docum... from Operations menu", driver);

			//5. Check in the objects
			//------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);


			Log.message("5. Check in the objects", driver);

			//Verification: To verify if the SFDs are converted to MFDs
			//----------------------------------------------------------
			String[] SFDs = dataPool.get("SFDs").split("\n");
			for(int count = 0; count < SFDs.length; count++) {
				if(homePage.listView.getColumnValueByItemName(SFDs[count], Caption.Column.Coln_SingleFile.Value).equals("No"))
					Log.fail("Test Case Failed. The SFD " + SFDs[count] + " was not converted to MFD.", driver);
			}

			Log.pass("Test Case Passed. Multi-select Conversion using Operations menu is successful.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			if(driver != null && homePage != null){
				homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value);
				homePage.listView.removeColumn(Caption.Column.Coln_SingleFile.Value);
			}
			Utility.quitDriver(driver);
		}
	}

} //End class SelectedObjectOperations

package MFClient.Tests;


import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
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
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class Annotations {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String userName = null;
	public static String password = null;
	public static String testVault = null;
	public static String configURL = null;
	public static String productVersion = null;
	public static String userFullName = null;
	public static String driverType = null;
	public static String className = null;
	public String methodName = null;
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
			userName = xmlParameters.getParameter("UserName");
			userFullName = xmlParameters.getParameter("UserFullName");
			password = xmlParameters.getParameter("Password");
			testVault = xmlParameters.getParameter("VaultName");	
			configURL = xmlParameters.getParameter("ConfigurationURL");
			driverType = xmlParameters.getParameter("driverType");

			className = this.getClass().getSimpleName().toString().trim();
			if (driverType.equalsIgnoreCase("IE"))
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + driverType.toUpperCase().trim() + xmlParameters.getParameter("driverVersion").trim();
			else
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + driverType.toUpperCase().trim();

			Utility.restoreTestVault();//Restore the annotation vault
			Utility.configureUsers(xlTestDataWorkBook);//configure the user to restore vault
			Utility.setAnnotations();//Set the annotation registry key settings

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
	 * Annotations_19136 : Verify 'New Annotations' are not visible if metadata tab are selected for doc,xls,pdf
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Annotations"}, 
			description = "Verify 'New Annotations' are not visible if metadata tab are selected for doc,xls,pdf.")
	public void Annotations_19136(HashMap<String,String> dataValues, String driverType) throws Exception {


		try {



			driver = WebDriverUtils.getDriver();

			//Pre-requiste : login to MFWA with valid credentials
			//---------------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver,true);//Login to Mfwa with valid credentials

			//Step-1 : Navigate to the specified search view
			//----------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Selected the specified object from the search view
			//-----------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))//Verify if object is displayed in search view
				throw new Exception("Object : " +  dataPool.get("ObjectName") + " is not displayed in search view.");

			Log.message("2. Selected the " + dataPool.get("ObjectName") + " from the search view.");

			//Verification : Verify if 'New annotations' option is displayed in task pane
			//---------------------------------------------------------------------------
			if(!homePage.taskPanel.isItemExists(Caption.Taskpanel.NewAnnotations.Value))
				Log.pass("Test Case Passed." + Caption.Taskpanel.NewAnnotations.Value + " is not visible for selected object :" + dataPool.get("ObjectName") + " if metadata tab are selected.",driver);
			else
				Log.fail("Test Case Failed." + Caption.Taskpanel.NewAnnotations.Value + " is visible selected object :" + dataPool.get("ObjectName") ,driver);

		}//End try
		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Annotations_19136

	/**
	 * Annotations_19137 : Verify 'New Annotations' are visible if preview tab are selected for doc,xls,pdf
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Annotations"}, 
			description = "Verify 'New Annotations' are visible if preview tab are selected for doc,xls,pdf.")
	public void Annotations_19137(HashMap<String,String> dataValues, String driverType) throws Exception {


		try {



			driver = WebDriverUtils.getDriver();

			//Pre-requiste : login to MFWA with valid credentials
			//---------------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver,true);//Login to Mfwa with valid credentials

			//Step-1 : Navigate to the specified search view
			//----------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Selected the specified object from the search view
			//-----------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))//Verify if object is displayed in search view
				throw new Exception("Object : " +  dataPool.get("ObjectName") + " is not displayed in search view.");

			Log.message("2. Selected the " + dataPool.get("ObjectName") + " from the search view.");

			//Step-3 : Selected the preview tab for the selected object
			//---------------------------------------------------------
			homePage.previewPane.clickPreviewTab();//Selected the preview tab for the object

			Log.message("3. Preview tab is selected for the specified object : " + dataPool.get("ObjectName") , driver);

			//Verification : Verify if 'New annotations' option is displayed in task pane
			//---------------------------------------------------------------------------
			if(homePage.taskPanel.isItemExists(Caption.Taskpanel.NewAnnotations.Value))
				Log.pass("Test Case Passed." + Caption.Taskpanel.NewAnnotations.Value + " is visible for the selected object :" + dataPool.get("ObjectName") + " if preview tab are selected.",driver);
			else
				Log.fail("Test Case Failed." + Caption.Taskpanel.NewAnnotations.Value + " is not visible selected object :" + dataPool.get("ObjectName") + " if preview tab is selected.",driver);

		}//End try
		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Annotations_19137

	/**
	 * Annotations_19138 : Verify 'New Annotations' are not visible for other object types
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Annotations"}, 
			description = "Verify 'New Annotations' are not visible for other object types.")
	public void Annotations_19138(HashMap<String,String> dataValues, String driverType) throws Exception {


		try {



			driver = WebDriverUtils.getDriver();

			//Pre-requiste : login to MFWA with valid credentials
			//---------------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver,true);//Login to Mfwa with valid credentials

			//Step-1 : Navigate to the specified search view
			//----------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Selected the specified object from the search view
			//-----------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))//Verify if object is displayed in search view
				throw new Exception("Object : " +  dataPool.get("ObjectName") + " is not displayed in search view.");

			Log.message("2. Selected the " + dataPool.get("ObjectName") + " from the search view.");

			//Step-3 : Selected the preview tab for the selected object
			//---------------------------------------------------------
			homePage.previewPane.clickPreviewTab();//Selected the preview tab for the object

			Log.message("3. Preview tab is selected for the specified object : " + dataPool.get("ObjectName") , driver);

			//Verification : Verify if 'New annotations' option is displayed in task pane
			//---------------------------------------------------------------------------
			if(!homePage.taskPanel.isItemExists(Caption.Taskpanel.NewAnnotations.Value))
				Log.pass("Test Case Passed." + Caption.Taskpanel.NewAnnotations.Value + " is not visible for other object types :" + dataPool.get("ObjectName") ,driver);
			else
				Log.fail("Test Case Failed." + Caption.Taskpanel.NewAnnotations.Value + " is visible for other object types :" + dataPool.get("ObjectName"),driver);

		}//End try
		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Annotations_19138

	/**
	 * Annotations_19153 : For multi selected document objects ,the annotations are created for recently selected object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Annotations"}, 
			description = "For multi selected document objects ,the annotations are created for recently selected object.")
	public void Annotations_19153(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("Safari") || driverType.equalsIgnoreCase("Firefox"))
			throw new SkipException(driverType.toUpperCase() +" driver does not support multi select.");

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-requiste : login to MFWA with valid credentials
			//---------------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver,true);//Login to Mfwa with valid credentials

			//Step-1 : Navigate to the specified search view
			//----------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"),"");

			Log.message("1. Logged into the default page and navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Selected the specified object from the search view
			//-----------------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("ObjectName"));//Select the Multiple objects in list view

			Log.message("2. Selected the multiple objects from the search " + viewToNavigate  + " view.");

			//Step-3 : Selected the 'Annotations' object type in menu bar
			//-----------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.MenuItems.Annotation.Value);

			Log.message("3. Selected the annotation option in new menu bar.", driver);

			String expectedResults = "";

			//Verify if 'New annotations' option is not displayed in task pane
			//---------------------------------------------------------------------------
			if(homePage.listView.isItemExists("Annotations"))
				expectedResults = "Annotation object wrongly listed in search view for multiple objects.";

			//Verify if 'Save annotation' option is not displayed in task pane
			//----------------------------------------------------------------
			if(homePage.taskPanel.isItemExists(Caption.Taskpanel.SaveAnnotations.Value))
				expectedResults += "Save annotation option is displayed in task pane for multiple objects.";

			//Verification : Verify if 'New annotations' option is not displayed in task pane
			//-------------------------------------------------------------------------------
			if(expectedResults.equals(""))			
				Log.pass("Test Case Passed." + Caption.Taskpanel.NewAnnotations.Value + " is not created for the selected multiselect object." ,driver);
			else
				Log.fail("Test Case Failed." + Caption.Taskpanel.NewAnnotations.Value + " is created for the selected multiselect object.",driver);

		}//End try
		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Annotations_19153

	/**
	 * Annotations_19145 : Verify to create 'New Annotations' from 'New' menu for older object version in 'History' view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Annotations"}, 
			description = "Verify to create 'New Annotations' from 'New' menu for older object version in 'History' view.")
	public void Annotations_19145(HashMap<String,String> dataValues, String driverType) throws Exception {


		try {



			driver = WebDriverUtils.getDriver();

			//Pre-requiste : login to MFWA with valid credentials
			//---------------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver,true);//Login to Mfwa with valid credentials

			//Step-1 : Navigate to the specified search view
			//----------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Selected the specified object from the search view
			//-----------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new Exception("Object : " + dataPool.get("ObjectName") + " is not displayed in search view.");

			Log.message("2. Selected the " + dataPool.get("ObjectName") + " from the search view.");

			//Step-3 : Navigate to the history view
			//-------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.History.Value);//Select the history option from the operations menu

			Log.message("3. Selected the history option from the operations menu.", driver);

			//Step-4 : Select the latest version in the history view
			//------------------------------------------------------
			if(!homePage.listView.clickItemByIndex(1))//Select the older version in history view
				throw new Exception("No objects is displayed in list view.");

			Log.message("4. Selected the latest version of the object : " + dataPool.get("ObjectName"), driver);

			//Step-5 : Selected the 'Annotations' object type in menu bar
			//-----------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.MenuItems.Annotation.Value);//Select the annotation option from the new menu bar
			String newAnnotation = homePage.listView.getSelectedListViewItem();//Get the new annotaiton document

			Log.message("5. Selected the annotation option in new menu bar.", driver);

			String expectedResults = "" ;

			//Verify if annotation object is not displayed for object listed in history view
			//------------------------------------------------------------------------------
			if(!homePage.listView.isItemExists(newAnnotation))
				expectedResults = "Annoation object '" + newAnnotation + "' is not displayed in listview.";

			//Verify if annotation object is not displayed for object listed in history view
			//------------------------------------------------------------------------------
			if(!homePage.taskPanel.isItemExists(Caption.Taskpanel.SaveAnnotations.Value))
				expectedResults += "Save Annoation object is not displayed in task pane.";

			//Verification : Verify if 'New annotations' option is displayed in task pane
			//---------------------------------------------------------------------------
			if(expectedResults.equals(""))
				Log.pass("Test Case Passed." + Caption.Taskpanel.NewAnnotations.Value + " is created for selected older version object.", driver);
			else
				Log.fail("Test Case Failed." + Caption.Taskpanel.NewAnnotations.Value + " is not created for selected older version of object.", driver);

		}//End try
		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Annotations_19145


	/**
	 * Annotations_19149 : Verify if object actions are takes place after selecting links for current annotation object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Annotations"}, 
			description = "Verify if object actions are takes place after selecting links for current annotation object.")
	public void Annotations_19149(HashMap<String,String> dataValues, String driverType) throws Exception {


		try {



			driver = WebDriverUtils.getDriver();//Instantiate the webdriver

			//Pre-requiste : login to MFWA with valid credentials
			//---------------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver,true);//Login to Mfwa with valid credentials

			//Step-1 : Navigate to the specified search view
			//----------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"),"");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Selected the specified object & Create the new annotation for selected object
			//-------------------------------------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))//Select the object from the search view
				throw new Exception("Object : " + dataPool.get("ObjectName") + " is not displayed in search view.");

			homePage.previewPane.clickPreviewTab();//Select the preview tab for the selected object

			if(!homePage.taskPanel.clickItem(Caption.Taskpanel.NewAnnotations.Value))//Select the new annotation from the task pane
				throw new Exception("Option : " + Caption.Taskpanel.NewAnnotations.Value + " is not displayed in task pane.");

			String newAnnotation = homePage.listView.getSelectedListViewItem();//Get the new annotaiton document

			if(!homePage.taskPanel.clickItem(Caption.Taskpanel.SaveAnnotations.Value))//Select the Save annotation option from the task pane
				throw new Exception("Option : " + Caption.Taskpanel.SaveAnnotations.Value + " is not displayed in task pane.");

			Log.message("2. Created the new annotation document : " + newAnnotation + " for the selected object : " + dataPool.get("ObjectName"), driver);

			//Verify if new annotation object is displayed in list view
			//---------------------------------------------------------
			if(!homePage.listView.clickItem(newAnnotation))//Select the new created annotaion object			
				throw new Exception("Object : " + newAnnotation + " is not displayed in search view.");

			//Step-3 : Select the 'Properties' option from the operations menu
			//----------------------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value);//Select the properties option from the operations menu

			Log.message("3. Selected 'Properties' option for the created annotation object :  " + newAnnotation + " from the search view.");

			//Verification : Verify if MetadataCard is opened for the created annotated object
			//--------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the pop-out metadatacard
			if(MetadataCard.isMetadataCardOpened(driver)&& metadataCard.getTitle().equals(newAnnotation))
				Log.pass("Test Case Passed.Metadatacard is opened for the selected annotated object while selecting the 'Properties' link as expected.",driver);
			else
				Log.fail("Test Case Failed.Metaddatacard is not opened for the selected annotated object.", driver);		

		}//End try
		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Annotations_19149


	/**
	 * Annotations_44324 : Verify if object actions are takes place after selecting history/Relationship for annotation object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Annotations"}, 
			description = "Verify if object actions are takes place after selecting history/Relationship for annotation object.")
	public void Annotations_44324(HashMap<String,String> dataValues, String driverType) throws Exception {


		try {



			driver = WebDriverUtils.getDriver();//Instantiate the webdriver

			//Pre-requiste : login to MFWA with valid credentials
			//---------------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver,true);//Login to Mfwa with valid credentials

			//Step-1 : Navigate to the specified search view
			//----------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"),"");

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Selected the specified object & Create the new annotation for selected object
			//-------------------------------------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))//Select the object from the search view
				throw new Exception("Object : " + dataPool.get("ObjectName") + " is not displayed in list view.");

			homePage.previewPane.clickPreviewTab();//Select the preview for the selected object

			homePage.menuBar.clickNewMenuItem(Caption.MenuItems.Annotation.Value);//Select the new annotation from the menu bar

			String newAnnotation = homePage.listView.getSelectedListViewItem();//Get the new annotaiton document

			if(!homePage.taskPanel.clickItem(Caption.Taskpanel.SaveAnnotations.Value))//Select the Save annotation option from the task pane
				throw new Exception("Option : " + Caption.Taskpanel.SaveAnnotations.Value + " is not displayed in task pane.");

			Log.message("2. Created the new annotation document : " + newAnnotation + " for the selected object : " + dataPool.get("ObjectName"), driver);

			//Verify if new annotation object is selected by default 
			//------------------------------------------------------
			if(!homePage.listView.clickItem(newAnnotation))//Verify if newly created annotation object is selected
				throw new Exception("New annotation object : " + newAnnotation + " is not displayed in list view.");//Select the newly created annotaion object			

			//Step-3 : Select the specified option from operations menu
			//---------------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(dataPool.get("links"));//Select the specified option in operations menu

			Log.message("3. Selected the : " + dataPool.get("links") + " option from the operations menu.", driver);

			String expectedResults ="";

			//Verify if selected link is history or relationships
			//----------------------------------------------------
			if(dataPool.get("links").equals("History") || dataPool.get("links").equals("Relationships")){

				//Verification : Verify if selected object is navigated to the option
				//-------------------------------------------------------------------
				if(!homePage.listView.getViewCaption().contains(dataPool.get("links")))
					expectedResults = "Created annotation object : " + newAnnotation + " is not displayed with selected : '" + dataPool.get("links") + "' links";

			}//End if
			else {
				MFilesDialog mfilesDialog = new MFilesDialog(driver);//Instantiate the M-files dialog

				//Verify if selected annontated document is displayed with selected dialog
				if(!mfilesDialog.getTitle().contains(dataPool.get("links")))
					expectedResults += "Created annotation object : " + newAnnotation + " is not displayed with selected : '" + dataPool.get("links") + "' links";

			}//End else

			//Verification : Verify if created annotation object is displayed with selected links
			//-----------------------------------------------------------------------------------
			if(expectedResults.equals(""))
				Log.pass("Test Case Passed.Selected annotated object is displayed in : " + dataPool.get("links") + " view.", driver);
			else
				Log.fail("Test Case Failed.Selected annotated object is not displayed in navigated view : " + dataPool.get("links"), driver);

		}//End try
		catch(Exception e) {	
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Annotations_44324

	/**
	 * Annotations_38923 : Verify 'New Annotations' are not visible for views - Common view, Other view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Annotations"}, 
			description = "Verify 'New Annotations' are not visible for views- Common view, Other view.")
	public void Annotations_38923(HashMap<String,String> dataValues, String driverType) throws Exception {


		try {



			driver = WebDriverUtils.getDriver();

			//Pre-requiste : login to MFWA with valid credentials
			//---------------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver,true);//Login to Mfwa with valid credentials

			String expectedResults = "";			

			//Navigate to the history or Relationship view
			//--------------------------------------------
			if(dataPool.get("ObjectToSelect").equalsIgnoreCase("true")){

				//Step-1 : Select the any existing object
				//---------------------------------------
				homePage.searchPanel.clickSearch();//Click the search button

				if(!homePage.listView.clickItemByIndex(0))//Select the random object in the list view
					throw new Exception("No objects are displayed in list view.");

				homePage.menuBar.ClickOperationsMenu(dataPool.get("NavigateToView"));//Click the 'History' option in menubar

				Log.message("1. Selected the object : " + homePage.listView.clickItemByIndex(0) + " and Naviagated to the : " + dataPool.get("NavigateToView") + " view.", driver);

				//Verify if new annotation option is displayed in task pane for common views
				//--------------------------------------------------------------------------
				if(homePage.taskPanel.isItemExists(Caption.Taskpanel.NewAnnotations.Value))
					expectedResults = "Option : " + Caption.Taskpanel.NewAnnotations.Value + " is displayed in task pane.";

			}//End if 

			//Navigate to the common search view
			//----------------------------------
			else{			

				//Step-1 : Navigate to the specified search view
				//----------------------------------------------
				String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"),"");

				Log.message("1. Navigated to '" + viewToNavigate + "' search view.",driver);

				//Step-2 : Selected the preview tab for the selected object
				//---------------------------------------------------------
				homePage.previewPane.clickPreviewTab();//Selected the preview tab for the object

				Log.message("2. Preview tab is selected for the specified object : " + dataPool.get("ObjectName"), driver);

				//Verification : Verify if 'New annotations' option is displayed in task pane
				//---------------------------------------------------------------------------
				if(homePage.taskPanel.isItemExists(Caption.Taskpanel.NewAnnotations.Value))
					expectedResults = "Option : " + Caption.Taskpanel.NewAnnotations.Value + " is displayed in task pane.";

			}//End else

			//Verification : Verify if 'New Annotations' option is displayed in task pane for common views
			//--------------------------------------------------------------------------------------------
			if(expectedResults.equals(""))
				Log.pass("Test Case Passed." + Caption.Taskpanel.NewAnnotations.Value + " is not visible for the common search view : " +  dataPool.get("NavigateToView") ,driver);
			else
				Log.fail("Test Case Failed." + Caption.Taskpanel.NewAnnotations.Value + " is visible for the search view.",driver);

		}//End try
		catch(Exception e) {	
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Annotations_38923

	/**
	 * Annotations_3195 : Verify the Context menu and Operation menu items for selected annotated document
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Annotations"}, 
			description = "Verify the Context menu and Operation menu items for selected annotated document.")
	public void Annotations_3195(HashMap<String,String> dataValues, String driverType) throws Exception {


		try {



			driver = WebDriverUtils.getDriver();

			//Pre-requiste : login to MFWA with valid credentials
			//---------------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver,true);//Login to Mfwa with valid credentials

			//Step-1 : Navigate to the specified search view
			//----------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Selected the specified object from the search view
			//-----------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))//Select the specified object in list view
				throw new Exception("Object : " + dataPool.get("ObjectName") + " is not displayed in search view");

			Log.message("2. Selected the " + dataPool.get("ObjectName") + " from the search view.");

			//Step-3 : Selected the preview tab for the selected object
			//---------------------------------------------------------
			homePage.previewPane.clickPreviewTab();//Selected the preview tab for the object

			Log.message("3. Preview tab is selected for the specified object : " + dataPool.get("ObjectName") , driver);

			//Verify if 'New Annotations' is displayed in task pane
			//-----------------------------------------------------
			if(!homePage.taskPanel.isItemExists(Caption.Taskpanel.NewAnnotations.Value))//Verify if new annotations are displayed in task pane
				throw new Exception("New annotation option is not displayed in task pane.");

			//Step-4 : Click the 'New annotation'  from task pane
			//---------------------------------------------------
			if(!homePage.taskPanel.clickItem(Caption.Taskpanel.NewAnnotations.Value))//Select the 'New Annotation' option in task pane
				throw new Exception("'New annoations' option is not displayed in task pane");

			String annotatedObject =  homePage.listView.getSelectedListViewItem();//Get the selected annotated object from the list view

			Log.message("4. Selected the " + Caption.Taskpanel.NewAnnotations.Value + " option from the task pane.",driver);

			//Verify if 'Save Annotations' is displayed in task pane
			//------------------------------------------------------
			if(!homePage.taskPanel.isItemExists(Caption.Taskpanel.SaveAnnotations.Value))//Verify if new annotations are displayed in task pane
				throw new Exception("New annotated object creation is failed.");// Verify if new annotation object is created or not

			//Step-5 : Select the Save Annotation option in task pane
			//-------------------------------------------------------
			if(!homePage.taskPanel.clickItem(Caption.Taskpanel.SaveAnnotations.Value))//Save the created annotation object
				throw new Exception("'Save Annotations' option is not displayed in task pane.");

			Log.message("5. Created annotation object : " + annotatedObject + " is saved in list view using " + Caption.Taskpanel.SaveAnnotations.Value + " option from the task pane.", driver);

			//Step-6 : Fetch the created annotation object from the list view
			//---------------------------------------------------------------
			String navigateToView = SearchPanel.searchOrNavigatetoView(driver, " ",annotatedObject);//Navigate to the annotated object view

			Log.message("6. Navigated to the " + navigateToView + " annotated object view.",driver);

			//Verify if specified option should be enabled in operations menu
			//---------------------------------------------------------------
			String itemEnabled = "";

			homePage.listView.clickItem(annotatedObject);//Select the annotated object
			String[] operationsMenu = dataPool.get("OperationsMenuOptions").split("\n");//Fetch the options enabled in operations menu

			for(int count = 0; count < operationsMenu.length; count++) {
				if(!homePage.menuBar.IsItemEnabledInOperationsMenu(operationsMenu[count])){//Verify if specified object enabled in operations menu 
					itemEnabled += "Option : " + operationsMenu[count] + " is not displayed in Operations menu.";

				}//End if 
			}//End for

			//Verify if all the expected items are enabled in context menu
			//------------------------------------------------------------
			homePage.listView.rightClickItem(annotatedObject);//Right click the annotated object

			String[] contextMenu = dataPool.get("ContextMenuOptions").split("\n");

			for(int count = 0; count < contextMenu.length; count++) {
				if(!homePage.listView.isItemAvailableinContextmenu(contextMenu[count])) {//Verify if specified object enabled in context menu
					itemEnabled += "Option : " + operationsMenu[count] + " is not displayed in Context menu.";
				}//End if
			}//End for

			//Verification : Verify if all the expected items are enabled in the context/operations menu 
			//------------------------------------------------------------------------------------------
			if(itemEnabled.equals(""))
				Log.pass("Test Case Passed.All the expected items are enabled in Context and Operations menu for the annotated object.", driver);
			else
				Log.fail("Test Case Failed.All the expected items are not enabled in Context and Operations menu for the annotated object." + itemEnabled, driver);

		}//End try
		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Annotations_3195


	/**
	 * Annotations_44171 : Check if annotations can be deleted before checking in the changes
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Annotations"}, 
			description = "Check if annotations can be deleted before checking in the changes.")
	public void Annotations_44171(HashMap<String,String> dataValues, String driverType) throws Exception {


		try {



			driver = WebDriverUtils.getDriver();

			//Pre-requiste : login to MFWA with valid credentials
			//---------------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver,true);//Login to Mfwa with valid credentials

			//Step-1 : Select the new document object from the task pane
			//----------------------------------------------------------
			if(!homePage.taskPanel.clickItem(Caption.ObjecTypes.Document.Value))
				throw new Exception("Object : 'Document' is not displayed in task pane.");

			Log.message("1. Selected the new document object from the task pane." ,driver);

			//Step-2 : Create the new document object from the specified template
			//-------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the pop-out metadatacard
			metadataCard.setTemplateUsingClass(dataPool.get("selectClass"));//Select the template in pop-out metadatacard
			metadataCard = new MetadataCard(driver);//Instantiate the pop-out metadatacard
			metadataCard.setPropertyValue(dataPool.get("propName"), dataPool.get("propValue"));//Set the property name and property value in the pop-out metadatacard
			metadataCard.setCheckInImmediately(true);//Set the check in immediately in pop-out metadatacard
			metadataCard.saveAndClose();//Save the metadatacard
			String newDocumentObject = homePage.listView.getSelectedListViewItem();//Get the selected list view object

			Log.message("2. Created the new document :  " + dataPool.get("propValue") + " metadatacard.");


			//Step-3 : Navigate to search view and select the created document object
			//-----------------------------------------------------------------------
			String navigateToView = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"),newDocumentObject);//Navigated to the specified search view 

			Log.message("3. Navigated to the " + navigateToView + " search view.", driver);

			//Step-4 : Selected the preview tab for the selected object
			//---------------------------------------------------------
			if(!homePage.listView.clickItem(newDocumentObject))//Select the new object in list view
				throw new Exception("Crated annotation object " + newDocumentObject + " is not displayed in the listview.");

			homePage.previewPane.clickPreviewTab();//Selected the preview tab for the object

			Log.message("4. Preview tab is selected for the specified object : " + newDocumentObject , driver);

			//Step-5 : Select the 'New annotation' object from the task pane
			//--------------------------------------------------------------
			if(!homePage.taskPanel.clickItem(Caption.Taskpanel.NewAnnotations.Value))//Select the new annotation object from the task pane
				throw new Exception("New annotation option is not displayed in task pane.");

			String annotatedObject =  homePage.listView.getSelectedListViewItem();//Get the selected annotated object from the list view

			Log.message("5. Selected the New annotation object from the task pane.", driver);

			//Verify if 'Save Annotations' is displayed in task pane
			//------------------------------------------------------
			if(!homePage.taskPanel.isItemExists(Caption.Taskpanel.SaveAnnotations.Value))//Verify if new annotation object is created or not
				throw new Exception("New annotated object creation is failed.");

			//Step-7 : Delete the created annotation object
			//---------------------------------------------
			if(!homePage.listView.clickItem(annotatedObject))//Select the annotated object from the list view
				throw new Exception("Created annotation object is not displayed in list view.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Delete.Value);//Select the delete option from the operations menu

			Log.message("6. Deleted the created annotation object : " + annotatedObject, driver);

			//Verify if 'Confirm Delete' dialog exists when selecting the delete 
			//------------------------------------------------------------------
			if(!MFilesDialog.exists(driver, "Confirm Delete"))//Verify if 'Confirm delete' dialog is exists or not
				throw new Exception("Created annotation object is not deleted while select the Delete option from the Operations menu.");

			//Step-8 : Select the 'ok' button in Confirm delete dialog
			//--------------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver, "Confirm Delete");//Instantiate the Mfiles dialog
			mfilesDialog.clickOkButton();//Click the 'Ok' button in M-files dialog

			Log.message("7. Selected the 'Yes' button in 'Confirm Delete' M-files dialog.", driver);

			//Step-9 : Navigate to the annotated search view
			//----------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, "", annotatedObject);

			Log.message("8. Navigated to the " + viewToNavigate + " search view.", driver);

			//Verification : Verify if annotated object is displayed in list view or not
			//--------------------------------------------------------------------------
			if(!homePage.listView.isItemExists(annotatedObject))//verify if item exist in list view
				Log.pass("Test Case Passed.Created annotation object is deleted successfully before checking the changes of the annotated object.", driver);
			else
				Log.fail("Test Case Failed.Created annotation object is not deleted before checking the changes of the annotated object.", driver);


		}//End try
		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Annotations_44171

	/**
	 * Annotations_19140 : Verify 'New Annotations' are visible for already annotated documents object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Annotations"}, 
			description = "Verify 'New Annotations' are visible for already annotated documents object.")
	public void Annotations_19140(HashMap<String,String> dataValues, String driverType) throws Exception {


		try {



			driver = WebDriverUtils.getDriver();

			//Pre-requiste : login to MFWA with valid credentials
			//---------------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver,true);//Login to Mfwa with valid credentials

			//Step-1 : Select the new document object from the task pane
			//----------------------------------------------------------
			if(!homePage.taskPanel.clickItem(Caption.ObjecTypes.Document.Value))
				throw new Exception("Document object type is not displayed in task pane.");

			Log.message("1. Selected the new document object from the task pane." ,driver);

			//Step-2 : Create the new document object from the specified template
			//-------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the pop-out metadatacard
			metadataCard.setTemplateUsingClass(dataPool.get("selectClass"));//Select the template in pop-out metadatacard
			metadataCard = new MetadataCard(driver);//Instantiate the pop-out metadatacard
			metadataCard.setPropertyValue(dataPool.get("propName"), dataPool.get("propValue"));//Set the property name and property value in the pop-out metadatacard
			metadataCard.setCheckInImmediately(true);//Set the check in immediately in pop-out metadatacard
			metadataCard.saveAndClose();//Save the metadatacard
			String newDocumentObject = homePage.listView.getSelectedListViewItem();//Get the selected list view object

			Log.message("2. Created the new document :  " + dataPool.get("propValue") + " metadatacard.");

			//Step-3 : Navigate to search view and select the created document object
			//-----------------------------------------------------------------------
			String navigateToView = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"),newDocumentObject);//Navigated to the specified search view 

			Log.message("3. Navigated to the " + navigateToView + " search view.", driver);

			//Step-4 : Selected the preview tab for the selected object
			//---------------------------------------------------------
			if(!homePage.listView.clickItem(newDocumentObject))//Select the new object in list view
				throw new Exception("Created document object is not displayed in listview.");

			homePage.previewPane.clickPreviewTab();//Selected the preview tab for the object

			Log.message("4. Preview tab is selected for the specified object : " + newDocumentObject , driver);

			//Step-5 : Select the 'New annotation' object from the task pane
			//--------------------------------------------------------------
			if(!homePage.taskPanel.clickItem(Caption.Taskpanel.NewAnnotations.Value))//Select the new annotation object from the task pane
				throw new Exception("New Annotation option is not displayed in task pane.");

			String annotatedObject =  homePage.listView.getSelectedListViewItem();//Get the selected annotated object from the list view

			Log.message("5. Selected the New annotation object from the task pane.", driver);

			//Step-6 : Select the Save Annotation option in task pane
			//-------------------------------------------------------
			if(!homePage.taskPanel.clickItem(Caption.Taskpanel.SaveAnnotations.Value))//Save the created annotation object
				throw new Exception("Save Annotation option is not displayed in task pane.");

			Log.message("6. Created annotation object : " + annotatedObject + " is saved in list view using " + Caption.Taskpanel.SaveAnnotations.Value + " option from the task pane.", driver);

			//Step-7 : Navigate to created object search view 
			//-----------------------------------------------
			navigateToView = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"),newDocumentObject);//Navigated to the specified search view 				

			if(!homePage.listView.clickItem(newDocumentObject))//Selected the new document object from list view
				throw new Exception("Created new document object is not displayed in list view.");

			Log.message("7. Navigated to the " + navigateToView + " search view.", driver);

			//Step-8 : Click the Preview tab for specified object
			//---------------------------------------------------
			homePage.previewPane.clickPreviewTab();//Click the preview tab

			Log.message("8. Selected the preview tab for selected object : " + newDocumentObject, driver);

			//Verification : Verify if 'New Annotation' option is displayed for the task pane
			//-------------------------------------------------------------------------------
			if(!homePage.taskPanel.isItemExists(Caption.Taskpanel.NewAnnotations.Value))
				Log.pass("Test Case Passed." + Caption.Taskpanel.NewAnnotations.Value + " are not visible for already annotated document object as expected.", driver);
			else
				Log.fail("Test Case Failed." + Caption.Taskpanel.NewAnnotations.Value + " are wrongly visible for the already annotated document object." , driver);

		}//End try
		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Annotations_19140

	/**
	 * Annotations_19141 : Verify 'New Annotations' are visible for already annotated documents object by other user
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Annotations"}, 
			description = "Verify 'New Annotations' are visible for already annotated documents object by other user.")
	public void Annotations_19141(HashMap<String,String> dataValues, String driverType) throws Exception {

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-requiste : login to MFWA with valid credentials
			//---------------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver,true);//Login to Mfwa with valid credentials

			//Step-1 : Navigate to any view
			//-----------------------------
			String navigateToView = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to : " + navigateToView + " search view.", driver);

			//Step-2 : Select the object from the list view
			//---------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))//Select the object from the list view
				throw new Exception("Specified Object is not displayed in list view." + dataPool.get("ObjectName"));

			Log.message("2. Selected the object : " + dataPool.get("ObjectName") + " from the list view.", driver);

			//Step-3 : Click the Preview tab for specified object
			//---------------------------------------------------
			homePage.previewPane.clickPreviewTab();//Click the preview tab

			Log.message("3. Selected the preview tab for selected object : " + dataPool.get("ObjectName"), driver);

			//Step-4 : Select the 'New annotation' object from the task pane
			//--------------------------------------------------------------
			if(!homePage.taskPanel.clickItem(Caption.Taskpanel.NewAnnotations.Value))//Select the new annotation object from the task pane
				throw new Exception("New Annotation option is not displayed in task pane.");

			String annotatedObject =  homePage.listView.getSelectedListViewItem();//Get the selected annotated object from the list view

			Log.message("4. Created the New annotation object : " + annotatedObject + " for selected object : " + dataPool.get("ObjectName") , driver);

			//Step-5 : Select the Save Annotation option in task pane
			//-------------------------------------------------------
			if(!homePage.taskPanel.clickItem(Caption.Taskpanel.SaveAnnotations.Value))//Save the created annotation object
				throw new Exception("Save annotation option is not displayed in task pane.");

			Log.message("5. Created annotation object : " + annotatedObject + " is saved in list view using " + Caption.Taskpanel.SaveAnnotations.Value + " option from the task pane.", driver);

			//Step-6 : Click the logout from menu bar
			//---------------------------------------
			Utility.logOut(driver);//logout from the web access

			Log.message("6. logout from the webaccess from the user " + userName, driver);

			//Step-7 : Login to MFWA with other user
			//--------------------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("Username"), dataPool.get("Password"), testVault);//login to MFWA with valid credentials

			Log.message("7. Logged into MFWA as non-admin user : " + dataPool.get("Username"), driver);

			//Step-8 : Select the new object from the list view
			//-------------------------------------------------
			navigateToView = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))//Select the object from the list view
				throw new Exception("Specified object : " + dataPool.get("ObjectName") + " is not displayed in list view.");

			Log.message("8. Navigated to the specified search view and Selected " + dataPool.get("ObjectName") + " object ", driver);

			//Step-9 : Click the Preview tab for specified object
			//---------------------------------------------------
			homePage.previewPane.clickPreviewTab();//Click the preview tab

			Log.message("9. Clicked the preview tab for selected object : " + dataPool.get("ObjectName") , driver);

			//Verification : Verify if 'New Annotation' option is displayed for the task pane
			//-------------------------------------------------------------------------------
			if(homePage.taskPanel.isItemExists(Caption.Taskpanel.NewAnnotations.Value))
				Log.pass("Test Case Passed." + Caption.Taskpanel.NewAnnotations.Value + " are displayed for already annotated objects  by other user : " + dataPool.get("Username"), driver);
			else
				Log.fail("Test Case Failed." + Caption.Taskpanel.NewAnnotations.Value + " are not displayed other user annotated objects." , driver);

		}//End try
		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Annotations_19141


	/**
	 * Annotations_19143 : Verify to create 'New Annotations' from 'New' menu for Checked out object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Annotations"}, 
			description = "Verify to create 'New Annotations' from 'New' menu for Checked out object.")
	public void Annotations_19143(HashMap<String,String> dataValues, String driverType) throws Exception {


		try {



			driver = WebDriverUtils.getDriver();

			//Pre-requiste : login to MFWA with valid credentials
			//---------------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver,true);//Login to Mfwa with valid credentials

			//Step-1 : Navigate to any view
			//-----------------------------
			String navigateToView = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"),dataPool.get("ObjectName"));

			Log.message("1. Navigated to : " + navigateToView + " search view.", driver);

			//Step-2 : Select the 'Checkout' option from the task pane
			//--------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new Exception("Specified object " + dataPool.get("ObjectName") + " is not displayed in list view.");

			Log.message("2. Selected the object : " + dataPool.get("ObjectName") + " from the list view.", driver);

			//Step-3 : Check out selected object and click the preview tab
			//------------------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.CheckOut.Value);//Click the check out option from the list view
			homePage.previewPane.clickPreviewTab();//Click the preview tab from the metadatatab
			Utils.fluentWait(driver);

			Log.message("3. Selected the 'Preview' tab for the selected checked out object : " +dataPool.get("ObjectName") , driver);

			//Step-4 : Create the new annotation object for selected object
			//-------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.MenuItems.Annotation.Value);//Click the 'New Annotations' option from the task panel.

			Log.message("4. Selected the " + Caption.MenuItems.Annotation.Value + " object for the selected object.", driver);

			//Verify if Mfiles dialog exist
			//-----------------------------
			if(MFilesDialog.exists(driver,"Annotating")){//Verify if M-files dialog exist or not
				MFilesDialog mfilesDialog = new MFilesDialog(driver, "Annotating");//Instantiate the MFiles dialog
				mfilesDialog.clickOkButton();//Click the 'Ok' button in the M-files dialog	
				Utils.fluentWait(driver);
			}//End if				

			//Step-5 : Select the save annotations value in task pane
			//-------------------------------------------------------
			String newAnnotatedObject = homePage.listView.getSelectedListViewItem();//Get the new annotated object name

			if(!homePage.taskPanel.clickItem(Caption.Taskpanel.SaveAnnotations.Value))//Save the created annotation object
				throw new Exception("Save annoation object is not displayed in task pane.");

			Log.message("5. New annotated object : " + newAnnotatedObject + " is created successfully.", driver);

			String[] annotatedObject = newAnnotatedObject.split(" ");
			String annotatedObjectValue = annotatedObject[annotatedObject.length-2];

			//Step-6 : Check in the selected specified object
			//-----------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))//Selected the object from the list view
				throw new Exception("Specified object : " + dataPool.get("ObjectName") + " is not displayed in list view." );

			if(!homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value))//Check in the selected checked out object from task pane
				throw new Exception("CheckIn option is not displayed in task pane.");

			homePage.previewPane.clickMetadataTab();//Click the metadatatab
			MetadataCard metadataCard = new MetadataCard(driver,true);//Instantiate the right pane metadatacard
			int version = metadataCard.getVersion();//Get the selected object version
			String objectVersion = Integer.toString(version);//Converted the integer value into string

			Log.message("6. Check in the selected checked out object : " + dataPool.get("ObjectName") , driver);

			//Verification : Verify if annotated objects are listed in the annotated list view
			//--------------------------------------------------------------------------------
			if(annotatedObjectValue.contains(objectVersion))
				Log.pass("Test Case Passed.New annotation object created successfully for checked out object using 'New annotations' from Menu bar.", driver);
			else
				Log.fail("Test Case Failed.New annotation object is not created successfully for checked out object.", driver);

		}//End try

		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Annotations_19143


	/**
	 * Annotations_42184 : Verify if user can Create copy of annotation inside MFD
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Annotations"}, 
			description = "Verify if user can Create copy of annotation inside MFD.")
	public void Annotations_42184(HashMap<String,String> dataValues, String driverType) throws Exception {

		try{



			driver = WebDriverUtils.getDriver();

			//Pre-requiste : login to MFWA with valid credentials
			//---------------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver,true);//Login to Mfwa with valid credentials

			//Step-1 : Navigate to any view
			//-----------------------------
			String navigateToView = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"),dataPool.get("ObjectName"));

			Log.message("1. Navigated to : " + navigateToView + " search view.", driver);

			//Step-2 : Select the MFD object in list view
			//-------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))//Click the list view object
				throw new Exception("Object : " + dataPool.get("ObjectName") + " is not displayed in list view.");

			homePage.listView.expandItemByName(dataPool.get("ObjectName"));//Expand the MFD object in the list view

			Log.message("2. Selected the MFD object from the list view : " + dataPool.get("ObjectName"), driver);

			//Step-3 : Select the expand item in MFD object
			//---------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName1")))//Click the Selected object in the expanded MFD object
				throw new Exception("Specified object : " + dataPool.get("ObjectName1") + " is not displayed in list view.");

			homePage.previewPane.clickPreviewTab();//Select the preview tab

			Log.message("3. Selected the object : " + dataPool.get("ObjectName1")+ " in expanded MFD object." , driver);

			//Step-4 : Create the new annotation object and save the annotations
			//------------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.MenuItems.Annotation.Value);
			String newAnnotatedObject = homePage.listView.getSelectedListViewItem();//get the selected list view item

			//Verify if save annotation option is displayed in task pane
			//----------------------------------------------------------
			if(!homePage.taskPanel.clickItem(Caption.Taskpanel.SaveAnnotations.Value))
				throw new Exception("Save annotation option is not displayed in task pane.");

			Log.message("4. Created the new annotation " + newAnnotatedObject + " for the selected object : " + dataPool.get("ObjectName1") , driver);

			//Verification : Verify if 'Make copy' option is enable for the selected object
			//-----------------------------------------------------------------------------
			if(!homePage.taskPanel.isItemExists(Caption.MenuItems.MakeCopy.Value))
				Log.pass("Test Case Passed.User cannot create the copy of the selected annotated object : " + newAnnotatedObject, driver);
			else
				Log.fail("Test Case Faild.User can create the copy of the selected object.", driver);

		}//End try

		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Annotations_42184

	/**
	 * Annotations_37862 : Verify if creating new annotation should be possible for all supported file formats
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Annotations"}, 
			description = "Creating new annotation should be possible for all supported file formats.")
	public void Annotations_37862(HashMap<String,String> dataValues, String driverType) throws Exception {

		try{



			driver = WebDriverUtils.getDriver();

			//Pre-requiste : login to MFWA with valid credentials
			//---------------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver,true);//Login to Mfwa with valid credentials

			//Step-1 : Navigate to any view
			//-----------------------------
			String navigateToView = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"),dataPool.get("SearchWord"));

			Log.message("1. Navigated to : " + navigateToView + " search view.", driver);

			//Step-2 : Select the object from the search view
			//-----------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))//Select the object from the list view
				throw new Exception("Specified object : " + dataPool.get("ObjectName") + " is not displayed in list view.");

			Log.message("2. Selected object : " + dataPool.get("ObjectName") + " from the search view.", driver);

			//Step-3 : Create the new annotation from the menu bar
			//----------------------------------------------------
			homePage.previewPane.clickPreviewTab();//Select the preview tab from the metadatacard

			//Step-4 : Select the new annotation from the menu bar
			//----------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.MenuItems.Annotation.Value);//Select the new annotation from the menu bar
			String newAnnotation = homePage.listView.getSelectedListViewItem();//get the new annotation name

			Log.message("3. Created the new annotation : " + newAnnotation + " from menu bar.");

			//Step-4 : Save the created annotated object
			//------------------------------------------
			if(!homePage.taskPanel.clickItem(Caption.Taskpanel.SaveAnnotations.Value))//Save the annotation value from the task pane
				throw new Exception("Save annotation option is not displayed in task pane");

			Log.message("4. Saved the created annotated object : " + newAnnotation, driver);

			//Step-5 : Navigate to the 'Search only : Annotations' view
			//---------------------------------------------------------
			navigateToView = SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchonlyAnnotations.Value, dataPool.get("SearchWord"));

			Log.message("5. Navigated to the : " + Caption.Search.SearchonlyAnnotations.Value + " search view.", driver);

			//Verify if created annotation object is displayed in search view
			//---------------------------------------------------------------
			if(homePage.listView.isItemExists(newAnnotation))//Verify if created annotation object is displayed or not
				Log.pass("Test Case Passed.New annotation object : " + newAnnotation + " is created successfully.", driver);
			else
				Log.fail("Test Case Failed.New annotation object : " + newAnnotation + " is not created successfully.", driver);		

		}//End try	
		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Annotations_37862


	/**
	 * Annotations_42204 : Verify if a document with annotations can be renamed
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Annotations"}, 
			description = "Verify if a document with annotations can be renamed.")
	public void Annotations_42204(HashMap<String,String> dataValues, String driverType) throws Exception {

		try{



			driver = WebDriverUtils.getDriver();

			//Pre-requiste : login to MFWA with valid credentials
			//---------------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver,true);//Login to Mfwa with valid credentials

			//Step-1 : Navigate to any view
			//-----------------------------
			String navigateToView = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"),dataPool.get("ObjectName"));

			Log.message("1. Navigated to : " + navigateToView + " search view.", driver);

			//Step-2 : Select the object from the search view
			//-----------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))//Select the object from the list view
				throw new Exception("Specified object : " + dataPool.get("ObjectName") + " is not displayed in list view.");

			Log.message("2. Selected object : " + dataPool.get("ObjectName") + " from the search view.", driver);

			//Step-3 : Create the new annotation from the menu bar
			//----------------------------------------------------
			homePage.previewPane.clickPreviewTab();//Select the preview tab from the metadatacard

			Log.message("3. Selected the 'Preview' tab from the metadatacard.", driver);

			//Step-4 : Select the new annotation from the menu bar
			//----------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.MenuItems.Annotation.Value);//Select the new annotation from the menu bar
			String newAnnotation = homePage.listView.getSelectedListViewItem();//get the new annotation name

			Log.message("4. Created the new annotation : " + newAnnotation + " from menu bar.");

			//Step-5 : Save the created annotated object
			//------------------------------------------
			if(!homePage.taskPanel.clickItem(Caption.Taskpanel.SaveAnnotations.Value))//Save the annotation value from the task pane
				throw new Exception("Save annotation option is not displayed in task pane.");

			Log.message("5. Saved the created annotated object : " + newAnnotation, driver);

			//Step-6 : Navigate to the 'Search only : Annotations' view
			//---------------------------------------------------------
			navigateToView = SearchPanel.searchOrNavigatetoView(driver,Caption.Search.SearchonlyAnnotations.Value,newAnnotation);

			Log.message("6. Navigated to the : " + navigateToView + " search view." , driver);

			//Step-7 : Select the annotation object in metadatacard tab
			//---------------------------------------------------------
			if(!homePage.listView.clickItem(newAnnotation))//Select the new annotation from the list view
				throw new Exception("Created annotation " + newAnnotation + " object is not displayed in the list view.");

			homePage.previewPane.clickMetadataTab();//Select the metadatatab in preview pane

			Log.message("7. Selected the metadatatab for created annotated object : " + newAnnotation, driver);

			//Step-8 : Rename the target object for the selected annotated object
			//-------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver,true);//Instantiate the right pane metadatacard
			metadataCard.setPropertyValue(dataPool.get("PropName"),dataPool.get("PropValue"));//Set the property name & value is changed in metadatacard
			metadataCard.saveAndClose();//Save the right pane metadatacard

			Log.message("8. Changed the property value for selected object : " + dataPool.get("PropName"), driver);

			//Verification : Verify if annotation object is displayed after rename tha annotation object
			//------------------------------------------------------------------------------------------
			homePage.previewPane.clickPreviewTab();//Selected the preview tab in right pane
			if(homePage.previewPane.isPreviewTabObjectNotDisplayed())//Verify if selected object is not displayed in preview tab
				Log.pass("Test Case Passed.Selected annotation object " + newAnnotation + " is not displayed in preview pane after renamed the target object : " + dataPool.get("PropName"), driver);
			else
				Log.fail("Test Case Failed.Selected annotation object " + newAnnotation + " is wrongly displayed in preview pane after renamed the target object : " + dataPool.get("PropName"), driver);

		}//End try
		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Annotations_42204


	/**
	 * Annotations_38894 : Verify Remove from favorites for selected annotated document
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Annotations"}, 
			description = "Verify Remove from favorites for selected annotated document.")
	public void Annotations_38894(HashMap<String,String> dataValues, String driverType) throws Exception {

		try{



			driver = WebDriverUtils.getDriver();

			//Pre-requiste : login to MFWA with valid credentials
			//---------------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver,true);//Login to Mfwa with valid credentials

			//Step-1 : Navigate to any view
			//-----------------------------
			String navigateToView = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"),dataPool.get("ObjectName"));

			Log.message("1. Navigated to : " + navigateToView + " search view.", driver);

			//Step-2 : Select the object from the search view
			//-----------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))//Select the object from the list view
				throw new Exception("Specified object : " + dataPool.get("ObjectName") + " is not displayed in list view.");

			Log.message("2. Selected object : " + dataPool.get("ObjectName") + " from the search view.", driver);

			//Step-3 : Create the new annotation from the menu bar
			//----------------------------------------------------
			homePage.previewPane.clickPreviewTab();//Select the preview tab from the metadatacard

			Log.message("3. Selected the 'Preview' tab from the metadatacard.", driver);

			//Step-4 : Select the new annotation from the menu bar
			//----------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.MenuItems.Annotation.Value);//Select the new annotation from the menu bar
			String newAnnotation = homePage.listView.getSelectedListViewItem();//get the new annotation name

			Log.message("4. Created the new annotation : " + newAnnotation + " from menu bar.");

			//Step-5 : Save the created annotated object
			//------------------------------------------
			if(!homePage.taskPanel.clickItem(Caption.Taskpanel.SaveAnnotations.Value))//Save the annotation value from the task pane
				throw new Exception("Save annotation option is not displayed in task pane.");

			Log.message("5. Saved the created annotated object : " + newAnnotation, driver);

			//Step-6 : Navigate to the search only: annotations view
			//------------------------------------------------------
			navigateToView = SearchPanel.searchOrNavigatetoView(driver,Caption.Search.SearchonlyAnnotations.Value," ");//Navigate to the search only annotations view

			Log.message("6. Navigated to the : " + navigateToView + " search view." , driver);

			//Step-7 : Set the annotation object as favorites and navigate to the favorites view
			//----------------------------------------------------------------------------------
			if(!homePage.listView.clickItem(newAnnotation))//Select the new annotation object in search view
				throw new Exception("Created annotation : " + newAnnotation + " object is not displayed in the list view.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.AddToFavorites.Value);//Add the favorites value to the object

			MFilesDialog mfilesdialog = new MFilesDialog(driver);//Instantiate the M-files dialog
			if(mfilesdialog.getMessage().contains("One object was affected."))//Verify if "One object was affected" message is displayed
				mfilesdialog.clickOkButton();//Select the ok button in mfiles dialog

			Log.message("7. Selected annotation object : " + newAnnotation + " is added to the favorites." , driver);

			//Step-8 : Navigate to the favorites view
			//---------------------------------------
			if(!homePage.taskPanel.clickItem(Caption.MenuItems.Favorites.Value))//Navigate to the Favorites view
				throw new Exception("'Favorites' option is not displayed in task pane.");

			Log.message("8. Navigated to the : " + Caption.MenuItems.Favorites.Value + " view.", driver);

			//Step-9 : Select the annotation object in the favorites view
			//-----------------------------------------------------------
			if(!homePage.listView.clickItem(newAnnotation))//Select the created annotation object in favorites view
				throw new Exception("Created annotation object : " + newAnnotation + " is not displayed in list view.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.RemoveFromFavorites.Value);//Select the "Remove from favorites" opetion from the operation menu

			String ExpectedResults = "";

			//Verify if one object is selected is displayed or not 
			//----------------------------------------------------
			if(!MFilesDialog.exists(driver))//Verify if Mfiles dialog is exists or not
				throw new Exception("Confirmation remove from favorites dialog is not displayed");

			mfilesdialog = new MFilesDialog(driver,"M-files");//Instantiate the M-files dialog
			if(!mfilesdialog.getMessage().equals(dataPool.get("ExpectedMessage")))
				ExpectedResults += "Expected M-files dialog is not displayed with expected message. Message : " + mfilesdialog.getMessage();

			mfilesdialog.clickCancelButton();//Click the 'No' button in confirm remove from favorites dialog

			//Verify if new annotation object is listed in favorites view after selecting the 'No' button in favorites dialog
			//---------------------------------------------------------------------------------------------------------------
			if(!homePage.listView.isItemExists(newAnnotation))//Verify if annotation object is displayed in favorites view
				ExpectedResults += "Selected annotation object " + newAnnotation + " is removed from the favorites view after selecting 'No' in favorites dialog.";

			//Step-10 : Select the 'Yes' in Confirmation dialog for remove from favorites
			//---------------------------------------------------------------------------
			if(!homePage.listView.rightClickItem(newAnnotation))//Right click the new annotation object in favorites view
				throw new Exception("Specified annotation object : " + newAnnotation + " is not displayed in list view.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.RemoveFromFavorites.Value);//Remove from teh favorites view

			mfilesdialog = new MFilesDialog(driver);//Instantiate the M-files dialog
			mfilesdialog.clickOkButton();//Click the 'Yes' button in M-files Favorites dialog

			//Verify if "One object was affected" message is displayed after selecting the 'Yes' in favorites view
			//----------------------------------------------------------------------------------------------------
			mfilesdialog = new MFilesDialog(driver);//Instantiate the M-files dialog
			if(mfilesdialog.getMessage().contains("One object was affected."))//Verify if actual favorites 
				mfilesdialog.clickOkButton();//Select the ok button in mfiles dialog

			Log.message("10. Selected the annotation object : " + newAnnotation + " remove the object from favorites view.", driver);

			//Verify if new annotation object is listed in favorites view 
			//-----------------------------------------------------------
			if(homePage.listView.isItemExists(newAnnotation))
				ExpectedResults += "Selected annotation object is not removed from the favorites view after selecting the 'Yes' in confirmation favorites dialog.";

			//Verification : Verify if Remove from favorites operation for selected annotated document
			//----------------------------------------------------------------------------------------
			if(ExpectedResults.equals(""))
				Log.pass("Test Case Passed.Selected annotation object " + newAnnotation + " is removed from the favorites view successfully." , driver);
			else
				Log.fail("Test Case Failed.Selected annotation object" + newAnnotation + " is not removed from the favorites view." + ExpectedResults, driver);		

		}//End try
		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Annotations_38894

	/**
	 * Annotations_44124 : Check Undo Checkout to annotation
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Annotations"}, 
			description = "Check Undo Checkout to annotation.")
	public void Annotations_44124(HashMap<String,String> dataValues, String driverType) throws Exception {

		try{



			driver = WebDriverUtils.getDriver();

			//Pre-requiste : login to MFWA with valid credentials
			//---------------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver,true);//Login to Mfwa with valid credentials

			//Step-1 : Navigate to any view
			//-----------------------------
			String navigateToView = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"),dataPool.get("ObjectName"));

			Log.message("1. Navigated to : " + navigateToView + " search view.", driver);

			//Step-2 : Select the object from the search view
			//-----------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))//Select the object from the list view
				throw new Exception("Specified object : " + dataPool.get("ObjectName") + " is not displayed in list view.");

			Log.message("2. Selected object : " + dataPool.get("ObjectName") + " from the search view.", driver);

			//Step-3 : Create the new annotation from the menu bar
			//----------------------------------------------------
			homePage.previewPane.clickPreviewTab();//Select the preview tab from the metadatacard

			Log.message("3. Selected the 'Preview' tab from the metadatacard.", driver);

			//Step-4 : Select the new annotation from the menu bar
			//----------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.MenuItems.Annotation.Value);//Select the new annotation from the menu bar
			String newAnnotation = homePage.listView.getSelectedListViewItem();//get the new annotation name

			Log.message("4. Created the new annotation : " + newAnnotation + " from menu bar.");

			//Step-5 : Save the created annotated object
			//------------------------------------------
			if(!homePage.taskPanel.clickItem(Caption.Taskpanel.SaveAnnotations.Value))//Save the annotation value from the task pane
				throw new Exception("Save Annotation option is not displayed in task pane.");

			Log.message("5. Saved the created annotated object : " + newAnnotation, driver);

			//Step-6 : Navigate to the search only: annotations view
			//------------------------------------------------------
			navigateToView = SearchPanel.searchOrNavigatetoView(driver,Caption.Search.SearchonlyAnnotations.Value,newAnnotation);//Navigate to the search only annotations view

			if(!homePage.listView.clickItem(newAnnotation))//Select the new annotation in search view
				throw new Exception("Created annotation object : " + newAnnotation + " is not displayed in list view.");

			homePage.previewPane.clickMetadataTab();//Select the metadatacard in right pane

			Log.message("6. Navigated to the : " + navigateToView + " search view and select the metadatacard." , driver);

			//Step-7 : Check out the annotated object and add new property to the object
			//--------------------------------------------------------------------------
			if(!homePage.listView.rightClickItem(newAnnotation))//Right click the new annotation object
				throw new Exception("Created annotation object : " + newAnnotation + " is not displayed in list view.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.CheckOut.Value);//Check out the annotation object

			Log.message("7. Checked out the created annotation object : " + newAnnotation);

			//Step-8 : Add the new property in annotation object metadatacard
			//---------------------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver,true);//Instantiate the right pane metadatacard
			metadatacard.addNewProperty("Customer");//Add the new property in annotation metadatacard
			metadatacard.saveAndClose();//Save the metadatacard

			Log.message("8. Added the new 'Customer' property and Saved the annotation object metadatacard : "+ newAnnotation, driver);

			//Step-9 : Undo checkout the selected annotated object
			//----------------------------------------------------
			if(!homePage.listView.clickItem(newAnnotation))//Select the new annotation in search view
				throw new Exception("Created annotation object : " + newAnnotation + " is not displayed in list view.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.UndoCheckOut.Value);//Undo Check out the annotation object

			//Verify if M-files dialog is exist or not
			//----------------------------------------
			if(!MFilesDialog.exists(driver,"Confirm Undo Checkout"))
				throw new Exception("Confirm undo check out dialog is not displayed for the checkout object.");

			//Instantiate the M-files dialog
			//-----------------------------
			MFilesDialog mfilesdialog = new MFilesDialog(driver, "Confirm Undo Checkout");//Instantiate the M-files dialog
			mfilesdialog.clickOkButton();//Select the yes button in "Confirm undo checkout" dialog

			Log.message("9. Undo checked out the '" + newAnnotation + "' created annotation object.", driver);

			//Verification : Verify if added property is not displayed in selected annotation object
			//--------------------------------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true);//Instantiate the right pane metadatacard
			if(!metadatacard.propertyExists("Customer"))//Verify if property exists in the right pane metadatacard
				Log.pass("Test Case Passed.Undo checkout is working as expected.Added property \" Customer \" is not displayed in Selected annotation object : " + newAnnotation , driver);
			else
				Log.fail("Test Case Failed.Undo checkout is not working as expected.Added property \" Customer \" is displayed in selected annotation object.", driver);		

		}//End try		
		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Annotations_44124



	/**
	 * Annotations_42158 : Check the Annotations user rights when set to 'Only for me'"
	 */
	@Test(dataProviderClass = DataProviderUtils.class,dataProvider = "excelDataReader",groups = {"Annotations"}, 
			description = "Check the Annotations user rights when set to 'Only for me'.")
	public void Annotations_42158 (HashMap<String,String> dataValues, String driverType) throws Exception {

		try{		



			driver = WebDriverUtils.getDriver();

			//Pre-requiste : login to MFWA with valid credentials
			//---------------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap<String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver,true);//Login to Mfwa with valid credentials

			//Step-1 : Navigate to any view
			//-----------------------------
			String navigateToView = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"),dataPool.get("ObjectName"));

			Log.message("1. Navigated to : " + navigateToView + " search view.",driver);

			//Step-2 : Select the object from the search view
			//-----------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))//Select the object from the list view
				throw new Exception("Specified object : " + dataPool.get("ObjectName") + " is not displayed in task pane.");

			Log.message("2. Selected object : "+ dataPool.get("ObjectName") + "from the search view.", driver);

			//Step-3 : Create the new annotation from the menu bar
			//---------------------------------------------------
			homePage.previewPane.clickPreviewTab();//Select the preview tab from the metadatacard

			Log.message("3. Selected the 'Preview' tab from the metadatacard.", driver);

			//Step-4 : Select the new annotation from the menu bar
			//----------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.MenuItems.Annotation.Value);//Select the new annotation from the menu bar
			String newAnnotation = homePage.listView.getSelectedListViewItem();//get the new annotation name

			Log.message("4. Created the new annotation : " + newAnnotation + "from menu bar.");

			//Step-5 : Set the Permission to the annoation object
			//---------------------------------------------------
			homePage.previewPane.clickMetadataTab();//Click the metadata tab in right pane
			MetadataCard metadatacard = new MetadataCard(driver,true);//Instantiate the right pane metadatacard
			metadatacard.setPermission(dataPool.get("Permission"));//Set the permission to the object
			metadatacard.saveAndClose();//Save the metadatacard

			Log.message("5. Set the Permission : " + dataPool.get("Permission") + " to the annotated object : " + newAnnotation, driver);

			//Step-6 : logout from the MFWA and login as another user
			//-------------------------------------------------------
			Utility.logOut(driver);//logout from the MFWA
			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("Username"), dataPool.get("Password"), testVault);//login to MFWA with valid credentials
			navigateToView = SearchPanel.searchOrNavigatetoView(driver, Caption.Search.SearchonlyAnnotations.Value, newAnnotation);//Navigate to the search view

			Log.message("6. Navigated to the '"+ navigateToView + "' search view ",driver);

			String ExpectedResults = "";//Set the expected results

			//Verify if specified object is displayed with the annotation object 
			//------------------------------------------------------------------
			if(homePage.listView.isItemExists(newAnnotation))
				ExpectedResults += "Created annotation object with  permission 'Only for me' displayed for another user is not working as expected.";

			//Step-8 : logout from the second user and login with first user
			//-----------------------------------------------------
			Utility.logOut(driver);//logout from the webaccess
			homePage = LoginPage.launchDriverAndLogin(driver, true);//Login to Mfwa with valid credentials

			Log.message("8. Logout from the second user : " + dataPool.get("Username") + " and login as " + userName , driver);

			//Step-9 : Navigate to search view and select the object in the search view
			//-------------------------------------------------------------------------
			navigateToView = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"),dataPool.get("ObjectName"));

			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))//Select the specified object name
				throw new Exception("Specified object : " + dataPool.get("ObjectName") + " is not displayed in list view.");

			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Relationships.Value);

			Log.message("9. Selected the specied object from the list view : " + dataPool.get("ObjectName") + " and navigated to the relationships view of that object.");

			//Step-10 : Save the created annotation object
			//--------------------------------------------
			if(!homePage.listView.clickItem(newAnnotation))//Select the created annotation object
				throw new Exception("Created annotation object : " + newAnnotation + " is not visible to the user : " + userName );

			if(!homePage.taskPanel.clickItem(Caption.Taskpanel.SaveAnnotations.Value))//Save the annotation value
				throw new Exception("'Save Annotation' option is not displayed for specified annotation object : " + newAnnotation);

			Log.message("10. Created annotation object : " + newAnnotation + " is saved using  " + Caption.Taskpanel.SaveAnnotations.Value + " option from the taskpanel." , driver);

			//Step-11 : Login with other user and verify if annotation object is displayed after save the annotation
			//------------------------------------------------------------------------------------------------------
			Utility.logOut(driver);//logout from the web access
			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("Username"),dataPool.get("Password"),testVault);//login to MFWA with valid credentials
			navigateToView = SearchPanel.searchOrNavigatetoView(driver,Caption.Search.SearchonlyAnnotations.Value ,newAnnotation);//Navigate to the search view

			Log.message("11. logout from the user : " + userName +  " login as second user :  " + dataPool.get("Username") + " & navigate to search view : " + navigateToView , driver);

			//Verify if specified object is displayed with the annotation object 
			//------------------------------------------------------------------
			if(homePage.listView.isItemExists(newAnnotation))
				ExpectedResults += "Created annotation object with  permission 'Only for me' displayed for another user is not working as expected.";

			//Verification : Verify if annotation object is not displayed after set the Permission with 'Only for me'
			//-------------------------------------------------------------------------------------------------------
			if(ExpectedResults.equals(""))
				Log.pass("Test Case Passed.Annotation object is not displayed as expected for the other user when set the 'Only for me' permission as expected.", driver);
			else
				Log.fail("Test Case Failed.Annotation obejct is displayed for the other user wrongly when set the 'Only for me' permission" + ExpectedResults, driver);

		}//End try		
		catch(Exception e)
		{
			Log.exception(e,driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Annotations_42158

	/**
	 * Annotations_44131 : "Check the Annotations user rights when set to ''full control of internal users'"
	 */
	@Test(dataProviderClass = DataProviderUtils.class,dataProvider = "excelDataReader",groups = {"Annotations"}, 
			description = "Check the Annotations user rights when set to 'full control of internal users'.")
	public void Annotations_44131 (HashMap<String,String> dataValues, String driverType) throws Exception {

		try{		



			driver = WebDriverUtils.getDriver();

			//Pre-requiste : login to MFWA with valid credentials
			//---------------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap<String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver,true);//Login to Mfwa with valid credentials

			//Step-1 : Navigate to any view
			//-----------------------------
			String navigateToView = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"),dataPool.get("ObjectName"));

			Log.message("1. Navigated to : " + navigateToView + " search view.",driver);

			//Step-2 : Select the object from the search view
			//-----------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))//Select the object from the list view
				throw new Exception("Specified object : " + dataPool.get("ObjectName") + " is not displayed in list view.");

			Log.message("2. Selected object : "+ dataPool.get("ObjectName") + "from the search view.", driver);

			//Step-3 : Create the new annotation from the menu bar
			//---------------------------------------------------
			homePage.previewPane.clickPreviewTab();//Select the preview tab from the metadatacard

			Log.message("3. Selected the 'Preview' tab from the metadatacard.", driver);

			//Step-4 : Select the new annotation from the menu bar
			//----------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.MenuItems.Annotation.Value);//Select the new annotation from the menu bar
			String newAnnotation = homePage.listView.getSelectedListViewItem();//get the new annotation name

			if(!homePage.taskPanel.clickItem(Caption.MenuItems.CheckIn.Value))//Check in the selected checked out object from task pane
				throw new Exception("CheckIn option is not displayed in task pane.");

			Log.message("4. Created the new annotation : " + newAnnotation + "from menu bar.", driver);

			//Step-5 : Set the Permission to the annoation object
			//---------------------------------------------------
			homePage.previewPane.clickMetadataTab();//Click the metadata tab in right pane
			MetadataCard metadatacard = new MetadataCard(driver,true);//Instantiate the right pane metadatacard
			if(!metadatacard.getPermission().equalsIgnoreCase(dataPool.get("Permission")))
			{
				metadatacard.setPermission(dataPool.get("Permission"));//Set the permission to the object
				metadatacard.saveAndClose();//Save the metadatacard
				Log.message("5. Set the Permission : " + dataPool.get("Permission") + " to the annotated object : " + newAnnotation, driver);
			}
			else
				Log.message("5. Annotated object :'" + newAnnotation + "' is created with Permission : " + dataPool.get("Permission"), driver);

			//Step-6 : logout from the MFWA and login as another user
			//-------------------------------------------------------
			Utility.logOut(driver);//logout from the MFWA
			homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("Username"),dataPool.get("Password"),testVault);//login to MFWA with valid credentials
			navigateToView = SearchPanel.searchOrNavigatetoView(driver,Caption.Search.SearchonlyAnnotations.Value,newAnnotation);//Navigate to the search view

			Log.message("6. Navigated to the '"+ navigateToView + "' search view ",driver);

			//Verification : Verify if created annotation document is displayed for the other user
			//------------------------------------------------------------------------------------
			if(homePage.listView.isItemExists(newAnnotation))
				Log.pass("Test Case Passed.Annotation object is displayed as expected for the other user when set the " + dataPool.get("Permission") + " permission as expected.", driver);
			else
				Log.fail("Test Case Failed.Annotation obejct is not displayed for the other user when set the " + dataPool.get("Permission") + " permission", driver);

		}//End try		
		catch(Exception e)
		{
			Log.exception(e,driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Annotations_44131


	/**
	 * Annotations_41993 : Check the save popup while navigating from annotation documents to task pane option
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Annotations"}, 
			description = "Check the save popup while navigating from annotation documents to task pane option.")
	public void Annotations_41993(HashMap<String,String> dataValues, String driverType) throws Exception {

		try{



			driver = WebDriverUtils.getDriver();

			//Pre-requiste : login to MFWA with valid credentials
			//---------------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver,true);//Login to Mfwa with valid credentials

			//Step-1 : Navigate to any view
			//-----------------------------
			String navigateToView = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"),dataPool.get("ObjectName"));

			Log.message("1. Navigated to : " + navigateToView + " search view.", driver);

			//Step-2 : Select the object from the search view
			//-----------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))//Select the object from the list view
				throw new Exception("Specified object : " + dataPool.get("ObjectName") + " is not displayed in list view.");

			Log.message("2. Selected object : " + dataPool.get("ObjectName") + " from the search view.", driver);

			//Step-3 : Create the new annotation from the menu bar
			//----------------------------------------------------
			homePage.previewPane.clickPreviewTab();//Select the preview tab from the metadatacard

			Log.message("3. Selected the 'Preview' tab from the metadatacard.", driver);

			//Step-4 : Select the new annotation from the menu bar
			//----------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.MenuItems.Annotation.Value);//Select the new annotation from the menu bar
			String newAnnotation = homePage.listView.getSelectedListViewItem();//get the new annotation name

			Log.message("4. Created the new annotation : " + newAnnotation + " from menu bar.");

			//Step-5 : Save & Edit the created annotated object
			//-------------------------------------------------
			if(!homePage.taskPanel.clickItem(Caption.Taskpanel.SaveAnnotations.Value))//Save the annotation value from the task pane
				throw new Exception("'Save Annotation' option is not displayed in task pane.");

			if(!homePage.taskPanel.clickItem(Caption.Taskpanel.EditAnnotations.Value))//Edit the created annotation value from the task pane
				throw new Exception("'Edit Annotation' option is not displayed in task pane.");

			Log.message("5. Saved & Edited the created annotated object : " + newAnnotation, driver);

			//Step-6 : Navigate to specified view
			//-----------------------------------
			if(!homePage.taskPanel.clickItem(Caption.MenuItems.Favorites.Value))//Click the favorites link from the task pane
				throw new Exception("'Favorites' option is not displayed in task pane.");

			Log.message("6. Navigated to the 'Favorites' view.",driver);

			String expectedResults = "";

			//Verify if navigated to specified view
			//-------------------------------------
			if(!homePage.menuBar.GetBreadCrumbItem().equals(testVault+">" + Caption.MenuItems.Favorites.Value))
				expectedResults = "Navigate to \" Favorites \" view is not performed successfully.";

			//Verify if M-files dialog is displayed
			//-------------------------------------
			if(MFilesDialog.exists(driver))
				expectedResults = "Unexpected M-files dialog is displayed while navigate to Favorites view.";

			//Verification : Verify if no pop-up is displayed when navigated to the specified view
			//------------------------------------------------------------------------------------
			if(expectedResults.equals(""))
				Log.pass("Test Case Passed.No pop-up dialog is displayed when user navigated to : " + Caption.MenuItems.Favorites.Value + " view without saving the annoations.", driver);
			else
				Log.fail("Test Case Failed.Pop-up dialog is displayed while navigate to : " + Caption.MenuItems.Favorites.Value + " view." + expectedResults, driver);

		}//End try
		catch(Exception e)
		{
			Log.exception(e,driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Annotations_41993


	/**
	 * Annotations_42297 : Check if annotations can be created for non-file objects, e.g. assignment
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Annotations"}, 
			description = "Check if annotations can be created for non-file objects, e.g. assignment.")
	public void Annotations_42297(HashMap<String,String> dataValues, String driverType) throws Exception {

		try{



			driver = WebDriverUtils.getDriver();

			//Pre-requiste : login to MFWA with valid credentials
			//---------------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver,true);//Login to Mfwa with valid credentials

			//Step-1 : Create the new assignment object
			//-----------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Assignment.Value);//Select the new assignment object from menu bar
			String assigName =  Utility.getObjectName(methodName).toString(); //Name of the object with current method date & time
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the assignment metadatacard
			metadataCard.setPropertyValue("Name or title",assigName);//Set the property values for Name or title
			metadataCard.setPropertyValue("Assigned to",userFullName);//Set the property values for assigned to property
			metadataCard.saveAndClose();//Save the metadatacard

			Log.message("1. Created the new assignment object : " + assigName, driver);

			//Step-2 : Navigate to the any search view
			//----------------------------------------
			String navigateToView = SearchPanel.searchOrNavigatetoView(driver,Caption.Search.SearchOnlyAssignments.Value,assigName);

			Log.message("2. Navigated to the : " + navigateToView + " view.");

			//Step-3 : Select the created assignment object 
			//---------------------------------------------
			if(!homePage.listView.clickItem(assigName))//Select the created assignment object
				throw new Exception("Specified object : " + assigName + " is not displayed in list view.");

			homePage.menuBar.clickNewMenuItem(Caption.MenuItems.Annotation.Value);//Select the new annotation object from the menu bar

			Log.message("3. Selected the new annotation object from the menu bar.", driver);

			//Verify if M-files dialog is displayed or not
			//--------------------------------------------
			if(!MFilesDialog.exists(driver))
				throw new Exception("Expected Warning M-files dialog is not displayed when creating annotation for Assignment object.");

			//Verification : Verify if annotation is not created for assignment object
			//------------------------------------------------------------------------
			MFilesDialog mfilesDialog = new MFilesDialog(driver, "M-Files Web");//Instantiate the mfiles dialog
			if(mfilesDialog.getMessage().equals(dataPool.get("ExpectedMessage")))
				Log.pass("Test Case Passed.Annotations object is not created for non-file 'Assignment' object as expected.", driver);
			else
				Log.fail("Test Case Failed.Annotations is wrongly created for the non-file 'Assignment' object.", driver);

		}//End try
		catch(Exception e)
		{
			Log.exception(e,driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Annotations_42297

	/**
	 * Annotations_3202 : Verify name of the annotated document
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Annotations"}, 
			description = "Verify name of the annotated document.")
	public void Annotations_3202(HashMap<String,String> dataValues, String driverType) throws Exception {

		try{



			driver = WebDriverUtils.getDriver();

			//Pre-requiste : login to MFWA with valid credentials
			//---------------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver,true);//Login to Mfwa with valid credentials

			//Step-1 : Navigate to any view
			//-----------------------------
			String navigateToView = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"),dataPool.get("ObjectName"));

			Log.message("1. Navigated to : " + navigateToView + " search view.", driver);

			//Step-2 : Select the object from the search view
			//-----------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectName"));//Select the object from the list view
			MetadataCard metadataCard = new MetadataCard(driver,true);//Instantiate the right pane metadatacard
			int version = metadataCard.getVersion();//Get the selected object version
			String objectVersion = Integer.toString(version);//Converted the integer value into string

			Log.message("2. Selected object : " + dataPool.get("ObjectName") + " from the search view.", driver);

			//Step-3 : Create the new annotation from the menu bar
			//----------------------------------------------------
			homePage.previewPane.clickPreviewTab();//Select the preview tab from the metadatacard

			Log.message("3. Selected the 'Preview' tab from the metadatacard.", driver);

			//Step-4 : Select the new annotation from the menu bar
			//----------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.MenuItems.Annotation.Value);//Select the new annotation from the menu bar
			String newAnnotation = homePage.listView.getSelectedListViewItem();//get the new annotation name

			Log.message("4. Created the new annotation : " + newAnnotation + " from menu bar.");

			String expectedAnnotationName = "Annotations for " +  dataPool.get("ObjectName") + " v"+ objectVersion + " (" + userName + ")" ;

			//Verification : Verify if new annotation object name created as expected
			//-----------------------------------------------------------------------
			if(expectedAnnotationName.equals(newAnnotation))//Verify if annotation name object is created as expected
				Log.pass("Test Case Passed.Annotation object is name set as expected. Annotation object : " + newAnnotation , driver);
			else
				Log.fail("Test Case Failed.Annotation object is not set as expected.", driver);

		}//End try
		catch(Exception e)
		{
			Log.exception(e,driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Annotations_42297

	/**
	 * Annotations_43027 : Check options present for annotated object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Annotations"}, 
			description = "Check options present for annotated object.")
	public void Annotations_43027(HashMap<String,String> dataValues, String driverType) throws Exception {


		try {



			driver = WebDriverUtils.getDriver();

			//Pre-requiste : login to MFWA with valid credentials
			//---------------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver,true);//Login to Mfwa with valid credentials

			//Step-1 : Navigate to any view
			//-----------------------------
			String navigateToView = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"),dataPool.get("ObjectName"));

			Log.message("1. Navigated to : " + navigateToView + " search view.", driver);

			//Step-2 : Select the object from the list view
			//---------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectName"));//Select the object from the list view

			Log.message("2. Selected the object : " + dataPool.get("ObjectName") + " from the list view.", driver);

			//Step-3 : Click the Preview tab for specified object
			//---------------------------------------------------
			homePage.previewPane.clickPreviewTab();//Click the preview tab

			Log.message("3. Selected the preview tab for selected object : " + dataPool.get("ObjectName")   , driver);

			//Verify if 'New Annotations' is displayed in task pane
			//-----------------------------------------------------
			if(!homePage.taskPanel.isItemExists(Caption.Taskpanel.NewAnnotations.Value))//Verify if new annotations are displayed in task pane
				throw new Exception("New annotation option is not displayed in task pane.");

			//Step-4 : Select the 'New annotation' object from the task pane
			//--------------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.Taskpanel.NewAnnotations.Value);//Select the new annotation object from the task pane
			String annotatedObject =  homePage.listView.getSelectedListViewItem();//Get the selected annotated object from the list view

			Log.message("4. Created the New annotation object : " + annotatedObject + " for selected object : " + dataPool.get("ObjectName") , driver);

			//Verify if 'New Annotations' is displayed in task pane
			//-----------------------------------------------------
			if(homePage.taskPanel.isItemExists(Caption.Taskpanel.NewAnnotations.Value))//Verify if new annotations are displayed in task pane
				throw new Exception("New annotation option is not displayed in task pane.");

			//Step-5 : Select the Save Annotation option in task pane
			//-------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.Taskpanel.SaveAnnotations.Value);//Save the created annotation object

			Log.message("5. Created annotation object : " + annotatedObject + " is saved in list view using " + Caption.Taskpanel.SaveAnnotations.Value + " option from the task pane.", driver);

			//Step-6 : Select the parent document object for the created annoatated object
			//----------------------------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new Exception("Specified object : " + dataPool.get("ObjectName") + " is not displayed in list view.");

			Log.message("6. Selected the parent object : " + dataPool.get("ObjectName") + " for the created annotated object : " + annotatedObject , driver);

			String expectedResults = "";

			//Verify if 'hide annotation' option is displayed in task pane
			//------------------------------------------------------------
			if(!homePage.taskPanel.isItemExists(Caption.Taskpanel.HideAnnotations.Value))
				expectedResults = "Hide annotation is not displayed in task pane.";

			//Verify if 'Edit annotation' option is displayed in task pane
			//------------------------------------------------------------
			if(!homePage.taskPanel.isItemExists(Caption.Taskpanel.EditAnnotations.Value))
				expectedResults +=  "Edit annotaion is not displaye in task pane.";

			//Verification :  Verify if expected annotation option is displayed for the parent object
			//---------------------------------------------------------------------------------------
			if(expectedResults.equals(""))//Verify if annotation oprtion is displayed as expected
				Log.pass("Test Case Passed.Selected parent object : " + dataPool.get("ObjectName") + " is displayed with expected options : " + Caption.Taskpanel.EditAnnotations.Value + " & " + Caption.Taskpanel.HideAnnotations.Value );
			else
				Log.fail("Test Case Failed.Selected parent object : " +  dataPool.get("ObjectName") + " is not displayed with the expected annotation option " + Caption.Taskpanel.EditAnnotations.Value + " & " + Caption.Taskpanel.HideAnnotations.Value, driver);

		}//End try
		catch(Exception e)
		{
			Log.exception(e,driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End Annotations_43027

	/**
	 * Annotations_43031 : Validate functionality of Hide Annotation
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Annotations"}, 
			description = "Validate functionality of Hide Annotation.")
	public void Annotations_43031(HashMap<String,String> dataValues, String driverType) throws Exception {

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-requiste : login to MFWA with valid credentials
			//---------------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver,true);//Login to Mfwa with valid credentials

			//Step-1 : Navigate to any view
			//-----------------------------
			String navigateToView = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to : " + navigateToView + " search view.", driver);

			//Step-2 : Select the object from the list view
			//---------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))//Select the object from the list view
				throw new Exception("Specified object : " + dataPool.get("ObjectName") + " is not displayed in the list view.");

			Log.message("2. Selected the object : " + dataPool.get("ObjectName") + " from the list view.", driver);

			//Step-3 : Click the Preview tab for specified object
			//---------------------------------------------------
			homePage.previewPane.clickPreviewTab();//Click the preview tab

			Log.message("3. Selected the preview tab for selected object : " + dataPool.get("ObjectName"), driver);

			//Step-4 : Select the 'New annotation' object from the task pane
			//--------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.MenuItems.Annotation.Value);//Select the new annotation object from the new menu bar

			String annotatedObject =  homePage.listView.getSelectedListViewItem();//Get the selected annotated object from the list view

			Log.message("4. Created the New annotation object : " + annotatedObject + " for selected object : " + dataPool.get("ObjectName") , driver);

			//Step-5 : Select the Save Annotation option in task pane
			//-------------------------------------------------------
			if(!homePage.taskPanel.clickItem(Caption.Taskpanel.SaveAnnotations.Value))//Save the created annotation object
				throw new Exception("'Save Annotation' option is not displayed in task pane.");

			Log.message("5. Created annotation object : " + annotatedObject + " is saved in list view using " + Caption.Taskpanel.SaveAnnotations.Value + " option from the task pane.", driver);

			//Step-6 : Select the parent document object for the created annoatated object
			//----------------------------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new Exception("Specified object : " + dataPool.get("ObjectName") + " is not displayed in list view.");

			Log.message("6. Selected the parent object : " + dataPool.get("ObjectName") + " for the created annotated object : " + annotatedObject , driver);

			//Step-7 : Select the hide annotation object from the task pane
			//-------------------------------------------------------------
			if(!homePage.taskPanel.clickItem(Caption.Taskpanel.HideAnnotations.Value))//Selected the hide annotation option from the task panel
				throw new Exception("Hide annoation is not displayed for the selected object : " + dataPool.get("ObjectName") );

			Log.message("7. Selected the '" + Caption.Taskpanel.HideAnnotations.Value + "' option for annotated parent document :  " + dataPool.get("ObjectName"), driver);

			//Verification : Verify if "Show annotation" option is displayed in task pane
			//---------------------------------------------------------------------------
			if(homePage.taskPanel.isItemExists(Caption.Taskpanel.ShowAnnotations.Value))//Verify if show annotation option is displayed in task pane
				Log.pass("Test Case Passed.'" + Caption.Taskpanel.ShowAnnotations.Value + "' is displayed in task pane as expected when hide the annotation object.", driver);
			else
				Log.fail("Test Case Failed.'" + Caption.Taskpanel.ShowAnnotations.Value + "' is not displayed in task pane.", driver);			

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally{
			Utility.quitDriver(driver);
		}//End finally

	}//End Annotations_43031

	/**
	 * Annotations_43032 : Validate functionality of Show Annotation
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Annotations"}, 
			description = "Validate functionality of Show Annotation.")
	public void Annotations_43032(HashMap<String,String> dataValues, String driverType) throws Exception {

		try {



			driver = WebDriverUtils.getDriver();

			//Pre-requiste : login to MFWA with valid credentials
			//---------------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver,true);//Login to Mfwa with valid credentials

			//Step-1 : Navigate to any view
			//-----------------------------
			String navigateToView = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));

			Log.message("1. Navigated to : " + navigateToView + " search view.", driver);

			//Step-2 : Select the object from the list view
			//---------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))//Select the object from the list view
				throw new Exception("Object " + dataPool.get("ObjectName") + " is not displayed in list view.");

			Log.message("2. Selected the object : " + dataPool.get("ObjectName") + " from the list view.", driver);

			//Step-3 : Click the Preview tab for specified object
			//---------------------------------------------------
			homePage.previewPane.clickPreviewTab();//Click the preview tab

			Log.message("3. Selected the preview tab for selected object : " + dataPool.get("ObjectName")   , driver);

			//Verify if 'New Annotations' is displayed in task pane
			//-----------------------------------------------------
			if(!homePage.taskPanel.isItemExists(Caption.Taskpanel.NewAnnotations.Value))//Verify if new annotations are displayed in task pane
				throw new Exception("New annotation option is not displayed in task pane.");

			//Step-4 : Select the 'New annotation' object from the task pane
			//--------------------------------------------------------------
			if(!homePage.taskPanel.clickItem(Caption.Taskpanel.NewAnnotations.Value))//Select the new annotation object from the task pane
				throw new Exception("Option : " + Caption.Taskpanel.NewAnnotations.Value + " is not displayed in task pane.");

			String annotatedObject =  homePage.listView.getSelectedListViewItem();//Get the selected annotated object from the list view

			Log.message("4. Created the New annotation object : " + annotatedObject + " for selected object : " + dataPool.get("ObjectName") , driver);

			//Verify if 'New Annotations' is displayed in task pane
			//-----------------------------------------------------
			if(homePage.taskPanel.isItemExists(Caption.Taskpanel.NewAnnotations.Value))//Verify if new annotations are displayed in task pane
				throw new Exception("New annotation option is not displayed in task pane.");

			//Step-5 : Select the Save Annotation option in task pane
			//-------------------------------------------------------
			if(!homePage.taskPanel.clickItem(Caption.Taskpanel.SaveAnnotations.Value))//Save the created annotation object
				throw new Exception("Option : " + Caption.Taskpanel.SaveAnnotations.Value + " is not displayed in task pane.");

			Log.message("5. Created annotation object : " + annotatedObject + " is saved in list view using " + Caption.Taskpanel.SaveAnnotations.Value + " option from the task pane.", driver);

			//Step-6 : Select the parent document object for the created annoatated object
			//----------------------------------------------------------------------------
			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new Exception("Object : " + dataPool.get("ObjectName") + " is not displayed in listview.");

			Log.message("6. Selected the parent object : " + dataPool.get("ObjectName") + " for the created annotated object : " + annotatedObject , driver);

			//Verification :  Verify if expected annotation option is displayed for the parent object
			//---------------------------------------------------------------------------------------
			if(!homePage.taskPanel.isItemExists(Caption.Taskpanel.HideAnnotations.Value))//Verify if hide annotation is displayed in task pane				
				throw new Exception("Hide annoation is not displayed for the selected object : " + dataPool.get("ObjectName") );

			//Step-7 : Select the hide annotation object from the task pane
			//-------------------------------------------------------------
			if(!homePage.taskPanel.clickItem(Caption.Taskpanel.HideAnnotations.Value))//Selected the hide annotation option from the task panel
				throw new Exception("Expected " + Caption.Taskpanel.HideAnnotations.Value + " is not displayed in task pane." );

			Log.message("7. Selected the '" + Caption.Taskpanel.HideAnnotations.Value + "' option for annotated parent document :  " + dataPool.get("ObjectName"), driver);

			//Step-8 : Select the show annotation option from the task pane
			//-------------------------------------------------------------
			if(!homePage.taskPanel.clickItem(Caption.Taskpanel.ShowAnnotations.Value))//Select the show annotation option from the task pane
				throw new Exception("Expected " +  Caption.Taskpanel.ShowAnnotations.Value + " is not displayed in task pane." );

			Log.message("8. Selected the '" +  Caption.Taskpanel.ShowAnnotations.Value + "' option from the task pane.");

			String expectedResults = "";

			//Verify if expected annotation option is displayed in task pane
			//--------------------------------------------------------------
			if(!homePage.taskPanel.isItemExists(Caption.Taskpanel.HideAnnotations.Value))
				expectedResults = "Expected " + Caption.Taskpanel.HideAnnotations.Value + " is not displayed in task pane.";

			//Verify if expected annotation option is displyed in task pane
			//-------------------------------------------------------------
			if(!homePage.taskPanel.isItemExists(Caption.Taskpanel.EditAnnotations.Value))
				expectedResults += "Expected " + Caption.Taskpanel.EditAnnotations.Value + " is not displayed in task pane.";

			//Verification : Verify the expected annotation option is displayed in task pane 
			//------------------------------------------------------------------------------
			if(expectedResults.equals(""))//Verify if expected annotated object is displayed
				Log.pass("Test Case Passed.Expected annotation option is displayed when selecting the show annotation option. Expected annotations : " + Caption.Taskpanel.HideAnnotations.Value + " & " + Caption.Taskpanel.EditAnnotations.Value, driver);
			else
				Log.fail("Test Case Failed.Expected annotation option is not displayed when selecting the show annotation." + expectedResults, driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally{
			Utility.quitDriver(driver);
		}//End finally

	}//End Annotations_43032

}//End Annotations


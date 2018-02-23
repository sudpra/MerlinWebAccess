package MFClient.Tests.ExternalConnectors;

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
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class ExternalViews {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String userName = null;
	public static String password = null;
	public static String testVault = null;
	public static String configURL = null;
	public static String productVersion = null;
	public static String extnViewName = null;
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
			password = xmlParameters.getParameter("Password");
			testVault = xmlParameters.getParameter("VaultName");	
			configURL = xmlParameters.getParameter("ConfigurationURL");
			extnViewName = xmlParameters.getParameter("ExternalViewName");
			driverType = xmlParameters.getParameter("driverType");

			className = this.getClass().getSimpleName().toString().trim();
			if (driverType.equalsIgnoreCase("IE"))
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + driverType.toUpperCase().trim() + xmlParameters.getParameter("driverVersion").trim();
			else
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + driverType.toUpperCase().trim();

			Utility.restoreTestVault();
			Utility.configureUsers(xlTestDataWorkBook);
			String extRepoAppName = xmlParameters.getParameter("ExternalRepositoryApp");
			Utility.installVaultApplication(extRepoAppName);
			Utility.takeVaultOfflineAndBringOnline(testVault, "OfflineAndOnline");	

			if (!Utility.isExternalViewExists(extnViewName))
				throw new Exception("isExternalViewExists : " + extnViewName + " is not exist in the home view.");


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
	 * ExternalViews_43105 : Check Add property link for external objects
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"ExternalViews","Bug","BugCase : 138216" }, 
			description = "Check Add property link for external objects.")
	public void ExternalViews_43105(HashMap<String,String> dataValues, String driverType) throws Exception {


		try {



			driver = WebDriverUtils.getDriver();//get the webdriver

			//Pre-requiste : login to MFWA with valid credentials
			//---------------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver,true);//Login to Mfwa with valid credentials

			//Step-1 : Navigated to the external objects view
			//-----------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));// Navigated to the search view
			homePage.listView.clickItem(dataPool.get("ObjectName"));//Selected the object from list view

			Log.message("1. Navigated to the : " + viewToNavigate + " search view and selected the " + dataPool.get("ObjectName") + " object.", driver);

			MetadataCard metadataCard = new MetadataCard(driver,true);

			//Verification : Verify if add property link is not displayed
			//-----------------------------------------------------------
			if(!metadataCard.isAddPropertyLinkDisplayed())
				Log.pass("Test Case Passed.'Add property' link is not displayed for selected unmanaged document : " + dataPool.get("ObjectName") + " as expected.", driver);
			else
				Log.fail("Test Case Failed.'Add property' link is displayed for selected unmanaged document :  " + dataPool.get("ObjectName"), driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End ExternalViews_43105



	/**
	 * ExternalViews_43087 : Check Add to favorite option available for managed objects
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"ExternalViews"}, 
			description = "Check Add to favorite option available for managed objects.")
	public void ExternalViews_43087(HashMap<String,String> dataValues, String driverType) throws Exception {

		try {



			driver = WebDriverUtils.getDriver();//get the webdriver

			//Pre-requiste : login to MFWA with valid credentials
			//---------------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver,true);//Login to Mfwa with valid credentials

			//Step-1 : Navigated to the external objects view
			//-----------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("ObjectName"));// Navigated to the search view

			if(!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new Exception("Object : " + dataPool.get("ObjectName") + " is not displayed in " + dataPool.get("NavigateToView") + " search view ");

			Log.message("1. Navigated to the : " + viewToNavigate + " search view and selected the " + dataPool.get("ObjectName") + " object.", driver);

			//Step-2 : Select the 'Favorites' icon in right pane metadatacard
			//---------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiate the right pane metadatadatacard
			metadataCard.setFavorite(true);//Set the favorites icon in right pane metadatacard

			MFilesDialog mfilesDialog = new MFilesDialog(driver);//Instantiate the Mfiles dialog
			mfilesDialog.clickOkButton();//Click the 'ok' button in M-files dialog

			Log.message("2. Selected favorites icon for selected managed object : " + dataPool.get("ObjectName") , driver);

			//Step-3 : Navigated to 'Favorites' view
			//--------------------------------------
			viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");//Navigated to the Favorites view

			Log.message("3. Navigated to the 'Favorites' view.", driver);			

			//Verification : Verify if selected object is displayed in favorites view
			//-----------------------------------------------------------------------
			if(homePage.listView.isItemExists(dataPool.get("ObjectName")))
				Log.message("Test Case Passed. Managed object : " + dataPool.get("ObjectName") + " is displayed in 'Favorites' view as expected.", driver);
			else
				Log.message("Test Case Failed. Managed object : " + dataPool.get("ObjectName") + " is not displayed in 'Favorites' view.", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End ExternalViews_43087



	/**
	 * ExternalViews_43230 : Check external view column header list is same as MFClient
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"ExternalViews"}, 
			description = "Check external view column header list is same as MFClient.")
	public void ExternalViews_43230(HashMap<String,String> dataValues, String driverType) throws Exception {

		try {



			driver = WebDriverUtils.getDriver();//get the webdriver

			//Pre-requiste : login to MFWA with valid credentials
			//---------------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver,true);//Login to Mfwa with valid credentials

			//Step-1 : Navigated to the external objects view
			//-----------------------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");// Navigated to the search view

			Log.message("1. Navigated to the '" + viewToNavigate + "' view.", driver);

			//Get the column values 
			//---------------------
			String[] expectedColumns = dataPool.get("Columns").split("\n");

			String results = "";

			//Verify if expected column is displayed in list view
			//---------------------------------------------------
			String[] visibleColumns = homePage.listView.getVisibleColumns();//Get the available visible columns

			//Verify if expected column exist or not
			//--------------------------------------
			for (int itemIdx=0; itemIdx<visibleColumns.length; itemIdx++) {
				if(!visibleColumns[itemIdx].equals(expectedColumns[itemIdx]))
					results += "Column " + visibleColumns[itemIdx] + " is does not exists in the external view.";
			}

			//Verification : Verify if column exists as expected
			//--------------------------------------------------
			if(results.equals(""))
				Log.pass("Test Case Passed. Column header for external views are same as MFClient columns.", driver);
			else
				Log.fail("Test Case Failed. Column header is not displayed as expected in external views : " +results, driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End ExternalViews_43087





}

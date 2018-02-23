package MFClient.Tests.Search;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.Utils;
import genericLibrary.WebDriverUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class SearchWordOperations {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String userName = null;
	public static String password = null;
	public static String testVault = null;
	public static String configURL = null;
	public static String userFullName = null;
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
			userFullName = xmlParameters.getParameter("UserFullName");
			Utility.restoreTestVault();
			Utility.configureUsers(xlTestDataWorkBook);
			className = this.getClass().getSimpleName().toString().trim();
			if (xmlParameters.getParameter("driverType").equalsIgnoreCase("IE"))
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim() + xmlParameters.getParameter("driverVersion").trim();
			else
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim();


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
	}

	/**
	 * 22.2.1 : Check for warning message when performing Wild card search.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint22", "SearchWordOperations","WildCardSearch"},
			description = "Check for warning message when performing Wild card search.")
	public void SprintTest22_2_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		//Pre-requisite : Launch Driver and Login to MFWA
		//-----------------------------------------------
		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);				

			String viewToNavigate = dataPool.get("NavigateToView");

			//Step-1 : Navigate to specified View
			//-----------------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);

			if (viewToNavigate.toUpperCase().equals("") || viewToNavigate == null)
				viewToNavigate = Caption.Search.SearchAllObjects.Value;

			if (viewToNavigate.toUpperCase().contains("SEARCH")) {

				homePage.searchPanel.search("", viewToNavigate); // Search for the documents
			}
			else { //Navigates to the specified view

				homePage.taskPanel.clickItem("Home");
				Utils.fluentWait(driver);

				homePage.listView.navigateThroughView(viewToNavigate);
			}

			Log.message("Step-1 : Navigated to " + viewToNavigate + " View.");

			//Step-2 : Enter wild card character and click search
			//---------------------------------------------------
			homePage.searchPanel.setSearchWord(dataPool.get("WildCardString"));
			homePage.searchPanel.clickSearch(); 

			Log.message("Step-2 : Wild Card Character is entered and search button is clicked.");

			//Verification : Verify Message dialog with your search could be slow error message appears
			//------------------------------------------------------------------------------------------

			//Verifies the existence of M-Files Dialog
			if (!MFilesDialog.exists(driver)) 
				throw new SkipException("M-Files dialog has not opened after performing wild card search.");

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class //Instantiating M-Files dialog
			String warningMsg = mfilesDialog.getMessage(); //Gets the message in M-Files dialog

			//Verifies that message contains 'Your Search could be slow'

			if (warningMsg.equalsIgnoreCase("The search request could not be completed. Wildcard characters cannot be used as first characters in a search request."))
				Log.pass("Test case Passed. M-Files dialog with Your search could be slow error message appeared.");
			else
				Log.fail("Test case Failed. M-Files dialog with Your search could be slow error message is not displayed.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest22_2_1


	/**
	 * 22.2.3 : Perform search after clicking Ok in the warning dialog of wild card search
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint22", "SearchWordOperations","WildCardSearch"}, 
			description = "Perform search after clicking Ok in the warning dialog of wild card search.")
	public void SprintTest22_2_3(HashMap<String,String> dataValues, String driverType) throws Exception {

		//Pre-requisite : Launch Driver and Login to MFWA
		//-----------------------------------------------
		driver = null; 
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			String viewToNavigate = dataPool.get("NavigateToView");

			homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			if (viewToNavigate.toUpperCase().equals("") || viewToNavigate == null)
				viewToNavigate = Caption.Search.SearchAllObjects.Value;

			if (viewToNavigate.toUpperCase().contains("SEARCH")) {

				homePage.searchPanel.search("", viewToNavigate); // Search for the documents
			}
			else { //Navigates to the specified view

				homePage.taskPanel.clickItem("Home");
				Utils.fluentWait(driver);

				homePage.listView.navigateThroughView(viewToNavigate);
			}

			Log.message("Step-1 : Navigated to " + viewToNavigate + " View.");

			//Step-2 : Enter wild card character and click search
			//---------------------------------------------------
			homePage.searchPanel.setSearchWord(dataPool.get("WildCardString"));
			homePage.searchPanel.clickSearch(); 

			Log.message("Step-2 : Wild Card Character is entered and search button is clicked.");

			//Step-3 : Click Ok in the confirmation dialog of Wild Card search and perform normal search operation
			//----------------------------------------------------------------
			if (!MFilesDialog.exists(driver))  //Verifies the existence of M-Files Dialog
				throw new SkipException("M-Files dialog has not opened after performing wild card search.");

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class //Instantiating M-Files dialog
			String warningMsg = mfilesDialog.getMessage(); //Gets the message in M-Files dialog

			if (!warningMsg.equalsIgnoreCase("The search request could not be completed. Wildcard characters cannot be used as first characters in a search request."))	//Verifies that message contains 'Your Search could be slow'
				throw new SkipException("M-Files dialog with Your search could be slow error message is not displayed.");

			mfilesDialog.clickOkButton();

			homePage.searchPanel.clickSearch(); //Click Search button

			Log.message("Step-3 : Clicked Ok in the Wild card search confirmation dialog and normal search operation is performed.");

			//Verification : Verify Message dialog with your search could be slow error message appears
			//------------------------------------------------------------------------------------------
			//Verifies that list contains the item
			if (homePage.listView.itemCount() >0)
				Log.pass("Test case Passed. Normal search opearation worked fine after clicking ok in the wild card search dialog.");
			else
				Log.fail("Test case Failed. No items displayed in the list on performing normal search after clicking Ok in the wild card search dialog.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest22_2_3

	/**
	 * 22.2.4 : Clicking Ok button in the wild card search warning dialog, should clear the value in the search value
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint22", "SearchWordOperations","WildCardSearch"}, 
			description = "Clicking Ok button in the wild card search warning dialog, should clear the value in the search value.")
	public void SprintTest22_2_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		//Pre-requisite : Launch Driver and Login to MFWA
		//-----------------------------------------------
		driver = null; 
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			String viewToNavigate = dataPool.get("NavigateToView");

			homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			if (viewToNavigate.toUpperCase().equals("") || viewToNavigate == null)
				viewToNavigate = Caption.Search.SearchAllObjects.Value;

			if (viewToNavigate.toUpperCase().contains("SEARCH")) {

				homePage.searchPanel.search("", viewToNavigate); // Search for the documents
			}
			else { //Navigates to the specified view

				homePage.taskPanel.clickItem("Home");
				Utils.fluentWait(driver);

				homePage.listView.navigateThroughView(viewToNavigate);
			}

			Log.message("Step-1 : Navigated to " + viewToNavigate + " View.");

			//Step-2 : Enter wild card character and click search
			//---------------------------------------------------
			homePage.searchPanel.setSearchWord(dataPool.get("WildCardString"));
			homePage.searchPanel.clickSearch(); 
			Utils.fluentWait(driver);

			Log.message("Step-2 : Wild Card Character is entered and search button is clicked.");

			//Step-3 : Click Ok in the confirmation dialog of Wild Card search and perform normal search operation
			//----------------------------------------------------------------
			if (!MFilesDialog.exists(driver))  //Verifies the existence of M-Files Dialog
				throw new SkipException("M-Files dialog has not opened after performing wild card search.");

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class //Instantiating M-Files dialog
			String warningMsg = mfilesDialog.getMessage(); //Gets the message in M-Files dialog

			if (!warningMsg.equalsIgnoreCase("The search request could not be completed. Wildcard characters cannot be used as first characters in a search request.")) //Verifies that message contains 'Your Search could be slow'
				throw new SkipException("M-Files dialog with Your search could be slow error message is not displayed.");

			mfilesDialog.clickOkButton();

			Log.message("Step-3 : Clicked Ok in the Wild card search confirmation dialog and normal search operation is performed.");

			//Verification : Verify Message dialog with your search could be slow error message appears
			//------------------------------------------------------------------------------------------
			//Verifies that list contains the item
			if (homePage.searchPanel.getSearchWord().equals(""))
				Log.pass("Test case Passed. Search Text Box is cleared after clicking Ok in the wild card warning dialog.");
			else
				Log.fail("Test case Failed. Search Text Box is not cleared after clicking Ok in the wild card warning dialog.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest22_2_4

	/**
	 * 22.2.6 : Clicking Show Details link in the wild card search warning dialog should display detailed error description
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint22", "SearchWordOperations","WildCardSearch", "ShowDetailsLink"}, 
			description = "Clicking Show Details link in the wild card search warning dialog should display detailed error description.")
	public void SprintTest22_2_6(HashMap<String,String> dataValues, String driverType) throws Exception {

		//Pre-requisite : Launch Driver and Login to MFWA
		//-----------------------------------------------
		driver = null; 
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			String viewToNavigate = dataPool.get("NavigateToView");

			homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			if (viewToNavigate.toUpperCase().equals("") || viewToNavigate == null)
				viewToNavigate = Caption.Search.SearchAllObjects.Value;

			if (viewToNavigate.toUpperCase().contains("SEARCH")) {

				homePage.searchPanel.search("", viewToNavigate); // Search for the documents
			}
			else { //Navigates to the specified view

				homePage.taskPanel.clickItem("Home");
				Utils.fluentWait(driver);

				homePage.listView.navigateThroughView(viewToNavigate);
			}

			Log.message("Step-1 : Navigated to " + viewToNavigate + " View.");

			//Step-2 : Enter wild card character and click search
			//---------------------------------------------------
			homePage.searchPanel.setSearchWord(dataPool.get("WildCardString"));
			homePage.searchPanel.clickSearch(); 

			Log.message("Step-2 : Wild Card Character is entered and search button is clicked.");

			//Step-3 : Click Show Details link in warning dialog
			//--------------------------------------------------
			if (!MFilesDialog.exists(driver))  //Verifies the existence of M-Files Dialog
				throw new SkipException("M-Files dialog has not opened after performing wild card search.");

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class //Instantiating M-Files dialog

			//Verifies Show Details link is available in M-Files dialog
			if (!mfilesDialog.isShowDetailLinkExists())
				throw new SkipException("The 'Show Details' button was not visible.");

			mfilesDialog.clickShowDetailLink(); //Clicks Show Detail link	

			Log.message("Step-3 : Show Detail link is clicked.");

			//Verification : To Verify detailed error description is shown
			//------------------------------------------------------------
			mfilesDialog = new MFilesDialog(driver); //Instantiating M-Files dialog

			//Verifies detailed error message has value
			if (!mfilesDialog.getDetailedMessage().equals(""))
				Log.pass("Test case Passed. Detailed error message is displayed on clicking Show Details link.");
			else
				Log.fail("Test case Failed. Detailed error message is not displayed on clicking Show Details link.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest22_2_6


	/**
	 * 22.2.7 : Clicking Show Details link in the wild card search warning dialog should display Copy to Clipboard button
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint22", "SearchWordOperations", "ShowDetailsLink"}, 
			description = "Clicking Show Details link in the wild card search warning dialog should display Copy to Clipboard button.")
	public void SprintTest22_2_7(HashMap<String,String> dataValues, String driverType) throws Exception {

		//Pre-requisite : Launch Driver and Login to MFWA
		//-----------------------------------------------
		driver = null; 
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			String viewToNavigate = dataPool.get("NavigateToView");

			homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			if (viewToNavigate.toUpperCase().equals("") || viewToNavigate == null)
				viewToNavigate = Caption.Search.SearchAllObjects.Value;

			if (viewToNavigate.toUpperCase().contains("SEARCH")) {

				homePage.searchPanel.search("", viewToNavigate); // Search for the documents
			}
			else { //Navigates to the specified view

				homePage.taskPanel.clickItem("Home");
				Utils.fluentWait(driver);

				homePage.listView.navigateThroughView(viewToNavigate);
			}

			Log.message("Step-1 : Navigated to " + viewToNavigate + " View.");

			//Step-2 : Enter wild card character and click search
			//---------------------------------------------------
			homePage.searchPanel.setSearchWord(dataPool.get("WildCardString"));
			homePage.searchPanel.clickSearch(); 
			Utils.fluentWait(driver);

			Log.message("Step-2 : Wild Card Character is entered and search button is clicked.");

			//Step-3 : Click Show Details link in warning dialog
			//--------------------------------------------------
			if (!MFilesDialog.exists(driver))  //Verifies the existence of M-Files Dialog
				throw new SkipException("M-Files dialog has not opened after performing wild card search.");

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class //Instantiating M-Files dialog

			//Verifies Show Details link is available in M-Files dialog
			if (!mfilesDialog.isShowDetailLinkExists())
				throw new Exception ("Show Detail link does not exists.");

			mfilesDialog.clickShowDetailLink(); //Clicks Show Detail link	
			Utils.fluentWait(driver);

			Log.message("Step-3 : Show Detail link is clicked.");

			//Verification : To Verify Copy To Clipboard button is displayed after clicking Show Details link
			//-----------------------------------------------------------------------------------------------
			mfilesDialog = new MFilesDialog(driver); //Instantiating M-Files dialog

			//Verifies copy to clipboard buttoon is displayed after clickin show details link
			if (mfilesDialog.isCopyToClipboardBtnDisplayed())
				Log.pass("Test case Passed. Copy to Clipboard button is displayed after clicking Show Details link.");
			else
				Log.fail("Test case Failed. Copy to Clipboard button is not displayed after clicking Show Details link.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest22_2_7

	/**
	 * 22.2.8 : Clicking Show Details link twice in the wild card search warning dialog should hide detailed error description
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint22", "SearchWordOperations", "ShowDetailsLink"}, 
			description = "Clicking Show Details link twice in the wild card search warning dialog should hide detailed error description.")
	public void SprintTest22_2_8(HashMap<String,String> dataValues, String driverType) throws Exception {

		//Pre-requisite : Launch Driver and Login to MFWA
		//-----------------------------------------------
		driver = null; 
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			homePage = LoginPage.launchDriverAndLogin(driver, true);

			String viewToNavigate = dataPool.get("NavigateToView");

			//Step-1 : Navigate to specified View
			//-----------------------------------
			if (viewToNavigate.toUpperCase().equals("") || viewToNavigate == null)
				viewToNavigate = Caption.Search.SearchAllObjects.Value;

			if (viewToNavigate.toUpperCase().contains("SEARCH")) {

				homePage.searchPanel.search("", viewToNavigate); // Search for the documents
			}
			else { //Navigates to the specified view

				homePage.taskPanel.clickItem("Home");
				Utils.fluentWait(driver);

				homePage.listView.navigateThroughView(viewToNavigate);
			}

			Log.message("Step-1 : Navigated to " + viewToNavigate + " View.");

			//Step-2 : Enter wild card character and click search
			//---------------------------------------------------
			homePage.searchPanel.setSearchWord(dataPool.get("WildCardString"));
			homePage.searchPanel.clickSearch(); 

			Log.message("Step-2 : Wild Card Character is entered and search button is clicked.");

			//Step-3 : Click Show Details link twice in warning dialog
			//---------------------------------------------------------
			if (!MFilesDialog.exists(driver))  //Verifies the existence of M-Files Dialog
				throw new SkipException("M-Files dialog has not opened after performing wild card search.");

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class //Instantiating M-Files dialog

			//Verifies Show Details link is available in M-Files dialog
			if (!mfilesDialog.isShowDetailLinkExists())
				throw new Exception ("Show Detail link does not exists.");

			mfilesDialog.clickShowDetailLink(); //Clicks Show Detail link
			mfilesDialog.clickShowDetailLink(); //Clicks Show Detail link

			Log.message("Step-3 : Show Detail link is clicked twice.");

			//Verification : To Verify detailed error description is shown
			//------------------------------------------------------------
			mfilesDialog = new MFilesDialog(driver); //Instantiating M-Files dialog

			//Verifies detailed error message has value
			if (mfilesDialog.getDetailedMessage().equals(""))
				Log.pass("Test case Passed. Detailed error message is hidden on clicking Show Details link twice.");
			else
				Log.fail("Test case Failed. Detailed error message is not hidden on clicking Show Details link twice.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest22_2_8

	/**
	 * 22.2.9 : Clicking Show Details link twice in the wild card search warning dialog should hide 'Copy to Clipboard' button
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint22", "SearchWordOperations", "ShowDetailsLink"}, 
			description = "Clicking Show Details link twice in the wild card search warning dialog should hide 'Copy to Clipboard' button.")
	public void SprintTest22_2_9(HashMap<String,String> dataValues, String driverType) throws Exception {

		//Pre-requisite : Launch Driver and Login to MFWA
		//-----------------------------------------------
		driver = null; 
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			homePage = LoginPage.launchDriverAndLogin(driver, true);

			String viewToNavigate = dataPool.get("NavigateToView");

			//Step-1 : Navigate to specified View
			//-----------------------------------
			if (viewToNavigate.toUpperCase().equals("") || viewToNavigate == null)
				viewToNavigate = Caption.Search.SearchAllObjects.Value;

			if (viewToNavigate.toUpperCase().contains("SEARCH")) {

				homePage.searchPanel.search("", viewToNavigate); // Search for the documents
			}
			else { //Navigates to the specified view

				homePage.taskPanel.clickItem("Home");
				Utils.fluentWait(driver);

				homePage.listView.navigateThroughView(viewToNavigate);
			}

			Log.message("Step-1 : Navigated to " + viewToNavigate + " View.");

			//Step-2 : Enter wild card character and click search
			//---------------------------------------------------
			homePage.searchPanel.setSearchWord(dataPool.get("WildCardString"));
			homePage.searchPanel.clickSearch(); 

			Log.message("Step-2 : Wild Card Character is entered and search button is clicked.");

			//Step-3 : Click Show Details link twice in warning dialog
			//----------------------------------------------------------
			if (!MFilesDialog.exists(driver))  //Verifies the existence of M-Files Dialog
				throw new SkipException("M-Files dialog has not opened after performing wild card search.");

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class //Instantiating M-Files dialog

			//Verifies Show Details link is available in M-Files dialog
			if (!mfilesDialog.isShowDetailLinkExists())
				throw new Exception ("Show Detail link does not exists.");

			mfilesDialog.clickShowDetailLink(); //Clicks Show Detail link
			mfilesDialog.clickShowDetailLink(); //Clicks Show Detail link	

			Log.message("Step-3 : Show Detail link is clicked twice.");

			//Verification : To Verify Copy To Clipboard button is displayed after clicking Show Details link
			//-----------------------------------------------------------------------------------------------
			mfilesDialog = new MFilesDialog(driver); //Instantiating M-Files dialog

			//Verifies copy to clipboard buttoon is displayed after clickin show details link
			if (!mfilesDialog.isCopyToClipboardBtnDisplayed())
				Log.pass("Test case Passed. Copy to Clipboard button is hidden after clicking Show Details link twice.");
			else
				Log.fail("Test case Failed. Copy to Clipboard button is not hidden after clicking Show Details link twice.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest22_2_9

	/**
	 * 22.2.10 : Detailed error description should not be displayed by default in the wild card search warning dialog.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint22", "SearchWordOperations", "ShowDetailsLink"}, 
			description = "Detailed error description should not be displayed by default in the wild card search warning dialog.")
	public void SprintTest22_2_10(HashMap<String,String> dataValues, String driverType) throws Exception {

		//Pre-requisite : Launch Driver and Login to MFWA
		//-----------------------------------------------
		driver = null; 
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			homePage = LoginPage.launchDriverAndLogin(driver, true);

			String viewToNavigate = dataPool.get("NavigateToView");

			//Step-1 : Navigate to specified View
			//-----------------------------------
			if (viewToNavigate.toUpperCase().equals("") || viewToNavigate == null)
				viewToNavigate = Caption.Search.SearchAllObjects.Value;

			if (viewToNavigate.toUpperCase().contains("SEARCH")) {

				homePage.searchPanel.search("", viewToNavigate); // Search for the documents
			}
			else { //Navigates to the specified view

				homePage.taskPanel.clickItem("Home");
				Utils.fluentWait(driver);

				homePage.listView.navigateThroughView(viewToNavigate);
			}

			Log.message("Step-1 : Navigated to " + viewToNavigate + " View.");

			//Step-2 : Enter wild card character and click search
			//---------------------------------------------------
			homePage.searchPanel.setSearchWord(dataPool.get("WildCardString"));
			homePage.searchPanel.clickSearch(); 

			Log.message("Step-2 : Wild Card Character is entered and search button is clicked.");

			//Verification : To Verify detailed error description is not available by default
			//-------------------------------------------------------------------------------
			if (!MFilesDialog.exists(driver))  //Verifies the existence of M-Files Dialog
				throw new SkipException("M-Files dialog has not opened after performing wild card search.");

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class //Instantiating M-Files dialog

			//Verifies detailed error message is hidden by default
			if (mfilesDialog.getDetailedMessage().equals(""))
				Log.pass("Test case Passed. Detailed error message is hidden by default.");
			else
				Log.fail("Test case Failed. Detailed error message is not hidden by default.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest22_2_10

	/**
	 * 22.2.11 : Copy to Clipboard button should not be displayed by default in the wild card search warning dialog.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint22", "SearchWordOperations"}, 
			description = "Copy to Clipboard button should not be displayed by default in the wild card search warning dialog.")
	public void SprintTest22_2_11(HashMap<String,String> dataValues, String driverType) throws Exception {

		//Pre-requisite : Launch Driver and Login to MFWA
		//-----------------------------------------------
		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;
		HomePage homePage = null;

		try {				


			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			String viewToNavigate = dataPool.get("NavigateToView");

			homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to specified View
			//-----------------------------------
			if (viewToNavigate.toUpperCase().equals("") || viewToNavigate == null)
				viewToNavigate = Caption.Search.SearchAllObjects.Value;

			if (viewToNavigate.toUpperCase().contains("SEARCH")) {

				homePage.searchPanel.search("", viewToNavigate); // Search for the documents
			}
			else { //Navigates to the specified view

				homePage.taskPanel.clickItem("Home");
				Utils.fluentWait(driver);

				homePage.listView.navigateThroughView(viewToNavigate);
			}

			Log.message("Step-1 : Navigated to " + viewToNavigate + " View.");

			//Step-2 : Enter wild card character and click search
			//---------------------------------------------------
			homePage.searchPanel.setSearchWord(dataPool.get("WildCardString"));
			homePage.searchPanel.clickSearch(); 

			Log.message("Step-2 : Wild Card Character is entered and search button is clicked.");

			//Verification : To Verify detailed error description is not available by default
			//-------------------------------------------------------------------------------
			if (!MFilesDialog.exists(driver)) //Verifies the existence of M-Files Dialog
				throw new SkipException("M-Files dialog has not opened after performing wild card search.");

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class //Instantiating M-Files dialog

			//Verifies detailed error message is hidden by default
			if (!mfilesDialog.isCopyToClipboardBtnDisplayed())
				Log.pass("Test case Passed. Copy to Clipboard button is hidden by default.");
			else
				Log.fail("Test case Failed. Copy to Clipboard button is not hidden by default.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest22_2_11

	/**
	 * 58.2.19 : Search an object using it's part of it's comment
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint58", "Comment","SearchWordOperations"}, 
			description = "Search an object using it's part of it's comment.")
	public void SprintTest58_2_19(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object and open it's metadatacard
			//---------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);
			MetadataCard metadataCard = new MetadataCard(driver);

			Log.message("2. Search for an object and open it's metadatacard");

			//3. Set the Comment and save
			//----------------------------
			metadataCard.setComments(dataPool.get("Comment"));
			metadataCard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("3. Set the Comment and save.");

			//4. Search using the comment
			//----------------------------
			homePage.searchPanel.search(dataPool.get("Comment").split(" ")[1], "Search only: " + dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			Log.message("4. Search using the comment.");

			//Verification: To verify if the object is listed
			//-----------------------------------------------
			if(homePage.listView.isItemExists(dataPool.get("Object")))
				Log.pass("Test Case Passed. Comment was set to the object through metadatacard.");
			else
				Log.fail("Test Case Failed. Comment was not set to the object through metadatacard.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 58.2.20 : Search an object using earlier comment
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint58", "Comment","SearchWordOperations"}, 
			description = "Search an object using earlier comment.")
	public void SprintTest58_2_20(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {




			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object and open it's metadatacard
			//---------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");

			Utils.fluentWait(driver);
			MetadataCard metadataCard = new MetadataCard(driver, true);

			Log.message("2. Search for an object and open it's metadatacard");

			//3. Set the Comment and save
			//----------------------------
			metadataCard.setComments(dataPool.get("Comment1"));
			metadataCard.saveAndClose();
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			metadataCard = new MetadataCard(driver, true);
			metadataCard.setComments(dataPool.get("Comment2"));
			metadataCard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("3. Set the Comment and save.");

			//4. Search using the comment
			//----------------------------
			homePage.searchPanel.search(dataPool.get("Comment1"), "Search only: " + dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			Log.message("4. Search using the comment.");

			//Verification: To verify if the object is listed
			//-----------------------------------------------
			if(homePage.listView.isItemExists(dataPool.get("Object")))
				Log.pass("Test Case Passed. Comment was set to the object through metadatacard.");
			else
				Log.fail("Test Case Failed. Comment was not set to the object through metadatacard.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}



	/**
	 * 100.1.1 : Existing search keywords are displayed on entering the first letter of the keyword again
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint100", "Comment","SearchWordOperations"}, 
			description = "Existing search keywords are displayed on entering the first letter of the keyword again.")
	public void SprintTest100_1_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Create some search history
			//-----------------------------
			String[] searchKeys = dataPool.get("SearchWord").split("\n");

			for(int counter = 0; counter < searchKeys.length; counter++) {
				homePage.searchPanel.setSearchWord(searchKeys[counter], true);
				homePage.searchPanel.clickSearch();
				Utils.fluentWait(driver);
			}

			Log.message("2. Create some search history");

			//3. Enter the First letter of the previously used search words
			//--------------------------------------------------------------
			List<String> searchWords = homePage.searchPanel.getSearchHistoryForSearchWord(Character.toString(searchKeys[0].charAt(0)));

			Log.message("3. Enter the First letter of the previously used search words");

			//Verification: To verify if the first letter matching keywords are listed
			//-------------------------------------------------------------------------
			boolean flag = false;

			for(int counter = 0; counter < searchKeys.length; counter++) {
				if(searchKeys[counter].indexOf(searchKeys[0].charAt(0)) == 0 && searchWords.indexOf(searchKeys[counter]) != -1)
					flag = true;
				else if(searchKeys[counter].indexOf(searchKeys[0].charAt(0)) != 0 && searchWords.indexOf(searchKeys[counter]) == -1)
					flag = true;
				else {
					flag = false;
					break;
				}
			}

			if(flag)
				Log.pass("Test Case Passed. The keywords starting with the specified character are listed as expected.");
			else
				Log.fail("Test Case Failed. Search history listing did not work as expected.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.1.2 : search keywords are displayed on entering the first letter of the keyword again, while doing Advanced Search
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint100", "Comment","SearchWordOperations"}, 
			description = "search keywords are displayed on entering the first letter of the keyword again, while doing Advanced Search.")
	public void SprintTest100_1_2(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Set an advanced Search condition
			//------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), dataPool.get("Value"));

			Log.message("2. Set an advanced Search condition");

			//3. Create some search history
			//-----------------------------
			String[] searchKeys = dataPool.get("SearchWord").split("\n");

			for(int counter = 0; counter < searchKeys.length; counter++) {
				homePage.searchPanel.setSearchWord(searchKeys[counter], true);
				homePage.searchPanel.clickSearch();
				Utils.fluentWait(driver);
			}

			Log.message("3. Create some search history");

			//4. Enter the First letter of the previously used search words
			//--------------------------------------------------------------
			List<String> searchWords = homePage.searchPanel.getSearchHistoryForSearchWord(Character.toString(searchKeys[0].charAt(0)));
			Log.message("4. Enter the First letter of the previously used search words");

			//Verification: To verify if the first letter matching keywords are listed
			//-------------------------------------------------------------------------
			boolean flag = false;

			for(int counter = 0; counter < searchKeys.length; counter++) {
				if(searchKeys[counter].indexOf(searchKeys[0].charAt(0)) == 0 && searchWords.indexOf(searchKeys[counter]) != -1)
					flag = true;
				else if(searchKeys[counter].indexOf(searchKeys[0].charAt(0)) != 0 && searchWords.indexOf(searchKeys[counter]) == -1)
					flag = true;
				else {
					flag = false;
					break;
				}
			}

			if(flag)
				Log.pass("Test Case Passed. The keywords starting with the specified character are listed as expected.");
			else
				Log.fail("Test Case Failed. Search history listing did not work as expected.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.1.3 : search keywords starting with the same letter are displayed in alphabetical order
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint100", "Comment","SearchWordOperations"}, 
			description = "search keywords starting with the same letter are displayed in alphabetical order.")
	public void SprintTest100_1_3(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");


			//2. Create some search history
			//-----------------------------
			homePage.searchPanel.clearHistory();
			MFilesDialog mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.confirmDeleteHistory();

			String[] searchKeys = dataPool.get("SearchWord").split("\n");
			List<String> expectedOrder = new ArrayList<String>(Arrays.asList(searchKeys));

			Collections.sort(expectedOrder);

			for(int counter = 0; counter < searchKeys.length; counter++) {
				homePage.searchPanel.setSearchWord(searchKeys[counter], true);
				homePage.searchPanel.clickSearch();
				Utils.fluentWait(driver);
			}

			Log.message("2. Create some search history");

			//3. Enter the First letter of the previously used search words
			//--------------------------------------------------------------
			List<String> searchWords = homePage.searchPanel.getSearchHistoryForSearchWord(Character.toString(searchKeys[0].charAt(0)));

			Log.message("3. Enter the First letter of the previously used search words");

			//Verification: To verify if the history is listed in alphabetical order
			//-------------------------------------------------------------------------
			Arrays.sort(searchKeys);
			boolean flag = false;

			for(int counter = 0; counter < searchKeys.length; counter++) {
				if(expectedOrder.get(counter).toString().indexOf(searchKeys[0].charAt(0)) == 0 && searchWords.indexOf(expectedOrder.get(counter).toString()) == counter)
					flag = true;
				else {
					flag = false;
					break;
				}
			}

			if(flag)
				Log.pass("Test Case Passed. The keywords starting with the specified character are listed as expected.");
			else
				Log.fail("Test Case Failed. Search history listing did not work as expected.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.1.4 : search keywords are cleared on clicking Clear History option
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint100", "Clear History","SearchWordOperations"}, 
			description = "search keywords are cleared on clicking Clear History option.")
	public void SprintTest100_1_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Create some search history
			//-----------------------------
			String[] searchKeys = dataPool.get("SearchWord").split("\n");

			for(int counter = 0; counter < searchKeys.length; counter++) {
				homePage.searchPanel.setSearchWord(searchKeys[counter], true);
				homePage.searchPanel.clickSearch();
				Utils.fluentWait(driver);
			}

			Log.message("2. Create some search history");

			//3. Click the Clear History option and confirm the action
			//---------------------------------------------------------
			homePage.searchPanel.clearHistory();
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			MFilesDialog mFilesDialog = new MFilesDialog(driver);
			mFilesDialog.confirmDeleteHistory();
			Utils.fluentWait(driver);

			Log.message("3. Click the Clear History option and confirm the action.");

			//Verification: To verify if the history has been cleared
			//--------------------------------------------------------
			List<String> searchWords = homePage.searchPanel.getSearchHistory();

			if(searchWords.size() == 0)
				Log.pass("Test Case Passed. History is cleared as expected.");
			else
				Log.fail("Test Case Failed. History is not cleared.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 100.1.6 : Ensure whether the search keyword is not listed for the mentioned steps
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint100", "Comment","SearchWordOperations"}, 
			description = "Ensure whether the search keyword is not listed for the mentioned steps.")
	public void SprintTest100_1_6(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Navigate to a view and Set a search word
			//---------------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("View"));
			Utils.fluentWait(driver);

			homePage.searchPanel.setSearchWord(dataPool.get("SearchWord"));
			Utils.fluentWait(driver);

			Log.message("2. Navigate to a view and Set a search word.");

			//3. Return to home view
			//-----------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);
			Utils.fluentWait(driver);

			Log.message("3. Return to home view.");

			//4. Enter the first letter of the search word used earlier
			//----------------------------------------------------------
			homePage.searchPanel.setSearchWord(Character.toString(dataPool.get("SearchWord").charAt(0)), true);
			Utils.fluentWait(driver);

			Log.message("4. Enter the first letter of the search word used earlier");

			//Verification: To verify if the history has been cleared
			//--------------------------------------------------------
			List<String> searchWords = homePage.searchPanel.getSearchHistory();

			if(searchWords.size() == 0)
				Log.pass("Test Case Passed. The earlier Searchword was not displayed as expected.");
			else
				Log.fail("Test Case Failed. The earlier Searchword was displayed.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}


}


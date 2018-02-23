package MFClient.Tests;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.Utils;
import genericLibrary.WebDriverUtils;

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
import MFClient.Wrappers.FileTransferDialog;
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.PreviewPane;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Test
@Listeners(EmailReport.class)

public class FileTransfer {
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
	@BeforeClass
	public void init() throws Exception {

		try {

			xlTestDataWorkBook = "FileTransfer.xls";
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
	 * TestCaseID : 72_1_1
	 * <br>Description : MFWA-Verify 'File transfer' item is always available in the task pane area</br>
	 * @param dataProvider
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint72"}, 
			description = "Check the File transfer option is available in task pane for the common views")
	public void SprintTest72_1_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;
		try {
			//1. Login to the Home View.


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.", driver);

			//Step-2 : Navigate to Common View
			homePage.listView.navigateThroughView(dataPool.get("ViewName"));
			String viewToNavigate =  dataPool.get("ViewName");
			Log.message("2. Navigated to '" + viewToNavigate + "' view.", driver); 

			//Step-3 : Verifies if File transfer option is available in task pane
			if(homePage.taskPanel.isItemExists(dataPool.get("CheckItem")))
				Log.pass("Test Case Passed. The File transfer link is displayed in task pane for" + viewToNavigate + " view.", driver);
			else
				Log.fail("Test Case Failed. The File transfer link is not displayed in task pane for" + viewToNavigate + " view.", driver);
		}//End try

		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {

			Utility.quitDriver(driver);
		} //End finally
	}//End test case 72_1_1

	/**
	 * TestCaseID : 72_1_2
	 * <br>Description : MFWA-Verify 'File transfer' item is always available in operation menu</br>
	 * @param dataProvider
	 * @param context
	 * @throws Exception
	 */	
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint72"}, 
			description = "Check the File transfer option is available in opertion menu for the common views")
	public void SprintTest72_1_2(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {
			//1. Login to the Home View.


			driver = WebDriverUtils.getDriver(driverType,2);

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.", driver);

			//Step-2 : Navigate to any common Views
			homePage.listView.navigateThroughView(dataPool.get("ViewName"));
			String viewToNavigate =  dataPool.get("ViewName");
			Log.message("2. Navigated to '" + viewToNavigate + "' view.", driver); 

			//Step-3 : Verifies if File transfer option is available in Operation menu
			if(!homePage.menuBar.IsOperationMenuItemExists(dataPool.get("CheckItem")))
				Log.pass("Test Case Passed. The File transfer link is displayed in operation menu for" + viewToNavigate + " view.", driver);
			else
				Log.fail("Test Case Failed. The File transfer link is not displayed in operation menu for" + viewToNavigate + " view.", driver);
		}//End try

		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {

			Utility.quitDriver(driver);

		} //End finally
	}//End test case 72_1_2


	/**
	 * TestCaseID : 72_1_3
	 * <br>Description : MFWA-Verify 'File Transfer' is available in task pane after selecting a object in a list view</br>
	 * @param dataProvider
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint72"}, 
			description = "Check the File transfer option is available in task pane after selecting any existing object")
	public void SprintTest72_1_3(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Log.message("1. Logged into the Home View.", driver);

			//Step-2 : Navigate to any common Views
			SearchPanel searchpanel = new SearchPanel(driver);
			searchpanel.clickSearchBtn(driver);
			Log.message("2. To perform all object search.", driver); 

			//Step-3 : Select any existing object
			homePage.listView.clickItemByIndex(0);
			Utils.fluentWait(driver);
			Log.message("3. Select any existing object.", driver);

			//Step-4 : Verifies if File transfer option is available in task pane
			if(homePage.taskPanel.isItemExists(dataPool.get("CheckItem")))
				Log.pass("Test Case Passed. The File transfer link is displayed in task pane after selecting any existinig object.", driver);
			else
				Log.fail("Test Case Failed. The File transfer link is not displayed in task pane after selecting any existinig object.", driver);
		}//End try

		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {

			Utility.quitDriver(driver);
		} //End finally
	}//End test case 72_1_3


	/**
	 * TestCaseID : 72_1_4
	 * <br>Description : MFWA-Verify 'File Transfer' is available in operation menu after selecting a object in a list view</br>
	 * @param dataProvider
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint72"}, 
			description = "Check the File transfer option is available in operation menu after selecting any existing object")
	public void SprintTest72_1_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {			
			//1. Login to the Home View.


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("1. Logged into the Home View.", driver);

			//Step-2 : To perform all object search
			SearchPanel searchpanel = new SearchPanel(driver);
			searchpanel.clickSearchBtn(driver);
			Log.message("2. To perform all object search.", driver); 

			//Step-3 : Select any existing object
			homePage.listView.clickItemByIndex(0);
			Utils.fluentWait(driver);
			Log.message("3. Select any existing object.", driver);

			//Step-4 : Verifies if File transfer option is available in operation menu
			if(!homePage.menuBar.IsOperationMenuItemExists(dataPool.get("CheckItem")))
				Log.pass("Test Case Passed. The File transfer link is displayed in opertion menu after selecting any existinig object.", driver);
			else
				Log.fail("Test Case Failed. The File transfer link is not displayed in operation menu after selecting any existinig object.", driver);
		}//End try

		catch(Exception e) {
			Log.exception(e, driver);

		}//End catch

		finally {

			Utility.quitDriver(driver);
		} //End finally
	}//End test case 72_1_4

	/**
	 * TestCaseID : 72_1_5
	 * <br>Description : Verify 'File transfer' dialog displayed after clicking File transfer control option form task pane 
	 * @param dataProvider
	 * @param context
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint72"}, 
			description = "Check 'File transfer' dialog displayed after clicking File transfer control option form task pane")
	public void SprintTest72_1_5(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : Navigate to Home View
			homePage.listView.navigateThroughView(dataPool.get("ViewName"));
			String viewToNavigate =  dataPool.get("ViewName");
			Log.message("1. Navigated to '" + viewToNavigate + "' view.", driver); 

			//Step-2 : Click File transfer option from task pane area
			homePage.taskPanel.clickItem(dataPool.get("CheckItem"));
			Utils.fluentWait(driver);
			Log.message("2. '" + dataPool.get("CheckItem") + "' option clicked form task pane.", driver); 
			FileTransferDialog filetransfer = new FileTransferDialog(driver);	
			Utils.fluentWait(driver);

			//Verifies if File transfer dialog is open
			if(filetransfer.isFileTransferDialogDisplayed())
				Log.pass("Test Case Passed. The File transfer dialog is displayed after clicking File transfer control option form task pane.", driver);
			else
				Log.fail("Test Case Failed. The File transfer dialog is not displayed after clicking File transfer control option form task pane.", driver);
		}//End try

		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {

			Utility.quitDriver(driver);
		} //End finally
	}//End test case 72_1_5

	/**
	 * TestCaseID : 72_1_6
	 * <br>Description : Verify 'File transfer' dialog displayed after clicking on right pane tabs
	 * @param dataProvider
	 * @param context
	 * @throws Exception
	 */	
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint72"}, 
			description = "Check 'File transfer' dialog displayed after clicking File transfer control option form operation menu")
	public void SprintTest72_1_6(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;
		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : To perform all object search
			SearchPanel searchpanel = new SearchPanel(driver);
			searchpanel.clickSearchBtn(driver);
			Log.message("1. To perform all object search.", driver); 

			//Step-2 : Click File transfer option from task pane area
			homePage.taskPanel.clickItem(dataPool.get("CheckItem"));
			Utils.fluentWait(driver);

			//Step-3 : Check the File transfer dialog is displayed
			FileTransferDialog filetransfer = new FileTransferDialog(driver);			
			if(!filetransfer.isFileTransferDialogDisplayed())
				throw new Exception("File transfer dialog is not get open.");
			Log.message("2.  File transfer dialog is displayed.", driver);

			//Step-4 : Click on meta data tab in right pane
			PreviewPane preview = new PreviewPane(driver);
			preview.clickPreviewPaneTabs(dataPool.get("tabname"));
			Log.message("3. Clicked on metadata tab from right pane", driver);

			//Step-5 : Verifies if File transfer dialog is open
			if(!filetransfer.isFileTransferDialogDisplayed())
				Log.pass("Test Case Passed. The File transfer dialog is closed after clicking on right pane metadata tab. ", driver);
			else
				Log.fail("Test Case Failed. The File transfer dialog is not closed after clicking on right pane metadata tab.",driver);
		} // End try

		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {

			Utility.quitDriver(driver);
		} //End finally
	}//End test case 72_1_6

	/**
	 * TestCaseID : 72_1_7
	 * <br>Description : Verify 'File transfer' dialog displayed after clicking on file transfer dialog 
	 * @param dataProvider
	 * @param context
	 * @throws Exception
	 */	
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint72"}, 
			description = " Check 'File transfer' dialog displayed after clicking on file transfer dialog ")
	public void SprintTest72_1_7(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;
		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : To perform all object search
			SearchPanel searchpanel = new SearchPanel(driver);
			searchpanel.clickSearchBtn(driver);
			Log.message("1. To perform all object search.", driver); 

			//Step-2 : Click File transfer option from task pane area
			homePage.taskPanel.clickItem(dataPool.get("CheckItem"));
			Utils.fluentWait(driver);
			Log.message("2. Select first item in the search list", driver); 

			//Step-3 : Check the File transfer dialog is displayed
			FileTransferDialog filetransfer = new FileTransferDialog(driver);			
			if(!filetransfer.isFileTransferDialogDisplayed())
				throw new Exception("File transfer dialog is not get open.");
			Log.message("3.  File transfer dialog is displayed.", driver);

			//Step-4 : Click on meta data tab in right pane
			filetransfer.clickOnDialog();
			Log.message("4.  Clicked on Filetransfer dialog.", driver);

			//Step-5 : Verifies if File transfer dialog is open
			if(filetransfer.isFileTransferDialogDisplayed())
				Log.pass("Test Case Passed.The File transfer dialog is displayed after clicking on file transfer dialog.", driver);
			else
				Log.fail("Test Case Failed.The File transfer dialog is not displayed after clicking on file transfer dialog.", driver);
		} // End try

		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {

			Utility.quitDriver(driver);
		} //End finally
	}//End test case 72_1_7

	/**
	 * TestCaseID : 72_1_8
	 * <br>Description : Verify top right corner "X" button is  displayed in 'File transfer' dialog  
	 * @param dataProvider
	 * @param context
	 * @throws Exception
	 */	
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint72"}, 
			description = " Check  X (close) button is  displayed in 'File transfer' dialog ")
	public void SprintTest72_1_8(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			//Step-1 : Navigate to Home View
			homePage.listView.navigateThroughView(dataPool.get("ViewName"));
			String viewToNavigate =  dataPool.get("ViewName");
			Log.message("1. Navigated to '" + viewToNavigate + "' view.", driver); 

			//Step-2 : Click File transfer option from task pane area
			homePage.taskPanel.clickItem(dataPool.get("CheckItem"));
			Utils.fluentWait(driver);
			Log.message("2 : Click File transfer option from task pane area.", driver);

			//Step-3 : Verifies if File transfer dialog is open
			FileTransferDialog filetransfer = new FileTransferDialog(driver);			
			if(!filetransfer.isFileTransferDialogDisplayed())
				throw new Exception("File transfer dialog is not get open.");
			Log.message("3 : File transfer dialog is displayed.", driver);

			//Step-4 : Verifies if cross 'X' icon is available in File transfer dialog
			//--------------------------------------------------------------------------
			if(filetransfer.isCrossIconDisplayed())
				Log.pass("Test Case Passed. Close 'X' icon is available in File transfer dialog.", driver);
			else
				Log.fail("Test Case Failed. Close 'X' icon is not available in File transfer dialog", driver);
		} // End try

		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {

			Utility.quitDriver(driver);
		} //End finally
	}//End test case 72_1_8


	/**
	 * TestCaseID : 72_1_10
	 * <br>Description : Verify user able to drag the 'File transfer' dialog 
	 * @param dataProvider
	 * @param context
	 * @throws Exception
	 * @user 'Saraswathi'
	 */	
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint72"}, 
			description = "Verify user able to drag the 'File transfer' dialog in MFWA")
	public void SprintTest72_1_10(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("Safari") || driverType.equalsIgnoreCase("IE"))
			throw new SkipException(driverType.toUpperCase() + " driver does not support key actions.");

		driver = null;

		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			//Step-1 : Navigate to Home View
			homePage.listView.navigateThroughView(dataPool.get("ViewName"));
			Log.message("1. Navigated to '" + dataPool.get("ViewName") + "' view.", driver); 

			//Step-2 : Click File transfer option from task pane area
			homePage.taskPanel.clickItem(dataPool.get("CheckItem"));
			Utils.fluentWait(driver);
			Log.message("2 : Click File transfer option from task pane area.", driver);

			//Step-3 : Verifies if File transfer dialog is open
			FileTransferDialog filetransfer = new FileTransferDialog(driver);				
			if(!filetransfer.isFileTransferDialogDisplayed())
				throw new Exception("File transfer dialog is not get open.");
			Log.message("3 : File transfer dialog is displayed.", driver);

			//Step-4 : Try to drag & drop the file transfer dialog
			if(!filetransfer.dragFileTransferDialog())
				Log.pass("Test Case Passed. The File transfer dialog is not able to drag in MFWA.", driver);
			else
				Log.fail("Test Case Failed. The File transfer dialog is able to drag the dialog.", driver);
		} // End try

		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {

			Utility.quitDriver(driver);
		} //End finally
	}//End test case 72_1_10


	/**
	 * TestCaseID : 72_1_11
	 * <br>Description : MFWA-Verify default contents file transfer dialog
	 * @param dataProvider
	 * @param context
	 * @throws Exception
	 */	
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint72"}, 
			description = "Check the default contents displayed in file transfer dialog")
	public void SprintTest72_1_11(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);


			//Step-1 : Navigate to Home View
			homePage.listView.navigateThroughView(dataPool.get("ViewName"));
			Log.message("1. Navigated to '" + dataPool.get("ViewName") + "' view.", driver); 

			//Step-2 : Click File transfer option from task pane area
			homePage.taskPanel.clickItem(dataPool.get("CheckItem"));
			Utils.fluentWait(driver);
			Log.message("2 : Click File transfer option from task pane area.", driver);

			//Step-3 : Verifies if File transfer dialog is open
			FileTransferDialog filetransfer = new FileTransferDialog(driver);			
			if(!filetransfer.isFileTransferDialogDisplayed())
				throw new Exception("File transfer dialog is not get open.");
			Log.message("3 : File transfer dialog is displayed.", driver);

			//Step-4 : Check the progress dialog default texts
			String uploadtext = filetransfer.getProgressText(dataPool.get("UploadText"));
			String downloadtext = filetransfer.getProgressText(dataPool.get("DownloadText"));
			if(uploadtext.equalsIgnoreCase(dataPool.get("text")) && downloadtext.equalsIgnoreCase(dataPool.get("text")))
				Log.pass("Test Case Passed. Default contents are displayed properly in File Transfer Dialog.", driver);
			else
				Log.fail("Test Case Failed. Default contents are not displayed properly in File Transfer Dialog.", driver);

		} // End try

		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {

			Utility.quitDriver(driver); 
		} //End finally
	}//End test case 72_1_11

	/**
	 * TestCaseID : 72_1_12
	 * <br>Description : MFWA-Verify the Download object title in file transfer dialog </br>
	 * @param dataProvider
	 * @param context
	 * @throws Exception
	 */	
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint72", "SKIP_JavaApplet"}, 
			description = "  Check the Download object title in file transfer dialog")
	public void SprintTest72_1_12(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {


			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1 : To perform all object search
			SearchPanel searchpanel = new SearchPanel(driver);
			searchpanel.clickSearchBtn(driver);
			Log.message("1. To perform all object search.", driver); 

			//Step-2 : Select any existing object
			homePage.listView.clickItemByIndex(0);
			Utils.fluentWait(driver);
			String selecteddocument = homePage.listView.getItemNameByItemIndex(0);
			Utils.fluentWait(driver);
			Log.message("2. Select any existing object.", driver);

			//Step-3 : Click  option from task pane area
			homePage.taskPanel.clickItem(dataPool.get("CheckItem1"));
			Utils.fluentWait(driver);
			Log.message("3 : Click Open (Download) option from task pane area.", driver);

			//Step-4 : Click 'Checkout' option from MFiles dialog
			MFilesDialog mfilesdialog = new MFilesDialog(driver);
			mfilesdialog.clickCheckOutButton(); 
			Utils.fluentWait(driver);
			Log.message("4 : Click 'Checkout' option from MFiles dialog.", driver);

			//Step-5 : Click File transfer option from task pane area
			homePage.taskPanel.clickItem(dataPool.get("CheckItem2"));
			Utils.fluentWait(driver);
			Log.message("5 : Click File transfer option from task pane area.", driver);

			//Step-6 : Verifies if File transfer dialog is open
			FileTransferDialog filetransfer = new FileTransferDialog(driver);			
			if(!filetransfer.isFileTransferDialogDisplayed())
				throw new Exception("File transfer dialog is not get open.");
			Log.message("6 : File transfer dialog is displayed.", driver);

			//Step-7 : Check the progress dialog  texts
			String downloadtext = filetransfer.getProgressText(dataPool.get("DownloadText"));
			if(downloadtext.equalsIgnoreCase(selecteddocument))
				Log.pass("Test Case Passed. The downloaded object (" + selecteddocument + ") title is dispayed in File transfer dialog.", driver);
			else
				Log.fail("Test Case Failed. The downloaded object (" + selecteddocument + ") title is not dispayed in File transfer dialog.", driver);
		} // End try

		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {

			Utility.quitDriver(driver);
		} //End finally
	}//End test case 72_1_12


}
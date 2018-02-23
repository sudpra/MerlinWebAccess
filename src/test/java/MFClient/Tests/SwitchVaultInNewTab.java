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
import MFClient.Wrappers.Caption;
import MFClient.Wrappers.ListView;
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.Utility;


@Listeners(EmailReport.class)

public class SwitchVaultInNewTab {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String userName = null;
	public static String userFullName = null;
	public static String password = null;
	public static String testVault = null;
	public static String configURL = null;
	public static WebDriver driver = null;
	public static String productVersion = null;
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
			className = this.getClass().getSimpleName().toString().trim();
			if (xmlParameters.getParameter("driverType").equalsIgnoreCase("IE"))
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim() + xmlParameters.getParameter("driverVersion").trim();
			else
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim();
			Utility.restoreTestVault();
			Utility.configureUsers(xlTestDataWorkBook);
			/*	Utility.restoreTestVault();*/

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

			Utility.destroyTestVault();

		}//End try

		catch(Exception e){
			throw e;
		}//End Catch
	}

	/**
	 * 107.16.1 : Verify if all vault list display when clicking username in search pane area.
	 */
	@Test( groups = {"Sprint107", "SwitchVaultInNewTab"}, 
			description = "Verify if all vault list display when clicking username in search pane area.")
	public void SprintTest107_16_1() throws Exception {

		driver = WebDriverUtils.getDriver();

		try {	

			//Logged into MFWA with valid creadentials
			//----------------------------------------
			LoginPage loginPage = new LoginPage(driver); 
			HomePage homePage = LoginPage.loginIntoWebApplication(driver, true); //Logged into webaccess using valid creadentials

			//Step-1 : Get the Listed vault login page
			//-----------------------------------------
			String[] vaultList = loginPage.getVaultList();//Get the vault list from the 

			Log.message("1. Get vault listed after logged into MFWA.", driver);

			//Step-2 : Select the specified vault from the vault list 
			//-------------------------------------------------------
			int availableVaults = vaultList.length;//Get the count for displayed vaults
			loginPage.selectVault(testVault);//Selected the specified vault in test data

			Log.message("2. Selected the '" + testVault + "' from the vault list and Navigated ot the homepage.");

			//Step-3 : Get the vault lists from the menu bar
			//----------------------------------------------
			homePage.menuBar.selectMenuItemVault();//Select the 'Vaults' options in menubar
			String[] vaultName = homePage.menuBar.getVaultList();//Get the vaultname listed in the menubar
			int vaultCount = vaultName.length;//Get the count for displayed vaults	  		

			Log.message("3. Get the all Vaults from the vault listed in menubar.");

			//Check if vaults displayed in menubar and vault list as same
			//-----------------------------------------------------------
			if (!(availableVaults == vaultCount))
				throw new Exception("Vaults are not displayed same as the vault lists.");

			boolean flag = false;//Initiate the flag 

			for(int itemIdx=0;itemIdx<availableVaults;itemIdx++)
				if(vaultName[itemIdx].equalsIgnoreCase(vaultList[itemIdx])){//Check if all vaults are listed correctly while clicking username 
					System.out.println(vaultName[itemIdx]);
					flag = true;
				}//End if

			//Verification : Verify if all vaults are displayed while selecting the username from the menu bar
			//------------------------------------------------------------------------------------------------
			if(flag == true)
				Log.pass("Test Case Passed.All Vaults are displayed when clicking the user name in search area.");
			else
				Log.fail("Test Case Failed. Vaults are not displayed as expeceted.", driver);

		}//End try
		catch(Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End SprintTest107_16_1

	/**
	 * 107_16_3 : Verify if user able to click on another vault which is displayed in vault list
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"Sprint105", "SwitchVaultInNewTab"}, description = "Verify if user able to click on another vault which is displayed in vault list.")
	public void SprintTest107_16_3(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = WebDriverUtils.getDriver();
		String vault_Tab = driver.getWindowHandle();//Navigate to new window

		try {	
			//Logged into MFWA with valid creadentials
			//----------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			LoginPage loginPage = new LoginPage(driver); 
			HomePage homePage = LoginPage.loginIntoWebApplication(driver, true); //Logged into webaccess using valid creadentials

			//Step-1 : Get the Listed vault login page
			//-----------------------------------------
			String[] vaultList = loginPage.getVaultList();//Get the vault list after the web page login 

			Log.message("1. Get vault listed after logged into MFWA.", driver);

			//Step-2 : Select the specified vault from the vault list 
			//-------------------------------------------------------
			int availableVaults = vaultList.length;//Get the count for displayed vaults
			loginPage.selectVault(testVault);//Selected the specified vault in test data

			Log.message("2. Selected the '" + testVault + "' from the vault list and Navigated ot the homepage.");

			//Step-3 : Get the vault lists from the menu bar
			//----------------------------------------------
			homePage.menuBar.selectMenuItemVault();//Select the 'Vaults' options in menubar
			String[] vaultName = homePage.menuBar.getVaultList();//Get the vaultname listed in the menubar
			int vaultCount = vaultName.length;//Get the count for displayed vaults	  		

			Log.message("3. Get the all Vaults from the vault listed in menubar.");

			//Check if vaults displayed in menubar and vault list as same
			//-----------------------------------------------------------
			if (!(availableVaults == vaultCount))//Checl if vault list displayed as same in login page and menu bar
				throw new Exception("Vaults are not displayed as expected.");

			//Step-4 : Select the vault in menu bar
			//-------------------------------------
			homePage.menuBar.selectVault(dataPool.get("Vault"));//Selected the vaults to be clicked in vaults option 
			for (String vault_NewTab : driver.getWindowHandles())//Open the vault in new tab
				driver.switchTo().window(vault_NewTab);

			Log.message("4. Selected vault : '"+ dataPool.get("Vault") + "' is opened in new tab.", driver);

			String expectedVault = homePage.menuBar.getVaultNameInBreadCrumb();//Get the vaultname which displayed in breadcrumb

			//Verification : Verify if Selected vault is opened in new tab
			//------------------------------------------------------------
			if(expectedVault.trim().equalsIgnoreCase(dataPool.get("Vault")))
				Log.pass("Test Case Passed. Selected vault from vault list is displayed in another tab.");
			else
				Log.fail("Test Case Failed.Selected vault from vault list is not displayed in another tab.", driver);

		}//End try
		catch (Exception e) { 
			Log.exception(e, driver);
		}//End catch
		finally { 
			driver.close();//Close the another tab in webpage 
			driver.switchTo().window(vault_Tab);//Switch the window in existing webpage
			Utility.quitDriver(driver);
		}//End finally
	}//End SprintTest107_16_3

	/**
	 * 107.16.6 : Verify if user able to view Vault command in task pane.
	 */
	@Test( groups = {"Sprint107", "SwitchVaultInNewTab"}, description = "Verify if user able to view Vault command in task pane.")
	public void SprintTest107_16_6() throws Exception {

		driver = WebDriverUtils.getDriver();

		try {	
			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into MFWA and navigate to home page.");


			if(homePage.taskPanel.isItemExists(Caption.Taskpanel.Vaults.Value))
				Log.message("Test Case Passed. User able view the 'Vault' command is displayed in taskpane.");
			else
				Log.fail("Test Case Failed.'Vault' command is not displayed in taskpane.", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End SprintTest107_16_6

	/**
	 * 107_16_7 : Verify if vault list is display when clicking on vault command in task pane.
	 *  
	 */
	@Test(groups = {"Sprint105", "SwitchVaultInNewTab"}, description = "Verify if vault list is display when clicking on vault task pane command.")
	public void SprintTest107_16_7() throws Exception {

		driver = WebDriverUtils.getDriver();

		try{

			//Logged into MFWA with valid credentials
			//----------------------------------------
			LoginPage loginPage = new LoginPage(driver); 
			HomePage homePage = LoginPage.loginIntoWebApplication(driver, true); //Logged into webaccess using valid creadentials

			//Step-1 : Get the Listed vault login page
			//-----------------------------------------
			String[] vaultList = loginPage.getVaultList();//Get the vault list after the web page login 

			Log.message("1. Get vault listed after logged into MFWA.", driver);

			//Step-2 : Select the specified vault from the vault list 
			//-------------------------------------------------------
			int availableVaults = vaultList.length;//Get the count for displayed vaults
			loginPage.selectVault(testVault);//Selected the specified vault in test data

			Log.message("2. Selected the '" + testVault + "' from the vault list and Navigated to the homepage.");

			//Step-3 : Click the 'Vault' command from the taskpane
			//----------------------------------------------------
			homePage.taskPanel.clickItem(Caption.Taskpanel.Vaults.Value);//Click the 'Vaults' command in taskpane

			Log.message("3. Selected the '"+ Caption.Taskpanel.Vaults.Value + "' command from the taskpanel view.");

			//Step-4 : Get the vault displayed after clicking the vaults command in taskpane
			//------------------------------------------------------------------------------
			String[] expectedVaults = homePage.taskPanel.getVaultList();//Get the vault list from the taskpane command
			int vaultCnt = expectedVaults.length;//Get the count for the displayed vaults

			//Check if vaults displayed in taskpane and vault list as same
			//------------------------------------------------------------
			if (!(availableVaults == vaultCnt))//Check if vault lists are same as taskpanel & login webpage
				throw new Exception("Vaults are not displayed same as the vault lists.");

			boolean flag = false;//Set the flag as false

			for(int itemIdx=0;itemIdx<availableVaults;itemIdx++)//Compare the 
				if(expectedVaults[itemIdx].equalsIgnoreCase(vaultList[itemIdx])){//Check if all vaults are listed correctly while clicking username 
					System.out.println(expectedVaults[itemIdx]);
					flag = true;//Set the flag as true when the condition is satisfied
				}//End if

			//Verification : Verify if all vaults are displayed in vault list while clicking the vault command in taskpane.
			//------------------------------------------------------------------------------------------------------------
			if(flag == true)
				Log.pass("Test Case Passed.All Vaults are displayed when clicking the vault command from the taskpanel.");
			else
				Log.fail("Test Case Failed. All Vaults are not displayed when clicking the vault command from the taskpanel.", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End SprintTest107_16_7

	/**
	 * 107_16_8 : Verify if user able to logged into selected vault in new tab
	 * 
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"Sprint105", "SwitchVaultInNewTab"}, description = "Verify if user able to logged into selected vault in new tab.")
	public void SprintTest107_16_8(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = WebDriverUtils.getDriver();
		String vault_Tab = driver.getWindowHandle();//Navigate to new window

		try{
			//Logged into MFWA with valid credentials
			//----------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			LoginPage loginPage = new LoginPage(driver); 
			HomePage homePage = LoginPage.loginIntoWebApplication(driver, true); //Logged into webaccess using valid creadentials

			//Step-1 : Get the Listed vault login page
			//-----------------------------------------
			String[] vaultList = loginPage.getVaultList();//Get the vault list after the web page login 

			Log.message("1. Get vault listed after logged into MFWA.", driver);

			//Step-2 : Select the specified vault from the vault list 
			//-------------------------------------------------------
			int availableVaults = vaultList.length;//Get the count for displayed vaults
			loginPage.selectVault(testVault);//Selected the specified vault in test data

			Log.message("2. Selected the '" + testVault + "' from the vault list and Navigated to the homepage.");

			//Step-3 : Click the 'Vault' command from the taskpane
			//----------------------------------------------------
			homePage.taskPanel.clickItem(Caption.Taskpanel.Vaults.Value);//Click the 'Vaults' command in taskpane

			Log.message("3. Selected the '"+ Caption.Taskpanel.Vaults.Value + "' command from the taskpanel view.");

			//Step-4 : Get the vault displayed after clicking the vaults command in taskpane
			//------------------------------------------------------------------------------
			String[] expectedVaults = homePage.taskPanel.getVaultList();//Get the vault list from the taskpane command
			int vaultCnt = expectedVaults.length;//Get the count for the displayed vaults

			//Check if vault displayed while selecting the vault command from taskpane  and vault list as same
			//------------------------------------------------------------
			if (!(availableVaults == vaultCnt))//Check if vault lists are same as taskpanel & login webpage
				throw new Exception("Vaults are not displayed same as the vault lists.");

			//Step-4 : Select the specified vault in taskpane
			//-----------------------------------------------
			homePage.taskPanel.selectVault(dataPool.get("Vault"));//Selected the vaults to be clicked in vaults option 
			for (String vault_NewTab : driver.getWindowHandles())//Open the vault in new tab
				driver.switchTo().window(vault_NewTab);//Navigate to vault in new tab

			Log.message("4. Selected vault : '"+ dataPool.get("Vault") + "' is opened in new tab.", driver);

			String expectedVault = homePage.menuBar.getVaultNameInBreadCrumb();//Get the vaultname which displayed in breadcrumb

			//Verification : Verify if Selected vault is opened in new tab
			//------------------------------------------------------------
			if(expectedVault.trim().equalsIgnoreCase(dataPool.get("Vault")))
				Log.pass("Test Case Passed. Selected vault from vault list is displayed in another tab.");
			else
				Log.fail("Test Case Failed.Selected vault from vault list is not displayed in another tab.", driver);

		}
		catch(Exception e){
			Log.exception(e, driver);
		}
		finally {
			driver.close();//Close the another tab in webpage 
			driver.switchTo().window(vault_Tab);//Switch the window in existing webpage
			Utility.quitDriver(driver);
		}//End finally
	}//End SprintTest107_16_8

	/**
	 * 107_16_9 : Verify if user logged into two vaults at same time and perform the object creation
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"Sprint105", "SwitchVaultInNewTab"}, description = "Verify if user logged into two vaults at same time and perform the object creation.")
	public void SprintTest107_16_9(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = WebDriverUtils.getDriver();
		String vault_Tab = driver.getWindowHandle();//Navigate to new window with vault

		try {

			//Logged into MFWA with valid credentials
			//----------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			LoginPage loginPage = new LoginPage(driver); 
			HomePage homePage = LoginPage.loginIntoWebApplication(driver, true); //Logged into webaccess using valid creadentials

			//Step-1 : Get the Listed vault login page
			//-----------------------------------------
			String[] vaultList = loginPage.getVaultList();//Get the vault list after the web page login 

			Log.message("1. Get vault listed after logged into MFWA.", driver);

			//Step-2 : Select the specified vault from the vault list 
			//-------------------------------------------------------
			int availableVaults = vaultList.length;//Get the count for displayed vaults
			loginPage.selectVault(testVault);//Selected the specified vault in test data

			Log.message("2. Selected the '" + testVault + "' from the vault list and Navigated to the homepage.");

			//Step-3 : Click the 'Vault' command from the taskpane
			//----------------------------------------------------
			homePage.taskPanel.clickItem(Caption.Taskpanel.Vaults.Value);//Click the 'Vaults' command in taskpane

			Log.message("3. Selected the '"+ Caption.Taskpanel.Vaults.Value + "' command from the taskpanel view.");

			//Step-4 : Get the vault displayed after clicking the vaults command in taskpane
			//------------------------------------------------------------------------------
			String[] expectedVaults = homePage.taskPanel.getVaultList();//Get the vault list from the taskpane command
			int vaultCnt = expectedVaults.length;//Get the count for the displayed vaults in vault list

			Log.message("4. Get the all vaults displayed from the vault list.");

			//Check if vault displayed while selecting the vault command from taskpane and vault list as same
			//-----------------------------------------------------------------------------------------------
			if (!(availableVaults == vaultCnt))//Check if vault lists are same as taskpanel & login webpage
				throw new Exception("Vaults are not displayed as same as the vault lists.");

			//Step-5 : Select the specified vault in new tab using the 'Vaults' command in task pane  
			//--------------------------------------------------------------------------------------
			homePage.taskPanel.selectVault(dataPool.get("Vault"));//Selected the vaults to be clicked in vaults option 
			for (String vault_NewTab : driver.getWindowHandles())//Open the vault in new tab
				driver.switchTo().window(vault_NewTab);//Navigate to vault in new tab

			Log.message("5. Selected vault : '"+ dataPool.get("Vault") + "' is opened in new tab.", driver);


			String expectedVault = homePage.menuBar.getVaultNameInBreadCrumb();//Get the vaultname which displayed in breadcrumb

			//Check if vault displayed in new tab is same as the specified vault
			//------------------------------------------------------------------
			if(!(expectedVault.trim().equalsIgnoreCase(dataPool.get("Vault"))))
				throw new Exception("Specified vault is not displayed in new tab.");

			//Step-6 : Navigate to the first tab and Create an object
			//--------------------------------------------------------
			driver.switchTo().window(vault_Tab);//Switch the window in new tab

			Log.message("6. Navigate to first tab with '" + testVault + "' vault.");

			//Step-7 : Create a new customer object in existing vault 
			//-------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Customer.Value);

			Log.message("7. Created the new Customer object through taskpane.");

			//Step-8 : Instantiate the metadata card and set the metadata card property
			//-------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); // Initialize new metadata card.
			metadataCard.setInfo(dataPool.get("Property")+dataPool.get("Object"));
			metadataCard.setOpenForEditing(false); // Disable open for editing option in new metadata card
			metadataCard.setCheckInImmediately(true);//Set the check in immediately check box
			metadataCard.saveAndClose();//Save the metadata card

			Log.message("8. Set the Properties to object and save the metadata card.",driver);

			//Verification : Verify if the specified object is created in first tab
			//---------------------------------------------------------------------
			if(homePage.listView.isItemExists(dataPool.get("Object")))
				Log.pass("Test Case Passed.Specified object '"+ dataPool.get("Object") + "' is created successfully in first tab and user able to access the two vaults.");
			else
				Log.fail("Test Case Failed.Object '"+ dataPool.get("Object") + "' is not created.", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End SprintTest107_16_9


	/**
	 * 107_16_30 : Verify if user is able to see the history view while navigate to another vault  
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"Sprint107", "SwitchVaultInNewTab"}, description = "Verify if user is able to see the history view while navigate to another vault.")
	public void SprintTest107_16_30(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = WebDriverUtils.getDriver();
		String vault_Tab = driver.getWindowHandle();//Navigate to new window with vault

		try{

			//Logged into MFWA with valid credentials
			//----------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			LoginPage loginPage = new LoginPage(driver); 
			HomePage homePage = LoginPage.loginIntoWebApplication(driver, true); //Logged into webaccess using valid creadentials

			//Step-1 : Get the Listed vault login page
			//-----------------------------------------
			String[] vaultList = loginPage.getVaultList();//Get the vault list after the web page login 

			Log.message("1. Get vault listed after logged into MFWA.", driver);

			//Step-2 : Select the specified vault from the vault list 
			//-------------------------------------------------------
			int availableVaults = vaultList.length;//Get the count for displayed vaults
			loginPage.selectVault(testVault);//Selected the specified vault in test data

			Log.message("2. Selected the '" + testVault + "' from the vault list and Navigated to the homepage.");

			//Step-3 : Click the 'Vault' command from the taskpane
			//----------------------------------------------------
			homePage.taskPanel.clickItem(Caption.Taskpanel.Vaults.Value);//Click the 'Vaults' command in taskpane

			Log.message("3. Selected the '"+ Caption.Taskpanel.Vaults.Value + "' command from the taskpanel view.");

			//Step-4 : Get the vault displayed after clicking the vaults command in taskpane
			//------------------------------------------------------------------------------
			String[] expectedVaults = homePage.taskPanel.getVaultList();//Get the vault list from the taskpane command
			int vaultCnt = expectedVaults.length;//Get the count for the displayed vaults in vault list

			Log.message("4. Get the all vaults displayed from the vault list.");

			//Check if vault displayed while selecting the vault command from taskpane and vault list as same
			//-----------------------------------------------------------------------------------------------
			if (!(availableVaults == vaultCnt))//Check if vault lists are same as taskpanel & login webpage
				throw new Exception("Vaults are not displayed as same as the vault lists.");

			//Step-5 : Select the specified vault in new tab using the 'Vaults' command in task pane  
			//--------------------------------------------------------------------------------------
			homePage.taskPanel.selectVault(dataPool.get("Vault"));//Selected the vaults to be clicked in vaults option 
			for (String vault_NewTab : driver.getWindowHandles())//Open the vault in new tab
				driver.switchTo().window(vault_NewTab);//Navigate to vault in new tab

			Log.message("5. Selected vault : '"+ dataPool.get("Vault") + "' is opened in new tab.", driver);


			String expectedVault = homePage.menuBar.getVaultNameInBreadCrumb();//Get the vaultname which displayed in breadcrumb

			//Check if vault displayed in new tab is same as the specified vault
			//------------------------------------------------------------------
			if(!(expectedVault.trim().equalsIgnoreCase(dataPool.get("Vault"))))
				throw new Exception("Specified vault is not displayed in new tab.");

			//Step-6 : Select the 'Relationships' option in taskpane
			//------------------------------------------------------
			homePage.searchPanel.clickSearch();//Click the search button
			homePage.listView.clickItemByIndex(0);//Select the first object in list view
			homePage.taskPanel.clickItem(Caption.MenuItems.History.Value);//Click the 'Relationships' option in task pane

			Log.message("6. Clicked the 'History' option from the taskpanel.");

			//Verification : Verify if the relationships view is displayed or not in vault open with new tab
			//----------------------------------------------------------------------------------------------
			if(ListView.isHistoryViewOpened(driver))//Verify if relation ship view is displayed in 
				Log.pass("Test Case Passed.History view is displayed in new tab vault.");
			else
				Log.fail("Test Case Failed. History view is not displayed in new tab vault.", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally{
			driver.close();//Close the another tab in webpage 
			driver.switchTo().window(vault_Tab);//Switch the window in existing webpage
			Utility.quitDriver(driver);
		}//End finally
	}//End SprintTest107_16_30


	/**
	 * 107_16_31 : Verify if user is able to see the relationships view while navigate to another vault 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"Sprint107", "SwitchVaultInNewTab"}, description = "Verify if user logged into two vaults at same time and perform the object creation.")
	public void SprintTest107_16_31(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = WebDriverUtils.getDriver();
		String vault_Tab = driver.getWindowHandle();//Navigate to new window with vault

		try{

			//Logged into MFWA with valid credentials
			//----------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			LoginPage loginPage = new LoginPage(driver); 
			HomePage homePage = LoginPage.loginIntoWebApplication(driver, true); //Logged into webaccess using valid creadentials

			//Step-1 : Get the Listed vault login page
			//-----------------------------------------
			String[] vaultList = loginPage.getVaultList();//Get the vault list after the web page login 

			Log.message("1. Get vault listed after logged into MFWA.", driver);

			//Step-2 : Select the specified vault from the vault list 
			//-------------------------------------------------------
			int availableVaults = vaultList.length;//Get the count for displayed vaults
			loginPage.selectVault(testVault);//Selected the specified vault in test data

			Log.message("2. Selected the '" + testVault + "' from the vault list and Navigated to the homepage.");

			//Step-3 : Click the 'Vault' command from the taskpane
			//----------------------------------------------------
			homePage.taskPanel.clickItem(Caption.Taskpanel.Vaults.Value);//Click the 'Vaults' command in taskpane

			Log.message("3. Selected the '"+ Caption.Taskpanel.Vaults.Value + "' command from the taskpanel view.");

			//Step-4 : Get the vault displayed after clicking the vaults command in taskpane
			//------------------------------------------------------------------------------
			String[] expectedVaults = homePage.taskPanel.getVaultList();//Get the vault list from the taskpane command
			int vaultCnt = expectedVaults.length;//Get the count for the displayed vaults in vault list

			Log.message("4. Get the all vaults displayed from the vault list.");

			//Check if vault displayed while selecting the vault command from taskpane and vault list as same
			//-----------------------------------------------------------------------------------------------
			if (!(availableVaults == vaultCnt))//Check if vault lists are same as taskpanel & login webpage
				throw new Exception("Vaults are not displayed as same as the vault lists.");

			//Step-5 : Select the specified vault in new tab using the 'Vaults' command in task pane  
			//--------------------------------------------------------------------------------------
			homePage.taskPanel.selectVault(dataPool.get("Vault"));//Selected the vaults to be clicked in vaults option 
			for (String vault_NewTab : driver.getWindowHandles())//Open the vault in new tab
				driver.switchTo().window(vault_NewTab);//Navigate to vault in new tab

			Log.message("5. Selected vault : '"+ dataPool.get("Vault") + "' is opened in new tab.", driver);


			String expectedVault = homePage.menuBar.getVaultNameInBreadCrumb();//Get the vaultname which displayed in breadcrumb

			//Check if vault displayed in new tab is same as the specified vault
			//------------------------------------------------------------------
			if(!(expectedVault.trim().equalsIgnoreCase(dataPool.get("Vault"))))
				throw new Exception("Specified vault is not displayed in new tab.");

			//Step-6 : Select the 'Relationships' option in taskpane
			//------------------------------------------------------
			homePage.searchPanel.clickSearch();//Click the search button
			homePage.listView.clickItemByIndex(0);//Select the first object in list view
			homePage.taskPanel.clickItem(Caption.MenuItems.Relationships.Value);//Click the 'Relationships' option in task pane

			Log.message("6. Clicked the 'Relationships' option from the taskpanel.");

			//Verification : Verify if the relationships view is displayed or not in vault open with new tab
			//----------------------------------------------------------------------------------------------
			if(ListView.isRelationshipsViewOpened(driver))//Verify if relation ship view is displayed in 
				Log.pass("Test Case Passed. Relationship view is displayed in new tab vault.");
			else
				Log.fail("Test Case Failed. Relationship view is not displayed in new tab vault.", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally{
			driver.close();//Close the another tab in webpage 
			driver.switchTo().window(vault_Tab);//Switch the window in existing webpage
			Utility.quitDriver(driver);
		}//End finally
	}//End SprintTest107_16_31






}//End SwitchVaultInNewTab

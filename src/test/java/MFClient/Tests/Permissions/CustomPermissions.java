package MFClient.Tests.Permissions;


import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.WebDriverUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
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

import MFClient.Pages.ConfigurationPage;
import MFClient.Pages.HomePage;
import MFClient.Pages.LoginPage;
import MFClient.Wrappers.Caption;
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class CustomPermissions {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String userName = null;
	public static String password = null;
	public static String testVault = null;
	public static String driverType = null;
	public String methodName = null;
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
			userName = xmlParameters.getParameter("UserName");
			password = xmlParameters.getParameter("Password");
			driverType = xmlParameters.getParameter("driverType");
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
	 * CustomPermission_26821 : Verify if the object is viewable when it has the permission to edit only.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Custom Permission"},
			description = "Verify if the object is viewable when it has the permission to edit only.")
	public void CustomPermission_26821(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("Username"), dataPool.get("Password"), testVault); //Launched driver and logged in

			Log.message("1. Logged into MFWA as non admin user : "+ dataPool.get("Username") +" which has the edit only permission for " + dataPool.get("ObjectName") + " object.", driver);

			//Step-2 : Navigate to template folder and select any template object in the view
			//--------------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), dataPool.get("SearchWord"));//Navigate to the specific view

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new SkipException(dataPool.get("ObjectName")+" does not exist in the view");

			Log.message("2. Navigated to '" + viewtonavigate + "' view and selected the object("+ dataPool.get("ObjectName") +") in the view.", driver);

			//Step-3: Gets the class property values
			//--------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard in the rightpane

			metadataCard.setPropertyValue(dataPool.get("PropName"), dataPool.get("PropValue"));//Edit the property values & 
			metadataCard.saveAndClose();//Save metadatacard

			String ExpectedResults = "";

			metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard in the rightpane
			metadataCard.clickPermissionField();

			//Verify if user has change the permission for the selected object
			//----------------------------------------------------------------
			if(MFilesDialog.exists(driver, "Permissions"))
				ExpectedResults = "Change Permission dialog is displayed when user has the permission to edit only.";

			//Verify if user has delete permission for selected object
			//--------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectName"));
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Delete.Value);

			MFilesDialog mfilesDialog = new MFilesDialog(driver,"Confirm Delete");

			if(MFilesDialog.exists(driver, "Confirm Delete"))//verify if Confirm Delete dialog is displayed or not
				mfilesDialog.clickOkButton();//Click the 'Yes' button in Confirm Delete dialog


			mfilesDialog = new MFilesDialog(driver,"M-Files Web");

			//Verify if user has delete permission for selected object
			//--------------------------------------------------------
			if(!mfilesDialog.getMessage().equalsIgnoreCase(dataPool.get("WarningMessage")))
				ExpectedResults += "Access denied error message is not displayed when user performing the delete operation which has the permission to edit only.";


			//Verification : Verify if the object is viewable when it has the permission to edit only.
			//---------------------------------------------------------------------------------------
			if(ExpectedResults.equals(""))
				Log.pass("Test Case Passed.Selected object : " + dataPool.get("ObjectName") + " is editable for user had edit only permission &  Change and Delete permission is denied.", driver);
			else
				Log.fail("Test Case Failed.Selected object : " + dataPool.get("ObjectName") + " not is editable for user. Addional Info : " + ExpectedResults, driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try {
				Utility.quitDriver(driver);
			}
			catch(Exception e0) {Log.exception(e0, driver);} //End catch
		}//End finally

	}//End CustomPermission_26821		


	/**
	 * CustomPermission_24715 : Verify Selecting 'properties' option for the denied object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Custom Permission"},
			description = "Verify Selecting 'properties' option for the denied object.")
	public void CustomPermission_24715(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("Username"), dataPool.get("Password"), testVault); //Launched driver and logged in

			Log.message("1. Logged into MFWA as non admin user : "+ dataPool.get("Username") +" which has the edit only permission for " + dataPool.get("ObjectName") + " object.", driver);

			//Step-2 : Navigate to template folder and select any template object in the view
			//--------------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), dataPool.get("SearchWord"));//Navigate to the specific view

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new SkipException(dataPool.get("ObjectName")+" does not exist in the view");

			Log.message("2. Navigated to '" + viewtonavigate + "' view and selected the object("+ dataPool.get("ObjectName") +") in the view.", driver);

			String ExpectedResults = "";


			//Step-3: Gets the class property values
			//--------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard in the rightpane

			metadataCard.clickProperty(dataPool.get("PropName"));

			if(metadataCard.isEditMode())
				ExpectedResults = "Selected object : " + dataPool.get("ObjectName") + " metadatacard is in edit mode when user has read only permission.";


			metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard in the rightpane
			metadataCard.clickPermissionField();

			//Verify if user has change the permission for the selected object
			//----------------------------------------------------------------
			if(MFilesDialog.exists(driver, "Permissions"))
				ExpectedResults = "Change Permission dialog is displayed when user has the permission to edit only.";

			//Verify if user has delete permission for selected object
			//--------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectName"));
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Delete.Value);

			MFilesDialog mfilesDialog = new MFilesDialog(driver,"Confirm Delete");

			if(MFilesDialog.exists(driver, "Confirm Delete"))//verify if Confirm Delete dialog is displayed or not
				mfilesDialog.clickOkButton();//Click the 'Yes' button in Confirm Delete dialog


			mfilesDialog = new MFilesDialog(driver,"M-Files Web");

			//Verify if user has delete permission for selected object
			//--------------------------------------------------------
			if(!mfilesDialog.getMessage().equalsIgnoreCase(dataPool.get("WarningMessage")))
				ExpectedResults += "Access denied error message is not displayed when user performing the delete operation which has the permission to read only.";


			//Verification : Verify if the object is viewable when it has the permission to edit only.
			//---------------------------------------------------------------------------------------
			if(ExpectedResults.equals(""))
				Log.pass("Test Case Passed.Selected object : " + dataPool.get("ObjectName") + " properties are not editable for user had read only permission.", driver);
			else
				Log.fail("Test Case Failed.Selected object : " + dataPool.get("ObjectName") + " is editable by user. Addional Info : " + ExpectedResults, driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try {
				Utility.quitDriver(driver);
			}
			catch(Exception e0) {Log.exception(e0, driver);} //End catch
		}//End finally

	}//End CustomPermission_24715


	/**
	 * CustomPermission_26822 : Verify if the object is viewable when it has the permission to delete only
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Custom Permission"},
			description = "Verify if the object is viewable when it has the permission to delete only.")
	public void CustomPermission_26822(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("Username"), dataPool.get("Password"), testVault); //Launched driver and logged in

			Log.message("1. Logged into MFWA as non admin user : "+ dataPool.get("Username") +" which has the edit only permission for " + dataPool.get("ObjectName") + " object.", driver);

			//Step-2 : Navigate to template folder and select any template object in the view
			//--------------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), dataPool.get("SearchWord"));//Navigate to the specific view

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new SkipException(dataPool.get("ObjectName")+" does not exist in the view");

			Log.message("2. Navigated to '" + viewtonavigate + "' view and selected the object("+ dataPool.get("ObjectName") +") in the view.", driver);

			String ExpectedResults = "";


			//Step-3: Gets the class property values
			//--------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard in the rightpane

			metadataCard.clickProperty(dataPool.get("PropName"));

			if(metadataCard.isEditMode())
				ExpectedResults = "Selected object : " + dataPool.get("ObjectName") + " metadatacard is in edit mode when user has read only permission.";


			metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard in the rightpane
			metadataCard.clickPermissionField();

			//Verify if user has change the permission for the selected object
			//----------------------------------------------------------------
			if(MFilesDialog.exists(driver, "Permissions"))
				ExpectedResults = "Change Permission dialog is displayed when user has the permission to delete only.";

			//Verify if user has delete permission for selected object
			//--------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectName"));
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Delete.Value);

			MFilesDialog mfilesDialog = new MFilesDialog(driver,"Confirm Delete");

			if(MFilesDialog.exists(driver, "Confirm Delete"))//verify if Confirm Delete dialog is displayed or not
				mfilesDialog.clickOkButton();//Click the 'Yes' button in Confirm Delete dialog	


			//Verification : Verify if the object is viewable when it has the permission to edit only.
			//---------------------------------------------------------------------------------------
			if(ExpectedResults.equals("") && homePage.listView.isItemExists(dataPool.get("ObjectName")))
				Log.pass("Test Case Passed.Selected object : " + dataPool.get("ObjectName") + " can be deleted by user when it has delete only permission for that object.", driver);
			else
				Log.fail("Test Case Failed.Selected object : " + dataPool.get("ObjectName") + " is not deleted by user when it has delete only permission. Addional Info : " + ExpectedResults, driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try{
				Utility.quitDriver(driver);
			}
			catch(Exception e0) {Log.exception(e0, driver);} //End catch
		}//End finally

	}//End CustomPermission_26822

	/**
	 * CustomPermission_26823 : Verify if the object is viewable when it has only change permission
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Custom Permission"},
			description = "Verify if the object is viewable when it has only change permission.")
	public void CustomPermission_26823(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("Username"), dataPool.get("Password"), testVault); //Launched driver and logged in

			Log.message("1. Logged into MFWA as non admin user : "+ dataPool.get("Username") +" which has the edit only permission for " + dataPool.get("ObjectName") + " object.", driver);

			//Step-2 : Navigate to template folder and select any template object in the view
			//--------------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), dataPool.get("SearchWord"));//Navigate to the specific view

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new SkipException(dataPool.get("ObjectName")+" does not exist in the view");

			Log.message("2. Navigated to '" + viewtonavigate + "' view and selected the object("+ dataPool.get("ObjectName") +") in the view.", driver);

			String ExpectedResults = "";


			//Step-3: Gets the class property values
			//--------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard in the rightpane

			metadataCard.clickProperty(dataPool.get("PropName"));

			if(metadataCard.isEditMode())
				ExpectedResults = "Selected object : " + dataPool.get("ObjectName") + " metadatacard is in edit mode when user has change only permission.";


			metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard in the rightpane
			metadataCard.setPermission(dataPool.get("Permission"));

			//Verify if user has change the permission for the selected object
			//----------------------------------------------------------------
			if(MFilesDialog.exists(driver, "Permissions"))
				ExpectedResults = "Change Permission dialog is displayed when user has the permission to edit only.";

			//Verify if user has delete permission for selected object
			//--------------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectName"));
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Delete.Value);

			MFilesDialog mfilesDialog = new MFilesDialog(driver,"Confirm Delete");

			if(MFilesDialog.exists(driver, "Confirm Delete"))//verify if Confirm Delete dialog is displayed or not
				mfilesDialog.clickOkButton();//Click the 'Yes' button in Confirm Delete dialog	

			//Verify if user has delete permission for selected object
			//--------------------------------------------------------
			if(!mfilesDialog.getMessage().equalsIgnoreCase(dataPool.get("WarningMessage")))
				ExpectedResults += "Access denied error message is not displayed when user performing the delete operation which has the permission to read only.";


			//Verification : Verify if the object is viewable when it has the permission to edit only.
			//---------------------------------------------------------------------------------------
			if(ExpectedResults.equals("") && homePage.listView.isItemExists(dataPool.get("ObjectName")))
				Log.pass("Test Case Passed.Selected object : " + dataPool.get("ObjectName") + " can be deleted by user when it has delete only permission for that object.", driver);
			else
				Log.fail("Test Case Failed.Selected object : " + dataPool.get("ObjectName") + " is not deleted by user when it has delete only permission. Addional Info : " + ExpectedResults, driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			try{
				Utility.quitDriver(driver);
			}
			catch(Exception e0) {Log.exception(e0, driver);} //End catch
		}//End finally

	}//End CustomPermission_26823


	/**
	 * CustomPermission_30964 : Verify if non admin user able to see the denied object in the object type property in metadata card with Hide permission in the configuration settings
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Custom Permission - User"},
			description = "Verify if non admin user able to see the denied object in the object type property in metadata card with Hide permission in the configuration settings.")
	public void CustomPermission_30964(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to WebConfig
			//----------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configPage = LoginPage.launchDriverAndLoginToConfig(driver, true);//login to the configuration page with user name

			//Pre-requisites :  Click the controls links in configuration page
			//--------------------------------------------------------
			configPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);//Click the controls area in vault specific settings
			configPage.configurationPanel.setVaultCommands(dataPool.get("HiddenProp"),"Hide");//Click the 'Hide' option in Configuration page
			configPage.configurationPanel.saveSettings();//Click the save button in Config settings

			Log.message("Pre-requisites : Navigated to the " + Caption.ConfigSettings.VaultSpecificSettings.Value + " Clicked the controls link in Configuration page and Selected the 'Hide' option in hidden properties.");

			//Step-1 : Logout from configuration page & login to the MFWA default page with non-admin user
			//--------------------------------------------------------------------------------------------
			configPage.clickLogOut();//Logout from the configuration Page
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("Username"), dataPool.get("Password"), testVault);

			Log.message("1. Logged out from Configuration Page & Login to the Default web page with Non-Admin user : " + dataPool.get("Username"), driver);

			//Step-2 : Navigated to search view & Select the customer object
			//--------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), dataPool.get("SearchWord"));//Navigate to the specific view & Select the specified object

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new SkipException(dataPool.get("ObjectName")+" does not exist in the view");

			Log.message("2. Navigated to the : " + viewtonavigate + " view  & Selected the object : " + dataPool.get("ObjectName") + " in list view.");

			String ExpectedOutcome = "";

			//Step-3 : Add the contact person in selected object metadatacard & Verify if denied property value is not displayed in contact person property 
			//---------------------------------------------------------------------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiate the right pane metadatacard
			metadataCard.addNewProperty(dataPool.get("PropName"));//Add the contact person property in metadatacard

			//Verify if country property saved with some other value then the denied value should not be displayed in the drop down list
			//--------------------------------------------------------------------------------------------------------------------------
			metadataCard.clickPropertyField(dataPool.get("PropName"));//Click the pro
			ArrayList<String> propValues = metadataCard.getAvailablePropertyValues(dataPool.get("PropName"));


			//Verify if the typed in value is listed for selected dropdown property
			//----------------------------------------------------------------------
			for (int i = 0; i < propValues.size(); i++) {
				if(propValues.get(i).trim().equals(dataPool.get("HiddenPropValue")))			
					ExpectedOutcome += "Denied Property value : " + dataPool.get("HiddenPropValue") + " value is displayed in value list dropdown. Actual Outcome : " + propValues.get(i);
				else
					Log.message("3. Denied Property value : "  + dataPool.get("HiddenPropValue") +  " is not displayed in value list for property : " + dataPool.get("PropName"), driver);
			}//End for


			//Step-4 : Navigate to Search Only : Projects view, Select the specified object & Verify if contact person property is displayed
			//------------------------------------------------------------------------------------------------------------------------------
			viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, "Search only: Projects", "");//Navigate to the specific view & Select the specified object

			if (!homePage.listView.clickItem("Office Design"))
				throw new SkipException(dataPool.get("Office Design")+" does not exist in the view");

			ArrayList<String> metadataProperties = metadataCard.getMetadatacardProperties();

			//Verify if the typed in value is listed for selected dropdown property
			//----------------------------------------------------------------------
			for (int i = 0; i < metadataProperties.size(); i++) {
				if(metadataProperties.get(i).trim().equals(dataPool.get("PropName")))			
					ExpectedOutcome += "Property : " + dataPool.get("PropName") + " is displayed in selected object : 'Office Design'. Actual Outcome : " + metadataProperties.get(i);
				else
					Log.message("4. Property : "  + dataPool.get("PropName") +  " is not displayed in selected object : 'Office Design'." + dataPool.get("PropName"), driver);
			}//End for

			//Verification : Verify if non admin user is not able to see the denied property value in the object type property with hide Permission configuration settings
			//------------------------------------------------------------------------------------------------------------------------------------------------------------
			if(ExpectedOutcome.equals(""))
				Log.pass("Test Case Passed. Denied Property value : " + dataPool.get("HiddenPropValue") + " is not displayed in selected object & Property : " + dataPool.get("PropName")+ " is not displayed which have denied property value for non-admin user : " + dataPool.get("Username"), driver);
			else
				Log.fail("Test Case Failed. Denied Property value : " + dataPool.get("HiddenPropValue") + " is displayed in selected object & Property : " + dataPool.get("PropName")+ " is displayed which have denied property value for non-admin user : " + dataPool.get("Username") + ".Additional Info : " + ExpectedOutcome, driver);

		}

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {

			try {

				//Pre-Requisite : Login to WebConfig
				//----------------------------------
				ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
				ConfigurationPage configPage = LoginPage.launchDriverAndLoginToConfig(driver, true);//login to the configuration page with user name

				//Pre-requisites :  Click the controls links in configuration page
				//--------------------------------------------------------
				configPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);//Click the controls area in vault specific settings
				configPage.configurationPanel.setVaultCommands(dataPool.get("HiddenProp"),"Show");//Click the 'Hide' option in Configuration page
				configPage.configurationPanel.saveSettings();//Click the save button in Config settings

				Utility.quitDriver(driver);
			}//End try
			catch(Exception e0) {Log.exception(e0, driver);} //End catch

		}//End finally

	}//End CustomPermission_30964

	/**
	 * CustomPermission_30955 : Verify if non admin user able to see the denied value in the value list property with Show permission in the Configuration Settings in existing object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Custom Permission - User"},
			description = "Verify if non admin user able to see the denied value in the value list property with Show permission in the Configuration Settings in existing object.")
	public void CustomPermission_30955(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to WebConfig
			//----------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configPage = LoginPage.launchDriverAndLoginToConfig(driver, true);//login to the configuration page with user name

			//Pre-requisites :  Click the controls links in configuration page
			//--------------------------------------------------------
			configPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);//Click the controls area in vault specific settings
			configPage.configurationPanel.setVaultCommands(dataPool.get("HiddenProp"),"Show");//Click the 'Hide' option in Configuration page
			configPage.configurationPanel.saveSettings();//Click the save button in Config settings

			Log.message("Pre-requisites : Navigated to the " + Caption.ConfigSettings.VaultSpecificSettings.Value + " Clicked the controls link in Configuration page and Selected the 'Hide' option in hidden properties.");

			//Step-1 : Logout from configuration page & login to the MFWA default page with non-admin user
			//--------------------------------------------------------------------------------------------
			configPage.clickLogOut();//Logout from the configuration Page
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("Username"), dataPool.get("Password"), testVault);

			Log.message("1. Logged out from Configuration Page & Login to the Default web page with Non-Admin user : " + dataPool.get("Username"), driver);

			//Step-2 : Navigated to search view & Select the customer object
			//--------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), dataPool.get("ObjectName"));//Navigate to the specific view & Select the specified object

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new SkipException(dataPool.get("ObjectName")+" does not exist in the view");


			Log.message("2. Navigated to the : " + viewtonavigate + " view  & Selected the object : " + dataPool.get("ObjectName") + " list view.");

			String ExpectedOutcome = "";			

			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiate the right pane metadatacard

			//Verify if Property value is hidden when user denied permission to view the value list
			//-------------------------------------------------------------------------------------
			if(!metadataCard.getPropertyValue(dataPool.get("PropName")).equalsIgnoreCase(dataPool.get("PropValue")))
				ExpectedOutcome = "Property : " + dataPool.get("PropName") + " value is not in hidden mode. Actual Outcome : " + dataPool.get("PropValue");
			else
				Log.message("3. Denied property value : "  + dataPool.get("PropName") +  " value is set as 'hidden' when user denied the permission to view the value list." , driver);


			//Verify if country property saved with some other value then the denied value should not be displayed in the drop down list
			//--------------------------------------------------------------------------------------------------------------------------
			metadataCard.clickPropertyField(dataPool.get("PropName"));//Click the pro
			ArrayList<String> propValues = metadataCard.getAvailablePropertyValues(dataPool.get("PropName"));

			//Verify if the typed in value is listed for selected dropdown property
			//----------------------------------------------------------------------
			int flag = 0;// instantiate the flag variable for verification
			for (int i = 0; i < propValues.size(); i++) {
				if(propValues.get(i).trim().equals(dataPool.get("HiddenPropertyValue")))			
					ExpectedOutcome += "Denied Property value : " + dataPool.get("HiddenPropertyValue") + " value is displayed in value list dropdown. Actual Outcome : " + propValues.get(i);
				flag = 1;
			}//End for

			if(flag == 0)
				Log.message("4. Denied Property value : "  + dataPool.get("HiddenPropertyValue") +  " is not displayed in value list for property : " + dataPool.get("PropName"), driver);


			String[] propertyValues = dataPool.get("MultiPropeties").split("/n"); //Split the multiple property values

			//Verify if multiple Property saved with multiple values including denied value also then the denied value should be displayed as '(hidden)'
			//-----------------------------------------------------------------------------------------------------------------------------------------
			for (int loopIdx=0; loopIdx<propertyValues.length; loopIdx++) //Added with multiple property values. 
				metadataCard.setPropertyValue(dataPool.get("PropName"), propertyValues[loopIdx].trim(), loopIdx+2); //Sets the country property values

			metadataCard.saveAndClose();//Save the metadatacard changes

			//Fetch all added property values & Verify if 'hidden' value is displayed or not
			//--------------------------------------------------------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiate the right pane metadatacard
			ArrayList<String> addedpropValues = metadataCard.getPropertyValues(dataPool.get("PropName"));

			//Verify if hidden property value is displayed in value is listed for selected property
			//-------------------------------------------------------------------------------------
			for (int i = 0; i < addedpropValues.size(); i++) {
				if(addedpropValues.get(i).trim().equals(dataPool.get("PropValue")))			
					ExpectedOutcome += "Denied Property value : " + dataPool.get("HiddenPropertyValue") + " is not displayed in value list for Property : " + dataPool.get("PropName") + " . Actual Outcome : " + addedpropValues.get(i);
				else
					Log.message("4. Denied Property value : "  + dataPool.get("HiddenPropertyValue") +  " is not displayed in value list for property : " + dataPool.get("PropName"), driver);
			}//End for


			//Verification : Verify if Denied property value is not displayed in value list dropdown for selected property for non-admin user
			//-------------------------------------------------------------------------------------------------------------------------------
			if(ExpectedOutcome.equals(""))
				Log.pass("Test Case Passed. Denied Property value : " + dataPool.get("HiddenPropertyValue") + " is not displayed in value list drop down for non-admin user : " + dataPool.get("Username"), driver);
			else
				Log.fail("Test Case Failed. Denied Property value : " + dataPool.get("HiddenPropertyValue") + " is displayed in value list drop down for non-admin user : " + dataPool.get("Username") + " even though its permission is denied in Configuration settings.Additional Info : " + ExpectedOutcome, driver);


		} //End try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {

			try { 

				ConfigurationPage configPage = LoginPage.launchDriverAndLoginToConfig(driver, true);//login to the configuration page with user name

				//Pre-requisites :  Click the controls links in configuration page
				//----------------------------------------------------------------
				ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
				configPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);//Click the controls area in vault specific settings
				configPage.configurationPanel.setVaultCommands(dataPool.get("HiddenProp"),"Show");//Click the 'Hide' option in Configuration page
				configPage.configurationPanel.saveSettings();//Click the save button in Config settings

				Utility.quitDriver(driver);
			}
			catch(Exception e0) {Log.exception(e0, driver);} //End catch	

		}//End finally

	}//End CustomPermission_30955

	/**
	 * CustomPermission_30969 : Verify if non admin user with see and read all objects permission able to see the denied object in the object type property for multi selected objects with Hide permission in the configuration settings
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Custom Permission - User"},
			description = "Verify if non admin user with see and read all objects permission able to see the denied object in the object type property for multi selected objects with Hide permission in the configuration settings.")
	public void CustomPermission_30969(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to WebConfig
			//----------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configPage = LoginPage.launchDriverAndLoginToConfig(driver, true);//login to the configuration page with user name

			//Pre-requisites :  Click the controls links in configuration page
			//--------------------------------------------------------
			configPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);//Click the controls area in vault specific settings
			configPage.configurationPanel.setVaultCommands(dataPool.get("HiddenProp"),"Hide");//Click the 'Hide' option in Configuration page
			configPage.configurationPanel.saveSettings();//Click the save button in Config settings

			Log.message("Pre-requisites : Navigated to the " + Caption.ConfigSettings.VaultSpecificSettings.Value + " Clicked the controls link in Configuration page and Selected the 'Hide' option in hidden properties.");

			//Step-1 : Logout from configuration page & login to the MFWA default page with non-admin user
			//--------------------------------------------------------------------------------------------
			configPage.clickLogOut();//Logout from the configuration Page
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("Username"), dataPool.get("Password"), testVault);

			Log.message("1. Logged out from Configuration Page & Login to the Default web page with Non-Admin user : " + dataPool.get("Username"), driver);

			//Step-2 : Navigated to search view & Select the customer object
			//--------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), dataPool.get("SearchWord"));//Navigate to the specific view & Select the specified object
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));				

			Log.message("2. Navigated to the : " + viewtonavigate + " view  & Selected the objects : " + dataPool.get("Objects") + " in list view.");

			//Step-3 : Open the metadatacard for selected multiple objects in operations menu &  Add the 'Contact person value'
			//-----------------------------------------------------------------------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value);//Click the properties value in operations menu

			MetadataCard metadataCard = new MetadataCard(driver);//Instantitate the popup metadatacard
			metadataCard.addNewProperty(dataPool.get("PropName"));//Add the new 'Contact person' property
			metadataCard.saveAndClose();//Save the metadatacard

			Log.message("3. Opened the metadatacard for selected multiple objects & Added the new Property : " + dataPool.get("PropName"), driver);

			String ExpectedOutcome = "";

			//Verify if denied value should not be displayed in the contact person property drop down list
			//--------------------------------------------------------------------------------------------			
			metadataCard = new MetadataCard(driver, true);//Instantitate rightpane metadatacard
			ArrayList<String> propValues = metadataCard.getAvailablePropertyValues(dataPool.get("PropName"));//Fetch the property values listed in dropdown list

			//Verify if the typed in value is listed for selected dropdown property
			//---------------------------------------------------------------------
			int flag = 0;// instantiate the flag variable for verification

			for (int i = 0; i < propValues.size(); i++) {
				if(propValues.get(i).trim().equals(dataPool.get("HiddenPropValue"))) {			
					ExpectedOutcome += "Denied Property value : " + dataPool.get("HiddenPropValue") + " value is displayed in value list dropdown. Actual Outcome : " + propValues.get(i);	
					flag = 1;
				}//End if
			}//End for

			if(flag==0)//Print the log message message if verification is passed
				Log.message("4. Denied Property value : "  + dataPool.get("HiddenPropertyValue") +  " is not displayed in value list for property : " + dataPool.get("PropName"), driver);

			//Step-5 : Select multiple object which already have a denied property value in the metadata card
			//-----------------------------------------------------------------------------------------------
			viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, "Search only: Projects", "");//Navigate to the specific view & Select the specified object
			homePage.listView.clickMultipleItems(dataPool.get("ObjectsWithDeniedProperty"));	

			Log.message("5. Selected the Multiple project objects : ( " + dataPool.get("ObjectsWithDeniedProperty") + " ) which already having denied property value : " + dataPool.get("HiddenPropValue") , driver);

			//Instantiate the right pane metadatacard
			//---------------------------------------
			metadataCard = new MetadataCard(driver, true);

			//Verify if Property is exists in the Metadatacard or not
			//-------------------------------------------------------
			if (metadataCard.propertyExists(dataPool.get("PropName")))
				ExpectedOutcome += "Denied Property : " + dataPool.get("PropName") + " is displayed in selected objects.";
			else
				Log.message("6. Denied property : " + dataPool.get("PropName") + " is not displayed in selected project objects : " + dataPool.get("MultiProjects") + " as expected.", driver);

			//Step-7 : Select the multiple object which are all have different  contact person values including denied object in the view 
			//---------------------------------------------------------------------------------------------------------------------------
			viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, "Search only: Projects", "");//Navigate to the specific view & Select the specified object
			homePage.listView.clickMultipleItems(dataPool.get("ObjectsWithoutDeniedProperty"));	

			Log.message("7. Selected the Multiple project objects : ( " + dataPool.get("ObjectsWithoutDeniedProperty") + " ) which are all have different denied property values : " + dataPool.get("HiddenPropValue") , driver);

			//Verify if Property is exists in the Metadatacard or not
			//-------------------------------------------------------
			metadataCard = new MetadataCard(driver, true);//Instantiate the right pane metadatacard
			if (metadataCard.getPropertyValue(dataPool.get("PropName")).equalsIgnoreCase(dataPool.get("HiddenPropValue")))
				ExpectedOutcome += "Denied Property : " + dataPool.get("PropName") + " is displayed in selected objects.";
			else
				Log.message("6. Denied property : " + dataPool.get("PropName") + " is not displayed in selected project objects : " + dataPool.get("ObjectsWithoutDeniedProperty") + " as expected.", driver);

			//Verification : Verify if Denied property value is displayed for selected multiple property for non-admin user
			//-------------------------------------------------------------------------------------------------------------
			if(ExpectedOutcome.equals(""))
				Log.pass("Test Case Passed. Denied Property value : " + dataPool.get("HiddenPropertyValue") + " is not displayed for multiselected objects when non-admin user : " + dataPool.get("Username") + " logged into the application.", driver);
			else
				Log.fail("Test Case Failed. Denied Property value : " + dataPool.get("HiddenPropertyValue") + " is displayed for multiselected objects when non-admin user : " + dataPool.get("Username") + " even though its permission is denied in Configuration settings.Additional Info : " + ExpectedOutcome, driver);


		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {

			try {
				ConfigurationPage configPage = LoginPage.launchDriverAndLoginToConfig(driver, true);//login to the configuration page with user name

				//Pre-requisites :  Click the controls links in configuration page
				//----------------------------------------------------------------
				ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
				configPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);//Click the controls area in vault specific settings
				configPage.configurationPanel.setVaultCommands(dataPool.get("HiddenProp"),"Show");//Click the 'Hide' option in Configuration page
				configPage.configurationPanel.saveSettings();//Click the save button in Config settings

				Utility.quitDriver(driver);
			}//End try
			catch(Exception e0) {Log.exception(e0, driver);} //End catch	

		}//End finally
	}//End CustomPermission_30969
	
	
	/**
	 * CustomPermission_30946 : Verify if non admin user able to see the denied class for multi selected objects with Hide Permission in the configuration settings
	 *//*
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Custom Permission - User"},
			description = "Verify if non admin user able to see the denied class for multi selected objects with Hide Permission in the configuration settings.")
	public void CustomPermission_30946(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null; 

		try {

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to WebConfig
			//----------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			ConfigurationPage configPage = LoginPage.launchDriverAndLoginToConfig(driver, true);//login to the configuration page with user name

			//Pre-requisites :  Click the controls links in configuration page
			//--------------------------------------------------------
			configPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);//Click the controls area in vault specific settings
			configPage.configurationPanel.setVaultCommands(dataPool.get("HiddenProp"),"Hide");//Click the 'Hide' option in Configuration page
			configPage.configurationPanel.saveSettings();//Click the save button in Config settings

			Log.message("Pre-requisites : Navigated to the " + Caption.ConfigSettings.VaultSpecificSettings.Value + " Clicked the controls link in Configuration page and Selected the 'Hide' option in hidden properties.");

			//Step-1 : Logout from configuration page & login to the MFWA default page with non-admin user
			//--------------------------------------------------------------------------------------------
			configPage.clickLogOut();//Logout from the configuration Page
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("Username"), dataPool.get("Password"), testVault);

			Log.message("1. Logged out from Configuration Page & Login to the Default web page with Non-Admin user : " + dataPool.get("Username"), driver);

			//Step-2 : Navigated to search view & Select the customer object
			//--------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), dataPool.get("SearchWord"));//Navigate to the specific view & Select the specified object
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));				

			Log.message("2. Navigated to the : " + viewtonavigate + " view  & Selected the objects : " + dataPool.get("ClasswithHiddenValue") + " in list view.");
		
			String ExpectedOutcome = "";
			
			//Verify if Property is exists in the Metadatacard or not
			//-------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiate the right pane metadatacard
			if (!metadataCard.getPropertyValue(dataPool.get("PropName")).equalsIgnoreCase(dataPool.get("PropValue")))
				ExpectedOutcome += "Hidden Property : " + dataPool.get("PropName") + "  value is displayed in selected objects.";
			else
				Log.message("3. Hidden property : " + dataPool.get("PropName") + " value is not displayed in selected objects : " + dataPool.get("ClasswithHiddenValue") + " as expected.", driver);
			
			
			
			
			
		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {

			try {
				ConfigurationPage configPage = LoginPage.launchDriverAndLoginToConfig(driver, true);//login to the configuration page with user name

				//Pre-requisites :  Click the controls links in configuration page
				//----------------------------------------------------------------
				ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
				configPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_Controls.Value);//Click the controls area in vault specific settings
				configPage.configurationPanel.setVaultCommands(dataPool.get("HiddenProp"),"Show");//Click the 'Hide' option in Configuration page
				configPage.configurationPanel.saveSettings();//Click the save button in Config settings

				Utility.quitDriver(driver);
			}//End try
			catch(Exception e0) {Log.exception(e0, driver);} //End catch	

		}//End finally
	}//End CustomPermission_30969
	
*/	
	
	
	

}
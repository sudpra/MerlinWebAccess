package MFClient.Tests.MetadataConfigurability;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.WebDriverUtils;

import java.util.ArrayList;
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
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;


@Listeners(EmailReport.class)

public class MetadataConfigDescription {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String userName = null;
	public static String userFullName = null;
	public static String password = null;
	public static String testVault = null;
	public static String configURL = null;
	public static String driverType = null;
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
			userFullName = xmlParameters.getParameter("UserFullName");
			password = xmlParameters.getParameter("Password");
			configURL = xmlParameters.getParameter("ConfigurationURL");
			testVault = xmlParameters.getParameter("VaultName");	
			driverType = xmlParameters.getParameter("driverType");
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
	 * 105.13.3A : Description should be  available on clicking the properties while creating a new object [eg:customer]-through taskpanel.
	 */

	@Test(groups = {"Sprint105", "MetadataConfigurability", "PropertyDescription"}, description = "Description should be  available on clicking the properties while creating a new object [eg:customer]-through taskpanel.")
	public void SprintTest105_13_3A() throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Step-1 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-2 : Click the new 'Customer' object from the Taskpanel
			//-----------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Customer.Value);

			Log.message("1. Click the 'Customer' object from the Taskpanel.");

			//Step-3 : Click the Customer property in Metadata card
			//-----------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); //Instantiate the metadata card
			metadataCard.savePropValue("Customer name");

			Log.message("2. Property : 'Customer name' is clicked in metadata card.");

			String description = metadataCard.getPropertyDescriptionValue("Customer name");
			//Verification : Verify if Description is available in 'Customer name' field
			//--------------------------------------------------------------------------
			if (description.equals("test"))
				Log.pass("Test Case Passed. Description 'test' is available for 'Customer name' property.");
			else
				Log.fail("Test Case failed.Description 'test' is not available for 'Customer name' property.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End SprintTest105.13.3A

	/**
	 * 105.13.3B : Description is available on clicking the properties while creating a new object [eg:customer]-through menu bar.
	 * 
	 */

	@Test(groups = {"Sprint105", "MetadataConfigurability", "PropertyDescription"}, description = "Description is available on clicking the properties while creating a new object [eg:customer]-through menu bar.")
	public void SprintTest105_13_3B() throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Step-1 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-2 : Click the new 'Customer' object from the Taskpanel
			//-----------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Customer.Value);
			Log.message("1. Click the 'Customer' object from the Menubar.");

			//Step-3 : Click the Customer property in Metadata card
			//-----------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); //Instantiate the metadata card
			metadataCard.savePropValue("Customer name");

			String description = metadataCard.getPropertyDescriptionValue("Customer name");
			//Verification : Verify if Description is available in 'Customer name' field
			//--------------------------------------------------------------------------
			if (description.equals("test"))
				Log.pass("Test Case Passed. Description 'test' is available for 'Customer name' property.");
			else
				Log.fail("Test Case failed.Description 'test' is not available for 'Customer name' property.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End SprintTest105.13.3B

	/**
	 * 105.13.4A : Check description is available on clicking the properties in an existing object [eg:customer]-from double clicking the object.
	 * 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Sprint105", "MetadataConfigurability", "PropertyDescription"}, description = "Check description is available on clicking the properties in an existing object [eg:customer]-from double clicking the object.")
	public void SprintTest105_13_4A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Login to MFiles web access
			//-----------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in


			//Step-1 : Click any existing 'Customer' object from the search icon
			//------------------------------------------------------------------
			homePage.taskPanel.clickItem("Home");
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver,dataPool.get("ViewToNavigate") ,"");
			Log.message("1. Navigate to " + viewtonavigate + " view.");


			//Step-3 : Click the Customer property in Metadata card
			//-----------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); //Instantiate the metadata card
			metadataCard.savePropValue("Customer name");
			Log.message("2. Click Property 'Customer name' from the Metadata card.");

			String description = metadataCard.getPropertyDescriptionValue("Customer name");
			//Verification : Verify if Description is available in 'Customer name' field
			//--------------------------------------------------------------------------
			if (description.equals("test"))
				Log.pass("Test Case Passed. Description 'test' is available for 'Customer name' property.");
			else
				Log.fail("Test Case failed. Description 'test' is not available for 'Customer name' property.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End SprintTest105.13.4A

	/**
	 * 105.13.4B : Check description is available on clicking the properties in an existing object [eg:customer]-from rightpane metadatacard.
	 * 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Sprint105", "MetadataConfigurability", "PropertyDescription"}, 
			description = "Check description is available on clicking the properties in an existing object [eg:customer]-from rightpane metadatacard.")
	public void SprintTest105_13_4B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Step-1 : Login to MFiles Web Access
			//-----------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in
			Log.message("1. Logged into MFWA and Select any vault.");

			//Step-2 : Navigate 'Customer' view
			//----------------------------------
			homePage.taskPanel.clickItem("Home");
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), " ");
			Log.message("2. Navigate to " + viewtonavigate + " view.");

			//Step-3 : Click any existing Customer object
			//------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectToClick"));
			Log.message("3. Select the Object : " +dataPool.get("ObjectToClick")+ " from the list view." );

			//Step-4 : Instantiate the metadata card & Click the Property name
			//----------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver,true);
			metadataCard.savePropValue("Customer name");
			Log.message("4. Click the 'Customer name' object from metadata card.");

			String description = metadataCard.getPropertyDescriptionValue("Customer name");
			//Verification : Verify if Description is available in 'Customer name' field
			//--------------------------------------------------------------------------
			if (description.equals("test"))
				Log.pass("Test Case Passed. Description 'test' is available for 'Customer name' property.");
			else
				Log.fail("Test Case failed. Description 'test' is not available for 'Customer name' property.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		}
		finally {
			Utility.quitDriver(driver);
		}
	}//End SprintTest105.13.4B

	/**
	 * 105.13.4C : Check description is available on clicking the properties in an existing object [eg:customer]-from context menu properties
	 * 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Sprint105", "MetadataConfigurability", "PropertyDescription"}, 
			description = "Check description is available on clicking the properties in an existing object [eg:customer]-from context menu properties.")
	public void SprintTest105_13_4C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Login to MFiles Web Access
			//-----------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to 'Manage Customers' view  
			//--------------------------------------------
			homePage.taskPanel.clickItem("Home");
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), " ");

			Log.message("1. Navigate to " + viewtonavigate + " view.");

			//Step-2 : Click any existing Customer object
			//------------------------------------------------
			homePage.listView.rightClickItem(dataPool.get("ObjectToClick"));

			Log.message("2. Select the Object : " + dataPool.get("ObjectToClick") + " from the list view." );

			//Step-3 : Click 'Properties' from the Context menu
			//---------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value);

			Log.message("3. Click the 'Properties' option from the context menu.");

			//Step-4 : Instantiate the Metadatacard & Click 'Customer name' property
			//----------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);
			metadataCard.savePropValue("Customer name");

			Log.message("4. Click the 'Customer name' object from metadata card.");

			String description = metadataCard.getPropertyDescriptionValue("Customer name");
			//Verification : Verify if Description is available in 'Customer name' field
			//--------------------------------------------------------------------------
			if (description.equals("test"))
				Log.pass("Test Case Passed. Description 'test' is available for 'Customer name' property.");
			else
				Log.fail("Test Case failed. Description 'test' is not available for 'Customer name' property.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End SprintTest105_13_4C


	/**
	 * 105.13.4D : Check description is available on clicking the properties in an existing object [eg:customer]-from TaskPanel properties
	 * 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Sprint105", "MetadataConfigurability", "PropertyDescription"}, 
			description = "Check description is available on clicking the properties in an existing object [eg:customer]-from TaskPanel properties.")
	public void SprintTest105_13_4D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Logged into MFiles Web Access
			//-----------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to 'Manage Customers' view  
			//--------------------------------------------
			homePage.taskPanel.clickItem("Home");
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), " ");

			Log.message("1. Navigate to '" + viewtonavigate + "' view.");

			//Step-2 : Select the Object from the listview
			//--------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectToClick"));//Click the customer object item from the list view

			Log.message("2. Object " + dataPool.get("ObjectToClick") + " is selected from listview.");

			//Step-3 : Click the Properties from the taskpanel 
			//------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value); //Click the properties from the task panel option

			Log.message("3. Clicked 'Properties' option from the taskpanel.");

			//Step-4 : Instantiate the Metadatacard & Clicked 'Customer name' property
			//----------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);
			metadataCard.savePropValue("Customer name");

			Log.message("4. Click the 'Customer name' object from metadata card.");

			String description = metadataCard.getPropertyDescriptionValue("Customer name");

			//Verification : Verify if Description is available in 'Customer name' field
			//--------------------------------------------------------------------------
			if (description.equals("test"))
				Log.pass("Test Case Passed. Description 'test' is available for 'Customer name' property.");
			else
				Log.fail("Test Case failed. Description 'test' is not available for 'Customer name' property.", driver);

		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End SprintTest105_13_4D

	/**
	 * 105.13.4E : Description is available on clicking the properties in an existing object [eg:customer]-Clicking Popout metadata card under the settings menu.
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Sprint105", "MetadataConfigurability", "PropertyDescription"}, 
			description = "Description is available on clicking the properties in an existing object [eg:customer]-Clicking Popout metadata card under the settings menu.")
	public void SprintTest105_13_4E(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Logged into MFiles Web Access
			//-----------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view  
			//-----------------------------------
			homePage.taskPanel.clickItem("Home");
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), " ");

			Log.message("1. Navigate to '" + viewtonavigate + "' view.");

			//Step-2 : Select the Object from the listview
			//--------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectToClick"));//Click the customer object item from the list view

			Log.message("2. Object " + dataPool.get("ObjectToClick") + " is selected from listview.");

			//Step-3 : Click the metadata card from the 'Pop-out metadata card'
			//----------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver,true);
			metadataCard.popOutMetadatacard();

			Log.message("3. Pop-out metadata card is selected under the Setting menu in Rightpane metadata card.");

			//Step-4 : Clicked Property: 'Customer Name' in metadata card
			//-----------------------------------------------------------
			MetadataCard popOutMetadatacard = new MetadataCard(driver);//Instantiate the Pop-out metadata card
			popOutMetadatacard.savePropValue("Customer name");

			Log.message("4. Clicked 'Customer Name' Property in Pop-out metadata card.");

			String description = popOutMetadatacard.getPropertyDescriptionValue("Customer name");
			//Verification : Verify if Description is available in 'Customer name' field
			//--------------------------------------------------------------------------
			if (description.equals("test"))
				Log.pass("Test Case Passed. Description 'test' is available for 'Customer name' property.");
			else
				Log.fail("Test Case failed. Description 'test' is not available for 'Customer name' property.", driver);

		}

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End SprintTest105_13_4E


	/**
	 * 105.13.7 : Check description is available on clicking the properties in an existing object [eg:customer]-from context menu properties.
	 * 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Sprint105", "MetadataConfigurability", "PropertyDescription", "Script"}, 
			description = "Check description is available on clicking the properties in an existing object [eg:customer]-from context menu properties.")
	public void SprintTest105_13_7(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Login to MFiles Web Access
			//-----------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to 'Manage Customers' view
			//--------------------------------------------
			homePage.taskPanel.clickItem("Home");
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), " ");

			Log.message("1. Navigate to " + viewtonavigate + " view.");

			//Step-2 :  Select the multiple objects for same objects
			//------------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("ObjectToClick"));

			Log.message("2. Selected the Multiple Customer objects " + dataPool.get("ObjectToClick"));


			//Step-3 : Instantiate the Metadatacard & Click 'Customer name' property
			//----------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver,true); //Instantiate the metadata card in right pane
			metadataCard.savePropValue("Customer name"); //Click the 

			Log.message("3. Click the 'Customer name' object from metadata card.");

			String description = metadataCard.getPropertyDescriptionValue("Customer name");
			//Verification : Verify if Description is available in 'Customer name' field
			//--------------------------------------------------------------------------
			if (description.equals("test"))
				Log.pass("Test Case Passed. Description 'test' is available for 'Customer name' property.");
			else
				Log.fail("Test Case failed. Description 'test' is not available for 'Customer name' property.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End SprintTest105_13_7


	/**
	 * 105.13.16A : Description for the selected property should be displayed as valid http link in newly created Object[eg:Project] via task panel
	 */
	@Test(groups = {"MetadataConfigurability", "PropertyDescription"}, description = "Description for the selected property should be displayed as valid http link in new object property description via taskpane.")
	public void SprintTest105_13_16A() throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Login to MFiles Web Access
			//--------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched Webdriver and logged into MFWA

			//Step-1 : New Object 'Project' link is clicked from the task pane
			//----------------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Project.Value);//Clicked the new object link from the task pane

			Log.message("1. New object : 'Project' is clicked from the taskpane & new metadata card is opened.");

			//Step-2 : Sets the class value in the metadatacard
			//-------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); //Instantiate the metadata card

			metadataCard.setInfo("Class::Internal Project");//Sets the class value in the metadatacard

			Log.message("2. Class : 'Internal Project' is selected in the metadatacard");

			//Step-3 : check the expected description link is displayed in the property description 
			//-------------------------------------------------------------------------------------
			int exist=0;
			ArrayList<String> actualLinks = metadataCard.getPropertyDescriptionLinks("Project manager");//Get the description link metadata value 

			for (int i=0;i< actualLinks.size();i++)
				if(actualLinks.get(i).trim().equalsIgnoreCase("http M-Files web link")){//Checks if expected link text is same as displayed in the property description
					exist = 1;//exist is set to 1 if expected link text is displayed in the property description
					break;
				}

			if(exist != 1)//Checks expected Valid link text is displayed in the property description 
				throw new Exception(" Expected Valid URL link text(http M-Files web link) is not displayed in the property description");

			Log.message("3. Valid Description link : 'http M-Files web link' is displayed for 'Project manager' property in new Project metadata card.");	

			//Step-3 : Clicks the description link
			//------------------------------------
			metadataCard.clickPropertyDescriptionLink("Project manager","http M-Files web link");//Click the link for property description

			Log.message("4. Description link : 'http M-Files web link' is clicked from the property description in the new 'Project' object metadata card.");

			//Verification : Valid link URL is opened or not
			//---------------------------------------------------------
			if (Utility.tabExists(driver, "www.m-files.com/en", "URL"))
				Log.pass("Test Case Passed. New webpage is opened with the 'http://www.m-files.com/en' url which is mentioned in 'Project manager' property description in metadata card", driver);
			else
				Log.fail("Test Case Failed.New webpage is not opened with the 'http://www.m-files.com/en' url which is mentioned in 'Project manager' property description in metadata card", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			driver = Utility.switchToTab(driver, "M-Files Web", "title");
			Utility.quitDriver(driver);
		}//End finally
	}//End SpritTest105.13.16A

	/**
	 * 105.13.16B : Description for the selected property should be displayed as valid http link in new object object[eg.Project]-from the operations menu.
	 */
	@Test(groups = {"MetadataConfigurability", "PropertyDescription"}, description = "Description for the selected property should be displayed as valid http link in new object property description-from the operations menu.")
	public void SprintTest105_13_16B() throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Login to MFiles Web Access
			//--------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched Webdriver and logged into MFWA

			//Step-1 : New Object 'Project' link is clicked from the task pane
			//----------------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Project.Value);//Clicked the new object link from the operation menu

			Log.message("1. New object : 'Project' is clicked from the new operation menu & new metadata card is opened.");

			//Step-2 : Sets the class value in the metadatacard
			//-------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); //Instantiate the metadata card

			metadataCard.setInfo("Class::Internal Project");//Sets the class value in the metadatacard

			Log.message("2. Class : 'Internal Project' is selected in the metadatacard");

			//Step-3 : check the expected description link is displayed in the property description 
			//-------------------------------------------------------------------------------------
			metadataCard = new MetadataCard(driver); //Instantiate the metadata card
			int exist=0;
			ArrayList<String> actualLinks = metadataCard.getPropertyDescriptionLinks("Project manager");//Get the description link metadata value 

			for (int i=0;i< actualLinks.size();i++)
				if(actualLinks.get(i).trim().equalsIgnoreCase("http M-Files web link")){//Checks if expected link text is same as displayed in the property description
					exist = 1;//exist is set to 1 if expected link text is displayed in the property description
					break;
				}

			if(exist != 1)//Checks expected Valid link text is displayed in the property description 
				throw new Exception(" Expected Valid URL link text(http M-Files web link) is not displayed in the property description");

			Log.message("3. Valid Description link : 'http M-Files web link' is displayed for 'Project manager' property in new Project metadata card.");	

			//Step-3 : Clicks the description link
			//------------------------------------
			metadataCard.clickPropertyDescriptionLink("Project manager","http M-Files web link");//Click the link for property description

			Log.message("4. Description link : 'http M-Files web link' is clicked from the property description in the new 'Project' object metadata card.");

			//Verification : Valid link URL is opened or not
			//---------------------------------------------------------
			if (Utility.tabExists(driver, "www.m-files.com/en", "URL"))
				Log.pass("Test Case Passed. New webpage is opened with the 'http://www.m-files.com/en' url which is mentioned in 'Project manager' property description in metadata card", driver);
			else
				Log.fail("Test Case Failed.New webpage is not opened with the 'http://www.m-files.com/en' url which is mentioned in 'Project manager' property description in metadata card", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			driver = Utility.switchToTab(driver, "M-Files Web", "title");
			Utility.quitDriver(driver);
		}//End finally
	}//End SpritTest105.13.16B

	/**
	 * 1.2.15.1.2C : Property description contains valid http link [New object creation via Object Property]
	 */
	@Test(groups = {"MetadataConfigurability", "PropertyDescription"}, description = "Property description contains valid http link [New object creation via Object Property]")
	public void TC_1_2_15_1_2C() throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Login to MFiles Web Access
			//--------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched Webdriver and logged into MFWA

			//Step-1 : Navigate to specified view
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, "4. Manage Employees", "");

			Log.message("1. Navigated to : " + viewToNavigate + " view.");

			//Step-2 : Select any existing object
			//------------------------------------------------
			if (!homePage.listView.clickItem("Andy Nash"))//Select the specified object
				throw new SkipException("Andy Nash is not selected in the view");

			Log.message("2. Selected the specified object : Andy Nash in list view.");

			//Step-3 : Clicks the new field link in the object lookup property in the metadatacard
			//------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard of existing object

			metadataCard.clickAddValueButton("Project");//Clicks the Add value button to open the new customer object metadatacard

			Log.message("3. Opened the new object(Project) metadatacard via object type property", driver);

			//Step-4 : Sets the class value in the metadatacard
			//-------------------------------------------------
			metadataCard = new MetadataCard(driver); //Instantiate the metadata card

			metadataCard.setInfo("Class::Internal Project");//Sets the class value in the metadatacard

			Log.message("4. Class : 'Internal Project' is selected in the metadatacard");

			//Step-3 : check the expected description link is displayed in the property description 
			//-------------------------------------------------------------------------------------
			metadataCard = new MetadataCard(driver); //Instantiate the metadata card
			int exist=0;
			ArrayList<String> actualLinks = metadataCard.getPropertyDescriptionLinks("Project manager");//Get the description link metadata value 

			for (int i=0;i< actualLinks.size();i++)
				if(actualLinks.get(i).trim().equalsIgnoreCase("http M-Files web link")){//Checks if expected link text is same as displayed in the property description
					exist = 1;//exist is set to 1 if expected link text is displayed in the property description
					break;
				}

			if(exist != 1)//Checks expected Valid link text is displayed in the property description 
				throw new Exception(" Expected Valid URL link text(http M-Files web link) is not displayed in the property description");

			Log.message("5. Valid Description link : 'http M-Files web link' is displayed for 'Project manager' property in new Project metadata card.");	

			//Step-3 : Clicks the description link
			//------------------------------------
			metadataCard.clickPropertyDescriptionLink("Project manager","http M-Files web link");//Click the link for property description

			Log.message("6. Description link : 'http M-Files web link' is clicked from the property description in the new 'Project' object metadata card.");

			//Verification : Valid link URL is opened or not
			//---------------------------------------------------------
			if (Utility.tabExists(driver, "www.m-files.com/en", "URL"))
				Log.pass("Test Case Passed. New webpage is opened with the 'http://www.m-files.com/en' url which is mentioned in 'Project manager' property description in metadata card", driver);
			else
				Log.fail("Test Case Failed.New webpage is not opened with the 'http://www.m-files.com/en' url which is mentioned in 'Project manager' property description in metadata card", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			driver = Utility.switchToTab(driver, "M-Files Web", "title");
			Utility.quitDriver(driver);
		}//End finally
	}//End 1.2.15.1.2C


	/**
	 * 105.13.16C :Description for the selected property should be displayed as valid http link in any existing object.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"MetadataConfigurability", "PropertyDescription"}, description = "Description for the selected property should be displayed as valid http link in any existing object.")
	public void SprintTest105_13_16C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Logged into MFiles Web Access
			//-----------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched Webdriver and logged into MFWA

			//Step-1 : Navigate to the specified view
			//---------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), " ");

			Log.message("1. Navigated to " + viewtonavigate + " view.");

			//Step-2 : Select any existing object in the view
			//-----------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectToClick"));//Clicks the existing object in the view

			Log.message("2. Existing object : '"+dataPool.get("ObjectToClick")+"' is selected in the view.");

			//Step-3 : check the expected description link is displayed in the property description 
			//-------------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true); //Instantiate the metadata card
			int exist=0;
			ArrayList<String> actualLinks = metadataCard.getPropertyDescriptionLinks("Project manager");//Get the description link metadata value 

			for (int i=0;i< actualLinks.size();i++)
				if(actualLinks.get(i).trim().equalsIgnoreCase("http M-Files web link")){//Checks if expected link text is same as displayed in the property description
					exist = 1;//exist is set to 1 if expected link text is displayed in the property description
					break;
				}

			if(exist != 1)//Checks expected Valid link text is displayed in the property description 
				throw new Exception(" Expected Valid URL link text(http M-Files web link) is not displayed in the property description");

			Log.message("3. Valid Description link : 'http M-Files web link' is displayed for 'Project manager' property in new Project metadata card.");	

			//Step-3 : Clicks the description link
			//------------------------------------
			metadataCard.clickPropertyDescriptionLink("Project manager","http M-Files web link");//Click the link for property description

			Log.message("4. Description link : 'http M-Files web link' is clicked from the property description in the new 'Project' object metadata card.");

			//Verification : Valid link URL is opened or not
			//---------------------------------------------------------
			if (Utility.tabExists(driver, "www.m-files.com/en", "URL"))
				Log.pass("Test Case Passed. New webpage is opened with the 'http://www.m-files.com/en' url which is mentioned in 'Project manager' property description in metadata card", driver);
			else
				Log.fail("Test Case Failed.New webpage is not opened with the 'http://www.m-files.com/en' url which is mentioned in 'Project manager' property description in metadata card", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			driver = Utility.switchToTab(driver, "M-Files Web", "title");
			Utility.quitDriver(driver);
		}//End finally
	}//End SpritTest105.13.16C	

	/**
	 * 1.2.15.2.2B :Property description contains valid http link [Existing Object - Pop out metadatacard]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"MetadataConfigurability", "PropertyDescription"}, description = "Property description contains valid http link [Existing Object - Pop out metadatacard]")
	public void TC_1_2_15_2_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Logged into MFiles Web Access
			//-----------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched Webdriver and logged into MFWA

			//Step-1 : Navigate to the specified view
			//---------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), " ");

			Log.message("1. Navigated to " + viewtonavigate + " view.");

			//Step-2 : Select any existing object in the view
			//-----------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectToClick"));//Clicks the existing object in the view

			homePage.menuBar.ClickOperationsMenu("Properties");//Opens the popout metadatacard of the object

			Log.message("2. Existing object : '"+dataPool.get("ObjectToClick")+"' is selected and popped out metadatacard of the object is opened in the view.", driver);

			//Step-3 : check the expected description link is displayed in the property description 
			//-------------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); //Instantiate the metadata card
			int exist=0;
			ArrayList<String> actualLinks = metadataCard.getPropertyDescriptionLinks("Project manager");//Get the description link metadata value 

			for (int i=0;i< actualLinks.size();i++)
				if(actualLinks.get(i).trim().equalsIgnoreCase("http M-Files web link")){//Checks if expected link text is same as displayed in the property description
					exist = 1;//exist is set to 1 if expected link text is displayed in the property description
					break;
				}

			if(exist != 1)//Checks expected Valid link text is displayed in the property description 
				throw new Exception(" Expected Valid URL link text(http M-Files web link) is not displayed in the property description");

			Log.message("3. Valid Description link : 'http M-Files web link' is displayed for 'Project manager' property in new Project metadata card.");	

			//Step-3 : Clicks the description link
			//------------------------------------
			metadataCard.clickPropertyDescriptionLink("Project manager","http M-Files web link");//Click the link for property description

			Log.message("4. Description link : 'http M-Files web link' is clicked from the property description in the new 'Project' object metadata card.");

			//Verification : Valid link URL is opened or not
			//---------------------------------------------------------
			if (Utility.tabExists(driver, "www.m-files.com/en", "URL"))
				Log.pass("Test Case Passed. New webpage is opened with the 'http://www.m-files.com/en' url which is mentioned in 'Project manager' property description in metadata card", driver);
			else
				Log.fail("Test Case Failed.New webpage is not opened with the 'http://www.m-files.com/en' url which is mentioned in 'Project manager' property description in metadata card", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			driver = Utility.switchToTab(driver, "M-Files Web", "title");
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_15_2_2B	

	/**
	 * TC_1_2_15_2_2C : Property description contains valid http link [Multiple existing objects]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"SKIP_MultiSelect", "MetadataConfigurability", "PropertyDescription"}, description = "Property description contains valid http link [Multiple existing objects]")
	public void TC_1_2_15_2_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Logged into MFiles Web Access
			//-----------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched Webdriver and logged into MFWA

			//Step-1 : Navigate to the specified view
			//---------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), " ");

			Log.message("1. Navigated to " + viewtonavigate + " view.");

			//Step-2 : Select any existing object in the view
			//-----------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("ObjectsToClick"));//Clicks the existing object in the view

			Log.message("2. Existing objects is selected in the view.", driver);

			//Step-3 : check the expected description link is displayed in the property description 
			//-------------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true); //Instantiate the metadata card
			int exist=0;
			ArrayList<String> actualLinks = metadataCard.getPropertyDescriptionLinks("Project manager");//Get the description link metadata value 

			for (int i=0;i< actualLinks.size();i++)
				if(actualLinks.get(i).trim().equalsIgnoreCase("http M-Files web link")){//Checks if expected link text is same as displayed in the property description
					exist = 1;//exist is set to 1 if expected link text is displayed in the property description
					break;
				}

			if(exist != 1)//Checks expected Valid link text is displayed in the property description 
				throw new Exception(" Expected Valid URL link text(http M-Files web link) is not displayed in the property description");

			Log.message("3. Valid Description link : 'http M-Files web link' is displayed for 'Project manager' property in new Project metadata card.");	

			//Step-3 : Clicks the description link
			//------------------------------------
			metadataCard.clickPropertyDescriptionLink("Project manager","http M-Files web link");//Click the link for property description

			Log.message("4. Description link : 'http M-Files web link' is clicked from the property description in the new 'Project' object metadata card.");

			//Verification : Valid link URL is opened or not
			//---------------------------------------------------------
			if (Utility.tabExists(driver, "www.m-files.com/en", "URL"))
				Log.pass("Test Case Passed. New webpage is opened with the 'http://www.m-files.com/en' url which is mentioned in 'Project manager' property description in metadata card", driver);
			else
				Log.fail("Test Case Failed.New webpage is not opened with the 'http://www.m-files.com/en' url which is mentioned in 'Project manager' property description in metadata card", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			driver = Utility.switchToTab(driver, "M-Files Web", "title");
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_15_2_2C	

	/**
	 * TC_1_2_15_2_2D : Property description contains valid http link [Multiple existing Object - Pop out metadatacard]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"SKIP_MultiSelect", "MetadataConfigurability", "PropertyDescription"}, description = "Property description contains valid http link [Multiple existing Object - Pop out metadatacard]")
	public void TC_1_2_15_2_2D(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Logged into MFiles Web Access
			//-----------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched Webdriver and logged into MFWA

			//Step-1 : Navigate to the specified view
			//---------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), " ");

			Log.message("1. Navigated to " + viewtonavigate + " view.");

			//Step-2 : Select any existing object in the view
			//-----------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("ObjectsToClick"));//Clicks the existing objects in the view

			homePage.menuBar.ClickOperationsMenu("Properties");//Opens the popout metadatacard of the object

			Log.message("2. Existing object is selected and popped out metadatacard of the objects is opened in the view.", driver);

			//Step-3 : check the expected description link is displayed in the property description 
			//-------------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); //Instantiate the metadata card
			int exist=0;
			ArrayList<String> actualLinks = metadataCard.getPropertyDescriptionLinks("Project manager");//Get the description link metadata value 

			for (int i=0;i< actualLinks.size();i++)
				if(actualLinks.get(i).trim().equalsIgnoreCase("http M-Files web link")){//Checks if expected link text is same as displayed in the property description
					exist = 1;//exist is set to 1 if expected link text is displayed in the property description
					break;
				}

			if(exist != 1)//Checks expected Valid link text is displayed in the property description 
				throw new Exception(" Expected Valid URL link text(http M-Files web link) is not displayed in the property description");

			Log.message("3. Valid Description link : 'http M-Files web link' is displayed for 'Project manager' property in new Project metadata card.");	

			//Step-3 : Clicks the description link
			//------------------------------------
			metadataCard.clickPropertyDescriptionLink("Project manager","http M-Files web link");//Click the link for property description

			Log.message("4. Description link : 'http M-Files web link' is clicked from the property description in the new 'Project' object metadata card.");

			//Verification : Valid link URL is opened or not
			//---------------------------------------------------------
			if (Utility.tabExists(driver, "www.m-files.com/en", "URL"))
				Log.pass("Test Case Passed. New webpage is opened with the 'http://www.m-files.com/en' url which is mentioned in 'Project manager' property description in metadata card", driver);
			else
				Log.fail("Test Case Failed.New webpage is not opened with the 'http://www.m-files.com/en' url which is mentioned in 'Project manager' property description in metadata card", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			driver = Utility.switchToTab(driver, "M-Files Web", "title");
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_15_2_2D	


	/**
	 * 105_13_16D : Description is displayed as invalid http link for the new object
	 * 
	 */
	@Test(groups = {"MetadataConfigurability", "PropertyDescription"}, description = "Description is displayed as invalid http link for the new object")
	public void SprintTest105_13_16D() throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Logged into MFiles Web Access
			//-----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched Webdriver and logged into MFWA with valid credentials

			//Step-1 : New Object 'Project' link is clicked from the task pane
			//----------------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Project.Value);//Clicked the new object link from the task pane

			Log.message("1. New object : 'Project' is clicked from the taskpane & new metadata card is opened.");

			//Step-2 : Sets the class value in the metadatacard
			//-------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); //Instantiate the metadata card

			metadataCard.setInfo("Class::Internal Project");//Sets the class value in the metadatacard

			Log.message("2. Class : 'Internal Project' is selected in the metadatacard");

			//Step-3 : check the expected description link is displayed in the property description 
			//-------------------------------------------------------------------------------------
			metadataCard = new MetadataCard(driver); //Instantiate the metadata card
			int exist=0;
			ArrayList<String> actualLinks = metadataCard.getPropertyDescriptionLinks("Project manager");//Get the description link metadata value 

			for (int i=0;i< actualLinks.size();i++)
				if(actualLinks.get(i).trim().equalsIgnoreCase("http invalid M-Files web link")){//Checks if expected link text is same as displayed in the property description
					exist = 1;//exist is set to 1 if expected link text is displayed in the property description
					break;
				}

			if(exist != 1)//Checks expected Valid link text is displayed in the property description 
				throw new Exception(" Expected Valid URL link text(http invalid M-Files web link) is not displayed in the property description");

			Log.message("3. Valid Description link : 'http invalid M-Files web link' is displayed for 'Project manager' property in new Project metadata card.");	

			//Step-3 : Clicks the description link
			//------------------------------------
			metadataCard.clickPropertyDescriptionLink("Project manager","http invalid M-Files web link");//Click the link for property description

			Log.message("4. Description link : 'http invalid M-Files web link' is clicked from the property description in the new 'Project' object metadata card.");

			//Verification : Invalid link URL is opened or not
			//-------------------------------------------------
			if (Utility.tabExists(driver, "http://www.invalidm-files.com/en", "URL"))
				Log.pass("Test Case Passed. New webpage is opened with the 'http://www.invalidm-files.com/en' url which is mentioned in 'Project manager' property description in metadata card", driver);
			else
				Log.fail("Test Case Failed.New webpage is not opened with the 'http://www.invalidm-files.com/en' url which is mentioned in 'Project manager' property description in metadata card", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			driver = Utility.switchToTab(driver, "M-Files Web", "title");
			Utility.quitDriver(driver);
		}//End finally

	}//End SprintTest105_13_16D

	/**
	 * 105_13_16E : Check whether a invalid http link as Description is displayed as invalid http link for the existing object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"MetadataConfigurability", "PropertyDescription"}, description = "Check whether a invalid http link as Description is displayed as invalid http link for the existing object")
	public void SprintTest105_13_16E(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Logged into MFiles Web Access
			//-----------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched Webdriver and logged into MFWA with valid credentials

			//Step-1 : Navigate to the specified view
			//---------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), " ");

			Log.message("1. Navigated to " + viewtonavigate + " view.");

			//Step-2 : Select any existing object in the view
			//-----------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectToClick"));//Clicks the existing object in the view

			Log.message("2. Existing object : '"+dataPool.get("ObjectToClick")+"' is selected in the view.");

			//Step-3 : check the expected description link is displayed in the property description 
			//-------------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true); //Instantiate the metadata card
			int exist=0;
			ArrayList<String> actualLinks = metadataCard.getPropertyDescriptionLinks("Project manager");//Get the description link metadata value 

			for (int i=0;i< actualLinks.size();i++)
				if(actualLinks.get(i).trim().equalsIgnoreCase("http invalid M-Files web link")){//Checks if expected link text is same as displayed in the property description
					exist = 1;//exist is set to 1 if expected link text is displayed in the property description
					break;
				}

			if(exist != 1)//Checks expected Valid link text is displayed in the property description 
				throw new Exception(" Expected Valid URL link text(http invalid M-Files web link) is not displayed in the property description");

			Log.message("3. Valid Description link : 'http invalid M-Files web link' is displayed for 'Project manager' property in new Project metadata card.");	

			//Step-3 : Clicks the description link
			//------------------------------------
			metadataCard.clickPropertyDescriptionLink("Project manager","http invalid M-Files web link");//Click the link for property description

			Log.message("4. Description link : 'http invalid M-Files web link' is clicked from the property description in the new 'Project' object metadata card.");

			//Verification : Invalid link URL is opened or not
			//-------------------------------------------------
			if (Utility.tabExists(driver, "http://www.invalidm-files.com/en", "URL"))
				Log.pass("Test Case Passed. New webpage is opened with the 'http://www.invalidm-files.com/en' url which is mentioned in 'Project manager' property description in metadata card", driver);
			else
				Log.fail("Test Case Failed.New webpage is not opened with the 'http://www.invalidm-files.com/en' url which is mentioned in 'Project manager' property description in metadata card", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			driver = Utility.switchToTab(driver, "M-Files Web", "title");
			Utility.quitDriver(driver);
		}//End finally
	}//End 105.13.16E

	/**
	 * 1.2.12.2.2B : Property description contains invalid http link [Existing Object - Pop out metadatacard]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"MetadataConfigurability", "PropertyDescription"}, description = "Property description contains invalid http link [Existing Object - Pop out metadatacard]")
	public void TC_1_2_12_2_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Logged into MFiles Web Access
			//-----------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched Webdriver and logged into MFWA with valid credentials

			//Step-1 : Navigate to the specified view
			//---------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), " ");

			Log.message("1. Navigated to " + viewtonavigate + " view.");

			//Step-2 : Select any existing object in the view
			//-----------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectToClick"));//Clicks the existing object in the view

			homePage.menuBar.ClickOperationsMenu("Properties");//Opens the popped out metadatacard

			Log.message("2. Existing object : '"+dataPool.get("ObjectToClick")+"' is selected and the popped out metadatacard of the object is opened in the view.", driver);

			//Step-3 : check the expected description link is displayed in the property description 
			//-------------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); //Instantiate the metadata card
			int exist=0;
			ArrayList<String> actualLinks = metadataCard.getPropertyDescriptionLinks("Project manager");//Get the description link metadata value 

			for (int i=0;i< actualLinks.size();i++)
				if(actualLinks.get(i).trim().equalsIgnoreCase("http invalid M-Files web link")){//Checks if expected link text is same as displayed in the property description
					exist = 1;//exist is set to 1 if expected link text is displayed in the property description
					break;
				}

			if(exist != 1)//Checks expected Valid link text is displayed in the property description 
				throw new Exception(" Expected Valid URL link text(http invalid M-Files web link) is not displayed in the property description");

			Log.message("3. Valid Description link : 'http invalid M-Files web link' is displayed for 'Project manager' property in new Project metadata card.");	

			//Step-3 : Clicks the description link
			//------------------------------------
			metadataCard.clickPropertyDescriptionLink("Project manager","http invalid M-Files web link");//Click the link for property description

			Log.message("4. Description link : 'http invalid M-Files web link' is clicked from the property description in the new 'Project' object metadata card.");

			//Verification : Invalid link URL is opened or not
			//-------------------------------------------------
			if (Utility.tabExists(driver, "http://www.invalidm-files.com/en", "URL"))
				Log.pass("Test Case Passed. New webpage is opened with the 'http://www.invalidm-files.com/en' url which is mentioned in 'Project manager' property description in metadata card", driver);
			else
				Log.fail("Test Case Failed.New webpage is not opened with the 'http://www.invalidm-files.com/en' url which is mentioned in 'Project manager' property description in metadata card", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			driver = Utility.switchToTab(driver, "M-Files Web", "title");
			Utility.quitDriver(driver);
		}//End finally
	}//End 1.2.12.2.2B

	/**
	 * 1.2.12.2.2C : Property description contains invalid http link [Multiple existing objects]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"SKIP_MultiSelect", "MetadataConfigurability", "PropertyDescription"}, description = "Property description contains invalid http link [Multiple existing objects]")
	public void TC_1_2_12_2_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Logged into MFiles Web Access
			//-----------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched Webdriver and logged into MFWA with valid credentials

			//Step-1 : Navigate to the specified view
			//---------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), " ");

			Log.message("1. Navigated to " + viewtonavigate + " view.");

			//Step-2 : Select any existing object in the view
			//-----------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("ObjectsToClick"));//Clicks the existing objects in the view

			Log.message("2. Existing object is selected in the view.", driver);

			//Step-3 : check the expected description link is displayed in the property description 
			//-------------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true); //Instantiate the metadata card
			int exist=0;
			ArrayList<String> actualLinks = metadataCard.getPropertyDescriptionLinks("Project manager");//Get the description link metadata value 

			for (int i=0;i< actualLinks.size();i++)
				if(actualLinks.get(i).trim().equalsIgnoreCase("http invalid M-Files web link")){//Checks if expected link text is same as displayed in the property description
					exist = 1;//exist is set to 1 if expected link text is displayed in the property description
					break;
				}

			if(exist != 1)//Checks expected Valid link text is displayed in the property description 
				throw new Exception(" Expected Valid URL link text(http invalid M-Files web link) is not displayed in the property description");

			Log.message("3. Valid Description link : 'http invalid M-Files web link' is displayed for 'Project manager' property in new Project metadata card.");	

			//Step-3 : Clicks the description link
			//------------------------------------
			metadataCard.clickPropertyDescriptionLink("Project manager","http invalid M-Files web link");//Click the link for property description

			Log.message("4. Description link : 'http invalid M-Files web link' is clicked from the property description in the new 'Project' object metadata card.");

			//Verification : Invalid link URL is opened or not
			//-------------------------------------------------
			if (Utility.tabExists(driver, "http://www.invalidm-files.com/en", "URL"))
				Log.pass("Test Case Passed. New webpage is opened with the 'http://www.invalidm-files.com/en' url which is mentioned in 'Project manager' property description in metadata card", driver);
			else
				Log.fail("Test Case Failed.New webpage is not opened with the 'http://www.invalidm-files.com/en' url which is mentioned in 'Project manager' property description in metadata card", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			driver = Utility.switchToTab(driver, "M-Files Web", "title");
			Utility.quitDriver(driver);
		}//End finally
	}//End 1.2.12.2.2C

	/**
	 * 1.2.12.2.2D : Property description contains invalid http link [Existing Object - Pop out metadatacard]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"SKIP_MultiSelect", "MetadataConfigurability", "PropertyDescription"}, description = "Property description contains invalid http link [Existing Object - Pop out metadatacard]")
	public void TC_1_2_12_2_2D(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Logged into MFiles Web Access
			//-----------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched Webdriver and logged into MFWA with valid credentials

			//Step-1 : Navigate to the specified view
			//---------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), " ");

			Log.message("1. Navigated to " + viewtonavigate + " view.");

			//Step-2 : Select any existing object in the view
			//-----------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("ObjectsToClick"));//Clicks the existing objects in the view

			homePage.menuBar.ClickOperationsMenu("Properties");//Opens the popped out metadatacard

			Log.message("2. Existing objects is selected and the popped out metadatacard of the multiple objects is opened in the view.", driver);

			//Step-3 : check the expected description link is displayed in the property description 
			//-------------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); //Instantiate the metadata card
			int exist=0;
			ArrayList<String> actualLinks = metadataCard.getPropertyDescriptionLinks("Project manager");//Get the description link metadata value 

			for (int i=0;i< actualLinks.size();i++)
				if(actualLinks.get(i).trim().equalsIgnoreCase("http invalid M-Files web link")){//Checks if expected link text is same as displayed in the property description
					exist = 1;//exist is set to 1 if expected link text is displayed in the property description
					break;
				}

			if(exist != 1)//Checks expected Valid link text is displayed in the property description 
				throw new Exception(" Expected Valid URL link text(http invalid M-Files web link) is not displayed in the property description");

			Log.message("3. Valid Description link : 'http invalid M-Files web link' is displayed for 'Project manager' property in new Project metadata card.");	

			//Step-3 : Clicks the description link
			//------------------------------------
			metadataCard.clickPropertyDescriptionLink("Project manager","http invalid M-Files web link");//Click the link for property description

			Log.message("4. Description link : 'http invalid M-Files web link' is clicked from the property description in the new 'Project' object metadata card.");

			//Verification : Invalid link URL is opened or not
			//-------------------------------------------------
			if (Utility.tabExists(driver, "http://www.invalidm-files.com/en", "URL"))
				Log.pass("Test Case Passed. New webpage is opened with the 'http://www.invalidm-files.com/en' url which is mentioned in 'Project manager' property description in metadata card", driver);
			else
				Log.fail("Test Case Failed.New webpage is not opened with the 'http://www.invalidm-files.com/en' url which is mentioned in 'Project manager' property description in metadata card", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			driver = Utility.switchToTab(driver, "M-Files Web", "title");
			Utility.quitDriver(driver);
		}//End finally
	}//End 1.2.12.2.2D

	/**
	 * 105.13.19 : Focus should be set on 'Customer Name' property along with description on clicking new object link in metadata card.
	 */
	@Test(groups = {"Sprint105", "MetadataConfigurability", "PropertyDescription"}, description = "Focus should be set on 'Customer Name' property along with description on clicking new object link in metadata card.")
	public void SprintTest105_13_19() throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//logged into MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Click the new 'Customer' object from the Taskpanel
			//-----------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Customer.Value);
			Log.message("1. Click the 'Customer' object from the Menubar.");

			//Step-2 : Click the Customer property in Metadata card
			//-----------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); //Instantiate the metadata card
			metadataCard.savePropValue("Customer name");

			Log.message("2. Property : 'Customer name' is clicked from the metadata card.");

			String description = metadataCard.getPropertyDescriptionValue("Customer name");

			//Verification : Verify if Description is available in 'Customer name' field
			//--------------------------------------------------------------------------
			if (description.equals("test"))
				Log.pass("Test Case Passed. Description 'test' is displayed on above 'Customer name' property.");
			else
				Log.fail("Test Case failed. Description 'test' is not displayed on above 'Customer name' property.", driver);

		}

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End SprintTest105_13_19


	/**
	 * 105.13.20A : Description should be  displayed on adding a new property for creating new Object[eg.keywords] - through taskpanel.
	 * 
	 */
	@Test(groups = {"Sprint105", "MetadataConfigurability", "PropertyDescription"}, description = "Description should be  displayed on adding a new property for creating new Object[eg.keywords] - through taskpanel.")
	public void SprintTest105_13_20A() throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Login to MFiles Web Access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Click the new 'Customer' object from the Taskpanel
			//-----------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Customer.Value);

			Log.message("1. Click the 'Customer' object from the Taskpanel.");

			//Step-2 : Added the new property 'Keyword' for newly created object
			//------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); //Instantiate the metadata card
			metadataCard.addNewProperty("Keywords");

			Log.message("2. Added the Property : 'Keywords' in newly created 'Customer' metadata card.");

			//Step-3 : Property 'Keywords' is clicked
			//---------------------------------------
			metadataCard.savePropValue("Keywords");

			Log.message("3. Property 'Keywords' is clicked in newly created 'Customer' metadata card.");

			String description = metadataCard.getPropertyDescriptionValue("Customer name");
			//Verification : Verify if Description is available in 'Customer name' field
			//--------------------------------------------------------------------------
			if (description.equals("test"))
				Log.pass("Test Case Passed. Description 'test' is displayed for newly created 'Keywords' property.");
			else
				Log.fail("Test Case failed. Description 'test' is not displayed for newly created 'Keywords' property.", driver);

		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End SprintTest105_13_20A

	/**
	 * 105.13.20B : Description should be  displayed on adding a new property for creating new Object - through menu bar
	 */

	@Test(groups = {"Sprint105", "MetadataConfigurability", "PropertyDescription"}, description = "Description should be  displayed on adding a new property for creating new Object - through menu bar.")
	public void SprintTest105_13_20B() throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Login to MFiles Web Access
			//--------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-2 : Click the new 'Customer' object from the Taskpanel
			//-----------------------------------------------------------
			homePage.menuBar.clickNewMenuItem(Caption.ObjecTypes.Customer.Value);
			Log.message("1. Click the 'Customer' object from the Menubar.");

			//Step-2 : Added the new property 'Keyword' for newly created object
			//------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); //Instantiate the metadata card
			metadataCard.addNewProperty("Keywords");

			Log.message("2. Added the Property : 'Keywords' in newly created 'Customer' metadata card.");

			//Step-3 : Property 'Keywords' is clicked
			//---------------------------------------
			metadataCard.savePropValue("Keywords");

			Log.message("3. Property 'Keywords' is clicked in newly created 'Customer' metadata card..");

			String description = metadataCard.getPropertyDescriptionValue("Customer name");
			//Verification : Verify if Description is available in 'Customer name' field
			//--------------------------------------------------------------------------
			if (description.equals("test"))
				Log.pass("Test Case Passed. Description 'test' is displayed for newly created 'Keywords' property.");
			else
				Log.fail("Test Case failed. Description 'test' is not displayed for newly created 'Keywords' property.", driver);

		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End SprintTest105_13_20B


	/**
	 * 105.13.20C : Description should be  displayed on adding a new property for an existing  Objet properties - from right pane metadata card
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Sprint105", "MetadataConfigurability", "PropertyDescription"}, description = "Description should be  displayed on adding a new property for an existing  Objet properties - from right pane metadata card.")
	public void SprintTest105_13_20C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Logged into MFiles Web Access
			//-----------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view  
			//-----------------------------------
			homePage.taskPanel.clickItem("Home");
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), " ");			

			Log.message("1. Navigate to '" + viewtonavigate + "' view.");

			//Step-2 : Select the Object from the listview
			//--------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectToClick"));//Click the customer object item from the list view

			Log.message("2. Object " + dataPool.get("ObjectToClick") + " is selected from listview.");

			//Step-2 : Added the new property 'Keyword' for newly created object
			//------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver,true); //Instantiate the metadata card
			metadataCard.addNewProperty("Keywords");

			Log.message("3. Added the Property : 'Keywords' in newly created 'Customer' metadata card.");

			//Step-3 : Property 'Keywords' is clicked
			//---------------------------------------
			metadataCard.savePropValue("Keywords");

			Log.message("4. Property 'Keywords' is clicked in newly created 'Customer' metadata card.");

			String description = metadataCard.getPropertyDescriptionValue("Customer name");
			//Verification : Verify if Description is available in 'Customer name' field
			//--------------------------------------------------------------------------
			if (description.equals("test"))
				Log.pass("Test Case Passed. Description 'test' is displayed for newly created 'Keywords' property.");
			else
				Log.fail("Test Case failed. Description 'test' is not displayed for newly created 'Keywords' property.", driver);

		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End SprintTest105_13_20C


	/**
	 * 105.13.21 : Description should be  displayed on adding a new property for an existing  Objet properties - from right pane metadata card
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Sprint105", "MetadataConfigurability", "PropertyDescription"},
			description = "Description should be  displayed on adding a new property for an existing  Objet properties - from right pane metadata card.")
	public void SprintTest105_13_21(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Logged into MFiles Web Access
			//-----------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view  
			//-----------------------------------
			homePage.taskPanel.clickItem("Home");
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), " ");			

			Log.message("1. Navigate to '" + viewtonavigate + "' view.");

			//Step-2 : Select the Object from the listview
			//--------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectToClick"));//Click the customer object item from the list view

			Log.message("2. Object " + dataPool.get("ObjectToClick") + " is selected from listview.");

			//Step-3 : 'Make Copy' link is clicked from the Taskpanel
			//-------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.MakeCopy.Value);

			Log.message("3. 'Make Copy' link is clicked from Taskpanel.");

			//Verification : Verify if Description is available in 'Customer name' field
			//--------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); //Instantiate the metadata card

			metadataCard.clickProperty("Customer name");

			String description = metadataCard.getPropertyDescriptionValue("Customer name");

			//Verification : Verify if Description is available in 'Customer name' field
			//--------------------------------------------------------------------------
			if (description.equals("test"))
				Log.pass("Test Case Passed. Description 'test' is displayed for property 'Customer name' from the Make Copy option.");
			else
				Log.fail("Test Case failed. Description 'test' is not displayed ffor property 'Customer name' from the Make Copy option.", driver);

		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End SprintTest105_13_21

	/**
	 * 105.13.22 : Description should be  displayed on adding a new property for an existing  Objet properties - from right pane metadata card.
	 * 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Sprint105", "MetadataConfigurability", "PropertyDescription"}, 
			description = "Description should be  displayed on adding a new property for an existing  Objet properties - from right pane metadata card.")
	public void SprintTest105_13_22(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Logged into MFiles Web Access
			//-----------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view  
			//-----------------------------------
			homePage.taskPanel.clickItem("Home");
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), " ");			

			Log.message("1. Navigate to '" + viewtonavigate + "' view.");

			//Step-2 : object is selected from the list view
			//----------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectToClick"));//Click the customer object item from the list view

			Log.message("2. Object " + dataPool.get("ObjectToClick") + " is selected from listview.");

			//Step-3 : 'Metadata tab' is right clicked in right pane menu
			//-----------------------------------------------------------
			homePage.previewPane.popoutRightPaneMetadataTab();//Popout the metadata card in rightpane metadatatab

			Log.message("3. Pop-out metadata tab is displayed by right clicking the preview pane metadata tab.");

			//Step-5 : Property 'Customer name' is clicked
			//--------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); //Instantiate the metadata card
			metadataCard.savePropValue("Customer name");

			Log.message("4. Property 'Customer name' is clicked in existing customer :  '" + dataPool.get("ObjectToClick") + "' metadata card.");


			String description = metadataCard.getPropertyDescriptionValue("Customer name");

			//Verification : Verify if Description is available in 'Customer name' field
			//--------------------------------------------------------------------------
			if (description.equals("test"))
				Log.pass("Test Case Passed. Description 'test' is displayed for property 'Customer name' from the pop-out metadata card.");
			else
				Log.fail("Test Case failed. Description 'test' is not displayed for property 'Customer name' from the pop-out metadata card.", driver);


		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End SprintTest105_13_22

	/**
	 * 105.13.26A : Description should be displayed when opening the object from history view- through task pane
	 * 
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Sprint105", "MetadataConfigurability", "PropertyDescription"}, 
			description = "Description should be displayed when opening the object from history view- through task pane.")
	public void SprintTest105_13_26A(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Logged into MFiles Web Access
			//-----------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view  
			//-----------------------------------
			homePage.taskPanel.clickItem("Home");
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), " ");			

			Log.message("1. Navigate to '" + viewtonavigate + "' view.");

			//check the object exists in list view
			//-------------------------------------
			if (!(homePage.listView.isItemExists(dataPool.get("ObjectToClick"))))
				throw new SkipException("Invalid Test Data. The specified Object " + dataPool.get("ObjectToClick") + " was not found in the vault."); 


			//Step-2 : Object is selected from the list view
			//----------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectToClick"));//Click the customer object item from the list view

			Log.message("2. Object " + dataPool.get("ObjectToClick") + " is selected from listview.");

			//Step-3 : 'History' view is clicked from the task panel
			//------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.History.Value);//Click the history option from task panel

			Log.message("3. 'History' view of object " + dataPool.get("ObjectToClick") + " is opened.");

			//Step-4 : Click on the latest version of the object
			//--------------------------------------------------
			if (!homePage.listView.clickItemByIndex(0)) //Selects the latest version of the object 
				throw new Exception("Latest version of an object (" + dataPool.get("ObjectName") + ") is not selected.");

			Log.message("4. Latest version of an object (" + dataPool.get("ObjectName") + ") is got selected.");

			//Step-5 : Click on the 'Properties' option from the taskpanel
			//------------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			Log.message("5. Click on the 'Properties' option from the taskpanel.");

			//Step-6 : Property 'Customer name' is clicked
			//--------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); //Instantiate the metadata card
			metadataCard.savePropValue("Customer name");

			Log.message("6. Property 'Customer name' is clicked in existing customer :  '" + dataPool.get("ObjectToClick") + "' metadata card.");


			String description = metadataCard.getPropertyDescriptionValue("Customer name");

			//Verification : Verify if Description is available in 'Customer name' field
			//--------------------------------------------------------------------------
			if (description.equals("test"))
				Log.pass("Test Case Passed. Description 'test' is displayed for property 'Customer name' in history view.");
			else
				Log.fail("Test Case failed. Description 'test' is not displayed for property 'Customer name' in history view.", driver);


		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End SprintTest105_13_26A


	/**
	 * 105.13.26B : Description should be displayed when opening the object from history view- through Context menu
	 * 
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Sprint105", "MetadataConfigurability", "PropertyDescription"}, 
			description = "Description should be displayed when opening the object from history view- through Context menu.")
	public void SprintTest105_13_26B(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Logged into MFiles Web Access
			//-----------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view  
			//-----------------------------------
			homePage.taskPanel.clickItem("Home");
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), " ");			

			Log.message("1. Navigate to '" + viewtonavigate + "' view.");

			//Check the object is existing in listview or not 
			//-----------------------------------------------
			if (!(homePage.listView.isItemExists(dataPool.get("ObjectToClick"))))
				throw new SkipException("Invalid Test Data. The specified Object " + dataPool.get("ObjectToClick") + " was not found in the vault."); 


			//Step-2 : Object is selected from the list view
			//----------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectToClick"));//Click the customer object item from the list view

			Log.message("2. Object " + dataPool.get("ObjectToClick") + " is selected from listview.");

			//Step-3 : 'History' view is clicked from the task panel
			//------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.History.Value);//Click the history option from task panel

			Log.message("3. 'History' view of object " + dataPool.get("ObjectToClick") + " is opened.");

			//Step-4 : Click on the latest version of the object
			//--------------------------------------------------
			if (!homePage.listView.rightClickItemByIndex(0)) //Selects the latest version of the object 
				throw new Exception("Latest version of an object (" + dataPool.get("ObjectName") + ") is not selected.");

			Log.message("4. Right clicked the Latest version of an object (" + dataPool.get("ObjectName") + ") in history view.");

			//Step-5 : Click on the 'Properties' option from the taskpanel
			//------------------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value);

			Log.message("5. Option 'Properties' is clicked from the Context Menu item.");

			//Step-6 : Property 'Customer name' is clicked
			//--------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); //Instantiate the metadata card
			metadataCard.savePropValue("Customer name");

			Log.message("6. Property 'Customer name' is clicked in existing customer object :  '" + dataPool.get("ObjectToClick") + "' in metadata card.");

			String description = metadataCard.getPropertyDescriptionValue("Customer name");
			//Verification : Verify if Description is available in 'Customer name' field
			//--------------------------------------------------------------------------
			if (description.equals("test"))
				Log.pass("Test Case Passed. Description 'test' is displayed for property 'Customer name' in history view.");
			else
				Log.fail("Test Case failed. Description 'test' is not displayed for property 'Customer name' in history view.", driver);


		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End SprintTest105_13_26B

	/**
	 * 105.13.26C : Description should be displayed when opening the object from history view- through Operation menu.		 * 
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Sprint105", "MetadataConfigurability", "PropertyDescription"}, 
			description = "Description should be displayed when opening the object from history view- through Operation menu.")
	public void SprintTest105_13_26C(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Logged into MFiles Web Access
			//-----------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view  
			//-----------------------------------
			homePage.taskPanel.clickItem("Home");
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), " ");			

			Log.message("1. Navigate to '" + viewtonavigate + "' view.");

			//Check the object is exists in the listview
			//---------------------------------------------
			if (!(homePage.listView.isItemExists(dataPool.get("ObjectToClick"))))
				throw new SkipException("Invalid Test Data. The specified Object " + dataPool.get("ObjectToClick") + " was not found in the vault."); 


			//Step-2 : Object is selected from the list view
			//----------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectToClick"));//Click the customer object item from the list view

			Log.message("2. Object " + dataPool.get("ObjectToClick") + " is selected from listview.");

			//Step-3 : 'History' view is clicked from the task panel
			//------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.History.Value);//Click the history option from task panel

			Log.message("3. 'History' view of object " + dataPool.get("ObjectToClick") + " is opened.");

			//Step-4 : Click on the latest version of the object
			//--------------------------------------------------
			if (!homePage.listView.clickItemByIndex(0)) //Selects the latest version of the object 
				throw new Exception("Latest version of an object (" + dataPool.get("ObjectName") + ") is not selected.");

			Log.message("4. Latest version of an object got selected.");

			//Step-5 : Click on the 'Properties' option from the taskpanel
			//------------------------------------------------------------
			homePage.menuBar.ClickSettingsItem(Caption.MenuItems.Properties.Value);

			Log.message("5. Click on the 'Properties' option from the Settings menu.");


			//Step-6 : Property 'Customer name' is clicked
			//--------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); //Instantiate the metadata card
			metadataCard.savePropValue("Customer name");

			Log.message("6. Property 'Customer name' is clicked in existing customer :  '" + dataPool.get("ObjectToClick") + "' metadata card.");

			String description = metadataCard.getPropertyDescriptionValue("Customer name");
			//Verification : Verify if Description is available in 'Customer name' field
			//--------------------------------------------------------------------------
			if (description.equals("test"))
				Log.pass("Test Case Passed. Description 'test' is displayed for property 'Customer name' in history view.");
			else
				Log.fail("Test Case failed. Description 'test' is not displayed for property 'Customer name' in history view.", driver);


		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End SprintTest105.13.26C

	/**
	 * 105.13.27A : Metadata layout should not affected on providing a very long text Description for selected properties-through Right pane metadata card.
	 * 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Sprint105", "MetadataConfigurability", "PropertyDescription"}, 
			description = "Metadata layout should not affected on providing a very long text Description for selected properties-through Right pane metadata card.")
	public void SprintTest105_13_27A (HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Logged into MFiles Web Access
			//-----------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to any search view
			//------------------------------------
			homePage.searchPanel.clickSearch();

			Log.message("1. Navigate to search view by clicking the Search icon.");

			//Step-2 : Selected the any existing 'document' object
			//---------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectToClick"));

			Log.message("2. Selected the document " + dataPool.get("ObjectToClick") + " from the list view.");

			//Step-3 : Clicked the property : 'Name or Title' in Document object
			//------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver,true); 
			metadataCard.savePropValue("Name or title");

			Log.message("3. Property 'Name or title' is clicked in existing customer :  '" + dataPool.get("ObjectToClick") + "' metadata card.");

			String description = metadataCard.getPropertyDescriptionValue("Name or title");

			//Verification : Verify if Description is available in 'Customer name' field
			//--------------------------------------------------------------------------
			if (description.equals("Tooltip_plan test automation in execution for the 12.0.4830.0 build in 105.13.27 case"))
				Log.pass("Test Case Passed. Document property : 'Name or Title' description is displayed without affecting the metadata layout .");
			else
				Log.fail("Test Case failed.Document property : 'Name or Title'  is not displayed with description in top of the respective field. ", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End SprintTest105.13.27A

	/**
	 * 105.13.27B : Metadata layout should not affected on providing a very long text Description for selected properties-through task panel.
	 * 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Sprint105", "MetadataConfigurability", "PropertyDescription"}, 
			description = "Metadata layout should not affected on providing a very long text Description for selected properties-through task panel.")
	public void SprintTest105_13_27B (HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Logged into MFiles Web Access
			//-----------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to any search view
			//------------------------------------
			homePage.searchPanel.clickSearch();

			Log.message("1. Navigate to search view by clicking the Search icon.");

			//Step-2 : Selected the any existing 'document' object
			//---------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectToClick"));

			Log.message("2. Selected the document " + dataPool.get("ObjectToClick") + " from the list view.");

			//Step-3 : Clicked option 'Properties' in task panel
			//--------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);

			Log.message("3. 'Properties' option is clicked from the task panel.");


			//Step-3 : Clicked the property : 'Name or Title' in Document object
			//------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); 
			metadataCard.savePropValue("Name or title");

			Log.message("3. Property 'Name or title' is clicked in existing customer :  '" + dataPool.get("ObjectToClick") + "' metadata card.");

			String description = metadataCard.getPropertyDescriptionValue("Name or title");

			//Verification : Verify if Description is available in 'Customer name' field
			//--------------------------------------------------------------------------
			if (description.equals("Tooltip_plan test automation in execution for the 12.0.4830.0 build in 105.13.27 case"))
				Log.pass("Test Case Passed. Document property : 'Name or Title' description is displayed without affecting the metadata layout .");
			else
				Log.fail("Test Case failed.Document property : 'Name or Title'  is not displayed with description in top of the respective field. ", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End SprintTest105.13.27B


	/**
	 * 105.13.27C : Metadata layout should not affected on providing a very long text Description for selected properties-through Context menu.
	 * 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Sprint105", "MetadataConfigurability", "PropertyDescription"}, 
			description = "Metadata layout should not affected on providing a very long text Description for selected properties-through Context menu")
	public void SprintTest105_13_27C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Logged into MFiles Web Access
			//-----------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to any search view
			//------------------------------------
			homePage.searchPanel.clickSearch();

			Log.message("1. Navigate to search view by clicking the Search icon.");

			//Step-2 : Selected the any existing 'document' object
			//---------------------------------------------------
			homePage.listView.rightClickItem(dataPool.get("ObjectToClick"));

			Log.message("2. Selected the document " + dataPool.get("ObjectToClick") + " from the list view.");

			//Step-3 : Clicked option 'Properties' in task panel
			//--------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value);

			Log.message("3. 'Properties' option is clicked from the Context menu Item.");


			//Step-3 : Clicked the property : 'Name or Title' in Document object
			//------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); 
			metadataCard.savePropValue("Name or title");

			Log.message("4. Property 'Name or title' is clicked in existing customer :  '" + dataPool.get("ObjectToClick") + "' metadata card.");

			String description = metadataCard.getPropertyDescriptionValue("Name or title");

			//Verification : Verify if Description is available in 'Customer name' field
			//--------------------------------------------------------------------------
			if (description.equals("Tooltip_plan test automation in execution for the 12.0.4830.0 build in 105.13.27 case"))
				Log.pass("Test Case Passed. Document property : 'Name or Title' description is displayed without affecting the metadata layout .");
			else
				Log.fail("Test Case failed.Document property : 'Name or Title'  is not displayed with description in top of the respective field. ", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End SprintTest105.13.27C


	/**
	 * 105.13.27D : Metadata layout should not affected on providing a very long text Description for selected properties- through Settings menu.
	 * 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Sprint105", "MetadataConfigurability", "PropertyDescription"}, 
			description = "Metadata layout should not affected on providing a very long text Description for selected properties-through Settings menu")
	public void SprintTest105_13_27D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Logged into MFiles Web Access
			//-----------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to any search view
			//------------------------------------
			homePage.searchPanel.clickSearch();

			Log.message("1. Navigate to search view by clicking the Search icon.");

			//Step-2 : Selected the any existing 'document' object
			//---------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectToClick"));

			Log.message("2. Selected the document " + dataPool.get("ObjectToClick") + " from the list view.");

			//Step-3 : Clicked option 'Properties' in task panel
			//--------------------------------------------------
			homePage.menuBar.clickSettingsMenuItems(Caption.MenuItems.Properties.Value);

			Log.message("3. 'Properties' option is clicked from the Context menu option.");


			//Step-3 : Clicked the property : 'Name or Title' in Document object
			//------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver); 
			metadataCard.savePropValue("Name or title");

			Log.message("4. Property 'Name or title' is clicked in existing customer :  '" + dataPool.get("ObjectToClick") + "' metadata card.");

			String description = metadataCard.getPropertyDescriptionValue("Name or title");

			//Verification : Verify if Description is available in 'Customer name' field
			//--------------------------------------s------------------------------------
			if (description.equals("Tooltip_plan test automation in execution for the 12.0.4830.0 build in 105.13.27 case"))
				Log.pass("Test Case Passed. Document property : 'Name or Title' description is displayed without affecting the metadata layout .");
			else
				Log.fail("Test Case failed.Document property : 'Name or Title'  is not displayed with description in top of the respective field. ", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End SprintTest105.13.27D

	/**
	 * TC.1.2.10.1A : Verify if Javascript is displayed as plain text in MetadataCard & Property Description [Object creation via Task pane]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability", "MetadataConfigurability", "MetadataDescription", "PropertyDescription"}, 
			description = "Verify if Javascript is displayed as plain text in MetadataCard & Property Description [Object creation via Task pane]")
	public void TC_1_2_10_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Click the new object link from task pane
			//------------------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));//Clicks the new object link from task pane

			Log.message("1." + dataPool.get("ObjectType") + " is clicked from New item in task pane", driver);

			//Step-2:Checks the metadatacard & property descriptions in the metadatacard
			//--------------------------------------------------------------------------
			String ExpectedMetadata="";

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard



			if (!metadataCard.getMetadataDescriptionText().trim().equalsIgnoreCase(dataPool.get("MetadataCardDesc").trim()))
				ExpectedMetadata = "MetadataCard description is not displayed as expected["+dataPool.get("MetadataCardDesc")+"].[Actual Value: "+metadataCard.getMetadataDescriptionText()+"].";

			if (!metadataCard.getPropertyDescriptionValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyDesc")))
				ExpectedMetadata = " Property description is not displayed as expected["+dataPool.get("PropertyDesc")+"] for the property("+dataPool.get("Property")+").[Actual Value: "+metadataCard.getPropertyDescriptionValue(dataPool.get("Property"))+"]";

			//Verification : Verify if metadata configurability is working as expected 
			//------------------------------------------------------------------------
			if(ExpectedMetadata.equalsIgnoreCase(""))
				Log.pass("Test Case Passed. Javascript is displayed as plain text in MetadataCard & Property description [Object creation via Task pane]");
			else
				Log.fail("Test Case Failed. Javascript is not set as plain text in MetadataCard/Property description [Object creation via Task pane]. For more details: "+ExpectedMetadata, driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_10_1A

	/**
	 * TC.1.2.10.1B : Verify if Javascript is displayed as plain text in MetadataCard & Property Description [Object creation via New item menu]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if Javascript is displayed as plain text in MetadataCard & Property Description  [Object creation via New item menu]")
	public void TC_1_2_10_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Click the new object link from new item menu
			//---------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//Clicks the new menu item

			Log.message("1." + dataPool.get("ObjectType") + " is clicked from new item menu bar", driver);

			//Step-2:Checks the metadatacard & property descriptions in the metadatacard
			//--------------------------------------------------------------------------
			String ExpectedMetadata="";

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			if (!metadataCard.getMetadataDescriptionText().trim().equalsIgnoreCase(dataPool.get("MetadataCardDesc").trim()))
				ExpectedMetadata = "MetadataCard description is not displayed as expected["+dataPool.get("MetadataCardDesc")+"].[Actual Value: "+metadataCard.getMetadataDescriptionText()+"].";

			if (!metadataCard.getPropertyDescriptionValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyDesc")))
				ExpectedMetadata = " Property description is not displayed as expected["+dataPool.get("PropertyDesc")+"] for the property("+dataPool.get("Property")+").[Actual Value: "+metadataCard.getPropertyDescriptionValue(dataPool.get("Property"))+"]";

			//Verification : Verify if metadata configurability is working as expected 
			//------------------------------------------------------------------------
			if(ExpectedMetadata.equalsIgnoreCase(""))
				Log.pass("Test Case Passed. Javascript is displayed as plain text in MetadataCard & Property description [Object creation via New item menu]");
			else
				Log.fail("Test Case Failed. Javascript is not set as plain text in MetadataCard/Property description [Object creation via New item menu]. For more details: "+ExpectedMetadata, driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_10_1B

	/**
	 * TC.1.2.10.1C : Verify if Javascript is displayed as plain text in MetadataCard & Property Description [Object creation via Object property]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if Javascript is displayed as plain text in MetadataCard & Property Description [Object creation via Object property]")
	public void TC_1_2_10_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to : " + viewToNavigate + " view.");

			//Step-2 : Select any existing object
			//------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			Log.message("2. Selected the specified object : " + dataPool.get("Object") + " in list view.");

			//Step-3 : Clicks the new field link in the object lookup property in the metadatacard
			//------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard of existing object

			metadataCard.clickAddValueButton(dataPool.get("ObjectType"));//Clicks the Add value button to open the new customer object metadatacard

			Log.message("3. Opened the new object(" + dataPool.get("ObjectType") +  ") metadatacard via object type property", driver);

			//Step-4:Checks the metadatacard & property descriptions in the metadatacard
			//--------------------------------------------------------------------------
			String ExpectedMetadata="";

			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			if (!metadataCard.getMetadataDescriptionText().equalsIgnoreCase(dataPool.get("MetadataCardDesc")))
				ExpectedMetadata = "MetadataCard description is not displayed as expected["+dataPool.get("MetadataCardDesc")+"].[Actual Value: "+metadataCard.getMetadataDescriptionText()+"].";

			if (!metadataCard.getPropertyDescriptionValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyDesc")))
				ExpectedMetadata = " Property description is not displayed as expected["+dataPool.get("PropertyDesc")+"] for the property("+dataPool.get("Property")+").[Actual Value: "+metadataCard.getPropertyDescriptionValue(dataPool.get("Property"))+"]";

			//Verification : Verify if metadata configurability is working as expected 
			//------------------------------------------------------------------------
			if(ExpectedMetadata.equalsIgnoreCase(""))
				Log.pass("Test Case Passed. Javascript is displayed as plain text in MetadataCard & Property description [Object creation via Object property]");
			else
				Log.fail("Test Case Failed. Javascript is not set as plain text in MetadataCard/Property description [Object creation via Object property]. For more details: "+ExpectedMetadata, driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_10_1C

	/**
	 * TC_1.2.1.2A : Verify if Javascript is displayed as plain text in MetadataCard & Property Description [Existing object metadatacard]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if Javascript is displayed as plain text in MetadataCard & Property Description [Existing object metadatacard].")
	public void TC_1_2_10_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to : " + viewToNavigate + " view.");

			//Step-2 : Select any existing object
			//------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			Log.message("2. Selected the specified object : " + dataPool.get("Object") + " in list view.", driver);

			//Step-3:Checks the metadatacard & property descriptions in the metadatacard
			//--------------------------------------------------------------------------
			String ExpectedMetadata="";

			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (!metadataCard.getMetadataDescriptionText().trim().equalsIgnoreCase(dataPool.get("MetadataCardDesc").trim()))
				ExpectedMetadata = "MetadataCard description is not displayed as expected["+dataPool.get("MetadataCardDesc")+"].[Actual Value: "+metadataCard.getMetadataDescriptionText()+"].";

			if (!metadataCard.getPropertyDescriptionValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyDesc")))
				ExpectedMetadata = " Property description is not displayed as expected["+dataPool.get("PropertyDesc")+"] for the property("+dataPool.get("Property")+").[Actual Value: "+metadataCard.getPropertyDescriptionValue(dataPool.get("Property"))+"]";

			//Verification : Verify if metadata configurability is working as expected 
			//------------------------------------------------------------------------
			if(ExpectedMetadata.equalsIgnoreCase(""))
				Log.pass("Test Case Passed. Javascript is displayed as plain text in MetadataCard & Property description [Existing object metadata card]");
			else
				Log.fail("Test Case Failed. Javascript is not set as plain text in MetadataCard/Property description [Existing object metadata card]. For more details: "+ExpectedMetadata, driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_10_2A

	/**
	 * TC.1.2.10.2B : Verify if Javascript is displayed as plain text in MetadataCard & Property Description [Existing object popout metadata card].
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if Javascript is displayed as plain text in MetadataCard & Property Description [Existing object popout metadata card].")
	public void TC_1_2_10_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to : " + viewToNavigate + " view.");

			//Step-2 : Select any existing object
			//------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			Log.message("2. Selected the specified object : " + dataPool.get("Object") + " in list view.");

			//Step-3: Open the popout metadatacard of the object
			//--------------------------------------------------
			homePage.taskPanel.clickItem("Properties");//Clicks the properties option from task pane

			Log.message("3. Existing object popped out metadatacard is opened via task pane", driver);

			//Step-4:Checks the metadatacard & property descriptions in the metadatacard
			//--------------------------------------------------------------------------
			String ExpectedMetadata="";

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard


			if (!metadataCard.getMetadataDescriptionText().equalsIgnoreCase(dataPool.get("MetadataCardDesc")))
				ExpectedMetadata = "MetadataCard description is not displayed as expected["+dataPool.get("MetadataCardDesc")+"].[Actual Value: "+metadataCard.getMetadataDescriptionText()+"].";

			if (!metadataCard.getPropertyDescriptionValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyDesc")))
				ExpectedMetadata = " Property description is not displayed as expected["+dataPool.get("PropertyDesc")+"] for the property("+dataPool.get("Property")+").[Actual Value: "+metadataCard.getPropertyDescriptionValue(dataPool.get("Property"))+"]";

			//Verification : Verify if metadata configurability is working as expected 
			//------------------------------------------------------------------------
			if(ExpectedMetadata.equalsIgnoreCase(""))
				Log.pass("Test Case Passed. Javascript is displayed as plain text in MetadataCard & Property description [Existing object popout metadata card]");
			else
				Log.fail("Test Case Failed. Javascript is not set as plain text in MetadataCard/Property description [Existing object popout metadata card]. For more details: "+ExpectedMetadata, driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_10_2B

	/**
	 * TC_1.2.10.3A : Verify if Javascript is displayed as plain text in MetadataCard & Property Description [Multiple Existing objects metadata card].
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if Javascript is displayed as plain text in MetadataCard & Property Description [Multiple Existing objects metadata card].")
	public void TC_1_2_10_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to : " + viewToNavigate + " view.");

			//Step-2 : Select any existing object
			//------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));//Selects the multiple objects in the view

			Log.message("2. Multiple objects selected in the list view.", driver);

			//Step-3:Checks the metadatacard & property descriptions in the metadatacard
			//--------------------------------------------------------------------------
			String ExpectedMetadata="";

			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if(driverType.equalsIgnoreCase("edge"))


				if (!metadataCard.getMetadataDescriptionText().equalsIgnoreCase(dataPool.get("MetadataCardDesc")))
					ExpectedMetadata = "MetadataCard description is not displayed as expected["+dataPool.get("MetadataCardDesc")+"].[Actual Value: "+metadataCard.getMetadataDescriptionText()+"].";

			if (!metadataCard.getPropertyDescriptionValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyDesc")))
				ExpectedMetadata = " Property description is not displayed as expected["+dataPool.get("PropertyDesc")+"] for the property("+dataPool.get("Property")+").[Actual Value: "+metadataCard.getPropertyDescriptionValue(dataPool.get("Property"))+"]";

			//Verification : Verify if metadata configurability is working as expected 
			//------------------------------------------------------------------------
			if(ExpectedMetadata.equalsIgnoreCase(""))
				Log.pass("Test Case Passed. Javascript is displayed as plain text in MetadataCard & Property description [Multiple Existing objects metadata card]");
			else
				Log.fail("Test Case Failed. Javascript is not set as plain text in MetadataCard/Property description [Multiple Existing objects metadata card]. For more details: "+ExpectedMetadata, driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_10_3A

	/**
	 * TC_1.2.10.3B : Verify if Javascript is displayed as plain text in MetadataCard & Property Description [Multiple Existing objects popout metadata card].
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if Javascript is displayed as plain text in MetadataCard & Property Description [Multiple Existing objects popout metadata card].")
	public void TC_1_2_10_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to : " + viewToNavigate + " view.");

			//Step-2 : Select multiple existing objects
			//------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));//Selects the multiple objects in the view

			Log.message("2. Multiple objects selected in the list view.");

			//Step-3: Open the popout metadatacard of the object
			//--------------------------------------------------
			homePage.taskPanel.clickItem("Properties");//Clicks the properties option from task pane

			Log.message("3. Existing object popped out metadatacard is opened via task pane", driver);

			//Step-4:Checks the metadatacard & property descriptions in the metadatacard
			//--------------------------------------------------------------------------
			String ExpectedMetadata="";

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			if (!metadataCard.getMetadataDescriptionText().equalsIgnoreCase(dataPool.get("MetadataCardDesc")))
				ExpectedMetadata = "MetadataCard description is not displayed as expected["+dataPool.get("MetadataCardDesc")+"].[Actual Value: "+metadataCard.getMetadataDescriptionText()+"].";

			if (!metadataCard.getPropertyDescriptionValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyDesc")))
				ExpectedMetadata = " Property description is not displayed as expected["+dataPool.get("PropertyDesc")+"] for the property("+dataPool.get("Property")+").[Actual Value: "+metadataCard.getPropertyDescriptionValue(dataPool.get("Property"))+"]";

			//Verification : Verify if metadata configurability is working as expected 
			//------------------------------------------------------------------------
			if(ExpectedMetadata.equalsIgnoreCase(""))
				Log.pass("Test Case Passed. Javascript is displayed as plain text in MetadataCard & Property description [Multiple Existing objects popout metadata card]");
			else
				Log.fail("Test Case Failed. Javascript is not set as plain text in MetadataCard/Property description [Multiple Existing objects popout metadata card]. For more details: "+ExpectedMetadata, driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_10_3B

	/**
	 * TC.1.2.12.1.1A : Verify if invalid url webpage is launched without getting any error while click on the invalid http link in MetadataCard Description [Object creation via Task pane]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability", "MetadataConfigurability", "MetadataDescription", "PropertyDescription"}, 
			description = "Verify if invalid url webpage is launched without getting any error while click on the invalid http link in MetadataCard Description [Object creation via Task pane]")
	public void TC_1_2_12_1_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Click the new object link from task pane
			//------------------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));//Clicks the new object link from task pane

			Log.message("1. " + dataPool.get("ObjectType") + " is clicked from New item in task pane", driver);

			//Step-2:Checks the invalid link is displayed as expected in the metadatacard descriptions
			//----------------------------------------------------------------------------------------
			int exist = 0 ;
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard
			ArrayList<String> actualLinks = metadataCard.getMetadataDescriptionLinks();//Gets the available links displayed in the metadatacard description

			for (int i=0;i< actualLinks.size();i++)
				if(actualLinks.get(i).trim().equalsIgnoreCase(dataPool.get("LinkText"))){ //Checks if expected link text is same as displayed in the metadatacard description
					exist = 1;//exist is set to 1 if expected link text is displayed in the metadatcard description
					break;
				}

			if(exist != 1)//Checks expected invalid link text is displayed in the metadatacard description 
				throw new Exception(" Expected Invalid URL link text("+dataPool.get("LinkText")+") is not displayed in the metadatacard description");

			Log.message("2. Checked the invalid URL link text("+dataPool.get("LinkText")+") is displayed in the metadatacard description");

			//Step-3:Clicks the invalid link displayed in the metadatacard descriptions
			//----------------------------------------------------------------------------------------
			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.clickMetadataDescriptionLink(dataPool.get("LinkText"));//Clicks the metadatacard description link

			Log.message("3. Clicked the invalid URL link in the metadatacard description", driver);

			//Verifies if new tab is opened while click on the invalid link in the metadatcard description
			//------------------------------------------------------------------------------------------------------
			if (Utility.tabExists(driver, dataPool.get("InvalidLinkURL"), "URL"))
				Log.pass("Test Case Passed. Invalid webpage is opened while click on the invalid http link in MetadataCard description [Object creation via Task pane]", driver);
			else
				Log.fail("Test Case Failed. Invalid webpage is opened while click on the invalid http link in MetadataCard description [Object creation via Task pane].", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			driver = Utility.switchToTab(driver, "M-Files Web", "title");
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_12_1_1A

	/**
	 * TC.1.2.12.1.1B : Verify if invalid url webpage is launched without getting any error while click on the invalid http link in MetadataCard Description [Object creation via New item menu]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if invalid url webpage is launched without getting any error while click on the invalid http link in MetadataCard Description  [Object creation via New item menu]")
	public void TC_1_2_12_1_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();	

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Click the new object link from new item menu
			//---------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//Clicks the new menu item

			Log.message("1." + dataPool.get("ObjectType") + " is clicked from new item menu bar", driver);

			//Step-2:Checks the invalid link is displayed as expected in the metadatacard descriptions
			//----------------------------------------------------------------------------------------
			int exist = 0 ;
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard
			ArrayList<String> actualLinks = metadataCard.getMetadataDescriptionLinks();//Gets the available links displayed in the metadatacard description

			for (int i=0;i< actualLinks.size();i++)
				if(actualLinks.get(i).trim().equalsIgnoreCase(dataPool.get("LinkText"))){ //Checks if expected link text is same as displayed in the metadatacard description
					exist = 1;//exist is set to 1 if expected link text is displayed in the metadatcard description
					break;
				}

			if(exist != 1)//Checks expected invalid link text is displayed in the metadatacard description 
				throw new Exception(" Expected Invalid URL link text("+dataPool.get("LinkText")+") is not displayed in the metadatacard description");

			Log.message("2. Checked the invalid URL link text("+dataPool.get("LinkText")+") is displayed in the metadatacard description");

			//Step-3:Clicks the invalid link displayed in the metadatacard descriptions
			//----------------------------------------------------------------------------------------
			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.clickMetadataDescriptionLink(dataPool.get("LinkText"));//Clicks the metadatacard description link

			Log.message("3. Clicked the invalid URL link in the metadatacard description", driver);

			//Verifies if new tab is opened while click on the invalid link in the metadatcard description
			//------------------------------------------------------------------------------------------------------
			if (Utility.tabExists(driver, dataPool.get("InvalidLinkURL"), "URL"))
				Log.pass("Test Case Passed. Invalid webpage is opened while click on the invalid http link in MetadataCard description [Object creation via New item menu]", driver);
			else
				Log.fail("Test Case Failed. Invalid webpage is opened while click on the invalid http link in MetadataCard description [Object creation via New item menu].", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			driver = Utility.switchToTab(driver, "M-Files Web", "title");
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_12_1_1B

	/**
	 * TC.1.2.12.1.1C : Verify if invalid url webpage is launched without getting any error while click on the invalid http link in MetadataCard Description [Object creation via Object property]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if invalid url webpage is launched without getting any error while click on the invalid http link in MetadataCard Description [Object creation via Object property]")
	public void TC_1_2_12_1_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to : " + viewToNavigate + " view.");

			//Step-2 : Select any existing object
			//------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			Log.message("2. Selected the specified object : " + dataPool.get("Object") + " in list view.");

			//Step-3 : Clicks the new field link in the object lookup property in the metadatacard
			//------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard of existing object

			metadataCard.clickAddValueButton(dataPool.get("ObjectType"));//Clicks the Add value button to open the new customer object metadatacard

			Log.message("3. Opened the new object(" + dataPool.get("ObjectType") +  ") metadatacard via object type property", driver);

			//Step-4:Checks the invalid link is displayed as expected in the metadatacard descriptions
			//----------------------------------------------------------------------------------------
			int exist = 0 ;
			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard
			ArrayList<String> actualLinks = metadataCard.getMetadataDescriptionLinks();//Gets the available links displayed in the metadatacard description

			for (int i=0;i< actualLinks.size();i++)
				if(actualLinks.get(i).trim().equalsIgnoreCase(dataPool.get("LinkText"))){ //Checks if expected link text is same as displayed in the metadatacard description
					exist = 1;//exist is set to 1 if expected link text is displayed in the metadatcard description
					break;
				}

			if(exist != 1)//Checks expected invalid link text is displayed in the metadatacard description 
				throw new Exception(" Expected Invalid URL link text("+dataPool.get("LinkText")+") is not displayed in the metadatacard description");

			Log.message("4. Checked the invalid URL link text("+dataPool.get("LinkText")+") is displayed in the metadatacard description");

			//Step-5:Clicks the invalid link displayed in the metadatacard descriptions
			//----------------------------------------------------------------------------------------
			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.clickMetadataDescriptionLink(dataPool.get("LinkText"));//Clicks the metadatacard description link

			Log.message("5. Clicked the invalid URL link in the metadatacard description", driver);

			//Verifies if new tab is opened while click on the invalid link in the metadatcard description
			//------------------------------------------------------------------------------------------------------
			if (Utility.tabExists(driver, dataPool.get("InvalidLinkURL"), "URL"))
				Log.pass("Test Case Passed. Invalid webpage is opened while click on the invalid http link in MetadataCard description [Object creation via Object property]", driver);
			else
				Log.fail("Test Case Failed. Invalid webpage is opened while click on the invalid http link in MetadataCard description [Object creation via Object property].", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			driver = Utility.switchToTab(driver, "M-Files Web", "title");
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_12_1_1C

	/**
	 * TC.1.2.12.2.1A : Verify if invalid url webpage is launched without getting any error while click on the invalid http link in MetadataCard Description [Existing object metadatacard]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if invalid url webpage is launched without getting any error while click on the invalid http link in MetadataCard Description [Existing object metadatacard].")
	public void TC_1_2_12_2_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to : " + viewToNavigate + " view.");

			//Step-2 : Select any existing object
			//------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			Log.message("2. Selected the specified object : " + dataPool.get("Object") + " in list view.", driver);

			//Step-3:Checks the invalid link is displayed as expected in the metadatacard descriptions
			//----------------------------------------------------------------------------------------
			int exist = 0 ;
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard
			ArrayList<String> actualLinks = metadataCard.getMetadataDescriptionLinks();//Gets the available links displayed in the metadatacard description

			for (int i=0;i< actualLinks.size();i++)
				if(actualLinks.get(i).trim().equalsIgnoreCase(dataPool.get("LinkText"))){ //Checks if expected link text is same as displayed in the metadatacard description
					exist = 1;//exist is set to 1 if expected link text is displayed in the metadatcard description
					break;
				}

			if(exist != 1)//Checks expected invalid link text is displayed in the metadatacard description 
				throw new Exception(" Expected Invalid URL link text("+dataPool.get("LinkText")+") is not displayed in the metadatacard description");

			Log.message("3. Checked the invalid URL link text("+dataPool.get("LinkText")+") is displayed in the metadatacard description");

			//Step-4:Clicks the invalid link displayed in the metadatacard descriptions
			//----------------------------------------------------------------------------------------
			metadataCard.clickMetadataDescriptionLink(dataPool.get("LinkText"));//Clicks the metadatacard description link

			Log.message("4. Clicked the invalid URL link in the metadatacard description", driver);

			//Verifies if new tab is opened while click on the invalid link in the metadatcard description
			//------------------------------------------------------------------------------------------------------
			if (Utility.tabExists(driver, dataPool.get("InvalidLinkURL"), "URL"))
				Log.pass("Test Case Passed. Invalid webpage is opened while click on the invalid http link in MetadataCard description [Existing object metadatacard]", driver);
			else
				Log.fail("Test Case Failed. Invalid webpage is opened while click on the invalid http link in MetadataCard description [Existing object metadatacard].", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			driver = Utility.switchToTab(driver, "M-Files Web", "title");
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_12_2_1A

	/**
	 * TC.1.2.12.2.1B : Verify if invalid url webpage is launched without getting any error while click on the invalid http link in MetadataCard Description [Existing object popout metadata card].
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if invalid url webpage is launched without getting any error while click on the invalid http link in MetadataCard Description [Existing object popout metadata card].")
	public void TC_1_2_12_2_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to : " + viewToNavigate + " view.");

			//Step-2 : Select any existing object
			//------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			Log.message("2. Selected the specified object : " + dataPool.get("Object") + " in list view.");

			//Step-3: Open the popout metadatacard of the object
			//--------------------------------------------------
			homePage.taskPanel.clickItem("Properties");//Clicks the properties option from task pane

			Log.message("3. Existing object popped out metadatacard is opened via task pane", driver);

			//Step-4:Checks the invalid link is displayed as expected in the metadatacard descriptions
			//----------------------------------------------------------------------------------------
			int exist = 0 ;
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard
			ArrayList<String> actualLinks = metadataCard.getMetadataDescriptionLinks();//Gets the available links displayed in the metadatacard description

			for (int i=0;i< actualLinks.size();i++)
				if(actualLinks.get(i).trim().equalsIgnoreCase(dataPool.get("LinkText"))){ //Checks if expected link text is same as displayed in the metadatacard description
					exist = 1;//exist is set to 1 if expected link text is displayed in the metadatcard description
					break;
				}

			if(exist != 1)//Checks expected invalid link text is displayed in the metadatacard description 
				throw new Exception(" Expected Invalid URL link text("+dataPool.get("LinkText")+") is not displayed in the metadatacard description");

			Log.message("4. Checked the invalid URL link text("+dataPool.get("LinkText")+") is displayed in the metadatacard description");

			//Step-5:Clicks the invalid link displayed in the metadatacard descriptions
			//----------------------------------------------------------------------------------------
			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.clickMetadataDescriptionLink(dataPool.get("LinkText"));//Clicks the metadatacard description link

			Log.message("5. Clicked the invalid URL link in the metadatacard description", driver);

			//Verifies if new tab is opened while click on the invalid link in the metadatcard description
			//------------------------------------------------------------------------------------------------------
			if (Utility.tabExists(driver, dataPool.get("InvalidLinkURL"), "URL"))
				Log.pass("Test Case Passed. Invalid webpage is opened while click on the invalid http link in MetadataCard description [Existing object popout metadata card]", driver);
			else
				Log.fail("Test Case Failed. Invalid webpage is opened while click on the invalid http link in MetadataCard description [Existing object popout metadata card].", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			driver = Utility.switchToTab(driver, "M-Files Web", "title");
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_12_2_1B

	/**
	 * TC_1.2.12.2.1C : Verify if invalid url webpage is launched without getting any error while click on the invalid http link in MetadataCard Description [Multiple Existing objects metadata card].
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if invalid url webpage is launched without getting any error while click on the invalid http link in MetadataCard Description [Multiple Existing objects metadata card].")
	public void TC_1_2_12_2_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to : " + viewToNavigate + " view.");

			//Step-2 : Select any existing object
			//------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));//Selects the multiple objects in the view

			Log.message("2. Multiple objects selected in the list view.", driver);

			//Step-3:Checks the invalid link is displayed as expected in the metadatacard descriptions
			//----------------------------------------------------------------------------------------
			int exist = 0 ;
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard
			ArrayList<String> actualLinks = metadataCard.getMetadataDescriptionLinks();//Gets the available links displayed in the metadatacard description

			for (int i=0;i< actualLinks.size();i++)
				if(actualLinks.get(i).trim().equalsIgnoreCase(dataPool.get("LinkText"))){ //Checks if expected link text is same as displayed in the metadatacard description
					exist = 1;//exist is set to 1 if expected link text is displayed in the metadatcard description
					break;
				}

			if(exist != 1)//Checks expected invalid link text is displayed in the metadatacard description 
				throw new Exception(" Expected Invalid URL link text("+dataPool.get("LinkText")+") is not displayed in the metadatacard description");

			Log.message("3. Checked the invalid URL link text("+dataPool.get("LinkText")+") is displayed in the metadatacard description");

			//Step-4:Clicks the invalid link displayed in the metadatacard descriptions
			//----------------------------------------------------------------------------------------
			metadataCard.clickMetadataDescriptionLink(dataPool.get("LinkText"));//Clicks the metadatacard description link

			Log.message("4. Clicked the invalid URL link in the metadatacard description", driver);

			//Verifies if new tab is opened while click on the invalid link in the metadatcard description
			//------------------------------------------------------------------------------------------------------
			if (Utility.tabExists(driver, dataPool.get("InvalidLinkURL"), "URL"))
				Log.pass("Test Case Passed. Invalid webpage is opened while click on the invalid http link in MetadataCard description [Multiple Existing objects metadata card]", driver);
			else
				Log.fail("Test Case Failed. Invalid webpage is opened while click on the invalid http link in MetadataCard description [Multiple Existing objects metadata card].", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			driver = Utility.switchToTab(driver, "M-Files Web", "title");
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_12_2_1C

	/**
	 * TC.1.2.12.2.1D : Verify if invalid url webpage is launched without getting any error while click on the invalid http link in MetadataCard Description [Multiple Existing objects popout metadata card].
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if invalid url webpage is launched without getting any error while click on the invalid http link in MetadataCard Description [Multiple Existing objects popout metadata card].")
	public void TC_1_2_12_2_1D(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to : " + viewToNavigate + " view.");

			//Step-2 : Select multiple existing objects
			//------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));//Selects the multiple objects in the view

			Log.message("2. Multiple objects selected in the list view.");

			//Step-3: Open the popout metadatacard of the object
			//--------------------------------------------------
			homePage.taskPanel.clickItem("Properties");//Clicks the properties option from task pane

			Log.message("3. Existing object popped out metadatacard is opened via task pane", driver);

			//Step-4:Checks the invalid link is displayed as expected in the metadatacard descriptions
			//----------------------------------------------------------------------------------------
			int exist = 0 ;
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard
			ArrayList<String> actualLinks = metadataCard.getMetadataDescriptionLinks();//Gets the available links displayed in the metadatacard description

			for (int i=0;i< actualLinks.size();i++)
				if(actualLinks.get(i).trim().equalsIgnoreCase(dataPool.get("LinkText"))){ //Checks if expected link text is same as displayed in the metadatacard description
					exist = 1;//exist is set to 1 if expected link text is displayed in the metadatcard description
					break;
				}

			if(exist != 1)//Checks expected invalid link text is displayed in the metadatacard description 
				throw new Exception(" Expected Invalid URL link text("+dataPool.get("LinkText")+") is not displayed in the metadatacard description");

			Log.message("4. Checked the invalid URL link text("+dataPool.get("LinkText")+") is displayed in the metadatacard description");

			//Step-5:Clicks the invalid link displayed in the metadatacard descriptions
			//----------------------------------------------------------------------------------------
			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.clickMetadataDescriptionLink(dataPool.get("LinkText"));//Clicks the metadatacard description link

			Log.message("5. Clicked the invalid URL link in the metadatacard description", driver);

			//Verifies if new tab is opened while click on the invalid link in the metadatcard description
			//------------------------------------------------------------------------------------------------------
			if (Utility.tabExists(driver, dataPool.get("InvalidLinkURL"), "URL"))
				Log.pass("Test Case Passed. Invalid webpage is opened while click on the invalid http link in MetadataCard description [Object creation via Task pane]", driver);
			else
				Log.fail("Test Case Failed. Invalid webpage is not opened while click on the invalid http link in MetadataCard description [Object creation via Task pane].", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			driver = Utility.switchToTab(driver, "M-Files Web", "title");
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_12_2_1D

	/**
	 * TC.1.2.15.1.1A : Verify if valid url webpage is launched without getting any error while click on the valid http link in MetadataCard Description [Object creation via Task pane]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability", "MetadataConfigurability", "MetadataDescription", "PropertyDescription"}, 
			description = "Verify if valid url webpage is launched without getting any error while click on the valid http link in MetadataCard Description [Object creation via Task pane]")
	public void TC_1_2_15_1_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Click the new object link from task pane
			//------------------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));//Clicks the new object link from task pane

			Log.message("1. " + dataPool.get("ObjectType") + " is clicked from New item in task pane", driver);

			//Step-2:Checks the Valid link is displayed as expected in the metadatacard descriptions
			//----------------------------------------------------------------------------------------
			int exist = 0 ;
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard
			ArrayList<String> actualLinks = metadataCard.getMetadataDescriptionLinks();//Gets the available links displayed in the metadatacard description

			for (int i=0;i< actualLinks.size();i++)
				if(actualLinks.get(i).trim().equalsIgnoreCase(dataPool.get("LinkText"))){ //Checks if expected link text is same as displayed in the metadatacard description
					exist = 1;//exist is set to 1 if expected link text is displayed in the metadatcard description
					break;
				}

			if(exist != 1)//Checks expected Valid link text is displayed in the metadatacard description 
				throw new Exception(" Expected Valid URL link text("+dataPool.get("LinkText")+") is not displayed in the metadatacard description");

			Log.message("2. Checked the Valid URL link text("+dataPool.get("LinkText")+") is displayed in the metadatacard description");

			//Step-3:Clicks the Valid link displayed in the metadatacard descriptions
			//----------------------------------------------------------------------------------------
			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.clickMetadataDescriptionLink(dataPool.get("LinkText"));//Clicks the metadatacard description link

			Log.message("3. Clicked the Valid URL link in the metadatacard description", driver);

			//Verifies if new tab is opened while click on the Valid link in the metadatcard description
			//------------------------------------------------------------------------------------------------------
			if (Utility.tabExists(driver, dataPool.get("ValidLinkURL"), "URL"))
				Log.pass("Test Case Passed. Valid webpage is opened while click on the Valid http link in MetadataCard description [Object creation via Task pane]", driver);
			else
				Log.fail("Test Case Failed. Valid webpage is not opened while click on the Valid http link in MetadataCard description [Object creation via Task pane].", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			driver = Utility.switchToTab(driver, "M-Files Web", "title");
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_15_1_1A

	/**
	 * TC.1.2.15.1.1B : Verify if Valid url webpage is launched without getting any error while click on the Valid http link in MetadataCard Description [Object creation via New item menu]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if Valid url webpage is launched without getting any error while click on the Valid http link in MetadataCard Description  [Object creation via New item menu]")
	public void TC_1_2_15_1_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Click the new object link from new item menu
			//---------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//Clicks the new menu item

			Log.message("1." + dataPool.get("ObjectType") + " is clicked from new item menu bar", driver);

			//Step-2:Checks the Valid link is displayed as expected in the metadatacard descriptions
			//----------------------------------------------------------------------------------------
			int exist = 0 ;
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard
			ArrayList<String> actualLinks = metadataCard.getMetadataDescriptionLinks();//Gets the available links displayed in the metadatacard description

			for (int i=0;i< actualLinks.size();i++)
				if(actualLinks.get(i).trim().equalsIgnoreCase(dataPool.get("LinkText"))){ //Checks if expected link text is same as displayed in the metadatacard description
					exist = 1;//exist is set to 1 if expected link text is displayed in the metadatcard description
					break;
				}

			if(exist != 1)//Checks expected Valid link text is displayed in the metadatacard description 
				throw new Exception(" Expected Valid URL link text("+dataPool.get("LinkText")+") is not displayed in the metadatacard description");

			Log.message("2. Checked the Valid URL link text("+dataPool.get("LinkText")+") is displayed in the metadatacard description");

			//Step-3:Clicks the Valid link displayed in the metadatacard descriptions
			//----------------------------------------------------------------------------------------
			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.clickMetadataDescriptionLink(dataPool.get("LinkText"));//Clicks the metadatacard description link

			Log.message("3. Clicked the Valid URL link in the metadatacard description", driver);

			//Verifies if new tab is opened while click on the Valid link in the metadatcard description
			//------------------------------------------------------------------------------------------------------
			if (Utility.tabExists(driver, dataPool.get("ValidLinkURL"), "URL"))
				Log.pass("Test Case Passed. Valid webpage is opened while click on the Valid http link in MetadataCard description [Object creation via New item menu]", driver);
			else
				Log.fail("Test Case Failed. Valid webpage is opened while click on the Valid http link in MetadataCard description [Object creation via New item menu].", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			driver = Utility.switchToTab(driver, "M-Files Web", "title");
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_15_1_1B

	/**
	 * TC.1.2.15.1.1C : Verify if Valid url webpage is launched without getting any error while click on the Valid http link in MetadataCard Description [Object creation via Object property]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if Valid url webpage is launched without getting any error while click on the Valid http link in MetadataCard Description [Object creation via Object property]")
	public void TC_1_2_15_1_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to : " + viewToNavigate + " view.");

			//Step-2 : Select any existing object
			//------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			Log.message("2. Selected the specified object : " + dataPool.get("Object") + " in list view.");

			//Step-3 : Clicks the new field link in the object lookup property in the metadatacard
			//------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard of existing object

			metadataCard.clickAddValueButton(dataPool.get("ObjectType"));//Clicks the Add value button to open the new customer object metadatacard

			Log.message("3. Opened the new object(" + dataPool.get("ObjectType") +  ") metadatacard via object type property", driver);

			//Step-4:Checks the Valid link is displayed as expected in the metadatacard descriptions
			//----------------------------------------------------------------------------------------
			int exist = 0 ;
			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard
			ArrayList<String> actualLinks = metadataCard.getMetadataDescriptionLinks();//Gets the available links displayed in the metadatacard description

			for (int i=0;i< actualLinks.size();i++)
				if(actualLinks.get(i).trim().equalsIgnoreCase(dataPool.get("LinkText"))){ //Checks if expected link text is same as displayed in the metadatacard description
					exist = 1;//exist is set to 1 if expected link text is displayed in the metadatcard description
					break;
				}

			if(exist != 1)//Checks expected Valid link text is displayed in the metadatacard description 
				throw new Exception(" Expected Valid URL link text("+dataPool.get("LinkText")+") is not displayed in the metadatacard description");

			Log.message("4. Checked the Valid URL link text("+dataPool.get("LinkText")+") is displayed in the metadatacard description");

			//Step-5:Clicks the Valid link displayed in the metadatacard descriptions
			//----------------------------------------------------------------------------------------
			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.clickMetadataDescriptionLink(dataPool.get("LinkText"));//Clicks the metadatacard description link

			Log.message("5. Clicked the Valid URL link in the metadatacard description", driver);

			//Verifies if new tab is opened while click on the Valid link in the metadatcard description
			//------------------------------------------------------------------------------------------------------
			if (Utility.tabExists(driver, dataPool.get("ValidLinkURL"), "URL"))
				Log.pass("Test Case Passed. Valid webpage is opened while click on the Valid http link in MetadataCard description [Object creation via Object property]", driver);
			else
				Log.fail("Test Case Failed. Valid webpage is opened while click on the Valid http link in MetadataCard description [Object creation via Object property].", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			driver = Utility.switchToTab(driver, "M-Files Web", "title");
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_15_1_1C

	/**
	 * TC.1.2.15.2.1A : Verify if Valid url webpage is launched without getting any error while click on the Valid http link in MetadataCard Description [Existing object metadatacard]
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if Valid url webpage is launched without getting any error while click on the Valid http link in MetadataCard Description [Existing object metadatacard].")
	public void TC_1_2_15_2_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to : " + viewToNavigate + " view.");

			//Step-2 : Select any existing object
			//------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			Log.message("2. Selected the specified object : " + dataPool.get("Object") + " in list view.", driver);

			//Step-3:Checks the Valid link is displayed as expected in the metadatacard descriptions
			//----------------------------------------------------------------------------------------
			int exist = 0 ;
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard
			ArrayList<String> actualLinks = metadataCard.getMetadataDescriptionLinks();//Gets the available links displayed in the metadatacard description

			for (int i=0;i< actualLinks.size();i++)
				if(actualLinks.get(i).trim().equalsIgnoreCase(dataPool.get("LinkText"))){ //Checks if expected link text is same as displayed in the metadatacard description
					exist = 1;//exist is set to 1 if expected link text is displayed in the metadatcard description
					break;
				}

			if(exist != 1)//Checks expected Valid link text is displayed in the metadatacard description 
				throw new Exception(" Expected Valid URL link text("+dataPool.get("LinkText")+") is not displayed in the metadatacard description");

			Log.message("3. Checked the Valid URL link text("+dataPool.get("LinkText")+") is displayed in the metadatacard description");

			//Step-4:Clicks the Valid link displayed in the metadatacard descriptions
			//----------------------------------------------------------------------------------------
			metadataCard.clickMetadataDescriptionLink(dataPool.get("LinkText"));//Clicks the metadatacard description link

			Log.message("4. Clicked the Valid URL link in the metadatacard description", driver);

			//Verifies if new tab is opened while click on the Valid link in the metadatcard description
			//------------------------------------------------------------------------------------------------------
			if (Utility.tabExists(driver, dataPool.get("ValidLinkURL"), "URL"))
				Log.pass("Test Case Passed. Valid webpage is opened while click on the Valid http link in MetadataCard description [Existing object metadatacard]", driver);
			else
				Log.fail("Test Case Failed. Valid webpage is opened while click on the Valid http link in MetadataCard description [Existing object metadatacard].", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			driver = Utility.switchToTab(driver, "M-Files Web", "title");
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_15_2_1A

	/**
	 * TC.1.2.15.2.1B : Verify if Valid url webpage is launched without getting any error while click on the Valid http link in MetadataCard Description [Existing object popout metadata card].
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if Valid url webpage is launched without getting any error while click on the Valid http link in MetadataCard Description [Existing object popout metadata card].")
	public void TC_1_2_15_2_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to : " + viewToNavigate + " view.");

			//Step-2 : Select any existing object
			//------------------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			Log.message("2. Selected the specified object : " + dataPool.get("Object") + " in list view.");

			//Step-3: Open the popout metadatacard of the object
			//--------------------------------------------------
			homePage.taskPanel.clickItem("Properties");//Clicks the properties option from task pane

			Log.message("3. Existing object popped out metadatacard is opened via task pane", driver);

			//Step-4:Checks the Valid link is displayed as expected in the metadatacard descriptions
			//----------------------------------------------------------------------------------------
			int exist = 0 ;
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard
			ArrayList<String> actualLinks = metadataCard.getMetadataDescriptionLinks();//Gets the available links displayed in the metadatacard description

			for (int i=0;i< actualLinks.size();i++)
				if(actualLinks.get(i).trim().equalsIgnoreCase(dataPool.get("LinkText"))){ //Checks if expected link text is same as displayed in the metadatacard description
					exist = 1;//exist is set to 1 if expected link text is displayed in the metadatcard description
					break;
				}

			if(exist != 1)//Checks expected Valid link text is displayed in the metadatacard description 
				throw new Exception(" Expected Valid URL link text("+dataPool.get("LinkText")+") is not displayed in the metadatacard description");

			Log.message("4. Checked the Valid URL link text("+dataPool.get("LinkText")+") is displayed in the metadatacard description");

			//Step-5:Clicks the Valid link displayed in the metadatacard descriptions
			//----------------------------------------------------------------------------------------
			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.clickMetadataDescriptionLink(dataPool.get("LinkText"));//Clicks the metadatacard description link

			Log.message("5. Clicked the Valid URL link in the metadatacard description", driver);

			//Verifies if new tab is opened while click on the Valid link in the metadatcard description
			//------------------------------------------------------------------------------------------------------
			if (Utility.tabExists(driver, dataPool.get("ValidLinkURL"), "URL"))
				Log.pass("Test Case Passed. Valid webpage is opened while click on the Valid http link in MetadataCard description [Existing object popout metadata card]", driver);
			else
				Log.fail("Test Case Failed. Valid webpage is opened while click on the Valid http link in MetadataCard description [Existing object popout metadata card].", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			driver = Utility.switchToTab(driver, "M-Files Web", "title");
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_15_2_1B

	/**
	 * TC_1.2.15.2.1C : Verify if Valid url webpage is launched without getting any error while click on the Valid http link in MetadataCard Description [Multiple Existing objects metadata card].
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "FN119MetadataConfigurability - isDefault"}, 
			description = "Verify if Valid url webpage is launched without getting any error while click on the Valid http link in MetadataCard Description [Multiple Existing objects metadata card].")
	public void TC_1_2_15_2_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to : " + viewToNavigate + " view.");

			//Step-2 : Select any existing object
			//------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));//Selects the multiple objects in the view

			Log.message("2. Multiple objects selected in the list view.", driver);

			//Step-3:Checks the Valid link is displayed as expected in the metadatacard descriptions
			//----------------------------------------------------------------------------------------
			int exist = 0 ;
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard
			ArrayList<String> actualLinks = metadataCard.getMetadataDescriptionLinks();//Gets the available links displayed in the metadatacard description

			for (int i=0;i< actualLinks.size();i++)
				if(actualLinks.get(i).trim().equalsIgnoreCase(dataPool.get("LinkText"))){ //Checks if expected link text is same as displayed in the metadatacard description
					exist = 1;//exist is set to 1 if expected link text is displayed in the metadatcard description
					break;
				}

			if(exist != 1)//Checks expected Valid link text is displayed in the metadatacard description 
				throw new Exception(" Expected Valid URL link text("+dataPool.get("LinkText")+") is not displayed in the metadatacard description");

			Log.message("3. Checked the Valid URL link text("+dataPool.get("LinkText")+") is displayed in the metadatacard description");

			//Step-4:Clicks the Valid link displayed in the metadatacard descriptions
			//----------------------------------------------------------------------------------------
			metadataCard.clickMetadataDescriptionLink(dataPool.get("LinkText"));//Clicks the metadatacard description link

			Log.message("4. Clicked the Valid URL link in the metadatacard description", driver);

			//Verifies if new tab is opened while click on the Valid link in the metadatcard description
			//------------------------------------------------------------------------------------------------------
			if (Utility.tabExists(driver, dataPool.get("ValidLinkURL"), "URL"))
				Log.pass("Test Case Passed. Valid webpage is opened while click on the Valid http link in MetadataCard description [Multiple Existing objects metadata card]", driver);
			else
				Log.fail("Test Case Failed. Valid webpage is opened while click on the Valid http link in MetadataCard description [Multiple Existing objects metadata card].", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			driver = Utility.switchToTab(driver, "M-Files Web", "title");
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_15_2_1C

	/**
	 * TC.1.2.15.2.1D : Verify if Valid url webpage is launched without getting any error while click on the Valid http link in MetadataCard Description [Multiple Existing objects popout metadata card].
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_MultiSelect", "FN119MetadataConfigurability"}, 
			description = "Verify if Valid url webpage is launched without getting any error while click on the Valid http link in MetadataCard Description [Multiple Existing objects popout metadata card].")
	public void TC_1_2_15_2_1D(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to : " + viewToNavigate + " view.");

			//Step-2 : Select multiple existing objects
			//------------------------------------------------
			homePage.listView.clickMultipleItems(dataPool.get("Objects"));//Selects the multiple objects in the view

			Log.message("2. Multiple objects selected in the list view.");

			//Step-3: Open the popout metadatacard of the object
			//--------------------------------------------------
			homePage.taskPanel.clickItem("Properties");//Clicks the properties option from task pane

			Log.message("3. Existing object popped out metadatacard is opened via task pane", driver);

			//Step-4:Checks the Valid link is displayed as expected in the metadatacard descriptions
			//----------------------------------------------------------------------------------------
			int exist = 0 ;
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard
			ArrayList<String> actualLinks = metadataCard.getMetadataDescriptionLinks();//Gets the available links displayed in the metadatacard description

			for (int i=0;i< actualLinks.size();i++)
				if(actualLinks.get(i).trim().equalsIgnoreCase(dataPool.get("LinkText"))){ //Checks if expected link text is same as displayed in the metadatacard description
					exist = 1;//exist is set to 1 if expected link text is displayed in the metadatcard description
					break;
				}

			if(exist != 1)//Checks expected Valid link text is displayed in the metadatacard description 
				throw new Exception(" Expected Valid URL link text("+dataPool.get("LinkText")+") is not displayed in the metadatacard description");

			Log.message("4. Checked the Valid URL link text("+dataPool.get("LinkText")+") is displayed in the metadatacard description");

			//Step-5:Clicks the Valid link displayed in the metadatacard descriptions
			//----------------------------------------------------------------------------------------
			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.clickMetadataDescriptionLink(dataPool.get("LinkText"));//Clicks the metadatacard description link

			Log.message("5. Clicked the Valid URL link in the metadatacard description", driver);

			//Verifies if new tab is opened while click on the Valid link in the metadatcard description
			//------------------------------------------------------------------------------------------------------
			if (Utility.tabExists(driver, dataPool.get("ValidLinkURL"), "URL"))
				Log.pass("Test Case Passed. Valid webpage is opened while click on the Valid http link in MetadataCard description [Object creation via Task pane]", driver);
			else
				Log.fail("Test Case Failed. Valid webpage is opened while click on the Valid http link in MetadataCard description [Object creation via Task pane].", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			driver = Utility.switchToTab(driver, "M-Files Web", "title");
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_15_2_1D

}//End MeatdataConfigDescription

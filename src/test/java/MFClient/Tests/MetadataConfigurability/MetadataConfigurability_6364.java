package MFClient.Tests.MetadataConfigurability;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.KeyEventUtils;
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
import MFClient.Wrappers.ListView;
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)

public class MetadataConfigurability_6364 {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String userName = null;
	public static String userFullName = null;
	public static String password = null;
	public static String testVault = null;
	public static String configURL = null;
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
	 * 1.1.1A.1A : Verify the metadatacard themes should be set as expected when selecting  existing object-in task pane 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"M6364_MetadataConfigurability"},description = "Verify the metadatacard themes should be set as expected when selecting  existing object-in task pane." )
	public void SprintTest1_1_1A_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			homePage.searchPanel.search("", dataPool.get("SearchType"));//Navigate to 'Search only:Documents' view

			Log.message("1. Navigate to specified : " + dataPool.get("SearchType") + " search view.");

			//Step-2 : Select the any existing document object
			//------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectType"));//Selected the specified object type

			Log.message("2. Selected the specified object : " + dataPool.get("ObjectType") + " in list view.");

			//Step-3 : Select the 'Properties' option in task pane
			//----------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);//Select the 'Properties' option from the task pane

			Log.message("3. Selected the 'Properties' option from the task pane.",driver);

			//Step-4 : Instantiate the metadata card 
			//---------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the metadatacard

			String ExpectedMetadata = "";

			//Verify the Metadatacard header colour
			//-------------------------------------
			if(!dataPool.get("ExpectedColour").contains(metadataCard.getHeaderColor()))//Verify the header colour is set as expected
				ExpectedMetadata = "Header colour("+metadataCard.getHeaderColor()+") is not set as expected("+dataPool.get("ExpectedColour")+").";

			//Verify the Property label is displayed or not
			//---------------------------------------------
			if(!metadataCard.propertyExists(dataPool.get("PropertyLabel")))
				ExpectedMetadata += "Property label :  " + dataPool.get("PropertyLabel") +" is not set as expeced.";

			//Verify if Object version & Object ID is not displayed
			//-----------------------------------------------------
			if(metadataCard.isObjectVersionDisplayed() && metadataCard.isObjectIDDisplayed())
				ExpectedMetadata = "Object version and Object ID is displayed. ";

			//Verify if Footer & Add property link is displayed or not
			//--------------------------------------------------------
			if(metadataCard.isfooterDisplayed() && metadataCard.isAddPropertyLinkDisplayed())
				ExpectedMetadata += "Metadatacard footer is displayed.";

			//Verify if Description back ground colour is displayed or not
			//------------------------------------------------------------
			if(!dataPool.get("DescriptionColour").toUpperCase().contains(metadataCard.getMetadataDescriptionColour().toUpperCase()))
				ExpectedMetadata += "Metadatacard description colour("+metadataCard.getMetadataDescriptionColour()+") is not set as expected("+dataPool.get("DescriptionColour")+").";

			//Verify the group header colour
			//------------------------------
			if(!dataPool.get("GroupColour").toUpperCase().contains(metadataCard.getGroupHeaderColour().toUpperCase()))
				ExpectedMetadata += "Group header colour("+metadataCard.getGroupHeaderColour()+") is not set as expected("+dataPool.get("GroupColour")+"). ";

			//Verify if group text colour and group text is displayed correctly
			//-----------------------------------------------------------------
			if(!dataPool.get("GroupTextColour").toUpperCase().contains(metadataCard.getGroupTextColour().toUpperCase()) && !metadataCard.getPropertyGroupText(1).equals("Test"))
				ExpectedMetadata += "Group text colour("+metadataCard.getGroupTextColour()+") & Group text("+metadataCard.getPropertyGroupText(1)+") is not set as expected(Expected group text color: "+dataPool.get("GroupTextColour")+" :: Expected group text: 'Test').";

			//Verification : Verify if the Metadata theme is set as expected
			//--------------------------------------------------------------
			if(ExpectedMetadata.equals(""))//Verify all values are set as expected
				Log.pass("Test Case Passed.Metadatacard theme Configuration is set as expected in metadatacard.", driver);
			else
				Log.fail("Test Case Failed.Metadatacard theme Configuration is not set as expected in metadatacard." + ExpectedMetadata, driver);


		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End SprintTest1_1_1A_1A


	/**
	 * 1.1.1A.1B : Verify the metadatacard themes should be set as expected when selecting  existing object-in Operations  menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"M6364_MetadataConfigurability"},description = "Verify the metadatacard themes should be set as expected when selecting  existing object-in Operations  menu." )
	public void SprintTest1_1_1A_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			homePage.searchPanel.search("", dataPool.get("SearchType"));//Navigate to 'Search only:Documents' view

			Log.message("1. Navigate to specified : " + dataPool.get("SearchType") + " search view.");

			//Step-2 : Select the any existing document object
			//------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectType"));//Selected the specified object type

			Log.message("2. Selected the specified object : " + dataPool.get("ObjectType") + " in list view.");

			//Step-3 : Select the 'Properties' option in task pane
			//----------------------------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.Properties.Value);//Select the 'Properties' option from the task pane

			Log.message("3. Selected the 'Properties' option from the Operations menu.",driver);

			//Step-4 : Resize the metadatacard for the selected item
			//------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the metadatacard

			String ExpectedMetadata = "";

			//Verify the Metadatacard header colour
			//-------------------------------------
			if(!dataPool.get("ExpectedColour").toUpperCase().contains(metadataCard.getHeaderColor().toUpperCase()))//Verify the header colour is set as expected
				ExpectedMetadata = "Header colour("+metadataCard.getHeaderColor()+") is not set as expected("+dataPool.get("ExpectedColour")+").";

			//Verify if Object version & Object ID is not displayed
			//-----------------------------------------------------
			if(metadataCard.isObjectVersionDisplayed() && metadataCard.isObjectIDDisplayed())
				ExpectedMetadata = "Object version and Object ID is displayed. ";

			//Verify if Footer & Add property link is displayed or not
			//--------------------------------------------------------
			if(metadataCard.isfooterDisplayed() && metadataCard.isAddPropertyLinkDisplayed())
				ExpectedMetadata += "Metadatacard footer is displayed.";

			//Verify if Description back ground colour is displayed or not
			//------------------------------------------------------------
			if(!dataPool.get("DescriptionColour").toUpperCase().contains(metadataCard.getMetadataDescriptionColour().toUpperCase()))
				ExpectedMetadata += "Metadatacard description colour("+metadataCard.getMetadataDescriptionColour()+") is not set as expected("+dataPool.get("DescriptionColour")+").";

			//Verify if Description text colour is displayed or not
			//-----------------------------------------------------
			if(!dataPool.get("DescriptionTextColour").toUpperCase().contains(metadataCard.getMetadataDescriptionTextColour().toUpperCase()))
				ExpectedMetadata += "Metadatacard description text colour("+metadataCard.getMetadataDescriptionTextColour()+") is not set as expected("+dataPool.get("DescriptionTextColour")+").";


			//Verify if Description text is displayed correctly
			//-------------------------------------------------
			if(!metadataCard.getMetadataDescriptionText().equals(dataPool.get("DescriptionText").replaceAll(" ", "").trim()))
				ExpectedMetadata += "Metadatacard description text is not set as expected.";

			//Verification : Verify if the Metadata theme is set as expected
			//--------------------------------------------------------------
			if(ExpectedMetadata.equals(""))//Verify all values are set as expected
				Log.pass("Test Case Passed.Metadatacard theme Configuration is set as expected in metadatacard.", driver);
			else
				Log.fail("Test Case Failed.Metadatacard theme Configuration is not set as expected in metadatacard." + ExpectedMetadata, driver);


		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End SprintTest1_1_1A_1B


	/**
	 * 1.1.1A.1C : Verify the metadatacard themes should be set as expected when selecting  existing object-in Context menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"M6364_MetadataConfigurability"},description = "Verify the metadatacard themes should be set as expected when selecting  existing object-in Context menu." )
	public void SprintTest1_1_1A_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			homePage.searchPanel.search("", dataPool.get("SearchType"));//Navigate to 'Search only:Documents' view

			Log.message("1. Navigate to specified : " + dataPool.get("SearchType") + " search view.");

			//Step-2 : Select the any existing document object
			//------------------------------------------------
			homePage.listView.rightClickItem(dataPool.get("ObjectType"));//Selected the specified object type

			Log.message("2. Selected the specified object : " + dataPool.get("ObjectType") + " in list view.");

			//Step-3 : Select the Metadata properties option from the Context menu
			//--------------------------------------------------------------------
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value);

			Log.message("3. Opened the Metadatacard through the context menu 'Properties' option.");

			//Step-4 : Instantiate the Metadatacard
			//-------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the metadatacard

			String ExpectedMetadata = "";

			//Verify the Metadatacard header colour
			//-------------------------------------
			if(!dataPool.get("ExpectedColour").toUpperCase().contains(metadataCard.getHeaderColor().toUpperCase()))//Verify the header colour is set as expected
				ExpectedMetadata = "Header colour("+metadataCard.getHeaderColor()+") is not set as expected("+dataPool.get("ExpectedColour")+").";

			//Verify if Footer & Add property link is displayed or not
			//--------------------------------------------------------
			if(metadataCard.isfooterDisplayed() && metadataCard.isAddPropertyLinkDisplayed())
				ExpectedMetadata += "Metadatacard footer is displayed.";

			//Verify if Description back ground colour is displayed or not
			//------------------------------------------------------------
			if(!dataPool.get("DescriptionColour").toUpperCase().contains(metadataCard.getMetadataDescriptionColour().toUpperCase()))
				ExpectedMetadata += "Metadatacard description colour("+metadataCard.getMetadataDescriptionColour()+") is not set as expected("+dataPool.get("DescriptionColour")+").";

			//Verify if Description text colour is displayed or not
			//-----------------------------------------------------
			if(!dataPool.get("DescriptionTextColour").toUpperCase().contains(metadataCard.getMetadataDescriptionTextColour().toUpperCase()))
				ExpectedMetadata += "Metadatacard description text colour("+metadataCard.getMetadataDescriptionTextColour()+") is not set as expected("+dataPool.get("DescriptionTextColour")+").";

			//Verify if Description text is displayed correctly
			//-------------------------------------------------
			if(!metadataCard.getMetadataDescriptionText().equals(dataPool.get("DescriptionText").replaceAll(" ", "").trim()))
				ExpectedMetadata += "Metadatacard description text is not set as expected.";

			//Verification : Verify if the Metadata theme is set as expected
			//--------------------------------------------------------------
			if(ExpectedMetadata.equals(""))//Verify all values are set as expected
				Log.pass("Test Case Passed.Metadatacard theme Configuration is set as expected in metadatacard.", driver);
			else
				Log.fail("Test Case Failed.Metadatacard theme Configuration is not set as expected in metadatacard." + ExpectedMetadata, driver);


		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally{
			Utility.quitDriver(driver);
		}//End finally
	}//End SprintTest1_1_1A_1C


	/**
	 * 1.1.1A.1D : VVerify the metadatacard themes should be set as expected when selecting  existing object-in settings menu pop-out metadata
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"M6364_MetadataConfigurability"},description = "Verify the metadatacard themes should be set as expected when selecting  existing object-in settings menu pop-out metadata." )
	public void SprintTest1_1_1A_1D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			homePage.searchPanel.search("", dataPool.get("SearchType"));//Navigate to 'Search only:Documents' view

			Log.message("1. Navigate to specified : " + dataPool.get("SearchType") + " search view.");

			//Step-2 : Select the any existing document object
			//------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectType"));//Selected the specified object type

			Log.message("2. Selected the specified object : " + dataPool.get("ObjectType") + " in list view.");

			//Step-3 : Select the Metadata properties option from the Context menu
			//--------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver,true);//Instantiate the metadatacard
			metadataCard.popOutMetadatacard();

			Log.message("3. Opened the Metadatacard through the settings menu.");

			//Step-4 : Instantiate the Metadatacard
			//-------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the metadatacard

			String ExpectedMetadata = "";

			//Verify the Metadatacard header colour
			//-------------------------------------
			if(!dataPool.get("ExpectedColour").toUpperCase().contains(metadatacard.getHeaderColor().toUpperCase()))//Verify the header colour is set as expected
				ExpectedMetadata = "Header colour("+metadataCard.getHeaderColor()+") is not set as expected("+dataPool.get("ExpectedColour")+").";

			//Verify if Footer & Add property link is displayed or not
			//--------------------------------------------------------
			if(metadatacard.isfooterDisplayed() && metadatacard.isAddPropertyLinkDisplayed())
				ExpectedMetadata += "Metadatacard footer is displayed.";

			//Verify if Description back ground colour is displayed or not
			//------------------------------------------------------------
			if(!dataPool.get("DescriptionColour").toUpperCase().contains(metadatacard.getMetadataDescriptionColour().toUpperCase()))
				ExpectedMetadata += "Metadatacard description colour("+metadataCard.getMetadataDescriptionColour()+") is not set as expected("+dataPool.get("DescriptionColour")+").";

			//Verify if Description text colour is displayed or not
			//-----------------------------------------------------
			if(!dataPool.get("DescriptionTextColour").toUpperCase().contains(metadatacard.getMetadataDescriptionTextColour().toUpperCase()))
				ExpectedMetadata += "Metadatacard description text colour("+metadataCard.getMetadataDescriptionTextColour()+") is not set as expected("+dataPool.get("DescriptionTextColour")+").";

			//Verify if Description text is displayed correctly
			//-------------------------------------------------
			if(!metadatacard.getMetadataDescriptionText().equals(dataPool.get("DescriptionText").replaceAll(" ", "").trim()))
				ExpectedMetadata += "Metadatacard description text is not set as expected.";

			if(!metadatacard.getImageSize().equals(dataPool.get("ImageSize")))
				ExpectedMetadata += "Imagesize is not set as expected.";

			//Checks the property description value
			//--------------------------------------
			if (metadatacard.isPropertyDescriptionDisplayed(dataPool.get("PropertyName"))) {//Checks the property description is not displayed

				if (!metadatacard.getPropertyDescriptionValue(dataPool.get("PropertyName")).equals(dataPool.get("PropertyDescValue")))//Checks the property description value
					ExpectedMetadata += "Expected property description("+ dataPool.get("PropertyDescValue") +") value is not displayed for the property(" + dataPool.get("PropertyName") + ")[Displayed value:"+ metadatacard.getPropertyDescriptionValue(dataPool.get("PropertyName")) +"];"; 

				if (!(dataPool.get("PropertyDescriptionBGColor").toUpperCase().contains(metadatacard.getPropertyDescriptionBGColor(dataPool.get("PropertyName")).toUpperCase())))//Checks the property description background color value
					ExpectedMetadata += "Expected background color(" + dataPool.get("PropertyDescriptionBGColor") + ") is not displayed for the property (" + dataPool.get("PropertyName") + ") description.[Displayed color:"+ metadatacard.getPropertyDescriptionBGColor(dataPool.get("PropertyName")) +"];";

				if (!dataPool.get("PropertyDescColor").toUpperCase().contains(metadatacard.getPropertyDescriptionTextColour(dataPool.get("PropertyName")).toUpperCase()))//Checks the property description text color value
					ExpectedMetadata += "Expected property description text color(" + dataPool.get("PropertyDescColor") + ") is not displayed for the property (" + dataPool.get("PropertyName") + ") description.[Displayed color:"+ metadatacard.getPropertyDescriptionTextColour(dataPool.get("PropertyName")) +"];";
			}
			else
				ExpectedMetadata += "Property description is not displayed in the metadatacard for the property(" + dataPool.get("PropertyDescName") + ")";


			//Verification : Verify if the Metadata theme is set as expected
			//--------------------------------------------------------------
			if(ExpectedMetadata.equals(""))//Verify all values are set as expected
				Log.pass("Test Case Passed.Metadatacard theme Configuration is set as expected in metadatacard.", driver);
			else
				Log.fail("Test Case Failed.Metadatacard theme Configuration is not set as expected in metadatacard." + ExpectedMetadata, driver);


		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally{
			Utility.quitDriver(driver);
		}//End finally
	}//End SprintTest1_1_1A_1D

	/**
	 * 1.1.1A.1E : Verify the metadatacard themes should be set as expected when selecting  existing object-in rightpane pop-out metadatacard
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"M6364_MetadataConfigurability"},description = "Verify the metadatacard themes should be set as expected when selecting  existing object-in rightpane pop-out metadatacard." )
	public void SprintTest1_1_1A_1E(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			homePage.searchPanel.search("", dataPool.get("SearchType"));//Navigate to 'Search only:Documents' view

			Log.message("1. Navigate to specified : " + dataPool.get("SearchType") + " search view.");

			//Step-2 : Select the any existing document object
			//------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectType"));//Selected the specified object type

			Log.message("2. Selected the specified object : " + dataPool.get("ObjectType") + " in list view.");

			//Step-3 : Select the Metadata properties option from the Context menu
			//--------------------------------------------------------------------
			homePage.previewPane.popoutRightPaneMetadataTab();//Popout the right pane metadatacard

			Log.message("3. Opened the Metadatacard through the right pane metadata card.");

			//Step-4 : Instantiate the Metadatacard
			//-------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the metadatacard

			String ExpectedMetadata = "";

			//Verify the Metadatacard header colour
			//-------------------------------------
			if(!dataPool.get("ExpectedColour").toUpperCase().contains(metadatacard.getHeaderColor().toUpperCase()))//Verify the header colour is set as expected
				ExpectedMetadata = "Header colour("+metadatacard.getHeaderColor()+") is not set as expected("+dataPool.get("ExpectedColour")+").";

			//Verify if Footer & Add property link is displayed or not
			//--------------------------------------------------------
			if(metadatacard.isfooterDisplayed() && metadatacard.isAddPropertyLinkDisplayed())
				ExpectedMetadata += "Metadatacard footer is displayed.";

			//Verify if Description back ground colour is displayed or not
			//------------------------------------------------------------
			if(!dataPool.get("DescriptionColour").toUpperCase().contains(metadatacard.getMetadataDescriptionColour().toUpperCase()))
				ExpectedMetadata += "Metadatacard description colour("+metadatacard.getMetadataDescriptionColour()+") is not set as expected("+dataPool.get("DescriptionColour")+").";

			//Verify if Description text colour is displayed or not
			//-----------------------------------------------------
			if(!dataPool.get("DescriptionTextColour").toUpperCase().contains(metadatacard.getMetadataDescriptionTextColour().toUpperCase()))
				ExpectedMetadata += "Metadatacard description text colour("+metadatacard.getMetadataDescriptionTextColour()+") is not set as expected("+dataPool.get("DescriptionTextColour")+").";

			//Verify if Description text is displayed correctly
			//-------------------------------------------------
			if(!metadatacard.getMetadataDescriptionText().equals(dataPool.get("DescriptionText").replaceAll(" ", "").trim()))
				ExpectedMetadata += "Metadatacard description text is not set as expected.";

			//Verify if image size is dislayed as expected
			//---------------------------------------------
			if(!metadatacard.getImageSize().equals(dataPool.get("ImageSize")))
				ExpectedMetadata += "Imagesize is not set as expected.";

			//Checks the property description value
			//--------------------------------------
			if (metadatacard.isPropertyDescriptionDisplayed(dataPool.get("PropertyName"))) {//Checks the property description is not displayed

				if (!metadatacard.getPropertyDescriptionValue(dataPool.get("PropertyName")).equals(dataPool.get("PropertyDescValue")))//Checks the property description value
					ExpectedMetadata += "Expected property description("+ dataPool.get("PropertyDescValue") +") value is not displayed for the property(" + dataPool.get("PropertyName") + ")[Displayed value:"+ metadatacard.getPropertyDescriptionValue(dataPool.get("PropertyName")) +"];"; 

				if (!(dataPool.get("PropertyDescriptionBGColor").toUpperCase().contains(metadatacard.getPropertyDescriptionBGColor(dataPool.get("PropertyName")).toUpperCase())))//Checks the property description background color value
					ExpectedMetadata += "Expected background color(" + dataPool.get("PropertyDescriptionBGColor") + ") is not displayed for the property (" + dataPool.get("PropertyName") + ") description.[Displayed color:"+ metadatacard.getPropertyDescriptionBGColor(dataPool.get("PropertyName")) +"];";

				if (!dataPool.get("PropertyDescColor").toUpperCase().contains(metadatacard.getPropertyDescriptionTextColour(dataPool.get("PropertyName")).toUpperCase()))//Checks the property description text color value
					ExpectedMetadata += "Expected property description text color(" + dataPool.get("PropertyDescColor") + ") is not displayed for the property (" + dataPool.get("PropertyName") + ") description.[Displayed color:"+ metadatacard.getPropertyDescriptionTextColour(dataPool.get("PropertyName")) +"];";
			}
			else
				ExpectedMetadata += "Property description is not displayed in the metadatacard for the property(" + dataPool.get("PropertyDescName") + ")";


			//Verification : Verify if the Metadata theme is set as expected
			//--------------------------------------------------------------
			if(ExpectedMetadata.equals(""))//Verify all values are set as expected
				Log.pass("Test Case Passed.Metadatacard theme Configuration is set as expected in metadatacard.", driver);
			else
				Log.fail("Test Case Failed.Metadatacard theme Configuration is not set as expected in metadatacard." + ExpectedMetadata, driver);


		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally{
			Utility.quitDriver(driver);
		}//End finally
	}//End SprintTest1_1_1A_1E


	/**
	 * 1.1.2.1A : Check Metadata card UI with metadata card configurations [While setting conifgured value in the metadatacard] 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"M6364MetadataConfigurability"}, 
			description = "Check that the UI looks correct and feels usable when configurations are applied")
	public void SprintTest1_1_2_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged into the default webpage

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ")");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected in the view");

			Log.message("2. Object(" + dataPool.get("ObjectName") + ") is selected in the view ");

			//Step-3: Set the required configuration property with value in the metadatacard
			//------------------------------------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver, true);//Instantiates the metadatacard
			String ExpectedMetadata = "";
			String[] property = dataPool.get("Properties").split("::");

			if (!metadatacard.propertyExists(property[0]))
				if (!metadatacard.addNewProperty(property[0]))
					throw new Exception("Property ("+ property[0] +") is not added in the metadatacard");

			metadatacard.setPropertyValue(property[0], property[1]);


			metadatacard.clickProperty(property[0]);


			if (!metadatacard.getPropertyValue(property[0]).equalsIgnoreCase(property[1]))
				throw new Exception("Value("+ property[1] +") is not set for the property("+ property[0] +") in the metadatacard");

			Log.message("3. Configured Property (" + property[0] + ") with value (" + property[1] + ") is added in the metadatacard.");

			//Step-4 : Checks all the configurations are set in the metadatacard
			//------------------------------------------------------------------
			//Checks the Metadatacard button color
			//------------------------------------
			if (!dataPool.get("ButtonColor").toUpperCase().contains(metadatacard.getMetadataButtonColor().toUpperCase()))
				ExpectedMetadata += "Expected Button color(" + dataPool.get("ButtonColor") + ") is not set in metadatacard buttons.[Displayed color:"+ metadatacard.getMetadataButtonColor() +"];";

			//Checks the Metadatacard Ribbon color
			//-------------------------------------------------
			if (!dataPool.get("RibbonColor").toUpperCase().contains(metadatacard.getHeaderColor().toUpperCase()))//Verify the header colour is set as expected
				ExpectedMetadata = "Expected header color(" + dataPool.get("RibbonColor") + ") is not set in metadatacard buttons.[Displayed color:"+ metadatacard.getHeaderColor() +";";

			//Checks the metadatacard description value
			//-------------------------------------------------
			if (!metadatacard.getMetadataDescriptionText().equals(dataPool.get("MetadatacardDescValue").replaceAll(" ", "").trim()))
				ExpectedMetadata += "Expected metadatacard description("+ dataPool.get("MetadatacardDescValue") +") value is not displayed in the metadatacard.[Displayed text :"+ metadatacard.getMetadataDescriptionText() +"];"; 

			//Checks the metadatacard description background color value
			//------------------------------------------------------------------
			if (!dataPool.get("MetadatacardDescBGColor").toUpperCase().contains(metadatacard.getMetadataDescriptionColour().toUpperCase()))
				ExpectedMetadata += "Expected metadata description background color(" + dataPool.get("MetadatacardDescBGColor") + ") is not displayed in the metadatcard.[Displayed color:"+ metadatacard.getMetadataDescriptionColour() +"];"; 

			//Checks the metadatacard description text color value
			//------------------------------------------------------------------
			if (!dataPool.get("MetadatacardDescTextColor").toUpperCase().contains(metadatacard.getMetadataDescriptionTextColour().toUpperCase()))
				ExpectedMetadata += "Expected metadata description text color(" + dataPool.get("MetadatacardDescTextColor") + ") is not displayed in the metadatcard.[Displayed color:"+ metadatacard.getMetadataDescriptionTextColour() +"];"; 

			//Checks the property description value
			//--------------------------------------
			if (metadatacard.isPropertyDescriptionDisplayed(dataPool.get("PropertyDescName"))) {//Checks the property description is not displayed

				if (!metadatacard.getPropertyDescriptionValue(dataPool.get("PropertyDescName")).equals(dataPool.get("PropertyDescValue")))//Checks the property description value
					ExpectedMetadata += "Expected property description("+ dataPool.get("PropertyDescValue") +") value is not displayed for the property(" + dataPool.get("PropertyDescName") + ")[Displayed value:"+ metadatacard.getPropertyDescriptionValue(dataPool.get("PropertyDescName")) +"];"; 

				if (!(dataPool.get("PropertyDescriptionBGColor").toUpperCase().contains(metadatacard.getPropertyDescriptionBGColor(dataPool.get("PropertyDescName")).toUpperCase())))//Checks the property description background color value
					ExpectedMetadata += "Expected background color(" + dataPool.get("PropertyDescriptionBGColor") + ") is not displayed for the property (" + dataPool.get("PropertyDescName") + ") description.[Displayed color:"+ metadatacard.getPropertyDescriptionBGColor(dataPool.get("PropertyDescName")) +"];";

				if (!dataPool.get("PropertyDescColor").toUpperCase().contains(metadatacard.getPropertyDescriptionTextColour(dataPool.get("PropertyDescName")).toUpperCase()))//Checks the property description text color value
					ExpectedMetadata += "Expected property description text color(" + dataPool.get("PropertyDescColor") + ") is not displayed for the property (" + dataPool.get("PropertyDescName") + ") description.[Displayed color:"+ metadatacard.getPropertyDescriptionTextColour(dataPool.get("PropertyDescName")) +"];";
			}
			else
				ExpectedMetadata += "Property description is not displayed in the metadatacard for the property(" + dataPool.get("PropertyDescName") + ")";

			//Checks the property value is set as per the configuration
			//---------------------------------------------------------------------
			String results = "";
			int k;

			String[] expectedValues = dataPool.get("SetValueProperty1Values").split(",");//Gets the expected values from test data

			ArrayList<String> propValues = metadatacard.getPropertyValues(dataPool.get("SetValueProperty1"));//Gets the actual value from the metadatacard

			for (int i=0; i< propValues.size(); i++) {
				k=0;
				for (int j=0; j < expectedValues.length; j++) {
					if (propValues.get(i).toString().equalsIgnoreCase(expectedValues[j])) 
						k = 1;
				}
				if (k!=1)
					results += propValues.get(i).toString()+";";
			}

			if (!results.equalsIgnoreCase(""))//Checks the Property1 value is set in the metadatacard
				ExpectedMetadata += "Expected values(" + dataPool.get("SetValueProperty1Values") + ") is not set in the property(" + dataPool.get("SetValueProperty1") +  ")[Displayed Values:"+ metadatacard.getPropertyValues(dataPool.get("SetValueProperty1")) +"];";

			if (!metadatacard.getPropertyValue(dataPool.get("SetValueProperty2")).equalsIgnoreCase(dataPool.get("SetValueProperty2Value")))//Checks the Property2 value is set in the metadatacard
				ExpectedMetadata += "Expected value(" + dataPool.get("SetValueProperty2Value") + ") is not set in the property(" + dataPool.get("SetValueProperty2") +  ")[Displayed Value:"+ metadatacard.getPropertyValue(dataPool.get("SetValueProperty2")) +"];";

			//Checks the Property Group header color
			//--------------------------------------------------
			if (!dataPool.get("GroupBGColor").toUpperCase().contains(metadatacard.getGroupHeaderColour().toUpperCase()))
				ExpectedMetadata += "Expected group header color(" + dataPool.get("GroupBGColor") + ") is not set in the property group.[Displayed Group Header Color:"+ metadatacard.getGroupHeaderColour() +"];";

			//Checks the AddProperty link is not displayed in the metadatacard
			//----------------------------------------------------------------------------
			if (metadatacard.isAddPropertyLinkDisplayed())
				ExpectedMetadata += "Add Property link is displayed in the metadatacard";

			//Checks the Footer is not displayed in the metadatacard
			//------------------------------------------------------------------
			if (metadatacard.isfooterDisplayed())
				ExpectedMetadata += "Footer is displayed in the metadatacard;";

			//Checks the property label is changed in the metadatacard
			//--------------------------------------------------------------------
			if (!metadatacard.propertyExists(dataPool.get("SetValueProperty1")))
				ExpectedMetadata += "Expected Label(" + dataPool.get("SetValueProperty1") + ") is not displayed in the metadatacard;";

			//Verify if Configurations are set correctly
			//------------------------------------------
			if (ExpectedMetadata.equals(""))
				Log.pass("Test Case Passed. Configurations set as per defined rule in the metadatacard", driver);
			else
				Log.fail("Test Case Failed. Configurations is not set as per defined rule in the metadatacard. For more details:[" + ExpectedMetadata + "] ", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest1_1_2_1A

	/**
	 * 1.1.2.1B : Check Metadata card UI with metadata card configurations [While clearing configuration value in metadatacard] 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"M6364MetadataConfigurability"}, 
			description = "Check Metadata card UI with metadata card configurations [While clearing configuration value in metadatacard]")
	public void SprintTest1_1_2_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged into the default webpage

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ")");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected in the view");

			Log.message("2. Object(" + dataPool.get("ObjectName") + ") is selected in the view ");

			//Step-3: Open the popout metadatacard
			//------------------------------------
			if (!homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value))
				throw new Exception("Properties is not clicked from taskpane");

			MetadataCard metadatacard = new MetadataCard(driver);//Instantiates the metadatacard

			Log.message("3. Pop-Out metadatacard is opened via task pane");

			//Step-4: Set the required configuration property with value in the metadatacard
			//------------------------------------------------------------------------------
			String ExpectedMetadata = "";
			String[] property = dataPool.get("Properties").split("::");

			if (!metadatacard.propertyExists(property[0]))
				if (!metadatacard.addNewProperty(property[0]))
					throw new Exception("Property ("+ property[0] +") is not added in the metadatacard");

			metadatacard.setPropertyValue(property[0], property[1]);


			metadatacard.clickProperty(property[0]);


			if (!metadatacard.getPropertyValue(property[0]).equalsIgnoreCase(property[1]))
				throw new Exception("Value("+ property[1] +") is not set for the property("+ property[0] +") in the metadatacard");

			Log.message("4. Configured Property (" + property[0] + ") with value (" + property[1] + ") is added in the metadatacard.");

			//Step-4 : Clear the configured value in the metadatacard
			//-------------------------------------------------------
			metadatacard.setPropertyValue(property[0], "");//Clears the configured value in the metadatacard

			if (!metadatacard.getPropertyValue(property[0]).equalsIgnoreCase(""))
				throw new Exception("Value is not cleared for the property in the metadatacard");

			Log.message("5. Configured Property (" + property[0] + ") value (" + property[1] + ") is cleared in the metadatacard.");

			//Step-5 : Checks all the configurations are cleared in the metadatacard
			//------------------------------------------------------------------
			//Checks the Metadatacard button color
			//------------------------------------
			if (!dataPool.get("ButtonColor").toUpperCase().contains(metadatacard.getMetadataButtonColor().toUpperCase()))
				ExpectedMetadata += "Expected Button color(" + dataPool.get("ButtonColor") + ") is not set in metadatacard buttons.[Displayed color:"+ metadatacard.getMetadataButtonColor() +"];";

			//Checks the Metadatacard Ribbon color
			//-------------------------------------------------
			if (!dataPool.get("RibbonColor").toUpperCase().contains(metadatacard.getHeaderColor().toUpperCase()))//Verify the header colour is set as expected
				ExpectedMetadata = "Expected header color(" + dataPool.get("RibbonColor") + ") is not set in metadatacard buttons.[Displayed color:"+ metadatacard.getHeaderColor() +";";

			//Checks the metadatacard description is not displayed in the metadatacard
			//------------------------------------------------------------------------
			if (metadatacard.metadataDescriptionisDisplayed())
				ExpectedMetadata += "Metadatacard description is displayed in the metadatacard after clearing the configured value in the metadatacard.[Displayed text :"+ metadatacard.getMetadataDescriptionText() +"];";

			//Checks the property description is not displayed
			//-------------------------------------------------
			if (metadatacard.isPropertyDescriptionDisplayed(dataPool.get("PropertyDescName")))
				ExpectedMetadata += "Property description is displayed in the metadatacard after clearing the configured value in the metadatacard.[Displayed value:"+ metadatacard.getPropertyDescriptionValue(dataPool.get("PropertyDescName")) +"];";

			//Checks the property value is set as per the configuration
			//---------------------------------------------------------------------
			String results = "";
			int k;
			String[] expectedValues = dataPool.get("SetValueProperty1Values").split(",");//Gets the expected values from test data

			ArrayList<String> propValues = metadatacard.getPropertyValues(dataPool.get("SetValueProp1"));//Gets the actual value from the metadatacard

			for (int i=0; i< propValues.size(); i++) {
				k=0;
				for (int j=0; j < expectedValues.length; j++) {
					if (propValues.get(i).toString().equalsIgnoreCase(expectedValues[j])) 
						k = 1;
				}
				if (k!=1)
					results += propValues.get(i).toString()+";";
			}

			if (!results.equalsIgnoreCase(""))//Checks the Property1 value is set in the metadatacard
				ExpectedMetadata += "Expected values(" + dataPool.get("SetValueProperty1Values") + ") is not set in the property(" + dataPool.get("SetValueProperty1") +  ")[Displayed Values:"+ metadatacard.getPropertyValues(dataPool.get("SetValueProp1")) +"];";

			if (!metadatacard.getPropertyValue(dataPool.get("SetValueProperty2")).equalsIgnoreCase(dataPool.get("SetValueProperty2Value")))//Checks the Property2 value is set in the metadatacard
				ExpectedMetadata += "Expected value(" + dataPool.get("SetValueProperty2Value") + ") is not set in the property(" + dataPool.get("SetValueProperty2") +  ")[Displayed Value:"+ metadatacard.getPropertyValue(dataPool.get("SetValueProperty2")) +"];";

			//Checks the AddProperty link is displayed in the metadatacard
			//----------------------------------------------------------------------------
			if (!metadatacard.isAddPropertyLinkDisplayed())
				ExpectedMetadata += "Add Property link is not displayed in the metadatacard";

			//Checks the Footer is displayed in the metadatacard
			//------------------------------------------------------------------
			if (!metadatacard.isfooterDisplayed())
				ExpectedMetadata += "Footer is not displayed in the metadatacard;";

			//Checks the property label is not changed in the metadatacard
			//--------------------------------------------------------------------
			if (!metadatacard.propertyExists(dataPool.get("SetValueProp1")))
				ExpectedMetadata += "Property(" + dataPool.get("SetValueProp1") + ") is not displayed in the metadatacard;";

			//Verify if Configurations are set correctly
			//------------------------------------------
			if (ExpectedMetadata.equals(""))
				Log.pass("Test Case Passed. Configurations are not set while clearing the configured value in the metadatacard", driver);
			else
				Log.fail("Test Case Failed. Configurations are set in the metadatacard. For more details:[" + ExpectedMetadata + "] ", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest1_1_2_1B

	/**
	 * 1.1.2.1C : Checks Metadatacard configuration is not applied [While selecting non-configured value in the metadatacard] 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"M6364MetadataConfigurability"}, 
			description = "Checks Metadatacard configuration is not applied [While selecting non-configured value in the metadatacard]")
	public void SprintTest1_1_2_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged into the default webpage

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ")");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected in the view");

			Log.message("2. Object(" + dataPool.get("ObjectName") + ") is selected in the view ");

			//Step-3: Set the required configuration property with non configured value in the metadatacard
			//------------------------------------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver, true);//Instantiates the metadatacard
			String ExpectedMetadata = "";
			String[] property = dataPool.get("Properties").split("::");

			if (!metadatacard.propertyExists(property[0]))
				if (!metadatacard.addNewProperty(property[0]))
					throw new Exception("Property ("+ property[0] +") is not added in the metadatacard");

			metadatacard.setPropertyValue(property[0], property[1]);


			if (!metadatacard.getPropertyValue(property[0]).equalsIgnoreCase(property[1]))
				throw new Exception("Value("+ property[1] +") is not set for the property("+ property[0] +") in the metadatacard");

			Log.message("3. Configured Property (" + property[0] + ") with value (" + property[1] + ") is set in the metadatacard.");

			//Step-4 : Checks all the configurations are cleared in the metadatacard
			//------------------------------------------------------------------
			//Checks the Metadatacard button color
			//------------------------------------
			if (!dataPool.get("ButtonColor").toUpperCase().contains(metadatacard.getMetadataButtonColor().toUpperCase()))
				ExpectedMetadata += "Button color(" + dataPool.get("ButtonColor") + ") is set in metadatacard buttons for non-configured value.[Displayed color:"+ metadatacard.getMetadataButtonColor() +"];";

			//Checks the Metadatacard Ribbon color
			//-------------------------------------------------
			if (!dataPool.get("RibbonColor").toUpperCase().contains(metadatacard.getHeaderColor().toUpperCase()))//Verify the header colour is set as expected
				ExpectedMetadata = "Ribbon color(" + dataPool.get("RibbonColor") + ") is set in metadatacard for non-configured value.[Displayed color:"+ metadatacard.getHeaderColor() +";";

			//Checks the metadatacard description is not displayed in the metadatacard
			//------------------------------------------------------------------------
			if (metadatacard.metadataDescriptionisDisplayed())
				ExpectedMetadata += "Metadatacard description is displayed in the metadatacard for the non-configured value in the metadatacard.[Displayed text :"+ metadatacard.getMetadataDescriptionText() +"];";

			//Checks the property description is not displayed
			//-------------------------------------------------
			if (metadatacard.isPropertyDescriptionDisplayed(dataPool.get("PropertyDescName")))
				ExpectedMetadata += "Property description is displayed in the metadatacard while selecting the non-configured value in the metadatacard.[Displayed value:"+ metadatacard.getPropertyDescriptionValue(dataPool.get("PropertyDescName")) +"];";

			//Checks the additional properties is not displayed in the metadatacard
			//---------------------------------------------------------------------
			if (metadatacard.propertyExists(dataPool.get("SetValueProp1")))//Checks if additional property is not added in the metadatacard
				ExpectedMetadata += dataPool.get("SetValueProp1") + " is displayed in the metadatacard with values "+ metadatacard.getPropertyValues(dataPool.get("SetValueProp1"));

			if (metadatacard.propertyExists(dataPool.get("SetValueProperty2")))//Checks if additional property is not added in the metadatacard
				ExpectedMetadata += dataPool.get("SetValueProperty2") + " is displayed in the metadatacard with values "+ metadatacard.getPropertyValue(dataPool.get("SetValueProperty2"));

			//Checks the AddProperty link is displayed in the metadatacard
			//----------------------------------------------------------------------------
			if (!metadatacard.isAddPropertyLinkDisplayed())
				ExpectedMetadata += "Add Property link is not displayed in the metadatacard";

			//Checks the Footer is displayed in the metadatacard
			//------------------------------------------------------------------
			if (!metadatacard.isfooterDisplayed())
				ExpectedMetadata += "Footer is not displayed in the metadatacard;";

			//Verify if Configurations are set correctly
			//------------------------------------------
			if (ExpectedMetadata.equals(""))
				Log.pass("Test Case Passed. Configurations are not set while selecting the non-configured value in the metadatacard", driver);
			else
				Log.fail("Test Case Failed. Configurations are set in the metadatacard while selecting the non-configured value in the metadatcard. For more details:[" + ExpectedMetadata + "] ", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest1_1_2_1C

	/**
	 * 1.1.2B : Check Metadata card UI with metadata card configurations [Discard Changes] 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"M6364MetadataConfigurability"}, 
			description = "Check Metadata card UI with metadata card configurations [Discard Changes]")
	public void SprintTest1_1_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged into the default webpage

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ")");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected in the view");

			Log.message("2. Object(" + dataPool.get("ObjectName") + ") is selected in the view ");

			//Step-3: Open the popout metadatacard
			//------------------------------------
			if (!homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value))
				throw new Exception("Properties is not clicked from taskpane");

			MetadataCard metadatacard = new MetadataCard(driver);//Instantiates the metadatacard

			Log.message("3. Pop-Out metadatacard is opened via task pane");

			//Step-4: Set the required configuration property with non configured value in the metadatacard
			//------------------------------------------------------------------------------
			String ExpectedMetadata = "";
			String[] property = dataPool.get("Properties").split("::");

			if (!metadatacard.propertyExists(property[0]))
				if (!metadatacard.addNewProperty(property[0]))
					throw new Exception("Property ("+ property[0] +") is not added in the metadatacard");

			metadatacard.setPropertyValue(property[0], property[1]);


			metadatacard.clickProperty(property[0]);


			if (!metadatacard.getPropertyValue(property[0]).equalsIgnoreCase(property[1]))
				throw new Exception("Value("+ property[1] +") is not set for the property("+ property[0] +") in the metadatacard");

			Log.message("4. Configured Property (" + property[0] + ") with value (" + property[1] + ") is set in the metadatacard.");

			//step-4: Discard the changes in metadatacard
			//----------------------------------------------
			metadatacard.clickDiscardButton();


			Log.message("5. Changes are discarded in the metadatacard.");

			//Step-5 : Checks configurations are not set in the metadatacard for the non-configured value
			//--------------------------------------------------------------------------------------------
			//Checks the Metadatacard button color
			//------------------------------------
			metadatacard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (!dataPool.get("ButtonColor").toUpperCase().contains(metadatacard.getMetadataButtonColor().toUpperCase()))
				ExpectedMetadata += "Button color(" + dataPool.get("ButtonColor") + ") is set in metadatacard buttons after discard the changes.[Displayed color:"+ metadatacard.getMetadataButtonColor() +"];";

			//Checks the Metadatacard Ribbon color
			//-------------------------------------------------
			if (!dataPool.get("RibbonColor").toUpperCase().contains(metadatacard.getHeaderColor().toUpperCase()))//Verify the header colour is set as expected
				ExpectedMetadata = "Header color(" + dataPool.get("RibbonColor") + ") is set in metadatacard after discard the changes.[Displayed color:"+ metadatacard.getHeaderColor() +";";

			//Checks the metadatacard description is not displayed in the metadatacard
			//------------------------------------------------------------------------
			if (metadatacard.metadataDescriptionisDisplayed())
				ExpectedMetadata += "Metadatacard description is displayed in the metadatacard after discard the changes in the metadatacard.[Displayed text :"+ metadatacard.getMetadataDescriptionText() +"];";

			//Checks the property description is not displayed
			//-------------------------------------------------
			if (metadatacard.isPropertyDescriptionDisplayed(dataPool.get("PropertyDescName")))
				ExpectedMetadata += "Property description is displayed in the metadatacard after discard the changes in the metadatacard.[Displayed value:"+ metadatacard.getPropertyDescriptionValue(dataPool.get("PropertyDescName")) +"];";


			//Checks the additional properties is not displayed in the metadatacard
			//---------------------------------------------------------------------
			if (metadatacard.propertyExists(dataPool.get("SetValueProp1")))//Checks if additional property is not added in the metadatacard
				ExpectedMetadata += dataPool.get("SetValueProp1") + " is displayed in the metadatacard with values "+ metadatacard.getPropertyValues(dataPool.get("SetValueProp1"));

			if (metadatacard.propertyExists(dataPool.get("SetValueProperty2")))//Checks if additional property is not added in the metadatacard
				ExpectedMetadata += dataPool.get("SetValueProperty2") + " is displayed in the metadatacard with values "+ metadatacard.getPropertyValue(dataPool.get("SetValueProperty2"));

			//Checks the AddProperty link is displayed in the metadatacard
			//----------------------------------------------------------------------------
			if (!metadatacard.isAddPropertyLinkDisplayed())
				ExpectedMetadata += "Add Property link is not displayed in the metadatacard";

			//Checks the Footer is displayed in the metadatacard
			//------------------------------------------------------------------
			if (!metadatacard.isfooterDisplayed())
				ExpectedMetadata += "Footer is not displayed in the metadatacard;";

			//Verify if Configurations are set correctly
			//------------------------------------------
			if (ExpectedMetadata.equals(""))
				Log.pass("Test Case Passed. Configurations are not set while discard the changes in the metadatacard", driver);
			else
				Log.fail("Test Case Failed. Configurations are set in the metadatacard while discard the changes in the metadatcard. For more details:[" + ExpectedMetadata + "] ", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest1_1_2B

	/**
	 * 1.1.2C : Check Metadata card UI with metadata card configurations [Cursor position, moving with TAB Key] 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "M6364MetadataConfigurability"}, 
			description = "Check Metadata card UI with metadata card configurations [Cursor position, moving with TAB Key]")
	public void SprintTest1_1_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("Safari") || driverType.equalsIgnoreCase("IE"))
			throw new SkipException(driverType.toUpperCase() +" does not support key actions.");

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged into the default webpage

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("ObjectName")))
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ")");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not selected in the view");

			Log.message("2. Object(" + dataPool.get("ObjectName") + ") is selected in the view ");

			//Step-3: Open the popout metadatacard
			//------------------------------------
			if (!homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value))
				throw new Exception("Properties is not clicked from taskpane");

			MetadataCard metadatacard = new MetadataCard(driver);//Instantiates the metadatacard

			Log.message("3. Pop-Out metadatacard is opened via task pane");


			//Step-4: Set the required configuration property with value in the metadatacard
			//------------------------------------------------------------------------------
			String ExpectedMetadata = "";
			String[] property = dataPool.get("Properties").split("::");

			if (!metadatacard.propertyExists(property[0]))
				if (!metadatacard.addNewProperty(property[0]))
					throw new Exception("Property ("+ property[0] +") is not added in the metadatacard");

			metadatacard.setPropertyValue(property[0], property[1]);


			metadatacard.clickProperty(property[0]);


			if (!metadatacard.getPropertyValue(property[0]).equalsIgnoreCase(property[1]))
				throw new Exception("Value("+ property[1] +") is not set for the property("+ property[0] +") in the metadatacard");

			Log.message("4. Configured Property (" + property[0] + ") with value (" + property[1] + ") is added in the metadatacard.");

			//Step-5 : Checks all the configurations are set in the metadatacard
			//------------------------------------------------------------------
			//Checks the Metadatacard button color
			//------------------------------------
			if (!dataPool.get("ButtonColor").toUpperCase().contains(metadatacard.getMetadataButtonColor().toUpperCase()))
				ExpectedMetadata += "Expected Button color(" + dataPool.get("ButtonColor") + ") is not set in metadatacard buttons.[Displayed color:"+ metadatacard.getMetadataButtonColor() +"];";

			//Checks the Metadatacard Ribbon color
			//-------------------------------------------------
			if (!dataPool.get("RibbonColor").toUpperCase().contains(metadatacard.getHeaderColor().toUpperCase()))//Verify the header colour is set as expected
				ExpectedMetadata = "Expected header color(" + dataPool.get("RibbonColor") + ") is not set in metadatacard buttons.[Displayed color:"+ metadatacard.getHeaderColor() +";";

			//Checks the metadatacard description value
			//-------------------------------------------------
			if (!metadatacard.getMetadataDescriptionText().equals(dataPool.get("MetadatacardDescValue").replaceAll(" ", "").trim()))
				ExpectedMetadata += "Expected metadatacard description("+ dataPool.get("MetadatacardDescValue") +") value is not displayed in the metadatacard.[Displayed text :"+ metadatacard.getMetadataDescriptionText() +"];"; 

			//Checks the metadatacard description background color value
			//------------------------------------------------------------------
			if (!dataPool.get("MetadatacardDescBGColor").toUpperCase().contains(metadatacard.getMetadataDescriptionColour().toUpperCase()))
				ExpectedMetadata += "Expected metadata description background color(" + dataPool.get("MetadatacardDescBGColor") + ") is not displayed in the metadatcard.[Displayed color:"+ metadatacard.getMetadataDescriptionColour() +"];"; 

			//Checks the metadatacard description text color value
			//------------------------------------------------------------------
			if (!dataPool.get("MetadatacardDescTextColor").toUpperCase().contains(metadatacard.getMetadataDescriptionTextColour().toUpperCase()))
				ExpectedMetadata += "Expected metadata description text color(" + dataPool.get("MetadatacardDescTextColor") + ") is not displayed in the metadatcard.[Displayed color:"+ metadatacard.getMetadataDescriptionTextColour() +"];"; 

			//Checks the property description value
			//--------------------------------------
			if (metadatacard.isPropertyDescriptionDisplayed(dataPool.get("PropertyDescName"))) {//Checks the property description is not displayed

				if (!metadatacard.getPropertyDescriptionValue(dataPool.get("PropertyDescName")).equals(dataPool.get("PropertyDescValue")))//Checks the property description value
					ExpectedMetadata += "Expected property description("+ dataPool.get("PropertyDescValue") +") value is not displayed for the property(" + dataPool.get("PropertyDescName") + ")[Displayed value:"+ metadatacard.getPropertyDescriptionValue(dataPool.get("PropertyDescName")) +"];"; 

				if (!metadatacard.getPropertyDescriptionBGColor(dataPool.get("PropertyDescName")).equals(dataPool.get("PropertyDescriptionBGColor")))//Checks the property description background color value
					ExpectedMetadata += "Expected background color(" + dataPool.get("PropertyDescriptionBGColor") + ") is not displayed for the property (" + dataPool.get("PropertyDescName") + ") description.[Displayed color:"+ metadatacard.getPropertyDescriptionBGColor(dataPool.get("PropertyDescName")) +"];";

				if (!dataPool.get("PropertyDescColor").toUpperCase().contains(metadatacard.getPropertyDescriptionTextColour(dataPool.get("PropertyDescName")).toUpperCase()))//Checks the property description text color value
					ExpectedMetadata += "Expected property description text color(" + dataPool.get("PropertyDescColor") + ") is not displayed for the property (" + dataPool.get("PropertyDescName") + ") description.[Displayed color:"+ metadatacard.getPropertyDescriptionTextColour(dataPool.get("PropertyDescName")) +"];";
			}
			else
				ExpectedMetadata += "Property description is not displayed in the metadatacard for the property(" + dataPool.get("PropertyDescName") + ")";

			//Checks the property value is set as per the configuration
			//---------------------------------------------------------------------
			String results = "";
			int k;

			String[] expectedValues = dataPool.get("SetValueProperty1Values").split(",");//Gets the expected values from test data

			ArrayList<String> propValues = metadatacard.getPropertyValues(dataPool.get("SetValueProperty1"));//Gets the actual value from the metadatacard

			for (int i=0; i< propValues.size(); i++) {
				k=0;
				for (int j=0; j < expectedValues.length; j++) {
					if (propValues.get(i).toString().equalsIgnoreCase(expectedValues[j])) 
						k = 1;
				}
				if (k!=1)
					results += propValues.get(i).toString()+";";
			}

			if (!results.equalsIgnoreCase(""))//Checks the Property1 value is set in the metadatacard
				ExpectedMetadata += "Expected values(" + dataPool.get("SetValueProperty1Values") + ") is not set in the property(" + dataPool.get("SetValueProperty1") +  ")[Displayed Values:"+ metadatacard.getPropertyValues(dataPool.get("SetValueProperty1")) +"];";

			if (!metadatacard.getPropertyValue(dataPool.get("SetValueProperty2")).equalsIgnoreCase(dataPool.get("SetValueProperty2Value")))//Checks the Property2 value is set in the metadatacard
				ExpectedMetadata += "Expected value(" + dataPool.get("SetValueProperty2Value") + ") is not set in the property(" + dataPool.get("SetValueProperty2") +  ")[Displayed Value:"+ metadatacard.getPropertyValue(dataPool.get("SetValueProperty2")) +"];";

			//Checks the Property Group header color
			//--------------------------------------------------
			if (!dataPool.get("GroupBGColor").toUpperCase().contains(metadatacard.getGroupHeaderColour().toUpperCase()))
				ExpectedMetadata += "Expected group header color(" + dataPool.get("GroupBGColor") + ") is not set in the property group.[Displayed Group Header Color:"+ metadatacard.getGroupHeaderColour() +"];";

			//Checks the AddProperty link is not displayed in the metadatacard
			//----------------------------------------------------------------------------
			if (metadatacard.isAddPropertyLinkDisplayed())
				ExpectedMetadata += "Add Property link is displayed in the metadatacard";

			//Checks the Footer is not displayed in the metadatacard
			//------------------------------------------------------------------
			if (metadatacard.isfooterDisplayed())
				ExpectedMetadata += "Footer is displayed in the metadatacard;";

			//Checks the property label is changed in the metadatacard
			//--------------------------------------------------------------------
			if (!metadatacard.propertyExists(dataPool.get("SetValueProperty1")))
				ExpectedMetadata += "Expected Label(" + dataPool.get("SetValueProperty1") + ") is not displayed in the metadatacard;";

			//Checks the configuration
			//------------------------------------------
			if (ExpectedMetadata.equals(""))
				Log.message("5. Configurations set as per defined rule in the metadatacard");
			else
				Log.message("5. Configurations is not set as per defined rule in the metadatacard. For more details:[" + ExpectedMetadata + "] ");

			//Verify the tab key function in the metadatacard
			//-----------------------------------------------
			ArrayList<String> properties = new ArrayList<String>();
			properties = metadatacard.getMetadatacardProperties();//Gets the available properties in the metadatacard
			metadatacard.clickProperty(properties.get(0).toString());//Clicks the first property in the metadatacard
			int i=0,j=0;
			String focus="";
			for (i=1; i < properties.size(); i++) {
				if(!metadatacard.isMultiValueProperty(properties.get(i))) {
					KeyEventUtils.pressTAB(driver);//Presses tab key
					if(!metadatacard.isPropertyInFocus(properties.get(i)))
						focus += "Focus is not in" + properties.get(i);
				}
				else {
					for (j=0; j < metadatacard.getPropertyValues(properties.get(i)).size(); j++ ) {
						KeyEventUtils.pressTAB(driver);//Presses tab key
						if(!metadatacard.isPropertyInFocus(properties.get(i)))
							focus += "Focus is not in" + properties.get(i);
					}
				}
			}

			//Verify if tab key is working properly
			//------------------------------------------
			if (focus.equals(""))
				Log.pass("Test Case Passed. Cursor position, moving with TAB Key is working properly in metadatacard with metadata card configurations", driver);
			else
				Log.fail("Test Case Failed. Cursor position, moving with TAB Key is not working properly in metadatacard with metadata card configurations.", driver);

			//ui-state-focus

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest1_1_2C


	/**
	 * 1.1.3 : Converting SFDs to MFD with metadatacard configuration setvalue 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"M6364MetadataConfigurability"}, 
			description = "Convert Single document to Multi File Document(MFD) with metadatacard configuration setvalue")
	public void SprintTest1_1_3(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to View
			//-------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), dataPool.get("SearchWord"));

			Log.message("1. Navigated to '" + viewToNavigate + "' view.");

			//Step-2 : Select the object and perform check out operation
			//----------------------------------------------------------
			if (!homePage.listView.isSFDBasedOnObjectIcon(dataPool.get("ObjectName"))) //Checks if this is SFD
				throw new SkipException("Object (" + dataPool.get("ObjectName") + ") is not Single file document.");

			if (!homePage.listView.clickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.taskPanel.clickItem(Caption.MenuItems.CheckOut.Value); //Selects Show Members from task panel


			if (!ListView.isCheckedOutByItemName(driver, dataPool.get("ObjectName"))) //Checks if it is in checked in state
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not checked out.");

			Log.message("2. Object (" + dataPool.get("ObjectName") + ") is checked out.");

			//Step-3 : Add the configuration property in the metadata card
			//-------------------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver, true);//Instantiates the metadatacard
			metadatacard.addNewProperty(dataPool.get("Property"));


			if (!metadatacard.propertyExists(dataPool.get("Property")))//Checks property is added in the metadatacard
				throw new Exception("Property does not exist in the metadatacard");

			metadatacard.clickOKBtn(driver);


			Log.message("3. Configuration property (" + dataPool.get("ObjectName") + ") is added and saved in the metadatacard");

			//Step-4 : Select Convert to MFD from context menu
			//---------------------------------------------------
			if (!homePage.listView.rightClickItem(dataPool.get("ObjectName"))) //Selects the Object in the list
				throw new Exception("Object (" + dataPool.get("ObjectName") + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.ConvertToMFD_C.Value); //Clicks Convert SFD to MFD option from operations menu


			Log.message("4. Convert SFD to MFD option is selected from context menu for SFD (" + dataPool.get("ObjectName") + ").");

			//Step-5 : Check in the object
			//----------------------------
			String mfdName = dataPool.get("ObjectName").split("\\.")[0];

			if (!homePage.listView.rightClickItem(mfdName)) //Selects the Object in the list
				throw new Exception("Object (" + mfdName + ") is not got selected.");

			homePage.listView.clickContextMenuItem(Caption.MenuItems.CheckIn.Value); //Clicks Convert SFD to MFD option from operations menu


			Log.message("5. Object is checked in using context menu");

			ListView refreshedListView = homePage.listView.clickRefresh();//Refresh the listing view

			if (refreshedListView.isSFDBasedOnObjectIcon(mfdName))
				throw new Exception("Object(" + dataPool.get("ObjectName") + ") is not converted to MFD.");

			metadatacard = new MetadataCard(driver, true);//Instantiates the metadatacard


			//Verification : To Verify if SFD object is converted to MFD object and Configured value is saved in the metadatacard
			//--------------------------------------------------------------------------------------------------------------------			
			if (metadatacard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyValue")))
				Log.pass("Test case Passed. SFD is converted to MFD (" + mfdName + ") through context menu and configured value (" + dataPool.get("Property") + ") is set for the configured property (" + dataPool.get("PropertyValue") + ") in the metadatacard.",driver);
			else
				Log.fail("Test case failed. Configured value (" + dataPool.get("Property") + ") is not set for the configured property (" + dataPool.get("PropertyValue") + ") in the metadatacard.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest1_1_3A


	/**
	 * 1.1.4.1A : Pop Out Metadata Card from Right Pane.
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"M6364MetadataConfigurability"},description = "Pop Out Metadata Card from Right Pane" )
	public void SprintTest1_1_4_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			homePage.searchPanel.search("", dataPool.get("SearchType"));

			Log.message("1. Navigate to specified : " + dataPool.get("SearchType") + " search view.");

			//Step-2 : Select the any existing document object
			//------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectType"));

			Log.message("2. Selected the specified object : " + dataPool.get("ObjectType") + " in list view.");

			//Step-3 : Select the Right pane pop-out metadatacard
			//---------------------------------------------------
			homePage.previewPane.popoutRightPaneMetadataTab();//Popout the right pane metadatacard from the right pane

			Log.message("3. Selected the Pop-out metadatacard from right click the Right pane 'Metadatacard'.");

			//Step-4 : Add the Specified property value
			//-----------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the metadata card
			if(!metadataCard.propertyExists(dataPool.get("PropertyName"))){//Verify if property is exists in metadata or not 
				metadataCard.addNewProperty(dataPool.get("PropertyName"));//Add the new property value
				metadataCard.saveAndClose();//Save the metadata card
			}

			//Step-5 : Get the Property value from the selected property name
			//---------------------------------------------------------------
			String PropertyValue = metadataCard.getPropertyValue(dataPool.get("PropertyName"));//Get the specified property value 

			Log.message("4. Get the Property : " + dataPool.get("PropertyName") + " value.");

			//Verification : Verify the Property value is set as expected setvalue
			//--------------------------------------------------------------------
			if(PropertyValue.equals(""))
				Log.pass("Test Case Passed. Property : " + dataPool.get("PropertyName") + " is not set with value : Sales in popout metadatacard opened via right pane of existing object",driver);
			else
				Log.fail("Test Case Failed.Property : " + dataPool.get("PropertyName") + " is set with value as : Sales in popout metadatacard opened via right pane.", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//SprintTest1_1_4_1A

	/**
	 * 1.1.4.1B : Pop Out Metadata Card from settings menu
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"M6364MetadataConfigurability"},description = "Pop Out Metadata Card from settings menu" )
	public void SprintTest1_1_4_1B(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			homePage.searchPanel.search("", dataPool.get("SearchType"));

			Log.message("1. Navigate to specified : " + dataPool.get("SearchType") + " search view.");

			//Step-2 : Select the any existing document object
			//------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectType"));

			Log.message("2. Selected the specified object : " + dataPool.get("ObjectType") + " in list view.");

			//Step-3 : Add the department property value
			//-------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver,true);//Instantiate the right pane metadatacard
			metadataCard.popOutMetadatacard();//Pop-out the metadatacard from the settings menu

			Log.message("3. Selected the Pop-out metadatacard from the settings menu in rightpane metadatacard.", driver);


			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the Settings menu pop-out metadatacard
			if(!metadatacard.propertyExists(dataPool.get("PropertyName"))){//Verify if property is existing or not  
				metadatacard.addNewProperty(dataPool.get("PropertyName"));//Added the specified property in metadatacard
				metadatacard.clickCreateBtn();//Click the create button in metadatacard
			}

			//Step-4 : Get the Property value for the specified property
			//----------------------------------------------------------
			metadataCard = new MetadataCard(driver,true);//Instantiate the right pane metadatacard
			String PropertyValue = metadataCard.getPropertyValue(dataPool.get("PropertyName"));//Get the Property value in metadatacard

			Log.message("4. Property : " + dataPool.get("PropertyName") + " is set as value as 'Sales' expected.");

			//Verification : Verify the Specified department value is set as expected
			//-----------------------------------------------------------------------
			if(PropertyValue.equals(""))
				Log.pass("Test Case Passed. Property : " + dataPool.get("PropertyName") + " is not set with value : Sales in popout metadatacard opened via settings menu of existing object",driver);
			else
				Log.fail("Test Case Failed.Property : " + dataPool.get("PropertyName") + " is set with value as : Sales in popout metadatacard opened via settings menu of existing object", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//SprintTest1_1_4_1B

	/**
	 * 31704 : Verify if supported colour code works in metadatacard ribbon and button colours for existing objects
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"M6364MetadataConfigurability" , "Bug"},description = "Verify if supported colour code works in metadatacard ribbon and button colours for existing objects." )
	public void SprintTest_31704(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Navigate to specified view
			//-----------------------------------
			homePage.searchPanel.search("", dataPool.get("SearchType"));

			Log.message("1. Navigate to specified : " + dataPool.get("SearchType") + " search view.");

			//Step-2 : Select the any existing employee object
			//------------------------------------------------
			homePage.listView.clickItem(dataPool.get("ObjectType"));

			Log.message("2. Selected the specified object : " + dataPool.get("ObjectType") + " in list view.");

			//Step-3 : Instantiate the metadatacard and verify the theme colours
			//------------------------------------------------------------------
			if(!homePage.listView.isItemExists(dataPool.get("ObjectType")))
				throw new Exception("Specified object : "+ dataPool.get("ObjectType") + " does not exists in the list.");

			MetadataCard metadataCard = new MetadataCard(driver,true);//Instantiate the right pane metadatacard
			String ExpectedMetadata = " ";

			//Verify the Metadatacard header colour is set as expected
			//--------------------------------------------------------
			if(!metadataCard.getHeaderColor().equals(dataPool.get("ExpectedHeaderColour")))//Verify the header colour is set as expected
				ExpectedMetadata = "Header colour is not set as expected.Displayed colour is [" + metadataCard.getHeaderColor() + "]";

			//Verify the metadata header colour in the edit mode
			//--------------------------------------------------
			metadataCard.clickProperty("Class");
			String headerColour = metadataCard.getHeaderColor();
			System.out.println(headerColour);
			if(!metadataCard.getHeaderColor().equalsIgnoreCase(dataPool.get("ExpectedHeaderEditColour")))
				ExpectedMetadata += "Header colour is not set as expected in edit mode.Displayed colour is [" + metadataCard.getHeaderColor() + "]";

			//Verify if button colour is set as expected
			//------------------------------------------
			if (!metadataCard.getMetadataButtonColor().equalsIgnoreCase(dataPool.get("ButtonColor")))
				ExpectedMetadata += "Expected Button color(" + dataPool.get("ButtonColor") + ") is not set in metadatacard buttons.[Displayed color:"+ metadataCard.getMetadataButtonColor() +"];";

			//Verification : Verify if the supported colour code is set as expected
			//---------------------------------------------------------------------
			if(ExpectedMetadata.equals(""))//Verify all values are set as expected
				Log.pass("Test Case Passed.Supported colour codes are displayed correctly in metadata header and button.");
			else
				Log.fail("Test Case Failed.Supported colour codes are not displayed correctly in metadata header and button." + ExpectedMetadata, driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally { 
			Utility.quitDriver(driver);
		}//End finally
	}//SprintTest_31704_1A


}//End MetadataConfigurability_6364

package MFClient.Tests.MetadataConfigurability;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
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
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;
import MFClient.Wrappers.MFilesDialog; 

@Listeners(EmailReport.class)
public class MultipleMetadataConfigurations {


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
	 * 1.2.3.1A : Metadata card functionality with two layer configuration hierarchy while creating new object via task pane 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"FN119_MetadataConfigurability"},description = "Verify the Metadata card functionality with two layer configuration hierarchy rules while creating new object via task pane" )
	public void TC_1_2_3_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Click the new object link from task pane
			//---------------------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));//Clicks the new object link from task pane

			Log.message("1. " + dataPool.get("ObjectType") + " is clicked from New item in task pane");

			//Step-2: Verifies the First layer configurations in the metadatacard
			//--------------------------------------------------------------------
			String ExpectedMetadata = "";

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadata card

			if (!metadataCard.getImageSize().equalsIgnoreCase(dataPool.get("Layer1ImageSize")))//Checks the image size sets in the metadatacard description
				ExpectedMetadata += "Expected metadatacard description image size[" + dataPool.get("Layer1ImageSize") + "] is not set for Layer1 configuration in the metadatacard.[Actual image size:" + metadataCard.getImageSize() + "]";

			String ActualVaule = dataPool.get("MetadataDescLayer1").trim().replaceAll("\n", "").replaceAll("\r", "");
			ActualVaule = ActualVaule.replaceAll(" ", "");

			if (!metadataCard.getMetadataDescriptionText().trim().equalsIgnoreCase(ActualVaule.trim()))//Checks the metadatacard description for the layer1 configuration
				ExpectedMetadata += "Expected metadatacard description[" + dataPool.get("MetadataDescLayer1") + "] is not set for Layer1 configuration in the metadatacard.[Actual Metadatacard Description:" + metadataCard.getMetadataDescriptionText() + "]";

			if (!metadataCard.getPropertyDescriptionValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyDescLayer1")))//Checks the property description for layer1 configuration
				ExpectedMetadata += "Expected property description[" + dataPool.get("PropertyDescLayer1") + "] is not set for Layer1 configuration in the metadatacard.[Actual property Description:" + metadataCard.getPropertyDescriptionValue(dataPool.get("Property")) + "]";

			if (!metadataCard.getPropertyGroupText(1).equalsIgnoreCase(dataPool.get("Layer1GroupTitle")))//Checks the property group title for layer1 configuration
				ExpectedMetadata += "Expected group title[" + dataPool.get("Layer1GroupTitle") + "] is not set for Layer1 configuration in the metadatacard.[Actual Group title:" + metadataCard.getPropertyGroupText(1) + "]";

			if (!ExpectedMetadata.equals(""))//Checks the layer1 configuration is set properly
				throw new Exception("Layer1 configuration rule is not set as expected. For more details: "+ ExpectedMetadata);

			Log.message("2. Layer1 rule is set as expected in the new object metadatacard");

			//Step-3: Set the Layer2 rule configuration in the metadata card
			//--------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Layer2Prop"), dataPool.get("Layer2PropValue"));//Sets the required condition for Layer2 configuration

			if (!metadataCard.getPropertyValue(dataPool.get("Layer2Prop")).equalsIgnoreCase(dataPool.get("Layer2PropValue")))//Checks if the required value is set
				throw new Exception("Property( "+ dataPool.get("Layer2Prop") + " ) is not set with the value( "+ dataPool.get("Layer2PropValue") + " ) in the metadatacard");

			Log.message("3. Property( "+ dataPool.get("Layer2Prop") + " ) is set with the value( "+ dataPool.get("Layer2PropValue") + " ) to apply layer2 configuration in the metadatacard");

			//Step-4: Verifies the Second layer configurations in the metadatacard
			//--------------------------------------------------------------------
			ExpectedMetadata = "";

			if (!metadataCard.getImageSize().equalsIgnoreCase(dataPool.get("Layer2ImageSize")))//Checks the image size sets in the metadatacard description
				ExpectedMetadata += "Expected metadatacard description image size[" + dataPool.get("Layer2ImageSize") + "] is not set for Layer2 configuration in the metadatacard.[Actual image size:" + metadataCard.getImageSize() + "]";

			String ActualVaule1 = dataPool.get("MetadataDescLayer2").trim().replaceAll("\n", "").replaceAll("\r", "");
			ActualVaule1 = ActualVaule1.replaceAll(" ", "");

			if (!metadataCard.getMetadataDescriptionText().trim().equalsIgnoreCase(ActualVaule1.trim()))//Checks the metadatacard description for layer2 configuration
				ExpectedMetadata += "Expected metadatacard description[" + dataPool.get("MetadataDescLayer2") + "] is not set for Layer2 configuration in the metadatacard.[Actual Metadatacard Description:" + metadataCard.getMetadataDescriptionText() + "]";

			if (!metadataCard.getPropertyDescriptionValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyDescLayer2")))//Checks the property description for layer2 configuration
				ExpectedMetadata += "Expected property description[" + dataPool.get("PropertyDescLayer2") + "] is not set for Layer2 configuration in the metadatacard.[Actual property Description:" + metadataCard.getPropertyDescriptionValue(dataPool.get("Property")) + "]";

			if (!metadataCard.getPropertyGroupText(1).equalsIgnoreCase(dataPool.get("Layer2GroupTitle")))//Checks the property group title for layer2 configuration
				ExpectedMetadata += "Expected group title[" + dataPool.get("Layer2GroupTitle") + "] is not set for Layer2 configuration in the metadatacard.[Actual Group title:" + metadataCard.getPropertyGroupText(1) + "]";


			if (ExpectedMetadata.equals(""))//Checks the layer2 configuration is set correctly
				Log.pass("Test case Passed. Metadata card functionality with two layer configuration hierarchy is working as expected while creating new object via task pane", driver);
			else
				Log.fail("Test case failed. Layer2 rule is not set as expected in the new object metadatacard. [" + ExpectedMetadata + "]", driver);


		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_3_1A

	/**
	 * 1.2.3.1B : Metadata card functionality with two layer configuration hierarchy while creating new object via New item menu 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"FN119_MetadataConfigurability"},description = "Verify the Metadata card functionality with two layer configuration hierarchy rules while creating new object via new item menu" )
	public void TC_1_2_3_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Click the new object link from new item menu
			//---------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//Clicks the new menu item

			Log.message("1. " + dataPool.get("ObjectType") + " is clicked from new item menu bar");

			//Step-2: Verifies the First layer configurations in the metadatacard
			//--------------------------------------------------------------------
			String ExpectedMetadata = "";

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadata card

			if (!metadataCard.getImageSize().equalsIgnoreCase(dataPool.get("Layer1ImageSize")))//Checks the image size sets in the metadatacard description
				ExpectedMetadata += "Expected metadatacard description image size[" + dataPool.get("Layer1ImageSize") + "] is not set for Layer1 configuration in the metadatacard.[Actual image size:" + metadataCard.getImageSize() + "]";

			String ActualVaule = dataPool.get("MetadataDescLayer1").trim().replaceAll("\n", "").replaceAll("\r", "");
			ActualVaule = ActualVaule.replaceAll(" ", "");

			if (!metadataCard.getMetadataDescriptionText().trim().equalsIgnoreCase(ActualVaule.trim()))//Checks the metadatacard description for the layer1 configuration
				ExpectedMetadata += "Expected metadatacard description[" + dataPool.get("MetadataDescLayer1") + "] is not set for Layer1 configuration in the metadatacard.[Actual Metadatacard Description:" + metadataCard.getMetadataDescriptionText() + "]";

			if (!metadataCard.getPropertyDescriptionValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyDescLayer1")))//Checks the property description for layer1 configuration
				ExpectedMetadata += "Expected property description[" + dataPool.get("PropertyDescLayer1") + "] is not set for Layer1 configuration in the metadatacard.[Actual property Description:" + metadataCard.getPropertyDescriptionValue(dataPool.get("Property")) + "]";

			if (!metadataCard.getPropertyGroupText(1).equalsIgnoreCase(dataPool.get("Layer1GroupTitle")))//Checks the property group title for layer1 configuration
				ExpectedMetadata += "Expected group title[" + dataPool.get("Layer1GroupTitle") + "] is not set for Layer1 configuration in the metadatacard.[Actual Group title:" + metadataCard.getPropertyGroupText(1) + "]";

			if (!ExpectedMetadata.equals(""))//Checks the layer1 configuration is set properly
				throw new Exception("Layer1 configuration rule is not set as expected. For more details: "+ ExpectedMetadata);

			Log.message("2. Layer1 rule is set as expected in the new object metadatacard");

			//Step-3: Set the Layer2 rule configuration in the metadata card
			//--------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Layer2Prop"), dataPool.get("Layer2PropValue"));//Sets the required condition for Layer2 configuration

			if (!metadataCard.getPropertyValue(dataPool.get("Layer2Prop")).equalsIgnoreCase(dataPool.get("Layer2PropValue")))//Checks if the required value is set
				throw new Exception("Property( "+ dataPool.get("Layer2Prop") + " ) is not set with the value( "+ dataPool.get("Layer2PropValue") + " ) in the metadatacard");

			Log.message("3. Property( "+ dataPool.get("Layer2Prop") + " ) is set with the value( "+ dataPool.get("Layer2PropValue") + " ) to apply layer2 configuration in the metadatacard");

			//Step-4: Verifies the Second layer configurations in the metadatacard
			//--------------------------------------------------------------------
			ExpectedMetadata = "";

			if (!metadataCard.getImageSize().equalsIgnoreCase(dataPool.get("Layer2ImageSize")))//Checks the image size sets in the metadatacard description
				ExpectedMetadata += "Expected metadatacard description image size[" + dataPool.get("Layer2ImageSize") + "] is not set for Layer2 configuration in the metadatacard.[Actual image size:" + metadataCard.getImageSize() + "]";

			String ActualVaule1 = dataPool.get("MetadataDescLayer2").trim().replaceAll("\n", "").replaceAll("\r", "");
			ActualVaule1 = ActualVaule1.replaceAll(" ", "");

			if (!metadataCard.getMetadataDescriptionText().equalsIgnoreCase(ActualVaule1.trim()))//Checks the metadatacard description for layer2 configuration
				ExpectedMetadata += "Expected metadatacard description[" + dataPool.get("MetadataDescLayer2") + "] is not set for Layer2 configuration in the metadatacard.[Actual Metadatacard Description:" + metadataCard.getMetadataDescriptionText() + "]";

			if (!metadataCard.getPropertyDescriptionValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyDescLayer2")))//Checks the property description for layer2 configuration
				ExpectedMetadata += "Expected property description[" + dataPool.get("PropertyDescLayer2") + "] is not set for Layer2 configuration in the metadatacard.[Actual property Description:" + metadataCard.getPropertyDescriptionValue(dataPool.get("Property")) + "]";

			if (!metadataCard.getPropertyGroupText(1).equalsIgnoreCase(dataPool.get("Layer2GroupTitle")))//Checks the property group title for layer2 configuration
				ExpectedMetadata += "Expected group title[" + dataPool.get("Layer2GroupTitle") + "] is not set for Layer2 configuration in the metadatacard.[Actual Group title:" + metadataCard.getPropertyGroupText(1) + "]";


			if (ExpectedMetadata.equals(""))//Checks the layer2 configuration is set correctly
				Log.pass("Test case Passed. Metadata card functionality with two layer configuration hierarchy is working as expected while creating new object via new item menu bar", driver);
			else
				Log.fail("Test case failed. Layer2 rule is not set as expected in the new object metadatacard. [" + ExpectedMetadata + "]", driver);


		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_3_1B

	/**
	 * 1.2.3.1C : Metadata card functionality with two layer configuration hierarchy while creating new object via object property 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"FN119_MetadataConfigurability"},description = "Verify the Metadata card functionality with two layer configuration hierarchy rules while creating new object via object property" )
	public void TC_1_2_3_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

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

			Log.message("3. Opened the new object(" + dataPool.get("ObjectType") +  ") metadatacard via object type property");

			//Step-4: Verifies the First layer configurations in the metadatacard
			//--------------------------------------------------------------------
			String ExpectedMetadata = "";

			metadataCard = new MetadataCard(driver);//Instantiates the metadata card

			if (!metadataCard.getImageSize().equalsIgnoreCase(dataPool.get("Layer1ImageSize")))//Checks the image size sets in the metadatacard description
				ExpectedMetadata += "Expected metadatacard description image size[" + dataPool.get("Layer1ImageSize") + "] is not set for Layer1 configuration in the metadatacard.[Actual image size:" + metadataCard.getImageSize() + "]";

			String ActualVaule = dataPool.get("MetadataDescLayer1").trim().replaceAll("\n", "").replaceAll("\r", "");
			ActualVaule = ActualVaule.replaceAll(" ", "");

			if (!metadataCard.getMetadataDescriptionText().equalsIgnoreCase(ActualVaule.trim()))//Checks the metadatacard description for the layer1 configuration
				ExpectedMetadata += "Expected metadatacard description[" + dataPool.get("MetadataDescLayer1") + "] is not set for Layer1 configuration in the metadatacard.[Actual Metadatacard Description:" + metadataCard.getMetadataDescriptionText() + "]";

			if (!metadataCard.getPropertyDescriptionValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyDescLayer1")))//Checks the property description for layer1 configuration
				ExpectedMetadata += "Expected property description[" + dataPool.get("PropertyDescLayer1") + "] is not set for Layer1 configuration in the metadatacard.[Actual property Description:" + metadataCard.getPropertyDescriptionValue(dataPool.get("Property")) + "]";

			if (!metadataCard.getPropertyGroupText(1).equalsIgnoreCase(dataPool.get("Layer1GroupTitle")))//Checks the property group title for layer1 configuration
				ExpectedMetadata += "Expected group title[" + dataPool.get("Layer1GroupTitle") + "] is not set for Layer1 configuration in the metadatacard.[Actual Group title:" + metadataCard.getPropertyGroupText(1) + "]";

			if (!ExpectedMetadata.equals(""))//Checks the layer1 configuration is set properly
				throw new Exception("Layer1 configuration rule is not set as expected. For more details: "+ ExpectedMetadata);

			Log.message("4. Layer1 rule is set as expected in the new object metadatacard");

			//Step-5: Set the Layer2 rule configuration in the metadata card
			//--------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Layer2Prop"), dataPool.get("Layer2PropValue"));//Sets the required condition for Layer2 configuration

			if (!metadataCard.getPropertyValue(dataPool.get("Layer2Prop")).equalsIgnoreCase(dataPool.get("Layer2PropValue")))//Checks if the required value is set
				throw new Exception("Property( "+ dataPool.get("Layer2Prop") + " ) is not set with the value( "+ dataPool.get("Layer2PropValue") + " ) in the metadatacard");

			Log.message("5. Property( "+ dataPool.get("Layer2Prop") + " ) is set with the value( "+ dataPool.get("Layer2PropValue") + " ) to apply layer2 configuration in the metadatacard");

			//Step-4: Verifies the Second layer configurations in the metadata card
			//--------------------------------------------------------------------
			ExpectedMetadata = "";

			if (!metadataCard.getImageSize().equalsIgnoreCase(dataPool.get("Layer2ImageSize")))//Checks the image size sets in the metadatacard description
				ExpectedMetadata += "Expected metadatacard description image size[" + dataPool.get("Layer2ImageSize") + "] is not set for Layer2 configuration in the metadatacard.[Actual image size:" + metadataCard.getImageSize() + "]";

			String ActualVaule1 = dataPool.get("MetadataDescLayer2").trim().replaceAll("\n", "").replaceAll("\r", "");
			ActualVaule1 = ActualVaule1.replaceAll(" ", "");

			if (!metadataCard.getMetadataDescriptionText().equalsIgnoreCase(ActualVaule1.trim()))//Checks the metadatacard description for layer2 configuration
				ExpectedMetadata += "Expected metadatacard description[" + dataPool.get("MetadataDescLayer2") + "] is not set for Layer2 configuration in the metadatacard.[Actual Metadatacard Description:" + metadataCard.getMetadataDescriptionText() + "]";

			if (!metadataCard.getPropertyDescriptionValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyDescLayer2")))//Checks the property description for layer2 configuration
				ExpectedMetadata += "Expected property description[" + dataPool.get("PropertyDescLayer2") + "] is not set for Layer2 configuration in the metadatacard.[Actual property Description:" + metadataCard.getPropertyDescriptionValue(dataPool.get("Property")) + "]";

			if (!metadataCard.getPropertyGroupText(1).equalsIgnoreCase(dataPool.get("Layer2GroupTitle")))//Checks the property group title for layer2 configuration
				ExpectedMetadata += "Expected group title[" + dataPool.get("Layer2GroupTitle") + "] is not set for Layer2 configuration in the metadatacard.[Actual Group title:" + metadataCard.getPropertyGroupText(1) + "]";


			if (ExpectedMetadata.equals(""))//Checks the layer2 configuration is set correctly
				Log.pass("Test case Passed. Metadata card functionality with two layer configuration hierarchy is working as expected while creating new object via object type property", driver);
			else
				Log.fail("Test case failed. Layer2 rule is not set as expected in the new object metadatacard. [" + ExpectedMetadata + "]", driver);


		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_3_1C

	/**
	 * 1.2.3.2A : Metadata card functionality with two layer configuration hierarchy in existing object 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"FN119_MetadataConfigurability"},description = "Verify the Metadata card functionality with two layer configuration hierarchy in existing object" )
	public void TC_1_2_3_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

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

			//Step-3: Verifies the First layer configurations in the metadatacard
			//--------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard of existing object

			String ExpectedMetadata = "";

			if (!metadataCard.getImageSize().equalsIgnoreCase(dataPool.get("Layer1ImageSize")))//Checks the image size sets in the metadatacard description
				ExpectedMetadata += "Expected metadatacard description image size[" + dataPool.get("Layer1ImageSize") + "] is not set for Layer1 configuration in the metadatacard.[Actual image size:" + metadataCard.getImageSize() + "]";

			String ActualVaule = dataPool.get("MetadataDescLayer1").trim().replaceAll("\n", "").replaceAll("\r", "");
			ActualVaule = ActualVaule.replaceAll(" ", "");

			if (!metadataCard.getMetadataDescriptionText().equalsIgnoreCase(ActualVaule.trim()))//Checks the metadatacard description for the layer1 configuration
				ExpectedMetadata += "Expected metadatacard description[" + dataPool.get("MetadataDescLayer1") + "] is not set for Layer1 configuration in the metadatacard.[Actual Metadatacard Description:" + metadataCard.getMetadataDescriptionText() + "]";

			if (!metadataCard.getPropertyDescriptionValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyDescLayer1")))//Checks the property description for layer1 configuration
				ExpectedMetadata += "Expected property description[" + dataPool.get("PropertyDescLayer1") + "] is not set for Layer1 configuration in the metadatacard.[Actual property Description:" + metadataCard.getPropertyDescriptionValue(dataPool.get("Property")) + "]";

			if (!metadataCard.getPropertyGroupText(1).equalsIgnoreCase(dataPool.get("Layer1GroupTitle")))//Checks the property group title for layer1 configuration
				ExpectedMetadata += "Expected group title[" + dataPool.get("Layer1GroupTitle") + "] is not set for Layer1 configuration in the metadatacard.[Actual Group title:" + metadataCard.getPropertyGroupText(1) + "]";

			if (!ExpectedMetadata.equals(""))//Checks the layer1 configuration is set properly
				throw new Exception("Layer1 configuration rule is not set as expected. For more details: "+ ExpectedMetadata);

			Log.message("3. Layer1 rule is set as expected in the existing object metadatacard");

			//Step-4: Set the Layer2 rule configuration in the metadata card
			//--------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Layer2Prop"), dataPool.get("Layer2PropValue"));//Sets the required condition for Layer2 configuration

			if (!metadataCard.getPropertyValue(dataPool.get("Layer2Prop")).equalsIgnoreCase(dataPool.get("Layer2PropValue")))//Checks if the required value is set
				throw new Exception("Property( "+ dataPool.get("Layer2Prop") + " ) is not set with the value( "+ dataPool.get("Layer2PropValue") + " ) in the metadatacard");

			Log.message("4. Property( "+ dataPool.get("Layer2Prop") + " ) is set with the value( "+ dataPool.get("Layer2PropValue") + " ) to apply layer2 configuration in the metadatacard");

			//Step-5: Verifies the Second layer configurations in the metadata card
			//--------------------------------------------------------------------
			ExpectedMetadata = "";

			if (!metadataCard.getImageSize().equalsIgnoreCase(dataPool.get("Layer2ImageSize")))//Checks the image size sets in the metadatacard description
				ExpectedMetadata += "Expected metadatacard description image size[" + dataPool.get("Layer2ImageSize") + "] is not set for Layer2 configuration in the metadatacard.[Actual image size:" + metadataCard.getImageSize() + "]";

			String ActualVaule1 = dataPool.get("MetadataDescLayer2").trim().replaceAll("\n", "").replaceAll("\r", "");
			ActualVaule1 = ActualVaule1.replaceAll(" ", "");

			if (!metadataCard.getMetadataDescriptionText().equalsIgnoreCase(ActualVaule1.trim()))//Checks the metadatacard description for layer2 configuration
				ExpectedMetadata += "Expected metadatacard description[" + dataPool.get("MetadataDescLayer2") + "] is not set for Layer2 configuration in the metadatacard.[Actual Metadatacard Description:" + metadataCard.getMetadataDescriptionText() + "]";

			if (!metadataCard.getPropertyDescriptionValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyDescLayer2")))//Checks the property description for layer2 configuration
				ExpectedMetadata += "Expected property description[" + dataPool.get("PropertyDescLayer2") + "] is not set for Layer2 configuration in the metadatacard.[Actual property Description:" + metadataCard.getPropertyDescriptionValue(dataPool.get("Property")) + "]";

			if (!metadataCard.getPropertyGroupText(1).equalsIgnoreCase(dataPool.get("Layer2GroupTitle")))//Checks the property group title for layer2 configuration
				ExpectedMetadata += "Expected group title[" + dataPool.get("Layer2GroupTitle") + "] is not set for Layer2 configuration in the metadatacard.[Actual Group title:" + metadataCard.getPropertyGroupText(1) + "]";


			if (ExpectedMetadata.equals(""))//Checks the layer2 configuration is set correctly
				Log.pass("Test case Passed. Metadata card functionality with two layer configuration hierarchy is working as expected in existing object", driver);
			else
				Log.fail("Test case failed. Layer2 rule is not set as expected in the existing object metadatacard. [" + ExpectedMetadata + "]", driver);


		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_3_2A

	/**
	 * 1.2.3.2B : Metadata card functionality with two layer configuration hierarchy in existing object popped out metadata card 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"FN119_MetadataConfigurability"},description = "Verify the Metadata card functionality with two layer configuration hierarchy in existing object popped out metadatacard" )
	public void TC_1_2_3_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

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
			homePage.taskPanel.clickItem("Properties");

			Log.message("3. Properties is clicked in taks pane.");

			//Step-4: Verifies the First layer configurations in the metadatacard
			//--------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the popout metadatacard of existing object

			String ExpectedMetadata = "";

			if (!metadataCard.getImageSize().equalsIgnoreCase(dataPool.get("Layer1ImageSize")))//Checks the image size sets in the metadatacard description
				ExpectedMetadata += "Expected metadatacard description image size[" + dataPool.get("Layer1ImageSize") + "] is not set for Layer1 configuration in the metadatacard.[Actual image size:" + metadataCard.getImageSize() + "]";

			String ActualVaule = dataPool.get("MetadataDescLayer1").trim().replaceAll("\n", "").replaceAll("\r", "");
			ActualVaule = ActualVaule.replaceAll(" ", "");

			if (!metadataCard.getMetadataDescriptionText().equalsIgnoreCase(ActualVaule.trim()))//Checks the metadatacard description for the layer1 configuration
				ExpectedMetadata += "Expected metadatacard description[" + dataPool.get("MetadataDescLayer1") + "] is not set for Layer1 configuration in the metadatacard.[Actual Metadatacard Description:" + metadataCard.getMetadataDescriptionText() + "]";

			if (!metadataCard.getPropertyDescriptionValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyDescLayer1")))//Checks the property description for layer1 configuration
				ExpectedMetadata += "Expected property description[" + dataPool.get("PropertyDescLayer1") + "] is not set for Layer1 configuration in the metadatacard.[Actual property Description:" + metadataCard.getPropertyDescriptionValue(dataPool.get("Property")) + "]";

			if (!metadataCard.getPropertyGroupText(1).equalsIgnoreCase(dataPool.get("Layer1GroupTitle")))//Checks the property group title for layer1 configuration
				ExpectedMetadata += "Expected group title[" + dataPool.get("Layer1GroupTitle") + "] is not set for Layer1 configuration in the metadatacard.[Actual Group title:" + metadataCard.getPropertyGroupText(1) + "]";

			if (!ExpectedMetadata.equals(""))//Checks the layer1 configuration is set properly
				throw new Exception("Layer1 configuration rule is not set as expected. For more details: "+ ExpectedMetadata);

			Log.message("4. Layer1 rule is set as expected in the existing object metadatacard");

			//Step-5: Set the Layer2 rule configuration in the metadata card
			//--------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Layer2Prop"), dataPool.get("Layer2PropValue"));//Sets the required condition for Layer2 configuration

			if (!metadataCard.getPropertyValue(dataPool.get("Layer2Prop")).equalsIgnoreCase(dataPool.get("Layer2PropValue")))//Checks if the required value is set
				throw new Exception("Property( "+ dataPool.get("Layer2Prop") + " ) is not set with the value( "+ dataPool.get("Layer2PropValue") + " ) in the metadatacard");

			Log.message("5. Property( "+ dataPool.get("Layer2Prop") + " ) is set with the value( "+ dataPool.get("Layer2PropValue") + " ) to apply layer2 configuration in the metadatacard");

			//Step-6: Verifies the Second layer configurations in the metadata card
			//--------------------------------------------------------------------
			ExpectedMetadata = "";

			if (!metadataCard.getImageSize().equalsIgnoreCase(dataPool.get("Layer2ImageSize")))//Checks the image size sets in the metadatacard description
				ExpectedMetadata += "Expected metadatacard description image size[" + dataPool.get("Layer2ImageSize") + "] is not set for Layer2 configuration in the metadatacard.[Actual image size:" + metadataCard.getImageSize() + "]";

			String ActualVaule1 = dataPool.get("MetadataDescLayer2").trim().replaceAll("\n", "").replaceAll("\r", "");
			ActualVaule1 = ActualVaule1.replaceAll(" ", "");

			if (!metadataCard.getMetadataDescriptionText().equalsIgnoreCase(ActualVaule1.trim()))//Checks the metadatacard description for layer2 configuration
				ExpectedMetadata += "Expected metadatacard description[" + dataPool.get("MetadataDescLayer2") + "] is not set for Layer2 configuration in the metadatacard.[Actual Metadatacard Description:" + metadataCard.getMetadataDescriptionText() + "]";

			if (!metadataCard.getPropertyDescriptionValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyDescLayer2")))//Checks the property description for layer2 configuration
				ExpectedMetadata += "Expected property description[" + dataPool.get("PropertyDescLayer2") + "] is not set for Layer2 configuration in the metadatacard.[Actual property Description:" + metadataCard.getPropertyDescriptionValue(dataPool.get("Property")) + "]";

			if (!metadataCard.getPropertyGroupText(1).equalsIgnoreCase(dataPool.get("Layer2GroupTitle")))//Checks the property group title for layer2 configuration
				ExpectedMetadata += "Expected group title[" + dataPool.get("Layer2GroupTitle") + "] is not set for Layer2 configuration in the metadatacard.[Actual Group title:" + metadataCard.getPropertyGroupText(1) + "]";


			if (ExpectedMetadata.equals(""))//Checks the layer2 configuration is set correctly
				Log.pass("Test case Passed. Metadata card functionality with two layer configuration hierarchy is working as expected in existing object popped out metadatacard", driver);
			else
				Log.fail("Test case failed. Layer2 rule is not set as expected in the existing object popped out metadatacard. [" + ExpectedMetadata + "]", driver);


		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_3_2B

	/**
	 * 1.2.3.2C : Metadata card functionality with two layer configuration hierarchy in existing object [Multiselected objects] 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"SKIP_MultiSelect", "FN119_MetadataConfigurability"},description = "Verify the Metadata card functionality with two layer configuration hierarchy in existing object [Multiselected objects]" )
	public void TC_1_2_3_2C(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; //Webdriver instance

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
			homePage.listView.clickMultipleItems(dataPool.get("Object"));//Selects the multiple objects in the view

			Log.message("2. Multiple objects selected in the list view.");

			//Step-3: Verifies the First layer configurations in the metadatacard
			//--------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard of existing object

			String ExpectedMetadata = "";

			if (!metadataCard.getImageSize().equalsIgnoreCase(dataPool.get("Layer1ImageSize")))//Checks the image size sets in the metadatacard description
				ExpectedMetadata += "Expected metadatacard description image size[" + dataPool.get("Layer1ImageSize") + "] is not set for Layer1 configuration in the metadatacard.[Actual image size:" + metadataCard.getImageSize() + "]";

			String ActualVaule = dataPool.get("MetadataDescLayer1").trim().replaceAll("\n", "").replaceAll("\r", "");
			ActualVaule = ActualVaule.replaceAll(" ", "");

			if (!metadataCard.getMetadataDescriptionText().equalsIgnoreCase(ActualVaule.trim()))//Checks the metadatacard description for the layer1 configuration
				ExpectedMetadata += "Expected metadatacard description[" + dataPool.get("MetadataDescLayer1") + "] is not set for Layer1 configuration in the metadatacard.[Actual Metadatacard Description:" + metadataCard.getMetadataDescriptionText() + "]";

			if (!metadataCard.getPropertyDescriptionValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyDescLayer1")))//Checks the property description for layer1 configuration
				ExpectedMetadata += "Expected property description[" + dataPool.get("PropertyDescLayer1") + "] is not set for Layer1 configuration in the metadatacard.[Actual property Description:" + metadataCard.getPropertyDescriptionValue(dataPool.get("Property")) + "]";

			if (!metadataCard.getPropertyGroupText(1).equalsIgnoreCase(dataPool.get("Layer1GroupTitle")))//Checks the property group title for layer1 configuration
				ExpectedMetadata += "Expected group title[" + dataPool.get("Layer1GroupTitle") + "] is not set for Layer1 configuration in the metadatacard.[Actual Group title:" + metadataCard.getPropertyGroupText(1) + "]";

			if (!ExpectedMetadata.equals(""))//Checks the layer1 configuration is set properly
				throw new Exception("Layer1 configuration rule is not set as expected. For more details: "+ ExpectedMetadata);

			Log.message("3. Layer1 rule is set as expected in the existing object metadatacard");

			//Step-4: Set the Layer2 rule configuration in the metadata card
			//--------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Layer2Prop"), dataPool.get("Layer2PropValue"));//Sets the required condition for Layer2 configuration

			if (!metadataCard.getPropertyValue(dataPool.get("Layer2Prop")).equalsIgnoreCase(dataPool.get("Layer2PropValue")))//Checks if the required value is set
				throw new Exception("Property( "+ dataPool.get("Layer2Prop") + " ) is not set with the value( "+ dataPool.get("Layer2PropValue") + " ) in the metadatacard");

			Log.message("4. Property( "+ dataPool.get("Layer2Prop") + " ) is set with the value( "+ dataPool.get("Layer2PropValue") + " ) to apply layer2 configuration in the metadatacard");

			//Step-5: Verifies the Second layer configurations in the metadata card
			//--------------------------------------------------------------------
			ExpectedMetadata = "";

			if (!metadataCard.getImageSize().equalsIgnoreCase(dataPool.get("Layer2ImageSize")))//Checks the image size sets in the metadatacard description
				ExpectedMetadata += "Expected metadatacard description image size[" + dataPool.get("Layer2ImageSize") + "] is not set for Layer2 configuration in the metadatacard.[Actual image size:" + metadataCard.getImageSize() + "]";


			String ActualVaule1 = dataPool.get("MetadataDescLayer2").trim().replaceAll("\n", "").replaceAll("\r", "");
			ActualVaule1 = ActualVaule1.replaceAll(" ", "");

			if (!metadataCard.getMetadataDescriptionText().equalsIgnoreCase(ActualVaule1.trim()))//Checks the metadatacard description for layer2 configuration
				ExpectedMetadata += "Expected metadatacard description[" + dataPool.get("MetadataDescLayer2") + "] is not set for Layer2 configuration in the metadatacard.[Actual Metadatacard Description:" + metadataCard.getMetadataDescriptionText() + "]";

			if (!metadataCard.getPropertyDescriptionValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyDescLayer2")))//Checks the property description for layer2 configuration
				ExpectedMetadata += "Expected property description[" + dataPool.get("PropertyDescLayer2") + "] is not set for Layer2 configuration in the metadatacard.[Actual property Description:" + metadataCard.getPropertyDescriptionValue(dataPool.get("Property")) + "]";

			if (!metadataCard.getPropertyGroupText(1).equalsIgnoreCase(dataPool.get("Layer2GroupTitle")))//Checks the property group title for layer2 configuration
				ExpectedMetadata += "Expected group title[" + dataPool.get("Layer2GroupTitle") + "] is not set for Layer2 configuration in the metadatacard.[Actual Group title:" + metadataCard.getPropertyGroupText(1) + "]";


			if (ExpectedMetadata.equals(""))//Checks the layer2 configuration is set correctly
				Log.pass("Test case Passed. Metadata card functionality with two layer configuration hierarchy is working as expected in multiselected existing objects", driver);
			else
				Log.fail("Test case failed. Layer2 rule is not set as expected in the multiselected existing object metadatacard. [" + ExpectedMetadata + "]", driver);


		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_3_2C

	/**
	 * 1.2.3.2D : Metadata card functionality with two layer configuration hierarchy in multi selected existing object popped out metadata card 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"SKIP_MultiSelect", "FN119_MetadataConfigurability"},description = "Verify the Metadata card functionality with two layer configuration hierarchy in multiple existing object popped out metadatacard" )
	public void TC_1_2_3_2D(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; //Webdriver instance

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
			homePage.listView.clickMultipleItems(dataPool.get("Object"));//Selects the multiple objects in the view

			Log.message("2. Multiple objects selected in the list view.");

			//Step-3: Open the popout metadatacard of the object
			//--------------------------------------------------
			homePage.taskPanel.clickItem("Properties");

			Log.message("3. Properties is clicked in taks pane.");

			//Step-4: Verifies the First layer configurations in the metadatacard
			//--------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the popout metadatacard of existing object

			String ExpectedMetadata = "";

			if (!metadataCard.getImageSize().equalsIgnoreCase(dataPool.get("Layer1ImageSize")))//Checks the image size sets in the metadatacard description
				ExpectedMetadata += "Expected metadatacard description image size[" + dataPool.get("Layer1ImageSize") + "] is not set for Layer1 configuration in the metadatacard.[Actual image size:" + metadataCard.getImageSize() + "]";

			String ActualVaule = dataPool.get("MetadataDescLayer1").trim().replaceAll("\n", "").replaceAll("\r", "");
			ActualVaule = ActualVaule.replaceAll(" ", "");

			if (!metadataCard.getMetadataDescriptionText().equalsIgnoreCase(ActualVaule.trim()))//Checks the metadatacard description for the layer1 configuration
				ExpectedMetadata += "Expected metadatacard description[" + dataPool.get("MetadataDescLayer1") + "] is not set for Layer1 configuration in the metadatacard.[Actual Metadatacard Description:" + metadataCard.getMetadataDescriptionText() + "]";

			if (!metadataCard.getPropertyDescriptionValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyDescLayer1")))//Checks the property description for layer1 configuration
				ExpectedMetadata += "Expected property description[" + dataPool.get("PropertyDescLayer1") + "] is not set for Layer1 configuration in the metadatacard.[Actual property Description:" + metadataCard.getPropertyDescriptionValue(dataPool.get("Property")) + "]";

			if (!metadataCard.getPropertyGroupText(1).equalsIgnoreCase(dataPool.get("Layer1GroupTitle")))//Checks the property group title for layer1 configuration
				ExpectedMetadata += "Expected group title[" + dataPool.get("Layer1GroupTitle") + "] is not set for Layer1 configuration in the metadatacard.[Actual Group title:" + metadataCard.getPropertyGroupText(1) + "]";

			if (!ExpectedMetadata.equals(""))//Checks the layer1 configuration is set properly
				throw new Exception("Layer1 configuration rule is not set as expected. For more details: "+ ExpectedMetadata);

			Log.message("4. Layer1 rule is set as expected in the existing object metadatacard");

			//Step-5: Set the Layer2 rule configuration in the metadata card
			//--------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Layer2Prop"), dataPool.get("Layer2PropValue"));//Sets the required condition for Layer2 configuration

			if (!metadataCard.getPropertyValue(dataPool.get("Layer2Prop")).equalsIgnoreCase(dataPool.get("Layer2PropValue")))//Checks if the required value is set
				throw new Exception("Property( "+ dataPool.get("Layer2Prop") + " ) is not set with the value( "+ dataPool.get("Layer2PropValue") + " ) in the metadatacard");

			Log.message("5. Property( "+ dataPool.get("Layer2Prop") + " ) is set with the value( "+ dataPool.get("Layer2PropValue") + " ) to apply layer2 configuration in the metadatacard");

			//Step-6: Verifies the Second layer configurations in the metadata card
			//--------------------------------------------------------------------
			ExpectedMetadata = "";

			if (!metadataCard.getImageSize().equalsIgnoreCase(dataPool.get("Layer2ImageSize")))//Checks the image size sets in the metadatacard description
				ExpectedMetadata += "Expected metadatacard description image size[" + dataPool.get("Layer2ImageSize") + "] is not set for Layer2 configuration in the metadatacard.[Actual image size:" + metadataCard.getImageSize() + "]";

			String ActualVaule1 = dataPool.get("MetadataDescLayer2").trim().replaceAll("\n", "").replaceAll("\r", "");
			ActualVaule1 = ActualVaule1.replaceAll(" ", "");

			if (!metadataCard.getMetadataDescriptionText().equalsIgnoreCase(ActualVaule1.trim()))//Checks the metadatacard description for layer2 configuration
				ExpectedMetadata += "Expected metadatacard description[" + dataPool.get("MetadataDescLayer2") + "] is not set for Layer2 configuration in the metadatacard.[Actual Metadatacard Description:" + metadataCard.getMetadataDescriptionText() + "]";

			if (!metadataCard.getPropertyDescriptionValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyDescLayer2")))//Checks the property description for layer2 configuration
				ExpectedMetadata += "Expected property description[" + dataPool.get("PropertyDescLayer2") + "] is not set for Layer2 configuration in the metadatacard.[Actual property Description:" + metadataCard.getPropertyDescriptionValue(dataPool.get("Property")) + "]";

			if (!metadataCard.getPropertyGroupText(1).equalsIgnoreCase(dataPool.get("Layer2GroupTitle")))//Checks the property group title for layer2 configuration
				ExpectedMetadata += "Expected group title[" + dataPool.get("Layer2GroupTitle") + "] is not set for Layer2 configuration in the metadatacard.[Actual Group title:" + metadataCard.getPropertyGroupText(1) + "]";


			if (ExpectedMetadata.equals(""))//Checks the layer2 configuration is set correctly
				Log.pass("Test case Passed. Metadata card functionality with two layer configuration hierarchy is working as expected in multiselected existing object popped out metadatacard", driver);
			else
				Log.fail("Test case failed. Layer2 rule is not set as expected in the multiselected existing object popped out metadatacard. [" + ExpectedMetadata + "]", driver);


		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_1_2_3_2D

	/**
	 * 1.4.4.1A : Rule contains several conditions (classId, objectType and properties) [Object creation via task pane] 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"FN119_MetadataConfigurability"},description = "Verify the Metadata card functionality with several conditions (classId, objectType and properties) while Object creation via task pane" )
	public void TC_1_4_4_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();


			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Click the new object link from task pane
			//---------------------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));//Clicks the new object link from task pane

			Log.message("1. First condition (ObjectType) is set[" + dataPool.get("ObjectType") + " is clicked from task pane]");

			//Step-2: Selects the class in the metadatacard
			//---------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.setPropertyValue("Class", dataPool.get("ClassName"));// Sets the second condition in the metadatacard

			Log.message("2. Second condition (Class) is set[" + dataPool.get("ClassName") + " is set in the metadatacard]");

			//Step-3: Sets the necessary properties in the metadatacard for third condition
			//-----------------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Property1"), dataPool.get("Property1Value"));// Adds & sets the Property1 in the metadatacard

			if (!metadataCard.getPropertyValue(dataPool.get("Property1")).equalsIgnoreCase(dataPool.get("Property1Value")))
				throw new Exception("Property("+ dataPool.get("Property1")+ ") is not set with value("+ dataPool.get("Property1Value")+") in the metadatacard for third condition");

			metadataCard.setPropertyValue(dataPool.get("Property2"), dataPool.get("Property2Value"));// Adds & sets the Property1 in the metadatacard

			if (!metadataCard.getPropertyValue(dataPool.get("Property2")).equalsIgnoreCase(dataPool.get("Property2Value")))
				throw new Exception("Property("+ dataPool.get("Property2")+ ") is not set with value("+ dataPool.get("Property2Value")+") in the metadatacard for third condition");

			Log.message("3. Third condition (Properties) is set[Properties with their required values is set in the metadatacard]");
			Log.message("3.1. Property("+ dataPool.get("Property1")+ ") is set with value("+ dataPool.get("Property1Value")+") in the metadatacard");
			Log.message("3.2. Property("+ dataPool.get("Property2")+ ") is set with value("+ dataPool.get("Property2Value")+") in the metadatacard");

			//Verifies the metadatacard configurations applied in the metadatacard
			//-----------------------------------------------------------------------

			String ExpectedMetadata = "";

			String ActualVaule = dataPool.get("MetadataDesc").trim().replaceAll("\n", "").replaceAll("\r", "");
			ActualVaule = ActualVaule.replaceAll(" ", "");

			if (!metadataCard.getMetadataDescriptionText().equalsIgnoreCase(ActualVaule.trim()))
				ExpectedMetadata = "Expected Metadatacard Description("+ dataPool.get("MetadataDesc") +") is not in the metadatacard. [Actual value:"+ metadataCard.getMetadataDescriptionText() +"]";

			if (!dataPool.get("HeaderEditColor").toUpperCase().contains(metadataCard.getHeaderColor().toUpperCase()))
				ExpectedMetadata += " Expected header color("+ dataPool.get("HeaderEditColor") +") is not displayed in the metadatacard in Edit mode. [Actual Value:"+ metadataCard.getHeaderColor() +"]"; 

			if (ExpectedMetadata.equals(""))
				Log.pass("Test case Passed. Metadata card functionality with several conditions (classId, objectType and properties) is working as expected while Object creation via task pane", driver);
			else
				Log.fail("Test case Failed. Metadata card functionality with several conditions (classId, objectType and properties) is not working as expected while Object creation via task pane. For more details:["+ ExpectedMetadata +"]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End 	TC_1_4_4_1A

	/**
	 * 1.4.4.1B : Rule contains several conditions (classId, objectType and properties) [Object creation via New item menu] 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"FN119_MetadataConfigurability"},description = "Verify the Metadata card functionality with several conditions (classId, objectType and properties) while Object creation via New item menu" )
	public void TC_1_4_4_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Click the new object link from new item menu
			//---------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//Clicks the new menu item

			Log.message("1. First condition (ObjectType) is set[" + dataPool.get("ObjectType") + " is clicked from new item menu]");

			//Step-2: Selects the class in the metadatacard
			//---------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.setPropertyValue("Class", dataPool.get("ClassName"));// Sets the second condition in the metadatacard

			Log.message("2. Second condition (Class) is set[" + dataPool.get("ClassName") + " is set in the metadatacard]");

			//Step-3: Sets the necessary properties in the metadatacard for third condition
			//-----------------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Property1"), dataPool.get("Property1Value"));// Adds & sets the Property1 in the metadatacard

			if (!metadataCard.getPropertyValue(dataPool.get("Property1")).equalsIgnoreCase(dataPool.get("Property1Value")))
				throw new Exception("Property("+ dataPool.get("Property1")+ ") is not set with value("+ dataPool.get("Property1Value")+") in the metadatacard for third condition");

			metadataCard.setPropertyValue(dataPool.get("Property2"), dataPool.get("Property2Value"));// Adds & sets the Property1 in the metadatacard

			if (!metadataCard.getPropertyValue(dataPool.get("Property2")).equalsIgnoreCase(dataPool.get("Property2Value")))
				throw new Exception("Property("+ dataPool.get("Property2")+ ") is not set with value("+ dataPool.get("Property2Value")+") in the metadatacard for third condition");

			Log.message("3. Third condition (Properties) is set[Properties with their required values is set in the metadatacard]");
			Log.message("3.1. Property("+ dataPool.get("Property1")+ ") is set with value("+ dataPool.get("Property1Value")+") in the metadatacard");
			Log.message("3.2. Property("+ dataPool.get("Property2")+ ") is set with value("+ dataPool.get("Property2Value")+") in the metadatacard");

			//Verifies the metadatacard configurations applied in the metadatacard
			//-----------------------------------------------------------------------

			String ExpectedMetadata = "";

			String ActualVaule = dataPool.get("MetadataDesc").trim().replaceAll("\n", "").replaceAll("\r", "");
			ActualVaule = ActualVaule.replaceAll(" ", "");

			if (!metadataCard.getMetadataDescriptionText().equalsIgnoreCase(ActualVaule.trim()))
				ExpectedMetadata = "Expected Metadatacard Description("+ dataPool.get("MetadataDesc") +") is not in the metadatacard. [Actual value:"+ metadataCard.getMetadataDescriptionText() +"]";

			if (!dataPool.get("HeaderEditColor").toUpperCase().contains(metadataCard.getHeaderColor().toUpperCase()))
				ExpectedMetadata += " Expected header color("+ dataPool.get("HeaderEditColor") +") is not displayed in the metadatacard in Edit mode. [Actual Value:"+ metadataCard.getHeaderColor() +"]"; 

			if (ExpectedMetadata.equals(""))
				Log.pass("Test case Passed. Metadata card functionality with several conditions (classId, objectType and properties) is working as expected while Object creation via new item menu", driver);
			else
				Log.fail("Test case Failed. Metadata card functionality with several conditions (classId, objectType and properties) is not working as expected while Object creation via new item menu. For more details:["+ ExpectedMetadata +"]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End 	TC_1_4_4_1B

	/**
	 * 1.4.4.1C : Rule contains several conditions (classId, objectType and properties) [Object creation via object property] 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"FN119_MetadataConfigurability"},description = "Verify the Metadata card functionality with several conditions (classId, objectType and properties) while Object creation via object property" )
	public void TC_1_4_4_1C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Open the new object metadatacard via object property
			//-----------------------------------
			SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard of existing object

			metadataCard.clickAddValueButton(dataPool.get("ObjectType"));//Clicks the Add value button to open the new customer object metadatacard

			Log.message("1. First condition (ObjectType) is set[" + dataPool.get("ObjectType") + " is clicked from object property]");

			//Step-2: Selects the class in the metadatacard
			//---------------------------------------------
			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard.setPropertyValue("Class", dataPool.get("ClassName"));// Sets the second condition in the metadatacard

			Log.message("2. Second condition (Class) is set[" + dataPool.get("ClassName") + " is set in the metadatacard]");

			//Step-3: Sets the necessary properties in the metadatacard for third condition
			//-----------------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Property1"), dataPool.get("Property1Value"));// Adds & sets the Property1 in the metadatacard

			if (!metadataCard.getPropertyValue(dataPool.get("Property1")).equalsIgnoreCase(dataPool.get("Property1Value")))
				throw new Exception("Property("+ dataPool.get("Property1")+ ") is not set with value("+ dataPool.get("Property1Value")+") in the metadatacard for third condition");

			metadataCard.setPropertyValue(dataPool.get("Property2"), dataPool.get("Property2Value"));// Adds & sets the Property1 in the metadatacard

			if (!metadataCard.getPropertyValue(dataPool.get("Property2")).equalsIgnoreCase(dataPool.get("Property2Value")))
				throw new Exception("Property("+ dataPool.get("Property2")+ ") is not set with value("+ dataPool.get("Property2Value")+") in the metadatacard for third condition");

			Log.message("3. Third condition (Properties) is set[Properties with their required values is set in the metadatacard]");
			Log.message("3.1. Property("+ dataPool.get("Property1")+ ") is set with value("+ dataPool.get("Property1Value")+") in the metadatacard");
			Log.message("3.2. Property("+ dataPool.get("Property2")+ ") is set with value("+ dataPool.get("Property2Value")+") in the metadatacard");

			//Verifies the metadatacard configurations applied in the metadatacard
			//-----------------------------------------------------------------------

			String ExpectedMetadata = "";

			String ActualVaule = dataPool.get("MetadataDesc").trim().replaceAll("\n", "").replaceAll("\r", "");
			ActualVaule = ActualVaule.replaceAll(" ", "");

			if (!metadataCard.getMetadataDescriptionText().equalsIgnoreCase(ActualVaule.trim()))
				ExpectedMetadata = "Expected Metadatacard Description("+ dataPool.get("MetadataDesc") +") is not in the metadatacard. [Actual value:"+ metadataCard.getMetadataDescriptionText() +"]";

			if (!dataPool.get("HeaderEditColor").toUpperCase().contains(metadataCard.getHeaderColor().toUpperCase()))
				ExpectedMetadata += " Expected header color("+ dataPool.get("HeaderEditColor") +") is not displayed in the metadatacard in Edit mode. [Actual Value:"+ metadataCard.getHeaderColor() +"]"; 

			if (ExpectedMetadata.equals(""))
				Log.pass("Test case Passed. Metadata card functionality with several conditions (classId, objectType and properties) is working as expected while Object creation via object property", driver);
			else
				Log.fail("Test case Failed. Metadata card functionality with several conditions (classId, objectType and properties) is not working as expected while Object creation via object property. For more details:["+ ExpectedMetadata +"]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End 	TC_1_4_4_1C

	/**
	 * 1.4.4.2A : Rule contains several conditions (classId, objectType and properties) [Object modification in existing object] 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"FN119_MetadataConfigurability"},description = "Verify the Metadata card functionality with several conditions (classId, objectType and properties) while Object modification in existing object" )
	public void TC_1_4_4_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Open the new object metadatacard via object property
			//-----------------------------------
			SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			Log.message("1. First condition (ObjectType) is set[Existing object " + dataPool.get("Object") + " is selected in the view]");

			//Step-2: Selects the class in the metadatacard
			//---------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard of existing object

			metadataCard.setPropertyValue("Class", dataPool.get("ClassName"));// Sets the second condition in the metadatacard

			Log.message("2. Second condition (Class) is set[" + dataPool.get("ClassName") + " is set in the metadatacard]");

			//Step-3: Sets the necessary properties in the metadatacard for third condition
			//-----------------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Property1"), dataPool.get("Property1Value"));// Adds & sets the Property1 in the metadatacard

			if (!metadataCard.getPropertyValue(dataPool.get("Property1")).equalsIgnoreCase(dataPool.get("Property1Value")))
				throw new Exception("Property("+ dataPool.get("Property1")+ ") is not set with value("+ dataPool.get("Property1Value")+") in the metadatacard for third condition");

			metadataCard.setPropertyValue(dataPool.get("Property2"), dataPool.get("Property2Value"));// Adds & sets the Property1 in the metadatacard

			if (!metadataCard.getPropertyValue(dataPool.get("Property2")).equalsIgnoreCase(dataPool.get("Property2Value")))
				throw new Exception("Property("+ dataPool.get("Property2")+ ") is not set with value("+ dataPool.get("Property2Value")+") in the metadatacard for third condition");

			Log.message("3. Third condition (Properties) is set[Properties with their required values is set in the metadatacard]");
			Log.message("3.1. Property("+ dataPool.get("Property1")+ ") is set with value("+ dataPool.get("Property1Value")+") in the metadatacard");
			Log.message("3.2. Property("+ dataPool.get("Property2")+ ") is set with value("+ dataPool.get("Property2Value")+") in the metadatacard");

			//Verifies the metadatacard configurations applied in the metadatacard
			//-----------------------------------------------------------------------

			String ExpectedMetadata = "";

			String ActualVaule = dataPool.get("MetadataDesc").trim().replaceAll("\n", "").replaceAll("\r", "");
			ActualVaule = ActualVaule.replaceAll(" ", "");

			if (!metadataCard.getMetadataDescriptionText().equalsIgnoreCase(ActualVaule.trim()))
				ExpectedMetadata = "Expected Metadatacard Description("+ dataPool.get("MetadataDesc") +") is not in the metadatacard. [Actual value:"+ metadataCard.getMetadataDescriptionText() +"]";

			if (!dataPool.get("HeaderEditColor").toUpperCase().contains(metadataCard.getHeaderColor().toUpperCase()))
				ExpectedMetadata += " Expected header color("+ dataPool.get("HeaderEditColor") +") is not displayed in the metadatacard in Edit mode. [Actual Value:"+ metadataCard.getHeaderColor() +"]"; 

			if (ExpectedMetadata.equals(""))
				Log.pass("Test case Passed. Metadata card functionality with several conditions (classId, objectType and properties) is working as expected while Object modification in existing object", driver);
			else	
				Log.fail("Test case Failed. Metadata card functionality with several conditions (classId, objectType and properties) is not working as expected while Object modification in existing object. For more details:["+ ExpectedMetadata +"]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End 	TC_1_4_4_2A

	/**
	 * 1.4.4.2B : Rule contains several conditions (classId, objectType and properties) [Object modification in existing object via pop out metadatacard] 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"FN119_MetadataConfigurability"},description = "Verify the Metadata card functionality with several conditions (classId, objectType and properties) while Object modification in existing object popped out metadatacard" )
	public void TC_1_4_4_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Open the new object metadatacard via object property
			//-----------------------------------
			SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			homePage.menuBar.ClickOperationsMenu("Properties");//Opens the popped out metadatacard

			Log.message("1. First condition (ObjectType) is set[Existing object " + dataPool.get("Object") + " is selected and popped out metadatacard of that object is opened in the view]");

			//Step-2: Selects the class in the metadatacard
			//---------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard of existing object

			metadataCard.setPropertyValue("Class", dataPool.get("ClassName"));// Sets the second condition in the metadatacard

			Log.message("2. Second condition (Class) is set[" + dataPool.get("ClassName") + " is set in the metadatacard]");

			//Step-3: Sets the necessary properties in the metadatacard for third condition
			//-----------------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Property1"), dataPool.get("Property1Value"));// Adds & sets the Property1 in the metadatacard

			if (!metadataCard.getPropertyValue(dataPool.get("Property1")).equalsIgnoreCase(dataPool.get("Property1Value")))
				throw new Exception("Property("+ dataPool.get("Property1")+ ") is not set with value("+ dataPool.get("Property1Value")+") in the metadatacard for third condition");

			metadataCard.setPropertyValue(dataPool.get("Property2"), dataPool.get("Property2Value"));// Adds & sets the Property1 in the metadatacard

			if (!metadataCard.getPropertyValue(dataPool.get("Property2")).equalsIgnoreCase(dataPool.get("Property2Value")))
				throw new Exception("Property("+ dataPool.get("Property2")+ ") is not set with value("+ dataPool.get("Property2Value")+") in the metadatacard for third condition");

			Log.message("3. Third condition (Properties) is set[Properties with their required values is set in the metadatacard]");
			Log.message("3.1. Property("+ dataPool.get("Property1")+ ") is set with value("+ dataPool.get("Property1Value")+") in the metadatacard");
			Log.message("3.2. Property("+ dataPool.get("Property2")+ ") is set with value("+ dataPool.get("Property2Value")+") in the metadatacard");

			//Verifies the metadatacard configurations applied in the metadatacard
			//-----------------------------------------------------------------------

			String ExpectedMetadata = "";

			String ActualVaule = dataPool.get("MetadataDesc").trim().replaceAll("\n", "").replaceAll("\r", "");
			ActualVaule = ActualVaule.replaceAll(" ", "");

			if (!metadataCard.getMetadataDescriptionText().equalsIgnoreCase(ActualVaule))
				ExpectedMetadata = "Expected Metadatacard Description("+ dataPool.get("MetadataDesc") +") is not in the metadatacard. [Actual value:"+ metadataCard.getMetadataDescriptionText() +"]";

			if (!dataPool.get("HeaderEditColor").toUpperCase().contains(metadataCard.getHeaderColor().toUpperCase()))
				ExpectedMetadata += " Expected header color("+ dataPool.get("HeaderEditColor") +") is not displayed in the metadatacard in Edit mode. [Actual Value:"+ metadataCard.getHeaderColor() +"]"; 

			if (ExpectedMetadata.equals(""))
				Log.pass("Test case Passed. Metadata card functionality with several conditions (classId, objectType and properties) is working as expected while Object modification in existing object poppoed out metadatacard", driver);
			else	
				Log.fail("Test case Failed. Metadata card functionality with several conditions (classId, objectType and properties) is not working as expected while Object modification in existing object popped out metadatacard. For more details:["+ ExpectedMetadata +"]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End 	TC_1_4_4_2B


	/**
	 * 1.4.4.3A : Rule contains several conditions (classId, objectType and properties) [Object modification in multi selected existing objects metadata card] 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"SKIP_MultiSelect", "FN119_MetadataConfigurability","Bug"},description = "Verify the Metadata card functionality with several conditions (classId, objectType and properties) while Object modification in multi selected existing objects" )
	public void TC_1_4_4_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Open the new object metadatacard via object property
			//-----------------------------------
			SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			homePage.listView.clickMultipleItems(dataPool.get("Object"));//Selects the multiple objects in the view

			Log.message("1. First condition (ObjectType) is set[Existing object " + dataPool.get("Object") + " is selected in the view]");

			//Step-2: Selects the class in the metadatacard
			//---------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard of existing object

			metadataCard.setPropertyValue("Class", dataPool.get("ClassName"));// Sets the second condition in the metadatacard

			Log.message("2. Second condition (Class) is set[" + dataPool.get("ClassName") + " is set in the metadatacard]");

			//Step-3: Sets the necessary properties in the metadatacard for third condition
			//-----------------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Property1"), dataPool.get("Property1Value"));// Adds & sets the Property1 in the metadatacard

			if (!metadataCard.getPropertyValue(dataPool.get("Property1")).equalsIgnoreCase(dataPool.get("Property1Value")))
				throw new Exception("Property("+ dataPool.get("Property1")+ ") is not set with value("+ dataPool.get("Property1Value")+") in the metadatacard for third condition");

			metadataCard.setPropertyValue(dataPool.get("Property2"), dataPool.get("Property2Value"));// Adds & sets the Property1 in the metadatacard

			if (!metadataCard.getPropertyValue(dataPool.get("Property2")).equalsIgnoreCase(dataPool.get("Property2Value")))
				throw new Exception("Property("+ dataPool.get("Property2")+ ") is not set with value("+ dataPool.get("Property2Value")+") in the metadatacard for third condition");

			Log.message("3. Third condition (Properties) is set[Properties with their required values is set in the metadatacard]");
			Log.message("3.1. Property("+ dataPool.get("Property1")+ ") is set with value("+ dataPool.get("Property1Value")+") in the metadatacard");
			Log.message("3.2. Property("+ dataPool.get("Property2")+ ") is set with value("+ dataPool.get("Property2Value")+") in the metadatacard");

			//Verifies the metadatacard configurations applied in the metadatacard
			//-----------------------------------------------------------------------
			driver.switchTo().defaultContent();

			metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard of existing object

			String ExpectedMetadata = "";

			String ActualVaule = dataPool.get("MetadataDesc").trim().replaceAll("\n", "").replaceAll("\r", "");
			ActualVaule = ActualVaule.replaceAll(" ", "");

			if (!metadataCard.getMetadataDescriptionText().equalsIgnoreCase(ActualVaule.trim()))
				ExpectedMetadata = "Expected Metadatacard Description("+ dataPool.get("MetadataDesc") +") is not in the metadatacard. [Actual value:"+ metadataCard.getMetadataDescriptionText() +"]";

			if (!dataPool.get("HeaderEditColor").toUpperCase().contains(metadataCard.getHeaderColor().toUpperCase()))
				ExpectedMetadata += " Expected header color("+ dataPool.get("HeaderEditColor") +") is not displayed in the metadatacard in Edit mode. [Actual Value:"+ metadataCard.getHeaderColor() +"]"; 

			if (ExpectedMetadata.equals(""))
				Log.pass("Test case Passed. Metadata card functionality with several conditions (classId, objectType and properties) is working as expected while Object modification in multi selected existing objects", driver);
			else	
				Log.fail("Test case Failed. Metadata card functionality with several conditions (classId, objectType and properties) is not working as expected while Object modification in multi selected existing objects. For more details:["+ ExpectedMetadata +"]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End 	TC_1_4_4_3A

	/**
	 * 1.4.4.3B : Rule contains several conditions (classId, objectType and properties) [Object modification in multi selected existing objects pop out metadata card] 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"SKIP_MultiSelect", "FN119_MetadataConfigurability","Bug"},description = "Verify the Metadata card functionality with several conditions (classId, objectType and properties) while Object modification in multiselected existing objects popped out metadatacard" )
	public void TC_1_4_4_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("SAFARI"))
			throw new SkipException(driverType + " driver does not supports Multi-select.");

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Open the new object metadatacard via object property
			//-----------------------------------
			SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			homePage.listView.clickMultipleItems(dataPool.get("Object"));//Selects the multiple objects in the view

			homePage.menuBar.ClickOperationsMenu("Properties");//Opens the popped out metadatacard

			Log.message("1. First condition (ObjectType) is set[Existing object " + dataPool.get("Object") + " is selected and popped out metadatacard of that object is opened in the view]");

			//Step-2: Selects the class in the metadatacard
			//---------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard of existing object

			metadataCard.setPropertyValue("Class", dataPool.get("ClassName"));// Sets the second condition in the metadatacard

			Log.message("2. Second condition (Class) is set[" + dataPool.get("ClassName") + " is set in the metadatacard]");

			//Step-3: Sets the necessary properties in the metadatacard for third condition
			//-----------------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Property1"), dataPool.get("Property1Value"));// Adds & sets the Property1 in the metadatacard

			if (!metadataCard.getPropertyValue(dataPool.get("Property1")).equalsIgnoreCase(dataPool.get("Property1Value")))
				throw new Exception("Property("+ dataPool.get("Property1")+ ") is not set with value("+ dataPool.get("Property1Value")+") in the metadatacard for third condition");

			metadataCard.setPropertyValue(dataPool.get("Property2"), dataPool.get("Property2Value"));// Adds & sets the Property1 in the metadatacard

			if (!metadataCard.getPropertyValue(dataPool.get("Property2")).equalsIgnoreCase(dataPool.get("Property2Value")))
				throw new Exception("Property("+ dataPool.get("Property2")+ ") is not set with value("+ dataPool.get("Property2Value")+") in the metadatacard for third condition");

			Log.message("3. Third condition (Properties) is set[Properties with their required values is set in the metadatacard]");
			Log.message("3.1. Property("+ dataPool.get("Property1")+ ") is set with value("+ dataPool.get("Property1Value")+") in the metadatacard");
			Log.message("3.2. Property("+ dataPool.get("Property2")+ ") is set with value("+ dataPool.get("Property2Value")+") in the metadatacard");

			//Verifies the metadatacard configurations applied in the metadatacard
			//-----------------------------------------------------------------------
			driver.switchTo().defaultContent();

			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard of existing object

			String ExpectedMetadata = "";

			String ActualVaule = dataPool.get("MetadataDesc").trim().replaceAll("\n", "").replaceAll("\r", "");
			ActualVaule = ActualVaule.replaceAll(" ", "");

			if (!metadataCard.getMetadataDescriptionText().equalsIgnoreCase(ActualVaule.trim()))
				ExpectedMetadata = "Expected Metadatacard Description("+ dataPool.get("MetadataDesc") +") is not in the metadatacard. [Actual value:"+ metadataCard.getMetadataDescriptionText() +"]";

			if (!dataPool.get("HeaderEditColor").toUpperCase().contains(metadataCard.getHeaderColor().toUpperCase()))
				ExpectedMetadata += " Expected header color("+ dataPool.get("HeaderEditColor") +") is not displayed in the metadatacard in Edit mode. [Actual Value:"+ metadataCard.getHeaderColor() +"]"; 

			if (ExpectedMetadata.equals(""))
				Log.pass("Test case Passed. Metadata card functionality with several conditions (classId, objectType and properties) is working as expected while Object modification in multiselected existing objects poppoed out metadatacard", driver);
			else	
				Log.fail("Test case Failed. Metadata card functionality with several conditions (classId, objectType and properties) is not working as expected while Object modification in multiselected existing objects popped out metadatacard. For more details:["+ ExpectedMetadata +"]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End 	TC_1_4_4_3B

	/**
	 * 1.4.3A : Hierarchical rules, more than one rule in same level [New Object creation] 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"FN119_MetadataConfigurability","Bug"},description = "Verify the Metadata card functionality with Hierarchical rules, more than one rule in same level [New Object creation] " )
	public void TC_1_4_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in
			String ExpectedMetadata = "";

			//Step-1: Click the new object link from task pane
			//---------------------------------------------------
			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));

			Log.message("1."+ dataPool.get("ObjectType") +" is clicked from task pane");

			//Step-2: Opens the document object metadatacard
			//----------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			metadataCard = metadataCard.setTemplate(dataPool.get("Template"));//Selects the template in the metadatacard

			metadataCard.setPropertyValue(dataPool.get("PropName1"), dataPool.get("PropName1Value1"));//Sets the class in the metadatacard

			if (!metadataCard.getPropertyValue(dataPool.get("PropName1")).equalsIgnoreCase(dataPool.get("PropName1Value1")))
				throw new Exception("Property("+dataPool.get("PropName1")+") is not set with value("+dataPool.get("PropName1Value1")+") in the metadatacard");

			Log.message("2. New "+dataPool.get("ObjectType") +" object with class("+ dataPool.get("PropName1Value1") +") metadatacard is opened", driver);

			//Step-2.1: Cheks the first level behavior in the metadatacard
			//------------------------------------------------------
			if (metadataCard.propertyExists(dataPool.get("Property1")))
				ExpectedMetadata += "Property("+ dataPool.get("Property1") + ") is exists in the metadatacard for the first level hierarchy.";

			//Step-3: Sets the level 1.1 hierarchy behavior in the metadatacard
			//--------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("PropName1"), dataPool.get("PropName1Value2"));//Sets the class in the metadatacard

			if (!metadataCard.getPropertyValue(dataPool.get("PropName1")).equalsIgnoreCase(dataPool.get("PropName1Value2")))
				throw new Exception("Property("+dataPool.get("PropName1")+") is not set with value("+dataPool.get("PropName1Value2")+") in the metadatacard for level 1.1 hierarchy rule");

			Log.message("3. Property("+dataPool.get("PropName1")+") is set with value("+dataPool.get("PropName1Value2")+") in the metadatacard for level 1.1 hierarchy rule", driver);

			//Step-3.1: Checks the level 1.1 behavior in the metadatacard
			//------------------------------------------------------------
			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			if (!metadataCard.propertyExists(dataPool.get("Property1")))
				ExpectedMetadata += " Property("+ dataPool.get("Property1") + ") is not exists in the metadatacard for the 1.1 level hierarchy.";

			//Step-3.2: Checks the level 1.1 behavior in the metadatacard
			//------------------------------------------------------------
			if (!metadataCard.isRequiredProperty(dataPool.get("Property2")))
				ExpectedMetadata += " Property("+ dataPool.get("Property2") + ") is not a required property in the metadatacard for the 1.1 level hierarchy.";

			//Step-4 : Sets the level 1.1.1 hierarchy rule behavior in the metadatacard
			//--------------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("PropName2"), dataPool.get("PropName2Value1"));

			driver.switchTo().defaultContent();

			if (MFilesDialog.exists(driver, Caption.MFilesDialog.ConfirmAutoFill.Value)) {//Checks the autofill dialog is displayed in the view
				MFilesDialog mfilesDialog = new MFilesDialog(driver);
				mfilesDialog.clickCancelButton();//Clicks the cancel button in the MFilesDialog
			}

			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			if (!metadataCard.getPropertyValue(dataPool.get("PropName2")).equalsIgnoreCase(dataPool.get("PropName2Value1")))
				throw new Exception("Property("+dataPool.get("PropName2")+") is not set with value("+dataPool.get("PropName2Value1")+") in the metadatacard for level 1.1.1 Hierarchy rule configuration");

			Log.message("4. Property("+dataPool.get("PropName2")+") is set with value("+dataPool.get("PropName2Value1")+") in the metadatacard for level 1.1.1 Hierarchy rule configuration ", driver);

			//Step-4.1: Checks the level 1.1 behavior in the metadatacard
			//------------------------------------------------------------
			if (metadataCard.propertyExists(dataPool.get("Property1")))
				ExpectedMetadata += " Property("+ dataPool.get("Property1") + ") is exists in the metadatacard for the 1.1.1 level hierarchy.";

			//Step-5 : Sets the level 1.1.2 hierarchy rule behavior in the metadatacard
			//--------------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("PropName2"), dataPool.get("PropName2Value2"));

			driver.switchTo().defaultContent();

			if (MFilesDialog.exists(driver, Caption.MFilesDialog.ConfirmAutoFill.Value)) {//Checks the autofill dialog is displayed in the view
				MFilesDialog mfilesDialog = new MFilesDialog(driver);
				mfilesDialog.clickCancelButton();//Clicks the cancel button in the MFilesDialog
			}

			metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			if (!metadataCard.getPropertyValue(dataPool.get("PropName2")).equalsIgnoreCase(dataPool.get("PropName2Value2")))
				throw new Exception("Property("+dataPool.get("PropName2")+") is not set with value("+dataPool.get("PropName2Value2")+") in the metadatacard for level 1.1.2 Hierarchy rule configuration");

			Log.message("5. Property("+dataPool.get("PropName2")+") is set with value("+dataPool.get("PropName2Value2")+") in the metadatacard for level 1.1.2 Hierarchy rule configuration");

			//Step-5.1: Checks the level 1.1.2 behavior in the metadatacard
			//------------------------------------------------------------
			if (metadataCard.isRequiredProperty(dataPool.get("Property2")))
				ExpectedMetadata += " Property("+ dataPool.get("Property2") + ") is set as required property in the metadatacard for the 1.1.2 level hierarchy.";


			//Verifies if hierarchy rules applied correctly in the metadatacard
			//-----------------------------------------------------------------
			if (ExpectedMetadata.equals(""))
				Log.pass("Test Case Passed. Metadata card functionality with Hierarchical rules, more than one rule in same level [New Object creation] is working as expected", driver);
			else
				Log.fail("Test Case Failed. Metadata card functionality with Hierarchical rules, more than one rule in same level [New Object creation] is not working as expected[For more details: " + ExpectedMetadata + "]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End 	TC_1_4_3A

	/**
	 * 1.4.3B : Hierarchical rules, more than one rule in same level [Existing object] 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"FN119_MetadataConfigurability","Bug"},description = "Verify the Metadata card functionality with Hierarchical rules, more than one rule in same level [Existing object] " )
	public void TC_1_4_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; //Webdriver instance

		try {



			driver = WebDriverUtils.getDriver();

			//Launch the MFWA with valid credentials
			//--------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in
			String ExpectedMetadata = "";

			//Step-1 : Navigate to specified view
			//-----------------------------------
			String viewToNavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("NavigateToView"), "");

			Log.message("1. Navigated to : " + viewToNavigate + " view.");

			//Step-2 : Select any existing object
			//------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("Object")))//Select the specified object
				throw new SkipException(dataPool.get("Object") + " is not selected in the view");

			Log.message("2. Selected the specified object : " + dataPool.get("Object") + " in list view.");

			//Step-2.1: Cheks the first level behavior in the metadatacard
			//------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (metadataCard.propertyExists(dataPool.get("Property1")))
				ExpectedMetadata += "Property("+ dataPool.get("Property1") + ") is exists in the metadatacard for the first level hierarchy.";

			//Step-3: Sets the level 1.1 hierarchy behavior in the metadatacard
			//--------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("PropName1"), dataPool.get("PropName1Value2"));//Sets the class in the metadatacard

			if (!metadataCard.getPropertyValue(dataPool.get("PropName1")).equalsIgnoreCase(dataPool.get("PropName1Value2")))
				throw new Exception("Property("+dataPool.get("PropName1")+") is not set with value("+dataPool.get("PropName1Value2")+") in the metadatacard for the level 1.1 hierarchy behavior");

			Log.message("3. Property("+dataPool.get("PropName1")+") is set with value("+dataPool.get("PropName1Value2")+") in the metadatacard for the level 1.1 hierarchy behavior", driver);

			//Step-3.1: Checks the level 1.1 behavior in the metadatacard
			//------------------------------------------------------------
			if (!metadataCard.propertyExists(dataPool.get("Property1")))
				ExpectedMetadata += " Property("+ dataPool.get("Property1") + ") is not exists in the metadatacard for the 1.1 level hierarchy.";

			//Step-3.2: Checks the level 1.1 behavior in the metadatacard
			//------------------------------------------------------------
			if (!metadataCard.isRequiredProperty(dataPool.get("Property2")))
				ExpectedMetadata += " Property("+ dataPool.get("Property2") + ") is not a required property in the metadatacard for the 1.1 level hierarchy.";

			//Step-4 : Sets the level 1.1.1 hierarchy rule behavior in the metadatacard
			//--------------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("PropName2"), dataPool.get("PropName2Value1"));

			driver.switchTo().defaultContent();

			if (MFilesDialog.exists(driver, Caption.MFilesDialog.ConfirmAutoFill.Value)) {//Checks the autofill dialog is displayed in the view
				MFilesDialog mfilesDialog = new MFilesDialog(driver);
				mfilesDialog.clickCancelButton();//Clicks the cancel button in the MFilesDialog
			}

			metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (!metadataCard.getPropertyValue(dataPool.get("PropName2")).equalsIgnoreCase(dataPool.get("PropName2Value1")))
				throw new Exception("Property("+dataPool.get("PropName2")+") is not set with value("+dataPool.get("PropName2Value1")+") in the metadatacard for level 1.1.1 Hierarchy rule configuration");

			Log.message("4. Property("+dataPool.get("PropName2")+") is set with value("+dataPool.get("PropName2Value1")+") in the metadatacard for level 1.1.1 Hierarchy rule configuration", driver);

			//Step-4.1: Checks the level 1.1 behavior in the metadatacard
			//------------------------------------------------------------
			if (metadataCard.propertyExists(dataPool.get("Property1")))
				ExpectedMetadata += " Property("+ dataPool.get("Property1") + ") is exists in the metadatacard for the 1.1.1 level hierarchy.";

			//Step-5 : Sets the level 1.1.2 hierarchy rule behavior in the metadatacard
			//--------------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("PropName2"), dataPool.get("PropName2Value2"));

			driver.switchTo().defaultContent();

			if (MFilesDialog.exists(driver, Caption.MFilesDialog.ConfirmAutoFill.Value)) {//Checks the autofill dialog is displayed in the view
				MFilesDialog mfilesDialog = new MFilesDialog(driver);
				mfilesDialog.clickCancelButton();//Clicks the cancel button in the MFilesDialog
			}

			metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard

			if (!metadataCard.getPropertyValue(dataPool.get("PropName2")).equalsIgnoreCase(dataPool.get("PropName2Value2")))
				throw new Exception("Property("+dataPool.get("PropName2")+") is not set with value("+dataPool.get("PropName2Value2")+") in the metadatacard for level 1.1.2 Hierarchy rule configuration");

			Log.message("5. Property("+dataPool.get("PropName2")+") is set with value("+dataPool.get("PropName2Value2")+") in the metadatacard for level 1.1.2 Hierarchy rule configuration");

			//Step-5.1: Checks the level 1.1.2 behavior in the metadatacard
			//------------------------------------------------------------
			if (metadataCard.isRequiredProperty(dataPool.get("Property2")))
				ExpectedMetadata += " Property("+ dataPool.get("Property2") + ") is set as required property in the metadatacard for the 1.1.2 level hierarchy.";

			//Verifies if hierarchy rules applied correctly in the metadatacard
			//-----------------------------------------------------------------
			if (ExpectedMetadata.equals(""))
				Log.pass("Test Case Passed. Metadata card functionality with Hierarchical rules, more than one rule in same level [Existing object] is working as expected", driver);
			else
				Log.fail("Test Case Failed. Metadata card functionality with Hierarchical rules, more than one rule in same level [Existing object] is not working as expected[For more details: " + ExpectedMetadata + "]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End 	TC_1_4_3B


}

package MFClient.Tests.MetadataConfigurability;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.WebDriverUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;

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
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;


@Listeners(EmailReport.class)

public class MetadataConfigDefaultValue {

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

	/*
	 * 105.12.1.1A: Verify if default value is displayed for the defined properties based on the class configuration in metadatacard using task pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint105"}, 
			description = "Default value should be set for the defined property of the defined class in new object metadatacard using task pane")
	public void SprintTest105_12_1_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1:- Click on New object link from task pane
			//---------------------------------------------------------

			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));//Clicks the new object link from taskpane

			Log.message("1. New "+ dataPool.get("ObjectType") + " object link is clicked from task pane", driver);

			//Step-2:- Select the template in the template selector
			//-----------------------------------------------------

			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate Metadatacard
			metadatacard.setTemplate(dataPool.get("Template"));//Sets the template

			Log.message("2. Selected the template", driver);

			//Step-3:- Select the class in the metadata card
			//----------------------------------------------

			String property= dataPool.get("Properties");
			String className= property.split("::")[1];

			metadatacard = new MetadataCard(driver);

			metadatacard.setPropertyValue(property.split("::")[0], property.split("::")[1]);//Sets the class value in the metadatacard

			if (!metadatacard.getPropertyValue("Class").equalsIgnoreCase(className))//Checks the correct class is selected
				throw new Exception("Defined class is not selected");

			Log.message("3. Class (" + className + ") is selected in the metadata card", driver);

			//Verification: Verify if default value is displayed for the defined property in the metadatacard
			//------------------------------------------------------------------------------------------------

			if (metadatacard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("ExpectedValue")))//Compares the expected value with property value in metadatacard
				Log.pass("Test case Passed. Expected default value (" +dataPool.get("ExpectedValue") + ") is displayed in the defined(" +dataPool.get("Property") + ") property for the defined(" + className + ") class in the metadatacard ", driver);
			else
				Log.fail("Test case Failed. Expected default value (" +dataPool.get("ExpectedValue") + ") is not displayed in the defined(" +dataPool.get("Property") + ") property for the defined(" + className + ") class in the metadatacard ", driver);


		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End Catch

		finally {
			Utility.quitDriver(driver);
		}//End Finally

	}// End SprintTest105_12_1_1A

	/*
	 * 105.12.1.1B: Verify if default value is displayed for the defined properties based on the class configuration in metadatacard using New menu bar
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint105"}, 
			description = "Default value should be set for the defined property of the defined class in new object metadatacard using new menu bar")
	public void SprintTest105_12_1_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1:- Click on New object link from task pane
			//---------------------------------------------------------

			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//Clicks the new object link from New Menu bar

			Log.message("1. New "+ dataPool.get("ObjectType") + " object link is clicked from New Menu bar", driver);

			//Step-2:- Select the template in the template selector
			//-----------------------------------------------------

			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate Metadatacard
			metadatacard.setTemplate(dataPool.get("Template"));//Sets the template

			Log.message("2. Selected the template", driver);

			//Step-3:- Select the class in the metadata card
			//----------------------------------------------

			String property= dataPool.get("Properties");
			String className= property.split("::")[1];

			metadatacard = new MetadataCard(driver);

			metadatacard.setPropertyValue(property.split("::")[0], property.split("::")[1]);//Sets the class value in the metadatacard

			if (!metadatacard.getPropertyValue("Class").equalsIgnoreCase(className))//Checks the correct class is selected
				throw new Exception("Defined class is not selected");

			Log.message("3. Class (" + className + ") is selected in the metadata card", driver);

			//Verification: Verify if default value is displayed for the defined property in the metadatacard
			//------------------------------------------------------------------------------------------------

			if (metadatacard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("ExpectedValue")))//Compares the expected value with property value in metadatacard
				Log.pass("Test case Passed. Expected default value (" +dataPool.get("ExpectedValue") + ") is displayed in the defined(" +dataPool.get("Property") + ") property for the defined(" + className + ") class in the metadatacard ", driver);
			else
				Log.fail("Test case Failed. Expected default value (" +dataPool.get("ExpectedValue") + ") is not displayed in the defined(" +dataPool.get("Property") + ") property for the defined(" + className + ") class in the metadatacard ", driver);


		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End Catch

		finally {
			Utility.quitDriver(driver);
		}//End Finally

	}// End SprintTest105_12_1_1B

	/*
	 * 105.12.1.2A: Verify if default value is not displayed for the defined properties for the un-defined class configuration in metadatacard using task pane 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint105"}, 
			description = "Default value should not be set for the defined properties for the un-defined class in metadatacard")
	public void SprintTest105_12_1_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1:- Click on New object link from task pane
			//---------------------------------------------------------

			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));//Clicks the new object link from taskpane

			Log.message("1. New "+ dataPool.get("ObjectType") + " object link is clicked from task pane", driver);

			//Step-2:- Select the template in the template selector
			//-----------------------------------------------------

			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate Metadatacard
			metadatacard.setTemplate(dataPool.get("Template"));//Sets the template

			Log.message("2. Selected the template", driver);

			//Step-3:- Select the un-defined class in the metadatacard
			//----------------------------------------------

			String property= dataPool.get("Properties");
			String className= property.split("::")[1];

			metadatacard = new MetadataCard(driver);
			metadatacard.setPropertyValue(property.split("::")[0], property.split("::")[1]);//Sets the class value in the metadatacard

			if (!metadatacard.getPropertyValue("Class").equalsIgnoreCase(className))//Checks the correct class is selected
				throw new Exception("Defined class is not selected");

			Log.message("3. Class (" + className + ") is selected in the metadata card", driver);

			//Verification: Verify if default value is not displayed for the un-defined property in the metadatacard using task pane
			//------------------------------------------------------------------------------------------------

			if (metadatacard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(""))//Checks the expected property value is empty for un-defined class
				Log.pass("Test case Passed. Default value is not displayed in the defined(" +dataPool.get("Property") + ") property for the un-defined(" + className + ") class in the metadatacard ", driver);
			else
				Log.fail("Test case Failed. Default value is displayed in the defined(" +dataPool.get("Property") + ") property for the un-defined(" + className + ") class in the metadatacard ", driver);


		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End Catch

		finally {
			Utility.quitDriver(driver);
		}//End Finally

	}// End SprintTest105_12_1_2A

	/*
	 * 105.12.1.2B: Verify if default value is not displayed for the defined properties for the un-defined class configuration in metadatacard using new menu bar 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint105"}, 
			description = "Default value should not be set for the defined properties for the un-defined class in metadatacard using new menu bar")
	public void SprintTest105_12_1_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1:- Click on New object link from new menu bar
			//---------------------------------------------------------

			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//Clicks the new object link from new menu bar 

			Log.message("1. New "+ dataPool.get("ObjectType") + " object link is clicked from task pane", driver);

			//Step-2:- Select the template in the template selector
			//-----------------------------------------------------

			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate Metadatacard
			metadatacard.setTemplate(dataPool.get("Template"));//Sets the template

			Log.message("2. Selected the template", driver);

			//Step-3:- Select the un-defined class in the metadatacard
			//----------------------------------------------

			String property= dataPool.get("Properties");
			String className= property.split("::")[1];

			metadatacard = new MetadataCard(driver);
			metadatacard.setPropertyValue(property.split("::")[0], property.split("::")[1]);//Sets the class value in the metadatacard

			if (!metadatacard.getPropertyValue("Class").equalsIgnoreCase(className))//Checks the correct class is selected
				throw new Exception("Expected class(" + className + ") is not selected");

			Log.message("3. Class (" + className + ") is selected in the metadata card", driver);

			//Verification: Verify if default value is not displayed for the un-defined property in the metadatacard using task pane
			//------------------------------------------------------------------------------------------------

			if (metadatacard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(""))//Checks the expected property value is empty for un-defined class
				Log.pass("Test case Passed. Default value is not displayed in the defined(" +dataPool.get("Property") + ") property for the un-defined(" + className + ") class in the metadatacard ", driver);
			else
				Log.fail("Test case Failed. Default value is displayed in the defined(" +dataPool.get("Property") + ") property for the un-defined(" + className + ") class in the metadatacard ", driver);


		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End Catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally

	}// End SprintTest105_12_1_2B

	/*
	 * 105.12.2.1A: Verify if default value is displayed for the defined property based on the object type in new metadata card using task pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint105"}, 
			description = "Default value should be displayed for the defined properties for the defined object type in metadatacard using task pane")
	public void SprintTest105_12_2_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1:- Click on New object link from task pane
			//----------------------------------------------------------

			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));//Clicks the new object link from task pane

			Log.message("1. New "+ dataPool.get("ObjectType") + " object link is clicked from task pane", driver);

			//Verification: Verify if default value is displayed for the defined property in the metadatacard using task pane
			//-----------------------------------------------------------------------------------------------------------------------

			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate Metadatacard

			if (metadatacard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("ExpectedValue")))//Checks the expected property value is displayed for defined objecttype
				Log.pass("Test case Passed. Expected default value(" +dataPool.get("ExpectedValue") + ") is displayed in the defined(" +dataPool.get("Property") + ") property for the defined(" + dataPool.get("ObjectType") + ") object type in the metadatacard using task pane", driver);
			else
				Log.fail("Test case Failed. Expected default value(" +dataPool.get("ExpectedValue") + ") is not displayed in the defined(" +dataPool.get("Property") + ") property for the defined(" + dataPool.get("ObjectType") + ") object type in the metadatacard using task pane ", driver);

		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally
	}//End 105.12.2.1A

	/*
	 * 105.12.2.1B: Verify if default value is displayed for the defined property based on the object type in new metadata card using new item menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint105"}, 
			description = "Default value should be displayed for the defined properties for the defined object type in metadatacard using new item menu")
	public void SprintTest105_12_2_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1:- Click on New object link from new menu bar
			//-----------------------------------------------------------

			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//Clicks the new object link from new menu bar

			Log.message("1. New "+ dataPool.get("ObjectType") + " object link is clicked from task pane", driver);

			//Verification: Verify if default value is displayed for the defined property in the metadatacard using new item menu
			//-----------------------------------------------------------------------------------------------------------------------

			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate Metadatacard

			if (metadatacard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("ExpectedValue")))//Checks the expected property value is displayed for defined objecttype
				Log.pass("Test case Passed. Expected default value(" +dataPool.get("ExpectedValue") + ") is displayed in the defined(" +dataPool.get("Property") + ") property for the defined(" + dataPool.get("ObjectType") + ") object type in the metadatacard using new menu bar ", driver);
			else
				Log.fail("Test case Failed. Expected default value(" +dataPool.get("ExpectedValue") + ") is not displayed in the defined(" +dataPool.get("Property") + ") property for the defined(" + dataPool.get("ObjectType") + ") object type in the metadatacard using new menu bar", driver);

		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally
	}//End 105.12.2.1B

	/*
	 * 105.12.2.2A: Verify if default value is not displayed for the defined property for un-defined object type in new metadata card using task pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint105"}, 
			description = "Default value should not be displayed for the defined properties for the un-defined object type in metadatacard using task pane")
	public void SprintTest105_12_2_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1:- Click on New object link from task pane
			//----------------------------------------------------------

			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));//Clicks the new object link from task pane

			Log.message("1. New "+ dataPool.get("ObjectType") + " object link is clicked from task pane", driver);

			//Verification: Verify if default value is not displayed for the defined property for the un-defined object in the metadatacard using task pane
			//----------------------------------------------------------------------------------------------------------------------------------------------

			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate Metadatacard

			if (metadatacard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(""))//Checks the expected property value is empty for un-defined objecttype
				Log.pass("Test case Passed. Default value is not displayed in the defined(" +dataPool.get("Property") + ") property for the un-defined(" + dataPool.get("ObjectType") + ") object type in the metadatacard using task pane", driver);
			else
				Log.fail("Test case Failed. Default value is displayed in the defined(" +dataPool.get("Property") + ") property for the un-defined(" + dataPool.get("ObjectType") + ") object type in the metadatacard using task pane ", driver);

		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally
	}//End 105.12.2.2A

	/*
	 * 105.12.2.2B: Verify if default value is not displayed for the defined property for the un-defined object type in new metadatacard using new item menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint105"}, 
			description = "Default value should not be displayed for the defined properties for the un-defined object type in metadatacard using new item menu")
	public void SprintTest105_12_2_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1:- Click on New object link from new menu bar
			//-----------------------------------------------------------

			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//Clicks the new object link from new menu bar

			Log.message("1. New "+ dataPool.get("ObjectType") + " object link is clicked from task pane", driver);

			//Verification: Verify if default value is displayed for the defined property in the metadatacard using new item menu
			//-----------------------------------------------------------------------------------------------------------------------

			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate Metadatacard

			if (metadatacard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(""))//Checks the expected property value is empty for un-defined objecttype
				Log.pass("Test case Passed. Default value is not displayed in the defined(" +dataPool.get("Property") + ") property for the un-defined(" + dataPool.get("ObjectType") + ") object type in the metadatacard using new menu bar ", driver);
			else
				Log.fail("Test case Failed. Empty default value is not displayed in the defined(" +dataPool.get("Property") + ") property for the defined(" + dataPool.get("ObjectType") + ") object type in the metadatacard using new menu bar", driver);

		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally
	}//End 105.12.2.2B

	/*
	 * 105.12.3.1A: Verify if default value is displayed while adding the defined properties based on the class configuration in metadatacard using task pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint105"}, 
			description = "Default value should be set for the defined property of the defined class while adding the defined property in new metadatacard using task pane")
	public void SprintTest105_12_3_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1:- Click on New object link from task pane
			//---------------------------------------------------------

			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));//Clicks the new object link from taskpane

			Log.message("1. New "+ dataPool.get("ObjectType") + " object link is clicked from task pane", driver);

			//Step-2:- Select the template in the template selector
			//-----------------------------------------------------

			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate Metadatacard
			metadatacard.setTemplate(dataPool.get("Template"));//Sets the template

			Log.message("2. Selected the template", driver);

			//Step-3:- Select the class in the metadata card
			//----------------------------------------------

			String property= dataPool.get("Properties");
			String className= property.split("::")[1];

			metadatacard = new MetadataCard(driver);
			metadatacard.setPropertyValue(property.split("::")[0], property.split("::")[1]);//Sets the class value in the metadatacard


			if (!metadatacard.getPropertyValue("Class").equalsIgnoreCase(className))//Checks the correct class is selected
				throw new Exception("Defined class is not selected");

			metadatacard.addNewProperty(dataPool.get("Property"));//Adds the defined property in the metadatacard


			if (!metadatacard.propertyExists(dataPool.get("Property")))// Checks the defined property is added to the metadatacard
				throw new Exception("Defined property is not added");

			Log.message("3. Defined Class (" + className + ") is selected and Defined Property (" + dataPool.get("Property") + ")  is added in the metadata card", driver);

			//Verification: Verify if default value is displayed for the added defined property in the metadatacard
			//------------------------------------------------------------------------------------------------

			if (metadatacard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("ExpectedValue")))//Compares the expected value with property value in metadatacard
				Log.pass("Test case Passed. Expected default value (" +dataPool.get("ExpectedValue") + ") is displayed in the added defined(" +dataPool.get("Property") + ") property for the defined(" + className + ") class in the metadatacard ", driver);
			else
				Log.fail("Test case Failed. Expected default value (" +dataPool.get("ExpectedValue") + ") is not displayed in the added defined(" +dataPool.get("Property") + ") property for the defined(" + className + ") class in the metadatacard ", driver);


		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End Catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally

	}// End SprintTest105_12_3_1A

	/*
	 * 105.12.3.1B: Verify if default value is displayed while adding the defined properties based on the class configuration in metadatacard using new item menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint105"}, 
			description = "Default value should be set for the defined property of the defined class while adding the defined property in new metadatacard using new item menu")
	public void SprintTest105_12_3_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1:- Click on New object link from new item menu
			//---------------------------------------------------------

			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//Clicks the new object link from New Menu bar

			Log.message("1. New "+ dataPool.get("ObjectType") + " object link is clicked from Mew menu bar", driver);

			//Step-2:- Select the template in the template selector
			//-----------------------------------------------------

			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate Metadatacard
			metadatacard.setTemplate(dataPool.get("Template"));//Sets the template

			Log.message("2. Selected the template", driver);

			//Step-3:- Select the class in the metadata card
			//----------------------------------------------

			String property= dataPool.get("Properties");
			String className= property.split("::")[1];

			metadatacard = new MetadataCard(driver);
			metadatacard.setPropertyValue(property.split("::")[0], property.split("::")[1]);//Sets the class value in the metadatacard


			if (!metadatacard.getPropertyValue("Class").equalsIgnoreCase(className))//Checks the correct class is selected
				throw new Exception("Defined class is not selected");

			metadatacard.addNewProperty(dataPool.get("Property"));//Adds the defined property in the metadatacard


			if (!metadatacard.propertyExists(dataPool.get("Property")))// Checks the defined property is added to the metadatacard
				throw new Exception("Defined property is not added");

			Log.message("3. Defined Class (" + className + ") is selected and Defined Property (" + dataPool.get("Property") + ")  is added in the metadata card", driver);

			//Verification: Verify if default value is displayed for the added defined property in the metadatacard
			//------------------------------------------------------------------------------------------------

			if (metadatacard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("ExpectedValue")))//Compares the expected value with property value in metadatacard
				Log.pass("Test case Passed. Expected default value (" +dataPool.get("ExpectedValue") + ") is displayed in the added defined(" +dataPool.get("Property") + ") property for the defined(" + className + ") class in the metadatacard ", driver);
			else
				Log.fail("Test case Failed. Expected default value (" +dataPool.get("ExpectedValue") + ") is not displayed in the added defined(" +dataPool.get("Property") + ") property for the defined(" + className + ") class in the metadatacard ", driver);


		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End Catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally

	}// End SprintTest105_12_3_1B

	/*
	 * 105.12.3.2A: Verify if default value is not displayed while adding the defined properties for the different class in metadatacard using task pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint105"}, 
			description = "Default value should not be set for the defined property for un-defined class while adding the defined property in new metadatacard using task pane")
	public void SprintTest105_12_3_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1:- Click on New object link from task pane
			//---------------------------------------------------------

			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));//Clicks the new object link from taskpane

			Log.message("1. New "+ dataPool.get("ObjectType") + " object link is clicked from task pane", driver);

			//Step-2:- Select the template in the template selector
			//-----------------------------------------------------

			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate Metadatacard
			metadatacard.setTemplate(dataPool.get("Template"));//Sets the template

			Log.message("2. Selected the template", driver);

			//Step-3:- Select the class in the metadata card
			//----------------------------------------------

			String property= dataPool.get("Properties");
			String className= property.split("::")[1];

			metadatacard = new MetadataCard(driver);
			metadatacard.setPropertyValue(property.split("::")[0], property.split("::")[1]);//Sets the class value in the metadatacard


			if (!metadatacard.getPropertyValue("Class").equalsIgnoreCase(className))//Checks the correct class is selected
				throw new Exception("Defined class is not selected");

			metadatacard.addNewProperty(dataPool.get("Property"));//Adds the defined property in the metadatacard


			if (!metadatacard.propertyExists(dataPool.get("Property")))// Checks the defined property is added to the metadatacard
				throw new Exception("Defined property is not added");

			Log.message("3. Defined Class (" + className + ") is selected and Defined Property (" + dataPool.get("Property") + ")  is added in the metadata card", driver);

			//Verification: Verify if default value is displayed for the added defined property in the metadatacard
			//------------------------------------------------------------------------------------------------

			if (metadatacard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(""))//Checks the expected value is empty in metadatacard for the different class
				Log.pass("Test case Passed. Default value is not displayed in the added defined(" +dataPool.get("Property") + ") property for the un-defined(" + className + ") class in the metadatacard ", driver);
			else
				Log.fail("Test case Failed. Empty Default value is not displayed in the added defined(" +dataPool.get("Property") + ") property for the un-defined(" + className + ") class in the metadatacard ", driver);


		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End Catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally

	}// End SprintTest105_12_3_2A

	/*
	 * 105.12.3.2B: Verify if default value is not displayed while adding the defined properties for the un-defined class in metadatacard using new item menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint105"}, 
			description = "Default value should not be set for the defined property for the un-defined class while adding the defined property in new metadatacard using new item menu")
	public void SprintTest105_12_3_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1:- Click on New object link from new item menu
			//---------------------------------------------------------

			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//Clicks the new object link from New Menu bar

			Log.message("1. New "+ dataPool.get("ObjectType") + " object link is clicked from Mew menu bar", driver);

			//Step-2:- Select the template in the template selector
			//-----------------------------------------------------

			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate Metadatacard
			metadatacard.setTemplate(dataPool.get("Template"));//Sets the template

			Log.message("2. Selected the template", driver);

			//Step-3:- Select the class in the metadata card
			//----------------------------------------------

			String property= dataPool.get("Properties");
			String className= property.split("::")[1];
			metadatacard = new MetadataCard(driver);	  		
			metadatacard.setPropertyValue(property.split("::")[0], property.split("::")[1]);//Sets the class value in the metadatacard


			if (!metadatacard.getPropertyValue("Class").equalsIgnoreCase(className))//Checks the correct class is selected
				throw new Exception("Defined class is not selected");

			metadatacard.addNewProperty(dataPool.get("Property"));//Adds the defined property in the metadatacard


			if (!metadatacard.propertyExists(dataPool.get("Property")))// Checks the defined property is added to the metadatacard
				throw new Exception("Defined property is not added");

			Log.message("3. Defined Class (" + className + ") is selected and Defined Property (" + dataPool.get("Property") + ")  is added in the metadata card", driver);

			//Verification: Verify if default value is displayed for the added defined property in the metadatacard
			//------------------------------------------------------------------------------------------------

			if (metadatacard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(""))//Checks the expected value is empty in metadatacard for the different class
				Log.pass("Test case Passed. Default value (" +dataPool.get("ExpectedValue") + ") is not displayed in the added defined(" +dataPool.get("Property") + ") property for the un-defined(" + className + ") class in the metadatacard ", driver);
			else
				Log.fail("Test case Failed. Default value (" +dataPool.get("ExpectedValue") + ") is displayed in the added defined(" +dataPool.get("Property") + ") property for the un-defined(" + className + ") class in the metadatacard ", driver);


		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End Catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally

	}// End SprintTest105_12_3_2B

	/*
	 * 105.12.4.1A: Verify if default value is displayed while adding the defined property in new metadata card based on the object type configuration using task pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint105"}, 
			description = "Default value should be displayed for the defined properties for the defined object type while adding the defined property in metadatacard using task pane")
	public void SprintTest105_12_4_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1:- Click on New object link from task pane
			//----------------------------------------------------------

			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));//Clicks the new object link from task pane

			Log.message("1. New "+ dataPool.get("ObjectType") + " object link is clicked from task pane" , driver);

			//Step-2: Add the defined property in the metadata card

			MetadataCard metadatacard =new MetadataCard(driver);//Instantiate Metadatacard

			if (!metadatacard.addNewProperty(dataPool.get("Property")))//Adds the defined property in the metadatacard
				throw new Exception("Defined property is not added");

			Log.message("2. Defined(" + dataPool.get("Property") + ") property is added into the defined(" + dataPool.get("ObjectType") + ") object type metadatacard ", driver);

			//Verification: Verify if default value is displayed for the defined property in the metadatacard using task pane
			//-----------------------------------------------------------------------------------------------------------------------
			String result = metadatacard.getPropertyValue(dataPool.get("Property"));

			if (result.contains(dataPool.get("ExpectedValue1")) && result.contains(dataPool.get("ExpectedValue2")))//Checks the expected property value is displayed for defined objecttype
				Log.pass("Test case Passed. Expected default value(" + dataPool.get("ExpectedValue1") + ", "+dataPool.get("ExpectedValue2")+") is displayed in the defined(" +dataPool.get("Property") + ") property for the defined(" + dataPool.get("ObjectType") + ") object type in the metadatacard using task pane.", driver);
			else
				Log.fail("Test case Failed. Expected default value(" + dataPool.get("ExpectedValue1") + ", "+dataPool.get("ExpectedValue2")+") is not displayed in the defined(" +dataPool.get("Property") + ") property for the defined(" + dataPool.get("ObjectType") + ") object type in the metadatacard using task pane.  Actual value displayed in the metadatacard ["+ metadatacard.getPropertyValue(dataPool.get("Property")) +"] ", driver);

		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally
	}//End 105.12.4.1A

	/*
	 * 105.12.4.1B: Verify if default value is displayed while adding the defined property in new metadata card based on the object type configuration using new item menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint105"}, 
			description = "Default value should be displayed for the defined properties for the defined object type while adding the defined property in metadatacard using new item menu")
	public void SprintTest105_12_4_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1:- Click on New object link from new menu bar
			//-----------------------------------------------------------

			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//Clicks the new object link from new menu bar

			Log.message("1. New "+ dataPool.get("ObjectType") + " object link is clicked from task pane", driver);


			//Step-2: Add the defined property in the metadata card

			MetadataCard metadatacard =new MetadataCard(driver);//Instantiate Metadatacard

			if (!metadatacard.addNewProperty(dataPool.get("Property")))//Adds the defined property in the metadatacard
				throw new Exception("Defined property is not added");

			Log.message("2. Defined(" + dataPool.get("Property") + ") property is added into the defined(" + dataPool.get("ObjectType") + ") object type metadatacard ", driver);

			//Verification: Verify if default value is displayed for the defined property in the metadatacard using new menu bar
			//-----------------------------------------------------------------------------------------------------------------------
			String result = metadatacard.getPropertyValue(dataPool.get("Property"));

			if (result.contains(dataPool.get("ExpectedValue1")) && result.contains(dataPool.get("ExpectedValue2")))//Checks the expected property value is displayed for defined objecttype
				Log.pass("Test case Passed. Expected default value(" + dataPool.get("ExpectedValue1") + ", "+dataPool.get("ExpectedValue2")+") is displayed in the defined(" +dataPool.get("Property") + ") property for the defined(" + dataPool.get("ObjectType") + ") object type in the metadatacard using new menu bar", driver);
			else
				Log.fail("Test case Failed. Expected default value(" + dataPool.get("ExpectedValue1") + ", "+dataPool.get("ExpectedValue2")+") is not displayed in the defined(" +dataPool.get("Property") + ") property for the defined(" + dataPool.get("ObjectType") + ") object type in the metadatacard using new menu bar.  Actual value displayed in the metadatacard ["+ metadatacard.getPropertyValue(dataPool.get("Property")) +"]", driver);

		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally
	}//End 105.12.4.1B

	/* 105.12.4.2A: Verify if default value is not displayed while adding the defined property in new metadata card for different object type using task pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint105"}, 
			description = "Default value should not be displayed for the defined properties for the un-defined object type while adding the defined property in metadatacard using task pane")
	public void SprintTest105_12_4_2A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1:- Click on New object link from task pane
			//----------------------------------------------------------

			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));//Clicks the new object link from task pane

			Log.message("1. New "+ dataPool.get("ObjectType") + " object link is clicked from task pane", driver);

			//Step-2: Add the defined property in the metadata card
			//-----------------------------------------------------
			MetadataCard metadatacard = null;

			if(dataPool.get("ObjectType").equalsIgnoreCase("Document"))
			{
				if(!Utility.selectTemplate(dataPool.get("Template"), driver))
					throw new Exception("Template '" + dataPool.get("Template") + "' is not selected.");

				metadatacard = new MetadataCard(driver);//Instantiate Metadatacard
				metadatacard.setInfo(dataPool.get("Class"));
			}
			else
			{
				metadatacard = new MetadataCard(driver);//Instantiate Metadatacard

				metadatacard.addNewProperty(dataPool.get("Property"));//Adds the defined property in the metadatacard
			}

			if (!metadatacard.propertyExists(dataPool.get("Property")))// Checks the defined property is added to the metadatacard
				throw new Exception("Defined property is not added");

			Log.message("2. Defined(" + dataPool.get("Property") + ") property is added into the un-defined(" + dataPool.get("ObjectType") + ") object type metadatacard ", driver);

			//Verification: Verify if default value is displayed for the defined property in the metadatacard using task pane
			//-----------------------------------------------------------------------------------------------------------------------

			if (metadatacard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(""))//Checks the expected property value is displayed for defined objecttype
				Log.pass("Test case Passed. Default value is not displayed in the defined(" +dataPool.get("Property") + ") property for the un-defined(" + dataPool.get("ObjectType") + ") object type in the metadatacard using task pane", driver);
			else
				Log.fail("Test case Failed. Default value is displayed in the defined(" +dataPool.get("Property") + ") property for the un-defined(" + dataPool.get("ObjectType") + ") object type in the metadatacard using task pane", driver);

		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally
	}//End 105.12.4.2A

	/*
	 * 105.12.4.2B: Verify if default value is displayed while adding the defined property in new metadata card based on the object type configuration using new item menu
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint105"}, 
			description = "Default value should not be displayed for the defined properties for the un-defined object type while adding the defined property in metadatacard using new item menu")
	public void SprintTest105_12_4_2B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1:- Click on New object link from new menu bar
			//-----------------------------------------------------------

			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//Clicks the new object link from new menu bar

			Log.message("1. New "+ dataPool.get("ObjectType") + " object link is clicked from task pane", driver);


			//Step-2: Add the defined property in the metadata card

			MetadataCard metadatacard =new MetadataCard(driver);//Instantiate Metadatacard
			metadatacard.setInfo(dataPool.get("Class"));//Sets the class in the metadatacard
			metadatacard.addNewProperty(dataPool.get("Property"));//Adds the defined property in the metadatacard


			if (!metadatacard.propertyExists(dataPool.get("Property")))// Checks the defined property is added to the metadatacard
				throw new Exception("Defined property is not added");

			Log.message("2. Defined(" + dataPool.get("Property") + ") property is added into the un-defined(" + dataPool.get("ObjectType") + ") object type metadatacard ", driver);

			//Verification: Verify if default value is displayed for the defined property in the metadatacard using new menu bar
			//-----------------------------------------------------------------------------------------------------------------------

			if (metadatacard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(""))//Checks the expected property value is displayed for defined objecttype
				Log.pass("Test case Passed. Default value is not displayed in the defined(" +dataPool.get("Property") + ") property for the un-defined(" + dataPool.get("ObjectType") + ") object type in the metadatacard using new menu bar", driver);
			else
				Log.fail("Test case Failed. Default value is not displayed in the defined(" +dataPool.get("Property") + ") property for the un-defined(" + dataPool.get("ObjectType") + ") object type in the metadatacard using new menu bar", driver);

		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally
	}//End 105.12.4.1B

	/*
	 * 105.12.5.1A: Verify if JavaScript is displayed as plain text while using script value as default value for any property in the metadata card using task pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint105"}, 
			description = "Javascript value should be dislayed as the plain text for the defined properties in metadatacard using task pane")
	public void SprintTest105_12_5_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1:- Click on New object link from task pane
			//------------------------------------------------

			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));//Clicks the new object link from task pane

			Log.message("1. New "+ dataPool.get("ObjectType") + " object link is clicked from task pane", driver);

			//Step-2: Add the defined property in the metadata card

			MetadataCard metadatacard =new MetadataCard(driver);//Instantiate Metadatacard
			metadatacard.addNewProperty(dataPool.get("Property"));//Adds the defined property in the metadatacard


			if (!metadatacard.propertyExists(dataPool.get("Property")))// Checks the defined property is added to the metadatacard
				throw new Exception("Defined property is not added");

			Log.message("2. Defined(" + dataPool.get("Property") + ") property is added into the metadatacard ", driver);

			//Verification: Verify if any alert dialog is displayed while adding the (script value) defined property in metadatacard
			//--------------------------------------------------------------------------------------------------------------------

			if (Utility.isAlertPresent(driver))
				Log.fail("Test case failed. Alert dialog displayed with the alert message("+ driver.switchTo().alert().getText() + ") while adding the script value defined property in the metadatacard.", driver);

			//Verification: Verify if default value is displayed for the defined property in the metadatacard using task pane
			//----------------------------------------------------------------------------------------------------------------

			else if (metadatacard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("ExpectedValue")))//Checks the expected property value is displayed
				Log.pass("Test case Passed. Script value is displayed as a plain text in the defined(" +dataPool.get("Property") + ") property in the metadatacard using task pane", driver);
			else
				Log.fail("Test case Failed. Script value is not displayed in the defined(" +dataPool.get("Property") + ") property in the metadatacard using task pane", driver);


		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally
	}//End 105.12.5.1A

	/*
	 * 105.12.5.1B: Verify if JavaScript is displayed as plain text while using script value as default value for any property in the metadata card using new menu bar
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint105"}, 
			description = "Javascript value should be dislayed as the plain text for the defined properties in metadatacard using new item menu")
	public void SprintTest105_12_5_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1:- Click on New object link from task pane
			//------------------------------------------------

			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//Clicks the new object link from new menu bar

			Log.message("1. New "+ dataPool.get("ObjectType") + " object link is clicked from new menu bar", driver);

			//Step-2: Add the defined property in the metadata card

			MetadataCard metadatacard =new MetadataCard(driver);//Instantiate Metadatacard
			metadatacard.addNewProperty(dataPool.get("Property"));//Adds the defined property in the metadatacard


			if (!metadatacard.propertyExists(dataPool.get("Property")))// Checks the defined property is added to the metadatacard
				throw new Exception("Defined property is not added");

			Log.message("2. Defined(" + dataPool.get("Property") + ") property is added into the metadatacard ", driver);

			//Verification: Verify if any alert dialog is displayed while adding the (script value) defined property in metadatacard
			//--------------------------------------------------------------------------------------------------------------------

			if (Utility.isAlertPresent(driver))
				Log.fail("Test case failed. Alert dialog displayed with the alert message("+ driver.switchTo().alert().getText() + ") while adding the script value defined property in the metadatacard.", driver);

			//Verification: Verify if default value is displayed for the defined property in the metadatacard using new menu bar
			//-----------------------------------------------------------------------------------------------------------------------
			else if (metadatacard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("ExpectedValue")))//Checks the expected property value is displayed
				Log.pass("Test case Passed. Script value is displayed as a plain text in the defined(" +dataPool.get("Property") + ") property in the metadatacard using new menu bar", driver);
			else
				Log.fail("Test case Failed. Script value is not displayed in the defined(" +dataPool.get("Property") + ") property in the metadatacard using new menu bar", driver);

		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally
	}//End 105.12.5.1B

	/*
	 * 105.12.6.1A: Verify if Email link is displayed as plain text for the defined property in the metadata card using task pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint105"}, 
			description = "Email link should be displayed as a plain text for the defined property in the metadata card using task pane")
	public void SprintTest105_12_6_1A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1:- Click on New object link from task pane
			//------------------------------------------------

			homePage.taskPanel.clickItem(dataPool.get("ObjectType"));//Clicks the new object link from task pane

			Log.message("1. New "+ dataPool.get("ObjectType") + " object link is clicked from task pane", driver);

			//Step-2: Add the defined property in the metadata card

			MetadataCard metadatacard =new MetadataCard(driver);//Instantiate Metadatacard
			metadatacard.addNewProperty(dataPool.get("Property"));//Adds the defined property in the metadatacard


			if (!metadatacard.propertyExists(dataPool.get("Property")))// Checks the defined property is added to the metadatacard
				throw new Exception("Defined property is not added");

			Log.message("2. Defined(" + dataPool.get("Property") + ") property is added into the metadatacard ", driver);

			//Verification: Verify if default value is displayed for the defined property in the metadatacard using task pane
			//----------------------------------------------------------------------------------------------------------------

			if (metadatacard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("ExpectedValue")))//Checks the expected property value is displayed
				Log.pass("Test case Passed. Defined value(" + dataPool.get("ExpectedValue") + ") is displayed as a plain text in the defined(" +dataPool.get("Property") + ") property in the metadatacard using task pane", driver);
			else
				Log.fail("Test case Failed. Defined value(" + dataPool.get("ExpectedValue") + ") is not displayed in the defined(" +dataPool.get("Property") + ") property in the metadatacard using task pane", driver);


		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally
	}//End 105.12.6.1A

	/*
	 * 105.12.6.1B: Verify if Email link is displayed as plain text for the defined property in the metadata card using new menu bar
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint105"}, 
			description = "Email link should be displayed as a plain text for the defined property in the metadata card using new menu bar")
	public void SprintTest105_12_6_1B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1:- Click on New object link from task pane
			//------------------------------------------------

			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//Clicks the new object link from new menu bar

			Log.message("1. New "+ dataPool.get("ObjectType") + " object link is clicked from new menu bar", driver);

			//Step-2: Add the defined property in the metadata card

			MetadataCard metadatacard =new MetadataCard(driver);//Instantiate Metadatacard
			metadatacard.addNewProperty(dataPool.get("Property"));//Adds the defined property in the metadatacard


			if (!metadatacard.propertyExists(dataPool.get("Property")))// Checks the defined property is added to the metadatacard
				throw new Exception("Defined property is not added");

			Log.message("2. Defined(" + dataPool.get("Property") + ") property is added into the metadatacard ", driver);

			//Verification: Verify if default value is displayed for the defined property in the metadatacard using new menu bar
			//-----------------------------------------------------------------------------------------------------------------------
			if (metadatacard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("ExpectedValue")))//Checks the expected property value is displayed
				Log.pass("Test case Passed. Email link(" + dataPool.get("ExpectedValue") + ") is displayed as a plain text in the defined(" +dataPool.get("Property") + ") property in the metadatacard using new menu bar", driver);
			else
				Log.fail("Test case Failed. Email link(" + dataPool.get("ExpectedValue") + ") is not displayed in the defined(" +dataPool.get("Property") + ") property in the metadatacard using new menu bar", driver);

		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally
	}//End 105.12.6.1B

	/*
	 * 37101: Filling of property triggers automatic filling of another property.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MetadataConfigDefaultValue"}, 
			description = "Filling of property triggers automatic filling of another property.")
	public void TC_37101(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-Requisite: Login to the web access
			//--------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Navigate to the search view
			//-----------------------------------
			String navigateToView = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("viewToNavigate"), "");//Navigates to the search view

			Log.message("1. Navigated to the search view: '" + navigateToView + "'");

			//Step-2: Select the object in the view
			//-------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new Exception("Object - '" + dataPool.get("ObjectName") + "' is not selected in the list view");

			Log.message("2. Object - '" + dataPool.get("ObjectName") + "' is selected in the list view.");

			//Step-3: Set the property values in the metadatacard
			//---------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the right pane metadatacard
			metadataCard.addNewProperty(dataPool.get("Property1"));
			metadataCard.addNewProperty(dataPool.get("Property2"));

			Log.message("3. Required properties('" + dataPool.get("Property1") + "' & '" + dataPool.get("Property2") + "') is added in the metadata card and ");

			//Step-4: Trigger the rule by setting the property value in the metadata card
			//---------------------------------------------------------------------------
			metadataCard.setInfo(dataPool.get("Properties"));//Sets the property values in the metadatacard

			Log.message("4. Property('" + dataPool.get("Properties").split("::")[0] + "') is set with the value('" + dataPool.get("Properties").split("::")[1] + "') in the metadata card");

			//Verification: Verify if added properties is set with the defined values in the metadata card
			//--------------------------------------------------------------------------------------------
			String result = "";

			if(!metadataCard.getPropertyValue(dataPool.get("Property1")).equalsIgnoreCase(dataPool.get("Property1Value")))//Checks if first property fills with the defined value in the metadatacard
				result = "Property('" + dataPool.get("Property1") + "') is not set with the value('" + dataPool.get("Property1Value") + "') in the metadata card;";

			if(!metadataCard.getPropertyValue(dataPool.get("Property2")).equalsIgnoreCase(dataPool.get("Property2Value")))//Checks if second property fills with the defined value in the metadatacard
				result += "Property('" + dataPool.get("Property2") + "') is not set with the value('" + dataPool.get("Property2Value") + "') in the metadata card;";

			if (result.equals(""))
				Log.pass("Test case passed. Filling of property triggers automatic filling of another properties successfully.");
			else
				Log.fail("Test case failed. Filling of property not triggers automatic filling of another properties.[Additional info. : "+ result +"]", driver);

		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally
	}//End TC_37101

	/*
	 * 37259: Modifications of property which has value added by SetValue
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MetadataConfigDefaultValue"}, 
			description = "Modifications of property which has value added by SetValue")
	public void TC_37259(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-Requisite: Login to the web access
			//--------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: New object metadatacard via new menu item
			//-------------------------------------------------
			if (!homePage.taskPanel.clickItem(dataPool.get("ObjectType")))
				throw new Exception("'"+ dataPool.get("ObjectType") +"' is not clicked from task pane.");

			if(!Utility.selectTemplate(dataPool.get("Template"), driver))
				throw new Exception("Template'"+ dataPool.get("Template") +"' is not selected.");

			Log.message("1. New '"+ dataPool.get("ObjectType") +"' object type metadata card is opened from task pane.");

			//Step-2: Select the class and check the defined property value for that class in the metadata card
			//-------------------------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadata card
			metadataCard.setInfo(dataPool.get("Class"));//Sets the class in the metadata card

			if(!metadataCard.getPropertyValue(dataPool.get("Class").split("::")[0]).equalsIgnoreCase(dataPool.get("Class").split("::")[1]))
				throw new Exception("Class is not set with the value '" + dataPool.get("Class").split("::")[1] + "' in the metadatacard.");

			if (!metadataCard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyValue")))
				throw new Exception("Property '" + dataPool.get("Property") + "' is not set with the value '" + dataPool.get("PropertyValue") + "' while selecting the Class '" + dataPool.get("Class").split("::")[1] + "' in the metadatacard.");

			Log.message("2. Property '" + dataPool.get("Property") + "' is set with the value '" + dataPool.get("PropertyValue") + "' while selecting the Class value '" + dataPool.get("Class").split("::")[1] + "' in the metadatacard.", driver);

			//Step-3: Modify the value of property which automatically set while selecting the class
			//--------------------------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Property"), dataPool.get("ModifyPropertyValue"));//Modifies the property value in the metadata card

			if(!metadataCard.getPropertyValue(dataPool.get("Property")).equals(dataPool.get("ModifyPropertyValue")))
				throw new Exception("Property '" + dataPool.get("Property") + "' is not modified with the value '" + dataPool.get("ModifyPropertyValue") + "' in the metadata card.");

			Log.message("3. Property '" + dataPool.get("Property") + "' is modified with the value '" + dataPool.get("ModifyPropertyValue") + "' in the metadata card.");

			//Step-4: Adds the Property which triggers the set value for some other property
			//------------------------------------------------------------------------------
			if(!metadataCard.addNewProperty(dataPool.get("Property1")))//Adds the new property and checks if exists in the metadata card
				throw new Exception("Property '" + dataPool.get("Property1") + "' is not added in the metadata card.");

			if(!metadataCard.getPropertyValue(dataPool.get("Property")).equals(dataPool.get("ModifyPropertyValue")))//Checks if the expected value in the metadata card
				throw new Exception("Property '" + dataPool.get("Property") + "' is modified with the value '" + metadataCard.getPropertyValue(dataPool.get("Property")) + "' while adding the property '" + dataPool.get("Property1") + "'in the metadata card.");

			Log.message("4. Property '" + dataPool.get("Property") + "' is not modified while adding the property '" + dataPool.get("Property1") + "'in the metadata card., driver");

			//Step-5: Modify the value of property which automatically set while selecting the class
			//--------------------------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Property"), dataPool.get("ModifyPropertyValue1"));//Modifies the property value in the metadata card

			if(!metadataCard.getPropertyValue(dataPool.get("Property")).equals(dataPool.get("ModifyPropertyValue1")))//Checks if the expected value in the metadata card
				throw new Exception("Property '" + dataPool.get("Property") + "' is not modified with the value '" + dataPool.get("ModifyPropertyValue1") + "' in the metadata card.");

			Log.message("5. Property '" + dataPool.get("Property") + "' is modified with the value '" + dataPool.get("ModifyPropertyValue1") + "' in the metadata card.");

			//Step-6: Select the value in the Property which triggers the set value for some other property
			//---------------------------------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Property1"), dataPool.get("Property1Value1"));//Sets the property value in the metadata card

			if(!metadataCard.getPropertyValue(dataPool.get("Property1")).equals(dataPool.get("Property1Value1")))//Checks if the expected value in the metadata card
				throw new Exception("Property '" + dataPool.get("Property1") + "' is not set with the value '" + dataPool.get("Property1Value1") + "' in the metadata card.");

			if(!metadataCard.getPropertyValue(dataPool.get("Property")).equals(dataPool.get("ModifyPropertyValue1")))//Checks if the expected value in the metadata card
				throw new Exception("Property '" + dataPool.get("Property") + "' is modified with the value '" + metadataCard.getPropertyValue(dataPool.get("Property1")) + "' while setting the property '" + dataPool.get("Property1") + "' with value '" + dataPool.get("Property1Value1") + "' in the metadata card.");

			Log.message("6. Property '" + dataPool.get("Property") + "' is not modified while adding the property '" + dataPool.get("Property1") + "'in the metadata card., driver");

			//Step-7: Select the value in the Property which triggers the set value for some other property
			//---------------------------------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Property1"), dataPool.get("Property1Value2"));//Sets the property value in the metadata card

			if(!metadataCard.getPropertyValue(dataPool.get("Property1")).equals(dataPool.get("Property1Value2")))//Checks if the expected value in the metadata card
				throw new Exception("Property '" + dataPool.get("Property1") + "' is not set with the value '" + dataPool.get("Property1Value2") + "' in the metadata card.");

			if(!metadataCard.propertyExists(dataPool.get("AdditonalProperty")))//Checks if property is exists in the metadata card
				throw new Exception("Additonal property('" + dataPool.get("AdditonalProperty") + "') is not added in the metadata card while selecting the value '" + dataPool.get("Property1") + "' in the property '" + dataPool.get("Property1Value2") + "'.");

			if(!metadataCard.getPropertyValue(dataPool.get("AdditonalProperty")).equals(dataPool.get("AdditonalPropertyValue")))
				throw new Exception("Property '" + dataPool.get("AdditonalProperty") + "' is not set with the value '" + dataPool.get("AdditonalPropertyValue") + "' while selecting the value '" + dataPool.get("Property1") + "' in the property '" + dataPool.get("Property1Value2") + "' in the metadata card.");

			Log.message("7. Property '" + dataPool.get("AdditonalProperty") + "' is added with the value '" + dataPool.get("AdditonalPropertyValue") + "' while selecting the value '" + dataPool.get("Property1") + "' in the property '" + dataPool.get("Property1Value2") + "' in the metadata card.", driver);

			//Step-8: Modify the value of property which automatically set while selecting the class
			//--------------------------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("AdditonalProperty"), dataPool.get("ModifyAdditonalPropertyValue"));//Modifies the property value in the metadata card

			if(!metadataCard.getPropertyValue(dataPool.get("AdditonalProperty")).equals(dataPool.get("ModifyAdditonalPropertyValue")))//Checks if the expected value in the metadata card
				throw new Exception("Property '" + dataPool.get("AdditonalProperty") + "' is not modified with the value '" + dataPool.get("ModifyAdditonalPropertyValue") + "' in the metadata card.");

			Log.message("8. Property '" + dataPool.get("AdditonalProperty") + "' is modified with the value '" + dataPool.get("ModifyAdditonalPropertyValue") + "' in the metadata card.");

			//Step-9: Change the class value and roll back to the same class in the metadata card
			//-----------------------------------------------------------------------------------
			metadataCard.setInfo(dataPool.get("Class1"));//Changes the class in the metadata card

			if(!metadataCard.getPropertyValue(dataPool.get("Class1").split("::")[0]).equalsIgnoreCase(dataPool.get("Class1").split("::")[1]))
				throw new Exception("Class is not set with the value '" + dataPool.get("Class1").split("::")[1] + "' in the metadatacard.");

			Log.message("9.1. Class is set with the value '" + dataPool.get("Class1").split("::")[1] + "' in the metadata card.");

			metadataCard.setInfo(dataPool.get("Class"));//Sets the class in the metadata card

			if(!metadataCard.getPropertyValue(dataPool.get("Class").split("::")[0]).equalsIgnoreCase(dataPool.get("Class").split("::")[1]))
				throw new Exception("Class is not set with the value '" + dataPool.get("Class").split("::")[1] + "' in the metadatacard.");

			Log.message("9.2. Class is set back to the previous value '" + dataPool.get("Class1").split("::")[1] + "' in the metadata card.");

			//Verification: If Additonaly added property value is modified while changing the value in the metadata card
			//----------------------------------------------------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("AdditonalProperty")).equals(dataPool.get("ModifyAdditonalPropertyValue")))//Checks if the expected value in the metadata card
				Log.pass("Test case passed. Modifications of property which has value added by SetValue not modified while performing modifications of filter conditions.", driver);
			else
				Log.fail("Test case failed. Property '" + dataPool.get("AdditonalProperty") + "' value '" + metadataCard.getPropertyValue(dataPool.get("AdditonalProperty")) + "' modified while changing the class value in the metadata card.", driver);		

		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally
	}//End TC_37259

	/*
	 * 37058: SetValue modifies same property in several rules
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MetadataConfigDefaultValue"}, 
			description = "SetValue modifies same property in several rules.")
	public void TC_37058(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-Requisite: Login to the web access
			//--------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: New object metadatacard via new menu item
			//-------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//Clicks the new menu item in the metadata card

			Log.message("1. New '" + dataPool.get("ObjectType") + "' metadata card is opened via new menu bar.");

			//Verification: Verify is set value is working as expected while isForced is true/false
			//-------------------------------------------------------------------------------------
			String result = "";
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadata card

			if(!metadataCard.getPropertyValue(dataPool.get("Property1")).equalsIgnoreCase(dataPool.get("Property1Value")))
				result = "Property '" + dataPool.get("Property1") + "' is not with the value '" + dataPool.get("Property1Value") + "' in the metadata card even though 'isForced' is set as true.[Actual value : '" + metadataCard.getPropertyValue(dataPool.get("Property1")) + "'];";

			if(!metadataCard.getPropertyValue(dataPool.get("Property2")).equalsIgnoreCase(dataPool.get("Property2Value")))
				result += "Property '" + dataPool.get("Property2") + "' is not with the value '" + dataPool.get("Property2Value") + "' in the metadata card even though 'isForced' is set as false.[Actual value : '" + metadataCard.getPropertyValue(dataPool.get("Property2")) + "'];";

			if(result.equals(""))
				Log.pass("Test case passed. SetValue modifies same property in several rules successfully while isForced: true/false used.");
			else
				Log.fail("Test case failed. SetValue not modifies same property in several rules while isForced: true/false used. Additional info. : + " +result, driver);

		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally
	}//End TC_37058

	/*
	 * 36543 : SetValue sets value right away when property is added
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MetadataConfigDefaultValue"}, 
			description = "SetValue sets value right away when property is added")
	public void TC_36543(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-Requisite: Login to the web access
			//--------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: New object metadatacard via new menu item
			//-------------------------------------------------
			if (!homePage.taskPanel.clickItem(dataPool.get("ObjectType")))
				throw new Exception("'"+ dataPool.get("ObjectType") +"' is not clicked from task pane.");

			if(!Utility.selectTemplate(dataPool.get("Template"), driver))
				throw new Exception("Template'"+ dataPool.get("Template") +"' is not selected.");

			Log.message("1. New '"+ dataPool.get("ObjectType") +"' object type metadata card is opened from task pane.");

			//Step-2: Select the class and check the defined property value for that class in the metadata card
			//-------------------------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadata card
			metadataCard.setInfo(dataPool.get("Class"));//Sets the class in the metadata card

			Log.message("2. Class property is set with the value '" + dataPool.get("Class").split("::")[1] + "' in the metadata card.");

			//Verification: If property values set as expected while selecting the class value in the metadata card
			//-----------------------------------------------------------------------------------------------------
			String[] properties = dataPool.get("Properties").split("\n");
			String result = "";

			for (int i = 0; i < properties.length; i++)
				if(!metadataCard.propertyExists(properties[i].split("::")[0]))
					result += "'" + properties[i].split("::")[0] + "';";

			if (!result.equals(""))
				throw new Exception("Test case failed. Properties not added in the metadata card while selecting the class. Additional info.: The following properties not exists in the metadata card[" + result + "]");

			result = "";

			for (int i = 0; i < properties.length; i++)
				if (!metadataCard.getPropertyValue(properties[i].split("::")[0]).equals(properties[i].split("::")[1]))
					result += "Expected property: '" + properties[i].split("::")[0] + "' & Expected value: '" + properties[i].split("::")[0] + "' [Actual value : '" + metadataCard.getPropertyValue(properties[i].split("::")[0]) + "'];";

			if(result.equals(""))
				Log.pass("Test case passed. SetValue sets value right away when property is set with the required value.");
			else
				Log.fail("Test case failed. SetValue not sets value right away when property is set with the required value. Additonal info. : "+result, driver);

		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally
	}//End TC_36543

	/*
	 * 37309 : SetValue with ""
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MetadataConfigDefaultValue"}, 
			description = "SetValue with \"\"")
	public void TC_37309(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-Requisite: Login to the web access
			//--------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: New object metadatacard via new menu item
			//-------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));

			Log.message("1. New '"+ dataPool.get("ObjectType") +"' object type metadata card is opened from new menu bar.");

			//Verification: If property values set as expected while selecting the class value in the metadata card
			//-----------------------------------------------------------------------------------------------------
			String[] properties = dataPool.get("Properties").split("\n");
			String result = "";

			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadata card

			for (int i = 0; i < properties.length; i++)
				if(!metadataCard.propertyExists(properties[i].split("::")[0]))
					result += "'" + properties[i].split("::")[0] + "';";

			if (!result.equals(""))
				throw new Exception("Test case failed. Properties not added in the metadata card. Additional info.: The following properties not exists in the metadata card[" + result + "]");

			result = "";

			for (int i = 0; i < properties.length; i++)
				if (!metadataCard.getPropertyValue(properties[i].split("::")[0]).equals(properties[i].split("::")[1]))
					result += "Expected property: '" + properties[i].split("::")[0] + "' & Expected value: '" + properties[i].split("::")[1] + "' [Actual value : '" + metadataCard.getPropertyValue(properties[i].split("::")[0]) + "'];";

			if(result.equals(""))
				Log.pass("Test case passed. SetValue with \"\" is working as expected.");
			else
				Log.fail("Test case failed. SetValue with \"\" is not working as expected. Additonal info. : "+result, driver);

		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally
	}//End TC_37309

	/*
	 * 45922 : Check if Country=UK then set Department =Sales with additional true
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MetadataConfigDefaultValue"}, 
			description = "Check if Country=UK then set Department =Sales with additional true")
	public void TC_45922(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-Requisite: Login to the web access
			//--------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: New object metadatacard via new menu item
			//-------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));

			Log.message("1. New '"+ dataPool.get("ObjectType") +"' object type metadata card is opened from new menu bar.");

			//Step-2 : Set the filter condition property value in the metadata card
			//---------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadata card
			metadataCard.setPropertyValue(dataPool.get("Filter").split("::")[0], dataPool.get("Filter").split("::")[1]);//Sets the filter condition in the metadata card

			Log.message("2. Property '" + dataPool.get("Filter").split("::")[0] + "' is set with value '" + dataPool.get("Filter").split("::")[1] + "' in the metadata card");

			//Verification: Check if Property is set with the expected value in the metadata card
			//-----------------------------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("Behavior").split("::")[0]).equalsIgnoreCase(dataPool.get("Behavior").split("::")[1]))
				Log.pass("Test case passed. Check if Country=UK then set Department =Sales with additional true is working as expected.");
			else
				Log.fail("Test case failed. Check if Country=UK then set Department =Sales with additional true is not working as expected. [Additonal info. : Property '" + dataPool.get("Behavior").split("::")[0] + "' is not set with the value '" + dataPool.get("Behavior").split("::")[1] + "']", driver);

		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally
	}//End TC_45922

	/*
	 * 45923 : Check set value works for array object
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MetadataConfigDefaultValue"}, 
			description = "Check set value works for array object")
	public void TC_45923(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-Requisite: Login to the web access
			//--------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: New object metadatacard via new menu item
			//-------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));

			Log.message("1. New '"+ dataPool.get("ObjectType") +"' object type metadata card is opened from new menu bar.");

			//Verification: Check the metadata description and property value
			//---------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadata card
			String result = "";

			//Check if MetadataCard description is set in the metadatacard
			//------------------------------------------------------------
			if(!metadataCard.getMetadataDescriptionText().equalsIgnoreCase(dataPool.get("MetadataCardDescription")))
				result = "Expected MetadataCard description('" + dataPool.get("MetadataCardDescription") + "') is not displayed.Actual value : '" + metadataCard.getMetadataDescriptionText() + "';";

			//Check if Property is exists in the metadata card
			//------------------------------------------------
			if (metadataCard.propertyExists(dataPool.get("AdditonalProperty")))
			{
				//Check if property is required in the metadata card
				//--------------------------------------------------
				if (!metadataCard.isRequiredProperty(dataPool.get("AdditonalProperty")))
					result += "Property '" + dataPool.get("AdditonalProperty") + "' is not marked as requried in the metadata card.;";

				//Check if property is set with the expected value in the metadata card
				//----------------------------------------------------------------------
				if(!metadataCard.getPropertyValue(dataPool.get("AdditonalProperty")).equalsIgnoreCase(dataPool.get("AdditonalPropertyValue")))
					result += "Property '" + dataPool.get("AdditonalProperty") + "' is not set with the value '" + dataPool.get("AdditonalProperty") + "' in the metadatacard;";

			}
			else
				result += "Property '" + dataPool.get("AdditonalProperty") + "' is not added in the metadata card.;";

			//Verification: If Set values works for array object
			//---------------------------------------------------
			if (result.equals(""))
				Log.pass("Test case passed. Set values works for array object as expected.");
			else
				Log.fail("Test case failed. Set values not works for array object. [Additional info. : '" + result + "']", driver);

		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally
	}//End TC_45923

	/*
	 * 31652 : Descriptions and group headers works/not works when rule is true/false
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"MetadataConfigDefaultValue"}, 
			description = "Descriptions, and group headers works/not works when rule is true/false")
	public void TC_31652(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-Requisite: Login to the web access
			//--------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: New object metadatacard via new menu item
			//-------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));

			Log.message("1. New '"+ dataPool.get("ObjectType") +"' object type metadata card is opened from new menu bar.");

			//Step-2 : Set the Filter condition in the metadata card
			//------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard
			metadataCard.setPropertyValue("Class", dataPool.get("Class"));//Sets the class value in the metadata card

			Log.message("2. Class is set with '" + dataPool.get("Class") + "' in the metadata card.");

			//Check if Descriptions and group headers works/not works when rule is true/false
			//-------------------------------------------------------------------------------
			String result = "";
			if(dataPool.get("Enabled").equalsIgnoreCase("TRUE"))
			{
				//Check if MetadataCard description is set in the metadatacard
				//------------------------------------------------------------
				if(!metadataCard.getMetadataDescriptionText().equalsIgnoreCase(dataPool.get("MetadataCardDescription")))
					result = "Expected MetadataCard description('" + dataPool.get("MetadataCardDescription") + "') is not displayed.Actual value : '" + metadataCard.getMetadataDescriptionText() + "';";

				//Check if Property is exists in the metadata card
				//------------------------------------------------
				if (!metadataCard.propertyExists(dataPool.get("AdditonalProperty")))
					result += "Property '" + dataPool.get("AdditonalProperty") + "' is not added in the metadata card.;";

				//Check if group is displayed in the metadata card
				//------------------------------------------------
				if (!metadataCard.isPropertyGroupDisplayed(1, dataPool.get("GroupTitle")))
					result += "Property group with title '" + dataPool.get("GroupTitle") + "' is not displayed in the metadata card;"; 

				//Check if property description is displayed in the metadata card
				//----------------------------------------------------------------
				if (!metadataCard.getPropertyDescriptionValue(dataPool.get("AdditonalProperty")).equalsIgnoreCase(dataPool.get("AdditonalPropertyDescription")))
					result += "Property description '" + dataPool.get("AdditonalPropertyDescription") + "' is not displayed for the property '" + dataPool.get("AdditonalProperty") + "' in the metadata card;"; 
			}
			else
			{
				//Check if MetadataCard description is displayed in the metadatacard
				//------------------------------------------------------------------
				if(metadataCard.metadataDescriptionisDisplayed())
					result = "MetadataCard description is displayed in the metadatacard;";

				//Check if Property is exists in the metadata card
				//------------------------------------------------
				if (metadataCard.propertyExists(dataPool.get("AdditonalProperty")))
					result += "Property '" + dataPool.get("AdditonalProperty") + "' is added in the metadata card.;";

				//Check if group is displayed in the metadata card
				//------------------------------------------------
				if (metadataCard.isPropertyGroupDisplayed(1, dataPool.get("GroupTitle")))
					result += "Property group with the title '" + dataPool.get("GroupTitle") + "' is displayed in the metadata card;"; 
			}

			//Verification: Descriptions and group headers works/not works when rule is true/false
			//---------------------------------------------------
			if (result.equals(""))
				Log.pass("Test case passed. Descriptions and group headers works as expected when rule is '" + dataPool.get("Enabled") + "'.");
			else
				Log.fail("Test case failed. Descriptions and group headers not works as expected when rule is '" + dataPool.get("Enabled") + "'. Additional info. : '" + result + "'", driver);

		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally
	}//End TC_31652

	/**
	 *  40025 :  SetValue usage in several rules
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"SetValue"},description = " SetValue usage in several rules." )
	public void TC_40025(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();//Launches the browser

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-Requisite: Login to the web access
			//--------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: New object metadatacard via new menu item
			//-------------------------------------------------
			if (!homePage.taskPanel.clickItem(dataPool.get("ObjectType")))
				throw new Exception("'"+ dataPool.get("ObjectType") +"' is not clicked from task pane.");

			if(!Utility.selectTemplate(dataPool.get("Template"), driver))
				throw new Exception("Template'"+ dataPool.get("Template") +"' is not selected.");

			Log.message("1. New '"+ dataPool.get("ObjectType") +"' object type metadata card is opened from task pane.");

			//Step-2: Select the class and check the defined property value for that class in the metadata card
			//-------------------------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadata card
			metadataCard.setInfo(dataPool.get("Class"));//Sets the class in the metadata card

			if(!metadataCard.getPropertyValue(dataPool.get("Class").split("::")[0]).equalsIgnoreCase(dataPool.get("Class").split("::")[1]))
				throw new Exception("Class is not set with the value '" + dataPool.get("Class").split("::")[1] + "' in the metadatacard.");

			Log.message("2. Class is set with the value '" + dataPool.get("Class").split("::")[1] + "' in the metadatacard.", driver);

			//Step-3: Add the property in the metadatacard
			//--------------------------------------------
			if(!metadataCard.addNewProperty(dataPool.get("Property")))
				throw new Exception("Property '" + dataPool.get("Property") + "' is not added in the metadatacard.");

			Log.message("3. Property '" + dataPool.get("Property") + "' is added in the metadatacard.");

			//Check the behavior in the metadatacard
			//--------------------------------------
			String result = "";

			if(!metadataCard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyValue")))
				result = "Property '" + dataPool.get("Property") + "' is not set with the value '" + dataPool.get("PropertyValue") + "' in the metadatacard.;";

			if(metadataCard.propertyExists(dataPool.get("Property1")))
				result += "Property '" + dataPool.get("Property1") + "' is added in the metadatacard;";

			if(result.equals(""))
				Log.pass("Test Case passed. Set value works as expected.");
			else
				Log.fail("Test case failed. Set value not works as expected. Additonal info. : '" + result + "'");


		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally{
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_40025

	/**
	 *  40030 :  SetValue does not support operator "Overwrite"
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"SetValue"},description = "SetValue does not support operator \"Overwrite\"" )
	public void TC_40030(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();//Launches the browser

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-Requisite: Login to the web access
			//--------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: New object metadatacard via new menu item
			//-------------------------------------------------
			if (!homePage.taskPanel.clickItem(dataPool.get("ObjectType")))
				throw new Exception("'"+ dataPool.get("ObjectType") +"' is not clicked from task pane.");

			if(!Utility.selectTemplate(dataPool.get("Template"), driver))
				throw new Exception("Template'"+ dataPool.get("Template") +"' is not selected.");

			Log.message("1. New '"+ dataPool.get("ObjectType") +"' object type metadata card is opened from task pane.");

			//Step-2: Add the property in the metadatacard
			//--------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadata card

			if(!metadataCard.addNewProperty(dataPool.get("Property")))
				throw new Exception("Property '" + dataPool.get("Property") + "' is not added in the metadatacard.");

			Log.message("2. Property '" + dataPool.get("Property") + "' is added in the metadatacard.");

			//Step-3: Set the filter condition 
			//--------------------------------
			metadataCard.setPropertyValue(dataPool.get("Property"), dataPool.get("PropertyValue1"));//Sets the property value in the metadatacard

			if(!metadataCard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyValue1")))
				throw new Exception("Property '" + dataPool.get("Property") + "' is not set with the value '" + dataPool.get("PropertyValue1") + "' in the metadatacard.");

			Log.message("3. Property '" + dataPool.get("Property") + "' is set with the value '" + dataPool.get("PropertyValue1") + "' in the metadatacard.");

			//Step-4: Check the behavior in the metadatacard
			//----------------------------------------------
			if(!metadataCard.getPropertyValue(dataPool.get("SetValueProperty")).equalsIgnoreCase(dataPool.get("PropertyValue1")))
				throw new Exception("Property '" + dataPool.get("SetValueProperty") + "' is not set with the value '" + dataPool.get("PropertyValue1") + "' in the metadatacard.");

			Log.message("4. Property '" + dataPool.get("SetValueProperty") + "' is set with the value '" + dataPool.get("PropertyValue1") + "' in the metadatacard.");

			//Step-5: Set the filter condition 
			//--------------------------------
			metadataCard.setPropertyValue(dataPool.get("Property"), dataPool.get("PropertyValue2"));//Sets the property value in the metadatacard

			if(!metadataCard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyValue2")))
				throw new Exception("Property '" + dataPool.get("Property") + "' is not set with the value '" + dataPool.get("PropertyValue2") + "' in the metadatacard.");

			Log.message("5. Property '" + dataPool.get("Property") + "' is set with the value '" + dataPool.get("PropertyValue2") + "' in the metadatacard.");

			//Verification: Check the behavior in the metadatacard
			//----------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("SetValueProperty")).equalsIgnoreCase(dataPool.get("PropertyValue1")))
				Log.pass("Test Case passed. SetValue does not support operator \"Overwrite\"");
			else
				Log.fail("Test case failed. SetValue supports operator \"Overwrite\". Additonal info. : Property '" + dataPool.get("SetValueProperty") + "' is set with the value '" + metadataCard.getPropertyValue(dataPool.get("SetValueProperty")) + "'. [Expected value: '" + dataPool.get("PropertyValue1") + "']");


		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally{
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_40030

	/**
	 *  40043 : MFWA: FN119 : Value added by using SetValue is not removed when class is changed.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"SetValue"},description = "MFWA: FN119 : Value added by using SetValue is not removed when class is changed." )
	public void TC_40043(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();//Launches the browser

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-Requisite: Login to the web access
			//--------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: New object metadatacard via new menu item
			//-------------------------------------------------
			if (!homePage.taskPanel.clickItem(dataPool.get("ObjectType")))
				throw new Exception("'"+ dataPool.get("ObjectType") +"' is not clicked from task pane.");

			if(!Utility.selectTemplate(dataPool.get("Template"), driver))
				throw new Exception("Template'"+ dataPool.get("Template") +"' is not selected.");

			Log.message("1. New '"+ dataPool.get("ObjectType") +"' object type metadata card is opened from task pane.");

			//Step-2: Select the class and check the defined property value for that class in the metadata card
			//-------------------------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadata card
			metadataCard.setInfo(dataPool.get("Class"));//Sets the class in the metadata card

			if(!metadataCard.getPropertyValue(dataPool.get("Class").split("::")[0]).equalsIgnoreCase(dataPool.get("Class").split("::")[1]))
				throw new Exception("Class is not set with the value '" + dataPool.get("Class").split("::")[1] + "' in the metadatacard.");

			if (!metadataCard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyValue")))
				throw new Exception("Property '" + dataPool.get("Property") + "' is not set with the value '" + dataPool.get("PropertyValue") + "' while selecting the Class '" + dataPool.get("Class").split("::")[1] + "' in the metadatacard.");

			Log.message("2. Property '" + dataPool.get("Property") + "' is set with the value '" + dataPool.get("PropertyValue") + "' while selecting the Class value '" + dataPool.get("Class").split("::")[1] + "' in the metadatacard.", driver);

			//Step-3 : Change the class in the metadatacard
			//---------------------------------------------
			metadataCard.setInfo(dataPool.get("Class1"));//Changes the class in the metadatacard

			if(!metadataCard.getPropertyValue(dataPool.get("Class1").split("::")[0]).equalsIgnoreCase(dataPool.get("Class1").split("::")[1]))
				throw new Exception("Class is not set with the value '" + dataPool.get("Class").split("::")[1] + "' in the metadatacard.");

			Log.message("3. Class is set with the value '" + dataPool.get("Class1").split("::")[1] + "' in the metadatacard.");

			//Verification: Check if SetValue is retained while changing the class in the metadatacard
			//------------------------------------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyValue")))
				Log.pass("Test Case Passed. Value added by using SetValue is not removed when class is changed successfully.", driver);
			else
				Log.fail("Test case failed. Value added by using SetValue is removed when class is changed. [Property '" + dataPool.get("Property") + "' value is not cleared and set with the value '" + dataPool.get("PropertyValue") + "']", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally{
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_40043	


	/**
	 *  38205 : Prefilled property value should be overwritten by setValue when "IsForced" : true
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"SetValue"},description = "Prefilled property value should be overwritten by setValue when \"IsForced\" : true" )
	public void TC_38205(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();//Launches the browser

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-Requisite: Login to the web access
			//--------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Navigate to the search view
			//-----------------------------------
			String navigateToView = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("viewToNavigate"), "");//Navigates to the search view

			Log.message("1. Navigated to the search view: '" + navigateToView + "'");

			//Step-2: Select the object in the view
			//-------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new Exception("Object - '" + dataPool.get("ObjectName") + "' is not selected in the list view");

			Log.message("2. Object - '" + dataPool.get("ObjectName") + "' is selected in the list view.");

			//Step-3: Open the new object metadatacard
			//----------------------------------------
			if(!homePage.taskPanel.clickItem(dataPool.get("ObjectType")))
				throw new Exception("'" + dataPool.get("ObjectType") + "' is not clicked from task pane.");

			Log.message("3. '" + dataPool.get("ObjectType") + "' is clicked from task pane.");

			//Verification: Check if Prefilled property value should be overwritten by setValue when "IsForced" : true
			//---------------------------------------------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard

			if(metadataCard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyValue")))
				Log.pass("Test case passed. Prefilled property value is overwritten by setValue when \"IsForced\" : true", driver);
			else
				Log.fail("Test case failed. Prefilled property value is not overwritten by setValue when \"IsForced\" : true. Additional info. : Expected property(" + dataPool.get("Property") + ") value '" + dataPool.get("PropertyValue") + "' & Actual Value: '" + metadataCard.getPropertyValue(dataPool.get("Property")) + "'", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally{
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_38205	

	/**
	 *  38275 : SetValue should work when SSLU condition property exists in metadata card
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"SetValue"},description = "SetValue should work when SSLU condition property exists in metadata card")
	public void TC_38275(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();//Launches the browser

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-Requisite: Login to the web access
			//--------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Navigate to the search view
			//-----------------------------------
			String navigateToView = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("viewToNavigate"), "");//Navigates to the search view

			Log.message("1. Navigated to the search view: '" + navigateToView + "'");

			//Step-2: Select the object in the view
			//-------------------------------------
			if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
				throw new Exception("Object - '" + dataPool.get("ObjectName") + "' is not selected in the list view");

			Log.message("2. Object - '" + dataPool.get("ObjectName") + "' is selected in the list view.");

			//Step-3: Open the new object metadatacard
			//----------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver, true);//Instantiates the metadatacard
			metadataCard.setInfo(dataPool.get("FilterCondition"));//Sets the property value in the metadatacard

			Log.message("3. Property '" + dataPool.get("FilterCondition").split("::")[0] + "' is set with the value '" + dataPool.get("FilterCondition").split("::")[1] + "' in the metadatacard.");

			//Verification: Check if Prefilled property value should be overwritten by setValue when "IsForced" : true
			//---------------------------------------------------------------------------------------------------------
			if(!metadataCard.propertyExists(dataPool.get("Property")))
				throw new Exception("Property '" + dataPool.get("Property") + "' is not added in the metadatacard.");

			if(metadataCard.getPropertyValue(dataPool.get("Property")).equalsIgnoreCase(dataPool.get("PropertyValue")))
				Log.pass("Test case passed. SetValue works as expected when SSLU condition property exists in metadata card", driver);
			else
				Log.fail("Test case failed. SetValue not works as expected when SSLU condition property exists in metadata card. Additional info. : Expected property(" + dataPool.get("Property") + ") value '" + dataPool.get("PropertyValue") + "' & Actual Value: '" + metadataCard.getPropertyValue(dataPool.get("Property")) + "'", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally{
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_38275

	/**
	 *  36542 : SetValue with auto-fill and pre-fill
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"SetValue"}, description = "SetValue with auto-fill/pre-fill" )
	public void TC_36542(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();//Launches the browser

			//Login to the MFWA with valid credentials
			//----------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in
			int j = 1;

			//Step: Navigate to view and select the object if needed
			//-------------------------------------------------------
			if(!dataPool.get("Condition").equalsIgnoreCase("AutoFill"))
			{
				String navigateToView = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("viewToNavigate"), "");//Navigates to the search view

				Log.message(j+++". Navigated to the search view: '" + navigateToView + "'");

				//Step: Select the object in the view
				//-------------------------------------
				if (!homePage.listView.clickItem(dataPool.get("ObjectName")))
					throw new Exception("Object - '" + dataPool.get("ObjectName") + "' is not selected in the list view");

				Log.message(j+++". Object - '" + dataPool.get("ObjectName") + "' is selected in the list view.");
			}

			//Step: Click the new object link from task pane
			//--------------------------------------------------
			if(!homePage.taskPanel.clickItem(dataPool.get("ObjectType")))
				throw new Exception("'" + dataPool.get("ObjectType") + "' is not clicked from task pane.");

			Log.message(j+++". '" + dataPool.get("ObjectType") + "' is clicked from task pane.");

			//Step: Set the template and select the class
			//-----------------------------------------------
			if(!Utility.selectTemplate(dataPool.get("Template"), driver))//Selects the template
				throw new Exception("Template '" + dataPool.get("Template") + "' is not selected");

			MetadataCard metadataCard = new MetadataCard(driver);//Instantaites the metadatacard
			metadataCard.setInfo(dataPool.get("Class"));//Selects the class in the metadatacard

			Log.message(j+++". Template '" + dataPool.get("Template") + "' is selected and class '" + dataPool.get("Class").split("::")[1] + "' is set in the metadatacard.");

			if(!dataPool.get("Condition").equalsIgnoreCase("PreFill"))
			{
				//Step: Set the property value which sets the auto fill value in the metadatacard
				//----------------------------------------------------------------------------------
				metadataCard.setInfo(dataPool.get("Company"));//Sets the company value in the metadatacard

				if(!MFilesDialog.exists(driver, "Confirm Autofill"))
					throw new Exception("Auto fill dialog is not displayed while setting the property '" + dataPool.get("Company").split("::")[0] + "' with value '" + dataPool.get("Company").split("::")[1] + "' in the metadatacard.");

				MFilesDialog mfDialog = new MFilesDialog(driver, "Confirm Autofill");//Instantiates the MFiles Dialog
				mfDialog.clickOkButton();//Clicks OK button in the metadatacard

				Log.message(j+++". Clicked Yes in the Auto fill dialog which is displayed while setting the property '" + dataPool.get("Company").split("::")[0] + "' with value '" + dataPool.get("Company").split("::")[1] + "' in the metadatacard.");

				//Step: Check if properties are filled in the metadatacard as expected
				//------------------------------------------------------------------------
				if(!metadataCard.getPropertyValue(dataPool.get("State").split("::")[0]).equalsIgnoreCase(dataPool.get("State").split("::")[1]))
					throw new Exception("Property '" + dataPool.get("State").split("::")[0] + "' is not autofilled with the value '" + dataPool.get("State").split("::")[1] + "' in the metadatacard.");

				Log.message(j+++". Property '" + dataPool.get("State").split("::")[0] + "' is autofilled with the value '" + dataPool.get("State").split("::")[1] + "' in the metadatacard.", driver);

				//Step: Set the property value which sets the auto fill value in the metadatacard
				//----------------------------------------------------------------------------------
				metadataCard.setInfo(dataPool.get("Variety"));//Sets the company value in the metadatacard

				if(!MFilesDialog.exists(driver, "Confirm Autofill"))
					throw new Exception("Auto fill dialog is not displayed while setting the property '" + dataPool.get("Variety").split("::")[0] + "' with value '" + dataPool.get("Variety").split("::")[1] + "' in the metadatacard.");

				mfDialog = new MFilesDialog(driver, "Confirm Autofill");//Instantiates the MFiles Dialog
				mfDialog.clickOkButton();//Clicks OK button in the metadatacard
				metadataCard.clickProperty(dataPool.get("Variety").split("::")[0]);

				Log.message(j+++". Clicked Yes in the Auto fill dialog which is displayed while setting the property '" + dataPool.get("Variety").split("::")[0] + "' with value '" + dataPool.get("Variety").split("::")[1] + "' in the metadatacard.");
			}
			else
			{
				//Step: Check if the autofill dialog is dispayed and  click yes button in the metadatacard
				//-----------------------------------------------------------------------------------------
				if(!MFilesDialog.exists(driver, "Confirm Autofill"))
					throw new Exception("Auto fill dialog is not displayed while setting the property '" + dataPool.get("Company").split("::")[0] + "' with value '" + dataPool.get("Company").split("::")[1] + "' in the metadatacard.");

				MFilesDialog mfDialog = new MFilesDialog(driver, "Confirm Autofill");//Instantiates the MFiles Dialog
				mfDialog.clickOkButton();//Clicks OK button in the metadatacard

				Log.message(j+++". Clicked Yes in the Auto fill dialog.");
			}

			//Verification: Check if properties are filled in the metadatacard as expected
			//------------------------------------------------------------------------
			String[] properties = dataPool.get("Properties").split("\n");
			String result = "";
			String propValue = "";

			for (int i = 0; i < properties.length; i++)
			{
				propValue = "";

				if(properties[i].split("::").length != 1)
					propValue = properties[i].split("::")[1];

				if(!metadataCard.getPropertyValue(properties[i].split("::")[0]).equalsIgnoreCase(propValue))
					if(dataPool.get("Condition").equals("AutoFill"))
						result += "Property '" + properties[i].split("::")[0] + "' is not set with the value '" + propValue + "' in the metadatacard.[Actual property value:'" + metadataCard.getPropertyValue(properties[i].split("::")[0]) + "'];";
					else
						result += "Property '" + properties[i].split("::")[0] + "' is not prefilled with the value '" + propValue + "' in the metadatacard.[Actual property value:'" + metadataCard.getPropertyValue(properties[i].split("::")[0]) + "'];";
			}

			if(result.equals(""))
				Log.pass("Test case passed. SetValue with '" + dataPool.get("Condition") + "' is working as expected.", driver);
			else
				Log.fail("Test case failed. SetValue with '" + dataPool.get("Condition") + "' is not working as expected.[Additonal info. : \"" + result + "\"]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally{
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_36542

}//End class MetadataConfigDefaultValue


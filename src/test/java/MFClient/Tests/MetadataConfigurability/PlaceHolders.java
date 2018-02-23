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
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class PlaceHolders {

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
	 * TC_35915: Forced ReadOnly Boolean property placeholder follows value changes of source property
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"PlaceHolders"}, 
			description = "Forced ReadOnly Boolean property placeholder follows value changes of source property")
	public void TC_35915(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-Requisite: Login to the web access
			//--------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: New object metadatacard via new menu item
			//-------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//CLicks the new menu item

			if(!Utility.selectTemplate(dataPool.get("Template"), driver))
				throw new Exception("Template'"+ dataPool.get("Template") +"' is not selected.");

			Log.message("1. New '"+ dataPool.get("ObjectType") +"' object type metadata card is opened from new item menu.");

			//Step-2: Set the class in the metadatacard
			//-----------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard
			metadataCard.setPropertyValue(dataPool.get("Class").split("::")[0], dataPool.get("Class").split("::")[1]);//Sets the property value in the metadatacard

			Log.message("2. Class is set with the value '"+ dataPool.get("Class").split("::")[1] +"' in the metadatacard.");

			//Step-3: Check if source property and Additonal Property has same values
			//-----------------------------------------------------------------------
			if(!metadataCard.getPropertyValue(dataPool.get("SourceProperty")).equalsIgnoreCase(dataPool.get("SourcePropertyValue")))
				throw new Exception("Property '" + dataPool.get("SourceProperty") + "' is not set with the value '" + dataPool.get("SourcePropertyValue").replaceAll("\n", ",") + "' in the metadatacard.");

			Log.message("3.1. Property '" + dataPool.get("SourceProperty") + "' is set with the value '" + dataPool.get("SourcePropertyValue") + "' in the metadatacard.");

			if(!metadataCard.getPropertyValue(dataPool.get("AdditionalProperty")).equalsIgnoreCase(metadataCard.getPropertyValue(dataPool.get("SourceProperty"))))
				throw new Exception("Property '" + dataPool.get("AdditionalProperty") + "' is not set with the value '" + dataPool.get("SourcePropertyValue").replaceAll("\n", ",") + "' in the metadatacard when source property value is set.");

			Log.message("3.2. Property '" + dataPool.get("AdditionalProperty") + "' is set with the value '" + dataPool.get("SourcePropertyValue").replaceAll("\n", ",") + "' as source property value in the metadatacard");

			//Step-4: Check if the additional property is read only property
			//--------------------------------------------------------------
			metadataCard.clickProperty(dataPool.get("AdditionalProperty"));//Clicks the property in the metadatacard

			if(metadataCard.isPropertyInEditMode(dataPool.get("AdditionalProperty")))
				throw new Exception(dataPool.get("AdditionalProperty")+" is not a read only property");

			Log.message("4. '" + dataPool.get("AdditionalProperty") + "' is not editable in the metadatacard.");

			//Step-5: Modify source property value and verify placeholder value updated
			//--------------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("SourceProperty"), dataPool.get("SourcePropertyValue1"));//Sets the property value in the metadatacard

			if(!metadataCard.getPropertyValue(dataPool.get("SourceProperty")).equalsIgnoreCase(dataPool.get("SourcePropertyValue1")))
				throw new Exception("Property '" + dataPool.get("SourceProperty") + "' is not set with the value '" + dataPool.get("SourcePropertyValue1") + "' in the metadatacard.");

			Log.message("5. Source Property '" + dataPool.get("SourceProperty") + "' is set with the value '" + dataPool.get("SourcePropertyValue") + "' in the metadatacard.");

			//Verification: IF Additonal property is set with the source property value
			//--------------------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("AdditionalProperty")).contains(dataPool.get("SourcePropertyValue1")))
				Log.pass("Test case passed. Forced ReadOnly Boolean property placeholder follows value changes of source property as expected.", driver);
			else
				Log.fail("Test case failed. After source property modification, Property '" + dataPool.get("AdditionalProperty") + "' is set with the value '" + dataPool.get("SourcePropertyValue1").replaceAll("\n", ",") + "' as source property value in the metadatacard.", driver);


		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally
	}//End TC_35915

	/*
	 * TC_35930: Forced ReadOnly multi-select property placeholder follows values of source property
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"PlaceHolders"}, 
			description = "Forced ReadOnly multi-select property placeholder follows values of source property")
	public void TC_35930(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-Requisite: Login to the web access
			//--------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: New object metadatacard via new menu item
			//-------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//CLicks the new menu item

			if(!Utility.selectTemplate(dataPool.get("Template"), driver))
				throw new Exception("Template'"+ dataPool.get("Template") +"' is not selected.");

			Log.message("1. New '"+ dataPool.get("ObjectType") +"' object type metadata card is opened from new item menu.");

			//Step-2: Set the class in the metadatacard
			//-----------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard
			metadataCard.setPropertyValue(dataPool.get("Class").split("::")[0], dataPool.get("Class").split("::")[1]);//Sets the property value in the metadatacard

			Log.message("2. Class is set with the value '"+ dataPool.get("Class").split("::")[1] +"' in the metadatacard.");

			//Step-3: Check if source property and Additonal Property has same values
			//-----------------------------------------------------------------------
			if(!metadataCard.getPropertyValue(dataPool.get("SourceProperty")).equalsIgnoreCase(dataPool.get("SourcePropertyValue")))
				throw new Exception("Property '" + dataPool.get("SourceProperty") + "' is not set with the value '" + dataPool.get("SourcePropertyValue").replaceAll("\n", ",") + "' in the metadatacard.");

			Log.message("3.1. Property '" + dataPool.get("SourceProperty") + "' is set with the value '" + dataPool.get("SourcePropertyValue") + "' in the metadatacard.");

			if(!metadataCard.getPropertyValue(dataPool.get("AdditionalProperty")).equalsIgnoreCase(metadataCard.getPropertyValue(dataPool.get("SourceProperty"))))
				throw new Exception("Property '" + dataPool.get("AdditionalProperty") + "' is not set with the value '" + dataPool.get("SourcePropertyValue").replaceAll("\n", ",") + "' in the metadatacard when source property value is set.");

			Log.message("3.2. Property '" + dataPool.get("AdditionalProperty") + "' is set with the value '" + dataPool.get("SourcePropertyValue").replaceAll("\n", ",") + "' as source property value in the metadatacard");

			//Step-4: Check if the additional property is read only property
			//--------------------------------------------------------------
			metadataCard.clickProperty(dataPool.get("AdditionalProperty"));//Clicks the property in the metadatacard

			if(metadataCard.isPropertyInEditMode(dataPool.get("AdditionalProperty")))
				throw new Exception(dataPool.get("AdditionalProperty")+" is not a read only property");

			Log.message("4. '" + dataPool.get("AdditionalProperty") + "' is not editable in the metadatacard.");

			//Step-5: Modify source property value and verify placeholder value updated
			//--------------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("SourceProperty"), dataPool.get("SourcePropertyValue1").split("\n")[0], 1);//Sets the property value in the metadatacard

			if(!metadataCard.getPropertyValue(dataPool.get("SourceProperty")).equalsIgnoreCase(dataPool.get("SourcePropertyValue1")))
				throw new Exception("Property '" + dataPool.get("SourceProperty") + "' is not set with the value '" + dataPool.get("SourcePropertyValue1").replaceAll("\n", ",") + "' in the metadatacard.");

			Log.message("5. Source Property '" + dataPool.get("SourceProperty") + "' is set with the value '" + dataPool.get("SourcePropertyValue") + "' in the metadatacard.");

			//Verification: IF Additonal property is set with the source property value
			//--------------------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("AdditionalProperty")).contains(dataPool.get("SourcePropertyValue1").split("\n")[0]))
				Log.pass("Test case passed. Forced ReadOnly multi-select property placeholder follows values of source property as expected.", driver);
			else
				Log.fail("Test case failed. After source property modification, Property '" + dataPool.get("AdditionalProperty") + "' is set with the value '" + dataPool.get("SourcePropertyValue1").replaceAll("\n", ",") + "' as source property value in the metadatacard.", driver);


		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally
	}//End TC_35930

	/*
	 * TC_35934: Forced multi-select property placeholder follows value changes of source property
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"PlaceHolders"}, 
			description = "Forced multi-select property placeholder follows value changes of source property")
	public void TC_35934(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-Requisite: Login to the web access
			//--------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: New object metadatacard via new menu item
			//-------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//CLicks the new menu item

			if(!Utility.selectTemplate(dataPool.get("Template"), driver))
				throw new Exception("Template'"+ dataPool.get("Template") +"' is not selected.");

			Log.message("1. New '"+ dataPool.get("ObjectType") +"' object type metadata card is opened from new item menu.");

			//Step-2: Set the class in the metadatacard
			//-----------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard
			metadataCard.setPropertyValue(dataPool.get("Class").split("::")[0], dataPool.get("Class").split("::")[1]);//Sets the property value in the metadatacard

			Log.message("2. Class is set with the value '"+ dataPool.get("Class").split("::")[1] +"' in the metadatacard.");

			//Step-3: Check if source property and Additonal Property has same values
			//-----------------------------------------------------------------------
			if(!metadataCard.getPropertyValue(dataPool.get("SourceProperty")).equalsIgnoreCase(dataPool.get("SourcePropertyValue")))
				throw new Exception("Property '" + dataPool.get("SourceProperty") + "' is not set with the value '" + dataPool.get("SourcePropertyValue").replaceAll("\n", ",") + "' in the metadatacard.");

			Log.message("3.1. Property '" + dataPool.get("SourceProperty") + "' is set with the value '" + dataPool.get("SourcePropertyValue") + "' in the metadatacard.");

			if(!metadataCard.getPropertyValue(dataPool.get("AdditionalProperty")).equalsIgnoreCase(metadataCard.getPropertyValue(dataPool.get("SourceProperty"))))
				throw new Exception("Property '" + dataPool.get("AdditionalProperty") + "' is not set with the value '" + dataPool.get("SourcePropertyValue").replaceAll("\n", ",") + "' in the metadatacard when source property value is set.");

			Log.message("3.2. Property '" + dataPool.get("AdditionalProperty") + "' is set with the value '" + dataPool.get("SourcePropertyValue").replaceAll("\n", ",") + "' as source property value in the metadatacard");

			//Step-4: Modify place holder property value in the metadatacard
			//--------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("AdditionalProperty"), dataPool.get("AdditionalPropertyValue"), 1);//Modifies the property value in the metadatacard

			Log.message("4. Place holder property '" + dataPool.get("AdditionalProperty") + "' is set with the value '" + dataPool.get("AdditionalPropertyValue") + "' in the metadatacard.", driver);

			//Step-5: Modify source property value and verify placeholder value updated
			//--------------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("SourceProperty"), dataPool.get("SourcePropertyValue1").split("\n")[0], 1);//Sets the property value in the metadatacard

			if(!metadataCard.getPropertyValue(dataPool.get("SourceProperty")).equalsIgnoreCase(dataPool.get("SourcePropertyValue1")))
				throw new Exception("Property '" + dataPool.get("SourceProperty") + "' is not set with the value '" + dataPool.get("SourcePropertyValue1").replaceAll("\n", ",") + "' in the metadatacard.");

			Log.message("5. Source Property '" + dataPool.get("SourceProperty") + "' is set with the value '" + dataPool.get("SourcePropertyValue") + "' in the metadatacard.");


			//Verification: IF Additonal property is set with the source property value
			//--------------------------------------------------------------------------
			if(!metadataCard.getPropertyValue(dataPool.get("AdditionalProperty")).contains(dataPool.get("SourcePropertyValue1").split("\n")[0]))
				Log.pass("Test case passed. Forced multi-select property placeholder follows value changes of source property as expected.", driver);
			else
				Log.fail("Test case failed. After source property modification, Property '" + dataPool.get("AdditionalProperty") + "' is set with the value '" + dataPool.get("SourcePropertyValue1").replaceAll("\n", ",") + "' as source property value in the metadatacard.", driver);


		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally
	}//End TC_35934

	/*
	 * TC_35938: Non-Forced multi-select property placeholder follows value changes of source property
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"PlaceHolders"}, 
			description = "Non-Forced multi-select property placeholder follows value changes of source property")
	public void TC_35938(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-Requisite: Login to the web access
			//--------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: New object metadatacard via new menu item
			//-------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//CLicks the new menu item

			if(!Utility.selectTemplate(dataPool.get("Template"), driver))
				throw new Exception("Template'"+ dataPool.get("Template") +"' is not selected.");

			Log.message("1. New '"+ dataPool.get("ObjectType") +"' object type metadata card is opened from new item menu.");

			//Step-2: Set the class in the metadatacard
			//-----------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard
			metadataCard.setPropertyValue(dataPool.get("Class").split("::")[0], dataPool.get("Class").split("::")[1]);//Sets the property value in the metadatacard

			Log.message("2. Class is set with the value '"+ dataPool.get("Class").split("::")[1] +"' in the metadatacard.");

			//Step-3: Check if source property and Additonal Property has same values
			//-----------------------------------------------------------------------
			if(!metadataCard.getPropertyValue(dataPool.get("SourceProperty")).equalsIgnoreCase(dataPool.get("SourcePropertyValue")))
				throw new Exception("Property '" + dataPool.get("SourceProperty") + "' is not set with the value '" + dataPool.get("SourcePropertyValue").replaceAll("\n", ",") + "' in the metadatacard.");

			Log.message("3.1. Property '" + dataPool.get("SourceProperty") + "' is set with the value '" + dataPool.get("SourcePropertyValue") + "' in the metadatacard.");

			if(!metadataCard.getPropertyValue(dataPool.get("AdditionalProperty")).equalsIgnoreCase(metadataCard.getPropertyValue(dataPool.get("SourceProperty"))))
				throw new Exception("Property '" + dataPool.get("AdditionalProperty") + "' is not set with the value '" + dataPool.get("SourcePropertyValue").replaceAll("\n", ",") + "' in the metadatacard when source property value is set.");

			Log.message("3.2. Property '" + dataPool.get("AdditionalProperty") + "' is set with the value '" + dataPool.get("SourcePropertyValue").replaceAll("\n", ",") + "' as source property value in the metadatacard");

			//Step-4: Modify place holder property value in the metadatacard
			//--------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("AdditionalProperty"), dataPool.get("AdditionalPropertyValue"), 1);//Modifies the property value in the metadatacard

			Log.message("4. Place holder property '" + dataPool.get("AdditionalProperty") + "' is set with the value '" + dataPool.get("AdditionalPropertyValue") + "' in the metadatacard.", driver);

			//Step-5: Modify source property value and verify placeholder value updated
			//--------------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("SourceProperty"), dataPool.get("SourcePropertyValue1").split("\n")[0], 1);//Sets the property value in the metadatacard

			if(!metadataCard.getPropertyValue(dataPool.get("SourceProperty")).equalsIgnoreCase(dataPool.get("SourcePropertyValue1")))
				throw new Exception("Property '" + dataPool.get("SourceProperty") + "' is not set with the value '" + dataPool.get("SourcePropertyValue1").replaceAll("\n", ",") + "' in the metadatacard.");

			Log.message("5. Source Property '" + dataPool.get("SourceProperty") + "' is set with the value '" + dataPool.get("SourcePropertyValue") + "' in the metadatacard.");

			//Verification: IF Additonal property is set with the source property value
			//--------------------------------------------------------------------------
			if(!metadataCard.getPropertyValue(dataPool.get("AdditionalProperty")).contains(dataPool.get("SourcePropertyValue1").split("\n")[0]))
				Log.pass("Test case passed. Non-Forced multi-select property placeholder follows value changes of source property as expected.", driver);
			else
				Log.fail("Test case failed. After source property modification, Property '" + dataPool.get("AdditionalProperty") + "' is set with the value '" + dataPool.get("SourcePropertyValue1").replaceAll("\n", ",") + "' as source property value in the metadatacard.", driver);


		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally
	}//End TC_35938

	/*
	 * 35940: Non-Forced ReadOnly multi-select property placeholder follows value changes of source property
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"PlaceHolders"}, 
			description = "Non-Forced ReadOnly multi-select property placeholder follows value changes of source property")
	public void TC_35940(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-Requisite: Login to the web access
			//--------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: New object metadatacard via new menu item
			//-------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//CLicks the new menu item

			if(!Utility.selectTemplate(dataPool.get("Template"), driver))
				throw new Exception("Template'"+ dataPool.get("Template") +"' is not selected.");

			Log.message("1. New '"+ dataPool.get("ObjectType") +"' object type metadata card is opened from new item menu.");

			//Step-2: Set the class in the metadatacard
			//-----------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard
			metadataCard.setPropertyValue(dataPool.get("Class").split("::")[0], dataPool.get("Class").split("::")[1]);//Sets the property value in the metadatacard

			Log.message("2. Class is set with the value '"+ dataPool.get("Class").split("::")[1] +"' in the metadatacard.");

			//Step-3: Check if source property and Additonal Property has same values
			//-----------------------------------------------------------------------
			if(!metadataCard.getPropertyValue(dataPool.get("SourceProperty")).equalsIgnoreCase(dataPool.get("SourcePropertyValue")))
				throw new Exception("Property '" + dataPool.get("SourceProperty") + "' is not set with the value '" + dataPool.get("SourcePropertyValue").replaceAll("\n", ",") + "' in the metadatacard.");

			Log.message("3.1. Property '" + dataPool.get("SourceProperty") + "' is set with the value '" + dataPool.get("SourcePropertyValue") + "' in the metadatacard.");

			if(!metadataCard.getPropertyValue(dataPool.get("AdditionalProperty")).equalsIgnoreCase(metadataCard.getPropertyValue(dataPool.get("SourceProperty"))))
				throw new Exception("Property '" + dataPool.get("AdditionalProperty") + "' is not set with the value '" + dataPool.get("SourcePropertyValue").replaceAll("\n", ",") + "' in the metadatacard when source property value is set.");

			Log.message("3.2. Property '" + dataPool.get("AdditionalProperty") + "' is set with the value '" + dataPool.get("SourcePropertyValue").replaceAll("\n", ",") + "' as source property value in the metadatacard");

			//Step-4: Check if the additional property is read only property
			//--------------------------------------------------------------
			metadataCard.clickProperty(dataPool.get("AdditionalProperty"));//Clicks the property in the metadatacard

			if(metadataCard.isPropertyInEditMode(dataPool.get("AdditionalProperty")))
				throw new Exception(dataPool.get("AdditionalProperty")+" is not a read only property");

			Log.message("4. '" + dataPool.get("AdditionalProperty") + "' is not editable in the metadatacard.");

			//Step-5: Modify source property value and verify placeholder value updated
			//--------------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("SourceProperty"), dataPool.get("SourcePropertyValue1").split("\n")[0], 1);//Sets the property value in the metadatacard

			if(!metadataCard.getPropertyValue(dataPool.get("SourceProperty")).equalsIgnoreCase(dataPool.get("SourcePropertyValue1")))
				throw new Exception("Property '" + dataPool.get("SourceProperty") + "' is not set with the value '" + dataPool.get("SourcePropertyValue1").replaceAll("\n", ",") + "' in the metadatacard.");

			Log.message("5. Property '" + dataPool.get("SourceProperty") + "' is set with the value '" + dataPool.get("SourcePropertyValue") + "' in the metadatacard.");

			//Verification: IF Additonal property is set with the source property value
			//--------------------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("AdditionalProperty")).equalsIgnoreCase(metadataCard.getPropertyValue(dataPool.get("SourceProperty"))))
				Log.pass("Test case passed. Non-Forced ReadOnly multi-select property placeholder follows value changes of source propert as expected.", driver);
			else
				Log.fail("Test case failed. After source property modification, Property '" + dataPool.get("AdditionalProperty") + "' is not set with the value '" + dataPool.get("SourcePropertyValue1").replaceAll("\n", ",") + "' as source property value in the metadatacard.", driver);


		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally
	}//End TC_35940

	/*
	 * 35943: Forced Boolean property placeholder follows value changes of source property
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"PlaceHolders"}, 
			description = "Non-Forced Boolean property placeholder follows value changes of source property")
	public void TC_35943(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-Requisite: Login to the web access
			//--------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: New object metadatacard via new menu item
			//-------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//CLicks the new menu item

			if(!Utility.selectTemplate(dataPool.get("Template"), driver))
				throw new Exception("Template'"+ dataPool.get("Template") +"' is not selected.");

			Log.message("1. New '"+ dataPool.get("ObjectType") +"' object type metadata card is opened from new item menu.");

			//Step-2: Set the class in the metadatacard
			//-----------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard
			metadataCard.setPropertyValue(dataPool.get("Class").split("::")[0], dataPool.get("Class").split("::")[1]);//Sets the property value in the metadatacard

			Log.message("2. Class is set with the value '"+ dataPool.get("Class").split("::")[1] +"' in the metadatacard.");

			//Step-3: Modify the source property value and check the place holder property value
			//----------------------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("SourceProperty").split("::")[0], dataPool.get("SourceProperty").split("::")[1]);//Sets the property value in the metadatacard

			if(!metadataCard.getPropertyValue(dataPool.get("SourceProperty").split("::")[0]).equalsIgnoreCase(dataPool.get("SourceProperty").split("::")[1]))
				throw new Exception("Property '" + dataPool.get("SourceProperty").split("::")[0] + "' is not set with the value '" + dataPool.get("SourceProperty").split("::")[1] + "' in the metadatacard.");

			Log.message("3.1. Property '" + dataPool.get("SourceProperty").split("::")[0] + "' is set with the value '" + dataPool.get("SourceProperty").split("::")[1] + "' in the metadatacard.");

			if(!metadataCard.getPropertyValue(dataPool.get("AdditionalProperty")).equalsIgnoreCase(dataPool.get("SourceProperty").split("::")[1]))
				throw new Exception("Property '" + dataPool.get("AdditionalProperty") + "' is not set with the value '" + dataPool.get("SourceProperty").split("::")[1] + "' in the metadatacard when source property value is set.");

			Log.message("3.2. Property '" + dataPool.get("AdditionalProperty").split("::")[0] + "' is set with the value '" + dataPool.get("SourceProperty").split("::")[1] + "' in the metadatacard when source property value is set.");

			//Step-4: Modify the additional property value in the metadatacard
			//----------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("AdditionalProperty"), "");//Sets the property value in the metadatacard

			if(!metadataCard.getPropertyValue(dataPool.get("AdditionalProperty")).equals(""))
				throw new Exception("Property '" + dataPool.get("AdditionalProperty") + "' is not emptied in the metadatacard.");

			Log.message("4. Property '" + dataPool.get("AdditionalProperty") + "' is emptied in the metadatacard.");

			//Step-5: Modify the source property value in the metadatacard
			//-------------------------------------------------------------
			metadataCard.setInfo(dataPool.get("ModifyProperties"));//Sets the property value in the metadatacard

			Log.message("5. Properties modified in the metadatacard.", driver);

			//Verification: Check if additonal property value is modified
			//-----------------------------------------------------------
			if(!metadataCard.getPropertyValue(dataPool.get("AdditionalProperty")).equalsIgnoreCase(dataPool.get("SourcePropertyValue")))
				Log.pass("Test case passed. Forced Boolean property placeholder follows value changes of source property till boolean property is not modified.", driver);
			else
				Log.fail("Test case failed. Forced Boolean property placeholder follows value changes of source property even after modifying the boolean property.[Additonal info.: AdditionalProperty property is set with the value of source property.]", driver);

		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally
	}//End TC_35943

	/*
	 * 35944: Non-Forced Boolean property placeholder follows value changes of source property
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"PlaceHolders"}, 
			description = "Non-Forced Boolean property placeholder follows value changes of source property")
	public void TC_35944(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-Requisite: Login to the web access
			//--------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: New object metadatacard via new menu item
			//-------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//CLicks the new menu item

			if(!Utility.selectTemplate(dataPool.get("Template"), driver))
				throw new Exception("Template'"+ dataPool.get("Template") +"' is not selected.");

			Log.message("1. New '"+ dataPool.get("ObjectType") +"' object type metadata card is opened from new item menu.");

			//Step-2: Set the class in the metadatacard
			//-----------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard
			metadataCard.setPropertyValue(dataPool.get("Class").split("::")[0], dataPool.get("Class").split("::")[1]);//Sets the property value in the metadatacard

			Log.message("2. Class is set with the value '"+ dataPool.get("Class").split("::")[1] +"' in the metadatacard.");

			//Step-3: Modify the source property value and check the place holder property value
			//----------------------------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("SourceProperty").split("::")[0], dataPool.get("SourceProperty").split("::")[1]);//Sets the property value in the metadatacard

			if(!metadataCard.getPropertyValue(dataPool.get("SourceProperty").split("::")[0]).equalsIgnoreCase(dataPool.get("SourceProperty").split("::")[1]))
				throw new Exception("Property '" + dataPool.get("SourceProperty").split("::")[0] + "' is not set with the value '" + dataPool.get("SourceProperty").split("::")[1] + "' in the metadatacard.");

			Log.message("3.1. Property '" + dataPool.get("SourceProperty").split("::")[0] + "' is set with the value '" + dataPool.get("SourceProperty").split("::")[1] + "' in the metadatacard.");

			if(!metadataCard.getPropertyValue(dataPool.get("AdditionalProperty")).equalsIgnoreCase(dataPool.get("SourceProperty").split("::")[1]))
				throw new Exception("Property '" + dataPool.get("AdditionalProperty") + "' is not set with the value '" + dataPool.get("SourceProperty").split("::")[1] + "' in the metadatacard when source property value is set.");

			Log.message("3.2. Property '" + dataPool.get("AdditionalProperty").split("::")[0] + "' is set with the value '" + dataPool.get("SourceProperty").split("::")[1] + "' in the metadatacard when source property value is set.");

			//Step-4: Modify the additional property value in the metadatacard
			//----------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("AdditionalProperty"), "");//Sets the property value in the metadatacard

			if(!metadataCard.getPropertyValue(dataPool.get("AdditionalProperty")).equals(""))
				throw new Exception("Property '" + dataPool.get("AdditionalProperty") + "' is not emptied in the metadatacard.");

			Log.message("4. Property '" + dataPool.get("AdditionalProperty") + "' is emptied in the metadatacard.");

			//Step-5: Modify the source property value in the metadatacard
			//-------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("SourceProperty1").split("::")[0], dataPool.get("SourceProperty1").split("::")[1]);//Sets the property value in the metadatacard

			if(!metadataCard.getPropertyValue(dataPool.get("SourceProperty1").split("::")[0]).equalsIgnoreCase(dataPool.get("SourceProperty1").split("::")[1]))
				throw new Exception("Property '" + dataPool.get("SourceProperty1").split("::")[0] + "' is not set with the value '" + dataPool.get("SourceProperty1").split("::")[1] + "' in the metadatacard.");

			Log.message("5. Property '" + dataPool.get("SourceProperty1").split("::")[0] + "' is set with the value '" + dataPool.get("SourceProperty1").split("::")[1] + "' in the metadatacard.");

			//Verification: Check if additonal property value is modified
			//-----------------------------------------------------------
			if(!metadataCard.getPropertyValue(dataPool.get("AdditionalProperty")).equalsIgnoreCase(dataPool.get("SourceProperty1").split("::")[1]))
				Log.pass("Test case passed. Non-Forced Boolean property placeholder follows value changes of source property till boolean property is not modified.", driver);
			else
				Log.fail("Test case failed. Non-Forced Boolean property placeholder follows value changes of source property even after modifying the boolean property.[Additonal info.: AdditionalProperty property is set with the value of source property.]", driver);

		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally
	}//End TC_35944

	/*
	 * 36094: Placeholders points to itself
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"PlaceHolders"}, 
			description = "Placeholders points to itself.")
	public void TC_36094(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-Requisite: Login to the web access
			//--------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: New object metadatacard via task pane
			//-------------------------------------------------
			if (!homePage.taskPanel.clickItem(dataPool.get("ObjectType")))
				throw new Exception("'"+ dataPool.get("ObjectType") +"' is not clicked from task pane.");

			Log.message("1. New '"+ dataPool.get("ObjectType") +"' object type metadata card is opened from task pane.");

			//Step-2: Set the class in the metadatacard
			//-----------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard
			metadataCard.setPropertyValue(dataPool.get("Class").split("::")[0], dataPool.get("Class").split("::")[1]);//Sets the property value in the metadatacard

			Log.message("2. Class is set with the value '"+ dataPool.get("Class").split("::")[1] +"' in the metadatacard.");

			//Verification: Check if the warning dialog is displayed with the expected messgae
			//--------------------------------------------------------------------------------
			if(!MFilesDialog.exists(driver, "M-Files Web"))
				throw new Exception("Test case failed. Warning dialog is not displayed while Dynamic placeholders points to itself.");

			MFilesDialog mfDialog = new MFilesDialog(driver, "M-Files Web");//Instantiates the M-Files Dialog
			String actualMessage = mfDialog.getMessage();//Gets the message from the MFDialog

			if(actualMessage.equalsIgnoreCase(dataPool.get("ExpectedMessage")))
				Log.pass("Test case passed. When Dynamic placeholders points to itself, the warning dialog with message '" + actualMessage + "' is displayed as expected.", driver);
			else
				Log.fail("Test case failed. When Dynamic placeholders points to itself, the warning dialog with message '" + dataPool.get("ExpectedMessage") + "' is not displayed as expected.[Actual message displayed: '" + actualMessage + "']", driver);

		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally
	}//End TC_36094

	/*
	 * 36095: Placeholders points to each other
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"PlaceHolders"}, 
			description = "Placeholders points to each other.")
	public void TC_36095(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-Requisite: Login to the web access
			//--------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: New object metadatacard via new menu item
			//-------------------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));//CLicks the new menu item			

			Log.message("1. New '"+ dataPool.get("ObjectType") +"' object type metadata card is opened from task pane.");

			//Verification: Check if the warning dialog is displayed with the expected messgae
			//--------------------------------------------------------------------------------
			if(!MFilesDialog.exists(driver, "M-Files Web"))
				throw new Exception("Test case failed. Warning dialog is not displayed while Dynamic placeholders points to each other.");

			MFilesDialog mfDialog = new MFilesDialog(driver, "M-Files Web");//Instantiates the M-Files Dialog
			String actualMessage = mfDialog.getMessage();//Gets the message from the MFDialog

			if(actualMessage.equalsIgnoreCase(dataPool.get("ExpectedMessage")))
				Log.pass("Test case passed. When Dynamic placeholders points to each other, the warning dialog with message '" + actualMessage + "' is displayed as expected.", driver);
			else
				Log.fail("Test case failed. When Dynamic placeholders points to each other, the warning dialog with message '" + dataPool.get("ExpectedMessage") + "' is not displayed as expected.[Actual message displayed: '" + actualMessage + "']", driver);

		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally
	}//End TC_36095

	/*
	 * 38255: SetValue with placeholder should add text even if placeholder is empty.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"PlaceHolders"}, 
			description = "SetValue with placeholder should add text even if placeholder is empty.")
	public void TC_38255(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			Log.message("2. Class property is set with the value '" + dataPool.get("Class").split("::")[1] + "' in the metadatacard.");

			//Verification: Check if the placeholder value updated with the sub rule value in the metadatcard
			//-----------------------------------------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("Property1")).equalsIgnoreCase(dataPool.get("Property1Value")))
				Log.pass("Test case passed. SetValue with placeholder is added text even if placeholder is empty.", driver);
			else
				Log.fail("Test case failed. SetValue with placeholder is not added text even if placeholder is empty.[Expected property '" + dataPool.get("Property1") + "' value : '" + dataPool.get("Property1Value") + "' & Actula value: '" + metadataCard.getPropertyValue(dataPool.get("Property1")) + "']", driver);


		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally
	}//End TC_38255

	/*
	 * 39919: SetValue actions should not be triggered immediately for existing object if custom placeholder is defined
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"PlaceHolders"}, 
			description = "SetValue actions should not be triggered immediately for existing object if custom placeholder is defined.")
	public void TC_39919(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-Requisite: Login to the web access
			//--------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1: Navigate to search view
			//--------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("SearchCondition").split("::")[0], dataPool.get("SearchCondition").split("::")[1], dataPool.get("SearchCondition").split("::")[2]);
			homePage.searchPanel.clickSearch();//Clicks the search buttom

			Log.message("1. Navigated to the '" + dataPool.get("SearchCondition") + "' view");

			//Verification: Select the object and verify the object name in the list view
			//----------------------------------------------------------------------
			String[] items = homePage.listView.getAllItemNames();//Gets the list view items
			int listCount = items.length;
			String item = "";
			int randomCount = Utility.getRandomNumber(1, listCount);
			MetadataCard metadataCard = null;
			String objName = "";

			if(randomCount > 3)
				randomCount = 3;

			for(int i = 0; i < randomCount; i++)
			{
				item = homePage.listView.getItemNameByItemIndex(i);

				if(!homePage.listView.clickItem(item))//Selects the object in the view
					throw new Exception("Object '" + item + "' is not selected in the list.");

				metadataCard = new MetadataCard(driver, true);//Instantiates the right pane metadatacard
				objName = metadataCard.getPropertyValue(dataPool.get("NameProperty"));

				if(!item.contains(objName))
					Log.fail("Test case failed. Object Name modified in the metadatacard after selecting the object in the list view. [Expected name: '" + item + "' & Actual name: '" + objName + "']", driver);
			}

			Log.pass("Test case passed. SetValue actions is not triggered immediately for existing object if custom placeholder is defined.");



		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally
	}//End TC_39919

	/**
	 *  40021 : MFWA: FN119: Placeholder stops to work when rule becomes inactive
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"SetValue"},description = "MFWA: FN119: Placeholder stops to work when rule becomes inactive." )
	public void TC_40021(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();//Launches the browser

			//Login to the MFWA with valid credentials
			//----------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Select the new document object
			//---------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));

			Log.message("1. Clicked the new '" + dataPool.get("ObjectType") + "' Object from the new menu bar.");

			//Step-2 : Set the property value in the specified property name 
			//--------------------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the metadatacard
			metadatacard.setPropertyValue(dataPool.get("Property"),dataPool.get("PropertyValue"));//Set the specified property value in the metadatacard

			Log.message("2. Property '"+dataPool.get("Property")+"' is set with the value '"+ dataPool.get("PropertyValue") + "' in the metadata card", driver);

			//Step-3 : Check the Behavior for the set filter in the metadat card
			//------------------------------------------------------------------
			if(!metadatacard.getMetadataDescriptionText().equalsIgnoreCase(dataPool.get("MetadataDescription")))
				throw new Exception("Metadatacard description is not set with the value '" + dataPool.get("MetadataDescription") + "' in the metadatacard.");

			Log.message("3.1. Metadatacard description value '" + dataPool.get("MetadataDescription") + "' is displayed as expected.");

			if(!metadatacard.propertyExists(dataPool.get("PlaceHolderProperty1")))
				throw new Exception("Property '" + dataPool.get("PlaceHolderProperty") + "' is not set with the label '" + dataPool.get("PlaceHolderProperty1") + "' in the metadata card");

			if(!metadatacard.getPropertyValue(dataPool.get("PlaceHolderProperty1")).equalsIgnoreCase(dataPool.get("PlaceHolderProperty1Value")))
				throw new Exception("Property '" + dataPool.get("PlaceHolderProperty1") + "' is not set with the value '" + dataPool.get("PlaceHolderProperty1Value") + "' in the metadata card");

			Log.message("3.2. Property '" + dataPool.get("PlaceHolderProperty") + "' is not set with the label '" + dataPool.get("PlaceHolderProperty1") + "' and set with the value '" + dataPool.get("PlaceHolderProperty2Value") + "' as expected in the metadata card");

			//Step-4: Change the placeholder source property value in the metadatacard
			//------------------------------------------------------------------------
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("PropertyValue1"));//Change the soure property value in the metadatacard

			Log.message("4. Changed the source property '" + dataPool.get("Property") + "' value '" + dataPool.get("PropertyValue1") + "' in the metadatacard");

			//Verification: Check if Placeholder value is empty while clearing the source property value
			//------------------------------------------------------------------------------------------
			String result = "";

			if(!metadatacard.propertyExists(dataPool.get("PlaceHolderProperty")))
				result = "Property '" + dataPool.get("PlaceHolderProperty") + "' is set with the label '" + dataPool.get("PlaceHolderProperty1") + "' in the metadata card after change the source property value;";

			if(!metadatacard.getPropertyValue(dataPool.get("PlaceHolderProperty")).equalsIgnoreCase(dataPool.get("PlaceHolderProperty1Value")))
				result += "Property '" + dataPool.get("PlaceHolderProperty1") + "' is not set with the value '" + dataPool.get("PlaceHolderProperty1Value") + "' in the metadata card after change the source property value. Actual value: '" + metadatacard.getPropertyValue(dataPool.get("PlaceHolderProperty")) + "';";

			if(result.equals(""))
				Log.pass("Test Case Passed. Placeholder stops to work when rule becomes inactive", driver);
			else
				Log.fail("Test case failed. Placeholder not stops to work when rule becomes inactive. [Additional info. : " + result + "]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally{
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_40021

	/*
	 * 40022: Removing placeholder value (placeholder modification) should make placeholder invalid
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"PlaceHolders"}, 
			description = "Removing placeholder value (placeholder modification) should make placeholder invalid")
	public void TC_40022(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			Log.message("2. Class property is set with the value '" + dataPool.get("Class").split("::")[1] + "' in the metadatacard.");

			//Step-3 : Set the property value in the metadatacard
			//---------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Property").split("::")[0], dataPool.get("Property").split("::")[1]);//Sets the property value in the metadatacard

			if(!metadataCard.getPropertyValue(dataPool.get("Property").split("::")[0]).equalsIgnoreCase(dataPool.get("Property").split("::")[1]))
				throw new Exception("Property '" + dataPool.get("Property").split("::")[0] + "' is not set with the value '" + dataPool.get("Property").split("::")[1] + "' in the metadatacard.");

			Log.message("3. Property '" + dataPool.get("Property").split("::")[0] + "' is set with the value '" + dataPool.get("Property").split("::")[1] + "' in the metadatacard.");

			//Step-4: CHeck if the Place holder value is set for the expected property
			//------------------------------------------------------------------------
			if(!metadataCard.getPropertyValue(dataPool.get("Property1")).equalsIgnoreCase(dataPool.get("Property1Value")))
				throw new Exception("Property '" + dataPool.get("Property1") + "' is not set with the value '" + dataPool.get("Property1Value") + "' in the metadatacard.");

			Log.message("4. Property '" + dataPool.get("Property1") + "' is set with the source property '" + dataPool.get("Property").split("::")[0] + "' value '" + dataPool.get("Property1Value") + "' as expected in the metadatacard.");

			//Step-5: Clear the PlaceHolder value in the metadatacard
			//-------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Property1"), "");//Clears the placeholder in the metadatacard

			if(!metadataCard.getPropertyValue(dataPool.get("Property1")).equals(""))
				throw new Exception("Property '" + dataPool.get("Property1") + "' value is not cleared in the metadatacard.");

			Log.message("5. Property '" + dataPool.get("Property1") + "' value is cleared in the metadatacard.", driver);

			//Step-6: Again set the Name or title value in the metadatacard
			//-------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("PropertyValue").split("::")[0], "");//Empties the property value in the metadatacard
			metadataCard.setPropertyValue(dataPool.get("PropertyValue").split("::")[0], dataPool.get("PropertyValue").split("::")[1]);//Sets the property value in the metadatacard

			if(!metadataCard.getPropertyValue(dataPool.get("PropertyValue").split("::")[0]).equalsIgnoreCase(dataPool.get("PropertyValue").split("::")[1]))
				throw new Exception("Property '" + dataPool.get("PropertyValue").split("::")[0] + "' is not set with the value '" + dataPool.get("PropertyValue").split("::")[1] + "' in the metadatacard.");

			Log.message("6. Property '" + dataPool.get("Property").split("::")[0] + "' is set with the value '" + dataPool.get("Property").split("::")[1] + "' in the metadatacard.");

			//Verification: Check if place holder is set as inactive or not
			//-------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("Property1")).equals(""))
				Log.pass("Test case passed. Removing placeholder value (placeholder modification) makes placeholder invalid as expected.");
			else
				Log.fail("Test case failed. Removing placeholder value (placeholder modification) is not making placeholder invalid. [Additional info.: Property '" + dataPool.get("Property1") + "' value is not empty in the metadatacard. Actual value: '" + metadataCard.getPropertyValue(dataPool.get("Property1")) + "']", driver);

		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally
	}//End TC_40022


	/*
	 * 40023: Different source property placeholder for same property
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"PlaceHolders"}, 
			description = "Different source property placeholder for same property")
	public void TC_40023(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			Log.message("2. Class property is set with the value '" + dataPool.get("Class").split("::")[1] + "' in the metadatacard.");

			//Step-3 : Set the property value in the metadatacard
			//---------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("Property").split("::")[0], dataPool.get("Property").split("::")[1]);//Sets the property value in the metadatacard

			if(!metadataCard.getPropertyValue(dataPool.get("Property").split("::")[0]).equalsIgnoreCase(dataPool.get("Property").split("::")[1]))
				throw new Exception("Property '" + dataPool.get("Property").split("::")[0] + "' is not set with the value '" + dataPool.get("Property").split("::")[1] + "' in the metadatacard.");

			Log.message("3. Property '" + dataPool.get("Property").split("::")[0] + "' is set with the value '" + dataPool.get("Property").split("::")[1] + "' in the metadatacard.");

			//Step-4: CHeck if the Place holder value is set for the expected property
			//------------------------------------------------------------------------
			if(!metadataCard.getPropertyValue(dataPool.get("Property1")).equalsIgnoreCase(dataPool.get("Property1Value")))
				throw new Exception("Property '" + dataPool.get("Property1") + "' is not set with the value '" + dataPool.get("Property1Value") + "' in the metadatacard.");

			Log.message("4. Property '" + dataPool.get("Property1") + "' is set with the source property '" + dataPool.get("Property").split("::")[0] + "' value '" + dataPool.get("Property1Value") + "' as expected in the metadatacard.");

			//Step-5: Set the sub rule filter condition in the metadatacard
			//-------------------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("SubProperty").split("::")[0], dataPool.get("SubProperty").split("::")[1]);//Sets the property value in the metadatacard

			if(!metadataCard.getPropertyValue(dataPool.get("SubProperty").split("::")[0]).equalsIgnoreCase(dataPool.get("SubProperty").split("::")[1]))
				throw new Exception("Property '" + dataPool.get("SubProperty").split("::")[0] + "' is not set with the value '" + dataPool.get("SubProperty").split("::")[1] + "' in the metadatacard.");

			Log.message("5. Property '" + dataPool.get("SubProperty").split("::")[0] + "' is set with the value '" + dataPool.get("SubProperty").split("::")[1] + "' in the metadatacard.");

			//Verification: Check if the placeholder value updated with the sub rule value in the metadatcard
			//-----------------------------------------------------------------------------------------------
			if(metadataCard.getPropertyValue(dataPool.get("Property1")).equalsIgnoreCase(dataPool.get("SubProperty").split("::")[1]))
				Log.pass("Test case passed. Different source property placeholder for same property is working as expected.", driver);
			else
				Log.fail("Test case failed. Different source property placeholder for same property is not working as expected.[Expected property '" + dataPool.get("Property1") + "' value : '" + dataPool.get("SubProperty").split("::")[1] + "' & Actula value: '" + metadataCard.getPropertyValue(dataPool.get("Property1")) + "']", driver);


		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally
	}//End TC_40023

	/*
	 * 40033: Changing value of SetValue property triggers SetValue action from another rule
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"PlaceHolders"}, 
			description = "Changing value of SetValue property triggers SetValue action from another rule")
	public void TC_40033(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			Log.message("1. Clicked the new '" + dataPool.get("ObjectType") + "' Object from the new menu bar.");

			//Step-2 : Set the property value in the specified property name 
			//--------------------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the metadatacard
			metadatacard.setInfo(dataPool.get("Properties"));//Set the properties values in the metadatacard
			String[] properties = dataPool.get("ClearProperties").split("\n");

			for(int i = 0; i < properties.length; i++)
				metadatacard.setPropertyValue(properties[i], "");//Empties the property value in the metadatacard

			metadatacard.saveAndClose();//saves the changes

			Log.message("2. New object with created with the required values.", driver);

			//Step-3: Change the propery value in the metadatacard
			//----------------------------------------------------
			metadatacard = new MetadataCard(driver, true);//Intantiates the metadatacard
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("PropertyValue"));//Sets the property values in the metadatacard

			Log.message("3. Property '" + dataPool.get("Property") + "' is set with the value '" + dataPool.get("PropertyValue") + "' in the metadatacard");

			//Verification: Check if SetValue is triggered for the another property while modifying some other property
			//---------------------------------------------------------------------------------------------------------
			String result = "";

			if(!metadatacard.getPropertyValue(dataPool.get("Property1")).equals(""))
				result = "Property '" + dataPool.get("Property1") + "' value is not empty. [Actual value: '" + metadatacard.getPropertyValue(dataPool.get("Property1")) + "'];";

			if(!metadatacard.getPropertyValue(dataPool.get("Property2")).equals(""))
				result += "Property '" + dataPool.get("Property2") + "' value is not empty. [Actual value: '" + metadatacard.getPropertyValue(dataPool.get("Property2")) + "'];";

			if(result.equals(""))
				Log.pass("Test case passed. Changing value of SetValue property which not triggers SetValue action from another rule");
			else
				Log.fail("Test case failed. Changing value of SetValue property triggers SetValue action from another rule. ['" + result + "']", driver);

		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally
	}//End TC_40033

	/*
	 * 40036: Empty placeholder should not removed when class is changed.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"PlaceHolders"}, 
			description = "Empty placeholder should not removed when class is changed.")
	public void TC_40036(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			Log.message("1. Clicked the new '" + dataPool.get("ObjectType") + "' Object from the new menu bar.");

			//Step-2 : Set the property value in the specified property name 
			//--------------------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the metadatacard
			metadatacard.setInfo(dataPool.get("Properties"));//Set the properties values in the metadatacard

			Log.message("2.1. Class is set in the metadatacard.");

			String[] newProps = dataPool.get("AdditionalProps").split("\n"); 


			for (int i = 0; i < newProps.length; i++)
				if (!metadatacard.addNewProperty(newProps[i]))
					throw new Exception("Property '" + newProps[i] + "' is not added in the metadatacard.");

			Log.message("2.2. Additonal properties added in the metadatacard.");

			//Step-3: Check the configurations are set as expected
			//----------------------------------------------------
			String result = "";

			if(!metadatacard.getMetadataDescriptionText().equalsIgnoreCase(dataPool.get("MetadataDescription")))
				result = "Metadatacard description is not set as expected;";

			if(!metadatacard.getPropertyValue(dataPool.get("AdditionalProp1")).equalsIgnoreCase(dataPool.get("AdditionalProp1Value")))
				result += "Property '" + dataPool.get("AdditionalProp1") + "' is not set with the value '" + dataPool.get("AdditionalProp1Value") + "' in the metadatacard.;";

			if(!result.equals(""))
				throw new Exception("Configurations are not set as expected for the object type '" + dataPool.get("ObjectType") + "' filter condition in the metadatacard.[Additonal info. : " + result + "]");

			Log.message("3. Configurations are set as expected for the object type '" + dataPool.get("ObjectType") + "' in the metadatacard.", driver);

			//Step-4: Clear the property value in the metadatacard
			//----------------------------------------------------
			metadatacard.setPropertyValue(dataPool.get("AdditionalProp1"), "");//Empties the property value in the metadatacard

			Log.message("4. Property '" + dataPool.get("AdditionalProp1") + "' is emptied in the metadatacard.");

			//Step-5 : Change the class in the metadatacard
			//---------------------------------------------
			metadatacard.setPropertyValue(dataPool.get("Class1"), dataPool.get("Class1Value"));//Sets the class value in the metadatacard

			if(!metadatacard.getPropertyValue(dataPool.get("Class1")).equalsIgnoreCase(dataPool.get("Class1Value")))
				throw new Exception("Property '" + dataPool.get("Class1") + "' is not set with the value '" + dataPool.get("Class1Value") + "' in the metadatacard.");

			Log.message("5. Property '" + dataPool.get("Class1") + "' is set with the value '" + dataPool.get("Class1Value") + "' in the metadatacard.");

			//Step-6 : Check if empty property is removed from the metadatacard
			//-----------------------------------------------------------------
			result = "";

			if(metadatacard.propertyExists(dataPool.get("AdditionalProp1")))
				result += "Emptied property '" + dataPool.get("AdditionalProp1") + "' is not removed from the metadatacard.;";

			if(!metadatacard.propertyExists(dataPool.get("AdditionalProp2")))
				result += "Empty placeholder property '" + dataPool.get("AdditionalProp2") + "' is removed from the metadatacard.;";

			if(!result.equals(""))
				throw new Exception(result);

			Log.message("6. While changing the class empty placeholder property '" + dataPool.get("AdditionalProp2") + "' is not removed and emptied property '" + dataPool.get("AdditionalProp1") + "' is removed from the metadatacard.", driver);

			//Step-7: Set the source property value for the second additional property in the metadatacard
			//--------------------------------------------------------------------------------------------
			metadatacard.setPropertyValue(dataPool.get("SourceProperty"), dataPool.get("SourcePropertyValue"));//Sets the source property value in the metadatacard

			if(!metadatacard.getPropertyValue(dataPool.get("SourceProperty")).equalsIgnoreCase(dataPool.get("SourcePropertyValue")))
				throw new Exception("Source property '" + dataPool.get("SourceProperty") + "' is not set with the value '" + dataPool.get("SourcePropertyValue") + "' in the metadatacard.");

			Log.message("7. Source property '" + dataPool.get("SourceProperty") + "' is set with the value '" + dataPool.get("SourcePropertyValue") + "' in the metadatacard.");

			//Step-8: Check the placeholder value is updated while sets the source property value in the metadatacard
			//--------------------------------------------------------------------------------------------------------
			if(!metadatacard.getPropertyValue(dataPool.get("AdditionalProp2")).equalsIgnoreCase(dataPool.get("SourcePropertyValue")))
				throw new Exception("Placeholder of source property value is not updated in the property '" + dataPool.get("AdditionalProp2") + "' while updating the source property value in the metadatacard.");

			Log.message("8. Placeholder of source property value is updated in the property '" + dataPool.get("AdditionalProp2") + "' while updating the source property value in the metadatacard.", driver);

			//Step-9: Empty the placeholder value in the metadatacard
			//--------------------------------------------------------
			metadatacard.setPropertyValue(dataPool.get("AdditionalProp2"), "");//Empties the property value in the metadatacard

			Log.message("9. Property '" + dataPool.get("AdditionalProp2") + "' is emptied in the metadatacard.");

			//Step-10 : Change the class in the metadatacard
			//---------------------------------------------
			metadatacard.setInfo(dataPool.get("Properties"));//Sets the class value in the metadatacard

			if(!metadatacard.getPropertyValue(dataPool.get("Properties").split("::")[0]).equalsIgnoreCase(dataPool.get("Properties").split("::")[1]))
				throw new Exception("Property '" + dataPool.get("Properties").split("::")[0] + "' is not set with the value '" + dataPool.get("Properties").split("::")[1] + "' in the metadatacard.");

			Log.message("10. Property '" + dataPool.get("Properties").split("::")[0] + "' is set with the value '" + dataPool.get("Properties").split("::")[1] + "' in the metadatacard.");

			//Verification: Check if Emptied placeholder value is removed from the metadatacard
			//----------------------------------------------------------------------------------
			if(!metadatacard.propertyExists(dataPool.get("AdditionalProp2")))
				Log.pass("Test case passed. Emptied placeholder value property is removed when class is changed.");
			else
				Log.fail("Test case failed. Emptied placeholder value property is not removed when class is changed.", driver);

		}//End Try

		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch

		finally {
			Utility.quitDriver(driver );
		}//End Finally
	}//End TC_40036

	/**
	 *  40038 : MFWA: FN119 : Placeholder value is set to "null" when source value is removed.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"Placeholders"},description = "MFWA: FN119 : Placeholder value is set to \"null\" when source value is removed." )
	public void TC_40038(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();//Launches the browser

			//Login to the MFWA with valid credentials
			//----------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Select the new document object
			//---------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));

			Log.message("1. Clicked the new '" + dataPool.get("ObjectType") + "' Object from the new menu bar.");

			//Step-2 : Set the property value in the specified property name 
			//--------------------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the metadatacard
			metadatacard.setPropertyValue(dataPool.get("PropertyName"),dataPool.get("PropertyValue"));//Set the specified property value in the metadatacard

			Log.message("2. Property '"+dataPool.get("PropertyName")+"' is set with the value '"+ dataPool.get("PropertyValue") + "' in the metadata card", driver);

			//Step-3 : Check the Behavior for the set filter in the metadat card
			//------------------------------------------------------------------
			if(!metadatacard.getPropertyValue(dataPool.get("PlaceHolderProperty1")).equalsIgnoreCase(dataPool.get("PlaceHolderProperty1Value")))
				throw new Exception("Property '" + dataPool.get("PlaceHolderProperty1") + "' is not set with the value '" + dataPool.get("PlaceHolderProperty1Value") + "' in the metadata card");

			Log.message("3.1. Property '" + dataPool.get("PlaceHolderProperty1") + "' is set with the value '" + dataPool.get("PlaceHolderProperty1Value") + "' as expected in the metadata card");

			if(!metadatacard.propertyExists(dataPool.get("PlaceHolderProperty2")))
				throw new Exception("Property '' is not added in the metadata card");

			if(!metadatacard.getPropertyValue(dataPool.get("PlaceHolderProperty2")).equalsIgnoreCase(dataPool.get("PlaceHolderProperty2Value")))
				throw new Exception("Property '" + dataPool.get("PlaceHolderProperty2") + "' is not set with the value '" + dataPool.get("PlaceHolderProperty2Value") + "' in the metadata card");

			Log.message("3.1. Property '" + dataPool.get("PlaceHolderProperty2") + "' is added and set with the value '" + dataPool.get("PlaceHolderProperty2Value") + "' as expected in the metadata card");

			//Step-4: Clear the placeholder source property value in the metadatacard
			//------------------------------------------------------------------------
			metadatacard.setPropertyValue(dataPool.get("PlaceHolderProperty1"), "");//Clear the soure property value in the metadatacard

			Log.message("4. Cleared the source property '" + dataPool.get("PlaceHolderProperty1") + "' in the metadatacard");

			//Verification: Check if Placeholder value is empty while clearing the source property value
			//------------------------------------------------------------------------------------------
			if(metadatacard.getPropertyValue(dataPool.get("PlaceHolderProperty2")).equals(""))
				Log.pass("Test Case Passed. Placeholder value is cleared as expected when source value is removed", driver);
			else
				Log.fail("Test case failed. Placeholder value is not cleared when source value is removed. [Property '" + dataPool.get("PlaceHolderProperty2") + "' value is not cleared and set with the value '" + metadatacard.getPropertyValue(dataPool.get("PlaceHolderProperty2")) + "']", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally{
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_40038

	/**
	 *  40044 : Chain of SetValue in one rule
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"SetValue"}, description = "Chain of SetValue in one rule" )
	public void TC_40044(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();//Launches the browser

			//Login to the MFWA with valid credentials
			//----------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Select the new document object
			//---------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));

			Log.message("1. Clicked the new '" + dataPool.get("ObjectType") + "' Object from the new menu bar.");

			//Verification: Check if properties are modified in the metadatacard
			//------------------------------------------------------------------
			String result = "";
			String[] properties = dataPool.get("ExpectedProperties").split("\n");
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiates the metadatacard
			String[] expectedValues = null;
			String[] actualValues = null;
			String property = "";
			int i, j, k = 0;

			String actualValue = "", expectedValue = "";

			for (i = 0; i < properties.length; i++)
			{
				property = properties[i].split("::")[0];

				if(properties[i].split("::")[1].contains(">"))
				{
					expectedValues = (properties[i].split("::")[1]).split(">");
					actualValues = metadataCard.getPropertyValue(property).split("\n");

					for(j = 0; j < expectedValues.length; j++)
					{
						expectedValue += expectedValues[j]+";";
						Boolean exists = false;
						for(k = 0; k < actualValues.length; k++)
							if(actualValues[k].equalsIgnoreCase(expectedValues[j]))
							{
								exists = true;
								break;
							}
						if(!exists)
							actualValue += expectedValues[j]+";";
					}

					if(!actualValue.equals(""))
						result += "Property '" + property + "' is not set with the values '" + actualValue + "' in the metadatacard.[Actual property value: '" + expectedValue + "'];";
				}
				else
				{
					expectedValues = (properties[i].split("::")[1]).split(",");
					expectedValue = "";

					for(int l = 0; l < expectedValues.length; l++)
					{
						if(expectedValues[l].contains("Current user:"))
							expectedValues[l] += " "+userName;

						expectedValue += expectedValues[l];

						if(l != (expectedValues.length-1))
							expectedValue += ",";
					}

					if(!metadataCard.getPropertyValue(property).equalsIgnoreCase(expectedValue))
						result += "Property '" + property + "' is not set with the value '" + expectedValue + "' in the metadatacard.[Actual property value: '" + metadataCard.getPropertyValue(property) + "'];";
				}
			}

			if(result.equals(""))
				Log.pass("Test case passed. Chain of SetValue in one rule works as expected.");
			else
				Log.fail("Test case failed. Chain of SetValue in one rule not works as expected. [Additional info. : " + result + "]", driver);

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally{
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_40044

	/**
	 *  40048 : Triggering SetValue with IsForced & placeholders parameters
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"SetValue"}, description = "Triggering SetValue with IsForced & placeholders parameters" )
	public void TC_40048(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();//Launches the browser

			//Login to the MFWA with valid credentials
			//----------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Select the new document object
			//---------------------------------------
			homePage.menuBar.clickNewMenuItem(dataPool.get("ObjectType"));

			Log.message("1. Clicked the new '" + dataPool.get("ObjectType") + "' Object from the new menu bar.");

			//Step-2 : Create the object with the required values 
			//---------------------------------------------------
			MetadataCard metadatacard = new MetadataCard(driver);//Instantiate the metadatacard
			metadatacard.addNewProperty(dataPool.get("AddProperty1"));//Adds the new property in the metadatacard
			metadatacard.addNewProperty(dataPool.get("AddProperty2"));//Adds the new property in the metadatacard
			metadatacard.setInfo(dataPool.get("Properties"));//Sets the property values in the metadatacard
			metadatacard.saveAndClose();//Creates the object in the view

			//Step-3 : Change the property value and check the metadatacard
			//-------------------------------------------------------------
			metadatacard = new MetadataCard(driver, true);//Instantiates the metadatacard

			metadatacard.setPropertyValue(dataPool.get("NameProperty"), dataPool.get("NamePropertyValue"));//Sets the property value in the metadatacard

			if(!metadatacard.getPropertyValue(dataPool.get("NameProperty")).equalsIgnoreCase(dataPool.get("NamePropertyValue")))
				throw new Exception("Property '" + dataPool.get("NameProperty") + "' is not set with the value '" + dataPool.get("NamePropertyValue") + "' in the metadatacard.");

			Log.message("3.1. Property '" + dataPool.get("NameProperty") + "' is set with the value '" + dataPool.get("NamePropertyValue") + "' in the metadatacard.");

			String[] properties = dataPool.get("CheckProperties").split("\n");

			for (int i = 0; i < properties.length; i++)
				if(!metadatacard.getPropertyValue(properties[i].split("::")[0]).equalsIgnoreCase(properties[i].split("::")[1]))
					throw new Exception("Property '" + properties[i].split("::")[0] + "' is modified while modifing the property '" + dataPool.get("NameProperty") + "' in the metadatacard.[Modified property value: '" + metadatacard.getPropertyValue(properties[i].split("::")[0]) + "']");

			Log.message("3.2. Other properties are not modified while modifing the '" + dataPool.get("NameProperty") + "' property in the metadatacard", driver);

			//Step-4 : Change the property value and check the metadatacard
			//-------------------------------------------------------------
			metadatacard.setPropertyValue(dataPool.get("TelephoneProperty"), dataPool.get("TelephonePropertyValue"));//Sets the property value in the metadatacard

			if(!metadatacard.getPropertyValue(dataPool.get("TelephoneProperty")).equalsIgnoreCase(dataPool.get("TelephonePropertyValue")))
				throw new Exception("Property '" + dataPool.get("TelephoneProperty") + "' is not set with the value '" + dataPool.get("TelephonePropertyValue") + "' in the metadatacard.");

			Log.message("4.1. Property '" + dataPool.get("TelephoneProperty") + "' is set with the value '" + dataPool.get("TelephonePropertyValue") + "' in the metadatacard.");

			properties = dataPool.get("CheckProperties1").split("\n");

			for (int i = 0; i < properties.length; i++)
				if(!metadatacard.getPropertyValue(properties[i].split("::")[0]).equalsIgnoreCase(properties[i].split("::")[1]))
					throw new Exception("Property '" + properties[i].split("::")[0] + "' is modified while modifing the property '" + dataPool.get("TelephoneProperty") + "' in the metadatacard.[Modified property value: '" + metadatacard.getPropertyValue(properties[i].split("::")[0]) + "']");

			Log.message("4.2. Other properties are not modified while modifing the '" + dataPool.get("TelephoneProperty") + "' property in the metadatacard", driver);

			//Step-5 : Set the filter condition and check the metadatacard
			//-------------------------------------------------------------
			metadatacard.setPropertyValue(dataPool.get("DepartmentProperty"), dataPool.get("DepartmentPropertyValue"));//Sets the property value in the metadatacard

			if(!metadatacard.getPropertyValue(dataPool.get("DepartmentProperty")).equalsIgnoreCase(dataPool.get("DepartmentPropertyValue")))
				throw new Exception("Property '" + dataPool.get("DepartmentProperty") + "' is not set with the value '" + dataPool.get("DepartmentPropertyValue") + "' in the metadatacard.");

			Log.message("5. Property '" + dataPool.get("DepartmentProperty") + "' is set with the value '" + dataPool.get("DepartmentPropertyValue") + "' in the metadatacard.");

			//Verification: Check if properties are modified in the metadatacard
			//------------------------------------------------------------------
			String result = "";
			properties = dataPool.get("ExpectedProperties").split("\n");

			for (int i = 0; i < properties.length; i++)
				if(!metadatacard.getPropertyValue(properties[i].split("::")[0]).equalsIgnoreCase(properties[i].split("::")[1]))
					result += "Property '" + properties[i].split("::")[0] + "' is not set with the value '" + properties[i].split("::")[1] + "' in the metadatacard.[Actual property value: '" + metadatacard.getPropertyValue(properties[i].split("::")[0]) + "'];";

			if(result.equals(""))
				Log.pass("Test case passed. Triggering SetValue with IsForced & placeholders parameters works as expected.");
			else
				Log.fail("Test case failed. Triggering SetValue with IsForced & placeholders parameters not works as expected. [Additional info. : " + result + "]", driver);


		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch
		finally{
			Utility.quitDriver(driver);
		}//End finally
	}//End TC_40048

}//End class PlaceHolders

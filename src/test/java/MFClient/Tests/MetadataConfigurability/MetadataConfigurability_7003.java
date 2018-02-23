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
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class MetadataConfigurability_7003 {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String userName = null;
	public static String userFullName = null;
	public static String password = null;
	public static String testVault = null;
	public static String configURL = null;
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
			Utility.restoreTestVault();

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
	}//End cleanApp

	/**
	 *  1.1.28A : Verify the  %CURRENT_USER% used in text and multi line text for newly created object
	 */

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",
			groups = {"M7003MetadataConfigurability"},description = "Verify %CURRENT_USER% used in text and multi line text for newly created object" )
	public void SprintTest1_1_28A(HashMap<String,String> dataValues, String driverType) throws Exception {


		driver = WebDriverUtils.getDriver();

		try {
			//Login to the MFWA with valid credentials
			//----------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			//Step-1 : Create the new document object
			//---------------------------------------
			homePage.taskPanel.clickItem(Caption.ObjecTypes.Document.Value);

			Log.message("1. Create the new 'Document' Object from the taskpanel.");

			//Step-2 : Select the 'Unclassifed document' from the metadata template
			//---------------------------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver);//Instantiate the MetadataCard
			metadataCard.setTemplateUsingClass("Unclassified Document");//Select the 'Unclassified Document'

			Log.message("2. Selected the template from the newly created metadata card.");

			//Step-3 : get metadatacard description value
			//-------------------------------------------
			String Property1 =  metadataCard.getPropertyValue(dataPool.get("PropertyName1"));
			String Property2 = metadataCard.getPropertyValue(dataPool.get("PropertyName2"));

			//Verification : Verify if the property value is set value as expected
			//---------------------------------------------------------------------
			if((Property1.equals(dataPool.get("Propertyvalue1"))) && (Property2.equals(dataPool.get("Propertyvalue2"))))
				Log.pass("Test Case Passed. Property value is set as expected.",driver);
			else
				Log.fail("Test Case Failed. Property value is not set as expected.", driver);

		}//End try
		catch(Exception e){
			Log.exception(e);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}//End SprintTest1_1_28A


}

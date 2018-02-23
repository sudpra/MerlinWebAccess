package MFClient.Tests.Permissions;

import genericLibrary.DataProviderUtils;
import genericLibrary.Log;
import genericLibrary.WebDriverUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.openqa.selenium.WebDriver;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import MFClient.Pages.HomePage;
import MFClient.Pages.LoginPage;
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

public class ExtendedMetadataDrivenOff {

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
	}//End cleanApp


	/**
	 * ExtendedMetadataDrivenOff_27018 : Verify 'Custom' permission value are not changed for the change class option
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {"Extended metadata driven permission - OFF"},
			description = "Verify 'Custom' permission value are not changed for the change class option.")
	public void ExtendedMetadataDrivenOff_27018(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {

			Log.testCaseInfo(Utility.getMethodDescription() + "[" + driverType.toUpperCase() + "]", className + " - " + Utility.getMethodName(), className, productVersion);

			driver = WebDriverUtils.getDriver();

			//Pre-Requisite : Login to MFiles web access
			//------------------------------------------
			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, dataPool.get("User"), dataPool.get("Password"), testVault); //Launched driver and logged in

			//Step-1 : Navigate to any view and then select any existing object in the view
			//-----------------------------------------------------------------------------
			String viewtonavigate = SearchPanel.searchOrNavigatetoView(driver, dataPool.get("ViewToNavigate"), dataPool.get("ObjectName"));//Navigate to the specific view
			homePage.listView.clickItem(dataPool.get("ObjectName"));//Selects the object in the view

			Log.message("1. Navigated to " + viewtonavigate + " and selected the object \"" + dataPool.get("ObjectName") +  "\" in search view.", driver);

			//Step-2 : Modify the class for the selected objects
			//--------------------------------------------------
			MetadataCard metadataCard = new MetadataCard(driver,true);//Instantiate the right pane metadatacard

			//Verify if Permission value is set as expected
			//---------------------------------------------
			if(!metadataCard.getPermission().equalsIgnoreCase("Custom"))
				throw new Exception("Invalid Test Data. Expected : 'Custom' Permission is not set for selected object : " + dataPool.get("ObjectName"));

			//Change the class name in right pane metadatacard
			//------------------------------------------------
			metadataCard.setPropertyValue(dataPool.get("PropName"), dataPool.get("PropValue"));
			metadataCard.saveAndClose();//Save the changes in right pane metadatacard

			Log.message("2. Changed : " + dataPool.get("PropName") + " Property value as : '" + dataPool.get("PropValue") + "' & Save the changes in opened metadatacard. ", driver);

			//Verification : Verify if 'Custom' permission is not change when change class option
			//-----------------------------------------------------------------------------------
			if(metadataCard.getPermission().equalsIgnoreCase("Custom"))
				Log.pass("Test Case Passed. 'Custom' permission is not changed when changing the Property '" + dataPool.get("PropName")  + "' value as : " + dataPool.get("PropValue"), driver);
			else
				Log.fail("Test Case Failed. 'Custom' permission is changed when changing the 'Class' property value : " + dataPool.get("PropValue"), driver);


		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}//End ExtendedMetadataDrivenOff_27018







}

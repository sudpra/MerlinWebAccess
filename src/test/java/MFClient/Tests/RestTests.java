package MFClient.Tests;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import org.testng.xml.XmlTest;

import MFClient.Wrappers.RestAuthenticator;
import MFClient.Wrappers.RestProcessor;
import MFClient.Wrappers.RestResponse;
import MFClient.Wrappers.RestUtils;
import MFClient.Wrappers.RestValidator;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class RestTests {


	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String configURL = null;
	public static String userName = null;
	public static String password = null;
	public static String testVault = null;
	public static String productVersion = null;
	public static String userFullName = null;
	public static String driverType = null;

	public static String className = null;

	/**
	 * onSuiteStart : Before Suite method to clean screenshots folder and backup the test vault before starting the execution
	 * @throws Exception 
	 */
	@BeforeSuite (alwaysRun=true)
	public void onSuiteStart(ITestContext context) throws Exception {

		try {

			Log.cleanScreenShotFolder(context);
			Log.deleteDownloadedFilesFolder(context);
			Utility.installApplication();
			Utility.backupTestVault();


		} //End try
		catch (Exception e) {
			throw e;
		}	//End catch	

	} //End onSuiteStart

	/**
	 * init : Before Class method to perform initial operations.
	 */
	@BeforeClass (alwaysRun=true)
	public void init() throws Exception {

		try {

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			xlTestDataWorkBook = xmlParameters.getParameter("TestData");
			loginURL = xmlParameters.getParameter("webSite");
			configURL = xmlParameters.getParameter("ConfigurationURL");
			userName = xmlParameters.getParameter("UserName");
			password = xmlParameters.getParameter("Password");
			testVault = xmlParameters.getParameter("VaultName");		
			userFullName = xmlParameters.getParameter("UserFullName");
			driverType = xmlParameters.getParameter("driverType");
			className = this.getClass().getSimpleName().toString().trim();
			productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim();

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

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Rest", "RestGet"}, 
			description = "Validate HTTP GET responses of M-Files")
	public void restGetRequest(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null;

		try {

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			Log.testCaseInfo(Utility.getMethodDescription() + " - " + dataPool.get("TestName") , "HTTP GET - " + dataPool.get("TestName"), className, productVersion);
			String vaultGuid = RestUtils.getVaultGuidFromMFServer("Sample Vault");

			//Step-1 : Execute HTTP Rest Get response
			//----------------------------------------
			RestAuthenticator restAuthenticator = new RestAuthenticator();
			RestProcessor restProcessor = restAuthenticator.getAuthenticationTokenFromVault( vaultGuid );
			System.out.println(dataPool.get("RestURL"));
			RestResponse restResponse = restProcessor.get( dataPool.get("RestURL") );

			Log.message( "1. Rest Get response for URL '" + dataPool.get("RestURL") + "' is executed." );

			//Verification : Verify if REST GET response is as expected
			//---------------------------------------------------------
			RestValidator restValidator = new RestValidator(restResponse);

			if (restValidator.verifyResponse( dataPool.get("ResponseCode"), 
					dataPool.get("Headers"), dataPool.get("Message"), dataPool.get("Body") ))
				Log.pass( "Test case Passed. Rest URL '" + dataPool.get("RestURL") + "' get request is as expected. " + restValidator.response());
			else
				Log.fail( "Test case Passed. Rest URL '" + dataPool.get("RestURL") + "' get request is as expected. " + restValidator.response());

		}//End try
		catch(Exception e){
			Log.exception(e, driver);
		}//End catch

		finally {
			Log.endTestCase();
		}

	}//End restGetRequest

	@Test
	public void test() throws Exception {

		String vaultGuid = RestUtils.getVaultGuidFromMFServer("Sample Vault");
		System.out.println("Vault Guid : " + vaultGuid);

		RestAuthenticator restAuthenticator = new RestAuthenticator();

		RestProcessor restProcessor = restAuthenticator.getAuthenticationTokenFromVault( vaultGuid );
		RestResponse rp = restProcessor.get( "/REST/structure/objecttypes/1.aspx" );
		System.out.println("Response Body : " + rp.getResponseBody());
		System.out.println("Response code : " + rp.getResponseCode());
		System.out.println("Response Message : " + rp.getResponseMessage());
		System.out.println("Response Header : " + rp.getHeader( "Content-Type" ));
		System.out.println("Response Headers : " + rp.getHeaders().toString());

	}


} //End class RestTests
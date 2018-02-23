package MFClient.Tests;

import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.Reporter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;
import org.zaproxy.clientapi.core.ClientApi;

import MFClient.Pages.LoginPage;
import MFClient.Wrappers.Utility;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.WebDriverUtils;
import genericLibrary.ZAPFactory;

@Listeners(EmailReport.class)
public class SecurityTest_MFiles {
	//private static final String VaultName = null;
	public int ZAP_SESSION_PORT =0;
	public static String ZAP_HOSTNAME=null;
	public static String ZAP_SESSION_IP=null;
	public static String ZAP_URI_PORT=null;
	public static String APPLICATION_URL=null;
	public static String USERNAME=null;
	public static String PASSWORD=null;
	public static String VAULT_NAME=null;
	public static String VAULT_ID=null;
	public static String DEFAULTWEBPAGE=null;
	public static String IS_SECURITY_TEST=null;
	public static String xlTestDataWorkBook = null;
	public static String siteNode="http://localhost";

	@BeforeSuite
	public void cleanScreenShots(ITestContext context) throws Exception {
		Log.cleanScreenShotFolder(context);
		Log.deleteDownloadedFilesFolder(context);
		//			Utility.backupTestVault();

	}

	/**
	 * init : Before Class method to perform initial operations.
	 */
	@BeforeClass (alwaysRun=true)
	public void init() throws Exception {

		try {
			XmlTest xmlTestNG = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();

			ZAP_SESSION_IP = xmlTestNG.getParameter("zapSessionIP").toString();
			ZAP_SESSION_PORT = Integer.parseInt(xmlTestNG.getParameter("zap_SESSION_PORT"));
			ZAP_URI_PORT= xmlTestNG.getParameter("zapURI_PORT");
			ZAP_HOSTNAME = xmlTestNG.getParameter("zapHostName").toString();
			IS_SECURITY_TEST=xmlTestNG.getParameter("securityTest").toString();

			xlTestDataWorkBook = xmlTestNG.getParameter("smokeTestDataSheet");
			APPLICATION_URL = xmlTestNG.getParameter("webSite").toString();
			USERNAME = xmlTestNG.getParameter("UserName").toString();
			PASSWORD = xmlTestNG.getParameter("Password").toString();
			VAULT_NAME = xmlTestNG.getParameter("VaultName").toString();
			VAULT_ID = xmlTestNG.getParameter("VaultID").toString();
			DEFAULTWEBPAGE=xmlTestNG.getParameter("defaultPage").toString();


		} //End try

		catch(Exception e) {
			if (e.getClass().toString().contains("NullPointerException")) 
				throw new Exception ("Test data sheet does not exists.");
			else
				throw e;
		} //End catch
	}

	/**
	 * 1.1.1 : Security Test for Default Views of Web Client
	 */
	@Test(description="1.1.1 : Security Test for Taskpane Default Views of Web Client")
	public void WebSecurityTest1_1_1(ITestContext context) throws Exception {
		// Get the web driver instance
		final WebDriver driver = WebDriverUtils.getDriver("zap",0);
		ZAPFactory zap = new ZAPFactory(driver,IS_SECURITY_TEST);
		ClientApi api = new ClientApi(ZAP_HOSTNAME, ZAP_SESSION_PORT);

		zap.startZAP();
		zap.startSession(ZAP_HOSTNAME, ZAP_SESSION_PORT);
		try {
			// Step-1: Navigate to Login Page
			driver.get(APPLICATION_URL);
			Log.message("Step-1: M-Files Web Access Login Page launched successfully!!");

			//login to Web Access client
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToWebApplication(USERNAME, PASSWORD, VAULT_NAME);
			Log.message("Step-2: Successfully logged into  M-Files Web Access Application !!");

			api.activeScanSiteInScope(ZAP_URI_PORT);
			api.addIncludeInContext("Default Context",siteNode);
			api.includeOneMatchingNodeInContext("Default Context",siteNode);

			//Navigate to 'Ássigned to ME' view
			driver.get(DEFAULTWEBPAGE+"#"+VAULT_ID+"/views/V9");
			Log.message("Step-3: Successfully logged into  'Assigned TO ME' view !!");

			//Navigate to 'CheckedOut to ME' view
			driver.get(DEFAULTWEBPAGE+"#"+VAULT_ID+"/views/V5");
			Log.message("Step-4: Successfully logged into  'Checked Out TO ME' view !!");

			//Navigate to 'Recently Accessed by ME' view
			driver.get(DEFAULTWEBPAGE+"#"+VAULT_ID+"/views/V14");
			Log.message("Step-5: Successfully logged into  'Recently Accessed by ME' view !!");

			//Navigate to 'Favorites' view
			driver.get(DEFAULTWEBPAGE+"#"+VAULT_ID+"/views/V15");
			Log.message("Step-5: Successfully logged into  'Favorites' view !!");

			// Spider Scan
			zap.initiateSpiderScan(api, ZAP_URI_PORT);
			zap.saveSessionHTML(api, "ZAPSecuritySpiderScanReport_FavoritesVIEW");
			Log.securityMessage("--- Spider Scan at M-Files Web Site 'Favorites' view ", driver, "ZAPSecuritySpiderScanReport_FavoritesVIEW");
			Log.message("Spider Scan Security Error Message Details at 'Favorites' view : " + zap.checkErrors(api));

			//Active Scan
			zap.initiateActiveScan(api, ZAP_URI_PORT, "50");
			zap.saveSessionHTML(api, "ZAPSecurityActiveScanReport_FavoritesVIEW");
			Log.securityMessage("--- Active Scan on M-Files Web Site 'Favorites'", driver, "ZAPSecurityActiveScanReport_FavoritesVIEW");
			Log.message("Active Scan Security Error Message Details at Favorites VIEW: " + zap.checkErrors(api));

			//			zap.initiateAjaxScan(api, ZAP_URI_PORT);

			// Step-3: Logout of application
			if (!Utility.logOut(driver)) //Logs out from web access default page
				throw new Exception("Log out is not successful.");
			else
				Log.message("Step-6: Logged-out of M-Files Web Accss application");


		} // End try

		catch (Exception e) {
			Log.exception(e);
			zap.saveSession(api,"secure");
			zap.saveSessionHTML(api, "ZAPSecurityScanReport");
			zap.stopZAP(ZAP_SESSION_IP, ZAP_SESSION_PORT);
		} // End catch

		finally {
			zap.stopZAP(ZAP_SESSION_IP, ZAP_SESSION_PORT);
			driver.quit();
		} // End finally

	} // End SprintTest1_1_1

}

package MFClient.Tests;

import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.WebDriverUtils;

import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.Reporter;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import org.testng.xml.XmlTest;

import MFClient.Pages.HomePage;
import MFClient.Pages.LoginPage;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class OnSuiteStart {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String configURL = null;
	public static String userName = null;
	public static String password = null;
	public static String testVault = null;
	public static String className = null;
	public static String productVersion = null;
	public String methodName = null;
	public String userFullName = null;
	private WebDriver driver = null;


	/**
	 * onSuiteStart : Before Suite method to clean screenshots folder and backup the test vault before starting the execution
	 * @throws Exception 
	 */
	@BeforeSuite (alwaysRun=true)
	public void onSuiteStart(ITestContext context) throws Exception {

		try {

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			xlTestDataWorkBook = xmlParameters.getParameter("TestData");
			loginURL = xmlParameters.getParameter("webSite");
			configURL = xmlParameters.getParameter("ConfigurationURL");
			userName = xmlParameters.getParameter("UserName");
			userFullName = xmlParameters.getParameter("UserFullName");
			password = xmlParameters.getParameter("Password");
			testVault = xmlParameters.getParameter("VaultName");

			//Makes Clean screenshots folder to store screenshots of current execution
			Log.cleanScreenShotFolder(context);

			//Makes Clean downloaded files folder to store any downloaded files of current execution
			Log.deleteDownloadedFilesFolder(context);

			//Installs M-Files Application
			Utility.installApplication();

			//Backups test vault to the Vault location in project
			Utility.backupTestVault();

			//Restores Test Vault from the vault location
			Utility.restoreTestVault();

			//Configures Users to the vault
			Utility.configureUsers(xlTestDataWorkBook);

			int maxAttempts = 2;
			HomePage homePage = null;

			for(int i=1; i <= maxAttempts; ++i){

				try{

					//Performs first login the application after to the installation, to ensure if login is successful for next attempts.
					driver = WebDriverUtils.getDriver();
					homePage = LoginPage.launchDriverAndLogin(driver, true);

					if (homePage != null) 
						break;
				}
				catch (Exception e) {

					if (driver != null) //Quits driver and closes browser
						driver.quit();

					continue;
				}
			}

			//Destroys the users
			Utility.destroyUsers(xlTestDataWorkBook);

			//Destroys the Restored vault
			Utility.destroyTestVault();

			//Checks if default page is loaded successfully after the login
			if (homePage == null)
				throw new Exception("Login to Web access is not successful");

		} //End try
		catch (Exception e) {
			throw e;
		}	//End catch	

		finally {

			if (driver != null) //Quits driver and closes browser
				driver.quit();
		} //End finally

	} //End onSuiteStart

} //End Class OnSuiteStart
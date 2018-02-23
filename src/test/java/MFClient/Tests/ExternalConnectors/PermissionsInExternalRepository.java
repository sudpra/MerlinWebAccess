package MFClient.Tests.ExternalConnectors;

import java.lang.reflect.Method;
import java.util.HashMap;

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
import MFClient.Wrappers.ExternalRepositoryHelper;
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.Utility;
import genericLibrary.DataProviderUtils;
import genericLibrary.Log;
import genericLibrary.TestMethodWebDriverManager;

public class PermissionsInExternalRepository {

	//TODO: Remove all unnecessary variables
	public String xlTestDataWorkBook = null;
	public String loginURL = null;
	public String configURL = null;
	public String userName = null;
	public String userFullName = null;
	public String password = null;
	public String testVault = null;
	public String className = null;
	public String productVersion = null;
	public String extnViewName = null;
	public String methodName = null;

	private TestMethodWebDriverManager driverManager = null;

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
			userFullName = xmlParameters.getParameter("UserFullName");
			password = xmlParameters.getParameter("Password");
			testVault = xmlParameters.getParameter("VaultName");
			extnViewName = xmlParameters.getParameter("ExternalViewName");
			className = this.getClass().getSimpleName().toString().trim();

			driverManager = new TestMethodWebDriverManager();

			if (xmlParameters.getParameter("driverType").equalsIgnoreCase("IE"))
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim() + xmlParameters.getParameter("driverVersion").trim();
			else
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim();


			Utility.restoreTestVault();
			Utility.configureUsers(xlTestDataWorkBook);

			ExternalRepositoryHelper.installExternalRepository(className, xlTestDataWorkBook);

			//Log in to the external repository as a specific user
			ExternalRepositoryHelper.userSpecificLogin();

		} //End try

		catch(Exception e) {
			if (e instanceof SkipException || e.getMessage().contains("isExternalViewExists")) 
				throw new SkipException(e.getMessage());
			else if (e.getClass().toString().contains("NullPointerException")) 
				throw new Exception ("Test data sheet does not exists.");
			else
				throw e;
		} //End catch

	} //End init


	/**
	 * ReadDenyExtRepoObjInExternalViewOrMFilesViewTestBody : This is the test body for checking that object is not visible in object listing of views.
	 * Test case steps do not differ whether using external view or M-Files view BUT they are conceptually quite different things.
	 * @param dataPool - test data of the caller test case. Test data should contain Path to either external view or normal M-Files view.
	 */
	private void ReadDenyExtRepoObjInExternalViewOrMFilesViewTestBody(HashMap<String,String> dataPool) throws Exception{

		WebDriver driver = null;

		try {

			driver = driverManager.startTesting(Utility.getMethodName());

			//Login to MFWA
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("Navigating to path ('" + dataPool.get("Path") + "').", driver);

			//Navigate to a view
			if( !homePage.listView.navigateThroughView(dataPool.get("Path")) )
				throw new Exception("Navigation to path '" + dataPool.get("Path") + "' failed");

			String objectName = dataPool.get("Object");

			Log.message("Verifying that object " + objectName + " is not visible in without read permission.", driver);

			if (!homePage.listView.isItemExists(objectName))
				Log.pass("Test Case passed. Object " + objectName + " was not found when navigated to path '" + dataPool.get("Path") + "' because the mapped user does not have read permission to it.");
			else
				Log.fail("Test case failed. Object " + objectName + " was found in path '" + dataPool.get("Path") + "' even though the mapped user does not have read permission to it.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}

	/**
	 * WriteDenyExtRepoObjInExternalViewOrMFilesViewTestBody : This is the test body for checking that object cannot be modified BUT can be read
	 * when navigating to external or M-Files view. Test case steps do not differ whether using external view or M-Files view BUT they are conceptually 
	 * quite different things.
	 * @param dataPool - test data of the caller test case. Test data should contain Path to either external view or normal M-Files view.
	 */
	private void WriteDenyExtRepoObjInExternalViewOrMFilesViewTestBody(HashMap<String,String> dataPool) throws Exception{

		WebDriver driver = null; 

		try {

			driver = driverManager.startTesting(Utility.getMethodName());

			//Login to MFWA
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("Navigating to path ('" + dataPool.get("Path") + "').", driver);

			//Navigate to a view
			if( !homePage.listView.navigateThroughView(dataPool.get("Path")) )
				throw new Exception("Navigation to path '" + dataPool.get("Path") + "' failed");

			String objectName = dataPool.get("Object");

			homePage.listView.clickItem(objectName);

			MetadataCard metadatacard = new MetadataCard(driver, true);

			String result = "";

			result += ExternalRepositoryHelper.verifyPropertyNotEditable(metadatacard, "Class");
			result += ExternalRepositoryHelper.verifyPropertyNotEditable(metadatacard, dataPool.get("Property"));

			String expectedPageCount = dataPool.get("PageCount");

			result += ExternalRepositoryHelper.verifyPreview(driver, homePage, objectName, expectedPageCount);

			if (result.equals(""))
				Log.pass("Test Case passed. Object " + objectName + " cannot be modified when navigated to path '" + dataPool.get("Path") + "' because the mapped user does not have write permission to it BUT file can be accessed by using preview.");
			else
				Log.fail("Test case failed. Details: " + result, driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}

	/**
	 * WriteDenyFolderObjInExternalViewOrMFilesViewTestBody : This is the test body for checking that folder object cannot be modified BUT can be accessed
	 * when navigating to external or M-Files view. Test case steps do not differ whether using external view or M-Files view BUT they are conceptually 
	 * quite different things.
	 * @param dataPool - test data of the caller test case. Test data should contain Path to either external view or normal M-Files view.
	 */
	private void WriteDenyFolderObjInExternalViewOrMFilesViewTestBody(HashMap<String,String> dataPool) throws Exception{

		WebDriver driver = null; 

		try {

			driver = driverManager.startTesting(Utility.getMethodName());

			//Login to MFWA
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("Navigating to path ('" + dataPool.get("Path") + "').", driver);

			//Navigate to a view
			if( !homePage.listView.navigateThroughView(dataPool.get("Path")) )
				throw new Exception("Navigation to path '" + dataPool.get("Path") + "' failed");

			String objectName = dataPool.get("Object");

			String controlObject = dataPool.get("ControlObject");
			int expectedCount = Integer.parseInt(dataPool.get("ObjectCountInFolder"));

			String result = "";

			result += ExternalRepositoryHelper.verifyFolderAccess(driver, homePage, objectName, controlObject, expectedCount);

			homePage.listView.clickItem(objectName);

			MetadataCard metadatacard = new MetadataCard(driver, true);

			result += ExternalRepositoryHelper.verifyPropertyNotEditable(metadatacard, "Class");
			result += ExternalRepositoryHelper.verifyPropertyNotEditable(metadatacard, dataPool.get("Property"));

			if (result.equals(""))
				Log.pass("Test Case passed. Folder object " + objectName + " cannot be modified when navigated to path '" + dataPool.get("Path") + "' because the mapped user does not have write permission to it BUT the folder can be accessed.");
			else
				Log.fail("Test case failed. Details: " + result, driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}

	/**
	 * writeLogTestCaseStart : Call Log.testCaseInfo before running the test
	 */
	@BeforeMethod (alwaysRun=true)
	public void writeLogTestCaseStart(Method method) throws Exception {

		try {

			Test test = method.getAnnotation(Test.class);

			XmlTest xmltest = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String driverType = xmltest.getParameter("driverType");

			Log.testCaseInfo(test.description() + "[" + driverType.toUpperCase() + "]", className + " - " +  method.getName(), className, productVersion);

		} //End try

		catch(Exception e) {
			if (e.getClass().toString().contains("NullPointerException")) 
				throw new Exception ("Test data sheet does not exists.");
			else
				throw e;
		} //End catch		
	} //End writeLogTestCaseStart

	@AfterMethod (alwaysRun=true)
	public void quitDrivers(Method method) throws Exception {

		driverManager.quitTestMethodWebDrivers(method.getName());
		Log.endTestCase();//Ends the test case
	}

	/**
	 * cleanApp : At after class method to destroy the vault used in the class
	 */
	@AfterClass (alwaysRun = true)
	public void cleanApp() throws Exception{

		try {

			Utility.clearExternalRepository(className);//Clears the external repository created for this class
			Utility.destroyUsers(xlTestDataWorkBook);//Destroys the user in MFServer
			Utility.destroyTestVault();//Destroys the Vault in MFServer

		}//End try

		catch(Exception e){
			throw e;
		}//End Catch
	}//End cleanApp

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Unmanaged and promoted objects cannot be seen in quick search results if Windows ACL denies read permission from user", priority = 1)
	public void QuickSearchReadDenyExtRepoObj(HashMap<String,String> dataPool, String driverType) throws Exception {

		WebDriver driver = null; 

		try {

			driver = driverManager.startTesting(Utility.getMethodName());

			//Login to MFWA
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			homePage.searchPanel.quickSearch(dataPool.get("SearchWord"));

			Log.message("Verifying that objects from external repository are not visible without read permission.", driver);

			String[] objectNames = dataPool.get("Objects").split("\n");

			String result = "";
			for (int i = 0; i < objectNames.length; i++)
			{
				Log.message("Verifying that object '" + objectNames[i] + "' is not visible.");

				if(homePage.listView.isItemExists(objectNames[i]))
					result += "'Object " + objectNames[i] + " was found in quick search even though the mapped user does not have read permission to it.'";
			}

			if (result.equals(""))
				Log.pass("Test Case passed. Objects from external repository were not found in quick search because the mapped user does not have read permission to them.");
			else
				Log.fail("Test case failed. Details " + result, driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Unmanaged and promoted objects in quick search results cannot be modified BUT file can read when Windows ACL write permission is denied from user", priority = 2)
	public void QuickSearchWriteDenyExtRepoObj(HashMap<String,String> dataPool, String driverType) throws Exception {

		WebDriver driver = null; 

		try {

			driver = driverManager.startTesting(Utility.getMethodName());

			//Login to MFWA
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			homePage.searchPanel.quickSearch(dataPool.get("SearchWord"));

			String objectName = dataPool.get("Object");

			homePage.listView.clickItem(objectName);

			MetadataCard metadatacard = new MetadataCard(driver, true);

			String result = "";

			result += ExternalRepositoryHelper.verifyPropertyNotEditable(metadatacard, "Class");
			result += ExternalRepositoryHelper.verifyPropertyNotEditable(metadatacard, dataPool.get("Property"));

			String expectedPageCount = dataPool.get("PageCount");

			result += ExternalRepositoryHelper.verifyPreview(driver, homePage, objectName, expectedPageCount);

			if (result.equals(""))
				Log.pass("Test Case passed. Object " + objectName + " in external repository cannot be modified in quick search results because the mapped user does not have write permission to it BUT file can be accessed by using preview.");
			else
				Log.fail("Test case failed. Details: " + result, driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Unmanaged and promoted folder objects in quick search results cannot be modified BUT the folder can be accessed when Windows ACL write permission is denied from user", priority = 3)
	public void QuickSearchWriteDenyFolderObj(HashMap<String,String> dataPool, String driverType) throws Exception {

		WebDriver driver = null; 

		try {

			driver = driverManager.startTesting(Utility.getMethodName());

			//Login to MFWA
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			homePage.searchPanel.quickSearch(dataPool.get("SearchWord"));

			String objectName = dataPool.get("Object");

			String controlObject = dataPool.get("ControlObject");
			int expectedCount = Integer.parseInt(dataPool.get("ObjectCountInFolder"));

			String result = "";

			result += ExternalRepositoryHelper.verifyFolderAccess(driver, homePage, objectName, controlObject, expectedCount);

			homePage.listView.clickItem(objectName);

			MetadataCard metadatacard = new MetadataCard(driver, true);

			result += ExternalRepositoryHelper.verifyPropertyNotEditable(metadatacard, "Class");
			result += ExternalRepositoryHelper.verifyPropertyNotEditable(metadatacard, dataPool.get("Property"));

			if (result.equals(""))
				Log.pass("Test Case passed. Folder object " + objectName + " in external repository cannot be modified in quick search results because the mapped user does not have write permission to it BUT the folder can be accessed.");
			else
				Log.fail("Test case failed. Details: " + result, driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}


	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Promoted objects cannot be seen when browsing external views if Windows ACL denies read permission from user", priority = 4)
	public void BrowseRepoReadDenyPromotedObj(HashMap<String,String> dataPool, String driverType) throws Exception {

		//Called by using test data which defines Path to external view (in external repository)
		ReadDenyExtRepoObjInExternalViewOrMFilesViewTestBody(dataPool);

	}



	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Unmanaged objects can be seen when browsing external views if Windows ACL denies read permission from user but the object content cannot be accessed", priority = 4)
	public void BrowseRepoReadDenyUnmanagedObj(HashMap<String,String> dataPool, String driverType) throws Exception {

		WebDriver driver = null;

		try {

			driver = driverManager.startTesting(Utility.getMethodName());

			//Login to MFWA
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("Navigating to path ('" + dataPool.get("Path") + "').", driver);

			//Navigate to a view
			if( !homePage.listView.navigateThroughView(dataPool.get("Path")) )
				throw new Exception("Navigation to path '" + dataPool.get("Path") + "' failed");

			String objectName = dataPool.get("Object");

			Log.message("Double clicking object " + objectName);

			homePage.listView.doubleClickItem(objectName);

			MFilesDialog errorDialog = new MFilesDialog(driver);

			String message = errorDialog.getMessage();

			Log.message("Error message '" + message + "' was displayed.", driver);

			errorDialog.clickOkButton();

			if (message.contains("Not found.") || message.contains("Access denied."))
				Log.pass("Test Case passed. Object " + objectName + " was visible when navigated to path '" + dataPool.get("Path") + "' with denied read permission but its content cannot be accessed by double clicking object.");
			else
				Log.fail("Test case failed. Object " + objectName + " did not display correct error message when it was double clicked. The message was '" + message + "'", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}



	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Unmanaged and promoted objects cannot be modified when browsing external views BUT file can read if Windows ACL write permission is denied from user", priority = 5)
	public void BrowseRepoWriteDenyExtRepoObj(HashMap<String,String> dataPool, String driverType) throws Exception {

		//Called by using test data which defines Path to external view (in external repository)
		WriteDenyExtRepoObjInExternalViewOrMFilesViewTestBody(dataPool);

	}

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Unmanaged and promoted folder objects cannot be modified when browsing external views BUT the folder can be accessed when Windows ACL write permission is denied from user", priority = 6)
	public void BrowseRepoWriteDenyFolderObj(HashMap<String,String> dataPool, String driverType) throws Exception {

		//Called by using test data which defines Path to external view (in external repository)
		WriteDenyFolderObjInExternalViewOrMFilesViewTestBody(dataPool);

	}

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Promoted objects cannot be seen when browsing normal M-Files views if Windows ACL denies read permission from user", priority = 7)
	public void InViewReadDenyPromotedObj(HashMap<String,String> dataPool, String driverType) throws Exception {

		//Called by using test data which defines Path to normal M-Files view (only for promoted objects)
		ReadDenyExtRepoObjInExternalViewOrMFilesViewTestBody(dataPool);

	}

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Promoted objects cannot be modified when browsing normal M-Files views BUT file can read if Windows ACL write permission is denied from user", priority = 8)
	public void InViewWriteDenyPromotedObj(HashMap<String,String> dataPool, String driverType) throws Exception {

		//Called by using test data which defines Path to normal M-Files view (only for promoted objects)
		WriteDenyExtRepoObjInExternalViewOrMFilesViewTestBody(dataPool);

	}

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Unmanaged and promoted folder objects cannot be modified in external views BUT the folder can be accessed when Windows ACL write permission is denied from user", priority = 9)
	public void InViewWriteDenyFolderObj(HashMap<String,String> dataPool, String driverType) throws Exception {

		//Called by using test data which defines Path to normal M-Files view (only for promoted objects)
		WriteDenyFolderObjInExternalViewOrMFilesViewTestBody(dataPool);

	}

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Promoted objects cannot be seen in advanced search results if Windows ACL denies read permission from user", priority = 10)
	public void AdvSearchReadDenyPromotedObj(HashMap<String,String> dataPool, String driverType) throws Exception {

		WebDriver driver = null; 

		try {

			driver = driverManager.startTesting(Utility.getMethodName());

			//Login to MFWA
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("Making advanced search: '" + dataPool.get("SearchProperty") + " is " + dataPool.get("SearchPropertyValue") + "'", driver);

			//Make a quick search using a search word
			homePage.searchPanel.setAdditionalConditions(dataPool.get("SearchProperty"), "is", dataPool.get("SearchPropertyValue"));
			homePage.searchPanel.clickSearch();

			String objectName = dataPool.get("Object");

			Log.message("Verifying that object " + objectName + " is not visible in without read permission.", driver);

			if (!homePage.listView.isItemExists(objectName))
				Log.pass("Test case passed. Object '" + objectName + "' from external repository was not found in advanced search because the mapped user does not have read permission to it.");
			else
				Log.fail("Test case failed. Object '" + objectName + "' from external repository was found in advanced search even though the mapped user does not have read permission to it.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}


	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Promoted objects cannot be modified when using advanced search BUT file can read if Windows ACL write permission is denied from user", priority = 11)
	public void AdvSearchWriteDenyPromotedObj(HashMap<String,String> dataPool, String driverType) throws Exception {

		WebDriver driver = null; 

		try {

			driver = driverManager.startTesting(Utility.getMethodName());

			//Login to MFWA
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("Making advanced search: '" + dataPool.get("SearchProperty") + " is " + dataPool.get("SearchPropertyValue") + "'", driver);

			//Make a quick search using a search word
			homePage.searchPanel.setAdditionalConditions(dataPool.get("SearchProperty"), "is", dataPool.get("SearchPropertyValue"));
			homePage.searchPanel.clickSearch();

			String objectName = dataPool.get("Object");

			homePage.listView.clickItem(objectName);

			MetadataCard metadatacard = new MetadataCard(driver, true);

			String result = "";

			result += ExternalRepositoryHelper.verifyPropertyNotEditable(metadatacard, "Class");
			result += ExternalRepositoryHelper.verifyPropertyNotEditable(metadatacard, dataPool.get("Property"));

			String expectedPageCount = dataPool.get("PageCount");

			result += ExternalRepositoryHelper.verifyPreview(driver, homePage, objectName, expectedPageCount);

			if (result.equals(""))
				Log.pass("Test Case passed. Object " + objectName + " cannot be modified when using advanced search because the mapped user does not have write permission to it BUT file can be accessed by using preview.");
			else
				Log.fail("Test case failed. Details: " + result, driver);
		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Promoted folder objects cannot be modified when using advanced search BUT file can read if Windows ACL write permission is denied from user", priority = 11)
	public void AdvSearchWriteDenyFolderObj(HashMap<String,String> dataPool, String driverType) throws Exception {

		WebDriver driver = null; 

		try {

			driver = driverManager.startTesting(Utility.getMethodName());

			//Login to MFWA
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("Making advanced search: '" + dataPool.get("SearchProperty") + " is " + dataPool.get("SearchPropertyValue") + "'", driver);

			//Make a quick search using a search word
			homePage.searchPanel.setAdditionalConditions(dataPool.get("SearchProperty"), "is", dataPool.get("SearchPropertyValue"));
			homePage.searchPanel.clickSearch();

			String objectName = dataPool.get("Object");

			String controlObject = dataPool.get("ControlObject");
			int expectedCount = Integer.parseInt(dataPool.get("ObjectCountInFolder"));

			String result = "";

			result += ExternalRepositoryHelper.verifyFolderAccess(driver, homePage, objectName, controlObject, expectedCount);

			homePage.listView.clickItem(objectName);

			MetadataCard metadatacard = new MetadataCard(driver, true);

			result += ExternalRepositoryHelper.verifyPropertyNotEditable(metadatacard, "Class");
			result += ExternalRepositoryHelper.verifyPropertyNotEditable(metadatacard, dataPool.get("Property"));

			if (result.equals(""))
				Log.pass("Test Case passed. Folder object " + objectName + " cannot be modified when using advanced search because the mapped user does not have write permission to it BUT the folder can be accessed.");
			else
				Log.fail("Test case failed. Details: " + result, driver);
		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}

	//TODO: Hyperlink cases -> link to promoted object when read is denied or write is denied. Folder objects also??

	//TODO: I think user can also get "M-Files URL" that points inside a folder in ext repo -> test if read or write denied?

}

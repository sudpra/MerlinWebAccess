package MFClient.Tests.ExternalConnectors;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.TestMethodWebDriverManager;

import java.lang.reflect.Method;
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
import MFClient.Wrappers.ExternalRepositoryHelper;
import MFClient.Wrappers.ListView;
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class CommonAuthExternalRepositorySmoke {

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
	 * writeLogTestCaseStart : Call Log.testCaseInfo before running the test
	 */
	/*@BeforeMethod (alwaysRun=true)
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
	 */
	/**
	 * quitDriver: Quits the driver after the method
	 * @throws Exception
	 */
	/*
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
	 */

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


	/**
	 * insertColumnsAndVerifyTheirValues : Inserts columns, mentioned in test data dataPool, to the current view. Then compares the values
	 * of the columns against expected values for each test object mentioned in test data. In the end, all columns whose values were read are removed.
	 * NOTE, removes columns even if they were already present in the view.
	 * @param driver - WebDriver object
	 * @param homePage - HomePage object
	 * @param dataPool - Hashmap of the test method's test data. Should contain keys "Columns", "Objects", and "ValuesObj0", "ValuesObj1" "ValuesObj...
	 * (for each test object).
	 * Columns contains each column to be verified separated by line breaks. 
	 * Objects contains each object whose columns should be verified separated by line breaks.
	 * ValuesObj0, ValuesObj1 etc. should contain the values for columns in same order as the columns are mentioned in "Columns".
	 * @return Informative string if the expected and actual values of any property values of any objects mentioned in dataPool test data do not match.
	 * Empty string is returned if there are not issues.
	 */
	private String insertColumnsAndVerifyTheirValues(WebDriver driver, HomePage homePage, HashMap<String,String> dataPool) throws Exception{

		String result = "";

		try{

			//Each column and object are separated by line breaks in the string.
			String[] columns = dataPool.get("Columns").split("\n");
			String[] objects = dataPool.get("Objects").split("\n");

			//Insert columns from test data to list view
			for(int i = 0; i < columns.length; ++i){
				Log.message("Inserting column ('" + columns[i] + "').");
				homePage.listView.insertColumn(columns[i]);
			}

			Log.message("Columns inserted. Next reading the values from columns.", driver);

			//Go through objects that are mentioned in test data
			for(int i = 0; i < objects.length; ++i){

				String objectName = objects[i];

				//The key for this test object used in test data, in other words, the Excel column title for the cell that contains 
				//the property values of this test object
				String testdataKey = "ValuesObj" + i;

				//The values for the properties are separated by line breaks. They should be in the same order as their respective columns
				String[] expectedColumnValues = dataPool.get(testdataKey).split("\n");

				//Go through each column in test data for this test object
				for(int j = 0; j < columns.length; ++j){

					String columnName = columns[j];
					Log.message(objectName + ": reading value of column '" + columnName +"'");
					String columnValue = homePage.listView.getColumnValueByItemName(objectName, columnName);

					//If the column value is empty then let's mark it as "-"
					if(columnValue.equals(""))
						columnValue = "-";

					//Compare the expected property value in the column against the actual column value that is displayed
					result += ExternalRepositoryHelper.verifyPropertyValue(objectName, columnName, expectedColumnValues[j], columnValue);
				}
			}

			Log.message("Removing all inserted columns");
			for(int i = 0; i < columns.length; ++i){
				homePage.listView.removeColumn(columns[i]);
			}

			return result;
		}
		catch(Exception e){
			throw new Exception("Expcetion in insertColumnsAndReadTheirValues: " + e.getMessage());
		}
	}

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Search for unmanaged in external repository", priority = 1)
	public void UnmanagedObjectsQuickSearch(HashMap<String,String> dataPool, String driverType) throws Exception {

		WebDriver driver = null; 

		try {

			driver = driverManager.startTesting(Utility.getMethodName());

			//Login to MFWA
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			homePage.searchPanel.quickSearch(dataPool.get("SearchWord"));

			String result = ExternalRepositoryHelper.verifyExternalRepositoryDocumentObjects(driver, homePage, 
					dataPool.get("Objects").split("\n"),
					dataPool.get("Repository").split("\n"),
					dataPool.get("Location").split("\n"),
					dataPool.get("PageCount").split("\n")
					);

			//Verify if any error situations were written to test result string
			if (result.equals(""))
				Log.pass("Test Case passed. Expected objects from external repository were found in search results when using quick search.");
			else
				Log.fail("Test case failed. Additional info. : " + result + ".", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Search for unmanaged folder objects in external repository", priority = 2)
	public void UnmanagedFolderObjsQuickSearch(HashMap<String,String> dataPool, String driverType) throws Exception {

		WebDriver driver = null; 

		try {

			driver = driverManager.startTesting(Utility.getMethodName());

			//Login to MFWA
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			homePage.searchPanel.quickSearch(dataPool.get("SearchWord"));

			String result = ExternalRepositoryHelper.verifyExternalRepositoryFolderObjects(driver, homePage, 
					dataPool.get("Objects").split("\n"),
					dataPool.get("Repository").split("\n"),
					dataPool.get("Location").split("\n"),
					dataPool.get("ControlObject").split("\n"),
					dataPool.get("ObjectCountInFolder").split("\n")
					);

			//Verify if any error situations were written to test result string
			if (result.equals(""))
				Log.pass("Test Case passed. Expected objects from external repository were found in search results when using quick search.");
			else
				Log.fail("Test case failed. Additional info. : " + result + ".", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Search for promoted objects in external repository", priority = 3)
	public void PromotedObjectsQuickSearch(HashMap<String,String> dataPool, String driverType) throws Exception {

		WebDriver driver = null; 

		try {

			//TODO: Should a different search word be used before and after promotion?

			driver = driverManager.startTesting(Utility.getMethodName());

			//Login to MFWA
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			homePage.searchPanel.quickSearch(dataPool.get("SearchWord"));

			String[] objects = dataPool.get("Objects").split("\n");
			String[] classes = dataPool.get("Class").split("\n");
			String[] properties = dataPool.get("Property").split("\n");
			String[] propertyValues = dataPool.get("PropertyValue").split("\n");

			for(int i=0; i < objects.length; ++i){

				ExternalRepositoryHelper.promoteObject(driver, objects[i], homePage, classes[i], properties[i], propertyValues[i]);
			}

			homePage.taskPanel.clickItem("Home");

			homePage.searchPanel.quickSearch(dataPool.get("SearchWord"));

			String result = ExternalRepositoryHelper.verifyExternalRepositoryDocumentObjects(driver, homePage, 
					dataPool.get("Objects").split("\n"),
					dataPool.get("Repository").split("\n"),
					dataPool.get("Location").split("\n"),
					dataPool.get("PageCount").split("\n")
					);

			//Verify if any error situations were written to test result string
			if (result.equals(""))
				Log.pass("Test Case passed. Expected objects from external repository were found in search results when using quick search.");
			else
				Log.fail("Test case failed. Additional info. : " + result + ".", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}

	//TODO: Probably should make another test method for verifying promoted object. Basically same case but some object is promoted beforehand.
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Browse external repository for objects", priority = 4)
	public void BrowseToObjectsInExtRepo(HashMap<String,String> dataPool, String driverType) throws Exception {

		WebDriver driver = null; 

		try {

			driver = driverManager.startTesting(Utility.getMethodName());

			//Login to MFWA
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("Navigating to external repository path ('" + dataPool.get("Path") + "').", driver);

			//Navigate to external repository folder
			homePage.listView.navigateThroughView(dataPool.get("Path"));

			String result = ExternalRepositoryHelper.verifyExternalRepositoryDocumentObjects(driver, homePage, 
					dataPool.get("Objects").split("\n"),
					dataPool.get("Repository").split("\n"),
					dataPool.get("Location").split("\n"),
					dataPool.get("PageCount").split("\n")
					);

			//Verify if any error situations were written to test result string
			if (result.equals(""))
				Log.pass("Test Case passed. Expected objects were found in external repository when navigating to their location.");
			else
				Log.fail("Test case failed. Additional info. : " + result + ".", driver);


		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Promote unmanaged object when browsed to external repository", priority = 5)
	public void PromoteObjWhenBrowsingExtRepo(HashMap<String,String> dataPool, String driverType) throws Exception {

		WebDriver driver = null; 

		try {

			driver = driverManager.startTesting(Utility.getMethodName());

			//Login to MFWA
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("Navigating to external repository path ('" + dataPool.get("Path") + "').", driver);

			//Navigate to external repository folder
			homePage.listView.navigateThroughView(dataPool.get("Path"));

			String objectName = dataPool.get("ObjectName");

			ExternalRepositoryHelper.promoteObject(driver, objectName, homePage, dataPool.get("Class"), dataPool.get("Property"), dataPool.get("PropertyValue"));

			String result = ExternalRepositoryHelper.verifyPromotedObject(driver, objectName, homePage, 
					dataPool.get("Class"), 
					dataPool.get("Property"), 
					dataPool.get("PropertyValue"), 
					dataPool.get("Location"),
					dataPool.get("Repository"));

			//Verify if any error situations were written to test result string
			if (result.equals(""))
				Log.pass("Test Case passed. The object was promoted succesfully. Class and additional property " + dataPool.get("Property") + " were set. ID and version were received." );
			else
				Log.fail("Test Case ailed. Additional info : " + result, driver);


		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Promote unmanaged object when searching objects from external repository", priority = 6)
	public void PromoteObjWhenSearchingExtRepo(HashMap<String,String> dataPool, String driverType) throws Exception {

		WebDriver driver = null; 

		try {

			driver = driverManager.startTesting(Utility.getMethodName());

			//Login to MFWA
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("Making quick search using searchword ('" + dataPool.get("SearchWord") + "').", driver);

			//Search for unanaged object in external repository
			homePage.searchPanel.search(dataPool.get("SearchWord"), "");

			String objectName = dataPool.get("ObjectName");

			ExternalRepositoryHelper.promoteObject(driver, objectName, homePage, dataPool.get("Class"), dataPool.get("Property"), dataPool.get("PropertyValue"));

			String result = ExternalRepositoryHelper.verifyPromotedObject(driver, objectName, homePage, 
					dataPool.get("Class"), 
					dataPool.get("Property"), 
					dataPool.get("PropertyValue"), 
					dataPool.get("Location"),
					dataPool.get("Repository"));

			//Verify if any error situations were written to test result string
			if (result.equals(""))
				Log.pass("Test Case passed. The object was promoted succesfully. Class and additional property " + dataPool.get("Property") + " were set. ID and version were received." );
			else
				Log.fail("Test Case ailed. Additional info : " + result, driver);


		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Promoted object can be found in views", priority = 7)
	public void ViewContainsPromotedObj(HashMap<String,String> dataPool, String driverType) throws Exception {

		WebDriver driver = null; 

		try {

			driver = driverManager.startTesting(Utility.getMethodName());

			//Login to MFWA
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("Navigating to external repository path ('" + dataPool.get("Path") + "').", driver);

			//Navigate to external repository folder
			homePage.listView.navigateThroughView(dataPool.get("Path"));

			String objectName = dataPool.get("ObjectName");

			ExternalRepositoryHelper.promoteObject(driver, objectName, homePage, dataPool.get("Class"), dataPool.get("Property"), dataPool.get("PropertyValue"));

			homePage.taskPanel.clickItem("Home");

			Log.message("Navigating to view ('" + dataPool.get("View") + "').", driver);

			homePage.listView.navigateThroughView(dataPool.get("View"));

			//Verify that the promoted object is found in view based on its metadata
			if (homePage.listView.isItemExists(objectName))
				Log.pass("Test Case passed. The promoted object " + objectName + " was found in view " + dataPool.get("View"));
			else
				Log.fail("Test Case failed. The promoted object " + objectName + " was not found in view " + dataPool.get("View"), driver);


		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}


	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Promoted object can be found in advanced search", priority = 8)
	public void AdvancedSearchPromotedObj(HashMap<String,String> dataPool, String driverType) throws Exception {

		WebDriver driver = null; 

		try {

			driver = driverManager.startTesting(Utility.getMethodName());

			//Login to MFWA
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("Making quick search using searchword ('" + dataPool.get("SearchWord") + "').", driver);

			//Search for unanaged object in external repository
			homePage.searchPanel.search(dataPool.get("SearchWord"), "");

			String objectName = dataPool.get("ObjectName");

			ExternalRepositoryHelper.promoteObject(driver, objectName, homePage, dataPool.get("Class"), dataPool.get("Property"), dataPool.get("PropertyValue"));

			Log.message("Making advanced search: '" + dataPool.get("SearchProperty") + " is " + dataPool.get("SearchPropertyValue") + "'", driver);

			homePage.searchPanel.setSearchWord("");
			homePage.searchPanel.setAdditionalConditions(dataPool.get("SearchProperty"), "is", dataPool.get("SearchPropertyValue"));
			homePage.searchPanel.clickSearch();

			//Verify that the promoted object is found in advanced search based on its metadata
			if (homePage.listView.isItemExists(objectName))
				Log.pass("Test Case passed. The promoted object " + objectName + " was found in advanced search: '" + dataPool.get("SearchProperty") + " is " + dataPool.get("SearchPropertyValue") + "'");
			else
				Log.fail("Test Case failed. The promoted object " + objectName + " was not found in advanced search: '" + dataPool.get("SearchProperty") + " is " + dataPool.get("SearchPropertyValue") + "'", driver);


		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Check out, modify, check in promoted object", priority = 9)
	public void CheckOutEditCheckInPromotedObj(HashMap<String,String> dataPool, String driverType) throws Exception {

		WebDriver driver = null; 

		try {

			driver = driverManager.startTesting(Utility.getMethodName());

			//Login to MFWA
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("Making quick search using searchword ('" + dataPool.get("SearchWord") + "').", driver);

			homePage.searchPanel.search(dataPool.get("SearchWord"), "");

			String objectName = dataPool.get("ObjectName");

			ExternalRepositoryHelper.promoteObject(driver, objectName, homePage, dataPool.get("Class"), dataPool.get("Property"), dataPool.get("PropertyValue"));

			Log.message("Checking out object ('" + objectName + "').", driver);

			homePage.taskPanel.clickItem("Check Out");

			if(!ListView.isCheckedOutByItemName(driver, objectName))
				throw new Exception("Managed object was not checked out.");

			Log.message("Modifying metadata of object ('" + objectName + "') by setting property " + dataPool.get("Property") + " with value " + dataPool.get("PropertyValue2"));

			MetadataCard metadatacard = new MetadataCard(driver, true);
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("PropertyValue2"));
			metadatacard.saveAndClose();

			Log.message("Checking in object ('" + objectName + "').", driver);

			homePage.taskPanel.clickItem("Check In");

			if(ListView.isCheckedOutByItemName(driver, objectName))
				throw new Exception("Managed object was not checked in.");

			homePage.taskPanel.clickItem("Properties");

			metadatacard = new MetadataCard(driver);

			Log.message("Verifying that property " + dataPool.get("Property") + " was set with value " + dataPool.get("PropertyValue2"), driver);

			String actualPropertyValue = metadatacard.getPropertyValue(dataPool.get("Property"));

			//Verify that the promoted object was modified properly during checkout
			if (actualPropertyValue.equals(dataPool.get("PropertyValue2")))
				Log.pass("Test Case passed. The promoted object " + objectName + " was checked out, property " + dataPool.get("Property") + " set with value " + dataPool.get("PropertyValue2") + ", and checked in.");
			else
				Log.fail("Test Case failed. The promoted object " + objectName + " property " + dataPool.get("Property") + " was not modified properly, Expected: " + dataPool.get("PropertyValue2") + " vs. Actual: " + actualPropertyValue, driver);


		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Check out unmanaged object, edit its name, and check it back in", priority = 10)
	public void CheckOutEditCheckInUnmanagedObj(HashMap<String,String> dataPool, String driverType) throws Exception {

		WebDriver driver = null; 

		try {

			driver = driverManager.startTesting(Utility.getMethodName());

			//Login to MFWA
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("Navigating to external repository path ('" + dataPool.get("Path") + "').", driver);

			//Navigate to external repository folder
			homePage.listView.navigateThroughView(dataPool.get("Path"));

			String objectName = dataPool.get("ObjectName");

			homePage.listView.clickItem(objectName);

			Log.message("Checking out object ('" + objectName + "').", driver);

			homePage.taskPanel.clickItem("Check Out");

			if(!ListView.isCheckedOutByItemName(driver, objectName))
				throw new Exception("Unmanaged object was not checked out.");

			String fileExtension = objectName.split("\\.")[1];

			String newNameForObject = objectName + "_renamed";

			Log.message("Renaming the object by using rightpane metadata card. New name: '" + newNameForObject + "'");

			MetadataCard metadataCard = new MetadataCard(driver, true);
			metadataCard.setPropertyValue("Name or title", newNameForObject);
			metadataCard.saveAndClose();

			String newNameWithFileExtension = newNameForObject + "." + fileExtension;

			Log.message("Checking in renamed object ('" + newNameWithFileExtension + "').", driver);

			homePage.taskPanel.clickItem("Check In");

			if(!ListView.isCheckedOutByItemName(driver, newNameWithFileExtension)){
				Log.pass("Unmanaged object '" + newNameWithFileExtension + "' was succesfully checked in after editing object's name.");
			}
			else{
				Log.fail("Unmanaged object '" + newNameWithFileExtension + "' was not checked in after editing its name.", driver);
			}

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}


	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Modify metadata of promoted object when browsing external repository", priority = 10)
	public void EditMetadataPromotedObjBrowse(HashMap<String,String> dataPool, String driverType) throws Exception {

		WebDriver driver = null; 

		try {

			driver = driverManager.startTesting(Utility.getMethodName());

			//Login to MFWA
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("Navigating to external repository path ('" + dataPool.get("Path") + "').", driver);

			//Navigate to external repository folder
			homePage.listView.navigateThroughView(dataPool.get("Path"));

			String objectName = dataPool.get("ObjectName");

			ExternalRepositoryHelper.promoteObject(driver, objectName, homePage, dataPool.get("Class"), dataPool.get("Property"), dataPool.get("PropertyValue"));

			homePage.taskPanel.clickItem("Home");

			Log.message("Navigating again to external repository path ('" + dataPool.get("Path") + "').", driver);

			//Navigate again to the same external repository folder
			homePage.listView.navigateThroughView(dataPool.get("Path"));

			homePage.listView.clickItem(objectName);

			Log.message("Modifying metadata of object ('" + objectName + "') by setting property " + dataPool.get("Property") + " with value " + dataPool.get("PropertyValue2"), driver);

			MetadataCard metadatacard = new MetadataCard(driver, true);
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("PropertyValue2"));
			metadatacard.saveAndClose();

			homePage.taskPanel.clickItem("Properties");

			metadatacard = new MetadataCard(driver);

			Log.message("Verifying that property " + dataPool.get("Property") + " was set with value " + dataPool.get("PropertyValue2"), driver);

			String actualPropertyValue = metadatacard.getPropertyValue(dataPool.get("Property"));

			//Verify that the promoted object was modified properly
			if (actualPropertyValue.equals(dataPool.get("PropertyValue2")))
				Log.pass("Test Case passed. The promoted object " + objectName + " had its property " + dataPool.get("Property") + " set with value " + dataPool.get("PropertyValue2"));
			else
				Log.fail("Test Case failed. The promoted object " + objectName + " property " + dataPool.get("Property") + " was not modified properly, Expected: " + dataPool.get("PropertyValue2") + " vs. Actual: " + actualPropertyValue, driver);


		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Modify metadata of promoted object when using quick search", priority = 11)
	public void EditMetadataPromotedObjQSearch(HashMap<String,String> dataPool, String driverType) throws Exception {

		WebDriver driver = null; 

		try {

			driver = driverManager.startTesting(Utility.getMethodName());

			//Login to MFWA
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("Navigating to external repository path ('" + dataPool.get("Path") + "').", driver);

			//Navigate to external repository folder
			homePage.listView.navigateThroughView(dataPool.get("Path"));

			String objectName = dataPool.get("ObjectName");

			ExternalRepositoryHelper.promoteObject(driver, objectName, homePage, dataPool.get("Class"), dataPool.get("Property"), dataPool.get("PropertyValue"));

			homePage.taskPanel.clickItem("Home");

			Log.message("Making quick search using searchword ('" + dataPool.get("SearchWord") + "').", driver);

			homePage.searchPanel.search(dataPool.get("SearchWord"), "");

			homePage.listView.clickItem(objectName);

			Log.message("Modifying metadata of object ('" + objectName + "') by setting property " + dataPool.get("Property") + " with value " + dataPool.get("PropertyValue2"), driver);

			MetadataCard metadatacard = new MetadataCard(driver, true);
			metadatacard.setPropertyValue(dataPool.get("Property"), dataPool.get("PropertyValue2"));
			metadatacard.saveAndClose();

			homePage.taskPanel.clickItem("Properties");

			metadatacard = new MetadataCard(driver);

			Log.message("Verifying that property " + dataPool.get("Property") + " was set with value " + dataPool.get("PropertyValue2"), driver);

			String actualPropertyValue = metadatacard.getPropertyValue(dataPool.get("Property"));

			//Verify that the promoted object was modified properly
			if (actualPropertyValue.equals(dataPool.get("PropertyValue2")))
				Log.pass("Test Case passed. The promoted object " + objectName + " had its property " + dataPool.get("Property") + " set with value " + dataPool.get("PropertyValue2"));
			else
				Log.fail("Test Case failed. The promoted object " + objectName + " property " + dataPool.get("Property") + " was not modified properly, Expected: " + dataPool.get("PropertyValue2") + " vs. Actual: " + actualPropertyValue, driver);


		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}


	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Browse to a view path (external view or M-Files view), insert columns and verify the column values for objects.", priority = 12)
	public void ColumnsExtRepoBrowse(HashMap<String,String> dataPool, String driverType) throws Exception {

		WebDriver driver = null; 

		try {

			driver = driverManager.startTesting(Utility.getMethodName());

			//Login to MFWA
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			Log.message("Navigating to path ('" + dataPool.get("Path") + "').", driver);

			homePage.listView.navigateThroughView(dataPool.get("Path"));

			String result = insertColumnsAndVerifyTheirValues(driver, homePage, dataPool);

			if(result.equalsIgnoreCase(""))
				Log.pass("The objects in path '" + dataPool.get("Path") + "' had expected property values in inserted columns.");
			else
				Log.fail("Test case failed. Additional info. : " + result, driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}


	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Make a quick search, insert columns and verify the column values for objects.", priority = 13)
	public void ColumnsExtRepoQuickSearch(HashMap<String,String> dataPool, String driverType) throws Exception {

		WebDriver driver = null; 
		Boolean displayModeChanged = false;
		HomePage homePage = null;

		try {

			driver = driverManager.startTesting(Utility.getMethodName());

			//Login to MFWA
			homePage = LoginPage.launchDriverAndLogin(driver, true);

			homePage.searchPanel.search(dataPool.get("SearchWord"), "");

			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");
			displayModeChanged = true;

			String result = insertColumnsAndVerifyTheirValues(driver, homePage, dataPool);

			if(result.equalsIgnoreCase(""))
				Log.pass("The objects in quick search results had expected property values in inserted columns.");
			else
				Log.fail("Test case failed. Additional info. : " + result, driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			if(displayModeChanged && homePage != null && driver != null)
				homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");
			Utility.quitDriver(driver);
		}//End finally
	}


	//TODO: Use Hyperlink to access promoted object (file or folder)

	//TODO: Can also try accessing a folder by using "M-Files Web URL"

	//TODO: Rename unmanaged/promoted object

	//TODO: Delete unmanaged/promoted object

}

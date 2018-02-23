package MFClient.Tests.Search;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.Utils;
import genericLibrary.WebDriverUtils;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.openqa.selenium.By;
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
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class SearchInMetadataOrContent {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String userName = null;
	public static String password = null;
	public static String testVault = null;
	public static String configURL = null;
	public static String userFullName = null;
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
			configURL = xmlParameters.getParameter("ConfigurationURL");
			userName = xmlParameters.getParameter("UserName");
			password = xmlParameters.getParameter("Password");
			testVault = xmlParameters.getParameter("VaultName");		
			userFullName = xmlParameters.getParameter("UserFullName");
			Utility.restoreTestVault();
			Utility.configureUsers(xlTestDataWorkBook);
			className = this.getClass().getSimpleName().toString().trim();
			if (xmlParameters.getParameter("driverType").equalsIgnoreCase("IE"))
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim() + xmlParameters.getParameter("driverVersion").trim();
			else
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim();


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
	}

	/**
	 * 24.2.5 : Empty value in the search field - Search in Metadata
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "SearchInMetadataOrContent"}, 
			description = "Empty value in the search field - Search in Metadata")
	public void SprintTest24_2_5(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			int expectedCount = 0;
			int count = 0;

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Perform Pre-defined search with empty search word 
			//----------------------------------------------------
			homePage.searchPanel.clickSearch();
			Utils.fluentWait(driver);

			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");
			Utils.fluentWait(driver);

			expectedCount = homePage.listView.itemCount();
			String[] expectedObjects = new String[expectedCount];

			for(count = 0; count < expectedCount; count++) 
				expectedObjects[count] = homePage.listView.getItemNameByItemIndex(count);

			Log.message("Step-1: Perform Pre-defined search with empty search word. Object count : " +  expectedCount);

			//2. Change the Search in Type and click the Search Button
			//--------------------------------------------------------
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");

			Utils.fluentWait(driver);
			homePage.searchPanel.setSearchInType(dataPool.get("SearchInType"));
			homePage.searchPanel.clickSearch();
			Utils.fluentWait(driver);

			Log.message("2. Change the Search in Type and click the Search Button");

			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");
			Utils.fluentWait(driver);

			//Verification: To verify if all the objects are displayed
			//---------------------------------------------------------
			Utils.fluentWait(driver);

			if(expectedCount != homePage.listView.itemCount()) 
				throw new SkipException("The Count did not match the expected value. Expected Count : " + expectedCount + "  Actual count : " + homePage.listView.itemCount());

			for(count = 0; count < expectedCount; count++){
				if(!homePage.listView.getItemNameByItemIndex(count).equals(expectedObjects[count]))
					break;
			}

			if(count == expectedCount)
				Log.pass("Test Case Passed. Empty search provided same search result when performing search twice with " +  dataPool.get("SearchInType"));
			else
				Log.fail("Test Case Failed. Empty search provided different search results when performing search twice with " + dataPool.get("SearchInType"), driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {

			if(!driver.equals(null)){
				try
				{
					HomePage homePage = new HomePage(driver);
					homePage.searchPanel.setSearchInType("Search in metadata and file contents");
					Utils.fluentWait(driver);
					homePage.searchPanel.clickSearch();
					Utils.fluentWait(driver);
					homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");
					Utils.fluentWait(driver);
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.6 : Empty value in the search field - Search in file content
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "SearchInMetadataOrContent"}, 
			description = "Empty value in the search field - Search in file content")
	public void SprintTest24_2_6(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			int expectedCount = 0;
			int count = 0;

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Perform Pre-defined search with search word 
			//----------------------------------------------------
			homePage.searchPanel.clickSearch();

			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");
			Utils.fluentWait(driver);

			expectedCount = homePage.listView.itemCount();
			String[] expectedObjects = new String[expectedCount];

			for(count = 0; count < expectedCount; count++) 
				expectedObjects[count] = homePage.listView.getItemNameByItemIndex(count);

			Log.message("Step-1: Perform Pre-defined search with empty search word .");

			//2. Change the Search in Type and click the Search Button
			//--------------------------------------------------------

			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");

			if(!homePage.searchPanel.isSearchInTypeExists(dataPool.get("SearchInType")))
				Log.fail("Test Case Failed. The Given 'Search In type' " + dataPool.get("SearchInType") + " was not found.", driver);

			Utils.fluentWait(driver);
			homePage.searchPanel.setSearchInType(dataPool.get("SearchInType"));
			homePage.searchPanel.clickSearch();
			Utils.fluentWait(driver);

			Log.message("2. Change the Search in Type and click the Search Button.");


			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");
			Utils.fluentWait(driver);


			//Verification: To verify if all the objects are displayed
			//---------------------------------------------------------
			Utils.fluentWait(driver);
			if(expectedCount != homePage.listView.itemCount())
				throw new SkipException("The Count did not match the expected value.");

			for(count = 0; count < expectedCount; count++){
				if(!homePage.listView.getItemNameByItemIndex(count).equals(expectedObjects[count]))
					break;
			}

			if(count == expectedCount)
				Log.pass("Test Case Passed. Empty search provided same search result when performing search twice with search type " +  dataPool.get("SearchInType"));
			else
				Log.fail("Test Case Failed. Empty search provided different search results when performing search twice with search type " + dataPool.get("SearchInType"), driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			if(!driver.equals(null)){
				try
				{
					HomePage homePage = new HomePage(driver);
					homePage.searchPanel.setSearchInType("Search in metadata and file contents");
					homePage.searchPanel.clickSearch();
					Utils.fluentWait(driver);
					homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");
					Utils.fluentWait(driver);
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.7 : Empty value in the search field - Search in metadata and file content
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "SearchInMetadataOrContent"}, 
			description = "Empty value in the search field - Search in metadata and file content")
	public void SprintTest24_2_7(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			int expectedCount = 0;
			int count = 0;

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Perform Pre-defined search
			//-----------------------------------
			Utils.fluentWait(driver);
			homePage.searchPanel.clickSearch();
			Utils.fluentWait(driver);

			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");
			Utils.fluentWait(driver);

			expectedCount = homePage.listView.itemCount();
			String[] expectedObjects = new String[expectedCount];

			//Get names of all objects
			for(count = 0; count < expectedCount; count++) 
				expectedObjects[count] = homePage.listView.getItemNameByItemIndex(count);

			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");
			Utils.fluentWait(driver);

			if(!homePage.searchPanel.isSearchInTypeExists(dataPool.get("SearchInType"))) {
				homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");
				Utils.fluentWait(driver);
				Log.fail("Test Case Failed. The Given 'Search In type' " + dataPool.get("SearchInType") + " was not found.", driver);
			}

			Log.message("Step-1: Perform Pre-defined search with empty search word .");

			//2. Change the Search in Type and click the Search Button
			//--------------------------------------------------------
			homePage.taskPanel.clickItem(Caption.MenuItems.Home.Value);
			Utils.fluentWait(driver);
			homePage.searchPanel.setSearchInType(dataPool.get("SearchInType"));
			homePage.searchPanel.clickSearch();
			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");
			Utils.fluentWait(driver);

			Log.message("2. Change the Search in Type and click the Search Button.");

			//Verification: To verify if all the objects are displayed
			//---------------------------------------------------------
			Utils.fluentWait(driver);
			if(expectedCount != homePage.listView.itemCount()) 
				throw new SkipException("The Count did not match the expected value.");

			int actualCount = homePage.listView.itemCount();
			String[] actualObjects = new String[actualCount];

			//Get names of all objects
			for(count = 0; count < actualCount; count++) 
				actualObjects[count] = homePage.listView.getItemNameByItemIndex(count);

			//Going through all expected object names in order to compare them to actual found object names
			for(int i = 0; i < expectedObjects.length; ++i)
			{
				String objectName = "";
				Boolean found = false;

				for(int j = 0; j < actualObjects.length; ++j)
				{
					if(expectedObjects[i].equals(actualObjects[j]))
					{
						found = true;
						break;
					}
				}

				if(!found)
				{
					Log.fail("Test Case Failed. The expected object " + objectName + " was not found in search results." , driver);
				}

			}

			if(actualCount == expectedCount)
				Log.pass("Test Case Passed. The same search result is displayed for two sequential empty searches.");
			else
				Log.fail("Test Case Failed. The search result differs between two sequential empty searches.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			if(!driver.equals(null)){
				try
				{
					HomePage homePage = new HomePage(driver);
					homePage.searchPanel.setSearchInType("Search in metadata and file contents");
					homePage.searchPanel.clickSearch();
					Utils.fluentWait(driver);
					homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");
					Utils.fluentWait(driver);
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.8 : Search with any extension  - Search in Metadata
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24", "SearchInMetadataOrContent"}, 
			description = "Search with any extension  - Search in Metadata")
	public void SprintTest24_2_8(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("Safari"))
			throw new SkipException(driverType.toUpperCase() + " driver does not support Actions.");

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			int expectedCount = 0;
			int count = 0;

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Perform Pre-defined search with search word 
			//----------------------------------------------------
			if(!homePage.searchPanel.isSearchInTypeExists(dataPool.get("SearchInType")))
				Log.fail("Test Case Failed. The Given 'Search In type' " + dataPool.get("SearchInType") + " was not found.", driver);

			Utils.fluentWait(driver);
			homePage.searchPanel.setSearchInType(dataPool.get("SearchInType"));
			homePage.searchPanel.search(dataPool.get("SearchWord"), Caption.Search.SearchAllObjects.Value);
			Utils.fluentWait(driver);

			Log.message("Step-1: Perform Pre-defined search with search word .");

			//Verification: To verify if only the objects satisfying the condition are present in the search results
			//------------------------------------------------------------------------------------------------------
			Utils.fluentWait(driver);
			homePage.listView.insertColumn(dataPool.get("Property"));
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			expectedCount = homePage.listView.itemCount();

			//Resizing column so that even longer object names are fully visible
			homePage.listView.resizeColumn("Name", 200);

			for(count = 0; count < expectedCount; count++){
				if(homePage.listView.getColumnValueByItemIndex(count, dataPool.get("Property")).equals(dataPool.get("Value"))) {
					if(!homePage.listView.getItemNameByItemIndex(count).endsWith(dataPool.get("SearchWord")))
						break;
				}
			}

			homePage.listView.removeColumn(dataPool.get("Property"));

			if(count == expectedCount)
				Log.pass("Test Case Passed. Only the objects of the specified extension were found in the search results.");
			else
				Log.fail("Test Case Failed. The object that did not satisfy the condition was also present in the Search result.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			if(!driver.equals(null)){
				try
				{
					HomePage homePage = new HomePage(driver);
					homePage.searchPanel.setSearchInType("Search in metadata and file contents");
					Utils.fluentWait(driver);
					homePage.searchPanel.clickSearch();
					Utils.fluentWait(driver);
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.9A : Search with any extension  - Search in file content (With Search Results).
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "SearchInMetadataOrContent"}, 
			description = "Search with any extension  - Search in file content (With Search Results).")
	public void SprintTest24_2_9A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			String[] resultantObjects = dataPool.get("ExpectedObjects").split("\n");
			int count = 0;

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Perform Pre-defined search with search word 
			//----------------------------------------------------
			if(!homePage.searchPanel.isSearchInTypeExists(dataPool.get("SearchInType")))
				Log.fail("Test Case Failed. The Given 'Search In type' " + dataPool.get("SearchInType") + " was not found.", driver);

			Utils.fluentWait(driver);
			homePage.searchPanel.setSearchInType(dataPool.get("SearchInType"));
			homePage.searchPanel.search(dataPool.get("SearchWord"), Caption.Search.SearchAllObjects.Value);
			Utils.fluentWait(driver);

			Log.message("Step-1: Perform Pre-defined search with search word .");

			//Verification: To verify if only the objects satisfying the condition are present in the search results
			//------------------------------------------------------------------------------------------------------
			if(resultantObjects.length != homePage.listView.itemCount()) 
				throw new SkipException("The Expected number of items was not found in the Search results.");

			for(count = 0; count < resultantObjects.length; count++){
				if(!homePage.listView.isItemExists(resultantObjects[count]))
					break;
			}

			if(count == resultantObjects.length)
				Log.pass("Test Case Passed. Only the objects of the expected objects were found in the search results.");
			else
				Log.fail("Test Case Failed. The object that did not satisfy the condition was also present in the Search result.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			if(!driver.equals(null)){
				try
				{
					HomePage homePage = new HomePage(driver);
					homePage.searchPanel.setSearchInType("Search in metadata and file contents");
					Utils.fluentWait(driver);
					homePage.searchPanel.clickSearch();
					Utils.fluentWait(driver);
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.9B : Search with any extension  - Search in file content (Without Search Results).
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "SearchInMetadataOrContent"}, 
			description = "Search with any extension  - Search in file content (Without Search Results).")
	public void SprintTest24_2_9B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Perform Pre-defined search with search word 
			//----------------------------------------------------
			if(!homePage.searchPanel.isSearchInTypeExists(dataPool.get("SearchInType")))
				Log.fail("Test Case Failed. The Given 'Search In type' " + dataPool.get("SearchInType") + " was not found.", driver);

			Utils.fluentWait(driver);
			homePage.searchPanel.setSearchInType(dataPool.get("SearchInType"));
			homePage.searchPanel.search(dataPool.get("SearchWord"), Caption.Search.SearchAllObjects.Value);
			Utils.fluentWait(driver);

			Log.message("Step-1: Perform Pre-defined search with search word .");

			//Verification: To verify if only the objects satisfying the condition are present in the search results
			//------------------------------------------------------------------------------------------------------
			if(homePage.listView.itemCount() == 0 && driver.findElement(By.id("noResultsText")).getText().equals(dataPool.get("ExpectedMessage"))) 
				Log.pass("Test Case Passed. The Search result did not return any result as expected.");
			else
				Log.fail("Test Case Failed. The Search result had objects which did not satisfy the condition.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			if(!driver.equals(null)){
				try
				{
					HomePage homePage = new HomePage(driver);
					homePage.searchPanel.setSearchInType("Search in metadata and file contents");
					Utils.fluentWait(driver);
					homePage.searchPanel.clickSearch();
					Utils.fluentWait(driver);
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.10A : Search with any extension  - Search in metadata and file content with Search Results
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"SKIP_KeyActions", "Sprint24", "SearchInMetadataOrContent"}, 
			description = "Search with any extension  - Search in metadata and file content with Search Results")
	public void SprintTest24_2_10A(HashMap<String,String> dataValues, String driverType) throws Exception {

		if (driverType.equalsIgnoreCase("IE") || driverType.equalsIgnoreCase("Safari"))
			throw new SkipException(driverType.toUpperCase() + " driver does not support Actions.");

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			int expectedCount = 0;
			int count = 0;

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Perform Pre-defined search with search word 
			//----------------------------------------------------
			if(!homePage.searchPanel.isSearchInTypeExists(dataPool.get("SearchInType")))
				Log.fail("Test Case Failed. The Given 'Search In type' " + dataPool.get("SearchInType") + " was not found.", driver);

			Utils.fluentWait(driver);
			homePage.searchPanel.setSearchInType(dataPool.get("SearchInType"));
			homePage.searchPanel.search(dataPool.get("SearchWord"), Caption.Search.SearchAllObjects.Value);
			Utils.fluentWait(driver);

			Log.message("Step-1: Perform Pre-defined search with search word .");

			//Verification: To verify if only the objects satisfying the condition are present in the search results
			//------------------------------------------------------------------------------------------------------
			Utils.fluentWait(driver);
			homePage.listView.insertColumn(dataPool.get("Property"));
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			expectedCount = homePage.listView.itemCount();

			String objectName = "";

			//Resizing column so that even longer object names are fully visible
			homePage.listView.resizeColumn("Name", 50);

			for(count = 0; count < expectedCount; count++){
				if(homePage.listView.getColumnValueByItemIndex(count, dataPool.get("Property")).equals(dataPool.get("Value"))) {
					if(!homePage.listView.getItemNameByItemIndex(count).endsWith(dataPool.get("SearchWord")))
					{
						objectName = homePage.listView.getItemNameByItemIndex(count);
						break;
					}
				}
			}

			homePage.listView.removeColumn(dataPool.get("Property"));

			if(count == expectedCount)
				Log.pass("Test Case Passed. Only the objects of the specified extension were found in the search results.");
			else
				Log.fail("Test Case Failed. The object '" + objectName + "' that did not satisfy the condition was also present in the Search result.", driver);
		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {

			if(!driver.equals(null)){
				try
				{
					HomePage homePage = new HomePage(driver);
					homePage.searchPanel.setSearchInType("Search in metadata and file contents");
					Utils.fluentWait(driver);
					homePage.searchPanel.clickSearch();
					Utils.fluentWait(driver);
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.10B : Search with any extension  - Search in metadata and file content with no Search Results
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "SearchInMetadataOrContent"}, 
			description = "Search with any extension  - Search in metadata and file content with no Search Results")
	public void SprintTest24_2_10B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Perform Pre-defined search with search word 
			//----------------------------------------------------
			if(!homePage.searchPanel.isSearchInTypeExists(dataPool.get("SearchInType")))
				Log.fail("Test Case Failed. The Given 'Search In type' " + dataPool.get("SearchInType") + " was not found.", driver);

			Utils.fluentWait(driver);
			homePage.searchPanel.setSearchInType(dataPool.get("SearchInType"));
			homePage.searchPanel.search(dataPool.get("SearchWord"), Caption.Search.SearchAllObjects.Value);
			Utils.fluentWait(driver);

			Log.message("Step-1: Perform Pre-defined search with search word .");

			//Verification: To verify if only the objects satisfying the condition are present in the search results
			//------------------------------------------------------------------------------------------------------
			if(homePage.listView.itemCount() != 0) 
				throw new SkipException("The Search result returned objects.");

			if(driver.findElement(By.id("noResultsText")).isDisplayed() && driver.findElement(By.id("noResultsText")).getText().equals(dataPool.get("ExpectedMessage")))
				Log.pass("Test Case Passed. The Expected Message was displayed when there is no search result to be displayed.");
			else
				Log.fail("Test Case Failed. The Expected Message was not displayed when there is no search result to be displayed.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			if(!driver.equals(null)){
				try
				{
					HomePage homePage = new HomePage(driver);
					homePage.searchPanel.setSearchInType("Search in metadata and file contents");
					Utils.fluentWait(driver);
					homePage.searchPanel.clickSearch();
					Utils.fluentWait(driver);
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.11A : Create some file with the names starting with 'Tes'  - Search in Metadata - With Search Results
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "SearchInMetadataOrContent"}, 
			description = "Create some file with the names starting with 'Tes'  - Search in Metadata - With Search Results")
	public void SprintTest24_2_11A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			int expectedCount = 0;
			int count = 0;

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Perform Pre-defined search with search word 
			//----------------------------------------------------
			if(!homePage.searchPanel.isSearchInTypeExists(dataPool.get("SearchInType")))
				Log.fail("Test Case Failed. The Given 'Search In type' " + dataPool.get("SearchInType") + " was not found.", driver);
			Utils.fluentWait(driver);
			homePage.searchPanel.setSearchInType(dataPool.get("SearchInType"));
			homePage.searchPanel.search(dataPool.get("SearchWord"), Caption.Search.SearchAllObjects.Value);
			Utils.fluentWait(driver);

			Log.message("Step-1: Perform Pre-defined search with search word .");

			//Verification: To verify if only the objects satisfying the condition are present in the search results
			//------------------------------------------------------------------------------------------------------
			Utils.fluentWait(driver);
			homePage.listView.insertColumn(dataPool.get("Property"));
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			expectedCount = homePage.listView.itemCount();

			for(count = 0; count < expectedCount; count++){
				if(homePage.listView.getColumnValueByItemIndex(count, dataPool.get("Property")).equals(dataPool.get("Value"))) {
					if(!homePage.listView.getItemNameByItemIndex(count).toLowerCase().contains((dataPool.get("SearchWord").replace("*", "").toLowerCase())))
						break;
				}
			}

			homePage.listView.removeColumn(dataPool.get("Property"));

			if(count == expectedCount)
				Log.pass("Test Case Passed. Only the objects of the specified text in metadata were found in the search results.");
			else
				Log.fail("Test Case Failed. The object that did not satisfy the condition was also present in the Search result.", driver);
		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			if(!driver.equals(null)){
				try
				{
					HomePage homePage = new HomePage(driver);
					homePage.searchPanel.setSearchInType("Search in metadata and file contents");
					Utils.fluentWait(driver);
					homePage.searchPanel.clickSearch();
					Utils.fluentWait(driver);
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.11B : Create some file with the names starting with 'Tes'  - Search in Metadata - With no Search Results
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "SearchInMetadataOrContent"}, 
			description = "Create some file with the names starting with 'Tes'  - Search in Metadata - With no Search Results")
	public void SprintTest24_2_11B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Perform Pre-defined search with search word 
			//----------------------------------------------------
			if(!homePage.searchPanel.isSearchInTypeExists(dataPool.get("SearchInType")))
				Log.fail("Test Case Failed. The Given 'Search In type' " + dataPool.get("SearchInType") + " was not found.", driver);
			Utils.fluentWait(driver);
			homePage.searchPanel.setSearchInType(dataPool.get("SearchInType"));
			homePage.searchPanel.search(dataPool.get("SearchWord"), Caption.Search.SearchAllObjects.Value);
			Utils.fluentWait(driver);

			Log.message("Step-1: Perform Pre-defined search with search word .");

			//Verification: To verify if only the objects satisfying the condition are present in the search results
			//------------------------------------------------------------------------------------------------------
			Utils.fluentWait(driver);
			if(homePage.listView.itemCount() != 0) 
				throw new SkipException("The Search results was not empty.");

			if(driver.findElement(By.id("noResultsText")).isDisplayed() && driver.findElement(By.id("noResultsText")).getText().equals(dataPool.get("ExpectedMessage")))
				Log.pass("Test Case Passed. The Expected Message was displayed when there is no search result to be displayed.");
			else
				Log.fail("Test Case Failed. The Expected Message was not displayed when there is no search result to be displayed.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			if(!driver.equals(null)){
				try
				{
					HomePage homePage = new HomePage(driver);
					homePage.searchPanel.setSearchInType("Search in metadata and file contents");
					Utils.fluentWait(driver);
					homePage.searchPanel.clickSearch();
					Utils.fluentWait(driver);
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.12A : Create some file with the names starting with 'Tes'  - Search in File content - With Search Results
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "SearchInMetadataOrContent"}, 
			description = "Create some file with the names starting with 'Tes'  - Search in File content - With Search Results")
	public void SprintTest24_2_12A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			String[] expectedObjects = dataPool.get("ExpectedObjects").split("\n");
			int count = 0;

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Perform Pre-defined search with search word 
			//----------------------------------------------------
			if(!homePage.searchPanel.isSearchInTypeExists(dataPool.get("SearchInType")))
				Log.fail("Test Case Failed. The Given 'Search In type' " + dataPool.get("SearchInType") + " was not found.", driver);
			Utils.fluentWait(driver);
			homePage.searchPanel.setSearchInType(dataPool.get("SearchInType"));
			homePage.searchPanel.search(dataPool.get("SearchWord"), Caption.Search.SearchAllObjects.Value);
			Utils.fluentWait(driver);

			Log.message("Step-1: Perform Pre-defined search with search word .");

			//Verification: To verify if only the objects satisfying the condition are present in the search results
			//------------------------------------------------------------------------------------------------------
			if(homePage.listView.itemCount() == 0) 
				throw new SkipException("The Search results was empty, even when there are objects in the vault that satisfy the condition.");

			if(homePage.listView.itemCount() != expectedObjects.length) 
				throw new SkipException("The Number of objects in the search results did not match the expected objects count.");

			for(count = 0; count < expectedObjects.length; count++){
				if(!homePage.listView.isItemExists(expectedObjects[count]))
					break;
			}

			if(count == expectedObjects.length)
				Log.pass("Test Case Passed. Only the objects of the specified text in content were found in the search results.");
			else 
				Log.fail("Test Case Failed. The object that did not satisfy the condition was also present in the Search result.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			if(!driver.equals(null)){
				try
				{
					HomePage homePage = new HomePage(driver);
					homePage.searchPanel.setSearchInType("Search in metadata and file contents");
					Utils.fluentWait(driver);
					homePage.searchPanel.clickSearch();
					Utils.fluentWait(driver);
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.12B :Create some file with the names starting with 'Tes'  - Search in File content - with no Search results
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "SearchInMetadataOrContent"}, 
			description = "Create some file with the names starting with 'Tes'  - Search in File content - with no Search results")
	public void SprintTest24_2_12B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Perform Pre-defined search with search word 
			//----------------------------------------------------
			if(!homePage.searchPanel.isSearchInTypeExists(dataPool.get("SearchInType")))
				Log.fail("Test Case Failed. The Given 'Search In type' " + dataPool.get("SearchInType") + " was not found.", driver);

			Utils.fluentWait(driver);
			homePage.searchPanel.setSearchInType(dataPool.get("SearchInType"));
			homePage.searchPanel.search(dataPool.get("SearchWord"), Caption.Search.SearchAllObjects.Value);
			Utils.fluentWait(driver);

			Log.message("Step-1: Perform Pre-defined search with search word .");

			//Verification: To verify if only the objects satisfying the condition are present in the search results
			//------------------------------------------------------------------------------------------------------
			Utils.fluentWait(driver);
			if(homePage.listView.itemCount() != 0) 
				throw new SkipException("The Search results was not empty.");

			if(driver.findElement(By.id("noResultsText")).isDisplayed() && driver.findElement(By.id("noResultsText")).getText().equals(dataPool.get("ExpectedMessage")))
				Log.pass("Test Case Passed. The Expected Message was displayed when there is no search result to be displayed.");
			else
				Log.fail("Test Case Failed. The Expected Message was not displayed when there is no search result to be displayed.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			if(!driver.equals(null)){
				try
				{
					HomePage homePage = new HomePage(driver);
					homePage.searchPanel.setSearchInType("Search in metadata and file contents");
					Utils.fluentWait(driver);
					homePage.searchPanel.clickSearch();
					Utils.fluentWait(driver);
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.13A : Create some file with the names starting with 'Tes'  - Search in Metadata and file content - With Search Result
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "SearchInMetadataOrContent"}, 
			description = "Create some file with the names starting with 'Tes'  - Search in Metadata and file content - With Search Result")
	public void SprintTest24_2_13A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			int expectedCount = 0;
			int count = 0;

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Perform Pre-defined search with search word 
			//----------------------------------------------------
			if(!homePage.searchPanel.isSearchInTypeExists(dataPool.get("SearchInType")))
				Log.fail("Test Case Failed. The Given 'Search In type' " + dataPool.get("SearchInType") + " was not found.", driver);

			Utils.fluentWait(driver);
			homePage.searchPanel.setSearchInType(dataPool.get("SearchInType"));
			homePage.searchPanel.search(dataPool.get("SearchWord"), Caption.Search.SearchAllObjects.Value);
			Utils.fluentWait(driver);

			Log.message("Step-1: Perform Pre-defined search with search word .");

			//Verification: To verify if only the objects satisfying the condition are present in the search results
			//------------------------------------------------------------------------------------------------------
			if(homePage.listView.itemCount() == 0) 
				throw new SkipException("The Search results was empty, even when there are objects in the vault that satisfy the condition.");

			Utils.fluentWait(driver);
			homePage.listView.insertColumn(dataPool.get("Property"));
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			expectedCount = homePage.listView.itemCount();

			for(count = 0; count < expectedCount; count++){
				if(homePage.listView.getColumnValueByItemIndex(count, dataPool.get("Property")).equals(dataPool.get("Value"))) {
					if(!homePage.listView.getItemNameByItemIndex(count).toLowerCase().contains((dataPool.get("SearchWord").replace("*", "").toLowerCase())))
						break;
				}
			}

			homePage.listView.removeColumn(dataPool.get("Property"));

			if(count == expectedCount)
				Log.pass("Test Case Passed. Only the objects of the specified text in metadata were found in the search results.");
			else
				Log.fail("Test Case Failed. The object that did not satisfy the condition was also present in the Search result.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			if(!driver.equals(null)){
				try
				{
					HomePage homePage = new HomePage(driver);
					homePage.searchPanel.setSearchInType("Search in metadata and file contents");
					Utils.fluentWait(driver);
					homePage.searchPanel.clickSearch();
					Utils.fluentWait(driver);
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.13B : Create some file with the names starting with 'Tes'  - Search in Metadata and file content - Without Search Result
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "SearchInMetadataOrContent"}, 
			description = "Create some file with the names starting with 'Tes'  - Search in Metadata and file content - Without Search Result")
	public void SprintTest24_2_13B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Perform Pre-defined search with search word 
			//----------------------------------------------------
			if(!homePage.searchPanel.isSearchInTypeExists(dataPool.get("SearchInType")))
				Log.fail("Test Case Failed. The Given 'Search In type' " + dataPool.get("SearchInType") + " was not found.", driver);

			Utils.fluentWait(driver);
			homePage.searchPanel.setSearchInType(dataPool.get("SearchInType"));
			homePage.searchPanel.search(dataPool.get("SearchWord"), Caption.Search.SearchAllObjects.Value);
			Utils.fluentWait(driver);

			Log.message("Step-1: Perform Pre-defined search with search word .");

			//Verification: To verify if only the objects satisfying the condition are present in the search results
			//------------------------------------------------------------------------------------------------------
			Utils.fluentWait(driver);
			if(homePage.listView.itemCount() != 0) 
				throw new SkipException("The Search results was not empty.");

			if(driver.findElement(By.id("noResultsText")).isDisplayed() && driver.findElement(By.id("noResultsText")).getText().equals(dataPool.get("ExpectedMessage")))
				Log.pass("Test Case Passed. The Expected Message was displayed when there is no search result to be displayed.");
			else
				Log.fail("Test Case Failed. The Expected Message was not displayed when there is no search result to be displayed.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			if(!driver.equals(null)){
				try
				{
					HomePage homePage = new HomePage(driver);
					homePage.searchPanel.setSearchInType("Search in metadata and file contents");
					Utils.fluentWait(driver);
					homePage.searchPanel.clickSearch();
					Utils.fluentWait(driver);
				}
				catch(Exception e0)
				{
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);
		} //End finally

	}



}


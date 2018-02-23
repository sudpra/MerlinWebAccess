package MFClient.Tests.Search;

import genericLibrary.ActionEventUtils;
import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.TestMethodWebDriverManager;
import genericLibrary.Utils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class AdvancedOptionsInQuickSearch {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String userName = null;
	public static String password = null;
	public static String testVault = null;
	public static String configURL = null;
	public static String userFullName = null;
	public static String className = null;
	public static String productVersion = null;
	//public static WebDriver driver = null;

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
			password = xmlParameters.getParameter("Password");
			testVault = xmlParameters.getParameter("VaultName");		
			userFullName = xmlParameters.getParameter("UserFullName");
			Utility.restoreTestVault();
			Utility.configureUsers(xlTestDataWorkBook);
			className = this.getClass().getSimpleName().toString().trim();

			driverManager = new TestMethodWebDriverManager();

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
			Utility.destroyUsers(xlTestDataWorkBook);
			Utility.destroyTestVault();

		}//End try

		catch(Exception e){
			throw e;
		}//End Catch
	}


	/**
	 * 24.2.18A : Check for default options in Advanced search - Radio Button Options
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "AdvancedOptionsInQuickSearch"}, 
			description = "Check for default options in Advanced search - Radio Button Options")
	public void SprintTest24_2_18A(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Click the Advanced Search Link 
			//---------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Click the Advanced Search Link ");

			//Verification: To verify if only the objects satisfying the condition are present in the search results
			//------------------------------------------------------------------------------------------------------
			Utils.fluentWait(driver);
			String css = "html.js body.ui-widget div#page.ui-layout-center div#rightPanel.ui-layout-center div#browseHolder.ui-layout-center div#browse.ui-layout-center div#search.ui-layout-north form#searchForm div#searchAdvanced div#searchType.searchRow input#searchAllWordsButton";
			WebElement allWords = driver.findElement(By.cssSelector(css));
			WebElement anyWord = driver.findElement(By.id("searchAnyWordButton"));
			WebElement booleanOption = driver.findElement(By.id("searchBooleanButton"));

			if(allWords.isDisplayed() && allWords.isEnabled() && anyWord.isDisplayed() && anyWord.isEnabled() && booleanOption.isDisplayed() && booleanOption.isEnabled())
				Log.pass("Test Case Passed. The Expected Radio button controls were present.");
			else
				Log.fail("Test Case Failed. The Expected radio button controls were not found.", driver);
		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.19A : Condition Sorting:  'All words' + '<Objects>' +  'is' (With Search Result) 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "AdvancedOptionsInQuickSearch"}, 
			description = "Condition Sorting:  'All words' + '<Objects>' +  'is' (With Search Result)")
	public void SprintTest24_2_19A(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			int expectedCount = 0;
			int counter = 0;
			int count = 0;
			String[] searchWord = dataPool.get("SearchWord").split(" ");

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Click the Advanced Search Link 
			//---------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Click the Advanced Search Link ");

			//Step-2: Set the necessary conditions
			//-------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), dataPool.get("Value"));
			homePage.searchPanel.search(dataPool.get("SearchWord"), "");
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the necessary conditions");

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
				if(!homePage.listView.getColumnValueByItemIndex(count, dataPool.get("Property")).equals(dataPool.get("Value"))) 
					break;
				for(counter = 0; counter < searchWord.length; counter++) {
					if(!homePage.listView.getItemNameByItemIndex(count).toLowerCase().contains((searchWord[counter].toLowerCase())))
						break;
				}
			}

			homePage.listView.removeColumn(dataPool.get("Property"));

			if(count == expectedCount)
				Log.pass("Test Case Passed. Only the objects that satisfied the conditions were present in the Search result.");
			else
				Log.fail("Test Case Failed. The object that did not satisfy the condition was also present in the Search result.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.19B : Condition Sorting:  'All words' + '<Objects>' +  'is' (Without Search Results) 
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "AdvancedOptionsInQuickSearch"}, 
			description = "Condition Sorting:  'All words' + '<Objects>' +  'is' (Without Search Results)")
	public void SprintTest24_2_19B(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Click the Advanced Search Link 
			//---------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Click the Advanced Search Link ");

			//Step-2: Set the necessary conditions
			//-------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), dataPool.get("Value"));
			homePage.searchPanel.search(dataPool.get("SearchWord"), "");
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the necessary conditions");

			//Verification: To verify if only the objects satisfying the condition are present in the search results
			//------------------------------------------------------------------------------------------------------
			Utils.fluentWait(driver);
			if(homePage.listView.itemCount() != 0) 
				throw new SkipException("The Search results was not empty, when it is supposed to be.");

			if(driver.findElement(By.id("noResultsText")).isDisplayed() && driver.findElement(By.id("noResultsText")).getText().equals(dataPool.get("ExpectedMessage")))
				Log.pass("Test Case Passed. The Expected Message was displayed as there is no search result to be displayed.");
			else
				Log.fail("Test Case Failed. The Expected Message was not displayed even when there is no search result to be displayed.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.20 : Condition Sorting:  'All words' + '<Objects>' +  'is not'  
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "Search"}, 
			description = "Condition Sorting:  'All words' + '<Objects>' +  'is not' ")
	public void SprintTest24_2_20(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());	

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			int expectedCount = 0;
			int counter = 0;
			int count = 0;
			String[] searchWord = dataPool.get("SearchWord").split(" ");


			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Click the Advanced Search Link 
			//---------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Click the Advanced Search Link ");

			//Step-2: Set the necessary conditions
			//-------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), dataPool.get("Value"));
			homePage.searchPanel.search(dataPool.get("SearchWord"), "");
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the necessary conditions");

			//Verification: To verify if only the objects satisfying the condition are present in the search results
			//------------------------------------------------------------------------------------------------------
			if(dataPool.get("SearchResultExpected").equals("Yes")) {

				if(homePage.listView.itemCount() == 0) 
					Log.fail("Test Case Failed. The Search results was empty, even when there are objects in the vault that satisfy the condition.", driver);

				homePage.listView.insertColumn(dataPool.get("Property"));
				Utils.fluentWait(driver);

				Utils.fluentWait(driver);
				expectedCount = homePage.listView.itemCount();

				for(count = 0; count < expectedCount; count++){
					if(homePage.listView.getColumnValueByItemIndex(count, dataPool.get("Property")).equals(dataPool.get("Value"))) 
						break;
					for(counter = 0; counter < searchWord.length; counter++) {
						if(!homePage.listView.getItemNameByItemIndex(count).toLowerCase().contains((searchWord[counter].toLowerCase())))
							break;
					}
				}

				homePage.listView.removeColumn(dataPool.get("Property"));

				if(count == expectedCount)
					Log.pass("Test Case Passed. Only the objects that satisfied the conditions were present in the Search result.");
				else
					Log.fail("Test Case Failed. The object that did not satisfy the condition was also present in the Search result.", driver);
			}
			else {
				Utils.fluentWait(driver);
				if(homePage.listView.itemCount() != 0) 
					Log.fail("Test Case Failed. The Search results was not empty, when it is supposed to be.", driver);

				if(driver.findElement(By.id("noResultsText")).isDisplayed() && driver.findElement(By.id("noResultsText")).getText().equals(dataPool.get("ExpectedMessage")))
					Log.pass("Test Case Passed. The Expected Message was displayed as there is no search result to be displayed.");
				else
					Log.fail("Test Case Failed. The Expected Message was not displayed even when there is no search result to be displayed.", driver);

			}
		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.21 : Condition Sorting:  'All words' + '<Objects>' +  'is empty'   
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "AdvancedOptionsInQuickSearch"}, 
			description = "Condition Sorting:  'All words' + '<Objects>' +  'is empty' ")
	public void SprintTest24_2_21(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {		



			driver = driverManager.startTesting(Utility.getMethodName());

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			int expectedCount = 0;
			int counter = 0;
			int count = 0;
			String[] searchWord = dataPool.get("SearchWord").split(" ");

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Click the Advanced Search Link 
			//---------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Click the Advanced Search Link ");

			//Step-2: Set the necessary conditions
			//-------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), "");
			homePage.searchPanel.search(dataPool.get("SearchWord"), "");
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the necessary conditions");

			//Verification: To verify if only the objects satisfying the condition are present in the search results
			//------------------------------------------------------------------------------------------------------
			if(dataPool.get("SearchResultExpected").equals("Yes")) {

				if(homePage.listView.itemCount() == 0) 
					throw new SkipException("The Search results was empty, even when there are objects in the vault that satisfy the condition.");

				Utils.fluentWait(driver);
				homePage.listView.insertColumn(dataPool.get("Property"));
				Utils.fluentWait(driver);

				Utils.fluentWait(driver);
				expectedCount = homePage.listView.itemCount();

				for(count = 0; count < expectedCount; count++){
					if(!homePage.listView.getColumnValueByItemIndex(count, dataPool.get("Property")).equals("")) 
						break;
					for(counter = 0; counter < searchWord.length; counter++) {
						if(!homePage.listView.getItemNameByItemIndex(count).toLowerCase().contains((searchWord[counter].toLowerCase())))
							break;
					}
				}

				homePage.listView.removeColumn(dataPool.get("Property"));

				if(count == expectedCount)
					Log.pass("Test Case Passed. Only the objects that satisfied the conditions were present in the Search result.");
				else
					Log.fail("Test Case Failed. The object that did not satisfy the condition was also present in the Search result.", driver);
			}
			else {
				Utils.fluentWait(driver);
				if(homePage.listView.itemCount() != 0) 
					throw new SkipException("The Search results was not empty, when it is supposed to be.");

				if(driver.findElement(By.id("noResultsText")).isDisplayed() && driver.findElement(By.id("noResultsText")).getText().equals(dataPool.get("ExpectedMessage")))
					Log.pass("Test Case Passed. The Expected Message was displayed as there is no search result to be displayed.");
				else
					Log.fail("Test Case Failed. The Expected Message was not displayed even when there is no search result to be displayed.", driver);

			}
		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.22 : Condition Sorting:  'All words' + '<Objects>' +  'is not empty'    
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "AdvancedOptionsInQuickSearch"}, 
			description = "Condition Sorting:  'All words' + '<Objects>' +  'is not empty' ")
	public void SprintTest24_2_22(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			int expectedCount = 0;
			int counter = 0;
			int count = 0;
			String[] searchWord = dataPool.get("SearchWord").split(" ");

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Click the Advanced Search Link 
			//---------------------------------------

			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Click the Advanced Search Link ");

			//Step-2: Set the necessary conditions
			//-------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), "");
			homePage.searchPanel.search(dataPool.get("SearchWord"), "");
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the necessary conditions");

			//Verification: To verify if only the objects satisfying the condition are present in the search results
			//------------------------------------------------------------------------------------------------------
			if(dataPool.get("SearchResultExpected").equals("Yes")) {

				if(homePage.listView.itemCount() == 0) 
					throw new SkipException("The Search results was empty, even when there are objects in the vault that satisfy the condition.");

				Utils.fluentWait(driver);
				homePage.listView.insertColumn(dataPool.get("Property"));
				Utils.fluentWait(driver);

				Utils.fluentWait(driver);
				expectedCount = homePage.listView.itemCount();

				for(count = 0; count < expectedCount; count++){
					if(homePage.listView.getColumnValueByItemIndex(count, dataPool.get("Property")).equals("")) 
						break;
					for(counter = 0; counter < searchWord.length; counter++) {
						if(!homePage.listView.getItemNameByItemIndex(count).toLowerCase().contains((searchWord[counter].toLowerCase())))
							break;
					}
				}

				homePage.listView.removeColumn(dataPool.get("Property"));

				if(count == expectedCount)
					Log.pass("Test Case Passed. Only the objects that satisfied the conditions were present in the Search result.");
				else
					Log.fail("Test Case Failed. The object that did not satisfy the condition was also present in the Search result.", driver);
			}
			else {
				Utils.fluentWait(driver);
				if(homePage.listView.itemCount() != 0) 
					throw new SkipException("The Search results was not empty, when it is supposed to be.");

				if(driver.findElement(By.id("noResultsText")).isDisplayed() && driver.findElement(By.id("noResultsText")).getText().equals(dataPool.get("ExpectedMessage")))
					Log.pass("Test Case Passed. The Expected Message was displayed as there is no search result to be displayed.");
				else
					Log.fail("Test Case Failed. The Expected Message was not displayed even when there is no search result to be displayed.", driver);

			}
		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.23A : Condition Sorting:  'Any word' + '<Objects>' +  'is' (With Search Results)  
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "AdvancedOptionsInQuickSearch"}, 
			description = "Condition Sorting:  'Any word' + '<Objects>' +  'is' (With Search Results)")
	public void SprintTest24_2_23A(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			int expectedCount = 0;
			int counter = 0;
			int flag = 0;
			int count = 0;
			String[] searchWord = dataPool.get("SearchWord").split(" ");

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Click the Advanced Search Link 
			//---------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Click the Advanced Search Link ");

			//Step-2: Set the necessary conditions
			//-------------------------------------
			homePage.searchPanel.setSearchOption(Caption.Search.SearchAnyWord.Value);
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), dataPool.get("Value"));
			homePage.searchPanel.search(dataPool.get("SearchWord"), "");
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the necessary conditions");

			//Verification: To verify if only the objects satisfying the condition are present in the search results
			//------------------------------------------------------------------------------------------------------
			if(homePage.listView.itemCount() == 0) 
				throw new SkipException("The Search results was empty, even when there are objects in the vault that satisfy the condition.");

			Utils.fluentWait(driver);
			homePage.listView.insertColumn(dataPool.get("Property"));
			Utils.fluentWait(driver);

			expectedCount = homePage.listView.itemCount();

			for(count = 0; count < expectedCount; count++){
				if(!homePage.listView.getColumnValueByItemIndex(count, dataPool.get("Property")).equals(dataPool.get("Value"))) 
					break;

				flag = 0;
				for(counter = 0; counter < searchWord.length; counter++) {
					if(!homePage.listView.getItemNameByItemIndex(count).toLowerCase().contains((searchWord[counter].toLowerCase())))
						flag++;
				}

				if(flag == searchWord.length)
					break;
			}

			homePage.listView.removeColumn(dataPool.get("Property"));

			if(count == expectedCount)
				Log.pass("Test Case Passed. Only the objects that satisfied the conditions were present in the Search result.");
			else
				Log.fail("Test Case Failed. The object that did not satisfy the condition was also present in the Search result.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.23B : Condition Sorting:  'Any word' + '<Objects>' +  'is' (Without Search Results)  
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "AdvancedOptionsInQuickSearch"}, 
			description = "Condition Sorting:  'Any word' + '<Objects>' +  'is' (Without Search Results)")
	public void SprintTest24_2_23B(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Click the Advanced Search Link 
			//---------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Click the Advanced Search Link ");

			//Step-2: Set the necessary conditions
			//-------------------------------------
			homePage.searchPanel.setSearchOption(Caption.Search.SearchAnyWord.Value);
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), dataPool.get("Value"));
			homePage.searchPanel.search(dataPool.get("SearchWord"), "");
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the necessary conditions");

			//Verification: To verify if only the objects satisfying the condition are present in the search results
			//------------------------------------------------------------------------------------------------------
			Utils.fluentWait(driver);
			if(homePage.listView.itemCount() != 0) 
				throw new SkipException("The Search results was not empty, when it is supposed to be.");

			if(driver.findElement(By.id("noResultsText")).isDisplayed() && driver.findElement(By.id("noResultsText")).getText().equals(dataPool.get("ExpectedMessage")))
				Log.pass("Test Case Passed. The Expected Message was displayed as there is no search result to be displayed.");
			else
				Log.fail("Test Case Failed. The Expected Message was not displayed even when there is no search result to be displayed.", driver);


		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.24A : Condition Sorting:  'Any word' + '<Objects>' +  'is not' (With Search Results)   
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "AdvancedOptionsInQuickSearch"}, 
			description = "Condition Sorting:  'Any word' + '<Objects>' +  'is not' (With Search Results)")
	public void SprintTest24_2_24A(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			int expectedCount = 0;
			int counter = 0;
			int flag = 0;
			int count = 0;
			String[] searchWord = dataPool.get("SearchWord").split(" ");

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Click the Advanced Search Link 
			//---------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Click the Advanced Search Link ");

			//Step-2: Set the necessary conditions
			//-------------------------------------
			homePage.searchPanel.setSearchOption(Caption.Search.SearchAnyWord.Value);
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), dataPool.get("Value"));
			homePage.searchPanel.search(dataPool.get("SearchWord"), "");
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the necessary conditions");

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
				if(homePage.listView.getColumnValueByItemIndex(count, dataPool.get("Property")).equals(dataPool.get("Value"))) 
					break;

				flag = 0;
				for(counter = 0; counter < searchWord.length; counter++) {
					if(!homePage.listView.getItemNameByItemIndex(count).toLowerCase().contains((searchWord[counter].toLowerCase())))
						flag++;
				}

				if(flag == searchWord.length)
					break;
			}

			homePage.listView.removeColumn(dataPool.get("Property"));

			if(count == expectedCount)
				Log.pass("Test Case Passed. Only the objects that satisfied the conditions were present in the Search result.");
			else
				Log.fail("Test Case Failed. The object that did not satisfy the condition was also present in the Search result.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.24B : Condition Sorting:  'Any word' + '<Objects>' +  'is not' (Without Search Results)   
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "AdvancedOptionsInQuickSearch"}, 
			description = "Condition Sorting:  'Any word' + '<Objects>' +  'is not' (Without Search Results)")
	public void SprintTest24_2_24B(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Click the Advanced Search Link 
			//---------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Click the Advanced Search Link ");

			//Step-2: Set the necessary conditions
			//-------------------------------------
			homePage.searchPanel.setSearchOption(Caption.Search.SearchAnyWord.Value);
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), dataPool.get("Value"));
			homePage.searchPanel.search(dataPool.get("SearchWord"), "");
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the necessary conditions");

			//Verification: To verify if only the objects satisfying the condition are present in the search results
			//------------------------------------------------------------------------------------------------------
			Utils.fluentWait(driver);
			if(homePage.listView.itemCount() != 0) 
				throw new SkipException("The Search results was not empty, when it is supposed to be.");

			if(driver.findElement(By.id("noResultsText")).isDisplayed() && driver.findElement(By.id("noResultsText")).getText().equals(dataPool.get("ExpectedMessage")))
				Log.pass("Test Case Passed. The Expected Message was displayed as there is no search result to be displayed.");
			else
				Log.fail("Test Case Failed. The Expected Message was not displayed even when there is no search result to be displayed.", driver);
		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.25A : Condition Sorting:  'Any word' + '<Objects>' +  'is empty'  (With Search Results)      
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "AdvancedOptionsInQuickSearch","Bug"}, 
			description = "Condition Sorting:  'Any word' + '<Objects>' +  'is empty' (With Search Results)")
	public void SprintTest24_2_25A(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			int expectedCount = 0;
			int counter = 0;
			int flag = 0;
			int count = 0;
			String[] searchWord = dataPool.get("SearchWord").split(" ");

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Click the Advanced Search Link 
			//---------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Click the Advanced Search Link ");

			//Step-2: Set the necessary conditions
			//-------------------------------------
			Utils.fluentWait(driver);
			homePage.searchPanel.setSearchOption(Caption.Search.SearchAnyWord.Value);
			Utils.fluentWait(driver);
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), "");
			homePage.searchPanel.search(dataPool.get("SearchWord"), "");
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the necessary conditions");

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

				if(!homePage.listView.getColumnValueByItemIndex(count, dataPool.get("Property")).equals("")) 
					break;

				else if (homePage.listView.getColumnValueByItemIndex(count, dataPool.get("Property")).equals(""))

					flag = 0;
				for(counter = 0; counter < searchWord.length; counter++) {
					if(!homePage.listView.getItemNameByItemIndex(count).toLowerCase().contains((searchWord[counter].toLowerCase())))
						flag++;
				}

				if(flag == searchWord.length)
					break;

			}

			homePage.listView.removeColumn(dataPool.get("Property"));

			if(count == expectedCount)
				Log.pass("Test Case Passed. Only the objects that satisfied the conditions were present in the Search result.");
			else
				Log.fail("Test Case Failed. The object that did not satisfy the condition was also present in the Search result.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.25B : Condition Sorting:  'Any word' + '<Objects>' +  'is empty' (Without Search Results)    
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "AdvancedOptionsInQuickSearch"}, 
			description = "Condition Sorting:  'Any word' + '<Objects>' +  'is empty' (Without Search Results)")
	public void SprintTest24_2_25B(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			dataPool = new ConcurrentHashMap <String, String>(dataValues);


			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Click the Advanced Search Link 
			//---------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Click the Advanced Search Link ");

			//Step-2: Set the necessary conditions
			//-------------------------------------
			homePage.searchPanel.setSearchOption(Caption.Search.SearchAnyWord.Value);
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), "");
			homePage.searchPanel.search(dataPool.get("SearchWord"), "");
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the necessary conditions");

			//Verification: To verify if only the objects satisfying the condition are present in the search results
			//------------------------------------------------------------------------------------------------------
			Utils.fluentWait(driver);
			if(homePage.listView.itemCount() != 0) 
				throw new SkipException("The Search results was not empty, when it is supposed to be.");

			if(driver.findElement(By.id("noResultsText")).isDisplayed() && driver.findElement(By.id("noResultsText")).getText().equals(dataPool.get("ExpectedMessage")))
				Log.pass("Test Case Passed. The Expected Message was displayed as there is no search result to be displayed.");
			else
				Log.fail("Test Case Failed. The Expected Message was not displayed even when there is no search result to be displayed.", driver);


		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.26A : Condition Sorting:  'Any word' + '<Objects>' +  'is not empty' (With Search Results)  
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "AdvancedOptionsInQuickSearch"}, 
			description = "Condition Sorting:  'Any word' + '<Objects>' +  'is not empty' (With Search Results)")
	public void SprintTest24_2_26A(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			int expectedCount = 0;
			int counter = 0;
			int count = 0;
			int flag = 0;
			String[] searchWord = dataPool.get("SearchWord").split(" ");

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Click the Advanced Search Link 
			//---------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Click the Advanced Search Link ");

			//Step-2: Set the necessary conditions
			//-------------------------------------
			homePage.searchPanel.setSearchOption(Caption.Search.SearchAnyWord.Value);
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), "");
			homePage.searchPanel.search(dataPool.get("SearchWord"), "");
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the necessary conditions");

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
				if(homePage.listView.getColumnValueByItemIndex(count, dataPool.get("Property")).equals("")) 
					break;

				flag = 0;
				for(counter = 0; counter < searchWord.length; counter++) {
					if(!homePage.listView.getItemNameByItemIndex(count).toLowerCase().contains((searchWord[counter].toLowerCase())))
						flag++;
				}

				if(flag == searchWord.length)
					break;
			}

			homePage.listView.removeColumn(dataPool.get("Property"));

			if(count == expectedCount)
				Log.pass("Test Case Passed. Only the objects that satisfied the conditions were present in the Search result.");
			else
				Log.fail("Test Case Failed. The object that did not satisfy the condition was also present in the Search result.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.26B : Condition Sorting:  'Any word' + '<Objects>' +  'is not empty' (Without Search Results)     
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "AdvancedOptionsInQuickSearch"}, 
			description = "Condition Sorting:  'Any word' + '<Objects>' +  'is not empty' (Without Search Results)")
	public void SprintTest24_2_26B(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Click the Advanced Search Link 
			//---------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Click the Advanced Search Link ");

			//Step-2: Set the necessary conditions
			//-------------------------------------
			homePage.searchPanel.setSearchOption(Caption.Search.SearchAnyWord.Value);
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), "");
			homePage.searchPanel.search(dataPool.get("SearchWord"), "");
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the necessary conditions");

			//Verification: To verify if only the objects satisfying the condition are present in the search results
			//------------------------------------------------------------------------------------------------------
			Utils.fluentWait(driver);
			if(homePage.listView.itemCount() != 0) 
				throw new SkipException("The Search results was not empty, when it is supposed to be.");

			if(driver.findElement(By.id("noResultsText")).isDisplayed() && driver.findElement(By.id("noResultsText")).getText().equals(dataPool.get("ExpectedMessage")))
				Log.pass("Test Case Passed. The Expected Message was displayed as there is no search result to be displayed.");
			else
				Log.fail("Test Case Failed. The Expected Message was not displayed even when there is no search result to be displayed.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.27A : Condition Sorting:  'Booelan (+)' + '<Objects>' +  'Condition' (With Search Results)     
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "Search", "Smoke"}, 
			description = "Condition Sorting:  'Booelan (+)' + '<Objects>' +  'Condition' (With Search Results)")
	public void SprintTest24_2_27A(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			int expectedCount = 0;
			int count = 0;

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Click the Advanced Search Link 
			//---------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Click the Advanced Search Link ");

			//Step-2: Set the necessary conditions
			//-------------------------------------
			homePage.searchPanel.setSearchOption(Caption.Search.SearchBoolean.Value);
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), dataPool.get("Value"));
			homePage.searchPanel.search(dataPool.get("SearchWord"), Caption.Search.SearchAllObjects.Value);
			String searchWord = dataPool.get("SearchWord").replace("+", "");
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the necessary conditions");

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
				if(!homePage.listView.getColumnValueByItemIndex(count, dataPool.get("Property")).equals(dataPool.get("Value"))) 
					break;

				if(!homePage.listView.getItemNameByItemIndex(count).toLowerCase().contains((searchWord.toLowerCase())))
					break;

			}

			homePage.listView.removeColumn(dataPool.get("Property"));

			if(count == expectedCount)
				Log.pass("Test Case Passed. Only the objects that satisfied the conditions were present in the Search result.");
			else
				Log.fail("Test Case Failed. The object that did not satisfy the condition was also present in the Search result.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.27B : Condition Sorting:  'Booelan (+)' + '<Objects>' +  'Condition' (Without Search Results)     
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "AdvancedOptionsInQuickSearch"}, 
			description = "Condition Sorting:  'Booelan (+)' + '<Objects>' +  'Condition' (Without Search Results)")
	public void SprintTest24_2_27B(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Click the Advanced Search Link 
			//---------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Click the Advanced Search Link ");

			//Step-2: Set the necessary conditions
			//-------------------------------------
			homePage.searchPanel.setSearchOption(Caption.Search.SearchBoolean.Value);
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), dataPool.get("Value"));
			homePage.searchPanel.search(dataPool.get("SearchWord"), Caption.Search.SearchAllObjects.Value);
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the necessary conditions");

			//Verification: To verify if only the objects satisfying the condition are present in the search results
			//------------------------------------------------------------------------------------------------------
			Utils.fluentWait(driver);
			if(homePage.listView.itemCount() != 0) 
				throw new SkipException("The Search results was not empty, when it is supposed to be.");

			if(driver.findElement(By.id("noResultsText")).isDisplayed() && driver.findElement(By.id("noResultsText")).getText().equals(dataPool.get("ExpectedMessage")))
				Log.pass("Test Case Passed. The Expected Message was displayed as there is no search result to be displayed.");
			else
				Log.fail("Test Case Failed. The Expected Message was not displayed even when there is no search result to be displayed.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.28A : Condition Sorting:  'Booelan (-)' + '<Objects>' +  'Condition' (With Search Results)      
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "AdvancedOptionsInQuickSearch"}, 
			description = "Condition Sorting:  'Booelan (-)' + '<Objects>' +  'Condition' (With Search Results)")
	public void SprintTest24_2_28A(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			int expectedCount = 0;
			int count = 0;

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Click the Advanced Search Link 
			//---------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Click the Advanced Search Link ");

			//Step-2: Set the necessary conditions
			//-------------------------------------
			homePage.searchPanel.setSearchOption(Caption.Search.SearchBoolean.Value);
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), dataPool.get("Value"));
			homePage.searchPanel.search(dataPool.get("SearchWord"), Caption.Search.SearchAllObjects.Value);
			String searchWord = dataPool.get("SearchWord").replace("-", "");
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the necessary conditions");

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
				if(!homePage.listView.getColumnValueByItemIndex(count, dataPool.get("Property")).equals(dataPool.get("Value"))) 
					break;

				if(!homePage.listView.getItemNameByItemIndex(count).toLowerCase().contains((searchWord.toLowerCase())))
					break;

			}

			homePage.listView.removeColumn(dataPool.get("Property"));

			if(count == expectedCount)
				Log.pass("Test Case Passed. Only the objects that satisfied the conditions were present in the Search result.");
			else
				Log.fail("Test Case Failed. The object that did not satisfy the condition was also present in the Search result.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.28B : Condition Sorting:  'Booelan (-)' + '<Objects>' +  'Condition' (Without Search Results)      
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "AdvancedOptionsInQuickSearch"}, 
			description = "Condition Sorting:  'Booelan (-)' + '<Objects>' +  'Condition' (Without Search Results)")
	public void SprintTest24_2_28B(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			dataPool = new ConcurrentHashMap <String, String>(dataValues);


			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Click the Advanced Search Link 
			//---------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Click the Advanced Search Link ");

			//Step-2: Set the necessary conditions
			//-------------------------------------
			homePage.searchPanel.setSearchOption(Caption.Search.SearchBoolean.Value);
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), dataPool.get("Value"));
			homePage.searchPanel.search(dataPool.get("SearchWord"), Caption.Search.SearchAllObjects.Value);
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the necessary conditions");

			//Verification: To verify if only the objects satisfying the condition are present in the search results
			//------------------------------------------------------------------------------------------------------
			Utils.fluentWait(driver);
			if(homePage.listView.itemCount() != 0) 
				throw new SkipException("The Search results was not empty, when it is supposed to be.");

			if(driver.findElement(By.id("noResultsText")).isDisplayed() && driver.findElement(By.id("noResultsText")).getText().equals(dataPool.get("ExpectedMessage")))
				Log.pass("Test Case Passed. The Expected Message was displayed as there is no search result to be displayed.");
			else
				Log.fail("Test Case Failed. The Expected Message was not displayed even when there is no search result to be displayed.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.29A : Condition Sorting:  'Booelan (Phrase)' + '<Objects>' +  'Condition (With Search Results)      
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "AdvancedOptionsInQuickSearch"}, 
			description = "Condition Sorting:  'Booelan (Phrase)' + '<Objects>' +  'Condition (With Search Results)")
	public void SprintTest24_2_29A(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			int expectedCount = 0;
			int count = 0;
			int flag = 0;

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Click the Advanced Search Link 
			//---------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Click the Advanced Search Link ");

			//Step-2: Set the necessary conditions
			//-------------------------------------
			homePage.searchPanel.setSearchOption(Caption.Search.SearchBoolean.Value);
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), dataPool.get("Value"));
			homePage.searchPanel.search(dataPool.get("SearchWord"), Caption.Search.SearchAllObjects.Value);
			String searchWord = dataPool.get("SearchWord").replace("\"", "");
			String[] searchWords = searchWord.split(" ");
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the necessary conditions");

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

				if(!homePage.listView.getColumnValueByItemIndex(count, dataPool.get("Property")).equals(dataPool.get("Value"))) 
					break;

				for(int counter = 0; counter < searchWords.length; counter++) {
					if(!homePage.listView.getItemNameByItemIndex(count).toLowerCase().contains((searchWords[counter].toLowerCase())))
						flag++;
				}

				if(flag != 0)
					break;

			}

			homePage.listView.removeColumn(dataPool.get("Property"));

			if(count == expectedCount)
				Log.pass("Test Case Passed. Only the objects that satisfied the conditions were present in the Search result.");
			else
				Log.fail("Test Case Failed. The object that did not satisfy the condition was also present in the Search result.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.29B : Condition Sorting:  'Booelan (Phrase)' + '<Objects>' +  'Condition (Without Search Results)      
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "AdvancedOptionsInQuickSearch"}, 
			description = "Condition Sorting:  'Booelan (Phrase)' + '<Objects>' +  'Condition (Without Search Results)")
	public void SprintTest24_2_29B(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			dataPool = new ConcurrentHashMap <String, String>(dataValues);


			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Click the Advanced Search Link 
			//---------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Click the Advanced Search Link ");

			//Step-2: Set the necessary conditions
			//-------------------------------------
			homePage.searchPanel.setSearchOption(Caption.Search.SearchBoolean.Value);
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), dataPool.get("Value"));
			homePage.searchPanel.search(dataPool.get("SearchWord"), Caption.Search.SearchAllObjects.Value);
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the necessary conditions");

			//Verification: To verify if only the objects satisfying the condition are present in the search results
			//------------------------------------------------------------------------------------------------------
			Utils.fluentWait(driver);
			if(homePage.listView.itemCount() != 0) 
				throw new SkipException("The Search results was not empty, when it is supposed to be.");

			if(driver.findElement(By.id("noResultsText")).isDisplayed() && driver.findElement(By.id("noResultsText")).getText().equals(dataPool.get("ExpectedMessage")))
				Log.pass("Test Case Passed. The Expected Message was displayed as there is no search result to be displayed.");
			else
				Log.fail("Test Case Failed. The Expected Message was not displayed even when there is no search result to be displayed.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.30A : Condition Sorting:  'Booelan (AND)' + '<Objects>' +  'Condition' (With Search Result)       
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "AdvancedOptionsInQuickSearch"}, 
			description = "Condition Sorting:  'Booelan (AND)' + '<Objects>' +  'Condition' (With Search Result)")
	public void SprintTest24_2_30A(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			int expectedCount = 0;
			int count = 0;
			int flag = 0;

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Click the Advanced Search Link 
			//---------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Click the Advanced Search Link ");

			//Step-2: Set the necessary conditions
			//-------------------------------------
			homePage.searchPanel.setSearchOption(Caption.Search.SearchBoolean.Value);
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), dataPool.get("Value"));
			homePage.searchPanel.search(dataPool.get("SearchWord"), Caption.Search.SearchAllObjects.Value);
			String searchWord = dataPool.get("SearchWord").replace("\"", "").replace("and", "");
			String[] searchWords = searchWord.split(" ");
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the necessary conditions");

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

				if(!homePage.listView.getColumnValueByItemIndex(count, dataPool.get("Property")).equals(dataPool.get("Value"))) 
					break;

				for(int counter = 0; counter < searchWords.length; counter++) {
					if(!homePage.listView.getItemNameByItemIndex(count).toLowerCase().contains((searchWords[counter].toLowerCase())))
						flag++;
				}

				if(flag != 0)
					break;

			}

			homePage.listView.removeColumn(dataPool.get("Property"));

			if(count == expectedCount)
				Log.pass("Test Case Passed. Only the objects that satisfied the conditions were present in the Search result.");
			else
				Log.fail("Test Case Failed. The object that did not satisfy the condition was also present in the Search result.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.30B : Condition Sorting:  'Booelan (AND)' + '<Objects>' +  'Condition' (Without Search Result)       
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "AdvancedOptionsInQuickSearch"}, 
			description = "Condition Sorting:  'Booelan (AND)' + '<Objects>' +  'Condition' (Without Search Result)")
	public void SprintTest24_2_30B(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Click the Advanced Search Link 
			//---------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Click the Advanced Search Link ");

			//Step-2: Set the necessary conditions
			//-------------------------------------
			homePage.searchPanel.setSearchOption(Caption.Search.SearchBoolean.Value);
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), dataPool.get("Value"));
			homePage.searchPanel.search(dataPool.get("SearchWord"), Caption.Search.SearchAllObjects.Value);
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the necessary conditions");

			//Verification: To verify if only the objects satisfying the condition are present in the search results
			//------------------------------------------------------------------------------------------------------
			Utils.fluentWait(driver);
			if(homePage.listView.itemCount() != 0) 
				throw new SkipException("The Search results was not empty, when it is supposed to be.");

			if(driver.findElement(By.id("noResultsText")).isDisplayed() && driver.findElement(By.id("noResultsText")).getText().equals(dataPool.get("ExpectedMessage")))
				Log.pass("Test Case Passed. The Expected Message was displayed as there is no search result to be displayed.");
			else
				Log.fail("Test Case Failed. The Expected Message was not displayed even when there is no search result to be displayed.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.31A : Condition Sorting:  'Booelan (OR)' + '<Objects>' +  'Condition' (With Search Results)        
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "AdvancedOptionsInQuickSearch"}, 
			description = "Condition Sorting:  'Booelan (OR)' + '<Objects>' +  'Condition' (With Search Results)")
	public void SprintTest24_2_31A(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			int expectedCount = 0;
			int count = 0;
			int flag = 0;

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Click the Advanced Search Link 
			//---------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Click the Advanced Search Link ");

			//Step-2: Set the necessary conditions
			//-------------------------------------
			homePage.searchPanel.setSearchOption(Caption.Search.SearchBoolean.Value);
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), dataPool.get("Value"));
			homePage.searchPanel.search(dataPool.get("SearchWord"), Caption.Search.SearchAllObjects.Value);
			String searchWord = dataPool.get("SearchWord").replace("\"", "").replace("or", "");
			String[] searchWords = searchWord.split(" ");
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the necessary conditions");

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

				if(!homePage.listView.getColumnValueByItemIndex(count, dataPool.get("Property")).equals(dataPool.get("Value"))) 
					break;

				flag = 0;
				for(int counter = 0; counter < searchWords.length; counter++) {
					if(!homePage.listView.getItemNameByItemIndex(count).toLowerCase().contains((searchWords[counter].toLowerCase())))
						flag++;
				}

				if(flag == searchWords.length)
					break;

			}

			homePage.listView.removeColumn(dataPool.get("Property"));

			if(count == expectedCount)
				Log.pass("Test Case Passed. Only the objects that satisfied the conditions were present in the Search result.");
			else
				Log.fail("Test Case Failed. The object that did not satisfy the condition was also present in the Search result.", driver);
		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.31B : Condition Sorting:  'Booelan (OR)' + '<Objects>' +  'Condition' (With Search Results)        
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "AdvancedOptionsInQuickSearch"}, 
			description = "Condition Sorting:  'Booelan (OR)' + '<Objects>' +  'Condition' (With Search Results)")
	public void SprintTest24_2_31B(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Click the Advanced Search Link 
			//---------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Click the Advanced Search Link ");

			//Step-2: Set the necessary conditions
			//-------------------------------------
			homePage.searchPanel.setSearchOption(Caption.Search.SearchBoolean.Value);
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), dataPool.get("Value"));
			homePage.searchPanel.search(dataPool.get("SearchWord"), Caption.Search.SearchAllObjects.Value);
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the necessary conditions");

			//Verification: To verify if only the objects satisfying the condition are present in the search results
			//------------------------------------------------------------------------------------------------------
			Utils.fluentWait(driver);
			if(homePage.listView.itemCount() != 0) 
				throw new SkipException("The Search results was not empty, when it is supposed to be.");

			if(driver.findElement(By.id("noResultsText")).isDisplayed() && driver.findElement(By.id("noResultsText")).getText().equals(dataPool.get("ExpectedMessage")))
				Log.pass("Test Case Passed. The Expected Message was displayed as there is no search result to be displayed.");
			else
				Log.fail("Test Case Failed. The Expected Message was not displayed even when there is no search result to be displayed.", driver);
		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.32A : Condition Sorting:  'Booelan (())' + '<Objects>' +  'Condition' (With Search Results)         
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "AdvancedOptionsInQuickSearch"}, 
			description = "Condition Sorting:  'Booelan (())' + '<Objects>' +  'Condition' (With Search Results)")
	public void SprintTest24_2_32A(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			int expectedCount = 0;
			int count = 0;
			int flag = 0;					

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Click the Advanced Search Link 
			//---------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Click the Advanced Search Link ");

			//Step-2: Set the necessary conditions
			//-------------------------------------
			homePage.searchPanel.setSearchOption(Caption.Search.SearchBoolean.Value);
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), dataPool.get("Value"));
			homePage.searchPanel.search(dataPool.get("SearchWord"), Caption.Search.SearchAllObjects.Value);
			String[] optionalWords = dataPool.get("Optional").split(",");
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the necessary conditions");

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

				if(!homePage.listView.getColumnValueByItemIndex(count, dataPool.get("Property")).equals(dataPool.get("Value"))) 
					break;

				if(!homePage.listView.getItemNameByItemIndex(count).toLowerCase().contains((dataPool.get("Required").toLowerCase())))
					break;

				flag = 0;
				for(int counter = 0; counter < optionalWords.length; counter++) {
					if(!homePage.listView.getItemNameByItemIndex(count).toLowerCase().contains((optionalWords[counter].toLowerCase())))
						flag++;
				}

				if(flag == optionalWords.length)
					break;

			}

			homePage.listView.removeColumn(dataPool.get("Property"));

			if(count == expectedCount)
				Log.pass("Test Case Passed. Only the objects that satisfied the conditions were present in the Search result.");
			else
				Log.fail("Test Case Failed. The object that did not satisfy the condition was also present in the Search result.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.32B : Condition Sorting:  'Booelan (())' + '<Objects>' +  'Condition' (Without Search Results)         
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "AdvancedOptionsInQuickSearch"}, 
			description = "Condition Sorting:  'Booelan (())' + '<Objects>' +  'Condition' (Without Search Results)")
	public void SprintTest24_2_32B(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Click the Advanced Search Link 
			//---------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Click the Advanced Search Link ");

			//Step-2: Set the necessary conditions
			//-------------------------------------
			homePage.searchPanel.setSearchOption(Caption.Search.SearchBoolean.Value);
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), dataPool.get("Value"));
			homePage.searchPanel.search(dataPool.get("SearchWord"), Caption.Search.SearchAllObjects.Value);
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the necessary conditions");

			//Verification: To verify if only the objects satisfying the condition are present in the search results
			//------------------------------------------------------------------------------------------------------
			Utils.fluentWait(driver);
			if(homePage.listView.itemCount() != 0) 
				throw new SkipException("The Search results was not empty, when it is supposed to be.");

			if(driver.findElement(By.id("noResultsText")).isDisplayed() && driver.findElement(By.id("noResultsText")).getText().equals(dataPool.get("ExpectedMessage")))
				Log.pass("Test Case Passed. The Expected Message was displayed as there is no search result to be displayed.");
			else
				Log.fail("Test Case Failed. The Expected Message was not displayed even when there is no search result to be displayed.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		} //End finally

	}


	/**
	 * 39.1.81: Search within this folder (With Search results)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "Password","AdvancedOptionsInQuickSearch"}, 
			description = "Search within this folder (With Search results)")
	public void SprintTest39_1_81(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			//1. navigate to any view or perfrom a search
			//--------------------------------------------
			homePage.listView.navigateThroughView(dataPool.get("View"));
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			List<String> objects = homePage.listView.getColumnValues("Name");

			if(objects.indexOf(dataPool.get("ObjectName")) < 0)
				throw new SkipException("Invalid Test data. The object was not found in the view specified.");

			Log.message("1. navigate to any view or perfrom a search");

			//2. Click the Advanced Search link
			//----------------------------------
			Utils.fluentWait(driver);
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("2. Click the Advanced Search link");

			//3. Check the Search within this folder check box
			//--------------------------------------------------
			homePage.searchPanel.setSearchWithInThisFolder(true);

			Log.message("3. Check the Search within this folder check box");

			//4. Click the Search button
			//---------------------------
			homePage.searchPanel.search(dataPool.get("ObjectName"), dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			Log.message("4. Click the Search button");

			//5. Click the Reset All link and confirm the action
			//---------------------------------------------------
			homePage.searchPanel.resetAll();
			Utils.fluentWait(driver);

			Log.message("5. Click the Reset All link and confirm the action");

			//Verification: To verify if the Search within this folder checkbox is enabled
			//-----------------------------------------------------------------------------
			for(int count = 0; count < homePage.listView.itemCount(); count++) {
				if(objects.indexOf(homePage.listView.getItemNameByItemIndex(count)) < 0)
					Log.fail("Test Case Failed. The Search result has an object that was not from the current folder.", driver);
			}

			Log.pass("Test Case Passed. All the Objects in the Search results were from within the current folder.");

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 39.1.82: Search within this folder (Without search results)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "Password","AdvancedOptionsInQuickSearch"}, 
			description = "Search within this folder (Without search results)")
	public void SprintTest39_1_82(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			//1. navigate to any view or perfrom a search
			//--------------------------------------------
			homePage.listView.navigateThroughView(dataPool.get("View"));
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			List<String> objects = homePage.listView.getColumnValues("Name");

			if(objects.indexOf(dataPool.get("ObjectName")) >= 0)
				throw new SkipException("Invalid Test data. The object was found in the view specified.");

			Log.message("1. navigate to any view or perfrom a search");

			//2. Click the Advanced Search link
			//----------------------------------
			Utils.fluentWait(driver);
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("2. Click the Advanced Search link");

			//3. Check the Search within this folder check box
			//--------------------------------------------------
			homePage.searchPanel.setSearchWithInThisFolder(true);

			Log.message("3. Check the Search within this folder check box");

			//4. Click the Search button
			//---------------------------
			homePage.searchPanel.search(dataPool.get("ObjectName"), dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			Log.message("4. Click the Search button");

			//6. Click the Turn off 'Search within this folder' option
			//---------------------------------------------------------
			Utils.fluentWait(driver);
			WebElement expectedMessage = driver.findElement(By.cssSelector("div[id='turnOffSearchwithInFolder']"));

			if(expectedMessage.isDisplayed())
				ActionEventUtils.click(driver, expectedMessage);
			else
				throw new Exception("The 'Turn off Search within this folder' option was not displayed.");


			//Verification: To verify if the Search within this folder checkbox is enabled
			//-----------------------------------------------------------------------------
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			if(homePage.listView.isItemExists(dataPool.get("ObjectName")))
				Log.pass("Test Case Passed. The Search within this folder option works as expected.");
			else
				Log.fail("Test Case Failed. The Search within this folder option did not work as expected.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}


	/**
	 * 39.1.86: Enabled Search within this folder check box
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "Password","AdvancedOptionsInQuickSearch"}, 
			description = "Enabled Search within this folder check box")
	public void SprintTest39_1_86(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			//1. navigate to any view or perfrom a search
			//--------------------------------------------
			SearchPanel.searchOrNavigatetoView(driver, dataPool.get("View"), "");
			Utils.fluentWait(driver);

			Log.message("1. navigate to any view or perfrom a search");

			//2. Click the Advanced Search link
			//----------------------------------
			Utils.fluentWait(driver);
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("2. Click the Advanced Search link");

			//Verification: To verify if the Search within this folder checkbox is enabled
			//-----------------------------------------------------------------------------
			if(driver.findElement(By.cssSelector("input[id='searchWithinThisFolder']")).isEnabled())
				Log.pass("Test Case Passed. The search within this folder checkbox is enabled.");
			else
				Log.fail("Test Case Failed. The search within this folder checkbox is disabled.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 39.1.87: Search within this folder disabled in Home View
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint25", "Password"}, 
			description = "Search within this folder disabled in Home View")
	public void SprintTest39_1_87(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			//1. Click the Advanced Search link
			//----------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("1. Clicked the Advanced Search link");

			//Verification: To verify if the Search within this folder in disabled
			//---------------------------------------------------------------------
			if(driver.findElement(By.cssSelector("div[id='searchWithinThisViewContainer']")).getAttribute("style").contains("display: none"))
				Log.pass("Test Case Passed. The search within this folder checkbox is disabled.");
			else
				Log.fail("Test Case Failed. The search within this folder checkbox is enabled.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}


	/**
	 * 47.2.31 : Show More Results displayed
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint47", "AdvancedOptionsInQuickSearch"}, 
			description = "Show More Results displayed.")
	public void SprintTest47_2_31(HashMap<String,String> dataValues, String driverType) throws Exception {

		//Pre-requisite : Launch Driver and Login to MFWA
		//-----------------------------------------------
		WebDriver driver = null; 

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Select Search all objects and click the Search button
			//---------------------------------------------------------
			homePage.searchPanel.search("", dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			Log.message("1. Select Search all objects and click the Search button");

			//Verification: To verify if the object types with objects more than 50 has show more results link
			//-------------------------------------------------------------------------------------------------
			int count = 0;
			int groups = homePage.listView.groupCount();
			for(count = 0; count < groups; count++) {
				if(homePage.listView.getGroupHeader(count).split("\\(")[1].split("\\)")[0].equals("50+"))
					if(!homePage.listView.isShowMoreResultsDisplayed(homePage.listView.getGroupHeader(count).split(" \\(")[0]))
						Log.fail("Test Case Failed. 'Show more Results' link was not displayed for a group with more than 50 objects.", driver);
			}

			if(count == groups)
				Log.pass("Test Case Passed. The 'Show more Results' link was displayed for object types with more than 50 objects.");
			else
				Log.fail("Test Case Failed. Verification was not successful.", driver);


		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest47_2_31

	/**
	 * 47.2.32 : Show More Results not displayed
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint47", "AdvancedOptionsInQuickSearch"}, 
			description = "Show More Results not displayed.")
	public void SprintTest47_2_32(HashMap<String,String> dataValues, String driverType) throws Exception {

		//Pre-requisite : Launch Driver and Login to MFWA
		//-----------------------------------------------
		WebDriver driver = null; 

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Select Search all objects and click the Search button
			//---------------------------------------------------------
			homePage.searchPanel.search("", dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			Log.message("1. Select Search all objects and click the Search button");

			//Verification: To verify if the object types with objects more than 50 has show more results link
			//-------------------------------------------------------------------------------------------------
			int count = 0;
			int groups = homePage.listView.groupCount();
			for(count = 0; count < groups; count++) {
				if(!homePage.listView.getGroupHeader(count).split("\\(")[1].split("\\)")[0].equals("50+"))
					if(homePage.listView.isShowMoreResultsDisplayed(homePage.listView.getGroupHeader(count).split(" \\(")[0]))
						Log.fail("Test Case Failed. 'Show more Results' link was displayed for a group with less than 50 objects.", driver);
			}

			if(count == groups)
				Log.pass("Test Case Passed. The 'Show more Results' link was not displayed for object types with less than 50 objects.");
			else
				Log.fail("Test Case Failed. Verification was not successful.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest47_2_32

	/**
	 * 47.2.33 : Click Show More Results (Search made with keyword)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint47", "AdvancedOptionsInQuickSearch"}, 
			description = "Click Show More Results (Search made with keyword).")
	public void SprintTest47_2_33(HashMap<String,String> dataValues, String driverType) throws Exception {

		//Pre-requisite : Launch Driver and Login to MFWA
		//-----------------------------------------------
		WebDriver driver = null; 

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Select Search all objects and click the Search button
			//---------------------------------------------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), dataPool.get("SearchType"));

			Log.message("1. Select Search all objects and click the Search button");

			//2. Click the Show more Results link
			//------------------------------------
			homePage.listView.clickShowMoreResults(dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			Log.message("2. Click the Show more Results link.");

			//Verification: To verify if all the matching objects are listed
			//---------------------------------------------------------------
			Utils.fluentWait(driver);
			if(homePage.listView.itemCount() != Integer.parseInt(dataPool.get("ExpectedCount")))
				throw new Exception("The Expected number of objects was not listed.");

			homePage.listView.insertColumn(Caption.Column.Coln_SingleFile.Value);
			Utils.fluentWait(driver);

			int count = 0;
			int objects = homePage.listView.itemCount();

			for(count = 0; count < objects; count++) {
				if(homePage.listView.getColumnValueByItemIndex(count, Caption.Column.Coln_SingleFile.Value).equalsIgnoreCase("Yes")) 
					if(!homePage.listView.getItemNameByItemIndex(count).contains(dataPool.get("SearchWord")))
						Log.fail("Test Case Failed. The Objects that dont match the Search string are also listed.", driver);
			}

			if(count == objects)
				Log.pass("Test Case Passed. The 'Show more Results' link Works as expected.");
			else
				Log.fail("Test Case Failed. Verification was not successful.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest47_2_33

	/**
	 * 47.2.34 : Click Show More Results (Search made without keyword)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Bug", "Sprint47", "AdvancedOptionsInQuickSearch"}, 
			description = "Click Show More Results (Search made without keyword).")
	public void SprintTest47_2_34(HashMap<String,String> dataValues, String driverType) throws Exception {

		//Pre-requisite : Launch Driver and Login to MFWA
		//-----------------------------------------------
		WebDriver driver = null; 
		HomePage homePage = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Select Search all objects and click the Search button
			//---------------------------------------------------------
			homePage.searchPanel.search("", dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			Log.message("1. Select Search all objects and click the Search button");

			//2. Click the Show more Results link
			//------------------------------------
			homePage.listView.clickShowMoreResults(dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			Log.message("2. Click the Show more Results link.");

			//Verification: To verify if all the matching objects are listed
			//---------------------------------------------------------------
			Utils.fluentWait(driver);
			if(homePage.listView.itemCount() != Integer.parseInt(dataPool.get("ExpectedCount")))
				throw new Exception("The Expected number of objects was not listed.");

			homePage.listView.insertColumn("Object Type");
			Utils.fluentWait(driver);

			if(homePage.listView.getItemOccurence(dataPool.get("ObjectType"), "Object Type") == Integer.parseInt(dataPool.get("ExpectedCount"))) 
				Log.pass("Test Case Passed. The 'Show more Results' link Works as expected.");
			else
				Log.fail("Test Case Failed. Verification was not successful.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if(!(driver.equals(null) && homePage.equals(null))){
				homePage.listView.removeColumn("Object Type");
			}
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest47_2_34

	/**
	 * SprintTest93825 : Verify if advanced search drop down values are displayed correctly.
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Bug", "Sprint47", "AdvancedOptionsInQuickSearch"}, 
			description = "Verify if advanced search drop down values are displayed as expected in search field.")
	public void SprintTest93825(HashMap<String,String> dataValues, String driverType) throws Exception {

		WebDriver driver = null; 
		HomePage homePage = null;

		try {



			driver = driverManager.startTesting(Utility.getMethodName());//Launches driver and returns the instance of the driver

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			homePage = LoginPage.launchDriverAndLogin(driver, true);//login to the web access

			//Step-1 : Select specified search option in advance search and click the Search button
			//-------------------------------------------------------------------------------------
			homePage.searchPanel.search("", dataPool.get("SearchType"));//Select the search type in advance search
			Utils.fluentWait(driver);

			Log.message("1. Selected the " + dataPool.get("SearchType") + " and click the Search button.");

			//Verify if selected search type is set in advance search
			//-------------------------------------------------------
			if(!homePage.searchPanel.getSearchType().equals(dataPool.get("SearchType")))//Verify if search type is set as expected
				throw new Exception("Search type : " + dataPool.get("SearchType") + " is not set in advance search field.");


			//Verification : Verify if object is displayed in the list view
			//-------------------------------------------------------------
			if(homePage.listView.itemCount() != 0) //Verify if list view object is listed as expected
				Log.pass("Test Case Passed.Advanced search drop down values are displayed as specified search type: " + dataPool.get("SearchType") , driver);
			else
				Log.fail("Test Case Failed.Advanced search drop down values are not displayed in search type.Expected : "  + dataPool.get("SearchType") + ". Actual : " + homePage.searchPanel.getSearchType() , driver);

		}
		catch (Exception e) {
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest93825

}//End advancedOptionsInQuickSearch


package MFClient.Tests.Search;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.Utils;
import genericLibrary.WebDriverUtils;

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
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class PredefinedSearch {

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
	 * 17.6.2 : 'Search type with default options
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint17", "Smoke","PredefinedSearch"}, 
			description = "'Search type with default options")
	public void SprintTest17_6_2(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			String option = dataPool.get("Options");
			String options[] = option.split(",");

			int count = 0;

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			//Step-1: Click the drop-down box in the Search box
			//--------------------------------------------------
			String[] availableOptions = homePage.searchPanel.getSearchTypeOptions();

			Log.message("Step-1: Click the drop-down box in the Search box");

			//Verificaiton: To Verify if the expected options are available in the drop down listView
			//------------------------------------------------------------------------------------
			for(count = 0; count < options.length; count++){
				for(int counter = 0; counter < availableOptions.length; counter++){
					if(options[count].equals(availableOptions[counter]))
						break;
				}

				if(count == availableOptions.length)
					break;
			}

			if( count == options.length)
				Log.pass("Test Case Passed. The expected options were available in the search type combo box.");
			else
				Log.fail("Test Case Failed. The expected option " + options[count] + " was not available in the search type combo box.", driver);

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
	 * 22.6.4 : Verify the Clear indication in Collection members view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint22","PredefinedSearch"}, 
			description = "Verify the Clear indication in Collection members view.")
	public void SprintTest22_6_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		//Pre-requisite : Launch Driver and Login to MFWA
		//-----------------------------------------------
		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);	

			String viewToNavigate = dataPool.get("NavigateToView");

			//Step-1 : Navigate to specified View
			//-----------------------------------
			if (viewToNavigate.toUpperCase().equals("") || viewToNavigate == null)
				viewToNavigate = Caption.Search.SearchAllObjects.Value;

			if (viewToNavigate.toUpperCase().contains("SEARCH")) {

				homePage.searchPanel.search(dataPool.get("DocumentCollection"), viewToNavigate); // Search for the documents
			}
			else { //Navigates to the specified view

				homePage.taskPanel.clickItem("Home");
				Utils.fluentWait(driver);

				homePage.listView.navigateThroughView(viewToNavigate);
			}

			Utils.fluentWait(driver);

			Log.message("Step-1 : Navigated to " + viewToNavigate + " View.");

			//Step-2 : Select the document and select Sub-Objects from Task Pane
			//------------------------------------------------------------------
			if (!homePage.listView.isItemExists(dataPool.get("DocumentCollection")))  //Checks for the existence of Document collection in the list
				throw new SkipException("Document Collection (" + dataPool.get("DocumentCollection") + ") does not exists in the list.");

			if (!homePage.listView.isColumnExists("Object Type")) // Inserts Object Type column
				homePage.listView.insertColumn("Object Type");

			if (!homePage.listView.getColumnValueByItemName(dataPool.get("DocumentCollection"), "Object Type").equalsIgnoreCase("Document Collection"))  //Checks for the existence of Document collection in the list
				throw new SkipException("Object (" + dataPool.get("DocumentCollection") + ") is not of Document Collection object type.");

			homePage.listView.clickItem(dataPool.get("DocumentCollection")); //Clicks the document collection
			Utils.fluentWait(driver);

			homePage.taskPanel.clickItem("Show Members"); //Clicks Show Members from task panel
			Utils.fluentWait(driver);

			Log.message("Step-2 : Document collection is selected and Sub Objects is clicked from task pane.");

			//Verification : Verify if members of document collection is opened
			//------------------------------------------------------------------
			//Verifies if caption of show members view has collection members with name of the document collection
			if (homePage.listView.getViewCaption().equalsIgnoreCase("Collection members - " + dataPool.get("DocumentCollection")))
				Log.pass("Test case Passed. The Collection Members view was clearly indicated.");
			else
				Log.fail("Test case Failed. The Collection members view was not indicated.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest22_6_4

	/**
	 * 24.2.1 : Pre-defined search with search word
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint24","PredefinedSearch"}, 
			description = "Pre-defined search with search word")
	public void SprintTest24_2_1(HashMap<String,String> dataValues, String driverType) throws Exception {

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
			homePage.searchPanel.search(dataPool.get("SearchWord"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			Log.message("Step-1: Perform Pre-defined search with search word .");

			//Verification: To verify if "There are no results to display" is displayed 
			//--------------------------------------------------------------------------
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
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.2 : Pre-defined search with search word with * at the end
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "PredefinedSearch"}, 
			description = "Pre-defined search with search word with * at the end")
	public void SprintTest24_2_2(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			if(!dataPool.get("SearchWord").endsWith("*")) 
				throw new SkipException("Invalid Test data. The Search Word does not end with an '*'");

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Perform Pre-defined search with search word 
			//----------------------------------------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			Log.message("Step-1: Perform Pre-defined search with search word .");

			//Verification: To verify if "There are no results to display" is displayed 
			//--------------------------------------------------------------------------
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
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 24.2.4 : Pre-defined search with search options in search dropdown options with empty objects of that type
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24","PredefinedSearch"}, 
			description = "Pre-defined search with search options in search dropdown options with empty objects of that type")
	public void SprintTest24_2_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			int count = 0;

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Perform Pre-defined search with search word and advanced conditions
			//----------------------------------------------------------------------------
			homePage.listView.navigateThroughView(dataPool.get("Path"));
			homePage.searchPanel.clearHistory();//clicks the clear search history in the search text field drop down
			MFilesDialog mfDialog = new MFilesDialog(driver);//Instantiates the Clear search history M-Files 
			mfDialog.clickOkButton();//Clicks ok Button in the clear search history m-files dialog
			homePage.searchPanel.clickAdvancedSearch(true);
			homePage.searchPanel.setSearchWithInThisFolder(true);
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), dataPool.get("Value"));
			homePage.searchPanel.search(dataPool.get("SearchWord"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);

			Log.message("Step-1: Perform Pre-defined search with search word and advanced conditions.");

			//Verification: To verify if "There are no results to display" is displayed 
			//--------------------------------------------------------------------------
			WebElement noResultsPanel = driver.findElement(By.id("dvemptyMsg"));
			List<WebElement> messages = noResultsPanel.findElements(By.cssSelector("div"));
			for (int i=0; i<messages.size();i++){
				System.out.println(messages.get(i).getText());
			}
			String[] expectedMessages = dataPool.get("ExpectedMessages").split("\n");

			for(count = 0; count < messages.size(); count++) {
				if(!messages.get(count).isDisplayed() || !messages.get(count).getText().trim().equals(expectedMessages[count]))
					break;
			}

			if( count == messages.size())
				Log.pass("Test Case Passed. The Expected Messages appeared in the expected order.");
			else
				Log.fail("Test Case Failed. The Expected Messages did not appear in the expected order.", driver);

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
	 * 24.2.33 : Search within the object types empty search word         
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "PredefinedSearch"}, 
			description = "Search within the object types empty search word")
	public void SprintTest24_2_33(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			int expectedCount = 0;
			int count = 0;

			//Step-1: Search for the given object type 
			//------------------------------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);

			homePage.searchPanel.search("", "Search only: " + dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			Log.message("1. Search for the given object type");

			//Verification: To verify if only the objects of the given object type are displayed
			//-----------------------------------------------------------------------------------
			if(homePage.listView.itemCount() == 0) 
				throw new SkipException("The Search results was empty, even when there are objects in the vault that satisfy the condition.");

			Utils.fluentWait(driver);
			homePage.listView.insertColumn("Object Type");
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			expectedCount = homePage.listView.itemCount();

			for(count = 0; count < expectedCount; count++){

				if(!homePage.listView.getColumnValueByItemIndex(count, "Object Type").toLowerCase().contains(dataPool.get("ExpectedType").toLowerCase())) 
					Log.fail("Test Case Failed. The objects that do not belong to this object type are also displayed.", driver);

			}

			if(count == expectedCount)
				Log.pass("Test Case Passed. Only the objects of the given object type were displayed.");
			else
				Log.fail("Test Case Failed. The objects that do not belong to this object type are also displayed.", driver);

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			if(!(driver.equals(null) && homePage.equals(null))){
				try
				{
					homePage.listView.removeColumn("Object Type");
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
	 * 24.2.34 : Search within the object types with search word         
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "PredefinedSearch", "Smoke"}, 
			description = "Search within the object types with search word")
	public void SprintTest24_2_34(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			int expectedCount = 0;
			int count = 0;
			int flag = 0;

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Search for the given object type 
			//------------------------------------------------
			homePage.searchPanel.search(dataPool.get("SearchWord"), "Search only: " + dataPool.get("ObjectType"));
			String[] searchWords =  dataPool.get("SearchWord").split(" ");
			Utils.fluentWait(driver);

			Log.message("1. Search for the given object type");

			//Verification: To verify if only the objects of the given object type are displayed
			//-----------------------------------------------------------------------------------
			if(homePage.listView.itemCount() == 0) 
				throw new SkipException("The Search results was empty, even when there are objects in the vault that satisfy the condition.");

			Utils.fluentWait(driver);
			homePage.listView.insertColumn("Object Type");
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			expectedCount = homePage.listView.itemCount();

			for(count = 0; count < expectedCount; count++){

				if(!homePage.listView.getColumnValueByItemIndex(count, "Object Type").toLowerCase().contains(dataPool.get("ExpectedType").toLowerCase())) 
					break;

				flag = 0;
				for(int counter = 0; counter < searchWords.length; counter++) {
					if(!homePage.listView.getItemNameByItemIndex(count).toLowerCase().contains((searchWords[counter].toLowerCase())))
						flag++;
				}

				if(flag == searchWords.length)
					break;

			}

			homePage.listView.removeColumn("Object Type");

			if(count == expectedCount)
				Log.pass("Test Case Passed. Only the objects of the given object type were displayed.");
			else
				Log.fail("Test Case Failed. The objects that do not belong to this object type or did not match the Search word are also displayed.", driver);

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
	 * 24.2.35A : Search with various options (SearchWord, SearchType & Advanced Conditions)         
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "PredefinedSearch"}, 
			description = "Search with various options (SearchWord, SearchType & Advanced Conditions)")
	public void SprintTest24_2_35A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

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
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), dataPool.get("Value"));
			homePage.searchPanel.search(dataPool.get("SearchWord"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);
			String[] optionalWords = dataPool.get("SearchWord").split(" ");

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

					if(!homePage.listView.getColumnValueByItemIndex(count, dataPool.get("Property")).equals(dataPool.get("Value"))) 
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
			else {
				Utils.fluentWait(driver);
				if(homePage.listView.itemCount() != 0) 
					throw new SkipException("The Search results was not empty, when it is supposed to be.");

				WebElement expectedMessage = driver.findElement(By.id("newSearchText"));

				if(expectedMessage.isDisplayed() && expectedMessage.getText().equals(dataPool.get("ExpectedMessage")))
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
	 * 24.2.35B : Search with various options (SearchWord, SearchType, Advanced Conditions & Search Within this view)         
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint24", "PredefinedSearch", "Smoke"}, 
			description = "Search with various options (SearchWord, SearchType, Advanced Conditions & Search Within this view)")
	public void SprintTest24_2_35B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;
		HomePage homePage = null;		

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			int expectedCount = 0;
			int count = 0;
			int flag = 0;

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Click the Advanced Search Link 
			//---------------------------------------
			homePage.listView.navigateThroughView(dataPool.get("Path"));
			Utils.fluentWait(driver);

			String expectedObjects = homePage.listView.getColumnValues("Name").toString(); 

			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Click the Advanced Search Link ");

			//Step-2: Set the necessary conditions
			//-------------------------------------
			homePage.searchPanel.setSearchWithInThisFolder(true);
			Utils.fluentWait(driver);
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), dataPool.get("Value"));
			homePage.searchPanel.search(dataPool.get("SearchWord"), dataPool.get("SearchType"));
			Utils.fluentWait(driver);
			String[] optionalWords = dataPool.get("SearchWord").split(" ");

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

					if(!expectedObjects.contains(homePage.listView.getColumnValueByItemIndex(count, "Name")))
						break;

					if(!homePage.listView.getColumnValueByItemIndex(count, dataPool.get("Property")).equals(dataPool.get("Value"))) 
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
			else {
				Utils.fluentWait(driver);
				if(homePage.listView.itemCount() != 0) 
					throw new SkipException("The Search results was not empty, when it is supposed to be.");

				WebElement expectedMessage = driver.findElement(By.id("turnOffSearchwithInFolder"));

				if(expectedMessage.isDisplayed() && expectedMessage.getText().equals(dataPool.get("ExpectedMessage")))
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
			if(!(driver.equals(null)&& homePage.equals(null))){		
				try
				{
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
	 * 47.2.2 : Verify to perform predefined search options
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint47", "PredefinedSearch","Bug"}, 
			description = "Verify to perform predefined search options.")
	public void SprintTest47_2_2(HashMap<String,String> dataValues, String driverType) throws Exception {

		//Pre-requisite : Launch Driver and Login to MFWA
		//-----------------------------------------------
		driver = null; 
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Click on the Advanced Search link
			//-------------------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);

			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("1. Click on the Advanced Search link.");

			//2. Select the Search type and click the Search button
			//------------------------------------------------------
			homePage.searchPanel.search("", dataPool.get("SearchType"));
			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");
			Utils.fluentWait(driver);

			Log.message("2. Select the Search type and click the Search button.");

			//3. Insert the ObjectType column
			//--------------------------------
			homePage.listView.insertColumn("Object Type");
			Utils.fluentWait(driver);

			Log.message("3. Insert the ObjectType column");

			//Verification : To verify if the value field is editable in Advanced Search
			//---------------------------------------------------------------------------
			String[] expectedTypes = dataPool.get("ExpectedTypes").split("\n");

			for(int count = 0; count < expectedTypes.length; count++) {
				if(!homePage.listView.isItemExists(expectedTypes[count], "Object Type"))
					Log.fail("Test Case Failed. An expected object type "+ expectedTypes[count]+ " was not listed.", driver);
			}

			Log.pass("Test case Passed. The objects of the expected object type were displayed.");

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if(!(driver.equals(null)&& homePage.equals(null))){	
				try
				{
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

	} //End SprintTest47_2_2



}


package MFClient.Tests.Search;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.Utils;
import genericLibrary.WebDriverUtils;

import java.text.SimpleDateFormat;


import java.util.Date;
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
public class DatePropertySearch {

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
	 * 22.5.3A : Verify to perform advanced search with 'is before' search conditions for date property(eg:Accessed by me)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint22", "DatePropertySearch"}, 
			description = "Verify to perform advanced search with ''is before'' search conditions for date property(eg:Accessed by me)")
	public void SprintTest22_5_3A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			Date expectedDate = null;
			expectedDate = dateFormat.parse(dataPool.get("Expected"));
			Date actualDate = null;
			int count = 0;

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Expand the advanced Search 
			//-----------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			homePage.searchPanel.setSearchType(Caption.Search.SearchAllObjects.Value);
			Utils.fluentWait(driver);

			Log.message("Step-1: Expanded the advanced Search.");

			//Step-2: Set the Advanced Search condition and perform the Search
			//----------------------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"),  dataPool.get("Value"));
			Utils.fluentWait(driver);
			homePage.searchPanel.clickSearch();
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the Advanced Search condition and perform the Search");

			//Verification: To verify if only the object meeting the conditions are listed in the Search result
			//--------------------------------------------------------------------------------------------------
			homePage.listView.insertColumn(dataPool.get("Property"));

			int objCount = homePage.listView.itemCount();
			String propValue = null;

			for(count = 0; count < objCount; count++) {
				propValue = homePage.listView.getColumnValueByItemIndex(count, dataPool.get("Property"));
				if(!propValue.equals(""))
					actualDate = dateFormat.parse(propValue);

				if(dataPool.get("Condition").equals("is before") && actualDate.compareTo(expectedDate) != -1) {
					Log.fail("Test Case Failed. The Objects that did not satisfy the condition were also listed.", driver);
					break;
				}

			}

			if(count == objCount)
				Log.pass("Test Case Passed. Only the objects that satisfy the conditions are found in the search results.");

			if(homePage.listView.isColumnExists(dataPool.get("Property")))
				homePage.listView.removeColumn(dataPool.get("Property"));

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
	 * 22.5.3B : Verify to perform advanced search with 'is after' search conditions for date property(eg:Accessed by me)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint22", "DatePropertySearch"}, 
			description = "Verify to perform advanced search with 'is after' search conditions for date property(eg:Accessed by me)")
	public void SprintTest22_5_3B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			Date expectedDate = null;
			expectedDate = dateFormat.parse(dataPool.get("Expected"));
			Date actualDate = null;
			int count = 0;

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Expand the advanced Search 
			//-----------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);
			homePage.searchPanel.setSearchType(Caption.Search.SearchAllObjects.Value);
			Utils.fluentWait(driver);

			Log.message("Step-1: Expand the advanced Search.");

			//Step-2: Set the Advanced Search condition and perform the Search
			//----------------------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"),  dataPool.get("Value"));
			Utils.fluentWait(driver);
			homePage.searchPanel.clickSearch();
			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the Advanced Search condition and perform the Search");

			//Verification: To verify if only the object meeting the conditions are listed in the Search result
			//--------------------------------------------------------------------------------------------------
			Utils.fluentWait(driver);
			homePage.listView.insertColumn(dataPool.get("Property"));

			int objCount = homePage.listView.itemCount();
			String propValue = null;

			for(count = 0; count < objCount; count++) {
				propValue = homePage.listView.getColumnValueByItemIndex(count, dataPool.get("Property"));
				if(!propValue.equals(""))
					actualDate = dateFormat.parse(propValue);

				if(dataPool.get("Condition").equals("is after") && actualDate.compareTo(expectedDate) != 1) {
					Log.fail("Test Case Failed. The Objects that did not satisfy the condition were also listed.", driver);
					break;
				}

			}

			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");
			if(homePage.listView.isColumnExists(dataPool.get("Property")))
				homePage.listView.removeColumn(dataPool.get("Property"));

			if(count == objCount)
				Log.pass("Test Case Passed. Only the objects that satisfy the conditions are found in the search results.");

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
	 * 22.5.3C : Verify to perform advanced search with 'today' search conditions for date property(eg:Accessed by me)
	 */
	@SuppressWarnings("deprecation")
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint22", "DatePropertySearch"}, 
	description = "Verify to perform advanced search with 'today' search conditions for date property(eg:Accessed by me)")
	public void SprintTest22_5_3C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			Date actualDate = null;
			Date today = new Date();
			int count = 0;

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			//Step-1: Expand the advanced Search 
			//-----------------------------------
			Utils.fluentWait(driver);

			homePage.searchPanel.search(dataPool.get("ObjectName"), Caption.Search.SearchAllObjects.Value);
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName")))
				throw new SkipException("Invaliad Test Data. The specified object is not found in the vault.");

			homePage.listView.clickItem(dataPool.get("ObjectName")); //access the object to 
			Utils.fluentWait(driver);

			homePage.taskPanel.clickItem("Check Out");
			Utils.fluentWait(driver);
			homePage.taskPanel.clickItem("Check In");
			Utils.fluentWait(driver);

			homePage.searchPanel.clickAdvancedSearch(true);
			homePage.searchPanel.resetAll();
			Utils.fluentWait(driver);

			Log.message("Step-1: Expand the advanced Search.");

			//Step-2: Set the Advanced Search condition and perform the Search
			//----------------------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"),  "");
			Utils.fluentWait(driver);
			homePage.searchPanel.clickSearch();
			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");

			Log.message("Step-2: Set the Advanced Search condition and perform the Search");

			//Verification: To verify if only the object meeting the conditions are listed in the Search result
			//--------------------------------------------------------------------------------------------------
			Utils.fluentWait(driver);
			homePage.listView.insertColumn(dataPool.get("Property"));
			Utils.fluentWait(driver);

			int objCount = homePage.listView.itemCount();
			String propValue = null;

			for(count = 0; count < objCount; count++) {
				propValue = homePage.listView.getColumnValueByItemIndex(count, dataPool.get("Property"));
				if(!propValue.equals(""))
					actualDate = dateFormat.parse(propValue);

				if(dataPool.get("Condition").equals("today") && (actualDate.getDate() != today.getDate() || actualDate.getMonth() != today.getMonth() || actualDate.getYear() != today.getYear())) {
					Log.fail("Test Case Failed. The Objects that did not satisfy the condition were also listed.", driver);
					break;
				}

			}

			if(count == objCount)
				Log.pass("Test Case Passed. Only the objects that satisfy the conditions are found in the search results.");

			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");
			if(homePage.listView.isColumnExists(dataPool.get("Property")))
				homePage.listView.removeColumn(dataPool.get("Property"));

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
	 * 22.5.3D : Verify to perform advanced search with 'is not empty' search conditions for date property(eg:Accessed by me)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint22", "DatePropertySearch"}, 
			description = "Verify to perform advanced search with 'is not empty' search conditions for date property(eg:Accessed by me)")
	public void SprintTest22_5_3D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			int count = 0;

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Expand the advanced Search 
			//-----------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Expand the advanced Search.");

			//Step-2: Set the Advanced Search condition and perform the Search
			//----------------------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"),  "");
			Utils.fluentWait(driver);
			homePage.searchPanel.clickSearch();
			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");

			Log.message("Step-2: Set the Advanced Search condition and perform the Search");

			//Verification: To verify if only the object meeting the conditions are listed in the Search result
			//--------------------------------------------------------------------------------------------------
			homePage.listView.insertColumn(dataPool.get("Property"));

			int objCount = homePage.listView.itemCount();
			String propValue = null;

			for(count = 0; count < objCount; count++) {
				propValue = homePage.listView.getColumnValueByItemIndex(count, dataPool.get("Property"));

				if(dataPool.get("Condition").equals("is not empty") && propValue.equals("")) {
					Log.fail("Test Case Failed. The Objects that did not satisfy the condition were also listed.", driver);
					break;
				}

			}

			if(count == objCount)
				Log.pass("Test Case Passed. Only the objects that satisfy the conditions are found in the search results.");

			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");
			if(homePage.listView.isColumnExists(dataPool.get("Property")))
				homePage.listView.removeColumn(dataPool.get("Property"));

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
	 * 22.5.3E : Verify to perform advanced search with "is empty" search conditions for date property(eg:Accessed by me)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint22", "DatePropertySearch"}, 
			description = "Verify to perform advanced search with 'is empty' search conditions for date property(eg:Accessed by me)")
	public void SprintTest22_5_3E(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			int count = 0;

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Expand the advanced Search 
			//-----------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Expand the advanced Search.");

			//Step-2: Set the Advanced Search condition and perform the Search
			//----------------------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"),"");
			Utils.fluentWait(driver);
			homePage.searchPanel.clickSearch();
			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");

			Log.message("Step-2: Set the Advanced Search condition and perform the Search");

			//Verification: To verify if only the object meeting the conditions are listed in the Search result
			//--------------------------------------------------------------------------------------------------

			homePage.listView.insertColumn(dataPool.get("Property"));

			int objCount = homePage.listView.itemCount();
			String propValue = null;

			for(count = 0; count < objCount; count++) {
				propValue = homePage.listView.getColumnValueByItemIndex(count, dataPool.get("Property"));

				if(dataPool.get("Condition").equals("is empty") && !propValue.equals("")) {
					Log.fail("Test Case Failed. The Objects that did not satisfy the condition were also listed.", driver);
					break;
				}

			}

			if(count == objCount)
				Log.pass("Test Case Passed. Only the objects that satisfy the conditions are found in the search results.");

			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");
			if(homePage.listView.isColumnExists(dataPool.get("Property")))
				homePage.listView.removeColumn(dataPool.get("Property"));

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
	 * 22.5.3F : Verify to perform advanced search with 'is' search conditions for date property(eg:Accessed by me)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint22", "DatePropertySearch"}, 
			description = "Verify to perform advanced search with 'is' search conditions for date property(eg:Accessed by me)")
	public void SprintTest22_5_3F(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);		

			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			Date expectedDate = null;
			if(!dataPool.get("Value").equals("")) 
				expectedDate = dateFormat.parse(dataPool.get("Value").toString());
			Date actualDate = null;
			int count = 0;

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Expand the advanced Search 
			//-----------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Expand the advanced Search.");

			//Step-2: Set the Advanced Search condition and perform the Search
			//----------------------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"),  dataPool.get("Expected"));
			Utils.fluentWait(driver);
			homePage.searchPanel.clickSearch();
			Utils.fluentWait(driver);
			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");

			Log.message("Step-2: Set the Advanced Search condition and perform the Search");

			//Verification: To verify if only the object meeting the conditions are listed in the Search result
			//--------------------------------------------------------------------------------------------------

			homePage.listView.insertColumn(dataPool.get("Property"));

			int objCount = homePage.listView.itemCount();
			String propValue = null;

			for(count = 0; count < objCount; count++) {
				propValue = homePage.listView.getColumnValueByItemIndex(count, dataPool.get("Property"));
				if(!propValue.equals(""))
					actualDate = dateFormat.parse(propValue);

				if(dataPool.get("Condition").equals("is") && actualDate.compareTo(expectedDate) != 0) {
					Log.fail("Test Case Failed. The Objects that did not satisfy the condition were also listed.", driver);
					break;
				}

			}

			if(count == objCount)
				Log.pass("Test Case Passed. Only the objects that satisfy the conditions are found in the search results.");

			homePage.menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");
			if(homePage.listView.isColumnExists(dataPool.get("Property")))
				homePage.listView.removeColumn(dataPool.get("Property"));

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
	 * 22.5.3G : Verify to perform advanced search with 'within the last week' search conditions for date property(eg:Accessed by me)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint22", "DatePropertySearch"}, 
			description = "Verify to perform advanced search with 'within the last week' search conditions for date property(eg:Accessed by me)")
	public void SprintTest22_5_3G(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			Date actualDate = null;
			Date today = new Date();
			int count = 0;

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Expand the advanced Search 
			//-----------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);

			homePage.searchPanel.search(dataPool.get("ObjectName"), dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) 
				throw new SkipException("Invaliad Test Data. The specified object is not found in the vault.");

			homePage.listView.rightClickItem(dataPool.get("ObjectName"));
			Utils.fluentWait(driver);

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			MetadataCard metadataCard = new MetadataCard(driver);
			metadataCard.setComments("Test");
			metadataCard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("Step-1: Expand the advanced Search.");

			//Step-2: Set the Advanced Search condition and perform the Search
			//----------------------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"),  "");
			Utils.fluentWait(driver);
			homePage.searchPanel.clickSearch();
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the Advanced Search condition and perform the Search");

			//Verification: To verify if only the object meeting the conditions are listed in the Search result
			//--------------------------------------------------------------------------------------------------
			Utils.fluentWait(driver);

			homePage.listView.insertColumn(dataPool.get("ColumnName"));

			int objCount = homePage.listView.itemCount();
			String propValue = null;

			for(count = 0; count < objCount; count++) {
				propValue = homePage.listView.getColumnValueByItemIndex(count, dataPool.get("ColumnName"));
				if(!propValue.equals(""))
					actualDate = dateFormat.parse(propValue);

				long diff = today.getTime() - actualDate.getTime();

				long diffDays = diff / (24 * 60 * 60 * 1000);

				if(dataPool.get("Condition").equals("within the last week") && diffDays > 7) 
					Log.fail("Test Case Failed. The Objects that did not satisfy the condition were also listed.", driver);
			}

			if(count == objCount)
				Log.pass("Test Case Passed. Only the objects that satisfy the conditions are found in the search results.");

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
	 * 22.5.3H : Verify to perform advanced search with 'within the last month' search conditions for date property(eg:Accessed by me)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint22", "DatePropertySearch"}, 
			description = "Verify to perform advanced search with 'within the last month' search conditions for date property(eg:Accessed by me)")
	public void SprintTest22_5_3H(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			Date actualDate = null;
			Date today = new Date();
			int count = 0;

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Expand the advanced Search 
			//-----------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);

			homePage.searchPanel.search(dataPool.get("ObjectName"), dataPool.get("ObjectType"));

			Utils.fluentWait(driver);
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) 
				throw new SkipException("Invaliad Test Data. The specified object is not found in the vault.");

			homePage.listView.rightClickItem(dataPool.get("ObjectName"));
			Utils.fluentWait(driver);

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			MetadataCard metadataCard = new MetadataCard(driver);
			metadataCard.setComments("Test");
			metadataCard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("Step-1: Expand the advanced Search.");

			//Step-2: Set the Advanced Search condition and perform the Search
			//----------------------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"),  "");
			Utils.fluentWait(driver);
			homePage.searchPanel.clickSearch();
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the Advanced Search condition and perform the Search");

			//Verification: To verify if only the object meeting the conditions are listed in the Search result
			//--------------------------------------------------------------------------------------------------
			Utils.fluentWait(driver);

			homePage.listView.insertColumn(dataPool.get("ColumnName"));

			int objCount = homePage.listView.itemCount();
			String propValue = null;

			for(count = 0; count < objCount; count++) {
				propValue = homePage.listView.getColumnValueByItemIndex(count, dataPool.get("ColumnName"));
				if(!propValue.equals(""))
					actualDate = dateFormat.parse(propValue);

				long diff = today.getTime() - actualDate.getTime();

				long diffDays = diff / (24 * 60 * 60 * 1000);

				if(diffDays > 31) 
					Log.fail("Test Case Failed. The Objects that did not satisfy the condition were also listed.", driver);


			}

			if(count == objCount)
				Log.pass("Test Case Passed. Only the objects that satisfy the conditions are found in the search results.");

			if(homePage.listView.isColumnExists(dataPool.get("ColumnName")))
				homePage.listView.removeColumn(dataPool.get("ColumnName"));

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
	 * 22.5.3I : Verify to perform advanced search with 'within the last year' search conditions for date property(eg:Accessed by me)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint22", "DatePropertySearch"}, 
			description = "Verify to perform advanced search with 'within the last year' search conditions for date property(eg:Accessed by me)")
	public void SprintTest22_5_3I(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			Date actualDate = null;
			Date today = new Date();
			int count = 0;

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Expand the advanced Search 
			//-----------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);

			homePage.searchPanel.search(dataPool.get("ObjectName"), dataPool.get("ObjectType"));

			Utils.fluentWait(driver);
			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) 
				throw new SkipException("Invaliad Test Data. The specified object is not found in the vault.");

			homePage.listView.rightClickItem(dataPool.get("ObjectName"));
			Utils.fluentWait(driver);

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			MetadataCard metadataCard = new MetadataCard(driver);
			metadataCard.setComments("Test");
			metadataCard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("Step-1: Expand the advanced Search.");

			//Step-2: Set the Advanced Search condition and perform the Search
			//----------------------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"),  "");
			Utils.fluentWait(driver);
			homePage.searchPanel.clickSearch();
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the Advanced Search condition and perform the Search");

			//Verification: To verify if only the object meeting the conditions are listed in the Search result
			//--------------------------------------------------------------------------------------------------
			Utils.fluentWait(driver);

			homePage.listView.insertColumn(dataPool.get("ColumnName"));

			int objCount = homePage.listView.itemCount();
			String propValue = null;

			for(count = 0; count < objCount; count++) {
				propValue = homePage.listView.getColumnValueByItemIndex(count, dataPool.get("ColumnName"));
				if(!propValue.equals(""))
					actualDate = dateFormat.parse(propValue);

				long diff = today.getTime() - actualDate.getTime();

				long diffDays = diff / (24 * 60 * 60 * 1000);

				if(diffDays > 366) 
					Log.fail("Test Case Failed. The Objects that did not satisfy the condition were also listed.", driver);

			}

			if(count == objCount)
				Log.pass("Test Case Passed. Only the objects that satisfy the conditions are found in the search results.");

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
	 * 22.5.4A : Verify to perform advanced search operation after entering the value for the date property with calendar. - 'is before'
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint22", "DatePropertySearch"}, 
			description = "Verify to perform advanced search operation after entering the value for the date property with calendar. - 'is before'")
	public void SprintTest22_5_4A(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			Date expectedDate = null;
			expectedDate = dateFormat.parse(dataPool.get("Expected"));
			Date actualDate = null;
			int count = 0;

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Expand the advanced Search 
			//-----------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Expand the advanced Search.");

			//Step-2: Set the Advanced Search condition and perform the Search
			//----------------------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"),  dataPool.get("Value"));
			Utils.fluentWait(driver);
			homePage.searchPanel.setSearchWord(dataPool.get("SearchWord"));
			homePage.searchPanel.clickSearch();
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the Advanced Search condition and perform the Search");

			//Verification: To verify if only the object meeting the conditions are listed in the Search result
			//--------------------------------------------------------------------------------------------------
			homePage.listView.insertColumn(dataPool.get("Property"));

			int objCount = homePage.listView.itemCount();
			String propValue = null;

			for(count = 0; count < objCount; count++) {
				propValue = homePage.listView.getColumnValueByItemIndex(count, dataPool.get("Property"));
				if(!propValue.equals(""))
					actualDate = dateFormat.parse(propValue);

				if((dataPool.get("Condition").equals("is before") && actualDate.compareTo(expectedDate) != -1) || !homePage.listView.getItemNameByItemIndex(count).toLowerCase().contains(dataPool.get("SearchWord").toLowerCase())) {
					Log.fail("Test Case Failed. The Objects that did not satisfy the condition were also listed.", driver);
					break;
				}

			}

			if(count == objCount)
				Log.pass("Test Case Passed. Only the objects that satisfy the conditions are found in the search results.");

			if(homePage.listView.isColumnExists(dataPool.get("Property")))
				homePage.listView.removeColumn(dataPool.get("Property"));

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
	 * 22.5.4B : Verify to perform advanced search with 'is after' search conditions for date property(eg:Accessed by me)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint22", "DatePropertySearch"}, 
			description = "Verify to perform advanced search with 'is after' search conditions for date property(eg:Accessed by me)")
	public void SprintTest22_5_4B(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			Date expectedDate = null;
			expectedDate = dateFormat.parse(dataPool.get("Expected"));
			Date actualDate = null;
			int count = 0;

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Expand the advanced Search 
			//-----------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Expand the advanced Search.");

			//Step-2: Set the Advanced Search condition and perform the Search
			//----------------------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"),  dataPool.get("Value"));
			Utils.fluentWait(driver);
			homePage.searchPanel.setSearchWord(dataPool.get("SearchWord"));
			homePage.searchPanel.clickSearch();
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the Advanced Search condition and perform the Search");

			//Verification: To verify if only the object meeting the conditions are listed in the Search result
			//--------------------------------------------------------------------------------------------------
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			homePage.listView.insertColumn(dataPool.get("Property"));

			int objCount = homePage.listView.itemCount();
			String propValue = null;

			for(count = 0; count < objCount; count++) {
				propValue = homePage.listView.getColumnValueByItemIndex(count, dataPool.get("Property"));
				if(!propValue.equals(""))
					actualDate = dateFormat.parse(propValue);

				if((dataPool.get("Condition").equals("is after") && actualDate.compareTo(expectedDate) != 1)  || (!homePage.listView.getItemNameByItemIndex(count).toLowerCase().contains(dataPool.get("SearchWord").toLowerCase()))) {
					Log.fail("Test Case Failed. The Objects that did not satisfy the condition were also listed.", driver);
					break;
				}

			}

			if(count == objCount)
				Log.pass("Test Case Passed. Only the objects that satisfy the conditions are found in the search results.");

			if(homePage.listView.isColumnExists(dataPool.get("Property")))
				homePage.listView.removeColumn(dataPool.get("Property"));

		} //End try

		catch(Exception e)
		{
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 22.5.4C : Verify to perform advanced search with 'today' search conditions for date property(eg:Accessed by me)
	 */
	@SuppressWarnings("deprecation")
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint22", "DatePropertySearch"}, 
	description = "Verify to perform advanced search with 'today' search conditions for date property(eg:Accessed by me)")
	public void SprintTest22_5_4C(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			Date actualDate = null;
			Date today = new Date();
			int count = 0;

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			//Step-1: Expand the advanced Search 
			//-----------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			homePage.searchPanel.search(dataPool.get("ObjectName"), dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) 
				throw new SkipException("Invaliad Test Data. The specified object is not found in the vault.");

			homePage.listView.rightClickItem(dataPool.get("ObjectName"));
			Utils.fluentWait(driver);
			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			MetadataCard metadataCard = new MetadataCard(driver);
			metadataCard.setComments("Test");
			metadataCard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("Step-1: Expand the advanced Search.");

			//Step-2: Set the Advanced Search condition and perform the Search
			//----------------------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"),  "");
			Utils.fluentWait(driver);
			homePage.searchPanel.setSearchWord(dataPool.get("SearchWord"));
			homePage.searchPanel.clickSearch();
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the Advanced Search condition and perform the Search");

			//Verification: To verify if only the object meeting the conditions are listed in the Search result
			//--------------------------------------------------------------------------------------------------
			Utils.fluentWait(driver);
			homePage.listView.insertColumn(dataPool.get("Property"));
			Utils.fluentWait(driver);

			int objCount = homePage.listView.itemCount();
			String propValue = null;

			for(count = 0; count < objCount; count++) {
				propValue = homePage.listView.getColumnValueByItemIndex(count, dataPool.get("Property"));
				if(!propValue.equals(""))
					actualDate = dateFormat.parse(propValue);

				if((dataPool.get("Condition").equals("today") && (actualDate.getDate() != today.getDate() || actualDate.getMonth() != today.getMonth() || actualDate.getYear() != today.getYear())) || (!homePage.listView.getItemNameByItemIndex(count).toLowerCase().contains(dataPool.get("SearchWord").toLowerCase()))) {
					Log.fail("Test Case Failed. The Objects that did not satisfy the condition were also listed.", driver);
					break;
				}

			}

			if(count == objCount)
				Log.pass("Test Case Passed. Only the objects that satisfy the conditions are found in the search results.");

			if(homePage.listView.isColumnExists(dataPool.get("Property")))
				homePage.listView.removeColumn(dataPool.get("Property"));

		} //End try

		catch(Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally
	}

	/**
	 * 22.5.4D : Verify to perform advanced search with 'is not empty' search conditions for date property(eg:Accessed by me)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint22", "DatePropertySearch"}, 
			description = "Verify to perform advanced search with 'is not empty' search conditions for date property(eg:Accessed by me)")
	public void SprintTest22_5_4D(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);
			int count = 0;

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Expand the advanced Search 
			//-----------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Expand the advanced Search.");

			//Step-2: Set the Advanced Search condition and perform the Search
			//----------------------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"),  "");
			Utils.fluentWait(driver);
			homePage.searchPanel.setSearchWord(dataPool.get("SearchWord"));
			homePage.searchPanel.clickSearch();
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the Advanced Search condition and perform the Search");

			//Verification: To verify if only the object meeting the conditions are listed in the Search result
			//--------------------------------------------------------------------------------------------------
			homePage.listView.insertColumn(dataPool.get("Property"));

			int objCount = homePage.listView.itemCount();
			String propValue = null;

			for(count = 0; count < objCount; count++) {
				propValue = homePage.listView.getColumnValueByItemIndex(count, dataPool.get("Property"));

				if((dataPool.get("Condition").equals("is not empty") && propValue.equals(""))  || (!homePage.listView.getItemNameByItemIndex(count).toLowerCase().contains(dataPool.get("SearchWord").toLowerCase()))) {
					Log.fail("Test Case Failed. The Objects that did not satisfy the condition were also listed.", driver);
					break;
				}

			}

			if(count == objCount)
				Log.pass("Test Case Passed. Only the objects that satisfy the conditions are found in the search results.");

			if(homePage.listView.isColumnExists(dataPool.get("Property")))
				homePage.listView.removeColumn(dataPool.get("Property"));

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
	 * 22.5.4E : Verify to perform advanced search with "is empty" search conditions for date property(eg:Accessed by me)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint22", "DatePropertySearch", "Smoke"}, 
			description = "Verify to perform advanced search with 'is empty' search conditions for date property(eg:Accessed by me)")
	public void SprintTest22_5_4E(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			int count = 0;

			//Step-1: Expand the advanced Search 
			//-----------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Expand the advanced Search.");

			//Step-2: Set the Advanced Search condition and perform the Search
			//----------------------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"),"");
			Utils.fluentWait(driver);
			homePage.searchPanel.setSearchWord(dataPool.get("SearchWord"));
			homePage.searchPanel.clickSearch();
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the Advanced Search condition and perform the Search");

			//Verification: To verify if only the object meeting the conditions are listed in the Search result
			//--------------------------------------------------------------------------------------------------
			homePage.listView.insertColumn(dataPool.get("Property"));

			int objCount = homePage.listView.itemCount();
			String propValue = null;

			for(count = 0; count < objCount; count++) {
				propValue = homePage.listView.getColumnValueByItemIndex(count, dataPool.get("Property"));

				if((dataPool.get("Condition").equals("is empty") && !propValue.equals("")) || (!homePage.listView.getItemNameByItemIndex(count).toLowerCase().contains(dataPool.get("SearchWord").toLowerCase()))) {
					Log.fail("Test Case Failed. The Objects that did not satisfy the condition were also listed.", driver);
					break;
				}

			}

			if(count == objCount)
				Log.pass("Test Case Passed. Only the objects that satisfy the conditions are found in the search results.");

			if(homePage.listView.isColumnExists(dataPool.get("Property")))
				homePage.listView.removeColumn(dataPool.get("Property"));

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
	 * 22.5.4F : Verify to perform advanced search with 'is' search conditions for date property(eg:Accessed by me)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint22", "DatePropertySearch"}, 
			description = "Verify to perform advanced search with 'is' search conditions for date property(eg:Accessed by me)")
	public void SprintTest22_5_4F(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			Date expectedDate = null;
			if(!dataPool.get("Value").equals("")) 
				expectedDate = dateFormat.parse(dataPool.get("Value").toString());
			Date actualDate = null;
			int count = 0;

			//Step-1: Expand the advanced Search 
			//-----------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Expand the advanced Search.");

			//Step-2: Set the Advanced Search condition and perform the Search
			//----------------------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"),  dataPool.get("Expected"));
			Utils.fluentWait(driver);
			homePage.searchPanel.setSearchWord(dataPool.get("SearchWord"));
			homePage.searchPanel.clickSearch();
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the Advanced Search condition and perform the Search");

			//Verification: To verify if only the object meeting the conditions are listed in the Search result
			//--------------------------------------------------------------------------------------------------

			homePage.listView.insertColumn(dataPool.get("Property"));

			int objCount = homePage.listView.itemCount();
			String propValue = null;

			for(count = 0; count < objCount; count++) {
				propValue = homePage.listView.getColumnValueByItemIndex(count, dataPool.get("Property"));
				if(!propValue.equals(""))
					actualDate = dateFormat.parse(propValue);

				if((dataPool.get("Condition").equals("is") && actualDate.compareTo(expectedDate) != 0) || (!homePage.listView.getItemNameByItemIndex(count).toLowerCase().contains(dataPool.get("SearchWord").toLowerCase()))) { 
					Log.fail("Test Case Failed. The Objects that did not satisfy the condition were also listed.", driver);
					break;
				}

			}

			if(count == objCount)
				Log.pass("Test Case Passed. Only the objects that satisfy the conditions are found in the search results.");

			if(homePage.listView.isColumnExists(dataPool.get("Property")))
				homePage.listView.removeColumn(dataPool.get("Property"));

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
	 * 22.5.4G : Verify to perform advanced search with 'within the last week' search conditions for date property(eg:Accessed by me)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint22", "DatePropertySearch"}, 
			description = "Verify to perform advanced search with 'within the last week' search conditions for date property(eg:Accessed by me)")
	public void SprintTest22_5_4G(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			Date actualDate = null;
			Date today = new Date();
			int count = 0;

			//Step-1: Expand the advanced Search 
			//-----------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);

			homePage.searchPanel.search(dataPool.get("ObjectName"), dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) 
				throw new SkipException("Invaliad Test Data. The specified object is not found in the vault.");

			homePage.listView.rightClickItem(dataPool.get("ObjectName"));
			Utils.fluentWait(driver);

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			MetadataCard metadataCard = new MetadataCard(driver);
			metadataCard.setComments("Test");
			metadataCard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("Step-1: Expand the advanced Search.");

			//Step-2: Set the Advanced Search condition and perform the Search
			//----------------------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"),  "");
			Utils.fluentWait(driver);
			homePage.searchPanel.setSearchWord(dataPool.get("SearchWord"));
			homePage.searchPanel.clickSearch();
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the Advanced Search condition and perform the Search");

			//Verification: To verify if only the object meeting the conditions are listed in the Search result
			//--------------------------------------------------------------------------------------------------
			Utils.fluentWait(driver);

			homePage.listView.insertColumn(dataPool.get("ColumnName"));

			int objCount = homePage.listView.itemCount();
			String propValue = null;

			for(count = 0; count < objCount; count++) {
				propValue = homePage.listView.getColumnValueByItemIndex(count, dataPool.get("ColumnName"));
				if(!propValue.equals(""))
					actualDate = dateFormat.parse(propValue);

				long diff = today.getTime() - actualDate.getTime();

				long diffDays = diff / (24 * 60 * 60 * 1000);

				if((dataPool.get("Condition").equals("within the last week") && diffDays > 7) ||  (!homePage.listView.getItemNameByItemIndex(count).toLowerCase().contains(dataPool.get("SearchWord").toLowerCase()))) {
					Log.fail("Test Case Failed. The Objects that did not satisfy the condition were also listed.", driver);
					break;
				}

			}

			if(count == objCount)
				Log.pass("Test Case Passed. Only the objects that satisfy the conditions are found in the search results.");

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
	 * 22.5.4H : Verify to perform advanced search with 'within the last month' search conditions for date property(eg:Accessed by me)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint22", "DatePropertySearch"}, 
			description = "Verify to perform advanced search with 'within the last month' search conditions for date property(eg:Accessed by me)")
	public void SprintTest22_5_4H(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			Date actualDate = null;
			Date today = new Date();
			int count = 0;

			//Step-1: Expand the advanced Search 
			//-----------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);

			homePage.searchPanel.search(dataPool.get("ObjectName"), dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) 
				throw new SkipException("Invaliad Test Data. The specified object is not found in the vault.");

			homePage.listView.rightClickItem(dataPool.get("ObjectName"));
			Utils.fluentWait(driver);

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			MetadataCard metadataCard = new MetadataCard(driver);
			metadataCard.setComments("Test");
			metadataCard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("Step-1: Expand the advanced Search.");

			//Step-2: Set the Advanced Search condition and perform the Search
			//----------------------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"),  "");
			Utils.fluentWait(driver);
			homePage.searchPanel.setSearchWord(dataPool.get("SearchWord"));
			homePage.searchPanel.clickSearch();
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the Advanced Search condition and perform the Search");

			//Verification: To verify if only the object meeting the conditions are listed in the Search result
			//--------------------------------------------------------------------------------------------------
			Utils.fluentWait(driver);

			homePage.listView.insertColumn(dataPool.get("ColumnName"));

			int objCount = homePage.listView.itemCount();
			String propValue = null;

			for(count = 0; count < objCount; count++) {
				propValue = homePage.listView.getColumnValueByItemIndex(count, dataPool.get("ColumnName"));
				if(!propValue.equals(""))
					actualDate = dateFormat.parse(propValue);

				long diff = today.getTime() - actualDate.getTime();

				long diffDays = diff / (24 * 60 * 60 * 1000);

				if(dataPool.get("Condition").equals("within the last week") && diffDays > 31 ||  (!homePage.listView.getItemNameByItemIndex(count).toLowerCase().contains(dataPool.get("SearchWord").toLowerCase()))) {
					Log.fail("Test Case Failed. The Objects that did not satisfy the condition were also listed.", driver);
					break;
				}

			}

			if(count == objCount)
				Log.pass("Test Case Passed. Only the objects that satisfy the conditions are found in the search results.");

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
	 * 22.5.4I : Verify to perform advanced search with 'within the last year' search conditions for date property(eg:Accessed by me)
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint22", "DatePropertySearch"}, 
			description = "Verify to perform advanced search with 'within the last year' search conditions for date property(eg:Accessed by me)")
	public void SprintTest22_5_4I(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			Date actualDate = null;
			Date today = new Date();
			int count = 0;

			//Step-1: Expand the advanced Search 
			//-----------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);

			homePage.searchPanel.search(dataPool.get("ObjectName"), dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.isItemExists(dataPool.get("ObjectName"))) 
				throw new SkipException("Invaliad Test Data. The specified object is not found in the vault.");

			homePage.listView.rightClickItem(dataPool.get("ObjectName"));
			Utils.fluentWait(driver);

			homePage.listView.clickContextMenuItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);

			MetadataCard metadataCard = new MetadataCard(driver);
			metadataCard.setComments("Test");
			metadataCard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("Step-1: Expand the advanced Search.");

			//Step-2: Set the Advanced Search condition and perform the Search
			//----------------------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"),  "");
			Utils.fluentWait(driver);
			homePage.searchPanel.setSearchWord(dataPool.get("SearchWord"));
			homePage.searchPanel.clickSearch();
			Utils.fluentWait(driver);

			Log.message("Step-2: Set the Advanced Search condition and perform the Search");

			//Verification: To verify if only the object meeting the conditions are listed in the Search result
			//--------------------------------------------------------------------------------------------------
			Utils.fluentWait(driver);

			homePage.listView.insertColumn(dataPool.get("ColumnName"));

			int objCount = homePage.listView.itemCount();
			String propValue = null;

			for(count = 0; count < objCount; count++) {
				propValue = homePage.listView.getColumnValueByItemIndex(count, dataPool.get("ColumnName"));
				if(!propValue.equals(""))
					actualDate = dateFormat.parse(propValue);

				long diff = today.getTime() - actualDate.getTime();

				long diffDays = diff / (24 * 60 * 60 * 1000);

				if(dataPool.get("Condition").equals("within the last week") && diffDays > 366 ||  (!homePage.listView.getItemNameByItemIndex(count).toLowerCase().contains(dataPool.get("SearchWord").toLowerCase()))) {
					Log.fail("Test Case Failed. The Objects that did not satisfy the condition were also listed.", driver);
					break;
				}

			}

			if(count == objCount)
				Log.pass("Test Case Passed. Only the objects that satisfy the conditions are found in the search results.");

		}

		catch(Exception e)
		{
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		} //End finally

	}




}


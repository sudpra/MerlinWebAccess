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
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class PropertiesInAdvancedSearch {

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
	 * 22.4.4 : Verify to perform advanced search using integer property with any alphabetical values / decimal or non-integer values
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint22", "PropertiesInAdvancedSearch","WildcardSearch"}, 
			description = "Copy to Clipboard button should not be displayed by default in the wild card search warning dialog.")
	public void SprintTest22_4_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		//Pre-requisite : Launch Driver and Login to MFWA
		//-----------------------------------------------
		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			String viewToNavigate = dataPool.get("NavigateToView");

			//Step-1 : Navigate to specified View
			//-----------------------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);

			if (viewToNavigate.toUpperCase().equals("") || viewToNavigate == null)
				viewToNavigate = Caption.Search.SearchAllObjects.Value;

			if (viewToNavigate.toUpperCase().contains("SEARCH")) {

				homePage.searchPanel.search("", viewToNavigate); // Search for the documents
			}
			else { //Navigates to the specified view

				homePage.taskPanel.clickItem("Home");
				Utils.fluentWait(driver);

				homePage.listView.navigateThroughView(viewToNavigate);
			}

			Log.message("Step-1 : Navigated to " + viewToNavigate + " View.");

			//Step-2 : Open Advanced Search and select integer property
			//---------------------------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);
			homePage.searchPanel.setAdditionalConditions(dataPool.get("IntegerProperty"), dataPool.get("Condition"), dataPool.get("NonIntegerValue"));
			homePage.searchPanel.clickSearch(); 

			Log.message("Step-2 : Integer property and non-integer value value is selected in advanced search.");

			//Verification : To Verify M-Files message dialog appears
			//-------------------------------------------------------
			if (!MFilesDialog.exists(driver)) //Verifies the existence of M-Files Dialog
				throw new SkipException("M-Files dialog did not appear.");

			MFilesDialog mfilesDialog = new MFilesDialog(driver); //Instantiating MFilesDialog wrapper class //Instantiating M-Files dialog

			//Verifies detailed error message is hidden by default
			if (mfilesDialog.getMessage().equalsIgnoreCase("Enter an integer."))
				Log.pass("Test case Passed. 'Enter an integer.' error message appeared on entering non-integer value.");
			else
				Log.fail("Test case Failed. 'Enter an integer.' error message does not appeared on entering non-integer value.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest22_4_4


	/**
	 * 22.5.1 : Verify with any value other than date value in the advanced search bar for 'Accessed by me' property
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint22", "PropertiesInAdvancedSearch"}, 
			description = "Verify with any value other than date value in the advanced search bar for 'Accessed by me' property")
	public void SprintTest22_5_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Expand the advanced Search 
			//-----------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Expand the advanced Search.");

			//Step-2: Set the property and the invalid value
			//-----------------------------------------------
			try {

				homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), dataPool.get("Value"));

				Log.message("Step-2: Set the property and the invalid value");
			}

			//Verification: To verify if the invalid values cant be set for date property type
			//--------------------------------------------------------------------------------
			catch (Exception e) {
				WebElement adSearchBar = driver.findElement(By.cssSelector("div[id='searchAdvancedConditions']" +
						">div[class='ddlSearchAdvanced searchRow']"));
				WebElement searchTextBox = adSearchBar.findElement(By.cssSelector("div[class='searchValueTypeHolder advancedSearchConditionDiv']>div, " + "div[class='searchValueTypeHolder advancedSearchConditionDiv']>input"));

				if(!searchTextBox.getAttribute("value").equals(dataPool.get("Value")))
					Log.pass("Test Case Passed. Values other than date cant be entered to the Value field for " + dataPool.get("Property"));
				else
					Log.fail("Test Case Failed. Values other than date can be entered to the Value field for " + dataPool.get("Property"), driver);

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
	 * 22.5.2 : Verify to remove value in the property field
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint22", "PropertiesInAdvancedSearch","Remove value in property field"}, 
			description = "Verify to remove value in the property field")
	public void SprintTest22_5_2(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 
		ConcurrentHashMap <String, String> dataPool = null;

		try {



			driver = WebDriverUtils.getDriver();

			dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//Pre-requisite : Launch Driver and Login to MFWA
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//Step-1: Expand the advanced Search 
			//-----------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("Step-1: Expand the advanced Search.");

			//Step-2: Set the property and try to clear the value
			//----------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), "");
			WebElement adSearchBar = driver.findElement(By.cssSelector("div[id='searchAdvancedConditions']>div[class='ddlSearchAdvanced searchRow']"));
			WebElement searchTextBox = adSearchBar.findElement(By.cssSelector("div[class='searchValueTypeHolder advancedSearchConditionDiv']>div, " + "div[class='searchValueTypeHolder advancedSearchConditionDiv']>input"));

			try {
				searchTextBox.clear();
				Log.message("Step-2: Set the property and the invalid value");
			}

			//Verification: To verify if the values cant be emptied  for date property type
			//--------------------------------------------------------------------------------
			catch (Exception e) {

				if(e.getClass().toString().contains("org.openqa.selenium.InvalidElementStateException") && !searchTextBox.getAttribute("readonly").equals("readonly") && !searchTextBox.getAttribute("value").equals(""))
					Log.pass("Test Case Passed. The value field of the property cannot be empty. The field is read-only");
				else
					Log.fail("Test Case Failed. The value field is not empty.", driver);

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
	 * 47.2.1 : User should able to type values in the Value box of advanced Search
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint47", "PropertiesInAdvancedSearch"}, 
			description = "User should able to type values in the Value box of advanced Search.")
	public void SprintTest47_2_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		//Pre-requisite : Launch Driver and Login to MFWA
		//-----------------------------------------------
		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Click on the Advanced Search link
			//-------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("1. Click on the Advanced Search link.");

			//2. Enter the property and the condition
			//----------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), dataPool.get("Value"));
			Utils.fluentWait(driver);

			Log.message("2. Enter the property and the condition.");

			//Verification : To verify if the value field is editable in Advanced Search
			//---------------------------------------------------------------------------
			if (homePage.searchPanel.getAdditionalConditions().equals(dataPool.get("Property")+":"+dataPool.get("Condition")+":"+dataPool.get("Value")))
				Log.pass("Test case Passed. The Value field in the advanced Search was editable."); 
			else
				Log.fail("Test case Failed. Unable to set value in the Value field of Advanced Search.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest47_2_1


	/**
	 * 47.2.21 : Verify to select 'IS NOT' condition for 'Assignment' property
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint47", "PropertiesInAdvancedSearch"}, 
			description = "Verify to select 'IS NOT' condition for 'Assignment' property.")
	public void SprintTest47_2_21(HashMap<String,String> dataValues, String driverType) throws Exception {

		//Pre-requisite : Launch Driver and Login to MFWA
		//-----------------------------------------------
		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Click on the Advanced Search link
			//-------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("1. Click on the Advanced Search link.");

			//2. Enter the property and the condition
			//----------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), "");
			Utils.fluentWait(driver);

			Log.message("2. Enter the property and the condition.");

			//Verification : To verify if the value field is editable in Advanced Search
			//---------------------------------------------------------------------------
			if (homePage.searchPanel.getAdditionalConditions().equals(dataPool.get("Property")+":"+dataPool.get("Condition")+":"))
				Log.pass("Test case Passed. The Value field in the advanced Search was editable."); 
			else
				Log.fail("Test case Failed. Unable to set value in the Value field of Advanced Search.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest22_2_4

	/**
	 * 47.2.26A : Verify the third dropdown displayed for selected property with 'IS' condition
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint47", "PropertiesInAdvancedSearch"}, 
			description = "Verify the third dropdown displayed for selected property with 'IS' condition.")
	public void SprintTest47_2_26A(HashMap<String,String> dataValues, String driverType) throws Exception {

		//Pre-requisite : Launch Driver and Login to MFWA
		//-----------------------------------------------
		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Click on the Advanced Search link
			//-------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("1. Click on the Advanced Search link.");

			//2. Enter the property and the condition
			//----------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), "");
			Utils.fluentWait(driver);

			Log.message("2. Enter the property and the condition.");

			//Verification : To verify if the value field appears
			//----------------------------------------------------
			if (homePage.searchPanel.isValueFieldDisplayed(1))
				Log.pass("Test case Passed. The Value field was displayed as expected."); 
			else
				Log.fail("Test case Failed. The value field was not displayed.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest47_2_26A

	/**
	 * 47.2.26B : Verify the third dropdown displayed for selected property with 'IS' condition
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint47", "PropertiesInAdvancedSearch"}, 
			description = "Verify the third dropdown displayed for selected property with 'IS' condition.")
	public void SprintTest47_2_26B(HashMap<String,String> dataValues, String driverType) throws Exception {

		//Pre-requisite : Launch Driver and Login to MFWA
		//-----------------------------------------------
		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Click on the Advanced Search link
			//-------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("1. Click on the Advanced Search link.");

			//2. Enter the property and the condition
			//----------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), "");
			Utils.fluentWait(driver);

			Log.message("2. Enter the property and the condition.");

			//Verification : To verify if the value field appears
			//----------------------------------------------------
			if (!homePage.searchPanel.isValueFieldDisplayed(1))
				Log.pass("Test case Passed. The Value field was displayed as expected."); 
			else
				Log.fail("Test case Failed. The value field was not displayed.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest47_2_26B

	/**
	 * 47.2.28 : Verify the removed value displayed in third dropdown
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint47", "PropertiesInAdvancedSearch"}, 
			description = "Verify the removed value displayed in third dropdown.")
	public void SprintTest47_2_28(HashMap<String,String> dataValues, String driverType) throws Exception {

		//Pre-requisite : Launch Driver and Login to MFWA
		//-----------------------------------------------
		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Click on the Advanced Search link
			//-------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("1. Click on the Advanced Search link.");

			//2. Enter the property and the condition
			//----------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), dataPool.get("Value"));
			Utils.fluentWait(driver);

			Log.message("2. Enter the property and the condition.");

			//3. Remove the value set in the value field
			//-------------------------------------------
			homePage.searchPanel.setAdvancedSearchValue("");
			Utils.fluentWait(driver);

			Log.message("3. Remove the value set in the value field");

			//Verification : To verify if the value field appears
			//----------------------------------------------------
			if(!homePage.searchPanel.getAdditionalConditions().contains(dataPool.get("Value")))
				Log.pass("Test case Passed. The value in the Value field was removed as expected."); 
			else
				Log.fail("Test case Failed. The value field was displayed.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest47_2_28

	/**
	 * 47.2.29 : Verify the values displayed upon performing 'Advanced search' with invalid value in third dropdown
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint47", "PropertiesInAdvancedSearch"}, 
			description = "Verify the values displayed upon performing 'Advanced search' with invalid value in third dropdown.")
	public void SprintTest47_2_29(HashMap<String,String> dataValues, String driverType) throws Exception {

		//Pre-requisite : Launch Driver and Login to MFWA
		//-----------------------------------------------
		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Click on the Advanced Search link
			//-------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("1. Click on the Advanced Search link.");

			//2. Enter the property and the condition
			//----------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), "");
			Utils.fluentWait(driver);

			Log.message("2. Enter the property and the condition.");

			//3. Set an invalie value
			//------------------------
			homePage.searchPanel.typeInAdvancedSearchValue(dataPool.get("Value"));
			Utils.fluentWait(driver);

			Log.message("3. Set an invalid value");

			//Verification : To verify if the value field appears
			//----------------------------------------------------
			String value = driver.findElement(By.cssSelector("div[id*='searchPropertyCriterion_']")).findElement(By.cssSelector("div[class*='searchValueTypeHolder']")).findElement(By.cssSelector("div[class*='ffb']")).getText().replaceAll("\n", "").replaceAll("\u00A0"," ").trim();

			if(value.replaceAll("&nbsp;", " ").trim().equalsIgnoreCase("No matches"))
				Log.pass("Test case Passed. The value in the Value field was removed as expected."); 
			else
				Log.fail("Test case Failed. The value field was displayed.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest47_2_29

	/**
	 * 47.2.30 : Verify the values displayed in the third dropdown when the condition is changed from 'IS EMPTY' to 'IS'
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint47", "PropertiesInAdvancedSearch"}, 
			description = "Verify the values displayed in the third dropdown when the condition is changed from 'IS EMPTY' to 'IS'.")
	public void SprintTest47_2_30(HashMap<String,String> dataValues, String driverType) throws Exception {

		//Pre-requisite : Launch Driver and Login to MFWA
		//-----------------------------------------------
		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			//1. Click on the Advanced Search link
			//-------------------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("1. Click on the Advanced Search link.");

			//2. Enter the property and the condition
			//----------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition1"), "");
			Utils.fluentWait(driver);

			Log.message("2. Enter the property and the condition.");

			//3. Change the condition field value
			//------------------------------------
			homePage.searchPanel.setAdditionalConditions("", dataPool.get("Condition2"), "");
			Utils.fluentWait(driver);

			Log.message("3. 2. Change the condition field value.");

			//3. Click the drop down box in the value field
			//----------------------------------------------
			List<String> availableValues = homePage.searchPanel.getAdvancedSearchValues();

			Log.message("3. Click the drop down box in the value field.");

			//Verification : To Verify if the expected values are listed
			//-----------------------------------------------------------
			String[] expectedValues = dataPool.get("Values").split("\n");

			if(expectedValues.length != availableValues.size())
				throw new Exception("The Number of expected values were not listed in the drop down.");

			for(int count = 0; count < expectedValues.length; count++) {
				if(availableValues.indexOf(expectedValues[count]) == -1)
					Log.fail("Test Case Failed. Expected Value - " + expectedValues[count] + " was not listed in the drop down.", driver);
			}

			Log.pass("Test case Passed. All the expected values were listed in the drop down.");

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest47_2_30

	/**
	 * 47.2.35 : Mark for Archiving as Advanced Search condition
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = { "Sprint47", "PropertiesInAdvancedSearch"}, 
			description = "Mark for Archiving as Advanced Search condition.")
	public void SprintTest47_2_35(HashMap<String,String> dataValues, String driverType) throws Exception {

		//Pre-requisite : Launch Driver and Login to MFWA
		//-----------------------------------------------
		driver = null; 
		HomePage homePage = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Set the condition
			//---------------------
			homePage = LoginPage.launchDriverAndLogin(driver, true);

			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), dataPool.get("Value"));
			Utils.fluentWait(driver);

			Log.message("1. Set the condition.");

			//2. Select 'Search all objects' and click the Search button
			//------------------------------------------------------------
			homePage.searchPanel.search("", Caption.Search.SearchAllObjects.Value);
			Utils.fluentWait(driver);

			Log.message("2. Select 'Search all objects' and click the Search button.");

			//Verification: To verify if the objects marked for archiving are displayed
			//--------------------------------------------------------------------------
			homePage.listView.insertColumn(dataPool.get("Property"));
			Utils.fluentWait(driver);
			int itemCount = homePage.listView.itemCount();
			int count = 0;
			for(count = 0; count < itemCount; count++) {
				if(!homePage.listView.getColumnValueByItemIndex(count, dataPool.get("Property")).equals(dataPool.get("Value"))) {
					homePage.listView.removeColumn(dataPool.get("Property"));
					Log.fail("Test Case Failed. The Objects that did not match the condition were also listed.", driver);
				}
			}

			if(count == itemCount) {
				homePage.listView.removeColumn(dataPool.get("Property"));
				Log.pass("Test Case Passed. Only objects that match the search condition were listed.");
			}
			else {
				homePage.listView.removeColumn(dataPool.get("Property"));
				Log.fail("Test Case Failed. Verification was not successful.", driver);
			}

		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally
	} //End SprintTest47_2_35

	/**
	 * 58.2.18 : Search an object using it's comment
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint58", "Comment","PropertiesInAdvancedSearch"}, 
			description = "Search an object using it's comment.")
	public void SprintTest58_2_18(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Search for an object and open it's metadatacard
			//---------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");

			homePage.taskPanel.clickItem(Caption.MenuItems.Properties.Value);
			Utils.fluentWait(driver);
			MetadataCard metadataCard = new MetadataCard(driver);

			Log.message("2. Search for an object and open it's metadatacard");

			//3. Set the Comment and save
			//----------------------------
			metadataCard.setComments(dataPool.get("Comment"));
			metadataCard.saveAndClose();
			Utils.fluentWait(driver);

			Log.message("3. Set the Comment and save.");

			//4. Search using the comment
			//----------------------------
			homePage.searchPanel.search(dataPool.get("Comment"), "Search only: " + dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			Log.message("4. Search using the comment.");

			//Verification: To verify if the object is listed
			//-----------------------------------------------
			if(homePage.listView.isItemExists(dataPool.get("Object")))
				Log.pass("Test Case Passed. Comment was set to the object through metadatacard.");
			else
				Log.fail("Test Case Failed. Comment was not set to the object through metadatacard.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}



	/**
	 * 105.5.1 : Check whether existing multiline property is displayed in advanced search dropdown list
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint100", "Comment","PropertiesInAdvancedSearch"}, 
			description = "Check whether existing multiline property is displayed in advanced search dropdown list.")
	public void SprintTest105_5_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Expand Advanced Search
			//--------------------------
			homePage.searchPanel.clickAdvancedSearch(true);
			Utils.fluentWait(driver);

			Log.message("2. Expand Advanced Search.");

			//Verification: To verify if the given property exists in the advanced Search property list
			//------------------------------------------------------------------------------------------
			if(homePage.searchPanel.isPropertyExists(dataPool.get("Property")))
				Log.pass("Test Case Passed. The property exists in the property field of advanced Search.");
			else
				Log.fail("Test Case Failed. The property does not exist in the property field of advanced Search.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105.5.2 : Check user is able to search the records using multiline property
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint100", "Comment","PropertiesInAdvancedSearch"}, 
			description = "Check user is able to search the records using multiline property.")
	public void SprintTest105_5_2(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Set some value to a multi-line text property
			//------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");

			Utils.fluentWait(driver);

			MetadataCard metadataCard = new MetadataCard(driver, true);
			metadataCard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));
			metadataCard.saveAndClose();

			Log.message("2. Set some value to a multi-line text property.");

			//3. Set the advanced search condition using the multi-line property
			//--------------------------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), dataPool.get("Value").split("\n")[0]);
			homePage.searchPanel.search("", "Search only: " + dataPool.get("ObjectType"));

			Log.message("3. Set the advanced search condition using the multi-line property.");

			//Verification: To verify if the Object is listed in the search result
			//---------------------------------------------------------------------
			if(homePage.listView.isItemExists(dataPool.get("Object")))
				Log.pass("Test Case Passed. Search using multi-line text property works as expected.");
			else
				Log.fail("Test Case Failed. Search using multi-line text property works as expected.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105.5.3 : Check user is able to search the records using 2 multiline properties
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint100", "Comment","PropertiesInAdvancedSearch"}, 
			description = "Check user is able to search the records using 2 multiline properties.")
	public void SprintTest105_5_3(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Set some value to a multi-line text property
			//------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");

			Utils.fluentWait(driver);

			MetadataCard metadataCard = new MetadataCard(driver, true);
			metadataCard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));
			metadataCard.setComments(dataPool.get("Comment"));
			metadataCard.saveAndClose();

			Log.message("2. Set some value to a multi-line text property.");

			//3. Set the advanced search condition using multiple multi-line property
			//------------------------------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), dataPool.get("Value").split("\n")[0]);
			homePage.searchPanel.setAdditionalConditions("Comment", dataPool.get("Condition"), dataPool.get("Comment"), 2);
			homePage.searchPanel.search("", "Search only: " + dataPool.get("ObjectType"));

			Log.message("3. Set the advanced search condition using multiple multi-line property.");

			//Verification: To verify if the Object is listed in the search result
			//---------------------------------------------------------------------
			if(homePage.listView.isItemExists(dataPool.get("Object")))
				Log.pass("Test Case Passed. Search using multi-line text property works as expected.");
			else
				Log.fail("Test Case Failed. Search using multi-line text property works as expected.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105.5.4 : Changing the order of multiline properties conditions in Advanced Search does not change the search result
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint100", "Comment","PropertiesInAdvancedSearch"}, 
			description = "Changing the order of multiline properties conditions in Advanced Search does not change the search result.")
	public void SprintTest105_5_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Set some value to a multi-line text property
			//------------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");

			Utils.fluentWait(driver);

			MetadataCard metadataCard = new MetadataCard(driver, true);
			metadataCard.setPropertyValue(dataPool.get("Property"), dataPool.get("Value"));
			metadataCard.setComments(dataPool.get("Comment"));
			metadataCard.saveAndClose();

			Log.message("2. Set some value to a multi-line text property.");

			//3. Set the advanced search condition using multiple multi-line property
			//------------------------------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), dataPool.get("Value").split("\n")[0]);
			homePage.searchPanel.setAdditionalConditions("Comment", dataPool.get("Condition"), dataPool.get("Comment"), 2);
			homePage.searchPanel.search("", "Search only: " + dataPool.get("ObjectType"));

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new Exception("The Expected object was not listed in the search result.");

			int expectedCount = homePage.listView.itemCount();

			Log.message("3. Set the advanced search condition using multiple multi-line property.");

			//4. Change the order of the condition and perform the search
			//------------------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property"), dataPool.get("Condition"), dataPool.get("Value").split("\n")[0], 2);
			homePage.searchPanel.setAdditionalConditions("Comment", dataPool.get("Condition"), dataPool.get("Comment"), 1);
			homePage.searchPanel.search("", "Search only: " + dataPool.get("ObjectType"));

			Log.message("4. Change the order of the condition and perform the search");

			//Verification: To verify if the Object is listed in the search result
			//---------------------------------------------------------------------
			if(homePage.listView.isItemExists(dataPool.get("Object")) && expectedCount == homePage.listView.itemCount())
				Log.pass("Test Case Passed. Search using multi-line text property works as expected.");
			else
				Log.fail("Test Case Failed. Search using multi-line text property works as expected.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105.5.5 : Changing the order of conditions in Advanced Search does not change the search result
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint100", "Comment","PropertiesInAdvancedSearch"}, 
			description = "Changing the order of conditions in Advanced Search does not change the search result.")
	public void SprintTest105_5_5(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Set some values to different properties
			//-------------------------------------------
			homePage.searchPanel.search(dataPool.get("Object"), "Search only: " + dataPool.get("ObjectType"));
			Utils.fluentWait(driver);

			if(!homePage.listView.clickItem(dataPool.get("Object")))
				throw new SkipException("Invalid Test Data. The Specified object was not found in the Search results.");

			Utils.fluentWait(driver);

			MetadataCard metadataCard = new MetadataCard(driver, true);
			metadataCard.setPropertyValue(dataPool.get("Property1"), dataPool.get("Value1"));
			metadataCard.setPropertyValue(dataPool.get("Property2"), dataPool.get("Value2"));
			metadataCard.saveAndClose();

			Log.message("2. Set some values to different properties.");

			//3. Set the advanced search condition using multiple multi-line property
			//------------------------------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property1"), dataPool.get("Condition1"), dataPool.get("Value1"));
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property2"), dataPool.get("Condition2"), dataPool.get("Value2"), 2);
			homePage.searchPanel.search("", "Search only: " + dataPool.get("ObjectType"));

			if(!homePage.listView.isItemExists(dataPool.get("Object")))
				throw new Exception("The Expected object was not listed in the search result.");

			int expectedCount = homePage.listView.itemCount();

			Log.message("3. Set the advanced search condition using multiple multi-line property.");

			//4. Change the order of the condition and perform the search
			//------------------------------------------------------------
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property1"), dataPool.get("Condition1"), dataPool.get("Value1"), 2);
			homePage.searchPanel.setAdditionalConditions(dataPool.get("Property2"), dataPool.get("Condition2"), dataPool.get("Value2"), 1);
			homePage.searchPanel.search("", "Search only: " + dataPool.get("ObjectType"));

			Log.message("4. Change the order of the condition and perform the search");

			//Verification: To verify if the Object is listed in the search result
			//---------------------------------------------------------------------
			if(homePage.listView.isItemExists(dataPool.get("Object")) && expectedCount == homePage.listView.itemCount())
				Log.pass("Test Case Passed. Search using multi-line text property works as expected.");
			else
				Log.fail("Test Case Failed. Search using multi-line text property works as expected.", driver);
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}


	/**
	 * 105.5.7 : Adding more than 5 conditions and performing search
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint100", "Comment"}, 
			description = "Adding more than 5 conditions and performing search.")
	public void SprintTest105_5_7(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the Home View.
			//----------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);

			Log.message("1. Logged into the Home View.");

			//2. Set the advanced search condition using multiple property
			//-------------------------------------------------------------
			String[] conditions = dataPool.get("Conditions").split("\n");
			for(int count = 0; count < conditions.length; count++) 
				homePage.searchPanel.setAdditionalConditions(conditions[count].split(":")[0], conditions[count].split(":")[1], conditions[count].split(":")[2], count+1);

			homePage.searchPanel.search(dataPool.get("SearchWord"), dataPool.get("SearchType"));

			Log.message("2. Set the advanced search condition using multiple property.");

			//Verification: To verify if the expected objects are listed in the search results
			//---------------------------------------------------------------------------------
			if(dataPool.get("Objects").equals("")) {
				if(homePage.listView.itemCount() == 0)
					Log.pass("Test Case Passed. As expected no objects were listed in the search result.");
				else
					Log.fail("Test Case Failed. Objects were listed when empty search result is expected.", driver);
			}
			else {
				String[] objects = dataPool.get("Objects").split("\n");

				for(int count = 0; count < objects.length; count++) {
					if(!homePage.listView.isItemExists(objects[count]))
						Log.fail("Test Case Failed. An expected object " + objects[count] + " was not listed in the search result.", driver);
				}
				Log.pass("Test Case Passed. The Expected objects were listed in the search result.");
			}
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

	/**
	 * 105.5.8 : Check if  advanced search using multiline property yields search results in 'Assigned to me' view
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint100", "Comment","PropertiesInAdvancedSearch"}, 
			description = "Check if  advanced search using multiline property yields search results in 'Assigned to me' view.")
	public void SprintTest105_5_8(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			//1. Login to the vault and navigate to any view.
			//-----------------------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);
			Utils.fluentWait(driver);
			homePage.listView.navigateThroughView(dataPool.get("View"));
			Utils.fluentWait(driver);

			Log.message("1. Login to the vault and navigate to any view..");

			//2. Set the advanced search condition using multiple property
			//-------------------------------------------------------------
			String[] conditions = dataPool.get("Conditions").split("\n");
			for(int count = 0; count < conditions.length; count++) 
				homePage.searchPanel.setAdditionalConditions(conditions[count].split(":")[0], conditions[count].split(":")[1], conditions[count].split(":")[2], count+1);

			homePage.searchPanel.search(dataPool.get("SearchWord"), dataPool.get("SearchType"));

			Log.message("2. Set the advanced search condition using multiple property.");

			//Verification: To verify if the expected objects are listed in the search results
			//---------------------------------------------------------------------------------
			if(dataPool.get("Objects").equals("")) {
				if(homePage.listView.itemCount() == 0)
					Log.pass("Test Case Passed. As expected no objects were listed in the search result.");
				else
					Log.fail("Test Case Failed. Objects were listed when empty search result is expected.", driver);
			}
			else {
				String[] objects = dataPool.get("Objects").split("\n");

				for(int count = 0; count < objects.length; count++) {
					if(!homePage.listView.isItemExists(objects[count]))
						Log.fail("Test Case Failed. An expected object " + objects[count] + " was not listed in the search result.", driver);
				}
				Log.pass("Test Case Passed. The Expected objects were listed in the search result.");
			}
		}

		catch(Exception e) {
			Log.exception(e, driver);
		}

		finally {
			Utility.quitDriver(driver);
		}
	}

}


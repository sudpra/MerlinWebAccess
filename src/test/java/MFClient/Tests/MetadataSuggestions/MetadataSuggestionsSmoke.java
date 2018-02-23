package MFClient.Tests.MetadataSuggestions;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.TestMethodWebDriverManager;

import org.openqa.selenium.WebDriver;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import MFClient.Pages.HomePage;
import MFClient.Pages.LoginPage;
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.MetadataSuggestions;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class MetadataSuggestionsSmoke {

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

	protected TestMethodWebDriverManager driverManager = null;

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

			String metadataSuggestionAppName = xmlParameters.getParameter("MetadataSuggestionsApp");
			Utility.installVaultApplication(metadataSuggestionAppName);

			String[] metadataProviderNames = xmlParameters.getParameter("MetadataProviderName").split(",");
			String[] configurationFiles = xmlParameters.getParameter("MetadataProviderConfigurationFile").split(",");

			for(int i = 0; i < metadataProviderNames.length; ++i){
				Utility.configureMetadataProvider(metadataProviderNames[i], configurationFiles[i]);
			}
		}
		catch(Exception e) {
			if (e.getClass().toString().contains("NullPointerException")) 
				throw new Exception ("Test data sheet does not exists.");
			else
				throw e;
		}

	}

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

			Utility.destroyUsers(xlTestDataWorkBook);//Destroys the user in MFServer
			Utility.destroyTestVault();//Destroys the Vault in MFServer

		}//End try

		catch(Exception e){
			throw e;
		}//End Catch
	}//End cleanApp


	protected MetadataSuggestions triggerMetadataSuggestionsInRightPane(WebDriver driver, HomePage homePage, String objectName) throws Exception{

		Log.message("Selecting object '" + objectName + "' to open its metadata card in right pane");
		homePage.listView.clickItem(objectName);

		MetadataCard metadataCard = new MetadataCard(driver, true);
		metadataCard.metadataSuggestions.clickAnalyzeButton();

		return metadataCard.metadataSuggestions;
	}

	protected MetadataSuggestions triggerMetadataSuggestionsInPopoutMDCard(WebDriver driver, HomePage homePage, String objectName) throws Exception{

		Log.message("Opening context menu of object '" + objectName + "' to open its popout metadata card");
		homePage.listView.rightClickItem(objectName);
		homePage.listView.clickContextMenuItem("Properties");

		MetadataCard metadataCard = new MetadataCard(driver, false);
		metadataCard.metadataSuggestions.clickAnalyzeButton();

		return metadataCard.metadataSuggestions;

	}

	protected String verifyMSLUPropertyContainsValues(MetadataCard metadataCard, String property, String[] expectedValues) throws Exception{

		//TODO: Required to check that no extra values exist in the property? For example, expectedValues.length == actualPropertyValues.size()?

		String result = "";

		ArrayList<String> actualPropertyValues = metadataCard.getPropertyValues(property);

		for(int i = 0; i < expectedValues.length; ++i){

			String expectedValue = expectedValues[i];

			if(!actualPropertyValues.contains(expectedValue))
				result += "Expected metadata value '"+ expectedValue + "' was not added to text property '" + property + "' property in the metadata card when multiple suggestions were selected. ";

		}

		return result;
	}

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Select to add metadata suggestion to a property that already exists on the metadata card of the object.", priority = 1)
	public void ExistingPropertyAddSuggestion(HashMap<String,String> dataPool, String driverType) throws Exception {

		WebDriver driver = null; 

		try {
			//Test data
			String searchWord_TD = dataPool.get("SearchWord");
			String objectName_TD = dataPool.get("Object");
			String property_TD = dataPool.get("Property");
			String suggestion_TD = dataPool.get("Suggestion");

			//Test start
			driver = driverManager.startTesting(Utility.getMethodName());

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			homePage.searchPanel.quickSearch(searchWord_TD);
			MetadataSuggestions metadataSuggestions = triggerMetadataSuggestionsInRightPane(driver, homePage, objectName_TD);

			MetadataCard metadataCard = metadataSuggestions.selectSuggestedPropertyValue(property_TD, suggestion_TD);

			String addedPropertyValueBeforeSaving = metadataCard.getPropertyValue(property_TD);

			if(!addedPropertyValueBeforeSaving.equals(suggestion_TD))
				throw new Exception("Suggested metadata value '"+ suggestion_TD + "' was not added to the '" + property_TD + "' property in the metadata card.");

			metadataCard.saveAndClose();

			//Check that value is still there after saving
			homePage.taskPanel.clickItem("Properties");
			metadataCard = new MetadataCard(driver, false);

			String addedPropertyValueAfterSaving = metadataCard.getPropertyValue(property_TD);

			if(addedPropertyValueAfterSaving.equals(suggestion_TD))
				Log.pass("Test Case passed. Selected metadata suggestion '"+ suggestion_TD + "' was succesfully added to the existing property '" + property_TD + "' in the metadata card.");
			else
				Log.fail("Test case failed. Selected metadata suggestion '"+ suggestion_TD + "' was not saved to the existing property '" + property_TD + "'.", driver);

		}//End try
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Select to add suggested property (that does not already exist on metadata card) and select suggested value to the added property", priority = 2)
	public void AddSuggestedPropertyAndValue(HashMap<String,String> dataPool, String driverType) throws Exception {

		WebDriver driver = null; 

		try {
			//Test data
			String searchWord_TD = dataPool.get("SearchWord");
			String objectName_TD = dataPool.get("Object");
			String property_TD = dataPool.get("Property");
			String suggestion_TD = dataPool.get("Suggestion");

			//Test start
			driver = driverManager.startTesting(Utility.getMethodName());

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			homePage.searchPanel.quickSearch(searchWord_TD);
			MetadataSuggestions metadataSuggestions = triggerMetadataSuggestionsInRightPane(driver, homePage, objectName_TD);

			metadataSuggestions.selectSuggestedProperty(property_TD);
			MetadataCard metadataCard = metadataSuggestions.selectSuggestedPropertyValue(property_TD, suggestion_TD);

			String addedPropertyValueBeforeSaving = metadataCard.getPropertyValue(property_TD);

			if(!addedPropertyValueBeforeSaving.equals(suggestion_TD))
				throw new Exception("Suggested metadata value '"+ suggestion_TD + "' was not added to the '" + property_TD + "' property in the metadata card.");

			metadataCard.saveAndClose();

			//Check that value is still there after saving
			homePage.taskPanel.clickItem("Properties");
			metadataCard = new MetadataCard(driver, false);

			String addedPropertyValueAfterSaving = metadataCard.getPropertyValue(property_TD);

			if(addedPropertyValueAfterSaving.equals(suggestion_TD))
				Log.pass("Test Case passed. Selected metadata suggestion '"+ suggestion_TD + "' and suggested property '" + property_TD + "' were succesfully added in the metadata card.");
			else
				Log.fail("Test case failed. Selected metadata suggestion '"+ suggestion_TD + "' was not saved to the added property '" + property_TD + "'.", driver);
		}
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally

	}


	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Selected suggestions are separated by comma when multiple suggested values are selected to text property.", priority = 3)
	public void TextPropSelectManySuggestions(HashMap<String,String> dataPool, String driverType) throws Exception {

		WebDriver driver = null; 

		try {
			//Test data
			String searchWord_TD = dataPool.get("SearchWord");
			String objectName_TD = dataPool.get("Object");
			String property_TD = dataPool.get("Property");
			String[] suggestions_TD = dataPool.get("Suggestions").split("\n");
			String expectedPropertyValue_TD = dataPool.get("ExpectedValue");

			//Test start
			driver = driverManager.startTesting(Utility.getMethodName());

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			homePage.searchPanel.quickSearch(searchWord_TD);
			MetadataSuggestions metadataSuggestions = triggerMetadataSuggestionsInPopoutMDCard(driver, homePage, objectName_TD);

			metadataSuggestions.selectSuggestedProperty(property_TD);
			MetadataCard metadataCard = metadataSuggestions.selectMultipleSuggestedPropertyValues(property_TD, suggestions_TD);

			String addedPropertyValueBeforeSaving = metadataCard.getPropertyValue(property_TD);

			if(!addedPropertyValueBeforeSaving.equals(expectedPropertyValue_TD))
				throw new Exception("Expected metadata value '"+ expectedPropertyValue_TD + "' was not added to text property '" + property_TD + "' in the metadata card when multiple suggestions were selected.");

			metadataCard.saveAndClose();

			//Check that value is still there after saving
			metadataCard = new MetadataCard(driver, true);

			String addedPropertyValueAfterSaving = metadataCard.getPropertyValue(property_TD);

			if(addedPropertyValueAfterSaving.equals(expectedPropertyValue_TD))
				Log.pass("Test case passed. Expected metadata value '"+ expectedPropertyValue_TD + "' was added to text property '" + property_TD + "' when multiple suggestions were selected.");
			else
				Log.fail("Test case failed. Expected metadata value '"+ expectedPropertyValue_TD + "' was not saved to the text property '" + property_TD + "' when multiple suggestions were selected.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Selected suggestions are added to property values when multiple suggested values are selected to MSLU (multiselect lookup) property.", priority = 4)
	public void MSLUPropSelectManySuggestions(HashMap<String,String> dataPool, String driverType) throws Exception {

		WebDriver driver = null; 

		try {
			//Test data
			String searchWord_TD = dataPool.get("SearchWord");
			String objectName_TD = dataPool.get("Object");
			String property_TD = dataPool.get("Property");
			String[] suggestions_TD = dataPool.get("Suggestions").split("\n");

			//Test start
			driver = driverManager.startTesting(Utility.getMethodName());

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			homePage.searchPanel.quickSearch(searchWord_TD);
			MetadataSuggestions metadataSuggestions = triggerMetadataSuggestionsInPopoutMDCard(driver, homePage, objectName_TD);

			metadataSuggestions.selectSuggestedProperty(property_TD);
			MetadataCard metadataCard = metadataSuggestions.selectMultipleSuggestedPropertyValues(property_TD, suggestions_TD);

			String result = "";

			result += verifyMSLUPropertyContainsValues(metadataCard, property_TD, suggestions_TD);

			if(!result.equals(""))
				throw new Exception("Adding property values to MSLU property did not work as expected. Additional info: " + result);

			metadataCard.saveAndClose();

			//Check that value is still there after saving
			metadataCard = new MetadataCard(driver, true);

			result += verifyMSLUPropertyContainsValues(metadataCard, property_TD, suggestions_TD);

			if(result.equals(""))
				Log.pass("Test case passed. Expected metadata values were added to MSLU property '" + property_TD + "' when multiple suggestions were selected.");
			else
				Log.fail("Test case failed. Additional info: " + result, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}


	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Select metadata suggestions to multiple different properties", priority = 5)
	public void SelectSuggestionsToManyProps(HashMap<String,String> dataPool, String driverType) throws Exception {

		WebDriver driver = null; 

		try {
			//Test data
			String searchWord_TD = dataPool.get("SearchWord");
			String objectName_TD = dataPool.get("Object");
			String[] properties_TD = dataPool.get("Properties").split("\n");
			String[] suggestions_TD = dataPool.get("Suggestions").split("\n");

			//Test start
			driver = driverManager.startTesting(Utility.getMethodName());

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			homePage.searchPanel.quickSearch(searchWord_TD);
			MetadataSuggestions metadataSuggestions = triggerMetadataSuggestionsInRightPane(driver, homePage, objectName_TD);

			metadataSuggestions.selectMultipleSuggestedProperties(properties_TD);
			MetadataCard metadataCard = null;

			for(int i = 0; i < properties_TD.length; ++i){

				String property = properties_TD[i];
				String suggestion = suggestions_TD[i];

				metadataCard = metadataSuggestions.selectSuggestedPropertyValue(property, suggestion);

				//TODO: Question: This test case is missing verification of property values before saving. That is probably OK.
			}

			metadataCard.saveAndClose();

			//Check that values are still there after saving
			homePage.taskPanel.clickItem("Properties");
			metadataCard = new MetadataCard(driver, false);

			String result = "";

			for(int j = 0; j < properties_TD.length; ++j){

				String property = properties_TD[j];
				String suggestion = suggestions_TD[j];

				String addedPropertyValueAfterSaving = metadataCard.getPropertyValue(property);

				if(!addedPropertyValueAfterSaving.equals(suggestion)){
					result += "Selected metadata suggestion '"+ suggestion + "' was not saved to the property '" + property + "'.";
				}
			}

			if(result.equals(""))
				Log.pass("Test case passed. Expected metadata values were added to multiple properties.");
			else
				Log.fail("Test case failed. Additional info: " + result, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}


	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Not selecting any received suggestions to any property.", priority = 6)
	public void NotSelectingAnySuggestions(HashMap<String,String> dataPool, String driverType) throws Exception {

		WebDriver driver = null; 

		try{
			//Test data
			String searchWord_TD = dataPool.get("SearchWord");
			String objectName_TD = dataPool.get("Object");
			String modifyProp_TD = dataPool.get("ModifyProperty");
			String modifyValue_TD = dataPool.get("ModifyValue");
			String[] properties_TD = dataPool.get("Properties").split("\n");
			String[] propValues_TD = dataPool.get("ExpectedValues").split("\n");

			//Test start
			driver = driverManager.startTesting(Utility.getMethodName());

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			homePage.searchPanel.quickSearch(searchWord_TD);
			MetadataCard metadataCard = triggerMetadataSuggestionsInPopoutMDCard(driver, homePage, objectName_TD).getMetadataCard();

			Log.message("Setting value '" + modifyValue_TD + "' to property '" + modifyProp_TD + "'" );
			metadataCard.setPropertyValue(modifyProp_TD, modifyValue_TD);

			metadataCard.saveAndClose();

			metadataCard = new MetadataCard(driver, true);

			String result = "";

			for(int i = 0; i < properties_TD.length; ++i){

				String property = properties_TD[i];
				String expectedValue = propValues_TD[i];

				String propertyValueAfterSaving = metadataCard.getPropertyValue(property);

				if(propertyValueAfterSaving.equals("")){

					//Setting "-" character to variable if the property has empty value so that it can be compared with expected value in test data. 
					//Test data marks empty property values as "-"
					propertyValueAfterSaving = "-";
				}

				if(!propertyValueAfterSaving.equals(expectedValue)){
					result += "The property '"+ property + "' contains unexpected value '" + propertyValueAfterSaving + "' instead of expected value '" + expectedValue +"' after saving changes without selecting any metadata suggestions.";
				}
			}

			if(result.equals(""))
				Log.pass("Test case passed. Properties contained expected values when any of the received metadata suggestions were not selected.");
			else
				Log.fail("Test case failed. Additional info: " + result, driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}


	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Metadata suggestion for new value in a value list that is based on an object type.", priority = 7)
	public void NewObjectSuggested(HashMap<String,String> dataPool, String driverType) throws Exception {

		WebDriver driver = null; 

		try{	
			//Test data
			String searchWord_TD = dataPool.get("SearchWord");
			String objectName_TD = dataPool.get("Object");
			String property_TD = dataPool.get("Property");
			String suggestion_TD = dataPool.get("Suggestion");
			String relationshipText_TD = dataPool.get("RelationshipText");

			//Test start
			driver = driverManager.startTesting(Utility.getMethodName());

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			homePage.searchPanel.quickSearch(searchWord_TD);
			MetadataSuggestions metadataSuggestions = triggerMetadataSuggestionsInRightPane(driver, homePage, objectName_TD);

			metadataSuggestions.selectSuggestedProperty(property_TD);
			MetadataCard newObjMetadataCard = metadataSuggestions.selectNewObjectSuggestedPropertyValue(property_TD, suggestion_TD);

			//Creates the object based on the metadata suggestion
			newObjMetadataCard.saveAndClose();

			MetadataCard originalObjMetadataCard = new MetadataCard(driver, true);

			originalObjMetadataCard.savePropValue(property_TD);

			String createdNewObjValueBeforeSaving = originalObjMetadataCard.getPropertyValue(property_TD);

			if(!createdNewObjValueBeforeSaving.equals(suggestion_TD))
				throw new Exception("Suggested new object metadata value '"+ suggestion_TD + "' was not added to the '" + property_TD + "' property in the metadata card after creating the object.");

			//Saves the original object with the selected metadata suggestion
			originalObjMetadataCard.saveAndClose();

			originalObjMetadataCard = new MetadataCard(driver, true);

			String createdNewObjValueAfterSaving = originalObjMetadataCard.getPropertyValue(property_TD);

			if(!createdNewObjValueAfterSaving.equals(suggestion_TD))
				throw new Exception("Suggested new object metadata value '"+ suggestion_TD + "' was not added to the '" + property_TD + "' property in the metadata card after saving.");


			Log.message("Expanding relationships of object '" + objectName_TD + "' to see that object '" + suggestion_TD + "' was created based on the suggestion to property '" + property_TD + "'");
			homePage.listView.expandItemByName(objectName_TD);

			homePage.listView.expandRelations(relationshipText_TD);

			if(homePage.listView.isItemExists(suggestion_TD))
				Log.pass("Selected metadata suggestion '"+ suggestion_TD + "' was succesfully added to the property '" + property_TD + "' and a new object was created of the suggested value.");
			else
				Log.fail("Test case failed. The newly created object '"+ suggestion_TD + "' based on the metadata suggestion to property '" + property_TD + "' was not found in the expanded relationships of the object '" + objectName_TD + "'", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}


	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Metadata suggestion for new value in a value list.", priority = 8)
	public void NewValuelistValueSuggested(HashMap<String,String> dataPool, String driverType) throws Exception {

		WebDriver driver = null; 

		try{
			//Test data
			String searchWord_TD = dataPool.get("SearchWord");
			String objectName_TD = dataPool.get("Object");
			String property_TD = dataPool.get("Property");
			String suggestion_TD = dataPool.get("Suggestion");

			//Test start
			driver = driverManager.startTesting(Utility.getMethodName());

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			homePage.searchPanel.quickSearch(searchWord_TD);
			MetadataSuggestions metadataSuggestions = triggerMetadataSuggestionsInPopoutMDCard(driver, homePage, objectName_TD);

			metadataSuggestions.selectSuggestedProperty(property_TD);
			MetadataCard metadataCard = metadataSuggestions.selectNewSuggestedValueListPropertyValue(property_TD, suggestion_TD);

			String createdPropertyValueBeforeSaving = metadataCard.getPropertyValue(property_TD);

			if(!createdPropertyValueBeforeSaving.equals(suggestion_TD))
				throw new Exception("Suggested new value list value '"+ suggestion_TD + "' was not added to the '" + property_TD + "' property in the metadata card after creating the value.");

			metadataCard.saveAndClose();

			metadataCard = new MetadataCard(driver, true);
			String createdPropertyValueAfterSaving = metadataCard.getPropertyValue(property_TD);

			if(createdPropertyValueAfterSaving.equals(suggestion_TD))
				Log.pass("Test case passed. Selected metadata suggestion '"+ suggestion_TD + "' was succesfully added to the property '" + property_TD + "' and a new value list value was created of the suggested value.");
			else
				Log.fail("Test case failed. Selected metadata suggestion '"+ suggestion_TD + "' was not added to the property '" + property_TD + "' as a new value list value.", driver);


		}
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}
}

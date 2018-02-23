package MFClient.Tests.MetadataSuggestions;

import genericLibrary.DataProviderUtils;
import genericLibrary.Log;

import java.util.HashMap;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import MFClient.Pages.HomePage;
import MFClient.Pages.LoginPage;
import MFClient.Wrappers.ExternalRepositoryHelper;
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.MetadataSuggestions;
import MFClient.Wrappers.Utility;

public class MetadataSuggestionsSmokeExternalRepository extends MetadataSuggestionsSmoke{

	@BeforeClass (alwaysRun=true)
	public void setupExternalRepository() throws Exception{

		ExternalRepositoryHelper.installExternalRepository(className, xlTestDataWorkBook);

	}

	@AfterClass (alwaysRun = true)
	public void cleanExternalRepository() throws Exception{
		try{
			Utility.clearExternalRepository(className);//Clears the external repository created for this class
		}
		catch(Exception e){
			throw e;
		}

	}

	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader",groups = {},
			description = "Unmanaged object's class is automatically changed to default class of managed object, when analysis is done.", priority = 99)
	public void UnmanagedObjAutomaticSetClass(HashMap<String,String> dataPool, String driverType) throws Exception {

		WebDriver driver = null; 

		try {
			//Test data
			String searchWord_TD = dataPool.get("SearchWord");
			String objectName_TD = dataPool.get("Object");
			String expectedClass_TD = dataPool.get("Class");
			String property_TD = dataPool.get("Property");
			String suggestion_TD = dataPool.get("Suggestion");

			//Test start
			driver = driverManager.startTesting(Utility.getMethodName());

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true);

			homePage.searchPanel.quickSearch(searchWord_TD);

			MetadataSuggestions metadataSuggestions = triggerMetadataSuggestionsInPopoutMDCard(driver, homePage, objectName_TD);

			MetadataCard metadataCard = metadataSuggestions.getMetadataCard();

			Log.message("Checking the class of object '" + objectName_TD + "'");

			String actualClass = metadataCard.getPropertyValue("Class");

			if(!actualClass.equals(expectedClass_TD))
				throw new Exception("The class of the unmanaged object was not changed to '" + expectedClass_TD + "' after the metadata suggestions analysis.");

			metadataSuggestions.selectSuggestedProperty(property_TD);
			metadataSuggestions.selectSuggestedPropertyValue(property_TD, suggestion_TD);

			metadataCard.saveAndClose();

			metadataCard = new MetadataCard(driver, true);

			String classValueAfterSaving = metadataCard.getPropertyValue("Class");

			if(classValueAfterSaving.equals(expectedClass_TD))
				Log.pass("Test case passed. The class of the unmanaged object was changed to '" + expectedClass_TD + "' after receiving and saving metadata suggestions.");
			else
				Log.fail("Test case failed. The class of the unmanaged object was '" + classValueAfterSaving + "' instead of changing to '" + expectedClass_TD + "' after receiving and saving metadata suggestions.", driver);

		}
		catch (Exception e) {
			Log.exception(e, driver);
		}//End catch
		finally {
			Utility.quitDriver(driver);
		}//End finally
	}


}

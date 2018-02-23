package MFClient.Wrappers;

import genericLibrary.Log;
import genericLibrary.Utils;
import genericLibrary.WebDriverUtils;

import org.openqa.selenium.WebDriver;
import org.testng.Reporter;
import org.testng.xml.XmlTest;

import MFClient.Pages.HomePage;
import MFClient.Pages.LoginPage;

public class ExternalRepositoryHelper {

	public static void installExternalRepository(String className, String xlTestDataWorkBook) throws Exception{

		XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();

		String extnViewName = xmlParameters.getParameter("ExternalViewName");
		String extRepoAppName = xmlParameters.getParameter("ExternalRepositoryApp");

		Utility.setExternalRepository(extnViewName, className);

		//Setting full control permission to external repository
		Utility.setFullControlToExternalRepoWindowsACLPermission(className);

		//Setting file permissions to individual files and folders by using Windows ACLs
		Utility.setWindowsACLPermission(className, xlTestDataWorkBook);

		Utility.installVaultApplication(extRepoAppName);
		Utility.setFileShareNamedValue(className);

		//Promote some objects for the tests to use
		Utility.setupPromoteObjects(xlTestDataWorkBook);

		if (!Utility.isExternalViewExists(xmlParameters.getParameter("ExternalRepositoryDisplayName")))
			throw new Exception("isExternalViewExists : " + xmlParameters.getParameter("ExternalRepositoryDisplayName") + " is not exist in the home view.");
	}


	public static void userSpecificLogin() throws Exception{

		WebDriver webDriver = null;

		try{

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();

			// External repository information from xml
			String repoUsername = xmlParameters.getParameter("ExternalRepositoryTestUserUsername");
			String repoUserPassword = xmlParameters.getParameter("ExternalRepositoryTestUserPassword");
			String repoName = xmlParameters.getParameter("ExternalRepositoryDisplayName");

			System.out.println("ExternalRepositoryHelper.userSpecificLogin logging in to external repository '" + repoName + "'");

			webDriver = WebDriverUtils.getDriver();

			HomePage homePage = LoginPage.launchDriverAndLogin(webDriver, false);

			//Open external repositories dialog
			homePage.menuBar.ClickUserInfo("External Repositories");
			MFilesDialog mfdialog = new MFilesDialog(webDriver);

			//Login to external repository and close dialog
			mfdialog.loginToExternalRepository(repoUsername, repoUserPassword, repoName);

			//Navigate to external repository folder
			homePage.listView.doubleClickItem(repoName);


			//Now verifying that log in to external repository was successful
			//Open external repositories dialog
			homePage.menuBar.ClickUserInfo("External Repositories");
			mfdialog = new MFilesDialog(webDriver);

			//Get the logged in status of external repository
			String loggedInAs = mfdialog.getLoggedInStatusOfExternalRepository(repoName);

			if(!loggedInAs.equals("Logged in as " + repoUsername))
				throw new Exception("User " + repoUsername + " not logged in to external repository " + repoName);


		}
		catch(Exception e){
			throw new Exception("Exception in ExternalRepositoryHelper.userSpecificLogin " + e);
		}
		finally{

			if(webDriver != null){
				webDriver.quit();
			}
		}
	}



	/**
	 * verifyExternalRepositoryDocumentObjects : Verifies that given document objects in external repository contain expected metadata and that filedata can
	 * be accessed by using preview. The documents should exist in the current view.
	 * @param webDriver - WebDriver object
	 * @param homePage - HomePage object 
	 * @param expectedObjects - Names of expected objects in String array
	 * @param expectedRepositories - Expected Repository property values of objects, in the same order as expectedObjects
	 * @param expectedLocations - Expected Location property values of objects, in the same order as expectedObjects
	 * @param expectedPageCounts - Expected number of pages in the documents (verified by using preview), in the same order as expectedObjects
	 * @return Informative result string of any errors that are detected in the verification. Empty String returned if everything is as expected
	 * @throws Exception 
	 */
	public static String verifyExternalRepositoryDocumentObjects(WebDriver webDriver, HomePage homePage,
			String[] expectedObjects,
			String[] expectedRepositories,
			String[] expectedLocations,
			String[] expectedPageCounts) throws Exception{


		String result = "";

		try{

			Log.message("Verifying that documents can be accessed by using preview and that they have correct metadata", webDriver);

			//Going through all expected objects
			for (int i = 0; i < expectedObjects.length; i++)
			{
				String objectName = expectedObjects[i];
				String repositoryName = expectedRepositories[i];
				String location = expectedLocations[i];
				String pageCount = expectedPageCounts[i];

				result += verifyMetadata(webDriver, homePage, objectName, repositoryName, location);
				result += verifyPreview(webDriver, homePage, objectName, pageCount);

			}
			return result;
		}
		catch(Exception e){
			throw new Exception("Exception in verifyExternalRepositoryObjects : " + e, e);
		}
	}

	/**
	 * verifyExternalRepositoryFolderObjects : Verifies that given folder objects in external repository contain expected metadata and that 
	 * the folders can be accessed. The folders should exist in the current view.
	 * @return Informative result string of any errors that are detected in the verification. Empty String returned if everything is as expected
	 * @throws Exception 
	 */
	public static String verifyExternalRepositoryFolderObjects(WebDriver webDriver, HomePage homePage,
			String[] expectedObjects,
			String[] expectedRepositories,
			String[] expectedLocations,
			String[] expectedControlObjects,
			String[] expectedObjectCountsInFolder) throws Exception{

		String result = "";

		try{

			Log.message("Verifying that folders can be accessed and that they have correct metadata", webDriver);

			//Going through all expected objects
			for (int i = 0; i < expectedObjects.length; i++)
			{
				String folderName = expectedObjects[i];
				String repositoryName = expectedRepositories[i];
				String location = expectedLocations[i];
				String controlObject = expectedControlObjects[i];
				int objectCountInFolder = Integer.parseInt(expectedObjectCountsInFolder[i]);

				//These verifications have to be in this order because our double click wrapper method does not work with already selected objects
				result += verifyFolderAccess(webDriver, homePage, folderName, controlObject, objectCountInFolder);
				result += verifyMetadata(webDriver, homePage, folderName, repositoryName, location);

			}
		}
		catch(Exception e){
			throw new Exception("Exception in verifyExternalRepositoryFolderObjects : " + e, e);
		}

		return result;
	}

	/**
	 * verifyMetadata : Selects the object and verifies basic metadata of objects in external repository. 
	 * The checked properties are Location and Repository.
	 * @return Informative result string of any errors that are detected in the verification. Empty String returned if everything is as expected
	 * @throws Exception 
	 */
	public static String verifyMetadata(WebDriver webDriver, HomePage homePage,
			String expectedObject,
			String expectedRepository,
			String expectedLocation) throws Exception{

		String result = "";
		try{

			if(!homePage.listView.clickItem(expectedObject)) {
				result += "'Expected object not found in listing: " + expectedObject + "' ";
			}

			Log.message("Verifying object '" + expectedObject + "' metadata: Will check Repository and Location properties.");

			//Right pane metadata card
			MetadataCard metadatacard = new MetadataCard(webDriver, true);

			//Get values of properties
			String actualRepository = metadatacard.getPropertyValue("Repository");
			String actualLocation = metadatacard.getPropertyValue("Location");

			//Expected location is received as "-" from test data if it is empty
			if(expectedLocation.equals("-"))
				expectedLocation = "";
			
			result += verifyPropertyValue(expectedObject, "Repository", expectedRepository, actualRepository);
			result += verifyPropertyValue(expectedObject, "Location", expectedLocation, actualLocation);

		}
		catch(Exception e){
			throw new Exception("Exception in verifyMetadata : " + e, e);
		}
		return result;
	}

	/**
	 * verifyPreview : Verifies that object's preview contains expected number of pages. The object should be already selected when calling this.
	 * @return Informative result string of any errors that are detected in the verification. Empty String returned if everything is as expected
	 * @throws Exception 
	 */
	public static String verifyPreview(WebDriver webDriver, HomePage homePage, String objectName, String expectedPageCount) throws Exception{

		String result = "";

		try{

			homePage.previewPane.clickPreviewTab();

			Log.message("Verifying object '" + objectName + "' preview: Will check the displayed page count of the document.");

			String actualPageCount = homePage.previewPane.getPreviewDocumentPageCount();

			//Verify that page count of preview document is as expected
			if(!actualPageCount.equals(expectedPageCount))
				result += "'Page count in preview for object " + objectName + ", Expected: " + expectedPageCount + " vs. Actual: " + actualPageCount +  "' ";

			//Select metadata tab again so that next object will start from correct tab
			homePage.previewPane.clickMetadataTab();
		}
		catch(Exception e){
			throw new Exception("Exception in verifyPreview : " + e, e);
		}

		return result;
	}

	/**
	 * verifyFolderAccess : Verifies that folder object can be accessed by entering the view by checking existence of an control object and
	 * number of objects in the folder. After verification in the folder, browser returns to the view where the folder was accessed from.
	 * @return Informative result string of any errors that are detected in the verification. Empty String returned if everything is as expected
	 * @throws Exception 
	 */
	public static String verifyFolderAccess(WebDriver webDriver, HomePage homePage, String folderName, String controlObject, int expectedCount) throws Exception{

		String result = "";

		try{

			//Go to the folder
			homePage.listView.doubleClickItem(folderName);

			Utils.fluentWait(webDriver);

			Log.message("Verifying folder " + folderName + " content: Will check existence of control object '" + controlObject + "' and expected object count " + expectedCount, webDriver);


			//TODO: verify breadcrump? -> that does not seem to be shown at the moment if navigating from search results..


			//Verify that the folder contains the expected control object
			if(!homePage.listView.isItemExists(controlObject)){
				result += "'Control object " + controlObject + " was not found in folder " + folderName + " after attempting to open the folder' ";
			}

			int actualObjectCountInFolder = homePage.listView.itemCount();

			//Verify that the folder contains expected number of objects
			if(actualObjectCountInFolder != expectedCount){
				result += "'Object count inside folder " + folderName + ", Expected: " + expectedCount + " vs. Actual: " + actualObjectCountInFolder + "' ";
			}

			//Return to the view where the folder was entered from
			webDriver.navigate().back();

		}
		catch(Exception e){
			throw new Exception("Exception in verifyFolderAccess : " + e, e);
		}
		return result;
	}


	/**
	 * verifyPropertyValue : Helper method for verifying if two property values (String) match. Will return informative message that can used in 
	 * test's output if the values do not match.
	 * @param objectName - Name of the object, will only be used in informative output and NOT used in verification
	 * @param propertyName - Name of  the property, will only be used in informative output and NOT used in verification
	 * @param expected - Expected value used in verification
	 * @param actual - Actual value used in verification
	 * @return Informative string if the expected and actual values do not match. If they match, returns empty String.
	 */
	public static String verifyPropertyValue(String objectName, String propertyName, String expected, String actual){

		if(!expected.equals(actual))
			return "'" + propertyName  + " property for object " + objectName + ", Expected: " + expected + " vs. Actual: " + actual + "' ";
		else
			return "";
	}

	/**
	 * promoteObject : Selects the given object from listview and promotes it by setting its class and giving it an additional
	 * property value. The promotion is done through right pane metadata card.
	 * @param webDriver - WebDriver object
	 * @param objectName - Name of the object
	 * @param homePage - HomePage object 
	 * @param objClass - Class that will be given to the object during promotion
	 * @param propertyName - Name of the property that will be given a value during promotion
	 * @param propertyValue - Value that will be given to the 'propertyName' property
	 * @throws Exception 
	 */
	public static void promoteObject(WebDriver webDriver, String objectName, HomePage homePage,
			String objClass,
			String propertyName,
			String propertyValue) throws Exception{

		try{

			//Select object
			homePage.listView.clickItem(objectName);

			Log.message("Promoting object '" + objectName + "' by setting class '" + objClass +"' and property '" + propertyName + "' with value '" + propertyValue + "'");

			//Metadata card is opened
			MetadataCard metadatacard = new MetadataCard(webDriver, true);

			//Setting Class property to promote the object and set some additional property
			metadatacard.setPropertyValue("Class", objClass);
			metadatacard.setPropertyValue(propertyName, propertyValue );
			metadatacard.saveAndClose();
		}
		catch(Exception e){
			throw new Exception("Exception in promoteObject : " + e, e);
		}
	}

	/**
	 * verifyPromotedObject : Verifies that given promoted object contains expected metadata, including that it receives object ID and 
	 * version. This method expects that the object is already selected and it will pop out the object's metadata card.
	 * @param objectName - Name of the object
	 * @param homePage - HomePage object 
	 * @param expectedClass - Expected class of promoted object
	 * @param propertyName - Name of the property to be verified
	 * @param expectedPropertyValue - Value of property to be verified
	 * @param expectedLocation - Expected value of Location property
	 * @param expectedRepository - Expected value of Repository property
	 * @return Informative result string of any errors that are detected in the verification. Empty String returned if everything is as expected
	 * @throws Exception 
	 */
	public static String verifyPromotedObject(WebDriver webDriver, String objectName, HomePage homePage,
			String expectedClass,
			String propertyName,
			String expectedPropertyValue,
			String expectedLocation,
			String expectedRepository) throws Exception{

		String result = "";

		//Pop out metadata card of the promoted object
		homePage.taskPanel.clickItem("Properties");
		MetadataCard metadatacard = new MetadataCard(webDriver);

		Log.message("Verifying promoted object " + objectName + ": Will check Class, Repository, Location, and " + propertyName + " properties, version and ID.", webDriver);

		//Verify promoted object's class, an additional property, Repository, and Location properties
		String actualClass = metadatacard.getPropertyValue("Class");
		String actualPropertyValue = metadatacard.getPropertyValue(propertyName);
		String actualLocation = metadatacard.getPropertyValue("Location");
		String actualRepository = metadatacard.getPropertyValue("Repository");

		result += verifyPropertyValue(objectName, "Class", expectedClass, actualClass);
		result += verifyPropertyValue(objectName, propertyName, expectedPropertyValue, actualPropertyValue);
		result += verifyPropertyValue(objectName, "Repository", expectedRepository, actualRepository);
		result += verifyPropertyValue(objectName, "Location", expectedLocation, actualLocation);

		//Verify that promoted object has version
		String actualVersion = Integer.toString(metadatacard.getVersion());

		result += verifyPropertyValue(objectName, "Version", "1", actualVersion);

		//Verify that promoted object has ID
		int actualObjectID = metadatacard.getObjectID();

		if(actualObjectID < 0)
			result +=  "'The ID of " + objectName +" was not set properly when promoted, actual ID: " + actualObjectID + "'";

		return result;
	}

	public static String verifyPropertyNotEditable(MetadataCard metadatacard, String propertyName) throws Exception{

		String result = "";

		metadatacard.clickProperty(propertyName);

		Log.message("Verifying that '" + propertyName + "' property cannot be modified", metadatacard.driver);

		if(metadatacard.isPropertyInEditMode(propertyName))
			result+= "'" + propertyName + "' property can be modified even though the mapped user does not have write permission to the object' ";

		return result;
	}
}

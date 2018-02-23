package MFClient.Wrappers;

import genericLibrary.ActionEventUtils;
import genericLibrary.Log;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;

public class MetadataSuggestions {

	@FindBy(how=How.CSS,using="button[class*='mf-analyze-button']")
	private WebElement analyzeButton;

	@FindBy(how=How.CSS,using="div[class*='mf-analyze-status']")
	private WebElement analyzeStatus;

	@FindBy(how=How.CSS,using="div#mf-property-suggestions")
	private WebElement propertySuggestions;

	private WebDriver driver;
	private MetadataCard metadataCard;

	private final String ANALYSIS_STATUS_COMPLETE = "Analysis complete";
	private final String ANALYSIS_STATUS_NO_RESULTS = "No suggestions available";

	public MetadataSuggestions(final WebDriver driver, MetadataCard mdCard) throws Exception {
		this.driver = driver;
		this.metadataCard = mdCard;
		PageFactory.initElements(this.driver, this);
	}

	private WebElement getSuggestionsRow(String property) throws Exception{

		//The row element which contains the property
		WebElement propertyElement = metadataCard.getPropertyElement(property).findElement(By.xpath("../../.."));

		//The row element which contains the suggestions. It is the second sibling element of the property element
		WebElement suggestionsRow = propertyElement.findElement(By.xpath("following-sibling::tr[2]"));

		return suggestionsRow;
	}

	private WebElement getSuggestionValueButtonElement(WebElement suggestionsRowElement, String suggestion){

		//The clickable button element which contains the suggested value for the property
		WebElement suggestionButtonElement = suggestionsRowElement.findElement(By.xpath(".//div[@class='mf-value-suggestion-value-content' and normalize-space(.) ='" + suggestion + "']"));
		return suggestionButtonElement;
	}

	private WebElement getSuggestedPropertyButtonElement(String property){

		WebElement suggestedPropertyButtonElement = propertySuggestions.findElement(By.xpath(".//div[@class='mf-property-suggestion-value-content' and normalize-space(.) ='" + property + "']"));
		return suggestedPropertyButtonElement;
	}

	/**
	 * clickAnalyzeButton : Clicks the analyze button on the metadata card in order to receive metadata suggestions.
	 */
	public void clickAnalyzeButton() throws Exception{

		try{
			metadataCard.switchFrame();

			Log.message("Clicking 'Analyze' button in the metadata card.");

			ActionEventUtils.click(this.driver, analyzeButton);

			String analyzeStatusText = analyzeStatus.getText();
			int snooze = 0;

			//Waiting for the analysis to be finished by periodically checking the status text or until timeout happens. 
			//Analysis is finished when the result of analysis is displayed in the status text field. 
			while(snooze < 15 && !analyzeStatusText.equals(ANALYSIS_STATUS_COMPLETE) && !analyzeStatusText.equals(ANALYSIS_STATUS_NO_RESULTS)){
				Thread.sleep(1000);
				analyzeStatusText = analyzeStatus.getText();
				snooze++;
			}

			if(snooze >=  15)
				throw new Exception("Metadata analysis was not completed and it was timeout.");

			Log.message("Analysis finished.", this.driver);
		}
		catch(Exception e){
			throw new Exception("Exception in MetadataSuggestions.clickAnalyzeButton " + e.getMessage());
		}
		finally{
			metadataCard.switchToDefaultContent();
		}

	}

	/**
	 * selectSuggestedPropertyValue : Selects metadata suggestion to specified property. The property, where the suggestion is given, is required to be 
	 * already added to the metadata card (it does not matter how the property has been added).
	 * @param property - Name of the property
	 * @param suggestion - The suggestion value that will be selected
	 * @return Metadata Card object where the metadata suggestion was selected
	 */
	public MetadataCard selectSuggestedPropertyValue(String property, String suggestion) throws Exception {

		try{
			metadataCard.switchFrame();

			Log.message("Selecting suggestion value '" + suggestion + "' to property '" + property + "'");

			//Getting the element of the specific suggestion value for the specific property
			WebElement suggestionsRow = getSuggestionsRow(property);
			WebElement suggestionButtonElement = getSuggestionValueButtonElement(suggestionsRow, suggestion);

			ActionEventUtils.click(this.driver, suggestionButtonElement);

			//Switch driver to default content before calling public method of MetadataCard
			metadataCard.switchToDefaultContent();

			//Save the property value so that it is not in edit mode
			metadataCard.savePropValue(property);

			return metadataCard;

		}
		catch(Exception e){
			throw new Exception("Exception in MetadataSuggestions.selectSuggestedPropertyValue " + e.getMessage());
		}
		finally{
			metadataCard.switchToDefaultContent();
		}

	}

	/**
	 * selectMultipleSuggestedPropertyValues : Selects multiple metadata suggestions to specified property. The property, where the suggestion is given, is required to be 
	 * already added to the metadata card (it does not matter how the property has been added).
	 * @param property - Name of the property
	 * @param suggestion - String array containing the suggestion values that will be selected
	 * @return Metadata Card object where the metadata suggestion was selected
	 */
	public MetadataCard selectMultipleSuggestedPropertyValues(String property, String[] propertyValues) throws Exception{

		try{
			metadataCard.switchFrame();

			Log.message("Selecting multiple suggestions to property '" + property + "'");

			//Go through all property values
			for(int i = 0; i < propertyValues.length; ++i){

				Log.message("Selecting suggestion value '" + propertyValues[i] + "' to property '" + property + "'");

				//Getting the element of the specific suggestion value for the specific property
				WebElement suggestionsRow = getSuggestionsRow(property);
				WebElement suggestionButtonElement = getSuggestionValueButtonElement(suggestionsRow, propertyValues[i]);

				ActionEventUtils.click(this.driver, suggestionButtonElement);
			}

			//Switch driver to default content before calling public method of MetadataCard
			metadataCard.switchToDefaultContent();

			//Save the property value so that it is not in edit mode
			metadataCard.savePropValue(property);

			return metadataCard;

		}
		catch(Exception e){
			throw new Exception("Exception in MetadataSuggestions.selectMultipleSuggestedPropertyValues " + e.getMessage());
		}
		finally{
			metadataCard.switchToDefaultContent();
		}

	}

	/**
	 * selectNewObjectSuggestedPropertyValue : Selects a metadata suggestion that will prompt new object creation. This method accepts
	 * the alert message prompt and leaves the metadata card of the new object open and ready for editing.
	 * @param property - Name of the property
	 * @param suggestion - String of the suggestion value that will be selected
	 * @return Metadata Card object of the new object based on the metadata suggestion
	 */
	public MetadataCard selectNewObjectSuggestedPropertyValue(String property, String suggestion) throws Exception{

		try{
			metadataCard.switchFrame();

			Log.message("Selecting suggestion value '" + suggestion + "' to property '" + property + "'");

			//Getting the element of the specific suggestion value for the specific property
			WebElement suggestionsRow = getSuggestionsRow(property);
			WebElement suggestionButtonElement = getSuggestionValueButtonElement(suggestionsRow, suggestion);

			ActionEventUtils.click(this.driver, suggestionButtonElement);

			//Alert window appears when new value is entered for value list.
			this.driver.switchTo().alert().accept();

			MetadataCard newObjMetadataCard = new MetadataCard(driver, "New " + property);

			Log.message("Opened metadata card of new object creation, based on metadata suggestion value '" + suggestion + "' to property '" + property + "'", this.driver);

			return newObjMetadataCard;

		}
		catch(Exception e){
			throw new Exception("Exception in MetadataSuggestions.selectNewObjectSuggestedPropertyValue " + e.getMessage());
		}
		finally{
			metadataCard.switchToDefaultContent();
		}

	}

	/**
	 * selectNewSuggestedValueListPropertyValue : Selects a metadata suggestion that will prompt new value list value creation. This method accepts
	 * the alert message prompt and then accepts the name dialog, effectively choosing the suggested name for the value.
	 * @param property - Name of the property
	 * @param suggestion - String of the suggestion value that will be selected
	 * @return Metadata Card object where the metadata suggestion was selected
	 */
	public MetadataCard selectNewSuggestedValueListPropertyValue(String property, String suggestion) throws Exception {

		try{
			metadataCard.switchFrame();

			Log.message("Selecting suggestion value '" + suggestion + "' to property '" + property + "'");

			//Getting the element of the specific suggestion value for the specific property
			WebElement suggestionsRow = getSuggestionsRow(property);
			WebElement suggestionButtonElement = getSuggestionValueButtonElement(suggestionsRow, suggestion);

			ActionEventUtils.click(this.driver, suggestionButtonElement);

			Log.message("Accepting the creation of new value '" + suggestion + "'");

			//Alert window appears when new value is entered for value list.
			this.driver.switchTo().alert().accept();
			this.driver.switchTo().defaultContent();

			//Dialog for accepting to create new value to value list
			MFilesDialog dialog = new MFilesDialog(this.driver);
			dialog.clickOkButton();

			//Save the property value so that it is not in edit mode
			metadataCard.savePropValue(property);

			return metadataCard;
		}
		catch(Exception e){
			throw new Exception("Exception in MetadataSuggestions.selectNewSuggestedValueListPropertyValue " + e.getMessage());
		}
		finally{
			metadataCard.switchToDefaultContent();
		}

	}

	/**
	 * selectSuggestedProperty : Selects property suggestion. This means that the property is not yet in the metadata card but it has metadata suggestions.
	 * @param property - Name of the property
	 */
	public void selectSuggestedProperty(String property) throws Exception{

		try{

			metadataCard.switchFrame();

			Log.message("Selecting suggested property '" + property + "'");

			//Getting the element of the specific specific suggested property
			WebElement suggestedPropertyButtonElement = getSuggestedPropertyButtonElement(property);

			ActionEventUtils.click(this.driver, suggestedPropertyButtonElement);

			Log.message("Suggested property '" + property + "' was selected.", this.driver);

		}
		catch(Exception e){
			throw new Exception("Exception in MetadataSuggestions.selectSuggestedProperty " + e.getMessage());
		}
		finally{
			metadataCard.switchToDefaultContent();
		}
	}

	/**
	 * selectMultipleSuggestedProperties : Selects multiple suggested properties. This means that these properties are not yet in the metadata card but 
	 * each of them have metadata suggestions.
	 * @param properties - String array containing the names of the properties
	 */
	public void selectMultipleSuggestedProperties(String[] properties) throws Exception{

		try{
			metadataCard.switchFrame();

			for(int i = 0; i < properties.length; ++i){
				Log.message("Selecting suggested property '" + properties[i] + "'");

				//Getting the element of the specific specific suggested property
				WebElement suggestedPropertyButtonElement = getSuggestedPropertyButtonElement(properties[i]);

				ActionEventUtils.click(this.driver, suggestedPropertyButtonElement);

			}

			Log.message("Suggested properties were selected.", this.driver);

		}
		catch(Exception e){
			throw new Exception("Exception in MetadataSuggestions.selectSuggestedProperty " + e.getMessage());
		}
		finally{
			metadataCard.switchToDefaultContent();
		}

	}

	/**
	 * getMetadataCard : Gets the MetadataCard object related to this MetadataSuggestions object
	 * @return MetadataCard object
	 */
	public MetadataCard getMetadataCard(){

		return metadataCard;
	}
}
